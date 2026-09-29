package com.feelingpilates.transicion.programacion.adapter.jpa.testinfra;

import com.feelingpilates.transicion.programacion.adapter.jpa.LegacyTurnJpaReader;
import com.feelingpilates.transicion.programacion.adapter.jpa.mapper.LegacyTurnProjectionMapper;
import com.feelingpilates.transicion.programacion.adapter.jpa.projection.LegacyTurnProjectionCatalog;
import com.feelingpilates.transicion.programacion.adapter.jpa.projection.LegacyTurnProjectionQueryExecutor;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.flywaydb.core.Flyway;
import org.hibernate.Session;
import org.postgresql.ds.PGSimpleDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.SharedEntityManagerCreator;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@TestConfiguration(proxyBeanMethods = false)
@EnableTransactionManagement
@EnableAspectJAutoProxy
public class LegacyTurnR2PostgresTestConfiguration {

    public static final LocalDate FECHA = LocalDate.of(2026, 9, 28);
    public static final UUID TURNO_RECURRENTE = UUID.fromString("20000000-0000-4000-8000-000000000001");
    public static final UUID TURNO_EXCEPCION = UUID.fromString("20000000-0000-4000-8000-000000000002");
    public static final UUID TURNO_CANCELACION = UUID.fromString("20000000-0000-4000-8000-000000000003");

    private F2eSelectOnlyRole role;
    private UUID salonId;
    private UUID userId;
    private UUID activityId;
    private String schemaFingerprint;
    private int appliedMigrations;
    private String flywayHead;

    @Bean(destroyMethod = "stop")
    PostgreSQLContainer<?> f2eR2PostgresContainer() {
        PostgreSQLContainer<?> container = new PostgreSQLContainer<>(DockerImageName.parse("postgres:16-alpine"));
        container.start();
        return container;
    }

    @Bean(name = "f2eR2PrivilegedDataSource")
    DataSource f2eR2PrivilegedDataSource(PostgreSQLContainer<?> f2eR2PostgresContainer) {
        PGSimpleDataSource dataSource = new PGSimpleDataSource();
        dataSource.setURL(f2eR2PostgresContainer.getJdbcUrl());
        dataSource.setUser(f2eR2PostgresContainer.getUsername());
        dataSource.setPassword(f2eR2PostgresContainer.getPassword());
        Flyway flyway = Flyway.configure().dataSource(dataSource).locations("classpath:db/migration").load();
        flyway.migrate();
        flyway.validate();
        var info = flyway.info();
        flywayHead = info.current() == null ? null : info.current().getVersion().getVersion();
        appliedMigrations = info.applied().length;
        if (!"46".equals(flywayHead) || appliedMigrations != 49 || info.pending().length != 0
                || java.util.Arrays.stream(info.all()).anyMatch(
                migration -> migration.getState() != org.flywaydb.core.api.MigrationState.SUCCESS)) {
            throw new IllegalStateException("R2 Flyway V1-V46 validation not proven");
        }
        insertFixtures(dataSource);
        schemaFingerprint = F2ePostgresTestConfiguration.calcularHuellaEsquema(dataSource);
        role = F2eSelectOnlyRole.crear(dataSource, "f2e_r2_reader_", "turno-a", Set.of(
                "public.turno_instructor",
                "public.turno_instructor_usuario",
                "public.turno_instructor_asignacion"));
        return dataSource;
    }

    @Bean(name = "legacyTurnJdbcCapture")
    LegacyTurnJdbcCapture legacyTurnJdbcCapture() {
        return new LegacyTurnJdbcCapture();
    }

    @Bean(name = "f2eR2ReaderDataSource")
    DataSource f2eR2ReaderDataSource(
            @Qualifier("f2eR2PrivilegedDataSource") DataSource privileged,
            PostgreSQLContainer<?> container,
            @Qualifier("legacyTurnJdbcCapture") LegacyTurnJdbcCapture capture) {
        Objects.requireNonNull(privileged, "privileged");
        return capture.wrap(role.crearDataSource(canonicalUrl(container)));
    }

    @Bean(name = "f2eR2StatementPolicyInspector")
    F2eStatementPolicyInspector f2eR2StatementPolicyInspector() {
        return F2eStatementPolicyInspector.paraR2(LegacyTurnProjectionCatalog.R2_LEGACY_TURN_V1);
    }

