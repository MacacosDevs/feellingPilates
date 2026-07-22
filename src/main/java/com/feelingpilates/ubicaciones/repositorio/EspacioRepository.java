package com.feelingpilates.ubicaciones.repositorio;

import com.feelingpilates.ubicaciones.entidad.Espacio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EspacioRepository extends JpaRepository<Espacio, UUID> {

    List<Espacio> findBySalonIdAndActivoTrue(UUID salonId);

    void deleteBySalonId(UUID salonId);
}
