# FeelingPilates — F2E / diseño de adapters read-only y consistencia de snapshot

## 1. Identidad de la unidad y lifecycle

```text
Unidad: F2E / boundary de readers JPA hacia detector puro
Tipo: DESIGN / RESEARCH
Role: DESIGN_EXECUTOR / RESEARCHER
Correction role F2E-ADAPTERS-SNAPSHOT-DESIGN.1.2: DESIGN_CORRECTOR / DOCUMENT_CORRECTOR
Correction role F2E-ADAPTERS-SNAPSHOT-AUTHORITY-GAP-R1: DESIGN_CORRECTOR / AUTHORITY_GAP_RESOLVER
Execution profile: DOCUMENTAL / READ_ONLY_RESEARCH
Checkpoint: auditoria/fase-2e-diseno-adapters-read-only-snapshot-consistency.md
Estado máximo de este output: ADAPTERS / SNAPSHOT CONSISTENCY DESIGN MATERIALIZED
Design gate: PENDING / NOT_PERFORMED
Implementation: NOT_AUTHORIZED
DB access: NOT_AUTHORIZED
Data audit: NOT_AUTHORIZED / NOT_PERFORMED
```

Este checkpoint es el único output físico autorizado por
`auditoria/handoffs/HANDOFF-F2E-DISENO-ADAPTERS-READ-ONLY-SNAPSHOT-CONSISTENCY.md`. Cierra el
diseño del límite JPA hacia el núcleo puro ya materializado; no implementa readers, queries,
projections, coordinator, tests, configuración, SQL, migraciones, reportes ni acceso a datos.

Materializar este documento no equivale a aprobarlo. Su siguiente gate es exclusivamente un
`FRESH_INDEPENDENT_DESIGN_DOCUMENT_AUDIT` con `P0=0 / P1=0`.

## 2. Base Git y autoridad de inicio

Pre-flight físico de esta ejecución:

```text
Repository: /Users/jesusaldaircruzortiz/Desktop/Feelingpilates/feelingpilates
Branch: operacion/excepciones-horario-fecha
HEAD: eeb35d00213543299287466f466cde04b3e34ab9
Initial staging: VACÍO
Initial working tree: CLEAN
Baseline dirty autorizado: NINGUNO
```

Pre-flight físico de la corrección focalizada F2E-ADAPTERS-SNAPSHOT-DESIGN.1.1:

```text
Branch: operacion/excepciones-horario-fecha
HEAD: eeb35d00213543299287466f466cde04b3e34ab9
Staging: VACÍO
Working tree / baseline autorizado:
?? auditoria/fase-2e-diseno-adapters-read-only-snapshot-consistency.md
Unexpected paths: NINGUNO
Role: DESIGN_CORRECTOR / DOCUMENT_CORRECTOR
```

Pre-flight físico de la corrección focalizada F2E-ADAPTERS-SNAPSHOT-DESIGN.1.2:

```text
Branch: operacion/excepciones-horario-fecha
HEAD: eeb35d00213543299287466f466cde04b3e34ab9
Staging: VACÍO
Working tree / baseline autorizado:
?? auditoria/fase-2e-diseno-adapters-read-only-snapshot-consistency.md
Unexpected paths: NINGUNO
Role: DESIGN_CORRECTOR / DOCUMENT_CORRECTOR
```

Autoridad comprobada:

```text
Active handoff: APPROVED / ACTIVE
Target: F2E / boundary de readers JPA hacia detector puro
Type: DESIGN / RESEARCH
Target: AUTHORIZED_TO_START
Target started before this execution: NO
Checkpoint before this execution: NOT_CREATED / PENDING
Design gate: PENDING / NOT_PERFORMED
Pure detector core: IMPLEMENTATION CLOSED / TECHNICAL IMPLEMENTATION GATE PASS
F2E identity/semantic detector design: CLOSED / PASS
F2E.1 preparation: CLOSED / PASS
F2D.1: CLOSED / PASS
F2D.2: CLOSED / DARK_LAUNCH / NOT_PRODUCTIVE
Implementation: NOT_AUTHORIZED
DB: NOT_AUTHORIZED
Data audit: NOT_AUTHORIZED / NOT_PERFORMED
Data source: DATA_SOURCE_NOT_AVAILABLE
Authority: TurnoInstructor / LEGACY_VIVO / PRODUCTIVO
Cutover: false
```

No se encontró contradicción entre handoff, review, `ESTADO-ACTUAL`, canónicos y evidencia física.
La diferencia entre los SHA históricos de esos documentos y el `HEAD` operativo no es una
contradicción: los canónicos exigen obtener el `HEAD` por pre-flight y conservar los cortes
históricos.

## 3. Scope y prohibiciones

### 3.1 Scope realizado

- inventario físico de readers, entities, associations, repositories y consumers relevantes;
- campos exactos, nullability, query/projection shape y mapping inmutable por source;
- boundary de managed entities y lazy loading;
- definición y matriz de consistencia por evaluación;
- decisión de transacción, aislamiento y fallo cerrado;
- diseño de non-mutation, runtime isolation, packages y dependencias;
- slicing futuro, coordinator, pruebas, HostValidator y prerrequisitos de data audit;
- creación exclusiva de este checkpoint.

### 3.2 Scope prohibido y no realizado

```text
src/main/**: NO MODIFICADO
src/test/**: NO MODIFICADO
pom.xml: NO MODIFICADO
src/main/resources/**: NO MODIFICADO
Flyway/schema/SQL: NO MODIFICADO / NO EJECUTADO
DB connection/query: NO EJECUTADA
Data audit/report material: NO EJECUTADO
Adapters/projections/coordinator: NO IMPLEMENTADOS
Crosswalk/selection/resolver/fence: NO IMPLEMENTADOS
Reserva/ReservaService/TurnoInstructor: NO MODIFICADOS
Frontend/mobile/controllers/jobs/listeners/runners: NO MODIFICADOS
Migration/normalization/cutover/authority change: NO
git add/commit/push: NO
```

## 4. Inputs obligatorios y evidencia física

Se leyeron completos:

- `AGENTS.md` y `auditoria/orquestacion/{README,WORKFLOW,STATE-MACHINE,GATES,ROLES}.md`;
- el handoff activo, su review y `auditoria/ESTADO-ACTUAL.md`;
- los canónicos `README-REESTRUCTURACION`, `ARQUITECTURA-ACTUAL`,
  `DECISIONES-ARQUITECTONICAS`, `REGLAS-DE-TRABAJO-IA`, `DOMINIO-FUNCIONAL` y
  `MAPA-LEGACY-Y-MIGRACION`;
- F2E.1, su review; el diseño de identidad/detector, su review; el handoff del núcleo puro y su
  review técnico;
- F2D.1, su review final; F2D.2 y su review documental.

Evidencia física inspeccionada read-only:

- `Reserva`, `ReservaRepository`, `ReservaService` y su caracterización;
- `TurnoInstructor`, `TurnoInstructorAsignacion`, ambos repositories,
  `TurnoInstructorService` y sus caracterizaciones;
- `BloqueProgramacion`, `Asignacion`, `AsignacionRepository`, `ProgramacionNominal`;
- `AjusteProgramacionFecha`, su repository/persistence/service,
  `AplicadorAjustesProgramacion`, `ProgramacionEfectiva`, `ProgramacionValidador`;
- `OcurrenciaNominal`, `OcurrenciaEfectiva`, `ReferenciaOcurrencia`;
- `HorarioOperacion`, `SalonHorarioExcepcion`, sus repositories,
  `HorarioOperacionResolver`, `HorarioEfectivoSalon`;
- maestros `Salon`, `Usuario`, `UsuarioRol`, `Rol`, `TipoActividad` y sus repositories;
- migraciones V15, V17–V22, V41 y V43–V47;
- `pom.xml`, `application*.properties`, `docker-compose.yml` y configuración Testcontainers;
- tests de persistencia, composición, fail-closed, dark launch y arquitectura;
- todos los tipos production del núcleo puro
  `com.feelingpilates.transicion.programacion.detector/**`.

Hechos determinantes:

1. `Reserva` y `TurnoInstructor` se leen hoy como entities y tienen associations `LAZY`.
2. `AsignacionRepository.buscarNominalesDeFecha` ya demuestra que una projection nativa es viable,
   pero sus ocho campos no contienen toda la provenance física requerida por un data audit.
3. `AjusteProgramacionFechaRepository` devuelve entities sin associations, pero managed y
   mutables.
4. `ProgramacionEfectiva` ejecuta varias lecturas: nominales, ajustes, operación y maestros. Su
   `@Transactional(readOnly=true)` no declara isolation.
5. No existe configuración de isolation, `PlatformTransactionManager` custom ni default
   transaccional custom. Spring usa `ISOLATION_DEFAULT`; PostgreSQL tiene `READ COMMITTED` como
   default.
6. El datasource es PostgreSQL; Docker/Testcontainers usa `postgres:16-alpine`; Flyway está
   habilitado y JPA usa `ddl-auto=validate`.
7. El core acepta sólo snapshots inmutables y no conoce Spring/JPA/repositories. Este diseño no lo
   modifica.
8. `pom.xml` fija Spring Boot `4.1.0`; su dependency management resuelve Hibernate ORM
   `7.4.1.Final`. La API física `org.hibernate.query.CommonQueryContract`, heredada por
   `NativeQuery`, contiene `setParameterList(String, Collection, Class)` para expresiones
   `IN (:values)`. Esa extensión Hibernate disponible, y no una capacidad multivaluada del estándar
   Jakarta Persistence, fundamenta el binding cerrado de 12.2.

## 5. Invariante de boundary

```text
JPA / repositories / managed entities
        ↓
adapter boundary
        ↓
immutable snapshots
        ↓
pure detector core
```

Está prohibida toda dependencia o entrega en sentido inverso:

```text
pure detector core
        -/-> JPA / Spring / repository / EntityManager / managed entity
```

Una entity detached tampoco es un snapshot válido. Sólo pueden salir de la transacción records,
enums, UUIDs, fechas/horas, strings, maps/listas defensivamente copiadas y los tipos inmutables ya
materializados del core. Ninguna entity, proxy Hibernate, `PersistentCollection`, projection proxy
Spring Data ni `Page` cruza el boundary.

## 6. Inventario de readers

| Source | Authority | Repository/reader actual | Forma actual | Fields disponibles | Fields requeridos/ausentes | Historia | Managed/lazy risk | Target | Slice |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| Reserva | `Reserva` productiva | `ReservaRepository` | `Reserva` entity | ID, IDs relacionados, fecha, rango, estado, timestamps | Falta cualquier target de programación persistido | Snapshot del hecho; target histórico desconocido | Cuatro `ManyToOne LAZY`; alta | `ReservationSourceSnapshot`, siempre sin historical target desde esta fuente | R1 |
| Turno legacy | `TurnoInstructor` productivo | `TurnoInstructorRepository` + `TurnoInstructorAsignacionRepository` | Entity aggregate | turno, tipo, activo, salón, día/fecha, rango, miembros, asignaciones, timestamps del turno | Falta serie, vigencia funcional, intención/target puntual e historia de asignaciones | `CURRENT_SNAPSHOT_ONLY`; claim histórico recurrente=`UNKNOWN_HISTORY`; puntual preserva UNKNOWN_INTENT y marca history no persistida | salón, instructores y asignaciones `LAZY`; alta | `GenericSourceSnapshot` por átomo observable legacy | R2 |
| Programación nominal nueva | Ninguna productiva; dark launch | `AsignacionRepository.buscarNominalesDeFecha` / `ProgramacionNominal` | Projection → `OcurrenciaNominal` | referencia de serie, IDs de versiones y snapshot nominal | Projection actual omite vigencias/timestamps/serie de bloque para provenance completa | Versiones físicas actuales aplicables a fecha | Projection actual sin entity; bajo | `ProgrammingCandidateSnapshot` de tipo `NOMINAL_OCCURRENCE` cuando el claim exige nominales | R3 |
| Programación efectiva nueva | Ninguna productiva; dark launch | `ProgramacionEfectiva` | Derived immutable `OcurrenciaEfectiva` tras múltiples readers | referencia, origen, fecha, salón, instructor, actividad, rango | No expone por sí sola nominal/ajuste backing ni causa durable de omisión | Snapshot actual derivado | Entities internas de ajuste/maestros y lazy de maestros; medio | `ProgrammingCandidateSnapshot` recurrent/replacement/addition | R5 |
| Ajustes nuevos | Ninguna productiva; dark launch | `AjusteProgramacionFechaRepository` | Managed entity | tipo, fecha, target/result, activo, timestamps | Ninguno físico; falta projection read-only | Fila actual; timestamps técnicos | Sin associations, pero managed/mutable; medio | `GenericSourceSnapshot` NEW_* y provenance para nominal/effective outcome | R4 |
| Operación/maestros | Autoridad productiva compartida | repositories + `HorarioEfectivoSalon` + `ProgramacionValidador` | Entities/resolver | estado/rango operativo, activos, rol, especialidad, oferta | No son candidates ni source atoms; sólo evidence de validez | Snapshot actual | Varias collections/relations `LAZY`; alta | Evidence backing de effective universe; nunca `SourceSnapshot` independiente | R5 |

`TurnoInstructorAsignacionRepository.deleteByTurno_Id` es writer y queda expresamente excluido de
todo contrato de lectura.

## 7. Contratos exactos de Reserva

### 7.1 Estrategia seleccionada

`PROJECTION_FIRST` mediante un `ReservaProjectionQueryExecutor` dedicado, plain y adapter-local,
que usa `EntityManager.createNativeQuery` y mapea cada tupla a un record concreto
`ReservaProjectionRow`. No usa Spring Data interface projection ni modifica
`ReservaRepository`. La SQL nativa queda acotada por IDs explícitos o por
`salonIds + [desde,hasta]`, ordenada por `r.id`. No se carga `Reserva` ni se navegan asociaciones.
El contrato obtiene los IDs directamente de las columnas `r.salon_id`, `r.instructor_id` y
`r.tipo_actividad_id`; no hace join ni inicializa objetos relacionados en el mapper.
No se proyecta `cliente_id`: es PII innecesaria para el detector.

No se permite un scan ilimitado. El request exige una de estas formas:

```text
byReservationIds(non-empty ids)
byScope(non-empty salonIds, desde inclusive, hasta inclusive, bounded)
```

### 7.2 Campos exactos

| Campo lógico | Origen físico | Tipo Java | Nullable | Significado | Destino | Evidence/provenance |
| --- | --- | --- | --- | --- | --- | --- |
| `reservationId` | `reserva.id` | `UUID` | NO | Identidad source | `reservationId`, `sourceIdentity` | `recordIds[0]` |
| `state` | `reserva.estado` | enum/string | NO | Estado observado | `ReservationState` | normalized `state` |
| `date` | `reserva.fecha` | `LocalDate` | NO | Fecha del hecho reservado | `date` | business date |
| `salonId` | `reserva.salon_id` | `UUID` | NO | Salón snapshot | `salonId` | matching dimension |
| `instructorId` | `reserva.instructor_id` | `UUID` | NO | Instructor snapshot | `instructorId` | matching dimension |
| `activityId` | `reserva.tipo_actividad_id` | `UUID` | NO | Actividad snapshot | `activityId` | matching dimension |
| `start` | `reserva.hora_inicio` | `LocalTime` | NO | Inicio half-open | `ReservedSubinterval.start` | containment evidence |
| `end` | `reserva.hora_fin` | `LocalTime` | NO | Fin half-open; debe ser `> start` | `ReservedSubinterval.end` | containment evidence |
| `createdAtTechnical` | `reserva.creado_en` | `OffsetDateTime` | NO | Inserción técnica, no vigencia | `additionalObservableFields` | provenance solamente |
| `updatedAtTechnical` | `reserva.actualizado_en` | `OffsetDateTime` | NO | Última escritura técnica, no historia funcional | `additionalObservableFields` | provenance solamente |

Mapping cerrado:

```text
ReservationSourceSnapshot(
  reservationId, state, date, salonId, instructorId, activityId,
  ReservedSubinterval(start,end), snapshotIdentity, sourceFingerprint,
  {createdAtTechnical, updatedAtTechnical}, Optional.empty(), provenance)
```

`historicalProgrammingTarget=Optional.empty()` es obligatorio para toda fila leída sólo desde el
schema actual. Un target histórico demostrado sólo puede venir de otra fuente futura nombrada y
autorizada; nunca se deduce de contención, coincidencia de campos o un turno vigente actual.

```text
target histórico desconocido -> Optional.empty(); evaluación normal 0..N
target histórico demostrado -> HistoricalProgrammingTargetSnapshot desde evidence externa
                               autorizada, nunca desde ReservaReader
```

Shape inválida, enum desconocido, null físico inesperado o rango no positivo es `ADAPTER_INPUT_INVALID`,
no `MISSING` y no un snapshot parcial.

## 8. Contratos exactos de TurnoInstructor legacy

### 8.1 Estrategia seleccionada

`PROJECTION_FIRST` con dos consultas SQL nativas escalares ejecutadas por un único
`LegacyTurnProjectionQueryExecutor` plain y adapter-local mediante `EntityManager`; el executor
produce los records concretos `LegacyTurnMemberRow` y `LegacyAssignmentRow`. No usa ni modifica
`TurnoInstructorRepository` o `TurnoInstructorAsignacionRepository`. Las dos consultas viven en
ese executor y se ejecutan dentro de la misma transacción suministrada. Se
rechaza un único join cartesiano de `instructores × asignaciones`, porque duplicaría filas y puede
ocultar miembros sin actividad o asignaciones lógicamente huérfanas.

1. `LegacyTurnMemberRow`: header del turno más `turno_instructor_usuario` mediante `LEFT JOIN`.
2. `LegacyAssignmentRow`: PK y rango de `turno_instructor_asignacion` para los mismos turnos.

Ambas consultas se acotan por scope de salón y fecha/día de evaluación, incluyen activo como campo
y filtran las filas relevantes del snapshot actual. Orden estable:

```text
members:     turno.id, member_usuario_id NULLS FIRST
assignments: turno_id, usuario_id, tipo_actividad_id
```

### 8.2 Projection `LegacyTurnMemberRow`

| Campo lógico | Origen físico | Tipo | Nullable | Significado/destino |
| --- | --- | --- | --- | --- |
| `turnId` | `turno_instructor.id` | `UUID` | NO | Source record ID |
| `type` | `turno_instructor.tipo` | enum/string | NO | `LEGACY_RECURRENTE`, `LEGACY_EXCEPCION` o `LEGACY_CANCELACION` |
| `active` | `turno_instructor.activo` | `boolean` | NO | Estado actual observable |
| `salonId` | `turno_instructor.salon_id` | `UUID` | NO | Salón observable |
| `dayOfWeek` | `turno_instructor.dia_semana` | `Short` | SÍ | Requerido sólo para recurrente; 0=domingo |
| `date` | `turno_instructor.fecha` | `LocalDate` | SÍ | Requerido sólo para excepción/cancelación |
| `turnStart` | `turno_instructor.hora_inicio` | `LocalTime` | NO | Inicio del bloque |
| `turnEnd` | `turno_instructor.hora_fin` | `LocalTime` | NO | Fin del bloque |
| `createdAtTechnical` | `turno_instructor.creado_en` | `OffsetDateTime` | NO | Evidencia técnica, nunca `vigenteDesde` |
| `updatedAtTechnical` | `turno_instructor.actualizado_en` | `OffsetDateTime` | NO | Evidencia técnica, nunca `vigenteHasta` |
| `memberInstructorId` | `turno_instructor_usuario.usuario_id` | `UUID` | SÍ | Null prueba un turno sin miembro; no se inventa instructor |

### 8.3 Projection `LegacyAssignmentRow`

| Campo lógico | Origen físico | Tipo | Nullable | Significado/destino |
| --- | --- | --- | --- | --- |
| `turnId` | `turno_instructor_asignacion.turno_id` | `UUID` | NO | FK/parte de source identity |
| `instructorId` | `turno_instructor_asignacion.usuario_id` | `UUID` | NO | Instructor observable |
| `activityId` | `turno_instructor_asignacion.tipo_actividad_id` | `UUID` | NO | Actividad observable |
| `assignmentStartRaw` | `turno_instructor_asignacion.hora_inicio` | `LocalTime` | SÍ | Ambos null significa herencia del bloque |
| `assignmentEndRaw` | `turno_instructor_asignacion.hora_fin` | `LocalTime` | SÍ | Ambos null significa herencia del bloque |

Agregación y mapping cerrados:

- una fila de asignación cuyo instructor pertenece al turno produce un átomo observable con
  `sourceIdentity=turnId/instructorId/activityId`;
- si ambas horas de asignación son null, el rango efectivo observable usa
  `[turnStart,turnEnd)` y provenance conserva `assignmentStartRaw=NULL`,
  `assignmentEndRaw=NULL`, `rangeRule=LEGACY_FULL_TURN_FALLBACK`;
- si ambas son no-null, se usan literalmente y deben formar un rango positivo contenido en el
  turno;
- sólo una hora null, rango inválido/fuera del turno, asignación para no-miembro, turno sin miembro
  o miembro sin actividad son anomalías estructurales representables: se conservan como markers
  en `observableFields` y `EvidenceProvenance.normalizedFields`, sin fabricar candidate elegible;
- cada átomo produce `GenericSourceSnapshot` con source system `LEGACY`, atom type derivado
  exclusivamente de `turn.type`, fields normalizados completos, record IDs físicos y
  `CURRENT_SNAPSHOT_ONLY`;
- para `LEGACY_RECURRENTE`, un átomo estructuralmente anómalo usa
  `INCOMPATIBLE_EVIDENCE` y resulta `DIVERGENT_INCOMPATIBLE`;
- para `LEGACY_EXCEPCION`, toda forma representable, con o sin esos markers, usa exclusivamente
  `LEGACY_EXCEPTION_UNKNOWN_INTENT` y resulta `UNSUPPORTED + UNKNOWN_INTENT`; no se convierte a
  reemplazo o adición;
- para `LEGACY_CANCELACION`, toda forma representable, con o sin esos markers, usa exclusivamente
  `LEGACY_CANCELLATION_UNKNOWN_INTENT` y resulta `UNSUPPORTED + UNKNOWN_INTENT`; su rango se
  conserva como evidence y nunca targetea automáticamente una serie;
- null/imposible en un required header/PK/source type, assignment sin header correlacionable o
  duplicate físico impide formar un source confiable y aborta R2 antes del classifier.

`createdAtTechnical`/`updatedAtTechnical`, IDs y orden no reconstruyen historia. Para
`LEGACY_RECURRENTE`, un claim que requiera estado anterior usa el scenario legal
`LEGACY_HISTORY_REQUIRED` y resulta `UNKNOWN_HISTORY + UNSUPPORTED +
LEGACY_FUNCTIONAL_VALIDITY_NOT_PERSISTED`. Para `LEGACY_EXCEPCION`/`LEGACY_CANCELACION`, el core
no admite ese scenario: prevalece el scenario puntual `UNKNOWN_INTENT`, el result conserva
`historyStatus=CURRENT_SNAPSHOT_ONLY` y la falta de historia queda como marker
`LEGACY_FUNCTIONAL_VALIDITY_NOT_PERSISTED` en source/provenance. No se fuerza una combinación
`SourceAtomType/scenario/historyStatus` que `DetectorClassifier` rechace.

## 9. Programación nominal nueva

### 9.1 Estrategia seleccionada

`PROJECTION_FIRST` mediante SQL nativa en un `NominalProjectionQueryExecutor` plain y
adapter-local que usa `EntityManager` y devuelve el record concreto `NominalProjectionRow`. La
interface projection existente `AsignacionRepository.OcurrenciaNominalProjection` y
`buscarNominalesDeFecha` no se extienden, envuelven ni reutilizan: demuestran la forma básica, pero
omiten provenance y modificar el repository productivo ampliaría innecesariamente el boundary.
La futura query conserva los
predicados aprobados —ambas filas activas, ambas vigencias contienen la fecha y día del bloque— y
añade los campos exactos siguientes. Orden: `a.serie_id, a.id`.

### 9.2 Campos exactos

| Grupo | Campos físicos | Tipo/nullability | Uso |
| --- | --- | --- | --- |
| Contexto | parámetro `fecha`, `b.dia_semana` | `LocalDate` NO, `short` NO | fecha de occurrence y prueba de day convention |
| Identidad ASG | `a.serie_id`, `a.id` | `UUID` NO | `ReferenciaOcurrencia(SERIE_ASIGNACION, serieId, fecha)` y record IDs |
| Relación | `a.bloque_id`, `b.id`, `b.serie_id` | `UUID` NO | consistencia FK, versión y serie de bloque como provenance |
| Snapshot nominal | `b.salon_id`, `a.instructor_id`, `a.tipo_actividad_id`, `a.hora_inicio`, `a.hora_fin` | UUID/LocalTime NO | campos de `ProgrammingCandidateSnapshot` |
| Bloque físico | `b.hora_inicio`, `b.hora_fin` | `LocalTime` NO | validar que asignación esté contenida, sin recortar |
| Vigencia ASG | `a.vigente_desde`, `a.vigente_hasta`, `a.activo` | desde NO, hasta SÍ, activo NO | evidence de aplicabilidad inclusiva |
| Vigencia bloque | `b.vigente_desde`, `b.vigente_hasta`, `b.activo` | desde NO, hasta SÍ, activo NO | evidence de aplicabilidad inclusiva |
| Técnica ASG | `a.creado_en`, `a.actualizado_en` | `OffsetDateTime` NO | provenance técnica |
| Técnica bloque | `b.creado_en`, `b.actualizado_en` | `OffsetDateTime` NO | provenance técnica |

Los únicos null permitidos son `a.vigente_hasta` y `b.vigente_hasta`, que significan extremo
superior abierto. Cualquier otro null, rango no positivo, asignación fuera del rango de bloque,
fecha fuera de vigencia, día distinto o duplicidad de `serieId` para la fecha es
`ADAPTER_INPUT_INVALID`/invariante rota y aborta la unidad afectada.

Mapping nominal:

```text
reference: (SERIE_ASIGNACION, a.serie_id, fecha)
candidateType: NOMINAL_OCCURRENCE
snapshot fields: salon/instructor/activity/a.hora_inicio/a.hora_fin
recordIds: [a.id, b.id]
observableFields: series, versions, both ranges, both vigencias, both active flags,
                  technical timestamps and day convention
```

Una nominal no es por sí sola el candidate universe efectivo final.

## 10. Reader de ajustes

### 10.1 Estrategia seleccionada

`PROJECTION_FIRST` mediante SQL nativa en un `AdjustmentProjectionQueryExecutor` plain y
adapter-local que usa `EntityManager` y devuelve el record concreto `AdjustmentProjectionRow` con
todos los campos escalares de `programacion_ajuste_fecha`. No se usa ni modifica
`AjusteProgramacionFechaRepository`. La única consulta autorizada es por fecha exacta de
evaluación. No existe port R4 por rango: ningún consumer del scope candidato lo necesita y un
audit futuro debe iterar unidades fechadas mediante R6, no ampliar R4. No
se entrega `AjusteProgramacionFecha` managed al mapper del detector. Orden: `fecha,id`.

### 10.2 Campos exactos y nullability

| Campo | Origen | Tipo | Nullable | Semántica |
| --- | --- | --- | --- | --- |
| `adjustmentId` | `id` | `UUID` | NO | source identity; identidad de adición |
| `type` | `tipo` | enum/string | NO | CANCELACION/REEMPLAZO/ADICION |
| `date` | `fecha` | `LocalDate` | NO | fecha atómica |
| `assignmentSeriesId` | `asignacion_serie_id` | `UUID` | Por forma | requerido para cancelación/reemplazo; prohibido para adición |
| `resultSalonId` | `salon_resultado_id` | `UUID` | Por forma | prohibido para cancelación; requerido para reemplazo/adición |
| `resultInstructorId` | `instructor_resultado_id` | `UUID` | Por forma | igual |
| `resultActivityId` | `tipo_actividad_resultado_id` | `UUID` | Por forma | igual |
| `resultStart` | `hora_inicio_resultado` | `LocalTime` | Por forma | igual; rango positivo si presente |
| `resultEnd` | `hora_fin_resultado` | `LocalTime` | Por forma | igual; rango positivo si presente |
| `active` | `activo` | `boolean` | NO | estado observable; evaluación material usa activos |
| `createdAtTechnical` | `creado_en` | `OffsetDateTime` | NO | provenance, no vigencia |
| `updatedAtTechnical` | `actualizado_en` | `OffsetDateTime` | NO | provenance, no historia funcional |

Los null se serializan explícitamente como `ABSENT_BY_ADJUSTMENT_FORM` en maps inmutables; nunca se
omiten ambiguamente ni se insertan como null en `EvidenceProvenance.normalizedFields`.

### 10.3 Contrato por tipo

| Tipo | Source atom | Referencia nominal | Evidence efectiva esperada | Provenance/fallo |
| --- | --- | --- | --- | --- |
| CANCELACION | `NEW_CANCELACION` | `(SERIE_ASIGNACION, assignmentSeriesId, date)`, exactamente una nominal | cero occurrences con esa referencia: `EXPECTED_ABSENCE` | adjustment ID + nominal version IDs; 0 nominal=`MISSING`, >1 o efectiva presente=`DIVERGENT`/ambiguous |
| REEMPLAZO | `NEW_REEMPLAZO` | misma referencia, exactamente una nominal | exactamente una efectiva `REPLACEMENT_OCCURRENCE` con snapshot resultado y misma referencia | adjustment ID + nominal version IDs + result fields; ausencia/múltiples/mismatch=`DIVERGENT` |
| ADICION | `NEW_ADICION` | `NOT_APPLICABLE`; no serie sintética | exactamente una efectiva `(AJUSTE,adjustmentId,date)` con snapshot resultado | adjustment ID + result fields; ausencia/múltiples/mismatch=`MISSING` o `DIVERGENT` conforme al core |

No existe mapping legacy en este reader. Igualdad de forma o outcome con `EXCEPCION`/
`CANCELACION` legacy no añade evidence de identidad ni intención.

