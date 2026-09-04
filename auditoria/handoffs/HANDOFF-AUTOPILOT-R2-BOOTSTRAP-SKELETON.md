# FeelingPilates — HANDOFF: AUTOPILOT R2 bootstrap skeleton and core contracts

**Role for a future execution:** `AUTOPILOT_R2_EXECUTOR / PYTHON_BOOTSTRAP_CONTRACT_MATERIALIZER`

**Workflow profile:** implementation/infrastructure; fresh independent handoff
re-audit passed and explicit activation is recorded.

## Authority and bounded target

This handoff materializes the authority for the first real Autopilot code phase:

```text
AUTOPILOT R2 — Python bootstrap skeleton and core contracts
```

Its purpose is to create the smallest versioned Python skeleton that freezes a
package boundary, language-neutral domain contracts, execution and repository
ports, a durable-state port, runtime baselines, initial machine-readable
schemas, telemetry provenance semantics, dependency direction, and stdlib-only
unit/contract tests.

R2 must preserve the accepted R1 decision:

```text
AUTOPILOT R1: ACCEPTED / PUBLISHED
Runtime: PYTHON_SDK_PRIMARY
Primary execution: openai-codex SDK
Fallback: Codex CLI
Durable state: SQLite through Python sqlite3 behind StateStore
Historical Python orchestrator: REFERENCE / SELECTIVE_REUSE
Historical F2D engine: NOT_RUNTIME
auto_publish: false
```

R1 P2-1 is binding for R2. Its closure is
`CLOSED_AT_R2_CONTRACT_AUTHORITY`: the future contract must preserve `null` for
unavailable classes, observed-zero semantics, no fabricated usage, no synthetic
zero, no heuristic decomposition presented as observed, direct evidence for
`OBSERVED` provenance, and explicit separation for any future
`DERIVED`/`ESTIMATED` values. This closes the contract gap only; it does not
claim that telemetry implementation is complete.

## Required pre-flight for a future R2 executor

Before any R2 write, independently verify the branch, `HEAD`, resolved upstream,
staging, working tree, this handoff, the current Autopilot status, protocol
authority, and the fresh handoff-audit result. Do not repair a mismatch.

The R2 implementation begins only after all of the following exist:

1. This handoff is materialized.
2. A fresh independent audit of this handoff is recorded.
3. An explicit activation authorizes the exact R2 target.

The eventual implementation then requires its own tests and fresh independent
implementation audit. Publication remains separately gated; `auto_publish=false`
does not change.

## Exact future R2 implementation allowlist

A future R2 executor may create or modify only these paths:

```text
tools/autopilot/README.md
tools/autopilot/pyproject.toml
tools/autopilot/config/runtime-contract.json
tools/autopilot/schemas/agent-result.schema.json
tools/autopilot/schemas/checkpoint.schema.json
tools/autopilot/schemas/usage-record.schema.json
tools/autopilot/schemas/workflow.schema.json
tools/autopilot/src/feelingpilates_autopilot/__init__.py
tools/autopilot/src/feelingpilates_autopilot/domain/__init__.py
tools/autopilot/src/feelingpilates_autopilot/domain/models.py
tools/autopilot/src/feelingpilates_autopilot/domain/states.py
tools/autopilot/src/feelingpilates_autopilot/domain/events.py
tools/autopilot/src/feelingpilates_autopilot/domain/failures.py
tools/autopilot/src/feelingpilates_autopilot/ports/__init__.py
tools/autopilot/src/feelingpilates_autopilot/ports/agent_executor.py
tools/autopilot/src/feelingpilates_autopilot/ports/state_store.py
tools/autopilot/src/feelingpilates_autopilot/ports/repository.py
tools/autopilot/src/feelingpilates_autopilot/ports/clock.py
tools/autopilot/tests/__init__.py
tools/autopilot/tests/test_imports.py
tools/autopilot/tests/test_domain_contracts.py
tools/autopilot/tests/test_port_boundaries.py
tools/autopilot/tests/test_json_schemas.py
tools/autopilot/tests/test_runtime_contract.py
```

No other path is within the R2 implementation scope.

