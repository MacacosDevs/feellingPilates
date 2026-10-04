package com.feelingpilates.ubicaciones.servicio;

import com.feelingpilates.ubicaciones.dominio.HorarioEfectivo;
import com.feelingpilates.ubicaciones.entidad.HorarioOperacion;
import com.feelingpilates.ubicaciones.entidad.SalonHorarioExcepcion;
import com.feelingpilates.ubicaciones.repositorio.HorarioOperacionRepository;
import com.feelingpilates.ubicaciones.repositorio.SalonHorarioExcepcionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class HorarioEfectivoSalonTest {

    private static final UUID SALON_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    /** 2026-08-24 es lunes: dia_semana = 1. */
    private static final LocalDate LUNES = LocalDate.of(2026, 8, 24);

    private SalonHorarioExcepcionRepository excepcionRepository;
    private HorarioOperacionRepository horarioOperacionRepository;
    private HorarioEfectivoSalon horarioEfectivoSalon;

    @BeforeEach
    void preparar() {
        excepcionRepository = mock(SalonHorarioExcepcionRepository.class);
        horarioOperacionRepository = mock(HorarioOperacionRepository.class);
        when(excepcionRepository.findBySalonIdAndFechaAndActivoTrue(any(), any())).thenReturn(Optional.empty());
        when(horarioOperacionRepository.findVigente(any(), any(short.class), any())).thenReturn(List.of());

        horarioEfectivoSalon = new HorarioEfectivoSalon(
                excepcionRepository, new HorarioOperacionResolver(horarioOperacionRepository));
    }

    @Test
    void excepcionCerradaGanaSobreElHorarioSemanal() {
        haySemanal(LocalTime.of(8, 0), LocalTime.of(20, 0));
        hayExcepcionCerrada();

        HorarioEfectivo efectivo = horarioEfectivoSalon.resolver(SALON_ID, LUNES);

        assertThat(efectivo.estado()).isEqualTo(HorarioEfectivo.Estado.CERRADO);
        assertThat(efectivo.estaCerrado()).isTrue();
        assertThat(efectivo.horaApertura()).isNull();
        assertThat(efectivo.contiene(LocalTime.of(9, 0), LocalTime.of(10, 0))).isFalse();
    }

    @Test
    void excepcionCerradaNiSiquieraConsultaElHorarioSemanal() {
        hayExcepcionCerrada();

        horarioEfectivoSalon.resolver(SALON_ID, LUNES);

        verifyNoInteractions(horarioOperacionRepository);
    }

    @Test
    void excepcionAbiertaGanaSobreElHorarioSemanal() {
        haySemanal(LocalTime.of(8, 0), LocalTime.of(20, 0));
        hayExcepcionAbierta(LocalTime.of(7, 0), LocalTime.of(22, 0));

        HorarioEfectivo efectivo = horarioEfectivoSalon.resolver(SALON_ID, LUNES);

        assertThat(efectivo.estado()).isEqualTo(HorarioEfectivo.Estado.ABIERTO);
        assertThat(efectivo.origen()).isEqualTo(HorarioEfectivo.Origen.EXCEPCION);
        assertThat(efectivo.horaApertura()).isEqualTo(LocalTime.of(7, 0));
        assertThat(efectivo.horaCierre()).isEqualTo(LocalTime.of(22, 0));
    }

    @Test
    void sinExcepcionUsaElHorarioSemanalVigente() {
        haySemanal(LocalTime.of(8, 0), LocalTime.of(20, 0));

        HorarioEfectivo efectivo = horarioEfectivoSalon.resolver(SALON_ID, LUNES);

        assertThat(efectivo.estado()).isEqualTo(HorarioEfectivo.Estado.ABIERTO);
        assertThat(efectivo.origen()).isEqualTo(HorarioEfectivo.Origen.SEMANAL);
        assertThat(efectivo.horaApertura()).isEqualTo(LocalTime.of(8, 0));
        assertThat(efectivo.horaCierre()).isEqualTo(LocalTime.of(20, 0));
    }

    @Test
    void sinExcepcionNiHorarioSemanalEsNoOperativo() {
        HorarioEfectivo efectivo = horarioEfectivoSalon.resolver(SALON_ID, LUNES);

        assertThat(efectivo.estado()).isEqualTo(HorarioEfectivo.Estado.NO_OPERATIVO);
        assertThat(efectivo.estaCerrado()).isFalse();
        assertThat(efectivo.estaAbierto()).isFalse();
    }

    /**
     * NO_OPERATIVO y CERRADO no son lo mismo: el salon "cerrado ese dia" es una decision explicita
     * (excepcion) y el "sin horario configurado" es ausencia de configuracion.
     */
    @Test
    void noOperativoYCerradoNoSeColapsan() {
        HorarioEfectivo sinNada = horarioEfectivoSalon.resolver(SALON_ID, LUNES);
        hayExcepcionCerrada();
        HorarioEfectivo cerrado = horarioEfectivoSalon.resolver(SALON_ID, LUNES);

        assertThat(sinNada.estado()).isNotEqualTo(cerrado.estado());
    }

    /**
     * {@code SalonHorarioExcepcionService.guardar} nunca consulta HorarioOperacion, asi que un
     * horario especial en un dia sin plantilla semanal es un estado legitimo del modelo actual:
     * ese dia el salon abre con el horario de la excepcion.
     */
    @Test
    void excepcionAbiertaAplicaAunqueNoHayaHorarioSemanalParaEseDia() {
        hayExcepcionAbierta(LocalTime.of(10, 0), LocalTime.of(14, 0));

        HorarioEfectivo efectivo = horarioEfectivoSalon.resolver(SALON_ID, LUNES);

        assertThat(efectivo.estado()).isEqualTo(HorarioEfectivo.Estado.ABIERTO);
        assertThat(efectivo.horaApertura()).isEqualTo(LocalTime.of(10, 0));
    }

    @Test
    void consultaElHorarioSemanalConElDiaDeLaSemanaDeLaFecha() {
        horarioEfectivoSalon.resolver(SALON_ID, LUNES);

        verify(horarioOperacionRepository).findVigente(SALON_ID, (short) 1, LUNES);
    }

    @Test
    void rechazaFechaNula() {
        assertThatThrownBy(() -> horarioEfectivoSalon.resolver(SALON_ID, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void nuncaUsaElRelojDelSistemaSinoLaFechaRecibida() {
        LocalDate fechaLejana = LocalDate.of(2031, 3, 5);
        HorarioOperacion horario = new HorarioOperacion();
        horario.setHoraApertura(LocalTime.of(6, 0));
        horario.setHoraCierre(LocalTime.of(9, 0));
        when(horarioOperacionRepository.findVigente(SALON_ID, (short) 3, fechaLejana))
                .thenReturn(List.of(horario));

        HorarioEfectivo efectivo = horarioEfectivoSalon.resolver(SALON_ID, fechaLejana);

        assertThat(efectivo.horaApertura()).isEqualTo(LocalTime.of(6, 0));
        verify(excepcionRepository).findBySalonIdAndFechaAndActivoTrue(SALON_ID, fechaLejana);
    }

    private void haySemanal(LocalTime apertura, LocalTime cierre) {
        HorarioOperacion horario = new HorarioOperacion();
        horario.setDiaSemana((short) 1);
        horario.setHoraApertura(apertura);
        horario.setHoraCierre(cierre);
        when(horarioOperacionRepository.findVigente(SALON_ID, (short) 1, LUNES)).thenReturn(List.of(horario));
    }

    private void hayExcepcionCerrada() {
        SalonHorarioExcepcion excepcion = new SalonHorarioExcepcion();
        excepcion.setFecha(LUNES);
        excepcion.setCerrado(true);
        when(excepcionRepository.findBySalonIdAndFechaAndActivoTrue(SALON_ID, LUNES))
                .thenReturn(Optional.of(excepcion));
    }

    private void hayExcepcionAbierta(LocalTime apertura, LocalTime cierre) {
        SalonHorarioExcepcion excepcion = new SalonHorarioExcepcion();
        excepcion.setFecha(LUNES);
        excepcion.setCerrado(false);
        excepcion.setHoraApertura(apertura);
        excepcion.setHoraCierre(cierre);
        when(excepcionRepository.findBySalonIdAndFechaAndActivoTrue(SALON_ID, LUNES))
                .thenReturn(Optional.of(excepcion));
    }
}
