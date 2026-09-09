# FeelingPilates — HANDOFF: AUTOPILOT R6 Workflow Engine

**Materialization role:** `POST_R5_SUCCESSOR_ARCHITECT /
AUTHORITY_BOUNDARY_DESIGNER / DEPENDENCY_GRAPH_AUDITOR /
HANDOFF_MATERIALIZER`

**Workflow profile:** `DOCUMENTATION_ONLY / AUTHORITY_MATERIALIZATION /
NO_IMPLEMENTATION / NO_APPROVAL / NO_ACTIVATION / NO_PUBLICATION`

## Lifecycle and canonical target

This handoff materializes the candidate authority for exactly one post-R5
successor:

```text
AUTOPILOT R6 — Workflow Engine
Responsibility: deterministic workflow/gate decision, bounded action
authorization, and durable decision/effect evidence
```

Its lifecycle is exactly:

```text
HANDOFF: MATERIALIZED / APPROVED / ACTIVE
TARGET: SELECTED / AUTHORIZED_TO_START / NOT_STARTED
IMPLEMENTATION: AUTHORIZED_TO_START
INITIAL_HANDOFF_AUDIT: FAIL / P0=0 / P1=4 / P2=0
CORRECTION.1: MATERIALIZED / FRESH_REAUDIT P1=4 OPEN / NEW_P1=0
CORRECTION.2: MATERIALIZED
CORRECTION.2 FRESH REAUDIT: FAIL / P0=0 / OPEN_P1=2 / NEW_P0=0 / NEW_P1=0 / NEW_P2=0
P1-1: CLOSED_BY_CORRECTION_2_FRESH_REAUDIT
P1-2: CLOSED_BY_CORRECTION_2_FRESH_REAUDIT
CORRECTION.3: MATERIALIZED
FINAL_TWO_FINDING_FRESH_REAUDIT: PASS / OPEN_P1=0 / NEW_P0=0 / NEW_P1=0 / NEW_P2=0
P1-3: CLOSED_BY_CORRECTION_3_FINAL_FRESH_REAUDIT
P1-4: CLOSED_BY_CORRECTION_3_FINAL_FRESH_REAUDIT
P1-1/P1-2 REGRESSION: CLOSED / NO_REGRESSION
HANDOFF_AUDIT: PASS
TECHNICAL_AUTHORITY_BLOCKERS: NONE
AUDIT: auditoria/reviews/AUTOPILOT-R6-WORKFLOW-ENGINE-HANDOFF-AUDIT.md
ACTIVE_AUTOPILOT_HANDOFF: R6 WORKFLOW ENGINE
R7: NOT_AUTHORIZED
F2E: UNCHANGED
auto_publish: false
NEXT ALLOWED ACTION: EXECUTE_ACTIVE_AUTOPILOT_R6_WORKFLOW_ENGINE
```

The first fresh independent handoff audit has since completed with
`P0=0 / P1=4 / P2=0 / FAIL`. This Correction.1 addresses, without closing,
exactly those four P1 targets:

```text
FRESH HANDOFF AUDIT: FAIL / P0=0 / P1=4 / P2=0
P1-1: WORKFLOW_DEFINITION_AND_CANONICALIZATION_AUTHORITY_UNFROZEN
P1-2: ACTION_ATTEMPT_EXECUTION_AND_EFFECT_CONTRACT_UNFROZEN
P1-3: STATESTORE_ATOMIC_OPERATION_AUTHORITY_UNFROZEN
P1-4: SQLITE_WORKFLOW_ENGINE_RELATIONAL_AUTHORITY_UNFROZEN
CORRECTION.1: MATERIALIZED / FRESH_REAUDIT P1=4 OPEN / NEW_P1=0
CORRECTION.2: MATERIALIZED
CORRECTION.2 FRESH REAUDIT: FAIL / P0=0 / OPEN_P1=2 / NEW_P0=0 / NEW_P1=0 / NEW_P2=0
P1-1: CLOSED
P1-2: CLOSED
P1-3: OPEN / OBSERVATION_CUSTODY_AUTHORITY_UNFROZEN
P1-4: OPEN / R6_EVIDENCE_RELATIONAL_SCOPE_UNFROZEN
CORRECTION.3: MATERIALIZED
FINAL TWO-FINDING FRESH REAUDIT: PASS / OPEN_P1=0 / NEW_P0=0 / NEW_P1=0 / NEW_P2=0
P1-3: CLOSED
P1-4: CLOSED
P1-1/P1-2 REGRESSION: CLOSED / NO_REGRESSION
READY_TO_APPROVE_AND_ACTIVATE_R6_HANDOFF: SI
ACTIVATION: MATERIALIZED / APPROVED / ACTIVE
NEXT ALLOWED ACTION: EXECUTE_ACTIVE_AUTOPILOT_R6_WORKFLOW_ENGINE
```

Correction.1's fresh bounded re-audit preserved all four findings as open with
`NEW_P1=0`. Correction.2 closed P1-1 and P1-2, while its fresh re-audit kept
P1-3 and P1-4 open because cross-owner observation custody and same-run,
cross-attempt R6 evidence authority remained unfrozen. Correction.3 corrects
only those two residual contract ambiguities and preserves the closed P1-1 and
P1-2 authority unchanged.
Correction materialization alone was not P1 closure, approval, activation, or
implementation authority. The final fresh independent re-audit has now closed
P1-3 and P1-4 with no regression of P1-1/P1-2 and no new finding. The separate
competent lifecycle action recorded by this publication approves and activates
this exact handoff and authorizes implementation only within its exact 21-path
allowlist.

## Baseline and repository authority

The physical baseline independently verified before this materialization was:

```text
Repository: /Users/jesusaldaircruzortiz/Desktop/Feelingpilates/feelingpilates-autopilot-r1
Branch: orquestacion/autopilot-r1
HEAD: 27945f2333c3a44586643c5711f39629c5713185
Resolved upstream: 27945f2333c3a44586643c5711f39629c5713185
Live remote: 27945f2333c3a44586643c5711f39629c5713185
Working tree: CLEAN
Staging: EMPTY
```

The physical Correction.1 preflight preserved the same branch, `HEAD`,
resolved upstream, live remote, and empty staging area. Its working tree was
exactly the already-materialized authority pair and no other path:

```text
 M auditoria/orquestacion/AUTOPILOT-ESTADO-ACTUAL.md
?? auditoria/handoffs/HANDOFF-AUTOPILOT-R6-WORKFLOW-ENGINE.md
```

The selection was derived from the physical R1 preflight, accepted R2 domain
and port contracts, accepted R3 SQLite durability/recovery foundation, closed
R4 CLI fallback/diagnostic adapter, closed R5 Python SDK primary adapter, the
normative orchestration protocol, their handoffs and final audits, and the
current Autopilot state. Repository authority prevails over advisory or chat
history.

The consecutive repository lifecycle `R1` through `R5`, and the current action
that permits post-R5 successor selection, support the next ordinal `R6`. No
ordinal is skipped and no parallel naming scheme is introduced.

## Successor selection

### Selected target

`Workflow Engine` is selected with **HIGH** confidence because it is the
smallest coherent component that closes the present authority/execution gap:

```text
normalized operational fact
→ protocol interpretation
→ deterministic workflow decision
→ durably authorized bounded action
→ durable observation/effect evidence
```

R2 already defines `Workflow`, `Run`, `Attempt`, `AgentResult`,
`ExecutionRequest`, `ExecutionObservation`, `FailureRecord`, `UsageRecord`,
the `AgentExecutor`, `Repository`, `Clock`, and `StateStore` ports. R3 provides
the durable SQLite implementation, optimistic state versioning,
transition/checkpoint atomicity, leases/fencing, idempotency, and recovery
context. R4 and R5 provide concrete executor implementations. None of R2–R5
owns the decision that a fact permits a gate transition or a next action.

R6 consumes those accepted capabilities instead of duplicating them. It is on
the critical path to a first autonomous end-to-end Autopilot, is independently
testable with fake executors/repositories, a deterministic clock, and temporary
SQLite, and can be implemented without first implementing Git mutation,
publication, model routing, context compilation, retry/quota policy, or a
supervisor.

### Rejected or deferred candidates

| Candidate | Accepted inputs it could consume | New authority it would own | Reason not selected now |
| --- | --- | --- | --- |
| `ContextCompiler` | `Repository.read_text`, bounded request facts | provenance-aware prompt/context assembly | Useful and independently testable, but it cannot interpret `AgentResult`, advance gates, or durably authorize a next action. R6 accepts only already-authorized prepared instructions and leaves compilation separate. |
| `ModelRouter` | executor capabilities, requested role/model facts | executor/model selection and fallback choice | Both adapters exist, but no engine currently requests a turn or consumes its result. Routing first would leave the larger workflow-authority gap open. |
| `Retry / Quota Governor` | `FailureRecord`, `UsageRecord`, clock | retry/backoff/quota eligibility | Typed quota/network evidence remains incomplete and retry policy needs workflow state plus durable attempt/effect evidence. Selecting it now would couple policy to an absent engine. |
| `Recovery Engine` | `RunRecoveryContext`, checkpoints, sessions, failures | resume/replay/recovery choice | R3 supplies recovery facts, but legal continuation and action authorization must exist first. R6 records uncertainty and fails closed; recovery choice remains a later phase. |
| `Reconciler` | Git/state/provider evidence | resolution of conflicting or uncertain facts | It depends on repository/Git evidence and recovery policy not yet implemented. It must not be merged into R6. |
| `Repository / Git Adapter` | accepted `Repository` port | physical Git snapshot/read implementation | Independently useful and needed later, but it supplies facts only; it does not close fact-to-decision authority. Git mutation is expressly excluded. |
| `Worktree Manager` | Git adapter, workflow action | worktree creation/assignment/cleanup | Depends on an unimplemented Git adapter and workflow authorization. Selecting it now would invert the dependency order. |
| `Publisher` | approved gate facts, Git adapter | bounded publication action | Depends on the engine and Git adapter. `auto_publish=false` also requires the engine to stop at manual publication authority rather than publish automatically. |
| `Supervisor / runtime composition` | all runtime ports and services | scheduling, process lifecycle, composition | It requires several unimplemented neighbors and would prematurely combine them into a monolith. |

All rejected candidates remain `DEFERRED / NOT_AUTHORIZED`; this comparison
does not materialize their designs or allowlists.

## Exact responsibility boundary

### Input facts and accepted contracts

R6 may consume only typed facts obtained from the exact authoritative origin
declared for each fact:

- one schema-valid, explicitly approved workflow definition and its canonical
  fingerprint;
- one durable `Run` and R6 workflow-control record with exact workflow, phase,
  version, and lifecycle identity;
- the current applicable phase, role, gate, transition targets, correction
  target, and gate applicability from that workflow definition;
- one validated provider-neutral `PreparedExecutionSpecV1` whose instructions,
  workflow, run, phase, role, gate, workspace, sandbox, model, effort, and
  timeout are resolved by explicit upstream authority, but which deliberately
  contains no action, attempt, execution, or resume/session identity;
- the `ExecutionObservation` returned directly to the authorized R6 execution
  path by its unique immediate `get_result`, including exactly one valid
  semantic `AgentResult` on success or one normalized `FailureRecord` on
  terminal failure; after receipt persistence or any owner/fence boundary, R6
  may consume only the durable receipt and never a caller-supplied observation;
- R3 state version, lease/fencing, idempotency, checkpoint, session, usage,
  artifact, failure, and `RunRecoveryContext` facts;
- a `RepositorySnapshot` only as a typed observation when a caller supplies an
  authorized `Repository` implementation; and
- time only through the accepted `Clock` port.

Provider-native SDK/CLI values, free-form logs, chat text, a session identity,
or a process exit code alone are not workflow authority.

### Decisions R6 owns

R6 owns only deterministic workflow-policy decisions for one declared
workflow/run:

1. validate the workflow graph, current phase, current decision version, role,
   gate, applicability, and result identity;
2. determine whether a declared non-applicable phase is skipped explicitly,
   without inventing `PASS`;
3. interpret a semantically valid `AgentResult` under `ORQ-PROTOCOL-V1` and the
   approved workflow definition;
4. choose the one declared next phase after an applicable gate `PASS`;
5. choose the one declared correction phase only for an authorized,
   unambiguous, correctable P1 with an available explicit
   `CORRECTION_REQUIRED` edge in the finitely unrolled DAG;
6. require human authority for P0, a human-decision flag, an ambiguous or
   non-correctable P1, no remaining declared correction edge, missing authority,
   or a safety stop;
7. enter the manual publication boundary when all pre-publication gates pass;
8. mark terminal completion only when the declared profile has no remaining
   applicable phase and all required prior gates are durably satisfied; and
9. authorize exactly one bounded action only after its identity and policy
   decision are durably committed.

`NOT_APPLICABLE`, `PENDING`, `PASS`, `FAIL`, `BLOCKED`, and `STOP` retain their
accepted meanings. R6 must not convert a future gate to `PASS`, treat
`NOT_APPLICABLE` as `PASS`, or interpret a pre-semantic provider failure as a
P0/P1/P2 or gate result.

The minimum semantic matrix is binding:

| Consumed semantic fact | R6 disposition |
| --- | --- |
| Declared non-applicable phase | Persist explicit skip; preserve `NOT_APPLICABLE`; select only its declared successor. |
| Applicable gate `PASS`, agent status compatible, no P0/P1, no human decision | Persist the pass decision; authorize only the declared next action. |
| `PENDING` | Preserve pending state; no next action. |
| `FAIL` or `BLOCKED` | Persist the result; no implicit correction or retry; follow only an explicitly declared safe stop. |
| `STOP`, any P0, `SECURITY_STOP`, or `requires_human_decision=true` | Persist stop evidence and require human authority. |
| Nonempty P1, `p1_correctable=true`, one declared correction target, correction artifact present, explicit edge available | Authorize only that correction target. |
| Nonempty P1 with ambiguity, no correction authority, invalid correction shape, or no remaining declared correction edge | Require human authority; no next external effect. |
| `PASS` contradicted by P0/P1, human-decision requirement, incompatible agent status, or undeclared transition | Fail closed as a contract/authority inconsistency; do not progress. |
| Failed, interrupted, pending, running, or unknown `ExecutionObservation` without `AgentResult` | Treat as operational fact only; invent no semantic gate result and authorize no implicit retry. |

### Actions R6 may request or perform

After durable authorization, R6 may:

- request or start exactly one declared agent phase through an injected
  `AgentExecutor` using the already-bounded `ExecutionRequest`;
- observe that exact execution through `get_result`; R6 V1 does not call
  `resume` or `interrupt`, and consumes only adapter-normalized cancellation,
  timeout, interruption, or containment facts;
- request an authorized correction phase, documentation phase, independent
  audit phase, deterministic host-validation phase, or manual publication
  wait, as declared by the approved workflow;
- create an unresolved `HumanDecision` request when the policy requires human
  authority; and
