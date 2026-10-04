# F2E R4 — self-contained reader implementation handoff, current clean main

Status at capture: MATERIALIZED_CANDIDATE / NOT_ACTIVE / NOT_AUTHORIZED_TO_IMPLEMENT.
Base: `d4fb4428d7251160cef4e90fd0da37192e0ae0cd`; current source of truth is canonical main, not historical allowlists.
Scope of this lifecycle: documentation, independent review, publication, merge and process closure only. Separate activation is required. Reader remains NOT_IMPLEMENTED.

## Authority reconstruction and preserved boundaries

R1–R3 CLOSED, R4 design CLOSED and minimum R4 schema prerequisite CLOSED. PR #27 normal merge is the base above, closure candidate 9dbacaa504a0f5607c46bc9c1b13441831bbdfdf, factual-only child 231c3c66cbd363921d21dd57c78fa3d5e9f0e744. PR #24 design closure, #25 fixture reconciliation, #26 schema and #27 closure are ancestors. These facts resolve historical pending capture labels without editing them. Current main has 428 tracked files and 50 SQL migrations, head V47. V47 SHA256 `afcd7316ec922e1afd10fa719cd3d26815b62e8b0a46a3641a266df8ee390e06`, physical Flyway checksum -1624594375 from accepted schema receipt. All historical migrations including integrated V47 are READ-ONLY; no additional migration is authorized or required. Assignment exclusion remains absent. No entity/repository/writer is selected.

Normative precedence: current explicit human lifecycle scope; closed design and closed schema within their competence; this handoff after independent publication and separate activation; factual receipts and real gates; ESTADO process records; physical Git evidence; STATE derived cache. Historic R3 paths/claims/catalogs are precedent only. TurnoInstructor LEGACY_VIVO / PRODUCTIVO, dark launch, current client API/Web/Reservations, productive Programación and DEFAULT_DENY are preserved. R5 composition, R6 multi-reader coordination, productive routing, deployment and cutover NOT_AUTHORIZED. No effective programming is composed.

## Reader contract derived from closed R4 design

The following §§3–6 are reproduced from the sealed R4 design as the operative reader contract. The selected schema is now installed V47/50 and CLOSED, resolving references to a future prerequisite. The concrete catalog, hashes, tests and paths below materialize its previously deferred handoff details. They neither relax it nor import historical implementation.

## 3. Port, SQL y resultado completo

Firma futura exacta, sin implementación presente:

```text
AdjustmentReadPort.readActiveAdjustmentsOnDate(
    AdjustmentReadSnapshotContext context, LocalDate fecha) -> AdjustmentReadSet
AdjustmentReadSet(sources: List<GenericSourceSnapshot>,
                  backing: List<AdjustmentBackingSnapshot>)
```

Context y fecha son obligatorios, iguales; operación exclusivamente por fecha exacta. No scope de salón, rango, IDs ni filtro de targets nominales. Cero filas es válido sólo tras lectura, validación, probes y completion exitosos. Salón final puede cambiar en reemplazo, por lo que filtro por salón de origen perdería hechos. Sources/backing son listas defensivas inmutables, 0..N, biyectivas por adjustmentId y fecha, orden `fecha,id` (UUID sin signo como PostgreSQL); no output parcial. Backing conserva exactamente los doce campos de la tabla, sin masters, proxies ni handles físicos.

SQL DATA único seleccionado para el futuro catálogo `R4_ADJUSTMENT_V1`:

```sql
SELECT id, tipo, fecha, asignacion_serie_id,
       salon_resultado_id, instructor_resultado_id, tipo_actividad_resultado_id,
       hora_inicio_resultado, hora_fin_resultado, activo, creado_en, actualizado_en
FROM programacion_ajuste_fecha
WHERE fecha = :fecha AND activo = :active
ORDER BY fecha, id
```

`AdjustmentProjectionQueryExecutor` plain, constructor DI de EntityManager, native query unwrap Hibernate NativeQuery con scalar named binding `fecha` LocalDate.class y `active` Boolean.class=true, una ejecución DATA, doce columnas/tipos exactos en `AdjustmentProjectionRow`. No colección/rango, SELECT *, joins, entity hydration, DISTINCT, LIMIT, locks, winner selection, flush, repository ni sentencia oculta. El catálogo deberá incluir exactamente DATA y probes/metadata allowlisted, con captura física de SQL y bindings; unknown SQL falla cerrado. Query nunca inventa schema ausente. Filas fuera de fecha/inactivas no forman parte del claim: no se declara audit de toda la tabla.

Primero validar todas las filas: no null en campos obligatorios, tipos conocidos, fecha exacta y activo true, forma por tipo completa, rango positivo y precisión PostgreSQL microsegundos, timestamps representables y no null. No exigir creado <= actualizado como regla de dominio nueva. Después rechazar ID físico duplicado, más de un target activo CANCELACION/REEMPLAZO por serie/fecha, orden no estricto, o mismatch de cardinalidad/backing. Sólo entonces construir fuentes; ninguna deduplicación previa oculta errores.

