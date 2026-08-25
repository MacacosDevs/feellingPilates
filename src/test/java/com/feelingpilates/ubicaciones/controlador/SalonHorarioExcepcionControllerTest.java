package com.feelingpilates.ubicaciones.controlador;

import com.feelingpilates.seguridad.AutorizadorSalon;
import com.feelingpilates.seguridad.JwtAuthFilter;
import com.feelingpilates.seguridad.SecurityConfig;
import com.feelingpilates.seguridad.UsuarioAutenticado;
import com.feelingpilates.ubicaciones.dominio.CambioExcepcionHorario;
import com.feelingpilates.ubicaciones.dominio.ConflictoProgramacionPuntual;
import com.feelingpilates.ubicaciones.dominio.ValidadorImpactoExcepcionHorario;
import com.feelingpilates.ubicaciones.entidad.Salon;
import com.feelingpilates.ubicaciones.entidad.SalonHorarioExcepcion;
import com.feelingpilates.ubicaciones.repositorio.SalonHorarioExcepcionRepository;
import com.feelingpilates.ubicaciones.servicio.HorarioOperacionResolver;
import com.feelingpilates.ubicaciones.servicio.SalonHorarioExcepcionService;
import com.feelingpilates.ubicaciones.servicio.SalonLock;
import com.feelingpilates.usuarios.entidad.Permiso;
import com.feelingpilates.usuarios.entidad.Rol;
import com.feelingpilates.usuarios.entidad.Usuario;
import com.feelingpilates.usuarios.entidad.UsuarioRol;
import com.feelingpilates.usuarios.repositorio.UsuarioRepository;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.sql.SQLException;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Controller de F2C.2: traduccion HTTP de los cuatro endpoints de escritura (legacy + por-fecha),
 * no-colision de mappings, orden de autorizacion del DELETE legacy y whitelist 409.
 *
 * <p>A diferencia de {@code SalonHorarioOperacionControllerTest}, aqui se importa el
 * {@link SalonHorarioExcepcionService} REAL (no un doble): la traduccion whitelist vive dentro del
 * propio service (no hay una capa de aplicacion separada), asi que probarla de verdad exige no
 * mockearlo. Se mockean sus colaboradores de mas bajo nivel.
 */
@WebMvcTest(SalonHorarioExcepcionController.class)
@Import({
        SecurityConfig.class,
        AutorizadorSalon.class,
        SalonHorarioExcepcionService.class,
        SalonHorarioExcepcionControllerTest.ConfiguracionTest.class
})
class SalonHorarioExcepcionControllerTest {

    private static final UUID ACTOR_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private static final UUID SALON_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID SALON_AJENO_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final LocalDate HOY = LocalDate.of(2026, 8, 20);
    private static final LocalDate MANANA = HOY.plusDays(1);
    private static final LocalDate AYER = HOY.minusDays(1);

    private static final String CERRADO_VALIDO = """
            {"cerrado": true, "horaApertura": null, "horaCierre": null}
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UsuarioRepository usuarioRepository;

    @MockitoBean
    private SalonHorarioExcepcionRepository excepcionRepository;

    @MockitoBean
    private SalonLock salonLock;

    @MockitoBean
    private HorarioOperacionResolver horarioOperacionResolver;

    @MockitoBean
    private ValidadorImpactoExcepcionHorario validador;

    private Salon salon;

    @BeforeEach
    void prepararActorConScopeYColaboradoresPermisivos() {
        when(usuarioRepository.findById(ACTOR_ID))
                .thenReturn(Optional.of(usuarioConScope(SALON_ID, "salon.administrar")));

        salon = new Salon();
        salon.setId(SALON_ID);
        when(salonLock.adquirir(SALON_ID)).thenReturn(salon);
        when(validador.evaluar(any())).thenReturn(List.of());
        when(horarioOperacionResolver.resolver(any(), any())).thenReturn(Optional.empty());
        when(excepcionRepository.findBySalonIdAndFechaAndActivoTrue(any(), any())).thenReturn(Optional.empty());
        when(excepcionRepository.saveAndFlush(any(SalonHorarioExcepcion.class))).thenAnswer(i -> {
            SalonHorarioExcepcion e = i.getArgument(0);
            if (e.getId() == null) {
                e.setId(UUID.randomUUID());
            }
            return e;
        });
    }

    // ---------- A1: los cuatro endpoints de escritura producen el mismo efecto ----------

    @Test
    void legacyPutConFechaEnElBodyCrea200() throws Exception {
        mockMvc.perform(put("/api/salones/{salonId}/excepciones-horario", SALON_ID)
                        .with(administrador())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fecha": "%s", "cerrado": true, "horaApertura": null, "horaCierre": null}
                                """.formatted(MANANA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fecha").value(MANANA.toString()))
                .andExpect(jsonPath("$.cerrado").value(true));
    }

