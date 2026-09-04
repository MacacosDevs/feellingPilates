# FeelingPilates — HANDOFF: AUTOPILOT R3 durable state and recovery foundation

**Original execution role:** `AUTOPILOT_R3_AUTHORITY_DOCUMENTER /
DURABLE_STATE_SCOPE_MATERIALIZER / RECOVERY_CONTRACT_DESIGNER`

**Workflow profile:** implementation/infrastructure/durability authority
materialization and bounded handoff activation. This handoff records authority
to begin the exact target; it is not implementation, an implementation PASS,
target completion, publication, closure, runtime activation, productive
authority, or cutover.

## Authority and bounded target

This handoff materializes the authority for the next Autopilot infrastructure
target:

```text
AUTOPILOT R3 — SQLite durable state and recovery foundation
```

R3 is the concrete, durable `StateStore` foundation required for an Autopilot
process to survive process and machine interruption without reconstructing its
workflow history from chat context. It persists state; it does not decide
workflow or product policy.

R3 must preserve the accepted R1 and R2 authority:

```text
R1: ACCEPTED / PUBLISHED / HISTORICAL
Runtime: PYTHON_SDK_PRIMARY
Primary execution: Codex SDK
Fallback: Codex CLI
Durable-state direction: SQLite through Python sqlite3 behind StateStore
auto_publish: false

R2: PUBLISHED / CLOSED / HISTORICAL
R2 active: NO
R2 implementation publication commit: ec440841889bcfc7cd73279a1219de4e84054b1f
R2 closure commit: c25756cd5a3208330f1df8c3402a6c261ce9a5c5
R2 implementation audit: P0=0 / P1=0 / P2=1
R2 publication closure audit: P0=0 / P1=0 / P2=0
```

The repository and its canonical documents prevail over chat. A future R3
executor must independently verify branch, `HEAD`, resolved upstream, staging,
working tree, this handoff, current Autopilot state, applicable protocol
authority, and the fresh handoff audit before writing. A mismatch must stop;
it must not be repaired by that executor.

## Required lifecycle before implementation

R3 implementation is forbidden until all of the following are physically
present:

1. This handoff is materialized.
2. A fresh, independent R3 handoff audit is recorded.
3. A competent lifecycle transition explicitly approves and activates this
   exact target for implementation.

After implementation, deterministic R3 tests and a fresh, independent R3
implementation audit are required. Any correction and re-audit are bounded by
their own authorized lifecycle. Publication and publication closure remain
separate gates. The executor, corrector, and documenter do not self-audit.

After the initial materialization, first fresh audit, Correction.1, fresh
re-audit.1, bounded Correction.2, and the fresh independent handoff re-audit,
the lifecycle is:

```text
MATERIALIZED
APPROVED
ACTIVE
TARGET_AUTHORIZED_TO_START
IMPLEMENTATION_AUTHORIZED_TO_START
TARGET_NOT_STARTED
```

It must not be represented as `IMPLEMENTED`, `IMPLEMENTATION_PASS`,
`TARGET_COMPLETED`, `PUBLISHED_TARGET`, `PUBLISHED`, `CLOSED`, `HISTORICAL`,
or `R4_AUTHORIZED`.

The lifecycle history preserved by this handoff is:

```text
INITIAL FRESH AUDIT: P0=0 / P1=9 / P2=0
CORRECTION.1: MATERIALIZED
FRESH RE-AUDIT.1: P0=0 / P1=1 / P2=0
P1-1 THROUGH P1-9: CLOSED_BY_FRESH_REAUDIT
P1-10 LIFECYCLE INCONSISTENCY: CLOSED_BY_FRESH_REAUDIT
CORRECTION.2: MATERIALIZED / CONFIRMED_BY_FRESH_REAUDIT
FRESH HANDOFF RE-AUDIT.2: P0=0 / P1=0 / P2=0
```

The fresh independent re-audit confirms Correction.2 and closes P1-10. The
competent lifecycle action recorded here approves and activates only this exact
R3 target and its unchanged 14-path implementation allowlist.

## R2 carry-forward debt

`R2 P2-1` remains `OPEN / NON_BLOCKING / CARRY_FORWARD`. The R2 contracts are
accepted; this is behavioral test-coverage debt, not authorization to reopen
R2. All three items remain visible:

