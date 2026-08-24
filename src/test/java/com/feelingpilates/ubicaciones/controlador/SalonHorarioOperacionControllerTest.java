package com.feelingpilates.ubicaciones.controlador;

import com.feelingpilates.exception.ConflictException;
import com.feelingpilates.exception.ResourceNotFoundException;
import com.feelingpilates.exception.ValidacionException;
import com.feelingpilates.seguridad.AutorizadorSalon;
import com.feelingpilates.seguridad.JwtAuthFilter;
import com.feelingpilates.seguridad.SecurityConfig;
import com.feelingpilates.seguridad.UsuarioAutenticado;
import com.feelingpilates.ubicaciones.entidad.HorarioOperacion;
import com.feelingpilates.ubicaciones.entidad.Salon;
import com.feelingpilates.ubicaciones.repositorio.HorarioOperacionRepository;
import com.feelingpilates.ubicaciones.repositorio.SalonRepository;
import com.feelingpilates.ubicaciones.servicio.CerrarHorarioOperacion;
import com.feelingpilates.ubicaciones.servicio.HorarioOperacionErrores;
import com.feelingpilates.ubicaciones.servicio.SalonHorarioOperacionService;
import com.feelingpilates.ubicaciones.servicio.VersionarHorarioOperacion;
import com.feelingpilates.usuarios.entidad.Permiso;
import com.feelingpilates.usuarios.entidad.Rol;
import com.feelingpilates.usuarios.entidad.Usuario;
import com.feelingpilates.usuarios.entidad.UsuarioRol;
import com.feelingpilates.usuarios.repositorio.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.time.LocalDate;
import java.time.LocalTime;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Controller/aplicacion de F2B.3b.2a: traduccion HTTP de los tres endpoints de horarios
 * versionados. Los writers ({@link VersionarHorarioOperacion}, {@link CerrarHorarioOperacion}) se
 * sustituyen por dobles: aqui se prueba la traduccion HTTP (status, codigo, orden de
 * autorizacion), no el dominio de clasificacion temporal (eso ya lo cubren los tests unitarios de
 * cada writer y los de persistencia).
 */
@WebMvcTest(SalonHorarioOperacionController.class)
@Import({
        SecurityConfig.class,
        AutorizadorSalon.class,
        SalonHorarioOperacionService.class,
        SalonHorarioOperacionControllerTest.ConfiguracionTest.class
})
class SalonHorarioOperacionControllerTest {

    private static final UUID ACTOR_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private static final UUID SALON_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID SALON_AJENO_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final short LUNES = 1;

