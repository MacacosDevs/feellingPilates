package com.feelingpilates.usuarios.servicio;

import com.feelingpilates.exception.ConflictException;
import com.feelingpilates.exception.ResourceNotFoundException;
import com.feelingpilates.usuarios.dto.PermisoResponse;
import com.feelingpilates.usuarios.dto.RolResponse;
import com.feelingpilates.usuarios.entidad.Permiso;
import com.feelingpilates.usuarios.entidad.Rol;
import com.feelingpilates.usuarios.repositorio.PermisoRepository;
import com.feelingpilates.usuarios.repositorio.RolRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class RolService {

    private final RolRepository rolRepository;
    private final PermisoRepository permisoRepository;

    public RolService(RolRepository rolRepository, PermisoRepository permisoRepository) {
        this.rolRepository = rolRepository;
        this.permisoRepository = permisoRepository;
    }

    @Transactional(readOnly = true)
    public List<RolResponse> listar() {
        return rolRepository.findAll().stream().map(this::mapear).toList();
    }

    @Transactional(readOnly = true)
    public List<PermisoResponse> listarPermisos() {
        return permisoRepository.findAll().stream()
                .map(p -> new PermisoResponse(p.getCodigo(), p.getDescripcion(), p.getCategoria()))
                .toList();
    }

    @Transactional
    public RolResponse crear(String nombre, String descripcion) {
        if (rolRepository.findByNombre(nombre).isPresent()) {
            throw new ConflictException("Ya existe un rol con ese nombre: " + nombre);
        }
        Rol rol = new Rol();
        rol.setNombre(nombre);
        rol.setDescripcion(descripcion);
        return mapear(rolRepository.save(rol));
    }

    @Transactional
    public RolResponse actualizarDatos(UUID rolId, String nombre, String descripcion) {
        Rol rol = rolRepository.findById(rolId)
                .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado: " + rolId));
        if (Rol.SUPER_ADMIN.equals(rol.getNombre())) {
            throw new ConflictException("SUPER_ADMIN no es editable");
        }
        rolRepository.findByNombre(nombre)
                .filter(otro -> !otro.getId().equals(rolId))
                .ifPresent(otro -> {
                    throw new ConflictException("Ya existe un rol con ese nombre: " + nombre);
                });

        rol.setNombre(nombre);
        rol.setDescripcion(descripcion);
        return mapear(rol);
    }

    @Transactional
    public RolResponse actualizarPermisos(UUID rolId, List<String> codigos) {
        Rol rol = rolRepository.findById(rolId)
                .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado: " + rolId));
        if (Rol.SUPER_ADMIN.equals(rol.getNombre())) {
            throw new ConflictException("SUPER_ADMIN no es editable: siempre tiene todos los permisos");
        }

        Set<String> codigosUnicos = new HashSet<>(codigos);
        List<Permiso> permisos = permisoRepository.findAllByCodigoIn(codigos);
        if (permisos.size() != codigosUnicos.size()) {
            throw new ResourceNotFoundException("Alguno de los permisos indicados no existe");
        }

        rol.setPermisos(new HashSet<>(permisos));
        return mapear(rol);
    }

    private RolResponse mapear(Rol rol) {
        boolean esSuperAdmin = Rol.SUPER_ADMIN.equals(rol.getNombre());
        // SUPER_ADMIN no usa rol_permiso: mostramos el catalogo completo como informativo,
        // que es exactamente lo que AuthService le otorga dinamicamente al hacer login.
        List<String> codigos = esSuperAdmin
                ? permisoRepository.findAll().stream().map(Permiso::getCodigo).sorted().toList()
                : rol.getPermisos().stream().map(Permiso::getCodigo).sorted().toList();
        return new RolResponse(rol.getId(), rol.getNombre(), rol.getDescripcion(), codigos, !esSuperAdmin);
    }
}
