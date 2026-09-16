package com.feelingpilates.transicion.programacion.adapter.jpa.testinfra;

import com.feelingpilates.transicion.programacion.adapter.jpa.ReservaJpaReader;
import com.feelingpilates.transicion.programacion.adapter.jpa.mapper.ReservaProjectionMapper;
import com.feelingpilates.transicion.programacion.adapter.jpa.projection.ReservaProjectionQueryExecutor;
import com.feelingpilates.transicion.programacion.read.ReadSnapshotContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.flywaydb.core.Flyway;
import org.postgresql.ds.PGSimpleDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.springframework.orm.jpa.EntityManagerHolder;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.SharedEntityManagerCreator;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@TestConfiguration(proxyBeanMethods = false)
@EnableTransactionManagement
@EnableAspectJAutoProxy
public class F2ePostgresTestConfiguration {

    public static final UUID RESERVA_UNO = UUID.fromString("10000000-0000-4000-8000-000000000001");
    public static final UUID RESERVA_DOS = UUID.fromString("10000000-0000-4000-8000-000000000002");
    private static final String CLAVE_CATALOGO = "r1-a";

    private F2eSelectOnlyRole rolLector;
    private DescriptorRecursoLector descriptor;
    private final TipoGrafoHostilPrueba tipoGrafoHostil;
    private final List<EntityManagerFactory> fabricasAutoridadAdicionales = new ArrayList<>();
    private DataSource dataSourceAutoridad;
    private DataSource dataSourceParticipante;
    private EntityManagerFactory fabricaAutoridad;
    private EntityManagerFactory fabricaParticipante;
    private JpaTransactionManager transactionManagerAutoridad;
    private JpaTransactionManager transactionManagerParticipante;
    private EntityManager entityManagerAutoridad;
    private EntityManager entityManagerParticipante;
    private volatile boolean transactionManagerHostilParticipo;
    private volatile boolean sesionHostilParticipo;

    public F2ePostgresTestConfiguration() {
        this(TipoGrafoHostilPrueba.NINGUNO);
    }

    private F2ePostgresTestConfiguration(TipoGrafoHostilPrueba tipoGrafoHostil) {
        this.tipoGrafoHostil = Objects.requireNonNull(tipoGrafoHostil, "tipoGrafoHostil");
    }

    public static AnnotationConfigApplicationContext abrirContextoRecursoHostilPrueba(String tipo) {
        TipoGrafoHostilPrueba seleccionado = TipoGrafoHostilPrueba.valueOf(tipo);
        AnnotationConfigApplicationContext contexto = new AnnotationConfigApplicationContext();
        contexto.registerBean(F2ePostgresTestConfiguration.class,
                () -> new F2ePostgresTestConfiguration(seleccionado));
        contexto.refresh();
        return contexto;
    }

    @Bean(destroyMethod = "stop")
    PostgreSQLContainer<?> f2ePostgresContainer() {
        PostgreSQLContainer<?> container = new PostgreSQLContainer<>(DockerImageName.parse("postgres:16-alpine"));
        container.start();
        return container;
    }

    @Bean
    DataSource f2ePrivilegedDataSource(PostgreSQLContainer<?> f2ePostgresContainer) {
        PGSimpleDataSource dataSource = new PGSimpleDataSource();
        dataSource.setURL(f2ePostgresContainer.getJdbcUrl());
        dataSource.setUser(f2ePostgresContainer.getUsername());
        dataSource.setPassword(f2ePostgresContainer.getPassword());
        Flyway flyway = Flyway.configure().dataSource(dataSource).locations("classpath:db/migration").load();
        flyway.migrate();
        flyway.validate();
        var informacionMigraciones = flyway.info();
        if (informacionMigraciones.current() == null
                || !"47".equals(informacionMigraciones.current().getVersion().getVersion())
                || informacionMigraciones.pending().length != 0
                || informacionMigraciones.applied().length != 50
                || java.util.Arrays.stream(informacionMigraciones.all()).anyMatch(
                        m -> m.getState() != org.flywaydb.core.api.MigrationState.SUCCESS)) {
            throw new IllegalStateException("F2E Flyway host validation not proven");
        }
        insertarFixtures(dataSource);
        rolLector = F2eSelectOnlyRole.crear(dataSource, CLAVE_CATALOGO);
        return dataSource;
    }

