package com.feelingpilates.programacion;

import com.feelingpilates.TestcontainersConfiguration;
import com.feelingpilates.programacion.entidad.Asignacion;
import com.feelingpilates.programacion.entidad.AjusteProgramacionFecha;
import com.feelingpilates.programacion.entidad.BloqueProgramacion;
import com.feelingpilates.programacion.repositorio.AsignacionRepository;
import com.feelingpilates.programacion.repositorio.BloqueProgramacionRepository;
import jakarta.persistence.EntityManagerFactory;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Transactional
class ProgramacionPersistenciaTest {

    @Autowired
    private Flyway flyway;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Autowired
    private BloqueProgramacionRepository bloqueRepository;

    @Autowired
    private AsignacionRepository asignacionRepository;

    @Test
    void flywayMigraDesdeV1HastaV47() {
        assertThat(flyway.info().current().getVersion().getVersion()).isEqualTo("47");
        assertThat(flyway.info().applied()).hasSize(50);
    }

    @Test
    void jpaValidaYRegistraLasEntidadesDeProgramacion() {
        assertThat(entityManagerFactory.getMetamodel().entity(BloqueProgramacion.class)).isNotNull();
        assertThat(entityManagerFactory.getMetamodel().entity(Asignacion.class)).isNotNull();
        assertThat(entityManagerFactory.getMetamodel().entity(AjusteProgramacionFecha.class)).isNotNull();
    }

    @Test
    void v41CreaLasDosTablasVacias() {
        Integer bloques = jdbcTemplate.queryForObject("select count(*) from programacion_bloque", Integer.class);
        Integer asignaciones = jdbcTemplate.queryForObject(
                "select count(*) from programacion_asignacion", Integer.class);

        assertThat(bloques).isZero();
        assertThat(asignaciones).isZero();
    }

    @Test
    void repositoriosAplicanTraslapeSemanalYVigencia() {
        UUID salonId = jdbcTemplate.queryForObject("select id from salon limit 1", UUID.class);
        UUID instructorId = jdbcTemplate.queryForObject("select id from usuario limit 1", UUID.class);
        UUID actividadId = jdbcTemplate.queryForObject("select id from tipo_actividad limit 1", UUID.class);
        UUID bloqueId = UUID.randomUUID();

        jdbcTemplate.update("""
                insert into programacion_bloque
                    (id, serie_id, salon_id, dia_semana, hora_inicio, hora_fin,
                     vigente_desde, vigente_hasta, activo)
                values (?, ?, ?, 1, ?::time, ?::time, ?::date, ?::date, true)
                """,
                bloqueId, UUID.randomUUID(), salonId,
                "10:00", "12:00", "2027-01-01", "2027-01-31");
        jdbcTemplate.update("""
                insert into programacion_asignacion
                    (id, serie_id, bloque_id, instructor_id, tipo_actividad_id,
                     hora_inicio, hora_fin, vigente_desde, vigente_hasta, activo)
                values (?, ?, ?, ?, ?, ?::time, ?::time, ?::date, ?::date, true)
                """,
                UUID.randomUUID(), UUID.randomUUID(), bloqueId, instructorId, actividadId,
                "10:00", "12:00", "2027-01-01", "2027-01-31");

        assertThat(bloqueRepository.buscarTraslapesActivos(
                salonId, (short) 1, LocalTime.of(11, 0), LocalTime.of(13, 0),
                LocalDate.of(2027, 1, 15), null)).singleElement();
        assertThat(bloqueRepository.buscarTraslapesActivos(
                salonId, (short) 1, LocalTime.NOON, LocalTime.of(14, 0),
                LocalDate.of(2027, 1, 15), null)).isEmpty();
        assertThat(bloqueRepository.buscarTraslapesActivos(
                salonId, (short) 1, LocalTime.of(11, 0), LocalTime.of(13, 0),
                LocalDate.of(2027, 2, 1), null)).isEmpty();

        assertThat(asignacionRepository.buscarConflictosRecurrentesDelInstructor(
                instructorId, (short) 1, LocalTime.of(11, 0), LocalTime.of(13, 0),
                LocalDate.of(2027, 1, 15), null)).singleElement();
        assertThat(asignacionRepository.buscarConflictosRecurrentesDelInstructor(
                instructorId, (short) 2, LocalTime.of(11, 0), LocalTime.of(13, 0),
                LocalDate.of(2027, 1, 15), null)).isEmpty();
        assertThat(asignacionRepository.buscarConflictosRecurrentesDelInstructor(
                instructorId, (short) 1, LocalTime.of(11, 0), LocalTime.of(13, 0),
                LocalDate.of(2027, 2, 1), null)).isEmpty();
    }

