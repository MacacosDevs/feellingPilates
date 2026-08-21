package com.feelingpilates.seguridad;

import com.feelingpilates.calendario.controlador.ReservaController;
import com.feelingpilates.calendario.controlador.TurnoInstructorController;
import com.feelingpilates.calendario.entidad.Reserva;
import com.feelingpilates.calendario.entidad.TurnoInstructor;
import com.feelingpilates.calendario.repositorio.ReservaRepository;
import com.feelingpilates.calendario.repositorio.TurnoInstructorAsignacionRepository;
import com.feelingpilates.calendario.repositorio.TurnoInstructorRepository;
import com.feelingpilates.calendario.servicio.ReservaService;
import com.feelingpilates.calendario.servicio.TurnoInstructorService;
import com.feelingpilates.ubicaciones.controlador.SalonHorarioExcepcionController;
import com.feelingpilates.ubicaciones.entidad.Salon;
import com.feelingpilates.ubicaciones.entidad.SalonHorarioExcepcion;
import com.feelingpilates.ubicaciones.repositorio.HorarioOperacionRepository;
import com.feelingpilates.ubicaciones.repositorio.SalonHorarioExcepcionRepository;
import com.feelingpilates.ubicaciones.repositorio.SalonRepository;
import com.feelingpilates.ubicaciones.repositorio.TipoActividadRepository;
import com.feelingpilates.ubicaciones.servicio.SalonHorarioExcepcionService;
import com.feelingpilates.usuarios.entidad.Permiso;
import com.feelingpilates.usuarios.entidad.Rol;
import com.feelingpilates.usuarios.entidad.Usuario;
import com.feelingpilates.usuarios.entidad.UsuarioRol;
import com.feelingpilates.usuarios.repositorio.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({
        SalonHorarioExcepcionController.class,
        TurnoInstructorController.class,
        ReservaController.class
})
@Import({
        SecurityConfig.class,
        AutorizadorSalon.class,
        SalonHorarioExcepcionService.class,
        TurnoInstructorService.class,
        ReservaService.class,
        AutorizacionContextualControllerTest.ConfiguracionTest.class
})
class AutorizacionContextualControllerTest {

    private static final UUID ACTOR_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private static final UUID SALON_A_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID SALON_B_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID RECURSO_ID = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DatosTest datos;

    @BeforeEach
    void prepararActor() {
        datos.limpiar();
        datos.usuarios.put(ACTOR_ID, actorLocal());
    }

    @Test
    void noEliminaExcepcionDeOtroSalonConUuidValido() throws Exception {
        SalonHorarioExcepcion excepcion = new SalonHorarioExcepcion();
        excepcion.setId(RECURSO_ID);
        excepcion.setSalon(salon(SALON_B_ID));
        datos.excepciones.put(RECURSO_ID, excepcion);

        mockMvc.perform(delete("/api/salones/{salonId}/excepciones-horario/{id}", SALON_B_ID, RECURSO_ID)
                        .with(actorAutenticado()))
                .andExpect(status().isForbidden());

        org.assertj.core.api.Assertions.assertThat(datos.excepcionesGuardadas).isZero();
    }

    @Test
    void noEliminaTurnoDeOtroSalonConUuidValido() throws Exception {
        TurnoInstructor turno = new TurnoInstructor();
        turno.setId(RECURSO_ID);
        turno.setSalon(salon(SALON_B_ID));
        datos.turnos.put(RECURSO_ID, turno);

        mockMvc.perform(delete("/api/turnos-instructor/{id}", RECURSO_ID).with(actorAutenticado()))
                .andExpect(status().isForbidden());

        org.assertj.core.api.Assertions.assertThat(datos.turnosGuardados).isZero();
    }

    @Test
    void noCancelaReservaDeOtroSalonConUuidValido() throws Exception {
        Reserva reserva = new Reserva();
        reserva.setId(RECURSO_ID);
        reserva.setSalon(salon(SALON_B_ID));
        datos.reservas.put(RECURSO_ID, reserva);

        mockMvc.perform(delete("/api/reservas/{id}", RECURSO_ID).with(actorAutenticado()))
                .andExpect(status().isForbidden());

        org.assertj.core.api.Assertions.assertThat(datos.reservasGuardadas).isZero();
    }

