# F2E R5 — deterministic pure effective-composition implementation handoff

Version `F2E-R5-IMPLEMENTATION-HANDOFF-V1`. Capture: MATERIALIZED_CANDIDATE / PENDING_FRESH_INDEPENDENT_AUDIT / NOT_ACTIVE.
R5 implementation NOT_AUTHORIZED / NOT_IMPLEMENTED at this capture. Technical acceptance REQUIRED / NOT_EXECUTED.

## Authority reconstruction and precedence

Exact canonical main/base `c3acd8f3aadca247d3a65417a61ffe60032bc692`, normal PR33 merge.
Closed R5 design SHA256 `428d9ca95c59485dde7a274f31ca24bc0dafd94d94bd8476e84993fcb95024bf`.
PR33 published terminal receipt https://github.com/MacacosDevs/feellingPilates/pull/33#issuecomment-5976906695 establishes DESIGN_RECONCILED / INDEPENDENTLY_AUDITED / APPROVED / PUBLISHED / INTEGRATED / CLOSED; immutable pending headers are earlier captures.
R4 implementation CLOSED at PR32 `03f30e52361b1f6a9d0dcede0c15340aa144026b`; closed R3/R4 facts and R1–R4 authority remain byte-identical. The present user explicitly authorizes this handoff lifecycle and a separate activation lifecycle, including fresh independent GPT-6.1-Sol High audits, bounded corrections, publication, normal merge and truthful closure. It authorizes no implementation in this run.

Authority precedence: current explicit user scope plus CLOSED R5 design, CLOSED identity semantics and unchanged R3/R4 contracts; historical adapters/services are semantic provenance where reconciled by R5, never restored runtime authority. Existing state caches/ESTADO reflect prior captures and are not R5 authority. This additive handoff and its separately published terminal/activation evidence supersede those earlier captures for R5 only. R1–R4 CLOSED; R5 design CLOSED; TurnoInstructor LEGACY_VIVO / PRODUCTIVO; dark launch; current client API/Web/Reservations and productive Programación preserved; no cutover, deployment, migrations, database reads or productive wiring. R6 NOT_AUTHORIZED / NOT_STARTED.

The following CLOSED design §§2–7 are reproduced verbatim to make the handoff self-contained. Their conceptual names are now materialized by the exact responsibility/DTO/path contract below; no semantic rule is weakened or added. Historical graph at Git `95900d8a1d787a24aff4ee4e10f69d540ce81339` remains provenance only, not an implementation dependency or a second graph to restore.

## 2. Port and supplied-input boundary

Conceptual new plain immutable API (names specify design, not existing classes):

```text
EffectiveProgrammingComposer.compose(EffectiveCompositionInput)
    -> EffectiveProgrammingCompositionResult
Input = (date, businessZoneId, ruleVersion, envelope, nominalReadSet,
         adjustmentReadSet, validityEvidence)
Result = (date, businessZoneId, ruleVersion, envelope, candidates,
          backingByReference, omissions, suppressions, inputCommitment,
          resultContentFingerprint, resultSnapshotIdentity, evidenceMode)
```

No context from a reader is accepted as composition authority. Input is one exact global date,
explicit business zone, rule `F2E-R5-PURE-COMPOSITION-V1`, the unchanged closed
`NominalProgrammingReadSet` and `AdjustmentReadSet`, plus immutable scalar validity evidence.
No date range, dimension-filtered nominal/adjustment slice, caller-provided effective candidates,
entities, proxies, repositories, callbacks, clock, SQL handles or transaction manager. Deep immutable
copies are required, with no exposed mutable arrays/collections. R5 performs no I/O, logging,
publication, retry, resource admission, shared-context reservation or transaction completion.

Envelope fields: mode `SYNTHETIC_DESIGN_FIXTURE` or `R6_SUPPLIED_COHERENT_READ_FACTS`, date, zone,
invocationIdentity, authorityVersion, completionReceiptIdentity, and participant evidence for
R3, R4 and validity facts. Each participant contains its original source/schema/rule versions,
read scope, executionIdentity, snapshotIdentity, physicalResourceIdentity, transactionBoundaryIdentity,
statementCaptureIdentity and completion status. Preserve original reader metadata/hashes verbatim.
For synthetic mode, physical/completion fields use explicit `NOT_APPLICABLE_SYNTHETIC` and each
participant is marked SYNTHETIC; no database/read/snapshot/completion proof is asserted. Mixed modes
are invalid. Every result carries mode; synthetic output cannot enter a real detector/read-set route.

Real mode is a future admission requirement, not presently runnable authorization. R6 alone must
supply globally complete R3/R4/validity facts from one physical REPEATABLE_READ/read-only snapshot,
with evidence binding actual resources, statement captures and successful transaction completion.
R5 checks declared equality/binding/completeness, required nonblank fields, no synthetic tokens,
consistent date/zone/rules and successful completion. R5 cannot independently authenticate a DB
snapshot or create an R6 capability: metadata comparisons are necessary but not proof. R6 must
validate authenticity and release atomically only after completion. No R5 claim of physical validity
arises from equal manager names, schema hashes, PostgreSQL snapshot text or separate test owners.

Current sealed R3_INTERNAL_RR_TEST and R4_INTERNAL_RR_TEST claims/reservations remain untouched.
R6 requires separate authority to reconcile participant contexts/managers and descriptor compatibility;
no chained independent owners, enum extension, copied contexts or bypass is prescribed here.
Actual V47/50 compatibility must be proved in that future envelope without weakening closed R3's
V46/49 acceptance configuration. Until then real integration remains NOT_AUTHORIZED, not a hidden
R5 prerequisite or permission to reopen readers.

## 3. Input graph and validity evidence

R5 revalidates structural content even when read-set constructors previously ran. N is all applicable
nominal occurrences on date, keyed by `(SERIE_ASIGNACION, assignmentSeriesId, date)`; cardinality
exactly one per reference. Candidates must be NOMINAL_OCCURRENCE, bijective with backing, and
all candidate dimensions/reference/date agree with backing. Retain every NominalBackingSnapshot
field, including physical assignment/block IDs, series, applicability, flags and technical timestamps,
and candidate observables/provenance/fingerprints. Enforce closed R3 applicability/form/hash rules;
never repair or recompute its stored hashes. Duplicate applicable versions abort, including equal bytes.

A is all active exact-date adjustment facts, bijective by adjustment UUID/date with twelve-field
backing. Preserve source atom, source identity, observable map, provenance and both source/snapshot
hashes. Revalidate closed R4 form, normalization, active/date, twelve fields, source/backing agreement,
hash and unique target constraints. CANCELACION/REEMPLAZO require a series and ADICION forbids it;
cancellation has no five result fields; other forms require all five and positive microsecond interval.
Inactive/wrong-date rows in this supplied active read set are invalid input, not silently filtered.
Repeated adjustment ID or multiple active targets per series/date abort even if content is identical.

Validity evidence is a supplied complete immutable graph for all preliminary final outcome dimensions.
It is supporting evidence, never a SourceSnapshot or occurrence candidate. Required scalar content:

| Relation | Values and absence proof |
| --- | --- |
| Salon by final ID | queried ID, PRESENT(id, activo) or explicit proven ABSENT |
| Effective hours by final salon/date | all applicable active exact-date exceptions (id, salonId, date, cerrado, opening, closing, activo); all applicable weekly versions (id, salonId, day, opening, closing, validFrom, validUntil); explicit complete empty relation where none |
| Instructor by final ID | queried ID, PRESENT(id, estatus) or proven ABSENT; complete role edges (usuarioId, rolId, rol.nombre, nullable salonId) and specialization edges (usuarioId, activityId) |
| Activity by final ID | queried ID, PRESENT(id, activo) or proven ABSENT |
| Salon offering | complete (salonId, activityId) relation for each required salon |

Every relation carries source/schema/rule versions, supplied participant binding, query scope/coverage,
physical record or composite edge keys and typed scalar values. Missing map key means missing evidence,
not proven ABSENT/empty. Unknown, malformed, duplicate conflicting key, ambiguous applicable hours,
wrong IDs/date/coverage/participant binding abort the unit. Distinct role/edge records are retained;
identical content does not license discarding physical records. A relation not needed after the first
validity failure can remain unevaluated, but required graph coverage must still be complete for all
preliminary outcomes. No operation/master facts are required solely for a cancelled nominal; its
suppression depends on target and adjustment, never removed nominal operational validity.

R6 will own acquisition/catalog/descriptor/capture scope; R5 imposes no SQL, reader or entity strategy.
Evidence schema identity must be supplied from validated facts, never fabricated from current date,
a migration count or text label. Synthetic fixtures explicitly state their own fixture descriptors.

## 4. Composition and exact adjustment semantics

Order: validate inputs → index N and A globally → resolve target adjustments → construct preliminary
outcomes P and cancellation proofs S → evaluate final validity → validate survivors E → complete
accounting/backing/hash validation → return immutable result. No nominal operation/master filtering
before target lookup. Never use proximity, UUID order, timestamps, first/latest, scoring or winner selection.

