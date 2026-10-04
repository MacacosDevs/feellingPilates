# FeelingPilates — handoff de implementación F2E R2 / lector Turno legacy

Estado: `MATERIALIZED_CANDIDATE / PENDING_FRESH_INDEPENDENT_HANDOFF_AUDIT / NOT_APPROVED / NOT_PUBLISHED / NOT_ACTIVE`.

Implementación R2: `NOT_AUTHORIZED / NOT_STARTED`.

Tipo: `IMPLEMENTATION_MECHANICAL / READ_ONLY_JPA_ADAPTER / DARK_LAUNCH_ONLY`.

Resultado de esta unidad: materializar una autoridad mecánica finita para un futuro executor. Este documento no implementa, no aprueba, no publica, no activa y no cambia autoridad productiva. El gate de proceso `gate_887fcc0b76b0` autoriza únicamente esta materialización documental; no transmite autorización de implementación.

## 1. Propósito y binding de autoridad

El resultado futuro acotado es:

```text
fecha + salones
→ dos projections escalares de Turno legacy bajo una única TX RR/read-only de prueba
→ validación total y aggregation
→ LegacyTurnReadSet inmutable de GenericSourceSnapshot
```

Autoridad publicada y bytes obligatorios:

| Fuente | SHA-256 | Uso |
| --- | --- | --- |
| `auditoria/fase-2e-r2-diseno-lector-turno-legacy-integracion.md` | `db4673dd0705c41e62d0b77339d45b2cd1e87c51e26064112ad95b843dafe9cf` | Autoridad primaria; cláusulas R2 §§2–10. |
| `auditoria/handoffs/HANDOFF-F2E-R2-DISENO-LECTOR-TURNO-LEGACY.md` | `221347b46c5032b384a306c61908c8fd0f2c26076455609fb880098be8f0d2d4` | Provenance/profile de diseño; no es autoridad implementativa. |
| `auditoria/reviews/F2E-R2-REVIEW-DISENO-LECTOR-TURNO-LEGACY.md` | `742590c0a15fdc2455a3d35c27ff0a1fbb0f63e2559078a02915a69416c90654` | PASS fresh de suficiencia de diseño, publicación y límites. |
| `auditoria/ESTADO-ACTUAL.md` | `dd6bf7a45a6b54335a5afd5500e5cc6357a8c9e026ed37937ef498a29a13ebaf` | Estado operativo y fronteras productivas. |

Preflight físico de authoring: branch `operacion/excepciones-horario-fecha`, `HEAD=f3ce79ce49a25a1a229e0033971ed07b855e8631`, index vacío y working tree limpio antes de crear este único path. El `HEAD` anterior identifica este corte documental, no queda congelado como requisito de una ejecución futura. Antes de ejecutar se repite PREPARE y se exige el commit exacto que active este handoff.

Precedencia fail-closed:

```text
canónicos + diseño R2 publicado + handoff R2 activo exacto
> cache operacional / resumen / conversación / código preexistente
```

Una contradicción, hash distinto, handoff no activo o cambio de scope termina en Human Gate; no se reconcilia durante implementación.

## 2. Modelo de scope cerrado

Los cuatro sets siguientes son finitos y normativos. Sólo `AUTHORIZED_NEW` puede crearse y sólo `AUTHORIZED_MODIFIED` puede cambiar. `READ_ONLY_DEPENDENCY` puede leerse/importarse sin editar. `FORBIDDEN_EXPLICIT` nombra superficies críticas; además rige:

```text
DEFAULT_DENY = cualquier path del repositorio que no pertenezca a
               AUTHORIZED_NEW UNION AUTHORIZED_MODIFIED
```

No autorizan globs, packages, prefijos, archivos auxiliares, renames, generated sources ni paths “equivalentes”. Si hace falta un segundo path fuera de estos sets: `SCOPE_EXPANSION_REQUIRED`, stop antes de escribir.

### 2.1 `AUTHORIZED_NEW` — production, exact12

