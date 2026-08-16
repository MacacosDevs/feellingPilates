package com.feelingpilates.usuarios.dto;

import com.feelingpilates.usuarios.entidad.Usuario;
import com.feelingpilates.usuarios.entidad.UsuarioRol;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record UsuarioResponse(
        UUID id,
        String correo,
        String nombre,
        String telefono,
        String fotoUrl,
        String descripcion,
        String proveedorAuth,
        String estatus,
        List<String> roles,
        List<RolAsignadoResponse> rolesAsignados,
        OffsetDateTime creadoEn,
        List<String> permisos) {

    public static UsuarioResponse desde(Usuario usuario) {
        return desde(usuario, List.of());
    }

    /** Incluye los permisos efectivos del usuario (usar solo al construir la respuesta del propio usuario logueado). */
    public static UsuarioResponse desde(Usuario usuario, List<String> permisos) {
        Map<String, List<UUID>> sedesPorRol = new LinkedHashMap<>();
        for (UsuarioRol ur : usuario.getRoles()) {
            sedesPorRol
                    .computeIfAbsent(ur.getRol().getNombre(), k -> new java.util.ArrayList<>())
                    .add(ur.getSalon() != null ? ur.getSalon().getId() : null);
        }
        List<RolAsignadoResponse> rolesAsignados = sedesPorRol.entrySet().stream()
                .map(e -> new RolAsignadoResponse(
                        e.getKey(), e.getValue().stream().filter(java.util.Objects::nonNull).toList()))
                .toList();

        return new UsuarioResponse(
                usuario.getId(),
                usuario.getCorreo(),
                usuario.getNombre(),
                usuario.getTelefono(),
                usuario.getFotoUrl(),
                usuario.getDescripcion(),
                usuario.getProveedorAuth().name(),
                usuario.getEstatus().name(),
                usuario.getRoles().stream().map(ur -> ur.getRol().getNombre()).distinct().toList(),
                rolesAsignados,
                usuario.getCreadoEn(),
                permisos);
    }
}
