# FeelingPilates — HANDOFF: AUTOPILOT R6 Workflow Engine implementation Correction.2

**Authority role:** `R6_CORRECTION_2_AUTHORITY_MATERIALIZER /
RESIDUAL_FINDING_MAPPER / IMPLEMENTATION_REAUDIT_PERSISTER /
REGRESSION_GATE_DESIGNER / AUTHORITY_BOUNDARY_EDITOR`

**Workflow profile:** `AUTHORITY_MATERIALIZATION / DOCUMENTATION_ONLY /
NO_IMPLEMENTATION / NO_PUBLICATION / NO_SELF_AUDIT`

## Lifecycle and sources

```text
Correction: AUTOPILOT-R6-WORKFLOW-ENGINE-IMPLEMENTATION-CORRECTION-2
Status: MATERIALIZED / AUTHORITY_AUDIT_FAILED /
  AUTHORITY_CORRECTION_REQUIRED / NOT_EXECUTABLE
Authority base: da5b119d0819f2dcca74a9a71331394b0f752870
Parent R6 authority:
  auditoria/handoffs/HANDOFF-AUTOPILOT-R6-WORKFLOW-ENGINE.md
Parent correction:
  auditoria/handoffs/HANDOFF-AUTOPILOT-R6-WORKFLOW-ENGINE-IMPLEMENTATION-CORRECTION-1.md
Source re-audit:
  auditoria/reviews/AUTOPILOT-R6-WORKFLOW-ENGINE-CORRECTION-1-IMPLEMENTATION-REAUDIT.md
Source re-audit result: FAIL / PARTIAL_CLOSURE / NEW_P0=0 / NEW_P1=0 / NEW_P2=0
Fresh Correction.2 authority audit:
  auditoria/reviews/AUTOPILOT-R6-CORRECTION-2-AUTHORITY-AUDIT.md
Correction.2 authority audit result: FAIL / P0=0 / P1=2 / P2=0
READY_TO_PUBLISH_R6_CORRECTION_2_AUTHORITY: NO
R6: APPROVED / ACTIVE
R6 implementation: CORRECTION_MATERIALIZED / REAUDIT_FAILED /
  CORRECTION_REQUIRED / NOT_ACCEPTED / NOT_PUBLISHED
Correction.1: APPROVED / ACTIVE / PUBLISHED / EXECUTED /
  REAUDIT_FAILED / PARTIAL_CLOSURE
READY_TO_EXECUTE_CORRECTION_2: NO
READY_TO_ACCEPT_R6_IMPLEMENTATION: NO
R7: NOT_AUTHORIZED
F2E: UNCHANGED / NOT_EXECUTED
auto_publish: false
Forward Lane: WAITING_FOR_MAIN
FORWARD_LANE_RESYNC_REQUIRED: NO
NEXT REQUIRED AUTHORITY ACTION:
  MATERIALIZE_AUTOPILOT_R6_CORRECTION_2_AUTHORITY_CORRECTION_1
```

Materialization is not execution authority. The fresh independent authority
audit failed this handoff with two blocking P1 findings. Correction.2 may not
be published as executable or executed until a separate bounded authority
correction is materialized, independently audited, and competently published.
That correction is not performed by this state update.

## Fresh independent authority audit result

```text
P0: 0
P1: 2
P2: 0
AUDIT_RESULT: FAIL
READY_TO_PUBLISH_R6_CORRECTION_2_AUTHORITY: NO
READY_TO_EXECUTE_CORRECTION_2: NO
READY_TO_ACCEPT_R6_IMPLEMENTATION: NO
```

The two authority blockers are persisted with evidence, impact, and required
correction at
`auditoria/reviews/AUTOPILOT-R6-CORRECTION-2-AUTHORITY-AUDIT.md`:

```text
P1-A: policy compatibility matrix overlaps and is not fully discriminated
P1-B: residual result/idempotency contract is non-exact and conflicts with
      parent R6
```

