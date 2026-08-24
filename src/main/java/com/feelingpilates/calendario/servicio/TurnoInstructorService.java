package com.feelingpilates.calendario.servicio;

import com.feelingpilates.calendario.dto.ActualizarTurnoRequest;
import com.feelingpilates.calendario.dto.AsignacionInstructorRequest;
import com.feelingpilates.calendario.dto.TurnoInstructorRequest;
import com.feelingpilates.calendario.dto.TurnoInstructorResponse;
import com.feelingpilates.calendario.entidad.TurnoInstructor;
import com.feelingpilates.calendario.entidad.TurnoInstructorAsignacion;
import com.feelingpilates.calendario.repositorio.TurnoInstructorAsignacionRepository;
import com.feelingpilates.calendario.repositorio.TurnoInstructorRepository;
import com.feelingpilates.exception.ResourceNotFoundException;
import com.feelingpilates.exception.ValidacionException;
import com.feelingpilates.seguridad.AutorizadorSalon;
import com.feelingpilates.ubicaciones.dominio.CoberturaVigencia;
import com.feelingpilates.ubicaciones.dominio.DiaSemanaOperacion;
import com.feelingpilates.ubicaciones.dominio.HorarioEfectivo;
import com.feelingpilates.ubicaciones.dominio.RangoVigencia;
import com.feelingpilates.ubicaciones.entidad.HorarioOperacion;
import com.feelingpilates.ubicaciones.entidad.Salon;
import com.feelingpilates.ubicaciones.entidad.TipoActividad;
import com.feelingpilates.ubicaciones.repositorio.HorarioOperacionRepository;
import com.feelingpilates.ubicaciones.repositorio.SalonRepository;
import com.feelingpilates.ubicaciones.repositorio.TipoActividadRepository;
import com.feelingpilates.ubicaciones.servicio.HorarioEfectivoSalon;
import com.feelingpilates.ubicaciones.servicio.SalonLock;
import com.feelingpilates.usuarios.entidad.Rol;
import com.feelingpilates.usuarios.entidad.Usuario;
import com.feelingpilates.usuarios.repositorio.UsuarioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** Bloques de horario (recurrentes o por fecha) de un salon, con uno o mas instructores y actividades. */
@Service
@Transactional
public class TurnoInstructorService {

    private final TurnoInstructorRepository turnoRepository;
    private final TurnoInstructorAsignacionRepository asignacionRepository;
    private final UsuarioRepository usuarioRepository;
    private final SalonRepository salonRepository;
    private final HorarioOperacionRepository horarioOperacionRepository;
    private final TipoActividadRepository tipoActividadRepository;
    private final HorarioEfectivoSalon horarioEfectivoSalon;
    private final AutorizadorSalon autorizadorSalon;
    private final SalonLock salonLock;
    private final Clock reloj;

    public TurnoInstructorService(
            TurnoInstructorRepository turnoRepository,
            TurnoInstructorAsignacionRepository asignacionRepository,
            UsuarioRepository usuarioRepository,
            SalonRepository salonRepository,
            HorarioOperacionRepository horarioOperacionRepository,
            TipoActividadRepository tipoActividadRepository,
            HorarioEfectivoSalon horarioEfectivoSalon,
            AutorizadorSalon autorizadorSalon,
            SalonLock salonLock,
            Clock reloj) {
        this.turnoRepository = turnoRepository;
        this.asignacionRepository = asignacionRepository;
        this.usuarioRepository = usuarioRepository;
        this.salonRepository = salonRepository;
        this.horarioOperacionRepository = horarioOperacionRepository;
        this.tipoActividadRepository = tipoActividadRepository;
        this.horarioEfectivoSalon = horarioEfectivoSalon;
        this.autorizadorSalon = autorizadorSalon;
        this.salonLock = salonLock;
        this.reloj = reloj;
    }

