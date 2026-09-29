# F2E R3 — diseño reconciliado del reader de programación nominal

## Estado y autoridad

```text
Tipo: DESIGN / RECONCILIATION
Base canónica: 2422ee555f6e49e7edb27bfca9c71f66c2ef2c7a
Estado: CORRECTION1_CANDIDATE / PENDING_FRESH_INDEPENDENT_DESIGN_REAUDIT / NOT_APPROVED / NOT_PUBLISHED
Implementación R3: NOT_AUTHORIZED
V47: ABSENT / NOT_AUTHORIZED
Flyway canónico: V46
TurnoInstructor: LEGACY_VIVO / PRODUCTIVO
Dark launch: PRESERVED
Cutover, R4-R6: NOT_AUTHORIZED
Pagos / Notificaciones: OUT_OF_SCOPE
```

Este documento reconcilia el subconjunto histórico R3 con el esquema canónico actual. No aprueba el diseño histórico completo ni autoriza código, tests, configuración, migración, acceso/escritura a base de datos, activación o routing productivo.

## Procedencia inspeccionada

- `auditoria/fase-2e-diseno-adapters-read-only-snapshot-consistency.md`, especialmente §§9, 12.1–12.2, 19, 33.3 y 34.
- `auditoria/reviews/F2E-ADAPTERS-SNAPSHOT-DESIGN-REVIEW.md`, que conserva R3 como candidato no autorizado y el audit histórico del diseño amplio.
- `auditoria/fase-2e-identidad-semantica-detector-read-only.md`, especialmente §§4, 5, 8.1 y la matriz de identidad E/F.
- Autoridad transaccional histórica §§15, 17–18 y enmienda R1 §37.9 del diseño adapters; diseño R2 §6.1–6.3; `ReadSnapshotContext.java`, `ReservaJpaReader.java` y `LegacyTurnJpaReader.java` actuales. R1 usa manager `f2eReaderTransactionManager`/`MANDATORY` con owner test `READ_COMMITTED`; R2 usa manager `f2eR2ReaderTransactionManager`/`MANDATORY` con owner test `REPEATABLE_READ`. Ninguno autoriza por sí mismo una composición R6.
- Canonical migrations V41–V46; entities `Asignacion`/`BloqueProgramacion`, `ReferenciaOcurrencia`; `AsignacionRepository` actual.
- Contenido histórico de `V47__programacion_ajustes_fecha.sql`, leído directamente del objeto Git `95900d8` (no materializado en el worktree). La copia histórica declara F2D.2 y no es autoridad para restaurar V47.

Los nombres ingleses del port/read-set/row se retienen porque el contrato histórico los fija como nombres conceptuales exactos de R3. Los nombres físicos existentes tampoco se renombran.

## HISTORICAL_R3_CONTRACT

### Explícito en diseño histórico

