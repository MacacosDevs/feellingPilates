package com.feelingpilates.ubicaciones.servicio;

import com.feelingpilates.exception.ValidacionException;
import com.feelingpilates.ubicaciones.dominio.CambioHorarioOperacion;
import com.feelingpilates.ubicaciones.dominio.ConflictoProgramacion;
import com.feelingpilates.ubicaciones.dominio.ValidadorImpactoCambioHorarioOperacion;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Codigos de dominio estables de los writers de {@code HorarioOperacion} y la ejecucion comun de
 * la validacion inversa (Politica A). Viven en un solo sitio para que
 * {@link VersionarHorarioOperacion} y {@link CerrarHorarioOperacion} no puedan divergir en el
 * codigo que emiten para la misma condicion.
 *
 * <p>Sigue la convencion del proyecto: el codigo estable es el prefijo del mensaje de la
 * {@link ValidacionException}.
 */
public final class HorarioOperacionErrores {

    /** {@code efectivoDesde} anterior a la fecha de negocio. Comun a versionar y cerrar. */
    public static final String EFECTIVO_DESDE_EN_EL_PASADO =
            "EFECTIVO_DESDE_EN_EL_PASADO: la fecha efectiva no puede ser anterior a hoy";

    /** {@code diaSemana} fuera de 0..6. Comun a versionar y cerrar. */
    public static final String DIA_SEMANA_INVALIDO =
            "DIA_SEMANA_INVALIDO: el día de la semana debe estar entre 0 y 6";

    /** {@code horaCierre} no posterior a {@code horaApertura}. Solo versionar. */
    public static final String HORA_CIERRE_DEBE_SER_POSTERIOR =
            "HORA_CIERRE_DEBE_SER_POSTERIOR: la hora de cierre debe ser posterior a la de apertura";

    /** Ya existe una version que empieza exactamente en {@code efectivoDesde}. Solo versionar. */
    public static final String YA_EXISTE_VERSION_EN_ESA_FECHA =
            "YA_EXISTE_VERSION_EN_ESA_FECHA: ya existe una versión del horario que inicia en esa fecha";

    /** Habria que partir el historial o reorganizar el futuro planificado. Solo versionar. */
    public static final String VERSIONADO_INTERMEDIO_NO_SOPORTADO =
            "VERSIONADO_INTERMEDIO_NO_SOPORTADO: no se admite insertar una versión intermedia ni "
                    + "reorganizar versiones futuras ya planificadas";

    /** Ninguna version cubre {@code efectivoDesde}. Solo cerrar. */
    public static final String NO_EXISTE_VERSION_VIGENTE_EN_ESA_FECHA =
            "NO_EXISTE_VERSION_VIGENTE_EN_ESA_FECHA: no hay una versión del horario vigente en esa fecha";

    /**
     * Cerrar en el inicio exacto de una version equivaldria a cancelar la version completa, que es
     * otra operacion. Es el <b>unico</b> codigo publico para {@code D == vigenteDesde} en cerrar.
     */
    public static final String CANCELACION_DE_VERSION_NO_SOPORTADA =
            "CANCELACION_DE_VERSION_NO_SOPORTADA: cerrar en la fecha de inicio de una versión "
                    + "equivaldría a cancelarla; no está soportado";

    /** Existe una version con {@code vigenteDesde > efectivoDesde}. Solo cerrar. */
    public static final String CIERRE_CON_VERSIONES_FUTURAS =
            "CIERRE_CON_VERSIONES_FUTURAS: existen versiones futuras planificadas para ese día; "
                    + "cerrar no las reorganiza ni las elimina";

    /** Politica A: el cambio dejaria programacion existente incompatible. Comun. */
    public static final String PROGRAMACION_INCOMPATIBLE_CON_HORARIO =
            "PROGRAMACION_INCOMPATIBLE_CON_HORARIO: el cambio dejaría programación existente fuera "
                    + "del horario del salón";

    private HorarioOperacionErrores() {
    }

    /**
     * Politica A: consulta todos los validadores de impacto y rechaza si alguno reporta conflicto.
     * <b>No</b> modifica, recorta ni desactiva la programacion en conflicto: se rechaza el cambio
     * de horario y el operador decide.
     *
     * <p>Debe invocarse <b>antes</b> de cualquier escritura del writer, para que un rechazo no
     * llegue siquiera a emitir el UPDATE.
     *
     * <p>El mensaje incluye tipo e IDs de lo que estorba para que el rechazo sea diagnosticable;
     * no se construye ningun DTO HTTP.
     */
    static void verificarSinImpacto(
            List<ValidadorImpactoCambioHorarioOperacion> validadores, CambioHorarioOperacion cambio) {
        List<ConflictoProgramacion> conflictos = new ArrayList<>();
        for (ValidadorImpactoCambioHorarioOperacion validador : validadores) {
            List<ConflictoProgramacion> encontrados = validador.evaluar(cambio);
            if (encontrados != null) {
                conflictos.addAll(encontrados);
            }
        }
        if (conflictos.isEmpty()) {
            return;
        }
        throw new ValidacionException(PROGRAMACION_INCOMPATIBLE_CON_HORARIO + ": "
                + conflictos.stream().map(ConflictoProgramacion::toString).collect(Collectors.joining(", ")));
    }
}
