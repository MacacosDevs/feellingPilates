# FeelingPilates — Autopilot: estado actual

**Status:** CANONICAL FOR AUTOPILOT HANDOFF STATUS

## R1 preserved

```text
AUTOPILOT R1: ACCEPTED / PUBLISHED / HISTORICAL
Runtime selection: PYTHON_SDK_PRIMARY
Primary execution: Codex SDK
Fallback: Codex CLI
Durable-state direction: SQLite through Python sqlite3 behind an abstraction
auto_publish: false
```

The historical Python orchestrator remains `REFERENCE / SELECTIVE_REUSE` only.
The historical F2D engine is `NOT_RUNTIME` for Autopilot.

## R2 and R3 historical closure; R4 activation pre-state preserved

```text
Target: AUTOPILOT R2 — Python bootstrap skeleton and core contracts
Active Autopilot handoff (historical at R2/R3 closure): NONE
R2: IMPLEMENTATION_COMPLETED / IMPLEMENTATION_AUDIT_PASS / PUBLISHED / PUBLICATION_CLOSURE_AUDIT_PASS / CLOSED / HISTORICAL
R2 active: NO
R2 implementation publication commit: ec440841889bcfc7cd73279a1219de4e84054b1f
R2 implementation findings: P0=0 / P1=0 / P2=1
R2 closure re-audit: P0=0 / P1=0 / P2=0
R2 material P1: ALL CLOSED
R2 Debt A — LeaseResolution.NONE / NO_RELEVANT_LEASE: CLOSED_BY_R3
R2 Debt B — malformed embedded usage_record behavioral validation: OPEN / NON_BLOCKING / CARRY_FORWARD / OUTSIDE_R3_SCOPE
R2 Debt C — attached branch without upstream behavioral coverage: OPEN / NON_BLOCKING / CARRY_FORWARD / OUTSIDE_R3_SCOPE
R3 handoff: APPROVED / CLOSED / HISTORICAL / NOT_ACTIVE
R3 target: AUTOPILOT R3 — SQLite durable state and recovery foundation
R3 target: IMPLEMENTED / PUBLISHED / COMPLETED
R3 implementation: ACCEPTED / PUBLISHED
R3 final implementation audit: P0=0 / P1=0 / P2=0 / PASS
R3 publication: COMPLETE / PUBLISHED
R3 publication commit: 8c46617ec2d6cc78593a883ea2f3d92217b9a0e0
R3 publication-closure audit.1 (historical): P0=0 / P1=1 / P2=0
R3 publication-closure P1-1 (historical): PUBLICATION_CLOSURE_LIFECYCLE_NOT_RECONCILED
R3 publication-closure Correction.1: MATERIALIZED
R3 final publication-closure re-audit: P0=0 / P1=0 / P2=0 / PASS
R3 publication-closure P1-1: CLOSED
R3 closure: CLOSED / HISTORICAL
R4 canonical target: AUTOPILOT R4 — Codex CLI adapter
R4 architectural role: AgentExecutor FALLBACK / DIAGNOSTIC
R4 authority handoff (historical pre-activation): MATERIALIZED / NOT_APPROVED / NOT_ACTIVE
R4 first handoff audit: P0=0 / P1=1 / P2=0
R4 P1-1: NEW_TURN_SESSION_EXTRACTION_CARDINALITY_AND_REQUIREDNESS_UNDEFINED
R4 P1-1 disposition: CORRECTED_BY_MATERIALIZED_CORRECTION / PENDING_FRESH_REAUDIT
R4 target (historical pre-activation): NOT_AUTHORIZED / NOT_STARTED
R4 implementation (historical pre-activation): NOT_AUTHORIZED
Active Autopilot handoff (historical pre-activation): NONE
R5: NOT_AUTHORIZED
Next permitted lifecycle action (historical pre-activation): FRESH_REAUDIT_AUTOPILOT_R4_HANDOFF
F2E: UNCHANGED
auto_publish: false
```

