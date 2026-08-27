package com.feelingpilates.programacion.servicio;

import com.feelingpilates.exception.ConflictException;
import com.feelingpilates.programacion.entidad.AjusteProgramacionFecha;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class StaleDiscoveryAjusteProgramacionTest {

    private static final UUID ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private static final UUID SALON = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
    private static final UUID INSTRUCTOR = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc");
    private static final UUID ACTIVIDAD = UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd");
    private static final LocalDate FECHA = LocalDate.of(2027, 1, 4);

    private AjusteProgramacionFechaRepository repository;
    private AjusteProgramacionFechaPersistence persistence;
    private ProgramacionNominal nominal;
    private AplicadorAjustesProgramacion aplicador;
    private ProgramacionValidador validador;
    private AjusteProgramacionFechaService service;

    @BeforeEach
    void preparar() {
        repository = mock(AjusteProgramacionFechaRepository.class);
        persistence = mock(AjusteProgramacionFechaPersistence.class);
        nominal = mock(ProgramacionNominal.class);
        aplicador = mock(AplicadorAjustesProgramacion.class);
        validador = mock(ProgramacionValidador.class);
        service = new AjusteProgramacionFechaService(
                repository, persistence, nominal, aplicador, validador,
                mock(SalonLocks.class), mock(InstructorLocks.class),
                Clock.fixed(Instant.parse("2026-08-26T12:00:00Z"), ZoneOffset.UTC));
    }

    @Test
    void testARamaAAbortaSiApareceFilaActiva() {
        AjusteProgramacionFecha aparecida = adicion();
        when(repository.findById(ID)).thenReturn(Optional.empty(), Optional.of(aparecida));

        assertRamaA(aparecida, true);
    }

    @Test
    void testARamaAAbortaSiApareceFilaInactiva() {
        AjusteProgramacionFecha aparecida = adicion();
        aparecida.retirar();
        when(repository.findById(ID)).thenReturn(Optional.empty(), Optional.of(aparecida));

        assertRamaA(aparecida, false);
    }

    private void assertRamaA(AjusteProgramacionFecha aparecida, boolean activoEsperado) {
        assertThatThrownBy(() -> service.guardarAdicion(ID, FECHA, resultado()))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining(ProgramacionErrores.CONFLICTO_AJUSTE_PROGRAMACION);

        assertThat(aparecida.isActivo()).isEqualTo(activoEsperado);
        verify(persistence, never()).crear(org.mockito.ArgumentMatchers.any());
        verify(persistence, never()).flushManaged();
        verify(nominal, never()).todasEnFecha(org.mockito.ArgumentMatchers.any());
        verify(aplicador, never()).aplicar(
                org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
        verify(validador, never()).validarMutacion(
                org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }

    private AjusteProgramacionFecha adicion() {
        return AjusteProgramacionFecha.nuevaAdicion(
                ID, FECHA, SALON, INSTRUCTOR, ACTIVIDAD,
                LocalTime.of(10, 0), LocalTime.of(11, 0));
    }

    private AjusteProgramacionFechaService.Resultado resultado() {
        return new AjusteProgramacionFechaService.Resultado(
                SALON, INSTRUCTOR, ACTIVIDAD, LocalTime.of(10, 0), LocalTime.of(11, 0));
    }
}
