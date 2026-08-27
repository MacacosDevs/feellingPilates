# Contratos de roles

Las configuraciones de modelo son operacionales. El protocolo define capacidades y perfiles abstractos, no un SKU LLM concreto. Cada ejecución toma snapshot before/after y reporta un `AgentResult` estructurado cuando aplica.

| Rol | Propósito / inputs | Escritura y sandbox | Gates / prohibiciones | Independencia |
| --- | --- | --- | --- | --- |
| ORCHESTRATOR | Descubre estado físico y documental; recibe canónicos, snapshots, workflow y resultados estructurados. | No implementa código; coordina el motor operativo. | Aplica sólo transiciones y gates explícitos; no convierte frases libres en gates. | No sustituye las fuentes canónicas por su journal. |
| EXECUTOR | Materializa la intervención autorizada usando scope, canónicos y checkpoint pertinentes. | Puede escribir sólo el scope aprobado en workspace-write. | No hace `git add`, commit o push; no amplía arquitectura ni se autoaudita. | Debe entregar su trabajo a AUDITOR fresh. |
| AUDITOR | Revisa adversarialmente implementación, diff, tests, intervención y canónicos. | Read-only y sandbox efímero. | Clasifica P0/P1/P2; no modifica archivos ni Git. | Siempre fresh e independiente del ejecutor/corrector. |
| CORRECTOR | Cierra sólo los P1 de un audit y artefacto correctivo autorizado. | Puede escribir sólo el scope de corrección en workspace-write. | No inventa soluciones, no modifica P2 fuera de scope, no aprueba su corrección ni publica. | Requiere re-audit fresh; HostValidator cuando aplique. |
| HOST_VALIDATOR | Produce evidencia reproducible de host para plan de validación aprobado. | Host determinista; no LLM, tokens = 0. | Plan allowlisted; Docker preflight; nunca comandos desde AgentResult; conserva `HEAD`, staging y fingerprint. | Es independiente de claims LLM. |
| DOCUMENTER | Materializa documentación autorizada y preserva trazabilidad histórica. | Sólo paths documentales allowlisted en workspace-write. | No cambia código, tests, migraciones ni configuración; cambio de código durante su run es `SECURITY_STOP`. | Lo audita DOCUMENT_AUDITOR fresh. |
| DOCUMENT_AUDITOR | Verifica coherencia documental con evidencia física, gates e historia. | Read-only y sandbox efímero. | No edita ni convierte claims del documenter en evidencia. | Siempre fresh e independiente del DOCUMENTER. |
| PUBLISHER | Verifica y publica únicamente cuando la política y los gates lo permiten. | Puede realizar la publicación autorizada desde el entorno configurado. | No decide arquitectura; exige scope exacto, gates PASS y remoto consistente; evita duplicar un commit ya remoto. | La publicación queda sujeta al cierre documental independiente. |

## Reglas transversales

- El working tree puede tener un `PREEXISTING_AUTHORIZED_DIRTY_BASELINE`. El scope pertenece al delta que el rol produce, no a todos los paths ya dirty.
- Un baseline existente puede ser evidencia read-only para DOCUMENTER o AUDITOR; no puede normalizarse, revertirse ni atribuirse a su ejecución.
- El audit de implementación y el documental no reutilizan la conclusión de otro agente como aprobación. Inspeccionan por sí mismos la evidencia actual.
- Ningún rol determina una fase funcional futura por inferencia. Consulta roadmap y canónicos; sin autorización explícita, el estado terminal permanece sin próxima acción.
