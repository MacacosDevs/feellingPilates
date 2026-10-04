# FeelingPilates — F2E / identidad, semántica legacy y detector-only

## 1. Identidad de la unidad y lifecycle

```text
Unidad: F2E / cierre de identidad, semántica legacy y contrato detector-only
Tipo: DESIGN / RESEARCH
Role original: DESIGN_EXECUTOR / RESEARCHER
Correction role F2E-IDENTIDAD-DETECTOR.1.1: DESIGN_CORRECTOR / DOCUMENT_CORRECTOR
Execution profile: DESIGN / READ_ONLY_RESEARCH
Fresh execution: SI
Checkpoint: auditoria/fase-2e-identidad-semantica-detector-read-only.md
Estado de este output: DESIGN_APPROVED / CLOSED
Design/documentation gate: PASS
Final review: auditoria/reviews/F2E-IDENTIDAD-DETECTOR-REVIEW-DISENO.md
Implementation: NOT_AUTHORIZED
Migration: NOT_AUTHORIZED
Cutover: false
```

Este checkpoint es el único output de la unidad autorizada por
`auditoria/handoffs/HANDOFF-F2E-IDENTIDAD-DETECTOR-READ-ONLY.md`. Cierra diseño y semántica
detector-only; no implementa el detector, no ejecuta data audit, no selecciona mappings, no
persiste crosswalk, no materializa resolver o fence y no cambia autoridad productiva.

El nombre conserva exactamente la ruta impuesta por el handoff. No existía físicamente al iniciar
la unidad y no se creó ningún otro archivo.

## 2. Base Git y pre-flight fresh

Baseline original de materialización de la unidad:

```text
Repository: /Users/jesusaldaircruzortiz/Desktop/Feelingpilates/feelingpilates
Branch inicial: operacion/excepciones-horario-fecha
HEAD inicial: 258c21b0383579a6734bc20f87b0c84008cc9bdf
Staging inicial: VACÍO
Working tree inicial: CLEAN
Baseline dirty autorizado: NINGUNO
```

Antes de analizar se confirmó físicamente:

```text
HANDOFF: APPROVED / ACTIVE / AUTHORIZED_FOR_DESIGN_RESEARCH
Target: F2E / cierre de identidad, semántica legacy y contrato detector-only
Type: DESIGN / RESEARCH
Checkpoint target al iniciar: PENDING / NOT_CREATED
Design/document gate al iniciar: PENDING
Implementation: NOT_AUTHORIZED
Migration: NOT_AUTHORIZED
Cutover: false
Authority: TurnoInstructor / LEGACY_VIVO / PRODUCTIVO
Runtime: DARK_LAUNCH
Productive: NOT_PRODUCTIVE
```

No se encontró contradicción de autoridad ni baseline físico incompatible.

Pre-flight físico de la corrección F2E-IDENTIDAD-DETECTOR.1.1:

```text
Branch: operacion/excepciones-horario-fecha
HEAD: 258c21b0383579a6734bc20f87b0c84008cc9bdf
Staging: VACÍO
Working tree / baseline autorizado:
?? auditoria/fase-2e-identidad-semantica-detector-read-only.md
Unexpected paths: NINGUNO
```

## 3. Autoridad, scope y prohibiciones

### 3.1 Autoridad leída

Se leyeron completos, antes de cerrar decisiones:

- `AGENTS.md` y `auditoria/orquestacion/{README,WORKFLOW,STATE-MACHINE,GATES,ROLES}.md`;
- el handoff activo y
  `auditoria/reviews/HANDOFF-F2E-IDENTIDAD-DETECTOR-READ-ONLY-REVIEW.md`;
- `auditoria/{ESTADO-ACTUAL,ARQUITECTURA-ACTUAL,DECISIONES-ARQUITECTONICAS}.md`;
- `auditoria/{README-REESTRUCTURACION,REGLAS-DE-TRABAJO-IA}.md`;
- `auditoria/contexto/{DOMINIO-FUNCIONAL,MAPA-LEGACY-Y-MIGRACION}.md`;
- el diseño aprobado F2D.1, su review final, el checkpoint F2D.2 y su review documental;
- el checkpoint F2E.1, su review final y su handoff histórico.

La cadena competente queda interpretada sin sustituir una fuente por otra:

```text
F2D.1 + review final -> autoridad de diseño aprobada
F2D.2 + review documental -> materialización y cierre dark-launch
código + migraciones + tests -> evidencia física de materialización y comportamiento cubierto
canónicos -> estado y autoridad productiva vigentes
```

### 3.2 Scope realizado

- investigación read-only de identidad de Reserva, legacy y programación nueva;
- cierre D04;
- cierre D03 para generación `0..N`, evidencia, clasificación, ambigüedad y provenance;
- cierre D09 para semántica observable e historia no reconstruible;
- cierre D10 para semántica observable legacy/nueva;
- cierre D11 para auditor, candidate/evidence generator y report/metrics detector-only;
- creación exclusiva de este checkpoint.

### 3.3 Scope prohibido y no realizado

```text
Código productivo: NO MODIFICADO
Tests: NO MODIFICADOS
Migraciones/Flyway: NO MODIFICADAS
SQL/data queries: NO EJECUTADAS
Datos: NO MODIFICADOS
Reserva/ReservaService: NO MODIFICADOS
Frontend/mobile: NO MODIFICADOS
Controllers/endpoints: NO CREADOS NI MODIFICADOS
Auditor/detector/crosswalk/resolver/fence code: NO CREADO
Crosswalk persistido: NO
Mapping seleccionado: NO
Migration/normalization/repair: NO
MIGRANDO/NUEVA: NO
Cutover/authority change: NO
git add/commit/push: NO
```

## 4. Evidencia física inspeccionada

Se inspeccionaron read-only, como mínimo:

- legacy: `Reserva`, `TurnoInstructor`, `TurnoInstructorAsignacion`, DTOs, controllers,
  repositories, `ReservaService` y `TurnoInstructorService`;
- caracterización: `ReservaServiceCaracterizacionTest`,
  `TurnoInstructorServiceCaracterizacionTest` y
  `TurnoInstructorServiceHorarioVersionadoTest`;
- nuevo: `BloqueProgramacion`, `Asignacion`, `AjusteProgramacionFecha`, sus repositories,
  `OcurrenciaNominal`, `OcurrenciaEfectiva`, `ReferenciaOcurrencia`, `ProgramacionNominal`,
  `AplicadorAjustesProgramacion`, `ProgramacionEfectiva`, `ProgramacionValidador` y el writer
  interno de ajustes;
- tests de aplicador, programación efectiva y writer de ajustes;
- operación: `HorarioOperacion`, `SalonHorarioExcepcion`, sus repositories,
  `HorarioOperacionResolver`, `HorarioEfectivoSalon` y `SalonHorarioExcepcionService`;
- migraciones V15, V17, V19–V22, V38, V41 y V47, además de V18 para excepción operativa;
- `EntidadBase` para la semántica física de timestamps.

Hechos físicos determinantes:

1. `Reserva` persiste `reserva.id`, salón, instructor, cliente, actividad, fecha, inicio, fin,
   estado y timestamps; no persiste turno, asignación legacy, serie, versión, ajuste ni occurrence.
2. `ReservaService` calcula `horaFin` desde la duración actual y acepta la reserva cuando el rango
   está contenido por algún turno vigente. No verifica la fila de actividad/rango de
   `TurnoInstructorAsignacion` y no conserva cuál turno permitió reservar.
3. Para reservas, cualquier `CANCELACION` legacy del instructor/salón/fecha vacía todo el día; una
   o más `EXCEPCION` sustituyen todos sus recurrentes de esa fecha.
4. `TurnoInstructor` recurrente no tiene vigencia funcional. Update muta la misma fila; reemplaza
   completamente las filas de asignación mediante delete/flush/insert. Delete es `activo=false`.
5. `creado_en`/`actualizado_en` provienen de timestamps técnicos JPA; las asignaciones legacy no
   tienen timestamps ni historia propia.
6. La programación nueva separa serie lógica, fila versionada y occurrence fechada. V47 protege
   que una serie de asignación tenga como máximo una versión activa aplicable a una fecha.
