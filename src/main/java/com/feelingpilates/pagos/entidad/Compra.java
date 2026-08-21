package com.feelingpilates.pagos.entidad;

import com.feelingpilates.comun.entidad.EntidadBase;
import com.feelingpilates.ubicaciones.entidad.Salon;
import com.feelingpilates.usuarios.entidad.Usuario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "compra")
@Getter
@Setter
@NoArgsConstructor
public class Compra extends EntidadBase {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "paquete_id")
    private Paquete paquete;

    @Column(name = "monto_centavos", nullable = false)
    private int montoCentavos;

    @Column(nullable = false)
    private String moneda = "mxn";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoCompra estado = EstadoCompra.pendiente;

    @Column(name = "stripe_payment_intent_id", unique = true)
    private String stripePaymentIntentId;

    @Column(name = "fecha_expiracion")
    private OffsetDateTime fechaExpiracion;

    // La app la genera una vez por intento de compra (no por cada tap), para
    // que un reintento tras un timeout de red reutilice el PaymentIntent en
    // vez de crear uno duplicado.
    @Column(name = "idempotency_key", unique = true)
    private String idempotencyKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "metodo_pago", nullable = false)
    private MetodoPago metodoPago = MetodoPago.stripe;

    // Quien la cobro en caja (recepcion/admin); null para compras hechas por el
    // propio cliente desde la app con Stripe.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "registrada_por_id")
    private Usuario registradaPor;

    // Sede donde se hizo la venta (siempre presente en ventas de caja; null en
    // compras por Stripe hechas por el cliente desde la app).
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "salon_id")
    private Salon salon;

    // Comparten el mismo valor todas las Compra creadas en un mismo checkout
    // de caja (un ticket con uno o varios paquetes); null para compras viejas
    // o hechas por Stripe fuera de este flujo.
    @Column(name = "grupo_compra_id")
    private UUID grupoCompraId;

    // Posicion (1-based) de esta linea dentro de su grupoCompraId.
    @Column(name = "numero_item")
    private Integer numeroItem;

    // Motivo capturado al cancelar o reembolsar una venta de caja.
    @Column(name = "motivo_estado")
    private String motivoEstado;

    public enum EstadoCompra { pendiente, pagada, fallida, cancelada, reembolsada }

    public enum MetodoPago { efectivo, transferencia, stripe }
}
