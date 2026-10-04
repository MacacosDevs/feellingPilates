package com.feelingpilates.ubicaciones.servicio;

import com.feelingpilates.exception.ConflictException;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import java.sql.SQLException;

import static com.feelingpilates.ubicaciones.servicio.ConflictoVigenciaHorarioTranslator.traduciendoConflictoDeVigencia;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * El EXCLUDE es el backstop final, no el control de flujo normal. Estos tests fijan las dos
 * mitades del contrato: se traduce {@code 23P01} del constraint esperado, y NO se traduce nada mas.
 */
class ConflictoVigenciaHorarioTranslatorTest {

    @Test
    void devuelveElResultadoCuandoNoHayError() {
        assertThat(traduciendoConflictoDeVigencia(() -> "ok")).isEqualTo("ok");
    }

    @Test
    void traduce23P01DelConstraintDeVigencia() {
        assertThatThrownBy(() -> traduciendoConflictoDeVigencia(() -> {
            throw violacion("23P01", "ex_horario_operacion_vigencia");
        }))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("CONFLICTO_VIGENCIA_HORARIO");
    }

    @Test
    void traduce23P01AunSiElDriverNoReportaElNombreDelConstraint() {
        assertThatThrownBy(() -> traduciendoConflictoDeVigencia(() -> {
            throw violacion("23P01", null);
        }))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void traduce23P01AnidadoEnUnaSQLExceptionSinEnvoltorioDeHibernate() {
        DataIntegrityViolationException error = new DataIntegrityViolationException(
                "fallo", new SQLException("exclusion violation", "23P01"));

        assertThatThrownBy(() -> traduciendoConflictoDeVigencia(() -> {
            throw error;
        }))
                .isInstanceOf(ConflictException.class);
    }

    /** Mutacion 17: cualquier DataIntegrityViolationException traducida como conflicto de horario. */
    @Test
    void noTraduceUnDataIntegrityViolationGenerico() {
        DataIntegrityViolationException error = new DataIntegrityViolationException("clave duplicada");

        assertThatThrownBy(() -> traduciendoConflictoDeVigencia(() -> {
            throw error;
        }))
                .isSameAs(error);
    }

    @Test
    void noTraduceOtraViolacionDeIntegridadConOtroSqlstate() {
        assertThatThrownBy(() -> traduciendoConflictoDeVigencia(() -> {
            throw violacion("23505", "uq_algo");
        }))
                .isInstanceOf(DataIntegrityViolationException.class)
                .isNotInstanceOf(ConflictException.class);
    }

    @Test
    void noTraduce23P01DeOtroConstraintDistinto() {
        assertThatThrownBy(() -> traduciendoConflictoDeVigencia(() -> {
            throw violacion("23P01", "ex_alguna_otra_cosa");
        }))
                .isInstanceOf(DataIntegrityViolationException.class)
                .isNotInstanceOf(ConflictException.class);
    }

    @Test
    void noTraduceUnErrorQueNoEsDeIntegridad() {
        IllegalStateException error = new IllegalStateException("otra cosa");

        assertThatThrownBy(() -> traduciendoConflictoDeVigencia(() -> {
            throw error;
        }))
                .isSameAs(error);
    }

    /** Cadena real observada: Spring -> Hibernate ConstraintViolationException -> SQLException. */
    private DataIntegrityViolationException violacion(String sqlState, String constraint) {
        SQLException sqlException = new SQLException("violacion", sqlState);
        ConstraintViolationException hibernate = new ConstraintViolationException(
                "violacion", sqlException, constraint);
        return new DataIntegrityViolationException("fallo de integridad", hibernate);
    }
}
