package com.feelingpilates.ubicaciones.controlador;

import com.feelingpilates.ubicaciones.dto.GuardarExcepcionSalonRequest;
import com.feelingpilates.ubicaciones.dto.SalonHorarioExcepcionResponse;
import com.feelingpilates.ubicaciones.servicio.SalonHorarioExcepcionService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/salones/{salonId}/excepciones-horario")
public class SalonHorarioExcepcionController {

    private final SalonHorarioExcepcionService excepcionService;

    public SalonHorarioExcepcionController(SalonHorarioExcepcionService excepcionService) {
        this.excepcionService = excepcionService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('salon.leer')")
    public List<SalonHorarioExcepcionResponse> listar(
            @PathVariable UUID salonId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return excepcionService.listarPorRango(salonId, desde, hasta);
    }

    @PutMapping
    @PreAuthorize("hasAuthority('salon.administrar')")
    public SalonHorarioExcepcionResponse guardar(
            @PathVariable UUID salonId, @Valid @RequestBody GuardarExcepcionSalonRequest request) {
        return excepcionService.guardar(salonId, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('salon.administrar')")
    public void eliminar(@PathVariable UUID salonId, @PathVariable UUID id) {
        excepcionService.eliminar(id);
    }
}
