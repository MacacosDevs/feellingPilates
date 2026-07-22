package com.feelingpilates.ubicaciones.dto;

import java.time.LocalTime;
import java.util.UUID;

public record HorarioOperacionResponse(UUID id, short diaSemana, LocalTime horaApertura, LocalTime horaCierre) {
}
