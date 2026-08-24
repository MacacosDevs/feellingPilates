package com.feelingpilates.ubicaciones.servicio;

import com.feelingpilates.exception.ConflictException;
import org.hibernate.exception.ConstraintViolationException;

import java.sql.SQLException;

/**
 * Traduccion <b>defensiva</b> de la violacion del EXCLUDE {@code ex_horario_operacion_vigencia}
 * (PostgreSQL SQLSTATE {@code 23P01}) a un error de dominio con codigo estable.
 *
 * <p>El EXCLUDE es el <b>backstop final</b> de la invariante de no-solape, no el control de flujo
 * normal: con el lock de {@code Salon} y la clasificacion de edge cases, el camino normal produce
 * errores de dominio legibles y este traductor no llega a dispararse. Existe porque el lock solo
 * serializa a quien pasa por el service, mientras que el EXCLUDE protege la tabla siempre.
 *
 * <p>Regla estricta: <b>nunca</b> se traduce un {@code DataIntegrityViolationException} generico.
 * Solo se traduce cuando en la cadena de causas se identifica SQLSTATE {@code 23P01} y, cuando el
 * driver lo reporta, el constraint esperado. Cualquier otra violacion de integridad se relanza
 * intacta: ocultarla detras de "conflicto de horario" haria indiagnosticable un bug distinto.
 */
public final class ConflictoVigenciaHorarioTranslator {

    static final String CONSTRAINT = "ex_horario_operacion_vigencia";
    static final String SQLSTATE_EXCLUSION_VIOLATION = "23P01";
    public static final String CONFLICTO_VIGENCIA_HORARIO =
            "CONFLICTO_VIGENCIA_HORARIO: la vigencia solicitada se solapa con otra versión del "
                    + "horario de ese salón y día";

    private ConflictoVigenciaHorarioTranslator() {
    }

    /**
     * Envuelve la escritura: si falla con la violacion del EXCLUDE, la traduce; en cualquier otro
     * caso relanza la excepcion original sin tocarla.
     *
     * <p>El {@code flush} que puede disparar {@code 23P01} debe ocurrir <b>dentro</b> de este
     * bloque. Si se dejara al commit del proxy de Spring, la excepcion se lanzaria fuera del
     * metodo y ningun {@code catch} del writer la veria.
     */
    public static <T> T traduciendoConflictoDeVigencia(Escritura<T> escritura) {
        try {
            return escritura.ejecutar();
        } catch (RuntimeException e) {
            if (esViolacionDelExcludeDeVigencia(e)) {
                // Solo se lanza: tras un error JDBC la sesion de Hibernate queda inconsistente y no
                // debe seguir usandose. Al ser RuntimeException, Spring mantiene el rollback.
                throw new ConflictException(CONFLICTO_VIGENCIA_HORARIO);
            }
            throw e;
        }
    }

    /**
     * Recorre la cadena de causas buscando la violacion del EXCLUDE. Se inspecciona primero el
     * {@link ConstraintViolationException} de Hibernate y no la {@code PSQLException} del driver,
     * para no acoplar {@code ubicaciones} a PostgreSQL.
     *
     * <p>Si Hibernate identifico el constraint y NO es el esperado, la respuesta es firme: no se
     * traduce, y no se cae al barrido de {@link SQLException}. Ese respaldo existe solo para el
     * caso en que Hibernate no envolvio el error y por tanto no hay nombre de constraint que
     * consultar; {@code 23P01} es {@code exclusion_violation} y el esquema declara un unico
     * EXCLUDE (V45), asi que ahi la atribucion es inequivoca.
     */
    static boolean esViolacionDelExcludeDeVigencia(Throwable error) {
        for (Throwable causa = error; causa != null && causa.getCause() != causa; causa = causa.getCause()) {
            if (causa instanceof ConstraintViolationException cve) {
                if (!SQLSTATE_EXCLUSION_VIOLATION.equals(cve.getSQLState())) {
                    return false;
                }
                return cve.getConstraintName() == null || CONSTRAINT.equals(cve.getConstraintName());
            }
            if (causa instanceof SQLException sqlException) {
                return SQLSTATE_EXCLUSION_VIOLATION.equals(sqlException.getSQLState());
            }
        }
        return false;
    }

    @FunctionalInterface
    public interface Escritura<T> {
        T ejecutar();
    }
}