The exact 24-path R2 implementation allowlist is materialized and independently
accepted. Its implementation publication is
`ec440841889bcfc7cd73279a1219de4e84054b1f`; the closure documentation commit
does not replace that implementation publication identity. The fresh independent
publication-closure re-audit passed and R2 is now terminal, closed, and
historical. This does not authorize R3 implementation, F2E, runtime activation,
productive authority, or cutover.

## R2 P2-1 carry-forward test debt

R2 P2-1 was historical `OPEN / NON_BLOCKING / CARRY_FORWARD` test-coverage
debt. Later R3 implementation evidence now closes only Debt A, without
rewriting R2 history:

- A. `LeaseResolution.NONE / NO_RELEVANT_LEASE`: `CLOSED_BY_R3`. A concrete
  SQLite StateStore, real file-backed behavior, durable run, no relevant lease,
  close/reopen, and recovery by `run_id` produced `LeaseResolution.NONE` in the
  final independent R3 implementation audit.
- B. Malformed embedded `usage_record` behavioral validation remains
  `OPEN / NON_BLOCKING / CARRY_FORWARD / OUTSIDE_R3_SCOPE`:
  the underlying `UsageRecord`/`AgentResult` contract passed audit, but the
  behavioral test helper does not yet prove rejection of every malformed
  embedded `usage_record` payload.
- C. Attached branch without upstream behavioral coverage remains
  `OPEN / NON_BLOCKING / CARRY_FORWARD / OUTSIDE_R3_SCOPE`: the
  repository contract supports an attached branch with no upstream, but that
  exact behavioral case is not yet tested.

Debts B/C remain non-blocking and visible to a future competent Autopilot
hardening/testing phase; they must not be silently dropped.

## R3 durable-state implementation and publication lifecycle closed

```text
Handoff: auditoria/handoffs/HANDOFF-AUTOPILOT-R3-DURABLE-STATE.md
Target: AUTOPILOT R3 — SQLite durable state and recovery foundation
Type: IMPLEMENTATION / INFRASTRUCTURE / DURABILITY
Lifecycle: MATERIALIZED / APPROVED / ACTIVE (historical) / CLOSED / HISTORICAL
Target: IMPLEMENTED / PUBLISHED / COMPLETED
R3 implementation: ACCEPTED / PUBLISHED
Final fresh implementation re-audit: P0=0 / P1=0 / P2=0 / PASS
P1-1 through P1-8: CLOSED
Publication: COMPLETE / PUBLISHED
Publication commit: 8c46617ec2d6cc78593a883ea2f3d92217b9a0e0
Publication-closure audit.1 (historical): P0=0 / P1=1 / P2=0
Publication-closure P1-1 (historical): PUBLICATION_CLOSURE_LIFECYCLE_NOT_RECONCILED
Correction.1: MATERIALIZED
Final independent publication-closure re-audit: P0=0 / P1=0 / P2=0 / PASS
Publication-closure P1-1: CLOSED
Closure: CLOSED / HISTORICAL
R3 handoff: CLOSED / HISTORICAL / NOT_ACTIVE
Active Autopilot handoff (historical at R3 closure): NONE
R4 authority handoff (historical pre-activation): MATERIALIZED / NOT_APPROVED / NOT_ACTIVE
R4 first handoff audit: P0=0 / P1=1 / P2=0
R4 P1-1: CORRECTED_BY_MATERIALIZED_CORRECTION / PENDING_FRESH_REAUDIT
R4 target (historical pre-activation): AUTOPILOT R4 — Codex CLI adapter / NOT_AUTHORIZED / NOT_STARTED
R4 implementation (historical pre-activation): NOT_AUTHORIZED
R5: NOT_AUTHORIZED
Next permitted lifecycle action (historical pre-activation): FRESH_REAUDIT_AUTOPILOT_R4_HANDOFF
F2E: UNCHANGED
auto_publish: false
```

The accepted R3 implementation is the exact 14-path stdlib `sqlite3`
StateStore and recovery-foundation allowlist. It does not authorize extra
implementation paths, runtime database creation in the checkout,
workflow-engine policy, adapters beyond that allowlist, model invocation, Git
or worktree operations, supervision, F2E execution, runtime activation,
productive authority, or cutover.

