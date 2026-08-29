package com.feelingpilates.clases.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record ClaseResponse(
        UUID id,
        UUID salonId,
        String salonNombre,
        UUID tipoActividadId,
        String tipoActividadNombre,
        UUID instructorId,
        String instructorNombre,
        LocalDate fecha,
        LocalTime horaInicio,
        LocalTime horaFin,
        short capacidad,
        int lugaresOcupados,
        String estado
) {
}
