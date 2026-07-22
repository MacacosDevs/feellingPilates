package com.feelingpilates.ubicaciones.dto;

import java.util.UUID;

public record TipoMaquinaResponse(UUID id, String nombre, String descripcion, boolean activo) {
}