| Case | Nominal requirement | Preliminary outcome/reference | Mandatory backing |
| --- | --- | --- | --- |
| Unadjusted nominal | exactly one | RECURRENT_OCCURRENCE; original five dimensions; same SERIE_ASIGNACION/date | original nominal candidate and all backing |
| CANCELACION | exactly one target | no P or E for target; one suppression proof with same nominal reference | unique target nominal plus full cancellation source/backing |
| REEMPLAZO | exactly one target | REPLACEMENT_OCCURRENCE; replace all five dimensions; same SERIE_ASIGNACION/date | original nominal plus unique full replacement source/backing |
| ADICION | no nominal target, nominal axis NOT_APPLICABLE | ADDITION_OCCURRENCE; full result; AJUSTE/adjustmentId/date | full addition source/backing, explicitly no nominal |

Replacement removes the recurrent outcome and creates exactly one replacement preliminary outcome;
it cannot change identity to adjustment ID or append a second occurrence. Equal nominal/result
values still yield replacement origin with adjustment provenance. Addition never creates a synthetic
series; equal result dimensions from distinct addition IDs remain separate until set-conflict checks.
Cancellation never emits a fake empty effective candidate and cannot be inferred from salon closure.

Orphan cancellation/replacement (target zero) is total `TARGET_NOMINAL_MISSING` composition rejection;
more than one target is `AMBIGUOUS_OR_CONTRADICTORY_INPUT`. These preserve failing reference and all
involved immutable evidence; no successful partial result or fabricated nominal. R4 remains a valid
facts read when the target is absent. Detector-level MISSING/BLOCKER is a separate core classification
and is not returned as a successful R5 result. Simultaneous cancellation/replacement, multiple targeted
replacements or duplicate cancellations abort; an addition cannot target a series. Contradictory
source/backing, wrong reference type/origin or unexplained disappearance also abort.

## 5. Final graph validity, omissions and conflicts

For each P, evaluate these historical read-time predicates in this exact priority. An omission records
the first failing cause, reference, candidate origin, all five final dimensions, original backing,
relevant supporting graph and rule/schema/envelope provenance. It is not a writer-time validation.

1. Salon present and active: `SALON_INEXISTENTE_O_INACTIVO`.
2. Final salon operational on date: `SALON_NO_OPERATIVO_EN_FECHA`.
3. Entire final [start,end) contained in effective hours: `AJUSTE_FUERA_DE_HORARIO_EFECTIVO`.
4. Instructor present and estatus activo: `INSTRUCTOR_INEXISTENTE_O_INACTIVO`.
5. INSTRUCTOR role global (null salon) or final-salon scoped: `ROL_INSTRUCTOR_AUSENTE`.
6. Activity present and active: `ACTIVIDAD_INEXISTENTE_O_INACTIVA`.
7. Instructor specialization contains final activity: `ESPECIALIDAD_AUSENTE`.
8. Final salon offering contains final activity: `ACTIVIDAD_NO_OFRECIDA_POR_SALON`.

One active date exception overrides weekly hours: closed → closed; open → its hours even without a
weekly template. With no exception, one applicable weekly version supplies hours; none → nonoperational.
More than one applicable exception/weekly version is ambiguous evidence, never omission/winner.
Intervals are positive half-open [start,end), microsecond precision, one LocalDate; containment permits
matching opening/closing endpoints. No rounding, overnight reinterpretation, trimming or merging.
Business zone is explicit provenance, not a UTC conversion of local scheduling times.

Filter documented omissions O before duplicate operative-key and instructor-overlap checks on E.
Repeated operative key `(salon,instructor,activity,date,start,end)` aborts even for distinct references.
Same instructor with positive overlap `a.start < b.end && b.start < a.end` aborts, including cross-salon.
Adjacent intervals are allowed. No new blanket salon-only overlap constraint or capacity rule is created.
Repeated reference is always an invariant failure before validity filtering; omissions cannot conceal
contradictory identities. Distinct additions omitted for real validity causes remain separately accounted.

Replacement/addition outcome validation is limited to this nominal/effective graph: complete exact
result equality, supported origin/reference, required backing, final operational/master rules and
historically authorized set conflicts. No reservation/history/crosswalk/legacy intent/commercial policy,
productive mutation, selected target or new domain predicate. Invalid final operation/master state
is an explained omission, not an operational read failure or successful adjustment outcome assertion.

R5 produces evidence, not DetectorResult/status. A valid cancellation proof supports future core
EXPECTED_ABSENCE/SUPPRESSED only with nominal=1 and E=0. Replacement/addition omission supports
DIVERGENT_INCOMPATIBLE, never EXPECTED_ABSENCE. Unadjusted omission proves current operational/master
incompatibility, not cancellation. Missing evidence/unexplained absence aborts rather than inventing a cause.

## 6. Exact success contract and failure boundary

`compose` succeeds iff every input admission/content/coverage check, semantic predicate, graph
invariant and canonicalization check succeeds. Success means a complete computed effective universe,
not that all adjustments have a valid present outcome. Omissions can exist in a successful result.
There is no externally visible provisional/partial result or best-effort prefix.

Let N be unique nominal references, T the unique cancellation/replacement targets, D addition references,
S cancellation-suppressed references, P preliminary references, O omissions and E effective candidates:

```text
T subset N; S subset T; D disjoint N (reference type is part of identity)
P = (N minus S) union D
P = E disjoint-union O
N union D = E disjoint-union O disjoint-union S
|P| = |N| - |S| + |D|; |E| + |O| + |S| = |N| + |D|
```

Every adjustment is consumed once as suppression, replacement of its nominal, or addition; each nominal
is consumed once as unadjusted, replaced or cancelled. Replacement count does not increase |P|.
Each P reference has exactly one compatible backing; every effective candidate has one backingByReference
entry and no extra entries exist. O and S retain their own backing with no effective map entry. Result
also retains the input envelope/commitment so empty sets do not lose read-completeness provenance.
Each emitted E has correct type/reference/date, exact final dimensions, valid final graph and no set
conflicts. Each O has exactly one supported first cause and its exact P/backing. Each S has exactly
one valid cancellation and exactly one nominal with neither P, O nor E for that reference.
No unaccounted input/source/backing, unexplained absent result or unbacked occurrence is permitted.

An empty E is valid for empty completed inputs, all valid cancellations, all documented omissions,
or their mixture after all checks; empty input lists do not themselves prove completion. An orphan
adjustment or failed/absent query cannot become empty success. No authoritative successful read output
exists today merely because synthetic composition passed.

Design failure categories: `COMPOSITION_INPUT_INVALID`, `INPUT_EVIDENCE_INCOMPLETE`,
`INPUT_ENVELOPE_INVALID`, `TARGET_NOMINAL_MISSING`, `AMBIGUOUS_OR_CONTRADICTORY_INPUT`,
`READ_SET_INVARIANT_VIOLATION`, `EFFECTIVE_SET_CONFLICT`, `CANONICALIZATION_FAILURE`.
These are proposed R5 exceptions, not changes to closed reader/core enums. Failure carries date,
ordered involved references/IDs, precise invariant/rule and available immutable evidence; preserve
original cause where applicable. Unknown programmer failure propagates, never converts to omission.
Upstream database/schema/privilege/timeout/probe/rollback/completion failures retain their own causes,
SQLState and closed categories and prevent real admission/release. No retry, fallback, repair or silent drop.

## 7. Deterministic ordering and commitments

External collections are canonical, independent of input iteration/map insertion order. R5 validates
closed input list order, preserves their original bytes/order in retained evidence, and sorts its derived
views. Reference order is `(date ISO, type rank SERIE_ASIGNACION=0/AJUSTE=1, unsigned UUID 128-bit)`.
Candidates, backing-map entries, omissions and suppressions follow reference order. Adjustment evidence
uses `(date,unsigned adjustment UUID)`; nominal evidence `(reference, unsigned assignment UUID,
unsigned block UUID)`. Supporting relation rows sort by relation name then canonical composite key;
optional null salon in role key sorts before present UUID. Conflicting repeated keys reject, not dedup.
Failure diagnostics sort references/physical IDs with these same rules; ties never select a winner.

R5 canonical value format V1: each value is an ASCII-tagged recursive length-framed sequence.
`seq(parts)` bytes are ASCII decimal count + ':' followed by decimal byte-length + ':' + raw bytes for
each part, exactly the framing of ReadSnapshotIdentifiers.secuencia. Strings use strict UTF-8 (no NUL,
no unpaired surrogate, no locale-dependent coercion); scalars use UUID lowercase, ISO date, time
HH:mm:ss.ffffff, UTC instant uuuu-MM-ddTHH:mm:ss.ffffffZ, booleans true/false and exact enum names.
Null is `seq('ABSENT')`, distinct from `seq('VALUE', type, normalizedValue)` including empty text.
Records are `seq('RECORD', typeName, version, fieldCount, fieldName, encodedValue, ...)`, fields sorted
by unsigned UTF-8 field-name bytes; all declared fields appear. Lists are `seq('LIST', count, values...)`
in the orders above. Maps are `seq('MAP', count, encodedKey, encodedValue, ...)` sorted by canonical key
bytes. This applies recursively to every field of the input/result and original reader records/provenance,
including maps and typed absence; no toString/semanticHash/delimiter concatenation is a substitute.
Every original reader stored fingerprint remains an opaque preserved field, with its own rule validation.