    @Bean(name = "f2eR2ReaderEntityManagerFactory", destroyMethod = "close")
    EntityManagerFactory f2eR2ReaderEntityManagerFactory(
            @Qualifier("f2eR2ReaderDataSource") DataSource readerDataSource,
            @Qualifier("f2eR2StatementPolicyInspector") F2eStatementPolicyInspector inspector) {
        LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
        factory.setDataSource(readerDataSource);
        factory.setPersistenceUnitName("f2eR2ReaderPersistenceUnit");
        factory.setPackagesToScan("com.feelingpilates");
        factory.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto", "validate");
        properties.put("hibernate.default_schema", "public");
        properties.put("hibernate.session_factory.statement_inspector", inspector);
        properties.put("hibernate.show_sql", false);
        factory.setJpaPropertyMap(properties);
        factory.afterPropertiesSet();
        return Objects.requireNonNull(factory.getObject(), "f2eR2ReaderEntityManagerFactory");
    }

    @Bean(name = "f2eR2ReaderEntityManager")
    EntityManager f2eR2ReaderEntityManager(
            @Qualifier("f2eR2ReaderEntityManagerFactory") EntityManagerFactory factory) {
        return SharedEntityManagerCreator.createSharedEntityManager(factory);
    }

    @Bean(name = "f2eR2ReaderTransactionManager")
    JpaTransactionManager f2eR2ReaderTransactionManager(
            @Qualifier("f2eR2ReaderEntityManagerFactory") EntityManagerFactory factory,
            @Qualifier("f2eR2ReaderDataSource") DataSource readerDataSource) {
        JpaTransactionManager manager = new JpaTransactionManager(factory);
        manager.setDataSource(readerDataSource);
        return manager;
    }

    @Bean(name = "legacyTurnProjectionQueryExecutor")
    LegacyTurnProjectionQueryExecutor legacyTurnProjectionQueryExecutor(
            @Qualifier("f2eR2ReaderEntityManager") EntityManager entityManager) {
        return new LegacyTurnProjectionQueryExecutor(
                entityManager, LegacyTurnProjectionCatalog.R2_LEGACY_TURN_V1);
    }

    @Bean(name = "legacyTurnProjectionMapper")
    LegacyTurnProjectionMapper legacyTurnProjectionMapper() {
        return new LegacyTurnProjectionMapper(LegacyTurnProjectionCatalog.R2_LEGACY_TURN_V1);
    }

    @Bean(name = "legacyTurnJpaReader")
    LegacyTurnJpaReader legacyTurnJpaReader(
            @Qualifier("legacyTurnProjectionQueryExecutor") LegacyTurnProjectionQueryExecutor executor,
            @Qualifier("legacyTurnProjectionMapper") LegacyTurnProjectionMapper mapper,
            @Qualifier("legacyTurnContextRegistry") LegacyTurnTransactionTestOwner.ContextRegistry contexts) {
        return new LegacyTurnJpaReader(executor, mapper, LegacyTurnProjectionCatalog.R2_LEGACY_TURN_V1,
                contexts);
    }

    @Bean(name = "legacyTurnContextRegistry")
    LegacyTurnTransactionTestOwner.ContextRegistry legacyTurnContextRegistry() {
        return new LegacyTurnTransactionTestOwner.ContextRegistry();
    }

    @Bean(name = "legacyTurnTransactionTestOwner")
    LegacyTurnTransactionTestOwner legacyTurnTransactionTestOwner(
            @Qualifier("legacyTurnJpaReader") com.feelingpilates.transicion.programacion.read.LegacyTurnReadPort reader,
            @Qualifier("legacyTurnProjectionQueryExecutor") LegacyTurnProjectionQueryExecutor executor,
            @Qualifier("legacyTurnProjectionMapper") LegacyTurnProjectionMapper mapper,
            @Qualifier("f2eR2StatementPolicyInspector") F2eStatementPolicyInspector inspector,
            @Qualifier("legacyTurnJdbcCapture") LegacyTurnJdbcCapture jdbcCapture,
            @Qualifier("legacyTurnContextRegistry") LegacyTurnTransactionTestOwner.ContextRegistry contexts,
            @Qualifier("f2eR2ReaderDataSource") DataSource readerDataSource,
            @Qualifier("f2eR2ReaderEntityManagerFactory") EntityManagerFactory factory,
            @Qualifier("f2eR2ReaderTransactionManager") JpaTransactionManager manager,
            @Qualifier("f2eR2ReaderEntityManager") EntityManager entityManager,
            PostgreSQLContainer<?> container) {
        Descriptor descriptor = new Descriptor(
                "turno-a", "fixture-r2-turno-a", "fixture:postgres16:r2:turno-a",
                schemaFingerprint, canonicalUrl(container), container.getDatabaseName(), "public",
                role.principal(), readerDataSource, factory, manager, entityManager, inspector);
        return new LegacyTurnTransactionTestOwner(
                reader, executor, mapper, inspector, jdbcCapture, contexts, descriptor);
    }

