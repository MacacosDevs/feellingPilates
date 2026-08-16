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

    List<TurnoInstructor> findByUsuarioIdAndSalonIdAndActivoTrue(UUID usuarioId, UUID salonId);

    List<TurnoInstructor> findBySalonIdAndActivoTrue(UUID salonId);

    List<TurnoInstructor> findByUsuarioIdAndActivoTrue(UUID usuarioId);

    List<TurnoInstructor> findByUsuarioIdAndSalonIdAndActivoTrueAndDiaSemana(
            UUID usuarioId, UUID salonId, Short diaSemana);

    List<TurnoInstructor> findByUsuarioIdAndSalonIdAndActivoTrueAndFecha(
            UUID usuarioId, UUID salonId, LocalDate fecha);

    /**
     * Turnos EXCEPCION/CANCELACION (con fecha) de un instructor en un salon, paginados. El dia
     * de la semana no existe como columna para estos turnos (solo aplica a RECURRENTE), asi que
     * se deriva de la fecha con EXTRACT(DOW ...), que ya coincide con la convencion 0=domingo
     * usada en el resto del sistema.
     */
    @Query(
            value = """
                    select * from turno_instructor t
                    where t.usuario_id = :usuarioId and t.salon_id = :salonId
                    and t.activo = true and t.tipo <> 'RECURRENTE'
                    and (:tipo is null or t.tipo = :tipo)
                    and (:diaSemana is null or extract(dow from t.fecha) = :diaSemana)
                    order by t.fecha desc
                    """,
            countQuery = """
                    select count(*) from turno_instructor t
                    where t.usuario_id = :usuarioId and t.salon_id = :salonId
                    and t.activo = true and t.tipo <> 'RECURRENTE'
                    and (:tipo is null or t.tipo = :tipo)
                    and (:diaSemana is null or extract(dow from t.fecha) = :diaSemana)
                    """,
            nativeQuery = true)
    Page<TurnoInstructor> buscarPuntuales(
            @Param("usuarioId") UUID usuarioId,
            @Param("salonId") UUID salonId,
            @Param("tipo") String tipo,
            @Param("diaSemana") Integer diaSemana,
            Pageable pageable);
}
