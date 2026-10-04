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
     * Version(es) de {@code (salonId, diaSemana)} vigentes en {@code fecha}. Legalmente hay a lo
     * sumo una: desde V46 la unicidad NO la da ya {@code UNIQUE(salon_id, dia_semana)} (retirado
     * en esa migracion, que habilita N versiones historicas por salon+dia), sino el EXCLUDE
     * {@code ex_horario_operacion_vigencia} (V45), que prohibe que dos versiones del mismo
     * salon+dia tengan vigencias solapadas. Se devuelve {@link List} en vez de
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

    /**
     * Igual que {@link #findVersionesQueIntersectan}, pero tomando lock pesimista de las filas
     * devueltas. La usan los writers de horario, que deciden y escriben sobre ese estado.
     *
     * <p>El {@code for update} se escribe literal en el SQL porque
     * {@link org.springframework.data.jpa.repository.Lock} no es fiable sobre queries nativas
     * (JPA no define {@code setLockMode} para ellas).
     *
     * <p><b>No sustituye al lock de {@code Salon}</b>: si el dia no tiene ninguna version, esta
     * query recorre cero filas y no bloquea nada. La serializacion real la da
     * {@link com.feelingpilates.ubicaciones.servicio.SalonLock}, que se adquiere <b>antes</b>.
     */
    @Query(value = """
            select h.*
            from horario_operacion h
            where h.salon_id = :salonId
              and h.dia_semana = :diaSemana
              and (cast(:hasta as date) is null or h.vigente_desde is null or h.vigente_desde <= :hasta)
              and (h.vigente_hasta is null or h.vigente_hasta >= :desde)
            order by h.vigente_desde asc nulls first
            for update
            """, nativeQuery = true)
    List<HorarioOperacion> bloquearVersionesQueIntersectan(
            @Param("salonId") UUID salonId,
            @Param("diaSemana") short diaSemana,
            @Param("desde") LocalDate desde,
            @Param("hasta") LocalDate hasta);

    /**
     * Toda la historia de un salon (o de un dia de ese salon), pasado cerrado incluido: ninguna
     * otra query del repositorio devuelve eso. Lectura pura para el endpoint de historial
     * (F2B.3b.2a, §24 del diseño); sin lock, sin invariantes nuevas.
     *
     * <p>{@code cast(:diaSemana as smallint) is null}: mismo patron que {@code cast(:hasta as date)
     * is null} de {@link #findVersionesQueIntersectan}, necesario porque PostgreSQL no puede
     * inferir el tipo de un parametro nulo. {@code nulls first} explicito porque en PostgreSQL
     * {@code ASC} pone {@code NULL} al final por defecto, y la fila legada {@code vigente_desde =
     * NULL} es {@code -infinito} y debe ir primera.
     */
    @Query(value = """
            select h.*
            from horario_operacion h
            where h.salon_id = :salonId
              and (cast(:diaSemana as smallint) is null or h.dia_semana = :diaSemana)
            order by h.dia_semana asc, h.vigente_desde asc nulls first
            """, nativeQuery = true)
    List<HorarioOperacion> findVersionesOrdenadas(
            @Param("salonId") UUID salonId,
            @Param("diaSemana") Short diaSemana);
}
