package com.feelingpilates.auth;

import com.feelingpilates.auth.dto.LoginRequest;
import com.feelingpilates.auth.dto.RegistroRequest;
import com.feelingpilates.auth.dto.TokenResponse;
import com.feelingpilates.exception.ConflictException;
import com.feelingpilates.seguridad.JwtService;
import com.feelingpilates.usuarios.entidad.Rol;
import com.feelingpilates.usuarios.entidad.Usuario;
import com.feelingpilates.usuarios.entidad.UsuarioRol;
import com.feelingpilates.usuarios.repositorio.RolRepository;
import com.feelingpilates.usuarios.repositorio.UsuarioRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UsuarioRepository usuarioRepository, RolRepository rolRepository,
                       PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public TokenResponse registrar(RegistroRequest request) {
        if (usuarioRepository.existsByCorreo(request.correo())) {
            throw new ConflictException("El correo ya está registrado");
        }
        Usuario usuario = new Usuario();
        usuario.setCorreo(request.correo());
        usuario.setContrasenaHash(passwordEncoder.encode(request.contrasena()));
        usuario.setProveedorAuth(Usuario.ProveedorAuth.local);
        usuario.setNombre(request.nombre());

        Rol rolCliente = rolRepository.findByNombre(Rol.CLIENTE)
                .orElseThrow(() -> new IllegalStateException("El rol CLIENTE no existe; revisa las migraciones"));
        usuario.getRoles().add(new UsuarioRol(usuario, rolCliente));

        usuarioRepository.save(usuario);
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

    private TokenResponse generarRespuesta(Usuario usuario) {
        List<String> roles = usuario.getRoles().stream()
                .map(ur -> ur.getRol().getNombre())
                .distinct()
                .toList();
        List<String> permisos = usuario.getRoles().stream()
                .flatMap(ur -> ur.getRol().getPermisos().stream())
                .map(p -> p.getCodigo())
                .distinct()
                .toList();
        return TokenResponse.bearer(jwtService.generarToken(usuario, roles, permisos));
    }
}
