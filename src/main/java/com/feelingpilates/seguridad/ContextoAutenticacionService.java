package com.feelingpilates.seguridad;

import com.feelingpilates.usuarios.entidad.Usuario;
import com.feelingpilates.usuarios.repositorio.UsuarioRepository;
import com.feelingpilates.usuarios.servicio.PermisoResolver;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Resuelve, dentro de una transaccion, los roles y permisos vigentes de un
 * usuario. Existe para que JwtAuthFilter pueda recalcularlos en cada request
 * sin arrastrar colecciones lazy de Hibernate fuera de la sesion (el filtro
 * corre antes de que exista una sesion/transaccion abierta).
 */
@Service
public class ContextoAutenticacionService {

    private final UsuarioRepository usuarioRepository;
    private final PermisoResolver permisoResolver;

    public ContextoAutenticacionService(UsuarioRepository usuarioRepository, PermisoResolver permisoResolver) {
        this.usuarioRepository = usuarioRepository;
        this.permisoResolver = permisoResolver;
    }

    public record Contexto(UUID id, String correo, boolean activo, List<String> roles, List<String> permisos) {
    }

    @Transactional(readOnly = true)
    public Optional<Contexto> resolver(UUID usuarioId) {
        return usuarioRepository.findById(usuarioId).map(usuario -> new Contexto(
                usuario.getId(),
                usuario.getCorreo(),
                usuario.getEstatus() == Usuario.EstatusUsuario.activo,
                usuario.getRoles().stream().map(ur -> ur.getRol().getNombre()).distinct().toList(),
                permisoResolver.resolver(usuario)));
    }
}
