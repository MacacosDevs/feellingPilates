# FeelingPilates — Autopilot: estado actual

**Status:** CANONICAL FOR AUTOPILOT HANDOFF STATUS

## R1 preserved

```text
AUTOPILOT R1: ACCEPTED / PUBLISHED
Runtime selection: PYTHON_SDK_PRIMARY
Primary execution: Codex SDK
Fallback: Codex CLI
Durable-state direction: SQLite through Python sqlite3 behind an abstraction
auto_publish: false
```

The historical Python orchestrator remains `REFERENCE / SELECTIVE_REUSE` only.
The historical F2D engine is `NOT_RUNTIME` for Autopilot.

## Current R2 handoff

```text
Target: AUTOPILOT R2 — Python bootstrap skeleton and core contracts
Active handoff: auditoria/handoffs/HANDOFF-AUTOPILOT-R2-BOOTSTRAP-SKELETON.md
Handoff: MATERIALIZED / APPROVED / ACTIVE
Target: AUTHORIZED_TO_START / NOT_STARTED
R2 implementation: AUTHORIZED_TO_START
Next allowed action: EXECUTE_ACTIVE_AUTOPILOT_R2_BOOTSTRAP_SKELETON
R3: NOT_AUTHORIZED
```

This activation authorizes only the exact R2 implementation target and its
existing audited allowlist. It does not create `tools/autopilot/`, install
packages, invoke an SDK, create SQLite state, execute a workflow, publish,
activate runtime, complete the target, or authorize R3 or cutover.

```text
R1 P2-1 telemetry provenance gap:
CLOSED_AT_R2_CONTRACT_AUTHORITY
```

The corrected handoff requires direct evidence for every `OBSERVED` token class,
preserves unavailable classes as `null`, prohibits synthetic zeroes, fabricated
usage, and heuristic decomposition as observed usage, and separates any future
`DERIVED` or `ESTIMATED` metric from observed telemetry. This is contract
authority only; no telemetry implementation is complete or authorized.

## F2E isolation

```text
F2E: UNCHANGED / OUTSIDE_AUTOPILOT_R2
```

No Autopilot R2 handoff action changes product code, TurnoInstructor authority,
the dark-launch state, or cutover.