- call only accepted or explicitly extended `StateStore` operations needed to
  commit the R6 definition, run-control version, action authorization,
  observation/effect evidence, decision, transition, and checkpoint.

R6 selects neither the concrete executor nor model. An injected executor and
already-resolved request are prerequisites. Automatic SDK-to-CLI fallback is
forbidden.

R6 may reach only the exact `READY_TO_PUBLISH` durable manual-publication
boundary defined below, but with `auto_publish=false` it must not call a
publisher or execute Git publication.

### Durable evidence R6 produces

R6 must produce, through `StateStore`, durable and cross-linked evidence for:

- workflow definition version and canonical fingerprint;
- current phase, lifecycle state, gate applicability, and optimistic decision
  version;
- stable action, attempt, and execution identities;
- the fact/evidence reference consumed by each decision;
- the decision rule and previous/next workflow state;
- pre-effect action authorization, request fingerprint, idempotency identity,
  and mandatory run-scoped lease/fencing evidence for every provider action;
- one immutable observation receipt containing the normalized observation
  status and bounded provider-neutral session, usage, failure, artifact, and
  evidence-reference snapshot, plus an effect bound to that receipt when one
  exists;
- explicit `UNCERTAIN_EFFECT` state when authorization exists but no trustworthy
  terminal effect can be established; and
- one atomic decision/transition/checkpoint state change for each durable
  workflow advancement.

StateStore implements transactions and storage. R6 supplies policy intent and
typed records; it must not issue SQL, control WAL, calculate fencing tokens, or
reimplement R3 idempotency.

### Fail-closed conditions

R6 must authorize no next side effect when any of these holds:

- workflow definition missing, invalid, cyclic, ambiguous,
  unfingerprinted, or inconsistent with the durable run;
- phase, role, gate, workflow, run, attempt, execution, result, checkpoint,
  session, repository snapshot, lease, fencing, or version identity mismatch;
- undeclared next/correction target, duplicate phase/action identity, stale
  decision version, idempotency conflict, or conflicting payload fingerprint;
- an applicable required gate is absent, `PENDING`, `FAIL`, `BLOCKED`, or
  `STOP` when a transition requires `PASS`;
- P0, unresolved human decision, ambiguous/non-correctable P1, no remaining
  declared correction edge,
  `SECURITY_STOP`, or authority outside the approved profile;
- failed/interrupted/unknown execution without a separately authorized
  deterministic policy action;
- action authorization committed but provider-effect outcome is missing,
  conflicting, or cannot be proved after restart;
- requested retry, resume, fallback, Git mutation, publication, context
  expansion, model selection, or broader sandbox authority is not explicitly
  owned by R6; or
- `auto_publish` is true, absent where required, contradictory, or would cause
  automatic publication.

An uncertain external effect is not automatically replayed. With the required
later valid recording lease, R6 persists `UNCERTAIN_EFFECT`, transitions to
`FAILED_SAFE`, and awaits a later authorized Recovery Engine or Reconciler. If
that lease is unavailable, it returns the durable
`AUTHORIZED_NOT_OBSERVED` record as unresolved, makes no mutation or provider
call, and fails closed. It does not guess whether the provider acted.

### Explicit non-responsibilities

R6 does not own:

- provider transport, SDK/CLI parsing, session mechanics, timeout cleanup, or
  usage normalization already owned by `AgentExecutor` adapters;
- SQLite transaction implementation, lease allocation, fencing-token
  generation, or storage policy already owned by `StateStore`;
- repository evidence selection, context compilation, prompt authoring,
  provenance budgeting, omission/truncation policy, or secret discovery;
- model/executor routing, automatic fallback, quota policy, retry/backoff,
  recovery/replay/resume eligibility, or uncertain-effect reconciliation;
- Git commands, worktree mutation, staging, commit, push, publication, remote
  reconciliation, or branch policy;
- process scheduling, daemonization, launchd, long-lived polling, worker
  supervision, or runtime composition;
- gate content review, human architectural/product judgment, or conversion of
  an agent claim into physical evidence;
- F2E/product execution or migration, runtime activation, productive authority,
  product fence, or cutover; or
- R7 or any later target.

## Adjacent ownership freeze

| Boundary | R6 owns | Adjacent owner remains |
| --- | --- | --- |
| R6 ↔ `AgentExecutor` | legal authorization and consumption of normalized observation | adapter owns provider execution mechanics and normalization |
| R6 ↔ `StateStore` | policy intent, identities, records to commit, expected version | store owns atomicity, SQL, WAL, optimistic update, idempotency and fencing enforcement |
| R6 ↔ `ContextCompiler` | validates identity/bounds of prepared instructions only | future compiler owns source selection, provenance, budget, omissions and secret boundaries |
| R6 ↔ `ModelRouter` | requires one resolved executor/model request | future router owns primary/fallback/model/effort selection |
| R6 ↔ Retry/Quota Governor | persists normalized fact and stops safely | future governor owns retryability, delay, quota and budget decisions |
| R6 ↔ Recovery/Reconciler | detects and records unresolved/uncertain state | future components own resume/replay eligibility and cross-source reconciliation |
| R6 ↔ Repository/Git | may consume typed snapshot/read facts | future adapter owns Git implementation; no mutation is authorized |
| R6 ↔ Publisher | may authorize only a manual-wait boundary | future publisher owns a separately approved publication action |
| R6 ↔ Supervisor | one bounded engine step and deterministic return | future supervisor owns scheduling, process lifecycle and composition |

Provider adapters must not become workflow authority. `StateStore` must not
become policy authority.

## External side-effect and attempt boundary

R6 owns the minimum side-effect envelope required to avoid knowingly invoking
a provider before durable authorization:

```text
validate workflow/run/phase/prepared execution spec
→ deterministically derive stable action/attempt/execution identities
→ atomically commit action authorization + workflow checkpoint
→ only then call AgentExecutor.start exactly once
→ call get_result exactly once and normalize the provider-neutral observation
→ while the current fence remains valid, durably commit the immutable
  WorkflowObservationReceiptV1 without changing workflow control version
→ finalize the effect only from that receipt, or finalize no-receipt uncertainty
→ atomically commit the resulting workflow decision + checkpoint
```

Raw cross-owner `ExecutionObservation`, `AgentResult`, `FailureRecord`,
`UsageRecord`, `SessionReference`, or artifact payloads are forbidden as fresh
semantic assertions. Only a durable `WorkflowObservationReceiptV1` created by
the authorized R6 capture path proves that the unique `get_result` observation
may cross an owner/fence boundary. If the process stops or the fence expires
before receipt commit, the outcome is `UNCERTAIN_EFFECT` with reason
`NO_DURABLE_OBSERVATION_RECEIPT`. R6 must not turn that gap into an automatic
retry, new session, resume, CLI fallback, provider-history lookup, or semantic
success.

R6 owns attempt identity, pre-effect authorization evidence, request
fingerprinting, and post-observation effect recording. Replay/retry eligibility
and resolution of uncertain outcomes belong to later Recovery/Reconciler
authority. This split is explicit and must be tested.

## Workflow Definition V1 authority — Correction.1 as amended by Correction.2

Correction.1 replaces the previously underspecified contract delta with the
following binding format. It does not authorize implementation.

### Exact top-level object

`WORKFLOW_SCHEMA_VERSION` is exactly `workflow-definition-v1`.
`workflow.schema.json` must accept one JSON object containing exactly these
nine required fields and no optional or unknown fields:

| Field | Exact JSON contract |
| --- | --- |
| `schema_version` | string constant `workflow-definition-v1` |
| `workflow_id` | canonical identifier |
| `profile_id` | canonical identifier |
| `entry_phase_id` | canonical phase identifier |
| `auto_publish` | boolean constant `false` |
| `max_correction_actions` | integer, not boolean, `0..16` |
| `phases` | nonempty array of phase objects; semantically unordered |
| `gates` | nonempty array of gate objects; semantically unordered |
| `transitions` | nonempty array of transition objects; semantically unordered |

All Workflow Definition V1 identifier fields use ASCII lowercase kebab-case,
length `1..64`, and the
regular expression `^[a-z][a-z0-9]*(?:-[a-z0-9]+)*$`. Values are already
canonical: no trimming, case folding, Unicode normalization, aliasing, or
fallback by array position is permitted. Every duplicate identifier or
unresolved reference makes the whole definition invalid.

### Exact phase, gate, and transition objects

A phase contains exactly these five required fields:

```text
phase_id: canonical identifier
kind: IMPLEMENTATION | AUDIT | CORRECTION | DOCUMENTATION | VALIDATION | PUBLICATION
role: EXECUTOR | AUDITOR | CORRECTOR | DOCUMENTER | DOCUMENT_AUDITOR |
      HOST_VALIDATOR | PUBLISHER
gate_id: canonical identifier
action_mode: AGENT_EXECUTION | EXTERNAL_FACT_WAIT |
             MANUAL_PUBLICATION_BOUNDARY
```

The only legal role/kind/action combinations are:

```text
IMPLEMENTATION / EXECUTOR / AGENT_EXECUTION
AUDIT / AUDITOR / AGENT_EXECUTION
AUDIT / DOCUMENT_AUDITOR / AGENT_EXECUTION
CORRECTION / CORRECTOR / AGENT_EXECUTION
DOCUMENTATION / DOCUMENTER / AGENT_EXECUTION
VALIDATION / HOST_VALIDATOR / EXTERNAL_FACT_WAIT
PUBLICATION / PUBLISHER / MANUAL_PUBLICATION_BOUNDARY
```

`EXTERNAL_FACT_WAIT` records that R6 awaits a separately authorized,
deterministic HostValidator fact; R6 does not execute host commands.
`MANUAL_PUBLICATION_BOUNDARY` is a non-executable control boundary and never
authorizes or invokes a publisher. Its referenced gate must be
`PUBLICATION / APPLICABLE`.

A gate contains exactly these three required fields:

```text
gate_id: canonical identifier
gate_kind: SCOPE | TESTS | IMPLEMENTATION | HOST_VALIDATION |
           DOCUMENTATION | PUBLICATION | PUBLICATION_CLOSURE
applicability: APPLICABLE | NOT_APPLICABLE
```

A transition contains exactly these six required fields. The binding constant
is `TRANSITION_REQUIRED_FIELD_COUNT = 6`; there is no seventh field:

```text
transition_id: canonical identifier
from_phase_id: canonical identifier
gate_id: canonical identifier
trigger: PASS | NOT_APPLICABLE | CORRECTION_REQUIRED |
         MANUAL_PUBLICATION_BOUNDARY | HUMAN_STOP | FAILED_SAFE
to_phase_id: canonical identifier or null
terminal_outcome: COMPLETED | READY_TO_PUBLISH | HUMAN_STOP |
                  FAILED_SAFE | null
```

Exactly one of `to_phase_id` and `terminal_outcome` is non-null. That structural
rule is further restricted by the complete trigger/target matrix below; it does
not authorize an arbitrary trigger to choose an arbitrary phase or terminal.
The
transition's `gate_id` must equal the gate referenced by its source phase.
Each `(from_phase_id, trigger)` pair is unique. A
`MANUAL_PUBLICATION_BOUNDARY` trigger targeting `READY_TO_PUBLISH` is legal
only from a `PUBLICATION / PUBLISHER / MANUAL_PUBLICATION_BOUNDARY` phase;
entering it leaves publication and publication-closure gates `PENDING`, invokes
no external action, and preserves `auto_publish=false`.

### Explicit graph, entry, terminal, applicability, and cycle rules

The definition is an explicit ID-referenced directed graph. Array position has
no semantic authority. `entry_phase_id` must resolve exactly once and is the
only phase with no incoming non-terminal edge. Every other phase has at least
one incoming edge, every phase is reachable from entry, and every path reaches
a terminal outcome.

The graph over non-null `to_phase_id` edges must be a DAG. Self-edges and all
direct or indirect cycles are invalid. Correction is represented by finitely
unrolled, distinct phase identities, for example
`audit-1 -> correct-1 -> reaudit-1`; it never returns to a previously visited
phase. `CORRECTION_REQUIRED` must target a `CORRECTION` phase. The exact number
of `CORRECTION_REQUIRED` edges must equal `max_correction_actions`; therefore
the schema-level maximum is finite and exhaustion cannot be reinterpreted as a
retry. A correctable P1 at a phase without such an edge produces
`HUMAN_STOP`. R6 never synthesizes a correction phase or edge dynamically.

For an `APPLICABLE` gate, its source phase has exactly one `PASS`, one
`HUMAN_STOP`, and one `FAILED_SAFE` transition, plus zero or one
`CORRECTION_REQUIRED` transition. It has no `NOT_APPLICABLE` transition. For a
`NOT_APPLICABLE` gate, its source phase has exactly one
`NOT_APPLICABLE` transition, no `PASS` or `CORRECTION_REQUIRED` transition,
and R6 invokes no action for that phase. A publication-boundary phase is the
single exception to outgoing-edge cardinality: it has exactly one
`MANUAL_PUBLICATION_BOUNDARY` edge to terminal `READY_TO_PUBLISH`, no `PASS`
edge, and R6 selects it without a gate result or PUBLISHER invocation.

The following matrix is exhaustive and is schema authority:

| Trigger | Legal non-null target | Additional exact condition |
| --- | --- | --- |
| `PASS` | `to_phase_id` | Target is the declared next phase and all ordinary graph/reference/DAG rules hold. |
| `PASS` | terminal `COMPLETED` | No publication boundary is applicable to this profile/path, no pre-completion phase remains on the path, and every applicable prerequisite required for completion is durably satisfied. |
| `PASS` | terminal `READY_TO_PUBLISH` | `auto_publish=false`, manual publication is applicable, no pre-publication phase remains on the path, every applicable pre-publication prerequisite is durably satisfied, and publication plus publication-closure remain `PENDING`. This is the compact terminal encoding when the definition does not use a separate publication-boundary phase on that path. |
| `NOT_APPLICABLE` | `to_phase_id` | Source gate is declared `NOT_APPLICABLE`; this is an explicit skip and creates no `PASS`. |
| `NOT_APPLICABLE` | terminal `COMPLETED` or `READY_TO_PUBLISH` | Only under the same respective completion/publication conditions as above, without fabricating `PASS`. |
| `CORRECTION_REQUIRED` | `to_phase_id` | Target is a distinct, explicit `CORRECTION` phase and the edge preserves the DAG. |
| `MANUAL_PUBLICATION_BOUNDARY` | terminal `READY_TO_PUBLISH` | Source is exactly `PUBLICATION / PUBLISHER / MANUAL_PUBLICATION_BOUNDARY`; no publisher is invoked. |
| `HUMAN_STOP` | terminal `HUMAN_STOP` | No phase target and no other terminal are legal. |
| `FAILED_SAFE` | terminal `FAILED_SAFE` | No phase target and no other terminal are legal. |

