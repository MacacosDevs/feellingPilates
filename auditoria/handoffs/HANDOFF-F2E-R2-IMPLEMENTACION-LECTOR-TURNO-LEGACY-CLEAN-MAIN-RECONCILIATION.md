# FeelingPilates — Handoff de Implementación F2E R2 / Lector Turno Legacy (Reconciliación Clean-Main)

Estado: `MATERIALIZED_CANDIDATE / PENDING_FRESH_INDEPENDENT_HANDOFF_AUDIT / NOT_APPROVED / NOT_ACTIVE`.

Implementación R2: `NOT_AUTHORIZED / NOT_IMPLEMENTED`.

Tipo: `IMPLEMENTATION_MECHANICAL / READ_ONLY_JPA_ADAPTER / DARK_LAUNCH_ONLY / CLEAN_MAIN_RECONCILED`.

Resultado de esta unidad: materializar la autoridad de implementación de F2E R2 reconciliada contra el estado actual de `origin/main` limpio (`103ebe5ca25c0726559040f48b4668e9cffb0a4c`). Este documento es el sucesor canónico del handoff histórico de implementación R2; no implementa código, no aprueba implementación, no publica en producción, no activa runtime y no altera la autoridad productiva vigente.

---

## 1. Propósito, Proveniencia y Binding de Autoridad

### 1.1 Objetivo del componente R2 en runtime
El componente R2 es un adaptador JPA exclusivamente de lectura, desacoplado y sin estereotipos de Spring, cuyo flujo futuro es:

```text
fecha + salones
→ dos projections escalares de Turno legacy bajo una única TX Repeatable Read / read-only de prueba
→ validación total y aggregation (key-first, correlación de proyecciones, invariantes de negocio)
→ LegacyTurnReadSet inmutable compuesto exclusivamente de GenericSourceSnapshot
```

### 1.2 Proveniencia Histórica y Base Canónica
Este documento reconcilia y sucede operativamente al handoff histórico de implementación F2E R2:

| Artefacto / Hito | Identificador / Hash | Rol y Estado |
| --- | --- | --- |
| **Handoff histórico R2** | `auditoria/handoffs/HANDOFF-F2E-R2-IMPLEMENTACION-LECTOR-TURNO-LEGACY.md` | Inmutable; procedencia histórica (`7463798e80c898cc78d731012afc3b737f74c328a134d4da09d65ea79adad8e8`). |
| **Commit publicación histórica** | `6140978bfd7b723fbbf9ddde1b5b5ba4f777c43c` | Publicación original en branch de operación histórica. |
| **Base canónica actual** | `103ebe5ca25c0726559040f48b4668e9cffb0a4c` | `origin/main` actual integrado (Lanes 0–4 + Fundación Clean F2E + Pure Core + ReferenciaOcurrencia + R1 Reserva JPA Reader). |
| **Diseño R2 publicado** | `auditoria/fase-2e-r2-diseno-lector-turno-legacy-integracion.md` | Autoridad primaria técnica; cláusulas R2 §§2–10 (`db4673dd0705c41e62d0b77339d45b2cd1e87c51e26064112ad95b843dafe9cf`). |
| **Handoff diseño R2** | `auditoria/handoffs/HANDOFF-F2E-R2-DISENO-LECTOR-TURNO-LEGACY.md` | Provenance de diseño; no implementativo (`221347b46c5032b384a306c61908c8fd0f2c26076455609fb880098be8f0d2d4`). |
| **Review diseño R2** | `auditoria/reviews/F2E-R2-REVIEW-DISENO-LECTOR-TURNO-LEGACY.md` | PASS de auditoría independiente de diseño (`742590c0a15fdc2455a3d35c27ff0a1fbb0f63e2559078a02915a69416c90654`). |
| **Autoridad de Proceso Activa** | Orca Product Delivery (`F2E-RUNBOOK.md`, `F2E-EXECUTION-POLICY.md`, `F2E-STATE.json`, `ESTADO-ACTUAL.md`) | Proceso operativo vigente; sustituye el protocolo multiagente legacy ORQ-1. |

### 1.3 Precedencia Fail-Closed
En caso de discrepancia o ambigüedad, rige la estricta precedencia fail-closed:

```text
código canónico en main + diseño R2 publicado + handoff R2 activo exacto
> cache operacional / resúmenes / contexto de chat / código histórico de branches laterales
```

Cualquier contradicción, divergencia de hashes, handoff no activo o intento de alteración de scope finaliza inmediatamente en Human Gate; no se reconcilia unilateralmente durante la ejecución.

---

## 2. Modelo de Scope Cerrado y Allowlist de Rutas

El modelo de scope es estricto y exhaustivo. Rige:

