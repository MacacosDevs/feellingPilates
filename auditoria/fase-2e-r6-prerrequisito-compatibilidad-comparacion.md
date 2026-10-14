# F2E R6 — separate comparison-compatibility design prerequisite

Authority class: COMPARISON_COMPATIBILITY_DESIGN / DOCUMENTATION_ONLY.
Capture: MATERIALIZED_CANDIDATE / NOT_INDEPENDENTLY_ACCEPTED / NOT_PUBLISHED.
Canonical base `552fe66d673bb4cfa14651cccbdf26ac32bb5611`, tree `545685d195f866826a9be23599ff4c6049acab18`.
Executable compatibility and R6 implementation: NOT_AUTHORIZED / NOT_STARTED.

## 1. Competence, predecessor and separation

The user's explicit three-NEW-document authorization is the sole write authority for
this prerequisite. It authorizes selecting, materializing, auditing, publishing and
closing a comparison design, not source/test/guard changes or implementation.
This design fixes the integration contract only; CLOSED R1–R5 product meanings,
accepted APIs, old-mode behavior and physical admission authority remain untouched.

Rejected R6 design `21313eec5d696afbb384bb5e7de656c85e18739e` remains on its original
branch and is not an ancestor of this independent main-based prerequisite branch.
Its independently rejected bytes are NOT_APPROVED, P0/P1/P2=0/2/0; design SHA256
`e8615655b84ab2977175a5c8cbd5937e613c6dade1e2b1f3ad1d2c2cd7365c41`.
Original independent D1 report SHA256
`46e0cab4234fe97bf07baf0f31d0ea576fec874d9993d31693dda3d2afc1e8db`, task
`task_fc41df2d1624` / dispatch `ctx_ed02209057e1`, is retained at
`~/.codex/feelingpilates-evidence/r6-design-reconciliation-20261013/D1-INDEPENDENT-AUDIT.md`.
Neither rejection nor candidate is rewritten, relabeled approved or absorbed here.
The human approved the exact subject of HUMAN-GATE-DECISION.md and subsequently
provided the missing exact three documentary paths. That is design authority only.

Competent unchanged product constraints:
[identity §§7.2–7.8,9.1–9.3](fase-2e-identidad-semantica-detector-read-only.md),
[adapters §12.3 and §16](fase-2e-diseno-adapters-read-only-snapshot-consistency.md),
[CLOSED R2 §3.3](fase-2e-r2-diseno-lector-turno-legacy-integracion.md),
[CLOSED R5 §§4–8](fase-2e-r5-diseno-composicion-efectiva-reconciliado.md) and
[R5 handoff](handoffs/HANDOFF-F2E-R5-IMPLEMENTACION-COMPOSICION-EFECTIVA-CLEAN-MAIN.md).
Preflight and implementation/closure evidence are chronology, not new physical proof.

The concrete compatibility defect has TWO unchanged guards: DetectorClassifier
classifyAddition(0) returns MISSING, and DetectorResult.validateSourceSemantics rejects
NEW_ADICION/zero-effective/DIVERGENT_INCOMPATIBLE. Changing ClassificationContext,
lying about count/source/scenario, or constructing an old DetectorResult directly
cannot solve it. CLOSED R5 and identity require explained operational addition omission
to be divergent incompatibility. Legacy observations also require typed candidates,
actual evaluations/results/counts/ambiguity/blocking; an observation-only ledger fails.

Selected solution: a new plain pure `comparison.v2` layer over supplied immutable
facts, with a versioned result variant only where the old count-only result cannot
faithfully encode the already required omission semantics. All representable cases
continue to retain exact outputs from the unchanged core classifier. No shared-reader
admission, transaction/resource/snapshot capture/validity acquisition or R6 coordinator
is implemented or authorized by this selection. R1/R2 shared admission A and R3/R4/R5
shared admission/descriptor/R6 caller compatibility B remain separate pending lifecycles.

## 2. Supplied input, trust and whole-unit failure

Future `ComparisonInputV2` supplies invocation/evaluation metadata, date, explicit
business zone, evaluatedAt, fixed `F2E-COMPARISON-COMPATIBILITY-V2` rule/catalog,
immutable Reserva snapshots, immutable R2 GenericSourceSnapshots, nominal read set,
adjustment read set and the actual issued EffectiveProgrammingCompositionResult.
It never obtains inputs, SQL or physical resources. Lists/maps are defensively copied;
no entity/proxy/resource handle or callbacks, clock, random, network, logging sink,
framework annotation, reflection or I/O edge is allowed. Explicit fixture mode and
owner-authenticated real mode remain distinct; no synthetic/mixed route upgrade.