- **Propósito:** reader de solo lectura de ocurrencias nominales nuevas, entregadas como `ProgrammingCandidateSnapshot` de tipo `NOMINAL_OCCURRENCE` y provenance/backing inmutable. No produce programación efectiva.
- **Port/operación:** `readNominalOnDate(ReadSnapshotContext, LocalDate fecha)`; fecha exacta obligatoria.
- **Consulta:** executor dedicado `NominalProjectionQueryExecutor`, SQL nativa, `EntityManager`, binding escalar tipado; no modificar, envolver ni reutilizar `AsignacionRepository` para este contrato.
- **Fuentes:** `programacion_asignacion` unida por `bloque_id` a `programacion_bloque`; las dos filas deben estar activas, sus vigencias contienen la fecha y el bloque coincide con el día de semana derivado (0=domingo).
- **Salida:** `NominalProgrammingReadSet(candidates, backing)`, inmutable; 0..N, con máximo una nominal por serie de asignación y fecha; cero filas es una lectura válida del universo.
- **Identidad:** `ReferenciaOcurrencia(SERIE_ASIGNACION, a.serie_id, fecha)`. La fila/version física de asignación y la de bloque son provenance, no identidad de occurrence.
- **Projection:** series/IDs físicos; salón, instructor, actividad, horas nominales; horas del bloque; ambas vigencias y flags activos; timestamps técnicos de ambas filas; convención de día. Orden SQL por `a.serie_id, a.id`.
- **Validez:** vigencias inclusivas; `vigente_hasta = NULL` significa extremo abierto. Rechazar nulls físicos inesperados, rangos no positivos, asignación fuera del bloque, discrepancia de fecha/día y duplicidad de `serieId` como input/invariante inválida, sin éxito parcial.
- **Snapshots:** sólo valores escalares/records inmutables; ninguna entity, proxy o colección managed cruza el límite. Lectura sin escrituras.
- **R1/R2:** no se usan sus read sets/readers como fuente de datos. Infraestructura sólo se comparte donde se declare expresamente.
- **Boundary:** no ajustes, composición efectiva, comparación/reconciliación cross-source ni coordinación.
- **Aceptación histórica:** mapping/query/vigencias/cardinalidad/provenance/duplicados/no-write y aceptación PostgreSQL; esta lista amplia histórica requiere ajuste por la diferencia V47 que se resuelve abajo.

### Derivado con evidencia histórica

- Candidate identity es la referencia lógica de serie-fecha; `a.id` y `b.id` identifican las versiones físicas leídas.
- Las columnas nominales del candidate son salón del bloque y asignación de instructor/actividad/hora. El read set conserva además el backing completo como evidencia de aplicabilidad y trazabilidad.
- Varias series en una fecha son válidas; sólo una repetición de la misma serie en esa fecha rompe la cardinalidad R3.
- Timestamps de creación/actualización son exclusivamente técnicos; nunca delimitan vigencia ni orden de preferencia.

### No especificado históricamente

- Política para estado preexistente contradictorio que V46 no impide: se cierra aquí mediante detección de duplicados aplicables durante la lectura y rechazo completo, sin migración ni reparación.
- Propagación/isolation exactas y owner de transacción del R3 aislado; se fijan abajo usando infraestructura aceptada R1/R2, sin atribuirle a esos readers datos R3.
- Hash/slice concreto de R3 en el harness compartido; requiere extensión aislada descrita abajo.

## CURRENT_V46_SCHEMA_CAPABILITIES

| Necesidad R3 | Estado en V46 | Evidencia/efecto |
| --- | --- | --- |
| Tablas base de bloques y asignaciones, FK asignación→bloque y FKs a salón/usuario/actividad | AVAILABLE_IN_V46 | V41 crea ambas tablas y relaciones. |
| IDs físicos, series, día, horas, flags, fechas de vigencia y timestamps | AVAILABLE_IN_V46 | V41 contiene todos los campos requeridos. |
| Rangos internos válidos y día 0–6 | AVAILABLE_IN_V46 | V41 CHECK de horas/vigencia y día. |
| Índices por bloque, instructor/vigencia y series; bloque salón/día/vigencia y serie | AVAILABLE_IN_V46 | V41. Los índices apoyan filtros generales; ninguno establece unicidad por serie/fecha. |
| Filtro de dos activos + vigencias inclusivas + día exacto | AVAILABLE_IN_V46 | Columnas presentes y semántica ejecutable en SQL. |
| Una versión de asignación activa por `serie_id` en fecha dada | NOT_ENFORCED_IN_V46 | No hay UNIQUE/EXCLUDE equivalente en V41–V46. R3 impone la cardinalidad del read set exitoso detectando repetición aplicable y fallando cerrado. Un estado V46 inválido sigue siendo posible. |
| Una única fila física de bloque por serie/fecha | PARTIALLY_AVAILABLE_IN_V46 | No hay exclusión por serie de bloque. R3 no define serie de bloque como cardinalidad candidate; `bloque_id` es provenance y cada asignación referencia un bloque único. La repetición de candidate assignment-series sigue gobernada por la regla de asignación. |
| `btree_gist` | AVAILABLE_IN_V46 | V44 instala extensión requerida para EXCLUDE; V45 la usa con `daterange` en horario. La extensión sola no añade garantía a asignaciones. |
| Esquema de ajustes por fecha | ABSENT_UNTIL_HISTORICAL_V47 | No se necesita para nominal R3; consumer R4+. |
| Policies de salón (V42), horarios versionados (V43–V46) | NO_LONGER_APPLICABLE | No forman parte del universo de ocurrencias nominales R3 ni validan candidatos R3. |

