package com.feelingpilates.programacion;

import com.feelingpilates.TestcontainersConfiguration;
import com.feelingpilates.exception.ConflictException;
import com.feelingpilates.programacion.entidad.AjusteProgramacionFecha;
import com.feelingpilates.programacion.repositorio.AjusteProgramacionFechaRepository;
import com.feelingpilates.programacion.servicio.AjusteProgramacionFechaPersistence;
import com.feelingpilates.programacion.servicio.AjusteProgramacionFechaService;
import com.feelingpilates.programacion.servicio.ConflictoAjusteProgramacionException;
import com.feelingpilates.programacion.servicio.ConflictoAjusteProgramacionTranslator;
import com.feelingpilates.ubicaciones.servicio.SalonLocks;
import com.feelingpilates.usuarios.servicio.InstructorLocks;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class AjusteIdConcurrenciaTest {

    @Autowired private AjusteProgramacionFechaRepository repository;
    @Autowired private AjusteProgramacionFechaPersistence persistence;
    @Autowired private AjusteProgramacionFechaService service;
    @Autowired private SalonLocks salonLocks;
    @Autowired private InstructorLocks instructorLocks;
    @Autowired private TransactionTemplate transactionTemplate;
    @Autowired private JdbcTemplate jdbc;

    @Test
    void testBColisionPkRealYTestCRetryExternoEnNuevaTransaccion() throws Exception {
        UUID ajusteId = UUID.randomUUID();
        List<UUID> salones = jdbc.queryForList("select id from salon order by id limit 2", UUID.class);
        List<UUID> instructores = jdbc.queryForList("select id from usuario order by id limit 2", UUID.class);
        UUID actividad = jdbc.queryForObject(
                "select id from tipo_actividad order by id limit 1", UUID.class);
        LocalDate fecha = LocalDate.of(2028, 1, 3);
        Snapshot a = new Snapshot(salones.get(0), instructores.get(0), actividad,
                LocalTime.of(9, 0), LocalTime.of(10, 0));
        Snapshot b = new Snapshot(salones.get(1), instructores.get(1), actividad,
                LocalTime.of(12, 0), LocalTime.of(13, 0));

        CountDownLatch ambasRelecturasAusentes = new CountDownLatch(2);
        CountDownLatch liberarPersist = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<ResultadoTx> txA = executor.submit(() -> competir(
                    ajusteId, fecha, a, ambasRelecturasAusentes, liberarPersist));
            Future<ResultadoTx> txB = executor.submit(() -> competir(
                    ajusteId, fecha, b, ambasRelecturasAusentes, liberarPersist));

            assertThat(ambasRelecturasAusentes.await(10, TimeUnit.SECONDS)).isTrue();
            liberarPersist.countDown();

            List<ResultadoTx> resultados = List.of(
                    txA.get(15, TimeUnit.SECONDS), txB.get(15, TimeUnit.SECONDS));
            assertThat(resultados).filteredOn(ResultadoTx::gano).hasSize(1);
            ResultadoTx perdedor = resultados.stream().filter(r -> !r.gano()).findFirst().orElseThrow();
            assertThat(perdedor.sqlState()).isEqualTo("23505");
            assertThat(perdedor.constraint()).isEqualTo("programacion_ajuste_fecha_pkey");

            AjusteProgramacionFecha persistida = repository.findById(ajusteId).orElseThrow();
            Snapshot ganador = snapshot(persistida);
            assertThat(ganador).isIn(a, b);
            assertThat(jdbc.queryForObject(
                    "select count(*) from programacion_ajuste_fecha where id = ?",
                    Integer.class, ajusteId)).isOne();

            // TEST C: retry externo real; la llamada abre una transacción nueva y hace discovery,
            // locks de los recursos persistidos, relectura y no-op. No reutiliza la tx fallida.
            OffsetDateTime actualizadoAntes = persistida.getActualizadoEn();
            AjusteProgramacionFecha retry = service.guardarAdicion(
                    ajusteId, fecha, comando(ganador));
            assertThat(retry.getActualizadoEn()).isEqualTo(actualizadoAntes);
            assertThat(snapshot(retry)).isEqualTo(ganador);

            service.retirarAdicion(ajusteId);
            assertThat(repository.findById(ajusteId)).get()
                    .extracting(AjusteProgramacionFecha::isActivo).isEqualTo(false);
            assertThatThrownBy(() -> service.guardarAdicion(ajusteId, fecha, comando(ganador)))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining("histórico");
        } finally {
            executor.shutdownNow();
        }
    }

    private ResultadoTx competir(
            UUID ajusteId,
            LocalDate fecha,
            Snapshot snapshot,
            CountDownLatch ambasRelecturasAusentes,
            CountDownLatch liberarPersist) {
        try {
            transactionTemplate.executeWithoutResult(status -> {
                assertThat(repository.findById(ajusteId)).isEmpty();
                salonLocks.adquirirOrdenados(List.of(snapshot.salonId()));
                instructorLocks.adquirirOrdenados(List.of(snapshot.instructorId()));
                assertThat(repository.findById(ajusteId)).isEmpty();
                ambasRelecturasAusentes.countDown();
                esperar(liberarPersist);
                AjusteProgramacionFecha nueva = AjusteProgramacionFecha.nuevaAdicion(
                        ajusteId, fecha, snapshot.salonId(), snapshot.instructorId(),
                        snapshot.actividadId(), snapshot.inicio(), snapshot.fin());
                ConflictoAjusteProgramacionTranslator.traduciendoAjusteId(
                        () -> persistence.crear(nueva));
            });
            return new ResultadoTx(true, null, null);
        } catch (ConflictoAjusteProgramacionException conflicto) {
            return new ResultadoTx(false, conflicto.getSqlState(), conflicto.getConstraint());
        }
    }

    private void esperar(CountDownLatch latch) {
        try {
            if (!latch.await(10, TimeUnit.SECONDS)) {
                throw new AssertionError("La barrera de persist no fue liberada");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AssertionError("Interrumpido esperando barrera", e);
        }
    }

    private Snapshot snapshot(AjusteProgramacionFecha ajuste) {
        return new Snapshot(
                ajuste.getSalonResultadoId(), ajuste.getInstructorResultadoId(),
                ajuste.getTipoActividadResultadoId(), ajuste.getHoraInicioResultado(),
                ajuste.getHoraFinResultado());
    }

    private AjusteProgramacionFechaService.Resultado comando(Snapshot snapshot) {
        return new AjusteProgramacionFechaService.Resultado(
                snapshot.salonId(), snapshot.instructorId(), snapshot.actividadId(),
                snapshot.inicio(), snapshot.fin());
    }

    private record Snapshot(
            UUID salonId, UUID instructorId, UUID actividadId,
            LocalTime inicio, LocalTime fin) {
    }

    private record ResultadoTx(boolean gano, String sqlState, String constraint) {
    }
}