Require date/zone and declared completion/envelope identities to bind the same supplied
R3/R4/R5 unit; exact original nominal/adjustment data, source formulas and atom identities
must agree with R5.input(). Enforce whole-set bijection, duplicate identity/reference
rejection, no truncation and complete closed R5 candidate/backing/omission/suppression
accounting. R5's result can only be issued by its validated pure pipeline. No call to
package-private verifyResult, reflective construction or copied substitute result.
If any supplied input lacks required evidence, fail the entire comparison batch with
ComparisonFailureV2, safe IDs/stage and original cause; no partial batch/semantic MISSING.

This pure layer validates supplied commitments/relationships, not physical authenticity.
R6 alone must later authenticate one RR/read-only resource, private admission/capture
and successful transaction completion before supplying real inputs. No metadata equality
here proves a snapshot. Tests of this prerequisite's future executable layer may use
explicit supplied fixtures; they do not certify physical admission or activate R6.

## 3. Legacy typed generation and exact one-evaluation accounting

One R2 source atom produces exactly one typed core evaluation/DetectorResult, wrapped
by ComparisonEvaluationV2; the raw comparison ledger is supplementary evidence.
Fixed group order: Reserva, LEGACY_RECURRENTE, LEGACY_EXCEPCION, LEGACY_CANCELACION,
NEW_CANCELACION, NEW_REEMPLAZO, NEW_ADICION. Within groups sort original sourceIdentity
as unsigned UTF-8; within target universe sort closed occurrence reference, then
snapshot identity. Reject duplicate references rather than choose/deduplicate.
No extra nominal/effective evaluation of the same puntual atom; axes are explicit.

| Source | Candidate universe / approved observational predicate | Exact core scenario |
| --- | --- | --- |
| Reserva | R5 effective universe via unchanged CandidateEvidenceGenerator; equality date/salon/instructor/activity and reserved subinterval containment | standard(); no historical target or required-target claim |
| Complete recurrent | R3 nominal universe, same evaluationDate and domingo-zero day, salon/instructor/activity and exact effectiveStart/effectiveEnd observations | standard() for the CURRENT_OBSERVATION claim; RECURRENT_CLAIM_DEPENDENT is policy, never a new core enum |
| Representable anomalous recurrent | Preserve all source/provenance markers; no invented complete candidate dimensions; zero candidates for structurally incomplete evidence | incompatibleEvidence(); DIVERGENT_INCOMPATIBLE/blocking |
| Legacy exception | R5 effective universe; exact date/salon/instructor/activity and effectiveStart/effectiveEnd observations when all safely present | legacyExceptionUnknownIntent(), always UNSUPPORTED/UNKNOWN_INTENT |
| Legacy cancellation | R3 nominal universe; exact evaluation date/salon/instructor only | legacyCancellationUnknownIntent(), always UNSUPPORTED/UNKNOWN_INTENT |

These are exact observational candidate rules, not identity of business intention,
selected mappings or new productive equivalence. Exception candidates include plausible
replacement/addition/recurrent outcomes without inferring which intent existed.
Cancellation activity/range is retained as explanation and NEVER narrows its plausible
nominal set. Two nominal references with same date/salon/instructor remain two candidates,
even with differing activity/range. No day-wide legacy cancellation becomes a new exact
target/suppression. Reserva→legacy stays explanatory only, not a target evaluation.

Recurrent physical date=NULL is not compared to target date: use closed evaluationDate
and applicable dayOfWeek. Exact interval comparison uses R2's effectiveStart/effectiveEnd;
raw null/null only has the fallback already supplied by R2. Preserve original raw,
rangeRule, atomKind, membershipStatus, markers, all 26 observables and provenance.
ABSENT/NULL sentinel values are never parsed as UUID/valid time or converted to values.
For punctual atoms UNKNOWN_INTENT dominates structural anomaly. Missing safely required
candidate dimensions gives zero candidates, retaining the source; it is not an access
failure. Cancellation requires only its three safely observable target dimensions:
a member gap with a known instructor may still provide them; missing activity/range
cannot silently narrow cancellation. An absent instructor gives no complete candidate.
Recurrent structural anomaly uses the existing R2 scenarioPolicy, not a guessed normal
claim. Required-target/history modes are outside this contract; no inferred historical
functional validity from timestamps, markers or field matches.

