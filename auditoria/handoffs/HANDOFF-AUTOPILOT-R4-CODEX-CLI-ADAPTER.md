# FeelingPilates — HANDOFF: AUTOPILOT R4 Codex CLI adapter

**Original materialization role:** `R4_AUTHORITY_DESIGNER /
AGENT_EXECUTION_BOUNDARY_DESIGNER / CODEX_CLI_ADAPTER_DESIGNER /
SECURITY_BOUNDARY_DESIGNER / RECOVERY_CONTRACT_DESIGNER`

**Original materialization workflow profile:** `DOCUMENTATION_ONLY /
AUTHORITY_MATERIALIZATION / NO_IMPLEMENTATION / NO_ACTIVATION / NO_PUBLICATION`

The original materialization recorded below was prepared for a later fresh
independent adversarial audit and, at that historical point, was not approval or
implementation authority. The authoritative current lifecycle is the final
publication-closure and lifecycle-closure addendum at the end of this document.
It preserves the exact R4 target and its published implementation without
authorizing runtime activation, productive authority, cutover, R5, or F2E
authority.

## Canonical target and evidence chain

The canonical target materialized here is:

```text
AUTOPILOT R4 — Codex CLI adapter
Role: AgentExecutor FALLBACK / DIAGNOSTIC implementation over the local Codex CLI
```

The target follows the current physical repository authority, not chat memory:

- the historical pre-activation state closed R3 and authorized materialization
  of this exact R4 authority;
- accepted R1 selects `PYTHON_SDK_PRIMARY`, fixes Codex CLI as
  `FALLBACK / DIAGNOSTIC / CONTRACT COMPARISON`, records the tested CLI
  baseline, and defers the CLI fallback adapter;
- accepted R2 creates the transport-neutral `AgentExecutor` port for both the
  future Python SDK adapter and Codex CLI fallback adapter, while excluding
  both concrete adapters from R2; and
- closed R3 materializes `StateStore`, remains historical, and explicitly
  excludes SDK/CLI adapters and workflow policy.

No current Autopilot canonical identifies a competing or materially different
R4 target. The historical fresh re-audit and activation authorized its exact
implementation; the later independent audit accepted that implementation and
the publication checkpoint recorded in the current addendum published it.

## Historical pre-activation lifecycle and authority state

```text
R4 HANDOFF (HISTORICAL PRE-ACTIVATION): MATERIALIZED / NOT_APPROVED / NOT_ACTIVE
R4 TARGET (HISTORICAL PRE-ACTIVATION): NOT_AUTHORIZED / NOT_STARTED
R4 IMPLEMENTATION (HISTORICAL PRE-ACTIVATION): NOT_AUTHORIZED / NOT_STARTED
R4 FIRST FRESH HANDOFF AUDIT: P0=0 / P1=1 / P2=0
R4 P1-1: NEW_TURN_SESSION_EXTRACTION_CARDINALITY_AND_REQUIREDNESS_UNDEFINED
R4 CORRECTION.1: MATERIALIZED
R4 P1-1 DISPOSITION: CORRECTED_BY_MATERIALIZED_CORRECTION / PENDING_FRESH_REAUDIT
READY_FOR_FRESH_HANDOFF_AUDIT: SI
ACTIVE_AUTOPILOT_HANDOFF (HISTORICAL PRE-ACTIVATION): NONE
NEXT ALLOWED ACTION (HISTORICAL PRE-ACTIVATION): FRESH_AUDIT_AUTOPILOT_R4_HANDOFF
```

The later audit was required to be fresh, independent, adversarial, and
read-only. Its successful re-audit and this competent lifecycle publication are
now the only basis for current approval and activation. Original materialization
alone never established `APPROVED`, `ACTIVE`, `AUTHORIZED_TO_START`,
`IMPLEMENTED`, `PUBLISHED`, or `CLOSED`.

## Architectural position

The accepted execution architecture remains:

```text
Workflow Engine / future consumer
        |
        v
AgentExecutor port
        |
        +--> Python SDK Adapter — PRIMARY
        |
        +--> Codex CLI Adapter — FALLBACK / DIAGNOSTIC
```

Python SDK remains primary regardless of implementation order. R4 must not
invert, supersede, or weaken that decision. A future workflow consumer imports
the `AgentExecutor` abstraction; it must not import `subprocess`, CLI argument
construction, JSONL/event parsing, process IDs, signals, or other CLI mechanics
directly.

R4 executes one bounded requested agent turn. It does not decide the next
workflow step, evaluate legal workflow transitions, schedule phases, select a
retry policy, persist workflow progress, or authorize continuation.

## AgentExecutor-compatible extension

