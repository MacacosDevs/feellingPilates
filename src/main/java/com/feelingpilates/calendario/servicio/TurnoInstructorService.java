package com.feelingpilates.calendario.servicio;

import com.feelingpilates.calendario.dto.TurnoInstructorRequest;
import com.feelingpilates.calendario.dto.TurnoInstructorResponse;
import com.feelingpilates.calendario.entidad.TurnoInstructor;
import com.feelingpilates.calendario.repositorio.TurnoInstructorRepository;
import com.feelingpilates.exception.ResourceNotFoundException;
import com.feelingpilates.exception.ValidacionException;
import com.feelingpilates.ubicaciones.entidad.HorarioOperacion;
import com.feelingpilates.ubicaciones.entidad.Salon;
import com.feelingpilates.ubicaciones.entidad.SalonHorarioExcepcion;
import com.feelingpilates.ubicaciones.entidad.TipoActividad;
import com.feelingpilates.ubicaciones.repositorio.HorarioOperacionRepository;
import com.feelingpilates.ubicaciones.repositorio.SalonHorarioExcepcionRepository;
import com.feelingpilates.ubicaciones.repositorio.SalonRepository;
import com.feelingpilates.ubicaciones.repositorio.TipoActividadRepository;
import com.feelingpilates.usuarios.entidad.Rol;
import com.feelingpilates.usuarios.entidad.Usuario;
import com.feelingpilates.usuarios.repositorio.UsuarioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Turnos (recurrentes o por fecha) que cubre un instructor en un salon. */
@Service
@Transactional
public class TurnoInstructorService {

    private final TurnoInstructorRepository turnoRepository;
    private final UsuarioRepository usuarioRepository;
    private final SalonRepository salonRepository;
    private final HorarioOperacionRepository horarioOperacionRepository;
    private final TipoActividadRepository tipoActividadRepository;
    private final SalonHorarioExcepcionRepository salonHorarioExcepcionRepository;

    public TurnoInstructorService(
            TurnoInstructorRepository turnoRepository,
            UsuarioRepository usuarioRepository,
            SalonRepository salonRepository,
            HorarioOperacionRepository horarioOperacionRepository,
            TipoActividadRepository tipoActividadRepository,
            SalonHorarioExcepcionRepository salonHorarioExcepcionRepository) {
        this.turnoRepository = turnoRepository;
        this.usuarioRepository = usuarioRepository;
        this.salonRepository = salonRepository;
        this.horarioOperacionRepository = horarioOperacionRepository;
        this.tipoActividadRepository = tipoActividadRepository;
        this.salonHorarioExcepcionRepository = salonHorarioExcepcionRepository;
    }

