package com.feelingpilates.ubicaciones.servicio;

import com.feelingpilates.exception.CodigoErrorExtractor;
import com.feelingpilates.exception.ValidacionException;
import com.feelingpilates.ubicaciones.dominio.CambioExcepcionHorario;
import com.feelingpilates.ubicaciones.dominio.ConflictoProgramacionPuntual;
import com.feelingpilates.ubicaciones.dominio.ValidadorImpactoExcepcionHorario;
import com.feelingpilates.ubicaciones.entidad.SalonHorarioExcepcion;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Codigos de dominio estables del writer de {@link SalonHorarioExcepcion} y la ejecucion comun de
 * la validacion inversa puntual (Politica A). Analogo exacto de {@link HorarioOperacionErrores}
 * para el lado de excepciones por fecha.
 *
 * <p>Sigue la convencion del proyecto: el codigo estable es el prefijo del mensaje de la
 * {@link ValidacionException}.
 */
public final class SalonHorarioExcepcionErrores {

    /** {@code fecha} anterior a la fecha de negocio, en crear / modificar / cancelar. */
    public static final String EXCEPCION_HORARIO_EN_EL_PASADO =
            "EXCEPCION_HORARIO_EN_EL_PASADO: la fecha no puede ser anterior a hoy";

    /** {@code cerrado = false} sin hora de apertura o de cierre. */
    public static final String HORARIO_ESPECIAL_INCOMPLETO =
            "HORARIO_ESPECIAL_INCOMPLETO: un horario especial requiere hora de apertura y de cierre";

    /** {@code horaCierre <= horaApertura}. Reutilizado literalmente de {@link HorarioOperacionErrores}. */
    public static final String HORA_CIERRE_DEBE_SER_POSTERIOR =
            HorarioOperacionErrores.HORA_CIERRE_DEBE_SER_POSTERIOR;

    /** Cancelacion sin excepcion activa para {@code (salon, fecha)}, o id ajeno al salon. */
    public static final String EXCEPCION_HORARIO_NO_EXISTE =
            "EXCEPCION_HORARIO_NO_EXISTE: no existe una excepción activa para esa fecha";

    /** El port de impacto puntual reporta al menos un conflicto. */
    public static final String PROGRAMACION_PUNTUAL_INCOMPATIBLE_CON_EXCEPCION =
            "PROGRAMACION_PUNTUAL_INCOMPATIBLE_CON_EXCEPCION: el cambio dejaría programación puntual "
                    + "existente fuera del horario resultante";

    /** Backstop del indice unico parcial ({@code idx_salon_horario_excepcion_unica}, 23505). */
    public static final String CONFLICTO_EXCEPCION_HORARIO =
            "CONFLICTO_EXCEPCION_HORARIO: ya existe una excepción activa para ese salón y fecha";

    /**
     * Whitelist cerrada y explicita, igual que {@link HorarioOperacionErrores#esConflictoDeEstado}:
     * la capa HTTP traduce estos dos codigos a 409. Cualquier otro codigo de este catalogo sigue
     * siendo 400.
     */
    private static final Set<String> CONFLICTOS_DE_ESTADO = Set.of(
            CodigoErrorExtractor.extraer(PROGRAMACION_PUNTUAL_INCOMPATIBLE_CON_EXCEPCION),
            CodigoErrorExtractor.extraer(CONFLICTO_EXCEPCION_HORARIO));

    private SalonHorarioExcepcionErrores() {
    }

    /** true si el mensaje corresponde a un choque con el estado (impacto puntual o indice unico). */
    public static boolean esConflictoDeEstado(String mensaje) {
        String codigo = CodigoErrorExtractor.extraer(mensaje);
        return codigo != null && CONFLICTOS_DE_ESTADO.contains(codigo);
    }

    /**
     * Politica A puntual: consulta todos los validadores de impacto y rechaza si alguno reporta
     * conflicto. <b>No</b> modifica ni cancela la programacion puntual en conflicto: se rechaza el
     * cambio y el operador decide.
     *
     * <p>Debe invocarse <b>antes</b> de cualquier escritura del writer.
     */
    static void verificarSinImpactoPuntual(
            List<ValidadorImpactoExcepcionHorario> validadores, CambioExcepcionHorario cambio) {
        List<ConflictoProgramacionPuntual> conflictos = new ArrayList<>();
        for (ValidadorImpactoExcepcionHorario validador : validadores) {
            List<ConflictoProgramacionPuntual> encontrados = validador.evaluar(cambio);
            if (encontrados != null) {
                conflictos.addAll(encontrados);
            }
        }
        if (conflictos.isEmpty()) {
            return;
        }
        throw new ValidacionException(PROGRAMACION_PUNTUAL_INCOMPATIBLE_CON_EXCEPCION + ": "
                + conflictos.stream().map(ConflictoProgramacionPuntual::toString).collect(Collectors.joining(", ")));
    }
}
