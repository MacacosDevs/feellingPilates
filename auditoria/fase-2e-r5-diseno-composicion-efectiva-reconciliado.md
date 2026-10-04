# F2E R5 — reconciled pure effective-programming design

Version: `F2E-R5-PURE-COMPOSITION-V1`. Status: `MATERIALIZED_CANDIDATE / PENDING_FRESH_INDEPENDENT_DESIGN_AUDIT`.
Scope: DESIGN_ONLY. R5 implementation/handoff/activation and R6 remain NOT_AUTHORIZED.
Base: `03f30e52361b1f6a9d0dcede0c15340aa144026b` (canonical main, PR32).

## 1. Authority and clean-main reconciliation

Current explicit human scope authorizes this design lifecycle, independent audit, bounded corrections,
publication, merge and factual closure. It authorizes pure composition, not database access or execution.
Within that scope, this successor reconciles only the R5 portions of the historical adapters design
§§11–12.4,24,33.5. The sealed historical document remains provenance; its absent F2D runtime graph,
entity-mapping/JPA reader, R3/R4 invocations, repositories, graph factory and transaction-test-owner
premises are superseded for R5 by the pure supplied-input contract below. No historical services are
restored; no productive service is called. F2D semantic rules remain the source of composition and
final validity, with immutable diagnostics. There is no independent second graph to compare with:
R5 constructs and verifies the nominal → adjusted → validated effective graph itself.

The independently approved authority preflight selected `R5_DESIGN_RECONCILIATION_REQUIRED`,
not implementation. Its candidate SHA256 is `4ef24801472b7e3e77e8ac18efb4986c10b4f0f6a2e408a61afc996eb156edf4`;
independent receipt SHA256 `c7ed627323e382cc13515c315e36769f187840679ae860df3e23063e99c35fe2`.
Both are retained under `~/.codex/feelingpilates-evidence/r5-authority-preflight-20261004/`.
This design is self-contained for semantic composition; physical acquisition is deliberately a
separate future R6 authority contract, not an unfilled R5 reader design.

Read-only authorities: [identity/core](fase-2e-identidad-semantica-detector-read-only.md) §§6–8;
[historical adapters](fase-2e-diseno-adapters-read-only-snapshot-consistency.md) §§9–13,18.1,24,33.5–33.6;
[closed R3](fase-2e-r3-diseno-reader-programacion-nominal-reconciliado.md);
[closed R4](fase-2e-r4-diseno-reader-ajustes-fecha-reconciliado.md);
[closed schema](fase-2e-r4-prerrequisito-esquema-reconciliado.md);
[closed R4 handoff](handoffs/HANDOFF-F2E-R4-IMPLEMENTACION-READER-AJUSTES-FECHA-CLEAN-MAIN.md).
Historical Git `95900d8` supplies AplicadorAjustesProgramacion, ProgramacionValidador and diagnostics
as semantic provenance only. Current HorarioEfectivoSalon establishes exact-date/weekly precedence.
R4 implementation is CLOSED through PR32 and retained independent implementation/closure receipts;
stale ESTADO/STATE activation captures do not supersede these later closure facts.

R1–R4 stay CLOSED. TurnoInstructor stays LEGACY_VIVO / PRODUCTIVO; dark launch, current client
API/Web/Reservations and productive Programación remain unchanged. No migrations, database reads,
productive wiring, routing, writer, repair, dependency, deployment, cutover, Payments or Notifications.
No code or tests are created in this lifecycle. All 455 baseline paths and 50 migrations are sealed.

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

## 8. Future acceptance and explicit exclusions

A later separately approved/published/active implementation handoff must freeze additive isolated paths
for the plain composer/immutable DTOs and pure tests. No existing R1–R4/core/productive source modification
is implied. No R5 JPA reader, EntityManager, transaction annotations, Spring bean or historical restoration.
Architecture acceptance requires zero persistence/framework/I/O edges and zero productive callers/beans.
Pure tests use synthetic typed fixtures with explicit mode; they do not counterfeit reader execution receipts.

Required semantic acceptance matrix: recurrent identity/backing; cancellation unique target/suppression;
replacement of all five fields including moved final salon and unchanged identity; addition identity/no
nominal; orphan target, duplicates/contradictions/forms/wrong dates/hashes/backing; ambiguous hours and
missing evidence; every ordered omission predicate; final-salon precedence and boundaries; same instructor
cross-salon overlap, adjacency, duplicate operative key and no invented salon-only rule; full accounting,
empty mixed outcomes, unbacked/mismatched graph rejection; exact canonical golden vectors/Unicode/null
framing, unsigned UUID/reference order, permutation invariance, sensitivity and immutable results;
synthetic-vs-real/mixed-mode rejection and no false shared-snapshot claims. These are REQUIRED/NOT_EXECUTED
future implementation gates, not tests passed by this documentary lifecycle.

Physical PostgreSQL/SELECT-only/no-write/checksum/concurrency/completion/HostValidator acceptance belongs
to a future authorized physical integration/R6 lifecycle. It cannot be marked PASS or silently waived
because pure R5 has no reads. Historical R5 DB expectations are relocated with the removed read ownership;
R6 must prove shared physical RR/read-only coherence, participant admission and actual descriptors before
real release. No R6 implementation/activation is authorized, and no speculative concrete R6 design is fixed.

## 9. This design lifecycle and process closure

Exact repository write scope: this new design; new
`reviews/F2E-R5-DESIGN-INDEPENDENT-RECEIPT.md`; new
`reviews/F2E-R5-DESIGN-PROCESS-CLOSURE.md`. All baseline paths, ESTADO/STATE and source/test/migration
bytes stay unchanged. External run evidence is retained by RUN_COORDINATOR under
`~/.codex/feelingpilates-evidence/r5-design-reconciliation-20261004/`; backup not guaranteed.
The user explicitly authorizes coordinator bounded corrections, publication and normal merge.

Freeze each candidate commit/manifest; use exactly one fresh independent GPT-6.1-Sol High auditor per
round, no auditor subagents/self-approval. Retain blocked commits/reports; corrections never reset budgets.
P0/authority contradiction: zero autonomous corrections/HUMAN_GATE. P1 bounded uniquely supported design
correction: at most one materialized corrective candidate. P2 mechanical: at most two. Most restrictive
budget applies; change requiring domain/API/schema/reader/TX/productive authority is HUMAN_GATE.
Main movement, authority contradiction or exhausted rounds stops publication fail closed. Required fresh
re-audit follows any semantic correction; approval alone is not closure or implementation permission.

After APPROVED P0/P1=0, record a factual receipt direct child preserving design bytes; verify stationary
live main, exact scope and all baseline seals; publish and normally merge that exact head. Verify merged
ancestry, exact audited bytes, unchanged baseline and clean local state. Then use one fresh independent
closure auditor for that distinct round over live merge and retained evidence, publish its factual result
and terminal closure in the merged PR record without changing audited design bytes or inventing future hashes.
Terminal design status CLOSED requires these real publication/integration/closure facts, not a candidate
header rewrite. If closure audit blocks, retain evidence and correct only bounded documentary process facts
under fresh audit; no fabricated successful closure. Tests/DB/host for this document-only change are
NOT_APPLICABLE / NOT_EXECUTED; diff, scope, hashes, authority, ancestry and freshness checks are required.

The sole eligible next lifecycle after truthful design closure is separate R5 IMPLEMENTATION HANDOFF
reconciliation, not its automatic start/activation. R5 implementation and R6 remain NOT_AUTHORIZED;
R1–R4 CLOSED and all product constraints above remain preserved.