```text
DEFAULT_DENY = Cualquier ruta del repositorio que no pertenezca explícitamente a
               CURRENT_R2_AUTHORIZED_NEW ∪ CURRENT_R2_AUTHORIZED_MODIFIED
```

No se admiten comodines, paquetes enteros, prefijos, archivos auxiliares no listados, renombrados, clases autogeneradas ni rutas supuestamente equivalentes. Cualquier necesidad de un archivo adicional requiere la emisión de `SCOPE_EXPANSION_REQUIRED` y detención total antes de escribir.

### 2.1 `CURRENT_R2_AUTHORIZED_NEW` — Producción (exact12)
Archivos de producción a crear exclusivamente durante el futuro lifecycle de implementación:

| # | Ruta exacta | Responsabilidad única | Cláusula Diseño |
| --- | --- | --- | --- |
| 1 | `src/main/java/com/feelingpilates/transicion/programacion/read/LegacyTurnReadPort.java` | Port único `readForDate(LegacyTurnReadContext, LegacyTurnScope)`; retorna `LegacyTurnReadSet`; cero overloads/consumers. | §§2, 9–10 |
| 2 | `src/main/java/com/feelingpilates/transicion/programacion/read/LegacyTurnScope.java` | Record inmutable de `Set<UUID> salonIds` + `LocalDate fecha`; validaciones pre-SQL; copia defensiva; ordenes natural y unsigned separados. | §§2, 3.2, 5 |
| 3 | `src/main/java/com/feelingpilates/transicion/programacion/read/LegacyTurnReadContext.java` | Contexto exacto de diez campos; enums R2 propios de catálogo/claim; framing LP/SEQ/MAP; fórmulas D13 y keysets 26+20. | §3 completo |
| 4 | `src/main/java/com/feelingpilates/transicion/programacion/read/LegacyTurnReadSet.java` | Record inmutable con `List<GenericSourceSnapshot> sources`, 0..N; copia defensiva; sin metadata operacional mutable. | §§2, 3.4 |
| 5 | `src/main/java/com/feelingpilates/transicion/programacion/read/LegacyAdapterRejection.java` | Record inmutable por unidad rechazada: código cerrado, queryId, scope seguro, marker, IDs seguros, multiplicidad física; sin SQL ni PII. | §§2, 6.3, 10 |
| 6 | `src/main/java/com/feelingpilates/transicion/programacion/read/LegacyAdapterInputInvalid.java` | Excepción con lista inmutable no vacía de rechazos (`ADAPTER_INPUT_INVALID` o `READ_SET_INVARIANT_VIOLATION`); no transforma fallos de infraestructura/TX. | §§2, 6.3 |
| 7 | `src/main/java/com/feelingpilates/transicion/programacion/adapter/jpa/projection/LegacyTurnProjectionCatalog.java` | Catálogo R2 propio `R2_LEGACY_TURN_V1`: seis sentencias SQL exactas, IDs canónicos, aliases, tipos y planes de bind tipados. | §§3.1, 4–5 |
| 8 | `src/main/java/com/feelingpilates/transicion/programacion/adapter/jpa/projection/LegacyTurnMemberRow.java` | Record concreto de once campos para MEMBERS en ordinal, alias y tipos exactos. | §§2, 5 |
| 9 | `src/main/java/com/feelingpilates/transicion/programacion/adapter/jpa/projection/LegacyAssignmentRow.java` | Record concreto de cinco campos para ASSIGNMENTS en ordinal, alias y tipos exactos. | §§2, 5 |
| 10 | `src/main/java/com/feelingpilates/transicion/programacion/adapter/jpa/projection/LegacyTurnProjectionQueryExecutor.java` | Ejecutor `EntityManager.createNativeQuery`/`NativeQuery` con binding named tipado; MEMBERS primero; ASSIGNMENTS condicional a parents usables; sin mapeo de dominio. | §§2, 4–5 |
| 11 | `src/main/java/com/feelingpilates/transicion/programacion/adapter/jpa/mapper/LegacyTurnProjectionMapper.java` | Validación key-first + payload/correlación total, K exacto, agregación, markers URN, provenance/fingerprints y construcción all-or-nothing. | §§2–3, 6.3, 10 |
| 12 | `src/main/java/com/feelingpilates/transicion/programacion/adapter/jpa/LegacyTurnJpaReader.java` | Implementación del port; valida contexto/scope; TX MANDATORY; coordina executor → mapper; POJO plain sin estereotipos Spring. | §§6.1–6.3, 8–10 |

