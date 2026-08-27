# FeelingPilates — Decisiones arquitectónicas

Status: CANONICAL
Last updated: 2026-08-27
Repository verification: VERIFIED
Last verified against commit:
95900d8a1d787a24aff4ee4e10f69d540ce81339
Verification scope: decisiones aceptadas, incluida la materialización en commit Git verificado de F2D.2 en dark launch y en revisión documental

## Estados

### Estado de decisión

- `PROPUESTA`
- `ACEPTADA`
- `SUPERSEDIDA`
- `DESCARTADA`

### Estado de materialización

- `NO_APLICA`
- `NO_INICIADA`
- `PARCIAL`
- `IMPLEMENTADA`
- `VERIFICADA`

La aceptación de una decisión y su implementación son dimensiones independientes.

---

# DA-001 — Monolito modular antes que microservicios prematuros

**Estado de decisión:** ACEPTADA
**Estado de materialización:** PARCIAL

**Decisión**

FeelingPilates evoluciona inicialmente como monolito modular.

No se introduce por defecto:

- arquitectura distribuida;
- broker;
- microservicios;
- coordinación remota entre dominios.

**Motivo breve**

Los principales problemas actuales son de límites de dominio, integridad y transición legacy, no de escala operacional que requiera distribución.

**Impacta a**

Backend completo y futuras separaciones de módulos.

**Origen**

Dirección arquitectónica general de la reestructuración.

---

# DA-002 — Horario operativo y programación son conceptos independientes

**Estado de decisión:** ACEPTADA
**Estado de materialización:** VERIFICADA

**Decisión**

El horario durante el cual un salón puede operar no constituye por sí mismo programación de actividades.

La programación debe respetar el horario operativo, pero ambos dominios se modelan separadamente.

**Motivo breve**

Permite representar correctamente aperturas, cierres, excepciones y múltiples programaciones dentro de la misma ventana operativa.

**Impacta a**

Salones, horarios, programación, reservas.

**Origen**

F1/F2; materializado especialmente en F2A–F2C.

---

# DA-003 — Regla recurrente no equivale a sesión concreta

**Estado de decisión:** ACEPTADA
**Estado de materialización:** PARCIAL

**Decisión**

Una regla recurrente define programación nominal/repetitiva.

Una futura sesión representa una ocurrencia concreta.

No deben conflarse.

**Motivo breve**

Confirmaciones, capacidad, historial y reservas necesitan identidad concreta sin mutar la recurrencia.

**Impacta a**

Programación, futuras sesiones, reservas.

**Origen**

Diseño de programación de F1/F2.

---

# DA-004 — Migración incremental y preservación de consumidores

**Estado de decisión:** ACEPTADA
**Estado de materialización:** VERIFICADA

**Decisión**

Las áreas legacy se sustituyen de forma incremental.

Un contrato consumido no se elimina hasta disponer de una estrategia de migración/cutover validada.

**Motivo breve**

Evitar Big Bang rewrites y desacoplar evolución interna del despliegue de consumidores.

**Impacta a**

Backend, frontend, mobile y migraciones.

**Origen**

Aplicada explícitamente en F2B/F2C.

---

# DA-005 — PostgreSQL protege invariantes temporales críticas

**Estado de decisión:** ACEPTADA
**Estado de materialización:** VERIFICADA

**Decisión**

Cuando una invariante temporal/concurrente puede violarse pese a prechecks de aplicación, PostgreSQL participa como defensa de integridad mediante constraints apropiadas.

**Motivo breve**

Un precheck Java aislado no cierra carreras concurrentes.

**Impacta a**

Horarios operativos y futuros modelos temporales.

**Origen**

F2B.

---

# DA-006 — Vigencias explícitas, sin sentinels artificiales

**Estado de decisión:** ACEPTADA
**Estado de materialización:** VERIFICADA

**Decisión**

Las vigencias temporales se representan mediante límites de fecha explícitos y `null` cuando corresponde representar un extremo abierto.

No se utilizan fechas centinela como sustituto de infinito.

**Motivo breve**

Semántica temporal más precisa y constraints PostgreSQL coherentes.

**Impacta a**

Horario operativo y programación versionada.

**Origen**

F2B.

---

# DA-007 — Resolver temporal único para el horario operativo

**Estado de decisión:** ACEPTADA
**Estado de materialización:** VERIFICADA

**Decisión**

Los consumidores no deben recomponer de forma independiente:

```text
excepción puntual
+
horario semanal
```

El horario efectivo se obtiene mediante una única composición temporal.

**Motivo breve**

Evitar semánticas divergentes entre consumidores.

**Impacta a**

Programación, reservas y salones.

**Origen**

F2B/F2C.

---

# DA-008 — Locking compartido por todos los writers que compiten

**Estado de decisión:** ACEPTADA
**Estado de materialización:** PARCIAL

**Decisión**

Un lock sólo cierra una carrera si todos los writers que compiten por la misma invariante participan del protocolo compatible.

**Motivo breve**

Un writer protegido y otro no protegido siguen permitiendo TOCTOU y estados imposibles.

**Impacta a**

Horarios, excepciones, reservas y futuras operaciones concurrentes.

**Origen**

F2B/F2C y reafirmado por reviews posteriores.

---

# DA-009 — `Clock` inyectado para reglas temporales

**Estado de decisión:** ACEPTADA
**Estado de materialización:** VERIFICADA EN ÁREAS REFACTORIZADAS

**Decisión**

Las reglas de negocio que dependen de la fecha utilizan un `Clock` inyectado.

