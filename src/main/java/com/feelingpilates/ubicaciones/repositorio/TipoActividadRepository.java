package com.feelingpilates.ubicaciones.repositorio;

import com.feelingpilates.ubicaciones.entidad.TipoActividad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface TipoActividadRepository extends JpaRepository<TipoActividad, UUID> {

    List<TipoActividad> findByActivoTrueOrderByNombre();

    @Query(value = """
            select * from tipo_actividad t
            where t.activo = true
              and (
                t.nombre ilike concat('%', :buscar, '%')
                or exists (select 1 from unnest(t.etiquetas) e where e ilike concat('%', :buscar, '%'))
              )
            order by t.nombre
            """, nativeQuery = true)
    List<TipoActividad> buscarActivosPorNombreOEtiqueta(@Param("buscar") String buscar);
}