LegacyCandidateGeneratorV2 evaluates every target in its complete universe and retains
one typed ComparisonCandidateEvidenceV2 row per target, with explicit MATCH/MISMATCH/
UNOBSERVABLE per date/day/salon/instructor/activity/start/end, original provenance and
whether the approved predicate admits it. Only plausible complete candidates enter
CandidateGenerationResult; nonplausible observations remain in the supplementary ledger.
Thus generatedCandidateCount is the number of retained core candidates, not the size
of the inspected universe; the ledger records universe and excluded counts separately.
This is not silent dropping of evidence or a selected winner.

For admitted candidates, create unchanged DetectorCandidate with exact reference/type/
snapshot/fingerprint/provenance, FIELD_MATCH evidence for the applicable closed core
dimensions and a candidateEvidenceHash also binding the typed interval/day ledger.
Do not invent interval rejection enums or claim RESERVED_SUBINTERVAL containment for
legacy. No excluded interval candidate is forced into a REJECTED core object with a
false reason. F2DAuthorityGuard runs for every target; incompatible target type/reference
is a whole-input incompatibility failure, never omitted to fabricate normal success.
Core candidates have only supported, truthful evidence and obey all existing constructors.

Unchanged DetectorClassifier performs each legal scenario. Retain the resulting immutable
DetectorResult without patching fields. Multiplicity yields exact candidateCount,
MULTIPLE_CANDIDATES, reproducible AMBIGUOUS reason and blocked capabilities; no observer
policy string substitutes for those fields. Punctual UNKNOWN_INTENT persists at 0/1/N.
Empty R2 source set yields zero evaluations, with explicit source/accounting commitment,
not an invented gap. Every representable GAP/assignment atom remains accounted once.

## 4. Adjustment dispatch and omission-aware versioned result

For each original R4 source, derive reference from CLOSED identity: cancellation and
replacement use target series/date; addition uses AJUSTE/adjustment-id/date. Validate
source type, original backing/fields/provenance and R5 linkage before counts. Determine
nominal count from R3 exact target reference, effective count from R5 exact reference,
and the unique R5 omission or suppression entry by that reference AND originating
adjustment identity. Do not use only matching dimensions or an empty effective list.

R5's complete accounting partitions each outcome into survivor, explained omission or
valid cancellation suppression. Multiple outcomes, mismatched backing/source, orphan
adjustment, omission+survivor, omission+suppression, duplicate cause or unaccounted
expected outcome aborts whole input; do not relabel an inconsistent R5 object as MISSING.
Check omission's exact cause, support, source/backing, rule/schema/envelope and result
commitments against the issued R5 result/input; an arbitrary caller omission flag is
not evidence. Preserve the full original evidence object, never reconstruct its cause.

| Case | Compatibility dispatch and result |
| --- | --- |
| Valid cancellation, n=1/e=0, exact suppression | unchanged core newCancellation(n,e), EXPECTED_ABSENCE; retain suppression/backing |
| Replacement present, n=1/e=1 | unchanged core newReplacement(n,e), complete/present |
| Explained replacement omission, n=1/e=0 | unchanged core newReplacement(n,e), DIVERGENT_INCOMPATIBLE/DIVERGENT/blocking; retain omission |
| Addition survivor, e=1 | unchanged core newAddition(e), complete/present |
| Explained addition omission, e=0 | new EXPLAINED_ADDITION_OMISSION_V2 variant; DIVERGENT_INCOMPATIBLE/DIVERGENT/blocking, exact e=0 |
| Otherwise malformed/contradictory/unaccounted supplied R5 result | whole ComparisonFailureV2, no classifier/result publication |

The variant is necessary because BOTH old DetectorClassifier and DetectorResult encode
count-only addition absence as MISSING. Neither is changed or bypassed. Never construct
a forged old DetectorResult for the omitted addition, alter NEW_ADICION, substitute
INCOMPATIBLE_EVIDENCE or fake e=1. This specialization uses additional R5 evidence to
preserve the already CLOSED omission meaning; it is not a change to old count-only
behavior. It cannot be selected merely because effective count is zero.

