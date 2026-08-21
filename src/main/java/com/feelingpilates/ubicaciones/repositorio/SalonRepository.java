package com.feelingpilates.ubicaciones.repositorio;

import com.feelingpilates.ubicaciones.entidad.Salon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface SalonRepository extends JpaRepository<Salon, UUID> {

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
