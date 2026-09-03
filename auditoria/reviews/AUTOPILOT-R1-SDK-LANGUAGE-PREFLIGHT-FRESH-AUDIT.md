# FeelingPilates — AUTOPILOT R1 SDK/language preflight — fresh audit

**Audit role:** `R1_AUDITOR / SDK_CONTRACT_AUDITOR / LANGUAGE_SELECTION_AUDITOR / WORKTREE_ISOLATION_AUDITOR / RECOVERY_ARCHITECTURE_AUDITOR`

**Audit mode:** `READ_ONLY / FRESH / INDEPENDENT / ADVERSARIAL`

**Date:** 2026-09-03

**Audited report:** `auditoria/orquestacion/AUTOPILOT-R1-SDK-LANGUAGE-PREFLIGHT.md`

**Audited report SHA-256:** `98e936031912c82f4112c559d0b65c54a7a5a96de8661cab9e0103fe36664fbd`

## Scope and physical baseline

The audit independently evaluated the materialized R1 preflight and the physical isolation of its dedicated worktree. It did not execute or modify F2E, modify the feature worktree, modify the historical Python orchestrator, implement R2, or grant authority for any later implementation phase.

| Evidence | Audited value | Result |
| --- | --- | --- |
| Feature worktree | `/Users/jesusaldaircruzortiz/Desktop/Feelingpilates/feelingpilates` | `PASS` |
| Feature branch | `operacion/excepciones-horario-fecha` | `PASS` |
| Feature `HEAD` | `feb9d6e9abf01fe93a03367769156650d7956b53` | `PASS` |
| Feature upstream | `feb9d6e9abf01fe93a03367769156650d7956b53` | `PASS` |
| Feature staging | `EMPTY` | `PASS` |
| Feature working tree | exactly `?? auditoria/handoffs/HANDOFF-F2E-R1-RESERVA-READER-JPA-READ-ONLY.md` | `PASS` |
| Feature frozen R1 SHA-256 | `b65965288c0840934f4db301b7d81efb5ac818640958863902e62ea7f4897185` | `PASS` |
| Autopilot worktree | `/Users/jesusaldaircruzortiz/Desktop/Feelingpilates/feelingpilates-autopilot-r1` | `PASS` |
| Autopilot branch | `orquestacion/autopilot-r1` | `PASS` |
| Autopilot `HEAD` | `feb9d6e9abf01fe93a03367769156650d7956b53` | `PASS` |
| Autopilot upstream before publication | none configured | `PASS` |
| Autopilot staging before persistence | `EMPTY` | `PASS` |
| Autopilot working tree before persistence | exactly `?? auditoria/orquestacion/AUTOPILOT-R1-SDK-LANGUAGE-PREFLIGHT.md` | `PASS` |
| Worktree isolation | dedicated branch/worktree; feature baseline unchanged | `PASS` |

## Tested runtimes and SDKs

The audited report records these tested versions and isolated probe conditions:

| Component | Tested version / state |
| --- | --- |
| Python runtime | `3.14.6` |
| Python SDK | `openai-codex==0.147.0` |
| Python SDK runtime dependency observed | `pydantic==2.13.5`; bundled `openai-codex-cli-bin==0.147.0` |
| Node.js runtime | `v24.18.0` |
| npm | `11.16.0` |
| TypeScript SDK | `@openai/codex-sdk==0.153.1` |
| TypeScript SDK CLI dependency observed | `@openai/codex==0.153.1` |
| Codex CLI fallback observed | `codex-cli 0.150.1` |
| Python SQLite | `3.53.3` through standard-library `sqlite3` |
| Node SQLite | `3.53.1` through built-in `node:sqlite` |

The SDKs were installed only in disposable `/tmp` probe roots. Neither SDK was installed globally or added to FeelingPilates manifests, lockfiles, dependencies, or source.

## Authentication conclusion

