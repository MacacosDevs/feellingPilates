package com.feelingpilates.calendario.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record ReservaRequest(
        @NotNull UUID salonId,
        @NotNull UUID instructorId,
        @NotNull UUID clienteId,
        @NotNull UUID tipoActividadId,
        @NotNull LocalDate fecha,
        @NotNull LocalTime horaInicio) {
}
