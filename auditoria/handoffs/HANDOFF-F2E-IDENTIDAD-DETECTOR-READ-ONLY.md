# FeelingPilates — Handoff F2E / identidad, semántica legacy y detector-only

Handoff status: `APPROVED / ACTIVE / AUTHORIZED_FOR_DESIGN_RESEARCH`

Target unit: `F2E / cierre de identidad, semántica legacy y contrato detector-only`

Type: `DESIGN / RESEARCH`

Execution profile: `DESIGN / READ_ONLY_RESEARCH`

Materialization role: `DESIGN_EXECUTOR / RESEARCHER`

Este handoff materializa el contrato documental para una unidad mínima de investigación y diseño
read-only. El audit fresh e independiente, persistido en
`auditoria/reviews/HANDOFF-F2E-IDENTIDAD-DETECTOR-READ-ONLY-REVIEW.md`, reportó
`P0=0 / P1=0 / P2=0` y aprobó su activación. Por tanto, la unidad está activa únicamente para
iniciar su trabajo `DESIGN / RESEARCH`; esa activación no inicia ni aprueba el diseño de la unidad,
no crea su checkpoint, no autoriza implementación y no sustituye el audit fresh e independiente
del output documental futuro.

La unidad futura, sólo después de la aprobación y activación documental competente de este
handoff, podrá observar evidencia física, diseñar generación de candidatos `0..N`, clasificar
resultados y cerrar el contrato conceptual del detector. No podrá seleccionar mappings reales,
persistir crosswalk, implementar resolver o fence, modificar datos, migrar, activar programación
nueva ni cambiar autoridad productiva.

## 1. Autoridad y derivación física

La autoridad de este handoff procede exclusivamente del repositorio y de F2E.1 cerrada. No procede
de la numeración, del chat ni de memoria conversacional.

Fuentes que determinan el scope:

- `auditoria/ESTADO-ACTUAL.md` registra F2E.1 como `DESIGN/PREPARATION APPROVED / CLOSED`, ningún
  handoff activo, `DATA_SOURCE_NOT_AVAILABLE`, dark launch no productivo y autoridad legacy;
- `auditoria/fase-2e-preparacion-migracion-controlada.md`, sección 26.2, deriva una Candidate Unit A
  de cierre de decisiones y arquitectura `DESIGN/READ-ONLY` antes de cualquier materialización;
- `auditoria/reviews/F2E.1-REVIEW-DISENO-PREPARACION.md` confirma el gate final fresh
  `P0=0 / P1=0 / P2=0` sin autorizar F2E.2 ni implementación;
- `auditoria/fase-2d1-diseno-ajustes-programacion-fecha.md` y
  `auditoria/reviews/F2D.1.2-RE-REVIEW-FINAL.md` contienen el contrato de diseño F2D.1 aprobado
  que esta unidad debe preservar y no puede reformular arbitrariamente;
- `auditoria/fase-2d2-implementacion-dark-launch-ajustes-programacion-fecha.md` y
  `auditoria/reviews/F2D.2-REVIEW-DOCUMENTAL.md` demuestran la materialización física y el cierre
  documental F2D.2 en dark launch, sin sustituir la autoridad del diseño F2D.1;
- `auditoria/handoffs/HANDOFF-F2E-PREPARACION.md` está `CLOSED / HISTORICAL` y no autoriza una
  unidad posterior por sí solo;
- los canónicos preservan `TurnoInstructor` como única autoridad productiva y la programación
  nueva como `IMPLEMENTADO_NO_PRODUCTIVO / DARK_LAUNCH`.

F2E.1 dejó `D03`, `D04`, `D08`, `D09`, `D10` y `D11` como
`BLOCKING_FOR_NEXT_GATE`. Esta unidad toma un subconjunto mínimo y seguro de Candidate Unit A:

```text
D04: IN_SCOPE
D03: IN_SCOPE — DETECTOR_ONLY
D09: IN_SCOPE — OBSERVABLE_SEMANTICS
D10: IN_SCOPE — OBSERVABLE_SEMANTICS
D11: IN_SCOPE — DETECTOR_ONLY
D08: DEFERRED — FENCE EXCLUDED
```

No se denomina ni autoriza `F2E.2`. La numeración no crea autoridad.

## 2. Baseline de materialización

```text
Repository: /Users/jesusaldaircruzortiz/Desktop/Feelingpilates/feelingpilates
Branch: operacion/excepciones-horario-fecha
HEAD: d1fa018a333bbd6a6ee375cd5ab24863b4162b4a
Working tree inicial: CLEAN
Staging inicial: VACÍO
Handoff funcional activo previo: NINGUNO
Data source: DATA_SOURCE_NOT_AVAILABLE
```

Este snapshot sólo prueba el corte de creación del handoff. El audit del handoff y el inicio
eventual de la unidad requieren cada uno un pre-flight fresh; no pueden reutilizar este baseline
como sustituto de evidencia actual.

## 3. Estado e invariantes preservados

```text
F2D.2: CLOSED
F2E.1: DESIGN / PREPARATION APPROVED / CLOSED
F2E.1 design/documentation gate: PASS
F2E.1 data audit design: APPROVED
F2E.1 data audit execution: NOT_PERFORMED
Data source: DATA_SOURCE_NOT_AVAILABLE
Active handoff before this materialization: NINGUNO
Implementation: NOT_AUTHORIZED
Migration: NOT_AUTHORIZED
Cutover: false
Runtime: DARK_LAUNCH
Productive: NOT_PRODUCTIVE
Authority: TurnoInstructor / LEGACY_VIVO / PRODUCTIVO
```

