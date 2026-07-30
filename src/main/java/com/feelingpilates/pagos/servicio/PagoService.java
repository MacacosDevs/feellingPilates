package com.feelingpilates.pagos.servicio;

import com.feelingpilates.exception.ResourceNotFoundException;
import com.feelingpilates.exception.ValidacionException;
import com.feelingpilates.pagos.dto.CompraResponse;
import com.feelingpilates.pagos.dto.CrearPagoResponse;
import com.feelingpilates.pagos.dto.PaqueteActivoResponse;
import com.feelingpilates.pagos.entidad.Compra;
import com.feelingpilates.pagos.entidad.Compra.EstadoCompra;
import com.feelingpilates.pagos.entidad.Paquete;
import com.feelingpilates.pagos.entidad.Paquete.CategoriaPaquete;
import com.feelingpilates.pagos.repositorio.CompraRepository;
import com.feelingpilates.pagos.repositorio.PaqueteRepository;
import com.feelingpilates.usuarios.entidad.Usuario;
import com.feelingpilates.usuarios.repositorio.UsuarioRepository;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.PaymentIntent;
import com.stripe.net.ApiResource;
import com.stripe.net.Webhook;
import com.stripe.param.PaymentIntentCreateParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class PagoService {

    private static final Logger log = LoggerFactory.getLogger(PagoService.class);

    private final PaqueteRepository paqueteRepository;
    private final CompraRepository compraRepository;
    private final UsuarioRepository usuarioRepository;
    private final String publishableKey;
    private final String webhookSecret;
    private final long compraPendienteExpiraMinutos;

    public PagoService(PaqueteRepository paqueteRepository,
                        CompraRepository compraRepository,
                        UsuarioRepository usuarioRepository,
                        @Value("${app.stripe.publishable-key}") String publishableKey,
                        @Value("${app.stripe.webhook-secret}") String webhookSecret,
                        @Value("${app.pagos.compra-pendiente-expira-minutos}") long compraPendienteExpiraMinutos) {
        this.paqueteRepository = paqueteRepository;
        this.compraRepository = compraRepository;
        this.usuarioRepository = usuarioRepository;
        this.publishableKey = publishableKey;
        this.webhookSecret = webhookSecret;
        this.compraPendienteExpiraMinutos = compraPendienteExpiraMinutos;
    }

    @Transactional
    public CrearPagoResponse crearIntentoPago(UUID usuarioId, UUID paqueteId) {
        Paquete paquete = paqueteRepository.findById(paqueteId)
                .filter(Paquete::isActivo)
                .orElseThrow(() -> new ResourceNotFoundException("Paquete no encontrado"));
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        Compra compra = new Compra();
        compra.setUsuario(usuario);
        compra.setPaquete(paquete);
        compra.setMontoCentavos(paquete.getPrecioCentavos());
        compra = compraRepository.save(compra);

        try {
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount((long) paquete.getPrecioCentavos())
                    .setCurrency(compra.getMoneda())
                    .putMetadata("compraId", compra.getId().toString())
                    .setAutomaticPaymentMethods(
                            PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                    .setEnabled(true)
                                    // Sin return_url no podemos completar métodos que redirigen
                                    // fuera de la app; como el PaymentSheet aquí solo se usa para
                                    // tarjeta, se desactivan en vez de manejar ese redirect.
                                    .setAllowRedirects(
                                            PaymentIntentCreateParams.AutomaticPaymentMethods.AllowRedirects.NEVER)
                                    .build())
                    .build();
            PaymentIntent intent = PaymentIntent.create(params);
            compra.setStripePaymentIntentId(intent.getId());
            compraRepository.save(compra);
            return new CrearPagoResponse(compra.getId(), intent.getClientSecret(), publishableKey);
        } catch (StripeException e) {
            log.error("Stripe rechazó la creación del PaymentIntent para compra {}: {}", compra.getId(), e.toString());
            throw new ValidacionException("No se pudo iniciar el pago: " + e.getMessage());
        }
    }

    // No existe todavía un sistema de reservas que descuente clases usadas de una
    // compra (ver comentario en AccountScreen.tsx), así que "paquete activo" es
    // solo el más reciente vigente (pagado y sin expirar) por categoría; un combo
    // cuenta como activo tanto para pilates como para bacu_fit.
    public List<PaqueteActivoResponse> obtenerPaquetesActivos(UUID usuarioId) {
        OffsetDateTime ahora = OffsetDateTime.now();
        List<Compra> vigentes = compraRepository
                .findByUsuarioIdAndEstadoOrderByFechaExpiracionDesc(usuarioId, EstadoCompra.pagada)
                .stream()
                .filter(c -> c.getFechaExpiracion() != null && c.getFechaExpiracion().isAfter(ahora))
                .toList();

        List<PaqueteActivoResponse> resultado = new ArrayList<>();
        for (CategoriaPaquete categoria : List.of(CategoriaPaquete.pilates, CategoriaPaquete.bacu_fit)) {
            vigentes.stream()
                    .filter(c -> c.getPaquete().getCategoria() == categoria
                            || c.getPaquete().getCategoria() == CategoriaPaquete.combo)
                    .findFirst()
                    .ifPresent(c -> resultado.add(aPaqueteActivoResponse(categoria, c)));
        }
        return resultado;
    }

    public List<CompraResponse> obtenerHistorialCompras(UUID usuarioId) {
        return compraRepository.findByUsuarioIdOrderByCreadoEnDesc(usuarioId).stream()
                .map(this::aCompraResponse)
                .toList();
    }

    private CompraResponse aCompraResponse(Compra compra) {
        return new CompraResponse(
                compra.getId(),
                compra.getPaquete().getNombre(),
                compra.getPaquete().getCategoria().name(),
                compra.getMontoCentavos(),
                compra.getEstado().name(),
                compra.getCreadoEn(),
                compra.getFechaExpiracion());
    }

    private PaqueteActivoResponse aPaqueteActivoResponse(CategoriaPaquete categoria, Compra compra) {
        OffsetDateTime fechaInicio = compra.getFechaExpiracion().minusDays(compra.getPaquete().getVigenciaDias());
        return new PaqueteActivoResponse(categoria.name(), compra.getPaquete().getNombre(), fechaInicio,
                compra.getFechaExpiracion());
    }

    @Transactional
    public void procesarWebhook(String payload, String firmaStripe) {
        Event event;
        try {
            event = Webhook.constructEvent(payload, firmaStripe, webhookSecret);
        } catch (SignatureVerificationException e) {
            throw new ValidacionException("Firma de webhook de Stripe inválida");
        }

        if (!event.getType().startsWith("payment_intent.")) {
            log.debug("Webhook de Stripe con tipo {} ignorado", event.getType());
            return;
        }

        // El deserializador tipado falla en silencio (getObject() vacío) cuando la
        // versión de API de la cuenta de Stripe no coincide con la que espera esta
        // versión de stripe-java; en ese caso se parsea el JSON crudo del evento.
        EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();
        PaymentIntent intent = deserializer.getObject()
                .filter(PaymentIntent.class::isInstance)
                .map(PaymentIntent.class::cast)
                .orElseGet(() -> ApiResource.GSON.fromJson(deserializer.getRawJson(), PaymentIntent.class));

        switch (event.getType()) {
            case "payment_intent.succeeded" -> marcarComoPagada(intent);
            case "payment_intent.payment_failed" -> marcarComoFallida(intent);
            default -> log.debug("Webhook de Stripe con tipo {} ignorado", event.getType());
        }
    }

    private void marcarComoPagada(PaymentIntent intent) {
        compraRepository.findByStripePaymentIntentId(intent.getId()).ifPresentOrElse(compra -> {
            if (compra.getEstado() == EstadoCompra.pagada) {
                return;
            }
            compra.setEstado(EstadoCompra.pagada);
            compra.setFechaExpiracion(OffsetDateTime.now().plusDays(compra.getPaquete().getVigenciaDias()));
            compraRepository.save(compra);
        }, () -> log.warn("payment_intent.succeeded para {} sin Compra asociada", intent.getId()));
    }

    private void marcarComoFallida(PaymentIntent intent) {
        compraRepository.findByStripePaymentIntentId(intent.getId())
                .ifPresent(compra -> {
                    compra.setEstado(EstadoCompra.fallida);
                    compraRepository.save(compra);
                });
    }

    // El cliente abrió la hoja de pago pero nunca la confirmó (canceló, se le
    // fue la conexión, cerró la app a medio proceso): sin esto la compra se
    // queda en "pendiente" para siempre en su historial.
    @Scheduled(fixedRate = 15, timeUnit = TimeUnit.MINUTES)
    @Transactional
    public void cancelarComprasPendientesExpiradas() {
        OffsetDateTime limite = OffsetDateTime.now().minusMinutes(compraPendienteExpiraMinutos);
        List<Compra> expiradas = compraRepository.findByEstadoAndCreadoEnBefore(EstadoCompra.pendiente, limite);
        if (expiradas.isEmpty()) {
            return;
        }
        expiradas.forEach(compra -> compra.setEstado(EstadoCompra.cancelada));
        compraRepository.saveAll(expiradas);
        log.info("Canceladas {} compras pendientes con más de {} minutos de antigüedad",
                expiradas.size(), compraPendienteExpiraMinutos);
    }
}
