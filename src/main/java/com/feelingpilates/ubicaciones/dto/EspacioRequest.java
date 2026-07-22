package com.feelingpilates.ubicaciones.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record EspacioRequest(
        @NotBlank String nombre,
        @NotNull @Min(1) Short capacidad,
        boolean permitePareja,
        List<MaquinaItem> maquinas) {
}