La existencia de `BloqueProgramacion`, `Asignacion`, `AjusteProgramacionFecha`,
`ProgramacionNominal` o `ProgramacionEfectiva` no les concede autoridad productiva.

La autoridad de diseño, la evidencia de materialización y el código físico son fuentes
complementarias que deben contrastarse:

```text
F2D.1 + review final -> decisiones de diseño aprobadas
F2D.2 + review documental -> materialización y cierre dark-launch demostrados
código/migraciones/tests -> evidencia física de implementación
```

Ninguna de esas fuentes sustituye unilateralmente a las otras. En particular, código físico no es
la única fuente de diseño. Una divergencia material no se corrige silenciosamente ni se resuelve
prefiriendo el comportamiento implementado: se registra como `AUTHORITY_CONFLICT / BLOCKER` para
el claim afectado.

## 4. Propósito exacto

La unidad debe cerrar el contrato documental necesario para que una futura implementación
read-only, autorizada por otro handoff, pueda:

```text
READ
OBSERVE
GENERATE 0..N CANDIDATES
PRESERVE EVIDENCE
CLASSIFY
REPORT
METRIC
FAIL CLOSED
```

La unidad debe resolver mediante evidencia física:

1. la identidad target futura de `Reserva` (`D04`);
2. el contrato de generación y clasificación de candidatos, sin selección (`D03 detector-only`);
3. la semántica observable de vigencia e historia de `TurnoInstructor` (`D09`);
4. la semántica observable de excepción y cancelación legacy frente al modelo nuevo (`D10`);
5. la arquitectura conceptual cerrada del futuro auditor/generador/métricas (`D11 detector-only`).

El producto de la unidad es diseño auditado. No es código, mapping de registros, auditoría material
de datos, migración ni activación.

## 5. Entry conditions de la unidad futura

Antes de ejecutar la unidad, el agente debe verificar físicamente y registrar:

- F2E.1 `CLOSED`;
- design/documentation gate F2E.1 `PASS`;
- F2D.2 `CLOSED`;
- branch y `HEAD` fresh;
- staging y working tree clean o baseline conocido y expresamente autorizado;
- `TurnoInstructor` como autoridad productiva vigente;
- runtime `DARK_LAUNCH`;
- estado productivo `NOT_PRODUCTIVE`;
- `cutover=false`;
- ningún handoff previo activo incompatible;
- este handoff auditado fresh, aprobado y activado antes de iniciar la unidad;
- canónicos, checkpoint F2E.1, review F2E.1 y evidencia física coherentes;
- lectura completa, antes de analizar o cerrar decisiones, del contrato F2D.1, su review final, el
  checkpoint F2D.2 y su review documental enumerados en la sección 7;
- contraste explícito entre diseño F2D.1, cierre/materialización F2D.2 y código físico, conservando
  cualquier divergencia como `AUTHORITY_CONFLICT / BLOCKER` en vez de normalizarla;
- ningún source de datos asumido;
- scope de escritura limitado al checkpoint único de la sección 20.

Si falla una condición material de autoridad, coherencia o seguridad, la unidad no inicia. La falta
de Docker, la ausencia de una fuente material o la prohibición de implementar no son por sí solas
`HUMAN_STOP` cuando el diseño puede completarse read-only.

## 6. Workflow profile y gates

```text
WORKFLOW_PROFILE: F2E_IDENTITY_LEGACY_SEMANTICS_DETECTOR_ONLY_DESIGN
RESEARCH: READ_ONLY
DOCUMENT MATERIALIZATION: ONE CHECKPOINT ONLY
PRODUCT MATERIALIZATION: NOT_APPLICABLE
IMPLEMENTATION GATE: NOT_APPLICABLE
TESTS GATE: NOT_APPLICABLE
HOST VALIDATION: NOT_APPLICABLE
SCOPE/SECURITY CONTROLS: APPLICABLE
HANDOFF DOCUMENT AUDIT: APPLICABLE / PASS
UNIT DESIGN/DOCUMENTATION GATE: APPLICABLE / PENDING
```

`NOT_APPLICABLE` no significa `PASS`. Los controles de scope, touched paths, `HEAD`, staging,
working tree y seguridad siguen siendo obligatorios.

Gate inmediato del handoff materializado:

`FRESH_INDEPENDENT_HANDOFF_DOCUMENT_AUDIT: PASS`

Evidencia persistente: `auditoria/reviews/HANDOFF-F2E-IDENTIDAD-DETECTOR-READ-ONLY-REVIEW.md`.

Gate de salida de la unidad, sólo si este handoff obtiene aprobación y activación previa:

`FRESH_INDEPENDENT_DESIGN_DOCUMENT_AUDIT`

Ninguno de esos gates autoriza implementación.

## 7. Required Inputs / inputs autoritativos obligatorios

La unidad debe leer completos y usar como autoridad, como mínimo:

- `AGENTS.md`;
- `auditoria/orquestacion/{README,WORKFLOW,STATE-MACHINE,GATES,ROLES}.md`;
- `auditoria/{ESTADO-ACTUAL,ARQUITECTURA-ACTUAL,DECISIONES-ARQUITECTONICAS}.md`;
- `auditoria/{README-REESTRUCTURACION,REGLAS-DE-TRABAJO-IA}.md`;
- `auditoria/contexto/{DOMINIO-FUNCIONAL,MAPA-LEGACY-Y-MIGRACION}.md`;
- `auditoria/fase-2e-preparacion-migracion-controlada.md`;
- `auditoria/reviews/F2E.1-REVIEW-DISENO-PREPARACION.md`;
- `auditoria/fase-2d1-diseno-ajustes-programacion-fecha.md`;
- `auditoria/reviews/F2D.1.2-RE-REVIEW-FINAL.md`;
- `auditoria/fase-2d2-implementacion-dark-launch-ajustes-programacion-fecha.md`;
- `auditoria/reviews/F2D.2-REVIEW-DOCUMENTAL.md`;
- `auditoria/handoffs/HANDOFF-F2E-PREPARACION.md`;
- este handoff ya auditado y activado;
- código, migraciones y tests relevantes, sólo mediante inspección read-only.

No se usan chats, memoria conversacional ni nombres de fase inferidos como autoridad.

Semántica de autoridad obligatoria:

- F2D.1 y su review final fijan el contrato de diseño aprobado; esta unidad puede analizarlo y
  refinar límites de migración/detector-only, pero no contradecirlo sin una intervención
  arquitectónica explícita;
- F2D.2 y su review documental demuestran materialización y cierre en dark launch; no reescriben
  por sí solos el diseño aprobado;
- el código, las migraciones y los tests demuestran comportamiento físico, no autoridad de diseño
  exclusiva ni autoridad productiva;
- deben preservarse, si la inspección física confirma el contrato aprobado, target de
  `CANCELACION`/`REEMPLAZO` `serieId + fecha`, identidad recurrente/reemplazo `serieId + fecha`,
  identidad de adición `ajusteId + fecha` e identidad propia del ajuste;
- toda incompatibilidad material entre estas fuentes se registra en el checkpoint como
  `F2D_CONTRACT_COMPATIBILITY=FAIL` más `AUTHORITY_CONFLICT / BLOCKER`; nunca se corrige o adopta
  silenciosamente durante esta unidad.

## 8. Evidencia física mínima a inspeccionar

La investigación debe inspeccionar, sin modificar:

- entidades legacy `TurnoInstructor`, `TurnoInstructorAsignacion` y `Reserva`;
- `TurnoInstructorService`, `ReservaService` y sus controllers;
- `TurnoInstructorRepository`, `TurnoInstructorAsignacionRepository` y `ReservaRepository`;
- DTOs relevantes de turnos, asignaciones legacy y reservas;
- tests de caracterización de `TurnoInstructorService` y `ReservaService`;
- `BloqueProgramacion`, `Asignacion`, `AjusteProgramacionFecha` y sus repositorios;
- `OcurrenciaNominal`, `OcurrenciaEfectiva` y `ReferenciaOcurrencia`;
- `ProgramacionNominal`, `ProgramacionEfectiva`, aplicación/persistencia de ajustes y tests
  relevantes;
- `HorarioOperacion`, `SalonHorarioExcepcion`, sus repositorios/services y tests relevantes;
- migraciones que establecen las formas legacy, reservas, programación nueva, vigencias, horario y
  ajustes, incluida V47;
- DTOs, queries y tests adicionales que la evidencia física demuestre pertinentes al scope.

La lista orienta el mínimo; no autoriza modificar ninguno de esos paths.

## 9. D04 — reservation target identity

La unidad debe determinar documentalmente qué referencia futura debe usar `Reserva`. Debe separar
sin conflar:

- serie lógica;
- asignación física/versionada;
- occurrence nominal o efectiva;
- ventana reservable;
- occurrence + subintervalo reservado;
- ajuste puntual;
- snapshot persistido de `Reserva`.

Debe evaluar explícitamente, con evidencia física, al menos estas alternativas:

```text
A) occurrence/session exacta;
B) ventana padre reservable;
C) occurrence + subintervalo reservado;
D) otra identidad diseñada con garantías equivalentes.
```

Reglas obligatorias:

```text
Reserva contenida en un TurnoInstructor
!=
Reserva idéntica al TurnoInstructor

containment -> candidate evidence
containment -/-> identity proof
```

El checkpoint debe cerrar una identidad target explícita, incluida la semántica del subintervalo,
estabilidad, cardinalidad, evidencia y relación con snapshots, o registrar inequívocamente un
blocker humano real y la política exacta que falta. No puede delegar la elección a un futuro
implementation executor.

No se modifica `Reserva`, `ReservaService`, DTO, repository, controller, esquema ni FK.

## 10. Matriz definitiva de identity atoms

El checkpoint debe producir una matriz definitiva y tipada de átomos source/target. Debe cubrir,
como mínimo:

- turno legacy;
- asignación legacy;
- serie nueva de bloque y/o asignación, distinguidas cuando corresponda;
- fila/version de asignación nueva;
- occurrence nominal;
- occurrence efectiva;
- ventana reservable;
- ajuste `CANCELACION`, `REEMPLAZO` o `ADICION`;
- reserva y su snapshot;
- cualquier átomo adicional requerido por la decisión D04.

