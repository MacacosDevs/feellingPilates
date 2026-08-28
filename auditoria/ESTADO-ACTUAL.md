# FeelingPilates — Estado actual de la reestructuración

Status: CANONICAL
Last updated: 2026-08-27
Repository verification: VERIFIED
Last verified against commit:
a971f8b9d56ffe901a2c6a07c4e757628686ad85
Verification scope: base física para activar canónicamente el handoff F2E; dark launch y autoridad productiva preservados

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

### F2E — Preparación

**HANDOFF_APROBADO / ACTIVO / INICIO_DE_PREPARACION_AUTORIZADO**

El handoff `auditoria/handoffs/HANDOFF-F2E-PREPARACION.md` tiene autoridad persistida para iniciar
exclusivamente el diseño y la preparación F2E dentro de su scope. Su auditoría fresh e independiente
reportó `P0=0 / P1=0 / P2=0` y queda persistida en
`auditoria/reviews/HANDOFF-F2E-PREPARACION-REVIEW-DOCUMENTAL.md`.

Estado de la unidad:

```text
handoff: APPROVED / ACTIVE
target: F2E / preparación
checkpoint F2E: NO CREADO / PENDING
design/documentation gate F2E: PENDING
implementation: NOT_AUTHORIZED
migration: NOT_AUTHORIZED
cutover: false
authority: TurnoInstructor / LEGACY_VIVO / PRODUCTIVO
runtime: DARK_LAUNCH
productive: NOT_PRODUCTIVE
```

La aprobación del handoff no aprueba por anticipado el diseño F2E ni autoriza implementación,
migración productiva, cutover o cambio de autoridad.

## Próximo paso

**EJECUTAR F2E / PREPARACIÓN SEGÚN `HANDOFF-F2E-PREPARACION.md`.**

El primer trabajo autorizado es `pre-flight + inventario + checkpoint/diseño F2E`. Esta acción
es exclusivamente documental y preparatoria: no autoriza implementar, migrar, activar, ejecutar
cutover ni cambiar la autoridad productiva.

## Handoff activo

`auditoria/handoffs/HANDOFF-F2E-PREPARACION.md`

```text
Target: F2E / preparación
Status: APPROVED / ACTIVE
Authority: AUTHORIZED_FOR_F2E_PREPARATION
Next gate: FRESH_INDEPENDENT_DESIGN_DOCUMENT_AUDIT / PENDING
```

`auditoria/handoffs/HANDOFF-F2D2.md` queda preservado como handoff histórico, cerrado y superseded.

## Advertencias inmediatas

- `AjusteProgramacionFecha`, `InstructorLocks`, V47 y el resolver de programación efectiva están materializados y documentados únicamente como dark launch no productivo.
- No existe fence de cutover F2D implementado.
- No existe cutover F2D implementado.
- `TurnoInstructor` sigue siendo la autoridad productiva actual de programación.
- `BloqueProgramacion + Asignacion` permanece `IMPLEMENTADO_NO_PRODUCTIVO`.
- El cierre de F2D.2 no autorizó por sí mismo F2E; el handoff F2E aprobado y activo autoriza ahora
  sólo su preparación/diseño y preserva todos los límites anteriores.
