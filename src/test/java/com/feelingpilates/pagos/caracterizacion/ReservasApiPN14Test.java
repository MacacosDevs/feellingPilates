package com.feelingpilates.pagos.caracterizacion;

import com.feelingpilates.calendario.controlador.ReservaController;
import com.feelingpilates.calendario.dto.*;
import com.feelingpilates.calendario.entidad.Reserva;
import com.feelingpilates.calendario.servicio.ReservaService;
import com.feelingpilates.exception.*;
import com.feelingpilates.seguridad.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.*;
import org.springframework.test.web.servlet.MockMvc;
import java.time.*;
import java.util.*;
import static com.feelingpilates.pagos.caracterizacion.PN14Fixtures.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReservaController.class)
@Import({SecurityConfig.class,ReservasApiPN14Test.Config.class})
class ReservasApiPN14Test {
    @Autowired MockMvc mvc;@Autowired ReservaService service;
    @BeforeEach void setup(){reset(service);}
    ReservaResponse response(){return new ReservaResponse(COMPRA,SALON,"Sede",ACTOR,"Instructor",CLIENTE,"Cliente",PAQUETE,"Actividad",LocalDate.of(2026,8,24),LocalTime.of(9,0),LocalTime.of(9,45),Reserva.Estado.CONFIRMADA);}
    String body(){return "{\"salonId\":\""+SALON+"\",\"instructorId\":\""+ACTOR+"\",\"clienteId\":\""+CLIENTE+"\",\"tipoActividadId\":\""+PAQUETE+"\",\"fecha\":\"2026-08-24\",\"horaInicio\":\"09:00:00\"}";}
    String responseJSON(){return "{\"id\":\""+COMPRA+"\",\"salonId\":\""+SALON+"\",\"salonNombre\":\"Sede\",\"instructorId\":\""+ACTOR+"\",\"instructorNombre\":\"Instructor\",\"clienteId\":\""+CLIENTE+"\",\"clienteNombre\":\"Cliente\",\"tipoActividadId\":\""+PAQUETE+"\",\"tipoActividadNombre\":\"Actividad\",\"fecha\":\"2026-08-24\",\"horaInicio\":\"09:00:00\",\"horaFin\":\"09:45:00\",\"estado\":\"CONFIRMADA\"}";}
    @Test void M11_crear201DTOCompletoYCancelar204ActorPrincipal() throws Exception {
        when(service.crear(eq(ACTOR),any())).thenReturn(response());
        mvc.perform(post("/api/reservas").with(principal("reserva.administrar")).contentType("application/json").content(body()))
            .andExpect(status().isCreated()).andExpect(content().json("{\"id\":\""+COMPRA+"\",\"salonId\":\""+SALON+"\",\"salonNombre\":\"Sede\",\"instructorId\":\""+ACTOR+"\",\"instructorNombre\":\"Instructor\",\"clienteId\":\""+CLIENTE+"\",\"clienteNombre\":\"Cliente\",\"tipoActividadId\":\""+PAQUETE+"\",\"tipoActividadNombre\":\"Actividad\",\"fecha\":\"2026-08-24\",\"horaInicio\":\"09:00:00\",\"horaFin\":\"09:45:00\",\"estado\":\"CONFIRMADA\"}",org.springframework.test.json.JsonCompareMode.STRICT));
        verify(service).crear(ACTOR,new ReservaRequest(SALON,ACTOR,CLIENTE,PAQUETE,LocalDate.of(2026,8,24),LocalTime.of(9,0)));
        mvc.perform(delete("/api/reservas/{id}",COMPRA).with(principal("reserva.administrar"))).andExpect(status().isNoContent()).andExpect(content().string(""));
        verify(service).cancelar(ACTOR,COMPRA);
    }
    @Test void M11_listarCalendarioYMiasUsanPrincipal() throws Exception {
        when(service.listarPorCliente(ACTOR)).thenReturn(List.of(response()));when(service.listarPorSalonYFecha(ACTOR,SALON,LocalDate.of(2026,8,24))).thenReturn(List.of(response()));
        mvc.perform(get("/api/reservas/mias").with(principal()).param("clienteId",CLIENTE.toString())).andExpect(status().isOk())
            .andExpect(content().json("["+responseJSON()+"]",org.springframework.test.json.JsonCompareMode.STRICT));
        verify(service).listarPorCliente(ACTOR);verify(service,never()).listarPorCliente(CLIENTE);
        mvc.perform(get("/api/reservas").with(principal("calendario.leer")).param("salonId",SALON.toString()).param("fecha","2026-08-24"))
            .andExpect(status().isOk()).andExpect(content().json("["+responseJSON()+"]",org.springframework.test.json.JsonCompareMode.STRICT));
        verify(service).listarPorSalonYFecha(ACTOR,SALON,LocalDate.of(2026,8,24));
    }
    @Test void M11_401403NotNull400YAdviceSinInvocarEnRechazos() throws Exception {
        mvc.perform(get("/api/reservas/mias")).andExpect(status().isUnauthorized());mvc.perform(post("/api/reservas").contentType("application/json").content(body())).andExpect(status().isUnauthorized());
        mvc.perform(delete("/api/reservas/{id}",COMPRA)).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/reservas").with(principal()).contentType("application/json").content(body())).andExpect(status().isForbidden());
        mvc.perform(delete("/api/reservas/{id}",COMPRA).with(principal())).andExpect(status().isForbidden());
        mvc.perform(get("/api/reservas").with(principal()).param("salonId",SALON.toString()).param("fecha","2026-08-24")).andExpect(status().isForbidden());
        mvc.perform(post("/api/reservas").with(principal("reserva.administrar")).contentType("application/json").content("{}"))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.fieldErrors.salonId").exists()).andExpect(jsonPath("$.fieldErrors.instructorId").exists())
            .andExpect(jsonPath("$.fieldErrors.clienteId").exists()).andExpect(jsonPath("$.fieldErrors.tipoActividadId").exists())
            .andExpect(jsonPath("$.fieldErrors.fecha").exists()).andExpect(jsonPath("$.fieldErrors.horaInicio").exists());verifyNoInteractions(service);
        when(service.crear(eq(ACTOR),any())).thenThrow(new ValidacionException("fuera de horario"));
        mvc.perform(post("/api/reservas").with(principal("reserva.administrar")).contentType("application/json").content(body())).andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").value("fuera de horario"));
        doThrow(new ResourceNotFoundException("Reserva no encontrada")).when(service).cancelar(ACTOR,COMPRA);
        mvc.perform(delete("/api/reservas/{id}",COMPRA).with(principal("reserva.administrar"))).andExpect(status().isNotFound());
    }
    @TestConfiguration(proxyBeanMethods=false) static class Config {
        @Bean ReservaService reservaService(){return mock(ReservaService.class);}
        @Bean JwtAuthFilter jwtAuthFilter(){return new JwtAuthFilter(null,null);}
    }
}
