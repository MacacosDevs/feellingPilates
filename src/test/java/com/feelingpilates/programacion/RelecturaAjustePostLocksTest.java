package com.feelingpilates.programacion;

import com.feelingpilates.TestcontainersConfiguration;
import com.feelingpilates.programacion.entidad.AjusteProgramacionFecha;
import com.feelingpilates.programacion.repositorio.AjusteProgramacionFechaRepository;
import com.feelingpilates.programacion.servicio.AjusteProgramacionFechaPersistence;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class RelecturaAjustePostLocksTest {

    @Autowired private AjusteProgramacionFechaRepository repository;
    @Autowired private AjusteProgramacionFechaPersistence persistence;
    @Autowired private TransactionTemplate transactions;
    @Autowired private JdbcTemplate jdbc;

    @Test
    void refreshHaceVisibleUnCommitConcurrenteOcultoPorElFirstLevelCache() throws Exception {
        UUID id = UUID.randomUUID();
        UUID salon = jdbc.queryForObject("select id from salon order by id limit 1", UUID.class);
        UUID instructor = jdbc.queryForObject("select id from usuario order by id limit 1", UUID.class);
        UUID actividad = jdbc.queryForObject(
                "select id from tipo_actividad order by id limit 1", UUID.class);
        transactions.executeWithoutResult(ignored -> persistence.crear(
                AjusteProgramacionFecha.nuevaAdicion(
                        id, LocalDate.of(2029, 1, 8), salon, instructor, actividad,
                        LocalTime.of(10, 0), LocalTime.of(11, 0))));

        CountDownLatch discoveryCargado = new CountDownLatch(1);
        CountDownLatch commitExternoListo = new CountDownLatch(1);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            Future<?> lector = executor.submit(() -> transactions.executeWithoutResult(ignored -> {
                AjusteProgramacionFecha discovery = repository.findById(id).orElseThrow();
                assertThat(discovery.getHoraInicioResultado()).isEqualTo(LocalTime.of(10, 0));
                discoveryCargado.countDown();
                esperar(commitExternoListo);

                AjusteProgramacionFecha desdeCache = repository.findById(id).orElseThrow();
                assertThat(desdeCache).isSameAs(discovery);
                assertThat(desdeCache.getHoraInicioResultado()).isEqualTo(LocalTime.of(10, 0));

                persistence.refrescar(desdeCache);
                assertThat(desdeCache.getHoraInicioResultado()).isEqualTo(LocalTime.NOON);
                assertThat(desdeCache.getHoraFinResultado()).isEqualTo(LocalTime.of(13, 0));
            }));

            assertThat(discoveryCargado.await(10, TimeUnit.SECONDS)).isTrue();
            transactions.executeWithoutResult(ignored -> jdbc.update("""
                    update programacion_ajuste_fecha
                       set hora_inicio_resultado = '12:00',
                           hora_fin_resultado = '13:00',
                           actualizado_en = now()
                     where id = ?
                    """, id));
            commitExternoListo.countDown();
            lector.get(10, TimeUnit.SECONDS);
        } finally {
            commitExternoListo.countDown();
            executor.shutdownNow();
            jdbc.update("delete from programacion_ajuste_fecha where id = ?", id);
        }
    }

    private void esperar(CountDownLatch latch) {
        try {
            if (!latch.await(10, TimeUnit.SECONDS)) {
                throw new AssertionError("El commit externo no terminó");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AssertionError("Interrumpido esperando commit externo", e);
        }
    }
}
