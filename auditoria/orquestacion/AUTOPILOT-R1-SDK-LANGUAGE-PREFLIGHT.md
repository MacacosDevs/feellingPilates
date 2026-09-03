# FeelingPilates — AUTOPILOT-R1-DUAL-SDK-LANGUAGE-PREFLIGHT

**Status:** R1 materialized; no workflow engine implemented.

**Date:** 2026-09-03

**Evidence vocabulary:** **DOCUMENTED** means a public package manifest/README or CLI help exposed the capability. **OBSERVED** means this R1 executed it. **INFERRED** is a bounded conclusion from those facts. **UNKNOWN** is intentionally not claimed.

## Baseline

**OBSERVED before any write:**

| Check | Required | Observed |
| --- | --- | --- |
| Feature worktree | `/Users/jesusaldaircruzortiz/Desktop/Feelingpilates/feelingpilates` | exact path |
| Branch | `operacion/excepciones-horario-fecha` | exact |
| `HEAD` | `feb9d6e9abf01fe93a03367769156650d7956b53` | exact |
| Upstream resolved SHA | `feb9d6e9abf01fe93a03367769156650d7956b53` | exact |
| Staging | empty | empty |
| Working tree | `?? auditoria/handoffs/HANDOFF-F2E-R1-RESERVA-READER-JPA-READ-ONLY.md` | exact and only entry |
| Handoff SHA-256 | `b65965288c0840934f4db301b7d81efb5ac818640958863902e62ea7f4897185` | exact |

The complete required protocol authority was read from this R1 worktree: `AGENTS.md`, plus `README.md`, `WORKFLOW.md`, `STATE-MACHINE.md`, `GATES.md`, and `ROLES.md` under `auditoria/orquestacion/`. This is a documentation-only preflight; F2E was not executed or advanced.

## Worktree

**OBSERVED:** a normal Git operation created `/Users/jesusaldaircruzortiz/Desktop/Feelingpilates/feelingpilates-autopilot-r1` and new branch `orquestacion/autopilot-r1` directly from `feb9d6e9abf01fe93a03367769156650d7956b53`.

Immediately after creation, the Autopilot worktree had the required branch and HEAD, empty staging, and a clean working tree. The feature worktree was not checked out, moved, copied, stashed, reset, cleaned, staged, or otherwise modified.

## Host

| Component | Observed version / state |
| --- | --- |
| Python | `3.14.6` |
| Node.js | `v24.18.0` |
| npm | `11.16.0` |
| Codex CLI | `codex-cli 0.150.1` |
| Local account | `codex login status` reported `Logged in using ChatGPT` |
| Node SQLite | built-in `node:sqlite` import succeeds |

## SDK versions

### Python

| Field | Evidence |
| --- | --- |
| Candidate | `openai-codex` |
| Stable version tested | `0.147.0` |
| Registry result | **OBSERVED:** PyPI listed `0.147.0`, then `0.144.4`; no prerelease was selected. |
| Isolated installation | **PASS:** `/tmp/feelingpilates-r1-python.hfSeXY/venv`; not in either Git worktree. |
| Runtime relationship | **OBSERVED:** requires `pydantic>=2.12` (installed `2.13.5`) and bundled `openai-codex-cli-bin==0.147.0`. The package launches its bundled Codex app-server over stdio JSON-RPC. |
| Published surface used | **DOCUMENTED/OBSERVED:** `Codex`, `thread_start`, `thread_resume`, `Thread.run`, `Thread.turn`, `TurnHandle.interrupt`, `Sandbox`, typed `TurnResult`. |

### TypeScript

| Field | Evidence |
| --- | --- |
| Candidate | `@openai/codex-sdk` |
| Stable version tested | `0.153.1` |
| Registry result | **OBSERVED:** npm `latest=0.153.1`; `alpha=0.154.0-alpha.2` was deliberately not selected. |
| Isolated installation | **PASS:** `/tmp/feelingpilates-r1-typescript.3EcACg`; not in either Git worktree. |
| Runtime relationship | **OBSERVED:** depends on `@openai/codex@0.153.1` and its platform CLI binary. The SDK spawns `codex exec --experimental-json` and exchanges JSONL. |
| Published surface used | **DOCUMENTED/OBSERVED:** `Codex.startThread`, `resumeThread`, `Thread.run`, `runStreamed`, `outputSchema`, `AbortSignal`, sandbox and model options. |

Neither candidate was globally installed. No prerelease, application dependency, lockfile, package manifest, requirements file, or production code was added to FeelingPilates.

## Authentication

