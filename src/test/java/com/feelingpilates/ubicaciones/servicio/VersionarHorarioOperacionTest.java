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
 * Unitarios de {@link VersionarHorarioOperacion} con mocks: clasificacion de edge cases, orden del
 * protocolo (lock -> lectura -> validacion inversa -> escritura) y orden de flush.
 */
class VersionarHorarioOperacionTest {

    private static final UUID SALON_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final short LUNES = 1;
    private static final LocalDate HOY = LocalDate.of(2026, 8, 20);
    private static final LocalDate SEP_1 = LocalDate.of(2026, 9, 1);
    private static final LocalDate AGO_31 = LocalDate.of(2026, 8, 31);
    private static final LocalDate SEP_15 = LocalDate.of(2026, 9, 15);
    private static final LocalTime OCHO = LocalTime.of(8, 0);
    private static final LocalTime VEINTE = LocalTime.of(20, 0);
    private static final LocalTime NUEVE = LocalTime.of(9, 0);
    private static final Clock RELOJ = Clock.fixed(
            HOY.atStartOfDay(ZoneId.of("UTC")).toInstant(), ZoneId.of("UTC"));

    private SalonLock salonLock;
    private HorarioOperacionRepository horarioRepository;
    private ValidadorImpactoCambioHorarioOperacion validador;
    private Salon salon;
    private VersionarHorarioOperacion writer;

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