## HISTORICAL_V47_DECOMPOSITION

La migración histórica contiene tres efectos conceptuales. Ninguno autoriza restaurar el archivo.

| Operación histórica | Objeto/propósito | Consumidor | ¿Lo usa R3? | Equivalente en V46 |
| --- | --- | --- | --- | --- |
| Bloque `DO`: detectar solapamientos de `daterange(vigente_desde, vigente_hasta, '[]')` entre asignaciones activas de igual `serie_id`; abortar sin reparar filas | Preflight histórico para instalar exclusión | Integridad general del versionado | No. PROVENANCE_ONLY; R3 detecta duplicados aplicables en su resultado | No. V46 no ejecuta ese preflight. |
| `ALTER TABLE programacion_asignacion ADD CONSTRAINT ex_programacion_asignacion_serie_vigencia EXCLUDE USING gist (serie_id WITH =, daterange(...) WITH &&) WHERE (activo)` | Evita versiones activas solapadas de igual serie | Integridad general del versionado | No. PROVENANCE_ONLY / NOT_R3_PREREQUISITE | No. R3 falla cerrado si aparecen dos versiones aplicables. |
| `CREATE TABLE programacion_ajuste_fecha` con PK/FKs, checks de tipo y forma, vigencia puntual implícita por fecha, flags/timestamps | Persistencia de cancelación/reemplazo/adición por fecha | R4 y composición posterior R5 | No | No, pero no es necesidad R3. |
| Índice único parcial `(asignacion_serie_id, fecha)` activo para cancelación/reemplazo | Un ajuste nominal targeteado por serie/fecha | R4/R5 | No | No, fuera de contrato R3. |
| Índices parciales de ajustes por salón/fecha, instructor/fecha y fecha | Lecturas de ajustes por fecha y dimensiones | R4/R5 | No | No, fuera de contrato R3. |
| Habilitación/uso de `btree_gist` para exclusión de asignaciones | Soporte técnico histórico de la restricción | Integridad general | No. PROVENANCE_ONLY | Sí: extensión presente desde V44, sin exclusión de asignaciones. |

La constraint histórica no es requisito de R3. La tabla y los índices de ajustes pertenecen a R4+ y quedan fuera de este diseño.

## R3_V47_DECISION

**`R3_CAN_FAIL_CLOSED_ON_V46_WITHOUT_SCHEMA_CHANGE`.**

Evidencia: el contrato histórico exige máximo una occurrence por `serie_id`/fecha **en una lectura exitosa** y rechazo de duplicados. V46 contiene todas las tablas/columnas para consultar las filas aplicables, aunque no impide solapamientos activos. La consulta expone todas las versiones físicas; R3 detecta repetición lógica y descarta la lectura completa. No selecciona una versión, deduplica, repara ni presenta un vacío exitoso. La ausencia de exclusión de esquema es una condición conocida de la autoridad V46, no una afirmación de limpieza de sus datos.

Contrato canónico R3: Flyway `V1→V46` (49 migraciones), migración nueva **NONE**, dependencia de autoridad de migración **NONE**, V47 **NOT_REQUIRED_FOR_R3 / ABSENT**, `programacion_ajuste_fecha` **R4+ / OUT_OF_SCOPE**. La exclusión histórica V47 queda como procedencia, sin requisito de lifecycle R3.

## NOMINAL_OCCURRENCE_SEMANTICS