ComparisonEvaluationV2 is a closed immutable tagged result contract with two variants:
CORE_V1 retains the original legal DetectorEvaluationRequest and exact DetectorResult;
EXPLAINED_ADDITION_OMISSION_V2 retains the original R4 source, authenticated-as-supplied
R5 linkage and complete typed omission plus full result fields below. Private issuance
validates variant shape; callers cannot supply arbitrary statuses/counts or mint a
variant without the exact issued R5 evidence. No old DetectorResult is present in this
variant, and the API exposes that absence explicitly rather than pretending V1 approved it.

Both variants expose run/evaluation/rule/version/evaluatedAt, source identity/fingerprint/
snapshot/provenance, target type/reference axis, relation cardinality, all typed candidates,
generated/eligible counts, mapping/ambiguity/reason, result/effective status/count, nominal
count presence, history/selection/reasons, blocking/blocked capabilities, F2D compatibility,
comparison evidence and input/content/execution commitments. Core variant projections
must equal its retained core object exactly. No selected/resolved/winner field exists.

For EXPLAINED_ADDITION_OMISSION_V2: source=NEW_ADICION, nominal count absent and axis
NOT_APPLICABLE; effective reference AJUSTE/id/date; effective relation ONE_TO_ONE required;
generated/eligible/effective counts=0; mapping=NO_CANDIDATES; ambiguity=NOT_AMBIGUOUS,
reason absent; result=DIVERGENT_INCOMPATIBLE, effective=DIVERGENT;
history=NOT_APPLICABLE; selection=NOT_SELECTED_BY_DETECTOR; expectedAbsenceReason and
unsupportedReason absent; blocking=true/BLOCKING and all five CLOSED blocked capabilities
(MAPPING_SELECTION, CROSSWALK_PERSISTENCE, MATERIAL_RESOLVER, MIGRATION, CUTOVER).
F2D compatibility and exact omission/support/backing remain retained; any F2D conflict
also blocks and cannot erase omission evidence. No semantic EXPECTED_ABSENCE/MISSING.

ComparisonResultV2 contains fixed-order complete evaluations and supplemental ledger,
source/evaluation/universe/candidate/outcome counts and bijection commitments. Each
source appears once in its required group; batch release only after all validation.
Type/version is always explicit, never masquerades as the old DetectorResult API.
No productive consumer, serializer route or runtime bean is added. Future R6 design
must explicitly consume this V2 contract rather than repeat rejected D1's old dispatch.

## 5. Canonicalization and executable acceptance specification

New canonicalization uses domain-separated SHA256 over strict UTF-8 typed length-prefix
values, explicit optional/null tags and list/map counts. UUID order is unsigned16;
references type/UUID/date; text unsigned UTF-8. Preserve original closed hashes verbatim;
V2 hashes additionally bind full input/source/R5 identity, variant/rule, ordered observations,
all result fields, original core request/result if present, complete omission evidence,
full source/evaluation accounting and supplied completion metadata. No delimiter ambiguity,
self-reference cycle, timestamps as identity, locale/default zone or permutation-induced
content drift. Separate content commitment from invocation/execution identity. Caller
permutation canonicalizes deterministically; duplicate identity never deduplicates.

Future implementation acceptance is bounded to pure comparison compatibility, never R6:

1. Old classifier/result constructor unchanged counterexamples: NEW_ADDITION(0) still
   MISSING; old direct NEW_ADICION/zero/divergent construction still rejected. V2 must
   classify every one of the eight R5 explained omission causes as divergent with full
   backing/support, true zero counts and every blocked capability. Survivor delegates
   unchanged; forged/missing/duplicate/wrong-adjustment/wrong-reference omission fails.
2. Full legacy atom matrix: complete/fallback, gap/memberless/nonmember, incomplete/outside
   interval, ABSENT activity/instructor, recurrent date=NULL, day mismatch, 0/1/N targets.
   Cancellation matching 2 nominals with different activity/range must remain ambiguous;
   exception UNKNOWN_INTENT at every count; anomalous recurrent divergent. Exactly one
   typed result per R2 atom; no supplied source lost or duplicated. No fake core enums.
