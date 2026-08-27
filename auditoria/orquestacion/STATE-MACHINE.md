# Máquina de estados normativa

Los estados normativos describen bloques posibles y qué evidencia o gate falta en cada uno; no toda intervención recorre todos los estados. El `WORKFLOW_PROFILE` autorizado determina los stages, gates, validaciones y roles aplicables. Un bloque no pertinente se marca `NOT_APPLICABLE` y se salta mediante una transición normativa explícita; eso no equivale a `PASS`. Un gate aplicable cuya etapa aún no ocurrió permanece `PENDING`. Para cada transición sólo se exigen en `PASS` los gates aplicables que deben haberse resuelto antes de ella; los gates de etapas futuras no bloquean esa transición ni pueden saltarse cuando llegue su etapa. El motor puede usar estados auxiliares para ejecutar una acción; no se convierten automáticamente en estados históricos de una fase. El estado real se determina por los canónicos y el pre-flight físico, no por un journal aislado.

| Estado normativo | Precondiciones y significado | Siguiente acción / rol | Salida | HUMAN_STOP / SECURITY_STOP |
| --- | --- | --- | --- | --- |
| `READY_FOR_EXECUTION` | Scope, fuentes y pre-flight verificados; intervención autorizada. | EXECUTE / EXECUTOR. | Materialización reportada. | Autoridad, scope o estado físico ambiguo. |
| `IMPLEMENTED_IN_REVIEW` | Hay materialización pendiente de aprobación. | AUDIT / AUDITOR fresh. | Implementación aprobada o corrección requerida. | P0, P1 no corregible o decisión faltante. |
| `CORRECTION_REQUIRED` | Audit detectó P1 material, corregible por un único artefacto autorizado. | CORRECT / CORRECTOR. | Corrección materializada. | P0, varias soluciones válidas, límite técnico o scope inseguro. |
| `HOST_VALIDATION_REQUIRED` | La corrección necesita evidencia de host/Docker que el sandbox LLM no puede producir. | VALIDATE / HOST_VALIDATOR. | PASS para re-audit; FAIL para nueva corrección. | `HOST_VALIDATION_BLOCKED` si no hay host/fallback seguro; `SECURITY_STOP` si cambia fuente. |
| `AUDITING_CORRECTION` | Una corrección y sus evidencias aplicables fueron materializadas. | RE-AUDIT / AUDITOR fresh. | Aprobación técnica o nueva corrección. | P0, P1 no corregible o decisión faltante. |
| `IMPLEMENTATION_APPROVED` | Auditoría independiente y gates técnicos aplicables en PASS. | DOCUMENT / DOCUMENTER. | Inicio de la documentación. | Pérdida de una precondición o evidencia incompatible. |
| `DOCUMENTING` | Se satisfizo la precondición documental definida por el profile. En un profile técnico incluye el implementation gate y demás gates técnicos exigibles antes de documentar. En uno documental/normativo puro incluye scope documental autorizado, autoridad normativa y controles de scope/seguridad; implementación, tests, evidencia técnica y HostValidator pueden ser `NOT_APPLICABLE`. | DOCUMENT / DOCUMENTER. | Documentación lista para auditoría. | Cambio fuera de allowlist, código modificado por DOCUMENTER o evidencia contradictoria. |
| `AUDITING_DOCUMENTATION` | Hay delta documental materializado. Se auditan autoridad, scope, coherencia normativa, independencia, seguridad y gates exigibles en esta etapa; la evidencia técnica sólo se contrasta cuando es aplicable al profile. | DOCUMENT AUDIT / DOCUMENT_AUDITOR fresh. | Lista para publicar o corrección documental. | P0, P1 ambiguo, decisión humana o pérdida de un gate aplicable ya resuelto. |
| `DOCUMENTATION_CORRECTION_REQUIRED` | P1 documental inequívoco y corregible; se preservan todos los gates aplicables ya resueltos. No exige ni inventa gates técnicos `NOT_APPLICABLE`, y conserva `PENDING` los gates de etapas futuras. | CORRECT DOCUMENTATION / DOCUMENTER. | Re-audit documental. | P0, P1 ambiguo, decisión humana, límite documental o pérdida de un control aplicable. |
| `READY_TO_PUBLISH` | Todos y sólo los gates aplicables exigibles antes de publicar están en `PASS`; ningún P0/P1, decisión humana o `SECURITY_STOP` pendiente; scope del workflow válido. Publication y publication closure gates permanecen `PENDING` si aplican. | PUBLISH / PUBLISHER, o parada segura si la política manual lo exige. | Publicación física verificada y publication gate en `PASS`. | Divergencia remota, scope de publicación inválido o publicación no autorizada. |
| `PUBLISHED_PENDING_CLOSURE_DOCUMENTATION` | La publicación y su gate están verificados en `PASS`; el publication closure gate sigue `PENDING` y falta reconciliación canónica. | DOCUMENT PUBLICATION CLOSURE / DOCUMENTER. | Cierre documental listo para auditoría; closure gate aún `PENDING`. | Evidencia de publicación inconsistente. |
| `AUDITING_PUBLICATION_CLOSURE` | La documentación de cierre fue materializada sin alterar gates ya aprobados; el publication closure gate se está resolviendo. | PUBLICATION CLOSURE AUDIT / DOCUMENT_AUDITOR fresh. | `PUBLISHED` y closure gate en `PASS`, o corrección de cierre. | P0, P1 ambiguo, decisión humana o mutación fuera de scope. |
| `PUBLICATION_CLOSURE_CORRECTION_REQUIRED` | Audit de cierre halló P1 documental inequívoco. | CORRECT PUBLICATION CLOSURE / DOCUMENTER. | Nuevo audit de cierre. | P0, ambigüedad, límite de cierre o mutación fuera de scope. |
| `PUBLISHED` | Publicación y cierre documental auditados; publication gate y publication closure gate en `PASS`. | Ninguna: terminal/no next action. | Sólo una nueva fase expresamente autorizada inicia otro workflow. | No se infiere continuidad. |
| `HUMAN_STOP` | Requiere decisión humana real o se alcanzó una condición de stop. | Ninguna hasta resolución explícita. | Reanudación autorizada con evidencia. | Es terminal para el motor hasta esa resolución. |

