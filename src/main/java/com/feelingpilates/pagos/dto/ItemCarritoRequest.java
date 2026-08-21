package com.feelingpilates.pagos.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ItemCarritoRequest(
        @NotNull UUID paqueteId,
        @Min(1) int cantidad
) {
}