All SDK probes were launched with `OPENAI_API_KEY` and `CODEX_API_KEY` removed from the probe process environment. Credentials, cookies, headers, and credential files were neither read nor printed.

| SDK | `CHATGPT_LOCAL_AUTH_SUPPORTED` | Evidence |
| --- | --- | --- |
| Python | **YES** | **OBSERVED:** `Codex.account()` returned normally and authenticated thread/turn calls completed using the existing local Codex state. |
| TypeScript | **YES** | **OBSERVED:** authenticated `codex exec` calls completed through the SDK with no supplied API key. |

The initial sandboxed host attempt could not create Codex state runtime data under the existing local Codex directory and raised a typed Python `TransportClosedError`. The authorized host execution then completed normally. This is an environment permission characteristic, not an authentication failure, and it is relevant to launchd/R2 deployment design.

## SDK contract matrix

`PASS` is observed in the isolated probes. `DOCUMENTED` was established from the installed SDK source/README. `ADAPTER_REQUIRED` means the R2 core must normalize the SDK-specific representation.

| Requirement | Python `openai-codex` | TypeScript `@openai/codex-sdk` |
| --- | --- | --- |
| A. Stable install | PASS | PASS |
| B. Existing ChatGPT authentication | PASS | PASS |
| C. New thread/session | PASS | PASS |
| D. Durable thread ID | PASS | PASS |
| E. Same-process continuation | PASS (`SAME_PROCESS_MARKER`) | PASS (`SAME_PROCESS_MARKER`) |
| F. Resume in a new process | PASS | PASS |
| G. Machine-usable result | PASS: typed `TurnResult` | PASS: `Turn` object and JSONL events |
| H. Structured JSON | PASS: `output_schema` | PASS: `outputSchema` |
| I. Model selection | PASS: server listed Luna/Terra/Sol; Luna selected for probes | PASS: `model` is passed to CLI; Luna selected for probes |
| J. Reasoning effort | PASS: `effort='low'` | PASS: `modelReasoningEffort='low'` |
| K. Read-only sandbox | PASS | PASS |
| L. Workspace-write sandbox in disposable workspace | PASS | PASS |
| M. Interruption/cancellation | PASS: `TurnHandle.interrupt()` ended a turn as `interrupted` | PASS: `AbortSignal` produced `AbortError` |
| N. Client process exit followed by resume | PASS | PASS |
| O. Streaming/event information | PASS: thread notifications | PASS: JSONL `thread.started`, `turn.started`, item, and completion events |
| P. Token/usage information | PASS, aggregate typed usage | PASS, token-field usage object |
| Q. Normalized errors | PASS: typed RPC/transport errors | ADAPTER_REQUIRED: JavaScript `Error` plus turn-failure/error events |
| R. Bounded worker `--once` compatibility | PASS: synchronous one-turn API | PASS: one `run()` call per worker process |

### Models and reasoning

**OBSERVED through the authenticated Python model-list API:**

| Logical model | Accepted configured identifier | Supported efforts reported |
| --- | --- | --- |
| LUNA | `gpt-5.6-luna` | `low`, `medium`, `high`, `xhigh`, `max` |
| TERRA | `gpt-5.6-terra` | `low`, `medium`, `high`, `xhigh`, `max`, `ultra` |
| SOL | `gpt-5.6-sol` | `low`, `medium`, `high`, `xhigh`, `max`, `ultra` |

Luna/low was the only model actually consumed for SDK smoke, sandbox, resume, and interruption probes. Terra and Sol were not used for trivial probes, by design. TypeScript has no public model-list method in the tested surface; it passes the selected string to the CLI. Its actual Luna selection succeeded. R2 must retain startup model-capability discovery/validation and fail closed if configured mappings are unavailable.

## Session / resume

Each SDK wrote only its non-secret thread ID to its temporary probe directory. The first process exited normally; a second independent process reconstructed the saved thread and asked for the harmless previous-turn marker.

| SDK | First turn | Same process | New process resume |
| --- | --- | --- | --- |
| Python | completed with durable ID | returned `SAME_PROCESS_MARKER` | same ID resumed; returned marker exactly; usage present |
| TypeScript | completed with durable ID | returned `SAME_PROCESS_MARKER` | same ID resumed; returned marker exactly; usage present |

**INFERRED:** durable post-process resume is a satisfied hard requirement for both candidates under the current local Codex/ChatGPT configuration. It is not a guarantee of indefinite retention or service-side availability; R2 must persist the ID and handle a rejected/missing session as a recovery event.

## Structured results

The exact schema constrained both first turns to:

```json
{"probe":"sdk-contract","status":"PASS"}
```

Both outputs parsed as JSON and exactly matched the schema-required object.