## 4. Mapping, provenance e identidad

`GenericSourceSnapshot.sourceSystem=NEW_DARK_LAUNCH` conforme al enum actual; vocabulario cerrado, no ampliar core. SourceAtomType es NEW_CANCELACION, NEW_REEMPLAZO o NEW_ADICION según tipo. `sourceIdentity` es el UUID canónico adjustmentId; no inventa serie para adición. Observables y normalizedFields incluyen **todos** los doce campos con nombres de la tabla. NULL permitido por forma se representa por `ABSENT_BY_ADJUSTMENT_FORM`, nunca null en maps ni key omitida. UUID lowercase, fecha ISO, hora HH:mm:ss.SSSSSS, timestamps UTC con seis decimales, boolean `true`, enum exacto. Se usa el framing de `ReadSnapshotIdentifiers.secuenciaTextos` y `mapaCanonico` existentes como utilidades read-only; no concatenación ambigua ni dependencia de HashMap.toString.

Provenance: sourceName del descriptor validado, schemaFingerprint real `sha256:<64 lowercase hex>`, recordIds=[adjustmentId], ruleId=R4_ADJUSTMENT_PROJECTION, ruleVersion=V1, businessTimeContext=`zoneId/fecha`, normalizedFields completo. No IDs/versiones nominales se afirman en R4, porque no se leen. No PII adicional.

Huella de source (sourceFingerprint) = SHA256 del framing `F2E-R4-SOURCE-V1`, SourceSystem, SourceAtomType, sourceIdentity, mapa canónico de los doce campos. SnapshotIdentity = SHA256 framing `F2E-R4-SNAPSHOT-V1`, executionProvenanceId, sourceFingerprint. Cambiar campo técnico/contenido cambia sourceFingerprint; sólo cambiar run/attempt/TX cambia snapshotIdentity, no sourceFingerprint. El backing conserva valores tipados y debe normalizarse idénticamente a la fuente, sin incorporar contexto en contenido físico. Read-set vacío conserva evidencia de ejecución en el owner, no fabrica una source vacía.

| Hecho R4 | Referencia / expected contract posterior | Qué entrega R4 |
| --- | --- | --- |
| CANCELACION | target `(SERIE_ASIGNACION,serie,fecha)`; exactamente una nominal, efectiva ausente | NEW_CANCELACION + target + campos nulos de resultado |
| REEMPLAZO | misma referencia de serie/fecha; exactamente una nominal y una efectiva con resultado | NEW_REEMPLAZO + target + snapshot resultado |
| ADICION | target nominal NOT_APPLICABLE; efectiva `(AJUSTE,id,fecha)` | NEW_ADICION + snapshot resultado; sin serie sintética |

R4 NO verifica target nominal ni efectiva, no genera ProgrammingCandidateSnapshot, EXPECTED_ABSENCE, DetectorResult, omissions ni resolución de negocio. Target nominal 0=MISSING, >1=ambiguous/invariant; outcome ausente/múltiple/mismatch son clasificación de core/R5 bajo su autoridad futura, no fallo inventado por R4 ni empty success. R3 sólo es dependencia de fixtures nominales de aceptación posteriores, sin llamada desde el reader R4. R5 compone, R6 coordina múltiples readers. No mapping legacy ni horario-salón como cancelación de ajuste.

## 5. Ownership transaccional y snapshot individual

Se reconcilia el contexto histórico genérico con `AdjustmentReadSnapshotContext` propio persistence-agnostic. NO se amplían context/claim/enums R1/R2/R3. Campos exactos: runIdentity, attemptIdentity, fecha, businessZone, ruleCatalogVersion, sourceName, schemaFingerprint, databaseName, schemaName, principal, physicalResourceIdentity, projectionCatalogVersion, readerInvocationIdentity, snapshotClaim, snapshotEvidenceId, statementCaptureCommitment. ProjectionCatalogVersion sólo R4_ADJUSTMENT_V1; snapshotClaim sólo R4_INTERNAL_RR_TEST. Texto requerido no blank/NUL/UTF-8 inválido; snapshotEvidenceId y commitment son SHA256 de 64 hex, schemaFingerprint prefijado sha256. Fecha de contexto igual al scope. Context construido/equivalencia de valores no concede autoridad.

Scope canónico `R4_ACTIVE_ADJUSTMENTS_ON_DATE_V1/fecha`. ExecutionProvenanceId = SHA256 del framing `F2E-R4-EXECUTION-V1` y campos en el orden anterior (fecha dentro del scope canónico, zoneId canónico), de forma determinista documentada en el handoff. Caller sólo aporta run/attempt/fecha/zone/rules; owner deriva los demás desde evidencia real y reserva privada ligada a la misma TX e instancia de contexto. Replay de contexto equivalente o reutilizado en otra invocación/TX falla antes de DATA.

