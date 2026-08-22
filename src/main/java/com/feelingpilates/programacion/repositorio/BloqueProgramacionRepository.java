package com.feelingpilates.programacion.repositorio;

import com.feelingpilates.programacion.entidad.BloqueProgramacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public interface BloqueProgramacionRepository extends JpaRepository<BloqueProgramacion, UUID> {

    List<BloqueProgramacion> findBySalonIdAndDiaSemanaAndActivoTrueOrderByHoraInicio(
            UUID salonId, short diaSemana);

    @Query(value = """
            select b.*
            from programacion_bloque b
            where b.salon_id = :salonId
              and b.dia_semana = :diaSemana
              and b.activo = true
              and b.hora_inicio < :horaFin
              and :horaInicio < b.hora_fin
              and (:vigenteHasta is null or b.vigente_desde <= :vigenteHasta)
              and (b.vigente_hasta is null or :vigenteDesde <= b.vigente_hasta)
            """, nativeQuery = true)
    List<BloqueProgramacion> buscarTraslapesActivos(
            @Param("salonId") UUID salonId,
            @Param("diaSemana") short diaSemana,
            @Param("horaInicio") LocalTime horaInicio,
            @Param("horaFin") LocalTime horaFin,
            @Param("vigenteDesde") LocalDate vigenteDesde,
            @Param("vigenteHasta") LocalDate vigenteHasta);
}
