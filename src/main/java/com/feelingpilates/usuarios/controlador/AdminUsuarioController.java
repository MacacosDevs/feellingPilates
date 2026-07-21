package com.feelingpilates.usuarios.controlador;

import com.feelingpilates.usuarios.dto.ActualizarPerfilRequest;
import com.feelingpilates.usuarios.dto.ActualizarSedesRequest;
import com.feelingpilates.usuarios.dto.AltaPersonalResponse;
import com.feelingpilates.usuarios.dto.CrearClienteRequest;
import com.feelingpilates.usuarios.dto.CrearPersonalRequest;
import com.feelingpilates.usuarios.dto.RolConteoResponse;
import com.feelingpilates.usuarios.dto.UsuarioResponse;
import com.feelingpilates.usuarios.entidad.Usuario;
import com.feelingpilates.usuarios.servicio.AltaUsuarioService;
import com.feelingpilates.usuarios.servicio.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/usuarios")
public class AdminUsuarioController {

    private final UsuarioService usuarioService;
    private final AltaUsuarioService altaUsuarioService;

    public AdminUsuarioController(UsuarioService usuarioService, AltaUsuarioService altaUsuarioService) {
        this.usuarioService = usuarioService;
        this.altaUsuarioService = altaUsuarioService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('usuario.leer')")
    public Page<UsuarioResponse> listar(
            Pageable pageable,
            @RequestParam(required = false) String rol,
            @RequestParam(required = false) String busqueda) {
        return usuarioService.listar(pageable, rol, busqueda);
    }

    @GetMapping("/conteo-roles")
    @PreAuthorize("hasAuthority('usuario.leer')")
    public List<RolConteoResponse> conteoPorRol() {
        return usuarioService.contarPorRol();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('usuario.leer')")
    public UsuarioResponse detalle(@PathVariable UUID id) {
        return usuarioService.obtenerPorId(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('usuario.administrar')")
    public UsuarioResponse actualizar(@PathVariable UUID id, @Valid @RequestBody ActualizarPerfilRequest request) {
        return usuarioService.actualizarPerfil(id, request);
    }

    /**
     * Reemplaza las sedes de un rol del usuario (PERSONAL, INSTRUCTOR o CLIENTE).
     * No aplica a ADMIN/SUPER_ADMIN, que son roles globales.
     */
    @PutMapping("/{id}/roles/{rol}/sedes")
    @PreAuthorize("hasAuthority('usuario.administrar')")
    public UsuarioResponse actualizarSedes(
            @PathVariable UUID id, @PathVariable String rol, @RequestBody ActualizarSedesRequest request) {
        return usuarioService.actualizarSedesRol(id, rol, request.salonIds());
    }

    /**
     * Alta de cliente (visita en sede). No requiere contraseña: se le envía una
     * invitación por correo para que él mismo la asigne.
     */
    @PostMapping("/clientes")
    @PreAuthorize("hasAuthority('usuario.crear.cliente')")
    public ResponseEntity<UsuarioResponse> crearCliente(@Valid @RequestBody CrearClienteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(altaUsuarioService.crearCliente(request));
    }

    /**
     * Alta de personal/instructor/admin. Solo disponible para quien tiene
     * 'usuario.administrar' (por ahora, solo ADMIN). Genera una contraseña temporal
     * que debe compartirse manualmente con la persona dada de alta.
     */
    @PostMapping("/personal")
    @PreAuthorize("hasAuthority('usuario.administrar')")
    public ResponseEntity<AltaPersonalResponse> crearPersonal(@Valid @RequestBody CrearPersonalRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(altaUsuarioService.crearPersonal(request));
    }

    /**
     * Activar/desactivar: disponible para ADMIN y PERSONAL. La gestión completa
     * (alta de personal, eliminar) sigue siendo exclusiva de ADMIN.
     */
    @PatchMapping("/{id}/suspender")
    @PreAuthorize("hasAuthority('usuario.activar')")
    public UsuarioResponse suspender(@PathVariable UUID id) {
        return usuarioService.cambiarEstatus(id, Usuario.EstatusUsuario.suspendido);
    }

    @PatchMapping("/{id}/reactivar")
    @PreAuthorize("hasAuthority('usuario.activar')")
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
