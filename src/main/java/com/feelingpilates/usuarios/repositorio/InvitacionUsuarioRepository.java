package com.feelingpilates.usuarios.repositorio;

import com.feelingpilates.usuarios.entidad.InvitacionUsuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface InvitacionUsuarioRepository extends JpaRepository<InvitacionUsuario, UUID> {

    Optional<InvitacionUsuario> findByToken(String token);
}
