package com.feelingpilates.ubicaciones.dominio;

import java.util.List;

/**
 * Port de validacion inversa: permite que los writers de horario de {@code ubicaciones} pregunten
 * "¿que programacion existente quedaria incompatible con este cambio?" sin conocer los modulos
 * que la guardan.
 *
 * <p>La direccion de dependencias del proyecto es {@code calendario -> ubicaciones} y
 * {@code programacion -> ubicaciones}, nunca al reves. Por eso el port se declara aqui y los
 * adapters viven en los modulos dependientes, que lo implementan como beans de Spring; el writer
 * inyecta {@code List<ValidadorImpactoCambioHorarioOperacion>} y no conoce ninguna implementacion.
 *
 * <p>Contrato:
 * <ul>
 *   <li>La evaluacion es <b>sincrona</b> y ocurre dentro de la transaccion del writer: no hay
 *       eventos, ni bus, ni asincronia. Si un adapter falla, la operacion completa revierte.</li>
 *   <li>Se invoca <b>antes</b> de cualquier escritura del writer. Politica A: si hay conflicto se
 *       rechaza el cambio de horario; nunca se modifica, recorta ni desactiva la programacion.</li>
 *   <li>Devolver lista vacia significa "sin impacto". Nunca {@code null}.</li>
 * </ul>
 */
public interface ValidadorImpactoCambioHorarioOperacion {

    /** Conflictos que provocaria {@code cambio}; lista vacia si ninguno. Nunca {@code null}. */
    List<ConflictoProgramacion> evaluar(CambioHorarioOperacion cambio);
}
