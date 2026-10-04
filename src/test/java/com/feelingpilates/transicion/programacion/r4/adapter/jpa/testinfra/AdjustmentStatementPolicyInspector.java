package com.feelingpilates.transicion.programacion.r4.adapter.jpa.testinfra;
import com.feelingpilates.transicion.programacion.r4.adapter.jpa.projection.AdjustmentProjectionCatalog;
import org.hibernate.resource.jdbc.spi.StatementInspector;
/** No registration outside the isolated acceptance context. */
public final class AdjustmentStatementPolicyInspector implements StatementInspector {
    @Override public String inspect(String sql) {
        AdjustmentProjectionCatalog.R4_ADJUSTMENT_V1.logicalId(sql);
        return sql;
    }
}
