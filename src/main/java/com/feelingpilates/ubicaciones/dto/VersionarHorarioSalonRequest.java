package com.feelingpilates.ubicaciones.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

public record VersionarHorarioSalonRequest(
        @NotNull Short diaSemana,
        @NotNull LocalDate efectivoDesde,
        @NotNull LocalTime horaApertura,
        @NotNull LocalTime horaCierre) {
}
