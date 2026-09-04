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
R2 implementation: IMPLEMENTATION_ACCEPTED
R2 implementation audit: P0=0 / P1=0 / P2=1
R2 material P1: ALL CLOSED
R2 target: COMPLETED / READY_TO_PUBLISH
R2 publication: NOT_YET_CONFIRMED
P2-1: OPEN / NON_BLOCKING TEST DEBT
Next allowed action after this commit is successfully pushed: CONFIRM_R2_PUBLICATION_AND_CLOSE
R3: NOT_AUTHORIZED
auto_publish: false
```

The exact 24-path R2 implementation allowlist is materialized and independently
accepted. Its publication is intentionally not yet confirmed: successful remote
push and a later bounded closure action are required. This does not authorize
R3, F2E, runtime activation, productive authority, or cutover.

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
