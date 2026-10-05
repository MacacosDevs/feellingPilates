package com.feelingpilates.transicion.programacion.r5.composition;
import org.junit.jupiter.api.Test;
import java.util.*;
import com.feelingpilates.programacion.dominio.ReferenciaOcurrencia;
import static org.junit.jupiter.api.Assertions.*;
import static com.feelingpilates.transicion.programacion.r5.composition.EffectiveCompositionTestFixtures.*;
class EffectiveCompositionPropertyTest {
    @Test void boundedExhaustiveIndependentAlgebraOracle() {
        long seed=0xF2E5L;var random=new Random(seed);int cases=0;
        for(int n=0;n<=3;n++)for(int mask=0;mask<(1<<(2*n));mask++)for(int adds=0;adds<=2;adds++)for(int cause=0;cause<=8;cause++) {
            int[] kinds=new int[n];int cancellations=0;
            for(int k=0;k<n;k++){kinds[k]=(mask>>(2*k))&3;if(kinds[k]==1)cancellations++;}
            var i=withCause(input(n,kinds,adds),cause);var r=new EffectiveProgrammingComposer().compose(i);int preliminary=n-cancellations+adds;
            assertEquals(cancellations,r.suppressions().size());assertEquals(cause==0?preliminary:0,r.candidates().size());assertEquals(cause==0?0:preliminary,r.omissions().size());
            assertEquals(n+adds,r.candidates().size()+r.omissions().size()+r.suppressions().size());
            var union=new HashSet<ReferenciaOcurrencia>();
            r.candidates().forEach(c->assertTrue(union.add(c.reference())));r.omissions().forEach(o->assertTrue(union.add(o.reference())));r.suppressions().forEach(s->assertTrue(union.add(s.reference())));
            var expected=new HashSet<ReferenciaOcurrencia>();for(int k=0;k<n;k++)expected.add(nominal(k).reference());
            for(int k=0;k<adds;k++)expected.add(new ReferenciaOcurrencia(ReferenciaOcurrencia.Tipo.AJUSTE,id(400+n+k),DATE));assertEquals(expected,union);
            for(var c:r.candidates()) {
                int index=(int)c.reference().id().getLeastSignificantBits();
                String origin=c.reference().tipo()==ReferenciaOcurrencia.Tipo.AJUSTE?"ADDITION_OCCURRENCE":kinds[index-100]==0?"RECURRENT_OCCURRENCE":"REPLACEMENT_OCCURRENCE";
                assertEquals(origin,c.candidateType().name());assertEquals(c.reference(),r.backingByReference().get(c.reference()).reference());
                assertEquals(hash("F2E-R5-CANDIDATE-V1",c.reference(),EffectiveCompositionBacking.Origin.valueOf(origin),c.salonId(),c.instructorId(),c.activityId(),c.start(),c.end(),RULE),c.candidateFingerprint());
            }
            int expectedCause=causeIndex(cause);if(cause!=0)r.omissions().forEach(o->assertEquals(EffectiveCompositionOmission.Cause.values()[expectedCause],o.cause()));
            var shuffled=new EffectiveProgrammingComposer().compose(permute(i,random));assertEquals(r.inputCommitment(),shuffled.inputCommitment());assertEquals(r.resultSnapshotIdentity(),shuffled.resultSnapshotIdentity());assertEquals(r.candidates(),shuffled.candidates());
            cases++;
        }
        assertEquals(2295,cases);System.out.println("R5_PROPERTY seed="+seed+" boundedCases="+cases);
    }
    private static int causeIndex(int cause) {return cause-1;}
    @Test void retainedFieldPerturbationsChangeInputButExecutionDoesNotChangeSemanticCandidateHash() {
        var i=input(1,new int[]{2},0);var r=new EffectiveProgrammingComposer().compose(i);
        var e=copy(i.envelope(),"invocationIdentity","SYNTHETIC_SECOND_ATTEMPT");var other=copy(i,"envelope",e);
        var t=new EffectiveProgrammingComposer().compose(other);assertEquals(r.candidates().getFirst().candidateFingerprint(),t.candidates().getFirst().candidateFingerprint());
        assertNotEquals(r.inputCommitment(),t.inputCommitment());assertNotEquals(r.resultSnapshotIdentity(),t.resultSnapshotIdentity());
        var nominalChanged=copy(nominal(0),"assignmentUpdated",nominal(0).assignmentUpdated().plusNanos(1000));
        var u=new EffectiveProgrammingComposer().compose(input(List.of(nominalChanged),List.of(adjustment(0,2))));
        assertEquals(r.candidates().getFirst().candidateFingerprint(),u.candidates().getFirst().candidateFingerprint());assertNotEquals(r.inputCommitment(),u.inputCommitment());
        assertNotEquals(new EffectiveProgrammingComposer().compose(input(1,new int[]{0},0)).candidates().getFirst().candidateFingerprint(),r.candidates().getFirst().candidateFingerprint());
    }
    @Test void everyRetainedRecordFieldIsInCanonicalPreimage() {
        var i=input(1,new int[]{3},1);var records=List.of(i,i.envelope(),i.envelope().participants().get("R3"),i.nominalReadSet().backing().getFirst(),
                i.nominalReadSet().candidates().getFirst(),i.nominalReadSet().candidates().getFirst().provenance(),i.adjustmentReadSet().backing().getFirst(),
                i.adjustmentReadSet().sources().getFirst(),i.validityEvidence().salons().get(id(2)),i.validityEvidence().salons().get(id(2)).metadata());
        int fields=0;
        for(var record:records)for(var c:record.getClass().getRecordComponents()) {
            try {var value=c.getAccessor().invoke(record);var map=new TreeMap<String,Object>();for(var f:record.getClass().getRecordComponents())map.put(f.getName(),f.getAccessor().invoke(record));
                byte[] before=oracleRecord(record.getClass().getSimpleName(),map);map.put(c.getName(),value==null?"PERTURBED":"PERTURBED_FIELD");
                assertFalse(Arrays.equals(before,oracleRecord(record.getClass().getSimpleName(),map)),c.getName());fields++;
            }catch(ReflectiveOperationException e){throw new AssertionError(e);}
        }
        assertTrue(fields>90);System.out.println("R5_RETAINED_FIELDS independentPreimageControls="+fields);
    }
}
