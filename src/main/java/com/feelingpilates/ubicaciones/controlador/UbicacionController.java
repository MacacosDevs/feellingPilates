package com.feelingpilates.ubicaciones.controlador;

import com.feelingpilates.ubicaciones.dto.EstadoResponse;
import com.feelingpilates.ubicaciones.dto.MunicipioResponse;
import com.feelingpilates.ubicaciones.repositorio.EstadoRepository;
import com.feelingpilates.ubicaciones.repositorio.MunicipioRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/ubicaciones")
public class UbicacionController {

    private final EstadoRepository estadoRepository;
    private final MunicipioRepository municipioRepository;

    public UbicacionController(EstadoRepository estadoRepository, MunicipioRepository municipioRepository) {
        this.estadoRepository = estadoRepository;
        this.municipioRepository = municipioRepository;
    }

    @GetMapping("/estados")
    public List<EstadoResponse> listarEstados() {
        return estadoRepository.findAllByOrderByNombre().stream()
                .map(e -> new EstadoResponse(e.getId(), e.getNombre()))
                .toList();
    }

    @GetMapping("/estados/{estadoId}/municipios")
    public List<MunicipioResponse> listarMunicipios(@PathVariable short estadoId) {
        return municipioRepository.findByIdEstadoIdOrderByNombre(estadoId).stream()
                .map(m -> new MunicipioResponse(m.getId().getId(), m.getId().getEstadoId(), m.getNombre()))
                .toList();
    }
}