## 11. Candidate universe nuevo: nominal y efectivo

La unidad cierra dos universos distintos. Nunca se mezclan en una misma
`CandidateGenerationResult`, porque una nominal y su efectiva pueden compartir referencia y el
core prohíbe candidate identities duplicadas.

### 11.1 `NOMINAL_UNIVERSE`

Usado para:

- legacy recurrente → series/occurrences nominales por fecha;
- target evidence de CANCELACION/REEMPLAZO nueva;
- demostrar `EXPECTED_ABSENCE` sin fabricar una occurrence efectiva.

Se obtiene del projection contract de la sección 9. Incluye occurrences aun si luego serían
omitidas por operación/maestros. Esto preserva `NOMINALES → AJUSTES → OPERATIVO FINAL`.

### 11.2 `EFFECTIVE_PRESENT_UNIVERSE`

Usado para:

- Reserva → candidates efectivos presentes;
- comparación del outcome actual de legacy puntual sin inferir intención;
- outcome de REEMPLAZO/ADICION;
- current outcome de un historical target ya demostrado por otra fuente.

Estrategia concreta: `ENTITY_MAPPING_IN_TRANSACTION` sólo para esta fuente derivada, mediante un
grafo dark-launch dedicado que instancia las APIs aprobadas y llama
`ProgramacionEfectiva.resolverGlobal(fecha)` conforme a 12.4; no reutiliza el bean productivo con
diagnóstico SLF4J. El servicio conserva la
composición F2D y entrega `OcurrenciaEfectiva` records inmutables. Internamente puede leer entities
de ajustes/maestros, pero éstas no salen de su llamada ni del transaction boundary.

Campos exactos de cada resultado:

| Campo derivado | Tipo/nullability | Destino |
| --- | --- | --- |
| `fecha` | `LocalDate` NO | `reference.fecha` y observable field |
| `salonId` | `UUID` NO | `ProgrammingCandidateSnapshot.salonId` |
| `instructorId` | `UUID` NO | `instructorId` |
| `tipoActividadId` | `UUID` NO | `activityId` |
| `horaInicio`/`horaFin` | `LocalTime` NO, positivo | `start/end` |
| `origen` | enum NO | candidate type recurrent/replacement/addition |
| `referencia.tipo/id/fecha` | enum/UUID/date NO | `reference` y candidate identity |

Mapping de tipo:

```text
RECURRENTE -> RECURRENT_OCCURRENCE + SERIE_ASIGNACION
REEMPLAZO -> REPLACEMENT_OCCURRENCE + SERIE_ASIGNACION
ADICION   -> ADDITION_OCCURRENCE + AJUSTE
```

El adapter lee además nominales y ajustes projections dentro del mismo snapshot para enlazar
provenance:

- recurrente: assignment/block version IDs de la nominal;
- reemplazo: los mismos IDs más el único adjustment target activo compatible;
- adición: adjustment ID igual a `reference.id`;
- cancelación: nominal backing y ajuste existen, pero no se crea candidate efectivo.

Una referencia efectiva sin backing inequívoco, origen/tipo incompatible, effective duplicada o
resultado que contradice el snapshot del ajuste es invariante rota y aborta la evaluación; nunca se
elige una fila ni se deduplica.

`ProgramacionEfectiva` omite por operación/maestros mediante el port existente
`ProgramacionDiagnostico`. El grafo 12.4 inyecta el collector in-memory y conserva la causa exacta
ya emitida como `EffectiveOmissionEvidence`, junto con nominal/ajuste y schema provenance. Para
CANCELACION válida la ausencia se reclasifica únicamente con nominal única y ajuste válido como
`EXPECTED_ABSENCE`; para REEMPLAZO/ADICION es `DIVERGENT_INCOMPATIBLE`. Una referencia ausente sin
resultado, sin cancelación válida y sin omission evidence compatible es
`READ_SET_INVARIANT_VIOLATION`; no se inventa la causa ni se delega al executor.

## 12. Operación y maestros como supporting evidence

No constituyen target candidates ni `SourceSnapshot` del detector. Participan sólo dentro de
`ProgramacionEfectiva`/`ProgramacionValidador`, en la misma transacción:

| Evidencia | Campos físicos observados | Nullability/semántica | Lazy relevante |
| --- | --- | --- | --- |
| Salón | `salon.id`, `salon.activo` | ID NO, activo NO | `salon.tiposActividad` LAZY |
| Horario especial | `salon_horario_excepcion.id,salon_id,fecha,cerrado,hora_apertura,hora_cierre,activo` | horas null sólo en cierre | `salon` LAZY, no necesario por ID |
| Horario semanal | `horario_operacion.id,salon_id,dia_semana,hora_apertura,hora_cierre,vigente_desde,vigente_hasta` | vigencias null=open | `salon` LAZY, no necesario por ID |
| Instructor | `usuario.id,estatus` | NO | `roles`, `roles.rol`, `roles.salon`, `especialidades` LAZY |
| Rol | `usuario_rol.usuario_id,rol_id,salon_id`, `rol.nombre` | salon null=global | relations LAZY |
| Actividad | `tipo_actividad.id,activo` | NO | ninguna requerida |
| Especialidad | `instructor_actividad.usuario_id,tipo_actividad_id` | NO | collection LAZY |
| Oferta salón | `salon_tipo_actividad.salon_id,tipo_actividad_id` | NO | collection LAZY |

Todo acceso lazy termina dentro de `ProgramacionEfectiva` y de la transacción suministrada por R6
o por el test harness individual. Las
entities de soporte no se usan para construir provenance fuera de ella; el resultado inmutable y
los backing projections son la evidencia transportable.

### 12.1 Tabla normativa de ports, ownership y query

Esta tabla sustituye cualquier formulación anterior que pudiera leerse como alternativa de
repository, projection o executor. Los nombres son conceptuales pero el ownership y la tecnología
no son opcionales.

| Reader | Port operation | Input exacto | Binding contract | Output exacto | Cardinalidad | Semántica de ausencia | Ordering | Owner físico de lectura | Tecnología | Future allowed production files | ¿Modificar tracked existente? |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| R1 Reserva | `readByReservationIds` | `ReadSnapshotContext` + `Set<UUID> reservationIds` no null/no vacío, sin elementos null | `r.id IN (:reservationIds)` + `NativeQuery.setParameterList("reservationIds", sortedIds, UUID.class)`; 12.2 | lista inmutable de `ReservationSourceSnapshot` | exactamente una por ID pedido | set vacío/null/elemento null=`ADAPTER_INPUT_INVALID` antes de SQL; si falta cualquier ID=`SOURCE_RECORD_NOT_FOUND`, aborta batch sin parciales | `r.id`; output por `reservationId` | nuevo `ReservaProjectionQueryExecutor` | SQL nativa + `EntityManager`, unwrap `NativeQuery`, `ReservaProjectionRow` | nuevos tipos bajo `transicion/programacion/read/**`, `adapter/jpa/projection/**`, `adapter/jpa/mapper/**`, `adapter/jpa/**` | NO |
| R1 Reserva | `readByScope` | context + `ReservationScope(Set<UUID> salonIds, LocalDate desde, LocalDate hasta)`; set no null/no vacío, extremos no null e inclusivos, `desde <= hasta`, ventana bounded | `r.salon_id IN (:salonIds)` por `setParameterList(..., UUID.class)`; fechas escalares tipadas; 12.2 | lista inmutable de `ReservationSourceSnapshot` | 0..N | salon set vacío/null/elemento null o rango null/invertido=`ADAPTER_INPUT_INVALID` antes de SQL; cero filas es válido | `r.id`; output por `reservationId` | el mismo executor | igual | los mismos paths | NO |
| R2 legacy | `readForDate` | context + `LegacyTurnScope(Set<UUID> salonIds, LocalDate fecha)`; set no null/no vacío y fecha no null | members: `t.salon_id IN (:salonIds)`; assignments: `turno_id IN (:turnIds)`; ambas por `setParameterList(..., UUID.class)` y escalares tipados; si `turnIds` derivado queda vacío no se ejecuta query 2; 12.2 | `LegacyTurnReadSet(sources)` inmutable o fallo `LegacyAdapterInputInvalid` con lista no vacía de `LegacyAdapterRejection`; nunca ambos | 0..N según 12.3; una evaluación por source publicado | scope vacío/null=`ADAPTER_INPUT_INVALID` antes de SQL; cero turnos tras query 1 produce read set vacío; mitad incoherente/malformed aborta sin parciales | members `turnId,memberId NULLS FIRST`; assignments `turnId,instructorId,activityId`; sources `turnId/member/activity/evidenceKey` | nuevo `LegacyTurnProjectionQueryExecutor` | dos SQL nativas + `EntityManager`, unwrap `NativeQuery`, records concretos | nuevos tipos en los mismos cuatro packages; no repository bajo `calendario` | NO |
| R3 nominal | `readNominalOnDate` | context + `LocalDate fecha` no null; `dayOfWeek` se deriva determinísticamente 0=domingo | named scalar binding de `fecha`, `dayOfWeek`, `assignmentActive=true`, `blockActive=true`; 12.2 | `NominalProgrammingReadSet(candidates,backing)` inmutable | 0..N; máximo una nominal por serie/fecha | fecha null=`ADAPTER_INPUT_INVALID` antes de SQL; cero es válido para universe; cuando otro claim exige target, el classifier decide `MISSING` | ambos por `serieId,asignacionVersionId` | nuevo `NominalProjectionQueryExecutor` | SQL nativa + `EntityManager`, unwrap `NativeQuery`, `NominalProjectionRow` | nuevos ports/executor/row/mapper/adapter en packages de transición | NO; no se cambia `AsignacionRepository` |
| R4 ajustes | `readActiveAdjustmentsOnDate` | context + `LocalDate fecha` no null | named scalar binding de `fecha` y `active=true`; 12.2; no existe collection/range parameter | `AdjustmentReadSet(sources,backing)` inmutable | 0..N | fecha null=`ADAPTER_INPUT_INVALID` antes de SQL; cero es válido | ambos por `fecha,adjustmentId` | nuevo `AdjustmentProjectionQueryExecutor` | SQL nativa + `EntityManager`, unwrap `NativeQuery`, `AdjustmentProjectionRow` | nuevos ports/executor/row/mapper/adapter en packages de transición | NO; no se cambia `AjusteProgramacionFechaRepository` |
| R5 efectiva | `readEffectiveOnDate` | context + `LocalDate fecha` | fuera de R1–R4 native binding; usa APIs F2D y los ports R3/R4 ya fijados | `EffectiveProgrammingReadSet(candidates, backingByReference, omissions)` inmutable | 0..N; cada referencia presente máximo una | cero global es válido; faltas respecto de R3/R4 se clasifican conforme a secciones 10–11 y nunca como éxito implícito | orden F2D existente | nuevo adapter R5 y graph factory, usando el grafo exacto de 12.4 | APIs F2D existentes + repositories existentes sólo dentro del grafo; R3/R4 para backing | nuevos adapter/collector/factory/mapper/read-set bajo packages de transición | NO; ningún tipo F2D se modifica |

Los cuatro executors R1–R4 son clases plain con constructor injection de `EntityManager`; no son
Spring Data repositories y no usan `@Repository`, `@Component` ni auto-scan. R1–R5 son clases
plain, también por constructor injection. En tests, `@TestConfiguration` los registra
explícitamente; en R6 los registra únicamente la configuración shadow doblemente condicionada.
La existencia de los repositories F2D actuales no hace reachable al adapter R5: sólo la factory y
el adapter nuevos quedan detrás de R6. No se amplía `@EnableJpaRepositories` ni el component scan.

Los envelopes conceptuales son exactos:

```text
LegacyTurnReadSet:
  sources = List<GenericSourceSnapshot> representables y ordenados
  no contiene DetectorResult ni artifacts operacionales

NominalProgrammingReadSet:
  candidates = List<ProgrammingCandidateSnapshot(NOMINAL_OCCURRENCE)>
  backing = List<NominalBackingSnapshot> con todos los campos de 9.2

AdjustmentReadSet:
  sources = List<GenericSourceSnapshot(NEW_CANCELACION|NEW_REEMPLAZO|NEW_ADICION)>
  backing = List<AdjustmentBackingSnapshot> con todos los campos de 10.2

EffectiveProgrammingReadSet:
  candidates = List<ProgrammingCandidateSnapshot(
    RECURRENT_OCCURRENCE|REPLACEMENT_OCCURRENCE|ADDITION_OCCURRENCE)>
  backingByReference = Map<ReferenciaOcurrencia, EffectiveBackingSnapshot>
  omissions = List<EffectiveOmissionEvidence>
```

Todos hacen defensive copies y no contienen projection proxies, entities ni repositories.

### 12.2 Contrato normativo de binding nativo R1–R4

#### 12.2.1 Mecanismo único

Los cuatro executors crean SQL nativa con `EntityManager.createNativeQuery`, hacen unwrap a
`org.hibernate.query.NativeQuery` y usan exclusivamente parámetros nombrados. El contrato fijado
es:

```text
scalar:
  NativeQuery.setParameter(name, nonNullValue, ExactJavaClass)

multi-valued UUID:
  SQL: physical_uuid_column IN (:parameterName)
  Java: immutable sorted List<UUID> obtenida de un Set<UUID> validado
  binding: NativeQuery.setParameterList(parameterName, sortedIds, UUID.class)
```

Hibernate genera sus bind markers y enlaza cada UUID como valor JDBC; el executor no construye ni
concatena markers o literales. Quedan prohibidos `ANY(CAST(:ids AS uuid[]))`, `java.sql.Array`,
arrays PostgreSQL, temporary tables, interpolación de UUIDs, concatenación de SQL y expansión
manual de placeholders. La elección usa la extensión físicamente disponible en Hibernate ORM
`7.4.1.Final`, administrado por Spring Boot `4.1.0`, y PostgreSQL sigue recibiendo UUIDs
parameterized nativos. No se afirma que Jakarta Persistence estandarice collection binding.

Antes de crear la query, el port hace defensive copy, rechaza set/elemento null y ordena los UUID
por su orden natural. El orden de binding sólo hace reproducible SQL capture; el `ORDER BY`
normativo determina el output. Ninguna colección vacía llega a `setParameterList`, por lo que no
se depende de la SQL que Hibernate pudiera producir para `IN ()`.

Los escalares se enlazan con la clase exacta indicada; no se usa `TemporalType` para `java.time`,
`setObject` sin tipo, conversión a texto ni cast nullable. Todo enum persistido como texto se
enlaza mediante su nombre canónico `String`, no como ordinal ni enum Java. Los flags y tipos que
son invariantes de la query son constantes del executor, no nuevos filtros del caller, pero se
siguen enlazando y nunca se interpolan.

| Tipo lógico/Java | Tipo PostgreSQL | Cardinalidad | Binding conceptual exacto | Null |
| --- | --- | --- | --- | --- |
| `UUID` | `uuid` | scalar | `setParameter(name, value, UUID.class)` | prohibido antes de SQL |
| `Set<UUID>` → `List<UUID>` canónica | `uuid` por elemento | multi | `IN (:name)` + `setParameterList(name, values, UUID.class)` | set/elemento null prohibido; vacío según port |
| `LocalDate` | `date` | scalar | `setParameter(name, value, LocalDate.class)` | prohibido antes de SQL |
| `LocalTime` | `time without time zone` | scalar | `setParameter(name, value, LocalTime.class)` | prohibido si un futuro query contract ya aprobado lo declara filtro; R1–R4 actuales no reciben horas como filtro |
| `Short`/`short` | `smallint` | scalar | boxing a `Short` y `setParameter(name, value, Short.class)` | prohibido; R2/R3 lo derivan de fecha |
| enum/status lógico | `varchar` | scalar | nombre canónico y `setParameter(name, value, String.class)` | prohibido; valores R2 son constantes del executor |
| `Boolean`/`boolean` | `boolean` | scalar | boxing a `Boolean` y `setParameter(name, value, Boolean.class)` | prohibido; `true` de R2–R4 es constante del executor |

No existe filtro nullable en R1–R4. Todo input null o incoherente produce `ADAPTER_INPUT_INVALID` antes
de crear/ejecutar SQL; queda prohibido `(:x IS NULL OR column = :x)`. Los null permitidos en
projections (horas raw legacy, vigencias abiertas y campos de ajuste por forma) son datos leídos,
no parámetros de filtro.

#### 12.2.2 Tabla exacta de parámetros por port/query

| Port/query | Parameter | Java/lógico | PostgreSQL | Scalar/multi | Binding | Empty semantics | Null semantics |
| --- | --- | --- | --- | --- | --- | --- | --- |
| R1 `readByReservationIds` | `reservationIds` | `Set<UUID>` → lista canónica | `uuid` | multi | `r.id IN (:reservationIds)` + `setParameterList(..., UUID.class)` | caller vacío=`ADAPTER_INPUT_INVALID`, cero SQL | set/elemento null=`ADAPTER_INPUT_INVALID`, cero SQL |
| R1 `readByScope` | `salonIds` | `Set<UUID>` → lista canónica | `uuid` | multi | `r.salon_id IN (:salonIds)` + `setParameterList(..., UUID.class)` | caller vacío=`ADAPTER_INPUT_INVALID`, cero SQL | set/elemento null=`ADAPTER_INPUT_INVALID`, cero SQL |
| R1 `readByScope` | `desde` | `LocalDate` | `date` | scalar | `r.fecha >= :desde`; typed scalar | no aplica | null=`ADAPTER_INPUT_INVALID`, cero SQL |
| R1 `readByScope` | `hasta` | `LocalDate` | `date` | scalar | `r.fecha <= :hasta`; typed scalar | no aplica; `desde > hasta` inválido | null=`ADAPTER_INPUT_INVALID`, cero SQL |
| R2 members | `salonIds` | `Set<UUID>` → lista canónica | `uuid` | multi | `t.salon_id IN (:salonIds)` + `setParameterList(..., UUID.class)` | caller vacío=`ADAPTER_INPUT_INVALID`, ninguna de las dos SQL | set/elemento null=`ADAPTER_INPUT_INVALID`, ninguna SQL |
| R2 members | `fecha` | `LocalDate` | `date` | scalar | comparación puntual; typed scalar | no aplica | null=`ADAPTER_INPUT_INVALID`, ninguna SQL |
| R2 members | `dayOfWeek` | `Short` derivado, 0=domingo | `smallint` | scalar | rama recurrente; typed scalar | no aplica | imposible tras fecha válida; si falla derivación=`ADAPTER_INPUT_INVALID` |
| R2 members | `active` | constante `Boolean.TRUE` | `boolean` | scalar | `t.activo = :active`; typed scalar | no aplica | prohibido |
| R2 members | `recurrentType`, `exceptionType`, `cancellationType` | constantes `String` con nombres del enum | `varchar` | tres scalars | comparaciones `t.tipo = :...`; typed scalar individual, no list | no aplica | prohibido |
| R2 assignments | `turnIds` | set derivado de query members → lista canónica | `uuid` | multi | `a.turno_id IN (:turnIds)` + `setParameterList(..., UUID.class)` | vacío derivado=cero turnos: omitir SQL 2 y devolver read set vacío | null/elemento null=invariante rota, `LegacyAdapterInputInvalid`, sin read set |
| R3 `readNominalOnDate` | `fecha` | `LocalDate` | `date` | scalar | cuatro comparaciones de vigencia; mismo named typed scalar | no aplica | null=`ADAPTER_INPUT_INVALID`, cero SQL |
| R3 `readNominalOnDate` | `dayOfWeek` | `Short` derivado, 0=domingo | `smallint` | scalar | `b.dia_semana = :dayOfWeek`; typed scalar | no aplica | imposible tras fecha válida; fallo=`ADAPTER_INPUT_INVALID` |
| R3 `readNominalOnDate` | `assignmentActive`, `blockActive` | constantes `Boolean.TRUE` | `boolean` | dos scalars | predicados `= :...`; typed scalar | no aplica | prohibido |
| R4 `readActiveAdjustmentsOnDate` | `fecha` | `LocalDate` | `date` | scalar | `a.fecha = :fecha`; typed scalar | no aplica | null=`ADAPTER_INPUT_INVALID`, cero SQL |
| R4 `readActiveAdjustmentsOnDate` | `active` | constante `Boolean.TRUE` | `boolean` | scalar | `a.activo = :active`; typed scalar | no aplica | prohibido |

`ReadSnapshotContext` no es parámetro SQL: fija provenance y se valida completo antes de toda
lectura. R4 queda cerrado a una sola fecha; no existen `desde`, `hasta`, collections ni segunda
operación R4. Agregar un filtro nuevo requiere otra decisión de diseño, no una elección local del
executor. R1 no filtra `Reserva.estado`; lo proyecta. R2 no recibe member/user IDs como filtro;
los proyecta desde membership/assignment. R3 no recibe salones, series ni IDs como filtro. Tipos
legacy y flags activos son constantes enlazadas del query contract, no inputs públicos.

### 12.3 Regla exhaustiva de átomos R2

El átomo normal de R2 es una fila física de
`turno_instructor_asignacion`, identificada por su PK compuesta
`(turno_id, usuario_id, tipo_actividad_id)`. La membresía
`turno_instructor_usuario` es evidence de pertenencia, no otro átomo cuando existe al menos una
asignación para ese miembro. Para no perder formas incompletas se permiten átomos de gap sólo de
evidence; nunca se confunden con una PK o identidad de dominio.

Vocabulario cerrado de markers:

```text
ABSENT_MEMBER              el turno no tiene filas de membresía
ABSENT_ASSIGNMENT          un miembro no tiene fila de asignación
ABSENT_ACTIVITY            consecuencia observable de ABSENT_ASSIGNMENT; no hay activityId
NON_MEMBER_ASSIGNMENT      la fila de asignación referencia instructor no miembro
FULL_TURN_RANGE_FALLBACK   ambos extremos raw son null; rango efectivo = rango del turno
EXPLICIT_ASSIGNMENT_RANGE  ambos extremos raw existen y se usan literalmente
INCOMPLETE_RANGE           exactamente un extremo raw es null
RANGE_OUTSIDE_TURN         rango explícito no positivo o no contenido
DUPLICATE_PHYSICAL_ROW     la misma PK compuesta apareció dos veces en projection output
DUPLICATE_LOGICAL_ATOM     dos filas/gaps distintos producirían la misma sourceIdentity
INVALID_REQUIRED_FIELD     null/imposible en una columna físicamente NOT NULL
ORPHAN_TURN_HEADER         assignment sin header de turno correlacionable
INVALID_SOURCE_TYPE        valor de tipo sin SourceAtomType legacy legal
```

Una gap identity se calcula de manera determinista como
`urn:f2e:legacy-gap:v1:turn=<turnId>:member=<memberId|ABSENT>:marker=<marker>`.
Su único propósito es identificar evidence reproducible; no es turno, assignment, serie ni target.
La identity de una assignment row es exactamente
`urn:f2e:legacy-assignment:v1:turn=<turnId>:member=<instructorId>:activity=<activityId>`, con UUIDs
lower-case canónicos.
`recordIds` conserva sólo IDs físicos existentes: `[turnId]` para `ABSENT_MEMBER`,
`[turnId,memberId]` para `ABSENT_ASSIGNMENT`, y
`[turnId,instructorId,activityId]` para una assignment row. Nunca se fabrica un assignment ID.

Sea `M` el set de miembros, `A(m)` las assignment rows cuyo instructor es `m` y `O` las assignment
rows cuyo instructor no pertenece a `M`. La cantidad publicada para un turno válido es:

```text
si M vacío y O vacío: 1 átomo gap ABSENT_MEMBER
en otro caso: |O| + suma para cada m de max(1, |A(m)|)
```

La validación tiene precedencia única y ocurre en este orden:

1. correlacionar las dos projections y validar payload requerido del adapter;
2. si no puede formarse un source R2 confiable, producir `LegacyAdapterRejection` y abortar el
   scope antes del core;
3. si puede formarse `GenericSourceSnapshot`, preservar su `SourceAtomType` físico;
4. para puntual representable, la intención desconocida D10 domina el status semántico y se usa
   siempre el scenario puntual específico, aun cuando existan markers estructurales;
5. sólo `LEGACY_RECURRENTE` representable con anomalía estructural usa
   `INCOMPATIBLE_EVIDENCE`.

`GenericSourceSnapshot` admite maps inmutables de strings; por ello `ABSENT_MEMBER`,
`ABSENT_ASSIGNMENT`, `ABSENT_ACTIVITY`, `NON_MEMBER_ASSIGNMENT`, `INCOMPLETE_RANGE` y
`RANGE_OUTSIDE_TURN` se transportan legalmente en `observableFields` y duplicados en
`EvidenceProvenance.normalizedFields`. No se añaden enums al core ni se fuerzan como
`DetectorCandidate.rejectionReasons`. Un átomo con esos markers genera cero candidates cuando la
estructura no permite candidate evidence completa. El `DetectorResult` conserva el source y su
provenance, por lo que la anomalía no se pierde aunque el status puntual sea `UNSUPPORTED`.

Esta es la matriz normativa completa. `K` es la cantidad de átomos calculada por la fórmula
anterior para el turno; cada fila representable produce exactamente una evaluación por snapshot.

| Shape física | SourceAtomType | ¿SourceSnapshot válido? | Scenario pasado al core | Resultado semántico esperado | Structural evidence markers | Abort operacional | Snapshots emitidos | Evaluaciones / DetectorResults |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| assignment completo, rango explícito o fallback completo | `LEGACY_RECURRENTE` | SÍ | `STANDARD_EVALUATION` o `REQUIRED_TARGET` según claim; `LEGACY_HISTORY_REQUIRED` sólo si el claim pide historia | evaluación normal 0..N; o `UNSUPPORTED/LEGACY_FUNCTIONAL_VALIDITY_NOT_PERSISTED` para historia | `EXPLICIT_ASSIGNMENT_RANGE` o `FULL_TURN_RANGE_FALLBACK` | NO | 1 por PK | 1 / 1 |
| gap por cero miembros, miembro sin assignment, orphan, incomplete/outside range | `LEGACY_RECURRENTE` | SÍ | `INCOMPATIBLE_EVIDENCE` | `DIVERGENT_INCOMPATIBLE`, blocking | combinación exacta de `ABSENT_*`, `NON_MEMBER_ASSIGNMENT`, `INCOMPLETE_RANGE`, `RANGE_OUTSIDE_TURN` | NO | 1 por gap/PK | 1 / 1 |
| assignment completo, rango explícito o fallback completo | `LEGACY_EXCEPCION` | SÍ | `LEGACY_EXCEPTION_UNKNOWN_INTENT` | `UNSUPPORTED + UNKNOWN_INTENT`, blocking; 0..N candidates observables no alteran el scenario | range marker correspondiente | NO | 1 por PK | 1 / 1 |
| gap por cero miembros, miembro sin assignment, orphan, incomplete/outside range | `LEGACY_EXCEPCION` | SÍ | `LEGACY_EXCEPTION_UNKNOWN_INTENT` | `UNSUPPORTED + UNKNOWN_INTENT`, blocking; nunca `DIVERGENT_INCOMPATIBLE` por scenario ilegal | markers estructurales exactos en source/provenance; cero candidates si faltan dimensiones seguras | NO | 1 por gap/PK | 1 / 1 |
| assignment completo, rango explícito o fallback completo | `LEGACY_CANCELACION` | SÍ | `LEGACY_CANCELLATION_UNKNOWN_INTENT` | `UNSUPPORTED + UNKNOWN_INTENT`, blocking; nunca `EXPECTED_ABSENCE` | range marker correspondiente | NO | 1 por PK | 1 / 1 |
| gap por cero miembros, miembro sin assignment, orphan, incomplete/outside range | `LEGACY_CANCELACION` | SÍ | `LEGACY_CANCELLATION_UNKNOWN_INTENT` | `UNSUPPORTED + UNKNOWN_INTENT`, blocking; nunca `DIVERGENT_INCOMPATIBLE` ni `EXPECTED_ABSENCE` | markers estructurales exactos en source/provenance; cero candidates si faltan dimensiones seguras | NO | 1 por gap/PK | 1 / 1 |
| query members encuentra cero turnos | no existe atom | NO; no hay source que construir | ninguno | ninguno; ausencia válida del port | ninguno | NO; se omite query assignments | 0 | 0 / 0 |
| null/imposible en header requerido: turn ID/type/active/salon, day/date exigido por tipo, rango de turno positivo o timestamps | tipo conocido si alcanzó a leerse; no se publica | NO bajo el contrato R2, aunque un map genérico pudiera construirse mecánicamente | ninguno | ninguno | `INVALID_REQUIRED_FIELD` en `LegacyAdapterRejection` | SÍ, `ADAPTER_INPUT_INVALID`; abort total del scope | 0 | 0 / 0 |
| assignment con turn ID no presente/correlacionable en query members | no confiable | NO | ninguno | ninguno | IDs seguros + `ORPHAN_TURN_HEADER` en `LegacyAdapterRejection` | SÍ, `READ_SET_INVARIANT_VIOLATION`; abort total | 0 | 0 / 0 |
| duplicate projection row, misma PK repetida o duplicate logical sourceIdentity tras ensamblar | tipo conocido si header es válido; no se publica | NO como read set inequívoco | ninguno | ninguno | PK/identity segura + `DUPLICATE_PHYSICAL_ROW` o `DUPLICATE_LOGICAL_ATOM` en `LegacyAdapterRejection` | SÍ, `READ_SET_INVARIANT_VIOLATION`; nunca deduplicar/elegir | 0 | 0 / 0 |
| source type físico desconocido o incompatible con `LEGACY` | no existe enum legal | NO | ninguno | ninguno | valor raw seguro + `INVALID_SOURCE_TYPE` en `LegacyAdapterRejection` | SÍ, `ADAPTER_INPUT_INVALID` | 0 | 0 / 0 |

La matriz 0/1/N que alimenta `K` queda exacta:

| Membership/assignment shape | K snapshots si el payload requerido es válido |
| --- | --- |
| 0 miembros, 0 assignments | 1 gap `ABSENT_MEMBER` |
| 0 miembros, N assignments | N assignment atoms, cada uno `ABSENT_MEMBER + NON_MEMBER_ASSIGNMENT` |
| 1 miembro, 0 assignments | 1 gap `ABSENT_ASSIGNMENT + ABSENT_ACTIVITY` |
| 1 miembro, 1 assignment | 1 assignment atom |
| 1 miembro, N assignments | N assignment atoms |
| N miembros, 0 assignments | N gaps, uno por miembro |
| N miembros con `a_i` assignments y `O` orphans | `sum(max(1,a_i)) + |O|` |
| misma actividad en miembros distintos | N PKs distintas; N atoms, no duplicate |

`LegacyAdapterRejection` es un artifact inmutable del adapter/error channel, no
`GenericSourceSnapshot`, `DetectorCandidate` ni `DetectorResult`. Contiene error code, query ID,
scope seguro, marker y sólo IDs/raw values no sensibles disponibles. Ante uno solo, R2 no devuelve
`LegacyTurnReadSet`; R6 registra el artifact como operational error, descarta todo el read set y no
invoca el classifier. En corrida exitosa no hay rejections y
`detectorEvaluations = semanticResults = sources.size()`.

Cada `LegacyAdapterRejection` representa exactamente un attempted logical atom: para un turno con
header inválido se calculan sus `K` unidades por membership/assignment y se emite un artifact por
unidad; una fila sin key suficiente cuenta como una unidad por ordinal estable de projection; dos
filas con la misma PK/sourceIdentity producen un solo rejected atom con
`observedPhysicalRowCount > 1`. La multiplicidad física completa queda en
`legacy_projection_rows_observed`. Así los conteos de átomos no aumentan artificialmente por un
duplicate y siguen siendo exactos aunque no exista `sourceIdentity` publicable.

Métricas futuras separadas, sin requerir implementación ahora:

```text
legacy_projection_rows_observed
source_atoms_read = source_atoms_valid + source_atoms_operationally_rejected
source_atoms_valid
source_atoms_operationally_rejected
source_snapshots_published
detector_evaluations
semantic_results
```

En éxito: rejected=0 y `published=evaluations=results=valid`. En abort R2: los conteos
observacionales `read/valid/rejected` pueden acompañar el operational error, pero
`published=evaluations=results=0`; ningún átomo pre-core rechazado produce `DetectorResult` y
ningún snapshot parcial se publica.

La query de members filtra turnos activos del scope con esta regla exacta: RECURRENTE cuyo
`dia_semana = dayOfWeek(fecha)` o EXCEPCION/CANCELACION cuya `fecha = fecha`; usa `LEFT JOIN` para
conservar cero miembros. La query de assignments usa los `turnId` resultantes y no filtra por
membresía. Correlation key obligatoria: `turnId`. Orden SQL obligatorio:
`turnId, memberId NULLS FIRST` y `turnId, instructorId, activityId`.

### 12.4 Grafo R5 y boundary de diagnóstico

R5 elige `DARK_LAUNCH_SPECIFIC_GRAPH`; no reutiliza el bean productivo existente de
`ProgramacionEfectiva`, porque éste recibe el bean `ProgramacionDiagnosticoSlf4j`. Una factory plain
`EffectiveProgrammingReadGraphFactory`, creada sólo por test config o R6 shadow config, construye
por evaluación este grafo con APIs públicas ya existentes:

```text
ProgramacionNominal(existing AsignacionRepository)
AplicadorAjustesProgramacion()
InMemoryProgramacionDiagnosticoCollector()
ProgramacionValidador(
  existing HorarioEfectivoSalon,
  existing SalonRepository,
  existing UsuarioRepository,
  existing TipoActividadRepository,
  collector)
ProgramacionEfectiva(
  dedicated ProgramacionNominal,
  existing AjusteProgramacionFechaRepository,
  dedicated AplicadorAjustesProgramacion,
  dedicated ProgramacionValidador)
```

`HorarioEfectivoSalon` sigue siendo el owner operacional aprobado. El bean existente se reutiliza
y, a su vez, conserva `SalonHorarioExcepcionRepository` y `HorarioOperacionResolver` como owners de
excepción puntual y horario semanal. Quedan fuera: `ProgramacionDiagnosticoSlf4j`, el bean
productivo de `ProgramacionValidador`, el bean productivo de `ProgramacionEfectiva`,
`AjusteProgramacionFechaPersistence`, `AjusteProgramacionFechaService`,
`BloqueProgramacionService`, locks, writers, `ReservaService`, `TurnoInstructorService`, controllers
y cualquier callback externo.

El adapter llama exactamente una vez `resolverGlobal(fecha)` en ese grafo. Dentro de la misma TX
lee además R3 y R4 para backing/provenance y compara multiset/ref/cardinalidad con el resultado F2D.
El collector implementa la interfaz existente `ProgramacionDiagnostico`, sólo agrega records
`Omision` a una lista privada, no loguea, no publica eventos y entrega al adapter una copia
inmutable. Su contenido se convierte en `EffectiveOmissionEvidence`; junto con candidates y
backing forma `EffectiveProgrammingReadSet`. Ninguna callback sale durante la lectura.

El grafo existente carga entities managed de ajuste, salón, horario, usuario/roles y actividad.
R5 sólo invoca getters a través de F2D, no retiene esas instancias y las descarta antes de salir.
Por ello R1–R4 eliminan dirty checking estructuralmente mediante projections; R5 lo controla con
rol SELECT-only, SQL policy, statistics de corroboración y checksum. No se declara que
`readOnly=true` vuelva inmutables esas entities.

```text
inside DB transaction: reads + F2D pure/in-memory computation + immutable omission evidence
after DB transaction: detector + report/log/file/network output, sólo bajo futura autorización
```

Este grafo no requiere modificar `ProgramacionEfectiva`, `ProgramacionValidador`, la interfaz
`ProgramacionDiagnostico` ni otro artefacto F2D; no existe `AUTHORITY_CONFLICT`.

## 13. Provenance, identities y fingerprints

Los readers reciben un `ReadSnapshotContext`; nunca leen clock/global state ni generan UUID/random.
El contexto contiene exactamente:

```text
runIdentity                 aportado por el caller
attemptIdentity             aportado por el caller; cambia sólo al reintentar el run completo
sourceName                  para R1, exclusivamente del DescriptorRecursoLector de 37.4
schemaFingerprint           para R1, exclusivamente del DescriptorRecursoLector de 37.4
projectionCatalogVersion    versión única de las queries/mappers
ruleCatalogVersion          versión de reglas detector/F2D
businessZone                ZoneId explícita
scopeCanonical              para R1, derivado internamente del scope tipado según 37.4
snapshotClaim               R1=SINGLE_READER_TEST; MULTI_READER_MVCC no está soportado por R1
snapshotEvidenceId          creado por el owner transaccional, no por el reader
```

Las fórmulas normativas usan UTF-8 y componentes length-prefixed, no concatenación ambigua:

```text
executionProvenanceId = SHA-256("F2E-EXECUTION-V1", runIdentity, attemptIdentity,
  sourceName, schemaFingerprint, projectionCatalogVersion, ruleCatalogVersion,
  businessZone, scopeCanonical)

logicalSnapshotId = SHA-256("F2E-LOGICAL-SNAPSHOT-V1", executionProvenanceId,
  snapshotClaim, snapshotEvidenceId)

sourceFingerprint = SHA-256("F2E-SOURCE-V1", schemaFingerprint,
  projectionContractId, projectionContractVersion, sourceSystem,
  sourceAtomType, sourceIdentity, canonicalNormalizedFields)

snapshotIdentity = SHA-256("F2E-ATOM-SNAPSHOT-V1", logicalSnapshotId,
  sourceAtomType, sourceIdentity, sourceFingerprint)

logicalReadSetFingerprint = SHA-256("F2E-READSET-V1",
  sort(sourceSystem, sourceAtomType, sourceIdentity, sourceFingerprint))
```

La formulación histórica siguiente describe únicamente una necesidad candidata de una futura R6;
no define un claim aceptable por R1 ni constituye autoridad suficiente para implementar R6. Una
futura unidad R6 deberá recibir autoridad separada para su contexto, provenance, observación de
statements y auditoría. Sólo entonces podrá definir un `MULTI_READER_MVCC`, verificar
`repeatable read` y `read only`, capturar la representación textual exacta de
`pg_current_snapshot()` y decidir una fórmula competente, como la candidata histórica:

```text
snapshotEvidenceId = SHA-256("F2E-PG-MVCC-V1", datasourceIdentity,
  "repeatable read", "read only", pgSnapshotInitial)
```

Al final R6 vuelve a leer `pg_current_snapshot()` y exige igualdad textual con el valor inicial.
Ese valor es identidad MVCC de la transacción, no identidad de negocio ni fingerprint del read set.
El `logicalReadSetFingerprint` se calcula después de mapear todo y describe contenido lógico; no
se usa para fingir simultaneidad. `executionProvenanceId` identifica run/attempt; tampoco demuestra
snapshot DB.

En tests individuales, el test transaction harness crea el contexto con
`snapshotClaim=SINGLE_READER_TEST` y:

```text
snapshotEvidenceId = SHA-256("F2E-TEST-TX-V1", fixtureIdentity, testInvocationIdentity,
  declaredIsolation, "read only")
```

Para R1, `fixtureIdentity` proviene exclusivamente del `DescriptorRecursoLector` definido en 37.4;
no es un literal elegible por el caller. `testInvocationIdentity` es reservado por el harness bajo
el lifecycle cerrado de 37.7.1. No se inventa `pgSnapshotFingerprint`, no se afirma snapshot
multi-reader y el mismo input produce el mismo ID. R1 standalone no necesita ni recibe fingerprint
PostgreSQL. R2–R5 pueden requerir otra autoridad de RR en sus unidades futuras, sin convertirse en
owners productivos del contrato R1.

`EvidenceProvenance` conserva `sourceName`, `schemaFingerprint`, record IDs físicos ordenados,
`ruleId/ruleVersion`, business context explícito y fields normalizados completos. Los markers
`ABSENT_*` son strings explícitos; timestamps técnicos conservan su nombre. Ninguno de los tres
identificadores anteriores reconstruye historia funcional.

Los timestamps técnicos se preservan sólo con nombres `createdAtTechnical` y
`updatedAtTechnical`. Nunca se renombran a vigencias ni se usan para elegir candidates.

## 14. Managed entities y lazy-loading boundary

### 14.1 Lifetime cerrado

1. R6 abre la única transacción física de una composición; el test harness abre la transacción
   física de una prueba individual sin atribuirse el claim production multi-reader.
2. Reserva, legacy, nominal y ajustes se leen projection-first; no crean managed entities para el
   adapter.
3. `ProgramacionEfectiva` puede crear entities managed internamente mientras resuelve ajustes,
   operación y maestros.
4. Dentro de la transacción suministrada se completa la lectura lazy necesaria, se construyen records de
   dominio, core snapshots, maps/listas inmutables, hashes y provenance.
5. Se valida forma, cardinalidad, F2D compatibility y fingerprint final.
6. Antes del commit/close se descartan todas las references a entities/proxies/projections.
7. Sólo snapshots/core results y run metadata inmutables salen de la transacción.

### 14.2 Objetos que no pueden escapar

```text
Reserva
TurnoInstructor
TurnoInstructorAsignacion
Asignacion / BloqueProgramacion
AjusteProgramacionFecha
Salon / HorarioOperacion / SalonHorarioExcepcion
Usuario / UsuarioRol / Rol / TipoActividad
EntityManager / Session
Spring Data projection proxies
Hibernate proxies / PersistentCollection
Repository / Page / Stream backed by persistence context
```

No se usa Open Session in View como safety net. La configuración no fija
`spring.jpa.open-in-view`; el diseño no depende de su default. Toda lazy access fuera de la
transacción debe fallar en tests y constituye bug de boundary.

## 15. Definición de `SAME_LOGICAL_SNAPSHOT`

Una evaluación cumple `SAME_LOGICAL_SNAPSHOT` sólo si:

1. todas sus lecturas materiales usan el mismo datasource PostgreSQL y la misma transacción física;
2. la transacción es `READ ONLY` y `REPEATABLE READ` desde antes del primer statement de datos;
3. todos los readers usan exactamente `Propagation.MANDATORY` y no abren/suspenden transacciones;
4. no existe I/O remoto, callback, reporte o espera humana dentro de la transacción;
5. el scope, business date/zone, rule versions y schema/Flyway fingerprint son únicos para la
   evaluación;
6. `pg_current_snapshot()` inicial y final son iguales y se registran en el run metadata;
7. todos los outputs llevan el mismo `logicalSnapshotId`;
8. toda shape/cardinalidad/provenance se valida antes de publicar el result;
9. si cualquiera de estas pruebas falla, se descarta la evaluación completa.

No significa que dos runs separados vean los mismos datos. Tampoco significa serialización con
writers: es un snapshot MVCC reproducible de un instante lógico. Outputs con distintos
`logicalSnapshotId` no pueden compararse como si fueran simultáneos.

## 16. Matriz de consistencia por evaluación aprobada

| Evaluation | Sources necesarias | Same snapshot | Drift tolerable | Fail-closed trigger | Boundary |
| --- | --- | --- | --- | --- | --- |
| Reserva → effective candidates | Reserva projection + effective result + nominal/adjustment backing | Obligatorio | Ninguno dentro del result | target backing no inequívoco, metadata cambia, snapshot mismatch | una TX RR por fecha/scope |
| Legacy recurrent atom → nominal candidates | legacy member/assignment projections + nominal projection | Obligatorio | Ninguno | una de las dos mitades legacy cambia o nominal cardinality/invariant falla | una TX RR |
| Legacy puntual → new candidates | legacy projections + nominal y/o effective universe según evidence | Obligatorio; siempre conserva UNKNOWN_INTENT | Ninguno | incoherencia material; no convierte a semantic success | una TX RR |
| New CANCELACION validation | adjustment + nominal target + effective universe | Obligatorio | Ninguno | nominal !=1, effective !=0, backing incompatible o snapshot failure | una TX RR |
| New REEMPLAZO validation | adjustment + nominal target + effective occurrence | Obligatorio | Ninguno | nominal/effective !=1 o snapshot/result mismatch | una TX RR |
| New ADICION validation | adjustment + effective occurrence | Obligatorio | Ninguno | effective !=1 o snapshot/result mismatch | una TX RR |
| Historical target current outcome | Reserva + source externa que ya demuestre target + effective universe | Obligatorio si algún día se autoriza | Ninguno | source externa ausente/no autorizada o outcome incoherente | futura TX RR; no ejecutable con schema actual |

`Reserva → legacy turn` puede conservarse como contexto explicativo de reservabilidad legacy, pero
no es una evaluación de target aprobada: un `TurnoInstructor` no es `ReferenciaOcurrencia` F2D.
No se convierte a `ProgrammingCandidateSnapshot` ni se usa como selected target.

Una query única aislada es statement-consistent bajo `READ COMMITTED` y no necesita RR para sí
sola. Sin embargo, ninguna evaluación compuesta de la tabla se declara completa con una sola de
sus queries; el coordinator aplica uniformemente RR al read set completo.

## 17. PostgreSQL e isolation: evidencia y decisión

Evidencia del proyecto:

```text
Spring @Transactional default isolation: ISOLATION_DEFAULT
Project explicit isolation configuration: NONE FOUND
PostgreSQL default_transaction_isolation: read committed (default estable)
Current effective multi-reader semantics: READ COMMITTED unless environment overrides server-side
```

En PostgreSQL `READ COMMITTED`, cada statement obtiene un snapshot al comenzar. Dos statements de
la misma transacción pueden observar commits concurrentes distintos. Por tanto, hoy
`ProgramacionEfectiva` puede teóricamente leer nominales en S1, ajustes en S2 y maestros/horario en
S3 bajo snapshots distintos. Que todos estén dentro de `@Transactional(readOnly=true)` no elimina
ese riesgo.

Decisión futura:

```text
Coordinator transaction:
@Transactional(
  propagation = REQUIRES_NEW,
  isolation = REPEATABLE_READ,
  readOnly = true,
  timeout = bounded)

Individual readers:
Propagation.MANDATORY
No REQUIRES_NEW
No NOT_SUPPORTED
No independent retry
```

Justificación:

- `REPEATABLE READ` fija el snapshot en el primer statement no-control y todos los SELECT
  posteriores ven la misma versión comprometida;
- no se requiere `SERIALIZABLE`: la unidad sólo observa y no toma decisiones productivas ni
  escribe invariantes; evitar su overhead/retry de serialization anomalies es proporcional al
  dark launch;
- no se usa una mega-query: mezclar Reserva, legacy, nominal, ajustes, operación y maestros haría
  frágil la cardinalidad y duplicaría la composición F2D;
- no se usan locks explícitos: RR read-only no bloquea updates normales; sí retiene versiones MVCC,
  por lo que la transacción debe ser date-scoped, bounded y sin I/O;
- fingerprints sin RR no bastan, porque podrían no detectar todo interleaving entre tablas;
- RR más metadata inicial/final y validaciones de read set ofrece corrección con complejidad
  acotada.

La configuración real se valida al inicio mediante `current_setting('transaction_isolation')`,
`current_setting('transaction_read_only')` y `pg_current_snapshot()`. Si el servidor/pool no honra
RR/read-only, no hay fallback a `READ COMMITTED`.

## 18. Transaction ownership y coordinator

Owner único del snapshot multi-reader de production: futuro `DetectorReadCoordinator` en
composition package. Responsabilidades:

1. validar request/scope/reglas sin DB;
2. abrir `REQUIRES_NEW + REPEATABLE_READ + readOnly`;
3. verificar isolation/read-only y capturar snapshot metadata;
4. invocar los readers requeridos para una sola fecha o bounded unit;
5. construir y validar todos los snapshots dentro de la TX;
6. capturar fingerprint final, comprobar igualdad y cerrar la TX;
7. sólo después entregar el read set inmutable al detector puro;
8. producir fuera de la TX el reporte no productivo autorizado.

El detector puede ejecutarse dentro o inmediatamente después de la TX porque sus inputs ya son
inmutables; por duración mínima, la opción elegida es ejecutarlo después del cierre. No accede a
lazy state.

Los adapters individuales no abren transacción. `MANDATORY` hace visible un wiring incorrecto. El
coordinator usa `REQUIRES_NEW` para no heredar accidentalmente una transacción productiva
`READ_COMMITTED`/read-write. No puede invocarse por self-invocation: debe cruzar un proxy Spring
activado sólo en la composition slice.

### 18.1 Dos contratos transaccionales distintos

**Contrato individual R1–R5.** Cada método público de adapter que ejecuta un read port se
materializará con:

```text
@Transactional(propagation = MANDATORY, readOnly = true)
```

`MANDATORY` significa exactamente: el adapter debe ser un bean/proxy Spring creado explícitamente
y rechaza la invocación si no hay una transacción ya activa. No crea, suspende, eleva isolation ni
reintenta transacciones. R1–R5 participan en la transacción suministrada y no son owners
conceptuales de `SAME_LOGICAL_SNAPSHOT`. La annotation se prueba cruzando el proxy; llamar con
`new` directamente no constituye evidencia de propagation.

**Contrato candidato de composición R6.** El diseño histórico reserva a un futuro método público
proxied de `DetectorReadCoordinator` la propiedad de una composición
`REQUIRES_NEW + REPEATABLE_READ + readOnly + timeout bounded`. Sin embargo,
`MULTI_READER_MVCC` no pertenece al claim set R1 cerrado en 37.4. Una futura R6 requiere autoridad
separada para su contexto, provenance y statement observations antes de poder componer R1–R5.
Ninguna annotation interna de F2D sustituye ese futuro owner.

R1 usa una sola query por operación y no declara ni necesita repeatable-read para demostrar
projection, mapping, no-write y participación `MANDATORY`. El R1 definido por esta autoridad no
puede formar parte de una evaluación multi-reader: una futura R6 deberá recibir autoridad separada
y corregir explícitamente el contrato de integración antes de admitirlo. Esto mantiene R1
implementable y auditable sin una compatibilidad MVCC ficticia.

### 18.2 Transaction harness TEST-ONLY

El futuro shared test infrastructure vive exclusivamente bajo
`src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/testinfra/**`. Una
`@TestConfiguration` registra como beans separados y por tanto proxied:

```text
ReaderTransactionTestHarness
R1..R5 adapter bajo prueba
executor/factory del slice bajo prueba
```

El harness expone dos métodos públicos, invocados por el test a través del proxy Spring:

```text
inSingleStatementReadOnly(...)
  @Transactional(REQUIRES_NEW, isolation=READ_COMMITTED, readOnly=true)
  uso: R1

inRepeatableReadOnly(...)
  @Transactional(REQUIRES_NEW, isolation=REPEATABLE_READ, readOnly=true)
  uso: R2–R5 y pruebas de composición interna multi-statement
```

Dentro del callback, el test llama al adapter bean proxied; así `MANDATORY` se evalúa realmente.
Después de abrir la TX y antes del callback, el harness consulta `current_setting` y rechaza
read-write o isolation distinta de la declarada; luego crea el `ReadSnapshotContext` determinista
de sección 13/37.4. `sourceName`, `fixtureIdentity` y `schemaFingerprint` provienen únicamente del
`DescriptorRecursoLector`; el test sólo aporta los inputs no confiables permitidos de run/reglas y
scope, que el harness valida. `testInvocationIdentity` se reserva por el registry del harness.
Una prueba negativa llama al adapter proxy fuera del harness y exige
`IllegalTransactionStateException`. Otra prueba verifica que R1 funciona bajo el método
single-statement sin R6 ni `pgSnapshotFingerprint`. El harness no se compila en main, no se escanea
en runtime y no concede semántica productiva.

## 19. Fail-closed ante inconsistencia

Contrato operacional cerrado:

```text
SNAPSHOT_CONSISTENCY_NOT_PROVEN
TRANSACTION_ISOLATION_MISMATCH
TRANSACTION_NOT_READ_ONLY
SNAPSHOT_FINGERPRINT_CHANGED
READ_SET_INVARIANT_VIOLATION
ADAPTER_INPUT_INVALID
SOURCE_ACCESS_FAILURE
NO_WRITE_GUARD_VIOLATION
```

La lista anterior es el catálogo operacional cross-slice histórico, no el enum exhaustivo de R1.
Para R1, 36.2 fija exactamente cuatro valores e incorpora `SOURCE_RECORD_NOT_FOUND`; los guards
transaccionales/no-write permanecen errores del owner/harness y no se convierten en
`ReservationReadException`.

Cualquiera produce:

```text
run_status = ABORTED
operational_error = código + source/scope + safe provenance
DetectorResult for affected read set = NONE
partial results = DISCARDED
report success/zero anomalies = FORBIDDEN
```

Nunca se traduce a `MISSING`, `UNSUPPORTED`, `AMBIGUOUS`, `EXPECTED_ABSENCE`, cero candidates ni
F2D conflict: éstos son estados semánticos sobre input válido. No hay retry interno silencioso.
Una capa futura puede reintentar el run completo una sola vez ante fallo transitorio allowlisted,
con nuevo `attemptIdentity`, nueva TX y evidencia de ambos intentos; no reintenta inconsistencias de
shape, authority, cardinalidad o no-write.

En R2, `ADAPTER_INPUT_INVALID` y `READ_SET_INVARIANT_VIOLATION` llevan la lista no vacía de
`LegacyAdapterRejection` seguros definida en 12.3. Esos artifacts viven sólo en
`operational_errors[]`; no se adapta a `GenericSourceSnapshot`, no se entrega al classifier y no
incrementa `detector_evaluations` ni `semantic_results`.

## 20. No-write y non-mutation

`@Transactional(readOnly=true)` es una declaración/hint útil y, con PostgreSQL, permite solicitar
una transacción read-only; no es por sí sola prueba absoluta contra writes en todos los transaction
managers ni contra mutación Java de una entity managed.

Defensas acumulativas requeridas:

1. projection-first para Reserva, legacy, nominal y ajustes;
2. repository/read ports estrechos sin `save`, `delete`, `flush`, `persist`, `merge` ni writer
   services;
3. adapter classes sin setters ni llamadas a mutadores de entities;
4. effective reader limitado a los métodos read-only existentes y mapping inmediato de records;
5. TX suministrada `readOnly`, y `REPEATABLE_READ` para R2–R6/multi-statement, verificadas en runtime;
6. rol PostgreSQL efímero SELECT-only para implementation tests; credential real separada sólo
   para data audit material;
7. `StatementInspector`/SQL capture de test que permita sólo SELECT y transaction metadata; DML,
   DDL, `FOR UPDATE` y llamadas a funciones no allowlisted fallan el test;
8. rol SELECT-only real: un write intencional de control debe ser denegado antes del test del
   reader;
9. before/after checksums y counts de tablas relevantes desde conexión administrativa de test;
10. unit spies verifican sólo métodos de lectura y cero interacción con writers;
11. architecture tests prohíben imports de persistence/writer services y los tokens
    `.save(`, `.delete(`, `.flush(`, `.persist(`, `.merge(` en adapter/composition;
12. prueba de entity state: ninguna managed entity queda dirty ni existe flush DML al commit.

Rollback no cuenta como evidencia de cero writes: un write revertido sigue siendo una violación.
Hibernate statistics por sí solas tampoco bastan; son corroboración. La combinación mínima
suficiente para un slice JPA es SQL capture + rol SELECT-only + checksums before/after + narrow
ports/architecture tests.

### 20.1 Credencial efímera de implementation tests

El PostgreSQL Testcontainers arranca y Flyway/fixtures usan una conexión privilegiada. Después del
commit del fixture, el harness crea un login efímero distinto para el reader, revoca `CREATE` del
schema y cualquier privilegio de tabla/secuencia/function, y concede sólo `CONNECT`, `USAGE` sobre
`public` y `SELECT` sobre las tablas exactas del slice listadas en 20.3. El datasource usado por el
adapter abre conexiones con ese login; no usa `SET ROLE` desde una conexión privilegiada.

Antes de ejecutar el reader, una transacción de control con ese login intenta un `INSERT` sobre una
tabla fixture y exige `insufficient_privilege`; se descarta y se abre una conexión limpia. No se
concede `USAGE/UPDATE` sobre sequences ni `EXECUTE` sobre functions de aplicación. Este rol es
creado y destruido dentro del container y no representa source, custodian ni credential material.

La futura auditoría material exige por separado source nombrado, custodio/authority, policy real,
identidad de la credencial y evidence de grants efectivos SELECT-only. Esa credencial no es
prerrequisito de R1–R6 implementation.

### 20.2 Mecanismo concreto de inspección SQL

Sin dependencia nueva, los integration tests registran un
`F2eStatementPolicyInspector implements org.hibernate.resource.jdbc.spi.StatementInspector` de
test mediante la propiedad Hibernate del `SessionFactory`. El inspector empieza a capturar sólo
después de Flyway y fixture setup, conserva cada statement normalizado y exige simultáneamente:

1. hash de la SQL normalizada presente en el catálogo exacto de query IDs del slice;
2. statement class allowlisted;
3. ausencia de cualquier token denylisted.

Clases candidatas globales para slices futuros; para R1 la sección 36.6 sustituye esta lista por
un catálogo exhaustivo de cuatro statements y no permite ninguna categoría abierta:

```text
SELECT ordinario sin lock y las SQL exactas catalogadas de R1–R5, incluidos backing y grafo F2D
WITH sólo cuando su statement terminal y todos sus CTE son SELECT read-only y el hash está catalogado
SELECT current_setting('transaction_isolation')
SELECT current_setting('transaction_read_only')
SELECT pg_current_snapshot()
SHOW de una setting exacta allowlisted
SET TRANSACTION ISOLATION LEVEL REPEATABLE READ / READ ONLY sólo si el driver lo emite y sólo antes del primer data SELECT
```

Prohibidos en cualquier posición relevante:

```text
INSERT UPDATE DELETE MERGE TRUNCATE
CREATE ALTER DROP GRANT REVOKE COMMENT VACUUM ANALYZE REINDEX
SELECT ... FOR UPDATE / FOR SHARE / FOR NO KEY UPDATE / FOR KEY SHARE
LOCK TABLE
CALL DO COPY
nextval setval o acceso/mutación de sequence
data-modifying CTE
función/procedure distinta de current_setting y pg_current_snapshot
```

Una SQL desconocida falla aunque empiece con `SELECT`. Los comandos JDBC de isolation/read-only que
no pasan por `StatementInspector` se prueban por `current_setting`; el rol SELECT-only es el
backstop para cualquier camino JDBC. La captura empieza antes del primer read y se detiene después
del commit; DDL/setup/cleanup privilegiados quedan fuera del intervalo medido.

### 20.3 Scope exacto de tablas y filas

| Slice | Tablas protegidas por checksum y grants SELECT | Row scope determinista |
| --- | --- | --- |
| R1 | `reserva` | IDs pedidos; o filas con `salon_id IN scope` y `fecha BETWEEN desde AND hasta` |
| R2 | `turno_instructor`, `turno_instructor_usuario`, `turno_instructor_asignacion` | `turno_instructor.id` seleccionados por salones + recurrente/day o puntual/date; children por esos `turno_id` |
| R3 | `programacion_asignacion`, `programacion_bloque` | asignaciones/bloques activos aplicables a `fecha`; los IDs exactos se congelan al construir baseline |
| R4 | `programacion_ajuste_fecha` | filas activas de la fecha exacta solicitada; no existe port de rango |
| R5 | `programacion_asignacion`, `programacion_bloque`, `programacion_ajuste_fecha`, `salon`, `salon_horario_excepcion`, `horario_operacion`, `usuario`, `usuario_rol`, `rol`, `tipo_actividad`, `instructor_actividad`, `salon_tipo_actividad` | nominales/ajustes de la fecha exacta; salones finales y horario/excepción aplicable a esa fecha; usuarios/roles/especialidades, actividades y oferta alcanzados por candidates |
| R6 | unión exacta R1–R5: las 16 tablas anteriores más `reserva`, `turno_instructor`, `turno_instructor_usuario`, `turno_instructor_asignacion` | unión de los IDs/scopes de la request por una fecha o bounded unit; no scan global |

