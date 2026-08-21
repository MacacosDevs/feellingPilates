package com.feelingpilates.ubicaciones.controlador;

import com.feelingpilates.ubicaciones.dto.CatalogoItemRequest;
import com.feelingpilates.ubicaciones.dto.TipoActividadResponse;
import com.feelingpilates.ubicaciones.entidad.TipoActividad;
import com.feelingpilates.ubicaciones.repositorio.TipoActividadRepository;
import jakarta.validation.Valid;
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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tipos-actividad")
public class TipoActividadController {

    private final TipoActividadRepository repository;

    public TipoActividadController(TipoActividadRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('actividades.leer')")
    public List<TipoActividadResponse> listar(@RequestParam(required = false) String buscar) {
        List<TipoActividad> tipos = (buscar == null || buscar.isBlank())
                ? repository.findByActivoTrueOrderByNombre()
                : repository.buscarActivosPorNombreOEtiqueta(buscar.trim());
        return tipos.stream().map(this::aResponse).toList();
    }

    @PostMapping
    @PreAuthorize("hasAuthority('actividades.gestionar')")
    public ResponseEntity<TipoActividadResponse> crear(@Valid @RequestBody CatalogoItemRequest request) {
        TipoActividad tipo = new TipoActividad();
        tipo.setNombre(request.nombre());
        tipo.setDescripcion(request.descripcion());
        if (request.duracionMinutos() != null) {
            tipo.setDuracionMinutos(request.duracionMinutos());
        }
        if (request.participantesPorReserva() != null) {
            tipo.setParticipantesPorReserva(request.participantesPorReserva());
        }
        tipo.setEtiquetas(normalizarEtiquetas(request.etiquetas()));
        return ResponseEntity.status(HttpStatus.CREATED).body(aResponse(repository.save(tipo)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('actividades.gestionar')")
    public TipoActividadResponse actualizar(@PathVariable UUID id, @Valid @RequestBody CatalogoItemRequest request) {
        TipoActividad tipo = repository.findById(id).orElseThrow();
        tipo.setNombre(request.nombre());
        tipo.setDescripcion(request.descripcion());
        if (request.duracionMinutos() != null) {
            tipo.setDuracionMinutos(request.duracionMinutos());
        }
        if (request.participantesPorReserva() != null) {
            tipo.setParticipantesPorReserva(request.participantesPorReserva());
        }
        tipo.setEtiquetas(normalizarEtiquetas(request.etiquetas()));
        return aResponse(repository.save(tipo));
    }

    @PatchMapping("/{id}/desactivar")
    @PreAuthorize("hasAuthority('actividades.gestionar')")
    public TipoActividadResponse desactivar(@PathVariable UUID id) {
        TipoActividad tipo = repository.findById(id).orElseThrow();
        tipo.setActivo(false);
        return aResponse(repository.save(tipo));
    }

    private List<String> normalizarEtiquetas(List<String> etiquetas) {
        if (etiquetas == null) return new ArrayList<>();
        return etiquetas.stream()
                .map(String::trim)
                .filter(e -> !e.isEmpty())
                .distinct()
                .toList();
    }

    private TipoActividadResponse aResponse(TipoActividad t) {
        return new TipoActividadResponse(
                t.getId(), t.getNombre(), t.getDescripcion(), t.isActivo(), t.getDuracionMinutos(),
                t.getParticipantesPorReserva(), t.getEtiquetas());
    }
}
