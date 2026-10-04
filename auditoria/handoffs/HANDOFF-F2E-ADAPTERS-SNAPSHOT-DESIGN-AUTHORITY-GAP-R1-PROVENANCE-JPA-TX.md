# FeelingPilates — Handoff F2E adapters/snapshot design — residual authority gap R1 provenance + JPA transaction topology

Handoff status: `MATERIALIZED / APPROVED / ACTIVE / TARGET_NOT_STARTED / DESIGN_EXECUTION_AUTHORIZED_TO_START`

Target: `F2E / adapters-snapshot design — residual authority gap R1 provenance + JPA transaction topology`

Type: `DESIGN / RESEARCH — CORRECTIVE AMENDMENT`

Role that materialized this handoff:
`HANDOFF_PREPARER / DESIGN_AUTHORITY_GAP_SCOPER / CORRECTIVE_DESIGN_PLANNER`

Correction role:
`HANDOFF_DOCUMENT_CORRECTOR / DESIGN_SCOPE_CORRECTOR / REGRESSION_BOUNDARY_CORRECTOR`

## 1. Purpose and current authority

This handoff materializes a new, bounded corrective design/research unit. Its sole purpose is to
authorize, after its persisted fresh independent handoff audit and explicit activation, a design
executor to close two residual technical authority gaps discovered by the downstream fresh audit
of the corrected R1 handoff:

1. exact identity/provenance authority for R1; and
2. unequivocal JPA transaction-resource topology for the R1 integration/test slice.

Repository documents and physical Git evidence are authoritative. Chat is coordination only.
This handoff does not itself resolve either gap, amend the canonical design, correct R1, approve
R1, authorize implementation, or activate any runtime path. Its activation authorizes only the
future design target described below; it does not start that target.

The target canonical design file is exactly:

```text
auditoria/fase-2e-diseno-adapters-read-only-snapshot-consistency.md
```

The technical gaps are resolvable by the future design executor without a business or product
decision:

```text
Human/business decision required: NO
Technical design authority required: YES
```

No human is asked to choose a hash grammar, provenance schema, transaction-manager strategy,
EntityManager wiring strategy, or Spring proxy arrangement. Those choices belong to the future
design executor and then to a fresh independent design audit.

## 2. Materialization pre-flight and dirty baseline

Physical pre-flight for this handoff preparation:

```text
Repository: /Users/jesusaldaircruzortiz/Desktop/Feelingpilates/feelingpilates
Branch: operacion/excepciones-horario-fecha
HEAD: 89d0fd5746a4410d7bbcd3c410c000fadce318d1
Upstream: 89d0fd5746a4410d7bbcd3c410c000fadce318d1
Initial staging: EMPTY
Initial working tree:
?? auditoria/handoffs/HANDOFF-F2E-R1-RESERVA-READER-JPA-READ-ONLY.md
Unexpected initial paths: NONE
```

The R1 handoff is a preexisting authorized dirty baseline. It is untracked, unapproved and
inactive. It is evidence for this preparation, not output attributable to it.

Corrective-handoff activation baseline:

```text
CORRECTIVE_HANDOFF_BEFORE_SHA256=55860636479c3c327930e5ecdcfc7354e6ea1cd62ee056dfcd652c3b506ee249
Fresh independent handoff audit: P0=0 / P1=0 / P2=0
```

Mandatory immutable fingerprint:

```text
R1_BEFORE_SHA256=b65965288c0840934f4db301b7d81efb5ac818640958863902e62ea7f4897185
R1_AFTER_SHA256=b65965288c0840934f4db301b7d81efb5ac818640958863902e62ea7f4897185
```

During this handoff preparation and during the future corrective design target, R1 must not be
modified, renamed, deleted, moved, staged, cleaned, stashed, normalized, or regenerated. A
fingerprint mismatch is `FAIL / SCOPE VIOLATION / STOP` and may not be reconciled inside this
workflow.

The exact delta created by this preparation is only:

```text
?? auditoria/handoffs/HANDOFF-F2E-ADAPTERS-SNAPSHOT-DESIGN-AUTHORITY-GAP-R1-PROVENANCE-JPA-TX.md
```

The expected final working tree for this preparation is exactly:

```text
?? auditoria/handoffs/HANDOFF-F2E-R1-RESERVA-READER-JPA-READ-ONLY.md
?? auditoria/handoffs/HANDOFF-F2E-ADAPTERS-SNAPSHOT-DESIGN-AUTHORITY-GAP-R1-PROVENANCE-JPA-TX.md
```

Staging must remain empty and `HEAD`/upstream must remain unchanged.

## 3. Causal history preserved

The earlier adapters/snapshot design and its first corrective amendment are not reopened:

```text
Historical adapters/snapshot design: PASS / CLOSED / PRESERVED
First corrective authority-gap amendment: COMPLETED / CLOSED / PASS / HISTORICAL / PRESERVED
First corrective scope: VALID / NOT REOPENED
```

That amendment correctly closed its authorized scope:

- failure vocabulary;
- `ReservationReadException`;
- SQL policy and policy-failure boundary;
- SQL normalization and catalog;
- checksum canonicalization;
- row, table and slice hashes; and
- their approved golden vectors, including Vector H.

The current unit does not claim that closure was incorrect. A later downstream R1 re-audit found
new residual authority gaps in areas that the prior correction did not fully concretize. Minimal
cross-references to the existing SQL/checksum framing are allowed only if the new identity design
must decide whether to reuse it. Its algorithms, catalog, failure boundary and golden vectors may
not be redesigned.

## 4. Evidence and exact residual finding state

The source of this new gap is the fresh independent audit of the corrected R1 handoff, considered
together with the physical R1 draft and the canonical design. Its result is:

```text
P0: 0
P1 open: 2
P2: 0
New findings: 0

P1-1 exact/fail-closed allowlist: CLOSED
P1-2 context/provenance/failures: OPEN only for identity/provenance authority
P1-3 test-only JPA topology: OPEN
P1-4 SQL/checksum: CLOSED
```

Current downstream R1 state:

```text
R1 handoff: MATERIALIZED
R1 fresh audit: P0=0 / P1=2 / P2=0
P1-1: CLOSED
P1-2: OPEN / BLOCKED_BY_RESIDUAL_DESIGN_AUTHORITY_GAP
P1-3: OPEN / BLOCKED_BY_RESIDUAL_DESIGN_AUTHORITY_GAP
P1-4: CLOSED
R1: NOT_APPROVED / NOT_ACTIVE
R1 implementation: NOT_STARTED / NOT_AUTHORIZED
```

Physical evidence of gap A includes canonical-design section 13 and R1 sections 5–6: the R1
draft materializes exact `ID_HASH`/`SEQ` framing, domain tags, normalized-field domains, exact
source fields, canonical scope, projection version, provenance shape and identity vectors beyond
what the current canonical design closes with sufficient precision.

Physical evidence of gap B includes canonical-design sections 18.1–18.2 and R1 section 10: the
reader remains conceptually `MANDATORY + readOnly`, while the test topology has multiple
transactional resources/managers and no `@Primary`; the canonical design does not yet select the
reader transaction manager and same-resource path mechanically enough to prevent ambiguity.

This handoff does not close P1-2 or P1-3. It creates the bounded design authority needed for a
future correction to do so.

## 5. Target execution allowlist

This handoff has received a fresh independent handoff-document audit, is approved, and is
explicitly active. The future `DESIGN_EXECUTOR / RESEARCHER` may therefore modify exactly one file:

```text
auditoria/fase-2e-diseno-adapters-read-only-snapshot-consistency.md
```

Target execution allowlist:

```text
EXACTLY_ONE_EXISTING_FILE
NO NEW TARGET-DESIGN DOCUMENT
NO OTHER MODIFIED, CREATED, RENAMED OR DELETED PATH
```

During target design execution the expected working-tree shape is exactly:

```text
 M auditoria/fase-2e-diseno-adapters-read-only-snapshot-consistency.md
?? auditoria/handoffs/HANDOFF-F2E-R1-RESERVA-READER-JPA-READ-ONLY.md
```

By that point this corrective handoff is expected to be tracked/published and must remain
unchanged. The R1 fingerprint before and after target execution must equal the immutable baseline
from section 2.

Forbidden during target design execution:

- modifying this handoff, R1, `auditoria/ESTADO-ACTUAL.md`, any review, any other handoff, or any
  other canon;
- Java, resources, configuration, Maven, tests, migrations, schema, SQL or DB work;
- Docker, Testcontainers or HostValidator;
- Git staging, commit, push, stash, clean or history mutation;
- R1 implementation or handoff correction;
- R2–R6 design or implementation; and
- product activation, data audit, migration, authority change or cutover.

## 6. Corrective design scope A — identity and provenance

The future design amendment must provide enough normative authority to implement every identity
without invention. It must choose exactly one grammar and close all inputs, exclusions and
cross-consistency rules. This handoff deliberately does not choose that grammar.

### 6.1 Normative identity grammar

The design must decide and document one exact, versioned grammar for the identities in this
scope. At minimum it must close:

- whether the existing checksum `LP`/`SEQ` grammar is reused, and the exact reuse contract, or
  whether a separate identity grammar is required;