Para cada átomo debe definir identidad estable, temporalidad, provenance, campos obligatorios,
cardinalidades admitidas, evidencia disponible, evidencia ausente y relaciones que no demuestran
identidad.

## 11. D03 — crosswalk detector-only

D03 sólo puede cerrarse en esta unidad para:

- generación de `0..N` candidatos;
- generación y preservación de evidencia;
- clasificación determinista y reproducible.

El contrato debe definir:

- source atoms y target atoms tipados;
- relaciones admitidas y cardinalidad por relación/tipo;
- candidate identity y candidate evidence;
- source snapshot/fingerprint y target snapshot/fingerprint;
- provenance y versión de regla;
- criterios de elegibilidad y rechazo de cada candidato;
- mapping/result status y ambiguity status;
- `ambiguity_reason` reproducible y obligatorio cuando `ambiguity_status=AMBIGUOUS`;
- `selection_status` explícito, sin selección implícita;
- `blocking` explícito y el alcance material que bloquea;
- conteo de candidatos sin reducción silenciosa;
- tratamiento de ausencia esperada, ausencia anómala, unsupported y divergencia;
- determinismo, reproducibilidad y hashes conceptuales de evidencia;
- separación inequívoca entre contrato general y mappings de registros reales.

Todos los candidatos se conservan. La unidad no puede elegir uno, declarar un mapping real ni
diseñar una heurística de desempate material.

Invariante contractual obligatorio para D03 detector-only:

```text
candidate_count > 1
AND no existe una regla de selección inequívoca, aprobada y aplicable
-> mapping_status = MULTIPLE_CANDIDATES
-> ambiguity_status = AMBIGUOUS
-> ambiguity_reason = REQUIRED
-> selection_status = NOT_SELECTED_BY_DETECTOR
-> blocking = true
```

La razón debe identificar de forma reproducible el motivo material, por ejemplo múltiples
candidatos temporales o de asignación, evidencia histórica insuficiente, intención legacy no
persistida o identidad target no resuelta. Esos ejemplos no canonizan un enum físico.

Aunque una regla futura inequívoca llegue a existir, este componente detector-only no materializa
ni persiste una selección definitiva: sólo reporta evidencia y la aplicabilidad de la regla para
un boundary posterior expresamente autorizado. Con múltiples candidatos y sin esa regla, queda
prohibido ordenar y elegir el primero, el más cercano, el más reciente, el de mayor solapamiento,
una coincidencia parcial, el de mayor probabilidad o cualquier heurística no aprobada.

El resultado bloquea selección/persistencia de crosswalk, resolver material, migration y cutover,
según corresponda. No bloquea por sí solo detección de candidatos, reporting o métricas que
conserven la ambigüedad completa.

Queda prohibido:

```text
SELECT FINAL TARGET
PERSIST CROSSWALK
CREATE CROSSWALK TABLE
MATERIALIZE RESOLVER
```

El contrato puede cerrarse sin datos materiales. Los mappings individuales y cualquier claim de
cobertura real requieren una fuente autorizada y no pertenecen a esta unidad.

## 12. Cardinalidades y vocabulario de estados

El diseño debe representar explícitamente, sin asumir cardinalidad universal:

```text
1:1
1:N
N:1
0:1
1:0 cuando exprese supresión efectiva válida
MISSING
MULTIPLE_CANDIDATES
AMBIGUOUS
DIVERGENT
EXPECTED_ABSENCE / SUPPRESSED
UNSUPPORTED
UNKNOWN_HISTORY cuando aplique
```

Puede usar vocabulario semánticamente equivalente si provee una tabla de correspondencia
inequívoca. Cardinalidad, estado de mapping, estado de evidencia y estado efectivo son ejes
separados; un conteo no permite inferir automáticamente los otros.

Una cancelación nueva válida preserva la decisión ya cerrada:

```text
CANCELACION válida
+ target nominal válido y único
-> EXPECTED_ABSENCE / SUPPRESSED
```

Eso no es `MISSING`. `MISSING` identifica ausencia anómala de una entidad o resultado requerido.
La unidad no reabre esta decisión salvo evidencia física real que contradiga los canónicos; una
contradicción material debe registrarse y detener el claim afectado.

## 13. Candidate evidence schema

El checkpoint debe cerrar un schema conceptual autocontenido para cada resultado y su lista `0..N`
de candidatos. Debe incluir, como mínimo:

```text
detector_run_identity / evaluation_identity
source_system
source_atom_kind
source_stable_identity
source_snapshot_identity / fingerprint
target_atom_kind esperado
candidate_count
candidates
mapping_status
result_status
ambiguity_status
ambiguity_reason cuando ambiguity_status=AMBIGUOUS
selection_status
blocking + blocked_capabilities
history_status cuando aplique
rule_id + rule_version
evaluated_at
evidence_hash
error/status provenance
```

Cada candidato debe incluir, como mínimo:

```text
candidate_target_atom_kind
candidate_target_identity
relationship_evidence_kind
normalized observable fields
matching_dimensions
mismatch_dimensions
eligibility status
rejection reasons
objective_score opcional
candidate evidence hash
provenance
```

`objective_score`, si existe una métrica objetiva, es sólo evidencia explicativa y nunca puede
usarse para seleccionar, desempatar ni colapsar candidatos. La lista conserva identidad y evidencia
de cada candidato.

