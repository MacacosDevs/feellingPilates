package com.feelingpilates.ubicaciones.servicio;

import com.feelingpilates.exception.ConflictException;
import com.feelingpilates.exception.ResourceNotFoundException;
import com.feelingpilates.exception.ValidacionException;
import com.feelingpilates.seguridad.AutorizadorSalon;
import com.feelingpilates.ubicaciones.dominio.CambioExcepcionHorario;
import com.feelingpilates.ubicaciones.dominio.ValidadorImpactoExcepcionHorario;
import com.feelingpilates.ubicaciones.dto.GuardarExcepcionSalonPorFechaRequest;
import com.feelingpilates.ubicaciones.dto.GuardarExcepcionSalonRequest;
import com.feelingpilates.ubicaciones.dto.SalonHorarioExcepcionResponse;
import com.feelingpilates.ubicaciones.entidad.Salon;
import com.feelingpilates.ubicaciones.entidad.SalonHorarioExcepcion;
import com.feelingpilates.ubicaciones.repositorio.SalonHorarioExcepcionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import static com.feelingpilates.ubicaciones.servicio.ConflictoExcepcionHorarioTranslator.traduciendoConflictoDeExcepcion;

/**
 * Excepciones puntuales al horario semanal de un salon: cerrado por festivo, u horario especial esa
 * fecha. Endurecido en F2C.2 con el protocolo compartido F2B: {@link Clock} central, {@link SalonLock}
 * y validacion inversa de programacion puntual antes de cualquier escritura.
 *
 * <p>Semantica REPLACEMENT: como maximo una fila {@code activo = true} por {@code (salon, fecha)}
 * (indice unico parcial {@code idx_salon_horario_excepcion_unica}). Activa + contenido igual es
 * NO-OP real; activa + contenido distinto actualiza LA MISMA fila (sin historial de ediciones
 * intermedias); solo filas inactivas siempre insertan una fila nueva, nunca reactivan una vieja.
 *
 * <p>Es tambien la capa de traduccion HTTP (igual que {@link SalonHorarioOperacionService} para el
 * horario semanal): los dos codigos 409 de {@link SalonHorarioExcepcionErrores} se traducen aqui de
 * {@link ValidacionException} a {@link ConflictException}; cualquier otro codigo sigue siendo 400.
 */
@Service
@Transactional
public class SalonHorarioExcepcionService {

    private final SalonHorarioExcepcionRepository excepcionRepository;
    private final SalonLock salonLock;
    private final AutorizadorSalon autorizadorSalon;
    private final HorarioOperacionResolver horarioOperacionResolver;
    private final List<ValidadorImpactoExcepcionHorario> validadoresDeImpacto;
    private final Clock reloj;

    public SalonHorarioExcepcionService(
            SalonHorarioExcepcionRepository excepcionRepository,
            SalonLock salonLock,
            AutorizadorSalon autorizadorSalon,
            HorarioOperacionResolver horarioOperacionResolver,
            List<ValidadorImpactoExcepcionHorario> validadoresDeImpacto,
            Clock reloj) {
        this.excepcionRepository = excepcionRepository;
        this.salonLock = salonLock;
        this.autorizadorSalon = autorizadorSalon;
        this.horarioOperacionResolver = horarioOperacionResolver;
        this.validadoresDeImpacto = validadoresDeImpacto;
        this.reloj = reloj;
    }

    @Transactional(readOnly = true)
    public List<SalonHorarioExcepcionResponse> listarPorRango(
            UUID actorId, UUID salonId, LocalDate desde, LocalDate hasta) {
        autorizadorSalon.verificarAccesoSalon(actorId, "salon.leer", salonId);
        return excepcionRepository.findBySalonIdAndFechaBetweenAndActivoTrueOrderByFecha(salonId, desde, hasta)
                .stream()
                .map(this::aResponse)
                .toList();
    }

    /** Legacy: la fecha viaja en el body. Converge en {@link #upsert} con la API por fecha. */
    public SalonHorarioExcepcionResponse guardar(
            UUID actorId, UUID salonId, GuardarExcepcionSalonRequest request) {
        return traduciendoConflictosDeEstado(() -> upsert(
                actorId, salonId, request.fecha(), request.cerrado(), request.horaApertura(), request.horaCierre()));
    }

