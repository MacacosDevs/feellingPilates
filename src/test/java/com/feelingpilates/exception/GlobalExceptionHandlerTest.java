package com.feelingpilates.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unitario del {@link GlobalExceptionHandler}: verifica que {@code ErrorResponse.codigo} se llena
 * a partir de {@link CodigoErrorExtractor} en {@code build(...)}, sin regresion sobre el contrato
 * existente de status/message. Llama directamente a los metodos {@code @ExceptionHandler}, sin
 * MockMvc: son metodos publicos puros respecto de su firma (excepcion, request) -&gt; ResponseEntity.
 */
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private HttpServletRequest request;

    @BeforeEach
    void preparar() {
        handler = new GlobalExceptionHandler();
        request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/salones/algo");
    }

    @Test
    void conflictExceptionConCodigoEstablePoblaCodigoYConservaMensajeIntegro() {
        ResponseEntity<ErrorResponse> respuesta = handler.handleConflict(
                new ConflictException("CIERRE_CON_VERSIONES_FUTURAS: existen versiones futuras"), request);

        assertThat(respuesta.getStatusCode().value()).isEqualTo(409);
        assertThat(respuesta.getBody().message()).isEqualTo("CIERRE_CON_VERSIONES_FUTURAS: existen versiones futuras");
        assertThat(respuesta.getBody().codigo()).isEqualTo("CIERRE_CON_VERSIONES_FUTURAS");
    }

    @Test
    void errorSinCodigoEstableDaCodigoNuloYConservaMensaje() {
        ResponseEntity<ErrorResponse> respuesta = handler.handleGeneric(new IllegalStateException("boom"), request);

        assertThat(respuesta.getStatusCode().value()).isEqualTo(500);
        assertThat(respuesta.getBody().message()).isEqualTo("Ocurrió un error inesperado");
        assertThat(respuesta.getBody().codigo()).isNull();
    }

    @Test
    void validacionExceptionSinPrefijoDaCodigoNuloYSigueSiendo400() {
        ResponseEntity<ErrorResponse> respuesta = handler.handleValidacion(
                new ValidacionException("Estado/municipio inválido"), request);

        assertThat(respuesta.getStatusCode().value()).isEqualTo(400);
        assertThat(respuesta.getBody().message()).isEqualTo("Estado/municipio inválido");
        assertThat(respuesta.getBody().codigo()).isNull();
    }

    @Test
    void validacionExceptionConCodigoEstableExtraeCodigoPeroSigueSiendo400() {
        ResponseEntity<ErrorResponse> respuesta = handler.handleValidacion(
                new ValidacionException("DIA_SEMANA_INVALIDO: el día de la semana debe estar entre 0 y 6"), request);

        assertThat(respuesta.getStatusCode().value()).isEqualTo(400);
        assertThat(respuesta.getBody().codigo()).isEqualTo("DIA_SEMANA_INVALIDO");
    }

    @Test
    void mensajeNuloNoLanzaNullPointerYDaCodigoNulo() {
        ResponseEntity<ErrorResponse> respuesta = handler.handleValidacion(new ValidacionException(null), request);

        assertThat(respuesta.getBody().message()).isNull();
        assertThat(respuesta.getBody().codigo()).isNull();
    }

    @Test
    void handlerNoLegibleDevuelve400ConMensajeFijoYCodigoNulo() {
        ResponseEntity<ErrorResponse> respuesta = handler.handleNoLegible(
                mock(org.springframework.http.converter.HttpMessageNotReadableException.class), request);

        assertThat(respuesta.getStatusCode().value()).isEqualTo(400);
        assertThat(respuesta.getBody().message()).isEqualTo("Solicitud mal formada");
        assertThat(respuesta.getBody().codigo()).isNull();
    }
}
