package com.feelingpilates.ubicaciones.controlador;

import com.feelingpilates.ubicaciones.dto.CatalogoItemRequest;
import com.feelingpilates.ubicaciones.dto.TipoRecursoResponse;
import com.feelingpilates.ubicaciones.entidad.TipoRecurso;
import com.feelingpilates.ubicaciones.repositorio.TipoRecursoRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tipos-recurso")
public class TipoRecursoController {

    private final TipoRecursoRepository repository;

    public TipoRecursoController(TipoRecursoRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('salon.leer')")
    public List<TipoRecursoResponse> listar() {
        return repository.findByActivoTrueOrderByNombre().stream().map(this::aResponse).toList();
    }

    @PostMapping
    @PreAuthorize("hasAuthority('salon.administrar')")
    public ResponseEntity<TipoRecursoResponse> crear(@Valid @RequestBody CatalogoItemRequest request) {
        TipoRecurso tipo = new TipoRecurso();
        tipo.setNombre(request.nombre());
        tipo.setDescripcion(request.descripcion());
        return ResponseEntity.status(HttpStatus.CREATED).body(aResponse(repository.save(tipo)));
    }

    @PatchMapping("/{id}/desactivar")
    @PreAuthorize("hasAuthority('salon.administrar')")
    public TipoRecursoResponse desactivar(@PathVariable UUID id) {
        TipoRecurso tipo = repository.findById(id).orElseThrow();
        tipo.setActivo(false);
        return aResponse(repository.save(tipo));
    }

    private TipoRecursoResponse aResponse(TipoRecurso t) {
        return new TipoRecursoResponse(t.getId(), t.getNombre(), t.getDescripcion(), t.isActivo());
    }
}
