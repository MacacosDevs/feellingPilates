package com.feelingpilates.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegistroRequest(
        @NotBlank @Email String correo,
        @NotBlank @Size(min = 8, max = 72) String contrasena,
        @NotBlank String nombre) {
}
