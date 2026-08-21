package com.feelingpilates.calendario.controlador;

import com.feelingpilates.calendario.dto.ActualizarTurnoRequest;
import com.feelingpilates.calendario.dto.TurnoInstructorRequest;
import com.feelingpilates.calendario.dto.TurnoInstructorResponse;
import com.feelingpilates.calendario.entidad.TurnoInstructor;
import com.feelingpilates.calendario.servicio.TurnoInstructorService;
import com.feelingpilates.seguridad.UsuarioAutenticado;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/turnos-instructor")
public class TurnoInstructorController {

    private final TurnoInstructorService turnoInstructorService;

    public TurnoInstructorController(TurnoInstructorService turnoInstructorService) {
        this.turnoInstructorService = turnoInstructorService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('calendario.leer')")
    public List<TurnoInstructorResponse> listar(
            @RequestParam UUID salonId, @RequestParam(required = false) UUID usuarioId) {
        return usuarioId == null
                ? turnoInstructorService.listarPorSalon(salonId)
                : turnoInstructorService.listarPorInstructorYSalon(usuarioId, salonId);
    }

    /** Excepciones/cancelaciones (con fecha) de un salón, paginadas y filtrables por instructor, tipo y día de la semana. */
    @GetMapping("/puntuales")
    @PreAuthorize("hasAuthority('calendario.leer')")
    public Page<TurnoInstructorResponse> listarPuntuales(
            @RequestParam UUID salonId,
            @RequestParam(required = false) UUID usuarioId,
            @RequestParam(required = false) TurnoInstructor.Tipo tipo,
            @RequestParam(required = false) Integer diaSemana,
            Pageable pageable) {
        return turnoInstructorService.listarPuntualesPaginado(salonId, usuarioId, tipo, diaSemana, pageable);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('calendario.gestionar') "
            + "or (#request.tipo().name() == 'CANCELACION' and hasAuthority('calendario.cancelar')) "
            + "or (#request.tipo().name() == 'EXCEPCION' and hasAuthority('calendario.editar'))")
    public ResponseEntity<TurnoInstructorResponse> crear(
            @AuthenticationPrincipal UsuarioAutenticado actor,
            @Valid @RequestBody TurnoInstructorRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(turnoInstructorService.crear(actor.id(), request));
    }

    /** Mueve un bloque recurrente existente (dia, hora, instructores, actividades). */
    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('calendario.gestionar') or hasAuthority('calendario.editar')")
    public TurnoInstructorResponse actualizarTurno(
            @AuthenticationPrincipal UsuarioAutenticado actor,
            @PathVariable UUID id, @Valid @RequestBody ActualizarTurnoRequest request) {
        return turnoInstructorService.actualizarTurno(actor.id(), id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('calendario.gestionar')")
    public ResponseEntity<Void> eliminar(
            @AuthenticationPrincipal UsuarioAutenticado actor, @PathVariable UUID id) {
        turnoInstructorService.eliminar(actor.id(), id);
        return ResponseEntity.noContent().build();
    }
}