| Path exacto | Responsabilidad única | Cláusula de diseño |
| --- | --- | --- |
| `src/main/java/com/feelingpilates/transicion/programacion/read/LegacyTurnReadPort.java` | Port único `readForDate(LegacyTurnReadContext, LegacyTurnScope)`; retorna `LegacyTurnReadSet`; cero overloads/consumers. | §§2, 9–10 |
| `src/main/java/com/feelingpilates/transicion/programacion/read/LegacyTurnScope.java` | Record inmutable de `Set<UUID> salonIds` + `LocalDate fecha`; null/vacío/null-element preSQL; defensive copy y ordenes natural/unsigned separados. | §§2, 3.2, 5 |
| `src/main/java/com/feelingpilates/transicion/programacion/read/LegacyTurnReadContext.java` | Contexto exacto de diez campos, enums R2 propios de catálogo/claim, framing LP/SEQ/MAP, cinco fórmulas D13 y keysets 26+20; no extiende tipos R1. | §3 completo |
| `src/main/java/com/feelingpilates/transicion/programacion/read/LegacyTurnReadSet.java` | Record con exactamente `List<GenericSourceSnapshot> sources`, 0..N, copia defensiva, sin metadata operacional. | §§2, 3.4 |
| `src/main/java/com/feelingpilates/transicion/programacion/read/LegacyAdapterRejection.java` | Record inmutable por unidad intentada: code cerrado, queryId, scope seguro, marker, IDs/raw seguros, ordinal/multiplicidad física; sin SQL/PII. | §§2, 6.3, 10 |
| `src/main/java/com/feelingpilates/transicion/programacion/read/LegacyAdapterInputInvalid.java` | Excepción total con lista inmutable no vacía de rejections y sólo clasificación `ADAPTER_INPUT_INVALID` o `READ_SET_INVARIANT_VIOLATION`; no convierte fallos físicos/policy/TX. | §§2, 6.3 |
| `src/main/java/com/feelingpilates/transicion/programacion/adapter/jpa/projection/LegacyTurnProjectionCatalog.java` | Catálogo R2 propio `R2_LEGACY_TURN_V1`: seis shapes/IDs exactos, aliases, tipos y planes de bind; sin ampliar el default R1. | §§3.1, 4–5 |
| `src/main/java/com/feelingpilates/transicion/programacion/adapter/jpa/projection/LegacyTurnMemberRow.java` | Record concreto de once campos de MEMBERS en ordinal/alias/tipo exactos. | §§2, 5 |
| `src/main/java/com/feelingpilates/transicion/programacion/adapter/jpa/projection/LegacyAssignmentRow.java` | Record concreto de cinco campos de ASSIGNMENTS en ordinal/alias/tipo exactos. | §§2, 5 |
| `src/main/java/com/feelingpilates/transicion/programacion/adapter/jpa/projection/LegacyTurnProjectionQueryExecutor.java` | `EntityManager.createNativeQuery`/`NativeQuery`, binding named tipado, MEMBERS primero y ASSIGNMENTS sólo con parent UUIDs usables; no mapping semántico. | §§2, 4–5 |
| `src/main/java/com/feelingpilates/transicion/programacion/adapter/jpa/mapper/LegacyTurnProjectionMapper.java` | Validación key-first + payload/correlación total, K exacto, aggregation, markers, URNs, provenance/fingerprints y construcción all-or-nothing. | §§2–3, 6.3, 10 |
| `src/main/java/com/feelingpilates/transicion/programacion/adapter/jpa/LegacyTurnJpaReader.java` | Implementa port, valida catálogo/context/scope, participa obligatoriamente en TX nombrada y coordina executor→mapper; plain, sin bean productivo. | §§6.1–6.3, 8–10 |

Reglas de shape para estos doce paths:

- sólo los cuatro packages permitidos por diseño §22;
- constructor DI plain; cero `@Component`, `@Service`, `@Repository`, `@Configuration`, `@Bean`;
- `read` permanece persistence-agnostic y no importa JPA/Spring/entities/repositories;
- ningún entity/proxy/managed collection/stream/repository/`EntityManager` cruza el boundary;
- no se crea utility/helper adicional: lógica necesaria queda en los tipos exactos anteriores.

### 2.2 `AUTHORIZED_NEW` — tests/test-only, exact10

| Path exacto | Responsabilidad y cobertura obligatoria | Cláusula de diseño |
| --- | --- | --- |
| `src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/LegacyTurnProjectionMapperTest.java` | Matriz 0/1/N, K, gaps/assignments/nonmembers, ranges, markers, UNKNOWN_INTENT, 26+20 keys, fórmulas y rechazo total. | §§2–3, 10 |
| `src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/LegacyTurnProjectionQueryExecutorTest.java` | Seis SQL shapes, IDs, aliases/types, plan named, setters JDBC reales/orden y short-circuit query2. | §§4–5, 10 |
| `src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/LegacyTurnJpaReaderPostgreSqlTest.java` | PG16 real, projections/ordering, role exact3, denied writes, checksums persistidos de tres tablas y integrated Flyway head. | §§5, 7, 10 |
| `src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/LegacyTurnJpaReaderTransactionTest.java` | Proxies reales, MANDATORY, owner RR/readOnly, Session/PgConnection, probes/capture/prefixes/completion y negativos wrong-resource. | §§6.1–6.3, 10 |
| `src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/LegacyTurnJpaReaderConcurrencyTest.java` | Writer entre DATA statements, snapshot RR estable, intento posterior observa cambio, control RC raw separado y reader R2 en RC rechaza preDATA. | §§7, 10 |
| `src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/LegacyTurnJpaReaderRuntimeIsolationTest.java` | Contextos default/prod sin beans R2, cero callers/controllers/triggers/config switches y TurnoInstructor productivo intacto. | §§8–10 |
| `src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/LegacyTurnR2ArchitectureTest.java` | Assertions R2 separadas: packages/dependencias, plain classes, no writers/escapes, port/context/catalog/owner exactos y cero reachability productiva. | §§8–10 |
| `src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/testinfra/LegacyTurnR2PostgresTestConfiguration.java` | Grafo test-only exacto R2, descriptor/registry/fixtures/bootstrap/observer/cleanup; descubre head Flyway integrado, no importa config Reserva completa. | §§6.1, 7, 9–10 |
| `src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/testinfra/LegacyTurnTransactionTestOwner.java` | Owner R2 dedicado, proxy distinto del reader, registro/reserva/completion/capture y único boundary RR. | §§3.1, 6 completo |
| `src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/testinfra/LegacyTurnJdbcCapture.java` | Instrumentación TEST-ONLY transparente de DS/Connection/PreparedStatement/ResultSet, setters/execute y object identities reales. | §§5, 6.2–6.3 |

No se autoriza otro fixture/helper/config/test. Los fixtures, descriptor y registry que sólo sirven al grafo R2 quedan como tipos nested/private en los tres paths testinfra anteriores; extraerlos sería scope expansion.

### 2.3 `AUTHORIZED_MODIFIED` — exact4

