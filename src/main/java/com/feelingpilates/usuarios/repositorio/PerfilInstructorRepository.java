package com.feelingpilates.usuarios.repositorio;

import com.feelingpilates.usuarios.entidad.PerfilInstructor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PerfilInstructorRepository extends JpaRepository<PerfilInstructor, UUID> {

    Optional<PerfilInstructor> findByUsuarioId(UUID usuarioId);
}