    @Transactional(readOnly = true)
    public List<TurnoInstructorResponse> listarPorInstructorYSalon(UUID usuarioId, UUID salonId) {
        return turnoRepository.buscarPorInstructorYSalon(usuarioId, salonId).stream()
                .map(this::aResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TurnoInstructorResponse> listarPorSalon(UUID salonId) {
        return turnoRepository.findBySalonIdAndActivoTrue(salonId).stream()
                .map(this::aResponse)
                .toList();
    }

    /** Bloques puntuales (EXCEPCION/CANCELACION) de un salón, paginados y filtrables por instructor, tipo y día. */
    @Transactional(readOnly = true)
    public Page<TurnoInstructorResponse> listarPuntualesPaginado(
            UUID salonId, UUID usuarioId, TurnoInstructor.Tipo tipo, Integer diaSemana, Pageable pageable) {
        return turnoRepository
                .buscarPuntuales(salonId, usuarioId, tipo != null ? tipo.name() : null, diaSemana, pageable)
                .map(this::aResponse);
    }

    public TurnoInstructorResponse crear(UUID actorId, TurnoInstructorRequest request) {
        String[] permisos = switch (request.tipo()) {
            case RECURRENTE -> new String[]{"calendario.gestionar"};
            case EXCEPCION -> new String[]{"calendario.gestionar", "calendario.editar"};
            case CANCELACION -> new String[]{"calendario.gestionar", "calendario.cancelar"};
        };
        autorizadorSalon.verificarAccesoSalon(actorId, request.salonId(), permisos);
        // Protocolo de lock compartido: un turno RECURRENTE es programacion abierta al futuro que
        // puede volverse incompatible con el horario del salon, asi que se serializa contra los
        // writers de horario ANTES de leer/validar nada de ese horario. La autorizacion va primero
        // a proposito: no se retiene un lock por peticiones que no tienen permiso.
        //
        // EXCEPCION y CANCELACION NO lo toman: EXCEPCION esta fuera de la Politica A (su invariante
        // ya no la mantiene el sistema hoy, ver ImpactoTurnosRecurrentesEnHorario) y CANCELACION ni
        // siquiera valida horario. Tomar el lock ahi daria falsa sensacion de proteccion.
        if (request.tipo() == TurnoInstructor.Tipo.RECURRENTE) {
            salonLock.adquirir(request.salonId());
        }
        Map<Usuario, AsignacionResuelta> asignaciones = resolverAsignaciones(request.asignaciones());
        Set<Usuario> instructores = new LinkedHashSet<>(asignaciones.keySet());
        Salon salon = salonRepository.findById(request.salonId())
                .orElseThrow(() -> new ResourceNotFoundException("Salón no encontrado"));

        instructores.forEach(usuario -> validarEsInstructorDelSalon(usuario, salon.getId()));

        if (!request.horaFin().isAfter(request.horaInicio())) {
            throw new ValidacionException("La hora de fin debe ser posterior a la de inicio");
        }
        validarRangosDeAsignaciones(asignaciones, request.horaInicio(), request.horaFin());

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
                yield DiaSemanaOperacion.desde(request.fecha().getDayOfWeek());
            }
        };

        // CANCELACION cubre el día completo (00:00-23:59) como marcador de "no atiende",
        // no un rango real de horas: no tiene sentido validarlo contra el horario del salón.
        if (request.tipo() != TurnoInstructor.Tipo.CANCELACION) {
            validarDentroDeHorarioSalon(salon.getId(), diaSemana, request.fecha(), request.horaInicio(), request.horaFin());
            validarSinTraslape(
                    salon.getId(), request.tipo(), diaSemana, request.fecha(),
                    request.horaInicio(), request.horaFin(), null);
        }

        TurnoInstructor turno = new TurnoInstructor();
        turno.setInstructores(instructores);
        turno.setSalon(salon);
        turno.setTipo(request.tipo());
        turno.setDiaSemana(request.tipo() == TurnoInstructor.Tipo.RECURRENTE ? request.diaSemana() : null);
        turno.setFecha(request.tipo() == TurnoInstructor.Tipo.RECURRENTE ? null : request.fecha());
        turno.setHoraInicio(request.horaInicio());
        turno.setHoraFin(request.horaFin());
        turno = turnoRepository.save(turno);
        List<TurnoInstructorAsignacion> filas = reemplazarAsignaciones(turno, asignaciones);

        return aResponse(turno, filas);
    }

