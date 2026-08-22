package com.feelingpilates.programacion;

import com.feelingpilates.TestcontainersConfiguration;
import com.feelingpilates.programacion.entidad.Asignacion;
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
    void flywayMigraDesdeV1HastaV41() {
        assertThat(flyway.info().current().getVersion().getVersion()).isEqualTo("41");
        assertThat(flyway.info().applied()).hasSize(44);
    }

    @Test
    void jpaValidaYRegistraLasEntidadesDeProgramacion() {
        assertThat(entityManagerFactory.getMetamodel().entity(BloqueProgramacion.class)).isNotNull();
        assertThat(entityManagerFactory.getMetamodel().entity(Asignacion.class)).isNotNull();
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
}
