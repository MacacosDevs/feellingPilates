package com.feelingpilates.ubicaciones.servicio;

import com.feelingpilates.exception.ValidacionException;
import com.feelingpilates.ubicaciones.dto.HorarioOperacionRequest;
import com.feelingpilates.ubicaciones.dto.SalonDetalleResponse;
import com.feelingpilates.ubicaciones.dto.SalonRequest;
import com.feelingpilates.ubicaciones.entidad.Estado;
import com.feelingpilates.ubicaciones.entidad.HorarioOperacion;
import com.feelingpilates.ubicaciones.entidad.Municipio;
import com.feelingpilates.ubicaciones.entidad.Salon;
import com.feelingpilates.ubicaciones.repositorio.HorarioOperacionRepository;
import com.feelingpilates.ubicaciones.repositorio.MunicipioRepository;
import com.feelingpilates.ubicaciones.repositorio.SalonRecursoRepository;
import com.feelingpilates.ubicaciones.repositorio.SalonRepository;
import com.feelingpilates.ubicaciones.repositorio.TipoActividadRepository;
import com.feelingpilates.ubicaciones.repositorio.TipoRecursoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * F2B.2: el PUT de salon dejo de ser destructivo con los horarios. El frontend actual SIEMPRE
 * envia {@code horarios}, incluso para cambiar solo el telefono, asi que estos tests fijan la
 * cuarentena de compatibilidad: payload equivalente = no-op, payload distinto = rechazo.
 */
class SalonServiceTest {

    private static final UUID SALON_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final short ESTADO_ID = 22;
    private static final short MUNICIPIO_ID = 14;
    private static final LocalDate HOY = LocalDate.of(2026, 8, 23);
    private static final Clock RELOJ = Clock.fixed(
            HOY.atStartOfDay(ZoneId.of("UTC")).toInstant(), ZoneId.of("UTC"));

    private SalonRepository salonRepository;
    private HorarioOperacionRepository horarioOperacionRepository;
    private TipoActividadRepository tipoActividadRepository;
    private TipoRecursoRepository tipoRecursoRepository;
    private SalonRecursoRepository salonRecursoRepository;
    private MunicipioRepository municipioRepository;
    private SalonService service;

    private Salon salon;
    private List<HorarioOperacion> horariosEnBd;

    @BeforeEach
    void preparar() {
        salonRepository = mock(SalonRepository.class);
        horarioOperacionRepository = mock(HorarioOperacionRepository.class);
        tipoActividadRepository = mock(TipoActividadRepository.class);
        tipoRecursoRepository = mock(TipoRecursoRepository.class);
        salonRecursoRepository = mock(SalonRecursoRepository.class);
        municipioRepository = mock(MunicipioRepository.class);

        salon = new Salon();
        salon.setId(SALON_ID);
        salon.setNombre("Juriquilla");
        salon.setEstadoId(ESTADO_ID);
        salon.setMunicipioId(MUNICIPIO_ID);
        salon.setTelefono("4420000000");

        Estado estado = new Estado();
        estado.setId(ESTADO_ID);
        estado.setNombre("Querétaro");
        Municipio municipio = new Municipio();
        municipio.setId(new Municipio.Id(ESTADO_ID, MUNICIPIO_ID));
        municipio.setEstado(estado);
        municipio.setNombre("Querétaro");

        horariosEnBd = new ArrayList<>(List.of(
                horario((short) 1, LocalTime.of(8, 0), LocalTime.of(20, 0), null, null),
                horario((short) 2, LocalTime.of(8, 0), LocalTime.of(20, 0), null, null)));

        when(salonRepository.findById(SALON_ID)).thenReturn(Optional.of(salon));
        when(salonRepository.save(any(Salon.class))).thenAnswer(i -> i.getArgument(0));
        when(municipioRepository.findById(any())).thenReturn(Optional.of(municipio));
        when(tipoActividadRepository.findAllById(any())).thenReturn(List.of());
        when(salonRecursoRepository.findBySalonId(any())).thenReturn(List.of());
        when(horarioOperacionRepository.findBySalonIdOrderByDiaSemana(SALON_ID))
                .thenAnswer(i -> List.copyOf(horariosEnBd));

        service = new SalonService(
                salonRepository,
                horarioOperacionRepository,
                tipoActividadRepository,
                tipoRecursoRepository,
                salonRecursoRepository,
                municipioRepository,
                RELOJ);
    }

    // --- A: horarios == null no toca nada -------------------------------------------------

    @Test
    void actualizarConHorariosNuloNoTocaLosHorarios() {
        service.actualizar(SALON_ID, request("4421111111", null));

        verify(horarioOperacionRepository, never()).deleteBySalonId(any());
        verify(horarioOperacionRepository, never()).save(any());
        assertThat(salon.getTelefono()).isEqualTo("4421111111");
    }

    @Test
    void actualizarSoloElTelefonoConservaLosHorariosIdenticos() {
        service.actualizar(SALON_ID, request("4421111111", null));

        assertThat(horariosEnBd).extracting(
                        HorarioOperacion::getDiaSemana, HorarioOperacion::getHoraApertura, HorarioOperacion::getHoraCierre)
                .containsExactly(
                        tuple((short) 1, LocalTime.of(8, 0), LocalTime.of(20, 0)),
                        tuple((short) 2, LocalTime.of(8, 0), LocalTime.of(20, 0)));
    }