Reglas arquitectónicas de producción:
- Clases POJO / records sin estereotipos (`@Component`, `@Service`, `@Repository`, `@Configuration`, `@Bean`).
- El paquete `read` es agnóstico de persistencia y no importa JPA, Hibernate ni Spring.
- Ninguna entidad JPA gestionada, proxy, colección gestionada ni `EntityManager` cruza el límite hacia el detector.
- No se permiten helpers o utilidades adicionales fuera de estos doce archivos.

### 2.2 `CURRENT_R2_AUTHORIZED_NEW` — Test / Testinfra (exact10)
Archivos de prueba y testinfra a crear exclusivamente durante el futuro lifecycle de implementación:

| # | Ruta exacta | Responsabilidad y cobertura obligatoria | Cláusula Diseño |
| --- | --- | --- | --- |
| 13 | `src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/LegacyTurnProjectionMapperTest.java` | Matriz 0/1/N, cálculo de K, gaps/assignments/nonmembers, markers, UNKNOWN_INTENT, keysets 26+20, rechazo total all-or-nothing. | §§2–3, 10 |
| 14 | `src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/LegacyTurnProjectionQueryExecutorTest.java` | Validación de seis shapes SQL, IDs canónicos, aliases/tipos, plan named, JDBC setters reales y short-circuit de query2. | §§4–5, 10 |
| 15 | `src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/LegacyTurnJpaReaderPostgreSqlTest.java` | PostgreSQL 16 real; proyecciones/ordenamiento; rol SELECT-only exact3 tablas; verificación de 4 escrituras denegadas (SQLState 42501); checksums de corte persistidos. | §§5, 7, 10 |
| 16 | `src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/LegacyTurnJpaReaderTransactionTest.java` | Validación de proxies reales; propagación MANDATORY; owner Repeatable Read / readOnly; probes de sesión PgConnection y rechazos ante conexión ajena. | §§6.1–6.3, 10 |
| 17 | `src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/LegacyTurnJpaReaderConcurrencyTest.java` | Concurrencia real con escritor intercalado; snapshot RR inmutable; segundo intento observa cambio; control RC raw separado; rechazo de reader R2 en RC pre-DATA. | §§7, 10 |
| 18 | `src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/LegacyTurnJpaReaderRuntimeIsolationTest.java` | Verificación de contextos productivos sin beans R2; cero controllers, jobs, triggers o callers activos; TurnoInstructor productivo intacto. | §§8–10 |
| 19 | `src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/LegacyTurnR2ArchitectureTest.java` | Aserciones de arquitectura R2: paquetes, dependencias, POJOs puros, ausencia de mutadores, port/context/catalog exactos y cero alcanzabilidad productiva. | §§8–10 |
| 20 | `src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/testinfra/LegacyTurnR2PostgresTestConfiguration.java` | Grafo de configuración test-only para R2; Testcontainers PG16; descubrimiento dinámico de head Flyway; aislamiento del grafo de Reserva. | §§6.1, 7, 9–10 |
| 21 | `src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/testinfra/LegacyTurnTransactionTestOwner.java` | Componente de prueba que actúa como owner transaccional (`REQUIRES_NEW`, `REPEATABLE_READ`, `readOnly = true`); proxy separado del reader. | §§3.1, 6 |
| 22 | `src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/testinfra/LegacyTurnJdbcCapture.java` | Instrumentación de prueba sobre DataSource/Connection/PreparedStatement para verificar llamadas JDBC reales, bindings e identidades de objeto. | §§5, 6.2–6.3 |

### 2.3 `CURRENT_R2_AUTHORIZED_MODIFIED` — Existentes Modificables (exact4)
Archivos existentes en `origin/main` cuya modificación acotada está autorizada para soportar R2:

| # | Ruta existente | SHA-256 en main actual | Delta Permitido | Delta Prohibido |
| --- | --- | --- | --- | --- |
| 1 | `src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/testinfra/F2eSelectOnlyRole.java` | `532502268c445a38beef4339f6fa9eced0770a0af665a29c2beada7f43f434c1` | Añadir factory parametrizable neutral que reciba el slice sellado de grants; instancia R2 otorga SELECT exclusivamente a las 3 tablas de Turno y built-ins mínimos. | Alterar factory o grants R1 de `public.reserva`, alterar SQLState 42501, otorgar privilegios globales o modificar pruebas R1 existentes. |
| 2 | `src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/testinfra/F2eStatementPolicyInspector.java` | `512da4beb1aa9c845aaab02097b2ab798a5fa9f7b18b0d45cb9374ad8408be94` | Parametrizar por catálogo sellado; vincular plan/inspector/JDBC para catálogo R2; mantener normalizador y hash común. | Incorporar sentencias R2 al catálogo default R1, permitir snapshot en R1, relajar denylist o alterar aserciones R1. |
| 3 | `src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/testinfra/F2eSliceChecksum.java` | `db4757b6cb7b52087ccfa2da70505844dd935a78d016e34fdbeb64a5ca96ca25` | Incorporar soporte neutral explícito para todas las columnas en orden de esquema ordinal y PK física/compuesta para los dominios R2. | Alterar selectores, codecs, orden o hashing del slice de Reserva; omitir columnas de tablas de Turno. |
| 4 | `src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/ReservaJpaReaderArchitectureTest.java` | `e8303ef30a703f39b939624d6dd4d5252a8fe90ab07bd442cb81a2d976625009` | Composición de allowlists selladas conformes a §3; registrar colecciones disjuntas `R1main11`, `R1test10`, `R2main12`, `R2test10`. | Modificar o relajar los conjuntos R1 originales; sustituir enumeraciones exactas por filtros por prefijo, regex o comodines. |

