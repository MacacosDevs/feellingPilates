package com.feelingpilates.ubicaciones.dto;

import java.util.UUID;

public record RecursoItemResponse(UUID tipoRecursoId, String nombre, short cantidad) {
}
