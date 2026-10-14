package com.feelingpilates.transicion.programacion.comparison.v2;

import java.util.*;
import java.time.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import com.feelingpilates.transicion.programacion.detector.*;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.*;
import static com.feelingpilates.transicion.programacion.comparison.v2.ComparisonCandidateEvidenceV2.Match.*;

/** Observational predicates only; each excluded pair remains in the typed ledger. */
public final class LegacyCandidateGeneratorV2 {
    private static final Set<String> FIELDS = Set.of("turnId","type","active","salonId","dayOfWeek","date","turnStart","turnEnd",
            "createdAtTechnical","updatedAtTechnical","memberInstructorId","instructorId","activityId","assignmentStartRaw","assignmentEndRaw",
            "effectiveStart","effectiveEnd","rangeRule","atomKind","evidenceKey","membershipStatus","markers","historyStatus","historyMarker","scenarioPolicy","evaluationDate");
    public record Generated(CandidateGenerationResult candidates, List<ComparisonCandidateEvidenceV2> observations, ClassificationContext classification) {
        public Generated { observations = List.copyOf(observations); }
    }
    public Generated generate(GenericSourceSnapshot source, List<ProgrammingCandidateSnapshot> universe, LocalDate date) {
        try {
            var f = source.observableFields();
            ComparisonFailureV2.require(source.sourceSystem() == SourceSystem.LEGACY &&
                    Set.of(SourceAtomType.LEGACY_RECURRENTE,SourceAtomType.LEGACY_EXCEPCION,SourceAtomType.LEGACY_CANCELACION).contains(source.sourceAtomType()),
                    "LEGACY", "legacy source type");
            ComparisonFailureV2.require(f.keySet().equals(FIELDS), "LEGACY", "closed R2 observable key set");
            for (var entry : f.entrySet()) ComparisonFailureV2.require(entry.getValue().equals(source.provenance().normalizedFields().get(entry.getKey())),
                    "LEGACY", "preserved R2 observable provenance");
            ComparisonFailureV2.require(f.get("evaluationDate").equals(date.toString()) && f.get("active").equals("true") &&
                    f.get("historyStatus").equals("CURRENT_SNAPSHOT_ONLY"), "LEGACY", "date/active/current source");
            verifyClosedSource(source);
            boolean recurrent = source.sourceAtomType() == SourceAtomType.LEGACY_RECURRENTE;
            boolean cancellation = source.sourceAtomType() == SourceAtomType.LEGACY_CANCELACION;
            String physicalType = recurrent ? "RECURRENTE" : cancellation ? "CANCELACION" : "EXCEPCION";
            ComparisonFailureV2.require(f.get("type").equals(physicalType), "LEGACY", "physical type unchanged");
            boolean anomalous = List.of("ABSENT_MEMBER","ABSENT_ASSIGNMENT","ABSENT_ACTIVITY","NON_MEMBER_ASSIGNMENT","INCOMPLETE_RANGE","RANGE_OUTSIDE_TURN")
                    .stream().anyMatch(f.get("markers")::contains);
            String expectedPolicy = recurrent ? anomalous ? "INCOMPATIBLE_EVIDENCE" : "RECURRENT_CLAIM_DEPENDENT"
                    : cancellation ? "LEGACY_CANCELLATION_UNKNOWN_INTENT" : "LEGACY_EXCEPTION_UNKNOWN_INTENT";
            ComparisonFailureV2.require(expectedPolicy.equals(f.get("scenarioPolicy")), "LEGACY", "preserved R2 scenario policy");
            ComparisonFailureV2.require(recurrent ? f.get("date").equals("NULL") && f.get("dayOfWeek").equals(Integer.toString(date.getDayOfWeek().getValue()%7))
                    : f.get("date").equals(date.toString()) && f.get("dayOfWeek").equals("NULL"), "LEGACY", "physical date/day shape");
            var sorted = new ArrayList<>(List.copyOf(universe)); sorted.sort(Comparator.comparing(ProgrammingCandidateSnapshot::reference));
            var seen = new HashSet<Object>(); var candidates = new ArrayList<DetectorCandidate>(); var observations = new ArrayList<ComparisonCandidateEvidenceV2>();
            var guard = new F2DAuthorityGuard();
            for (var target : sorted) {
                ComparisonFailureV2.require(seen.add(target.reference()), "LEGACY", "duplicate target reference");
                ComparisonFailureV2.require(!guard.evaluate(target).blocking(), "LEGACY", "F2D target type/reference");
                var d = new LinkedHashMap<String,ComparisonCandidateEvidenceV2.Observation>();
                observe(d,"date",f.get("evaluationDate"),target.reference().fecha().toString());
                observe(d,"day",recurrent?f.get("dayOfWeek"):"ABSENT",Integer.toString(target.reference().fecha().getDayOfWeek().getValue()%7));
                observe(d,"salon",f.get("salonId"),target.salonId().toString());
                observe(d,"instructor",f.get("instructorId"),target.instructorId().toString());
                observe(d,"activity",f.get("activityId"),target.activityId().toString());
                observeTime(d,"start",f.get("effectiveStart"),target.start()); observeTime(d,"end",f.get("effectiveEnd"),target.end());
                boolean admitted = matches(d,"date","salon","instructor") && (cancellation ||
                        matches(d,"activity","start","end") && (!recurrent || matches(d,"day") && !anomalous)
                        && safeRange(f));
                var observation = new ComparisonCandidateEvidenceV2(source,target,cancellation||recurrent?"NOMINAL":"EFFECTIVE",d,admitted);
                observations.add(observation);
                if (admitted) candidates.add(candidate(target,d,cancellation,ComparisonCanonicalizerV2.hash("F2E-LEGACY-CANDIDATE-EVIDENCE-V2",observation)));
            }
            var classification = recurrent ? anomalous ? ClassificationContext.incompatibleEvidence() : ClassificationContext.standard()
                    : cancellation ? ClassificationContext.legacyCancellationUnknownIntent() : ClassificationContext.legacyExceptionUnknownIntent();
            return new Generated(new CandidateGenerationResult(candidates,F2DCompatibilityResult.compatible()),observations,classification);
        } catch (ComparisonFailureV2 e) { throw e; }
        catch (RuntimeException e) { throw new ComparisonFailureV2("LEGACY", "invalid supplied legacy evidence", e); }
    }
    private static boolean safeRange(Map<String,String> f) {
        if (!Set.of("LEGACY_EXPLICIT_ASSIGNMENT_RANGE","LEGACY_FULL_TURN_FALLBACK").contains(f.get("rangeRule"))) return false;
        var start=LocalTime.parse(f.get("effectiveStart"));var end=LocalTime.parse(f.get("effectiveEnd"));
        return end.isAfter(start) && !start.isBefore(LocalTime.parse(f.get("turnStart"))) && !end.isAfter(LocalTime.parse(f.get("turnEnd")));
    }
    private static boolean absent(String value) { return value.equals("ABSENT") || value.equals("NULL"); }
    private static void observe(Map<String,ComparisonCandidateEvidenceV2.Observation> d,String key,String a,String b) {
        d.put(key,new ComparisonCandidateEvidenceV2.Observation(a,b,absent(a)?UNOBSERVABLE:a.equals(b)?MATCH:MISMATCH));
    }
    private static void observeTime(Map<String,ComparisonCandidateEvidenceV2.Observation> d,String key,String a,LocalTime b) {
        d.put(key,new ComparisonCandidateEvidenceV2.Observation(a,b.toString(),absent(a)?UNOBSERVABLE:LocalTime.parse(a).equals(b)?MATCH:MISMATCH));
    }
    private static boolean matches(Map<String,ComparisonCandidateEvidenceV2.Observation> d,String... keys) {
        return Arrays.stream(keys).allMatch(key->d.get(key).match()==MATCH);
    }
    static DetectorCandidate candidate(ProgrammingCandidateSnapshot target,Map<String,ComparisonCandidateEvidenceV2.Observation> dimensions,
            boolean cancellation,String evidenceHash) {
        var evidence=new ArrayList<CandidateEvidenceItem>();var matching=EnumSet.noneOf(EvidenceDimension.class);
        var mapping=new LinkedHashMap<String,EvidenceDimension>();mapping.put("date",EvidenceDimension.DATE);mapping.put("salon",EvidenceDimension.SALON);mapping.put("instructor",EvidenceDimension.INSTRUCTOR);
        if (!cancellation) mapping.put("activity",EvidenceDimension.ACTIVITY);
        mapping.forEach((key,dimension)->{var o=dimensions.get(key);matching.add(dimension);
            evidence.add(new CandidateEvidenceItem(dimension,EvidenceKind.FIELD_MATCH,o.sourceValue(),o.targetValue(),true));});
        return new DetectorCandidate(target.reference(),target.candidateType(),target.snapshotIdentity(),target.candidateFingerprint(),evidence,
                EvidenceKind.FIELD_MATCH,target.observableFields(),matching,Set.of(),EligibilityStatus.ELIGIBLE,List.of(),evidenceHash,target.provenance());
    }
    private static byte[] closedFrame(byte[]... fields) {
        var out=new java.io.ByteArrayOutputStream();out.writeBytes((fields.length+":").getBytes(StandardCharsets.US_ASCII));
        for(var field:fields){out.writeBytes((field.length+":").getBytes(StandardCharsets.US_ASCII));out.writeBytes(field);}return out.toByteArray();
    }
    private static byte[] utf8(String text) { ComparisonCanonicalizerV2.hash("VALIDATE_TEXT",text);return text.getBytes(StandardCharsets.UTF_8); }
    private static String closedHash(String... fields) { return digest(closedFrame(Arrays.stream(fields).map(LegacyCandidateGeneratorV2::utf8).toArray(byte[][]::new))); }
    private static String digest(byte[] bytes) {
        try{return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));}catch(NoSuchAlgorithmException e){throw new IllegalStateException(e);}
    }
    private static void verifyClosedSource(GenericSourceSnapshot s) {
        var p=s.provenance();var f=p.normalizedFields();
        ComparisonFailureV2.require(f.size()==46 && p.ruleId().equals("R2_LEGACY_TURN_PROJECTION") && p.ruleVersion().equals("V1") &&
                f.get("projectionContractId").equals(p.ruleId()) && f.get("projectionContractVersion").equals(p.ruleVersion()) &&
                f.get("projectionCatalogVersion").equals("R2_LEGACY_TURN_V1") && f.get("sourceSystem").equals("LEGACY") &&
                f.get("sourceAtomType").equals(s.sourceAtomType().name()) && f.get("sourceIdentity").equals(s.sourceIdentity()) &&
                f.get("sourceFingerprint").equals(s.sourceFingerprint()) && f.get("snapshotIdentity").equals(s.snapshotIdentity()),"LEGACY","closed source descriptors");
        ComparisonFailureV2.require(f.get("snapshotClaim").equals("R2_INTERNAL_RR_TEST") && f.get("operation").equals("READ_FOR_DATE") &&
                f.get("transactionIsolation").equals("repeatable read") && f.get("transactionAccessMode").equals("read only") &&
                s.observableFields().get("historyMarker").equals("LEGACY_FUNCTIONAL_VALIDITY_NOT_PERSISTED"),"LEGACY","preserved closed declaration labels");
        var o=s.observableFields();boolean assignment=o.get("atomKind").equals("ASSIGNMENT");
        var ids=assignment?List.of(o.get("turnId"),o.get("instructorId"),o.get("activityId")):
                o.get("instructorId").equals("ABSENT")?List.of(o.get("turnId")):List.of(o.get("turnId"),o.get("instructorId"));
        String identity=assignment?"urn:f2e:legacy-assignment:v1:turn="+o.get("turnId")+":member="+o.get("instructorId")+":activity="+o.get("activityId"):
                "urn:f2e:legacy-gap:v1:turn="+o.get("turnId")+":member="+o.get("instructorId")+":marker="+(o.get("atomKind").equals("GAP_ABSENT_MEMBER")?"ABSENT_MEMBER":"ABSENT_ASSIGNMENT");
        ComparisonFailureV2.require(p.recordIds().equals(ids) && s.sourceIdentity().equals(identity),"LEGACY","original R2 physical keys/identity");
        String business=new String(closedFrame(utf8("F2E-R2-BUSINESS-CONTEXT-V1"),utf8(f.get("businessZone")),utf8(f.get("scopeCanonical"))),StandardCharsets.UTF_8);
        ComparisonFailureV2.require(p.businessTimeContext().equals(business),"LEGACY","original R2 business context");
        var keys=new ArrayList<>(s.observableFields().keySet());keys.sort(ComparisonCanonicalizerV2.TEXT_ORDER);
        var map=new ArrayList<byte[]>();map.add(utf8("F2E-R2-NORMALIZED-FIELDS-V1"));map.add(utf8(Integer.toString(keys.size())));
        for(var key:keys)map.add(closedFrame(utf8("F2E-R2-NORMALIZED-FIELD-V1"),utf8(key),utf8(s.observableFields().get(key))));
        String fingerprint=digest(closedFrame(utf8("F2E-SOURCE-V1"),utf8(p.schemaFingerprint()),utf8(p.ruleId()),utf8(p.ruleVersion()),
                utf8("LEGACY"),utf8(s.sourceAtomType().name()),utf8(s.sourceIdentity()),closedFrame(map.toArray(byte[][]::new))));
        String execution=closedHash("F2E-EXECUTION-V1",f.get("runIdentity"),f.get("attemptIdentity"),p.sourceName(),p.schemaFingerprint(),
                f.get("projectionCatalogVersion"),f.get("ruleCatalogVersion"),f.get("businessZone"),f.get("scopeCanonical"));
        String logical=closedHash("F2E-LOGICAL-SNAPSHOT-V1",execution,f.get("snapshotClaim"),f.get("snapshotEvidenceId"));
        String snapshot=closedHash("F2E-ATOM-SNAPSHOT-V1",logical,s.sourceAtomType().name(),s.sourceIdentity(),fingerprint);
        ComparisonFailureV2.require(fingerprint.equals(s.sourceFingerprint()) && execution.equals(f.get("executionProvenanceId")) &&
                logical.equals(f.get("logicalSnapshotId")) && snapshot.equals(s.snapshotIdentity()),"LEGACY","original closed R2 commitment formulas");
    }

}