R1 no incluye tablas maestras porque su native query sólo obtiene FKs escalares desde
`reserva`; no navega ni lee los targets. R3 incluye bloque porque lo une y valida. R5/R6 incluyen
las relaciones join sin entity propia porque el lazy graph sí las lee. Los grants pueden cubrir la
tabla completa dentro del container; el checksum nunca es global y se restringe a los PK/scope IDs
del fixture.

### 20.4 Canonicalización y orden before/read/after

Esta subsección conserva el resumen histórico del diseño. Para R1, la gramática, versiones,
column set, row/table/slice hashes y golden vectors exactos de 36.8–36.9 son la autoridad
normativa y sustituyen cualquier formulación aquí que sólo diga `length:value` o `SHA-256`.

Para cada tabla se seleccionan **todas** sus columnas persistidas, en orden de ordinal de schema,
no sólo los campos usados por el reader. Las filas se ordenan por PK física: UUID simple; o tupla
PK en orden de columnas para `turno_instructor_usuario`, `turno_instructor_asignacion`,
`instructor_actividad` y `salon_tipo_actividad`; `usuario_rol` se ordena por su PK UUID `id`.
La serialización usa UTF-8,
componentes `length:value` y tags de tipo:

```text
NULL=<NULL>
UUID=lower-case canonical
boolean=true|false
date=ISO-8601 yyyy-MM-dd
time=HH:mm:ss.ffffff, precisión microsegundo fija
timestamptz=UTC ISO-8601 con seis dígitos fraccionales y Z
integer/short=decimal sin padding
enum/text=bytes UTF-8 length-prefixed, sin trim/case folding
```

El stream incluye `schema.table`, nombres/tipos de columna, row count y cada PK/row canónica; se
calcula `SHA-256` por tabla y después un hash de slice ordenado por nombre de tabla. El mismo helper
se reutiliza desde R1; cambiar schema, columna, null, timestamp o fila altera el hash.

Orden obligatorio:

1. aplicar Flyway y crear fixture con conexión privilegiada;
2. commit completo del setup;
3. resolver/fijar PKs del row scope y calcular baseline con observer privilegiado;
4. abrir conexión del rol SELECT-only y ejecutar reader/transaction;
5. commit/cierre del reader sin cleanup;
6. con fixture aún quiescent, calcular final checksum con observer privilegiado y comparar hashes y counts;
7. inspeccionar SQL capturada/statistics y sólo entonces hacer cleanup privilegiado fuera del gate.

No hay writer concurrente durante esta prueba. Los tests de snapshot con writer concurrente son
otros tests controlados con latches y no usan before/after checksum como atribución de mutación.

### 20.5 Proof material distinto

En un audit material potencialmente concurrente no se exige checksum global before/after: un
writer legítimo externo lo invalidaría sin demostrar mutación del adapter. El proof requerido es
la combinación de credential/policy SELECT-only verificada, transaction read-only, catálogo de SQL
allowlisted, `application_name`/run identity y session/audit evidence del servidor para esa
conexión. Sólo se usan checksums si la source es un snapshot restaurado/quiescent o si el custodio
garantiza el row scope congelado. La autorización material debe nombrar qué evidencia de sesión o
audit log está disponible; el harness Testcontainers no se presenta como sustituto.

## 21. Runtime isolation y Spring beans

Decisión por slice:

```text
Reader implementation slices R1-R5:
  adapter/mappers = plain classes, NO @Component/@Service/@Repository/@Configuration
  production bean = NONE
  integration tests instantiate/import explicitly

Composition slice R6:
  adapters + coordinator may become Spring beans only behind both:
    profile: f2e-detector-shadow
    property: app.f2e.detector.adapters.enabled=true (matchIfMissing=false)
  default/dev/prod without both gates: beans ABSENT
```

No basta con que “nadie los llame”. Tests deben comprobar:

- contexto default y `prod`: no existen beans adapter/coordinator;
- contexto shadow explícito: existen, pero no hay controller, endpoint, scheduler, listener,
  runner, writer ni productive service que los consuma;
- búsqueda de dependencias demuestra que `ReservaService`, `TurnoInstructorService`, controllers,
  jobs y operación productiva no importan packages de transición;
- ninguna property de runtime existente activa el profile/property por default;
- no existe fallback o reader switch.

Los query executors adapter-local no son repositories ni beans autodetectados. Se registran sólo
por la misma configuración condicional en R6. Antes de R6, las pruebas los crean en
`@TestConfiguration`; no se amplía el component scan productivo.

`f2e-detector-shadow` y `app.f2e.detector.adapters.enabled` son contratos de configuración
**futuros y nuevos**; no se afirma que existan hoy. El handoff R6 deberá autorizar explícitamente
crear una nueva configuration class bajo `transicion/programacion/composition/**` con
`@Profile("f2e-detector-shadow")` y
`@ConditionalOnProperty(name="app.f2e.detector.adapters.enabled", havingValue="true",
matchIfMissing=false)`. No necesita modificar `application*.properties`: ausencia de la property
mantiene todo deshabilitado y los tests la aportan inline. Si se pretendiera crear/modificar un
resource file, el handoff R6 tendría que allowlistarlo de forma expresa.

R6 no tiene consumer runtime en este diseño. En shadow opt-in materializa coordinator/readers pero
ningún scheduler, controller, listener, runner, startup hook o service productivo los invoca. Los
integration tests obtienen el coordinator del contexto shadow y llaman directamente su proxy; los
tests default/prod demuestran ausencia de beans. Añadir un trigger requeriría otro diseño/handoff y
no queda autorizado por R6.

## 22. Package architecture futura

```text
com.feelingpilates.transicion.programacion.detector
  núcleo puro existente; sin cambios de dependencia

com.feelingpilates.transicion.programacion.read
  ports/read contracts y run/read-set records inmutables

com.feelingpilates.transicion.programacion.adapter.jpa.projection
  records/projection executors escalares; JPA/EntityManager permitido

com.feelingpilates.transicion.programacion.adapter.jpa.mapper
  mapping projection/domain record -> core snapshots

com.feelingpilates.transicion.programacion.adapter.jpa
  adapters R1-R5 que implementan read ports

com.feelingpilates.transicion.programacion.composition
  coordinator, conditional configuration y operational error contract
```

No se ubican adapters bajo `com.feelingpilates.programacion`, para no mezclarlos con la autoridad
dark-launch F2D ni debilitar sus tests de aislamiento.

## 23. Matriz de dependencias

| From → To | Estado | Motivo |
| --- | --- | --- |
| `detector` → `java.*`, `ReferenciaOcurrencia` | ALLOWED | contrato puro existente |
| `detector` → `read`, `adapter`, Spring/JPA/repositories | FORBIDDEN | inversión del boundary |
| `read` → `detector`, `java.*` | ALLOWED | ports expresan outputs del core |
| `read` → JPA/entities/repositories/Spring | FORBIDDEN | ports permanecen persistence-agnostic |
| `adapter.jpa.projection` → JPA + source entities/schema | ALLOWED | boundary físico |
| `adapter.jpa.mapper` → projection/read/detector | ALLOWED | mapping hacia inmutables |
| `adapter.jpa` → projection/mapper/read/detector + read-only existing services | ALLOWED | implementación del port |
| `adapter.jpa` → writer services/persistence helpers | FORBIDDEN | no-write |
| `composition` → read/adapters/detector + Spring transaction API | ALLOWED | ownership transaccional |
| `calendario`, `programacion`, `ubicaciones` productivos → transition adapter/composition | FORBIDDEN | no consumer/authority switch |
| controllers/jobs/listeners/runners → adapter/composition | FORBIDDEN | runtime isolation |
| adapter → `ReservaService`/`TurnoInstructorService` | FORBIDDEN | no reuso de consumers/writers productivos |
| adapter effective → `ProgramacionEfectiva` | ALLOWED, R5 ONLY | preserva composición F2D read-only |

## 24. Slicing de readers

| Orden | Slice | Input/output | Dependencias | Qué no incluye | HostValidator |
| --- | --- | --- | --- | --- | --- |
| R1 | Reserva reader | projection exacta → `ReservationSourceSnapshot` sin historical target | core cerrado + schema V15 | candidates, legacy, coordinator, bean productivo | REQUIRED |
| R2 | Turno legacy reader | dos projections → `LegacyTurnReadSet` o `LegacyAdapterRejection` pre-core | R1 sólo como patrón; core | mapping legacy, history inference, writer repo | REQUIRED |
| R3 | Nominal new reader | dedicated native executor/row → nominal candidates/provenance | F2D/V41/V47 | effective composition, adjustments mapping | REQUIRED |
| R4 | Adjustment reader | scalar projection → NEW_* source snapshots | R3 para tests target; V47 | legacy mapping, effective resolver | REQUIRED |
| R5 | Effective new reader | `ProgramacionEfectiva` + R3/R4 backing → effective candidates/outcomes | R3 + R4 + F2D services | coordinator cross-source, reporting | REQUIRED |
| R6 | Coordinator/composition | RR read set → pure detector request/result | R1–R5 PASS | resolver, selection, report persistence, productive consumers | REQUIRED |

No se agrupan todos: cada source tiene riesgos distintos y un gate independiente reduce el radio
de error. R3 precede R4/R5 porque cancelación/reemplazo necesitan nominal evidence. R4 precede R5
para enlazar provenance efectiva.

### Primer slice recomendado

`R1 — Reserva reader`.

Motivos:

- mapea directamente al tipo exacto `ReservationSourceSnapshot` ya materializado;
- una sola tabla y query permiten validar el patrón projection-first, fingerprints y no-write;
- cierra físicamente el mayor riesgo lazy de Reserva sin depender de F2D composition;
- demuestra de forma ejecutable que historical target permanece vacío;
- no necesita Turno, nominales, ajustes, operación, coordinator productivo ni decisiones de
  mapping;
- crea la base reusable de SQL capture, SELECT-only credential y HostValidator para los slices
  siguientes.

Este checkpoint no autoriza R1.

### 24.1 Allowlists futuras cerradas por slice

Cada handoff futuro deberá concretar filenames dentro de estos paths/tipos; no podrá ampliar el
tipo de artefacto. Todos son archivos nuevos salvo los shared testinfra ya creados por un slice
anterior. Ningún slice modifica un repository/service/entity/config productivo existente.

| Slice | New production files allowed | New test files/config allowed | Existing tracked production modification |
| --- | --- | --- | --- |
| R1 | port/context/scope bajo `src/main/java/com/feelingpilates/transicion/programacion/read/**`; `ReservaProjectionRow` + executor bajo `adapter/jpa/projection/**`; mapper bajo `adapter/jpa/mapper/**`; `ReservaJpaReader` bajo `adapter/jpa/**` | unit mapper; PostgreSQL projection/integration; architecture; `adapter/jpa/testinfra/**` para container, transaction harness, SELECT role, StatementInspector y checksum | NO |
| R2 | port/scope/read set/operational rejection contract; `LegacyTurnMemberRow`, `LegacyAssignmentRow`, un executor; mapper/aggregator y `LegacyTurnJpaReader`, todos sólo en packages de transición | R2 unit/JPA/shape/scenario/count tests; puede reutilizar testinfra R1 sin cambiar semántica Reserva | NO |
| R3 | port; `NominalProjectionRow`, executor, mapper, `NominalJpaReader` | unit/JPA/vigencia/cardinality/no-write tests + shared testinfra | NO; `AsignacionRepository` queda intacto |
| R4 | port/backing snapshot; `AdjustmentProjectionRow`, executor, mapper, `AdjustmentJpaReader` | forms/null/cardinality/JPA/no-write tests + shared testinfra | NO; `AjusteProgramacionFechaRepository` queda intacto |
| R5 | port/read-set/omission evidence; `InMemoryProgramacionDiagnosticoCollector`, `EffectiveProgrammingReadGraphFactory`, mapper y `EffectiveProgrammingJpaReader` | graph, omission, F2D backing, entity-escape, SQL/checksum/RR tests + shared testinfra | NO; F2D queda intacto |
| R6 | sólo `DetectorReadCoordinator`, operational error/read-set envelope y conditional shadow configuration bajo `transicion/programacion/composition/**` | default/prod/shadow context, proxy/transaction metadata, latches, abort/no-write/architecture tests | NO; sin resources por default y sin productive consumer |

R2 hereda de R1 exclusivamente container PostgreSQL, Flyway boot, SELECT-only role setup, SQL
policy inspector, checksum canonicalizer, architecture rules y transaction test owner. No hereda
queries, DTOs, mappers, identities ni semántica de Reserva. R3–R6 reutilizan el mismo testinfra bajo
la misma regla.

`HostValidator` es REQUIRED para el integration gate de cada slice R1–R6 que arranque
PostgreSQL/Testcontainers. El subset unitario de mapper/records/architecture que no necesita
Docker puede ejecutarse sin HostValidator. Docker ausente bloquea sólo el evidence gate de host;
no se reclasifica como P1 semántico.

## 25. Coordinator decision

```text
Coordinator: REQUIRED_AFTER_READERS
Implementation now: NOT_AUTHORIZED
```

Preconditions para R6:

- R1–R5 implementados y auditados con projection/mapping/no-write contracts PASS;
- operational error contract materializado sin traducirse a domain statuses;
- Spring conditional isolation probada en default/prod/shadow contexts;
- PostgreSQL RR/read-only metadata probe probado;
- tests concurrent-update/read demuestran un único snapshot;
- todos los outputs comparten `logicalSnapshotId` y ninguna entity/projection escapa;
- sigue sin existir data audit material, report sink productivo, crosswalk, selector, resolver,
  fence o consumer productivo.

## 26. Estrategia futura de tests

### 26.1 Unit/mapping por slice

- binding tipado, collection vacía/null, nullability, enums, rangos half-open, orden y canonical hashes;
- Reserva: estado, IDs, timestamps técnicos, historical target vacío;
- legacy: dos null=fallback; una null/gap/orphan divergent sólo para RECURRENTE; las mismas shapes
  EXCEPCION/CANCELACION permanecen UNKNOWN_INTENT/UNSUPPORTED; malformed/duplicate abortan pre-core
  con cero evaluations/results; RECURRENTE/EXCEPCION/CANCELACION sin inferencias;
- nominal: vigencias inclusivas/open-end, serie/version/reference y duplicate series fail-closed;
- ajustes: las tres formas y todas las combinaciones inválidas;
- effective: origin/reference mapping y backing exacto por recurrent/replacement/addition;
- inputs y projection DTOs intactos después de mapping; defensive copies.

### 26.2 JPA/projection

`@DataJpaTest` es admisible sólo si ejecuta PostgreSQL/Testcontainers y Flyway real; H2 queda
prohibido por native SQL, `daterange`, partial indexes y EXCLUDE. Verificar cada alias/tipo/null,
scope bounded, ordering, SQL catalogada con `IN (:ids)`/`setParameterList`, scalar binding,
short-circuit/invalidación de vacíos, cero duplicados por joins y que no aparecen
entities/proxies en output. R4 prueba exclusivamente fecha exacta y ausencia de operación por
rango.

### 26.3 PostgreSQL/Testcontainers

- PostgreSQL `16-alpine`, Flyway hasta V47 y `ddl-auto=validate`;
- fixtures para relaciones lazy, miembros sin actividades, vigencias, adjustments y operación;
- queries reales y mapping exacto;
- rol SELECT-only y negativa de INSERT/UPDATE/DELETE/DDL;
- schema fingerprint y query compatibility.

### 26.4 Transaction/isolation

- R1 standalone: assert `transaction_isolation=read committed` y
  `transaction_read_only=on`, sin claim multi-reader;
- R2–R5 multi-statement harness y R6: assert
  `transaction_isolation=repeatable read` y `transaction_read_only=on`;
- reader fuera de TX falla por `MANDATORY`;
- el owner guard del test harness rechaza read-write antes de invocar el adapter; R1 acepta RC sólo
  con `SINGLE_READER_TEST` y rechaza `MULTI_READER_MVCC` antes de probes/SQL; cualquier futura
  composición RR/R6 requiere autoridad y contrato separados;
- dos latches: writer comitea entre nominal y adjustment/master reads; el run RR conserva el primer
  snapshot y un run posterior ve el nuevo estado;
- control negativo READ COMMITTED demuestra que statements pueden ver estados distintos;
- initial/final `pg_current_snapshot()` iguales;
- exception en cualquier reader descarta el read set completo y no invoca classifier/report success.

### 26.5 No-write

- SQL capture allowlist sólo SELECT/transaction metadata;
- credentials SELECT-only verificadas;
- checksums/counts before/after desde conexión admin;
- repository spies sin writers;
- Hibernate statistics con entity insert/update/delete count cero como corroboración;
- test que muta deliberadamente una entity/control intenta flush y es rechazado, para demostrar que
  el guard realmente detecta;
- rollback-only no se acepta como sustituto.

### 26.6 Architecture/runtime

- package dependency matrix de la sección 23;
- core sigue sin Spring/JPA/Hibernate/repositories;
- adapter slices sin stereotypes antes de R6;
- default/prod context sin beans; shadow context doblemente habilitado;
- cero imports desde productive services/controllers/jobs/listeners/runners;
- ausencia de endpoints, scheduler, event listener, startup runner y reader/writer switch.

## 27. HostValidator

```text
Current DESIGN unit: NOT_REQUIRED
Future JPA implementation slices R1-R6: REQUIRED
```

Plan determinista allowlisted futuro:

1. Docker daemon accesible y API compatible;
2. imagen `postgres:16-alpine` disponible;
3. puerto/red/volumen temporal de Testcontainers funcional;
4. Flyway aplica hasta V47, checksums válidos, sin failed/pending inesperado;
5. JPA valida mappings;
6. targeted mapping/projection tests;
7. SQL/no-write and SELECT-only tests;
8. RR/concurrency tests cuando el slice usa múltiples statements;
9. architecture/runtime tests;
10. preservar HEAD, staging y source fingerprint antes/después.

Una falta de Docker es `HOST_VALIDATION_BLOCKED`, no un test PASS ni un fallo semántico. El
HostValidator no ejecuta comandos sugeridos libremente por un agente.

## 28. Prerrequisitos cerrados de data audit

Dos gates no se mezclan:

```text
Adapter implementation R1–R6:
  source/custodian/credential material: NOT_REQUIRED
  PostgreSQL Testcontainers + rol efímero SELECT-only + HostValidator: REQUIRED para integration

Material data audit:
  source nombrada + custodian/authority + credential/policy SELECT-only real: REQUIRED
  Testcontainers credential: NOT_ACCEPTABLE como sustituto
```

Para cambiar algún día de `DATA_SOURCE_NOT_AVAILABLE` a
`AUTHORIZED_READ_ONLY_DATA_AUDIT` deben satisfacerse todos:

1. autoridad documental/handoff que autorice exactamente data audit, source, scope y operador;
2. source nombrada: ambiente, custodio, endpoint/snapshot timestamp y checksum;
3. réplica/export/snapshot coherente o acceso PostgreSQL con RR strategy aprobada;
4. credencial exclusiva SELECT-only sobre tablas allowlisted, sin DML/DDL, verificada mediante
   capability/negative test;
5. R1–R6 técnicos necesarios implementados, auditados y HostValidator PASS;
6. commit, detector/rule/projection versions fijados;
7. Flyway fingerprint: current V47, applied versions/descriptions/checksums/states, sin failed o
   pending incompatible;
8. schema fingerprint de tablas, columnas, tipos, nullability, PK/FK/CHECK/unique/partial indexes/
   EXCLUDE relevantes;
9. compatibilidad de cada query/projection contra ese schema;
10. timezone/business zone, day convention, half-open intervals, inclusive vigencias y bounded
    date/salon scope declarados;
11. snapshot strategy y metadata initial/final verificadas;
12. política PII/minimization y acceso al artefacto;
13. report mechanism inmutable/no productivo preparado;
14. abort/error channel operacional distinto de domain findings;
15. confirmation explícita: crosswalk/selection/resolver/fence/migration/cutover siguen fuera.

Falta uno solo: `DATA AUDIT = NOT_AUTHORIZED / ABORT`.

El data audit material requiere R6, no un runner externo que llame R1–R5 libremente, porque R6 es
el único owner aprobado de `REQUIRES_NEW + REPEATABLE_READ`, verification de transaction metadata,
`ReadSnapshotContext`, initial/final MVCC guard, composición y discard atómico. Un runner futuro
puede solicitar una corrida y escribir el reporte después, pero no abrir otra transacción ni
recomponer readers. Sin R6 no existe evidence suficiente para afirmar que legacy, nominal,
ajustes, efectiva y Reserva pertenecen al mismo logical snapshot.

## 29. Requirement de reporte futuro

El audit futuro producirá sólo un artefacto no productivo, inmutable y orientado a archivos:

```text
run-directory/
  manifest.json
  results.ndjson
  metrics.json
  errors.ndjson
  SHA256SUMS
```

El manifest contiene run/attempt/logical snapshot IDs, source/schema/Flyway fingerprints, scope,
versions, timestamps, counts y hashes. Se escribe primero en un path temporal y se publica por
rename atómico sólo tras completar hashes; un run abortado conserva manifest/error y nunca un
reporte de éxito. No se escribe ninguna tabla productiva, crosswalk, mapping, selected target ni
estado de fence. El sink se ejecuta fuera de la transacción de lectura sobre el read set inmutable.

Este diseño no implementa el report mechanism ni autoriza filesystem output material.

## 30. Áreas diferidas y autoridad preservada

```text
D08: DEFERRED
Crosswalk persistence: NOT_AUTHORIZED
Mapping selection: NOT_AUTHORIZED
Resolver material: NOT_AUTHORIZED
Fence/cohort/enforcement: NOT_AUTHORIZED
Migration/normalization/backfill: NOT_AUTHORIZED
MIGRANDO: NO
NUEVA: NO
Cutover: false
Reserva schema/target backfill: NOT_AUTHORIZED
TurnoInstructor authority: PRESERVED / PRODUCTIVE
DARK_LAUNCH: PRESERVED
NOT_PRODUCTIVE: PRESERVED
```

## 31. Tabla final de decisiones

| ID | Question | Decision | Evidence | Alternatives rejected | Implications | Future owner/slice | Status |
| --- | --- | --- | --- | --- | --- | --- | --- |
| AD-01 | ¿Entity o projection para Reserva? | Dedicated native `EntityManager` executor + concrete row; binding `NativeQuery` regido por AD-31; `ReservaRepository` intacto | 4 LAZY relations; runtime isolation; stack Boot/Hibernate físico | Entity/repository modification/Spring proxy projection | cero lazy/managed, no scan y query mecánica | R1 | CLOSED |
| AD-02 | ¿Cómo representar target histórico de Reserva? | `Optional.empty()` desde schema actual | no FK/target persistido | inferir por contención | unknown distinto de demonstrated | R1 | CLOSED |
| AD-03 | ¿Cómo leer legacy aggregate? | dedicated native executor, dos concrete rows, binding 12.2, assignment atom + gap/abort rules 12.3 | evita producto cartesiano, fija incompletos y respeta escenarios admitidos por core | entity graph / existing repositories / single join | current snapshot, counts y boundary pre-core deterministas | R2 | CLOSED |
| AD-04 | ¿Historia legacy? | recurrente histórico usa `LEGACY_HISTORY_REQUIRED/UNKNOWN_HISTORY`; puntual conserva scenario UNKNOWN_INTENT y marker de historia no persistida | no versioning + combinaciones legales del classifier; timestamps técnicos | timestamps como vigencia / scenario histórico ilegal para puntual | fail-closed sin `INVALID_SCENARIO` | R2 | CLOSED |
| AD-05 | ¿Intento puntual legacy? | toda EXCEPCION/CANCELACION representable usa su scenario específico UNKNOWN_INTENT/UNSUPPORTED, aun con markers; malformed aborta pre-core | D10 + `DetectorClassifier.validateSourceScenario` físico | map automático / `INCOMPATIBLE_EVIDENCE` puntual ilegal | no legacy→APF y ningún INVALID_SCENARIO fabricado | R2 | CLOSED |
| AD-06 | ¿Nominal query actual basta? | No; dedicated native executor/row con scalar binding 12.2, sin modificar ni envolver `AsignacionRepository` | ocho campos actuales omiten provenance | extender/reusar repository productivo | contract, parámetros y owner exactos | R3 | CLOSED |
| AD-07 | ¿Nominal=effective universe? | No; universes separados por claim | F2D pipeline/cancellation | lista única mezclada | evita duplicate identity y false missing | R3/R5 | CLOSED |
| AD-08 | ¿Cómo leer ajustes? | dedicated native executor + concrete row por fecha exacta, binding escalar 12.2; sin port de rango; repository intacto | entity mutable; V47 shape exacta; consumer sólo por fecha | managed entity/existing repository/range sin consumer | no dirty checking ni superficie R4 innecesaria | R4 | CLOSED |
| AD-09 | ¿Cómo preservar F2D effective semantics? | graph dark-launch dedicado construido con APIs F2D + collector in-memory; no bean productivo PE | implementation/review F2D PASS y SLF4J side effect | duplicar composer/reusar bean con logger | F2D intacto, no I/O en TX | R5 | CLOSED |
| AD-10 | ¿Boundary lazy? | todo lazy termina dentro de la TX suministrada por R6 o test harness | associations físicas | OSIV/detach | no proxies fuera | R1-R5 | CLOSED |
| AD-11 | ¿Snapshot suficiente? | una TX física RR por evaluation read set | PG RC statement snapshots | RC best effort | reproducible MVCC view | R6 | CLOSED |
| AD-12 | ¿Single query? | Rechazada globalmente | varias granularidades + F2D service | mega-query | modularidad y cardinalidad | R6 | CLOSED |
| AD-13 | ¿Transaction owner? | R6 `REQUIRES_NEW/RR`; readers proxied `MANDATORY`; test-only owner explícito | Spring propagation semantics | cada adapter abre TX / depender de R6 para test | misma TX y R1 independiente | R1-R6 | CLOSED |
| AD-14 | ¿Serializable/locks? | No; RR read-only, sin locks | observational dark launch | serializable/table locks | menos bloqueo/overhead | R6 | CLOSED |
| AD-15 | ¿Snapshot failure? | ABORTED operational, discard all | core error separation | map a MISSING/UNSUPPORTED | fail-closed real | R6 | CLOSED |
| AD-16 | ¿`readOnly=true` basta? | No; StatementInspector exact-hash policy + rol SELECT-only + scoped canonical checksum + architecture/statistics; R1 usa las versiones y catálogo exactos de 36.4–36.9 | Spring lo trata como hint/optimization | sólo annotation/rollback/global checksum | evidencia 0 writes acotada | all implementation slices | CLOSED / AMENDED_FOR_R1 |
| AD-17 | ¿Beans antes de composition? | No; plain adapters/executors con `EntityManager` y unwrap `NativeQuery`, test-only import | runtime isolation requirement + binding Hibernate explícito | scanned internal bean / Spring Data repository | ausencia verificable sin volver opcional el query contract | R1-R5 | CLOSED |
| AD-18 | ¿Beans en R6? | sólo profile + disabled-by-default property | dark launch | unconditional bean | explicit opt-in no productivo | R6 | CLOSED |
| AD-19 | ¿Primer slice? | Reserva reader | mapping directo/one table/high lazy risk | combined readers | menor riesgo y reusable harness | R1 | CLOSED |
| AD-20 | ¿Coordinator? | REQUIRED_AFTER_READERS | multi-reader same snapshot | deferred indefinitely / now | no composition premature | R6 | CLOSED |
| AD-21 | ¿HostValidator? | design no; every JPA slice yes | PostgreSQL/Testcontainers physical behavior | sandbox-only | evidence real | R1-R6 | CLOSED |
| AD-22 | ¿Data audit ahora? | No; source/custodian/credential material separados del rol efímero de implementation | source unavailable/no authority | Testcontainers/local config como source | no fabricated results | future data-audit handoff | CLOSED |
| AD-23 | ¿Report? | immutable file artifact outside read TX | no DB/crosswalk writes | DB report table | nonproductive/auditable | future audit | CLOSED |
| AD-24 | ¿Firmas/ausencia de read ports? | Tabla 12.1; missing requested Reserva=`SOURCE_RECORD_NOT_FOUND`, abort total/cero parciales; filtro collection vacío=`ADAPTER_INPUT_INVALID`; resultado de scope/universe sin filas es válido por claim | core + source cardinality + 36.2 | dejarlo al executor / quitar filtro vacío / mapear a semantic `MISSING` | handoff R1–R5 sin decisión residual | R1-R5 | CLOSED / AMENDED_FOR_R1 |
| AD-25 | ¿Átomo e incompletos R2? | assignment PK; gaps con evidence key; fórmula exacta; representable vs pre-core rejection y counts 12.3 | PKs V19/V20/V22 + core real | dedupe / divergent universal | mismo estado produce mismo count y scenario legal | R2 | CLOSED |
| AD-26 | ¿Provenance depende de R6? | `ReadSnapshotContext`; R6 o test harness lo suministra; MVCC, run y read-set son IDs separados | single reader no debe inventar pg snapshot | global clock/random/fingerprint falso | R1 independiente | R1-R6 | CLOSED |
| AD-27 | ¿Diagnóstico R5? | collector adapter-local in-memory dentro de graph dedicado | interface `ProgramacionDiagnostico` inyectable | SLF4J callback/no diagnostics | omissions transportables sin I/O | R5 | CLOSED |
| AD-28 | ¿Checksum/no-write scope? | tablas/rows 20.3 y orden before/read/after; R1 usa `public.reserva`, todas sus columns y `F2E_CHECKSUM_*_V1` de 36.8–36.9 | schema físico V15 y concurrencia | checksum global/no DML genérico/hash sin table o scope | proof reproducible por slice | R1-R6 | CLOSED / AMENDED_FOR_R1 |
| AD-29 | ¿Cómo se inyectan readers/executors? | plain constructor injection; test config antes de R6 y conditional config en R6 | no autodetection requirement | stereotypes/repository scan | runtime reachability cerrada | R1-R6 | CLOSED |
| AD-30 | ¿Profile/property/consumer? | future new config; doble condition; absent by default; coordinator sin trigger, test lo invoca | keys no existen hoy | fingir config o scheduler | materialización aislada verificable | R6 | CLOSED |
| AD-31 | ¿Binding nativo R1–R4? | named scalars tipados y UUID multivaluado exclusivamente con `IN (:ids)` + Hibernate `NativeQuery.setParameterList(..., UUID.class)`; vacíos/null según 12.2 antes de SQL | Boot 4.1.0, Hibernate ORM 7.4.1.Final y API `CommonQueryContract` física; PostgreSQL uuid/date/time/smallint/varchar/boolean | `ANY(uuid[])`, arrays JDBC, temporary table, string interpolation, placeholder expansion manual, nullable filters | una estrategia segura y determinista; executor no decide | R1-R4 | CLOSED |
| AD-32 | ¿Precedencia structural vs semantic legacy? | malformed/unrepresentable aborta pre-core; recurrente representable anómala usa `INCOMPATIBLE_EVIDENCE`; EXCEPCION/CANCELACION representables siempre usan scenario UNKNOWN_INTENT propio y transportan markers en source/provenance | `DetectorClassifier.validateSourceScenario`, `GenericSourceSnapshot` y D10 físicos | `DIVERGENT_INCOMPATIBLE` universal / cambiar core / perder markers | cero `INVALID_SCENARIO`, artifacts y counts separados | R2 | CLOSED |

