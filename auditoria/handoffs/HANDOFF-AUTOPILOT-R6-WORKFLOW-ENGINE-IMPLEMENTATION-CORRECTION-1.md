# FeelingPilates — HANDOFF: AUTOPILOT R6 Workflow Engine implementation Correction.1

**Authority role:** `R6_CORRECTION_AUTHORITY_MATERIALIZER /
IMPLEMENTATION_FINDING_MAPPER / REGRESSION_GATE_DESIGNER /
AUTHORITY_BOUNDARY_EDITOR`

**Publication role:** `R6_CORRECTION_AUTHORITY_PUBLISHER / AUDIT_PERSISTER /
LIFECYCLE_STATE_UPDATER / SCOPED_GIT_PUBLISHER`

**Workflow profile:** `DOCUMENTATION_PUBLICATION / EXACT_STAGE /
PRESERVE_DIRTY_IMPLEMENTATION / NO_IMPLEMENTATION / NO_R7 / NO_F2E`

## Lifecycle and sources

```text
Correction: AUTOPILOT-R6-WORKFLOW-ENGINE-IMPLEMENTATION-CORRECTION-1
Status: APPROVED / ACTIVE / PUBLISHED
Execution: P0-1_THROUGH_P1-3_CORRECTIONS_MATERIALIZED /
  P1-4_READY_TO_RESUME
Authority base: 6005319aebe7f23814f7d270555faa2f7cda03b4
Parent R6 authority: auditoria/handoffs/HANDOFF-AUTOPILOT-R6-WORKFLOW-ENGINE.md
Source audit: auditoria/reviews/AUTOPILOT-R6-WORKFLOW-ENGINE-IMPLEMENTATION-AUDIT.md
Source audit result: FAIL / P0=3 / P1=6 / P2=1
Fresh authority audit:
  auditoria/reviews/AUTOPILOT-R6-WORKFLOW-ENGINE-IMPLEMENTATION-CORRECTION-1-AUTHORITY-AUDIT.md
Fresh authority audit result: PASS / P0=0 / P1=0 / P2=0
Correction.1 authority: ACCEPTABLE
R6: APPROVED / ACTIVE
R6 implementation candidate: AUTHORIZED_DIRTY / AUDIT_FAILED /
  CORRECTION_REQUIRED / NOT_ACCEPTED / NOT_PUBLISHED
READY_TO_PUBLISH_R6_CORRECTION_1_AUTHORITY: SI / CONSUMED_BY_PUBLICATION
CORRECTION_1_AUTHORITY_PUBLICATION: COMPLETE / PUBLISHED
Index naming authority correction:
  APPROVED / ACTIVE / PUBLISHED / EXECUTABLE
P1_4_INDEX_NAMING_AUTHORITY: APPROVED / ACTIVE / EXECUTABLE
P1_4_INDEX_NAMING_FRESH_AUDIT: PASS / P0=0 / P1=0 / P2=0
P1_4_INDEX_NAMING_AUTHORITY_AUDIT:
  auditoria/reviews/AUTOPILOT-R6-CORRECTION-1-P1-4-INDEX-NAMING-AUTHORITY-AUDIT.md
P1_4_INDEX_NAME_AUTHORITY_GAP: CLOSED
READY_TO_CORRECT_R6_IMPLEMENTATION: SI
READY_TO_RESUME_P1_4: SI
READY_TO_ACCEPT_R6_IMPLEMENTATION: NO
R7: NOT_AUTHORIZED
F2E: UNCHANGED / NOT_EXECUTED
auto_publish: false
Forward Lane: WAITING_FOR_MAIN
Forward Lane resync required: NO
NEXT ACTION:
  RESUME_R6_CORRECTION_1_P1_4_RELATIONAL_AUTHORITY_IMPLEMENTATION
```

The fresh independent authority audit accepted this exact bounded handoff with
`P0=0 / P1=0 / P2=0`. Its competent documentation-only publication activates
Correction.1 for execution. This does not accept or publish the implementation;
the future corrector remains subject to the exact scope and a subsequent fresh
implementation re-audit.

