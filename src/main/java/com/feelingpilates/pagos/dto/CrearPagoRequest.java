package com.feelingpilates.pagos.dto;

public record CrearPagoRequest(
        String idempotencyKey
) {
}