- Python: **OBSERVED** `Thread.run(..., output_schema=schema)` returned the exact valid string. The SDK sends typed app-server `output_schema` parameters.
- TypeScript: **OBSERVED** `runStreamed(..., { outputSchema: schema })` returned the exact valid string. Installed SDK source creates a temporary schema file and supplies it to the CLI `--output-schema` option.

This is contractually structured output, not JSON-looking prose. R2 still needs an application-level `AgentResult` schema validator and relational-invariant validator because no SDK validates FeelingPilates-specific gate semantics.

## Sandbox

All write probes used SDK-created threads whose working directory was a disposable directory below their respective `/tmp/feelingpilates-r1-*` probe root. No FeelingPilates path was a probe workspace.

| SDK | Read-only write attempt | Workspace-write attempt |
| --- | --- | --- |
| Python | File absent; response said the command failed because workspace is read-only. | File existed with `allowed`; response `WRITE_DONE`. |
| TypeScript | File absent; response said workspace is read-only. | File existed with `allowed`; response `WRITE_DONE`. |

**OBSERVED:** both contracts establish the intended sandbox modes. **INFERRED:** R2 must additionally fingerprint the authoritative worktree before/after every role, because sandbox policy alone is not the whole scope/security contract.

## SQLite matrix

No real Autopilot database was created. Both probes used isolated databases under their disposable `/tmp` roots.

| Requirement | Python stdlib `sqlite3` | TypeScript `node:sqlite` |
| --- | --- | --- |
| SQLite version | `3.53.3` | `3.53.1` |
| WAL | PASS (`wal`) | PASS (`wal`) |
| `foreign_keys=ON` | PASS | PASS |
| `busy_timeout=100` | PASS | PASS |
| `BEGIN IMMEDIATE` | PASS | PASS |
| Rollback | PASS: transient row absent | PASS: transient row absent |
| Unique idempotency key | PASS: constraint rejected duplicate | PASS: constraint rejected duplicate |
| Optimistic version update | PASS: version 1 update succeeded; stale update changed 0 rows | PASS: same |
| Concurrent writer lock | PASS: second connection rejected while immediate transaction held | PASS: same |
| Commit/reopen persistence | PASS | PASS |
| `integrity_check` | PASS (`ok`) | PASS (`ok`) |
| Backup | PASS: `VACUUM INTO` backup created | PASS: built-in `backup()` created backup |

**INFERRED:** both native/local choices meet the mandatory SQLite contract. Python has the lower additional dependency surface because `sqlite3` is in the standard library. Node’s `node:sqlite` also required no third-party package on this host, but operational use is synchronous and must be isolated from long event-loop work in R2.

Schema migrations can be implemented as numbered, transactional migrations in either runtime; backup is available in both. A launchd worker can run either language, but the Python path preserves the existing project runtime and SQLite standard-library approach. R2 must establish its database directory, file permissions, backup/retention policy, and crash-recovery protocol explicitly.

## Historical reuse

**OBSERVED read-only:** `/Users/jesusaldaircruzortiz/FeelingPilatesOrchestrator` contains a 4,719-line Python orchestration core, 17 test modules/classes with 223 test methods, role prompts, JSON schemas, workflow configuration, historical run artifacts, Git state discovery, gate logic, recovery handling, signal handling, host validation, and manual-publication safeguards.

The historical engine is deliberately F2D-specific and persists `state.json`; it is not copied into FeelingPilates and must not be treated as current protocol authority.

| Historical material | Python SDK-primary core | TypeScript SDK-primary core | Rationale |
| --- | --- | --- | --- |
| Agent result JSON schema and schema preflight | DIRECTLY_REUSABLE | PORTABLE_FIXTURE | JSON is language-neutral; Python validator can be extracted without a port. |
| Git snapshots/fingerprinting and source-delta checks | DIRECTLY_REUSABLE after F2D identifiers are removed | PORTABLE_ALGORITHM | Existing implementation is Python; its repo-specific detector cannot be carried unchanged. |
| Publication safeguards / `auto_publish=false` | PORTABLE_ALGORITHM | PORTABLE_ALGORITHM | Policy is reusable; F2D commits and paths are obsolete. |
| Run artifacts, lifecycle, subprocess capture, signal handling | DIRECTLY_REUSABLE as patterns/components | PORTABLE_ALGORITHM | Python implementation exists; the SDK runner must replace CLI-only launch code. |
| Failure classifiers and pre-semantic recovery | PORTABLE_ALGORITHM | PORTABLE_ALGORITHM | Existing literals are CLI/F2D-oriented and must be reconciled with SDK errors. |
| Gate/state transition logic | PORTABLE_ALGORITHM | PORTABLE_ALGORITHM | Protocol authority has evolved and must be driven by current `auditoria/orquestacion/`. |
| HostValidator allowlisted execution | PORTABLE_ALGORITHM | PORTABLE_ALGORITHM | General safety structure is reusable; F2D Maven plan is obsolete. |
| Recovery scenarios/tests | PORTABLE_FIXTURE | PORTABLE_FIXTURE | 223 tests provide scenarios and invariants; their F2D state data cannot define R2 behavior. |
| F2D-specific discovery, workflow, config, state journal | OBSOLETE / REWRITE_REQUIRED | OBSOLETE / REWRITE_REQUIRED | It encodes completed F2D state and JSON journal assumptions. |