- exact domain/version tags;
- component ordering and nesting;
- UTF-8 byte representation;
- byte-length versus character-length semantics;
- framing of scalar, sequence, set, map and pre-framed byte values;
- canonical scalar formats needed by R1;
- exact algorithm/digest and exact canonical preimage;
- exact output encoding/representation, including case and length when applicable;
- null, empty and optional-value semantics;
- collection semantics, including deterministic ordering and duplicate handling; and
- fail-closed behavior for values that cannot be represented canonically.

The amendment may not leave `ID_HASH`, `SEQ`, domain tags, normalized-field entry framing or
alternative framings for the R1 implementation executor to select. The identity algorithm,
digest, output encoding and representation remain future design decisions required of the target;
this handoff does not select SHA-256 or any other algorithm for these four identities. The target
must select and document exactly one complete solution, after which none of algorithm/digest,
encoding/representation, case when applicable, domain/version, canonical preimage, framing,
ordering, null/empty or collection semantics may remain open.

### 6.2 `executionProvenanceId`

The design must close:

- its semantic purpose as execution provenance, not business identity;
- every technical input and its exact ordering/representation;
- which values change between a run and a complete retry/attempt;
- every value that is explicitly excluded;
- its domain/version and exact formula;
- consistency with context, scope and catalog versions; and
- at least one complete reproducible golden vector.

It must prevent run/attempt metadata from being misrepresented as source, reservation, snapshot,
or business identity.

### 6.3 `logicalSnapshotId`

The design must close:

- the exact equivalence relation that makes two snapshots logically equal;
- every participating input and every excluded execution-only input;
- exact semantics of `SINGLE_READER_TEST` and any future claim referenced only for compatibility;
- scope and source semantics that participate;
- domain/version and exact formula;
- its distinction from `executionProvenanceId`; and
- at least one complete reproducible golden vector.

If an execution identity participates, the design must explicitly justify why that does not make
logical equality attempt-dependent; if it does not participate, the alternative linkage and
consistency invariant must be exact. The implementation executor may not decide this trade-off.

### 6.4 `sourceFingerprint`

The design must close exhaustively:

- its semantic purpose and exact projection/source contract;
- the complete field set and exact field order;
- physical versus business representation of every field;
- exact scalar canonicalization for IDs, enums/text, dates, times and timestamps;
- whether each technical timestamp participates and why;
- explicit PII exclusions, including the treatment of `cliente_id`;
- representation of `historicalProgrammingTarget=Optional.empty()`;
- projection contract identity and version participation;
- source system/type/identity participation;
- domain/version and exact formula; and
- at least one complete reproducible golden vector.

Field selection may not be delegated to code, reflection, map iteration, tests, or the R1
implementation executor.

### 6.5 `snapshotIdentity`

The design must close:

- its exact semantic purpose;
- its relation to `logicalSnapshotId`, `sourceFingerprint`, execution provenance,
  `ReservationSourceSnapshot`, source atom type and source identity;
- exact inputs, exclusions, ordering, domain/version and formula;
- invariants preventing identity from contradicting the snapshot payload/provenance; and
- at least one complete reproducible golden vector.

The correction must not reinterpret or redesign the identity semantics of the already-approved
pure detector.

### 6.6 Canonical scope

The current possibility of caller-supplied `scopeCanonical` drifting from the actual typed scope
must be eliminated. The design must choose one exact fail-closed contract, for example internal
derivation from typed scope, exact verification of supplied canonical bytes against that
derivation, or another technically equivalent mechanism.

Whichever solution is chosen, it must close:

- typed scope variants and exact canonical derivation;
- ordering, framing and encoding;
- owner of derivation/validation;
- whether canonical scope is stored or computed;
- error behavior before SQL;
- participation in each relevant identity and provenance field; and
- tests/audit evidence sufficient to prove `typed scope != canonical scope` is impossible or
  rejected before use.

The R1 executor may not select among these strategies.

### 6.7 Projection catalog version

`projectionCatalogVersion` may not remain arbitrary non-empty text. The design must close:

- its exact Java/conceptual type;
- the single source of truth;
- the exact allowed value or closed version set for R1;
- construction and validation rules;
- whether and how it participates in execution identity, logical identity, source fingerprint and
  provenance;
- equality with the version used by the SQL projection/mapping contract; and
- incompatibility/drift behavior, which must fail closed.

### 6.8 Exact provenance shape

The design must close the exact immutable evidence produced by R1 and its mapping into the
existing `EvidenceProvenance` contract. It must specify, with types, ordering and null/empty rules:

- physical `recordIds` and their relation to rows actually read;
- observable and normalized fields, if applicable;
- source/system/type/identity;
- allowed technical timestamps and their names;
- projection/catalog identity and version;
- execution identity;
- logical snapshot identity;
- typed/canonical scope identity;
- snapshot evidence/claim when applicable;
- rule/business context; and
- any other normative field required for a self-consistent R1 snapshot.