The published R2 port is the governing conceptual boundary. Repository
inspection shows that a bounded compatible extension of
`ports/agent_executor.py` is required for concrete CLI execution; the current
`ExecutionRequest` contains only `run_id` and `instructions`, and the current
observation has one `raw_output` field without the required process metadata or
stdout/stderr separation.

The future R4 change may extend that port only as needed to model, with
provider-neutral types:

- expected workflow, run, role, and gate identity;
- bounded prompt/instructions;
- validated absolute working directory;
- supported sandbox mode;
- model and reasoning-effort selection;
- positive per-turn timeout;
- execution/session identity needed by `start`, `resume`, `interrupt`, and
  observation;
- separate raw stdout and stderr auxiliary evidence;
- exit status, signal termination, timeout/cancellation state, and explicit
  diagnostic truncation metadata.

The extension must preserve the R2 operations and compatibility concept:
`capabilities`, `start`, `resume`, `interrupt`, and `get_result`. It must not
expose provider-native or `subprocess` objects through the port. Existing
`ExecutionObservation`, `AgentResult`, `FailureRecord`, `UsageRecord`, artifact,
checkpoint, and session concepts remain authoritative. Any changed terminal
invariant must remain strict: success requires one valid `AgentResult`; failure
requires a normalized `FailureRecord`; acknowledged cancellation/interruption
must never carry a fabricated successful result.

## Safe invocation and command builder

The future adapter must resolve the executable explicitly before execution. A
configured executable must resolve to an absolute existing executable file; a
bounded `PATH` lookup may be performed once by trusted infrastructure
configuration and the resolved absolute path retained. Executable selection is
not supplied by model output or free-form workflow prose.

Every invocation must use an explicit immutable argv sequence with
`shell=False`. Shell command strings, `shell=True`, interpolation, command
substitution, redirections, pipelines, aliases, and raw caller-provided flag
concatenation are forbidden. No LLM-produced shell command may become adapter
execution authority.

The deterministic builder accepts only modeled and validated values:

```text
resolved executable
operation: new turn or resume
model
reasoning effort
validated working directory
supported sandbox mode
canonical structured-output schema reference
bounded prompt/request input
validated opaque session identity for resume
```

The builder may emit only flags verified for the supported capability profile.
Unknown operations, models, efforts, sandbox values, flags, malformed session
identities, missing schema files, nonpositive timeouts, or malformed
configuration fail closed. There is no `extra_args`, raw flags, arbitrary
config override, or generic escape hatch.

Prompt content is data supplied through the CLI's supported request-input
channel; it is not parsed as shell syntax. Secrets must not be placed in argv.

## CLI version and capability boundary

Accepted R1 evidence records:

```text
CLI package: codex-cli
Tested version: 0.150.1
Authentication observed: Logged in using ChatGPT
Observed exec flags: --model, --sandbox, --ephemeral, --output-schema, --json
Observed session commands: codex exec resume; codex exec fork
```

This is the authoritative tested baseline, not a promise that all later CLI
versions have identical syntax or events. R4 implementation must provide a
bounded, non-agent-turn version/capability probe where required, covering the
version and the exact new-turn/resume flags/events the adapter consumes. An
unsupported, missing, contradictory, or unparsable capability profile fails
closed before a semantic result is consumed.

Version-specific argv spellings and event names belong only in the
infrastructure adapter and its evidence. No provider/version-specific literal
becomes core domain or workflow authority.

## Authentication and environment boundary

R4 preserves local Codex/ChatGPT authentication. It must not require an API key,
read or copy credential files into the repository, persist credentials in
SQLite, or log tokens, cookies, headers, credential contents, or the complete
parent/child environment.

Authentication availability is runtime infrastructure state. The child may
inherit the trusted runtime environment required for local Codex operation, but
that environment is never made durable evidence or normal result output.
Environment overrides, if a later implementation needs them, must come from
trusted adapter configuration rather than an agent request and must be explicit
and allowlisted. Tests must place a sentinel secret in the child environment
and prove it is absent from serialized observations and ordinary diagnostics.

## Working directory and sandbox authority

Every turn has an explicit validated working directory supplied through the
execution context. It must be absolute, exist, be a directory, and satisfy the
caller's already-authorized workspace boundary. The adapter must pass it as the
subprocess `cwd`; it must never silently inherit whichever directory launched
Autopilot.

The supported adapter mapping is exact:

```text
READ_ONLY       -> CLI read-only
WORKSPACE_WRITE -> CLI workspace-write
```

R4 does not authorize `danger-full-access` or a broader mode. Unknown or
unsupported mappings fail closed. The command builder must never grant more
filesystem authority than requested. Workspace-write permits only the agent
turn's separately authorized workspace scope; it does not grant publication or
Git lifecycle authority.

## Structured result and schema validation

For every operation requiring a semantic result, the adapter must request the
canonical `agent-result.schema.json` through the CLI structured-output
capability and use the verified machine/event channel. The result parser must:

