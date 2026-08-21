package com.feelingpilates.pagos.dto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record PaqueteGestionResponse(
        UUID id,
        String nombre,
        String descripcion,
        int precioCentavos,
        int vigenciaDias,
        String unitarioTexto,
        boolean destacado,
        boolean activo,
        int orden,
        List<ActividadPaqueteResponse> actividades,
        OffsetDateTime creadoEn
) {
}
