package com.feelingpilates.seguridad;

import java.util.UUID;

/** Principal autenticado que viaja en el SecurityContext. */
public record UsuarioAutenticado(UUID id, String correo) {
}
