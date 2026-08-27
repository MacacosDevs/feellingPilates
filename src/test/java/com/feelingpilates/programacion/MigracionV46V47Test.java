package com.feelingpilates.programacion;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Testcontainers
class MigracionV46V47Test {

    @Container
    private static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres:16-alpine"));

    @Test
    void v47PreauditaSinRepararYLuegoMaterializaConstraintsAprobadas() throws Exception {
        migrarHasta("46");
        try (Connection connection = conectar()) {
            UUID bloque = insertarBloque(connection);
            UUID serie = UUID.randomUUID();
            UUID instructor = primerUuid(connection, "usuario");
            UUID actividad = primerUuid(connection, "tipo_actividad");
            UUID a = insertarAsignacion(connection, bloque, serie, instructor, actividad,
                    LocalDate.of(2027, 1, 1), LocalDate.of(2027, 1, 31), true);
            UUID b = insertarAsignacion(connection, bloque, serie, instructor, actividad,
                    LocalDate.of(2027, 1, 15), null, true);

            assertThatThrownBy(() -> migrarHasta("47"))
                    .hasMessageContaining("vigencias activas solapadas");
            assertThat(contar(connection, "programacion_asignacion", a, b)).isEqualTo(2);
            assertThat(constraintExiste(connection, "ex_programacion_asignacion_serie_vigencia"))
                    .isFalse();

            try (PreparedStatement ps = connection.prepareStatement(
                    "delete from programacion_asignacion where id = ?")) {
                ps.setObject(1, b);
                ps.executeUpdate();
            }
            migrarHasta("47");

            assertThat(constraintTipo(connection, "programacion_ajuste_fecha_pkey")).isEqualTo("p");
            assertThat(constraintTipo(connection, "ex_programacion_asignacion_serie_vigencia"))
                    .isEqualTo("x");
            assertThat(constraintExiste(connection, "chk_programacion_ajuste_tipo")).isTrue();
            assertThat(constraintDefinicion(connection, "chk_programacion_ajuste_tipo"))
                    .contains("CANCELACION", "REEMPLAZO", "ADICION");
            assertThat(constraintExiste(connection, "chk_programacion_ajuste_forma")).isTrue();
            assertThat(constraintExiste(connection, "chk_programacion_ajuste_rango")).isTrue();
            assertThat(indiceExiste(connection, "idx_programacion_ajuste_target_activo")).isTrue();
            assertThat(indiceExiste(connection, "idx_programacion_ajuste_salon_fecha_activo"))
                    .isTrue();
            assertThat(indiceExiste(connection, "idx_programacion_ajuste_instructor_fecha_activo"))
                    .isTrue();
            assertThat(indiceExiste(connection, "idx_programacion_ajuste_fecha_activo")).isTrue();
            assertThat(fkSerieExiste(connection)).isFalse();
            assertThat(fksResultado(connection)).isEqualTo(3);

            insertarAsignacion(connection, bloque, serie, instructor, actividad,
                    LocalDate.of(2027, 2, 1), null, true);
            assertThatThrownBy(() -> insertarAsignacion(connection, bloque, serie, instructor,
                    actividad, LocalDate.of(2027, 1, 31), null, true))
                    .hasMessageContaining("ex_programacion_asignacion_serie_vigencia");

            UUID salon = primerUuid(connection, "salon");
            UUID targetSerie = UUID.randomUUID();
            insertarAjuste(connection, UUID.randomUUID(), "CANCELACION", targetSerie,
                    LocalDate.of(2027, 3, 1), null, null, null, null, null, true);
            assertThatThrownBy(() -> insertarAjuste(connection, UUID.randomUUID(), "REEMPLAZO",
                    targetSerie, LocalDate.of(2027, 3, 1), salon, instructor, actividad,
                    "10:00", "11:00", true))
                    .hasMessageContaining("idx_programacion_ajuste_target_activo");

            // El unique es parcial: el historial inactivo no bloquea otro target activo.
            insertarAjuste(connection, UUID.randomUUID(), "REEMPLAZO", targetSerie,
                    LocalDate.of(2027, 3, 1), salon, instructor, actividad,
                    "10:00", "11:00", false);

            assertThatThrownBy(() -> insertarAjuste(connection, UUID.randomUUID(), "CANCELACION",
                    UUID.randomUUID(), LocalDate.of(2027, 3, 3), salon, null, null,
                    null, null, true))
                    .hasMessageContaining("chk_programacion_ajuste_forma");

            assertThatThrownBy(() -> insertarAjuste(connection, UUID.randomUUID(), "ADICION",
                    null, LocalDate.of(2027, 3, 2), salon, instructor, actividad,
                    "11:00", "10:00", true))
                    .hasMessageContaining("chk_programacion_ajuste_rango");

            // Un tipo desconocido viola simultáneamente los CHECKs de tipo y forma; PostgreSQL no
            // garantiza cuál reporta primero. La definición física anterior aísla el contrato de
            // tipo sin depender de ese orden, y los casos siguientes ejercitan forma por separado.
            assertThatThrownBy(() -> insertarAjuste(connection, UUID.randomUUID(), "OTRO",
                    null, LocalDate.of(2027, 3, 4), salon, instructor, actividad,
                    "10:00", "11:00", true))
                    .satisfies(error -> assertThat(error.getMessage())
                            .containsAnyOf(
                                    "chk_programacion_ajuste_tipo",
                                    "chk_programacion_ajuste_forma"));
            assertThatThrownBy(() -> insertarAjuste(connection, UUID.randomUUID(), "CANCELACION",
                    UUID.randomUUID(), LocalDate.of(2027, 3, 5), salon, instructor, actividad,
                    "10:00", "11:00", true))
                    .hasMessageContaining("chk_programacion_ajuste_forma");
            assertThatThrownBy(() -> insertarAjuste(connection, UUID.randomUUID(), "REEMPLAZO",
                    UUID.randomUUID(), LocalDate.of(2027, 3, 6), null, null, null,
                    null, null, true))
                    .hasMessageContaining("chk_programacion_ajuste_forma");
            assertThatThrownBy(() -> insertarAjuste(connection, UUID.randomUUID(), "ADICION",
                    UUID.randomUUID(), LocalDate.of(2027, 3, 7), salon, instructor, actividad,
                    "10:00", "11:00", true))
                    .hasMessageContaining("chk_programacion_ajuste_forma");
            assertThatThrownBy(() -> insertarAjuste(connection, UUID.randomUUID(), "ADICION",
                    null, LocalDate.of(2027, 3, 8), salon, instructor, actividad,
                    "10:00", "10:00", true))
                    .hasMessageContaining("chk_programacion_ajuste_rango");
        }
    }

