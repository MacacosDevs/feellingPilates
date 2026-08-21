package com.feelingpilates.pagos.dto;

import java.util.UUID;

public record ActividadPaqueteResponse(
        UUID tipoActividadId,
        String nombreActividad,
        int cantidadClases
) {
}
