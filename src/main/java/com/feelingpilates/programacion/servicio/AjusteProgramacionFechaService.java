package com.feelingpilates.programacion.servicio;

import com.feelingpilates.exception.ConflictException;
import com.feelingpilates.exception.ResourceNotFoundException;
import com.feelingpilates.exception.ValidacionException;
import com.feelingpilates.programacion.dominio.OcurrenciaEfectiva;
import com.feelingpilates.programacion.dominio.OcurrenciaNominal;
import com.feelingpilates.programacion.dominio.ProgramacionInvarianteException;
import com.feelingpilates.programacion.dominio.ReferenciaOcurrencia;
import com.feelingpilates.programacion.entidad.AjusteProgramacionFecha;
import com.feelingpilates.programacion.repositorio.AjusteProgramacionFechaRepository;
import com.feelingpilates.ubicaciones.servicio.SalonLocks;
import com.feelingpilates.usuarios.servicio.InstructorLocks;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/** Writer interno F2D.2. No expone controller ni participa de flujos legacy. */
@Service
@Transactional
public class AjusteProgramacionFechaService {

    private final AjusteProgramacionFechaRepository repository;
    private final AjusteProgramacionFechaPersistence persistence;
    private final ProgramacionNominal nominal;
    private final AplicadorAjustesProgramacion aplicador;
    private final ProgramacionValidador validador;
    private final SalonLocks salonLocks;
    private final InstructorLocks instructorLocks;
    private final Clock reloj;

    public AjusteProgramacionFechaService(
            AjusteProgramacionFechaRepository repository,
            AjusteProgramacionFechaPersistence persistence,
            ProgramacionNominal nominal,
            AplicadorAjustesProgramacion aplicador,
            ProgramacionValidador validador,
            SalonLocks salonLocks,
            InstructorLocks instructorLocks,
            Clock reloj) {
        this.repository = repository;
        this.persistence = persistence;
        this.nominal = nominal;
        this.aplicador = aplicador;
        this.validador = validador;
        this.salonLocks = salonLocks;
        this.instructorLocks = instructorLocks;
        this.reloj = reloj;
    }

    public AjusteProgramacionFecha guardarAdicion(
            UUID ajusteId, LocalDate fecha, Resultado resultado) {
        validarIdFechaResultado(ajusteId, fecha, resultado);
        validarFechaMutable(fecha);

        AjusteProgramacionFecha historico = repository.findById(ajusteId).orElse(null);
        if (historico != null) {
            if (!historico.isActivo()) {
                throw nuevoConflicto("El UUID histórico de una adición no puede reutilizarse");
            }
            return actualizarAdicion(historico, fecha, resultado);
        }

        salonLocks.adquirirOrdenados(List.of(resultado.salonId()));
        instructorLocks.adquirirOrdenados(List.of(resultado.instructorId()));

        // RAMA A: exactamente una relectura histórica. La aparición, activa o inactiva,
        // aborta antes de proyección, creación de entidad, persist o flush.
        if (repository.findById(ajusteId).isPresent()) {
            throw nuevoConflicto("La identidad apareció después del discovery inicial");
        }

        // RAMA B: desde aquí no se vuelve a consultar la PK antes de persistir.
        AjusteProgramacionFecha nuevo = AjusteProgramacionFecha.nuevaAdicion(
                ajusteId, fecha, resultado.salonId(), resultado.instructorId(),
                resultado.actividadId(), resultado.horaInicio(), resultado.horaFin());
        ReferenciaOcurrencia referencia = referenciaAjuste(ajusteId, fecha);
        validarProyeccion(fecha, nuevo, null, referencia);
        return ConflictoAjusteProgramacionTranslator.traduciendoAjusteId(
                () -> persistence.crear(nuevo));
    }