| Path existente / SHA-256 de entrada | Delta permitido | Delta prohibido | Evidencia de preservación obligatoria |
| --- | --- | --- | --- |
| `src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/testinfra/F2eSelectOnlyRole.java` / `532502268c445a38beef4339f6fa9eced0770a0af665a29c2beada7f43f434c1` | Añadir factory neutral que acepte un slice sellado de grants y controles negativos; instancia R2 concede sólo las tres tablas §7 y built-ins mínimos. | Cambiar factory R1, principal/grant `public.reserva`, wrapper/metadata hostil, SQLState42501 o ampliar grants/global PUBLIC. | Tests Reserva PG/TX permanecen verdes; prueba R1 de grant exacto; R2 verifica role membership, grants exact3, no extra tables/functions y cuatro writes denegados. |
| `src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/testinfra/F2eStatementPolicyInspector.java` / `512da4beb1aa9c845aaab02097b2ab798a5fa9f7b18b0d45cb9374ad8408be94` | Parametrizar por catálogo sellado y añadir asociación plan/inspector/JDBC para R2; conservar normalizer/hash común. | Añadir shapes R2 al catálogo default R1, permitir snapshot en R1, suavizar denylist/unknown SQL/capture ownership o afirmar execute desde inspect. | Constructor/default R1 sigue catálogo exact4 y snapshot denied; goldens/unknown SELECT/JDBC0 R1; instancia R2 exact6 y cada occurrence tiene tres evidencias independientes. |
| `src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/testinfra/F2eSliceChecksum.java` / `db4757b6cb7b52087ccfa2da70505844dd935a78d016e34fdbeb64a5ca96ca25` | Añadir seam neutral explícito para all-column/schema-ordinal/PK compuesta y domains R2 separados. | Cambiar selectors, codecs, domains, order, simple UUID PK, vectors o hashes de Reserva; omitir columnas/tablas desconocidas. | Todos los vectores R1 byte-exactos se recomputan; nuevos R2 cubren null, tabla vacía, PK triple, three entries, counts y slice before/after. |
| `src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/ReservaJpaReaderArchitectureTest.java` / `e8303ef30a703f39b939624d6dd4d5252a8fe90ab07bd442cb81a2d976625009` | Únicamente composición de allowlists selladas conforme §3; puede nombrar exact12 main/exact10 test R2 y separar enumeraciones. | Cambiar/eliminar miembros R1, roots, guards, singleton assertions, MANDATORY/readOnly assertions; prefix/wildcard/count/skip/exclusion. | Literal R1main11/R1test10 idénticos, ocho assertions de sets/disjunción de §3 y todas las pruebas previas verdes. |

No se permite reformateo incidental ni refactor de conveniencia. Cada diff hunk debe trazarse al delta permitido de su fila.

### 2.4 `READ_ONLY_DEPENDENCY` — lista finita exacta

Canónicos/protocolo:

```text
auditoria/fase-2e-r2-diseno-lector-turno-legacy-integracion.md
auditoria/handoffs/HANDOFF-F2E-R2-DISENO-LECTOR-TURNO-LEGACY.md
auditoria/reviews/F2E-R2-REVIEW-DISENO-LECTOR-TURNO-LEGACY.md
auditoria/ESTADO-ACTUAL.md
auditoria/orquestacion/README.md
auditoria/orquestacion/WORKFLOW.md
auditoria/orquestacion/STATE-MACHINE.md
auditoria/orquestacion/GATES.md
auditoria/orquestacion/ROLES.md
auditoria/orquestacion/F2E-RUNBOOK.md
auditoria/orquestacion/F2E-EXECUTION-POLICY.md
auditoria/orquestacion/F2E-STATE.json
```

Core/source/schema:

```text
src/main/java/com/feelingpilates/transicion/programacion/detector/GenericSourceSnapshot.java
src/main/java/com/feelingpilates/transicion/programacion/detector/EvidenceProvenance.java
src/main/java/com/feelingpilates/transicion/programacion/detector/DetectorVocabulary.java
src/main/java/com/feelingpilates/transicion/programacion/detector/SourceSnapshot.java
src/main/resources/db/migration/V15__calendario_instructores.sql
src/main/resources/db/migration/V19__turno_instructor_multiples.sql
src/main/resources/db/migration/V20__turno_instructor_asignacion.sql
src/main/resources/db/migration/V22__asignacion_rango_horario.sql
src/main/java/com/feelingpilates/calendario/entidad/TurnoInstructor.java
src/main/java/com/feelingpilates/calendario/entidad/TurnoInstructorAsignacion.java
```

R1 main preservado, exact11:

```text
src/main/java/com/feelingpilates/transicion/programacion/adapter/jpa/ReservaJpaReader.java
src/main/java/com/feelingpilates/transicion/programacion/adapter/jpa/mapper/ReservaProjectionMapper.java
src/main/java/com/feelingpilates/transicion/programacion/adapter/jpa/policy/F2eSqlPolicyViolationException.java
src/main/java/com/feelingpilates/transicion/programacion/adapter/jpa/projection/ReservaProjectionQueryExecutor.java
src/main/java/com/feelingpilates/transicion/programacion/adapter/jpa/projection/ReservaProjectionRow.java
src/main/java/com/feelingpilates/transicion/programacion/read/ReadSnapshotContext.java
src/main/java/com/feelingpilates/transicion/programacion/read/ReadSnapshotIdentifiers.java
src/main/java/com/feelingpilates/transicion/programacion/read/ReservationReadException.java
src/main/java/com/feelingpilates/transicion/programacion/read/ReservationReadFailureCode.java
src/main/java/com/feelingpilates/transicion/programacion/read/ReservationReadPort.java
src/main/java/com/feelingpilates/transicion/programacion/read/ReservationScope.java
```

R1 test no modificable, exact6:

```text
src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/ReservaJpaReaderPostgreSqlTest.java
src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/ReservaJpaReaderRuntimeIsolationTest.java
src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/ReservaJpaReaderTransactionTest.java
src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/ReservaProjectionMapperTest.java
src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/testinfra/F2ePostgresTestConfiguration.java
src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/testinfra/ReaderTransactionTestHarness.java
```

