package com.feelingpilates.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** Unitario, sin Spring: unico sitio del proyecto que parsea el prefijo {@code CODIGO: texto}. */
class CodigoErrorExtractorTest {

    @Test
    void extraeElCodigoEstableDelPrefijo() {
        assertThat(CodigoErrorExtractor.extraer("CIERRE_CON_VERSIONES_FUTURAS: existen versiones futuras"))
                .isEqualTo("CIERRE_CON_VERSIONES_FUTURAS");
    }

    @Test
    void tomaElPrefijoHastaElPrimerDosPuntosCuandoHayVarios() {
        assertThat(CodigoErrorExtractor.extraer(
                "PROGRAMACION_INCOMPATIBLE_CON_HORARIO: el cambio dejaría programación: TURNO_RECURRENTE[abc]"))
                .isEqualTo("PROGRAMACION_INCOMPATIBLE_CON_HORARIO");
    }

    @Test
    void mensajeSinPrefijoDaNull() {
        assertThat(CodigoErrorExtractor.extraer("Salón no encontrado")).isNull();
    }

    @Test
    void codigoDemasiadoCortoDaNull() {
        assertThat(CodigoErrorExtractor.extraer("AB: texto")).isNull();
    }

    @Test
    void prefijoEnMinusculasDaNull() {
        assertThat(CodigoErrorExtractor.extraer("minusculas: texto")).isNull();
    }

    @Test
    void soloDosPuntosSinCodigoDaNull() {
        assertThat(CodigoErrorExtractor.extraer(": texto")).isNull();
    }

    @Test
    void mensajeNuloDaNull() {
        assertThat(CodigoErrorExtractor.extraer(null)).isNull();
    }
}
