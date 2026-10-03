package com.feelingpilates.transicion.programacion.adapter.jpa.testinfra;

import com.feelingpilates.transicion.programacion.adapter.jpa.NominalJpaReader;
import com.feelingpilates.transicion.programacion.adapter.jpa.mapper.NominalProjectionMapper;
import com.feelingpilates.transicion.programacion.adapter.jpa.projection.NominalProjectionCatalog;
import com.feelingpilates.transicion.programacion.adapter.jpa.projection.NominalProjectionQueryExecutor;
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
@EnableAspectJAutoProxy(exposeProxy = true)
public class NominalPostgresTestConfiguration {

    public static final LocalDate FECHA = LocalDate.of(2026, 9, 28);
    public static final UUID BLOCK = UUID.fromString("30000000-0000-4000-8000-000000000001");
    public static final UUID ASSIGNMENT = UUID.fromString("30000000-0000-4000-8000-000000000002");
    public static final UUID SERIES = UUID.fromString("30000000-0000-4000-8000-000000000003");

    private F2eSelectOnlyRole role;
    private UUID salonId;
    private UUID userId;
    private UUID activityId;
    private String schemaFingerprint;
    private int appliedMigrations;
    private String flywayHead;

    @Bean(destroyMethod = "stop")
    PostgreSQLContainer<?> f2eR3PostgresContainer() {
        PostgreSQLContainer<?> container = new PostgreSQLContainer<>(DockerImageName.parse("postgres:16-alpine"));
        container.start();
        return container;
    }

    @Bean(name = "f2eR3PrivilegedDataSource")
    DataSource f2eR3PrivilegedDataSource(PostgreSQLContainer<?> f2eR3PostgresContainer) {
        PGSimpleDataSource dataSource = new PGSimpleDataSource();
        dataSource.setURL(f2eR3PostgresContainer.getJdbcUrl());
        dataSource.setUser(f2eR3PostgresContainer.getUsername());
        dataSource.setPassword(f2eR3PostgresContainer.getPassword());
        Flyway flyway = Flyway.configure().dataSource(dataSource).locations("classpath:db/migration").load();
        flyway.migrate();
        flyway.validate();
        var info = flyway.info();
        flywayHead = info.current() == null ? null : info.current().getVersion().getVersion();
        appliedMigrations = info.applied().length;
        if (!"46".equals(flywayHead) || appliedMigrations != 49 || info.pending().length != 0
                || java.util.Arrays.stream(info.all()).anyMatch(
                migration -> migration.getState() != org.flywaydb.core.api.MigrationState.SUCCESS)) {
            throw new IllegalStateException("R3 Flyway V1-V46 validation not proven");
        }
        insertFixtures(dataSource);
        schemaFingerprint = F2ePostgresTestConfiguration.calcularHuellaEsquema(dataSource);
        role = F2eSelectOnlyRole.crear(dataSource, "f2e_r3_reader_", "nominal-a", Set.of(
                "public.programacion_asignacion", "public.programacion_bloque"));
        return dataSource;
    }

    @Bean(name = "nominalJdbcCapture")
    NominalTransactionTestOwner.JdbcCapture nominalJdbcCapture() {
        return new NominalTransactionTestOwner.JdbcCapture();
    }

    @Bean(name = "f2eR3ReaderDataSource")
    DataSource f2eR3ReaderDataSource(
            @Qualifier("f2eR3PrivilegedDataSource") DataSource privileged,
            PostgreSQLContainer<?> container,
            @Qualifier("nominalJdbcCapture") NominalTransactionTestOwner.JdbcCapture capture) {
        Objects.requireNonNull(privileged, "privileged");
        return capture.wrap(role.crearDataSource(canonicalUrl(container)));
    }

    @Bean(name = "f2eR3StatementPolicyInspector")
    F2eStatementPolicyInspector f2eR3StatementPolicyInspector() {
        return F2eStatementPolicyInspector.paraR3(NominalProjectionCatalog.R3_NOMINAL_V1);
    }

