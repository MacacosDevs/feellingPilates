package com.feelingpilates.ubicaciones;

import com.feelingpilates.TestcontainersConfiguration;
import com.feelingpilates.calendario.dto.AsignacionInstructorRequest;
import com.feelingpilates.calendario.dto.ReservaRequest;
import com.feelingpilates.calendario.dto.TurnoInstructorRequest;
import com.feelingpilates.calendario.entidad.TurnoInstructor;
import com.feelingpilates.calendario.servicio.ReservaService;
import com.feelingpilates.calendario.servicio.TurnoInstructorService;
import com.feelingpilates.exception.ConflictException;
import com.feelingpilates.exception.ValidacionException;
import com.feelingpilates.seguridad.AutorizadorSalon;
import com.feelingpilates.ubicaciones.dominio.HorarioEfectivo;
import com.feelingpilates.ubicaciones.dto.GuardarExcepcionSalonRequest;
import com.feelingpilates.ubicaciones.servicio.HorarioEfectivoSalon;
import com.feelingpilates.ubicaciones.servicio.SalonHorarioExcepcionService;
import com.feelingpilates.ubicaciones.servicio.SalonLock;
import com.feelingpilates.ubicaciones.servicio.VersionarHorarioOperacion;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Concurrencia real contra PostgreSQL para el protocolo de {@code SalonHorarioExcepcion} (F2C.2):
 * demuestra que {@code SalonLock} serializa de verdad a los tres writers que comparten la
 * invariante "todo objeto puntual de una fecha cabe en el horario efectivo de esa fecha".
 *
 * <p>Mismo metodo que {@link HorarioOperacionConcurrenciaTest}: dos transacciones independientes en
 * dos hilos, {@link TransactionTemplate} propio, coordinadas por {@link CountDownLatch}. La ventana
 * de observacion es una asercion de no-progreso, no el mecanismo de sincronizacion.
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
class SalonHorarioExcepcionConcurrenciaTest {

    private static final long VENTANA_OBSERVACION_MS = 700;
    private static final long TIMEOUT_TEST_S = 60;
    private static final short LUNES = 1;

    private static final ExecutorService POOL = Executors.newFixedThreadPool(2);

    @MockitoBean
    private AutorizadorSalon autorizadorSalon;

    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private TransactionTemplate transactionTemplate;
    @Autowired
    private SalonLock salonLock;
    @Autowired
    private SalonHorarioExcepcionService excepcionService;
    @Autowired
    private VersionarHorarioOperacion versionar;
    @Autowired
    private TurnoInstructorService turnoService;
    @Autowired
    private ReservaService reservaService;
    @Autowired
    private HorarioEfectivoSalon horarioEfectivoSalon;
    @Autowired
    private Clock reloj;

    private UUID salonId;
    private UUID actorId;
    private UUID instructorId;
    private UUID clienteId;
    private UUID actividadId;
    private LocalDate d;

    @AfterAll
    static void apagarPool() {
        POOL.shutdownNow();
    }

    @BeforeEach
    void preparar() {
        salonId = UUID.randomUUID();
        actorId = UUID.randomUUID();
        jdbcTemplate.update("""
                insert into salon (id, nombre, estado_id, municipio_id, direccion)
                values (?, ?, 22, 14, 'Calle de prueba')
                """, salonId, "Salón concurrencia excepción " + salonId);

        List<UUID> usuarios = jdbcTemplate.queryForList("select id from usuario order by id limit 2", UUID.class);
        instructorId = usuarios.get(0);
        clienteId = usuarios.get(1);
        actividadId = jdbcTemplate.queryForObject("select id from tipo_actividad limit 1", UUID.class);
        UUID rolInstructorId = jdbcTemplate.queryForObject(
                "select id from rol where nombre = 'INSTRUCTOR'", UUID.class);
        jdbcTemplate.update("""
                insert into usuario_rol (id, usuario_id, rol_id, salon_id) values (?, ?, ?, ?)
                on conflict do nothing
                """, UUID.randomUUID(), instructorId, rolInstructorId, salonId);
        jdbcTemplate.update("""
                insert into instructor_actividad (usuario_id, tipo_actividad_id) values (?, ?)
                on conflict do nothing
                """, instructorId, actividadId);

        d = proximoLunes(LocalDate.now(reloj).plusDays(30));
    }

