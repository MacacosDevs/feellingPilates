package com.feelingpilates.ubicaciones.controlador;

import com.feelingpilates.exception.ValidacionException;
import com.feelingpilates.ubicaciones.dto.ActividadRecursoRequest;
import com.feelingpilates.ubicaciones.dto.ActividadRecursoResponse;
import com.feelingpilates.ubicaciones.entidad.ActividadRecurso;
import com.feelingpilates.ubicaciones.entidad.TipoActividad;
import com.feelingpilates.ubicaciones.entidad.TipoRecurso;
import com.feelingpilates.ubicaciones.repositorio.ActividadRecursoRepository;
import com.feelingpilates.ubicaciones.repositorio.TipoActividadRepository;
import com.feelingpilates.ubicaciones.repositorio.TipoRecursoRepository;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tipos-actividad/{tipoActividadId}/recursos")
public class ActividadRecursoController {

    private final ActividadRecursoRepository actividadRecursoRepository;
    private final TipoActividadRepository tipoActividadRepository;
    private final TipoRecursoRepository tipoRecursoRepository;

    public ActividadRecursoController(
            ActividadRecursoRepository actividadRecursoRepository,
            TipoActividadRepository tipoActividadRepository,
            TipoRecursoRepository tipoRecursoRepository) {
        this.actividadRecursoRepository = actividadRecursoRepository;
        this.tipoActividadRepository = tipoActividadRepository;
        this.tipoRecursoRepository = tipoRecursoRepository;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('actividades.leer')")
    public List<ActividadRecursoResponse> listar(@PathVariable UUID tipoActividadId) {
        tipoActividadRepository.findById(tipoActividadId).orElseThrow();
        return actividadRecursoRepository.findByTipoActividadId(tipoActividadId).stream()
                .map(this::aResponse)
                .toList();
    }

    @PutMapping
    @PreAuthorize("hasAuthority('actividades.gestionar')")
    @Transactional
    public List<ActividadRecursoResponse> reemplazar(
            @PathVariable UUID tipoActividadId,
            @Valid @RequestBody List<ActividadRecursoRequest> request) {
        TipoActividad tipoActividad = tipoActividadRepository.findById(tipoActividadId).orElseThrow();

        actividadRecursoRepository.deleteByTipoActividadId(tipoActividadId);
        actividadRecursoRepository.flush();

        for (ActividadRecursoRequest r : request) {
            TipoRecurso tipoRecurso = tipoRecursoRepository.findById(r.tipoRecursoId())
                    .orElseThrow(() -> new ValidacionException("Tipo de recurso inválido"));
            actividadRecursoRepository.save(
                    new ActividadRecurso(tipoActividad, tipoRecurso, r.cantidad()));
        }

        return actividadRecursoRepository.findByTipoActividadId(tipoActividadId).stream()
                .map(this::aResponse)
                .toList();
    }

    private ActividadRecursoResponse aResponse(ActividadRecurso ar) {
        return new ActividadRecursoResponse(
                ar.getTipoRecurso().getId(),
                ar.getTipoRecurso().getNombre(),
                ar.getCantidad());
    }
}
