# FeelingPilates — Estado actual de la reestructuración

Status: CANONICAL / R4_DESIGN_INTEGRATED / PROCESS_CLOSURE_CONDITIONAL
Last updated: 2026-10-03 (UTC)
Current lifecycle: F2E_R4_DESIGN_PROCESS_CLOSURE
Canonical design merge: d3abd2c27a8de094c9cc60e0db43d9a816713ab2 (PR #23)
R1–R3: CLOSED / ACCEPTED / PUBLISHED / INTEGRATED; PR #22 closure verified
R4 design: COMPLETE / INDEPENDENTLY_AUDITED / ACCEPTED / PUBLISHED / INTEGRATED
R4 process closure: PENDING_FRESH_INDEPENDENT_AUDIT_AND_VERIFIED_CLOSURE_MERGE
R4 implementation/handoff/migration: NOT_AUTHORIZED / NOT_IMPLEMENTED / NONE_CREATED
R4–R6 activation: NOT_AUTHORIZED
TurnoInstructor: LEGACY_VIVO / PRODUCTIVO
Dark launch: PRESERVED; Cutover: NOT_AUTHORIZED
Flyway: V46 / 49 migrations / V47 ABSENT
Payments / Notifications: OUT_OF_SCOPE

## Estado vigente — R4 diseño integrado, cierre separado condicionado

Diseño autocontenido: [reader de ajustes](fase-2e-r4-diseno-reader-ajustes-fecha-reconciliado.md).
Auditoría exact-byte fresh [recibo](reviews/F2E-R4-DESIGN-INDEPENDENT-RECEIPT.md): task_b31cab48b966/ctx_876fa9a64312, APPROVED DESIGN ONLY, P0/P1/P2 0/0/0.
PR [#23](https://github.com/MacacosDevs/feellingPilates/pull/23) MERGED; candidato43248a6 y reciboc35ac5a, diseño SHA256 d966e32318c10b5fa6226f17f63001d71fbbe24419d7dee70842a97f9c7204d4.
Sin correcciones; sin tests técnicos ejecutados en este lifecycle documental.
R3 cierre PR [#22](https://github.com/MacacosDevs/feellingPilates/pull/22) integrado en 5c8a9ab; sus condiciones y fuentes selladas corroboradas antes de comenzar R4.
R4 sólo facts/backing exact-date; requiere mínimo prerrequisito de persistencia separado, no V47 bundle ni exclusión de asignaciones. No entity managed/restauración, composición R5 ni owner multi-reader R6.
Cierre separado: [condiciones](reviews/F2E-R4-DESIGN-PROCESS-CLOSURE.md).

CLOSED only after fresh independent closure audit APPROVED with P0/P1=0; factual receipt committed as direct child preserving all three audited candidate files; normal merge of candidate+receipt into main verified live; exact four-path process delta from d3abd2c; design and design receipt unchanged; all baseline source, tests, migrations and sealed R1-R3 authority unchanged; clean index, tracked and untracked; PR22/PR23 and audited candidate/receipt ancestors of main. Otherwise PROCESS_CLOSURE_PENDING / FAIL_CLOSED.

Tras ese cumplimiento el diseño R4 queda CLOSED; receipt factual y Git/GitHub live resuelven los estados de captura sin reescribir bytes auditados.
Siguiente lifecycle necesario: MINIMUM_R4_SCHEMA_PREREQUISITE_AUTHORITY, read-only/design authority antes de una migración separadamente autorizada y un futuro handoff R4. No se inicia por este cierre.

## HISTORICAL_PROVENANCE_ONLY — corte completo anterior

El bloque siguiente conserva íntegro el corte previo R3; sus etiquetas y próximas acciones describen ese corte y se subordinan al estado vigente de arriba y a evidencia live.

# FeelingPilates — Estado actual de la reestructuración

Status: CANONICAL / R3_IMPLEMENTATION_INTEGRATED / PROCESS_CLOSURE_CONDITIONAL
Last updated: 2026-10-03
Canonical implementation merge: 3329cc0d8d8c2d6b44df980e7e96e3b24dcfe3a8
Current lifecycle: F2E_R3_IMPLEMENTATION_PROCESS_CLOSURE
R1: CLOSED / ACCEPTED / PUBLISHED / INTEGRATED; guard reconciliado separadamente por PR #20
R2 Implementation: IMPLEMENTED / ACCEPTED / PUBLISHED / INTEGRATED / CLOSED
R3 Implementation: IMPLEMENTED / INDEPENDENTLY_AUDITED / ACCEPTED / PUBLISHED / INTEGRATED
R3 Process Closure: CANDIDATE / PENDING_FRESH_INDEPENDENT_PROCESS_AUDIT / EFFECTIVE_ONLY_AFTER_VERIFIED_CLOSURE_MERGE
R3 Handoff: ACTIVE_FOR_IMPLEMENTED_R3 / DEFAULT_DENY / WRITE_SCOPE_20_UNCHANGED
R4-R6: NOT_AUTHORIZED
TurnoInstructor: LEGACY_VIVO / PRODUCTIVO
Dark launch: PRESERVED
Cutover: NOT_AUTHORIZED
Flyway: V46 (49 migrations V1–V46); V47 ABSENT; migración R3 NONE
Payments / Notifications: OUT_OF_SCOPE

## Estado vigente — implementación R3 integrada, cierre de proceso condicionado

PR [#21](https://github.com/MacacosDevs/feellingPilates/pull/21) quedó MERGED el
2026-10-03T22:41:28Z. Su merge `3329cc0d8d8c2d6b44df980e7e96e3b24dcfe3a8` tiene padres
`c79216f99c66cbd4d5d909498ee41222e10090c9` y
`c22952c18d0da732594314e7e34e150bdc8db856`.
El candidato técnico aprobado es `94d8f06c16ee64d85c45e7cd6e47ec4ab5ce375f`;
el hijo de publicación añadió sólo el recibo de revisión, sin cambiar código.
La verificación live posterior probó ese historial y las 20 fuentes idénticas.
Recibo técnico: `auditoria/reviews/F2E-R3-IMPLEMENTACION-READER-PROGRAMACION-NOMINAL-CLEAN-MAIN-REVIEW.md`.
Auditoría fresh independiente: Task `task_50459af87d58`, Dispatch
`ctx_07373e2b6353`, APPROVED, P0/P1/P2 `0/0/0`; P1-1 CLOSED.
Gate real `gate_54c01ce6906e`: READY_FOR_CONTROLLED_PUBLICATION.
Regresión independiente: **549 tests, 0 failures, 0 errors, 0 skipped**;
R3 36, R1/R2 94, detector 37, Lane 1–4 382; clean compile 217 fuentes.
JDBC_BINDING, CHECKSUM_STABILITY y CHECKSUM_SENSITIVITY: PASS.

PR #19 activó el handoff (merge `cefcde1adf51cc7eedbf98538fb6a5f4b3322e13`).
PR #20 reconcilió el guard R1 con autorización humana separada, auditoría y merge
`c79216f99c66cbd4d5d909498ee41222e10090c9`; no amplió el WRITE_SCOPE R3.
La implementación usa exactamente 18 NEW y 2 MODIFIED aditivas; ninguna migración,
configuración, routing productivo, client API, web, reservas ni cutover cambia.

El cierre separado se define en `auditoria/reviews/F2E-R3-IMPLEMENTACION-CIERRE-POST-MERGE.md`.
CLOSED sólo cuando: auditoría independiente del candidato exacto APPROVED con P0/P1/P2 0/0/0; recibo factual de esa auditoría publicado como hijo directo sin cambiar los tres archivos auditados; merge normal de ese historial en main verificado live; delta respecto de 3329cc0d limitado a las cuatro rutas de proceso; las 20 fuentes R3 y las tres autoridades selladas conservan sus hashes aprobados; PR #19/#20/#21 y candidato/recibo de cierre son ancestros del main verificado. Si falta una prueba: PROCESS_CLOSURE_PENDING / FAIL_CLOSED.
No se declara CLOSED en este corte candidato. La condición se resuelve mediante
Git/GitHub live y el recibo `auditoria/reviews/F2E-R3-IMPLEMENTACION-CIERRE-INDEPENDENT-RECEIPT.md`; el cache STATE no concede autoridad.
Tras cumplirla, R3 es IMPLEMENTED / ACCEPTED / PUBLISHED / INTEGRATED / CLOSED.
La siguiente acción autorizada es sólo completar este cierre; R4–R6 requieren
una autoridad futura independiente y no quedan autorizados por el cierre.

## HISTORICAL_PROVENANCE_ONLY — snapshots anteriores

Todo el contenido siguiente conserva los cortes históricos completos. Sus frases
«estado vigente», NOT_IMPLEMENTED, NOT_ACTIVE o próximos lifecycles describen
esos cortes; no reemplazan el bloque actual ni las fuentes superiores publicadas.

# FeelingPilates — Estado actual de la reestructuración

Status: CANONICAL / CLEAN_MAIN_RECONCILIATION / R3_HANDOFF_ACTIVATION_FINALIZATION_CONDITIONAL
Last updated: 2026-09-29
Repository verification: VERIFIED
Canonical base commit: cd644059faee6c7baa73e05c74f19fc89c3756fe
Flyway head: V46
Historical F2E source HEAD: 6140978bfd7b723fbbf9ddde1b5b5ba4f777c43c
Current lifecycle: F2E_R3_HANDOFF_ACTIVATION_FINALIZATION
R1: CLOSED / ACCEPTED / PUBLISHED / INTEGRATED
R2 Design: COMPLETE / AUDITED / PUBLISHED / CLOSED
Historical R2 Implementation Handoff: REPOSITORY_PUBLISHED_HISTORICAL_ARTIFACT (commit 6140978) / PROVENANCE_ONLY
Current Clean-Main R2 Implementation Handoff: RECONCILED / PUBLISHED / INDEPENDENTLY_APPROVED / PREACTIVATION_AUDITED / ACTIVE
Preactivation audit: INDEPENDENTLY_AUDITED / APPROVED / READY_FOR_ACTIVATION_TRANSITION
Independent approval review: auditoria/reviews/HANDOFF-F2E-R2-IMPLEMENTACION-LECTOR-TURNO-LEGACY-CLEAN-MAIN-RECONCILIATION-REVIEW.md (commit 6fd7818)
Preactivation audit receipt: auditoria/reviews/HANDOFF-F2E-R2-ACTIVATION-PRE-AUDIT-CANDIDATE.md (candidate a19cb460e101f869eb10985b0ef36ec4bdf717bc; receipt commit a360a536022207ebe231eb2588f5a9ea1620e835)
R2 Implementation Authority: AUTHORIZED_TO_START
R2 Implementation State: NOT_IMPLEMENTED
R4-R6: NOT_AUTHORIZED
R3 Design: RECONCILED / INDEPENDENTLY_AUDITED / APPROVED / PUBLISHED / INTEGRATED / CLOSED
R3 Implementation: AUTHORIZED_TO_START / NOT_IMPLEMENTED (effective only after verified finalization merge)
R3 Implementation Handoff: MATERIALIZED / RECONCILED / INDEPENDENTLY_AUDITED / APPROVED / PUBLISHED / INTEGRATED / ACTIVE (effective only after verified finalization merge)
R3 Handoff Activation: INDEPENDENTLY_AUDITED / APPROVED / PUBLISHED / INTEGRATED / FINALIZATION_CONDITIONAL
TurnoInstructor: LEGACY_VIVO / PRODUCTIVO
Dark launch: PRESERVED
Cutover: NOT_AUTHORIZED
V47: ABSENT
Payments / Notifications: OUT_OF_SCOPE

## Estado vigente — finalización condicionada de activación R3

Base canónica de esta finalización `cd644059faee6c7baa73e05c74f19fc89c3756fe`
(merge del PR #18). El diseño R3 está `CLOSED`; el handoff R3 está
`MATERIALIZED / RECONCILED / INDEPENDENTLY_AUDITED / APPROVED / PUBLISHED /
INTEGRATED`. Su activación se rige por la condición verificable de
`auditoria/reviews/HANDOFF-F2E-R3-ACTIVATION-FINALIZATION.md`.
La aprobación independiente del handoff está publicada en
`auditoria/reviews/F2E-R3-REVIEW-HANDOFF-IMPLEMENTACION-READER-PROGRAMACION-NOMINAL-CLEAN-MAIN.md`
(recibo `c9f28f2dd1901a1af8a874e3ced89c66e9b5f02b`, P0/P1/P2 `0/0/0`).
El handoff conserva SHA-256
`a74d139ca6c924dcbb98c368d80f38a17cf423408c7db30054e621b67b4cefa9`.

El candidato original `e397a56c7b42c160b2eb12ef2c30529418a75a01` y
Correction.1 `0d0db6ef813fe04ddf11a3d72557311317761faa` recibieron
reauditoría fresh independiente `APPROVED`, P0/P1/P2 `0/0/0`, con ambos P1
cerrados. El recibo se publicó en el commit
`875c94eea6c2b9ffdc16171777f51ea8a0f42b91` y quedó integrado en main
por PR #18, merge `cd644059faee6c7baa73e05c74f19fc89c3756fe`.
Mientras esta finalización no esté integrada y verificada en main, el handoff
sigue `NOT_ACTIVE` y R3 sigue `NOT_AUTHORIZED / NOT_IMPLEMENTED`. Tras verificar
la integración del commit de finalización y sus precondiciones, el handoff
queda `ACTIVE` y la autoridad R3 `AUTHORIZED_TO_START`; la implementación
permanece `NOT_IMPLEMENTED`. El registro candidato por sí solo no activa nada.

La allowlist publicada conserva NEW 18, MODIFIED 2, WRITE_SCOPE 20, READ_ONLY
17 y PROVENANCE_ONLY 4, con los seis hashes exactos del handoff y
`DEFAULT_DENY` obligatorio. La implementación futura autorizada sólo podrá
escribir dentro del WRITE_SCOPE de 20 rutas.
Flyway V46; V47 ausente; migración R3 ninguna. `TurnoInstructor` sigue
`LEGACY_VIVO / PRODUCTIVO`; Programación productiva no cambia; R3 permanece
`DARK_LAUNCH` sin routing productivo. Client API, web y reservas no cambian.
R4–R6 y cutover siguen `NOT_AUTHORIZED`; Payments / Notifications siguen
`OUT_OF_SCOPE`.

Las secciones de snapshots anteriores que siguen debajo son procedencia
histórica; este bloque y la autoridad publicada determinan el estado vigente.

## Candidato local del handoff R3

Base canónica `5964844e92fb467cda78debef01e318ebe33546f`. El handoff de
implementación R3 `auditoria/handoffs/HANDOFF-F2E-R3-IMPLEMENTACION-READER-PROGRAMACION-NOMINAL-CLEAN-MAIN.md`
queda `MATERIALIZED_CANDIDATE / PENDING_FRESH_INDEPENDENT_HANDOFF_REAUDIT /
NOT_APPROVED / NOT_PUBLISHED / NOT_ACTIVE`. Los allowlists del handoff son
propuestas exactas para auditoría independiente posterior; no autorizan
escritura de implementación en este lifecycle. R3 sigue `NOT_AUTHORIZED /
NOT_IMPLEMENTED`; R4–R6 siguen `NOT_AUTHORIZED`. Flyway V46, V47 ausente,
migración R3 ninguna; TurnoInstructor y routing productivo sin cambio.
El candidato original `30379f343b81f653173e4cb78806aeb50e9b1317` recibió
auditoría independiente `BLOCKED` (P1=2: binding JDBC real y checksum de slice
dinámico). Correction.1 corrige sólo el contrato de aceptación del handoff;
no modifica el diseño cerrado ni la allowlist de implementación. El próximo
gate es una reaudit fresh en nuevo chat; no se ejecuta aquí.

## Cierre post-merge del diseño R3

El diseño R3 reconciliado quedó publicado e integrado por PR #14 en el merge commit
canónico `f61bdb7237c133ebcb7f5171366e9d9e8b7c3a89`. Su lineage es:

```text
a26bfa5b9769fdb38cb5514773cc65679b81d468
→ a9902a37593bd6ecde26fd671ca753bb59c4a4e7
→ a8d3044e44b590a04f1570c70a8b3bdd79fddc98
→ f61bdb7237c133ebcb7f5171366e9d9e8b7c3a89
```

Diseño: `RECONCILED / INDEPENDENTLY_AUDITED / APPROVED / PUBLISHED / INTEGRATED / CLOSED`.
El artefacto es `auditoria/fase-2e-r3-diseno-reader-programacion-nominal-reconciliado.md`
con SHA-256 `42a21c09d137d363e938d441a499d48c5ec7537a73ee23861bfce2c4d75677f9`.
La revisión independiente publicada permanece en
`auditoria/reviews/F2E-R3-REVIEW-DISENO-READER-PROGRAMACION-NOMINAL-RECONCILIADO.md`.

R3 implementación: `NOT_AUTHORIZED / NOT_IMPLEMENTED`. No existe handoff activo de
implementación R3. El siguiente lifecycle exacto es `R3 IMPLEMENTATION HANDOFF`, cuyo
propósito será derivar del diseño cerrado el allowlist de implementación clean-main y
su contrato de aceptación. Este cierre no autoriza implementación.

Decisiones de diseño preservadas: Flyway `V46`; `V47 ABSENT / NOT_REQUIRED_FOR_R3`;
ninguna migración; duplicados aplicables resueltos `READ_TIME_FAIL_CLOSED`; reader
`MANDATORY / readOnly=true` con `f2eReaderTransactionManager`; owner individual
`REQUIRES_NEW / REPEATABLE_READ / readOnly=true`; invocación sin owner fail-closed;
ownership futuro de múltiples readers corresponde a R6 y no está autorizado. R4-R6
siguen `NOT_AUTHORIZED`; `TurnoInstructor` sigue `LEGACY_VIVO / PRODUCTIVO`; dark launch
`PRESERVED`; cutover `NOT_AUTHORIZED`; Payments / Notifications `OUT_OF_SCOPE`.

Este lifecycle sólo registra hechos de reconciliación, auditoría, aprobación,
publicación e integración ya ocurridos. No cambia diseño, producto, pruebas,
configuración, migraciones ni autoridad de implementación.

---

## Estado vigente — handoff de implementación R2 activo

El handoff R2 reconciliado conserva sus bytes exactos, está respaldado por la revisión independiente publicada en PR #10 y por la auditoría fresh independiente del candidato exacto `a19cb460e101f869eb10985b0ef36ec4bdf717bc`. La auditoría aprobó exclusivamente la transición mecánica posterior registrada aquí.

```text
HANDOFF APPROVAL: PUBLISHED / APPROVED / INDEPENDENTLY_APPROVED
CURRENT ACTIVE IMPLEMENTATION HANDOFF: auditoria/handoffs/HANDOFF-F2E-R2-IMPLEMENTACION-LECTOR-TURNO-LEGACY-CLEAN-MAIN-RECONCILIATION.md
HANDOFF: RECONCILED / PUBLISHED / INDEPENDENTLY_APPROVED / PREACTIVATION_AUDITED / ACTIVE
PREACTIVATION AUDIT: APPROVED / READY_FOR_ACTIVATION_TRANSITION
R2 IMPLEMENTATION AUTHORITY: AUTHORIZED_TO_START
R2 IMPLEMENTATION STATE: NOT_IMPLEMENTED
R3-R6: NOT_AUTHORIZED
TurnoInstructor: LEGACY_VIVO / PRODUCTIVO
Dark launch: PRESERVED
Cutover: NOT_AUTHORIZED
Flyway: V46
V47: ABSENT
Payments / Notifications: OUT_OF_SCOPE
STATE: DERIVED / NON_PRODUCT_AUTHORITY / OPERATIONAL_CACHE
```

`AUTHORIZED_TO_START` authorizes only a separate subsequent R2 implementation lifecycle to write within the exact approved R2 allowlist. It does not mean implemented, productive, cut over, migrated, or authorized for R3–R6, V47, Payments, or Notifications. This process-only transition changes no product code, tests, test infrastructure, migrations, configuration, product authority, dark-launch boundary, or implementation scope.
## Snapshot del repositorio verificado en 3B.0

Branch verificada (snapshot 3B.0):

`operacion/excepciones-horario-fecha`

Base de código/estado verificada:

`8c40594d2caf8b5230b364cb76cd8f48fe5ed98a`

Esta es la base contra la que se reprodujo el baseline y se verificó la documentación durante 3B.0.

HEAD operativo actual:

`NO SE PERSISTE COMO VALOR ESTÁTICO`

Debe verificarse al comienzo de cada intervención mediante:

`git rev-parse HEAD`

Upstream verificado (snapshot 3B.0):

`origin/operacion/excepciones-horario-fecha`

Ahead/behind verificado (snapshot 3B.0):

`0/0 SOBRE REFERENCIA LOCAL`

Remote live:

`NO VERIFICADO / NO FETCH EN 3B.0`

Última ruta local verificada durante 3B.0:

`/Users/jesusaldaircruzortiz/Desktop/Feelingpilates/feelingpilates`

Baseline reproducido:

```text
493/493 PASS
0 failures
0 errors
0 skipped
```

Comando:

`./mvnw test`

Working tree previo a materialización:

```text
?? auditoria/fase-2d1-diseno-ajustes-programacion-fecha.md
```

## Último estado reportado

Los siguientes datos son históricos y fueron reconciliados durante 3B.0.

Última branch reportada:

`operacion/excepciones-horario-fecha`

Último HEAD reportado:

`8c40594d2caf8b5230b364cb76cd8f48fe5ed98a`

Último working tree reportado:

```text
?? auditoria/fase-2d1-diseno-ajustes-programacion-fecha.md
```

Es decir: sólo el checkpoint F2D.1 aparecía como untracked en el último reporte.

El estado remoto exacto de esa branch debe volver a verificarse.

## Tests

Último baseline reportado antes de F2D.1:

```text
493/493 PASS
0 failures
0 errors
0 skipped
44 clases
```

Baseline reproducido durante 3B.0:

```text
493/493 PASS
0 failures
0 errors
0 skipped
```

Comando:

`./mvnw test`

## Estado de fases

### F2C

**CERRADA**

Es la última fase de implementación confirmada como cerrada.

### F2D.1 — Diseño de ajustes puntuales de programación

**DISEÑO\_APROBADO / CERRADA / PUBLICADA**

El gate final posterior a F2D.1.2 reportó:

```text
P0: 0
P1: 0
P2: 0
```

Checkpoint aprobado:

`auditoria/fase-2d1-diseno-ajustes-programacion-fecha.md`

SHA-256 aprobado:

`58af39f41b3bc089ebbd4ec67f684e270087ddf4eb695f2c7b55276d0aff352e`

La cadena histórica completa queda preservada en el checkpoint, el review original y las intervenciones/re-reviews F2D.1.1 y F2D.1.2.

`DISEÑO_APROBADO` describe exclusivamente el cierre de diseño F2D.1; no declara por sí solo implementación. El cierre posterior de F2D.2 se documenta separadamente.

### F2D.1.1 — Corrección post-review

**EJECUTADA**

El review original que motivó la intervención reportó:

```text
P0: 0
P1: 8
P2: 3
```

El re-review posterior reportó:

```text
P0: 0
P1: 1
P2: 0
```

Evidencia:

`auditoria/reviews/F2D.1.1-RE-REVIEW-POST-CORRECCION.md`

### F2D.1.2 — Aislamiento dark-launch

**EJECUTADA**

La intervención retiró `ImpactoAjustesEnExcepcionHorario` del alcance F2D.2 y lo difirió a una futura fase de activación/cutover.

El re-review final reportó `P0=0 / P1=0 / P2=0`, con `8/8` P1 cerrados y `15/15` mutaciones detectadas.

Evidencia:

`auditoria/reviews/F2D.1.2-RE-REVIEW-FINAL.md`

### F2D.2

**CERRADA / MATERIALIZADA / APROBADA_TECNICA_Y_DOCUMENTALMENTE / PUBLICADA_Y_VERIFICADA**

La implementación dark launch quedó materializada en el commit Git verificado `95900d8a1d787a24aff4ee4e10f69d540ce81339`; la documentación de cierre publicada quedó en `5c5d67e590260476372e5c8166062c0fb7429da1`. Una auditoría documental fresh e independiente, READ-ONLY, sobre la branch `operacion/excepciones-horario-fecha` y el HEAD `f6456310454a297397a63dac0c7b4c418bde9f5c` reportó `P0=0 / P1=0 / P2=1 editorial`, `DOCUMENTATION_GATE=PASS`, `PUBLICATION_CLOSURE_GATE=PASS` y `F2D2_DOCUMENTATION_STATUS=CLOSED`.

Evidencia persistente del audit:

`auditoria/reviews/F2D.2-REVIEW-DOCUMENTAL.md`

El P2 editorial se limitó a una frase stale del checkpoint sobre revalidación PostgreSQL. La frase describía el corte histórico del sandbox; la validación host posterior y distinta ya había resuelto el pendiente con `28/28 PASS` focalizados y `553/553 PASS` en la suite completa. El cierre documental no modifica el resultado histórico `BUILD FAILURE` ambiental del sandbox.

Los ejes vigentes son:

```text
F2D.2: CERRADA
design: DISEÑO_APROBADO
materialization: MATERIALIZADA
technical gate: PASS
documentation gate: PASS
publication: PUBLICADA / VERIFICADA
publication closure: PASS
runtime: DARK_LAUNCH
productive: NOT_PRODUCTIVE
cutover: false
authority: UNCHANGED — TurnoInstructor / LEGACY_VIVO / PRODUCTIVO
```

Intervención F2D.2:

**DISEÑADA / APROBADA POR GATE FINAL / MATERIALIZADA DOCUMENTALMENTE**

Gate final de la intervención:

```text
P0: 0
P1: 0
P2: 0
```

Intervención ejecutada:

`auditoria/intervenciones/F2D.2.2-CIERRE-CARRERA-AJUSTE-ID.md`

Review final:

`auditoria/reviews/F2D.2.2-RE-REVIEW-FINAL.md`

Las intervenciones F2D.2 original y F2D.2.1 se conservan exclusivamente como historia del gate y no son ejecutables. La evidencia de la implementación está en el checkpoint `auditoria/fase-2d2-implementacion-dark-launch-ajustes-programacion-fecha.md`, en la intervención F2D.2.2 y en el commit Git verificado; el baseline autorizado se usa sólo como evidencia física histórica.

La implementación incluye V47, código interno y tests F2D.2, pero esto no crea una autoridad productiva nueva. `TurnoInstructor` sigue siendo la autoridad productiva; no hay cutover ni fence implementados.

### F2E.1 — Preparación/diseño de migración controlada

**DISEÑO/PREPARACIÓN APROBADO / CERRADO**

El diseño materializado en `auditoria/fase-2e-preparacion-migracion-controlada.md` fue auditado
fresh e independientemente en modo `READ_ONLY`. El resultado `P0=0 / P1=0 / P2=0`,
`F2E1_DESIGN_DOCUMENT_GATE=PASS` y `READY_FOR_F2E1_CLOSURE=SI` queda persistido en
`auditoria/reviews/F2E.1-REVIEW-DISENO-PREPARACION.md`. El handoff que autorizó exclusivamente
esta preparación queda `CLOSED / HISTORICAL` en `auditoria/handoffs/HANDOFF-F2E-PREPARACION.md`.

Estado de la unidad:

```text
target: F2E / preparación
checkpoint F2E.1: PERSISTIDO / DESIGN_APPROVED / CLOSED
design/documentation gate F2E.1: PASS
P0=0 / P1=0 / P2=0
requires human decision: NO
data audit design: APPROVED
data audit material execution: NOT_PERFORMED
data source: DATA_SOURCE_NOT_AVAILABLE
implementation: NOT_AUTHORIZED
migration: NOT_AUTHORIZED
cutover: false
authority: TurnoInstructor / LEGACY_VIVO / PRODUCTIVO
runtime: DARK_LAUNCH
productive: NOT_PRODUCTIVE
```

El cierre de F2E.1 no autorizó implementación, migración productiva, F2E.2, cutover ni cambio de
autoridad. En ese corte histórico, `D03/D04` seguían `BLOCKING_FOR_NEXT_GATE`; `D08-D11` y los
demás blockers/deferred decisions conservaron la clasificación final de aquel checkpoint. La
unidad posterior de identidad/semántica/detector-only documentada a continuación cerró
`D03/D04/D09/D10/D11` sólo dentro de su scope aprobado y mantuvo `D08` diferida.

### F2E — Identidad, semántica legacy y detector-only

**DESIGN APPROVED / CLOSED**

La unidad `DESIGN / RESEARCH` materializada en
`auditoria/fase-2e-identidad-semantica-detector-read-only.md` fue auditada fresh e
independientemente en modo `READ_ONLY`. El resultado final queda persistido en
`auditoria/reviews/F2E-IDENTIDAD-DETECTOR-REVIEW-DISENO.md`:

```text
P0: 0
P1: 0
P2: 0
IDENTITY_LEGACY_DETECTOR_DESIGN_GATE: PASS
READY_FOR_DESIGN_CLOSURE: SI
requires human decision: NO
```

Estado de la unidad:

```text
checkpoint: PERSISTED / DESIGN_APPROVED / CLOSED
D03 detector-only: CLOSED
D04: CLOSED
D09 detector-only: CLOSED
D10 detector-only: CLOSED
D11 detector-only: CLOSED
D08: DEFERRED
data source: DATA_SOURCE_NOT_AVAILABLE
data audit execution: NOT_PERFORMED
implementation: NOT_AUTHORIZED
migration: NOT_AUTHORIZED
fence: NOT_AUTHORIZED
cutover: false
runtime: DARK_LAUNCH
productive: NOT_PRODUCTIVE
authority: TurnoInstructor / LEGACY_VIVO / PRODUCTIVO
```

D04 separa canónicamente `programming_target_identity` (referencia de occurrence efectiva),
`reservation_identity` (`reserva.id`) y `reservation_consumption_snapshot`
(`[horaInicio,horaFin)`). D03/D11 cierran sólo generación `0..N`, evidencia, clasificación,
ambigüedad, provenance y reporte sin selección ni persistencia. El cierre es exclusivamente de
diseño: no implementa detector, crosswalk, resolver o fence y no autoriza migración ni cutover.

### F2E — Detector read-only / materialización mínima del núcleo puro

**IMPLEMENTATION CLOSED / TECHNICAL IMPLEMENTATION GATE PASS / DARK_LAUNCH / NOT_PRODUCTIVE**

La unidad quedó materializada exclusivamente con 21 archivos production y 7 test/helper nuevos en
`src/**/com/feelingpilates/transicion/programacion/detector/**`; ningún archivo productivo tracked
existente fue modificado.

El technical audit fresh e independiente queda persistido en
`auditoria/reviews/F2E-DETECTOR-READ-ONLY-NUCLEO-PURO-REVIEW-IMPLEMENTACION.md`:

```text
Targeted tests: 37; Failures: 0; Errors: 0; Skipped: 0; Result: PASS / BUILD SUCCESS
P0=0 / P1=0 / P2=0
SCOPE_GATE / SEMANTIC_CONTRACT_GATE / IMMUTABILITY_GATE / RUNTIME_ISOLATION_GATE / TEST_GATE: PASS
TECHNICAL_IMPLEMENTATION_GATE: PASS
READY_FOR_IMPLEMENTATION_CLOSURE: SI
Implementation materialized: SI
Implementation performed: SI
Implementation: CLOSED
```

La suite más amplia permanece `OPTIONAL_ATTEMPT`: la última evidencia fresh disponible registró
118 errores ambientales Docker/Testcontainers, sin fallos conocidos del detector. No equivale a
PASS de suite global ni bloquea este slice, cuyo host validation no era requerido.

El checkpoint específico de implementación no es requerido por el workflow profile ni por las exit
conditions del handoff; `ESTADO-ACTUAL` y el review técnico son la evidencia competente.

## Unidad cerrada preservada — F2E adapters/snapshot design authority gap R1

```text
LAST CLOSED UNIT: F2E / adapters-snapshot design — authority gap R1
TYPE: DESIGN / RESEARCH — CORRECTIVE AMENDMENT
STATUS: COMPLETED / CLOSED
ACTIVE HANDOFF: NINGUNO — historical snapshot only
AUTHORITY GAP: RESOLVED_BY_CORRECTIVE_DESIGN / CLOSED
DESIGN CANONICAL: auditoria/fase-2e-diseno-adapters-read-only-snapshot-consistency.md
CORRECTIVE DESIGN AMENDMENT: MATERIALIZED
FRESH DESIGN AUDIT: PASS
P0 / P1 / P2: 0 / 0 / 0
DESIGN_GATE: PASS
READY_FOR_CORRECTIVE_DESIGN_CLOSURE: SI
REQUIRES HUMAN DECISION: NO
FINAL REVIEW: auditoria/reviews/F2E-ADAPTERS-SNAPSHOT-AUTHORITY-GAP-R1-DESIGN-REVIEW.md
CORRECTIVE HANDOFF: COMPLETED / CLOSED / HISTORICAL / NOT_ACTIVE
```

El diseño adapters/snapshot original conserva su `PASS` histórico. Esta unidad cerrada no se
reabre: permanece `COMPLETED / CLOSED / PASS / HISTORICAL`. Un re-audit downstream posterior de
R1 identificó un nuevo residual authority gap, con alcance distinto y sin alterar este cierre.

## Unidad cerrada — F2E adapters/snapshot residual authority gap R1 provenance + JPA transaction topology

```text
TARGET: F2E / adapters-snapshot design — residual authority gap R1 provenance + JPA transaction topology
TYPE: DESIGN / RESEARCH — CORRECTIVE AMENDMENT
STATUS: COMPLETED / CLOSED / PUBLISHED
FRESH_INDEPENDENT_DESIGN_AUDIT: PASS
P0 / P1 / P2: 0 / 0 / 0
PUBLICATION COMMIT: f23e91390d21ebd06040f3ed60f631e05d23d653
PUBLISHED DESIGN SHA-256: 6c72cba1f83fbc2bcf3b3219d8252d2410ec482e30ae85d30fd2eeb04e8883d8
TARGET CANONICAL: auditoria/fase-2e-diseno-adapters-read-only-snapshot-consistency.md
FINAL REVIEW: auditoria/reviews/F2E-ADAPTERS-SNAPSHOT-RESIDUAL-AUTHORITY-GAP-R1-PROVENANCE-JPA-TX-DESIGN-REVIEW.md
CORRECTIVE HANDOFF: COMPLETED / CLOSED / HISTORICAL / NOT_ACTIVE
CORRECTIVE DESIGN HANDOFF ACTIVE: NINGUNO
DOWNSTREAM R1 HANDOFF: ver unidad cerrada a continuación; CONSUMED_BY_R1 / NOT_ACTIVE
```

La enmienda residual publicada cierra exclusivamente la autoridad de identidad/provenance V2 y la
topología JPA de transacción/recurso R1. Las marcas internas `NOT_SELF_APPROVED` y
`PENDING_FRESH_AUDIT` del checkpoint conservan el estado histórico de la candidate al momento de
ser escrita; el lifecycle posterior competente queda persistido por este canónico y por el review
fresh independiente. Esa publicación del diseño no aprobó ni activó por sí sola el handoff R1;
la auditoría fresh posterior y la transición competente se registran a continuación.

## Unidad cerrada vigente — F2E R1 reserva reader JPA read-only

```text
F2E R1 residual design: PUBLISHED / CLOSED
R1 handoff physical content: CORRECTED_TO_FINAL_V2_DESIGN_AUTHORITY
ACTIVE HANDOFF: NINGUNO — R1 CLOSED; NO_NEXT_PHASE_AUTHORIZED
CONSUMED IMPLEMENTATION HANDOFF: auditoria/handoffs/HANDOFF-F2E-R1-RESERVA-READER-JPA-READ-ONLY.md
AUDITED HANDOFF SHA-256: 3fd71faca4d4c049ad5cb37b52bc6fd512509cf5b696bdc5c28d28cb966af8ef
PUBLISHED DESIGN SHA-256: 6c72cba1f83fbc2bcf3b3219d8252d2410ec482e30ae85d30fd2eeb04e8883d8
FRESH_INDEPENDENT_HANDOFF_DOCUMENT_AUDIT: PASS
P0 / P1 / P2: 0 / 0 / 0
HISTORICAL READY_TO_APPROVE_F2E_R1_HANDOFF: YES — APPROVAL CONSUMED_BY_R1
HANDOFF AUDIT REVIEW: auditoria/reviews/HANDOFF-F2E-R1-RESERVA-READER-JPA-READ-ONLY-REVIEW.md
R1 handoff: APPROVED / PUBLISHED / CONSUMED_BY_R1 / NOT_ACTIVE
R1 implementation: ACCEPTED / PUBLISHED / MATERIALIZED / IMMUTABLE_VALIDATED_CANDIDATE
R1 lifecycle: CLOSED
IMPLEMENTATION AUTHORITY: EXACTLY THE AUDITED HANDOFF SHA ABOVE / F2E R1 ONLY
R1 fresh technical validation / independent technical audit: PASS
GAP1 / GAP2 / GAP3 / GAP4 / GAP5: CLOSED
ORIGINAL TECH01 / TECH02 / TECH03 / TECH04 / TECH05 / TECH06 / TECH08 PROVENANCE: RECOVERED_AUTHORITATIVELY
TECH02 / TECH03 / TECH04 / TECH05 / TECH06: CLOSED
TECH01: CLOSED — INDEPENDENTLY_VERIFIED_NATIVE_FIRST_LATER_METADATA_PROOF
TECH08: CLOSED — EXACT_SHARED_NATIVE_METADATA_OVERLAP_ONLY
TECH07: CLOSED FOR_CURRENT_R1_HOST_INVARIANT ONLY
DOCUMENTATION: PUBLICATION_CLOSURE_MATERIALIZED / PENDING_INDEPENDENT_AUDIT
PRIOR ACCEPTANCE AUDIT/GATE: task_5f12bc24768c PASS / gate_b263068ee65c PASS
AUTHORITY ACCEPTANCE REQUIREMENT: SATISFIED_BY_INDEPENDENT_AUDIT_AND_COMPETENT_GATE
R1 acceptance: ACCEPTED / PUBLISHED
R1 publication: PUBLISHED — f5e0239d378119b9c1a5ec94e8f09db77cc4fe3c
PRIOR PUBLICATION POST-AUDIT/GATE: task_a5c106ff3d5a PASS / gate_65645a7533da PASS
PUBLICATION CLOSURE: MATERIALIZED / PENDING_INDEPENDENT_AUDIT
P2-EVIDENCE-01: NON_BLOCKING / PRESERVED
CURRENT CLOSURE RUN: run_fb92631a2300
CURRENT PRECOMMIT CLOSURE AUDIT / COMMIT AUTHORIZATION GATE: PENDING / NOT_EXECUTED
CURRENT CLOSURE DOCUMENTATION PUBLICATION / POST-CLOSURE AUDIT / FINAL GATE: PENDING / NOT_EXECUTED
CONSOLIDATED REVIEW: auditoria/reviews/F2E-R1-PROVENANCE-VALIDACION-ACEPTACION.md
NEXT ALLOWED WORKFLOW ACTION: INDEPENDENT_CLOSURE_AUDIT_ONLY; LATER STAGES REQUIRE ACTUAL COMPETENT GATES
NEXT FUNCTIONAL PHASE: NINGUNA / NOT_AUTHORIZED
R2-R6: NOT_AUTHORIZED
```

Las marcas internas `NOT_APPROVED`, `NOT_ACTIVE` e `IMPLEMENTATION_NOT_AUTHORIZED` del handoff y
del review residual conservan el estado histórico de esos artefactos al materializarse. No se
reescriben después del audit. El review fresh de handoff y este canónico competente registran la
transición histórica posterior a `APPROVED / PUBLISHED / ACTIVE`; la publicación exacta probada
permite registrar ahora `APPROVED / PUBLISHED / CONSUMED_BY_R1 / NOT_ACTIVE` sin alterar el
handoff físico ni reactivarlo. Su ruta y SHA siguen siendo autoridad de implementación e historia.

El estado histórico `ACTIVE` autorizó exclusivamente la implementación F2E R1 delimitada por el handoff exacto con
SHA-256 `3fd71faca4d4c049ad5cb37b52bc6fd512509cf5b696bdc5c28d28cb966af8ef`.
La activación histórica por sí sola no declaró ejecución. El corte físico posterior ahora acredita
la implementación R1 materializada, aceptada y publicada. R1 queda CLOSED, sin handoff activo,
sin siguiente fase autorizada, migración, data audit, cutover, R2-R6 ni cambio de autoridad productiva.

### Corte histórico — provenance y aceptación antes de la prueba nativa

Los párrafos siguientes conservan el corte `run_b0efaa8ebe42`: «vigente», «actual», OPEN,
PENDING y siguiente paso describen aquel momento. No son blockers presentes después de la
incorporación nativa; la vista vigente es la unidad cerrada arriba y el cierre de publicación abajo.

La materialización documental anterior `run_3c3b68d0f06b` y su authority gate
`gate_f598ddc359bd` conservan FAIL de autoridad por recovery scope acotado, sin reinterpretar
sus audits ni los preflight FAILs como PASS. La corrección documental vigente se materializa en
`run_b0efaa8ebe42 / task_f12663b70140 / ctx_9eed7ce97d17`, tras recuperación ampliada
`task_c42c90b259c2 / ctx_04b86b5c6d48`, reporte físico
`/tmp/feelingpilates-f2e-tech-recovery.uj4KrG/RECOVERY-REPORT.md` SHA-256
`a8462f86fb79edc55b5140241c80a18b70e31ed6380c36b18f2a7f0e3032d2ce`
y verificación independiente del coordinador. Supersede el missing-definition blocker por
recuperación autenticada, sin dispensar requisitos originales ni aprobar aceptación.
El candidato preexistente conserva
exactamente 21 archivos (11 main + 10 test/helper), path-set SHA-256
`f400a0602f95e318845da670bee4f819f057842adf8a60506564d5bd75e41d14` y content-manifest SHA-256
`e2b64abd6aba8a050df6184f6c5440d87db83a67182fb43e622f48cf96f4f3ce`, sobre branch
`operacion/excepciones-horario-fecha`, HEAD `a0ec85818b771d4ac924b427fa1e90244ea9fe8e`, upstream
local `origin/operacion/excepciones-horario-fecha`, staging vacío y tracked limpio antes del delta
documental. Los 21 archivos son baseline autorizado del usuario, inmutable y no atribuible al
documenter/corrector; el delta actual sólo modifica este canónico y el review consolidado ya
existente. Baseline dirty autorizado de entrada: ESTADO SHA-256
`bca2d82576a9da804a69f9a2c44e3e07db817a96e8f8a63629d1f6228ca6babb`
y review SHA-256 `189976c40f6a34171abe46e2f8391dca1e248bcf0d690a185cc3ec9bf22d2439`.

En `run_6d0dfb237a61`, los cinco targeted fresh pasaron 59 tests (14/4/6/28/7), la suite completa
69 suites/649 tests y el host separado 7 tests, todos exit0/BUILD SUCCESS y failures/errors/skips0.
El audit independiente `task_8a6b068015dd / ctx_ad561af5fcce / msg_1a1cde68af29` emitió
`PASS — FIVE ACCEPTANCE GAPS CLOSED AND TECHNICAL VALIDATION VERIFIED`; el gate
`gate_01a63c36e518 / task_4714a303190a` emitió PASS sólo para
`READY_FOR_F2E_R1_AUTHORITY_DOCUMENTATION_AND_ACCEPTANCE_MATERIALIZATION`, sin aceptar/publicar R1.
El review consolidado persiste manifest per-file, comandos/UTC/counts/exit, evidencia original
sellada, host/checksum/SQL y el audit físico; los summaries siguen siendo navegación.

El preflight real anterior `run_7fdc6b2552a6` conserva Worker A y B FAIL, y su gate
`gate_fbb14ecd0a05` conserva PASS resume-only. Las siete definiciones y mappings originales
TECH01/02/03/04/05/06/08 ya están RECOVERED_AUTHORITATIVELY desde informes independientes
literales S0 row265 a 2026-09-14T02:12:47.057Z, S1 row327 a 03:11:00.624Z y S2 row410 a
04:01:34.908Z, con asignaciones originales, session/cwd/HEAD/authority y sellos file/raw/text
persistidos en el review existente, junto con los ocho bloques originales y refinamientos adversos.
No se afirma que los originales fueran Git-tracked ni que /tmp/sessions tengan retención garantizada.
TECH01/TECH-01 son aliases ortográficos; no se renumeran AD, IA ni design gates.

TECH02/03/04/05/06 quedan CLOSED por sus definiciones originales + código físico del candidate e2 +
casos nominales XML fresh full retenidos + receipts/logs originales y audit técnico competente,
con mapping de autoridad/source/test/evidence/audit específico para cada uno en el review.
Ningún closure count histórico ni C0/C1/C2 self-report basta por sí solo.
TECH01 queda OPEN — ORIGINAL_REAL_JDBC_METADATA_ACCEPTANCE_EVIDENCE_UNPROVEN:
la metadata Proxy sintetiza ENTREGADA hostil, mientras ORIGINAL nativa pgjdbc sigue siendo
igual a CONFIGURADA/DESCRIPTOR. Las negativas primera/posterior ahora rechazan causalmente la
metadata manteniendo configurada autorizada, pero no prueban URL nativa real independientemente
hostil exigida por S2. TECH08 queda OPEN sólo por ese requisito metadata solapado; participating
hostile graph, cambio del delegate físico bound y demás casos obligatorios están corroborados.
Esto es insuficiencia de evidencia de aceptación original, no nuevo defecto productivo probado,
suite fallida, invalidación de GAP1–5/PASS técnico ni reapertura de implementación.

La definición original TECH07 también se recuperó; se preservan ausencia histórica del plan
configured/static/non-LLM HostValidator F2E R1 y su posterior reapertura independiente S2.
Su disposición vigente permanece CLOSED FOR_CURRENT_R1_HOST_INVARIANT ONLY, por Orca technical
Run actual y diseño27/28/37.10 + handoff10/13/14: PostgreSQL16/JPA/Flyway50 SUCCESS/currentV47/
no pending, SELECT-only/denied INSERT42501, manifest ordenado y scoped checksum equality.
Owner nominal `/Users/jesusaldaircruzortiz/FeelingPilatesOrchestrator` y mecanismos
HostValidator/Autopilot son OLD_PROCESS_ONLY, nunca retrospectivamente aprobados ni revividos.
Testcontainers satisface la invariante de implementación R1 vigente; no sustituye data audit material.

`P2-EVIDENCE-01` permanece NON_BLOCKING: arrays individuales JSON incompletos (incluido denied
INSERT), roots/logs originales y XML fresh full/host parseados independientemente corroboran los
outcomes; targeted XML fueron sobrescritos. No se fabrican records ni se reparan originales.
El audit/gate de autoridad independiente del Run actual permanece PENDING al corte documental,
sin inventar futuros IDs/veredictos ni autoaprobar. R1 sigue MATERIALIZED / NOT_ACCEPTED /
NOT_PUBLISHED; aceptación FAIL requerida mientras TECH01/08 no acrediten la prueba original.
Completar provenance ORIGINAL no equivale a suficiencia de CLOSURE evidence.
El siguiente paso exacto es audit fresh e independiente de autoridad/documentación y gate competente
sobre la corrección actual. La resolución del requisito original no acreditado necesita autorización
separada y evidencia específica; no autoriza automáticamente cambiar implementación, tests o
publicación. El PASS técnico permanece intacto.


Review consolidado:

[F2E-R1-PROVENANCE-VALIDACION-ACEPTACION.md](reviews/F2E-R1-PROVENANCE-VALIDACION-ACEPTACION.md)

## Autoridad y límites preservados

```text
TurnoInstructor: PRODUCTIVE AUTHORITY / LEGACY_VIVO / PRODUCTIVO
Pure detector: DARK_LAUNCH / NOT_PRODUCTIVE
Adapters R1: ACCEPTED / PUBLISHED / CLOSED / TECHNICAL_PASS / DARK_LAUNCH / NOT_PRODUCTIVE
Data source: DATA_SOURCE_NOT_AVAILABLE
Data audit: NOT_AUTHORIZED
D08: DEFERRED
Crosswalk / Resolver / Fence / Migration: NOT_AUTHORIZED
MIGRANDO: NO
NUEVA: NO
Cutover: NOT_AUTHORIZED / false
R1 implementation: ACCEPTED / PUBLISHED / IMMUTABLE_VALIDATED_CANDIDATE / NOT_REOPENED
R1 acceptance: ACCEPTED / PUBLISHED
R1 lifecycle: CLOSED
R1 handoff: APPROVED / PUBLISHED / CONSUMED_BY_R1 / NOT_ACTIVE
ACTIVE HANDOFF: NINGUNO
R1 publication: PUBLISHED — f5e0239d378119b9c1a5ec94e8f09db77cc4fe3c
PUBLICATION CLOSURE: MATERIALIZED / PENDING_INDEPENDENT_AUDIT
CURRENT CLOSURE AUDITS / GATES / DOCUMENTATION PUBLICATION: PENDING / NOT_EXECUTED
NEXT FUNCTIONAL PHASE: NINGUNA / NOT_AUTHORIZED
R2-R6: NOT_AUTHORIZED
Java / tests / test-only Spring topology: ACCEPTED / PUBLISHED UNDER THE EXACT CONSUMED R1 HANDOFF; NOT_REOPENED
DB change / SQL migration / productive Spring configuration / data audit / cutover: NOT_AUTHORIZED
Payments / Notifications / Capacity / Mobile: OUT_OF_SCOPE
```

Human/business decision: `NOT_REQUIRED`.

Technical design authority: `CLOSED / PUBLISHED`.

Implementation authority: `ACCEPTED / PUBLISHED / IMMUTABLE / BOUNDED BY EXACT CONSUMED R1 HANDOFF / NOT_REOPENED`.

Prior acceptance authority audit/gate: `task_5f12bc24768c PASS / gate_b263068ee65c PASS`.

Current closure Run `run_fb92631a2300`: precommit closure audit, commit authorization gate,
closure documentation publication, post-closure audit and final gate `PENDING / NOT_EXECUTED`.

Authority acceptance requirement: `SATISFIED_BY_INDEPENDENT_AUDIT_AND_COMPETENT_GATE`.

Prior acceptance documentation authority audit: `PASS — F2E R1 AUTHORITY PROVENANCE COMPLETE AND ACCEPTANCE VERIFIED`; auditor fresh independiente, no autoaudit del documenter. Los nuevos bytes de cierre aún requieren su auditor independiente.

Los PENDING conservados abajo pertenecen al corte histórico de entrega documental antes del audit/gate.
Los resultados competentes reales posteriores se registran en la resolución terminal histórica de aceptación;
no se atribuyen al documenter ni se reescribe su entrega pendiente como aceptación.

## F2E R1 — resolución de autoridad histórica posterior al corte documental anterior

Este bloque conserva el corte histórico anterior, incluido su FAIL y TECH01/08 OPEN;
no es el estado vigente tras la incorporación nativa registrada a continuación.

Run histórico `run_b0efaa8ebe42`: recuperación ORIGINAL completa y corrección documental
auténtica verificadas; no aceptación. El auditor fresh independiente
`task_7a60734d59d2 / ctx_848f387aa672 / msg_902b5e1687fb` emitió terminal FAIL:
`ORIGINAL F2E-R1-TECH-01 REAL/NATIVE HOSTILE JDBC METADATA ACCEPTANCE EVIDENCE UNPROVEN;
F2E-R1-TECH-08 OPEN FOR THE OVERLAPPING REQUIREMENT`.
Report físico: `/tmp/feelingpilates-f2e-tech-recovery.uj4KrG/AUDIT-AUTHORITY-REPORT.md`,
SHA-256 `d8ff8c93361b6605ddbee1911b93030cb603c5381d808d6bbae6a70fda48008b`.
Su unique worker_done fue aceptado; Task/Dispatch settled y terminal released,
con transcript archivado. Los originales y cierres02–06 están acreditados; TECH07
permanece CLOSED FOR_CURRENT_R1_HOST_INVARIANT ONLY, sin cierre retroactivo del mecanismo viejo.

Gate-only Task `task_cf9c20914c19`, gate `gate_ad9e29b8d8e8`: inicialmente pending
`2026-09-16T17:27:43Z`, resolución `FAIL` en `2026-09-16T17:28:03Z`, Task failed.
El bloqueo ya NO es provenance faltante: falta la prueba original de metadata JDBC real
independientemente hostil en primeras y posteriores observaciones. Los casos actuales
mantienen ORIGINAL autorizada y generan ENTREGADA hostil mediante proxy de metadata;
no se eleva esa prueba causal a evidencia nativa ni se infiere un nuevo defecto de backend.

Los PENDING anteriores son exclusivamente el corte histórico de entrega documental;
esta resolución competente fue el estado de aquel corte. R1 sigue MATERIALIZED / NOT_ACCEPTED /
NOT_PUBLISHED; publicación NOT_AUTHORIZED. TECH01 y TECH08 siguen OPEN. El PASS técnico
`run_6d0dfb237a61 / gate_01a63c36e518`, GAP1–5 CLOSED y P2-EVIDENCE-01 NON_BLOCKING
no cambian. No se rerun tests ni se reabre implementación. TurnoInstructor permanece
LEGACY_VIVO / PRODUCTIVO, dark launch PRESERVED, cutover false/NOT_AUTHORIZED y R2–R6
NOT_AUTHORIZED; ningún mecanismo histórico fue ejecutado o revivido.

Esta entrada sólo persiste el resultado competente posterior: el auditor inspeccionó
ESTADO SHA `ba96e00deacc04df2625da78d5ead598b573158a4d078ca57a291b03d3ca588b` y
review SHA `cf92b2e60f29c1506b4cd9187ffb56ceed703f31007806bf5ecf75d6549b52e3`, antes
de esta metadata terminal. No se atribuye al auditor inspección de bytes añadidos después;
el coordinador verifica este delta de resultados, sin cambiar mapping/proofs ni aprobar aceptación.
Candidate exact21 sigue path SHA `f400a0602f95e318845da670bee4f819f057842adf8a60506564d5bd75e41d14`
y content SHA `e2b64abd6aba8a050df6184f6c5440d87db83a67182fb43e622f48cf96f4f3ce`.
Siguiente paso requiere autorización separada para resolución específica del requisito original
y nuevo review/gate competente; no autoriza automáticamente código, tests, publicación o cutover.

## F2E R1 — corte documental auditado: residual nativo incorporado, aceptación entonces pendiente

Este bloque conserva la entrega previa al audit/gate. El estado entonces ACCEPTED / NOT_PUBLISHED
se registra en la resolución terminal histórica posterior; el cierre vigente queda abajo.

Run documental `run_6b83c0a8f3a4 / task_41443902b374 / ctx_77c671acdfb6`, rol
FRESH_F2E_R1_FINAL_AUTHORITY_DOCUMENTER, documentación exclusivamente.
Resultado de entrega: `AUTHORITY_UPDATE_MATERIALIZED_PENDING_ACCEPTANCE_AUDIT_AND_GATE`.
TECH01 CLOSED por prueba nativa first/later verificada independientemente; TECH08 CLOSED
sólo por ese solapamiento exacto. Los OPEN y FAIL del corte previo siguen siendo historia
verdadera, sin ser blockers presentes ni transformarse retrospectivamente en PASS.
R1 sigue `MATERIALIZED / IMMUTABLE_VALIDATED_CANDIDATE / NOT_ACCEPTED /
PENDING_INDEPENDENT_ACCEPTANCE_AUDIT_AND_GATE`; publicación `NOT_PUBLISHED / NOT_AUTHORIZED`.

Fuente real: residual Run `run_8b529b21e8ad`, analyst
`task_60e34917f8f4 / ctx_90cb3b888ddb` EVIDENCE_ONLY_CLOSURE_FEASIBLE (plan);
auditor `task_ecd83cdad97f / ctx_ef96b159acfc`
`PASS — TECH-01/08 NATIVE JDBC METADATA RESIDUAL VERIFIED`;
gate-only `task_fd4e5d9c44d9 / gate_4ab844c58bbb` PASS a 2026-09-16T17:50:43Z,
`PASS — TECH-01/08 RESIDUAL CLOSED / READY_FOR_ACCEPTANCE_AUTHORITY_UPDATE`.
Ese PASS permite esta incorporación, no acepta R1. Audit/gate de aceptación actual
`PENDING_INDEPENDENT_ACCEPTANCE_AUDIT_AND_GATE`: ningún ID futuro/final PASS inventado.
Sección10 del [review consolidado](reviews/F2E-R1-PROVENANCE-VALIDACION-ACEPTACION.md)
retiene dictamen literal, input original completo, markers y sellos durables de evidencia.

Driver42.7.11/PostgreSQL16.14, PgDatabaseMetaData.getURL devuelve PgConnection.creatingURL;
propiedad nativa soportada disableColumnSanitiser=true, mutacionMetadatos null, wrapper sólo
registra/reenvía. CONFIGURADA==DESCRIPTOR `jdbc:postgresql://localhost:54344/test`;
ORIGINAL==ENTREGADA nativa `jdbc:postgresql://localhost:54344/test?disableColumnSanitiser=true`.
Comparación configurada380 pasa y metadata381 rechaza causalmente antes de capture/probes.
Primera significa primera invocación harness fresh, no primera metadata del bootstrap Hibernate.
Primera negativa → éxito canónico → negativa posterior → éxito canónico final: 4 probes,
2negativas/2positivas exit0, no JUnit/full suite. Negativas cero nuevo reader SQL/output y
completion real abort; positivas una reserva/exact3SELECT. Compilación exact21 a classes externas
es evidencia original previa, no ejecutada por este documenter; candidato permanece congelado.
Matriz S1 sigue proxy simulation sobre participantes reales; dictamen independiente literal
adjudica matriz previa + query nativa first/later contra residual exacto S2, sin debilitar originales.

Scope medido dos reservas, filas2, tabla BEFORE==AFTER
`0a98a347d56b645ef8856dbad59056ee13b2d3b110b026219338c7ca31e45ec1` y slice
`72c8a5a30980264b1fc9aa3b3414ac4372661908e1b40b86cdb3a037d8b0ba2d`, 0mutation attempts.
Migraciones/setup/fixtures/grants privilegiados EXISTENTES contienen writes ANTES de medición:
no zero global DB writes. Launcher fallido exit1/0probes antes de fixture se conserva; mismatch
run2-before wrapperCommand genérico run se declara, receipt final original run2 correcto.
No originales reparados, nuevos paths o ejecución técnica/Git/legacy por este rol.

Binding de la entrega pendiente: branch `operacion/excepciones-horario-fecha`, HEAD
`a0ec85818b771d4ac924b427fa1e90244ea9fe8e`, staging vacío, exact21+review=22untracked,
sin extras; candidate path SHA `f400a0602f95e318845da670bee4f819f057842adf8a60506564d5bd75e41d14`,
content SHA `e2b64abd6aba8a050df6184f6c5440d87db83a67182fb43e622f48cf96f4f3ce`, diseño
SHA `6c72cba1f83fbc2bcf3b3219d8252d2410ec482e30ae85d30fd2eeb04e8883d8`, handoff
SHA `3fd71faca4d4c049ad5cb37b52bc6fd512509cf5b696bdc5c28d28cb966af8ef`, main technical
`run_6d0dfb237a61 / gate_01a63c36e518 PASS`, provenance histórico
`run_b0efaa8ebe42 / gate_ad9e29b8d8e8 FAIL` y residual
`run_8b529b21e8ad / gate_4ab844c58bbb PASS`. Antes externos preservados en
`/tmp/feelingpilates-f2e-final-authority.DVYXaM/*.before`: ESTADO SHA
`267ae9f6b6f76a7bd86d9e520f9c65ee7ad04bb9a9f4615e7b6a68b3c07bcae1`, review SHA
`4801262ca2dfc63af6d597f849ec66db512ab730c2b753ce1dbccd9e49696a44`; único delta ambos docs.
Native audit SHA `4782b8988dddbef65f7d160025666d9636adaa54bbc32f6e14bdb7757dbaf573`,
run2 receipt SHA `5313c1969b6c41a39568cfb6e2fcb93513c89b8f3b66bfa5cf55742b1d7af10f`,
log SHA `ef7ee27ae58f79f6812e9999da254fda230358093cbd5ccba5baee9822617b8f`.

GAP1–5 CLOSED y prior fresh PASS técnico 59targeted/69suites649full/host7, audit
`task_8a6b068015dd / ctx_ad561af5fcce` PASS — FIVE ACCEPTANCE GAPS CLOSED AND TECHNICAL
VALIDATION VERIFIED siguen intactos, no ejecutados este Run. TECH02–06 mappings preservados;
TECH07 CLOSED FOR_CURRENT_R1_HOST_INVARIANT ONLY, mecanismo histórico OLD_PROCESS_ONLY.
P2-EVIDENCE-01 NON_BLOCKING conserva arrays JSON incompletos y targetedXML overwritten/unretained;
no repair fabricado. Diseño CLOSED/PUBLISHED y handoff APPROVED/PUBLISHED/ACTIVE preservados.
TurnoInstructor LEGACY_VIVO/PRODUCTIVO, dark launch PRESERVED, cutover NOT_AUTHORIZED/false,
R2–R6 NOT_AUTHORIZED y Payments/Notifications OUT_OF_SCOPE. Siguiente paso único:
audit fresh independiente de aceptación/autoridad/documentación y gate competente sobre este
binding y delta; este documenter no se autoaudita, no crea gate ni aprueba aceptación/publicación.

## F2E R1 — resolución terminal competente histórica: ACCEPTED / NOT_PUBLISHED

Este bloque conserva íntegro el resultado y los límites de `run_6b83c0a8f3a4` antes de publicar.
Sus «actual», NOT_PUBLISHED y siguiente paso son históricos; el estado vigente es el cierre
de publicación posterior. Aquel estado pasado no se transforma retrospectivamente en PUBLISHED.


Run actual `run_6b83c0a8f3a4`, coordinator PRODUCT_DELIVERY_COORDINATOR /
FINAL_ACCEPTANCE_GATE_COORDINATOR. El documenter `task_41443902b374 / ctx_77c671acdfb6`
entregó sólo AUTHORITY_UPDATE_MATERIALIZED_PENDING_ACCEPTANCE_AUDIT_AND_GATE;
unique worker_done `msg_8933fa1c36e9`, settled/released, transcript captured.
Aceptación no se atribuye al documenter, al gate residual ni a tests verdes por sí solos.

Audit NUEVO, fresh, independiente, READ_ONLY:
`task_5f12bc24768c / ctx_f99011b64799 / msg_17995a84cc7d`.
Unique worker_done succeeded a `2026-09-16T18:10:36Z`; Task/Dispatch settled,
terminal released, transcript captured y delivery completo acknowledged.
Verdicto: `PASS — F2E R1 AUTHORITY PROVENANCE COMPLETE AND ACCEPTANCE VERIFIED`.
Report físico `/tmp/feelingpilates-f2e-final-authority.DVYXaM/AUDIT-ACCEPTANCE-REPORT.md`,
SHA-256 `edc5b95b02dd1cb38cb3464f474302f8f8c410052ffc8372b69c4baeae619ac6`.
P0=0 / P1=0 / P2=1 NON_BLOCKING existente; ningún residual bloqueante.

Las quince respuestas independientes fueron YES: provenance auténtica de ocho TECH;
TECH01 cumple S0/S1/S2 por metadata nativa first/later sin source change; TECH08 sólo overlap;
TECH02–06 soportados; TECH07 estrechamente actual; GAP1–5 cerrados sobre exact snapshot;
todas las pruebas bind a content SHA exacto; FAIL históricos verdaderos; P2 transparente y
no bloqueante; cero implementation delta; TurnoInstructor productivo; dark launch preservado;
cutover/R2–R6 no autorizados; aceptación soportable separadamente de publicación.
El auditor verificó cero diferencias en sus 460 archivos de snapshot y 35 evidencias originales.

Gate-only Task coordinator-owned `task_59b41e1a65f6`, gate `gate_b263068ee65c`,
opciones PASS/FAIL: inicialmente `pending` a `2026-09-16T18:11:17Z`,
resolución real `PASS` a `2026-09-16T18:11:25Z`.
Semántica EXACTA:
`PASS — F2E R1 ACCEPTED / NOT_PUBLISHED / READY_FOR_PUBLICATION_PREFLIGHT`.
Se contrastó nuevamente integridad física antes de crear/resolver el gate; heartbeat o borrador
nunca sustituyeron el único worker_done terminal.

### Identidad exacta de aceptación

| Binding | Valor |
| --- | --- |
| Branch | `operacion/excepciones-horario-fecha` |
| Baseline HEAD | `a0ec85818b771d4ac924b427fa1e90244ea9fe8e` |
| Exact21 candidate path-set SHA-256 | `f400a0602f95e318845da670bee4f819f057842adf8a60506564d5bd75e41d14` |
| Candidate-content manifest SHA-256 | `e2b64abd6aba8a050df6184f6c5440d87db83a67182fb43e622f48cf96f4f3ce` |
| Design CLOSED / PUBLISHED SHA-256 | `6c72cba1f83fbc2bcf3b3219d8252d2410ec482e30ae85d30fd2eeb04e8883d8` |
| Sole handoff APPROVED / PUBLISHED / ACTIVE SHA-256 | `3fd71faca4d4c049ad5cb37b52bc6fd512509cf5b696bdc5c28d28cb966af8ef` |
| Main technical | `run_6d0dfb237a61 / gate_01a63c36e518 PASS` |
| Provenance recovery / previous acceptance | `run_b0efaa8ebe42 / gate_ad9e29b8d8e8 FAIL` histórico preservado |
| Native residual | `run_8b529b21e8ad / gate_4ab844c58bbb PASS` |
| Main independent technical audit | `task_8a6b068015dd / ctx_ad561af5fcce` PASS |
| Native independent technical audit | `task_ecd83cdad97f / ctx_ef96b159acfc` PASS; SHA `4782b8988dddbef65f7d160025666d9636adaa54bbc32f6e14bdb7757dbaf573` |
| Current independent acceptance audit | `task_5f12bc24768c / ctx_f99011b64799` PASS |
| Current acceptance gate | `task_59b41e1a65f6 / gate_b263068ee65c PASS` |

Los 18 prerrequisitos de aceptación están satisfechos: design/handoff válidos, exact21
congelado, GAP1–5 CLOSED, TECH01 CLOSED por prueba nativa, TECH02–06 CLOSED,
TECH07 CLOSED FOR_CURRENT_R1_HOST_INVARIANT ONLY, TECH08 CLOSED sólo por overlap,
ambos audits técnicos y sus gates PASS, P2 transparente/no bloqueante, fuente byte-idéntica.
El audit independiente actual y la integridad final fundamentan esta transición competente.
59 targeted / 69 suites y 649 full / host7 pertenecen a `run_6d0dfb237a61`;
native4 (2negativas/2positivas) pertenece a `run_8b529b21e8ad`.
Este Run documental no ejecutó tests, Maven, JDBC, SQL, compilación o containers.

### Bytes auditados y metadata terminal

El auditor inspeccionó ESTADO SHA
`692fc8b7d4271e04d5851db4c0bf38e66ca7500e067f71e2a473d2e6ce09553b`
y review SHA `0c6969f54fd3e27cc28caf8857683e0f613b4d0e8c74251a99639a7029a0840d`,
ambos con aceptación pendiente al corte. Copias byte-exactas externas:
`/tmp/feelingpilates-f2e-final-authority.DVYXaM/*.audited`.
Estos nuevos estados/IDs terminales sólo se persisten DESPUÉS del audit y gate reales PASS,
en los mismos dos documentos, como reconciliación terminal expresamente autorizada.
No se afirma que el auditor inspeccionó bytes futuros. El coordinador verifica el delta de
resultado y la identidad de los 21 archivos sin alterar definiciones S0/S1/S2, mapping,
input nativo, proofs, sellos originales o FAIL históricos; no autoaudita implementación.

Estado terminal: R1 `ACCEPTED / NOT_PUBLISHED`; candidate sigue
`MATERIALIZED / IMMUTABLE_VALIDATED_CANDIDATE`.
Publication `NOT_PUBLISHED / READY_FOR_PUBLICATION_PREFLIGHT`;
publication execution `NOT_AUTHORIZED_IN_THIS_RUN`.
TurnoInstructor `LEGACY_VIVO / PRODUCTIVO`, dark launch `PRESERVED / NOT_PRODUCTIVE`,
cutover `NOT_AUTHORIZED / false`, R2–R6 `NOT_AUTHORIZED`.
DATA_SOURCE_NOT_AVAILABLE / material data audit NOT_AUTHORIZED y Payments/Notifications
OUT_OF_SCOPE permanecen. Autopilot/FeelingPilatesOrchestrator/HostValidator siguen
OLD_PROCESS_ONLY; nada legado ejecutado, revivido o retroactivamente aprobado.
P2-EVIDENCE-01 `NON_BLOCKING`: arrays JSON incompletos y targeted XML overwritten/unretained,
logs/root counts y XML full/host retenidos corroborantes; ningún repair/fabricación.
Cero Java/test/config/SQL/migration mutation; staging EMPTY; ningún commit/push/publicación.
Sólo un PUBLICATION_PREFLIGHT futuro separado, con autorización y gate propios, es posible;
no publicación, activación productiva, cutover o R2–R6 en este Run.

## F2E R1 — cierre de publicación vigente: CLOSED / ACCEPTED / PUBLISHED

Run de cierre `run_fb92631a2300`; documenter exclusivo
`task_9a3a4512c780 / ctx_9dc3f5dc789f`, rol `F2E_R1_PUBLICATION_CLOSURE_DOCUMENTER`.
Entrega: `DOCUMENTATION_CLOSURE_MATERIALIZED_PENDING_INDEPENDENT_AUDIT`.
Este cierre reconcilia la publicación de implementación ya probada; no es audit, gate,
coordinación o publicación de estos nuevos bytes. R1 CLOSED está expresamente autorizado
por esa publicación competente, sin exigir un futuro SHA de cierre ni inventarlo.

| Identidad publicada exacta | Valor |
| --- | --- |
| Branch | `operacion/excepciones-horario-fecha` |
| Commit publicado / baseline verificado del cierre | `f5e0239d378119b9c1a5ec94e8f09db77cc4fe3c` |
| Único parent | `a0ec85818b771d4ac924b427fa1e90244ea9fe8e` |
| Publication canonical set | 23 paths:22 NEW /1 MODIFIED |
| Commit publication manifest SHA-256 | `0c305f556c6753b130a53a18b2e510f49318f6d7f9fb39c669f5a2e1d5bd9b80` |
| Implementación exact21 path-set SHA-256 | `f400a0602f95e318845da670bee4f819f057842adf8a60506564d5bd75e41d14` |
| Implementación exact21 content manifest SHA-256 | `e2b64abd6aba8a050df6184f6c5440d87db83a67182fb43e622f48cf96f4f3ce` |
| Diseño CLOSED / PUBLISHED SHA-256 | `6c72cba1f83fbc2bcf3b3219d8252d2410ec482e30ae85d30fd2eeb04e8883d8` |
| Handoff publicado consumido SHA-256 | `3fd71faca4d4c049ad5cb37b52bc6fd512509cf5b696bdc5c28d28cb966af8ef` |
| ESTADO publicado / preclosure SHA-256 histórico | `94c477fca3d17113b00d2f3d35ca3f741b903b3e3fbd043ffd9f38af5d4d3237` |
| Review resealed publicado / preclosure SHA-256 histórico | `c300e851a313b252fd1b12e32816540ff5939edd4dc44125fb6829033698f1e4` |

Cadena real preservada: preflight original `run_4a37e5400083 / gate_f48f00abaf3e`
y manifest `c699ab84e194ad31bd1918cd5e7f9af1aa6078519cfbbc1eaca33dadf3c4c708`
son HISTORICAL_ONLY. Primera publicación `run_683c285000f8` falló cached diff check exit2,
18 líneas×2 espacios finales, cero commits/pushes. Higiene/reseal
`run_89649c4139e8 / gate_629f95236c32 PASS` eliminó exactamente36 espacios y nada más,
semántica unchanged; review original histórico
`cfd744c610e5f983527c6506e53a3a90402e74600625bacbec1150f9a89a4fe6` preservado como tal.
Tras preflight resealed previo `run_0e78daeb44b4 / gate_89bc92a8bae5`, el fresh preflight.2
`run_b5c5d0cc61a0 / gate_cb4bc2d34c43 PASS` verificó el manifest resealed exacto.
Ninguno reemplaza la ejecución/publicación competente posterior ni reescribe FAIL anteriores.

Publicación real `run_8b913c526294`: staged audit independiente
`task_b93db209e206 / ctx_bdb22a3f225b`
`PASS — F2E R1 RESEALED STAGED ACCEPTED SNAPSHOT VERIFIED`; authorization gate
`task_4594a303de2c / gate_bd04ea00c795`
`PASS — AUTHORIZED_TO_COMMIT_AND_PUSH_EXACT_RESEALED_F2E_R1_SNAPSHOT`.
Un único commit f5 de parent a0 y un único push normal nonforce fast-forward con refspec
exactSHA→`refs/heads/operacion/excepciones-horario-fecha` en origin, sin tags ni otros refs.
Post-publication audit NUEVO fresh independiente `task_a5c106ff3d5a / ctx_e16589253474`:
`PASS — F2E R1 RESEALED ACCEPTED SNAPSHOT PUBLISHED EXACTLY`.
Completion gate real `task_4b0c8ac25cd0 / gate_65645a7533da`, PASS a
`2026-09-16T20:20:30Z`:
`PASS — F2E R1 RESEALED ACCEPTED SNAPSHOT PUBLISHED / READY_FOR_PUBLICATION_CLOSURE`.
Consulta read-only actual gate-list sobre aquel Run/Task confirma resolved/PASS.
HEAD/upstream/origin live=f5 y ahead/behind0/0 verificados, sin fetch.
Entrada de esta materialización: staging EMPTY, worktree CLEAN, untracked0; los dos docs
coincidían físicamente con sus blobs inmutables f5 antes del delta; exact21 permanece intacto.

La sección12 del [review consolidado](reviews/F2E-R1-PROVENANCE-VALIDACION-ACEPTACION.md)
persiste cronología, identidades, receipts y sellos de los originales físicos bajo
`/tmp/feelingpilates-f2e-publication-execution2.fEAgdl/` y snapshots de entrada bajo
`/tmp/feelingpilates-f2e-publication-closure.HwP7MH/`; son custodia local sin retención garantizada.
El review anterior completo es prefijo byte-exacto; definiciones/citas/mappings no se reescriben.
Los estados históricos MATERIALIZED/NOT_ACCEPTED, ACCEPTED/NOT_PUBLISHED, authority FAIL
gate_f598ddc359bd y gate_ad9e29b8d8e8, residual nativo y P2 conservan su significado original.

Estado vigente: diseño `CLOSED / PUBLISHED`, handoff `APPROVED / PUBLISHED / CONSUMED_BY_R1 /
NOT_ACTIVE`, implementación `ACCEPTED / PUBLISHED / IMMUTABLE`, R1 lifecycle `CLOSED`,
publicación `PUBLISHED`, cierre documental `MATERIALIZED / PENDING_INDEPENDENT_AUDIT`.
`ACTIVE HANDOFF: NINGUNO`; ruta consumida exacta
`auditoria/handoffs/HANDOFF-F2E-R1-RESERVA-READER-JPA-READ-ONLY.md` conservada como autoridad
de implementación e historia, sin reactivación. `NEXT FUNCTIONAL PHASE: NINGUNA / NOT_AUTHORIZED`.
TECH01–06/08 y GAP1–5 CLOSED; TECH07 `CLOSED FOR_CURRENT_R1_HOST_INVARIANT ONLY`, técnico PASS.
59 targeted/69 suites649full/host7 pertenecen a `run_6d0dfb237a61`; native4 a
`run_8b529b21e8ad`, ninguna prueba nueva de este Run. `P2-EVIDENCE-01 NON_BLOCKING / PRESERVED`:
arrays JSON incompletos/deniedINSERT omitted, targetedXML overwritten/unretained, roots/logs y
full/hostXML retenidos corroborantes; no fabricación ni repair de originales.
TurnoInstructor `LEGACY_VIVO / PRODUCTIVO`, R1 `DARK_LAUNCH / NOT_PRODUCTIVE`, dark launch
PRESERVED, cutover `NOT_AUTHORIZED / false`, R2–R6 NOT_AUTHORIZED; Payments/Notifications
OUT_OF_SCOPE. Data source DATA_SOURCE_NOT_AVAILABLE; data audit material/migration/fence
DEFERRED/NOT_AUTHORIZED; Autopilot/FeelingPilatesOrchestrator/HostValidator OLD_PROCESS_ONLY.

Workflow actual `run_fb92631a2300`: precommit closure audit, commit authorization gate,
publicación de docs de cierre, post-closure audit y final gate `PENDING / NOT_EXECUTED`.
Los PASS anteriores sólo cubren aceptación/publicación previa; no cubren nuevos bytes de cierre.
Siguiente acción exacta: audit independiente del cierre materializado; las etapas posteriores
requieren resultados/gates competentes reales. No self-audit, futuros IDs/veredictos inventados,
commit/push de este cierre ya ejecutados ni nueva fase funcional inferida.

## F2E R2 — autoridad de diseño materializada y auditada, pendiente de publicación

Corte terminal documental 2026-09-16, Run `run_df3cbaebd5d7`.
El protocolo y el gate precedente `run_b000b8a5b647 / gate_236c6b0bdf43 PASS`
autorizaron únicamente `R2_DESIGN_AUTHORITY_MATERIALIZATION`.
Clasificación previa `R2_DESIGN_AUTHORITY_INCOMPLETE`: tres gaps de integración
A/B/C, no indefinición funcional ni autorización de implementación.

Baseline físico fresco: branch `operacion/excepciones-horario-fecha`,
HEAD/upstream/origin live `6c2eacc870499e74ead74c1851630f9f53b1c676`,
ahead/behind0/0, CLEAN/index EMPTY/untracked0 al inicio; sin fetch.
R1 es terminal `CLOSED / ACCEPTED / PUBLISHED / DARK_LAUNCH_PRESERVED`,
`run_fb92631a2300 / gate_7e4a087bd1ce PASS`, resolved2026-09-16T20:51:36Z,
commit de cierre publicado6c2 ancestral de esta nueva unidad. Los PENDING en
las secciones documentales R1 anteriores son cortes de materialización históricos;
el cierre posterior fue corroborado físicamente por preflight y auditor fresh
(gate real, HEAD/upstream/live). No se reabre ni reescribe R1.

### Artefactos locales R2 y provenance sellada

| Artifact / identidad | Estado actual / SHA-256 |
| --- | --- |
| Diseño R2 | [fase-2e-r2-diseno-lector-turno-legacy-integracion.md](fase-2e-r2-diseno-lector-turno-legacy-integracion.md), MATERIALIZED / AUDITED / COMPLETE / NOT_PUBLISHED; db4673dd0705c41e62d0b77339d45b2cd1e87c51e26064112ad95b843dafe9cf |
| Research handoff R2 | [HANDOFF-F2E-R2-DISENO-LECTOR-TURNO-LEGACY.md](handoffs/HANDOFF-F2E-R2-DISENO-LECTOR-TURNO-LEGACY.md), DESIGN/RESEARCH provenance/profile, NOT_ACTIVE_IMPLEMENTATION_HANDOFF / NOT_PUBLISHED; 221347b46c5032b384a306c61908c8fd0f2c26076455609fb880098be8f0d2d4 |
| Review R2 | [F2E-R2-REVIEW-DISENO-LECTOR-TURNO-LEGACY.md](reviews/F2E-R2-REVIEW-DISENO-LECTOR-TURNO-LEGACY.md), informes independientes originales completos FAIL y fresh PASS, receipt coordinador; 0863b2a704f56b9d55a141edb3d191629c25d35f8706a66b7c878e176564b785 |
| Canónico primario D, read-only | CLOSED / PUBLISHED; 6c72cba1f83fbc2bcf3b3219d8252d2410ec482e30ae85d30fd2eeb04e8883d8 |
| R1 exact21 path-set / content preservados | f400a0602f95e318845da670bee4f819f057842adf8a60506564d5bd75e41d14 / e2b64abd6aba8a050df6184f6c5440d87db83a67182fb43e622f48cf96f4f3ce |

La nueva autoridad precisa solamente gaps R2; las reglas existentes D8/12/13/18–30
continúan. D36–37 sigue R1-only salvo fundamentos neutrales compartidos adoptados
explícitamente; ningún contexto/DTO/ReservaSQL/mapper/enum/identidad R1 se vuelve
genérico por analogía. Los headers PENDING_FRESH_REAUDIT_AND_GATE en inputs R2
sellados describen la entrega anterior del corrector; su aceptación documental
posterior es el informe independiente reproducido en review y el gate real aquí.
No se cambian los bytes auditados para introducir resultados futuros en su preimage.
El review conserva fuente/hashes, distingue NORMATIVE anterior, SUPPORTING físico y
nuevas decisiones R2, y no atribuye al auditor este estado postgate.

### Worker, CORRECT y re-audit cronológicos

Materializador fresh `task_c195cda67de7 / ctx_c45b806cebbf` entregó
`R2_DESIGN_AUTHORITY_MATERIALIZED`; complete/released/transcript captured.
Auditor inicial separado `task_1c407b9a88a1 / ctx_3c018d86ea0f` emitió
**FAIL — F2E R2 DESIGN AUTHORITY REMAINS INCOMPLETE**, P0=0/P1=2:
B-01 SQL implícita getSchema/getCatalog fuera del catálogo5, AB-02 corte de MEMBERS
antes de ASSIGNMENTS impedía K exacto de header inválido con keys suficientes.
Informe histórico original SHA
`bda484b6140405491740b6c193fd120d06dbd3550cc26ee458866500f489109a`.
Preimages diseño `3a6eb4e4b38ff90b66e85680c407ae808dbba471b21d4be79dd2c321cd77afaa`
y research `e9c56d7beb47f8558a756fe2e132aa3874069fd902cbb0d26270646256c1bb83`
se preservan externamente; FAIL inicial no se convierte en PASS retroactivo.

CORRECT documental acotado, misma allowlist de dos candidatos, corrector fresh
`task_a2e7ce748158 / ctx_ef6c945da2a1`, sólo B-01/AB-02 y referencias dependientes:
catálogo6 incorpora RESOURCE real explícito/capturado, native URL/user locales,
getSchema/getCatalog prohibidos; extracción keys primero y errores proyectados
pendientes hasta ASSIGNMENTS/K exacto, frente a hard operational immediate abort.
Entrega `R2_DESIGN_AUTHORITY_MATERIALIZED`, no self-approval/cierre competente;
complete/released/transcript captured. Diseño §§3/8/9 se mantuvo byte-exacto.

Reauditor fresh independiente distinto `task_9a5c9b6b0b55 / ctx_4d4dfda77e33`
revisitó las doce preguntas, todos los sources/contratos y ambos P1; Done
`msg_5fe1bdca614f` a2026-09-16T22:28:11Z:
**PASS — F2E R2 DESIGN AUTHORITY COMPLETE FOR HANDOFF**.
GAP A/B/C COMPLETE, B-01/AB-02 CLOSED a nivel diseño; P0=0/P1=0/P2=0.
Informe original íntegro SHA
`bf3057c4f5d085e049f76ab901289c58bf694d9048b46c31c90fa99330acb27b`,
44186bytes/254LF; persistido exacto en review, con hash y byte equality verificados.
Todos los workers completados y liberados, transcripts captured, ACK de Done después
de release. El auditor hizo cero mutaciones de repo; el coordinador persiste su
receipt y este estado terminal autorizado, no una autoauditoría de su propio diseño.

### Instancia de los tres gaps y límites finales

| Gap | Contrato auditado completo | Disposición |
| --- | --- | --- |
| A | Context R2 propio10 fields/trust/registry; fecha+salones, triple PK/gap URNs/markers/K; facts versus structural/unknown intent; LP/SEQ/UTF8/maps26+20/D13/source-only immutable LegacyTurnReadSet; rechazo exacto pre-core | COMPLETE AT DESIGN LEVEL |
| B | Catálogo6: MEMBERS/ASSIGNMENTS +I/R/RESOURCE/S; tipos/aliases/binding/capture independiente de ejecución; owner TEST-only RR/readOnly y reader MANDATORY; native same-resource/allstatements/initialfinal snapshot/completion; SELECT-only3tables/all-column compoundPK scopedchecksum y ventanas no-write/concurrencia separadas | COMPLETE AT DESIGN LEVEL |
| C | Única excepción futura explícita sólo a ReservaJpaReaderArchitectureTest, R1literalmain11/test10 y todos sus guards preservados, R2SEALED separate sets/global union sin extra/missing/overlap; aprobación futura exacthandoff obligatoria | COMPLETE AT DESIGN LEVEL |

Las siete capacidades D24.1 son exactas: container/Flyway REUSE_AS_IS sólo capacidad
aislada; SELECT-role/SQLinspection/checksum/architecture EXTEND_WITH_R2_NEUTRAL_CAPABILITY
con preservación R1; owner RC/Reserva actual NOT_REUSABLE, nuevo owner RR R2 separado.
No shared source cambia ahora. No filenames de implementación/allowlist/commands/counts
futuros se inventan. Golden vectors, pre-callback fingerprint commitments, exact Maven
commands/counts y criterios R1 59/649/host7/native4 son NOT_YET_NORMATIVE para R2.
Tests/Maven/JDBC/container/SQL/host execution no aplican a esta unidad documental:
NOT_EXECUTED, nunca green test totals ni implementación aceptada.

R2 standalone lee únicamente turno_instructor, turno_instructor_usuario y
turno_instructor_asignacion mediante dos projections escalares, no Cartesian join:
active recurrent weekday domingo0 y puntual exact-date, LEFT JOIN conserva cero
miembros, todas las assignments inclnonmembers por parent UUIDs; sólo cero MEMBERS
rows real es éxito-empty con query2 omitida. Puntual UNKNOWN_INTENT/no inventedhistory;
bad required/type/duplicates/orphans causan rechazo total, cero publicación parcial.
No nominal/effective generation/targets/classifier/intent inference/productive consumer.

### Gate competente y única siguiente unidad

GateTask real `task_348d69549331`; gate nuevo `gate_3a050e22dc0f`,
creado pending2026-09-16T22:29:59Z **después** del re-audit terminal.
Resolved PASS2026-09-16T22:30:03Z; binding diseño/research/review/informe finales y
baseline6c2 inmutables; scope/integrity verificados:
**PASS — F2E R2 DESIGN AUTHORITY COMPLETE / READY_FOR_R2_DESIGN_AUTHORITY_PUBLICATION**.

Selección competente específica del profile R2, **no requisito universal**:
`R2_DESIGN_AUTHORITY_PUBLICATION` separada antes de futuro implementation handoff ACTIVE.
La autoridad necesita publicación/versionado/sello y closure con sus propios preflight,
scope exacto, auditor fresh y gates. Local materialization/audit/gate no es PUBLISHED.
Suficiencia para AUTHORING es de diseño y se puede evaluar sólo bajo autorización
documental nueva, no equivale a handoff APPROVED/ACTIVE o permiso de implementación.
En esta unidad **no** se creó implementation handoff ni se activó R2.

`ACTIVE HANDOFF: NINGUNO`; research R2 `NOT_ACTIVE_IMPLEMENTATION_HANDOFF`.
`NEXT AUTHORIZED LIFECYCLE: R2_DESIGN_AUTHORITY_PUBLICATION` solamente.
`NEXT FUNCTIONAL PHASE / R2 IMPLEMENTATION: NOT_AUTHORIZED`.
R2 design `MATERIALIZED / AUDITED / COMPLETE / NOT_PUBLISHED`;
R2 implementation `NOT_AUTHORIZED / NOT_STARTED`, no aceptación ni publicación R2.
No stage/commit/push/fetch ni publicación automática en este Run.

TurnoInstructor `LEGACY_VIVO / PRODUCTIVO`; R1 `CLOSED / ACCEPTED / PUBLISHED /
DARK_LAUNCH_PRESERVED`; R2 previsto `DARK_LAUNCH / NON_PRODUCTIVE`, default/product
contexts ABSENT y zero productive callers como obligaciones futuras, no activation.
Cutover `NOT_AUTHORIZED`; R3 nominal/candidates, R4 exact-date NEW_* adjustments,
R5 F2D graph/collector/backing y R6 cross-source RR/shadow `NOT_AUTHORIZED_IN_R2`.
Target selection/crosswalk/resolver/fence/cross-source composition/materialdataaudit/
reportsink/migration/productive shadow activation excluidos. Payments/Notifications
OUT_OF_SCOPE; Autopilot/FeelingPilatesOrchestrator/HostValidator OLD_PROCESS_ONLY.
P2-EVIDENCE-01 `NON_BLOCKING / PRESERVED`, sin reparar/fabricar arrays/XML/originales.

Scope terminal permitido: este único tracked document modificado y tres nuevos docs
R2 allowlisted (design/research/review); implementación/tests/config/SQL/migraciones/
pom/core/legacyR1 delta0. Staging EMPTY; HEAD/upstream/live y R1 exact21 unchanged.
El review y este append son delta coordinador autorizado posterior al audit, no
delta atribuible al auditor ni bytes futuros auditados por él. La sección R1 anterior
se conserva como prefijo byte-exacto. Git diff --check y cached check exigidos PASS.

## F2E R2 — cierre de publicación de autoridad de diseño

Corte de reconciliación 2026-09-17 (America/Mexico_City), Run `run_9e2fc3f4c74f`.
Este bloque es la vista vigente R2. La sección anterior y los headers de los
artefactos sellados son cortes históricos de materialización/corrección anteriores
a publicar; sus NOT_PUBLISHED y siguientes acciones no son el estado presente.
Se preservan completos: preflight, FAIL inicial con dos P1, corrección, re-audit
PASS y gate de diseño; no se transforman retrospectivamente en first-pass PASS.

Publicación comprobada: `run_ea24bf9a335a`, commit
`061dda98319722bcc2c601e707c25d7433ac44c1`, único parent
`6c2eacc870499e74ead74c1851630f9f53b1c676`, branch
`operacion/excepciones-horario-fecha`. Exactamente cuatro documentos (3 NEW,
1 MODIFIED), manifest TSV UTF-8 sorted/LF/final LF SHA-256
`f367f8d657b2f26237a98f22baea8f1dde54841685c4134f7b05683038d48fe2`.
HEAD/upstream/origin live iguales a ese commit, ahead/behind0/0, entrada CLEAN,
index EMPTY/untracked0; fresh ls-remote y blobs/parent/manifest verificados.

Preflight `run_b04f6e0ac339 / gate_4954c971c1e7 PASS`; staged audit independiente
`task_152bef6a1caf / ctx_0c0a4fe8d250`:
`PASS — F2E R2 DESIGN AUTHORITY STAGED SNAPSHOT VERIFIED`.
Authorization `gate_40f8471dfebd`:
`PASS — AUTHORIZED_TO_COMMIT_AND_PUSH_EXACT_F2E_R2_DESIGN_AUTHORITY`.
Un commit y un push normal non-force fast-forward, sólo exact branch, sin tags ni
otros refs. Post-publication auditor separado
`task_de971a00b41d / ctx_4685c46279e2`:
`PASS — F2E R2 DESIGN AUTHORITY PUBLISHED EXACTLY`.
Completion `task_d08dce894db1 / gate_e140218d660b`, resolved/PASS:
`PASS — F2E R2 DESIGN AUTHORITY PUBLISHED / READY_FOR_DESIGN_PUBLICATION_CLOSURE`.
El gate real fue contrastado read-only; no se sustituye por una afirmación del chat.

Identidades preclosure históricas: ESTADO
`aad8f9436dfc14f9ce45c645aa002e7692f91766483f48cbd82f8d93ea8d0d26`, review
`0863b2a704f56b9d55a141edb3d191629c25d35f8706a66b7c878e176564b785`.
Diseño normativo publicado permanece inmutable SHA-256
`db4673dd0705c41e62d0b77339d45b2cd1e87c51e26064112ad95b843dafe9cf`;
research provenance publicado permanece inmutable SHA-256
`221347b46c5032b384a306c61908c8fd0f2c26076455609fb880098be8f0d2d4`.
El research handoff sigue DESIGN/RESEARCH, nunca implementación ni ACTIVE.
El [review existente](reviews/F2E-R2-REVIEW-DISENO-LECTOR-TURNO-LEGACY.md)
conserva los informes originales íntegros y añade el receipt de publicación/cierre.

```text
R1: CLOSED / ACCEPTED / PUBLISHED
R2 design authority: COMPLETE / AUDITED / PUBLISHED
R2 design-publication lifecycle: CLOSED — AUTHORITY_RECONCILIATION_MATERIALIZED
CURRENT CLOSURE AUDIT / AUTHORIZATION GATE: PENDING / NOT_EXECUTED
CURRENT CLOSURE-DOC PUBLICATION / POST-CLOSURE AUDIT / FINAL GATE: PENDING / NOT_EXECUTED
R2 DESIGN/RESEARCH handoff: PUBLISHED_PROVENANCE_ONLY / NOT_IMPLEMENTATION_AUTHORITY
R2 implementation handoff: HISTORICAL_ARTIFACT_PUBLISHED (commit 6140978) / NOT_ACTIVE / ALLOWLIST_RECONCILIATION_REQUIRED
ACTIVE IMPLEMENTATION HANDOFF: NINGUNO
R2 implementation: NOT_AUTHORIZED / NOT_STARTED
R2 designed runtime: DARK_LAUNCH / NON_PRODUCTIVE; NO_ACTIVATION
TurnoInstructor: LEGACY_VIVO / PRODUCTIVO
Dark launch: PRESERVED
Cutover: NOT_AUTHORIZED
R3 / R4 / R5 / R6: NOT_AUTHORIZED_IN_R2
Payments / Notifications: OUT_OF_SCOPE
P2-EVIDENCE-01: NON_BLOCKING / PRESERVED
Autopilot / FeelingPilatesOrchestrator / HostValidator: OLD_PROCESS_ONLY
```

CLOSED arriba reconcilia autoridad completa/auditada y publicación ya probadas;
no falsifica el futuro audit, commit, push o gate de estos bytes de cierre.
El gate terminal de cierre sólo podrá pasar tras ambas auditorías independientes
y publicación controlada. Profile actual: DOCUMENTATION_ONLY, dos rutas
ESTADO/review; implementación/tests/host/build/JDBC/SQL NOT_APPLICABLE/NOT_EXECUTED.
Scope/safety y auditorías/gates/publicación documental sí aplican, sin autoaprobación.

### Próximo lifecycle separado, no implementación por continuidad

Diseño R2 §11, research handoff §4 y WORKFLOW/STATE-MACHINE/GATES/ROLES permiten
evaluar `R2_IMPLEMENTATION_HANDOFF_MATERIALIZATION` con autorización documental
propia. Publication y closure competentes son requisitos específicos del profile
antes de futuro handoff ACTIVE; éste todavía necesitará su propio audit,
aprobación/activación y autorización implementativa separada. No existe ahora.

Selección deliberada del usuario para el siguiente proceso, condicionada al gate
final de este cierre: `F2E_OPTIMIZED_EXECUTION_BOOTSTRAP_R1`.
Es PROCESS_ONLY, no prerrequisito normativo nuevo de producto, no continuación de
R1 técnico ni permiso implícito R2. Sólo se selecciona su apertura futura; aquí no
se materializa, publica, activa ni diseña el bootstrap. Su scope/profile, auditoría
y publicación/activación de proceso necesitan su lifecycle y autoridad propios.
Secuencia seleccionada: cierre R2 design publication → bootstrap process-only →
process publication/activation competente → materialización de handoff R2 bajo
autorización propia → implementación únicamente tras autorización propia.
Un checkpoint clean HEAD/upstream/live al terminar será apto para empezar ese
bootstrap; no se presume su ejecución ni se transmite autoridad para código.

### Coordinación cross-lane preservada, sin integración

Payments Slice2 fue reportado CLOSED/ACCEPTED/PUBLISHED por coordinación,
branch aislada `pagos/pagos-notificaciones-r1`, HEAD conocido
`8a912217adf6ea2d7d56e3e818845c3338db250e`, Run `run_6859a7f36296`.
Clasificación `SHARED_BUT_COMPATIBLE + INTEGRATION_POINTS_IDENTIFIED`;
es evidencia de coordinación, no una nueva auditoría Payments en este cierre.
V48/V49 y V49/52 pertenecen a esa lane; no se integra ni reconcilia Flyway aquí.
Futuro handoff/implementación F2E revalidará el head Flyway integrado vigente:
V47/50 no se asume globalmente terminal. Cambios compartidos Reserva/Programación/
capacity/cupos requieren `CROSS_LANE_DEPENDENCY_REQUIRED` antes de escribir.
No se transfiere ownership: F2E observa Reserva; Reservations posee write/state/
capacity; Payments posee semántica financiera; Notifications delivery infrastructure.
TurnoInstructor sigue productivo. Cero integración, cutover o activación.

## F2E — activación del proceso optimizado y cierre de publicación R1

Corte documental de activación: Run `run_4ba4573df505`, 2026-09-18 America/Mexico_City.
Profile `F2E_OPTIMIZED_EXECUTION_PROCESS_ACTIVATION_PUBLICATION_CLOSURE_R1`:
PROCESS_ONLY / DOCUMENTATION_ONLY / STRICT_MINIMUM_CLOSURE_SCOPE.
Este bloque es autoridad de lifecycle **de proceso**, subordinada a diseño/producto;
no altera producto, dominio, implementación, R1, diseño R2, APIs, DB, migraciones,
configuración ni runtime. Los bloques anteriores y sus FAIL/PENDING son historia.

### Publicación previa reconciliada y provenance

Bootstrap `run_748f43974201 / gate_1f3b69eceac0`, resolved/PASS:
`PASS — F2E OPTIMIZED EXECUTION BOOTSTRAP ACCEPTED / READY_FOR_CONTROLLED_PROCESS_PUBLICATION`.
Publicación `run_ef219776d9be`, commit
`e5ba4c2b8b5885703cb18da5fbf0d7694d0b1646`, único parent
`ab9705db6463ebef4576aff4f6559808cc0fdb9f`.
Exact3 NEW documentos RUNBOOK/STATE/EXECUTION POLICY; manifest UTF-8 TSV
path/status/sha256/classification, sorted/LF/final LF SHA-256
`094f6234f1e5f67884c19a2e7ca2276f440bfce0acd0ad1e402200c8d958a46c`.
Identidades publicadas iniciales: RUNBOOK
`a7007c34ad999bdbe0e31f1d50cd04ec3b142ea5d6523a3debce310106d795b2`;
STATE `61e4fb6a966355b02bfa545ec8a784d3979c394325e9b5d9fee77377d3354f5d`;
POLICY `84ec1c761d92e98cecd092eeb2aa368be28bd380b56b1c4b20a084254c86e44e`.
Esos blobs se conservan como historia en el commit anterior; POLICY permanece
byte-for-byte inmutable en este cierre. RUNBOOK cambia sólo extensión STATE V2 y
navegación de corte; STATE se reconcilia bajo allowlist propio.

Staged audit `task_aca9cdb17008 / ctx_8a0d0c5681c3`:
`PASS — F2E OPTIMIZED PROCESS STAGED SNAPSHOT VERIFIED`, P0=0/P1=0/NEW P2=0.
Gate `task_f67905604121 / gate_fe66af787469`, resolved/PASS:
`PASS — AUTHORIZED_TO_COMMIT_AND_PUSH_EXACT_F2E_OPTIMIZED_PROCESS_SNAPSHOT`.
Un commit y push normal non-force, sólo branch autorizada, cero tags/otros refs.
Post-publication audit nuevo `task_7056d4705490 / ctx_0575c05bdd05`:
`PASS — F2E OPTIMIZED EXECUTION PROCESS PUBLISHED EXACTLY`, P0=0/P1=0/NEW P2=0.
Ambos settled/released, transcript captured y Done ACK después de release.
Completion `task_4356c8476e8b / gate_84206560034d`, resolved/PASS:
`PASS — F2E OPTIMIZED EXECUTION PROCESS PUBLISHED / READY_FOR_PROCESS_ACTIVATION_CLOSURE`.
Fresh preflight de este cierre: HEAD/upstream/live iguales al commit publicado,
0/0, CLEAN/index EMPTY/untracked0, parent/blobs/manifest/checks PASS y gates reales
consultados no-mutante. Evidencia local durable:
`/Users/jesusaldaircruzortiz/.codex/feelingpilates-evidence/run_4ba4573df505/`.
Historia original en directorios hermanos run_748f43974201 y run_ef219776d9be;
retención local, sin promesa de backup externo. No /tmp como autoridad durable.

### Activación operacional y límites de efecto

Semántica autorizada únicamente:
`F2E OPTIMIZED EXECUTION PROCESS ACTIVE FOR FUTURE AUTHORIZED F2E LIFECYCLES`.
No reasigna tasks existentes ni crea autoridad de implementación/producto.
Autoridad normativa de producto/diseño → implementation handoff activo → audits
competentes → Decision Gates resueltos → ESTADO → hechos Git → STATE derivado.
RUNBOOK/POLICY gobiernan HOW, nunca WHAT. Contradicción falla cerrado.

En el corte de materialización: bootstrap ACCEPTED, proceso PUBLISHED, activación
NOT_ACTIVE / ACTIVATION_DOCUMENTED_PENDING_AUDIT_PUBLICATION, cierre PENDING.
Este texto es la declaración **condicionada** de activación. Efecto ACTIVE exige
primer audit independiente terminal PASS, gate de autorización de publicación PASS
y commit de cierre publicado exactamente con parent checkpoint anterior. Ninguna
optimización se usa para dispensar el rigor del presente cierre.
Uso futuro y cierre CLOSED requieren además auditor post-activación NUEVO terminal
PASS y gate final resolved/PASS del Run run_4ba4573df505 con semantic exacto:
`PASS — F2E OPTIMIZED EXECUTION PROCESS ACTIVE / READY_FOR_R2_IMPLEMENTATION_HANDOFF_MATERIALIZATION`.
Manifest/hashes/commit/gates finales se bindan en receipts externos reales; IDs y
SHA todavía no existentes no se inventan dentro de este commit. Antes de usarlos,
reconstruir por RUNBOOK §10.1: Git/ls-remote + hashes + audits/gates reales. Si falta
binding/evidencia o hay drift, stop. No modificar bytes después del audit/push.

| Clase tras activación verificada | Prácticas / estado |
| --- | --- |
| ACTIVE_IMMEDIATELY → ACTIVE | Tool-first/delta-first; contrato compacto EXTENDS AgentResult; RAW/SUMMARY; debugging sistemático falsable; fan-out adaptativo; no silent fallback; precedencia; fail-closed Human Gates; métricas ligeras. |
| PILOT_FIRST → ACTIVE_AS_PILOTS | Luna mechanical/deterministic validation; Terra bounded technical/documentation/provenance; L0–L4 sin ocultar autoridad; FAST/GATE; presupuestos correctivos; STATE packaging. Sólo tasks futuros autorizados y medición independiente; nunca calidad/defaults por nombre. |
| DEFER → INACTIVE | Luna backend high-risk audit; Terra high-risk acceptance; automatic publication/productive activation/cutover; aggressive RAW deletion; automatic schema/migration reconciliation; unsupported tooling. Silent fallback sigue FORBIDDEN. |

Routing intacto: Luna PILOT_FIRST, Terra PILOT_FIRST, Sol ESCALATION_ONLY;
PROVEN_DEFAULT NONE. Capabilities/catalog/matriz originales no cambiados. Terra
pilots0, métricas NOT_COLLECTED; Luna pilots0. Sol para riesgo/ambigüedad crítica,
JPA/TX/snapshot/JDBC/concurrency/ownership y audit high-risk competente/fresh.
FAST nunca acepta; GATE conserva pruebas competentes frescas PG/Testcontainers,
Flyway actual compatible, JPA owner/isolation/readOnly/native physical JDBC,
RR multi-statement snapshot, catálogo/capture/binds, SELECT-only/denied writes,
checksum/no-write, concurrency/architecture/absence, host real cuando requerido,
auditor NUEVO independiente y Decision Gate. No rebaja ninguna autoridad técnica.
Corrección/fan-out/Human Gates permanecen policy §§5–7 exactos. Tests/backend/host
execution de este cierre exclusivamente documental NOT_APPLICABLE/NOT_EXECUTED.

### STATE, métricas e incidentes preservados

STATE V2 continúa DERIVED / NON_PRODUCT_AUTHORITY / OPERATIONAL_CACHE. Selectores
HEAD/upstream/live/gate se resuelven de herramientas y receipts con binding estricto;
ningún SHA autorreferencial, gate futuro ficticio ni cache superior al producto.
Actualizar versión de contrato operacional requiere esta autorización documental y
fresh audits; no es un cambio de schema de base de datos. Snapshot V1 queda histórico.
Métricas FUTURE_OPTIMIZED_EXECUTION_ONLY tienen frontera en gate final resolved_at:
contadores0 y arrays de pilotos vacíos, sin importar los workers de bootstrap/cierre
como pilotos. Wallclock0 inicia medición futura; RAW/SUMMARY bytes0 miden sólo evidencia
futura. Historia previa se referencia aparte; no inventar tokens/costo/cuota/éxitos.

Incidentes de publicación preservados: nueve archivos RAW/evidencia temporalmente
en repo root, movidos fuera antes de staging, sin drift residual; dos starts
bloqueados por update notice, released y same-task retry después de skip, sin package
update. Receipts originales OPERATIONAL-RECOVERY y AUDITOR-STARTUP-RECOVERY e informes
independientes permanecen en run_ef219776d9be. RUNBOOK §§1/7 ya exige snapshots/reportes
externos bajo evidencia designada; no se añade cleanup ni cambio de policy. La raíz
externa absoluta se valida antes de escribir RAW. Errores operacionales recuperables
se registran conforme ORQ, no se ocultan ni se convierten en ciclos técnicos.
Known process P2 F2E-PROCESS-P2-01 NON_BLOCKING/PRESERVED: Cursor table/routing clarity;
route cursor/claude-opus-4-8 INELIGIBLE en todas las celdas de matriz restrictiva.
No reparación ni promoción silenciosa. Historical P2-EVIDENCE-01 preservado.

### Producto/cross-lane y siguiente lifecycle exacto

R1 CLOSED/ACCEPTED/PUBLISHED; R2 design COMPLETE/AUDITED/PUBLISHED/CLOSED;
R2 implementation handoff HISTORICAL_ARTIFACT_PUBLISHED (commit 6140978) / NOT_ACTIVE / ALLOWLIST_RECONCILIATION_REQUIRED; ACTIVE IMPLEMENTATION HANDOFF
NINGUNO; R2 implementation NOT_AUTHORIZED/NOT_STARTED. TurnoInstructor
LEGACY_VIVO/PRODUCTIVO; dark launch PRESERVED; cutover NOT_AUTHORIZED;
R3–R6 NOT_AUTHORIZED_IN_R2. Ninguna activación de runtime/producto aquí.
Payments/Notifications separado; coordinación SHARED_BUT_COMPATIBLE +
INTEGRATION_POINTS_IDENTIFIED, Payments Slice2 CLOSED/PUBLISHED V49/52 conocido por
coordinación. Ownership F2E observa Reserva/snapshot y Programación/Turno autorizado;
Reservations write/state/capacity, Payments financial rights, Notifications delivery.
CROSS_LANE_DEPENDENCY_REQUIRED antes de escribir superficies compartidas Reserva/
Programación/Turno/capacity/cupos. Validación integrada futura determina Flyway actual
compatible con migraciones F2E; V47/50 histórico nunca globalmente terminal. Cero
integración Payments, SQL, migración o reconciliación automática aquí.

Diseño R2 §11, research handoff §4 y el cierre R2 anterior sustentan el único siguiente
lifecycle, condicionado al gate final de activación:
`R2_IMPLEMENTATION_HANDOFF_MATERIALIZATION`.
Es readiness de materialización **documental** bajo su propio scope/preflight; NO
handoff creado/activo aquí y NO implementación. Secuencia: cierre de activación de
proceso → materialización de implementation handoff R2 → audit/gate/publicación/
activación competente del handoff → implementación sólo después de autoridad propia.
No abrir workers de ese siguiente lifecycle en este Run, ni inferir R3–R6/cutover.

## Reconciliación operacional Clean-Main: Corrección 2 (Auditabilidad y Estado R2)

Fecha: 2026-09-20.

### 1. Resolución P0 — Auditabilidad de fuentes históricas mediante objetos Git locales
El repositorio de reconciliación comparte el almacén de objetos Git con el historial original. El commit histórico `6140978bfd7b723fbbf9ddde1b5b5ba4f777c43c` está localmente disponible de forma inmutable (`git cat-file -e 6140978bfd7b723fbbf9ddde1b5b5ba4f777c43c^{commit}`).
Cualquier auditoría independiente en entorno sandboxed debe verificar la integridad y bytes de las fuentes históricas mediante inspección directa de objetos Git (`git show 6140978...:<path>`, `git cat-file`, `git ls-tree`, `git diff`) dentro del sandbox local, sin requerir acceso al sistema de archivos del worktree externo.

### 2. Resolución P1-1 — Distinción de ejes del handoff de implementación R2
Se distinguen estrictamente los cuatro ejes de estado del handoff R2 para eliminar contradicciones entre artefactos:
- **Marcador interno del documento**: El texto del archivo [`HANDOFF-F2E-R2-IMPLEMENTACION-LECTOR-TURNO-LEGACY.md`](handoffs/HANDOFF-F2E-R2-IMPLEMENTACION-LECTOR-TURNO-LEGACY.md) conserva su encabezado histórico inmutable: `MATERIALIZED_CANDIDATE / PENDING_FRESH_INDEPENDENT_HANDOFF_AUDIT / NOT_APPROVED / NOT_PUBLISHED / NOT_ACTIVE`.
- **Evento de publicación en repositorio**: Publicado formalmente mediante el commit histórico `6140978bfd7b723fbbf9ddde1b5b5ba4f777c43c` (`docs(auditoria): publica handoff de implementación F2E R2`).
- **Aprobación implementativa**: `NOT_AUTHORIZED`.
- **Activación operativa**: `NOT_ACTIVE`.
- **Implementación R2**: `NOT_IMPLEMENTED / NOT_AUTHORIZED` (0 archivos de código R2 en el repositorio).

### 3. Resolución P1-2 — Clasificación de dependencias de protocolo legacy y estado de allowlist R2
El handoff R2 cita cinco documentos de orquestación en su lista de dependencias `READ_ONLY_DEPENDENCY`:
- `auditoria/orquestacion/README.md`
- `auditoria/orquestacion/WORKFLOW.md`
- `auditoria/orquestacion/STATE-MACHINE.md`
- `auditoria/orquestacion/GATES.md`
- `auditoria/orquestacion/ROLES.md`

**Clasificación técnica**:
- Todos ellos corresponden al protocolo multiagente previo (ORQ-1 / FeelingPilatesOrchestrator), clasificados como `CURRENT_PROCESS_EQUIVALENT_EXISTS` (el proceso actual es Orca Product Delivery: RUNBOOK y POLICY) y `LEGACY_PROTOCOL_PROVENANCE_ONLY`.
- Ninguno de estos documentos constituye autoridad de dominio/producto, ni dependencia de compilación/implementación ni dependencia de tests.
- Por tanto, **NO** se trasplantan a la fundación clean-main. Permanecen accesibles como procedencia histórica vía objetos Git (`orquestacion/orq-1-protocolo` / `6140978bfd7b723fbbf9ddde1b5b5ba4f777c43c`).
- El estado de la allowlist del handoff R2 se clasifica formalmente como: `HANDOFF_ALLOWLIST_RECONCILIATION_REQUIRED`.
- **Separación de lifecycles**: La publicación e integración de la **FUNDACIÓN CLEAN-MAIN** (Detector Pure Core, ReferenciaOcurrencia, R1 Reserva JPA Reader, y autoridades F2E) es completamente independiente de la activación de R2. El handoff R2 permanece publicado como artefacto histórico, no activo, y con reconciliación de allowlist pendiente para el siguiente ciclo competente.

## Reconciliación Clean-Main: Handoff de Implementación R2

Fecha: 2026-09-20.

### 1. Resolución de Reconciliación de Allowlist y Sucesor Canónico
Se ha materializado el artefacto sucesor canónico del handoff histórico de implementación R2:
- **Artefacto sucesor**: [`auditoria/handoffs/HANDOFF-F2E-R2-IMPLEMENTACION-LECTOR-TURNO-LEGACY-CLEAN-MAIN-RECONCILIATION.md`](handoffs/HANDOFF-F2E-R2-IMPLEMENTACION-LECTOR-TURNO-LEGACY-CLEAN-MAIN-RECONCILIATION.md)
- **Handoff histórico**: [`auditoria/handoffs/HANDOFF-F2E-R2-IMPLEMENTACION-LECTOR-TURNO-LEGACY.md`](handoffs/HANDOFF-F2E-R2-IMPLEMENTACION-LECTOR-TURNO-LEGACY.md) (SHA-256: `7463798e80c898cc78d731012afc3b737f74c328a134d4da09d65ea79adad8e8`, publicado en commit `6140978bfd7b723fbbf9ddde1b5b5ba4f777c43c`) se preserva inmutable como procedencia histórica.
- **Base canónica**: `103ebe5ca25c0726559040f48b4668e9cffb0a4c` (`origin/main`).
- **Estado del sucesor**: `MATERIALIZED_CANDIDATE / PENDING_FRESH_INDEPENDENT_HANDOFF_AUDIT / NOT_APPROVED / NOT_ACTIVE`.
- **Implementación R2**: `NOT_AUTHORIZED / NOT_IMPLEMENTED` (cero archivos bajo `src/` modificados o creados).

### 2. Reclasificación Determinista del Árbol de Trabajo
Se reclasificaron las 65 rutas del handoff histórico frente al árbol de `origin/main`:
- **`CURRENT_R2_AUTHORIZED_NEW` (22 rutas)**: 12 de producción y 10 de prueba. Todas ausentes en el árbol actual (`hash=ABSENT`), autorizadas exclusivamente para su creación en un ciclo futuro de implementación tras activación formal. Path-set hash: `21a2300e72ad63e0b3d06f5fdb81ff4215952ee5bdbf6f234ad7fc55adfd09f3`.
- **`CURRENT_R2_AUTHORIZED_MODIFIED` (4 rutas)**: `F2eSelectOnlyRole.java`, `F2eStatementPolicyInspector.java`, `F2eSliceChecksum.java`, `ReservaJpaReaderArchitectureTest.java`. Las cuatro existen en `origin/main` y sus hashes de entrada coinciden byte a byte con los registrados en la autoridad histórica. Path-set hash: `0249c0508404ae27f855457440501175153c73701a28f88e804ca3466d0a5c6a`.
- **`WRITE_SCOPE` (`NEW` ∪ `MODIFIED`, 26 rutas)**: Path-set hash: `e32e6c04c5fec4f9c406ff27f57abec39b61f58ad77f75cf9e192007d4cb6028` (coincide exactamente con el hash del allowlist histórico).
- **`CURRENT_R2_READ_ONLY` (34 rutas)**: 7 de documentación y proceso activo, 10 de pure core / esquema / entidades legacy, 11 de R1 main y 6 de R1 test. Las 34 existen en el árbol actual de `main`. Path-set hash: `da34f1a22e865b5993386ec07f675a2c20c5785e0ee3922c31047b6abbfc4457`.
- **`CURRENT_R2_PROVENANCE_ONLY` (6 rutas)**: 5 documentos del protocolo multiagente legacy ORQ-1 (`README.md`, `WORKFLOW.md`, `STATE-MACHINE.md`, `GATES.md`, `ROLES.md`) ausentes del árbol de trabajo y reclasificados como procedencia histórica pura, más el handoff histórico. Path-set hash: `8557d930091c525532da43da098e6730179083403da6bd3d9239c18e0dfc8262`.
- **`TOTAL_ACTIVE_PATHS` (`WRITE_SCOPE` ∪ `READ_ONLY`, 60 rutas)**: Path-set hash: `0e429c517bd93ec94ade3ae6c92c33bd1d8e5f00153ab4fb7bdc331abd007ace`.

### 3. Preservación de Fronteras de Producto y Proceso
- `TurnoInstructor`: autoridad productiva viva preservada (`LEGACY_VIVO / PRODUCTIVO`).
- Dark launch: preservado (sin endpoints, sin beans productivos, sin wiring).
- Cutover: `NOT_AUTHORIZED`.
- Fases R3 a R6: `NOT_AUTHORIZED`.
- Migraciones Flyway: techo preservado en `V46`; `V47` ausente; cero modificaciones de base de datos.
- Pagos / Notificaciones: fuera de alcance.
- Siguiente lifecycle autorizado: Únicamente **Activación del Handoff R2** (`R2_HANDOFF_ACTIVATION`). No autoriza implementación.

## F2E R2 — cierre post-merge de implementación

Corte de cierre: 2026-09-28. Base canónica verificada: `c4ac3bc20f8797e0b96b23a544e6983be443cdf0` (PR #12, merge commit). La línea publicada comprende `707a514a76e599918dcd5fbd36f9516cff83177c` → `ac794da0c0b44b11a4333edf2113a4da05279cbb` → revisión independiente `a6bb5e06e6ebba49311595c033a43f8f71b474e0` → merge `c4ac3bc20f8797e0b96b23a544e6983be443cdf0`.

R2 design: **CLOSED**. Handoff de implementación R2: **RECONCILED / AUDITED / PUBLISHED / ACTIVE_FOR_COMPLETED_R2_IMPLEMENTATION_LIFECYCLE**; su actividad histórica no habilita trabajo posterior. Implementación R2: **IMPLEMENTED / LOCALLY_VALIDATED / INDEPENDENTLY_AUDITED / ACCEPTED / PUBLISHED / INTEGRATED / CLOSED**. El review publicado `auditoria/reviews/F2E-R2-IMPLEMENTACION-LECTOR-TURNO-LEGACY-CLEAN-MAIN-REVIEW.md` registra `APPROVED`, P0=0, P1=0, P2=0 y los seis hallazgos P1 previos cerrados. El handoff activo conserva SHA-256 `77995cb58e3c838024e567c48f44c8e10f9b7df9f26dbcf501dc7fbddb291a32`; el review conserva SHA-256 `dc47b8158a8e26dc95a8b09eacd669e5b81a588cc147fae78ca381a8fdef78d1`.

Recibo técnico post-merge: `./mvnw clean compile` PASS, 207 fuentes Java productivas; R2 35/35 PASS; R1 59/59 PASS; regresión completa 513/513 PASS; BUILD SUCCESS; git diff --check PASS. Código R2 permanece dark launch. TurnoInstructor **LEGACY_VIVO / PRODUCTIVO**; routing productivo R2 **ABSENT**; dark launch **PRESERVED**; cutover **NOT_AUTHORIZED**. Flyway V46; V47 **ABSENT**; AjusteProgramacionFecha **ABSENT**; Pagos / Notificaciones **OUT_OF_SCOPE**.

R3, R4, R5 y R6 permanecen **NOT_AUTHORIZED**. El cierre de R2 no autoriza R3. La única siguiente lifecycle es **F2E SUCCESSOR / R3 AUTHORITY PREFLIGHT**, modo **READ_ONLY / FRESH / NO_IMPLEMENTATION**, que determinará el alcance R3 desde autoridad canónica del repositorio. No se presume alcance ni comportamiento R3.

## F2E R3 — reconciliación de diseño nominal (2026-09-28)

Artefacto candidato: [`fase-2e-r3-diseno-reader-programacion-nominal-reconciliado.md`](fase-2e-r3-diseno-reader-programacion-nominal-reconciliado.md), reconciliado sobre `2422ee555f6e49e7edb27bfca9c71f66c2ef2c7a`.

- Diseño R3: candidato original `a26bfa5` auditado **BLOCKED** (P1=2); corrección.1 **CORRECTION1_CANDIDATE / PENDING_FRESH_INDEPENDENT_DESIGN_REAUDIT / NOT_APPROVED / NOT_PUBLISHED**.
- Decisión de esquema corregida tras auditoría independiente `BLOCKED` (P1=2): **`R3_CAN_FAIL_CLOSED_ON_V46_WITHOUT_SCHEMA_CHANGE`**. V46 no impide solapamientos activos de `programacion_asignacion`; R3 debe exponer todas las versiones aplicables y rechazar completamente series duplicadas en la fecha exacta. Migración R3: **NONE**; V47: **ABSENT / NOT_REQUIRED_FOR_R3**. La exclusión histórica V47 es sólo procedencia. Tabla/índices de ajustes: R4+ fuera de alcance.
- Decisión transaccional corregida: reader R3 `MANDATORY/readOnly` con manager explícito `f2eReaderTransactionManager` y owner individual test-only separado `REQUIRES_NEW/REPEATABLE_READ/readOnly`; sin invocación standalone sin owner. La composición multi-reader sigue **R6 NOT_AUTHORIZED** y requiere ese mismo manager, owner único/snapshot PostgreSQL compartido y autoridad de integración separada. La corrección no habilita implementación.
- Implementación R3: **NOT_AUTHORIZED**. R4-R6: **NOT_AUTHORIZED**.
- Flyway: V46. TurnoInstructor: **LEGACY_VIVO / PRODUCTIVO**. Dark launch preservado; cutover no autorizado.

Este estado es derivado/documental y no habilita implementación, migración, DB access, activación ni cambio de autoridad. El JSON de orquestación permanece como cache operacional derivado de un lifecycle/HEAD anterior y no se actualiza en este corte.

## F2E R3 — cierre post-merge del handoff de implementación

Corte de cierre: 2026-09-28. Base canónica previa verificada: `543c60af9ac74a65b2c8630d4cdb0ca8ba269307` (PR #16, merge commit). La rama `AldairCruz7/f2e-r3-handoff-process-closure` partió directamente de esa base; `origin/main` local y remoto coincidían antes de publicar.

Diseño R3: **CLOSED**. Handoff de implementación R3 [`HANDOFF-F2E-R3-IMPLEMENTACION-READER-PROGRAMACION-NOMINAL-CLEAN-MAIN.md`](handoffs/HANDOFF-F2E-R3-IMPLEMENTACION-READER-PROGRAMACION-NOMINAL-CLEAN-MAIN.md): **MATERIALIZED / RECONCILED / INDEPENDENTLY_AUDITED / APPROVED / PUBLISHED / INTEGRATED / NOT_ACTIVE**, SHA-256 `a74d139ca6c924dcbb98c368d80f38a17cf423408c7db30054e621b67b4cefa9`. El candidato `30379f343b81f653173e4cb78806aeb50e9b1317`, Correction.1 `fe5035a78b4e7bb6af558da6101354cf0686ce3a` y recibo independiente `c9f28f2dd1901a1af8a874e3ced89c66e9b5f02b` están integrados. PR #16 está merged en `543c60af9ac74a65b2c8630d4cdb0ca8ba269307`. El review independiente registra **APPROVED**, P0/P1/P2 `0/0/0`, `P1_1_JDBC_BINDING=CLOSED`, `P1_2_CHECKSUM=CLOSED` y revisión de Correction.1.

Implementación R3: **NOT_AUTHORIZED / NOT_IMPLEMENTED**; no existe handoff activo R3. R4–R6 **NOT_AUTHORIZED**. Siguiente lifecycle: **R3 HANDOFF ACTIVATION**; ese lifecycle futuro podrá evaluar `ACTIVE / AUTHORIZED_TO_START`. Este cierre no activa ni autoriza implementación.

Contrato preservado: Flyway V46; V47 **ABSENT / NOT_REQUIRED_FOR_R3**; migración R3 **NONE**; duplicados serie/fecha **READ_TIME_FAIL_CLOSED**; reader `f2eReaderTransactionManager / MANDATORY / readOnly=true`; owner individual `REQUIRES_NEW / REPEATABLE_READ / readOnly=true`; owner multi-reader R6 **NOT_AUTHORIZED**. `TurnoInstructor` permanece **LEGACY_VIVO / PRODUCTIVO**; autoridad productiva de Programación sin cambio; R3 **DARK_LAUNCH**; client API/web/reservations sin cambio; cutover **NOT_AUTHORIZED**; Pagos/Notificaciones **OUT_OF_SCOPE**.

La allowlist terminal conserva 18 NEW, 2 MODIFIED, WRITE_SCOPE 20, READ_ONLY 17, PROVENANCE_ONLY 4 y DEFAULT_DENY **ENFORCED**; los seis path-set hashes publicados se reprodujeron exactamente. Handoff y review permanecen byte por byte inmutables. Cierre documental/proceso únicamente; `src/` sin delta.