7. F2D materializa recurrente/reemplazo con referencia `(SERIE_ASIGNACION, serieId, fecha)` y
   adición con `(AJUSTE, ajusteId, fecha)`.
8. Una cancelación nueva válida exige nominal única y la suprime; un reemplazo conserva la
   referencia de serie y sustituye el snapshot; una adición no tiene target nominal y crea una
   occurrence con referencia propia.
9. `SalonHorarioExcepcion` sustituye la operación semanal del salón en una fecha. No es un ajuste
   de programación ni un marcador `TurnoInstructor.CANCELACION`.
10. Ninguna ruta examinada muta o cancela automáticamente una `Reserva` persistida cuando cambia
    o desaparece programación.

## 5. Compatibilidad obligatoria con F2D

```text
F2D_CONTRACT_COMPATIBILITY: PASS
AUTHORITY_CONFLICTS DETECTED: 0
```

El diseño aprobado, la materialización y el código coinciden en estos contratos que esta unidad no
reabre:

| Concepto | Contrato preservado |
| --- | --- |
| Target de `CANCELACION`/`REEMPLAZO` | `serieId + fecha`, sobre occurrence nominal |
| Identidad recurrente | `(SERIE_ASIGNACION, serieId, fecha)` |
| Identidad de reemplazo | La misma `(SERIE_ASIGNACION, serieId, fecha)` |
| Identidad de adición | `(AJUSTE, ajusteId, fecha)` |
| Identidad del ajuste | `ajusteId`, con fecha inmutable para adición activa |
| Composición | `NOMINALES -> AJUSTES -> OPERATIVO FINAL -> MAESTROS/INVARIANTES` |
| Cancelación válida | target nominal único + cero efectivas = `EXPECTED_ABSENCE / SUPPRESSED` |
| Target requerido ausente | `MISSING / BLOCKER`, no supresión válida |

Todo futuro detector debe emitir `F2D_CONTRACT_COMPATIBLE` para una evaluación compatible. Una
divergencia material debe emitir `F2D_AUTHORITY_CONFLICT`, `blocking=true`, detener el claim
afectado y nunca reinterpretar F2D desde el código observado.

## 6. D04 — identidad target futura de Reserva

### 6.1 Problema demostrado

La identidad de `Reserva` es `reserva.id`. Su relación actual con programación sólo se demuestra
por una validación transitoria de contención; la referencia usada no se persiste. Por ejemplo:

```text
Turno/ventana: [08:00,12:00)
Reserva:       [09:00,10:00)

containment: SI
same identity: NO
```

Invariante:

```text
Reserva contenida en TurnoInstructor
!=
Reserva identificada por TurnoInstructor

containment -> candidate evidence
containment -/-> target identity proof
```

### 6.2 Matriz definitiva de átomos de identidad

`Card.` describe relaciones posibles hacia otros átomos, no una cardinalidad universal.

| # | Átomo | Identidad | Estabilidad y temporalidad | Card. | Authority | Materialized? | Productive? | ¿Target de Reserva? | Evidencia / límites |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| A | Reserva actual | `reserva.id` | Estable; fecha, salón, instructor, actividad y `[inicio,fin)` son snapshot del hecho reservado | N:1 posible hacia occurrence; cada Reserva tendrá `0..1` asociación según mapping/estado futuro | Reserva legacy | Sí, tabla | Sí | Es la identidad source, no target de programación | Entidad, V15 y repository; sin FK de programación |
| B | Turno legacy | `turno_instructor.id` | ID estable, contenido mutable; recurrente sin vigencia o puntual por fecha | 1:N, N:1, 0:N | `TurnoInstructor` | Sí | Sí | No | Agrupa instructores/actividades; activo mutable; timestamps no son vigencia |
| C | Asignación legacy | `(turno_id, usuario_id, tipo_actividad_id)` | Fila reemplazable; rango nullable, sin historia/timestamps propios | 1:1, 1:N, N:1, 0:N | `TurnoInstructor` | Sí | Sí | No | Null/null hereda ventana del turno sólo como semántica operativa |
| D1 | Serie nueva de bloque | `programacion_bloque.serie_id` | Lógica, versionable, no fechada | 1:N versiones y occurrences | Ninguna productiva | Sí | No | No | Contenedor salón/día/rango; no identifica asignación ni occurrence reservable |
| D2 | Serie nueva de asignación | `programacion_asignacion.serie_id` | Lógica, versionable, no fechada | 1:N versiones; 1 occurrence por fecha válida | Ninguna productiva | Sí | No | Sólo combinada con fecha | Identidad aprobada por F2D para occurrence recurrente/reemplazo |
| E | Versión/asignación nueva | `programacion_asignacion.id` | Fila física con vigencia inclusiva | N:1 serie; 0:1 por serie/fecha válida | Ninguna productiva | Sí | No | No | V47 EXCLUDE evita versiones activas solapadas; la fila no es la serie |
| F | Occurrence recurrente | `(SERIE_ASIGNACION, serieId, fecha)` | Estable por fecha; contenido proviene de la versión aplicable | 1:1 referencia; 1:N reservas | Ninguna productiva | Runtime | No | Sí, en el contrato elegido | `ReferenciaOcurrencia` + nominal/efectiva |
| G | Occurrence reemplazada | `(SERIE_ASIGNACION, serieId, fecha)` | Conserva identidad de F; snapshot resultado explícito | 1:1 target nominal y 1:1 efectiva válida | Ninguna productiva | Runtime + ajuste | No | Sí, en el contrato elegido | F2D y aplicador: cambia contenido, no referencia |
| H | Occurrence de adición | `(AJUSTE, ajusteId, fecha)` | Propia; retirar/recrear exige UUID nuevo | 0:1 nominal; 1:1 efectiva válida | Ninguna productiva | Runtime + ajuste | No | Sí, en el contrato elegido | No tiene serie/target nominal |
| I | Ajuste puntual | `programacion_ajuste_fecha.id` | ID propio; activo mutable; fecha de adición activa inmutable | Cancelación 1:0; reemplazo 1:1; adición 0:1→1 | Ninguna productiva | Sí | No | No por sí solo | Tipo y forma determinan target/output; un ajuste no es la Reserva |
| J | Ventana reservable / snapshot efectivo | Snapshot final `(fecha,salón,instructor,actividad,inicio,fin)` de una occurrence identificada | Derivada para el snapshot/reglas evaluados; puede cambiar sin cambiar la referencia | 1:N reservas; 0 si suprimida/inválida | Futura | Sólo runtime | No | No; es contexto/evidence de la occurrence | No existe tabla `Sesion`; operación/maestros pueden omitirla fail-closed |
| K | Subintervalo reservado | No tiene identidad target propia; `[horaInicio,horaFin)` pertenece al snapshot de consumo de `reserva.id` | Snapshot histórico de consumo de la Reserva | N:1 dentro de J; exactamente 1 por Reserva | Reserva legacy/futura | Sí en Reserva | Sí | No; nunca integra la identidad target | Half-open; sirve para contención/evidence y no tiene que ser igual a J |
| L | Occurrence efectiva | `ReferenciaOcurrencia` | Referencia lógica estable; presencia y contenido actuales se evalúan por separado y pueden quedar suprimidos | 0:1 resultado efectivo actual por referencia; 1:N reservas | Futura | Runtime | No | Sí, exclusivamente mediante F/G/H | El snapshot final y el estado efectivo no forman parte de la referencia |

No se utiliza `TurnoInstructor`, una fila versionada, una serie sin fecha, un ajuste aislado ni la
contención como identidad target.

### 6.3 Alternativas evaluadas

