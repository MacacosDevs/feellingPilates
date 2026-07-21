package com.feelingpilates.usuarios.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

/**
 * Alta directa de personal/instructor/admin; el sistema genera una contraseña temporal.
 * salonIds: sedes donde aplica el rol. Obligatorio (al menos 1) para PERSONAL e INSTRUCTOR
 * (pueden estar en varias sedes); debe venir vacío para ADMIN, que es un rol global.
 */
public record CrearPersonalRequest(
        @NotBlank @Email String correo,
        @NotBlank @Size(max = 150) String nombre,
        @Size(max = 30) String telefono,
        @NotBlank @Pattern(regexp = "PERSONAL|INSTRUCTOR|ADMIN", message = "Rol inválido") String rol,
        List<UUID> salonIds) {
}
