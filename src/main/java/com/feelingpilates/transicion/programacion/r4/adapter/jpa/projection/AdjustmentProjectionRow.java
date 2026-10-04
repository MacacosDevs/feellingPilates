package com.feelingpilates.transicion.programacion.r4.adapter.jpa.projection;
import java.time.*;
import java.util.UUID;
/** Twelve scalar fields, in the closed DATA catalog order. */
public record AdjustmentProjectionRow(UUID id, String tipo, LocalDate fecha, UUID asignacionSerieId, UUID salonResultadoId,
        UUID instructorResultadoId, UUID tipoActividadResultadoId, LocalTime horaInicioResultado,
        LocalTime horaFinResultado, Boolean activo, OffsetDateTime creadoEn, OffsetDateTime actualizadoEn) { }