    @AfterEach
    void limpiar() {
        jdbcTemplate.update("delete from reserva where salon_id = ?", salonId);
        jdbcTemplate.update("""
                delete from turno_instructor_asignacion
                where turno_id in (select id from turno_instructor where salon_id = ?)
                """, salonId);
        jdbcTemplate.update("""
                delete from turno_instructor_usuario
                where turno_id in (select id from turno_instructor where salon_id = ?)
                """, salonId);
        jdbcTemplate.update("delete from turno_instructor where salon_id = ?", salonId);
        jdbcTemplate.update("delete from salon_horario_excepcion where salon_id = ?", salonId);
        jdbcTemplate.update("delete from horario_operacion where salon_id = ?", salonId);
        jdbcTemplate.update("delete from usuario_rol where salon_id = ?", salonId);
        jdbcTemplate.update("delete from salon where id = ?", salonId);
    }

    // ================= C1: dos upserts, misma fecha =================

    @Test
    void dosUpsertsSimultaneosParaLaMismaFechaSeSerializanYDejanUnaSolaFilaActiva() {
        Carrera carrera = correr(
                () -> excepcionService.guardar(actorId, salonId,
                        new GuardarExcepcionSalonRequest(d, true, null, null)),
                () -> excepcionService.guardar(actorId, salonId,
                        new GuardarExcepcionSalonRequest(d, false, LocalTime.of(10, 0), LocalTime.of(16, 0))));

        assertThat(carrera.errorPrimero()).isNull();
        assertThat(carrera.errorSegundo()).isNull();
        assertThat(carrera.segundoTerminoBajoElLock()).isFalse();

        assertThat(filasActivas()).isEqualTo(1);
        // El segundo relee el estado del primero y actualiza LA MISMA fila (nunca inserta otra).
        assertThat(totalFilas()).isEqualTo(1);
        Boolean cerradoFinal = jdbcTemplate.queryForObject(
                "select cerrado from salon_horario_excepcion where salon_id = ? and fecha = ? and activo",
                Boolean.class, salonId, d);
        assertThat(cerradoFinal).isFalse();
    }

    // ================= C2: modificar vs. cancelar =================

    /**
     * Cancelar primero: el segundo (modificar) no encuentra fila activa y por Caso A crea una
     * NUEVA fila. Resultado: la vieja queda inactiva, la nueva queda activa; nunca dos activas.
     */
    @Test
    void cancelarPrimeroHaceQueModificarCreeUnaFilaNueva() {
        var creada = excepcionService.guardar(actorId, salonId,
                new GuardarExcepcionSalonRequest(d, true, null, null));

        Carrera carrera = correr(
                () -> excepcionService.eliminar(actorId, salonId, creada.id()),
                () -> excepcionService.guardar(actorId, salonId,
                        new GuardarExcepcionSalonRequest(d, false, LocalTime.of(9, 0), LocalTime.of(18, 0))));

        assertThat(carrera.errorPrimero()).isNull();
        assertThat(carrera.errorSegundo()).isNull();
        assertThat(carrera.segundoTerminoBajoElLock()).isFalse();

        assertThat(filasActivas()).isEqualTo(1);
        assertThat(totalFilas()).isEqualTo(2);
        Boolean viejaActiva = jdbcTemplate.queryForObject(
                "select activo from salon_horario_excepcion where id = ?", Boolean.class, creada.id());
        assertThat(viejaActiva).isFalse();
    }

    // ================= C3: excepcion vs. writer semanal =================

    @Test
    void excepcionVsHorarioSemanalSeSerializanYElEfectivoQuedaCoherente() {
        Carrera carrera = correr(
                () -> excepcionService.guardar(actorId, salonId,
                        new GuardarExcepcionSalonRequest(d, false, LocalTime.of(10, 0), LocalTime.of(16, 0))),
                () -> versionar.ejecutar(new VersionarHorarioOperacion.VersionarHorario(
                        salonId, LUNES, d, LocalTime.of(8, 0), LocalTime.of(20, 0))));

        assertThat(carrera.errorPrimero()).isNull();
        assertThat(carrera.errorSegundo()).isNull();
        assertThat(carrera.segundoTerminoBajoElLock()).isFalse();

        // La excepcion tiene precedencia absoluta: gana pase lo que pase con el semanal.
        HorarioEfectivo efectivo = horarioEfectivoSalon.resolver(salonId, d);
        assertThat(efectivo.vieneDeExcepcion()).isTrue();
        assertThat(efectivo.horaApertura()).isEqualTo(LocalTime.of(10, 0));
        assertThat(efectivo.horaCierre()).isEqualTo(LocalTime.of(16, 0));
    }

    // ================= C4: excepcion vs. TurnoInstructor.EXCEPCION =================

