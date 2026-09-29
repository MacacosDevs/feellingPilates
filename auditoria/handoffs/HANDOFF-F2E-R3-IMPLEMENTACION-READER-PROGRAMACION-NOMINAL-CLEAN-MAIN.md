# F2E R3 — handoff de implementación del reader de programación nominal, clean main

## Autoridad y estado

- Base canónica y `origin/main` verificados: `5964844e92fb467cda78debef01e318ebe33546f`.
- Diseño cerrado: `auditoria/fase-2e-r3-diseno-reader-programacion-nominal-reconciliado.md`, SHA-256 `42a21c09d137d363e938d441a499d48c5ec7537a73ee23861bfce2c4d75677f9`. Su bloque de estado interno conserva texto previo a la publicación; el cierre posterior consta en `auditoria/ESTADO-ACTUAL.md` y `auditoria/orquestacion/F2E-STATE.json`.
- Revisión independiente: `auditoria/reviews/F2E-R3-REVIEW-DISENO-READER-PROGRAMACION-NOMINAL-RECONCILIADO.md`: APPROVED, P0=0, P1=0, P2=0. Diseño reconciliado, publicado, integrado y cerrado.
- Este documento: `MATERIALIZED_CANDIDATE / PENDING_FRESH_INDEPENDENT_HANDOFF_AUDIT / NOT_APPROVED / NOT_PUBLISHED / NOT_ACTIVE`.
- Implementación R3: `NOT_AUTHORIZED / NOT_IMPLEMENTED`. Este allowlist es propuesta exacta para la auditoría siguiente, no permiso de escritura actual.

## Objetivo y árbol reconciliado

El futuro port lee únicamente programación nominal aplicable a **una** `LocalDate`, desde `programacion_asignacion` y `programacion_bloque`, y entrega `NominalProgrammingReadSet(candidates, backing)`. Un candidate usa `ProgrammingCandidateSnapshot` con `CandidateType.NOMINAL_OCCURRENCE` y `ReferenciaOcurrencia(SERIE_ASIGNACION, serie_id, fecha)`. No lee ajustes, no produce programación efectiva y no reconcilia datos R1/R2.

| Dependencia actual | Clasificación | Uso preciso |
| --- | --- | --- |
| `ProgrammingCandidateSnapshot`, `EvidenceProvenance`, `ReferenciaOcurrencia`, `DetectorVocabulary`, `ReadSnapshotIdentifiers` | REUSE_READ_ONLY | Vocabulario inmutable, identidad lógica, codificación/hash común. |
| `ReadSnapshotContext`, `LegacyTurnReadContext`, readers/read sets R1/R2 | NOT_APPLICABLE para datos R3; READ_ONLY como precedente | Contextos cerrados a claims/catálogos R1/R2; no convertirlos en contrato R3. |
| `F2ePostgresTestConfiguration` | REUSE_READ_ONLY | Patrón de PostgreSQL 16, Flyway V1→V46, descriptor y probes; configuración R3 aislada. |
| `F2eSelectOnlyRole` | REUSE_READ_ONLY | `crear(privilegiada, prefijo, clave, Set.of(...))` ya admite dos tablas; controles negativos R3 se escriben en test propio porque su helper de INSERT está fijado a reserva. |
| `F2eStatementPolicyInspector` | AUTHORIZED_ADDITIVE_MODIFICATION_CANDIDATE | Factoría/catálogo cerrado R3; preservar catálogos R1/R2 y captura por invocación. |
| `F2eSliceChecksum` | AUTHORIZED_ADDITIVE_MODIFICATION_CANDIDATE | Funciones R3 de hash de datos de bloque/asignación, sin alterar R1/R2. |
| `ReaderTransactionTestHarness`, `LegacyTurnTransactionTestOwner`, `LegacyTurnJdbcCapture` | READ_ONLY como precedente; NOT_APPLICABLE como owner R3 | Sus identidades y reglas están selladas a R1/R2. Owner R3 aislado. |
| V41 y V42–V46; entidades/repositories productivos | READ_ONLY o PROVENANCE_ONLY según allowlist | V41 define columnas; V46 es head. `AsignacionRepository` no es executor R3. |
| V47 histórico y `programacion_ajuste_fecha` | PROVENANCE_ONLY / fuera de alcance | No existe V47 en árbol canónico; no es prerequisito R3. |