### 2.4 `CURRENT_R2_READ_ONLY` — Dependencias Activas de Lectura (exact34)
Archivos existentes en el árbol actual de `main` que pueden ser leídos o importados durante la implementación sin sufrir alteración alguna:

#### A. Documentación Canónica y Proceso Activo (exact7)
```text
auditoria/fase-2e-r2-diseno-lector-turno-legacy-integracion.md
auditoria/handoffs/HANDOFF-F2E-R2-DISENO-LECTOR-TURNO-LEGACY.md
auditoria/reviews/F2E-R2-REVIEW-DISENO-LECTOR-TURNO-LEGACY.md
auditoria/ESTADO-ACTUAL.md
auditoria/orquestacion/F2E-RUNBOOK.md
auditoria/orquestacion/F2E-EXECUTION-POLICY.md
auditoria/orquestacion/F2E-STATE.json
```

#### B. Núcleo Puro del Detector, Esquema y Entidades Legacy (exact10)
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

#### C. Lector R1 Reserva JPA — Producción Inmutable (exact11)
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

#### D. Lector R1 Reserva JPA — Pruebas Inmutables (exact6)
```text
src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/ReservaJpaReaderPostgreSqlTest.java
src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/ReservaJpaReaderRuntimeIsolationTest.java
src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/ReservaJpaReaderTransactionTest.java
src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/ReservaProjectionMapperTest.java
src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/testinfra/F2ePostgresTestConfiguration.java
src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/testinfra/ReaderTransactionTestHarness.java
```

### 2.5 `CURRENT_R2_PROVENANCE_ONLY` — Procedencia Histórica Excluida de la Allowlist Activa (exact6)
Los siguientes artefactos históricos forman parte del registro de procedencia inmutable en Git (commits `6140978bfd7b723fbbf9ddde1b5b5ba4f777c43c` y predecesores), pero **NO** forman parte de la allowlist activa de dependencias en clean-main:

```text
auditoria/orquestacion/README.md
auditoria/orquestacion/WORKFLOW.md
auditoria/orquestacion/STATE-MACHINE.md
auditoria/orquestacion/GATES.md
auditoria/orquestacion/ROLES.md
auditoria/handoffs/HANDOFF-F2E-R2-IMPLEMENTACION-LECTOR-TURNO-LEGACY.md
```

Razón: los cinco primeros corresponden al protocolo multiagente legacy ORQ-1 (reemplazado por Orca Product Delivery) y no están presentes en el árbol de trabajo de `main`. El sexto es el handoff histórico cuya autoridad es continuada y actualizada por este documento.

### 2.6 `FORBIDDEN_EXPLICIT` y Restricciones Estrictas de Dominio
Queda terminantemente prohibido crear, modificar o tocar cualquiera de las siguientes rutas o paquetes (incluyendo todos sus descendientes):

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

Restricciones suplementarias obligatorias:
- Prohibida la implementación de fases R3 a R6.
- Prohibida la activación productiva, controllers, endpoints REST, jobs o listeners para R2.
- Prohibido cualquier cutover o sustitución de servicios vivos.
- Preservación estricta de la autoridad productiva de `TurnoInstructor` (`LEGACY_VIVO / PRODUCTIVO`).
- Preservación estricta del aislamiento de reservas (escrituras y capacidad pertenecen a Reserva).
- Prohibida cualquier modificación de scripts de migración Flyway (techo se mantiene en V46; V47 ausente).
- Pagos (`pagos`) y Notificaciones (`notificaciones`) permanecen fuera de alcance.

---

## 3. Hashes Deterministas de Sets y Contenido