    /** §60 (analogo). El turno EXCEPCION debe tomar el lock: si no lo hiciera, este test lo detecta. */
    @Test
    void excepcionDeCierrePrimeroHaceQueElTurnoExcepcionSeaRechazado() {
        Carrera carrera = correr(
                () -> excepcionService.guardar(actorId, salonId,
                        new GuardarExcepcionSalonRequest(d, true, null, null)),
                () -> turnoService.crear(instructorId, turnoExcepcion(d, LocalTime.of(9, 0), LocalTime.of(10, 0))));

        assertThat(carrera.errorPrimero()).isNull();
        assertThat(carrera.segundoTerminoBajoElLock()).isFalse();
        assertThat(carrera.errorSegundo())
                .isInstanceOf(ValidacionException.class)
                .hasMessageContaining("cerrado");

        assertThat(contarTurnos()).isZero();
    }

    // ================= C4-inverso: TurnoInstructor.EXCEPCION gana primero =================

    /**
     * Orden inverso de C4. Con el semanal abierto (08-20 lunes), el turno EXCEPCION gana la
     * carrera y commitea primero; el writer de excepcion, al fin adquirir el lock, debe ver ese
     * turno puntual y rechazar el cierre. No basta con que las dos operaciones "terminen": el
     * estado final exige que el turno exista y la excepcion incompatible NO haya quedado aplicada.
     */
    @Test
    void turnoExcepcionPrimeroHaceQueLaExcepcionDeCierreSeaRechazada() {
        versionar.ejecutar(new VersionarHorarioOperacion.VersionarHorario(
                salonId, LUNES, d, LocalTime.of(8, 0), LocalTime.of(20, 0)));

        Carrera carrera = correr(
                () -> turnoService.crear(instructorId, turnoExcepcion(d, LocalTime.of(9, 0), LocalTime.of(10, 0))),
                () -> excepcionService.guardar(actorId, salonId,
                        new GuardarExcepcionSalonRequest(d, true, null, null)));

        assertThat(carrera.errorPrimero()).isNull();
        assertThat(carrera.segundoTerminoBajoElLock()).isFalse();
        assertThat(carrera.errorSegundo())
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("PROGRAMACION_PUNTUAL_INCOMPATIBLE_CON_EXCEPCION");

        assertThat(contarTurnos()).isEqualTo(1);
        assertThat(filasActivas()).isZero();
    }

    // ================= C5-inverso: ReservaService.crear gana primero =================

    /**
     * Orden inverso de C5. Con el semanal abierto y un turno RECURRENTE del instructor ya
     * vigente, la reserva gana la carrera y commitea CONFIRMADA primero; el writer de excepcion,
     * al fin adquirir el lock, debe ver esa reserva y rechazar el cierre. Demuestra serializacion
     * verdadera: la reserva valida existe y la excepcion incompatible NO queda aplicada.
     */
    @Test
    void reservaPrimeroHaceQueLaExcepcionDeCierreSeaRechazada() {
        // El turno RECURRENTE exige cobertura semanal SIN huecos desde hoy en adelante (no solo en
        // "d"): se versiona desde hoy, no desde "d", para que
        // validarContraHorarioSemanalVigenteHaciaElFuturo encuentre la version vigente hoy mismo.
        versionar.ejecutar(new VersionarHorarioOperacion.VersionarHorario(
                salonId, LUNES, LocalDate.now(reloj), LocalTime.of(8, 0), LocalTime.of(20, 0)));
        turnoService.crear(instructorId, turnoRecurrente(LUNES, LocalTime.of(8, 0), LocalTime.of(20, 0)));

        Carrera carrera = correr(
                () -> reservaService.crear(actorId, new ReservaRequest(
                        salonId, instructorId, clienteId, actividadId, d, LocalTime.of(9, 0))),
                () -> excepcionService.guardar(actorId, salonId,
                        new GuardarExcepcionSalonRequest(d, true, null, null)));

        assertThat(carrera.errorPrimero()).isNull();
        assertThat(carrera.segundoTerminoBajoElLock()).isFalse();
        assertThat(carrera.errorSegundo())
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("PROGRAMACION_PUNTUAL_INCOMPATIBLE_CON_EXCEPCION");

        assertThat(contarReservas()).isEqualTo(1);
        assertThat(filasActivas()).isZero();
    }

    // ================= C5: excepcion vs. ReservaService.crear =================

    @Test
    void excepcionDeCierrePrimeroHaceQueLaReservaSeaRechazada() {
        Carrera carrera = correr(
                () -> excepcionService.guardar(actorId, salonId,
                        new GuardarExcepcionSalonRequest(d, true, null, null)),
                () -> reservaService.crear(actorId, new ReservaRequest(
                        salonId, instructorId, clienteId, actividadId, d, LocalTime.of(9, 0))));

        assertThat(carrera.errorPrimero()).isNull();
        assertThat(carrera.segundoTerminoBajoElLock()).isFalse();
        assertThat(carrera.errorSegundo())
                .isInstanceOf(ValidacionException.class)
                .hasMessageContaining("fuera del horario");

        assertThat(contarReservas()).isZero();
    }

