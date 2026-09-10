# FeelingPilates — AUTOPILOT R6 Workflow Engine implementation audit

**Audit role:** `FRESH_INDEPENDENT_R6_IMPLEMENTATION_AUDITOR`

**Persisting role:** `R6_CORRECTION_AUTHORITY_MATERIALIZER /
IMPLEMENTATION_FINDING_MAPPER`

**Mode:** `AUDIT_RESULT_PERSISTENCE / DOCUMENTATION_ONLY /
NO_IMPLEMENTATION / NO_ACCEPTANCE / NO_PUBLICATION`

## Object, provenance, and authority

This review persists the result of the fresh independent audit of the dirty R6
Workflow Engine implementation candidate. It records the audit as supplied; it
does not correct, accept, publish, close, or activate the candidate.

```text
Repository: /Users/jesusaldaircruzortiz/Desktop/Feelingpilates/feelingpilates-autopilot-r1
Branch: orquestacion/autopilot-r1
Authority base / HEAD: 6005319aebe7f23814f7d270555faa2f7cda03b4
Resolved upstream: 6005319aebe7f23814f7d270555faa2f7cda03b4
Live remote: 6005319aebe7f23814f7d270555faa2f7cda03b4
Staging: EMPTY
Published R6 implementation allowlist: 22 exact paths
Candidate dirty implementation paths: 19
Candidate paths outside allowlist: 0
Implementation candidate: AUTHORIZED_DIRTY / MATERIALIZED / AUDIT_FAILED
```

The parent semantic authority is
`auditoria/handoffs/HANDOFF-AUTOPILOT-R6-WORKFLOW-ENGINE.md`. Repository and
parent handoff authority prevail over this historical audit artifact.

## Audit result

```text
P0=3
P1=6
P2=1
READY_TO_ACCEPT_R6_IMPLEMENTATION=NO
AUDIT_RESULT=FAIL
```

| Finding | Severity | Blocking | Disposition |
| --- | --- | --- | --- |
| P0-1 — Engine re-entry repeats provider execution | P0 | YES | OPEN / CORRECTION_REQUIRED |
| P0-2 — Provider execution starts without required durable authorization evidence | P0 | YES | OPEN / CORRECTION_REQUIRED |
| P0-3 — Fabricated or contradictory receipts can authorize observed success | P0 | YES | OPEN / CORRECTION_REQUIRED |
| P1-1 — Workflow schema and graph validator are not fail-closed | P1 | YES | OPEN / CORRECTION_REQUIRED |
| P1-2 — WorkflowPolicy accepts contradictory or insufficiently scoped facts | P1 | YES | OPEN / CORRECTION_REQUIRED |
| P1-3 — Exact R6 domain and StateStore contracts are missing | P1 | YES | OPEN / CORRECTION_REQUIRED |
| P1-4 — Migration 002 lacks the frozen relational authority | P1 | YES | OPEN / CORRECTION_REQUIRED |
| P1-5 — Effect finalization is not a policy-driven atomic workflow transition | P1 | YES | OPEN / CORRECTION_REQUIRED |
| P1-6 — Green suites do not exercise the frozen R6 authority | P1 | YES | OPEN / CORRECTION_REQUIRED |
| P2-1 — README carries contradictory stale workflow-engine wording | P2 | NO | OPEN / NON_BLOCKING / CARRY_FORWARD |

## P0-1 — Engine re-entry repeats provider execution

### Evidence

The audited production-engine and file-backed SQLite reproduction began after
a completed action at durable `state_version=2`. Re-entry produced:

```text
versions: 2 -> 4
provider starts: 2
get_result calls: 2
workflow actions: 2
workflow effects: 2
second provider_called: true
```

The candidate derives action identity from mutable control version before
looking up existing action/effect state. A post-effect version can therefore
derive a different action for the same semantic workflow phase.

### Impact and blocking status

This violates the R6 at-most-once provider boundary and may duplicate an
external effect. It is a blocking P0; no R6 acceptance is competent while it
is open.

### Required correction

- Resolve the durable action/effect/receipt for the canonical
  workflow/run/phase/durable-phase identity before deriving or authorizing a
  new action.
- Do not let a mutable post-effect `runs.state_version` create a new semantic
  action identity for an already-authorized phase.
- Enforce the frozen one-action relationship independently in SQLite.
- Re-entry with a durable effect, a durable receipt without an effect, or a
  durable action without a receipt must make zero provider calls. Only a
  genuinely new action may execute once.
- Add a real `WorkflowEngine` plus file-backed `SQLiteStateStore` regression
  beginning from the durable version after first effect finalization. The
  second invocation must produce `start=0`, `get_result=0`, `action=0`, and
  `effect=0` additional events/rows.
- Do not introduce retry, resume, polling, fallback, or recovery orchestration
  as the repair.

## P0-2 — Provider execution lacks complete durable authorization

### Evidence

The audited candidate treated a `workflow_actions` row plus a run-version
increment and lease check as sufficient authorization. It did not atomically
materialize the complete R6 decision/control/transition/checkpoint/Attempt/
idempotency evidence before `AgentExecutor.start`.

