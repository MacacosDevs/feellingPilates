package com.feelingpilates.auth;

import com.feelingpilates.TestcontainersConfiguration;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@Transactional
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GoogleTokenVerifier googleTokenVerifier;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private String registrarYObtenerToken(String correo) throws Exception {
        String body = mockMvc.perform(post("/api/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"correo": "%s", "contrasena": "secreta123", "nombre": "Ana Prueba"}
                                """.formatted(correo)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).get("token").asText();
    }

    @Test
    void registroDevuelveTokenConRolCliente() throws Exception {
        String token = registrarYObtenerToken("registro@test.com");
        assertThat(token).isNotBlank();

        // el payload del JWT es la segunda parte, base64url
        String payload = new String(java.util.Base64.getUrlDecoder().decode(token.split("\\.")[1]));
        JsonNode claims = objectMapper.readTree(payload);
        assertThat(claims.get("roles").toString()).contains("CLIENTE");
    }

    @Test
    void registroConCorreoDuplicadoDevuelve409() throws Exception {
        registrarYObtenerToken("duplicado@test.com");
        mockMvc.perform(post("/api/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"correo": "duplicado@test.com", "contrasena": "secreta123", "nombre": "Otra"}
                                """))
                .andExpect(status().isConflict());
    }

    @Test
    void loginCorrectoDevuelveToken() throws Exception {
        registrarYObtenerToken("login@test.com");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"correo": "login@test.com", "contrasena": "secreta123"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.tipo").value("Bearer"));
    }

    @Test
    void loginConContrasenaIncorrectaDevuelve401() throws Exception {
        registrarYObtenerToken("mal-login@test.com");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"correo": "mal-login@test.com", "contrasena": "incorrecta"}
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void mePerfilSinTokenDevuelve401() throws Exception {
        mockMvc.perform(get("/api/usuarios/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void mePerfilConTokenDevuelveDatos() throws Exception {
        String token = registrarYObtenerToken("perfil@test.com");
        mockMvc.perform(get("/api/usuarios/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correo").value("perfil@test.com"))
                .andExpect(jsonPath("$.roles[0]").value("CLIENTE"));
    }

    @Test
    void actualizarPerfilPropioFunciona() throws Exception {
        String token = registrarYObtenerToken("editar@test.com");
        mockMvc.perform(put("/api/usuarios/me")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre": "Nombre Nuevo", "telefono": "5512345678"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Nombre Nuevo"))
                .andExpect(jsonPath("$.telefono").value("5512345678"));
    }

    @Test
    void clienteNoPuedeListarUsuariosAdmin() throws Exception {
        String token = registrarYObtenerToken("cliente@test.com");
        mockMvc.perform(get("/api/admin/usuarios")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void googleStubDevuelve501() throws Exception {
        mockMvc.perform(post("/api/auth/google")
                        .contentType(MediaType.APPLICATION_JSON)
                .content("""
                                {"idToken": "fake-token"}
                                """))
                .andExpect(status().isNotImplemented())
                .andExpect(jsonPath("$.status").value(501))
                .andExpect(jsonPath("$.error").value("Not Implemented"))
                .andExpect(jsonPath("$.message").value("El inicio de sesión con Google está deshabilitado"))
                .andExpect(jsonPath("$.path").value("/api/auth/google"));

        verifyNoInteractions(googleTokenVerifier);
    }

    @Test
    void invitacionInvalidaNoReflejaTokenEnRespuesta() throws Exception {
        String token = "token-invitacion-super-secreto";

        String respuesta = mockMvc.perform(get("/api/auth/invitaciones/{token}", token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Invitación no encontrada"))
                .andExpect(jsonPath("$.path").value("/api/auth/invitaciones/[REDACTADO]"))
                .andReturn().getResponse().getContentAsString();

        assertThat(respuesta).doesNotContain(token);
    }
}
