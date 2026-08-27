# FeelingPilates — entrada para agentes

El repositorio es la autoridad arquitectónica y normativa. El chat sólo coordina: nunca es autoridad persistente. El orquestador externo ejecuta el workflow, pero no sustituye los documentos ni la evidencia física de este repositorio.

Antes de actuar, inspecciona físicamente branch, `HEAD`, staging, working tree y los archivos aplicables. No reconstruyas el estado desde conversaciones. Lee en este orden:

1. `auditoria/README-REESTRUCTURACION.md` y `auditoria/ESTADO-ACTUAL.md`.
2. El handoff activo indicado allí y el canónico de dominio, arquitectura o decisión pertinente.
3. El checkpoint, review e intervención concretos cuando la tarea requiera trazabilidad.
4. `auditoria/orquestacion/README.md` y el documento del protocolo aplicable.

Respeta el scope, el baseline dirty autorizado y las intervenciones autorizadas. Mide los cambios de cada agente mediante snapshots before/after; no atribuyas automáticamente al agente el baseline preexistente. No inventes la siguiente fase: debe estar autorizada por los canónicos físicos.

Roles de ejecución, corrección, documentación y auditoría están separados. Nadie se autoaudita; los auditores son independientes y con contexto fresh. Tests verdes no aprueban por sí solos la arquitectura, y código existente no establece autoridad productiva. Publicación no es activación productiva ni cutover.

El protocolo completo está en `auditoria/orquestacion/`: `README.md`, `WORKFLOW.md`, `STATE-MACHINE.md`, `GATES.md` y `ROLES.md`.