### 3.1 Algoritmos de Hash Empleados
- **Hash de archivo individual**: `SHA-256` estándar sobre los bytes completos del archivo en disco.
- **Hash de conjunto de rutas (path-set hash)**: las rutas relativas canónicas del conjunto se ordenan alfabéticamente (orden lexicográfico ASCII/UTF-8), se concatenan separadas por un carácter de salto de línea (`\n`), finalizando con un salto de línea terminal (`\n`), y se calcula el `SHA-256` sobre dicha representación en bytes UTF-8.

### 3.2 Hashes Deterministas de Conjuntos de Rutas

| Conjunto | Cardinalidad | Path-Set SHA-256 |
| --- | --- | --- |
| `CURRENT_R2_AUTHORIZED_NEW` | 22 | `21a2300e72ad63e0b3d06f5fdb81ff4215952ee5bdbf6f234ad7fc55adfd09f3` |
| `CURRENT_R2_AUTHORIZED_MODIFIED` | 4 | `0249c0508404ae27f855457440501175153c73701a28f88e804ca3466d0a5c6a` |
| **WRITE_SCOPE (`NEW` ∪ `MODIFIED`)** | **26** | `e32e6c04c5fec4f9c406ff27f57abec39b61f58ad77f75cf9e192007d4cb6028` |
| `CURRENT_R2_READ_ONLY` | 34 | `da34f1a22e865b5993386ec07f675a2c20c5785e0ee3922c31047b6abbfc4457` |
| `CURRENT_R2_PROVENANCE_ONLY` | 6 | `8557d930091c525532da43da098e6730179083403da6bd3d9239c18e0dfc8262` |
| **TOTAL_ACTIVE_PATHS (`WRITE_SCOPE` ∪ `READ_ONLY`)** | **60** | `0e429c517bd93ec94ade3ae6c92c33bd1d8e5f00153ab4fb7bdc331abd007ace` |

*Nota de verificación*: El hash de `WRITE_SCOPE` (`e32e6c04c5fec4f9c406ff27f57abec39b61f58ad77f75cf9e192007d4cb6028`) coincide exactamente con el hash del allowlist histórico, certificando matemáticamente que la superficie de escritura autorizada para la implementación es idéntica y no ha sufrido derivas.

---

## 4. Delta Acotado del Guard Arquitectónico `ReservaJpaReaderArchitectureTest`

El archivo `ReservaJpaReaderArchitectureTest.java` debe conservar literalmente los conjuntos de clases R1 existentes:

```java
// R1main11
Set<String> R1_MAIN = Set.of(
    "com.feelingpilates.transicion.programacion.adapter.jpa.ReservaJpaReader",
    "com.feelingpilates.transicion.programacion.adapter.jpa.mapper.ReservaProjectionMapper",
    "com.feelingpilates.transicion.programacion.adapter.jpa.policy.F2eSqlPolicyViolationException",
    "com.feelingpilates.transicion.programacion.adapter.jpa.projection.ReservaProjectionQueryExecutor",
    "com.feelingpilates.transicion.programacion.adapter.jpa.projection.ReservaProjectionRow",
    "com.feelingpilates.transicion.programacion.read.ReadSnapshotContext",
    "com.feelingpilates.transicion.programacion.read.ReadSnapshotIdentifiers",
    "com.feelingpilates.transicion.programacion.read.ReservationReadException",
    "com.feelingpilates.transicion.programacion.read.ReservationReadFailureCode",
    "com.feelingpilates.transicion.programacion.read.ReservationReadPort",
    "com.feelingpilates.transicion.programacion.read.ReservationScope"
);

// R1test10
Set<String> R1_TEST = Set.of(
    "com.feelingpilates.transicion.programacion.adapter.jpa.ReservaJpaReaderArchitectureTest",
    "com.feelingpilates.transicion.programacion.adapter.jpa.ReservaJpaReaderPostgreSqlTest",
    "com.feelingpilates.transicion.programacion.adapter.jpa.ReservaJpaReaderRuntimeIsolationTest",
    "com.feelingpilates.transicion.programacion.adapter.jpa.ReservaJpaReaderTransactionTest",
    "com.feelingpilates.transicion.programacion.adapter.jpa.ReservaProjectionMapperTest",
    "com.feelingpilates.transicion.programacion.adapter.jpa.testinfra.F2ePostgresTestConfiguration",
    "com.feelingpilates.transicion.programacion.adapter.jpa.testinfra.F2eSelectOnlyRole",
    "com.feelingpilates.transicion.programacion.adapter.jpa.testinfra.F2eSliceChecksum",
    "com.feelingpilates.transicion.programacion.adapter.jpa.testinfra.F2eStatementPolicyInspector",
    "com.feelingpilates.transicion.programacion.adapter.jpa.testinfra.ReaderTransactionTestHarness"
);
```