`CHATGPT_LOCAL_AUTH_SUPPORTED = YES` for both candidates. The authenticated probes completed with `OPENAI_API_KEY` and `CODEX_API_KEY` removed from the probe process environment, using the existing local ChatGPT/Codex authentication state. Credentials, cookies, headers, and credential-file contents were neither read nor printed.

The initial sandbox restriction affecting creation of local Codex runtime state was correctly classified as an environment-permission characteristic, not an authentication failure. Launchd authentication and state-directory behavior remains an explicit R2 preflight requirement.

## SDK contract conclusions

Both candidates passed stable installation, existing ChatGPT authentication, new session creation, durable thread identification, same-process continuation, new-process resume, machine-usable results, structured JSON, model and reasoning selection, read-only and workspace-write sandbox behavior, cancellation/interruption, streaming/events, token/usage observation, and bounded one-turn execution.

The normalized error layer remains adapter work: Python exposes typed RPC/transport errors; the tested TypeScript surface combines JavaScript `Error` with structured turn failure/error events. This does not invalidate the TypeScript candidate.

| Conclusion | Result |
| --- | --- |
| `PYTHON_CROSS_PROCESS_RESUME` | `PASS` |
| `TYPESCRIPT_CROSS_PROCESS_RESUME` | `PASS` |
| `PYTHON_STRUCTURED_RESULTS` | `PASS` |
| `TYPESCRIPT_STRUCTURED_RESULTS` | `PASS` |
| `PYTHON_SANDBOX` | `PASS` |
| `TYPESCRIPT_SANDBOX` | `PASS` |
| `PYTHON_SQLITE` | `PASS` |
| `TYPESCRIPT_SQLITE` | `PASS` |

Cross-process resume was demonstrated for both candidates by persisting only the non-secret thread ID, exiting the first client process, reconstructing the session in a second independent process, and recovering the expected marker with usage present.

Structured-result probes for both SDKs produced the exact schema-valid object `{"probe":"sdk-contract","status":"PASS"}` through their documented schema mechanisms, rather than relying on unconstrained JSON-looking prose.

Sandbox probes for both SDKs rejected a write in read-only mode and permitted the expected write only inside a disposable workspace under workspace-write mode. No FeelingPilates path served as a probe workspace.

SQLite probes passed WAL, foreign keys, busy timeout, `BEGIN IMMEDIATE`, rollback, unique idempotency enforcement, optimistic versioning, concurrent-writer locking, commit/reopen persistence, integrity checking, and backup in both runtimes. Python `sqlite3` has the selected lower-dependency path.

## Historical reuse conclusion

The historical Python orchestrator is `REFERENCE / SELECTIVE_REUSE ONLY`. Its generic schemas, Git fingerprinting, run-artifact, signal-handling, host-safety, recovery-scenario, and publication-safeguard patterns may be selectively extracted or adapted only after current authority and F2D assumptions are removed.

The historical F2D-specific discovery, workflow, configuration, and state engine is `NOT_AUTHORIZED_AS_NEW_RUNTIME` and must be rewritten for any future authorized R2 scope. Historical code and tests are evidence and reusable input; they are not current protocol authority.

## Language selection conclusion

`LANGUAGE_SELECTION = PYTHON_SELECTION_JUSTIFIED`.

The accepted decision is:

- runtime architecture: Python 3.14.x-compatible;
- primary agent execution: `openai-codex` SDK;
- fallback: Codex CLI;
- durable state: SQLite through Python `sqlite3` behind a `StateStore` abstraction;
- historical orchestrator: reference/selective reuse only;
- historical F2D-specific engine: not the new runtime; and
- TypeScript candidate: valid, but not selected.

TypeScript did not fail the required contract. Both candidates passed the hard requirements. Python won the documented technical tie-break through its typed recovery/error surface, standard-library SQLite path, reduced operational/dependency complexity for this repository, direct selective reuse of generic historical Python components, and single-language maintainability. The TypeScript SDK remains a valid reference shape, including its finer observed token fields.

## Findings

```text
P0 = 0
P1 = 0
P2 = 1
```

### P2-1 — explicit null semantics for normalized telemetry

