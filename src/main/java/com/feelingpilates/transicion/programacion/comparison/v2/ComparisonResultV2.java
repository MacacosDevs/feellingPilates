package com.feelingpilates.transicion.programacion.comparison.v2;

import java.util.*;

/** Only the validated whole-batch pipeline can issue a result. */
public final class ComparisonResultV2 {
    private final ComparisonInputV2 input;
    private final List<ComparisonEvaluationV2> evaluations;
    private final List<ComparisonCandidateEvidenceV2> observations;
    private final int sourceCount, universeCount, excludedCount;
    private final String inputCommitment, contentCommitment, executionCommitment;
    private ComparisonResultV2(List<ComparisonEvaluationV2> evaluations, List<ComparisonCandidateEvidenceV2> observations,
            ComparisonInputV2 input) {
        this.input=input;
        int sourceCount=input.reservations().size()+input.legacy().size()+input.adjustmentSources().size();
        String inputCommitment=ComparisonCanonicalizerV2.hash("F2E-COMPARISON-INPUT-V2",input);
        String invocationIdentity=input.invocationIdentity();
        this.evaluations = List.copyOf(evaluations); this.observations = List.copyOf(observations);
        this.sourceCount = sourceCount; this.universeCount = observations.size();
        this.excludedCount = (int) observations.stream().filter(o -> !o.admitted()).count();
        this.inputCommitment = inputCommitment;
        ComparisonFailureV2.require(sourceCount == evaluations.size(), "ACCOUNTING", "one evaluation per source");
        var seen = new HashSet<String>();
        for (var e : evaluations) ComparisonFailureV2.require(seen.add(e.evaluationIdentity()), "ACCOUNTING", "duplicate evaluation");
        this.contentCommitment = ComparisonCanonicalizerV2.hash("F2E-COMPARISON-CONTENT-V2", evaluations, observations,
                sourceCount, universeCount, excludedCount, input.compositionBinding());
        this.executionCommitment = ComparisonCanonicalizerV2.hash("F2E-COMPARISON-EXECUTION-V2", invocationIdentity, inputCommitment, contentCommitment);
    }
    static ComparisonResultV2 issue(List<ComparisonEvaluationV2> evaluations, List<ComparisonCandidateEvidenceV2> observations,
            ComparisonInputV2 input) {
        return new ComparisonResultV2(evaluations, observations, input);
    }
    public ComparisonInputV2 input() { return input; }
    public List<ComparisonEvaluationV2> evaluations() { return evaluations; }
    public List<ComparisonCandidateEvidenceV2> observations() { return observations; }
    public int sourceCount() { return sourceCount; }
    public int evaluationCount() { return evaluations.size(); }
    /** Supplemental legacy inspected pairs; other groups retain their complete core candidate sets. */
    public int universeCount() { return universeCount; }
    public int excludedCount() { return excludedCount; }
    public String inputCommitment() { return inputCommitment; }
    public String contentCommitment() { return contentCommitment; }
    public String executionCommitment() { return executionCommitment; }
}
