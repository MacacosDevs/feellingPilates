package com.feelingpilates.transicion.programacion.comparison.v2;

import java.util.*;
import com.feelingpilates.transicion.programacion.detector.*;
import com.feelingpilates.transicion.programacion.r5.composition.EffectiveProgrammingCompositionResult;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.*;

/** Plain pure supplied-input dispatcher; has no bean, transaction, reader or productive caller. */
public final class ComparisonCompatibilityV2 {
    public ComparisonResultV2 compare(ComparisonInputV2 input) {
        try { return ComparisonResultV2.issue(input); }
        catch(ComparisonFailureV2 e) { throw e.withContext(input,null); }
        catch(RuntimeException e) { throw new ComparisonFailureV2("COMPARISON","whole comparison unit rejected",e).withContext(input,null); }
    }
    record Parts(List<ComparisonEvaluationV2> evaluations, List<ComparisonCandidateEvidenceV2> observations) { }
    static Parts derive(ComparisonInputV2 input) {
        SourceSnapshot current=null;
        try {
            Objects.requireNonNull(input); OmissionAwareAdjustmentClassifierV2.validate(input);
            var evaluations=new ArrayList<ComparisonEvaluationV2>();var observations=new ArrayList<ComparisonCandidateEvidenceV2>();
            EffectiveProgrammingCompositionResult effective=input.effectiveResult();
            for(var source:input.reservations()) {
                current=source;
                var generation=new CandidateEvidenceGenerator().generate(source,effective.candidates());
                String evidence=ComparisonCanonicalizerV2.hash("F2E-RESERVA-EVIDENCE-V2",source,generation,input.compositionBinding());
                var request=request(input,source,generation,ClassificationContext.standard(),RelationCardinality.ONE_TO_MANY,evidence);
                evaluations.add(ComparisonEvaluationV2.core(request,new DetectorClassifier().classify(request),"EFFECTIVE",evidence));
            }
            for(var source:input.legacy()) {
                current=source;
                boolean nominal=source.sourceAtomType()!=SourceAtomType.LEGACY_EXCEPCION;
                var generated=new LegacyCandidateGeneratorV2().generate(source,nominal?input.nominalCandidates():input.effectiveCandidates(),input.date());
                observations.addAll(generated.observations());
                String evidence=ComparisonCanonicalizerV2.hash("F2E-LEGACY-EVIDENCE-V2",source,generated.observations(),input.compositionBinding());
                var request=request(input,source,generated.candidates(),generated.classification(),RelationCardinality.ZERO_TO_MANY,evidence);
                evaluations.add(ComparisonEvaluationV2.core(request,new DetectorClassifier().classify(request),nominal?"NOMINAL":"EFFECTIVE",evidence));
            }
            var adjustments=new ArrayList<>(input.adjustmentSources());adjustments.sort(Comparator.comparing((GenericSourceSnapshot s)->s.sourceAtomType().ordinal())
                    .thenComparing(GenericSourceSnapshot::sourceIdentity,ComparisonCanonicalizerV2.TEXT_ORDER));
            var adjustmentClassifier=new OmissionAwareAdjustmentClassifierV2();
            for(var source:adjustments) { current=source; evaluations.add(adjustmentClassifier.classify(input,source)); }
            return new Parts(List.copyOf(evaluations),List.copyOf(observations));
        } catch(ComparisonFailureV2 e) { throw e.withContext(input,current); }
        catch(RuntimeException e) { throw new ComparisonFailureV2("COMPARISON","whole comparison unit rejected",e).withContext(input,current); }
    }
    static DetectorEvaluationRequest request(ComparisonInputV2 i,SourceSnapshot source,CandidateGenerationResult candidates,
            ClassificationContext context,RelationCardinality cardinality,String evidence) {
        String identity=ComparisonCanonicalizerV2.hash("F2E-COMPARISON-EVALUATION-ID-V2",i.invocationIdentity(),ComparisonInputV2.RULE,
                source.sourceAtomType(),source.sourceIdentity(),context.scenario());
        return new DetectorEvaluationRequest(i.runIdentity(),identity,ComparisonInputV2.RULE,"R6_DIAGNOSTIC_COMPARISON",ComparisonInputV2.RULE,
                i.evaluatedAt(),source,TargetAtomType.EFFECTIVE_OCCURRENCE_REFERENCE,cardinality,candidates,context,source.provenance(),evidence);
    }
}
