package com.feelingpilates.ubicaciones.repositorio;

import com.feelingpilates.ubicaciones.entidad.HorarioOperacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface HorarioOperacionRepository extends JpaRepository<HorarioOperacion, UUID> {

    List<HorarioOperacion> findBySalonIdOrderByDiaSemana(UUID salonId);

    void deleteBySalonId(UUID salonId);

    /**
     * Version(es) de {@code (salonId, diaSemana)} vigentes en {@code fecha}. Legalmente hay
     * a lo sumo una (UNIQUE(salon_id, dia_semana)); se devuelve {@link List} en vez de
     * {@link java.util.Optional} para que el resolver pueda distinguir explicitamente 0/1/>1
     * en vez de ocultar el estado imposible tras una excepcion generica.
     */
    @Query(value = """
            select h.*
            from horario_operacion h
            where h.salon_id = :salonId
              and h.dia_semana = :diaSemana
              and (h.vigente_desde is null or h.vigente_desde <= :fecha)
              and (h.vigente_hasta is null or h.vigente_hasta >= :fecha)
            """, nativeQuery = true)
    List<HorarioOperacion> findVigente(
            @Param("salonId") UUID salonId,
            @Param("diaSemana") short diaSemana,
            @Param("fecha") LocalDate fecha);

    /**
     * Versiones de {@code (salonId, diaSemana)} cuya vigencia intersecta {@code [desde, hasta]}.
     * {@code hasta == null} representa un rango abierto hacia +infinito. {@code desde} es
     * obligatorio. Ordenado por {@code vigenteDesde} con NULLS FIRST para que la fila legada
     * (si existe) quede primera y el barrido de cobertura por intervalos pueda hacerse en una
     * sola pasada.
     */
    @Query(value = """
            select h.*
            from horario_operacion h
            where h.salon_id = :salonId
              and h.dia_semana = :diaSemana
              and (cast(:hasta as date) is null or h.vigente_desde is null or h.vigente_desde <= :hasta)
              and (h.vigente_hasta is null or h.vigente_hasta >= :desde)
            order by h.vigente_desde asc nulls first
            """, nativeQuery = true)
    List<HorarioOperacion> findVersionesQueIntersectan(
            @Param("salonId") UUID salonId,
            @Param("diaSemana") short diaSemana,
            @Param("desde") LocalDate desde,
            @Param("hasta") LocalDate hasta);
}
