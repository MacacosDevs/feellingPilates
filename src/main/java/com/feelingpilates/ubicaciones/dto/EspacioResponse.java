package com.feelingpilates.ubicaciones.dto;

import java.util.List;
import java.util.UUID;

public record EspacioResponse(
        UUID id,
        String nombre,
        short capacidad,
        boolean permitePareja,
        List<MaquinaItemResponse> maquinas) {
}
