package com.feelingpilates.ubicaciones.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CerrarHorarioSalonRequest(
        @NotNull Short diaSemana,
        @NotNull LocalDate efectivoDesde) {
}
