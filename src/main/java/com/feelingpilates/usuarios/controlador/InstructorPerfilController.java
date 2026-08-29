package com.feelingpilates.usuarios.controlador;

import com.feelingpilates.seguridad.UsuarioAutenticado;
import com.feelingpilates.usuarios.dto.ActualizarPerfilInstructorRequest;
import com.feelingpilates.usuarios.dto.PerfilInstructorResponse;
import com.feelingpilates.usuarios.servicio.PerfilInstructorService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/instructores/me")
public class InstructorPerfilController {

    private final PerfilInstructorService perfilInstructorService;

    public InstructorPerfilController(PerfilInstructorService perfilInstructorService) {
        this.perfilInstructorService = perfilInstructorService;
    }

    @PutMapping("/perfil")
    public PerfilInstructorResponse actualizarMiPerfil(@AuthenticationPrincipal UsuarioAutenticado usuario,
                                                        @Valid @RequestBody ActualizarPerfilInstructorRequest request) {
        return perfilInstructorService.actualizarMiPerfil(usuario.id(), request);
    }
}