    public void retirarAdicion(UUID ajusteId) {
        AjusteProgramacionFecha discovery = repository.findByIdAndActivoTrue(ajusteId)
                .orElseThrow(() -> noExiste("La adición no existe o está retirada"));
        SnapshotAjuste discoverySnapshot = snapshot(discovery);
        validarFechaMutable(discovery.getFecha());
        salonLocks.adquirirOrdenados(List.of(discovery.getSalonResultadoId()));
        instructorLocks.adquirirOrdenados(List.of(discovery.getInstructorResultadoId()));
        AjusteProgramacionFecha managed = releerActivo(ajusteId)
                .orElseThrow(() -> lockSetDesactualizado("La adición cambió durante el discovery"));
        comparar(discoverySnapshot, managed);
        validarProyeccion(managed.getFecha(), null, managed.getId(), null);
        managed.retirar();
        persistence.flushManaged();
    }

    public AjusteProgramacionFecha guardarTarget(
            UUID serieId, LocalDate fecha, AjusteProgramacionFecha.Tipo tipo, Resultado resultado) {
        if (tipo != AjusteProgramacionFecha.Tipo.CANCELACION
                && tipo != AjusteProgramacionFecha.Tipo.REEMPLAZO) {
            throw formaInvalida("El target sólo admite CANCELACION o REEMPLAZO");
        }
        validarRequerido(serieId, "La serie es obligatoria");
        validarRequerido(fecha, "La fecha es obligatoria");
        if (tipo == AjusteProgramacionFecha.Tipo.REEMPLAZO) {
            validarResultado(resultado);
        } else if (resultado != null) {
            throw formaInvalida("Una cancelación no porta resultado");
        }
        validarFechaMutable(fecha);

        OcurrenciaNominal nominalDiscovery = nominalUnica(serieId, fecha);
        AjusteProgramacionFecha ajusteDiscovery = targetActivoUnico(serieId, fecha, false);
        SnapshotAjuste ajusteDiscoverySnapshot = snapshot(ajusteDiscovery);
        Set<UUID> salones = new LinkedHashSet<>();
        Set<UUID> instructores = new LinkedHashSet<>();
        agregarRecursos(nominalDiscovery, salones, instructores);
        agregarRecursos(ajusteDiscovery, salones, instructores);
        agregarRecursos(resultado, salones, instructores);
        salonLocks.adquirirOrdenados(salones);
        instructorLocks.adquirirOrdenados(instructores);

        OcurrenciaNominal nominalManaged = nominalUnica(serieId, fecha);
        AjusteProgramacionFecha ajusteManaged = targetActivoUnico(serieId, fecha, true);
        if (!snapshot(nominalDiscovery).equals(snapshot(nominalManaged))
                || !Objects.equals(ajusteDiscoverySnapshot, snapshot(ajusteManaged))) {
            throw lockSetDesactualizado("El target o sus recursos cambiaron bajo locks");
        }

        if (ajusteManaged == null) {
            AjusteProgramacionFecha nuevo = AjusteProgramacionFecha.nuevoTarget(
                    UUID.randomUUID(), tipo, serieId, fecha,
                    resultado == null ? null : resultado.salonId(),
                    resultado == null ? null : resultado.instructorId(),
                    resultado == null ? null : resultado.actividadId(),
                    resultado == null ? null : resultado.horaInicio(),
                    resultado == null ? null : resultado.horaFin());
            ReferenciaOcurrencia referencia = tipo == AjusteProgramacionFecha.Tipo.REEMPLAZO
                    ? referenciaSerie(serieId, fecha) : null;
            validarProyeccion(fecha, nuevo, null, referencia);
            return ConflictoAjusteProgramacionTranslator.traduciendoTarget(
                    () -> persistence.crear(nuevo));
        }

        SnapshotAjuste solicitado = new SnapshotAjuste(
                ajusteManaged.getId(), tipo, fecha, serieId,
                resultado == null ? null : resultado.salonId(),
                resultado == null ? null : resultado.instructorId(),
                resultado == null ? null : resultado.actividadId(),
                resultado == null ? null : resultado.horaInicio(),
                resultado == null ? null : resultado.horaFin(), true);
        if (solicitado.equals(snapshot(ajusteManaged))) {
            return ajusteManaged;
        }
        AjusteProgramacionFecha proyectado = AjusteProgramacionFecha.nuevoTarget(
                ajusteManaged.getId(), tipo, serieId, fecha,
                resultado == null ? null : resultado.salonId(),
                resultado == null ? null : resultado.instructorId(),
                resultado == null ? null : resultado.actividadId(),
                resultado == null ? null : resultado.horaInicio(),
                resultado == null ? null : resultado.horaFin());
        validarProyeccion(fecha, proyectado, ajusteManaged.getId(),
                tipo == AjusteProgramacionFecha.Tipo.REEMPLAZO
                        ? referenciaSerie(serieId, fecha) : null);
        ajusteManaged.actualizarTipoYResultado(
                tipo,
                resultado == null ? null : resultado.salonId(),
                resultado == null ? null : resultado.instructorId(),
                resultado == null ? null : resultado.actividadId(),
                resultado == null ? null : resultado.horaInicio(),
                resultado == null ? null : resultado.horaFin());
        persistence.flushManaged();
        return ajusteManaged;
    }