- A. `LeaseResolution.NONE / NO_RELEVANT_LEASE` behavioral coverage is absent.
- B. Malformed embedded `usage_record` behavioral validation is incomplete.
- C. Attached branch without upstream behavioral coverage is absent.

Because lease recovery is within a future R3 implementation, that work may
provide behavioral coverage relevant to item A. This handoff does not close
item A. Only an implemented test and competent fresh audit may later record
that individual item as `CLOSED_BY_R3`. Items B and C are outside R3 scope and
must remain `OPEN / NON_BLOCKING / CARRY_FORWARD`.

## SQLite and runtime-location contract

The future implementation uses only Python stdlib `sqlite3`; no ORM, database
framework, third-party SQLite driver, or external locking system is authorized.
It must configure and verify, as appropriate:

```text
foreign_keys = ON
journal_mode = WAL
busy_timeout = configurable
synchronous = FULL
```

Critical state-changing transactions must explicitly use a transaction mode
equivalent to `BEGIN IMMEDIATE` where required to protect transitions and
leases. They must not rely on SQLite implicit transaction behavior.

The `StateStore` must accept an explicit database path or configuration. No
runtime database may be created inside the Git checkout. An eventual operating
default may be external runtime state, conceptually:

```text
~/Library/Application Support/FeelingPilatesAutopilot/
```

Tests must instead use disposable temporary directories and real temporary
SQLite files. Neither the implementation nor tests may leave `.db`, `.sqlite`,
`.sqlite3`, `-wal`, `-shm`, backup databases, temporary migration artifacts,
`__pycache__`, or `.pyc` files in the repository.

### Binding connection-PRAGMA lifecycle

Every SQLite connection opened by the future `StateStore`, including a
connection opened after process restart, must be configured and verified before
any semantic use. This is connection-scoped operational configuration, distinct
from durable semantic workflow state. A new connection must not silently lose
the required behavior merely because a prior process or connection configured
the database.

Before accepting a connection or store as operational, the future adapter must
establish and verify:

```text
PRAGMA foreign_keys = ON
PRAGMA busy_timeout = configured value
PRAGMA synchronous = FULL
effective PRAGMA journal_mode = wal
```

`foreign_keys`, `busy_timeout`, and `synchronous` are connection-scoped and
must be configured for every opened connection. `journal_mode=WAL` is a
database-level persistent setting: bootstrap may establish it when necessary,
but every store-open path must observe and validate the effective mode before
semantic use. It need not rewrite WAL on every connection when the database is
already in that mode. If a mandatory setting cannot be established or its
effective value cannot be verified, the adapter must fail closed and the store
is not operational.

## Migration contract

R3 must establish deterministic, versioned SQLite migrations. Migrations are
canonical implementation artifacts, not runtime-generated ORM metadata. The
migration runner must provide:

- ordered migration identifiers;
- a migration checksum and a `schema_migrations` registry;
- applied timestamps;
- idempotent execution;
- atomic application of one migration where SQLite permits;
- fail-closed checksum-drift detection for an already-applied migration;
- no silent applied marker after an interrupted or failed migration; and
- fail-closed rejection of an unknown future schema version.

An already-applied migration must never be silently rewritten. The initial
canonical migration may establish the complete initial R3 schema at
`tools/autopilot/migrations/001_initial.sql`.

### Binding migration checksum authority

A migration checksum is the lowercase hexadecimal SHA-256 digest of the raw
file bytes of its repository artifact. For example, the checksum input for
`tools/autopilot/migrations/001_initial.sql` is exactly the byte sequence
returned by reading that file in binary mode. Before hashing, the future runner
must not normalize line endings, decode and re-encode text, trim whitespace,
remove a BOM, parse SQL, remove comments, format SQL, or apply any
platform-specific conversion.

Migration execution may decode the artifact as SQLite requires only after the
raw-byte checksum is calculated. Checksum authority and execution decoding are
separate concerns. The migration registry must persist the exact checksum of
the repository artifact applied. An existing migration identifier paired with a
different raw-byte SHA-256 is checksum drift and must fail closed.

## Durable domain and relationship contract

The R3 schema must materialize the concrete durable concepts required by the
accepted R2 `StateStore` contract, at minimum:

```text
schema_migrations
workflows
runs
phases
attempts
transitions
checkpoints
agent_sessions
usage_records
failures
human_decisions
leases
```

It must also include only the minimal artifact/evidence-reference persistence
already required by that contract. It must not add unrelated product tables.