## Physical provenance and candidate preservation

The pre-write physical verification established:

```text
Repository: /Users/jesusaldaircruzortiz/Desktop/Feelingpilates/feelingpilates-autopilot-r1
Branch: orquestacion/autopilot-r1
HEAD: 6005319aebe7f23814f7d270555faa2f7cda03b4
Resolved upstream: 6005319aebe7f23814f7d270555faa2f7cda03b4
Live remote: 6005319aebe7f23814f7d270555faa2f7cda03b4
Staging: EMPTY
Implementation candidate: AUTHORIZED_DIRTY
Candidate dirty paths: 19
Published implementation allowlist: 22
Candidate paths outside allowlist: 0
```

The implementation diff fingerprint is the lowercase SHA-256 of this
deterministic byte stream: tracked `git diff HEAD --binary --no-ext-diff --
tools/autopilot`, followed in bytewise sorted path order by an `UNTRACKED
<path>` marker and the `git hash-object` object ID for each untracked path under
`tools/autopilot`.

```text
Implementation diff fingerprint before:
229721e6e7d39a73aebc8e8cef25ca5324cfd3c19cfe5c308ac6d6ef8ccc4278

Implementation diff fingerprint after:
229721e6e7d39a73aebc8e8cef25ca5324cfd3c19cfe5c308ac6d6ef8ccc4278

Candidate preserved:
PASS
```

An authorized correction executor may now change only the exact Correction.1
scope within the unchanged 22-path allowlist. It may not normalize, revert, or
stage unrelated candidate content, and it may not publish its own correction.

## Exact correction scope

Correction.1 is bounded to these blocking audit findings:

```text
P0-1
P0-2
P0-3
P1-1
P1-2
P1-3
P1-4
P1-5
P1-6
```

```text
P2-1: NON_BLOCKING / CARRY_FORWARD / OUTSIDE_CORRECTION_1
R4 P2: UNCHANGED / OPEN / NON_BLOCKING / CARRY_FORWARD /
  OUTSIDE_R6_CORRECTION_SCOPE
R2-R5 authority: UNCHANGED
R6 semantic target and seven goldens: UNCHANGED
```

The correction closes implementation conformance gaps only. It may not change
the approved R6 meaning to fit the candidate.

## Exact implementation allowlist — unchanged 22 paths

Correction.1 preserves the published allowlist without expansion. A future
authorized corrector may create or modify only:

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
ALLOWLIST_COUNT: 22
ALLOWLIST_EXPANSION: NONE
WILDCARDS: NONE
001_initial.sql: IMMUTABLE / OUT_OF_SCOPE
```

If competent correction requires any different, renamed, substituted, or 23rd
path, stop with `R6_CORRECTION_ALLOWLIST_AUTHORITY_GAP`. Do not widen the scope
implicitly.

## P0-1 correction authority — semantic action re-entry

The corrector must make semantic action identity stable for one canonical
workflow/run/phase/durable-phase execution and independent of mutable
post-effect `runs.state_version`.

Before deriving or authorizing any new action, `WorkflowEngine` must resolve
the durable workflow control and the existing action/receipt/effect chain for
that canonical phase. SQLite must independently enforce exactly one authorized
action for the frozen phase/action identity.

The authoritative re-entry matrix is:

```text
effect durable                 -> start 0 / get_result 0 / new action 0 / new effect 0
receipt durable, effect absent -> start 0 / get_result 0 / finalize from receipt only
action durable, receipt absent -> start 0 / get_result 0 / no raw transfer /
                                  only authorized no-receipt uncertainty handling