Define H(domain, values...) = lowercase SHA256(seq(UTF8(domain), encoded values...)). No self-hash:

- inputCommitment = H(`F2E-R5-INPUT-V1`, full Input including all preserved reader identities/envelope/evidence).
- candidateFingerprint = H(`F2E-R5-CANDIDATE-V1`, reference, origin, five final dimensions, ruleVersion).
- resultContentFingerprint = H(`F2E-R5-CONTENT-V1`, date, zone, ruleVersion, candidates excluding their
  snapshotIdentity but including candidateFingerprint, full backing, full omissions, full suppressions).
- resultSnapshotIdentity = H(`F2E-R5-SNAPSHOT-V1`, inputCommitment, resultContentFingerprint, full envelope).
- candidate snapshotIdentity = H(`F2E-R5-CANDIDATE-SNAPSHOT-V1`, resultSnapshotIdentity, reference,
  candidateFingerprint). Candidate provenance normalizedFields contains rule/date/zone, original inputs
  and supporting commitments but excludes result/candidate snapshotIdentity to avoid recursive hashing.

Candidate provenance: sourceName `R5_PURE_EFFECTIVE_GRAPH`; ruleId `F2E_R5_PURE_COMPOSITION`, version V1;
businessTimeContext zone/date; schemaFingerprint is H(`F2E-R5-SCHEMA-BUNDLE-V1`, all supplied participant
schema descriptor identities in participant-name order), a labeled composite commitment, never a claim
of new physical schema. recordIds are role-qualified original nominal/adjustment/support record keys in
canonical order; full originals remain in backing, not compressed away. NormalizedFields exposes exact
reference/origin/final dimensions and input/backing/support commitments. These fields are fixed before
content hashing; backing/support commitments use H with domains `F2E-R5-BACKING-V1` and
`F2E-R5-SUPPORT-V1` over the full canonical corresponding records, excluding derived result hashes.
Hash collision detected within a composition (same digest/different bytes) aborts.

Equal reference is not equal content, backing or execution. CandidateFingerprint is stable across an
otherwise identical attempt with unchanged reference/origin/dimensions/rule. Input/content/snapshot
commitments can change with backing versions, supporting facts or existing R3 provenance; no promise
that closed R3/R4 hashes are attempt invariant. Changed adjustment identity or technical fields changes
retained evidence/commitments even if result dimensions coincide. Permuting valid input map/derived
view insertion order changes no commitment; changing any retained field changes its enclosing preimage.


## 8. Concrete isolated implementation responsibility and DTO contract

All ten main files use package `com.feelingpilates.transicion.programacion.r5.composition`; all ten test files use the corresponding test package. Public classes are plain final/records; immutable nested typed records/enums are defined in their owning file, so no implicit extra-file authority. Java21/JUnit5 already exist; no dependency/configuration edit.

| File/class | Exact responsibility |
| --- | --- |
| EffectiveProgrammingComposer | Public `compose(EffectiveCompositionInput)` returning EffectiveProgrammingCompositionResult; stateless pure admission → targeting → validity → conflict/accounting → hash pipeline. No external collaborator with effects. Helpers can be private/nested in this authorized file. |
| EffectiveCompositionInput | Fields exactly date LocalDate, businessZoneId String (valid ZoneId), ruleVersion String, envelope EffectiveCompositionEnvelope, nominalReadSet NominalProgrammingReadSet, adjustmentReadSet AdjustmentReadSet, validityEvidence EffectiveValidityEvidence. Deep immutable original evidence retained. No read port/context/reader parameter. |
| EffectiveCompositionEnvelope | Typed mode/date/zone/invocationIdentity/authorityVersion/completionReceiptIdentity, immutable participant map keyed exactly R3/R4/VALIDITY. Participant records carry every field in §2 plus original scalar execution descriptor: runIdentity, attemptIdentity, ruleCatalogVersion, databaseName, schemaName, principal, projectionCatalogVersion, readerInvocationIdentity, snapshotClaim, snapshotEvidenceId, statementCaptureCommitment. These are data, never reader context objects/capabilities. Complete scope explicit even for empty readers. |
| EffectiveValidityEvidence | Typed immutable relations in §3: keyed salon/instructor/activity presence maps, per-salon/date hours, role/specialization/offering edge relations. Nested RelationMetadata(source/schema/rule versions, participantName, scope, complete, physical/composite record keys), Presence(PRESENT/ABSENT, queriedId, optional typed value), Salon(id,activo), Instructor(id,estatus), Activity(id,activo), DateException(id,salonId,date,cerrado,opening,closing,activo), WeeklyHours(id,salonId,day,opening,closing,validFrom,validUntil), RoleEdge(usuarioId,rolId,roleName,nullableSalonId), SpecializationEdge(usuarioId,activityId), OfferingEdge(salonId,activityId). Complete relation rows carry their metadata and physical/composite key; no generic Object/entity payload. |
| EffectiveProgrammingCompositionResult | Exactly §2 result fields. candidates List<ProgrammingCandidateSnapshot>, backingByReference immutable ordered Map<ReferenciaOcurrencia,EffectiveCompositionBacking>, omissions/suppressions typed immutable lists; full input retained (including originals and full validity graph), not just its digest. Every result contains mode and complete envelope even if E empty. Public construction revalidates partition/bijection/order/hash contract or is controlled inside composer; no inconsistent public result. |
| EffectiveCompositionBacking | Nested origin and all five final dimensions plus reference, original nominal candidate+NominalBackingSnapshot present iff recurrent/replacement; original adjustment GenericSourceSnapshot+AdjustmentBackingSnapshot present iff replacement/addition. Typed NOT_APPLICABLE nominal for addition and adjustment for recurrent; complete applicable support relation facts retained with their metadata. |
| EffectiveCompositionOmission | Reference, exact preliminary backing/origin/final dimensions, first supported cause enum in §5 order, supporting facts, rule/schema/envelope provenance. Distinct from failure/suppression and has no effective backing-map entry. |
| EffectiveCompositionSuppression | Reference, exactly original nominal candidate/backing and unique cancellation source/backing, rule/schema/envelope provenance. Never fabricated effective candidate or operational omission. |
| EffectiveCompositionFailure | R5-only exception; category exactly §6, date, deterministic involved references/IDs, precise rule/invariant, available immutable original/support evidence and original cause. Unknown programmer failures propagate. No changes to closed enums/exceptions. |
| EffectiveCompositionCanonicalizer | Pure V1 recursive framing, explicit fixed field schemas of these records and all original reader DTOs, strict scalar normalization/order/domains and collision check from §7. No reflection over arbitrary objects, toString serialization, clock, I/O, reader invocation or SemanticHash substitution. |

PRESENT requires matching queried/entity IDs and required scalar fields; ABSENT requires no entity value and explicit complete absence provenance. Status strings preserve source values: instructor `activo` is active; valid source statuses are exactly `activo`, `suspendido`, `eliminado` and role name `INSTRUCTOR` is exact; unknown status value is malformed evidence rather than invented omission. Hours use PostgreSQL day encoding Sunday0..Saturday6, inclusive validFrom/validUntil, open upper endpoint permitted, exact-date exception date/salon binding and active true; shape closed=(both times absent), open=(both times present positive time6). Distinct duplicate physical/composite keys reject even identical; shared nominal block physical IDs may recur only with identical complete block payload as closed R3 permits. Repeated assignment IDs reject. No fabricated physical primary key for composite edges.

Admission reimplements the closed PURE scalar validations under these R5 files; it may use immutable DTO accessors and ReadSnapshotIdentifiers pure methods, never instantiate/invoke R3/R4 mappers, readers, ports, contexts, services or repositories. Original snapshot contexts are PROVENANCE_ONLY sources for algorithm reconstruction. Nominal backing constructor is insufficient: explicitly repeat block/assignment IDs, flags, applicability, time containment, time6/timestamp6, day, physical consistency and closed list order `(unsigned seriesId,unsigned assignmentId)` before derived sorting. Reconstruct exact original normalizedFields: all closed mapper physical names (including `NULL` optional until tokens), fecha/day and snapshotEvidenceId/executionProvenanceId/statementCaptureCommitment; require exact map equality, record IDs, source/schema/rule/zone/date envelope agreement. Closed R3 observable map has exactly salonId/instructorId/activityId/start/end/fecha. R3 candidateFingerprint is closed hash seqText(`F2E-R3-CANDIDATE-V1`, closed reference.toString(), decoded mapaCanonico(observable)); projection fingerprint is closed hash seqText(`F2E-R3-PROJECTION-V1`, decoded mapaCanonico(physical)); snapshotIdentity is closed hash seqText(`F2E-R3-SNAPSHOT-V1`, original executionProvenanceId, projectionFingerprint). The closed reference.toString use applies only to validation of a preserved R3 hash, never R5 canonicalization.

