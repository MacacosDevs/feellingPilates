package com.feelingpilates.ubicaciones.repositorio;

import com.feelingpilates.ubicaciones.entidad.SalonMaquina;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SalonMaquinaRepository extends JpaRepository<SalonMaquina, SalonMaquina.Id> {

    List<SalonMaquina> findBySalonId(UUID salonId);

    void deleteBySalonId(UUID salonId);
}