## Frozen runtime contract

The future `tools/autopilot/config/runtime-contract.json` is a machine-readable,
tested baseline. It must not require that the SDK be installed during R2. It
must record at least:

```text
runtime: Python 3.14-compatible
primary_sdk.package: openai-codex
primary_sdk.tested_version: 0.147.0
fallback_cli: codex-cli
fallback_cli.tested_version: 0.150.1
state_backend: sqlite3 stdlib
state_backend.tested_sqlite_baseline: 3.53.3
authentication: ChatGPT local Codex login supported by R1 evidence
auto_publish: false
telemetry.unavailable_token_classes: null
telemetry.synthetic_zero_for_unavailable: forbidden
telemetry.fabricated_usage: forbidden
telemetry.heuristic_decomposition_as_observed: forbidden
telemetry.observed_value_requires_direct_evidence: true
telemetry.derived_or_estimated_usage_must_be_explicitly_labeled: true
```

Actual future adapters still must perform startup capability validation. SDK
pinning lives in this tested contract; the R2 `pyproject.toml` must not add a
production `openai-codex` dependency or unrelated dependency.

## Dependency boundary and domain contracts

The dependency direction is fixed as:

```text
domain
  ↑
ports
  ↑
future application/adapters
```

R2 contains only domain, ports, and contracts. The domain must not import
`openai_codex`, a subprocess Codex implementation, a SQLite adapter
implementation, Git implementation, launchd, or product source. Ports must
use internal language-neutral contracts and must not expose SDK-native session
or result types.

Sessions are operational references, never canonical project authority.

The future `AgentExecutor` port must be suitable for both the Python Codex SDK
adapter and Codex CLI fallback adapter, and conceptually support:

```text
capabilities
start
resume
interrupt
```

The future `StateStore` is only a durable-state abstraction. It must be able to
represent contracts/typed identifiers for workflow, run, phase, attempt,
transition, checkpoint, artifact, session, usage, failure, lease, and human
decision. R2 must not create database tables; SQLite migrations and the SQLite
implementation belong to a later authorized phase.

The repository and clock ports are contracts only. R2 must not implement Git,
worktree management, or time-driven supervision.

## Operational states and failure taxonomy

Operational state and work/phase kind are separate dimensions. R2 must freeze,
at minimum, these operational states:

```text
PENDING
RUNNING
WAITING_FOR_QUOTA
WAITING_FOR_NETWORK
RECOVERING
REVIEWING
CORRECTING
PAUSED
BLOCKED
HUMAN_DECISION_REQUIRED
FAILED_SAFE
COMPLETED
```

The language-neutral failure taxonomy must include at least:

```text
SDK_TRANSPORT_FAILURE
SDK_SESSION_FAILURE
CLI_PROCESS_FAILURE
NETWORK_UNAVAILABLE
QUOTA_EXHAUSTED
RESULT_CONTRACT_FAILURE
PROCESS_CRASH
UNCERTAIN_WRITE
GIT_BASELINE_DRIFT
TEST_FAILURE
SECURITY_STOP
```

No adapter-specific literal error string belongs in domain authority.

## Initial structured contracts and telemetry

The initial versioned, machine-validatable schemas are:

```text
agent-result.schema.json
checkpoint.schema.json
usage-record.schema.json
workflow.schema.json
```

They must express structure rather than turn arbitrary prose into workflow
transitions. R2 validates them with available standard-library mechanisms where
possible and must not add a third-party JSON Schema runtime merely for R2.

`usage-record.schema.json` must preserve the provenance of token measurements.
It must permit each unavailable field below to be `null`:

```text
input_tokens
cached_input_tokens
output_tokens
reasoning_output_tokens
total_tokens
```

`null` means `UNAVAILABLE / NOT OBSERVED`; `0` means `OBSERVED_ZERO`. They
must not be conflated. A zero is permitted only when the selected execution
adapter, runtime, or provider directly exposes evidence that the exact token
class is zero. Zero must never mean unknown, missing, not emitted, unsupported,
not observable, or not applicable by assumption.