R2's explicit typed logical identities remain canonical: workflow, run, phase,
attempt, transition, checkpoint, artifact, session, usage, failure, lease, and
human-decision IDs are not replaced by internal database row IDs. Durable
relationships must enforce foreign keys. Delete behavior must be conservative:
audit and recovery history must not be erased through convenience cascades.

The existing `Workflow`, `Run`, `Attempt`, `StateTransition`, `Checkpoint`,
`Artifact`, `SessionReference`, `UsageRecord`, `FailureRecord`, `Lease`,
`HumanDecision`, and `RunRecoveryContext` contracts remain conceptually
compatible. R3 may extend only `domain/models.py` and `ports/state_store.py`
as necessary for concrete durable storage; it must not weaken telemetry
truthfulness, run-based recovery, lease ambiguity, AgentExecutor,
RepositorySnapshot, or checkpoint semantics.

## Concurrency, leases, fencing, and idempotency

Durable mutable aggregates that need concurrent protection must carry explicit
version or fencing data. State-changing operations must be able to reject a
stale expected version; a stale worker must never silently overwrite newer
durable state.

Leases are durable coordination state, with capabilities to acquire, inspect,
renew, release, and expire or recover. Each lease must relate to its
run/workspace/purpose according to the accepted R2 contract. Recovery starting
from `run_id` must resolve, fail-closed, exactly one of:

```text
NO_RELEVANT_LEASE
ONE_RELEVANT_LEASE
AMBIGUOUS_OR_INCONSISTENT_LEASE
```

The accepted R2 equivalents are `LeaseResolution.NONE`,
`LeaseResolution.ONE_RELEVANT`, and `LeaseResolution.AMBIGUOUS`. Multiple
conflicting leases must never be selected silently.

Lease acquisition must issue a durable monotonic fencing token or equivalent
generation. A mutation protected by a lease must reject a stale fencing token;
PID or timestamp alone is insufficient. This protects against a worker that
resumes after its lease expired and a new worker acquired a later lease.

R3 must provide durable idempotency for applicable state-changing operations
and checkpoints. The same idempotency key with the same operation identity
must not produce duplicate semantic state. Reuse of a key with a conflicting
operation or payload must fail closed. Retries are not inherently safe.

### Binding lease time and protected-resource authority

Lease timestamps must be persisted as UTC instants using an ISO-8601 /
RFC3339-compatible representation with an explicit `Z` or UTC offset, unless a
later repository-consistent contract fixes another exact UTC representation. No
local-timezone or naive-datetime authority is permitted. Lease expiration is
evaluated against wall-clock UTC as `current_time >= expires_at`; at equality,
the lease is expired. The future durable lease domain must obtain current time
through the accepted `Clock` abstraction and its tests must use an injected,
deterministic fake `Clock`; it must not use direct `datetime.now()` while that
port is available.

Wall-clock UTC is the durable expiry authority because it survives process and
machine restart. A process-local monotonic clock may assist a live process but
must not be persisted as durable expiry authority. This requirement does not
introduce distributed-clock infrastructure.

Fencing-token monotonicity is per explicit `protected_resource_key`, not a
single global counter and not an ad-hoc tuple. That key is a stable, durably
representable identity for the exclusivity boundary being leased (for example,
a workspace/worktree resource, run-owned resource, or publication resource).
The lease's run, workspace, and purpose associations may remain metadata, but
PID, timestamp, owner name, or `run_id` alone must not implicitly define the
scope unless the future operation explicitly defines `run_id` itself as the
protected resource. For one `protected_resource_key`, every successful new
acquisition or reacquisition must issue a fencing token strictly greater than
every token previously issued for that key. Different keys need not share a
counter.

### Binding lease and fencing atomicity

Lease acquisition and reacquisition must use one explicit critical transaction
equivalent to:

```text
BEGIN IMMEDIATE
validate existing lease and protected-resource state
determine expiry and eligibility
allocate next fencing token for protected_resource_key
persist ownership
persist issued_at and expires_at
persist fencing token
COMMIT
```

On failure it must roll back all of those effects. No durable state may show a
changed owner without its fencing generation, or an issued acquisition fencing
generation without durable ownership and expiry. Renewal must atomically verify
lease identity, current ownership, expected fencing token/generation, and
non-revoked state when applicable, then update expiration. Normal renewal of
the same still-valid lease must not issue a new fencing generation unless the
explicit future model defines renewal as reacquisition. Expiry followed by new
acquisition or reacquisition must issue a newer token.