Método público proxied del reader:
`@Transactional(transactionManager="f2eReaderTransactionManager", propagation=MANDATORY, readOnly=true)`.
No abre/suspende/eleva/reintenta TX, no owner propio, no fallback default. Manager ausente/incorrecto, falta de proxy/owner, transacción sin RR o read-only, recurso distinto o contexto inválido se rechaza como TRANSACTION_CONTEXT_INVALID antes de DATA. En el futuro handoff debe normalizarse también la excepción de proxy/lookup previa al cuerpo, evitando el error previo R3 de manager faltante clasificado como database failure.

Owner de aceptación test-only, bean separado proxied en ApplicationContext R4 aislado, registra `f2eReaderTransactionManager` sólo en ese contexto. Abre `REQUIRES_NEW + REPEATABLE_READ + readOnly + timeout bounded`, sin I/O externo, una fecha por invocación. Valida inicio/final: `current_setting('transaction_isolation')=repeatable read`, `current_setting('transaction_read_only')=on`, `pg_current_snapshot()::text` idéntico, database/schema/principal y mismo DS/EMF/shared EM/Session/JDBC/PgConnection físico. Recurso no es sólo igualdad de texto caller. Asocia contexto por reserva privada a invocación/owner/manager/conexión; reader verifica activo/synchronization/read-only/RR y reserva antes de DATA. SnapshotEvidenceId compromete descriptor real, owner/invocación, manager/recurso, aislamiento/read-only y texto de snapshot; statementCaptureCommitment compromete catálogo/captura real, incluyendo SQL/binds/probes observados. Verifica cierre antes de entregar éxito.

Read set es provisional dentro de TX; se entrega al caller sólo tras probes finales, capturas, guardas y completion exitosa. Fallo de rollback/commit/completion invalida todo resultado. Ninguna entity/lazy/proxy/Connection escapa. Probes SQL SELECT y metadata necesarios se cierran en catálogo futuro; fingerprint de schema se verifica contra history/checksums reales y slice de tabla/keys/constraints/indexes. No descriptor que acepte V46 como si tuviera la tabla.

Este claim es lectura **individual R4** estable, no SAME_LOGICAL_SNAPSHOT con R1–R3. R6 es el único owner conceptual futuro multi-reader, requiere diseño/handoff/activación separados y reconciliar los claims/managers sellados antes de invocar participantes. Aquí no se construye coordinator, shadow wiring, multi-reader context ni coordinación por rango.

## 6. Fallos operacionales sin resultado parcial

`AdjustmentReadFailure` conceptual tiene Category, fecha cuando conocida, IDs físicos disponibles y cause conservada; excepción antes de éxito, sin DetectorResult. Categorías cerradas:

| Categoría | Trigger |
| --- | --- |
| INVALID_INPUT | fecha/context null, fechas distintas o forma inválida antes de SQL |
| TRANSACTION_CONTEXT_INVALID | proxy/manager/owner/RR/read-only/recurso/snapshot/claim/replay/completion inválidos |
| MALFORMED_PROJECTION | fila, tipo, forma, fecha, active, rango o scalar inválido |
| DUPLICATE_ACTIVE_TARGET_ON_DATE | dos CANCELACION/REEMPLAZO para misma serie/fecha, aun si iguales |
| CARDINALITY_OR_BACKING_MISMATCH | ID duplicado, orden no estricto, fuentes/backing no biyectivos o campos distintos |
| DATABASE_READ_FAILURE | SQL/JPA/JDBC/privilegios/policy/schema ausente, query incompleta o fallo de lectura |

SQLState/cause se preservan. Null list no es empty. Timeout, tabla ausente, descriptor mismatch o privilegios no se traducen a vacío ni retry/fallback. Si falla metadata de esquema antes de DATA, DATABASE_READ_FAILURE con causa de prerequisite/descriptor; si falla identidad transaccional, TRANSACTION_CONTEXT_INVALID. No reparación de datos/esquema. Ninguna selección del ajuste “más reciente”.


## Concrete trust and non-circular execution contract

Runtime reader, executor, mapper and catalog are plain classes with no Spring stereotype or productive bean registration. `AdjustmentJpaReader` exposes a nested `ContextAuthority` verification interface; only the isolated test owner implements it. Port/domain have no Spring/JPA/JDBC dependency. The isolated configuration creates separate SELECT-only DS/EMF/shared EM, named `f2eReaderTransactionManager`, JDK proxies exposing current proxy, and the acceptance owner. No production context registers this manager or R4 beans. No sealed R1–R3 context/enums/managers/tests are changed.

Owner outer boundary calls a separate proxied inner owner REQUIRES_NEW/RR/readOnly with bounded timeout; it normalizes missing manager/proxy lookup and completion exceptions to TRANSACTION_CONTEXT_INVALID with original cause, before publishing any provisional result. Reader verifies its own proxy target, mandatory TX and exact private reservation before DATA. Unknown SQL, privilege/SQL/schema errors retain DATABASE_READ_FAILURE; malformed data and duplicates retain the design categories. Failure translation must not broadly reclassify a database error as transaction error.

