package com.feelingpilates.ubicaciones.dto;

import java.util.UUID;

public record TipoRecursoResponse(UUID id, String nombre, String descripcion, boolean activo) {
}