    @Bean(name = "f2eReaderDataSource")
    DataSource f2eReaderDataSource(
            @Qualifier("f2ePrivilegedDataSource") DataSource privilegiada,
            PostgreSQLContainer<?> f2ePostgresContainer) {
        Objects.requireNonNull(privilegiada, "privilegiada");
        dataSourceAutoridad = rolLector.crearDataSource(urlCanonica(f2ePostgresContainer));
        dataSourceParticipante = tipoGrafoHostil == TipoGrafoHostilPrueba.DATA_SOURCE_REAL
                ? dataSourceContabilizado(dataSourceAutoridad).copiaReal()
                : dataSourceAutoridad;
        return dataSourceParticipante;
    }

    @Bean(name = "f2eStatementPolicyInspector")
    F2eStatementPolicyInspector f2eStatementPolicyInspector() {
        return new F2eStatementPolicyInspector();
    }

    @Bean(name = "f2eReaderEntityManagerFactory", destroyMethod = "close")
    EntityManagerFactory f2eReaderEntityManagerFactory(
            @Qualifier("f2eReaderDataSource") DataSource readerDataSource,
            @Qualifier("f2eStatementPolicyInspector") F2eStatementPolicyInspector inspector) {
        if (tipoGrafoHostil == TipoGrafoHostilPrueba.ENTITY_MANAGER_FACTORY_REAL) {
            fabricaAutoridad = crearFabricaEntityManager(
                    readerDataSource, inspector, "f2eReaderPersistenceUnitAuthority");
            fabricasAutoridadAdicionales.add(fabricaAutoridad);
        }
        fabricaParticipante = crearFabricaEntityManager(readerDataSource, inspector, "f2eReaderPersistenceUnit");
        if (fabricaAutoridad == null) fabricaAutoridad = fabricaParticipante;
        return fabricaParticipante;
    }

    private EntityManagerFactory crearFabricaEntityManager(
            DataSource readerDataSource,
            F2eStatementPolicyInspector inspector,
            String persistenceUnitName) {
        LocalContainerEntityManagerFactoryBean fabrica = new LocalContainerEntityManagerFactoryBean();
        fabrica.setDataSource(readerDataSource);
        fabrica.setPersistenceUnitName(persistenceUnitName);
        fabrica.setPackagesToScan("com.feelingpilates");
        fabrica.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        Map<String, Object> propiedades = new HashMap<>();
        propiedades.put("hibernate.hbm2ddl.auto", "validate");
        propiedades.put("hibernate.default_schema", "public");
        propiedades.put("hibernate.session_factory.statement_inspector", inspector);
        propiedades.put("hibernate.show_sql", false);
        fabrica.setJpaPropertyMap(propiedades);
        fabrica.afterPropertiesSet();
        return Objects.requireNonNull(fabrica.getObject(), "f2eReaderEntityManagerFactory");
    }

    @Bean(name = "f2eReaderEntityManager")
    EntityManager f2eReaderEntityManager(
            @Qualifier("f2eReaderEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        if (tipoGrafoHostil == TipoGrafoHostilPrueba.ENTITY_MANAGER_REAL) {
            entityManagerAutoridad = SharedEntityManagerCreator.createSharedEntityManager(entityManagerFactory);
        }
        entityManagerParticipante = SharedEntityManagerCreator.createSharedEntityManager(entityManagerFactory);
        if (entityManagerAutoridad == null) entityManagerAutoridad = entityManagerParticipante;
        return entityManagerParticipante;
    }

