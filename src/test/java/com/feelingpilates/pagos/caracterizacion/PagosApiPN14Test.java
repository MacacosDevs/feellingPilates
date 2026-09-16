package com.feelingpilates.pagos.caracterizacion;

import com.feelingpilates.exception.*;
import com.feelingpilates.pagos.controlador.*;
import com.feelingpilates.pagos.dto.*;
import com.feelingpilates.pagos.entidad.Compra;
import com.feelingpilates.pagos.servicio.PagoService;
import com.feelingpilates.seguridad.*;
import com.stripe.Stripe;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.*;
import org.springframework.test.web.servlet.MockMvc;
import java.util.*;
import static com.feelingpilates.pagos.caracterizacion.PN14Fixtures.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PagoController.class)
@Import({SecurityConfig.class,PagosApiPN14Test.Config.class})
@ResourceLock(STRIPE_LOCK)
class PagosApiPN14Test {
    @Autowired MockMvc mvc;@Autowired PagoService service;StripeScope stripe;
    @BeforeEach void setup(){reset(service);stripe=new StripeScope();}
    @AfterEach void restore(){stripe.close();}
    @Test void M10_intentoBodyOpcionalYKeyPrincipalRespuestaExacta() throws Exception {
        when(service.crearIntentoPago(ACTOR,PAQUETE,null)).thenReturn(new CrearPagoResponse(COMPRA,"secret_dummy","pk_test_pn14_dummy"));
        mvc.perform(post("/api/pagos/paquetes/{id}/intento",PAQUETE).with(principal()))
            .andExpect(status().isOk()).andExpect(content().json("{\"compraId\":\""+COMPRA+"\",\"clientSecret\":\"secret_dummy\",\"publishableKey\":\"pk_test_pn14_dummy\"}",org.springframework.test.json.JsonCompareMode.STRICT));
        when(service.crearIntentoPago(ACTOR,PAQUETE,"key_dummy")).thenReturn(new CrearPagoResponse(COMPRA,"secret_dummy","pk_test_pn14_dummy"));
        mvc.perform(post("/api/pagos/paquetes/{id}/intento",PAQUETE).with(principal()).contentType("application/json").content("{\"idempotencyKey\":\"key_dummy\"}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.compraId").value(COMPRA.toString()));
        verify(service).crearIntentoPago(ACTOR,PAQUETE,null);verify(service).crearIntentoPago(ACTOR,PAQUETE,"key_dummy");
    }
    @Test void M10_getPropioPrincipalSinPermisoAdicionalYDTOs() throws Exception {
        when(service.obtenerPaquetesActivos(ACTOR)).thenReturn(List.of(new PaqueteActivoResponse("pilates","Dummy",FECHA,FECHA.plusDays(30))));
        when(service.obtenerHistorialCompras(ACTOR)).thenReturn(List.of(new CompraResponse(COMPRA,"Dummy","pilates",12345,"pagada",FECHA,null)));
        mvc.perform(get("/api/pagos/mis-paquetes").with(principal())).andExpect(status().isOk())
            .andExpect(content().json("[{\"categoria\":\"pilates\",\"nombre\":\"Dummy\",\"fechaInicio\":\"2020-01-02T03:04:05Z\",\"fechaExpiracion\":\"2020-02-01T03:04:05Z\"}]",org.springframework.test.json.JsonCompareMode.STRICT));
        mvc.perform(get("/api/pagos/mis-compras").with(principal())).andExpect(status().isOk())
            .andExpect(content().json("[{\"id\":\""+COMPRA+"\",\"paqueteNombre\":\"Dummy\",\"categoria\":\"pilates\",\"montoCentavos\":12345,\"estado\":\"pagada\",\"creadoEn\":\"2020-01-02T03:04:05Z\",\"fechaExpiracion\":null}]",org.springframework.test.json.JsonCompareMode.STRICT));
        verify(service).obtenerPaquetesActivos(ACTOR);verify(service).obtenerHistorialCompras(ACTOR);
    }
    @Test void M03_webhookPublicoRawExactoHMACRealYRechazos() throws Exception {
        PagoFixture real=new PagoFixture();Compra c=compra(real.paquete,"pi_http_dummy");
        when(real.compras.findByStripePaymentIntentId("pi_http_dummy")).thenReturn(Optional.of(c));
        doAnswer(i->{real.service.procesarWebhook(i.getArgument(0),i.getArgument(1));return null;}).when(service).procesarWebhook(anyString(),anyString());
        String raw=evento("payment_intent.succeeded","{ \"id\": \"pi_http_dummy\", \"object\": \"payment_intent\" }",Stripe.API_VERSION);
        String signed=firma(raw);
        mvc.perform(post("/api/pagos/webhook").contentType("application/json").content(raw).header("Stripe-Signature",signed)).andExpect(status().isOk()).andExpect(content().string(""));
        verify(service).procesarWebhook(raw,signed);assertThat(c.getEstado()).isEqualTo(Compra.EstadoCompra.pagada);verify(real.compras).save(c);
        clearInvocations(real.compras);
        mvc.perform(post("/api/pagos/webhook").contentType("application/json").content(raw+" ").header("Stripe-Signature",signed))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").value("Firma de webhook de Stripe inválida"));
        mvc.perform(post("/api/pagos/webhook").contentType("application/json").content(raw).header("Stripe-Signature","t=1,v1=bad"))
            .andExpect(status().isBadRequest());verifyNoInteractions(real.compras);
        clearInvocations(service);
        // Generic advice currently converts MissingRequestHeaderException to HTTP 500.
        mvc.perform(post("/api/pagos/webhook").contentType("application/json").content(raw)).andExpect(status().isInternalServerError());
        verifyNoInteractions(service);
    }
    @Test void M10_privadas401YAdvice400404500() throws Exception {
        mvc.perform(get("/api/pagos/mis-compras")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/pagos/mis-paquetes")).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/pagos/paquetes/{id}/intento",PAQUETE)).andExpect(status().isUnauthorized());verifyNoInteractions(service);
        when(service.crearIntentoPago(ACTOR,PAQUETE,null)).thenThrow(new ResourceNotFoundException("Paquete no encontrado"));
        mvc.perform(post("/api/pagos/paquetes/{id}/intento",PAQUETE).with(principal())).andExpect(status().isNotFound()).andExpect(jsonPath("$.message").value("Paquete no encontrado"));
        doThrow(new ValidacionException("No se pudo iniciar el pago")).when(service).crearIntentoPago(ACTOR,PAQUETE,null);
        mvc.perform(post("/api/pagos/paquetes/{id}/intento",PAQUETE).with(principal())).andExpect(status().isBadRequest());
        when(service.obtenerHistorialCompras(ACTOR)).thenThrow(new NullPointerException("legacy categoria"));
        mvc.perform(get("/api/pagos/mis-compras").with(principal())).andExpect(status().isInternalServerError());
    }
    @TestConfiguration(proxyBeanMethods=false) static class Config {
        @Bean PagoService pagoService(){return mock(PagoService.class);}
        @Bean JwtAuthFilter jwtAuthFilter(){return new JwtAuthFilter(null,null);}
    }
}