Un profile exclusivamente documental puede recorrer, si así fue autorizado, `DOCUMENTING → AUDITING_DOCUMENTATION → DOCUMENTATION_CORRECTION_REQUIRED → DOCUMENTING → AUDITING_DOCUMENTATION → READY_TO_PUBLISH` sin pasar por `IMPLEMENTATION_APPROVED`, `HOST_VALIDATION_REQUIRED` ni otros estados técnicos no aplicables. Eso no permite desactivar controles de autoridad, scope, touched paths, `HEAD`/staging cuando correspondan o seguridad asociados a su capacidad de escritura.

Lifecycle temporal ilustrativo para un profile con publicación; no impone una secuencia universal:

```text
READY_TO_PUBLISH:
documentation_gate = PASS
publication_gate = PENDING
publication_closure_gate = PENDING

después de publicar y verificar:
publication_gate = PASS
publication_closure_gate = PENDING

después de documentar y auditar el cierre:
publication_closure_gate = PASS
```

## Estados auxiliares del motor actual

El motor externo actualmente distingue, entre otros, `IMPLEMENTING`, `AUDITING_IMPLEMENTATION`, `PUBLISHING`, `HOST_VALIDATION_BLOCKED` y `BLOCKED`. Son transitorios operacionales que aclaran una acción en curso o una indisponibilidad; no sustituyen los estados normativos anteriores ni deben copiarse como cronología canónica de todas las fases.

## Fallos operacionales pre-semánticos

`PRE_SEMANTIC_OPERATIONAL_FAILURE` significa que el schema preflight, proceso, invocación u otra operación falló antes de que un `AgentResult` válido fuera consumido y pudiera afectar semánticamente esta máquina. Incluye tanto la ausencia de resultado válido como un resultado no consumido. En ese caso no se genera un gate semántico, no se inventan P0/P1/P2, no se consume una transición de negocio ni se incrementa ningún contador semántico de corrección. Tampoco se registra al agente como si hubiera aprobado o fallado el contenido. El lifecycle operacional, exit code, causa, diagnóstico y evidencia técnica sí pueden persistirse.

Si la recuperación es determinista, segura y está autorizada, el motor puede reintentar, corregir configuración o preflight, reconciliar metadata o reconstruir el estado desde Git físico, artefactos canónicos, manifests, historial causal, fingerprints y evidencia persistida. Debe preservar `HEAD`, staging, scope, fingerprints, autoridad e historia cuando corresponda, y registrar el recovery sin reescribir artefactos históricos. La metadata operacional stale no prevalece sobre esas fuentes físicas o canónicas cuando la reconciliación es inequívoca.

El fallo escala a `HUMAN_STOP` sólo si no hay recuperación determinista segura, falta autoridad, hay conflicto entre fuentes de autoridad, existe `SECURITY_STOP`, hace falta una decisión de arquitectura o producto, no existe fallback seguro para el entorno indispensable, se excede la política operacional de retries/ciclos o resolverlo exige una mutación no autorizada. Un schema incompatible, metadata stale reconciliable o una invocación transitoria sin resultado consumido no bastan por sí solos para `HUMAN_STOP`.

```text
process exit != 0
+ no AgentResult válido
+ state_consumed = false
!= P0 / P1 / HUMAN_STOP automático
```

Primero se clasifica la causa operacional: `PROCESS FAILURE != SEMANTIC GATE FAILURE`.

## Dimensiones que no son una sola transición

`design`, `materialization`, `technical approval`, `publication`, `runtime`, `productive authority` y `cutover` son ejes ortogonales. Por ejemplo, un dark launch puede estar materializado, técnicamente aprobado y publicado, pero seguir `NOT_PRODUCTIVE`, con `cutover=false` y la autoridad productiva sin cambios. Por tanto:

```text
PUBLISHED != PRODUCTIVE
PUBLISHED != CUTOVER
MATERIALIZED != PRODUCTIVE
tests PASS != PRODUCTIVE
```

Los artefactos históricos conservan su SHA/base original. Tras una publicación, una verificación histórica correcta puede ser ancestral respecto del `HEAD` actual; no exige igualdad permanente ni autoriza reescribir historia.
