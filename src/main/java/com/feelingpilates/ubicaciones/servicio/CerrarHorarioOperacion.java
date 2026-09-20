package com.feelingpilates.ubicaciones.servicio;

import com.feelingpilates.exception.ValidacionException;
import com.feelingpilates.ubicaciones.dominio.CambioHorarioOperacion;
import com.feelingpilates.ubicaciones.dominio.ValidadorImpactoCambioHorarioOperacion;
import com.feelingpilates.ubicaciones.entidad.HorarioOperacion;
import com.feelingpilates.ubicaciones.repositorio.HorarioOperacionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static com.feelingpilates.ubicaciones.servicio.ConflictoVigenciaHorarioTranslator.traduciendoConflictoDeVigencia;

/**
 * Cierra el horario semanal de {@code (salonId, diaSemana)}: el salon <b>deja de operar
 * recurrentemente ese dia desde {@code efectivoDesde} hacia +infinito</b>.
 *
 * <p>De esa semantica se deriva la regla del futuro planificado: el comando no puede producir un
 * cierre temporal seguido de una reapertura automatica. Si existe cualquier version del mismo
 * salon/dia con {@code vigenteDesde > efectivoDesde}, se <b>rechaza</b>; esas versiones no se
 * borran, no se recortan, no se cancelan y no se alteran de ninguna forma automatica.
 *
 * <p>No es una suspension temporal y nunca inserta una version sucesora.
 */
@Service
@Transactional
public class CerrarHorarioOperacion {

    private final SalonLock salonLock;
    private final HorarioOperacionRepository horarioOperacionRepository;
    private final List<ValidadorImpactoCambioHorarioOperacion> validadoresDeImpacto;
    private final Clock reloj;

    public CerrarHorarioOperacion(
            SalonLock salonLock,
            HorarioOperacionRepository horarioOperacionRepository,
            List<ValidadorImpactoCambioHorarioOperacion> validadoresDeImpacto,
            Clock reloj) {
        this.salonLock = salonLock;
        this.horarioOperacionRepository = horarioOperacionRepository;
        this.validadoresDeImpacto = validadoresDeImpacto;
        this.reloj = reloj;
    }

    public HorarioOperacion ejecutar(CerrarHorario comando) {
        validarEntrada(comando);

        // Lock antes de leer: la clasificacion temporal debe decidirse sobre estado serializado.
        // Si otra transaccion planifica una version futura, este comando la ve al despertar y la
        // rechaza, en vez de cerrar contra una foto vieja del dia.
        salonLock.adquirir(comando.salonId());
        List<HorarioOperacion> versiones = horarioOperacionRepository.bloquearVersionesQueIntersectan(
                comando.salonId(), comando.diaSemana(), comando.efectivoDesde(), null);

        HorarioOperacion aCerrar = clasificar(versiones, comando.efectivoDesde());

        HorarioOperacionErrores.verificarSinImpacto(validadoresDeImpacto, CambioHorarioOperacion.cerrado(
                comando.salonId(), comando.diaSemana(), comando.efectivoDesde()));

        return traduciendoConflictoDeVigencia(() -> {
            aCerrar.setVigenteHasta(comando.efectivoDesde().minusDays(1));
            // Un solo UPDATE: no aplica el hazard de orden de la ActionQueue. El flush se mantiene
            // explicito para que un eventual 23P01 sea capturable dentro del metodo.
            return horarioOperacionRepository.saveAndFlush(aCerrar);
        });
    }

    private void validarEntrada(CerrarHorario comando) {
        if (comando == null) {
            throw new ValidacionException("Los datos del cierre son obligatorios");
        }
        if (comando.diaSemana() < 0 || comando.diaSemana() > 6) {
            throw new ValidacionException(HorarioOperacionErrores.DIA_SEMANA_INVALIDO);
        }
        if (comando.efectivoDesde() == null) {
            throw new ValidacionException("La fecha efectiva del cierre es obligatoria");
        }
        // Una fecha pasada se rechaza ANTES de cualquier clasificacion temporal.
        if (comando.efectivoDesde().isBefore(LocalDate.now(reloj))) {
            throw new ValidacionException(HorarioOperacionErrores.EFECTIVO_DESDE_EN_EL_PASADO);
        }
    }

    /**
     * Clasificacion temporal, en el orden <b>a -> b -> c</b>, que es contractual y no incidental:
     *
     * <ol type="a">
     *   <li>ninguna version contiene D -> {@code NO_EXISTE_VERSION_VIGENTE_EN_ESA_FECHA};</li>
     *   <li>{@code D == vigenteDesde} de esa version -> {@code CANCELACION_DE_VERSION_NO_SOPORTADA};</li>
     *   <li>existe alguna version con {@code vigenteDesde > D} -> {@code CIERRE_CON_VERSIONES_FUTURAS}.</li>
     * </ol>
     *
     * <p>Primero las comprobaciones sobre la version a la que apunta el comando, despues el estado
     * del dia alrededor. Lo fija el caso "D en un gap habiendo ademas una version futura": cumple
     * las condiciones de (a) y de (c) a la vez y debe responder (a). Con este orden cada estado de
     * entrada produce un unico codigo estable.
     *
     * <p>La existencia de futuro se determina con {@code vigenteDesde != null && vigenteDesde > D}.
     * NO se usa {@code vigenteHasta is null} como sinonimo de "actual" ni de "futura": una fila
     * legada {@code NULL/NULL} lo cumple sin ser futura, y una version futura acotada no lo cumple
     * siendolo. La guarda {@code != null} importa: {@code vigenteDesde == null} es -infinito y
     * nunca es posterior a D.
     *
     * <p>En los tres rechazos no se emite ninguna sentencia de escritura: la clasificacion ocurre
     * antes incluso de la validacion inversa.
     */
    private HorarioOperacion clasificar(List<HorarioOperacion> versiones, LocalDate efectivoDesde) {
        HorarioOperacion contieneD = versiones.isEmpty() ? null : versiones.get(0);
        if (contieneD == null
                || (contieneD.getVigenteDesde() != null && contieneD.getVigenteDesde().isAfter(efectivoDesde))) {
            throw new ValidacionException(HorarioOperacionErrores.NO_EXISTE_VERSION_VIGENTE_EN_ESA_FECHA);
        }
        if (contieneD.getVigenteDesde() != null && contieneD.getVigenteDesde().isEqual(efectivoDesde)) {
            throw new ValidacionException(HorarioOperacionErrores.CANCELACION_DE_VERSION_NO_SOPORTADA);
        }
        boolean hayFuturas = versiones.stream()
                .anyMatch(v -> v.getVigenteDesde() != null && v.getVigenteDesde().isAfter(efectivoDesde));
        if (hayFuturas) {
            throw new ValidacionException(HorarioOperacionErrores.CIERRE_CON_VERSIONES_FUTURAS);
        }
        return contieneD;
    }

    /** Comando interno: no es un DTO HTTP. F2B.3b.1 no expone API. */
    public record CerrarHorario(UUID salonId, short diaSemana, LocalDate efectivoDesde) {
    }
}