Every mutation declared lease/fencing-protected must accept or derive the
expected `protected_resource_key` and fencing token, and must reject a stale
generation. In particular, after worker A receives token `N`, the lease
expires, and worker B receives token `N+1` for the same key, A's attempt to
make a protected mutation with `N` must be rejected.

### Binding idempotency identity and atomicity

An idempotent operation is identified by all four of:

```text
idempotency_key
operation_kind
canonical_operation_identity
payload_fingerprint
```

For JSON-compatible operation payloads, `payload_fingerprint` is SHA-256 over
canonical UTF-8 JSON bytes. Canonical JSON uses sorted object keys,
deterministic separators with no insignificant whitespace, and only values in
the supported canonical JSON domain. The idempotency layer must reject values
outside that domain; it must not use `repr()`, `str(object)`, arbitrary caller
insertion order, or a runtime-invented serialization. A non-JSON-compatible
payload must supply an explicitly defined canonical byte representation before
it enters the idempotency layer.

On reuse of an `idempotency_key`, the exact same `operation_kind`,
`canonical_operation_identity`, and `payload_fingerprint` is a semantic replay
with no duplicate effect. Any conflict in one of those fields is an
idempotency conflict and must fail closed. Implementations must not compare
arbitrary mutable Python objects directly. Where idempotency protects a state
transition, idempotency consumption and its protected semantic state change
must commit in the same atomic transaction; a crash must leave neither one
without the other.

## Transitions and atomic checkpoints

Transitions are append-only durable evidence. A stored transition must preserve
enough information to establish its `from` state, event, `to` state, actor,
timestamp, workflow/run/phase identity, applicable gate/evidence reference,
idempotency identity, and relevant state version. R3 persists a transition it
is instructed to persist; it does not evaluate legal-transition policy.

For a lease/fencing-protected transition, durable evidence must also preserve
the exact `protected_resource_key` and `fencing_token` that authorized it. For
a transition not protected by fencing, that context may be null or not
applicable; the store must not fabricate a token. This evidence is required so
later recovery and audit can establish the lease generation authorizing a
mutation.

For critical progress, R3 must support one atomic transaction equivalent to:

```text
validate expected version and fencing
+ persist transition
+ persist checkpoint
+ advance durable run state and version
+ record idempotency consumption
COMMIT
```

Otherwise all of those changes roll back. A crash must not leave a committed
run transition without its required checkpoint, nor a committed checkpoint
without its required transition.

A durable checkpoint must preserve the accepted R2 checkpoint information and,
where applicable, workflow/run identity, phase, operational state, state
version, base commit/reference, last safe transition, session reference,
resume data, artifact/evidence references, and creation timestamp. It must not
store private model reasoning or a chat transcript as canonical state.

## Operational evidence persistence and recovery

Agent sessions are operational references rather than repository or project
authority. R3 persists adapter identity, session/thread reference, role,
independence group when represented, attempt/run relationship, and applicable
status/lifecycle so the reference can be recovered after restart.

Usage persistence must preserve R2 telemetry truthfulness. Unavailable observed
classes remain `NULL`; zero means directly observed zero only. The store must
not fabricate classes, convert `NULL` to zero, or serialize a heuristic
decomposition as observed. If persisted, `OBSERVED`, `UNAVAILABLE`, `DERIVED`,
and `ESTIMATED` provenance remain distinguishable according to the accepted
R2 contract.

Failures are normalized domain records, not transport-specific literal error
strings. They must preserve category, recoverability, stage/context, timestamp,
evidence/artifact reference when applicable, attempt/run association, and
already-modeled retry timing. R3 does not classify quota or network failures.

Human-decision persistence must survive restart: it retains why human authority
was needed, the requested decision, whether it is unresolved, and any later
competent resolution. It must not invent business decisions.

R3 recovery must use real, disposable SQLite databases to demonstrate that:

- committed transactions survive reopen;
- rolled-back or incomplete transactions do not appear committed;
- the latest checkpoint reloads after reopen;
- active lease state is reconstructed;
- idempotency consumption survives reopen;
- stale optimistic versions and stale fencing tokens fail;
- a failed migration is not recorded as applied; and
- `integrity_check` succeeds on a valid store.

