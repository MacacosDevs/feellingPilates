package com.feelingpilates.ubicaciones;

import com.feelingpilates.TestcontainersConfiguration;
import com.feelingpilates.calendario.dto.AsignacionInstructorRequest;
import com.feelingpilates.calendario.dto.TurnoInstructorRequest;
import com.feelingpilates.calendario.entidad.TurnoInstructor;
import com.feelingpilates.calendario.servicio.TurnoInstructorService;
import com.feelingpilates.exception.ConflictException;
import com.feelingpilates.exception.ValidacionException;
import com.feelingpilates.programacion.servicio.BloqueProgramacionService;
import com.feelingpilates.seguridad.AutorizadorSalon;
import com.feelingpilates.ubicaciones.servicio.CerrarHorarioOperacion;
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
 * Concurrencia real contra PostgreSQL: demuestra que el lock pesimista sobre la fila {@code Salon}
 * <b>serializa de verdad</b> a todos los writers que pueden crear una incompatibilidad entre el
 * horario de un salon y su programacion.
 *
 * <p>Metodo, no sleeps: dos transacciones independientes en dos hilos, con
 * {@link TransactionTemplate} propio y coordinadas por {@link CountDownLatch}. El orden de los
 * eventos lo fijan los latches; el unico temporizador es una <b>ventana de observacion</b> con la
 * que se afirma que el segundo hilo NO progreso mientras el primero retenia el lock — es una
 * asercion de no-progreso, no el mecanismo de sincronizacion. La correccion final de cada
 * escenario se afirma ademas sobre el estado committeado, que es determinista.
 *
 * <p>Cada escenario se comprueba en los <b>dos ordenes</b>: quien llega primero commitea y quien
 * llega segundo despierta, relee el estado ya actualizado y rechaza con un error de dominio. En
 * ningun orden se alcanza el estado prohibido "horario nuevo + programacion incompatible".
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
class HorarioOperacionConcurrenciaTest {

    /**
     * Ventana durante la que el primer hilo retiene el lock y observa que el segundo no termina.
     * Sin lock, el segundo completa en pocos milisegundos, asi que un protocolo roto se detecta.
     */
    private static final long VENTANA_OBSERVACION_MS = 700;
    private static final long TIMEOUT_TEST_S = 60;
    private static final short LUNES = 1;
    private static final LocalTime OCHO = LocalTime.of(8, 0);
    private static final LocalTime NUEVE = LocalTime.of(9, 0);
    private static final LocalTime VEINTE = LocalTime.of(20, 0);

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
    private VersionarHorarioOperacion versionar;
    @Autowired
    private CerrarHorarioOperacion cerrar;
    @Autowired
    private BloqueProgramacionService bloqueService;
    @Autowired
    private TurnoInstructorService turnoService;
    @Autowired
    private Clock reloj;

    private UUID salonId;
    private UUID instructorId;
    private UUID actividadId;
    private LocalDate d;

    @AfterAll
    static void apagarPool() {
        POOL.shutdownNow();
    }

    @BeforeEach
    void preparar() {
        salonId = UUID.randomUUID();
        jdbcTemplate.update("""
                insert into salon (id, nombre, estado_id, municipio_id, direccion)
                values (?, ?, 22, 14, 'Calle de prueba')
                """, salonId, "Salón concurrencia " + salonId);

        instructorId = jdbcTemplate.queryForObject("select id from usuario limit 1", UUID.class);
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
        jdbcTemplate.update("""
                delete from turno_instructor_asignacion
                where turno_id in (select id from turno_instructor where salon_id = ?)
                """, salonId);
        jdbcTemplate.update("""
                delete from turno_instructor_usuario
                where turno_id in (select id from turno_instructor where salon_id = ?)
                """, salonId);
        jdbcTemplate.update("delete from turno_instructor where salon_id = ?", salonId);
        jdbcTemplate.update("delete from programacion_bloque where salon_id = ?", salonId);
        jdbcTemplate.update("delete from horario_operacion where salon_id = ?", salonId);
        jdbcTemplate.update("delete from usuario_rol where salon_id = ?", salonId);
        jdbcTemplate.update("delete from salon where id = ?", salonId);
    }