These are authority findings, not new implementation findings. They do not
alter the six residual implementation findings, reopen the three independently
closed findings, or expand the 22-path implementation allowlist.

## Physical provenance and implementation preservation

```text
Repository: /Users/jesusaldaircruzortiz/Desktop/Feelingpilates/feelingpilates-autopilot-r1
Branch: orquestacion/autopilot-r1
HEAD: da5b119d0819f2dcca74a9a71331394b0f752870
Resolved upstream: da5b119d0819f2dcca74a9a71331394b0f752870
Live remote: da5b119d0819f2dcca74a9a71331394b0f752870
Staging: EMPTY
Implementation candidate: AUTHORIZED_DIRTY / UNCOMMITTED
Implementation paths: 22 / EXACT PUBLISHED ALLOWLIST
Candidate paths outside allowlist: 0
```

The implementation fingerprint is the lowercase SHA-256 of the deterministic
stream consisting of tracked `git diff HEAD --binary --no-ext-diff --
tools/autopilot`, followed in bytewise path order by an `UNTRACKED <path>`
marker and `git hash-object` value for each untracked `tools/autopilot` path.

```text
CORRECTION_2_BASE_IMPLEMENTATION_FINGERPRINT:
feb1c8a8a56f2d5dd33c14ba7e289a3620e5c7e7815c0681df60b68d7ec24ce0

Implementation fingerprint after authority materialization:
feb1c8a8a56f2d5dd33c14ba7e289a3620e5c7e7815c0681df60b68d7ec24ce0

Candidate preserved:
PASS
```

Correction.2 authority materialization may not alter, normalize, stage, or
attribute any byte of that candidate.

## Exact finding scope

Correction.2 covers exactly six residual blocking findings:

```text
P0-2
P0-3
P1-2
P1-3
P1-4
P1-6
```

Closed findings are regression gates only:

```text
P0-1: CLOSED / REGRESSION_GATE_ONLY
P1-1: CLOSED / REGRESSION_GATE_ONLY
P1-5: CLOSED / REGRESSION_GATE_ONLY
```

Carry-forward items remain outside implementation scope:

```text
R6 P2-1: OPEN / NON_BLOCKING / CARRY_FORWARD / OUTSIDE_CORRECTION_2
R4 P2: OPEN / NON_BLOCKING / CARRY_FORWARD / OUTSIDE_R6_CORRECTION_SCOPE
```

No closed finding may be redesigned. A later regression reopens it.

## Exact unchanged implementation allowlist