- **Serie fuente:** `programacion_asignacion.serie_id`; es identidad lógica de la recurrencia. No es `a.id`, `b.id` ni `b.serie_id`.
- **Fecha:** la `LocalDate` exacta solicitada. La fecha también integra la referencia de occurrence.
- **Día:** derivado determinísticamente de fecha según convención canónica Java/PostgreSQL 0=domingo…6=sábado; se compara con `b.dia_semana`.
- **Vigencia:** `a.vigente_desde <= fecha AND (a.vigente_hasta IS NULL OR fecha <= a.vigente_hasta)` y la misma condición para `b`; extremos inclusivos.
- **Actividad:** ambas filas deben tener `activo = true`.
- **Bloque:** `a.bloque_id = b.id`. La hora de asignación debe ser positiva y estar dentro del intervalo positivo de bloque; no se recorta.
- **Candidate fields:** `b.salon_id`, `a.instructor_id`, `a.tipo_actividad_id`, `a.hora_inicio`, `a.hora_fin`.
- **Candidate identity/reference:** `ReferenciaOcurrencia(SERIE_ASIGNACION, a.serie_id, fecha)`. No usar `AJUSTE`; éste requiere autoridad y semántica posterior.
- **Provenance/backing:** IDs de ambas versiones, ambas series, relación bloque, horas de bloque y asignación, ambas vigencias/activos, fecha/día derivado y timestamps técnicos. Backing no altera identidad ni orden de preferencia.
- **Orden:** serie de asignación ascendente, luego `a.id` ascendente para diagnóstico determinista. El orden no expresa autoridad.
- **Duplicados:** para la `LocalDate` exacta solicitada, si el resultado contiene más de una fila aplicable de `programacion_asignacion` activa con el mismo `a.serie_id` lógico (normalmente distintos `a.id` físicos/versiones), la lectura completa falla `DUPLICATE_SERIES_ON_DATE`. Se conservan privadamente los IDs físicos `a.id`, `a.bloque_id`/`b.id`, ambas series, flags y vigencias de las filas para diagnosticar el conflicto; no se publica candidate ni read set. Varios candidates de series diferentes sí son válidos.
- **Datos inválidos:** no se convierten en ausencia. Cualquier forma inesperada impide éxito del read set; error de input/invariante tipado y fail-closed.

## QUERY_CONTRACT

Una única SQL nativa de proyección en el executor aislado:

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

Bind `fecha` as `LocalDate`, day as derived scalar 0–6, and active flags as `true`, using named typed scalar binding consistent with accepted reader infrastructure. No user-provided lists/IDs, entity hydration, adjustments, salon/instructor/activity master joins, locks or writes. Expose **every** applicable physical assignment/version row and its block provenance to the mapper; no `DISTINCT`, `DISTINCT ON`, deduplicating `GROUP BY`, `LIMIT 1` per series, winner-selecting window filter or application deduplication before duplicate validation. `ORDER BY` is diagnostic only, never winner selection. Validate all row shapes/ranges/containment, then detect repeated `serie_id` over the complete result before constructing any successful output. Zero rows is a valid empty universe only when the query completes successfully.

The query cannot see malformed rows excluded by applicability predicates (for example inactive or out-of-date rows); R3’s claim is the set applicable under those predicates. V46 does not prevent active overlap. The read-time duplicate check is therefore mandatory and fails the whole operation. No filtering by productive reservation state or runtime authority is allowed.

## PORT_READSET_CONTRACT

Conservar los nombres históricos de operación/read set/proyección; reconciliar sólo el tipo del contexto, como se explica abajo:

```text
NominalProgrammingReadPort
  readNominalOnDate(NominalReadSnapshotContext context, LocalDate fecha)
      -> NominalProgrammingReadSet

NominalProgrammingReadSet
  candidates: immutable ordered List<ProgrammingCandidateSnapshot>
  backing: immutable ordered List<NominalBackingSnapshot>

NominalProjectionRow
  concrete scalar projection of the columns in QUERY_CONTRACT
```