The shape must explicitly exclude accidental PII, raw SQL, bind values, credentials, connection
data, arbitrary caller fields and inferred historical targets.

### 6.9 Identity golden vectors

The amendment must materialize independently reproducible golden vectors for exactly these four
outputs:

```text
executionProvenanceId
logicalSnapshotId
sourceFingerprint
snapshotIdentity
```

Every vector must include all literal inputs, canonical scalar values, ordering, every framed
component, the complete byte-exact preimage, the exact algorithm selected by the target, the exact
selected output encoding/representation and the expected output/digest according to that selected
contract. No input may be implicit or inherited from another vector without restating it. The
vectors must be sufficient for a later test to recompute the value independently using the
algorithm and representation selected by the design target; tests may not become the source of
authority. Requiring a closed decision and reproducible vector does not authorize this handoff to
preselect SHA-256 or another solution.

### 6.10 Cross-consistency invariants

The design must make at least these relations mechanically verifiable and fail closed:

```text
typed scope = canonical scope representation
projection catalog version = version used by projection/fingerprint/provenance
record IDs = records physically read and published
source fields = fields represented by sourceFingerprint
logical identity = the same canonical scope/source/snapshot semantics carried by the output
snapshot identity = the identity of that exact ReservationSourceSnapshot payload
execution provenance = the exact run/attempt context carried by provenance
```

No component may be independently caller-selectable when that would permit an internally
contradictory snapshot.

## 7. Corrective design scope B — JPA transaction resource

The future design amendment must select one exact Spring/JPA topology that proves every measured
R1 read and transaction probe uses the same SELECT-only reader resource. This handoff deliberately
does not select the solution.

### 7.1 Reader transaction-manager selection

The design must choose and document one technically valid, unambiguous mechanism so every proxied
`ReservaJpaReader` method with `MANDATORY + readOnly` resolves to
`f2eReaderTransactionManager`, or an exact final normative equivalent. Candidate mechanisms may
include an explicit `transactionManager` annotation attribute, a qualifier/type-level contract,
test-only `TransactionManagementConfigurer`, or another Spring-correct arrangement; this list is
not a decision.

The selected contract must prove:

- no default transaction-manager resolution;
- no ambiguity with multiple `PlatformTransactionManager` beans and no `@Primary`;
- no privileged transaction manager can satisfy the reader annotation;
- the harness and reader join the same transaction manager/resource; and
- a missing, renamed or incompatible manager fails deterministically.

### 7.2 Production-versus-test coupling

The design must decide explicitly whether a main class such as `ReservaJpaReader` may nominally
reference a transaction-manager bean that exists only in test wiring at R1.

If the answer is no, the design must specify the exact alternative architecture and how both test
R1 and future R6 composition preserve `MANDATORY` and the same-resource invariant. If the answer
is yes, it must justify and close compile-time safety, runtime isolation, future R6 composition,
bean-name stability and the absence of productive bean activation. The implementation executor
may not infer this boundary.

### 7.3 Reader `EntityManager`

The design must close exactly how `ReservaProjectionQueryExecutor` obtains the transaction-aware
`EntityManager` belonging to the reader `EntityManagerFactory`, including exact bean/resource
name, qualifier, creation mechanism and transaction binding.

The executor must not be able to receive a privileged, default or differently backed
`EntityManager`. Its reachable datasource must be the R1 SELECT-only reader datasource.

### 7.4 Harness probe `EntityManager`

The design must close the exact bean/resource and qualifier used to execute:

```text
SELECT current_setting('transaction_isolation')
SELECT current_setting('transaction_read_only')
```

The probes must participate in the same transaction opened by the reader transaction manager and
must use the same reader `EntityManager`/`SessionFactory` plane as the R1 data queries. Privileged
JDBC, a default EntityManager or another SessionFactory is forbidden.

### 7.5 Probe path through `StatementInspector`

The design must decide and confirm mechanically that both `current_setting` probes traverse the
same reader `SessionFactory` and its `F2eStatementPolicyInspector`. The already-approved R1 catalog
IDs remain meaningful only if the probes take that inspected path.

The amendment must define fail-closed evidence that detects a probe executed through privileged
JDBC, a default EntityManager, another SessionFactory, or a route invisible to the inspector.

### 7.6 Same-resource graph

The final design must freeze a single, mechanically testable graph equivalent to:

```text
ReaderTransactionTestHarness
  -> reader JpaTransactionManager
  -> reader EntityManagerFactory
  -> SELECT-only reader DataSource

ReservaJpaReader MANDATORY
  -> the SAME reader JpaTransactionManager transaction

ReservaProjectionQueryExecutor
  -> transaction-aware EntityManager from the SAME reader EntityManagerFactory

current_setting probes
  -> the SAME reader EntityManager / SessionFactory / transaction

F2eStatementPolicyInspector
  -> attached to that SAME reader SessionFactory
```

No edge may resolve by type/default fallback to the privileged plane.

### 7.7 Bean names and qualifiers

The amendment must freeze every architecturally relevant final name/qualifier and its owner. At a
minimum it must cover:

```text
reader DataSource
reader EntityManagerFactory
transaction-aware reader EntityManager
reader JpaTransactionManager
F2eStatementPolicyInspector
ReaderTransactionTestHarness
ReservaProjectionQueryExecutor
ReservaJpaReader
```

“Corresponding resource”, “the reader manager”, implicit type selection, `@Primary`, or a default
bean name is insufficient authority.

### 7.8 Real Spring proxy path

The design must specify:

- the exact harness bean and method carrying the transaction advisor;
- the exact reader bean/method carrying `MANDATORY`;
- which `TransactionInterceptor`/advisor resolves which manager;
- the call path from test through harness proxy, callback and reader proxy;
- how self-invocation is excluded;
- how manual `new` is excluded from transactional assertions; and
- the negative invocation proving the reader proxy fails outside a transaction.

The evidence must prove the real proxy path, not merely the presence of an annotation.

### 7.9 Privileged/reader separation and no-write resource guarantee

The reader graph must be structurally capable of reaching only the SELECT-only datasource.
Privileged resources remain limited to bootstrap, Flyway, fixtures, grants/role lifecycle,
negative controls, checksum observation and cleanup, all outside the inspected reader plane where
the existing contract requires it.

The design must close bean construction and injection so privileged datasource, EntityManager,
EntityManagerFactory and transaction manager cannot reach the reader, executor, harness or probes.
An accidental default/privileged resource is a `NO_WRITE_CONTRACT` failure, even if a test happens
not to issue DML.

## 8. Required future corrective-design outcomes

The target may be submitted to its fresh independent design audit only when the canonical design
records all these outcomes as closed by explicit normative decisions:

```text
IDENTITY_GRAMMAR: CLOSED
IDENTITY_ALGORITHM_DIGEST_OUTPUT_REPRESENTATION: CLOSED
EXECUTION_PROVENANCE_ID: CLOSED
LOGICAL_SNAPSHOT_ID: CLOSED
SOURCE_FINGERPRINT: CLOSED
SNAPSHOT_IDENTITY: CLOSED
SCOPE_CANONICAL: CLOSED
PROJECTION_CATALOG_VERSION: CLOSED
PROVENANCE_SHAPE: CLOSED
IDENTITY_GOLDEN_VECTORS: MATERIALIZED / REPRODUCIBLE

READER_TM_SELECTION: CLOSED
READER_EM_SELECTION: CLOSED
HARNESS_PROBE_EM: CLOSED
PROBE_STATEMENT_INSPECTION: CLOSED
SAME_RESOURCE_GRAPH: CLOSED
BEAN_NAMES_QUALIFIERS: CLOSED
SPRING_PROXY_PATH: CLOSED
PRIVILEGED_READER_SEPARATION: CLOSED
NO_WRITE_RESOURCE_GUARANTEE: CLOSED
```

“Implementation will decide”, competing alternatives without selection, illustrative pseudocode,
arbitrary non-empty strings, implied qualifiers, framework-default selection, and future tests as
authority do not satisfy these outcomes.

## 9. Mandatory fresh design-audit requirements

After target execution, a new fresh independent `DESIGN_AUDITOR / AUTHORITY_AUDITOR /
GOLDEN_VECTOR_AUDITOR / JPA_TRANSACTION_TOPOLOGY_AUDITOR` must adversarially verify at least:

A. every identity is reproducible byte-for-byte from only documented inputs;
B. every documented identity output/digest is independently recomputed using the exact algorithm
   and output representation selected by the design target and matches;
C. typed/canonical scope inconsistency is impossible or rejected fail-closed;
D. projection catalog version cannot drift across context, fingerprint and provenance;
E. no PII, raw SQL, binds, credentials or inferred historical target enters identity/provenance;
F. no default transaction-manager ambiguity exists with multiple managers;
G. `ReservaJpaReader` `MANDATORY` definitely resolves to the reader transaction manager;
H. harness probes definitely use the reader EntityManager and same reader transaction;
I. probe SQL definitely traverses the reader `StatementInspector`;
J. the privileged plane is unreachable from the reader graph;
K. the reader graph can reach only the SELECT-only datasource;
L. the prior failure/SQL/checksum contracts and their golden vectors remain unchanged;
M. the pure detector remains unchanged and its identity is not reinterpreted; and
N. no R6/product activation, data audit, migration, authority change or cutover is introduced.

