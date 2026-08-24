package com.feelingpilates.ubicaciones.servicio;

import com.feelingpilates.exception.ValidacionException;
import com.feelingpilates.ubicaciones.dominio.CambioHorarioOperacion;
import com.feelingpilates.ubicaciones.dominio.ValidadorImpactoCambioHorarioOperacion;
import com.feelingpilates.ubicaciones.entidad.HorarioOperacion;
import com.feelingpilates.ubicaciones.entidad.Salon;
import com.feelingpilates.ubicaciones.repositorio.HorarioOperacionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static com.feelingpilates.ubicaciones.servicio.ConflictoVigenciaHorarioTranslator.traduciendoConflictoDeVigencia;

/**
 * Abre o reemplaza la vigencia del horario semanal de {@code (salonId, diaSemana)} a partir de
 * {@code efectivoDesde}. Es un comando conservador: solo opera sobre el <b>borde abierto</b> del
 * historial. Nunca reescribe el pasado, nunca parte una version en tres y nunca reorganiza
 * versiones futuras ya planificadas; esos son comandos distintos, fuera de esta fase.
 *
 * <p>Casos:
 * <ul>
 *   <li><b>Alta / reapertura</b> (ninguna version cubre D ni hay posteriores): inserta
 *       {@code D/NULL} sin tocar la historia. El gap anterior se preserva.</li>
 *   <li><b>Append</b> (una version cubre D, sin posteriores): la cierra en {@code D-1}
 *       conservando su {@code vigenteDesde}, e inserta {@code D/NULL}. La fila legada
 *       {@code NULL/NULL} pasa asi a {@code NULL/D-1} + {@code D/NULL}.</li>
 *   <li><b>Rechazos</b>: {@code D == vigenteDesde} de una version existente, y cualquier
 *       situacion que exigiria insertar en medio del historial o tocar el futuro planificado.</li>
 * </ul>
 */
@Service
@Transactional
public class VersionarHorarioOperacion {

    private final SalonLock salonLock;
    private final HorarioOperacionRepository horarioOperacionRepository;
    private final List<ValidadorImpactoCambioHorarioOperacion> validadoresDeImpacto;
    private final Clock reloj;

    public VersionarHorarioOperacion(
            SalonLock salonLock,
            HorarioOperacionRepository horarioOperacionRepository,
            List<ValidadorImpactoCambioHorarioOperacion> validadoresDeImpacto,
            Clock reloj) {
        this.salonLock = salonLock;
        this.horarioOperacionRepository = horarioOperacionRepository;
        this.validadoresDeImpacto = validadoresDeImpacto;
        this.reloj = reloj;
    }

    /**
     * Toda la operacion vive en UNA sola transaccion: sin {@code REQUIRES_NEW}, sin commit
     * intermedio y sin segunda transaccion. Los {@code flush} de {@link #persistirAppend} no son
     * commits; si algo falla despues del primero, revierte todo.
     */
    public HorarioOperacion ejecutar(VersionarHorario comando) {
        validarEntrada(comando);

        // El lock va ANTES de leer las versiones: es lo que hace que la clasificacion se decida
        // sobre estado ya serializado. Un precheck fuera del lock seguido de persistencia dentro
        // decidiria sobre datos viejos y permitiria commitear dos lados incompatibles.
        Salon salon = salonLock.adquirir(comando.salonId());
        List<HorarioOperacion> versiones = horarioOperacionRepository.bloquearVersionesQueIntersectan(
                comando.salonId(), comando.diaSemana(), comando.efectivoDesde(), null);

        HorarioOperacion aCerrar = clasificar(versiones, comando.efectivoDesde());

        // Politica A antes de cualquier escritura: un rechazo no persiste nada y ni siquiera emite
        // el UPDATE de cierre de la version anterior.
        HorarioOperacionErrores.verificarSinImpacto(validadoresDeImpacto, CambioHorarioOperacion.abierto(
                comando.salonId(), comando.diaSemana(), comando.efectivoDesde(),
                comando.horaApertura(), comando.horaCierre()));

        return persistirAppend(salon, comando, aCerrar);
    }

    private void validarEntrada(VersionarHorario comando) {
        if (comando == null) {
            throw new ValidacionException("Los datos del versionado son obligatorios");
        }
        if (comando.diaSemana() < 0 || comando.diaSemana() > 6) {
            throw new ValidacionException(HorarioOperacionErrores.DIA_SEMANA_INVALIDO);
        }
        if (comando.horaApertura() == null || comando.horaCierre() == null) {
            throw new ValidacionException("La apertura y el cierre del horario son obligatorios");
        }
        if (!comando.horaCierre().isAfter(comando.horaApertura())) {
            throw new ValidacionException(HorarioOperacionErrores.HORA_CIERRE_DEBE_SER_POSTERIOR);
        }
        if (comando.efectivoDesde() == null) {
            throw new ValidacionException("La fecha efectiva del versionado es obligatoria");
        }
        if (comando.efectivoDesde().isBefore(LocalDate.now(reloj))) {
            throw new ValidacionException(HorarioOperacionErrores.EFECTIVO_DESDE_EN_EL_PASADO);
        }
    }

