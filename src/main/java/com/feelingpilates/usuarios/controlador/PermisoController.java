package com.feelingpilates.usuarios.controlador;

import com.feelingpilates.usuarios.dto.PermisoResponse;
import com.feelingpilates.usuarios.servicio.RolService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Catálogo de permisos (código + descripción) para cualquier usuario autenticado,
 * sin exponer qué rol tiene cuáles (eso sigue siendo exclusivo de RolController).
 * El frontend lo usa para armar mensajes de "no tienes permiso para X" sin
 * hardcodear el texto de cada permiso en cada pantalla.
 */
@RestController
@RequestMapping("/api/permisos")
public class PermisoController {

    private final RolService rolService;

    public PermisoController(RolService rolService) {
        this.rolService = rolService;
    }

    @GetMapping
    public List<PermisoResponse> listar() {
        return rolService.listarPermisos();
    }
}
