package com.feelingpilates.pagos.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.List;
import java.util.UUID;

public record RegistrarVentaCarritoRequest(
        @NotNull UUID clienteId,
        @NotNull UUID salonId,
        @Pattern(regexp = "efectivo|transferencia") String metodoPago,
        @NotEmpty @Valid List<ItemCarritoRequest> items
) {
}
