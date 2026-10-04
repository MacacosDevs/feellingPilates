# F2E R4 — diseño reconciliado del reader de ajustes por fecha exacta

Version: F2E-R4-DESIGN-V1
Status at capture: DESIGN_CANDIDATE / PENDING_FRESH_INDEPENDENT_AUDIT / NOT_PUBLISHED
Base: current clean main `5c8a9ab165a1852652ba0e01d06ff65ef6bc24c7` (PR #22).
Scope: DESIGN_ONLY. R4 implementation, migration and R4–R6 activation NOT_AUTHORIZED.
Author/coordinator: run_477ea987415a. Publication and closure require exact-byte fresh audit.

## 1. Autoridad vigente y reconciliación explícita

La instrucción humana de completar este lifecycle autoriza diseñar, auditar, corregir de forma acotada, publicar, integrar y cerrar el diseño R4. No autoriza implementar ni crear una migración. El preflight independiente `run_71ddfddfeab0 / task_740b849fa904 / ctx_6a98cab5e924` aprobó `R4_DESIGN_RECONCILIATION_REQUIRED`, P0/P1/P2=0/0/0; es evidencia de necesidad, no aprobación de estos bytes. Su resultado durable se conserva en Orca y en el directorio externo del run actual como `R4-PREFLIGHT-INDEPENDENT.json`.

Autoridad leída en main:

- `fase-2e-diseno-adapters-read-only-snapshot-consistency.md` §§10,12,18,33.4: purpose projection-first, fecha exacta, tres source atoms, backing completo, MANDATORY, host acceptance; sus supuestos de persistencia histórica se sustituyen para R4 por este diseño.
- `fase-2e-identidad-semantica-detector-read-only.md` §9.2–9.4: formas y referencias nuevas, separación legacy/operación, ausencia y cardinalidad semánticas. Sus reglas no se cambian.
- `fase-2e-r3-diseno-reader-programacion-nominal-reconciliado.md` y source R3 actuales: nominales aplicables, rechazo de series duplicadas, esquema V46 sin exclusión; R3 cerrado e inmutable.
- PR #20/#21/#22 y receipts R3: implementación y cierre integrados; R1/R2/R3 permanecen cerrados. F2E-STATE es cache subordinada, no concesión de autoridad.

Los objetos históricos `95900d8` (V47, entity/repository y servicios F2D) y `23caf0c` son PROVENANCE_ONLY. V47 contenía (A) exclusión de vigencias activas en asignaciones y (B) persistencia de ajustes; son separables. Ni A ni el paquete completo se restauran. No se requieren servicios F2D ausentes en main para leer hechos R4. Este diseño es autocontenido para R4; no altera los bytes sellados R1–R3 ni el diseño amplio histórico.

## 2. Decisión de esquema y AjusteProgramacionFecha

**R4_REQUIRES_SEPARATE_MINIMUM_ADJUSTMENT_PERSISTENCE_PREREQUISITE.** En main hay 49 migraciones hasta V46 y no tabla de ajustes ni persistencia equivalente. V46 no permite demostrar una lectura exitosa de ajustes: tabla ausente es fallo operacional, nunca universo vacío. No se reemplaza por TurnoInstructor, horario del salón, JSON, mocks productivos ni datos inferidos.

Se selecciona como fuente futura `programacion_ajuste_fecha` con el contrato mínimo siguiente. Esto fija el diseño del prerrequisito; NO crea DDL, asigna versión Flyway, instala tabla, importa datos, introduce entity/repository o autoriza un writer. El lifecycle independiente de prerrequisito debe reconciliar ownership cross-lane/Flyway, auditar la migración concreta y publicar/integrar su autoridad antes de cualquier handoff implementativo R4. La versión y checksum reales se fijarán allí desde main fresco, sin asumir V47 disponible. Un handoff R4 no podrá derivarse como ACTIVE sobre V46.

| Columna | Tipo PostgreSQL / Java | Nullability y regla |
| --- | --- | --- |
| id | UUID / UUID | NOT NULL, PK, identidad física |
| tipo | VARCHAR(16) / String validada | NOT NULL, CANCELACION/REEMPLAZO/ADICION exactamente |
| fecha | DATE / LocalDate | NOT NULL, fecha atómica |
| asignacion_serie_id | UUID / UUID | obligatorio cancelación/reemplazo; NULL adición |
| salon_resultado_id | UUID / UUID | NULL cancelación; obligatorio reemplazo/adición; FK salon(id) |
| instructor_resultado_id | UUID / UUID | misma forma; FK usuario(id) |
| tipo_actividad_resultado_id | UUID / UUID | misma forma; FK tipo_actividad(id) |
| hora_inicio_resultado | TIME WITHOUT TIME ZONE / LocalTime | NULL cancelación; obligatorio reemplazo/adición |
| hora_fin_resultado | TIME WITHOUT TIME ZONE / LocalTime | misma forma; fin > inicio, intervalo half-open |
| activo | BOOLEAN / Boolean validado | NOT NULL; sólo true entra al reader |
| creado_en | TIMESTAMPTZ / OffsetDateTime | NOT NULL, timestamp técnico |
| actualizado_en | TIMESTAMPTZ / OffsetDateTime | NOT NULL, timestamp técnico |

Prerrequisito mínimo incluye PK, FKs de resultados, check de tipos, check de forma completa (cancelación sin resultado, reemplazo con target y resultado, adición sin target y con resultado), check de rango positivo, y unicidad parcial `(asignacion_serie_id,fecha)` para activos CANCELACION/REEMPLAZO. Incluye índice parcial de fecha para activos como acceso del reader. No requiere índices por salón/instructor, exclusión de vigencias en asignaciones, extensiones nuevas, FK hacia una versión arbitraria de asignación, trigger, backfill ni repair. La serie es identidad lógica multiversión: su existencia/unicidad aplicable no se demuestra con FK a una fila y pertenece a evaluación posterior. Defaults técnicos pueden fijarse en el lifecycle de esquema sin redefinir temporalidad funcional.

`AjusteProgramacionFecha` designa el concepto de ajuste con estas formas; **no existe como entity actual autorizada**. La entity mutable y JpaRepository históricos no son contratos de lectura actuales. El reader futuro sólo usa proyección escalar, nunca materializa/reutiliza entity managed ni modifica repositorio. Fecha/UUID de una adición definen `(AJUSTE,id,fecha)`; cambiar su fecha no preserva identidad de la occurrence, recreación usa UUID nuevo. R4 observa el snapshot actual: no demuestra historia de mutaciones ni implementa esa política de writer. Timestamps técnicos no son vigencia, intención ni prueba de inmutabilidad histórica.

La unicidad de target se comprueba además al leer: no es permisible elegir un ajuste por recencia/UUID. Dos adiciones distintas pueden tener resultado igual y siguen siendo hechos distintos, sin deduplicación por dimensiones. Un resultado que coincida con legacy no prueba equivalencia.

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

## 7. Aceptación futura PostgreSQL, no-write y arquitectura

No tests/build/DB ejecutados en este lifecycle documental: NOT_APPLICABLE, no PASS técnico. Antes de aceptar implementación, el handoff separado debe fijar paths/allowlist default-deny, catálogo SQL exacto incluyendo probes, versión/checksum del prerrequisito integrado, wiring y manifests; no inferirlos por estos nombres conceptuales.

Aceptación obligatoria posterior:

- PostgreSQL real/Testcontainers con Flyway integrado fresco V1→V46/49 baseline más únicamente el prerrequisito R4 aprobado. Sin exclusión de asignaciones ni restauración V47 wholesale. V46 solo es control negativo: schema ausente falla, no read vacío. No H2/mocks como prueba de snapshot/privilegios. HostValidator real REQUIRED según §33.4 histórico; si indisponible, gate de implementación se bloquea, no se rebaja.
- Role R4 SELECT-only en programacion_ajuste_fecha y probes/metadata allowlisted, sin grants a nominales/legacy/reservas/masters salvo inspección de catálogo necesaria; sin INSERT/UPDATE/DELETE/TRUNCATE/DDL/sequence privileges. Owner y executor usan el mismo role/recurso. Fixtures y preparación de esquema usan owner administrativo separado antes de la captura, sin contaminar el intervalo de lectura.
- Intentos de writes denegados con SQLState 42501 en pruebas separadas del catálogo de lecturas; role/read-only se prueban por separado para no confundir 25006 con privilegios. Catálogo deny unknown SQL incluyendo writes/locks, bind fecha null/tipo incorrecto y filtro active incorrecto; sin flush automático.
- Checksums de schema/history y datos antes/después en ventana controlada de lectura y fallos, sensibilidad al mutar fixture desde conexión administrativa separada. Prueba concurrente R4: cambio committed en tabla después de snapshot inicial, lectura dentro RR observa versión inicial y nueva TX ve cambio. No claim cross-source ni checksum global estable durante writer concurrente; comparar vista misma TX y evidencia committed fuera de ventana.
- Tres formas válidas; inactive y otras fechas excluidas; empty y varios ajustes; cancelación sin resultado; reemplazo con cambio de salón; adiciones con iguales dimensiones pero IDs distintos. Fixtures inválidas prueban fail-closed de mapper/row, incluyendo duplicados simulados (DB constraint intacta en aceptación normal). Para probar SQL/DB duplicados en un esquema degradado sólo un fixture negativo aislado explícito; su descriptor no puede admitirse como schema aprobado. No remover constraints del schema normal.
- Precisión/timestamps/null tokens/provenance, orden UUID, cardinalidad y backing exactos, hashes repetibles y sensibilidad de todos campos. Target ausente en fixture nominal no produce fallo R4; duplicate target de ajustes sí. Nunca se valida effective graph ni legacy mapping.
- Proxy MANDATORY, owner REQUIRES_NEW/RR, sin TX/manager/owner, raw new/self-invocation, context forjado/replay, resource swapped, RC/read-write, snapshot/capture mismatch y completion failure: ninguno entrega read set. Provisional results retenidos por owner hasta cerrar.
- Guard R4 aditivo e independiente para package edges y beans/callers: port/domain sin JPA/Spring, adapter sin controllers/jobs/productive services/write repos/Payments/Notifications, sin R1/R2/R3 business APIs, R5/R6, default/prod beans o runtime switches. Core y guards existentes read-only; necesidad de modificarlos exige autoridad separada. Único wiring explícito test-only. Full regression pertinente conserva R1–R3 y lanes, sin usar PASS histórico como prueba fresh.

## 8. Lifecycle y cierre de diseño

Secuencia seleccionada: candidato documental → fresh independent design audit exact-byte → correcciones acotadas y fresh re-audit si corresponde → receipt/publicación PR → merge verificado → cierre de proceso documental separado con auditor fresh → publicación/merge de cierre y verificación final. Máximos por stage: P2 mecánico 2 ciclos; P1 inequívoco 1; TX/schema sólo corrección documental ya determinada por esta autoridad 1; P0/contradicción/alternativa material/expansión requieren HUMAN_GATE. Cambiar worker/run no resetea presupuesto.

Design completion no implica implementación ni schema instalado. Next lifecycle tras cierre: **MINIMUM_R4_SCHEMA_PREREQUISITE_AUTHORITY**, read-only/design authority primero; implementación de migración NO autorizada por este documento. Después de prerrequisito auditado/integrado se podrá abrir handoff R4 separado. No implementación handoff ACTIVE ahora, ningún código/migración en este PR.

R1–R3 CLOSED/IMMUTABLE; TurnoInstructor LEGACY_VIVO/PRODUCTIVO; dark launch PRESERVED; no cutover, client/API/reservations change, deployment ni activación R4–R6. Payments/Notifications OUT_OF_SCOPE. Este estado de captura queda histórico tras receipt y merges; cierre actual se resuelve por evidencia publicada y Git/GitHub live, no se reescriben bytes auditados retroactivamente.