    @Bean(name = "f2eReaderTransactionManager")
    JpaTransactionManager f2eReaderTransactionManager(
            @Qualifier("f2eReaderEntityManagerFactory") EntityManagerFactory entityManagerFactory,
            @Qualifier("f2eReaderDataSource") DataSource readerDataSource) {
        if (tipoGrafoHostil == TipoGrafoHostilPrueba.TRANSACTION_MANAGER_REAL) {
            transactionManagerAutoridad = crearTransactionManager(entityManagerFactory, readerDataSource);
            transactionManagerParticipante = new JpaTransactionManagerParticipantePrueba(
                    entityManagerFactory, () -> transactionManagerHostilParticipo = true);
        } else if (tipoGrafoHostil == TipoGrafoHostilPrueba.SESSION_REAL) {
            transactionManagerParticipante = new JpaTransactionManagerSesionHostilPrueba(
                    entityManagerFactory, () -> sesionHostilParticipo = true);
            transactionManagerAutoridad = transactionManagerParticipante;
        } else {
            transactionManagerParticipante = crearTransactionManager(entityManagerFactory, readerDataSource);
            transactionManagerAutoridad = transactionManagerParticipante;
        }
        transactionManagerParticipante.setDataSource(readerDataSource);
        return transactionManagerParticipante;
    }

    private JpaTransactionManager crearTransactionManager(
            EntityManagerFactory entityManagerFactory,
            DataSource readerDataSource) {
        JpaTransactionManager transactionManager = new JpaTransactionManager(entityManagerFactory);
        transactionManager.setDataSource(readerDataSource);
        return transactionManager;
    }

    @Bean(name = "reservaProjectionQueryExecutor")
    ReservaProjectionQueryExecutor reservaProjectionQueryExecutor(
            @Qualifier("f2eReaderEntityManager") EntityManager entityManager) {
        return new ReservaProjectionQueryExecutor(
                entityManager, ReadSnapshotContext.ProjectionCatalogVersion.R1_RESERVA_V1);
    }

    @Bean(name = "reservaProjectionMapper")
    ReservaProjectionMapper reservaProjectionMapper() {
        return new ReservaProjectionMapper(ReadSnapshotContext.ProjectionCatalogVersion.R1_RESERVA_V1);
    }

    @Bean(name = "reservaJpaReader")
    ReservaJpaReader reservaJpaReader(
            @Qualifier("reservaProjectionQueryExecutor") ReservaProjectionQueryExecutor ejecutor,
            @Qualifier("reservaProjectionMapper") ReservaProjectionMapper mapper,
            @Qualifier("f2eReaderDataSource") DataSource readerDataSource,
            @Qualifier("f2eReaderEntityManagerFactory") EntityManagerFactory entityManagerFactory,
            @Qualifier("f2eReaderTransactionManager") JpaTransactionManager transactionManager,
            @Qualifier("f2eReaderEntityManager") EntityManager entityManager,
            @Qualifier("f2eStatementPolicyInspector") F2eStatementPolicyInspector inspector,
            @Qualifier("f2ePrivilegedDataSource") DataSource privilegiada,
            PostgreSQLContainer<?> container) {
        DescriptorRecursoLector recurso = descriptor(
                readerDataSource, entityManagerFactory, transactionManager, entityManager,
                inspector, privilegiada, container);
        return new ReservaJpaReader(
                ejecutor, mapper, ReadSnapshotContext.ProjectionCatalogVersion.R1_RESERVA_V1,
                recurso.sourceName, recurso.identidadFuenteDatos, recurso.schemaFingerprint);
    }

    @Bean(name = "readerTransactionTestHarness")
    ReaderTransactionTestHarness readerTransactionTestHarness(
            @Qualifier("reservaJpaReader") com.feelingpilates.transicion.programacion.read.ReservationReadPort reader,
            @Qualifier("f2eStatementPolicyInspector") F2eStatementPolicyInspector inspector,
            @Qualifier("f2eReaderDataSource") DataSource readerDataSource,
            @Qualifier("f2eReaderEntityManagerFactory") EntityManagerFactory entityManagerFactory,
            @Qualifier("f2eReaderTransactionManager") JpaTransactionManager transactionManager,
            @Qualifier("f2eReaderEntityManager") EntityManager entityManager,
            @Qualifier("f2ePrivilegedDataSource") DataSource privilegiada,
            PostgreSQLContainer<?> container) {
        DescriptorRecursoLector recurso = descriptor(
                readerDataSource, entityManagerFactory, transactionManager, entityManager,
                inspector, privilegiada, container);
        if (tipoGrafoHostil == TipoGrafoHostilPrueba.CONEXION_FISICA_REAL) {
            dataSourceContabilizado(readerDataSource).conmutarFisicaEnSiguienteConexion();
        }
        return new ReaderTransactionTestHarness(
                reader, inspector, recurso, readerDataSource, entityManagerFactory,
                transactionManager, entityManager);
    }