The real-SQLite lease/recovery suite must explicitly cover acquire, inspect,
renew, release, expiration, and reacquisition after expiration; all of
`LeaseResolution.NONE`, `LeaseResolution.ONE_RELEVANT`, and
`LeaseResolution.AMBIGUOUS`; fencing-token monotonicity; and stale-fencing
rejection. It must include the following no-lease reopen scenario:

```text
durable run exists
no relevant lease exists
close StateStore/process
reopen database
load recovery context by run_id
=> LeaseResolution.NONE / NO_RELEVANT_LEASE, without inventing a lease
```

That future test may provide evidence relevant to R2 carry-forward debt A only
after implementation and a fresh independent R3 audit; this handoff does not
close the debt. The suite must also prove that acquire then release, followed
by close, reopen, and recovery, does not reconstruct an active lease. Durable
history of a released lease may remain if the schema preserves it, but it must
not be interpreted as active ownership.

The future concrete store must also expose bounded, deterministic stdlib SQLite
capabilities for `integrity_check` and explicit-destination backup. It must
never automatically back up into the Git checkout.

### Binding WAL-safe backup authority

The future backup capability must use Python `sqlite3`'s supported online
backup API, `Connection.backup(...)`, or an exact stdlib SQLite online-backup
equivalent. Raw filesystem copying of the main `.db`/`.sqlite` file while a
source store may be active in WAL mode is forbidden as a live-store backup
mechanism; copying only the main file is not a consistent backup. Manual copies
of the main database, `-wal`, and `-shm` files are not authorized as the
primary implementation.

The destination must be explicit, outside Git by default, distinct from the
source database, and never silently selected inside the repository. Backup
must observe the transactionally consistent snapshot provided by the stdlib
online-backup API. Validation tests must enable WAL, commit data, produce a
backup with that API, and prove the destination passes `PRAGMA integrity_check`.

## Exact future R3 implementation allowlist

Only a future R3 implementation that has completed the required lifecycle may
create or modify these paths:

```text
tools/autopilot/config/runtime-contract.json
tools/autopilot/src/feelingpilates_autopilot/domain/models.py
tools/autopilot/src/feelingpilates_autopilot/ports/state_store.py
tools/autopilot/src/feelingpilates_autopilot/adapters/__init__.py
tools/autopilot/src/feelingpilates_autopilot/adapters/state/__init__.py
tools/autopilot/src/feelingpilates_autopilot/adapters/state/migrations.py
tools/autopilot/src/feelingpilates_autopilot/adapters/state/sqlite_store.py
tools/autopilot/migrations/001_initial.sql
tools/autopilot/tests/test_sqlite_migrations.py
tools/autopilot/tests/test_sqlite_store.py
tools/autopilot/tests/test_sqlite_transactions.py
tools/autopilot/tests/test_sqlite_leases.py
tools/autopilot/tests/test_sqlite_idempotency.py
tools/autopilot/tests/test_sqlite_recovery.py
```

No other implementation path is authorized. If repository inspection later
shows any listed path structurally incompatible with the R2 package layout,
the future executor must report it to the handoff audit/correction process; it
must not silently substitute another path.

## Required future test authority

R3 tests must be deterministic, stdlib-only, and use temporary directories and
real temporary SQLite databases. Required coverage includes migration
application and idempotency, checksum drift, foreign keys, WAL, configurable
busy timeout, synchronous configuration, atomic rollback,
transition/checkpoint atomicity, optimistic concurrency, reload, lease
acquisition/renewal/expiry, `LeaseResolution.NONE`, lease ambiguity, monotonic
fencing, stale-fencing rejection, duplicate and conflicting idempotency,
session recovery, usage `NULL` preservation, failure recovery, human-decision
persistence, crash/reopen, integrity check, and backup.

The binding test evidence for this authority is additionally explicit:

- On a freshly opened or reopened connection, tests must verify
  `foreign_keys == 1`, the effective `busy_timeout` equals the configured
  value, `synchronous` is the `FULL` equivalent, and `journal_mode == wal`.
  Testing only the initial migration connection is insufficient.
- Migration tests must prove that equal raw bytes produce the same checksum;
  a line-ending byte change and a whitespace byte change each change the
  checksum; a reused migration ID with changed checksum fails closed; and a
  failed migration is not registered as applied.
