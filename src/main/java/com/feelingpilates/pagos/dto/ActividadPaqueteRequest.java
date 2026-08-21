package com.feelingpilates.pagos.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ActividadPaqueteRequest(
        @NotNull UUID tipoActividadId,
        @Min(1) int cantidadClases
) {
}
