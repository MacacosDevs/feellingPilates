# FeelingPilates — Handoff F2E / diseño de adapters read-only y consistencia de snapshot

Handoff status: `HANDOFF_MATERIALIZED / APPROVED / ACTIVE / AUTHORIZED_FOR_DESIGN_RESEARCH / TARGET_AUTHORIZED_TO_START / TARGET_NOT_STARTED`

Target unit: `F2E / boundary de readers JPA hacia detector puro`

Type: `DESIGN / RESEARCH`

Role that materialized this handoff: `DOCUMENTER`

This handoff is approved and active exclusively for the future design/research unit described below.
Its fresh, independent handoff-document audit is persisted in
`auditoria/reviews/HANDOFF-F2E-DISENO-ADAPTERS-READ-ONLY-SNAPSHOT-CONSISTENCY-REVIEW.md`.
Activation authorizes the target to start; it is not a design result, checkpoint, design-gate PASS,
implementation authorization, or technical gate.

## 1. Authority, derivation, and pre-flight

Repository authority is the repository, its canonical documents, and physical evidence; chat is coordination only. This handoff was materialized after this physical pre-flight:

```text
Repository: /Users/jesusaldaircruzortiz/Desktop/Feelingpilates/feelingpilates
Branch: operacion/excepciones-horario-fecha
HEAD: 823b320951eca290136b377d71ba3090b8108f20
Initial working tree: CLEAN
Initial staging: VACIO
Active handoff before materialization: NINGUNO
```

The future design agent and its fresh document auditor must repeat the physical pre-flight. A mismatch of branch, expected authority, active handoff, or a material canonical/physical contradiction is fail-closed: stop; do not reconcile, normalize, or infer a replacement scope.

The future unit must read in full before making a design conclusion:

- `AGENTS.md` and `auditoria/orquestacion/{README,WORKFLOW,STATE-MACHINE,GATES,ROLES}.md`;
- all applicable canons: `ESTADO-ACTUAL.md`, `ARQUITECTURA-ACTUAL.md`, `DECISIONES-ARQUITECTONICAS.md`, `README-REESTRUCTURACION.md`, `REGLAS-DE-TRABAJO-IA.md`, `contexto/DOMINIO-FUNCIONAL.md`, and `contexto/MAPA-LEGACY-Y-MIGRACION.md`;
- F2E preparation, detector design, the pure-core handoff, handoff review, technical implementation review, and their competent reviews;
- F2D.1 design/final review and F2D.2 materialization/documentary review;
- relevant repositories, entities, services, transaction annotations/configuration, PostgreSQL/Flyway configuration, and schema/migrations.

The status to preserve is:

```text
F2D.1: CLOSED / PASS
F2D.2: CLOSED / DARK_LAUNCH / NOT_PRODUCTIVE
F2E.1 preparation: CLOSED / PASS
F2E identity/semantic detector design: CLOSED / PASS
Pure detector core: IMPLEMENTATION CLOSED / TECHNICAL IMPLEMENTATION GATE PASS
Pure-core applicable tests: 37/37 PASS
Adapters: NOT_IMPLEMENTED
Active handoff before this activation: NINGUNO
Data source: DATA_SOURCE_NOT_AVAILABLE
Data audit: NOT_PERFORMED
Persisted crosswalk: NOT_AUTHORIZED
Resolver: NOT_AUTHORIZED
D08: DEFERRED
Migration: NOT_AUTHORIZED
Cutover: false
Runtime: DARK_LAUNCH
Productive: NOT_PRODUCTIVE
Productive authority: TurnoInstructor / LEGACY_VIVO / PRODUCTIVO
```

`HANDOFF_MATERIALIZED != APPROVED != ACTIVE != TARGET_DESIGN_COMPLETE`. Design is not implementation; materialization, publication, runtime, productive authority, and cutover remain independent axes.

## 2. Exact future purpose and allowlist

If this handoff is independently audited, approved, and explicitly activated, it authorizes only one future intervention: a `DESIGN / RESEARCH` checkpoint that closes the design debt at the JPA-reader-to-pure-detector boundary.

That checkpoint must determine enough for subsequent implementation slices to proceed without inventing queries/projections, snapshot semantics, transaction behavior, managed/detached behavior, immutable mappings, isolation, non-mutation evidence, HostValidator applicability, or slice order.

