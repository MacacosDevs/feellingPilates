package com.feelingpilates.pagos.repositorio;

import com.feelingpilates.pagos.entidad.Compra;
import com.feelingpilates.pagos.entidad.Compra.EstadoCompra;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CompraRepository extends JpaRepository<Compra, UUID> {

    Optional<Compra> findByStripePaymentIntentId(String stripePaymentIntentId);

    List<Compra> findByUsuarioIdAndEstadoOrderByFechaExpiracionDesc(UUID usuarioId, EstadoCompra estado);

    List<Compra> findByUsuarioIdOrderByCreadoEnDesc(UUID usuarioId);

    List<Compra> findByEstado(EstadoCompra estado);

    Optional<Compra> findByIdempotencyKey(String idempotencyKey);
}
