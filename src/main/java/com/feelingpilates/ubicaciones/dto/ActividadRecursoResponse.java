package com.feelingpilates.ubicaciones.dto;

import java.util.UUID;

public record ActividadRecursoResponse(
        UUID tipoRecursoId,
        String nombreRecurso,
        short cantidad) {
}