## Forma de producción futura

| Ruta NEW | Tipo/responsabilidad | Dependencias y razón de tipo nuevo |
| --- | --- | --- |
| `src/main/java/com/feelingpilates/transicion/programacion/read/NominalProgrammingReadPort.java` | Interfaz `readNominalOnDate(NominalReadSnapshotContext, LocalDate)` | Firma R3 propia; ports R1/R2 tienen otros scopes y datos. |
| `src/main/java/com/feelingpilates/transicion/programacion/read/NominalReadSnapshotContext.java` | Carrier inmutable de input y evidencia owner R3 | `ReadSnapshotContext` R1 y `LegacyTurnReadContext` R2 tienen claims sellados. |
| `src/main/java/com/feelingpilates/transicion/programacion/read/NominalProgrammingReadSet.java` | Listas ordenadas, defensivas e inmutables de candidates/backing | Read sets R1/R2 no representan filas nominales. |
| `src/main/java/com/feelingpilates/transicion/programacion/read/NominalBackingSnapshot.java` | Backing escalar por referencia y versiones físicas | `EvidenceProvenance` se reutiliza dentro del candidate, pero no contiene todos los campos físicos verificables de ambas tablas. |
| `src/main/java/com/feelingpilates/transicion/programacion/read/NominalReadFailure.java` | Categoría tipada y excepción/rechazo R3, con diagnóstico seguro | Excepciones R1/R2 codifican sus universos; una sola unidad R3 evita clases por categoría. No convertir fallos DB en vacío. |
| `src/main/java/com/feelingpilates/transicion/programacion/adapter/jpa/projection/NominalProjectionRow.java` | Record escalar de las 23 columnas consultadas | Rows R1/R2 modelan tablas diferentes. |
| `src/main/java/com/feelingpilates/transicion/programacion/adapter/jpa/projection/NominalProjectionCatalog.java` | SQL única y probes sellados, bindings/IDs verificables | Catálogo R2 de seis statements está cerrado a Turno. |
| `src/main/java/com/feelingpilates/transicion/programacion/adapter/jpa/projection/NominalProjectionQueryExecutor.java` | SQL nativa con `EntityManager`, parámetros nombrados tipados; materializa todas las filas | `AsignacionRepository` productivo carece de provenance y autoridad SQL; executor R2 es de Turno. |
| `src/main/java/com/feelingpilates/transicion/programacion/adapter/jpa/mapper/NominalProjectionMapper.java` | Valida forma, vigencias, intervalos, duplicados, cardinalidad y construye todo o falla | Mappers R1/R2 modelan otros universos. |
| `src/main/java/com/feelingpilates/transicion/programacion/adapter/jpa/NominalJpaReader.java` | Adapter MANDATORY, guarda owner/contexto, consulta y mapea | Readers R1/R2 están sellados a otras fuentes. Bean sólo en aceptación R3 aislada; ningún caller productivo. |

No se requiere clase `NominalScope`: la `LocalDate` exacta de la firma es el scope tipado. El owner deriva día y representación canónica de ella. No crear coordinator R6 ni adapters de ajustes.

## Query y semántica nominal

SQL nativa única para DATA, sobre V41/V46, sin master joins, hidratación entity, locks ni ajustes:

```sql
SELECT a.serie_id, a.id, a.bloque_id, b.id, b.serie_id,
       b.salon_id, a.instructor_id, a.tipo_actividad_id,
       a.hora_inicio, a.hora_fin, b.hora_inicio, b.hora_fin,
       a.vigente_desde, a.vigente_hasta, a.activo,
       b.vigente_desde, b.vigente_hasta, b.activo,
       a.creado_en, a.actualizado_en, b.creado_en, b.actualizado_en,
       b.dia_semana
FROM programacion_asignacion a
JOIN programacion_bloque b ON b.id = a.bloque_id
WHERE a.activo = :assignmentActive
  AND b.activo = :blockActive
  AND a.vigente_desde <= :fecha
  AND (a.vigente_hasta IS NULL OR :fecha <= a.vigente_hasta)
  AND b.vigente_desde <= :fecha
  AND (b.vigente_hasta IS NULL OR :fecha <= b.vigente_hasta)
  AND b.dia_semana = :dayOfWeek
ORDER BY a.serie_id, a.id
```

