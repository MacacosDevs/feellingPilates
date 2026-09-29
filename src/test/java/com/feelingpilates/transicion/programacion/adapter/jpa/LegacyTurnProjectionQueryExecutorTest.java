package com.feelingpilates.transicion.programacion.adapter.jpa;

import com.feelingpilates.transicion.programacion.adapter.jpa.mapper.LegacyTurnProjectionMapper;
import com.feelingpilates.transicion.programacion.adapter.jpa.projection.LegacyTurnProjectionCatalog;
import com.feelingpilates.transicion.programacion.adapter.jpa.projection.LegacyTurnProjectionQueryExecutor;
import com.feelingpilates.transicion.programacion.adapter.jpa.testinfra.F2eStatementPolicyInspector;
import com.feelingpilates.transicion.programacion.read.LegacyTurnReadContext;
import com.feelingpilates.transicion.programacion.read.LegacyTurnScope;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.hibernate.query.NativeQuery;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LegacyTurnProjectionQueryExecutorTest {

    private static final LegacyTurnProjectionCatalog CATALOG = LegacyTurnProjectionCatalog.R2_LEGACY_TURN_V1;

    @Test
    void catalogContainsExactlySixCanonicalStatementIdentities() {
        assertEquals(6, CATALOG.statements().size());
        assertEquals(LegacyTurnProjectionCatalog.MEMBERS_ID,
                F2eStatementPolicyInspector.identificar(canonicalMembers()));
        assertEquals(LegacyTurnProjectionCatalog.ASSIGNMENTS_ID,
                F2eStatementPolicyInspector.identificar(canonicalAssignments()));
        assertEquals(LegacyTurnProjectionCatalog.ISOLATION_ID,
                F2eStatementPolicyInspector.identificar(LegacyTurnProjectionCatalog.ISOLATION_SQL));
        assertEquals(LegacyTurnProjectionCatalog.READ_ONLY_ID,
                F2eStatementPolicyInspector.identificar(LegacyTurnProjectionCatalog.READ_ONLY_SQL));
        assertEquals(LegacyTurnProjectionCatalog.RESOURCE_ID,
                F2eStatementPolicyInspector.identificar(LegacyTurnProjectionCatalog.RESOURCE_SQL));
        assertEquals(LegacyTurnProjectionCatalog.SNAPSHOT_ID,
                F2eStatementPolicyInspector.identificar(LegacyTurnProjectionCatalog.SNAPSHOT_SQL));
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void membersUsesNaturalUuidOrderAndExactTypedNamedBindingPlan() {
        EntityManager entityManager = mock(EntityManager.class);
        Query jpaQuery = mock(Query.class);
        NativeQuery nativeQuery = mock(NativeQuery.class);
        when(entityManager.createNativeQuery(LegacyTurnProjectionCatalog.MEMBERS_SQL)).thenReturn(jpaQuery);
        when(jpaQuery.unwrap(NativeQuery.class)).thenReturn(nativeQuery);
        when(nativeQuery.getResultList()).thenReturn(List.of());
        UUID lowNatural = new UUID(Long.MIN_VALUE, 0);
        UUID highNatural = new UUID(0, 0);
        LegacyTurnScope scope = new LegacyTurnScope(Set.of(highNatural, lowNatural), LocalDate.of(2026, 9, 28));

        new LegacyTurnProjectionQueryExecutor(entityManager, CATALOG).consultarMiembros(scope);

        InOrder order = inOrder(nativeQuery);
        order.verify(nativeQuery).setParameterList("salonIds", List.of(lowNatural, highNatural), UUID.class);
        order.verify(nativeQuery).setParameter("active", Boolean.TRUE, Boolean.class);
        order.verify(nativeQuery).setParameter("recurrentType", "RECURRENTE", String.class);
        order.verify(nativeQuery).setParameter("dayOfWeek", (short) 1, Short.class);
        order.verify(nativeQuery).setParameter("exceptionType", "EXCEPCION", String.class);
        order.verify(nativeQuery).setParameter("cancellationType", "CANCELACION", String.class);
        order.verify(nativeQuery).setParameter("fecha", LocalDate.of(2026, 9, 28), LocalDate.class);
        order.verify(nativeQuery).getResultList();
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void readerOmitsAssignmentsOnlyWhenMembersIsActuallyEmpty() {
        EntityManager entityManager = mock(EntityManager.class);
        Query jpaQuery = mock(Query.class);
        NativeQuery nativeQuery = mock(NativeQuery.class);
        when(entityManager.createNativeQuery(LegacyTurnProjectionCatalog.MEMBERS_SQL)).thenReturn(jpaQuery);
        when(jpaQuery.unwrap(NativeQuery.class)).thenReturn(nativeQuery);
        when(nativeQuery.getResultList()).thenReturn(List.of());
        LegacyTurnScope scope = new LegacyTurnScope(Set.of(new UUID(0, 1)), LocalDate.of(2026, 9, 28));
        LegacyTurnReadContext context = context(scope);
        LegacyTurnProjectionQueryExecutor executor = new LegacyTurnProjectionQueryExecutor(entityManager, CATALOG);
        LegacyTurnJpaReader reader = new LegacyTurnJpaReader(executor,
                new LegacyTurnProjectionMapper(CATALOG), CATALOG);

        org.springframework.transaction.support.TransactionSynchronizationManager.setActualTransactionActive(true);
        org.springframework.transaction.support.TransactionSynchronizationManager.setCurrentTransactionReadOnly(true);
        org.springframework.transaction.support.TransactionSynchronizationManager.setCurrentTransactionIsolationLevel(
                org.springframework.transaction.TransactionDefinition.ISOLATION_REPEATABLE_READ);
        try {
            assertEquals(0, reader.readForDate(context, scope).sources().size());
            verify(entityManager, never()).createNativeQuery(LegacyTurnProjectionCatalog.ASSIGNMENTS_SQL);
        } finally {
            org.springframework.transaction.support.TransactionSynchronizationManager.clear();
        }
    }

    @Test
    void assignmentsRejectsEmptyParentsBeforeJpa() {
        EntityManager entityManager = mock(EntityManager.class);
        LegacyTurnProjectionQueryExecutor executor = new LegacyTurnProjectionQueryExecutor(entityManager, CATALOG);
        assertThrows(IllegalArgumentException.class, () -> executor.consultarAsignaciones(List.of()));
        verify(entityManager, never()).createNativeQuery(anyString());
    }

    private String canonicalMembers() {
        return LegacyTurnProjectionCatalog.MEMBERS_SQL.replace("(:salonIds)", "(?*)")
                .replace(":active", "?").replace(":recurrentType", "?")
                .replace(":dayOfWeek", "?").replace(":exceptionType", "?")
                .replace(":cancellationType", "?").replace(":fecha", "?");
    }

    private String canonicalAssignments() {
        return LegacyTurnProjectionCatalog.ASSIGNMENTS_SQL.replace("(:turnIds)", "(?*)");
    }

    private LegacyTurnReadContext context(LegacyTurnScope scope) {
        return new LegacyTurnReadContext("run", "attempt", "fixture:postgres16:r2:test",
                "sha256:" + "a".repeat(64), LegacyTurnReadContext.ProjectionCatalogVersion.R2_LEGACY_TURN_V1,
                "rules", ZoneId.of("UTC"), scope.canonical(),
                LegacyTurnReadContext.SnapshotClaim.R2_INTERNAL_RR_TEST, "b".repeat(64));
    }
}
