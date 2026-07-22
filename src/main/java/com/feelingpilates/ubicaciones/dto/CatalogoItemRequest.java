package com.feelingpilates.ubicaciones.dto;

import jakarta.validation.constraints.NotBlank;

public record CatalogoItemRequest(@NotBlank String nombre, String descripcion) {
}
