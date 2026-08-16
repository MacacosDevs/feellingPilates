package com.feelingpilates.calendario.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

/**
 * Un instructor del bloque, que actividades especificas da (puede quedar vacio: sin definir), y
 * que lapso cubre dentro del turno: horaInicio/horaFin nulos significa que cubre el bloque
 * completo; si se definen, deben caer dentro del rango del turno.
 */
public record AsignacionInstructorRequest(
        @NotNull UUID instructorId,
        List<UUID> tipoActividadIds,
        LocalTime horaInicio,
        LocalTime horaFin) {
}
