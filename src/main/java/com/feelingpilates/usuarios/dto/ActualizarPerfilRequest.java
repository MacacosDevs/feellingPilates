package com.feelingpilates.usuarios.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ActualizarPerfilRequest(
        @NotBlank @Size(max = 150) String nombre,
        @Size(max = 30) String telefono,
        @Size(max = 500) String fotoUrl,
        String descripcion) {
}
