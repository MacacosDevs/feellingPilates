package com.feelingpilates.ubicaciones.servicio;

import com.feelingpilates.exception.ResourceNotFoundException;
import com.feelingpilates.exception.ValidacionException;
import com.feelingpilates.ubicaciones.dominio.CambioHorarioOperacion;
import com.feelingpilates.ubicaciones.dominio.ConflictoProgramacion;
import com.feelingpilates.ubicaciones.dominio.ValidadorImpactoCambioHorarioOperacion;
import com.feelingpilates.ubicaciones.entidad.HorarioOperacion;
import com.feelingpilates.ubicaciones.entidad.Salon;
import com.feelingpilates.ubicaciones.repositorio.HorarioOperacionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyShort;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * Unitarios de {@link CerrarHorarioOperacion} con mocks, con foco en el orden temporal normativo
 * a -> b -> c de la clasificacion, que es contractual y produce un unico codigo por estado.
 */
class CerrarHorarioOperacionTest {

    private static final UUID SALON_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final short LUNES = 1;
    private static final LocalDate HOY = LocalDate.of(2026, 8, 20);
    private static final LocalDate AGO_25 = LocalDate.of(2026, 8, 25);
    private static final LocalDate AGO_31 = LocalDate.of(2026, 8, 31);
    private static final LocalDate SEP_1 = LocalDate.of(2026, 9, 1);
    private static final LocalDate SEP_5 = LocalDate.of(2026, 9, 5);
    private static final LocalDate SEP_15 = LocalDate.of(2026, 9, 15);
    private static final LocalTime OCHO = LocalTime.of(8, 0);
    private static final LocalTime VEINTE = LocalTime.of(20, 0);
    private static final Clock RELOJ = Clock.fixed(
            HOY.atStartOfDay(ZoneId.of("UTC")).toInstant(), ZoneId.of("UTC"));

    private SalonLock salonLock;
    private HorarioOperacionRepository horarioRepository;
    private ValidadorImpactoCambioHorarioOperacion validador;
    private Salon salon;
    private CerrarHorarioOperacion writer;

    @BeforeEach
    void preparar() {
        salonLock = mock(SalonLock.class);
        horarioRepository = mock(HorarioOperacionRepository.class);
        validador = mock(ValidadorImpactoCambioHorarioOperacion.class);

        salon = new Salon();
        salon.setId(SALON_ID);
        when(salonLock.adquirir(SALON_ID)).thenReturn(salon);
        when(validador.evaluar(any())).thenReturn(List.of());
        when(horarioRepository.saveAndFlush(any(HorarioOperacion.class)))
                .thenAnswer(i -> i.getArgument(0));

        writer = new CerrarHorarioOperacion(salonLock, horarioRepository, List.of(validador), RELOJ);
    }

    // ---------- 1. cierre valido ----------

    @Test
    void cierreDeLegacyNullNullDejaLaVersionEnDMenosUno() {
        HorarioOperacion legacy = version(null, null);
        versionesExistentes(legacy);

        HorarioOperacion cerrada = cerrar(SEP_1);

        assertThat(cerrada).isSameAs(legacy);
        assertThat(legacy.getVigenteDesde()).isNull();
        assertThat(legacy.getVigenteHasta()).isEqualTo(AGO_31);
    }

    @Test
    void cierreDeVersionActualAbiertaConservaSuVigenteDesdeYNoInsertaSucesora() {
        HorarioOperacion actual = version(SEP_1, null);
        versionesExistentes(actual);

        cerrar(SEP_15);

        assertThat(actual.getVigenteDesde()).isEqualTo(SEP_1);
        assertThat(actual.getVigenteHasta()).isEqualTo(SEP_15.minusDays(1));
        // Una sola escritura: nunca se crea una version sucesora.
        verify(horarioRepository, org.mockito.Mockito.times(1)).saveAndFlush(any());
    }

    // ---------- 2. no existe version que contiene D ----------

    @Test
    void sinNingunaVersionSeRechazaPorNoExistirVigenteEnEsaFecha() {
        versionesExistentes();

        assertThatThrownBy(() -> cerrar(SEP_1))
                .isInstanceOf(ValidacionException.class)
                .hasMessageContaining("NO_EXISTE_VERSION_VIGENTE_EN_ESA_FECHA");

        verify(horarioRepository, never()).saveAndFlush(any());
    }

