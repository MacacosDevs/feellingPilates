package com.feelingpilates.transicion.programacion.adapter.jpa;

import com.feelingpilates.transicion.programacion.adapter.jpa.projection.LegacyTurnProjectionCatalog;
import com.feelingpilates.transicion.programacion.adapter.jpa.testinfra.LegacyTurnR2PostgresTestConfiguration;
import com.feelingpilates.transicion.programacion.adapter.jpa.testinfra.LegacyTurnTransactionTestOwner;
import com.feelingpilates.transicion.programacion.adapter.jpa.testinfra.LegacyTurnJdbcCapture;
import com.feelingpilates.transicion.programacion.read.LegacyTurnReadContext;
import com.feelingpilates.transicion.programacion.read.LegacyTurnReadPort;
import com.feelingpilates.transicion.programacion.read.LegacyTurnScope;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityManager;
import org.hibernate.Session;
import org.springframework.orm.jpa.EntityManagerHolder;
import org.springframework.transaction.support.TransactionSynchronizationManager;
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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

    @Autowired
    @Qualifier("f2eR2ReaderEntityManagerFactory")
    EntityManagerFactory factory;

    @Autowired
    LegacyTurnJdbcCapture jdbcCapture;

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
        assertTrue(outcome.completed());
        assertTrue(outcome.jdbcObservations().stream().allMatch(observation ->
                observation.physicalConnection() == outcome.physicalConnection()
                        && observation.holder() == outcome.holder()
                        && observation.session() == outcome.session()
                        && observation.transactionOwner() == outcome.transactionOwner()
                        && observation.factory() == factory));
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

    @Test
    void forgedSourceSchemaAndSnapshotAreRejectedInsideValidOwnedTransactions() {
        LegacyTurnScope scope = scope(LegacyTurnR2PostgresTestConfiguration.FECHA);
        assertEquals("R2 context provenance is not owner bound", assertThrows(IllegalStateException.class, () -> owner.readWithForgedContext(
                seed("forged-source"), scope,
                trusted -> copyWithProvenance(trusted, "fixture:postgres16:r2:forged",
                        trusted.schemaFingerprint(), trusted.snapshotEvidenceId()))).getMessage());
        assertEquals("R2 context provenance is not owner bound", assertThrows(IllegalStateException.class, () -> owner.readWithForgedContext(
                seed("forged-schema"), scope,
                trusted -> copyWithProvenance(trusted, trusted.sourceName(),
                        "sha256:" + "f".repeat(64), trusted.snapshotEvidenceId()))).getMessage());
        assertEquals("R2 context provenance is not owner bound", assertThrows(IllegalStateException.class, () -> owner.readWithForgedContext(
                seed("forged-snapshot"), scope,
                trusted -> copyWithProvenance(trusted, trusted.sourceName(),
                        trusted.schemaFingerprint(), "f".repeat(64)))).getMessage());
    }

    @Test
    void captureRejectsChangedHolderSessionAndPhysicalConnectionAndEarlyClose() {
        EntityManagerFactory isolatedFactory = mock(EntityManagerFactory.class);
        EntityManager entityManager = mock(EntityManager.class);
        Session originalSession = mock(Session.class);
        Session replacementSession = mock(Session.class);
        EntityManagerHolder originalHolder = new EntityManagerHolder(entityManager);
        EntityManagerHolder replacementHolder = new EntityManagerHolder(mock(EntityManager.class));
        Object physical = new Object();
        when(entityManager.unwrap(Session.class)).thenReturn(originalSession);
        when(originalSession.isJoinedToTransaction()).thenReturn(true);
        TransactionSynchronizationManager.bindResource(isolatedFactory, originalHolder);
        var capture = jdbcCapture.open("resource-negative-fixture");
        try {
            jdbcCapture.bindResource(capture, this, isolatedFactory, originalHolder,
                    originalSession, physical);
            jdbcCapture.verifyBoundResource(capture, physical);
            TransactionSynchronizationManager.unbindResource(isolatedFactory);
            TransactionSynchronizationManager.bindResource(isolatedFactory, replacementHolder);
            assertThrows(IllegalStateException.class, () -> jdbcCapture.verifyBoundResource(capture, physical));
            TransactionSynchronizationManager.unbindResource(isolatedFactory);
            TransactionSynchronizationManager.bindResource(isolatedFactory, originalHolder);
            when(entityManager.unwrap(Session.class)).thenReturn(replacementSession);
            assertThrows(IllegalStateException.class, () -> jdbcCapture.verifyBoundResource(capture, physical));
            when(entityManager.unwrap(Session.class)).thenReturn(originalSession);
            assertThrows(IllegalStateException.class, () -> jdbcCapture.verifyBoundResource(capture, new Object()));
            jdbcCapture.close(capture);
            assertFalse(jdbcCapture.isOpen(capture));
            assertThrows(IllegalStateException.class, () -> jdbcCapture.snapshot(capture));
        } finally {
            jdbcCapture.discard(capture);
            TransactionSynchronizationManager.unbindResourceIfPossible(isolatedFactory);
        }
    }

    private LegacyTurnReadContext copyWithProvenance(LegacyTurnReadContext trusted,
                                                      String source, String schema, String snapshot) {
        return new LegacyTurnReadContext(trusted.runIdentity(), trusted.attemptIdentity(),
                source, schema, trusted.projectionCatalogVersion(), trusted.ruleCatalogVersion(),
                trusted.businessZone(), trusted.scopeCanonical(), trusted.snapshotClaim(), snapshot);
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
