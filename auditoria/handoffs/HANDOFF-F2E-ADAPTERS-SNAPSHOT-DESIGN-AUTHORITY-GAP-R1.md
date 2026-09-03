# FeelingPilates — Handoff F2E adapters/snapshot design — authority gap R1

Handoff status: `MATERIALIZED / APPROVED / ACTIVE / AUTHORIZED_FOR_CORRECTIVE_DESIGN_RESEARCH / TARGET_AUTHORIZED_TO_START / TARGET_NOT_STARTED`

Target: `F2E / adapters-snapshot design — authority gap R1`

Type: `DESIGN / RESEARCH — CORRECTIVE AMENDMENT`

Role that materialized this handoff: `DOCUMENTER`

## 1. Purpose, authority, and lifecycle

Repository documents and physical Git evidence are authoritative; chat is coordination only. This
handoff materializes a future corrective design unit after the R1 handoff correction found an
authority conflict in the design previously closed at
`auditoria/fase-2e-diseno-adapters-read-only-snapshot-consistency.md`.

The historical design closure remains historical evidence only. It does not authorize an executor
to choose between incompatible or incomplete contracts while preparing R1. The fresh independent
handoff-document audit is persisted in
`auditoria/reviews/HANDOFF-F2E-ADAPTERS-SNAPSHOT-DESIGN-AUTHORITY-GAP-R1-REVIEW.md`. Its
contractual result is `P0=0 / P1=0 / P2=0`, all applicable handoff gates `PASS`, and
`READY_TO_APPROVE_AND_ACTIVATE=SI`. This documentary action consequently approves and activates
only the corrective design/research unit.

```text
Handoff: MATERIALIZED
Fresh independent handoff-document audit: PASS / PERSISTED
Approved: YES
Active: YES
Authorized for corrective design research: YES
Target authorized to start: YES
Target started: NO
Target materialized: NO
Corrective design amendment: NOT_PERFORMED
Fresh corrective-design audit: NOT_PERFORMED
Authority gap closed: NO
```

`ACTIVE` means only that the corrective `DESIGN / RESEARCH — CORRECTIVE AMENDMENT` may begin.
It does not mean that the authority gap is resolved, the design has been corrected, a design
amendment has passed, R1 has been corrected or activated, or Java, DB, or R1 implementation is
authorized.

The immediate next action is:

```text
EXECUTE_ACTIVE_CORRECTIVE_DESIGN_RESEARCH_HANDOFF
```

## 2. Materialization pre-flight and preserved baseline

```text
Repository: /Users/jesusaldaircruzortiz/Desktop/Feelingpilates/feelingpilates
Branch: operacion/excepciones-horario-fecha
HEAD: f6b5ed7c5729502e856f0d088cddc52de5662527
Upstream: f6b5ed7c5729502e856f0d088cddc52de5662527
Initial staging: VACÍO
Preexisting authorized dirty baseline:
?? auditoria/handoffs/HANDOFF-F2E-R1-RESERVA-READER-JPA-READ-ONLY.md
Initial SHA-256 of the preexisting R1 draft:
b3d4131c9ac0d7fc594dea7a7c002c68d90ca14e95750afdb2006bb4a12ee25a
```

The R1 draft is a known preexisting baseline, not an approved handoff. It must remain unmodified,
unstaged, and unapproved. A material mismatch in branch, HEAD, upstream, staging, baseline, or
allowed delta is fail-closed: stop without reset, stash, checkout, clean, reconciliation, or
unapproved mutation.

## 3. Confirmed authority gap

The R1 draft is `MATERIALIZED / NOT_APPROVED / NOT_ACTIVE / BLOCKED_BY_AUTHORITY_GAP`. Its
preparation surfaced four unanswered or internally inconsistent technical questions. This
corrective amendment must resolve them in the canonical design; this handoff must not resolve them.

1. The R1 read contract says `INPUT_INVALID`, while the closed operational catalog says
   `ADAPTER_INPUT_INVALID`. A single normative taxonomy is absent.
2. R1 uses `SOURCE_RECORD_NOT_FOUND`, but its membership in the operational catalog, exact
   semantics, payload, batch behavior, and relation to `SOURCE_ACCESS_FAILURE` are not closed.
3. The SQL catalog requires normalized-SQL hashes but does not close the exact, versioned
   canonicalization algorithm or hash composition.
4. The checksum discussion fixes broad types and order but does not close exact, versioned row,
   table, and slice hash formulas.

