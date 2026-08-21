package com.feelingpilates.seguridad;

import com.feelingpilates.usuarios.entidad.Rol;
import com.feelingpilates.usuarios.entidad.Usuario;
import com.feelingpilates.usuarios.entidad.UsuarioRol;
import com.feelingpilates.usuarios.repositorio.UsuarioRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Set;
import java.util.UUID;

/** Autoriza una operacion combinando actor, permiso funcional y salon objetivo desde BD. */
@Service
public class AutorizadorSalon {

    private static final Set<String> ROLES_GLOBALES = Set.of(Rol.ADMIN, Rol.SUPER_ADMIN);

    private final UsuarioRepository usuarioRepository;

    public AutorizadorSalon(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public void verificarAccesoSalon(UUID actorId, String permiso, UUID salonId) {
        verificarAccesoSalon(actorId, salonId, permiso);
    }

    /**
     * Variante para operaciones cuyo permiso de borde admite alternativas. El permiso y el
     * alcance deben provenir de la misma asignacion UsuarioRol; no se mezclan entre roles.
     */
    @Transactional(readOnly = true)
    public void verificarAccesoSalon(UUID actorId, UUID salonId, String... permisos) {
        Usuario usuario = usuarioRepository.findById(actorId)
                .orElseThrow(() -> new AccessDeniedException("El usuario autenticado ya no existe"));
        if (usuario.getEstatus() != Usuario.EstatusUsuario.activo) {
            throw new AccessDeniedException("El usuario autenticado no esta activo");
        }

        Set<String> permisosRequeridos = Set.copyOf(Arrays.asList(permisos));
        boolean tienePermiso = usuario.getRoles().stream()
                .anyMatch(asignacion -> concedeAlguno(asignacion, permisosRequeridos));
        if (!tienePermiso) {
            throw new AccessDeniedException("El usuario no tiene el permiso requerido");
        }

        boolean tieneScope = usuario.getRoles().stream()
                .anyMatch(asignacion -> concedeAlguno(asignacion, permisosRequeridos)
                        && aplicaAlSalon(asignacion, salonId));
        if (!tieneScope) {
            throw new AccessDeniedException("El usuario no tiene acceso al salon objetivo");
        }
    }

    private boolean concedeAlguno(UsuarioRol asignacion, Set<String> permisosRequeridos) {
        if (Rol.SUPER_ADMIN.equals(asignacion.getRol().getNombre()) && asignacion.getSalon() == null) {
            return true;
        }
        return asignacion.getRol().getPermisos().stream()
                .anyMatch(permiso -> permisosRequeridos.contains(permiso.getCodigo()));
    }

    private boolean aplicaAlSalon(UsuarioRol asignacion, UUID salonId) {
        if (asignacion.getSalon() != null) {
            return asignacion.getSalon().getId().equals(salonId);
        }
        return ROLES_GLOBALES.contains(asignacion.getRol().getNombre());
    }
}
