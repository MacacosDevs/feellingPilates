# FeelingPilates — HANDOFF: AUTOPILOT R5 Python SDK primary adapter

**Materialization role:** `R5_AUTHORITY_DESIGNER /
PRIMARY_EXECUTOR_ARCHITECT / SDK_ADAPTER_BOUNDARY_DESIGNER`

**Workflow profile:** `DOCUMENTATION_ONLY / AUTHORITY_MATERIALIZATION /
NO_IMPLEMENTATION / NO_APPROVAL / NO_ACTIVATION / NO_PUBLICATION`

## Authority, lifecycle, and canonical target

This handoff materializes the candidate authority for:

```text
AUTOPILOT R5 — Python SDK primary adapter
Package baseline: openai-codex==0.147.0
Architectural role: AgentExecutor PRIMARY
```

Repository authority confirms this target. Accepted R1 selected
`PYTHON_SDK_PRIMARY`; accepted R2 materialized the transport-neutral
`AgentExecutor`; closed R3 materialized durable `StateStore`; and closed R4
implemented Codex CLI only as `FALLBACK / DIAGNOSTIC`. No competent canonical
defines a different Autopilot R5 target.

The candidate authority was materialized before fresh independent audit. The
fresh independent handoff audit at
`auditoria/reviews/AUTOPILOT-R5-PYTHON-SDK-ADAPTER-HANDOFF-AUDIT.md` passed;
this document is now the approved, active authority for the exact R5 target.
It authorizes implementation to start, but does not claim implementation,
acceptance, publication, closure, runtime activation, productive authority, or
cutover.

```text
R5 HANDOFF: MATERIALIZED / APPROVED / ACTIVE
R5 HANDOFF AUDIT: PASS / P0=0 / P1=0 / P2=0
R5 TARGET: AUTHORIZED_TO_START / NOT_STARTED
R5 IMPLEMENTATION: AUTHORIZED_TO_START
ACTIVE_AUTOPILOT_HANDOFF: R5 Python SDK primary adapter
NEXT ALLOWED ACTION: EXECUTE_ACTIVE_AUTOPILOT_R5_PYTHON_SDK_ADAPTER
R6: NOT_AUTHORIZED
F2E: UNCHANGED
auto_publish: false
```

The historical materialization did not itself establish audit `PASS`, approval,
activation, implementation authority, publication, runtime activation,
productive authority, or cutover. The later fresh independent handoff audit
and competent explicit lifecycle action now approve and activate this exact
target and exact allowlist. The activation does not establish implementation,
acceptance, publication, runtime activation, productive authority, or cutover.

## Materialization baseline

Physical pre-flight before this document was written:

```text
Repository: /Users/jesusaldaircruzortiz/Desktop/Feelingpilates/feelingpilates-autopilot-r1
Branch: orquestacion/autopilot-r1
HEAD: 9a998468d64b0e075c66c12af7bab317df9b622b
Resolved upstream: 9a998468d64b0e075c66c12af7bab317df9b622b
Live remote branch: 9a998468d64b0e075c66c12af7bab317df9b622b
Staging: EMPTY
Working tree: CLEAN
Python: 3.14.6
Current environment openai-codex installation: NOT INSTALLED
```

The absent package is not a version mismatch and is not repaired by this
documentation-only action. Accepted R1 remains the package/capability
authority. A future implementation must add the exact runtime dependency and
must fail closed if the installed version or capability surface differs from
the accepted profile.

## Architectural position and ownership

The architecture is frozen as:

```text
                   AgentExecutor
                        |
             +----------+----------+
             |                     |
     Python SDK Adapter       Codex CLI Adapter
          PRIMARY             FALLBACK / DIAGNOSTIC
            R5                       R4
```

R5 implements one bounded requested agent turn behind the existing
provider-neutral `AgentExecutor` operations:

```text
capabilities
start
resume
interrupt
get_result
```