    /** La fecha del path es autoritativa; el body no la repite. Converge en {@link #upsert} con el legacy. */
    public SalonHorarioExcepcionResponse guardarPorFecha(
            UUID actorId, UUID salonId, LocalDate fecha, GuardarExcepcionSalonPorFechaRequest request) {
        return traduciendoConflictosDeEstado(() -> upsert(
                actorId, salonId, fecha, request.cerrado(), request.horaApertura(), request.horaCierre()));
    }

    /**
     * Legacy DELETE por id. Orden de seguridad corregido (F2C.2): autorizar sobre el salon
     * CONTEXTUAL antes de leer nada, y no distinguir por status "no existe" de "existe en otro
     * salón" (misma 404 en ambos casos), para no revelar informacion cross-salon.
     */
    public void eliminar(UUID actorId, UUID salonIdContextual, UUID id) {
        traduciendoConflictosDeEstado(() -> {
            autorizadorSalon.verificarAccesoSalon(actorId, "salon.administrar", salonIdContextual);
            salonLock.adquirir(salonIdContextual);
            SalonHorarioExcepcion excepcion = excepcionRepository.findById(id)
                    .filter(SalonHorarioExcepcion::isActivo)
                    .filter(e -> e.getSalon().getId().equals(salonIdContextual))
                    .orElseThrow(() -> new ResourceNotFoundException(
                            SalonHorarioExcepcionErrores.EXCEPCION_HORARIO_NO_EXISTE));
            cancelar(excepcion);
            return null;
        });
    }