no durable action              -> one new authorization may permit one start and one get_result
```

State-version changes cannot create another provider execution. No retry,
resume, polling, fallback, provider-history lookup, or Recovery Engine is
authorized.

Blocking evidence must use production `WorkflowEngine` and a file-backed
`SQLiteStateStore`, begin from the durable version after first effect
finalization, close/reopen where required, and prove the second call adds zero
starts, results, actions, and effects.

## P0-2 correction authority — one durable authorization transaction

`AgentExecutor.start` is forbidden until one atomic StateStore transaction has
committed all required authorization evidence.

The transaction must validate:

- the complete durable workflow definition, canonical bytes, and fingerprint;
- exact workflow/run/control/current phase/lifecycle/version identity;
- phase, gate, role, gate fact/outcome, typed `WorkflowPolicy` decision, and
  declared transition authority;
- the exact `PreparedExecutionSpecV1`, request fingerprint, local phase and
  deterministic durable phase identity;
- the deterministic action, R3 `Attempt(ordinal=0)`, and execution identities;
- the current active R3 lease with exact run/holder/resource/fencing token;
- unseen or exact-replay R3 idempotency identity/payload; and
- the accepted R3 run state and expected optimistic version.

The same transaction must atomically insert or update the complete typed
authorization decision, action authorization, R3 Attempt,
StateTransition/Checkpoint, R3 idempotency row, workflow control/lifecycle,
operational run state, and `runs.state_version N -> N+1`. Required definition,
run, control, lease, phase, gate, and prepared-spec authority must already
exist. IDs, durable phase key, canonical payload, fingerprints, transition and
checkpoint identity are deterministic; the injected Clock may supply only the
non-authoritative timestamp.

Commit returns the exact typed `WorkflowAuthorizationResultV1` frozen by the
parent handoff, including decision/action/attempt/execution/request identity,
committed version, `AUTHORIZED_NOT_OBSERVED`, and replay status. Exact replay
is read-only and never grants a second executor call. Any missing, stale,
foreign, conflicting, partial, or failed component produces no mutation and
`start=0 / get_result=0`. A `workflow_actions` row alone is explicitly
incompetent authorization.

## P0-3 correction authority — canonical receipt and derived effect

Receipt capture must accept the normalized provider-neutral observation as the
source fact and internally derive or verify, rather than trust caller-selected
values:

- canonical observation bytes and fingerprint;
- terminal/nonterminal classification;
- exact workflow/run/action/attempt/execution/request binding;
- receipt preimage, receipt ID, canonical payload, and payload fingerprint;
- required current capture lease/holder/resource/fence; and
- the exact R3 receipt idempotency identity and payload evidence.

Receipt and idempotency persistence are one transaction; no partial receipt or
marker may survive, and receipt capture does not change control version.
Caller-provided IDs, fingerprints, bytes, statuses, or payloads cannot override
canonical derivation. The recording fence is mandatory for every new effect
commit.

Effect finalization accepts no raw or caller-supplied observation. It loads the
immutable durable receipt internally and derives the only valid mapping:

```text
SUCCEEDED   -> OBSERVED_SUCCEEDED / semantic policy decision
FAILED      -> OBSERVED_FAILED / FAILED_SAFE
INTERRUPTED -> OBSERVED_INTERRUPTED / FAILED_SAFE
PENDING     -> UNCERTAIN_EFFECT / NONTERMINAL_OBSERVATION / FAILED_SAFE-compatible
RUNNING     -> UNCERTAIN_EFFECT / NONTERMINAL_OBSERVATION / FAILED_SAFE-compatible
UNKNOWN     -> UNCERTAIN_EFFECT / NONTERMINAL_OBSERVATION / FAILED_SAFE-compatible
no receipt  -> UNCERTAIN_EFFECT / NO_DURABLE_OBSERVATION_RECEIPT / FAILED_SAFE
```

Production StateStore/SQLite negatives must reject the audited fabricated
receipt and all materially adjacent status inversions, including `FAILED ->
OBSERVED_SUCCEEDED`, `SUCCEEDED -> OBSERVED_FAILED`, terminal ->
`NONTERMINAL_OBSERVATION`, and nonterminal -> observed terminal.

## P1-1 correction authority — complete definition and graph validation

Implement the exact `workflow-definition-v1` JSON shape and semantic authority
from the parent handoff. Validation must reject before hashing/persistence:

- noncanonical or duplicate workflow, phase, gate, and transition identities;
- unresolved or mismatched source/target phase, gate, or frozen role references;
- unknown triggers or invalid per-phase outgoing cardinality;
- invalid terminal/nonterminal, publication, correction, human-stop,
  failed-safe, or `NOT_APPLICABLE` edge shapes;
- cycles, unreachable phases, invalid terminal structure, and correction-budget
  or graph inconsistencies; and
- authority inversions such as `PASS -> HUMAN_STOP`.

Both schema and production semantic validator must be tested. The exact
canonical goldens remain unchanged.

## P1-2 correction authority — fact identity and contradiction matrix

`WorkflowPolicy` must validate the complete workflow/fingerprint/run/phase/
role/gate identity and the execution status, semantic status, finding counts,
correction shape, correction budget, and exhaustion facts. Foreign-run facts
and internal contradictions are rejected, not routed.

In particular, `PASS` with any P0/P1, human-decision requirement, incompatible
agent/execution status, or undeclared transition is a fail-closed contract
contradiction. Correctable P1 reaches `CORRECTION_REQUIRED` only under the exact
parent-handoff matrix, not merely because a correction path exists.

Policy remains pure, deterministic, provider-free, persistence-free, Git-free,
routing-free, and retry-free.

## P1-3 correction authority — exact immutable contracts

Replace generic `**values -> object` R6 surfaces with the parent-handoff's
immutable V1 records, including `WorkflowControlV1`, `GateFactV1`,
`WorkflowDecisionV1`, `WorkflowActionAuthorizationV1`,
`WorkflowObservationReceiptV1`, `WorkflowEffectEvidenceV1`, and the exact
authorization/receipt/effect result records and enums.

`StateStore` must expose all and only these nine typed R6 operations:

1. `save_workflow_definition_v1`
2. `initialize_workflow_control_v1`
3. `commit_workflow_decision_v1`
4. `authorize_workflow_action_v1`
5. `record_workflow_observation_receipt_v1`
6. `record_workflow_effect_v1`
7. `load_workflow_control_v1`
8. `load_workflow_action_effect_v1`
9. `load_workflow_observation_receipt_v1`

The parent handoff's exact field names, vocabularies, inputs, results, replay
behavior, and error semantics are binding. Accepted R2–R5 contracts must remain
compatible and cannot be redesigned.

## P1-4 correction authority — exact append-only migration 002

`002_workflow_engine.sql` must create the parent-handoff's exact nine tables:

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

It must also create exactly the three parent-candidate unique indexes, in the
required SQLite FK column order, for:

```text
attempts(run_id, phase_id, attempt_id, ordinal)
leases(lease_id, run_id, holder, protected_resource_key, fencing_token)
idempotency_records(idempotency_key, operation_kind,
                    canonical_operation_identity, payload_fingerprint)