    @Test
    void conflictoGlobalDelInstructorEsRealmenteCrossSalonEnPostgres() {
        List<UUID> salones = jdbcTemplate.queryForList("select id from salon order by nombre limit 2", UUID.class);
        UUID salonA = salones.get(0);
        UUID salonB = salones.get(1);
        UUID instructorId = jdbcTemplate.queryForObject("select id from usuario limit 1", UUID.class);
        UUID actividadId = jdbcTemplate.queryForObject("select id from tipo_actividad limit 1", UUID.class);

        UUID bloqueA = insertarBloque(salonA, (short) 1, "10:00", "12:00", "2027-01-01", "2027-03-31");
        insertarAsignacion(bloqueA, instructorId, actividadId, "10:00", "12:00", "2027-01-01", "2027-03-31");
        insertarBloque(salonB, (short) 1, "11:00", "13:00", "2027-01-01", "2027-03-31");

        List<Asignacion> conflictos = asignacionRepository.buscarConflictosRecurrentesDelInstructor(
                instructorId, (short) 1, LocalTime.of(11, 0), LocalTime.of(13, 0),
                LocalDate.of(2027, 1, 15), null);

        assertThat(conflictos).extracting(Asignacion::getBloqueId).containsExactly(bloqueA);
    }

    @Test
    void asignacionDetectaConflictoCrossSalonConVigenciasAcotadas() {
        List<UUID> salones = jdbcTemplate.queryForList("select id from salon order by nombre limit 2", UUID.class);
        UUID salonA = salones.get(0);
        UUID salonB = salones.get(1);
        UUID instructorId = jdbcTemplate.queryForObject("select id from usuario limit 1", UUID.class);
        UUID actividadId = jdbcTemplate.queryForObject("select id from tipo_actividad limit 1", UUID.class);

        UUID bloqueA = insertarBloque(salonA, (short) 1, "10:00", "12:00", "2026-01-01", "2026-03-31");
        insertarAsignacion(bloqueA, instructorId, actividadId, "10:00", "12:00", "2026-01-01", "2026-03-31");
        insertarBloque(salonB, (short) 1, "11:00", "13:00", "2026-02-01", "2026-02-28");

        List<Asignacion> conflictos = asignacionRepository.buscarConflictosRecurrentesDelInstructor(
                instructorId, (short) 1, LocalTime.of(11, 0), LocalTime.of(13, 0),
                LocalDate.of(2026, 2, 1), LocalDate.of(2026, 2, 28));

        assertThat(conflictos).extracting(Asignacion::getBloqueId).containsExactly(bloqueA);
    }

    @Test
    void bloqueDetectaTraslapeConVigenciasAcotadas() {
        UUID salonId = jdbcTemplate.queryForObject("select id from salon limit 1", UUID.class);

        UUID bloqueId = insertarBloque(
                salonId, (short) 1, "10:00", "12:00", "2026-01-01", "2026-03-31");

        List<BloqueProgramacion> traslapes = bloqueRepository.buscarTraslapesActivos(
                salonId, (short) 1, LocalTime.of(11, 0), LocalTime.of(13, 0),
                LocalDate.of(2026, 2, 1), LocalDate.of(2026, 2, 28));

        assertThat(traslapes).extracting(BloqueProgramacion::getId).containsExactly(bloqueId);
    }

