package com.feelingpilates.transicion.programacion.detector;

import java.time.Instant;

import static com.feelingpilates.transicion.programacion.detector.DetectorValidation.required;
import static com.feelingpilates.transicion.programacion.detector.DetectorValidation.requiredText;

public record DetectorEvaluationRequest(
        String detectorRunIdentity,
        String evaluationIdentity,
        String detectorVersion,
        String ruleId,
        String ruleVersion,
        Instant evaluatedAt,
        SourceSnapshot source,
        DetectorVocabulary.TargetAtomType expectedTargetAtomType,
        DetectorVocabulary.RelationCardinality declaredRelationCardinality,
        CandidateGenerationResult candidateGeneration,
        ClassificationContext classificationContext,
        EvidenceProvenance provenance,
        String evidenceHash) {

    public DetectorEvaluationRequest {
        detectorRunIdentity = requiredText(detectorRunIdentity, "detectorRunIdentity");
        evaluationIdentity = requiredText(evaluationIdentity, "evaluationIdentity");
        detectorVersion = requiredText(detectorVersion, "detectorVersion");
        ruleId = requiredText(ruleId, "ruleId");
        ruleVersion = requiredText(ruleVersion, "ruleVersion");
        evaluatedAt = required(evaluatedAt, "evaluatedAt");
        source = required(source, "source");
        expectedTargetAtomType = required(expectedTargetAtomType, "expectedTargetAtomType");
        declaredRelationCardinality = required(
                declaredRelationCardinality, "declaredRelationCardinality");
        candidateGeneration = required(candidateGeneration, "candidateGeneration");
        classificationContext = required(classificationContext, "classificationContext");
        provenance = required(provenance, "provenance");
        evidenceHash = requiredText(evidenceHash, "evidenceHash");
    }
}
