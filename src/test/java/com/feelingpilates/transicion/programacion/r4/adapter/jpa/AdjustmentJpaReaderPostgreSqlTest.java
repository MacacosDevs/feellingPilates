package com.feelingpilates.transicion.programacion.r4.adapter.jpa;

import com.feelingpilates.transicion.programacion.r4.adapter.jpa.testinfra.*;
import com.feelingpilates.transicion.programacion.r4.read.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import javax.sql.DataSource;
import java.sql.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;
import static com.feelingpilates.transicion.programacion.r4.adapter.jpa.testinfra.AdjustmentPostgresTestConfiguration.*;

@SpringJUnitConfig(AdjustmentPostgresTestConfiguration.class)
class AdjustmentJpaReaderPostgreSqlTest {
    @Autowired AdjustmentTransactionTestOwner owner;
    @Autowired AdjustmentPostgresTestConfiguration fixture;
    @Autowired AdjustmentJdbcCapture jdbc;
    @Autowired @Qualifier("r4Admin") DataSource admin;
    @Autowired @Qualifier("r4DS") DataSource readerDS;
    @Autowired AdjustmentTransactionTestOwner.Descriptor descriptor;
    @BeforeEach void reset() { jdbc.fault(null);jdbc.beforeData(null);fixture.reset(admin); }
    @AfterEach void clear() { jdbc.fault(null);jdbc.beforeData(null); }
    @Test void realFlyway50V47ScalarPrecisionPhysicalJdbcManifestAndStableNoWriteChecksum() {
        var checksum=new AdjustmentSliceChecksum(admin);String before=checksum.onDate(FECHA);
        var schemaBefore=AdjustmentTransactionTestOwner.schemaFingerprint(readSchema(admin));
        var outcome=owner.read(AdjustmentTransactionTestOwner.Seed.fresh(),FECHA);
        assertEquals(3,outcome.set().sources().size());assertTrue(outcome.completion().committed);
        assertEquals(List.of(CANCEL,REPLACE,ADD),outcome.set().backing().stream().map(AdjustmentBackingSnapshot::id).toList());
        assertEquals("09:00:00.123456",outcome.set().sources().get(1).observableFields().get("hora_inicio_resultado"));
        var cap=jdbc.last();assertTrue(cap.success);assertEquals(17,cap.statements.size());assertEquals(2,cap.named.size());
        var data=cap.statements.get(8);assertEquals("setDate",data.binds.get(1).setter());assertEquals(FECHA.toString(),data.binds.get(1).value());
        assertEquals("setBoolean",data.binds.get(2).setter());assertEquals("true",data.binds.get(2).value());
        assertTrue(cap.statements.stream().allMatch(s->s.physical==cap.physical()&&s.completed&&!s.failed));
        assertEquals(6,cap.metadata.size());assertTrue(cap.lifecycle.stream().anyMatch(s->s.startsWith("commit:")));
        assertEquals(before,checksum.onDate(FECHA));assertEquals(schemaBefore,AdjustmentTransactionTestOwner.schemaFingerprint(readSchema(admin)));
        assertEquals(descriptor.fingerprint(),schemaBefore);assertEquals(50,readSchema(admin).getFirst().size());
        System.out.println("R4_REAL_PG_FLYWAY50_HEAD47_CHECKSUM=-1624594375 schema="+schemaBefore+" slice="+before);
        for(var statement:cap.statements) System.out.println("R4_RAW_JDBC "+statement.id+" sql="+statement.sql+" binds="+statement.setters+" nativeIdentity="+System.identityHashCode(statement.physical));
        System.out.println("R4_RAW_METADATA "+cap.metadata+" lifecycle="+cap.lifecycle);
        System.out.println("R4_JDBC date="+data.binds.get(1)+" active="+data.binds.get(2)+" statements="+cap.statements.stream().map(s->s.id).toList());
    }
    @Test void exactDateActivePredicateEmptyAndTwoIdenticalDimensionAdditionsSurvive() {
        fixture.insert(admin,new UUID(0,7),"ADICION",FECHA.plusDays(1),null,true);
        fixture.insert(admin,new UUID(0,8),"ADICION",FECHA,null,false);
        fixture.insert(admin,new UUID(0,9),"ADICION",FECHA,null,true);
        var set=owner.read(AdjustmentTransactionTestOwner.Seed.fresh(),FECHA).set();assertEquals(4,set.sources().size());
        assertEquals(2,set.backing().stream().filter(b->b.tipo().equals("ADICION")).count());
        assertTrue(owner.read(AdjustmentTransactionTestOwner.Seed.fresh(),FECHA.plusDays(2)).set().sources().isEmpty());
        assertEquals(17,jdbc.last().statements.size());
    }
    @Test void selectOnlyPrivilegesAndSeparateNonReadOnly42501Controls() throws Exception {
        try(var c=admin.getConnection();var q=c.createStatement()) { q.execute("CREATE SEQUENCE r4_denied_sequence"); }
        try {
            try(var c=readerDS.getConnection();var q=c.createStatement();var r=q.executeQuery(
                    "SELECT has_table_privilege(current_user,'programacion_ajuste_fecha','SELECT'),has_table_privilege(current_user,'flyway_schema_history','SELECT'),"
                    +"has_table_privilege(current_user,'reserva','SELECT'),has_table_privilege(current_user,'programacion_asignacion','SELECT'),"
                    +"has_table_privilege(current_user,'turno_instructor','SELECT'),has_table_privilege(current_user,'salon','SELECT'),"
                    +"has_table_privilege(current_user,'programacion_ajuste_fecha','INSERT,UPDATE,DELETE,TRUNCATE'),has_schema_privilege(current_user,'public','CREATE')")) {
                assertTrue(r.next());assertTrue(r.getBoolean(1));assertTrue(r.getBoolean(2));
                for(int i=3;i<=8;i++) assertFalse(r.getBoolean(i));
            }
            for(String sql:List.of("INSERT INTO programacion_ajuste_fecha(tipo,fecha) VALUES ('ADICION',CURRENT_DATE)",
                    "UPDATE programacion_ajuste_fecha SET activo=false","DELETE FROM programacion_ajuste_fecha","TRUNCATE programacion_ajuste_fecha",
                    "ALTER TABLE programacion_ajuste_fecha ADD COLUMN forbidden integer","DROP TABLE programacion_ajuste_fecha",
                    "CREATE TABLE public.r4_forbidden(id integer)","SELECT nextval('r4_denied_sequence')")) {
                try(var c=readerDS.getConnection();var q=c.createStatement()) {
                    assertFalse(c.isReadOnly());var e=assertThrows(SQLException.class,()->q.execute(sql));assertEquals("42501",e.getSQLState(),sql);
                    System.out.println("R4_NO_WRITE_SQLSTATE42501 "+sql);
                }
            }
            try(var c=admin.getConnection()) {
                c.setReadOnly(true);c.setAutoCommit(false);
                try(var q=c.createStatement()) { assertEquals("25006",assertThrows(SQLException.class,()->q.execute("UPDATE programacion_ajuste_fecha SET activo=false")).getSQLState()); }
                c.rollback();
            }
        } finally { sql(admin,"DROP SEQUENCE r4_denied_sequence"); }
    }
    @Test void sixActualJdbcBindingFaultControlsFailBeforeSuccessfulDataAndPreserveChecksum() {
        var checksum=new AdjustmentSliceChecksum(admin);String before=checksum.onDate(FECHA);
        for(var fault:List.of(AdjustmentJdbcCapture.Fault.WRONG_DATE,AdjustmentJdbcCapture.Fault.WRONG_SLOT,
                AdjustmentJdbcCapture.Fault.SWAPPED,AdjustmentJdbcCapture.Fault.OMITTED,AdjustmentJdbcCapture.Fault.WRONG_TYPE,AdjustmentJdbcCapture.Fault.ACTIVE_FALSE)) {
            jdbc.fault(fault);var e=assertThrows(AdjustmentReadFailure.class,()->owner.read(AdjustmentTransactionTestOwner.Seed.fresh(),FECHA));
            assertEquals(AdjustmentReadFailure.Category.DATABASE_READ_FAILURE,e.category());assertFalse(jdbc.last().success);
            assertTrue(jdbc.last().statements.size()>=9);assertEquals(before,checksum.onDate(FECHA));
            System.out.println("R4_JDBC_FAULT "+fault+" => "+e.category()+" setters="+jdbc.last().statements.get(8).setters);
        }
    }
    @Test void dynamicMembershipAndEveryApplicableMutableProjectedFieldAreSensitive() {
        var checksum=new AdjustmentSliceChecksum(admin);String initial=checksum.onDate(FECHA);assertEquals(initial,checksum.onDate(FECHA));
        fixture.insert(admin,new UUID(0,77),"ADICION",FECHA,null,true);assertNotEquals(initial,checksum.onDate(FECHA));
        sql(admin,"DELETE FROM programacion_ajuste_fecha WHERE id='00000000-0000-0000-0000-00000000004d'");assertEquals(initial,checksum.onDate(FECHA));
        for(String change:List.of("activo=false","fecha=fecha+1","id='90000000-0000-0000-0000-000000000044'",
                "hora_inicio_resultado='09:01:00.123456'","hora_fin_resultado='10:01:00.654321'",
                "creado_en=creado_en+interval '1 microsecond'","actualizado_en=actualizado_en+interval '1 microsecond'",
                "tipo='CANCELACION',salon_resultado_id=NULL,instructor_resultado_id=NULL,tipo_actividad_resultado_id=NULL,hora_inicio_resultado=NULL,hora_fin_resultado=NULL",
                "asignacion_serie_id='10000000-0000-0000-0000-000000000088'")) {
            fixture.reset(admin);String before=checksum.onDate(FECHA);
            sql(admin,"UPDATE programacion_ajuste_fecha SET "+change+" WHERE id='"+REPLACE+"'");assertNotEquals(before,checksum.onDate(FECHA),change);
        }
        fixture.reset(admin);sql(admin,"UPDATE programacion_ajuste_fecha SET fecha=fecha+1 WHERE id='"+ADD+"'");
        String outside=checksum.onDate(FECHA);sql(admin,"UPDATE programacion_ajuste_fecha SET fecha=fecha-1 WHERE id='"+ADD+"'");
        assertNotEquals(outside,checksum.onDate(FECHA));
        // Independent alternate FK values are inserted by privileged fixture controls.
        for(String column:List.of("salon_resultado_id","instructor_resultado_id","tipo_actividad_resultado_id")) {
            fixture.reset(admin);String before=checksum.onDate(FECHA);
            String table=column.equals("salon_resultado_id")?"salon":column.equals("instructor_resultado_id")?"usuario":"tipo_actividad";
            UUID old=column.equals("salon_resultado_id")?fixture.salon():column.equals("instructor_resultado_id")?fixture.instructor():fixture.activity();
            UUID alternate=alternate(admin,table,old);
            sql(admin,"UPDATE programacion_ajuste_fecha SET "+column+"='"+alternate+"' WHERE id='"+REPLACE+"'");
            assertNotEquals(before,checksum.onDate(FECHA),column);
        }
        System.out.println("R4_DYNAMIC_CHECKSUM_STABILITY_SENSITIVITY_PASS "+initial);
    }
    static UUID alternate(DataSource ds,String table,UUID old) {
        try(var c=ds.getConnection();var q=c.prepareStatement("SELECT id FROM "+table+" WHERE id<>? ORDER BY id LIMIT 1")) {
            q.setObject(1,old);try(var r=q.executeQuery()) { if(r.next()) return r.getObject(1,UUID.class); }
        } catch(SQLException e) { throw new IllegalStateException(e); }
        throw new IllegalStateException("alternate master fixture absent: "+table);
    }
    @Test void concurrentCommitDoesNotChangeIndividualRepeatableReadSnapshot() throws Exception {
        for(String mutation:List.of("INSERT","UPDATE","DEACTIVATE")) {
            fixture.reset(admin);var observed=new CountDownLatch(1);var committed=new CountDownLatch(1);
            jdbc.beforeData(()->{ observed.countDown();try { if(!committed.await(20,TimeUnit.SECONDS)) throw new IllegalStateException("commit barrier timeout"); }
                catch(InterruptedException e) { Thread.currentThread().interrupt();throw new IllegalStateException(e); } });
            var pool=Executors.newSingleThreadExecutor();
            try {
                Future<AdjustmentTransactionTestOwner.Outcome> read=pool.submit(()->owner.read(AdjustmentTransactionTestOwner.Seed.fresh(),FECHA));
                assertTrue(observed.await(20,TimeUnit.SECONDS));
                if(mutation.equals("INSERT")) fixture.insert(admin,new UUID(0,77),"ADICION",FECHA,null,true);
                else sql(admin,"UPDATE programacion_ajuste_fecha SET "+(mutation.equals("UPDATE")?"actualizado_en=actualizado_en+interval '1 second'":"activo=false")+" WHERE id='"+ADD+"'");
                committed.countDown();var original=read.get(25,TimeUnit.SECONDS);assertEquals(3,original.set().sources().size());
                assertEquals(OffsetDateTime.parse("2026-10-02T06:00:00.654321Z"),original.set().backing().getLast().actualizadoEn());
                jdbc.beforeData(null);var fresh=owner.read(AdjustmentTransactionTestOwner.Seed.fresh(),FECHA);
                if(mutation.equals("INSERT")) assertEquals(4,fresh.set().sources().size());
                else if(mutation.equals("DEACTIVATE")) assertEquals(2,fresh.set().sources().size());
                else assertNotEquals(original.set().backing().getLast().actualizadoEn(),fresh.set().backing().getLast().actualizadoEn());
                assertNotEquals(original.context().snapshotEvidenceId(),fresh.context().snapshotEvidenceId());
                System.out.println("R4_RR_CONCURRENT_COMMIT "+mutation+" original="+original.set().sources().size()+" fresh="+fresh.set().sources().size());
            } finally { committed.countDown();jdbc.beforeData(null);pool.shutdownNow(); }
        }
    }
    static void sql(DataSource ds,String sql) {
        try(var c=ds.getConnection();var q=c.createStatement()) { q.execute(sql); }
        catch(SQLException e) { throw new IllegalStateException(e); }
    }
}
