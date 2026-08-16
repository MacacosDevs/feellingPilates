package com.feelingpilates.calendario.dto;

import com.feelingpilates.calendario.entidad.TurnoInstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record TurnoInstructorResponse(
        UUID id,
        UUID usuarioId,
        String usuarioNombre,
        UUID salonId,
        String salonNombre,
        TurnoInstructor.Tipo tipo,
        Short diaSemana,
        LocalDate fecha,
        LocalTime horaInicio,
        LocalTime horaFin,
        UUID tipoActividadId,
        String tipoActividadNombre) {
}
