package com.feelingpilates.ubicaciones.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record SalonHorarioExcepcionResponse(
        UUID id,
        LocalDate fecha,
        boolean cerrado,
        LocalTime horaApertura,
        LocalTime horaCierre) {
}