ExecutionProvenanceId uses SHA256(ReadSnapshotIdentifiers.secuenciaTextos(...)) in this exact order: `F2E-R4-EXECUTION-V1`, runIdentity, attemptIdentity, scopeCanonical (`R4_ACTIVE_ADJUSTMENTS_ON_DATE_V1/` plus ISO fecha), businessZone.getId(), ruleCatalogVersion, sourceName, schemaFingerprint, databaseName, schemaName, principal, physicalResourceIdentity, projectionCatalogVersion.name(), readerInvocationIdentity, snapshotClaim.name(), snapshotEvidenceId, statementCaptureCommitment. Fecha occurs exactly once in scope; all other context fields once. No caller-controlled owner descriptor or evidence is trusted.

SnapshotEvidenceId is the same framing/hash of `F2E-R4-SNAPSHOT-EVIDENCE-V1`, owner invocation UUID, runIdentity, attemptIdentity, scopeCanonical, manager name, physicalResourceIdentity, databaseName, schemaName, principal, schemaFingerprint, literal `repeatable read`, literal `on`, exact initial pg_current_snapshot text. Physical identity denotes privately retained object identities of DS, EMF, EM holder, shared EM target, Session and native PgConnection; string equality alone never suffices.

The statement commitment is prospective and non-circular: before DATA, the owner freezes the expected complete ordered SQL/bind/metadata manifest derived from the closed catalog and independently known caller date/true, resource descriptor and initial snapshot. Hash framing `F2E-R4-STATEMENT-COMMITMENT-V1`, invocation UUID, catalog version, canonical manifest. It reserves that commitment and exact context instance privately. Actual observations are collected independently and compared against the complete expected manifest at close; they do not create their own expectations. Completion success is outside the digest and is a mandatory separate publication gate. No commitment can include its own context/source hash or future audit/commit/merge identifiers. Any missing/extra/failed statement, wrong bind/resource, replay or completion failure discards the whole read set.

## Closed SQL and metadata catalog R4_ADJUSTMENT_V1

Exactly nine logical SELECT identities. SQL is one-line normalized whitespace, no trailing semicolon; named DATA placeholders become `?` in occurrence order. Catalog ID algorithm: SHA256 of UTF8(`F2E_SQL_CATALOG_ID_V1\n` + byteLength + `:`) followed by positional SQL UTF8 bytes. Probes have zero binds. DATA named typed binds are fecha/LocalDate.class and active/Boolean.class=true. Actual JDBC SQL must match the positional catalog; unknown statements are denied before execution. No callable, createStatement, locks, flush, hidden metadata SQL or extra query is admitted.

### R4_ADJUSTMENTS_ON_DATE_V1

```sql
SELECT id, tipo, fecha, asignacion_serie_id, salon_resultado_id, instructor_resultado_id, tipo_actividad_resultado_id, hora_inicio_resultado, hora_fin_resultado, activo, creado_en, actualizado_en FROM programacion_ajuste_fecha WHERE fecha = :fecha AND activo = :active ORDER BY fecha, id
```

Catalog ID: `df80dc6e8de6a448b4b1432e9451a5bc659272275ab933e28b644a352a7b56ca`.

### R4_TX_ISOLATION_V1

```sql
SELECT current_setting('transaction_isolation')
```

Catalog ID: `4a669a2f628e12468e0d532889e0bcafd38c09a5aadfb27f2f20f56159c1671e`.

### R4_TX_READ_ONLY_V1

```sql
SELECT current_setting('transaction_read_only')
```

Catalog ID: `9963ea856cdf9bfb3e9c440b1c3a6c062fd375a906f465cbe7a7ac88878716c7`.

### R4_TX_RESOURCE_V1

```sql
SELECT current_database() AS database_name, current_schema() AS schema_name, current_user AS principal
```

Catalog ID: `851b907052ecdf5d34f77f047e95ff9c5e902e3b45b09bbd9975edc0ddad507c`.

### R4_TX_SNAPSHOT_V1

```sql
SELECT pg_current_snapshot()::text
```

Catalog ID: `24f02692f50e880b77a78f9ea64501ce276b7b0e2a93c880a4c23e03bbe19aa0`.

### R4_SCHEMA_HISTORY_V1

```sql
SELECT installed_rank, version, description, type, script, checksum, success FROM public.flyway_schema_history ORDER BY installed_rank
```

Catalog ID: `87cda2f204bb29283d741ac2f518ffc5be4ec2ced1eab4d34a24e322c587a6de`.

### R4_SCHEMA_COLUMNS_V1

```sql
SELECT a.attnum, a.attname, pg_catalog.format_type(a.atttypid, a.atttypmod) AS data_type, a.attnotnull, pg_catalog.pg_get_expr(d.adbin, d.adrelid) AS column_default FROM pg_catalog.pg_attribute a JOIN pg_catalog.pg_class c ON c.oid = a.attrelid JOIN pg_catalog.pg_namespace n ON n.oid = c.relnamespace LEFT JOIN pg_catalog.pg_attrdef d ON d.adrelid = a.attrelid AND d.adnum = a.attnum WHERE n.nspname = 'public' AND c.relname = 'programacion_ajuste_fecha' AND a.attnum > 0 AND NOT a.attisdropped ORDER BY a.attnum
```

