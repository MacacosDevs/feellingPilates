package com.feelingpilates.ubicaciones.repositorio;

import com.feelingpilates.TestcontainersConfiguration;
import com.feelingpilates.ubicaciones.entidad.HorarioOperacion;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * PostgreSQL/Testcontainers real (no mocks) contra las queries temporales nuevas de F2B.1.
 * La mayoria de los tests insertan una sola fila por combinacion salon+dia por transaccion.
 * Desde F2B.3a (ex_horario_operacion_vigencia) tambien se prueban escenarios multiversion
 * reales con dos filas no solapadas para el mismo salon+dia (ver seccion "multiversion real").
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Transactional
class HorarioOperacionRepositoryTest {

    @Autowired
    private HorarioOperacionRepository horarioOperacionRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // ---------- findVigente ----------

    @Test
    void findVigenteLegacyResuelvePasadoPresenteYFuturo() {
        UUID salonId = salonA();
        insertarHorario(salonId, (short) 1, null, null);

        assertThat(horarioOperacionRepository.findVigente(salonId, (short) 1, LocalDate.of(2000, 1, 1)))
                .hasSize(1);
        assertThat(horarioOperacionRepository.findVigente(salonId, (short) 1, LocalDate.of(2026, 8, 23)))
                .hasSize(1);
        assertThat(horarioOperacionRepository.findVigente(salonId, (short) 1, LocalDate.of(3000, 1, 1)))
                .hasSize(1);
    }

    @Test
    void findVigenteIncluyeFechaDesdeExacta() {
        UUID salonId = salonA();
        insertarHorario(salonId, (short) 2, LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30));

