package com.feelingpilates.ubicaciones.controlador;

import com.feelingpilates.ubicaciones.dto.CatalogoItemRequest;
import com.feelingpilates.ubicaciones.dto.TipoMaquinaResponse;
import com.feelingpilates.ubicaciones.entidad.TipoMaquina;
import com.feelingpilates.ubicaciones.repositorio.TipoMaquinaRepository;
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
@RequestMapping("/api/tipos-maquina")
public class TipoMaquinaController {

    private final TipoMaquinaRepository repository;

    public TipoMaquinaController(TipoMaquinaRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('salon.leer')")
    public List<TipoMaquinaResponse> listar() {
        return repository.findByActivoTrueOrderByNombre().stream().map(this::aResponse).toList();
    }

    @PostMapping
    @PreAuthorize("hasAuthority('salon.administrar')")
    public ResponseEntity<TipoMaquinaResponse> crear(@Valid @RequestBody CatalogoItemRequest request) {
        TipoMaquina tipo = new TipoMaquina();
        tipo.setNombre(request.nombre());
        tipo.setDescripcion(request.descripcion());
        return ResponseEntity.status(HttpStatus.CREATED).body(aResponse(repository.save(tipo)));
    }

    @PatchMapping("/{id}/desactivar")
    @PreAuthorize("hasAuthority('salon.administrar')")
    public TipoMaquinaResponse desactivar(@PathVariable UUID id) {
        TipoMaquina tipo = repository.findById(id).orElseThrow();
        tipo.setActivo(false);
        return aResponse(repository.save(tipo));
    }

    private TipoMaquinaResponse aResponse(TipoMaquina t) {
        return new TipoMaquinaResponse(t.getId(), t.getNombre(), t.getDescripcion(), t.isActivo());
    }
}
