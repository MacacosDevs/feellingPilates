package com.feelingpilates.transicion.programacion.r4.adapter.jpa;

import com.feelingpilates.transicion.programacion.r4.adapter.jpa.projection.*;
import com.feelingpilates.transicion.programacion.r4.adapter.jpa.mapper.AdjustmentProjectionMapper;
import com.feelingpilates.transicion.programacion.r4.read.*;
import org.springframework.transaction.annotation.*;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import java.time.LocalDate;
import java.util.Objects;

public final class AdjustmentJpaReader implements AdjustmentReadPort {
    private final AdjustmentProjectionQueryExecutor executor;
    private final AdjustmentProjectionMapper mapper;
    private final AdjustmentProjectionCatalog catalog;
    private final ContextAuthority authority;
    public AdjustmentJpaReader(AdjustmentProjectionQueryExecutor executor, AdjustmentProjectionMapper mapper,
                            AdjustmentProjectionCatalog catalog, ContextAuthority authority) {
        this.executor = Objects.requireNonNull(executor); this.mapper = Objects.requireNonNull(mapper);
        this.catalog = Objects.requireNonNull(catalog); this.authority = Objects.requireNonNull(authority);
        if (catalog != AdjustmentProjectionCatalog.R4_ADJUSTMENT_V1 || executor.catalog() != catalog
               ) throw new IllegalStateException("R4 catalog binding");
    }
    @Override
    @Transactional(transactionManager="f2eReaderTransactionManager", propagation=Propagation.MANDATORY, readOnly=true)
    public AdjustmentReadSet readActiveAdjustmentsOnDate(AdjustmentReadSnapshotContext context, LocalDate fecha) {
        if (context == null || fecha == null || !fecha.equals(context.fecha()))
            throw new AdjustmentReadFailure(AdjustmentReadFailure.Category.INVALID_INPUT, fecha);
        if (!TransactionSynchronizationManager.isActualTransactionActive()
                || !TransactionSynchronizationManager.isSynchronizationActive()
                || !TransactionSynchronizationManager.isCurrentTransactionReadOnly()
                || !Objects.equals(TransactionSynchronizationManager.getCurrentTransactionIsolationLevel(),
                    TransactionDefinition.ISOLATION_REPEATABLE_READ))
            throw new AdjustmentReadFailure(AdjustmentReadFailure.Category.TRANSACTION_CONTEXT_INVALID, fecha);
        try {
            Object proxy = org.springframework.aop.framework.AopContext.currentProxy();
            if (!(proxy instanceof AdjustmentReadPort)
                    || !(proxy instanceof org.springframework.aop.framework.Advised advised)
                    || advised.getTargetSource().getTarget() != this)
                throw new AdjustmentReadFailure(AdjustmentReadFailure.Category.TRANSACTION_CONTEXT_INVALID, fecha);
        } catch (AdjustmentReadFailure e) { throw e; }
        catch (Exception e) {
            throw new AdjustmentReadFailure(AdjustmentReadFailure.Category.TRANSACTION_CONTEXT_INVALID, fecha,
                    java.util.List.of(), e);
        }
        if (context.projectionCatalogVersion() != catalog.version())
            throw new AdjustmentReadFailure(AdjustmentReadFailure.Category.TRANSACTION_CONTEXT_INVALID, fecha);
        try { authority.verify(context, fecha); }
        catch (AdjustmentReadFailure e) { throw e; }
        catch (RuntimeException e) {
            throw new AdjustmentReadFailure(AdjustmentReadFailure.Category.TRANSACTION_CONTEXT_INVALID, fecha,
                    java.util.List.of(), e);
        }
        return mapper.map(executor.read(fecha), context, fecha);
    }
    @FunctionalInterface public interface ContextAuthority {
        void verify(AdjustmentReadSnapshotContext context, LocalDate fecha);
    }
}
