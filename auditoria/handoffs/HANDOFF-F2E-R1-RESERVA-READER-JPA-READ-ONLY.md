# FeelingPilates — Handoff F2E / R1 — Reserva reader JPA read-only

Handoff status: `MATERIALIZED / CORRECTED_TO_FINAL_V2_DESIGN_AUTHORITY / READY_FOR_FRESH_INDEPENDENT_HANDOFF_DOCUMENT_AUDIT / NOT_APPROVED / NOT_ACTIVE / TARGET_NOT_STARTED / IMPLEMENTATION_NOT_STARTED / IMPLEMENTATION_NOT_AUTHORIZED`

Target: `F2E / R1 — Reserva reader`

Type: `IMPLEMENTATION_READ_ONLY / JPA_ADAPTER`

Correction role: `IMPLEMENTATION_AUTHORITY_CORRECTOR / R1_HANDOFF_RECONCILER / V2_CONTRACT_MAPPER / SCOPE_GUARD`

## 1. Authority, lifecycle and entry gate

Repository documents and physical Git evidence are authoritative; chat is coordination only. This
handoff incorporates the final published authority in
`auditoria/fase-2e-diseno-adapters-read-only-snapshot-consistency.md`, including the preserved
corrective amendment 36.1–36.10 and the residual V2 amendment 37.1–37.14. The residual fresh
independent design PASS and publication closure are persisted in
`auditoria/reviews/F2E-ADAPTERS-SNAPSHOT-RESIDUAL-AUTHORITY-GAP-R1-PROVENANCE-JPA-TX-DESIGN-REVIEW.md`.

```text
Authority gap at design layer: RESOLVED_BY_RESIDUAL_CORRECTIVE_DESIGN / CLOSED / PUBLISHED
Residual corrective design audit: PASS / P0=0 / P1=0 / P2=0
Published design commit: f23e91390d21ebd06040f3ed60f631e05d23d653
Published design SHA-256: 6c72cba1f83fbc2bcf3b3219d8252d2410ec482e30ae85d30fd2eeb04e8883d8
Residual corrective handoff: COMPLETED / CLOSED / HISTORICAL / NOT_ACTIVE
Active handoff: NINGUNO
R1 handoff: MATERIALIZED / CORRECTED_TO_FINAL_V2_DESIGN_AUTHORITY / READY_FOR_FRESH_INDEPENDENT_HANDOFF_DOCUMENT_AUDIT / NOT_APPROVED / NOT_ACTIVE
R1 implementation: NOT_STARTED / NOT_AUTHORIZED
```

The internal `NOT_SELF_APPROVED` and `PENDING_FRESH_AUDIT` markers in design section 37 preserve
the historical candidate state before the later independent audit/publication; they are not
rewritten after publication. This handoff correction does not approve or activate R1. Its only
next gate is:

```text
FRESH_INDEPENDENT_HANDOFF_DOCUMENT_AUDIT
```

Only after that independent audit, a separate approval and explicit activation may authorize an
executor. `MATERIALIZED != APPROVED`, `APPROVED != ACTIVE`, `DARK_LAUNCH != PRODUCTIVE`, and
publication does not imply cutover.

Before future implementation, Git branch, expected published `HEAD`/upstream, staging and working
tree must match the activation authority exactly. Any mismatch is fail-closed: stop without reset,
stash, checkout, clean or reconciliation.

Preserved authority:

```text
Pure detector core: IMPLEMENTATION CLOSED / TECHNICAL IMPLEMENTATION GATE PASS
TurnoInstructor: PRODUCTIVE AUTHORITY
R1 adapters: NOT_IMPLEMENTED / NOT_PRODUCTIVE
Runtime: DARK_LAUNCH
Data source: DATA_SOURCE_NOT_AVAILABLE
Data audit: NOT_PERFORMED / NOT_AUTHORIZED
Cutover: false
```

## 2. Authorized implementation result

Once, and only once, this handoff is independently approved and explicitly active, R1 may
materialize this boundary:

```text
native JPA read
-> immutable scalar projection
-> adapter mapper
-> ReservationSourceSnapshot
```

R1 produces immutable reservation snapshots or a closed adapter/policy failure. It does not
produce `DetectorResult`, choose candidates, infer a historical target, create a crosswalk,
resolve a mapping, create a fence, migrate data, expose a consumer or change authority.

```text
Reserva / JPA -> projection -> mapper -> ReservationSourceSnapshot -> pure detector core
pure detector core -/-> JPA / Spring / repository / EntityManager / managed entity
```

No entity, detached entity, Hibernate proxy, `PersistentCollection`, Spring projection proxy,
`Page`, persistence-backed stream, repository or `EntityManager` crosses the boundary.

## 3. Exhaustive physical allowlists

### 3.1 `AUTHORIZED_PRODUCTION_PATHS_EXHAUSTIVE`

These and only these eleven production files may be created:

```text
src/main/java/com/feelingpilates/transicion/programacion/read/ReadSnapshotContext.java
src/main/java/com/feelingpilates/transicion/programacion/read/ReadSnapshotIdentifiers.java
src/main/java/com/feelingpilates/transicion/programacion/read/ReservationScope.java
src/main/java/com/feelingpilates/transicion/programacion/read/ReservationReadPort.java
src/main/java/com/feelingpilates/transicion/programacion/read/ReservationReadFailureCode.java
src/main/java/com/feelingpilates/transicion/programacion/read/ReservationReadException.java
src/main/java/com/feelingpilates/transicion/programacion/adapter/jpa/policy/F2eSqlPolicyViolationException.java
src/main/java/com/feelingpilates/transicion/programacion/adapter/jpa/projection/ReservaProjectionRow.java
src/main/java/com/feelingpilates/transicion/programacion/adapter/jpa/projection/ReservaProjectionQueryExecutor.java
src/main/java/com/feelingpilates/transicion/programacion/adapter/jpa/mapper/ReservaProjectionMapper.java
src/main/java/com/feelingpilates/transicion/programacion/adapter/jpa/ReservaJpaReader.java
```

`F2eSqlPolicyViolationException` is production boundary type under `adapter/jpa/policy`, exactly as
fixed by design 36.3.1. It is not testinfra: production reader code must be able to preserve its
identity without depending on test source code. It has no stereotype and creates no production
reachability.

Package/directory names may be used for organization only:

```text
DIRECTORY_GLOBS: NON_AUTHORIZING
ANY OTHER PRODUCTION FILE, INCLUDING INSIDE THOSE PACKAGES: FORBIDDEN
```

### 3.2 Existing production files

```text
AUTHORIZED_EXISTING_TRACKED_PRODUCTION_MODIFICATIONS: EMPTY SET
```

In particular, it is forbidden to modify:

```text
Reserva.java
ReservaRepository.java
ReservaService.java
ReservaController.java
src/main/java/com/feelingpilates/transicion/programacion/detector/**
pom.xml
application*.properties
application*.yaml
Flyway/**
schema
productive Spring configuration
```

### 3.3 `AUTHORIZED_TEST_PATHS_EXHAUSTIVE`

These and only these ten test files may be created:

```text
src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/ReservaProjectionMapperTest.java
src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/ReservaJpaReaderPostgreSqlTest.java
src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/ReservaJpaReaderTransactionTest.java
src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/ReservaJpaReaderArchitectureTest.java
src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/ReservaJpaReaderRuntimeIsolationTest.java
src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/testinfra/ReaderTransactionTestHarness.java
src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/testinfra/F2ePostgresTestConfiguration.java
src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/testinfra/F2eStatementPolicyInspector.java
src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/testinfra/F2eSelectOnlyRole.java
src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/testinfra/F2eSliceChecksum.java
```

```text
ANY OTHER TEST / HELPER / CONFIG FILE: FORBIDDEN
src/test/** GLOB: NON_AUTHORIZING
adapter/jpa/** GLOB: NON_AUTHORIZING
```

Existing tracked
`src/test/java/com/feelingpilates/TestcontainersConfiguration.java` is `READ/IMPORT ONLY / NO
MODIFICATION`. If it is not physically compatible, the executor uses the allowlisted
`F2ePostgresTestConfiguration`; incompatibility does not authorize editing or adding another file.

No other path is authorized. R2–R6, other handoffs, reviews, canons, resources, frontend/mobile,
reports, credentials and data artifacts remain forbidden.

## 4. Exact read contracts

`ReservationReadPort` exposes exactly:

```java
List<ReservationSourceSnapshot> readByReservationIds(
    ReadSnapshotContext context,
    Set<UUID> reservationIds);

List<ReservationSourceSnapshot> readByScope(
    ReadSnapshotContext context,
    ReservationScope scope);
```

All outputs are immutable defensive copies ordered by `reservationId`. `ReadSnapshotContext` is
not a SQL parameter.

### 4.1 `ReservationScope`

Exact record fields, in order:

| Field | Java type | Nullability | Invariant | Meaning |
| --- | --- | --- | --- | --- |
| `salonIds` | `Set<UUID>` | non-null; elements non-null | non-empty; defensive immutable copy; canonical order is UUID 16 unsigned bytes ascending | salons included |
| `desde` | `LocalDate` | non-null | `desde <= hasta` | inclusive first date |
| `hasta` | `LocalDate` | non-null | `desde <= hasta`; bounded caller-supplied window | inclusive last date |

There is no wildcard/full-table scope and no nullable filter.

### 4.2 `readByReservationIds`

The set is non-null, non-empty and has no null element. Validation and defensive copy happen
before query creation. UUIDs bind in canonical order. Each requested ID must yield exactly one
row. After a valid query:

```text
requestedIds - returnedIds non-empty
-> SOURCE_RECORD_NOT_FOUND
-> whole operation ABORTED
-> zero partial snapshots
```

An unexpected or duplicate ID is a read-set invariant failure, not not-found. Output follows
`ORDER BY r.id`.

### 4.3 `readByScope`

Context and scope are validated before query creation. Null/empty salon set, null element, null
endpoint, unbounded or inverted window is `ADAPTER_INPUT_INVALID` with zero SQL. Zero matching
rows is valid and returns an immutable empty list. Every returned valid row must satisfy the
requested salon/date scope. Output follows `ORDER BY r.id`.

## 5. Historical V1 context/identity text — SUPERSEDED / NON-NORMATIVE

