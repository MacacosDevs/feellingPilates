package com.feelingpilates.programacion.servicio;

import com.feelingpilates.exception.ResourceNotFoundException;
import com.feelingpilates.exception.ValidacionException;
import com.feelingpilates.programacion.entidad.Asignacion;
import com.feelingpilates.programacion.entidad.BloqueProgramacion;
import com.feelingpilates.programacion.repositorio.AsignacionRepository;
import com.feelingpilates.programacion.repositorio.BloqueProgramacionRepository;
import com.feelingpilates.ubicaciones.entidad.HorarioOperacion;
import com.feelingpilates.ubicaciones.entidad.Salon;
import com.feelingpilates.ubicaciones.entidad.TipoActividad;
import com.feelingpilates.ubicaciones.repositorio.HorarioOperacionRepository;
import com.feelingpilates.ubicaciones.repositorio.SalonRepository;
import com.feelingpilates.ubicaciones.repositorio.TipoActividadRepository;
import com.feelingpilates.usuarios.entidad.Rol;
import com.feelingpilates.usuarios.entidad.Usuario;
import com.feelingpilates.usuarios.repositorio.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

/** Escritura acotada del modelo recurrente base de Programacion. */
@Service
@Transactional
public class BloqueProgramacionService {

    private final BloqueProgramacionRepository bloqueRepository;
    private final AsignacionRepository asignacionRepository;
    private final SalonRepository salonRepository;
    private final HorarioOperacionRepository horarioOperacionRepository;
    private final UsuarioRepository usuarioRepository;
    private final TipoActividadRepository tipoActividadRepository;

    public BloqueProgramacionService(
            BloqueProgramacionRepository bloqueRepository,
            AsignacionRepository asignacionRepository,
            SalonRepository salonRepository,
            HorarioOperacionRepository horarioOperacionRepository,
            UsuarioRepository usuarioRepository,
            TipoActividadRepository tipoActividadRepository) {
        this.bloqueRepository = bloqueRepository;
        this.asignacionRepository = asignacionRepository;
        this.salonRepository = salonRepository;
        this.horarioOperacionRepository = horarioOperacionRepository;
        this.usuarioRepository = usuarioRepository;
        this.tipoActividadRepository = tipoActividadRepository;
    }

    public BloqueProgramacion crearBloque(CrearBloque comando) {
        validarComandoBloque(comando);
        Salon salon = salonRepository.findById(comando.salonId())
                .orElseThrow(() -> new ResourceNotFoundException("Salón no encontrado"));
        if (!salon.isActivo()) {
            throw new ValidacionException("No se puede programar un bloque en un salón inactivo");
        }

        validarDentroDelHorarioOperacion(comando);
        if (!bloqueRepository.buscarTraslapesActivos(
                comando.salonId(), comando.diaSemana(), comando.horaInicio(), comando.horaFin(),
                comando.vigenteDesde(), comando.vigenteHasta()).isEmpty()) {
            throw new ValidacionException(
                    "El bloque se traslapa con otro bloque activo del salón para ese día y vigencia");
        }

        BloqueProgramacion bloque = new BloqueProgramacion();
        bloque.setSerieId(comando.serieId());
        bloque.setSalonId(comando.salonId());
        bloque.setDiaSemana(comando.diaSemana());
        bloque.setHoraInicio(comando.horaInicio());
        bloque.setHoraFin(comando.horaFin());
        bloque.setVigenteDesde(comando.vigenteDesde());
        bloque.setVigenteHasta(comando.vigenteHasta());
        bloque.setActivo(true);
        return bloqueRepository.save(bloque);
    }

