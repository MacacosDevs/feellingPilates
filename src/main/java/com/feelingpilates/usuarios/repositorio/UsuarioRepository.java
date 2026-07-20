package com.feelingpilates.usuarios.repositorio;

import com.feelingpilates.usuarios.entidad.Usuario;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {

    @EntityGraph(attributePaths = {"roles", "roles.rol", "roles.rol.permisos"})
    Optional<Usuario> findByCorreo(String correo);

    boolean existsByCorreo(String correo);
}
