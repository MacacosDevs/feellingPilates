package com.feelingpilates.ubicaciones.servicio;

import com.feelingpilates.exception.ConflictException;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import java.sql.SQLException;

import static com.feelingpilates.ubicaciones.servicio.ConflictoExcepcionHorarioTranslator.traduciendoConflictoDeExcepcion;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * El indice unico parcial es el backstop final, no el control de flujo normal. Estos tests fijan
 * las dos mitades del contrato: se traduce {@code 23505} del constraint esperado, y NO se traduce
 * nada mas. Analogo exacto de {@code ConflictoVigenciaHorarioTranslatorTest}.
 */
class ConflictoExcepcionHorarioTranslatorTest {

    @Test
    void devuelveElResultadoCuandoNoHayError() {
        assertThat(traduciendoConflictoDeExcepcion(() -> "ok")).isEqualTo("ok");
    }

    @Test
    void traduce23505DelConstraintDelIndiceUnico() {
        assertThatThrownBy(() -> traduciendoConflictoDeExcepcion(() -> {
            throw violacion("23505", "idx_salon_horario_excepcion_unica");
        }))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("CONFLICTO_EXCEPCION_HORARIO");
    }

    /**
     * Sin nombre de constraint no hay evidencia inequivoca de que el 23505 sea el indice unico
     * parcial: podria ser cualquier otra violacion de unicidad. Se relanza intacta.
     */
    @Test
    void noTraduce23505SiElDriverNoReportaElNombreDelConstraint() {
        DataIntegrityViolationException error = violacion("23505", null);

        assertThatThrownBy(() -> traduciendoConflictoDeExcepcion(() -> {
            throw error;
        }))
                .isSameAs(error)
                .isNotInstanceOf(ConflictException.class);
    }

    /**
     * Un {@code SQLException} crudo, sin envoltorio de {@link ConstraintViolationException} de
     * Hibernate, no expone metadata estructurada del constraint: no es evidencia suficiente,
     * aunque el SQLSTATE sea {@code 23505}. Se relanza intacta.
     */
    @Test
    void noTraduce23505CrudoSinEnvoltorioDeHibernate() {
        DataIntegrityViolationException error = new DataIntegrityViolationException(
                "fallo", new SQLException("unique violation", "23505"));

        assertThatThrownBy(() -> traduciendoConflictoDeExcepcion(() -> {
            throw error;
        }))
                .isSameAs(error)
                .isNotInstanceOf(ConflictException.class);
    }

    /** Mutacion F: cualquier DataIntegrityViolationException traducida como conflicto de excepcion. */
    @Test
    void noTraduceUnDataIntegrityViolationGenerico() {
        DataIntegrityViolationException error = new DataIntegrityViolationException("clave duplicada");

        assertThatThrownBy(() -> traduciendoConflictoDeExcepcion(() -> {
            throw error;
        }))
                .isSameAs(error);
    }

    @Test
    void noTraduceOtraViolacionDeIntegridadConOtroSqlstate() {
        assertThatThrownBy(() -> traduciendoConflictoDeExcepcion(() -> {
            throw violacion("23P01", "otro_constraint");
        }))
                .isInstanceOf(DataIntegrityViolationException.class)
                .isNotInstanceOf(ConflictException.class);
    }

    @Test
    void noTraduce23505DeOtroConstraintDistinto() {
        assertThatThrownBy(() -> traduciendoConflictoDeExcepcion(() -> {
            throw violacion("23505", "otro_indice_unico");
        }))
                .isInstanceOf(DataIntegrityViolationException.class)
                .isNotInstanceOf(ConflictException.class);
    }

    @Test
    void noTraduceUnErrorQueNoEsDeIntegridad() {
        IllegalStateException error = new IllegalStateException("otra cosa");

        assertThatThrownBy(() -> traduciendoConflictoDeExcepcion(() -> {
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