Sections 5.1–6 below are retained only as correction traceability for the pre-residual handoff.
They are wholly superseded for implementation by section 6.1 of this handoff and design sections
37.2–37.8. No V1 formula, caller-supplied trusted label, caller-supplied canonical scope,
arbitrary catalog String or `MULTI_READER_MVCC` language in these historical sections is an
implementation choice or fallback.

### 5.1 Exact context shape

`ReadSnapshotContext` is an immutable record with these fields in this order:

| Field | Java type | Nullability/invariant | Meaning |
| --- | --- | --- | --- |
| `runIdentity` | `String` | non-null, non-blank | caller-owned identity of the complete run |
| `attemptIdentity` | `String` | non-null, non-blank; changes only for a whole-run retry | attempt identity |
| `sourceName` | `String` | non-null, non-blank | named logical datasource or named Testcontainers fixture |
| `schemaFingerprint` | `String` | non-null, non-blank | Flyway plus schema-contract fingerprint |
| `projectionCatalogVersion` | `String` | non-null, non-blank | one version for R1 SQL/projection/mappers |
| `ruleCatalogVersion` | `String` | non-null, non-blank | detector/F2D rules version |
| `businessZone` | `ZoneId` | non-null | explicit business timezone; never JVM default |
| `scopeCanonical` | `String` | non-null, non-blank; supplied after canonical ordering/framing | exact IDs or salon/date scope |
| `snapshotClaim` | `SnapshotClaim` | non-null | `SINGLE_READER_TEST` or `MULTI_READER_MVCC` only |
| `snapshotEvidenceId` | `String` | non-null, non-blank; created by transaction owner | evidence for the claim; never generated by reader |

The nested/adjacent enum contains exactly:

```text
SINGLE_READER_TEST
MULTI_READER_MVCC
```

R1 standalone implementation tests use exactly `SINGLE_READER_TEST`. R1 never fabricates a
multi-reader claim. `MULTI_READER_MVCC` may be received only from a future authorized R6 owner
running the same reader in its RR transaction; R6 is not implemented here.

For `SINGLE_READER_TEST` the proxied harness computes:

```text
snapshotEvidenceId = ID_HASH(
  "F2E-TEST-TX-V1",
  fixtureIdentity,
  testInvocationIdentity,
  declaredIsolation,
  "read only")
```

For R1 `declaredIsolation` is literal lower-case `read committed`. Inputs are deterministic
literals. No clock, random UUID, global state, `pg_current_snapshot()` or PostgreSQL snapshot
fingerprint participates.

The future R6-only claim, recorded here solely so the context has closed semantics, is:

```text
snapshotEvidenceId = ID_HASH(
  "F2E-PG-MVCC-V1",
  datasourceIdentity,
  "repeatable read",
  "read only",
  pgSnapshotInitial)
```

R6 must verify initial/final textual `pg_current_snapshot()` equality. R1 does not perform that
probe.

### 5.2 Formal `SEQ`, scalar representation and hash output

For all identifiers in this section, encode text as UTF-8. For byte string `x`:

```text
LP(x) = ASCII(byteLength(x)) || ":" || x
SEQ(x1..xN) = ASCII(N) || ":" || LP(x1) || ... || LP(xN)
ID_HASH(x1..xN) = lowercaseHex(SHA-256(SEQ(x1..xN)))
```

Counts and lengths are unsigned decimal ASCII without whitespace or leading zero except `0`.
Lengths are UTF-8 byte lengths, not chars/code points. `||` concatenates bytes. Required values
are never null. Empty value is distinct from absence because `LP(empty)="0:"`; no optional input
in these formulas may be silently omitted. All hash output is 64 lower-case hexadecimal chars.

Canonical scalars used by R1:

```text
String/version/claim/enum = exact non-null UTF-8 string; enum uses name()
UUID = RFC-4122 lower-case 8-4-4-4-12
ZoneId = ZoneId.getId(), no default substitution
LocalDate = uuuu-MM-dd
LocalTime = HH:mm:ss.SSSSSS; sub-microsecond value is invalid
OffsetDateTime = UTC uuuu-MM-dd'T'HH:mm:ss.SSSSSS'Z'; no default timezone
```

Sets of UUID are sorted by their 16 unsigned bytes. A canonical map is ordered by unsigned UTF-8
bytes of key; each entry is `SEQ("F2E-NORMALIZED-FIELD-V1", key, value)` and the map is
`SEQ("F2E-NORMALIZED-FIELDS-V1", ASCII(entryCount), entry1..entryN)`. Map iteration order is never
authority.

### 5.3 Exact identifier formulas

`ReadSnapshotIdentifiers` is a non-instantiable utility. Hashing is mandatory, not conditional.
It calculates exactly:

```text
executionProvenanceId = ID_HASH(
  "F2E-EXECUTION-V1",
  runIdentity,
  attemptIdentity,
  sourceName,
  schemaFingerprint,
  projectionCatalogVersion,
  ruleCatalogVersion,
  businessZone.getId(),
  scopeCanonical)

logicalSnapshotId = ID_HASH(
  "F2E-LOGICAL-SNAPSHOT-V1",
  executionProvenanceId,
  snapshotClaim.name(),
  snapshotEvidenceId)

sourceFingerprint = ID_HASH(
  "F2E-SOURCE-V1",
  schemaFingerprint,
  "R1_RESERVA_PROJECTION",
  "V1",
  "LEGACY",
  "RESERVA",
  reservationId,
  canonicalNormalizedFieldsBytes)

snapshotIdentity = ID_HASH(
  "F2E-ATOM-SNAPSHOT-V1",
  logicalSnapshotId,
  "RESERVA",
  reservationId,
  sourceFingerprint)
```

When a pre-framed byte value such as `canonicalNormalizedFieldsBytes` is an argument, `SEQ` still
applies its outer `LP` to those exact bytes. The R1 normalized source fields, in canonical key
order before map framing, are exactly:

```text
activityId
createdAtTechnical
date
end
instructorId
reservationId
salonId
start
state
updatedAtTechnical
```

Values use the scalar rules above. These and only these physical R1 values participate. Excluded:
`cliente_id`, historical target, candidate data, current `TurnoInstructor`, global time, random
values, SQL text/binds, JVM defaults and any inferred functional validity. Technical timestamps
participate as observable source values but remain technical evidence only.

`scopeCanonical` is exact:

```text
readByReservationIds:
  SEQ("F2E-READ-SCOPE-V1", "BY_RESERVATION_IDS", ASCII(idCount), sortedIds...)

readByScope:
  SEQ("F2E-READ-SCOPE-V1", "BY_SCOPE", ASCII(salonCount), sortedSalonIds..., desde, hasta)
```

It is stored/transferred as the exact UTF-8 `SEQ` byte string represented losslessly as a Java
`String`; no pretty JSON or collection `toString()` is permitted.

## 6. Historical V1 projection/provenance text — SUPERSEDED / NON-NORMATIVE

`ReservaProjectionRow` is an immutable scalar record with this exact order and contract:

| Field | Physical source | Java type | Nullable | Invariant/meaning |
| --- | --- | --- | --- | --- |
| `reservationId` | `reserva.id` | `UUID` | no | source/record identity |
| `state` | `reserva.estado` | `String` | no | exact DB enum text, maps only to known `ReservationState` |
| `date` | `reserva.fecha` | `LocalDate` | no | business date |
| `salonId` | `reserva.salon_id` | `UUID` | no | matching dimension |
| `instructorId` | `reserva.instructor_id` | `UUID` | no | matching dimension |
| `activityId` | `reserva.tipo_actividad_id` | `UUID` | no | matching dimension |
| `start` | `reserva.hora_inicio` | `LocalTime` | no | half-open start |
| `end` | `reserva.hora_fin` | `LocalTime` | no | must be after start |
| `createdAtTechnical` | `reserva.creado_en` | `OffsetDateTime` | no | technical insertion time, never validity |
| `updatedAtTechnical` | `reserva.actualizado_en` | `OffsetDateTime` | no | technical last-write time, never history |

The mapper creates exactly:

```text
ReservationSourceSnapshot(
  reservationId,
  ReservationState.valueOf(state),
  date,
  salonId,
  instructorId,
  activityId,
  ReservedSubinterval(start,end),
  snapshotIdentity,
  sourceFingerprint,
  additionalObservableFields,
  Optional.empty(),
  provenance)
```

`additionalObservableFields` contains exactly:

```text
createdAtTechnical -> canonical UTC microsecond string
updatedAtTechnical -> canonical UTC microsecond string
```

`EvidenceProvenance` uses its existing exact seven-field core shape:

```text
sourceName = context.sourceName
schemaFingerprint = context.schemaFingerprint
recordIds = immutable List.of(lowercase reservationId)
ruleId = "R1_RESERVA_PROJECTION"
ruleVersion = "V1"
businessTimeContext = SEQ(
  "F2E-R1-BUSINESS-CONTEXT-V1",
  context.businessZone.getId(),
  context.scopeCanonical)
normalizedFields = immutable map containing exactly:
  activityId, createdAtTechnical, date, end, executionProvenanceId,
  instructorId, logicalSnapshotId, projectionContractId,
  projectionContractVersion, reservationId, salonId, snapshotClaim,
  snapshotEvidenceId, start, state, updatedAtTechnical
```

The two projection constants are `R1_RESERVA_PROJECTION` and `V1`; identity values are the exact
outputs of section 5. Map keys are non-blank and values non-null. `recordIds` are physical IDs,
not target/candidate IDs. No PII, `cliente_id`, SQL, credential, raw invalid field or inferred
historical value enters provenance.

`historicalProgrammingTarget` is always `Optional.empty()` for every R1 row. R1 cannot infer a
target from containment, field equality, an active legacy turn, uniqueness, a candidate or a
crosswalk. `ReservedSubinterval` is `[start,end)` and requires `end > start`.

## 6.1 Final normative V2 identity, projection and provenance authority

This section is the sole implementation authority replacing historical sections 5–6. It copies
the final published contract of design sections 37.2–37.8; where any wording differs, the
published design at SHA-256
`6c72cba1f83fbc2bcf3b3219d8252d2410ec482e30ae85d30fd2eeb04e8883d8` prevails and the
handoff must fail closed rather than select an alternative.

### 6.1.1 `F2E_IDENTITY_V2`

For every byte string `x` and ordered sequence `x1..xN`:

```text
LP(x) = ASCII(unsignedDecimal(byteLength(x))) || UTF8(":") || x
SEQ(x1..xN) = ASCII(unsignedDecimal(N)) || UTF8(":") || LP(x1) || ... || LP(xN)
ID_HASH_V2(x1..xN) = lowercaseHex(SHA-256(SEQ(x1..xN)))
```