El schema no contiene `selected_target` funcional. `selection_status` es obligatorio y, para un
resultado con múltiples candidatos, su valor en este componente es
`NOT_SELECTED_BY_DETECTOR`. Si se conserva un campo de target seleccionado como boundary, debe ser
`null` y no puede contradecir ese estado.

Siempre que `ambiguity_status=AMBIGUOUS`, `ambiguity_reason` es obligatorio, no vacío y suficiente
para reproducir la clasificación desde evidencia/provenance. `candidate_count > 1` sin regla
inequívoca aprobada y aplicable exige además `mapping_status=MULTIPLE_CANDIDATES` y
`blocking=true`; no se permite degradar ese caso a warning ni éxito parcial.

## 14. D09 — semántica observable de `vigenteDesde` legacy

La unidad debe caracterizar documentalmente la semántica observable de `TurnoInstructor` respecto
a:

- vigencia funcional;
- historia disponible;
- create/update/delete o desactivación;
- reemplazo de asignaciones;
- timestamps técnicos;
- rango temporal sobre el cual una observación puede ser afirmada con seguridad.

Hipótesis físicas a verificar, no resultados asumidos:

- el recurrente legacy no tiene `vigenteDesde` funcional;
- update muta la fila existente;
- las asignaciones pueden reemplazarse;
- timestamps técnicos no equivalen automáticamente a vigencia funcional.

El checkpoint debe decidir qué hechos puede detectar de forma segura desde código, schema,
migraciones, tests y snapshots eventualmente autorizados. Cuando no exista evidencia histórica
suficiente debe producir `UNSUPPORTED`, `UNKNOWN_HISTORY`, `AMBIGUOUS` o equivalente fail-closed.

Queda prohibido inventar historia, usar timestamps técnicos como vigencia por intuición o
reconstruir historia heurísticamente.

## 15. D10 — formas puntuales legacy frente al modelo completo de ajustes nuevos

La unidad debe caracterizar la semántica observable y precedencia física de:

- `TurnoInstructor.EXCEPCION`;
- `TurnoInstructor.CANCELACION`;
- `HorarioOperacion`;
- `SalonHorarioExcepcion`;
- `AjusteProgramacionFecha.CANCELACION`;
- `AjusteProgramacionFecha.REEMPLAZO`;
- `AjusteProgramacionFecha.ADICION`;
- `ProgramacionNominal`;
- `ProgramacionEfectiva`;
- `ReservaService`.

Debe distinguir explícitamente:

- cancelación global legacy por fecha/instructor/salón;
- excepción legacy y su alcance observable;
- supresión de una occurrence nominal concreta del modelo nuevo;
- salón cerrado por excepción operativa;
- omisión por horario operativo efectivo;
- omisión fail-closed por maestros u otra precondición;
- reserva existente, confirmada o cancelada, como hecho separado de programación.

`AjusteProgramacionFecha` debe caracterizarse como modelo completo, separando sus tres formas y sin
reducir el análisis a `CANCELACION`:

- `CANCELACION`: target nominal obligatorio y único; una forma válida conserva ese target y
  produce `EXPECTED_ABSENCE / SUPPRESSED`. Un target requerido inexistente es `MISSING / BLOCKER`.
  Esta decisión F2D aprobada no se reabre;
- `REEMPLAZO`: target nominal, source identity, snapshot/resultado efectivo, identidad conservada
  `serieId + fecha`, campos que cambian y no cambian, cardinalidad, evidence y relación observable
  posible o imposible con `TurnoInstructor.EXCEPCION`;
- `ADICION`: ausencia de target nominal cuando corresponde, identidad basada en ajuste
  `ajusteId + fecha`, resultado efectivo, cardinalidad, evidence y relación observable con
  `TurnoInstructor.EXCEPCION`.

Para cada tipo nuevo el checkpoint debe distinguir target nominal cuando exista, source identity,
resultado efectivo, identity, cardinalidad, `EXPECTED_ABSENCE` cuando aplique, `MISSING` cuando sea
anomalía y evidence observable. Debe registrar también qué dimensiones se conservan y cuáles
cambian sin convertir similitud de campos en identidad.

El checkpoint debe producir una matriz o contrato semánticamente equivalente que compare, como
mínimo:

```text
legacy punctual form
new adjustment type
observable similarities
observable differences
target semantics
effective-result semantics
identity + cardinality
evidence
can infer mapping?
status if not inferable
```

La comparación cubre `TurnoInstructor.EXCEPCION` y `TurnoInstructor.CANCELACION` frente a
`AjusteProgramacionFecha.CANCELACION`, `REEMPLAZO` y `ADICION`; no presupone equivalencias
automáticas entre ninguna pareja.

La similitud de outcome no demuestra identidad de intención. Cuando una fila legacy no persista su
target o intención individual, el resultado debe ser `UNSUPPORTED`, `AMBIGUOUS` o equivalente. No
se infiere intención, no se convierte automáticamente una excepción en reemplazo/adición y no se
expande una cancelación a series candidatas como si fueran targets demostrados.

En particular queda prohibido inferir:

```text
TurnoInstructor.EXCEPCION -> AjusteProgramacionFecha.REEMPLAZO
TurnoInstructor.EXCEPCION -> AjusteProgramacionFecha.ADICION
```

