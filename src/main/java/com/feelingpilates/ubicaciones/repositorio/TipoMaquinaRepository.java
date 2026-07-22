package com.feelingpilates.ubicaciones.repositorio;

import com.feelingpilates.ubicaciones.entidad.TipoMaquina;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TipoMaquinaRepository extends JpaRepository<TipoMaquina, UUID> {

    List<TipoMaquina> findByActivoTrueOrderByNombre();
}
