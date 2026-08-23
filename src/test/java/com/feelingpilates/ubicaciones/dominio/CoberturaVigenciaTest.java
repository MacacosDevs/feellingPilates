package com.feelingpilates.ubicaciones.dominio;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CoberturaVigenciaTest {

    private static final LocalDate ENE_1 = LocalDate.of(2026, 1, 1);
    private static final LocalDate AGO_31 = LocalDate.of(2026, 8, 31);
    private static final LocalDate SEP_1 = LocalDate.of(2026, 9, 1);
    private static final LocalDate SEP_15 = LocalDate.of(2026, 9, 15);
    private static final LocalDate DIC_31 = LocalDate.of(2026, 12, 31);

    private static RangoVigencia rango(LocalDate desde, LocalDate hasta) {
        return new RangoVigencia(desde, hasta);
    }

    @Test
    void unaVersionQueContieneElObjetivoFinitoLoCubre() {
        assertThat(CoberturaVigencia.cubreCompletamente(
                rango(ENE_1, DIC_31),
                List.of(rango(LocalDate.of(2025, 1, 1), LocalDate.of(2027, 1, 1))))).isTrue();
    }

    @Test
    void dosVersionesContiguasCubrenElObjetivo() {
        assertThat(CoberturaVigencia.cubreCompletamente(
                rango(ENE_1, DIC_31),
                List.of(rango(null, AGO_31), rango(SEP_1, null)))).isTrue();
    }

    @Test
    void dosVersionesContiguasCubrenAunqueLleguenDesordenadas() {
        assertThat(CoberturaVigencia.cubreCompletamente(
                rango(ENE_1, DIC_31),
                List.of(rango(SEP_1, null), rango(null, AGO_31)))).isTrue();
    }

    @Test
    void unGapDeUnDiaRompeLaCobertura() {
        CoberturaVigencia.Resultado resultado = CoberturaVigencia.evaluar(
                rango(ENE_1, DIC_31),
                List.of(rango(null, AGO_31), rango(SEP_1.plusDays(1), null)));

        assertThat(resultado.completa()).isFalse();
        assertThat(resultado.tieneGap()).isTrue();
        assertThat(resultado.gapDesde()).isEqualTo(SEP_1);
        assertThat(resultado.gapHasta()).isEqualTo(SEP_1);
    }

    @Test
    void unGapDeVariasSemanasRompeLaCobertura() {
        CoberturaVigencia.Resultado resultado = CoberturaVigencia.evaluar(
                rango(ENE_1, DIC_31),
                List.of(rango(null, AGO_31), rango(SEP_15, null)));

        assertThat(resultado.completa()).isFalse();
        assertThat(resultado.gapDesde()).isEqualTo(SEP_1);
        assertThat(resultado.gapHasta()).isEqualTo(SEP_15.minusDays(1));
    }

    @Test
    void objetivoAbiertoSeCubreSoloSiLaUltimaVersionLlegaAInfinito() {
        assertThat(CoberturaVigencia.cubreCompletamente(
                rango(ENE_1, null),
                List.of(rango(null, AGO_31), rango(SEP_1, null)))).isTrue();
    }

    @Test
    void objetivoAbiertoNoSeCubreSiLaUltimaVersionTerminaEnFechaFinita() {
        CoberturaVigencia.Resultado resultado = CoberturaVigencia.evaluar(
                rango(ENE_1, null),
                List.of(rango(null, AGO_31), rango(SEP_1, DIC_31)));

        assertThat(resultado.completa()).isFalse();
        assertThat(resultado.gapDesde()).isEqualTo(DIC_31.plusDays(1));
        assertThat(resultado.gapHasta()).isNull();
    }

    @Test
    void objetivoAbiertoNoSeCubreConUnaUnicaVersionFinita() {
        assertThat(CoberturaVigencia.cubreCompletamente(
                rango(ENE_1, null),
                List.of(rango(null, DIC_31)))).isFalse();
    }

    @Test
    void versionNullNullCubreCualquierObjetivo() {
        assertThat(CoberturaVigencia.cubreCompletamente(
                rango(ENE_1, DIC_31), List.of(rango(null, null)))).isTrue();
        assertThat(CoberturaVigencia.cubreCompletamente(
                rango(ENE_1, null), List.of(rango(null, null)))).isTrue();
        assertThat(CoberturaVigencia.cubreCompletamente(
                rango(null, null), List.of(rango(null, null)))).isTrue();
    }

    @Test
    void objetivoAbiertoHaciaMenosInfinitoExigeUnaVersionAbiertaHaciaMenosInfinito() {
        assertThat(CoberturaVigencia.cubreCompletamente(
                rango(null, DIC_31), List.of(rango(ENE_1, null)))).isFalse();
        assertThat(CoberturaVigencia.cubreCompletamente(
                rango(null, DIC_31), List.of(rango(null, null)))).isTrue();
    }

    @Test
    void sinVersionesNoHayCobertura() {
        CoberturaVigencia.Resultado resultado = CoberturaVigencia.evaluar(rango(ENE_1, DIC_31), List.of());

        assertThat(resultado.completa()).isFalse();
        assertThat(resultado.gapDesde()).isEqualTo(ENE_1);
        assertThat(resultado.gapHasta()).isEqualTo(DIC_31);
    }

    @Test
    void versionesQueNoTocanElObjetivoNoAportanCobertura() {
        assertThat(CoberturaVigencia.cubreCompletamente(
                rango(SEP_1, DIC_31), List.of(rango(ENE_1, AGO_31)))).isFalse();
    }

    @Test
    void fronteraExactaLaVersionQueEmpiezaElMismoDiaQueElObjetivoLoCubre() {
        assertThat(CoberturaVigencia.cubreCompletamente(
                rango(ENE_1, DIC_31), List.of(rango(ENE_1, DIC_31)))).isTrue();
    }

    @Test
    void fronteraExactaUnDiaTardeDejaElPrimerDiaSinCubrir() {
        CoberturaVigencia.Resultado resultado = CoberturaVigencia.evaluar(
                rango(ENE_1, DIC_31), List.of(rango(ENE_1.plusDays(1), DIC_31)));

        assertThat(resultado.completa()).isFalse();
        assertThat(resultado.gapDesde()).isEqualTo(ENE_1);
        assertThat(resultado.gapHasta()).isEqualTo(ENE_1);
    }

    @Test
    void fronteraExactaUnDiaCortoDejaElUltimoDiaSinCubrir() {
        CoberturaVigencia.Resultado resultado = CoberturaVigencia.evaluar(
                rango(ENE_1, DIC_31), List.of(rango(ENE_1, DIC_31.minusDays(1))));

        assertThat(resultado.completa()).isFalse();
        assertThat(resultado.gapDesde()).isEqualTo(DIC_31);
        assertThat(resultado.gapHasta()).isEqualTo(DIC_31);
    }

    @Test
    void versionesSolapadasCubrenIgualQueLasContiguas() {
        assertThat(CoberturaVigencia.cubreCompletamente(
                rango(ENE_1, DIC_31),
                List.of(rango(null, SEP_15), rango(AGO_31, null)))).isTrue();
    }

    @Test
    void unaVersionQueEngloboAOtraNoRetrocedeLaFrontera() {
        assertThat(CoberturaVigencia.cubreCompletamente(
                rango(ENE_1, DIC_31),
                List.of(rango(null, null), rango(SEP_1, SEP_15)))).isTrue();
    }

    @Test
    void noDesbordaConLosLimitesExtremosDeLocalDate() {
        assertThat(CoberturaVigencia.cubreCompletamente(
                rango(ENE_1, LocalDate.MAX), List.of(rango(LocalDate.MIN, LocalDate.MAX)))).isTrue();
        assertThat(CoberturaVigencia.cubreCompletamente(
                rango(null, DIC_31), List.of(rango(LocalDate.MIN, DIC_31)))).isFalse();
        assertThat(CoberturaVigencia.cubreCompletamente(
                rango(ENE_1, null), List.of(rango(ENE_1, LocalDate.MAX)))).isFalse();
    }
}