Closed hash here means lowercase SHA256(ReadSnapshotIdentifiers.secuenciaTextos(fields)); preserve stored value and reject discrepancy, never repair it. R3 original execution hash inputs, in order: `F2E-R3-EXECUTION-V1`, runIdentity,attemptIdentity,sourceName,schemaFingerprint,ruleCatalogVersion,zone,`R3_NOMINAL_ON_DATE_V1/date/day`,readerInvocationIdentity,databaseName,schemaName,principal,physicalResourceIdentity,`R3_NOMINAL_V1`,`R3_INTERNAL_RR_TEST`,snapshotEvidenceId,statementCaptureCommitment. R4 original execution hash inputs: `F2E-R4-EXECUTION-V1`,runIdentity,attemptIdentity,`R4_ACTIVE_ADJUSTMENTS_ON_DATE_V1/date`,zone,ruleCatalogVersion,sourceName,schemaFingerprint,databaseName,schemaName,principal,physicalResourceIdentity,`R4_ADJUSTMENT_V1`,readerInvocationIdentity,`R4_INTERNAL_RR_TEST`,snapshotEvidenceId,statementCaptureCommitment. Original declared claims remain closed reader evidence labels, not coherent R6 authority.

Revalidate R4 forms/timestamp range (-4712..294276, not OffsetDateTime.MIN/MAX), all twelve normalizedFields with `ABSENT_BY_ADJUSTMENT_FORM` null token, source atom NEW_CANCELACION/NEW_REEMPLAZO/NEW_ADICION and NEW_DARK_LAUNCH, exact sourceIdentity/recordIds/provenance, rule `R4_ADJUSTMENT_PROJECTION`/V1 and zone/date. Original R4 sourceFingerprint = closed hash seqText(`F2E-R4-SOURCE-V1`,`NEW_DARK_LAUNCH`,atom,id,decoded mapaCanonico(twelveFields)); snapshotIdentity = closed hash seqText(`F2E-R4-SNAPSHOT-V1`,original executionProvenanceId,sourceFingerprint). Closed originals remain stored as supplied. Complete scalar envelope descriptor allows these validations for synthetic fixtures without creating closed read contexts or physical proof. Synthetic mode uses NOT_APPLICABLE_SYNTHETIC for envelope physical/completion fields and explicitly synthetic fixture execution descriptor fields; original stored hashes still obey the closed formula. Real descriptor metadata equality remains insufficient authenticity, and no real route is enabled.

R5 candidate observableFields have exactly salonId/instructorId/activityId/start/end/fecha (closed normalized scalar format); provenance normalizedFields also exposes referenceType/referenceId/origin/rule/date/zone/inputCommitment/backingCommitment/supportCommitment. Preserve full originals in backing/input, with role-qualified recordIds. R5 candidate fingerprints and recursive encodings follow §7, independent of these closed validation formulas. Every declared DTO field has a fixed name, version and typed absence in canonical schema; a null map/list/required field is invalid, not an empty relation. Canonical field-name/map-key ordering is unsigned UTF8; typed integral counts/day normalize ASCII decimal with no leading zero or plus. Strings/UUID/date/time/instant/bool/enums follow §7. No nanosecond rounding; UTC conversion for technical timestamps only, no local scheduling conversion. Validate all included support rows before returning even if first omission short-circuits semantic predicates.

R6 alone obtains mutually coherent inputs in a future shared physical read-only REPEATABLE_READ snapshot, supplies authenticity/capture/descriptor/completion and atomically releases after successful completion. R5 has no physical owner/reservation/read edge and cannot authenticate strings. R3 V46/49 vs R4 V47/50 compatibility is future R6 REQUIRED/NOT_EXECUTED, not permission to relax/reopen readers. Productive caller/bean/read-set detector admission of synthetic output is prohibited.

## 9. Exhaustive implementation acceptance (REQUIRED / NOT_EXECUTED)

All future tests below use explicit SYNTHETIC_DESIGN_FIXTURE mode, typed scalars and already-supplied unchanged closed DTOs; no DB or reader call in R5 suites. Assertions must independently compute expected identity/dimensions/backing/partition/cause/hashes, not compare composer to itself. Fixed deterministic seeds and bounded enumerations; no new property-testing dependency. Retain seeds, generated case counts, failure cases, raw Maven logs and Surefire XML; counts derived at execution, never inherited 583.

| Suite (NEW basename) | Mandatory observable acceptance and negative controls |
| --- | --- |
| EffectiveProgrammingComposerTest | Unadjusted recurrent, unique cancellation, all-five replacement including moved final salon and equal final values, addition without nominal; exact origins/reference types/IDs/date/backing; cancellation of operationally invalid nominal without its master facts succeeds. Two distinct identical-dimension additions survive targeting and then conflict if both E, or remain distinct O if omitted. Orphan cancellation/replacement whole-unit rejection; no fake nominal/partial result. |
| EffectiveCompositionAdmissionTest | Null/malformed input and each required DTO scalar; date/zone/rule/source/schema/provenance/scope/coverage mismatch; active/wrong date/forms/target/result/time/timestamp; all original stored hashes and maps/record IDs; original closed list order; duplicate references/physical assignment IDs, inconsistent shared block, adjustment IDs/active targets including equal bytes and cancellation+replacement. Empty lists with absent completion/coverage fail. Explicit real metadata checks (no synthetic token, successful completion, equal physical boundary/captures) can only validate declared data, never prove physical authenticity; mixed-mode/participant labels/forged binding negatives. |
| EffectiveCompositionValidityTest | Each eight first omission predicates independently and combinations prove exact priority; explicit ABSENT versus missing evidence; role global/final-salon versus other salon; specialization/offering; inactive master. Date closed/open exception precedence including no weekly, zero/one/multiple applicable weekly/exception, inclusive/open validity/day and final-salon lookup; exact containment endpoints, microsecond precision, negative/zero/overnight intervals rejected. Structurally invalid/unknown/conflicting/unbound/incomplete relation anywhere rejects even behind earlier omission; complete but semantically unused valid relation remains retained. |
| EffectiveCompositionInvariantTest | E/O/S disjoint/exhaustive=N∪D, P=(N−S)∪D; every input consumed once, replacement preserves count, bijective E backing, separate O/S; empty input/all cancellation/all omission/mixed empty success only with full coverage. Constructed/mutated result mismatch/extra/missing/unbacked candidate and inconsistent partition fails. Surviving duplicate operative key and instructor positive overlap (cross-salon too) total rejection after O removed; adjacency passes; same-salon overlap by different instructors passes. No winner, retry, dedup or omission hiding duplicate identities. |
| EffectiveCompositionCanonicalizerTest | Literal independent golden byte+SHA256 vectors for seq(empty), null, empty string, strings with delimiter/Unicode, nested full record/list/map and complete composition. Reject NUL/unpaired surrogate/nanos; distinct ABSENT/value/tokens/empty; unsigned UUID extremes and reference type/date order; exact domain/version/field schema; original reader fingerprints opaque after validated own formulas; nonrecursive dependency order, composite schema labeling and collision-control seam confined to tests/private pure helper. |
| EffectiveCompositionPropertyTest | Bounded cross-product of 0..3 nominal references and compatible unique adjustment subsets/types plus additions/validity causes. Independent algebra oracle asserts all cardinality/identity/accounting/backing rules; input map/support insertion permutations preserve outputs/commitments, while reordering closed reader lists rejects. Idempotent same input, origin-sensitive equal replacement, every retained field perturbation changes enclosing preimage, candidate semantic hash stability across execution-only changes, original/backing/support sensitivity, no partial success on injected contradiction/orphan. Enumerate overlap/adjacency and exact time6 boundary cases. |
| EffectiveCompositionImmutabilityTest | Mutate all caller-owned lists/maps/nested relation builders after construction; inputs/results/provenance/backing/failures remain unchanged; returned collections reject mutation, no exposed arrays/handles; repeated/concurrent compose on same input is equal and stateless. |
| EffectiveCompositionArchitectureTest | Exact ten-main/ten-test Java inventory; missing/extra/partial/stereotype/import/caller negative controls. Main edges allow only JDK pure computation, listed immutable closed DTOs/reference/ReadSnapshotIdentifiers and own package; deny Spring/JPA/JDBC/SQL/transaction/repositories/entities/read ports/contexts/mappers/readers/productive services/files/network/clock/logging/thread effects and forbidden reflection/dynamic escape. Scan all production sources for callers/beans/reachability into R5, expect zero. Preserve old recursive inventories unmodified. |
| EffectiveCompositionRuntimeIsolationTest | Existing production ApplicationContext under established test profiles contains no R5 bean, dependency, registration, factory or productive caller. Classpath/source dependency checks corroborate static gate, with independent negative registration/caller fixture. Test-only inspection uses existing Spring test dependency, not R5 runtime dependency or read owner. Synthetic result never presented as real DetectorEvaluationRequest/admitted route. |
| EffectiveCompositionTestFixtures | Pure deterministic typed fixture builder and independent algebra/canonical oracle only, nested records in this file; no readers/context acquisition/DB/resources. Inputs retain explicit synthetic envelope and original closed formulas. Not itself a Test suite or technical gate. |

