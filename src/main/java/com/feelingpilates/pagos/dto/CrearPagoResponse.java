package com.feelingpilates.pagos.dto;

import java.util.List;
import java.util.UUID;

public record CrearPagoResponse(
        List<UUID> compraIds,
        String clientSecret,
        String publishableKey
) {
}
