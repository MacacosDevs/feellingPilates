package com.feelingpilates.calendario.controlador;

import com.feelingpilates.calendario.dto.ReservaRequest;
import com.feelingpilates.calendario.dto.ReservaResponse;
import com.feelingpilates.calendario.servicio.ReservaService;
import com.feelingpilates.seguridad.UsuarioAutenticado;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/reservas")
public class ReservaController {

    private final ReservaService reservaService;

    public ReservaController(ReservaService reservaService) {
        this.reservaService = reservaService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('calendario.leer')")
    public List<ReservaResponse> listarPorSalonYFecha(
            @AuthenticationPrincipal UsuarioAutenticado usuario,
            @RequestParam UUID salonId,
            @RequestParam LocalDate fecha) {
        return reservaService.listarPorSalonYFecha(usuario.id(), salonId, fecha);
    }

    @GetMapping("/mias")
    public List<ReservaResponse> listarMias(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return reservaService.listarPorCliente(usuario.id());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('reserva.administrar')")
    public ResponseEntity<ReservaResponse> crear(
            @AuthenticationPrincipal UsuarioAutenticado usuario,
            @Valid @RequestBody ReservaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reservaService.crear(usuario.id(), request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('reserva.administrar')")
    public ResponseEntity<Void> cancelar(
            @AuthenticationPrincipal UsuarioAutenticado usuario, @PathVariable UUID id) {
        reservaService.cancelar(usuario.id(), id);
        return ResponseEntity.noContent().build();
    }
}
