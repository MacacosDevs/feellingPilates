package com.feelingpilates.ubicaciones.dto;

import java.util.UUID;

public record MaquinaItemResponse(UUID tipoMaquinaId, String nombre, short cantidad) {
}
