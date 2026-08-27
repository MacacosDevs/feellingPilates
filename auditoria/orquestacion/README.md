# FeelingPilates — protocolo de orquestación

**Versión:** `ORQ-PROTOCOL-V1`
**Estado:** NORMATIVO

## Propósito

Este directorio versiona el protocolo estable para trabajo multiagente en FeelingPilates. Permite retomar una intervención sin depender de chats, journals efímeros ni memoria de agentes. No define una fase funcional nueva ni altera la autoridad de las fuentes canónicas existentes.

## Autoridad y operación

| Artefacto | Papel |
| --- | --- |
| Repositorio y sus canónicos | Fuente de verdad arquitectónica y normativa. |
| Git, código y migraciones | Evidencia física de materialización. Su existencia no crea autoridad productiva. |
| Tests | Evidencia técnica del comportamiento cubierto; un PASS no equivale por sí solo a aprobación arquitectónica. |
| Chat | Mecanismo temporal de coordinación; nunca autoridad persistente. |
| `FeelingPilatesOrchestrator` externo | Motor operativo que descubre, ejecuta roles, aplica gates y conserva evidencia de runs. |
| `state.json` | Journal/cache operacional; no canon. |
| `runs/` y logs | Evidencia operacional o diagnóstica; no canon normativo. |

El orquestador vive fuera de este repositorio y no se copia aquí. Su comportamiento vigente se contrasta con este contrato, pero sus IDs de runs, sesiones, logs y estado local no forman parte del contrato versionado.

## Documentos del protocolo

- [WORKFLOW.md](WORKFLOW.md): flujo normativo y sus workflows.
- [STATE-MACHINE.md](STATE-MACHINE.md): estados estables y estados operacionales del motor.
- [GATES.md](GATES.md): clasificación, gates, stops y reglas de evidencia.
- [ROLES.md](ROLES.md): contratos, aislamiento e independencia de cada rol.

Los canónicos de `auditoria/` siguen siendo la autoridad para estado actual, arquitectura, decisiones, dominio y migración. Los checkpoints, intervenciones y reviews conservan historia: no se reescriben para coincidir artificialmente con el `HEAD` actual.

## Evolución

Cambiar el protocolo es una modificación normativa: requiere scope explícito, contraste con el motor externo cuando sea aplicable y review independiente. No se fijan aquí SKUs de modelos LLM, contadores concretos, IDs de runs ni resultados históricos de tests; son configuración o evidencia operacional que puede cambiar.