    private synchronized DescriptorRecursoLector descriptor(
            DataSource readerDataSource,
            EntityManagerFactory entityManagerFactory,
            JpaTransactionManager transactionManager,
            EntityManager entityManager,
            F2eStatementPolicyInspector inspector,
            DataSource privilegiada,
            PostgreSQLContainer<?> container) {
        if (descriptor == null) {
            String huella = calcularHuellaEsquema(privilegiada);
            descriptor = new DescriptorRecursoLector(
                    "fixture:postgres16:" + CLAVE_CATALOGO,
                    "fixture-" + CLAVE_CATALOGO,
                    huella,
                    urlCanonica(container),
                    container.getDatabaseName(),
                    "public",
                    rolLector.principal(),
                    dataSourceAutoridad,
                    fabricaAutoridad,
                    transactionManagerAutoridad,
                    entityManagerAutoridad);
        }
        if (tipoGrafoHostil == TipoGrafoHostilPrueba.NINGUNO
                && (descriptor.readerDataSource != readerDataSource
                || descriptor.entityManagerFactory != entityManagerFactory
                || descriptor.transactionManager != transactionManager
                || descriptor.entityManager != entityManager)) {
            throw new IllegalStateException("F2E reader resource provenance not proven");
        }
        return descriptor;
    }

    private F2eSelectOnlyRole.DataSourceLectorContabilizado dataSourceContabilizado(DataSource dataSource) {
        if (!(dataSource instanceof F2eSelectOnlyRole.DataSourceLectorContabilizado contabilizado)) {
            throw new IllegalStateException("F2E reader DataSource instrumentation not proven");
        }
        return contabilizado;
    }

    public boolean conexionesFisicasDistintasPrueba() {
        F2eSelectOnlyRole.DataSourceLectorContabilizado dataSource =
                dataSourceContabilizado(dataSourceParticipante);
        return dataSource.conmutacionFisicaRealizada() && dataSource.recursosFisicosDistintos();
    }

    public boolean grafoHostilParticipantePrueba() {
        return switch (tipoGrafoHostil) {
            case DATA_SOURCE_REAL -> dataSourceParticipante != dataSourceAutoridad
                    && dataSourceContabilizado(dataSourceParticipante).conexionesEntregadas() > 0
                    && dataSourceContabilizado(dataSourceAutoridad).conexionesEntregadas() == 0;
            case ENTITY_MANAGER_FACTORY_REAL -> fabricaParticipante != fabricaAutoridad
                    && transactionManagerParticipante.getEntityManagerFactory() == fabricaParticipante;
            case TRANSACTION_MANAGER_REAL -> transactionManagerParticipante != transactionManagerAutoridad
                    && transactionManagerHostilParticipo;
            case ENTITY_MANAGER_REAL -> entityManagerParticipante != entityManagerAutoridad
                    && fabricaParticipante == fabricaAutoridad
                    && transactionManagerParticipante == transactionManagerAutoridad;
            case SESSION_REAL -> sesionHostilParticipo
                    && transactionManagerParticipante == transactionManagerAutoridad;
            case CONEXION_FISICA_REAL -> conexionesFisicasDistintasPrueba();
            case NINGUNO -> dataSourceParticipante == dataSourceAutoridad
                    && fabricaParticipante == fabricaAutoridad
                    && transactionManagerParticipante == transactionManagerAutoridad
                    && entityManagerParticipante == entityManagerAutoridad;
        };
    }

    @jakarta.annotation.PreDestroy
    void cerrarRecursosHostiles() {
        for (EntityManagerFactory fabrica : fabricasAutoridadAdicionales) {
            if (fabrica.isOpen()) fabrica.close();
        }
    }

