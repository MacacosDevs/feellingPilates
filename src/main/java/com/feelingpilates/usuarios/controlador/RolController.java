package com.feelingpilates.usuarios.controlador;

import com.feelingpilates.usuarios.dto.ActualizarPermisosRolRequest;
import com.feelingpilates.usuarios.dto.ActualizarRolRequest;
import com.feelingpilates.usuarios.dto.CrearRolRequest;
import com.feelingpilates.usuarios.dto.PermisoResponse;
import com.feelingpilates.usuarios.dto.RolResponse;
import com.feelingpilates.usuarios.servicio.RolService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Gestión de permisos por rol. Exclusivo de SUPER_ADMIN: evita que un ADMIN
 * pueda ampliarse sus propios permisos (escalación de privilegios).
 */
@RestController
@RequestMapping("/api/admin/roles")
public class RolController {

    private final RolService rolService;

    public RolController(RolService rolService) {
        this.rolService = rolService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('rol.permisos.gestionar')")
    public List<RolResponse> listar() {
        return rolService.listar();
    }

    @GetMapping("/permisos")
    @PreAuthorize("hasAuthority('rol.permisos.gestionar')")
    public List<PermisoResponse> listarPermisos() {
        return rolService.listarPermisos();
    }

    @PutMapping("/{id}/permisos")
    @PreAuthorize("hasAuthority('rol.permisos.gestionar')")
    public RolResponse actualizarPermisos(@PathVariable UUID id, @Valid @RequestBody ActualizarPermisosRolRequest request) {
        return rolService.actualizarPermisos(id, request.permisos());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('rol.gestionar')")
    public RolResponse crear(@Valid @RequestBody CrearRolRequest request) {
        return rolService.crear(request.nombre(), request.descripcion());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('rol.gestionar')")
    public RolResponse actualizar(@PathVariable UUID id, @Valid @RequestBody ActualizarRolRequest request) {
        return rolService.actualizarDatos(id, request.nombre(), request.descripcion());
    }
}
