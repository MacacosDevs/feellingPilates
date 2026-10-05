package com.feelingpilates.transicion.programacion.r5.composition;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static com.feelingpilates.transicion.programacion.r5.composition.EffectiveCompositionTestFixtures.*;
class EffectiveCompositionInvariantTest {
    @Test void emptyAllSuppressionAllOmissionAndMixedEmptyRetainCompleteInput() {
        for(var i:List.of(input(0,new int[0],0),input(2,new int[]{1,1},0),withCause(input(2,new int[]{0,2},1),1),withCause(input(2,new int[]{1,3},1),2))) {
            var r=new EffectiveProgrammingComposer().compose(i);assertTrue(r.candidates().isEmpty());assertTrue(r.backingByReference().isEmpty());
            assertSame(i,r.input());assertSame(i.envelope(),r.envelope());assertEquals(hash("F2E-R5-INPUT-V1",i),r.inputCommitment());
            long adds=i.adjustmentReadSet().backing().stream().filter(b->b.tipo().equals("ADICION")).count();
            assertEquals(i.nominalReadSet().backing().size()+adds,r.omissions().size()+r.suppressions().size());
        }
    }
    @Test void issuanceRejectsMissingExtraChangedCandidatesBackingAndCommitments() {
        var i=input(1,new int[]{2},1);var r=new EffectiveProgrammingComposer().compose(i);
        assertThrows(EffectiveCompositionFailure.class,()->EffectiveProgrammingCompositionResult.issue(i,List.of(),r.backingByReference(),r.omissions(),r.suppressions(),r.inputCommitment(),r.resultContentFingerprint(),r.resultSnapshotIdentity()));
        assertThrows(EffectiveCompositionFailure.class,()->EffectiveProgrammingCompositionResult.issue(i,r.candidates(),Map.of(),r.omissions(),r.suppressions(),r.inputCommitment(),r.resultContentFingerprint(),r.resultSnapshotIdentity()));
        for(int which=0;which<3;which++) {
            final int w=which;assertThrows(EffectiveCompositionFailure.class,()->EffectiveProgrammingCompositionResult.issue(i,r.candidates(),r.backingByReference(),r.omissions(),r.suppressions(),w==0?HASH:r.inputCommitment(),w==1?HASH:r.resultContentFingerprint(),w==2?HASH:r.resultSnapshotIdentity()));
        }
        var candidates=new ArrayList<>(r.candidates());candidates.set(0,copy(candidates.getFirst(),"salonId",id(999)));
        assertThrows(EffectiveCompositionFailure.class,()->EffectiveProgrammingCompositionResult.issue(i,candidates,r.backingByReference(),r.omissions(),r.suppressions(),r.inputCommitment(),r.resultContentFingerprint(),r.resultSnapshotIdentity()));
        var omitted=new EffectiveProgrammingComposer().compose(withCause(i,1));
        assertThrows(EffectiveCompositionFailure.class,()->EffectiveProgrammingCompositionResult.issue(omitted.input(),omitted.candidates(),omitted.backingByReference(),List.of(),omitted.suppressions(),omitted.inputCommitment(),omitted.resultContentFingerprint(),omitted.resultSnapshotIdentity()));
    }
    @Test void instructorPositiveOverlapCrossSalonRejectsButAdjacencyAndDifferentInstructorPass() {
        var a=adjustment(0,4);var b=adjustment(1,4);
        for(int micros:List.of(-1,0,1)) {
            var changed=copy(copy(b,"instructorResultadoId",a.instructorResultadoId()),"horaInicioResultado",a.horaFinResultado().plusNanos(micros*1000L));
            var i=input(List.of(),List.of(a,copy(changed,"salonResultadoId",id(2))));
            if(micros<0)EffectiveCompositionAdmissionTest.rejects(i,EffectiveCompositionFailure.Category.EFFECTIVE_SET_CONFLICT);
            else assertEquals(2,new EffectiveProgrammingComposer().compose(i).candidates().size());
        }
        var overlap=copy(copy(b,"horaInicioResultado",a.horaInicioResultado()),"horaFinResultado",a.horaFinResultado());
        assertEquals(2,new EffectiveProgrammingComposer().compose(input(List.of(),List.of(a,overlap))).candidates().size());
        var conflict=copy(overlap,"instructorResultadoId",a.instructorResultadoId());
        EffectiveCompositionAdmissionTest.rejects(input(List.of(),List.of(a,conflict)),EffectiveCompositionFailure.Category.EFFECTIVE_SET_CONFLICT);
        assertEquals(2,new EffectiveProgrammingComposer().compose(withCause(input(List.of(),List.of(a,conflict)),1)).omissions().size());
    }
}