1. keep stdout and stderr separate;
2. accept only the version-profile event/result shape verified by the adapter;
3. locate exactly one terminal candidate structured result;
4. decode it without heuristic repair;
5. validate the complete accepted JSON schema, including nested
   `usage_record`, required fields, enums, non-empty fields, conditional
   correction rules, and `additionalProperties: false` boundaries;
6. construct the accepted internal domain values so dataclass invariants also
   execute;
7. require exact agreement with the request's workflow, run, role, and gate
   identity; and
8. reconcile any returned session reference with independently extracted CLI
   session evidence.

Missing output, malformed JSON/JSONL, duplicate or unknown terminal shapes,
unknown fields, schema violations, empty required summaries, identity mismatch,
or session conflict produce `RESULT_CONTRACT_FAILURE`. Invalid data is not
repaired, completed, coerced, or exposed as canonical `AgentResult`.

Raw stdout is only the candidate machine/result channel. Stderr is diagnostic
auxiliary evidence and is never parsed as `AgentResult`. Neither raw stream is
canonical repository/workflow authority. Exit code zero alone is not semantic
success.

## Deterministic exit and terminal semantics

The future adapter must implement this precedence:

| Physical result | Internal observation |
| --- | --- |
| Exit 0 and exactly one valid, identity-consistent structured result | `SUCCEEDED` with `AgentResult` |
| Exit 0 with missing/malformed/invalid result | `FAILED` with `RESULT_CONTRACT_FAILURE` |
| Nonzero normal exit | `FAILED` with `CLI_PROCESS_FAILURE`; no canonical `AgentResult` |
| Unexpected signal termination | `FAILED` with `CLI_PROCESS_FAILURE`; signal preserved |
| Adapter timeout | `FAILED` with `CLI_PROCESS_FAILURE`; timeout explicit and cleanup complete |
| Acknowledged explicit cancellation | `INTERRUPTED` with normalized failure/evidence; no `AgentResult` |

A process that reached a valid terminal success before cancellation was
acknowledged may remain successful. Once cancellation has been delivered or
acknowledged, later partial output cannot be promoted to success.

Concrete provider error text remains bounded raw evidence. R4 may normalize
only what the existing stable `FailureRecord` boundary supports; it does not
create the future global network/quota classifier or workflow retry policy.
`PROCESS FAILURE != SEMANTIC GATE FAILURE`: a missing valid result does not
invent P0/P1/P2, a gate result, or a business transition.

## Session, resume, and restart contract

### New-turn session evidence: source, validity, and cardinality

For a **new turn**, session evidence is optional and independent from the
`AgentResult` validation axis. The adapter may extract a session/thread identity
only from the designated machine-readable session/thread event of the supported
CLI capability profile, or that profile's exact version-supported equivalent.
It must never infer an identity from free-form stdout, stderr, diagnostic prose,
or a regex guess over arbitrary log lines. Non-session events are ignored for
session extraction.

A recognized session event is valid only when its required identifier is
present, is string-like as required by the verified event contract, is nonempty
after that contract's exact canonical validation, and has the supported event
shape. The adapter must not transform unrelated values into identities. A
missing required identifier, `null` where the event requires an identifier,
blank/whitespace-only content, wrong structural type, or invalid supported-event
shape is malformed protocol evidence.

The cardinality decision is deterministic after canonical validation and
normalization of valid identifiers:

| Recognized new-turn session evidence | Required `SessionReference` disposition |
| --- | --- |
| Zero recognized session/thread events | `None`; this is allowed. |
| Exactly one unique valid identifier | Construct exactly one reference bound to the exact run, execution/attempt context, adapter, and role. |
| Repeated events with the same valid identifier | Deduplicate to exactly one reference; repeated evidence is not a conflict. |
| Two or more distinct valid identifiers | Fail closed; no reference is selected. |
| Any malformed recognized session/thread event | Fail closed; no reference is constructed from that malformed evidence. |

Zero recognized session events do not fail an otherwise successful execution.
Thus `exit=0` plus one valid, identity-consistent `AgentResult` plus zero
recognized session events produces `SUCCEEDED` with `session=None`. The absence
of a reference must remain `None`; it must never become an empty string,
synthetic identifier, zero value, or derived identifier.

Multiple distinct valid identifiers are ambiguous protocol evidence. The adapter
must return a non-success observation with normalized `RESULT_CONTRACT_FAILURE`
and bounded raw diagnostic evidence; it must not use first-wins, last-wins,
lexicographic selection, or any other guess. A malformed recognized event is
also a closed protocol failure and must not be silently reclassified as zero
events. Consequently, a valid-looking `AgentResult` cannot make conflicting or
malformed session evidence successful.

The terminal result and session evidence remain separate validation axes:

| AgentResult axis | Session-evidence axis | Required terminal disposition |
| --- | --- | --- |
| Valid | Zero recognized events | `SUCCEEDED`, `session=None`. |
| Valid | One unique valid ID, including repeated identical events | `SUCCEEDED`, exactly one normalized `SessionReference`. |
| Valid | Distinct valid IDs or malformed recognized event | `FAILED` with normalized protocol/result-contract failure; no selected session. |
| Invalid/missing, or execution otherwise fails | One unique valid ID observed before failure | Non-success with the actual normalized execution/result failure; preserve that session as operational evidence. |
| Invalid/missing, or execution otherwise fails | Distinct IDs or malformed recognized event | Non-success; preserve the actual failure and the session-protocol defect in bounded evidence, with no usable `SessionReference`. |

In particular, a valid unambiguous session observed before a later nonzero exit,
signal termination, timeout, acknowledged cancellation, malformed/missing final
`AgentResult`, or structured-result validation failure is preserved in the
failed/interrupted `ExecutionObservation`. The `AgentResult` is absent unless it
is independently valid and contractually allowed for that terminal mode; the
existing port's strict terminal invariant otherwise applies. If session evidence
is conflicting or malformed before that later failure, the observation preserves
the actual normalized failure together with bounded protocol-defect evidence,
and no resumable session state is emitted.

### Resume input and recovery boundary

A **resume turn** is different: exactly one explicitly supplied, validated
`SessionReference`/session ID is required as input from the caller's
execution/handle/checkpoint context before invocation. Resume construction
rejects a missing, blank, malformed, conflicting, unknown, or unrepresentable
identity, and rejects a raw arbitrary resume fragment. Session ID remains argv
data, never shell syntax. There is no new-turn cardinality inference during
resume construction and the adapter never searches prior arbitrary logs to
guess a session.

Resume rejection or session loss becomes a normalized failed observation; it is
not silently replaced with a new session. Same-process continuation and a
second adapter/process constructing the verified resume command must be covered
at the adapter boundary with deterministic fake-CLI evidence.

```text
SessionReference exists != workflow authorized
SessionReference absent != workflow failed
SessionReference persisted != resume authorized
CLI session existence != workflow authorization
```

`SessionReference` is operational evidence, not a secret and not canonical
workflow authority. After restart, R3 `StateStore` remains durable recovery
authority, and a later workflow/orchestration layer must reconcile the run,
checkpoint, Git/workspace evidence, and session before deciding whether resume
is eligible. R4 never makes that authorization decision and never writes hidden
SQLite state.

## Timeout, interruption, and child cleanup

The adapter enforces the positive bounded per-turn timeout supplied by its
caller. It must create the CLI process in a controllable process group/session,
retain only internal execution bookkeeping, and support interruption through
the `AgentExecutor` port.

On timeout or explicit cancellation it must:

1. mark the termination cause deterministically;
2. deliver the platform-appropriate graceful termination to the complete child
   process group;
3. wait only a bounded grace period;
4. force termination of the group if still alive;
5. reap the child and avoid zombies or unmanaged descendants;
6. preserve final exit/signal status, partial stdout, partial stderr, explicit
   truncation state, and any already verified session identity; and
7. return the normalized failed/interrupted observation without inventing an
   `AgentResult`.

If the platform cannot provide the required process-group cleanup semantics,
the capability probe must fail closed. Workflow retry decisions remain outside
R4.

## Output and log bounds

The future implementation must define finite byte bounds. Structured stdout
must be captured/spooled outside the Git checkout and retained in full only up
to the configured validation bound. Exceeding that bound terminates the turn
and fails closed; the adapter must not truncate the candidate result and parse
the prefix as if complete.

Diagnostic stderr and auxiliary post-validation output may be truncated to
their independent finite limits. Every truncation is explicit in the
observation/evidence with original-size information where safely available.
Truncated evidence must never be labeled complete. Temporary capture artifacts
are cleaned after the observation is built and must not become repository
artifacts or hidden durable authority.

## Telemetry truthfulness

Usage is normalized only from verified CLI runtime events attributable to the
turn. Each populated token class requires direct evidence for that exact class.

```text
Unavailable component -> NULL / UNAVAILABLE
Directly observed zero -> 0 / OBSERVED, with direct evidence
Aggregate-only usage -> observed aggregate populated; components remain NULL
```

R4 forbids invented classes, synthetic zeroes, heuristic decomposition,
provider-estimated values presented as observed, and inference from bytes,
pricing, context length, or another token field. Any future derived/estimated
metric remains explicitly distinct and cannot replace a null observed field.

An agent-produced embedded `usage_record` must itself pass the complete schema
and domain validation. It does not establish runtime observation by assertion:
any `OBSERVED` embedded value must be supported by matching verified CLI usage
evidence, and a contradiction fails closed. The adapter's normalized
`ExecutionObservation.usage_record` remains the operational usage record.