The future unit is documentation/research only. Its sole physical output is exactly one checkpoint document, unless the canonical protocol itself requires another documentary artifact. It must not create Java, tests, repositories, projections, SQL, Flyway, schema/data changes, connections to a database, a data audit, a crosswalk, resolver, fence, migration, cutover, or authority change.

For this target design unit:

```text
HOST_VALIDATION: NOT_REQUIRED
DB connection or query execution: FORBIDDEN
Data source assumption: FORBIDDEN
Implementation of adapters: FORBIDDEN
Runtime/consumer wiring: FORBIDDEN
```

## 3. Physical evidence to use, not silently generalize

The future checkpoint must distinguish what is physically demonstrated from what remains a design decision.

| Source | Current physical evidence | Consequence for the future design |
| --- | --- | --- |
| `ReservaRepository` / `Reserva` | Reader methods return `Reserva` entities. `Reserva` holds `salon`, `instructor`, `cliente`, and `tipoActividad` as `LAZY` `ManyToOne`; it also has id, date, time range, and state. | A future reader needs an explicit query/projection and an in-transaction extraction strategy. Passing `Reserva` outside an adapter is forbidden. |
| `TurnoInstructorRepository` / `TurnoInstructor` | Repository returns entities. The entity has current type, active flag, salon, date/day/range, and `LAZY` instructors/assignments. | It can support a current observation only; it does not prove functional history. |
| `TurnoInstructorAsignacionRepository` | Exists as a `JpaRepository` and exposes a writer operation. | Its existence does not authorize use as a reader contract or a mutation. |
| `AsignacionRepository` / `ProgramacionNominal` | `buscarNominalesDeFecha` is already a native projection for active, date-valid recurrente rows; `ProgramacionNominal` maps it to nominal occurrences. | The design must assess its field sufficiency and whether nominal rows alone form the final candidate universe. |
| `AjusteProgramacionFechaRepository` / `AjusteProgramacionFecha` | Current entity/read methods cover active date adjustments. Adjustment type is `CANCELACION`, `REEMPLAZO`, or `ADICION`; new-model identity/semantics are already constrained by F2D. | The design must specify read evidence by type without inferring a legacy mapping or persisting one. |
| `ProgramacionEfectiva` | Internally composes nominales, active adjustments, and fail-closed validation into current effective occurrences. | Candidate-universe design must analyze this composition, the final salon, and failure/absence provenance. |
| detector pure core | `ReservationSourceSnapshot`, `ProgrammingCandidateSnapshot`, provenance, historical target snapshot, and immutable result contracts exist under `com.feelingpilates.transicion.programacion.detector`. | Future adapters terminate by producing these supported immutable snapshots or an explicitly equivalent mapping; managed JPA entities never cross into the core. |

`@Transactional(readOnly = true)` is present in existing services, including `ProgramacionNominal` and `ProgramacionEfectiva`. This is evidence of local usage only, not evidence that a multi-query logical snapshot is consistent, that a query is projection-safe, or that no managed entity can be dirtied.

## 4. Design debt that the future checkpoint must close

The future checkpoint must explicitly resolve, with repository-backed evidence and a fail-closed disposition where evidence is insufficient:

1. exact fields needed by every reader;
2. exact query/projection contract for each field set;
3. evidence/provenance retained in every snapshot;
4. which facts remain `UNKNOWN`, `UNKNOWN_HISTORY`, `UNSUPPORTED`, `MISSING`, or otherwise fail-closed;
5. the lifetime of a managed JPA entity and the exact in-transaction mapping point to an immutable snapshot;
6. all lazy relationships needed by the mapping and how lazy loading is prevented after the boundary;
7. transaction scope, isolation level, read combinations sharing a logical snapshot, and inconsistency handling;
8. a no-write/no-mutation strategy with objectively testable evidence;
9. package/dependency/wiring strategy that establishes dark-launch runtime isolation;
10. the minimum ordered future implementation slices, their HostValidator needs, and the conditions for any coordinator.

No future executor may fill an unresolved item with a convenient implementation assumption.

## 5. Query and projection contract to design, not implement

For every source reader, the future design must make a source-by-source table that contains:

- source and physical repository/entity/service examined;
- required output snapshot type and exact field list;
- exact query or projection shape that supplies each field, including associations and ordering where material;
- explicit provenance/fingerprint/evidence fields;
- nullability and shape validation;
- observable facts versus `UNKNOWN`/`UNSUPPORTED` facts;
- source-specific failure and fail-closed behavior;
- proof that no managed entity leaks to the pure core.

The design must not implement a repository method, JPQL, native SQL, DTO, interface projection, or query annotation. It must decide the future contract only after checking the physical schema/Flyway and the current mappings.

## 6. Reader-specific design obligations

### 6.1 Reserva reader

Design production of `ReservationSourceSnapshot` from current legacy evidence. It must preserve at least:

```text
reserva.id
fecha
horaInicio/horaFin as the reserved half-open subinterval
salon
instructor
actividad
estado
source fingerprint / provenance / any required observable evidence
```

`reservation_identity = reserva.id` remains distinct from a programming target identity and from the reserved interval. The design must not invent `HistoricalProgrammingTargetSnapshot` when no historical target is persisted/demonstrated. It must document how state and related ids are captured without exposing `Reserva` or lazy proxies beyond the adapter boundary.

### 6.2 TurnoInstructor reader

Design immutable legacy snapshots from `TurnoInstructor` and, where required, its instructor/activity assignment evidence. Preserve current observable type, active state, salon, date/day, range, instructors/activities, and technical timestamps only as evidence if physically available.

The only affirmative history posture without additional persisted evidence is `CURRENT_SNAPSHOT_ONLY`. A claim requiring functional legacy history must become `UNKNOWN_HISTORY` plus the approved unsupported/blocking behavior. `creado_en`, `actualizado_en`, IDs, query ordering, or technical timestamps must never be treated as `vigenteDesde`/`vigenteHasta` or as reconstructed functional validity.

### 6.3 New-programming reader

Design the correct candidate-occurrence universe. It must not equate `AsignacionRepository.buscarNominalesDeFecha` alone with the final universe. The design must analyze and document the relationship among:

```text
ProgramacionNominal
→ AjusteProgramacionFecha
→ ProgramacionEfectiva
→ final salon operational/master-data fail-closed validation
```

It must establish whether the detector needs nominal candidates, effective candidates, both with provenance, or distinct universes by claim. It must preserve F2D ordering and semantics: nominales first, adjustments, results, final-salon operational/master-data/invariant validation, then filtering/order. It must not silently filter before adjustment, flatten `CANCELACION`/`REEMPLAZO`/`ADICION`, or use an absence with no provenance as a demonstrated outcome.

### 6.4 Adjustment reader

Design read-only evidence for each new adjustment form:

```text
CANCELACION
REEMPLAZO
ADICION
```

For each, record the observed type, date, active status, target/result fields, source identity, provenance, and the relation to nominal/effective evidence needed by the detector. Preserve approved new semantics: targeted cancellation/replacement uses the F2D target identity; addition has its own identity. Do not infer a legacy mapping from a similarly shaped legacy `EXCEPCION` or `CANCELACION`, and do not persist any mapping.

### 6.5 Coordinator

Initial status is `NOT_NEEDED_YET`. A coordinator is not authorized for implementation. The design may keep it deferred or define conditions under which it becomes necessary, but it must not claim one is unnecessary until it has resolved the shared logical snapshot contract for all sources it would combine.

## 7. Managed-entity, lazy-loading, and immutable-boundary contract

The checkpoint must choose and justify the future approach from evidence, rather than prescribing one here. It must decide:

- where the transaction starts and ends for every adapter/composition read;
- whether data is obtained through a projection, DTO mapping, explicit in-transaction entity mapping, or another supported approach;
- exactly which associations are fetched/extracted while managed;
- how an adapter prevents a lazy proxy/entity from reaching detector packages;
- where the immutable snapshot is fully constructed, validated, fingerprinted, and defensively copied;
- how it proves the pure core receives only supported immutable snapshots;
- how it prevents accidental dirty checking, flushes, writers, and repository mutations.

No managed entity from JPA may cross into `com.feelingpilates.transicion.programacion.detector`. A detached entity is not by itself a safe detector input: the design must explain why the retained data is immutable, complete, and free of post-boundary lazy loading. A `readOnly=true` transaction alone is insufficient proof of non-mutation.