La firma histórica usaba el nombre conceptual `ReadSnapshotContext`; la clase Java canónica actual está sellada a `R1_RESERVA_V1` y `SINGLE_READER_TEST`. No se puede pasar a R3 ni ampliar sus enums sin quebrar la autoridad R1. La decisión R3 es un contexto propio `NominalReadSnapshotContext` persistence-agnostic, según el precedente R2, con el mismo papel de carrier de identidad y evidencia; ésta es una reconciliación explícita de la firma histórica, no una dependencia del contexto de Reserva. El futuro handoff debe materializar esta firma exacta y dejar intacto el tipo R1.

`ProgrammingCandidateSnapshot` is the existing detector vocabulary, candidate type exactly `NOMINAL_OCCURRENCE`, reference type exactly `SERIE_ASIGNACION` including exact `LocalDate`. Candidate and backing collections have equal cardinality and deterministic row order; backing must correspond one-to-one by `ReferenciaOcurrencia`, while physical version IDs distinguish evidence versions. One logical series/date occurs at most once in a successful read set. There is no partial-success result. Use defensive copies and immutable scalar values.

Modelo de fallo tipado conceptual, sin crear clases ahora: `INVALID_INPUT` (fecha/contexto inválido, antes de SQL); `TRANSACTION_CONTEXT_INVALID` (owner, manager, recurso, aislamiento, snapshot o evidencia inválidos); `MALFORMED_PROJECTION` (fila/tipo/rango/relación inválidos); `DUPLICATE_SERIES_ON_DATE` (más de una versión aplicable para la serie/fecha); `CARDINALITY_OR_BACKING_MISMATCH` (candidates/backing o referencia no biyectivos); `DATABASE_READ_FAILURE` (JDBC/JPA/política SQL/privilegios/completion). Son categorías de rechazo, no nombres de excepciones nuevas. Un fallo operacional jamás se convierte en read set vacío ni en rechazo semántico del detector.

## TRANSACTION_SNAPSHOT_CONTRACT

### Owner, propagation y llamadas permitidas

- El método público proxied R3 usa exactamente `@Transactional(transactionManager="f2eReaderTransactionManager", propagation=MANDATORY, readOnly=true)`. Es el nombre de manager compartible fijado por la enmienda R1 §37.9.2; en aceptación R3 un ApplicationContext de prueba aislado registra un bean R3 con ese nombre, sin alterar el bean/owner R1 de su contexto de prueba ni registrar uno productivo. El reader nunca abre, suspende, reemplaza ni reintenta una transacción, ni cambia su isolation. Invocarlo directamente sin transacción owner, por `new`/self-invocation o con manager ausente/incorrecto está prohibido y falla antes de DATA.
- Una invocación R3 individual sólo está permitida **dentro** de un owner R3 explícito. Para aceptación R3 el owner test-only, proxied y separado del reader, declara `@Transactional(transactionManager="f2eReaderTransactionManager", propagation=REQUIRES_NEW, isolation=REPEATABLE_READ, readOnly=true)` con timeout acotado. Abre y cierra una transacción física sobre el datasource PostgreSQL SELECT-only R3, crea el contexto tras probes válidos y retiene el read set provisional hasta validar snapshot/recurso/completion. Ningún caller abre por accidente una transacción mediante el port. Una sola query bajo RR tiene snapshot PostgreSQL estable durante esa transacción, pero este modo prueba sólo la lectura nominal individual; no declara simultaneidad con R1/R2 ni un claim multi-reader.
- Para una **futura** evaluación multi-reader, el único owner conceptual es `DetectorReadCoordinator` de R6, todavía **NOT_AUTHORIZED**. Su método público proxied deberá abrir una sola transacción física `REQUIRES_NEW / REPEATABLE_READ / readOnly=true / timeout acotado` con el mismo manager `f2eReaderTransactionManager`. R3 y cada reader participante deberán resolver ese **mismo** manager y datasource/EMF/Session/Connection físicos con `MANDATORY`, sin segunda transacción ni cambio de snapshot. El owner captura `pg_current_snapshot()::text` inicial y final dentro de esa transacción y exige igualdad textual; todos los statements/resultados quedan ligados al mismo recurso y evidencia. Sólo tras validar cada read set, el conjunto, los probes finales y la completion se puede publicar un resultado. Una evaluación con R1/R2 actuales no es ejecutable: sus contextos/claims están sellados a pruebas individuales y R2 nombra otro manager; no se reinterpretan como autorización R6. Una futura integración requiere autoridad separada de R6 y reconciliación de esos contratos, sin modificar este principio de ownership R3.
- El owner valida al inicio y al final `transaction_isolation=repeatable read`, `transaction_read_only=on`, identidad de manager/descriptor/DS/EMF/shared EM/Session, conexión JDBC y `PgConnection` nativos, database/schema/principal y texto de snapshot; comprueba misma conexión antes/después de DATA. El reader verifica transacción activa, read-only y RR y la asociación de su contexto con la reserva privada del owner antes de SQL. Manager/resource/owner/snapshot equivocados o faltantes son `TRANSACTION_CONTEXT_INVALID`; no fallback a manager default, RC, otro recurso o un segundo SELECT independiente. Las verificaciones de recurso/probes son evidencia interna del owner y deben formar parte del catálogo cerrado de SQL cuando emiten SQL.

