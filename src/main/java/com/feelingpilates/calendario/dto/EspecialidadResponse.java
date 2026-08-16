package com.feelingpilates.calendario.dto;

import java.util.UUID;

public record EspecialidadResponse(UUID tipoActividadId, String nombre, short duracionMinutos) {
}