    // ---------- 3. D en el pasado ----------

    @Test
    void efectivoDesdeEnElPasadoSeRechazaAntesDeLaClasificacionTemporal() {
        assertThatThrownBy(() -> cerrar(HOY.minusDays(1)))
                .isInstanceOf(ValidacionException.class)
                .hasMessageContaining("EFECTIVO_DESDE_EN_EL_PASADO");

        // Ni siquiera se toma el lock ni se leen versiones.
        verifyNoMoreInteractions(salonLock, horarioRepository);
    }

    // ---------- 4. D == vigenteDesde ----------

    @Test
    void dIgualAVigenteDesdeEsCancelacionDeVersionNoSoportada() {
        HorarioOperacion anterior = version(null, AGO_31);
        HorarioOperacion actual = version(SEP_1, null);
        // La query [1-sep, +inf) no devuelve la anterior (termina el 31-ago).
        versionesExistentes(actual);

        assertThatThrownBy(() -> cerrar(SEP_1))
                .isInstanceOf(ValidacionException.class)
                .hasMessageContaining("CANCELACION_DE_VERSION_NO_SOPORTADA");

        assertThat(actual.getVigenteHasta()).isNull();
        assertThat(anterior.getVigenteHasta()).isEqualTo(AGO_31);
        verify(horarioRepository, never()).saveAndFlush(any());
    }

    /**
     * Barrera explicita contra el codigo descartado: {@code CIERRE_COINCIDE_CON_INICIO_VERSION} no
     * existe como codigo publico y no debe emitirse nunca.
     */
    @Test
    void dIgualAVigenteDesdeNoEmiteElCodigoDescartado() {
        versionesExistentes(version(SEP_1, null));

        assertThatThrownBy(() -> cerrar(SEP_1))
                .hasMessageNotContaining("CIERRE_COINCIDE_CON_INICIO_VERSION");
    }

    // ---------- 5. version futura ----------

    @Test
    void versionFuturaPlanificadaRechazaElCierre() {
        HorarioOperacion actual = version(null, AGO_31);
        HorarioOperacion futura = version(SEP_1, null);
        versionesExistentes(actual, futura);

        assertThatThrownBy(() -> cerrar(AGO_25))
                .isInstanceOf(ValidacionException.class)
                .hasMessageContaining("CIERRE_CON_VERSIONES_FUTURAS");

        verify(horarioRepository, never()).saveAndFlush(any());
    }

    // ---------- 6. gap + futuro -> gana (a), no (c) ----------

    @Test
    void dEnGapConVersionFuturaRespondeNoExisteVigenteYNoCierreConFuturas() {
        // A: -inf..31-ago (no la devuelve la query desde 5-sep), B: 15-sep..+inf.
        versionesExistentes(version(SEP_15, null));

        assertThatThrownBy(() -> cerrar(SEP_5))
                .isInstanceOf(ValidacionException.class)
                .hasMessageContaining("NO_EXISTE_VERSION_VIGENTE_EN_ESA_FECHA")
                .hasMessageNotContaining("CIERRE_CON_VERSIONES_FUTURAS");
    }

    // ---------- 7/8. conflictos de validacion inversa ----------

    @Test
    void conflictoDeTurnoRecurrenteRechazaElCierre() {
        versionesExistentes(version(null, null));
        UUID turnoId = UUID.randomUUID();
        when(validador.evaluar(any()))
                .thenReturn(List.of(ConflictoProgramacion.turnoRecurrente(turnoId, "08:00-09:00")));

        assertThatThrownBy(() -> cerrar(SEP_1))
                .isInstanceOf(ValidacionException.class)
                .hasMessageContaining("PROGRAMACION_INCOMPATIBLE_CON_HORARIO")
                .hasMessageContaining(turnoId.toString());

        verify(horarioRepository, never()).saveAndFlush(any());
    }

