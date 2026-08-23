package com.feelingpilates.ubicaciones.servicio;

import com.feelingpilates.ubicaciones.entidad.HorarioOperacion;
import com.feelingpilates.ubicaciones.repositorio.HorarioOperacionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class HorarioOperacionResolverTest {

    private static final UUID SALON_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    // 2026-08-23 es domingo; 2026-08-24 es lunes.
    private static final LocalDate DOMINGO = LocalDate.of(2026, 8, 23);
    private static final LocalDate LUNES = LocalDate.of(2026, 8, 24);

    private HorarioOperacionRepository horarioOperacionRepository;
    private HorarioOperacionResolver resolver;

    @BeforeEach
    void setUp() {
        horarioOperacionRepository = mock(HorarioOperacionRepository.class);
        resolver = new HorarioOperacionResolver(horarioOperacionRepository);
    }

    @Test
    void repositoryVacioDevuelveOptionalEmpty() {
        when(horarioOperacionRepository.findVigente(SALON_ID, (short) 1, LUNES)).thenReturn(List.of());

        assertThat(resolver.resolver(SALON_ID, LUNES)).isEmpty();
    }

    @Test
    void repositoryConUnaFilaDevuelveEsaInstancia() {
        HorarioOperacion horario = new HorarioOperacion();
        when(horarioOperacionRepository.findVigente(SALON_ID, (short) 1, LUNES)).thenReturn(List.of(horario));

        assertThat(resolver.resolver(SALON_ID, LUNES)).contains(horario);
    }

    @Test
    void repositoryConDosFilasFallaRuidosamente() {
        HorarioOperacion a = new HorarioOperacion();
        HorarioOperacion b = new HorarioOperacion();
        when(horarioOperacionRepository.findVigente(SALON_ID, (short) 1, LUNES)).thenReturn(List.of(a, b));

        assertThatThrownBy(() -> resolver.resolver(SALON_ID, LUNES))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void fechaDomingoConsultaElRepositoryConDiaSemanaCero() {
        when(horarioOperacionRepository.findVigente(SALON_ID, (short) 0, DOMINGO)).thenReturn(List.of());

        resolver.resolver(SALON_ID, DOMINGO);

        verify(horarioOperacionRepository).findVigente(SALON_ID, (short) 0, DOMINGO);
    }

    @Test
    void fechaLunesConsultaElRepositoryConDiaSemanaUno() {
        when(horarioOperacionRepository.findVigente(SALON_ID, (short) 1, LUNES)).thenReturn(List.of());

        resolver.resolver(SALON_ID, LUNES);

        verify(horarioOperacionRepository).findVigente(SALON_ID, (short) 1, LUNES);
    }
}
