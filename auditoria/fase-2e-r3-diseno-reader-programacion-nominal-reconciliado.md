# F2E R3 — diseño reconciliado del reader de programación nominal

## Estado y autoridad

```text
Tipo: DESIGN / RECONCILIATION
Base canónica: 2422ee555f6e49e7edb27bfca9c71f66c2ef2c7a
Estado: MATERIALIZED_CANDIDATE / PENDING_FRESH_INDEPENDENT_DESIGN_AUDIT / NOT_APPROVED / NOT_PUBLISHED
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

- Política para estado preexistente contradictorio que el motor de base de datos no impide (en particular solapamientos activos por serie antes de instalar garantía física): se cierra aquí como preflight obligatorio y rechazo fail-closed.
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
| Una versión de asignación activa por `serie_id` en fecha dada | ABSENT_UNTIL_HISTORICAL_V47 | No hay UNIQUE/EXCLUDE equivalente en V41–V46. R3 requiere la garantía para su cardinalidad nominal. |
| Una única fila física de bloque por serie/fecha | PARTIALLY_AVAILABLE_IN_V46 | No hay exclusión por serie de bloque. R3 no define serie de bloque como cardinalidad candidate; `bloque_id` es provenance y cada asignación referencia un bloque único. La repetición de candidate assignment-series sigue gobernada por la regla de asignación. |
| `btree_gist` | AVAILABLE_IN_V46 | V44 instala extensión requerida para EXCLUDE; V45 la usa con `daterange` en horario. La extensión sola no añade garantía a asignaciones. |
| Esquema de ajustes por fecha | ABSENT_UNTIL_HISTORICAL_V47 | No se necesita para nominal R3; consumer R4+. |
| Policies de salón (V42), horarios versionados (V43–V46) | NO_LONGER_APPLICABLE | No forman parte del universo de ocurrencias nominales R3 ni validan candidatos R3. |

## HISTORICAL_V47_DECOMPOSITION

La migración histórica contiene tres efectos conceptuales. Ninguno autoriza restaurar el archivo.

| Operación histórica | Objeto/propósito | Consumidor | ¿Lo usa R3? | Equivalente en V46 |
| --- | --- | --- | --- | --- |
| Bloque `DO`: detectar solapamientos de `daterange(vigente_desde, vigente_hasta, '[]')` entre asignaciones activas de igual `serie_id`; abortar sin reparar filas | Preflight de integridad para poder añadir exclusión sin ocultar conflictos existentes | R3 cardinalidad nominal y cualquier consumidor de versiones de asignación | Sí, como precondición para imponer la garantía; no es query de lectura | No. V46 no ejecuta este preflight de programación. |
| `ALTER TABLE programacion_asignacion ADD CONSTRAINT ex_programacion_asignacion_serie_vigencia EXCLUDE USING gist (serie_id WITH =, daterange(...) WITH &&) WHERE (activo)` | Evita versiones activas de igual serie con vigencias inclusivas solapadas | Integridad general del versionado; R3 consume la unicidad por serie/fecha | Sí. Necesaria para respaldar máximo 1 nominal por serie/fecha | No. V41–V46 sólo tienen checks de rango e índices no únicos. |
| `CREATE TABLE programacion_ajuste_fecha` con PK/FKs, checks de tipo y forma, vigencia puntual implícita por fecha, flags/timestamps | Persistencia de cancelación/reemplazo/adición por fecha | R4 y composición posterior R5 | No | No, pero no es necesidad R3. |
| Índice único parcial `(asignacion_serie_id, fecha)` activo para cancelación/reemplazo | Un ajuste nominal targeteado por serie/fecha | R4/R5 | No | No, fuera de contrato R3. |
| Índices parciales de ajustes por salón/fecha, instructor/fecha y fecha | Lecturas de ajustes por fecha y dimensiones | R4/R5 | No | No, fuera de contrato R3. |
| Habilitación/uso de `btree_gist` para exclusión de asignaciones | Soporte técnico de la restricción | R3/general | Sí, el soporte está ya presente desde V44 | Sí: extensión disponible; falta sólo la constraint. |

La constraint es una regla de integridad compartida del versionado que R3 necesita para sustentar su cardinalidad. La tabla y los índices de ajustes no deben adelantarse a R4/R5.

## R3_V47_DECISION

**B. `R3_REQUIRES_A_SMALL_NEW_SCHEMA_CHANGE_BUT_NOT_HISTORICAL_V47`.**

Evidencia: el contrato histórico exige máximo una versión activa por serie de asignación en la fecha y falla ante duplicidad; V41–V46 no restringe solapamientos activos entre filas de `programacion_asignacion`; el SQL de lectura no puede convertir un estado duplicado en una nominal confiable. La mitad R3-relevante de V47 es únicamente el preflight de conflictos preexistentes y la exclusión por serie/rango inclusivo sobre `programacion_asignacion`. `btree_gist` ya existe por V44. La tabla de ajustes, sus checks, índices y unique parcial son scope R4/R5 y quedan excluidos.

Diseño futuro condicionado: una migración nueva, con numeración posterior que se resuelva en lifecycle autorizado, debe validar primero conflictos existentes y fallar sin reparar; sólo después instalar una restricción equivalente acotada a `programacion_asignacion`. No es autorización para escribir migración ni para elevar Flyway desde V46 en este ciclo. No se declara aquí SQL ejecutable ni WRITE_SCOPE.

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
- **Duplicados:** varios candidates de series diferentes permitidos. Una repetición de la misma serie-fecha, aunque provenga de diferente `a.id`, es invariante rota y aborta la lectura completa; no deduplicar ni elegir.
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

Bind `fecha` as `LocalDate`, day as derived scalar 0–6, and active flags as `true`, using named typed scalar binding consistent with accepted reader infrastructure. No user-provided lists/IDs, entity hydration, adjustments, salon/instructor/activity master joins, locks or writes. Map every selected row to a concrete `NominalProjectionRow`, validate row shape/ranges/containment and detect repeated `serie_id`, then produce the immutable set. Zero rows is a valid empty universe.

The query cannot see malformed rows excluded by applicability predicates (for example inactive or out-of-date rows); R3’s claim is the set applicable under those predicates. Constraint preflight/installation prevents the active overlap that could otherwise duplicate an applicable assignment series. No filtering by productive reservation state or runtime authority is allowed.

## PORT_READSET_CONTRACT

Keep historically frozen names:

```text
NominalProgrammingReadPort
  readNominalOnDate(ReadSnapshotContext context, LocalDate fecha)
      -> NominalProgrammingReadSet

