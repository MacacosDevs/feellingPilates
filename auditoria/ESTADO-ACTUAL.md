# FeelingPilates — Estado actual de la reestructuración

Status: CANONICAL
Last updated: 2026-09-15
Repository verification: VERIFIED
Last verified against commit:
f23e91390d21ebd06040f3ed60f631e05d23d653
Verification scope: publicación, cierre y activación del handoff F2E R1 auditado; implementación autorizada y no iniciada, dark launch y autoridad productiva preservados

La referencia anterior identifica la base histórica de esta materialización documental. No sustituye el `HEAD` operativo, que debe obtenerse mediante pre-flight en cada intervención.

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
DOWNSTREAM R1 HANDOFF: ver unidad activa a continuación
```

La enmienda residual publicada cierra exclusivamente la autoridad de identidad/provenance V2 y la
topología JPA de transacción/recurso R1. Las marcas internas `NOT_SELF_APPROVED` y
`PENDING_FRESH_AUDIT` del checkpoint conservan el estado histórico de la candidate al momento de
ser escrita; el lifecycle posterior competente queda persistido por este canónico y por el review
fresh independiente. Esa publicación del diseño no aprobó ni activó por sí sola el handoff R1;
la auditoría fresh posterior y la transición competente se registran a continuación.

## Handoff activo — F2E R1 reserva reader JPA read-only

```text
F2E R1 residual design: PUBLISHED / CLOSED
R1 handoff physical content: CORRECTED_TO_FINAL_V2_DESIGN_AUTHORITY
ACTIVE HANDOFF: auditoria/handoffs/HANDOFF-F2E-R1-RESERVA-READER-JPA-READ-ONLY.md
AUDITED HANDOFF SHA-256: 3fd71faca4d4c049ad5cb37b52bc6fd512509cf5b696bdc5c28d28cb966af8ef
PUBLISHED DESIGN SHA-256: 6c72cba1f83fbc2bcf3b3219d8252d2410ec482e30ae85d30fd2eeb04e8883d8
FRESH_INDEPENDENT_HANDOFF_DOCUMENT_AUDIT: PASS
P0 / P1 / P2: 0 / 0 / 0
READY_TO_APPROVE_F2E_R1_HANDOFF: YES
HANDOFF AUDIT REVIEW: auditoria/reviews/HANDOFF-F2E-R1-RESERVA-READER-JPA-READ-ONLY-REVIEW.md
R1 handoff: APPROVED / PUBLISHED / ACTIVE
R1 implementation: AUTHORIZED / NOT_STARTED
IMPLEMENTATION AUTHORITY: EXACTLY THE AUDITED HANDOFF SHA ABOVE / F2E R1 ONLY
NEXT ALLOWED ACTION: EXECUTE_F2E_R1_IMPLEMENTATION
R2-R6: NOT_AUTHORIZED
```

Las marcas internas `NOT_APPROVED`, `NOT_ACTIVE` e `IMPLEMENTATION_NOT_AUTHORIZED` del handoff y
del review residual conservan el estado histórico de esos artefactos al materializarse. No se
reescriben después del audit. El review fresh de handoff y este canónico competente registran la
transición posterior a `APPROVED / PUBLISHED / ACTIVE`; no queda una autoridad operacional
contradictoria.

`ACTIVE` autoriza exclusivamente la implementación F2E R1 delimitada por el handoff exacto con
SHA-256 `3fd71faca4d4c049ad5cb37b52bc6fd512509cf5b696bdc5c28d28cb966af8ef`.
No declara implementación iniciada o completada, tests ejecutados, migración, data audit, cutover,
R2-R6 ni cambio de autoridad productiva.

## Autoridad y límites preservados

```text
TurnoInstructor: PRODUCTIVE AUTHORITY
Pure detector: DARK_LAUNCH / NOT_PRODUCTIVE
Adapters R1: IMPLEMENTATION AUTHORIZED / NOT_STARTED / NOT_PRODUCTIVE
Data source: DATA_SOURCE_NOT_AVAILABLE
Data audit: NOT_AUTHORIZED
D08: DEFERRED
Crosswalk / Resolver / Fence / Migration: NOT_AUTHORIZED
MIGRANDO: NO
NUEVA: NO
Cutover: false
R1 implementation: AUTHORIZED / NOT_STARTED / NOT_EXECUTED
Java / tests / test-only Spring topology: AUTHORIZED ONLY BY THE EXACT ACTIVE R1 HANDOFF
DB change / SQL migration / productive Spring configuration / data audit / cutover: NOT_AUTHORIZED
Payments / Notifications / Capacity / Mobile: OUT_OF_SCOPE
```

Human/business decision: `NOT_REQUIRED`.

Technical design authority: `CLOSED / PUBLISHED`.

Implementation authority: `AUTHORIZED / NOT_STARTED / BOUNDED BY EXACT ACTIVE R1 HANDOFF`.

## Carril activo paralelo — Payments & Notifications / PN-13

Este lifecycle pertenece exclusivamente al carril físicamente aislado de Payments &
Notifications. Coexiste con el lifecycle F2E activo descrito arriba: no lo cierra, supersede,
absorbe, modifica ni autoriza a integrarlo, y no cambia el estado de candidatos inéditos de sus
otros worktrees.

```text
LANE: PAYMENTS & NOTIFICATIONS
BRANCH: pagos/pagos-notificaciones-r1
WORKTREE: /Users/jesusaldaircruzortiz/Desktop/Feelingpilates/feelingpilates-payments-notifications
FROZEN BASELINE: a0ec85818b771d4ac924b427fa1e90244ea9fe8e

