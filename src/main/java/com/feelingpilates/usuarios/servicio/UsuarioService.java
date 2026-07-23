package com.feelingpilates.usuarios.servicio;

import com.feelingpilates.exception.ResourceNotFoundException;
import com.feelingpilates.exception.ValidacionException;
import com.feelingpilates.ubicaciones.entidad.Salon;
import com.feelingpilates.ubicaciones.repositorio.SalonRepository;
import com.feelingpilates.usuarios.dto.ActualizarPerfilRequest;
import com.feelingpilates.usuarios.dto.FotoUsuario;
import com.feelingpilates.usuarios.dto.RolConteoResponse;
import com.feelingpilates.usuarios.dto.UsuarioResponse;
import com.feelingpilates.usuarios.entidad.Rol;
import com.feelingpilates.usuarios.entidad.Usuario;
import com.feelingpilates.usuarios.entidad.UsuarioRol;
import com.feelingpilates.usuarios.repositorio.UsuarioRepository;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
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

    @Transactional
    public UsuarioResponse actualizarFoto(UUID id, MultipartFile archivo) {
        if (archivo.isEmpty() || archivo.getContentType() == null || !archivo.getContentType().startsWith("image/")) {
            throw new ValidacionException("El archivo debe ser una imagen");
        }
        Usuario usuario = buscar(id);
        try {
            ByteArrayOutputStream salida = new ByteArrayOutputStream();
            Thumbnails.of(archivo.getInputStream())
                    .size(512, 512)
                    .outputFormat("jpg")
                    .outputQuality(0.85)
                    .toOutputStream(salida);
            usuario.setFotoDatos(salida.toByteArray());
            usuario.setFotoTipo("image/jpeg");
            usuario.setFotoUrl("/api/usuarios/" + id + "/foto");
        } catch (IOException e) {
            throw new ValidacionException("No se pudo procesar la imagen");
        }
        return UsuarioResponse.desde(usuario);
    }

    @Transactional(readOnly = true)
    public FotoUsuario obtenerFoto(UUID id) {
        Usuario usuario = buscar(id);
        if (usuario.getFotoDatos() == null) {
            throw new ResourceNotFoundException("El usuario no tiene foto de perfil");
        }
        return new FotoUsuario(usuario.getFotoDatos(), usuario.getFotoTipo());
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
     * Reemplaza las sedes asignadas a un rol concreto del usuario. Solo aplica a PERSONAL e
     * INSTRUCTOR: ADMIN/SUPER_ADMIN son roles globales y CLIENTE puede ir a cualquier sede,
     * así que a ninguno de los dos se le puede asignar una sede desde aquí.
     */
    @Transactional
    public UsuarioResponse actualizarSedesRol(UUID id, String nombreRol, List<UUID> salonIds) {
        if (nombreRol.equals(Rol.CLIENTE)) {
            throw new ValidacionException("Los clientes pueden ir a cualquier sede; no se les asigna una en particular");
        }
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