    // --- B/C: payload del frontend actual, identico (y desordenado) ------------------------

    @Test
    void actualizarConHorariosIdenticosEsNoOpYPermiteCambiarOtrosCampos() {
        service.actualizar(SALON_ID, request("4421111111", List.of(
                horarioRequest((short) 1, 8, 20),
                horarioRequest((short) 2, 8, 20))));

        verify(horarioOperacionRepository, never()).deleteBySalonId(any());
        verify(horarioOperacionRepository, never()).save(any());
        assertThat(salon.getTelefono()).isEqualTo("4421111111");
    }

    /** El frontend construye la lista desde un arreglo de dias; el orden no es identidad. */
    @Test
    void actualizarConLosMismosHorariosEnOtroOrdenSeAcepta() {
        SalonDetalleResponse resultado = service.actualizar(SALON_ID, request("4421111111", List.of(
                horarioRequest((short) 2, 8, 20),
                horarioRequest((short) 1, 8, 20))));

        verify(horarioOperacionRepository, never()).deleteBySalonId(any());
        verify(horarioOperacionRepository, never()).save(any());
        assertThat(resultado.telefono()).isEqualTo("4421111111");
    }

    // --- D: un horario distinto se rechaza -------------------------------------------------

    @Test
    void actualizarConUnHorarioDistintoSeRechaza() {
        assertThatThrownBy(() -> service.actualizar(SALON_ID, request("4421111111", List.of(
                horarioRequest((short) 1, 9, 20),
                horarioRequest((short) 2, 8, 20)))))
                .isInstanceOf(ValidacionException.class)
                .hasMessageContaining("HORARIOS_REQUIEREN_VERSIONADO");

        verify(horarioOperacionRepository, never()).deleteBySalonId(any());
        verify(horarioOperacionRepository, never()).save(any());
    }

    @Test
    void agregarUnDiaNuevoSeRechaza() {
        assertThatThrownBy(() -> service.actualizar(SALON_ID, request(null, List.of(
                horarioRequest((short) 1, 8, 20),
                horarioRequest((short) 2, 8, 20),
                horarioRequest((short) 3, 8, 20)))))
                .isInstanceOf(ValidacionException.class)
                .hasMessageContaining("HORARIOS_REQUIEREN_VERSIONADO");
    }

    @Test
    void quitarUnDiaSeRechaza() {
        assertThatThrownBy(() -> service.actualizar(SALON_ID, request(null, List.of(
                horarioRequest((short) 1, 8, 20)))))
                .isInstanceOf(ValidacionException.class)
                .hasMessageContaining("HORARIOS_REQUIEREN_VERSIONADO");
    }

    /**
     * El rechazo ocurre ANTES de aplicar los demas campos: un cambio de horario no puede dejar el
     * telefono a medio actualizar en la entidad gestionada.
     */
    @Test
    void elRechazoPorHorarioDistintoNoDejaOtrosCamposAplicados() {
        assertThatThrownBy(() -> service.actualizar(SALON_ID, request("4429999999", List.of(
                horarioRequest((short) 1, 9, 20),
                horarioRequest((short) 2, 8, 20)))))
                .isInstanceOf(ValidacionException.class);

        assertThat(salon.getTelefono()).isEqualTo("4420000000");
        assertThat(salon.getNombre()).isEqualTo("Juriquilla");
        verify(salonRepository, never()).save(any());
    }

    // --- E: lista vacia ---------------------------------------------------------------------

    @Test
    void listaVaciaSobreUnSalonConHorariosSeRechaza() {
        assertThatThrownBy(() -> service.actualizar(SALON_ID, request(null, List.of())))
                .isInstanceOf(ValidacionException.class)
                .hasMessageContaining("HORARIOS_REQUIEREN_VERSIONADO");

        verify(horarioOperacionRepository, never()).deleteBySalonId(any());
    }

    /** Si el salon ya no tiene horarios, la lista vacia coincide con el estado actual: no-op. */
    @Test
    void listaVaciaSobreUnSalonSinHorariosEsNoOp() {
        horariosEnBd.clear();

        service.actualizar(SALON_ID, request("4421111111", List.of()));

        verify(horarioOperacionRepository, never()).deleteBySalonId(any());
        assertThat(salon.getTelefono()).isEqualTo("4421111111");
    }

    // --- validaciones preservadas -----------------------------------------------------------

    @Test
    void conservaLaValidacionDeCierrePosteriorAApertura() {
        assertThatThrownBy(() -> service.actualizar(SALON_ID, request(null, List.of(
                horarioRequest((short) 1, 20, 8)))))
                .isInstanceOf(ValidacionException.class)
                .hasMessageContaining("hora de cierre debe ser posterior");
    }