        writer = new VersionarHorarioOperacion(
                salonLock, horarioRepository, List.of(validador), RELOJ);
    }

    // ---------- 1. alta sin historia ----------

    @Test
    void altaSinHistoriaInsertaVersionAbiertaSinCerrarNada() {
        versionesExistentes();

        HorarioOperacion nueva = versionar(SEP_1, OCHO, VEINTE);

        assertThat(nueva.getVigenteDesde()).isEqualTo(SEP_1);
        assertThat(nueva.getVigenteHasta()).isNull();
        assertThat(nueva.getHoraApertura()).isEqualTo(OCHO);
        assertThat(nueva.getHoraCierre()).isEqualTo(VEINTE);
        assertThat(nueva.getSalon()).isSameAs(salon);
        verify(horarioRepository).saveAndFlush(any(HorarioOperacion.class));
    }

    // ---------- 2. legacy NULL/NULL ----------

    @Test
    void legacyNullNullSeCierraEnDMenosUnoYSeInsertaLaNueva() {
        HorarioOperacion legacy = version(null, null, OCHO, VEINTE);
        versionesExistentes(legacy);

        HorarioOperacion nueva = versionar(SEP_1, NUEVE, VEINTE);

        assertThat(legacy.getVigenteDesde()).isNull();
        assertThat(legacy.getVigenteHasta()).isEqualTo(AGO_31);
        assertThat(nueva.getVigenteDesde()).isEqualTo(SEP_1);
        assertThat(nueva.getVigenteHasta()).isNull();
    }

    // ---------- 3. append sobre version no legada ----------

    @Test
    void appendSobreVersionAbiertaConservaSuVigenteDesde() {
        HorarioOperacion actual = version(LocalDate.of(2026, 1, 1), null, OCHO, VEINTE);
        versionesExistentes(actual);

        HorarioOperacion nueva = versionar(SEP_1, NUEVE, VEINTE);

        assertThat(actual.getVigenteDesde()).isEqualTo(LocalDate.of(2026, 1, 1));
        assertThat(actual.getVigenteHasta()).isEqualTo(AGO_31);
        assertThat(nueva.getVigenteDesde()).isEqualTo(SEP_1);
    }

    // ---------- 4. reapertura tras historia cerrada ----------

    @Test
    void reaperturaTrasHistoriaCerradaInsertaSinTocarElPasado() {
        // La historia termino el 31-ago; la query de [15-sep, +inf) no la devuelve.
        versionesExistentes();

        HorarioOperacion nueva = versionar(SEP_15, OCHO, VEINTE);

        assertThat(nueva.getVigenteDesde()).isEqualTo(SEP_15);
        assertThat(nueva.getVigenteHasta()).isNull();
        // Una sola escritura: la del INSERT. El gap 1-14 sep queda preservado.
        verify(horarioRepository).saveAndFlush(any(HorarioOperacion.class));
    }

    // ---------- 5. D en el pasado ----------

    @Test
    void efectivoDesdeEnElPasadoSeRechazaAntesDeTocarLaBase() {
        assertThatThrownBy(() -> versionar(HOY.minusDays(1), OCHO, VEINTE))
                .isInstanceOf(ValidacionException.class)
                .hasMessageContaining("EFECTIVO_DESDE_EN_EL_PASADO");

        verifyNoMoreInteractions(salonLock, horarioRepository);
    }

    @Test
    void efectivoDesdeIgualAHoyEsValido() {
        versionesExistentes();

        assertThat(versionar(HOY, OCHO, VEINTE).getVigenteDesde()).isEqualTo(HOY);
    }

    // ---------- 6. D == vigenteDesde ----------

    @Test
    void dIgualAVigenteDesdeSeRechazaSinUpdateInPlace() {
        HorarioOperacion existente = version(SEP_1, null, OCHO, VEINTE);
        versionesExistentes(existente);

        assertThatThrownBy(() -> versionar(SEP_1, NUEVE, VEINTE))
                .isInstanceOf(ValidacionException.class)
                .hasMessageContaining("YA_EXISTE_VERSION_EN_ESA_FECHA");

        // Ni UPDATE in-place de las horas, ni cierre en D-1 (dejaria un rango invalido).
        assertThat(existente.getHoraApertura()).isEqualTo(OCHO);
        assertThat(existente.getVigenteHasta()).isNull();
        verify(horarioRepository, never()).saveAndFlush(any());
    }

    // ---------- 7. futuro / intermedio rechazado ----------

    @Test
    void gapConVersionFuturaRechazaInsercionIntermedia() {
        // D=1-sep cae en un gap; solo existe la version futura del 15-sep.
        versionesExistentes(version(SEP_15, null, OCHO, VEINTE));

        assertThatThrownBy(() -> versionar(SEP_1, NUEVE, VEINTE))
                .isInstanceOf(ValidacionException.class)
                .hasMessageContaining("VERSIONADO_INTERMEDIO_NO_SOPORTADO");

        verify(horarioRepository, never()).saveAndFlush(any());
    }

    @Test
    void versionQueContieneDConSucesoraPlanificadaRechazaElAppend() {
        HorarioOperacion actual = version(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 9, 30), OCHO, VEINTE);
        HorarioOperacion futura = version(LocalDate.of(2026, 10, 1), null, OCHO, VEINTE);
        versionesExistentes(actual, futura);

        assertThatThrownBy(() -> versionar(SEP_1, NUEVE, VEINTE))
                .isInstanceOf(ValidacionException.class)
                .hasMessageContaining("VERSIONADO_INTERMEDIO_NO_SOPORTADO");

        assertThat(actual.getVigenteHasta()).isEqualTo(LocalDate.of(2026, 9, 30));
        assertThat(futura.getVigenteDesde()).isEqualTo(LocalDate.of(2026, 10, 1));
        verify(horarioRepository, never()).saveAndFlush(any());
    }

    // ---------- 8/9. horas invalidas ----------

    @Test
    void aperturaIgualACierreSeRechaza() {
        assertThatThrownBy(() -> versionar(SEP_1, OCHO, OCHO))
                .isInstanceOf(ValidacionException.class)
                .hasMessageContaining("HORA_CIERRE_DEBE_SER_POSTERIOR");
    }

    @Test
    void aperturaPosteriorAlCierreSeRechaza() {
        assertThatThrownBy(() -> versionar(SEP_1, VEINTE, OCHO))
                .isInstanceOf(ValidacionException.class)
                .hasMessageContaining("HORA_CIERRE_DEBE_SER_POSTERIOR");
    }

    // ---------- 10. diaSemana invalido ----------

    @Test
    void diaSemanaFueraDeRangoSeRechaza() {
        assertThatThrownBy(() -> writer.ejecutar(new VersionarHorarioOperacion.VersionarHorario(
                SALON_ID, (short) -1, SEP_1, OCHO, VEINTE)))
                .isInstanceOf(ValidacionException.class)
                .hasMessageContaining("DIA_SEMANA_INVALIDO");

        assertThatThrownBy(() -> writer.ejecutar(new VersionarHorarioOperacion.VersionarHorario(
                SALON_ID, (short) 7, SEP_1, OCHO, VEINTE)))
                .isInstanceOf(ValidacionException.class)
                .hasMessageContaining("DIA_SEMANA_INVALIDO");
    }

    // ---------- 11. salon inexistente ----------

    @Test
    void salonInexistenteFallaEnElLock() {
        when(salonLock.adquirir(SALON_ID)).thenThrow(new ResourceNotFoundException("Salón no encontrado"));

        assertThatThrownBy(() -> versionar(SEP_1, OCHO, VEINTE))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(horarioRepository, never()).saveAndFlush(any());
    }

    // ---------- 12/13. conflictos de validacion inversa ----------

    @Test
    void conflictoDeTurnoRecurrenteRechazaElVersionadoEIdentificaElTurno() {
        versionesExistentes(version(null, null, OCHO, VEINTE));
        UUID turnoId = UUID.randomUUID();
        when(validador.evaluar(any()))
                .thenReturn(List.of(ConflictoProgramacion.turnoRecurrente(turnoId, "08:00-09:00")));

        assertThatThrownBy(() -> versionar(SEP_1, NUEVE, VEINTE))
                .isInstanceOf(ValidacionException.class)
                .hasMessageContaining("PROGRAMACION_INCOMPATIBLE_CON_HORARIO")
                .hasMessageContaining("TURNO_RECURRENTE")
                .hasMessageContaining(turnoId.toString());

        verify(horarioRepository, never()).saveAndFlush(any());
    }

    @Test
    void conflictoDeBloqueRechazaElVersionadoEIdentificaElBloque() {
        versionesExistentes(version(null, null, OCHO, VEINTE));
        UUID bloqueId = UUID.randomUUID();
        when(validador.evaluar(any()))
                .thenReturn(List.of(ConflictoProgramacion.bloqueProgramacion(bloqueId, "08:00-09:00")));

        assertThatThrownBy(() -> versionar(SEP_1, NUEVE, VEINTE))
                .isInstanceOf(ValidacionException.class)
                .hasMessageContaining("BLOQUE_PROGRAMACION")
                .hasMessageContaining(bloqueId.toString());
    }

    @Test
    void consultaTodosLosValidadoresRegistrados() {
        ValidadorImpactoCambioHorarioOperacion otro = mock(ValidadorImpactoCambioHorarioOperacion.class);
        when(otro.evaluar(any())).thenReturn(List.of());
        writer = new VersionarHorarioOperacion(
                salonLock, horarioRepository, List.of(validador, otro), RELOJ);
        versionesExistentes();

        versionar(SEP_1, OCHO, VEINTE);

        verify(validador).evaluar(any());
        verify(otro).evaluar(any());
    }

    @Test
    void elCambioQueVeElValidadorEsAbiertoConLasHorasNuevas() {
        versionesExistentes();
        ArgumentCaptor<CambioHorarioOperacion> captor = ArgumentCaptor.forClass(CambioHorarioOperacion.class);

        versionar(SEP_1, NUEVE, VEINTE);

        verify(validador).evaluar(captor.capture());
        CambioHorarioOperacion cambio = captor.getValue();
        assertThat(cambio.estaAbierto()).isTrue();
        assertThat(cambio.salonId()).isEqualTo(SALON_ID);
        assertThat(cambio.diaSemana()).isEqualTo(LUNES);
        assertThat(cambio.efectivoDesde()).isEqualTo(SEP_1);
        assertThat(cambio.horaApertura()).isEqualTo(NUEVE);
        assertThat(cambio.horaCierre()).isEqualTo(VEINTE);
    }

    // ---------- 14. validadores ANTES de persistir ----------

    @Test
    void laValidacionInversaOcurreAntesDeCualquierEscritura() {
        versionesExistentes(version(null, null, OCHO, VEINTE));

        versionar(SEP_1, NUEVE, VEINTE);

        InOrder orden = inOrder(validador, horarioRepository);
        orden.verify(validador).evaluar(any());
        orden.verify(horarioRepository, org.mockito.Mockito.atLeastOnce())
                .saveAndFlush(any(HorarioOperacion.class));
    }

    // ---------- 15. lock ANTES de leer ----------

    @Test
    void elLockDeSalonSeAdquiereAntesDeLeerLasVersiones() {
        versionesExistentes(version(null, null, OCHO, VEINTE));

        versionar(SEP_1, NUEVE, VEINTE);

        InOrder orden = inOrder(salonLock, horarioRepository, validador);
        orden.verify(salonLock).adquirir(SALON_ID);
        orden.verify(horarioRepository).bloquearVersionesQueIntersectan(
                eq(SALON_ID), eq(LUNES), eq(SEP_1), nullable(LocalDate.class));
        orden.verify(validador).evaluar(any());
    }

    // ---------- orden de flush: UPDATE -> flush -> INSERT -> flush ----------

    @Test
    void cierraLaVersionViejaConFlushAntesDeInsertarLaNueva() {
        HorarioOperacion legacy = version(null, null, OCHO, VEINTE);
        versionesExistentes(legacy);

        versionar(SEP_1, NUEVE, VEINTE);

        ArgumentCaptor<HorarioOperacion> captor = ArgumentCaptor.forClass(HorarioOperacion.class);
        verify(horarioRepository, org.mockito.Mockito.times(2)).saveAndFlush(captor.capture());
        List<HorarioOperacion> escrituras = captor.getAllValues();
        // Primero la vieja ya cerrada, despues la nueva: si el INSERT saliera primero, con la
        // vieja todavia abierta en PostgreSQL, el EXCLUDE lo rechazaria legitimamente.
        assertThat(escrituras.get(0)).isSameAs(legacy);
        assertThat(escrituras.get(0).getVigenteHasta()).isEqualTo(AGO_31);
        assertThat(escrituras.get(1)).isNotSameAs(legacy);
        assertThat(escrituras.get(1).getVigenteDesde()).isEqualTo(SEP_1);
    }

    // ---------- helpers ----------

    private HorarioOperacion versionar(LocalDate efectivoDesde, LocalTime apertura, LocalTime cierre) {
        return writer.ejecutar(new VersionarHorarioOperacion.VersionarHorario(
                SALON_ID, LUNES, efectivoDesde, apertura, cierre));
    }

    private void versionesExistentes(HorarioOperacion... versiones) {
        when(horarioRepository.bloquearVersionesQueIntersectan(
                eq(SALON_ID), anyShort(), any(), nullable(LocalDate.class)))
                .thenReturn(List.of(versiones));
    }

    private HorarioOperacion version(
            LocalDate desde, LocalDate hasta, LocalTime apertura, LocalTime cierre) {
        HorarioOperacion horario = new HorarioOperacion();
        horario.setId(UUID.randomUUID());
        horario.setSalon(salon);
        horario.setDiaSemana(LUNES);
        horario.setHoraApertura(apertura);
        horario.setHoraCierre(cierre);
        horario.setVigenteDesde(desde);
        horario.setVigenteHasta(hasta);
        return horario;
    }
}
