package com.feelingpilates.transicion.programacion.detector;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import static com.feelingpilates.transicion.programacion.detector.DetectorValidation.equal;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.EligibilityStatus.ELIGIBLE;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.EligibilityStatus.REJECTED;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.EvidenceDimension.ACTIVITY;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.EvidenceDimension.DATE;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.EvidenceDimension.INSTRUCTOR;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.EvidenceDimension.RESERVED_SUBINTERVAL;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.EvidenceDimension.SALON;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.EvidenceKind.CONTAINMENT;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.EvidenceKind.FIELD_MATCH;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.F2DCompatibilityStatus.F2D_AUTHORITY_CONFLICT;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.RejectionReason.ACTIVITY_MISMATCH;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.RejectionReason.DATE_MISMATCH;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.RejectionReason.INSTRUCTOR_MISMATCH;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.RejectionReason.RESERVED_SUBINTERVAL_NOT_CONTAINED;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.RejectionReason.SALON_MISMATCH;

public final class CandidateEvidenceGenerator {

    private static final Comparator<ProgrammingCandidateSnapshot> STABLE_ORDER = Comparator
            .comparing(ProgrammingCandidateSnapshot::reference)
            .thenComparing(ProgrammingCandidateSnapshot::snapshotIdentity);

    private final F2DAuthorityGuard authorityGuard;

    public CandidateEvidenceGenerator() {
        this(new F2DAuthorityGuard());
    }

    public CandidateEvidenceGenerator(F2DAuthorityGuard authorityGuard) {
        this.authorityGuard = DetectorValidation.required(authorityGuard, "authorityGuard");
    }

    public CandidateGenerationResult generate(
            ReservationSourceSnapshot source,
            List<ProgrammingCandidateSnapshot> candidateUniverse) {
        DetectorValidation.required(source, "source");
        List<ProgrammingCandidateSnapshot> snapshots = List.copyOf(
                DetectorValidation.required(candidateUniverse, "candidateUniverse"));
        List<F2DCompatibilityResult> compatibilityResults = snapshots.stream()
                .map(authorityGuard::evaluate)
                .toList();
        List<ProgrammingCandidateSnapshot> ordered = snapshots.stream().sorted(STABLE_ORDER).toList();
        List<DetectorCandidate> candidates = ordered.stream()
                .map(snapshot -> toCandidate(source, snapshot, authorityGuard.evaluate(snapshot)))
                .toList();
        return new CandidateGenerationResult(candidates, authorityGuard.combine(compatibilityResults));
    }

    private DetectorCandidate toCandidate(
            ReservationSourceSnapshot source,
            ProgrammingCandidateSnapshot target,
            F2DCompatibilityResult compatibility) {
        EnumSet<DetectorVocabulary.EvidenceDimension> matching = EnumSet.noneOf(
                DetectorVocabulary.EvidenceDimension.class);
        EnumSet<DetectorVocabulary.EvidenceDimension> mismatch = EnumSet.noneOf(
                DetectorVocabulary.EvidenceDimension.class);
        List<CandidateEvidenceItem> evidence = new ArrayList<>();
        List<DetectorVocabulary.RejectionReason> rejections = new ArrayList<>();

        fieldEvidence(DATE, source.date(), target.reference().fecha(), DATE_MISMATCH,
                matching, mismatch, evidence, rejections);
        fieldEvidence(SALON, source.salonId(), target.salonId(), SALON_MISMATCH,
                matching, mismatch, evidence, rejections);
        fieldEvidence(INSTRUCTOR, source.instructorId(), target.instructorId(), INSTRUCTOR_MISMATCH,
                matching, mismatch, evidence, rejections);
        fieldEvidence(ACTIVITY, source.activityId(), target.activityId(), ACTIVITY_MISMATCH,
                matching, mismatch, evidence, rejections);

        boolean contained = source.reservedSubinterval().isContainedIn(target.start(), target.end());
        dimension(RESERVED_SUBINTERVAL, contained, matching, mismatch);
        evidence.add(new CandidateEvidenceItem(
                RESERVED_SUBINTERVAL,
                CONTAINMENT,
                source.reservedSubinterval().start() + "-" + source.reservedSubinterval().end(),
                target.start() + "-" + target.end(),
                contained));
        if (!contained) {
            rejections.add(RESERVED_SUBINTERVAL_NOT_CONTAINED);
        }
        if (compatibility.status() == F2D_AUTHORITY_CONFLICT) {
            rejections.add(DetectorVocabulary.RejectionReason.F2D_AUTHORITY_CONFLICT);
        }

        DetectorVocabulary.EligibilityStatus status = rejections.isEmpty() ? ELIGIBLE : REJECTED;
        String evidenceHash = candidateHash(source, target, matching, mismatch, status, rejections);
        return new DetectorCandidate(
                target.reference(),
                target.candidateType(),
                target.snapshotIdentity(),
                target.candidateFingerprint(),
                evidence,
                contained ? CONTAINMENT : FIELD_MATCH,
                target.observableFields(),
                Set.copyOf(matching),
                Set.copyOf(mismatch),
                status,
                rejections,
                evidenceHash,
                target.provenance());
    }

    private void fieldEvidence(
            DetectorVocabulary.EvidenceDimension dimension,
            Object sourceValue,
            Object targetValue,
            DetectorVocabulary.RejectionReason rejection,
            Set<DetectorVocabulary.EvidenceDimension> matching,
            Set<DetectorVocabulary.EvidenceDimension> mismatch,
            List<CandidateEvidenceItem> evidence,
            List<DetectorVocabulary.RejectionReason> rejections) {
        boolean matches = equal(sourceValue, targetValue);
        dimension(dimension, matches, matching, mismatch);
        evidence.add(new CandidateEvidenceItem(
                dimension, FIELD_MATCH, sourceValue.toString(), targetValue.toString(), matches));
        if (!matches) {
            rejections.add(rejection);
        }
    }

    private void dimension(
            DetectorVocabulary.EvidenceDimension dimension,
            boolean matches,
            Set<DetectorVocabulary.EvidenceDimension> matching,
            Set<DetectorVocabulary.EvidenceDimension> mismatch) {
        (matches ? matching : mismatch).add(dimension);
    }

    private String candidateHash(
            ReservationSourceSnapshot source,
            ProgrammingCandidateSnapshot target,
            Set<DetectorVocabulary.EvidenceDimension> matching,
            Set<DetectorVocabulary.EvidenceDimension> mismatch,
            DetectorVocabulary.EligibilityStatus status,
            List<DetectorVocabulary.RejectionReason> rejections) {
        return SemanticHash.sha256(source.sourceFingerprint() + '|' + target.candidateFingerprint() + '|'
                + target.reference() + '|' + matching.stream().sorted().toList() + '|'
                + mismatch.stream().sorted().toList() + '|' + status + '|' + rejections);
    }
}
