package com.feelingpilates.ubicaciones.dominio;

import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;

import static org.assertj.core.api.Assertions.assertThat;

class DiaSemanaOperacionTest {

    @Test
    void domingoEsCero() {
        assertThat(DiaSemanaOperacion.desde(DayOfWeek.SUNDAY)).isEqualTo((short) 0);
    }

    @Test
    void lunesEsUno() {
        assertThat(DiaSemanaOperacion.desde(DayOfWeek.MONDAY)).isEqualTo((short) 1);
    }

    @Test
    void martesEsDos() {
        assertThat(DiaSemanaOperacion.desde(DayOfWeek.TUESDAY)).isEqualTo((short) 2);
    }

    @Test
    void miercolesEsTres() {
        assertThat(DiaSemanaOperacion.desde(DayOfWeek.WEDNESDAY)).isEqualTo((short) 3);
    }

    @Test
    void juevesEsCuatro() {
        assertThat(DiaSemanaOperacion.desde(DayOfWeek.THURSDAY)).isEqualTo((short) 4);
    }

    @Test
    void viernesEsCinco() {
        assertThat(DiaSemanaOperacion.desde(DayOfWeek.FRIDAY)).isEqualTo((short) 5);
    }

    @Test
    void sabadoEsSeis() {
        assertThat(DiaSemanaOperacion.desde(DayOfWeek.SATURDAY)).isEqualTo((short) 6);
    }
}
