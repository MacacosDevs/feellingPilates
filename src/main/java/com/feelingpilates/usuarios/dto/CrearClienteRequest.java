package com.feelingpilates.usuarios.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

/**
 * Alta de un cliente (visita en sede); se le envía una invitación por correo para fijar su contraseña.
 * salonIds es opcional (puede venir null o vacío): el cliente puede ir a cualquier sede.
 */
public record CrearClienteRequest(
        @NotBlank @Email String correo,
        @NotBlank @Size(max = 150) String nombre,
        @Size(max = 30) String telefono,
        List<UUID> salonIds) {
}
