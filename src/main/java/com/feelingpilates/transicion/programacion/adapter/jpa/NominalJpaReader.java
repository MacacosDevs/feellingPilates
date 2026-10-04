package com.feelingpilates.transicion.programacion.adapter.jpa;

import com.feelingpilates.transicion.programacion.adapter.jpa.projection.*;
import com.feelingpilates.transicion.programacion.adapter.jpa.mapper.NominalProjectionMapper;
import com.feelingpilates.transicion.programacion.read.*;
import org.springframework.transaction.annotation.*;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import java.time.LocalDate;
import java.util.Objects;

public final class NominalJpaReader implements NominalProgrammingReadPort {
    private final NominalProjectionQueryExecutor executor;
    private final NominalProjectionMapper mapper;
    private final NominalProjectionCatalog catalog;
    private final ContextAuthority authority;
    public NominalJpaReader(NominalProjectionQueryExecutor executor, NominalProjectionMapper mapper,
                            NominalProjectionCatalog catalog, ContextAuthority authority) {
        this.executor = Objects.requireNonNull(executor); this.mapper = Objects.requireNonNull(mapper);
        this.catalog = Objects.requireNonNull(catalog); this.authority = Objects.requireNonNull(authority);
        if (catalog != NominalProjectionCatalog.R3_NOMINAL_V1 || executor.catalog() != catalog
                || mapper.catalog() != catalog) throw new IllegalStateException("R3 catalog binding");
    }
    @Override
    @Transactional(transactionManager="f2eReaderTransactionManager", propagation=Propagation.MANDATORY, readOnly=true)
    public NominalProgrammingReadSet readNominalOnDate(NominalReadSnapshotContext context, LocalDate fecha) {
        if (context == null || fecha == null || !fecha.equals(context.fecha()))
            throw new NominalReadFailure(NominalReadFailure.Category.INVALID_INPUT, fecha);
        if (!TransactionSynchronizationManager.isActualTransactionActive()
                || !TransactionSynchronizationManager.isSynchronizationActive()
                || !TransactionSynchronizationManager.isCurrentTransactionReadOnly()
                || !Objects.equals(TransactionSynchronizationManager.getCurrentTransactionIsolationLevel(),
                    TransactionDefinition.ISOLATION_REPEATABLE_READ))
            throw new NominalReadFailure(NominalReadFailure.Category.TRANSACTION_CONTEXT_INVALID, fecha);
        try {
            Object proxy = org.springframework.aop.framework.AopContext.currentProxy();
            if (!(proxy instanceof NominalProgrammingReadPort)
                    || !(proxy instanceof org.springframework.aop.framework.Advised advised)
                    || advised.getTargetSource().getTarget() != this)
                throw new NominalReadFailure(NominalReadFailure.Category.TRANSACTION_CONTEXT_INVALID, fecha);
        } catch (NominalReadFailure e) { throw e; }
        catch (Exception e) {
            throw new NominalReadFailure(NominalReadFailure.Category.TRANSACTION_CONTEXT_INVALID, fecha,
                    java.util.List.of(), e);
        }
        if (context.projectionCatalogVersion() != catalog.version())
            throw new NominalReadFailure(NominalReadFailure.Category.TRANSACTION_CONTEXT_INVALID, fecha);
        try { authority.verify(context, fecha); }
        catch (NominalReadFailure e) { throw e; }
        catch (RuntimeException e) {
            throw new NominalReadFailure(NominalReadFailure.Category.TRANSACTION_CONTEXT_INVALID, fecha,
                    java.util.List.of(), e);
        }
        return mapper.mapear(executor.consultarNominal(fecha), context, fecha);
    }
    @FunctionalInterface public interface ContextAuthority {
        void verify(NominalReadSnapshotContext context, LocalDate fecha);
    }
}
