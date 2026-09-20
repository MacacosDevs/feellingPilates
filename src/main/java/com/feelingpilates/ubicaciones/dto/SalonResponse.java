package com.feelingpilates.ubicaciones.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record SalonResponse(
        UUID id,
        String nombre,
        String direccion,
        Short estadoId,
        String estadoNombre,
        Short municipioId,
        String municipioNombre,
        OffsetDateTime creadoEn) {
}