`F2ePostgresTestConfiguration` conserva thresholds/fixtures R1 históricos. R2 reutiliza capacidad de PostgreSQL/Flyway, no el grafo Reserva ni sus literales `V47/applied50`. `ReaderTransactionTestHarness` permanece R1/RC y no se adapta: el owner RR R2 nuevo es obligatorio.

### 2.5 `FORBIDDEN_EXPLICIT` y default deny

```text
pom.xml
src/main/resources/application.properties
src/main/resources/application-dev.properties
src/main/resources/application-prod.properties
src/main/resources/db/migration
src/main/java/com/feelingpilates/calendario/controlador/TurnoInstructorController.java
src/main/java/com/feelingpilates/calendario/servicio/TurnoInstructorService.java
src/main/java/com/feelingpilates/calendario/repositorio/TurnoInstructorRepository.java
src/main/java/com/feelingpilates/calendario/repositorio/TurnoInstructorAsignacionRepository.java
src/main/java/com/feelingpilates/calendario/entidad/TurnoInstructor.java
src/main/java/com/feelingpilates/calendario/entidad/TurnoInstructorAsignacion.java
src/main/java/com/feelingpilates/calendario/controlador/ReservaController.java
src/main/java/com/feelingpilates/calendario/servicio/ReservaService.java
src/main/java/com/feelingpilates/calendario/repositorio/ReservaRepository.java
src/main/java/com/feelingpilates/calendario/entidad/Reserva.java
src/main/java/com/feelingpilates/programacion
src/main/java/com/feelingpilates/pagos
src/main/java/com/feelingpilates/notificaciones
src/main/java/com/feelingpilates/transicion/programacion/composition
src/main/java/com/feelingpilates/transicion/programacion/detector
src/main/java/com/feelingpilates/transicion/programacion/read/ReadSnapshotContext.java
src/main/java/com/feelingpilates/transicion/programacion/read/ReadSnapshotIdentifiers.java
src/main/java/com/feelingpilates/transicion/programacion/read/ReservationReadPort.java
src/main/java/com/feelingpilates/transicion/programacion/adapter/jpa/ReservaJpaReader.java
```

Las entradas directorio anteriores significan path exacto + todos sus descendientes. Las duplicadas en `READ_ONLY_DEPENDENCY` pueden leerse, pero nunca modificarse. También están prohibidos: R3–R6, target/candidate selection, crosswalk, resolver, fence, report sink, material data audit, migration/backfill, controller/job/listener/runner, productive bean/config/consumer, switch de reader, Payments, Notifications, activation y cutover. `TurnoInstructor` conserva comportamiento y autoridad `LEGACY_VIVO / PRODUCTIVO`; Reservations conserva write/state/capacity ownership.

## 3. Delta acotado del guard `ReservaJpaReaderArchitectureTest`

El archivo conserva literalmente estos sets R1; no se renombran ni se generan por prefijo:

```text
R1main11 = {
  src/main/java/com/feelingpilates/transicion/programacion/adapter/jpa/ReservaJpaReader.java,
  src/main/java/com/feelingpilates/transicion/programacion/adapter/jpa/mapper/ReservaProjectionMapper.java,
  src/main/java/com/feelingpilates/transicion/programacion/adapter/jpa/policy/F2eSqlPolicyViolationException.java,
  src/main/java/com/feelingpilates/transicion/programacion/adapter/jpa/projection/ReservaProjectionQueryExecutor.java,
  src/main/java/com/feelingpilates/transicion/programacion/adapter/jpa/projection/ReservaProjectionRow.java,
  src/main/java/com/feelingpilates/transicion/programacion/read/ReadSnapshotContext.java,
  src/main/java/com/feelingpilates/transicion/programacion/read/ReadSnapshotIdentifiers.java,
  src/main/java/com/feelingpilates/transicion/programacion/read/ReservationReadException.java,
  src/main/java/com/feelingpilates/transicion/programacion/read/ReservationReadFailureCode.java,
  src/main/java/com/feelingpilates/transicion/programacion/read/ReservationReadPort.java,
  src/main/java/com/feelingpilates/transicion/programacion/read/ReservationScope.java
}
```

```text
R1test10 = {
  src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/ReservaJpaReaderArchitectureTest.java,
  src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/ReservaJpaReaderPostgreSqlTest.java,
  src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/ReservaJpaReaderRuntimeIsolationTest.java,
  src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/ReservaJpaReaderTransactionTest.java,
  src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/ReservaProjectionMapperTest.java,
  src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/testinfra/F2ePostgresTestConfiguration.java,
  src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/testinfra/F2eSelectOnlyRole.java,
  src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/testinfra/F2eSliceChecksum.java,
  src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/testinfra/F2eStatementPolicyInspector.java,
  src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/testinfra/ReaderTransactionTestHarness.java
}
```

`R2main12` es exactamente la lista §2.1 y `R2test10` exactamente la lista §2.2. El test debe enumerar físicamente las mismas raíces completas actuales y afirmar todas estas condiciones, separadamente:

```text
actualR1Main == R1main11
actualR1Test == R1test10
actualR2Main == R2main12
actualR2Test == R2test10
actualMain == R1main11 UNION R2main12
actualTest == R1test10 UNION R2test10
R1main11 INTERSECT R2main12 == EMPTY
R1test10 INTERSECT R2test10 == EMPTY
```

Extra, missing u overlap falla. Quedan prohibidos `startsWith`, regex de prefix ownership, wildcard, count-only, root skip/exclusion, filtrado que oculte unknowns o aceptación por número total. Shared testinfra modificado permanece miembro de `R1test10`, no se cuenta como R2 nuevo.