The current `ExecutionRequest`, `ExecutionHandle`, `ExecutionObservation`,
`ExecutionStatus`, `ExecutorCapabilities`, `SessionReference`, `AgentResult`,
`UsageRecord`, and `FailureRecord` contracts are sufficient for R5. R5 must not
change the port or domain schemas merely to expose SDK-native convenience.

R5 does not decide:

- workflow phase, legal transition, next gate, or continuation;
- retry, backoff, quota, model-routing, or global failure policy;
- automatic SDK-to-CLI fallback;
- publication or any Git lifecycle operation;
- idempotency, checkpoint commitment, recovery action, or workflow advancement;
- repository context selection or prompt compilation; or
- StateStore persistence.

Those decisions belong to later explicitly authorized orchestration. A
consumer depends on `AgentExecutor`, never on `openai_codex` classes, SDK event
types, transport exceptions, thread objects, turn handles, or app-server
details.

## Official SDK, capability, and authentication boundary

Accepted R1 authority fixes the tested baseline:

```text
Package: openai-codex
Version: 0.147.0
Runtime: Python 3.14-compatible
Authentication: existing local ChatGPT/Codex login
Observed surface: Codex, thread_start, thread_resume, Thread.run,
  Thread.turn, TurnHandle.interrupt, Sandbox, typed TurnResult
```

The future implementation must pin `openai-codex==0.147.0` in
`pyproject.toml` and record the R5 capability profile in
`runtime-contract.json`. Adapter construction or an explicit bounded startup
self-test must verify the installed distribution version and the exact
new-thread, resume, structured-output, sandbox, model, reasoning, event/usage,
and interruption capabilities used by R5. Unsupported, missing, contradictory,
or unrecognized capability evidence fails closed before semantic success.

Provider/version names and spellings remain infrastructure details. A future
version is not accepted merely because it imports. Supporting another version
requires a separately reviewed explicit capability profile; silent best-effort
compatibility is forbidden.

R5 uses the existing local ChatGPT/Codex authentication architecture. It must
not:

- make `OPENAI_API_KEY`, `CODEX_API_KEY`, or another API key mandatory;
- read, copy, inspect, serialize, or persist credential-file contents;
- persist tokens, cookies, headers, environment dumps, or SDK client state;
- copy authentication files into the repository or StateStore; or
- include secrets in normal result, event, failure, or diagnostic evidence.

The SDK may use the trusted local runtime state through its normal supported
mechanism. Authentication availability and local state-directory access are
runtime capability conditions, not workflow authority. Tests inject only a
fake SDK seam and sentinel secrets; the normal suite never accesses real local
credentials.

## Request, workspace, model, and context bounds

R5 receives already-prepared bounded `ExecutionRequest.instructions`. It is not
`ContextCompiler` and must not select repository files, synthesize context, or
expand a prompt from the checkout.

Before invoking the SDK, the adapter validates all required request identity,
positive timeout, configured model/effort allowlists, absolute existing working
directory, symlink-aware containment in the configured authorized workspace
root, and the exact sandbox mapping:

```text
READ_ONLY       -> SDK read-only Sandbox
WORKSPACE_WRITE -> SDK workspace-write Sandbox
```

Unknown or broader sandbox authority fails closed. R5 must define positive
finite limits for instruction UTF-8 bytes, final structured-output bytes,
recognized event count, per-event projected evidence, total projected event
evidence, and retained diagnostics. Exceeding a semantic result bound fails
closed; truncating a structured candidate and parsing its prefix is forbidden.
Diagnostic truncation is explicit and never represented as complete evidence.

R5 passes the canonical `agent-result.schema.json` as the SDK output schema. It
does not accept a schema path or schema body from agent output. No temporary
schema, event, or capture artifact may be created inside the Git checkout.

## New session and session-reference contract

For a new turn, the adapter creates exactly one SDK thread with `thread_start`
and extracts the durable thread identity from the typed returned thread/session
surface fixed by the supported capability profile. The SDK-created identity is
required for the R5 primary adapter because it is the operational reference for
same-process continuation and cross-process resume.

