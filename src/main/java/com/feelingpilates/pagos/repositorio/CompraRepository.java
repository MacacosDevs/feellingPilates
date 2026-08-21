package com.feelingpilates.pagos.repositorio;

import com.feelingpilates.pagos.entidad.Compra;
import com.feelingpilates.pagos.entidad.Compra.EstadoCompra;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CompraRepository extends JpaRepository<Compra, UUID> {

    Optional<Compra> findByStripePaymentIntentId(String stripePaymentIntentId);

    List<Compra> findByUsuarioIdAndEstadoOrderByFechaExpiracionDesc(UUID usuarioId, EstadoCompra estado);

    List<Compra> findByUsuarioIdOrderByCreadoEnDesc(UUID usuarioId);

    List<Compra> findByEstado(EstadoCompra estado);

    Optional<Compra> findByIdempotencyKey(String idempotencyKey);

    List<Compra> findByMetodoPagoInOrderByCreadoEnDesc(List<Compra.MetodoPago> metodosPago);

    List<Compra> findByGrupoCompraIdOrderByNumeroItemAsc(UUID grupoCompraId);

    @Query("""
            select c from Compra c
            where c.metodoPago in :metodosPago
            and (:metodoPago is null or c.metodoPago = :metodoPago)
            and (:salonId is null or c.salon.id = :salonId)
            and (:estado is null or c.estado = :estado)
            and (:registradaPorId is null or c.registradaPor.id = :registradaPorId)
            and c.creadoEn >= :desde
            and c.creadoEn <= :hasta
            and (:busqueda is null
                 or lower(c.usuario.nombre) like lower(concat('%', cast(:busqueda as string), '%'))
                 or lower(c.registradaPor.nombre) like lower(concat('%', cast(:busqueda as string), '%')))
            """)
    Page<Compra> buscarVentas(@Param("metodosPago") List<Compra.MetodoPago> metodosPago,
                                      @Param("metodoPago") Compra.MetodoPago metodoPago,
                                      @Param("salonId") UUID salonId,
                                      @Param("estado") EstadoCompra estado,
                                      @Param("registradaPorId") UUID registradaPorId,
                                      @Param("desde") OffsetDateTime desde,
                                      @Param("hasta") OffsetDateTime hasta,
                                      @Param("busqueda") String busqueda,
                                      Pageable pageable);
}