sin evidencia inequívoca. Si la intención individual legacy no está persistida, el detector no la
reclasifica como cancelación targeteada, reemplazo ni adición; conserva `UNSUPPORTED / AMBIGUOUS`,
`ambiguity_reason`, candidatos y evidencia, y falla cerrado para mapping material.

D10 sólo puede cerrarse para detector-only cuando las tres formas nuevas hayan quedado
caracterizadas y los casos legacy sin intención demostrable permanezcan fail-closed. No necesita
ni autoriza mappings productivos.

## 16. D11 — arquitectura conceptual detector-only

La unidad debe cerrar la arquitectura conceptual del futuro subconjunto read-only, sin elegir un
stack adicional innecesario. Debe separar al menos estos ownerships:

1. source observation y snapshot metadata;
2. identity atom projection;
3. candidate/evidence generation;
4. classification y fail-closed policy;
5. immutable report/metrics output;
6. ejecución/orquestación read-only y manejo de errores.

Para cada ownership debe definir:

- purpose y límite;
- inputs y outputs;
- interfaces conceptuales;
- schemas de request/result/evidence/error;
- dependencias permitidas y prohibidas;
- invariantes de no mutación;
- observabilidad;
- comportamiento ante evidencia ausente, ambigua, inconsistente o cambiante;
- boundary con selección/crosswalk, resolver comparativo, fence, migración y autoridad futura.

El result schema conceptual compartido por D11 debe expresar, como mínimo y sin imponer todavía
una representación física:

```text
candidate_count
candidates[] con identity + evidence + matching/mismatch dimensions
mapping_status
ambiguity_status
ambiguity_reason cuando aplique
selection_status
evidence/provenance
blocking + blocked_capabilities
```

Para `candidate_count > 1` sin regla de selección inequívoca, aprobada y aplicable, D11 debe
propagar exactamente la semántica de D03: `MULTIPLE_CANDIDATES`, `AMBIGUOUS`, razón obligatoria,
`NOT_SELECTED_BY_DETECTOR` y `blocking=true`. Ningún ownership puede convertir un score, orden,
proximidad, recencia, solapamiento, coincidencia parcial o probabilidad en selección.

Ningún futuro executor de `IMPLEMENTATION_READ_ONLY` debe tener que decidir la arquitectura del
subconjunto detector-only. La unidad sí debe dejar fuera, de manera expresa, las decisiones físicas
de componentes no incluidos.

## 17. Boundary del detector

El futuro detector diseñado debe poder:

```text
READ
OBSERVE
GENERATE 0..N CANDIDATES
CLASSIFY
REPORT
METRIC
FAIL CLOSED
```

No debe poder:

```text
SELECT FINAL TARGET
PERSIST CROSSWALK
MUTATE SOURCE OR TARGET DATA
MIGRATE
NORMALIZE
REPAIR
ACTIVATE
WRITE PRODUCTIVE STATE
CHANGE AUTHORITY
ENFORCE FENCE
SWITCH READERS OR WRITERS
```

Los outputs del detector son evidencia no autoritativa. No son input productivo implícito, no
conceden equivalencia y no habilitan una transición de autoridad.

## 18. Métricas, evidencia y error handling

El contrato debe definir métricas reproducibles, sin PII innecesaria, al menos para:

- resultados y candidatos por source/target atom kind;
- distribución de cardinalidad `0/1/>1` y relaciones `1:1`, `1:N`, `N:1`, `0:1`;
- `exact/equivalent` sólo cuando el contrato detector pueda demostrarlo sin selección;
- `expected_absence/suppressed` separado de `missing_unexpected`;
- `multiple_candidates`, `ambiguous`, `unsupported`, `unknown_history` y `divergent`;
- cobertura del scope observado, sin llamarla cobertura de mapping real;
- errores de source, schema, snapshot, reglas y evidencia;
- versión de detector/regla y fingerprints de ejecución.

Debe separar:

```text
operational error
semantic classification
security/authority stop
```

Una falla operacional no se transforma en un mapping status positivo ni en ausencia de anomalías.
Una fuente o snapshot cambiante invalida o aborta la evaluación afectada de forma fail-closed.

## 19. Data source y consultas

Estado vigente:

`DATA_SOURCE_NOT_AVAILABLE`

La unidad puede completarse mediante inspección read-only de canónicos, código, migraciones,
schemas declarados y tests. La falta de datos materiales no es `HUMAN_STOP` por defecto y no
autoriza inventar conteos, anomalías, mappings ni cobertura.

Consultas materiales sólo son admisibles si antes de ejecutarlas existe acumulativamente:

```text
fuente concreta identificada por nombre
+ autorización explícita read-only que nombre esa fuente
+ scope y custodio definidos
+ garantía verificable de no mutación
```

Sin esas condiciones:

```text
DATA QUERIES: FORBIDDEN
SQL: FORBIDDEN
```

Con esas condiciones, el máximo permitido son consultas `SELECT` estrictamente read-only para
evidencia de diseño. Siguen prohibidos DDL, DML, funciones mutantes, locks con intención de escritura,
normalización, reparación y persistencia de outputs en sistemas productivos.

## 20. Output único de la unidad

La unidad autorizada debe crear o materializar exactamente un checkpoint documental:

`auditoria/fase-2e-identidad-semantica-detector-read-only.md`

No puede crear reviews, intervenciones, código, tablas, migraciones, reportes materiales de datos u
otros artefactos durante la misma ejecución. El review posterior será producido por un auditor
fresh e independiente, fuera del scope de escritura de la unidad.

El checkpoint debe ser autocontenido y contener exactamente los resultados exigidos por las
secciones 9 a 18, además de pre-flight, evidencia, decisiones, blockers, prerequisites y límites.

## 21. Expected outputs

El checkpoint debe incluir, como mínimo:

1. matriz definitiva de identity atoms;
2. decisión D04;
3. reglas detector-only D03;
4. mapping status vocabulary;
5. candidate evidence schema;
6. semántica determinista de multiple candidates, incluida la invariante `N>1`;
7. `ambiguity_reason` obligatorio para todo resultado `AMBIGUOUS`;
8. semántica detector non-selection con `selection_status=NOT_SELECTED_BY_DETECTOR`;
9. blocker semantics separando detección/reporting de crosswalk/resolver/migration/cutover;
10. candidate evidence completo, incluidas dimensiones coincidentes y divergentes;
11. result schema detector-only suficiente y coherente entre D03/D11;
12. D09 observable semantics;
13. límites de historical reconstruction;
14. D10 observable semantics para `CANCELACION`, `REEMPLAZO` y `ADICION`;
15. matriz legacy punctual form vs new adjustment type y casos `UNSUPPORTED / AMBIGUOUS`;
16. D11 detector-only architecture;
17. interfaces conceptuales;
18. result schemas;
19. metrics/evidence contract;
20. `F2D_CONTRACT_COMPATIBILITY: PASS / FAIL`, fuentes contrastadas y divergencias detectadas;
21. `AUTHORITY_CONFLICT / BLOCKER` para toda incompatibilidad material con F2D aprobada;
22. fail-closed rules;
23. decisions closed;
24. decisions deferred;
25. blockers;
26. prerequisites exactos para un futuro handoff `IMPLEMENTATION_READ_ONLY`.

El checkpoint debe distinguir claims cerrados por evidencia, claims limitados a observación y
decisiones que permanecen diferidas. No puede presentar candidate scope, diseño o detección como
materialización.

## 22. Decisiones diferidas y D08 excluida

Quedan expresamente diferidos:

- `D08 / fence cross-salon`;
- cohorte conexa salón-instructor o protocolo cross-model;
- persistencia y enforcement de fence;
- persistencia de crosswalk;
- selección definitiva de mappings reales;
- auditoría material de datos;
- resolver material o productivo;
- migration y normalization productiva;
- writer/read switching;
- estados reales `MIGRANDO` y `NUEVA`;
- cutover;
- cambio de autoridad productiva;
- políticas humanas sobre casos históricos ambiguos que aparezcan.

Estado de D08/fence preservado:

```text
D08: DEFERRED
Fence: DESIGN HISTÓRICO / FUTURE
Materialization: NOT_MATERIALIZED
Active: NO
Enforcement: NOT_AUTHORIZED
```

Esta unidad no cierra ni reabre la cohorte cross-salon, persistencia del fence, enforcement,
`MIGRANDO` o `NUEVA`.

## 23. Scope absolutamente prohibido

La unidad no autoriza:

- modificar `src/main`;
- modificar `src/test`;
- modificar migraciones;
- modificar `Reserva`;
- modificar `ReservaService`;
- crear auditor code;
- crear detector code;
- crear crosswalk code o table;
- crear resolver code;
- crear fence code o table;
- ejecutar SQL, salvo la excepción read-only expresa y condicionada de la sección 19;
- data mutation;
- migration;
- normalization;
- repair;
- productive read switching;
- productive write switching;
- frontend;
- mobile;
- controllers;
- endpoints;
- `MIGRANDO`;
- `NUEVA`;
- cutover;
- authority change;
- `git add`, commit o push.

La única escritura autorizada para la unidad es el checkpoint exacto de la sección 20.

## 24. Fail-closed

Ante ambigüedad, historia no demostrable, intención no persistida, evidencia incompleta, source
cambiante o contradicción:

```text
NO seleccionar
NO mapear como definitivo
NO persistir
NO migrar
NO activar
NO inferir historia
NO inferir intención
NO reparar
NO cambiar authority
```

El resultado debe conservar los candidatos `0..N`, evidence/provenance y un status como
`MISSING`, `MULTIPLE_CANDIDATES`, `AMBIGUOUS`, `UNSUPPORTED`, `UNKNOWN_HISTORY`, `DIVERGENT` o
equivalente. Un blocker/status se reporta; no se oculta mediante fallback, primera fila, score,
proximidad o contención.

Reglas fail-closed transversales adicionales:

```text
candidate_count > 1 sin regla inequívoca aprobada/aplicable
-> MULTIPLE_CANDIDATES + AMBIGUOUS + ambiguity_reason
-> NOT_SELECTED_BY_DETECTOR + BLOCKER

intención legacy no persistida
-> UNSUPPORTED / AMBIGUOUS; no inferir tipo nuevo

conflicto material con F2D aprobada
-> F2D_CONTRACT_COMPATIBILITY=FAIL + AUTHORITY_CONFLICT / BLOCKER

historia no demostrable
-> UNSUPPORTED / UNKNOWN_HISTORY
```