    @Test
    void rechazaDosDefinicionesParaElMismoDiaEnElRequest() {
        assertThatThrownBy(() -> service.actualizar(SALON_ID, request(null, List.of(
                horarioRequest((short) 1, 8, 20),
                horarioRequest((short) 1, 9, 21)))))
                .isInstanceOf(ValidacionException.class)
                .hasMessageContaining("más de una vez");
    }

    // --- F/G: creacion ----------------------------------------------------------------------

    @Test
    void laCreacionSigueCreandoLosHorariosIniciales() {
        when(salonRepository.save(any(Salon.class))).thenAnswer(i -> {
            Salon nuevo = i.getArgument(0);
            nuevo.setId(SALON_ID);
            return nuevo;
        });
        horariosEnBd.clear();

        service.crear(request("4420000000", List.of(
                horarioRequest((short) 1, 8, 20),
                horarioRequest((short) 2, 8, 20))));

        verify(horarioOperacionRepository, never()).deleteBySalonId(any());
        verify(horarioOperacionRepository, org.mockito.Mockito.times(2)).save(any(HorarioOperacion.class));
    }

    // --- H/I/J: mapDetalle ------------------------------------------------------------------

    @Test
    void elDetalleConDatosLegadosDevuelveLaConfiguracionSemanalCompleta() {
        SalonDetalleResponse detalle = service.obtenerDetalle(SALON_ID);

        assertThat(detalle.horarios())
                .extracting("diaSemana", "horaApertura", "horaCierre")
                .containsExactly(
                        tuple((short) 1, LocalTime.of(8, 0), LocalTime.of(20, 0)),
                        tuple((short) 2, LocalTime.of(8, 0), LocalTime.of(20, 0)));
    }

    /** Prepara el mundo posterior al DROP UNIQUE: solo la version vigente hoy sale al detalle. */
    @Test
    void elDetalleFiltraLasVersionesQueNoEstanVigentesHoy() {
        horariosEnBd.clear();
        horariosEnBd.add(horario((short) 1, LocalTime.of(8, 0), LocalTime.of(20, 0), null, HOY.minusDays(1)));
        horariosEnBd.add(horario((short) 1, LocalTime.of(9, 0), LocalTime.of(21, 0), HOY, null));
        horariosEnBd.add(horario((short) 2, LocalTime.of(7, 0), LocalTime.of(15, 0), HOY.plusDays(10), null));

        SalonDetalleResponse detalle = service.obtenerDetalle(SALON_ID);

        assertThat(detalle.horarios())
                .extracting("diaSemana", "horaApertura", "horaCierre")
                .containsExactly(tuple((short) 1, LocalTime.of(9, 0), LocalTime.of(21, 0)));
    }

    @Test
    void dosVersionesVigentesElMismoDiaFallanRuidosamente() {
        horariosEnBd.clear();
        horariosEnBd.add(horario((short) 1, LocalTime.of(8, 0), LocalTime.of(20, 0), null, null));
        horariosEnBd.add(horario((short) 1, LocalTime.of(9, 0), LocalTime.of(21, 0), HOY.minusDays(3), null));

        assertThatThrownBy(() -> service.obtenerDetalle(SALON_ID))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("más de una versión vigente");
    }

    /** El contrato HTTP no cambia: el detalle no expone vigencias ni historial. */
    @Test
    void elDetalleNoExponeVigenciasNiHistorial() {
        List<String> campos = java.util.Arrays.stream(
                        com.feelingpilates.ubicaciones.dto.HorarioOperacionResponse.class.getRecordComponents())
                .map(java.lang.reflect.RecordComponent::getName)
                .toList();

        assertThat(campos).containsExactly("id", "diaSemana", "horaApertura", "horaCierre");
    }

    /** La comparacion es por definicion funcional del horario, no por el id de la fila. */
    @Test
    void laComparacionIgnoraElIdDeLaFila() {
        horariosEnBd.forEach(h -> h.setId(UUID.randomUUID()));

        service.actualizar(SALON_ID, request("4421111111", List.of(
                horarioRequest((short) 1, 8, 20),
                horarioRequest((short) 2, 8, 20))));

        verify(horarioOperacionRepository, never()).save(any());
    }

    private SalonRequest request(String telefono, List<HorarioOperacionRequest> horarios) {
        return new SalonRequest(
                "Juriquilla", ESTADO_ID, MUNICIPIO_ID, telefono,
                null, null, null, null, null, null, null, null, null,
                List.of(), horarios, List.of());
    }

    private HorarioOperacionRequest horarioRequest(short diaSemana, int apertura, int cierre) {
        return new HorarioOperacionRequest(diaSemana, LocalTime.of(apertura, 0), LocalTime.of(cierre, 0));
    }

    private HorarioOperacion horario(
            short diaSemana, LocalTime apertura, LocalTime cierre, LocalDate vigenteDesde, LocalDate vigenteHasta) {
        HorarioOperacion horario = new HorarioOperacion();
        horario.setSalon(salon);
        horario.setDiaSemana(diaSemana);
        horario.setHoraApertura(apertura);
        horario.setHoraCierre(cierre);
        horario.setVigenteDesde(vigenteDesde);
        horario.setVigenteHasta(vigenteHasta);
        return horario;
    }
}