    private static void migrarHasta(String version) {
        Flyway.configure()
                .dataSource(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword())
                .target(version)
                .load()
                .migrate();
    }

    private static Connection conectar() throws SQLException {
        return DriverManager.getConnection(
                POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword());
    }

    private static UUID insertarBloque(Connection c) throws SQLException {
        UUID id = UUID.randomUUID();
        try (PreparedStatement ps = c.prepareStatement("""
                insert into programacion_bloque
                    (id, serie_id, salon_id, dia_semana, hora_inicio, hora_fin,
                     vigente_desde, vigente_hasta, activo)
                values (?, ?, (select id from salon limit 1), 1, '08:00', '20:00',
                        '2027-01-01', null, true)
                """)) {
            ps.setObject(1, id);
            ps.setObject(2, UUID.randomUUID());
            ps.executeUpdate();
        }
        return id;
    }

    private static UUID insertarAsignacion(
            Connection c, UUID bloque, UUID serie, UUID instructor, UUID actividad,
            LocalDate desde, LocalDate hasta, boolean activo) throws SQLException {
        UUID id = UUID.randomUUID();
        try (PreparedStatement ps = c.prepareStatement("""
                insert into programacion_asignacion
                    (id, serie_id, bloque_id, instructor_id, tipo_actividad_id,
                     hora_inicio, hora_fin, vigente_desde, vigente_hasta, activo)
                values (?, ?, ?, ?, ?, '10:00', '11:00', ?, ?, ?)
                """)) {
            ps.setObject(1, id);
            ps.setObject(2, serie);
            ps.setObject(3, bloque);
            ps.setObject(4, instructor);
            ps.setObject(5, actividad);
            ps.setObject(6, desde);
            ps.setObject(7, hasta);
            ps.setBoolean(8, activo);
            ps.executeUpdate();
        }
        return id;
    }