Regression gates use actual canonical paths in READ_ONLY below: every existing detector suite including F2DAuthorityGuard, all R1–R4 mapper/executor/transaction/PG/concurrency/architecture/runtime-isolation suites, all current programacion/ubicaciones/turnos/reservas Lane1–4 behavior suites and full default repository regression. Existing PostgreSQL/HostValidator host requirements for those existing suites remain REQUIRED; unavailable host blocks their acceptance, no skip/guard masking/config change. This is regression of prior slices, no R5 database acceptance or newly coherent shared-snapshot proof. Physical shared snapshot/no-write/concurrency/completion/actual descriptor gates for R5 acquisition belong to future R6 and stay REQUIRED/NOT_EXECUTED, not waived or PASS.

Commands from repository root with unchanged Java21/Maven/Testcontainers setup: `./mvnw -DskipTests compile`; `./mvnw -Dtest='EffectiveProgrammingComposerTest,EffectiveCompositionAdmissionTest,EffectiveCompositionValidityTest,EffectiveCompositionInvariantTest,EffectiveCompositionCanonicalizerTest,EffectiveCompositionPropertyTest,EffectiveCompositionImmutabilityTest,EffectiveCompositionArchitectureTest,EffectiveCompositionRuntimeIsolationTest' test`; all existing READ_ONLY *Test.java suites selected by their exact basenames (derive comma list from the sealed path set and record executed command); then `./mvnw test`. Exit0 and zero failed/errors/skips; exact full inventory/report reconciliation and competent real existing PG host evidence required. `git diff --check`, exact NEW inventory/write-scope hash, full baseline/migrations/source authority preservation and fresh independent exact implementation-candidate acceptance required. No technical tests/build/database/HostValidator are executed or marked PASS by this documentary run.

## 10. Exact implementation path sets and DEFAULT_DENY

NEW absent on base; MODIFIED empty; READ_ONLY consumable but immutable; PROVENANCE_ONLY may be inspected to reconstruct authority/algorithms but not imported/invoked as R5 implementation dependencies. Test-source READ_ONLY entries support independent regression/architecture inspection only. Main dependency permission is restricted further by §8/9 to pure DTOs/utilities, never all READ_ONLY paths. No wildcard/directory write authority. Every path outside NEW∪MODIFIED is write-denied; newly needed path, changed public contract or any closed R3/R4 authority modification requires STOP/HUMAN_GATE before writing. All50 migrations, all existing guards/configuration/productive code unchanged. NEW responsibility is not permission for unrelated code.

Path hash algorithm: deduplicate, sort unsigned UTF8 bytes, join LF and append LF for nonempty set; empty set zero bytes; lowercase SHA256. WRITE_SCOPE=NEW∪MODIFIED; TOTAL_ACTIVE_PATHS=WRITE_SCOPE∪READ_ONLY; provenance separate. Content seals below use Git blob bytes at frozen base (physical manifests separately preserve checkout/EOL normalization); no prospective content hash for absent NEW files.

### CURRENT_R5_AUTHORIZED_NEW

```text
src/main/java/com/feelingpilates/transicion/programacion/r5/composition/EffectiveCompositionBacking.java
src/main/java/com/feelingpilates/transicion/programacion/r5/composition/EffectiveCompositionCanonicalizer.java
src/main/java/com/feelingpilates/transicion/programacion/r5/composition/EffectiveCompositionEnvelope.java
src/main/java/com/feelingpilates/transicion/programacion/r5/composition/EffectiveCompositionFailure.java
src/main/java/com/feelingpilates/transicion/programacion/r5/composition/EffectiveCompositionInput.java
src/main/java/com/feelingpilates/transicion/programacion/r5/composition/EffectiveCompositionOmission.java
src/main/java/com/feelingpilates/transicion/programacion/r5/composition/EffectiveCompositionSuppression.java
src/main/java/com/feelingpilates/transicion/programacion/r5/composition/EffectiveProgrammingComposer.java
src/main/java/com/feelingpilates/transicion/programacion/r5/composition/EffectiveProgrammingCompositionResult.java
src/main/java/com/feelingpilates/transicion/programacion/r5/composition/EffectiveValidityEvidence.java
src/test/java/com/feelingpilates/transicion/programacion/r5/composition/EffectiveCompositionAdmissionTest.java
src/test/java/com/feelingpilates/transicion/programacion/r5/composition/EffectiveCompositionArchitectureTest.java
src/test/java/com/feelingpilates/transicion/programacion/r5/composition/EffectiveCompositionCanonicalizerTest.java
src/test/java/com/feelingpilates/transicion/programacion/r5/composition/EffectiveCompositionImmutabilityTest.java
src/test/java/com/feelingpilates/transicion/programacion/r5/composition/EffectiveCompositionInvariantTest.java
src/test/java/com/feelingpilates/transicion/programacion/r5/composition/EffectiveCompositionPropertyTest.java
src/test/java/com/feelingpilates/transicion/programacion/r5/composition/EffectiveCompositionRuntimeIsolationTest.java
src/test/java/com/feelingpilates/transicion/programacion/r5/composition/EffectiveCompositionTestFixtures.java
src/test/java/com/feelingpilates/transicion/programacion/r5/composition/EffectiveCompositionValidityTest.java
src/test/java/com/feelingpilates/transicion/programacion/r5/composition/EffectiveProgrammingComposerTest.java
```

### CURRENT_R5_AUTHORIZED_MODIFIED

EMPTY_SET (zero bytes).

### CURRENT_R5_READ_ONLY

```text
auditoria/fase-2e-identidad-semantica-detector-read-only.md
auditoria/fase-2e-r3-diseno-reader-programacion-nominal-reconciliado.md
auditoria/fase-2e-r4-diseno-reader-ajustes-fecha-reconciliado.md
auditoria/fase-2e-r5-diseno-composicion-efectiva-reconciliado.md
src/main/java/com/feelingpilates/programacion/dominio/ReferenciaOcurrencia.java
src/main/java/com/feelingpilates/transicion/programacion/detector/DetectorVocabulary.java
src/main/java/com/feelingpilates/transicion/programacion/detector/EvidenceProvenance.java
src/main/java/com/feelingpilates/transicion/programacion/detector/F2DAuthorityGuard.java
src/main/java/com/feelingpilates/transicion/programacion/detector/GenericSourceSnapshot.java
src/main/java/com/feelingpilates/transicion/programacion/detector/ProgrammingCandidateSnapshot.java
src/main/java/com/feelingpilates/transicion/programacion/r4/read/AdjustmentBackingSnapshot.java
src/main/java/com/feelingpilates/transicion/programacion/r4/read/AdjustmentReadSet.java
src/main/java/com/feelingpilates/transicion/programacion/read/NominalBackingSnapshot.java
src/main/java/com/feelingpilates/transicion/programacion/read/NominalProgrammingReadSet.java
src/main/java/com/feelingpilates/transicion/programacion/read/ReadSnapshotIdentifiers.java
src/test/java/com/feelingpilates/programacion/ProgramacionPersistenciaTest.java
src/test/java/com/feelingpilates/programacion/repositorio/BloqueProgramacionRepositoryVigenciaTest.java
src/test/java/com/feelingpilates/programacion/servicio/BloqueProgramacionServiceTest.java
src/test/java/com/feelingpilates/programacion/servicio/ImpactoBloquesEnHorarioTest.java
src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/LegacyTurnJpaReaderConcurrencyTest.java
src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/LegacyTurnJpaReaderPostgreSqlTest.java
src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/LegacyTurnJpaReaderRuntimeIsolationTest.java
src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/LegacyTurnJpaReaderTransactionTest.java
src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/LegacyTurnProjectionMapperTest.java
src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/LegacyTurnProjectionQueryExecutorTest.java
src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/LegacyTurnR2ArchitectureTest.java
src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/NominalJpaReaderArchitectureTest.java
src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/NominalJpaReaderPostgreSqlTest.java
src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/NominalJpaReaderRuntimeIsolationTest.java
src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/NominalJpaReaderTransactionTest.java
src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/NominalProjectionMapperTest.java
src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/NominalProjectionQueryExecutorTest.java
src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/ReservaJpaReaderArchitectureTest.java
src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/ReservaJpaReaderPostgreSqlTest.java
src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/ReservaJpaReaderRuntimeIsolationTest.java
src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/ReservaJpaReaderTransactionTest.java
src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/ReservaProjectionMapperTest.java
src/test/java/com/feelingpilates/transicion/programacion/detector/CandidateEvidenceGeneratorTest.java
src/test/java/com/feelingpilates/transicion/programacion/detector/DetectorArchitectureIsolationTest.java
src/test/java/com/feelingpilates/transicion/programacion/detector/DetectorClassifierTest.java
src/test/java/com/feelingpilates/transicion/programacion/detector/DetectorImmutabilityTest.java
src/test/java/com/feelingpilates/transicion/programacion/detector/DetectorResultInvariantTest.java
src/test/java/com/feelingpilates/transicion/programacion/detector/F2DAuthorityGuardTest.java
src/test/java/com/feelingpilates/transicion/programacion/r4/adapter/jpa/AdjustmentJpaReaderArchitectureTest.java
src/test/java/com/feelingpilates/transicion/programacion/r4/adapter/jpa/AdjustmentJpaReaderPostgreSqlTest.java
src/test/java/com/feelingpilates/transicion/programacion/r4/adapter/jpa/AdjustmentJpaReaderRuntimeIsolationTest.java
src/test/java/com/feelingpilates/transicion/programacion/r4/adapter/jpa/AdjustmentJpaReaderTransactionTest.java
src/test/java/com/feelingpilates/transicion/programacion/r4/adapter/jpa/AdjustmentProjectionMapperTest.java
src/test/java/com/feelingpilates/transicion/programacion/r4/adapter/jpa/AdjustmentProjectionQueryExecutorTest.java
src/test/java/com/feelingpilates/ubicaciones/HorarioOperacionConcurrenciaTest.java
src/test/java/com/feelingpilates/ubicaciones/HorarioOperacionMigracionV42V43Test.java
src/test/java/com/feelingpilates/ubicaciones/HorarioOperacionMigracionV43V46Test.java
src/test/java/com/feelingpilates/ubicaciones/HorarioOperacionVersionadoPersistenciaTest.java
src/test/java/com/feelingpilates/ubicaciones/HorarioOperacionWritersPersistenciaTest.java
src/test/java/com/feelingpilates/ubicaciones/SalonHorarioOperacionHistorialPersistenciaTest.java
src/test/java/com/feelingpilates/ubicaciones/UbicacionesPersistenciaTest.java
src/test/java/com/feelingpilates/ubicaciones/controlador/SalonHorarioOperacionControllerTest.java
src/test/java/com/feelingpilates/ubicaciones/dominio/CoberturaVigenciaTest.java
src/test/java/com/feelingpilates/ubicaciones/dominio/DiaSemanaOperacionTest.java
src/test/java/com/feelingpilates/ubicaciones/dominio/RangoVigenciaTest.java
src/test/java/com/feelingpilates/ubicaciones/repositorio/HorarioOperacionRepositoryTest.java
src/test/java/com/feelingpilates/ubicaciones/servicio/CerrarHorarioOperacionTest.java
src/test/java/com/feelingpilates/ubicaciones/servicio/ConflictoVigenciaHorarioTranslatorTest.java
src/test/java/com/feelingpilates/ubicaciones/servicio/HorarioEfectivoSalonTest.java
src/test/java/com/feelingpilates/ubicaciones/servicio/HorarioOperacionErroresTest.java
src/test/java/com/feelingpilates/ubicaciones/servicio/HorarioOperacionResolverPersistenciaTest.java
src/test/java/com/feelingpilates/ubicaciones/servicio/HorarioOperacionResolverTest.java
src/test/java/com/feelingpilates/ubicaciones/servicio/SalonServiceTest.java
src/test/java/com/feelingpilates/ubicaciones/servicio/VersionarHorarioOperacionTest.java
```