## 8. Logical snapshot consistency and transaction decision

This is a mandatory design decision. A future evaluation may require reads of `Reserva`, `TurnoInstructor`, nominal/new programming, effective programming, and adjustments. The checkpoint must define what a reproducible “same logical snapshot” means for each combination and which timestamps/fingerprints/provenance make that claim auditable.

It must physically inspect the applicable Spring transaction configuration, datasource/PostgreSQL configuration, Flyway/schema constraints, and transaction annotations before choosing isolation. It must explicitly record and contrast the PostgreSQL behavior that, under `READ COMMITTED`, multiple statements in one transaction can observe different statement-level snapshots. It must not claim the configured isolation until that configuration is physically confirmed.

The future decision must state:

- transaction owner and boundary;
- desired/required isolation and why;
- the source combinations that require one common snapshot;
- whether a single query, one transaction, stronger isolation, retry/version validation, or another evidenced strategy is necessary;
- which source mutations or interleavings can invalidate an evaluation;
- the reproducible inconsistency detection and fail-closed result when a sufficient snapshot cannot be obtained;
- how detector provenance differentiates a semantic unsupported result from an operational/environmental consistency failure.

No transaction is opened, no isolation is configured, and no PostgreSQL query is executed by this handoff or its future design-only target.

## 9. Non-mutation and runtime isolation design

The future checkpoint must define objective evidence for all of the following:

```text
No INSERT
No UPDATE
No DELETE
No save / persist / merge / delete
No flush of dirty managed state
No mutation of a managed entity
No writer invocation
No reader/consumer/authority switch
```

It may evaluate projections, DTO/snapshot construction, transaction hints, detach handling, SQL capture, repository/integration tests, and architecture tests, but must not declare one technique sufficient without physical evidence. The later first JPA implementation must verify these claims against real SQL/projections and PostgreSQL behavior.

The design also must select a demonstrable dark-launch isolation strategy. A Spring bean can be runtime-reachable even if called “internal”; bean creation is not dark-launch isolation. The checkpoint must decide package/dependency direction, bean/wiring policy, consumer boundary, feature isolation, or an equivalent enforceable strategy that prevents productive consumption.

Until separately authorized, later implementation slices are prohibited from adding or switching:

```text
controller / endpoint
scheduler
event listener
startup runner
ReservaService integration
reader switching
writer switching
productive consumer
frontend/mobile integration
```

## 10. Future validation and HostValidator plan

This design unit does not run tests and has `HOST_VALIDATION=NOT_REQUIRED`. The first future JPA implementation has `HOST_VALIDATION=REQUIRED` unless a later, evidence-backed design audit changes that classification.

The checkpoint must distinguish and specify a later plan for:

1. pure mapping/unit tests;
2. repository/projection contract tests;
3. JPA integration tests;
4. transaction/isolation and logical-snapshot consistency tests;
5. non-mutation/no-write/dirty-checking tests;
6. architecture and runtime-isolation tests;
7. PostgreSQL/Testcontainers tests and the deterministic HostValidator plan/prerequisites.

For the first JPA slice, HostValidator scope must include real SQL/projection behavior, transaction/isolation behavior, PostgreSQL behavior, absence of writes, and Testcontainers/host prerequisites. `@Transactional(readOnly=true)` alone cannot count as no-write evidence.

## 11. Future implementation slicing

The future checkpoint must determine the minimal order, dependencies, and isolated test plan for implementation slices. It must consider at least:

1. Reserva reader;
2. TurnoInstructor reader;
3. new-programming reader;
4. adjustment reader;
5. composition/coordinator, only if its conditions are met.

It must not presume one combined implementation. Each proposed slice must state its inputs/outputs, transactions, snapshot dependencies, immutable boundary, no-write proof, runtime-isolation proof, HostValidator classification, and what remains unavailable to the next slice.

## 12. Data, migration, authority, and other exclusions