    @Test
    void conflictoGlobalCrossSalonNoAplicaConVigenciasDisjuntas() {
        List<UUID> salones = jdbcTemplate.queryForList("select id from salon order by nombre limit 2", UUID.class);
        UUID salonA = salones.get(0);
        UUID salonB = salones.get(1);
        UUID instructorId = jdbcTemplate.queryForObject("select id from usuario limit 1", UUID.class);
        UUID actividadId = jdbcTemplate.queryForObject("select id from tipo_actividad limit 1", UUID.class);

        UUID bloqueA = insertarBloque(salonA, (short) 1, "10:00", "12:00", "2026-01-01", "2026-01-31");
        insertarAsignacion(bloqueA, instructorId, actividadId, "10:00", "12:00", "2026-01-01", "2026-01-31");
        insertarBloque(salonB, (short) 1, "11:00", "13:00", "2026-02-01", "2026-03-31");

        List<Asignacion> conflictos = asignacionRepository.buscarConflictosRecurrentesDelInstructor(
                instructorId, (short) 1, LocalTime.of(11, 0), LocalTime.of(13, 0),
                LocalDate.of(2026, 2, 1), LocalDate.of(2026, 3, 31));

        assertThat(conflictos).isEmpty();
    }

    @Test
    void asignacionConsideraInterseccionCuandoLasVigenciasCompartenElUltimoDia() {
        UUID salonId = jdbcTemplate.queryForObject("select id from salon limit 1", UUID.class);
        UUID instructorId = jdbcTemplate.queryForObject("select id from usuario limit 1", UUID.class);
        UUID actividadId = jdbcTemplate.queryForObject("select id from tipo_actividad limit 1", UUID.class);

        UUID bloqueId = insertarBloque(
                salonId, (short) 1, "10:00", "12:00", "2026-01-01", "2026-01-31");
        insertarAsignacion(
                bloqueId, instructorId, actividadId, "10:00", "12:00", "2026-01-01", "2026-01-31");

        List<Asignacion> conflictos = asignacionRepository.buscarConflictosRecurrentesDelInstructor(
                instructorId, (short) 1, LocalTime.of(11, 0), LocalTime.of(13, 0),
                LocalDate.of(2026, 1, 31), LocalDate.of(2026, 2, 28));

        assertThat(conflictos).extracting(Asignacion::getBloqueId).containsExactly(bloqueId);
    }

    @Test
    void queryGlobalTrataIntervalosContiguosComoSinConflicto() {
        UUID salonId = jdbcTemplate.queryForObject("select id from salon limit 1", UUID.class);
        UUID instructorId = jdbcTemplate.queryForObject("select id from usuario limit 1", UUID.class);
        UUID actividadId = jdbcTemplate.queryForObject("select id from tipo_actividad limit 1", UUID.class);

        UUID bloqueId = insertarBloque(salonId, (short) 1, "08:00", "10:00", "2027-01-01", "2027-03-31");
        insertarAsignacion(bloqueId, instructorId, actividadId, "08:00", "10:00", "2027-01-01", "2027-03-31");

        List<Asignacion> conflictos = asignacionRepository.buscarConflictosRecurrentesDelInstructor(
                instructorId, (short) 1, LocalTime.of(10, 0), LocalTime.of(12, 0),
                LocalDate.of(2027, 1, 15), LocalDate.of(2027, 2, 28));

        assertThat(conflictos).isEmpty();
    }

    @Test
    void bloqueNoDetectaTraslapeConVigenciasAcotadasDisjuntas() {
        UUID salonId = jdbcTemplate.queryForObject("select id from salon limit 1", UUID.class);

        insertarBloque(salonId, (short) 1, "10:00", "12:00", "2026-01-01", "2026-01-31");

        List<BloqueProgramacion> traslapes = bloqueRepository.buscarTraslapesActivos(
                salonId, (short) 1, LocalTime.of(11, 0), LocalTime.of(13, 0),
                LocalDate.of(2026, 2, 1), LocalDate.of(2026, 3, 31));

        assertThat(traslapes).isEmpty();
    }

    @Test
    void bloqueTrataIntervalosContiguosComoSinTraslapeConVigenciasAcotadas() {
        UUID salonId = jdbcTemplate.queryForObject("select id from salon limit 1", UUID.class);

        insertarBloque(salonId, (short) 1, "08:00", "10:00", "2026-01-01", "2026-03-31");

        List<BloqueProgramacion> traslapes = bloqueRepository.buscarTraslapesActivos(
                salonId, (short) 1, LocalTime.of(10, 0), LocalTime.of(12, 0),
                LocalDate.of(2026, 2, 1), LocalDate.of(2026, 2, 28));

        assertThat(traslapes).isEmpty();
    }