    private static void insertarAjuste(
            Connection c, UUID id, String tipo, UUID serie, LocalDate fecha,
            UUID salon, UUID instructor, UUID actividad, String inicio, String fin,
            boolean activo) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("""
                insert into programacion_ajuste_fecha
                    (id, tipo, fecha, asignacion_serie_id, salon_resultado_id,
                     instructor_resultado_id, tipo_actividad_resultado_id,
                     hora_inicio_resultado, hora_fin_resultado, activo)
                values (?, ?, ?, ?, ?, ?, ?, cast(? as time), cast(? as time), ?)
                """)) {
            ps.setObject(1, id);
            ps.setString(2, tipo);
            ps.setObject(3, fecha);
            ps.setObject(4, serie);
            ps.setObject(5, salon);
            ps.setObject(6, instructor);
            ps.setObject(7, actividad);
            ps.setString(8, inicio);
            ps.setString(9, fin);
            ps.setBoolean(10, activo);
            ps.executeUpdate();
        }
    }

    private static UUID primerUuid(Connection c, String tabla) throws SQLException {
        try (Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("select id from " + tabla + " limit 1")) {
            rs.next();
            return (UUID) rs.getObject(1);
        }
    }

    private static int contar(Connection c, String tabla, UUID a, UUID b) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "select count(*) from " + tabla + " where id in (?, ?)")) {
            ps.setObject(1, a);
            ps.setObject(2, b);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    private static boolean constraintExiste(Connection c, String nombre) throws SQLException {
        return constraintTipo(c, nombre) != null;
    }

    private static String constraintTipo(Connection c, String nombre) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "select contype::text from pg_constraint where conname = ?")) {
            ps.setString(1, nombre);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString(1) : null;
            }
        }
    }

    private static String constraintDefinicion(Connection c, String nombre) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "select pg_get_constraintdef(oid) from pg_constraint where conname = ?")) {
            ps.setString(1, nombre);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString(1) : null;
            }
        }
    }

    private static boolean indiceExiste(Connection c, String nombre) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "select 1 from pg_indexes where indexname = ?")) {
            ps.setString(1, nombre);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private static boolean fkSerieExiste(Connection c) throws SQLException {
        try (Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("""
                     select 1
                     from pg_constraint c
                     join pg_attribute a on a.attrelid = c.conrelid
                                        and a.attnum = any(c.conkey)
                     where c.conrelid = 'programacion_ajuste_fecha'::regclass
                       and c.contype = 'f'
                       and a.attname = 'asignacion_serie_id'
                     """)) {
            return rs.next();
        }
    }

    private static int fksResultado(Connection c) throws SQLException {
        try (Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("""
                     select count(*)
                     from pg_constraint c
                     join pg_attribute a on a.attrelid = c.conrelid
                                        and a.attnum = any(c.conkey)
                     where c.conrelid = 'programacion_ajuste_fecha'::regclass
                       and c.contype = 'f'
                       and a.attname in (
                           'salon_resultado_id',
                           'instructor_resultado_id',
                           'tipo_actividad_resultado_id')
                     """)) {
            rs.next();
            return rs.getInt(1);
        }
    }
}
