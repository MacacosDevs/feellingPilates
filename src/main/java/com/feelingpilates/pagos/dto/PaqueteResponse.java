package com.feelingpilates.pagos.dto;

import java.util.List;
import java.util.UUID;

public record PaqueteResponse(
        UUID id,
        String categoria,
        String nombre,
        String descripcion,
        int precioCentavos,
        int vigenciaDias,
        String unitarioTexto,
        boolean destacado,
        List<ActividadPaqueteResponse> actividades
) {
}
