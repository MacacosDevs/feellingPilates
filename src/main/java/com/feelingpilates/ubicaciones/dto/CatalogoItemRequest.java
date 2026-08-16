package com.feelingpilates.ubicaciones.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record CatalogoItemRequest(@NotBlank String nombre, String descripcion, @Positive Short duracionMinutos) {
}
