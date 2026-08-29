package com.feelingpilates.clases.controlador;

import com.feelingpilates.clases.dto.CheckinRequest;
import com.feelingpilates.clases.dto.ClaseReservaResponse;
import com.feelingpilates.clases.servicio.ClaseReservaService;
import com.feelingpilates.seguridad.UsuarioAutenticado;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@RestController
public class ClaseReservaController {

    private static final Set<String> ROLES_STAFF = Set.of("ROLE_ADMIN", "ROLE_PERSONAL");

    private final ClaseReservaService claseReservaService;

    public ClaseReservaController(ClaseReservaService claseReservaService) {
        this.claseReservaService = claseReservaService;
    }

    @PostMapping("/api/clases/{id}/reservas")
    @PreAuthorize("hasAuthority('clase.reservar')")
    public ResponseEntity<ClaseReservaResponse> reservar(
            @AuthenticationPrincipal UsuarioAutenticado usuario, @PathVariable UUID id) {
        return ResponseEntity.status(HttpStatus.CREATED).body(claseReservaService.reservar(id, usuario.id()));
    }

    @DeleteMapping("/api/clases/reservas/{id}")
    @PreAuthorize("hasAuthority('clase.reservar')")
    public ResponseEntity<Void> cancelar(@AuthenticationPrincipal UsuarioAutenticado usuario, @PathVariable UUID id) {
        claseReservaService.cancelar(id, usuario.id());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/clases/mis-reservas")
    @PreAuthorize("hasAuthority('clase.reservar')")
    public List<ClaseReservaResponse> misReservas(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return claseReservaService.listarMias(usuario.id());
    }

    @GetMapping("/api/clases/{id}/reservas")
    @PreAuthorize("hasAuthority('clase.checkin')")
    public List<ClaseReservaResponse> listarAsistentes(
            @AuthenticationPrincipal UsuarioAutenticado usuario, @PathVariable UUID id, Authentication authentication) {
        return claseReservaService.listarAsistentes(id, usuario.id(), esStaff(authentication));
    }

    @PostMapping("/api/clases/reservas/{id}/checkin")
    @PreAuthorize("hasAuthority('clase.checkin')")
    public ClaseReservaResponse checkin(
            @AuthenticationPrincipal UsuarioAutenticado usuario,
            @PathVariable UUID id,
            @RequestBody CheckinRequest request,
            Authentication authentication) {
        return claseReservaService.checkin(id, request.claseId(), usuario.id(), esStaff(authentication));
    }

    // ADMIN/PERSONAL pueden pasar lista o hacer checkin de cualquier clase, no solo las
    // propias como instructor; ambos roles ya vienen en el JWT como ROLE_* (JwtAuthFilter).
    private boolean esStaff(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(ROLES_STAFF::contains);
    }
}