This malformed embedded-usage validation naturally may produce competent
future evidence for R2 Debt B, but this authority document does not close it.

## StateStore, idempotency, Git, and mutation boundaries

R4 produces observations; it does not own durable workflow state. It must not
advance a run, phase, gate, transition, checkpoint, lease, idempotency record,
or human decision, and it must not write SQLite directly. A later engine may
persist an observation through the accepted `StateStore` contract.

Operation identity may be passed through the typed execution context, but R4
does not decide whether an operation previously committed. R3 remains durable
idempotency and recovery authority.

The adapter lifecycle must never run `git add`, `commit`, `push`, `merge`,
`rebase`, `reset`, `clean`, or `stash`. A separately authorized agent turn may
modify its permitted workspace under `WORKSPACE_WRITE`; that is not adapter
publication authority. `auto_publish` remains `false`.

## Exact future R4 implementation allowlist

Only after a fresh independent handoff audit and a later competent explicit
approval/activation may a future R4 executor create or modify exactly these
paths:

```text
tools/autopilot/README.md
tools/autopilot/config/runtime-contract.json
tools/autopilot/src/feelingpilates_autopilot/ports/agent_executor.py
tools/autopilot/src/feelingpilates_autopilot/adapters/__init__.py
tools/autopilot/src/feelingpilates_autopilot/adapters/execution/__init__.py
tools/autopilot/src/feelingpilates_autopilot/adapters/execution/command.py
tools/autopilot/src/feelingpilates_autopilot/adapters/execution/codex_cli.py
tools/autopilot/src/feelingpilates_autopilot/adapters/execution/result_parser.py
tools/autopilot/tests/fixtures/fake_codex_cli.py
tools/autopilot/tests/test_codex_cli_command.py
tools/autopilot/tests/test_codex_cli_adapter.py
tools/autopilot/tests/test_codex_cli_process.py
tools/autopilot/tests/test_codex_cli_results.py
```

This is an exact 13-path allowlist with no wildcard authority. It follows the
existing Python package, `adapters/state` subpackage, stdlib `unittest`, and
`test_<behavior>.py` conventions. `runtime-contract.json` requires a bounded
capability/version update; `agent_executor.py` requires the compatible extension
described above; `README.md` must stop claiming that no execution adapter exists
once implementation is real. No dependency or schema change is currently
required: accepted R2 `AgentResult`, usage schema, domain models, and stable
failure vocabulary remain authoritative.

If fresh audit or future pre-flight proves any listed path structurally
incompatible or a required change outside this list, implementation must stop
and return to an explicit handoff correction. It must not silently substitute,
add, or broaden a path.

## Required deterministic test authority

The normal R4 suite uses stdlib-only `unittest`, deterministic fixtures, and a
fake executable process. It must not require a live Codex turn, network access,
provider cost, local credential access, Git mutation, or F2E execution.

Required material coverage includes:

- deterministic argv for new and resumed turns; explicit executable and cwd;
- no shell interpolation and adversarial prompt/argument values remaining data;
- fail-closed unknown flags, capabilities, models, efforts, sessions, and
  sandbox modes;
- exact read-only/workspace-write mapping and rejection of broader authority;
- valid structured `AgentResult` success and exact identity agreement;
- malformed JSON/JSONL, missing/duplicate result, unknown fields, invalid enum,
  whitespace-only summary, invalid correction shape, and nested schema failure;
- malformed embedded `usage_record`, including unknown nested fields and
  provenance/value/evidence contradictions;
- exit zero without valid semantic result, nonzero exit, signal termination,
  and strict stdout/stderr separation;
- new turn with zero recognized session events and valid `AgentResult` succeeds
  with `session_reference is None`;
- new turn with exactly one valid session ID succeeds with exactly one
  `SessionReference`;
- repeated identical valid session events succeed with one deduplicated
  `SessionReference`;
- multiple distinct valid session IDs fail closed with no arbitrarily selected
  `SessionReference`;
- malformed recognized session events, including blank IDs, fail closed rather
  than becoming zero-event evidence;
- a valid unique session observed before nonzero exit, timeout, cancellation, or
  malformed final `AgentResult` is preserved as operational evidence in the
  appropriate failed/interrupted observation;
- conflicting session IDs followed by a later failure retain no arbitrarily
  selected resumable reference and preserve bounded conflict evidence;
- same-process continuation boundary, deterministic resume argv,
  second-process/adapter resume construction, and resume rejection for missing,
  blank, malformed, conflicting, unknown/unrepresentable, and raw arbitrary
  resume input;
- timeout, explicit cancellation, grace/forced termination, child-group cleanup,
  reaping, and preservation of partial auxiliary evidence;