```

Materialize every frozen column, PK, candidate UNIQUE key, composite FK,
decision/action and R3 attempt/execution/request binding, capture/recording
fence field, idempotency binding, lifecycle/status/terminal/uncertainty CHECK,
and control/version relationship. SQLite itself must reject cross-scope and
contradictory direct inserts. `001_initial.sql` and accepted R3 checksum/order/
reopen behavior remain unchanged.

### P1-4 supporting-index name authority clarification

The P1-4 executor stopped before modifying Migration 002 because the published
authority froze exact index structures but did not freeze exact index names,
while its instruction required exact names and prohibited inventing them. The
gap is persisted at
`auditoria/reviews/AUTOPILOT-R6-CORRECTION-1-P1-4-INDEX-NAMING-AUTHORITY-GAP.md`.

The fresh independent audit at
`auditoria/reviews/AUTOPILOT-R6-CORRECTION-1-P1-4-INDEX-NAMING-AUTHORITY-AUDIT.md`
passed with `P0=0 / P1=0 / P2=0`; competent publication makes this
clarification binding Correction.1 authority.

This binding clarification is:

```text
R6_SUPPORTING_INDEX_NAME_AUTHORITY:
  IMPLEMENTATION_DEFINED / NON_SEMANTIC

Index naming authority correction:
  APPROVED / ACTIVE / PUBLISHED / EXECUTABLE