The R3 handoff preserves all R2 contracts and R2's terminal status. The final
independent R3 implementation audit provides the competent later evidence to
mark Debt A `CLOSED_BY_R3`; B and C remain open outside R3 scope. Historical R2
evidence remains historical rather than being rewritten as if it existed then.

The successful gated push published the accepted R3 target and implementation
at `8c46617ec2d6cc78593a883ea2f3d92217b9a0e0`. The first fresh independent
publication-closure audit historically found `P0=0 / P1=1 / P2=0` with
`P1-1 — PUBLICATION_CLOSURE_LIFECYCLE_NOT_RECONCILED`; Correction.1 then
materialized the reconciliation. The final fresh independent
publication-closure re-audit recorded `P0=0 / P1=0 / P2=0`, closed P1-1, and
permitted this competent lifecycle closure. R3 is therefore closed and
historical, with no active Autopilot implementation handoff. The temporary DNS
unavailability during Correction.1 is historical operational evidence only;
it does not negate the completed publication.

```text
R1 P2-1 telemetry provenance gap:
CLOSED_AT_R2_CONTRACT_AUTHORITY
```

The corrected handoff requires direct evidence for every `OBSERVED` token class,
preserves unavailable classes as `null`, prohibits synthetic zeroes, fabricated
usage, and heuristic decomposition as observed usage, and separates any future
`DERIVED` or `ESTIMATED` metric from observed telemetry. This is contract
authority only; no telemetry implementation is complete or authorized.

## R4 authority lifecycle history — pre-activation state

```text
Handoff: auditoria/handoffs/HANDOFF-AUTOPILOT-R4-CODEX-CLI-ADAPTER.md
Target: AUTOPILOT R4 — Codex CLI adapter
Type: IMPLEMENTATION / INFRASTRUCTURE / FALLBACK_EXECUTION_ADAPTER authority
Lifecycle (historical pre-activation): MATERIALIZED / NOT_APPROVED / NOT_ACTIVE
Target authorization (historical pre-activation): NOT_AUTHORIZED / NOT_STARTED
Implementation authorization (historical pre-activation): NOT_AUTHORIZED
First fresh handoff audit: P0=0 / P1=1 / P2=0
P1-1: NEW_TURN_SESSION_EXTRACTION_CARDINALITY_AND_REQUIREDNESS_UNDEFINED
Correction.1: MATERIALIZED
P1-1 disposition: CORRECTED_BY_MATERIALIZED_CORRECTION / PENDING_FRESH_REAUDIT
Ready for fresh handoff re-audit: SI
Active Autopilot handoff (historical pre-activation): NONE
R5: NOT_AUTHORIZED
Next permitted lifecycle action (historical pre-activation): FRESH_REAUDIT_AUTOPILOT_R4_HANDOFF
```

The R4 handoff materializes only the future authority candidate for a Codex CLI
adapter behind the accepted `AgentExecutor` port. It preserves the Python SDK as
`PRIMARY` and limits Codex CLI to `FALLBACK / DIAGNOSTIC`; implementation order
cannot invert that architecture. The handoff defines an exact 13-path future
implementation allowlist, secure argv/no-shell execution, explicit cwd and
read-only/workspace-write sandbox mapping, strict structured-result validation,
and a deterministic new-turn session contract: zero events are allowed, one
unique valid ID is captured, repeated identical IDs are deduplicated, distinct
IDs and malformed recognized events fail closed, and a valid observed session
is preserved as operational evidence on later failure. Resume still requires
one caller-supplied validated identity. It also preserves bounded
timeout/cancellation and child cleanup, separate stdout/stderr evidence,
truthful telemetry, and secret/log boundaries.

The first fresh independent R4 handoff audit found only P1-1; Correction.1
materialized its deterministic session contract. That history is preserved: at
the time, materialization was not approval or activation. No R4 implementation
path had been changed, no active handoff existed, and only a fresh independent
re-audit could occur next. The handoff does not authorize the Python SDK adapter,
workflow engine, `ContextCompiler`, model router, Git/worktree adapters,
publisher, supervisor/launchd, R5+, F2E, runtime activation, productive
authority, or cutover.

