package com.feelingpilates.ubicaciones;

import com.feelingpilates.TestcontainersConfiguration;
import com.feelingpilates.seguridad.UsuarioAutenticado;
import com.feelingpilates.usuarios.entidad.Permiso;
import com.feelingpilates.usuarios.entidad.Rol;
import com.feelingpilates.usuarios.entidad.Usuario;
import com.feelingpilates.usuarios.entidad.UsuarioRol;
import com.feelingpilates.usuarios.repositorio.UsuarioRepository;
import com.feelingpilates.ubicaciones.entidad.Salon;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.http.MediaType.APPLICATION_JSON;

/**
 * PostgreSQL/Testcontainers real contra el endpoint de historial (F2B.3b.2a, §33 del diseño):
 * orden fisico, aislamiento entre salones, y los tres resultados distintos 403/404/{@code 200 []}.
 * {@code UsuarioRepository} se sustituye por un doble para fijar el scope del actor sin tener que
 * levantar el esquema completo de roles/permisos: {@link com.feelingpilates.seguridad.AutorizadorSalon}
 * es real y corre contra ese doble igual que contra la tabla real.
 *
 * <p>Deliberadamente sin {@code @Transactional} de clase: se ejercitan los writers reales via HTTP,
 * y la limpieza es explicita, igual que {@code HorarioOperacionWritersPersistenciaTest}.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class SalonHorarioOperacionHistorialPersistenciaTest {

    private static final UUID ACTOR_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private Clock reloj;

    @MockitoBean
    private UsuarioRepository usuarioRepository;

    private UUID salonAId;
    private UUID salonBId;

    @BeforeEach
    void prepararSalones() {
        salonAId = crearSalon("Salón A");
        salonBId = crearSalon("Salón B");
    }

    @AfterEach
    void limpiar() {
        jdbcTemplate.update("delete from horario_operacion where salon_id in (?, ?)", salonAId, salonBId);
        jdbcTemplate.update("delete from salon where id in (?, ?)", salonAId, salonBId);
    }

    @Test
    void legacyMasVersionNuevaQuedaEnDosElementosConNullsPreservados() throws Exception {
        insertarHorario(salonAId, (short) 1, null, null);
        LocalDate d = LocalDate.now(reloj).plusDays(30);

        mockMvc.perform(post("/api/salones/{salonId}/horarios/versiones", salonAId)
                        .with(autenticado(salonAId, "salon.administrar", "salon.leer"))
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"diaSemana": 1, "efectivoDesde": "%s", "horaApertura": "08:00", "horaCierre": "20:00"}
                                """.formatted(d)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/salones/{salonId}/horarios/historial", salonAId)
                        .param("diaSemana", "1")
                        .with(autenticado(salonAId, "salon.leer")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").doesNotExist())
                .andExpect(jsonPath("$[0].vigenteDesde").doesNotExist())
                .andExpect(jsonPath("$[0].vigenteHasta").value(d.minusDays(1).toString()))
                .andExpect(jsonPath("$[1].id").doesNotExist())
                .andExpect(jsonPath("$[1].vigenteDesde").value(d.toString()))
                .andExpect(jsonPath("$[1].vigenteHasta").doesNotExist());
    }

    @Test
    void ordenSemanaCompletaEsDiaAscYVigenteDesdeAscConNullPrimero() throws Exception {
        insertarHorario(salonAId, (short) 3, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 6, 30));
        insertarHorario(salonAId, (short) 3, null, LocalDate.of(2025, 12, 31));
        insertarHorario(salonAId, (short) 1, null, null);

        mockMvc.perform(get("/api/salones/{salonId}/horarios/historial", salonAId)
                        .with(autenticado(salonAId, "salon.leer")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].diaSemana").value(1))
                .andExpect(jsonPath("$[1].diaSemana").value(3))
                .andExpect(jsonPath("$[1].vigenteDesde").doesNotExist())
                .andExpect(jsonPath("$[2].diaSemana").value(3))
                .andExpect(jsonPath("$[2].vigenteDesde").value("2026-01-01"));
    }

    @Test
    void filtroPorDiaDevuelveSoloEseDiaEnElMismoOrden() throws Exception {
        insertarHorario(salonAId, (short) 1, null, LocalDate.of(2025, 12, 31));
        insertarHorario(salonAId, (short) 1, LocalDate.of(2026, 1, 1), null);
        insertarHorario(salonAId, (short) 2, null, null);

        mockMvc.perform(get("/api/salones/{salonId}/horarios/historial", salonAId)
                        .param("diaSemana", "1")
                        .with(autenticado(salonAId, "salon.leer")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].diaSemana").value(1))
                .andExpect(jsonPath("$[0].vigenteHasta").value("2025-12-31"))
                .andExpect(jsonPath("$[1].vigenteDesde").value("2026-01-01"));
    }

    @Test
    void historialDeUnSalonNoContieneFilasDeOtro() throws Exception {
        insertarHorario(salonAId, (short) 1, null, null);
        insertarHorario(salonBId, (short) 1, null, null);

        mockMvc.perform(get("/api/salones/{salonId}/horarios/historial", salonAId)
                        .with(autenticado(salonAId, "salon.leer")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void actorConAlcanceSoloSobreBRecibe403AlPedirHistorialDeA() throws Exception {
        insertarHorario(salonAId, (short) 1, null, null);

        mockMvc.perform(get("/api/salones/{salonId}/horarios/historial", salonAId)
                        .with(autenticado(salonBId, "salon.leer")))
                .andExpect(status().isForbidden());
    }

    @Test
    void salonSinHorariosDevuelveListaVaciaYSalonInexistenteDevuelve404() throws Exception {
        mockMvc.perform(get("/api/salones/{salonId}/horarios/historial", salonAId)
                        .with(autenticado(salonAId, "salon.leer")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        UUID inexistente = UUID.randomUUID();
        mockMvc.perform(get("/api/salones/{salonId}/horarios/historial", inexistente)
                        .with(autenticado(inexistente, "salon.leer")))
                .andExpect(status().isNotFound());
    }

    @Test
    void cierreQuedaReflejadoSinVersionAbierta() throws Exception {
        LocalDate d0 = LocalDate.now(reloj).plusDays(10);
        LocalDate d1 = LocalDate.now(reloj).plusDays(30);
        insertarHorario(salonAId, (short) 4, d0, null);

        mockMvc.perform(post("/api/salones/{salonId}/horarios/cierres", salonAId)
                        .with(autenticado(salonAId, "salon.administrar", "salon.leer"))
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"diaSemana": 4, "efectivoDesde": "%s"}
                                """.formatted(d1)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/salones/{salonId}/horarios/historial", salonAId)
                        .param("diaSemana", "4")
                        .with(autenticado(salonAId, "salon.leer")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].vigenteHasta").value(d1.minusDays(1).toString()));
    }

    // ---------- helpers ----------

    private RequestPostProcessor autenticado(UUID salonScopeId, String... permisos) {
        when(usuarioRepository.findById(ACTOR_ID)).thenReturn(Optional.of(usuarioConScope(salonScopeId, permisos)));
        UsuarioAutenticado principal = new UsuarioAutenticado(ACTOR_ID, "actor@test.com");
        List<SimpleGrantedAuthority> autoridades = List.of(permisos).stream()
                .map(SimpleGrantedAuthority::new)
                .toList();
        return authentication(UsernamePasswordAuthenticationToken.authenticated(principal, null, autoridades));
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

    private UUID crearSalon(String nombre) {
        UUID id = UUID.randomUUID();
        jdbcTemplate.update("""
                insert into salon (id, nombre, estado_id, municipio_id, direccion)
                values (?, ?, 22, 14, 'Calle de prueba')
                """, id, nombre + " " + id);
        return id;
    }

    private void insertarHorario(UUID salonId, short diaSemana, LocalDate desde, LocalDate hasta) {
        jdbcTemplate.update("""
                insert into horario_operacion
                    (id, salon_id, dia_semana, hora_apertura, hora_cierre, vigente_desde, vigente_hasta)
                values (?, ?, ?, '08:00', '20:00', ?, ?)
                """,
                UUID.randomUUID(), salonId, diaSemana, desde, hasta);
    }
}
