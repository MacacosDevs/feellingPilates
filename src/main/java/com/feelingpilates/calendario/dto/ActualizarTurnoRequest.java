package com.feelingpilates.calendario.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;
import java.util.UUID;

/**
 * Mueve un turno recurrente existente: dia, horas y actividad. El frontend
 * siempre manda el dia vigente (cambiado o no), asi que no hay ambiguedad de
 * "sin cambios"; tipoActividadId nulo significa "sin actividad asignada".
 */
public record ActualizarTurnoRequest(
        @NotNull Short diaSemana,
        @NotNull LocalTime horaInicio,
        @NotNull LocalTime horaFin,
        UUID tipoActividadId) {
}
