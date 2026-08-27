# Workflow normativo

## Pre-flight y preparación

Todo workflow comienza con `PREPARE`: identificar repo y scope, leer los canónicos competentes, inspeccionar físicamente branch, `HEAD`, staging y working tree, y tomar un snapshot. Un baseline dirty previamente autorizado se conserva y se registra; el scope del agente se calcula por su delta before/after. Una discrepancia material entre documentación y evidencia física se reconcilia o se detiene.

Un fallo anterior al consumo semántico de un `AgentResult` válido se clasifica como `PRE_SEMANTIC_OPERATIONAL_FAILURE` conforme a [GATES.md](GATES.md) y [STATE-MACHINE.md](STATE-MACHINE.md); no consume por sí mismo una transición semántica.

## Workflow profile y bloques componibles

Cada intervención declara explícitamente un `WORKFLOW_PROFILE`, o decisión canónica equivalente, con los stages, gates, validaciones y roles aplicables a su scope. La aplicabilidad se deriva del scope autorizado, del artefacto o intervención y de ese profile; nunca de la decisión unilateral de un agente que quiera omitir un control. Un bloque no pertinente se declara `NOT_APPLICABLE`, no `PASS`; un bloque aplicable pendiente no puede omitirse.

La aplicabilidad y el lifecycle de un gate son dimensiones distintas. `NOT_APPLICABLE` significa que el gate no pertenece al profile; `PENDING` significa que sí pertenece, pero su etapa todavía no ocurrió o aún no fue evaluada. Para una transición `T`, deben estar en `PASS` todos y sólo los gates aplicables que el profile exige resolver antes de `T`. Los gates aplicables de etapas futuras permanecen `PENDING`; no se convierten en `PASS` ni en `NOT_APPLICABLE` para adelantar la transición.

Los bloques del protocolo son componibles. La secuencia siguiente ilustra los bloques posibles de un cambio técnico con documentación y publicación; no es un recorrido universal. Un cambio exclusivamente documental o normativo puede componer `DOCUMENT → DOCUMENT AUDIT → READY TO PUBLISH → PUBLICATION → PUBLICATION CLOSURE → PUBLICATION CLOSURE AUDIT` sin fabricar gates de implementación o tests. Las transiciones concretas las define el profile autorizado.

```text
PREPARE → EXECUTE → AUDIT
                      │
          P1 corregible ─→ CORRECT → HOST VALIDATION (si aplica) → RE-AUDIT
                      │                                      │
                      └──────────────────────────────────────┘
                                    ↓
                                DOCUMENT → DOCUMENT AUDIT
                                    │             │
                        P1 documental corregible  │ PASS
                                    └──→ CORRECT ─┘
                                                  ↓
                                           READY TO PUBLISH
                                                  ↓
                                             PUBLICATION
                                                  ↓
                                      PUBLICATION CLOSURE
                                                  ↓
                                    PUBLICATION CLOSURE AUDIT
                                                  ↓
                                     terminal / no next action
```

Un P0, una decisión faltante, un P1 no corregible, un límite de ciclos o una condición de seguridad llevan a `HUMAN_STOP`; no se ocultan mediante una nueva fase o una corrección no autorizada.

`FeelingPilatesOrchestrator` puede implementar workflows concretos, pero su workflow operativo actual no define por sí solo la semántica universal de este protocolo. Si recibe un profile autorizado que todavía no implementa, debe rechazarlo explícitamente con `UNSUPPORTED_WORKFLOW_PROFILE` o una clasificación equivalente que falle de forma cerrada. No debe fabricar gates, saltarse controles, reinterpretar el profile ni degradarlo silenciosamente a otro workflow. Éste es un requisito contractual, no una afirmación de que esa clasificación ya exista en el motor.

## Workflow de implementación

