package com.feelingpilates.ubicaciones.dominio;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RangoVigenciaTest {

    private static final LocalDate D1 = LocalDate.of(2026, 1, 1);
    private static final LocalDate D2 = LocalDate.of(2026, 6, 15);
    private static final LocalDate D3 = LocalDate.of(2026, 12, 31);

    // --- contiene ---

    @Test
    void nullNullContieneCualquierFecha() {
        RangoVigencia rango = new RangoVigencia(null, null);

        assertThat(rango.contiene(LocalDate.of(1900, 1, 1))).isTrue();
        assertThat(rango.contiene(D2)).isTrue();
        assertThat(rango.contiene(LocalDate.of(3000, 1, 1))).isTrue();
    }

    @Test
    void boundedContieneDesdeExacto() {
        RangoVigencia rango = new RangoVigencia(D1, D3);

        assertThat(rango.contiene(D1)).isTrue();
    }

    @Test
    void boundedContieneHastaExacto() {
        RangoVigencia rango = new RangoVigencia(D1, D3);

        assertThat(rango.contiene(D3)).isTrue();
    }

    @Test
    void fechaAntesDeDesdeNoEstaContenida() {
        RangoVigencia rango = new RangoVigencia(D1, D3);

        assertThat(rango.contiene(D1.minusDays(1))).isFalse();
    }

    @Test
    void fechaDespuesDeHastaNoEstaContenida() {
        RangoVigencia rango = new RangoVigencia(D1, D3);

        assertThat(rango.contiene(D3.plusDays(1))).isFalse();
    }

    @Test
    void nullDesdeFechaHastaContieneCualquierFechaHastaElLimite() {
        RangoVigencia rango = new RangoVigencia(null, D2);

        assertThat(rango.contiene(LocalDate.of(1900, 1, 1))).isTrue();
        assertThat(rango.contiene(D2)).isTrue();
        assertThat(rango.contiene(D2.plusDays(1))).isFalse();
    }

    @Test
    void fechaDesdeNullHastaContieneCualquierFechaDesdeElLimite() {
        RangoVigencia rango = new RangoVigencia(D1, null);

        assertThat(rango.contiene(D1.minusDays(1))).isFalse();
        assertThat(rango.contiene(D1)).isTrue();
        assertThat(rango.contiene(LocalDate.of(3000, 1, 1))).isTrue();
    }

    @Test
    void contieneRechazaFechaNula() {
        RangoVigencia rango = new RangoVigencia(null, null);

        assertThatThrownBy(() -> rango.contiene(null)).isInstanceOf(IllegalArgumentException.class);
    }

    // --- intersecta ---

    @Test
    void intersectanEnUnaFronteraComun() {
        RangoVigencia a = new RangoVigencia(null, LocalDate.of(2026, 9, 1));
        RangoVigencia b = new RangoVigencia(LocalDate.of(2026, 9, 1), null);

        assertThat(a.intersecta(b)).isTrue();
        assertThat(b.intersecta(a)).isTrue();
    }

    @Test
    void rangosSeparadosPorUnDiaNoIntersectan() {
        RangoVigencia a = new RangoVigencia(null, LocalDate.of(2026, 8, 31));
        RangoVigencia b = new RangoVigencia(LocalDate.of(2026, 9, 1), null);

        assertThat(a.intersecta(b)).isFalse();
        assertThat(b.intersecta(a)).isFalse();
    }

    @Test
    void rangosSolapadosIntersectanYNoSonSoloContiguos() {
        RangoVigencia a = new RangoVigencia(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 9, 10));
        RangoVigencia b = new RangoVigencia(LocalDate.of(2026, 9, 1), LocalDate.of(2026, 12, 31));

        assertThat(a.intersecta(b)).isTrue();
        assertThat(a.esContiguoCon(b)).isFalse();
        assertThat(b.esContiguoCon(a)).isFalse();
    }

    // --- esContiguoCon ---

    @Test
    void rangosSeparadosPorUnDiaSonContiguos() {
        RangoVigencia a = new RangoVigencia(null, LocalDate.of(2026, 8, 31));
        RangoVigencia b = new RangoVigencia(LocalDate.of(2026, 9, 1), null);

        assertThat(a.esContiguoCon(b)).isTrue();
        assertThat(b.esContiguoCon(a)).isTrue();
    }

    @Test
    void rangoAbiertoHaciaMasInfinitoNuncaEsContiguoConUnaVersionPosteriorSinOverflow() {
        RangoVigencia abierto = new RangoVigencia(D1, null);
        RangoVigencia otro = new RangoVigencia(D3, null);

        assertThat(abierto.esContiguoCon(otro)).isFalse();
    }

    // --- invariante de construccion ---

    @Test
    void rechazaDesdePosteriorAHasta() {
        assertThatThrownBy(() -> new RangoVigencia(D3, D1)).isInstanceOf(IllegalArgumentException.class);
    }
}
