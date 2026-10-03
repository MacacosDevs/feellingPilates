package com.feelingpilates.transicion.programacion.adapter.jpa;

import com.feelingpilates.transicion.programacion.adapter.jpa.testinfra.*;
import com.feelingpilates.transicion.programacion.adapter.jpa.projection.*;
import com.feelingpilates.transicion.programacion.read.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.testcontainers.containers.PostgreSQLContainer;
import javax.sql.DataSource;
import java.time.*;
import java.sql.*;
import java.util.*;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;
import static com.feelingpilates.transicion.programacion.adapter.jpa.testinfra.NominalPostgresTestConfiguration.*;

import org.springframework.aop.support.AopUtils;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.IllegalTransactionStateException;

@SpringJUnitConfig(NominalPostgresTestConfiguration.class)
class NominalJpaReaderTransactionTest {
    @Autowired NominalTransactionTestOwner owner;
    @Autowired NominalPostgresTestConfiguration config;
    @Autowired @Qualifier("f2eR3PrivilegedDataSource") DataSource privileged;
    @Autowired @Qualifier("f2eR3ReaderDataSource") DataSource readerDS;
    @Autowired NominalTransactionTestOwner.JdbcCapture capture;
    @Autowired PostgreSQLContainer<?> container;
    @BeforeEach void reset() { config.resetFixtures(privileged); }
    static NominalTransactionTestOwner.Seed seed() {
        return new NominalTransactionTestOwner.Seed("run-r3",UUID.randomUUID().toString(),"rules-v1",ZoneId.of("America/Mexico_City"));
    }
    void sql(String sql) throws SQLException {
        try(var c=privileged.getConnection();var q=c.createStatement()) { q.execute(sql); }
    }
    String assignmentWhere() { return " WHERE id='"+ASSIGNMENT+"'"; }
    String blockWhere() { return " WHERE id='"+BLOCK+"'"; }
    @Autowired NominalProgrammingReadPort reader;
    @Autowired @Qualifier("f2eReaderTransactionManager") JpaTransactionManager manager;
    @Autowired @Qualifier("f2eR3ReaderEntityManagerFactory") jakarta.persistence.EntityManagerFactory factory;
    @Test void separateProxiesMandatoryOwnerAndBoundedRRReadOnlyResourceCompletion() {
        assertTrue(AopUtils.isAopProxy(reader));assertTrue(AopUtils.isAopProxy(owner));assertNotSame(reader,owner);
        assertEquals(NominalReadFailure.Category.TRANSACTION_CONTEXT_INVALID,
                assertThrows(NominalReadFailure.class,()->reader.readNominalOnDate(NominalProjectionMapperTest.context(),FECHA)).category());
        var outcome=owner.inRepeatableReadOnly(seed(),FECHA);
        assertTrue(outcome.completed());assertEquals(outcome.snapshotInitial,outcome.snapshotFinal);
        assertEquals(9,outcome.statementIds.size());assertEquals(9,outcome.jdbcObservations.size());
        assertEquals(4,outcome.metadataObservations.size());assertEquals(1,outcome.readSet().candidates().size());
        assertTrue(outcome.physicalConnection instanceof org.postgresql.jdbc.PgConnection);
        assertTrue(outcome.jdbcObservations.stream().allMatch(o->o.holder()==outcome.holder && o.session()==outcome.session
                && o.physicalConnection()==outcome.physicalConnection && o.factory()==factory));
        System.out.println("R3 owned RR initial="+outcome.snapshotInitial+" final="+outcome.snapshotFinal+" resource/snapshot/completion SQL="+outcome.statementIds+" metadata="+outcome.metadataObservations);
    }
    @Test void missingOwnerReadCommittedWrongManagerAndDirectConstructionFailBeforeData() {
        for(int isolation:List.of(TransactionDefinition.ISOLATION_READ_COMMITTED,TransactionDefinition.ISOLATION_REPEATABLE_READ)) {
            var tx=new TransactionTemplate(manager);tx.setIsolationLevel(isolation);tx.setReadOnly(true);
            assertEquals(NominalReadFailure.Category.TRANSACTION_CONTEXT_INVALID,
                    assertThrows(NominalReadFailure.class,()->tx.execute(s->reader.readNominalOnDate(NominalProjectionMapperTest.context(),FECHA))).category());
        }
        var wrong=new org.springframework.jdbc.datasource.DataSourceTransactionManager(privileged);
        var tx=new TransactionTemplate(wrong);tx.setIsolationLevel(TransactionDefinition.ISOLATION_REPEATABLE_READ);tx.setReadOnly(true);
        assertEquals(NominalReadFailure.Category.TRANSACTION_CONTEXT_INVALID,
                assertThrows(NominalReadFailure.class,()->tx.execute(s->reader.readNominalOnDate(NominalProjectionMapperTest.context(),FECHA))).category());
        var executor=new NominalProjectionQueryExecutor(org.mockito.Mockito.mock(jakarta.persistence.EntityManager.class),NominalProjectionCatalog.R3_NOMINAL_V1);
        var direct=new NominalJpaReader(executor,new com.feelingpilates.transicion.programacion.adapter.jpa.mapper.NominalProjectionMapper(
                NominalProjectionCatalog.R3_NOMINAL_V1),NominalProjectionCatalog.R3_NOMINAL_V1,(c,d)->fail("authority reached without TX"));
        assertThrows(NominalReadFailure.class,()->direct.readNominalOnDate(NominalProjectionMapperTest.context(),FECHA));
        assertInvalidFault(NominalTransactionTestOwner.Fault.DIRECT_READER);
    }
    @Test void directReaderAndOwnerInsideOtherwiseValidPhysicalRRTransactionFailBeforeData() {
        var directEM=org.mockito.Mockito.mock(jakarta.persistence.EntityManager.class);
        var executor=new NominalProjectionQueryExecutor(directEM,NominalProjectionCatalog.R3_NOMINAL_V1);
        var directReader=new NominalJpaReader(executor,
                new com.feelingpilates.transicion.programacion.adapter.jpa.mapper.NominalProjectionMapper(NominalProjectionCatalog.R3_NOMINAL_V1),
                NominalProjectionCatalog.R3_NOMINAL_V1,(c,d)->{});
        var tx=new TransactionTemplate(manager);tx.setIsolationLevel(TransactionDefinition.ISOLATION_REPEATABLE_READ);tx.setReadOnly(true);
        assertEquals(NominalReadFailure.Category.TRANSACTION_CONTEXT_INVALID,
                assertThrows(NominalReadFailure.class,()->tx.execute(s->directReader.readNominalOnDate(NominalProjectionMapperTest.context(),FECHA))).category());
        org.mockito.Mockito.verifyNoInteractions(directEM);
        var actual=org.springframework.test.util.AopTestUtils.<NominalTransactionTestOwner>getTargetObject(owner);
        var descriptor=(NominalPostgresTestConfiguration.Descriptor)org.springframework.test.util.ReflectionTestUtils.getField(actual,"descriptor");
        var actualExecutor=(NominalProjectionQueryExecutor)org.springframework.test.util.ReflectionTestUtils.getField(actual,"executor");
        var inspector=(F2eStatementPolicyInspector)org.springframework.test.util.ReflectionTestUtils.getField(actual,"inspector");
        var contexts=(NominalTransactionTestOwner.ContextRegistry)org.springframework.test.util.ReflectionTestUtils.getField(actual,"contexts");
        var directOwner=new NominalTransactionTestOwner(reader,actualExecutor,inspector,capture,contexts,descriptor);
        assertEquals(NominalReadFailure.Category.TRANSACTION_CONTEXT_INVALID,
                assertThrows(NominalReadFailure.class,()->tx.execute(s->directOwner.inRepeatableReadOnly(seed(),FECHA))).category());
    }
    @Test void equalValueForgedReplayCrossAttemptAndTamperedFieldsFailClosed() {
        var prior=owner.inRepeatableReadOnly(seed(),FECHA).context;
        assertEquals(NominalReadFailure.Category.TRANSACTION_CONTEXT_INVALID,
                assertThrows(NominalReadFailure.class,()->owner.negative(seed(),FECHA,c->prior,NominalTransactionTestOwner.Fault.NONE)).category());
        assertEquals(NominalReadFailure.Category.TRANSACTION_CONTEXT_INVALID,
                assertThrows(NominalReadFailure.class,()->owner.negative(seed(),FECHA,c->copy(c),NominalTransactionTestOwner.Fault.NONE)).category());
        for(String field:List.of("sourceName","schemaFingerprint","snapshotEvidenceId","statementCaptureCommitment",
                "readerInvocationIdentity","physicalResourceIdentity","attemptIdentity","principal","databaseName","schemaName")) {
            assertEquals(NominalReadFailure.Category.TRANSACTION_CONTEXT_INVALID,
                    assertThrows(NominalReadFailure.class,()->owner.negative(seed(),FECHA,c->tamper(c,field),NominalTransactionTestOwner.Fault.NONE)).category(),field);
        }
        var same=seed();owner.inRepeatableReadOnly(same,FECHA);
        assertThrows(NominalReadFailure.class,()->owner.inRepeatableReadOnly(same,FECHA));
        assertEquals("SUCCESS",owner.state(same));
        assertInvalidFault(NominalTransactionTestOwner.Fault.SECOND_READ);
    }
    static NominalReadSnapshotContext copy(NominalReadSnapshotContext c) { return tamper(c,""); }
    static NominalReadSnapshotContext tamper(NominalReadSnapshotContext c,String field) {
        var components=NominalReadSnapshotContext.class.getRecordComponents();Object[] args=new Object[components.length];
        Class<?>[] types=new Class<?>[components.length];
        try {
            for(int i=0;i<args.length;i++) {
                args[i]=components[i].getAccessor().invoke(c);types[i]=components[i].getType();
                if(components[i].getName().equals(field)) args[i]=field.equals("schemaFingerprint")?"sha256:"+"f".repeat(64):
                        field.equals("snapshotEvidenceId")||field.equals("statementCaptureCommitment")?"f".repeat(64):"forged";
            }
            return NominalReadSnapshotContext.class.getConstructor(types).newInstance(args);
        } catch(ReflectiveOperationException e) {throw new AssertionError(e);}
    }
    @Test void physicalResourceSnapshotAndCompletionFailuresNeverReleaseProvisionalOutput() {
        assertInvalidFault(NominalTransactionTestOwner.Fault.RESOURCE);
        assertInvalidFault(NominalTransactionTestOwner.Fault.SNAPSHOT);
        assertInvalidFault(NominalTransactionTestOwner.Fault.COMPLETION);
        var s=seed();var rolled=owner.negative(s,FECHA,c->c,NominalTransactionTestOwner.Fault.ROLLBACK);
        assertFalse(rolled.completed());assertEquals("ABORTED",owner.state(s));
        assertEquals(NominalReadFailure.Category.TRANSACTION_CONTEXT_INVALID,
                assertThrows(NominalReadFailure.class,rolled::readSet).category());
        var extra=seed();var failure=assertThrows(NominalReadFailure.class,()->owner.negative(extra,FECHA,c->c,NominalTransactionTestOwner.Fault.EXTRA_SQL));
        assertEquals(NominalReadFailure.Category.DATABASE_READ_FAILURE,failure.category());assertNotNull(failure.getCause());
        assertEquals("ABORTED",owner.state(extra));
        assertEquals(NominalReadFailure.Category.DATABASE_READ_FAILURE,
                assertThrows(NominalReadFailure.class,()->owner.negative(seed(),FECHA,c->c,NominalTransactionTestOwner.Fault.EXTRA_JDBC)).category());
    }
    void assertInvalidFault(NominalTransactionTestOwner.Fault fault) {
        var s=seed();var failure=assertThrows(NominalReadFailure.class,()->owner.negative(s,FECHA,c->c,fault));
        assertEquals(NominalReadFailure.Category.TRANSACTION_CONTEXT_INVALID,failure.category(),fault.toString());
        assertEquals("ABORTED",owner.state(s));
    }
    @Test void actualDatabasePermissionFailurePreserves42501CauseAndNoOutput() throws Exception {
        String principal=config.readerPrincipal();
        sql("REVOKE SELECT ON programacion_asignacion FROM "+principal);
        try {
            var s=seed();var failure=assertThrows(NominalReadFailure.class,()->owner.inRepeatableReadOnly(s,FECHA));
            assertEquals(NominalReadFailure.Category.DATABASE_READ_FAILURE,failure.category());
            Throwable cause=failure;boolean found=false;
            while(cause!=null) { if(cause instanceof SQLException e && "42501".equals(e.getSQLState())) found=true;cause=cause.getCause(); }
            assertTrue(found);assertEquals("ABORTED",owner.state(s));
        } finally { sql("GRANT SELECT ON programacion_asignacion TO "+principal); }
    }
    @Test void invalidSeedDoesNotReserveAndScopeMismatchRejected() {
        assertThrows(NominalReadFailure.class,()->owner.inRepeatableReadOnly(null,FECHA));
        assertThrows(NominalReadFailure.class,()->owner.inRepeatableReadOnly(seed(),null));
        var s=new NominalTransactionTestOwner.Seed("run","\uD800","rules",ZoneId.of("UTC"));
        assertThrows(NominalReadFailure.class,()->owner.inRepeatableReadOnly(s,FECHA));
    }
}
