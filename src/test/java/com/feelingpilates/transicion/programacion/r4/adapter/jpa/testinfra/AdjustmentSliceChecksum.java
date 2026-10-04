package com.feelingpilates.transicion.programacion.r4.adapter.jpa.testinfra;

import com.feelingpilates.transicion.programacion.r4.read.AdjustmentReadSnapshotContext;
import javax.sql.DataSource;
import java.sql.*;
import java.time.*;
import java.util.*;
/** Dedicated observer, fresh connection and dynamic membership on every call. */
public final class AdjustmentSliceChecksum {
    private final DataSource ds;
    public AdjustmentSliceChecksum(DataSource ds) { this.ds=Objects.requireNonNull(ds); }
    public String onDate(LocalDate fecha) {
        List<String> values=new ArrayList<>(List.of("F2E-R4-OBSERVER-V1","R4_ACTIVE_ADJUSTMENTS_ON_DATE_V1/"+fecha));
        List<String> rows=new ArrayList<>();
        try(var c=ds.getConnection();var q=c.prepareStatement("SELECT id,tipo,fecha,asignacion_serie_id,salon_resultado_id,instructor_resultado_id,tipo_actividad_resultado_id,hora_inicio_resultado,hora_fin_resultado,activo,creado_en,actualizado_en FROM programacion_ajuste_fecha WHERE fecha = ? AND activo = ? ORDER BY fecha,id")) {
            q.setObject(1,fecha,Types.DATE);q.setBoolean(2,true);
            try(var r=q.executeQuery()) {
                while(r.next()) {
                    List<String> f=new ArrayList<>();
                    for(int i=1;i<=12;i++) {
                        Object v=(i==8||i==9)?r.getObject(i,LocalTime.class):(i==11||i==12)?r.getObject(i,OffsetDateTime.class):r.getObject(i);
                        String tag=i==1||i>=4&&i<=7?"UUID":i==2?"TEXT":i==3?"DATE":i==8||i==9?"TIME":i==10?"BOOLEAN":"TIMESTAMPTZ";
                        f.add(Integer.toString(i));f.add(tag);f.add(v==null?"SQL_NULL":"VALUE");
                        f.add(v==null?"SQL_NULL":v instanceof java.sql.Date d?d.toLocalDate().toString():v instanceof OffsetDateTime t?t.toInstant().toString():v.toString());
                    }
                    rows.add(com.feelingpilates.transicion.programacion.read.ReadSnapshotIdentifiers.decodificarUtf8(
                            com.feelingpilates.transicion.programacion.read.ReadSnapshotIdentifiers.secuenciaTextos(f.toArray(String[]::new))));
                }
            }
        } catch(SQLException e) { throw new IllegalStateException(e); }
        values.add(Integer.toString(rows.size()));values.addAll(rows);
        return AdjustmentReadSnapshotContext.hash(values.toArray(String[]::new));
    }
}