The identity must be a nonempty bounded string in the accepted session grammar.
It is validated, not trimmed, guessed, hashed, repaired, or extracted from
free-form diagnostics. Recognized SDK notifications that also carry thread
identity are auxiliary evidence and must reconcile with the returned typed
thread identity.

```text
One valid returned ID, with absent or identical event IDs -> one SessionReference
Missing/blank/malformed returned ID -> SDK_SESSION_FAILURE
Any distinct recognized event ID -> SDK_SESSION_FAILURE / no selected reference
Repeated identical recognized IDs -> deduplicated evidence / one reference
```

The reference is bound to exact run, adapter (`openai-codex`), role, and
attempt context. An optional `AgentResult.session_reference`, when present,
must exactly equal the independently observed SDK thread ID. Its absence does
not discard the independently observed operational reference; a conflict fails
closed.

If a valid unique session is observed before a later turn failure, timeout,
cancellation, malformed result, or provider error, the failed/interrupted
`ExecutionObservation` preserves that reference. If session evidence is absent,
malformed, or conflicting, R5 must not fabricate a resumable reference.

```text
SessionReference exists != workflow may continue
SessionReference persisted != resume is authorized
SDK thread exists != gate or workflow authority
```

The reference is operational evidence only. R3 `StateStore` and a later engine
remain responsible for durable recovery and eligibility decisions.

## Continuation, resume, and cross-process semantics

`resume` requires a caller-supplied `ExecutionHandle` with persisted
`resume_context`, exactly one `SessionReference`, and a compatible
`Checkpoint`. Before any SDK call, R5 fails closed on:

- missing, blank, malformed, or overlong thread identity;
- run mismatch among handle, request, checkpoint, and session;
- workflow mismatch between request and checkpoint;
- adapter mismatch, including a Codex CLI session supplied to R5;
- role, attempt, or opaque-reference mismatch; or
- checkpoint session conflict.

Resume never searches prior logs and never starts a replacement thread. A
rejected/missing SDK session becomes `SDK_SESSION_FAILURE`; it is not silently
converted into a new turn or CLI execution.

Within one process, R5 may reuse the live SDK thread object only when its
validated identity and full provenance exactly match the supplied reference.
Otherwise it reconstructs the thread using `thread_resume`. A fresh adapter
instance in the same or a new process must be able to resume using only the
caller-persisted reference/context and SDK-supported durable identity. Both
paths must produce equivalent domain observations.

“Cross-adapter” testing means a second independently constructed instance of
the R5 SDK adapter. Cross-provider resume is forbidden: an R4 CLI reference is
not accepted by R5, and an R5 reference is not accepted by R4.

The SDK-returned resumed thread identity, and any recognized identity events,
must exactly equal the caller-supplied ID before a semantic success can be
reported.

## Structured result boundary

The typed SDK `TurnResult` and SDK notifications are provider evidence, not
domain result types. For a terminal semantic turn, the only structured-result
candidate is the bounded final response exposed by the supported typed
`TurnResult` profile. Event messages and free-form diagnostics cannot supply a
second or replacement canonical result.

R5 must decode exactly one complete JSON object without heuristic repair and
enforce both the canonical schema and internal domain invariants. It preserves
R2/R4 strictness, including:

- `schema_version`, workflow, run, role, and gate identity;
- canonical agent status and gate-result vocabulary;
- nonempty summary and recommendation;
- exact P0/P1/P2 findings shape;
- strict nested test evidence and enum validation;
- strict changed-path, artifact, correction, boolean, session, and usage shape;
- `additionalProperties: false` / unknown-field rejection at every governed
  boundary; and
- the existing strict terminal `ExecutionObservation` semantics.

SDK schema enforcement is defense in depth and does not replace local schema,
relational, and dataclass validation. The future bounded refactor of
`result_parser.py` may expose its existing strict AgentResult/usage parsing to
both adapters by accepting an explicit adapter provenance. CLI JSONL parsing
and R4 behavior must remain unchanged.

Malformed JSON, missing output, multiple candidates, truncated output,
unknown fields, nested evidence defects, invalid enums, wrong identity,
embedded usage without matching runtime evidence, session conflict, or any
domain invariant violation produces `RESULT_CONTRACT_FAILURE`. R5 must not
repair, coerce, complete, retry, or publish a canonical `AgentResult` from
invalid output. SDK completion alone is not semantic success.