PN-12.4: APPROVED / COMPLETED / HISTORICAL
PN-13 HANDOFF: COMPLETED FOR AUTHORITY MATERIALIZATION / HISTORICAL / NOT_ACTIVE
PN-13 HANDOFF PATH: auditoria/handoffs/HANDOFF-PN13-MATERIALIZACION-AUTORIDAD-PAGOS-NOTIFICACIONES.md
PN-13 HANDOFF SHA-256: 601d285a23c87b131b4b4946d2f858ad918faea12e492d76f7e9da7acd073e22
ACTIVE HANDOFF: NINGUNO

INDEPENDENT AUDIT EVIDENCE: auditoria/reviews/HANDOFF-PN13-MATERIALIZACION-AUTORIDAD-PAGOS-NOTIFICACIONES-REVIEW.md
AUDIT EVIDENCE SHA-256: 26a0e67588f7a9e5bd13c79aa9006a833d63834cf3cf1a1a19467194e27df5f5
AUDIT RESULT: PASS
P0 / P1 / P2: 0 / 0 / 0

PN-13: MATERIALIZED / ACCEPTED / PUBLISHED / CLOSED
PN-13 WORKFLOW STATE: PUBLISHED
PN-13 ORIGINATING AUTHORITY PUBLICATION COMMIT (HISTORICAL SNAPSHOT): a292a86225766acba0bb3333039b2ac30a36d48b
PN-13 PUBLICATION PARENT: a0ec85818b771d4ac924b427fa1e90244ea9fe8e
PN-13 PUBLICATION UPSTREAM: origin/pagos/pagos-notificaciones-r1
PN-13 VERIFIED PUBLICATION SNAPSHOT: LOCAL = UPSTREAM = LIVE ORIGIN = a292a86225766acba0bb3333039b2ac30a36d48b
PN-13 PUBLICATION EVIDENCE: auditoria/reviews/PN13-REVIEW-PUBLICACION-AUTORIDAD-PAGOS-NOTIFICACIONES.md
PN-13 CHECKPOINT: auditoria/fase-pn13-materializacion-autoridad-pagos-notificaciones.md
PN-13.1 (HISTORICAL): FRESH INDEPENDENT AUTHORITY AUDIT / FAIL
PN-13.1 P0 / P1 / P2: 0 / 10 / 0
PN-13.1 REVIEW: auditoria/reviews/PN13-REVIEW-MATERIALIZACION-AUTORIDAD-PAGOS-NOTIFICACIONES.md
PN-13.1 REVIEW SHA-256: cda17b50e562059382f42c46ebe827fedf6519682f95259860be4d0a48215477
PN-13.1.1: FAILED AUTHORITY AUDIT EVIDENCE MATERIALIZATION / PASS / EVIDENCE_ONLY / NO_CORRECTION
PN-13 RESIDUAL AUTHORITY AUDIT (HISTORICAL): FAIL / P0=0 / P1=5 / P2=1
PN-13 R1.2 FRESH AUTHORITY AUDIT: PASS / P0=0 / P1=0 / P2=1
PN-13 R1.2 REVIEW: auditoria/reviews/PN13-R1.2-REVIEW-MATERIALIZACION-AUTORIDAD-PAGOS-NOTIFICACIONES.md
PN-13 R1.2 REVIEW SHA-256: da21981cbb4d0925fc7732e645580dd22e5e369938f6b165a0e7009d0eb4510b
PN-13 AUTHORITY CORRECTION: ACCEPTED
PN13-001..PN13-010: CLOSED
NEW-PN13-011..NEW-PN13-016: CLOSED
NEW-PN13-017: OPEN / P2 / EDITORIAL / NON_BLOCKING / IMPLEMENTATION_INDEPENDENT
PN-13 DOCUMENTATION GATE: PASS
PN-13 PUBLICATION GATE: PASS
PN-13 PUBLICATION CLOSURE GATE: PASS — task_8284a0151874 / gate_0284fb3efcc7 / RESOLVED PASS
PN-13 CLOSURE DOCUMENTATION: AUDITED / CLOSED / FRESH_INDEPENDENT_AUDIT_PASS / NOT_SELF_AUDITED
PN-13 CLOSURE STATUS: CLOSED
PN-13.2 (HISTORICAL): NOT_AUTHORIZED
PN-14: NOT_AUTHORIZED
IMPLEMENTATION: NOT_AUTHORIZED
NEXT ALLOWED ACTION: NONE / TERMINAL / NO_CONTINUATION_INFERRED

