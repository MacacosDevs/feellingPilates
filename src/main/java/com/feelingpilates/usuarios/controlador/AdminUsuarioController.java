package com.feelingpilates.usuarios.controlador;

import com.feelingpilates.usuarios.dto.UsuarioResponse;
import com.feelingpilates.usuarios.entidad.Usuario;
import com.feelingpilates.usuarios.servicio.UsuarioService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/usuarios")
public class AdminUsuarioController {

    private final UsuarioService usuarioService;

    public AdminUsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('usuario.leer')")
    public Page<UsuarioResponse> listar(Pageable pageable) {
        return usuarioService.listar(pageable);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('usuario.leer')")
    public UsuarioResponse detalle(@PathVariable UUID id) {
        return usuarioService.obtenerPorId(id);
    }

    @PatchMapping("/{id}/suspender")
    @PreAuthorize("hasAuthority('usuario.administrar')")
    public UsuarioResponse suspender(@PathVariable UUID id) {
        return usuarioService.cambiarEstatus(id, Usuario.EstatusUsuario.suspendido);
    }

    @PatchMapping("/{id}/reactivar")
    @PreAuthorize("hasAuthority('usuario.administrar')")
    public UsuarioResponse reactivar(@PathVariable UUID id) {
        return usuarioService.cambiarEstatus(id, Usuario.EstatusUsuario.activo);
    }

    @PatchMapping("/{id}/eliminar")
    @PreAuthorize("hasAuthority('usuario.administrar')")
    public UsuarioResponse eliminar(@PathVariable UUID id) {
        // Borrado logico: nunca se elimina la fila
        return usuarioService.cambiarEstatus(id, Usuario.EstatusUsuario.eliminado);
    }
}