| Alternativa | Estabilidad/historicidad | Puntuales y cardinalidad | Semántica de Reserva | Decisión |
| --- | --- | --- | --- | --- |
| A. Reserva → occurrence exacta como target | La referencia F2D es estable; K no se pierde porque permanece en el snapshot de consumo de Reserva/asociación | Soporta reemplazo, adición y la referencia histórica de una cancelación; una occurrence admite `0..N` reservas | Target inequívoco y consumo parcial expresado por separado | **Elegida** |
| B. Reserva → ventana padre reservable | Expresa contención, pero una ventana sólo por campos no tiene identidad estable; una serie sin fecha no distingue occurrence | Puede conflar varias occurrences contenedoras y no expresa bien adiciones | Reproduce la validación legacy, no la identidad futura inequívoca | Rechazada |
| C. Identidad target = occurrence + subintervalo | La referencia F2D es estable, pero añadir K crea pseudo-identidades dependientes del consumo de cada Reserva | Dos reservas de la misma occurrence podrían compartir o diferir en K sin cambiar el target real | Confunde identidad de programación con snapshot de consumo | Rechazada como identidad; ambos datos se conservan como campos separados de la asociación conceptual |
| D. Sesión materializada nueva | Podría dar historia propia, pero `Sesion` no existe ni está diseñada/autorizada en esta unidad | Añade lifecycle, persistencia y migración no decididos | No es necesaria para cerrar el contrato lógico | Rechazada por expansión no autorizada |

### 6.4 Contrato elegido

```text
reservation_identity = reserva.id

programming_target_identity =
    effective_occurrence_reference

reservation_consumption_snapshot =
    reserved_subinterval [horaInicio,horaFin)

effective_occurrence_reference =
    recurrente/reemplazo -> (SERIE_ASIGNACION, serieId, fecha)
    adición              -> (AJUSTE, ajusteId, fecha)
```

La asociación futura conserva los tres conceptos sin convertirlos en una nueva entidad material:

```text
ReservaAssociation {                 // forma conceptual, no clase ni tabla autorizada
  reservaId                          // reservation_identity
  programmingTargetReference         // programming_target_identity
  reservedInicio                     // reservation_consumption_snapshot
  reservedFin                        // reservation_consumption_snapshot
}
```

Invariantes:

1. `reservation_identity`, `programming_target_identity` y
   `reservation_consumption_snapshot` son conceptos distintos. Cada Reserva tiene `0..1`
   asociación según mapping/estado futuro; cada asociación apunta a una sola referencia de
   occurrence y una occurrence admite `0..N` reservas.
2. Para demostrar inicialmente una asociación, la referencia target debe ser inequívoca y el
   snapshot de la occurrence/ventana debe contener totalmente
   `[reserva.inicio,reserva.fin)`. Presencia actual, contenido efectivo e identidad se evalúan en
   ejes separados.
3. Contención y coincidencia de fecha, salón, instructor, actividad o intervalo generan candidate
   evidence; no forman `candidate_identity` ni prueban el target.
4. Un reemplazo conserva la referencia `serieId+fecha`; sus atributos efectivos pueden cambiar sin
   cambiar `programming_target_identity`. El snapshot de consumo de Reserva permanece histórico.
5. Una cancelación válida produce `EXPECTED_ABSENCE / SUPPRESSED`. Si la asociación histórica ya
   está demostrada, conserva `programmingTargetReference` aunque `ProgramacionEfectiva` ya no emita
   la occurrence. Una incompatibilidad operativa con una Reserva confirmada se clasifica por
   separado y puede bloquear; nunca borra, sustituye ni convierte la referencia en `MISSING`.
6. El estado `CANCELADA` de Reserva no borra su asociación histórica. La disposición/migración de
   reservas pasadas o canceladas sigue diferida, pero la forma de identidad no cambia.
7. Salón, instructor, actividad, fecha e intervalo permanecen como snapshot/evidence de Reserva;
   no sustituyen ni integran la referencia.
8. Dos Reservas distintas pueden compartir la misma occurrence target e incluso el mismo rango
   reservado sin colisión: siguen siendo distintas por `reserva.id`.

### 6.5 Decisión D04

```text
D04: CLOSED_BY_THIS_DESIGN
Chosen target identity: EFFECTIVE_OCCURRENCE_REFERENCE
Reservation consumption snapshot: RESERVED_SUBINTERVAL
Association ownership/identity: RESERVA.ID
Containment != identity: PRESERVED
Human decision required for D04: NO
```

Rationale: la referencia aprobada por F2D identifica exclusivamente la occurrence target. El
subintervalo probado físicamente se conserva como snapshot de consumo de la Reserva/asociación,
sin inventar una sesión persistida ni usar consumo o coincidencia de campos como identidad.

### 6.6 Respuestas autocontenidas para un futuro executor

```text
What identifies Reserva?                 -> reserva.id
What identifies the programming target? -> ReferenciaOcurrencia aprobada por F2D
What interval was reserved?              -> snapshot de consumo [horaInicio,horaFin)
Is that interval part of target identity?-> NO
Can two Reservas share one target?        -> SI
Can they also share the same interval?    -> SI; reserva.id evita colisión
Does cancellation erase the reference?   -> NO; conserva identidad histórica
```

## 7. D03 — contrato detector-only

### 7.1 Boundary

El detector puede:

```text
READ
OBSERVE
PROJECT TYPED ATOMS
GENERATE 0..N CANDIDATES
PRESERVE EVIDENCE
CLASSIFY
REPORT
METRIC
FAIL CLOSED
```

No puede:

```text
SELECT FINAL TARGET
WRITE selected_target
PERSIST CROSSWALK
CREATE CROSSWALK TABLE
MATERIALIZE RESOLVER
MUTATE DATA
MIGRATE / NORMALIZE / REPAIR
ENFORCE FENCE
CHANGE AUTHORITY
SWITCH READERS/WRITERS/CONSUMERS
```

`selection_status` es obligatorio y siempre vale `NOT_SELECTED_BY_DETECTOR` en este componente,
incluso cuando queda un solo candidato. Un candidato único es evidencia para un boundary futuro;
no es un mapping final materializado.

### 7.2 Source atoms, target atoms y candidate rules

| Source atom | Target universe | Regla de generación aprobada | Evidencia que no basta | Salida fail-closed relevante |
| --- | --- | --- | --- | --- |
| Reserva A | Referencias de occurrence F/G/H y su resultado efectivo por separado | La Reserva aporta su snapshot y subintervalo; una occurrence `PRESENT` puede generar candidate evidence cuando coincide en fecha/salón/instructor/actividad y su rango contiene K. Una referencia histórica ya demostrada se conserva aunque su outcome actual sea `EXPECTED_ABSENCE` | Contención o coincidencia de campos; K nunca integra la identidad candidate | 0 no implica automáticamente `MISSING`; >1 es ambiguo; 1 no se selecciona |
| Turno RECURRENTE B | Series/asignaciones D2/E y nominales por fecha | Salón, día, instructor, actividad/rango observables y contexto de fecha explícito | ID de turno, timestamps, cercanía | Historia desconocida limita a `UNKNOWN_HISTORY`; 0..N |
| Asignación legacy C | Asignaciones/series D2/E | Instructor, actividad, rango propio o fallback declarado, salón/día y fecha | Fallback null/null o fields match | 0..N; no inferir vigencia/serie |
| EXCEPCION legacy B/C | Ajustes I u occurrences G/H en fecha | Proyectar candidatos por fecha y dimensiones observables; conservar tanto reemplazo como adición plausibles | Que el reader sustituya recurrentes o que el writer la trate como puntual | `UNSUPPORTED / UNKNOWN_INTENT`; puede además ser `AMBIGUOUS` |
| CANCELACION legacy B/C | Nominales F en la fecha | Candidatos del mismo salón+instructor+fecha; el rango legacy se conserva como evidencia, no como scope target demostrado | Outcome legacy de día cancelado | `UNSUPPORTED / UNKNOWN_INTENT`; nunca `EXPECTED_ABSENCE` sin target nominal demostrado |
| APF CANCELACION I | Nominal F y outcome efectivo | `serieId+fecha`; exigir exactamente una nominal y cero efectivas atribuibles al ajuste | Lista efectiva vacía sin provenance | `EXPECTED_ABSENCE` si válido; `MISSING`/`DIVERGENT` si no |
| APF REEMPLAZO I | Nominal F y efectiva G | Misma `serieId+fecha`, snapshot resultado completo, exactamente una efectiva válida | Igualdad parcial de campos | `MISSING`, `AMBIGUOUS` o `DIVERGENT_INCOMPATIBLE` |
| APF ADICION I | Efectiva H | Sin target nominal; `ajusteId+fecha`, snapshot completo, exactamente una efectiva válida | Crear una serie sintética | `NOT_APPLICABLE` en eje nominal; `MISSING`/`DIVERGENT` si falta outcome |

