package com.feelingpilates.calendario.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;
import java.util.List;

/**
 * Mueve un bloque recurrente existente: dia, horas, instructores y actividades. El frontend
 * siempre manda el dia vigente (cambiado o no), asi que no hay ambiguedad de "sin cambios".
 */
public record ActualizarTurnoRequest(
        @NotNull Short diaSemana,
        @NotNull LocalTime horaInicio,
        @NotNull LocalTime horaFin,
        @NotEmpty List<AsignacionInstructorRequest> asignaciones) {
}