- bounded stdout/stderr, explicit truncation, and no parsing of truncated result
  data;
- exact directly observed usage, aggregate-only usage, unavailable `NULL`,
  observed zero, no fabricated classes, and embedded/runtime consistency;
- raw auxiliary evidence and stable normalized failure mapping; and
- a sentinel sensitive environment value absent from normal serialization and
  logs.

A separate optional real-CLI smoke/probe may later be run only if explicitly
authorized by repository protocol. It must be bounded, non-destructive, use a
disposable workspace, incur no normal-suite dependency, and record version and
capability evidence truthfully. It cannot replace deterministic tests.

## Carry-forward debt

```text
R2 Debt A — LeaseResolution.NONE / NO_RELEVANT_LEASE:
  CLOSED_BY_R3

R2 Debt B — malformed embedded usage_record behavioral validation:
  OPEN / NON_BLOCKING / CARRY_FORWARD
  POTENTIALLY_CLOSABLE_BY_R4 /
  REQUIRES_IMPLEMENTATION_AND_FRESH_AUDIT

R2 Debt C — attached branch without upstream behavioral coverage:
  OPEN / NON_BLOCKING / CARRY_FORWARD / OUTSIDE_R4_SCOPE
```

R4 must not widen to close Debt C. This handoff does not close any open debt.

## Explicit exclusions

R4 does not authorize:

- Python SDK adapter implementation; Python SDK remains the selected primary;
- workflow engine, workflow scheduling, phase orchestration, or engine-owned
  automatic retry policy;
- `ContextCompiler` or model router;
- Git adapter or worktree manager;
- publisher or any automatic publication behavior;
- process supervisor, launchd, usage governor, or global retry/quota classifier;
- R5 or any later Autopilot functionality;
- F2E/product implementation or execution;
- runtime activation, productive authority, migration, fence, or cutover.

It also does not reactivate or rewrite R3. R3 remains closed and historical.

## Required fresh security/adapter review gate

Before any implementation authority may exist, a fresh independent
`SECURITY / ADAPTER HANDOFF AUDIT` must review this materialized handoff,
canonical evidence, exact 13-path allowlist, and boundary consistency. If later
implementation is independently authorized and performed, a separate fresh
`SECURITY / ADAPTER IMPLEMENTATION AUDIT` must materially inspect:

- subprocess security and deterministic argv construction;
- explicit executable, cwd, and sandbox authority;
- complete structured-output/schema/domain validation;
- result/request identity and session/resume correctness;
- timeout, cancellation, process-group cleanup, and reaping;
- stdout/stderr separation and output/log bounds;
- exit/failure normalization without semantic gate invention;
- telemetry truthfulness and malformed embedded usage coverage;
- authentication, environment, secret, and log boundaries;
- port isolation and absence of CLI mechanics in consumers/domain; and
- exact scope isolation, no workflow engine, no StateStore writes, no Git
  lifecycle, no publication, no R5+, and no F2E change.

The R4 author, executor, or corrector must not perform its own independent
audit.

## Preserved authority — historical pre-activation snapshot

```text
R1: ACCEPTED / PUBLISHED / HISTORICAL
Primary executor: Python SDK
Codex CLI: FALLBACK / DIAGNOSTIC
R2: PUBLISHED / CLOSED / HISTORICAL
R3: IMPLEMENTED / ACCEPTED / PUBLISHED / CLOSED / HISTORICAL / NOT_ACTIVE
R4 HANDOFF (HISTORICAL PRE-ACTIVATION): MATERIALIZED / NOT_APPROVED / NOT_ACTIVE
R4 TARGET (HISTORICAL PRE-ACTIVATION): NOT_AUTHORIZED / NOT_STARTED
R4 IMPLEMENTATION (HISTORICAL PRE-ACTIVATION): NOT_AUTHORIZED / NOT_STARTED
R4 FIRST FRESH HANDOFF AUDIT: P0=0 / P1=1 / P2=0
R4 P1-1: NEW_TURN_SESSION_EXTRACTION_CARDINALITY_AND_REQUIREDNESS_UNDEFINED
R4 CORRECTION.1: MATERIALIZED
R4 P1-1 DISPOSITION: CORRECTED_BY_MATERIALIZED_CORRECTION / PENDING_FRESH_REAUDIT
ACTIVE_AUTOPILOT_HANDOFF (HISTORICAL PRE-ACTIVATION): NONE
R5+: NOT_AUTHORIZED
F2E: UNCHANGED
auto_publish: false
NEXT ALLOWED ACTION (HISTORICAL PRE-ACTIVATION): FRESH_REAUDIT_AUTOPILOT_R4_HANDOFF
```

## Activation addendum — preserved historical activation state