## 32. Blockers y human decisions

No existe blocker de autoridad o decisión humana para presentar este diseño a audit fresh.

Blockers materiales preservados:

| Blocker | Afecta | Resolution path |
| --- | --- | --- |
| Design gate no ejecutado | cualquier implementation handoff | fresh independent design audit P0=0/P1=0 |
| Readers/coordinator no implementados | data audit | slices R1–R6 separados y auditados |
| `DATA_SOURCE_NOT_AVAILABLE` | material data audit | prerequisites de sección 28 |
| SELECT-only credential ausente | material data audit | owner/authority futura; no crear aquí |
| Mappings reales no observados | crosswalk/resolver/migration | audit futuro; nunca heurística |
| D08/fence diferido | migration/cutover | unidad y gate separados |
| Crosswalk/selector/resolver no autorizados | selection/migration | futuros designs/handoffs |

```text
Human decision required for current scope: NO
Blocked by authority for current checkpoint: NO
Open questions within current scope: NINGUNA
```

## 33. Future handoff candidates — propuestas, no autorización

### 33.1 R1 Reserva reader

```text
Type: IMPLEMENTATION_READ_ONLY / JPA_ADAPTER
Scope: projection, mapper, immutable output, tests; no production bean
Dependencies: this design PASS + pure core
HostValidator: REQUIRED
Tests: unit mapping, PostgreSQL projection, no-write, lazy/architecture/runtime absence
```

El documenter de ese handoff debe copiar, no decidir: dedicated native executor/row y repository
productivo intacto (7.1/12.1); fields/mapping (7.2); dos firmas y ausencia (12.1); reader proxied
`MANDATORY` + single-statement test owner (18.1–18.2); `ReadSnapshotContext` sin PG fingerprint
inventado (13); constructor/test wiring (12.1/21); role, StatementInspector, failure contract,
catálogo SQL y checksum de `public.reserva` (20 y 36.2–36.9); binding, vacíos y nulls exactos
(12.2); HostValidator (24.1/27); y allowlist R1 (24.1). No
queda decisión arquitectónica de R1 para el executor.

### 33.2 R2 Turno legacy reader

```text
Type: IMPLEMENTATION_READ_ONLY / JPA_ADAPTER
Scope: two dedicated native rows, aggregation, current history/unknown intent evidence,
       structural markers y operational rejection artifact
Dependencies: this design PASS; shared R1 testinfra required, no Reserva semantics
HostValidator: REQUIRED
Tests: binding/empty/null, joins/cardinality, null fallback, recurrent divergent,
       punctual UNKNOWN_INTENT aun con markers, pre-core abort/counts, no history inference/no-write
```

### 33.3 R3 Nominal programming reader

```text
Type: IMPLEMENTATION_READ_ONLY / JPA_ADAPTER
Scope: dedicated native executor/row, exact scalar binding and nominal snapshot mapping
Dependencies: this design PASS + F2D contracts
HostValidator: REQUIRED
Tests: V41/V47, vigencias, series, provenance, duplicate fail-closed, no-write
```

### 33.4 R4 Adjustment reader

```text
Type: IMPLEMENTATION_READ_ONLY / JPA_ADAPTER
Scope: dedicated native executor/row por fecha exacta + three typed source mappings; no range port
Dependencies: R3 for nominal-target test fixtures
HostValidator: REQUIRED
Tests: scalar/null binding, date-only surface, all shapes/provenance/no legacy mapping/no-write
```

### 33.5 R5 Effective programming reader

```text
Type: IMPLEMENTATION_READ_ONLY / JPA_ADAPTER
Scope: dedicated F2D graph + in-memory diagnostics + R3/R4 backing/outcome validation
Dependencies: R3 + R4 PASS; F2D services unchanged
HostValidator: REQUIRED
Tests: origin/reference, cancellation absence, replacement/addition, final salon,
       fail-closed absence, RR read set, no entity escape/no-write
```

### 33.6 R6 Coordinator/composition

```text
Type: IMPLEMENTATION_READ_ONLY / DARK_LAUNCH_COMPOSITION
Scope: transaction owner, metadata guard, snapshot read set, conditional beans
Dependencies: R1-R5 PASS
HostValidator: REQUIRED
Tests: concurrent snapshots, operational abort, default/prod absence, shadow opt-in,
       no productive reachability/no-write
```

Cada candidato requiere su propio handoff auditado y activo. Este checkpoint no autoriza ninguno.

## 34. Exit conditions y siguiente gate

Exit conditions documentales de esta materialización:

- checkpoint único y autocontenido;
- inventory, exact fields y query/projection strategy cerrados;
- core/F2D preservados sin rediseño;
- managed/lazy/immutable boundary cerrado;
- SAME_LOGICAL_SNAPSHOT, isolation, transaction ownership y fail-closed cerrados;
- no-write, runtime isolation, packages/dependencies y slicing cerrados;
- coordinator, tests, HostValidator y data-audit prerequisites cerrados;
- D08, crosswalk, resolver, fence, migration, authority y cutover preservados;
- ningún código, test, config, migration, DB o dato modificado;
- `HEAD`/staging invariantes y único touched path igual a este checkpoint;
- whitespace y `git diff --check` limpios.

Siguiente gate:

```text
FRESH_INDEPENDENT_DESIGN_DOCUMENT_AUDIT
Required: P0=0 / P1=0
Design gate now: PENDING / NOT_PERFORMED
Implementation authorized: NO
Maximum disposition: READY FOR FRESH INDEPENDENT DESIGN DOCUMENT AUDIT
```

## 35. Declaración explícita de no implementación

```text
ADAPTERS / SNAPSHOT CONSISTENCY DESIGN MATERIALIZED
DESIGN_GATE PASS: NO DECLARADO
UNIT CLOSED: NO DECLARADO
IMPLEMENTATION PERFORMED: NO
IMPLEMENTATION AUTHORIZED: NO
DB QUERIED: NO
DATA MODIFIED: NO
DATA AUDIT AUTHORIZED: NO
MIGRATION AUTHORIZED: NO
CUTOVER: false
```

## 36. AUTHORITY GAP R1 — CORRECTIVE AMENDMENT

### 36.1 Historia, alcance y precedencia

El audit final registrado en
`auditoria/reviews/F2E-ADAPTERS-SNAPSHOT-DESIGN-REVIEW.md` conserva su resultado histórico
`PASS`. La preparación downstream de R1 demostró después que ese diseño no daba autoridad
suficiente para implementar mecánicamente el vocabulario de fallos, la identidad del catálogo SQL
ni los hashes de no-write. Esta sección corrige esa deuda sin reescribir el audit histórico.

Las decisiones 36.2–36.10 son normativas y sustituyen, sólo donde exista diferencia, cualquier
formulación anterior menos precisa de este mismo documento. Se mantienen sin cambios:

```text
projection-first
reader role SELECT-only
privileged setup/observer plane separado
StatementInspector durante la ventana medida
unknown SQL -> FAIL, incluido unknown SELECT
checksum limitado al slice
fixture aislado y quiescent para no-write
test concurrente separado del checksum
JPA/transaction/runtime isolation ya diseñados
pure detector core sin cambios
R1 draft y R1 implementation no autorizados por este amendment
```

Estado máximo de esta corrección:

```text
CORRECTIVE DESIGN AMENDMENT MATERIALIZED
READY FOR FRESH INDEPENDENT DESIGN DOCUMENT AUDIT
DESIGN_GATE del amendment: PENDING / NOT_PERFORMED
AUTHORITY_GAP CLOSED por auto-declaración: NO
R1: NOT_APPROVED / NOT_ACTIVE / NOT_IMPLEMENTED
```

### 36.2 Vocabulario normativo de fallos R1

El nombre canónico de invalidación del boundary de adapters es
`ADAPTER_INPUT_INVALID`. `INPUT_INVALID` no es alias ni valor permitido de
`ReservationReadFailureCode`; el vocabulario del detector puro conserva su independencia y no se
modifica. El mismo código R1 cubre dos stages que se distinguen en `safeContext`: input inválido
del caller antes de SQL y valor físico proyectado imposible de convertir después de SQL. No se crea
un quinto código sólo por el stage.

El enum futuro `ReservationReadFailureCode` contiene exactamente, para R1:

```text
ADAPTER_INPUT_INVALID
SOURCE_RECORD_NOT_FOUND
READ_SET_INVARIANT_VIOLATION
SOURCE_ACCESS_FAILURE
```

No contiene códigos de estados semánticos del detector. Los fallos de proxy/owner transaccional,
HostValidator, Docker, container, Flyway/setup, datasource startup o control de credenciales que
ocurren antes de entrar al read semántico son `PRE_SEMANTIC / ENVIRONMENT_FAILURE` y no se
convierten en `SOURCE_ACCESS_FAILURE`. `TRANSACTION_NOT_READ_ONLY`, SQL policy y
`NO_WRITE_GUARD_VIOLATION` pertenecen al owner/harness/gate operacional, no a
`ReservationReadFailureCode`.

| Code | Trigger | Layer/stage | SQL executed? | Batch behavior | Partial results? | Retry semantics | Cause | Safe context | Notes |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `ADAPTER_INPUT_INVALID` | `context`/colección/scope/scalar null; colección vacía o con null; ventana no bounded o invertida; UUID/scalar requerido inválido; o null/estado/tipo/rango físico proyectado imposible de mapear | validación public port; o projection mapper | NO para caller input; SÍ para row inválida | abort total de la operación | NONE | NON_RETRYABLE con mismo input/snapshot | null antes de SQL; cause original permitido para mapping | operación, scope seguro, counts; para row, ordinal/column name sin valor | Nunca se traduce a resultado del detector. |
| `SOURCE_RECORD_NOT_FOUND` | en `readByReservationIds`, `requestedIds - returnedIds` no vacío tras una query válida | validación de completitud post-query | SÍ | abort del batch completo | NONE | NON_RETRYABLE dentro del mismo snapshot | null | operación, IDs pedidos/faltantes ordenados, requested/returned count, statement ID | Es código propio. Cero filas de `readByScope` no lo dispara. |
| `READ_SET_INVARIANT_VIOLATION` | ID no pedido, ID duplicado, cardinalidad imposible o correlación interna contradictoria después de obtener rows válidas | assembly/completitud post-query | SÍ | abort total | NONE | NON_RETRYABLE con mismo snapshot | nullable; se preserva si existe | operación, IDs inesperados/duplicados, counts, statement ID | No se deduplica ni elige una fila. |
| `SOURCE_ACCESS_FAILURE` | `jakarta.persistence.PersistenceException`, `org.hibernate.HibernateException`, `java.sql.SQLException` o subtype en la cause chain durante create/bind/execute/materialize, siempre que la chain no contenga `F2eSqlPolicyViolationException`; también fallo del driver/conexión observable dentro del reader | adapter JPA/physical read | NO si falla create/bind; MAY_HAVE_STARTED si falla execute/materialize | abort total | NONE | CONDITIONAL_WHOLE_ATTEMPT_ONLY por policy externa; nunca retry interno ni parcial | non-null y preservada | operación, scope/counts, statement ID si ya se conoce, SQLState class/vendor code si existen | El código por sí solo no autoriza retry; SQL policy tiene precedencia y queda fuera de este code. |

Reglas de `readByReservationIds`:

```text
requested ID absent
-> SOURCE_RECORD_NOT_FOUND
-> SQL ya ejecutada
-> batch ABORTED
-> returned snapshots = NONE
-> retry interno = NO
```

### 36.3 Shape y cause policy de `ReservationReadException`

`ReservationReadException` es una `RuntimeException` adapter-local con shape conceptual exacto:

```text
ReservationReadException
  failureCode: ReservationReadFailureCode, required
  message: stable message selected only from failureCode, required
  safeContext: immutable Map<String,String>, required, never null
  cause: Throwable nullable according to the table above
```

Mensajes permitidos, sin interpolación:

```text
ADAPTER_INPUT_INVALID          -> "Reservation read input or projected row is invalid"
SOURCE_RECORD_NOT_FOUND        -> "One or more requested reservation records were not found"
READ_SET_INVARIANT_VIOLATION   -> "Reservation read-set invariant was violated"
SOURCE_ACCESS_FAILURE          -> "Reservation source access failed"
```

Keys exhaustivas de `safeContext`; toda key no listada se rechaza:

```text
operation = READ_BY_RESERVATION_IDS | READ_BY_SCOPE
projectionContractId
scopeKind = BY_RESERVATION_IDS | BY_SCOPE
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

Las listas UUID se serializan lower-case, ordenadas por sus 16 octetos unsigned y separadas por
coma; fechas usan `yyyy-MM-dd`; counts/ordinal/vendor code usan decimal ASCII. Se omite una key no
aplicable: no se inserta null. `sqlStateClass` contiene como máximo los primeros dos caracteres
ASCII de SQLState. `physicalColumn` contiene sólo el nombre catalogado, nunca su valor.

Prohibido en `message`, `safeContext`, output/report y logging normal: `cliente_id`, nombres,
correo/teléfono, credentials/URL, valores raw proyectados, bind values, SQL raw/canónica o
parámetros expandidos. Los UUID de reserva/salón y las fechas de scope son identificadores
técnicos permitidos por este contrato. La cause y su stack existen sólo para diagnóstico interno
restringido; nunca se serializan ni se copian a mensaje/context/report.

Wrapping normativo:

1. una `ReservationReadException` ya construida se relanza intacta;
2. validación deliberada del caller crea `ADAPTER_INPUT_INVALID` sin cause;
3. conversión/mapping deliberada de una row inválida crea `ADAPTER_INPUT_INVALID` y conserva como
   cause la excepción original si existe;
4. ausencia e invariantes esperables se crean con cause null, salvo que la invariante haya sido
   detectada por una excepción interna concreta, que se conserva;
5. si cualquier elemento de la cause chain es `PersistenceException`, `HibernateException` o
   `SQLException`, y la chain no contiene el fallo de policy definido a continuación, se envuelve
   una sola vez como `SOURCE_ACCESS_FAILURE` preservando como cause la excepción externa capturada
   y, con ella, toda la chain;
6. `READ_SET_INVARIANT_VIOLATION` se crea sólo en el punto de detección de uno de sus triggers
   exhaustivos; no existe un `catch RuntimeException` que lo use como fallback semántico;
7. cualquier otro `RuntimeException` no clasificado por estas reglas se propaga intacto como
   defecto operacional/de implementación y aborta el run; nunca se convierte por descarte en un
   `ReservationReadFailureCode`; `Error` tampoco se envuelve;
8. `IllegalTransactionStateException` lanzada por el proxy antes de entrar al método y fallos
   pre-semánticos del environment no se envuelven como `ReservationReadException`.

#### 36.3.1 Frontera normativa de SQL policy

El tipo operacional concreto es
`com.feelingpilates.transicion.programacion.adapter.jpa.policy.F2eSqlPolicyViolationException`,
una `RuntimeException` final de la policy layer. No hereda de `ReservationReadException`,
`PersistenceException` ni `HibernateException`, y no implementa ni expone
`ReservationReadFailureCode`. Se declara como boundary type del adapter para que production code
pueda preservar su identidad sin depender del source set de tests; no es domain vocabulary, no es
un bean y no crea reachability productiva. En R1, únicamente el
`F2eStatementPolicyInspector` test-only crea y lanza instancias durante el integration/no-write
gate.

La excepción contiene un `reason` obligatorio del vocabulario cerrado siguiente y, cuando pudo
calcularse sin revelar SQL, el `catalogStatementId` SHA-256; nunca contiene SQL raw/canónica, bind
values, credentials, URL ni datos proyectados:

```text
NORMALIZATION_REJECTED
CATALOG_MISS
STATEMENT_CLASS_DENIED
DENYLIST_VIOLATION
```

`unknown SQL` y `unknown SELECT` son ambos `CATALOG_MISS`; no se crea una categoría permisiva para
SELECT. El stage normativo dentro de `StatementInspector.inspect` es:

```text
SQL recibida por StatementInspector
-> F2E_SQL_CANON_V1
-> cálculo de F2E_SQL_CATALOG_ID_V1 cuando la normalización fue válida
-> validación statement-class + denylist + catálogo exhaustivo de 36.6
-> si falla cualquier control: lanzar F2eSqlPolicyViolationException y NO retornar la SQL
-> sólo si todos pasan: retornar la SQL a Hibernate/JDBC
```

Por contrato, lanzar antes de retornar impide preparar/ejecutar el statement rechazado. El
statement inválido no puede llegar a JDBC; statements válidos anteriores, si los hubiera en otra
operación futura, no cambian esa afirmación por-statement.

La frontera de `ReservaJpaReader` aplica esta precedencia sobre la excepción capturada y toda su
cause chain:

```text
1. ReservationReadException
   -> rethrow de la misma instancia
2. primera F2eSqlPolicyViolationException de la chain
   -> extraer y relanzar exactamente esa misma instancia
3. PersistenceException | HibernateException | SQLException en la chain
   -> ReservationReadException(SOURCE_ACCESS_FAILURE), una sola envoltura
4. cualquier otro RuntimeException
   -> rethrow de la misma instancia, sin reclasificación
```

La regla 2 precede a la 3 incluso si Hibernate envolviera el fallo del inspector en una excepción
de persistence. Así, SQL policy no puede convertirse en `SOURCE_ACCESS_FAILURE` ni en
`READ_SET_INVARIANT_VIOLATION`. La inspección de cause chain debe ser cycle-safe; extrae la
instancia concreta de policy, no fabrica otra y no copia el wrapper externo a su message/context.

Los triggers exhaustivos de `READ_SET_INVARIANT_VIOLATION` para R1 son únicamente:

1. `readByReservationIds` devuelve un `reservationId` fuera del set pedido;
2. `readByScope` devuelve una row válida cuyo `salonId` o `fecha` queda fuera del scope solicitado;
3. cualquiera de las dos operaciones observa más de una row física con el mismo
   `reservationId`;
4. después de mapear rows individualmente válidas, el número de snapshots ensamblados difiere del
   número de rows físicas válidas, o el ID de un snapshot no coincide con el ID de su row origen.

Cada trigger se comprueba explícitamente y crea `ReservationReadException` con
`READ_SET_INVARIANT_VIOLATION`. La ausencia de IDs pedidos tiene precedencia propia
`SOURCE_RECORD_NOT_FOUND`; row inválida usa `ADAPTER_INPUT_INVALID`; fallos JPA/driver usan
`SOURCE_ACCESS_FAILURE`; SQL policy, host/environment y transaction/no-write guards quedan fuera.
La lista es cerrada: una excepción inesperada no añade implícitamente un quinto trigger.

Tabla normativa de propagación:

| Failure origin | Concrete exception/type | ¿Caught by `ReservaJpaReader`? | ¿Mapped to `ReservationReadFailureCode`? | ¿Propagated unchanged? | ¿SQL executed? | Owner/gate |
| --- | --- | --- | --- | --- | --- | --- | --- |
| caller input o projected row inválida | `ReservationReadException(ADAPTER_INPUT_INVALID)` | NO catch; se crea deliberadamente | SÍ, `ADAPTER_INPUT_INVALID` | NO APLICA | NO para caller; SÍ para row inválida | R1 adapter/mapper contract |
| ID pedido ausente | `ReservationReadException(SOURCE_RECORD_NOT_FOUND)` | NO catch; se crea deliberadamente | SÍ, `SOURCE_RECORD_NOT_FOUND` | NO APLICA | SÍ | R1 completeness check |
| uno de los cuatro triggers exhaustivos del read set | `ReservationReadException(READ_SET_INVARIANT_VIOLATION)` | NO catch; se crea deliberadamente | SÍ, `READ_SET_INVARIANT_VIOLATION` | NO APLICA | SÍ | R1 assembly/invariant check |
| fallo físico JPA/Hibernate/JDBC sin policy failure en la chain | `PersistenceException`, `HibernateException` o chain con `SQLException` | SÍ | SÍ, `SOURCE_ACCESS_FAILURE` | NO; wrapper único preserva cause | NO o MAY_HAVE_STARTED según 36.2 | R1 physical-read boundary |
| normalization reject, catalog miss, denylist o statement-class violation | la misma instancia `F2eSqlPolicyViolationException` | SÍ, sólo para precedencia/extracción | NO | SÍ, misma instancia | NO para el statement rechazado | `F2eStatementPolicyInspector` / integration-no-write gate |
| proxy, `HostValidator`, Docker, container, Flyway/setup, datasource startup o credential preflight | `IllegalTransactionStateException` o `PRE_SEMANTIC_OPERATIONAL_FAILURE` del harness/environment | NO; ocurre antes/fuera del método semántico | NO | SÍ, por su owner operacional | NO | transaction owner / HostValidator / environment gate |

Los tests futuros R1 deben comprobar al menos: cada `reason` de policy; unknown SQL y unknown
SELECT como `CATALOG_MISS`; identidad exacta de la instancia directa y de la extraída de una chain
envolvente; ausencia de `ReservationReadException`; ausencia de
`READ_SET_INVARIANT_VIOLATION` y `SOURCE_ACCESS_FAILURE`; y evidencia JDBC de que el statement
rechazado no fue preparado ni ejecutado. También deben probar que los cuatro statements
catalogados sí atraviesan el inspector, para no obtener un guard que falle todo indiscriminadamente.

### 36.4 SQL canonicalization `F2E_SQL_CANON_V1`

Input exacto: la `String` que Hibernate entrega a `StatementInspector.inspect(String)` antes de
execution. Contiene SQL renderizada por Hibernate/dialect y bind markers, incluida la expansión de
un `IN`; no contiene valores bound y el normalizer nunca intenta reconstruirlos.

Algoritmo determinista sobre code points Java, emitiendo UTF-8:

1. rechazar input null, vacío, NUL, quote/dollar-quote sin cierre, comment opener fuera de quote
   o cualquier `;` fuera de quote;
2. reconocer regiones de single quote `'...'` con escape `''`, double quote `"..."` con escape
   `""`, y dollar quote PostgreSQL `$$...$$` o `$tag$...$tag$`, con tag
   `[A-Za-z_][A-Za-z0-9_]*`; preservar exactamente sus code points y emitirlos luego en UTF-8;
3. fuera de esas regiones, colapsar cada run no vacío de ASCII whitespace
   `U+0009..U+000D` o `U+0020` a un solo `U+0020`; eliminar el run inicial/final; whitespace
   Unicode distinto no se transforma;
4. preservar exactamente casing de keywords, identificadores no quoted, quoted identifiers y
   literals; no hay upper/lower folding ni Unicode NFC/NFD;
5. fuera de quotes, convertir cada marker `?`, `?` seguido de uno o más dígitos, o `$` seguido de
   uno o más dígitos a `?`; el texto quoted no cambia; `:name`, `@p1` y cualquier otro marker no
   observado en este stack se preservan y por ello no pueden coincidir con el catálogo R1;
6. después de esa tokenización, convertir todo paréntesis cuyo contenido completo sea uno o más
   markers separados sólo por comas/whitespace, por ejemplo `(?)`, `(?, ?, ?)` o `($1,$2)`, en
   `(?*)`; no se colapsa una lista con expresiones, literals u otros tokens;
7. serializar el resultado como UTF-8. No se elimina comment: `--`, `/*...*/`, Hibernate comment
   o hint causan rechazo. No se elimina semicolon: causa rechazo.

El esquema no es un parser SQL general. Es suficiente para las cuatro SQL R1 exactas de 36.6;
cualquier construcción distinta normaliza a otra identidad o falla normalización y, en ambos
casos, el inspector falla cerrado.

### 36.5 Framing e identidad del catálogo SQL

Para una SQL canónica `C`, sea `n` su longitud en bytes UTF-8, no chars/code points. La preimage
exacta es:

```text
UTF8("F2E_SQL_CATALOG_ID_V1\n")
|| ASCII(decimal(n), sin signo y sin leading zero salvo "0")
|| UTF8(":")
|| UTF8(C)
```

`catalogStatementId = lowercaseHex(SHA-256(preimage))`, exactamente 64 caracteres. El ID siempre
queda asociado a `F2E_SQL_CANON_V1`; cambiar normalización o framing exige otra versión y otros
IDs. No se hashea SQL raw ni values bound.

### 36.6 Catálogo exhaustivo permitido para R1

El `StatementInspector` de R1 permite exactamente estos cuatro IDs durante la ventana medida:

| Logical ID | Canonical SQL | Catalog statement ID |
| --- | --- | --- |
| `R1_RESERVA_BY_IDS_V1` | `SELECT r.id, r.estado, r.fecha, r.salon_id, r.instructor_id, r.tipo_actividad_id, r.hora_inicio, r.hora_fin, r.creado_en, r.actualizado_en FROM public.reserva r WHERE r.id IN (?*) ORDER BY r.id` | `dfa84c5db84f44c41adb82b0a58a2f080fc05a0612df15ababbd5dc064192a5b` |
| `R1_RESERVA_BY_SCOPE_V1` | `SELECT r.id, r.estado, r.fecha, r.salon_id, r.instructor_id, r.tipo_actividad_id, r.hora_inicio, r.hora_fin, r.creado_en, r.actualizado_en FROM public.reserva r WHERE r.salon_id IN (?*) AND r.fecha >= ? AND r.fecha <= ? ORDER BY r.id` | `c04cd40e2e3b991de845e73df65b6c9988be10c21cc1ee71474ab848b1e8eb9a` |
| `R1_TX_ISOLATION_V1` | `SELECT current_setting('transaction_isolation')` | `4a669a2f628e12468e0d532889e0bcafd38c09a5aadfb27f2f20f56159c1671e` |
| `R1_TX_READ_ONLY_V1` | `SELECT current_setting('transaction_read_only')` | `9963ea856cdf9bfb3e9c440b1c3a6c062fd375a906f465cbe7a7ac88878716c7` |

No se permite para R1 `pg_current_snapshot()`, `SHOW`, `SET`, metadata de schema, sequence SQL ni
otro statement. El harness ejecuta los dos probes mediante el mismo SessionFactory inspeccionado.
Preparación JDBC de isolation/read-only que no atraviese `StatementInspector` se verifica por los
probes y por el rol SELECT-only, pero no se finge como SQL observada. Flyway, fixture, creación del
rol, negative write control, checksum observer y cleanup usan conexiones separadas fuera de la
ventana/SessionFactory inspeccionada.

```text
normalization failure -> FAIL
normalized/hash not present in the four-entry catalog -> FAIL
unknown SQL -> FAIL
unknown SELECT -> FAIL
statement-class/denylist failure, aun con hash presente -> FAIL
```

Cada `FAIL` de este bloque lanza la misma clase operacional
`F2eSqlPolicyViolationException` con el `reason` correspondiente de 36.3.1 antes de retornar desde
`StatementInspector`; el statement rechazado no se prepara ni ejecuta y la excepción no se adapta
a `ReservationReadException`.

### 36.7 Golden vectors SQL

Cada `framed input` siguiente muestra exactamente los bytes UTF-8 antes de SHA-256; el salto tras
`V1` es un byte LF `0a`.

| Vector | Raw observed input | Canonical / framed input | Expected SHA-256 |
| --- | --- | --- | --- |
| SQL-A by IDs | `  SELECT\n r.id, r.estado, r.fecha, r.salon_id, r.instructor_id, r.tipo_actividad_id, r.hora_inicio, r.hora_fin, r.creado_en, r.actualizado_en\tFROM public.reserva r WHERE r.id IN (?, ?, ?) ORDER BY r.id  ` | canonical = catalog row `R1_RESERVA_BY_IDS_V1`; framed = `F2E_SQL_CATALOG_ID_V1\n193:` + canonical | `dfa84c5db84f44c41adb82b0a58a2f080fc05a0612df15ababbd5dc064192a5b` |
| SQL-B by scope | `SELECT r.id, r.estado, r.fecha, r.salon_id, r.instructor_id, r.tipo_actividad_id, r.hora_inicio, r.hora_fin, r.creado_en, r.actualizado_en FROM public.reserva r WHERE r.salon_id IN ($1,$2) AND r.fecha >= $3 AND r.fecha <= $4 ORDER BY r.id` | canonical = catalog row `R1_RESERVA_BY_SCOPE_V1`; framed = `F2E_SQL_CATALOG_ID_V1\n233:` + canonical | `c04cd40e2e3b991de845e73df65b6c9988be10c21cc1ee71474ab848b1e8eb9a` |
| SQL-C whitespace equivalent | `SELECT  r.id,\r\n\tr.estado, r.fecha, r.salon_id, r.instructor_id, r.tipo_actividad_id, r.hora_inicio, r.hora_fin, r.creado_en, r.actualizado_en FROM public.reserva r WHERE r.id IN (?1) ORDER BY r.id` | misma canonical/framed/hash de SQL-A | `dfa84c5db84f44c41adb82b0a58a2f080fc05a0612df15ababbd5dc064192a5b` |
| SQL-D unknown SELECT | `SELECT r.id FROM public.reserva r ORDER BY r.id` | `F2E_SQL_CATALOG_ID_V1\n47:SELECT r.id FROM public.reserva r ORDER BY r.id` | `8a4c3fcfd898385d6344056818c3f24628fb96f1acc1f2f9d327bb45371fec37` → catalog miss / FAIL |

### 36.8 Checksum canonicalization `F2E_CHECKSUM_*_V1`

#### Primitive framing

Para byte string `x`, `LP(x) = ASCII(byteLength(x)) || ":" || x`. Para una lista ordenada,
`SEQ(x1..xN) = ASCII(N) || ":" || LP(x1) || ... || LP(xN)`. N y longitudes son decimales sin
signo, sin whitespace ni leading zero salvo `0`; toda longitud mide bytes, y todo texto se codifica
UTF-8. `||` concatena bytes. Estas reglas eliminan colisiones de fronteras.

#### Scope, tabla, row y columns R1

R1 protege exactamente `public.reserva`. El row scope se congela antes del baseline:

```text
readByReservationIds -> rows cuyo id pertenece al set pedido
readByScope          -> rows cuyo salon_id pertenece al set y fecha está BETWEEN desde AND hasta
```

No se incluyen filas externas. Se leen las once columnas persistidas, incluida `cliente_id` para
demostrar no-mutación de la fila completa; ese UUID sólo entra al byte stream privado del hash y
nunca al snapshot, context, error o reporte:

```text
1 id                  U
2 salon_id            U
3 instructor_id       U
4 cliente_id          U
5 tipo_actividad_id   U
6 fecha               D
7 hora_inicio         T
8 hora_fin            T
9 estado              S
10 creado_en          Z
11 actualizado_en     Z
```

Este orden es fijo por contrato V15, no reflection ni orden incidental de `ResultSet`. Todas son
`NOT NULL` físicamente; el scheme define null para reutilización/verificación defensiva.

Tags y valores canónicos:

| Tag | Tipo | Canonical value bytes |
| --- | --- | --- |
| `U` | UUID | RFC-4122 textual lower-case `8-4-4-4-12` |
| `S` | enum/text | bytes raw devueltos por DB en UTF-8, sin trim, case fold ni NFC/NFD; `estado` usa string DB exacta |
| `D` | PostgreSQL `DATE` | `uuuu-MM-dd`, locale-free |
| `T` | PostgreSQL `TIME` | `HH:mm:ss.SSSSSS`, seis dígitos; nanosegundos no divisibles por 1000 son inválidos |
| `Z` | PostgreSQL `TIMESTAMPTZ` | normalizado a UTC, `uuuu-MM-dd'T'HH:mm:ss.SSSSSS'Z'`, seis dígitos; nunca timezone default |
| `B` | boolean | `true` o `false` ASCII |
| `I` | integer/short | decimal base 10 con `-` sólo si negativo, sin `+` ni padding |
| `Q` | exact numeric | plain decimal sin exponente; cero=`0`; sin trailing fractional zero ni decimal point final; negative zero=`0` |

`B`, `I` y `Q` son `NOT_APPLICABLE` al column set R1. No se admite float/double. Para cada field:

```text
non-null = SEQ("F2E_CHECKSUM_FIELD_V1", columnName, declaredTypeTag, "V", canonicalValue)
NULL     = SEQ("F2E_CHECKSUM_FIELD_V1", columnName, declaredTypeTag, "N", empty-byte-string)
```

Así NULL difiere de empty string, texto `"null"`, cero y false; el type tag permanece incluso en
NULL.

#### Row hash

`field1..fieldN` sigue el orden de columns anterior:

```text
rowPreimage = SEQ("F2E_CHECKSUM_ROW_V1", ASCII(N), field1, ..., fieldN)
rowHash = lowercaseHex(SHA-256(rowPreimage))
```

La table identity no entra al row hash: entra obligatoriamente en el nivel table. Rows se ordenan
por PK, para `reserva` por los 16 octetos UUID unsigned ascending; equivale al orden lexicográfico
de su forma canonical lower-case de longitud fija.

#### Table hash

```text
tableIdentity = "public.reserva"
tablePreimage = SEQ("F2E_CHECKSUM_TABLE_V1", tableIdentity, ASCII(rowCount),
                    rowHash1, ..., rowHashN)
