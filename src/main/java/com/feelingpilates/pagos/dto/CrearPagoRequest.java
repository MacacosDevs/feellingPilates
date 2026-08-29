package com.feelingpilates.pagos.dto;

import java.util.List;
import java.util.UUID;

public record CrearPagoRequest(
        List<UUID> paqueteIds,
        String idempotencyKey
) {
}