    public Asignacion crearAsignacion(CrearAsignacion comando) {
        validarComandoAsignacion(comando);
        BloqueProgramacion bloque = bloqueRepository.findById(comando.bloqueId())
                .orElseThrow(() -> new ResourceNotFoundException("Bloque de programación no encontrado"));
        if (!bloque.isActivo()) {
            throw new ValidacionException("El bloque de programación no está activo");
        }

        Usuario instructor = usuarioRepository.findById(comando.instructorId())
                .orElseThrow(() -> new ResourceNotFoundException("Instructor no encontrado"));
        validarInstructorHabilitado(instructor, bloque.getSalonId());

        TipoActividad actividad = tipoActividadRepository.findById(comando.tipoActividadId())
                .orElseThrow(() -> new ResourceNotFoundException("Tipo de actividad no encontrado"));
        if (!actividad.isActivo()) {
            throw new ValidacionException("El tipo de actividad no está activo");
        }

        Salon salon = salonRepository.findById(bloque.getSalonId())
                .orElseThrow(() -> new ResourceNotFoundException("Salón del bloque no encontrado"));
        if (!contieneActividad(salon.getTiposActividad().stream().toList(), actividad.getId())) {
            throw new ValidacionException("El salón no ofrece ese tipo de actividad");
        }
        if (!contieneActividad(instructor.getEspecialidades().stream().toList(), actividad.getId())) {
            throw new ValidacionException("El instructor no tiene la especialidad para esa actividad");
        }

        validarContencionEnBloque(comando, bloque);
        validarSinTraslapeDentroDelBloque(comando);
        validarSinConflictoGlobal(comando, bloque.getDiaSemana());

        Asignacion asignacion = new Asignacion();
        asignacion.setSerieId(comando.serieId());
        asignacion.setBloqueId(comando.bloqueId());
        asignacion.setInstructorId(comando.instructorId());
        asignacion.setTipoActividadId(comando.tipoActividadId());
        asignacion.setHoraInicio(comando.horaInicio());
        asignacion.setHoraFin(comando.horaFin());
        asignacion.setVigenteDesde(comando.vigenteDesde());
        asignacion.setVigenteHasta(comando.vigenteHasta());
        asignacion.setActivo(true);
        return asignacionRepository.save(asignacion);
    }

    private void validarComandoBloque(CrearBloque comando) {
        if (comando == null) {
            throw new ValidacionException("Los datos del bloque son obligatorios");
        }
        requerir(comando.serieId(), "La serie del bloque es obligatoria");
        requerir(comando.salonId(), "El salón del bloque es obligatorio");
        requerir(comando.horaInicio(), "La hora de inicio del bloque es obligatoria");
        requerir(comando.horaFin(), "La hora de fin del bloque es obligatoria");
        requerir(comando.vigenteDesde(), "La vigencia inicial del bloque es obligatoria");
        if (comando.diaSemana() < 0 || comando.diaSemana() > 6) {
            throw new ValidacionException("El día de la semana debe estar entre 0 y 6");
        }
        validarRango(comando.horaInicio(), comando.horaFin());
        validarVigencia(comando.vigenteDesde(), comando.vigenteHasta());
    }

    private void validarComandoAsignacion(CrearAsignacion comando) {
        if (comando == null) {
            throw new ValidacionException("Los datos de la asignación son obligatorios");
        }
        requerir(comando.serieId(), "La serie de la asignación es obligatoria");
        requerir(comando.bloqueId(), "El bloque de la asignación es obligatorio");
        requerir(comando.instructorId(), "El instructor es obligatorio");
        requerir(comando.tipoActividadId(), "La actividad es obligatoria");
        requerir(comando.horaInicio(), "La hora de inicio de la asignación es obligatoria");
        requerir(comando.horaFin(), "La hora de fin de la asignación es obligatoria");
        requerir(comando.vigenteDesde(), "La vigencia inicial de la asignación es obligatoria");
        validarRango(comando.horaInicio(), comando.horaFin());
        validarVigencia(comando.vigenteDesde(), comando.vigenteHasta());
    }

    private void validarRango(LocalTime inicio, LocalTime fin) {
        if (!fin.isAfter(inicio)) {
            throw new ValidacionException("La hora de fin debe ser posterior a la hora de inicio");
        }
    }

    private void validarVigencia(LocalDate desde, LocalDate hasta) {
        if (hasta != null && hasta.isBefore(desde)) {
            throw new ValidacionException("La vigencia final no puede ser anterior a la inicial");
        }
    }

    private void validarDentroDelHorarioOperacion(CrearBloque comando) {
        List<HorarioOperacion> horarios = horarioOperacionRepository
                .findBySalonIdOrderByDiaSemana(comando.salonId());
        boolean contenido = horarios.stream()
                .filter(h -> h.getDiaSemana() != null && h.getDiaSemana() == comando.diaSemana())
                .anyMatch(h -> !comando.horaInicio().isBefore(h.getHoraApertura())
                        && !comando.horaFin().isAfter(h.getHoraCierre()));
        if (!contenido) {
            throw new ValidacionException("El bloque debe estar contenido en el horario de operación del salón");
        }
    }

