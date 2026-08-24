package com.feelingpilates.ubicaciones.repositorio;

import com.feelingpilates.ubicaciones.entidad.Salon;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SalonRepository extends JpaRepository<Salon, UUID> {

    /**
     * Toma un lock pesimista de fila ({@code SELECT ... FOR UPDATE}) sobre el salon. Es el punto
     * de serializacion compartido de todos los writers que tocan el horario de operacion del
     * salon o la programacion que depende de el.
     *
     * <p>Se bloquea la fila <b>padre</b> y no las de {@code horario_operacion} porque un dia puede
     * no tener ninguna version todavia: un {@code FOR UPDATE} sobre cero filas no bloquea nada y
     * dos altas iniciales concurrentes correrian en paralelo, quedando solo el EXCLUDE como red.
     *
     * <p>La query es JPQL a proposito: {@link Lock} solo aplica {@code FOR UPDATE} de forma fiable
     * sobre JPQL; JPA no define {@code setLockMode} para queries nativas.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Salon s where s.id = :salonId")
    Optional<Salon> bloquearParaActualizar(@Param("salonId") UUID salonId);

    @Query(value = """
            select s.id as id, s.nombre as nombre, s.direccion as direccion,
                   s.estado_id as estadoId, e.nombre as estadoNombre,
                   s.municipio_id as municipioId, m.nombre as municipioNombre,
                   s.creado_en as creadoEn
            from salon s
            join estado e on e.id = s.estado_id
            join municipio m on m.estado_id = s.estado_id and m.id = s.municipio_id
            where s.activo = true
            order by e.nombre, m.nombre, s.nombre
            """, nativeQuery = true)
    List<SalonProjection> listarActivos();

    long countByIdInAndActivoTrue(List<UUID> ids);

    interface SalonProjection {
        UUID getId();
        String getNombre();
        String getDireccion();
        Short getEstadoId();
        String getEstadoNombre();
        Short getMunicipioId();
        String getMunicipioNombre();
        Instant getCreadoEn();
    }
}
