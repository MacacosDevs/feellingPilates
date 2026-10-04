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
    void flywayMigraCatalogoCanonicoCompletoSinMigracionesDesconocidas() {
        var info = flyway.info();
        var versions = new java.util.HashSet<String>();
        for (int version = 1; version <= 46; version++) versions.add(Integer.toString(version));
        versions.addAll(java.util.Set.of("22.1", "22.2", "22.3"));
        boolean prerequisite = java.util.Arrays.stream(info.all()).anyMatch(
                migration -> "V47__programacion_ajuste_fecha.sql".equals(migration.getScript()));
        if (prerequisite) versions.add("47");
        assertThat(info.current().getVersion().getVersion()).isEqualTo(prerequisite ? "47" : "46");
        assertThat(info.pending()).isEmpty();
        assertThat(info.all()).hasSize(prerequisite ? 50 : 49);
        assertThat(info.applied()).hasSize(prerequisite ? 50 : 49);
        assertThat(java.util.Arrays.stream(info.all()).map(migration -> {
            assertThat(migration.getState()).isEqualTo(org.flywaydb.core.api.MigrationState.SUCCESS);
            return migration.getVersion().getVersion();
        }).toList()).containsExactlyInAnyOrderElementsOf(versions);
        assertThat(jdbcTemplate.queryForObject(
                "SELECT to_regclass('public.programacion_ajuste_fecha') IS NOT NULL", Boolean.class))
                .isEqualTo(prerequisite);
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


    @Test
    void fixturesV46RechazanCatalogosDesconocidosYConservanFailClosed() {
        var baseline = new java.util.ArrayList<org.flywaydb.core.api.MigrationInfo>();
        for (int version = 1; version <= 46; version++) baseline.add(migration(
                Integer.toString(version), org.flywaydb.core.api.MigrationState.SUCCESS, "baseline"));
        for (String version : java.util.List.of("22.1", "22.2", "22.3")) baseline.add(migration(
                version, org.flywaydb.core.api.MigrationState.SUCCESS, "baseline"));
        assertThat(validFixture(baseline, baseline, java.util.List.of(), baseline.get(45))).isTrue();
        var resolved = new java.util.ArrayList<>(baseline);
        resolved.add(migration("47", org.flywaydb.core.api.MigrationState.ABOVE_TARGET,
                "V47__programacion_ajuste_fecha.sql"));
        assertThat(validFixture(resolved, baseline, java.util.List.of(), baseline.get(45))).isTrue();
        for (var state : org.flywaydb.core.api.MigrationState.values()) {
            if (state == org.flywaydb.core.api.MigrationState.ABOVE_TARGET) continue;
            var invalid = new java.util.ArrayList<>(baseline);
            invalid.add(migration("47", state, "V47__programacion_ajuste_fecha.sql"));
            assertThat(validFixture(invalid, baseline, java.util.List.of(), baseline.get(45))).isFalse();
        }
        for (String version : java.util.List.of("46.1", "48")) {
            var invalid = new java.util.ArrayList<>(baseline);
            invalid.add(migration(version, org.flywaydb.core.api.MigrationState.ABOVE_TARGET,
                    "V47__programacion_ajuste_fecha.sql"));
            assertThat(validFixture(invalid, baseline, java.util.List.of(), baseline.get(45))).isFalse();
        }
        var wrongScript = new java.util.ArrayList<>(baseline);
        wrongScript.add(migration("47", org.flywaydb.core.api.MigrationState.ABOVE_TARGET, "V47__other.sql"));
        assertThat(validFixture(wrongScript, baseline, java.util.List.of(), baseline.get(45))).isFalse();
        resolved.add(resolved.get(49));
        assertThat(validFixture(resolved, baseline, java.util.List.of(), baseline.get(45))).isFalse();
        var missing = new java.util.ArrayList<>(baseline); missing.remove(0);
        assertThat(validFixture(missing, missing, java.util.List.of(), baseline.get(45))).isFalse();
        var duplicate = new java.util.ArrayList<>(baseline); duplicate.set(0, baseline.get(1));
        assertThat(validFixture(duplicate, duplicate, java.util.List.of(), baseline.get(45))).isFalse();
        assertThat(validFixture(baseline, baseline, java.util.List.of(baseline.get(0)), baseline.get(45))).isFalse();
        assertThat(validFixture(baseline, baseline, java.util.List.of(), null)).isFalse();
        assertThat(validFixture(baseline, baseline, java.util.List.of(), baseline.get(44))).isFalse();
        var failed = new java.util.ArrayList<>(baseline);
        failed.set(0, migration("1", org.flywaydb.core.api.MigrationState.FAILED, "baseline"));
        assertThat(validFixture(failed, failed, java.util.List.of(), baseline.get(45))).isFalse();
    }

    private static org.flywaydb.core.api.MigrationInfo migration(String version,
            org.flywaydb.core.api.MigrationState state, String script) {
        var info = org.mockito.Mockito.mock(org.flywaydb.core.api.MigrationInfo.class);
        org.mockito.Mockito.when(info.getVersion()).thenReturn(org.flywaydb.core.api.MigrationVersion.fromVersion(version));
        org.mockito.Mockito.when(info.getState()).thenReturn(state);
        org.mockito.Mockito.when(info.getScript()).thenReturn(script);
        return info;
    }

    private static boolean validFixture(java.util.List<org.flywaydb.core.api.MigrationInfo> all,
            java.util.List<org.flywaydb.core.api.MigrationInfo> applied,
            java.util.List<org.flywaydb.core.api.MigrationInfo> pending,
            org.flywaydb.core.api.MigrationInfo current) {
        var info = org.mockito.Mockito.mock(org.flywaydb.core.api.MigrationInfoService.class);
        org.mockito.Mockito.when(info.all()).thenReturn(all.toArray(org.flywaydb.core.api.MigrationInfo[]::new));
        org.mockito.Mockito.when(info.applied()).thenReturn(applied.toArray(org.flywaydb.core.api.MigrationInfo[]::new));
        org.mockito.Mockito.when(info.pending()).thenReturn(pending.toArray(org.flywaydb.core.api.MigrationInfo[]::new));
        org.mockito.Mockito.when(info.current()).thenReturn(current);
        return com.feelingpilates.transicion.programacion.adapter.jpa.testinfra.F2ePostgresTestConfiguration
                .catalogoFixtureV46Valido(info);
    }

    @Autowired
    private javax.sql.DataSource schemaDataSource;

    @Test
    void r4EsquemaFisicoTieneSoloElContratoMinimo() throws Exception {
        try (var connection = schemaDataSource.getConnection()) {
            var expected = java.util.List.of(
                    "id:uuid:NO", "tipo:varchar:NO", "fecha:date:NO", "asignacion_serie_id:uuid:YES",
                    "salon_resultado_id:uuid:YES", "instructor_resultado_id:uuid:YES",
                    "tipo_actividad_resultado_id:uuid:YES", "hora_inicio_resultado:time:YES",
                    "hora_fin_resultado:time:YES", "activo:bool:NO", "creado_en:timestamptz:NO",
                    "actualizado_en:timestamptz:NO");
            assertThat(schemaStrings(connection, "SELECT column_name||':'||udt_name||':'||is_nullable "
                    + "FROM information_schema.columns WHERE table_schema='public' "
                    + "AND table_name='programacion_ajuste_fecha' ORDER BY ordinal_position"))
                    .containsExactlyElementsOf(expected);
            assertThat(schemaStrings(connection, "SELECT column_name||':'||column_default "
                    + "FROM information_schema.columns WHERE table_schema='public' "
                    + "AND table_name='programacion_ajuste_fecha' AND column_default IS NOT NULL ORDER BY ordinal_position"))
                    .containsExactly("id:gen_random_uuid()", "activo:true", "creado_en:now()", "actualizado_en:now()");
            assertThat(schemaStrings(connection, "SELECT character_maximum_length::text FROM information_schema.columns "
                    + "WHERE table_schema='public' AND table_name='programacion_ajuste_fecha' AND column_name='tipo'"))
                    .containsExactly("16");
            assertThat(schemaStrings(connection, "SELECT datetime_precision::text FROM information_schema.columns "
                    + "WHERE table_schema='public' AND table_name='programacion_ajuste_fecha' "
                    + "AND data_type IN ('time without time zone','timestamp with time zone') ORDER BY ordinal_position"))
                    .containsExactly("6", "6", "6", "6");
            assertThat(schemaStrings(connection, "SELECT conname||':'||contype::text||':'||convalidated||':'||condeferrable "
                    + "FROM pg_constraint WHERE conrelid='programacion_ajuste_fecha'::regclass ORDER BY conname"))
                    .containsExactly("chk_ajuste_fecha_forma:c:true:false", "chk_ajuste_fecha_horas:c:true:false",
                            "chk_ajuste_fecha_tipo:c:true:false", "fk_ajuste_fecha_instructor:f:true:false",
                            "fk_ajuste_fecha_salon:f:true:false", "fk_ajuste_fecha_tipo_actividad:f:true:false",
                            "programacion_ajuste_fecha_pkey:p:true:false");
            assertThat(schemaStrings(connection, "SELECT conname||':'||confrelid::regclass::text||':'||conkey::text"
                    + "||':'||confkey::text||':'||confupdtype::text||':'||confdeltype::text||':'||confmatchtype::text "
                    + "FROM pg_constraint WHERE conrelid='programacion_ajuste_fecha'::regclass AND contype='f' ORDER BY conname"))
                    .containsExactly("fk_ajuste_fecha_instructor:usuario:{6}:{1}:a:a:s",
                            "fk_ajuste_fecha_salon:salon:{5}:{1}:a:a:s",
                            "fk_ajuste_fecha_tipo_actividad:tipo_actividad:{7}:{1}:a:a:s");
            assertThat(schemaStrings(connection, "SELECT c.relname||':'||am.amname||':'||i.indisunique||':'||i.indisvalid"
                    + "||':'||i.indkey::text||':'||i.indnatts||':'||(i.indexprs IS NULL) FROM pg_index i "
                    + "JOIN pg_class c ON c.oid=i.indexrelid JOIN pg_am am ON am.oid=c.relam "
                    + "WHERE i.indrelid='programacion_ajuste_fecha'::regclass ORDER BY c.relname"))
                    .containsExactly("idx_ajuste_fecha_fecha_activo:btree:false:true:3:1:true",
                            "programacion_ajuste_fecha_pkey:btree:true:true:1:1:true",
                            "uq_ajuste_fecha_target_activo:btree:true:true:4 3:2:true");
            var predicates = schemaStrings(connection, "SELECT pg_get_expr(indpred,indrelid) FROM pg_index "
                    + "WHERE indrelid='programacion_ajuste_fecha'::regclass AND indpred IS NOT NULL ORDER BY indexrelid::regclass::text");
            assertThat(predicates).hasSize(2);
            assertThat(predicates.get(0)).isEqualTo("activo");
            assertThat(predicates.get(1)).contains("activo", "CANCELACION", "REEMPLAZO").doesNotContain("ADICION");
            assertThat(schemaStrings(connection, "SELECT count(*)::text FROM pg_trigger "
                    + "WHERE tgrelid='programacion_ajuste_fecha'::regclass AND NOT tgisinternal"))
                    .containsExactly("0");
            assertThat(schemaStrings(connection, "SELECT count(*)::text FROM pg_constraint "
                    + "WHERE conrelid='programacion_asignacion'::regclass AND contype='x'"))
                    .containsExactly("0");
        }
    }

    @Test
    void r4TresFormasRechazanTodaCombinacionParcialInclusoInactiva() throws Exception {
        try (var connection = schemaDataSource.getConnection()) {
            connection.setAutoCommit(false);
            try {
                for (String tipo : java.util.List.of("CANCELACION", "REEMPLAZO", "ADICION")) {
                    for (boolean active : java.util.List.of(true, false)) {
                        for (int mask = 0; mask < 64; mask++) {
                            var values = adjustmentValues(connection, "REEMPLAZO"); values[1] = "'" + tipo + "'";
                            for (int bit = 0; bit < 6; bit++) if ((mask & (1 << bit)) == 0) values[3 + bit] = "NULL";
                            values[9] = Boolean.toString(active);
                            boolean valid = mask == (tipo.equals("CANCELACION") ? 1 : tipo.equals("REEMPLAZO") ? 63 : 62);
                            schemaAttempt(connection, adjustmentInsert(values), valid ? null : "23514");
                        }
                    }
                }
            } finally { connection.rollback(); }
        }
    }

    @Test
    void r4ChecksNotNullPkFkYPrecisionSePruebanEnPostgresql() throws Exception {
        try (var connection = schemaDataSource.getConnection()) {
            connection.setAutoCommit(false);
            try (var statement = connection.createStatement()) { statement.execute("SET LOCAL TIME ZONE 'UTC'"); }
            try {
                for (int column : new int[]{0, 1, 2, 9, 10, 11}) {
                    var values = adjustmentValues(connection, "ADICION"); values[column] = "NULL";
                    schemaAttempt(connection, adjustmentInsert(values), "23502");
                }
                for (String tipo : java.util.List.of("INVALIDO", "adicion", " ADICION")) {
                    var values = adjustmentValues(connection, "ADICION"); values[1] = "'" + tipo + "'";
                    schemaAttempt(connection, adjustmentInsert(values), "23514");
                }
                for (String end : java.util.List.of("'10:00:00.123456'", "'09:00:00'")) {
                    var values = adjustmentValues(connection, "ADICION"); values[8] = end;
                    schemaAttempt(connection, adjustmentInsert(values), "23514");
                }
                for (int column : new int[]{4, 5, 6}) {
                    var values = adjustmentValues(connection, "ADICION"); values[column] = "'" + UUID.randomUUID() + "'";
                    schemaAttempt(connection, adjustmentInsert(values), "23503");
                }
                var values = adjustmentValues(connection, "ADICION");
                try (var statement = connection.createStatement()) { statement.execute(adjustmentInsert(values)); }
                schemaAttempt(connection, adjustmentInsert(values), "23505");
                var row = schemaStrings(connection, "SELECT hora_inicio_resultado::text||':'||hora_fin_resultado::text"
                        + "||':'||creado_en::text||':'||actualizado_en::text FROM programacion_ajuste_fecha WHERE id=" + values[0]);
                assertThat(row).containsExactly("10:00:00.123456:11:00:00.654321:2027-01-02 03:04:05.123456+00:2026-01-01 02:03:04.654321+00");
                // Reversed technical timestamps are allowed: there is no new mutation/history policy.
                try (var statement = connection.createStatement()) {
                    statement.execute("INSERT INTO programacion_ajuste_fecha(tipo,fecha,asignacion_serie_id) "
                            + "VALUES ('CANCELACION','2027-01-04','" + UUID.randomUUID() + "'),"
                            + "('CANCELACION','2027-01-04','" + UUID.randomUUID() + "')");
                }
                assertThat(schemaStrings(connection, "SELECT count(*)::text||':'||count(DISTINCT id)::text"
                        + "||':'||bool_and(activo AND creado_en IS NOT NULL AND actualizado_en IS NOT NULL"
                        + " AND creado_en=actualizado_en AND creado_en=transaction_timestamp())::text"
                        + " FROM programacion_ajuste_fecha WHERE tipo='CANCELACION'"))
                        .containsExactly("2:2:true");
            } finally { connection.rollback(); }
        }
    }

    @Test
    void r4UnicidadParcialCubreTiposActivacionCambiosYAdicionesIdenticas() throws Exception {
        try (var connection = schemaDataSource.getConnection()) {
            connection.setAutoCommit(false);
            try {
                var cancellation = adjustmentValues(connection, "CANCELACION");
                var replacement = adjustmentValues(connection, "REEMPLAZO"); replacement[3] = cancellation[3];
                var inactive = replacement.clone(); inactive[0] = "'" + UUID.randomUUID() + "'"; inactive[9] = "false";
                var inactive2 = inactive.clone(); inactive2[0] = "'" + UUID.randomUUID() + "'";
                try (var statement = connection.createStatement()) {
                    statement.execute(adjustmentInsert(cancellation));
                    statement.execute(adjustmentInsert(inactive)); statement.execute(adjustmentInsert(inactive2));
                }
                schemaAttempt(connection, adjustmentInsert(replacement), "23505");
                var duplicateCancellation = cancellation.clone(); duplicateCancellation[0] = "'" + UUID.randomUUID() + "'";
                schemaAttempt(connection, adjustmentInsert(duplicateCancellation), "23505");
                schemaAttempt(connection, "UPDATE programacion_ajuste_fecha SET activo=true WHERE id=" + inactive[0], "23505");
                replacement[2] = "'2027-01-05'";
                try (var statement = connection.createStatement()) { statement.execute(adjustmentInsert(replacement)); }
                schemaAttempt(connection, "UPDATE programacion_ajuste_fecha SET fecha='2027-01-04' WHERE id=" + replacement[0], "23505");
                var another = adjustmentValues(connection, "CANCELACION");
                try (var statement = connection.createStatement()) { statement.execute(adjustmentInsert(another)); }
                schemaAttempt(connection, "UPDATE programacion_ajuste_fecha SET asignacion_serie_id=" + cancellation[3]
                        + " WHERE id=" + another[0], "23505");
                try (var statement = connection.createStatement()) {
                    statement.execute("UPDATE programacion_ajuste_fecha SET activo=false WHERE id=" + cancellation[0]);
                    statement.execute("UPDATE programacion_ajuste_fecha SET activo=true WHERE id=" + inactive[0]);
                }
                var addition = adjustmentValues(connection, "ADICION"); var identical = addition.clone();
                identical[0] = "'" + UUID.randomUUID() + "'";
                try (var statement = connection.createStatement()) {
                    statement.execute(adjustmentInsert(addition)); statement.execute(adjustmentInsert(identical));
                }
                assertThat(schemaStrings(connection, "SELECT count(*)::text FROM programacion_ajuste_fecha WHERE tipo='ADICION'"))
                        .containsExactly("2");
                var toTarget = addition.clone(); toTarget[1] = "'REEMPLAZO'"; toTarget[3] = cancellation[3];
                schemaAttempt(connection, "UPDATE programacion_ajuste_fecha SET tipo='REEMPLAZO',asignacion_serie_id="
                        + cancellation[3] + " WHERE id=" + addition[0], "23505");
            } finally { connection.rollback(); }
        }
    }

    @Test
    void r4UpgradeV46PreservaHistoriaDatosEsquemaYSerializaTargetConcurrente() throws Exception {
        try (var postgres = new org.testcontainers.containers.PostgreSQLContainer<>("postgres:16-alpine")) {
            postgres.start();
            var dataSource = new org.postgresql.ds.PGSimpleDataSource();
            dataSource.setURL(postgres.getJdbcUrl()); dataSource.setUser(postgres.getUsername()); dataSource.setPassword(postgres.getPassword());
            var baseline = Flyway.configure().dataSource(dataSource).locations("classpath:db/migration").target("46").load();
            baseline.migrate(); baseline.validate();
            assertThat(com.feelingpilates.transicion.programacion.adapter.jpa.testinfra.F2ePostgresTestConfiguration
                    .catalogoFixtureV46Valido(baseline.info())).isTrue();
            var applied = java.util.Arrays.stream(baseline.info().applied())
                    .map(m -> m.getVersion()+":"+m.getScript()+":"+m.getChecksum()).toList();
            try (var connection = dataSource.getConnection()) {
                schemaAttempt(connection, "SELECT * FROM programacion_ajuste_fecha", "42P01");
                try (var statement = connection.createStatement()) {
                    statement.execute("INSERT INTO usuario(id,correo,nombre) VALUES ('10000000-0000-0000-0000-000000000001','r4-schema@example.invalid','R4 schema')");
                    statement.execute("INSERT INTO salon(id,nombre,estado_id,municipio_id) VALUES ('10000000-0000-0000-0000-000000000002','R4 schema',22,14)");
                    statement.execute("INSERT INTO tipo_actividad(id,nombre) VALUES ('10000000-0000-0000-0000-000000000003','R4 schema')");
                    statement.execute("INSERT INTO programacion_bloque(id,serie_id,salon_id,dia_semana,hora_inicio,hora_fin,vigente_desde) "
                            + "SELECT '10000000-0000-0000-0000-000000000004','10000000-0000-0000-0000-000000000005',id,1,'10:00','12:00','2027-01-01' FROM salon WHERE nombre='R4 schema'");
                    statement.execute("INSERT INTO programacion_asignacion(serie_id,bloque_id,instructor_id,tipo_actividad_id,hora_inicio,hora_fin,vigente_desde) "
                            + "SELECT '10000000-0000-0000-0000-000000000006','10000000-0000-0000-0000-000000000004',"
                            + "'10000000-0000-0000-0000-000000000001','10000000-0000-0000-0000-000000000003','10:00','11:00','2027-01-01' FROM generate_series(1,2)");
                }
                var tables = schemaStrings(connection, "SELECT tablename FROM pg_tables WHERE schemaname='public' AND tablename <> 'flyway_schema_history' ORDER BY tablename");
                var before = oldSchemaSnapshot(connection, tables);
                var extensions = schemaStrings(connection, "SELECT extname||':'||extversion FROM pg_extension ORDER BY extname");
                var latest = Flyway.configure().dataSource(dataSource).locations("classpath:db/migration").load();
                assertThat(latest.migrate().migrationsExecuted).isEqualTo(1); latest.validate();
                assertThat(latest.info().current().getVersion().getVersion()).isEqualTo("47");
                assertThat(latest.info().applied()).hasSize(50); assertThat(latest.info().pending()).isEmpty();
                var installed = schemaStrings(connection, "SELECT script||':'||checksum::text FROM flyway_schema_history WHERE version='47' AND success");
                assertThat(installed).hasSize(1);
                assertThat(installed.get(0)).startsWith("V47__programacion_ajuste_fecha.sql:");
                System.out.println("R4_SCHEMA_INSTALLED_FLYWAY=" + installed.get(0));
                assertThat(java.util.Arrays.stream(latest.info().applied()).filter(m -> !"47".equals(m.getVersion().getVersion()))
                        .map(m -> m.getVersion()+":"+m.getScript()+":"+m.getChecksum()).toList()).containsExactlyElementsOf(applied);
                assertThat(latest.migrate().migrationsExecuted).isZero(); latest.validate();
                assertThat(oldSchemaSnapshot(connection, tables)).containsExactlyElementsOf(before);
                assertThat(schemaStrings(connection, "SELECT extname||':'||extversion FROM pg_extension ORDER BY extname")).containsExactlyElementsOf(extensions);
                assertThat(schemaStrings(connection, "SELECT count(*)::text FROM programacion_ajuste_fecha")).containsExactly("0");
                assertThat(schemaStrings(connection, "SELECT count(*)::text FROM pg_constraint WHERE conrelid='programacion_asignacion'::regclass AND contype='x'")).containsExactly("0");
                var addition = adjustmentValues(connection, "ADICION");
                addition[4] = "'10000000-0000-0000-0000-000000000002'";
                addition[5] = "'10000000-0000-0000-0000-000000000001'";
                addition[6] = "'10000000-0000-0000-0000-000000000003'";
                try (var statement = connection.createStatement()) { statement.execute(adjustmentInsert(addition)); }
                for (String master : java.util.List.of("salon", "usuario", "tipo_actividad")) {
                    String id = master.equals("salon") ? addition[4] : master.equals("usuario") ? addition[5] : addition[6];
                    schemaAttempt(connection, "DELETE FROM " + master + " WHERE id=" + id, "23503");
                    schemaAttempt(connection, "UPDATE " + master + " SET id='" + UUID.randomUUID() + "' WHERE id=" + id, "23503");
                }
            }
            concurrentActiveTarget(dataSource);
        }
    }

    private static java.util.List<String> oldSchemaSnapshot(java.sql.Connection connection, java.util.List<String> tables) throws Exception {
        var result = new java.util.ArrayList<String>();
        for (String table : tables) {
            String name = "public.\"" + table.replace("\"", "\"\"") + "\"";
            result.addAll(schemaStrings(connection, "SELECT row_to_json(t)::text FROM " + name + " t ORDER BY row_to_json(t)::text"));
            result.addAll(schemaStrings(connection, "SELECT attname||':'||format_type(atttypid,atttypmod)||':'||attnotnull||':'||COALESCE(pg_get_expr(d.adbin,d.adrelid),'') "
                    + "FROM pg_attribute a LEFT JOIN pg_attrdef d ON d.adrelid=a.attrelid AND d.adnum=a.attnum WHERE a.attrelid='" + name + "'::regclass AND a.attnum>0 AND NOT a.attisdropped ORDER BY a.attnum"));
            result.addAll(schemaStrings(connection, "SELECT conname||':'||pg_get_constraintdef(oid) FROM pg_constraint WHERE conrelid='" + name + "'::regclass ORDER BY conname"));
            result.addAll(schemaStrings(connection, "SELECT indexdef FROM pg_indexes WHERE schemaname='public' AND tablename='" + table + "' ORDER BY indexname"));
        }
        return result;
    }

    private static void concurrentActiveTarget(javax.sql.DataSource dataSource) throws Exception {
        var executor = java.util.concurrent.Executors.newSingleThreadExecutor();
        try (var first = dataSource.getConnection(); var observer = dataSource.getConnection()) {
            first.setAutoCommit(false);
            var values = adjustmentValues(first, "CANCELACION");
            try (var statement = first.createStatement()) { statement.execute(adjustmentInsert(values)); }
            var competing = values.clone(); competing[0] = "'" + UUID.randomUUID() + "'";
            var pid = new java.util.concurrent.CompletableFuture<String>();
            var future = executor.submit(() -> {
                try (var second = dataSource.getConnection()) {
                    second.setAutoCommit(false);
                    try (var statement = second.createStatement()) {
                        statement.execute("SET LOCAL statement_timeout='8s'");
                        pid.complete(schemaStrings(second, "SELECT pg_backend_pid()::text").get(0));
                        try { statement.execute(adjustmentInsert(competing)); second.commit(); return "COMMITTED"; }
                        catch (java.sql.SQLException error) { second.rollback(); return error.getSQLState(); }
                    }
                }
            });
            String backend = pid.get(5, java.util.concurrent.TimeUnit.SECONDS);
            long deadline = System.nanoTime() + java.util.concurrent.TimeUnit.SECONDS.toNanos(5);
            boolean blocked = false;
            while (System.nanoTime() < deadline) {
                blocked = schemaStrings(observer, "SELECT COALESCE(wait_event_type,'') FROM pg_stat_activity WHERE pid=" + backend).equals(java.util.List.of("Lock"));
                if (blocked) break;
                Thread.sleep(10);
            }
            assertThat(blocked).as("conflicting insert observed waiting for unique-index transaction lock").isTrue();
            first.commit();
            assertThat(future.get(5, java.util.concurrent.TimeUnit.SECONDS)).isEqualTo("23505");
            assertThat(schemaStrings(observer, "SELECT count(*)::text FROM programacion_ajuste_fecha WHERE asignacion_serie_id=" + values[3])).containsExactly("1");
        } finally { executor.shutdownNow(); }
    }

    private static String[] adjustmentValues(java.sql.Connection connection, String tipo) throws Exception {
        return new String[]{"'"+UUID.randomUUID()+"'", "'"+tipo+"'", "'2027-01-04'",
                tipo.equals("ADICION") ? "NULL" : "'"+UUID.randomUUID()+"'",
                tipo.equals("CANCELACION") ? "NULL" : "'"+schemaStrings(connection,"SELECT id::text FROM salon ORDER BY id LIMIT 1").get(0)+"'",
                tipo.equals("CANCELACION") ? "NULL" : "'"+schemaStrings(connection,"SELECT id::text FROM usuario ORDER BY id LIMIT 1").get(0)+"'",
                tipo.equals("CANCELACION") ? "NULL" : "'"+schemaStrings(connection,"SELECT id::text FROM tipo_actividad ORDER BY id LIMIT 1").get(0)+"'",
                tipo.equals("CANCELACION") ? "NULL" : "'10:00:00.123456'",
                tipo.equals("CANCELACION") ? "NULL" : "'11:00:00.654321'", "true",
                "'2027-01-02 03:04:05.123456+00'", "'2026-01-01 02:03:04.654321+00'"};
    }

    private static String adjustmentInsert(String[] values) {
        return "INSERT INTO programacion_ajuste_fecha(id,tipo,fecha,asignacion_serie_id,salon_resultado_id,"
                + "instructor_resultado_id,tipo_actividad_resultado_id,hora_inicio_resultado,hora_fin_resultado,activo,creado_en,actualizado_en) VALUES ("
                + String.join(",", values) + ")";
    }

    private static void schemaAttempt(java.sql.Connection connection, String sql, String expectedState) throws Exception {
        boolean ownTransaction = connection.getAutoCommit();
        if (ownTransaction) connection.setAutoCommit(false);
        var savepoint = connection.setSavepoint();
        try (var statement = connection.createStatement()) {
            try { statement.execute(sql); assertThat(expectedState).as(sql).isNull(); }
            catch (java.sql.SQLException error) { assertThat(error.getSQLState()).as(sql).isEqualTo(expectedState); }
        } finally {
            connection.rollback(savepoint); connection.releaseSavepoint(savepoint);
            if (ownTransaction) { connection.rollback(); connection.setAutoCommit(true); }
        }
    }

    private static java.util.List<String> schemaStrings(java.sql.Connection connection, String sql) throws Exception {
        try (var statement = connection.createStatement(); var rows = statement.executeQuery(sql)) {
            var values = new java.util.ArrayList<String>();
            while (rows.next()) values.add(rows.getString(1));
            return values;
        }
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