`PASS` may never target `HUMAN_STOP` or `FAILED_SAFE`.
`CORRECTION_REQUIRED` may never target a terminal. If no valid declared
correction edge remains, the exact disposition is `HUMAN_STOP`, preserving the
protocol rule for an exhausted correction limit; it is not a synthesized edge,
retry, or `FAILED_SAFE`. `PENDING` is not a transition trigger in V1: it creates
the non-progressing `WAIT` decision, retains the phase, and follows no graph
edge. Therefore `NOT_APPLICABLE != PENDING != PASS` both in validation and at
runtime.

For schema validation, “publication is applicable” means the definition
contains at least one `APPLICABLE` gate whose `gate_kind` is `PUBLICATION`.
`PASS` or `NOT_APPLICABLE` may terminate at `READY_TO_PUBLISH` only under that
condition and when no separate publication-boundary phase follows that source;
they may terminate at `COMPLETED` only when the definition contains no
`APPLICABLE` publication gate. A gate need not be referenced by a phase only
when its kind is `PUBLICATION` or `PUBLICATION_CLOSURE` and it exists solely to
retain the required `PENDING` lifecycle for a compact direct
`READY_TO_PUBLISH` encoding. All other gates must be referenced by at least one
phase. These are definition-time validity rules. Runtime selection additionally
requires the durable prerequisite states named in the matrix; failure to prove
them authorizes no transition.

Terminal outcomes are not phases and have no outgoing edges. `COMPLETED`
means every applicable prerequisite on that path is durably satisfied;
`READY_TO_PUBLISH` means manual publication authority is still required;
`HUMAN_STOP` requires competent human authority; and `FAILED_SAFE` is a
non-progressing operational terminal for R6. A transition with both or neither
target representations is invalid.

### Exact gate-to-policy mapping

R6 consumes the accepted `GateResult` vocabulary unchanged:

| Normalized fact | Exact R6 decision |
| --- | --- |
| `NOT_APPLICABLE` | Legal only for a definition gate declared `NOT_APPLICABLE`; follow its exact edge without creating `PASS`. |
| `PENDING` | `WAIT`; persist no progress transition and authorize no action. |
| `PASS` | Legal only for `APPLICABLE`; reject contradictions, then follow the exact `PASS` edge. |
| nonempty correctable P1 with valid artifact | Follow the exact `CORRECTION_REQUIRED` edge; if absent, `HUMAN_STOP`. |
| `FAIL` or `BLOCKED` | Follow the exact `FAILED_SAFE` terminal edge; no correction or retry is inferred. |
| `STOP`, P0, `SECURITY_STOP`, human flag, ambiguous/non-correctable P1 | Follow the exact `HUMAN_STOP` terminal edge. |
| entry into `MANUAL_PUBLICATION_BOUNDARY` | Persist `READY_TO_PUBLISH` with publication gates still `PENDING`; do not interpret or fabricate `PASS`. |

Unknown values, missing edges, multiple matching edges, role/gate/status
contradictions, or a `PASS` carrying P0/P1/human authority are invalid and
authorize neither progress nor an external effect.

### Exact workflow canonicalization and fingerprint

Validation precedes canonicalization. After validation, canonicalization is:

1. retain all nine top-level fields and every field of every child object;
   there is no non-semantic metadata in V1;
2. sort `phases` ascending by the UTF-8 byte sequence of `phase_id`;
3. sort `gates` ascending by the UTF-8 byte sequence of `gate_id`;
4. sort `transitions` ascending by the UTF-8 byte sequence of
   `transition_id`;
5. recursively sort JSON object keys by Unicode code point, encode with UTF-8,
   `ensure_ascii=false`, compact separators `,` and `:`, no trailing newline,
   and reject NaN, infinity, floats, duplicate JSON keys, and values outside
   null/boolean/string/integer/array/object;
6. preserve strings byte-for-byte after UTF-8 decoding; perform no whitespace,
   Unicode, path, or case normalization; and encode the sole numeric field as
   its base-10 JSON integer with no leading zero or plus sign.

No collection is semantically ordered in V1; relations come only from IDs and
edges. The workflow fingerprint is lowercase hexadecimal SHA-256 over exactly
those canonical UTF-8 bytes. Every V1 field is fingerprint-authoritative. A
policy change changes the fingerprint; a representation-only reorder of the
three arrays or of object keys does not.

### Binding workflow golden

The canonical bytes for the binding golden fixture are exactly the following
single line, without a trailing newline:

```json
{"auto_publish":false,"entry_phase_id":"audit","gates":[{"applicability":"APPLICABLE","gate_id":"documentation","gate_kind":"DOCUMENTATION"},{"applicability":"APPLICABLE","gate_id":"implementation","gate_kind":"IMPLEMENTATION"}],"max_correction_actions":0,"phases":[{"action_mode":"AGENT_EXECUTION","gate_id":"implementation","kind":"AUDIT","phase_id":"audit","role":"AUDITOR"},{"action_mode":"AGENT_EXECUTION","gate_id":"documentation","kind":"DOCUMENTATION","phase_id":"document","role":"DOCUMENTER"}],"profile_id":"documentation-audit","schema_version":"workflow-definition-v1","transitions":[{"from_phase_id":"audit","gate_id":"implementation","terminal_outcome":"FAILED_SAFE","to_phase_id":null,"transition_id":"audit-failed","trigger":"FAILED_SAFE"},{"from_phase_id":"audit","gate_id":"implementation","terminal_outcome":"HUMAN_STOP","to_phase_id":null,"transition_id":"audit-human","trigger":"HUMAN_STOP"},{"from_phase_id":"audit","gate_id":"implementation","terminal_outcome":null,"to_phase_id":"document","transition_id":"audit-pass","trigger":"PASS"},{"from_phase_id":"document","gate_id":"documentation","terminal_outcome":"FAILED_SAFE","to_phase_id":null,"transition_id":"document-failed","trigger":"FAILED_SAFE"},{"from_phase_id":"document","gate_id":"documentation","terminal_outcome":"HUMAN_STOP","to_phase_id":null,"transition_id":"document-human","trigger":"HUMAN_STOP"},{"from_phase_id":"document","gate_id":"documentation","terminal_outcome":"COMPLETED","to_phase_id":null,"transition_id":"document-pass","trigger":"PASS"}],"workflow_id":"golden-workflow"}
```

Its required fingerprint is:

```text
0b7b19fac6fc44ebeca474990b3de1fd5701590fcc5bfe51fd099525d768de54
```

Future tests must prove that reordered phase/gate/transition arrays produce
that exact value; a separately valid graph with a different legal PASS target
produces a different value; an applicability change accompanied by its
required valid edge-shape change produces a different value; and an unknown
field, duplicate ID, unresolved reference, or cycle is invalid before hashing.

## Action, attempt, execution, receipt, and effect authority — Correction.1 as amended by Correction.3

### Prepared execution and request fingerprint

R6 is the only attempt authority. A caller supplies
`PreparedExecutionSpecV1`, not a pre-authorized `ExecutionRequest`. It contains
exactly these fields:

```text
workflow_id, run_id, phase_id, gate_id, role,
instructions, working_directory, sandbox, model,
reasoning_effort, timeout_milliseconds
```

All are required. `workflow_id`, `phase_id`, and `gate_id` obey the Workflow
Definition V1 canonical identifier rule; `run_id` is the exact nonblank
accepted R2 `RunId`; `role` exactly equals the referenced phase role;
`instructions`, `working_directory`, `model`, and `reasoning_effort` are
nonblank strings; `working_directory` is an upstream-resolved absolute trusted
workspace path; `sandbox` is exactly `READ_ONLY` or `WORKSPACE_WRITE`; and
`timeout_milliseconds` is an integer, not boolean, in `1..86400000`. The spec
contains no action, attempt, execution, session, or resume identity. R6 V1 does
not resume provider sessions.

The request-fingerprint projection contains exactly the eleven fields above,
with the same names and values. It uses the same canonical JSON rules, except
it contains no semantic arrays, and is lowercase hexadecimal SHA-256 over the
canonical bytes. No Python `repr`, float seconds, environment value, timestamp,
or provider-native object enters the projection. R6 constructs the final
accepted `ExecutionRequest` with `run_id`, `instructions`, `workflow_id`,
`role`, `gate`, `working_directory`, `sandbox`, `model`, and
`reasoning_effort` copied exactly; `timeout_seconds` equal to
`timeout_milliseconds / 1000` only after fingerprinting; and the deterministic
`attempt_id` and `execution_id` below. The accepted request has no `phase_id`
field, so phase identity is bound through the durable R3 `Attempt` and verified
against the prepared spec rather than invented or dropped.

Binding request golden:

```json
{"gate_id":"implementation","instructions":"Audit the bounded change.","model":"gpt-5.6-terra","phase_id":"audit","reasoning_effort":"high","role":"AUDITOR","run_id":"run-001","sandbox":"READ_ONLY","timeout_milliseconds":300000,"workflow_id":"golden-workflow","working_directory":"/workspace/feelingpilates"}
```

```text
request_fingerprint = 07200548a86a2f95702f6a8d2bc88eafe599bb6dc63c94b0068f0a9f62fadaa3
```

### Exact provider-neutral domain delta

`domain/workflow.py` owns these immutable V1 values and no provider, SQLite,
Git, or SDK/CLI type:

| Value | Exact fields / vocabulary |
| --- | --- |
| `WorkflowDefinitionV1` | the exact nine definition fields above plus computed `canonical_bytes` and `workflow_fingerprint` |
| `PreparedExecutionSpecV1` | the exact eleven request-projection fields above plus computed `request_fingerprint` |
| `WorkflowControlLifecycle` | `READY`, `ACTION_AUTHORIZED`, `WAITING_EXTERNAL`, `HUMAN_STOP`, `FAILED_SAFE`, `READY_TO_PUBLISH`, `COMPLETED` |
| `WorkflowControlV1` | workflow/run/fingerprint, current phase, lifecycle, `control_version` |
| `WorkflowDecisionRecordKind` | `STANDALONE_POLICY`, `ACTION_AUTHORIZATION`, `EFFECT_FINALIZATION` |
| `WorkflowDecisionKind` | `AUTHORIZE_PHASE_ACTION`, `ADVANCE`, `CORRECTION_REQUIRED`, `WAIT`, `EXTERNAL_FACT_WAIT`, `HUMAN_STOP`, `FAILED_SAFE`, `READY_TO_PUBLISH`, `COMPLETED` |
| `WorkflowDecisionV1` | decision ID/record kind/decision kind, workflow/run/fingerprint, control version before/after, source phase/gate, nullable definition transition/target phase/terminal outcome/action ID, required evidence fingerprint |
| `WorkflowActionAuthorizationV1` | action/decision/workflow/run/fingerprint/phase/gate/request identities, attempt and execution IDs, protected key/token, authorization timestamp, `AUTHORIZED_NOT_OBSERVED` |
| `WorkflowObservationReceiptV1` | immutable action/attempt/execution/request-bound normalized observation snapshot, receipt identity, capture fence evidence, and timestamp |
| `WorkflowEffectState` | the exact five-state vocabulary below |
| `WorkflowUncertaintyReason` | `NONTERMINAL_OBSERVATION`, `NO_DURABLE_OBSERVATION_RECEIPT` |
| `WorkflowEffectEvidenceV1` | effect/action/receipt/workflow/run/phase/attempt/execution identities, terminal state, uncertainty reason, observation fingerprint, recording token, timestamp |
| `WorkflowAuthorizationResultV1` | durable authorization fields, committed version, `replayed` boolean |
| `WorkflowObservationReceiptResultV1` | durable receipt fields and `replayed` boolean; no control-version result because capture does not advance control |
| `WorkflowEffectResultV1` | durable effect and decision fields, committed version, `replayed` boolean |
| `GateFactV1` | workflow/run/fingerprint/phase/gate identity, accepted `GateResult`, evidence fingerprint |

The exact record field names are binding:

```text
WorkflowControlV1(
  workflow_id, workflow_fingerprint, run_id, current_phase_id,
  lifecycle, control_version
)

WorkflowDecisionV1(
  decision_id, decision_record_kind, decision_kind,
  workflow_id, workflow_fingerprint, run_id,
  control_version_before, control_version_after, phase_id, gate_id,
  definition_transition_id, target_phase_id, terminal_outcome,
  action_id, evidence_fingerprint, created_at
)

WorkflowActionAuthorizationV1(
  action_id, decision_id, workflow_id, workflow_fingerprint, run_id,
  authorization_control_version, phase_id, gate_id, request_fingerprint,
  attempt_id, execution_id, authorization_lease_id,
  authorization_lease_holder,
  protected_resource_key, authorization_fencing_token, authorized_at,
  effect_state
)

WorkflowObservationReceiptV1(
  receipt_id, workflow_id, workflow_fingerprint, run_id,
  action_id, attempt_id, execution_id, request_fingerprint,
  observation_status, observation_terminal, observation_fingerprint,
  canonical_observation_json,
  capture_lease_id, capture_holder_id, capture_resource_key,
  capture_fencing_token, created_at
)

WorkflowEffectEvidenceV1(
  effect_id, action_id, receipt_id, decision_id,
  workflow_id, workflow_fingerprint,
  run_id, phase_id, attempt_id, execution_id, effect_state,
  uncertainty_reason, observation_status, observation_fingerprint,
  recording_lease_id, recording_lease_holder, protected_resource_key,
  recording_fencing_token,
  recorded_at
)

GateFactV1(
  workflow_id, workflow_fingerprint, run_id, phase_id, gate_id,
  gate_result, evidence_fingerprint
)
```

`decision_record_kind` is derived exactly from the committing operation:
authorization uses `ACTION_AUTHORIZATION`, a decision atomically committed
with an effect uses `EFFECT_FINALIZATION`, and a non-provider decision uses
`STANDALONE_POLICY`. Every nullable decision field is null only when its decision kind makes the
concept inapplicable; the decision constructor enforces that exact shape. In
particular, `AUTHORIZE_PHASE_ACTION` requires action ID and no terminal
outcome; `ADVANCE`/`CORRECTION_REQUIRED` require a definition transition and
target phase; terminal decision kinds require their matching definition
transition and terminal outcome and no target phase. `WAIT` and
`EXTERNAL_FACT_WAIT` change no phase and have no definition transition.
`evidence_fingerprint` is always required: it is the request fingerprint for
authorization, the consumed `GateFactV1.evidence_fingerprint` for a gate-driven
non-effect decision, the workflow fingerprint for the deterministic manual-
publication boundary, the receipt's observation fingerprint for a
receipt-backed effect decision, or the exact no-receipt uncertainty fingerprint
defined below.
`action_id` is required by
`AUTHORIZE_PHASE_ACTION` and by every decision committed with an effect;
standalone skip/wait/manual/terminal decisions have it null.

The control mutation for each decision kind is exact:

```text
AUTHORIZE_PHASE_ACTION -> same current_phase_id / ACTION_AUTHORIZED
ADVANCE                -> target_phase_id / READY
CORRECTION_REQUIRED    -> target_phase_id / READY
WAIT                   -> same current_phase_id / WAITING_EXTERNAL
EXTERNAL_FACT_WAIT     -> same current_phase_id / WAITING_EXTERNAL
HUMAN_STOP             -> same current_phase_id / HUMAN_STOP
FAILED_SAFE            -> same current_phase_id / FAILED_SAFE
READY_TO_PUBLISH       -> same publication current_phase_id / READY_TO_PUBLISH
COMPLETED              -> same source current_phase_id / COMPLETED
```