Los filtros de elegibilidad sólo pueden usar reglas documentadas, versionadas y deterministas.
Un campo o score opcional puede explicar evidencia, nunca ordenar para elegir.

### 7.3 Cardinalidades por relación

| Relación | Cardinalidades admitidas | Interpretación |
| --- | --- | --- |
| Turno legacy → serie/asignación nueva | `1:1`, `1:N`, `N:1`, `0:N` | Granularidad, consolidación e historia impiden asumir isomorfismo |
| Asignación legacy → serie/asignación nueva | `1:1`, `1:N`, `N:1`, `0:N` | Rango nullable y versionado requieren evidencia |
| Legacy recurrente → occurrences por horizonte | `1:N`; por fecha `0:1` o `0:N` candidatos | Fecha es obligatoria para evaluar occurrence |
| Reserva → occurrence | Asociación conceptual agregada `N:1`; cada Reserva `0..1` asociación; detección histórica `0:1`, `1:1` o `1:N` candidates | Cada asociación apunta a una referencia; una occurrence puede tener N reservas y el subintervalo pertenece a cada Reserva |
| APF CANCELACION → nominal | `1:1` requerido | Cero = `MISSING`; más de uno = invariante/ambigüedad |
| APF CANCELACION → efectiva | `1:0` esperado | `EXPECTED_ABSENCE / SUPPRESSED` |
| APF REEMPLAZO → nominal/efectiva | `1:1` / `1:1` | Conserva referencia de serie |
| APF ADICION → nominal/efectiva | `0:1` (`NOT_APPLICABLE`) / `1:1` | Referencia propia de ajuste |

Representaciones requeridas por el vocabulario:

```text
1:1, 1:N, N:1, 0:1, 0:N, 1:0
MISSING, MULTIPLE_CANDIDATES, AMBIGUOUS
DIVERGENT_INCOMPATIBLE, EXPECTED_ABSENCE, UNSUPPORTED
```

Cardinalidad observada, mapping status, ambiguity, effective result e history son ejes separados.

### 7.4 Vocabulario de estados

| Eje / estado | Definición detector-only | Blocking |
| --- | --- | --- |
| `mapping_status=NO_CANDIDATES` | Cero candidatos elegibles, todavía requiere clasificación causal | Según `result_status`; nunca éxito automático |
| `mapping_status=UNIQUE_CANDIDATE` | Un candidato elegible; no seleccionado | No para reporting; sí para mapping/crosswalk hasta boundary autorizado |
| `mapping_status=MULTIPLE_CANDIDATES` | Más de un candidato elegible | Sí para mapping/crosswalk/resolver/migration/cutover |
| `result_status=CANDIDATE_EVALUATION_COMPLETE` | La generación/clasificación `0..N` terminó; no afirma selección | No por sí solo |
| `result_status=EXPECTED_ABSENCE` | Target nominal único y cancelación nueva válida demuestran supresión | No por sí solo |
| `result_status=MISSING` | Una entidad/target/outcome requerido por contrato no existe | Sí |
| `result_status=UNSUPPORTED` | Source reconocido sin transformación/semántica aprobada | Sí para mapping material |
| `result_status=DIVERGENT_INCOMPATIBLE` | Evidencia relacionada viola forma, contenido, cardinalidad, operación o maestros | Sí |
| `ambiguity_status=AMBIGUOUS` | No existe una interpretación única desde evidencia aprobada | Sí |
| `history_status=CURRENT_SNAPSHOT_ONLY` | Sólo el estado observado puede afirmarse | No por sí solo |
| `history_status=UNKNOWN_HISTORY` | El claim requiere historia no persistida | Sí para ese claim |
| `selection_status=NOT_SELECTED_BY_DETECTOR` | Boundary obligatorio del detector | No para detectar; impide usar output como mapping |

`EXACT_IDENTITY_EVIDENCE`, `APPROVED_EQUIVALENCE_EVIDENCE`, `CONTAINMENT` y `FIELD_MATCH` son
calificaciones de candidate evidence. No son selección final. `CONTAINMENT` y `FIELD_MATCH` nunca
elevan por sí solas un candidate a identidad exacta.

### 7.5 Invariante de múltiples candidatos y no heurísticas

```text
candidate_count > 1
AND no existe regla inequívoca, aprobada y aplicable
-> mapping_status = MULTIPLE_CANDIDATES
-> ambiguity_status = AMBIGUOUS
-> ambiguity_reason = REQUIRED / NON_EMPTY / REPRODUCIBLE
-> selection_status = NOT_SELECTED_BY_DETECTOR
-> blocking = true
-> blocked_capabilities incluye
   [MAPPING_SELECTION, CROSSWALK_PERSISTENCE, MATERIAL_RESOLVER,
    MIGRATION, CUTOVER]
```

Queda prohibido seleccionar por first, closest, latest, highest overlap, highest score, most
probable, orden alfabético, UUID, timestamp proximity o cualquier otra heurística no aprobada.
El orden de salida sólo puede ser determinista para reproducibilidad y nunca concede preferencia.

### 7.6 Candidate count cero

`candidate_count=0` no determina por sí solo `MISSING`.

| Contexto | Clasificación |
| --- | --- |
| APF `CANCELACION` válida + nominal única + cero efectivas | `EXPECTED_ABSENCE / SUPPRESSED` |
| APF `ADICION` en eje nominal | `NOT_APPLICABLE` |
| Target/outcome requerido y ausente | `MISSING / BLOCKER` |
| Historia requerida no observable | `UNSUPPORTED + UNKNOWN_HISTORY` |
| Forma legacy reconocida sin intención persistida | `UNSUPPORTED / UNKNOWN_INTENT`, posible ambigüedad |
| Cero tras filtros con evidencia incompatible | `DIVERGENT_INCOMPATIBLE` |

Una Reserva con asociación histórica demostrada conserva su `programmingTargetReference` cuando
una cancelación válida suprime la occurrence. El outcome actual es `EXPECTED_ABSENCE / SUPPRESSED`;
si la Reserva sigue confirmada y una regla operativa exige presencia, esa incompatibilidad se
reporta y bloquea en un eje separado. No se borra la asociación, no se cambia su identidad y no se
reclasifica la referencia como `MISSING`.

La razón normalizada mínima de la supresión válida es
`TARGET_NOMINAL_SUPPRESSED_BY_VALID_CANCELLATION`; se acompaña con ajuste, target nominal y regla
F2D versionada.

### 7.7 Result contract conceptual

La siguiente forma es normativa en semántica, no un JSON schema ni una clase autorizada:

```text
DetectorResult
  detector_run_identity: required
  evaluation_identity: required, stable within run
  detector_version: required
  rule_id: required
  rule_version: required
  evaluated_at: required

  source_system: LEGACY | NEW_DARK_LAUNCH
  source_atom_type: required typed atom
  source_identity: required
  source_snapshot_identity: required
  source_fingerprint: required
  source_observable_fields: normalized snapshot
  source_reservation_id: required exactly when source_atom_type = Reserva
  source_reservation_snapshot: required exactly when source_atom_type = Reserva;
    state/date/salon/instructor/activity, excluding source_reserved_subinterval
  source_reserved_subinterval: required exactly when source_atom_type = Reserva

  expected_target_atom_type: required
  declared_relation_cardinality: required
  generated_candidate_count: required, >= 0
  candidate_count: required, eligible candidate count >= 0
  candidates[]: required, all generated candidates including rejected evidence

  mapping_status: required
  result_status: CANDIDATE_EVALUATION_COMPLETE | EXPECTED_ABSENCE | MISSING |
    UNSUPPORTED | DIVERGENT_INCOMPATIBLE
  effective_result_status: PRESENT | EXPECTED_ABSENCE | NOT_APPLICABLE | MISSING | DIVERGENT
  effective_occurrence_count: required when applicable
  history_status: CURRENT_SNAPSHOT_ONLY | UNKNOWN_HISTORY | NOT_APPLICABLE
  ambiguity_status: AMBIGUOUS | NOT_AMBIGUOUS | NOT_APPLICABLE
  ambiguity_reason: required non-empty exactly when AMBIGUOUS
  selection_status: NOT_SELECTED_BY_DETECTOR
  selected_target_identity: absent; if transport requires it, null

  expected_absence_reason: required exactly for EXPECTED_ABSENCE
  unsupported_reason: required exactly for UNSUPPORTED
  blocking: required boolean
  blocking_status: BLOCKING | NON_BLOCKING
  blocked_capabilities[]: required when blocking

  F2D_contract_compatibility: F2D_CONTRACT_COMPATIBLE | F2D_AUTHORITY_CONFLICT
  provenance: required
  evidence_hash: required
  result_hash: required
```

