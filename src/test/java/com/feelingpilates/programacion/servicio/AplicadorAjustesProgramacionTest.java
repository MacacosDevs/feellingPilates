package com.feelingpilates.programacion.servicio;

import com.feelingpilates.programacion.dominio.OcurrenciaEfectiva;
import com.feelingpilates.programacion.dominio.OcurrenciaNominal;
import com.feelingpilates.programacion.dominio.ProgramacionInvarianteException;
import com.feelingpilates.programacion.entidad.AjusteProgramacionFecha;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AplicadorAjustesProgramacionTest {

    private static final LocalDate FECHA = LocalDate.of(2027, 1, 4);
    private static final UUID SERIE = UUID.randomUUID();
    private static final UUID SALON_A = UUID.randomUUID();
    private static final UUID SALON_B = UUID.randomUUID();
    private static final UUID INSTRUCTOR_A = UUID.randomUUID();
    private static final UUID INSTRUCTOR_B = UUID.randomUUID();
    private static final UUID ACTIVIDAD = UUID.randomUUID();

    private final AplicadorAjustesProgramacion aplicador = new AplicadorAjustesProgramacion();

    @Test
    void recurrenteSinAjustePermanece() {
        assertThat(aplicador.aplicar(List.of(nominal()), List.of()))
                .singleElement()
                .extracting(OcurrenciaEfectiva::origen)
                .isEqualTo(OcurrenciaEfectiva.Origen.RECURRENTE);
    }

    @Test
    void cancelacionSuprimeSoloLaNominalTarget() {
        AjusteProgramacionFecha ajuste = AjusteProgramacionFecha.nuevoTarget(
                UUID.randomUUID(), AjusteProgramacionFecha.Tipo.CANCELACION,
                SERIE, FECHA, null, null, null, null, null);

        assertThat(aplicador.aplicar(List.of(nominal()), List.of(ajuste))).isEmpty();
    }

    @Test
    void reemplazoConservaReferenciaDeSerieYCambiaDestino() {
        AjusteProgramacionFecha ajuste = AjusteProgramacionFecha.nuevoTarget(
                UUID.randomUUID(), AjusteProgramacionFecha.Tipo.REEMPLAZO,
                SERIE, FECHA, SALON_B, INSTRUCTOR_B, ACTIVIDAD,
                LocalTime.of(12, 0), LocalTime.of(13, 0));

        assertThat(aplicador.aplicar(List.of(nominal()), List.of(ajuste)))
                .singleElement()
                .satisfies(o -> {
                    assertThat(o.origen()).isEqualTo(OcurrenciaEfectiva.Origen.REEMPLAZO);
                    assertThat(o.salonId()).isEqualTo(SALON_B);
                    assertThat(o.instructorId()).isEqualTo(INSTRUCTOR_B);
                    assertThat(o.referencia().id()).isEqualTo(SERIE);
                });
    }

    @Test
    void adicionSeIncorporaSinTemplate() {
        UUID ajusteId = UUID.randomUUID();
        AjusteProgramacionFecha adicion = AjusteProgramacionFecha.nuevaAdicion(
                ajusteId, FECHA, SALON_B, INSTRUCTOR_B, ACTIVIDAD,
                LocalTime.of(12, 0), LocalTime.of(13, 0));

        assertThat(aplicador.aplicar(List.of(), List.of(adicion)))
                .singleElement()
                .satisfies(o -> {
                    assertThat(o.origen()).isEqualTo(OcurrenciaEfectiva.Origen.ADICION);
                    assertThat(o.referencia().id()).isEqualTo(ajusteId);
                });
    }

    @Test
    void targetSinNominalFallaSinResultadoParcial() {
        AjusteProgramacionFecha ajuste = AjusteProgramacionFecha.nuevoTarget(
                UUID.randomUUID(), AjusteProgramacionFecha.Tipo.CANCELACION,
                SERIE, FECHA, null, null, null, null, null);

        assertThatThrownBy(() -> aplicador.aplicar(List.of(), List.of(ajuste)))
                .isInstanceOf(ProgramacionInvarianteException.class)
                .hasMessageContaining(ProgramacionErrores.ASIGNACION_OBJETIVO_NO_EXISTE);
    }

    @Test
    void dosNominalesDeMismaSerieFallaAntesDeAplicar() {
        OcurrenciaNominal segunda = new OcurrenciaNominal(
                FECHA, SERIE, UUID.randomUUID(), UUID.randomUUID(), SALON_B,
                INSTRUCTOR_B, ACTIVIDAD, LocalTime.NOON, LocalTime.of(13, 0));

        assertThatThrownBy(() -> aplicador.aplicar(List.of(nominal(), segunda), List.of()))
                .isInstanceOf(ProgramacionInvarianteException.class)
                .hasMessageContaining("Más de una versión nominal");
    }

    private OcurrenciaNominal nominal() {
        return new OcurrenciaNominal(
                FECHA, SERIE, UUID.randomUUID(), UUID.randomUUID(), SALON_A,
                INSTRUCTOR_A, ACTIVIDAD, LocalTime.of(10, 0), LocalTime.of(11, 0));
    }
}