## SDK event and streaming boundary

R5 consumes events through an adapter-local translator. SDK classes and
version-specific strings never enter domain, ports, StateStore, or workflow
authority. The translator projects only allowlisted evidence needed for:

- thread/session reconciliation;
- lifecycle observation;
- directly attributable current-turn usage;
- terminal error evidence; and
- cancellation/cleanup confirmation.

Unknown SDK events do not become workflow state, `AgentResult`, gate evidence,
session identity, usage, or success. They may contribute only a bounded,
sanitized event-type diagnostic counter. R5 must never serialize `repr(event)`,
arbitrary provider payloads, complete client objects, or unknown fields into
normal observations. A malformed recognized session, usage, terminal, or error
event fails the corresponding adapter contract closed.

Provider event ordering cannot override the terminal arbitration rules below.
The typed final `TurnResult` remains the sole result candidate; events remain
operational evidence.

## Timeout, explicit cancellation, and cleanup

Every turn has a positive finite request timeout. `start` returns an
`ExecutionHandle` while adapter-owned bounded execution bookkeeping manages the
active SDK turn. `interrupt` must genuinely target that active turn through
the supported `TurnHandle.interrupt()` surface, not merely set a local flag.

On timeout or explicit cancellation R5 must:

1. atomically record the winning termination cause;
2. request SDK interruption of the active turn;
3. cancel/close any adapter-owned task or async bridge when necessary;
4. wait only a bounded cleanup interval;
5. join and dispose every adapter-owned thread/task/loop/client resource;
6. preserve bounded partial event evidence, directly observed session, and
   safe diagnostics; and
7. produce no fabricated successful `AgentResult`.

Timeout and explicit cancellation remain distinct:

| Cause | Observation |
| --- | --- |
| Request timeout wins before terminal success | `FAILED`, `timed_out=true`, `cancelled=false` |
| Explicit caller cancellation wins before terminal success | `INTERRUPTED`, `cancelled=true`, `timed_out=false` |
| Fully validated success commits before cancellation is accepted | `SUCCEEDED` |

The race is decided under one adapter-local terminal lock. Once timeout or
accepted cancellation wins, late output cannot be promoted to success. A
cancellation requested after an already committed terminal observation is a
no-op. Failure to interrupt or clean up fails closed and remains visible in
bounded evidence; no unmanaged background task, thread, event loop, or SDK turn
may remain.

Global retry/backoff and fallback policy are outside R5.

## Failure normalization

R5 returns only the existing `FailureRecord` vocabulary. The supported
0.147.0 profile must normalize at least:

```text
invalid/rejected/missing/conflicting SDK session -> SDK_SESSION_FAILURE
typed transport/app-server closure or SDK invocation failure -> SDK_TRANSPORT_FAILURE
structured result, recognized-event, or local domain validation defect -> RESULT_CONTRACT_FAILURE
timeout -> SDK_TRANSPORT_FAILURE with timed_out=true
explicit cancellation -> SDK_TRANSPORT_FAILURE with INTERRUPTED/cancelled=true
```

`NETWORK_UNAVAILABLE` or `QUOTA_EXHAUSTED` may be used only when an explicitly
versioned typed SDK error/code provides direct unambiguous evidence. R1 did not
establish those concrete signal shapes, so free-form message matching,
substring classifiers, inferred quota policy, and optimistic retryability are
forbidden. Unknown provider failures normalize conservatively to
`SDK_TRANSPORT_FAILURE`, with bounded safe provider evidence auxiliary only.

R5 does not decide `retryable=true` from policy or prose. A later orchestration
layer owns retry decisions. A provider failure does not invent P0/P1/P2, a
gate result, or a workflow transition.

## Telemetry truthfulness

Usage is operational evidence for the exact requested turn. R5 maps only
fields whose semantics are directly supported by the verified 0.147.0 typed
usage/event profile and attributable to that turn.