`candidate_count` cuenta candidates `ELIGIBLE` después de filtros aprobados; `candidates[]`
conserva además los `REJECTED` para no borrar evidencia. Por tanto
`generated_candidate_count = size(candidates[])` y
`candidate_count = count(candidates[].eligibility_status == ELIGIBLE)`. La invariante `>1` usa
`candidate_count`.

Cada candidato contiene:

```text
CandidateEvidence
  candidate_identity: required
  candidate_target_reference: required when candidate represents a programming occurrence;
    equals candidate_identity and excludes source_reserved_subinterval
  candidate_type: required
  candidate_snapshot_identity: required
  candidate_fingerprint: required
  candidate_evidence: required typed list
  relationship_evidence_kind:
    EXACT_IDENTITY | APPROVED_EQUIVALENCE_RULE | CONTAINMENT | FIELD_MATCH
  normalized_observable_fields: required
  matching_dimensions[]: required
  mismatch_dimensions[]: required
  eligibility_status: ELIGIBLE | REJECTED
  rejection_reasons[]: required when REJECTED
  objective_score: optional explanatory evidence only
  candidate_evidence_hash: required
  provenance: required
```

Para una evaluación de Reserva, `source_reservation_id`, `source_reservation_snapshot` y
`source_reserved_subinterval` describen el hecho source. Cada
`candidates[].candidate_target_reference` identifica exclusivamente la occurrence aprobada por F2D;
el intervalo sólo aparece en evidence de matching/contención. Nunca se construye
`candidate_identity = occurrence reference + reserved interval`.

Dimensiones mínimas cuando apliquen: fecha, salón, instructor, actividad, serie, versión,
occurrence reference, rango half-open, vigencia, activo, tipo de ajuste, target nominal, resultado
efectivo y estado de maestros/operación. Provenance incluye source/schema fingerprint, IDs de
records, regla/version, business time context y hash de campos normalizados. No incluye PII no
necesaria.

### 7.8 Cierre D03 detector-only

```text
D03 detector-only: CLOSED_BY_THIS_DESIGN
Source atoms: CLOSED
Target atoms: CLOSED
Cardinalities: CLOSED
0..N generation: CLOSED
Candidate evidence: CLOSED
Multiple candidates: CLOSED / FAIL-CLOSED
No selection: CLOSED
Expected absence: CLOSED
Unsupported/provenance: CLOSED
Final mapping selection: DEFERRED / OUT_OF_SCOPE
Persisted crosswalk: DEFERRED / OUT_OF_SCOPE
```

## 8. D09 — semántica observable de `vigenteDesde` legacy

### 8.1 Hechos observables

| Hecho | Qué demuestra | Qué no demuestra |
| --- | --- | --- |
| `TurnoInstructor.id` | Identidad física de la fila actual | Identidad de serie o historia de versiones |
| `tipo=RECURRENTE`, `diaSemana`, rango y `activo` | Regla observable en el snapshot actual | Desde cuándo rigió ese contenido |
| `creado_en` | Instante técnico de inserción de la fila | `vigenteDesde` funcional; puede existir import/backfill |
| `actualizado_en` | Última escritura técnica detectada sobre la fila | Qué campos cambiaron, estados previos o `vigenteHasta` |
| `activo=false` | La fila está desactivada en el snapshot | Fecha/causa exacta de desactivación como historia funcional |
| Update de recurrente | Muta día/rango/instructores de la misma fila | Versiones anteriores; no se conserva before-image |
| Reemplazo de asignaciones | Delete total + insert de filas compuestas | Historia de asignaciones; no tienen timestamps propios |
| Validación `[hoy,+infinito)` | Política del writer al crear/mover hoy | Vigencia histórica persistida del Turno |

La única ventana afirmable sin otra fuente es `CURRENT_SNAPSHOT_ONLY`: el conjunto y contenido
observados en un snapshot coherente. No puede afirmarse que el contenido actual haya regido desde
`creado_en`, que haya terminado en `actualizado_en` o que una Reserva pasada usara esa misma
versión semántica.

### 8.2 Contrato detector-only D09

1. Para claims exclusivamente sobre el snapshot actual, emitir
   `history_status=CURRENT_SNAPSHOT_ONLY` y conservar fila, activo, tipo, día/fecha, rango,
   relaciones y timestamps técnicos como evidence.
2. Si el mapping requiere saber el contenido del Turno/asignación en un instante anterior a la
   observación o antes de una mutación no historiada, emitir `history_status=UNKNOWN_HISTORY`,
   `result_status=UNSUPPORTED`, razón `LEGACY_FUNCTIONAL_VALIDITY_NOT_PERSISTED` y `blocking=true`
   para ese mapping.
3. Si varias reconstrucciones siguen siendo compatibles con la evidencia, añadir
   `ambiguity_status=AMBIGUOUS` y `ambiguity_reason=LEGACY_HISTORY_HAS_MULTIPLE_PLAUSIBLE_STATES`.
4. `creado_en` y `actualizado_en` pueden conservarse como technical provenance. Nunca se renombran
   ni transforman en `vigenteDesde`/`vigenteHasta`.
5. Una fuente externa futura sólo puede ampliar historia si está nombrada, autorizada y tiene
   provenance verificable; su política pertenece a data audit, no a este checkpoint.

No se reconstruye historia heurísticamente por recencia, timestamps, orden de UUID, fecha de
Reserva ni comparación con el estado nuevo.

### 8.3 Decisión D09

```text
D09 detector-only: CLOSED_BY_THIS_DESIGN
Observable semantics: CURRENT_SNAPSHOT_ONLY
Functional legacy history: NOT_PERSISTED / UNKNOWN_HISTORY
No invented history: REQUIRED
Material history policy/data audit: DEFERRED
Human decision required for this detector contract: NO
```

Cerrar D09 significa que el detector sabe qué afirmar y cómo fallar cerrado; no significa que una
historia inexistente haya sido reconstruida.

## 9. D10 — semántica puntual legacy y modelo nuevo

### 9.1 Formas legacy observables

#### `TurnoInstructor.EXCEPCION`

- source identity: `turno_instructor.id` y fecha exacta;
- porta salón, rango, uno o más instructores y cero o más filas instructor/actividad;
- el writer la valida como bloque puntual y rechaza traslape con recurrentes/excepciones;
- `ReservaService` interpreta la existencia de una o más excepciones del instructor/salón/fecha
  como sustitución global de todos sus recurrentes de ese día;
- no persiste target recurrente, serie, tipo de intención, before-image ni relación individual;
- no puede saberse si pretendía adición, reemplazo o una forma legacy distinta.

Resultado detector: generar candidatos por fecha/dimensiones y conservar evidence, pero emitir
`UNSUPPORTED / UNKNOWN_INTENT`; si hay varias interpretaciones/candidatos, además `AMBIGUOUS` con
razón obligatoria. Nunca `EXCEPCION -> REEMPLAZO` ni `EXCEPCION -> ADICION` automáticamente.

#### `TurnoInstructor.CANCELACION`

- source identity: `turno_instructor.id` y fecha exacta;
- persiste salón, rango e instructores/asignaciones, pero el writer no valida horario ni traslape;
- para reservas, cualquier cancelación del instructor/salón/fecha anula todo el día, sin usar su
  rango;
- no persiste qué recurrente(s) pretendía cancelar, serie ni target individual.

Resultado detector: generar `0..N` nominales plausibles del mismo salón/instructor/fecha y
conservar el rango como evidence no targeteante. Emitir `UNSUPPORTED / UNKNOWN_INTENT`; cero no es
`EXPECTED_ABSENCE` y uno no prueba target. Varias nominales exigen `MULTIPLE_CANDIDATES +
AMBIGUOUS + NOT_SELECTED_BY_DETECTOR + BLOCKER`.