La modificación permitida consiste únicamente en componer los allowlists sellados para verificar independientemente:
- `actualR1Main == R1_MAIN`
- `actualR1Test == R1_TEST`
- `actualR2Main == R2_MAIN` (exactamente las 12 clases de producción de §2.1)
- `actualR2Test == R2_TEST` (exactamente las 10 clases de prueba de §2.2)
- `R1_MAIN ∩ R2_MAIN == Ø`
- `R1_TEST ∩ R2_TEST == Ø`
- `actualMain == R1_MAIN ∪ R2_MAIN`
- `actualTest == R1_TEST ∪ R2_TEST`

Cualquier clase no registrada, faltante o superpuesta produce fallo inmediato del test.

---

## 5. Requisitos Transaccionales, Recursos Físicos y Probes

### 5.1 Semántica Transaccional del Reader
Firma obligatoria en `LegacyTurnJpaReader.java`:

```java
@Transactional(
    transactionManager = "f2eR2ReaderTransactionManager",
    propagation = Propagation.MANDATORY,
    readOnly = true)
LegacyTurnReadSet readForDate(LegacyTurnReadContext context, LegacyTurnScope scope);
```

- Prohibido abrir transacciones nuevas (`REQUIRES_NEW`), suspender transacciones o degradar el aislamiento.
- Si se invoca fuera de una transacción o con un transaction manager incorrecto, debe fallar antes de emitir cualquier consulta SQL.

### 5.2 Owner Transaccional de Pruebas
Exclusivamente en `LegacyTurnTransactionTestOwner.java` (testinfra):

```java
@Transactional(
    transactionManager = "f2eR2ReaderTransactionManager",
    propagation = Propagation.REQUIRES_NEW,
    isolation = Isolation.REPEATABLE_READ,
    readOnly = true)
```

- Reader y Owner deben ser proxies distintos; la auto-invocación no prueba propagación transaccional.
- Nombres de beans y calificadores exactos:
  * `f2eR2PrivilegedDataSource`
  * `f2eR2ReaderDataSource`
  * `f2eR2StatementPolicyInspector`
  * `f2eR2ReaderEntityManagerFactory`
  * `f2eR2ReaderEntityManager`
  * `f2eR2ReaderTransactionManager`
  * `legacyTurnProjectionQueryExecutor`
  * `legacyTurnProjectionMapper`
  * `legacyTurnJpaReader`
  * `legacyTurnTransactionTestOwner`

### 5.3 Probes de Sesión y Verificación de Conexión Física
Por cada invocación transaccional se exige verificar:
1. Misma instancia de Hibernate `Session`, `EntityManagerHolder` y conexión física `PgConnection`.
2. Probes de validación de conexión inicial y final mediante las sentencias canónicas de probes:
   - `R2_TX_ISOLATION_V1`: `SHOW transaction_isolation` o `current_setting('transaction_isolation')`.
   - `R2_TX_READ_ONLY_V1`: `SHOW transaction_read_only` o `current_setting('transaction_read_only')`.
   - `R2_TX_RESOURCE_IDENTITY_V1`: `SELECT current_database(), current_schema()`.
   - `R2_TX_SNAPSHOT_V1`: `SELECT pg_current_snapshot()::text`.
3. Snapshot consistency: el snapshot textual obtenido al inicio de la transacción debe ser exactamente idéntico al obtenido al cierre de la misma (`snapshot_initial == snapshot_final`).
4. Prohibida la invocación de `Connection.getSchema()`, `getCatalog()`, inspección de metadatos o traversal durante la ejecución de negocio.

---

## 6. Catálogo SQL Cerrado, Binding y Short-Circuit

### 6.1 Catálogo SQL Cerrado (exact6)
El ejecutor utiliza exclusivamente las seis sentencias SQL canónicas validadas en el diseño §4:

| Sentencia ID | Hash Canónico (Diseño §4) | Rol | Longitud |
| --- | --- | --- | --- |
| `R2_LEGACY_MEMBERS_V1` | `9852b6e9487a71cb47d76e834e82eceafcfbdd194b6e66d718f7da1ccdf3a769` | DATA1: Cabecera de turnos + LEFT JOIN usuarios. | 579 bytes |
| `R2_LEGACY_ASSIGNMENTS_V1` | `6b21c28ee8961f783e986704604791181aa175592abc0b73553fa321445ce219` | DATA2: Asignaciones de instructores para turnos usables. | 290 bytes |
| `R2_TX_ISOLATION_V1` | `4a669a2f628e12468e0d532889e0bcafd38c09a5aadfb27f2f20f56159c1671e` | Verificación de aislamiento `repeatable read`. | Probe |
| `R2_TX_READ_ONLY_V1` | `9963ea856cdf9bfb3e9c440b1c3a6c062fd375a906f465cbe7a7ac88878716c7` | Verificación de modo `read only = on`. | Probe |
| `R2_TX_RESOURCE_IDENTITY_V1` | `0ed00ba3ec87635658759a60f48bc9ea57df82a338b863f23be641fdf6b7b3ad` | Identidad de BD y esquema activos. | Probe |
| `R2_TX_SNAPSHOT_V1` | `24f02692f50e880b77a78f9ea64501ce276b7b0e2a93c880a4c23e03bbe19aa0` | Captura de ID textual de snapshot PostgreSQL. | Probe |

