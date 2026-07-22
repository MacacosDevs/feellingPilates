package com.feelingpilates.ubicaciones.repositorio;

import com.feelingpilates.ubicaciones.entidad.HorarioOperacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface HorarioOperacionRepository extends JpaRepository<HorarioOperacion, UUID> {

    List<HorarioOperacion> findBySalonIdOrderByDiaSemana(UUID salonId);

    void deleteBySalonId(UUID salonId);
}