```text
Directly observed class -> value, including 0, with OBSERVED provenance
Unavailable class -> NULL / UNAVAILABLE
Observed aggregate only -> aggregate populated; component classes remain NULL
Cumulative session value not attributable to this turn -> unavailable for turn usage
model_context_window -> never token consumption
```

No subtraction of cumulative counters, proportional allocation, heuristic
decomposition, inference from text/bytes/pricing/context length, or translation
from a different token class may be marked `OBSERVED`. Richer SDK usage is
persisted only when each exact internal token class has direct supported
evidence. Unknown extra SDK counters remain bounded provider evidence and do
not silently extend the domain.

An agent-produced embedded `usage_record` must pass the complete strict schema
and its `adapter` must identify `openai-codex`. Every embedded `OBSERVED` value
must exactly match adapter-observed current-turn usage; missing runtime support
or contradiction fails closed. Observed zero remains `0`; unavailable remains
`NULL`.

## Secret, StateStore, Git, and fallback isolation

R5 produces `ExecutionObservation`. It must not directly call `StateStore`,
advance durable state, commit checkpoints, consume idempotency keys, acquire
leases, select recovery action, or infer continuation from a session. No SDK
thread/client object is serializable durable authority.

R5 must not invoke Codex CLI on any SDK outcome. In particular, this is
forbidden:

```text
try SDK
catch failure
execute CLI automatically
```

R4 remains a separately selectable fallback/diagnostic capability for a future
router or engine. R5 does not implement that router.

R5 performs no `git add`, commit, push, merge, rebase, reset, clean, or stash.
`WORKSPACE_WRITE` grants only the separately authorized agent turn's workspace
scope; it is not Git or publication authority. `auto_publish` remains `false`.

Normal serialization and diagnostics must exclude credential material,
environment dumps, request headers, cookies, auth paths, SDK client internals,
arbitrary event payloads, and sentinel secrets even when a fake/provider error
contains them.

## Exact future R5 implementation allowlist

Following the fresh independent handoff audit `PASS` and explicit
approval/activation, an R5 executor may create or modify exactly these 12
paths:

```text
tools/autopilot/README.md
tools/autopilot/pyproject.toml
tools/autopilot/config/runtime-contract.json
tools/autopilot/src/feelingpilates_autopilot/adapters/__init__.py
tools/autopilot/src/feelingpilates_autopilot/adapters/execution/__init__.py
tools/autopilot/src/feelingpilates_autopilot/adapters/execution/result_parser.py
tools/autopilot/src/feelingpilates_autopilot/adapters/execution/codex_sdk.py
tools/autopilot/src/feelingpilates_autopilot/adapters/execution/sdk_events.py
tools/autopilot/tests/fixtures/fake_codex_sdk.py
tools/autopilot/tests/test_codex_sdk_adapter.py
tools/autopilot/tests/test_codex_sdk_results.py
tools/autopilot/tests/test_codex_sdk_lifecycle.py
```

Allowlist rationale:

- `README.md`, `pyproject.toml`, and `runtime-contract.json` record the primary
  adapter, exact dependency/capability baseline, auth posture, and unchanged
  no-routing/no-publication architecture;
- the two adapter package `__init__.py` files expose the implementation without
  import-time I/O;
- `result_parser.py` receives only the minimum backward-compatible
  transport-neutral extraction needed to reuse R2/R4 strict result and usage
  validation with explicit adapter provenance;
- `codex_sdk.py` owns client/thread/turn lifecycle, request validation,
  capability checks, timeout/cancellation, normalization, and observation;
- `sdk_events.py` isolates version-specific event/usage projection and bounds;
  and
- one fake plus three test modules follow the current fixture and
  `test_<adapter>_<behavior>.py` conventions while separating result contracts
  from session/lifecycle races.

No change to `ports/agent_executor.py`, domain models, JSON schemas, R4 CLI
adapter, CLI command builder, StateStore, migrations, or product code is
required. The existing port and schemas are sufficient. If implementation or a
fresh audit proves otherwise, the target stops for an explicit authority
correction; the executor must not add, substitute, rename, or wildcard a path.

