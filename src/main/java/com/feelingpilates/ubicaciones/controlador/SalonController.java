package com.feelingpilates.ubicaciones.controlador;

import com.feelingpilates.ubicaciones.dto.SalonDetalleResponse;
import com.feelingpilates.ubicaciones.dto.SalonRequest;
import com.feelingpilates.ubicaciones.dto.SalonResponse;
import com.feelingpilates.ubicaciones.repositorio.SalonRepository;
import com.feelingpilates.ubicaciones.servicio.SalonService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/salones")
public class SalonController {

    private final SalonRepository salonRepository;
    private final SalonService salonService;

    public SalonController(SalonRepository salonRepository, SalonService salonService) {
        this.salonRepository = salonRepository;
        this.salonService = salonService;
    }

    @GetMapping
    public List<SalonResponse> listar() {
        return salonRepository.listarActivos().stream()
                .map(p -> new SalonResponse(
                        p.getId(), p.getNombre(), p.getDireccion(),
                        p.getEstadoId(), p.getEstadoNombre(),
                        p.getMunicipioId(), p.getMunicipioNombre(),
                        p.getPermitePareja()))
                .toList();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('salon.leer')")
    public SalonDetalleResponse detalle(@PathVariable UUID id) {
        return salonService.obtenerDetalle(id);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('salon.administrar')")
    public ResponseEntity<SalonDetalleResponse> crear(@Valid @RequestBody SalonRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(salonService.crear(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('salon.administrar')")
    public SalonDetalleResponse actualizar(@PathVariable UUID id, @Valid @RequestBody SalonRequest request) {
        return salonService.actualizar(id, request);
    }
}
