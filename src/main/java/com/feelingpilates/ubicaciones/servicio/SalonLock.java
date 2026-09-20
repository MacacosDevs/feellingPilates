package com.feelingpilates.ubicaciones.servicio;

import com.feelingpilates.exception.ResourceNotFoundException;
import com.feelingpilates.ubicaciones.entidad.Salon;
import com.feelingpilates.ubicaciones.repositorio.SalonRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Punto de serializacion compartido por todos los writers que pueden crear una incompatibilidad
 * entre el horario de operacion de un salon y la programacion que depende de el
 * ({@code HorarioOperacion}, {@code BloqueProgramacion}, {@code TurnoInstructor} RECURRENTE).
 *
 * <p>Protocolo obligatorio: <b>adquirir el lock ANTES de leer el estado sobre el que se decide</b>.
 * La secuencia correcta es {@code lock -> leer horario -> validar -> persistir -> commit}. Validar
 * primero y bloquear despues no serializa nada: las dos transacciones habrian leido el estado viejo
 * y ambas podrian commitear lados incompatibles.
 *
 * <p>{@link Propagation#MANDATORY} y no {@code REQUIRED} deliberadamente: el lock debe vivir en la
 * transaccion del llamante y liberarse en su commit. Con {@code REQUIRED}, invocarlo fuera de
 * transaccion abriria una propia, tomaria el lock y lo soltaria de inmediato — un fallo silencioso
 * que dejaria el protocolo inutil sin que ningun test lo notara.
 *
 * <p>No usa {@code synchronized}, {@code ReentrantLock}, advisory locks ni locks distribuidos: el
 * {@code FOR UPDATE} vive en la base y funciona igual con varias instancias de la aplicacion.
 */
@Component
public class SalonLock {

    private final SalonRepository salonRepository;

    public SalonLock(SalonRepository salonRepository) {
        this.salonRepository = salonRepository;
    }

    /**
     * Bloquea la fila del salon hasta el commit de la transaccion en curso y la devuelve.
     *
     * <p>Devuelve la entidad ya bloqueada (en vez de {@code void}) para que el llamante que la
     * necesita no tenga que releerla con un {@code findById} redundante: este lock es tambien la
     * unica comprobacion de existencia que necesitan los writers de horario. Los llamantes que
     * solo quieren serializar pueden ignorar el retorno.
     *
     * @throws ResourceNotFoundException si el salon no existe.
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public Salon adquirir(UUID salonId) {
        if (salonId == null) {
            throw new ResourceNotFoundException("Salón no encontrado");
        }
        return salonRepository.bloquearParaActualizar(salonId)
                .orElseThrow(() -> new ResourceNotFoundException("Salón no encontrado"));
    }
}
