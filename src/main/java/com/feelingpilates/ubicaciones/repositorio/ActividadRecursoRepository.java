package com.feelingpilates.ubicaciones.repositorio;

import com.feelingpilates.ubicaciones.entidad.ActividadRecurso;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ActividadRecursoRepository extends JpaRepository<ActividadRecurso, ActividadRecurso.Id> {

    List<ActividadRecurso> findByTipoActividadId(UUID tipoActividadId);

    void deleteByTipoActividadId(UUID tipoActividadId);
}