tableHash = lowercaseHex(SHA-256(tablePreimage))
```

Los row hashes son sus 64 bytes ASCII lower-case y siguen PK order. Empty table:
`SEQ("F2E_CHECKSUM_TABLE_V1","public.reserva","0")`; no hay sentinel implícito.

#### Scope identity y slice hash

UUIDs se ordenan como arriba. La scope identity son bytes, no detector snapshot identity:

```text
IDs scope = SEQ("F2E_CHECKSUM_SCOPE_V1", "BY_RESERVATION_IDS", ASCII(idCount), id1..idN)
range scope = SEQ("F2E_CHECKSUM_SCOPE_V1", "BY_SCOPE", ASCII(salonCount),
                  salonId1..salonIdN, desde, hasta)
```

`desde/hasta` usan `D`. Para cada tabla:

```text
entry = SEQ("F2E_CHECKSUM_SLICE_TABLE_ENTRY_V1", tableIdentity, tableHash)
slicePreimage = SEQ("F2E_CHECKSUM_SLICE_V1", scopeIdentityBytes,
                    ASCII(tableCount), entry1, ..., entryN)
sliceHash = lowercaseHex(SHA-256(slicePreimage))
```

Entries se ordenan por bytes UTF-8 unsigned de `tableIdentity`; nunca por map iteration. R1 tiene
siempre una entry `public.reserva`, aun con cero rows. El empty-slice general de cero tablas es
la misma fórmula con `tableCount="0"` y cero entries. Los domains `FIELD`, `ROW`, `TABLE`,
`SCOPE`, `SLICE_TABLE_ENTRY` y `SLICE` separan niveles y evitan equivalencia accidental.

### 36.9 Golden vectors checksum/hashes

Las expresiones `SEQ`/`LP` son bytes exactos según 36.8. Los valores no mostrados como hex son
UTF-8 ASCII en estos vectores.

**A — una row R1.** Fields en orden:

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
rowPreimage=SEQ("F2E_CHECKSUM_ROW_V1","11",FIELD(id)...FIELD(actualizado_en))
rowHash=7f2438b5698a3d3ed9aec57674c64834da4aba252c08828768dbd148fa60006c
```

**B — fronteras de concatenación.** Con columns `left,right`, ambas `S`:

```text
ROW("ab","c") preimage =
4:19:F2E_CHECKSUM_ROW_V11:242:5:21:F2E_CHECKSUM_FIELD_V14:left1:S1:V2:ab42:5:21:F2E_CHECKSUM_FIELD_V15:right1:S1:V1:c
hash = f3a481c27a51936e546068c1d24bf8014fbcb6b03e9fc44a4004b49d1bea8c95

ROW("a","bc") preimage =
4:19:F2E_CHECKSUM_ROW_V11:241:5:21:F2E_CHECKSUM_FIELD_V14:left1:S1:V1:a43:5:21:F2E_CHECKSUM_FIELD_V15:right1:S1:V2:bc
hash = 7b3ae77aa7da771cc416c0863408ead4f483d21d35ba3a47d06155e05fe12ae1
```

**C — table con la row A.**

```text
preimage = 4:21:F2E_CHECKSUM_TABLE_V114:public.reserva1:164:7f2438b5698a3d3ed9aec57674c64834da4aba252c08828768dbd148fa60006c
tableHash = da4cad572663f11e0d5c8a4b6265729465089e13e940924f3958acac41e6ad1b
```

**D — misma row hash, distinta table identity.**

```text
public.reserva        -> da4cad572663f11e0d5c8a4b6265729465089e13e940924f3958acac41e6ad1b
audit.reserva_shadow  preimage =
4:21:F2E_CHECKSUM_TABLE_V120:audit.reserva_shadow1:164:7f2438b5698a3d3ed9aec57674c64834da4aba252c08828768dbd148fa60006c
audit tableHash = 71b3084caaf55b7ad11c58f3f513a98228e6bce0535466cc11f7d96be6ec2278
```

**E — normalización de orden de dos rows.** Row A0 difiere de A en
`id=...0000`, `fecha=2026-09-01`, `hora_inicio=07:00:00.000000` y
`hora_fin=08:00:00.000000`; los demás fields son iguales.

```text
A0 rowHash = ea5f926c634840690be3087662db5dab08f446323378b075037e6e59e318f3e0
input iteration [A,A0] or [A0,A]
canonical PK order [A0,A]
table preimage =
5:21:F2E_CHECKSUM_TABLE_V114:public.reserva1:264:ea5f926c634840690be3087662db5dab08f446323378b075037e6e59e318f3e064:7f2438b5698a3d3ed9aec57674c64834da4aba252c08828768dbd148fa60006c
tableHash = ee3953739cf5633cfe8869659be5b4d7075a7c74beb80be22dab90ae15eecedc
```

**F — slice R1 by IDs con table C.**

```text
scope = 4:21:F2E_CHECKSUM_SCOPE_V118:BY_RESERVATION_IDS1:136:00000000-0000-0000-0000-000000000001
slice preimage =
4:21:F2E_CHECKSUM_SLICE_V189:4:21:F2E_CHECKSUM_SCOPE_V118:BY_RESERVATION_IDS1:136:00000000-0000-0000-0000-0000000000011:1122:3:33:F2E_CHECKSUM_SLICE_TABLE_ENTRY_V114:public.reserva64:da4cad572663f11e0d5c8a4b6265729465089e13e940924f3958acac41e6ad1b
sliceHash = ff619ecac74d86a149cc9110c9ac205990fc5a87eb10e61ba64c5b1a6967f5be
```

**G — misma table hash, scopes distintos.**

```text
scope A = SEQ("F2E_CHECKSUM_SCOPE_V1","BY_SCOPE","1",
              "00000000-0000-0000-0000-000000000002","2026-09-01","2026-09-02")
sliceHash A = 6b8c91ac4bb06fed2476d190b93e37a8cd75c2485a66033801bf01676e1f0b79

scope B = SEQ("F2E_CHECKSUM_SCOPE_V1","BY_SCOPE","1",
              "00000000-0000-0000-0000-000000000002","2026-09-02","2026-09-02")
sliceHash B = 7b79a1f2403d6e63e9a012fdda6a4da39c422f5c559df2d5f38c61bd0c4b0db5
```

Vectores de empty:

```text
empty public.reserva preimage = 3:21:F2E_CHECKSUM_TABLE_V114:public.reserva1:0
empty public.reserva tableHash = 7cd08818f587c66f33716592705b5d716a19fd1ac434cb65e0defce21abeb916

empty zero-table slice para scope F
-> sliceHash = 775d6bf38eb09356818f772684ab50e4d0c3c3776e623c21f179a8f5368a8625
```

**H — orden multi-table autocontenido.** Todo string se codifica en UTF-8. En este vector,
`LP(x)=ASCII(byteLength(x)) || ":" || x` y
`SEQ(x1..xN)=ASCII(N) || ":" || LP(x1) || ... || LP(xN)`; counts y longitudes son ASCII decimal
sin whitespace ni leading zero. No se requiere ningún input implícito de otro vector.

```text
scopeType = BY_RESERVATION_IDS
scopeInputs.idCount = 1
scopeInputs.ids = [00000000-0000-0000-0000-000000000001]
scopeIdentity = SEQ("F2E_CHECKSUM_SCOPE_V1","BY_RESERVATION_IDS","1",
                    "00000000-0000-0000-0000-000000000001")
scopeIdentity bytes =
4:21:F2E_CHECKSUM_SCOPE_V118:BY_RESERVATION_IDS1:136:00000000-0000-0000-0000-000000000001

table public.reserva:
  tableIdentity = public.reserva
  tableHash = da4cad572663f11e0d5c8a4b6265729465089e13e940924f3958acac41e6ad1b
  entry = SEQ("F2E_CHECKSUM_SLICE_TABLE_ENTRY_V1","public.reserva",
              "da4cad572663f11e0d5c8a4b6265729465089e13e940924f3958acac41e6ad1b")
  entry bytes =
  3:33:F2E_CHECKSUM_SLICE_TABLE_ENTRY_V114:public.reserva64:da4cad572663f11e0d5c8a4b6265729465089e13e940924f3958acac41e6ad1b

table audit.reserva_shadow:
  tableIdentity = audit.reserva_shadow
  tableHash = 71b3084caaf55b7ad11c58f3f513a98228e6bce0535466cc11f7d96be6ec2278
  entry = SEQ("F2E_CHECKSUM_SLICE_TABLE_ENTRY_V1","audit.reserva_shadow",
              "71b3084caaf55b7ad11c58f3f513a98228e6bce0535466cc11f7d96be6ec2278")
  entry bytes =
  3:33:F2E_CHECKSUM_SLICE_TABLE_ENTRY_V120:audit.reserva_shadow64:71b3084caaf55b7ad11c58f3f513a98228e6bce0535466cc11f7d96be6ec2278

Input permutation A = [public.reserva, audit.reserva_shadow]
Input permutation B = [audit.reserva_shadow, public.reserva]
canonical UTF-8 unsigned table order for A and B =
  [audit.reserva_shadow, public.reserva]
tableCount = ASCII("2")

slicePreimage for A and B =
SEQ("F2E_CHECKSUM_SLICE_V1", scopeIdentityBytes, "2",
    SEQ("F2E_CHECKSUM_SLICE_TABLE_ENTRY_V1","audit.reserva_shadow",
        "71b3084caaf55b7ad11c58f3f513a98228e6bce0535466cc11f7d96be6ec2278"),
    SEQ("F2E_CHECKSUM_SLICE_TABLE_ENTRY_V1","public.reserva",
        "da4cad572663f11e0d5c8a4b6265729465089e13e940924f3958acac41e6ad1b"))

slicePreimage bytes for A and B =
5:21:F2E_CHECKSUM_SLICE_V189:4:21:F2E_CHECKSUM_SCOPE_V118:BY_RESERVATION_IDS1:136:00000000-0000-0000-0000-0000000000011:2128:3:33:F2E_CHECKSUM_SLICE_TABLE_ENTRY_V120:audit.reserva_shadow64:71b3084caaf55b7ad11c58f3f513a98228e6bce0535466cc11f7d96be6ec2278122:3:33:F2E_CHECKSUM_SLICE_TABLE_ENTRY_V114:public.reserva64:da4cad572663f11e0d5c8a4b6265729465089e13e940924f3958acac41e6ad1b

expected sliceHash for A and B =
aa10c3ce64e25734e671c9bc9e91555a714655036cf03b27425e88e6c67b99a7
```

Ambas permutations se ordenan antes de construir entries; por ello producen exactamente la misma
secuencia canónica, los mismos bytes de `slicePreimage` y el mismo SHA-256 esperado.

### 36.10 Decisiones AD del amendment y salida

No se renumeran AD-01–AD-32. Se agregan:

| ID | Question | Decision | Evidence | Alternatives rejected | Implications | Future owner/slice | Status |
| --- | --- | --- | --- | --- | --- | --- | --- |
| AD-33 | ¿Vocabulario de fallos R1? | cuatro codes exhaustivos de 36.2; invalid canónico=`ADAPTER_INPUT_INVALID`; missing tiene code propio | layering adapter/core, contrato R1 y catálogo operacional previo | alias `INPUT_INVALID`; mapear missing a semantic `MISSING`; catch-all source failure | exception/batch mecánicos, cero parciales | R1 | CLOSED_BY_THIS_AMENDMENT |
| AD-34 | ¿Exception/cause/safe context y frontera de SQL policy? | shape, mensajes y keys de 36.3; `F2eSqlPolicyViolationException` pertenece al policy/integration gate, precede source-failure classification, se extrae de la cause chain y se relanza como la misma instancia; no hay fallback `RuntimeException -> READ_SET_INVARIANT_VIOLATION` | debugging sin exponer PII/SQL/credentials; separación entre policy layer y failure vocabulary R1 | cause descartada; message de vendor; raw SQL/context libre; convertir policy a `ReservationReadException`; catch-all semántico | evidencia preservada internamente, output seguro y policy failure sin reclasificación | R1 | CLOSED_BY_THIS_AMENDMENT |
| AD-35 | ¿Normalización e identidad SQL? | `F2E_SQL_CANON_V1` + `F2E_SQL_CATALOG_ID_V1`; catálogo R1 de cuatro IDs | StatementInspector/Hibernate binding y unknown-SQL fail | trim/hash vago; wildcard SELECT; metadata abierta | allowlist exacta y reproducible | R1 | CLOSED_BY_THIS_AMENDMENT |
| AD-36 | ¿Checksum row/table/slice? | `F2E_CHECKSUM_FIELD_V1`, `F2E_CHECKSUM_ROW_V1`, `F2E_CHECKSUM_TABLE_V1`, `F2E_CHECKSUM_SCOPE_V1`, `F2E_CHECKSUM_SLICE_TABLE_ENTRY_V1` y `F2E_CHECKSUM_SLICE_V1`, LP/SEQ y all columns R1 | V15, no-write scope y necesidad de domain separation | concatenación, reflection/iteration order, hash sin table/scope | hashes reproducibles, empty definido | R1/shared testinfra | CLOSED_BY_THIS_AMENDMENT |
| AD-37 | ¿Golden vectors? | SQL-A–D y checksum A–H/empty de 36.7/36.9; H declara scope identity, ambas permutations, orden canónico, entries, preimage completa y hash | cálculo documental local SHA-256 sobre preimages declaradas | hashes sin input normativo o scope/orden inferidos | future tests pueden copiar expected values sin decidir ni inferir inputs | R1/shared testinfra | CLOSED_BY_THIS_AMENDMENT |

Trazabilidad del gap:

| Insuficiencia downstream | Decisión adoptada | Preservado sin cambio |
| --- | --- | --- |
| `INPUT_INVALID` vs `ADAPTER_INPUT_INVALID` | un único nombre adapter, sin alias | vocabulario/error semantics del pure core |
| requested record absent | `SOURCE_RECORD_NOT_FOUND`, abort batch, cero parciales | scope-zero rows sigue válido |
| source physical failure/cause | `SOURCE_ACCESS_FAILURE` y wrapping 36.3 | pre-semantic environment separado |
| SQL normalization/hash abierto | schemes versionados, cuatro statements exhaustivos | unknown SQL/SELECT siempre FAIL |
| checksum/hash hierarchy abierto | LP/SEQ, fields, row/table/scope/slice exactos | estrategia no-write y concurrencia separada |
| golden vectors ausentes | inputs/preimages y SHA-256 conocidos | no tests ni implementación en esta unidad |

```text
Open technical questions in corrective scope: NINGUNA
Human decision required: NO
Pure detector changed: NO
R1 draft changed: NO
R1 implementation: NOT_STARTED / NOT_AUTHORIZED
R1 downstream P1 allowlist: PENDING
R1 downstream P1 test-only JPA topology: PENDING
R2-R6: NOT_AUTHORIZED
Next required gate: FRESH_INDEPENDENT_DESIGN_DOCUMENT_AUDIT
```

## 37. RESIDUAL AUTHORITY GAP R1 — PROVENANCE + JPA TRANSACTION TOPOLOGY AMENDMENT

### 37.1 Alcance, precedencia y lifecycle

Esta enmienda materializa exclusivamente el target autorizado por
`HANDOFF-F2E-ADAPTERS-SNAPSHOT-DESIGN-AUTHORITY-GAP-R1-PROVENANCE-JPA-TX`.
Hace normativos, para R1, la identidad/provenance y el grafo test-only de transacción/recurso que
las secciones 13 y 18.1–18.2 dejaban insuficientemente determinados. En esos dos ámbitos, 37
prevalece sobre las fórmulas o wiring anteriores. No modifica la semántica del detector puro ni
sus identidades ya aprobadas.

Para evitar una lectura residual contradictoria, toda referencia anterior de este documento a
`MULTI_READER_MVCC`, `REPEATABLE_READ` o composición R6 es sólo diseño histórico/candidato de una
unidad futura y no es autoridad de claim, isolation, provenance ni statement observation para R1.
R1 queda cerrado exclusivamente a `SINGLE_READER_TEST + READ_COMMITTED + readOnly=true`. Asimismo,
para R1, cualquier referencia anterior a `sourceName`, `schemaFingerprint` o `fixtureIdentity`
aportados como literales se sustituye por la autoridad única del `DescriptorRecursoLector` de
37.4.1.

La enmienda 36.1–36.10 queda cerrada, histórica y preservada byte-for-byte en su materia: los
cuatro `ReservationReadFailureCode`, `ReservationReadException`, la frontera de
`F2eSqlPolicyViolationException`, `F2E_SQL_CANON_V1`, sus cuatro statements/IDs y golden vectors,
y toda la jerarquía `F2E_CHECKSUM_*_V1` con sus vectores. También permanecen congelados los dos
ports, queries, binding, ordering, cardinalidad, `historicalProgrammingTarget=Optional.empty()`,
`MANDATORY`, `REQUIRES_NEW`, `READ_COMMITTED`, `readOnly=true` y ownership transaccional R1.

```text
R1 handoff: MATERIALIZED / NOT_APPROVED / NOT_ACTIVE / UNCHANGED
R1 implementation: NOT_STARTED / NOT_AUTHORIZED
This design candidate: MATERIALIZED / NOT_SELF_APPROVED
Original handoff P1-2 identity/provenance candidate: MATERIALIZED / PENDING FRESH AUDIT
Original handoff P1-3 JPA topology candidate: MATERIALIZED / PRESERVED CLOSED
Residual audit corrections reader-resource/claim/uniqueness: MATERIALIZED / PENDING FRESH AUDIT
Original handoff P1-1 and P1-4: CLOSED / NOT_REOPENED
```

### 37.2 Gramática normativa `F2E_IDENTITY_V2`

`F2E_IDENTITY_V2` reutiliza exactamente las primitivas `LP` y `SEQ` aprobadas en 36.8, sin
modificarlas ni cambiar los domains de checksum. Esta reutilización sólo comparte la gramática de
framing; los domains `F2E-R1-*-V2` de esta sección son distintos de `F2E_CHECKSUM_*_V1`.

Para todo byte string `x` y secuencia ordenada `x1..xN`:

```text
LP(x) = ASCII(unsignedDecimal(byteLength(x))) || UTF8(":") || x
SEQ(x1..xN) = ASCII(unsignedDecimal(N)) || UTF8(":") || LP(x1) || ... || LP(xN)
ID_HASH_V2(x1..xN) = lowercaseHex(SHA-256(SEQ(x1..xN)))
```

`unsignedDecimal` no tiene signo, whitespace ni leading zero, salvo el valor cero escrito `0`.
Toda longitud cuenta bytes, nunca chars ni code points. SHA-256 es FIPS 180-4 sobre la preimage
exacta; el output son exactamente 64 caracteres ASCII hexadecimales `0-9a-f`, sin prefijo,
separador, newline ni padding. Una preimage ya enmarcada vuelve a recibir el `LP` exterior cuando
es argumento de otro `SEQ`; no se aplana ni se interpreta.

Scalars cerrados:

| Tipo conceptual | Bytes canónicos |
| --- | --- |
| texto, version, bean name, claim, enum | UTF-8 exacto; enum usa `name()`; no trim, case-fold ni normalización Unicode |
| UUID | RFC-4122 lower-case `8-4-4-4-12` |
| `ZoneId` | `ZoneId.getId()`, nunca zone default |
| `LocalDate` | `uuuu-MM-dd`, locale-free |
| `LocalTime` | `HH:mm:ss.SSSSSS`; valor sub-microsegundo es inválido |
| `OffsetDateTime` | UTC `uuuu-MM-dd'T'HH:mm:ss.SSSSSS'Z'`; nunca timezone default |
| boolean | ASCII `true` o `false` |
| integer/count/position | decimal ASCII como `unsignedDecimal`, salvo que el contrato declare signed |

Un texto requerido es non-null, contiene al menos un code point no-whitespace, no contiene NUL ni
un surrogate UTF-16 sin pareja y se codifica sin replacement. El whitespace interior y las formas
Unicode distintas permanecen distintas. Required empty y null fallan; no existe null implícito ni
omisión silenciosa. Un optional sólo usa los dos componentes literales `ABSENT` y empty bytes
cuando la fórmula lo declara; un valor presente usa `VALUE` y sus bytes, por lo que absence,
empty, el texto `null` y el texto `ABSENT` no colisionan.

Los UUID sets son non-null, sin nulls ni duplicados y se ordenan por los 16 octetos unsigned. El
input Java `Set` ya elimina duplicados; si una frontera de serialización entrega dos UUID iguales,
se rechaza en vez de deduplicar. Un mapa canónico se ordena por bytes UTF-8 unsigned de key, rechaza
key duplicada después de encoding y representa cada entry como
`SEQ("F2E-R1-NORMALIZED-FIELD-V2", key, value)` y el mapa como
`SEQ("F2E-R1-NORMALIZED-FIELDS-V2", ASCII(entryCount), entry1..entryN)`. La iteration order de
`Map` nunca es autoridad.

Todo valor no representable según estas reglas falla antes de crear una identidad y antes de
publicar cualquier snapshot. SHA-256 se usa bajo la asunción criptográfica estándar de resistencia
a colisiones, no como prueba matemática de inyectividad. Dentro de una invocación, si un digest ya
observado se asocia a otra preimage, se aborta todo con
`IllegalStateException("F2E identity hash collision detected")`; no se elige uno ni se agrega sal.
Cambiar grammar, domain, algoritmo, field set u orden exige otro version/domain y nunca reescribe
identidades ya emitidas.

Owner futuro exacto: `ReadSnapshotIdentifiers` implementa `LP`, `SEQ`, scalar codecs y las cuatro
fórmulas; `ReservaProjectionMapper` aporta únicamente valores tipados ya validados;
`ReservaJpaReader` coordina el cálculo/recompute por invocación. Tests implementan un recomputador
independiente; ningún test, map iteration, reflection o chat puede redefinir los bytes.

### 37.3 Catálogo canónico de proyección R1

La única fuente de verdad conceptual/Java para R1 es el enum cerrado anidado
`ReadSnapshotContext.ProjectionCatalogVersion`. En este slice contiene exactamente un valor:

```text
enum constant: R1_RESERVA_V1
projectionContractId: R1_RESERVA_PROJECTION
projectionContractVersion: V1
canonicalCatalogValue: R1_RESERVA_PROJECTION/V1
sourceSystem: LEGACY
sourceAtomType: RESERVA
physicalTable: public.reserva
data statement IDs:
  R1_RESERVA_BY_IDS_V1
  R1_RESERVA_BY_SCOPE_V1
mapper contract: ReservaProjectionMapper / V1
```

El enum expone esos literales como valores inmutables; el caller no puede construir un string de
versión arbitrario. `ReservaProjectionQueryExecutor`, `ReservaProjectionMapper`,
`ReadSnapshotIdentifiers` y provenance aceptan sólo el mismo enum constant. Un ID/version/query o
mapper distinto es catalog drift y hace fallar construcción/context startup antes de SQL. No hay
alias, version fallback, valor `UNKNOWN` ni negociación.

La proyección SQL y el record `ReservaProjectionRow` conservan este orden físico exacto:

| Posición | Campo lógico | Columna | Tipo canónico | Source fingerprint | Snapshot/provenance |
| --- | --- | --- | --- | --- | --- |
| 1 | `reservationId` | `id` | `UUID` | incluido | incluido |
| 2 | `state` | `estado` | `TEXT` exacto DB, luego enum conocido | incluido | incluido |
| 3 | `date` | `fecha` | `DATE` | incluido | incluido |
| 4 | `salonId` | `salon_id` | `UUID` | incluido | incluido |
| 5 | `instructorId` | `instructor_id` | `UUID` | incluido | incluido |
| 6 | `activityId` | `tipo_actividad_id` | `UUID` | incluido | incluido |
| 7 | `start` | `hora_inicio` | `TIME_MICROS` | incluido | incluido |
| 8 | `end` | `hora_fin` | `TIME_MICROS` | incluido | incluido |
| 9 | `createdAtTechnical` | `creado_en` | `TIMESTAMP_UTC_MICROS` | incluido | incluido como técnico |
| 10 | `updatedAtTechnical` | `actualizado_en` | `TIMESTAMP_UTC_MICROS` | incluido | incluido como técnico |
| 11 | `historicalProgrammingTarget` | ninguna | `OPTIONAL_HISTORICAL_TARGET` | `ABSENT` explícito | `Optional.empty()` |

Las primeras diez posiciones son required/non-null. Los timestamps participan porque un cambio
físico observable de la row debe cambiar el fingerprint, pero nunca se renombran ni interpretan
como vigencia funcional. La posición 11 vincula el fingerprint con la afirmación normativa de
ausencia; se representa con presence `ABSENT` y value empty, nunca con null o un target inferido.

Cada campo se enmarca exactamente:

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

`cliente_id` es PII y queda excluido de query, row, projection, identity, provenance y error. Sólo
permanece en el checksum privado de 36.8 para prueba de no-mutación. También quedan excluidos SQL
raw/canónico, binds, credentials/URL, connection metadata, current clock, random values,
candidates, `TurnoInstructor`, masters no proyectados y cualquier validez histórica inferida. El
logical statement usado no entra al fingerprint de una row: ambas queries autorizadas pertenecen
al mismo catálogo V1 y una row idéntica tiene el mismo source fingerprint.

### 37.4 Contexto, scope y evidencia de transacción/statement

#### 37.4.1 Autoridad única del recurso lector

