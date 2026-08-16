package com.feelingpilates.calendario.dto;

import com.feelingpilates.calendario.entidad.TurnoInstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public record TurnoInstructorResponse(
        UUID id,
        List<InstructorResumen> instructores,
        UUID salonId,
        String salonNombre,
        TurnoInstructor.Tipo tipo,
        Short diaSemana,
        LocalDate fecha,
        LocalTime horaInicio,
        LocalTime horaFin,
        List<ActividadResumen> actividades,
        List<InstructorAsignacionResponse> asignaciones) {

    public record InstructorResumen(UUID id, String nombre) {
    }

    public record ActividadResumen(UUID id, String nombre) {
    }

    /**
     * Que actividades especificas da un instructor en este bloque y que lapso cubre: horaInicio
     * y horaFin nulos significa que cubre el turno completo.
     */
    public record InstructorAsignacionResponse(
            UUID instructorId,
            String instructorNombre,
            List<ActividadResumen> actividades,
            LocalTime horaInicio,
            LocalTime horaFin) {
    }
}
