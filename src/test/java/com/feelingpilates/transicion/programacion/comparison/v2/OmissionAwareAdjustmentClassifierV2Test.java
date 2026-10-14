package com.feelingpilates.transicion.programacion.comparison.v2;

import java.util.*;
import org.junit.jupiter.api.Test;
import com.feelingpilates.transicion.programacion.detector.*;
import com.feelingpilates.transicion.programacion.r5.composition.*;
import static org.junit.jupiter.api.Assertions.*;
import static com.feelingpilates.transicion.programacion.comparison.v2.ComparisonTestFixturesV2.*;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.*;
class OmissionAwareAdjustmentClassifierV2Test {
    @Test void allEightExplainedAdditionOmissionsUseTruthfulVersionedDivergence() {
        var causes=new HashSet<EffectiveCompositionOmission.Cause>();
        for(int cause=1;cause<=8;cause++) {
            var i=comparison(withCause(input(0,new int[]{},1),cause));var batch=new ComparisonCompatibilityV2().compare(i);var e=batch.evaluations().getFirst();
            causes.add(e.omission().orElseThrow().cause());assertEquals(ComparisonEvaluationV2.Variant.EXPLAINED_ADDITION_OMISSION_V2,e.variant());assertTrue(e.coreResult().isEmpty());
            assertSame(i.effectiveResult().omissions().getFirst(),e.omission().orElseThrow());
            var o=e.outcome();assertEquals(ResultStatus.DIVERGENT_INCOMPATIBLE,o.resultStatus());assertEquals(EffectiveResultStatus.DIVERGENT,o.effectiveResultStatus());
            assertEquals(0,o.candidateCount());assertEquals(0,o.generatedCandidateCount());assertEquals(0,o.effectiveOccurrenceCount());assertTrue(o.nominalTargetCandidateCount().isEmpty());
            assertEquals(MappingStatus.NO_CANDIDATES,o.mappingStatus());assertEquals(AmbiguityStatus.NOT_AMBIGUOUS,o.ambiguityStatus());assertTrue(o.ambiguityReason().isEmpty());
            assertTrue(o.blocking());assertEquals(5,o.blockedCapabilities().size());assertTrue(o.expectedAbsenceReason().isEmpty());assertTrue(o.unsupportedReason().isEmpty());
            assertEquals(HistoryStatus.NOT_APPLICABLE,o.historyStatus());assertEquals(SelectionStatus.NOT_SELECTED_BY_DETECTOR,o.selectionStatus());
            assertEquals(SourceAtomType.NEW_ADICION,e.request().source().sourceAtomType());
            var old=new DetectorClassifier().classify(e.request());assertEquals(ResultStatus.MISSING,old.resultStatus());
            assertThrows(DetectorInputInvalidException.class,()->new DetectorResult(old.detectorRunIdentity(),old.evaluationIdentity(),old.detectorVersion(),old.ruleId(),old.ruleVersion(),old.evaluatedAt(),old.source(),old.expectedTargetAtomType(),old.declaredRelationCardinality(),
                    old.candidates(),0,0,MappingStatus.NO_CANDIDATES,ResultStatus.DIVERGENT_INCOMPATIBLE,EffectiveResultStatus.DIVERGENT,OptionalInt.empty(),0,HistoryStatus.NOT_APPLICABLE,
                    AmbiguityStatus.NOT_AMBIGUOUS,Optional.empty(),SelectionStatus.NOT_SELECTED_BY_DETECTOR,Optional.empty(),Optional.empty(),true,BlockingStatus.BLOCKING,
                    old.blockedCapabilities(),old.f2dCompatibility(),old.provenance(),old.evidenceHash(),old.resultHash()));
        }
        assertEquals(Set.of(EffectiveCompositionOmission.Cause.values()),causes);
    }
    @Test void survivorsCancellationAndReplacementStillRetainExactCoreResults() {
        for(int kind:List.of(1,2,4)) {
            var i=kind==4?comparison(input(0,new int[]{},1)):comparison(input(1,new int[]{kind},0));
            var e=new ComparisonCompatibilityV2().compare(i).evaluations().getFirst();assertEquals(ComparisonEvaluationV2.Variant.CORE_V1,e.variant());
            assertEquals(new DetectorClassifier().classify(e.request()),e.coreResult().orElseThrow());
            assertEquals(kind==1?ResultStatus.EXPECTED_ABSENCE:ResultStatus.CANDIDATE_EVALUATION_COMPLETE,e.outcome().resultStatus());
        }
        for(int cause=1;cause<=8;cause++) {
            var e=new ComparisonCompatibilityV2().compare(comparison(withCause(input(1,new int[]{2},0),cause))).evaluations().getFirst();
            assertEquals(ComparisonEvaluationV2.Variant.CORE_V1,e.variant());assertEquals(ResultStatus.DIVERGENT_INCOMPATIBLE,e.outcome().resultStatus());
            assertTrue(e.omission().isPresent());assertEquals(cause-1,e.omission().orElseThrow().cause().ordinal());
            assertEquals(1,e.outcome().nominalTargetCandidateCount().orElseThrow());assertEquals(0,e.outcome().effectiveOccurrenceCount());
        }
    }
    @Test void omissionCannotBeMintedFromEmptyListWrongSourceOrAnotherR5Input() {
        var present=comparison(input(0,new int[]{},1));var omitted=comparison(withCause(input(0,new int[]{},1),1));
        assertThrows(ComparisonFailureV2.class,()->ComparisonEvaluationV2.omittedAddition(present,present.adjustmentSources().getFirst()));
        var other=comparison(withCause(input(List.of(),List.of(adjustment(1,4))),1));
        assertThrows(ComparisonFailureV2.class,()->ComparisonEvaluationV2.omittedAddition(omitted,other.adjustmentSources().getFirst()));
        assertThrows(ComparisonFailureV2.class,()->new ComparisonCompatibilityV2().compare(copy(omitted,"effectiveResult",other.effectiveResult())));
        assertThrows(ComparisonFailureV2.class,()->new ComparisonCompatibilityV2().compare(copy(omitted,"completionReceiptIdentity","another")));
    }
}
