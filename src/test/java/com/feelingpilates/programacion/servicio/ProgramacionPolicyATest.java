package com.feelingpilates.programacion.servicio;

import com.feelingpilates.exception.ValidacionException;
import com.feelingpilates.programacion.dominio.OcurrenciaNominal;
import com.feelingpilates.programacion.entidad.AjusteProgramacionFecha;
import com.feelingpilates.programacion.entidad.BloqueProgramacion;
import com.feelingpilates.programacion.repositorio.AjusteProgramacionFechaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ProgramacionPolicyATest {

    private static final UUID SERIE = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID BLOQUE = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID SALON = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final UUID INSTRUCTOR = UUID.fromString("44444444-4444-4444-4444-444444444444");
    private static final UUID ACTIVIDAD = UUID.fromString("55555555-5555-5555-5555-555555555555");
    private static final LocalDate FECHA_TARGET = LocalDate.of(2027, 1, 6);

    private AjusteProgramacionFechaRepository ajustes;
    private ProgramacionNominal nominal;
    private ProgramacionPolicyA policy;
    private BloqueProgramacion bloque;
    private BloqueProgramacionService.CrearAsignacion comando;

    @BeforeEach
    void preparar() {
        ajustes = mock(AjusteProgramacionFechaRepository.class);
        nominal = mock(ProgramacionNominal.class);
        Clock reloj = Clock.fixed(Instant.parse("2027-01-01T00:00:00Z"), ZoneOffset.UTC);
        policy = new ProgramacionPolicyA(ajustes, nominal, reloj);
        bloque = new BloqueProgramacion();
        bloque.setDiaSemana((short) 3);
        bloque.setVigenteDesde(LocalDate.of(2027, 1, 1));
        bloque.setVigenteHasta(LocalDate.of(2027, 12, 31));
        comando = new BloqueProgramacionService.CrearAsignacion(
                SERIE, BLOQUE, INSTRUCTOR, ACTIVIDAD,
                LocalTime.of(9, 0), LocalTime.of(10, 0),
                LocalDate.of(2027, 1, 1), LocalDate.of(2027, 12, 31));
        when(ajustes.buscarTargetsActivosDesde(SERIE, LocalDate.of(2027, 1, 1)))
                .thenReturn(List.of(targetActivo()));
    }

    @Test
    void permiteCuandoLaCreacionDejaExactamenteUnNominalEnElTarget() {
        when(nominal.porSerieYFecha(SERIE, FECHA_TARGET)).thenReturn(List.of());

        assertThatCode(() -> policy.validarNuevaAsignacion(comando, bloque))
                .doesNotThrowAnyException();
    }

    @Test
    void rechazaCuandoLaCreacionDejaMasDeUnNominalEnElTarget() {
        when(nominal.porSerieYFecha(SERIE, FECHA_TARGET))
                .thenReturn(List.of(nominal(), nominal()));

        assertThatThrownBy(() -> policy.validarNuevaAsignacion(comando, bloque))
                .isInstanceOf(ValidacionException.class)
                .hasMessageContaining("cardinalidad 3");
    }

    @Test
    void rechazaCuandoUnaCreacionNoAplicableDejaCeroNominales() {
        bloque.setDiaSemana((short) 4);
        when(nominal.porSerieYFecha(SERIE, FECHA_TARGET)).thenReturn(List.of());

        assertThatThrownBy(() -> policy.validarNuevaAsignacion(comando, bloque))
                .isInstanceOf(ValidacionException.class)
                .hasMessageContaining("cardinalidad 0");
    }

    private AjusteProgramacionFecha targetActivo() {
        return AjusteProgramacionFecha.nuevoTarget(
                UUID.fromString("66666666-6666-6666-6666-666666666666"),
                AjusteProgramacionFecha.Tipo.CANCELACION,
                SERIE,
                FECHA_TARGET,
                null, null, null, null, null);
    }

    private OcurrenciaNominal nominal() {
        return new OcurrenciaNominal(
                FECHA_TARGET, SERIE, UUID.randomUUID(), BLOQUE, SALON, INSTRUCTOR, ACTIVIDAD,
                LocalTime.of(9, 0), LocalTime.of(10, 0));
    }
}