Catalog ID: `6a4269c1e807dad48a46b4f3974cd688be243bbc4eb4c989c920a39947800a2e`.

### R4_SCHEMA_CONSTRAINTS_V1

```sql
SELECT con.conname, con.contype::text, con.convalidated, pg_catalog.pg_get_constraintdef(con.oid, true) AS definition FROM pg_catalog.pg_constraint con JOIN pg_catalog.pg_class c ON c.oid = con.conrelid JOIN pg_catalog.pg_namespace n ON n.oid = c.relnamespace WHERE n.nspname = 'public' AND c.relname = 'programacion_ajuste_fecha' ORDER BY con.conname
```

Catalog ID: `d326eb312c002af342e6ee21cfb00bb006d0bd5e0db0c59d1d25aaebda18653a`.

### R4_SCHEMA_INDEXES_V1

```sql
SELECT i.relname AS index_name, x.indisunique, x.indisvalid, x.indisready, pg_catalog.pg_get_indexdef(x.indexrelid) AS definition FROM pg_catalog.pg_index x JOIN pg_catalog.pg_class c ON c.oid = x.indrelid JOIN pg_catalog.pg_namespace n ON n.oid = c.relnamespace JOIN pg_catalog.pg_class i ON i.oid = x.indexrelid WHERE n.nspname = 'public' AND c.relname = 'programacion_ajuste_fecha' ORDER BY i.relname
```

Catalog ID: `c5229dde86781fb86c638f60d492efd901d56516b47f5836fe19e79ab9f7370c`.

Expected ordered manifest: ISOLATION, READ_ONLY, RESOURCE, SNAPSHOT, SCHEMA_HISTORY, SCHEMA_COLUMNS, SCHEMA_CONSTRAINTS, SCHEMA_INDEXES, DATA, ISOLATION, READ_ONLY, RESOURCE, SNAPSHOT, SCHEMA_HISTORY, SCHEMA_COLUMNS, SCHEMA_CONSTRAINTS, SCHEMA_INDEXES (17 executions, DATA exactly once; prefixes R4_TX_/R4_SCHEMA_ as above). Empty DATA follows the same manifest. RESOURCE must yield expected database/public/actual SELECT-only principal. Initial/final probes, descriptors and schema fingerprint must equal. Explicit metadata calls: Connection.unwrap(PGConnection.class), getMetaData().getURL(), getMetaData().getUserName() only, observed on the same bound physical resource at start/end; no catalog traversal via metadata. Session/EM object checks are non-SQL. TX driver lifecycle operations begin/read-only/isolation/commit/rollback are separately captured, not extra product SQL grants.

SchemaFingerprint is `sha256:` plus SHA256 framing `F2E-R4-SCHEMA-V1`, four labeled schema result sets in catalog order, each count and each scalar in returned column order, with explicit `SQL_NULL` sentinel and typed canonical values. PostgreSQL16 catalog rendering is fixed; acceptance compares all rows against an independently built fresh canonical Flyway V1→V47 installation, including full 50-entry history/version/script/checksum/success, twelve column types/null/defaults, exact seven constraints (PK, 3 FKs, 3 checks) and active uniqueness as an index, all actual indexes/partial predicates/validity and FK targets. The authoritative expected rows come from canonical DDL and fresh installation, never from the tested schema itself. Migration scripts are rehashed against frozen main. Missing/altered history/table/key/check/index or any extra adjustment structure fails DATABASE_READ_FAILURE before DATA. This does not grant reading master rows. History `SELECT` grant is technical metadata only.

## Real acceptance gates — future implementation only

No compile, Maven, database or reader acceptance is executed for this documentary lifecycle. All technical gates below are REQUIRED / NOT_EXECUTED until a separately authorized implementation exists. A mock or equality checksum alone is insufficient.