A token value may be represented as `OBSERVED` only when the selected execution
adapter, runtime, or provider directly exposes evidence attributable to that
exact token class. Direct telemetry for `input_tokens`, `cached_input_tokens`,
`output_tokens`, `reasoning_output_tokens`, or `total_tokens` is qualifying
evidence for that class only; the future field set may expand without weakening
this rule. If an adapter does not directly expose a class, that class remains
`null` and must not be populated from another metric.

It is forbidden to present any heuristic, estimated, reconstructed, inferred,
allocated, or decomposed value as `OBSERVED` usage. This includes heuristic or
estimated decomposition; arithmetic or proportional allocation from a total;
reconstruction from another token class; inference from pricing, context length,
request/response byte size, another model or adapter, or a model-generated
estimate; and reconstructed cached-token or reasoning-token values. For example,
when directly observed `total_tokens = 1000` is the only adapter evidence,
`total_tokens` may be populated but `input_tokens` and `output_tokens` must
remain `null`; assigning `600` and `400` as observed values is forbidden.

The future UsageRecord contract must distinguish at minimum `OBSERVED` and
`UNAVAILABLE`. If a later authorized phase adds derived or estimated metrics,
they must be explicitly classified `DERIVED` or `ESTIMATED`, retain their
derivation/provenance, remain distinguishable from observed measurements, never
silently replace a null observed token field, and never be used to claim
adapter-observed usage. R2 does not implement such derived telemetry.

For an aggregate-only adapter, preserve an aggregate field only when it is
directly observed, leave every unavailable component class `null`, do not
heuristically decompose the aggregate, and do not synthesize component zeroes.
The normalized contract preserves truthful differences in adapter granularity;
it does not force every adapter to populate the same fields. The schema and
tests must reject these fabrication semantics wherever mechanically
representable.

## Future R2 tests

R2 tests are stdlib-only unless later repository authority says otherwise.
Prefer `python -m unittest`; execute no Codex call, network operation, Git
mutation, SQLite production database, Spring test, or F2E target.

Required coverage is:

```text
package imports
operational-state invariants
failure taxonomy
port dependency isolation
runtime-contract parsing
schema structure
telemetry null-versus-zero semantics
telemetry provenance and aggregate-only adapter semantics
```

The R2 contract tests must cover at least:

```text
CASE A — aggregate-only evidence: directly observed total is populated;
         unavailable component classes are null.
CASE B — observed zero: directly reported output_tokens = 0 is permitted as
         OBSERVED_ZERO.
CASE C — missing output class: output_tokens remains null; it must not become
         0 merely because it is unavailable.
CASE D — heuristic decomposition: directly observed total_tokens = 1000 does
         not permit input_tokens = 600 and output_tokens = 400 as OBSERVED;
         such normalization is rejected.
CASE E — future derived/estimated value: if authorized later, it is explicitly
         labeled DERIVED or ESTIMATED and does not replace a null observed field.
```

## Explicit exclusions and isolation

R2 must not implement the Codex SDK adapter, Codex CLI adapter, SQLite database
implementation, workflow engine, context compiler, worktree manager,
reconciler, publisher, process supervisor, launchd, model-routing execution,
or an actual LLM invocation. It must not create SQLite state, install packages,
or modify product/F2E work.

The current F2E target is outside this handoff. Do not copy, modify, or execute
it; do not change TurnoInstructor authority or cutover. The untracked R1 source
is not implementation input except for any narrowly necessary invariance check.

The fresh independent handoff re-audit is recorded at
`auditoria/reviews/AUTOPILOT-R2-BOOTSTRAP-SKELETON-HANDOFF-REAUDIT.md`.
Activation authorizes only the exact R2 target and its existing implementation
allowlist. The next allowed action is
`EXECUTE_ACTIVE_AUTOPILOT_R2_BOOTSTRAP_SKELETON`. This activation is neither
implementation nor evidence of an R2 test, publication, runtime operation,
target completion, or R3 authority.

## Handoff lifecycle and next action

```text
MATERIALIZED
APPROVED
ACTIVE
TARGET_AUTHORIZED_TO_START
IMPLEMENTATION_AUTHORIZED_TO_START
NOT_STARTED
```