These gaps are technical. `Requires human decision: NO` remains preserved unless new evidence
proves otherwise. The corrective unit must not invent a conclusion merely to preserve the earlier
historical PASS.

## 4. Exact future scope

After this handoff is freshly audited, approved, and active, the future unit may analyze and
correct exclusively the following authority in the canonical design:

1. failure vocabulary and exception contract required by R1;
2. SQL statement canonicalization and catalog hashing;
3. checksum canonicalization and row/table/slice hash hierarchy;
4. minimum same-design-document references necessary to make those decisions coherent.

The future corrective design unit may modify exactly one existing file:

```text
auditoria/fase-2e-diseno-adapters-read-only-snapshot-consistency.md
```

That file is the only corrective-design artifact. The correction must be materialized directly as
an explicit amendment/version section inside that existing canonical design. During the target
design execution, creating a checkpoint, addendum, amendment file, or any parallel document is
forbidden:

```text
NO NEW DOCUMENT DURING TARGET DESIGN EXECUTION
```

No other existing file may be modified. In particular, the future unit must not modify
`auditoria/ESTADO-ACTUAL.md`, this corrective handoff, the R1 handoff, any other handoff,
`auditoria/reviews/F2E-ADAPTERS-SNAPSHOT-DESIGN-REVIEW.md`, any other review, or any other canon.
This physical allowlist is fail-closed. If the design corrector believes another file must change,
it must stop and report `SCOPE/AUTHORITY_GAP`; it must not expand scope. The amendment requires a
new fresh independent design audit.

## 5. Failure vocabulary and exception-contract questions to close

The future design must define `ReservationReadFailureCode`, or an equivalent explicitly named
conceptual contract, exhaustively enough for R1. At a minimum it must distinguish:

- invalid public or physical/projection input;
- requested record absent;
- physical/source/JPA access failure; and
- every other code already genuinely approved by the corrected catalog.

For every code, the amendment must specify trigger, detection stage, whether SQL was executed,
batch behavior, partial-result behavior, cause policy, retry semantics where applicable, and safe
context/PII/raw-SQL restrictions.

### 5.1 Invalid-input naming

The amendment must explicitly resolve `INPUT_INVALID` versus `ADAPTER_INPUT_INVALID`, establish
one final canonical value, and update every contradictory reference in the design. Aliases are
forbidden unless the amendment formally defines aliasing and its canonical value.

### 5.2 Requested record absent

The amendment must decide whether `SOURCE_RECORD_NOT_FOUND` is a read-layer code. If it is, it
must define the complete contract. If it is not, the amendment must identify the replacement code
for `readByReservationIds`. In either case it must preserve:

```text
requested ID absent → batch abort → no partial snapshots
```

### 5.3 Source access failures

The amendment must close the exact mapping of observable physical errors, including relevant
`PersistenceException`, `HibernateException`, JDBC/SQL exceptions, and any other physically
observable category. Host-preflight or Docker/environment failures must not be relabeled as
semantic read failures.

### 5.4 `ReservationReadException`

The amendment must close its exact shape: `failureCode`, message, cause, safe context, and the
permitted scope/record identifiers. It must state cause wrapping/preservation and PII restrictions.

## 6. SQL canonicalization and catalog identity questions to close

The future design must choose and justify an exact versioned SQL-normalization algorithm. It must
define behavior for leading/trailing and repeated whitespace, line breaks, SQL casing, quoted
identifiers and strings, comments, parameter/bind-marker representation, semicolons, canonical
encoding, and a version identifier.

It must also define the exact versioned identity formula for a catalogued statement, including the
inputs, framing, canonical representation, and hash composition. This handoff does not impose a
formula; a conceptual `version tag + normalized statement → SHA-256` is not a decision.

The amendment must close the exhaustive catalog of concrete R1 native `SELECT` statements and
permitted metadata/session statements. “Necessary metadata” is insufficient. Which concrete
statements belong to that catalog remains a future design decision.

The unknown-statement policy is not part of the authority gap and is not reopenable. It is an
unconditional constraint inherited from the current design:

```text
ANY STATEMENT NOT IN THE APPROVED CATALOG -> FAIL
UNKNOWN SQL -> FAIL, even when the statement begins with SELECT
```

The SQL authority gap is limited to exact canonical normalization, versioning, identity/hash, and
exhaustive enumeration of the concrete catalog. The corrective design unit is not authorized to
reconsider whether unknown SQL is tolerated.

## 7. Checksum canonicalization and hash hierarchy questions to close

