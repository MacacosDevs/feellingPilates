package com.feelingpilates.clases.repositorio;

import com.feelingpilates.clases.entidad.Clase;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ClaseRepository extends JpaRepository<Clase, UUID> {

    List<Clase> findBySalonIdInAndFechaBetweenOrderByFechaAscHoraInicioAsc(
            List<UUID> salonIds, LocalDate desde, LocalDate hasta);

    List<Clase> findByInstructorIdAndFechaBetweenOrderByFechaAscHoraInicioAsc(
            UUID instructorId, LocalDate desde, LocalDate hasta);

    /**
     * Upsert de materializacion: inserta la ocurrencia si no existe ya una con la misma
     * llave natural (salon+instructor+actividad+fecha+hora_inicio). El ON CONFLICT hace
     * esto seguro entre requests concurrentes pidiendo el mismo rango de fechas.
     */
    @Modifying
    @Query(
            value = """
                    insert into clase
                        (id, turno_origen_id, salon_id, instructor_id, tipo_actividad_id,
                         fecha, hora_inicio, hora_fin, capacidad, estado, creado_en, actualizado_en)
                    values
                        (gen_random_uuid(), :turnoOrigenId, :salonId, :instructorId, :tipoActividadId,
                         :fecha, :horaInicio, :horaFin, :capacidad, 'PROGRAMADA', now(), now())
                    on conflict (salon_id, instructor_id, tipo_actividad_id, fecha, hora_inicio) do nothing
                    """,
            nativeQuery = true)
    void insertarSiNoExiste(
            @Param("turnoOrigenId") UUID turnoOrigenId,
            @Param("salonId") UUID salonId,
            @Param("instructorId") UUID instructorId,
            @Param("tipoActividadId") UUID tipoActividadId,
            @Param("fecha") LocalDate fecha,
            @Param("horaInicio") LocalTime horaInicio,
            @Param("horaFin") LocalTime horaFin,
            @Param("capacidad") short capacidad);

    /**
     * Cuenta los lugares ocupados: CONFIRMADA y ASISTIO cuentan (siguen ocupando el lugar
     * que reservaron), solo CANCELADA lo libera. Nativo en vez de join JPQL porque en este
     * punto del módulo (lectura de clases) todavía no hace falta la entidad ClaseReserva
     * completa, y evita acoplar ClaseService a un repositorio de otro dominio antes de que exista.
     */
    @Query(value = "select count(*) from clase_reserva where clase_id = :claseId and estado <> 'CANCELADA'", nativeQuery = true)
    long contarOcupados(@Param("claseId") UUID claseId);

    /** Bloquea la fila mientras dura la transacción de reservar, para que dos requests
     * casi simultáneos no lean el mismo conteo de cupo disponible antes de insertar. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Clase c where c.id = :id")
    Optional<Clase> findConBloqueoById(@Param("id") UUID id);
}