Bind `:fecha` as `LocalDate`, both active flags as typed `Boolean.TRUE`, `:dayOfWeek` as typed `Short` derived from the exact date (`0=domingo` through `6=sábado`; Java `DayOfWeek.getValue()%7`). Bind every occurrence of `:fecha` to the same value. `vigente_hasta=NULL` is open-ended; both ends are inclusive. `a.serie_id` alone is logical series identity; `a.id` is assignment version, `a.bloque_id=b.id` identifies block version, and `b.serie_id` is backing, not candidate identity. `b.salon_id`, `a.instructor_id`, `a.tipo_actividad_id`, `a.hora_inicio`, `a.hora_fin` are candidate values. Preserve block hours, both validity ranges, active flags, exact date/day, technical timestamps, row IDs and both series in immutable backing; timestamps never select a winner.

Both intervals must be positive and assignment `[hora_inicio,hora_fin)` wholly contained in block interval. Reject null physical values where V41 requires non-null, unexpected types, inconsistent day/date/validity, invalid ranges and unmatched backing. Do not clip, repair or deduplicate. The query must expose **every** applicable physical assignment version. `DISTINCT`, `DISTINCT ON`, deduplicating `GROUP BY`, `LIMIT`, winner filtering with `ROW_NUMBER`, and `MAX/MIN` winner choice are forbidden. If two returned rows share `a.serie_id` for the requested date, reject the **entire** read as `DUPLICATE_SERIES_ON_DATE`, retaining safe physical IDs only for internal diagnosis. SQL order is deterministic diagnostic order, never priority. An empty read set is valid only after successful SQL and validation. Lists are immutable and one-to-one by occurrence reference; no JPA entity/proxy escapes.

## Fallos y transacción

Required failure categories are `INVALID_INPUT`, `TRANSACTION_CONTEXT_INVALID`, `MALFORMED_PROJECTION`, `DUPLICATE_SERIES_ON_DATE`, `CARDINALITY_OR_BACKING_MISMATCH`, `DATABASE_READ_FAILURE`. The proposed `NominalReadFailure` represents them without borrowing R2 `LegacyAdapterRejection` or R1 `ReservationReadException`. Error translation preserves causes and safe scope/physical version IDs; JDBC/JPA, SQL policy, privilege and completion failures never become empty-success, partial success or semantic detector rejection.

`NominalJpaReader.readNominalOnDate` is a proxied public method annotated `@Transactional(transactionManager="f2eReaderTransactionManager", propagation=MANDATORY, readOnly=true)`. It cannot create, suspend, replace or retry a transaction. The individual test-only owner is a separate proxied bean with `REQUIRES_NEW / REPEATABLE_READ / readOnly=true`, bounded timeout, the same manager and PostgreSQL SELECT-only datasource/EMF/Session/Connection. A call without owner or through direct `new`/self-invocation fails closed before DATA. Validate active TX, RR, read-only, manager identity, datasource/EMF/session/connection and native `PgConnection`, database/schema/principal, owner binding and snapshot initial/final on the **same** physical resource. Wrong manager/resource/owner, missing owner, forged or stale context, mismatched snapshot and failed completion are `TRANSACTION_CONTEXT_INVALID`. The owner retains provisional output until final probes, statement capture and transaction completion are valid. No R6 multi-reader owner is implemented.

`NominalReadSnapshotContext` separates:

| Authority | Fields/handling |
| --- | --- |
| CALLER_INPUT | `runIdentity`, `attemptIdentity`, exact `fecha`, `businessZone`, `ruleCatalogVersion`; validate presence/UTF-8/form and scope consistency. |
| OWNER_GENERATED_TRUSTED_EVIDENCE | Resource descriptor (`sourceName`, schema fingerprint, database/schema/principal and physical resource identity), frozen R3 catalog identity, unique `readerInvocationIdentity`, `R3_INTERNAL_RR_TEST` claim, `snapshotEvidenceId` derived from owner/attempt/manager/resource/RR/read-only/exact PostgreSQL snapshot text, and statement capture commitment. Only owner creates/reserves these **inside** the transaction. |
| DERIVED | `dayOfWeek` and canonical exact-date scope from `fecha`; never accept caller day or provenance strings. |
| VALIDATED | Reader matches the **same context instance/reservation** in private owner registry, current TX/resource/descriptor/catalog/scope/snapshot and invocation. Owner verifies final snapshot text, connection and complete ordered SQL/JDBC observation against initial evidence before publishing. Constructing equivalent fields is not authority. |

R2's owner/registry pattern is precedent only. Do not trust a public constructor, caller hash, `logicalSnapshotId`, or caller-provided SQL observation; an equal-value forged context or replay from another transaction/attempt fails. This closes the prior R2 provenance-forgery class of defect. Claim cannot be promoted to `MULTI_READER_MVCC`.

## SQL policy, no-write and checksum

Closed R3 catalog has exactly five logical SQL identities: `R3_NOMINAL_ON_DATE_V1` (DATA query above), `R3_TX_ISOLATION_V1` (`SELECT current_setting('transaction_isolation')`), `R3_TX_READ_ONLY_V1` (`SELECT current_setting('transaction_read_only')`), `R3_TX_RESOURCE_IDENTITY_V1` (`SELECT current_database() AS database_name, current_schema() AS schema_name`), `R3_TX_SNAPSHOT_V1` (`SELECT pg_current_snapshot()::text`). Probe calls may repeat before/after DATA; no extra SQL is implicitly authorized. Freeze normalized SQL IDs with existing `F2E_SQL_CATALOG_ID_V1` algorithm in `NominalProjectionCatalog`; tests verify exact binding and ordered observed statements. Extend inspector additively with R3 catalog factory/capture; unexpected SQL, unrecognized statements, nested/out-of-invocation capture and writes fail closed. JDBC metadata checks must be measured and bound to the same resource; if a required probe emits SQL, it must be explicitly cataloged before implementation approval, never silently admitted.

PostgreSQL 16/Testcontainers acceptance grants the R3 principal `SELECT` only on `public.programacion_asignacion` and `public.programacion_bloque`, using existing parameterized `F2eSelectOnlyRole.crear`. Do not grant adjustments, sequences, other product tables, INSERT/UPDATE/DELETE/TRUNCATE/DDL. Negative controls against **both** nominal tables for INSERT/UPDATE/DELETE and representative DDL must produce SQLState `42501`, with privilege failure never translated to success. Fixture setup uses privileged setup connection before read; R3 reader remains SELECT-only.

Extend `F2eSliceChecksum` additively for R3. Before/after measurements use the same exact-date slice definition and include **all rows of both nominal tables**, or a frozen superset of IDs that includes rows which could enter/leave applicability; hash all V41 columns read by R3 plus row identity and count, ordered by physical UUID, with explicit null encoding, table labels and scope/date/day. This catches changes to applicability, duplicate versions, candidate fields and backing fields; a checksum of only currently returned rows would miss a row removed from scope. Compare before == after; mismatch fails acceptance. Schema/Flyway fingerprint must cover V46 applied history and the relevant columns, keys, constraints and indexes, but cannot substitute for data checksum. V47 and assignment overlap exclusion are **not** expected.

## Arquitectura y aceptación futura

New R3 architecture guard checks port/domain/adapter package edges and Spring bean reachability. Production R3 must not depend on controllers/web, scheduled productive jobs, productive service routing, write repositories, R1 Reserva or R2 LegacyTurn business APIs/read sets, Payments, Notifications, `AjusteProgramacionFecha`, R4–R6 behavior, cutover/runtime switches or unauthorized Spring stereotypes. `NominalJpaReader`, executor and mapper are plain classes configured only by isolated acceptance wiring; no productive caller. Existing R1/R2 architecture guards are read-only; add no R3 references to them unless a fresh gate authorizes an additional path.