No deben depender de llamadas dispersas al reloj real del sistema.

**Motivo breve**

Determinismo, testabilidad y semántica temporal consistente.

**Impacta a**

Horarios, excepciones y futuros writers temporales.

**Origen**

F2B/F2C.

---

# DA-010 — Traducción de constraints sólo con evidencia inequívoca

**Estado de decisión:** ACEPTADA
**Estado de materialización:** VERIFICADA EN F2C

**Decisión**

Una excepción de integridad se traduce a un código de negocio específico únicamente cuando existe evidencia estructurada suficiente de la constraint concreta.

No basta un SQLSTATE genérico.

**Motivo breve**

Evitar ocultar violaciones de integridad no relacionadas bajo un error incorrecto.

**Impacta a**

Persistencia y errores API.

**Origen**

F2C.2.1.

---

# DA-011 — Códigos de error estables separados del mensaje humano

**Estado de decisión:** ACEPTADA
**Estado de materialización:** VERIFICADA EN ÁREAS MIGRADAS

**Decisión**

Los consumidores programáticos deben depender de códigos de error estables, no de análisis textual de mensajes humanos.

**Motivo breve**

Mantener contratos API robustos frente a cambios de wording.

**Impacta a**

Backend y frontend.

**Origen**

F2B/F2C.

---

# DA-012 — No modificar automáticamente templates recurrentes por eventos puntuales

**Estado de decisión:** ACEPTADA
**Estado de materialización:** PARCIAL

**Decisión**

Una excepción puntual de operación o programación no debe modificar, recortar o reversionar automáticamente la plantilla recurrente de semanas posteriores.

**Motivo breve**

Preservar la intención de una regla recurrente y separar temporalidades.

**Impacta a**

Programación y horario operativo.

**Origen**

F2C y diseño de F2D.

---

# Decisiones supersedidas relevantes

## DA-S-001 — Recortar automáticamente una ocurrencia recurrente al horario disponible

**Estado de decisión:** SUPERSEDIDA
**Estado de materialización:** NO\_APLICA

**Decisión anterior**

Una ocurrencia recurrente parcialmente fuera del horario efectivo podía considerarse recortable.

**Superseded by**

DA-012 y regla funcional correspondiente.

**Estado actual**

No se recorta automáticamente.

---

## DA-S-002 — Utilizar `TurnoInstructor.EXCEPCION` como mecanismo suficiente de reemplazo puntual futuro

**Estado de decisión:** SUPERSEDIDA
**Estado de materialización:** NO\_APLICA

**Decisión anterior**

Se consideró que el mecanismo legacy podría cubrir ajustes puntuales.

**Estado actual**

F2D.1 demostró que su semántica no permite expresar de forma adecuada el modelo futuro de reemplazo individual.

El diseño sustituto F2D fue aprobado posteriormente por el gate final de F2D.1. La afirmación histórica de que su materialización no había iniciado corresponde al corte anterior: F2D.2 ya está implementada internamente en dark launch y permanece en revisión documental.

---

# DA-013 — Ajustes F2D y F2D.2 como dark launch aislado

**Estado de decisión:** ACEPTADA
**Estado de materialización:** IMPLEMENTADA (F2D.2 `IMPLEMENTADA_EN_REVIEW`)

**Decisión**

F2D.2 materializa internamente el diseño aprobado de ajustes puntuales como dark launch. Su estado vigente es `IMPLEMENTADA_EN_REVIEW`: la implementación cuenta con aprobación técnica, pero la documentación sigue en revisión. Ningún estado exclusivo de `programacion_*` puede permitir, rechazar, modificar, ocultar o transformar un flujo productivo legacy durante F2D.2.

Por tanto, F2D.2 excluye controllers públicos, consumers productivos, adapters sobre writers legacy, `ImpactoAjustesEnExcepcionHorario`, `Reserva` legacy, frontend/mobile, cutover y fence persistido. `ImpactoAjustesEnExcepcionHorario` queda diferido a una futura fase de activación/cutover con fence efectivo.

La identidad y composición aprobadas son:

- target de `CANCELACION`/`REEMPLAZO`: `serieId + fecha` sobre la ocurrencia nominal;
- identidad recurrente/reemplazo: `serieId + fecha`;
- identidad de adición: `ajusteId + fecha`;
- `ProgramacionEfectiva`: `NOMINAL → AJUSTES → OPERATIVO FINAL`, aplicando el horario del salón resultado y validación fail-closed de maestros vigentes.

V47 incluye el EXCLUDE temporal por serie requerido y no introduce una FK directa desde el ajuste a `serieId`.

La concurrencia aprobada exige:

- Policy A inversa;
- `SalonLocks` deduplicados y ordenados;
- `InstructorLocks` deduplicados y ordenados;
- orden global `SALONES → INSTRUCTORES`;
- participación de todos los writers recurrentes competidores;
- relectura bajo locks y comparación del lock set para cerrar TOCTOU.

`Reserva` legacy permanece fuera de F2D.2. Se prohíben la doble autoridad productiva y cualquier activación implícita de la programación nueva.

**Motivo breve**

Permitir la materialización interna y verificable del modelo nuevo sin que datos dark-launch alteren la autoridad legacy antes de una activación controlada.

**Impacta a**

F2D.2, programación efectiva, persistencia futura, concurrencia y futura activación/cutover.

**Origen**

Checkpoint F2D.1, F2D.1.1, F2D.1.2, intervención F2D.2.2, checkpoint de implementación F2D.2 y gate final `P0=0 / P1=0 / P2=0`.
