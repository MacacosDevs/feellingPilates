package com.feelingpilates.pagos.repositorio;

import com.feelingpilates.pagos.entidad.Paquete;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PaqueteRepository extends JpaRepository<Paquete, UUID> {

    List<Paquete> findByActivoTrueOrderByCategoriaAscOrdenAsc();

    List<Paquete> findAllByOrderByActivoDescOrdenAsc();
}