- REAL_POSTGRES_HOST: real PostgreSQL16/Testcontainers, full Flyway50/head47 and physical installed V47 checksum -1624594375, canonical migration byte preservation. Real HostValidator REQUIRED; unavailable host blocks acceptance. V46 negative context has table absent and returns DATABASE_READ_FAILURE rather than empty success. No migration edits, new migration or constraint removal on canonical fixture.
- REAL_JDBC_BINDING_ACCEPTANCE: capture native query `setParameter` names, declared/runtime types and independent expected values plus actual executed PreparedStatement SQL, every 1-based setter slot/type/value on the same connection. Slot1 is fecha DATE (LocalDate preserved through actual JDBC Date/setDate or typed DATE setObject); slot2 active BOOLEAN true (setBoolean or typed BOOLEAN setObject). Record the actual setter and assert DATE/BOOLEAN independently. No additional slots. Fault controls wrong date, wrong slot, swapped parameters, omitted bind, wrong scalar type and active=false must fail the gate; executor mocks supplement only.
- SELECT_ONLY_NO_WRITE: R4 role SELECT only public.programacion_ajuste_fecha and public.flyway_schema_history, public schema USAGE and necessary public PostgreSQL system catalog SELECT/probe execution. No SELECT on nominal/legacy/reservations/master data, no write/TRUNCATE/DDL/sequence privileges or ownership. Use existing parameterized F2eSelectOnlyRole only as read-only helper; verify pg privileges for actual role. Owner/executor same role/resource. Privileged fixtures before capture. In separate non-read-only role-control TXs INSERT/UPDATE/DELETE/TRUNCATE/ALTER/DROP/CREATE and sequence writes are denied SQLState42501; read-only controls separately assert25006 using a privileged read-only TX. Unknown SQL, writes and locks are denied by both JDBC capture and inspector before execution. Do not contaminate successful read catalog with negative writes.
- CHECKSUM_STABILITY: new AdjustmentSliceChecksum dynamically selects every active row on exact fecha, including twelve fields and IDs, deterministic PostgreSQL unsigned UUID order, count, scope, type tags and SQL_NULL token. Before/after actual owned read including operational failure windows use a dedicated observer connection outside capture, fresh transactions and no external writers. Recompute membership each time; hashes equal, nonconstant deterministic output. Schema/history hashes also stable. No BEFORE-frozen IDs. Source serialization uses ABSENT_BY_ADJUSTMENT_FORM; observer SQL NULL is unambiguous independently typed.
- CHECKSUM_SENSITIVITY: fresh snapshot after each isolated privileged fixture transition proves inequality for applicable insertion, deletion, active false, date leaving/entering predicate and each mutable projected field (including timestamps, target/type and result fields, using valid forms). Same unchanged state repeats. Fields excluded by applicability are not falsely claimed audited. Constraint-preserving fixtures for normal acceptance.
- INDIVIDUAL_RR_SNAPSHOT: initial snapshot established before administrative concurrent commit inserts/updates/deactivates adjustment; owned DATA/final probes see original state, new TX sees committed change. Bounded barriers and physical commit evidence. Compare same-TX view while concurrency occurs; do not demand a global checksum remain stable across concurrent writer. No R1–R3 shared snapshot claim.
- FAIL_CLOSED_DATA: three valid forms, empty/multiple, inactive/other dates excluded, replacement changes salon, two identical-dimension additions with distinct IDs survive, missing nominal target does not fail R4. Mapper/projection fault fixtures cover every null/type/shape/range/microsecond/timestamp error, duplicate physical ID, duplicate cancellation/replacement target even identical and cancellation plus replacement contradiction, unordered/equal IDs, backing mismatch and null row list. Reject entire read, no dedup/winner/partial result. DB-degraded duplicate fixture, if used, is negative isolated and descriptor rejected; intact constraint in normal schema. Do not weaken canonical constraint to exercise mapper.
- PROVENANCE_HASH: exact twelve-field observables/normalized maps and typed backing bijection, immutable defensive lists; canonical UUID/date/time6/UTC timestamp6/true/tokens; framing and unsigned UUID order. Repeat content hashes; each field mutation changes sourceFingerprint; run/attempt/TX only changes snapshotIdentity. Wrong owner/source/schema/capture provenance fails.
- TRUST_COMPLETION: proxied MANDATORY, outer normalized manager lookup, no TX/owner, raw new/self invocation, RC, read-write, wrong manager/EMF/Session/PgConnection, forged equal-value context, stale invocation/replay, wrong date/catalog/claim, snapshot drift, metadata/catalog drift, failed initial/final probes/commit/rollback/completion, timeout and privilege failure all fail with design categories and original causes before result publication. No fallback/retry.
- ARCHITECTURE_RUNTIME_ISOLATION: static package edges plus production ApplicationContext/reachability assert no R4 beans, manager, productive caller or stereotypes; no repository/entities/hydration, web/controllers/jobs/services/routing, R1–R3 business reader calls, nominal/effective composition, Payments/Notifications, R5–R6 or cutover. Test-only owner and instrumentation do not enter runtime dependency graph.
- REGRESSION_SCOPE: compile, six dedicated R4 suites; existing R1–R3 transaction/PG/architecture/runtime tests, detector and bounded Lane1–4 plus full default repository regression, zero failed/error/skipped justified by this change. Fresh independent acceptance exact implementation candidate with RAW logs/XML/JDBC/catalog/schema/checksum/host receipts; diff-check and full preservation/scope proof. Test counts are derived at execution, not promised stale555. Host checks may reuse only exact-byte competent evidence where permitted; new R4 acceptance cannot inherit old schema PASS.

## Exact future implementation path sets — inactive until separate activation

All paths are canonical repository-relative files. NEW was physically absent on base; READ_ONLY and PROVENANCE_ONLY present. A new path is permission to implement its named R4 responsibility only. No directory or wildcard grant. Isolated R4 inspector/checksum/capture are NEW; shared sealed fixtures, guards and infra stay byte-identical.

### CURRENT_R4_AUTHORIZED_NEW

