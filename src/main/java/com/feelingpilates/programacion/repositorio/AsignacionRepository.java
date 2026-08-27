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

    @Query(value = """
            select
                a.serie_id as serieId,
                a.id as asignacionVersionId,
                b.id as bloqueVersionId,
                b.salon_id as salonId,
                a.instructor_id as instructorId,
                a.tipo_actividad_id as tipoActividadId,
                a.hora_inicio as horaInicio,
                a.hora_fin as horaFin
            from programacion_asignacion a
            join programacion_bloque b on b.id = a.bloque_id
            where a.activo = true
              and b.activo = true
              and b.dia_semana = :diaSemana
              and a.vigente_desde <= :fecha
              and (a.vigente_hasta is null or a.vigente_hasta >= :fecha)
              and b.vigente_desde <= :fecha
              and (b.vigente_hasta is null or b.vigente_hasta >= :fecha)
            order by a.serie_id, a.id
            """, nativeQuery = true)
    List<OcurrenciaNominalProjection> buscarNominalesDeFecha(
            @Param("fecha") LocalDate fecha, @Param("diaSemana") short diaSemana);

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
              and (cast(:vigenteHasta as date) is null or a.vigente_desde <= :vigenteHasta)
              and (a.vigente_hasta is null or :vigenteDesde <= a.vigente_hasta)
            """, nativeQuery = true)
    List<Asignacion> buscarConflictosRecurrentesDelInstructor(
            @Param("instructorId") UUID instructorId,
            @Param("diaSemana") short diaSemana,
            @Param("horaInicio") LocalTime horaInicio,
            @Param("horaFin") LocalTime horaFin,
            @Param("vigenteDesde") LocalDate vigenteDesde,
            @Param("vigenteHasta") LocalDate vigenteHasta);

    interface OcurrenciaNominalProjection {
        UUID getSerieId();
        UUID getAsignacionVersionId();
        UUID getBloqueVersionId();
        UUID getSalonId();
        UUID getInstructorId();
        UUID getTipoActividadId();
        LocalTime getHoraInicio();
        LocalTime getHoraFin();
    }
}