The future normalized telemetry contract lacks an explicit rule for unavailable token classes.

Required future rule:

```text
Unavailable token classes MUST remain null.

They MUST NOT:
- be fabricated;
- be inferred without evidence; or
- be replaced with synthetic zero values.
```

Disposition: `NON_BLOCKING_FOR_R1_PUBLICATION / MANDATORY_R2_CONTRACT_REQUIREMENT`.

## Audit gates

| Gate | Result |
| --- | --- |
| `FEATURE_BASELINE` | `PASS` |
| `AUTOPILOT_WORKTREE_BASELINE` | `PASS` |
| `WORKTREE_ISOLATION` | `PASS` |
| `R1_REPORT_SELF_CONTAINED` | `PASS` |
| `PYTHON_SDK_CONTRACT` | `PASS` |
| `TYPESCRIPT_SDK_CONTRACT` | `PASS` |
| `CHATGPT_AUTH_CONTRACT` | `PASS` |
| `CROSS_PROCESS_RESUME` | `PASS` |
| `STRUCTURED_RESULT_CONTRACT` | `PASS` |
| `SANDBOX_CONTRACT` | `PASS` |
| `SQLITE_DURABILITY` | `PASS` |
| `HISTORICAL_REUSE` | `PASS` |
| `LANGUAGE_SELECTION` | `PASS` |
| `CLI_FALLBACK` | `PASS` |
| `TELEMETRY_CONTRACT` | `PASS` |
| `DEFERRED_RISK_BOUNDARY` | `PASS` |
| `F2E_ISOLATION` | `PASS` |
| `CANONICAL_CONSISTENCY` | `PASS` |

`TELEMETRY_CONTRACT = PASS` accepts the R1 selection and observed adapter requirements; it does not waive P2-1. P2-1 is mandatory before an R2 normalized telemetry contract may be accepted.

## R2 prerequisites preserved

Publication and acceptance of R1 do not authorize R2 implementation. A future R2 requires all of the following:

1. explicit repository authority for R2;
2. a language-neutral `AgentExecutor` port;
3. exclusion of SDK-specific types from the workflow, gate, and recovery core;
4. treatment of SDK sessions as operational evidence, never canonical authority;
5. resume based on a durable checkpoint plus Git/worktree reconciliation, artifact reconciliation, and SDK-session reconciliation;
6. exact SDK, CLI, and runtime version pinning;
7. launchd authentication and state-directory preflight;
8. SQLite schema, migration, lease, idempotency, backup, and integrity design;
9. normalized error and telemetry contracts;
10. unavailable token classes represented as `null`, never fabricated;
11. selective generic reuse of historical Python components only;
12. rewrite of F2D-specific discovery, state, and workflow behavior; and
13. preservation of `auto_publish=false`.

## Final invariance

```text
Existing R1 report: UNCHANGED
Feature worktree: UNCHANGED / READ_ONLY
Feature frozen R1 fingerprint: PRESERVED
F2E: UNCHANGED / NOT_EXECUTED
Historical Python orchestrator: UNCHANGED
Protocol documents: UNCHANGED
R2: NOT_STARTED
R2 implementation: NOT_AUTHORIZED_BY_THIS_PUBLICATION
tools/autopilot/: NOT_CREATED
Production dependencies: NOT_INSTALLED
Product authority: UNCHANGED
Cutover: false
auto_publish: false
```

## Verdict

```text
READY_TO_ACCEPT_R1 = SI
LANGUAGE_SELECTION = PYTHON_SELECTION_JUSTIFIED
R1 = ELIGIBLE_FOR_ACCEPTANCE_AND_PUBLICATION
```

The accepted R1 checkpoint selects `PYTHON_SDK_PRIMARY`, with `openai-codex` as the primary adapter, Codex CLI as fallback, and Python `sqlite3` behind a `StateStore` abstraction for future authorized durable state. This verdict authorizes only the exact R1 documentation publication candidate; it does not authorize R2 or any F2E action.
