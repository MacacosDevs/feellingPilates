package com.feelingpilates.ubicaciones.servicio;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** Fija la whitelist cerrada de codigos que se traducen a 409 (F2B.3b.2a, §11.5 del diseño). */
class HorarioOperacionErroresTest {

    @Test
    void losSeisCodigosDeLaWhitelistSonConflictoDeEstado() {
        assertThat(HorarioOperacionErrores.esConflictoDeEstado("YA_EXISTE_VERSION_EN_ESA_FECHA: texto")).isTrue();
        assertThat(HorarioOperacionErrores.esConflictoDeEstado("VERSIONADO_INTERMEDIO_NO_SOPORTADO: texto")).isTrue();
        assertThat(HorarioOperacionErrores.esConflictoDeEstado("NO_EXISTE_VERSION_VIGENTE_EN_ESA_FECHA: texto")).isTrue();
        assertThat(HorarioOperacionErrores.esConflictoDeEstado("CANCELACION_DE_VERSION_NO_SOPORTADA: texto")).isTrue();
        assertThat(HorarioOperacionErrores.esConflictoDeEstado("CIERRE_CON_VERSIONES_FUTURAS: texto")).isTrue();
        assertThat(HorarioOperacionErrores.esConflictoDeEstado("PROGRAMACION_INCOMPATIBLE_CON_HORARIO: texto")).isTrue();
    }

    @Test
    void codigosFueraDeLaWhitelistNoSonConflictoDeEstado() {
        assertThat(HorarioOperacionErrores.esConflictoDeEstado("EFECTIVO_DESDE_EN_EL_PASADO: texto")).isFalse();
        assertThat(HorarioOperacionErrores.esConflictoDeEstado("DIA_SEMANA_INVALIDO: texto")).isFalse();
        assertThat(HorarioOperacionErrores.esConflictoDeEstado("HORA_CIERRE_DEBE_SER_POSTERIOR: texto")).isFalse();
        assertThat(HorarioOperacionErrores.esConflictoDeEstado("CODIGO_INVENTADO_QUE_NO_EXISTE: texto")).isFalse();
    }

    @Test
    void mensajeSinPrefijoNoEsConflictoDeEstado() {
        assertThat(HorarioOperacionErrores.esConflictoDeEstado("Salón no encontrado")).isFalse();
    }

    @Test
    void mensajeNuloNoEsConflictoDeEstado() {
        assertThat(HorarioOperacionErrores.esConflictoDeEstado(null)).isFalse();
    }

    @Test
    void programacionIncompatibleConDosDosPuntosSigueSiendoConflictoDeEstado() {
        assertThat(HorarioOperacionErrores.esConflictoDeEstado(
                HorarioOperacionErrores.PROGRAMACION_INCOMPATIBLE_CON_HORARIO
                        + ": TURNO_RECURRENTE[detalle], BLOQUE_PROGRAMACION[detalle]"))
                .isTrue();
    }
}
