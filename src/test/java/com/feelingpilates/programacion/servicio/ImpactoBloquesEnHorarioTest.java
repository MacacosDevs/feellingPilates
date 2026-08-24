package com.feelingpilates.programacion.servicio;

import com.feelingpilates.programacion.entidad.BloqueProgramacion;
import com.feelingpilates.programacion.repositorio.BloqueProgramacionRepository;
import com.feelingpilates.ubicaciones.dominio.CambioHorarioOperacion;
import com.feelingpilates.ubicaciones.dominio.ConflictoProgramacion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyShort;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ImpactoBloquesEnHorarioTest {

    private static final UUID SALON_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final short LUNES = 1;
    private static final LocalDate ENERO = LocalDate.of(2026, 1, 1);
    private static final LocalDate SEP_1 = LocalDate.of(2026, 9, 1);

    private BloqueProgramacionRepository bloqueRepository;
    private ImpactoBloquesEnHorario adapter;

    @BeforeEach
    void preparar() {
        bloqueRepository = mock(BloqueProgramacionRepository.class);
        adapter = new ImpactoBloquesEnHorario(bloqueRepository);
    }

    @Test
    void bloqueQueCabeEnElHorarioNuevoNoEsConflicto() {
        bloquesVigentes(bloque(9, 12, ENERO, null));

        assertThat(adapter.evaluar(abierto(9, 20))).isEmpty();
    }

    @Test
    void bloqueQueNoCabeEnElHorarioNuevoEsConflicto() {
        BloqueProgramacion bloque = bloque(8, 9, ENERO, null);
        bloquesVigentes(bloque);

        List<ConflictoProgramacion> conflictos = adapter.evaluar(abierto(9, 20));

        assertThat(conflictos).singleElement().satisfies(c -> {
            assertThat(c.origen()).isEqualTo(ConflictoProgramacion.Origen.BLOQUE_PROGRAMACION);
            assertThat(c.id()).isEqualTo(bloque.getId());
            assertThat(c.detalle()).isEqualTo("08:00-09:00");
        });
    }

    /**
     * Un bloque que empieza antes de D y sigue despues si entra, pero solo se le exige encajar en
     * el horario resultante, que rige desde D. Nunca se re-evalua el tramo anterior.
     */
    @Test
    void soloSeConsultaLaPorcionDelBloqueDesdeLaFechaEfectiva() {
        bloquesVigentes();
        ArgumentCaptor<LocalDate> desde = ArgumentCaptor.forClass(LocalDate.class);

        adapter.evaluar(abierto(9, 20));

        verify(bloqueRepository).buscarActivosVigentesDesde(
                org.mockito.ArgumentMatchers.eq(SALON_ID),
                org.mockito.ArgumentMatchers.eq(LUNES),
                desde.capture());
        assertThat(desde.getValue()).isEqualTo(SEP_1);
    }

    @Test
    void alCerrarElDiaCualquierBloqueVigenteDesdeDEsConflicto() {
        bloquesVigentes(bloque(9, 12, ENERO, null), bloque(14, 16, SEP_1, null));

        List<ConflictoProgramacion> conflictos = adapter.evaluar(
                CambioHorarioOperacion.cerrado(SALON_ID, LUNES, SEP_1));

        assertThat(conflictos).hasSize(2);
    }

    @Test
    void sinBloquesNoHayConflicto() {
        bloquesVigentes();

        assertThat(adapter.evaluar(abierto(9, 20))).isEmpty();
        assertThat(adapter.evaluar(CambioHorarioOperacion.cerrado(SALON_ID, LUNES, SEP_1))).isEmpty();
    }

    private void bloquesVigentes(BloqueProgramacion... bloques) {
        when(bloqueRepository.buscarActivosVigentesDesde(any(), anyShort(), any()))
                .thenReturn(List.of(bloques));
    }

    private CambioHorarioOperacion abierto(int apertura, int cierre) {
        return CambioHorarioOperacion.abierto(
                SALON_ID, LUNES, SEP_1, LocalTime.of(apertura, 0), LocalTime.of(cierre, 0));
    }

    private BloqueProgramacion bloque(int inicio, int fin, LocalDate desde, LocalDate hasta) {
        BloqueProgramacion bloque = new BloqueProgramacion();
        bloque.setId(UUID.randomUUID());
        bloque.setSerieId(UUID.randomUUID());
        bloque.setSalonId(SALON_ID);
        bloque.setDiaSemana(LUNES);
        bloque.setHoraInicio(LocalTime.of(inicio, 0));
        bloque.setHoraFin(LocalTime.of(fin, 0));
        bloque.setVigenteDesde(desde);
        bloque.setVigenteHasta(hasta);
        bloque.setActivo(true);
        return bloque;
    }
}
