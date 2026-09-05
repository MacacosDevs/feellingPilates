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
