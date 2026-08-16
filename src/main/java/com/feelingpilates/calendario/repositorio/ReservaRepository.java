package com.feelingpilates.calendario.repositorio;

import com.feelingpilates.calendario.entidad.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public interface ReservaRepository extends JpaRepository<Reserva, UUID> {

    List<Reserva> findByInstructorIdAndFechaAndEstado(UUID instructorId, LocalDate fecha, Reserva.Estado estado);

    List<Reserva> findBySalonIdAndFechaAndEstado(UUID salonId, LocalDate fecha, Reserva.Estado estado);

    List<Reserva> findByClienteIdAndEstadoOrderByFechaDescHoraInicioDesc(UUID clienteId, Reserva.Estado estado);

    @Query("""
            SELECT COUNT(r) > 0 FROM Reserva r
            WHERE r.instructor.id = :instructorId
              AND r.fecha = :fecha
              AND r.estado = :estado
              AND r.horaInicio < :horaFin
              AND r.horaFin > :horaInicio
            """)
    boolean existeTraslape(
            @Param("instructorId") UUID instructorId,
            @Param("fecha") LocalDate fecha,
            @Param("horaInicio") LocalTime horaInicio,
            @Param("horaFin") LocalTime horaFin,
            @Param("estado") Reserva.Estado estado);
}