Counts and lengths are unsigned decimal without sign, whitespace or leading zero except `0`.
Lengths are bytes, never chars/code points. Output is exactly 64 lower-case ASCII hex characters.
A pre-framed argument receives its outer `LP`; it is never flattened.

Canonical scalars are exact UTF-8 text; enum `name()`; lower-case RFC-4122 UUID; explicit
`ZoneId.getId()`; `LocalDate=uuuu-MM-dd`; `LocalTime=HH:mm:ss.SSSSSS`; and UTC
`OffsetDateTime=uuuu-MM-dd'T'HH:mm:ss.SSSSSS'Z'`. Sub-microsecond values, null required values,
empty required text, NUL, unpaired surrogate or replacement encoding fail before identity/output.
No trim, case-fold or Unicode normalization is permitted.

UUID sets are non-null, contain no null/duplicate serialized values and sort by unsigned 16-byte
UUID value. Canonical maps sort keys by unsigned UTF-8 bytes, reject encoded duplicate keys and
use:

```text
entry = SEQ("F2E-R1-NORMALIZED-FIELD-V2", key, value)
map = SEQ("F2E-R1-NORMALIZED-FIELDS-V2", ASCII(entryCount), entries...)
```

`ReadSnapshotIdentifiers` owns these primitives and the formulas below. Per invocation it also
binds every digest to its complete preimage and aborts a different-preimage collision with exactly
`IllegalStateException("F2E identity hash collision detected")`.

### 6.1.2 Closed projection catalog and context

The only catalog authority is
`ReadSnapshotContext.ProjectionCatalogVersion.R1_RESERVA_V1`, with:

```text
projectionContractId = R1_RESERVA_PROJECTION
projectionContractVersion = V1
canonicalCatalogValue = R1_RESERVA_PROJECTION/V1
sourceSystem = LEGACY
sourceAtomType = RESERVA
physicalTable = public.reserva
mapper contract = ReservaProjectionMapper / V1
data statements = R1_RESERVA_BY_IDS_V1 | R1_RESERVA_BY_SCOPE_V1
```

No String catalog, alias, fallback, `UNKNOWN` or negotiation exists. The corrected immutable
`ReadSnapshotContext` fields, in order, are:

```text
runIdentity: String
attemptIdentity: String
readerInvocationIdentity: String
sourceName: String — copied only from DescriptorRecursoLector
schemaFingerprint: String — copied only from DescriptorRecursoLector
projectionCatalogVersion: ProjectionCatalogVersion — exactly R1_RESERVA_V1
ruleCatalogVersion: String
businessZone: ZoneId
snapshotClaim: SnapshotClaim — exactly SINGLE_READER_TEST
snapshotEvidenceId: 64-char lower hex — harness-owned
statementObservationFingerprint: 64-char lower hex — harness-owned commitment
```

The caller may propose only run, attempt, transaction-boundary and reader-invocation identities,
rule catalog, business zone and typed port scope. It cannot provide/override `sourceName`,
`identidadFuenteDatos`, `schemaFingerprint`, catalog Strings, canonical scope, evidence IDs,
statement observations or final identities.

The typed scope is validated once and serialized once before query creation:

```text
readByReservationIds:
  scopeBytes = SEQ("F2E-R1-READ-SCOPE-V2", "READ_BY_RESERVATION_IDS",
                   ASCII(idCount), sortedReservationIds...)

readByScope:
  scopeBytes = SEQ("F2E-R1-READ-SCOPE-V2", "READ_BY_SCOPE",
                   ASCII(salonCount), sortedSalonIds..., desde, hasta)

scopeCanonical = UTF8-decode-strict(the same scopeBytes)
```

`MULTI_READER_MVCC`, `REPEATABLE_READ`, `pg_current_snapshot()` and another claim are not R1
capabilities. Any attempted foreign claim fails before registry reservation, resource validation,
capture, probes or SQL with exactly
`IllegalArgumentException("Unsupported R1 snapshot claim")` and zero identities/output.

After registry reservation and successful resource proof, the harness creates:

```text
snapshotEvidenceId = ID_HASH_V2(
  "F2E-R1-SINGLE-READER-TEST-EVIDENCE-V2",
  descriptor.identidadFuenteDatos,
  transactionBoundaryIdentity,
  "f2eReaderTransactionManager",
  "f2eReaderPersistenceUnit",
  "read committed",
  "read only")

statementObservationFingerprint = ID_HASH_V2(
  "F2E-R1-STATEMENT-OBSERVATIONS-V2",
  snapshotEvidenceId,
  observedIsolationValue,
  observedAccessMode,
  ASCII(statementCount),
  committedCatalogStatementId1, ..., committedCatalogStatementIdN)
```

The statement commitment and final capture both contain exactly `N=3` entries in this order:
isolation probe, read-only probe and the one operation-specific data statement. Isolation is
`read committed`; PostgreSQL access value `on` canonicalizes to `read only`. The harness compares
the final recomputation to the commitment before any result escapes.

### 6.1.3 Exact four identities

```text
executionProvenanceId = ID_HASH_V2(
  "F2E-R1-EXECUTION-PROVENANCE-V2",
  runIdentity, attemptIdentity, readerInvocationIdentity, operation,
  sourceName, schemaFingerprint,
  projectionCatalogVersion.canonicalCatalogValue,
  ruleCatalogVersion, businessZone.getId(), scopeBytes,
  snapshotClaim.name(), snapshotEvidenceId,
  statementObservationFingerprint)

logicalSnapshotId = ID_HASH_V2(
  "F2E-R1-LOGICAL-SNAPSHOT-V2",
  sourceName, schemaFingerprint,
  projectionCatalogVersion.canonicalCatalogValue,
  ruleCatalogVersion, businessZone.getId(), scopeBytes,
  snapshotClaim.name(), snapshotEvidenceId)

sourceFingerprint = ID_HASH_V2(
  "F2E-R1-SOURCE-FINGERPRINT-V2",
  sourceName, schemaFingerprint,
  projectionCatalogVersion.canonicalCatalogValue,
  "LEGACY", "RESERVA", reservationId,
  canonicalSourceProjectionBytes)

snapshotIdentity = ID_HASH_V2(
  "F2E-R1-SNAPSHOT-IDENTITY-V2",
  logicalSnapshotId, executionProvenanceId,
  projectionCatalogVersion.canonicalCatalogValue,
  "LEGACY", "RESERVA", reservationId, sourceFingerprint)
```

`executionProvenanceId` is invocation-specific. `logicalSnapshotId` excludes run, attempt,
reader invocation, operation mechanics, statement manifest and execution ID. `sourceFingerprint`
excludes execution/scope/query/rules/zone. `snapshotIdentity` binds the concrete output payload to
both logical and execution evidence.

### 6.1.4 Canonical projection and provenance

The eleven source positions are exactly:

```text
1 reservationId/id/UUID/VALUE
2 state/estado/TEXT/VALUE
3 date/fecha/DATE/VALUE
4 salonId/salon_id/UUID/VALUE
5 instructorId/instructor_id/UUID/VALUE
6 activityId/tipo_actividad_id/UUID/VALUE
7 start/hora_inicio/TIME_MICROS/VALUE
8 end/hora_fin/TIME_MICROS/VALUE
9 createdAtTechnical/creado_en/TIMESTAMP_UTC_MICROS/VALUE
10 updatedAtTechnical/actualizado_en/TIMESTAMP_UTC_MICROS/VALUE
11 historicalProgrammingTarget/NONE/OPTIONAL_HISTORICAL_TARGET/ABSENT/<empty bytes>
```

For each position:

```text
sourceField = SEQ(
  "F2E-R1-SOURCE-FIELD-V2",
  ASCII(position), logicalName, physicalColumnOr"NONE", typeTag,
  "VALUE"|"ABSENT", canonicalValueOrEmptyBytes)

canonicalSourceProjectionBytes = SEQ(
  "F2E-R1-CANONICAL-PROJECTION-V2",
  "R1_RESERVA_PROJECTION", "V1", "11",
  sourceField1, ..., sourceField11)
```

The authoritative vector projection is exactly `1210` bytes. `cliente_id` is excluded from query,
row, projection, identity, provenance and errors; it remains only in the private no-write checksum.

`EvidenceProvenance` retains its existing seven-field core shape. For every row it uses trusted
context source/schema, exactly one canonical reservation record ID, rule
`R1_RESERVA_PROJECTION/V1`, business context
`SEQ("F2E-R1-BUSINESS-CONTEXT-V2", businessZone.getId(), scopeBytes)` and an immutable map with
exactly these 32 keys:

```text
activityId, attemptIdentity, businessZone, createdAtTechnical, date, end,
executionProvenanceId, historicalProgrammingTarget, instructorId, logicalSnapshotId,
operation, projectionCatalogVersion, projectionContractId, projectionContractVersion,
readerInvocationIdentity, reservationId, ruleCatalogVersion, runIdentity, salonId,
scopeCanonical, snapshotClaim, snapshotEvidenceId, snapshotIdentity, sourceAtomType,
sourceFingerprint, sourceSystem, start, state, statementObservationFingerprint,
transactionAccessMode, transactionIsolation, updatedAtTechnical
```

No extra key is permitted. `historicalProgrammingTarget=ABSENT` and the snapshot value is always
`Optional.empty()`. `additionalObservableFields` contains exactly the two technical timestamps.
PII, SQL, binds, credentials, connection metadata/object IDs, thread IDs, arbitrary caller maps,
raw invalid values and inferred targets are excluded.

### 6.1.5 Authoritative golden vector

The exact common inputs are:

```text
K=r1-a
sourceName=fixture:postgres16:r1-a
identidadFuenteDatos=fixture-r1-a
schemaFingerprint=sha256:aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa
runIdentity=run-2026-09-13-001
attemptIdentity=attempt-01
readerInvocationIdentity=reader-invocation-0001
transactionBoundaryIdentity=tx-boundary-0001
operation=READ_BY_RESERVATION_IDS
projectionCatalogVersion=R1_RESERVA_PROJECTION/V1
ruleCatalogVersion=F2D-RULE-CATALOG/V1
businessZone=America/Mexico_City
scope reservationId=00000000-0000-4000-8000-000000000001
snapshotClaim=SINGLE_READER_TEST
observedIsolationValue=read committed
observedAccessMode=read only
```

