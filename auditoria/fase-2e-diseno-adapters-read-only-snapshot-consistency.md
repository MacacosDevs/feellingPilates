# FeelingPilates — F2E / diseño de adapters read-only y consistencia de snapshot

## 1. Identidad de la unidad y lifecycle

```text
Unidad: F2E / boundary de readers JPA hacia detector puro
Tipo: DESIGN / RESEARCH
Role: DESIGN_EXECUTOR / RESEARCHER
Correction role F2E-ADAPTERS-SNAPSHOT-DESIGN.1.2: DESIGN_CORRECTOR / DOCUMENT_CORRECTOR
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

Shape inválida, enum desconocido, null físico inesperado o rango no positivo es `INPUT_INVALID`,
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
`INPUT_INVALID`/invariante rota y aborta la unidad afectada.

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
| R1 Reserva | `readByReservationIds` | `ReadSnapshotContext` + `Set<UUID> reservationIds` no null/no vacío, sin elementos null | `r.id IN (:reservationIds)` + `NativeQuery.setParameterList("reservationIds", sortedIds, UUID.class)`; 12.2 | lista inmutable de `ReservationSourceSnapshot` | exactamente una por ID pedido | set vacío/null/elemento null=`INPUT_INVALID` antes de SQL; si falta cualquier ID=`SOURCE_RECORD_NOT_FOUND`, aborta batch sin parciales | `r.id`; output por `reservationId` | nuevo `ReservaProjectionQueryExecutor` | SQL nativa + `EntityManager`, unwrap `NativeQuery`, `ReservaProjectionRow` | nuevos tipos bajo `transicion/programacion/read/**`, `adapter/jpa/projection/**`, `adapter/jpa/mapper/**`, `adapter/jpa/**` | NO |
| R1 Reserva | `readByScope` | context + `ReservationScope(Set<UUID> salonIds, LocalDate desde, LocalDate hasta)`; set no null/no vacío, extremos no null e inclusivos, `desde <= hasta`, ventana bounded | `r.salon_id IN (:salonIds)` por `setParameterList(..., UUID.class)`; fechas escalares tipadas; 12.2 | lista inmutable de `ReservationSourceSnapshot` | 0..N | salon set vacío/null/elemento null o rango null/invertido=`INPUT_INVALID` antes de SQL; cero filas es válido | `r.id`; output por `reservationId` | el mismo executor | igual | los mismos paths | NO |
| R2 legacy | `readForDate` | context + `LegacyTurnScope(Set<UUID> salonIds, LocalDate fecha)`; set no null/no vacío y fecha no null | members: `t.salon_id IN (:salonIds)`; assignments: `turno_id IN (:turnIds)`; ambas por `setParameterList(..., UUID.class)` y escalares tipados; si `turnIds` derivado queda vacío no se ejecuta query 2; 12.2 | `LegacyTurnReadSet(sources)` inmutable o fallo `LegacyAdapterInputInvalid` con lista no vacía de `LegacyAdapterRejection`; nunca ambos | 0..N según 12.3; una evaluación por source publicado | scope vacío/null=`INPUT_INVALID` antes de SQL; cero turnos tras query 1 produce read set vacío; mitad incoherente/malformed aborta sin parciales | members `turnId,memberId NULLS FIRST`; assignments `turnId,instructorId,activityId`; sources `turnId/member/activity/evidenceKey` | nuevo `LegacyTurnProjectionQueryExecutor` | dos SQL nativas + `EntityManager`, unwrap `NativeQuery`, records concretos | nuevos tipos en los mismos cuatro packages; no repository bajo `calendario` | NO |
| R3 nominal | `readNominalOnDate` | context + `LocalDate fecha` no null; `dayOfWeek` se deriva determinísticamente 0=domingo | named scalar binding de `fecha`, `dayOfWeek`, `assignmentActive=true`, `blockActive=true`; 12.2 | `NominalProgrammingReadSet(candidates,backing)` inmutable | 0..N; máximo una nominal por serie/fecha | fecha null=`INPUT_INVALID` antes de SQL; cero es válido para universe; cuando otro claim exige target, el classifier decide `MISSING` | ambos por `serieId,asignacionVersionId` | nuevo `NominalProjectionQueryExecutor` | SQL nativa + `EntityManager`, unwrap `NativeQuery`, `NominalProjectionRow` | nuevos ports/executor/row/mapper/adapter en packages de transición | NO; no se cambia `AsignacionRepository` |
| R4 ajustes | `readActiveAdjustmentsOnDate` | context + `LocalDate fecha` no null | named scalar binding de `fecha` y `active=true`; 12.2; no existe collection/range parameter | `AdjustmentReadSet(sources,backing)` inmutable | 0..N | fecha null=`INPUT_INVALID` antes de SQL; cero es válido | ambos por `fecha,adjustmentId` | nuevo `AdjustmentProjectionQueryExecutor` | SQL nativa + `EntityManager`, unwrap `NativeQuery`, `AdjustmentProjectionRow` | nuevos ports/executor/row/mapper/adapter en packages de transición | NO; no se cambia `AjusteProgramacionFechaRepository` |
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

No existe filtro nullable en R1–R4. Todo input null o incoherente produce `INPUT_INVALID` antes
de crear/ejecutar SQL; queda prohibido `(:x IS NULL OR column = :x)`. Los null permitidos en
projections (horas raw legacy, vigencias abiertas y campos de ajuste por forma) son datos leídos,
no parámetros de filtro.

#### 12.2.2 Tabla exacta de parámetros por port/query

| Port/query | Parameter | Java/lógico | PostgreSQL | Scalar/multi | Binding | Empty semantics | Null semantics |
| --- | --- | --- | --- | --- | --- | --- | --- |
| R1 `readByReservationIds` | `reservationIds` | `Set<UUID>` → lista canónica | `uuid` | multi | `r.id IN (:reservationIds)` + `setParameterList(..., UUID.class)` | caller vacío=`INPUT_INVALID`, cero SQL | set/elemento null=`INPUT_INVALID`, cero SQL |
| R1 `readByScope` | `salonIds` | `Set<UUID>` → lista canónica | `uuid` | multi | `r.salon_id IN (:salonIds)` + `setParameterList(..., UUID.class)` | caller vacío=`INPUT_INVALID`, cero SQL | set/elemento null=`INPUT_INVALID`, cero SQL |
| R1 `readByScope` | `desde` | `LocalDate` | `date` | scalar | `r.fecha >= :desde`; typed scalar | no aplica | null=`INPUT_INVALID`, cero SQL |
| R1 `readByScope` | `hasta` | `LocalDate` | `date` | scalar | `r.fecha <= :hasta`; typed scalar | no aplica; `desde > hasta` inválido | null=`INPUT_INVALID`, cero SQL |
| R2 members | `salonIds` | `Set<UUID>` → lista canónica | `uuid` | multi | `t.salon_id IN (:salonIds)` + `setParameterList(..., UUID.class)` | caller vacío=`INPUT_INVALID`, ninguna de las dos SQL | set/elemento null=`INPUT_INVALID`, ninguna SQL |
| R2 members | `fecha` | `LocalDate` | `date` | scalar | comparación puntual; typed scalar | no aplica | null=`INPUT_INVALID`, ninguna SQL |
| R2 members | `dayOfWeek` | `Short` derivado, 0=domingo | `smallint` | scalar | rama recurrente; typed scalar | no aplica | imposible tras fecha válida; si falla derivación=`INPUT_INVALID` |
| R2 members | `active` | constante `Boolean.TRUE` | `boolean` | scalar | `t.activo = :active`; typed scalar | no aplica | prohibido |
| R2 members | `recurrentType`, `exceptionType`, `cancellationType` | constantes `String` con nombres del enum | `varchar` | tres scalars | comparaciones `t.tipo = :...`; typed scalar individual, no list | no aplica | prohibido |
| R2 assignments | `turnIds` | set derivado de query members → lista canónica | `uuid` | multi | `a.turno_id IN (:turnIds)` + `setParameterList(..., UUID.class)` | vacío derivado=cero turnos: omitir SQL 2 y devolver read set vacío | null/elemento null=invariante rota, `LegacyAdapterInputInvalid`, sin read set |
| R3 `readNominalOnDate` | `fecha` | `LocalDate` | `date` | scalar | cuatro comparaciones de vigencia; mismo named typed scalar | no aplica | null=`INPUT_INVALID`, cero SQL |
| R3 `readNominalOnDate` | `dayOfWeek` | `Short` derivado, 0=domingo | `smallint` | scalar | `b.dia_semana = :dayOfWeek`; typed scalar | no aplica | imposible tras fecha válida; fallo=`INPUT_INVALID` |
| R3 `readNominalOnDate` | `assignmentActive`, `blockActive` | constantes `Boolean.TRUE` | `boolean` | dos scalars | predicados `= :...`; typed scalar | no aplica | prohibido |
| R4 `readActiveAdjustmentsOnDate` | `fecha` | `LocalDate` | `date` | scalar | `a.fecha = :fecha`; typed scalar | no aplica | null=`INPUT_INVALID`, cero SQL |
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
sourceName                  datasource lógico autorizado o fixture Testcontainers nombrado
schemaFingerprint           Flyway + schema contract fingerprint
projectionCatalogVersion    versión única de las queries/mappers
ruleCatalogVersion          versión de reglas detector/F2D
businessZone                ZoneId explícita
scopeCanonical              fecha/ventana/salones/IDs, ordenados y length-prefixed
snapshotClaim               MULTI_READER_MVCC | SINGLE_READER_TEST
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

En production composition, sólo R6 crea el contexto. Para `MULTI_READER_MVCC`, primero verifica
`repeatable read` y `read only`, captura la representación textual exacta de
`pg_current_snapshot()` y calcula:

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

`fixtureIdentity` y `testInvocationIdentity` son inputs literales/deterministas del test. No se
inventa `pgSnapshotFingerprint`, no se afirma snapshot multi-reader y el mismo input produce el
mismo ID. R1 standalone no necesita ni recibe fingerprint PostgreSQL. R2–R5 pueden usar RR en el
harness por tener múltiples statements, sin convertirse en owners productivos del contrato.

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

**Contrato de composición R6.** Sólo el método público proxied del
`DetectorReadCoordinator` posee `REQUIRES_NEW + REPEATABLE_READ + readOnly + timeout bounded` y el
claim `MULTI_READER_MVCC`. R6 crea el `ReadSnapshotContext`, captura/valida metadata, compone R1–R5
y descarta el conjunto completo ante fallo. Ninguna annotation interna de F2D sustituye este owner.

R1 usa una sola query por operación y no declara ni necesita repeatable-read para demostrar
projection, mapping, no-write y participación `MANDATORY`. Si R1 forma parte de una evaluación
multi-reader, hereda RR de R6. Esto mantiene R1 implementable y auditable antes de que exista R6.

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
de sección 13 a partir de
`fixtureIdentity`, `testInvocationIdentity`, scope y versiones literales suministradas por el test.
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

Permitidos:

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
- el owner guard (test harness o R6) rechaza read-write antes de invocar el adapter; rechaza
  isolation distinta de RR para R2–R6; R1 acepta RC sólo con `SINGLE_READER_TEST` y R6 exige RR
  antes de entregarle un context `MULTI_READER_MVCC`;
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
| AD-16 | ¿`readOnly=true` basta? | No; StatementInspector exact-hash policy + rol SELECT-only + scoped canonical checksum + architecture/statistics | Spring lo trata como hint/optimization | sólo annotation/rollback/global checksum | evidencia 0 writes acotada | all implementation slices | CLOSED |
| AD-17 | ¿Beans antes de composition? | No; plain adapters/executors con `EntityManager` y unwrap `NativeQuery`, test-only import | runtime isolation requirement + binding Hibernate explícito | scanned internal bean / Spring Data repository | ausencia verificable sin volver opcional el query contract | R1-R5 | CLOSED |
| AD-18 | ¿Beans en R6? | sólo profile + disabled-by-default property | dark launch | unconditional bean | explicit opt-in no productivo | R6 | CLOSED |
| AD-19 | ¿Primer slice? | Reserva reader | mapping directo/one table/high lazy risk | combined readers | menor riesgo y reusable harness | R1 | CLOSED |
| AD-20 | ¿Coordinator? | REQUIRED_AFTER_READERS | multi-reader same snapshot | deferred indefinitely / now | no composition premature | R6 | CLOSED |
| AD-21 | ¿HostValidator? | design no; every JPA slice yes | PostgreSQL/Testcontainers physical behavior | sandbox-only | evidence real | R1-R6 | CLOSED |
| AD-22 | ¿Data audit ahora? | No; source/custodian/credential material separados del rol efímero de implementation | source unavailable/no authority | Testcontainers/local config como source | no fabricated results | future data-audit handoff | CLOSED |
| AD-23 | ¿Report? | immutable file artifact outside read TX | no DB/crosswalk writes | DB report table | nonproductive/auditable | future audit | CLOSED |
| AD-24 | ¿Firmas/ausencia de read ports? | Tabla 12.1; missing requested Reserva aborta, filtro collection vacío es inválido, resultado de scope/universe sin filas es válido por claim | core + source cardinality | dejarlo al executor / quitar filtro vacío | handoff R1–R5 sin decisión residual | R1-R5 | CLOSED |
| AD-25 | ¿Átomo e incompletos R2? | assignment PK; gaps con evidence key; fórmula exacta; representable vs pre-core rejection y counts 12.3 | PKs V19/V20/V22 + core real | dedupe / divergent universal | mismo estado produce mismo count y scenario legal | R2 | CLOSED |
| AD-26 | ¿Provenance depende de R6? | `ReadSnapshotContext`; R6 o test harness lo suministra; MVCC, run y read-set son IDs separados | single reader no debe inventar pg snapshot | global clock/random/fingerprint falso | R1 independiente | R1-R6 | CLOSED |
| AD-27 | ¿Diagnóstico R5? | collector adapter-local in-memory dentro de graph dedicado | interface `ProgramacionDiagnostico` inyectable | SLF4J callback/no diagnostics | omissions transportables sin I/O | R5 | CLOSED |
| AD-28 | ¿Checksum/no-write scope? | tablas/rows 20.3, canonicalización 20.4 y orden before/read/after | schema físico y concurrencia | checksum global/no DML genérico | proof reproducible por slice | R1-R6 | CLOSED |
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
inventado (13); constructor/test wiring (12.1/21); role, StatementInspector y checksum de `reserva`
(20); binding, vacíos y nulls exactos (12.2); HostValidator (24.1/27); y allowlist R1 (24.1). No
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
