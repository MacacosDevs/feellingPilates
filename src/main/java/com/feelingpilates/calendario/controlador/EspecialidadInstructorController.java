package com.feelingpilates.calendario.controlador;

import com.feelingpilates.calendario.dto.EspecialidadResponse;
import com.feelingpilates.calendario.dto.EspecialidadesRequest;
import com.feelingpilates.calendario.servicio.EspecialidadInstructorService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/usuarios/{usuarioId}/especialidades")
public class EspecialidadInstructorController {

    private final EspecialidadInstructorService especialidadInstructorService;

    public EspecialidadInstructorController(EspecialidadInstructorService especialidadInstructorService) {
        this.especialidadInstructorService = especialidadInstructorService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('calendario.leer')")
    public List<EspecialidadResponse> listar(@PathVariable UUID usuarioId) {
        return especialidadInstructorService.listar(usuarioId);
    }

    @PutMapping
    @PreAuthorize("hasAuthority('calendario.administrar')")
    public List<EspecialidadResponse> actualizar(@PathVariable UUID usuarioId, @RequestBody EspecialidadesRequest request) {
        return especialidadInstructorService.actualizar(usuarioId, request.tipoActividadIds());
    }
}