The future design must define exact canonicalization for type tags, field order, row order,
length framing, NULL, UUID, text, date, time precision, timestamptz UTC conversion and precision,
and numeric/boolean encoding if they participate.

It must close the row-hash algorithm through an exact version identifier, canonical row
representation, deterministic row ordering where rows are composed, and exact hash formula.

For the table-hash algorithm it must close exactly:

- version identifier;
- canonical table identity;
- schema identity when applicable;
- table-identity framing;
- exact ordering of row hashes;
- framing/composition of row hashes;
- empty-table representation; and
- exact table-hash formula.

The decision must state unambiguously how `schema.table` identity participates in the table hash.
Table identity may not remain outside the logical composition that distinguishes different tables.
This handoff does not select a prefix, separator, length framing, hash nesting, or formula; the
future design must select them exactly.

For the slice-hash algorithm it must close exactly:

- version identifier;
- scope identity and scope framing;
- table identity included per entry;
- deterministic table ordering;
- composition of table identities and table hashes;
- empty-slice representation when applicable; and
- exact slice-hash formula.

The slice hash must not depend on `HashMap` iteration, repository iteration, filesystem ordering,
database incidental ordering, or any other non-normative order. The future design must select one
explicit deterministic table ordering.

The resulting hierarchy must define precise versioned formulas for:

```text
row canonical representation → row hash
table identity + ordered/framed row hashes → table hash
scope identity + deterministically ordered table identities/table hashes → slice hash
```

The design must prevent concatenation ambiguity. If it uses length prefixes, it must explicitly
state whether lengths count UTF-8 bytes, characters, or another exact unit. None of the final hash
formulas is predetermined by this handoff.

## 8. Required amendment evidence

The future design must document golden vectors concrete enough for later implementation tests to
verify `known input → known canonical representation → known expected SHA-256` for:

- identifier/provenance when applicable;
- SQL normalization and catalog identity;
- row hash;
- table hash including table identity;
- slice hash with at least two table identities when the conceptual slice permits it;
- invariance under non-normative input/iteration ordering;
- distinction between two tables with canonically equal rows; and
- distinction between scopes when scope participates in the slice identity.

The corrective design unit documents these vectors; it does not implement or run tests.

## 9. Future entry, exit, and audit conditions

Before execution, the future corrective unit must verify that this handoff was freshly audited,
approved, and explicitly active; branch and expected post-publication HEAD/upstream are correct;
the target delta is clean except for explicitly authorized baseline; the former design closure is
recognized as historical PASS; the downstream R1 authority conflict is recognized; and R1
implementation remains `NOT_STARTED`.

As mandatory entry evidence, before any authorized modification it must calculate SHA-256 for:

```text
auditoria/handoffs/HANDOFF-F2E-R1-RESERVA-READER-JPA-READ-ONLY.md
```

The only authorized baseline is:

```text
R1_BEFORE_SHA256=b3d4131c9ac0d7fc594dea7a7c002c68d90ca14e95750afdb2006bb4a12ee25a
```

If the value differs, the unit must stop without reconciling or modifying the R1 draft. The draft
is an authorized preexisting untracked baseline and must not be stashed, cleaned, deleted, moved,
renamed, staged, or modified. Its presence by itself does not block the corrective design.

It may be submitted only to a fresh independent design audit after all of the following are true:

1. invalid-input naming is resolved;
2. requested-record-absent semantics are resolved;
3. the failure-code contract is exhaustive;
4. the read exception contract is exhaustive;
5. source-access failure wrapping is resolved;
6. SQL normalization is exact and versioned;
7. SQL catalog identity is exact and versioned;
8. allowed SQL and metadata are exhaustively catalogued;
9. unknown SQL unconditionally fails, including unknown `SELECT`;
10. checksum field/type canonicalization is exact;
11. row hash is exact and versioned;
12. table identity and its framing are explicit;
13. table hash is exact and versioned;
14. row-hash ordering is deterministic;
15. slice scope identity and framing are explicit;
16. table ordering is deterministic;
17. slice hash is exact and versioned;
18. complete golden vectors are documented;
19. contradictory references in the design are corrected;
20. the canonical design is the only modified file;
21. the R1 before/after SHA-256 values both equal the authorized baseline;
22. no pure-core, code, test, or implementation change occurred; and
23. a fresh independent design audit remains required.

As mandatory exit evidence, after the corrective design is complete the unit must recalculate the
same R1 draft SHA-256 and prove:

```text
R1_BEFORE_SHA256
= R1_AFTER_SHA256
= b3d4131c9ac0d7fc594dea7a7c002c68d90ca14e95750afdb2006bb4a12ee25a
```