    private void insertarFixtures(DataSource dataSource) {
        try (var conexion = dataSource.getConnection()) {
            UUID salon = primerUuid(conexion, "SELECT id FROM public.salon ORDER BY id LIMIT 1");
            UUID usuario = primerUuid(conexion, "SELECT id FROM public.usuario ORDER BY id LIMIT 1");
            UUID actividad = primerUuid(conexion, "SELECT id FROM public.tipo_actividad ORDER BY id LIMIT 1");
            try (var insercion = conexion.prepareStatement(
                    "INSERT INTO public.reserva "
                            + "(id,salon_id,instructor_id,cliente_id,tipo_actividad_id,fecha,hora_inicio,hora_fin,estado,creado_en,actualizado_en) "
                            + "VALUES (?,?,?,?,?,?,?,?,?,?,?) ON CONFLICT (id) DO NOTHING")) {
                insertarReserva(insercion, RESERVA_UNO, salon, usuario, actividad,
                        LocalDate.of(2026, 9, 15), LocalTime.of(8, 30), LocalTime.of(9, 30));
                insertarReserva(insercion, RESERVA_DOS, salon, usuario, actividad,
                        LocalDate.of(2026, 9, 16), LocalTime.of(10, 0), LocalTime.of(11, 0));
            }
        } catch (SQLException excepcion) {
            throw new IllegalStateException("Unable to create F2E reservation fixtures", excepcion);
        }
    }

    private void insertarReserva(
            java.sql.PreparedStatement insercion,
            UUID reserva,
            UUID salon,
            UUID usuario,
            UUID actividad,
            LocalDate fecha,
            LocalTime inicio,
            LocalTime fin) throws SQLException {
        insercion.setObject(1, reserva);
        insercion.setObject(2, salon);
        insercion.setObject(3, usuario);
        insercion.setObject(4, usuario);
        insercion.setObject(5, actividad);
        insercion.setObject(6, fecha);
        insercion.setObject(7, inicio);
        insercion.setObject(8, fin);
        insercion.setString(9, "CONFIRMADA");
        insercion.setObject(10, OffsetDateTime.of(2026, 9, 1, 14, 0, 0, 0, ZoneOffset.UTC));
        insercion.setObject(11, OffsetDateTime.of(2026, 9, 2, 15, 30, 0, 0, ZoneOffset.UTC));
        insercion.executeUpdate();
    }

    private UUID primerUuid(java.sql.Connection conexion, String sql) throws SQLException {
        try (var sentencia = conexion.createStatement(); var resultado = sentencia.executeQuery(sql)) {
            if (!resultado.next()) throw new IllegalStateException("Required fixture catalog is empty");
            return resultado.getObject(1, UUID.class);
        }
    }

    public static String calcularHuellaEsquema(DataSource dataSource) {
        try (var conexion = dataSource.getConnection()) {
            return calcularHuellaEsquema(conexion);
        } catch (SQLException excepcion) {
            throw new IllegalStateException("Unable to calculate installed schema fingerprint", excepcion);
        }
    }