    public void retirarTarget(UUID serieId, LocalDate fecha) {
        validarFechaMutable(fecha);
        OcurrenciaNominal nominalDiscovery = nominalUnica(serieId, fecha);
        AjusteProgramacionFecha discovery = targetActivoUnico(serieId, fecha, false);
        if (discovery == null) {
            throw noExiste("El ajuste target no existe");
        }
        SnapshotAjuste discoverySnapshot = snapshot(discovery);
        Set<UUID> salones = new LinkedHashSet<>();
        Set<UUID> instructores = new LinkedHashSet<>();
        agregarRecursos(nominalDiscovery, salones, instructores);
        agregarRecursos(discovery, salones, instructores);
        salonLocks.adquirirOrdenados(salones);
        instructorLocks.adquirirOrdenados(instructores);
        OcurrenciaNominal nominalManaged = nominalUnica(serieId, fecha);
        AjusteProgramacionFecha managed = targetActivoUnico(serieId, fecha, true);
        if (!snapshot(nominalDiscovery).equals(snapshot(nominalManaged))
                || !Objects.equals(discoverySnapshot, snapshot(managed))) {
            throw lockSetDesactualizado("El target cambió durante el discovery");
        }
        validarProyeccion(fecha, null, managed.getId(), referenciaSerie(serieId, fecha));
        managed.retirar();
        persistence.flushManaged();
    }

    private AjusteProgramacionFecha actualizarAdicion(
            AjusteProgramacionFecha discovery, LocalDate fecha, Resultado resultado) {
        SnapshotAjuste discoverySnapshot = snapshot(discovery);
        if (!discovery.getFecha().equals(fecha)) {
            throw formaInvalida("La fecha de una adición activa es inmutable");
        }
        Set<UUID> salones = new LinkedHashSet<>();
        Set<UUID> instructores = new LinkedHashSet<>();
        agregarRecursos(discovery, salones, instructores);
        agregarRecursos(resultado, salones, instructores);
        salonLocks.adquirirOrdenados(salones);
        instructorLocks.adquirirOrdenados(instructores);
        AjusteProgramacionFecha managed = releerActivo(discovery.getId())
                .orElseThrow(() -> lockSetDesactualizado("La adición cambió durante el discovery"));
        comparar(discoverySnapshot, managed);
        if (!managed.getFecha().equals(fecha)) {
            throw formaInvalida("La fecha de una adición activa es inmutable");
        }
        if (mismoResultado(managed, resultado)) {
            return managed;
        }
        AjusteProgramacionFecha proyectado = AjusteProgramacionFecha.nuevaAdicion(
                managed.getId(), fecha, resultado.salonId(), resultado.instructorId(),
                resultado.actividadId(), resultado.horaInicio(), resultado.horaFin());
        validarProyeccion(fecha, proyectado, managed.getId(), referenciaAjuste(managed.getId(), fecha));
        managed.actualizarResultado(
                resultado.salonId(), resultado.instructorId(), resultado.actividadId(),
                resultado.horaInicio(), resultado.horaFin());
        persistence.flushManaged();
        return managed;
    }