The row fields are exactly:

```text
reservationId=00000000-0000-4000-8000-000000000001
state=CONFIRMADA
date=2026-09-15
salonId=00000000-0000-4000-8000-000000000002
instructorId=00000000-0000-4000-8000-000000000003
activityId=00000000-0000-4000-8000-000000000004
start=08:30:00.000000
end=09:30:00.000000
createdAtTechnical=2026-09-01T14:00:00.000000Z
updatedAtTechnical=2026-09-02T15:30:00.000000Z
historicalProgrammingTarget=ABSENT/<empty bytes>
```

The exact two probe IDs and data ID are those in section 11.3, in the required order. The complete
byte-exact preimages are fixed by the formulas and inputs above and are reproduced without
abbreviation in published design section 37.8. Expected outputs are exactly:

```text
snapshotEvidenceId=f195883f1312824ab4cc5e6ff5a865a54fa7e1fb45dafe6cc49f5865a8952877
statementObservationFingerprint=7f7e27a90a3efff6803bf4a8e40dbe4377d8bf1a754187cb14502f47af135838
executionProvenanceId=b081c98ef4b71b8395051b3a9be319cc7d149c9306109828a0fbcb94a7c70795
logicalSnapshotId=d5b95d6a6c3b9feeee9c2cefdf93a1229f065b783e2db7984b6c6957234b8e88
sourceFingerprint=e98cb3c5c325bae4fd7a8541121e690d0e27e6d9fd81ab756add5ca0f9e99e3e
snapshotIdentity=5c27b72c6f6d7f183eec18d4e3d7b6383c9418f789b96485f892252c6a019d13
canonicalSourceProjectionBytes.length=1210
```

Tests must independently recompute every preimage/digest and must not use implementation output as
expected authority.

### 6.1.6 Mandatory cross-consistency

```text
typed port scope == the sole internally-derived scopeBytes
context catalog enum == query catalog == mapper V1 == fingerprint/provenance catalog
descriptor labels == context labels == reader-injected trusted labels
row reservationId == snapshot reservationId == provenance.recordIds[0]
row fields 1..11 == source fingerprint preimage == snapshot/provenance payload
all rows in one call share executionProvenanceId and logicalSnapshotId
each row has its own sourceFingerprint and snapshotIdentity
statementObservationFingerprint == final ordered manifest from the same reader SessionFactory
snapshotEvidenceId == exact harness transaction-boundary/resource contract
```

The reader recomputes all four identities from final immutable objects and compares digest bytes in
constant time before output. Any mismatch discards the complete batch and propagates exactly
`IllegalStateException("F2E identity/provenance consistency not proven")`; it does not add or
reclassify a `ReservationReadFailureCode`.

## 7. Query and binding contract

`ReservaProjectionQueryExecutor` is plain, adapter-local and constructor-injected with the reader
`EntityManager` specified in section 10. It is not a Spring Data repository and has no stereotype.

Both statements use `EntityManager.createNativeQuery`, unwrap
`org.hibernate.query.NativeQuery`, use named parameters and materialize concrete
`ReservaProjectionRow` records. The SQL shape is exactly the canonical SQL cataloged in section
11; physical table identity is `public.reserva`.

```text
UUID set:
  validate and defensive-copy Set<UUID>
  sort by UUID unsigned bytes
  SQL physical_uuid_column IN (:parameterName)
  NativeQuery.setParameterList(parameterName, sortedIds, UUID.class)

LocalDate scalar:
  NativeQuery.setParameter(name, value, LocalDate.class)
```

No empty collection reaches Hibernate. Forbidden: SQL interpolation, UUID literals, manual bind
expansion, positional caller parameters, `ANY(uuid[])`, `java.sql.Array`, temporary table,
untyped `setObject`, text conversion, nullable filters, dynamic predicate or state filter. R1
projects `estado`; it does not filter it. It never selects `cliente_id`, loads `Reserva`, joins a
master table, fetches associations or navigates lazy state.

## 8. Exhaustive R1 failure contract

### 8.1 `ReservationReadFailureCode`

The enum contains exactly:

```text
ADAPTER_INPUT_INVALID
SOURCE_RECORD_NOT_FOUND
READ_SET_INVARIANT_VIOLATION
SOURCE_ACCESS_FAILURE
```

`INPUT_INVALID` is stale historical wording, is not an alias and is forbidden in implementation.
Semantic detector statuses, transaction/no-write guards, HostValidator/environment and SQL policy
do not belong to this enum.

| Code | Exhaustive trigger | Stage | SQL executed? | Batch/partial output | Retry | Cause | Safe context |
| --- | --- | --- | --- | --- | --- | --- | --- |
| `ADAPTER_INPUT_INVALID` | context/collection/scope/scalar null; empty collection; null element; unbounded/inverted window; invalid required UUID/scalar; or projected null/unknown state/type/invalid positive range that cannot map | public-port validation or projection mapper | no for caller input; yes for invalid row | abort whole operation; none | non-retryable with same input/snapshot | null before SQL; original mapping cause allowed | operation, safe scope/counts; row ordinal/column name but never value |
| `SOURCE_RECORD_NOT_FOUND` | only `readByReservationIds`: `requestedIds - returnedIds` non-empty after valid query | post-query completeness | yes | abort whole batch; none | non-retryable in same snapshot | null | requested/missing sorted IDs, counts, statement ID |
| `READ_SET_INVARIANT_VIOLATION` | only one of four triggers in 8.2 | post-query assembly/completeness | yes | abort whole operation; none | non-retryable in same snapshot | nullable; preserve concrete internal cause if one exists | unexpected/duplicate IDs, safe counts, statement ID |
| `SOURCE_ACCESS_FAILURE` | `PersistenceException`, `HibernateException`, `SQLException` or subtype anywhere in the cause chain during create/bind/execute/materialize, provided the chain has no policy exception; also driver/connection failure observable inside reader | physical JPA read | no if create/bind fails; may have started for execute/materialize | abort whole operation; none | external policy may retry whole attempt only; never internal/partial | required and preserved | operation, safe scope/counts, statement ID if known, SQLState class/vendor code if present |

### 8.2 Closed read-set invariant triggers

`READ_SET_INVARIANT_VIOLATION` is created only when:

1. `readByReservationIds` returns an ID outside the requested set;
2. `readByScope` returns a valid row outside requested salon/date scope;
3. either operation observes more than one physical row for one reservation ID;
4. after individually valid rows map, snapshot count differs from valid physical row count, or a
   snapshot ID differs from its source row ID.

It is not caller invalidity, physical-row invalidity, missing requested IDs, SQL policy,
HostValidator/environment, transaction guard, JPA/Hibernate/JDBC failure or a catch-all.

### 8.3 `ReservationReadException`

Exact shape:

```text
final class ReservationReadException extends RuntimeException
  failureCode: ReservationReadFailureCode, required
  message: stable literal selected only from failureCode, required
  safeContext: immutable Map<String,String>, required/non-null
  cause: Throwable, nullable only as specified above
```

Stable messages:

```text
ADAPTER_INPUT_INVALID -> "Reservation read input or projected row is invalid"
SOURCE_RECORD_NOT_FOUND -> "One or more requested reservation records were not found"
READ_SET_INVARIANT_VIOLATION -> "Reservation read-set invariant was violated"
SOURCE_ACCESS_FAILURE -> "Reservation source access failed"
```

Allowed `safeContext` keys are exhaustive:

```text
operation
projectionContractId
scopeKind
reservationIds
salonIds
fromDate
toDate
requestedCount
returnedCount
missingReservationIds
unexpectedReservationIds
duplicateReservationIds
physicalRowOrdinal
physicalColumn
sqlCatalogStatementId
sqlStateClass
vendorErrorCode
```

`operation` is `READ_BY_RESERVATION_IDS` or `READ_BY_SCOPE`; `scopeKind` is
`BY_RESERVATION_IDS` or `BY_SCOPE`. UUID lists are lower-case, ordered by unsigned 16-byte value
and comma-separated; dates are `yyyy-MM-dd`; numeric values are decimal ASCII. Omit an
inapplicable key; never insert null. `sqlStateClass` is at most its first two ASCII chars.
`physicalColumn` is a cataloged column name only.

Messages/context/normal reporting must never include `cliente_id`, personal names, email/phone,
credentials/URL, raw projected values, bind values, raw/canonical SQL or expanded parameters.
Cause/stack are internal-only and never serialized into message/context/report.

Wrapping rules and catch precedence are exact:

```text
1. ReservationReadException -> rethrow same instance
2. first F2eSqlPolicyViolationException found cycle-safely in cause chain
   -> rethrow that exact instance
3. PersistenceException | HibernateException | SQLException in cause chain
   -> one ReservationReadException(SOURCE_ACCESS_FAILURE), preserving outer caught cause/chain
4. any other RuntimeException -> rethrow same instance
5. Error -> never wrap
```

Deliberate caller validation creates invalid-input without cause; deliberate row conversion keeps
the original conversion cause if one exists. Expected not-found has null cause. A concrete cause
that detects an invariant may be retained. There is no `catch RuntimeException ->
READ_SET_INVARIANT_VIOLATION` or other residual classifier. `IllegalTransactionStateException`
from the proxy and pre-semantic environment failures remain unchanged/outside the reader.

## 9. SQL-policy exception boundary

`F2eSqlPolicyViolationException` is final `RuntimeException`, not
`ReservationReadException`, `PersistenceException` or `HibernateException`. It carries exactly:

```text
reason: NORMALIZATION_REJECTED | CATALOG_MISS | STATEMENT_CLASS_DENIED | DENYLIST_VIOLATION
catalogStatementId: optional 64-char lower-case SHA-256, only when safely computed
```

It never carries SQL raw/canonical, binds, credentials, URL or projected data. Only the test-only
`F2eStatementPolicyInspector` creates it in R1. Owner:
`F2eStatementPolicyInspector / integration-no-write gate`.

```text
inspect(raw SQL)
-> normalize F2E_SQL_CANON_V1
-> compute catalog ID if normalization succeeded
-> validate statement class + denylist + four-entry catalog
-> violation: throw before returning SQL
-> allowed: return original SQL to Hibernate/JDBC
```

The rejected statement is never prepared/executed. Policy failure propagates unchanged, including
when Hibernate wraps it. It is never mapped to `ReservationReadException`,
`READ_SET_INVARIANT_VIOLATION` or `SOURCE_ACCESS_FAILURE`.