```text
ALLOWLIST_EXACT: SI
ALLOWLIST_SUFFICIENT: SI
ALLOWLIST_MINIMAL: SI
KNOWN_PATH_GAP: NONE
```

## Deterministic test authority

The normal R5 suite uses the fake SDK seam and is offline, deterministic, and
non-destructive. It must not require provider/network access, consume tokens,
read local credentials, execute Codex CLI, mutate Git, create StateStore data,
or execute F2E.

Material coverage must include:

- successful new bounded turn and exact required SDK session extraction;
- strict valid `AgentResult` success;
- malformed JSON/result, missing result, duplicate candidate, unknown field,
  invalid enum, empty required summary, nested evidence defect, invalid
  correction shape, and truncated result failure;
- wrong workflow, run, role, or gate identity;
- typed SDK/provider error normalization and conservative unknown-error mapping;
- same-process continuation through an identity-matched live thread;
- resume through a fresh R5 adapter instance using only persisted context and
  session evidence, representing cross-process recovery;
- rejection of missing, invalid, conflicting, cross-run, cross-role,
  cross-attempt, cross-checkpoint, cross-adapter, and returned-mismatch resume
  identity;
- preservation of a valid observed session on later failed, timed-out, or
  cancelled turn;
- timeout physically interrupting the active SDK turn;
- explicit cancellation physically interrupting the active SDK turn;
- success/cancel and timeout/cancel races with deterministic terminal
  precedence;
- bounded cleanup proving no adapter-owned task/thread/loop/turn remains;
- bounded instruction, result, event-count, event-evidence, and diagnostic
  handling;
- recognized malformed event failure and unknown event non-authority;
- directly observed usage present, absent-to-`NULL`, directly observed zero,
  aggregate-only semantics, richer exact-field mapping, and rejection of
  malformed/contradictory embedded usage;
- cumulative/unattributable and `model_context_window` values not represented
  as observed turn consumption;
- sentinel credentials/provider payload absent from observations, failures,
  serialization, and ordinary diagnostics;
- no credential/auth-file serialization or inspection;
- no hidden StateStore write or import;
- no subprocess/Codex CLI invocation and no automatic fallback; and
- all existing R2–R4 tests remaining green.

An optional real-SDK smoke probe is separate from the normal suite and requires
explicit later authorization. It must be bounded, non-destructive, use a
disposable workspace, preserve local-auth secrecy, record the exact installed
version/capabilities, and consume no authority beyond a single harmless turn.
It cannot replace deterministic tests or approve implementation.

## Carry-forward preservation

```text
R4 P2 — CAPABILITY_TIMEOUT_PRIMARY_CAUSE_MASKED_BY_PRE_REAP_GROUP_LIVENESS:
  OPEN / NON_BLOCKING / CARRY_FORWARD / OUTSIDE_R5_SCOPE

R2 Debt C — attached branch without upstream behavioral coverage:
  OPEN / NON_BLOCKING / CARRY_FORWARD / OUTSIDE_R5_SCOPE
```

R4 P2 concerns the CLI capability-probe diagnostic cause ordering. R5 does not
use or redesign that process-group probe and therefore supplies no competent
closure evidence. R2 Debt C concerns repository/upstream behavior, not SDK
execution. Neither item authorizes scope expansion.

## Explicit exclusions

R5 does not authorize:

- Codex CLI redesign, R4 P2 correction, or automatic fallback;
- `ContextCompiler`, model router, workflow engine, or retry/quota governor;
- Git adapter, worktree manager, publisher, supervisor, or launchd;
- StateStore workflow advancement, hidden persistence, or idempotency policy;
- new schemas or weakening of R2 domain contracts;
- R6 or any later Autopilot implementation;
- F2E/product code, tests, execution, data work, migration, fence, activation,
  productive-authority change, or cutover; or
- staging, commit, push, or publication.

## Fresh independent audit history and future implementation audit

