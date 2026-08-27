package com.feelingpilates.programacion.servicio;

import com.feelingpilates.exception.ConflictException;
import com.feelingpilates.exception.ValidacionException;
import com.feelingpilates.programacion.entidad.AjusteProgramacionFecha;
import com.feelingpilates.programacion.dominio.OcurrenciaNominal;
import com.feelingpilates.programacion.repositorio.AjusteProgramacionFechaRepository;
import com.feelingpilates.ubicaciones.servicio.SalonLocks;
import com.feelingpilates.usuarios.servicio.InstructorLocks;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AjusteProgramacionFechaServiceTest {

    private static final UUID ID = UUID.randomUUID();
    private static final UUID SALON = UUID.randomUUID();
    private static final UUID INSTRUCTOR = UUID.randomUUID();
    private static final UUID ACTIVIDAD = UUID.randomUUID();
    private static final LocalDate FECHA = LocalDate.of(2027, 2, 1);

    private AjusteProgramacionFechaRepository repository;
    private AjusteProgramacionFechaPersistence persistence;
    private ProgramacionValidador validador;
    private ProgramacionNominal nominal;
    private AplicadorAjustesProgramacion aplicador;
    private SalonLocks salonLocks;
    private InstructorLocks instructorLocks;
    private AjusteProgramacionFechaService service;

    @BeforeEach
    void preparar() {
        repository = mock(AjusteProgramacionFechaRepository.class);
        persistence = mock(AjusteProgramacionFechaPersistence.class);
        validador = mock(ProgramacionValidador.class);
        nominal = mock(ProgramacionNominal.class);
        aplicador = mock(AplicadorAjustesProgramacion.class);
        salonLocks = mock(SalonLocks.class);
        instructorLocks = mock(InstructorLocks.class);
        service = new AjusteProgramacionFechaService(
                repository,
                persistence,
                nominal,
                aplicador,
                validador,
                salonLocks,
                instructorLocks,
                Clock.fixed(Instant.parse("2026-08-26T12:00:00Z"), ZoneOffset.UTC));
    }

    @Test
    void fechaDeAdicionActivaEsInmutable() {
        AjusteProgramacionFecha existente = adicion(true);
        when(repository.findById(ID)).thenReturn(Optional.of(existente));

        assertThatThrownBy(() -> service.guardarAdicion(ID, FECHA.plusDays(1), resultado()))
                .isInstanceOf(ValidacionException.class)
                .hasMessageContaining("inmutable");
        verify(persistence, never()).flushManaged();
    }

    @Test
    void uuidHistoricoInactivoNoSeReactiva() {
        AjusteProgramacionFecha existente = adicion(false);
        when(repository.findById(ID)).thenReturn(Optional.of(existente));

        assertThatThrownBy(() -> service.guardarAdicion(ID, FECHA, resultado()))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("histórico");
        assertThat(existente.isActivo()).isFalse();
        verify(persistence, never()).crear(any());
        verify(persistence, never()).flushManaged();
    }

    @Test
    void updateConMismoContenidoEsNoOpReal() {
        AjusteProgramacionFecha existente = adicion(true);
        when(repository.findById(ID)).thenReturn(Optional.of(existente));
        when(repository.findByIdAndActivoTrue(ID)).thenReturn(Optional.of(existente));

        AjusteProgramacionFecha resultado = service.guardarAdicion(ID, FECHA, resultado());

        assertThat(resultado).isSameAs(existente);
        verify(persistence, never()).crear(any());
        verify(persistence, never()).flushManaged();
        verify(validador, never()).validarMutacion(any(), any());
    }

    @Test
    void pasadoSeRechazaUsandoClockInyectado() {
        assertThatThrownBy(() -> service.guardarAdicion(
                ID, LocalDate.of(2026, 8, 25), resultado()))
                .isInstanceOf(ValidacionException.class)
                .hasMessageContaining(ProgramacionErrores.AJUSTE_PROGRAMACION_EN_EL_PASADO);
        verify(repository, never()).findById(any());
    }

    @Test
    void updateActivoConMismaFechaUsaRecursosAnterioresYNuevosYEntidadManaged() {
        UUID salonNuevo = UUID.randomUUID();
        UUID instructorNuevo = UUID.randomUUID();
        AjusteProgramacionFecha existente = adicion(true);
        when(repository.findById(ID)).thenReturn(Optional.of(existente));
        when(repository.findByIdAndActivoTrue(ID)).thenReturn(Optional.of(existente));
        when(repository.findAllByFechaAndActivoTrueOrderById(FECHA)).thenReturn(java.util.List.of(existente));
        when(nominal.todasEnFecha(FECHA)).thenReturn(java.util.List.of());
        when(aplicador.aplicar(any(), any())).thenReturn(java.util.List.of());
        AjusteProgramacionFechaService.Resultado nuevo = new AjusteProgramacionFechaService.Resultado(
                salonNuevo, instructorNuevo, ACTIVIDAD,
                LocalTime.of(12, 0), LocalTime.of(13, 0));

        AjusteProgramacionFecha resultado = service.guardarAdicion(ID, FECHA, nuevo);

        assertThat(resultado).isSameAs(existente);
        assertThat(resultado.getFecha()).isEqualTo(FECHA);
        assertThat(resultado.getSalonResultadoId()).isEqualTo(salonNuevo);
        verify(salonLocks).adquirirOrdenados(org.mockito.ArgumentMatchers.argThat(
                ids -> ids.containsAll(java.util.List.of(SALON, salonNuevo))));
        verify(instructorLocks).adquirirOrdenados(org.mockito.ArgumentMatchers.argThat(
                ids -> ids.containsAll(java.util.List.of(INSTRUCTOR, instructorNuevo))));
        verify(persistence).flushManaged();
        verify(persistence, never()).crear(any());
    }

    @Test
    void staleDiscoveryDeTargetAbortaAntesDeProyectarOPersistir() {
        UUID serie = UUID.randomUUID();
        OcurrenciaNominal ocurrencia = new OcurrenciaNominal(
                FECHA, serie, UUID.randomUUID(), UUID.randomUUID(), SALON,
                INSTRUCTOR, ACTIVIDAD, LocalTime.of(10, 0), LocalTime.of(11, 0));
        AjusteProgramacionFecha aparecido = AjusteProgramacionFecha.nuevoTarget(
                UUID.randomUUID(), AjusteProgramacionFecha.Tipo.CANCELACION,
                serie, FECHA, null, null, null, null, null);
        when(nominal.porSerieYFecha(serie, FECHA))
                .thenReturn(java.util.List.of(ocurrencia), java.util.List.of(ocurrencia));
        when(repository.findByAsignacionSerieIdAndFechaAndActivoTrue(serie, FECHA))
                .thenReturn(java.util.List.of(), java.util.List.of(aparecido));

        assertThatThrownBy(() -> service.guardarTarget(
                serie, FECHA, AjusteProgramacionFecha.Tipo.CANCELACION, null))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining(ProgramacionErrores.CONFLICTO_LOCK_SET_DESACTUALIZADO);

        verify(repository, never()).findAllByFechaAndActivoTrueOrderById(any());
        verify(aplicador, never()).aplicar(any(), any());
        verify(persistence, never()).crear(any());
        verify(persistence, never()).flushManaged();
    }

    @Test
    void updateAdicionComparaSnapshotPrevioContraRefreshDelFirstLevelCache() {
        AjusteProgramacionFecha existente = adicion(true);
        when(repository.findById(ID)).thenReturn(Optional.of(existente));
        when(repository.findByIdAndActivoTrue(ID)).thenReturn(Optional.of(existente));
        doAnswer(invocacion -> {
            existente.actualizarResultado(
                    UUID.randomUUID(), UUID.randomUUID(), ACTIVIDAD,
                    LocalTime.NOON, LocalTime.of(13, 0));
            return null;
        }).when(persistence).refrescar(existente);

        assertThatThrownBy(() -> service.guardarAdicion(ID, FECHA, resultado()))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining(ProgramacionErrores.CONFLICTO_LOCK_SET_DESACTUALIZADO);

        verify(persistence, never()).flushManaged();
    }

    @Test
    void retiroAdicionComparaSnapshotPrevioContraRefreshDelFirstLevelCache() {
        AjusteProgramacionFecha existente = adicion(true);
        when(repository.findByIdAndActivoTrue(ID)).thenReturn(Optional.of(existente));
        doAnswer(invocacion -> {
            existente.actualizarResultado(
                    SALON, INSTRUCTOR, ACTIVIDAD, LocalTime.NOON, LocalTime.of(13, 0));
            return null;
        }).when(persistence).refrescar(existente);

        assertThatThrownBy(() -> service.retirarAdicion(ID))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining(ProgramacionErrores.CONFLICTO_LOCK_SET_DESACTUALIZADO);

        assertThat(existente.isActivo()).isTrue();
        verify(persistence, never()).flushManaged();
    }

    @Test
    void updateTargetComparaSnapshotPrevioContraRefreshDelFirstLevelCache() {
        UUID serie = UUID.randomUUID();
        OcurrenciaNominal ocurrencia = nominal(serie);
        AjusteProgramacionFecha existente = AjusteProgramacionFecha.nuevoTarget(
                UUID.randomUUID(), AjusteProgramacionFecha.Tipo.CANCELACION,
                serie, FECHA, null, null, null, null, null);
        when(nominal.porSerieYFecha(serie, FECHA))
                .thenReturn(java.util.List.of(ocurrencia));
        when(repository.findByAsignacionSerieIdAndFechaAndActivoTrue(serie, FECHA))
                .thenReturn(java.util.List.of(existente));
        doAnswer(invocacion -> {
            existente.actualizarTipoYResultado(
                    AjusteProgramacionFecha.Tipo.REEMPLAZO, SALON, INSTRUCTOR, ACTIVIDAD,
                    LocalTime.NOON, LocalTime.of(13, 0));
            return null;
        }).when(persistence).refrescar(existente);

        assertThatThrownBy(() -> service.guardarTarget(
                serie, FECHA, AjusteProgramacionFecha.Tipo.CANCELACION, null))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining(ProgramacionErrores.CONFLICTO_LOCK_SET_DESACTUALIZADO);

        verify(persistence, never()).flushManaged();
    }

    @Test
    void retiroTargetComparaSnapshotPrevioContraRefreshDelFirstLevelCache() {
        UUID serie = UUID.randomUUID();
        OcurrenciaNominal ocurrencia = nominal(serie);
        AjusteProgramacionFecha existente = AjusteProgramacionFecha.nuevoTarget(
                UUID.randomUUID(), AjusteProgramacionFecha.Tipo.REEMPLAZO,
                serie, FECHA, SALON, INSTRUCTOR, ACTIVIDAD,
                LocalTime.of(10, 0), LocalTime.of(11, 0));
        when(nominal.porSerieYFecha(serie, FECHA))
                .thenReturn(java.util.List.of(ocurrencia));
        when(repository.findByAsignacionSerieIdAndFechaAndActivoTrue(serie, FECHA))
                .thenReturn(java.util.List.of(existente));
        doAnswer(invocacion -> {
            existente.actualizarResultado(
                    SALON, INSTRUCTOR, ACTIVIDAD, LocalTime.NOON, LocalTime.of(13, 0));
            return null;
        }).when(persistence).refrescar(existente);

        assertThatThrownBy(() -> service.retirarTarget(serie, FECHA))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining(ProgramacionErrores.CONFLICTO_LOCK_SET_DESACTUALIZADO);

        assertThat(existente.isActivo()).isTrue();
        verify(persistence, never()).flushManaged();
    }

    private OcurrenciaNominal nominal(UUID serie) {
        return new OcurrenciaNominal(
                FECHA, serie, UUID.randomUUID(), UUID.randomUUID(), SALON,
                INSTRUCTOR, ACTIVIDAD, LocalTime.of(10, 0), LocalTime.of(11, 0));
    }

    private AjusteProgramacionFecha adicion(boolean activa) {
        AjusteProgramacionFecha ajuste = AjusteProgramacionFecha.nuevaAdicion(
                ID, FECHA, SALON, INSTRUCTOR, ACTIVIDAD,
                LocalTime.of(10, 0), LocalTime.of(11, 0));
        if (!activa) {
            ajuste.retirar();
        }
        return ajuste;
    }

    private AjusteProgramacionFechaService.Resultado resultado() {
        return new AjusteProgramacionFechaService.Resultado(
                SALON, INSTRUCTOR, ACTIVIDAD, LocalTime.of(10, 0), LocalTime.of(11, 0));
    }
}
