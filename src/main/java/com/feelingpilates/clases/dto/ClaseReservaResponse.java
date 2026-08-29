package com.feelingpilates.clases.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ClaseReservaResponse(
        UUID id,
        UUID clienteId,
        String clienteNombre,
        String clienteCorreo,
        String estado,
        OffsetDateTime asistioEn,
        OffsetDateTime creadoEn,
        ClaseResponse clase
) {
}
