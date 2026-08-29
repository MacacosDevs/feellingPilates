package com.feelingpilates.usuarios.controlador;

import com.feelingpilates.usuarios.dto.PerfilInstructorResponse;
import com.feelingpilates.usuarios.servicio.PerfilInstructorService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/publico/instructores")
public class InstructorPublicoController {

    private final PerfilInstructorService perfilInstructorService;

    public InstructorPublicoController(PerfilInstructorService perfilInstructorService) {
        this.perfilInstructorService = perfilInstructorService;
    }

    @GetMapping("/{usuarioId}")
    public PerfilInstructorResponse perfil(@PathVariable UUID usuarioId) {
        return perfilInstructorService.obtenerPorUsuarioId(usuarioId);
    }
}
