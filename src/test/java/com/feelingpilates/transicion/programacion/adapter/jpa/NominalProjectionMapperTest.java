package com.feelingpilates.transicion.programacion.adapter.jpa;

import com.feelingpilates.transicion.programacion.adapter.jpa.mapper.NominalProjectionMapper;
import com.feelingpilates.transicion.programacion.adapter.jpa.projection.*;
import com.feelingpilates.transicion.programacion.adapter.jpa.testinfra.*;
import com.feelingpilates.transicion.programacion.read.*;
import com.feelingpilates.programacion.dominio.ReferenciaOcurrencia;
import org.junit.jupiter.api.*;
import java.time.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class NominalProjectionMapperTest {
    static final LocalDate DATE=LocalDate.of(2026,9,28);
    static final NominalProjectionCatalog CATALOG=NominalProjectionCatalog.R3_NOMINAL_V1;
    final NominalProjectionMapper mapper=new NominalProjectionMapper(CATALOG);
    final NominalProjectionQueryExecutor executor=new NominalProjectionQueryExecutor(
            org.mockito.Mockito.mock(jakarta.persistence.EntityManager.class),CATALOG);
    static NominalReadSnapshotContext context() {
        String hash="0".repeat(64);
        return new NominalReadSnapshotContext("run","attempt",DATE,ZoneId.of("America/Mexico_City"),"rules-v1",
                "fixture:postgres16:r3:unit","sha256:"+hash,"db","public","reader","physical",CATALOG.version(),
                "invocation",NominalReadSnapshotContext.SnapshotClaim.R3_INTERNAL_RR_TEST,hash,hash);
    }
    static Object[] values() {
        UUID series=UUID.fromString("30000000-0000-4000-8000-000000000003");
        UUID assignment=UUID.fromString("30000000-0000-4000-8000-000000000002");
        UUID block=UUID.fromString("30000000-0000-4000-8000-000000000001");
        var timestamp=OffsetDateTime.parse("2026-09-01T12:00:00Z");
        return new Object[]{series,assignment,block,block,UUID.randomUUID(),UUID.randomUUID(),UUID.randomUUID(),UUID.randomUUID(),
                LocalTime.of(9,0),LocalTime.of(10,0),LocalTime.of(8,0),LocalTime.of(12,0),DATE,null,true,
                DATE,null,true,timestamp,timestamp,timestamp,timestamp,(short)1};
    }
    NominalProjectionRow row(Object[] values) { return executor.materializar(Collections.singletonList(values),DATE).getFirst(); }
    NominalProgrammingReadSet map(Object[] values) { return mapper.mapear(List.of(row(values)),context(),DATE); }
    @Test void nominalIdentityUsesAssignmentSeriesAndExactDateWithCompleteBacking() {
        Object[] v=values(); var set=map(v); var c=set.candidates().getFirst(); var b=set.backing().getFirst();
        assertEquals(new ReferenciaOcurrencia(ReferenciaOcurrencia.Tipo.SERIE_ASIGNACION,(UUID)v[0],DATE),c.reference());
        assertEquals(c.reference(),b.reference()); assertEquals(v[1],b.assignmentId());assertEquals(v[4],b.blockSeriesId());
        assertEquals(v[5],c.salonId()); assertEquals(v[6],c.instructorId());assertEquals(v[7],c.activityId());
        assertEquals(v[10],b.blockStart()); assertNull(b.assignmentUntil());
        assertEquals(28,c.provenance().normalizedFields().size());
        assertThrows(UnsupportedOperationException.class,()->set.candidates().clear());
        assertThrows(UnsupportedOperationException.class,()->set.backing().clear());
        assertThrows(UnsupportedOperationException.class,()->c.provenance().normalizedFields().clear());
    }
    @Test void everyRequiredNullAndEveryUnexpectedScalarTypeFailWholeRead() {
        Set<Integer> optional=Set.of(13,16);
        for(int slot=0;slot<23;slot++) {
            Object[] v=values(); if(!optional.contains(slot)) {
                v[slot]=null; assertEquals(NominalReadFailure.Category.MALFORMED_PROJECTION,
                        assertThrows(NominalReadFailure.class,()->map(v)).category(),"null slot "+slot);
            }
            Object[] wrong=values();wrong[slot]=new Object();
            assertEquals(NominalReadFailure.Category.MALFORMED_PROJECTION,
                    assertThrows(NominalReadFailure.class,()->row(wrong)).category(),"type slot "+slot);
        }
        assertThrows(NominalReadFailure.class,()->executor.materializar(List.of("not a projection"),DATE));
        assertThrows(NominalReadFailure.class,()->executor.materializar(Collections.singletonList(new Object[22]),DATE));
    }
    @Test void inclusivityOpenEndsPositiveContainmentAndAllApplicabilityFailures() {
        Object[] inclusive=values();inclusive[13]=DATE;inclusive[16]=DATE;assertEquals(1,map(inclusive).candidates().size());
        int[] slots={8,9,10,11,12,13,14,15,16,17,22,2};
        Object[] invalid={LocalTime.of(7,0),LocalTime.of(9,0),LocalTime.of(12,0),LocalTime.of(9,30),
                DATE.plusDays(1),DATE.minusDays(1),false,DATE.plusDays(1),DATE.minusDays(1),false,(short)0,UUID.randomUUID()};
        for(int i=0;i<slots.length;i++) {
            Object[] v=values();v[slots[i]]=invalid[i];
            assertEquals(NominalReadFailure.Category.MALFORMED_PROJECTION,
                    assertThrows(NominalReadFailure.class,()->map(v)).category(),"invalid slot "+slots[i]);
        }
        Object[] exact=values();exact[8]=exact[10];exact[9]=exact[11];assertEquals(1,map(exact).candidates().size());
    }
    @Test void zeroMultipleUnsignedOrderAndDuplicateSeriesDiscardEntireResult() {
        assertTrue(mapper.mapear(List.of(),context(),DATE).candidates().isEmpty());
        Object[] a=values(),b=values(); b[0]=UUID.fromString("f0000000-0000-4000-8000-000000000003");b[1]=UUID.randomUUID();b[2]=UUID.randomUUID();b[3]=b[2];
        var set=mapper.mapear(List.of(row(a),row(b)),context(),DATE);assertEquals(2,set.candidates().size());
        assertEquals(NominalReadFailure.Category.CARDINALITY_OR_BACKING_MISMATCH,
                assertThrows(NominalReadFailure.class,()->mapper.mapear(List.of(row(b),row(a)),context(),DATE)).category());
        b[0]=a[0];
        var failure=assertThrows(NominalReadFailure.class,()->mapper.mapear(List.of(row(a),row(b)),context(),DATE));
        assertEquals(NominalReadFailure.Category.DUPLICATE_SERIES_ON_DATE,failure.category());
        assertEquals(4,failure.physicalIds().size());
    }
    @Test void inconsistentPhysicalVersionsAndNaiveTimestampRejectEntireRead() {
        Object[] a=values(),b=a.clone();b[0]=UUID.fromString("f0000000-0000-4000-8000-000000000003");
        assertEquals(NominalReadFailure.Category.CARDINALITY_OR_BACKING_MISMATCH,
                assertThrows(NominalReadFailure.class,()->mapper.mapear(List.of(row(a),row(b)),context(),DATE)).category());
        b[1]=UUID.randomUUID();b[4]=UUID.randomUUID();
        assertEquals(NominalReadFailure.Category.CARDINALITY_OR_BACKING_MISMATCH,
                assertThrows(NominalReadFailure.class,()->mapper.mapear(List.of(row(a),row(b)),context(),DATE)).category());
        Object[] naive=values();naive[18]=LocalDateTime.of(2026,9,1,12,0);
        assertThrows(NominalReadFailure.class,()->row(naive));
    }
    @Test void allPhysicalRowsValidateBeforeDuplicateClassification() {
        Object[] a=values(),duplicate=a.clone(),malformed=values();malformed[5]=null;
        assertEquals(NominalReadFailure.Category.MALFORMED_PROJECTION,
                assertThrows(NominalReadFailure.class,()->mapper.mapear(List.of(row(a),row(duplicate),row(malformed)),context(),DATE)).category());
    }
    @Test void readSetEnforcesBijectionAndDefensiveCopies() {
        var set=map(values());
        assertEquals(NominalReadFailure.Category.CARDINALITY_OR_BACKING_MISMATCH,
                assertThrows(NominalReadFailure.class,()->new NominalProgrammingReadSet(set.candidates(),List.of())).category());
        assertThrows(NominalReadFailure.class,()->new NominalProgrammingReadSet(
                List.of(set.candidates().getFirst(),set.candidates().getFirst()),
                List.of(set.backing().getFirst(),set.backing().getFirst())));
        var candidates=new ArrayList<>(set.candidates()); var backing=new ArrayList<>(set.backing());
        var copy=new NominalProgrammingReadSet(candidates,backing);candidates.clear();backing.clear();
        assertEquals(1,copy.candidates().size());
    }
    @Test void invalidCallerScopeAndStrictUtf8FailBeforeSql() {
        assertEquals(NominalReadFailure.Category.INVALID_INPUT,
                assertThrows(NominalReadFailure.class,()->mapper.mapear(List.of(),context(),DATE.plusDays(1))).category());
        var c=context();
        for(String run:new String[]{"", "\u0000", "\uD800"})
            assertThrows(NominalReadFailure.class,()->new NominalReadSnapshotContext(run,c.attemptIdentity(),c.fecha(),c.businessZone(),
                    c.ruleCatalogVersion(),c.sourceName(),c.schemaFingerprint(),c.databaseName(),c.schemaName(),c.principal(),
                    c.physicalResourceIdentity(),c.projectionCatalogVersion(),c.readerInvocationIdentity(),c.snapshotClaim(),
                    c.snapshotEvidenceId(),c.statementCaptureCommitment()));
    }
}
