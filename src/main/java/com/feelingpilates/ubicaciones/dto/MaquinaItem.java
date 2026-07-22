package com.feelingpilates.ubicaciones.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record MaquinaItem(
        @NotNull UUID tipoMaquinaId,
        @Min(1) short cantidad) {
}
