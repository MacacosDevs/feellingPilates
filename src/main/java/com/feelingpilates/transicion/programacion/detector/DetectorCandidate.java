package com.feelingpilates.transicion.programacion.detector;

import com.feelingpilates.programacion.dominio.ReferenciaOcurrencia;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.feelingpilates.transicion.programacion.detector.DetectorValidation.required;
import static com.feelingpilates.transicion.programacion.detector.DetectorValidation.requiredText;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.EligibilityStatus.ELIGIBLE;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.EligibilityStatus.REJECTED;

public record DetectorCandidate(
        ReferenciaOcurrencia candidateIdentity,
        DetectorVocabulary.CandidateType candidateType,
        String candidateSnapshotIdentity,
        String candidateFingerprint,
        List<CandidateEvidenceItem> candidateEvidence,
        DetectorVocabulary.EvidenceKind relationshipEvidenceKind,
        Map<String, String> normalizedObservableFields,
        Set<DetectorVocabulary.EvidenceDimension> matchingDimensions,
        Set<DetectorVocabulary.EvidenceDimension> mismatchDimensions,
        DetectorVocabulary.EligibilityStatus eligibilityStatus,
        List<DetectorVocabulary.RejectionReason> rejectionReasons,
        String candidateEvidenceHash,
        EvidenceProvenance provenance) {

    public DetectorCandidate {
        candidateIdentity = required(candidateIdentity, "candidateIdentity");
        candidateType = required(candidateType, "candidateType");
        candidateSnapshotIdentity = requiredText(candidateSnapshotIdentity, "candidateSnapshotIdentity");
        candidateFingerprint = requiredText(candidateFingerprint, "candidateFingerprint");
        candidateEvidence = List.copyOf(required(candidateEvidence, "candidateEvidence"));
        relationshipEvidenceKind = required(relationshipEvidenceKind, "relationshipEvidenceKind");
        normalizedObservableFields = Map.copyOf(required(
                normalizedObservableFields, "normalizedObservableFields"));
        matchingDimensions = Set.copyOf(required(matchingDimensions, "matchingDimensions"));
        mismatchDimensions = Set.copyOf(required(mismatchDimensions, "mismatchDimensions"));
        eligibilityStatus = required(eligibilityStatus, "eligibilityStatus");
        rejectionReasons = List.copyOf(required(rejectionReasons, "rejectionReasons"));
        candidateEvidenceHash = requiredText(candidateEvidenceHash, "candidateEvidenceHash");
        provenance = required(provenance, "provenance");
        if (!matchingDimensions.stream().noneMatch(mismatchDimensions::contains)) {
            throw new DetectorInputInvalidException(
                    DetectorVocabulary.InputErrorCode.INVALID_SCENARIO,
                    "a dimension cannot match and mismatch simultaneously");
        }
        if ((eligibilityStatus == REJECTED) != !rejectionReasons.isEmpty()) {
            throw new DetectorInputInvalidException(
                    DetectorVocabulary.InputErrorCode.INVALID_SCENARIO,
                    "rejected candidates require reasons and eligible candidates forbid them");
        }
        if (eligibilityStatus == ELIGIBLE && !mismatchDimensions.isEmpty()) {
            throw new DetectorInputInvalidException(
                    DetectorVocabulary.InputErrorCode.INVALID_SCENARIO,
                    "eligible candidates cannot contain mismatches");
        }
    }

    public ReferenciaOcurrencia candidateTargetReference() {
        return candidateIdentity;
    }
}
