package com.feelingpilates.ubicaciones.dominio;

import java.util.List;

/**
 * Port de validacion inversa para excepciones puntuales de horario: permite que el writer de
 * {@code SalonHorarioExcepcion} en {@code ubicaciones} pregunte "¿que programacion puntual existente
 * quedaria incompatible con este cambio?" sin conocer los modulos que la guardan.
 *
 * <p>Analogo exacto de {@link ValidadorImpactoCambioHorarioOperacion}, pero para objetos
 * <b>puntuales</b> de una fecha ({@code TurnoInstructor.EXCEPCION}, {@code Reserva.CONFIRMADA}) en
 * vez de programacion recurrente. La direccion de dependencias sigue siendo
 * {@code calendario -> ubicaciones}: el port se declara aqui y el adapter vive en {@code calendario}.
 *
 * <p>Contrato:
 * <ul>
 *   <li>La evaluacion es <b>sincrona</b> y ocurre dentro de la transaccion del writer: si un
 *       adapter falla, la operacion completa revierte.</li>
 *   <li>Se invoca <b>antes</b> de cualquier escritura del writer. Si hay conflicto se rechaza el
 *       cambio; nunca se modifica, cancela ni desactiva la programacion puntual.</li>
 *   <li>Devolver lista vacia significa "sin impacto". Nunca {@code null}.</li>
 * </ul>
 */
public interface ValidadorImpactoExcepcionHorario {

    /** Conflictos que provocaria {@code cambio}; lista vacia si ninguno. Nunca {@code null}. */
    List<ConflictoProgramacionPuntual> evaluar(CambioExcepcionHorario cambio);
}