La autoridad R1 es exactamente una instancia inmutable test-only llamada
`DescriptorRecursoLector`, propiedad exclusiva de `F2ePostgresTestConfiguration`. Se materializa
como tipo anidado dentro de ese único archivo testinfra ya allowlisted; no crea otro bean ni otro
path. Su constructor no es público, el callback/caller no lo recibe y ningún `ReadSnapshotContext`
puede construirlo, sustituirlo o modificarlo.

El descriptor conserva exactamente:

```text
sourceName
identidadFuenteDatos              componente antes denominado fixtureIdentity
schemaFingerprint
jdbcUrlCanonicaSinCredenciales
databaseName
schemaName                        exactamente public para R1
credentialPrincipal               login efímero SELECT-only, nunca password
identity reference: f2eReaderDataSource
identity reference: f2eReaderEntityManagerFactory / PU f2eReaderPersistenceUnit
identity reference: f2eReaderTransactionManager
identity reference: f2eReaderEntityManager shared proxy
```

`sourceName`, `identidadFuenteDatos` y `schemaFingerprint` no son parámetros de
`inSingleStatementReadOnly`, del callback ni del port. `F2ePostgresTestConfiguration` los crea una
sola vez después de que el bootstrap privilegiado haya aplicado/verificado Flyway, haya calculado
la huella del schema realmente instalado y haya creado el login lector. Para una fixture R1, el
catálogo de la configuración posee una key ASCII non-blank única `K` y deriva sin libertad:

```text
identidadFuenteDatos = "fixture-" || K
sourceName = "fixture:postgres16:" || K
schemaFingerprint = resultado inmutable del bootstrap Flyway+schema ejecutado sobre
                    la misma instancia PostgreSQL identificada por endpoint/database/schema
```

El catálogo de fixtures, no el test method ni el caller, asigna `K`. El descriptor almacena el
resultado del bootstrap junto con las referencias exactas de los beans construidos con ese mismo
endpoint/database/schema/principal. No acepta un fingerprint esperado como sustituto del resultado
observado. El password, URL con credentials y valores de conexión sensibles nunca entran al
descriptor serializable, a identidades ni a provenance.

La configuración inyecta en `ReservaJpaReader` los tres valores confiables
`sourceName/identidadFuenteDatos/schemaFingerprint` desde esa misma instancia y el reader exige
igualdad exacta con el context creado por el harness. El harness usa directamente
`identidadFuenteDatos`, no una copia aportada por el callback, para calcular
`snapshotEvidenceId`.

Después de que el advisor abra la transacción y antes de abrir la capture o ejecutar el primer
probe/SELECT, el harness valida fail-closed:

1. por identidad Java, que sus instancias DS/EMF/TM/shared-EM son exactamente las del descriptor;
2. que el shared EM está joined a la transacción del TM/EMF descritos;
3. mediante la `Connection` obtenida sólo por el mismo `Session.doReturningWork`, que
   `DatabaseMetaData.getURL()` sin user-info/password/query, `Connection.getCatalog()`,
   `Connection.getSchema()` y `DatabaseMetaData.getUserName()` coinciden byte-for-byte con
   `jdbcUrlCanonicaSinCredenciales`, `databaseName`, `public` y `credentialPrincipal`;
4. que endpoint/database/schema de esa conexión son los de la misma instancia sobre la que el
   bootstrap produjo `schemaFingerprint`; y
5. que los valores confiables inyectados al reader y el context interno coinciden con el mismo
   descriptor.

La URL canónica permitida tiene exactamente la forma
`jdbc:postgresql://<host-lowercase-ascii>:<decimal-port>/<databaseName>`; query, fragment,
user-info, password, percent-encoding y aliases alternativos se rechazan. `databaseName`,
`schemaName` y `credentialPrincipal` son los strings exactos observados, sin trim/case-fold. Esta
metadata se usa sólo para validación local; no se publica ni participa en los cuatro golden
vectors.

Cualquier mismatch lanza antes de probes/SQL
`IllegalStateException("F2E reader resource provenance not proven")`, cierra sin aceptar
`snapshotEvidenceId`, no emite ninguna identidad/snapshot y no se reclasifica como
`ReservationReadFailureCode`. Rehashing de labels no satisface este control.

Acceptance criteria futuros obligatorios, sin implementación en esta unidad:

```text
descriptor + configured reader + active session exactos -> ACCEPT
caller intenta otro sourceName -> FAIL antes de probes/SQL; zero identities/output
caller intenta otro schemaFingerprint -> FAIL antes de probes/SQL; zero identities/output
active connection/session resuelve otro endpoint/database/schema/principal -> FAIL cerrado
descriptor/DS/EMF/TM/shared-EM no son las mismas instancias -> FAIL cerrado
successful rows + probes + provenance -> mismo DescriptorRecursoLector validado
```

El shape R1 corregido de `ReadSnapshotContext` es exactamente, en este orden:

| Campo | Tipo | Regla/owner |
| --- | --- | --- |
| `runIdentity` | `String` | required; owner del run |
| `attemptIdentity` | `String` | required; cambia para cada retry completo; owner del run |
| `readerInvocationIdentity` | `String` | required y único dentro de `(runIdentity,attemptIdentity)`; owner transaccional |
| `sourceName` | `String` | required; copiado sólo del `DescriptorRecursoLector`; caller override prohibido |
| `schemaFingerprint` | `String` | required; copiado sólo del `DescriptorRecursoLector`; caller override prohibido |
| `projectionCatalogVersion` | `ProjectionCatalogVersion` | exactamente `R1_RESERVA_V1`; no string libre |
| `ruleCatalogVersion` | `String` | required versión de reglas F2D; owner del run |
| `businessZone` | `ZoneId` | required explícita |
| `snapshotClaim` | `SnapshotClaim` | exactamente `SINGLE_READER_TEST`; único constant soportado por R1 |
| `snapshotEvidenceId` | 64-char lower hex | creado por el harness con el descriptor validado; nunca caller-supplied |
| `statementObservationFingerprint` | 64-char lower hex | commitment del harness tras probes, verificado contra manifest final |

Se elimina `scopeCanonical` como input del caller. El port recibe su scope tipado existente y
`ReservaJpaReader`, antes de crear la query, deriva una sola vez los bytes internos:

```text
readByReservationIds:
  scopeBytes = SEQ("F2E-R1-READ-SCOPE-V2", "READ_BY_RESERVATION_IDS",
                   ASCII(idCount), sortedReservationIds...)

readByScope:
  scopeBytes = SEQ("F2E-R1-READ-SCOPE-V2", "READ_BY_SCOPE",
                   ASCII(salonCount), sortedSalonIds..., desde, hasta)

scopeCanonical = UTF8-decode-strict(scopeBytes)
```

Todos los componentes actuales son ASCII, por lo que decode/encode es byte-identical. Los mismos
`scopeBytes` se reutilizan, sin reserialización, en execution ID, logical ID y business context.
El caller no puede suministrar un segundo scope representativo. Scope inválido o incapaz de
canonicalizarse conserva `ADAPTER_INPUT_INVALID`, ocurre antes de SQL y produce cero parciales.

R1 acepta exclusivamente `SINGLE_READER_TEST`. El enum R1 `SnapshotClaim` contiene exactamente
ese único constant; `MULTI_READER_MVCC` no es alias, fallback ni valor tolerado. Si una frontera de
deserialización, reflexión o integración intenta introducir `MULTI_READER_MVCC` u otro claim, el
harness/reader rechaza `IllegalArgumentException("Unsupported R1 snapshot claim")` antes de
validar evidencia, abrir capture o ejecutar probes/SQL, con cero identidades/output.

Después de validar el recurso de 37.4.1, el harness crea exactamente:

```text
snapshotEvidenceId = ID_HASH_V2(
  "F2E-R1-SINGLE-READER-TEST-EVIDENCE-V2",
  descriptor.identidadFuenteDatos,
  transactionBoundaryIdentity,
  "f2eReaderTransactionManager",
  "f2eReaderPersistenceUnit",
  "read committed",
  "read only")
```

`transactionBoundaryIdentity` identifica la invocación proxied del harness, no una row ni un
intento completo, y se rige por 37.7.1. Esta evidencia no pretende ser
`pg_current_snapshot()` ni demostrar simultaneidad entre readers. R1 no acepta, calcula ni
propaga `MULTI_READER_MVCC`, `repeatable read` o un probe PostgreSQL de snapshot. Una futura fase
multi-reader/R6 deberá recibir otro handoff, contrato de provenance, contrato de statement
observation y fresh audit; nada de esta enmienda la autoriza.

Después de los dos probes y antes del callback, el harness ya conoce sus valores/IDs y el único
data statement permitido para la operación solicitada. Calcula el commitment:

```text
statementObservationFingerprint = ID_HASH_V2(
  "F2E-R1-STATEMENT-OBSERVATIONS-V2",
  snapshotEvidenceId,
  observedIsolationValue,
  observedAccessMode,
  ASCII(statementCount),
  committedCatalogStatementId1, ..., committedCatalogStatementIdN)
```

Para R1 el orden y count comprometidos son isolation probe observado, read-only probe observado y
exactamente uno de los dos data statements esperado; `N=3`, isolation=`read committed`, access
mode=`read only`. Ese fingerprint entra al context antes de invocar el reader. Después del callback
el harness recalcula la misma fórmula con los tres IDs realmente capturados y exige igualdad exacta
antes de que el resultado escape. El inspector conserva IDs del catálogo, nunca SQL/binds. Orden,
count, claim, valores de probes o data statement distintos fallan antes de publicar snapshots.

### 37.5 Las cuatro identidades

#### 37.5.1 `executionProvenanceId`

Representa exactamente una invocación material del port R1 dentro de una transacción competente.
Todas las rows de esa invocación comparten el valor y ninguna otra invocación puede reutilizar su
`readerInvocationIdentity` dentro del attempt.

```text
executionProvenanceId = ID_HASH_V2(
  "F2E-R1-EXECUTION-PROVENANCE-V2",
  runIdentity,
  attemptIdentity,
  readerInvocationIdentity,
  operation,
  sourceName,
  schemaFingerprint,
  projectionCatalogVersion.canonicalCatalogValue,
  ruleCatalogVersion,
  businessZone.getId(),
  scopeBytes,
  snapshotClaim.name(),
  snapshotEvidenceId,
  statementObservationFingerprint)
```

Cambian el ID: run, retry/attempt, invocación, operación, source/schema/catalog/rules/zone/scope,
claim/evidencia, resultados de probes o manifest comprometido/verificado de statements. No
participan valores de rows,
reservation identity, clock, random, thread ID, Java object identity, Connection metadata, SQL ni
binds. Dos ejecuciones materialmente distintas deben tener distinto
`readerInvocationIdentity`; reutilizarlo aborta antes de probes/SQL. El fingerprint de statements
vincula la identidad a la evidencia del inspector, pero no sustituye la prueba topológica de
same-resource de 37.9–37.12.

#### 37.5.2 `logicalSnapshotId`

Representa la equivalence class de observaciones que comparten la misma fuente/schema/catalog,
reglas/zone/scope tipado y la misma evidencia competente de snapshot transaccional.

```text
logicalSnapshotId = ID_HASH_V2(
  "F2E-R1-LOGICAL-SNAPSHOT-V2",
  sourceName,
  schemaFingerprint,
  projectionCatalogVersion.canonicalCatalogValue,
  ruleCatalogVersion,
  businessZone.getId(),
  scopeBytes,
  snapshotClaim.name(),
  snapshotEvidenceId)
```

`runIdentity`, `attemptIdentity`, `readerInvocationIdentity`, operation mechanics, statement
manifest y `executionProvenanceId` están excluidos. Por ello no se declara equivalencia lógica por
mera igualdad de attempt. Una nueva transacción normalmente tiene otro `snapshotEvidenceId` y por
tanto otro logical ID; dos ejecuciones sólo lo comparten si el owner competente demuestra y
reutiliza exactamente la misma boundary/snapshot evidence y scope. Concatenar hashes de lecturas
en transacciones/connections distintas nunca crea esa evidencia.

#### 37.5.3 `sourceFingerprint`

Representa la row R1 observable y la afirmación de historical target ausente bajo una versión
exacta de source/schema/projection. Se calcula por row después de validar todos sus scalars:

```text
sourceFingerprint = ID_HASH_V2(
  "F2E-R1-SOURCE-FINGERPRINT-V2",
  sourceName,
  schemaFingerprint,
  projectionCatalogVersion.canonicalCatalogValue,
  "LEGACY",
  "RESERVA",
  reservationId,
  canonicalSourceProjectionBytes)
```

Cambian el fingerprint cualquier source/schema/catalog/source identity o cualquiera de las once
posiciones canónicas, incluidos timestamps o `ABSENT`. No participan ejecución, scope, snapshot,
query elegida, rules ni zone: el mismo source atom observado bajo el mismo catálogo conserva su
fingerprint aunque se alcance desde otra query/scope. La relación exacta de source metadata y
campos está cerrada en 37.3.

#### 37.5.4 `snapshotIdentity`

Identifica el payload concreto `ReservationSourceSnapshot` de una ejecución, excluyendo sólo su
propio campo autorreferente. Une logical snapshot, execution evidence y exact source atom:

```text
snapshotIdentity = ID_HASH_V2(
  "F2E-R1-SNAPSHOT-IDENTITY-V2",
  logicalSnapshotId,
  executionProvenanceId,
  projectionCatalogVersion.canonicalCatalogValue,
  "LEGACY",
  "RESERVA",
  reservationId,
  sourceFingerprint)
```

Así, dos invocaciones distintas pueden referirse a la misma logical snapshot y source content,
pero sus Java snapshots tienen distinta identidad porque su provenance de ejecución difiere. El
ID no redefine identidad semántica del detector puro. `state/date/salon/instructor/activity`,
intervalo, technical timestamps y historical absence quedan vinculados mediante
`sourceFingerprint`; claim/scope/boundary mediante `logicalSnapshotId`; run/attempt/invocation y
statements mediante `executionProvenanceId`.

### 37.6 Shape exacto de `EvidenceProvenance`

R1 usa sin cambiar el record core existente de siete campos. Para cada row:

```text
sourceName = context.sourceName
schemaFingerprint = context.schemaFingerprint
recordIds = immutable List.of(canonical reservationId)
ruleId = "R1_RESERVA_PROJECTION"
ruleVersion = "V1"
businessTimeContext = UTF8-decode-strict(SEQ(
  "F2E-R1-BUSINESS-CONTEXT-V2", businessZone.getId(), scopeBytes))
normalizedFields = immutable map con exactamente las keys de la tabla siguiente
```

| Key | Value canónico |
| --- | --- |
| `activityId` | UUID de field 6 |
| `attemptIdentity` | context exacto |
| `businessZone` | `ZoneId.getId()` |
| `createdAtTechnical` | field 9 UTC micros |
| `date` | field 3 DATE |
| `end` | field 8 TIME_MICROS |
| `executionProvenanceId` | output 37.5.1 |
| `historicalProgrammingTarget` | literal `ABSENT` |
| `instructorId` | UUID de field 5 |
| `logicalSnapshotId` | output 37.5.2 |
| `operation` | `READ_BY_RESERVATION_IDS` o `READ_BY_SCOPE` |
| `projectionCatalogVersion` | `R1_RESERVA_PROJECTION/V1` |
| `projectionContractId` | `R1_RESERVA_PROJECTION` |
| `projectionContractVersion` | `V1` |
| `readerInvocationIdentity` | context exacto |
| `reservationId` | UUID de field 1; igual a único `recordIds` |
| `ruleCatalogVersion` | context exacto |
| `runIdentity` | context exacto |
| `salonId` | UUID de field 4 |
| `scopeCanonical` | decode exacto de los mismos `scopeBytes` |
| `snapshotClaim` | enum `name()` |
| `snapshotEvidenceId` | context exacto |
| `snapshotIdentity` | output 37.5.4 |
| `sourceAtomType` | literal `RESERVA` |
| `sourceFingerprint` | output 37.5.3 |
| `sourceSystem` | literal `LEGACY` |
| `start` | field 7 TIME_MICROS |
| `state` | field 2 / known enum `name()` idéntico al DB value válido |
| `statementObservationFingerprint` | output 37.4 |
| `transactionAccessMode` | literal observado `read only` |
| `transactionIsolation` | literal observado `read committed` |
| `updatedAtTechnical` | field 10 UTC micros |

El map tiene exactamente 32 entries, keys/values non-null y keys non-blank. Su iteration order no
es normativo; para recompute/audit se aplica el orden unsigned UTF-8 y framing de map de 37.2. El
snapshot `additionalObservableFields` contiene exactamente `createdAtTechnical` y
`updatedAtTechnical` con los mismos bytes de projection/provenance. `recordIds` tiene exactamente
una identidad porque provenance es por snapshot/row; el conjunto ordenado de todos los
`recordIds` debe ser exactamente el de rows físicas publicadas.

Quedan explícitamente excluidos `cliente_id`, nombres/correo/teléfono, SQL raw/canónica, binds,
credentials/URL, role password, backend PID, transaction ID, Connection metadata/object IDs,
thread IDs, arbitrary caller maps, raw invalid values y target histórico inferido. Ninguna key
adicional es permitida. El core `EvidenceProvenance.semanticHash()` existente no sustituye ni
redefine ninguna identidad F2E de esta sección.

### 37.7 Construcción, recompute, cross-consistency e inmutabilidad

#### 37.7.1 Lifecycle exacto de unicidad R1

R1 no declara unicidad global, durable ni cross-restart. El namespace normativo es exactamente la
vida de una instancia de `ApplicationContext` test-only que contiene un único
`readerTransactionTestHarness` y un único `DescriptorRecursoLector`. Nace al inicializar ese
contexto y termina al cerrarlo. Sus garantías sobreviven method completion y transaction
completion, pero no sobreviven el cierre del `ApplicationContext`, el fin/restart del test process
o JVM ni un application restart. Un proceso/contexto nuevo crea deliberadamente otro namespace y
puede reutilizar los mismos strings sin violar R1. Como R1 no registra beans productivos, no existe
una garantía productiva de application-restart. Ninguna implementación conforme puede ampliar o
reducir este límite silenciosamente.

El único registry es estado privado del bean singleton `readerTransactionTestHarness`, con nombre
conceptual `RegistroUnicidadIdentidades`; no es otro bean, archivo, DB, filesystem, static global
ni estado del caller. El caller sólo propone `runIdentity`, `attemptIdentity`,
`transactionBoundaryIdentity` y `readerInvocationIdentity`; nunca declara que estén libres ni
modifica el registry.

El registry compara los bytes completos de estas keys, no sólo su digest. Los cuatro namespaces
son independientes por domain y usan exactamente:

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

Antes de validar recurso/probes/SQL, una única sección crítica del harness ejecuta una operación
atómica all-or-none:

1. crea `runKey=OPEN` si no existe; si existe, sólo `OPEN_AFTER_ABORT` puede admitir otro
   `attemptIdentity` y nunca mientras tenga un attempt `ACTIVE`/`UNKNOWN`;
2. exige que `attemptKey`, `boundaryKey` e `invocationKey` no existan;
3. inserta las tres como `ACTIVE` y liga el attempt activo al run; y
4. ante cualquier colisión no inserta ninguna key nueva y lanza exactamente
   `IllegalStateException("F2E execution provenance identity reuse")` antes de probes/SQL.

Lock/synchronization o una estructura concurrente son detalle interno sólo si preservan esa única
transición linealizable: dos invocaciones competidoras no pueden observar éxito para la misma key
y no se permite check-then-put separado.

Lifecycle terminal exacto:

```text
sin reserva completada
  -> no existe marker; los mismos inputs pueden volver a intentarse

reserva ACTIVE + resultado validado justo antes de escapar del harness
  -> attempt/boundary/invocation = CONSUMED_SUCCESS
  -> run = COMPLETED_SUCCESS
  -> ninguna key del run admite reuse ni un nuevo attempt

reserva ACTIVE + fallo antes de evidence, durante probes/read/recompute,
rollback o fallo antes de que un resultado escape
  -> attempt/boundary/invocation = CONSUMED_ABORTED
  -> run = OPEN_AFTER_ABORT
  -> esas tres keys nunca se reutilizan; sólo un attemptIdentity nuevo puede reintentar el run

fallo después de construir identidades pero antes de publicarlas
  -> igual que CONSUMED_ABORTED; ninguna identity key se libera

fallo/interrupción después de que el resultado haya escapado o después de marcar éxito
  -> CONSUMED_SUCCESS / COMPLETED_SUCCESS; reuse y retry quedan prohibidos

interrupción/crash sin poder ejecutar transición terminal dentro del mismo contexto
  -> ACTIVE pasa lógicamente a UNKNOWN o permanece ACTIVE; bloquea reuse y otro attempt del run
     hasta cerrar ese ApplicationContext
```

El registry no participa en la transacción DB: rollback/commit no borra markers. “Liberar” significa
únicamente soltar el mutex/lock de la sección crítica o retirar la condición `ACTIVE`; nunca borrar
`CONSUMED_SUCCESS`, `CONSUMED_ABORTED`, `UNKNOWN`, las keys ni el historial del run dentro del
namespace. El registry per-invocation de preimages usado para detectar colisión SHA-256 es distinto:
se destruye al terminar la invocación y no otorga ni revoca unicidad de run/attempt/boundary.

#### 37.7.2 Orden de construcción y consistencia

El orden obligatorio de una invocación es:

1. ya dentro del target proxied del harness, validar el seed/context, el scope tipado y que el claim
   sea exactamente `SINGLE_READER_TEST`; un claim distinto termina en el stage de 37.4/37.12 sin
   reservar keys;
2. reservar atómicamente las keys del namespace acotado de 37.7.1, resolver el único catalog enum
   y derivar una sola instancia de `scopeBytes`;
3. entrar por el harness/TM/EM topology de 37.9, validar el `DescriptorRecursoLector` contra la
   misma Session/Connection, crear `snapshotEvidenceId` sólo después de éxito y abrir capture;
4. ejecutar los dos probes por el reader EM, validar valores y calcular el commitment
   `statementObservationFingerprint` con el data statement exacto esperado;
5. construir el context completo e invocar exactamente una data query R1 por el mismo
   EM/session/resource;
6. validar/mapear todas las rows, calcular cada source fingerprint, los IDs execution/logical y
   cada snapshot identity; crear provenance y outputs inmutables;
7. al volver el callback, cerrar/capturar el manifest real y exigir que su recompute sea igual al
   commitment ya usado por los snapshots;
8. recomputar las cuatro fórmulas desde los objetos finales, comparar en constant-time los bytes
   de digest, verificar cross-record y entregar sólo si todo coincide;
9. aplicar la transición terminal de 37.7.1, cerrar el persistence context/transacción y soltar
   sólo el lock activo sin borrar ningún marker consumido/unknown.

Invariantes mecánicas:

```text
typed port scope == sole internally-derived scopeBytes
context catalog enum == query catalog == mapper V1 == fingerprint catalog == provenance catalog
row reservationId == snapshot.reservationId == provenance.recordIds[0]
row field1..11 == source fingerprint preimage == snapshot observable payload/provenance values
all rows in one call share executionProvenanceId and logicalSnapshotId
each row has its own sourceFingerprint and snapshotIdentity
statementObservationFingerprint == exact manifest seen by the same reader SessionFactory
snapshotEvidenceId == exact harness transaction boundary/resource contract
```

El reader no acepta del caller `scopeCanonical`, ninguno de los cuatro IDs, normalized fields ni
catalog strings. `statementObservationFingerprint` sólo llega dentro del context construido por el
harness/owner competente y nunca desde el test callback o caller del port. Toda lista/map/output es
defensive immutable y ningún valor cambia después de return. Cualquier mismatch interno,
cross-record o recompute descarta el batch completo y propaga sin reclasificar
`IllegalStateException("F2E identity/provenance consistency not proven")`; esto usa la regla
residual RuntimeException ya aprobada y no agrega un `ReservationReadFailureCode` ni un quinto
trigger de `READ_SET_INVARIANT_VIOLATION`.

### 37.8 Golden vectors byte-exactos `F2E_IDENTITY_V2`

Estos cuatro outputs fueron calculados sobre bytes ASCII/UTF-8 exactos y recomputados de forma
independiente con Python `hashlib.sha256` y Node `crypto.createHash('sha256')`; ambas
implementaciones produjeron los mismos valores. Las líneas `preimage=` contienen todos los bytes
previos a SHA-256, sin comillas ni newline final.

En los cuatro vectores, `K=r1-a`: por ello `sourceName=fixture:postgres16:r1-a`,
`identidadFuenteDatos=fixture-r1-a` y el `schemaFingerprint` mostrado son outputs confiables del
mismo `DescriptorRecursoLector` de 37.4.1, no literales que el caller pueda aportar. Esta
aclaración de ownership no modifica ningún byte, preimage, longitud ni hash de I-A–I-D.

#### Vector I-A — `executionProvenanceId`

Inputs directos completos:

```text
runIdentity=run-2026-09-13-001
attemptIdentity=attempt-01
readerInvocationIdentity=reader-invocation-0001
operation=READ_BY_RESERVATION_IDS
sourceName=fixture:postgres16:r1-a
schemaFingerprint=sha256:aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa
projectionCatalogVersion=R1_RESERVA_PROJECTION/V1
ruleCatalogVersion=F2D-RULE-CATALOG/V1
businessZone=America/Mexico_City
scopeBytes=4:20:F2E-R1-READ-SCOPE-V223:READ_BY_RESERVATION_IDS1:136:00000000-0000-4000-8000-000000000001
snapshotClaim=SINGLE_READER_TEST
```

Inputs y preimages completos de los dos componentes calculados:

```text
snapshotEvidence preimage=7:37:F2E-R1-SINGLE-READER-TEST-EVIDENCE-V212:fixture-r1-a16:tx-boundary-000127:f2eReaderTransactionManager24:f2eReaderPersistenceUnit14:read committed9:read only
snapshotEvidenceId=f195883f1312824ab4cc5e6ff5a865a54fa7e1fb45dafe6cc49f5865a8952877

observedIsolationValue=read committed
observedAccessMode=read only
statementCount=3
observed IDs, ordered:
4a669a2f628e12468e0d532889e0bcafd38c09a5aadfb27f2f20f56159c1671e
9963ea856cdf9bfb3e9c440b1c3a6c062fd375a906f465cbe7a7ac88878716c7
dfa84c5db84f44c41adb82b0a58a2f080fc05a0612df15ababbd5dc064192a5b
statementObservation preimage=8:32:F2E-R1-STATEMENT-OBSERVATIONS-V264:f195883f1312824ab4cc5e6ff5a865a54fa7e1fb45dafe6cc49f5865a895287714:read committed9:read only1:364:4a669a2f628e12468e0d532889e0bcafd38c09a5aadfb27f2f20f56159c1671e64:9963ea856cdf9bfb3e9c440b1c3a6c062fd375a906f465cbe7a7ac88878716c764:dfa84c5db84f44c41adb82b0a58a2f080fc05a0612df15ababbd5dc064192a5b
statementObservationFingerprint=7f7e27a90a3efff6803bf4a8e40dbe4377d8bf1a754187cb14502f47af135838
```

Canonical hash preimage y output:

```text
preimage=14:30:F2E-R1-EXECUTION-PROVENANCE-V218:run-2026-09-13-00110:attempt-0122:reader-invocation-000123:READ_BY_RESERVATION_IDS23:fixture:postgres16:r1-a71:sha256:aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa24:R1_RESERVA_PROJECTION/V119:F2D-RULE-CATALOG/V119:America/Mexico_City93:4:20:F2E-R1-READ-SCOPE-V223:READ_BY_RESERVATION_IDS1:136:00000000-0000-4000-8000-00000000000118:SINGLE_READER_TEST64:f195883f1312824ab4cc5e6ff5a865a54fa7e1fb45dafe6cc49f5865a895287764:7f7e27a90a3efff6803bf4a8e40dbe4377d8bf1a754187cb14502f47af135838
SHA-256 lower-hex=b081c98ef4b71b8395051b3a9be319cc7d149c9306109828a0fbcb94a7c70795
```

#### Vector I-B — `logicalSnapshotId`

Inputs directos completos:

```text
sourceName=fixture:postgres16:r1-a
schemaFingerprint=sha256:aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa
projectionCatalogVersion=R1_RESERVA_PROJECTION/V1
ruleCatalogVersion=F2D-RULE-CATALOG/V1
businessZone=America/Mexico_City
scopeBytes=4:20:F2E-R1-READ-SCOPE-V223:READ_BY_RESERVATION_IDS1:136:00000000-0000-4000-8000-000000000001
snapshotClaim=SINGLE_READER_TEST
snapshotEvidence inputs=(fixture-r1-a,tx-boundary-0001,f2eReaderTransactionManager,f2eReaderPersistenceUnit,read committed,read only)
snapshotEvidence preimage=7:37:F2E-R1-SINGLE-READER-TEST-EVIDENCE-V212:fixture-r1-a16:tx-boundary-000127:f2eReaderTransactionManager24:f2eReaderPersistenceUnit14:read committed9:read only
snapshotEvidenceId=f195883f1312824ab4cc5e6ff5a865a54fa7e1fb45dafe6cc49f5865a8952877
```

```text
preimage=9:26:F2E-R1-LOGICAL-SNAPSHOT-V223:fixture:postgres16:r1-a71:sha256:aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa24:R1_RESERVA_PROJECTION/V119:F2D-RULE-CATALOG/V119:America/Mexico_City93:4:20:F2E-R1-READ-SCOPE-V223:READ_BY_RESERVATION_IDS1:136:00000000-0000-4000-8000-00000000000118:SINGLE_READER_TEST64:f195883f1312824ab4cc5e6ff5a865a54fa7e1fb45dafe6cc49f5865a8952877
SHA-256 lower-hex=d5b95d6a6c3b9feeee9c2cefdf93a1229f065b783e2db7984b6c6957234b8e88
```

#### Vector I-C — `sourceFingerprint`

Inputs directos completos:

