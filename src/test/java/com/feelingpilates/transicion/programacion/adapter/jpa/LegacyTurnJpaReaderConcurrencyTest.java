package com.feelingpilates.transicion.programacion.adapter.jpa;

import com.feelingpilates.transicion.programacion.adapter.jpa.testinfra.LegacyTurnJdbcCapture;
import com.feelingpilates.transicion.programacion.adapter.jpa.testinfra.LegacyTurnR2PostgresTestConfiguration;
import com.feelingpilates.transicion.programacion.adapter.jpa.testinfra.LegacyTurnTransactionTestOwner;
import com.feelingpilates.transicion.programacion.read.LegacyTurnReadContext;
import com.feelingpilates.transicion.programacion.read.LegacyTurnReadPort;
import com.feelingpilates.transicion.programacion.read.LegacyTurnScope;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringJUnitConfig(LegacyTurnR2PostgresTestConfiguration.class)
class LegacyTurnJpaReaderConcurrencyTest {

    @Autowired @Qualifier("legacyTurnTransactionTestOwner")
    LegacyTurnTransactionTestOwner owner;
    @Autowired @Qualifier("legacyTurnJdbcCapture")
    LegacyTurnJdbcCapture capture;
    @Autowired @Qualifier("f2eR2PrivilegedDataSource")
    DataSource privileged;
    @Autowired @Qualifier("f2eR2ReaderDataSource")
    DataSource readerDataSource;
    @Autowired @Qualifier("f2eR2ReaderTransactionManager")
    JpaTransactionManager transactionManager;
    @Autowired @Qualifier("legacyTurnJpaReader")
    LegacyTurnReadPort reader;
    @Autowired
    LegacyTurnR2PostgresTestConfiguration configuration;

    @Test
    void repeatableReadKeepsAssignmentSnapshotStableAndNewAttemptSeesCommittedWriter() throws Exception {
        updateAssignment(LocalTime.of(9, 0), LocalTime.of(10, 0));
        CountDownLatch membersObserved = new CountDownLatch(1);
        CountDownLatch continueAssignments = new CountDownLatch(1);
        capture.installMembersBarrier(membersObserved, continueAssignments);
        var executor = Executors.newSingleThreadExecutor();
        try {
            var future = executor.submit(() -> owner.inRepeatableReadOnly(seed("concurrent-first"), scope()));
            assertTrue(membersObserved.await(30, TimeUnit.SECONDS));
            updateAssignment(LocalTime.of(10, 0), LocalTime.of(11, 0));
            continueAssignments.countDown();
            var first = future.get(30, TimeUnit.SECONDS);
            assertEquals("09:00:00.000000", recurrentStart(first));
            assertEquals(first.snapshotInitial(), first.snapshotFinal());

            capture.clearBarrier();
            var second = owner.inRepeatableReadOnly(seed("concurrent-second"), scope());
            assertEquals("10:00:00.000000", recurrentStart(second));
        } finally {
            continueAssignments.countDown();
            capture.clearBarrier();
            executor.shutdownNow();
            updateAssignment(LocalTime.of(9, 0), LocalTime.of(10, 0));
        }
    }

    @Test
    void rawReadCommittedControlObservesInterleavedWriterButR2ReaderRejectsRcPreData() throws Exception {
        updateAssignment(LocalTime.of(9, 0), LocalTime.of(10, 0));
        try (var connection = readerDataSource.getConnection()) {
            connection.setAutoCommit(false);
            connection.setReadOnly(true);
            connection.setTransactionIsolation(java.sql.Connection.TRANSACTION_READ_COMMITTED);
            try (var members = connection.prepareStatement(
                    "SELECT count(*) FROM public.turno_instructor_usuario WHERE turno_id=?")) {
                members.setObject(1, LegacyTurnR2PostgresTestConfiguration.TURNO_RECURRENTE);
                try (var rows = members.executeQuery()) { assertTrue(rows.next()); }
            }
            updateAssignment(LocalTime.of(10, 0), LocalTime.of(11, 0));
            try (var assignment = connection.prepareStatement(
                    "SELECT hora_inicio FROM public.turno_instructor_asignacion WHERE turno_id=?")) {
                assignment.setObject(1, LegacyTurnR2PostgresTestConfiguration.TURNO_RECURRENTE);
                try (var rows = assignment.executeQuery()) {
                    assertTrue(rows.next());
                    assertEquals(LocalTime.of(10, 0), rows.getObject(1, LocalTime.class));
                }
            }
            connection.rollback();
        } finally {
            updateAssignment(LocalTime.of(9, 0), LocalTime.of(10, 0));
        }

        TransactionTemplate template = new TransactionTemplate(transactionManager);
        template.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        template.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
        template.setReadOnly(true);
        IllegalStateException failure = assertThrows(IllegalStateException.class, () ->
                template.executeWithoutResult(status -> reader.readForDate(context("rc-rejected"), scope())));
        assertTrue(failure.getMessage().contains("REPEATABLE_READ"));
    }

    private String recurrentStart(LegacyTurnTransactionTestOwner.Outcome outcome) {
        return outcome.readSet().sources().stream()
                .filter(source -> source.sourceIdentity().contains(
                        LegacyTurnR2PostgresTestConfiguration.TURNO_RECURRENTE.toString()))
                .findFirst().orElseThrow().observableFields().get("effectiveStart");
    }

    private void updateAssignment(LocalTime start, LocalTime end) throws Exception {
        try (var connection = privileged.getConnection(); var statement = connection.prepareStatement(
                "UPDATE public.turno_instructor_asignacion SET hora_inicio=?,hora_fin=? WHERE turno_id=?")) {
            statement.setObject(1, start);
            statement.setObject(2, end);
            statement.setObject(3, LegacyTurnR2PostgresTestConfiguration.TURNO_RECURRENTE);
            assertEquals(1, statement.executeUpdate());
        }
    }

    private LegacyTurnScope scope() {
        return new LegacyTurnScope(Set.of(configuration.salonId()),
                LegacyTurnR2PostgresTestConfiguration.FECHA);
    }

    private LegacyTurnTransactionTestOwner.Seed seed(String attempt) {
        return new LegacyTurnTransactionTestOwner.Seed("run-r2-concurrency", attempt,
                "rules-v1", ZoneId.of("America/Mexico_City"));
    }

    private LegacyTurnReadContext context(String attempt) {
        LegacyTurnScope scope = scope();
        return new LegacyTurnReadContext("run-r2-rc", attempt, "fixture:postgres16:r2:turno-a",
                configuration.schemaFingerprint(), LegacyTurnReadContext.ProjectionCatalogVersion.R2_LEGACY_TURN_V1,
                "rules-v1", ZoneId.of("UTC"), scope.canonical(),
                LegacyTurnReadContext.SnapshotClaim.R2_INTERNAL_RR_TEST, "d".repeat(64));
    }
}
