package com.feelingpilates.ubicaciones.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ActividadRecursoRequest(
        @NotNull UUID tipoRecursoId,
        @Min(1) short cantidad) {
}