Terminal controls retain the source phase identity; `current_phase_id` is never
null. No decision kind has any other control mutation in V1.

The corresponding accepted R3 `OperationalState` is also exact:

```text
READY                 -> PENDING
ACTION_AUTHORIZED     -> RUNNING
WAITING_EXTERNAL      -> PAUSED
HUMAN_STOP            -> HUMAN_DECISION_REQUIRED
FAILED_SAFE           -> FAILED_SAFE
READY_TO_PUBLISH      -> PAUSED
COMPLETED             -> COMPLETED
```

Each R6 mutation supplies a `StateTransition` whose `previous` is the durable
pre-mutation `Run.state`, whose `current` is the mapped post-decision state,
whose `event` is exactly the `WorkflowDecisionKind` value, whose `actor` is
`WORKFLOW_ENGINE`, whose accepted-R3 `phase_id` is the deterministic durable
phase key for the decision's workflow-local phase, whose `gate_reference` is
the workflow-local `gate_id`, whose
`evidence_reference` is `sha256:<evidence_fingerprint>`, and whose
`state_version` is `control_version_after`. Its `Checkpoint` uses that same
post-decision operational state/version, transition ID, and evidence reference.
The `StateTransition` for authorization/effect mutations also carries the exact
protected key and fence; standalone non-effect transitions leave those two
fields null. Both R3 records carry the enclosing operation's exact idempotency
key, operation kind, canonical identity, and payload fingerprint.

### Stable action, attempt, execution, and decision identities

An executable action's canonical identity projection contains exactly the
pre-authorization `expected_control_version` under the field name
`control_version`:

```text
workflow_id
workflow_fingerprint
run_id
control_version
phase_id
gate_id
action_kind = AGENT_EXECUTION
request_fingerprint
```

It uses the same canonical JSON algorithm. Let `D` be its lowercase SHA-256:

```text
action_id    = action-<D>
attempt_id   = attempt-<D>
execution_id = execution-<D>
```

The action/attempt/execution relationship is one-to-one. R6 binds an accepted
R3 `Attempt` with `ordinal=0`; because the V1 graph is a DAG, a run can execute
each phase at most once. `attempt_id` is the durable workflow invocation
identity; the distinct `execution_id` is the mandatory AgentExecutor
handle/observation correlation identity. Both are deterministic aliases of the
same authorization digest and neither introduces additional cardinality. The
final `ExecutionRequest` supplies both IDs, so R4 or R5 must not allocate a
random execution identity. A second invocation for the same action is
forbidden.

Let `E` be the lowercase SHA-256 of the decision canonical JSON containing
exactly `workflow_id`, `workflow_fingerprint`, `run_id`,
`control_version_before`, `decision_kind`, `phase_id`, `gate_id`,
`definition_transition_id` (nullable), `target_phase_id` (nullable),
`terminal_outcome` (nullable), `action_id` (nullable), and
`evidence_fingerprint` (always a lowercase SHA-256). This projection prevents
equal local transition names or evidence in different runs from colliding.
The bound accepted R3 evidence IDs are exactly:

```text
decision_id   = decision-<E>
transition_id = transition-<E>
checkpoint_id = checkpoint-<E>
```

The effect identity is `effect-<SHA256>` over canonical JSON containing exactly
`action_id`. All prefixes are literal lowercase ASCII.

For the workflow and request goldens above, the canonical action bytes are:

```json
{"action_kind":"AGENT_EXECUTION","control_version":0,"gate_id":"implementation","phase_id":"audit","request_fingerprint":"07200548a86a2f95702f6a8d2bc88eafe599bb6dc63c94b0068f0a9f62fadaa3","run_id":"run-001","workflow_fingerprint":"0b7b19fac6fc44ebeca474990b3de1fd5701590fcc5bfe51fd099525d768de54","workflow_id":"golden-workflow"}
```

```text
D            = 4a4a9fb15cb3622e809bf643f1935f3715c5a970e602475ed2f7f8b615a89634
action_id    = action-4a4a9fb15cb3622e809bf643f1935f3715c5a970e602475ed2f7f8b615a89634
attempt_id   = attempt-4a4a9fb15cb3622e809bf643f1935f3715c5a970e602475ed2f7f8b615a89634
execution_id = execution-4a4a9fb15cb3622e809bf643f1935f3715c5a970e602475ed2f7f8b615a89634
```

### Exact provider invocation cardinality

```text
one new durable executable authorization -> one Attempt ordinal 0
one action -> one attempt -> one execution_id
new authorization result replayed=false -> exactly one AgentExecutor.start if
  the process continues to the call; crash/exception before the call -> zero
authorization replayed=true or loaded after re-entry -> zero AgentExecutor calls
one successful start -> exactly one immediate AgentExecutor.get_result call;
  R6 performs no polling loop
one get_result -> one normalized provider-neutral observation
one normalized observation + current valid fence -> zero or one durable receipt
no durable receipt -> no transferable observed-effect authority
same action -> never a second provider invocation under R6
```

A future retry requires separately authorized future retry/governor authority
and a new semantic action contract; R6 V1 provides none. A correction edge
authorizes a distinct correction phase/action, not a retry of the prior action.

### Exact action/effect state machine

The provider-neutral `WorkflowEffectState` vocabulary is exactly:

```text
AUTHORIZED_NOT_OBSERVED
OBSERVED_SUCCEEDED
OBSERVED_FAILED
OBSERVED_INTERRUPTED
UNCERTAIN_EFFECT
```

New authorization creates `AUTHORIZED_NOT_OBSERVED`. Exactly one immutable
terminal effect may then be recorded:

```text
AUTHORIZED_NOT_OBSERVED + SUCCEEDED receipt   -> OBSERVED_SUCCEEDED
AUTHORIZED_NOT_OBSERVED + FAILED receipt      -> OBSERVED_FAILED
AUTHORIZED_NOT_OBSERVED + INTERRUPTED receipt -> OBSERVED_INTERRUPTED
AUTHORIZED_NOT_OBSERVED + PENDING/RUNNING/UNKNOWN receipt
                                               -> UNCERTAIN_EFFECT / NONTERMINAL_OBSERVATION
AUTHORIZED_NOT_OBSERVED + no durable receipt  -> UNCERTAIN_EFFECT /
                                                  NO_DURABLE_OBSERVATION_RECEIPT
```

The observation/effect/decision matrix is exhaustive:

| Sole `get_result` disposition and durable authority | Durable effect | Exact workflow decision |
| --- | --- | --- |
| Durable receipt for `SUCCEEDED` with one valid `AgentResult` | `OBSERVED_SUCCEEDED` | Apply only the already-frozen semantic gate policy loaded from that receipt: `ADVANCE`, `CORRECTION_REQUIRED`, `WAIT`, `HUMAN_STOP`, `FAILED_SAFE`, `READY_TO_PUBLISH`, or `COMPLETED` as the exact declared graph/fact combination requires. |
| Durable receipt for `FAILED` | `OBSERVED_FAILED` | `FAILED_SAFE`; follow the exact `FAILED_SAFE -> FAILED_SAFE` terminal edge. |
| Durable receipt for `INTERRUPTED` | `OBSERVED_INTERRUPTED` | `FAILED_SAFE`; follow the exact `FAILED_SAFE -> FAILED_SAFE` terminal edge. |
| Durable receipt for `PENDING`, `RUNNING`, or `UNKNOWN` | `UNCERTAIN_EFFECT / NONTERMINAL_OBSERVATION` | Immediately classify `FAILED_SAFE`; follow the exact `FAILED_SAFE -> FAILED_SAFE` terminal edge. |
| Authorized action with no durable receipt | `UNCERTAIN_EFFECT / NO_DURABLE_OBSERVATION_RECEIPT` | `FAILED_SAFE`; follow the exact `FAILED_SAFE -> FAILED_SAFE` terminal edge. |

Only `SUCCEEDED` may derive semantic workflow policy from an `AgentResult`.
`FAILED` and `INTERRUPTED` remain operational evidence and can select neither
`WAIT` nor `CORRECTION_REQUIRED`. `PENDING`, `RUNNING`, and `UNKNOWN` are not
success, retry authority, `WAIT` authority, correction authority, or permission
to poll. Because R6 has only one `get_result` interaction, it classifies any of
those three observations as uncertainty in the same invocation. With a current
valid fence it first commits their receipt and then commits
`UNCERTAIN_EFFECT / NONTERMINAL_OBSERVATION`, the `FAILED_SAFE` decision,
transition, checkpoint, and `N -> N+1`. If receipt mutation authority has been
lost, the raw observation is not transferable: a later valid owner loads no
receipt and may commit only
`UNCERTAIN_EFFECT / NO_DURABLE_OBSERVATION_RECEIPT` without another provider
interaction.

All four terminal effect states are immutable under R6. Same-effect/same-
normalized-observation replay is idempotent; any materially different second
terminal claim is an effect conflict with no mutation. Future
Recovery/Reconciler authority may append separate reconciliation evidence but
must not rewrite the historical R6 effect.

### Complete observation projection and receipt identity — Correction.3

`WorkflowObservationReceiptV1` is the only authoritative cross-owner carrier
for the output of R6's unique `AgentExecutor.get_result`. Raw cross-owner
`ExecutionObservation`, `AgentResult`, `FailureRecord`, `UsageRecord`,
`SessionReference`, and artifact payload submission is not present in the R6
effect-finalization API and must be rejected by any lower-level adapter entry.
A matching caller-computed fingerprint is payload consistency only; it is not
observation provenance.

```text
RAW_CROSS_OWNER_EXECUTION_OBSERVATION = FORBIDDEN
```

The receipt is durable, immutable, append-only, provider-neutral, bounded,
action/attempt/execution/request-scoped, and fencing-protected at creation. It
is evidence custody only: it is not workflow policy, provider-history
authority, or Recovery/Reconciler authority.

After the sole `get_result`, R6 validates and normalizes the typed
`ExecutionObservation`. The exact canonical observation object has these keys
and no others:

```text
schema_version = workflow-observation-v1
workflow_id
workflow_fingerprint
run_id
action_id
attempt_id
execution_id
request_fingerprint
status
terminal
agent_result
failure
session
usage_record
artifacts
exit_code
termination_signal
timed_out
cancelled
stdout_truncated
stderr_truncated
output_limit_exceeded
evidence_references
```

`terminal` is derived and verified, never caller-selected: it is `true` for
`SUCCEEDED`, `FAILED`, and `INTERRUPTED`, and `false` for `PENDING`, `RUNNING`,
and `UNKNOWN`. The accepted `ExecutionObservation` shape is preserved exactly:
`SUCCEEDED` requires one valid `AgentResult` and null failure;
`FAILED`/`INTERRUPTED` require one complete normalized `FailureRecord` and null
AgentResult; nonterminal status requires both null. The workflow/run/action/
attempt/execution/request fields must equal the durable authorization.

Nested values are exact:

- `agent_result` is null or every field of the accepted R2 JSON contract,
  including literal `schema_version=r2`; `findings.p0/p1/p2`, `changed_paths`,
  and `artifacts` are duplicate-free sets sorted by UTF-8 bytes, and
  `test_evidence` is duplicate-free and sorted by each item's canonical JSON
  bytes;
- `failure` is null or exactly `failure_id`, `category`, `message`, `retryable`,
  and `evidence_reference` from the normalized `FailureRecord`;
- `session` is null or exactly `session_id`, `run_id`, `opaque_reference`,
  `adapter`, `role`, `attempt_id`, and `status`; its run and non-null attempt
  must match this action;
- `usage_record` is null or the complete accepted R2 object with
  `schema_version=r2`, `adapter`, and all five exact measurement objects, each
  containing `value`, `provenance`, and `direct_evidence`;
- `artifacts` contains exact objects `artifact_id`, `run_id`, and `reference`,
  rejects duplicate IDs, requires this run, and is sorted by each object's
  canonical JSON bytes; and
- `evidence_references` is derived, not caller-invented: it contains exactly
  each non-null failure evidence reference, every AgentResult test evidence
  reference, and every normalized artifact reference, rejects duplicates, and
  sorts values by UTF-8 bytes.

If `AgentResult.session_reference` and normalized session are both present,
their session identity must match. If embedded and observation-level usage are
both present, their canonical values must match. Any contradiction fails
before receipt persistence. Object keys use the already-frozen R6 canonical
JSON order. Raw output, raw stdout/stderr, provider-native events/objects,
environment, auth, secrets, timestamps, database-local IDs, lease/fence data,
and writer identity are excluded. The three accepted raw-output transport
fields are never persisted in the receipt; their bounded truncation/limit flags
above are persisted. Unknown fields, unbounded references, or provider-native
values fail closed.

The `observation_fingerprint` is lowercase SHA-256 over exactly the canonical
UTF-8 bytes of that object. The receipt identity is:

```text
receipt_id = r6obs-<R>
R = lowercase SHA-256 of canonical JSON containing exactly:
    workflow_id, workflow_fingerprint, run_id, action_id,
    attempt_id, execution_id, request_fingerprint, observation_fingerprint
```

The key order therefore emits
`{"action_id":...,"attempt_id":...,"execution_id":...,"observation_fingerprint":...,"request_fingerprint":...,"run_id":...,"workflow_fingerprint":...,"workflow_id":...}`.
There is zero or one receipt per action. Exact receipt replay is idempotent;
any different observation/receipt for the same action is a conflict.

The exact canonical receipt payload used for idempotency contains these fields
and no others: `schema_version=workflow-observation-receipt-v1`, `receipt_id`,
`workflow_id`, `workflow_fingerprint`, `run_id`, `action_id`, `attempt_id`,
`execution_id`, `request_fingerprint`, `observation_status`,
`observation_terminal`, `observation_fingerprint`, and `observation` containing
the exact canonical observation object. Capture lease/fence evidence,
idempotency metadata, replay status, and `created_at` are excluded from this
semantic payload.

Binding Correction.3 golden fixture canonical observation bytes are exactly
this single line, without a trailing newline:

```json
{"action_id":"action-4a4a9fb15cb3622e809bf643f1935f3715c5a970e602475ed2f7f8b615a89634","agent_result":{"artifacts":["artifact://audit/report"],"changed_paths":[],"correction_artifact":null,"findings":{"p0":[],"p1":[],"p2":[]},"gate":"implementation","gate_result":"PASS","p1_correctable":false,"recommendation":"Advance.","requires_human_decision":false,"role":"AUDITOR","run_id":"run-001","schema_version":"r2","session_reference":"session-001","status":"SUCCEEDED","summary":"Audit passed.","test_evidence":[{"evidence":"artifact://tests/unit","name":"unit","outcome":"PASS"}],"usage_record":{"adapter":"openai-codex","measurements":{"cached_input_tokens":{"direct_evidence":null,"provenance":"UNAVAILABLE","value":null},"input_tokens":{"direct_evidence":"usage://turn-001/input","provenance":"OBSERVED","value":100},"output_tokens":{"direct_evidence":"usage://turn-001/output","provenance":"OBSERVED","value":20},"reasoning_output_tokens":{"direct_evidence":"usage://turn-001/reasoning","provenance":"OBSERVED","value":5},"total_tokens":{"direct_evidence":"usage://turn-001/total","provenance":"OBSERVED","value":125}},"schema_version":"r2"},"workflow_id":"golden-workflow"},"artifacts":[{"artifact_id":"artifact-001","reference":"artifact://audit/report","run_id":"run-001"}],"attempt_id":"attempt-4a4a9fb15cb3622e809bf643f1935f3715c5a970e602475ed2f7f8b615a89634","cancelled":false,"evidence_references":["artifact://audit/report","artifact://tests/unit"],"execution_id":"execution-4a4a9fb15cb3622e809bf643f1935f3715c5a970e602475ed2f7f8b615a89634","exit_code":0,"failure":null,"output_limit_exceeded":false,"request_fingerprint":"07200548a86a2f95702f6a8d2bc88eafe599bb6dc63c94b0068f0a9f62fadaa3","run_id":"run-001","schema_version":"workflow-observation-v1","session":{"adapter":"openai-codex","attempt_id":"attempt-4a4a9fb15cb3622e809bf643f1935f3715c5a970e602475ed2f7f8b615a89634","opaque_reference":"session-001","role":"AUDITOR","run_id":"run-001","session_id":"session-001","status":"COMPLETED"},"status":"SUCCEEDED","stderr_truncated":false,"stdout_truncated":false,"terminal":true,"termination_signal":null,"timed_out":false,"usage_record":{"adapter":"openai-codex","measurements":{"cached_input_tokens":{"direct_evidence":null,"provenance":"UNAVAILABLE","value":null},"input_tokens":{"direct_evidence":"usage://turn-001/input","provenance":"OBSERVED","value":100},"output_tokens":{"direct_evidence":"usage://turn-001/output","provenance":"OBSERVED","value":20},"reasoning_output_tokens":{"direct_evidence":"usage://turn-001/reasoning","provenance":"OBSERVED","value":5},"total_tokens":{"direct_evidence":"usage://turn-001/total","provenance":"OBSERVED","value":125}},"schema_version":"r2"},"workflow_fingerprint":"0b7b19fac6fc44ebeca474990b3de1fd5701590fcc5bfe51fd099525d768de54","workflow_id":"golden-workflow"}
```

```text
observation_fingerprint = c14ce45d5d922dcf014e41f82f04aeae45b6488a60182393060a9e8c02a9661b
receipt_id = r6obs-a18831ebf7f7ae5d476280f3e480eb448a3d7a715f6d8695cf121fb0fae0f917
receipt_payload_fingerprint = bac47da1ec96c5eaa9e3da2241d0cd2e8486a55726e4f59e009ca8777dbe2b63
```

The receipt-ID canonical preimage for that fixture is exactly:

```json
{"action_id":"action-4a4a9fb15cb3622e809bf643f1935f3715c5a970e602475ed2f7f8b615a89634","attempt_id":"attempt-4a4a9fb15cb3622e809bf643f1935f3715c5a970e602475ed2f7f8b615a89634","execution_id":"execution-4a4a9fb15cb3622e809bf643f1935f3715c5a970e602475ed2f7f8b615a89634","observation_fingerprint":"c14ce45d5d922dcf014e41f82f04aeae45b6488a60182393060a9e8c02a9661b","request_fingerprint":"07200548a86a2f95702f6a8d2bc88eafe599bb6dc63c94b0068f0a9f62fadaa3","run_id":"run-001","workflow_fingerprint":"0b7b19fac6fc44ebeca474990b3de1fd5701590fcc5bfe51fd099525d768de54","workflow_id":"golden-workflow"}
```

Accepted `UsageRecord` has no public identity. Accordingly, the receipt's
normalized observation contains `UsageRecord | None`; there is no
caller-visible or domain `usage_id`, and no workflow, request, action,
observation, receipt, decision, or effect fingerprint includes a database row
identifier. When an auxiliary non-null record is persisted,
`SQLiteStateStore` derives the
storage-only accepted-R3 `usage_records.usage_id` value as:

```text
usage_row_key = r6usage-<U>
U = lowercase SHA-256 of canonical JSON containing exactly:
    action_id, observation_fingerprint
```

The canonical object keys are therefore emitted as
`{"action_id":...,"observation_fingerprint":...}`. This key is an adapter
implementation detail returned neither by the workflow domain nor by the R6
port and is not R6 effect authority. The store may insert or verify that
auxiliary R3 row; the authoritative usage snapshot is the canonical receipt.
A missing usage record is legal. A preexisting row with the derived key but
different run or canonical normalized payload fails closed, but no direct
`workflow_effects` FK to that weakly scoped row is permitted.

### Binding receipt, lease, and crash-window contract — Correction.3

```text
A. Crash before durable authorization commit
   -> transaction absent or rolled back
   -> no action or Attempt is authorized
   -> executor must not be called.

B. Crash after authorization commit and before AgentExecutor.start
   -> action exists with no receipt/effect (AUTHORIZED_NOT_OBSERVED)
   -> a later entry is replay/load, never a new invocation
   -> with a later valid recording lease, persist UNCERTAIN_EFFECT /
      NO_DURABLE_OBSERVATION_RECEIPT and FAILED_SAFE; without it, return
      unresolved AUTHORIZED_NOT_OBSERVED and stop without mutation.

C. get_result returns and the current fence commits the receipt
   -> receipt is immutable cross-owner authority
   -> if the fence later expires before effect commit, stale effect write fails
   -> a later valid owner loads the receipt and finalizes its exact mapped
      effect without raw observation input or provider invocation.

D. get_result returns but fence expires or process crashes before receipt commit
   -> stale receipt write fails or no receipt exists
   -> in-memory/raw observation is not transferable
   -> a later valid owner may record only UNCERTAIN_EFFECT /
      NO_DURABLE_OBSERVATION_RECEIPT and FAILED_SAFE
   -> no provider replay or history lookup.

E. Receipt contains PENDING, RUNNING, or UNKNOWN
   -> finalize only UNCERTAIN_EFFECT / NONTERMINAL_OBSERVATION / FAILED_SAFE
   -> no second get_result, polling, retry, or resume.

F. Crash after receipt and effect commit
   -> exact terminal effect, decision, transition, checkpoint, version, and
      receipt/idempotency records reload together
   -> consume durable evidence; never invoke again.
```

This deliberately sacrifices automatic replay in window B to remain safe in
window C. StateStore idempotency makes only the local durable transaction
idempotent; it does not and must not claim exactly-once provider semantics.

Receipt creation requires the current active unexpired R3 lease for
`workflow-run:<workflow_id>:<run_id>`, including exact lease ID, holder,
resource, run, and fencing token. Expiry before receipt commit rejects the
write; the in-memory observation gains no transferable authority. Expiry after
receipt commit does not invalidate the immutable receipt, but still rejects a
stale effect mutation. A later current owner supplies only workflow ID, run ID,
action ID, expected control state/version, its current valid recording lease/
fence, finalization decision inputs, and optionally the expected receipt ID.
The engine/store loads the receipt. It accepts no new observation payload.

The exact no-receipt uncertainty evidence projection is
`{"action_id":<action_id>,"reason":"NO_DURABLE_OBSERVATION_RECEIPT"}`;
its lowercase SHA-256 is the effect decision's required evidence fingerprint.
Any identity, fingerprint, mapping, prior-effect, resource, fence, or version
contradiction fails closed.

## StateStore atomic authority — Correction.1 as amended by Correction.3

R6 extends `StateStore` with exactly these port method names and semantic
contracts:

1. `save_workflow_definition_v1`
2. `initialize_workflow_control_v1`
3. `commit_workflow_decision_v1`
4. `authorize_workflow_action_v1`
5. `record_workflow_observation_receipt_v1`
6. `record_workflow_effect_v1`
7. `load_workflow_control_v1`
8. `load_workflow_observation_receipt_v1`
9. `load_workflow_action_effect_v1`

`save_workflow_definition_v1` accepts the validated typed definition, exact
canonical bytes, and fingerprint. It atomically verifies their equality,
inserts the accepted R3 `workflows` and `phases` rows together with the complete
immutable V1 graph when absent, returns the existing complete record for exact
replay, and rejects a partial preexisting base, mismatched phase kind, or the
same workflow ID with any different bytes or fingerprint. No definition/base
row survives a failed save transaction.

`initialize_workflow_control_v1` accepts workflow/run/fingerprint/entry phase
and requires an already durable, exactly matching accepted R3 `Run` whose
`state_version` is `0` and whose operational state is `PENDING`. A
missing/mismatched run is an unpersisted prerequisite
and fails without mutation. The operation atomically
creates exactly one control binding in lifecycle `READY`. Exact replay returns
the original; any conflict fails closed.

### Single control version

The sole R6 control version is the accepted `runs.state_version`; no second
mutable version counter is introduced. It starts at `0`. Every successful new
`commit_workflow_decision_v1`, `authorize_workflow_action_v1`, or
`record_workflow_effect_v1` compares caller `expected_control_version=N`,
updates the run using `WHERE state_version=N`, and commits `N+1` in the same
transaction as its decision/transition/checkpoint and other semantic rows. The
same update sets `runs.state` to the exact mapped R3 operational state; the R6
control row receives the exact phase/lifecycle mutation defined above.
Zero updated rows means stale-version conflict. Exact idempotent replay returns
the previously committed `N+1` and never increments again. Inside each
transaction, the store first resolves the supplied idempotency key: a complete
exact record returns replay even though durable version has advanced; a
conflicting record fails; only an unseen key proceeds to the expected-version
compare-and-update. Thus ordinary stale work cannot bypass concurrency and a
legitimate replay is not misclassified as stale.

`record_workflow_observation_receipt_v1` is deliberately excluded from that
list: it persists immutable capture evidence and does not mutate workflow
phase, lifecycle, operational state, or `runs.state_version`. Authorization has
already committed `N+1`; receipt creation retains that exact version; effect
finalization is the next control mutation and commits `N+2`. Receipt persistence
therefore introduces no second version authority.

`commit_workflow_decision_v1` is limited to non-provider decisions: `ADVANCE`
from an explicit `NOT_APPLICABLE` skip or an accepted external `PASS` fact;
`WAIT`; `EXTERNAL_FACT_WAIT`; or a fact-authorized `HUMAN_STOP`, `FAILED_SAFE`,
`READY_TO_PUBLISH`, or `COMPLETED`. `CORRECTION_REQUIRED` is never standalone
because its authority requires the AgentResult loaded from the authoritative
observation receipt and finalized by `record_workflow_effect_v1`. Inputs are the exact
workflow/run/fingerprint, expected version, current/target phase or terminal,
gate, nullable `definition_transition_id`, typed pure-policy decision, evidence
fingerprint, required accepted R3 `StateTransition` and `Checkpoint`, and the
deterministic idempotency identity. `WAIT`/`EXTERNAL_FACT_WAIT` use no
definition transition and retain the same phase; graph movement or a terminal
decision requires its exact definition transition. It validates durable
identities and graph references, then atomically inserts decision/state-
transition/checkpoint/idempotency evidence, updates control state, and
increments the single version. It never evaluates whether a gate should pass
and never calls external code.

Standalone decision idempotency is exactly:

```text
idempotency_key = r6-decision:<decision_id>
operation_kind = workflow_decision_commit_v1
canonical_operation_identity = <decision_id>
payload_fingerprint = SHA-256(canonical decision payload)
```

The decision payload contains every semantic input above, replacing the R3
transition/checkpoint objects with their stable IDs and excluding only the
four idempotency fields and Clock-derived creation timestamp. Exact replay
returns the original decision/version with `replayed=true`; any changed
kind/identity/payload fails closed.

### Authorize action operation

`authorize_workflow_action_v1` accepts exactly:

```text
workflow_id, workflow_fingerprint, run_id,
expected_control_version, phase_id, gate_id,
WorkflowDecisionV1(kind=AUTHORIZE_PHASE_ACTION),
action_kind=AGENT_EXECUTION, request_fingerprint,
action_id, Attempt(attempt_id, run_id, durable_phase_key, ordinal=0), execution_id,
authorization_lease_id, authorization_lease_holder,
protected_resource_key, authorization_fencing_token,
StateTransition, Checkpoint,
idempotency_key, operation_kind, canonical_operation_identity,
canonical authorization payload
```

An authorization decision has `definition_transition_id=null`: movement into
the current phase was already durably committed by the preceding decision, or
the phase is the initialized entry. Its accepted R3 `StateTransition` has event
`AUTHORIZE_PHASE_ACTION` and the exact operational transition to `RUNNING`
defined above.

The SQLite implementation uses one `BEGIN IMMEDIATE` transaction to validate
the durable run/control/fingerprint/current phase/gate and expected version;
validate the active unexpired R3 lease and exact fencing token; insert or replay
the decision, transition, checkpoint, action authorization, and R3 Attempt;
insert the R3 idempotency record; update control lifecycle to
`ACTION_AUTHORIZED`; increment `runs.state_version`; and commit all or nothing.
No `AgentExecutor` call occurs inside the transaction.

The authorization idempotency contract is exact:

```text
idempotency_key = r6-authorize:<action_id>
operation_kind = workflow_action_authorization_v1
canonical_operation_identity = <action_id>
payload_fingerprint = SHA-256(canonical authorization payload)
```

The authorization payload contains every semantic operation input above except
the idempotency key, operation kind, canonical-operation identity, its own
canonical payload bytes, transition/checkpoint object serialization, and
clock-derived `authorized_at`; it instead contains the stable transition and
checkpoint IDs. The timestamp is generated once from injected `Clock` and is
not identity authority.

New success returns one durable `WorkflowAuthorizationResultV1` containing
`decision_id`, `action_id`, `attempt_id`, `execution_id`, `request_fingerprint`,
`committed_control_version=N+1`, state `AUTHORIZED_NOT_OBSERVED`, and
`replayed=false`. Same key/kind/identity/payload returns that exact original
record with `replayed=true`, allocates no new Attempt, increments no version,
and authorizes no executor call. Any changed identity or payload, stale
version, stale fence, expired lease, wrong owner/run/resource, or conflicting
existing action fails closed.

### Mandatory fencing

Every `AGENT_EXECUTION` authorization is fencing-protected. The protected key
is exactly:

```text
workflow-run:<workflow_id>:<run_id>
```

Both identifiers are already canonical, so no escaping or normalization is
performed. R6 consumes a lease acquired through existing R3 authority; it does
not allocate fencing tokens. Authorization atomically requires the current
active, unexpired lease for that key to belong to the same run and supplied
holder and to carry the supplied token. Missing, released, expired, wrong-run,
or stale tokens are rejected before any provider effect. Pure
`commit_workflow_decision_v1` operations that authorize no external effect are
fencing-exempt, though they remain versioned and idempotent.

