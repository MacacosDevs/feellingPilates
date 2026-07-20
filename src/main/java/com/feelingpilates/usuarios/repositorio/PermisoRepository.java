package com.feelingpilates.usuarios.repositorio;

import com.feelingpilates.usuarios.entidad.Permiso;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PermisoRepository extends JpaRepository<Permiso, UUID> {

    Optional<Permiso> findByCodigo(String codigo);
}
