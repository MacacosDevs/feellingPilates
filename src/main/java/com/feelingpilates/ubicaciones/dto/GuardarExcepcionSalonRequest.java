package com.feelingpilates.ubicaciones.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

public record GuardarExcepcionSalonRequest(
        @NotNull LocalDate fecha,
        boolean cerrado,
        LocalTime horaApertura,
        LocalTime horaCierre) {
}
