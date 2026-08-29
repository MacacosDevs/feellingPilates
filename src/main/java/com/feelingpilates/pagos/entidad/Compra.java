package com.feelingpilates.pagos.entidad;

import com.feelingpilates.comun.entidad.EntidadBase;
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

    // Ya no es unico: un carrito con varios paquetes genera una Compra por
    // paquete, todas comparten el mismo PaymentIntent.
    @Column(name = "stripe_payment_intent_id")
    private String stripePaymentIntentId;

    @Column(name = "fecha_expiracion")
    private OffsetDateTime fechaExpiracion;

    // La app la genera una vez por intento de compra (no por cada tap), para
    // que un reintento tras un timeout de red reutilice el PaymentIntent en
    // vez de crear uno duplicado. Tampoco es unica por el mismo motivo que
    // stripePaymentIntentId: todas las Compra de un mismo carrito comparten
    // la clave del intento de checkout que las generó.
    @Column(name = "idempotency_key")
    private String idempotencyKey;

    public enum EstadoCompra { pendiente, pagada, fallida, cancelada, reembolsada }
}