    @Bean(name = "f2eR3ReaderEntityManagerFactory", destroyMethod = "close")
    EntityManagerFactory f2eR3ReaderEntityManagerFactory(
            @Qualifier("f2eR3ReaderDataSource") DataSource readerDataSource,
            @Qualifier("f2eR3StatementPolicyInspector") F2eStatementPolicyInspector inspector) {
        LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
        factory.setDataSource(readerDataSource);
        factory.setPersistenceUnitName("f2eR3ReaderPersistenceUnit");
        factory.setPackagesToScan("com.feelingpilates");
        factory.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto", "validate");
        properties.put("hibernate.dialect", NominalProjectionQueryExecutor.ScalarPostgresDialect.class.getName());
        properties.put("hibernate.default_schema", "public");
        properties.put("hibernate.session_factory.statement_inspector", inspector);
        properties.put("hibernate.show_sql", false);
        factory.setJpaPropertyMap(properties);
        factory.afterPropertiesSet();
        return Objects.requireNonNull(factory.getObject(), "f2eR3ReaderEntityManagerFactory");
    }

    @Bean(name = "f2eR3ReaderEntityManager")
    EntityManager f2eR3ReaderEntityManager(
            @Qualifier("f2eR3ReaderEntityManagerFactory") EntityManagerFactory factory,
            @Qualifier("nominalJdbcCapture") NominalTransactionTestOwner.JdbcCapture capture) {
        return capture.wrapEntityManager(SharedEntityManagerCreator.createSharedEntityManager(factory));
    }

    @Bean(name = "f2eReaderTransactionManager")
    JpaTransactionManager f2eReaderTransactionManager(
            @Qualifier("f2eR3ReaderEntityManagerFactory") EntityManagerFactory factory,
            @Qualifier("f2eR3ReaderDataSource") DataSource readerDataSource) {
        JpaTransactionManager manager = new JpaTransactionManager(factory);
        manager.setDataSource(readerDataSource);
        return manager;
    }

    @Bean
    static org.springframework.beans.factory.config.BeanPostProcessor nominalFailureBoundary() {
        return new org.springframework.beans.factory.config.BeanPostProcessor() {
            @Override public Object postProcessAfterInitialization(Object bean, String name) {
                if (!name.equals("nominalJpaReader")) return bean;
                var proxy = new org.springframework.aop.framework.ProxyFactory(bean);
                proxy.setInterfaces(com.feelingpilates.transicion.programacion.read.NominalProgrammingReadPort.class);
                proxy.setExposeProxy(true);
                proxy.addAdvice((org.aopalliance.intercept.MethodInterceptor) invocation -> {
                    try { return invocation.proceed(); }
                    catch (org.springframework.transaction.IllegalTransactionStateException e) {
                        Object[] args = invocation.getArguments();
                        LocalDate date = args.length == 2 && args[1] instanceof LocalDate d ? d : null;
                        throw new com.feelingpilates.transicion.programacion.read.NominalReadFailure(
                                com.feelingpilates.transicion.programacion.read.NominalReadFailure.Category.TRANSACTION_CONTEXT_INVALID,
                                date, java.util.List.of(), e);
                    }
                });
                return proxy.getProxy();
            }
        };
    }

    @Bean(name = "nominalProjectionQueryExecutor")
    NominalProjectionQueryExecutor nominalProjectionQueryExecutor(
            @Qualifier("f2eR3ReaderEntityManager") EntityManager entityManager) {
        return new NominalProjectionQueryExecutor(
                entityManager, NominalProjectionCatalog.R3_NOMINAL_V1);
    }

    @Bean(name = "nominalProjectionMapper")
    NominalProjectionMapper nominalProjectionMapper() {
        return new NominalProjectionMapper(NominalProjectionCatalog.R3_NOMINAL_V1);
    }

    @Bean(name = "nominalJpaReader")
    NominalJpaReader nominalJpaReader(
            @Qualifier("nominalProjectionQueryExecutor") NominalProjectionQueryExecutor executor,
            @Qualifier("nominalProjectionMapper") NominalProjectionMapper mapper,
            @Qualifier("nominalContextRegistry") NominalTransactionTestOwner.ContextRegistry contexts) {
        return new NominalJpaReader(executor, mapper, NominalProjectionCatalog.R3_NOMINAL_V1,
                contexts);
    }

    @Bean(name = "nominalContextRegistry")
    NominalTransactionTestOwner.ContextRegistry nominalContextRegistry() {
        return new NominalTransactionTestOwner.ContextRegistry();
    }

