package com.feelingpilates.usuarios.dto;

import java.util.List;
import java.util.UUID;

/**
 * @param editable false para SUPER_ADMIN: siempre tiene todos los permisos y no se
 *                 gestiona via rol_permiso, así que no se puede editar desde aquí.
 */
public record RolResponse(UUID id, String nombre, String descripcion, List<String> permisos, boolean editable) {
}
