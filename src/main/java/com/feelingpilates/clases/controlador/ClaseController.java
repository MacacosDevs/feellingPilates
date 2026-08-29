package com.feelingpilates.clases.controlador;

import com.feelingpilates.clases.dto.ClaseResponse;
import com.feelingpilates.clases.servicio.ClaseService;
import com.feelingpilates.seguridad.UsuarioAutenticado;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
public class ClaseController {

    private final ClaseService claseService;

    public ClaseController(ClaseService claseService) {
        this.claseService = claseService;
    }

    // Catálogo público de clases (ver el calendario no requiere sesión; reservar sí).
    @GetMapping("/api/publico/clases")
    public List<ClaseResponse> listarPublico(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) UUID salonId) {
        return claseService.listarPublico(desde, hasta, salonId);
    }

    @GetMapping("/api/publico/clases/{id}")
    public ClaseResponse obtener(@PathVariable UUID id) {
        return claseService.obtener(id);
    }

    // Las clases que el instructor autenticado imparte, para "Mi calendario".
    @GetMapping("/api/clases/mias-instructor")
    public List<ClaseResponse> misClasesComoInstructor(
            @AuthenticationPrincipal UsuarioAutenticado usuario,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return claseService.listarPorInstructor(usuario.id(), desde, hasta);
    }
}
