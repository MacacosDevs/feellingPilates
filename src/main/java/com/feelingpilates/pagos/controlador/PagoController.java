package com.feelingpilates.pagos.controlador;

import com.feelingpilates.pagos.dto.CompraResponse;
import com.feelingpilates.pagos.dto.CrearPagoRequest;
import com.feelingpilates.pagos.dto.CrearPagoResponse;
import com.feelingpilates.pagos.dto.PaqueteActivoResponse;
import com.feelingpilates.pagos.dto.ReembolsoResponse;
import com.feelingpilates.pagos.servicio.PagoService;
import com.feelingpilates.seguridad.UsuarioAutenticado;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/pagos")
public class PagoController {

    private final PagoService pagoService;

    public PagoController(PagoService pagoService) {
        this.pagoService = pagoService;
    }

    @PostMapping("/paquetes/{paqueteId}/intento")
    public CrearPagoResponse crearIntento(@AuthenticationPrincipal UsuarioAutenticado usuario,
                                           @PathVariable UUID paqueteId,
                                           @RequestBody(required = false) CrearPagoRequest request) {
        String idempotencyKey = request != null ? request.idempotencyKey() : null;
        return pagoService.crearIntentoPago(usuario.id(), paqueteId, idempotencyKey);
    }

    @GetMapping("/mis-paquetes")
    public List<PaqueteActivoResponse> misPaquetesActivos(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return pagoService.obtenerPaquetesActivos(usuario.id());
    }

    @GetMapping("/mis-compras")
    public List<CompraResponse> misCompras(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return pagoService.obtenerHistorialCompras(usuario.id());
    }

    @PostMapping("/compras/{compraId}/reembolso")
    @PreAuthorize("hasAuthority('pagos.reembolsar')")
    public ReembolsoResponse reembolsar(@PathVariable UUID compraId) {
        return pagoService.reembolsarCompra(compraId);
    }

    // Stripe llama este endpoint directo (sin JWT); la autenticidad se verifica
    // con la firma Stripe-Signature contra STRIPE_WEBHOOK_SECRET, no con el filtro JWT.
    @PostMapping("/webhook")
    public ResponseEntity<Void> webhook(@RequestBody String payload,
                                         @RequestHeader("Stripe-Signature") String firmaStripe) {
        pagoService.procesarWebhook(payload, firmaStripe);
        return ResponseEntity.ok().build();
    }
}