**Practical reuse difference:** Python preserves executable implementations for the generic schema, Git, run-artifact, signal, fingerprint, and host-safety layers. TypeScript would require a language port of all executable material; only schemas, fixtures, and algorithms would transfer. This is evidence, not a claim that the old Python engine can be adopted wholesale.

## CLI fallback

**OBSERVED:** local `codex-cli 0.150.1` is authenticated with ChatGPT. `codex exec --help` exposed `--model`, `--sandbox` (`read-only`, `workspace-write`, `danger-full-access`), `--ephemeral`, `--output-schema`, `--json`, and config overrides. `codex exec resume --help` and `codex exec fork --help` exposed the corresponding durable-session operations.

Codex CLI remains **FALLBACK / DIAGNOSTIC / CONTRACT COMPARISON**. R1 did not implement a fallback adapter.

## Token / event observability

| SDK | Observed event/usage shape | R2 implication |
| --- | --- | --- |
| Python | `TurnResult` exposed final response, items, status/error, timestamps, duration, and typed aggregate usage (`last`, `total`, `model_context_window`); streaming notifications include item completion, turn completion, and token-usage updates. | ADAPTER_REQUIRED to normalize aggregate/current counters into durable telemetry. |
| TypeScript | `runStreamed()` emitted structured JSONL lifecycle/item events and `turn.completed` usage with `input_tokens`, `cached_input_tokens`, `cache_write_input_tokens`, `output_tokens`, and `reasoning_output_tokens`. | ADAPTER_REQUIRED to normalize fields and preserve raw observed counters. |

The TypeScript SDK offers finer observed token fields; Python offers typed RPC error and session objects. Neither supplied a complete quota/governor signal in the tested minimal turns.

## Failure classification discovery

| Category | Python | TypeScript |
| --- | --- | --- |
| Local process/state failure | **OBSERVED:** typed `TransportClosedError` when a sandboxed process could not initialize local Codex state runtime. | UNKNOWN; not intentionally forced. |
| Session/turn race | **OBSERVED:** typed `InvalidRequestError` for an immediate interrupt before a turn was active; after one-second dispatch, `TurnInterruptResponse` and terminal `interrupted`. | **OBSERVED:** `AbortSignal` caused `AbortError`. |
| Schema/result failure | **DOCUMENTED** typed result/error surface; no intentionally invalid remote schema was sent. | **DOCUMENTED** turn failure/error events and thrown `Error`; no intentionally invalid remote schema was sent. |
| Network / transport outage | REQUIRES_LATER_FAULT_INJECTION | REQUIRES_LATER_FAULT_INJECTION |
| Quota / rate limit | REQUIRES_LATER_FAULT_INJECTION | REQUIRES_LATER_FAULT_INJECTION |

No quota exhaustion, network disconnection, or destructive fault injection was attempted. R2 must make retry/recovery decisions from normalized error categories, not invented string literals.

## Unknown / deferred

- **UNKNOWN:** long-horizon session retention, session eviction, and multi-day resume behavior.
- **UNKNOWN:** quota/rate-limit signal details and subscription usage accounting exposed to either SDK.
- **UNKNOWN:** launchd-specific state-directory permissions and keychain/Codex-auth lifecycle; the local state-runtime initialization observation makes this an R2 preflight requirement.
- **UNKNOWN:** behavior under real network loss, service outage, corrupt state DB, or partial process crash during a semantic checkpoint.
- **UNKNOWN:** production concurrency limits for simultaneous SDK workers. SQLite lock behavior was tested, not Codex account concurrency.
- **DEFERRED:** implementation of the Codex CLI fallback adapter, ModelRouter, SQLite state schema, worker supervisor, usage governor, telemetry store, context compiler, checkpoint/recovery engine, and any F2E execution.

## Language decision

### A. PYTHON_SDK_PRIMARY

