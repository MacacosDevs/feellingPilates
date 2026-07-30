package com.feelingpilates.pagos.dto;

import java.util.UUID;

public record CrearPagoResponse(
        UUID compraId,
        String clientSecret,
        String publishableKey
) {
}
