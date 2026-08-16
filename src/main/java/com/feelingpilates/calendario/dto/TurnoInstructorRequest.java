package com.feelingpilates.calendario.dto;

import com.feelingpilates.calendario.entidad.TurnoInstructor;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record TurnoInstructorRequest(
        @NotNull UUID usuarioId,
        @NotNull UUID salonId,
        @NotNull TurnoInstructor.Tipo tipo,
        Short diaSemana,
        LocalDate fecha,
        @NotNull LocalTime horaInicio,
        @NotNull LocalTime horaFin,
        UUID tipoActividadId) {
}
