package com.feelingpilates.ubicaciones.repositorio;

import com.feelingpilates.ubicaciones.entidad.SalonHorarioExcepcion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SalonHorarioExcepcionRepository extends JpaRepository<SalonHorarioExcepcion, UUID> {

    List<SalonHorarioExcepcion> findBySalonIdAndFechaBetweenAndActivoTrueOrderByFecha(
            UUID salonId, LocalDate desde, LocalDate hasta);

    Optional<SalonHorarioExcepcion> findBySalonIdAndFechaAndActivoTrue(UUID salonId, LocalDate fecha);
}
