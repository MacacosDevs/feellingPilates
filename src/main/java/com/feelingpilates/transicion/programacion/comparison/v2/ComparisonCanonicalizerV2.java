package com.feelingpilates.transicion.programacion.comparison.v2;

import java.util.*;
import java.time.*;
import java.nio.charset.*;
import java.security.*;
import com.feelingpilates.programacion.dominio.ReferenciaOcurrencia;
import com.feelingpilates.transicion.programacion.detector.*;

/** Explicit value schemas; no reflection, default locale or arbitrary toString fallback. */
public final class ComparisonCanonicalizerV2 {
    private ComparisonCanonicalizerV2() { }
    public static final Comparator<String> TEXT_ORDER = (a,b) -> Arrays.compareUnsigned(text(a), text(b));
    public static final Comparator<UUID> UUID_ORDER = (a,b) -> {
        int c = Long.compareUnsigned(a.getMostSignificantBits(), b.getMostSignificantBits());
        return c != 0 ? c : Long.compareUnsigned(a.getLeastSignificantBits(), b.getLeastSignificantBits());
    };
    public static final Comparator<ReferenciaOcurrencia> REFERENCE_ORDER = Comparator
            .comparing((ReferenciaOcurrencia r) -> r.tipo().ordinal())
            .thenComparing(ReferenciaOcurrencia::id, UUID_ORDER).thenComparing(ReferenciaOcurrencia::fecha);
    /** Semantic projection excludes comparison-only run/evaluation/time and hashes derived from them.
     * Original closed source, candidate and R5 commitments retain their exact supplied provenance. */
    static List<Object> content(ComparisonEvaluationV2 e) {
        var r=e.request();
        return List.of("EVALUATION_CONTENT_V2", e.variant(), r.detectorVersion(), r.ruleId(), r.ruleVersion(),
                r.source(), r.expectedTargetAtomType(), r.declaredRelationCardinality(), r.candidateGeneration(),
                r.classificationContext(), r.provenance(), r.evidenceHash(), e.outcome(), e.omission().isPresent(),
                e.axis(), e.evidenceCommitment());
    }
    static void requiredText(String s) {
        ComparisonFailureV2.require(s != null && !s.isBlank() && s.indexOf('\0') < 0, "CANONICALIZATION", "required text"); text(s);
    }
    private static byte[] text(String s) {
        ComparisonFailureV2.require(s!=null && s.indexOf('\0')<0,"CANONICALIZATION","no NUL text");
        try {
            var b = StandardCharsets.UTF_8.newEncoder().onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT).encode(java.nio.CharBuffer.wrap(s));
            byte[] result = new byte[b.remaining()]; b.get(result); return result;
        } catch (CharacterCodingException e) { throw new ComparisonFailureV2("CANONICALIZATION", "strict UTF-8", e); }
    }
    private static byte[] frame(byte[]... fields) {
        var out = new java.io.ByteArrayOutputStream();
        out.writeBytes((fields.length + ":").getBytes(StandardCharsets.US_ASCII));
        for (byte[] field : fields) { out.writeBytes((field.length + ":").getBytes(StandardCharsets.US_ASCII)); out.writeBytes(field); }
        return out.toByteArray();
    }
    private static byte[] scalar(String type, String value) { return frame(text(type), text(value)); }
    private static byte[] schema(String name, Object... fields) { return frame(text(name), encode(Arrays.asList(fields))); }
    public static String hash(String domain, Object... values) {
        requiredText(domain);
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(schema(domain, values))); }
        catch (NoSuchAlgorithmException e) { throw new IllegalStateException(e); }
    }
    private static int compareKeys(Object a,Object b) {
        if(a instanceof String left && b instanceof String right)return TEXT_ORDER.compare(left,right);
        if(a instanceof UUID left && b instanceof UUID right)return UUID_ORDER.compare(left,right);
        if(a instanceof ReferenciaOcurrencia left && b instanceof ReferenciaOcurrencia right)return REFERENCE_ORDER.compare(left,right);
        return Arrays.compareUnsigned(encode(a),encode(b));
    }
    public static byte[] encode(Object value) {
        if (value == null) return frame(text("ABSENT"));
        if (value instanceof String s) return scalar("TEXT", s);
        if (value instanceof UUID u) return scalar("UUID", u.toString());
        if (value instanceof Boolean b) return scalar("BOOL", b.toString());
        if (value instanceof Integer n) return scalar("INT", n.toString());
        if (value instanceof LocalDate d) return scalar("DATE", d.toString());
        if (value instanceof LocalTime t) return scalar("TIME", t.toString());
        if (value instanceof Instant t) return scalar("INSTANT", t.toString());
        if (value instanceof Enum<?> e) return schema("ENUM", e.getDeclaringClass().getName(), e.name());
        if (value instanceof Optional<?> o) return schema("OPTIONAL", o.isPresent(), o.orElse(null));
        if (value instanceof OptionalInt o) return schema("OPTIONAL_INT", o.isPresent(), o.isPresent() ? o.getAsInt() : null);
        if (value instanceof List<?> l) {
            var parts = new ArrayList<byte[]>(); parts.add(text("LIST")); parts.add(text(Integer.toString(l.size())));
            for (Object element : l) parts.add(encode(element)); return frame(parts.toArray(byte[][]::new));
        }
        if (value instanceof Set<?> set) {
            var parts = set.stream().sorted(ComparisonCanonicalizerV2::compareKeys).map(ComparisonCanonicalizerV2::encode).toList();
            var all = new ArrayList<byte[]>(); all.add(text("SET")); all.add(text(Integer.toString(parts.size()))); all.addAll(parts);
            return frame(all.toArray(byte[][]::new));
        }
        if (value instanceof Map<?,?> map) {
            var entries = new ArrayList<>(map.entrySet());
            for(var entry:entries)if(entry.getKey() instanceof String key)requiredText(key);
            entries.sort((a,b) -> compareKeys(a.getKey(),b.getKey()));
            var parts = new ArrayList<byte[]>(); parts.add(text("MAP")); parts.add(text(Integer.toString(map.size())));
            for (var entry : entries) parts.add(frame(encode(entry.getKey()), encode(entry.getValue())));
            return frame(parts.toArray(byte[][]::new));
        }
        if (value instanceof ReferenciaOcurrencia r) return schema("REFERENCE", r.tipo(), r.id(), r.fecha());
        if (value instanceof EvidenceProvenance p) return schema("PROVENANCE", p.sourceName(), p.schemaFingerprint(), p.recordIds(),
                p.ruleId(), p.ruleVersion(), p.businessTimeContext(), p.normalizedFields());
        if (value instanceof SourceSnapshot s) {
            if (s instanceof ReservationSourceSnapshot r) {
                ComparisonFailureV2.require(r.historicalProgrammingTarget().isEmpty(),"CANONICALIZATION","historical reservation mode unavailable");
                return schema("RESERVA", r.sourceIdentity(), r.sourceSystem(), r.sourceAtomType(),
                    r.state(), r.date(), r.salonId(), r.instructorId(), r.activityId(), r.reservedSubinterval().start(), r.reservedSubinterval().end(),
                    r.snapshotIdentity(), r.sourceFingerprint(), r.additionalObservableFields(), r.historicalProgrammingTarget().isPresent(), r.provenance());
            }
            if (s instanceof GenericSourceSnapshot g) return schema("SOURCE", g.sourceSystem(), g.sourceAtomType(), g.sourceIdentity(),
                    g.snapshotIdentity(), g.sourceFingerprint(), g.observableFields(), g.provenance());
            throw new ComparisonFailureV2("CANONICALIZATION", "unknown source subtype", null);
        }
        if (value instanceof ProgrammingCandidateSnapshot c) return schema("TARGET", c.reference(), c.candidateType(), c.snapshotIdentity(),
                c.candidateFingerprint(), c.salonId(), c.instructorId(), c.activityId(), c.start(), c.end(), c.observableFields(), c.provenance());
        if (value instanceof CandidateEvidenceItem e) return schema("EVIDENCE_ITEM", e.dimension(), e.kind(), e.sourceValue(), e.candidateValue(), e.matches());
        if (value instanceof DetectorCandidate c) return schema("CORE_CANDIDATE", c.candidateIdentity(), c.candidateType(), c.candidateSnapshotIdentity(),
                c.candidateFingerprint(), c.candidateEvidence(), c.relationshipEvidenceKind(), c.normalizedObservableFields(), c.matchingDimensions(),
                c.mismatchDimensions(), c.eligibilityStatus(), c.rejectionReasons(), c.candidateEvidenceHash(), c.provenance());
        if (value instanceof F2DCompatibilityResult c) return schema("F2D", c.status(), c.blocking(), c.conflictDetails());
        if (value instanceof CandidateGenerationResult c) return schema("GENERATED", c.candidates(), c.f2dCompatibility());
        if (value instanceof ClassificationContext c) return schema("SCENARIO", c.scenario(), c.historyStatus(), c.nominalTargetCandidateCount(), c.effectiveOccurrenceCount());
        if (value instanceof DetectorEvaluationRequest r) return schema("CORE_REQUEST", r.detectorRunIdentity(), r.evaluationIdentity(), r.detectorVersion(), r.ruleId(), r.ruleVersion(),
                r.evaluatedAt(), r.source(), r.expectedTargetAtomType(), r.declaredRelationCardinality(), r.candidateGeneration(), r.classificationContext(), r.provenance(), r.evidenceHash());
        if (value instanceof DetectorResult r) return schema("CORE_RESULT", r.detectorRunIdentity(), r.evaluationIdentity(), r.detectorVersion(), r.ruleId(), r.ruleVersion(), r.evaluatedAt(),
                r.source(), r.expectedTargetAtomType(), r.declaredRelationCardinality(), r.candidates(), r.generatedCandidateCount(), r.candidateCount(), r.mappingStatus(), r.resultStatus(),
                r.effectiveResultStatus(), r.nominalTargetCandidateCount(), r.effectiveOccurrenceCount(), r.historyStatus(), r.ambiguityStatus(), r.ambiguityReason(), r.selectionStatus(),
                r.expectedAbsenceReason(), r.unsupportedReason(), r.blocking(), r.blockingStatus(), r.blockedCapabilities(), r.f2dCompatibility(), r.provenance(), r.evidenceHash(), r.resultHash());
        if (value instanceof ComparisonCandidateEvidenceV2.Observation o) return schema("OBSERVATION", o.sourceValue(), o.targetValue(), o.match());
        if (value instanceof ComparisonCandidateEvidenceV2 o) return schema("PAIR", o.source(), o.target(), o.axis(), o.dimensions(), o.admitted());
        if (value instanceof ComparisonInputV2 i) return schema("INPUT", i.runIdentity(), i.invocationIdentity(), i.date(), i.businessZoneId(), i.evaluatedAt(), i.ruleVersion(),
                i.mode(), i.completionReceiptIdentity(), i.reservations(), i.legacy(), i.compositionBinding());
        if (value instanceof ComparisonEvaluationV2.Outcome o) return schema("OUTCOME", o.generatedCandidateCount(), o.candidateCount(), o.mappingStatus(), o.resultStatus(), o.effectiveResultStatus(),
                o.nominalTargetCandidateCount(), o.effectiveOccurrenceCount(), o.historyStatus(), o.ambiguityStatus(), o.ambiguityReason(), o.selectionStatus(), o.expectedAbsenceReason(),
                o.unsupportedReason(), o.blocking(), o.blockingStatus(), o.blockedCapabilities(), o.f2dCompatibility());
        if (value instanceof ComparisonEvaluationV2 e) return schema("EVALUATION", e.variant(), e.request(), e.coreResult(), e.outcome(), e.omission().isPresent(), e.axis(), e.evidenceCommitment(), e.resultCommitment());
        throw new ComparisonFailureV2("CANONICALIZATION", "unsupported canonical value", null);
    }
}
