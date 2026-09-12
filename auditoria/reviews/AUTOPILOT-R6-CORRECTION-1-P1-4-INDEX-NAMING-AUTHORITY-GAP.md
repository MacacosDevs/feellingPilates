# FeelingPilates — AUTOPILOT R6 Correction.1 P1-4 index-naming authority gap

**Role:** `R6_AUTHORITY_GAP_MATERIALIZER /
P1_4_RELATIONAL_AUTHORITY_CLARIFIER /
IMPLEMENTATION_PRESERVATION_AUDITOR`

**Mode:** `AUTHORITY_MATERIALIZATION / DOCUMENTATION_ONLY /
NO_IMPLEMENTATION / NO_PUBLICATION / NO_SELF_AUDIT`

## Object and provenance

This artifact records the authority gap encountered while executing only P1-4
of the published R6 implementation Correction.1. The P1-4 implementation
stopped before modification because the repository freezes the structure of
three supporting indexes but not their exact identifiers, while the execution
instruction required exact names and prohibited inventing them.

```text
Repository: /Users/jesusaldaircruzortiz/Desktop/Feelingpilates/feelingpilates-autopilot-r1
Branch: orquestacion/autopilot-r1
HEAD: e77ad031e2071039fd960af08205afea0257c022
Resolved upstream: e77ad031e2071039fd960af08205afea0257c022
Live remote: e77ad031e2071039fd960af08205afea0257c022
Staging: EMPTY
Correction.1: APPROVED / ACTIVE / PUBLISHED
Implementation candidate: AUTHORIZED_DIRTY / CORRECTION_REQUIRED /
  NOT_ACCEPTED / NOT_PUBLISHED
Current candidate paths: 20
Candidate paths outside 22-path allowlist: 0
Implementation candidate fingerprint before:
7925a0d6e15bce4e2be87df5b6391c426f9f3ab88d3db077bfab774c0b25181a
Implementation candidate fingerprint after:
7925a0d6e15bce4e2be87df5b6391c426f9f3ab88d3db077bfab774c0b25181a
Candidate preserved: PASS
P1-4 implementation attempt changes: NONE
```

The current fingerprint differs legitimately from the pre-Correction.1
candidate because P0-1 through P1-3 have since been materialized. It is the
preservation baseline for this documentation-only authority action.

## Gap finding

The parent R6 handoff and published Correction.1 freeze exactly:

- three explicit supporting indexes;
- `UNIQUE` for each index; and
- each source table and ordered column tuple.

They do not contain a `CREATE UNIQUE INDEX <name>` statement, an index-name
table, or any other exact identifier authority. Repository search found no
frozen name for any of the three indexes.

The implementation instruction simultaneously required exact index-name
strings and prohibited the executor from inventing them. Both requirements
cannot be satisfied from the published repository authority.

```text
P1-4 implementation: STOPPED_BEFORE_MODIFICATION
AUTHORITY_GAP: INDEX_NAMES_UNDER_SPECIFIED
Exact supporting-index count authority: PRESENT
Uniqueness authority: PRESENT
Ordered-column authority: PRESENT
Exact index-name authority before clarification: ABSENT
Authority contradiction: CONFIRMED
Allowlist gap: NONE
R6 semantic target gap: NONE
Relational-column gap identified by this stop: NONE
```

Only index-name selection is under-specified. This gap record does not audit or
close the rest of P1-4.

## Minimal binding clarification

```text
R6_SUPPORTING_INDEX_NAME_AUTHORITY:
  IMPLEMENTATION_DEFINED / NON_SEMANTIC
```

Exact supporting index names are not part of:

- workflow semantics;
- durable identity;
- relational identity;
- migration identity as an independent semantic authority;
- application or StateStore API; or
- runtime contract.

The future authorized P1-4 implementation must choose valid SQLite index
identifiers that are unique in the schema. No runtime or application logic may
branch on, query by, or otherwise depend on those identifier strings.

Once the corrected Migration 002 is accepted and published, its actual chosen
name spellings become fixed indirectly as part of the immutable migration
bytes. Any later rename is therefore a migration-byte/checksum change governed
by the accepted R3/R6 migration authority; this does not turn an index name
into independent migration, relational, application, or runtime semantics.

## Exact structural index authority — unchanged

Exactly three explicit R6 supporting indexes are required.

### Index A

```text
Parent/source table: attempts
Explicit index: YES
UNIQUE: YES
Ordered columns, exactly:
  1. run_id
  2. phase_id
  3. attempt_id
  4. ordinal
Name: IMPLEMENTATION_DEFINED
```

### Index B

```text
Parent/source table: leases
Explicit index: YES
UNIQUE: YES
Ordered columns, exactly:
  1. lease_id
  2. run_id
  3. holder
  4. protected_resource_key
  5. fencing_token
Name: IMPLEMENTATION_DEFINED
```

