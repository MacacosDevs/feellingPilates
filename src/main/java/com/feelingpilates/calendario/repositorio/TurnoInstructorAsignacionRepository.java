package com.feelingpilates.calendario.repositorio;

import com.feelingpilates.calendario.entidad.TurnoInstructorAsignacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TurnoInstructorAsignacionRepository
        extends JpaRepository<TurnoInstructorAsignacion, TurnoInstructorAsignacion.Id> {

    void deleteByTurno_Id(UUID turnoId);
}
