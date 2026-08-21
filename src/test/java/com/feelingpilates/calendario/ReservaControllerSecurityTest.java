package com.feelingpilates.calendario;

import com.feelingpilates.calendario.controlador.ReservaController;
import com.feelingpilates.calendario.dto.ReservaResponse;
import com.feelingpilates.calendario.entidad.Reserva;
import com.feelingpilates.calendario.servicio.ReservaService;
import com.feelingpilates.seguridad.JwtAuthFilter;
import com.feelingpilates.seguridad.SecurityConfig;
import com.feelingpilates.seguridad.UsuarioAutenticado;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReservaController.class)
@Import({SecurityConfig.class, ReservaControllerSecurityTest.ConfiguracionTest.class})
class ReservaControllerSecurityTest {

    private static final UUID CLIENTE_A_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private static final UUID CLIENTE_B_ID = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ReservaServiceDoble reservaService;

    @BeforeEach
    void limpiarDoble() {
        reservaService.limpiar();
    }

    @Test
    void clienteConsultaSusReservasUsandoLaIdentidadDelPrincipal() throws Exception {
        reservaService.preparar(CLIENTE_A_ID, reservaDe(CLIENTE_A_ID, "Cliente A"));

        mockMvc.perform(get("/api/reservas/mias").with(clienteAutenticado(CLIENTE_A_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].clienteId").value(CLIENTE_A_ID.toString()));

        assertThat(reservaService.consultas()).containsExactly(CLIENTE_A_ID);
    }

    @Test
    void clienteIdProporcionadoPorRequestNoReemplazaAlPrincipal() throws Exception {
        reservaService.preparar(CLIENTE_A_ID, reservaDe(CLIENTE_A_ID, "Cliente A"));

        mockMvc.perform(get("/api/reservas/mias")
                        .param("clienteId", CLIENTE_B_ID.toString())
                        .with(clienteAutenticado(CLIENTE_A_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].clienteId").value(CLIENTE_A_ID.toString()));

        assertThat(reservaService.consultas()).containsExactly(CLIENTE_A_ID);
    }

    @Test
    void reservasDeOtroClienteNuncaSeEntreganAlPrincipal() throws Exception {
        reservaService.preparar(CLIENTE_A_ID, reservaDe(CLIENTE_A_ID, "Cliente A"));
        reservaService.preparar(CLIENTE_B_ID, reservaDe(CLIENTE_B_ID, "Cliente B"));

        mockMvc.perform(get("/api/reservas/mias").with(clienteAutenticado(CLIENTE_A_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.clienteId == '%s')]", CLIENTE_B_ID).isEmpty());

        assertThat(reservaService.consultas()).containsExactly(CLIENTE_A_ID);
    }

    @Test
    void requestSinAutenticacionEsRechazado() throws Exception {
        mockMvc.perform(get("/api/reservas/mias"))
                .andExpect(status().isUnauthorized());

        assertThat(reservaService.consultas()).isEmpty();
    }

    private RequestPostProcessor clienteAutenticado(UUID clienteId) {
        UsuarioAutenticado principal = new UsuarioAutenticado(clienteId, clienteId + "@test.com");
        return authentication(UsernamePasswordAuthenticationToken.authenticated(principal, null, List.of()));
    }

    private ReservaResponse reservaDe(UUID clienteId, String clienteNombre) {
        return new ReservaResponse(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Salón Centro",
                UUID.randomUUID(),
                "Instructora",
                clienteId,
                clienteNombre,
                UUID.randomUUID(),
                "Pilates",
                LocalDate.of(2026, 8, 22),
                LocalTime.of(9, 0),
                LocalTime.of(10, 0),
                Reserva.Estado.CONFIRMADA);
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class ConfiguracionTest {

        @Bean
        ReservaServiceDoble reservaService() {
            return new ReservaServiceDoble();
        }

        @Bean
        JwtAuthFilter jwtAuthFilter() {
            return new JwtAuthFilter(null, null);
        }
    }

    static class ReservaServiceDoble extends ReservaService {

        private final Map<UUID, List<ReservaResponse>> respuestas = new HashMap<>();
        private final List<UUID> consultas = new ArrayList<>();

        ReservaServiceDoble() {
            super(null, null, null, null, null, null);
        }

        void preparar(UUID clienteId, ReservaResponse reserva) {
            respuestas.put(clienteId, List.of(reserva));
        }

        void limpiar() {
            respuestas.clear();
            consultas.clear();
        }

        List<UUID> consultas() {
            return List.copyOf(consultas);
        }

        @Override
        public List<ReservaResponse> listarPorCliente(UUID clienteId) {
            consultas.add(clienteId);
            return respuestas.getOrDefault(clienteId, List.of());
        }
    }
}