## 10. Fail-closed test-only JPA topology

### 10.1 Privileged plane

Privileged Testcontainers connection/resources may only:

- start container and bootstrap schema;
- run Flyway and `ddl-auto=validate` bootstrap as applicable;
- insert/commit fixtures;
- create/drop and grant the ephemeral SELECT-only login;
- execute the negative write control;
- resolve frozen checksum row IDs and calculate baseline/final checksum/count;
- clean up after the measured gate.

They are never injected into `ReservaProjectionQueryExecutor`, `ReservaJpaReader`, mapper or
transaction harness. Privileged setup/Flyway/observer work occurs outside the inspected reader
SessionFactory/window.

### 10.2 Reader plane and exact bean names

`F2ePostgresTestConfiguration`, imported explicitly only by R1 tests, creates the complete reader
plane with these exact names and qualifiers:

```text
f2eReaderDataSource
f2eReaderEntityManagerFactory
f2eReaderEntityManager
f2eReaderTransactionManager
f2eStatementPolicyInspector
readerTransactionTestHarness
reservaProjectionQueryExecutor
reservaProjectionMapper
reservaJpaReader
```

It also owns exactly one immutable, non-bean, non-public `DescriptorRecursoLector` nested in
`F2ePostgresTestConfiguration`. The descriptor is created once after privileged bootstrap has
applied/verified Flyway, computed the installed schema fingerprint and created the ephemeral
SELECT-only login. The caller, test callback and `ReadSnapshotContext` cannot receive, construct,
replace or mutate it.

The descriptor stores exactly:

```text
sourceName
identidadFuenteDatos
schemaFingerprint
jdbcUrlCanonicaSinCredenciales
databaseName
schemaName = public
credentialPrincipal
identity reference: f2eReaderDataSource
identity reference: f2eReaderEntityManagerFactory / PU f2eReaderPersistenceUnit
identity reference: f2eReaderTransactionManager
identity reference: f2eReaderEntityManager shared proxy
```

For fixture-catalog key `K`, assigned only by the configuration:

```text
identidadFuenteDatos = "fixture-" || K
sourceName = "fixture:postgres16:" || K
schemaFingerprint = immutable observed Flyway+schema result from that same endpoint/database/schema
```

No expected fingerprint supplied by caller can substitute the observed result. Password, URL with
credentials and sensitive connection values never enter serializable descriptor data, identities
or provenance.

`f2eReaderDataSource` uses the same container endpoint/database but exclusively the ephemeral
reader username/password created by `F2eSelectOnlyRole`. It has no privileged credential, routing
or `SET ROLE`. The role has only `CONNECT`, `USAGE` on `public`, and `SELECT` on
`public.reserva`; it has no CREATE, DML, sequence or application-function privilege.

`f2eReaderEntityManagerFactory` uses only `f2eReaderDataSource`, persistence-unit name
`f2eReaderPersistenceUnit`, the PostgreSQL dialect/entity metadata required to boot, and the exact
instance `f2eStatementPolicyInspector` through Hibernate
`hibernate.session_factory.statement_inspector`. It is not built from, delegated to or backed by
the privileged/default datasource.

`f2eReaderEntityManager` is the transaction-aware shared `EntityManager` created exclusively from
`f2eReaderEntityManagerFactory`. `ReservaProjectionQueryExecutor` receives exactly
`@Qualifier("f2eReaderEntityManager")`; no default or privileged `EntityManager` is eligible.

`f2eReaderTransactionManager` is a `JpaTransactionManager` constructed exclusively with
`f2eReaderEntityManagerFactory` and explicitly fixed to `f2eReaderDataSource`.
`ReaderTransactionTestHarness` and the reader advisor both select this manager by exact name. No
generic `PlatformTransactionManager`, `@Primary`, `TransactionManagementConfigurer`, default,
privileged or routing fallback is permitted.

`ReaderTransactionTestHarness` and `ReservaJpaReader` are registered as distinct Spring beans so
both annotations cross real proxies. The test calls harness proxy -> callback -> reader proxy;
there is no self-invocation and no manual `new ReservaJpaReader(...)` for propagation evidence.

The harness method used by R1 is exactly:

```java
@Transactional(
    transactionManager = "f2eReaderTransactionManager",
    propagation = REQUIRES_NEW,
    isolation = READ_COMMITTED,
    readOnly = true)
```

Each public `ReservaJpaReader` port method is exactly:

```java
@Transactional(
    transactionManager = "f2eReaderTransactionManager",
    propagation = MANDATORY,
    readOnly = true)
```

The main class is plain and is allowed to name this stable test-only bean contract because it has
no stereotype, bean, component scan, configuration, property or consumer. Without an explicitly
registered `reservaJpaReader` bean and the exact manager, there is no executable advisor and no
fallback. A future R6 must receive separate authority and provide the same bean-name/resource
contract; R6 is not authorized here.

The harness advisor opens/closes the only R1 transaction. The reader advisor resolves the same
manager, verifies `MANDATORY` and joins it; it never opens, suspends, upgrades or retries a
transaction. Nested/new transactions in callback, reader, executor, mapper or probes are
forbidden.

### 10.3 Shared EntityManager, Session and physical Connection proof

`f2eReaderEntityManager` is the single transaction-aware shared proxy produced only from
`f2eReaderEntityManagerFactory`. Harness probes and `ReservaProjectionQueryExecutor` receive the
same proxy bean by exact qualifier. During R1 it delegates to the single EntityManager/Session
bound by `f2eReaderTransactionManager` to that EMF. `EntityManagerFactory.createEntityManager`, a
second shared EM, default/privileged EM and unqualified `@PersistenceContext` are forbidden.

After begin and before the first probe the harness requires:

```text
EntityManager.isJoinedToTransaction() = true
Hibernate Session default-read-only = true
Hibernate flush mode = MANUAL
```

The persistence context lives only for this transaction. R1 materializes scalars/records, never
managed entities. `persist`, `merge`, `remove`, `flush`, dirty managed state, `clear`, `detach` and
lazy/proxy output are forbidden.

Before capture/probes, the harness obtains the reader connection only through
`f2eReaderEntityManager.unwrap(Session.class).doReturningWork(...)`. It performs no SQL and:

1. compares by Java identity the DS/EMF/TM/shared-EM instances with the descriptor;
2. verifies the shared EM is joined to the transaction of those exact TM/EMF instances;
3. validates, byte-for-byte, sanitized `DatabaseMetaData.getURL()`, `Connection.getCatalog()`,
   `Connection.getSchema()` and `DatabaseMetaData.getUserName()` against descriptor URL,
   database, `public` and credential principal;
4. verifies that endpoint/database/schema are the same instance used for the observed
   `schemaFingerprint`; and
5. verifies reader-injected labels and internal context labels equal the descriptor values.

The only accepted URL form is
`jdbc:postgresql://<host-lowercase-ascii>:<decimal-port>/<databaseName>`; query, fragment,
user-info, password, percent encoding and alternate aliases are rejected. After callback, a second
`doReturningWork` only compares the physical `Connection` reference with the first one; it does
not query metadata or execute SQL.

`DataSource.getConnection`, `DriverManager`, `JdbcTemplate`, another `doWork`, caller-supplied
JDBC connection, `createEntityManager`, second Session or independent/secondary Connection are
forbidden in the measured reader path. A second connection can never verify the first.

Any descriptor, label, instance, joined-resource or connection-metadata mismatch throws exactly
`IllegalStateException("F2E reader resource provenance not proven")` before capture, probes or
domain SQL. It accepts no `snapshotEvidenceId`, emits no identity/snapshot and is not reclassified
as `ReservationReadFailureCode`.

### 10.4 Exact probes, capture and statement manifest

The harness opens a non-nestable ThreadLocal capture on the exact
`f2eStatementPolicyInspector` after resource proof and snapshot evidence creation, then executes
through the same shared EntityManager/SessionFactory exactly:

```text
1. R1_TX_ISOLATION_V1
   SELECT current_setting('transaction_isolation')
   required result: read committed

2. R1_TX_READ_ONLY_V1
   SELECT current_setting('transaction_read_only')
   required PostgreSQL result: on
   canonical semantic value: read only

3. exactly one of R1_RESERVA_BY_IDS_V1 or R1_RESERVA_BY_SCOPE_V1
```

The inspector normalizes and validates each statement using sections 9 and 11, records the ordered
catalog ID before returning allowed SQL and never records binds/raw SQL as evidence. The harness
commits to the expected three-ID manifest before callback, recalculates it from the exact captured
manifest after callback and requires equality before any result escapes. Missing, extra,
out-of-order, nested, reused, cross-thread or unclosed capture fails closed.

`SHOW`, `SET`, `pg_current_snapshot()`, metadata/schema SQL, another probe and every unknown
statement—including unknown `SELECT`—are forbidden. The same
`F2eSqlPolicyViolationException` instance propagates before JDBC according to section 9.

The final call path is exactly:

```text
test -> harness proxy -> explicit f2eReaderTransactionManager REQUIRES_NEW/RC/readOnly
-> one transaction-bound reader EM/Session/Connection
-> DescriptorRecursoLector proof with zero SQL
-> inspector capture -> isolation probe -> read-only probe
-> callback -> reader proxy -> explicit same TM MANDATORY/readOnly
-> executor -> same shared EM/Session/Connection -> one data SELECT
-> mapping/identity/provenance -> final manifest/connection/recompute checks
-> terminal uniqueness transition -> transaction completion -> immutable result only
```

### 10.5 `RegistroUnicidadIdentidades`

The only registry is private state of the singleton `readerTransactionTestHarness` within the one
test-only `ApplicationContext` that also owns one descriptor. It is not another bean, static/global
state, caller state, file, DB or filesystem artifact. Its namespace survives method/transaction
completion and ends only when that `ApplicationContext` closes; it makes no cross-context,
cross-process, cross-JVM, restart or durable guarantee.

It compares complete key bytes—not only digests—in four independent domains:

```text
runKey = SEQ("F2E-R1-UNIQUENESS-RUN-V2",
             descriptor.identidadFuenteDatos, runIdentity)

attemptKey = SEQ("F2E-R1-UNIQUENESS-ATTEMPT-V2",
                 descriptor.identidadFuenteDatos, runIdentity, attemptIdentity)

boundaryKey = SEQ("F2E-R1-UNIQUENESS-BOUNDARY-V2",
                  descriptor.identidadFuenteDatos, runIdentity, attemptIdentity,
                  transactionBoundaryIdentity)

invocationKey = SEQ("F2E-R1-UNIQUENESS-INVOCATION-V2",
                    descriptor.identidadFuenteDatos, runIdentity, attemptIdentity,
                    readerInvocationIdentity)
```