R2 Debt A remains `CLOSED_BY_R3`. R2 Debt B remains
`OPEN / NON_BLOCKING / CARRY_FORWARD / POTENTIALLY_CLOSABLE_BY_R4 /
REQUIRES_IMPLEMENTATION_AND_FRESH_AUDIT`; R2 Debt C remains
`OPEN / NON_BLOCKING / CARRY_FORWARD / OUTSIDE_R4_SCOPE`.

## R4 handoff approved and active — preserved historical activation state

```text
Handoff: auditoria/handoffs/HANDOFF-AUTOPILOT-R4-CODEX-CLI-ADAPTER.md
Review: auditoria/reviews/AUTOPILOT-R4-CODEX-CLI-ADAPTER-HANDOFF-REAUDIT.md
R4 handoff: MATERIALIZED / APPROVED / ACTIVE
R4 handoff re-audit: PASS / P0=0 / P1=0 / P2=0
R4 P1-1: CLOSED_BY_FRESH_REAUDIT
R4 target: AUTOPILOT R4 — Codex CLI adapter
R4 target lifecycle: AUTHORIZED_TO_START / NOT_STARTED
R4 implementation: AUTHORIZED_TO_START
Active Autopilot handoff: R4 Codex CLI adapter
Primary executor: Python SDK
Codex CLI: FALLBACK / DIAGNOSTIC
R2 Debt A: CLOSED_BY_R3
R2 Debt B: OPEN / NON_BLOCKING / CARRY_FORWARD /
  POTENTIALLY_CLOSABLE_BY_R4 / REQUIRES_IMPLEMENTATION_AND_FRESH_AUDIT
R2 Debt C: OPEN / NON_BLOCKING / CARRY_FORWARD / OUTSIDE_R4_SCOPE
R3: CLOSED / HISTORICAL
R5: NOT_AUTHORIZED
F2E: UNCHANGED
auto_publish: false
Next permitted lifecycle action: EXECUTE_ACTIVE_AUTOPILOT_R4_CODEX_CLI_ADAPTER
```

This was activation authority only. The exact 13-path allowlist in the R4
handoff remained unchanged and implementation could not expand it. The later
independent R4 implementation audit supplied the evidence that closed Debt B;
Debt C remains outside R4 scope.

## F2E isolation

```text
F2E: UNCHANGED / OUTSIDE_AUTOPILOT_R4
```

No Autopilot R4 handoff action changes product code, TurnoInstructor authority,
the dark-launch state, or cutover.

## R4 publication lifecycle closed — authoritative current state

The exact, independently audited R4 Codex CLI adapter is accepted, published,
and closed by the fresh independent publication-closure audit at
`auditoria/reviews/AUTOPILOT-R4-PUBLICATION-CLOSURE-FINAL-AUDIT.md`. The final
fresh implementation re-audit remains
`auditoria/reviews/AUTOPILOT-R4-CODEX-CLI-ADAPTER-IMPLEMENTATION-FINAL-REAUDIT.md`.

