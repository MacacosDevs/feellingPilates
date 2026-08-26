# FeelingPilates — Handoff F2D.2

Handoff status: ACTIVE

## Estado de arranque

```text
F2C: CERRADA
F2D.1: DISEÑO_APROBADO / CERRADA
F2D.1.1: EJECUTADA
F2D.1.2: EJECUTADA
F2D.2: NO INICIADA
```

`DISEÑO_APROBADO` no significa implementación. F2D continúa diseñada y aprobada, pero **NO IMPLEMENTADA**.

## Autoridad productiva

- `TurnoInstructor`: `LEGACY_VIVO / PRODUCTIVO`; autoridad actual de programación.
- `BloqueProgramacion + Asignacion`: `IMPLEMENTADO_NO_PRODUCTIVO`.
- Ajustes F2D: diseñados y aprobados; no implementados.
- Cutover y fence F2D: no implementados.

No existe doble autoridad productiva.

## Autoridades documentales

- `auditoria/ESTADO-ACTUAL.md`
- `auditoria/ARQUITECTURA-ACTUAL.md`
- `auditoria/DECISIONES-ARQUITECTONICAS.md`
- `auditoria/REGLAS-DE-TRABAJO-IA.md`
- `auditoria/contexto/DOMINIO-FUNCIONAL.md`
- `auditoria/contexto/MAPA-LEGACY-Y-MIGRACION.md`

## Cadena de evidencia F2D.1

- Checkpoint aprobado: `auditoria/fase-2d1-diseno-ajustes-programacion-fecha.md`
- Review original: `auditoria/reviews/F2D.1-REVIEW-AJUSTES-PUNTUALES.md`
- Intervención F2D.1.1: `auditoria/intervenciones/F2D.1.1-CORRECCION-POST-REVIEW.md`
- Re-review post F2D.1.1: `auditoria/reviews/F2D.1.1-RE-REVIEW-POST-CORRECCION.md`
- Intervención F2D.1.2: `auditoria/intervenciones/F2D.1.2-AISLAMIENTO-DARK-LAUNCH.md`
- Re-review final: `auditoria/reviews/F2D.1.2-RE-REVIEW-FINAL.md`

SHA-256 del checkpoint aprobado:

`58af39f41b3bc089ebbd4ec67f684e270087ddf4eb695f2c7b55276d0aff352e`

La cadena preserva el review original `P0=0 / P1=8 / P2=3`, el re-review post F2D.1.1 `P0=0 / P1=1 / P2=0` y el gate final `P0=0 / P1=0 / P2=0`.

## Reglas obligatorias de F2D.2

F2D.2 será **DARK LAUNCH**:

- implementación interna únicamente;
- sin controllers públicos;
- sin consumers productivos;
- sin adapters sobre writers legacy;
- sin `ImpactoAjustesEnExcepcionHorario`;
- sin `Reserva` legacy;
- sin frontend/mobile;
- sin cutover ni fence persistido;
- sin doble autoridad.

Durante F2D.2 ningún estado exclusivo de `programacion_*` puede alterar el resultado observable de un flujo productivo legacy.

## Arranque de F2D.2

F2D.2 **NO se ejecuta directamente desde este handoff**.

Primero se debe:

1. preparar una intervención concreta conforme a `auditoria/REGLAS-DE-TRABAJO-IA.md`;
2. realizar pre-flight real;
3. validar la base y el scope;
4. sólo después ejecutar la intervención autorizada.

## STOP

Detenerse y reportar si:

- el checkpoint no coincide con el SHA-256 aprobado;
- F2D.1 fue modificado posteriormente;
- el estado Git diverge materialmente;
- la documentación contradice este corte;
- existe implementación F2D.2 inesperada.

Este handoff es autocontenido y no depende de conversaciones anteriores.
