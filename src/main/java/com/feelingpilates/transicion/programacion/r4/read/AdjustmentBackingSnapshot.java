package com.feelingpilates.transicion.programacion.r4.read;
import java.time.*;
import java.util.*;
import com.feelingpilates.transicion.programacion.read.ReadSnapshotIdentifiers;
/** Physical content only; no persistence handles or execution context. */
public record AdjustmentBackingSnapshot(UUID id, String tipo, LocalDate fecha, UUID asignacionSerieId, UUID salonResultadoId,
        UUID instructorResultadoId, UUID tipoActividadResultadoId, LocalTime horaInicioResultado,
        LocalTime horaFinResultado, Boolean activo, OffsetDateTime creadoEn, OffsetDateTime actualizadoEn) {
    public Map<String,String> normalizedFields() {
        String[] names={"id","tipo","fecha","asignacion_serie_id","salon_resultado_id",
                "instructor_resultado_id","tipo_actividad_resultado_id","hora_inicio_resultado",
                "hora_fin_resultado","activo","creado_en","actualizado_en"};
        Object[] values={id,tipo,fecha,asignacionSerieId,salonResultadoId,instructorResultadoId,
                tipoActividadResultadoId,horaInicioResultado,horaFinResultado,activo,creadoEn,actualizadoEn};
        Map<String,String> fields=new LinkedHashMap<>();
        for(int i=0;i<names.length;i++) {
            Object v=values[i];
            String value=v==null?"ABSENT_BY_ADJUSTMENT_FORM":v instanceof LocalTime t?
                    ReadSnapshotIdentifiers.hora(t):v instanceof OffsetDateTime t?
                    ReadSnapshotIdentifiers.instante(t):v.toString();
            fields.put(names[i],value);
        }
        return Map.copyOf(fields);
    }
}