### 9.2 Formas nuevas completas

| Tipo APF | Target nominal | Source identity | Output identity | Effective result | Card. | Ausencia / error | Temporalidad/evidence |
| --- | --- | --- | --- | --- | --- | --- | --- |
| `CANCELACION` | Obligatorio y único `(serieId,fecha)` | `ajusteId` + tipo + target | Ninguna efectiva; nominal conserva referencia | `EXPECTED_ABSENCE / SUPPRESSED` | target 1:1; efectiva 1:0 | target 0=`MISSING`; >1=`AMBIGUOUS/INVARIANT`; efectiva presente=`DIVERGENT` | Fecha atómica; no porta resultado; nominal y ajuste son evidence |
| `REEMPLAZO` | Obligatorio y único `(serieId,fecha)` | `ajusteId` + target + snapshot | `(SERIE_ASIGNACION,serieId,fecha)` | Una efectiva con snapshot final | 1:1 → 1:1 | target/outcome ausente=`MISSING` o `DIVERGENT`; múltiples bloquean | Puede cambiar salón/instructor/actividad/rango; conserva referencia; salón final gobierna |
| `ADICION` | No aplica | `ajusteId` + fecha + snapshot | `(AJUSTE,ajusteId,fecha)` | Una efectiva sin template | 0:1 nominal (`NOT_APPLICABLE`); 1:1 efectiva | outcome ausente=`MISSING`/`DIVERGENT`, no supresión | Fecha de adición activa inmutable; recrear usa UUID nuevo |

Una omisión posterior por horario o maestros no convierte reemplazo/adición en cancelación ni en
`EXPECTED_ABSENCE`: es `DIVERGENT_INCOMPATIBLE` respecto del outcome requerido.

### 9.3 Matriz legacy/nuevo puntual

| Legacy form | New form comparada | Similitudes observables | Diferencias/target semantics | ¿Mapping inferible? | Status fail-closed |
| --- | --- | --- | --- | --- | --- |
| EXCEPCION | CANCELACION | Fecha/salón/instructor pueden coincidir | Legacy aporta rango/resultado aparente; nueva cancelación no porta resultado y targetea serie | No | `UNSUPPORTED / UNKNOWN_INTENT` |
| EXCEPCION | REEMPLAZO | Puede producir presencia puntual con snapshot parecido | Legacy no identifica nominal; writer y reader legacy discrepan en sustitución | No | `UNSUPPORTED`; `AMBIGUOUS` si candidates |
| EXCEPCION | ADICION | Puede parecer una presencia puntual sin template | Legacy no persiste si había target ni identidad propia de adición equivalente | No | `UNSUPPORTED`; `AMBIGUOUS` si candidates |
| CANCELACION | CANCELACION | Fecha/salón/instructor y outcome de indisponibilidad pueden parecerse | Legacy cancela ámbito de día en reader y no targetea serie; nueva suprime una nominal exacta | No automáticamente | `UNSUPPORTED / UNKNOWN_INTENT`; multiple candidates bloquea |
| CANCELACION | REEMPLAZO | Ninguna equivalencia de intención demostrada | Legacy no porta snapshot resultado ni target individual | No | `UNSUPPORTED / DIVERGENT_INCOMPATIBLE` |
| CANCELACION | ADICION | Ninguna equivalencia de intención demostrada | Semánticas opuestas: indisponibilidad vs presencia añadida | No | `UNSUPPORTED / DIVERGENT_INCOMPATIBLE` |

Coincidencia de outcome o campos no demuestra identidad de intención. El detector conserva
candidatos y provenance; no convierte formas legacy a tipos APF.

### 9.4 Operación del salón y reservas

```text
SalonHorarioExcepcion
= operación efectiva del salón en una fecha
!= AjusteProgramacionFecha.CANCELACION
!= TurnoInstructor.CANCELACION
```

El pipeline nuevo evalúa el horario del salón final después de aplicar ajustes. Un cierre puede
omitir programación efectiva, pero no reescribe el ajuste ni representa su intención.

Disponibilidad, programación y Reserva persistida son hechos separados:

- `ReservaService` consulta horario efectivo y programación legacy sólo al crear;
- cancelar/sustituir programación no actualiza automáticamente filas `reserva`;
- `Reserva.cancelar` es una operación explícita que sólo cambia su propio estado;
- por tanto una programación suprimida y una Reserva confirmada asociada deben reportarse como
  incompatibilidad en el eje operativo; el detector no repara ni cancela, y la supresión no borra
  la `programmingTargetReference` histórica demostrada.

### 9.5 Decisión D10

```text
D10 detector-only: CLOSED_BY_THIS_DESIGN
Legacy EXCEPCION: OBSERVABLE + UNKNOWN_INTENT / UNSUPPORTED
Legacy CANCELACION: OBSERVABLE + UNKNOWN_INTENT / UNSUPPORTED
New CANCELACION/REEMPLAZO/ADICION: CHARACTERIZED
Automatic legacy intent inference: FORBIDDEN
Productive mapping: NOT_MATERIALIZED / NOT_AUTHORIZED
Human decision required for detector behavior: NO
Human disposition of individual ambiguous cases: DEFERRED
```

## 10. D11 — arquitectura conceptual detector-only

### 10.1 Ownership lógico

El futuro subconjunto pertenece lógicamente a un módulo no productivo de transición/auditoría de
programación dentro del monolito modular. No pertenece a `ReservaService`,
`TurnoInstructorService`, a una futura ruta productiva de `ProgramacionEfectiva` ni a
`ubicaciones`.

El módulo tiene dos componentes conceptuales separados y un coordinador:

| Ownership | Responsabilidad | Input | Output | No puede |
| --- | --- | --- | --- | --- |
| Source observer / snapshotter | Leer representaciones allowlisted y fijar metadata/fingerprints | Source handles read-only + scope | Typed immutable snapshots | Mutar, completar historia |
| Identity atom projector | Proyectar A–L sin conflar ID, versión, occurrence o snapshot | Snapshots | Typed atoms | Inferir mapping |
| Candidate/evidence generator | Aplicar reglas versionadas y conservar `0..N` | Source atom + candidate universe + context | CandidateEvidence[] | Clasificar por heurística o seleccionar |
| Semantic classifier | Calcular cardinalidad/status/ambiguity/blocking/F2D compatibility | Candidate set + context | DetectorResult | Persistir target o cambiar authority |
| Report/metrics projector | Emitir reporte inmutable y métricas derivadas | DetectorResult[] | Artefacto no autoritativo | Escribir DB productiva o ocultar casos |
| Read-only run coordinator | Validar input, coherencia de snapshot, lifecycle y errores | DetectorRunRequest | Run result o error tipado | Resolver mapping, migrar, enforce fence |

El auditor es el coordinador más observers/classifier/report. El candidate generator es un
componente separado dentro del mismo módulo para que la generación de evidencia no decida su propia
clasificación ni selección. La responsabilidad detector-only termina al emitir resultados,
evidence y métricas no autoritativas.

Consumers permitidos del output: auditor humano/documental, data-audit futuro autorizado,
reporting interno y un boundary futuro de selección/crosswalk expresamente autorizado. Ningún
consumer productivo puede usarlo como fallback o autoridad implícita.

### 10.2 Dependencias permitidas y prohibidas

Permitidas read-only:

- ports/queries de `calendario` para TI, asignaciones y reservas;
- ports/queries de `programacion` para series, versiones, nominales, ajustes y efectivas;
- horario/maestros como evidence y contexto;
- reglas F2D versionadas y clock/contexto temporal explícito;
- sink de reporte in-memory o artefacto inmutable no productivo autorizado por el futuro handoff.

Prohibidas:

- llamar writers/services mutantes;
- repository `save`, `persist`, `flush`, DDL/DML o locks con intención de escritura;
- dependencia desde flujos productivos hacia el detector;
- `selected_target`, persisted crosswalk, resolver material, fence/enforcement, migrador;
- fallback entre legacy y nuevo;
- publicar PII innecesaria.

### 10.3 Input contract

