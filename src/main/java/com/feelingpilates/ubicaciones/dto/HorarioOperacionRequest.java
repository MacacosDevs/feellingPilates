package com.feelingpilates.ubicaciones.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;

public record HorarioOperacionRequest(
        @NotNull Short diaSemana,
        @NotNull LocalTime horaApertura,
        @NotNull LocalTime horaCierre) {
}
