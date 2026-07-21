package com.feelingpilates.usuarios.servicio;

import com.feelingpilates.exception.ResourceNotFoundException;
import com.feelingpilates.exception.ValidacionException;
import com.feelingpilates.ubicaciones.entidad.Salon;
import com.feelingpilates.ubicaciones.repositorio.SalonRepository;
import com.feelingpilates.usuarios.dto.ActualizarPerfilRequest;
import com.feelingpilates.usuarios.dto.RolConteoResponse;
import com.feelingpilates.usuarios.dto.UsuarioResponse;
import com.feelingpilates.usuarios.entidad.Rol;
import com.feelingpilates.usuarios.entidad.Usuario;
import com.feelingpilates.usuarios.entidad.UsuarioRol;
import com.feelingpilates.usuarios.repositorio.UsuarioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final SalonRepository salonRepository;

    public UsuarioService(UsuarioRepository usuarioRepository, SalonRepository salonRepository) {
        this.usuarioRepository = usuarioRepository;
        this.salonRepository = salonRepository;
    }

    @Transactional(readOnly = true)
    public UsuarioResponse obtenerPorId(UUID id) {
        return UsuarioResponse.desde(buscar(id));
    }

    @Transactional
    public UsuarioResponse actualizarPerfil(UUID id, ActualizarPerfilRequest request) {
        Usuario usuario = buscar(id);
        usuario.setNombre(request.nombre());
        usuario.setTelefono(request.telefono());
        usuario.setFotoUrl(request.fotoUrl());
        usuario.setDescripcion(request.descripcion());
        return UsuarioResponse.desde(usuario);
    }

    @Transactional(readOnly = true)
    public Page<UsuarioResponse> listar(Pageable pageable, String rol, String busqueda) {
        String rolFiltro = (rol == null || rol.isBlank()) ? null : rol;
        String busquedaFiltro = (busqueda == null || busqueda.isBlank()) ? null : busqueda.trim();
        return usuarioRepository.buscar(rolFiltro, busquedaFiltro, pageable).map(UsuarioResponse::desde);
    }

    @Transactional(readOnly = true)
    public List<RolConteoResponse> contarPorRol() {
        return usuarioRepository.contarPorRol().stream()
                .map(p -> new RolConteoResponse(p.getRol(), p.getTotal()))
                .toList();
    }

    @Transactional
    public UsuarioResponse cambiarEstatus(UUID id, Usuario.EstatusUsuario estatus) {
        Usuario usuario = buscar(id);
        usuario.setEstatus(estatus);
        return UsuarioResponse.desde(usuario);
    }

    /**
     * Reemplaza las sedes asignadas a un rol concreto del usuario (ADMIN/SUPER_ADMIN no aplican,
     * son roles globales). El usuario debe tener ya ese rol asignado.
     */
    @Transactional
    public UsuarioResponse actualizarSedesRol(UUID id, String nombreRol, List<UUID> salonIds) {
        Usuario usuario = buscar(id);
        List<UUID> sedes = salonIds == null ? List.of() : salonIds;
        SedeRolValidador.validar(nombreRol, sedes);

        Rol rol = usuario.getRoles().stream()
                .map(UsuarioRol::getRol)
                .filter(r -> r.getNombre().equals(nombreRol))
                .findFirst()
                .orElseThrow(() -> new ValidacionException("El usuario no tiene el rol " + nombreRol));

        usuario.getRoles().removeIf(ur -> ur.getRol().getNombre().equals(nombreRol));

        if (sedes.isEmpty()) {
            usuario.getRoles().add(new UsuarioRol(usuario, rol));
        } else {
            List<Salon> salones = salonRepository.findAllById(sedes);
            if (salones.size() != sedes.size()) {
                throw new ValidacionException("Una o más sedes seleccionadas no existen");
            }
            for (Salon salon : salones) {
                usuario.getRoles().add(new UsuarioRol(usuario, rol, salon));
            }
        }
        return UsuarioResponse.desde(usuario);
    }

    private Usuario buscar(UUID id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + id));
    }
}
