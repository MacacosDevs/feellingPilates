package com.feelingpilates.ubicaciones.repositorio;

import com.feelingpilates.ubicaciones.entidad.SalonRecurso;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SalonRecursoRepository extends JpaRepository<SalonRecurso, SalonRecurso.Id> {

    List<SalonRecurso> findBySalonId(UUID salonId);

    void deleteBySalonId(UUID salonId);
}
