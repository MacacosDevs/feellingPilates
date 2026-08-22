package com.feelingpilates.programacion.repositorio;

import com.feelingpilates.programacion.entidad.Asignacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public interface AsignacionRepository extends JpaRepository<Asignacion, UUID> {

    List<Asignacion> findByBloqueIdAndActivoTrueOrderByHoraInicio(UUID bloqueId);

    @Query(value = """
            select a.*
            from programacion_asignacion a
            join programacion_bloque b on b.id = a.bloque_id
            where a.instructor_id = :instructorId
              and a.activo = true
              and b.activo = true
              and b.dia_semana = :diaSemana
              and a.hora_inicio < :horaFin
              and :horaInicio < a.hora_fin
              and (:vigenteHasta is null or a.vigente_desde <= :vigenteHasta)
              and (a.vigente_hasta is null or :vigenteDesde <= a.vigente_hasta)
            """, nativeQuery = true)
    List<Asignacion> buscarConflictosRecurrentesDelInstructor(
            @Param("instructorId") UUID instructorId,
            @Param("diaSemana") short diaSemana,
            @Param("horaInicio") LocalTime horaInicio,
            @Param("horaFin") LocalTime horaFin,
            @Param("vigenteDesde") LocalDate vigenteDesde,
            @Param("vigenteHasta") LocalDate vigenteHasta);
}