    /**
     * Clasifica el estado del dia a partir de las versiones que intersectan {@code [D, +infinito)}
     * — es decir, la que contiene D (si existe) mas todas las posteriores, y nada del pasado.
     *
     * @return la version que hay que cerrar en {@code D-1}, o {@code null} si es alta/reapertura.
     */
    private HorarioOperacion clasificar(List<HorarioOperacion> versiones, LocalDate efectivoDesde) {
        if (versiones.isEmpty()) {
            // Alta (nunca hubo horario) y reapertura (toda la historia esta cerrada antes de D) se
            // tratan igual y no hace falta distinguirlas: en ambas solo se inserta D/NULL.
            return null;
        }

        HorarioOperacion primera = versiones.get(0);
        if (!contiene(primera, efectivoDesde)) {
            // D cae en un gap con al menos una version posterior: insertar aqui seria partir el
            // historial por la mitad.
            throw new ValidacionException(HorarioOperacionErrores.VERSIONADO_INTERMEDIO_NO_SOPORTADO);
        }
        boolean existeVersionQueInicaEnD = versiones.stream()
                .anyMatch(v -> v.getVigenteDesde() != null && v.getVigenteDesde().isEqual(efectivoDesde));
        if (existeVersionQueInicaEnD) {
            // Corregir las horas de una version existente es otra operacion: aqui NO se hace UPDATE
            // in-place, ni se cierra esa version en D-1 (dejaria un rango vacio invalido).
            throw new ValidacionException(HorarioOperacionErrores.YA_EXISTE_VERSION_EN_ESA_FECHA);
        }
        if (versiones.size() > 1) {
            // La version que contiene D tiene sucesoras planificadas: el append simple las dejaria
            // solapadas o exigiria reorganizarlas.
            throw new ValidacionException(HorarioOperacionErrores.VERSIONADO_INTERMEDIO_NO_SOPORTADO);
        }
        return primera;
    }

    /** {@code vigenteDesde == null} es -infinito, asi que contiene cualquier fecha. */
    private boolean contiene(HorarioOperacion version, LocalDate fecha) {
        return version.getVigenteDesde() == null || !version.getVigenteDesde().isAfter(fecha);
    }

    /**
     * Orden transaccional obligatorio: <b>UPDATE -> flush -> INSERT -> flush</b>.
     *
     * <p>No es cosmetico. Hibernate ordena su {@code ActionQueue} con {@code EntityInsertAction}
     * ANTES que {@code EntityUpdateAction}: dejado a su criterio, el INSERT de {@code D/NULL}
     * saldria con la version anterior todavia abierta en PostgreSQL, se solaparia con ella y el
     * EXCLUDE {@code ex_horario_operacion_vigencia} lo rechazaria — legitimamente. El flush
     * intermedio no confia en ese orden: lo <b>impone</b>, forzando que la version vieja ya este
     * cerrada fisicamente antes de insertar la nueva.
     *
     * <p>El flush intermedio NO es un commit: si el INSERT falla, la transaccion revierte entera y
     * la version vieja conserva su {@code vigenteHasta} original.
     */
    private HorarioOperacion persistirAppend(
            Salon salon, VersionarHorario comando, HorarioOperacion aCerrar) {
        return traduciendoConflictoDeVigencia(() -> {
            if (aCerrar != null) {
                aCerrar.setVigenteHasta(comando.efectivoDesde().minusDays(1));
                horarioOperacionRepository.saveAndFlush(aCerrar);
            }

            HorarioOperacion nueva = new HorarioOperacion();
            nueva.setSalon(salon);
            nueva.setDiaSemana(comando.diaSemana());
            nueva.setHoraApertura(comando.horaApertura());
            nueva.setHoraCierre(comando.horaCierre());
            nueva.setVigenteDesde(comando.efectivoDesde());
            nueva.setVigenteHasta(null);
            // El flush es explicito y dentro del try para que un 23P01 sea capturable aqui: si se
            // dejara al commit del proxy, la excepcion se lanzaria fuera del metodo.
            return horarioOperacionRepository.saveAndFlush(nueva);
        });
    }

    /** Comando interno: no es un DTO HTTP. F2B.3b.1 no expone API. */
    public record VersionarHorario(
            UUID salonId,
            short diaSemana,
            LocalDate efectivoDesde,
            LocalTime horaApertura,
            LocalTime horaCierre) {
    }
}