    @Test
    void permiteDosSegmentosDelMismoInstructorYActividadEnElMismoBloque() {
        UUID salonId = jdbcTemplate.queryForObject("select id from salon limit 1", UUID.class);
        UUID instructorId = jdbcTemplate.queryForObject("select id from usuario limit 1", UUID.class);
        UUID actividadId = jdbcTemplate.queryForObject("select id from tipo_actividad limit 1", UUID.class);

        UUID bloqueId = insertarBloque(salonId, (short) 3, "08:00", "14:00", "2027-01-01", "2027-01-31");
        insertarAsignacion(bloqueId, instructorId, actividadId, "08:00", "10:00", "2027-01-01", "2027-01-31");
        insertarAsignacion(bloqueId, instructorId, actividadId, "12:00", "14:00", "2027-01-01", "2027-01-31");

        Integer total = jdbcTemplate.queryForObject(
                "select count(*) from programacion_asignacion where bloque_id = ?", Integer.class, bloqueId);

        assertThat(total).isEqualTo(2);
    }

    @Test
    void asignacionNoDetectaConflictoCuandoVigenciaExistenteEsPosteriorAlaConsulta() {
        UUID salonId = jdbcTemplate.queryForObject("select id from salon limit 1", UUID.class);
        UUID instructorId = jdbcTemplate.queryForObject("select id from usuario limit 1", UUID.class);
        UUID actividadId = jdbcTemplate.queryForObject("select id from tipo_actividad limit 1", UUID.class);

        UUID bloqueId = insertarBloque(salonId, (short) 1, "10:00", "12:00", "2026-03-01", null);
        insertarAsignacion(bloqueId, instructorId, actividadId, "10:00", "12:00", "2026-03-01", null);

        List<Asignacion> conflictos = asignacionRepository.buscarConflictosRecurrentesDelInstructor(
                instructorId, (short) 1, LocalTime.of(11, 0), LocalTime.of(13, 0),
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 2, 28));

        assertThat(conflictos).isEmpty();
    }

    @Test
    void bloqueNoDetectaTraslapeCuandoVigenciaExistenteEsPosteriorAlaConsulta() {
        UUID salonId = jdbcTemplate.queryForObject("select id from salon limit 1", UUID.class);

        insertarBloque(salonId, (short) 1, "10:00", "12:00", "2026-03-01", null);

        List<BloqueProgramacion> traslapes = bloqueRepository.buscarTraslapesActivos(
                salonId, (short) 1, LocalTime.of(11, 0), LocalTime.of(13, 0),
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 2, 28));

        assertThat(traslapes).isEmpty();
    }

    private UUID insertarBloque(
            UUID salonId, short diaSemana, String horaInicio, String horaFin,
            String vigenteDesde, String vigenteHasta) {
        UUID id = UUID.randomUUID();
        jdbcTemplate.update("""
                insert into programacion_bloque
                    (id, serie_id, salon_id, dia_semana, hora_inicio, hora_fin,
                     vigente_desde, vigente_hasta, activo)
                values (?, ?, ?, ?, ?::time, ?::time, ?::date, ?::date, true)
                """,
                id, UUID.randomUUID(), salonId, diaSemana, horaInicio, horaFin, vigenteDesde, vigenteHasta);
        return id;
    }

    private UUID insertarAsignacion(
            UUID bloqueId, UUID instructorId, UUID actividadId, String horaInicio, String horaFin,
            String vigenteDesde, String vigenteHasta) {
        UUID id = UUID.randomUUID();
        jdbcTemplate.update("""
                insert into programacion_asignacion
                    (id, serie_id, bloque_id, instructor_id, tipo_actividad_id,
                     hora_inicio, hora_fin, vigente_desde, vigente_hasta, activo)
                values (?, ?, ?, ?, ?, ?::time, ?::time, ?::date, ?::date, true)
                """,
                id, UUID.randomUUID(), bloqueId, instructorId, actividadId,
                horaInicio, horaFin, vigenteDesde, vigenteHasta);
        return id;
    }
}