Cualquier discrepancia de bytes, SQL adicional, uso de `WITH`, `SET`, bloqueos forzados o consultas no catalogadas debe ser rechazada antes de tocar JDBC.

### 6.2 Bindings y Ordenamiento de UUIDs
- Binds tipados:
  * `MEMBERS`: `salonIds` (List<UUID>), `active` (Boolean), `recurrentType` (String), `dayOfWeek` (Short), `exceptionType` (String), `cancellationType` (String), `fecha` (LocalDate).
  * `ASSIGNMENTS`: `turnIds` (List<UUID>).
- Las colecciones en los binds SQL se ordenan según el orden natural de `UUID` en Java (`compareTo`).
- La identidad en memoria, agrupación, URNs y checksums utilizan ordenamiento unsigned de bytes (`UUID16 unsigned`).
- Hibernate expande las listas; prohibido construir SQL dinámico mediante concatenación de cadenas.

### 6.3 Orden de Ejecución y Reglas de Short-Circuit
Orden canónico de ejecución:
```text
Iinitial → Rinitial → RESOURCEinitial → Sinitial → MEMBERS
→ [ASSIGNMENTS sólo si existen parent turnIds válidos y no vacíos]
→ Ifinal → Rfinal → RESOURCEfinal → Sfinal
→ Verificación nativa de conexión → Commit/Completion real
```

Reglas de short-circuit:
- Si `MEMBERS` devuelve 0 filas: se omite `ASSIGNMENTS`, se ejecutan las probes finales y se retorna un `LegacyTurnReadSet` vacío tras el commit exitoso.
- Si `MEMBERS` devuelve filas pero ninguna contiene un parent UUID válido/usable: se omite `ASSIGNMENTS` y se produce aborto all-or-nothing con rechazo total; nunca retorno vacío engañoso.
- Ante errores de payload con parent UUID usable: se ejecuta `ASSIGNMENTS` para completar la cardinalidad $K$ y se aborta posteriormente con todos los rechazos consolidados.
- Fallo físico, de conexión o de política SQL: aborto inmediato sin ejecutar consultas subsiguientes.

---

## 7. SELECT-Only, Checksums de Corte y Ausencia Total de Escritura

### 7.1 Rol de Base de Datos SELECT-Only Efímero
El usuario de base de datos utilizado por R2 en los tests con PostgreSQL real debe ser un rol estrictamente desprivilegiado:
- Conexión: `CONNECT`, `USAGE` sobre esquema `public`.
- Permisos exclusivamente de `SELECT` sobre exactamente tres tablas:
  * `public.turno_instructor`
  * `public.turno_instructor_usuario`
  * `public.turno_instructor_asignacion`
- Sin permisos de superusuario, sin `bypassRLS`, sin `SET ROLE`, sin `CREATE`, sin tablas temporales, sin permisos sobre secuencias ni funciones administrativas.
- Se exige prueba negativa explícita: intentos de `INSERT`, `UPDATE`, `DELETE` o `DDL` deben fallar con SQLState `42501` (`insufficient_privilege`).

### 7.2 Checksums Persistidos de Corte (Slice Checksum)
Para garantizar la ausencia absoluta de modificaciones colaterales:
1. El observador privilegiado calcula el checksum antes de la invocación (`before`) sobre todas las columnas en orden de esquema ordinal para las 3 tablas legacy en el scope probado.
2. Se ejecuta el lector R2 en una ventana quiescente.
3. El observador calcula el checksum después de la invocación (`after`).
4. Se exige `before == after` para las tres tablas, incluso si el scope está vacío.
5. Los conteos de filas, hashes de tablas y hash consolidado del slice deben coincidir exactamente.

---

## 8. Integración con Flyway y Entorno de Pruebas PostgreSQL