```text
R1: HISTORICAL
R2: PUBLISHED / CLOSED / HISTORICAL
R2 Debt A: CLOSED_BY_R3
R2 Debt B: CLOSED_BY_R4
R2 Debt C: OPEN / NON_BLOCKING / CARRY_FORWARD / OUTSIDE_R4_SCOPE
R3: CLOSED / HISTORICAL
R4: IMPLEMENTED / ACCEPTED / PUBLISHED / CLOSED / HISTORICAL
R4 handoff: APPROVED / CLOSED / HISTORICAL / NOT_ACTIVE
Active Autopilot handoff: NONE
R4 target: IMPLEMENTED / PUBLISHED / COMPLETED
R4 implementation: ACCEPTED / PUBLISHED
R4 final implementation audit: PASS / P0=0 / P1=0 / P2=1
R4 final nonblocking P2: OPEN / NON_BLOCKING /
  CARRY_FORWARD / CAPABILITY_TIMEOUT_PRIMARY_CAUSE_MASKED_BY_PRE_REAP_GROUP_LIVENESS
R4 publication: COMPLETE / PUBLISHED
R4 publication commit: e7f8cb3a66560df6981a0e1bfb54d0e942348ff1
R4 publication-closure audit: PASS / P0=0 / P1=0 / P2=0 new findings
READY_TO_CLOSE_R4_PUBLICATION: SI
R4 closure: PASS / CLOSED / HISTORICAL
Primary executor: Python SDK / PRIMARY
Codex CLI: IMPLEMENTED / ACCEPTED / PUBLISHED / HISTORICAL R4 capability / FALLBACK / DIAGNOSTIC
Automatic fallback: NOT_IMPLEMENTED
Workflow engine: NOT_IMPLEMENTED
R5: NOT_STARTED / NOT_AUTHORIZED
F2E: UNCHANGED
auto_publish: false
Next permitted lifecycle action: MATERIALIZE_AUTOPILOT_R5_AUTHORITY
```

R2 Debt B is a competent later closure by R4, supplied by strict structured
usage parsing and deterministic malformed embedded `usage_record` behavioral
tests; R2 history remains unchanged. Debt C remains explicit, open,
non-blocking, carried forward, and outside R4 scope. The R4 P2 remains open and
non-blocking: its timeout diagnostic can select a pre-reap group-liveness cause
even though subsequent reaping and process-group cleanup succeed. It is a
diagnostic cause-ordering issue only. It remains open, non-blocking, and
carried forward; it does not authorize an implementation change, R5, or F2E
work.

## R5 publication lifecycle closed — authoritative current state

The fresh independent R5 handoff audit at
`auditoria/reviews/AUTOPILOT-R5-PYTHON-SDK-ADAPTER-HANDOFF-AUDIT.md` passed
with `P0=0 / P1=0 / P2=0`. Repository authority therefore approves and
activates the exact materialized handoff at
`auditoria/handoffs/HANDOFF-AUTOPILOT-R5-PYTHON-SDK-ADAPTER.md`.

The implementation then materialized within its exact 12-path allowlist. Its
initial fresh audit found `P0=1 / P1=6 / P2=1`; Corrections 1 through 5 and
their independent re-audits preserved that history and closed the resume
authority, lifecycle/containment, result cardinality/bounds, session
contradiction, real SDK capability, fake realism, installed schema,
incremental session, observer lifecycle, installed-test isolation, and
documentation findings. The final audit persisted at
`auditoria/reviews/AUTOPILOT-R5-PYTHON-SDK-ADAPTER-IMPLEMENTATION-AUDIT.md`
records `OPEN_P0=0 / OPEN_P1=0 / NEW_P0=0 / NEW_P1=0 / NEW_P2=0`, technical
`PASS`, and `READY_TO_ACCEPT_R5_IMPLEMENTATION=SI`.

The exact implementation was accepted and published at
`66aa13dae83d167d733d4d7371776cfd6b4484ef`. The bounded publication-closure
review persisted at
`auditoria/reviews/AUTOPILOT-R5-PUBLICATION-CLOSURE-FINAL-AUDIT.md` verifies
the exact `15 / 15` publication scope, reconciles the accepted evidence, finds
no post-publication regression evidence and no technical, publication, or
closure blocker, and records R5 as `COMPETENT_TO_CLOSE`. The competent
lifecycle action therefore closes R5 and retires its handoff as historical.

No canonical post-R5 successor is currently selected. This closure does not
select a component, label one R6, authorize successor implementation, execute
F2E, activate a workflow engine, implement automatic fallback, or change
`auto_publish=false`.