### CURRENT_R5_PROVENANCE_ONLY

```text
auditoria/fase-2e-diseno-adapters-read-only-snapshot-consistency.md
auditoria/fase-2e-r4-prerrequisito-esquema-reconciliado.md
auditoria/handoffs/HANDOFF-F2E-R3-IMPLEMENTACION-READER-PROGRAMACION-NOMINAL-CLEAN-MAIN.md
auditoria/handoffs/HANDOFF-F2E-R4-IMPLEMENTACION-READER-AJUSTES-FECHA-CLEAN-MAIN.md
auditoria/reviews/F2E-R5-DESIGN-INDEPENDENT-RECEIPT.md
auditoria/reviews/F2E-R5-DESIGN-PROCESS-CLOSURE.md
pom.xml
src/main/java/com/feelingpilates/transicion/programacion/adapter/jpa/mapper/NominalProjectionMapper.java
src/main/java/com/feelingpilates/transicion/programacion/r4/adapter/jpa/mapper/AdjustmentProjectionMapper.java
src/main/java/com/feelingpilates/transicion/programacion/r4/read/AdjustmentReadSnapshotContext.java
src/main/java/com/feelingpilates/transicion/programacion/read/NominalReadSnapshotContext.java
src/main/java/com/feelingpilates/ubicaciones/servicio/HorarioEfectivoSalon.java
src/main/java/com/feelingpilates/ubicaciones/servicio/HorarioOperacionResolver.java
src/main/java/com/feelingpilates/usuarios/entidad/Rol.java
src/main/java/com/feelingpilates/usuarios/entidad/Usuario.java
```

| Set | Count | SHA256 |
| --- | ---: | --- |
| CURRENT_R5_AUTHORIZED_NEW | 20 | `330488d90768982ed3c18c7d49a2531767eda2409b2de178f7c5a2d68586d01b` |
| CURRENT_R5_AUTHORIZED_MODIFIED | 0 | `e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855` |
| CURRENT_R5_READ_ONLY | 69 | `3f420b74e85f36087598219874b04f460c1da49baf6864cb3b0100b474cb1b4d` |
| CURRENT_R5_PROVENANCE_ONLY | 15 | `224a681283d4ad2b009572414e20bc9a8d5f67a6d455b2077db83c3370431577` |
| WRITE_SCOPE | 20 | `330488d90768982ed3c18c7d49a2531767eda2409b2de178f7c5a2d68586d01b` |
| TOTAL_ACTIVE_PATHS | 89 | `11bb29d121ddab3cdb07fb7c22da56eb37fd1c726454ac1e8d210dfe5730341a` |

### Existing content seals (Git blobs at exact base)

