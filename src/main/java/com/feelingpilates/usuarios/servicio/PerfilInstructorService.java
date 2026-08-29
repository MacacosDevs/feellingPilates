package com.feelingpilates.usuarios.servicio;

import com.feelingpilates.exception.ResourceNotFoundException;
import com.feelingpilates.usuarios.dto.ActualizarPerfilInstructorRequest;
import com.feelingpilates.usuarios.dto.PerfilInstructorResponse;
import com.feelingpilates.usuarios.entidad.PerfilInstructor;
import com.feelingpilates.usuarios.entidad.Usuario;
import com.feelingpilates.usuarios.repositorio.PerfilInstructorRepository;
import com.feelingpilates.usuarios.repositorio.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class PerfilInstructorService {

    private final PerfilInstructorRepository perfilInstructorRepository;
    private final UsuarioRepository usuarioRepository;

    public PerfilInstructorService(PerfilInstructorRepository perfilInstructorRepository,
                                    UsuarioRepository usuarioRepository) {
        this.perfilInstructorRepository = perfilInstructorRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public PerfilInstructorResponse obtenerPorUsuarioId(UUID usuarioId) {
        Usuario usuario = buscarUsuario(usuarioId);
        PerfilInstructor perfil = perfilInstructorRepository.findByUsuarioId(usuarioId).orElse(null);
        return PerfilInstructorResponse.desde(usuario, perfil);
    }

    @Transactional
    public PerfilInstructorResponse actualizarMiPerfil(UUID usuarioId, ActualizarPerfilInstructorRequest request) {
        Usuario usuario = buscarUsuario(usuarioId);
        PerfilInstructor perfil = perfilInstructorRepository.findByUsuarioId(usuarioId)
                .orElseGet(() -> {
                    PerfilInstructor nuevo = new PerfilInstructor();
                    nuevo.setUsuario(usuario);
                    return nuevo;
                });
        perfil.setSobreSuClase(request.sobreSuClase());
        perfil.setInstagramUrl(request.instagramUrl());
        perfil.setFacebookUrl(request.facebookUrl());
        perfil.setTiktokUrl(request.tiktokUrl());
        perfil.setWhatsappUrl(request.whatsappUrl());
        perfilInstructorRepository.save(perfil);
        return PerfilInstructorResponse.desde(usuario, perfil);
    }

    private Usuario buscarUsuario(UUID usuarioId) {
        return usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + usuarioId));
    }
}