    public UUID salonId() { return salonId; }
    public UUID userId() { return userId; }
    public UUID activityId() { return activityId; }
    public String schemaFingerprint() { return schemaFingerprint; }
    public int appliedMigrations() { return appliedMigrations; }
    public String flywayHead() { return flywayHead; }
    public String readerPrincipal() { return role.principal(); }

    private void insertFixtures(DataSource dataSource) {
        try (var connection = dataSource.getConnection()) {
            salonId = firstUuid(connection, "SELECT id FROM public.salon ORDER BY id LIMIT 1");
            userId = firstUuid(connection, "SELECT id FROM public.usuario ORDER BY id LIMIT 1");
            activityId = firstUuid(connection, "SELECT id FROM public.tipo_actividad ORDER BY id LIMIT 1");
            try (var statement = connection.prepareStatement(
                    "INSERT INTO public.turno_instructor "
                            + "(id,salon_id,tipo,dia_semana,fecha,hora_inicio,hora_fin,activo,creado_en,actualizado_en) "
                            + "VALUES (?,?,?,?,?,?,?,?,?,?) ON CONFLICT (id) DO NOTHING")) {
                insertTurn(statement, TURNO_RECURRENTE, "RECURRENTE", (short) 1, null,
                        LocalTime.of(8, 0), LocalTime.of(12, 0));
                insertTurn(statement, TURNO_EXCEPCION, "EXCEPCION", null, FECHA,
                        LocalTime.of(13, 0), LocalTime.of(14, 0));
                insertTurn(statement, TURNO_CANCELACION, "CANCELACION", null, FECHA,
                        LocalTime.of(15, 0), LocalTime.of(16, 0));
            }
            try (var member = connection.prepareStatement(
                    "INSERT INTO public.turno_instructor_usuario(turno_id,usuario_id) VALUES (?,?) ON CONFLICT DO NOTHING")) {
                member.setObject(1, TURNO_RECURRENTE);
                member.setObject(2, userId);
                member.executeUpdate();
                member.setObject(1, TURNO_CANCELACION);
                member.executeUpdate();
            }
            try (var assignment = connection.prepareStatement(
                    "INSERT INTO public.turno_instructor_asignacion"
                            + "(turno_id,usuario_id,tipo_actividad_id,hora_inicio,hora_fin) "
                            + "VALUES (?,?,?,?,?) ON CONFLICT DO NOTHING")) {
                assignment.setObject(1, TURNO_RECURRENTE);
                assignment.setObject(2, userId);
                assignment.setObject(3, activityId);
                assignment.setObject(4, LocalTime.of(9, 0));
                assignment.setObject(5, LocalTime.of(10, 0));
                assignment.executeUpdate();
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to create R2 Turno fixtures", exception);
        }
    }

    private void insertTurn(
            java.sql.PreparedStatement statement, UUID id, String type, Short day, LocalDate date,
            LocalTime start, LocalTime end) throws SQLException {
        statement.setObject(1, id);
        statement.setObject(2, salonId);
        statement.setString(3, type);
        if (day == null) statement.setNull(4, java.sql.Types.SMALLINT); else statement.setShort(4, day);
        statement.setObject(5, date);
        statement.setObject(6, start);
        statement.setObject(7, end);
        statement.setBoolean(8, true);
        statement.setObject(9, OffsetDateTime.of(2026, 9, 1, 12, 0, 0, 0, ZoneOffset.UTC));
        statement.setObject(10, OffsetDateTime.of(2026, 9, 2, 12, 0, 0, 0, ZoneOffset.UTC));
        statement.executeUpdate();
    }

    private UUID firstUuid(java.sql.Connection connection, String sql) throws SQLException {
        try (var statement = connection.createStatement(); var rows = statement.executeQuery(sql)) {
            if (!rows.next()) throw new IllegalStateException("Required R2 fixture catalog is empty");
            return rows.getObject(1, UUID.class);
        }
    }

    private String canonicalUrl(PostgreSQLContainer<?> container) {
        return "jdbc:postgresql://" + container.getHost().toLowerCase(java.util.Locale.ROOT)
                + ':' + container.getMappedPort(5432) + '/' + container.getDatabaseName();
    }

    public record Descriptor(
            String fixtureKey,
            String fixtureIdentity,
            String sourceName,
            String schemaFingerprint,
            String canonicalJdbcUrl,
            String databaseName,
            String schemaName,
            String credentialPrincipal,
            DataSource readerDataSource,
            EntityManagerFactory entityManagerFactory,
            JpaTransactionManager transactionManager,
            EntityManager entityManager,
            F2eStatementPolicyInspector inspector) { }
}
