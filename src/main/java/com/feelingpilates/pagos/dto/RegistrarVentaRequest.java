package com.feelingpilates.pagos.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.UUID;

public record RegistrarVentaRequest(
        @NotNull UUID clienteId,
        @NotNull UUID paqueteId,
        @NotNull UUID salonId,
        @Pattern(regexp = "efectivo|transferencia") String metodoPago
) {
}