    private void validarInstructorHabilitado(Usuario instructor, UUID salonId) {
        if (instructor.getEstatus() != Usuario.EstatusUsuario.activo) {
            throw new ValidacionException("El instructor no está habilitado");
        }
        boolean tieneRol = instructor.getRoles().stream()
                .anyMatch(ur -> Rol.INSTRUCTOR.equals(ur.getRol().getNombre())
                        && (ur.getSalon() == null || salonId.equals(ur.getSalon().getId())));
        if (!tieneRol) {
            throw new ValidacionException("El usuario no tiene el rol de instructor en ese salón");
        }
    }

    private boolean contieneActividad(List<TipoActividad> actividades, UUID actividadId) {
        return actividades.stream().anyMatch(a -> actividadId.equals(a.getId()));
    }

    private void validarContencionEnBloque(CrearAsignacion comando, BloqueProgramacion bloque) {
        if (comando.horaInicio().isBefore(bloque.getHoraInicio())
                || comando.horaFin().isAfter(bloque.getHoraFin())) {
            throw new ValidacionException("La asignación debe estar contenida en el horario del bloque");
        }
        boolean iniciaAntes = comando.vigenteDesde().isBefore(bloque.getVigenteDesde());
        boolean terminaDespues = bloque.getVigenteHasta() != null
                && (comando.vigenteHasta() == null
                    || comando.vigenteHasta().isAfter(bloque.getVigenteHasta()));
        if (iniciaAntes || terminaDespues) {
            throw new ValidacionException("La vigencia de la asignación debe estar contenida en la del bloque");
        }
    }

    private void validarSinTraslapeDentroDelBloque(CrearAsignacion comando) {
        boolean conflicto = asignacionRepository
                .findByBloqueIdAndActivoTrueOrderByHoraInicio(comando.bloqueId()).stream()
                .filter(a -> comando.instructorId().equals(a.getInstructorId()))
                .anyMatch(a -> rangosSeTraslapan(
                        comando.horaInicio(), comando.horaFin(), a.getHoraInicio(), a.getHoraFin())
                        && vigenciasSeIntersectan(
                                comando.vigenteDesde(), comando.vigenteHasta(),
                                a.getVigenteDesde(), a.getVigenteHasta()));
        if (conflicto) {
            throw new ValidacionException(
                    "El instructor ya tiene una asignación traslapada en el bloque para esa vigencia");
        }
    }

    private void validarSinConflictoGlobal(CrearAsignacion comando, short diaSemana) {
        if (!asignacionRepository.buscarConflictosRecurrentesDelInstructor(
                comando.instructorId(), diaSemana, comando.horaInicio(), comando.horaFin(),
                comando.vigenteDesde(), comando.vigenteHasta()).isEmpty()) {
            throw new ValidacionException(
                    "El instructor ya tiene una asignación recurrente traslapada ese día para esa vigencia");
        }
    }

    static boolean rangosSeTraslapan(LocalTime aInicio, LocalTime aFin, LocalTime bInicio, LocalTime bFin) {
        return aInicio.isBefore(bFin) && bInicio.isBefore(aFin);
    }

    static boolean vigenciasSeIntersectan(
            LocalDate aDesde, LocalDate aHasta, LocalDate bDesde, LocalDate bHasta) {
        return (bHasta == null || !aDesde.isAfter(bHasta))
                && (aHasta == null || !bDesde.isAfter(aHasta));
    }

    private void requerir(Object valor, String mensaje) {
        if (valor == null) {
            throw new ValidacionException(mensaje);
        }
    }

    public record CrearBloque(
            UUID serieId,
            UUID salonId,
            short diaSemana,
            LocalTime horaInicio,
            LocalTime horaFin,
            LocalDate vigenteDesde,
            LocalDate vigenteHasta) {
    }

    public record CrearAsignacion(
            UUID serieId,
            UUID bloqueId,
            UUID instructorId,
            UUID tipoActividadId,
            LocalTime horaInicio,
            LocalTime horaFin,
            LocalDate vigenteDesde,
            LocalDate vigenteHasta) {
    }
}