The audit must additionally report these fail-closed regression checks explicitly:

```text
QUERY_READ_SEMANTICS_UNCHANGED: PASS required
HISTORICAL_TARGET_ALWAYS_EMPTY: PASS required
TRANSACTION_PROPAGATION_UNCHANGED: PASS required
TRANSACTION_ISOLATION_UNCHANGED: PASS required
TRANSACTION_READ_ONLY_UNCHANGED: PASS required
R6_NOT_AUTHORIZED: PASS required
```

The design executor/corrector cannot perform this audit or declare its own design gate `PASS`.

## 10. Regression constraints and exact exclusions

The following areas are outside corrective scope and may not be reopened or redesigned:

```text
failure vocabulary
ReservationReadException
F2eSqlPolicyViolationException ownership and propagation
SQL canonicalization
SQL catalog and SQL golden vectors
checksum canonicalization
row/table/slice hash hierarchy
checksum golden vectors, including Vector H
projection-first reader strategy
unknown SQL / unknown SELECT -> FAIL
pure detector semantics and implementation
```

The design may add only the minimum references necessary to state how a newly selected identity
grammar relates to an already-approved framing contract. Such a reference may not change that
contract.

The exhaustive regression boundary also freezes all approved R1 query/read semantics:

```text
readByReservationIds: FROZEN
readByScope: FROZEN
native EntityManager/createNativeQuery strategy: FROZEN
Hibernate NativeQuery named typed binding: FROZEN
UUID list binding with IN (:parameterName) + setParameterList(..., UUID.class): FROZEN
input/null/empty validation before SQL: FROZEN
deterministic ordering, result shape and cardinality: FROZEN
requested-ID missing -> SOURCE_RECORD_NOT_FOUND / whole batch abort / no partials: FROZEN
readByScope zero rows -> valid immutable empty result: FROZEN
bounded salon/date scope semantics: FROZEN
additional query/read operation: FORBIDDEN
```

The future corrective target may use these query/read contracts only as fixed inputs or context
for identity/provenance decisions. It may not change query shapes, binding strategy, result or
cardinality semantics, ordering, missing-record behavior, scope semantics, source data, or add a
read endpoint/operation.

The R1 historical programming target is likewise frozen:

```text
R1 historical programming target: ALWAYS_EMPTY
implementation representation: HistoricalProgrammingTargetSnapshot absent / Optional.empty()
0..1 representation: FROZEN
historical target inference: FORBIDDEN
resolver/crosswalk introduction: FORBIDDEN
```

The future corrective target may not infer a historical target from containment, equality, a
current legacy turn, uniqueness, a candidate or any other source, and may not change the approved
`Optional.empty()` representation.

All approved transaction propagation, isolation, read-only and ownership semantics remain frozen;
only the unresolved transaction-manager/resource selection and binding topology is in scope:

```text
ReservaJpaReader propagation: MANDATORY
ReservaJpaReader readOnly: true
ReservaJpaReader transaction ownership: NONE / participates in supplied transaction
R1 standalone/test reader slice isolation: READ_COMMITTED
R1 standalone REPEATABLE_READ: FORBIDDEN

ReaderTransactionTestHarness propagation: REQUIRES_NEW
ReaderTransactionTestHarness isolation for R1: READ_COMMITTED
ReaderTransactionTestHarness readOnly: true

IN_SCOPE transaction freedom:
  transaction-manager selection
  transaction-resource binding
  EntityManager / EntityManagerFactory selection and binding
  harness probe resource and StatementInspector path
  exact bean names and qualifiers
  Spring proxy path
  same-resource proof
  privileged/reader resource separation
  SELECT-only/no-write resource guarantee

OUT_OF_SCOPE transaction freedom:
  propagation semantics
  isolation semantics
  readOnly semantics
  R1 transaction ownership semantics
  R6 multi-reader snapshot ownership or implementation
```

The target must decide how the reader transaction manager is selected unequivocally without
creating invalid main-versus-test architectural coupling, but it may not use that open topology
decision to alter `MANDATORY`, `REQUIRES_NEW`, `READ_COMMITTED`, `readOnly=true` or ownership.
R6 and its `REPEATABLE_READ` multi-reader composition remain outside scope and unauthorized.

Also outside scope:

- code, tests, Maven, dependencies, application configuration and migrations;
- DB access, SQL execution, Docker, Testcontainers and HostValidator;
- R1 handoff correction or implementation;
- R2, R3, R4, R5 or R6;
- coordinator implementation/composition;
- data audit, data-source acquisition or material report;
- D08, crosswalk, selection, resolver or fence;
- migration, normalization, backfill or reader switch; and
- productive authority, activation or cutover.