```text
sourceName=fixture:postgres16:r1-a
schemaFingerprint=sha256:aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa
projectionCatalogVersion=R1_RESERVA_PROJECTION/V1
sourceSystem=LEGACY
sourceAtomType=RESERVA
reservationId=00000000-0000-4000-8000-000000000001
fields ordered:
1 reservationId/id/UUID/VALUE/00000000-0000-4000-8000-000000000001
2 state/estado/TEXT/VALUE/CONFIRMADA
3 date/fecha/DATE/VALUE/2026-09-15
4 salonId/salon_id/UUID/VALUE/00000000-0000-4000-8000-000000000002
5 instructorId/instructor_id/UUID/VALUE/00000000-0000-4000-8000-000000000003
6 activityId/tipo_actividad_id/UUID/VALUE/00000000-0000-4000-8000-000000000004
7 start/hora_inicio/TIME_MICROS/VALUE/08:30:00.000000
8 end/hora_fin/TIME_MICROS/VALUE/09:30:00.000000
9 createdAtTechnical/creado_en/TIMESTAMP_UTC_MICROS/VALUE/2026-09-01T14:00:00.000000Z
10 updatedAtTechnical/actualizado_en/TIMESTAMP_UTC_MICROS/VALUE/2026-09-02T15:30:00.000000Z
11 historicalProgrammingTarget/NONE/OPTIONAL_HISTORICAL_TARGET/ABSENT/<empty bytes>
```

Canonical projection bytes completos (1210 bytes):

```text
15:30:F2E-R1-CANONICAL-PROJECTION-V221:R1_RESERVA_PROJECTION2:V12:11102:7:22:F2E-R1-SOURCE-FIELD-V21:113:reservationId2:id4:UUID5:VALUE36:00000000-0000-4000-8000-00000000000171:7:22:F2E-R1-SOURCE-FIELD-V21:25:state6:estado4:TEXT5:VALUE10:CONFIRMADA69:7:22:F2E-R1-SOURCE-FIELD-V21:34:date5:fecha4:DATE5:VALUE10:2026-09-15101:7:22:F2E-R1-SOURCE-FIELD-V21:47:salonId8:salon_id4:UUID5:VALUE36:00000000-0000-4000-8000-000000000002113:7:22:F2E-R1-SOURCE-FIELD-V21:512:instructorId13:instructor_id4:UUID5:VALUE36:00000000-0000-4000-8000-000000000003115:7:22:F2E-R1-SOURCE-FIELD-V21:610:activityId17:tipo_actividad_id4:UUID5:VALUE36:00000000-0000-4000-8000-00000000000490:7:22:F2E-R1-SOURCE-FIELD-V21:75:start11:hora_inicio11:TIME_MICROS5:VALUE15:08:30:00.00000084:7:22:F2E-R1-SOURCE-FIELD-V21:83:end8:hora_fin11:TIME_MICROS5:VALUE15:09:30:00.000000122:7:22:F2E-R1-SOURCE-FIELD-V21:918:createdAtTechnical9:creado_en20:TIMESTAMP_UTC_MICROS5:VALUE27:2026-09-01T14:00:00.000000Z129:7:22:F2E-R1-SOURCE-FIELD-V22:1018:updatedAtTechnical14:actualizado_en20:TIMESTAMP_UTC_MICROS5:VALUE27:2026-09-02T15:30:00.000000Z106:7:22:F2E-R1-SOURCE-FIELD-V22:1127:historicalProgrammingTarget4:NONE26:OPTIONAL_HISTORICAL_TARGET6:ABSENT0:
```

```text
preimage=8:28:F2E-R1-SOURCE-FINGERPRINT-V223:fixture:postgres16:r1-a71:sha256:aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa24:R1_RESERVA_PROJECTION/V16:LEGACY7:RESERVA36:00000000-0000-4000-8000-0000000000011210:15:30:F2E-R1-CANONICAL-PROJECTION-V221:R1_RESERVA_PROJECTION2:V12:11102:7:22:F2E-R1-SOURCE-FIELD-V21:113:reservationId2:id4:UUID5:VALUE36:00000000-0000-4000-8000-00000000000171:7:22:F2E-R1-SOURCE-FIELD-V21:25:state6:estado4:TEXT5:VALUE10:CONFIRMADA69:7:22:F2E-R1-SOURCE-FIELD-V21:34:date5:fecha4:DATE5:VALUE10:2026-09-15101:7:22:F2E-R1-SOURCE-FIELD-V21:47:salonId8:salon_id4:UUID5:VALUE36:00000000-0000-4000-8000-000000000002113:7:22:F2E-R1-SOURCE-FIELD-V21:512:instructorId13:instructor_id4:UUID5:VALUE36:00000000-0000-4000-8000-000000000003115:7:22:F2E-R1-SOURCE-FIELD-V21:610:activityId17:tipo_actividad_id4:UUID5:VALUE36:00000000-0000-4000-8000-00000000000490:7:22:F2E-R1-SOURCE-FIELD-V21:75:start11:hora_inicio11:TIME_MICROS5:VALUE15:08:30:00.00000084:7:22:F2E-R1-SOURCE-FIELD-V21:83:end8:hora_fin11:TIME_MICROS5:VALUE15:09:30:00.000000122:7:22:F2E-R1-SOURCE-FIELD-V21:918:createdAtTechnical9:creado_en20:TIMESTAMP_UTC_MICROS5:VALUE27:2026-09-01T14:00:00.000000Z129:7:22:F2E-R1-SOURCE-FIELD-V22:1018:updatedAtTechnical14:actualizado_en20:TIMESTAMP_UTC_MICROS5:VALUE27:2026-09-02T15:30:00.000000Z106:7:22:F2E-R1-SOURCE-FIELD-V22:1127:historicalProgrammingTarget4:NONE26:OPTIONAL_HISTORICAL_TARGET6:ABSENT0:
SHA-256 lower-hex=e98cb3c5c325bae4fd7a8541121e690d0e27e6d9fd81ab756add5ca0f9e99e3e
```

#### Vector I-D — `snapshotIdentity`

Inputs directos completos:

```text
logicalSnapshotId=d5b95d6a6c3b9feeee9c2cefdf93a1229f065b783e2db7984b6c6957234b8e88
executionProvenanceId=b081c98ef4b71b8395051b3a9be319cc7d149c9306109828a0fbcb94a7c70795
projectionCatalogVersion=R1_RESERVA_PROJECTION/V1
sourceSystem=LEGACY
sourceAtomType=RESERVA
reservationId=00000000-0000-4000-8000-000000000001
sourceFingerprint=e98cb3c5c325bae4fd7a8541121e690d0e27e6d9fd81ab756add5ca0f9e99e3e
```

Los tres hashes input se obtienen de las preimages completas I-A, I-B e I-C y se vuelven a
tratar como 64 bytes ASCII lower-case, no como 32 bytes decoded.

```text
preimage=8:27:F2E-R1-SNAPSHOT-IDENTITY-V264:d5b95d6a6c3b9feeee9c2cefdf93a1229f065b783e2db7984b6c6957234b8e8864:b081c98ef4b71b8395051b3a9be319cc7d149c9306109828a0fbcb94a7c7079524:R1_RESERVA_PROJECTION/V16:LEGACY7:RESERVA36:00000000-0000-4000-8000-00000000000164:e98cb3c5c325bae4fd7a8541121e690d0e27e6d9fd81ab756add5ca0f9e99e3e
SHA-256 lower-hex=5c27b72c6f6d7f183eec18d4e3d7b6383c9418f789b96485f892252c6a019d13
```

### 37.9 Topología JPA test-only exacta

#### 37.9.1 Nombres, construcción y qualifiers

`F2ePostgresTestConfiguration`, importada explícitamente sólo por tests R1, crea exactamente el
reader plane con estos nombres. Dentro de la misma configuración crea además el único valor
inmutable no-bean `DescriptorRecursoLector` de 37.4.1, después del bootstrap y del armado de los
recursos reader; conserva en él las identity references exactas y lo entrega directamente al
harness/reader al construirlos, sin exponerlo al test/callback ni registrarlo como bean:

| Bean/resource name | Tipo/construcción normativa | Consumer exacto |
| --- | --- | --- |
| `f2eReaderDataSource` | DataSource nuevo con endpoint DB del container y sólo login efímero SELECT-only | sólo reader EMF y reader TM |
| `f2eStatementPolicyInspector` | singleton `F2eStatementPolicyInspector` | sólo property del reader EMF y harness capture |
| `f2eReaderEntityManagerFactory` | `LocalContainerEntityManagerFactoryBean`, PU `f2eReaderPersistenceUnit`, sólo reader DS, inspector exacto | reader EM proxy/TM |
| `f2eReaderEntityManager` | `SharedEntityManagerCreator.createSharedEntityManager(f2eReaderEntityManagerFactory)` | executor y harness probes |
| `f2eReaderTransactionManager` | `JpaTransactionManager(f2eReaderEntityManagerFactory)` con el mismo reader DS fijado explícitamente | harness advisor y reader advisor |
| `readerTransactionTestHarness` | bean distinto/proxied | tests R1 |
| `reservaProjectionQueryExecutor` | plain bean con `@Qualifier("f2eReaderEntityManager")` | reader |
| `reservaProjectionMapper` | plain bean | reader |
| `reservaJpaReader` | plain main class registrado explícitamente como bean test-only/proxied | callback del harness |

La privileged plane usa `f2ePrivilegedDataSource` sólo para container/Flyway/fixtures/grants,
negative write/checksum/cleanup. R1 no crea privileged EntityManagerFactory ni privileged
transaction manager. Si el contexto parent tiene defaults/autoconfigurados, no son candidatos:
cada factory method parameter e injection point anterior lleva el qualifier exacto. Ningún bean es
`@Primary`; no hay `TransactionManagementConfigurer`, type/default fallback, `SET ROLE`, routing
DataSource ni `AbstractRoutingDataSource`.

El reader EMF fija
`hibernate.session_factory.statement_inspector=f2eStatementPolicyInspector` con la instancia, no
un classname que pueda crear otra. Sólo ese EMF puede producir el shared EM. El TM se construye con
ese EMF y se le fija exactamente `f2eReaderDataSource`; startup falla si el EMF reporta otro DS,
PU name, inspector o resource. Executor/harness no reciben `DataSource`, `EntityManagerFactory` ni
otro EM. El harness sí recibe las identity references ya selladas dentro del descriptor para
compararlas, no como rutas alternativas de acceso; el reader recibe sólo sus tres labels
confiables y el mismo descriptor no-publicable requerido por 37.4.1.

#### 37.9.2 Selección de transaction manager y coupling main/test

Cada uno de los dos métodos públicos de `ReservaJpaReader` declara exactamente:

```java
@Transactional(
    transactionManager = "f2eReaderTransactionManager",
    propagation = Propagation.MANDATORY,
    readOnly = true)
```

Se autoriza que esa main class plain nombre el manager test-only en R1. Es un string de wiring
estable, compila sin el bean y no activa nada: la clase no tiene stereotype, production bean,
component scan, configuration, property ni consumer. Sin `reservaJpaReader` bean no existe advisor
ejecutable. Un contexto que intente registrar el reader sin el manager exacto falla al resolver el
advisor/primera invocación y no cae al manager default o privilegiado.

La futura composición R6, sólo si recibe autoridad separada, deberá registrar un manager llamado
exactamente `f2eReaderTransactionManager` respaldado por la misma resource plane que su
coordinator. No se autoriza R6 aquí ni un rename/fallback. Este bean-name contract mantiene
`MANDATORY` y evita que R1 deba adivinar entre test y production managers.

El método público exacto `readerTransactionTestHarness.inSingleStatementReadOnly(...)` declara:

```java
@Transactional(
    transactionManager = "f2eReaderTransactionManager",
    propagation = Propagation.REQUIRES_NEW,
    isolation = Isolation.READ_COMMITTED,
    readOnly = true)
```

El advisor del harness abre/cierra la única transacción R1. El advisor separado del reader resuelve
el mismo manager por nombre, verifica `MANDATORY` y participa; no abre, suspende, eleva isolation
ni reintenta. Un nested/new transaction dentro del callback, reader, executor, mapper o probes está
prohibido.

#### 37.9.3 EntityManager, persistence context y physical Connection

`f2eReaderEntityManager` es un shared transaction-aware proxy. Durante la invocación delega al
único EntityManager/Session que `JpaTransactionManager` liga al thread para
`f2eReaderEntityManagerFactory`; fuera de esa transacción no puede ejecutar R1. Executor y harness
reciben por identity el mismo bean proxy. `EntityManagerFactory.createEntityManager`, otro shared
EM, `@PersistenceContext` sin `unitName`, default EM y privileged EM están prohibidos.

El persistence context nace con el begin del TM y muere con su completion. Después de abrir, el
harness exige `EntityManager.isJoinedToTransaction()`, Hibernate `Session` default-read-only y
flush mode `MANUAL`; cualquier mismatch aborta antes del primer probe. R1 sólo materializa scalars
y records, no entities managed. `persist`, `merge`, `remove`, `flush`, dirty managed state,
`clear`, `detach` y output lazy/proxy están prohibidos; no se usa cleanup para ocultar writes.

La única conexión del reader se obtiene por el EMF/TM desde `f2eReaderDataSource`. Para evidencia
topológica, el harness puede usar exclusivamente
`f2eReaderEntityManager.unwrap(Session.class).doReturningWork(...)` antes de probes y después del
callback. El primer `doReturningWork`, antes de abrir capture, ejecuta exclusivamente la validación
local de `DatabaseMetaData`/catalog/schema/principal de 37.4.1 y conserva internamente la referencia
de esa `Connection`; no prepara ni ejecuta SQL. El segundo, después del callback, sólo compara por
referencia la `Connection`, sin consultar metadata ni ejecutar SQL. Ninguno publica PID, URL,
credentials u object identity. `DataSource.getConnection()`, `DriverManager`, `JdbcTemplate`,
otro `doWork`, un JDBC connection suministrado por caller y connection independiente están
prohibidos dentro de la ventana y nunca prueban same snapshot.

### 37.10 Probes, inspector y SELECT-only/no-write guarantee

El call path real y único es:

```text
test
-> readerTransactionTestHarness Spring proxy
-> TransactionInterceptor("f2eReaderTransactionManager") REQUIRES_NEW/RC/readOnly
-> harness target + transaction-bound f2eReaderEntityManager
-> validate DescriptorRecursoLector against same Session/Connection (zero SQL)
-> open inspector capture(readerInvocationIdentity, snapshotEvidenceId)
-> probe isolation through that EntityManager
-> probe read-only through that EntityManager
-> callback
-> reservaJpaReader Spring proxy
-> TransactionInterceptor("f2eReaderTransactionManager") MANDATORY/readOnly (joins)
-> reader target -> executor -> same shared EntityManager delegate/Session
-> one cataloged data query
-> mapper/identity/provenance
-> callback returns
-> harness verifies statement manifest + connection sameness + cross-consistency
-> harness target returns
-> outer interceptor completes/closes transaction and persistence context
```

No self-invocation cuenta: test obtiene el harness bean proxy y el callback invoca el reader bean
proxy distinto. Ninguna prueba transaccional usa `new ReaderTransactionTestHarness` o
`new ReservaJpaReader`; un test arquitectónico falla si falta alguno de ambos advisors o si el
advisor resuelve otro manager. La prueba negativa llama al reader proxy fuera del harness y exige
`IllegalTransactionStateException` antes de query/mapper.

Los probes autorizados son únicamente las dos native queries de 36.6 ejecutadas con
`f2eReaderEntityManager`:

```text
R1_TX_ISOLATION_V1 -> SELECT current_setting('transaction_isolation') -> read committed
R1_TX_READ_ONLY_V1 -> SELECT current_setting('transaction_read_only') -> on/read only
```

Se ejecutan una vez y antes de la data query. No se autorizan `DataSource.getConnection`, JDBC
privilegiado/default, otro EntityManager/SessionFactory, `SHOW`, `SET`, ningún statement SQL de
metadata/schema, `pg_current_snapshot()` o probe adicional R1. La lectura local de
`DatabaseMetaData`/catalog/schema/principal autorizada en 37.4.1 ocurre antes de la capture por la
misma Session/Connection, no es un statement catalogado ni una consulta SQL. El resultado `on` se
canonicaliza a `read only` sólo para IDs/provenance; cualquier otro value falla.

El singleton `f2eStatementPolicyInspector` está registrado en el mismo reader SessionFactory. El
harness abre una capture ThreadLocal no anidable después del begin y antes del primer probe; cada
`inspect` valida 36.3.1/36.4–36.6 y registra `(capture identity, ordered catalog ID)` antes de
retornar SQL. Tras la data query el harness exige tres IDs exactos en orden y cierra la capture aun
al fallar. Reuse, nested capture, missing/extra/out-of-order ID, thread change, call sin capture o
capture no cerrada aborta la operación. Tests concurrentes usan ThreadLocal separado y una
invocation identity única.

El inspector prueba que los tres statements atravesaron ese SessionFactory y que ningún statement
fuera del catálogo fue retornado. No ve bind values, transaction begin/commit, Connection identity,
DB grants ni prueba por sí solo same physical connection o ausencia de writes por otra resource;
esas propiedades provienen del grafo/binding, hard role fence y checksum.

No existe DataSource SQL proxy adicional: la cadena exacta de enforcement es (1) advisors Spring
que imponen una sola TX read-only, (2) shared EM proxy ligado al único reader EMF, (3) inspector
fail-closed antes de JDBC y (4) hard PostgreSQL role fence en toda physical connection del reader
DS. El login efímero sólo tiene `CONNECT`, `USAGE public` y `SELECT public.reserva`; sin CREATE,
DML, sequence, function application privilege ni `SET ROLE`. Un negative INSERT mediante una
transacción reader-role separada y fuera de la capture debe ser denegado; checksum/count
privilegiado before/after debe ser idéntico. Spring `readOnly=true` y statistics sólo corroboran,
no sustituyen inspector+role+checksum.

No hay hidden bypass: reader, executor, probes y harness carecen de referencia a privileged/default
DS/EMF/EM/TM; el único path SQL reachable es shared EM -> reader EMF -> reader DS -> SELECT-only
credential. Bootstrap, Flyway, fixtures, role lifecycle, negative control, checksum y cleanup usan
la privileged plane fuera de la ventana y nunca se mezclan en snapshot/provenance.

### 37.11 Grafo normativo y paths prohibidos

```text
                           TEST-ONLY READER PLANE

 test
   |
   v
 [readerTransactionTestHarness proxy]
   | TransactionInterceptor: explicit f2eReaderTransactionManager
   v
 [one REQUIRES_NEW / READ_COMMITTED / readOnly transaction]
   | binds one EntityManager/Session to f2eReaderEntityManagerFactory
   v
 [validate DescriptorRecursoLector on same Session/Connection; zero SQL]
   +----------------------------+-------------------------------+
   |                            |                               |
   v                            v                               v
 [isolation probe]          [read-only probe]          [reservaJpaReader proxy]
   |                            |                        explicit same TM / MANDATORY
   +----------------------------+-------------------------------+
                                |
                                v
                    [f2eReaderEntityManager shared proxy]
                                |
                                v
                 [same transaction-bound EntityManager/Session]
                                |
          f2eStatementPolicyInspector sees probes + one data query
                                |
                                v
                 [f2eReaderEntityManagerFactory / PU]
                                |
                                v
                    [f2eReaderDataSource only]
                                |
                                v
              [same transaction-bound physical Connection]
                                |
                                v
          [PostgreSQL ephemeral SELECT-only login / hard DB fence]

 PRIVILEGED PLANE (outside window only)
 [f2ePrivilegedDataSource] -> bootstrap/Flyway/fixture/grant/checksum/cleanup
                 -/-> harness, reader, executor, probes, reader EMF/TM

 FORBIDDEN
 reader/probe -X-> default or privileged TM/EM/EMF/DataSource
 reader/probe -X-> dataSource.getConnection()/DriverManager/JdbcTemplate
 reader/executor -X-> createEntityManager()/second Session/REQUIRES_NEW
 probe -X-> another StatementInspector/SessionFactory/connection
```

### 37.12 Fail-closed matrix

| Material condition | Detection/evidence | Required future behavior | Semantic mapping |
| --- | --- | --- | --- |
| unsupported/foreign snapshot claim | closed R1 enum plus boundary validation | `IllegalArgumentException("Unsupported R1 snapshot claim")` before registry reservation, resource validation, capture, probes or SQL; zero identity/output | no `ReservationReadFailureCode` |
| no active reader transaction | reader proxy `MANDATORY` | `IllegalTransactionStateException`, zero SQL/output | no `ReservationReadFailureCode` |
| another TM active | explicit reader advisor finds no resource for named reader TM | same deterministic `IllegalTransactionStateException`; never join wrong TM | none |
| manager missing/renamed/incompatible | bean/advisor context validation | context/first invocation fails before probe/query; no default fallback | pre-semantic topology failure |
| descriptor/resource label or identity mismatch | exact checks of 37.4.1 against reader DS/EMF/TM/shared EM and same Session/Connection | `IllegalStateException("F2E reader resource provenance not proven")` before capture/probes/SQL; no evidence, identity or output | unchanged RuntimeException |
| EM bean not shared/not transaction-bound | bean identity + `isJoinedToTransaction` + Session checks | abort before probe | `SNAPSHOT_CONSISTENCY_NOT_PROVEN` operational |
| second EM/EMF or wrong PU | architecture bean graph and runtime delegate identity | context/test FAIL; no candidate accepted | operational/topology |
| independent/unexpected Connection | before/after Session connection reference differs, forbidden API scan | abort/discard all; connection evidence cannot be substituted | operational/topology |
| probe escaped snapshot | expected EM bean/delegate, capture and ordered manifest absent | abort/discard all | `SNAPSHOT_CONSISTENCY_NOT_PROVEN` |
| inspector missing/wrong SessionFactory/capture | EMF property instance identity or manifest mismatch | context/operation FAIL before output | SQL/no-write policy layer |
| noncatalog SQL or write/DDL/lock/sequence SQL | inspector normalization/class/denylist/catalog | same `F2eSqlPolicyViolationException` instance before JDBC | exactly 36.3.1; no remap |
| SELECT-only credential/fence unavailable | role grant/login preflight or negative write control | integration gate does not start/accept reader | `PRE_SEMANTIC_OPERATIONAL_FAILURE` |
| physical mutation despite policy | before/after scoped checksum/count | no result accepted; integration gate FAIL | no semantic detector status |
| duplicate run/attempt/boundary/invocation key | single bounded registry and atomic reservation of 37.7.1, before resource/probe/SQL | `IllegalStateException("F2E execution provenance identity reuse")`; no partial key insertion | unchanged RuntimeException |
| provenance/field/catalog/scope mismatch | recompute and cross-consistency 37.7 | discard whole batch; exact identity consistency exception | unchanged RuntimeException |
| digest collision with different preimage | per-invocation preimage registry | discard whole batch; exact collision exception | unchanged RuntimeException |
| snapshot ID inconsistent with payload | reconstruct all direct component preimages | discard whole batch; zero partial snapshots | unchanged RuntimeException |

Ninguna fila de esta tabla agrega/reclasifica los cuatro failure codes o triggers cerrados en 36.
Los defectos de topology/policy/identity permanecen en sus owners operacionales; fallos físicos
JPA/Hibernate/JDBC reales conservan `SOURCE_ACCESS_FAILURE` conforme a 36.3.

### 37.13 Future implementation acceptance gates

Una futura corrección de handoff/implementación R1 deberá convertir estas decisiones en pruebas
objetivas, sin inventar autoridad:

1. recomputador independiente de `LP/SEQ/ID_HASH_V2`, los dos scopes y cuatro vectors I-A–I-D,
   incluida la prueba de que el ownership del descriptor no cambia sus bytes;
2. mutation vectors que cambien individualmente run, attempt, invocation, scope, catalog, row
   field, transaction evidence y labels del descriptor, comprobando inclusiones/exclusiones
   declaradas y rechazo de labels aportados por caller;
3. único enum catalog, mismos query IDs/mapper version y startup fail ante drift;
4. rechazo pre-SQL de scope inválido y prueba arquitectónica de ausencia de caller
   `scopeCanonical`/caller-generated output IDs;
5. provenance exacto de 7 fields/32 keys, record/row equality, PII/SQL/credential exclusions y
   historical `ABSENT`/`Optional.empty()`;
6. bean graph/qualifiers exactos, descriptor inmutable no-bean y no-publicable, sus identity
   references iguales a DS/EMF/TM/shared-EM reales, metadata de la misma Session/Connection igual a
   endpoint/database/schema/principal sellados, cero `@Primary`/fallback/routing y privileged plane
   unreachable;
7. advisors reales: harness explicit TM + reader explicit same TM; negative outside-TX;
8. shared EM bean/delegate/Session, joined transaction, one persistence context y same Connection
   reference before/after; forbidden connection/second-EM APIs ausentes;
9. dos probes por el shared EM, tres inspected IDs ordenados y ningún statement adicional;
10. actual SELECT-only login/grants, denied INSERT, inspector fail-closed y scoped checksum/count
    unchanged;
11. recompute/cross-record inconsistency, missing inspector/fence/wrong resource y cada fila de
    37.12 abortan sin outputs parciales;
12. `SnapshotClaim` R1 contiene/acepta exactamente `SINGLE_READER_TEST`; cualquier intento de
    `MULTI_READER_MVCC` u otro valor falla en el stage exacto de 37.4/37.12, sin evidence/probe/SQL;
13. registry único y acotado: carreras sobre cada key, reserva all-or-none, success, abort antes y
    después de construir identidades, rollback, interruption/`UNKNOWN`, retry sólo con attempt
    nuevo, markers no borrados al liberar y reutilización permitida únicamente tras crear otro
    `ApplicationContext` namespace;
14. no production bean/config/property/consumer, no R6, no managed/lazy escape y exact allowlists.

Los gates 1–14 son requisitos futuros; esta unidad documental no ejecuta ni aprueba R1. Los
queries/read semantics, historical target, propagation, isolation y read-only congelados deben
reportarse `UNCHANGED` por el fresh design audit y por el futuro handoff audit.

### 37.14 Decisiones AD y salida de esta enmienda

No se renumeran AD-01–AD-37. Se agregan:

| ID | Decision | Alternatives rejected | Status |
| --- | --- | --- | --- |
| AD-38 | `F2E_IDENTITY_V2`: LP/SEQ exacto, SHA-256, 64 lower-hex, domains separados | hash/encoding/framing implementation-defined; concatenación | CLOSED_BY_THIS_AMENDMENT / PENDING_FRESH_AUDIT |
| AD-39 | catálogo único enum `R1_RESERVA_V1`, scope derivado internamente y context sin canonical string libre | caller scope/version arbitrarios; verify-later | CLOSED_BY_THIS_AMENDMENT / PENDING_FRESH_AUDIT |
| AD-40 | execution ID incluye run/attempt/invocation y inspected manifest; logical ID excluye execution-only; source/snapshot formulas 37.5 | logical attempt-dependent; provenance sin query evidence | CLOSED_BY_THIS_AMENDMENT / PENDING_FRESH_AUDIT |
| AD-41 | canonical source projection de 11 posiciones, timestamps incluidos, PII excluida, historical `ABSENT` | reflection/map order; campos relevantes vagos; inferencia histórica | CLOSED_BY_THIS_AMENDMENT / PENDING_FRESH_AUDIT |
| AD-42 | provenance core exacto y cuatro vectors I-A–I-D byte-exactos, doble recompute | tests/chat como autoridad; expected adaptado al código | MATERIALIZED / PENDING_FRESH_AUDIT |
| AD-43 | reader y harness nombran explícitamente `f2eReaderTransactionManager`; main-class nominal test bean name permitido sin reachability | default TM, `@Primary`, configurer implícito, manager ambiguity | CLOSED_BY_THIS_AMENDMENT / PENDING_FRESH_AUDIT |
| AD-44 | único shared EM/Session/DS/Connection SELECT-only; probes e inspector en el mismo EMF | independent EM/JDBC como snapshot proof | CLOSED_BY_THIS_AMENDMENT / PENDING_FRESH_AUDIT |
| AD-45 | advisors/proxy path, inspector capture, hard role fence, graph y fail matrix 37.9–37.12 | annotations sin proxy; inspector/role/checksum aislados | CLOSED_BY_THIS_AMENDMENT / PENDING_FRESH_AUDIT |
| AD-46 | `DescriptorRecursoLector` inmutable y config-owned sella labels con DS/EMF/TM/shared-EM y metadata de la misma Session/Connection | labels caller-supplied; hash de labels como prueba; conexión independiente | CORRECTED_AND_MATERIALIZED / PENDING_FRESH_AUDIT |
| AD-47 | R1 soporta exclusivamente `SINGLE_READER_TEST + READ_COMMITTED`; `MULTI_READER_MVCC` se rechaza pre-evidence/probe/SQL y R6 requiere autoridad separada | claim dual R1; MVCC multi-reader sobre READ_COMMITTED; fallback | CORRECTED_AND_MATERIALIZED / PENDING_FRESH_AUDIT |
| AD-48 | un registry harness-owned, atómico y acotado al `ApplicationContext` conserva markers ACTIVE/UNKNOWN/consumed y define retry/restart | check-then-put; borrar al release; unicidad global o durability implícita | CORRECTED_AND_MATERIALIZED / PENDING_FRESH_AUDIT |

```text
Open technical questions in authorized corrective scope: NINGUNA
Human/business decision required: NO
Prior failure/SQL/checksum authority: PRESERVED / NOT_REOPENED
Query/read semantics: UNCHANGED
Historical programming target: ALWAYS_EMPTY / UNCHANGED
R1 propagation/isolation/readOnly/ownership: UNCHANGED
Pure detector: UNCHANGED / DARK_LAUNCH / NOT_PRODUCTIVE
R1 implementation: NOT_STARTED / NOT_AUTHORIZED
R2-R6: NOT_AUTHORIZED
Data audit / migration / resolver / fence / cutover: NOT_AUTHORIZED
TurnoInstructor: PRODUCTIVE AUTHORITY
cutover=false
Next required gate: FRESH_INDEPENDENT_DESIGN_AUDIT_IN_NEW_CHAT
```
