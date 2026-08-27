package com.feelingpilates.programacion.repositorio;

import com.feelingpilates.programacion.entidad.AjusteProgramacionFecha;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AjusteProgramacionFechaRepository
        extends JpaRepository<AjusteProgramacionFecha, UUID> {

    Optional<AjusteProgramacionFecha> findByIdAndActivoTrue(UUID id);

    List<AjusteProgramacionFecha> findByAsignacionSerieIdAndFechaAndActivoTrue(
            UUID asignacionSerieId, LocalDate fecha);

    List<AjusteProgramacionFecha> findAllByFechaAndActivoTrueOrderById(LocalDate fecha);

    List<AjusteProgramacionFecha> findAllBySalonResultadoIdAndFechaAndActivoTrue(
            UUID salonId, LocalDate fecha);

    List<AjusteProgramacionFecha> findAllByInstructorResultadoIdAndFechaAndActivoTrue(
            UUID instructorId, LocalDate fecha);

    @Query("""
            select a from AjusteProgramacionFecha a
            where a.asignacionSerieId = :serieId
              and a.activo = true
              and a.fecha >= :desde
            order by a.fecha, a.id
            """)
    List<AjusteProgramacionFecha> buscarTargetsActivosDesde(
            @Param("serieId") UUID serieId, @Param("desde") LocalDate desde);

    @Query("""
            select a from AjusteProgramacionFecha a
            where a.activo = true
              and a.fecha >= :desde
              and (cast(:hasta as date) is null or a.fecha <= :hasta)
            order by a.fecha, a.id
            """)
    List<AjusteProgramacionFecha> buscarActivosEnRango(
            @Param("desde") LocalDate desde, @Param("hasta") LocalDate hasta);
}
