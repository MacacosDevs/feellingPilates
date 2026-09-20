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
              and (cast(:vigenteHasta as date) is null or b.vigente_desde <= :vigenteHasta)
              and (b.vigente_hasta is null or :vigenteDesde <= b.vigente_hasta)
            """, nativeQuery = true)
    List<BloqueProgramacion> buscarTraslapesActivos(
            @Param("salonId") UUID salonId,
            @Param("diaSemana") short diaSemana,
            @Param("horaInicio") LocalTime horaInicio,
            @Param("horaFin") LocalTime horaFin,
            @Param("vigenteDesde") LocalDate vigenteDesde,
            @Param("vigenteHasta") LocalDate vigenteHasta);

    /**
     * Bloques activos del salon/dia cuya vigencia intersecta {@code [desde, +infinito)}. La usa la
     * validacion inversa del writer de horario para no traer tambien historia ya vencida, que
     * {@code findBySalonIdAndDiaSemanaAndActivoTrueOrderByHoraInicio} si devolveria.
     *
     * <p>Como {@code programacion_bloque.vigente_desde} es NOT NULL y el objetivo es abierto por la
     * derecha, la unica condicion necesaria <b>es</b> "la vigencia del bloque intersecta
     * {@code [desde, +infinito)}"; no hay que replicar {@code RangoVigencia.intersecta} en SQL.
     */
    @Query(value = """
            select b.*
            from programacion_bloque b
            where b.salon_id = :salonId
              and b.dia_semana = :diaSemana
              and b.activo = true
              and (b.vigente_hasta is null or b.vigente_hasta >= :desde)
            order by b.hora_inicio
            """, nativeQuery = true)
    List<BloqueProgramacion> buscarActivosVigentesDesde(
            @Param("salonId") UUID salonId,
            @Param("diaSemana") short diaSemana,
            @Param("desde") LocalDate desde);
}
