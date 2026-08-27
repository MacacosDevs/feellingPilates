# Gates, evidencia y stops

## Severidad

| Nivel | Significado | Efecto |
| --- | --- | --- |
| P0 | Bloqueo crítico de seguridad, autoridad o inconsistencia inaceptable. | `HUMAN_STOP`. |
| P1 | Problema material que impide aprobar el gate actual. | Corrección sólo si es inequívoca, autorizada y no requiere decisión humana; si no, `HUMAN_STOP`. |
| P2 | Mejora, deuda o riesgo no bloqueante. | Se registra y se decide explícitamente; sólo bloquea si el workflow o contrato concreto lo establece. |

Un resultado estructurado declara, como mínimo, rol, status, gate, hallazgos P0/P1/P2, evidencia de tests, paths cambiados, recomendación, `requires_human_decision`, `p1_correctable` y el artefacto correctivo cuando corresponde. El schema valida la forma; invariantes relacionales complejas se validan en runtime. Antes de lanzar un agente, el motor hace schema preflight. La metadata semánticamente redundante se canonicaliza para evitar que forme estados distintos sin significado. Una incompatibilidad o fallo anterior a un `AgentResult` válido se trata por la categoría pre-semántica definida abajo, no como hallazgo semántico.

## Aplicabilidad por profile y lifecycle del gate

Cada intervención declara mediante su `WORKFLOW_PROFILE`, o decisión canónica equivalente, qué stages, gates, validaciones y roles son aplicables. La aplicabilidad se deriva del scope autorizado y del artefacto o intervención; no puede decidirla unilateralmente un agente para saltarse controles. Omitir silenciosamente un gate aplicable es inválido.

La **aplicabilidad** responde si un gate pertenece al workflow: `APPLICABLE` o `NOT_APPLICABLE`. El **lifecycle** responde en qué situación está un gate aplicable: `PENDING`, `PASS`, `FAIL` y, cuando el vocabulario operativo lo requiera, `BLOCKED`/`STOP`. Por tanto, el estado conceptual de un gate admite al menos:

- `NOT_APPLICABLE`: el gate no pertenece a ese workflow/profile; no bloquea, no cuenta como aprobado y no se falsifica como ejecutado.
- `PENDING`: el gate pertenece al workflow, pero su etapa todavía no corresponde, no ocurrió o aún no fue evaluado.
- `PASS`: el gate aplicable fue ejecutado o resuelto satisfactoriamente en la etapa competente.
- `FAIL`: el gate aplicable fue evaluado y no pasó.
- `BLOCKED`/`STOP`: el gate aplicable no puede resolverse bajo las condiciones operacionales, de autoridad o seguridad vigentes, conforme a la clasificación correspondiente.

`NOT_APPLICABLE != PENDING != PASS`. Nunca se usa `NOT_APPLICABLE` para representar «todavía no llegamos a esa etapa». El catálogo siguiente contiene gates posibles: el profile selecciona los aplicables sin inventar resultados para los demás.

- **Scope gate:** el delta del run está dentro del scope/allowlist; snapshot before/after protege `HEAD`, staging y baseline. Un dirty baseline autorizado no se atribuye al run. Si un DOCUMENTER cambia código, tests, migraciones o cualquier path fuera de su allowlist, es `SECURITY_STOP`.
- **Tests gate:** las pruebas requeridas pasan en el entorno competente. Fallos funcionales son evidencia de fallo; fallos ambientales se clasifican por separado y no se hacen pasar por resultado semántico.
- **Implementation gate:** audit independiente verifica implementación real contra intervención, canónicos, diff, seguridad, concurrencia, persistencia, compatibilidad y tests. EXECUTOR y CORRECTOR no se autoaprueban.
- **Host validation:** se usa cuando la evidencia requerida depende de Docker/Testcontainers u otra capacidad host ausente en el sandbox LLM. El sandbox no tiene que demostrar esos tests Docker si existe este camino seguro.
- **Documentation gate:** audit fresh e independiente verifica autoridad, scope, coherencia normativa, delta, independencia, seguridad y gates exigibles en esa etapa. Contrasta evidencia técnica y preservación de gates técnicos sólo cuando son aplicables al profile.
- **Publication gate:** si es aplicable, permanece `PENDING` antes de publicar. Se evalúa alrededor y después de la acción, y pasa a `PASS` sólo al verificar materialmente, según política, commit o artefacto esperado, estado local/remoto cuando aplique, publicación real, ausencia de duplicación e invariantes pertinentes. Auto-publicar es una política explícita; en modo manual se para seguro antes de publicar.
- **Publication closure gate:** si es aplicable, permanece `PENDING` antes de publicar y después de publicar mientras falta el cierre. Sólo pasa a `PASS` tras la documentación de cierre y su audit independiente; su corrección documental tiene un ciclo propio.

## Requisitos de transición