## 11. Preserved architecture and product authority

The corrective lifecycle must preserve:

```text
TurnoInstructor: PRODUCTIVE AUTHORITY
Pure detector: DARK_LAUNCH / NOT_PRODUCTIVE
Adapters: NOT_IMPLEMENTED
R1: MATERIALIZED / NOT_APPROVED / NOT_ACTIVE / IMPLEMENTATION_NOT_AUTHORIZED
R2-R6: NOT_AUTHORIZED
Data source: DATA_SOURCE_NOT_AVAILABLE
Data audit: NOT_PERFORMED / NOT_AUTHORIZED
D08: DEFERRED
Crosswalk: NOT_AUTHORIZED
Resolver: NOT_AUTHORIZED
Fence: NOT_AUTHORIZED
Migration: NOT_AUTHORIZED
MIGRANDO: NO
NUEVA: NO
Cutover: false
```

Closing the design gaps in a future target does not itself approve or activate R1. The sequence
after a future design-audit PASS remains separately authorized work:

```text
correct R1 handoff from restored authority
-> fresh independent R1 handoff re-audit
-> explicit R1 approval and activation
-> implementation
```

## 12. Entry conditions for future target execution

The corrective design target must not start unless all are true:

1. this handoff has a persisted fresh independent handoff-document audit;
2. that audit reports `P0=0 / P1=0` and all applicable handoff gates `PASS`;
3. a competent lifecycle action explicitly marks this handoff `APPROVED / ACTIVE` and authorizes
   the target;
4. branch, expected HEAD/upstream, staging and working tree match the then-published baseline;
5. the previous corrective amendment is recognized as `CLOSED / HISTORICAL / PRESERVED`;
6. P1-1 and P1-4 remain closed and are not reopened;
7. P1-2/P1-3 are recognized as blocked by these residual design authority gaps;
8. R1 remains unmodified and its SHA-256 equals the baseline in section 2;
9. target execution allowlist remains exactly one canonical design file; and
10. R1 implementation, R2–R6, DB/data audit and product activation remain unauthorized.

Failure of any entry condition is `STOP`, not permission to repair history or broaden scope.

## 13. Exit conditions for future target execution

The future design executor may report only
`CORRECTIVE DESIGN AMENDMENT MATERIALIZED — READY FOR FRESH INDEPENDENT DESIGN AUDIT` after all
of the following are true:

1. every identity/provenance outcome in section 8 is normatively closed;
2. every JPA resource/topology outcome in section 8 is normatively closed;
3. all four identity golden vectors are complete and reproducible;
4. cross-consistency invariants are explicit and fail closed;
5. the same-resource graph and exact bean/qualifier topology are explicit;
6. previous failure/SQL/checksum authority is preserved;
7. pure core, product authority, dark launch and deferred work are preserved;
8. the canonical design is the only modified file;
9. R1 before/after fingerprints both equal the authorized SHA-256;
10. staging is empty and `HEAD` is unchanged;
11. no code, test, Maven, DB, Docker, HostValidator or implementation work occurred;
12. whitespace and `git diff --check` are clean; and
13. a fresh independent design audit remains pending.

The future design output must report:

```text
R1 baseline fingerprint: <R1_BEFORE_SHA256>
R1 final fingerprint: <R1_AFTER_SHA256>
R1 unchanged: PASS / FAIL
Touched paths: <exact list>
Unexpected paths: <exact list>
Open technical questions in authorized corrective scope: NINGUNA / <exact blocker>
Human/business decision required: NO / <exact contradiction>
Next gate: FRESH_INDEPENDENT_DESIGN_DOCUMENT_AUDIT
```

It may not declare the target `CLOSED`, the design gate `PASS`, R1 approved/active, or either R1
P1 closed.

## 14. Persisted fresh independent handoff audit and gates

The fresh independent audit was performed in mode
`READ_ONLY / FRESH / INDEPENDENT / ADVERSARIAL`. It persisted a final result of `P0=0 / P1=0 /
P2=0`. The original audit findings are closed only at the handoff-document level:

```text
P1-A identity algorithm authority: CLOSED
P1-B regression scope: CLOSED
New findings: 0
```