El blocker de múltiples candidatos impide selección/persistencia de crosswalk, resolver material,
migration y cutover, pero permite detector-only, reporting y métricas que conserven todos los
candidatos y su ambigüedad.

## 25. HUMAN_STOP

`HUMAN_STOP` se reserva para una decisión humana real, por ejemplo:

- contradicción canónica material irresoluble;
- autoridad insuficiente;
- decisión de producto imprescindible para elegir reservation target y que no admita un contrato
  fail-closed útil;
- semántica histórica que requiera política de producto y no pueda quedar explícitamente
  `UNSUPPORTED / UNKNOWN_HISTORY`;
- P0, P1 no corregible o `SECURITY_STOP`;
- necesidad de mutar un path o sistema fuera del scope.

No se usa `HUMAN_STOP` sólo por:

- falta de Docker;
- falta de datos reales cuando el diseño puede completarse;
- no poder implementar;
- no poder migrar;
- un problema mecánico recuperable dentro del protocolo.

## 26. Exit conditions

La unidad sólo puede presentarse a cierre cuando:

- D04 queda cerrada documentalmente o existe blocker humano explícito, exacto y no evitable por un
  contrato fail-closed;
- D03 queda cerrada para candidate generation detector-only, evidence y classification;
- D03 no permite cerrar si `candidate_count > 1` sin regla inequívoca aprobada/aplicable puede
  producir algo distinto de `MULTIPLE_CANDIDATES`, `AMBIGUOUS`, `ambiguity_reason` obligatorio,
  `NOT_SELECTED_BY_DETECTOR` y `BLOCKER`;
- D09 queda cerrada para observable semantics o limita explícitamente lo no demostrable a
  `UNSUPPORTED / UNKNOWN_HISTORY`;
- D10 queda cerrada para observable semantics o clasifica intención no demostrable como
  `AMBIGUOUS / UNSUPPORTED`;
- D10 caracteriza separadamente `AjusteProgramacionFecha.CANCELACION`, `REEMPLAZO` y `ADICION`,
  compara las formas puntuales legacy sin equivalencias automáticas y deja fail-closed toda
  intención legacy no demostrable;
- D11 queda cerrada para auditor, candidate/evidence generator y metrics/report output;
- D11 propaga el result schema mínimo, `ambiguity_reason`, `selection_status`, evidence y blocking
  status exigidos por D03;
- el checkpoint registra `F2D_CONTRACT_COMPATIBILITY: PASS / FAIL`; cualquier divergencia material
  queda `AUTHORITY_CONFLICT / BLOCKER` y no se corrige silenciosamente;
- ningún futuro executor debe decidir la arquitectura del detector-only;
- el checkpoint contiene todos los expected outputs;
- D08/fence permanece diferida y no materializada;
- no existe selección ni persistencia de mapping real;
- no hubo implementación, modificación de código/tests/migraciones ni SQL no autorizado;
- no hubo data mutation, migration, activation ni authority change;
- `TurnoInstructor`, `DARK_LAUNCH`, `NOT_PRODUCTIVE` y `cutover=false` permanecen;
- `HEAD`, staging, working tree y touched paths satisfacen el scope;
- un audit fresh e independiente de diseño/documentación reporta `P0=0` y `P1=0`.

Antes del audit de la unidad, su estado máximo será:

`DESIGN_MATERIALIZED / READY_FOR_FRESH_INDEPENDENT_DESIGN_DOCUMENT_AUDIT / NOT_APPROVED`

El autor del checkpoint no puede aprobar su propio diseño.

## 27. Gate posterior y futura implementación read-only

Next gate de la unidad:

`FRESH_INDEPENDENT_DESIGN_DOCUMENT_AUDIT`

Ese audit sólo puede aprobar o rechazar el diseño documental. No autoriza implementación.

Una futura unidad `IMPLEMENTATION_READ_ONLY` sólo podrá proponerse después de:

```text
design/document audit: PASS con P0=0 / P1=0
+ nuevo handoff específico auditado y activado
+ D11 detector-only cerrado
+ scope exacto y allowlist física
+ inputs/outputs/interfaces/result schemas ya decididos
+ fuente nombrada y autorización read-only si accede a datos materiales
```

Incluso entonces permanecerán fuera salvo nueva autoridad separada: selección y persistencia de
crosswalk, resolver de equivalencia, fence, migration, productive switching, cutover y cambio de
autoridad.

## 28. No-activaciones y no-autorizaciones

```text
This handoff approved: YES
This handoff active: YES
Target unit active: YES — DESIGN / RESEARCH ONLY
Implementation authorized: NO
Test code authorized: NO
Migration code authorized: NO
Crosswalk persistence authorized: NO
Resolver implementation authorized: NO
Fence implementation authorized: NO
Reserva modification authorized: NO
Data mutation authorized: NO
Migration authorized: NO
MIGRANDO authorized: NO
NUEVA authorized: NO
Cutover authorized: NO
Product authority change authorized: NO
```

El estado vigente de este artefacto es:

`HANDOFF APPROVED / ACTIVE / AUTHORIZED FOR DESIGN RESEARCH`

El diseño de la unidad sigue `PENDING`; su checkpoint no existe y requiere un audit fresh e
independiente antes de cualquier aprobación posterior.