The fresh independent handoff re-audit at
`auditoria/reviews/AUTOPILOT-R4-CODEX-CLI-ADAPTER-HANDOFF-REAUDIT.md` recorded
`P0=0 / P1=0 / P2=0` and closed P1-1. This competent authority publication
therefore activates only the exact target and exact 13-path implementation
allowlist already materialized in this handoff.

```text
R4 HANDOFF: MATERIALIZED / APPROVED / ACTIVE
R4 TARGET: AUTOPILOT R4 — Codex CLI adapter
R4 TARGET LIFECYCLE: AUTHORIZED_TO_START / NOT_STARTED
R4 IMPLEMENTATION: AUTHORIZED_TO_START
R4 HANDOFF AUDIT: PASS / P0=0 / P1=0 / P2=0
R4 P1-1: CLOSED_BY_FRESH_REAUDIT
ACTIVE_AUTOPILOT_HANDOFF: R4 CODEX CLI ADAPTER
Primary executor: Python SDK
Codex CLI: FALLBACK / DIAGNOSTIC
R5+: NOT_AUTHORIZED
F2E: UNCHANGED
auto_publish: false
NEXT ALLOWED ACTION: EXECUTE_ACTIVE_AUTOPILOT_R4_CODEX_CLI_ADAPTER
```

No R4 implementation has been started or accepted. This activation does not
authorize the Python SDK adapter, a workflow engine, StateStore mutation, Git
lifecycle authority, automatic fallback policy, R5+, or F2E/product behavior.

## Implementation acceptance addendum — preserved pre-publication state

The exact 13-path R4 implementation candidate authorized by this handoff was
materialized and then independently re-audited fresh. The final implementation
re-audit is persisted at
`auditoria/reviews/AUTOPILOT-R4-CODEX-CLI-ADAPTER-IMPLEMENTATION-FINAL-REAUDIT.md`.
It records `P0=0 / P1=0 / P2=1`, closes the final material process-cleanup P1,
and is sufficient to accept the implementation without changing the primary /
fallback architecture.

```text
R4 HANDOFF: APPROVED / ACTIVE
R4 TARGET: IMPLEMENTED
R4 IMPLEMENTATION: ACCEPTED
R4 IMPLEMENTATION AUDIT: PASS
R4 FINAL AUDIT: P0=0 / P1=0 / P2=1
READY_TO_ACCEPT_R4_IMPLEMENTATION: SI
R4 P1 — CAPABILITY_PROBE_LEADER_EXIT_DESCENDANT_NOT_CLEANED: CLOSED
R4 P2 — CAPABILITY_TIMEOUT_PRIMARY_CAUSE_MASKED_BY_PRE_REAP_GROUP_LIVENESS:
  OPEN / NON_BLOCKING
R4 PUBLICATION: PENDING until this accepted implementation commit is successfully pushed
R4 CLOSURE: PENDING
Primary executor: Python SDK
Codex CLI: IMPLEMENTED / ACCEPTED / FALLBACK / DIAGNOSTIC
Automatic fallback: NOT_IMPLEMENTED
Workflow engine: NOT_IMPLEMENTED
R2 Debt A: CLOSED_BY_R3
R2 Debt B: CLOSED_BY_R4
R2 Debt C: OPEN / NON_BLOCKING / CARRY_FORWARD / OUTSIDE_R4_SCOPE
R5+: NOT_AUTHORIZED
F2E: UNCHANGED
auto_publish: false
NEXT ALLOWED ACTION: PUBLISH_ACCEPTED_AUTOPILOT_R4_IMPLEMENTATION
```

R2 Debt B is closed by later competent R4 evidence, not rewritten as R2-era
coverage: the structured-result/usage parser and deterministic behavioral
tests reject non-object records, malformed scalar/type values, prohibited
negative values, unknown fields, invalid provenance, adapter/source mismatch,
and partial malformed objects while preserving unavailable `NULL` values and
directly observed zeroes. R2 Debt C remains outside this scope and open.

The open R4 P2 is diagnostic cause ordering only. During a capability-probe
timeout, cleanup status can be checked before the timed-out leader is reaped,
so `CapabilityError` can say `Codex CLI capability probe process group cleanup
failed` even though the leader is subsequently reaped, descendants and group
are gone, the probe is bounded, no orphan survives, no request/model timeout is
fabricated, and capability execution still fails closed. It is not a P1 and is
not closed by this acceptance.

## Post-publication reconciliation addendum — historical pre-closure state

The accepted R4 implementation was successfully published at the physical
checkpoint below. Publication is complete, but it is not lifecycle closure:
the publication-closure gate remains pending a fresh independent
publication-closure audit.

