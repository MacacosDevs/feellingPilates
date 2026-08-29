package com.feelingpilates.usuarios.dto;

import jakarta.validation.constraints.Size;

public record ActualizarPerfilInstructorRequest(
        @Size(max = 2000) String sobreSuClase,
        @Size(max = 300) String instagramUrl,
        @Size(max = 300) String facebookUrl,
        @Size(max = 300) String tiktokUrl,
        @Size(max = 300) String whatsappUrl) {
}
