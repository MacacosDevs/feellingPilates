package com.feelingpilates.transicion.programacion.detector;

import java.util.List;

import static com.feelingpilates.transicion.programacion.detector.DetectorValidation.required;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.EligibilityStatus.ELIGIBLE;

public record CandidateGenerationResult(
        List<DetectorCandidate> candidates,
        F2DCompatibilityResult f2dCompatibility) {

    public CandidateGenerationResult {
        candidates = List.copyOf(required(candidates, "candidates"));
        f2dCompatibility = required(f2dCompatibility, "f2dCompatibility");
        long distinct = candidates.stream().map(DetectorCandidate::candidateIdentity).distinct().count();
        if (distinct != candidates.size()) {
            throw new DetectorInputInvalidException(
                    DetectorVocabulary.InputErrorCode.DUPLICATE_CANDIDATE_IDENTITY,
                    "candidate identities must be unique within one evaluation");
        }
    }

    public int generatedCandidateCount() {
        return candidates.size();
    }

    public int candidateCount() {
        return (int) candidates.stream()
                .filter(candidate -> candidate.eligibilityStatus() == ELIGIBLE)
                .count();
    }
}
