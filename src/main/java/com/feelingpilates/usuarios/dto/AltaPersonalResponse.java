package com.feelingpilates.usuarios.dto;

/** Respuesta al dar de alta personal: incluye la contraseña temporal, mostrada una sola vez. */
public record AltaPersonalResponse(UsuarioResponse usuario, String contrasenaTemporal) {
}
