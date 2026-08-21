package com.feelingpilates.pagos.controlador;

import com.feelingpilates.pagos.dto.ActualizarPaqueteRequest;
import com.feelingpilates.pagos.dto.CrearPaqueteRequest;
import com.feelingpilates.pagos.dto.PaqueteGestionResponse;
import com.feelingpilates.pagos.servicio.PaqueteGestionService;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/ventas/servicios")
public class PaqueteGestionController {

    private final PaqueteGestionService service;

    public PaqueteGestionController(PaqueteGestionService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('venta.servicios.vista')")
    public List<PaqueteGestionResponse> listar() {
        return service.listarTodos();
    }

    @PostMapping
    @PreAuthorize("hasAuthority('venta.servicios.gestionar.crear')")
    public ResponseEntity<PaqueteGestionResponse> crear(@Valid @RequestBody CrearPaqueteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('venta.servicios.gestionar.editar')")
    public PaqueteGestionResponse actualizar(@PathVariable UUID id, @Valid @RequestBody ActualizarPaqueteRequest request) {
        return service.actualizar(id, request);
    }

    @PatchMapping("/{id}/deshabilitar")
    @PreAuthorize("hasAuthority('venta.servicios.gestionar.deshabilitar')")
    public PaqueteGestionResponse deshabilitar(@PathVariable UUID id) {
        return service.deshabilitar(id);
    }

    @PatchMapping("/{id}/habilitar")
    @PreAuthorize("hasAuthority('venta.servicios.gestionar.deshabilitar')")
    public PaqueteGestionResponse habilitar(@PathVariable UUID id) {
        return service.habilitar(id);
    }
}
