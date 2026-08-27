package com.feelingpilates.programacion.servicio;

import com.feelingpilates.exception.ResourceNotFoundException;
import com.feelingpilates.exception.ValidacionException;
import com.feelingpilates.exception.ConflictException;
import com.feelingpilates.programacion.dominio.OcurrenciaEfectiva;
import com.feelingpilates.programacion.dominio.OcurrenciaNominal;
import com.feelingpilates.programacion.dominio.ReferenciaOcurrencia;
import com.feelingpilates.programacion.entidad.AjusteProgramacionFecha;
import com.feelingpilates.programacion.entidad.Asignacion;
import com.feelingpilates.programacion.entidad.BloqueProgramacion;
import com.feelingpilates.programacion.repositorio.AjusteProgramacionFechaRepository;
import com.feelingpilates.programacion.repositorio.AsignacionRepository;
import com.feelingpilates.programacion.repositorio.BloqueProgramacionRepository;
import com.feelingpilates.ubicaciones.dominio.CoberturaVigencia;
import com.feelingpilates.ubicaciones.dominio.RangoVigencia;
import com.feelingpilates.ubicaciones.entidad.HorarioOperacion;
import com.feelingpilates.ubicaciones.entidad.Salon;
import com.feelingpilates.ubicaciones.entidad.TipoActividad;
import com.feelingpilates.ubicaciones.repositorio.HorarioOperacionRepository;
import com.feelingpilates.ubicaciones.repositorio.SalonRepository;
import com.feelingpilates.ubicaciones.repositorio.TipoActividadRepository;
import com.feelingpilates.ubicaciones.servicio.SalonLocks;
import com.feelingpilates.usuarios.entidad.Rol;
import com.feelingpilates.usuarios.entidad.Usuario;
import com.feelingpilates.usuarios.repositorio.UsuarioRepository;
import com.feelingpilates.usuarios.servicio.InstructorLocks;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
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
    private final SalonLocks salonLocks;
    private final InstructorLocks instructorLocks;
    private final ProgramacionPolicyA policyA;
    private final AjusteProgramacionFechaRepository ajusteRepository;
    private final AjusteProgramacionFechaPersistence ajustePersistence;
    private final ProgramacionNominal nominal;
    private final AplicadorAjustesProgramacion aplicador;
    private final ProgramacionValidador programacionValidador;

    public BloqueProgramacionService(
            BloqueProgramacionRepository bloqueRepository,
            AsignacionRepository asignacionRepository,
            SalonRepository salonRepository,
            HorarioOperacionRepository horarioOperacionRepository,
            UsuarioRepository usuarioRepository,
            TipoActividadRepository tipoActividadRepository,
            SalonLocks salonLocks,
            InstructorLocks instructorLocks,
            ProgramacionPolicyA policyA,
            AjusteProgramacionFechaRepository ajusteRepository,
            AjusteProgramacionFechaPersistence ajustePersistence,
            ProgramacionNominal nominal,
            AplicadorAjustesProgramacion aplicador,
            ProgramacionValidador programacionValidador) {
        this.bloqueRepository = bloqueRepository;
        this.asignacionRepository = asignacionRepository;
        this.salonRepository = salonRepository;
        this.horarioOperacionRepository = horarioOperacionRepository;
        this.usuarioRepository = usuarioRepository;
        this.tipoActividadRepository = tipoActividadRepository;
        this.salonLocks = salonLocks;
        this.instructorLocks = instructorLocks;
        this.policyA = policyA;
        this.ajusteRepository = ajusteRepository;
        this.ajustePersistence = ajustePersistence;
        this.nominal = nominal;
        this.aplicador = aplicador;
        this.programacionValidador = programacionValidador;
    }

    /**
     * Unico entry point que crea un bloque activo, es decir que vuelve visible programacion nueva
     * capaz de volverse incompatible con el horario del salon. Por eso participa en el protocolo
     * de lock compartido: {@link SalonLock} se adquiere <b>antes</b> de leer el horario contra el
     * que se valida, en la misma transaccion que el {@code save}.
     *
     * <p>El orden importa. Validar primero y bloquear despues no serializaria nada: un versionado
     * concurrente del horario y este alta habrian leido el estado viejo y podrian commitear ambos,
     * dejando un bloque fuera del horario que acaba de entrar en vigor.
     */
    public BloqueProgramacion crearBloque(CrearBloque comando) {
        validarComandoBloque(comando);
        salonLocks.adquirirOrdenados(List.of(comando.salonId()));
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
        BloqueProgramacion discovery = bloqueRepository.findById(comando.bloqueId())
                .orElseThrow(() -> new ResourceNotFoundException("Bloque de programación no encontrado"));
        if (!discovery.isActivo()) {
            throw new ValidacionException("El bloque de programación no está activo");
        }

        List<AjusteProgramacionFecha> ajustesDiscovery =
                ajustesRelevantes(comando, discovery, false);
        List<SnapshotAjuste> snapshotsDiscovery = ajustesDiscovery.stream()
                .map(this::snapshot)
                .toList();
        Set<UUID> salones = new LinkedHashSet<>(List.of(discovery.getSalonId()));
        Set<UUID> instructores = new LinkedHashSet<>(List.of(comando.instructorId()));
        agregarRecursosDeAjustes(ajustesDiscovery, salones, instructores);
        agregarRecursosNominalesDeTargets(
                comando, ajustesDiscovery, salones, instructores);

        salonLocks.adquirirOrdenados(List.copyOf(salones));
        instructorLocks.adquirirOrdenados(List.copyOf(instructores));

        BloqueProgramacion bloque = bloqueRepository.findById(comando.bloqueId())
                .orElseThrow(() -> new ConflictException(ProgramacionErrores.mensaje(
                        ProgramacionErrores.CONFLICTO_LOCK_SET_DESACTUALIZADO,
                        "El bloque desapareció durante el discovery")));
        if (!mismoBloqueDescubierto(discovery, bloque)) {
            throw new ConflictException(ProgramacionErrores.mensaje(
                    ProgramacionErrores.CONFLICTO_LOCK_SET_DESACTUALIZADO,
                    "El bloque o el salón cambiaron durante el discovery"));
        }

        List<AjusteProgramacionFecha> ajustesReleidos =
                ajustesRelevantes(comando, bloque, true);
        if (!snapshotsDiscovery.equals(ajustesReleidos.stream().map(this::snapshot).toList())) {
            throw new ConflictException(ProgramacionErrores.mensaje(
                    ProgramacionErrores.CONFLICTO_LOCK_SET_DESACTUALIZADO,
                    "Los ajustes efectivos cambiaron durante el discovery"));
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
        policyA.validarNuevaAsignacion(comando, bloque, ajustesReleidos);
        validarContraAjustesEfectivos(comando, bloque, ajustesReleidos);

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

    private boolean mismoBloqueDescubierto(BloqueProgramacion a, BloqueProgramacion b) {
        return Objects.equals(a.getId(), b.getId())
                && Objects.equals(a.getSerieId(), b.getSerieId())
                && Objects.equals(a.getSalonId(), b.getSalonId())
                && a.getDiaSemana() == b.getDiaSemana()
                && Objects.equals(a.getHoraInicio(), b.getHoraInicio())
                && Objects.equals(a.getHoraFin(), b.getHoraFin())
                && Objects.equals(a.getVigenteDesde(), b.getVigenteDesde())
                && Objects.equals(a.getVigenteHasta(), b.getVigenteHasta())
                && a.isActivo() == b.isActivo();
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

    /**
     * El bloque ya trae su propia vigencia explicita ({@code vigenteDesde} obligatorio,
     * {@code vigenteHasta} nullable = abierta), asi que no necesita reloj: su objetivo temporal es
     * exactamente esa vigencia. Las versiones del horario del salon para ese dia deben
     *
     * <ol>
     *   <li>CUBRIR completa la vigencia del bloque -- sin huecos, y llegando a +infinito si el
     *       bloque es abierto; un horario cuya cobertura termina dejaria al bloque sin respaldo;</li>
     *   <li>y TODAS contener el rango horario del bloque, no solo alguna.</li>
     * </ol>
     *
     * El barrido de cobertura es por intervalos, nunca dia por dia.
     */
    private void validarDentroDelHorarioOperacion(CrearBloque comando) {
        List<HorarioOperacion> versiones = horarioOperacionRepository.findVersionesQueIntersectan(
                comando.salonId(), comando.diaSemana(), comando.vigenteDesde(), comando.vigenteHasta());

        RangoVigencia objetivo = new RangoVigencia(comando.vigenteDesde(), comando.vigenteHasta());
        boolean cubierto = CoberturaVigencia.cubreCompletamente(objetivo, versiones.stream()
                .map(h -> new RangoVigencia(h.getVigenteDesde(), h.getVigenteHasta()))
                .toList());
        boolean todasLoContienen = !versiones.isEmpty() && versiones.stream()
                .allMatch(h -> !comando.horaInicio().isBefore(h.getHoraApertura())
                        && !comando.horaFin().isAfter(h.getHoraCierre()));

        if (!cubierto || !todasLoContienen) {
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

    private List<AjusteProgramacionFecha> ajustesRelevantes(
            CrearAsignacion comando, BloqueProgramacion bloque, boolean refrescar) {
        List<AjusteProgramacionFecha> ajustes = ajusteRepository.buscarActivosEnRango(
                comando.vigenteDesde(), comando.vigenteHasta());
        if (refrescar) {
            ajustes.forEach(ajustePersistence::refrescar);
        }
        return ajustes.stream()
                .filter(a -> aplicaEnDia(a.getFecha(), bloque.getDiaSemana()))
                .filter(a -> comando.serieId().equals(a.getAsignacionSerieId())
                        || comando.instructorId().equals(a.getInstructorResultadoId()))
                .toList();
    }

    private void agregarRecursosDeAjustes(
            List<AjusteProgramacionFecha> ajustes,
            Set<UUID> salones,
            Set<UUID> instructores) {
        ajustes.stream()
                .filter(a -> a.getSalonResultadoId() != null)
                .forEach(a -> {
                    salones.add(a.getSalonResultadoId());
                    instructores.add(a.getInstructorResultadoId());
                });
    }

    private void agregarRecursosNominalesDeTargets(
            CrearAsignacion comando,
            List<AjusteProgramacionFecha> ajustes,
            Set<UUID> salones,
            Set<UUID> instructores) {
        ajustes.stream()
                .filter(a -> comando.serieId().equals(a.getAsignacionSerieId()))
                .flatMap(a -> nominal.porSerieYFecha(comando.serieId(), a.getFecha()).stream())
                .forEach(n -> {
                    salones.add(n.salonId());
                    instructores.add(n.instructorId());
                });
    }

    private void validarContraAjustesEfectivos(
            CrearAsignacion comando,
            BloqueProgramacion bloque,
            List<AjusteProgramacionFecha> ajustesReleidos) {
        ajustesReleidos.stream()
                .map(AjusteProgramacionFecha::getFecha)
                .distinct()
                .sorted()
                .forEach(fecha -> validarFechaEfectiva(comando, bloque, fecha));
    }

    private void validarFechaEfectiva(
            CrearAsignacion comando, BloqueProgramacion bloque, LocalDate fecha) {
        List<OcurrenciaNominal> nominales = new ArrayList<>(nominal.todasEnFecha(fecha));
        nominales.add(new OcurrenciaNominal(
                fecha, comando.serieId(), comando.serieId(), bloque.getId(), bloque.getSalonId(),
                comando.instructorId(), comando.tipoActividadId(),
                comando.horaInicio(), comando.horaFin()));
        List<AjusteProgramacionFecha> ajustes =
                ajusteRepository.findAllByFechaAndActivoTrueOrderById(fecha);
        ajustes.forEach(ajustePersistence::refrescar);
        List<OcurrenciaEfectiva> efectivas = aplicador.aplicar(nominales, ajustes);
        ReferenciaOcurrencia referencia = ajustes.stream()
                .filter(a -> comando.serieId().equals(a.getAsignacionSerieId()))
                .filter(a -> a.getTipo() == AjusteProgramacionFecha.Tipo.CANCELACION)
                .findAny()
                .isPresent()
                ? null
                : new ReferenciaOcurrencia(
                        ReferenciaOcurrencia.Tipo.SERIE_ASIGNACION, comando.serieId(), fecha);
        programacionValidador.validarMutacion(efectivas, referencia);
    }

    private boolean aplicaEnDia(LocalDate fecha, short diaSemana) {
        return (short) (fecha.getDayOfWeek().getValue() % 7) == diaSemana;
    }

    private SnapshotAjuste snapshot(AjusteProgramacionFecha ajuste) {
        return new SnapshotAjuste(
                ajuste.getId(), ajuste.getTipo(), ajuste.getFecha(), ajuste.getAsignacionSerieId(),
                ajuste.getSalonResultadoId(), ajuste.getInstructorResultadoId(),
                ajuste.getTipoActividadResultadoId(), ajuste.getHoraInicioResultado(),
                ajuste.getHoraFinResultado(), ajuste.isActivo());
    }

    static boolean rangosSeTraslapan(LocalTime aInicio, LocalTime aFin, LocalTime bInicio, LocalTime bFin) {
        return aInicio.isBefore(bFin) && bInicio.isBefore(aFin);
    }

    /**
     * Delega en {@link RangoVigencia#intersecta}: una sola implementacion de la semantica de
     * interseccion de vigencias en todo el proyecto, para que no puedan divergir.
     */
    static boolean vigenciasSeIntersectan(
            LocalDate aDesde, LocalDate aHasta, LocalDate bDesde, LocalDate bHasta) {
        return new RangoVigencia(aDesde, aHasta).intersecta(new RangoVigencia(bDesde, bHasta));
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

    private record SnapshotAjuste(
            UUID id,
            AjusteProgramacionFecha.Tipo tipo,
            LocalDate fecha,
            UUID serieId,
            UUID salonId,
            UUID instructorId,
            UUID actividadId,
            LocalTime horaInicio,
            LocalTime horaFin,
            boolean activo) {
    }
}