    // ================= Horario vs Horario =================

    /**
     * §54. Dos transacciones versionan el mismo salon/dia. La segunda espera el lock y al despertar
     * clasifica contra el estado <b>ya committeado</b> por la primera: falla con un error de
     * dominio legible, no con un {@code 23P01} crudo, y nunca quedan vigencias solapadas.
     */
    @Test
    void horarioVsHorarioSerializaYLaSegundaReevaluaElEstadoNuevo() {
        insertarHorario(null, null, OCHO, VEINTE);

        Carrera carrera = correr(
                () -> versionar.ejecutar(new VersionarHorarioOperacion.VersionarHorario(
                        salonId, LUNES, d, NUEVE, VEINTE)),
                () -> versionar.ejecutar(new VersionarHorarioOperacion.VersionarHorario(
                        salonId, LUNES, d, LocalTime.of(10, 0), VEINTE)));

        assertThat(carrera.errorPrimero()).isNull();
        assertThat(carrera.segundoTerminoBajoElLock()).isFalse();
        assertThat(carrera.errorSegundo())
                .isInstanceOf(ValidacionException.class)
                .isNotInstanceOf(ConflictException.class);
        assertThat(carrera.errorSegundo()).hasMessageContaining("YA_EXISTE_VERSION_EN_ESA_FECHA");

        // Legacy cerrada + la version de la primera. Sin solapes: lo garantiza el EXCLUDE, pero
        // aqui lo que se demuestra es que la segunda ni siquiera intento escribir.
        assertThat(horarios()).hasSize(2);
        assertThat(aperturaVigenteEn(d)).isEqualTo(NUEVE);
    }

    /**
     * §55. El caso que justifica bloquear la fila padre: un dia <b>sin ninguna fila</b> de horario.
     * Un {@code FOR UPDATE} sobre {@code horario_operacion} recorreria cero filas y no bloquearia
     * nada; las dos altas correrian en paralelo y solo el EXCLUDE las separaria, con un 23P01 en
     * vez de un error de dominio. Con el lock de {@code Salon} la segunda espera y rechaza por
     * dominio.
     */
    @Test
    void altaConcurrenteSinHorarioPrevioLaSerializaElLockDeSalon() {
        assertThat(horarios()).isEmpty();

        Carrera carrera = correr(
                () -> versionar.ejecutar(new VersionarHorarioOperacion.VersionarHorario(
                        salonId, LUNES, d, OCHO, VEINTE)),
                () -> versionar.ejecutar(new VersionarHorarioOperacion.VersionarHorario(
                        salonId, LUNES, d, NUEVE, VEINTE)));

        assertThat(carrera.errorPrimero()).isNull();
        assertThat(carrera.segundoTerminoBajoElLock()).isFalse();
        assertThat(carrera.errorSegundo())
                .isInstanceOf(ValidacionException.class)
                .isNotInstanceOf(ConflictException.class)
                .hasMessageContaining("YA_EXISTE_VERSION_EN_ESA_FECHA");
        assertThat(carrera.errorSegundo()).hasMessageNotContaining("CONFLICTO_VIGENCIA_HORARIO");

        assertThat(horarios()).hasSize(1);
        assertThat(aperturaVigenteEn(d)).isEqualTo(OCHO);
    }