| Path | SHA256 |
| --- | --- |
| auditoria/fase-2e-diseno-adapters-read-only-snapshot-consistency.md | `6c72cba1f83fbc2bcf3b3219d8252d2410ec482e30ae85d30fd2eeb04e8883d8` |
| auditoria/fase-2e-identidad-semantica-detector-read-only.md | `6f850e9723f9861456d646039e4b233cff20d013ff956cab98f7370dffac4670` |
| auditoria/fase-2e-r3-diseno-reader-programacion-nominal-reconciliado.md | `42a21c09d137d363e938d441a499d48c5ec7537a73ee23861bfce2c4d75677f9` |
| auditoria/fase-2e-r4-diseno-reader-ajustes-fecha-reconciliado.md | `d966e32318c10b5fa6226f17f63001d71fbbe24419d7dee70842a97f9c7204d4` |
| auditoria/fase-2e-r4-prerrequisito-esquema-reconciliado.md | `f095844d82e6e1b091d02ef914035cc0ac0b984a8069f197f6ffb1f656b4623c` |
| auditoria/fase-2e-r5-diseno-composicion-efectiva-reconciliado.md | `428d9ca95c59485dde7a274f31ca24bc0dafd94d94bd8476e84993fcb95024bf` |
| auditoria/handoffs/HANDOFF-F2E-R3-IMPLEMENTACION-READER-PROGRAMACION-NOMINAL-CLEAN-MAIN.md | `a74d139ca6c924dcbb98c368d80f38a17cf423408c7db30054e621b67b4cefa9` |
| auditoria/handoffs/HANDOFF-F2E-R4-IMPLEMENTACION-READER-AJUSTES-FECHA-CLEAN-MAIN.md | `210bfc17e4e8bbf0917190262aa165c3804ab4f477bf6a30ea56021dc8961912` |
| auditoria/reviews/F2E-R5-DESIGN-INDEPENDENT-RECEIPT.md | `cb489b5fd8151e57e2113036de981e09f4a782bd0e4bc4884c4b26a282a89711` |
| auditoria/reviews/F2E-R5-DESIGN-PROCESS-CLOSURE.md | `c8bed626699ff958f385c98314ee41097d40ab4ec9951a8d218bfee5e4d731aa` |
| pom.xml | `904b68767ace499c7d842a5052d22f1ad93640b06cf3233ee0adf171dc949347` |
| src/main/java/com/feelingpilates/programacion/dominio/ReferenciaOcurrencia.java | `611d34112c796097c28835b488769747146be65f8f1a33f1485fdec7e4c5b82b` |
| src/main/java/com/feelingpilates/transicion/programacion/adapter/jpa/mapper/NominalProjectionMapper.java | `adae3e59ae6d834b50c7304bcdc95461e0341ffb18eb37e9fe9231371e4834cc` |
| src/main/java/com/feelingpilates/transicion/programacion/detector/DetectorVocabulary.java | `4123e78b89872daeb0d9fb03e8d49e5e732041f828e14b13f36fb187546d11f6` |
| src/main/java/com/feelingpilates/transicion/programacion/detector/EvidenceProvenance.java | `ba4312f10c96b1268e45e75895f3c818b9ab03afa7df22a09989785a7738bf83` |
| src/main/java/com/feelingpilates/transicion/programacion/detector/F2DAuthorityGuard.java | `fd178edda27b75dac42f6120c8a7d661e44d283becf25ed4a431e87e17e820be` |
| src/main/java/com/feelingpilates/transicion/programacion/detector/GenericSourceSnapshot.java | `36de8fe63df709e6213e0e2b9d2fef9b05d9f66afee768596ffc8377bc75348a` |
| src/main/java/com/feelingpilates/transicion/programacion/detector/ProgrammingCandidateSnapshot.java | `af5adc2977a6936301452f9d461b8c54123e0a4fe1ba5943ecfecce57e6275f7` |
| src/main/java/com/feelingpilates/transicion/programacion/r4/adapter/jpa/mapper/AdjustmentProjectionMapper.java | `a3e2a9405fc6ee60479f0af8edad442bf36bf99852fb8961ee3583f8081bbff0` |
| src/main/java/com/feelingpilates/transicion/programacion/r4/read/AdjustmentBackingSnapshot.java | `062fa1c73fd664c1abd216b58abf61989f202d348443a246475dd8d35142ade3` |
| src/main/java/com/feelingpilates/transicion/programacion/r4/read/AdjustmentReadSet.java | `685f6eea5c70f644ead083894c2772e7af08571a33d0bd7ec7efec5152423208` |
| src/main/java/com/feelingpilates/transicion/programacion/r4/read/AdjustmentReadSnapshotContext.java | `932f27f6925d5ffa92bd23c2f7833c3b9cccb5f8cac4ee5ca9f6907e05593506` |
| src/main/java/com/feelingpilates/transicion/programacion/read/NominalBackingSnapshot.java | `485cac252fffac7adb456d9f2801bd53601f920934372a7a84d0b1e0834926d0` |
| src/main/java/com/feelingpilates/transicion/programacion/read/NominalProgrammingReadSet.java | `2a6981a11d9c2742728667655e67b9e058ff98fb7b3d710cabea985f5c7b785f` |
| src/main/java/com/feelingpilates/transicion/programacion/read/NominalReadSnapshotContext.java | `b3e05e1a08b73403ce83459f936fd66041b7dad2a774cc9f182778cdaebf731c` |
| src/main/java/com/feelingpilates/transicion/programacion/read/ReadSnapshotIdentifiers.java | `1c23fb83cdec352b885e5ce6272f49edafb0388f832f9be134544647f6c72aed` |
| src/main/java/com/feelingpilates/ubicaciones/servicio/HorarioEfectivoSalon.java | `065e544c912e283c7800d109d4ec2b2f9f6016af40b272cf2bfa242db4c4c970` |
| src/main/java/com/feelingpilates/ubicaciones/servicio/HorarioOperacionResolver.java | `d90e18e46797830de52caf32dde6e36b343765a40a6cd909a8c6bd74b766a339` |
| src/main/java/com/feelingpilates/usuarios/entidad/Rol.java | `3c572b3890742009c79aa62e2fd74a31ee6963cb96bbc2dda5bddc91018fd7e6` |
| src/main/java/com/feelingpilates/usuarios/entidad/Usuario.java | `616d67f14e5799c826ff9acb72566b2ac095f5471274dcce7eff88161a3e3a36` |
| src/test/java/com/feelingpilates/programacion/ProgramacionPersistenciaTest.java | `0c846a24d982f3a2673525c9c59b0acaa07e813ceebe56524e7987a80f8d8cf3` |
| src/test/java/com/feelingpilates/programacion/repositorio/BloqueProgramacionRepositoryVigenciaTest.java | `e90211144efd46b3946816525c875dd4866ae2106117e076dd87f64e784e6c57` |
| src/test/java/com/feelingpilates/programacion/servicio/BloqueProgramacionServiceTest.java | `13946d1f2de3e4b18900f6ac9009071cac109bd879e2c2e496179378a0ce1446` |
| src/test/java/com/feelingpilates/programacion/servicio/ImpactoBloquesEnHorarioTest.java | `6986d12a538e57a58f866517775a7d6e2830da5c8a7127defd75309f3f7a83ac` |
| src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/LegacyTurnJpaReaderConcurrencyTest.java | `30d6a9f2d613c7befc1b669757015b3b1dfac26843663a76db97e8a5d3386046` |
| src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/LegacyTurnJpaReaderPostgreSqlTest.java | `add002b8a51325a19943d4256062cf0e640e9c3b28c0d8b40e4e314f07992120` |
| src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/LegacyTurnJpaReaderRuntimeIsolationTest.java | `6b7df969f0815cdf6fb4523ecf87a3dc6e1835eade9c7fd4236eff3a15d4b93d` |
| src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/LegacyTurnJpaReaderTransactionTest.java | `a7b11e1cbd1fbda8f4bb481c97fec6b665a41607fc6def18f9bb9c319672b6c8` |
| src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/LegacyTurnProjectionMapperTest.java | `af991dbd6222023ebb5dd32e5a00337a8c198a3ddce0ab38d1d21197938d9e48` |
| src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/LegacyTurnProjectionQueryExecutorTest.java | `79efff09a286f8aea41a12d6fdbb036eb27432b1c7dd642ee388f3fd2a03a069` |
| src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/LegacyTurnR2ArchitectureTest.java | `e9bcbbb290e24e7634fa4a2ef9f4800b3645ad7c8f7e25002fc90e887a2db4c3` |
| src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/NominalJpaReaderArchitectureTest.java | `ef0da62d7419e6c76e220d9ad2e20f74e374c5b65d4ac6ea4b080bf63eda54eb` |
| src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/NominalJpaReaderPostgreSqlTest.java | `1579417b1170d5e358db2ea3447c0ef91debab850c66148445ff573f6bb3b912` |
| src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/NominalJpaReaderRuntimeIsolationTest.java | `35cfdcfbf09a07e96c854b53cd8baf5173d370040dc5e48dc4d513838a3a4126` |
| src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/NominalJpaReaderTransactionTest.java | `c2b49e4d64221aed57a0241a72cac1f44a28d7de10acc0c9feecc9694ac142e1` |
| src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/NominalProjectionMapperTest.java | `3f4d79f6396779f405d87ec5ba86b8761241f3cf389916c7758cfe9b5d8cce43` |
| src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/NominalProjectionQueryExecutorTest.java | `d78e7475088a75cfab6e61db2c97df9a6d294766f8302541941a5ab93c6ad7cb` |
| src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/ReservaJpaReaderArchitectureTest.java | `78fab2470ce3be4b1508f43b4185d3bd83f14d5c2e2ebc325792482eaa2fdaa3` |
| src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/ReservaJpaReaderPostgreSqlTest.java | `c908ee55c640a932825b8dad22f556ee76f716c262914ec7d829f1eb5057c9b6` |
| src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/ReservaJpaReaderRuntimeIsolationTest.java | `a7f4db9c51ee7306e9a284f4c6a69eb9484cf2dd3cb30fb40402204770a7e9cd` |
| src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/ReservaJpaReaderTransactionTest.java | `72407f0db1c01490463208ba08791fbebf48925518b4aaf95438d051c48d12e8` |
| src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/ReservaProjectionMapperTest.java | `1a63df3f56cf3e4c62122d972d43c7564c1e71f6e79a7928d7fd88e32ae9cdfa` |
| src/test/java/com/feelingpilates/transicion/programacion/detector/CandidateEvidenceGeneratorTest.java | `16f0c8cfb45a1ec7f2522d0726381cf1021826d901a0dcd32937c2155ffd52e3` |
| src/test/java/com/feelingpilates/transicion/programacion/detector/DetectorArchitectureIsolationTest.java | `b38f7e23e82174bd33808aaf424158dd9e769b16574d5735fc372a5b9195e10d` |
| src/test/java/com/feelingpilates/transicion/programacion/detector/DetectorClassifierTest.java | `c25c0be74a4ea3c17218b152035fde4d020c216349ba0fc96b1f5f12f0247cca` |
| src/test/java/com/feelingpilates/transicion/programacion/detector/DetectorImmutabilityTest.java | `feacdaabc35291a04f3fd9747b5ae4304a79f1f5bc6e098ceca89b13dbd407f4` |
| src/test/java/com/feelingpilates/transicion/programacion/detector/DetectorResultInvariantTest.java | `cb5f0a44ad8463dd02f5838c5767c4e2b13ceba582cfbbee419b12bb2bc81fd9` |
| src/test/java/com/feelingpilates/transicion/programacion/detector/F2DAuthorityGuardTest.java | `cc74f82486574b92f52b9ad9375ce6b43d7bfab8f30fe2d156202d4aed4431ed` |
| src/test/java/com/feelingpilates/transicion/programacion/r4/adapter/jpa/AdjustmentJpaReaderArchitectureTest.java | `58e6d4cdb872b9792967276cb25bda26ab26039591a4ae7ede3f9d6f7137c9fa` |
| src/test/java/com/feelingpilates/transicion/programacion/r4/adapter/jpa/AdjustmentJpaReaderPostgreSqlTest.java | `e01c7607560610df49e85182a1d826b321912364c2a56f8d52c9afec742b538a` |
| src/test/java/com/feelingpilates/transicion/programacion/r4/adapter/jpa/AdjustmentJpaReaderRuntimeIsolationTest.java | `5f563f8882ebd532df9b8bb88f0557466f76966e210bdf18c7ad8791ea38cded` |
| src/test/java/com/feelingpilates/transicion/programacion/r4/adapter/jpa/AdjustmentJpaReaderTransactionTest.java | `bc967c9f1af2fb84ccfac549eb799795493bd1e5cc459024176bac383fe980be` |
| src/test/java/com/feelingpilates/transicion/programacion/r4/adapter/jpa/AdjustmentProjectionMapperTest.java | `92f47dadd236330c8fc517eca2c10e52a402da10d9613e5bee9fb2f37c37cea8` |
| src/test/java/com/feelingpilates/transicion/programacion/r4/adapter/jpa/AdjustmentProjectionQueryExecutorTest.java | `37c8bce3e0f698f23956ed1dbbdafee0a154eaa1b3aa74badf1b5a601b533ca1` |
| src/test/java/com/feelingpilates/ubicaciones/HorarioOperacionConcurrenciaTest.java | `1a94bcc0cbbe36d5a68669f0133ffc355612a1a0cc0d6445804f03cbdc043679` |
| src/test/java/com/feelingpilates/ubicaciones/HorarioOperacionMigracionV42V43Test.java | `bd1a753322cd7c7d0c7a6f46d1e0f0111af77eeea2a5a36db50de88f3d366537` |
| src/test/java/com/feelingpilates/ubicaciones/HorarioOperacionMigracionV43V46Test.java | `86c5896be9def5348188b0566467a4cd7a85f3a539b30c42ec7af092babe258e` |
| src/test/java/com/feelingpilates/ubicaciones/HorarioOperacionVersionadoPersistenciaTest.java | `ebff877409634ff6174ee929e5b744e3804905767276d53512fae778beaf0d5f` |
| src/test/java/com/feelingpilates/ubicaciones/HorarioOperacionWritersPersistenciaTest.java | `e2b7cfe533f5c7814b977457e43411fc6aea46afc0bdedb0d78523727597fa21` |
| src/test/java/com/feelingpilates/ubicaciones/SalonHorarioOperacionHistorialPersistenciaTest.java | `04717d21611e996c65061a509d9e0e3fab10f55c1f3c6972b4e93f09f3b0c220` |
| src/test/java/com/feelingpilates/ubicaciones/UbicacionesPersistenciaTest.java | `7cb658302e3bd901858299b8dd00fe5507a27b4488553fb64923f7bd126d9b01` |
| src/test/java/com/feelingpilates/ubicaciones/controlador/SalonHorarioOperacionControllerTest.java | `5e9cc1c39cfe7db87db930f82313c5471ef4daa45b224cbeda81d60adfd6893a` |
| src/test/java/com/feelingpilates/ubicaciones/dominio/CoberturaVigenciaTest.java | `804cbdb018a76954533a92df20a7de91078b2c53f4134b6f698bc7724ecdde1c` |
| src/test/java/com/feelingpilates/ubicaciones/dominio/DiaSemanaOperacionTest.java | `6e05f36e3a0acc388bbb86bcd84c8f12894b738392d746ca0298382432042f32` |
| src/test/java/com/feelingpilates/ubicaciones/dominio/RangoVigenciaTest.java | `e99495cc63f6fe1138844f78f3ed630885235369a5a32aa8ce5e1faeba69773b` |
| src/test/java/com/feelingpilates/ubicaciones/repositorio/HorarioOperacionRepositoryTest.java | `fb62d6d8b3eb9d1a5e0020160cbe870bf5a8c3814b971713e47435c7e9a76193` |
| src/test/java/com/feelingpilates/ubicaciones/servicio/CerrarHorarioOperacionTest.java | `aa8163bcad98c84db99ea576c9e77f7453b2dafb19691858eb19e475bb017898` |
| src/test/java/com/feelingpilates/ubicaciones/servicio/ConflictoVigenciaHorarioTranslatorTest.java | `d22788695c88e7120b2ac7fbc70538bfe08fc61cad4f20cb735c58f3c53bcd4e` |
| src/test/java/com/feelingpilates/ubicaciones/servicio/HorarioEfectivoSalonTest.java | `23f8e641f87438184c7c8983013662c64e6e7ebc0d42013fd25e0c11a9f2654d` |
| src/test/java/com/feelingpilates/ubicaciones/servicio/HorarioOperacionErroresTest.java | `e2a3f7cb0cf8c9fc42fcf75524bc54fc9d28c2a20f15ba4a01eee5d45f35b1b4` |
| src/test/java/com/feelingpilates/ubicaciones/servicio/HorarioOperacionResolverPersistenciaTest.java | `1f93faa3b40e479aff6ce17bb3889eac32dd753a3c0f71efd24613e3c11e52e2` |
| src/test/java/com/feelingpilates/ubicaciones/servicio/HorarioOperacionResolverTest.java | `a11b885d0d16b02d2360742a4ba247a10fb8da891c020b3b7a8fef9d36a8f7e2` |
| src/test/java/com/feelingpilates/ubicaciones/servicio/SalonServiceTest.java | `d14de2a22785c016390c4a79a49faa06ca3b3ffb1f9859fd929dadea28ab64d1` |
| src/test/java/com/feelingpilates/ubicaciones/servicio/VersionarHorarioOperacionTest.java | `51182f479980f766670a59c07a2942e8aca0d603f727ac19ab955bda16214c1c` |

