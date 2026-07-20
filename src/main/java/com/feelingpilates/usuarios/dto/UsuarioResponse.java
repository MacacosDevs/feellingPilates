package com.feelingpilates.usuarios.dto;

import com.feelingpilates.usuarios.entidad.Usuario;

import java.time.OffsetDateTime;
import java.util.List;
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
        OffsetDateTime creadoEn) {

    public static UsuarioResponse desde(Usuario usuario) {
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
                usuario.getCreadoEn());
    }
}
