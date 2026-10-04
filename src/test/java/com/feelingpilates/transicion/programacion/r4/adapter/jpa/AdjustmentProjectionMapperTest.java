package com.feelingpilates.transicion.programacion.r4.adapter.jpa;
import com.feelingpilates.transicion.programacion.r4.adapter.jpa.mapper.*;
import com.feelingpilates.transicion.programacion.r4.adapter.jpa.projection.*;
import com.feelingpilates.transicion.programacion.r4.read.*;
import com.feelingpilates.transicion.programacion.detector.*;
import com.feelingpilates.transicion.programacion.read.ReadSnapshotIdentifiers;
import org.junit.jupiter.api.*;
import java.time.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
class AdjustmentProjectionMapperTest {
    static final LocalDate DATE=LocalDate.of(2026,10,3);
    static AdjustmentReadSnapshotContext context(String run) {
        return new AdjustmentReadSnapshotContext(run,"attempt",DATE,ZoneId.of("America/Mexico_City"),"rules","source",
                "sha256:"+"0".repeat(64),"db","public","principal","physical",
                AdjustmentReadSnapshotContext.ProjectionCatalogVersion.R4_ADJUSTMENT_V1,"invocation",
                AdjustmentReadSnapshotContext.SnapshotClaim.R4_INTERNAL_RR_TEST,"1".repeat(64),"2".repeat(64));
    }
    static Object[] values(String type,UUID id) {
        boolean cancel=type.equals("CANCELACION"),add=type.equals("ADICION");
        return new Object[]{id,type,DATE,add?null:new UUID(0,99),cancel?null:new UUID(0,11),cancel?null:new UUID(0,12),
                cancel?null:new UUID(0,13),cancel?null:LocalTime.parse("09:00:00.123456"),cancel?null:LocalTime.parse("10:00:00.654321"),
                true,OffsetDateTime.parse("2026-10-03T00:00:00.123456Z"),OffsetDateTime.parse("2026-10-02T00:00:00.654321Z")};
    }
    static AdjustmentProjectionRow row(Object[] v) {
        return new AdjustmentProjectionRow((UUID)v[0],(String)v[1],(LocalDate)v[2],(UUID)v[3],(UUID)v[4],(UUID)v[5],
                (UUID)v[6],(LocalTime)v[7],(LocalTime)v[8],(Boolean)v[9],(OffsetDateTime)v[10],(OffsetDateTime)v[11]);
    }
    private final AdjustmentProjectionMapper mapper=new AdjustmentProjectionMapper();
    @Test void threeFormsTwelveFieldsCanonicalProvenanceAndNoNominalRequirement() {
        for(String type:List.of("CANCELACION","REEMPLAZO","ADICION")) {
            var set=mapper.map(List.of(row(values(type,new UUID(0,1)))),context("run"),DATE);
            var s=set.sources().getFirst();assertEquals(12,s.observableFields().size());
            assertEquals(s.observableFields(),s.provenance().normalizedFields());assertEquals(s.observableFields(),set.backing().getFirst().normalizedFields());
            assertEquals(DetectorVocabulary.SourceSystem.NEW_DARK_LAUNCH,s.sourceSystem());
            assertEquals("NEW_"+type,s.sourceAtomType().name());assertEquals("R4_ADJUSTMENT_PROJECTION",s.provenance().ruleId());
            assertEquals(List.of(new UUID(0,1).toString()),s.provenance().recordIds());
            assertEquals(type.equals("ADICION")?"ABSENT_BY_ADJUSTMENT_FORM":new UUID(0,99).toString(),s.observableFields().get("asignacion_serie_id"));
            assertEquals("2026-10-03T00:00:00.123456Z",s.observableFields().get("creado_en"));
            assertFalse(s.observableFields().containsKey("executionProvenanceId"));
            assertThrows(UnsupportedOperationException.class,()->set.sources().clear());
            assertThrows(UnsupportedOperationException.class,()->set.backing().clear());
        }
        assertTrue(mapper.map(List.of(),context("run"),DATE).sources().isEmpty());
    }
    @Test void allMandatoryNullsShapesRangesPrecisionAndTimestampFaultsRejectWholeRead() {
        for(String type:List.of("CANCELACION","REEMPLAZO","ADICION")) {
            Object[] valid=values(type,new UUID(0,2));
            for(int i=0;i<12;i++) {
                Object[] v=valid.clone();if(v[i]==null) v[i]=i==7||i==8?LocalTime.NOON:new UUID(0,4);else v[i]=null;
                final Object[] fault=v;
                var e=assertThrows(AdjustmentReadFailure.class,()->mapper.map(Arrays.asList(row(valid),row(fault)),context("run"),DATE));
                assertEquals(AdjustmentReadFailure.Category.MALFORMED_PROJECTION,e.category(),type+":"+i);
            }
        }
        for(Object[] mutation:List.of(new Object[]{1,"UNKNOWN"},new Object[]{2,DATE.plusDays(1)},new Object[]{9,false},
                new Object[]{7,LocalTime.parse("10:00:00.654321")},new Object[]{8,LocalTime.parse("08:00:00")},
                new Object[]{7,LocalTime.of(9,0,0,1)},new Object[]{10,OffsetDateTime.MAX},
                new Object[]{11,OffsetDateTime.parse("2026-10-03T00:00:00.000000001Z")})) {
            Object[] v=values("ADICION",new UUID(0,2));v[(Integer)mutation[0]]=mutation[1];
            assertEquals(AdjustmentReadFailure.Category.MALFORMED_PROJECTION,assertThrows(AdjustmentReadFailure.class,
                    ()->mapper.map(List.of(row(v)),context("run"),DATE)).category());
        }
        assertThrows(AdjustmentReadFailure.class,()->mapper.map(Arrays.asList((AdjustmentProjectionRow)null),context("run"),DATE));
        assertThrows(AdjustmentReadFailure.class,()->mapper.map(null,context("run"),DATE));
    }
    @Test void physicalDuplicatesContradictionEqualTargetsAndUnsignedOrderingFailClosed() {
        var cancel=row(values("CANCELACION",new UUID(0,1)));
        for(String other:List.of("CANCELACION","REEMPLAZO")) {
            assertEquals(AdjustmentReadFailure.Category.DUPLICATE_ACTIVE_TARGET_ON_DATE,assertThrows(AdjustmentReadFailure.class,
                    ()->mapper.map(List.of(cancel,row(values(other,new UUID(0,2)))),context("run"),DATE)).category());
        }
        var add=row(values("ADICION",new UUID(0,1)));
        assertEquals(AdjustmentReadFailure.Category.CARDINALITY_OR_BACKING_MISMATCH,assertThrows(AdjustmentReadFailure.class,
                ()->mapper.map(List.of(add,add),context("run"),DATE)).category());
        var high=row(values("ADICION",new UUID(Long.MIN_VALUE,1)));
        assertEquals(2,mapper.map(List.of(add,high),context("run"),DATE).sources().size());
        assertThrows(AdjustmentReadFailure.class,()->mapper.map(List.of(high,add),context("run"),DATE));
        Object[] bad=values("ADICION",new UUID(0,3));bad[9]=false;
        assertEquals(AdjustmentReadFailure.Category.MALFORMED_PROJECTION,assertThrows(AdjustmentReadFailure.class,
                ()->mapper.map(List.of(cancel,cancel,row(bad)),context("run"),DATE)).category());
    }
    @Test void fingerprintSensitivityForEachPhysicalFieldAndExecutionOnlySnapshotChanges() {
        Object[] base=values("REEMPLAZO",new UUID(0,1));var original=mapper.map(List.of(row(base)),context("run"),DATE).sources().getFirst();
        for(int slot:new int[]{0,3,4,5,6,7,8,10,11}) {
            Object[] v=base.clone();Object o=v[slot];v[slot]=o instanceof UUID u?new UUID(u.getMostSignificantBits(),u.getLeastSignificantBits()+1):
                    o instanceof LocalTime t?t.plusNanos(1000):((OffsetDateTime)o).plusNanos(1000);
            assertNotEquals(original.sourceFingerprint(),mapper.map(List.of(row(v)),context("run"),DATE).sources().getFirst().sourceFingerprint());
        }
        var same=mapper.map(List.of(row(base)),context("run2"),DATE).sources().getFirst();
        assertEquals(original.sourceFingerprint(),same.sourceFingerprint());assertNotEquals(original.snapshotIdentity(),same.snapshotIdentity());
        assertEquals(original,mapper.map(List.of(row(base)),context("run"),DATE).sources().getFirst());
        assertNotEquals(original.sourceFingerprint(),mapper.map(List.of(row(values("CANCELACION",new UUID(0,1)))),context("run"),DATE).sources().getFirst().sourceFingerprint());
        var set=mapper.map(List.of(row(base)),context("run"),DATE);
        assertThrows(AdjustmentReadFailure.class,()->new AdjustmentReadSet(set.sources(),List.of()));
        Object[] changed=base.clone();changed[4]=new UUID(0,99);
        var changedSet=mapper.map(List.of(row(changed)),context("run"),DATE);
        assertThrows(AdjustmentReadFailure.class,()->new AdjustmentReadSet(set.sources(),changedSet.backing()));
    }
    @Test void exactExecutionFramingAndInvalidContextText() {
        var c=context("run");assertEquals(AdjustmentReadSnapshotContext.hash("F2E-R4-EXECUTION-V1","run","attempt",
                "R4_ACTIVE_ADJUSTMENTS_ON_DATE_V1/2026-10-03","America/Mexico_City","rules","source","sha256:"+"0".repeat(64),
                "db","public","principal","physical","R4_ADJUSTMENT_V1","invocation","R4_INTERNAL_RR_TEST","1".repeat(64),"2".repeat(64)),c.executionProvenanceId());
        for(String text:Arrays.asList(null,""," ","bad\0","\uD800")) assertThrows(AdjustmentReadFailure.class,()->context(text));
        assertNotEquals(AdjustmentReadSnapshotContext.hash("ab","c"),AdjustmentReadSnapshotContext.hash("a","bc"));
    }
}