    public void eliminar(UUID actorId, UUID id) {
        TurnoInstructor turno = turnoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Turno no encontrado"));
        autorizadorSalon.verificarAccesoSalon(actorId, "calendario.gestionar", turno.getSalon().getId());
        turno.setActivo(false);
        turnoRepository.save(turno);
    }

    /** Mueve un bloque recurrente (dia y/u hora) y opcionalmente cambia sus instructores/actividades. */
    public TurnoInstructorResponse actualizarTurno(UUID actorId, UUID id, ActualizarTurnoRequest request) {
        TurnoInstructor turno = turnoRepository.findById(id)
                .filter(TurnoInstructor::isActivo)
                .orElseThrow(() -> new ResourceNotFoundException("Turno no encontrado"));
        autorizadorSalon.verificarAccesoSalon(
                actorId, turno.getSalon().getId(), "calendario.gestionar", "calendario.editar");

        if (turno.getTipo() != TurnoInstructor.Tipo.RECURRENTE) {
            throw new ValidacionException("Solo los bloques recurrentes se pueden mover desde el calendario");
        }
        short diaSemana = request.diaSemana();
        if (diaSemana < 0 || diaSemana > 6) {
            throw new ValidacionException("Día de la semana inválido");
        }
        if (!request.horaFin().isAfter(request.horaInicio())) {
            throw new ValidacionException("La hora de fin debe ser posterior a la de inicio");
        }

        Map<Usuario, AsignacionResuelta> asignaciones = resolverAsignaciones(request.asignaciones());
        Set<Usuario> instructores = new LinkedHashSet<>(asignaciones.keySet());
        instructores.forEach(usuario -> validarEsInstructorDelSalon(usuario, turno.getSalon().getId()));
        validarRangosDeAsignaciones(asignaciones, request.horaInicio(), request.horaFin());

        // Lock ANTES de validar contra el horario. Cargar el turno antes del lock es correcto: el
        // salon de un turno no puede cambiarse (el request no lo lleva), asi que esa lectura es de
        // identidad, no del estado sobre el que se decide. Como el lock es por salon, una sola
        // adquisicion cubre tanto el dia de origen como el de destino del movimiento.
        salonLock.adquirir(turno.getSalon().getId());
        validarDentroDeHorarioSalon(turno.getSalon().getId(), diaSemana, null, request.horaInicio(), request.horaFin());
        validarSinTraslape(
                turno.getSalon().getId(), TurnoInstructor.Tipo.RECURRENTE, diaSemana, null,
                request.horaInicio(), request.horaFin(), turno.getId());

        turno.setDiaSemana(diaSemana);
        turno.setHoraInicio(request.horaInicio());
        turno.setHoraFin(request.horaFin());
        turno.setInstructores(instructores);
        TurnoInstructor guardado = turnoRepository.save(turno);
        List<TurnoInstructorAsignacion> filas = reemplazarAsignaciones(guardado, asignaciones);
        return aResponse(guardado, filas);
    }

    /** Actividades que da un instructor en el bloque y el lapso que cubre (null = bloque completo). */
    private record AsignacionResuelta(Set<TipoActividad> actividades, LocalTime horaInicio, LocalTime horaFin) {
    }

