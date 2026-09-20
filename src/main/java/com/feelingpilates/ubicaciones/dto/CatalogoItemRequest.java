package com.feelingpilates.ubicaciones.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

import java.util.List;

public record CatalogoItemRequest(
        @NotBlank String nombre,
        String descripcion,
        @Positive Short duracionMinutos,
        @Positive Short participantesPorReserva,
        List<String> etiquetas) {
}