| Gate | Required proof | Current state |
| --- | --- | --- |
| `HANDOFF_CONTRACT` | self-contained target, role, lifecycle, allowlist, gates and stops | `PASS` |
| `AUTHORITY_GAP_CONTRACT` | exactly the two residual technical gaps; no prior scope reopened | `PASS` |
| `IDENTITY_PROVENANCE_SCOPE` | four identities, scope, catalog version and exact provenance covered | `PASS` |
| `IDENTITY_GOLDEN_VECTOR_SCOPE` | four byte-exact independently reproducible vectors required | `PASS` |
| `JPA_TRANSACTION_RESOURCE_SCOPE` | TM/EM/probes/inspector/proxies/names and same-resource graph covered | `PASS` |
| `NO_WRITE_RESOURCE_SCOPE` | reader-only reachability and privileged-plane exclusion required | `PASS` |
| `DOWNSTREAM_R1_ISOLATION` | R1 frozen and no implementation/correction authorized | `PASS` |
| `DIRTY_BASELINE_CONTRACT` | R1 SHA and exact untracked preservation are fail-closed | `PASS` |
| `REGRESSION_SCOPE` | P1-1/P1-4 and prior corrective authority remain closed/preserved | `PASS` |
| `PRODUCT_AUTHORITY_SCOPE` | productive authority and dark-launch boundaries are preserved | `PASS` |
| `DATA_MIGRATION_SCOPE` | no data, migration, resolver, fence or cutover authority added | `PASS` |
| `CANONICAL_CONSISTENCY` | target and decisions reconcile all relevant canonical references | `PASS` |
| `READY_TO_APPROVE_AND_ACTIVATE` | all preceding applicable gates pass in a fresh independent audit | `SI / PASS` |

## 15. Stop conditions

Stop immediately without mutation, reconciliation or scope expansion if any of these occurs:

- branch, expected HEAD, upstream or staging differs from the authorized baseline;
- R1 is missing, changed, staged, renamed or its SHA-256 differs;
- any unexpected path is dirty or the target would require a second file;
- the solution requires changing R1, code, tests, configuration, pure core or another canon;
- P1-1, P1-4 or the prior corrective amendment would need to be reopened;
- exact identity semantics cannot be closed without a business/product decision;
- the JPA topology cannot be closed without production activation or R6 authorization;
- privileged and reader resources cannot be proven structurally separate;
- data source, DB, Maven, Docker, SQL or HostValidator becomes necessary;
- an authority conflict exists between competent canonical sources; or
- any action would imply migration, cutover or a change to `TurnoInstructor` authority.

A stop reports the exact blocker. It does not invent an architectural answer or request the human
to select between technical options that remain within design authority.

## 16. Publication and lifecycle exclusions

This activation persists the handoff audit and changes only this handoff's lifecycle authority. It does not:

- audit itself;
- update `auditoria/ESTADO-ACTUAL.md`;
- start or complete the target design amendment;
- create a review artifact;
- correct or stage R1;
- run tests or implementation validation;
- perform `git add`, commit or push; or
- publish or close any lifecycle.

Lifecycle after approval and activation:

```text
New handoff: MATERIALIZED
Approved: YES
Active: YES — the only active handoff
Target started: NO
Target: AUTHORIZED_TO_START / NOT_STARTED
Design execution authorized: YES — DESIGN_EXECUTION_AUTHORIZED_TO_START
Fresh activation audit required before publication: YES
Human/business decision required: NO
Technical design authority required: YES / NOW AUTHORIZED VIA ACTIVE HANDOFF
```

Maximum disposition of this activation:

```text
HANDOFF AUDIT PERSISTED
RESIDUAL CORRECTIVE DESIGN HANDOFF APPROVED / ACTIVE
TARGET AUTHORIZED_TO_START / NOT_STARTED
READY FOR FRESH ACTIVATION AUDIT
```

It remains forbidden to infer from approval and activation:

```text
downstream R1 residual gaps resolved
any R1 lifecycle transition
R1 implementation authority
```

## 17. Fresh-audit correction traceability

This section records the material correction requested by the first fresh handoff audit and the
result of the subsequent fresh independent re-audit.

```text
Fresh handoff audit:
P0=0
P1=2
P2=0

Audit P1-A:
algorithm preselection in identity golden-vector requirements
Correction: MATERIALIZED
Finding status: CLOSED BY FRESH INDEPENDENT RE-AUDIT

Audit P1-B:
regression scope omitted query/historical/transaction semantics
Correction: MATERIALIZED
Finding status: CLOSED BY FRESH INDEPENDENT RE-AUDIT

Corrective handoff fresh re-audit: P0=0 / P1=0 / P2=0
Corrective handoff: MATERIALIZED / APPROVED / ACTIVE
Target: AUTHORIZED_TO_START / NOT_STARTED
Design execution authorized: YES
Maximum disposition: READY FOR FRESH ACTIVATION AUDIT
```

The design executor may not treat this handoff audit as a design-target audit. P1-2 and P1-3 of
R1 remain open pending the future target and its own fresh independent design audit.