### Impact and blocking status

An orphan action row can grant provider authority without proof of the exact
workflow decision that authorized it. This is a blocking P0.

### Required correction

Before provider start, one atomic authorization transaction must validate or
materialize the exact R6 authority: workflow definition and fingerprint;
workflow/run identity; current control, phase, gate facts/outcomes and typed
policy decision; allowed transition; checkpoint/control lifecycle; prepared
execution spec and durable phase identity; R3 `Attempt`; execution/action/
request identity; R3 idempotency evidence; optimistic
`runs.state_version` transition; and current lease/holder/resource/fencing
authority. It must define prerequisites, deterministic values, inserted or
updated rows, atomic commit, and the typed authorization result returned to the
engine. `start=0` and `get_result=0` on every failure. A lone action row is
never sufficient.

## P0-3 — Fabricated or contradictory receipts can authorize success

### Evidence

The production persistence surface materially accepted this contradictory
caller-supplied combination:

```text
receipt_id: arbitrary
observation_status: FAILED
observation_fingerprint: not-a-hash
canonical_observation_json: not-json
effect_state: OBSERVED_SUCCEEDED
```

The candidate accepted caller-selected receipt identity/fingerprints/payloads,
made the recording fence conditional, and did not derive effect meaning from a
durably loaded canonical receipt.

### Impact and blocking status

Fabricated evidence can be persisted as authoritative success. This breaks the
R6 observation-custody and ACTION -> RECEIPT -> EFFECT chain and is a blocking
P0.

### Required correction

- Receipt capture must internally canonicalize and validate observation bytes,
  observation fingerprint, receipt preimage/ID, receipt payload/fingerprint,
  terminal classification, and exact action/run/attempt/execution/request
  binding.
- Caller-supplied derived identity or fingerprint values cannot override the
  canonical derivation.
- The required capture fence and receipt idempotency evidence must commit
  atomically with the receipt.
- Effect finalization must load the durable receipt internally, require the
  recording fence, and derive the only legal effect state from the receipt
  status and exact R6 matrix.
- Direct production negatives must reject at least `FAILED ->
  OBSERVED_SUCCEEDED`, `SUCCEEDED -> OBSERVED_FAILED`, terminal receipt ->
  `NONTERMINAL_OBSERVATION`, and nonterminal receipt -> any observed terminal
  state.

## P1-1 — Workflow schema and graph validation are not fail-closed

### Evidence

The candidate does not enforce the complete `workflow-definition-v1`
structural and semantic graph authority. The audit specifically demonstrated
acceptance of a noncanonical transition ID and a `PASS -> HUMAN_STOP` authority
inversion.

### Impact and blocking status

Invalid or authority-inverting graphs can become workflow authority. This is a
blocking P1.

### Required correction

Enforce transition identities, source/target phases, trigger vocabulary, gate
and frozen role references, outgoing cardinality, terminal/nonterminal rules,
publication and correction transition rules, human-stop, failed-safe and
`NOT_APPLICABLE` paths, duplicate rejection, DAG/cycle validity, and frozen
reachability/terminal structure. JSON Schema is not a substitute for semantic
graph validation. Direct regressions must reject both audited counterexamples.

## P1-2 — WorkflowPolicy accepts contradictory or cross-scope facts

### Evidence

The policy did not validate the complete workflow/fingerprint/run/phase/role/
gate fact identity and could accept an `AgentResult` from another run. It also
routed a `PASS` carrying correctable P1 findings to correction merely because
correction was possible.

### Impact and blocking status

Cross-run or internally contradictory facts can drive workflow transitions.
This is a blocking P1.

### Required correction

Validate workflow, fingerprint, run, phase, role, gate, execution status,
semantic result status, finding severity/counts, and correction budget/
exhaustion against the exact parent-handoff matrix. Reject facts from another
run and contradictory semantic results. `PASS` plus correctable P1 is a
contract contradiction, not implicit correction authority. Preserve the
policy as deterministic, provider-free, persistence-free, Git-free,
routing-free, and retry-free.

## P1-3 — Exact domain and StateStore contracts are missing

### Evidence

The candidate exposed generic `**values -> object` R6 persistence methods and
only a subset of the frozen operations. It omitted exact typed contracts for
definition save, control initialization, and decision commit, as well as
required immutable workflow control/decision/authorization/effect result
records.

### Impact and blocking status

The engine/store boundary cannot express or enforce the accepted R6 authority.
This is a blocking P1.

### Required correction

Materialize the immutable parent-handoff records and all nine exact typed
StateStore operations:

1. `save_workflow_definition_v1`
2. `initialize_workflow_control_v1`
3. `commit_workflow_decision_v1`
4. `authorize_workflow_action_v1`
5. `record_workflow_observation_receipt_v1`
6. `record_workflow_effect_v1`
7. `load_workflow_control_v1`
8. `load_workflow_action_effect_v1`
9. `load_workflow_observation_receipt_v1`

The correction must preserve accepted R2–R5 contracts rather than redesigning
them.

## P1-4 — Migration 002 lacks frozen relational authority

### Evidence