P1_4_INDEX_NAMING_AUTHORITY:
  APPROVED / ACTIVE / EXECUTABLE

FRESH_AUDIT:
  PASS / P0=0 / P1=0 / P2=0

P1-4:
  READY_TO_RESUME / IMPLEMENTATION_FINDING_OPEN

READY_TO_RESUME_P1_4:
  SI
```

Exact names are not workflow semantics, durable identity, relational identity,
migration identity as independent semantic authority, application API, or
runtime contract. The implementation must choose valid SQLite identifiers that
are unique in the schema. Runtime and application logic must not depend on
them. After the corrected Migration 002 is accepted and published, the chosen
name spellings are fixed indirectly as part of its immutable migration bytes
and checksum authority.

The exact structural authority remains three explicit unique indexes and only
these three:

```text
Index A
  table: attempts
  unique: YES
  ordered columns: run_id, phase_id, attempt_id, ordinal
  name: IMPLEMENTATION_DEFINED

Index B
  table: leases
  unique: YES
  ordered columns: lease_id, run_id, holder,
                   protected_resource_key, fencing_token
  name: IMPLEMENTATION_DEFINED

Index C
  table: idempotency_records
  unique: YES
  ordered columns: idempotency_key, operation_kind,
                   canonical_operation_identity, payload_fingerprint
  name: IMPLEMENTATION_DEFINED
```

No fourth explicit R6 supporting index is authorized. No tuple may omit,
reorder, or add a column, and all three remain `UNIQUE`.

`sqlite_autoindex_*` indexes created internally by SQLite for table `PRIMARY
KEY` or `UNIQUE` constraints are not explicit Migration 002 supporting indexes
and are excluded from the explicit count of three.

P1-4 acceptance tests must introspect schema structure rather than assert
arbitrary names. They must prove exactly one explicit unique index for each
frozen table/ordered-column tuple, exactly three such indexes in total, and
failure for a missing tuple, reordered tuple, extra column, non-unique index,
or extra explicit supporting index. They may verify only that implementation-
defined identifiers are valid, schema-unique, and stable in the actual
Migration 002 bytes.

All other P1-4 requirements remain unchanged, including the exact nine tables,
columns, keys, composite FKs, R3 bindings, action/receipt/effect chain, fence
evidence, CHECK constraints, invalid-SQL matrix, reopen behavior, checksum,
and checksum drift. The 22-path allowlist remains exact and unchanged; no new
migration or implementation path is authorized.

## P1-5 correction authority — policy-driven atomic finalization

The finalization order is binding:

```text
load authoritative durable receipt
-> derive exact typed policy decision from receipt and durable workflow facts
-> validate the declared transition
-> atomically commit finalization
```

One transaction must commit effect, typed policy decision, declared
transition/Checkpoint, workflow control/lifecycle, mapped R3 operational state,
idempotency result, `runs.state_version N+1 -> N+2`, and mandatory recording-
fence evidence. It must be all-or-nothing under statement-boundary fault
injection. No effect may become durable first, and no effect-only authority may
survive.

## P1-6 correction authority — substantive production evidence

The corrected test authority must include:

- real engine plus file-backed SQLite current-version re-entry;
- pre-start inspection of complete durable authorization evidence;
- canonical receipt construction and fabricated/contradictory negatives;
- complete workflow graph and WorkflowPolicy identity/contradiction matrices;
- direct typed StateStore contract tests;
- exact migration columns, keys, FKs, indexes, CHECKs, and R3 bindings;
- effect/decision/control atomicity with statement-boundary fault injection;
- authorization, provider, receipt, effect, idempotency, and fencing crash
  windows;
- file close/reopen and later-owner receipt custody without provider replay;
- substantive accepted R2–R5 regression coverage; and
- independent constant goldens, never expectations generated tautologically by
  the production function being tested.

The previously weak enum-only, invalid-sandbox-only, fake-store re-entry, and
contradiction-permitting tests cannot remain the sole evidence.

## Fault-injection matrix

```text
before authorization commit
  -> provider 0 / no partial authority

