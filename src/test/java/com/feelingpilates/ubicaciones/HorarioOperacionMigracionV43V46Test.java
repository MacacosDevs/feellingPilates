package com.feelingpilates.ubicaciones;

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
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Prueba de migracion REAL y por etapas (V43..V46, F2B.3a) usando Flyway contra un
 * PostgreSQL efimero propio, sin contexto de Spring. Demuestra la transicion fisica
 * de horario_operacion de UNIQUE(salon_id, dia_semana) a EXCLUDE de vigencias,
 * inspeccionando pg_extension/pg_constraint y probando insercion real en cada etapa
 * en vez de asumir el efecto por el nombre de la migracion.
 */
@Testcontainers
class HorarioOperacionMigracionV43V46Test {

    @Container
    private static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres:16-alpine"));

    private static final String UNIQUE_CONSTRAINT = "horario_operacion_salon_id_dia_semana_key";
    private static final String EXCLUDE_CONSTRAINT = "ex_horario_operacion_vigencia";

    @Test
    void migracionRealV43AV46TransicionaDeUniqueAExclude() throws Exception {
        migrarHasta("43");

        try (Connection connection = conectar()) {
            UUID salonId = obtenerPrimerSalonId(connection);

            // ---------- Estado en V43 ----------
            assertThat(extensionExiste(connection, "btree_gist")).isFalse();
            assertThat(constraintExiste(connection, UNIQUE_CONSTRAINT)).isTrue();
            assertThat(constraintExiste(connection, EXCLUDE_CONSTRAINT)).isFalse();

            short diaV43 = 1;
            insertarHorario(connection, salonId, diaV43, null, null);
            assertThatThrownBy(() -> insertarHorario(connection, salonId, diaV43, null, null))
                    .hasMessageContaining(UNIQUE_CONSTRAINT);

            // ---------- Estado en V44 ----------
            migrarHasta("44");

            assertThat(extensionExiste(connection, "btree_gist")).isTrue();
            assertThat(constraintExiste(connection, UNIQUE_CONSTRAINT)).isTrue();
            assertThat(constraintExiste(connection, EXCLUDE_CONSTRAINT)).isFalse();

            // ---------- Estado en V45 (coexistencia: sin ventana sin garantia) ----------
            migrarHasta("45");

            assertThat(extensionExiste(connection, "btree_gist")).isTrue();
            assertThat(constraintExiste(connection, UNIQUE_CONSTRAINT)).isTrue();
            assertThat(constraintExiste(connection, EXCLUDE_CONSTRAINT)).isTrue();

            short diaV45 = 2;
            insertarHorario(connection, salonId, diaV45, null, null);
            assertThatThrownBy(() -> insertarHorario(connection, salonId, diaV45, null, null))
                    .isInstanceOf(Exception.class);

            // ---------- Estado en V46 ----------
            migrarHasta("46");

            assertThat(extensionExiste(connection, "btree_gist")).isTrue();
            assertThat(constraintExiste(connection, UNIQUE_CONSTRAINT)).isFalse();
            assertThat(constraintExiste(connection, EXCLUDE_CONSTRAINT)).isTrue();

            short diaV46 = 3;
            insertarHorario(connection, salonId, diaV46, null, LocalDate.of(2026, 8, 31));
            insertarHorario(connection, salonId, diaV46, LocalDate.of(2026, 9, 1), null);

            assertThat(contarFilas(connection, salonId, diaV46)).isEqualTo(2);
        }
    }

    private static void migrarHasta(String version) {
        Flyway.configure()
                .dataSource(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword())
                .target(version)
                .load()
                .migrate();
    }

    private static Connection conectar() throws Exception {
        return DriverManager.getConnection(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword());
    }

    private static UUID obtenerPrimerSalonId(Connection connection) throws Exception {
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery("select id from salon limit 1")) {
            assertThat(rs.next()).isTrue();
            return (UUID) rs.getObject("id");
        }
    }

    private static void insertarHorario(Connection connection, UUID salonId, short diaSemana,
            LocalDate desde, LocalDate hasta) throws Exception {
        try (PreparedStatement insert = connection.prepareStatement("""
                insert into horario_operacion (id, salon_id, dia_semana, hora_apertura, hora_cierre, vigente_desde, vigente_hasta)
                values (?, ?, ?, '08:00', '20:00', ?, ?)
                """)) {
            insert.setObject(1, UUID.randomUUID());
            insert.setObject(2, salonId);
            insert.setShort(3, diaSemana);
            setFechaOrNull(insert, 4, desde);
            setFechaOrNull(insert, 5, hasta);
            insert.executeUpdate();
        }
    }

    private static void setFechaOrNull(PreparedStatement ps, int index, LocalDate fecha) throws Exception {
        if (fecha == null) {
            ps.setNull(index, Types.DATE);
        } else {
            ps.setObject(index, fecha);
        }
    }

    private static int contarFilas(Connection connection, UUID salonId, short diaSemana) throws Exception {
        try (PreparedStatement select = connection.prepareStatement(
                "select count(*) from horario_operacion where salon_id = ? and dia_semana = ?")) {
            select.setObject(1, salonId);
            select.setShort(2, diaSemana);
            try (ResultSet rs = select.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    private static boolean extensionExiste(Connection connection, String nombre) throws Exception {
        try (PreparedStatement ps = connection.prepareStatement(
                "select 1 from pg_extension where extname = ?")) {
            ps.setString(1, nombre);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private static boolean constraintExiste(Connection connection, String nombre) throws Exception {
        try (PreparedStatement ps = connection.prepareStatement(
                "select 1 from pg_constraint where conname = ?")) {
            ps.setString(1, nombre);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }
}