3. All NEW cancellation/replacement/addition surviving/omitted/suppressed/orphan/duplicate/
   conflicting cases, truthful original counts, complete backing/accounting and whole-unit
   failure. No baseline R5 semantics or outcome partition may change.
4. Constructor adversaries for every V2 field/variant/count/reason/blocking/provenance/
   identity/canonical scalar, mutation resistance, deterministic permutations/Unicode/
   UUID sign boundary, source/candidate identity collision, empty source units, input
   hash sensitivity and no partial output after a late failure.
5. Exact path/edge/purity guards and negative controls: exact allowed consumer path and
   symbol pairs only; adjacent/fourth/wildcard/FQ/reflection/dynamic/import/bean/productive
   callers remain rejected. No existing R3/R4/R5 own inventory or pure restriction changes.
6. Real ApplicationContext default/dev/prod/shadow configurations show zero V2 beans,
   productive callers, triggers and DB attempts. No new configuration or bean registration.
7. All existing detector tests (Classifier, ResultInvariant, Immutability, F2DAuthorityGuard,
   architecture and candidate generation suites), all nine R5 test suites, R3/R4/R1/R2
   architecture and runtime-isolation suites, then full Maven regression. Existing PG/
   Testcontainers requirements of full regression must run on the competent host and
   retain their original no-write/concurrency/HostValidator obligations; unavailable
   environment stops acceptance, no skip or recycled PASS. Exact commands and XML seals
   belong to the separately authorized executable handoff, with all zero failures/errors/
   unauthorized skips. No dependency/build/config alteration to obtain green.

All executable tests above REQUIRED / NOT_EXECUTED. This documentary lifecycle runs
only scope/hash/link/ancestry/publication checks. No source/test/guard authority is touched,
so technical architecture/DB/HostValidator tests here are NOT_APPLICABLE_DOCUMENTATION_ONLY,
not PASS. No productive/material-source/runtime/snapshot certification is claimed.

## 6. Exact future executable scope: proposal for a new HUMAN_GATE

The following is a frozen PROPOSED allowance, NOT authority to create or modify files.
Pure comparison package is `com.feelingpilates.transicion.programacion.comparison.v2`.
Every other executable NEW/MODIFIED path is default-denied. Source/core/product/config/
migration edits are zero; no R6 facade/owner/admission/validity/capture/config path is included.

### NEW16, all currently absent

| Exact path | Sole reason |
| --- | --- |
| `src/main/java/com/feelingpilates/transicion/programacion/comparison/v2/ComparisonInputV2.java` | supplied-input DTO and complete cross-input binding |
| `src/main/java/com/feelingpilates/transicion/programacion/comparison/v2/ComparisonCandidateEvidenceV2.java` | typed complete per-target observations and exclusion accounting |
| `src/main/java/com/feelingpilates/transicion/programacion/comparison/v2/ComparisonEvaluationV2.java` | private validated CORE_V1 / EXPLAINED_ADDITION_OMISSION_V2 result variants |
| `src/main/java/com/feelingpilates/transicion/programacion/comparison/v2/ComparisonResultV2.java` | immutable exhaustive batch result and source/evaluation bijection |
| `src/main/java/com/feelingpilates/transicion/programacion/comparison/v2/ComparisonCompatibilityV2.java` | pure fixed-order dispatcher, original core delegation, atomic batch return |
| `src/main/java/com/feelingpilates/transicion/programacion/comparison/v2/LegacyCandidateGeneratorV2.java` | approved legacy predicates and typed core candidate generation |
| `src/main/java/com/feelingpilates/transicion/programacion/comparison/v2/OmissionAwareAdjustmentClassifierV2.java` | R4/R5 outcome/backing linkage and omission-aware variant dispatch |
| `src/main/java/com/feelingpilates/transicion/programacion/comparison/v2/ComparisonCanonicalizerV2.java` | strict V2 canonical bytes and domain-separated commitments |
| `src/main/java/com/feelingpilates/transicion/programacion/comparison/v2/ComparisonFailureV2.java` | whole-unit operational failure with retained original cause |
| `src/test/java/com/feelingpilates/transicion/programacion/comparison/v2/ComparisonTestFixturesV2.java` | explicit supplied fixture builder; no physical acceptance claim |
| `src/test/java/com/feelingpilates/transicion/programacion/comparison/v2/LegacyCandidateGeneratorV2Test.java` | full legacy matrix, multiplicity, core typed-result and marker regressions |
| `src/test/java/com/feelingpilates/transicion/programacion/comparison/v2/OmissionAwareAdjustmentClassifierV2Test.java` | all omission causes and old/V2 classifier/result differential controls |
| `src/test/java/com/feelingpilates/transicion/programacion/comparison/v2/ComparisonInvariantV2Test.java` | variant/count/backing/admission/immutability negatives and exhaustive ledger |
| `src/test/java/com/feelingpilates/transicion/programacion/comparison/v2/ComparisonDeterminismV2Test.java` | permutation/order/hash sensitivity and empty-unit/collision controls |
| `src/test/java/com/feelingpilates/transicion/programacion/comparison/v2/ComparisonArchitectureV2Test.java` | exact inventory, edge-specific guard exceptions and default-deny negative fixtures |
| `src/test/java/com/feelingpilates/transicion/programacion/comparison/v2/ComparisonRuntimeIsolationV2Test.java` | real-context no beans/callers/triggers/I/O across profile combinations |