    @Test
    void nuevoPutPorFechaConFechaEnElPathCrea200() throws Exception {
        mockMvc.perform(put("/api/salones/{salonId}/excepciones-horario/por-fecha/{fecha}", SALON_ID, MANANA)
                        .with(administrador())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CERRADO_VALIDO))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fecha").value(MANANA.toString()))
                .andExpect(jsonPath("$.cerrado").value(true));
    }

    @Test
    void legacyDeleteConUuidCancela() throws Exception {
        SalonHorarioExcepcion activa = excepcionActiva(MANANA);
        when(excepcionRepository.findById(activa.getId())).thenReturn(Optional.of(activa));

        mockMvc.perform(delete("/api/salones/{salonId}/excepciones-horario/{id}", SALON_ID, activa.getId())
                        .with(administrador()))
                .andExpect(status().isOk());

        verify(excepcionRepository).save(activa);
    }

    @Test
    void nuevoDeletePorFechaCancela() throws Exception {
        SalonHorarioExcepcion activa = excepcionActiva(MANANA);
        when(excepcionRepository.findBySalonIdAndFechaAndActivoTrue(SALON_ID, MANANA))
                .thenReturn(Optional.of(activa));

        mockMvc.perform(delete("/api/salones/{salonId}/excepciones-horario/por-fecha/{fecha}", SALON_ID, MANANA)
                        .with(administrador()))
                .andExpect(status().isOk());

        verify(excepcionRepository).save(activa);
    }

    // ---------- A2: los mappings no colisionan ----------

    @Test
    void deleteConUuidLlegaAlHandlerLegacyYConFechaAlDePorFecha() throws Exception {
        SalonHorarioExcepcion activa = excepcionActiva(MANANA);
        when(excepcionRepository.findById(activa.getId())).thenReturn(Optional.of(activa));
        when(excepcionRepository.findBySalonIdAndFechaAndActivoTrue(SALON_ID, MANANA))
                .thenReturn(Optional.of(activa));

        mockMvc.perform(delete("/api/salones/{salonId}/excepciones-horario/{id}", SALON_ID, activa.getId())
                        .with(administrador()))
                .andExpect(status().isOk());
        verify(excepcionRepository).findById(activa.getId());
        verify(excepcionRepository, never()).findBySalonIdAndFechaAndActivoTrue(any(), any());

        mockMvc.perform(delete("/api/salones/{salonId}/excepciones-horario/por-fecha/{fecha}", SALON_ID, MANANA)
                        .with(administrador()))
                .andExpect(status().isOk());
        verify(excepcionRepository).findBySalonIdAndFechaAndActivoTrue(SALON_ID, MANANA);
    }

    @Test
    void getLegacyIntactoConDesdeYHasta() throws Exception {
        when(excepcionRepository.findBySalonIdAndFechaBetweenAndActivoTrueOrderByFecha(SALON_ID, HOY, MANANA))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/salones/{salonId}/excepciones-horario", SALON_ID)
                        .param("desde", HOY.toString())
                        .param("hasta", MANANA.toString())
                        .with(lector()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // ---------- A3: 403 antes de revelar el recurso en el DELETE legacy ----------

    @Test
    void deleteLegacySinScopeRecibe403SinLeerLaExcepcion() throws Exception {
        mockMvc.perform(delete("/api/salones/{salonId}/excepciones-horario/{id}", SALON_AJENO_ID, UUID.randomUUID())
                        .with(administrador()))
                .andExpect(status().isForbidden());

        verifyNoInteractions(excepcionRepository);
    }

    @Test
    void deleteLegacyAutorizadoConIdDeOtroSalonRecibe404() throws Exception {
        UUID idAjeno = UUID.randomUUID();
        SalonHorarioExcepcion deOtroSalon = excepcionActiva(MANANA);
        Salon otro = new Salon();
        otro.setId(SALON_AJENO_ID);
        deOtroSalon.setSalon(otro);
        when(excepcionRepository.findById(idAjeno)).thenReturn(Optional.of(deOtroSalon));

        mockMvc.perform(delete("/api/salones/{salonId}/excepciones-horario/{id}", SALON_ID, idAjeno)
                        .with(administrador()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("EXCEPCION_HORARIO_NO_EXISTE"));

        verify(excepcionRepository, never()).save(any());
    }

    // ---------- A4: codigos estables, whitelist 409 cerrada ----------

    @Test
    void fechaEnElPasadoDevuelve400() throws Exception {
        mockMvc.perform(put("/api/salones/{salonId}/excepciones-horario/por-fecha/{fecha}", SALON_ID, AYER)
                        .with(administrador())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CERRADO_VALIDO))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("EXCEPCION_HORARIO_EN_EL_PASADO"));
    }

    @Test
    void horarioEspecialIncompletoDevuelve400() throws Exception {
        mockMvc.perform(put("/api/salones/{salonId}/excepciones-horario/por-fecha/{fecha}", SALON_ID, MANANA)
                        .with(administrador())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"cerrado": false, "horaApertura": null, "horaCierre": "16:00"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("HORARIO_ESPECIAL_INCOMPLETO"));
    }

    @Test
    void horaCierreNoPosteriorDevuelve400() throws Exception {
        mockMvc.perform(put("/api/salones/{salonId}/excepciones-horario/por-fecha/{fecha}", SALON_ID, MANANA)
                        .with(administrador())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"cerrado": false, "horaApertura": "16:00", "horaCierre": "10:00"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("HORA_CIERRE_DEBE_SER_POSTERIOR"));
    }

    @Test
    void cancelarInexistenteDevuelve404NoExiste() throws Exception {
        when(excepcionRepository.findBySalonIdAndFechaAndActivoTrue(SALON_ID, MANANA)).thenReturn(Optional.empty());

        mockMvc.perform(delete("/api/salones/{salonId}/excepciones-horario/por-fecha/{fecha}", SALON_ID, MANANA)
                        .with(administrador()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("EXCEPCION_HORARIO_NO_EXISTE"));
    }

    @Test
    void impactoPuntualIncompatibleDevuelve409() throws Exception {
        when(validador.evaluar(any())).thenReturn(
                List.of(ConflictoProgramacionPuntual.turnoExcepcion(UUID.randomUUID(), "16:00-18:00")));

        mockMvc.perform(put("/api/salones/{salonId}/excepciones-horario/por-fecha/{fecha}", SALON_ID, MANANA)
                        .with(administrador())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CERRADO_VALIDO))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("PROGRAMACION_PUNTUAL_INCOMPATIBLE_CON_EXCEPCION"));
    }

    @Test
    void violacionDelIndiceUnicoDevuelve409ConCodigoEstable() throws Exception {
        SQLException sqlException = new SQLException("violacion", "23505");
        ConstraintViolationException hibernate = new ConstraintViolationException(
                "violacion", sqlException, "idx_salon_horario_excepcion_unica");
        when(excepcionRepository.saveAndFlush(any(SalonHorarioExcepcion.class)))
                .thenThrow(new DataIntegrityViolationException("fallo de integridad", hibernate));

        mockMvc.perform(put("/api/salones/{salonId}/excepciones-horario/por-fecha/{fecha}", SALON_ID, MANANA)
                        .with(administrador())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CERRADO_VALIDO))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("CONFLICTO_EXCEPCION_HORARIO"));
    }

    // ---------- helpers ----------

    private RequestPostProcessor administrador() {
        return autenticadoConAutoridad("salon.administrar");
    }

    private RequestPostProcessor lector() {
        return autenticadoConAutoridad("salon.leer");
    }

    private RequestPostProcessor autenticadoConAutoridad(String autoridad) {
        UsuarioAutenticado principal = new UsuarioAutenticado(ACTOR_ID, "actor@test.com");
        return authentication(UsernamePasswordAuthenticationToken.authenticated(
                principal, null, List.of(new SimpleGrantedAuthority(autoridad))));
    }

    private Usuario usuarioConScope(UUID salonId, String... permisos) {
        Usuario usuario = new Usuario();
        usuario.setId(ACTOR_ID);
        usuario.setEstatus(Usuario.EstatusUsuario.activo);

        Rol rol = new Rol();
        rol.setNombre(Rol.PERSONAL);
        for (String codigo : permisos) {
            Permiso permiso = new Permiso();
            permiso.setCodigo(codigo);
            rol.getPermisos().add(permiso);
        }
        rol.getPermisos().add(permisoLeer());

        Salon salonScope = new Salon();
        salonScope.setId(salonId);
        usuario.getRoles().add(new UsuarioRol(usuario, rol, salonScope));
        return usuario;
    }

    private Permiso permisoLeer() {
        Permiso permiso = new Permiso();
        permiso.setCodigo("salon.leer");
        return permiso;
    }

    private SalonHorarioExcepcion excepcionActiva(LocalDate fecha) {
        SalonHorarioExcepcion e = new SalonHorarioExcepcion();
        e.setId(UUID.randomUUID());
        e.setSalon(salon);
        e.setFecha(fecha);
        e.setCerrado(true);
        e.setActivo(true);
        return e;
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class ConfiguracionTest {

        @Bean
        JwtAuthFilter jwtAuthFilter() {
            return new JwtAuthFilter(null, null);
        }

        @Bean
        Clock reloj() {
            return Clock.fixed(HOY.atStartOfDay(ZoneId.of("UTC")).toInstant(), ZoneId.of("UTC"));
        }
    }
}