### Index C

```text
Parent/source table: idempotency_records
Explicit index: YES
UNIQUE: YES
Ordered columns, exactly:
  1. idempotency_key
  2. operation_kind
  3. canonical_operation_identity
  4. payload_fingerprint
Name: IMPLEMENTATION_DEFINED
```

No fourth explicit R6 supporting index is authorized by this clarification.
No tuple may omit, reorder, or add a column, and none may be non-unique.

## Explicit indexes versus SQLite internal indexes

`exactly three supporting indexes` means exactly three explicit indexes
introduced by Migration 002 for the frozen parent candidate keys above.

SQLite-created internal indexes whose names match `sqlite_autoindex_*` and
arise automatically from table `PRIMARY KEY` or `UNIQUE` constraints are not
the three explicit R6 supporting indexes and are not counted against that
explicit count. The implementation may not create a fourth explicit supporting
index and disguise it as an internal index.

## Acceptance-test authority

P1-4 tests must introspect the resulting SQLite schema. They must not assert an
arbitrary exact name string. The test must classify explicit versus SQLite
internal indexes and prove, by table, uniqueness, and ordered columns:

```text
Explicit supporting index count: 3
attempts(run_id, phase_id, attempt_id, ordinal): EXACT / UNIQUE
leases(lease_id, run_id, holder, protected_resource_key, fencing_token):
  EXACT / UNIQUE
idempotency_records(idempotency_key, operation_kind,
  canonical_operation_identity, payload_fingerprint): EXACT / UNIQUE
```

The production acceptance matrix must fail for:

- a missing tuple;
- any reordered tuple;
- an omitted or extra column;
- a non-unique supporting index; or
- an additional explicit supporting index.

Implementation-defined names themselves are not acceptance semantics. Tests
may verify only that each chosen name is a valid SQLite identifier, unique in
the resulting schema, and stable in the materialized Migration 002 bytes.

## P1-4 authority preserved unchanged

This clarification changes no other P1-4 requirement. The following remain
binding exactly as published:

- nine exact R6 tables;
- all mandatory columns, primary keys, candidate UNIQUE keys, and composite
  foreign keys;
- R3 run, Attempt, transition/checkpoint, lease/fence, and idempotency
  relationships;
- decision/action relationship and action semantic uniqueness;
- ACTION -> RECEIPT and RECEIPT -> EFFECT scope;
- capture-fence and recording-fence evidence;
- lifecycle, status/terminal, and uncertainty CHECK constraints;
- schema introspection and direct invalid-SQL matrix; and
- migration reopen, checksum, and checksum-drift behavior.

`001_initial.sql` remains immutable. `002_workflow_engine.sql` remains the only
authorized R6 migration path.

## Allowlist and broader boundary

```text
Implementation allowlist: 22 / EXACT / UNCHANGED
Additional implementation path: NONE
Additional migration: NONE
P1-4 implementation path: tools/autopilot/migrations/002_workflow_engine.sql
R6 semantic target: UNCHANGED
P0-1/P0-2/P0-3 authority: UNCHANGED
P1-1/P1-2/P1-3 authority: UNCHANGED
P1-5/P1-6: OPEN / NOT_IMPLEMENTED_BY_THIS_ACTION
R2-R5 authority: UNCHANGED
Known R4 P2: OPEN / NON_BLOCKING / CARRY_FORWARD / UNCHANGED
Deferred capabilities: NOT_AUTHORIZED
R7: NOT_AUTHORIZED
F2E: UNCHANGED / NOT_EXECUTED
auto_publish: false
```

## Execution progress and gate

```text
P0-1: CORRECTION_MATERIALIZED
P0-2: CORRECTION_MATERIALIZED
P0-3: CORRECTION_MATERIALIZED
P1-1: CORRECTION_MATERIALIZED
P1-2: CORRECTION_MATERIALIZED
P1-3: CORRECTION_MATERIALIZED
P1-4: PAUSED_PENDING_INDEX_NAMING_AUTHORITY_CORRECTION_AUDIT
P1-5: OPEN
P1-6: OPEN
Index naming authority correction:
  MATERIALIZED / PENDING_FRESH_AUDIT / NOT_EXECUTABLE
READY_TO_RESUME_P1_4: NO
Correction.1 overall: ACTIVE
READY_TO_ACCEPT_R6_IMPLEMENTATION: NO
Forward Lane: WAITING_FOR_MAIN
FORWARD_LANE_RESYNC_REQUIRED: NO
NEXT ACTION:
  FRESH_INDEPENDENT_AUDIT_R6_P1_4_INDEX_NAMING_AUTHORITY_CORRECTION
```

No P1-4 implementation, staging, commit, push, P1-5/P1-6 implementation, or
R7 action is authorized by this materialization.
