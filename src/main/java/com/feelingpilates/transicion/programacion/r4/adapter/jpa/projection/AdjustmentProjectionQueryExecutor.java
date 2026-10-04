package com.feelingpilates.transicion.programacion.r4.adapter.jpa.projection;

import com.feelingpilates.transicion.programacion.r4.read.*;
import jakarta.persistence.EntityManager;
import org.hibernate.query.NativeQuery;
import java.time.*;
import java.util.*;

/** Scalar projection only. The exact date and active=true are the only DATA parameters. */
public final class AdjustmentProjectionQueryExecutor {
    private final EntityManager entityManager;
    private final AdjustmentProjectionCatalog catalog;
    public AdjustmentProjectionQueryExecutor(EntityManager entityManager, AdjustmentProjectionCatalog catalog) {
        this.entityManager=Objects.requireNonNull(entityManager); this.catalog=Objects.requireNonNull(catalog);
    }
    public AdjustmentProjectionCatalog catalog() { return catalog; }
    public List<AdjustmentProjectionRow> read(LocalDate fecha) {
        if(fecha==null) throw new AdjustmentReadFailure(AdjustmentReadFailure.Category.INVALID_INPUT,null);
        try {
            NativeQuery<?> q=query("R4_ADJUSTMENTS_ON_DATE_V1");
            var microsecondTime=new org.hibernate.type.internal.BasicTypeImpl<>(org.hibernate.type.descriptor.java.LocalTimeJavaType.INSTANCE,
                    org.hibernate.type.descriptor.jdbc.LocalTimeJdbcType.INSTANCE);
            q.addScalar("id",UUID.class).addScalar("tipo",String.class).addScalar("fecha",LocalDate.class)
                    .addScalar("asignacion_serie_id",UUID.class).addScalar("salon_resultado_id",UUID.class)
                    .addScalar("instructor_resultado_id",UUID.class).addScalar("tipo_actividad_resultado_id",UUID.class)
                    .addScalar("hora_inicio_resultado",microsecondTime).addScalar("hora_fin_resultado",microsecondTime)
                    .addScalar("activo",Boolean.class).addScalar("creado_en",OffsetDateTime.class)
                    .addScalar("actualizado_en",OffsetDateTime.class);
            q.setParameter("fecha",fecha,LocalDate.class);
            q.setParameter("active",Boolean.TRUE,Boolean.class);
            return materialize(q.getResultList(),fecha);
        } catch(AdjustmentReadFailure e) { throw e; }
        catch(RuntimeException e) { throw database(fecha,e); }
    }
    public List<AdjustmentProjectionRow> materialize(List<?> results,LocalDate fecha) {
        if(results==null) throw new AdjustmentReadFailure(AdjustmentReadFailure.Category.MALFORMED_PROJECTION,fecha);
        List<AdjustmentProjectionRow> rows=new ArrayList<>();
        for(Object result:results) {
            try {
                if(!(result instanceof Object[] v) || v.length!=12) throw new IllegalArgumentException("twelve scalars required");
                rows.add(new AdjustmentProjectionRow(cast(v[0],UUID.class),cast(v[1],String.class),cast(v[2],LocalDate.class),
                        cast(v[3],UUID.class),cast(v[4],UUID.class),cast(v[5],UUID.class),cast(v[6],UUID.class),
                        cast(v[7],LocalTime.class),cast(v[8],LocalTime.class),cast(v[9],Boolean.class),
                        cast(v[10],OffsetDateTime.class),cast(v[11],OffsetDateTime.class)));
            } catch(RuntimeException e) {
                List<UUID> ids=result instanceof Object[] v && v.length>0 && v[0] instanceof UUID id?List.of(id):List.of();
                throw new AdjustmentReadFailure(AdjustmentReadFailure.Category.MALFORMED_PROJECTION,fecha,ids,e);
            }
        }
        return List.copyOf(rows);
    }
    private <T> T cast(Object value,Class<T> type) { return value==null?null:type.cast(value); }
    private NativeQuery<?> query(String logicalId) {
        return entityManager.createNativeQuery(catalog.statement(logicalId).sql()).unwrap(NativeQuery.class);
    }
    /** Only the eight closed probes; DATA cannot bypass its typed binding method. */
    public List<?> probe(String logicalId) {
        if(logicalId.equals("R4_ADJUSTMENTS_ON_DATE_V1")) throw new IllegalArgumentException("DATA is not a probe");
        try { return List.copyOf(query(logicalId).getResultList()); }
        catch(AdjustmentReadFailure e) { throw e; }
        catch(RuntimeException e) { throw database(null,e); }
    }
    private AdjustmentReadFailure database(LocalDate fecha,Throwable cause) {
        return new AdjustmentReadFailure(AdjustmentReadFailure.Category.DATABASE_READ_FAILURE,fecha,List.of(),cause);
    }
}
