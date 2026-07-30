package com.feelingpilates.pagos.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record CompraResponse(
        UUID id,
        String paqueteNombre,
        String categoria,
        int montoCentavos,
        String estado,
        OffsetDateTime creadoEn,
        OffsetDateTime fechaExpiracion
) {
}