    /**
     * Resuelve, por instructor, que actividades especificas da en este bloque y que lapso cubre.
     * Valida que cada instructor y actividad existan, y que la actividad este entre las
     * especialidades del instructor (si no la tiene, no puede quedar asignado a darla en un turno).
     */
    private Map<Usuario, AsignacionResuelta> resolverAsignaciones(List<AsignacionInstructorRequest> asignaciones) {
        if (asignaciones == null || asignaciones.isEmpty()) {
            throw new ValidacionException("El bloque necesita al menos un instructor");
        }

        List<UUID> instructorIds = asignaciones.stream().map(AsignacionInstructorRequest::instructorId).toList();
        List<Usuario> usuarios = usuarioRepository.findAllById(instructorIds);
        if (usuarios.size() != new HashSet<>(instructorIds).size()) {
            throw new ResourceNotFoundException("Uno o más instructores no existen");
        }
        Map<UUID, Usuario> usuariosPorId = new HashMap<>();
        usuarios.forEach(u -> usuariosPorId.put(u.getId(), u));

        List<UUID> tipoActividadIds = asignaciones.stream()
                .flatMap(a -> a.tipoActividadIds() == null ? List.<UUID>of().stream() : a.tipoActividadIds().stream())
                .distinct()
                .toList();
        Map<UUID, TipoActividad> actividadesPorId = new HashMap<>();
        if (!tipoActividadIds.isEmpty()) {
            List<TipoActividad> actividades = tipoActividadRepository.findAllById(tipoActividadIds);
            if (actividades.size() != new HashSet<>(tipoActividadIds).size()) {
                throw new ResourceNotFoundException("Una o más actividades no existen");
            }
            actividades.forEach(a -> actividadesPorId.put(a.getId(), a));
        }

        Map<Usuario, AsignacionResuelta> resultado = new LinkedHashMap<>();
        for (AsignacionInstructorRequest asignacion : asignaciones) {
            Usuario usuario = usuariosPorId.get(asignacion.instructorId());
            Set<TipoActividad> actividadesInstructor = new HashSet<>();
            if (asignacion.tipoActividadIds() != null) {
                for (UUID tipoActividadId : asignacion.tipoActividadIds()) {
                    TipoActividad actividad = actividadesPorId.get(tipoActividadId);
                    if (!usuario.getEspecialidades().contains(actividad)) {
                        throw new ValidacionException(
                                "El usuario " + usuario.getNombre() + " no tiene la especialidad " + actividad.getNombre());
                    }
                    actividadesInstructor.add(actividad);
                }
            }
            if ((asignacion.horaInicio() == null) != (asignacion.horaFin() == null)) {
                throw new ValidacionException(
                        "El rango horario de " + usuario.getNombre() + " necesita ambas horas, o ninguna (tiempo completo)");
            }
            if (asignacion.horaInicio() != null && !asignacion.horaFin().isAfter(asignacion.horaInicio())) {
                throw new ValidacionException(
                        "El rango horario de " + usuario.getNombre() + " debe terminar después de que empieza");
            }
            resultado.put(usuario, new AsignacionResuelta(actividadesInstructor, asignacion.horaInicio(), asignacion.horaFin()));
        }
        return resultado;
    }

    /** Si un instructor tiene un rango propio (no "tiempo completo"), debe caer dentro del turno. */
    private void validarRangosDeAsignaciones(
            Map<Usuario, AsignacionResuelta> asignaciones, LocalTime turnoInicio, LocalTime turnoFin) {
        asignaciones.forEach((usuario, asignacion) -> {
            if (asignacion.horaInicio() == null) return;
            if (asignacion.horaInicio().isBefore(turnoInicio) || asignacion.horaFin().isAfter(turnoFin)) {
                throw new ValidacionException(
                        "El rango horario de " + usuario.getNombre() + " debe caer dentro del horario del bloque ("
                                + turnoInicio + " a " + turnoFin + ")");
            }
        });
    }

    /**
     * Reemplaza por completo las asignaciones instructor-actividad de un turno (borra y recrea).
     * Se hace por repositorio con flush entre el borrado y la inserción (no reemplazando la
     * colección de la entidad) porque las filas nuevas son instancias nuevas: si se dejara que
     * Hibernate difiera el reemplazo de la colección, encola los INSERT antes que los DELETE y
     * choca con la llave primaria compuesta cuando una fila coincide entre lo viejo y lo nuevo.
     */
    private List<TurnoInstructorAsignacion> reemplazarAsignaciones(
            TurnoInstructor turno, Map<Usuario, AsignacionResuelta> asignaciones) {
        asignacionRepository.deleteByTurno_Id(turno.getId());
        asignacionRepository.flush();
        List<TurnoInstructorAsignacion> nuevas = new ArrayList<>();
        asignaciones.forEach((usuario, asignacion) ->
                asignacion.actividades().forEach(actividad -> nuevas.add(new TurnoInstructorAsignacion(
                        turno, usuario, actividad, asignacion.horaInicio(), asignacion.horaFin()))));
        List<TurnoInstructorAsignacion> guardadas = asignacionRepository.saveAll(nuevas);
        asignacionRepository.flush();
        return guardadas;
    }