```text
R4 HANDOFF: APPROVED / ACTIVE
ACTIVE_AUTOPILOT_HANDOFF: R4 CODEX CLI ADAPTER
R4 TARGET: IMPLEMENTED / PUBLISHED
R4 IMPLEMENTATION: ACCEPTED / PUBLISHED
R4 IMPLEMENTATION AUDIT: PASS
R4 FINAL IMPLEMENTATION AUDIT: P0=0 / P1=0 / P2=1
R4 P2 — CAPABILITY_TIMEOUT_PRIMARY_CAUSE_MASKED_BY_PRE_REAP_GROUP_LIVENESS:
  OPEN / NON_BLOCKING
R4 PUBLICATION: COMPLETE / PUBLISHED
R4 PUBLICATION COMMIT: e7f8cb3a66560df6981a0e1bfb54d0e942348ff1
R4 CLOSURE: PENDING_FRESH_PUBLICATION_CLOSURE_AUDIT
R4: NOT_CLOSED / NOT_HISTORICAL
Primary executor: Python SDK / PRIMARY
Codex CLI: IMPLEMENTED / ACCEPTED / PUBLISHED / FALLBACK / DIAGNOSTIC
Automatic fallback: NOT_IMPLEMENTED
Workflow engine: NOT_IMPLEMENTED
R2 Debt A: CLOSED_BY_R3
R2 Debt B: CLOSED_BY_R4
R2 Debt C: OPEN / NON_BLOCKING / CARRY_FORWARD / OUTSIDE_R4_SCOPE
R5+: NOT_AUTHORIZED
F2E: UNCHANGED
auto_publish: false
NEXT ALLOWED ACTION: FRESH_AUDIT_AUTOPILOT_R4_PUBLICATION_CLOSURE
```

The open P2 remains diagnostic cause ordering only: group-cleanup state can be
observed before the timed-out leader is finally reaped, yielding a
cleanup-oriented `CapabilityError` even though the leader is ultimately reaped,
descendants and process group are gone, execution remains bounded, no orphan
survives, and no request/model timeout is fabricated. It does not block this
publication reconciliation or the required fresh closure audit, and it is not
silently closed here.

## Final publication-closure audit and lifecycle closure — authoritative current R4 state

The fresh independent publication-closure audit is persisted at
`auditoria/reviews/AUTOPILOT-R4-PUBLICATION-CLOSURE-FINAL-AUDIT.md`. It
verified the already-published implementation commit and the post-publication
reconciliation without changing implementation or lifecycle state itself. Its
`P0=0 / P1=0 / P2=0 new findings` result permits this competent lifecycle
closure; this addendum records the resulting terminal R4 state without
attributing the lifecycle mutation to the auditor.

```text
R4 HANDOFF: APPROVED / CLOSED / HISTORICAL
R4 ACTIVE: NO
R4 TARGET: IMPLEMENTED / PUBLISHED / COMPLETED
R4 IMPLEMENTATION: ACCEPTED / PUBLISHED
R4 IMPLEMENTATION AUDIT: PASS
R4 FINAL IMPLEMENTATION AUDIT: P0=0 / P1=0 / P2=1
R4 PUBLICATION: COMPLETE / PUBLISHED
R4 PUBLICATION COMMIT: e7f8cb3a66560df6981a0e1bfb54d0e942348ff1
R4 FINAL PUBLICATION-CLOSURE AUDIT: P0=0 / P1=0 / P2=0 NEW FINDINGS / PASS
READY_TO_CLOSE_R4_PUBLICATION: SI
R4 CLOSURE: CLOSED / HISTORICAL
R4 P2 — CAPABILITY_TIMEOUT_PRIMARY_CAUSE_MASKED_BY_PRE_REAP_GROUP_LIVENESS:
  OPEN / NON_BLOCKING / CARRY_FORWARD
Primary executor: Python SDK / PRIMARY
Codex CLI: IMPLEMENTED / ACCEPTED / PUBLISHED / HISTORICAL / FALLBACK / DIAGNOSTIC
Automatic fallback: NOT_IMPLEMENTED
Workflow engine: NOT_IMPLEMENTED
R2 Debt A: CLOSED_BY_R3
R2 Debt B: CLOSED_BY_R4
R2 Debt C: OPEN / NON_BLOCKING / CARRY_FORWARD / OUTSIDE_R4_SCOPE
ACTIVE_AUTOPILOT_HANDOFF: NONE
R5: NOT_STARTED / NOT_AUTHORIZED
F2E: UNCHANGED
auto_publish: false
NEXT ALLOWED ACTION: MATERIALIZE_AUTOPILOT_R5_AUTHORITY
```

The known P2 remains open and non-blocking. Bounded capability execution,
final leader reap, absence of descendants, removal of the process group, no
orphan, and fail-closed behavior are preserved; only diagnostic cause ordering
is carried forward. This R4 closure does not authorize or materialize R5,
implement an automatic fallback or workflow engine, execute F2E, or change
runtime, productive-authority, or cutover state.