    private void validarProyeccion(
            LocalDate fecha, AjusteProgramacionFecha reemplazo,
            UUID idAExcluir, ReferenciaOcurrencia referenciaMutada) {
        List<AjusteProgramacionFecha> proyectados = new ArrayList<>(
                ajustesActivosEnFechaFrescos(fecha));
        if (idAExcluir != null) {
            proyectados.removeIf(a -> idAExcluir.equals(a.getId()));
        }
        if (reemplazo != null) {
            proyectados.add(reemplazo);
        }
        List<OcurrenciaEfectiva> candidatos = aplicador.aplicar(
                nominal.todasEnFecha(fecha), proyectados);
        validador.validarMutacion(candidatos, referenciaMutada);
    }

    private OcurrenciaNominal nominalUnica(UUID serieId, LocalDate fecha) {
        List<OcurrenciaNominal> encontradas = nominal.porSerieYFecha(serieId, fecha);
        if (encontradas.isEmpty()) {
            throw new ResourceNotFoundException(ProgramacionErrores.mensaje(
                    ProgramacionErrores.ASIGNACION_OBJETIVO_NO_EXISTE,
                    "No existe ocurrencia nominal para la serie y fecha"));
        }
        if (encontradas.size() > 1) {
            throw new ProgramacionInvarianteException(
                    ProgramacionErrores.CONFLICTO_LOCK_SET_DESACTUALIZADO,
                    "Más de una nominal aplica al target", referenciaSerie(serieId, fecha));
        }
        return encontradas.getFirst();
    }

    private AjusteProgramacionFecha targetActivoUnico(
            UUID serieId, LocalDate fecha, boolean refrescar) {
        List<AjusteProgramacionFecha> encontrados =
                repository.findByAsignacionSerieIdAndFechaAndActivoTrue(serieId, fecha);
        if (refrescar) {
            encontrados.forEach(persistence::refrescar);
        }
        if (encontrados.size() > 1) {
            throw new ProgramacionInvarianteException(
                    ProgramacionErrores.CONFLICTO_AJUSTE_PROGRAMACION,
                    "Más de un target activo", referenciaSerie(serieId, fecha));
        }
        return encontrados.isEmpty() ? null : encontrados.getFirst();
    }

    private void validarFechaMutable(LocalDate fecha) {
        validarRequerido(fecha, "La fecha es obligatoria");
        LocalDate hoy = reloj.instant().atZone(reloj.getZone()).toLocalDate();
        if (fecha.isBefore(hoy)) {
            throw new ValidacionException(ProgramacionErrores.mensaje(
                    ProgramacionErrores.AJUSTE_PROGRAMACION_EN_EL_PASADO,
                    "No se puede mutar un ajuste del pasado"));
        }
    }

    private void validarIdFechaResultado(UUID id, LocalDate fecha, Resultado resultado) {
        validarRequerido(id, "El ajusteId es obligatorio");
        validarRequerido(fecha, "La fecha es obligatoria");
        validarResultado(resultado);
    }

    private void validarResultado(Resultado resultado) {
        if (resultado == null || resultado.salonId() == null || resultado.instructorId() == null
                || resultado.actividadId() == null || resultado.horaInicio() == null
                || resultado.horaFin() == null || !resultado.horaFin().isAfter(resultado.horaInicio())) {
            throw formaInvalida("El snapshot resultado debe estar completo y tener rango positivo");
        }
    }

    private void validarRequerido(Object valor, String mensaje) {
        if (valor == null) {
            throw formaInvalida(mensaje);
        }
    }

    private boolean mismoResultado(AjusteProgramacionFecha ajuste, Resultado resultado) {
        return Objects.equals(ajuste.getSalonResultadoId(), resultado.salonId())
                && Objects.equals(ajuste.getInstructorResultadoId(), resultado.instructorId())
                && Objects.equals(ajuste.getTipoActividadResultadoId(), resultado.actividadId())
                && Objects.equals(ajuste.getHoraInicioResultado(), resultado.horaInicio())
                && Objects.equals(ajuste.getHoraFinResultado(), resultado.horaFin());
    }

    private void comparar(SnapshotAjuste discovery, AjusteProgramacionFecha relectura) {
        if (!discovery.equals(snapshot(relectura))) {
            throw lockSetDesactualizado("El ajuste cambió bajo locks");
        }
    }

    private java.util.Optional<AjusteProgramacionFecha> releerActivo(UUID ajusteId) {
        java.util.Optional<AjusteProgramacionFecha> relectura =
                repository.findByIdAndActivoTrue(ajusteId);
        relectura.ifPresent(persistence::refrescar);
        return relectura;
    }

