package com.feelingpilates.programacion;

import com.feelingpilates.TestcontainersConfiguration;
import com.feelingpilates.programacion.entidad.AjusteProgramacionFecha;
import com.feelingpilates.programacion.repositorio.AjusteProgramacionFechaRepository;
import com.feelingpilates.programacion.servicio.AjusteProgramacionFechaPersistence;
import com.feelingpilates.programacion.servicio.AjusteProgramacionFechaService;
import com.feelingpilates.programacion.servicio.BloqueProgramacionService;
import com.feelingpilates.programacion.servicio.ConflictoAjusteProgramacionException;
import com.feelingpilates.programacion.servicio.ConflictoAjusteProgramacionTranslator;
import com.feelingpilates.programacion.servicio.ProgramacionErrores;
import com.feelingpilates.ubicaciones.servicio.SalonLocks;
import com.feelingpilates.usuarios.servicio.InstructorLocks;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class ProgramacionConcurrenciaTest {

    @Autowired private AjusteProgramacionFechaService ajustes;
    @Autowired private BloqueProgramacionService recurrentes;
    @Autowired private AjusteProgramacionFechaRepository repository;
    @Autowired private AjusteProgramacionFechaPersistence persistence;
    @Autowired private SalonLocks salonLocks;
    @Autowired private InstructorLocks instructorLocks;
    @Autowired private TransactionTemplate transactions;
    @Autowired private JdbcTemplate jdbc;

    private List<UUID> salones;
    private List<UUID> instructores;
    private UUID actividad;

    @BeforeEach
    void prepararMaestros() {
        salones = jdbc.queryForList("select id from salon order by id limit 2", UUID.class);
        instructores = jdbc.queryForList("select id from usuario order by id limit 2", UUID.class);
        actividad = jdbc.queryForObject(
                "select id from tipo_actividad order by id limit 1", UUID.class);
        UUID rolInstructor = jdbc.queryForObject(
                "select id from rol where nombre = 'INSTRUCTOR'", UUID.class);

        for (UUID salon : salones) {
            jdbc.update("""
                    insert into salon_tipo_actividad (salon_id, tipo_actividad_id)
                    values (?, ?) on conflict do nothing
                    """, salon, actividad);
            Integer horario = jdbc.queryForObject("""
                    select count(*) from horario_operacion
                     where salon_id = ? and dia_semana = 1
                       and (vigente_desde is null or vigente_desde <= '2029-01-01')
                       and (vigente_hasta is null or vigente_hasta >= '2029-12-31')
                    """, Integer.class, salon);
            if (horario == 0) {
                jdbc.update("""
                        insert into horario_operacion
                            (id, salon_id, dia_semana, hora_apertura, hora_cierre,
                             vigente_desde, vigente_hasta)
                        values (?, ?, 1, '08:00', '20:00', '2029-01-01', '2029-12-31')
                        """, UUID.randomUUID(), salon);
            }
        }
        for (UUID instructor : instructores) {
            jdbc.update("update usuario set estatus = 'activo' where id = ?", instructor);
            jdbc.update("""
                    insert into instructor_actividad (usuario_id, tipo_actividad_id)
                    values (?, ?) on conflict do nothing
                    """, instructor, actividad);
            jdbc.update("""
                    insert into usuario_rol (id, usuario_id, rol_id, salon_id)
                    select ?, ?, ?, null
                     where not exists (
                         select 1 from usuario_rol
                          where usuario_id = ? and rol_id = ? and salon_id is null)
                    """, UUID.randomUUID(), instructor, rolInstructor, instructor, rolInstructor);
        }
    }

    @Test
    void mismoInstructorMismoSalonSeSerializaYRechazaElSegundoSolape() throws Exception {
        competirAdicionesTraslapadas(
                LocalDate.of(2029, 1, 8), salones.get(0), salones.get(0));
    }

    @Test
    void mismoInstructorCrossSalonSeSerializaYRechazaElSegundoSolape() throws Exception {
        competirAdicionesTraslapadas(
                LocalDate.of(2029, 1, 15), salones.get(0), salones.get(1));
    }

    @Test
    void mismoTargetConcurrenteTieneUnGanadorYConstraintExacta() throws Exception {
        UUID serie = UUID.randomUUID();
        LocalDate fecha = LocalDate.of(2029, 1, 22);
        CountDownLatch inicio = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<Resultado> a = executor.submit(() -> insertarTargetConcurrente(
                    UUID.randomUUID(), serie, fecha, inicio));
            Future<Resultado> b = executor.submit(() -> insertarTargetConcurrente(
                    UUID.randomUUID(), serie, fecha, inicio));
            inicio.countDown();

            List<Resultado> resultados = List.of(
                    a.get(15, TimeUnit.SECONDS), b.get(15, TimeUnit.SECONDS));
            assertThat(resultados).filteredOn(Resultado::exito).hasSize(1);
            Throwable fallo = resultados.stream().filter(r -> !r.exito())
                    .map(Resultado::fallo).findFirst().orElseThrow();
            assertThat(fallo).isInstanceOf(ConflictoAjusteProgramacionException.class);
            ConflictoAjusteProgramacionException conflicto =
                    (ConflictoAjusteProgramacionException) fallo;
            assertThat(conflicto.getSqlState()).isEqualTo("23505");
            assertThat(conflicto.getConstraint())
                    .isEqualTo("idx_programacion_ajuste_target_activo");
            assertThat(jdbc.queryForObject("""
                    select count(*) from programacion_ajuste_fecha
                     where asignacion_serie_id = ? and fecha = ? and activo
                    """, Integer.class, serie, fecha)).isOne();
        } finally {
            executor.shutdownNow();
            jdbc.update("delete from programacion_ajuste_fecha where asignacion_serie_id = ?", serie);
        }
    }

    @Test
    void recurrenteContraAjusteConcurrenteNoPermiteDosOcurrenciasEfectivas() throws Exception {
        LocalDate fecha = LocalDate.of(2029, 2, 5);
        UUID serie = UUID.randomUUID();
        UUID bloque = insertarBloque(serie, salones.get(0), fecha);
        UUID ajusteId = UUID.randomUUID();
        CountDownLatch inicio = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<Resultado> ajuste = executor.submit(() -> ejecutar(inicio, () ->
                    ajustes.guardarAdicion(
                            ajusteId, fecha,
                            resultado(salones.get(0), instructores.get(0), 10, 12))));
            Future<Resultado> recurrente = executor.submit(() -> ejecutar(inicio, () ->
                    recurrentes.crearAsignacion(new BloqueProgramacionService.CrearAsignacion(
                            serie, bloque, instructores.get(0), actividad,
                            LocalTime.of(10, 0), LocalTime.NOON, fecha, fecha))));
            inicio.countDown();

            List<Resultado> resultados = List.of(
                    ajuste.get(15, TimeUnit.SECONDS), recurrente.get(15, TimeUnit.SECONDS));
            assertThat(resultados).filteredOn(Resultado::exito).hasSize(1);
            assertThat(resultados.stream().filter(r -> !r.exito())
                    .map(r -> r.fallo().getMessage()).findFirst().orElseThrow())
                    .containsAnyOf(
                            ProgramacionErrores.INSTRUCTOR_CON_PROGRAMACION_TRASLAPADA,
                            ProgramacionErrores.CONFLICTO_LOCK_SET_DESACTUALIZADO);
            int filas = jdbc.queryForObject(
                    "select count(*) from programacion_asignacion where serie_id = ?",
                    Integer.class, serie)
                    + jdbc.queryForObject(
                    "select count(*) from programacion_ajuste_fecha where id = ?",
                    Integer.class, ajusteId);
            assertThat(filas).isOne();
        } finally {
            executor.shutdownNow();
            jdbc.update("delete from programacion_ajuste_fecha where id = ?", ajusteId);
            jdbc.update("delete from programacion_asignacion where serie_id = ?", serie);
            jdbc.update("delete from programacion_bloque where id = ?", bloque);
        }
    }

    @Test
    void cambioDeSalonEInstructorEnOrdenInversoHaceSwapSinDeadlock() throws Exception {
        LocalDate fecha = LocalDate.of(2029, 1, 29);
        UUID ajusteA = UUID.randomUUID();
        UUID ajusteB = UUID.randomUUID();
        ajustes.guardarAdicion(
                ajusteA, fecha, resultado(salones.get(0), instructores.get(0), 9, 10));
        ajustes.guardarAdicion(
                ajusteB, fecha, resultado(salones.get(1), instructores.get(1), 11, 12));

        CountDownLatch inicio = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<Resultado> a = executor.submit(() -> ejecutar(inicio, () ->
                    ajustes.guardarAdicion(
                            ajusteA, fecha,
                            resultado(salones.get(1), instructores.get(1), 9, 10))));
            Future<Resultado> b = executor.submit(() -> ejecutar(inicio, () ->
                    ajustes.guardarAdicion(
                            ajusteB, fecha,
                            resultado(salones.get(0), instructores.get(0), 11, 12))));
            inicio.countDown();

            assertThat(List.of(a.get(15, TimeUnit.SECONDS), b.get(15, TimeUnit.SECONDS)))
                    .allMatch(Resultado::exito);
            assertThat(repository.findById(ajusteA)).get()
                    .extracting(AjusteProgramacionFecha::getSalonResultadoId,
                            AjusteProgramacionFecha::getInstructorResultadoId)
                    .containsExactly(salones.get(1), instructores.get(1));
            assertThat(repository.findById(ajusteB)).get()
                    .extracting(AjusteProgramacionFecha::getSalonResultadoId,
                            AjusteProgramacionFecha::getInstructorResultadoId)
                    .containsExactly(salones.get(0), instructores.get(0));
        } finally {
            executor.shutdownNow();
            jdbc.update("delete from programacion_ajuste_fecha where id in (?, ?)", ajusteA, ajusteB);
        }
    }

    private void competirAdicionesTraslapadas(
            LocalDate fecha, UUID salonA, UUID salonB) throws Exception {
        UUID idA = UUID.randomUUID();
        UUID idB = UUID.randomUUID();
        CountDownLatch inicio = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<Resultado> a = executor.submit(() -> ejecutar(inicio, () ->
                    ajustes.guardarAdicion(
                            idA, fecha, resultado(salonA, instructores.get(0), 10, 12))));
            Future<Resultado> b = executor.submit(() -> ejecutar(inicio, () ->
                    ajustes.guardarAdicion(
                            idB, fecha, resultado(salonB, instructores.get(0), 11, 13))));
            inicio.countDown();

            List<Resultado> resultados = List.of(
                    a.get(15, TimeUnit.SECONDS), b.get(15, TimeUnit.SECONDS));
            assertThat(resultados).filteredOn(Resultado::exito).hasSize(1);
            assertThat(resultados.stream().filter(r -> !r.exito())
                    .map(r -> r.fallo().getMessage()).findFirst().orElseThrow())
                    .contains(ProgramacionErrores.INSTRUCTOR_CON_PROGRAMACION_TRASLAPADA);
            assertThat(jdbc.queryForObject(
                    "select count(*) from programacion_ajuste_fecha where id in (?, ?)",
                    Integer.class, idA, idB)).isOne();
        } finally {
            executor.shutdownNow();
            jdbc.update("delete from programacion_ajuste_fecha where id in (?, ?)", idA, idB);
        }
    }

    private Resultado insertarTargetConcurrente(
            UUID id, UUID serie, LocalDate fecha, CountDownLatch inicio) {
        return ejecutar(inicio, () -> transactions.executeWithoutResult(ignored -> {
            salonLocks.adquirirOrdenados(List.of(salones.get(0)));
            instructorLocks.adquirirOrdenados(List.of(instructores.get(0)));
            AjusteProgramacionFecha target = AjusteProgramacionFecha.nuevoTarget(
                    id, AjusteProgramacionFecha.Tipo.CANCELACION, serie, fecha,
                    null, null, null, null, null);
            ConflictoAjusteProgramacionTranslator.traduciendoTarget(
                    () -> persistence.crear(target));
        }));
    }

    private Resultado ejecutar(CountDownLatch inicio, Runnable operacion) {
        try {
            esperar(inicio);
            operacion.run();
            return new Resultado(true, null);
        } catch (Throwable fallo) {
            return new Resultado(false, fallo);
        }
    }

    private UUID insertarBloque(UUID serie, UUID salon, LocalDate fecha) {
        UUID id = UUID.randomUUID();
        jdbc.update("""
                insert into programacion_bloque
                    (id, serie_id, salon_id, dia_semana, hora_inicio, hora_fin,
                     vigente_desde, vigente_hasta, activo)
                values (?, ?, ?, 1, '08:00', '14:00', ?, ?, true)
                """, id, serie, salon, fecha, fecha);
        return id;
    }

    private AjusteProgramacionFechaService.Resultado resultado(
            UUID salon, UUID instructor, int inicio, int fin) {
        return new AjusteProgramacionFechaService.Resultado(
                salon, instructor, actividad,
                LocalTime.of(inicio, 0), LocalTime.of(fin, 0));
    }

    private void esperar(CountDownLatch latch) {
        try {
            if (!latch.await(10, TimeUnit.SECONDS)) {
                throw new AssertionError("La barrera de inicio no fue liberada");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AssertionError("Interrumpido esperando barrera", e);
        }
    }

    private record Resultado(boolean exito, Throwable fallo) {
    }
}