Future validation gate, after independent handoff audit and separate implementation authorization: compile; R3 mapper unit cases for malformed types/nulls/ranges/containment, zero/multiple series, backing cardinality and order; executor test for exact SQL/named typed bindings and all physical versions; transaction tests for MANDATORY/missing owner, RR/read-only, wrong manager/resource, forged/replayed context, snapshot and completion; PostgreSQL 16/Testcontainers with Flyway V1→V46 (49 migrations) and absent V47; exact-date/day and inclusive/open validity; overlapping active assignment versions fail whole read; SELECT-only 42501 controls; five-statement closed catalog/unexpected SQL; nonconstant nominal data checksum before/after; architecture and runtime isolation; justified individual-owner RR concurrency snapshot stability; R1/R2 regressions because inspector/checksum shared; detector regression; bounded Lane 1–4 regressions; full repository regression; `git diff --check` and scope proof. Do not add migration tests for R3 or assert V47 overlap constraint.

Product boundary: TurnoInstructor `LEGACY_VIVO / PRODUCTIVO`; productive Programación authority unchanged; R3 dark launch only, productive routing absent; client API/web/reservations unchanged. R4–R6 and cutover not authorized. Flyway V46, V47 absent, migration none, `programacion_ajuste_fecha` out of scope. Payments and Notifications out of scope.

## Proposed exact implementation allowlist — inactive

The following **future** sets are canonical repository-relative file paths; there are no wildcard or directory grants. `NEW` paths were absent at base; `MODIFIED` and `READ_ONLY` paths were present. This handoff itself grants no current implementation mutation authority.

### CURRENT_R3_AUTHORIZED_NEW

```text
src/main/java/com/feelingpilates/transicion/programacion/adapter/jpa/NominalJpaReader.java
src/main/java/com/feelingpilates/transicion/programacion/adapter/jpa/mapper/NominalProjectionMapper.java
src/main/java/com/feelingpilates/transicion/programacion/adapter/jpa/projection/NominalProjectionCatalog.java
src/main/java/com/feelingpilates/transicion/programacion/adapter/jpa/projection/NominalProjectionQueryExecutor.java
src/main/java/com/feelingpilates/transicion/programacion/adapter/jpa/projection/NominalProjectionRow.java
src/main/java/com/feelingpilates/transicion/programacion/read/NominalBackingSnapshot.java
src/main/java/com/feelingpilates/transicion/programacion/read/NominalProgrammingReadPort.java
src/main/java/com/feelingpilates/transicion/programacion/read/NominalProgrammingReadSet.java
src/main/java/com/feelingpilates/transicion/programacion/read/NominalReadFailure.java
src/main/java/com/feelingpilates/transicion/programacion/read/NominalReadSnapshotContext.java
src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/NominalJpaReaderArchitectureTest.java
src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/NominalJpaReaderPostgreSqlTest.java
src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/NominalJpaReaderRuntimeIsolationTest.java
src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/NominalJpaReaderTransactionTest.java
src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/NominalProjectionMapperTest.java
src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/NominalProjectionQueryExecutorTest.java
src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/testinfra/NominalPostgresTestConfiguration.java
src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/testinfra/NominalTransactionTestOwner.java
```

### CURRENT_R3_AUTHORIZED_MODIFIED

```text
src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/testinfra/F2eSliceChecksum.java
src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/testinfra/F2eStatementPolicyInspector.java
```

Only additive R3 methods/factory are candidates. Existing R1/R2 contracts and assertions must retain their behavior.

### CURRENT_R3_READ_ONLY

