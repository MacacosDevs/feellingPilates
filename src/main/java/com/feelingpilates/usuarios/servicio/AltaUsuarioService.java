package com.feelingpilates.usuarios.servicio;

import com.feelingpilates.exception.ConflictException;
import com.feelingpilates.exception.ValidacionException;
import com.feelingpilates.notificaciones.EmailService;
import com.feelingpilates.ubicaciones.entidad.Salon;
import com.feelingpilates.ubicaciones.repositorio.SalonRepository;
import com.feelingpilates.usuarios.dto.AltaPersonalResponse;
import com.feelingpilates.usuarios.dto.CrearClienteRequest;
import com.feelingpilates.usuarios.dto.CrearPersonalRequest;
import com.feelingpilates.usuarios.dto.UsuarioResponse;
import com.feelingpilates.usuarios.entidad.InvitacionUsuario;
import com.feelingpilates.usuarios.entidad.Rol;
import com.feelingpilates.usuarios.entidad.Usuario;
import com.feelingpilates.usuarios.entidad.UsuarioRol;
import com.feelingpilates.usuarios.repositorio.InvitacionUsuarioRepository;
import com.feelingpilates.usuarios.repositorio.RolRepository;
import com.feelingpilates.usuarios.repositorio.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class AltaUsuarioService {

    private static final String ALFABETO_CONTRASENA_TEMPORAL =
            "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789";
    private static final SecureRandom ALEATORIO = new SecureRandom();

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final SalonRepository salonRepository;
    private final InvitacionUsuarioRepository invitacionRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final String frontendUrl;
    private final long invitacionExpiracionHoras;

    public AltaUsuarioService(
            UsuarioRepository usuarioRepository,
            RolRepository rolRepository,
            SalonRepository salonRepository,
            InvitacionUsuarioRepository invitacionRepository,
            PasswordEncoder passwordEncoder,
            EmailService emailService,
            @Value("${app.frontend.url}") String frontendUrl,
            @Value("${app.invitacion.expiracion-horas:168}") long invitacionExpiracionHoras) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.salonRepository = salonRepository;
        this.invitacionRepository = invitacionRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.frontendUrl = frontendUrl;
        this.invitacionExpiracionHoras = invitacionExpiracionHoras;
    }

    @Transactional
    public UsuarioResponse crearCliente(CrearClienteRequest request) {
        // El cliente puede ir a cualquier sede: salonIds es opcional, puede venir o no.
        Usuario usuario = crearUsuarioBase(
                request.correo(), request.nombre(), request.telefono(), Rol.CLIENTE, request.salonIds(),
                Usuario.ProveedorAuth.local);
        usuarioRepository.save(usuario);

        String token = generarToken();
        InvitacionUsuario invitacion = new InvitacionUsuario(
                usuario, token, OffsetDateTime.now().plusHours(invitacionExpiracionHoras));
        invitacionRepository.save(invitacion);

        String enlace = frontendUrl + "/invitacion/" + token;
        emailService.enviarInvitacionCliente(usuario.getCorreo(), usuario.getNombre(), enlace);

        return UsuarioResponse.desde(usuario);
    }

    @Transactional
    public Usuario crearClienteAutoRegistro(String correo, String nombre, String contrasenaPlano) {
        Usuario usuario = crearUsuarioBase(correo, nombre, null, Rol.CLIENTE, null, Usuario.ProveedorAuth.local);
        usuario.setContrasenaHash(passwordEncoder.encode(contrasenaPlano));
        usuarioRepository.save(usuario);
        return usuario;
    }

    @Transactional
    public Usuario crearClienteGoogle(String correo, String nombre, String fotoUrl) {
        // Sin contraseña: esta cuenta solo puede iniciar sesión con Google.
        Usuario usuario = crearUsuarioBase(correo, nombre, null, Rol.CLIENTE, null, Usuario.ProveedorAuth.google);
        if (fotoUrl != null && !fotoUrl.isBlank()) {
            // Es una URL externa (googleusercontent.com), no una foto subida a
            // nuestro backend: fotoUrl acepta ambas, el cliente ya resuelve URLs
            // absolutas tal cual (ver resolveMediaUrl en la app móvil).
            usuario.setFotoUrl(fotoUrl);
        }
        usuarioRepository.save(usuario);
        return usuario;
    }

    @Transactional
    public AltaPersonalResponse crearPersonal(CrearPersonalRequest request) {
        Usuario usuario = crearUsuarioBase(
                request.correo(), request.nombre(), request.telefono(), request.rol(), request.salonIds(),
                Usuario.ProveedorAuth.local);
        String contrasenaTemporal = generarContrasenaTemporal();
        usuario.setContrasenaHash(passwordEncoder.encode(contrasenaTemporal));
        usuarioRepository.save(usuario);

        return new AltaPersonalResponse(UsuarioResponse.desde(usuario), contrasenaTemporal);
    }

    private Usuario crearUsuarioBase(
            String correo, String nombre, String telefono, String nombreRol, List<UUID> salonIds,
            Usuario.ProveedorAuth proveedorAuth) {
        if (usuarioRepository.existsByCorreo(correo)) {
            throw new ConflictException("El correo ya está registrado");
        }
        List<UUID> sedes = salonIds == null ? List.of() : salonIds;
        SedeRolValidador.validar(nombreRol, sedes);

        Usuario usuario = new Usuario();
        usuario.setCorreo(correo);
        usuario.setNombre(nombre);
        usuario.setTelefono(telefono);
        usuario.setProveedorAuth(proveedorAuth);

        Rol rol = rolRepository.findByNombre(nombreRol)
                .orElseThrow(() -> new IllegalStateException("El rol " + nombreRol + " no existe; revisa las migraciones"));

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
        return usuario;
    }

    private String generarToken() {
        byte[] bytes = new byte[32];
        ALEATORIO.nextBytes(bytes);
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private String generarContrasenaTemporal() {
        StringBuilder sb = new StringBuilder(12);
        for (int i = 0; i < 12; i++) {
            sb.append(ALFABETO_CONTRASENA_TEMPORAL.charAt(ALEATORIO.nextInt(ALFABETO_CONTRASENA_TEMPORAL.length())));
        }
        return sb.toString();
    }
}