## 11. Handoff process and separate non-circular activation

This lifecycle writes only the three exact documentary process paths named below; all base paths stay immutable, including STATE/ESTADO. External BASELINE.json seals every tracked path (physical and Git), all50 migrations. These process writes are separate from future implementation NEW scope; no Java/test code created here.

- auditoria/handoffs/HANDOFF-F2E-R5-IMPLEMENTACION-COMPOSICION-EFECTIVA-CLEAN-MAIN.md
- auditoria/reviews/F2E-R5-HANDOFF-INDEPENDENT-RECEIPT.md
- auditoria/reviews/F2E-R5-HANDOFF-PROCESS-CLOSURE.md

Freeze exact commit and manifests; dispatch exactly one fresh independent GPT-6.1-Sol High auditor per round with no authorship/correction/prior-round participation; native Orca launch/effective evidence and external exact report. Fresh auditor rechecks canonical main, candidate scope, all content/path hashes, closed source semantic materialization and acceptance. Approval requires P0/P1=0, all P2 resolved. Bounded autonomous unique design-fixed P1 correction maximum1 and mechanical P2 maximum2, cumulative per lifecycle; blocked commits/reports remain in history; each corrected candidate needs one new auditor. Scope/authority contradictions, P0, main movement or exhausted rounds STOP/HUMAN_GATE. No silent rebase or self audit. Factual receipt direct child adds only reserved review/process documents, preserves approved handoff bytes. Publish/normal merge only on still-stationary base; verify actual parents/tree/ancestry/PR content/read-back, clean state and full preservation.

After actual merge, one new independent post-merge closure auditor must approve publication/integration/process facts before terminal receipt can claim HANDOFF_RECONCILED / INDEPENDENTLY_AUDITED / APPROVED / PUBLISHED / INTEGRATED / CLOSED. Publish its external report and factual terminal receipt in merged PR comments, read back and seal actual identities externally; do not rewrite immutable pending headers. Settlement/release every auditor and zero reclaimable terminals. CLOSED handoff stays NOT_ACTIVE; implementation NOT_AUTHORIZED / NOT_IMPLEMENTED.

Then separate activation on that freshly verified main: freeze exactly three NEW process-only paths `auditoria/reviews/F2E-R5-HANDOFF-ACTIVATION-CANDIDATE.md`, `auditoria/reviews/F2E-R5-HANDOFF-ACTIVATION-INDEPENDENT-RECEIPT.md`, `auditoria/reviews/F2E-R5-HANDOFF-ACTIVATION-FINALIZATION.md`; no STATE/ESTADO/closed authority edits. Its candidate binds published CLOSED handoff/design/independent receipt/terminal publication plus fixed path/content hashes and future finalization plan, explicitly NOT_ACTIVE. One fresh independent activation auditor approves exact candidate and deterministic plan; approval is not activation. Receipt direct child and normal merge must preserve candidate bytes/ancestry. Only thereafter materialize the already-reviewed deterministic finalization file in a separate commit/normal merge on that verified approval main, naming actual previously published approval/merge hashes, no future/self hash. Finalization changes process facts only: semantic/path/precondition change requires a fresh audit, not inherited approval. Closure audit checks actual finalization/publication/integration before truthful terminal activation closure.

Effect only after verified normal finalization merge and all fixed published approvals/design/handoff/ancestry/path hashes/NEW absence/full baseline/source/migration/clean-state gates: R5 handoff ACTIVE; R5 implementation AUTHORIZED_TO_START in exact NEW scope, NOT_IMPLEMENTED. R6 NOT_AUTHORIZED / NOT_STARTED, no productive route/read/transaction/database/cutover. Activation terminal external receipt publishes actual identities/hash chains without self-reference. Future pure composer implementation is a different lifecycle and is not started here.

Raw evidence owner RUN_COORDINATOR, LOCAL_RETAINED under `~/.codex/feelingpilates-evidence/r5-handoff-20261004/`; external backup NOT_GUARANTEED. Git-public documents and published PR receipts provide public authority, without claiming raw transcripts fully backed up. Technical gates remain REQUIRED/NOT_EXECUTED until implementation; documentary validation does not supply implementation acceptance.
