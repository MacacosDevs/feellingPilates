package com.feelingpilates.pagos.dto;

import jakarta.validation.constraints.NotBlank;

public record CambiarEstadoVentaRequest(
        @NotBlank String motivo
) {
}
