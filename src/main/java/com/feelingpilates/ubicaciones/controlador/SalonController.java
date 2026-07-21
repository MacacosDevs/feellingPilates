package com.feelingpilates.ubicaciones.controlador;

import com.feelingpilates.ubicaciones.dto.SalonResponse;
import com.feelingpilates.ubicaciones.repositorio.SalonRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/salones")
public class SalonController {

    private final SalonRepository salonRepository;

    public SalonController(SalonRepository salonRepository) {
        this.salonRepository = salonRepository;
    }

    @GetMapping
    public List<SalonResponse> listar() {
        return salonRepository.listarActivos().stream()
                .map(p -> new SalonResponse(
                        p.getId(), p.getNombre(), p.getDireccion(),
                        p.getEstadoId(), p.getEstadoNombre(),
                        p.getMunicipioId(), p.getMunicipioNombre()))
                .toList();
    }
}