```text
Data source: DATA_SOURCE_NOT_AVAILABLE
Material data audit: NOT_PERFORMED / FORBIDDEN
Persisted crosswalk: NOT_AUTHORIZED
Resolver: NOT_AUTHORIZED
D08/fence/cohort/enforcement: DEFERRED / NOT_AUTHORIZED
Migration/backfill/normalization: NOT_AUTHORIZED
MIGRANDO: NO
NUEVA: NO
Cutover: false
Authority change: FORBIDDEN
TurnoInstructor productive authority: PRESERVED
DARK_LAUNCH: PRESERVED
NOT_PRODUCTIVE: PRESERVED
```

Lack of a data source does not block the design. It does block a material data audit. The design must list remaining data-audit prerequisites:

- named and authorized data source;
- SELECT-only credentials and evidence;
- schema/Flyway fingerprint;
- coherent logical-snapshot semantics;
- approved readers;
- immutable, nonproductive report mechanism.

The detector may produce candidates in a later authorized dark-launch scope, but a persisted crosswalk remains a blocker for a resolver, migration, and cutover when those later scopes arise.

## 13. Entry and exit conditions for the future design target

Before activation, all of the following must be freshly verified:

```text
Pure core: CLOSED / PASS
Technical core gate: PASS
F2E design: CLOSED / PASS
F2E.1: CLOSED / PASS
Git: CLEAN
Active handoff before activation: NINGUNO
TurnoInstructor: PRODUCTIVE AUTHORITY
DARK_LAUNCH: SI
NOT_PRODUCTIVE: SI
D08: DEFERRED
Migration: NOT_AUTHORIZED
Cutover: false
Data source: NOT assumed
This handoff: independently audited, approved, and active for this exact design target
```

The target design can close only when its single checkpoint explicitly closes, with evidence and an independent fresh design audit, at least:

1. reader inventory;
2. exact source fields;
3. query/projection contracts;
4. immutable snapshot mappings;
5. managed-entity boundary;
6. lazy-loading boundary;
7. transaction boundary;
8. logical snapshot-consistency semantics;
9. isolation decision and justification;
10. fail-closed inconsistency behavior;
11. no-write/non-mutation design;
12. runtime-isolation design;
13. package/dependency direction;
14. reader slicing;
15. coordinator deferral or conditions;
16. future test strategy;
17. HostValidator applicability;
18. data-audit prerequisites;
19. forbidden scope;
20. future handoff candidates.

Its gate is `FRESH_INDEPENDENT_DESIGN_DOCUMENT_AUDIT`, with `P0=0` and `P1=0`. This handoff neither performs nor pre-approves that audit.

## 14. Lifecycle, activation, and next gate

```text
Handoff materialized: YES
Fresh independent handoff-document audit: PASS
Audit artifact: auditoria/reviews/HANDOFF-F2E-DISENO-ADAPTERS-READ-ONLY-SNAPSHOT-CONSISTENCY-REVIEW.md
Audited lifecycle before approval/activation: HANDOFF_MATERIALIZED / READY_FOR_FRESH_INDEPENDENT_HANDOFF_DOCUMENT_AUDIT / NOT_APPROVED / NOT_ACTIVE / TARGET_NOT_STARTED
Handoff approved: YES
Handoff active: YES
Authorization: AUTHORIZED_FOR_DESIGN_RESEARCH
Target design started: NO
Target design materialized: NO
Target design authorized now: YES — AUTHORIZED_TO_START
Design checkpoint: NOT_CREATED / PENDING
Design gate: PENDING / NOT_PERFORMED
Fresh target design audit: NOT_PERFORMED
Review created by this activation: YES
ESTADO-ACTUAL updated: YES
Next step: EXECUTE_ACTIVE_DESIGN_RESEARCH_HANDOFF
```

`ACTIVE` means only that the target `DESIGN / RESEARCH` unit may start. It does not mean that the
design was executed, a checkpoint was created, queries/projections or transaction/isolation were
decided, the design gate passed, adapters were implemented, DB access was authorized, or a data
audit was authorized. Snapshot consistency remains `IN_SCOPE / PENDING`; no isolation,
projection strategy, transaction shape, or detach strategy is decided by this activation.

The maximum disposition of this activation is:

```text
AUDIT PERSISTED
HANDOFF APPROVED / ACTIVE MATERIALIZED
READY FOR FRESH ACTIVATION AUDIT
```

No `git add`, commit, push, target execution, implementation, DB connection, data audit, authority
update, or lifecycle result beyond this approval/activation is authorized by this intervention.