Se preservan sin weakening los demás guards existentes: plain/no stereotypes; tokens writer/resource/config prohibidos sobre roots completos; `ReadSnapshotContext.ProjectionCatalogVersion` y `SnapshotClaim` R1 siguen singleton exact1; ambos métodos R1 conservan `f2eReaderTransactionManager`, `MANDATORY`, `readOnly`. El guard R2 separado verifica lo equivalente para su port sin ampliar enums/context/catalog R1.

## 4. Transacción, reader obligatorio y recurso físico

Firma obligatoria del reader:

```java
@Transactional(
    transactionManager = "f2eR2ReaderTransactionManager",
    propagation = Propagation.MANDATORY,
    readOnly = true)
LegacyTurnReadSet readForDate(LegacyTurnReadContext context, LegacyTurnScope scope)
```

No abre/suspende transacción, no `REQUIRES_NEW`, no cambia isolation, no retry/downgrade/fallback. Outside TX, manager incorrecto o manager ausente falla antes de query.

Owner futuro obligatorio y exclusivamente test-only, en `LegacyTurnTransactionTestOwner.java`:

```java
@Transactional(
    transactionManager = "f2eR2ReaderTransactionManager",
    propagation = Propagation.REQUIRES_NEW,
    isolation = Isolation.REPEATABLE_READ,
    readOnly = true)
```

Owner y reader son proxies distintos; self-invocation y `new` no prueban propagation. Bean names exactos:

```text
f2eR2PrivilegedDataSource
f2eR2ReaderDataSource
f2eR2StatementPolicyInspector
f2eR2ReaderEntityManagerFactory
f2eR2ReaderEntityManager
f2eR2ReaderTransactionManager
legacyTurnProjectionQueryExecutor
legacyTurnProjectionMapper
legacyTurnJpaReader
legacyTurnTransactionTestOwner
```

Todas las injections/factory parameters llevan qualifier exacto; no `@Primary`, default fallback, routing DS, privileged EM/TM, scan/repository enable ni product config.

Prueba física obligatoria por invocación:

1. descriptor y configured DS/EMF/PU/TM/sharedEM/inspector por identidad;
2. mismo `EntityManagerHolder`, shared EM, Hibernate `Session` joined, Spring RR/readOnly, `Session.defaultReadOnly=true`, MANUAL flush;
3. sólo ese Session obtiene por `doReturningWork` la Connection transaccional y el `PgConnection` nativo unwrap;
4. antes/después de cada statement se comparan por identidad Java esas mismas referencias;
5. URL configurada y `PgDatabaseMetaData.getURL()/getUserName()` nativos genuinos se validan localmente; no transformación;
6. `Connection/PgConnection.getSchema()`, `getCatalog()`, ParameterMetaData y metadata traversal están prohibidos durante la invocación;
7. database/schema reales vienen únicamente del statement RESOURCE por el mismo shared EM/Session/PgConnection;
8. final repite grafo/URL/principal/referencias; mismo wrapper con otro PgConnection falla;
9. output provisional sólo escapa después de commit/completion real exitoso.

## 5. Catálogo SQL cerrado, bindings y orden

Catálogo `CLOSED_SET` exact6, byte/hash bound al diseño §4:

| Logical ID | Catalog statement ID | Rol |
| --- | --- | --- |
| `R2_LEGACY_MEMBERS_V1` | `9852b6e9487a71cb47d76e834e82eceafcfbdd194b6e66d718f7da1ccdf3a769` | DATA1 header + LEFT JOIN membership. |
| `R2_LEGACY_ASSIGNMENTS_V1` | `6b21c28ee8961f783e986704604791181aa175592abc0b73553fa321445ce219` | DATA2 assignments de todos los parent turnIds usables. |
| `R2_TX_ISOLATION_V1` | `4a669a2f628e12468e0d532889e0bcafd38c09a5aadfb27f2f20f56159c1671e` | `current_setting('transaction_isolation')`. |
| `R2_TX_READ_ONLY_V1` | `9963ea856cdf9bfb3e9c440b1c3a6c062fd375a906f465cbe7a7ac88878716c7` | `current_setting('transaction_read_only')`. |
| `R2_TX_RESOURCE_IDENTITY_V1` | `0ed00ba3ec87635658759a60f48bc9ea57df82a338b863f23be641fdf6b7b3ad` | `current_database/current_schema`. |
| `R2_TX_SNAPSHOT_V1` | `24f02692f50e880b77a78f9ea64501ce276b7b0e2a93c880a4c23e03bbe19aa0` | `pg_current_snapshot()::text`. |

El executor copia literalmente las dos SQL DATA y los cuatro probes del diseño §4; las longitudes canónicas DATA son 579 y 290 bytes. Cualquier byte/hash/shape distinto, SQL extra, `WITH`, `SHOW`, `SET`, lock, metadata SQL implícita, unknown SELECT o catálogo faltante falla antes de JDBC.

Binding exacto:

```text
MEMBERS:
  salonIds:list UUID.class, active:Boolean.class, recurrentType:String.class,
  dayOfWeek:Short.class, exceptionType:String.class,
  cancellationType:String.class, fecha:LocalDate.class

ASSIGNMENTS:
  turnIds:list UUID.class

I/R/RESOURCE/S:
  zero binds
```

Las listas bound usan orden UUID natural Java; identity/output/checksum usan UUID16 unsigned. Hibernate expande listas; no concatenación, arrays, ANY, temp table, nullable filter ni SQL fabricada.