A mismatch is `FAIL / SCOPE VIOLATION`. The future corrective-design output must report:

```text
R1 baseline fingerprint: <R1_BEFORE_SHA256>
R1 final fingerprint: <R1_AFTER_SHA256>
R1 unchanged: PASS / FAIL
```

This report is required evidence for the subsequent fresh design audit. The exact future target
delta at submission is:

```text
M  auditoria/fase-2e-diseno-adapters-read-only-snapshot-consistency.md
?? auditoria/handoffs/HANDOFF-F2E-R1-RESERVA-READER-JPA-READ-ONLY.md
```

By then this corrective handoff is expected to be tracked/published and must remain unchanged.
No other path, staged or unstaged, is permitted.

The next competent gate after corrective-design execution is:

```text
FRESH_INDEPENDENT_DESIGN_DOCUMENT_AUDIT
```

It must audit the amendment itself and regressions. It is not an implementation audit.

## 10. Downstream R1 sequencing

The existing R1 draft is not to be modified by this materialization or by the future corrective
design unit. After a future amendment PASS and publication, the required sequence is:

```text
return to R1 handoff draft
→ handoff-document correction using the new authority
→ fresh independent R1 handoff re-audit
→ approval and activation
→ implementation
```

R1 is not automatically authorized by an amendment PASS. R1 P1-1 (exhaustive allowlist) and P1-3
(test-only JPA topology) remain pending for a later `HANDOFF_DOCUMENT_CORRECTION`; they do not
reopen design absent new evidence.

## 11. Explicit exclusions and preserved authority

The future corrective design unit must not modify `detector/**`. If closing any gap requires a
pure-core change, the result is `AUTHORITY_CONFLICT` and the unit must stop.

It also must not implement Java, create or run tests, use Docker, HostValidator, SQL, DB, Flyway,
schema changes, repositories, adapters, R1 implementation, R2-R6, data audit, crosswalk,
resolver, fence, migration, cutover, or an authority switch.

During target execution it also must not modify `auditoria/ESTADO-ACTUAL.md`; this corrective
handoff; the R1 handoff; any other handoff; any review; or any other canon, including
`ARQUITECTURA-ACTUAL.md`, `DECISIONES-ARQUITECTONICAS.md`, `DOMINIO-FUNCIONAL.md`,
`MAPA-LEGACY-Y-MIGRACION.md`, `README-REESTRUCTURACION.md`, and
`REGLAS-DE-TRABAJO-IA.md`. Review artifacts are historical evidence and are never edited
retroactively.

Lifecycle and closure updates are a separate later documentary action. Only after the corrective
design receives a fresh independent design-audit PASS may another authorized intervention persist
a new review, update handoff lifecycle, update `auditoria/ESTADO-ACTUAL.md`, or close/publish the
amendment. Target design execution and closure must not be combined.

The following authority remains unchanged:

```text
TurnoInstructor: PRODUCTIVE AUTHORITY
Pure detector: DARK_LAUNCH / NOT_PRODUCTIVE
Adapters: NOT_IMPLEMENTED
R1: NOT_AUTHORIZED
Data source: DATA_SOURCE_NOT_AVAILABLE
Data audit: NOT_PERFORMED / NOT_AUTHORIZED
R2-R6: NOT_AUTHORIZED
D08: DEFERRED
Crosswalk / resolver / fence / migration: NOT_AUTHORIZED
MIGRANDO: NO
NUEVA: NO
Cutover: false
```

This materialization does not update `auditoria/ESTADO-ACTUAL.md`, the canonical design, the R1
draft, review artifacts, code, tests, configuration, migrations, schema, or Git history.

## 12. Current disposition

```text
CORRECTIVE DESIGN HANDOFF MATERIALIZED
FRESH INDEPENDENT HANDOFF DOCUMENT AUDIT: PASS / PERSISTED
APPROVED
ACTIVE
AUTHORIZED_FOR_CORRECTIVE_DESIGN_RESEARCH
TARGET_AUTHORIZED_TO_START
TARGET_STARTED: NO
TARGET_MATERIALIZED: NO
CORRECTIVE_DESIGN_AMENDMENT: NOT_PERFORMED
FRESH_CORRECTIVE_DESIGN_AUDIT: NOT_PERFORMED
AUTHORITY_GAP: OPEN / TARGET_OF_ACTIVE_CORRECTION
```

No corrective design decision has been made by this handoff. No implementation, publication,
data access, migration, authority change, or cutover is authorized now.