Correction.2 may use only the already-published 22 paths; this handoff adds no
path, wildcard, migration, or dependency:

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
tools/autopilot/tests/test_codex_cli_command.py
```

```text
ALLOWLIST: 22 / EXACT / UNCHANGED
EXPANSION: NONE
001_initial.sql: IMMUTABLE / OUT_OF_SCOPE
```

If a twenty-third or substituted path is genuinely necessary, stop with
`R6_CORRECTION_2_ALLOWLIST_AUTHORITY_GAP`.

## P0-2 authority — the actual policy decision authorizes provider execution

The exact typed result returned by `WorkflowPolicy.decide(...)` is the semantic
prerequisite for durable action authorization. `WorkflowEngine` must consume
that result; it must not discard it, replace it with `plan_execution`, or infer
authorization independently.

Only a decision whose exact kind is `AUTHORIZE_PHASE_ACTION`, whose record kind
is `ACTION_AUTHORIZATION`, and whose workflow/fingerprint/run/phase/gate/action/
version/evidence fields match the current durable authority may reach
`authorize_workflow_action_v1`. Only successful commit of that exact decision
may permit `AgentExecutor.start`.

The engine/store boundary must carry the same typed decision plus exact
`StateTransition`, `Checkpoint`, control/version, Attempt, request,
idempotency, and fencing evidence. SQLite may validate and persist it but may
not synthesize a replacement decision ID or semantic decision from `action_id`,
request ID, `sha256(action_id)`, or any proxy.

```text
WorkflowPolicy -> exact WorkflowDecisionV1
-> authorize_workflow_action_v1 persists that same decision atomically
-> commit succeeds
-> AgentExecutor.start may occur once
```

Every non-execution decision exits before authorization and provider calls:

```text
HUMAN_STOP: start=0 / get_result=0 / new action=0 / receipt=0 / effect=0
FAILED_SAFE: start=0 / get_result=0 / new action=0 / receipt=0 / effect=0
NOT_APPLICABLE: start=0 / get_result=0 / new execution action=0
WAIT: start=0 / get_result=0
EXTERNAL_FACT_WAIT: start=0 / get_result=0
READY_TO_PUBLISH: start=0 / get_result=0
COMPLETED: start=0 / get_result=0
```

The parent R6 matrix remains decisive if a row is more restrictive.

### P0-2 production acceptance

A real `WorkflowEngine` plus real file-backed `SQLiteStateStore` test must feed
a valid scoped `HUMAN_STOP` policy fact and prove zero start/result/action/
receipt/effect creation. Equivalent direct tests cover every non-execution
state above.

For `AUTHORIZE_PHASE_ACTION`, the executor fake must open a separate SQLite
connection inside `start` and observe the same decision ID, record kind,
decision kind, transition, checkpoint, action, Attempt, idempotency, expected
version, committed version, and fence that `WorkflowPolicy` returned. Any
synthetic or different decision fails before provider execution.

## P0-3 authority — exact typed provider observation and final receipt lifecycle

### Typed observation custody

`WorkflowObservationReceiptRequestV1.normalized_observation` must be typed as
the accepted R2 provider-neutral `ExecutionObservation`, not `Mapping`, `dict`,
`Any`, or another open semantic container. Receipt capture validates the full
nested R2 contract before canonical derivation, including:

- exact execution/run/session/attempt identity;
- `ExecutionStatus` and terminal/nonterminal shape;
- `AgentResult`, gate, role, workflow/run and semantic status compatibility on
  success;
- normalized `FailureRecord` and absence of `AgentResult` on failure or
  interruption;
- absence of semantic terminal result on pending/running/unknown;
- typed `UsageRecord`, `SessionReference`, and `Artifact` values;
- result/session and embedded/observation usage consistency;
- bounded evidence references and output flags; and
- rejection of provider-native, unknown, or contradictory nested data.

Only after full validation may the receipt transaction internally derive the
canonical observation projection/bytes, observation fingerprint, terminal
classification, receipt preimage/ID, canonical receipt payload, payload
fingerprint, and exact action/run/attempt/execution/request binding. Caller-
created mappings and precomputed semantic claims are rejected even when no
provider call occurred.

Capture fence fields are required, typed, and non-null. The unique current
capture authority remains the accepted R3 lease ID, holder string, protected
resource string, and positive fencing-token integer matching the durable
action authorization.

### No late receipt after finalization

Receipt insertion must first establish that no effect/finalization exists for
the action. Once any authoritative effect exists, the action's receipt state is
terminal. This includes:

```text
UNCERTAIN_EFFECT / NO_DURABLE_OBSERVATION_RECEIPT
```

A later receipt attempt is rejected atomically with:

```text
new receipt row: 0
idempotency mutation: 0
state-version mutation: 0
effect mutation or observed-success upgrade: 0
```

Both the StateStore transaction and SQLite relational authority must enforce
this ordering. An application-only precheck is insufficient.

### P0-3 acceptance

Direct production tests must prove typed successful observation acceptance;
caller-fabricated success rejection; malformed nested `FAILED` rejection;
contradictory nested typed observation rejection; and late receipt rejection
after no-receipt finalization. Capture/recording fences, derived-effect matrix,
all seven goldens, and F1 receipt -> F2 effect custody remain mandatory.

## P1-2 authority — complete policy compatibility matrix

Every `WorkflowPolicy` branch must validate the entire
`WorkflowPolicyFactV1`: workflow, fingerprint, run, phase, role, gate,
execution status, semantic result, gate result, exact P0/P1/P2 counts,
human-decision flag, correction artifact/edge, correction count, maximum, and
exhaustion.

The parent R6 handoff section `Exact responsibility boundary -> Decisions R6
owns` and its minimum semantic matrix remain binding. This table makes the
residual compatibility rule executable:

| Exact compatible fact | Required disposition |
| --- | --- |
| Declared non-applicable phase plus `NOT_APPLICABLE`, no contradictory semantic result | explicit skip through only its declared edge; provider 0 |
| Applicable gate `PASS`, compatible `SUCCEEDED` semantic result, P0=0, P1=0, no human decision | declared `PASS` transition only |
| Gate/result `PENDING` with no terminal semantic claim | `WAIT`; no next action |
| `STOP`, any P0, `SECURITY_STOP`, or `requires_human_decision=true` | `HUMAN_STOP`; provider 0 |
| Nonempty P1, gate result not `PASS`, `p1_correctable=true`, one correction artifact, one declared correction edge, budget available | `CORRECTION_REQUIRED` to that edge only |
| Nonempty P1 with ambiguity, no artifact/edge, non-correctable status, or exhausted budget | `HUMAN_STOP`; provider 0 |
| `FAIL` or `BLOCKED` without the exact correction-qualified row above | declared `FAILED_SAFE` only; no retry |
| Failed/interrupted/nonterminal provider observation without `AgentResult` | operational effect mapping only; no invented semantic gate result |

All other combinations are contradictions and fail closed with no transition or
provider effect. In particular:

```text
GateResult.PASS + any P1 -> REJECT
GateResult.PASS + any P0 -> REJECT
GateResult.PASS + human decision -> REJECT
GateResult.PASS + incompatible execution/semantic status -> REJECT
foreign workflow/fingerprint/run/phase/role/gate -> REJECT
```

No branch may discover findings first and reinterpret an incompatible gate.
Policy remains deterministic, provider-free, persistence-free, Git-free,
routing-free, and retry-free.

## P1-3 authority — exact residual immutable typed contracts

Generic semantic types may not appear in the R6 request/result/load surfaces.
The exact accepted temporal type for `created_at`, `authorized_at`, and
`recorded_at` is Python `datetime`, supplied by the injected accepted `Clock`;
it is not `object`, a free-form string, or caller identity authority.

### Authorization request and result

`WorkflowActionAuthorizationRequestV1` must carry exact typed fields:

```text
workflow_id, workflow_fingerprint, run_id,
expected_control_version=N, phase_id, gate_id, role,
PreparedExecutionSpecV1, action_kind=AGENT_EXECUTION, request_fingerprint,
the exact WorkflowDecisionV1 returned by policy,
action_id, Attempt(ordinal=0, durable phase identity), execution_id,
StateTransition, Checkpoint,
authorization_lease_id, authorization_lease_holder,
protected_resource_key, authorization_fencing_token,
idempotency_key, operation_kind, canonical_operation_identity,
canonical authorization payload and payload_fingerprint
```

The typed decision must be `ACTION_AUTHORIZATION /
AUTHORIZE_PHASE_ACTION`, bind the same action and evidence, and carry
`control_version_before=N`, `control_version_after=N+1`. Transition,
checkpoint, idempotency, request, Attempt, and fence must bind those same
identities.

`WorkflowActionAuthorizationV1.authorization_control_version` is exactly the
pre-commit authorized version `N`. `WorkflowAuthorizationResultV1` separately
contains `committed_control_version=N+1`, the durable decision and authorization
evidence, and `replayed`. They may not be conflated:

```text
expected N=0
-> authorization_control_version=0
-> committed_control_version=1
```

Exact replay returns the same original `0/1` evidence, decision, transition,
checkpoint, and idempotency record with no mutation and no provider call.

### Receipt request and result

`WorkflowObservationReceiptRequestV1` carries the exact typed
`ExecutionObservation`, exact action/run/attempt/execution/request identity,
and required capture lease/holder/resource/fencing authority. It exposes no
mapping or caller-selected receipt identity/fingerprint/payload.

`WorkflowObservationReceiptResultV1` contains the immutable canonical
`WorkflowObservationReceiptV1`, its required capture-fence evidence, the exact
receipt idempotency evidence, and the authorization committed version it
retains. Receipt capture does not claim or create a new control version.

### Effect request and result

`WorkflowEffectRecordRequestV1` requires the exact typed finalization decision,
transition, checkpoint, effect idempotency binding, expected version, and
required recording lease/holder/resource/fencing authority. It accepts no raw
observation or generic semantic payload.

`WorkflowEffectResultV1` contains the durable effect, same finalization
decision, idempotency evidence, `committed_control_version=N+2`, and replay
status.

### Load results and idempotency evidence

Load results remain explicit state unions and must return the complete evidence
applicable to the loaded chain. `load_workflow_action_effect_v1` returns
authorization, zero-or-one receipt, zero-or-one effect, and the exact
authorization/receipt/effect idempotency records. Receipt and effect-specific
loads likewise return their applicable idempotency evidence.

Each typed idempotency projection contains at least the frozen R3 fields:

```text
idempotency_key
operation_kind
canonical_operation_identity
payload_fingerprint
transition_id
checkpoint_id
resulting_state_version
```

For receipt capture, transition/checkpoint/version are the already-committed
authorization anchors; receipt persistence does not advance them. Missing or
foreign idempotency evidence fails closed rather than producing a partial load.

All fence fields required by the operation are non-optional. All typed result
fields reflect durable database evidence, not caller values.

## P1-4 authority — SQLite decision/transition and terminal receipt invariants

### Decision kind to transition trigger

SQLite must independently reject a durable decision whose kind/result is
incompatible with its referenced workflow transition. Application validation
or a clean `foreign_key_check` alone is insufficient.

The exact compatibility relation is:

| Durable decision kind | Permitted referenced trigger |
| --- | --- |
| `AUTHORIZE_PHASE_ACTION` | no definition transition (`NULL`) |
| `WAIT` | no definition transition (`NULL`) |
| `EXTERNAL_FACT_WAIT` | no definition transition (`NULL`) |
| `ADVANCE` | `PASS` or `NOT_APPLICABLE`, with non-null target phase |
| `CORRECTION_REQUIRED` | `CORRECTION_REQUIRED`, with non-null correction target |
| `HUMAN_STOP` | `HUMAN_STOP` and matching terminal outcome |
| `FAILED_SAFE` | `FAILED_SAFE` and matching terminal outcome |
| `READY_TO_PUBLISH` | `MANUAL_PUBLICATION_BOUNDARY` and matching terminal outcome |
| `COMPLETED` | `PASS` or `NOT_APPLICABLE` and terminal outcome `COMPLETED` |

The parent handoff's stricter decision-shape, gate, source/target, scope, and
version constraints also apply. A direct SQL mutation from `ADVANCE` to
`CORRECTION_REQUIRED` while retaining a `PASS / audit-pass` transition must be
rejected by SQLite.

The physical mechanism is implementation-defined because parent authority
freezes the invariant, not a single SQLite technique. A composite candidate
key/FK, relational trigger, compatible CHECK/trigger combination, or another
SQLite-enforced mechanism is permitted only if it enforces the exact invariant
and introduces no semantic value outside R6.

### Terminal receipt ordering

SQLite must reject insertion of any `workflow_observation_receipts` row when a
`workflow_effects` row already exists for the same action. This includes the
terminal no-receipt state. The check and insert must be transactionally safe;
application-only ordering is insufficient.

Direct SQL acceptance must prove:

```text
incompatible decision kind / transition trigger -> REJECT
compatible decision / transition -> PASS
no-receipt terminal effect then late receipt -> REJECT
valid receipt before effect -> PASS
F1 valid receipt then F2 valid effect -> PASS
```

All previously correct tables, three implementation-named explicit indexes,
keys, FKs, CHECKs, action/receipt/effect scope, fences, reopen, and checksum
authority remain unchanged.

## P1-6 authority — direct regressions and mutation sensitivity

Correction.2 must add direct production or direct-SQL regressions for every
freshly reproduced counterexample:

1. valid scoped `HUMAN_STOP` through real engine+file SQLite -> provider 0;
2. decision observed through a separate connection inside `start` equals the
   actual policy decision;
3. `GateResult.PASS + correctable P1` -> rejected;
4. caller-fabricated nested successful observation -> rejected;
5. malformed nested `FAILED` observation -> rejected;
6. late receipt after no-receipt finalization -> rejected;
7. authorization version evidence exposes pre-commit `N` and committed `N+1`;
8. receipt request has no generic Mapping/dict/`Any` semantic authority;
9. required fence, `datetime`, decision, transition/checkpoint, and
   idempotency fields are exactly typed;
10. load results contain all required idempotency evidence;
11. direct SQL decision-kind/transition-trigger contradiction -> rejected; and
12. direct SQL late receipt after terminal no-receipt effect -> rejected.

Each test must fail if its residual defect is reintroduced:

```text
discard policy decision -> FAIL
synthesize decision ID from action ID -> FAIL
accept Mapping observation -> FAIL
allow late receipt -> FAIL
allow PASS+P1 correction -> FAIL
return wrong authorization version -> FAIL
remove decision-trigger SQLite enforcement -> FAIL
```

`88/88` focused or `158/158` complete green counts alone are explicitly
insufficient. For every residual, the implementation handoff must name the
exact production test or direct SQL probe that fails under the re-audit's
counterexample.

## Closed-finding regression authority

Correction.2 must rerun without redesign:

- P0-1: real current-version file-backed re-entry with second provider calls
  zero;
- P1-1: complete negative graph/schema matrix; and
- P1-5: policy-before-effect atomic finalization and fault rollback matrix.

```text
P0-1: REGRESSION_GATE
P1-1: REGRESSION_GATE
P1-5: REGRESSION_GATE
```

Any regression reopens the finding and blocks acceptance.

## Immutable seven-golden gate

```text
Workflow: 0b7b19fac6fc44ebeca474990b3de1fd5701590fcc5bfe51fd099525d768de54
Request: 07200548a86a2f95702f6a8d2bc88eafe599bb6dc63c94b0068f0a9f62fadaa3
Action: 4a4a9fb15cb3622e809bf643f1935f3715c5a970e602475ed2f7f8b615a89634
Durable phase: wfphase-b86c05eff72d8bec22b2cc4ad704dc3f49e78910ddd1a3dc95247a355919635e
Observation: c14ce45d5d922dcf014e41f82f04aeae45b6488a60182393060a9e8c02a9661b
Receipt: r6obs-a18831ebf7f7ae5d476280f3e480eb448a3d7a715f6d8695cf121fb0fae0f917
Receipt payload: bac47da1ec96c5eaa9e3da2241d0cd2e8486a55726e4f59e009ca8777dbe2b63
```

No golden change is authorized.

## R2-R5 regression and deferred boundary

Correction.2 must preserve accepted R2 execution contracts; R3 migration,
state/version, lease/fence, idempotency, and recovery behavior; R4 Codex CLI
`PUBLISHED / FALLBACK / DIAGNOSTIC`; and R5 Python SDK `PUBLISHED / PRIMARY`.

It authorizes none of ContextCompiler, ModelRouter, Retry/Quota Governor,
Recovery Engine, Reconciler, Repository/Git Adapter, Worktree Manager,
Publisher, Supervisor/runtime composition, automatic CLI fallback, R7, or F2E.
`auto_publish=false` remains binding.

## Correction.2 acceptance gates

Implementation is not ready for a fresh Correction.2 re-audit until all are
materially true:

```text
P0-2 actual typed policy decision is durable provider authorization: PASS
HUMAN_STOP provider calls: ZERO
synthetic decision identity: ABSENT
P0-3 typed provider observation and full nested validation: PASS
late receipt after finalization: REJECTED
P1-2 full compatibility matrix and PASS+P1 rejection: PASS
P1-3 exact residual typed contracts and N/N+1 evidence: PASS
generic/optional semantic authority: ABSENT
P1-4 decision-kind/transition-trigger SQLite binding: PASS
P1-4 terminal receipt lifecycle SQLite binding: PASS
P1-6 direct counterexample regressions and mutation sensitivity: PASS
P0-1 closed-finding regression: PASS
P1-1 closed-finding regression: PASS
P1-5 closed-finding regression: PASS
all seven goldens: PASS / UNCHANGED
R2-R5 regression: PASS
focused suite: PASS
complete suite: PASS
allowlist: 22 / EXACT
staging: EMPTY
```

Green aggregate counts are never sufficient by themselves. The correction
output must map each residual finding to the exact production test or direct
SQL probe that would fail under its reproduced defect.

## Explicit exclusions and stop conditions

Correction.2 does not authorize P2-1, the known R4 P2, redesign of P0-1/P1-1/
P1-5, retry/resume/recovery/fallback, a live provider, network, credentials,
Git mutation/publication, F2E, R7, or any path outside the 22-path allowlist.

Stop without implementation if provenance drifts, a 23rd path is required,
parent semantics would need to change, a closed finding would be redesigned,
typed observation cannot replace generic authority, the actual policy decision
cannot be persisted before provider start, SQLite cannot enforce either P1-4
invariant, or tests cannot directly detect each residual.

## Materialized authority state

```text
Correction.1: EXECUTED / REAUDIT_FAILED / PARTIAL_CLOSURE
Correction.1 closed findings: P0-1 / P1-1 / P1-5
Residual blocking findings: P0-2 / P0-3 / P1-2 / P1-3 / P1-4 / P1-6
Correction.2: MATERIALIZED / AUTHORITY_AUDIT_FAILED /
  AUTHORITY_CORRECTION_REQUIRED / NOT_EXECUTABLE
