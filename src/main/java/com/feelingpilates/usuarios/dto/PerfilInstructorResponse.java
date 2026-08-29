package com.feelingpilates.usuarios.dto;

import com.feelingpilates.usuarios.entidad.PerfilInstructor;
import com.feelingpilates.usuarios.entidad.Usuario;

import java.math.BigDecimal;
import java.util.UUID;

public record PerfilInstructorResponse(
        UUID usuarioId,
        String nombre,
        String fotoUrl,
        String sobreSuClase,
        BigDecimal calificacionPromedio,
        String instagramUrl,
        String facebookUrl,
        String tiktokUrl,
        String whatsappUrl) {

    public static PerfilInstructorResponse desde(Usuario usuario, PerfilInstructor perfil) {
        return new PerfilInstructorResponse(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getFotoUrl(),
                perfil != null ? perfil.getSobreSuClase() : null,
                perfil != null ? perfil.getCalificacionPromedio() : null,
                perfil != null ? perfil.getInstagramUrl() : null,
                perfil != null ? perfil.getFacebookUrl() : null,
                perfil != null ? perfil.getTiktokUrl() : null,
                perfil != null ? perfil.getWhatsappUrl() : null);
    }
}
