package com.feelingpilates.pagos.controlador;

import com.feelingpilates.pagos.dto.CambiarEstadoVentaRequest;
import com.feelingpilates.pagos.dto.RegistrarVentaCarritoRequest;
import com.feelingpilates.pagos.dto.RegistrarVentaRequest;
import com.feelingpilates.pagos.dto.SedeVentaResponse;
import com.feelingpilates.pagos.dto.VentaCarritoResponse;
import com.feelingpilates.pagos.dto.VentaResponse;
import com.feelingpilates.pagos.servicio.VentaService;
import com.feelingpilates.seguridad.UsuarioAutenticado;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/ventas")
public class VentaController {

    private final VentaService ventaService;

    public VentaController(VentaService ventaService) {
        this.ventaService = ventaService;
    }

    // Lista de referencia usada tanto por Nueva venta como por el filtro de
    // Sede de Gestion de ventas; cualquiera de las dos vistas basta para consultarla.
    @GetMapping("/sedes")
    @PreAuthorize("hasAnyAuthority('venta.registrar.vista', 'venta.gestion.vista')")
    public List<SedeVentaResponse> sedesDisponibles(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return ventaService.sedesDisponibles(usuario.id());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('venta.registrar.crear')")
    public ResponseEntity<VentaResponse> vender(@AuthenticationPrincipal UsuarioAutenticado usuario,
                                                      @Valid @RequestBody RegistrarVentaRequest request) {
        VentaResponse venta = ventaService.registrarVenta(
                request.clienteId(), request.paqueteId(), request.salonId(), request.metodoPago(), usuario.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(venta);
    }

    @PostMapping("/carrito")
    @PreAuthorize("hasAuthority('venta.registrar.crear')")
    public ResponseEntity<VentaCarritoResponse> venderCarrito(@AuthenticationPrincipal UsuarioAutenticado usuario,
                                                                    @Valid @RequestBody RegistrarVentaCarritoRequest request) {
        VentaCarritoResponse venta = ventaService.registrarVentaCarrito(
                request.clienteId(), request.salonId(), request.metodoPago(), request.items(), usuario.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(venta);
    }

    @PatchMapping("/{id}/reembolsar")
    @PreAuthorize("hasAuthority('venta.gestion.gestionar')")
    public VentaResponse reembolsarVenta(@PathVariable UUID id, @Valid @RequestBody CambiarEstadoVentaRequest request) {
        return ventaService.reembolsarVenta(id, request.motivo());
    }

    // Sin filtros ni paginacion: no hay forma de acotarlo a "solo lo propio", asi
    // que exige poder ver la gestion de ventas completa. El frontend usa /ventas/buscar.
    @GetMapping
    @PreAuthorize("hasAuthority('venta.gestion.ver.todos')")
    public List<VentaResponse> historial() {
        return ventaService.historial();
    }

    @GetMapping("/buscar")
    @PreAuthorize("hasAnyAuthority('venta.gestion.ver.propio', 'venta.gestion.ver.todos')")
    public Page<VentaResponse> historialFiltrado(
            @AuthenticationPrincipal UsuarioAutenticado usuario,
            Authentication authentication,
            @RequestParam(required = false) String metodoPago,
            @RequestParam(required = false) UUID salonId,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) String busqueda,
            Pageable pageable) {
        boolean veTodos = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("venta.gestion.ver.todos"));
        UUID registradaPorId = veTodos ? null : usuario.id();
        return ventaService.historial(metodoPago, salonId, estado, registradaPorId, desde, hasta, busqueda, pageable);
    }
}