    private org.springframework.test.web.servlet.request.RequestPostProcessor actorAutenticado() {
        UsuarioAutenticado principal = new UsuarioAutenticado(ACTOR_ID, "actor@test.com");
        List<SimpleGrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("salon.administrar"),
                new SimpleGrantedAuthority("calendario.gestionar"),
                new SimpleGrantedAuthority("reserva.administrar"));
        return authentication(UsernamePasswordAuthenticationToken.authenticated(principal, null, authorities));
    }

    private Usuario actorLocal() {
        Usuario usuario = new Usuario();
        usuario.setId(ACTOR_ID);
        usuario.setEstatus(Usuario.EstatusUsuario.activo);

        Rol rol = new Rol();
        rol.setNombre(Rol.PERSONAL);
        rol.getPermisos().add(permiso("salon.administrar"));
        rol.getPermisos().add(permiso("calendario.gestionar"));
        rol.getPermisos().add(permiso("reserva.administrar"));
        usuario.getRoles().add(new UsuarioRol(usuario, rol, salon(SALON_A_ID)));
        return usuario;
    }

    private Permiso permiso(String codigo) {
        Permiso permiso = new Permiso();
        permiso.setCodigo(codigo);
        return permiso;
    }

    private Salon salon(UUID id) {
        Salon salon = new Salon();
        salon.setId(id);
        return salon;
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class ConfiguracionTest {

        @Bean
        DatosTest datosTest() {
            return new DatosTest();
        }

        @Bean
        UsuarioRepository usuarioRepository(DatosTest datos) {
            return repositorio(UsuarioRepository.class, datos, "usuarios");
        }

        @Bean
        SalonHorarioExcepcionRepository salonHorarioExcepcionRepository(DatosTest datos) {
            return repositorio(SalonHorarioExcepcionRepository.class, datos, "excepciones");
        }

        @Bean
        TurnoInstructorRepository turnoInstructorRepository(DatosTest datos) {
            return repositorio(TurnoInstructorRepository.class, datos, "turnos");
        }

        @Bean
        ReservaRepository reservaRepository(DatosTest datos) {
            return repositorio(ReservaRepository.class, datos, "reservas");
        }

        @Bean
        SalonRepository salonRepository(DatosTest datos) {
            return repositorio(SalonRepository.class, datos, "salones");
        }

        @Bean
        TurnoInstructorAsignacionRepository turnoInstructorAsignacionRepository(DatosTest datos) {
            return repositorio(TurnoInstructorAsignacionRepository.class, datos, "otros");
        }

        @Bean
        HorarioOperacionRepository horarioOperacionRepository(DatosTest datos) {
            return repositorio(HorarioOperacionRepository.class, datos, "otros");
        }

        @Bean
        TipoActividadRepository tipoActividadRepository(DatosTest datos) {
            return repositorio(TipoActividadRepository.class, datos, "otros");
        }

        @Bean
        JwtAuthFilter jwtAuthFilter() {
            return new JwtAuthFilter(null, null);
        }

        @SuppressWarnings("unchecked")
        private static <T> T repositorio(Class<T> tipo, DatosTest datos, String coleccion) {
            return (T) Proxy.newProxyInstance(tipo.getClassLoader(), new Class<?>[]{tipo}, (proxy, method, args) -> {
                if (method.getName().equals("findById")) {
                    return Optional.ofNullable(datos.mapa(coleccion).get(args[0]));
                }
                if (method.getName().equals("save")) {
                    datos.registrarGuardado(coleccion);
                    return args[0];
                }
                if (method.getName().equals("findAll") || List.class.isAssignableFrom(method.getReturnType())) {
                    return List.of();
                }
                return valorPorDefecto(method.getReturnType());
            });
        }

        private static Object valorPorDefecto(Class<?> tipo) {
            if (!tipo.isPrimitive()) return null;
            if (tipo == boolean.class) return false;
            if (tipo == char.class) return '\0';
            return 0;
        }
    }

    static class DatosTest {
        final Map<UUID, Object> usuarios = new HashMap<>();
        final Map<UUID, Object> excepciones = new HashMap<>();
        final Map<UUID, Object> turnos = new HashMap<>();
        final Map<UUID, Object> reservas = new HashMap<>();
        final Map<UUID, Object> salones = new HashMap<>();
        final Map<UUID, Object> otros = new HashMap<>();
        int excepcionesGuardadas;
        int turnosGuardados;
        int reservasGuardadas;

        void limpiar() {
            usuarios.clear();
            excepciones.clear();
            turnos.clear();
            reservas.clear();
            salones.clear();
            otros.clear();
            excepcionesGuardadas = 0;
            turnosGuardados = 0;
            reservasGuardadas = 0;
        }

        Map<UUID, Object> mapa(String nombre) {
            return switch (nombre) {
                case "usuarios" -> usuarios;
                case "excepciones" -> excepciones;
                case "turnos" -> turnos;
                case "reservas" -> reservas;
                case "salones" -> salones;
                default -> otros;
            };
        }

        void registrarGuardado(String nombre) {
            switch (nombre) {
                case "excepciones" -> excepcionesGuardadas++;
                case "turnos" -> turnosGuardados++;
                case "reservas" -> reservasGuardadas++;
                default -> { }
            }
        }
    }
}