The fresh, independent, read-only, adversarial handoff audit is persisted at
`auditoria/reviews/AUTOPILOT-R5-PYTHON-SDK-ADAPTER-HANDOFF-AUDIT.md`. It
verified canonical target, architecture, exact allowlist, SDK/auth isolation,
structured-output strictness, session/resume and cross-process recovery,
timeout/cancellation, telemetry truthfulness, secret boundaries,
`AgentExecutor` compatibility, StateStore isolation, absence of automatic CLI
fallback, exclusions, and exact scope. Its `P0=0 / P1=0 / P2=0` result and the
separate competent lifecycle action activate this target and authorize R5
implementation to start.

If implementation is later authorized and materialized, a different fresh
independent implementation audit must materially inspect:

- exact SDK pin and supported capability profile;
- no import-time I/O and no provider-native type escaping the adapter;
- local-auth use without credential access or persistence;
- strict schema/domain/identity and terminal-result enforcement;
- deterministic new-session and resume behavior across adapter instances;
- physical timeout/cancellation and complete task/thread/turn cleanup;
- bounded event/result/diagnostic handling and unknown-event non-authority;
- stable conservative failure normalization without global policy;
- truthful current-turn telemetry with `NULL != 0`;
- secret/sentinel exclusion;
- no StateStore write, Git lifecycle, CLI invocation, or automatic fallback;
- all required offline tests and R2–R4 regression tests; and
- exact 12-path touched scope with unchanged unrelated files.

The R5 executor or corrector cannot perform its own independent audit or
declare implementation `PASS`.

## Stop conditions

Stop without implementation or authority expansion if:

- branch, expected baseline, staging, or materialized authority is inconsistent;
- a canonical source defines a materially different R5 target;
- implementation requires any path outside the exact allowlist;
- the existing provider-neutral port or schemas must be weakened;
- the supported SDK cannot provide required durable identity, resume,
  structured output, physical interruption, bounded cleanup, or truthful usage;
- local ChatGPT/Codex auth would require credential copying or mandatory API
  keys;
- deterministic offline tests cannot exercise the SDK seam;
- automatic CLI fallback, workflow policy, StateStore mutation, Git lifecycle,
  R6, or F2E becomes necessary; or
- a material technical decision lacks repository authority.

A stop reports the exact gap. It does not invent an implementation or silently
substitute CLI behavior.

## Final preserved authority

```text
R1: ACCEPTED / PUBLISHED / HISTORICAL
R2: PUBLISHED / CLOSED / HISTORICAL
R2 Debt A: CLOSED_BY_R3
R2 Debt B: CLOSED_BY_R4
R2 Debt C: OPEN / NON_BLOCKING / CARRY_FORWARD / OUTSIDE_R5_SCOPE
R3: CLOSED / HISTORICAL
R4: IMPLEMENTED / ACCEPTED / PUBLISHED / CLOSED / HISTORICAL
R4 P2: OPEN / NON_BLOCKING / CARRY_FORWARD / OUTSIDE_R5_SCOPE
Primary executor: Python SDK / PRIMARY
Codex CLI: HISTORICAL R4 CAPABILITY / FALLBACK / DIAGNOSTIC
Automatic fallback: NOT_IMPLEMENTED / NOT_AUTHORIZED
Workflow engine: NOT_IMPLEMENTED / NOT_AUTHORIZED
R5 HANDOFF: MATERIALIZED / APPROVED / ACTIVE
R5 HANDOFF AUDIT: PASS / P0=0 / P1=0 / P2=0
R5 TARGET: AUTHORIZED_TO_START / NOT_STARTED
R5 IMPLEMENTATION: AUTHORIZED_TO_START
ACTIVE_AUTOPILOT_HANDOFF: R5 Python SDK primary adapter
R6: NOT_AUTHORIZED
F2E: UNCHANGED
auto_publish: false
NEXT ALLOWED ACTION: EXECUTE_ACTIVE_AUTOPILOT_R5_PYTHON_SDK_ADAPTER
```

Current approved disposition:

```text
R5 AUTHORITY ACTIVATED — READY TO IMPLEMENT
```
