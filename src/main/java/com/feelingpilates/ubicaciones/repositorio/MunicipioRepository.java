package com.feelingpilates.ubicaciones.repositorio;

import com.feelingpilates.ubicaciones.entidad.Municipio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MunicipioRepository extends JpaRepository<Municipio, Municipio.Id> {

    List<Municipio> findByIdEstadoIdOrderByNombre(Short estadoId);
}