after authorization commit, before provider
  -> complete durable authorization recoverable / provider not replayed

after provider, before receipt commit
  -> no trusted receipt / no raw observation transfer / provider not replayed

after receipt commit, before effect
  -> later current owner loads receipt and finalizes / provider not replayed

inside finalization transaction
  -> no partial effect, decision, transition, checkpoint, control, or version

idempotency conflict
  -> no mutation / provider 0 when pre-start

fencing conflict
  -> no mutation / provider 0 when pre-start
```

This matrix tests durable state. It does not authorize recovery orchestration,
retry, resume, polling, or provider-history reconciliation.

## Blocking acceptance gates before implementation re-audit

Implementation correction cannot be considered ready for fresh implementation
re-audit unless all are materially true:

```text
P0-1: production engine+SQLite re-entry CLOSED
P0-2: exact durable authorization transaction CLOSED
P0-3: canonical receipt and contradiction rejection CLOSED
P1-1: complete schema/graph validation CLOSED
P1-2: complete fact/contradiction validation CLOSED
P1-3: all exact typed domain/StateStore contracts CLOSED
P1-4: exact relational migration authority CLOSED
P1-5: policy-driven atomic finalization CLOSED
P1-6: substantive tests and fault injection CLOSED
all seven R6 goldens: UNCHANGED / PASS
focused corrected R6 suite: PASS
complete suite: PASS
substantive R2-R5 regression: PASS
provider at-most-once gate: PASS
durable pre-start authorization evidence gate: PASS
receipt contradiction gate: PASS
migration authority gate: PASS
no deferred capability leakage: PASS
allowlist: 22 / EXACT
staging: EMPTY
```

For one canonical workflow/run/phase action, cumulative
`AgentExecutor.start <= 1` and `get_result <= 1` across initial execution,
re-entry, store reopen, a new current owner/fence, durable effect, receipt-only,
and no-receipt states. Mutable state version never creates a second provider
execution.

## Path-to-finding boundary

The following allocation narrows use of the existing 22 paths; it does not
require every listed path to change.

| Finding | Authorized implementation areas inside the 22-path allowlist |
| --- | --- |
| P0-1 | workflow engine; domain action identity; StateStore/SQLite identity and loads; migration constraints; engine/effect/migration regressions |
| P0-2 | domain typed authorization records; StateStore port; SQLite transaction; engine sequencing; migration relations; contract/port/engine/fail-closed tests |
| P0-3 | canonical receipt domain; StateStore/SQLite receipt and effect APIs; engine finalization; receipt/effect schema; evidence/fail-closed tests |
| P1-1 | workflow JSON schema; domain graph validator; schema/contract/policy/fail-closed tests |
| P1-2 | pure workflow policy and typed facts; policy/contract/fail-closed tests |
| P1-3 | domain exports/records; StateStore typed port and SQLite implementation; port/contract tests |
| P1-4 | append-only migration 002; SQLite adapter; migration/effect/contract tests |
| P1-5 | engine, policy, typed effect/decision records, StateStore/SQLite finalization transaction, migration bindings, engine/effect/fault tests |
| P1-6 | only the already-allowlisted test files and minimum runtime-contract assertions needed for R2–R5/R6 regression; no P2-1 README wording correction |

No deferred subsystem may be introduced through an allowlisted file.
`tools/autopilot/README.md` remains in the preserved 22-path parent allowlist,
but Correction.1 does not authorize changing it to close P2-1.

## Explicit exclusions

Correction.1 does not authorize:

- P2-1 correction;
- the carried R4 P2 diagnostic correction;
- ContextCompiler, ModelRouter, Retry/Quota Governor, Recovery Engine,
  Reconciler, Repository/Git Adapter, Worktree Manager, Publisher, Supervisor,
  runtime composition, or automatic CLI fallback;
- automatic retry, replay, resume, polling, provider-history lookup, or
  fallback;
- any Git mutation or publication, including `git add`, commit, or push;
- F2E implementation/execution, runtime activation, productive authority,
  migration, fence, or cutover; or
- R7 or any later successor.

## Stop conditions

Stop without correction and report the exact blocker if:

- branch, authority base, upstream/live remote, staging, or candidate
  provenance drifts materially;
- the preserved candidate is modified before authority audit/execution;
- a 23rd/different implementation path is required
  (`R6_CORRECTION_ALLOWLIST_AUTHORITY_GAP`);
- another authority document is required outside the three-path
  materialization scope (`R6_CORRECTION_AUTHORITY_DOCUMENT_SCOPE_GAP`);
- a finding cannot be closed without changing the approved R6 semantics,
  weakening R2–R5, or absorbing a deferred capability;
- provider at-most-once or pre-start durable authorization cannot be proven;
- a raw observation or caller-selected receipt/effect can cross the custody
  boundary; or
- tests require a live provider, credentials, network, Git mutation,
  publication, or F2E.

## Published active authority state

```text
R6: APPROVED / ACTIVE
R6 implementation candidate: AUDIT_FAILED / CORRECTION_REQUIRED /
  NOT_ACCEPTED / NOT_PUBLISHED
