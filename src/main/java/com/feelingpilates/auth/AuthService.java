package com.feelingpilates.auth;

import com.feelingpilates.auth.dto.CompletarInvitacionRequest;
import com.feelingpilates.auth.dto.InvitacionInfoResponse;
import com.feelingpilates.auth.dto.LoginRequest;
import com.feelingpilates.auth.dto.TokenResponse;
import com.feelingpilates.exception.ConflictException;
import com.feelingpilates.exception.ResourceNotFoundException;
import com.feelingpilates.seguridad.JwtService;
import com.feelingpilates.usuarios.entidad.InvitacionUsuario;
import com.feelingpilates.usuarios.entidad.Permiso;
import com.feelingpilates.usuarios.entidad.Rol;
import com.feelingpilates.usuarios.entidad.Usuario;
import com.feelingpilates.usuarios.repositorio.InvitacionUsuarioRepository;
import com.feelingpilates.usuarios.repositorio.PermisoRepository;
import com.feelingpilates.usuarios.repositorio.UsuarioRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final InvitacionUsuarioRepository invitacionRepository;
    private final PermisoRepository permisoRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UsuarioRepository usuarioRepository, InvitacionUsuarioRepository invitacionRepository,
                       PermisoRepository permisoRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.invitacionRepository = invitacionRepository;
        this.permisoRepository = permisoRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByCorreo(request.correo())
                .orElseThrow(() -> new BadCredentialsException("Credenciales inválidas"));
        if (usuario.getEstatus() != Usuario.EstatusUsuario.activo
                || usuario.getContrasenaHash() == null
                || !passwordEncoder.matches(request.contrasena(), usuario.getContrasenaHash())) {
            throw new BadCredentialsException("Credenciales inválidas");
        }
        return generarRespuesta(usuario);
    }

    @Transactional(readOnly = true)
    public InvitacionInfoResponse obtenerInvitacion(String token) {
        InvitacionUsuario invitacion = buscarInvitacionVigente(token);
        Usuario usuario = invitacion.getUsuario();
        return new InvitacionInfoResponse(usuario.getCorreo(), usuario.getNombre());
    }

    @Transactional
    public TokenResponse completarInvitacion(CompletarInvitacionRequest request) {
        InvitacionUsuario invitacion = buscarInvitacionVigente(request.token());
        Usuario usuario = invitacion.getUsuario();
        usuario.setContrasenaHash(passwordEncoder.encode(request.contrasena()));
        invitacion.setUsadoEn(OffsetDateTime.now());
        return generarRespuesta(usuario);
    }

    private InvitacionUsuario buscarInvitacionVigente(String token) {
        InvitacionUsuario invitacion = invitacionRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Invitación no encontrada"));
        if (!invitacion.estaVigente()) {
            throw new ConflictException("La invitación ya expiró o ya fue utilizada");
        }
        return invitacion;
    }

    private TokenResponse generarRespuesta(Usuario usuario) {
        List<String> roles = usuario.getRoles().stream()
                .map(ur -> ur.getRol().getNombre())
                .distinct()
                .toList();
        // SUPER_ADMIN siempre tiene todos los permisos, incluidos los que se agreguen
        // despues; no depende de rol_permiso para no requerir una migracion por permiso nuevo.
        List<String> permisos = roles.contains(Rol.SUPER_ADMIN)
                ? permisoRepository.findAll().stream().map(Permiso::getCodigo).distinct().toList()
                : usuario.getRoles().stream()
                        .flatMap(ur -> ur.getRol().getPermisos().stream())
                        .map(Permiso::getCodigo)
                        .distinct()
                        .toList();
        return TokenResponse.bearer(jwtService.generarToken(usuario, roles, permisos));
    }
}
