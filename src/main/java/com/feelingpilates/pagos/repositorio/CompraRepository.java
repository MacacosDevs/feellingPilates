package com.feelingpilates.pagos.repositorio;

import com.feelingpilates.pagos.entidad.Compra;
import com.feelingpilates.pagos.entidad.Compra.EstadoCompra;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CompraRepository extends JpaRepository<Compra, UUID> {

    // Varias filas de un mismo carrito comparten stripePaymentIntentId /
    // idempotencyKey (una Compra por paquete elegido), por eso ambas
    // devuelven List en vez de Optional.
    List<Compra> findAllByStripePaymentIntentId(String stripePaymentIntentId);

    List<Compra> findByUsuarioIdAndEstadoOrderByFechaExpiracionDesc(UUID usuarioId, EstadoCompra estado);

    List<Compra> findByUsuarioIdOrderByCreadoEnDesc(UUID usuarioId);

    List<Compra> findByEstado(EstadoCompra estado);

    List<Compra> findAllByIdempotencyKey(String idempotencyKey);
}