Por statement y reserva R2 se registran tres planos independientes: plan named tipado y valores canónicos; catalog ID del inspector antes de prepare; prepare/setters/execute/ResultSet JDBC reales sobre la Connection bound. Orden/slot/setter/length/value deben coincidir; inspect no prueba execution.

Orden exitoso exacto:

```text
Iinitial → Rinitial → RESOURCEinitial → Sinitial → MEMBERS
→ [ASSIGNMENTS sólo si parent turnIds UUID usables no vacío]
→ Ifinal → Rfinal → RESOURCEfinal → Sfinal
→ native local final guard → completion real
```

Short-circuit exacto:

- MEMBERS realmente devuelve 0 rows: omite query2, conserva probes finales y puede producir éxito vacío tras completion;
- MEMBERS no vacío y cero parent UUIDs usables: omite query2 y aborta; nunca éxito vacío;
- errores payload no-key con parent UUID usable: ejecuta query2 para todos los parents usables, completa K y aborta después;
- fallo físico/policy/binding/recurso/TX: abort inmediato; no query/probe extra para completar K.

## 6. Rechazo total, atoms y semántica

No hay publicación entre projections. La validación primero extrae keys usables, después valida payload y correlación de ambas projections, y sólo al final construye el read set.

Invariantes obligatorios:

- PK assignment `(turno_id, usuario_id, tipo_actividad_id)` y URNs literales del diseño §2;
- K exacto: `1` para M/O vacíos; en otro caso `|O| + sum(max(1,|A(m)|))`;
- header inválido con keys suficientes y 0 assignments produce 1 rejection; con 3 activity PKs produce 3;
- key insuficiente cuenta una unidad por ordinal estable; no inventa UUID/K;
- duplicate PK/identity produce una unidad rechazada con multiplicidad física completa, nunca dedupe exitoso;
- nonmember assignment representable; orphan turn header rechaza;
- lista de markers cerrada y rango fallback/raw/incomplete/outside exactos;
- input/payload/correlación inválidos abortan todo pre-core;
- en todo abort: `published=evaluations=results=0`, no `DetectorClassifier`, candidate, semantic result, success report ni “zero anomalies”;
- fallo physical/policy/topology/probe/completion no se convierte en `LegacyAdapterRejection`, `MISSING`, `UNSUPPORTED`, `EXPECTED_ABSENCE` o UNKNOWN_INTENT.

Puntuales conservan exclusivamente:

```text
EXCEPCION   → LEGACY_EXCEPTION_UNKNOWN_INTENT
CANCELACION → LEGACY_CANCELLATION_UNKNOWN_INTENT
```

No existe `LEGACY_PUNCTUAL_UNKNOWN_INTENT`. UNKNOWN_INTENT prevalece sobre anomalía estructural puntual; timestamps técnicos no infieren historia/intención. Recurrente puede producir `RECURRENT_CLAIM_DEPENDENT` o `INCOMPATIBLE_EVIDENCE`, sin invocar classifier ni recibir claim semántico.

## 7. SELECT-only, checksums y no-write

El login R2 efímero es distinto del admin/R1: CONNECT, USAGE `public`, SELECT exclusivamente en:

```text
public.turno_instructor
public.turno_instructor_usuario
public.turno_instructor_asignacion
```

Sin ownership/superuser/bypassRLS/SET ROLE/inheritance/CREATE/TEMP/DML/sequence/application function. Built-ins sólo los necesarios para las cuatro probes exactas. Revocar PUBLIC/memberships efectivos y verificar antes de reader.

Controles separados con login R2 exigen SQLState `42501` para INSERT, UPDATE, DELETE y DDL; cualquier éxito falla GATE aunque se haga rollback. Rollback, annotation o Hibernate statistics no sustituyen el fence.

Observer privilegiado:

1. congela parent turnIds por predicados de fecha/salón/active/tipos;
2. calcula before de las tres tablas con todas las columnas vivas en schema ordinal y PK física/compuesta;
3. ejecuta reader en ventana quiescent;
4. calcula after seleccionando children cada vez por frozen parents y verifica también igualdad del set de parents por predicado;
5. persiste counts + table hashes + slice hash y exige before==after para las tres entradas, incluso scope vacío.

Entries exactas: `public.turno_instructor`, `public.turno_instructor_asignacion`, `public.turno_instructor_usuario`. Columna nueva/desconocida, ordinal/tipo/nullability/PK/FK/index drift o tabla omitida falla compatibilidad; no se ignora. La concurrencia RR ocurre en otra prueba/ventana y no usa el checksum quiescent para atribuir writes.

## 8. Flyway integrado, host y cross-lane

La config R2 arranca `postgres:16-alpine`, ejecuta `migrate`, `validate`, `ddl-auto=validate` y descubre en esa ejecución:

- migration head integrado actual;
- ordered versions/scripts/checksums/success;
- cero failed y cero pending inesperadas.

`V47` y `applied50` son evidencia R1 histórica dentro de su config; nunca threshold global R2. No se fija V47/50 en catálogo, test, handoff o assertion R2. Si el head integrado contiene migraciones de otras lanes, se valida compatibilidad sin editar/reconciliar/renumerar migraciones.

Triggers cross-lane que detienen antes de escribir y emiten `CROSS_LANE_DEPENDENCY_REQUIRED`:

```text
src/main/java/com/feelingpilates/calendario/entidad/Reserva.java
src/main/java/com/feelingpilates/calendario/repositorio/ReservaRepository.java
src/main/java/com/feelingpilates/calendario/servicio/ReservaService.java
src/main/java/com/feelingpilates/transicion/programacion/read/ReservationReadPort.java
src/main/java/com/feelingpilates/transicion/programacion/adapter/jpa/ReservaJpaReader.java
src/main/java/com/feelingpilates/calendario/entidad/TurnoInstructor.java
src/main/java/com/feelingpilates/calendario/entidad/TurnoInstructorAsignacion.java
src/main/java/com/feelingpilates/calendario/repositorio/TurnoInstructorRepository.java
src/main/java/com/feelingpilates/calendario/repositorio/TurnoInstructorAsignacionRepository.java
src/main/java/com/feelingpilates/calendario/servicio/TurnoInstructorService.java
src/main/java/com/feelingpilates/programacion/servicio/ProgramacionEfectiva.java
src/main/java/com/feelingpilates/programacion/entidad/BloqueProgramacion.java
src/main/java/com/feelingpilates/programacion/repositorio/BloqueProgramacionRepository.java
src/main/java/com/feelingpilates/programacion/entidad/Asignacion.java
src/main/java/com/feelingpilates/programacion/repositorio/AsignacionRepository.java
src/main/resources/db/migration
```

También dispara ante capacity/cupos, estado/write de Reserva, schema/migration/dependency, Payments financial semantics o Notifications delivery. F2E sólo observa Reserva y, en R2, lee Turno en el scope autorizado; no recibe ownership. No hay integración automática ni merge de Payments.

## 9. FAST, GATE y comandos

### 9.1 FAST — feedback de changed surface solamente

Después de cada delta coherente:

```text
./mvnw -Dtest=LegacyTurnProjectionMapperTest,LegacyTurnProjectionQueryExecutorTest,LegacyTurnR2ArchitectureTest test
./mvnw -Dtest=LegacyTurnJpaReaderTransactionTest,LegacyTurnJpaReaderRuntimeIsolationTest test
git diff --check
git diff --cached --check
```

El segundo comando puede requerir Docker; si el host no está disponible se registra `BLOCKED`, no PASS ni finding semántico. FAST sólo da feedback/depuración del changed surface. Declaración obligatoria: **FAST no puede aceptar la implementación, cerrar el milestone, sustituir full regression, audit fresh ni Decision Gate.**

### 9.2 GATE — aceptación técnica completa

Sobre un candidate exacto, fresh, allowlist inmutable y host competente:

```text
./mvnw -Dtest=LegacyTurnProjectionMapperTest,LegacyTurnProjectionQueryExecutorTest,LegacyTurnJpaReaderPostgreSqlTest,LegacyTurnJpaReaderTransactionTest,LegacyTurnJpaReaderConcurrencyTest,LegacyTurnJpaReaderRuntimeIsolationTest,LegacyTurnR2ArchitectureTest test
./mvnw -Dtest=ReservaProjectionMapperTest,ReservaJpaReaderPostgreSqlTest,ReservaJpaReaderTransactionTest,ReservaJpaReaderRuntimeIsolationTest,ReservaJpaReaderArchitectureTest test
./mvnw test
git diff --check
git diff --cached --check
```

No se fijan conteos históricos. Registrar fresh por comando: UTC start/end, exit, suites/tests/failures/errors/skips, XML/logs y candidate manifest/hash.

GATE exige cumulativamente:

1. exact22 NEW + exact4 MODIFIED, cero extra/missing/rename, R1 literal sets preservados;
2. unit/scenario/count/immutability/formula/provenance/rejection tests;
3. targeted R2 completo y regresión R1 shared-seam completa;
4. full repository regression;
5. PostgreSQL16/Testcontainers real; H2 prohibido;
6. Flyway head integrado actual descubierto, migrate/validate/checksums, JPA ddl validate, sin freeze V47/50;
7. owner real `REQUIRES_NEW/REPEATABLE_READ/readOnly` y reader real `MANDATORY/readOnly` con manager exacto;
8. shared EM/Session/Connection/native PgConnection y same-resource before/after cada statement;
9. I/R/RESOURCE/S initial/final, snapshot textual initial==final, ordered multi-statement RR y completion real;
10. catálogo exact6, plan/binds, inspector, JDBC setters/execute y query2 short-circuit/failure prefixes;
11. matrix 0/1/N, K, nonmembers/gaps/ranges, UNKNOWN_INTENT exacto, total rejection y zero partial publication;
12. role exact3 tables/built-ins mínimos, cuatro denied writes 42501 y privileges sin widening;
13. persistent before/after counts/table hashes/slice hash de las tres tablas y no-write quiescent;
14. concurrencia RR + intento posterior + control RC raw separado + reader R2 en RC preDATA reject;
15. architecture/package/core purity/no writer/no escape; default/prod absence y zero productive callers/triggers;
16. cross-lane/schema/ownership guard y diff/scope/index/HEAD checks;
17. HostValidator/RAW durable con receipts, hashes y candidate binding;
18. auditor técnico NUEVO/fresh/independiente, read-only, que abra autoridad y RAW directamente;
19. P0=0, P1=0 y findings/limitations explícitos;
20. Decision Gate real resolved/PASS sobre el candidate exacto.

Tests verdes, summary del executor, FAST, reviewer no fresh o gate pendiente no aceptan. Publication/activation/cutover requieren lifecycles separados.

## 10. Routing, independencia y antecedente Terra

Routing vigente para esta materia:

| Trabajo | Ruta |
| --- | --- |
| Implementación JPA/RR/native JDBC/snapshot/concurrency/resource/no-write y corrección técnica | `Sol / ESCALATION_ONLY`, effort demostrado suficiente, candidate/scope exactos. |
| Audit técnico de aceptación | Otro worker `Sol` NUEVO/fresh, read-only, sin contexto del executor/corrector y con acceso completo a autoridad/RAW. |
| Host validation | Determinista/no LLM, plan allowlisted sobre el mismo candidate. |
| Inventario/hash/manifest puramente mecánico | Luna medium sólo como piloto explícito; validación independiente. |
| Documentación/referencia mecánica o P2/P1 bounded ya autorizado | Terra sólo PILOT_FIRST y sin semántica nueva. |