    public static String calcularHuellaEsquema(java.sql.Connection conexion) {
        // Stable schema definitions and applied history only; no OIDs, clocks or install timestamps.
        List<String> consultas = List.of(
                "SELECT c.relname,c.relkind::text FROM pg_class c JOIN pg_namespace n ON n.oid=c.relnamespace "
                        + "WHERE n.nspname='public' ORDER BY c.relname COLLATE \"C\"",
                "SELECT c.relname,a.attname,a.attnum::text,format_type(a.atttypid,a.atttypmod),"
                        + "a.attnotnull::text,COALESCE(pg_get_expr(d.adbin,d.adrelid),''),a.attidentity::text,a.attgenerated::text "
                        + "FROM pg_attribute a JOIN pg_class c ON c.oid=a.attrelid "
                        + "JOIN pg_namespace n ON n.oid=c.relnamespace "
                        + "LEFT JOIN pg_attrdef d ON d.adrelid=c.oid AND d.adnum=a.attnum "
                        + "WHERE n.nspname='public' AND a.attnum>0 AND NOT a.attisdropped "
                        + "ORDER BY c.relname COLLATE \"C\",a.attnum",
                "SELECT c.relname,k.conname,pg_get_constraintdef(k.oid,true),k.convalidated::text "
                        + "FROM pg_constraint k JOIN pg_class c ON c.oid=k.conrelid "
                        + "JOIN pg_namespace n ON n.oid=c.relnamespace WHERE n.nspname='public' "
                        + "ORDER BY c.relname COLLATE \"C\",k.conname COLLATE \"C\"",
                "SELECT tablename,indexname,indexdef FROM pg_indexes WHERE schemaname='public' "
                        + "ORDER BY tablename COLLATE \"C\",indexname COLLATE \"C\"",
                "SELECT t.typname,e.enumlabel,e.enumsortorder::text FROM pg_type t "
                        + "JOIN pg_enum e ON e.enumtypid=t.oid JOIN pg_namespace n ON n.oid=t.typnamespace "
                        + "WHERE n.nspname='public' ORDER BY t.typname COLLATE \"C\",e.enumsortorder",
                "SELECT sequencename,data_type,start_value::text,min_value::text,max_value::text,"
                        + "increment_by::text,cycle::text,cache_size::text FROM pg_sequences "
                        + "WHERE schemaname='public' ORDER BY sequencename COLLATE \"C\"",
                "SELECT c.relname,t.tgname,pg_get_triggerdef(t.oid,true) FROM pg_trigger t "
                        + "JOIN pg_class c ON c.oid=t.tgrelid JOIN pg_namespace n ON n.oid=c.relnamespace "
                        + "WHERE n.nspname='public' AND NOT t.tgisinternal "
                        + "ORDER BY c.relname COLLATE \"C\",t.tgname COLLATE \"C\"",
                "SELECT viewname,definition FROM pg_views WHERE schemaname='public' "
                        + "ORDER BY viewname COLLATE \"C\"",
                "SELECT extname,extversion FROM pg_extension ORDER BY extname COLLATE \"C\"",
                "SELECT version,script,checksum::text,success::text FROM public.flyway_schema_history "
                        + "ORDER BY version COLLATE \"C\" NULLS FIRST,script COLLATE \"C\"");
        try {
            List<List<List<String>>> metadatos = new ArrayList<>();
            for (int i = 0; i < consultas.size(); i++) {
                List<List<String>> registros = new ArrayList<>();
                try (var sentencia = conexion.createStatement(); var filas = sentencia.executeQuery(consultas.get(i))) {
                    int columnas = filas.getMetaData().getColumnCount();
                    while (filas.next()) {
                        List<String> campos = new ArrayList<>();
                        for (int c = 1; c <= columnas; c++) {
                            campos.add(filas.getString(c));
                        }
                        registros.add(campos);
                    }
                }
                metadatos.add(registros);
            }
            return componerHuellaMetadatos(metadatos);
        } catch (Exception excepcion) {
            throw new IllegalStateException("Unable to calculate installed schema fingerprint", excepcion);
        }
    }