    /** Nuevo: cancela por {@code (salonId, fecha)}. Converge en la misma {@link #cancelar} que el legacy. */
    public void eliminarPorFecha(UUID actorId, UUID salonId, LocalDate fecha) {
        traduciendoConflictosDeEstado(() -> {
            autorizadorSalon.verificarAccesoSalon(actorId, "salon.administrar", salonId);
            salonLock.adquirir(salonId);
            SalonHorarioExcepcion excepcion = excepcionRepository
                    .findBySalonIdAndFechaAndActivoTrue(salonId, fecha)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            SalonHorarioExcepcionErrores.EXCEPCION_HORARIO_NO_EXISTE));
            cancelar(excepcion);
            return null;
        });
    }

    /**
     * Caso de uso interno compartido por legacy y por-fecha. Protocolo: autorizar -&gt; validar
     * forma/temporalidad (puro, sin BD) -&gt; {@link SalonLock} -&gt; leer la activa -&gt;
     * no-op/impacto/persistir. El lock se mantiene durante la lectura de la activa, la validacion de
     * impacto y la persistencia.
     */
    private SalonHorarioExcepcionResponse upsert(
            UUID actorId, UUID salonId, LocalDate fecha, boolean cerrado, LocalTime horaApertura, LocalTime horaCierre) {
        autorizadorSalon.verificarAccesoSalon(actorId, "salon.administrar", salonId);
        validarFormaDelEstado(cerrado, horaApertura, horaCierre);
        if (fecha.isBefore(LocalDate.now(reloj))) {
            throw new ValidacionException(SalonHorarioExcepcionErrores.EXCEPCION_HORARIO_EN_EL_PASADO);
        }

        LocalTime aperturaNormalizada = cerrado ? null : horaApertura;
        LocalTime cierreNormalizado = cerrado ? null : horaCierre;

        // Lock ANTES de leer: la decision no-op/update/insert se toma sobre estado ya serializado.
        Salon salon = salonLock.adquirir(salonId);
        Optional<SalonHorarioExcepcion> activa =
                excepcionRepository.findBySalonIdAndFechaAndActivoTrue(salonId, fecha);

        if (activa.isPresent() && mismoContenido(activa.get(), cerrado, aperturaNormalizada, cierreNormalizado)) {
            // NO-OP real: ni save ni saveAndFlush. actualizado_en no cambia.
            return aResponse(activa.get());
        }

        // Politica A puntual antes de cualquier escritura: un rechazo no persiste nada.
        CambioExcepcionHorario cambio = cerrado
                ? CambioExcepcionHorario.cerrado(salonId, fecha)
                : CambioExcepcionHorario.horarioEspecial(salonId, fecha, aperturaNormalizada, cierreNormalizado);
        SalonHorarioExcepcionErrores.verificarSinImpactoPuntual(validadoresDeImpacto, cambio);

        // Sin fila activa (aunque existan inactivas) -> SIEMPRE fila nueva, nunca reactivacion.
        SalonHorarioExcepcion excepcion = activa.orElseGet(SalonHorarioExcepcion::new);
        excepcion.setSalon(salon);
        excepcion.setFecha(fecha);
        excepcion.setCerrado(cerrado);
        excepcion.setHoraApertura(aperturaNormalizada);
        excepcion.setHoraCierre(cierreNormalizado);
        excepcion.setActivo(true);

        // El flush va DENTRO del traductor para que un 23505 sea capturable aqui.
        return traduciendoConflictoDeExcepcion(() -> aResponse(excepcionRepository.saveAndFlush(excepcion)));
    }

    /**
     * Cancelacion compartida por legacy y por-fecha, sobre una excepcion activa ya localizada y ya
     * confirmada como perteneciente al salon correcto.
     *
     * <p>CRITICO (F2C.2): la validacion de impacto NO se evalua contra la excepcion que se esta
     * retirando, sino contra el horario que REGIRIA despues de retirarla (semanal vigente, o
     * NO_OPERATIVO si no hay version semanal). Cancelar no puede invalidar en silencio programacion
     * puntual que hoy cabe en la excepcion pero no cabria en ese horario resultante.
     */
    private void cancelar(SalonHorarioExcepcion excepcion) {
        if (excepcion.getFecha().isBefore(LocalDate.now(reloj))) {
            throw new ValidacionException(SalonHorarioExcepcionErrores.EXCEPCION_HORARIO_EN_EL_PASADO);
        }

        CambioExcepcionHorario cambioResultante =
                cambioTrasCancelar(excepcion.getSalon().getId(), excepcion.getFecha());
        SalonHorarioExcepcionErrores.verificarSinImpactoPuntual(validadoresDeImpacto, cambioResultante);

        excepcion.setActivo(false);
        excepcionRepository.save(excepcion);
    }

    /**
     * El estado que regiria {@code (salonId, fecha)} si la excepcion activa dejara de existir: la
     * version semanal vigente ese dia, representada como un {@code HORARIO_ESPECIAL} equivalente
     * (mismo admite: contencion completa), o {@code CERRADO} (admite nada, igual que NO_OPERATIVO)
     * si no hay ninguna version semanal vigente.
     */
    private CambioExcepcionHorario cambioTrasCancelar(UUID salonId, LocalDate fecha) {
        return horarioOperacionResolver.resolver(salonId, fecha)
                .map(h -> CambioExcepcionHorario.horarioEspecial(salonId, fecha, h.getHoraApertura(), h.getHoraCierre()))
                .orElseGet(() -> CambioExcepcionHorario.cerrado(salonId, fecha));
    }

    private void validarFormaDelEstado(boolean cerrado, LocalTime horaApertura, LocalTime horaCierre) {
        if (cerrado) {
            return;
        }
        if (horaApertura == null || horaCierre == null) {
            throw new ValidacionException(SalonHorarioExcepcionErrores.HORARIO_ESPECIAL_INCOMPLETO);
        }
        if (!horaCierre.isAfter(horaApertura)) {
            throw new ValidacionException(SalonHorarioExcepcionErrores.HORA_CIERRE_DEBE_SER_POSTERIOR);
        }
    }

    /** Compara SOLO el contenido de negocio (cerrado/horas), nunca id/timestamps/activo/salon. */
    private boolean mismoContenido(
            SalonHorarioExcepcion existente, boolean cerrado, LocalTime horaApertura, LocalTime horaCierre) {
        return existente.isCerrado() == cerrado
                && Objects.equals(existente.getHoraApertura(), horaApertura)
                && Objects.equals(existente.getHoraCierre(), horaCierre);
    }

    /**
     * Traduce una {@code ValidacionException} whitelisted (impacto puntual o backstop del indice
     * unico) a {@code ConflictException}, conservando el mismo mensaje -y por tanto el mismo codigo
     * estable-. Cualquier otra {@code ValidacionException} sigue siendo 400 (whitelist cerrada).
     */
    private <T> T traduciendoConflictosDeEstado(Supplier<T> operacion) {
        try {
            return operacion.get();
        } catch (ValidacionException e) {
            if (SalonHorarioExcepcionErrores.esConflictoDeEstado(e.getMessage())) {
                throw new ConflictException(e.getMessage());
            }
            throw e;
        }
    }

    private SalonHorarioExcepcionResponse aResponse(SalonHorarioExcepcion e) {
        return new SalonHorarioExcepcionResponse(e.getId(), e.getFecha(), e.isCerrado(), e.getHoraApertura(), e.getHoraCierre());
    }
}