Correction.2 authority audit: FAIL / P0=0 / P1=2 / P2=0
Correction.2 authority residuals:
  P1-A / POLICY_COMPATIBILITY_MATRIX_AMBIGUITY
  P1-B / RESULT_IDEMPOTENCY_AUTHORITY_CONTRADICTION_NON_EXACTNESS
READY_TO_PUBLISH_R6_CORRECTION_2_AUTHORITY: NO
READY_TO_EXECUTE_CORRECTION_2: NO
READY_TO_ACCEPT_R6_IMPLEMENTATION: NO
R6: APPROVED / ACTIVE
R6 implementation: CORRECTION_REQUIRED / NOT_ACCEPTED / NOT_PUBLISHED
R6 P2-1: OPEN / NON_BLOCKING / CARRY_FORWARD
Known R4 P2: OPEN / NON_BLOCKING / CARRY_FORWARD
R7: NOT_AUTHORIZED
F2E: UNCHANGED / NOT_EXECUTED
auto_publish: false
Forward Lane: WAITING_FOR_MAIN
FORWARD_LANE_RESYNC_REQUIRED: NO
NEXT REQUIRED AUTHORITY ACTION:
  MATERIALIZE_AUTOPILOT_R6_CORRECTION_2_AUTHORITY_CORRECTION_1
```