    // Pure test-infra seam shared by live metadata and synthetic sensitivity checks; no JDBC.
    private static String componerHuellaMetadatos(List<List<List<String>>> metadatos) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(com.feelingpilates.transicion.programacion.read.ReadSnapshotIdentifiers
                    .secuenciaTextos("F2E-INSTALLED-SCHEMA-FLYWAY-V1"));
            for (int i = 0; i < metadatos.size(); i++) {
                List<byte[]> filas = new ArrayList<>();
                for (List<String> fila : metadatos.get(i)) {
                    List<byte[]> campos = new ArrayList<>();
                    for (String valor : fila) {
                        campos.add(com.feelingpilates.transicion.programacion.read.ReadSnapshotIdentifiers
                                .secuenciaTextos(valor == null ? "N" : "V", valor == null ? "" : valor));
                    }
                    filas.add(com.feelingpilates.transicion.programacion.read.ReadSnapshotIdentifiers.secuencia(campos));
                }
                filas.sort(java.util.Arrays::compareUnsigned);
                List<byte[]> registros = new ArrayList<>();
                registros.add(Integer.toString(i).getBytes(StandardCharsets.US_ASCII));
                registros.addAll(filas);
                digest.update(com.feelingpilates.transicion.programacion.read.ReadSnapshotIdentifiers.secuencia(registros));
            }
            return "sha256:" + java.util.HexFormat.of().formatHex(digest.digest());
        } catch (java.security.NoSuchAlgorithmException excepcion) {
            throw new IllegalStateException("Unable to calculate installed schema fingerprint", excepcion);
        }
    }

    private String urlCanonica(PostgreSQLContainer<?> container) {
        return "jdbc:postgresql://" + container.getHost().toLowerCase(java.util.Locale.ROOT)
                + ':' + container.getMappedPort(5432) + '/' + container.getDatabaseName();
    }

    private static class JpaTransactionManagerParticipantePrueba extends JpaTransactionManager {
        private final Runnable alComenzar;

        private JpaTransactionManagerParticipantePrueba(
                EntityManagerFactory entityManagerFactory,
                Runnable alComenzar) {
            super(entityManagerFactory);
            this.alComenzar = Objects.requireNonNull(alComenzar, "alComenzar");
        }

        @Override
        protected void doBegin(Object transaction, TransactionDefinition definition) {
            super.doBegin(transaction, definition);
            alComenzar.run();
        }
    }

    private static final class JpaTransactionManagerSesionHostilPrueba extends JpaTransactionManager {
        private final Runnable alComenzar;

        private JpaTransactionManagerSesionHostilPrueba(
                EntityManagerFactory entityManagerFactory,
                Runnable alComenzar) {
            super(entityManagerFactory);
            this.alComenzar = Objects.requireNonNull(alComenzar, "alComenzar");
        }

        @Override
        protected void doBegin(Object transaction, TransactionDefinition definition) {
            super.doBegin(transaction, definition);
            Object recurso = TransactionSynchronizationManager.getResource(
                    Objects.requireNonNull(getEntityManagerFactory(), "entityManagerFactory"));
            if (!(recurso instanceof EntityManagerHolder holder)) {
                throw new IllegalStateException("F2E transaction-bound Session not proven");
            }
            Session sesion = holder.getEntityManager().unwrap(Session.class);
            sesion.setDefaultReadOnly(false);
            sesion.setHibernateFlushMode(FlushMode.AUTO);
            alComenzar.run();
        }
    }

    static final class DescriptorRecursoLector {
        final String sourceName;
        final String identidadFuenteDatos;
        final String schemaFingerprint;
        final String jdbcUrlCanonicaSinCredenciales;
        final String databaseName;
        final String schemaName;
        final String credentialPrincipal;
        final DataSource readerDataSource;
        final EntityManagerFactory entityManagerFactory;
        final JpaTransactionManager transactionManager;
        final EntityManager entityManager;

        private DescriptorRecursoLector(
                String sourceName,
                String identidadFuenteDatos,
                String schemaFingerprint,
                String jdbcUrlCanonicaSinCredenciales,
                String databaseName,
                String schemaName,
                String credentialPrincipal,
                DataSource readerDataSource,
                EntityManagerFactory entityManagerFactory,
                JpaTransactionManager transactionManager,
                EntityManager entityManager) {
            this.sourceName = sourceName;
            this.identidadFuenteDatos = identidadFuenteDatos;
            this.schemaFingerprint = schemaFingerprint;
            this.jdbcUrlCanonicaSinCredenciales = jdbcUrlCanonicaSinCredenciales;
            this.databaseName = databaseName;
            this.schemaName = schemaName;
            this.credentialPrincipal = credentialPrincipal;
            this.readerDataSource = readerDataSource;
            this.entityManagerFactory = entityManagerFactory;
            this.transactionManager = transactionManager;
            this.entityManager = entityManager;
        }
    }

    private enum TipoGrafoHostilPrueba {
        NINGUNO,
        DATA_SOURCE_REAL,
        ENTITY_MANAGER_FACTORY_REAL,
        TRANSACTION_MANAGER_REAL,
        ENTITY_MANAGER_REAL,
        SESSION_REAL,
        CONEXION_FISICA_REAL
    }
}