```text
src/main/java/com/feelingpilates/transicion/programacion/r4/adapter/jpa/AdjustmentJpaReader.java
src/main/java/com/feelingpilates/transicion/programacion/r4/adapter/jpa/mapper/AdjustmentProjectionMapper.java
src/main/java/com/feelingpilates/transicion/programacion/r4/adapter/jpa/projection/AdjustmentProjectionCatalog.java
src/main/java/com/feelingpilates/transicion/programacion/r4/adapter/jpa/projection/AdjustmentProjectionQueryExecutor.java
src/main/java/com/feelingpilates/transicion/programacion/r4/adapter/jpa/projection/AdjustmentProjectionRow.java
src/main/java/com/feelingpilates/transicion/programacion/r4/read/AdjustmentBackingSnapshot.java
src/main/java/com/feelingpilates/transicion/programacion/r4/read/AdjustmentReadFailure.java
src/main/java/com/feelingpilates/transicion/programacion/r4/read/AdjustmentReadPort.java
src/main/java/com/feelingpilates/transicion/programacion/r4/read/AdjustmentReadSet.java
src/main/java/com/feelingpilates/transicion/programacion/r4/read/AdjustmentReadSnapshotContext.java
src/test/java/com/feelingpilates/transicion/programacion/r4/adapter/jpa/AdjustmentJpaReaderArchitectureTest.java
src/test/java/com/feelingpilates/transicion/programacion/r4/adapter/jpa/AdjustmentJpaReaderPostgreSqlTest.java
src/test/java/com/feelingpilates/transicion/programacion/r4/adapter/jpa/AdjustmentJpaReaderRuntimeIsolationTest.java
src/test/java/com/feelingpilates/transicion/programacion/r4/adapter/jpa/AdjustmentJpaReaderTransactionTest.java
src/test/java/com/feelingpilates/transicion/programacion/r4/adapter/jpa/AdjustmentProjectionMapperTest.java
src/test/java/com/feelingpilates/transicion/programacion/r4/adapter/jpa/AdjustmentProjectionQueryExecutorTest.java
src/test/java/com/feelingpilates/transicion/programacion/r4/adapter/jpa/testinfra/AdjustmentJdbcCapture.java
src/test/java/com/feelingpilates/transicion/programacion/r4/adapter/jpa/testinfra/AdjustmentPostgresTestConfiguration.java
src/test/java/com/feelingpilates/transicion/programacion/r4/adapter/jpa/testinfra/AdjustmentSliceChecksum.java
src/test/java/com/feelingpilates/transicion/programacion/r4/adapter/jpa/testinfra/AdjustmentStatementPolicyInspector.java
src/test/java/com/feelingpilates/transicion/programacion/r4/adapter/jpa/testinfra/AdjustmentTransactionTestOwner.java
```

### CURRENT_R4_AUTHORIZED_MODIFIED

EMPTY_SET (0 paths; zero-byte hash input).

### CURRENT_R4_READ_ONLY

```text
auditoria/fase-2e-r4-diseno-reader-ajustes-fecha-reconciliado.md
auditoria/fase-2e-r4-prerrequisito-esquema-reconciliado.md
src/main/java/com/feelingpilates/transicion/programacion/adapter/jpa/policy/F2eSqlPolicyViolationException.java
src/main/java/com/feelingpilates/transicion/programacion/detector/DetectorVocabulary.java
src/main/java/com/feelingpilates/transicion/programacion/detector/EvidenceProvenance.java
src/main/java/com/feelingpilates/transicion/programacion/detector/GenericSourceSnapshot.java
src/main/java/com/feelingpilates/transicion/programacion/read/ReadSnapshotIdentifiers.java
src/main/resources/db/migration/V47__programacion_ajuste_fecha.sql
src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/testinfra/F2eSelectOnlyRole.java
```

### CURRENT_R4_PROVENANCE_ONLY

```text
auditoria/orquestacion/F2E-EXECUTION-POLICY.md
auditoria/orquestacion/F2E-RUNBOOK.md
auditoria/reviews/F2E-R4-DESIGN-CLOSURE-INDEPENDENT-RECEIPT.md
auditoria/reviews/F2E-R4-SCHEMA-CLOSURE-INDEPENDENT-RECEIPT.md
auditoria/reviews/F2E-R4-SCHEMA-INDEPENDENT-RECEIPT.md
src/main/java/com/feelingpilates/transicion/programacion/adapter/jpa/NominalJpaReader.java
src/main/java/com/feelingpilates/transicion/programacion/adapter/jpa/projection/NominalProjectionCatalog.java
src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/testinfra/NominalTransactionTestOwner.java
```

### DEFAULT_DENY and deterministic identities

Every path outside AUTHORIZED_NEW ∪ AUTHORIZED_MODIFIED is write-denied for future implementation. READ_ONLY files are consumable but immutable; PROVENANCE_ONLY are precedent/evidence, not implementation dependencies. All 50 migrations, existing R1–R3 source/tests/guards/handoffs, configuration and productive code are denied. A need outside this list requires STOP/HUMAN_GATE and newly reviewed scope before writing. Activation authorizes starting a future lifecycle within these paths, never implementation by this documentary run.