La aplicabilidad al profile no convierte todo gate en prerrequisito de toda transición. Para una transición `T`, deben estar en `PASS` todos y sólo los gates que (1) son aplicables al workflow y (2) deben estar resueltos antes de `T`. Los gates aplicables de etapas futuras permanecen `PENDING`; los no aplicables permanecen `NOT_APPLICABLE`.

`READY_TO_PUBLISH` requiere en `PASS` todos y sólo los gates aplicables exigibles **antes de publicar**, ningún P0 o P1 pendiente, ninguna decisión humana pendiente, ningún `SECURITY_STOP` y scope válido para ese workflow. Según el profile, esos prerrequisitos pueden incluir scope, implementation, tests y documentation gates. No exige implementation gate ni tests gate si no existe materialización o materia técnica correspondiente; sí los exige cuando el profile y el scope determinan que aplican. No exige publication gate ni publication closure gate en `PASS`: si son aplicables, ambos permanecen `PENDING` en esta etapa.

La aplicabilidad por profile nunca autoriza a desactivar seguridad. Para cualquier agente con capacidad de escritura, los invariantes de scope, validación de touched paths, `HEAD`/staging cuando correspondan y demás controles de seguridad pertinentes son obligatorios. Un profile no puede marcarlos `NOT_APPLICABLE` para evitar su verificación.

## Fallo operacional pre-semántico

`PRE_SEMANTIC_OPERATIONAL_FAILURE` ocurre antes de que un `AgentResult` válido sea consumido y pueda afectar semánticamente el workflow. Incluye, entre otros, schema incompatible en preflight, fallo de proceso o invocación sin resultado válido y resultado no consumido por la state machine.

Ante esta categoría no se genera gate semántico, no se inventan P0/P1/P2, no se consume una transición de negocio y no se incrementa el ciclo de corrección técnica, documental, de cierre de publicación ni otro contador semántico. El agente no queda registrado como aprobador ni como fallo semántico del contenido. Sí se registran de forma auditable el lifecycle operacional, exit code, causa, diagnóstico, evidencia técnica y recovery.

Cuando la causa sea recuperable de forma determinista, segura y dentro de la autoridad disponible, se permite retry, reparación de configuración, preflight corregido, reconciliación de metadata o reconstrucción desde estado físico. Se preservan `HEAD`, staging, scope, fingerprints, autoridad e historia cuando corresponda. La metadata operacional stale no prevalece sobre Git físico, artefactos canónicos, manifests, historial causal, fingerprints o evidencia persistida si estas fuentes permiten una reconciliación inequívoca. El recovery se registra sin reescribir historia para ocultar la inconsistencia.

Sólo escala a `HUMAN_STOP` si no existe recuperación determinista segura, falta autoridad, hay conflicto entre fuentes de autoridad, existe `SECURITY_STOP`, se requiere una decisión de arquitectura o producto, el entorno indispensable no tiene fallback seguro, se excede una política operacional aplicable de retry/ciclos o la reparación requeriría una mutación no autorizada. Un fallo de schema pre-semántico, una invocación transitoria o metadata stale pero reconciliable no equivalen por sí solos a `HUMAN_STOP`.

`PROCESS FAILURE != SEMANTIC GATE FAILURE`: `process exit != 0` junto con ausencia de `AgentResult` válido y `state_consumed=false` exige clasificar primero la causa operacional; no equivale automáticamente a P0, P1 o `HUMAN_STOP`.

## HostValidator determinista

El `HOST_VALIDATOR` no usa LLM ni tokens. Ejecuta desde el host un plan estático, configurado y allowlisted; nunca ejecuta comandos arbitrarios sugeridos por un `AgentResult`. Realiza preflight de Docker y mantiene invariantes de `HEAD`, staging y fingerprint de fuente antes, durante y después del plan. Los outputs de build son artefactos de ejecución y no equivalen a una mutación de fuente.

Un `FAIL` funcional indica que la validación se ejecutó y falló; una falta de Docker o de entorno es `BLOCKED`/ambiental y sólo exige parada humana si no existe fallback seguro. Ningún conteo histórico de tests es requisito universal del protocolo.

## Human y security stops

`HUMAN_STOP` se reserva para una decisión real de arquitectura o producto, P0, P1 no corregible, límite de ciclos, autoridad faltante, seguridad, o entorno requerido sin fallback seguro. No se usa para Docker inaccesible al sandbox cuando hay HostValidator, baseline dirty autorizado, P1 documental inequívoco ni fallos transitorios sin resultado semántico que pueden reintentarse con seguridad.

`SECURITY_STOP` protege mutación indebida, scope fuera de autorización, cambio de `HEAD`/staging no permitido, o pérdida de invariantes de fuente durante validación. No se resuelve reinterpretando logs: requiere inspección y decisión explícita.

Se mantienen contadores separados para corrección técnica, corrección documental y corrección de cierre de publicación. Sus máximos son configuración operacional, no una constante de este protocolo.
