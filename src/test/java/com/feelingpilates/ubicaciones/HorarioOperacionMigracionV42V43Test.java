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
import java.time.LocalTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Prueba de migracion REAL y por etapas (V1..V42, luego V42..V43) usando Flyway
 * contra un PostgreSQL efimero propio, sin contexto de Spring. El objetivo es
 * demostrar que una fila de horario_operacion insertada ANTES de que exista V43
 * sobrevive intacta al ALTER TABLE aditivo de V43, con vigencia NULL/NULL.
 */
@Testcontainers
class HorarioOperacionMigracionV42V43Test {

    @Container
    private static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres:16-alpine"));

    @Test
    void migracionRealV42AV43PreservaFilaLegadaConVigenciaNula() throws Exception {
        Flyway flywayHastaV42 = Flyway.configure()
                .dataSource(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword())
                .target("42")
                .load();
        flywayHastaV42.migrate();

        assertThat(flywayHastaV42.info().current().getVersion().getVersion()).isEqualTo("42");

        Flyway flywaySinTarget = Flyway.configure()
                .dataSource(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword())
                .load();
        assertThat(flywaySinTarget.info().pending())
                .extracting(info -> info.getVersion().getVersion())
                .contains("43");

        try (Connection connection = DriverManager.getConnection(
                POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword())) {

            assertThat(columnaExiste(connection, "horario_operacion", "vigente_desde")).isFalse();
            assertThat(columnaExiste(connection, "horario_operacion", "vigente_hasta")).isFalse();

            UUID salonId = obtenerPrimerSalonId(connection);
            UUID horarioId = UUID.randomUUID();
            short diaSemana = 1;
            LocalTime apertura = LocalTime.of(8, 0);
            LocalTime cierre = LocalTime.of(20, 0);

            insertarHorarioLegado(connection, horarioId, salonId, diaSemana, apertura, cierre);

            int totalAntes = contarFilas(connection);

            Flyway flywayHastaV43 = Flyway.configure()
                    .dataSource(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword())
                    .target("43")
                    .load();
            flywayHastaV43.migrate();

            assertThat(flywayHastaV43.info().current().getVersion().getVersion()).isEqualTo("43");

            try (PreparedStatement select = connection.prepareStatement("""
                    select salon_id, dia_semana, hora_apertura, hora_cierre, vigente_desde, vigente_hasta
                    from horario_operacion where id = ?
                    """)) {
                select.setObject(1, horarioId);
                try (ResultSet rs = select.executeQuery()) {
                    assertThat(rs.next()).isTrue();
                    assertThat(rs.getObject("salon_id")).isEqualTo(salonId);
                    assertThat(rs.getShort("dia_semana")).isEqualTo(diaSemana);
                    assertThat(rs.getObject("hora_apertura", LocalTime.class)).isEqualTo(apertura);
                    assertThat(rs.getObject("hora_cierre", LocalTime.class)).isEqualTo(cierre);
                    assertThat(rs.getObject("vigente_desde")).isNull();
                    assertThat(rs.getObject("vigente_hasta")).isNull();
                    assertThat(rs.next()).isFalse();
                }
            }

            int totalDespues = contarFilas(connection);
            assertThat(totalDespues).isEqualTo(totalAntes);
        }
    }

    private static UUID obtenerPrimerSalonId(Connection connection) throws Exception {
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery("select id from salon limit 1")) {
            assertThat(rs.next()).isTrue();
            return (UUID) rs.getObject("id");
        }
    }

    private static void insertarHorarioLegado(Connection connection, UUID id, UUID salonId, short diaSemana,
            LocalTime apertura, LocalTime cierre) throws Exception {
        try (PreparedStatement insert = connection.prepareStatement("""
                insert into horario_operacion (id, salon_id, dia_semana, hora_apertura, hora_cierre)
                values (?, ?, ?, ?, ?)
                """)) {
            insert.setObject(1, id);
            insert.setObject(2, salonId);
            insert.setShort(3, diaSemana);
            insert.setObject(4, apertura);
            insert.setObject(5, cierre);
            insert.executeUpdate();
        }
    }

    private static int contarFilas(Connection connection) throws Exception {
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery("select count(*) from horario_operacion")) {
            assertThat(rs.next()).isTrue();
            return rs.getInt(1);
        }
    }

    private static boolean columnaExiste(Connection connection, String tabla, String columna) throws Exception {
        try (PreparedStatement ps = connection.prepareStatement("""
                select 1 from information_schema.columns
                where table_name = ? and column_name = ?
                """)) {
            ps.setString(1, tabla);
            ps.setString(2, columna);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }
}
