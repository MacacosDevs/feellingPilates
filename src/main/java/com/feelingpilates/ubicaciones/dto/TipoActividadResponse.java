package com.feelingpilates.ubicaciones.dto;

import java.util.List;
import java.util.UUID;

public record TipoActividadResponse(
        UUID id,
        String nombre,
        String descripcion,
        boolean activo,
        short duracionMinutos,
        short participantesPorReserva,
        List<String> etiquetas) {
}
