package com.feelingpilates.pagos.servicio;

import com.feelingpilates.exception.ResourceNotFoundException;
import com.feelingpilates.exception.ValidacionException;
import com.feelingpilates.pagos.dto.CompraResponse;
import com.feelingpilates.pagos.dto.CrearPagoResponse;
import com.feelingpilates.pagos.dto.PaqueteActivoResponse;
import com.feelingpilates.pagos.dto.ReembolsoResponse;
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
import com.stripe.model.Charge;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import com.stripe.model.StripeObject;
import com.stripe.net.ApiResource;
import com.stripe.net.RequestOptions;
import com.stripe.net.Webhook;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.RefundCreateParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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
    public CrearPagoResponse crearIntentoPago(UUID usuarioId, List<UUID> paqueteIds, String idempotencyKey) {
        if (paqueteIds == null || paqueteIds.isEmpty()) {
            throw new ValidacionException("Debes seleccionar al menos un paquete");
        }
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            Optional<CrearPagoResponse> existente = reusarSiExiste(idempotencyKey);
            if (existente.isPresent()) {
                return existente.get();
            }
        }

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        // Un carrito genera una Compra por paquete elegido; todas comparten
        // idempotencyKey y, más abajo, el mismo PaymentIntent.
        List<Compra> compras = new ArrayList<>();
        for (UUID paqueteId : paqueteIds) {
            Paquete paquete = paqueteRepository.findById(paqueteId)
                    .filter(Paquete::isActivo)
                    .orElseThrow(() -> new ResourceNotFoundException("Paquete no encontrado"));
            Compra compra = new Compra();
            compra.setUsuario(usuario);
            compra.setPaquete(paquete);
            compra.setMontoCentavos(paquete.getPrecioCentavos());
            compra.setIdempotencyKey(idempotencyKey);
            compras.add(compraRepository.save(compra));
        }

        long totalCentavos = compras.stream().mapToLong(Compra::getMontoCentavos).sum();
        String moneda = compras.get(0).getMoneda();

        try {
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(totalCentavos)
                    .setCurrency(moneda)
                    .putMetadata("compraIds", compras.stream()
                            .map(c -> c.getId().toString())
                            .collect(Collectors.joining(",")))
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
            // Doble seguro: si por alguna razón nuestra propia comprobación de
            // arriba no atrapó el reintento (dos requests casi simultáneos), la
            // idempotencia de Stripe evita que se genere un segundo PaymentIntent
            // real con la misma clave dentro de sus 24 horas de vigencia.
            RequestOptions requestOptions = idempotencyKey == null || idempotencyKey.isBlank()
                    ? RequestOptions.getDefault()
                    : RequestOptions.builder().setIdempotencyKey(idempotencyKey).build();
            PaymentIntent intent = PaymentIntent.create(params, requestOptions);
            compras.forEach(c -> c.setStripePaymentIntentId(intent.getId()));
            compraRepository.saveAll(compras);
            List<UUID> compraIds = compras.stream().map(Compra::getId).toList();
            return new CrearPagoResponse(compraIds, intent.getClientSecret(), publishableKey);
        } catch (StripeException e) {
            log.error("Stripe rechazó la creación del PaymentIntent para compras {}: {}",
                    compras.stream().map(c -> c.getId().toString()).collect(Collectors.joining(",")), e.toString());
            throw new ValidacionException("No se pudo iniciar el pago: " + e.getMessage());
        }
    }

    // Si ya existe un grupo de Compra con esta clave (reintento tras timeout,
    // doble tap), se reutiliza su PaymentIntent en vez de crear uno nuevo.
    private Optional<CrearPagoResponse> reusarSiExiste(String idempotencyKey) {
        List<Compra> existentes = compraRepository.findAllByIdempotencyKey(idempotencyKey);
        if (existentes.isEmpty()) {
            return Optional.empty();
        }
        try {
            PaymentIntent intent = PaymentIntent.retrieve(existentes.get(0).getStripePaymentIntentId());
            List<UUID> compraIds = existentes.stream().map(Compra::getId).toList();
            return Optional.of(new CrearPagoResponse(compraIds, intent.getClientSecret(), publishableKey));
        } catch (StripeException e) {
            log.error("No se pudo recuperar el PaymentIntent existente para la clave {}: {}",
                    idempotencyKey, e.toString());
            throw new ValidacionException("No se pudo continuar el pago: " + e.getMessage());
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

        switch (event.getType()) {
            case "payment_intent.succeeded" ->
                    deserializarObjeto(event, PaymentIntent.class).ifPresent(this::marcarComoPagada);
            case "payment_intent.payment_failed" ->
                    deserializarObjeto(event, PaymentIntent.class).ifPresent(this::marcarComoFallida);
            // Cubre un reembolso hecho fuera de nuestro endpoint (ej. directo desde
            // el Dashboard de Stripe por el personal), no solo el que iniciamos nosotros.
            case "charge.refunded" ->
                    deserializarObjeto(event, Charge.class).ifPresent(this::marcarComoReembolsada);
            default -> log.debug("Webhook de Stripe con tipo {} ignorado", event.getType());
        }
    }

    // El deserializador tipado falla en silencio (getObject() vacío) cuando la
    // versión de API de la cuenta de Stripe no coincide con la que espera esta
    // versión de stripe-java; en ese caso se parsea el JSON crudo del evento.
    private <T extends StripeObject> Optional<T> deserializarObjeto(Event event, Class<T> tipo) {
        EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();
        T objeto = deserializer.getObject()
                .filter(tipo::isInstance)
                .map(tipo::cast)
                .orElseGet(() -> ApiResource.GSON.fromJson(deserializer.getRawJson(), tipo));
        return Optional.ofNullable(objeto);
    }

    private void marcarComoPagada(PaymentIntent intent) {
        List<Compra> compras = compraRepository.findAllByStripePaymentIntentId(intent.getId());
        if (compras.isEmpty()) {
            log.warn("payment_intent.succeeded para {} sin Compra asociada", intent.getId());
            return;
        }
        compras.forEach(this::aplicarPagada);
    }

    private void marcarComoFallida(PaymentIntent intent) {
        compraRepository.findAllByStripePaymentIntentId(intent.getId()).forEach(this::aplicarFallida);
    }

    private void aplicarPagada(Compra compra) {
        if (compra.getEstado() == EstadoCompra.pagada) {
            return;
        }
        compra.setEstado(EstadoCompra.pagada);
        compra.setFechaExpiracion(OffsetDateTime.now().plusDays(compra.getPaquete().getVigenciaDias()));
        compraRepository.save(compra);
    }

    private void aplicarFallida(Compra compra) {
        compra.setEstado(EstadoCompra.fallida);
        compraRepository.save(compra);
    }

    private void aplicarCancelada(Compra compra) {
        compra.setEstado(EstadoCompra.cancelada);
        compraRepository.save(compra);
    }

    private void marcarComoReembolsada(Charge charge) {
        String paymentIntentId = charge.getPaymentIntent();
        if (paymentIntentId == null) {
            return;
        }
        // Coherente con reembolsarCompra: un reembolso siempre es de carrito
        // completo, así que se marcan todas las Compra que comparten el
        // PaymentIntent, no solo una.
        compraRepository.findAllByStripePaymentIntentId(paymentIntentId).forEach(compra -> {
            if (compra.getEstado() == EstadoCompra.reembolsada) {
                return;
            }
            compra.setEstado(EstadoCompra.reembolsada);
            compraRepository.save(compra);
        });
    }

    // Solo se puede reembolsar una compra que de verdad se cobró; el permiso
    // 'pagos.reembolsar' (solo ADMIN) evita que un cliente se autoreembolse.
    //
    // El reembolso es siempre de carrito completo: como varias Compra pueden
    // compartir un mismo PaymentIntent (una por paquete elegido en el
    // carrito), reembolsar solo la línea pedida dejaría cobrado el resto del
    // carrito en Stripe pero sin forma confiable de correlacionar, desde el
    // webhook charge.refunded, qué línea corresponde a qué reembolso parcial.
    // Se prefiere reembolsar y marcar todo el grupo junto.
    @Transactional
    public ReembolsoResponse reembolsarCompra(UUID compraId) {
        Compra compra = compraRepository.findById(compraId)
                .orElseThrow(() -> new ResourceNotFoundException("Compra no encontrada"));
        if (compra.getEstado() != EstadoCompra.pagada) {
            throw new ValidacionException("Solo se puede reembolsar una compra pagada");
        }

        List<Compra> grupo = compraRepository.findAllByStripePaymentIntentId(compra.getStripePaymentIntentId());
        boolean todasPagadas = grupo.stream().allMatch(c -> c.getEstado() == EstadoCompra.pagada);
        if (!todasPagadas) {
            throw new ValidacionException("El carrito tiene compras en un estado inconsistente para reembolsar");
        }

        try {
            Refund refund = Refund.create(RefundCreateParams.builder()
                    .setPaymentIntent(compra.getStripePaymentIntentId())
                    .build());
            grupo.forEach(c -> c.setEstado(EstadoCompra.reembolsada));
            compraRepository.saveAll(grupo);
            return new ReembolsoResponse(compra.getId(), EstadoCompra.reembolsada.name(), refund.getAmount().intValue());
        } catch (StripeException e) {
            log.error("Stripe rechazó el reembolso de la compra {}: {}", compra.getId(), e.toString());
            throw new ValidacionException("No se pudo procesar el reembolso: " + e.getMessage());
        }
    }

    // Red de seguridad para cuando el webhook nunca llega (se cae la conexión,
    // Stripe no logra entregarlo, etc.): en vez de confiar ciegamente en que el
    // webhook avisó, cada compra "pendiente" se verifica directo contra la API
    // de Stripe. Solo si Stripe también dice que sigue sin resolverse y ya pasó
    // el tiempo límite, se marca como abandonada (cancelada).
    @Scheduled(fixedRate = 5, timeUnit = TimeUnit.MINUTES)
    @Transactional
    public void reconciliarComprasPendientes() {
        OffsetDateTime limiteAbandono = OffsetDateTime.now().minusMinutes(compraPendienteExpiraMinutos);
        List<Compra> pendientes = compraRepository.findByEstado(EstadoCompra.pendiente);
        for (Compra compra : pendientes) {
            reconciliarUnaCompra(compra, limiteAbandono);
        }
    }

    private void reconciliarUnaCompra(Compra compra, OffsetDateTime limiteAbandono) {
        if (compra.getStripePaymentIntentId() == null) {
            return;
        }
        PaymentIntent intent;
        try {
            intent = PaymentIntent.retrieve(compra.getStripePaymentIntentId());
        } catch (StripeException e) {
            log.warn("No se pudo reconciliar la compra {} contra Stripe: {}", compra.getId(), e.toString());
            return;
        }

        switch (intent.getStatus()) {
            case "succeeded" -> aplicarPagada(compra);
            case "canceled" -> aplicarCancelada(compra);
            default -> {
                if (compra.getCreadoEn().isBefore(limiteAbandono)) {
                    log.info("Compra {} sigue sin resolverse en Stripe (status={}) después de {} min, se cancela",
                            compra.getId(), intent.getStatus(), compraPendienteExpiraMinutos);
                    aplicarCancelada(compra);
                }
            }
        }
    }
}