After seed/typed-scope/claim validation and before resource proof/probes/SQL, one linearizable
critical operation reserves all keys or none:

1. create `runKey=OPEN` if absent; an existing run admits only a new `attemptIdentity` from
   `OPEN_AFTER_ABORT`, never while an attempt is `ACTIVE` or `UNKNOWN`;
2. require attempt/boundary/invocation keys all absent;
3. insert those three as `ACTIVE` and bind the active attempt to the run; and
4. on any collision insert nothing and throw exactly
   `IllegalStateException("F2E execution provenance identity reuse")` before probes/SQL.

Observable check-then-put and partial reservation are forbidden. Competing invocations cannot both
reserve the same key.

Lifecycle is exact:

```text
no completed reservation
  -> no marker; same inputs may try again

ACTIVE + fully validated result immediately before successful escape
  -> attempt/boundary/invocation = CONSUMED_SUCCESS
  -> run = COMPLETED_SUCCESS
  -> no key/new attempt is reusable inside this namespace

ACTIVE + failure before accepted escape, including evidence/probe/read/recompute/rollback
  -> attempt/boundary/invocation = CONSUMED_ABORTED
  -> run = OPEN_AFTER_ABORT
  -> consumed keys never reusable; retry only with a new attemptIdentity

failure after identities are built but before publication
  -> same CONSUMED_ABORTED / OPEN_AFTER_ABORT transition

failure/interruption after result escape or success marker
  -> CONSUMED_SUCCESS / COMPLETED_SUCCESS; reuse/retry prohibited

interruption/crash without terminal classification
  -> ACTIVE becomes logically UNKNOWN or remains ACTIVE
  -> reuse and another attempt for the run blocked until ApplicationContext close
```

Release only relinquishes mutex/active synchronization ownership. It never deletes
`CONSUMED_SUCCESS`, `CONSUMED_ABORTED`, `UNKNOWN`, their keys or run history and never makes a
consumed identity reusable. A new `ApplicationContext` intentionally starts a new namespace and
may reuse the same strings.

### 10.6 Mandatory pre-read and failure ordering

```text
validate seed/typed scope/SINGLE_READER_TEST
-> atomic uniqueness reservation
-> resolve catalog and sole scopeBytes
-> open exact reader transaction/resource graph
-> prove DescriptorRecursoLector against same Session/Connection
-> create snapshotEvidenceId and open capture
-> two ordered probes and statement commitment
-> one data SELECT
-> validate/map rows and build immutable provenance/identities
-> verify final manifest, same Connection and all recomputations
-> apply uniqueness terminal transition
-> allow immutable result to escape
```

No domain SQL, evidence acceptance, identity publication or partial output may occur before
resource proof. No resource switch, new connection or new session is permitted between proof,
probes and data SELECT. Every failure discards the complete batch and follows the exact owner/type
already defined by design sections 36–37; no second exception vocabulary is introduced.

Calling the reader proxy outside the harness must throw `IllegalTransactionStateException` before
SQL/output. A manually constructed harness/reader cannot satisfy propagation evidence. No
production `@Configuration`, property file or component scanning is added. Tests fail if a
privileged/default/autoconfigured resource supplies the graph or if any reader SQL is invisible to
the exact inspector.

## 11. `F2E_SQL_CANON_V1` and catalog

### 11.1 Exact normalization

Input is the exact `String` delivered to `StatementInspector.inspect` before execution. It
contains rendered Hibernate/dialect SQL and bind markers, not bound values.

Process Java code points and emit UTF-8:

1. Reject null, empty, NUL, unclosed quote/dollar quote, any comment opener outside quotes, or any
   semicolon outside quotes.
2. Recognize and preserve exactly single quotes with `''`, double quotes with `""`, and
   PostgreSQL `$$...$$`/`$tag$...$tag$` where tag is
   `[A-Za-z_][A-Za-z0-9_]*`.
3. Outside quotes, collapse every non-empty run of ASCII whitespace U+0009..U+000D or U+0020 to
   one U+0020 and remove leading/trailing runs. Other Unicode whitespace is unchanged.
4. Preserve casing exactly; no keyword/identifier folding and no Unicode normalization.
5. Outside quotes, normalize `?`, `?` plus one or more digits, and `$` plus one or more digits to
   `?`. Quoted text is unchanged. `:name`, `@p1` and other markers remain and therefore miss R1.
6. Replace a parenthesis whose entire content is one or more markers separated only by
   comma/whitespace—`(?)`, `(?, ?, ?)`, `($1,$2)`—with exactly `(?*)`. Do not collapse expressions
   or literals.
7. Serialize UTF-8. Comments and semicolons are rejected, never stripped.

This is not a general SQL parser. Anything outside the four exact statements either normalizes to
a different ID or fails closed.

### 11.2 Catalog ID framing

For canonical SQL bytes `C` and `n=byteLength(C)`:

```text
preimage = UTF8("F2E_SQL_CATALOG_ID_V1\n")
           || ASCII(decimal(n)) || UTF8(":") || C
catalogStatementId = lowercaseHex(SHA-256(preimage))
```

Decimal has no sign/leading zero except `0`. SQL raw and bind values are never hashed.

### 11.3 Exhaustive four-entry catalog

| Logical ID | Canonical SQL | Catalog ID |
| --- | --- | --- |
| `R1_RESERVA_BY_IDS_V1` | `SELECT r.id, r.estado, r.fecha, r.salon_id, r.instructor_id, r.tipo_actividad_id, r.hora_inicio, r.hora_fin, r.creado_en, r.actualizado_en FROM public.reserva r WHERE r.id IN (?*) ORDER BY r.id` | `dfa84c5db84f44c41adb82b0a58a2f080fc05a0612df15ababbd5dc064192a5b` |
| `R1_RESERVA_BY_SCOPE_V1` | `SELECT r.id, r.estado, r.fecha, r.salon_id, r.instructor_id, r.tipo_actividad_id, r.hora_inicio, r.hora_fin, r.creado_en, r.actualizado_en FROM public.reserva r WHERE r.salon_id IN (?*) AND r.fecha >= ? AND r.fecha <= ? ORDER BY r.id` | `c04cd40e2e3b991de845e73df65b6c9988be10c21cc1ee71474ab848b1e8eb9a` |
| `R1_TX_ISOLATION_V1` | `SELECT current_setting('transaction_isolation')` | `4a669a2f628e12468e0d532889e0bcafd38c09a5aadfb27f2f20f56159c1671e` |
| `R1_TX_READ_ONLY_V1` | `SELECT current_setting('transaction_read_only')` | `9963ea856cdf9bfb3e9c440b1c3a6c062fd375a906f465cbe7a7ac88878716c7` |

No `pg_current_snapshot()`, `SHOW`, `SET`, schema metadata, sequence SQL or other statement is
allowed in the reader window. Any normalization failure fails. Any normalized ID outside this
catalog fails, including unknown `SELECT`. Statement-class/denylist failure also fails even if an
ID matched. Failure is pre-execution `F2eSqlPolicyViolationException`.

### 11.4 SQL golden vectors

\n`, `\r` and `\t` below denote their single control byte in raw/framed test input.

```text
SQL-A raw:
"  SELECT\n r.id, r.estado, r.fecha, r.salon_id, r.instructor_id, r.tipo_actividad_id, r.hora_inicio, r.hora_fin, r.creado_en, r.actualizado_en\tFROM public.reserva r WHERE r.id IN (?, ?, ?) ORDER BY r.id  "
canonical: R1_RESERVA_BY_IDS_V1 canonical SQL
framed: "F2E_SQL_CATALOG_ID_V1\n193:" || canonical
SHA-256: dfa84c5db84f44c41adb82b0a58a2f080fc05a0612df15ababbd5dc064192a5b

SQL-B raw:
"SELECT r.id, r.estado, r.fecha, r.salon_id, r.instructor_id, r.tipo_actividad_id, r.hora_inicio, r.hora_fin, r.creado_en, r.actualizado_en FROM public.reserva r WHERE r.salon_id IN ($1,$2) AND r.fecha >= $3 AND r.fecha <= $4 ORDER BY r.id"
canonical: R1_RESERVA_BY_SCOPE_V1 canonical SQL
framed: "F2E_SQL_CATALOG_ID_V1\n233:" || canonical
SHA-256: c04cd40e2e3b991de845e73df65b6c9988be10c21cc1ee71474ab848b1e8eb9a

SQL-C raw:
"SELECT  r.id,\r\n\tr.estado, r.fecha, r.salon_id, r.instructor_id, r.tipo_actividad_id, r.hora_inicio, r.hora_fin, r.creado_en, r.actualizado_en FROM public.reserva r WHERE r.id IN (?1) ORDER BY r.id"
canonical/framed/hash: exactly SQL-A