### `NominalReadSnapshotContext` y confianza

- El caller puede aportar `runIdentity`, `attemptIdentity`, fecha exacta, `businessZone` y versión de reglas, sujetos a validación de forma y coherencia con el scope. No puede elegir `sourceName`, huella de esquema, catálogo R3, `readerInvocationIdentity`, claim, evidencia de snapshot ni compromiso de observación de statements.
- El owner deriva fecha/día/scope canónico del input tipado; obtiene `sourceName`, `schemaFingerprint` de un descriptor R3 validado contra Flyway V46 y recurso real; fija catálogo de proyección R3; reserva una identidad de invocación; crea el contexto R3 sólo tras abrir la TX y validar probes/recurso iniciales. El claim individual es exclusivamente `R3_INTERNAL_RR_TEST`; el `snapshotEvidenceId` se deriva de descriptor, invocación/owner, manager, aislamiento/read-only y texto exacto de `pg_current_snapshot()` observado. El compromiso de statements se liga al catálogo/capture real y se verifica al final. Campos de contexto son evidencia transportada, **no prueba por construcción**.
- El reader compara valores del contexto con descriptor/catálogo/scope y con la asociación privada de owner a esa transacción, recurso y snapshot observados. Un objeto equivalente fabricado por caller o reutilizado en otra TX/attempt no pasa. `logicalSnapshotId`, hashes de contenido o igualdad de strings aportados por caller nunca sustituyen probes PostgreSQL y mismo recurso físico. Para multi-reader el owner R6 futuro deberá crear una evidencia de claim distinta, autorizada para todos los participantes; `R3_INTERNAL_RR_TEST` no se promociona a `MULTI_READER_MVCC`.
- Los snapshots candidatos/backing son records escalares inmutables antes de salir de la transacción; ninguna entity/proxy/Session escapa. Shared testinfra sólo puede extenderse aditivamente para slice R3, catálogo y ownership, sin alterar R1/R2.

## NO_WRITE_CONTRACT

Future acceptance must prove all of the following against real PostgreSQL/Testcontainers with canonical Flyway `V1→V46` (49 migrations), `V47` absent and no R3 migration:

- Dedicated SELECT-only role has `SELECT` only on allowlisted `programacion_asignacion` and `programacion_bloque` (and only the exact required built-in transaction/resource probes); no INSERT/UPDATE/DELETE/TRUNCATE/DDL/sequence/write privileges or adjustment-table access.
- Attempts at forbidden writes fail with PostgreSQL insufficient-privilege SQLState `42501`; do not swallow or translate into success/empty read.
- Closed SQL catalog allows the exact SELECT projection (and required metadata inspection) and rejects unrecognized SQL fail-closed. No repository writer reachable from R3.
- Verify schema and migration-history checksum of the V46 R3 slice (columns, keys, constraints, indexes and applied migrations); do not expect an assignment exclusion constraint. A scoped data checksum where appropriate proves fixture integrity, not business validity.
- A V46 fixture with two overlapping active assignment versions of one `serie_id`, both applicable on the requested day, must return all physical rows to validation and fail `DUPLICATE_SERIES_ON_DATE` with no read set. Query, mapper and architecture acceptance proves no entity/proxy escape, no flush/write, correct empty and multi-series results, owner/reader propagation, snapshot/resource guards and full output discard on failure.