    private static final String VERSIONAR_VALIDO = """
            {"diaSemana": 1, "efectivoDesde": "2026-09-01", "horaApertura": "08:00", "horaCierre": "20:00"}
            """;
    private static final String CERRAR_VALIDO = """
            {"diaSemana": 1, "efectivoDesde": "2026-09-01"}
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UsuarioRepository usuarioRepository;

    @MockitoBean
    private SalonRepository salonRepository;

    @MockitoBean
    private HorarioOperacionRepository horarioOperacionRepository;

    @MockitoBean
    private VersionarHorarioOperacion versionarHorarioOperacion;

    @MockitoBean
    private CerrarHorarioOperacion cerrarHorarioOperacion;

    @BeforeEach
    void prepararActorConScopeCompleto() {
        when(usuarioRepository.findById(ACTOR_ID))
                .thenReturn(Optional.of(usuarioConScope(SALON_ID, "salon.administrar", "salon.leer")));
    }

    // ---------- 1-2: caminos felices ----------

    @Test
    void versionarConScopeYPermisoDevuelve201ConCuerpoSinId() throws Exception {
        when(versionarHorarioOperacion.ejecutar(any())).thenReturn(
                horario(LUNES, LocalTime.of(8, 0), LocalTime.of(20, 0), LocalDate.of(2026, 9, 1), null));

        mockMvc.perform(post("/api/salones/{salonId}/horarios/versiones", SALON_ID)
                        .with(administrador())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VERSIONAR_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.diaSemana").value(1))
                .andExpect(jsonPath("$.vigenteDesde").value("2026-09-01"))
                .andExpect(jsonPath("$.vigenteHasta").doesNotExist())
                .andExpect(jsonPath("$.id").doesNotExist());

        verify(versionarHorarioOperacion).ejecutar(new VersionarHorarioOperacion.VersionarHorario(
                SALON_ID, LUNES, LocalDate.of(2026, 9, 1), LocalTime.of(8, 0), LocalTime.of(20, 0)));
    }

    @Test
    void cerrarAutorizadoDevuelve200ConVigenteHastaCerrado() throws Exception {
        when(cerrarHorarioOperacion.ejecutar(any())).thenReturn(
                horario(LUNES, LocalTime.of(8, 0), LocalTime.of(20, 0), LocalDate.of(2026, 6, 1), LocalDate.of(2026, 8, 31)));

        mockMvc.perform(post("/api/salones/{salonId}/horarios/cierres", SALON_ID)
                        .with(administrador())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CERRAR_VALIDO))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vigenteHasta").value("2026-08-31"));

        verify(cerrarHorarioOperacion).ejecutar(
                new CerrarHorarioOperacion.CerrarHorario(SALON_ID, LUNES, LocalDate.of(2026, 9, 1)));
    }

    // ---------- 3-4: 400 de Bean Validation ----------

    @Test
    void versionarSinEfectivoDesdeDevuelve400ConFieldErrors() throws Exception {
        mockMvc.perform(post("/api/salones/{salonId}/horarios/versiones", SALON_ID)
                        .with(administrador())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"diaSemana": 1, "horaApertura": "08:00", "horaCierre": "20:00"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.efectivoDesde").exists());
        verifyNoInteractions(versionarHorarioOperacion);
    }

    @Test
    void versionarSinDiaSemanaDevuelve400ConFieldErrors() throws Exception {
        mockMvc.perform(post("/api/salones/{salonId}/horarios/versiones", SALON_ID)
                        .with(administrador())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"efectivoDesde": "2026-09-01", "horaApertura": "08:00", "horaCierre": "20:00"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.diaSemana").exists());
        verifyNoInteractions(versionarHorarioOperacion);
    }

    // ---------- 5-6: seguridad ----------

    @Test
    void autenticadoSinSalonAdministrarRecibe403YWriterNoInvocado() throws Exception {
        mockMvc.perform(post("/api/salones/{salonId}/horarios/versiones", SALON_ID)
                        .with(autenticadoConAutoridades())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VERSIONAR_VALIDO))
                .andExpect(status().isForbidden());
        verifyNoInteractions(versionarHorarioOperacion);
    }

    @Test
    void conPermisoPeroSalonFueraDeScopeRecibe403YWriterNoInvocado() throws Exception {
        mockMvc.perform(post("/api/salones/{salonId}/horarios/versiones", SALON_AJENO_ID)
                        .with(administrador())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VERSIONAR_VALIDO))
                .andExpect(status().isForbidden());
        verifyNoInteractions(versionarHorarioOperacion);
    }

    @Test
    void cerrarSinSalonAdministrarRecibe403YWriterNoInvocado() throws Exception {
        mockMvc.perform(post("/api/salones/{salonId}/horarios/cierres", SALON_ID)
                        .with(autenticadoConAutoridades())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CERRAR_VALIDO))
                .andExpect(status().isForbidden());
        verifyNoInteractions(cerrarHorarioOperacion);
    }

    @Test
    void cerrarConPermisoPeroSalonFueraDeScopeRecibe403YWriterNoInvocado() throws Exception {
        mockMvc.perform(post("/api/salones/{salonId}/horarios/cierres", SALON_AJENO_ID)
                        .with(administrador())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CERRAR_VALIDO))
                .andExpect(status().isForbidden());
        verifyNoInteractions(cerrarHorarioOperacion);
    }

    // ---------- 7-8: seguridad de historial ----------

    @Test
    void historialSoloConSalonLeerDevuelve200() throws Exception {
        when(salonRepository.existsById(SALON_ID)).thenReturn(true);
        when(horarioOperacionRepository.findVersionesOrdenadas(eq(SALON_ID), eq(null))).thenReturn(List.of());

        mockMvc.perform(get("/api/salones/{salonId}/horarios/historial", SALON_ID).with(lector()))
                .andExpect(status().isOk());
    }

    @Test
    void historialSinSalonLeerRecibe403() throws Exception {
        mockMvc.perform(get("/api/salones/{salonId}/horarios/historial", SALON_ID).with(autenticadoConAutoridades()))
                .andExpect(status().isForbidden());
    }

    // ---------- 9-14: whitelist 409 ----------

    @Test
    void programacionIncompatibleDevuelve409ConCodigoYMensajeConIds() throws Exception {
        when(versionarHorarioOperacion.ejecutar(any())).thenThrow(new ValidacionException(
                HorarioOperacionErrores.PROGRAMACION_INCOMPATIBLE_CON_HORARIO + ": TURNO_RECURRENTE[abc]"));

        mockMvc.perform(post("/api/salones/{salonId}/horarios/versiones", SALON_ID)
                        .with(administrador())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VERSIONAR_VALIDO))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("PROGRAMACION_INCOMPATIBLE_CON_HORARIO"))
                .andExpect(jsonPath("$.message").value(
                        HorarioOperacionErrores.PROGRAMACION_INCOMPATIBLE_CON_HORARIO + ": TURNO_RECURRENTE[abc]"));
    }

    @Test
    void cierreConVersionesFuturasDevuelve409() throws Exception {
        when(cerrarHorarioOperacion.ejecutar(any()))
                .thenThrow(new ValidacionException(HorarioOperacionErrores.CIERRE_CON_VERSIONES_FUTURAS));

        mockMvc.perform(post("/api/salones/{salonId}/horarios/cierres", SALON_ID)
                        .with(administrador())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CERRAR_VALIDO))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("CIERRE_CON_VERSIONES_FUTURAS"));
    }

    @Test
    void cancelacionDeVersionNoSoportadaDevuelve409() throws Exception {
        when(cerrarHorarioOperacion.ejecutar(any()))
                .thenThrow(new ValidacionException(HorarioOperacionErrores.CANCELACION_DE_VERSION_NO_SOPORTADA));

        mockMvc.perform(post("/api/salones/{salonId}/horarios/cierres", SALON_ID)
                        .with(administrador())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CERRAR_VALIDO))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("CANCELACION_DE_VERSION_NO_SOPORTADA"));
    }

    @Test
    void noExisteVersionVigenteEnEsaFechaDevuelve409() throws Exception {
        when(cerrarHorarioOperacion.ejecutar(any()))
                .thenThrow(new ValidacionException(HorarioOperacionErrores.NO_EXISTE_VERSION_VIGENTE_EN_ESA_FECHA));

        mockMvc.perform(post("/api/salones/{salonId}/horarios/cierres", SALON_ID)
                        .with(administrador())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CERRAR_VALIDO))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("NO_EXISTE_VERSION_VIGENTE_EN_ESA_FECHA"));
    }

    @Test
    void yaExisteVersionEnEsaFechaDevuelve409() throws Exception {
        when(versionarHorarioOperacion.ejecutar(any()))
                .thenThrow(new ValidacionException(HorarioOperacionErrores.YA_EXISTE_VERSION_EN_ESA_FECHA));

        mockMvc.perform(post("/api/salones/{salonId}/horarios/versiones", SALON_ID)
                        .with(administrador())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VERSIONAR_VALIDO))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("YA_EXISTE_VERSION_EN_ESA_FECHA"));
    }

    @Test
    void versionadoIntermedioNoSoportadoDevuelve409() throws Exception {
        when(versionarHorarioOperacion.ejecutar(any()))
                .thenThrow(new ValidacionException(HorarioOperacionErrores.VERSIONADO_INTERMEDIO_NO_SOPORTADO));

        mockMvc.perform(post("/api/salones/{salonId}/horarios/versiones", SALON_ID)
                        .with(administrador())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VERSIONAR_VALIDO))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("VERSIONADO_INTERMEDIO_NO_SOPORTADO"));
    }

    // ---------- 15: ConflictException ya traducida (23P01) ----------

    @Test
    void conflictoDeVigenciaYaTraducidoPasaComo409() throws Exception {
        when(cerrarHorarioOperacion.ejecutar(any()))
                .thenThrow(new ConflictException("CONFLICTO_VIGENCIA_HORARIO: choque de vigencia"));

        mockMvc.perform(post("/api/salones/{salonId}/horarios/cierres", SALON_ID)
                        .with(administrador())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CERRAR_VALIDO))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("CONFLICTO_VIGENCIA_HORARIO"));
    }

    // ---------- 16-17: la traduccion a 409 no es indiscriminada ----------

    @Test
    void efectivoDesdeEnElPasadoSigueSiendo400() throws Exception {
        when(versionarHorarioOperacion.ejecutar(any()))
                .thenThrow(new ValidacionException(HorarioOperacionErrores.EFECTIVO_DESDE_EN_EL_PASADO));

        mockMvc.perform(post("/api/salones/{salonId}/horarios/versiones", SALON_ID)
                        .with(administrador())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VERSIONAR_VALIDO))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("EFECTIVO_DESDE_EN_EL_PASADO"));
    }

    @Test
    void horaCierreDebeSerPosteriorSigueSiendo400() throws Exception {
        when(versionarHorarioOperacion.ejecutar(any()))
                .thenThrow(new ValidacionException(HorarioOperacionErrores.HORA_CIERRE_DEBE_SER_POSTERIOR));

        mockMvc.perform(post("/api/salones/{salonId}/horarios/versiones", SALON_ID)
                        .with(administrador())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VERSIONAR_VALIDO))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("HORA_CIERRE_DEBE_SER_POSTERIOR"));
    }

    // ---------- 18: salon inexistente desde SalonLock (writer) ----------

    @Test
    void resourceNotFoundDesdeElWriterDevuelve404() throws Exception {
        when(versionarHorarioOperacion.ejecutar(any()))
                .thenThrow(new ResourceNotFoundException("Salón no encontrado"));

        mockMvc.perform(post("/api/salones/{salonId}/horarios/versiones", SALON_ID)
                        .with(administrador())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VERSIONAR_VALIDO))
                .andExpect(status().isNotFound());
    }

    // ---------- 20-23: request malformado -> 400, codigo null (§11.6) ----------

    @Test
    void jsonInvalidoDevuelve400ConCodigoNull() throws Exception {
        mockMvc.perform(post("/api/salones/{salonId}/horarios/versiones", SALON_ID)
                        .with(administrador())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"diaSemana\":"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").doesNotExist());
        verifyNoInteractions(versionarHorarioOperacion);
    }

    @Test
    void fechaNoParseableDevuelve400ConCodigoNull() throws Exception {
        mockMvc.perform(post("/api/salones/{salonId}/horarios/versiones", SALON_ID)
                        .with(administrador())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"diaSemana": 1, "efectivoDesde": "01/09/2026", "horaApertura": "08:00", "horaCierre": "20:00"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").doesNotExist());
        verifyNoInteractions(versionarHorarioOperacion);
    }

    @Test
    void horaNoParseableDevuelve400ConCodigoNull() throws Exception {
        mockMvc.perform(post("/api/salones/{salonId}/horarios/versiones", SALON_ID)
                        .with(administrador())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"diaSemana": 1, "efectivoDesde": "2026-09-01", "horaApertura": "8am", "horaCierre": "20:00"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").doesNotExist());
        verifyNoInteractions(versionarHorarioOperacion);
    }

    @Test
    void salonIdNoUuidDevuelve400() throws Exception {
        mockMvc.perform(post("/api/salones/{salonId}/horarios/versiones", "no-es-un-uuid")
                        .with(administrador())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VERSIONAR_VALIDO))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").doesNotExist());
        verifyNoInteractions(versionarHorarioOperacion);
    }

    // ---------- 24-26: filtro de historial ----------

    @Test
    void historialConDiaSemanaSieteDevuelve400() throws Exception {
        mockMvc.perform(get("/api/salones/{salonId}/horarios/historial", SALON_ID)
                        .param("diaSemana", "7")
                        .with(lector()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("DIA_SEMANA_INVALIDO"));
        verifyNoInteractions(horarioOperacionRepository);
    }

    @Test
    void historialConDiaSemanaNegativaDevuelve400() throws Exception {
        mockMvc.perform(get("/api/salones/{salonId}/horarios/historial", SALON_ID)
                        .param("diaSemana", "-1")
                        .with(lector()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("DIA_SEMANA_INVALIDO"));
    }

    @Test
    void historialConDiaSemanaNoNumericaDevuelve400ConCodigoNull() throws Exception {
        mockMvc.perform(get("/api/salones/{salonId}/horarios/historial", SALON_ID)
                        .param("diaSemana", "abc")
                        .with(lector()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").doesNotExist());
        verifyNoInteractions(salonRepository);
        verifyNoInteractions(horarioOperacionRepository);
    }

    // ---------- 27-29: 403 / 404 / 200[] son tres resultados distintos ----------

    @Test
    void historialAutorizadoConSalonInexistenteDevuelve404YNoConsultaVersiones() throws Exception {
        when(salonRepository.existsById(SALON_ID)).thenReturn(false);

        mockMvc.perform(get("/api/salones/{salonId}/horarios/historial", SALON_ID).with(lector()))
                .andExpect(status().isNotFound());

        verify(salonRepository).existsById(SALON_ID);
        verifyNoInteractions(horarioOperacionRepository);
    }

    @Test
    void historialDeSalonExistenteSinFilasDevuelve200ListaVacia() throws Exception {
        when(salonRepository.existsById(SALON_ID)).thenReturn(true);
        when(horarioOperacionRepository.findVersionesOrdenadas(eq(SALON_ID), eq(null))).thenReturn(List.of());

        mockMvc.perform(get("/api/salones/{salonId}/horarios/historial", SALON_ID).with(lector()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void historialFueraDeScopeDevuelve403YNoConsultaExistencia() throws Exception {
        mockMvc.perform(get("/api/salones/{salonId}/horarios/historial", SALON_AJENO_ID).with(lector()))
                .andExpect(status().isForbidden());

        verify(salonRepository, never()).existsById(any());
        verifyNoInteractions(horarioOperacionRepository);
    }

    // ---------- 30: codigo desconocido no se traduce a 409 ----------

    @Test
    void codigoDesconocidoDelWriterNoSeConvierteEn409() throws Exception {
        when(versionarHorarioOperacion.ejecutar(any()))
                .thenThrow(new ValidacionException("OTRA_COSA: un error no clasificado"));

        mockMvc.perform(post("/api/salones/{salonId}/horarios/versiones", SALON_ID)
                        .with(administrador())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VERSIONAR_VALIDO))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("OTRA_COSA"));
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

    /** Autenticado, pero sin ninguna autoridad: sondea que @PreAuthorize rechaza antes del service. */
    private RequestPostProcessor autenticadoConAutoridades() {
        UsuarioAutenticado principal = new UsuarioAutenticado(ACTOR_ID, "actor@test.com");
        return authentication(UsernamePasswordAuthenticationToken.authenticated(principal, null, List.of()));
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

        Salon salon = new Salon();
        salon.setId(salonId);
        usuario.getRoles().add(new UsuarioRol(usuario, rol, salon));
        return usuario;
    }

    private HorarioOperacion horario(short dia, LocalTime apertura, LocalTime cierre, LocalDate desde, LocalDate hasta) {
        HorarioOperacion h = new HorarioOperacion();
        h.setDiaSemana(dia);
        h.setHoraApertura(apertura);
        h.setHoraCierre(cierre);
        h.setVigenteDesde(desde);
        h.setVigenteHasta(hasta);
        return h;
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class ConfiguracionTest {

        @Bean
        JwtAuthFilter jwtAuthFilter() {
            return new JwtAuthFilter(null, null);
        }
    }
}
