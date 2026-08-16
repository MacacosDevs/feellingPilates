package com.feelingpilates.usuarios.servicio;

import com.feelingpilates.usuarios.entidad.Permiso;
import com.feelingpilates.usuarios.entidad.Rol;
import com.feelingpilates.usuarios.entidad.Usuario;
import com.feelingpilates.usuarios.repositorio.PermisoRepository;
import org.springframework.stereotype.Component;

import java.util.List;

/** Calcula los permisos efectivos de un usuario: SUPER_ADMIN los tiene todos, el resto la union de sus roles. */
@Component
public class PermisoResolver {

    private final PermisoRepository permisoRepository;

    public PermisoResolver(PermisoRepository permisoRepository) {
        this.permisoRepository = permisoRepository;
    }

    public List<String> resolver(Usuario usuario) {
        List<String> roles = usuario.getRoles().stream()
                .map(ur -> ur.getRol().getNombre())
                .distinct()
                .toList();
        return roles.contains(Rol.SUPER_ADMIN)
                ? permisoRepository.findAll().stream().map(Permiso::getCodigo).distinct().toList()
                : usuario.getRoles().stream()
                        .flatMap(ur -> ur.getRol().getPermisos().stream())
                        .map(Permiso::getCodigo)
                        .distinct()
                        .toList();
    }
}