### B. TYPESCRIPT_SDK_PRIMARY

### C. BLOCKED_NO_CANDIDATE_SATISFIES_REQUIRED_CONTRACT

**Selected: A. PYTHON_SDK_PRIMARY**

**Selected primary SDK:** `openai-codex==0.147.0`.

### Evidence

Both candidates satisfy the R1 hard SDK contract under the current local ChatGPT-authenticated configuration: installation, auth without an API key, session creation, same-process continuation, cross-process resume, enforced structured output, sandbox behavior, low-effort Luna execution, cancellation, streaming, usage reporting, and bounded one-turn execution.

The selection follows the stated tie-break order, not language preference:

1. **Recovery semantics:** both cross-process resume tests passed. Python additionally exposed typed JSON-RPC `TransportClosedError` and `InvalidRequestError`, and an observed, acknowledged turn interruption with terminal `interrupted`; this is a stronger normalized error/control basis for the recovery layer.
2. **ChatGPT-local auth:** tie; both passed with key variables absent.
3. **Cross-process resume:** tie; both passed exact marker recall.
4. **Structured result:** tie; both produced an exact schema-valid object.
5. **Operational/dependency complexity:** Python uses the standard-library SQLite candidate and a typed SDK surface. The bundled SDK runtime is substantial, but no separate project runtime or SQLite package is required. TypeScript’s SDK is thin and passed, yet its installed implementation invokes `codex exec --experimental-json` although the current CLI help exposes `--json`; this works now but is a version-coupling risk to isolate behind an adapter.
6. **Safe reuse:** Python has material executable reuse from the historical 4,719-line Python core and 223 scenario tests. TypeScript would require a full port of the reusable implementation.
7. **Maintainability:** Python keeps the orchestration core, SQLite use, current tests, and SDK adapter in one language; all SDK/CLI calls still remain behind replaceable interfaces.

### Rejected alternative

**B. TYPESCRIPT_SDK_PRIMARY** is not rejected for a failed contract: its R1 result is strong and it remains the reference adapter shape for future comparison. It is rejected because, after all higher hard requirements passed, it has less direct safe reuse and introduces an undocumented-in-current-CLI-help `--experimental-json` coupling in the tested SDK implementation. Its superior observed per-turn usage fields are valuable and should inform the normalized telemetry contract.

### Remaining risks

The Python SDK’s app-server path is marked experimental by the local CLI and needs an adapter boundary, version pinning, startup self-test, and CLI fallback. The selected runtime must never make the historical state machine authoritative. No selection here authorizes publication, activation, cutover, or F2E advancement.

## R2 prerequisites

1. Independent audit of this report and a fresh preflight of both worktrees.
2. Explicit R2 architecture/intervention authorization from repository canons; do not infer a new product phase.
3. Define adapter interfaces: SDK-primary thread/session/run/stream/cancel/error/usage contract; CLI fallback remains separate.
4. Pin SDK/runtime versions and verify local ChatGPT-auth plus state-directory permissions in the intended launchd environment without exposing credentials.
5. Design, review, and migrate the SQLite schema: durable run/session/checkpoint/idempotency/lease/event tables; WAL, foreign keys, busy timeout, `BEGIN IMMEDIATE`, optimistic versioning, backup, and integrity recovery.
6. Port only the approved generic historical safety patterns; rewrite F2D-specific discovery/state logic against current protocol authority.
7. Establish normalized error, event, usage, model-capability, retry, cancellation, and session-loss contracts with later controlled fault injection.
8. Define bounded `--once` worker supervision and scope/fingerprint enforcement. Preserve `auto_publish=false`.

## Feature worktree invariance

**OBSERVED after report materialization:** branch `operacion/excepciones-horario-fecha`; `HEAD` and resolved upstream both `feb9d6e9abf01fe93a03367769156650d7956b53`; staging empty; working tree contains exactly `?? auditoria/handoffs/HANDOFF-F2E-R1-RESERVA-READER-JPA-READ-ONLY.md`; SHA-256 remains `b65965288c0840934f4db301b7d81efb5ac818640958863902e62ea7f4897185`.

## Autopilot worktree state

**OBSERVED after report materialization:** branch `orquestacion/autopilot-r1`; `HEAD` `feb9d6e9abf01fe93a03367769156650d7956b53`; staging empty; working tree contains exactly `?? auditoria/orquestacion/AUTOPILOT-R1-SDK-LANGUAGE-PREFLIGHT.md` and nothing else.

## Verdict

**A. R1 MATERIALIZED — READY_FOR_FRESH_R1_AUDIT**

No R2 implementation, staging, commit, push, publication, or F2E action was performed.