Correction.1: APPROVED / ACTIVE / PUBLISHED / EXECUTABLE
Correction.1 fresh authority audit: PASS / P0=0 / P1=0 / P2=0
P2-1: OPEN / NON_BLOCKING / CARRY_FORWARD / OUTSIDE_CORRECTION_1
R4 P2: OPEN / NON_BLOCKING / CARRY_FORWARD / OUTSIDE_R6_CORRECTION_SCOPE
R2-R5 authority: UNCHANGED
Deferred capabilities authorized: NONE
READY_TO_CORRECT_R6_IMPLEMENTATION: SI
READY_TO_ACCEPT_R6_IMPLEMENTATION: NO
READY_TO_PUBLISH_R6_CORRECTION_1_AUTHORITY: SI / CONSUMED_BY_PUBLICATION
CORRECTION_1_AUTHORITY_PUBLICATION: COMPLETE / PUBLISHED
R7: NOT_AUTHORIZED
F2E: UNCHANGED / NOT_EXECUTED
auto_publish: false
Forward Lane: WAITING_FOR_MAIN
Forward Lane resync required: NO
NEXT ACTION: EXECUTE_R6_IMPLEMENTATION_CORRECTION_1_P0_1_THROUGH_P1_6
```

## P1-4 index naming authority gap closed — implementation ready to resume

```text
Correction.1 overall: ACTIVE
P0-1: CORRECTION_MATERIALIZED
P0-2: CORRECTION_MATERIALIZED
P0-3: CORRECTION_MATERIALIZED
P1-1: CORRECTION_MATERIALIZED
P1-2: CORRECTION_MATERIALIZED
P1-3: CORRECTION_MATERIALIZED
P1-4: READY_TO_RESUME / IMPLEMENTATION_FINDING_OPEN /
  AUTHORIZED_TO_RESUME
P1-5: OPEN
P1-6: OPEN
Index naming authority correction:
  APPROVED / ACTIVE / PUBLISHED / EXECUTABLE
P1_4_INDEX_NAME_AUTHORITY_GAP: CLOSED
P1_4_INDEX_NAMING_FRESH_AUDIT: PASS / P0=0 / P1=0 / P2=0
READY_TO_RESUME_P1_4: SI
READY_TO_ACCEPT_R6_IMPLEMENTATION: NO
R7: NOT_AUTHORIZED
auto_publish: false
Forward Lane: WAITING_FOR_MAIN
Forward Lane resync required: NO
NEXT ACTION:
  RESUME_R6_CORRECTION_1_P1_4_RELATIONAL_AUTHORITY_IMPLEMENTATION
```

The authority gap alone is closed. P1-4 remains an open implementation finding
and is now authorized to resume under this exact clarification. This
publication does not materialize, close, accept, or publish the P1-4
implementation and does not authorize P1-5, P1-6, or R7 work.