NominalProgrammingReadSet
  candidates: immutable ordered List<ProgrammingCandidateSnapshot>
  backing: immutable ordered List<NominalBackingSnapshot>

NominalProjectionRow
  concrete scalar projection of the columns in QUERY_CONTRACT
```

`ProgrammingCandidateSnapshot` is the existing detector vocabulary, candidate type exactly `NOMINAL_OCCURRENCE`, reference type exactly `SERIE_ASIGNACION`. Candidate and backing collections have equal cardinality and row order; backing must correspond one-to-one by `ReferenciaOcurrencia`, while physical version IDs distinguish evidence versions. There is no partial-success result. Invalid context/date is `ADAPTER_INPUT_INVALID` before SQL; malformed or duplicate rows abort with typed reader/invariant failure; SQL/resource errors remain operational failures and never map to an empty set. Use defensive copies and immutable scalar values. Do not rename shared F2E contracts.

## TRANSACTION_SNAPSHOT_CONTRACT

- Reuse the accepted R1/R2 transaction infrastructure patterns and shared `ReadSnapshotContext` identity carrier; do not inject or call their readers/read sets. R3 reads a distinct slice.
- One transaction owns the complete single-query read and mapping. Propagation `REQUIRED`; `readOnly=true`; same datasource/transaction manager and connection identity as any enclosing evaluation transaction. No separate manager is justified by the current single-source query.
- Require repeatable snapshot semantics for the R3 read if called inside a multi-reader evaluation. Use the established owner-controlled transaction approach from R1/R2; final isolation configuration for R3’s enclosing evaluation must prove PostgreSQL snapshot behavior rather than silently copying weaker `READ COMMITTED`. A standalone one-query transaction is statement-consistent, but does not itself promise a cross-reader snapshot.
- No locks intended to write. Candidate snapshot is detached immutable data before transaction ends. Resource/snapshot identity is recorded in `ReadSnapshotContext` and must match the evaluation owner’s identity.
- Extend shared test infrastructure only with R3’s projection slice checksum/table allowlist and transaction owner assertions. Preserve existing R1/R2 checksums, SQL allowlists, owner contracts and reader boundaries unchanged.
- Any checksum is an acceptance identity for the R3 schema slice (`programacion_asignacion`, `programacion_bloque`, relevant constraint/index metadata), not a business-data hash or proof that rows are clean.

## NO_WRITE_CONTRACT

Future acceptance must prove all of the following against real PostgreSQL/Testcontainers:

- Dedicated SELECT-only role has `SELECT` only on allowlisted `programacion_asignacion` and `programacion_bloque` (and only required metadata access); no INSERT/UPDATE/DELETE/TRUNCATE/DDL/sequence/write privileges.
- Attempts at forbidden writes fail with PostgreSQL insufficient-privilege SQLState `42501`; do not swallow or translate into success/empty read.
- Closed SQL catalog allows the exact SELECT projection (and required metadata inspection) and rejects unrecognized SQL fail-closed. No repository writer reachable from R3.
- Verify checksum of the declared R3 schema slice including the eventual assignment exclusion constraint; schema/migration history fingerprint stays V46 in this design-only lifecycle.
- Query, mapper and architecture tests prove no entity/proxy escape, no flush/write, correct empty and multi-series results, duplicate rejection and transaction ownership/snapshot behavior.

## FUTURE_IMPLEMENTATION_SHAPE — advisory only

- **Potential new:** R3 read port/context scope if needed without renaming shared context; immutable read set/backing snapshot; `NominalProjectionRow`; plain native query executor; mapper; plain JPA reader; R3-specific unit/architecture/PostgreSQL acceptance tests and isolated test configuration.
- **Potential modified:** shared testinfra only through additive R3 registration, metadata slice checksum and SQL catalog entries; future migration authority may add a new narrowly scoped migration after conflict preflight. None is authorized here.
- **Read-only dependencies:** `ReadSnapshotContext`, `ProgrammingCandidateSnapshot`, `ReferenciaOcurrencia`, `programacion_asignacion`, `programacion_bloque`, accepted transaction/testinfra contracts.
- **Provenance-only:** historical V47 object and broad adapter design; no production dependency on `programacion_ajuste_fecha` or V47 adjustment indexes.

No concrete paths are frozen beyond existing read-only dependencies because the implementation allowlist has not been reconciled/authorized.

## BOUNDARIES

- **R1/R2:** neither reader nor its read set is a data dependency. Shared transaction/testinfra primitives may be reused with additive R3 slice support and no change to R1/R2 semantics.
- **Detector:** R3 provides nominal candidates and physical backing/provenance for an exact date. It does not compare sources, reconcile reservations or legacy turns, compose effective programming, select mappings, or coordinate a run.
- **R4/R5/R6:** R4 owns exact-date adjustment reads; R5 effective composition; R6 cross-source coordination/shadow read set. No adjustment schema, `AJUSTE` reference, effective result, comparator or coordinator moves into R3. Historical V47’s R4 schema is excluded by this reconciliation.
- **Product:** TurnoInstructor remains `LEGACY_VIVO / PRODUCTIVO`; programming authority and runtime routing unchanged; dark launch preserved; client API/web/reservations unchanged; no productive jobs, cutover, payments/notifications, deployment or R3-R6 activation.

## FUTURE_VALIDATION_CONTRACT

Applicable eventual categories: unit mapping; exact-date/day derivation; inclusive validity/open end; range/containment; query binding and projection; zero/multiple-series cardinality; duplicate series fail-closed; deterministic order; backing/candidate one-to-one; transaction ownership and PostgreSQL snapshot consistency; Testcontainers; SELECT-only role and SQLState `42501`; closed SQL catalog; slice checksum; architecture isolation/no runtime reachability; cross-lane and authorized full regression. Migration tests and conflict preflight become applicable only under a separately authorized schema-change lifecycle. No tests or suites were run or added here.

## Unresolved issues and next gate

No P0-level ambiguity remains for the design choice. The exact future Flyway version, migration file/name, transaction integration point in a multi-reader owner, and additive shared testinfra paths are implementation-lifecycle details and remain unfrozen. Before any schema/implementation work, a fresh independent design audit must accept this artifact. Any discovered active overlap during a future migration preflight must stop the migration for separate data disposition; no automatic repair is allowed.

```text
R3 design: MATERIALIZED_CANDIDATE / PENDING_FRESH_INDEPENDENT_DESIGN_AUDIT / NOT_APPROVED / NOT_PUBLISHED
R3 implementation: NOT_AUTHORIZED
R4-R6: NOT_AUTHORIZED
```