```text
DetectorRunRequest
  run_identity: required
  detector_version: required
  rule_catalog_version: required
  requested_at: required
  business_zone: required
  evaluation_date_or_window: required, explicit
  scope:
    source_atom_types[]
    salon_ids[] or declared all-within-authorized-source
    instructor_ids[] optional filter
    reservation_ids[] optional filter
  source_snapshot:
    source_name: required when material data exists
    source_system: LEGACY | NEW_DARK_LAUNCH
    schema_fingerprint: required
    flyway_fingerprint: required when applicable
    snapshot_identity/timestamp: required
    read_only_authorization_identity: required for material data
  legacy_sources[]:
    typed Turno / legacy assignment / Reserva representations
    technical timestamps retained as technical evidence
  new_candidate_universe:
    block series + assignment series
    assignment versions and vigencias
    nominal occurrences
    adjustments CANCELACION/REEMPLAZO/ADICION
    programming target references
    effective occurrence outcomes/snapshots, separados de la referencia
  date_time_context:
    explicit date/window, zone, day convention, [start,end) convention
  shared_dimensions:
    salon, instructor, activity master snapshots
    effective operating hours when applicable
  adjustment_context:
    adjustment identity/type/target/result/active + provenance
  reservation_context:
    reservation_identity: reserva.id
    reservation_snapshot: state/date/salon/instructor/activity + provenance
    reserved_subinterval: [horaInicio,horaFin), separado de target identity
  F2D_authority_constraints:
    approved contract version/fingerprint
    identities and composition invariants
```

Validaciones de entrada:

1. scope y fecha/contexto no nulos y coherentes;
2. source tipado y snapshot estable;
3. autorización read-only explícita si existe data source material;
4. reglas/F2D versionadas;
5. intervalos positivos half-open y fechas/vigencias sin reinterpretación;
6. abortar si cambia fingerprint/watermark durante la evaluación.

El detector no inventa inputs faltantes. Un source inexistente o no autorizado impide una corrida
material, no el contrato documental.

### 10.4 Output/result contract

El output es una colección inmutable de `DetectorResult` de 7.7 más:

```text
DetectorRunResult
  run_identity
  run_status: COMPLETED | COMPLETED_WITH_BLOCKERS | ABORTED
  input_fingerprint
  detector/rule/F2D contract versions
  scope_observed
  sources_inspected
  source_count
  results[]
  metrics
  operational_errors[]
  authority_conflicts[]
  started_at/completed_at
  report_hash
```

No contiene mappings seleccionados. Todo resultado conserva source identity, candidates,
matching/mismatch dimensions, mapping/result/ambiguity/history/selection/blocking states,
unsupported/expected-absence reason, provenance, hashes y compatibilidad F2D.

Para cada resultado cuyo source sea Reserva, el contrato representa separadamente:

```text
source_reservation_id
source_reservation_snapshot
source_reserved_subinterval
candidates[].candidate_target_reference
candidates[].candidate_evidence
mapping_status
ambiguity_status
selection_status
blocking_status
```

`candidate_target_reference` es exclusivamente `ReferenciaOcurrencia`; el snapshot de Reserva y
su subintervalo no se concatenan, hashean ni reinterpretan como identidad del target. Cancelación,
reemplazo o cambios de atributos efectivos pueden cambiar el outcome/snapshot evaluado sin cambiar
una referencia histórica ya demostrada.

Determinismo:

- mismo snapshot + mismas reglas + mismo contexto produce los mismos atoms, candidates, statuses y
  hashes semánticos;
- `run_identity` y timestamps son provenance y no cambian identidad lógica;
- el orden estable de reporte no tiene significado de preferencia.

### 10.5 Error contract

| Clase | Naturaleza | Ejemplos | Comportamiento |
| --- | --- | --- | --- |
| `DOMAIN_RESULT` | Resultado semántico esperado | unique/no candidates, expected absence, missing, divergent | Emitir DetectorResult; bloquear según status |
| `AMBIGUOUS_RESULT` | Resultado semántico | múltiples candidatos, intención/historia plural | Conservar todos; razón obligatoria; no seleccionar |
| `UNSUPPORTED_RESULT` | Resultado semántico | historia/intención legacy no persistida | Emitir reason/provenance; no inferir |
| `AUTHORITY_CONFLICT` | Stop de autoridad/seguridad | Contradicción material con F2D o canon | Abortar claim/run afectado; blocker; no reinterpretar |
| `INPUT_INVALID` | Fallo de contrato | fecha/rango/tipo/fingerprint incoherente | No producir éxito parcial para unidad afectada |
| `ENVIRONMENT_FAILURE` | Fallo operacional | source inaccesible, snapshot cambia, I/O de reporte | Run `ABORTED`; no convertir en cero anomalías |

Un fallo operacional no es un mapping status positivo, no es `EXPECTED_ABSENCE` y no reduce
conteos. Si el snapshot cambia, la evaluación afectada se invalida completa de forma fail-closed.

### 10.6 Observabilidad mínima

Métricas por run y, cuando aplique, por atom kind/salón/fecha sin PII:

```text
sources_inspected
source_count_by_atom_type
generated_candidates_total
candidate_counts_0_1_multiple
zero_candidate_count
multiple_candidate_count
ambiguous_count
unsupported_count
expected_absence_count
unexpected_missing_count
divergent_incompatible_count
unknown_history_count
legacy_semantic_unknown_count
reservation_identity_unresolved_count
reservation_target_suppressed_conflict_count
F2D_authority_conflict_count
operational_error_count_by_class
results_by_F2D_compatibility
detector_version / rule_version / source_fingerprint
```

`expected_absence_count` y `unexpected_missing_count` son métricas distintas. Cobertura significa
scope observado por el detector; nunca se presenta como cobertura de mappings reales ni como
autorización de cutover.

### 10.7 Cierre D11 detector-only

```text
D11 detector-only: CLOSED_BY_THIS_DESIGN
Ownership: CLOSED
Inputs: CLOSED
Outputs/results: CLOSED
Errors: CLOSED
Observability: CLOSED
F2D compatibility: CLOSED
Future executor architecture: CLOSED FOR DETECTOR-ONLY
Selection/crosswalk/resolver/fence/migration architecture: OUT_OF_SCOPE / DEFERRED
```

## 11. Data source y límites sin datos

```text
DATA_SOURCE_STATUS: DATA_SOURCE_NOT_AVAILABLE
DATA AUDIT DESIGN: APPROVED BY F2E.1
DATA AUDIT EXECUTION: NOT_PERFORMED
MATERIAL DATA ACCESS: NOT_AUTHORIZED
SQL: NOT_EXECUTED
```

La evidencia disponible son canónicos, código, migraciones y tests. Configuración, posibles
volúmenes o URLs no identifican por sí mismos una fuente autorizada. Por tanto este checkpoint no
afirma:

- conteos reales de sources/candidates/anomalías;
- cobertura real;
- mappings individuales;
- distribución histórica;
- ausencia de anomalías;
- equivalencia de datos legacy/nuevo.

Una corrida futura exige fuente nombrada, custodio/scope, autorización read-only, snapshot y
garantía verificable de no mutación. Sin ellos debe producir `ENVIRONMENT_FAILURE /
SOURCE_NOT_AUTHORIZED_OR_AVAILABLE`, no datos fabricados.

## 12. D08 expresamente diferida

```text
D08: DEFERRED
Cross-salon cohort/protocol: NOT_RESOLVED_BY_THIS_UNIT
Fence design extension: OUT_OF_SCOPE
Fence persistence: NOT_MATERIALIZED
Fence enforcement: NOT_AUTHORIZED
MIGRANDO: NOT_AUTHORIZED
NUEVA: NOT_AUTHORIZED
```

El detector futuro puede reportar evidencia observable cross-salon sin decidir cohorte, persistir
estado o hacer enforcement. Este checkpoint no reabre ni resuelve el fence.

## 13. Reglas fail-closed transversales

```text
Ambiguous -> NO seleccionar
Multiple candidates -> conservar todos / NO seleccionar
Unknown history -> NO inventar
Unsupported -> NO inferir
Unknown intent -> NO convertir a tipo nuevo
Containment/field match -> evidencia, NO identidad
F2D conflict -> NO reinterpretar / BLOCKER
Expected absence -> NO clasificar como missing
Missing requerido -> anomalía / BLOCKER
No data -> NO fabricar resultados
Operational failure -> NO reportar éxito ni cero anomalías
Reservation/programming conflict -> NO mutar Reserva
```