SQL-D raw/canonical:
"SELECT r.id FROM public.reserva r ORDER BY r.id"
framed: "F2E_SQL_CATALOG_ID_V1\n47:SELECT r.id FROM public.reserva r ORDER BY r.id"
SHA-256: 8a4c3fcfd898385d6344056818c3f24628fb96f1acc1f2f9d327bb45371fec37
result: CATALOG_MISS / FAIL BEFORE EXECUTION
```

## 12. `F2E_CHECKSUM_*_V1`

### 12.1 Exact table, scope and columns

Checksum protects exactly `public.reserva`. The row scope is frozen before baseline:

```text
BY_RESERVATION_IDS -> rows whose id belongs to requested IDs
BY_SCOPE -> rows whose salon_id belongs to salon IDs and fecha BETWEEN desde AND hasta
```

All eleven persisted columns participate in this fixed order:

| # | Column | PostgreSQL/contract type | Tag |
| --- | --- | --- | --- |
| 1 | `id` | UUID | `U` |
| 2 | `salon_id` | UUID | `U` |
| 3 | `instructor_id` | UUID | `U` |
| 4 | `cliente_id` | UUID | `U` |
| 5 | `tipo_actividad_id` | UUID | `U` |
| 6 | `fecha` | DATE | `D` |
| 7 | `hora_inicio` | TIME | `T` |
| 8 | `hora_fin` | TIME | `T` |
| 9 | `estado` | TEXT/status | `S` |
| 10 | `creado_en` | TIMESTAMPTZ | `Z` |
| 11 | `actualizado_en` | TIMESTAMPTZ | `Z` |

`cliente_id` remains private inside checksum bytes only; it never reaches snapshot, provenance,
errors or normal reporting. The order is contract/V15 order, not reflection/result iteration.

### 12.2 Primitive and field canonicalization

Use the same `LP` and `SEQ` primitives fixed by section 6.1.1 and published design 36.8; checksum
domains remain `F2E_CHECKSUM_*_V1` and are not replaced by identity V2 domains. Canonical
tags/values:

| Tag | Value |
| --- | --- |
| `U` | lower-case RFC-4122 UUID |
| `S` | exact UTF-8 DB text, no trim/case fold/NFC/NFD |
| `D` | locale-free `uuuu-MM-dd` |
| `T` | `HH:mm:ss.SSSSSS`; exactly six fractional digits; sub-microsecond invalid |
| `Z` | UTC `uuuu-MM-dd'T'HH:mm:ss.SSSSSS'Z'`; exactly six digits; no default zone |
| `B` | ASCII `true`/`false` |
| `I` | base-10 integer, `-` only if negative, no `+`/padding |
| `Q` | plain exact decimal; no exponent/trailing fractional zeros/final dot; zero and negative zero=`0` |

`B`, `I`, `Q` are not applicable to R1 columns; they are defined only to close the shared V1
scheme. Float/double are forbidden.

```text
non-null field = SEQ("F2E_CHECKSUM_FIELD_V1", columnName, typeTag, "V", canonicalValue)
NULL field = SEQ("F2E_CHECKSUM_FIELD_V1", columnName, typeTag, "N", empty-byte-string)
```

All R1 columns are physically NOT NULL; null framing is a defensive/shared contract. Null is
different from empty string, literal `null`, zero and false.

### 12.3 Row/table/scope/slice formulas

```text
rowPreimage = SEQ("F2E_CHECKSUM_ROW_V1", ASCII(fieldCount), field1..fieldN)
rowHash = lowercaseHex(SHA-256(rowPreimage))
```

Rows sort by UUID PK 16 unsigned bytes ascending. Table identity does not enter row hash.

```text
tableIdentity = "public.reserva"
tablePreimage = SEQ("F2E_CHECKSUM_TABLE_V1", tableIdentity, ASCII(rowCount),
                    rowHash1..rowHashN)
tableHash = lowercaseHex(SHA-256(tablePreimage))
```

Row hashes are their 64 ASCII lower-case bytes in PK order. Empty table is
`SEQ("F2E_CHECKSUM_TABLE_V1","public.reserva","0")` with no implicit sentinel.

```text
IDs scope = SEQ("F2E_CHECKSUM_SCOPE_V1", "BY_RESERVATION_IDS",
                ASCII(idCount), sortedIds...)

range scope = SEQ("F2E_CHECKSUM_SCOPE_V1", "BY_SCOPE", ASCII(salonCount),
                  sortedSalonIds..., desde, hasta)
```

Dates use `D`; UUID sort is unsigned bytes.

```text
entry = SEQ("F2E_CHECKSUM_SLICE_TABLE_ENTRY_V1", tableIdentity, tableHash)
slicePreimage = SEQ("F2E_CHECKSUM_SLICE_V1", scopeIdentityBytes,
                    ASCII(tableCount), sortedEntries...)
sliceHash = lowercaseHex(SHA-256(slicePreimage))
```

Entries sort by unsigned UTF-8 bytes of table identity. R1 has one `public.reserva` entry even for
zero rows. General zero-table slice uses tableCount `0` and no entries. Version domains are
exactly `FIELD`, `ROW`, `TABLE`, `SCOPE`, `SLICE_TABLE_ENTRY`, `SLICE` as spelled above.

### 12.4 Checksum golden vectors

All expressions use exact UTF-8 `SEQ`/`LP` bytes.

**A — one R1 row**

```text
id=U:00000000-0000-0000-0000-000000000001
salon_id=U:00000000-0000-0000-0000-000000000002
instructor_id=U:00000000-0000-0000-0000-000000000003
cliente_id=U:00000000-0000-0000-0000-000000000004
tipo_actividad_id=U:00000000-0000-0000-0000-000000000005
fecha=D:2026-09-02
hora_inicio=T:08:30:00.000000
hora_fin=T:09:15:30.123456
estado=S:CONFIRMADA
creado_en=Z:2026-09-02T14:00:00.000000Z
actualizado_en=Z:2026-09-02T14:05:06.000007Z
rowPreimage=SEQ("F2E_CHECKSUM_ROW_V1","11",FIELD(id)..FIELD(actualizado_en))
rowHash=7f2438b5698a3d3ed9aec57674c64834da4aba252c08828768dbd148fa60006c
```

**B — framing collision is prevented**

```text
ROW("ab","c") preimage =
4:19:F2E_CHECKSUM_ROW_V11:242:5:21:F2E_CHECKSUM_FIELD_V14:left1:S1:V2:ab42:5:21:F2E_CHECKSUM_FIELD_V15:right1:S1:V1:c
hash=f3a481c27a51936e546068c1d24bf8014fbcb6b03e9fc44a4004b49d1bea8c95

ROW("a","bc") preimage =
4:19:F2E_CHECKSUM_ROW_V11:241:5:21:F2E_CHECKSUM_FIELD_V14:left1:S1:V1:a43:5:21:F2E_CHECKSUM_FIELD_V15:right1:S1:V2:bc
hash=7b3ae77aa7da771cc416c0863408ead4f483d21d35ba3a47d06155e05fe12ae1
```

**C — table with row A**

```text
preimage=4:21:F2E_CHECKSUM_TABLE_V114:public.reserva1:164:7f2438b5698a3d3ed9aec57674c64834da4aba252c08828768dbd148fa60006c
tableHash=da4cad572663f11e0d5c8a4b6265729465089e13e940924f3958acac41e6ad1b
```

**D — same row hash, different table identity**

```text
public.reserva -> da4cad572663f11e0d5c8a4b6265729465089e13e940924f3958acac41e6ad1b
audit.reserva_shadow preimage=
4:21:F2E_CHECKSUM_TABLE_V120:audit.reserva_shadow1:164:7f2438b5698a3d3ed9aec57674c64834da4aba252c08828768dbd148fa60006c
audit tableHash=71b3084caaf55b7ad11c58f3f513a98228e6bce0535466cc11f7d96be6ec2278
```

**E — two-row ordering**

Row A0 differs from A only in `id=...0000`, `fecha=2026-09-01`,
`hora_inicio=07:00:00.000000`, `hora_fin=08:00:00.000000`.

```text
A0 rowHash=ea5f926c634840690be3087662db5dab08f446323378b075037e6e59e318f3e0
input [A,A0] or [A0,A]
canonical PK order [A0,A]
table preimage=
5:21:F2E_CHECKSUM_TABLE_V114:public.reserva1:264:ea5f926c634840690be3087662db5dab08f446323378b075037e6e59e318f3e064:7f2438b5698a3d3ed9aec57674c64834da4aba252c08828768dbd148fa60006c
tableHash=ee3953739cf5633cfe8869659be5b4d7075a7c74beb80be22dab90ae15eecedc
```

**F — R1 IDs slice with table C**

```text
scope=4:21:F2E_CHECKSUM_SCOPE_V118:BY_RESERVATION_IDS1:136:00000000-0000-0000-0000-000000000001
slice preimage=
4:21:F2E_CHECKSUM_SLICE_V189:4:21:F2E_CHECKSUM_SCOPE_V118:BY_RESERVATION_IDS1:136:00000000-0000-0000-0000-0000000000011:1122:3:33:F2E_CHECKSUM_SLICE_TABLE_ENTRY_V114:public.reserva64:da4cad572663f11e0d5c8a4b6265729465089e13e940924f3958acac41e6ad1b
sliceHash=ff619ecac74d86a149cc9110c9ac205990fc5a87eb10e61ba64c5b1a6967f5be
```

**G — same table hash, distinct scopes**

```text
scope A=SEQ("F2E_CHECKSUM_SCOPE_V1","BY_SCOPE","1",
            "00000000-0000-0000-0000-000000000002","2026-09-01","2026-09-02")
sliceHash A=6b8c91ac4bb06fed2476d190b93e37a8cd75c2485a66033801bf01676e1f0b79

scope B=SEQ("F2E_CHECKSUM_SCOPE_V1","BY_SCOPE","1",
            "00000000-0000-0000-0000-000000000002","2026-09-02","2026-09-02")
sliceHash B=7b79a1f2403d6e63e9a012fdda6a4da39c422f5c559df2d5f38c61bd0c4b0db5
```

**Empty vectors**

```text
empty public.reserva preimage=3:21:F2E_CHECKSUM_TABLE_V114:public.reserva1:0
empty public.reserva tableHash=7cd08818f587c66f33716592705b5d716a19fd1ac434cb65e0defce21abeb916
empty zero-table slice for scope F=775d6bf38eb09356818f772684ab50e4d0c3c3776e623c21f179a8f5368a8625
```

**H — complete multi-table ordering vector**

```text
scopeType=BY_RESERVATION_IDS
scopeInputs.idCount=1
scopeInputs.ids=[00000000-0000-0000-0000-000000000001]
scopeIdentity=SEQ("F2E_CHECKSUM_SCOPE_V1","BY_RESERVATION_IDS","1",
                  "00000000-0000-0000-0000-000000000001")
scopeIdentity bytes=
4:21:F2E_CHECKSUM_SCOPE_V118:BY_RESERVATION_IDS1:136:00000000-0000-0000-0000-000000000001

public.reserva:
  tableHash=da4cad572663f11e0d5c8a4b6265729465089e13e940924f3958acac41e6ad1b
  entry bytes=
  3:33:F2E_CHECKSUM_SLICE_TABLE_ENTRY_V114:public.reserva64:da4cad572663f11e0d5c8a4b6265729465089e13e940924f3958acac41e6ad1b

audit.reserva_shadow:
  tableHash=71b3084caaf55b7ad11c58f3f513a98228e6bce0535466cc11f7d96be6ec2278
  entry bytes=
  3:33:F2E_CHECKSUM_SLICE_TABLE_ENTRY_V120:audit.reserva_shadow64:71b3084caaf55b7ad11c58f3f513a98228e6bce0535466cc11f7d96be6ec2278

