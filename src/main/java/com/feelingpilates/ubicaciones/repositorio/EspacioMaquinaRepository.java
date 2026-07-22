package com.feelingpilates.ubicaciones.repositorio;

import com.feelingpilates.ubicaciones.entidad.EspacioMaquina;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EspacioMaquinaRepository extends JpaRepository<EspacioMaquina, EspacioMaquina.Id> {

    List<EspacioMaquina> findByEspacioId(UUID espacioId);

    void deleteByEspacioId(UUID espacioId);
}