    private void validarEsInstructorDelSalon(Usuario usuario, UUID salonId) {
        boolean esInstructorDelSalon = usuario.getRoles().stream()
                .anyMatch(ur -> Rol.INSTRUCTOR.equals(ur.getRol().getNombre())
                        && (ur.getSalon() == null || ur.getSalon().getId().equals(salonId)));
        if (!esInstructorDelSalon) {
            throw new ValidacionException("El usuario " + usuario.getNombre() + " no tiene el rol de instructor en ese salón");
        }
    }

    /**
     * Valida el rango de horas contra el horario del salon. Un turno con fecha (EXCEPCION) se
     * valida contra el horario EFECTIVO de esa fecha; uno sin fecha (RECURRENTE) contra las
     * versiones del horario semanal que rigen de hoy en adelante.
     */
    private void validarDentroDeHorarioSalon(
            UUID salonId, short diaSemana, LocalDate fecha, LocalTime inicio, LocalTime fin) {
        if (fecha != null) {
            validarContraHorarioEfectivo(salonId, fecha, inicio, fin);
            return;
        }
        validarContraHorarioSemanalVigenteHaciaElFuturo(salonId, diaSemana, inicio, fin);
    }

    /**
     * Turno EXCEPCION: existe en una fecha concreta, asi que se resuelve el horario efectivo de
     * ese dia (excepcion puntual sobre plantilla semanal versionada) y el turno debe caber dentro.
     */
    private void validarContraHorarioEfectivo(UUID salonId, LocalDate fecha, LocalTime inicio, LocalTime fin) {
        HorarioEfectivo efectivo = horarioEfectivoSalon.resolver(salonId, fecha);
        if (efectivo.estaCerrado()) {
            throw new ValidacionException("El salón está cerrado ese día (" + fecha + ")");
        }
        if (efectivo.contiene(inicio, fin)) {
            return;
        }
        throw new ValidacionException(efectivo.vieneDeExcepcion()
                ? "El turno debe caer dentro del horario especial del salón ese día"
                : "El turno debe caer dentro del horario de atención del salón ese día");
    }

    /**
     * Turno RECURRENTE: no tiene vigencia propia, es una regla abierta al futuro. Por eso su
     * objetivo temporal es {@code [hoy, +infinito)} y no basta con que ALGUNA version del horario
     * semanal lo admita:
     *
     * <ol>
     *   <li>las versiones aplicables deben CUBRIR ese objetivo completo, sin huecos y llegando a
     *       +infinito (si la ultima version termina en fecha finita, el turno quedaria huerfano);</li>
     *   <li>y TODAS ellas deben contener el rango de horas, no solo la que rige hoy.</li>
     * </ol>
     *
     * Un salon abierto 08-20 hasta agosto y 09-20 desde septiembre rechaza un recurrente 08-09,
     * aunque hoy quepa.
     */
    private void validarContraHorarioSemanalVigenteHaciaElFuturo(
            UUID salonId, short diaSemana, LocalTime inicio, LocalTime fin) {
        LocalDate fechaNegocio = LocalDate.now(reloj);
        List<HorarioOperacion> versiones = horarioOperacionRepository
                .findVersionesQueIntersectan(salonId, diaSemana, fechaNegocio, null);

        RangoVigencia objetivo = new RangoVigencia(fechaNegocio, null);
        boolean cubierto = CoberturaVigencia.cubreCompletamente(objetivo, versiones.stream()
                .map(h -> new RangoVigencia(h.getVigenteDesde(), h.getVigenteHasta()))
                .toList());
        boolean todasLoAdmiten = !versiones.isEmpty() && versiones.stream()
                .allMatch(h -> !inicio.isBefore(h.getHoraApertura()) && !fin.isAfter(h.getHoraCierre()));

        if (!cubierto || !todasLoAdmiten) {
            throw new ValidacionException("El turno debe caer dentro del horario de atención del salón ese día");
        }
    }