The candidate created the nine table names but omitted material parent-handoff
columns, candidate keys, composite foreign keys, R3 bindings, lifecycle and
status checks, idempotency/fencing relationships, and all three required
supporting indexes.

### Impact and blocking status

SQLite cannot independently reject cross-scope, contradictory, or orphan R6
evidence. This is a blocking P1.

### Required correction

Preserve append-only `001_initial.sql` and materialize the exact nine-table R6
relational design:

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

Also materialize the three frozen parent-candidate unique indexes for
`attempts`, `leases`, and `idempotency_records`, with every mandatory column,
PK/UNIQUE key, composite FK, R3 decision/attempt/execution/request binding,
capture/recording fence evidence, idempotency identity/payload evidence,
lifecycle/terminal/uncertainty check, and control/version relationship. Direct
SQLite tests, not table-name presence alone, must prove rejection.

## P1-5 — Effect and decision are not one authorized transition

### Evidence

The candidate persisted the effect before computing the success policy and did
not atomically bind effect, typed decision, transition/checkpoint, workflow
control/lifecycle, operational state, idempotency, version `N+1 -> N+2`, and
recording-fence evidence.

### Impact and blocking status

An effect can become durable before any decision authorizes its workflow
meaning, leaving partial or contradictory state. This is a blocking P1.

### Required correction

The required order is: load authoritative durable receipt; derive the exact
typed policy decision from receipt and workflow facts; validate the declared
transition; atomically commit the complete finalization transaction. Map
`FAILED` and `INTERRUPTED` to their observed effect and typed `FAILED_SAFE`
transition. Map `PENDING`, `RUNNING`, or `UNKNOWN` to `UNCERTAIN_EFFECT /
NONTERMINAL_OBSERVATION` and a `FAILED_SAFE`-compatible typed transition. No
effect-only durable authority may survive.

## P1-6 — Test authority is superficial

### Evidence

Green tests relied on fake-store re-entry with stale/pre-authorization
versions, permitted contradictory receipt/effect combinations, and did not
exercise the exact relational and transactional authority. The audited weak
coverage included an R2–R5 regression file with one enum assertion and a
fail-closed file with one invalid-sandbox assertion. Some expected canonical
values were generated tautologically by the function under test.

### Impact and blocking status

The suite can remain green while all P0s and material contract gaps remain.
This is a blocking P1.

### Required correction

Production-behavior tests must cover real engine plus file-backed SQLite
current-version re-entry; complete pre-start authorization evidence; fabricated
receipt/effect rejection; graph and policy negative matrices; direct typed
StateStore contracts; exact migration structure; atomic finalization; fault
injection; fencing windows; close/reopen custody; and substantive R2–R5
regression. Golden expectations must be independent constants. The prior weak
tests may remain only if they are no longer the sole evidence.

## P2-1 — README wording

The README simultaneously says no workflow engine exists and describes the R6
candidate. This is `OPEN / NON_BLOCKING / CARRY_FORWARD`; it is outside
Correction.1 and does not block readiness once every P0/P1 is competently
closed.

## Required fault-injection and acceptance evidence

Correction evidence must prove:

- before authorization commit: provider `0`;
- after authorization commit/before provider: durable action authority is
  recoverable without provider replay;
- after provider/before receipt: no trusted receipt and no raw-observation
  transfer;
- after receipt/before effect: a later owner finalizes without provider replay;
- inside finalization: no partial effect/decision/control state;
- idempotency or fencing conflict: no mutation;
- cumulative `AgentExecutor.start <= 1` and `get_result <= 1` for one canonical
  authorized action across re-entry, reopen, new owner/fence, finalized,
  receipt-only, and no-receipt states;
- all required durable authority exists before provider start; a
  `workflow_actions` row alone fails this gate;
- exact schema columns, candidate keys, composite FKs, indexes, checks, R3
  bindings, fence evidence, and idempotency evidence; and
- all seven frozen R6 goldens unchanged and passing, the focused corrected R6
  suite passing, the complete suite passing, substantive R2–R5 regression
  passing, no deferred capability leakage, exact `22 / 22` allowlist, and empty
  staging.

These tests inspect state and transaction boundaries; they do not authorize or
implement a Recovery Engine.

## Preserved boundaries and verdict

```text
R6 semantic target: UNCHANGED
R6: APPROVED / ACTIVE
R6 implementation candidate: AUDIT_FAILED / CORRECTION_REQUIRED
R2-R5 accepted authority: UNCHANGED
R4 P2: OPEN / NON_BLOCKING / CARRY_FORWARD
P2-1: OPEN / NON_BLOCKING / CARRY_FORWARD
Deferred capabilities: NOT_AUTHORIZED
R7: NOT_AUTHORIZED
F2E: UNCHANGED / NOT_EXECUTED
auto_publish: false
READY_TO_ACCEPT_R6_IMPLEMENTATION: NO
AUDIT_RESULT: FAIL
NEXT ACTION: MATERIALIZE_R6_IMPLEMENTATION_CORRECTION_AUTHORITY_FOR_P0-1_THROUGH_P1-6
```
