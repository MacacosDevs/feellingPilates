package com.feelingpilates.calendario.servicio;

import com.feelingpilates.calendario.dto.ReservaRequest;
import com.feelingpilates.calendario.dto.ReservaResponse;
import com.feelingpilates.calendario.entidad.Reserva;
import com.feelingpilates.calendario.entidad.TurnoInstructor;
import com.feelingpilates.calendario.repositorio.ReservaRepository;
import com.feelingpilates.calendario.repositorio.TurnoInstructorRepository;
import com.feelingpilates.exception.ResourceNotFoundException;
import com.feelingpilates.exception.ValidacionException;
import com.feelingpilates.seguridad.AutorizadorSalon;
import com.feelingpilates.ubicaciones.dominio.HorarioEfectivo;
import com.feelingpilates.ubicaciones.entidad.Salon;
import com.feelingpilates.ubicaciones.entidad.TipoActividad;
import com.feelingpilates.ubicaciones.repositorio.TipoActividadRepository;
import com.feelingpilates.ubicaciones.servicio.HorarioEfectivoSalon;
import com.feelingpilates.ubicaciones.servicio.SalonLock;
import com.feelingpilates.usuarios.entidad.Usuario;
import com.feelingpilates.usuarios.repositorio.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

/** Reservas de clientes con un instructor, dentro de un turno vigente de este. */
@Service
@Transactional
public class ReservaService {

    private final ReservaRepository reservaRepository;
    private final TurnoInstructorRepository turnoRepository;
    private final UsuarioRepository usuarioRepository;
    private final TipoActividadRepository tipoActividadRepository;
    private final AutorizadorSalon autorizadorSalon;
    private final SalonLock salonLock;
    private final HorarioEfectivoSalon horarioEfectivoSalon;

    public ReservaService(
            ReservaRepository reservaRepository,
            TurnoInstructorRepository turnoRepository,
            UsuarioRepository usuarioRepository,
            TipoActividadRepository tipoActividadRepository,
            AutorizadorSalon autorizadorSalon,
            SalonLock salonLock,
            HorarioEfectivoSalon horarioEfectivoSalon) {
        this.reservaRepository = reservaRepository;
        this.turnoRepository = turnoRepository;
        this.usuarioRepository = usuarioRepository;
        this.tipoActividadRepository = tipoActividadRepository;
        this.autorizadorSalon = autorizadorSalon;
        this.salonLock = salonLock;
        this.horarioEfectivoSalon = horarioEfectivoSalon;
    }