## FUTURE_IMPLEMENTATION_SHAPE — advisory only

- **Potential new:** R3 read port y `NominalReadSnapshotContext` propios; immutable read set/backing snapshot; `NominalProjectionRow`; plain native query executor; mapper; plain JPA reader; R3-specific unit/architecture/PostgreSQL acceptance tests and isolated test configuration.
- **Potential modified:** shared testinfra only through additive R3 registration, V46 metadata slice checksum and SQL catalog entries. No migration or migration test is part of R3.
- **Read-only dependencies:** R1 `ReadSnapshotContext` as a boundary precedent only; `ProgrammingCandidateSnapshot`, `ReferenciaOcurrencia`, `programacion_asignacion`, `programacion_bloque`, accepted transaction/testinfra contracts.
- **Provenance-only:** historical V47 object and broad adapter design; no production dependency on `programacion_ajuste_fecha` or V47 adjustment indexes.

No concrete paths are frozen beyond existing read-only dependencies because the implementation allowlist has not been reconciled/authorized.

## BOUNDARIES

- **R1/R2:** neither reader nor its read set is a data dependency. Shared transaction/testinfra primitives may be reused with additive R3 slice support and no change to R1/R2 semantics.
- **Detector:** R3 provides nominal candidates and physical backing/provenance for an exact date. It does not compare sources, reconcile reservations or legacy turns, compose effective programming, select mappings, or coordinate a run.
- **R4/R5/R6:** R4 owns exact-date adjustment reads; R5 effective composition; R6 cross-source coordination/shadow read set. No adjustment schema, `AJUSTE` reference, effective result, comparator or coordinator moves into R3. Historical V47’s R4 schema is excluded by this reconciliation.
- **Product:** TurnoInstructor remains `LEGACY_VIVO / PRODUCTIVO`; programming authority and runtime routing unchanged; dark launch preserved; client API/web/reservations unchanged; no productive jobs, cutover, payments/notifications, deployment or R3-R6 activation.

## FUTURE_VALIDATION_CONTRACT

Applicable eventual categories: unit mapping; exact-date/day derivation; inclusive validity/open end; range/containment; query binding and all physical versions; zero/multiple-series cardinality; duplicate-overlap fixture fail-closed; deterministic order; backing/candidate one-to-one; transaction ownership and PostgreSQL snapshot consistency; Testcontainers on `V1→V46`/49 migrations with V47 absent; SELECT-only role and SQLState `42501`; closed SQL catalog/unexpected SQL rejection; V46 slice/data checksum; architecture isolation/no runtime reachability; cross-lane and authorized full regression. No R3 migration test or conflict preflight is required. No tests or suites were run or added here.

## Unresolved issues and next gate

The two P1 decisions are closed at design level: V46 read-time rejection replaces the unauthorized migration prerequisite, and the R3 reader/owner propagation is fixed above. A future multi-reader R6 integration remains outside current authority; its owner and compatibility gates are defined above, not silently implemented. Before any R3 implementation handoff, a fresh independent design re-audit must accept this correction, followed by design publication. Invalid active overlap in V46 stops the R3 read without repair or partial output.

```text
R3 design: CORRECTION1_CANDIDATE / PENDING_FRESH_INDEPENDENT_DESIGN_REAUDIT / NOT_APPROVED / NOT_PUBLISHED
R3 implementation: NOT_AUTHORIZED
R4-R6: NOT_AUTHORIZED
```