### Record observation receipt operation — Correction.3

`record_workflow_observation_receipt_v1` accepts exactly:

```text
workflow_id, workflow_fingerprint, run_id,
action_id, attempt_id, execution_id, request_fingerprint,
normalized canonical observation payload, observation_fingerprint, receipt_id,
capture_lease_id, capture_holder_id,
protected_resource_key, capture_fencing_token,
idempotency_key, operation_kind, canonical_operation_identity,
canonical receipt payload
```

The operation is available only to the same R6 execution path that called the
sole `get_result`; no public/cross-owner raw-observation write surface exists.
It uses one `BEGIN IMMEDIATE` transaction to validate the durable action and
its workflow/fingerprint/run/attempt/execution/request binding; lifecycle
`ACTION_AUTHORIZED`; the canonical observation shape, status/terminal matrix,
fingerprint and deterministic receipt ID; and the current active unexpired R3
lease whose lease ID, holder, protected key and fencing token equal the action's
durable authorization-fence evidence. A later fence cannot create a receipt
for an observation retained across expiry. It
then resolves idempotency, inserts the immutable receipt and R3 idempotency row,
and commits all or nothing. It calls no provider, evaluates no workflow policy,
creates no transition/checkpoint/decision, and changes no control version.

Receipt cardinality is `UNIQUE(action_id)`. Exact replay returns the original
`WorkflowObservationReceiptResultV1(replayed=true)` and changes no state. A
different receipt or observation for the same action fails closed. Receipt
idempotency is exactly:

```text
idempotency_key = r6-observation:<action_id>
operation_kind = r6-observation
canonical_operation_identity = <action_id>
payload_fingerprint = SHA-256(exact canonical receipt payload)
```

The new R3 idempotency row uses the action-authorization transition/checkpoint
IDs and `authorization_committed_control_version` as its existing required
durable anchors; it does not create a new transition/checkpoint or claim a new
resulting state version. Fence generation, lease/holder identity, capture time,
and replay status are excluded from the semantic receipt payload.

### Record effect operation

`record_workflow_effect_v1` accepts exactly:

```text
workflow_id, workflow_fingerprint, run_id,
expected_control_version, action_id, request_fingerprint, attempt_id, execution_id,
effect_id, WorkflowEffectState, WorkflowUncertaintyReason or null,
receipt_id or null,
typed pure-policy WorkflowDecisionV1,
target phase or terminal outcome, nullable definition_transition_id,
StateTransition, Checkpoint,
recording_lease_id, recording_lease_holder,
protected_resource_key, recording_fencing_token,
idempotency_key, operation_kind, canonical_operation_identity,
canonical effect payload
```

Every new effect commit requires the supplied recording lease/token to be the
currently active, unexpired authority for the action's exact protected
resource. It may be the still-current authorization token or a later token used
solely for delayed persistence. The caller supplies no observation, result,
failure, session, usage, or artifact payload. When `receipt_id` is non-null,
the store loads the receipt and its canonical observation and validates its
exact workflow/fingerprint/run/action/attempt/execution/request/observation
binding before pure policy maps it to the only compatible effect. When
`receipt_id` is null, only the exact no-receipt uncertainty path is legal. The
operation uses one `BEGIN IMMEDIATE` transaction to
validate the run/control/fingerprint/version; exact action-attempt-execution
and request-fingerprint binding; action still has no effect;
current lease/fence; and pure-policy decision references. It then atomically
inserts one effect, decision, transition, checkpoint, R3 idempotency record and
receipt link when required; updates the current phase/lifecycle only as directed by that
already-computed policy decision; increments `runs.state_version`; and commits
all or nothing. `UNCERTAIN_EFFECT` always records `FAILED_SAFE` and cannot
advance through a semantic gate.

On re-entry, a newly acquired later valid token may use this operation with the
loaded durable receipt, or with no receipt only to record
`UNCERTAIN_EFFECT / NO_DURABLE_OBSERVATION_RECEIPT`. It may not supply raw
observation data, resume, query provider history, or invoke the executor. Both
the original authorization/capture tokens when present and the actual recording
token remain durable evidence.

Effect idempotency is exact:

```text
idempotency_key = r6-effect:<action_id>
operation_kind = workflow_effect_record_v1
canonical_operation_identity = effect:<action_id>
payload_fingerprint = SHA-256(canonical effect payload)
```

The effect payload contains exactly `schema_version=workflow-effect-v1`,
`workflow_id`, `workflow_fingerprint`, `run_id`, `action_id`, `attempt_id`,
`execution_id`, `request_fingerprint`, nullable `receipt_id`, nullable
`observation_fingerprint`, `effect_state`, nullable `uncertainty_reason`,
`decision_id`, `decision_record_kind`, `decision_kind`, nullable
`definition_transition_id`, nullable `target_phase_id`, nullable
`terminal_outcome`, and `evidence_fingerprint`. It excludes idempotency fields,
its own canonical payload bytes, object serializations,
clock-derived `recorded_at`, and writer-authorization fields
`recording_lease_id`, `recording_lease_holder`, `protected_resource_key`, and
`recording_fencing_token`; stable IDs replace the state-transition/checkpoint
objects. The protected resource is already fixed by the durable action. A fence
validates who may perform a new commit and is durable audit evidence, but is not
semantic effect identity. Thus a later current fence may commit the same
previously uncommitted canonical effect. After commit, same identity and
payload returns the original
`WorkflowEffectResultV1` with `replayed=true`, no duplicate evidence or version
increment; this replay is read-only and does not require the original fence to
remain current. Any changed semantic payload—including success followed by failure—is an
`IdempotencyConflict`/effect conflict and changes nothing.

`effect_id` remains the already-frozen one-row identity derived only from
`action_id`. The effect payload fingerprint, not `effect_id`, is the complete
semantic effect fingerprint. It binds receipt identity when present, exact
effect state, uncertainty reason, observation/evidence fingerprint and workflow
decision fields, while excluding fence generation, writer identity and
database-local auxiliary evidence IDs. Therefore no conflicting effect can
reuse the same semantic payload identity.

`load_workflow_control_v1(workflow_id, run_id)` requires both identities,
returns the immutable definition/fingerprint, current phase/lifecycle, and
`runs.state_version` only when the run exists and belongs to that exact
workflow. A missing run returns not found; an existing run paired with a
different workflow is an identity mismatch and fails closed.
`load_workflow_observation_receipt_v1(workflow_id, run_id, action_id)` requires
all three identities and returns none or exactly one immutable receipt. A
missing action/receipt returns not found; an action or receipt belonging to a
different workflow/run is an identity mismatch and fails closed. No global
receipt lookup exists.
`load_workflow_action_effect_v1(workflow_id, run_id, action_id)` requires all
three identities and returns the authorization, bound
attempt/execution, zero-or-one receipt, zero-or-one effect, and all three
idempotency records. On R6
re-entry, an action with no effect is never executable. If a newly acquired
valid recording lease is supplied, the engine may call
`record_workflow_effect_v1` once with the loaded receipt, or when no receipt
exists only with canonical `UNCERTAIN_EFFECT /
NO_DURABLE_OBSERVATION_RECEIPT` and `FAILED_SAFE`; without that lease it must
return the unresolved durable
`AUTHORIZED_NOT_OBSERVED` record, make no mutation, and stop. Exact terminal
evidence is consumed only from the receipt without duplicate execution. No
in-memory or caller-supplied observation can override these reads.

StateStore validates durable invariants only. It does not decide gate outcome,
correction, retry, model, routing, or recovery safety. `workflow_policy.py` is
pure; `workflow_engine.py` coordinates policy, these operations, and the
injected executor.

## SQLite relational authority — Correction.1 as amended by Correction.3

`002_workflow_engine.sql` is the only migration. It is append-only and must not
alter `001_initial.sql`. It adds the following nine tables and three supporting
unique indexes; no R3 workflow/run/phase/attempt/checkpoint/session/usage/
failure/artifact/lease/idempotency table is duplicated.

### Workflow-local and R3-durable namespaces

Workflow Definition V1 `phase_id` is local to one immutable workflow
definition. Accepted R3 `phases.phase_id` is instead a global primary key. R6
therefore derives the globally unique R3 value deterministically:

```text
durable_phase_key = wfphase-<P>
P = lowercase SHA-256 of canonical JSON containing exactly:
    workflow_id, workflow_fingerprint, local_phase_id
```

Canonical key ordering emits
`{"local_phase_id":...,"workflow_fingerprint":...,"workflow_id":...}`.
For the binding workflow golden and local phase `audit`, the canonical bytes
and required key are:

```json
{"local_phase_id":"audit","workflow_fingerprint":"0b7b19fac6fc44ebeca474990b3de1fd5701590fcc5bfe51fd099525d768de54","workflow_id":"golden-workflow"}
```

```text
durable_phase_key = wfphase-b86c05eff72d8bec22b2cc4ad704dc3f49e78910ddd1a3dc95247a355919635e
```

Every accepted R3 `WorkflowPhase.phase_id`, `Attempt.phase_id`,
`StateTransition.phase_id`, and `Checkpoint.phase_id` created by R6 uses this
durable key. R6 domain decisions, graph edges, prepared specs, and control state
retain the local `phase_id`. Two workflows may therefore both declare `audit`
without collision; equal workflow definition/local phase produces the same
durable key. Accepted R3 has no gate table or global gate primary key:
`transitions.gate_reference` is already qualified by its run/workflow record.
R6 gate IDs remain workflow-definition-scoped local IDs, so no second durable
gate-key namespace is needed.

### Definition tables

1. `workflow_definitions` keeps `workflow_id TEXT PRIMARY KEY`, exact schema,
   profile, entry local phase, false auto-publish, correction bound, canonical
   bytes, and lowercase-hex fingerprint constraints from Correction.1. It has
   `UNIQUE(workflow_id,workflow_fingerprint)` and a deferred FK
   `(workflow_id,workflow_fingerprint,entry_phase_id)` to the phase-definition
   candidate key.
2. `workflow_gate_definitions` has `workflow_id`, `workflow_fingerprint`, and
   local `gate_id` as its composite PK; the exact gate kind/applicability CHECKs;
   and FK `(workflow_id,workflow_fingerprint)` to the immutable definition.
3. `workflow_phase_definitions` has `workflow_id`, `workflow_fingerprint`, local
   `phase_id`, global `durable_phase_key`, local `gate_id`, role, and action
   mode. Its PK is `(workflow_id,workflow_fingerprint,phase_id)`; it has
   `UNIQUE(durable_phase_key)`, `UNIQUE(workflow_id,durable_phase_key)`, and
   `UNIQUE(workflow_id,workflow_fingerprint,phase_id,durable_phase_key)`,
   `UNIQUE(workflow_id,workflow_fingerprint,phase_id,gate_id)`, and
   `UNIQUE(workflow_id,workflow_fingerprint,phase_id,gate_id,durable_phase_key)`.
   FKs bind the definition, exact scoped gate, and
   `(workflow_id,durable_phase_key)` to accepted
   `phases(workflow_id,phase_id)`. The store verifies the exact phase-key
   derivation, accepted R3 kind, and role/action compatibility before commit.
4. `workflow_transition_definitions` includes workflow ID/fingerprint,
   transition ID, source local phase, gate, trigger, nullable target local phase,
   and nullable terminal. Its PK is
   `(workflow_id,workflow_fingerprint,transition_id)`; scoped composite FKs bind
   the source phase/gate and target phase; and UNIQUE
   `(workflow_id,workflow_fingerprint,from_phase_id,trigger)` plus CHECKs enforce
   the exhaustive transition matrix above.

Graph reachability, DAG topology, complete outgoing-edge cardinality, exact
correction count, transition matrix, durable-key derivation, and role/kind
compatibility are validated against the canonical definition in the same
transaction. Reload plus fingerprint verification occurs before commit.

### Control, decision, authorization, receipt, and effect tables

5. `workflow_controls` has `run_id` as PK, workflow ID/fingerprint, current
   local phase ID, current durable phase key, and exact lifecycle CHECK. FKs
   bind `(workflow_id,run_id)`, the definition, and the complete scoped phase
   candidate key. It has no version column: accepted `runs.state_version`
   remains the sole mutable control-version authority.

6. `workflow_decisions` includes the existing exact decision fields plus
   `decision_record_kind` and `durable_phase_key`. The exact record-kind
   vocabulary is `STANDALONE_POLICY`, `ACTION_AUTHORIZATION`, and
   `EFFECT_FINALIZATION`. Its CHECK matrix is binding:

   - `ACTION_AUTHORIZATION` requires decision kind
     `AUTHORIZE_PHASE_ACTION`, non-null action ID, and no definition movement;
   - `EFFECT_FINALIZATION` requires a non-null action ID and permits exactly
     `ADVANCE`, `CORRECTION_REQUIRED`, `WAIT`, `HUMAN_STOP`, `FAILED_SAFE`,
     `READY_TO_PUBLISH`, or `COMPLETED`;
   - `STANDALONE_POLICY` requires null action ID and permits exactly `ADVANCE`,
     `WAIT`, `EXTERNAL_FACT_WAIT`, `HUMAN_STOP`, `FAILED_SAFE`,
     `READY_TO_PUBLISH`, or `COMPLETED`.

   Decision kind, nullable transition/target/terminal shape, fingerprint,
   state-transition/checkpoint, and idempotency constraints remain exact.
   `control_version_after=control_version_before+1` and UNIQUE
   `(run_id,control_version_before)` remain mandatory. In addition to its PK,
   the table declares this exact downstream parent candidate key:

   ```text
   UNIQUE(decision_id, decision_record_kind, decision_kind,
          workflow_id, workflow_fingerprint, run_id,
          phase_id, durable_phase_key, gate_id,
          control_version_before, control_version_after, action_id)
   ```

   Composite FKs bind its workflow/run, scoped local/durable phase and gate,
   definition transition/target, R3 transition/checkpoint, and accepted
   idempotency tuple. Its non-null `action_id` also has a deferred FK to
   `workflow_actions(action_id)`; downstream action/effect composite FKs provide
   the full reverse scope proof.

