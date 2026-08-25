package com.feelingpilates.calendario.servicio;

import com.feelingpilates.calendario.entidad.Reserva;
import com.feelingpilates.calendario.entidad.TurnoInstructor;
import com.feelingpilates.calendario.repositorio.ReservaRepository;
import com.feelingpilates.calendario.repositorio.TurnoInstructorRepository;
import com.feelingpilates.ubicaciones.dominio.CambioExcepcionHorario;
import com.feelingpilates.ubicaciones.dominio.ConflictoProgramacionPuntual;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Adapter de {@code calendario} que reporta impacto puntual (turnos EXCEPCION, reservas
 * CONFIRMADA) para el port {@code ValidadorImpactoExcepcionHorario} de {@code ubicaciones}.
 */
class ImpactoPuntualEnExcepcionHorarioTest {

    private static final UUID SALON_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final LocalDate FECHA = LocalDate.of(2026, 9, 14);
    private static final LocalTime OCHO = LocalTime.of(8, 0);
    private static final LocalTime DIECISEIS = LocalTime.of(16, 0);

    private TurnoInstructorRepository turnoRepository;
    private ReservaRepository reservaRepository;
    private ImpactoPuntualEnExcepcionHorario adapter;

    @BeforeEach
    void preparar() {
        turnoRepository = mock(TurnoInstructorRepository.class);
        reservaRepository = mock(ReservaRepository.class);
        when(turnoRepository.buscarExcepcionesPorSalonYFecha(any(), any())).thenReturn(List.of());
        when(reservaRepository.findBySalonIdAndFechaAndEstado(any(), any(), any())).thenReturn(List.of());
        adapter = new ImpactoPuntualEnExcepcionHorario(turnoRepository, reservaRepository);
    }

    // ---------- I1: CERRADO, turno EXCEPCION activo bloquea; RECURRENTE ni se consulta ----------

    @Test
    void cerradoConTurnoExcepcionActivoEsConflicto() {
        TurnoInstructor excepcion = turno(LocalTime.of(9, 0), LocalTime.of(10, 0));
        when(turnoRepository.buscarExcepcionesPorSalonYFecha(SALON_ID, FECHA)).thenReturn(List.of(excepcion));

        List<ConflictoProgramacionPuntual> conflictos = adapter.evaluar(CambioExcepcionHorario.cerrado(SALON_ID, FECHA));

        assertThat(conflictos).hasSize(1);
        assertThat(conflictos.get(0).origen()).isEqualTo(ConflictoProgramacionPuntual.Origen.TURNO_EXCEPCION);
        assertThat(conflictos.get(0).id()).isEqualTo(excepcion.getId());
    }

    @Test
    void laConsultaDeRecurrentesNuncaSeInvoca() {
        adapter.evaluar(CambioExcepcionHorario.cerrado(SALON_ID, FECHA));

        verify(turnoRepository, never()).buscarRecurrentesPorSalonYDia(any(), any());
    }

    /**
     * CANCELACION cubre el dia como marcador de "no atiende" (00:00-23:59), no un intervalo
     * operativo real. La query del repositorio ya filtra {@code tipo = 'EXCEPCION'}, asi que un
     * salon con solo cancelaciones esa fecha se comporta como "sin turnos puntuales".
     */
    @Test
    void cerradoSinTurnosExcepcionActivosNoEsConflictoAunqueExistaUnaCancelacion() {
        List<ConflictoProgramacionPuntual> conflictos = adapter.evaluar(CambioExcepcionHorario.cerrado(SALON_ID, FECHA));

        assertThat(conflictos).isEmpty();
    }

    // ---------- I2: HORARIO_ESPECIAL 08-16, contencion completa (no solape) ----------