- Las pruebas de integración se ejecutan contra contenedores reales de PostgreSQL 16 (`postgres:16-alpine`) utilizando Testcontainers; el uso de H2 en memoria queda estrictamente prohibido.
- La configuración descubre dinámicamente el head actual de migraciones de Flyway integrado en `origin/main` (actualmente `V46`).
- Prohibido asumir o hardcodear `V47` o conteos históricos de migraciones (`applied50`).
- Se ejecutan las fases `migrate` y `validate` de Flyway, asegurando cero migraciones pendientes o fallidas y validando el esquema con `ddl-auto=validate`.

---

## 9. Comandos de Validación Operacional (FAST y GATE)

### 9.1 FAST — Feedback Rápido de Superficie Modificada
Para validación iterativa durante el desarrollo una vez autorizada la implementación:

```bash
./mvnw -Dtest=LegacyTurnProjectionMapperTest,LegacyTurnProjectionQueryExecutorTest,LegacyTurnR2ArchitectureTest test
./mvnw -Dtest=LegacyTurnJpaReaderTransactionTest,LegacyTurnJpaReaderRuntimeIsolationTest test
git diff --check
git diff --cached --check
```

*Regla*: FAST es únicamente para depuración local; **nunca** sustituye el GATE, la auditoría independiente ni autoriza aceptación o publicación.

### 9.2 GATE — Criterio de Aceptación Técnica Completa
Requisitos cumulativos indispensables sobre un candidate completo:

```bash
./mvnw -Dtest=LegacyTurnProjectionMapperTest,LegacyTurnProjectionQueryExecutorTest,LegacyTurnJpaReaderPostgreSqlTest,LegacyTurnJpaReaderTransactionTest,LegacyTurnJpaReaderConcurrencyTest,LegacyTurnJpaReaderRuntimeIsolationTest,LegacyTurnR2ArchitectureTest test
./mvnw -Dtest=ReservaProjectionMapperTest,ReservaJpaReaderPostgreSqlTest,ReservaJpaReaderTransactionTest,ReservaJpaReaderRuntimeIsolationTest,ReservaJpaReaderArchitectureTest test
./mvnw test
git diff --check
git diff --cached --check
```

Condiciones indispensables para PASS en GATE:
1. Exactamente 22 archivos nuevos y 4 modificados; cero archivos fuera de allowlist.
2. 100% de tests unitarios, de arquitectura y de integración verdes en Testcontainers PG16.
3. Regresión completa del repositorio verde (`mvn test`).
4. Verificación de snapshot Repeatable Read `snapshot_initial == snapshot_final`.
5. Verificación de 4 escrituras denegadas con SQLState `42501`.
6. Verificación de invariancia en checksums de corte (`before == after`).
7. Concurrencia real validada con escritor simultáneo.
8. Cero cambios en `src/` productivo fuera del scope permitido.
9. Auditoría independiente fresh con veredicto APPROVED, P0=0, P1=0.

---

## 10. Reconciliación con Autoridades de Proceso Vigentes

Este documento opera bajo el marco de **Orca Product Delivery**:
- Gobierna el ciclo técnico mediante `F2E-RUNBOOK.md` y `F2E-EXECUTION-POLICY.md`.
- El estado operativo se refleja de manera derivada en `auditoria/orquestacion/F2E-STATE.json` y `auditoria/ESTADO-ACTUAL.md`.
- Las referencias al protocolo multiagente histórico (`ORQ-1`: `README.md`, `WORKFLOW.md`, `STATE-MACHINE.md`, `GATES.md`, `ROLES.md`) quedan formalmente reclasificadas como `LEGACY_PROCESS_PROVENANCE_ONLY`.
- La orquestación activa, validación de gates y toma de decisiones se realizan mediante los procedimientos vigentes de Orca Product Delivery.

---

## 11. Estado Terminal de este Handoff y Próximo Lifecycle

Al materializarse y publicarse este documento en el repositorio:

```text
R1 Reserva JPA Reader: INTEGRATED / ACCEPTED / INMUTABLE
R2 Diseño: COMPLETE / AUDITED / PUBLISHED / CLOSED
R2 Implementation Handoff Sucesor: RECONCILED / AUDITED / PUBLISHED / NOT_ACTIVE
Handoff histórico R2: PROVENANCE_ONLY / IMMUTABLE
R2 Implementación: NOT_AUTHORIZED / NOT_IMPLEMENTED
R2 Activación: NOT_ACTIVE
TurnoInstructor Productivo: LEGACY_VIVO / PRODUCTIVO (intacto)
Dark Launch: PRESERVADO
Cutover: NOT_AUTHORIZED
Fases R3 a R6: NOT_AUTHORIZED
```

**Siguiente Lifecycle Autorizado**:
Únicamente la **Activación Formal del Handoff R2 Reconciliado** (`R2_HANDOFF_ACTIVATION`) en un ciclo separado.
Este documento **NO** autoriza el inicio de la implementación de R2.
