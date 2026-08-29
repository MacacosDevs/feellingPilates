package com.feelingpilates.auth;

import com.feelingpilates.auth.dto.CompletarInvitacionRequest;
import com.feelingpilates.auth.dto.GoogleTokenRequest;
import com.feelingpilates.auth.dto.InvitacionInfoResponse;
import com.feelingpilates.auth.dto.LoginRequest;
import com.feelingpilates.auth.dto.RegistroRequest;
import com.feelingpilates.auth.dto.TokenResponse;
import com.feelingpilates.exception.ConflictException;
import com.feelingpilates.exception.ResourceNotFoundException;
import com.feelingpilates.seguridad.JwtService;
import com.feelingpilates.usuarios.entidad.InvitacionUsuario;
import com.feelingpilates.usuarios.entidad.Usuario;
import com.feelingpilates.usuarios.repositorio.InvitacionUsuarioRepository;
import com.feelingpilates.usuarios.repositorio.UsuarioRepository;
import com.feelingpilates.usuarios.servicio.AltaUsuarioService;
import com.feelingpilates.usuarios.servicio.PermisoResolver;
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
    private final PermisoResolver permisoResolver;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AltaUsuarioService altaUsuarioService;
    private final GoogleTokenVerifier googleTokenVerifier;

    public AuthService(UsuarioRepository usuarioRepository, InvitacionUsuarioRepository invitacionRepository,
                       PermisoResolver permisoResolver, PasswordEncoder passwordEncoder, JwtService jwtService,
                       AltaUsuarioService altaUsuarioService, GoogleTokenVerifier googleTokenVerifier) {
        this.usuarioRepository = usuarioRepository;
        this.invitacionRepository = invitacionRepository;
        this.permisoResolver = permisoResolver;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.altaUsuarioService = altaUsuarioService;
        this.googleTokenVerifier = googleTokenVerifier;
    }

    @Transactional
    public TokenResponse registrar(RegistroRequest request) {
        Usuario usuario = altaUsuarioService.crearClienteRegistro(
                request.correo(), request.nombre(), request.contrasena());
        return generarRespuesta(usuario);
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

    @Transactional
    public TokenResponse loginConGoogle(GoogleTokenRequest request) {
        GoogleTokenVerifier.GooglePerfil perfil = googleTokenVerifier.verificar(request.idToken())
                .orElseThrow(() -> new BadCredentialsException("Token de Google inválido"));

        Usuario usuario = usuarioRepository.findByCorreo(perfil.correo())
                .orElseGet(() -> altaUsuarioService.crearClienteGoogle(perfil.correo(), perfil.nombre(), perfil.fotoUrl()));

        // Completa la foto en cuentas que ya existían sin una (p. ej. creadas antes
        // de este cambio); no pisa una foto que el usuario ya haya subido.
        if ((usuario.getFotoUrl() == null || usuario.getFotoUrl().isBlank())
                && perfil.fotoUrl() != null && !perfil.fotoUrl().isBlank()) {
            usuario.setFotoUrl(perfil.fotoUrl());
        }

        if (usuario.getEstatus() != Usuario.EstatusUsuario.activo) {
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
        List<String> permisos = permisoResolver.resolver(usuario);
        return TokenResponse.bearer(jwtService.generarToken(usuario, roles, permisos));
    }
}
