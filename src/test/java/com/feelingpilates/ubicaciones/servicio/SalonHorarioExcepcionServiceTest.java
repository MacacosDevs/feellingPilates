package com.feelingpilates.ubicaciones.servicio;

import com.feelingpilates.exception.ConflictException;
import com.feelingpilates.exception.ResourceNotFoundException;
import com.feelingpilates.exception.ValidacionException;
import com.feelingpilates.seguridad.AutorizadorSalon;
import com.feelingpilates.ubicaciones.dominio.CambioExcepcionHorario;
import com.feelingpilates.ubicaciones.dominio.ConflictoProgramacionPuntual;
import com.feelingpilates.ubicaciones.dominio.ValidadorImpactoExcepcionHorario;
import com.feelingpilates.ubicaciones.dto.GuardarExcepcionSalonPorFechaRequest;
import com.feelingpilates.ubicaciones.dto.GuardarExcepcionSalonRequest;
import com.feelingpilates.ubicaciones.dto.SalonHorarioExcepcionResponse;
import com.feelingpilates.ubicaciones.entidad.HorarioOperacion;
import com.feelingpilates.ubicaciones.entidad.Salon;
import com.feelingpilates.ubicaciones.entidad.SalonHorarioExcepcion;
import com.feelingpilates.ubicaciones.repositorio.SalonHorarioExcepcionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InOrder;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SalonHorarioExcepcionServiceTest {

    private static final UUID ACTOR_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private static final UUID SALON_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final LocalDate HOY = LocalDate.of(2026, 8, 20);
    private static final LocalDate AYER = HOY.minusDays(1);
    private static final LocalDate MANANA = HOY.plusDays(1);
    private static final LocalTime DIEZ = LocalTime.of(10, 0);
    private static final LocalTime DIECISEIS = LocalTime.of(16, 0);
    private static final Clock RELOJ = Clock.fixed(
            HOY.atStartOfDay(ZoneId.of("UTC")).toInstant(), ZoneId.of("UTC"));

    private SalonHorarioExcepcionRepository excepcionRepository;
    private SalonLock salonLock;
    private AutorizadorSalon autorizadorSalon;
    private HorarioOperacionResolver horarioOperacionResolver;
    private ValidadorImpactoExcepcionHorario validador;
    private Salon salon;
    private SalonHorarioExcepcionService service;

    @BeforeEach
    void preparar() {
        excepcionRepository = mock(SalonHorarioExcepcionRepository.class);
        salonLock = mock(SalonLock.class);
        autorizadorSalon = mock(AutorizadorSalon.class);
        horarioOperacionResolver = mock(HorarioOperacionResolver.class);
        validador = mock(ValidadorImpactoExcepcionHorario.class);

        salon = new Salon();
        salon.setId(SALON_ID);

        when(salonLock.adquirir(SALON_ID)).thenReturn(salon);
        when(validador.evaluar(any())).thenReturn(List.of());
        when(excepcionRepository.findBySalonIdAndFechaAndActivoTrue(any(), any())).thenReturn(Optional.empty());
        when(excepcionRepository.saveAndFlush(any(SalonHorarioExcepcion.class))).thenAnswer(i -> {
            SalonHorarioExcepcion e = i.getArgument(0);
            if (e.getId() == null) {
                e.setId(UUID.randomUUID());
            }
            return e;
        });
        when(horarioOperacionResolver.resolver(any(), any())).thenReturn(Optional.empty());

        service = new SalonHorarioExcepcionService(
                excepcionRepository, salonLock, autorizadorSalon, horarioOperacionResolver,
                List.of(validador), RELOJ);
    }

    // ---------- W1: temporalidad de alta ----------

    @ParameterizedTest
    @CsvSource({"AYER,true", "HOY,false", "MANANA,false"})
    void temporalidadDeAlta(String etiquetaFecha, boolean debeRechazar) {
        LocalDate fecha = fecha(etiquetaFecha);

        if (debeRechazar) {
            assertThatThrownBy(() -> service.guardar(ACTOR_ID, SALON_ID, cerrado(fecha)))
                    .isInstanceOf(ValidacionException.class)
                    .hasMessageContaining("EXCEPCION_HORARIO_EN_EL_PASADO");
            verify(excepcionRepository, never()).saveAndFlush(any());
        } else {
            SalonHorarioExcepcionResponse resultado = service.guardar(ACTOR_ID, SALON_ID, cerrado(fecha));
            assertThat(resultado.fecha()).isEqualTo(fecha);
        }
    }

    // ---------- W2: sin fila previa -> INSERT activa ----------

    @Test
    void sinFilaPreviaInsertaFilaActiva() {
        SalonHorarioExcepcionResponse resultado = service.guardar(ACTOR_ID, SALON_ID, especial(MANANA, DIEZ, DIECISEIS));

        assertThat(resultado.cerrado()).isFalse();
        assertThat(resultado.horaApertura()).isEqualTo(DIEZ);
        assertThat(resultado.horaCierre()).isEqualTo(DIECISEIS);
        verify(excepcionRepository).saveAndFlush(any(SalonHorarioExcepcion.class));
    }

    @Test
    void guardarPorFechaConvergeConElMismoUpsertQueElLegacy() {
        SalonHorarioExcepcionResponse resultado = service.guardarPorFecha(
                ACTOR_ID, SALON_ID, MANANA, new GuardarExcepcionSalonPorFechaRequest(false, DIEZ, DIECISEIS));

        assertThat(resultado.fecha()).isEqualTo(MANANA);
        assertThat(resultado.horaApertura()).isEqualTo(DIEZ);
        assertThat(resultado.horaCierre()).isEqualTo(DIECISEIS);
    }

    // ---------- W3: activa + mismo contenido -> no-op real ----------

    @Test
    void activaConMismoContenidoEsNoOpSinEscritura() {
        SalonHorarioExcepcion activa = excepcionActiva(MANANA, false, DIEZ, DIECISEIS);
        when(excepcionRepository.findBySalonIdAndFechaAndActivoTrue(SALON_ID, MANANA))
                .thenReturn(Optional.of(activa));

        SalonHorarioExcepcionResponse resultado = service.guardar(ACTOR_ID, SALON_ID, especial(MANANA, DIEZ, DIECISEIS));

        assertThat(resultado.id()).isEqualTo(activa.getId());
        verify(excepcionRepository, never()).save(any());
        verify(excepcionRepository, never()).saveAndFlush(any());
    }

    @Test
    void activaCerradaConMismoContenidoCerradoEsNoOp() {
        SalonHorarioExcepcion activa = excepcionActiva(MANANA, true, null, null);
        when(excepcionRepository.findBySalonIdAndFechaAndActivoTrue(SALON_ID, MANANA))
                .thenReturn(Optional.of(activa));

        service.guardar(ACTOR_ID, SALON_ID, cerrado(MANANA));

        verify(excepcionRepository, never()).save(any());
        verify(excepcionRepository, never()).saveAndFlush(any());
    }

    // ---------- W4: activa + contenido distinto -> UPDATE misma fila ----------

    @Test
    void activaConContenidoDistintoActualizaLaMismaFila() {
        SalonHorarioExcepcion activa = excepcionActiva(MANANA, false, DIEZ, DIECISEIS);
        UUID idOriginal = activa.getId();
        when(excepcionRepository.findBySalonIdAndFechaAndActivoTrue(SALON_ID, MANANA))
                .thenReturn(Optional.of(activa));

        SalonHorarioExcepcionResponse resultado = service.guardar(ACTOR_ID, SALON_ID, cerrado(MANANA));

        assertThat(resultado.id()).isEqualTo(idOriginal);
        assertThat(resultado.cerrado()).isTrue();
        verify(excepcionRepository).saveAndFlush(activa);
    }

    // ---------- W5: temporalidad de modificacion y cancelacion ----------

    @ParameterizedTest
    @CsvSource({"AYER,true", "HOY,false", "MANANA,false"})
    void temporalidadDeModificacion(String etiquetaFecha, boolean debeRechazar) {
        LocalDate fecha = fecha(etiquetaFecha);
        SalonHorarioExcepcion activa = excepcionActiva(fecha, false, DIEZ, DIECISEIS);
        when(excepcionRepository.findBySalonIdAndFechaAndActivoTrue(SALON_ID, fecha))
                .thenReturn(Optional.of(activa));

        if (debeRechazar) {
            assertThatThrownBy(() -> service.guardar(ACTOR_ID, SALON_ID, cerrado(fecha)))
                    .isInstanceOf(ValidacionException.class)
                    .hasMessageContaining("EXCEPCION_HORARIO_EN_EL_PASADO");
        } else {
            service.guardar(ACTOR_ID, SALON_ID, cerrado(fecha));
            assertThat(activa.isCerrado()).isTrue();
        }
    }

    @ParameterizedTest
    @CsvSource({"AYER,true", "HOY,false", "MANANA,false"})
    void temporalidadDeCancelacion(String etiquetaFecha, boolean debeRechazar) {
        LocalDate fecha = fecha(etiquetaFecha);
        SalonHorarioExcepcion activa = excepcionActiva(fecha, true, null, null);
        when(excepcionRepository.findById(activa.getId())).thenReturn(Optional.of(activa));

        if (debeRechazar) {
            assertThatThrownBy(() -> service.eliminar(ACTOR_ID, SALON_ID, activa.getId()))
                    .isInstanceOf(ValidacionException.class)
                    .hasMessageContaining("EXCEPCION_HORARIO_EN_EL_PASADO");
            assertThat(activa.isActivo()).isTrue();
        } else {
            service.eliminar(ACTOR_ID, SALON_ID, activa.getId());
            assertThat(activa.isActivo()).isFalse();
        }
    }

    // ---------- W6: cancelar sin fila activa -> 404, no 204 ----------

    @Test
    void cancelarSinFilaActivaLanza404NoExiste() {
        when(excepcionRepository.findById(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.eliminar(ACTOR_ID, SALON_ID, UUID.randomUUID()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("EXCEPCION_HORARIO_NO_EXISTE");
    }

    @Test
    void cancelarPorFechaSinFilaActivaLanza404() {
        when(excepcionRepository.findBySalonIdAndFechaAndActivoTrue(SALON_ID, MANANA))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.eliminarPorFecha(ACTOR_ID, SALON_ID, MANANA))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("EXCEPCION_HORARIO_NO_EXISTE");
    }

    // ---------- W7: solo filas inactivas -> INSERT nueva, nunca reactivacion ----------

    @Test
    void soloFilasInactivasInsertaFilaNuevaSinReactivar() {
        // findBySalonIdAndFechaAndActivoTrue solo ve activas: aunque existan inactivas en BD real,
        // el mock ya representa "ninguna activa" (default del @BeforeEach), asi que el resultado
        // observable es una insercion, nunca una reactivacion de una fila existente.
        SalonHorarioExcepcionResponse resultado = service.guardar(ACTOR_ID, SALON_ID, cerrado(MANANA));

        assertThat(resultado.cerrado()).isTrue();
        verify(excepcionRepository).saveAndFlush(any(SalonHorarioExcepcion.class));
    }

    // ---------- W8: normalizacion y validacion de forma ----------

    @Test
    void cerradoConHorasSeNormalizaANull() {
        SalonHorarioExcepcionResponse resultado = service.guardar(
                ACTOR_ID, SALON_ID, new GuardarExcepcionSalonRequest(MANANA, true, DIEZ, DIECISEIS));

        assertThat(resultado.horaApertura()).isNull();
        assertThat(resultado.horaCierre()).isNull();
    }

    @Test
    void especialSinHorasLanzaHorarioEspecialIncompleto() {
        assertThatThrownBy(() -> service.guardar(
                ACTOR_ID, SALON_ID, new GuardarExcepcionSalonRequest(MANANA, false, null, DIECISEIS)))
                .isInstanceOf(ValidacionException.class)
                .hasMessageContaining("HORARIO_ESPECIAL_INCOMPLETO");
        verify(excepcionRepository, never()).saveAndFlush(any());
    }

    @Test
    void cierreIgualAAperturaLanzaHoraCierreDebeSerPosterior() {
        assertThatThrownBy(() -> service.guardar(
                ACTOR_ID, SALON_ID, new GuardarExcepcionSalonRequest(MANANA, false, DIEZ, DIEZ)))
                .isInstanceOf(ValidacionException.class)
                .hasMessageContaining("HORA_CIERRE_DEBE_SER_POSTERIOR");
    }

    @Test
    void cierreAnteriorAAperturaLanzaHoraCierreDebeSerPosterior() {
        assertThatThrownBy(() -> service.guardar(
                ACTOR_ID, SALON_ID, new GuardarExcepcionSalonRequest(MANANA, false, DIECISEIS, DIEZ)))
                .isInstanceOf(ValidacionException.class)
                .hasMessageContaining("HORA_CIERRE_DEBE_SER_POSTERIOR");
    }

    // ---------- W9: orden del protocolo ----------

    @Test
    void protocoloCompletoEnOrdenAutorizarLockLeer() {
        service.guardar(ACTOR_ID, SALON_ID, especial(MANANA, DIEZ, DIECISEIS));

        InOrder orden = inOrder(autorizadorSalon, salonLock, excepcionRepository);
        orden.verify(autorizadorSalon).verificarAccesoSalon(ACTOR_ID, "salon.administrar", SALON_ID);
        orden.verify(salonLock).adquirir(SALON_ID);
        orden.verify(excepcionRepository).findBySalonIdAndFechaAndActivoTrue(SALON_ID, MANANA);
    }

    // ---------- W10: Clock.fixed, nunca el reloj real ----------

    /**
     * AYER (2026-08-19) ya es pasado tanto para el reloj fijo como para el reloj real del sistema
     * en cualquier fecha de ejecucion posterior a 2026-08-19: si production reemplazara el reloj
     * inyectado por {@code LocalDate.now()}, este caso seguiria rechazando por casualidad y el test
     * no detectaria la mutacion. Se usa en su lugar un reloj deliberadamente MUY alejado de "hoy" y
     * una fecha que solo es pasada relativa a ESE reloj: PASADA para {@code relojLejano} (2035-06-15)
     * pero FUTURA para el reloj real de cualquier maquina hasta bien entrado 2035. Si el writer usara
     * {@code LocalDate.now()} en vez del reloj inyectado, aceptaria la fecha por error y este test
     * fallaria de forma estable durante todo el horizonte razonable del proyecto.
     */
    @Test
    void usaElRelojFijoNoElRelojDelSistema() {
        Clock relojLejano = Clock.fixed(
                LocalDate.of(2035, 6, 15).atStartOfDay(ZoneId.of("UTC")).toInstant(), ZoneId.of("UTC"));
        LocalDate fechaPasadaSoloParaElRelojLejano = LocalDate.of(2035, 6, 14);
        SalonHorarioExcepcionService servicioConRelojLejano = new SalonHorarioExcepcionService(
                excepcionRepository, salonLock, autorizadorSalon, horarioOperacionResolver,
                List.of(validador), relojLejano);

        assertThatThrownBy(() -> servicioConRelojLejano.guardar(
                ACTOR_ID, SALON_ID, cerrado(fechaPasadaSoloParaElRelojLejano)))
                .isInstanceOf(ValidacionException.class)
                .hasMessageContaining("EXCEPCION_HORARIO_EN_EL_PASADO");
    }

    // ---------- Impacto puntual: whitelist 409 ----------

    @Test
    void impactoPuntualIncompatibleLanzaConflictException() {
        when(validador.evaluar(any())).thenReturn(
                List.of(ConflictoProgramacionPuntual.turnoExcepcion(UUID.randomUUID(), "16:00-18:00")));

        assertThatThrownBy(() -> service.guardar(ACTOR_ID, SALON_ID, especial(MANANA, DIEZ, DIECISEIS)))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("PROGRAMACION_PUNTUAL_INCOMPATIBLE_CON_EXCEPCION");
        verify(excepcionRepository, never()).saveAndFlush(any());
    }

    @Test
    void cancelacionValidaImpactoContraElHorarioResultanteNoContraLaExcepcion() {
        SalonHorarioExcepcion activa = excepcionActiva(MANANA, false, LocalTime.of(8, 0), LocalTime.of(20, 0));
        when(excepcionRepository.findById(activa.getId())).thenReturn(Optional.of(activa));
        HorarioOperacion semanal = new HorarioOperacion();
        semanal.setHoraApertura(LocalTime.of(8, 0));
        semanal.setHoraCierre(LocalTime.of(14, 0));
        when(horarioOperacionResolver.resolver(SALON_ID, MANANA)).thenReturn(Optional.of(semanal));
        // Una reserva de 16:00-17:00 cabria en la excepcion (08-20) pero NO en el semanal (08-14).
        when(validador.evaluar(any())).thenAnswer(inv -> {
            CambioExcepcionHorario cambio = inv.getArgument(0);
            return cambio.admite(LocalTime.of(16, 0), LocalTime.of(17, 0))
                    ? List.of()
                    : List.of(ConflictoProgramacionPuntual.reservaConfirmada(UUID.randomUUID(), "16:00-17:00"));
        });

        assertThatThrownBy(() -> service.eliminar(ACTOR_ID, SALON_ID, activa.getId()))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("PROGRAMACION_PUNTUAL_INCOMPATIBLE_CON_EXCEPCION");
        assertThat(activa.isActivo()).isTrue();
    }

    /**
     * Sin ninguna version semanal aplicable (default del {@code @BeforeEach}:
     * {@code horarioOperacionResolver.resolver} devuelve vacio), cancelar dejaria
     * {@code CERRADO}, equivalente a NO_OPERATIVO: ningun intervalo cabe. Un turno EXCEPCION que
     * hoy cabe en la excepcion activa (10-16) quedaria huerfano, asi que la cancelacion se
     * rechaza y la fila sigue activa.
     */
    @Test
    void cancelacionSinHorarioSemanalQueDejaNoOperativoEsRechazadaSiHayPuntualDependiente() {
        SalonHorarioExcepcion activa = excepcionActiva(MANANA, false, DIEZ, DIECISEIS);
        when(excepcionRepository.findById(activa.getId())).thenReturn(Optional.of(activa));
        when(validador.evaluar(any())).thenAnswer(inv -> {
            CambioExcepcionHorario cambio = inv.getArgument(0);
            return cambio.admite(LocalTime.of(11, 0), LocalTime.of(12, 0))
                    ? List.of()
                    : List.of(ConflictoProgramacionPuntual.turnoExcepcion(UUID.randomUUID(), "11:00-12:00"));
        });

        assertThatThrownBy(() -> service.eliminar(ACTOR_ID, SALON_ID, activa.getId()))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("PROGRAMACION_PUNTUAL_INCOMPATIBLE_CON_EXCEPCION");
        assertThat(activa.isActivo()).isTrue();
        verify(excepcionRepository, never()).save(any());
    }

    /**
     * El semanal resultante (08-18) es MAS AMPLIO que la excepcion que se cancela (10-16): toda
     * programacion puntual que cabia en la excepcion sigue cabiendo despues. La cancelacion se
     * permite y persiste como soft delete.
     */
    @Test
    void cancelacionHaciaUnHorarioSemanalMasAmplioEsPermitida() {
        SalonHorarioExcepcion activa = excepcionActiva(MANANA, false, LocalTime.of(10, 0), LocalTime.of(16, 0));
        when(excepcionRepository.findById(activa.getId())).thenReturn(Optional.of(activa));
        HorarioOperacion semanal = new HorarioOperacion();
        semanal.setHoraApertura(LocalTime.of(8, 0));
        semanal.setHoraCierre(LocalTime.of(18, 0));
        when(horarioOperacionResolver.resolver(SALON_ID, MANANA)).thenReturn(Optional.of(semanal));
        when(validador.evaluar(any())).thenAnswer(inv -> {
            CambioExcepcionHorario cambio = inv.getArgument(0);
            return cambio.admite(LocalTime.of(11, 0), LocalTime.of(12, 0))
                    ? List.of()
                    : List.of(ConflictoProgramacionPuntual.reservaConfirmada(UUID.randomUUID(), "11:00-12:00"));
        });

        service.eliminar(ACTOR_ID, SALON_ID, activa.getId());

        assertThat(activa.isActivo()).isFalse();
        verify(excepcionRepository).save(activa);
    }

    // ---------- CambioExcepcionHorario.admite: fin > inicio, contencion completa ----------

    @ParameterizedTest
    @CsvSource({
            "08:00,16:00,true",
            "10:00,12:00,true",
            "07:00,09:00,false",
            "15:00,17:00,false",
            "08:00,08:00,false",
            "10:00,09:00,false",
    })
    void horarioEspecialAdmiteSoloContencionCompletaConFinPosteriorAInicio(
            String inicio, String fin, boolean debeAdmitir) {
        CambioExcepcionHorario especial = CambioExcepcionHorario.horarioEspecial(
                SALON_ID, MANANA, LocalTime.of(8, 0), LocalTime.of(16, 0));

        assertThat(especial.admite(LocalTime.parse(inicio), LocalTime.parse(fin))).isEqualTo(debeAdmitir);
    }

    @ParameterizedTest
    @CsvSource({
            "08:00,16:00",
            "10:00,12:00",
            "07:00,09:00",
            "15:00,17:00",
            "08:00,08:00",
            "10:00,09:00",
    })
    void cerradoNuncaAdmiteNingunIntervalo(String inicio, String fin) {
        CambioExcepcionHorario cerrado = CambioExcepcionHorario.cerrado(SALON_ID, MANANA);

        assertThat(cerrado.admite(LocalTime.parse(inicio), LocalTime.parse(fin))).isFalse();
    }

    // ---------- helpers ----------

    private LocalDate fecha(String etiqueta) {
        return switch (etiqueta) {
            case "AYER" -> AYER;
            case "HOY" -> HOY;
            case "MANANA" -> MANANA;
            default -> throw new IllegalArgumentException(etiqueta);
        };
    }

    private GuardarExcepcionSalonRequest cerrado(LocalDate fecha) {
        return new GuardarExcepcionSalonRequest(fecha, true, null, null);
    }

    private GuardarExcepcionSalonRequest especial(LocalDate fecha, LocalTime apertura, LocalTime cierre) {
        return new GuardarExcepcionSalonRequest(fecha, false, apertura, cierre);
    }

    private SalonHorarioExcepcion excepcionActiva(
            LocalDate fecha, boolean cerrado, LocalTime apertura, LocalTime cierre) {
        SalonHorarioExcepcion e = new SalonHorarioExcepcion();
        e.setId(UUID.randomUUID());
        e.setSalon(salon);
        e.setFecha(fecha);
        e.setCerrado(cerrado);
        e.setHoraApertura(apertura);
        e.setHoraCierre(cierre);
        e.setActivo(true);
        return e;
    }
}
