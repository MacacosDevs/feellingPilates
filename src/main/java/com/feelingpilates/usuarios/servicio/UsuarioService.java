package com.feelingpilates.usuarios.servicio;

import com.feelingpilates.exception.ResourceNotFoundException;
import com.feelingpilates.usuarios.dto.ActualizarPerfilRequest;
import com.feelingpilates.usuarios.dto.UsuarioResponse;
import com.feelingpilates.usuarios.entidad.Usuario;
import com.feelingpilates.usuarios.repositorio.UsuarioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
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
    public Page<UsuarioResponse> listar(Pageable pageable) {
        return usuarioRepository.findAll(pageable).map(UsuarioResponse::desde);
    }

    @Transactional
    public UsuarioResponse cambiarEstatus(UUID id, Usuario.EstatusUsuario estatus) {
        Usuario usuario = buscar(id);
        usuario.setEstatus(estatus);
        return UsuarioResponse.desde(usuario);
    }

    private Usuario buscar(UUID id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + id));
    }
}