Los blockers semánticos impiden selección, persistencia, resolver material, migration y cutover
para el scope afectado. No impiden detectar, clasificar, reportar o medir conservando toda la
evidencia.

## 14. Decisiones cerradas, diferidas y blockers

### 14.1 Cierres de esta unidad

| Decisión | Estado | Cierre |
| --- | --- | --- |
| D03 | `CLOSED_BY_THIS_DESIGN` — detector-only | Atoms, 0..N, cardinalidad, evidence, status, ambiguity, non-selection, provenance |
| D04 | `CLOSED_BY_THIS_DESIGN` | Target de programación = referencia de occurrence; identidad de Reserva = `reserva.id`; subintervalo = snapshot de consumo separado |
| D09 | `CLOSED_BY_THIS_DESIGN` — detector-only | Snapshot actual observable; historia funcional desconocida se clasifica fail-closed |
| D10 | `CLOSED_BY_THIS_DESIGN` — detector-only | Legacy observable sin intención inferida; tres formas nuevas caracterizadas |
| D11 | `CLOSED_BY_THIS_DESIGN` — detector-only | Ownership, inputs, outputs, errors, metrics y boundary cerrados |
| D08 | `DEFERRED` | Fuera del scope autorizado |

### 14.2 Decisiones diferidas

- mappings individuales y disposición humana de casos históricos ambiguos;
- data audit material y conteos/coverage reales;
- política/migración de reservas pasadas o canceladas;
- persisted crosswalk y mapping selection;
- resolver material/comparativo;
- D08, cohorte cross-salon, fence y enforcement;
- normalización/migración y batch concreto;
- hardening de writers/masters;
- writer/reader/consumer switching;
- `MIGRANDO`, `NUEVA`, cutover y cambio de autoridad;
- cualquier eventual materialización física de asociación o `Sesion`, fuera de scope y no
  requerida por el contrato lógico cerrado.

### 14.3 Blockers vigentes

No hay blocker de autoridad ni decisión humana que impida presentar este checkpoint a audit fresh.
Permanecen blockers para fases materiales:

| Blocker | Afecta | Resolution path |
| --- | --- | --- |
| `DATA_SOURCE_NOT_AVAILABLE` | Data audit material | Fuente nombrada + autorización read-only + snapshot |
| Mappings reales no evaluados | Crosswalk/migration/cutover | Ejecutar detector/data audit bajo autoridad futura |
| Ambigüedad/historia/intención legacy por record | Mapping material | Disposición humana o evidence externa; nunca heurística |
| Crosswalk y selector no materializados | Resolver/migration | Diseño/gate/handoff separados |
| D08/fence diferidos | Fence/MIGRANDO/cutover | Unidad de diseño y gate específicos |
| Reservas sin referencia persistida | Migration/cutover | Expand/contract futuro conforme a D04, con mapping inequívoco |
| Authority material ausente | Cualquier implementación/migration | Nuevo handoff auditado y activo |

### 14.4 Human decisions

```text
Human decision required to complete current detector-only design: NO
Human decision required for D04: NO
Human dispositions deferred for actual ambiguous records/history: SI, when evidence produces them
```

Clasificar de forma segura `UNSUPPORTED`, `UNKNOWN_HISTORY`, `UNKNOWN_INTENT`, `AMBIGUOUS` o
`MISSING` no requiere una decisión humana en esta unidad. Elegir o reparar casos concretos sí puede
requerirla en una fase autorizada posterior.

## 15. Candidate future scope — no autorización

Sólo después de un audit fresh independiente con `P0=0 / P1=0` y un nuevo handoff específico,
podría proponerse una unidad `IMPLEMENTATION_READ_ONLY` con este scope máximo:

1. tipos inmutables internos para atoms, candidates, evidence, result, errors y run report, con
   `source_reservation_id`, reservation snapshot, reserved subinterval y candidate target reference
   como campos semánticamente separados;
2. adapters read-only para snapshots legacy y universo nuevo;
3. identity atom projector;
4. candidate/evidence generator con reglas versionadas de esta unidad, cuya candidate identity de
   occurrence sea sólo `ReferenciaOcurrencia` y conserve el subintervalo como source/evidence;
5. classifier fail-closed y F2D authority guard;
6. read-only coordinator;
7. métricas y reporte no autoritativo, sin PII innecesaria;
8. tests unitarios/de caracterización/arquitectura que demuestren `0..N`, no selección,
   expected absence, unknown history/intent, errores y no mutación.

El handoff futuro debe fijar allowlist física y, si accede a datos materiales, nombrar source y
autorización read-only. Debe prohibir físicamente repository writes, controllers productivos y
dependencia desde flujos productivos.

Fuera aun de ese candidate scope:

```text
selected mapping
persisted crosswalk/table
material equivalence resolver
fence/enforcement
data mutation/normalization/migration
Reserva schema/backfill
productive API/consumer
writer/reader switch
MIGRANDO/NUEVA/cutover/authority change
```

```text
Candidate type: IMPLEMENTATION_READ_ONLY
Candidate status: PROPOSED / NOT_AUTHORIZED
Implementation authorized by this checkpoint: NO
New handoff required: SI
Prerequisite gate: FRESH_INDEPENDENT_DESIGN_DOCUMENT_AUDIT
```

## 16. Autoridad y exit conditions

Estado que este checkpoint preserva:

```text
F2D.2: CLOSED
F2E.1: DESIGN / PREPARATION APPROVED / CLOSED
Runtime: DARK_LAUNCH
Productive: NOT_PRODUCTIVE
Product authority: TurnoInstructor / LEGACY_VIVO / PRODUCTIVO
Programación nueva: IMPLEMENTADO_NO_PRODUCTIVO
Cutover: false
D08/fence: DEFERRED / NOT_MATERIALIZED
Implementation: NOT_AUTHORIZED
Migration: NOT_AUTHORIZED
```

Exit conditions de materialización documental:

- checkpoint único autocontenido;
- D03/D04/D09/D10/D11 detector-only clasificados sin decisiones vagas;
- D08 preservada diferida;
- compatibilidad F2D contrastada;
- no selección, no persisted crosswalk, no resolver y no fence;
- no data audit material ni resultados inventados;
- no código/tests/migraciones/datos modificados;
- `HEAD` y staging invariantes y único touched path igual a este checkpoint;
- whitespace sin trailing spaces;
- relectura completa y contraste final con handoff, F2E.1, F2D y canónicos;
- siguiente gate exclusivamente `FRESH_INDEPENDENT_DESIGN_DOCUMENT_AUDIT`.

## 17. Cierre de lifecycle posterior

Una auditoría fresh e independiente `DESIGN_AUDITOR / DOCUMENT_AUDITOR`, ejecutada en modo
`READ_ONLY` sobre la branch `operacion/excepciones-horario-fecha` y el HEAD
`258c21b0383579a6734bc20f87b0c84008cc9bdf`, verificó la corrección final D04 y reportó:

```text
Previous P1: 1
P1 D04 target/snapshot separation closed: SI
New P0: 0
New P1: 0
New P2: 0
IDENTITY_LEGACY_DETECTOR_DESIGN_GATE: PASS
READY_FOR_DESIGN_CLOSURE: SI
Requires human decision: NO
```

El resultado completo queda persistido en
`auditoria/reviews/F2E-IDENTIDAD-DETECTOR-REVIEW-DISENO.md`. Por tanto, esta unidad
`DESIGN / RESEARCH` queda `DESIGN_APPROVED / CLOSED`.

`CLOSED` significa exclusivamente que el diseño de identidad, semántica legacy y detector-only
fue completado y auditado. No significa detector implementado, crosswalk persistido, resolver o
fence materializado, migración, cutover ni cambio de autoridad. `IMPLEMENTATION_READ_ONLY` sigue
siendo sólo `CANDIDATE / FUTURE / NOT_AUTHORIZED` y requiere un nuevo scope/handoff auditado y
activado.

Este cierre de lifecycle es una materialización del DOCUMENTER y no se autoaprueba. Antes de
`git add`, commit o push debe superar un audit documental fresh e independiente.
