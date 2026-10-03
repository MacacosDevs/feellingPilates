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

@SpringJUnitConfig(NominalPostgresTestConfiguration.class)
class NominalJpaReaderPostgreSqlTest {
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
    @Test void postgres16Flyway49Head46SchemaHistoryAndNoV47() throws Exception {
        assertTrue(container.getDockerImageName().startsWith("postgres:16-alpine"));
        assertEquals("46",config.flywayHead());assertEquals(49,config.appliedMigrations());
        assertTrue(config.schemaFingerprint().matches("sha256:[0-9a-f]{64}"));
        assertEquals(config.schemaFingerprint(),F2ePostgresTestConfiguration.calcularHuellaEsquema(privileged));
        try(var c=privileged.getConnection();var q=c.createStatement();var r=q.executeQuery(
                "SELECT current_setting('server_version_num')::int, "
                +"(SELECT count(*) FROM flyway_schema_history WHERE version='47'), "
                +"to_regclass('public.programacion_ajuste_fecha'), "
                +"(SELECT count(*) FROM pg_constraint WHERE conrelid='programacion_asignacion'::regclass AND contype='x')")) {
            assertTrue(r.next());assertTrue(r.getInt(1)>=160000&&r.getInt(1)<170000);
            assertEquals(0,r.getInt(2));assertNull(r.getObject(3));assertEquals(0,r.getInt(4));
        }
        String before=config.schemaFingerprint();
        sql("CREATE INDEX r3_metadata_sensitivity ON programacion_asignacion(actualizado_en)");
        assertNotEquals(before,F2ePostgresTestConfiguration.calcularHuellaEsquema(privileged));
        sql("DROP INDEX r3_metadata_sensitivity");assertEquals(before,F2ePostgresTestConfiguration.calcularHuellaEsquema(privileged));
    }
    @Test void exactlyTwoSelectGrantsAndBothTableWritesDdlSequencesOtherTablesDenied42501() throws Exception {
        try(var c=privileged.getConnection();var q=c.prepareStatement(
                "SELECT table_name,privilege_type FROM information_schema.role_table_grants WHERE grantee=? ORDER BY table_name,privilege_type")) {
            q.setString(1,config.readerPrincipal());try(var r=q.executeQuery()) {
                var grants=new ArrayList<String>();while(r.next()) grants.add(r.getString(1)+":"+r.getString(2));
                assertEquals(List.of("programacion_asignacion:SELECT","programacion_bloque:SELECT"),grants);
            }
        }
        for(String table:List.of("programacion_asignacion","programacion_bloque")) {
            assertDenied("INSERT INTO "+table+"(id) VALUES (gen_random_uuid())");
            assertDenied("UPDATE "+table+" SET activo=false WHERE false");
            assertDenied("DELETE FROM "+table+" WHERE false");
            assertDenied("TRUNCATE "+table);
            assertDenied("ALTER TABLE "+table+" ADD COLUMN r3_forbidden integer");
        }
        assertDenied("CREATE TABLE public.r3_forbidden(id integer)");
        assertDenied("CREATE TEMP TABLE r3_forbidden(id integer)");
        assertDenied("SELECT id FROM reserva");
        try(var c=privileged.getConnection();var q=c.prepareStatement(
                "SELECT count(*) FROM information_schema.role_usage_grants WHERE grantee=? AND object_type='SEQUENCE'")) {
            q.setString(1,config.readerPrincipal());try(var r=q.executeQuery()) {r.next();assertEquals(0,r.getInt(1));}
        }
    }
    private void assertDenied(String sql) {
        try(var c=readerDS.getConnection();var q=c.createStatement()) {
            SQLException e=assertThrows(SQLException.class,()->q.execute(sql));assertEquals("42501",e.getSQLState(),sql);
        } catch(SQLException e) { throw new AssertionError(e); }
    }
    @Test void REAL_JDBC_BINDING_ACCEPTANCE() {
        var outcome=owner.inRepeatableReadOnly(seed(),FECHA);
        assertTrue(outcome.completed());assertEquals(1,outcome.readSet().candidates().size());
        var data=outcome.jdbcObservations.get(4);
        NominalTransactionTestOwner.assertJdbcBinding(data,FECHA);
        assertEquals(NominalProjectionCatalog.positionalSql(),data.sql());
        assertEquals(List.of("assignmentActive","blockActive","fecha","fecha","fecha","fecha","dayOfWeek"),
                NominalProjectionCatalog.JDBC_OCCURRENCES);
        for(int slot=1;slot<=7;slot++) {
            var bind=data.binds().get(slot);
            assertEquals(slot,bind.position());
            assertEquals(slot<=2?"setBoolean":slot<=6?"setDate":"setShort",bind.setter());
            assertEquals(slot<=2?"java.lang.Boolean":slot<=6?"java.sql.Date":"java.lang.Short",bind.runtimeClass());
            assertEquals(slot<=2?"true":slot<=6?FECHA.toString():"1",bind.canonicalValue());
        }
        System.out.println("R3 REAL_JDBC_BINDING_ACCEPTANCE captured setters="+data.setterHistory()+" SQL="+data.sql());
        // Independent sensitivity controls change one observation at a time, never the expectations.
        for(int slot=1;slot<=7;slot++) {
            var b=data.binds().get(slot);
            for(var replacement:List.of(
                    new NominalTransactionTestOwner.JdbcCapture.BindObservation(slot,b.setter(),b.runtimeClass(),
                            slot<=2?"false":slot<=6?FECHA.plusDays(1).toString():"0",null),
                    new NominalTransactionTestOwner.JdbcCapture.BindObservation(slot+1,b.setter(),b.runtimeClass(),b.canonicalValue(),null),
                    new NominalTransactionTestOwner.JdbcCapture.BindObservation(slot,"setString","java.lang.String",b.canonicalValue(),null))) {
                Map<Integer,NominalTransactionTestOwner.JdbcCapture.BindObservation> changed=new HashMap<>(data.binds());
                changed.put(slot,replacement);assertRejects(data,changed,FECHA);
            }
            var omitted=new HashMap<>(data.binds());omitted.remove(slot);assertRejects(data,omitted,FECHA);
        }
        var swapped=new HashMap<>(data.binds());swapped.put(3,data.binds().get(7));swapped.put(7,data.binds().get(3));
        assertRejects(data,swapped,FECHA);
        assertThrows(NominalReadFailure.class,()->NominalTransactionTestOwner.assertJdbcBinding(data,FECHA.plusDays(1)));
        System.out.println("R3 JDBC_BINDING: PASS; wrong date/slot/day/type/omission/swap sensitivity rejected");
    }
    @Test void realPostgresForwardedBindingFaultsFailWithoutPublishingEvenWhenQueryReturnsEmpty() {
        for(var fault:NominalTransactionTestOwner.JdbcCapture.BindingFault.values()) {
            capture.installBindingFault(fault);
            var invocation=seed();
            try {
                var failure=assertThrows(NominalReadFailure.class,()->owner.inRepeatableReadOnly(invocation,FECHA),fault.toString());
                var expected=fault==NominalTransactionTestOwner.JdbcCapture.BindingFault.WRONG_DATE
                        || fault==NominalTransactionTestOwner.JdbcCapture.BindingFault.WRONG_DAY
                        ? NominalReadFailure.Category.TRANSACTION_CONTEXT_INVALID : NominalReadFailure.Category.DATABASE_READ_FAILURE;
                assertEquals(expected,failure.category());assertEquals("ABORTED",owner.state(invocation));
                var data=capture.failureObservations().get(4);
                assertThrows(NominalReadFailure.class,()->NominalTransactionTestOwner.assertJdbcBinding(data,FECHA));
                switch(fault) {
                    case WRONG_DATE -> assertEquals(FECHA.plusDays(1).toString(),data.binds().get(3).canonicalValue());
                    case WRONG_DAY -> { assertEquals("0",data.binds().get(7).canonicalValue());assertTrue(data.executeCompleted()); }
                    case WRONG_TYPE -> { assertEquals("setString",data.binds().get(3).setter());assertEquals("java.lang.String",data.binds().get(3).runtimeClass()); }
                    case WRONG_SLOT, OMIT -> assertFalse(data.binds().containsKey(3));
                    case SWAP -> {assertEquals("setShort",data.binds().get(3).setter());assertEquals("setDate",data.binds().get(7).setter());}
                }
                System.out.println("R3 actual driver sensitivity "+fault+" rejected="+failure.category()
                        +" executed="+data.executeCompleted()+" setters="+data.setterHistory());
            } finally {capture.clearBindingFault();}
        }
        assertEquals(1,owner.inRepeatableReadOnly(seed(),FECHA).readSet().candidates().size());
    }
    void assertRejects(NominalTransactionTestOwner.JdbcCapture.StatementObservation data,
            Map<Integer,NominalTransactionTestOwner.JdbcCapture.BindObservation> bindings,LocalDate date) {
        var changed=new NominalTransactionTestOwner.JdbcCapture.StatementObservation(data.ordinal(),data.sql(),bindings,
                data.setterHistory(),data.transactionOwner(),data.factory(),data.holder(),data.session(),data.connection(),
                data.physicalConnection(),data.executeEntered(),data.executeCompleted(),data.executeFailed());
        assertThrows(NominalReadFailure.class,()->NominalTransactionTestOwner.assertJdbcBinding(changed,date));
    }
    @Test void exactDateDayInclusiveOpenValidityActiveEmptyAndMultipleLogicalSeries() throws Exception {
        var first=owner.inRepeatableReadOnly(seed(),FECHA).readSet();assertEquals(1,first.candidates().size());
        assertEquals(SERIES,first.candidates().getFirst().reference().id());
        assertEquals(ASSIGNMENT,first.backing().getFirst().assignmentId());
        assertTrue(owner.inRepeatableReadOnly(seed(),FECHA.plusDays(1)).readSet().candidates().isEmpty());
        sql("UPDATE programacion_bloque SET vigente_hasta='"+FECHA+"'"+blockWhere());
        sql("UPDATE programacion_asignacion SET vigente_hasta='"+FECHA+"'"+assignmentWhere());
        assertEquals(1,owner.inRepeatableReadOnly(seed(),FECHA).readSet().candidates().size());
        assertTrue(owner.inRepeatableReadOnly(seed(),FECHA.plusDays(7)).readSet().candidates().isEmpty());
        config.resetFixtures(privileged);
        assertEquals(1,owner.inRepeatableReadOnly(seed(),FECHA.plusDays(7)).readSet().candidates().size());
        assertTrue(owner.inRepeatableReadOnly(seed(),FECHA.minusDays(7)).readSet().candidates().isEmpty());
        sql("UPDATE programacion_asignacion SET activo=false"+assignmentWhere());
        assertTrue(owner.inRepeatableReadOnly(seed(),FECHA).readSet().candidates().isEmpty());
        sql("UPDATE programacion_asignacion SET activo=true"+assignmentWhere());
        sql("UPDATE programacion_bloque SET activo=false"+blockWhere());
        assertTrue(owner.inRepeatableReadOnly(seed(),FECHA).readSet().candidates().isEmpty());
        sql("UPDATE programacion_bloque SET activo=true,dia_semana=0"+blockWhere());
        assertTrue(owner.inRepeatableReadOnly(seed(),FECHA).readSet().candidates().isEmpty());
        assertEquals(1,owner.inRepeatableReadOnly(seed(),FECHA.plusDays(6)).readSet().candidates().size());
        config.resetFixtures(privileged);
        config.insertAssignment(privileged,UUID.randomUUID(),UUID.fromString("f0000000-0000-4000-8000-000000000003"),FECHA,null,true);
        assertEquals(2,owner.inRepeatableReadOnly(seed(),FECHA).readSet().candidates().size());
    }
    @Test void preservesPostgresMicrosecondTimesAndTimestampInstants() throws Exception {
        sql("UPDATE programacion_asignacion SET hora_inicio='09:00:00.123456',hora_fin='10:00:00.654321',"
                +"creado_en='2026-09-01T12:00:00.123456Z',actualizado_en='2026-09-02T12:00:00.654321Z'"+assignmentWhere());
        sql("UPDATE programacion_bloque SET hora_inicio='08:00:00.123456',hora_fin='12:00:00.654321'"+blockWhere());
        var set=owner.inRepeatableReadOnly(seed(),FECHA).readSet();var c=set.candidates().getFirst();var b=set.backing().getFirst();
        assertEquals(LocalTime.parse("09:00:00.123456"),c.start());assertEquals(LocalTime.parse("10:00:00.654321"),c.end());
        assertEquals(LocalTime.parse("08:00:00.123456"),b.blockStart());assertEquals(LocalTime.parse("12:00:00.654321"),b.blockEnd());
        assertEquals(OffsetDateTime.parse("2026-09-01T12:00:00.123456Z"),b.assignmentCreated());
        assertEquals(OffsetDateTime.parse("2026-09-02T12:00:00.654321Z"),b.assignmentUpdated());
    }
    @Test void overlappingActiveVersionsFailEntireReadWithoutChoosingWinner() {
        config.insertAssignment(privileged,UUID.randomUUID(),SERIES,FECHA,null,true);
        var failure=assertThrows(NominalReadFailure.class,()->owner.inRepeatableReadOnly(seed(),FECHA));
        assertEquals(NominalReadFailure.Category.DUPLICATE_SERIES_ON_DATE,failure.category());
        assertEquals(4,failure.physicalIds().size());
    }
    @Test void outOfBlockAssignmentFailsWholeReadOnRealV46Data() throws Exception {
        sql("UPDATE programacion_asignacion SET hora_inicio='07:00'"+assignmentWhere());
        assertEquals(NominalReadFailure.Category.MALFORMED_PROJECTION,
                assertThrows(NominalReadFailure.class,()->owner.inRepeatableReadOnly(seed(),FECHA)).category());
    }
    @Test void DYNAMIC_SLICE_CHECKSUM_ACCEPTANCE_CHECKSUM_STABILITY() {
        var before=F2eSliceChecksum.calcularNominalDinamico(privileged,FECHA);
        assertEquals(before,F2eSliceChecksum.calcularNominalDinamico(privileged,FECHA));
        var read=owner.inRepeatableReadOnly(seed(),FECHA);assertTrue(read.completed());read.readSet();
        var after=F2eSliceChecksum.calcularNominalDinamico(privileged,FECHA);
        assertEquals(before,after);assertEquals(Map.of("public.programacion_bloque",1,"public.programacion_asignacion",1),after.rowCounts());
        assertNotEquals("0".repeat(64),after.sliceHash());assertEquals(9,read.statementIds.size());
        System.out.println("R3 CHECKSUM_STABILITY: PASS before="+before+" after="+after);
    }
    @Test void DYNAMIC_SLICE_CHECKSUM_ACCEPTANCE_CHECKSUM_SENSITIVITY() throws Exception {
        var last=F2eSliceChecksum.calcularNominalDinamico(privileged,FECHA);
        UUID newId=UUID.randomUUID();
        config.insertAssignment(privileged,newId,UUID.randomUUID(),FECHA,null,true);
        var current=F2eSliceChecksum.calcularNominalDinamico(privileged,FECHA);
        assertNotEquals(last,current);assertEquals(2,current.rowCounts().get("public.programacion_asignacion"));last=current;
        sql("DELETE FROM programacion_asignacion WHERE id='"+newId+"'");
        current=F2eSliceChecksum.calcularNominalDinamico(privileged,FECHA);assertNotEquals(last,current);last=current;
        sql("UPDATE programacion_asignacion SET hora_fin='10:30'"+assignmentWhere());
        current=F2eSliceChecksum.calcularNominalDinamico(privileged,FECHA);assertNotEquals(last,current);last=current;
        sql("UPDATE programacion_bloque SET hora_fin='12:30'"+blockWhere());
        current=F2eSliceChecksum.calcularNominalDinamico(privileged,FECHA);assertNotEquals(last,current);last=current;
        sql("UPDATE programacion_asignacion SET vigente_desde='"+FECHA.plusDays(1)+"'"+assignmentWhere());
        current=F2eSliceChecksum.calcularNominalDinamico(privileged,FECHA);assertNotEquals(last,current);
        assertEquals(0,current.rowCounts().get("public.programacion_asignacion"));last=current;
        // Applicable block without assignment must enter the dynamic slice.
        sql("INSERT INTO programacion_bloque SELECT gen_random_uuid(),gen_random_uuid(),salon_id,dia_semana,"
                +"hora_inicio,hora_fin,vigente_desde,vigente_hasta,activo,creado_en,actualizado_en FROM programacion_bloque"+blockWhere());
        current=F2eSliceChecksum.calcularNominalDinamico(privileged,FECHA);assertNotEquals(last,current);
        assertEquals(2,current.rowCounts().get("public.programacion_bloque"));last=current;
        sql("UPDATE programacion_bloque SET dia_semana=0"+blockWhere());
        current=F2eSliceChecksum.calcularNominalDinamico(privileged,FECHA);assertNotEquals(last,current);
        assertEquals(1,current.rowCounts().get("public.programacion_bloque"));
        assertNotEquals(current,F2eSliceChecksum.calcularNominalDinamico(privileged,FECHA.plusDays(1)));
        System.out.println("R3 CHECKSUM_SENSITIVITY: PASS insertion/deletion/assignment-field/block-field/nonapplicability/unassigned-block");
    }
    @Test void checksumTemporalSeriesValidityAndActiveFieldsAreMutationSensitive() throws Exception {
        for(String mutation:List.of(
                "UPDATE programacion_asignacion SET serie_id=gen_random_uuid()",
                "UPDATE programacion_asignacion SET hora_inicio='09:15'",
                "UPDATE programacion_asignacion SET hora_fin='10:15'",
                "UPDATE programacion_asignacion SET vigente_desde=vigente_desde-1",
                "UPDATE programacion_asignacion SET vigente_hasta='2026-10-05'",
                "UPDATE programacion_asignacion SET activo=false",
                "UPDATE programacion_asignacion SET creado_en=creado_en+interval '1 microsecond'",
                "UPDATE programacion_asignacion SET actualizado_en=actualizado_en+interval '1 microsecond'",
                "UPDATE programacion_bloque SET serie_id=gen_random_uuid()",
                "UPDATE programacion_bloque SET dia_semana=0",
                "UPDATE programacion_bloque SET hora_inicio='07:00'",
                "UPDATE programacion_bloque SET hora_fin='13:00'",
                "UPDATE programacion_bloque SET vigente_desde=vigente_desde-1",
                "UPDATE programacion_bloque SET vigente_hasta='2026-10-05'",
                "UPDATE programacion_bloque SET activo=false",
                "UPDATE programacion_bloque SET creado_en=creado_en+interval '1 microsecond'",
                "UPDATE programacion_bloque SET actualizado_en=actualizado_en+interval '1 microsecond'")) {
            config.resetFixtures(privileged);var before=F2eSliceChecksum.calcularNominalDinamico(privileged,FECHA);
            sql(mutation);assertNotEquals(before,F2eSliceChecksum.calcularNominalDinamico(privileged,FECHA),mutation);
        }
    }
    @Test void individualOwnerRRStaysOnSameSnapshotWhileConcurrentCommitBecomesVisibleToFreshAttempt() throws Exception {
        var observed=new CountDownLatch(1);var release=new CountDownLatch(1);
        capture.installDataBarrier(observed,release);
        var pool=Executors.newSingleThreadExecutor();
        try {
            var future=pool.submit(()->owner.inRepeatableReadOnly(seed(),FECHA));
            assertTrue(observed.await(20,TimeUnit.SECONDS));
            var before=F2eSliceChecksum.calcularNominalDinamico(privileged,FECHA);
            sql("UPDATE programacion_asignacion SET hora_fin='10:30'"+assignmentWhere());
            var after=F2eSliceChecksum.calcularNominalDinamico(privileged,FECHA);assertNotEquals(before,after);
            capture.clearBarrier();release.countDown();var first=future.get(30,TimeUnit.SECONDS);
            assertEquals(first.snapshotInitial,first.snapshotFinal);
            assertEquals(LocalTime.of(10,0),first.readSet().candidates().getFirst().end());
            assertTrue(first.jdbcObservations.stream().allMatch(o->o.physicalConnection()==first.physicalConnection));
            var fresh=owner.inRepeatableReadOnly(seed(),FECHA);
            assertEquals(LocalTime.of(10,30),fresh.readSet().candidates().getFirst().end());
            assertNotEquals(first.snapshotInitial,fresh.snapshotInitial);
            System.out.println("R3 concurrent commit: owned_initial="+first.snapshotInitial+" owned_final="+first.snapshotFinal
                    +" fresh_snapshot="+fresh.snapshotInitial+" owned_end=10:00 fresh_end=10:30");
        } finally { capture.clearBarrier();release.countDown();pool.shutdownNow(); }
    }
}
