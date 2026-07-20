package com.feelingpilates.usuarios.controlador;

import com.feelingpilates.seguridad.UsuarioAutenticado;
import com.feelingpilates.usuarios.dto.ActualizarPerfilRequest;
import com.feelingpilates.usuarios.dto.UsuarioResponse;
import com.feelingpilates.usuarios.servicio.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/me")
    public UsuarioResponse miPerfil(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return usuarioService.obtenerPorId(usuario.id());
    }

    @PutMapping("/me")
    public UsuarioResponse actualizarMiPerfil(@AuthenticationPrincipal UsuarioAutenticado usuario,
                                              @Valid @RequestBody ActualizarPerfilRequest request) {
        return usuarioService.actualizarPerfil(usuario.id(), request);
    }
}