1. `EXECUTE`: el EXECUTOR materializa sólo la intervención autorizada y reporta un `AgentResult` estructurado.
2. `AUDIT`: un AUDITOR fresh e independiente inspecciona código, diff, tests, canónicos e intervención; el ejecutor nunca aprueba su propio trabajo.
3. Si el audit encuentra P1 con una única corrección ya autorizada y sin decisión humana, pasa a `CORRECT`. De lo contrario, se detiene.
4. El CORRECTOR toca exclusivamente los P1 autorizados y no aprueba su propia corrección.
5. Cuando la corrección requiere evidencia de host, se ejecuta `HOST VALIDATION`; después hay `RE-AUDIT` independiente. La aprobación técnica exige los gates aplicables, no sólo tests verdes.

## Workflow documental

En un profile con materialización técnica, tras `IMPLEMENTATION_APPROVED` el DOCUMENTER materializa únicamente documentación permitida y conserva el historial. Un profile exclusivamente documental o normativo entra al bloque documental desde su precondición autorizada: scope documental y autoridad normativa válidos, pre-flight completo e invariantes de scope y seguridad satisfechos. No requiere aprobación de implementación, tests, evidencia técnica ni HostValidator cuando el profile los determina materialmente `NOT_APPLICABLE`; esos gates no se fabrican como `PASS`. Código dirty preexistente puede leerse como evidencia, pero un cambio de código introducido por el DOCUMENTER es `SECURITY_STOP`.

Un DOCUMENT_AUDITOR fresh e independiente revisa autoridad, scope, coherencia normativa, delta documental, seguridad y los gates que el profile exige resolver en esa etapa. Cuando el profile incluye una materialización técnica, contrasta también la evidencia y preservación de sus gates técnicos; un profile documental puro no exige evidencia de implementación inexistente. Un P1 documental sólo puede corregirse automáticamente si es inequívoco, no exige decisión humana y todos los gates aplicables ya resueltos se preservan, sin inventar resultados para gates `NOT_APPLICABLE` o `PENDING`. El ciclo documental tiene su propio contador.

## Workflow de publicación

`READY_TO_PUBLISH` existe sólo cuando todos y sólo los gates aplicables que deben resolverse **antes** de publicar están en `PASS`, no queda ningún P0 o P1 pendiente, no hay decisión humana pendiente ni `SECURITY_STOP`, y el scope de ese workflow sigue siendo válido. Un implementation gate o tests gate no se exige cuando la intervención no contiene materia técnica correspondiente; tampoco puede omitirse silenciosamente cuando sí aplica. Si el profile incluye publicación, el publication gate y el publication closure gate siguen `PENDING` en `READY_TO_PUBLISH`, porque sus etapas aún no ocurrieron.

La publicación automática es política explícita. Con `auto_publish=false`, el sistema debe detenerse de forma segura en `READY_TO_PUBLISH`; una publicación manual puede importarse tras verificarse físicamente. El publication gate se evalúa alrededor de la acción y sólo pasa a `PASS` después de verificar materialmente, según la política aplicable, el commit o artefacto esperado, estado local/remoto, publicación real, ausencia de duplicación e invariantes pertinentes. No se vuelve a publicar un commit ya verificado como remoto.

Publicar versiona y distribuye una decisión aprobada; no declara por sí mismo runtime, autoridad productiva ni cutover. Esas dimensiones se verifican por separado contra los canónicos y la evidencia física.

## Workflow de cierre de publicación

Después de publicar con el publication gate en `PASS`, `PUBLICATION_CLOSURE` documenta y reconcilia la evidencia posterior a la publicación. El publication closure gate permanece `PENDING` durante la publicación y la preparación de ese cierre. Sólo pasa a `PASS` después de materializar la documentación de cierre y superar el `PUBLICATION CLOSURE AUDIT` de un DOCUMENT_AUDITOR independiente; los P1 de ese cierre usan un contador de corrección separado. Cuando el cierre pasa, el workflow queda terminal: no se infiere una fase funcional siguiente. Ésta sólo puede derivarse de un roadmap o canónico que la autorice explícitamente.
