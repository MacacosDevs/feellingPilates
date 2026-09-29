package com.feelingpilates.transicion.programacion.adapter.jpa;

import com.feelingpilates.transicion.programacion.adapter.jpa.projection.LegacyTurnProjectionCatalog;
import com.feelingpilates.transicion.programacion.adapter.jpa.testinfra.LegacyTurnR2PostgresTestConfiguration;
import com.feelingpilates.transicion.programacion.adapter.jpa.testinfra.LegacyTurnTransactionTestOwner;
import com.feelingpilates.transicion.programacion.read.LegacyTurnReadContext;
import com.feelingpilates.transicion.programacion.read.LegacyTurnReadPort;
import com.feelingpilates.transicion.programacion.read.LegacyTurnScope;
import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.IllegalTransactionStateException;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringJUnitConfig(LegacyTurnR2PostgresTestConfiguration.class)
class LegacyTurnJpaReaderTransactionTest {

    @Autowired
    @Qualifier("legacyTurnTransactionTestOwner")
    LegacyTurnTransactionTestOwner owner;

    @Autowired
    @Qualifier("legacyTurnJpaReader")
    LegacyTurnReadPort reader;

    @Autowired
    LegacyTurnR2PostgresTestConfiguration configuration;

    @Test
    void distinctRealProxiesEnforceOwnerAndReaderTransactionContracts() throws Exception {
        assertTrue(AopUtils.isAopProxy(owner));
        assertTrue(AopUtils.isAopProxy(reader));
        assertNotSame(owner, reader);
        Transactional ownerTransaction = LegacyTurnTransactionTestOwner.class
                .getMethod("inRepeatableReadOnly", LegacyTurnTransactionTestOwner.Seed.class,
                        LegacyTurnScope.class).getAnnotation(Transactional.class);
        assertEquals("f2eR2ReaderTransactionManager", ownerTransaction.transactionManager());
        assertEquals(Propagation.REQUIRES_NEW, ownerTransaction.propagation());
        assertEquals(Isolation.REPEATABLE_READ, ownerTransaction.isolation());
        assertTrue(ownerTransaction.readOnly());
    }

    @Test
    void mandatoryReaderCannotSilentlyCreateItsOwnTransaction() {
        LegacyTurnScope scope = scope(LegacyTurnR2PostgresTestConfiguration.FECHA);
        assertThrows(IllegalTransactionStateException.class, () -> reader.readForDate(
                context(scope, "outside"), scope));
    }

    @Test
    void ownerProvidesOneStablePostgresSnapshotAndExactTenStatementManifest() {
        var seed = seed("tx-full");
        var outcome = owner.inRepeatableReadOnly(seed, scope(LegacyTurnR2PostgresTestConfiguration.FECHA));
        assertEquals(outcome.snapshotInitial(), outcome.snapshotFinal());
        assertEquals(outcome.resourceInitial(), outcome.resourceFinal());
        assertEquals(3, outcome.readSet().sources().size());
        assertEquals(List.of(
                LegacyTurnProjectionCatalog.ISOLATION_ID,
                LegacyTurnProjectionCatalog.READ_ONLY_ID,
                LegacyTurnProjectionCatalog.RESOURCE_ID,
                LegacyTurnProjectionCatalog.SNAPSHOT_ID,
                LegacyTurnProjectionCatalog.MEMBERS_ID,
                LegacyTurnProjectionCatalog.ASSIGNMENTS_ID,
                LegacyTurnProjectionCatalog.ISOLATION_ID,
                LegacyTurnProjectionCatalog.READ_ONLY_ID,
                LegacyTurnProjectionCatalog.RESOURCE_ID,
                LegacyTurnProjectionCatalog.SNAPSHOT_ID), outcome.statementIds());
        assertEquals(10, outcome.jdbcObservations().size());
        assertTrue(outcome.jdbcObservations().stream().allMatch(observation ->
                observation.physicalConnection() == outcome.physicalConnection()));
        assertEquals(7, outcome.jdbcObservations().get(4).binds().size());
        assertEquals(3, outcome.jdbcObservations().get(5).binds().size());
        assertEquals("SUCCESS", owner.state(seed));
    }

    @Test
    void realEmptyMembersShortCircuitStillExecutesFinalSnapshotProbes() {
        var outcome = owner.inRepeatableReadOnly(seed("tx-empty"), scope(LocalDate.of(2026, 9, 29)));
        assertTrue(outcome.readSet().sources().isEmpty());
        assertEquals(9, outcome.statementIds().size());
        assertTrue(outcome.statementIds().stream().noneMatch(LegacyTurnProjectionCatalog.ASSIGNMENTS_ID::equals));
        assertEquals(outcome.snapshotInitial(), outcome.snapshotFinal());
    }

    @Test
    void runAttemptPairCannotBeReusedWithinApplicationContext() {
        var seed = seed("tx-once");
        owner.inRepeatableReadOnly(seed, scope(LegacyTurnR2PostgresTestConfiguration.FECHA));
        assertThrows(IllegalStateException.class, () ->
                owner.inRepeatableReadOnly(seed, scope(LegacyTurnR2PostgresTestConfiguration.FECHA)));
    }

    private LegacyTurnTransactionTestOwner.Seed seed(String attempt) {
        return new LegacyTurnTransactionTestOwner.Seed("run-r2-transaction", attempt,
                "rules-v1", ZoneId.of("America/Mexico_City"));
    }

    private LegacyTurnScope scope(LocalDate date) {
        return new LegacyTurnScope(Set.of(configuration.salonId()), date);
    }

    private LegacyTurnReadContext context(LegacyTurnScope scope, String attempt) {
        return new LegacyTurnReadContext("run-outside", attempt,
                "fixture:postgres16:r2:turno-a", configuration.schemaFingerprint(),
                LegacyTurnReadContext.ProjectionCatalogVersion.R2_LEGACY_TURN_V1,
                "rules-v1", ZoneId.of("UTC"), scope.canonical(),
                LegacyTurnReadContext.SnapshotClaim.R2_INTERNAL_RR_TEST, "c".repeat(64));
    }
}
