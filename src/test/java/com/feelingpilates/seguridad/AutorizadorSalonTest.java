package com.feelingpilates.seguridad;

import com.feelingpilates.ubicaciones.entidad.Salon;
import com.feelingpilates.usuarios.entidad.Permiso;
import com.feelingpilates.usuarios.entidad.Rol;
import com.feelingpilates.usuarios.entidad.Usuario;
import com.feelingpilates.usuarios.entidad.UsuarioRol;
import com.feelingpilates.usuarios.repositorio.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import java.lang.reflect.Proxy;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AutorizadorSalonTest {

    private static final String PERMISO = "calendario.gestionar";
    private static final UUID ACTOR_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private static final UUID SALON_A_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID SALON_B_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");

    private UsuarioRepository usuarioRepository;
    private Optional<Usuario> usuarioEncontrado;

    private AutorizadorSalon autorizador;

    @BeforeEach
    void preparar() {
        usuarioEncontrado = Optional.empty();
        usuarioRepository = (UsuarioRepository) Proxy.newProxyInstance(
                UsuarioRepository.class.getClassLoader(),
                new Class<?>[]{UsuarioRepository.class},
                (proxy, method, args) -> method.getName().equals("findById")
                        ? usuarioEncontrado
                        : valorPorDefecto(method.getReturnType()));
        autorizador = new AutorizadorSalon(usuarioRepository);
    }

    @Test
    void permiteUsuarioConPermisoYScopeDelSalon() {
        usuarioEncontrado = Optional.of(usuarioLocal(PERMISO, SALON_A_ID));

        autorizador.verificarAccesoSalon(ACTOR_ID, PERMISO, SALON_A_ID);
    }

    @Test
    void deniegaMismoPermisoEnOtroSalon() {
        usuarioEncontrado = Optional.of(usuarioLocal(PERMISO, SALON_A_ID));

        assertThatThrownBy(() -> autorizador.verificarAccesoSalon(ACTOR_ID, PERMISO, SALON_B_ID))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("salon objetivo");
    }

    @Test
    void deniegaSalonCorrectoSinPermiso() {
        usuarioEncontrado = Optional.of(usuarioLocal("calendario.leer", SALON_A_ID));

        assertThatThrownBy(() -> autorizador.verificarAccesoSalon(ACTOR_ID, PERMISO, SALON_A_ID))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("permiso requerido");
    }

    @Test
    void permiteAdminGlobalEnDosSalones() {
        Usuario admin = usuarioConAsignacion(Rol.ADMIN, PERMISO, null);
        usuarioEncontrado = Optional.of(admin);

        autorizador.verificarAccesoSalon(ACTOR_ID, PERMISO, SALON_A_ID);
        autorizador.verificarAccesoSalon(ACTOR_ID, PERMISO, SALON_B_ID);
    }

    @Test
    void deniegaUsuarioInactivo() {
        Usuario usuario = usuarioLocal(PERMISO, SALON_A_ID);
        usuario.setEstatus(Usuario.EstatusUsuario.suspendido);
        usuarioEncontrado = Optional.of(usuario);

        assertThatThrownBy(() -> autorizador.verificarAccesoSalon(ACTOR_ID, PERMISO, SALON_A_ID))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("no esta activo");
    }

    @Test
    void deniegaUsuarioQueYaNoExiste() {
        usuarioEncontrado = Optional.empty();

        assertThatThrownBy(() -> autorizador.verificarAccesoSalon(ACTOR_ID, PERMISO, SALON_A_ID))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("ya no existe");
    }

    private Usuario usuarioLocal(String permiso, UUID salonId) {
        return usuarioConAsignacion(Rol.PERSONAL, permiso, salonId);
    }

    private Usuario usuarioConAsignacion(String nombreRol, String codigoPermiso, UUID salonId) {
        Usuario usuario = new Usuario();
        usuario.setId(ACTOR_ID);
        usuario.setEstatus(Usuario.EstatusUsuario.activo);

        Permiso permiso = new Permiso();
        permiso.setCodigo(codigoPermiso);
        Rol rol = new Rol();
        rol.setNombre(nombreRol);
        rol.getPermisos().add(permiso);

        Salon salon = null;
        if (salonId != null) {
            salon = new Salon();
            salon.setId(salonId);
        }
        usuario.getRoles().add(new UsuarioRol(usuario, rol, salon));
        return usuario;
    }

    private Object valorPorDefecto(Class<?> tipo) {
        if (!tipo.isPrimitive()) return null;
        if (tipo == boolean.class) return false;
        if (tipo == char.class) return '\0';
        return 0;
    }
}
