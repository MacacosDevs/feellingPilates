package com.feelingpilates.transicion.programacion.r5.composition;
import org.junit.jupiter.api.Test;
import java.util.*;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;
import static com.feelingpilates.transicion.programacion.r5.composition.EffectiveCompositionTestFixtures.*;
import static com.feelingpilates.transicion.programacion.r5.composition.EffectiveValidityEvidence.*;
class EffectiveCompositionImmutabilityTest {
    @Test void callerOwnedNestedBuildersAndReturnedCollectionsCannotMutateEvidence() {
        var original=input(1,new int[]{3},1);var v=original.validityEvidence();var salons=new HashMap<>(v.salons());
        var roles=new HashMap<>(v.roles());var relation=roles.values().iterator().next();var rows=new ArrayList<>(relation.rows());var keys=new ArrayList<>(relation.metadata().recordKeys());
        var id=rows.getFirst().usuarioId();roles.put(id,new Relation<>(copy(relation.metadata(),"recordKeys",keys),rows));
        var participants=new HashMap<>(original.envelope().participants());var envelope=copy(original.envelope(),"participants",participants);
        var i=copy(copy(original,"envelope",envelope),"validityEvidence",new EffectiveValidityEvidence(salons,v.instructors(),v.activities(),v.hours(),roles,v.specializations(),v.offerings()));
        var r=new EffectiveProgrammingComposer().compose(i);String before=r.inputCommitment();salons.clear();participants.clear();roles.clear();rows.clear();keys.clear();
        assertEquals(before,new EffectiveProgrammingComposer().compose(i).inputCommitment());
        assertThrows(UnsupportedOperationException.class,()->i.envelope().participants().clear());assertThrows(UnsupportedOperationException.class,()->i.validityEvidence().salons().clear());
        assertThrows(UnsupportedOperationException.class,()->i.validityEvidence().roles().get(id).rows().clear());assertThrows(UnsupportedOperationException.class,()->i.validityEvidence().roles().get(id).metadata().recordKeys().clear());
        assertThrows(UnsupportedOperationException.class,()->i.nominalReadSet().backing().clear());assertThrows(UnsupportedOperationException.class,()->i.adjustmentReadSet().sources().clear());
        assertThrows(UnsupportedOperationException.class,()->r.candidates().clear());assertThrows(UnsupportedOperationException.class,()->r.backingByReference().clear());assertThrows(UnsupportedOperationException.class,()->r.omissions().clear());assertThrows(UnsupportedOperationException.class,()->r.suppressions().clear());
        var c=r.candidates().getFirst();assertThrows(UnsupportedOperationException.class,()->c.observableFields().clear());assertThrows(UnsupportedOperationException.class,()->c.provenance().normalizedFields().clear());assertThrows(UnsupportedOperationException.class,()->c.provenance().recordIds().clear());
        var orphan=input(List.of(nominal(1)),List.of(adjustment(0,1)));var f=assertThrows(EffectiveCompositionFailure.class,()->new EffectiveProgrammingComposer().compose(orphan));
        assertThrows(UnsupportedOperationException.class,()->f.references().clear());assertThrows(UnsupportedOperationException.class,()->f.ids().clear());assertSame(orphan,f.evidence());
    }
    @Test void repeatedConcurrentCallsHaveNoSharedMutableState() throws Exception {
        var i=input(3,new int[]{0,2,3},1);var composer=new EffectiveProgrammingComposer();var expected=composer.compose(i);
        try(var pool=Executors.newFixedThreadPool(4)) {
            var jobs=new ArrayList<Future<EffectiveProgrammingCompositionResult>>();for(int k=0;k<24;k++)jobs.add(pool.submit(()->composer.compose(i)));
            for(var f:jobs) {var r=f.get(30,TimeUnit.SECONDS);assertEquals(expected.candidates(),r.candidates());assertEquals(expected.resultSnapshotIdentity(),r.resultSnapshotIdentity());assertArrayEquals(oracle(expected),oracle(r));}
        }
    }    @Test @SuppressWarnings({"rawtypes","unchecked"}) void everyCallerOwnedMapAndRelationBuilderIsFrozen() {
        var i=input(2,new int[]{0,3},1);String expected=new EffectiveProgrammingComposer().compose(i).inputCommitment();
        for(var f:EffectiveValidityEvidence.class.getRecordComponents()) {
            try {
                var builder=new LinkedHashMap((Map)f.getAccessor().invoke(i.validityEvidence()));var v=copy(i.validityEvidence(),f.getName(),builder);
                builder.clear();assertEquals(expected,new EffectiveProgrammingComposer().compose(copy(i,"validityEvidence",v)).inputCommitment());
            }catch(ReflectiveOperationException e){throw new AssertionError(e);}
        }
        var relations=new ArrayList<Relation<?>>();i.validityEvidence().hours().values().forEach(h->{relations.add(h.exceptions());relations.add(h.weekly());});
        relations.addAll(i.validityEvidence().roles().values());relations.addAll(i.validityEvidence().specializations().values());relations.addAll(i.validityEvidence().offerings().values());
        for(var relation:relations) {
            var rows=new ArrayList(relation.rows());var keys=new ArrayList<>(relation.metadata().recordKeys());
            var frozen=new Relation(copy(relation.metadata(),"recordKeys",keys),rows);byte[] before=EffectiveCompositionCanonicalizer.encode(frozen);rows.clear();keys.clear();
            assertArrayEquals(before,EffectiveCompositionCanonicalizer.encode(frozen));assertThrows(UnsupportedOperationException.class,()->frozen.rows().clear());
        }
        var nc=new ArrayList<>(i.nominalReadSet().candidates());var nb=new ArrayList<>(i.nominalReadSet().backing());
        var nominal=new com.feelingpilates.transicion.programacion.read.NominalProgrammingReadSet(nc,nb);nc.clear();nb.clear();
        assertEquals(expected,new EffectiveProgrammingComposer().compose(copy(i,"nominalReadSet",nominal)).inputCommitment());
        var as=new ArrayList<>(i.adjustmentReadSet().sources());var ab=new ArrayList<>(i.adjustmentReadSet().backing());
        var adjustment=new com.feelingpilates.transicion.programacion.r4.read.AdjustmentReadSet(as,ab);as.clear();ab.clear();
        assertEquals(expected,new EffectiveProgrammingComposer().compose(copy(i,"adjustmentReadSet",adjustment)).inputCommitment());
    }

}