### MODIFIED3, future guard-only changes

| Exact path | Frozen physical SHA256 | Sole permitted change |
| --- | --- | --- |
| `src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/NominalJpaReaderArchitectureTest.java` | `2c022d3cd60fbb2c9c4e3500e6d962416744d8b58c8f42ea318f8feb568d1b0d` | Exact DTO-only consumers: ComparisonInputV2→NominalProgrammingReadSet; OmissionAwareAdjustmentClassifierV2→NominalBackingSnapshot. No R3 ports/context/adapter/executor/mapper or other consumer exception. |
| `src/test/java/com/feelingpilates/transicion/programacion/r4/adapter/jpa/AdjustmentJpaReaderArchitectureTest.java` | `a453d9e1aabd048babbcaeed10774e7829ef28b9bf8ae2573a8ad8de3e23e305` | Exact DTO-only consumers: ComparisonInputV2→AdjustmentReadSet; OmissionAwareAdjustmentClassifierV2→AdjustmentBackingSnapshot. No R4 port/context/adapter/executor/mapper or other consumer exception. |
| `src/test/java/com/feelingpilates/transicion/programacion/r5/composition/EffectiveCompositionArchitectureTest.java` | `7f5a5dd00d50b3c1eca1071848963ed0dec5a770e05de7ebdc025a62a97293c4` | Only ComparisonInputV2→EffectiveProgrammingCompositionResult; ComparisonCompatibilityV2→EffectiveProgrammingCompositionResult; ComparisonEvaluationV2→EffectiveCompositionOmission; OmissionAwareAdjustmentClassifierV2→EffectiveProgrammingCompositionResult,EffectiveCompositionInput,EffectiveCompositionEnvelope,EffectiveCompositionBacking,EffectiveCompositionOmission,EffectiveCompositionSuppression,EffectiveValidityEvidence. No composer invocation or other R5 symbol. |

Every exception must check the exact path AND exact comparison.v2 package AND only its
listed symbols/imports. Preserve all existing guard functions/tests/inventories and
unrelated restrictions. Add negative controls to these same authorized test files;
do not remove/rename tests or replace the old ten-R5-consumer permission. New package
purity forbids framework/persistence/I/O/read ports/owner/config and allows only java
value/canonicalization types, the exact core DTOs and those explicitly listed R3/R4/R5
DTO edges. A FQ/dynamic/reflection edge is not an alternate permitted route. Any extra
consumer or symbol discovered during implementation requires a new gate, not an
unlogged expansion. Existing R5 code never imports comparison.v2.

### READ_ONLY481 and deterministic inventory

Define B as the complete exact tracked path set of immutable Git tree
`545685d195f866826a9be23599ff4c6049acab18` at base552fe66 (484 paths). The future
READ_ONLY set is exactly B minus the three explicitly enumerated MODIFIED paths above:
481 exact existing paths, including all closed production files, tests not listed as
MODIFIED, authorities, configuration and all50 migrations. This is a finite Git-object
set definition, not a directory wildcard. Every path and physical SHA256 is expanded in
retained `FUTURE-EXECUTABLE-PATHS.json`, including NEW16 and MODIFIED3 with reasons in
this design. Its SHA256 is
`dabe90ebc425611ed1c734d575cb061bf798c71be20d413199ddc65ef3a49a06`.
BASELINE.json separately seals all484 original physical bytes. Public publication must
include the exact expanded manifest as PR evidence and verify its readback. The committed
path tables, immutable Git tree and published manifest make scope independently recoverable.
Future NEW paths have no content hashes because implementation has not been written;
claiming future byte hashes would be false. Re-seal actual authorized candidate bytes
only in a future separately approved executable lifecycle.