    /** Contraparte de cierre: {@code Cerrar} relee DESPUES del lock y ve el futuro recien planificado. */
    @Test
    void versionarFuturoPrimeroHaceQueCerrarRechacePorVersionesFuturas() {
        insertarHorario(null, null, OCHO, VEINTE);
        LocalDate futuro = d.plusDays(28);

        Carrera carrera = correr(
                () -> versionar.ejecutar(new VersionarHorarioOperacion.VersionarHorario(
                        salonId, LUNES, futuro, NUEVE, VEINTE)),
                () -> cerrar.ejecutar(new CerrarHorarioOperacion.CerrarHorario(salonId, LUNES, d)));

        assertThat(carrera.errorPrimero()).isNull();
        assertThat(carrera.segundoTerminoBajoElLock()).isFalse();
        assertThat(carrera.errorSegundo())
                .isInstanceOf(ValidacionException.class)
                .hasMessageContaining("CIERRE_CON_VERSIONES_FUTURAS");

        // El cierre no ocurrio: la version legada sigue cerrando en futuro-1, no en d-1.
        assertThat(horarios()).hasSize(2);
        assertThat(aperturaVigenteEn(d)).isEqualTo(OCHO);
    }

    // ================= Horario vs Bloque =================

    /** §56. El horario toma el lock primero: el bloque espera y luego se valida contra el horario NUEVO. */
    @Test
    void horarioPrimeroHaceQueElBloqueIncompatibleSeaRechazado() {
        insertarHorario(null, null, OCHO, VEINTE);

        Carrera carrera = correr(
                () -> versionar.ejecutar(new VersionarHorarioOperacion.VersionarHorario(
                        salonId, LUNES, d, NUEVE, VEINTE)),
                () -> bloqueService.crearBloque(new BloqueProgramacionService.CrearBloque(
                        UUID.randomUUID(), salonId, LUNES, OCHO, NUEVE, d, null)));

        assertThat(carrera.errorPrimero()).isNull();
        assertThat(carrera.segundoTerminoBajoElLock()).isFalse();
        assertThat(carrera.errorSegundo()).isInstanceOf(ValidacionException.class);

        assertThat(contarBloques()).isZero();
        assertThat(aperturaVigenteEn(d)).isEqualTo(NUEVE);
    }

    /** §57. El bloque toma el lock primero: el versionado espera y la validacion inversa lo rechaza. */
    @Test
    void bloquePrimeroHaceQueElVersionadoIncompatibleSeaRechazado() {
        insertarHorario(null, null, OCHO, VEINTE);

        Carrera carrera = correr(
                () -> bloqueService.crearBloque(new BloqueProgramacionService.CrearBloque(
                        UUID.randomUUID(), salonId, LUNES, OCHO, NUEVE, d, null)),
                () -> versionar.ejecutar(new VersionarHorarioOperacion.VersionarHorario(
                        salonId, LUNES, d, NUEVE, VEINTE)));

        assertThat(carrera.errorPrimero()).isNull();
        assertThat(carrera.segundoTerminoBajoElLock()).isFalse();
        assertThat(carrera.errorSegundo())
                .isInstanceOf(ValidacionException.class)
                .hasMessageContaining("PROGRAMACION_INCOMPATIBLE_CON_HORARIO");

        // Politica A: el bloque no se toca y el horario NO cambia.
        assertThat(contarBloques()).isEqualTo(1);
        assertThat(horarios()).hasSize(1);
        assertThat(aperturaVigenteEn(d)).isEqualTo(OCHO);
    }

    // ================= Horario vs Turno =================

    /** §58. Mismo protocolo con {@code TurnoInstructor} RECURRENTE. */
    @Test
    void horarioPrimeroHaceQueElTurnoRecurrenteIncompatibleSeaRechazado() {
        insertarHorario(null, null, OCHO, VEINTE);

        Carrera carrera = correr(
                () -> versionar.ejecutar(new VersionarHorarioOperacion.VersionarHorario(
                        salonId, LUNES, d, NUEVE, VEINTE)),
                () -> turnoService.crear(instructorId, turnoRecurrente(OCHO, NUEVE)));

        assertThat(carrera.errorPrimero()).isNull();
        assertThat(carrera.segundoTerminoBajoElLock()).isFalse();
        assertThat(carrera.errorSegundo()).isInstanceOf(ValidacionException.class);

        assertThat(contarTurnos()).isZero();
        assertThat(aperturaVigenteEn(d)).isEqualTo(NUEVE);
    }