    @Transactional(readOnly = true)
    public List<ReservaResponse> listarPorSalonYFecha(UUID actorId, UUID salonId, LocalDate fecha) {
        autorizadorSalon.verificarAccesoSalon(actorId, "calendario.leer", salonId);
        return reservaRepository.findBySalonIdAndFechaAndEstado(salonId, fecha, Reserva.Estado.CONFIRMADA).stream()
                .map(this::aResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReservaResponse> listarPorCliente(UUID clienteId) {
        return reservaRepository.findByClienteIdAndEstadoOrderByFechaDescHoraInicioDesc(clienteId, Reserva.Estado.CONFIRMADA).stream()
                .map(this::aResponse)
                .toList();
    }

    public ReservaResponse crear(UUID actorId, ReservaRequest request) {
        autorizadorSalon.verificarAccesoSalon(actorId, "reserva.administrar", request.salonId());
        // SalonLock ANTES de leer: serializa esta reserva contra crear/modificar/cancelar una
        // excepcion de horario del mismo salon (F2C.2), para que no puedan commitear a la vez una
        // reserva y un cierre que la deja fuera de horario. Sustituye al findById redundante: el
        // lock ya comprueba existencia.
        Salon salon = salonLock.adquirir(request.salonId());
        Usuario instructor = usuarioRepository.findById(request.instructorId())
                .orElseThrow(() -> new ResourceNotFoundException("Instructor no encontrado"));
        Usuario cliente = usuarioRepository.findById(request.clienteId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));
        TipoActividad tipoActividad = tipoActividadRepository.findById(request.tipoActividadId())
                .orElseThrow(() -> new ResourceNotFoundException("Tipo de actividad no encontrado"));

        LocalTime horaInicio = request.horaInicio();
        LocalTime horaFin = horaInicio.plusMinutes(tipoActividad.getDuracionMinutos());

        // NO basta con el horario semanal: HorarioEfectivoSalon ya considera la excepcion de la
        // fecha. Cobertura COMPLETA, no solape; CERRADO y NO_OPERATIVO rechazan cualquier reserva.
        HorarioEfectivo horarioEfectivo = horarioEfectivoSalon.resolver(salon.getId(), request.fecha());
        if (!horarioEfectivo.contiene(horaInicio, horaFin)) {
            throw new ValidacionException("La reserva queda fuera del horario de atención del salón ese día");
        }

        if (instructor.getEspecialidades().stream().noneMatch(t -> t.getId().equals(tipoActividad.getId()))) {
            throw new ValidacionException("El instructor no está capacitado para impartir esa actividad");
        }

        List<TurnoInstructor> turnosVigentes = turnosVigentes(instructor.getId(), salon.getId(), request.fecha());
        boolean cabeEnTurno = turnosVigentes.stream()
                .anyMatch(t -> !horaInicio.isBefore(t.getHoraInicio()) && !horaFin.isAfter(t.getHoraFin()));
        if (!cabeEnTurno) {
            throw new ValidacionException("El instructor no tiene turno disponible en ese horario");
        }

        if (reservaRepository.existeTraslape(instructor.getId(), request.fecha(), horaInicio, horaFin, Reserva.Estado.CONFIRMADA)) {
            throw new ValidacionException("El instructor ya tiene una reserva en ese horario");
        }

        Reserva reserva = new Reserva();
        reserva.setSalon(salon);
        reserva.setInstructor(instructor);
        reserva.setCliente(cliente);
        reserva.setTipoActividad(tipoActividad);
        reserva.setFecha(request.fecha());
        reserva.setHoraInicio(horaInicio);
        reserva.setHoraFin(horaFin);

        return aResponse(reservaRepository.save(reserva));
    }

    public void cancelar(UUID actorId, UUID id) {
        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva no encontrada"));
        autorizadorSalon.verificarAccesoSalon(actorId, "reserva.administrar", reserva.getSalon().getId());
        reserva.setEstado(Reserva.Estado.CANCELADA);
        reservaRepository.save(reserva);
    }

    /**
     * Turnos que aplican para ese instructor/salon/fecha: si hay una cancelacion
     * para esa fecha exacta, no hay turnos disponibles ese dia. Si hay una o mas
     * excepciones para esa fecha, esas sobrescriben al turno recurrente. En otro
     * caso, aplica el turno recurrente de ese dia de la semana (si existe).
     */
    private List<TurnoInstructor> turnosVigentes(UUID instructorId, UUID salonId, LocalDate fecha) {
        List<TurnoInstructor> paraFecha = turnoRepository
                .buscarPuntualesPorInstructorSalonYFecha(instructorId, salonId, fecha);

        boolean cancelado = paraFecha.stream().anyMatch(t -> t.getTipo() == TurnoInstructor.Tipo.CANCELACION);
        if (cancelado) {
            return List.of();
        }

        List<TurnoInstructor> excepciones = paraFecha.stream()
                .filter(t -> t.getTipo() == TurnoInstructor.Tipo.EXCEPCION)
                .toList();
        if (!excepciones.isEmpty()) {
            return excepciones;
        }

        short diaSemana = (short) diaSemanaIso(fecha.getDayOfWeek());
        return turnoRepository.buscarRecurrentesPorInstructorSalonYDia(instructorId, salonId, diaSemana);
    }

    private int diaSemanaIso(DayOfWeek dayOfWeek) {
        return dayOfWeek == DayOfWeek.SUNDAY ? 0 : dayOfWeek.getValue();
    }

    private ReservaResponse aResponse(Reserva r) {
        return new ReservaResponse(
                r.getId(), r.getSalon().getId(), r.getSalon().getNombre(),
                r.getInstructor().getId(), r.getInstructor().getNombre(),
                r.getCliente().getId(), r.getCliente().getNombre(),
                r.getTipoActividad().getId(), r.getTipoActividad().getNombre(),
                r.getFecha(), r.getHoraInicio(), r.getHoraFin(), r.getEstado());
    }
}