## 7. Proof obligations for preservation and terminal boundary

Design-only preservation proof: base→this branch is exactly the three authorized NEW
Markdown documents; all484 existing physical SHA256s and Git blobs, source/test/guard/
config/core APIs and50 migration files remain byte-identical. No productive behavior
can change through this documentary-only delta. R1–R5 CLOSED, TurnoInstructor
LEGACY_VIVO / PRODUCTIVO, dark launch, current API/Web/Reservations and no cutover
remain preserved; no runtime/productive DB observation is inferred from unchanged code.

Future executable preservation proof required before its acceptance: MODIFIED only the
three guarded test exception surfaces; NEW16 only isolated plain value/pure classes;
all CLOSED production/core code and existing public APIs/hashes unchanged. Old V1
constructor/classifier counterexamples and full regression keep their original outcomes;
V2 handles only supplied evidence newly available at the comparison boundary. R5 still
alone composes, R3/R4 only observe, R1/R2 keep sources, pure detector still never selects.
The exact guard edge manifest proves no unrelated architecture restriction is relaxed.
Concrete tests in §5 prove typed legacy obligations and omission semantics retained;
no schema/data/routing mutation or bean/caller creates productive behavior. This is an
acceptance specification now, NOT_EXECUTED and not a claim that future code already passes.

After independent P0=0/P1=0 approval and separately verified publication/normal merge,
this design prerequisite may be CLOSED as a documentary lifecycle. It does not activate
its future executable proposal or retroactively approve D1. Because the selected design
requires NEW16 source/test and MODIFIED3 guard changes, terminal next action is a NEW
HUMAN_GATE for exactly §6 with §5 regressions and §7 preservation proofs. Stop before
any executable path or corrected R6 design is changed. User's conditional automatic
R6 resumption applies only if no additional authority gate is required; here one IS
required. A/B remain pending separate shared-admission prerequisites. No narrowed or
observation-only R6 design can evade those requirements.

## 8. Current lifecycle and audit/publication/closure protocol

Write allowance exactly NEW3/MODIFIED0/default DENY:

1. `auditoria/fase-2e-r6-prerrequisito-compatibilidad-comparacion.md`.
2. `auditoria/reviews/F2E-R6-COMPARISON-COMPATIBILITY-INDEPENDENT-RECEIPT.md`.
3. `auditoria/reviews/F2E-R6-COMPARISON-COMPATIBILITY-PROCESS-CLOSURE.md`.

The design and process plan are frozen together before the ONE fresh independent
GPT-6.1-Sol High auditor requested for this prerequisite. That auditor reviews contract,
path scope, fixed integration/closure algorithm and preservation; report must have
P0=0/P1=0. No author/preflight/D1 auditor participation, delegation or model fallback.
Requested/effective and observed launch are recorded without provider telemetry claims.
If blocked, preserve candidate/report and stop; this exact one-auditor authorization
does not grant a second context or automatic acceptance of changed design bytes.

After approval, add only the reserved byte-identical independent report as a direct
factual child, preserving both audited documents. Publication/merge/closure follow the
frozen process plan; they are factual coordinator validation, not a second independent
audit. Do not invent another closure auditor against the user's exact one-auditor
constraint. Any post-audit semantic change invalidates acceptance and needs new authority.
Publish actual candidate/audit/manifest/PR/merge facts and a final read-back terminal
receipt in the merged PR record without changing audited documentary bytes. Capture
headers remain truthful immutable snapshots. No fourth document, STATE/ESTADO rewrite,
implementation/activation handoff or future fabricated IDs/self-hashes.

Raw evidence owner RUN_COORDINATOR, LOCAL_RETAINED at
`~/.codex/feelingpilates-evidence/r6-comparison-compatibility-design-20261013/`;
external backup/provider transcript completeness NOT_GUARANTEED. Public Git/PR documents
and exact evidence readbacks carry published process facts. Publication is not source
implementation, deployment or productive activation.