    @Test
    void conflictoDeBloqueRechazaElCierre() {
        versionesExistentes(version(null, null));
        UUID bloqueId = UUID.randomUUID();
        when(validador.evaluar(any()))
                .thenReturn(List.of(ConflictoProgramacion.bloqueProgramacion(bloqueId, "08:00-09:00")));

        assertThatThrownBy(() -> cerrar(SEP_1))
                .isInstanceOf(ValidacionException.class)
                .hasMessageContaining(bloqueId.toString());
    }

    @Test
    void elCambioQueVeElValidadorEsCerradoYNoPortaHoras() {
        versionesExistentes(version(null, null));
        ArgumentCaptor<CambioHorarioOperacion> captor = ArgumentCaptor.forClass(CambioHorarioOperacion.class);

        cerrar(SEP_1);

        verify(validador).evaluar(captor.capture());
        CambioHorarioOperacion cambio = captor.getValue();
        assertThat(cambio.estaAbierto()).isFalse();
        assertThat(cambio.horaApertura()).isNull();
        assertThat(cambio.horaCierre()).isNull();
        assertThat(cambio.efectivoDesde()).isEqualTo(SEP_1);
        // Un dia cerrado no admite ninguna programacion.
        assertThat(cambio.admite(OCHO, VEINTE)).isFalse();
    }

    // ---------- 9. el rechazo no modifica filas ----------

    @Test
    void elRechazoPorFuturaNoModificaNingunaFila() {
        HorarioOperacion actual = version(null, AGO_31);
        HorarioOperacion futura = version(SEP_1, null);
        versionesExistentes(actual, futura);

        assertThatThrownBy(() -> cerrar(AGO_25)).isInstanceOf(ValidacionException.class);

        assertThat(actual.getVigenteDesde()).isNull();
        assertThat(actual.getVigenteHasta()).isEqualTo(AGO_31);
        assertThat(futura.getVigenteDesde()).isEqualTo(SEP_1);
        assertThat(futura.getVigenteHasta()).isNull();
        verify(horarioRepository, never()).saveAndFlush(any());
    }

    // ---------- 10. lock ANTES de leer ----------

    @Test
    void elLockDeSalonSeAdquiereAntesDeLeerLasVersiones() {
        versionesExistentes(version(null, null));

        cerrar(SEP_1);

        InOrder orden = inOrder(salonLock, horarioRepository, validador);
        orden.verify(salonLock).adquirir(SALON_ID);
        orden.verify(horarioRepository).bloquearVersionesQueIntersectan(
                eq(SALON_ID), eq(LUNES), eq(SEP_1), nullable(LocalDate.class));
        orden.verify(validador).evaluar(any());
        orden.verify(horarioRepository).saveAndFlush(any());
    }

    // ---------- validaciones de forma ----------

    @Test
    void diaSemanaFueraDeRangoSeRechaza() {
        assertThatThrownBy(() -> writer.ejecutar(
                new CerrarHorarioOperacion.CerrarHorario(SALON_ID, (short) 7, SEP_1)))
                .isInstanceOf(ValidacionException.class)
                .hasMessageContaining("DIA_SEMANA_INVALIDO");
    }

    @Test
    void salonInexistenteFallaEnElLock() {
        when(salonLock.adquirir(SALON_ID)).thenThrow(new ResourceNotFoundException("Salón no encontrado"));

        assertThatThrownBy(() -> cerrar(SEP_1)).isInstanceOf(ResourceNotFoundException.class);
    }

    // ---------- helpers ----------

    private HorarioOperacion cerrar(LocalDate efectivoDesde) {
        return writer.ejecutar(new CerrarHorarioOperacion.CerrarHorario(SALON_ID, LUNES, efectivoDesde));
    }

    private void versionesExistentes(HorarioOperacion... versiones) {
        when(horarioRepository.bloquearVersionesQueIntersectan(
                eq(SALON_ID), anyShort(), any(), nullable(LocalDate.class)))
                .thenReturn(List.of(versiones));
    }

    private HorarioOperacion version(LocalDate desde, LocalDate hasta) {
        HorarioOperacion horario = new HorarioOperacion();
        horario.setId(UUID.randomUUID());
        horario.setSalon(salon);
        horario.setDiaSemana(LUNES);
        horario.setHoraApertura(OCHO);
        horario.setHoraCierre(VEINTE);
        horario.setVigenteDesde(desde);
        horario.setVigenteHasta(hasta);
        return horario;
    }
}