    /** §59. Turno primero: el versionado espera y la validacion inversa detecta el turno. */
    @Test
    void turnoRecurrentePrimeroHaceQueElVersionadoIncompatibleSeaRechazado() {
        insertarHorario(null, null, OCHO, VEINTE);

        Carrera carrera = correr(
                () -> turnoService.crear(instructorId, turnoRecurrente(OCHO, NUEVE)),
                () -> versionar.ejecutar(new VersionarHorarioOperacion.VersionarHorario(
                        salonId, LUNES, d, NUEVE, VEINTE)));

        assertThat(carrera.errorPrimero()).isNull();
        assertThat(carrera.segundoTerminoBajoElLock()).isFalse();
        assertThat(carrera.errorSegundo())
                .isInstanceOf(ValidacionException.class)
                .hasMessageContaining("TURNO_RECURRENTE");

        assertThat(contarTurnos()).isEqualTo(1);
        assertThat(horarios()).hasSize(1);
        assertThat(aperturaVigenteEn(d)).isEqualTo(OCHO);
    }

    // ================= harness =================

    private record Carrera(Throwable errorPrimero, Throwable errorSegundo, boolean segundoTerminoBajoElLock) {
    }

    /**
     * Ejecuta dos transacciones independientes con orden determinista:
     *
     * <ol>
     *   <li>el primer hilo abre transaccion y adquiere el lock de {@code Salon};</li>
     *   <li>avisa por latch; el segundo hilo abre su transaccion y avisa que va a intentarlo;</li>
     *   <li>el primero comprueba, durante una ventana acotada, que el segundo <b>no termina</b>
     *       (esta esperando el lock), hace su trabajo y commitea, liberando el lock;</li>
     *   <li>el segundo despierta, relee el estado ya committeado y decide.</li>
     * </ol>
     *
     * El trabajo del primero corre en la misma transaccion que ya tiene el lock: volver a
     * adquirirlo es reentrante dentro de la misma transaccion de PostgreSQL.
     */
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
                    // Guarda de test (§61): un protocolo roto falla en segundos en vez de colgar
                    // CI. No es configuracion productiva y no ordena nada.
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

    private TurnoInstructorRequest turnoRecurrente(LocalTime inicio, LocalTime fin) {
        return new TurnoInstructorRequest(
                List.of(new AsignacionInstructorRequest(instructorId, List.of(actividadId), null, null)),
                salonId,
                TurnoInstructor.Tipo.RECURRENTE,
                LUNES,
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

    private void insertarHorario(LocalDate desde, LocalDate hasta, LocalTime apertura, LocalTime cierre) {
        jdbcTemplate.update("""
                insert into horario_operacion
                    (id, salon_id, dia_semana, hora_apertura, hora_cierre, vigente_desde, vigente_hasta)
                values (?, ?, ?, ?, ?, ?, ?)
                """, UUID.randomUUID(), salonId, LUNES, apertura, cierre, desde, hasta);
    }

    private List<UUID> horarios() {
        return jdbcTemplate.queryForList(
                "select id from horario_operacion where salon_id = ? and dia_semana = ?",
                UUID.class, salonId, LUNES);
    }

    private LocalTime aperturaVigenteEn(LocalDate fecha) {
        return jdbcTemplate.queryForObject("""
                select hora_apertura from horario_operacion
                where salon_id = ? and dia_semana = ?
                  and (vigente_desde is null or vigente_desde <= ?)
                  and (vigente_hasta is null or vigente_hasta >= ?)
                """, LocalTime.class, salonId, LUNES, fecha, fecha);
    }

    private int contarBloques() {
        return jdbcTemplate.queryForObject(
                "select count(*) from programacion_bloque where salon_id = ?", Integer.class, salonId);
    }

    private int contarTurnos() {
        return jdbcTemplate.queryForObject(
                "select count(*) from turno_instructor where salon_id = ?", Integer.class, salonId);
    }
}
