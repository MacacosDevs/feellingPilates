package com.feelingpilates.clases.repositorio;

import com.feelingpilates.clases.entidad.ClaseReserva;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ClaseReservaRepository extends JpaRepository<ClaseReserva, UUID> {

    // CONFIRMADA y ASISTIO cuentan como cupo ocupado, solo CANCELADA lo libera.
    long countByClaseIdAndEstadoNot(UUID claseId, ClaseReserva.Estado estadoExcluido);

    boolean existsByClaseIdAndClienteIdAndEstado(UUID claseId, UUID clienteId, ClaseReserva.Estado estado);

    // No filtra CANCELADA en el nombre para no atarse a un solo estado: "mis reservas"
    // muestra tanto lo confirmado (proximo) como lo ya asistido (historial), pero no lo cancelado.
    List<ClaseReserva> findByClienteIdAndEstadoNotOrderByClase_FechaDescClase_HoraInicioDesc(
            UUID clienteId, ClaseReserva.Estado estadoExcluido);

    List<ClaseReserva> findByClaseIdOrderByCreadoEnAsc(UUID claseId);

    Optional<ClaseReserva> findByIdAndClase_Id(UUID id, UUID claseId);
}