    private List<AjusteProgramacionFecha> ajustesActivosEnFechaFrescos(LocalDate fecha) {
        List<AjusteProgramacionFecha> ajustes =
                repository.findAllByFechaAndActivoTrueOrderById(fecha);
        ajustes.forEach(persistence::refrescar);
        return ajustes;
    }

    private void agregarRecursos(
            OcurrenciaNominal ocurrencia, Set<UUID> salones, Set<UUID> instructores) {
        if (ocurrencia != null) {
            salones.add(ocurrencia.salonId());
            instructores.add(ocurrencia.instructorId());
        }
    }

    private void agregarRecursos(
            AjusteProgramacionFecha ajuste, Set<UUID> salones, Set<UUID> instructores) {
        if (ajuste != null && ajuste.getSalonResultadoId() != null) {
            salones.add(ajuste.getSalonResultadoId());
            instructores.add(ajuste.getInstructorResultadoId());
        }
    }

    private void agregarRecursos(Resultado resultado, Set<UUID> salones, Set<UUID> instructores) {
        if (resultado != null) {
            salones.add(resultado.salonId());
            instructores.add(resultado.instructorId());
        }
    }

    private SnapshotNominal snapshot(OcurrenciaNominal n) {
        return new SnapshotNominal(n.serieId(), n.asignacionVersionId(), n.bloqueVersionId(),
                n.salonId(), n.instructorId(), n.tipoActividadId(), n.horaInicio(), n.horaFin());
    }

    private SnapshotAjuste snapshot(AjusteProgramacionFecha a) {
        return a == null ? null : new SnapshotAjuste(
                a.getId(), a.getTipo(), a.getFecha(), a.getAsignacionSerieId(),
                a.getSalonResultadoId(), a.getInstructorResultadoId(),
                a.getTipoActividadResultadoId(), a.getHoraInicioResultado(),
                a.getHoraFinResultado(), a.isActivo());
    }

    private ReferenciaOcurrencia referenciaSerie(UUID serieId, LocalDate fecha) {
        return new ReferenciaOcurrencia(
                ReferenciaOcurrencia.Tipo.SERIE_ASIGNACION, serieId, fecha);
    }

    private ReferenciaOcurrencia referenciaAjuste(UUID id, LocalDate fecha) {
        return new ReferenciaOcurrencia(ReferenciaOcurrencia.Tipo.AJUSTE, id, fecha);
    }

    private ValidacionException formaInvalida(String detalle) {
        return new ValidacionException(ProgramacionErrores.mensaje(
                ProgramacionErrores.AJUSTE_PROGRAMACION_FORMA_INVALIDA, detalle));
    }

    private ConflictException nuevoConflicto(String detalle) {
        return new ConflictException(ProgramacionErrores.mensaje(
                ProgramacionErrores.CONFLICTO_AJUSTE_PROGRAMACION, detalle));
    }

    private ConflictException lockSetDesactualizado(String detalle) {
        return new ConflictException(ProgramacionErrores.mensaje(
                ProgramacionErrores.CONFLICTO_LOCK_SET_DESACTUALIZADO, detalle));
    }

    private ResourceNotFoundException noExiste(String detalle) {
        return new ResourceNotFoundException(ProgramacionErrores.mensaje(
                ProgramacionErrores.AJUSTE_PROGRAMACION_NO_EXISTE, detalle));
    }

    public record Resultado(
            UUID salonId,
            UUID instructorId,
            UUID actividadId,
            LocalTime horaInicio,
            LocalTime horaFin) {
    }

    private record SnapshotNominal(
            UUID serieId, UUID asignacionId, UUID bloqueId, UUID salonId,
            UUID instructorId, UUID actividadId, LocalTime inicio, LocalTime fin) {
    }

    private record SnapshotAjuste(
            UUID id, AjusteProgramacionFecha.Tipo tipo, LocalDate fecha, UUID serieId,
            UUID salonId, UUID instructorId, UUID actividadId,
            LocalTime inicio, LocalTime fin, boolean activo) {
    }
}