permutation A=[public.reserva,audit.reserva_shadow]
permutation B=[audit.reserva_shadow,public.reserva]
canonical unsigned UTF-8 table order for both=[audit.reserva_shadow,public.reserva]
tableCount=ASCII("2")

slicePreimage=SEQ("F2E_CHECKSUM_SLICE_V1",scopeIdentityBytes,"2",
  ENTRY("audit.reserva_shadow",
        "71b3084caaf55b7ad11c58f3f513a98228e6bce0535466cc11f7d96be6ec2278"),
  ENTRY("public.reserva",
        "da4cad572663f11e0d5c8a4b6265729465089e13e940924f3958acac41e6ad1b"))

slicePreimage bytes=
5:21:F2E_CHECKSUM_SLICE_V189:4:21:F2E_CHECKSUM_SCOPE_V118:BY_RESERVATION_IDS1:136:00000000-0000-0000-0000-0000000000011:2128:3:33:F2E_CHECKSUM_SLICE_TABLE_ENTRY_V120:audit.reserva_shadow64:71b3084caaf55b7ad11c58f3f513a98228e6bce0535466cc11f7d96be6ec2278122:3:33:F2E_CHECKSUM_SLICE_TABLE_ENTRY_V114:public.reserva64:da4cad572663f11e0d5c8a4b6265729465089e13e940924f3958acac41e6ad1b

expected SHA-256 for both permutations=
aa10c3ce64e25734e671c9bc9e91555a714655036cf03b27425e88e6c67b99a7
```

The corrective-design audit recomputed 18/18 documented SHA-256 values with zero mismatches. That
is documentary authority/evidence only. R1 tests must independently calculate their own vectors;
they may not treat the review result as technical execution.

## 13. No-write integration gate

Use PostgreSQL `16-alpine`, real Flyway-compatible schema and `ddl-auto=validate`; H2 is forbidden.
The exact order is:

1. privileged container/Flyway/fixture setup and commit;
2. create/grant SELECT-only reader role;
3. resolve/freeze scope PKs and calculate baseline checksum/count using privileged observer;
4. on a clean reader-role connection, after a negative `INSERT` control was denied on a separate
   reader-role transaction, execute reader through the proxied harness;
5. commit/close reader transaction with no cleanup;
6. while fixture remains quiescent, calculate final checksum/count via privileged observer;
7. compare hashes/counts and inspect captured SQL/statistics;
8. clean up privileged resources outside the measured window.

No concurrent database writer runs in this checksum test. A concurrent-writer/R6 latch test is not
required for single-statement R1; the registry concurrency tests in section 14 are mandatory and
are a different concern. Rollback is not proof of no write. Hibernate statistics are only
corroboration. Required proof is exact SQL policy + actual SELECT-only credential + before/after
scoped checksum + narrow architecture.

HostValidator is required for this integration gate. Docker/container/network/Flyway/JPA startup
failure before semantic evidence is `PRE_SEMANTIC / ENVIRONMENT_FAILURE` or
`HOST_VALIDATION_BLOCKED`; it is not fabricated P0/P1/P2 and not `SOURCE_ACCESS_FAILURE`.

## 14. Required future tests

The exact allowlisted tests must cover at least:

A. exhaustive production/test allowlists, no modified tracked production, dependency matrix and
   no forbidden tokens/imports;
B. both native PostgreSQL queries, exact aliases/types, named typed binding, null/empty rejection
   before SQL, ordering/cardinality and no full-table fallback;
C. mapper state/null/range validation and exact immutable mapping;
D. historical target always empty and no inference;
E. `F2E_IDENTITY_V2`, both canonical scopes, exact snapshot evidence and statement observation,
   four independent golden vectors and the 1210-byte canonical projection;
F. all four failure codes, every trigger, stable messages, exhaustive safe context, PII/SQL
   exclusion, causes and all-or-nothing output;
G. all four policy reasons, direct and Hibernate-wrapped same-instance propagation, no
   `ReservationReadException`, and JDBC evidence rejected unknown SQL/unknown SELECT was not
   prepared/executed;
H. privileged-plane versus SELECT-only reader-plane isolation and denied negative write control;
I. exact `DescriptorRecursoLector`; correct DS/EMF/TM/shared-EM identities and active
   Session/physical Connection metadata succeed; caller source/schema override and every wrong
   DS/EMF/TM/EM/Session/Connection/JDBC URL/database/schema/principal fail before probes/SQL with
   zero accepted output;
J. real Spring proxies: reader explicitly names the same `f2eReaderTransactionManager`, reader
   outside TX fails `MANDATORY`, and harness `REQUIRES_NEW + READ_COMMITTED + readOnly` succeeds;
   no default/privileged manager, self-invocation or manual-new proof;
K. `F2E_SQL_CANON_V1`, catalog framing, four exact catalog IDs and SQL-A–D;
L. unknown SELECT and all other unknown SQL fail before execution;
M. checksum field/row/table/scope/entry/slice formulas and vectors A–H plus empty vectors;
N. baseline/final `public.reserva` scoped checksums and row counts identical;
O. actual SELECT-only login/grants, reader datasource credentials, and zero DML/DDL/lock/sequence
   statement;
P. no stereotypes/product bean/config/property/consumer; default and prod contexts expose no R1
   bean; test-only config is explicit;
Q. `SINGLE_READER_TEST` accepted and any `MULTI_READER_MVCC`/foreign claim rejected before
   registry/resource/evidence/probes/SQL; wrong isolation/read-only state rejected;
R. exact statement manifest: isolation probe, read-only probe, one operation-specific data SELECT;
   missing/extra/out-of-order statement and additional probe rejected;
S. same shared EntityManager, joined Session and physical Connection reference before/after;
   secondary EM/Session/Connection and `DataSource.getConnection`/`DriverManager`/`JdbcTemplate`
   escape rejected;
T. exact 32-key provenance, trusted descriptor-owned labels, typed scope/catalog/row/identity
   cross-consistency, recompute mismatch and digest collision all fail with zero partial output;
U. all four uniqueness domains and concurrent duplicate races; one linearizable all-or-none
   reservation, no partial key insertion and only one competing success;
V. success, abort before/after identity construction, rollback, post-publication failure,
   `UNKNOWN`/interruption and release semantics preserve the exact consumed/history states;
W. retry after abort requires a new `attemptIdentity`; consumed keys remain non-reusable within
   the context, and closing/recreating `ApplicationContext` ends/creates the namespace.

R1 needs no `REPEATABLE_READ`, concurrent-writer latch or R6 test. The future implementation may
be submitted only to a fresh independent technical audit after HostValidator and all applicable
targeted tests pass. The executor/corrector cannot self-audit.

## 15. Runtime, transaction and deferred boundaries

Production R1 classes have no `@Component`, `@Service`, `@Repository`, `@Configuration` or
autodetection. There is no production bean, property, profile, endpoint, scheduler, listener,
runner or consumer. Test-only DI is the sole R1 reachability in this slice.

`ReservaJpaReader` does not own a transaction, retry, isolation upgrade or report. It participates
through the explicit named `f2eReaderTransactionManager` via `MANDATORY + readOnly`. R1 standalone uses the test harness
`REQUIRES_NEW + READ_COMMITTED + readOnly`; it makes no multi-reader/SAME_LOGICAL_SNAPSHOT claim.

```text
R2 / R3 / R4 / R5 / R6: NOT_AUTHORIZED
Data source: DATA_SOURCE_NOT_AVAILABLE
Material data audit: NOT_PERFORMED / NOT_AUTHORIZED
D08: DEFERRED
Crosswalk / mapping selection / Resolver / Fence: NOT_AUTHORIZED
Migration / normalization / backfill: NOT_AUTHORIZED
MIGRANDO: NO
NUEVA: NO
Cutover: false
TurnoInstructor: PRODUCTIVE AUTHORITY
R1: DARK_LAUNCH / NOT_PRODUCTIVE
```

## 16. Finding traceability and current disposition

The earlier P1-1–P1-4 handoff corrections remain historical inputs. This correction addresses only
the four findings from the immediately preceding implementation-authorization audit:

| Finding | Correction materialized in repository authority | Status after this corrector |
| --- | --- | --- |
| `F2E-R1-IA-01` lifecycle authority | residual design audit/publication closure persisted in `ESTADO-ACTUAL` and one evidence-only review; R1 remains pending | `MATERIALIZED / PENDING FRESH HANDOFF AUDIT` |
| `F2E-R1-IA-02` stale identity/provenance | historical V1 explicitly non-normative; final V2 context, formulas, descriptor ownership, vectors and cross-consistency are sole authority | `MATERIALIZED / PENDING FRESH HANDOFF AUDIT` |
| `F2E-R1-IA-03` resource/JPA topology | descriptor, explicit reader TM, shared EM/Session/Connection proof, ordered manifest and failure ordering mapped | `MATERIALIZED / PENDING FRESH HANDOFF AUDIT` |
| `F2E-R1-IA-04` uniqueness lifecycle | one harness-owned registry, four keys, atomic reservation, terminal states, retry/release/context boundary and tests mapped | `MATERIALIZED / PENDING FRESH HANDOFF AUDIT` |

This corrector does not close or audit these findings. Only a later fresh independent handoff
auditor may decide `CLOSED/OPEN` and recommend a separate approval/activation.

```text
RESIDUAL DESIGN: FRESH AUDIT PASS / PUBLISHED / CLOSED
HANDOFF DOCUMENT CORRECTION: MATERIALIZED
HANDOFF: CORRECTED_TO_FINAL_V2_DESIGN_AUTHORITY
HANDOFF: READY_FOR_FRESH_INDEPENDENT_HANDOFF_DOCUMENT_AUDIT
HANDOFF: NOT_APPROVED / NOT_ACTIVE
TARGET: NOT_STARTED
IMPLEMENTATION: NOT_EXECUTED / NOT_AUTHORIZED
R2-R6: NOT_AUTHORIZED
OPEN QUESTIONS IN AUTHORIZED CORRECTION SCOPE: NINGUNA
HUMAN DECISION REQUIRED FOR CORRECTION: NO
```

No Maven, Docker, HostValidator, tests, compilation, SQL, DB or data audit is run by this document
correction. No code, test, Spring configuration, migration or Git history is modified. No
`git add`, commit or push is authorized.
