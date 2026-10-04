package com.feelingpilates.calendario.servicio;

import com.feelingpilates.calendario.entidad.TurnoInstructor;
import com.feelingpilates.calendario.repositorio.TurnoInstructorRepository;
import com.feelingpilates.ubicaciones.dominio.CambioHorarioOperacion;
import com.feelingpilates.ubicaciones.dominio.ConflictoProgramacion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyShort;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class ImpactoTurnosRecurrentesEnHorarioTest {

    private static final UUID SALON_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final short LUNES = 1;
    private static final LocalDate SEP_1 = LocalDate.of(2026, 9, 1);

    private TurnoInstructorRepository turnoRepository;
    private ImpactoTurnosRecurrentesEnHorario adapter;

    @BeforeEach
    void preparar() {
        turnoRepository = mock(TurnoInstructorRepository.class);
        adapter = new ImpactoTurnosRecurrentesEnHorario(turnoRepository);
    }

    @Test
    void turnoQueCabeEnElHorarioNuevoNoEsConflicto() {
        when(turnoRepository.buscarRecurrentesPorSalonYDia(SALON_ID, LUNES))
                .thenReturn(List.of(turno(9, 12)));

        assertThat(adapter.evaluar(abierto(9, 20))).isEmpty();
    }

    @Test
    void turnoQueEmpiezaAntesDeLaNuevaAperturaEsConflicto() {
        TurnoInstructor turno = turno(8, 9);
        when(turnoRepository.buscarRecurrentesPorSalonYDia(SALON_ID, LUNES)).thenReturn(List.of(turno));

        List<ConflictoProgramacion> conflictos = adapter.evaluar(abierto(9, 20));

        assertThat(conflictos).singleElement().satisfies(c -> {
            assertThat(c.origen()).isEqualTo(ConflictoProgramacion.Origen.TURNO_RECURRENTE);
            assertThat(c.id()).isEqualTo(turno.getId());
            assertThat(c.detalle()).isEqualTo("08:00-09:00");
        });
    }

    @Test
    void turnoQueTerminaDespuesDelNuevoCierreEsConflicto() {
        when(turnoRepository.buscarRecurrentesPorSalonYDia(SALON_ID, LUNES))
                .thenReturn(List.of(turno(18, 21)));

        assertThat(adapter.evaluar(abierto(8, 20))).hasSize(1);
    }

    @Test
    void aFronterasExactasElTurnoCabe() {
        when(turnoRepository.buscarRecurrentesPorSalonYDia(SALON_ID, LUNES))
                .thenReturn(List.of(turno(8, 20)));

        assertThat(adapter.evaluar(abierto(8, 20))).isEmpty();
    }

    @Test
    void alCerrarElDiaCualquierRecurrenteActivoEsConflicto() {
        when(turnoRepository.buscarRecurrentesPorSalonYDia(SALON_ID, LUNES))
                .thenReturn(List.of(turno(9, 12), turno(14, 16)));

        List<ConflictoProgramacion> conflictos = adapter.evaluar(
                CambioHorarioOperacion.cerrado(SALON_ID, LUNES, SEP_1));

        assertThat(conflictos).hasSize(2);
    }

    @Test
    void sinTurnosNoHayConflicto() {
        when(turnoRepository.buscarRecurrentesPorSalonYDia(SALON_ID, LUNES)).thenReturn(List.of());

        assertThat(adapter.evaluar(abierto(9, 20))).isEmpty();
        assertThat(adapter.evaluar(CambioHorarioOperacion.cerrado(SALON_ID, LUNES, SEP_1))).isEmpty();
    }

    /**
     * Turno EXCEPCION esta deliberadamente fuera de la Politica A: el adapter solo usa la query de
     * recurrentes y no consulta ninguna otra. Si alguien lo ampliara, esta aserción lo detecta.
     */
    @Test
    void noConsultaTurnosExcepcionNiCancelacion() {
        when(turnoRepository.buscarRecurrentesPorSalonYDia(any(), anyShort())).thenReturn(List.of());

        adapter.evaluar(abierto(9, 20));

        verify(turnoRepository).buscarRecurrentesPorSalonYDia(SALON_ID, LUNES);
        verifyNoMoreInteractions(turnoRepository);
    }

    private CambioHorarioOperacion abierto(int apertura, int cierre) {
        return CambioHorarioOperacion.abierto(
                SALON_ID, LUNES, SEP_1, LocalTime.of(apertura, 0), LocalTime.of(cierre, 0));
    }

    private TurnoInstructor turno(int inicio, int fin) {
        TurnoInstructor turno = new TurnoInstructor();
        turno.setId(UUID.randomUUID());
        turno.setTipo(TurnoInstructor.Tipo.RECURRENTE);
        turno.setDiaSemana(LUNES);
        turno.setHoraInicio(LocalTime.of(inicio, 0));
        turno.setHoraFin(LocalTime.of(fin, 0));
        return turno;
    }
}