- Idempotency tests must prove that the same key, operation identity, and
  canonical payload yields one semantic effect; conflicts in operation kind,
  canonical operation identity, or payload fingerprint each fail closed;
  equivalent JSON objects with different source key order yield the same
  fingerprint; and a meaningful payload change yields a different fingerprint.

No mock-only test may claim crash or reopen correctness. Test runs must leave
no runtime-state or bytecode artifact inside the repository.

## Explicit boundaries

R3 does not authorize or implement:

- Codex SDK or CLI adapters, LLM invocation, or model routing;
- ContextCompiler or the workflow orchestration engine;
- Git repository adapters or worktree management;
- publishing, process supervision, launchd, or network/quota classifiers;
- F2E or any product execution; or
- runtime activation, productive authority, or cutover.

It must not install dependencies, invoke Codex programmatically, or create a
runtime database in the checkout. `auto_publish` remains `false`; F2E remains
unchanged.

## Historical activation lifecycle and next action

```text
R3 HANDOFF: MATERIALIZED / APPROVED / ACTIVE
R3 TARGET: AUTOPILOT R3 — SQLite durable state and recovery foundation
R3 TARGET AUTHORIZATION: AUTHORIZED_TO_START / NOT_STARTED
R3 IMPLEMENTATION: AUTHORIZED_TO_START
R3 INITIAL FRESH AUDIT: P0=0 / P1=9 / P2=0
R3 CORRECTION.1: MATERIALIZED
R3 FRESH RE-AUDIT.1: P0=0 / P1=1 / P2=0
R3 TECHNICAL CORRECTIONS P1-1..P1-9: CLOSED_BY_FRESH_REAUDIT
R3 P1-10 LIFECYCLE INCONSISTENCY: CLOSED_BY_FRESH_REAUDIT
R3 CORRECTION.2: MATERIALIZED / CONFIRMED_BY_FRESH_REAUDIT
R3 FRESH HANDOFF RE-AUDIT.2: P0=0 / P1=0 / P2=0
NEXT ALLOWED ACTION: EXECUTE_ACTIVE_AUTOPILOT_R3_DURABLE_STATE
R2: PUBLISHED / CLOSED / HISTORICAL
R2 P2-1: OPEN / NON_BLOCKING / CARRY_FORWARD
R4: NOT_AUTHORIZED
F2E: UNCHANGED
auto_publish: false
```

## Implementation acceptance addendum — authoritative current R3 lifecycle

The preceding activation material and its handoff-audit history remain
historical evidence. The exact authorized implementation was subsequently
materialized and passed the final fresh independent implementation re-audit at
`auditoria/reviews/AUTOPILOT-R3-DURABLE-STATE-IMPLEMENTATION-FINAL-REAUDIT.md`.
That review is the evidence for this competent acceptance transition; it does
not convert the implementation into lifecycle closure.

```text
R3 HANDOFF: APPROVED / ACTIVE
R3 TARGET: IMPLEMENTED
R3 IMPLEMENTATION: ACCEPTED
R3 IMPLEMENTATION AUDIT: PASS (P0=0 / P1=0 / P2=0)
R3 PUBLICATION: PENDING until this accepted commit is successfully pushed
R3 CLOSURE: NOT_YET_PERFORMED
NEXT ALLOWED ACTION AFTER SUCCESSFUL PUSH:
  FRESH_AUDIT_AUTOPILOT_R3_PUBLICATION_CLOSURE
R4: NOT_AUTHORIZED
F2E: UNCHANGED
auto_publish: false
```

### R2 P2-1 later closure record

R2 remains `PUBLISHED / CLOSED / HISTORICAL`; its historical implementation
and audit records are not rewritten. Based on later, concrete R3 evidence,
R2 Debt A (`LeaseResolution.NONE / NO_RELEVANT_LEASE` behavioral coverage) is
`CLOSED_BY_R3`. The evidence is a real file-backed SQLite StateStore test with
a durable run, no relevant lease, close/reopen, recovery by `run_id`, and
`LeaseResolution.NONE / NO_RELEVANT_LEASE`, independently accepted by the final
R3 implementation re-audit.

R2 Debt B (malformed embedded `usage_record` behavioral validation) and Debt C
(attached branch without upstream behavioral coverage) remain
`OPEN / NON_BLOCKING / CARRY_FORWARD / OUTSIDE_R3_SCOPE`.