```text
R1: HISTORICAL
R2: PUBLISHED / CLOSED / HISTORICAL
R2 Debt A: CLOSED_BY_R3
R2 Debt B: CLOSED_BY_R4
R2 Debt C: OPEN / NON_BLOCKING / CARRY_FORWARD / OUTSIDE_R5_SCOPE
R3: CLOSED / HISTORICAL
R4: IMPLEMENTED / ACCEPTED / PUBLISHED / CLOSED / HISTORICAL
R4 handoff: APPROVED / CLOSED / HISTORICAL / NOT_ACTIVE
R4 P2 — CAPABILITY_TIMEOUT_PRIMARY_CAUSE_MASKED_BY_PRE_REAP_GROUP_LIVENESS:
  OPEN / NON_BLOCKING / CARRY_FORWARD / OUTSIDE_R5_SCOPE
Primary executor: Python SDK / PRIMARY
Python SDK adapter: IMPLEMENTED / ACCEPTED / PUBLISHED / PRIMARY
Codex CLI: IMPLEMENTED / PUBLISHED / HISTORICAL R4 capability / FALLBACK / DIAGNOSTIC
Automatic fallback: NOT_IMPLEMENTED / NOT_AUTHORIZED
Workflow engine: NOT_IMPLEMENTED / NOT_AUTHORIZED
R5 canonical target: AUTOPILOT R5 — Python SDK primary adapter
R5 handoff: MATERIALIZED / APPROVED / CLOSED / HISTORICAL / NOT_ACTIVE
R5 handoff audit: PASS / P0=0 / P1=0 / P2=0
R5: IMPLEMENTED / ACCEPTED / PUBLISHED / CLOSED / HISTORICAL
R5 target: IMPLEMENTED / ACCEPTED / PUBLISHED / COMPLETED
R5 implementation: ACCEPTED / PUBLISHED
R5 implementation audit: PASS / OPEN_P0=0 / OPEN_P1=0 / NEW_P0=0 / NEW_P1=0 / NEW_P2=0
R5 technical blockers: NONE
R5 publication: COMPLETE / PUBLISHED
R5 publication commit: 66aa13dae83d167d733d4d7371776cfd6b4484ef
R5 publication-closure review: PASS / COMPETENT_TO_CLOSE
R5 closure: COMPLETE / CLOSED / HISTORICAL
Active Autopilot handoff: NONE
R6: NOT_AUTHORIZED
Canonical post-R5 successor: NONE_CANONICALLY_SELECTED
Successor implementation authorized: NO
F2E: UNCHANGED
auto_publish: false
Next permitted lifecycle action: MATERIALIZE_POST_R5_SUCCESSOR_SELECTION_AND_BOUNDARY_AUTHORITY
```

R5 published the Python `openai-codex==0.147.0` adapter as the primary
implementation of the existing provider-neutral `AgentExecutor`. R4 remains a
separately selectable fallback/diagnostic capability; R5 contains no automatic
CLI fallback or workflow policy. The published implementation is limited to
the unchanged exact 12-path allowlist named in the now-historical R5 handoff.
That post-R5 successor-selection action was completed by the bounded R6
authority materialization. R5 remains closed and historical. The final fresh
R6 handoff audit has now passed, and the separately authorized authority
publication approves and activates the exact R6 handoff without claiming R6
implementation.

## R6 authority approved and active — authoritative current state

Repository evidence selects the workflow decision/authorization gap as the
next critical-path boundary. The exact candidate authority is materialized at
`auditoria/handoffs/HANDOFF-AUTOPILOT-R6-WORKFLOW-ENGINE.md`. The first fresh
independent handoff audit failed with four material contract-authority
findings. Correction.1 materialized an initial freeze; its fresh bounded
re-audit kept all four original P1 findings open with `NEW_P1=0` and confirmed
the 21-path allowlist, 97/97 R2-R5 regression, and the then-applicable
normative goldens.
Correction.2 materialized the transition, effect-policy, selector, namespace,
and initial relational authority. Its fresh re-audit closed P1-1 and P1-2,
kept P1-3 and P1-4 open, and reported `NEW_P1=0`. Correction.3 now freezes the
remaining observation-custody and R6 evidence-relational scope through a
durable `WorkflowObservationReceiptV1` and the exact authoritative chain
action authorization -> observation receipt -> effect. Raw observations are
not transferable across owners; accepted R3 usage/session/failure/artifact
records remain auxiliary evidence and are not effect-authority foreign-key
targets. The final bounded fresh re-audit persisted at
`auditoria/reviews/AUTOPILOT-R6-WORKFLOW-ENGINE-HANDOFF-AUDIT.md` closes P1-3
and P1-4, preserves P1-1 and P1-2 as closed, reports
`OPEN_P1=0 / NEW_P0=0 / NEW_P1=0 / NEW_P2=0`, and records `PASS` with
`READY_TO_APPROVE_AND_ACTIVATE_R6_HANDOFF=SI`.

