package com.feelingpilates.ubicaciones.servicio;

import com.feelingpilates.exception.ConflictException;
import org.hibernate.exception.ConstraintViolationException;

/**
 * Traduccion <b>defensiva</b> de la violacion del indice unico parcial
 * {@code idx_salon_horario_excepcion_unica} (PostgreSQL SQLSTATE {@code 23505}) a un error de
 * dominio con codigo estable.
 *
 * <p>Analogo exacto de {@link ConflictoVigenciaHorarioTranslator} para el 23505 en vez del 23P01:
 * el indice es el <b>backstop final</b> de "como maximo una fila activa por (salon, fecha)", no el
 * control de flujo normal. Con {@code SalonLock} y la clasificacion no-op/update/insert del writer,
 * el camino normal produce errores de dominio legibles y este traductor no llega a dispararse.
 *
 * <p>Regla estricta: <b>nunca</b> se traduce un {@code DataIntegrityViolationException} generico.
 * Solo se traduce cuando la cadena de causas contiene un {@link ConstraintViolationException} de
 * Hibernate cuyo SQLSTATE es {@code 23505} <b>y</b> cuyo {@code getConstraintName()} es exactamente
 * el esperado. Un SQLSTATE {@code 23505} sin ese constraint exacto (constraint distinto, ausente, o
 * un {@code SQLException} crudo sin envoltorio de Hibernate) no es evidencia suficiente: se relanza
 * intacta, igual que cualquier otra violacion de integridad. Ocultar esos casos detras de
 * "conflicto de excepcion" haria indiagnosticable un bug distinto.
 */
public final class ConflictoExcepcionHorarioTranslator {

    static final String CONSTRAINT = "idx_salon_horario_excepcion_unica";
    static final String SQLSTATE_UNIQUE_VIOLATION = "23505";

    private ConflictoExcepcionHorarioTranslator() {
    }

    /**
     * Envuelve la escritura: si falla con la violacion del indice unico parcial, la traduce; en
     * cualquier otro caso relanza la excepcion original sin tocarla.
     *
     * <p>El {@code flush} que puede disparar {@code 23505} debe ocurrir <b>dentro</b> de este
     * bloque (por eso {@code saveAndFlush}, no {@code save}). Si se dejara al commit del proxy de
     * Spring, la excepcion se lanzaria fuera del metodo y ningun {@code catch} del writer la veria.
     */
    public static <T> T traduciendoConflictoDeExcepcion(Escritura<T> escritura) {
        try {
            return escritura.ejecutar();
        } catch (RuntimeException e) {
            if (esViolacionDelIndiceUnico(e)) {
                // Solo se lanza: tras un error JDBC la sesion de Hibernate queda inconsistente y no
                // debe seguir usandose. Al ser RuntimeException, Spring mantiene el rollback.
                throw new ConflictException(SalonHorarioExcepcionErrores.CONFLICTO_EXCEPCION_HORARIO);
            }
            throw e;
        }
    }

    /**
     * Recorre la cadena de causas buscando la violacion del indice unico parcial. Se inspecciona
     * el {@link ConstraintViolationException} de Hibernate (no la {@code PSQLException} del
     * driver, para no acoplar {@code ubicaciones} a PostgreSQL) y unicamente esa fuente: es la
     * unica que expone {@code getConstraintName()} como metadata estructurada, sin parsear texto
     * del mensaje del driver.
     *
     * <p>Evidencia insuficiente para traducir: SQLSTATE {@code 23505} sin
     * {@link ConstraintViolationException} en la cadena (p. ej. un {@code SQLException} crudo),
     * con {@code getConstraintName() == null}, o con un nombre de constraint distinto al esperado.
     * En todos esos casos se relanza la excepcion original.
     */
    static boolean esViolacionDelIndiceUnico(Throwable error) {
        for (Throwable causa = error; causa != null && causa.getCause() != causa; causa = causa.getCause()) {
            if (causa instanceof ConstraintViolationException cve) {
                return SQLSTATE_UNIQUE_VIOLATION.equals(cve.getSQLState()) && CONSTRAINT.equals(cve.getConstraintName());
            }
        }
        return false;
    }

    @FunctionalInterface
    public interface Escritura<T> {
        T ejecutar();
    }
}