        assertThat(horarioOperacionRepository.findVigente(salonId, (short) 2, LocalDate.of(2026, 9, 1)))
                .hasSize(1);
    }

    @Test
    void findVigenteIncluyeFechaHastaExacta() {
        UUID salonId = salonA();
        insertarHorario(salonId, (short) 3, LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30));

        assertThat(horarioOperacionRepository.findVigente(salonId, (short) 3, LocalDate.of(2026, 9, 30)))
                .hasSize(1);
    }

    @Test
    void findVigenteExcluyeFechaFueraDeVigencia() {
        UUID salonId = salonA();
        insertarHorario(salonId, (short) 4, LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30));

        assertThat(horarioOperacionRepository.findVigente(salonId, (short) 4, LocalDate.of(2026, 8, 31)))
                .isEmpty();
        assertThat(horarioOperacionRepository.findVigente(salonId, (short) 4, LocalDate.of(2026, 10, 1)))
                .isEmpty();
    }

    @Test
    void findVigenteNoDevuelveFilasDeOtroDiaSemana() {
        UUID salonId = salonA();
        insertarHorario(salonId, (short) 5, null, null);

        assertThat(horarioOperacionRepository.findVigente(salonId, (short) 6, LocalDate.of(2026, 8, 23)))
                .isEmpty();
    }

    @Test
    void findVigenteNoDevuelveFilasDeOtroSalon() {
        UUID salonId = salonA();
        UUID otroSalonId = salonB();
        insertarHorario(salonId, (short) 0, null, null);

        assertThat(horarioOperacionRepository.findVigente(otroSalonId, (short) 0, LocalDate.of(2026, 8, 23)))
                .isEmpty();
    }

    // ---------- findVersionesQueIntersectan ----------

    @Test
    void findVersionesQueIntersectanRangoAcotadoQueIntersecta() {
        UUID salonId = salonA();
        insertarHorario(salonId, (short) 1, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 6, 30));

        assertThat(horarioOperacionRepository.findVersionesQueIntersectan(
                salonId, (short) 1, LocalDate.of(2026, 6, 1), LocalDate.of(2026, 12, 31)))
                .hasSize(1);
    }

    @Test
    void findVersionesQueIntersectanRangoAcotadoDisjunto() {
        UUID salonId = salonA();
        insertarHorario(salonId, (short) 2, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 6, 30));

        assertThat(horarioOperacionRepository.findVersionesQueIntersectan(
                salonId, (short) 2, LocalDate.of(2026, 7, 1), LocalDate.of(2026, 12, 31)))
                .isEmpty();
    }

    @Test
    void findVersionesQueIntersectanConsultaAbiertaHastaNull() {
        UUID salonId = salonA();
        insertarHorario(salonId, (short) 3, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 6, 30));

        assertThat(horarioOperacionRepository.findVersionesQueIntersectan(
                salonId, (short) 3, LocalDate.of(2026, 1, 1), null))
                .hasSize(1);
        assertThat(horarioOperacionRepository.findVersionesQueIntersectan(
                salonId, (short) 3, LocalDate.of(2026, 7, 1), null))
                .isEmpty();
    }

    @Test
    void findVersionesQueIntersectanIncluyeVersionLegacy() {
        UUID salonId = salonA();
        insertarHorario(salonId, (short) 4, null, null);

        assertThat(horarioOperacionRepository.findVersionesQueIntersectan(
                salonId, (short) 4, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31)))
                .hasSize(1);
        assertThat(horarioOperacionRepository.findVersionesQueIntersectan(
                salonId, (short) 4, LocalDate.of(2026, 1, 1), null))
                .hasSize(1);
    }

    @Test
    void findVersionesQueIntersectanNoDevuelveOtroDiaSemana() {
        UUID salonId = salonA();
        insertarHorario(salonId, (short) 5, LocalDate.of(2026, 1, 1), null);

        assertThat(horarioOperacionRepository.findVersionesQueIntersectan(
                salonId, (short) 6, LocalDate.of(2026, 1, 1), null))
                .isEmpty();
    }

    @Test
    void findVersionesQueIntersectanNoDevuelveOtroSalon() {
        UUID salonId = salonA();
        UUID otroSalonId = salonB();
        insertarHorario(salonId, (short) 0, LocalDate.of(2026, 1, 1), null);

        assertThat(horarioOperacionRepository.findVersionesQueIntersectan(
                otroSalonId, (short) 0, LocalDate.of(2026, 1, 1), null))
                .isEmpty();
    }

    // ---------- multiversion real (F2B.3a: ex_horario_operacion_vigencia) ----------

    @Test
    void findVigenteConDosVersionesContiguasResuelveCadaFechaASuVersion() {
        UUID salonId = salonA();
        short dia = 1;
        UUID versionA = insertarHorario(salonId, dia, null, LocalDate.of(2026, 8, 31));
        UUID versionB = insertarHorario(salonId, dia, LocalDate.of(2026, 9, 1), null);

        List<HorarioOperacion> enA = horarioOperacionRepository.findVigente(salonId, dia, LocalDate.of(2026, 8, 31));
        assertThat(enA).hasSize(1);
        assertThat(enA.get(0).getId()).isEqualTo(versionA);

        List<HorarioOperacion> enB = horarioOperacionRepository.findVigente(salonId, dia, LocalDate.of(2026, 9, 1));
        assertThat(enB).hasSize(1);
        assertThat(enB.get(0).getId()).isEqualTo(versionB);
    }

    @Test
    void findVersionesQueIntersectanConDosVersionesContiguasDevuelveAmbasAlCruzarFrontera() {
        UUID salonId = salonA();
        short dia = 2;
        UUID versionA = insertarHorario(salonId, dia, null, LocalDate.of(2026, 8, 31));
        UUID versionB = insertarHorario(salonId, dia, LocalDate.of(2026, 9, 1), null);

        List<HorarioOperacion> resultado = horarioOperacionRepository.findVersionesQueIntersectan(
                salonId, dia, LocalDate.of(2026, 8, 1), LocalDate.of(2026, 10, 1));

        assertThat(resultado).extracting(HorarioOperacion::getId).containsExactly(versionA, versionB);
    }

    private UUID salonA() {
        return salones().get(0);
    }

    private UUID salonB() {
        return salones().get(1);
    }

    private List<UUID> salones() {
        List<UUID> ids = jdbcTemplate.queryForList("select id from salon order by nombre", UUID.class);
        assertThat(ids).hasSizeGreaterThanOrEqualTo(2);
        return ids;
    }

    private UUID insertarHorario(UUID salonId, short diaSemana, LocalDate desde, LocalDate hasta) {
        UUID id = UUID.randomUUID();
        jdbcTemplate.update("""
                insert into horario_operacion
                    (id, salon_id, dia_semana, hora_apertura, hora_cierre, vigente_desde, vigente_hasta)
                values (?, ?, ?, '08:00', '20:00', ?, ?)
                """,
                id, salonId, diaSemana, desde, hasta);
        return id;
    }
}