7. `workflow_actions` stores `authorization_decision_id`, checked literal
   `authorization_decision_record_kind='ACTION_AUTHORIZATION'`, checked literal
   `authorization_decision_kind='AUTHORIZE_PHASE_ACTION'`, exact
   workflow/fingerprint/run/local phase/durable phase/gate scope,
   `authorization_control_version`, and
   `authorization_committed_control_version` with CHECK
   `committed=authorization_control_version+1`. Its composite FK uses all those
   columns plus `action_id` in the exact order of the decision candidate key.
   This relationally proves decision kind, scope, and version equality rather
   than relying on a simple decision-ID FK.

   ```text
   FOREIGN KEY(
     authorization_decision_id,
     authorization_decision_record_kind, authorization_decision_kind,
     workflow_id, workflow_fingerprint, run_id,
     phase_id, durable_phase_key, gate_id,
     authorization_control_version, authorization_committed_control_version,
     action_id
   ) REFERENCES workflow_decisions(
     decision_id, decision_record_kind, decision_kind,
     workflow_id, workflow_fingerprint, run_id,
     phase_id, durable_phase_key, gate_id,
     control_version_before, control_version_after, action_id
   )
   ```

   The remaining action columns and checks are action kind, request
   fingerprint, unique attempt ID, ordinal exactly zero, unique execution ID,
   authorization lease/holder/resource/token, idempotency tuple, and timestamp.
   It declares the exact action candidate key:

   ```text
   UNIQUE(action_id, workflow_id, workflow_fingerprint, run_id,
          phase_id, durable_phase_key, gate_id, request_fingerprint,
          attempt_id, execution_id, authorization_control_version,
          authorization_committed_control_version)
   ```

   FK `(run_id,durable_phase_key,attempt_id,attempt_ordinal)` targets the exact
   accepted R3 attempt index. UNIQUE `(run_id,authorization_control_version)`
   prevents two authorizations at one control version. The authorization
   decision's deferred action FK and the action's composite decision FK are
   `DEFERRABLE INITIALLY DEFERRED` for their atomic one-to-one insertion.

   Correction.3 additionally declares this exact all-non-null receipt parent
   candidate key:

   ```text
   UNIQUE(workflow_id, workflow_fingerprint, run_id, action_id,
          attempt_id, execution_id, request_fingerprint,
          authorization_lease_id, authorization_lease_holder,
          protected_resource_key, authorization_fencing_token)
   ```

8. `workflow_observation_receipts` is the sole durable cross-owner normalized
   observation authority. Its exact SQLite columns and nullability are:

   ```text
   receipt_id                    TEXT PRIMARY KEY NOT NULL
   workflow_id                   TEXT NOT NULL
   workflow_fingerprint          TEXT NOT NULL
   run_id                        TEXT NOT NULL
   action_id                     TEXT NOT NULL
   attempt_id                    TEXT NOT NULL
   execution_id                  TEXT NOT NULL
   request_fingerprint           TEXT NOT NULL
   observation_status            TEXT NOT NULL
   observation_terminal          INTEGER NOT NULL
   observation_fingerprint       TEXT NOT NULL
   canonical_observation_json    BLOB NOT NULL
   capture_lease_id              TEXT NOT NULL
   capture_holder_id             TEXT NOT NULL
   capture_resource_key          TEXT NOT NULL
   capture_fencing_token         INTEGER NOT NULL
   idempotency_key               TEXT NOT NULL
   idempotency_operation_kind    TEXT NOT NULL
   canonical_operation_identity  TEXT NOT NULL
   payload_fingerprint           TEXT NOT NULL
   created_at                    TEXT NOT NULL
   ```

   Exact CHECKs restrict `observation_status` to the six accepted
   `ExecutionStatus` values; require `observation_terminal IN (0,1)` and exact
   terminal/status correspondence; require positive capture fencing token;
   require lowercase 64-hex workflow/request/observation/payload fingerprints;
   require `idempotency_operation_kind='r6-observation'`; require
   `canonical_operation_identity=action_id`; and require
   `idempotency_key='r6-observation:' || action_id`. StateStore recomputes and
   byte-compares `canonical_observation_json`, observation fingerprint,
   deterministic receipt ID, and receipt payload fingerprint inside the same
   transaction.

   It declares `UNIQUE(action_id)` and the exact all-non-null effect parent key:

   ```text
   UNIQUE(receipt_id, workflow_id, workflow_fingerprint, run_id,
          action_id, attempt_id, execution_id, request_fingerprint,
          observation_fingerprint)
   ```

   Its exact receipt-to-action FK is:

   ```text
   FOREIGN KEY(
     workflow_id, workflow_fingerprint, run_id, action_id,
     attempt_id, execution_id, request_fingerprint,
     capture_lease_id, capture_holder_id,
     capture_resource_key, capture_fencing_token
   ) REFERENCES workflow_actions(
     workflow_id, workflow_fingerprint, run_id, action_id,
     attempt_id, execution_id, request_fingerprint,
     authorization_lease_id, authorization_lease_holder,
     protected_resource_key, authorization_fencing_token
   )
   ```

   This proves the receipt uses the exact action, attempt, execution, request,
   protected resource, and original authorization fence; a later fencing
   generation cannot turn retained memory into a receipt. A second composite FK
   `(capture_lease_id,run_id,capture_holder_id,capture_resource_key,
   capture_fencing_token)` targets the existing exact accepted `leases`
   candidate key. Runtime validation additionally proves that lease is current,
   active and unexpired at insert time. Its exact four-column idempotency FK
   targets the accepted R3 idempotency candidate key.

9. `workflow_effects` stores effect ID, unique action ID, exact action scope
   including workflow/fingerprint/run/local phase/durable phase/gate/request
   fingerprint/attempt/execution and both authorization versions, nullable
   receipt ID, terminal effect state, nullable uncertainty reason, nullable
   observation status/fingerprint, recording fence evidence, effect idempotency
   tuple, decision binding, and timestamp. It has no AgentResult bytes, session,
   usage row key, failure ID, artifact ID, or direct FK to those weakly scoped
   R3 tables. Its full composite action FK targets the action candidate key
   above.

   It separately stores `effect_decision_id`, checked literal
   `effect_decision_record_kind='EFFECT_FINALIZATION'`, checked
   `effect_decision_kind` in the exact effect-finalization vocabulary,
   `effect_control_version`, and `effect_committed_control_version` with CHECK
   `committed=effect_control_version+1` and CHECK
   `effect_control_version=authorization_committed_control_version`. A
   composite FK containing the exact
   action scope, both versions, action ID, record kind, and decision kind
   targets the decision candidate key. Thus an effect cannot cite an
   authorization/standalone decision or a decision from another run, phase,
   gate, action, or version. `effect_decision_id` is UNIQUE. Exact table CHECKs
   require `OBSERVED_FAILED`, `OBSERVED_INTERRUPTED`, and `UNCERTAIN_EFFECT` to
   use effect decision kind `FAILED_SAFE`; only `OBSERVED_SUCCEEDED` may use the
   remaining effect-finalization kinds according to the valid AgentResult loaded
   from its receipt.

   ```text
   FOREIGN KEY(
     effect_decision_id,
     effect_decision_record_kind, effect_decision_kind,
     workflow_id, workflow_fingerprint, run_id,
     phase_id, durable_phase_key, gate_id,
     effect_control_version, effect_committed_control_version, action_id
   ) REFERENCES workflow_decisions(
     decision_id, decision_record_kind, decision_kind,
     workflow_id, workflow_fingerprint, run_id,
     phase_id, durable_phase_key, gate_id,
     control_version_before, control_version_after, action_id
   )
   ```

   Receipt-backed effects declare this exact composite FK to the receipt parent
   key, including request fingerprint so no scope component is merely implied:

   ```text
   FOREIGN KEY(
     receipt_id, workflow_id, workflow_fingerprint, run_id,
     action_id, attempt_id, execution_id, request_fingerprint,
     observation_fingerprint
   ) REFERENCES workflow_observation_receipts(
     receipt_id, workflow_id, workflow_fingerprint, run_id,
     action_id, attempt_id, execution_id, request_fingerprint,
     observation_fingerprint
   )
   ```

   Exact CHECKs permit only these receipt/effect combinations:

   ```text
   OBSERVED_SUCCEEDED
     -> receipt_id NOT NULL / status SUCCEEDED /
        observation_fingerprint NOT NULL / uncertainty_reason NULL
   OBSERVED_FAILED
     -> receipt_id NOT NULL / status FAILED /
        observation_fingerprint NOT NULL / uncertainty_reason NULL
   OBSERVED_INTERRUPTED
     -> receipt_id NOT NULL / status INTERRUPTED /
        observation_fingerprint NOT NULL / uncertainty_reason NULL
   UNCERTAIN_EFFECT / NONTERMINAL_OBSERVATION
     -> receipt_id NOT NULL / status PENDING|RUNNING|UNKNOWN /
        observation_fingerprint NOT NULL
   UNCERTAIN_EFFECT / NO_DURABLE_OBSERVATION_RECEIPT
     -> receipt_id NULL / observation_status NULL /
        observation_fingerprint NULL
   ```

   For every non-`UNCERTAIN_EFFECT`, `uncertainty_reason` is null. For
   `UNCERTAIN_EFFECT`, it is exactly `NONTERMINAL_OBSERVATION` or
   `NO_DURABLE_OBSERVATION_RECEIPT`; the receipt/null combinations above admit
   no alternative. Caller-selected incompatible effect state, status, reason,
   receipt, or decision kind fails before or at the database constraint.

The final `002_workflow_engine.sql` table set is exactly nine tables:

```text
workflow_definitions
workflow_gate_definitions
workflow_phase_definitions
workflow_transition_definitions
workflow_controls
workflow_decisions
workflow_actions
workflow_observation_receipts
workflow_effects
```

The prior `workflow_effect_artifacts` proposal is removed and replaced by
`workflow_observation_receipts`; both may not coexist as competing semantic
effect-evidence authority. Accepted R3 `usage_records`, `agent_sessions`,
`failures`, and `artifacts` remain auxiliary operational evidence only. Their
normalized provider-neutral values may occur inside the receipt snapshot, but
no direct same-run R3 evidence FK can authorize or override an R6 effect.

The migration creates exactly three parent-candidate unique indexes on accepted
tables, in the stated order required by SQLite composite FKs:
`attempts(run_id,phase_id,attempt_id,ordinal)`,
`leases(lease_id,run_id,holder,protected_resource_key,fencing_token)`, and
`idempotency_records(idempotency_key,operation_kind,
canonical_operation_identity,payload_fingerprint)`. All new parent tuples are
declared as exact PK/UNIQUE candidates in their referenced column order. All
FKs use `ON DELETE RESTRICT`; no destructive migration, cascade, table rewrite,
or history rewrite is authorized.

Absence of an effect row remains exactly `AUTHORIZED_NOT_OBSERVED`; unique
action ID enforces zero-or-one effect. Direct negative SQL/StateStore tests must
prove two workflows' local `audit` phases receive distinct durable keys and
reject forced reuse of the other workflow's key, cross-run/cross-phase/cross-gate
authorization or effect decisions, wrong decision record/kind, mismatched
control versions, duplicate attempt or execution, and duplicate/conflicting
effect. Correction.3 additionally requires direct rejection of: receipt run A
bound to action run B; receipt action A bound to attempt B, execution B, or a
different request fingerprint; a second different receipt for one action;
effect action/attempt/run A referencing receipt B; observed effect without a
receipt; non-uncertain effect with an uncertainty reason; no-receipt uncertainty
with `NONTERMINAL_OBSERVATION`; and receipt-backed uncertainty with
`NO_DURABLE_OBSERVATION_RECEIPT`.

Application/port negative tests must prove that raw cross-owner observation
submission is absent or rejected, an internally consistent fabricated
observation cannot finalize an observed effect, a fake/foreign receipt ID fails
closed, and foreign same-run R3 usage/session/failure/artifact evidence cannot
authorize or alter an R6 effect.

Standalone-decision, authorization, receipt, and effect transactions reuse accepted
`idempotency_records` with the exact kinds, canonical identities, and payload
fingerprints defined above. Decision, authorization, and effect transactions
atomically bind their R3 transition/checkpoint, idempotency record, R6 rows, and
optimistic run-version update. Receipt capture atomically binds its immutable
row and idempotency record to the already-committed authorization anchors and
performs no run update. Exact replay never increments; any changed identity or
semantic payload conflicts.

R3 migration authority is unchanged: raw-file-byte lowercase SHA-256 before
decode, ordered `002_workflow_engine`, atomic migration application, recorded
checksum, exact known prefix, fail-closed drift/future migration, and no marker
after rollback. First open applies `002`; second open does not reapply it; close
and reopen reconstruct definition/fingerprint, control state/version,
decisions, action/attempt/execution binding, zero-or-one receipt, zero-or-one
effect, authoritative receipt link, auxiliary evidence snapshots, and all three
idempotency records identically. Once the receipt commits, reopen requires no
in-memory custody. No R6 authority may exist only in memory.

No change is authorized for `AgentExecutor`, `ExecutionRequest`,
`ExecutionObservation`, `AgentResult`, `FailureRecord`, `UsageRecord`,
`SessionReference`, `checkpoint.schema.json`, `agent-result.schema.json`,
`usage-record.schema.json`, the migration runner, or `001_initial.sql`. If a
future implementation cannot satisfy this contract within the exact allowlist,
it must stop with `R6_ALLOWLIST_AUTHORITY_GAP` rather than substitute a path.

## Exact future R6 implementation allowlist

The fresh independent handoff audit passed and the separate competent action
approved and activated this exact target. An R6 executor may create or modify
exactly these 21 paths:

```text
tools/autopilot/README.md
tools/autopilot/config/runtime-contract.json
tools/autopilot/schemas/workflow.schema.json
tools/autopilot/src/feelingpilates_autopilot/domain/__init__.py
tools/autopilot/src/feelingpilates_autopilot/domain/workflow.py
tools/autopilot/src/feelingpilates_autopilot/ports/state_store.py
tools/autopilot/src/feelingpilates_autopilot/application/__init__.py
tools/autopilot/src/feelingpilates_autopilot/application/workflow_policy.py
tools/autopilot/src/feelingpilates_autopilot/application/workflow_engine.py
tools/autopilot/src/feelingpilates_autopilot/adapters/state/sqlite_store.py
tools/autopilot/migrations/002_workflow_engine.sql
tools/autopilot/tests/test_json_schemas.py
tools/autopilot/tests/test_runtime_contract.py
tools/autopilot/tests/test_sqlite_migrations.py
tools/autopilot/tests/test_port_boundaries.py
tools/autopilot/tests/test_workflow_contracts.py
tools/autopilot/tests/test_workflow_policy.py
tools/autopilot/tests/test_workflow_engine.py
tools/autopilot/tests/test_workflow_effect_evidence.py
tools/autopilot/tests/test_workflow_fail_closed.py
tools/autopilot/tests/test_workflow_r2_r5_regression.py
```

Allowlist rationale:

- `README.md` and `runtime-contract.json` must truthfully record the bounded R6
  engine, unchanged primary/fallback adapters, deferred neighbors, and
  `auto_publish=false`;
- the workflow schema and new domain module define the strict provider-neutral
  workflow/fact/decision/action/receipt/effect contract without modifying accepted
  agent/result/session/usage contracts;
- the application package separates pure policy from orchestration and durable
  side-effect sequencing;
- `StateStore`, the SQLite adapter, and append-only migration provide the
  exact durable boundary R6 requires without changing R3's `001` history; and
- the ten named test paths cover schema/runtime truth, migration and port
  deltas, pure policy, orchestration, side-effect evidence, fail-closed cases,
  and accepted R2–R5 regression.