    @ParameterizedTest
    @CsvSource({
            "10:00,12:00,false",
            "08:00,16:00,false",
            "16:00,17:00,true",
            "07:00,09:00,true",
            "07:00,18:00,true",
    })
    void horarioEspecialContraTurnoExcepcion(String inicio, String fin, boolean esperaConflicto) {
        TurnoInstructor excepcion = turno(LocalTime.parse(inicio), LocalTime.parse(fin));
        when(turnoRepository.buscarExcepcionesPorSalonYFecha(SALON_ID, FECHA)).thenReturn(List.of(excepcion));

        List<ConflictoProgramacionPuntual> conflictos = adapter.evaluar(
                CambioExcepcionHorario.horarioEspecial(SALON_ID, FECHA, OCHO, DIECISEIS));

        assertThat(conflictos).hasSize(esperaConflicto ? 1 : 0);
    }

    // ---------- I3: Reserva.CONFIRMADA ----------

    @Test
    void reservaConfirmadaContenidaEsCompatible() {
        when(reservaRepository.findBySalonIdAndFechaAndEstado(SALON_ID, FECHA, Reserva.Estado.CONFIRMADA))
                .thenReturn(List.of(reserva(LocalTime.of(10, 0), LocalTime.of(11, 0))));

        List<ConflictoProgramacionPuntual> conflictos = adapter.evaluar(
                CambioExcepcionHorario.horarioEspecial(SALON_ID, FECHA, OCHO, DIECISEIS));

        assertThat(conflictos).isEmpty();
    }

    @Test
    void reservaConfirmadaParcialmenteFueraEsConflicto() {
        Reserva reserva = reserva(LocalTime.of(15, 0), LocalTime.of(17, 0));
        when(reservaRepository.findBySalonIdAndFechaAndEstado(SALON_ID, FECHA, Reserva.Estado.CONFIRMADA))
                .thenReturn(List.of(reserva));

        List<ConflictoProgramacionPuntual> conflictos = adapter.evaluar(
                CambioExcepcionHorario.horarioEspecial(SALON_ID, FECHA, OCHO, DIECISEIS));

        assertThat(conflictos).hasSize(1);
        assertThat(conflictos.get(0).origen()).isEqualTo(ConflictoProgramacionPuntual.Origen.RESERVA_CONFIRMADA);
        assertThat(conflictos.get(0).id()).isEqualTo(reserva.getId());
    }

    @Test
    void reservaConfirmadaConCerradoEsConflicto() {
        when(reservaRepository.findBySalonIdAndFechaAndEstado(SALON_ID, FECHA, Reserva.Estado.CONFIRMADA))
                .thenReturn(List.of(reserva(LocalTime.of(10, 0), LocalTime.of(11, 0))));

        List<ConflictoProgramacionPuntual> conflictos = adapter.evaluar(CambioExcepcionHorario.cerrado(SALON_ID, FECHA));

        assertThat(conflictos).hasSize(1);
    }

    // ---------- I4: Reserva.CANCELADA nunca bloquea ----------

    @Test
    void soloConsultaReservasConfirmadasNuncaCanceladas() {
        adapter.evaluar(CambioExcepcionHorario.cerrado(SALON_ID, FECHA));

        verify(reservaRepository).findBySalonIdAndFechaAndEstado(SALON_ID, FECHA, Reserva.Estado.CONFIRMADA);
        verify(reservaRepository, never())
                .findBySalonIdAndFechaAndEstado(SALON_ID, FECHA, Reserva.Estado.CANCELADA);
    }

    // ---------- helpers ----------

    private TurnoInstructor turno(LocalTime inicio, LocalTime fin) {
        TurnoInstructor t = new TurnoInstructor();
        t.setId(UUID.randomUUID());
        t.setTipo(TurnoInstructor.Tipo.EXCEPCION);
        t.setFecha(FECHA);
        t.setHoraInicio(inicio);
        t.setHoraFin(fin);
        return t;
    }

    private Reserva reserva(LocalTime inicio, LocalTime fin) {
        Reserva r = new Reserva();
        r.setId(UUID.randomUUID());
        r.setFecha(FECHA);
        r.setHoraInicio(inicio);
        r.setHoraFin(fin);
        r.setEstado(Reserva.Estado.CONFIRMADA);
        return r;
    }
}
