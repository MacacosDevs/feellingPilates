# FeelingPilates — Reestructuración

Status: CANONICAL
Last updated: 2026-08-25
Repository verification: VERIFIED
Last verified against commit:
8c40594d2caf8b5230b364cb76cd8f48fe5ed98a
Verification scope: navegación documental

## Propósito

Este directorio conserva el estado arquitectónico y operativo de la reestructuración de FeelingPilates.

La documentación canónica existe para que el proyecto no dependa del historial de una conversación, de la memoria de un agente o de prompts antiguos.

La regla principal es:

```text
Git / código / migraciones
→ demuestran qué existe

Tests ejecutados
→ demuestran comportamiento cubierto

Documentación canónica
→ explica el estado, arquitectura y decisiones vigentes

Checkpoints históricos
→ conservan trazabilidad de una fase

Handoffs
→ permiten retomar una conversación

Chats
→ son contexto temporal, no fuente canónica
```

## Documentos canónicos

### `ESTADO-ACTUAL.md`

Responde:

> ¿Dónde está exactamente la reestructuración y cuál es el siguiente paso?

Contiene el estado de fases, intervención activa, último estado reportado del repositorio, verificaciones pendientes, tests recientes y handoff activo.

Es la autoridad documental sobre el estado operativo presente.

---

### `ARQUITECTURA-ACTUAL.md`

Responde:

> ¿Qué componentes existen actualmente y cómo se relacionan?

Distingue expresamente entre:

- PRODUCTIVO;
- LEGACY\_VIVO;
- IMPLEMENTADO\_NO\_PRODUCTIVO;
- EN\_TRANSICION;
- DISEÑADO\_NO\_IMPLEMENTADO;
- PENDIENTE.

No describe la cronología de fases.

---

### `DECISIONES-ARQUITECTONICAS.md`

Responde:

> ¿Qué decisiones arquitectónicas y políticas técnicas siguen aceptadas?

Distingue entre:

- decisión aceptada;
- decisión supersedida o descartada;
- grado de materialización.

No sustituye las reglas funcionales del dominio.

---

### `REGLAS-DE-TRABAJO-IA.md`

Responde:

> ¿Cómo deben trabajar ChatGPT, Claude Code, Codex, Git y los tests sobre este proyecto?

Define:

- pre-flight;
- alcance;
- implementación;
- review;
- gates;
- commits;
- actualización documental;
- stop conditions;
- handoffs.

---

### `contexto/DOMINIO-FUNCIONAL.md`

Responde:

> ¿Qué reglas funcionales relativamente estables debe respetar FeelingPilates?

Contiene invariantes del negocio, no implementación Java ni decisiones de Git.

---

### `contexto/MAPA-LEGACY-Y-MIGRACION.md`

Responde:

> ¿Qué modelo manda actualmente y qué falta para sustituir el legacy?

Es la autoridad documental sobre:

- autoridad productiva;
- coexistencia legacy/nueva;
- consumers actuales;
- estado de migración;
- condiciones de cutover.

---

### `handoffs/`

Contiene artefactos temporales de continuidad entre conversaciones.

Para saber cuál es el handoff actualmente activo, consultar exclusivamente:

`ESTADO-ACTUAL.md`

El README no conserva esa ruta porque es un dato mutable.

---

### `reviews/`

Contiene evidencia histórica de reviews relevantes que deben conservarse
fuera del historial de conversaciones.

Un review puede registrar:

- objeto revisado;
- alcance;
- hallazgos;
- P0/P1/P2;
- evidencia;
- veredicto;
- condición de cierre.

Los archivos de `reviews/`:

- NO son autoridad sobre el estado operativo actual;
- NO sustituyen `ESTADO-ACTUAL.md`;
- NO deben reinterpretarse retroactivamente cuando una fase evoluciona;
- sirven como trazabilidad del gate que realmente ocurrió.

---

### `intervenciones/`

Contiene instrucciones operativas versionables para intervenciones
concretas.

Una intervención puede existir en estado:

- preparada;
- no ejecutada;
- ejecutada;
- abortada;

sin que ello implique que la arquitectura descrita por esa intervención
haya sido materializada.

Los archivos de `intervenciones/`:

- NO son arquitectura actual;
- NO son decisiones aceptadas por sí mismos;
- NO sustituyen checkpoints;
- permiten ejecutar una intervención futura sin reconstruir sus
  instrucciones desde una conversación o memoria.

La intervención actualmente activa, si existe, se identifica únicamente
desde `ESTADO-ACTUAL.md`.

## Orden inicial de lectura

Para retomar el proyecto:

1. `README-REESTRUCTURACION.md`
2. `ESTADO-ACTUAL.md`
3. handoff activo indicado por `ESTADO-ACTUAL.md`
4. `MAPA-LEGACY-Y-MIGRACION.md` si la tarea toca una transición
5. `ARQUITECTURA-ACTUAL.md`, `DECISIONES-ARQUITECTONICAS.md` o `DOMINIO-FUNCIONAL.md` según la tarea
6. checkpoint histórico específico sólo cuando haga falta trazabilidad

## Checkpoints históricos

Los checkpoints históricos existentes en `auditoria/` no son documentación canónica del estado presente.

Los reviews y las intervenciones tampoco sustituyen a los checkpoints:

```text
checkpoint
→ qué se diseñó / implementó en una fase

review
→ qué observó un gate sobre ese checkpoint

intervención
→ qué debe hacerse para una corrección concreta
```

Deben consultarse cuando:

- se necesite reconstruir por qué se tomó una decisión;
- una regresión afecte comportamiento caracterizado en una fase;
- sea necesario revisar una implementación de esa fase;
- un documento canónico cite el checkpoint como origen;
- se investigue una decisión supersedida.

No es necesario leer todos los checkpoints antes de cada intervención.

## Regla contra reconstrucción desde chats

Un chat no debe reconstruir el estado actual basándose únicamente en otra conversación.

Si existe contradicción entre un chat y las fuentes canónicas o el repositorio:

1. clasificar qué tipo de dato está en conflicto;
2. consultar la fuente competente;
3. verificar el repositorio cuando corresponda;
4. marcar la documentación `STALE` si ya no representa la realidad.

El historial de chat es una fuente auxiliar y temporal.