Repository authority therefore approves and activates only the exact R6
Workflow Engine handoff and its unchanged 21-path future implementation
allowlist. This is authority publication: R6 remains not started and is not
claimed implemented, accepted, implementation-published, closed, or
historical.

```text
R3: CLOSED / HISTORICAL
R4: IMPLEMENTED / ACCEPTED / PUBLISHED / CLOSED / HISTORICAL
R5: IMPLEMENTED / ACCEPTED / PUBLISHED / CLOSED / HISTORICAL
R5 handoff: APPROVED / CLOSED / HISTORICAL / NOT_ACTIVE
Selected post-R5 successor: AUTOPILOT R6 — Workflow Engine
R6 handoff: MATERIALIZED / APPROVED / ACTIVE
R6 target: SELECTED / AUTHORIZED_TO_START / NOT_STARTED
R6 implementation: AUTHORIZED_TO_START
R6 initial handoff audit: FAIL / P0=0 / P1=4 / P2=0
R6 Correction.1: MATERIALIZED
R6 Correction.1 fresh re-audit: FAIL / P0=0 / OPEN_P1=4 / NEW_P1=0 / P2=0
R6 Correction.2: MATERIALIZED
R6 Correction.2 fresh re-audit: FAIL / P0=0 / OPEN_P1=2 / NEW_P0=0 / NEW_P1=0 / NEW_P2=0
P1-1 — WORKFLOW_DEFINITION_AND_CANONICALIZATION_AUTHORITY_UNFROZEN:
  CLOSED_BY_CORRECTION_2_FRESH_REAUDIT
P1-2 — ACTION_ATTEMPT_EXECUTION_AND_EFFECT_CONTRACT_UNFROZEN:
  CLOSED_BY_CORRECTION_2_FRESH_REAUDIT
R6 Correction.3: MATERIALIZED
R6 final two-finding fresh re-audit: PASS / OPEN_P1=0 / NEW_P0=0 / NEW_P1=0 / NEW_P2=0
P1-3 — OBSERVATION_CUSTODY_AUTHORITY_UNFROZEN: CLOSED
P1-4 — R6_EVIDENCE_RELATIONAL_SCOPE_UNFROZEN: CLOSED
P1-1/P1-2 regression: CLOSED / NO_REGRESSION
R6 handoff audit: PASS
R6 technical authority blockers: NONE
Active Autopilot handoff: R6 Workflow Engine
R4 P2 — CAPABILITY_TIMEOUT_PRIMARY_CAUSE_MASKED_BY_PRE_REAP_GROUP_LIVENESS:
  OPEN / NON_BLOCKING / CARRY_FORWARD / OUTSIDE_R6_SCOPE
R2 Debt C — attached branch without upstream behavioral coverage:
  OPEN / NON_BLOCKING / CARRY_FORWARD / OUTSIDE_R6_SCOPE
R7: NOT_AUTHORIZED
F2E: UNCHANGED
auto_publish: false
Next permitted lifecycle action: EXECUTE_ACTIVE_AUTOPILOT_R6_WORKFLOW_ENGINE
```

R6 is bounded to deterministic workflow/gate decisions, pre-effect durable
action authorization, same-owner normalization and durable receipt of an
`ExecutionObservation`, and durable decision/effect evidence through the
accepted `StateStore`. It does
not own context compilation, model/executor routing, automatic fallback,
retry/quota policy, recovery/reconciliation, Git/worktrees, publication,
supervision/launchd, F2E, productive authority, or cutover. The original
authority publication permitted implementation to start only within its exact
21 paths. The bounded start then exposed the implementation-allowlist gap
recorded below before any implementation change occurred.

