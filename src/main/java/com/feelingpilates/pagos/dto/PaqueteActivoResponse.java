package com.feelingpilates.pagos.dto;

import java.time.OffsetDateTime;

public record PaqueteActivoResponse(
        String categoria,
        String nombre,
        OffsetDateTime fechaInicio,
        OffsetDateTime fechaExpiracion
) {
}
