package com.feelingpilates.calendario.dto;

import com.feelingpilates.calendario.entidad.Reserva;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record ReservaResponse(
        UUID id,
        UUID salonId,
        String salonNombre,
        UUID instructorId,
        String instructorNombre,
        UUID clienteId,
        String clienteNombre,
        UUID tipoActividadId,
        String tipoActividadNombre,
        LocalDate fecha,
        LocalTime horaInicio,
        LocalTime horaFin,
        Reserva.Estado estado) {
}
