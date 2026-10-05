package com.feelingpilates.transicion.programacion.r5.composition;
import org.junit.jupiter.api.Test;
import java.util.*;
import com.feelingpilates.programacion.dominio.ReferenciaOcurrencia;
import static org.junit.jupiter.api.Assertions.*;
import static com.feelingpilates.transicion.programacion.r5.composition.EffectiveCompositionTestFixtures.*;
class EffectiveProgrammingComposerTest {
    @Test void fourSemanticsRetainExactIdentityDimensionsAndOriginalBacking() {
        for(int kind=0;kind<=3;kind++) {
            var i=input(1,new int[]{kind},1);var r=new EffectiveProgrammingComposer().compose(i);
            assertEquals(kind==1?1:2,r.candidates().size());assertEquals(kind==1?1:0,r.suppressions().size());assertTrue(r.omissions().isEmpty());
            var addition=r.candidates().getLast();assertEquals(ReferenciaOcurrencia.Tipo.AJUSTE,addition.reference().tipo());assertEquals(id(401),addition.reference().id());
            var ab=r.backingByReference().get(addition.reference());assertEquals(EffectiveCompositionBacking.NominalAxis.NOT_APPLICABLE,ab.nominalAxis());assertNull(ab.nominalBacking());
            assertEquals(i.adjustmentReadSet().backing().getLast(),ab.adjustmentBacking());
            if(kind!=1) {
                var c=r.candidates().getFirst();assertEquals(i.nominalReadSet().candidates().getFirst().reference(),c.reference());
                assertEquals(kind==0?"RECURRENT_OCCURRENCE":"REPLACEMENT_OCCURRENCE",c.candidateType().name());
                var b=r.backingByReference().get(c.reference());assertEquals(i.nominalReadSet().backing().getFirst(),b.nominalBacking());
                assertEquals(i.nominalReadSet().candidates().getFirst(),b.nominalCandidate());
                var expected=kind==0?nominal(0):null;var a=kind==0?null:adjustment(0,kind);
                assertEquals(kind==0?expected.salonId():a.salonResultadoId(),c.salonId());assertEquals(kind==0?expected.instructorId():a.instructorResultadoId(),c.instructorId());
                assertEquals(kind==0?expected.activityId():a.tipoActividadResultadoId(),c.activityId());assertEquals(kind==0?expected.assignmentStart():a.horaInicioResultado(),c.start());
                assertEquals(kind==0?expected.assignmentEnd():a.horaFinResultado(),c.end());
            } else {var s=r.suppressions().getFirst();assertEquals(nominal(0),s.nominalBacking());assertEquals(adjustment(0,1),s.adjustmentBacking());}
            assertSame(i,r.input());assertEquals(EffectiveCompositionEnvelope.Mode.SYNTHETIC_DESIGN_FIXTURE,r.evidenceMode());
        }
    }
    @Test void cancellationNeedsNoCancelledNominalMasters() {
        var i=input(1,new int[]{1},0);assertTrue(i.validityEvidence().salons().isEmpty());
        var r=new EffectiveProgrammingComposer().compose(i);assertTrue(r.candidates().isEmpty());assertEquals(1,r.suppressions().size());
    }
    @Test void orphanTargetRejectsEntireUnitWithOriginalEvidence() {
        for(int kind:List.of(1,2,3)) {
            var i=input(List.of(nominal(1)),List.of(adjustment(0,kind)));
            var f=assertThrows(EffectiveCompositionFailure.class,()->new EffectiveProgrammingComposer().compose(i));
            assertEquals(EffectiveCompositionFailure.Category.TARGET_NOMINAL_MISSING,f.category());assertSame(i,f.evidence());
            assertTrue(f.references().stream().anyMatch(r->r.id().equals(id(100))));assertEquals(DATE,f.date());
        }
    }
    @Test void identicalDistinctAdditionsConflictOnlyAfterRealOmissionsAreRemoved() {
        var a=adjustment(0,4);var b=copy(a,"id",id(401));var i=input(List.of(),List.of(a,b));
        assertEquals(EffectiveCompositionFailure.Category.EFFECTIVE_SET_CONFLICT,assertThrows(EffectiveCompositionFailure.class,()->new EffectiveProgrammingComposer().compose(i)).category());
        var r=new EffectiveProgrammingComposer().compose(withCause(i,1));assertEquals(2,r.omissions().size());assertNotEquals(r.omissions().getFirst().reference(),r.omissions().getLast().reference());
    }
}