```text
auditoria/fase-2e-r3-diseno-reader-programacion-nominal-reconciliado.md
src/main/java/com/feelingpilates/programacion/dominio/ReferenciaOcurrencia.java
src/main/java/com/feelingpilates/transicion/programacion/adapter/jpa/LegacyTurnJpaReader.java
src/main/java/com/feelingpilates/transicion/programacion/adapter/jpa/ReservaJpaReader.java
src/main/java/com/feelingpilates/transicion/programacion/adapter/jpa/policy/F2eSqlPolicyViolationException.java
src/main/java/com/feelingpilates/transicion/programacion/detector/DetectorVocabulary.java
src/main/java/com/feelingpilates/transicion/programacion/detector/EvidenceProvenance.java
src/main/java/com/feelingpilates/transicion/programacion/detector/ProgrammingCandidateSnapshot.java
src/main/java/com/feelingpilates/transicion/programacion/read/LegacyTurnReadContext.java
src/main/java/com/feelingpilates/transicion/programacion/read/ReadSnapshotContext.java
src/main/java/com/feelingpilates/transicion/programacion/read/ReadSnapshotIdentifiers.java
src/main/resources/db/migration/V41__programacion_bloque_asignacion.sql
src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/testinfra/F2ePostgresTestConfiguration.java
src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/testinfra/F2eSelectOnlyRole.java
src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/testinfra/LegacyTurnJdbcCapture.java
src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/testinfra/LegacyTurnTransactionTestOwner.java
src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/testinfra/ReaderTransactionTestHarness.java
```

### CURRENT_R3_PROVENANCE_ONLY

```text
auditoria/ESTADO-ACTUAL.md
auditoria/fase-2e-diseno-adapters-read-only-snapshot-consistency.md
auditoria/handoffs/HANDOFF-F2E-R2-IMPLEMENTACION-LECTOR-TURNO-LEGACY-CLEAN-MAIN-RECONCILIATION.md
auditoria/reviews/F2E-R3-REVIEW-DISENO-READER-PROGRAMACION-NOMINAL-RECONCILIADO.md
```

Provenance files supply historical/process evidence only; they are not implementation dependencies. The absent historical V47 object has no repository path grant.

### DEFAULT_DENY

Every repository path **not** explicitly in `CURRENT_R3_AUTHORIZED_NEW` or `CURRENT_R3_AUTHORIZED_MODIFIED` is write-denied for future implementation. `READ_ONLY` paths may be read but never modified. `PROVENANCE_ONLY` paths are historical context, not implementation dependencies. Any future need for an unlisted path requires `STOP / HUMAN_GATE` and a new reviewed allowlist. Current implementation remains unauthorised even on proposed write paths.

### Path-set hashes

Algorithm: take canonical repository-relative paths in each set, sort lexicographically by UTF-8 byte sequence, join with a single `\n`, append a final `\n`, encode UTF-8, compute SHA-256. Union sets are de-duplicated before sorting. No path contents enter these hashes. The handoff audit must recompute them from the fenced lists above.

| Set | Count | SHA-256 |
| --- | ---: | --- |
| CURRENT_R3_AUTHORIZED_NEW | 18 | `594afab1a5107aa5ab22d35dfa9409249d7e7caecbbb4588b926f23b8146ce07` |
| CURRENT_R3_AUTHORIZED_MODIFIED | 2 | `2168358dbf50ccae22d0bcfa9d15499e26e3d1f3e5a0fd5d16669860c0c71f29` |
| WRITE_SCOPE (NEW ∪ MODIFIED) | 20 | `b1a7180e430b916b56a87363a2a01992f0cf8c3b55af2a0011a0f62ce4e4e12a` |
| CURRENT_R3_READ_ONLY | 17 | `1deff2786d5b591ec044c2249bda7eb6c1f30c7e7771e9770f040cf37e9d091e` |
| CURRENT_R3_PROVENANCE_ONLY | 4 | `17786d6343b0b676797a85d829fc727db13e62208f3861ca4ec96777b740e31e` |
| TOTAL_ACTIVE_PATHS (WRITE_SCOPE ∪ READ_ONLY) | 37 | `bb86bc48c4ea9f9fb5e64249ec8bd46070a12705048a74b7b9b4cfa7fa9ccb8d` |

## Gate siguiente

New chat, fresh independent R3 implementation handoff audit. No independent audit was run here. Only after separate approval/publication/activation could a future implementation lifecycle use these proposed paths. No push, PR, merge, deployment, migration, R3 activation or product cutover in this lifecycle.