    // ================= harness (identico a HorarioOperacionConcurrenciaTest) =================

    private record Carrera(Throwable errorPrimero, Throwable errorSegundo, boolean segundoTerminoBajoElLock) {
    }

    private Carrera correr(Runnable trabajoPrimero, Runnable trabajoSegundo) {
        CountDownLatch primeroTieneElLock = new CountDownLatch(1);
        CountDownLatch segundoVaAIntentarlo = new CountDownLatch(1);
        CountDownLatch segundoTermino = new CountDownLatch(1);
        AtomicReference<Throwable> errorPrimero = new AtomicReference<>();
        AtomicReference<Throwable> errorSegundo = new AtomicReference<>();
        AtomicBoolean segundoTerminoBajoElLock = new AtomicBoolean(false);

        Future<?> primero = POOL.submit(() -> {
            try {
                transactionTemplate.executeWithoutResult(status -> {
                    salonLock.adquirir(salonId);
                    primeroTieneElLock.countDown();
                    esperar(segundoVaAIntentarlo);
                    segundoTerminoBajoElLock.set(
                            esperarConLimite(segundoTermino, VENTANA_OBSERVACION_MS));
                    trabajoPrimero.run();
                });
            } catch (Throwable t) {
                errorPrimero.set(t);
            } finally {
                primeroTieneElLock.countDown();
            }
        });

        Future<?> segundo = POOL.submit(() -> {
            try {
                esperar(primeroTieneElLock);
                transactionTemplate.executeWithoutResult(status -> {
                    jdbcTemplate.execute("set local lock_timeout = '20s'");
                    segundoVaAIntentarlo.countDown();
                    trabajoSegundo.run();
                });
            } catch (Throwable t) {
                errorSegundo.set(t);
            } finally {
                segundoVaAIntentarlo.countDown();
                segundoTermino.countDown();
            }
        });

        esperarFuture(primero);
        esperarFuture(segundo);
        return new Carrera(errorPrimero.get(), errorSegundo.get(), segundoTerminoBajoElLock.get());
    }

    private void esperar(CountDownLatch latch) {
        if (!esperarConLimite(latch, TimeUnit.SECONDS.toMillis(TIMEOUT_TEST_S))) {
            throw new IllegalStateException("El otro hilo no llegó al punto de sincronización");
        }
    }

    private boolean esperarConLimite(CountDownLatch latch, long milis) {
        try {
            return latch.await(milis, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }

    private void esperarFuture(Future<?> future) {
        try {
            future.get(TIMEOUT_TEST_S, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new IllegalStateException("La transacción no terminó a tiempo", e);
        }
    }

    // ================= fixtures =================

    private TurnoInstructorRequest turnoExcepcion(LocalDate fecha, LocalTime inicio, LocalTime fin) {
        return new TurnoInstructorRequest(
                List.of(new AsignacionInstructorRequest(instructorId, List.of(actividadId), null, null)),
                salonId,
                TurnoInstructor.Tipo.EXCEPCION,
                null,
                fecha,
                inicio,
                fin);
    }

    private TurnoInstructorRequest turnoRecurrente(short diaSemana, LocalTime inicio, LocalTime fin) {
        return new TurnoInstructorRequest(
                List.of(new AsignacionInstructorRequest(instructorId, List.of(actividadId), null, null)),
                salonId,
                TurnoInstructor.Tipo.RECURRENTE,
                diaSemana,
                null,
                inicio,
                fin);
    }

    private LocalDate proximoLunes(LocalDate desde) {
        LocalDate fecha = desde;
        while (fecha.getDayOfWeek() != DayOfWeek.MONDAY) {
            fecha = fecha.plusDays(1);
        }
        return fecha;
    }

    private int filasActivas() {
        return jdbcTemplate.queryForObject(
                "select count(*) from salon_horario_excepcion where salon_id = ? and fecha = ? and activo",
                Integer.class, salonId, d);
    }

    private int totalFilas() {
        return jdbcTemplate.queryForObject(
                "select count(*) from salon_horario_excepcion where salon_id = ? and fecha = ?",
                Integer.class, salonId, d);
    }

    private int contarTurnos() {
        return jdbcTemplate.queryForObject(
                "select count(*) from turno_instructor where salon_id = ?", Integer.class, salonId);
    }

    private int contarReservas() {
        return jdbcTemplate.queryForObject(
                "select count(*) from reserva where salon_id = ?", Integer.class, salonId);
    }
}
