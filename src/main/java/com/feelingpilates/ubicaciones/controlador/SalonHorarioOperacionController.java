package com.feelingpilates.ubicaciones.controlador;

import com.feelingpilates.seguridad.UsuarioAutenticado;
import com.feelingpilates.ubicaciones.dto.CerrarHorarioSalonRequest;
import com.feelingpilates.ubicaciones.dto.HorarioOperacionVersionResponse;
import com.feelingpilates.ubicaciones.dto.VersionarHorarioSalonRequest;
import com.feelingpilates.ubicaciones.servicio.SalonHorarioOperacionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/salones/{salonId}/horarios")
public class SalonHorarioOperacionController {

    private final SalonHorarioOperacionService horarioOperacionService;

    public SalonHorarioOperacionController(SalonHorarioOperacionService horarioOperacionService) {
        this.horarioOperacionService = horarioOperacionService;
    }

    @PostMapping("/versiones")
    @PreAuthorize("hasAuthority('salon.administrar')")
    public ResponseEntity<HorarioOperacionVersionResponse> versionar(
            @AuthenticationPrincipal UsuarioAutenticado actor,
            @PathVariable UUID salonId,
            @Valid @RequestBody VersionarHorarioSalonRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(horarioOperacionService.versionar(actor.id(), salonId, request));
    }

    @PostMapping("/cierres")
    @PreAuthorize("hasAuthority('salon.administrar')")
    public HorarioOperacionVersionResponse cerrar(
            @AuthenticationPrincipal UsuarioAutenticado actor,
            @PathVariable UUID salonId,
            @Valid @RequestBody CerrarHorarioSalonRequest request) {
        return horarioOperacionService.cerrar(actor.id(), salonId, request);
    }

    @GetMapping("/historial")
    @PreAuthorize("hasAuthority('salon.leer')")
    public List<HorarioOperacionVersionResponse> historial(
            @AuthenticationPrincipal UsuarioAutenticado actor,
            @PathVariable UUID salonId,
            @RequestParam(required = false) Short diaSemana) {
        return horarioOperacionService.consultarHistorial(actor.id(), salonId, diaSemana);
    }
}