    @Bean(name = "nominalTransactionTestOwner")
    NominalTransactionTestOwner nominalTransactionTestOwner(
            @Qualifier("nominalJpaReader") com.feelingpilates.transicion.programacion.read.NominalProgrammingReadPort reader,
            @Qualifier("nominalProjectionQueryExecutor") NominalProjectionQueryExecutor executor,
            @Qualifier("nominalProjectionMapper") NominalProjectionMapper mapper,
            @Qualifier("f2eR3StatementPolicyInspector") F2eStatementPolicyInspector inspector,
            @Qualifier("nominalJdbcCapture") NominalTransactionTestOwner.JdbcCapture jdbcCapture,
            @Qualifier("nominalContextRegistry") NominalTransactionTestOwner.ContextRegistry contexts,
            @Qualifier("f2eR3ReaderDataSource") DataSource readerDataSource,
            @Qualifier("f2eR3ReaderEntityManagerFactory") EntityManagerFactory factory,
            @Qualifier("f2eReaderTransactionManager") JpaTransactionManager manager,
            @Qualifier("f2eR3ReaderEntityManager") EntityManager entityManager,
            PostgreSQLContainer<?> container) {
        Descriptor descriptor = new Descriptor(
                "nominal-a", "fixture-r3-nominal-a", "fixture:postgres16:r3:nominal-a",
                schemaFingerprint, canonicalUrl(container), container.getDatabaseName(), "public",
                role.principal(), readerDataSource, factory, manager, entityManager, inspector);
        return new NominalTransactionTestOwner(
                reader, executor, inspector, jdbcCapture, contexts, descriptor);
    }

    public UUID salonId() { return salonId; }
    public UUID userId() { return userId; }
    public UUID activityId() { return activityId; }
    public String schemaFingerprint() { return schemaFingerprint; }
    public int appliedMigrations() { return appliedMigrations; }
    public String flywayHead() { return flywayHead; }
    public String readerPrincipal() { return role.principal(); }

    public void resetFixtures(DataSource dataSource) {
        try(var c=dataSource.getConnection();var q=c.createStatement()) {
            q.executeUpdate("DELETE FROM programacion_asignacion"); q.executeUpdate("DELETE FROM programacion_bloque");
        } catch(SQLException e) { throw new IllegalStateException(e); }
        insertFixtures(dataSource);
    }
    private void insertFixtures(DataSource dataSource) {
        try (var c=dataSource.getConnection()) {
            salonId=firstUuid(c,"SELECT id FROM public.salon ORDER BY id LIMIT 1");
            userId=firstUuid(c,"SELECT id FROM public.usuario ORDER BY id LIMIT 1");
            activityId=firstUuid(c,"SELECT id FROM public.tipo_actividad ORDER BY id LIMIT 1");
            try(var q=c.prepareStatement("INSERT INTO programacion_bloque "
                    + "(id,serie_id,salon_id,dia_semana,hora_inicio,hora_fin,vigente_desde,vigente_hasta) "
                    + "VALUES (?,?,?,1,'08:00','12:00',?,NULL)")) {
                q.setObject(1,BLOCK);q.setObject(2,UUID.fromString("30000000-0000-4000-8000-000000000004"));
                q.setObject(3,salonId);q.setObject(4,FECHA);q.executeUpdate();
            }
            insertAssignment(dataSource,ASSIGNMENT,SERIES,FECHA,null,true);
        } catch(SQLException e) { throw new IllegalStateException("R3 fixture failed",e); }
    }
    public void insertAssignment(DataSource ds,UUID id,UUID series,LocalDate from,LocalDate until,boolean active) {
        try(var c=ds.getConnection();var q=c.prepareStatement("INSERT INTO programacion_asignacion "
                + "(id,serie_id,bloque_id,instructor_id,tipo_actividad_id,hora_inicio,hora_fin,vigente_desde,vigente_hasta,activo) "
                + "VALUES (?,?,?,?,?,'09:00','10:00',?,?,?)")) {
            q.setObject(1,id);q.setObject(2,series);q.setObject(3,BLOCK);q.setObject(4,userId);
            q.setObject(5,activityId);q.setObject(6,from);q.setObject(7,until);q.setBoolean(8,active);q.executeUpdate();
        } catch(SQLException e) { throw new IllegalStateException("R3 assignment fixture failed",e); }
    }

    private UUID firstUuid(java.sql.Connection connection, String sql) throws SQLException {
        try (var statement = connection.createStatement(); var rows = statement.executeQuery(sql)) {
            if (!rows.next()) throw new IllegalStateException("Required R3 fixture catalog is empty");
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