No `pyproject.toml` change is required: the existing package discovery includes
the new application package, R6 adds no dependency, and normal tests remain
stdlib-only. No executor adapter, result parser, existing migration, product,
F2E, Git, or orchestration-protocol document is in scope.

```text
ALLOWLIST_COUNT: 21
ALLOWLIST_EXACT: PASS
ALLOWLIST_SUFFICIENT: PASS
ALLOWLIST_MINIMAL: PASS
WILDCARDS: NONE
KNOWN_PATH_GAP: NONE
```

If implementation requires any different, renamed, additional, or substituted
path, it must stop for explicit handoff correction and fresh re-audit.

## Future deterministic test authority

Normal R6 tests must be offline, deterministic, stdlib-only, and
non-destructive. They use fake `AgentExecutor` and `Repository` implementations,
a deterministic `Clock`, and a real temporary `SQLiteStateStore`. They must not
use a live provider, credentials, network, Git mutation, publication, product
code, or F2E.

Required categories are exact:

- **Happy path:** explicit non-applicable skip; one authorized phase; durable
  pre-effect commit before fake executor invocation; valid `PASS`; declared
  next phase; final completion; manual publication wait when applicable.
- **Invalid input:** malformed/duplicate workflow nodes; undeclared target;
  workflow/run/phase/role/gate/attempt/execution mismatch; stale version;
  invalid bounds; invalid request fingerprint; `auto_publish=true`.
- **Authority boundary:** injected executor only; no adapter import; no model or
  fallback choice; prepared instructions remain data; no Git/publication,
  context compilation, SQL, or supervisor behavior.
- **Gate policy:** `NOT_APPLICABLE != PENDING != PASS`; future gates remain
  pending; P0 and human flag stop; correctable P1 reaches only its declared
  correction phase; ambiguous/non-correctable P1 and absence of a remaining
  declared correction edge stop.
- **Transition matrix:** `PASS -> phase` and condition-valid
  `PASS -> READY_TO_PUBLISH` are valid; `PASS -> HUMAN_STOP` is invalid;
  `CORRECTION_REQUIRED -> distinct correction phase` is valid and
  `CORRECTION_REQUIRED -> COMPLETED` invalid; `HUMAN_STOP -> HUMAN_STOP` is
  valid and `HUMAN_STOP -> phase` invalid; `FAILED_SAFE -> FAILED_SAFE` is
  valid and `FAILED_SAFE -> COMPLETED` invalid.
- **Pre-semantic/effect policy:** `FAILED -> OBSERVED_FAILED -> FAILED_SAFE`,
  `INTERRUPTED -> OBSERVED_INTERRUPTED -> FAILED_SAFE`, and first/only
  `PENDING`, `RUNNING`, or `UNKNOWN -> UNCERTAIN_EFFECT -> FAILED_SAFE`.
  None creates a semantic gate result or P0/P1/P2; FAILED performs no retry,
  INTERRUPTED performs no resume/retry, and each nonterminal result performs no
  second `get_result` or `start`.
- **Fail closed:** unknown state/event/action, missing evidence, authority leak,
  conflicting idempotency, stale fencing, ambiguous recovery, and unsafe
  publication all produce no next external effect.
- **Deterministic goldens:** assert the exact workflow canonical bytes and
  `0b7b19fac6fc44ebeca474990b3de1fd5701590fcc5bfe51fd099525d768de54`
  fingerprint, the exact request canonical bytes and
  `07200548a86a2f95702f6a8d2bc88eafe599bb6dc63c94b0068f0a9f62fadaa3`
  fingerprint, and the exact full `action-`, `attempt-`, and `execution-`
  identities recorded above. It also recomputes the exact Correction.3
  canonical observation, observation fingerprint, receipt-ID preimage,
  receipt ID, and receipt-payload fingerprint recorded above. Reordered unordered
  arrays/object keys remain identical; a valid semantic change differs. A test
  that only checks hash length or shape is insufficient.
- **Idempotency:** exact authorization replay returns the same action/attempt
  with `replayed=true`, no second invocation and no version increment;
  conflicting authorization fails. Exact receipt replay creates no duplicate
  and no control-version increment; a different receipt for the same action
  conflicts. Exact effect replay creates no duplicate
  effect/decision/transition/checkpoint; a conflicting terminal effect fails.
- **Durable evidence:** temporary SQLite close/reopen preserves definition,
  control state, authorization, receipt, effect, decision, canonical normalized
  observation, auxiliary session/usage/failure/artifact values, and the exact
  action/receipt/effect linkage without in-memory custody.
- **StateStore atomicity:** injected failures at every statement boundary prove
  all-or-nothing definition/control initialization, decision, authorization,
  Attempt binding, receipt, effect, R3 transition/checkpoint/idempotency, and
  optimistic version update. Receipt fault injection proves no partial row or
  idempotency marker and no `runs.state_version` mutation. Stale version,
  missing/expired/wrong lease, and stale fence reject before provider invocation
  or evidence mutation as applicable.
- **SQL constraints:** direct invalid inserts prove FK rejection for wrong
  workflow/run/local-or-durable phase/gate/attempt/evidence, duplicate
  action/attempt/execution and duplicate effect, action in run/phase/version A
  referencing an authorization decision from B, effect referencing an effect
  decision from another run/phase/gate/version, and wrong decision record/kind.
  Two workflows using local phase `audit` must persist with distinct durable
  phase keys. Direct receipt negatives cover cross-run/action/attempt/execution/
  request binding, second receipt, effect-to-foreign-receipt binding, observed
  effect without receipt, and every illegal uncertainty receipt/reason pair.
  Exact effect-vocabulary and terminal-shape CHECKs also apply.
- **External-effect ordering:** fake executor asserts authorization is durable
  before `start`, one `get_result` precedes receipt capture, and receipt capture
  precedes every receipt-backed effect. Crash windows before call, before
  receipt, and before effect commit are represented. An authorized action
  without a receipt becomes `UNCERTAIN_EFFECT /
  NO_DURABLE_OBSERVATION_RECEIPT` and is never replayed automatically. The fake
  also asserts zero calls when authorization, version, fence, or persistence
  fails.
- **Lease-expiry custody:** authorize under fence F1; invoke exactly once;
  capture a truthful `SUCCEEDED` observation; commit its receipt under F1;
  expire F1; reject F1's effect write; acquire F2; load the same receipt and
  finalize `OBSERVED_SUCCEEDED`; assert one total provider invocation.
  Separately expire F1 before receipt persistence, reject the stale receipt,
  prove F2 cannot submit raw observation, and permit only `UNCERTAIN_EFFECT /
  NO_DURABLE_OBSERVATION_RECEIPT / FAILED_SAFE` without provider replay.
- **Usage evidence:** absent usage is valid; present normalized `UsageRecord`
  persists in the receipt and may additionally reload through accepted R3
  auxiliary storage without a public/domain `usage_id`; its storage key is the
  exact adapter-local derivation. Foreign same-run usage/session/failure/artifact
  rows cannot authorize or change an effect because only the exact receipt can.
- **Recovery interaction:** `RunRecoveryContext` plus unambiguous completed
  effect may be read as fact; missing/conflicting effect, ambiguous lease, or
  unresolved human decision stops for future Recovery/Reconciler authority.
- **Cancellation/containment delegation:** R6 consumes adapter-normalized
  timeout/cancel facts and does not duplicate R4/R5 cleanup logic.
- **R2–R5 regression:** the complete existing suite remains green; R2 contracts,
  R3 migration/checksum/lease/fencing/idempotency/recovery behavior, R4 fallback
  isolation, and R5 primary/no-automatic-fallback behavior remain unchanged.

The crash-window matrix is binding:

```text
before authorization commit -> no action row, no provider call
after authorization commit but before start -> AUTHORIZED_NOT_OBSERVED;
  re-entry with a valid recording lease records no-receipt uncertainty; without
  one it returns unresolved without mutation; never starts
after get_result but before receipt commit -> stale/raw observation has no
  cross-owner authority; later lease records no-receipt uncertainty; never replays
after receipt commit but before effect commit -> later valid owner loads receipt,
  finalizes exact mapped effect, and never invokes provider
after effect commit -> load immutable receipt/effect and never execute again
```

Migration tests must apply `001` then `002`, verify raw-byte checksum and schema
prefix, close/reopen with all R6 records intact, open a second time without
duplicate migration or semantic rows, and fail closed on `002` checksum drift.

No live SDK/CLI smoke, host validation, or F2E execution is part of the normal
R6 gate. Host validation is `NOT_APPLICABLE` unless a later fresh audit proves
an R6 requirement that cannot be evidenced with temporary SQLite and fakes.

## Resolved handoff and future implementation audit gates

The fresh bounded R6 handoff re-audit resolved every following gate as `PASS`:

```text
CANONICAL_TARGET
ORDINAL_CONSISTENCY
EXACT_ALLOWLIST
BOUNDARY_OWNERSHIP
R2_R5_REUSE
CONTRACT_SCHEMA_DELTA
NO_AUTHORITY_LEAK
FAIL_CLOSED
DETERMINISM
PRE_EFFECT_DURABLE_AUTHORIZATION
UNCERTAIN_EFFECT_ISOLATION
DURABLE_EVIDENCE
OBSERVATION_RECEIPT_CUSTODY
ACTION_RECEIPT_EFFECT_RELATIONAL_SCOPE
TEST_AUTHORITY
F2E_ISOLATION
AUTO_PUBLISH_FALSE
NO_PREMATURE_NEXT_PHASE
```

A later fresh implementation audit, if implementation becomes separately
authorized, must additionally verify:

- exact `21 / 21` touched-path scope and immutable `001_initial.sql`;
- schema/graph strictness and canonical fingerprinting;
- protocol decision matrix and applicability/lifecycle separation;
- durable authorization-before-effect ordering under fault injection;
- atomic decision/checkpoint evidence, authoritative receipt custody and
  ACTION -> RECEIPT -> EFFECT composite scope, idempotency, versions,
  leases/fencing, reopen behavior, and explicit uncertainty;
- no policy inside provider adapters or `StateStore`;
- no context, routing, retry, recovery, Git, publication, or supervisor leak;
- offline deterministic R6 tests and full R2–R5 regression; and
- unchanged F2E/product authority and `auto_publish=false`.

The author, executor, and corrector must not perform their own independent
audit.

## Carry-forward

```text
R4 P2 — CAPABILITY_TIMEOUT_PRIMARY_CAUSE_MASKED_BY_PRE_REAP_GROUP_LIVENESS:
  OPEN / NON_BLOCKING / CARRY_FORWARD / OUTSIDE_R6_SCOPE

R2 Debt C — attached branch without upstream behavioral coverage:
  OPEN / NON_BLOCKING / CARRY_FORWARD / OUTSIDE_R6_SCOPE
```

R4 P2 belongs to CLI capability-probe diagnostic ordering, which R6 does not
modify. R2 Debt C requires behavioral evidence from a concrete Repository/Git
adapter; R6 consumes only a typed repository fact or fake and does not
implement that adapter. Neither is potentially closable by this R6 scope, and
R6 must not widen to close either debt.

## Explicit exclusions

This handoff does not authorize:

- `ContextCompiler`;
- `ModelRouter` or automatic SDK-to-CLI fallback;
- Retry / Quota Governor or unbounded retry;
- Recovery Engine, resume/replay policy, or Reconciler;
- Repository / Git Adapter or Worktree Manager;
- Publisher, Git publication, staging, commit, or push;
- Supervisor, runtime composition, daemon, launchd, or scheduling;
- F2E implementation or execution;
- runtime activation, productive authority, F2E/product migration, product
  fence, or cutover;
- automatic publication or any change to `auto_publish=false`; or
- R7 or any later implementation.

## Stop conditions

Stop without implementation or activation if:

- branch, `HEAD`, upstream, remote, staging, working tree, or repository
  authority differs materially from the expected future audit baseline;
- a competent canonical defines a different post-R5 successor or boundary;
- the workflow graph cannot be made deterministic without absorbing a deferred
  component;
- exact durable authorization/receipt/effect evidence requires a path outside the
  allowlist;
- accepted R2–R5 contracts must be weakened rather than compatibly extended;
- provider execution would occur before durable authorization;
- uncertain effects would be replayed without later Recovery/Reconciler
  authority;
- normal tests require live provider/network/Git/F2E access; or
- any implementation, activation, publication, R7, or product authority is
  inferred from this materialized handoff.

The stop must name the exact authority, boundary, or allowlist gap and return
to handoff correction. It must not silently broaden R6.

## Final preserved authority

```text
R1: ACCEPTED / PUBLISHED / HISTORICAL
R2: PUBLISHED / CLOSED / HISTORICAL
R3: IMPLEMENTED / ACCEPTED / PUBLISHED / CLOSED / HISTORICAL
R4: IMPLEMENTED / ACCEPTED / PUBLISHED / CLOSED / HISTORICAL
R5: IMPLEMENTED / ACCEPTED / PUBLISHED / CLOSED / HISTORICAL
R5 HANDOFF: APPROVED / CLOSED / HISTORICAL / NOT_ACTIVE
SELECTED POST-R5 SUCCESSOR: AUTOPILOT R6 — Workflow Engine
R6 HANDOFF: MATERIALIZED / APPROVED / ACTIVE
R6 TARGET: SELECTED / AUTHORIZED_TO_START / NOT_STARTED
R6 IMPLEMENTATION: AUTHORIZED_TO_START
R6 INITIAL HANDOFF AUDIT: FAIL / P0=0 / P1=4 / P2=0
R6 CORRECTION.1: MATERIALIZED / FRESH_REAUDIT P1=4 OPEN / NEW_P1=0
R6 CORRECTION.2: MATERIALIZED
R6 CORRECTION.2 FRESH REAUDIT: FAIL / P0=0 / OPEN_P1=2 / NEW_P0=0 / NEW_P1=0 / NEW_P2=0
P1-1: CLOSED
P1-2: CLOSED
R6 CORRECTION.3: MATERIALIZED
R6 FINAL TWO-FINDING FRESH REAUDIT: PASS / OPEN_P1=0 / NEW_P0=0 / NEW_P1=0 / NEW_P2=0
P1-3: CLOSED
P1-4: CLOSED
P1-1/P1-2 REGRESSION: CLOSED / NO_REGRESSION
R6 HANDOFF AUDIT: PASS
R6 TECHNICAL AUTHORITY BLOCKERS: NONE
ACTIVE_AUTOPILOT_HANDOFF: R6 WORKFLOW ENGINE
R4 P2: OPEN / NON_BLOCKING / CARRY_FORWARD / OUTSIDE_R6_SCOPE
R2 Debt C: OPEN / NON_BLOCKING / CARRY_FORWARD / OUTSIDE_R6_SCOPE
R7: NOT_AUTHORIZED
F2E: UNCHANGED
auto_publish: false
NEXT ALLOWED ACTION: EXECUTE_ACTIVE_AUTOPILOT_R6_WORKFLOW_ENGINE
```