Antecedente obligatorio:

```text
PREVIOUS_TERRA_PILOT_RESULT = INCONCLUSIVE_NO_ARTIFACT
```

Ese intento no produjo artefacto reutilizable, no prueba calidad, no cuenta como implementación/audit/evidence y no promueve Terra a default. No se relanza Terra para evadir riesgo, presupuesto o stop. High-risk Terra acceptance y Luna backend siguen `DEFER`; no silent fallback de modelo/effort.

## 11. Presupuestos correctivos, Human Gates y stops

Presupuestos máximos por finding/stage, usando el límite solapado más restrictivo:

| Clase | Máximo |
| --- | --- |
| P2 mecánico dentro de scope/autoridad | 2 |
| P1 bounded inequívoco con artefacto correctivo autorizado | 1 + re-audit fresh |
| JPA/TX/snapshot con semántica/solución ya fijada exactamente | 1; si no, Human Gate inmediato |
| P0 o contradicción de autoridad | 0 |
| Scope/schema/migration/cross-lane/API/domain/product activation/cutover/out-of-allowlist | 0 |

Un intento materializado/validado consume ciclo; fallo pre-semántico no consumido se registra aparte. Cambiar worker/model/effort/profile no reinicia contador. Agotamiento: `CORRECTION_BUDGET_EXHAUSTED`, fail closed. Corrección siempre: finding exacto → artefacto autorizado → delta mínimo → targeted validation → re-audit fresh; sin random loops ni convenience refactors.

Human Gates exactos aplicables, todos human-owned:

```text
PRODUCT_DECISION_REQUIRED
DOMAIN_RULE_CHANGE_REQUIRED
API_CONTRACT_CHANGE_REQUIRED
DESIGN_AUTHORITY_CHANGE_REQUIRED
IMPLEMENTATION_HANDOFF_SCOPE_CHANGE_REQUIRED
JPA_TRANSACTION_SEMANTICS_CHANGE_REQUIRED
SNAPSHOT_CONSISTENCY_RULE_CHANGE_REQUIRED
PHYSICAL_RESOURCE_IDENTITY_CHANGE_REQUIRED
ISOLATION_LEVEL_CHANGE_REQUIRED
DATABASE_SCHEMA_CHANGE_REQUIRED
MIGRATION_CHANGE_REQUIRED
FLYWAY_BASELINE_RECONCILIATION_REQUIRED
NEW_DEPENDENCY_REQUIRED
CROSS_LANE_DEPENDENCY_REQUIRED
SCOPE_EXPANSION_REQUIRED
AUTHORITY_RECONCILIATION_REQUIRED
NO_WRITE_INVARIANT_BREACH
REAL_HOST_VALIDATION_UNAVAILABLE
EVIDENCE_INSUFFICIENT
UNRESOLVED_P0
UNRESOLVED_P1_REQUIRING_AUTHORITY
CORRECTION_BUDGET_EXHAUSTED
AMBIGUOUS_AUTHORITY
BASELINE_DRIFT
REMOTE_DIVERGENCE
UNEXPECTED_REPOSITORY_MUTATION
PUBLICATION_REQUIRED
PRODUCTIVE_ACTIVATION_REQUIRED
CUTOVER_REQUIRED
MILESTONE_COMPLETE
```

Aliases de proceso preservados: `MODEL_ROUTING_UNAVAILABLE`, `STATE_DRIFT`, `SNAPSHOT_EVIDENCE_MISMATCH`, `R1_IMMUTABILITY_BREACH`. Detectar no autoriza resolver. Se detiene trabajo dependiente antes de escribir/continuar cuando falte autoridad, exista una alternativa material, haya drift/security breach, la evidencia requerida sea irrecuperable o se alcance un límite. Fallos operacionales recuperables se clasifican primero; no se inventa P0/P1/HUMAN_STOP ni se omite receipt.

## 12. Lifecycle y exit del futuro executor

Estado al materializar este documento:

```text
R1 = CLOSED / ACCEPTED / PUBLISHED / IMMUTABLE
R2 design = COMPLETE / AUDITED / PUBLISHED / CLOSED
R2 implementation handoff = MATERIALIZED_CANDIDATE / NOT_APPROVED / NOT_PUBLISHED / NOT_ACTIVE
ACTIVE IMPLEMENTATION HANDOFF = NINGUNO
R2 implementation = NOT_AUTHORIZED / NOT_STARTED
R2 runtime target = DARK_LAUNCH / NON_PRODUCTIVE
TurnoInstructor = LEGACY_VIVO / PRODUCTIVO
cutover = NOT_AUTHORIZED / false
R3-R6 = NOT_AUTHORIZED_IN_R2
Payments / Notifications = OUT_OF_SCOPE
```

Antes de cualquier código se requieren, en lifecycles separados y con receipts reales: audit fresh de este handoff → Decision Gate de aprobación → publicación/cierre documental cuando el profile lo exija → activación explícita del SHA exacto → dispatch implementativo que cite ese binding. Nada de este archivo satisface esas etapas por anticipado.

El futuro executor entrega únicamente candidate materializado y evidencia; no stage/commit/push/publicación, no autoaudit y no aceptación. Outcomes terminales permitidos:

```text
R2_IMPLEMENTATION_CANDIDATE_MATERIALIZED_PENDING_FRESH_GATE
BLOCKED_BY_AUTHORITY_CONTRADICTION
BLOCKED_BY_REQUIRED_SCOPE_EXPANSION
```

No existe completion recon-only, parcial, FAST-only ni green-tests-only.
