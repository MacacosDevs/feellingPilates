package com.feelingpilates.pagos.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CrearPaqueteRequest(
        @NotBlank String nombre,
        String descripcion,
        @Min(1) int precioCentavos,
        @Min(1) int vigenciaDias,
        String unitarioTexto,
        boolean destacado,
        int orden,
        @NotEmpty @Valid List<ActividadPaqueteRequest> actividades
) {
}