    @Transactional(readOnly = true)
    public List<TurnoInstructorResponse> listarPorInstructorYSalon(UUID usuarioId, UUID salonId) {
        return turnoRepository.findByUsuarioIdAndSalonIdAndActivoTrue(usuarioId, salonId).stream()
                .map(this::aResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TurnoInstructorResponse> listarPorSalon(UUID salonId) {
        return turnoRepository.findBySalonIdAndActivoTrue(salonId).stream()
                .map(this::aResponse)
                .toList();
    }

    /** Turnos puntuales (EXCEPCION/CANCELACION) de un instructor, paginados y filtrables por tipo y día de la semana. */
    @Transactional(readOnly = true)
    public Page<TurnoInstructorResponse> listarPuntualesPaginado(
            UUID usuarioId, UUID salonId, TurnoInstructor.Tipo tipo, Integer diaSemana, Pageable pageable) {
        return turnoRepository
                .buscarPuntuales(usuarioId, salonId, tipo != null ? tipo.name() : null, diaSemana, pageable)
                .map(this::aResponse);
    }

    public TurnoInstructorResponse crear(TurnoInstructorRequest request) {
        Usuario usuario = usuarioRepository.findById(request.usuarioId())
                .orElseThrow(() -> new ResourceNotFoundException("Instructor no encontrado"));
        Salon salon = salonRepository.findById(request.salonId())
                .orElseThrow(() -> new ResourceNotFoundException("Salón no encontrado"));

        validarEsInstructorDelSalon(usuario, salon.getId());

        if (!request.horaFin().isAfter(request.horaInicio())) {
            throw new ValidacionException("La hora de fin debe ser posterior a la de inicio");
        }

        Short diaSemana = switch (request.tipo()) {
            case RECURRENTE -> {
                if (request.diaSemana() == null || request.fecha() != null) {
                    throw new ValidacionException("Un turno recurrente requiere día de la semana y no fecha");
                }
                yield request.diaSemana();
            }
            case EXCEPCION, CANCELACION -> {
                if (request.fecha() == null || request.diaSemana() != null) {
                    throw new ValidacionException("Una excepción o cancelación requiere fecha y no día de la semana");
                }
                yield (short) diaSemanaIso(request.fecha().getDayOfWeek());
            }
        };

        // CANCELACION cubre el día completo (00:00-23:59) como marcador de "no atiende",
        // no un rango real de horas: no tiene sentido validarlo contra el horario del salón.
        if (request.tipo() != TurnoInstructor.Tipo.CANCELACION) {
            validarDentroDeHorarioSalon(salon.getId(), diaSemana, request.fecha(), request.horaInicio(), request.horaFin());
            validarSinTraslape(
                    usuario.getId(), salon.getId(), request.tipo(), diaSemana, request.fecha(),
                    request.horaInicio(), request.horaFin(), null);
        }

        TipoActividad actividad = resolverActividad(usuario, request.tipoActividadId());

        TurnoInstructor turno = new TurnoInstructor();
        turno.setUsuario(usuario);
        turno.setSalon(salon);
        turno.setTipo(request.tipo());
        turno.setDiaSemana(request.tipo() == TurnoInstructor.Tipo.RECURRENTE ? request.diaSemana() : null);
        turno.setFecha(request.tipo() == TurnoInstructor.Tipo.RECURRENTE ? null : request.fecha());
        turno.setHoraInicio(request.horaInicio());
        turno.setHoraFin(request.horaFin());
        turno.setTipoActividad(actividad);

        return aResponse(turnoRepository.save(turno));
    }

    public void eliminar(UUID id) {
        TurnoInstructor turno = turnoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Turno no encontrado"));
        turno.setActivo(false);
        turnoRepository.save(turno);
    }

    /** Mueve un turno recurrente (dia y/u hora) y opcionalmente cambia su actividad. */
    public TurnoInstructorResponse actualizarTurno(
            UUID id, short diaSemana, LocalTime horaInicio, LocalTime horaFin, UUID tipoActividadId) {
        TurnoInstructor turno = turnoRepository.findById(id)
                .filter(TurnoInstructor::isActivo)
                .orElseThrow(() -> new ResourceNotFoundException("Turno no encontrado"));

        if (turno.getTipo() != TurnoInstructor.Tipo.RECURRENTE) {
            throw new ValidacionException("Solo los turnos recurrentes se pueden mover desde el calendario");
        }
        if (diaSemana < 0 || diaSemana > 6) {
            throw new ValidacionException("Día de la semana inválido");
        }
        if (!horaFin.isAfter(horaInicio)) {
            throw new ValidacionException("La hora de fin debe ser posterior a la de inicio");
        }

        validarDentroDeHorarioSalon(turno.getSalon().getId(), diaSemana, null, horaInicio, horaFin);
        validarSinTraslape(
                turno.getUsuario().getId(), turno.getSalon().getId(), TurnoInstructor.Tipo.RECURRENTE, diaSemana,
                null, horaInicio, horaFin, turno.getId());
        TipoActividad actividad = resolverActividad(turno.getUsuario(), tipoActividadId);

        turno.setDiaSemana(diaSemana);
        turno.setHoraInicio(horaInicio);
        turno.setHoraFin(horaFin);
        turno.setTipoActividad(actividad);
        return aResponse(turnoRepository.save(turno));
    }

    private TipoActividad resolverActividad(Usuario usuario, UUID tipoActividadId) {
        if (tipoActividadId == null) {
            return null;
        }
        TipoActividad actividad = tipoActividadRepository.findById(tipoActividadId)
                .orElseThrow(() -> new ResourceNotFoundException("Actividad no encontrada"));
        if (!usuario.getEspecialidades().contains(actividad)) {
            throw new ValidacionException("El instructor no tiene esa actividad entre sus especialidades");
        }
        return actividad;
    }

    private void validarEsInstructorDelSalon(Usuario usuario, UUID salonId) {
        boolean esInstructorDelSalon = usuario.getRoles().stream()
                .anyMatch(ur -> Rol.INSTRUCTOR.equals(ur.getRol().getNombre())
                        && (ur.getSalon() == null || ur.getSalon().getId().equals(salonId)));
        if (!esInstructorDelSalon) {
            throw new ValidacionException("El usuario no tiene el rol de instructor en ese salón");
        }
    }

    /**
     * Valida el rango de horas contra el horario del salon. Si se pasa una fecha (turnos
     * EXCEPCION), primero se revisa si el salon tiene una excepcion para esa fecha exacta:
     * si esta cerrado ese dia no hay horario valido; si tiene horario especial, se valida
     * contra ese en vez del patron semanal. Los turnos RECURRENTE (fecha null) siempre
     * validan contra el patron semanal, ya que definen una regla general, no una fecha.
     */
    private void validarDentroDeHorarioSalon(
            UUID salonId, short diaSemana, LocalDate fecha, LocalTime inicio, LocalTime fin) {
        if (fecha != null) {
            Optional<SalonHorarioExcepcion> excepcion =
                    salonHorarioExcepcionRepository.findBySalonIdAndFechaAndActivoTrue(salonId, fecha);
            if (excepcion.isPresent()) {
                SalonHorarioExcepcion e = excepcion.get();
                if (e.isCerrado()) {
                    throw new ValidacionException("El salón está cerrado ese día (" + fecha + ")");
                }
                if (inicio.isBefore(e.getHoraApertura()) || fin.isAfter(e.getHoraCierre())) {
                    throw new ValidacionException("El turno debe caer dentro del horario especial del salón ese día");
                }
                return;
            }
        }

        List<HorarioOperacion> horarios = horarioOperacionRepository.findBySalonIdOrderByDiaSemana(salonId);
        boolean cabeEnHorario = horarios.stream()
                .filter(h -> h.getDiaSemana() == diaSemana)
                .anyMatch(h -> !inicio.isBefore(h.getHoraApertura()) && !fin.isAfter(h.getHoraCierre()));
        if (!cabeEnHorario) {
            throw new ValidacionException("El turno debe caer dentro del horario de atención del salón ese día");
        }
    }

    /**
     * Un instructor no puede tener dos turnos que se traslapen en el mismo dia. RECURRENTE se
     * compara contra otros RECURRENTE de ese dia de la semana; EXCEPCION se compara contra otras
     * EXCEPCION de esa misma fecha (no contra el recurrente: una excepcion esta pensada para
     * redefinir/extender el horario de esa fecha puntual, no para sumarse a el).
     */
    private void validarSinTraslape(
            UUID usuarioId, UUID salonId, TurnoInstructor.Tipo tipo, short diaSemana, LocalDate fecha,
            LocalTime inicio, LocalTime fin, UUID excluirTurnoId) {
        List<TurnoInstructor> existentes = tipo == TurnoInstructor.Tipo.RECURRENTE
                ? turnoRepository.findByUsuarioIdAndSalonIdAndActivoTrueAndDiaSemana(usuarioId, salonId, diaSemana)
                : turnoRepository.findByUsuarioIdAndSalonIdAndActivoTrueAndFecha(usuarioId, salonId, fecha).stream()
                        .filter(t -> t.getTipo() == TurnoInstructor.Tipo.EXCEPCION)
                        .toList();

        boolean traslapa = existentes.stream()
                .filter(t -> !t.getId().equals(excluirTurnoId))
                .anyMatch(t -> inicio.isBefore(t.getHoraFin()) && fin.isAfter(t.getHoraInicio()));
        if (traslapa) {
            throw new ValidacionException("El instructor ya tiene otro turno que se encima con ese horario ese día");
        }
    }

    /** DayOfWeek de java (1=lunes..7=domingo) al formato del sistema (0=domingo..6=sabado). */
    private int diaSemanaIso(DayOfWeek dayOfWeek) {
        return dayOfWeek == DayOfWeek.SUNDAY ? 0 : dayOfWeek.getValue();
    }

    private TurnoInstructorResponse aResponse(TurnoInstructor t) {
        return new TurnoInstructorResponse(
                t.getId(), t.getUsuario().getId(), t.getUsuario().getNombre(),
                t.getSalon().getId(), t.getSalon().getNombre(),
                t.getTipo(), t.getDiaSemana(), t.getFecha(), t.getHoraInicio(), t.getHoraFin(),
                t.getTipoActividad() != null ? t.getTipoActividad().getId() : null,
                t.getTipoActividad() != null ? t.getTipoActividad().getNombre() : null);
    }
}
