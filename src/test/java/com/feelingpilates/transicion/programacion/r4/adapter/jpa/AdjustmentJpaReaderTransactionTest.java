package com.feelingpilates.transicion.programacion.r4.adapter.jpa;
import com.feelingpilates.transicion.programacion.r4.adapter.jpa.projection.*;
import com.feelingpilates.transicion.programacion.r4.adapter.jpa.mapper.*;
import com.feelingpilates.transicion.programacion.r4.adapter.jpa.testinfra.*;
import com.feelingpilates.transicion.programacion.r4.read.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.orm.jpa.JpaTransactionManager;
import javax.sql.DataSource;
import java.time.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static com.feelingpilates.transicion.programacion.r4.adapter.jpa.testinfra.AdjustmentPostgresTestConfiguration.*;
@SpringJUnitConfig(AdjustmentPostgresTestConfiguration.class)
class AdjustmentJpaReaderTransactionTest {
    @Autowired AdjustmentTransactionTestOwner owner; @Autowired AdjustmentJdbcCapture jdbc;
    @Autowired AdjustmentReadPort reader; @Autowired AdjustmentProjectionQueryExecutor executor;
    @Autowired AdjustmentTransactionTestOwner.Registry registry;@Autowired AdjustmentPostgresTestConfiguration fixture;
    @Autowired @Qualifier("r4Admin") DataSource admin;
    @Autowired @Qualifier("r4DS") DataSource readerDS;
    @Autowired @Qualifier("r4EMF") jakarta.persistence.EntityManagerFactory factory;
    @Autowired @Qualifier("f2eReaderTransactionManager") JpaTransactionManager manager;
    @BeforeEach void reset() { jdbc.fault(null);jdbc.trust(null,null);jdbc.beforeData(null);fixture.reset(admin); }
    @AfterEach void clear() { jdbc.fault(null);jdbc.trust(null,null);jdbc.beforeData(null); }
    @Test void mandatoryManagerNoOwnerRawInvocationReadCommittedReadWriteAndReplayRejectedBeforeData() {
        var fake=AdjustmentProjectionMapperTest.context("run");
        assertEquals(AdjustmentReadFailure.Category.TRANSACTION_CONTEXT_INVALID,assertThrows(AdjustmentReadFailure.class,
                ()->reader.readActiveAdjustmentsOnDate(fake,FECHA)).category());
        var raw=new AdjustmentJpaReader(executor,new AdjustmentProjectionMapper(),AdjustmentProjectionCatalog.R4_ADJUSTMENT_V1,registry);
        assertThrows(AdjustmentReadFailure.class,()->raw.readActiveAdjustmentsOnDate(fake,FECHA));
        for(boolean readOnly:List.of(true,false)) {
            var tx=new TransactionTemplate(manager);tx.setReadOnly(readOnly);tx.setIsolationLevel(java.sql.Connection.TRANSACTION_READ_COMMITTED);
            assertEquals(AdjustmentReadFailure.Category.TRANSACTION_CONTEXT_INVALID,assertThrows(AdjustmentReadFailure.class,
                    ()->tx.execute(s->reader.readActiveAdjustmentsOnDate(fake,FECHA))).category());
        }
        for(var mode:List.of(AdjustmentTransactionTestOwner.Mode.DIRECT,AdjustmentTransactionTestOwner.Mode.REPLAY,
                AdjustmentTransactionTestOwner.Mode.ROLLBACK,AdjustmentTransactionTestOwner.Mode.NO_COMPLETION,AdjustmentTransactionTestOwner.Mode.AFTER_COMMIT)) {
            var e=assertThrows(AdjustmentReadFailure.class,()->owner.read(AdjustmentTransactionTestOwner.Seed.fresh(),FECHA,c->c,mode));
            assertEquals(AdjustmentReadFailure.Category.TRANSACTION_CONTEXT_INVALID,e.category());assertFalse(jdbc.last().success);
        }
        jdbc.fault(AdjustmentJdbcCapture.Fault.ROLLBACK);
        assertEquals(AdjustmentReadFailure.Category.TRANSACTION_CONTEXT_INVALID,assertThrows(AdjustmentReadFailure.class,
                ()->owner.read(AdjustmentTransactionTestOwner.Seed.fresh(),FECHA,c->c,AdjustmentTransactionTestOwner.Mode.ROLLBACK)).category());
        jdbc.fault(null);
        assertEquals(AdjustmentReadFailure.Category.DATABASE_READ_FAILURE,assertThrows(AdjustmentReadFailure.class,
                ()->owner.read(AdjustmentTransactionTestOwner.Seed.fresh(),FECHA,c->c,AdjustmentTransactionTestOwner.Mode.EXTRA_SQL)).category());
        for(boolean readOnly:List.of(true,false)) {
            var tx=new TransactionTemplate(manager);tx.setReadOnly(readOnly);tx.setIsolationLevel(java.sql.Connection.TRANSACTION_REPEATABLE_READ);
            assertEquals(AdjustmentReadFailure.Category.TRANSACTION_CONTEXT_INVALID,assertThrows(AdjustmentReadFailure.class,
                    ()->tx.execute(status->reader.readActiveAdjustmentsOnDate(fake,FECHA))).category());
        }
    }
    @Test void equivalentContextAndChangedDescriptorNeverAcquirePrivateReservationAuthority() {
        for(String field:List.of("copy","source","schema","principal","physical","invocation","snapshot","commitment","date")) {
            var e=assertThrows(AdjustmentReadFailure.class,()->owner.read(AdjustmentTransactionTestOwner.Seed.fresh(),FECHA,c->forge(c,field),AdjustmentTransactionTestOwner.Mode.NORMAL));
            assertEquals(field.equals("date")?AdjustmentReadFailure.Category.INVALID_INPUT:AdjustmentReadFailure.Category.TRANSACTION_CONTEXT_INVALID,e.category());
            assertEquals(8,jdbc.last().statements.size());assertFalse(jdbc.last().success);
        }
    }
    static AdjustmentReadSnapshotContext forge(AdjustmentReadSnapshotContext c,String field) {
        return new AdjustmentReadSnapshotContext(c.runIdentity(),c.attemptIdentity(),field.equals("date")?c.fecha().plusDays(1):c.fecha(),c.businessZone(),
                c.ruleCatalogVersion(),field.equals("source")?"hostile":c.sourceName(),field.equals("schema")?"sha256:"+"a".repeat(64):c.schemaFingerprint(),
                c.databaseName(),c.schemaName(),field.equals("principal")?"hostile":c.principal(),field.equals("physical")?"hostile":c.physicalResourceIdentity(),
                c.projectionCatalogVersion(),field.equals("invocation")?"hostile":c.readerInvocationIdentity(),c.snapshotClaim(),
                field.equals("snapshot")?"a".repeat(64):c.snapshotEvidenceId(),field.equals("commitment")?"a".repeat(64):c.statementCaptureCommitment());
    }
    @Test void initialFinalProbeSnapshotMetadataPhysicalAndCompletionFailuresDiscardEntireReadAndPreserveCause() {
        var checksum=new AdjustmentSliceChecksum(admin);String before=checksum.onDate(FECHA);
        for(var fault:List.of(AdjustmentJdbcCapture.Fault.INITIAL_PROBE,AdjustmentJdbcCapture.Fault.FINAL_PROBE,
                AdjustmentJdbcCapture.Fault.METADATA,AdjustmentJdbcCapture.Fault.SNAPSHOT,AdjustmentJdbcCapture.Fault.RESOURCE,AdjustmentJdbcCapture.Fault.COMMIT)) {
            jdbc.fault(fault);var e=assertThrows(AdjustmentReadFailure.class,()->owner.read(AdjustmentTransactionTestOwner.Seed.fresh(),FECHA));
            assertEquals(fault==AdjustmentJdbcCapture.Fault.INITIAL_PROBE||fault==AdjustmentJdbcCapture.Fault.FINAL_PROBE?
                    AdjustmentReadFailure.Category.DATABASE_READ_FAILURE:AdjustmentReadFailure.Category.TRANSACTION_CONTEXT_INVALID,e.category());
            assertNotNull(e.getCause());assertFalse(jdbc.last().success);assertEquals(before,checksum.onDate(FECHA));
            System.out.println("R4_TRUST_COMPLETION_FAIL_CLOSED "+fault+" => "+e.category());
        }
    }
    @Test void genuinelyDifferentDataSourceFactoryHolderSharedTargetSessionAndNativePgConnectionFailBeforeSql() throws Exception {
        var f=new org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean();f.setDataSource(readerDS);
        f.setPersistenceUnitName("r4-negative-independent-emf");f.setPackagesToScan("com.feelingpilates.transicion.programacion.r4");
        f.setJpaVendorAdapter(new org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter());
        f.setJpaPropertyMap(Map.of("hibernate.hbm2ddl.auto","none"));f.afterPropertiesSet();
        var otherFactory=Objects.requireNonNull(f.getObject());var otherEm=otherFactory.createEntityManager();
        try(var otherConnection=readerDS.getConnection()) {
            Object[][] overrides={
                {AdjustmentJdbcCapture.TrustKind.DATA_SOURCE,new org.springframework.jdbc.datasource.DelegatingDataSource(readerDS)},
                {AdjustmentJdbcCapture.TrustKind.FACTORY,otherFactory},
                {AdjustmentJdbcCapture.TrustKind.HOLDER,new org.springframework.orm.jpa.EntityManagerHolder(otherEm)},
                {AdjustmentJdbcCapture.TrustKind.SHARED_ENTITY_MANAGER,jdbc.wrapEntityManager(org.springframework.orm.jpa.SharedEntityManagerCreator.createSharedEntityManager(otherFactory))},
                {AdjustmentJdbcCapture.TrustKind.SESSION,otherEm.unwrap(org.hibernate.Session.class)},
                {AdjustmentJdbcCapture.TrustKind.PG_CONNECTION,otherConnection.unwrap(org.postgresql.PGConnection.class)}};
            for(Object[] override:overrides) {
                jdbc.trust((AdjustmentJdbcCapture.TrustKind)override[0],override[1]);
                var e=assertThrows(AdjustmentReadFailure.class,()->owner.read(AdjustmentTransactionTestOwner.Seed.fresh(),FECHA));
                assertEquals(AdjustmentReadFailure.Category.TRANSACTION_CONTEXT_INVALID,e.category());assertNotNull(e.getCause());
                assertEquals(0,jdbc.last().statements.size());assertFalse(jdbc.last().success);
                System.out.println("R4_REAL_DIFFERENT_RESOURCE_NEGATIVE "+override[0]+" beforeSQL=0");
            }
            jdbc.trust(null,null);
            try(var wrongManagerContext=new AnnotationConfigApplicationContext()) {
                wrongManagerContext.register(MissingManager.class);
                wrongManagerContext.registerBean("f2eReaderTransactionManager",JpaTransactionManager.class,()->new JpaTransactionManager(otherFactory));
                wrongManagerContext.refresh();var canonicalTx=new TransactionTemplate(manager);canonicalTx.setReadOnly(true);
                canonicalTx.setIsolationLevel(java.sql.Connection.TRANSACTION_REPEATABLE_READ);
                var e=assertThrows(AdjustmentReadFailure.class,()->canonicalTx.execute(status->wrongManagerContext.getBean(AdjustmentReadPort.class)
                        .readActiveAdjustmentsOnDate(AdjustmentProjectionMapperTest.context("run"),FECHA)));
                assertEquals(AdjustmentReadFailure.Category.TRANSACTION_CONTEXT_INVALID,e.category());assertNotNull(e.getCause());
                System.out.println("R4_REAL_WRONG_MANAGER_EMF_MANDATORY_NEGATIVE");
            }
        } finally { jdbc.trust(null,null);otherEm.close();otherFactory.close(); }
    }
    @Test void priorInvocationContextIsRejectedEvenWhenItsPhysicalContentStillMatches() {
        var prior=owner.read(AdjustmentTransactionTestOwner.Seed.fresh(),FECHA).context();
        var e=assertThrows(AdjustmentReadFailure.class,()->owner.read(AdjustmentTransactionTestOwner.Seed.fresh(),FECHA,c->prior,AdjustmentTransactionTestOwner.Mode.NORMAL));
        assertEquals(AdjustmentReadFailure.Category.TRANSACTION_CONTEXT_INVALID,e.category());assertEquals(FECHA,e.fecha());
        assertEquals(8,jdbc.last().statements.size());assertFalse(jdbc.last().success);
    }
    @Test void realPrivilegeFailureAndLockTimeoutRetainSqlStateAndNoPartialResult() throws Exception {
        String principal=fixture.principal();
        AdjustmentJpaReaderPostgreSqlTest.sql(admin,"REVOKE SELECT ON public.flyway_schema_history FROM "+principal);
        try {
            var e=assertThrows(AdjustmentReadFailure.class,()->owner.read(AdjustmentTransactionTestOwner.Seed.fresh(),FECHA));
            assertEquals(AdjustmentReadFailure.Category.DATABASE_READ_FAILURE,e.category());assertEquals("42501",sqlState(e));
            assertFalse(jdbc.last().success);assertEquals(5,jdbc.last().statements.size());
        } finally { AdjustmentJpaReaderPostgreSqlTest.sql(admin,"GRANT SELECT ON public.flyway_schema_history TO "+principal); }
        AdjustmentJpaReaderPostgreSqlTest.sql(admin,"ALTER ROLE "+principal+" SET statement_timeout='200ms'");
        try(var lock=admin.getConnection()) {
            lock.setAutoCommit(false);try(var q=lock.createStatement()) { q.execute("LOCK TABLE public.flyway_schema_history IN ACCESS EXCLUSIVE MODE"); }
            var e=assertThrows(AdjustmentReadFailure.class,()->owner.read(AdjustmentTransactionTestOwner.Seed.fresh(),FECHA));
            assertEquals(AdjustmentReadFailure.Category.DATABASE_READ_FAILURE,e.category());assertEquals("57014",sqlState(e));
            assertFalse(jdbc.last().success);lock.rollback();
        } finally { AdjustmentJpaReaderPostgreSqlTest.sql(admin,"ALTER ROLE "+principal+" RESET statement_timeout"); }
        System.out.println("R4_REAL_PRIVILEGE42501_TIMEOUT57014_FAIL_CLOSED");
    }
    private static String sqlState(Throwable e) {
        for(Throwable c=e;c!=null;c=c.getCause()) if(c instanceof java.sql.SQLException s) return s.getSQLState();
        throw new AssertionError("SQLState cause missing",e);
    }
    @org.springframework.context.annotation.Configuration(proxyBeanMethods=false)
    @org.springframework.transaction.annotation.EnableTransactionManagement
    @org.springframework.context.annotation.EnableAspectJAutoProxy(exposeProxy=true)
    static class MissingManager {
        @org.springframework.context.annotation.Bean("adjustmentReader") AdjustmentJpaReader reader() {
            return new AdjustmentJpaReader(new AdjustmentProjectionQueryExecutor(org.mockito.Mockito.mock(jakarta.persistence.EntityManager.class),
                    AdjustmentProjectionCatalog.R4_ADJUSTMENT_V1),new AdjustmentProjectionMapper(),AdjustmentProjectionCatalog.R4_ADJUSTMENT_V1,
                    new AdjustmentTransactionTestOwner.Registry());
        }
        @org.springframework.context.annotation.Bean static org.springframework.beans.factory.config.BeanPostProcessor boundary() {
            return AdjustmentPostgresTestConfiguration.adjustmentFailureBoundary();
        }
    }
    @Test void missingNamedManagerIsNormalizedBeforeMethodBodyAndData() {
        try(var ctx=new AnnotationConfigApplicationContext(MissingManager.class)) {
            var e=assertThrows(AdjustmentReadFailure.class,()->ctx.getBean(AdjustmentReadPort.class)
                    .readActiveAdjustmentsOnDate(AdjustmentProjectionMapperTest.context("run"),FECHA));
            assertEquals(AdjustmentReadFailure.Category.TRANSACTION_CONTEXT_INVALID,e.category());assertNotNull(e.getCause());
        }
    }
    @Test void isolatedAlteredHistoryExtraIndexAndExtraConstraintFailCanonicalDescriptorBeforeData() {
        try(var ctx=new AnnotationConfigApplicationContext(AdjustmentPostgresTestConfiguration.class)) {
            var isolatedAdmin=ctx.getBean("r4Admin",DataSource.class);var isolatedOwner=ctx.getBean(AdjustmentTransactionTestOwner.class);
            String[] changes={"UPDATE public.flyway_schema_history SET checksum=checksum+1 WHERE version='47'",
                    "CREATE INDEX r4_negative_extra_index ON programacion_ajuste_fecha(tipo)",
                    "ALTER TABLE programacion_ajuste_fecha ADD CONSTRAINT r4_negative_extra_check CHECK (true)"};
            String[] restore={"UPDATE public.flyway_schema_history SET checksum=-1624594375 WHERE version='47'",
                    "DROP INDEX r4_negative_extra_index","ALTER TABLE programacion_ajuste_fecha DROP CONSTRAINT r4_negative_extra_check"};
            for(int i=0;i<changes.length;i++) {
                AdjustmentJpaReaderPostgreSqlTest.sql(isolatedAdmin,changes[i]);
                try {
                    var e=assertThrows(AdjustmentReadFailure.class,()->isolatedOwner.read(AdjustmentTransactionTestOwner.Seed.fresh(),FECHA));
                    assertEquals(AdjustmentReadFailure.Category.DATABASE_READ_FAILURE,e.category());assertNotNull(e.getCause());
                    assertEquals(8,ctx.getBean(AdjustmentJdbcCapture.class).last().statements.size());
                    assertFalse(ctx.getBean(AdjustmentJdbcCapture.class).last().success);
                } finally { AdjustmentJpaReaderPostgreSqlTest.sql(isolatedAdmin,restore[i]); }
            }
            assertEquals(3,isolatedOwner.read(AdjustmentTransactionTestOwner.Seed.fresh(),FECHA).set().sources().size());
            System.out.println("R4_ISOLATED_HISTORY_INDEX_CONSTRAINT_DRIFT_NEGATIVE_PASS");
        }
    }
    @Test void actualV46MissingPrerequisiteIsDatabaseFailureBeforeDataNeverEmptySuccess() {
        try(var ctx=new AnnotationConfigApplicationContext()) {
            ctx.getEnvironment().getPropertySources().addFirst(new org.springframework.core.env.MapPropertySource("r4-negative",Map.of("r4.test.target","46")));
            ctx.register(AdjustmentPostgresTestConfiguration.class);ctx.refresh();
            var negative=ctx.getBean(AdjustmentTransactionTestOwner.class);
            var e=assertThrows(AdjustmentReadFailure.class,()->negative.read(AdjustmentTransactionTestOwner.Seed.fresh(),FECHA));
            assertEquals(AdjustmentReadFailure.Category.DATABASE_READ_FAILURE,e.category());
            assertEquals(8,ctx.getBean(AdjustmentJdbcCapture.class).last().statements.size());
            assertFalse(ctx.getBean(AdjustmentJdbcCapture.class).last().success);
            System.out.println("R4_V46_PREREQUISITE_NEGATIVE "+e.category());
        }
    }
}