F2E LIFECYCLE: ACTIVE / INDEPENDENT / UNCHANGED
F2E WORKTREE AND UNPUBLISHED CANDIDATES: NOT_INSPECTED / NOT_MODIFIED
F2E INTEGRATION: NOT_AUTHORIZED
```

La aprobación y activación históricas se limitaron al hash exacto del handoff indicado. Las marcas internas
`NOT_APPROVED`, `NOT_ACTIVE` y `PN-13 NOT_AUTHORIZED_TO_START` conservan el estado histórico del
artefacto cuando fue materializado; el review independiente y esta transición canónica posterior
son la evidencia competente del nuevo lifecycle. Después de PN-13.1, PN-13 permaneció materializada
y no aceptada porque ese audit falló con diez hallazgos P1; PN-13.1.1 materializa la evidencia
histórica sin corregirla.
El re-audit fresh R1.2 posterior cerró PN13-001..PN13-010 y NEW-PN13-011..NEW-PN13-016, reportó
`AUTHORITY_AUDIT=PASS / P0=0 / P1=0 / P2=1` y habilitó esta aceptación. PN-14 e implementación
siguen no autorizadas; la publicación y el cierre documental tienen gates `PASS`, basados en el
audit fresh independiente y la resolución competente registrados abajo. No se concede
autoridad de runtime, migración, integración ni cutover.

La publicación autorizada del conjunto exacto de diez documentos quedó en el commit indicado,
verificado fresh e independientemente por `task_eb0d3f4ce26d / ctx_70bd2f984fb8 /
msg_37a535a7afdc` (`PUBLICATION_VERIFICATION=PASS`). El Run `run_4b8a88e13c97` conserva el publisher
retry `task_1dc519bf9a1a / ctx_597b658ec31d / msg_6573bc881c87` (`PUBLICATION_RESULT=PASS`) y el gate
de publicación `task_7ad876b226e3 / gate_66365f81645f` (`RESOLVED / PASS`). El nuevo review de
publicación persiste esa evidencia como `EVIDENCE_ONLY / NOT_SELF_AUTHORIZING /
NOT_IMPLEMENTATION_AUTHORITY`; no constituye un audit del DOCUMENTER ni aprueba su propio cierre.
El audit fresh independiente `task_75fb7e989fe1 / ctx_139dcf4b6e60 / msg_b15a9378279f` reportó
`PUBLICATION_CLOSURE_AUDIT=PASS / nuevos P0=0 / P1=0 / P2=0`. El gate-only
`task_8284a0151874 / gate_0284fb3efcc7` quedó `RESOLVED / PASS`, con evidencia
`msg_b15a9378279f, msg_37a535a7afdc` y `repositoryWrites=false`. Esta transición materializa
`PN-13 MATERIALIZED / ACCEPTED / PUBLISHED / CLOSED`, documentación de cierre `AUDITED / CLOSED`
y estado normativo terminal `PUBLISHED` conforme a `STATE-MACHINE.md`; siguiente acción
`NONE / TERMINAL`, sin continuidad inferida. La evidencia ajena y su candidate histórico auditado
quedan en `auditoria/reviews/PN13-REVIEW-CIERRE-PUBLICACION-AUTORIDAD-PAGOS-NOTIFICACIONES.md`
como `EVIDENCE_ONLY / NOT_SELF_AUTHORIZING / NOT_IMPLEMENTATION_AUTHORITY`; no es audit propio.
El review de publicación permanece inmutable como snapshot histórico auditado: sus marcas
`PENDING / NOT_CLOSED` describen la candidate anterior, no el lifecycle vigente.
El commit de autoridad originario y la igualdad local/upstream/live arriba son snapshots históricos.
El preflight físico de esta materialización final verificó HEAD `a292a86225766acba0bb3333039b2ac30a36d48b`
y staging vacío; no fija un HEAD final permanente. En el snapshot histórico de materialización
documental del 2026-09-16 (`worker_done msg_16f4142ad6ee`, `2026-09-16T15:46:13Z`), previo al
publisher separado, este delta todavía no estaba committed ni pushed. La publicación posterior
y su verificación física se demuestran por Git y por el resultado del publisher/verifier
competentes; esta evidencia no fija un HEAD permanente ni fabrica un SHA autorreferencial,
sin abrir una fase funcional ni inventar hash, igualdad remota o commit de cierre.

PN13-001: CLOSED — checkpoint §18.1; DA-015/016/017/019
PN13-002 / NEW-PN13-011: CLOSED / CLOSED — checkpoint §18.2; Dominio §§13.3–13.5; DA-009
PN13-003: CLOSED — checkpoint §18.3; Dominio §13.5; DA-016
PN13-004: CLOSED — checkpoint §18.4; Dominio §13.5; DA-017
PN13-005 / NEW-PN13-012: CLOSED / CLOSED — checkpoint §18.5; Dominio §§13.6–13.7; DA-014/018/019
PN13-006 / NEW-PN13-013: CLOSED / CLOSED — checkpoint §18.6; Dominio §13.7; DA-014/018
PN13-007: CLOSED — checkpoint §18.7; Dominio §13.6; DA-019
PN13-008 / NEW-PN13-014: CLOSED / CLOSED — checkpoint §18.8; Dominio §13.6; DA-015/017/019
PN13-009: CLOSED — checkpoint §18.9; DA-014/017
PN13-010 / NEW-PN13-015: CLOSED / CLOSED — checkpoint §18.10; Dominio §13.8; DA-020
NEW-PN13-016: CLOSED — checkpoint §§5.1/18.1; DA-015
NEW-PN13-017: OPEN / P2 / EDITORIAL / NON_BLOCKING / IMPLEMENTATION_INDEPENDENT — checkpoint §5.1 frente a §18.1 y DA-015

El review histórico PN-13.1 conserva `FAIL / P1=10` y el audit residual conserva
`FAIL / P1=5 / P2=1`; no se reescriben retroactivamente. El review R1.2 es evidencia
`EVIDENCE_ONLY / NOT_SELF_AUTHORIZING`, y esta transición canónica posterior materializa la
aceptación. NEW-PN13-017 permanece explícitamente abierto y no se corrige en esta tarea.

Allowlist documental histórica de materialización del DOCUMENTER PN-13 (no ejecutable como scope de cierre):

```text
auditoria/fase-pn13-materializacion-autoridad-pagos-notificaciones.md
auditoria/contexto/DOMINIO-FUNCIONAL.md
auditoria/DECISIONES-ARQUITECTONICAS.md
auditoria/ARQUITECTURA-ACTUAL.md
auditoria/contexto/MAPA-LEGACY-Y-MIGRACION.md
auditoria/ESTADO-ACTUAL.md
```

El DOCUMENTER PN-13 no puede escribir su propio review independiente. El review
`auditoria/reviews/PN13-REVIEW-MATERIALIZACION-AUTORIDAD-PAGOS-NOTIFICACIONES.md` existe como
evidencia del audit y permanece `EVIDENCE_ONLY / NOT_NORMATIVE_AUTHORITY`.

Allowlist exhaustiva de esta materialización final del cierre (`SINGLE_WRITER /
DOCUMENTATION_ONLY / EVIDENCE_BOUND`): editar únicamente `auditoria/ESTADO-ACTUAL.md` y
`auditoria/fase-pn13-materializacion-autoridad-pagos-notificaciones.md`; crear únicamente
`auditoria/reviews/PN13-REVIEW-CIERRE-PUBLICACION-AUTORIDAD-PAGOS-NOTIFICACIONES.md` como evidencia
de la auditoría independiente ya emitida y del gate real resuelto. El review de publicación con
SHA-256 `96378bcc74da86f656d5eaf0a08042e246f6ce6f87bee3e77873679228c17a1f` y los ocho restantes
documentos aceptados/publicados permanecen byte-identical; no se autoriza staging, commit ni push
en esta materialización. La allowlist del bloque previo que creó el review de publicación es histórica.

Workflow profile de la transición terminal de cierre:

```text
PN-12.4 SCOPE / DOCUMENTATION / LIFECYCLE GATE: APPLICABLE
PN-13 PREPARE / DOCUMENT / DOCUMENT AUDIT: APPLICABLE
PN-13 MATERIALIZATION: COMPLETED / NOT_SELF_AUDITED
PN-13 AUTHORITY CORRECTION: ACCEPTED AFTER FRESH INDEPENDENT R1.2 AUDIT
PN-13 SCOPE / DELTA ISOLATION: APPLICABLE / PASS
PN-13 DOCUMENTATION GATE: APPLICABLE / PASS
PN-13 PUBLICATION GATE: APPLICABLE / PASS
PN-13 PUBLICATION CLOSURE GATE: APPLICABLE / PASS — gate_0284fb3efcc7
PN-13 WORKFLOW STATE: PUBLISHED
PN-13 CLOSURE DOCUMENTATION: AUDITED / CLOSED / FRESH_INDEPENDENT_AUDIT_PASS / NOT_SELF_AUDITED
PN-13 CLOSURE STATUS: CLOSED
NEXT ALLOWED ACTION: NONE / TERMINAL / NO_CONTINUATION_INFERRED
IMPLEMENTATION GATE: NOT_APPLICABLE / NOT_AUTHORIZED
TESTS GATE: NOT_APPLICABLE
HOST VALIDATION: NOT_APPLICABLE
```

`NOT_APPLICABLE` no equivale a `PASS`. No se ejecutaron ni se declaran aprobados tests,
HostValidator o validaciones de runtime en esta transición documental. La autoridad productiva y
el `cutover` permanecen sin cambios. El audit fresh independiente PN-13.1 confirmó el aislamiento
de scope/delta como `CONFORMING` y documentó diez P1; el audit residual posterior mantuvo
`AUTHORITY_AUDIT=FAIL` con cinco P1 y un P2. La corrección residual documental fue autorizada y
aplicada; el re-audit fresh R1.2 posterior reportó `PASS / P0=0 / P1=0 / P2=1`, y la transición
competente materializa ahora la aceptación con `DOCUMENTATION GATE=PASS`. El único P2 es
NEW-PN13-017, editorial, no bloqueante e independiente de implementación. La publicación física
posterior fue verificada y el publication gate está en `PASS`; el audit fresh independiente del
cierre y el gate `gate_0284fb3efcc7 / RESOLVED / PASS` sustentan el cierre documental
`AUDITED / CLOSED` y el estado normativo terminal `PUBLISHED`. Los nuevos hallazgos de cierre son
`P0=0 / P1=0 / P2=0`; el total combinado aceptado sigue `P0=0 / P1=0 / P2=1` por NEW-PN13-017.
La siguiente acción vigente es `NONE / TERMINAL`; PN-14 e implementación permanecen `NOT_AUTHORIZED`.
En el snapshot histórico de materialización documental (`worker_done msg_16f4142ad6ee`,
`2026-09-16T15:46:13Z`), la publicación del delta final de cierre y su verificación fresh
correspondían a tareas separadas; sus resultados posteriores se demuestran por Git y por el
publisher/verifier competentes, sin fijar un HEAD permanente ni un SHA autorreferencial.
