package com.feelingpilates.pagos.controlador;

import com.feelingpilates.pagos.dto.PaqueteResponse;
import com.feelingpilates.pagos.entidad.Paquete;
import com.feelingpilates.pagos.repositorio.PaqueteRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/publico/paquetes")
public class PaqueteController {

    private final PaqueteRepository paqueteRepository;

    public PaqueteController(PaqueteRepository paqueteRepository) {
        this.paqueteRepository = paqueteRepository;
    }

    @GetMapping
    public List<PaqueteResponse> listar() {
        return paqueteRepository.findByActivoTrueOrderByCategoriaAscOrdenAsc().stream()
                .map(this::aResponse)
                .toList();
    }

    private PaqueteResponse aResponse(Paquete p) {
        return new PaqueteResponse(
                p.getId(),
                p.getCategoria().name(),
                p.getNombre(),
                p.getDescripcion(),
                p.getPrecioCentavos(),
                p.getVigenciaDias(),
                p.getUnitarioTexto(),
                p.isDestacado());
    }
}
