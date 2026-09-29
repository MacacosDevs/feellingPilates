package com.feelingpilates.transicion.programacion.adapter.jpa;

import com.feelingpilates.transicion.programacion.adapter.jpa.mapper.LegacyTurnProjectionMapper;
import com.feelingpilates.transicion.programacion.adapter.jpa.projection.LegacyAssignmentRow;
import com.feelingpilates.transicion.programacion.adapter.jpa.projection.LegacyTurnMemberRow;
import com.feelingpilates.transicion.programacion.adapter.jpa.projection.LegacyTurnProjectionCatalog;
import com.feelingpilates.transicion.programacion.adapter.jpa.projection.LegacyTurnProjectionQueryExecutor;
import com.feelingpilates.transicion.programacion.read.LegacyTurnReadContext;
import com.feelingpilates.transicion.programacion.read.LegacyTurnReadPort;
import com.feelingpilates.transicion.programacion.read.LegacyTurnReadSet;
import com.feelingpilates.transicion.programacion.read.LegacyTurnScope;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class LegacyTurnJpaReader implements LegacyTurnReadPort {

    private final LegacyTurnProjectionQueryExecutor executor;
    private final LegacyTurnProjectionMapper mapper;
    private final LegacyTurnProjectionCatalog catalog;
    private final ContextAuthority contextAuthority;

    public LegacyTurnJpaReader(
            LegacyTurnProjectionQueryExecutor executor,
            LegacyTurnProjectionMapper mapper,
            LegacyTurnProjectionCatalog catalog,
            ContextAuthority contextAuthority) {
        this.executor = Objects.requireNonNull(executor, "executor");
        this.mapper = Objects.requireNonNull(mapper, "mapper");
        this.catalog = Objects.requireNonNull(catalog, "catalog");
        this.contextAuthority = Objects.requireNonNull(contextAuthority, "contextAuthority");
        if (catalog != LegacyTurnProjectionCatalog.R2_LEGACY_TURN_V1
                || executor.catalog() != catalog || mapper.catalog() != catalog) {
            throw new IllegalStateException("R2 reader catalog binding not proven");
        }
    }

    @Override
    @Transactional(
            transactionManager = "f2eR2ReaderTransactionManager",
            propagation = Propagation.MANDATORY,
            readOnly = true)
    public LegacyTurnReadSet readForDate(LegacyTurnReadContext context, LegacyTurnScope scope) {
        if (!TransactionSynchronizationManager.isActualTransactionActive()
                || !TransactionSynchronizationManager.isCurrentTransactionReadOnly()
                || !Objects.equals(TransactionSynchronizationManager.getCurrentTransactionIsolationLevel(),
                TransactionDefinition.ISOLATION_REPEATABLE_READ)) {
            throw new IllegalStateException("R2 reader requires an owned REPEATABLE_READ/read-only transaction");
        }
        Objects.requireNonNull(context, "context");
        Objects.requireNonNull(scope, "scope");
        if (context.projectionCatalogVersion() != catalog.version()
                || context.snapshotClaim() != LegacyTurnReadContext.SnapshotClaim.R2_INTERNAL_RR_TEST
                || !context.scopeCanonical().equals(scope.canonical())) {
            throw new IllegalArgumentException("R2 reader context/scope binding not proven");
        }
        contextAuthority.verify(context, scope);
        List<LegacyTurnMemberRow> members = executor.consultarMiembros(scope);
        List<UUID> parentIds = mapper.usableParentTurnIds(members);
        List<LegacyAssignmentRow> assignments = parentIds.isEmpty()
                ? List.of() : executor.consultarAsignaciones(parentIds);
        return mapper.mapear(members, assignments, context, scope);
    }

    @FunctionalInterface
    public interface ContextAuthority {
        void verify(LegacyTurnReadContext context, LegacyTurnScope scope);
    }
}
