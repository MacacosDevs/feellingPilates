package com.feelingpilates.usuarios.servicio;

import com.feelingpilates.exception.ResourceNotFoundException;
import com.feelingpilates.usuarios.entidad.Usuario;
import com.feelingpilates.usuarios.repositorio.UsuarioRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/** Adquiere todos los instructores después de los salones, en orden UUID ascendente. */
@Component
public class InstructorLocks {

    private final UsuarioRepository usuarioRepository;

    public InstructorLocks(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public List<Usuario> adquirirOrdenados(Collection<UUID> ids) {
        if (ids == null) {
            throw new IllegalArgumentException("La colección de instructores es obligatoria");
        }
        return ids.stream()
                .filter(Objects::nonNull)
                .distinct()
                .sorted(Comparator.naturalOrder())
                .map(this::adquirir)
                .toList();
    }

    private Usuario adquirir(UUID id) {
        return usuarioRepository.bloquearParaActualizar(id)
                .orElseThrow(() -> new ResourceNotFoundException("Instructor no encontrado"));
    }
}