    /**
     * Un salón es un espacio físico: no puede tener dos bloques que se traslapen en el mismo
     * horario, sin importar si comparten instructor o no. RECURRENTE se compara contra otros
     * RECURRENTE de ese dia de la semana; EXCEPCION se compara contra otras EXCEPCION de esa
     * misma fecha y también contra los RECURRENTE de ese día de la semana (un recurrente que no
     * fue reemplazado explícitamente sigue ocupando el salón esa fecha).
     */
    private void validarSinTraslape(
            UUID salonId, TurnoInstructor.Tipo tipo, short diaSemana, LocalDate fecha,
            LocalTime inicio, LocalTime fin, UUID excluirTurnoId) {
        List<TurnoInstructor> existentes = new ArrayList<>(turnoRepository.buscarRecurrentesPorSalonYDia(salonId, diaSemana));
        if (tipo == TurnoInstructor.Tipo.EXCEPCION) {
            existentes.addAll(turnoRepository.buscarExcepcionesPorSalonYFecha(salonId, fecha));
        }

        boolean traslapa = existentes.stream()
                .filter(t -> !t.getId().equals(excluirTurnoId))
                .anyMatch(t -> inicio.isBefore(t.getHoraFin()) && fin.isAfter(t.getHoraInicio()));
        if (traslapa) {
            throw new ValidacionException("Ese horario se cruza con otro bloque de este salón ese día");
        }
    }

    private TurnoInstructorResponse aResponse(TurnoInstructor t) {
        return aResponse(t, t.getAsignaciones());
    }

    /**
     * Variante para crear/actualizar: recibe las filas recien guardadas explicitamente en vez de
     * leerlas de {@code t.getAsignaciones()}, que en ese punto puede estar "stale" (ya se
     * inicializo vacia al crear la entidad o se cargo antes de insertar por otro repositorio).
     */
    private TurnoInstructorResponse aResponse(TurnoInstructor t, List<TurnoInstructorAsignacion> filasAsignacion) {
        Map<Usuario, List<TipoActividad>> actividadesPorInstructor = new LinkedHashMap<>();
        Map<Usuario, TurnoInstructorAsignacion> filaPorInstructor = new LinkedHashMap<>();
        Set<TipoActividad> actividadesDistintas = new HashSet<>();
        for (TurnoInstructorAsignacion asignacion : filasAsignacion) {
            actividadesPorInstructor
                    .computeIfAbsent(asignacion.getUsuario(), u -> new ArrayList<>())
                    .add(asignacion.getTipoActividad());
            filaPorInstructor.putIfAbsent(asignacion.getUsuario(), asignacion);
            actividadesDistintas.add(asignacion.getTipoActividad());
        }

        List<TurnoInstructorResponse.InstructorAsignacionResponse> asignaciones = t.getInstructores().stream()
                .sorted(Comparator.comparing(Usuario::getNombre))
                .map(u -> new TurnoInstructorResponse.InstructorAsignacionResponse(
                        u.getId(),
                        u.getNombre(),
                        actividadesPorInstructor.getOrDefault(u, List.of()).stream()
                                .map(a -> new TurnoInstructorResponse.ActividadResumen(a.getId(), a.getNombre()))
                                .sorted(Comparator.comparing(TurnoInstructorResponse.ActividadResumen::nombre))
                                .toList(),
                        filaPorInstructor.containsKey(u) ? filaPorInstructor.get(u).getHoraInicio() : null,
                        filaPorInstructor.containsKey(u) ? filaPorInstructor.get(u).getHoraFin() : null))
                .toList();

        return new TurnoInstructorResponse(
                t.getId(),
                t.getInstructores().stream()
                        .map(u -> new TurnoInstructorResponse.InstructorResumen(u.getId(), u.getNombre()))
                        .sorted((a, b) -> a.nombre().compareTo(b.nombre()))
                        .toList(),
                t.getSalon().getId(), t.getSalon().getNombre(),
                t.getTipo(), t.getDiaSemana(), t.getFecha(), t.getHoraInicio(), t.getHoraFin(),
                actividadesDistintas.stream()
                        .map(a -> new TurnoInstructorResponse.ActividadResumen(a.getId(), a.getNombre()))
                        .sorted((a, b) -> a.nombre().compareTo(b.nombre()))
                        .toList(),
                asignaciones);
    }
}