Path-set algorithm: deduplicate, sort UTF8 byte order, join LF, append final LF for nonempty set; EMPTY_SET is zero bytes; SHA256 UTF8. Union sets deduplicated; hashes contain paths only. Existing-content manifest separately records SHA256 bytes. No historical allowlist is reused.

| Set | Count | SHA256 |
| --- | ---: | --- |
| CURRENT_R4_AUTHORIZED_NEW | 21 | `f7514bde3d6cee12de6e26a30d3f1e6038c3031d2c99344da9eac55c870293b9` |
| CURRENT_R4_AUTHORIZED_MODIFIED | 0 | `e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855` |
| CURRENT_R4_READ_ONLY | 9 | `7392db15004fafe0b85ec7e661dcb5c6aa860b1c99c03182dc161edf2a16c83c` |
| CURRENT_R4_PROVENANCE_ONLY | 8 | `29d31787280a52a53f9bdeb8fbb2dfdc4a400f5a5a8ea2b612f3734788aa9103` |
| WRITE_SCOPE | 21 | `f7514bde3d6cee12de6e26a30d3f1e6038c3031d2c99344da9eac55c870293b9` |
| TOTAL_ACTIVE_PATHS | 30 | `1ed78c51c87351ff6cf31c0077cbe4f7859e59fb0d9b35c2cff93539ee21b1d5` |

## Bounded Correction.1 — R4-HANDOFF-P1-1

Original candidate be367b72f15d5d6597cbe8b0ddc56c666dfbbc6e remains BLOCKED P0/P1/P2=0/1/0 in history. Fresh native audit task_ba02f985def3 / ctx_a959651d67ac supplies exact path-only corrective artifact; no HUMAN_GATE or scope expansion required. This single P1 correction consumes handoff-stage P1 1/1 (P2 0/2). Basenames/responsibilities/contracts and existing READ_ONLY9/PROVENANCE_ONLY8 remain unchanged. No source file is created here.

Future domain/port package is com.feelingpilates.transicion.programacion.r4.read; adapters/tests use com.feelingpilates.transicion.programacion.r4.adapter.jpa and corresponding mapper/projection/testinfra subpackages. Existing read-only helpers remain at their actual sealed packages. All21 NEW paths are outside the sealed recursive R1/R2/R3 inventory roots. NEW R4 architecture suite must independently assert its entire exact ten-main/eleven-test inventory with extra/missing/stereotype/caller negative controls and domain package edges. Preserve complete current R1/R2/R3 inventory equality in ReservaJpaReaderArchitectureTest plus every broader no-escape/R2/R3 caller scan and full default regression; no source masking, skip, old guard edit or reuse grant. Correction is documentary path materialization only. New independent exact-byte re-audit required; NOT_ACTIVE / NOT_AUTHORIZED / NOT_IMPLEMENTED remain until separate lifecycles.

## Publication, process closure and separate activation

Fresh independent native GPT-6.1-Sol High audits exact committed handoff/process/STATE/ESTADO candidate, full external manifest including STATE, frozen process allowlist and baseline preservation. Exactly one fresh auditor per round. No self audit. P0/P1=0 required; numbered findings/corrective artifact; P1 bounded unique within authority maximum1, P2 mechanical maximum2, TX correction maximum1 only where closed design fixes solution; P0/authority/scope contradiction or exhausted budget HUMAN_GATE. Counters persist across rounds and blocked candidate commits remain history. Main movement STOP; do not silently rebase approval.

After approval, sole direct-child change is factual reserved independent receipt, preserving all audited bytes. Publish/normal merge only while live main still base; verify PR head/merge parents, exact five process paths, full baseline preservation outside ESTADO/STATE and new documentary files, predecessor/candidate/receipt ancestry, clean tracked/index/untracked and hashes. Then handoff lifecycle CLOSED/PUBLISHED/INTEGRATED, handoff NOT_ACTIVE and reader NOT_AUTHORIZED/NOT_IMPLEMENTED. Documentary technical tests NOT_APPLICABLE/NOT_EXECUTED.

Separate activation begins on that newly verified canonical main. Freeze a new exact process-only allowlist, state/ESTADO activation candidate and activation review; bind published handoff/design/schema/receipt hashes and allowlist hashes. Candidate explicitly NOT_ACTIVE; fresh independent activation audit APPROVED does not activate itself. Factual audit receipt direct-child preserves candidate bytes. Subsequent deterministic factual finalization changes process state only, names already published approval and fixed hashes; effective solely after its verified normal merge on main and all prerequisite ancestry/hashes/scope/clean checks. Then handoff ACTIVE, R4 implementation AUTHORIZED_TO_START but NOT_IMPLEMENTED. No product changes or R5–R6 authorization. If any finalization changes audited semantics/scope, require fresh audit rather than inherit approval. Future IDs/hashes are external receipts, never self-hashes.
