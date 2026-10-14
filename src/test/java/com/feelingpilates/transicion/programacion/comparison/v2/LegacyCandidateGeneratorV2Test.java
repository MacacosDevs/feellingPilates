package com.feelingpilates.transicion.programacion.comparison.v2;

import java.util.*;
import org.junit.jupiter.api.Test;
import com.feelingpilates.transicion.programacion.detector.*;
import static org.junit.jupiter.api.Assertions.*;
import static com.feelingpilates.transicion.programacion.comparison.v2.ComparisonTestFixturesV2.*;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.*;
class LegacyCandidateGeneratorV2Test {
    @Test void cancellationKeepsTwoNominalsDespiteDifferentActivityAndRange() {
        var a=nominal(0);var b=copy(nominal(1),"instructorId",id(20));b=copy(b,"activityId",id(4));
        var i=comparison(input(List.of(a,b),List.of()),legacy("CANCELACION",0,1));
        var batch=new ComparisonCompatibilityV2().compare(i);var e=batch.evaluations().getFirst();var r=e.coreResult().orElseThrow();
        assertEquals(2,r.candidateCount());assertEquals(MappingStatus.MULTIPLE_CANDIDATES,r.mappingStatus());
        assertEquals(ResultStatus.UNSUPPORTED,r.resultStatus());assertEquals(UnsupportedReason.UNKNOWN_INTENT,r.unsupportedReason().orElseThrow());
        assertEquals(AmbiguityReason.UNKNOWN_INTENT_MULTIPLE_PLAUSIBLE_INTERPRETATIONS,r.ambiguityReason().orElseThrow());
        assertEquals(5,r.blockedCapabilities().size());assertTrue(r.blocking());assertEquals(SelectionStatus.NOT_SELECTED_BY_DETECTOR,r.selectionStatus());
        assertEquals(2,batch.universeCount());assertEquals(0,batch.excludedCount());assertEquals(1,batch.evaluationCount());
    }
    @Test void entireClosedLegacyShapeMatrixProducesExactlyOneTypedResultPerAtom() {
        for(String type:List.of("RECURRENTE","EXCEPCION","CANCELACION"))for(int shape=0;shape<7;shape++) {
            var sources=legacy(type,shape,shape);var i=comparison(input(1,new int[]{0},0),sources);
            var batch=new ComparisonCompatibilityV2().compare(i);assertEquals(sources.size(),batch.sourceCount());assertEquals(sources.size(),batch.evaluationCount());
            for(var e:batch.evaluations()) {
                assertEquals(ComparisonEvaluationV2.Variant.CORE_V1,e.variant());var r=e.coreResult().orElseThrow();
                assertEquals(e.request().source(),r.source());assertEquals(26,r.source().observableFields().size());
                if(!type.equals("RECURRENTE")) { assertEquals(ResultStatus.UNSUPPORTED,r.resultStatus());assertEquals(UnsupportedReason.UNKNOWN_INTENT,r.unsupportedReason().orElseThrow()); }
                else if(shape>=2) { assertEquals(ResultStatus.DIVERGENT_INCOMPATIBLE,r.resultStatus());assertTrue(r.blocking());assertEquals(0,r.candidateCount()); }
                else assertEquals(ResultStatus.CANDIDATE_EVALUATION_COMPLETE,r.resultStatus());
                assertEquals(r.candidates().size(),r.generatedCandidateCount());assertEquals(SelectionStatus.NOT_SELECTED_BY_DETECTOR,r.selectionStatus());
            }
        }
    }
    @Test void cancellationGapWithKnownInstructorKeepsPlausibilityAndAbsentInstructorDoesNot() {
        for(int shape:List.of(2,3)) {
            var i=comparison(input(1,new int[]{0},0),legacy("CANCELACION",shape,10+shape));
            var r=new ComparisonCompatibilityV2().compare(i).evaluations().getFirst().coreResult().orElseThrow();
            assertEquals(shape==2?1:0,r.candidateCount());assertEquals(ResultStatus.UNSUPPORTED,r.resultStatus());
        }
    }
    @Test void excludedObservationsAreAccountedAndNoIntervalEnumIsInvented() {
        var i=comparison(input(1,new int[]{0},0),legacy("RECURRENTE",1,20));
        var r=new ComparisonCompatibilityV2().compare(i);assertEquals(1,r.universeCount());assertEquals(1,r.excludedCount());
        assertEquals(0,r.evaluations().getFirst().outcome().generatedCandidateCount());
        assertEquals(ComparisonCandidateEvidenceV2.Match.MISMATCH,r.observations().getFirst().dimensions().get("start").match());
    }
    @Test void sourceDateProvenanceAndTargetReferenceFailuresAbortInsteadOfDropping() {
        var i=comparison(input(1,new int[]{0},0),legacy("RECURRENTE",0,21));var s=i.legacy().getFirst();
        var fields=new HashMap<>(s.observableFields());fields.put("evaluationDate",DATE.plusDays(1).toString());
        var bad=copy(s,"observableFields",fields);
        assertThrows(ComparisonFailureV2.class,()->new ComparisonCompatibilityV2().compare(copy(i,"legacy",List.of(bad))));
        var target=copy(i.nominalCandidates().getFirst(),"candidateType",CandidateType.ADDITION_OCCURRENCE);
        assertThrows(ComparisonFailureV2.class,()->new LegacyCandidateGeneratorV2().generate(s,List.of(target),DATE));
        assertThrows(ComparisonFailureV2.class,()->new LegacyCandidateGeneratorV2().generate(s,List.of(i.nominalCandidates().getFirst(),i.nominalCandidates().getFirst()),DATE));
    }
}
