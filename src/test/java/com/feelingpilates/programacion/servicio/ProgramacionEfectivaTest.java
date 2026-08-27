package com.feelingpilates.programacion.servicio;

import com.feelingpilates.programacion.dominio.OcurrenciaEfectiva;
import com.feelingpilates.programacion.dominio.OcurrenciaNominal;
import com.feelingpilates.programacion.entidad.AjusteProgramacionFecha;
import com.feelingpilates.programacion.repositorio.AjusteProgramacionFechaRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ProgramacionEfectivaTest {

    private static final LocalDate FECHA = LocalDate.of(2028, 1, 3);

    @Test
    void resuelveGlobalAntesDeFiltrarReemplazoSalienteYEntrante() {
        UUID serie = UUID.randomUUID();
        UUID origen = UUID.randomUUID();
        UUID destino = UUID.randomUUID();
        UUID instructor = UUID.randomUUID();
        UUID actividad = UUID.randomUUID();
        OcurrenciaNominal nominal = new OcurrenciaNominal(
                FECHA, serie, UUID.randomUUID(), UUID.randomUUID(), origen,
                instructor, actividad, LocalTime.of(9, 0), LocalTime.of(10, 0));
        AjusteProgramacionFecha reemplazo = AjusteProgramacionFecha.nuevoTarget(
                UUID.randomUUID(), AjusteProgramacionFecha.Tipo.REEMPLAZO, serie, FECHA,
                destino, instructor, actividad, LocalTime.of(11, 0), LocalTime.NOON);

        ProgramacionNominal nominales = mock(ProgramacionNominal.class);
        AjusteProgramacionFechaRepository ajustes = mock(AjusteProgramacionFechaRepository.class);
        ProgramacionValidador validador = mock(ProgramacionValidador.class);
        when(nominales.todasEnFecha(FECHA)).thenReturn(List.of(nominal));
        when(ajustes.findAllByFechaAndActivoTrueOrderById(FECHA)).thenReturn(List.of(reemplazo));
        List<OcurrenciaEfectiva> compuesto = new AplicadorAjustesProgramacion()
                .aplicar(List.of(nominal), List.of(reemplazo));
        when(validador.filtrarFailClosedYValidar(compuesto)).thenReturn(compuesto);
        ProgramacionEfectiva service = new ProgramacionEfectiva(
                nominales, ajustes, new AplicadorAjustesProgramacion(), validador);

        assertThat(service.porSalonYFecha(origen, FECHA)).isEmpty();
        assertThat(service.porSalonYFecha(destino, FECHA))
                .singleElement()
                .extracting(OcurrenciaEfectiva::origen)
                .isEqualTo(OcurrenciaEfectiva.Origen.REEMPLAZO);
    }

    @Test
    void ordenaDeterministicamenteDespuesDeResolverGlobal() {
        ProgramacionNominal nominales = mock(ProgramacionNominal.class);
        AjusteProgramacionFechaRepository ajustes = mock(AjusteProgramacionFechaRepository.class);
        ProgramacionValidador validador = mock(ProgramacionValidador.class);
        UUID salon = UUID.randomUUID();
        UUID actividad = UUID.randomUUID();
        UUID instructorA = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID instructorB = UUID.fromString("22222222-2222-2222-2222-222222222222");
        List<OcurrenciaEfectiva> desordenadas = List.of(
                efectiva(salon, instructorB, actividad, 12, 13),
                efectiva(salon, instructorA, actividad, 9, 10));
        when(nominales.todasEnFecha(FECHA)).thenReturn(List.of());
        when(ajustes.findAllByFechaAndActivoTrueOrderById(FECHA)).thenReturn(List.of());
        when(validador.filtrarFailClosedYValidar(List.of())).thenReturn(desordenadas);
        ProgramacionEfectiva service = new ProgramacionEfectiva(
                nominales, ajustes, new AplicadorAjustesProgramacion(), validador);

        assertThat(service.porSalonYFecha(salon, FECHA))
                .extracting(OcurrenciaEfectiva::horaInicio)
                .containsExactly(LocalTime.of(9, 0), LocalTime.NOON);
    }

    @Test
    void porInstructorFiltraSoloDespuesDeResolverGlobal() {
        ProgramacionNominal nominales = mock(ProgramacionNominal.class);
        AjusteProgramacionFechaRepository ajustes = mock(AjusteProgramacionFechaRepository.class);
        ProgramacionValidador validador = mock(ProgramacionValidador.class);
        UUID salon = UUID.randomUUID();
        UUID actividad = UUID.randomUUID();
        UUID instructorBuscado = UUID.randomUUID();
        List<OcurrenciaEfectiva> globales = List.of(
                efectiva(salon, UUID.randomUUID(), actividad, 9, 10),
                efectiva(salon, instructorBuscado, actividad, 11, 12));
        when(nominales.todasEnFecha(FECHA)).thenReturn(List.of());
        when(ajustes.findAllByFechaAndActivoTrueOrderById(FECHA)).thenReturn(List.of());
        when(validador.filtrarFailClosedYValidar(List.of())).thenReturn(globales);
        ProgramacionEfectiva service = new ProgramacionEfectiva(
                nominales, ajustes, new AplicadorAjustesProgramacion(), validador);

        assertThat(service.porInstructorYFecha(instructorBuscado, FECHA))
                .singleElement()
                .extracting(OcurrenciaEfectiva::instructorId)
                .isEqualTo(instructorBuscado);
    }

    @Test
    void resolverComponeCancelacionYAdicionAntesDeValidar() {
        UUID serie = UUID.randomUUID();
        UUID salon = UUID.randomUUID();
        UUID instructor = UUID.randomUUID();
        UUID actividad = UUID.randomUUID();
        OcurrenciaNominal recurrente = new OcurrenciaNominal(
                FECHA, serie, UUID.randomUUID(), UUID.randomUUID(), salon,
                instructor, actividad, LocalTime.of(9, 0), LocalTime.of(10, 0));
        AjusteProgramacionFecha cancelacion = AjusteProgramacionFecha.nuevoTarget(
                UUID.randomUUID(), AjusteProgramacionFecha.Tipo.CANCELACION,
                serie, FECHA, null, null, null, null, null);
        AjusteProgramacionFecha adicion = AjusteProgramacionFecha.nuevaAdicion(
                UUID.randomUUID(), FECHA, salon, instructor, actividad,
                LocalTime.of(11, 0), LocalTime.NOON);
        ProgramacionNominal nominales = mock(ProgramacionNominal.class);
        AjusteProgramacionFechaRepository ajustes = mock(AjusteProgramacionFechaRepository.class);
        ProgramacionValidador validador = mock(ProgramacionValidador.class);
        when(nominales.todasEnFecha(FECHA)).thenReturn(List.of(recurrente));
        when(ajustes.findAllByFechaAndActivoTrueOrderById(FECHA))
                .thenReturn(List.of(cancelacion, adicion));
        List<OcurrenciaEfectiva> compuestas = new AplicadorAjustesProgramacion()
                .aplicar(List.of(recurrente), List.of(cancelacion, adicion));
        when(validador.filtrarFailClosedYValidar(compuestas)).thenReturn(compuestas);
        ProgramacionEfectiva service = new ProgramacionEfectiva(
                nominales, ajustes, new AplicadorAjustesProgramacion(), validador);

        assertThat(service.resolverGlobal(FECHA))
                .singleElement()
                .extracting(OcurrenciaEfectiva::origen)
                .isEqualTo(OcurrenciaEfectiva.Origen.ADICION);
    }

    @Test
    void invarianteGlobalNoEntregaResultadoParcial() {
        ProgramacionNominal nominales = mock(ProgramacionNominal.class);
        AjusteProgramacionFechaRepository ajustes = mock(AjusteProgramacionFechaRepository.class);
        ProgramacionValidador validador = mock(ProgramacionValidador.class);
        when(nominales.todasEnFecha(FECHA)).thenReturn(List.of());
        when(ajustes.findAllByFechaAndActivoTrueOrderById(FECHA)).thenReturn(List.of());
        when(validador.filtrarFailClosedYValidar(List.of()))
                .thenThrow(new com.feelingpilates.programacion.dominio.ProgramacionInvarianteException(
                        ProgramacionErrores.OCURRENCIA_EFECTIVA_DUPLICADA,
                        "estado corrupto", new com.feelingpilates.programacion.dominio.ReferenciaOcurrencia(
                        com.feelingpilates.programacion.dominio.ReferenciaOcurrencia.Tipo.AJUSTE,
                        UUID.randomUUID(), FECHA)));
        ProgramacionEfectiva service = new ProgramacionEfectiva(
                nominales, ajustes, new AplicadorAjustesProgramacion(), validador);

        assertThatThrownBy(() -> service.resolverGlobal(FECHA))
                .isInstanceOf(com.feelingpilates.programacion.dominio.ProgramacionInvarianteException.class)
                .hasMessageContaining(ProgramacionErrores.OCURRENCIA_EFECTIVA_DUPLICADA);
    }

    private OcurrenciaEfectiva efectiva(
            UUID salon, UUID instructor, UUID actividad, int inicio, int fin) {
        UUID id = UUID.randomUUID();
        return new OcurrenciaEfectiva(
                FECHA, salon, instructor, actividad, LocalTime.of(inicio, 0),
                LocalTime.of(fin, 0), OcurrenciaEfectiva.Origen.ADICION,
                new com.feelingpilates.programacion.dominio.ReferenciaOcurrencia(
                        com.feelingpilates.programacion.dominio.ReferenciaOcurrencia.Tipo.AJUSTE,
                        id, FECHA));
    }
}