Corrections 1, 2, and 3 preserve the already-audited target, ordinal,
critical-path selection, responsibility boundaries, existing exact goldens,
and exact 21-path future implementation allowlist. The final audit closes the
remaining two findings; this authority publication activates that bounded
implementation target without implementing it. R7 remains unauthorized, F2E
remains unchanged, and `auto_publish=false` remains binding.

## R6 implementation allowlist authority Correction.1 published — authoritative current state

The R6 implementation attempt stopped before any change after independently
identifying that its required update to
`tools/autopilot/config/runtime-contract.json` would truthfully change
`architecture.workflow_engine` from `NOT_IMPLEMENTED` to
`IMPLEMENTED_CANDIDATE`, while the existing accepted R4 regression test
`tools/autopilot/tests/test_codex_cli_command.py` asserts the old value and was
not included in the published 21-path R6 allowlist.

A repository-wide test search found no other assertion that necessarily
conflicts with this runtime-contract transition. The correction preserves all
21 published paths and adds only
`tools/autopilot/tests/test_codex_cli_command.py`, with authority limited to
the minimum expectation adjustment. R4 remains closed and historical; its CLI
behavior and regression protection remain unchanged. All audited R6 semantic
contracts and goldens remain unchanged.

The fresh bounded authority audit persisted at
`auditoria/reviews/AUTOPILOT-R6-ALLOWLIST-AUTHORITY-CORRECTION-1-AUDIT.md`
records `P0=0 / P1=0 / P2=0`, all required gates `PASS`, and
`READY_TO_PUBLISH_R6_ALLOWLIST_AUTHORITY_CORRECTION=SI`. The competent
publication action publishes the corrected 22-path implementation authority
and removes the bounded allowlist blocker.

```text
R4: IMPLEMENTED / ACCEPTED / PUBLISHED / CLOSED / HISTORICAL
R4 CLI behavior: UNCHANGED
R4 regression protection: PRESERVED
R4 P2 — CAPABILITY_TIMEOUT_PRIMARY_CAUSE_MASKED_BY_PRE_REAP_GROUP_LIVENESS:
  OPEN / NON_BLOCKING / CARRY_FORWARD / OUTSIDE_R6_SCOPE
R5: IMPLEMENTED / ACCEPTED / PUBLISHED / CLOSED / HISTORICAL
R6 handoff: MATERIALIZED / APPROVED / ACTIVE
R6 target: SELECTED / AUTHORIZED_TO_START / NOT_STARTED
R6 implementation authority: AUTHORIZED_TO_START
R6 implementation execution: AUTHORIZED_TO_RESUME
R6 initial implementation attempt: BLOCKED_BEFORE_ANY_CHANGE / HISTORICAL
R6 implementation blocker: NONE
R6 previous implementation allowlist: 21 PATHS
R6 corrected future implementation allowlist: 22 PATHS
R6 allowlist authority Correction.1: AUDITED / PUBLISHED
R6 allowlist authority Correction.1 audit: PASS / P0=0 / P1=0 / P2=0
R6 allowlist Correction.1 added path:
  tools/autopilot/tests/test_codex_cli_command.py
Other required paths discovered: NONE
R6 semantic contracts: UNCHANGED
R6 goldens: UNCHANGED
Active Autopilot handoff: R6 Workflow Engine
R7: NOT_AUTHORIZED
F2E: UNCHANGED
auto_publish: false
Next permitted lifecycle action: RESUME_ACTIVE_AUTOPILOT_R6_WORKFLOW_ENGINE_IMPLEMENTATION
```

The corrected 22-path allowlist is independently audited and published as
implementation authority. R6 remains active and implementation may resume.
This correction publication does not itself modify `tools/autopilot`,
implement R6, reopen R4, authorize R7, or execute F2E.
