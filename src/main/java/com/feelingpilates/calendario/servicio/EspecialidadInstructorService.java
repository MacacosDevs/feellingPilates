package com.feelingpilates.calendario.servicio;

import com.feelingpilates.calendario.dto.EspecialidadResponse;
import com.feelingpilates.exception.ResourceNotFoundException;
import com.feelingpilates.exception.ValidacionException;
import com.feelingpilates.ubicaciones.entidad.TipoActividad;
import com.feelingpilates.ubicaciones.repositorio.TipoActividadRepository;
import com.feelingpilates.usuarios.entidad.Usuario;
import com.feelingpilates.usuarios.repositorio.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;

/** Que actividades esta capacitado a impartir cada instructor. */
@Service
@Transactional
public class EspecialidadInstructorService {

    private final UsuarioRepository usuarioRepository;
    private final TipoActividadRepository tipoActividadRepository;

    public EspecialidadInstructorService(UsuarioRepository usuarioRepository, TipoActividadRepository tipoActividadRepository) {
        this.usuarioRepository = usuarioRepository;
        this.tipoActividadRepository = tipoActividadRepository;
    }

    @Transactional(readOnly = true)
    public List<EspecialidadResponse> listar(UUID usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        return usuario.getEspecialidades().stream()
                .map(t -> new EspecialidadResponse(t.getId(), t.getNombre(), t.getDuracionMinutos()))
                .sorted((a, b) -> a.nombre().compareTo(b.nombre()))
                .toList();
    }

    public List<EspecialidadResponse> actualizar(UUID usuarioId, List<UUID> tipoActividadIds) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        List<UUID> ids = tipoActividadIds == null ? List.of() : tipoActividadIds;
        List<TipoActividad> tipos = tipoActividadRepository.findAllById(ids);
        if (tipos.size() != ids.size()) {
            throw new ValidacionException("Alguno de los tipos de actividad no existe");
        }

        usuario.setEspecialidades(new HashSet<>(tipos));
        usuarioRepository.save(usuario);
        return listar(usuarioId);
    }
}
