package com.feelingpilates.calendario.repositorio;

import com.feelingpilates.calendario.entidad.TurnoInstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface TurnoInstructorRepository extends JpaRepository<TurnoInstructor, UUID> {

    List<TurnoInstructor> findBySalonIdAndActivoTrue(UUID salonId);

    @Query("select t from TurnoInstructor t join t.instructores i "
            + "where i.id = :usuarioId and t.salon.id = :salonId and t.activo = true")
    List<TurnoInstructor> buscarPorInstructorYSalon(@Param("usuarioId") UUID usuarioId, @Param("salonId") UUID salonId);

    @Query("select t from TurnoInstructor t join t.instructores i "
            + "where i.id = :usuarioId and t.salon.id = :salonId and t.activo = true "
            + "and t.tipo = 'RECURRENTE' and t.diaSemana = :diaSemana")
    List<TurnoInstructor> buscarRecurrentesPorInstructorSalonYDia(
            @Param("usuarioId") UUID usuarioId, @Param("salonId") UUID salonId, @Param("diaSemana") Short diaSemana);

    @Query("select t from TurnoInstructor t join t.instructores i "
            + "where i.id = :usuarioId and t.salon.id = :salonId and t.activo = true "
            + "and t.fecha = :fecha and t.tipo <> 'RECURRENTE'")
    List<TurnoInstructor> buscarPuntualesPorInstructorSalonYFecha(
            @Param("usuarioId") UUID usuarioId, @Param("salonId") UUID salonId, @Param("fecha") LocalDate fecha);

    /** Todos los turnos recurrentes de ese salón/día, sin importar el instructor (para validar que el salón, como espacio físico, no tenga dos bloques encimados). */
    @Query("select distinct t from TurnoInstructor t "
            + "where t.salon.id = :salonId and t.activo = true "
            + "and t.tipo = 'RECURRENTE' and t.diaSemana = :diaSemana")
    List<TurnoInstructor> buscarRecurrentesPorSalonYDia(@Param("salonId") UUID salonId, @Param("diaSemana") Short diaSemana);

    /** Todas las excepciones (EXCEPCION) de ese salón/fecha, sin importar el instructor. */
    @Query("select distinct t from TurnoInstructor t "
            + "where t.salon.id = :salonId and t.activo = true "
            + "and t.fecha = :fecha and t.tipo = 'EXCEPCION'")
    List<TurnoInstructor> buscarExcepcionesPorSalonYFecha(@Param("salonId") UUID salonId, @Param("fecha") LocalDate fecha);

    /**
     * Todos los bloques puntuales (EXCEPCION y CANCELACION) de ese salón/fecha, sin importar el
     * instructor — para materializar clases hace falta ver también las CANCELACION, a diferencia
     * de {@link #buscarExcepcionesPorSalonYFecha} que solo trae EXCEPCION.
     */
    @Query("select distinct t from TurnoInstructor t "
            + "where t.salon.id = :salonId and t.activo = true "
            + "and t.fecha = :fecha and t.tipo <> 'RECURRENTE'")
    List<TurnoInstructor> buscarPuntualesPorSalonYFecha(@Param("salonId") UUID salonId, @Param("fecha") LocalDate fecha);

    /**
     * Bloques EXCEPCION/CANCELACION (con fecha) de un salon, paginados. El dia de la semana no
     * existe como columna para estos bloques (solo aplica a RECURRENTE), asi que se deriva de la
     * fecha con EXTRACT(DOW ...), que ya coincide con la convencion 0=domingo del resto del
     * sistema. Filtra opcionalmente por instructor (:usuarioId puede ser null = cualquiera).
     */
    @Query(
            value = """
                    select distinct t.* from turno_instructor t
                    left join turno_instructor_usuario tiu on tiu.turno_id = t.id
                    where t.salon_id = :salonId
                    and t.activo = true and t.tipo <> 'RECURRENTE'
                    and (:usuarioId is null or tiu.usuario_id = :usuarioId)
                    and (:tipo is null or t.tipo = :tipo)
                    and (:diaSemana is null or extract(dow from t.fecha) = :diaSemana)
                    order by t.fecha desc
                    """,
            countQuery = """
                    select count(distinct t.id) from turno_instructor t
                    left join turno_instructor_usuario tiu on tiu.turno_id = t.id
                    where t.salon_id = :salonId
                    and t.activo = true and t.tipo <> 'RECURRENTE'
                    and (:usuarioId is null or tiu.usuario_id = :usuarioId)
                    and (:tipo is null or t.tipo = :tipo)
                    and (:diaSemana is null or extract(dow from t.fecha) = :diaSemana)
                    """,
            nativeQuery = true)
    Page<TurnoInstructor> buscarPuntuales(
            @Param("salonId") UUID salonId,
            @Param("usuarioId") UUID usuarioId,
            @Param("tipo") String tipo,
            @Param("diaSemana") Integer diaSemana,
            Pageable pageable);
}
