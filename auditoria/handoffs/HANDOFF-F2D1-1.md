# FeelingPilates — Handoff F2D.1.1

Handoff status: ARCHIVED
Materialization status: MATERIALIZED
Succeeded by: `auditoria/handoffs/HANDOFF-F2D2.md`
Repository verification: VERIFIED
Last verified against commit:
8c40594d2caf8b5230b364cb76cd8f48fe5ed98a

> `auditoria/ESTADO-ACTUAL.md` es la autoridad documental sobre el estado operativo.
>
> Este handoff archivado conserva la continuidad histórica específica de F2D.1.1. Las afirmaciones operativas de su cuerpo corresponden a su corte original y fueron sucedidas por `auditoria/handoffs/HANDOFF-F2D2.md`.

## Proyecto

FeelingPilates — reestructuración arquitectónica del backend y sus consumers.

## Punto de corte

```text
Última implementación cerrada:
F2C

F2D.1:
DISEÑADA
REVISADA
REQUIERE_AJUSTE

F2D.1.1:
PREPARADA
NO EJECUTADA
```

## Intervención pendiente

`F2D.1.1 — Corrección post-review del diseño de ajustes puntuales de programación`

## Estado

```text
PREPARADA
NO EJECUTADA
```

No considerar sus correcciones como materializadas.

---

# Cadena documental obligatoria

F2D.1.1 depende de tres artefactos concretos:

```text
auditoria/fase-2d1-diseno-ajustes-programacion-fecha.md
                │
                │ revisado por
                ▼
auditoria/reviews/F2D.1-REVIEW-AJUSTES-PUNTUALES.md
                │
                │ produce
                ▼
auditoria/intervenciones/F2D.1.1-CORRECCION-POST-REVIEW.md
```

Estado final de la cadena en este corte:

```text
F2D.1.1:
NO EJECUTADA
```

## Regla obligatoria

Al recuperar el repositorio deben existir físicamente los tres artefactos anteriores.

Si falta cualquiera:

**DETENERSE.**

No reconstruirlo desde:

- memoria;
- otra conversación;
- historial de ChatGPT;
- inferencia;
- un prompt parecido.

La continuidad de F2D.1.1 sólo es válida si los tres artefactos están presentes y coherentes.

---

# Último estado reportado del repositorio

## ADVERTENCIA

Los siguientes valores son:

```text
ÚLTIMO ESTADO REPORTADO
NO VERIFICADO ACTUALMENTE
```

Última branch reportada:

```text
operacion/excepciones-horario-fecha
```

Último HEAD reportado:

```text
8c40594d2caf8b5230b364cb76cd8f48fe5ed98a
```

Último working tree reportado:

```text
?? auditoria/fase-2d1-diseno-ajustes-programacion-fecha.md
```

Último baseline reportado antes de F2D.1:

```text
493/493 PASS
```

No utilizar esos valores como sustituto de un pre-flight real.

---

# Qué fue hecho

1. F2C fue implementada, revisada y cerrada.
2. F2D.1 fue diseñada.
3. Se creó el checkpoint:

   `auditoria/fase-2d1-diseno-ajustes-programacion-fecha.md`
4. F2D.1 fue sometida a review adversarial.
5. El review quedó preservado en:

   `auditoria/reviews/F2D.1-REVIEW-AJUSTES-PUNTUALES.md`
6. El review concluyó:

```text
REQUIERE AJUSTE F2D.1.1

P0: 0
P1: 8
P2: 3
```

7. Se preparó una intervención correctiva autocontenida en:

   `auditoria/intervenciones/F2D.1.1-CORRECCION-POST-REVIEW.md`

---

# Qué NO fue hecho

F2D.1.1 **NO fue ejecutada**.

Por tanto:

- el checkpoint F2D.1 no puede considerarse corregido;
- los 8 P1 no pueden considerarse resueltos;
- F2D.1 no está aprobada;
- no ocurrió re-review;
- no existe commit de cierre F2D.1 confirmado;
- F2D.2 no comenzó;
- no existe V47 F2D confirmada;
- no existe `AjusteProgramacionFecha` implementado;
- no existe `InstructorLock` implementado;
- no existen nuevos multi-locks F2D implementados;
- no existe nuevo resolver efectivo F2D;
- no existen tests F2D;
- no existe API F2D productiva;
- no existe fence de cutover implementado.

---

# Por qué existe F2D.1.1

El review detectó ocho P1:

1. activación productiva nueva sin cutover/fence;
2. target por serie temporalmente ambiguo;
3. falta de Policy A inversa contra ajustes huérfanos;
4. orden incorrecto del resolver efectivo;
5. writers recurrentes fuera del locking necesario;
6. TOCTOU en discovery del lock set;
7. reservas legacy atribuidas heurísticamente;
8. cambios posteriores de especialidad/estado/oferta/actividad capaces de invalidar programación nueva.

El detalle y la evidencia están en:

`auditoria/reviews/F2D.1-REVIEW-AJUSTES-PUNTUALES.md`

Las correcciones operativas preparadas están en:

`auditoria/intervenciones/F2D.1.1-CORRECCION-POST-REVIEW.md`

Este handoff no duplica esas especificaciones.

---

# Documentos que deben leerse

Antes de ejecutar F2D.1.1:

1. `auditoria/ESTADO-ACTUAL.md`
2. este handoff;
3. `auditoria/contexto/MAPA-LEGACY-Y-MIGRACION.md`
4. `auditoria/DECISIONES-ARQUITECTONICAS.md`
5. `auditoria/REGLAS-DE-TRABAJO-IA.md`
6. `auditoria/fase-2d1-diseno-ajustes-programacion-fecha.md`
7. `auditoria/reviews/F2D.1-REVIEW-AJUSTES-PUNTUALES.md`
8. `auditoria/intervenciones/F2D.1.1-CORRECCION-POST-REVIEW.md`

No es necesario consultar conversaciones anteriores.

---

# Primera acción al recuperar el repositorio

**NO ejecutar F2D.1.1 inmediatamente.**

Primero verificar:

```text
repositorio correcto
branch
HEAD
working tree
tracking remoto
checkpoint F2D.1
review F2D.1
intervención F2D.1.1
baseline
```

Comparar el estado real con el último estado reportado.

Si hay discrepancias:

**DETENERSE Y RECONCILIAR.**

---

# Precondición documental

Deben existir físicamente:

```text
auditoria/fase-2d1-diseno-ajustes-programacion-fecha.md
auditoria/reviews/F2D.1-REVIEW-AJUSTES-PUNTUALES.md
auditoria/intervenciones/F2D.1.1-CORRECCION-POST-REVIEW.md
```

Si falta alguno:

```text
STOP
```

No ejecutar una versión reconstruida de F2D.1.1.

---

# Scope de F2D.1.1

La intervención preparada sólo puede modificar:

```text
auditoria/fase-2d1-diseno-ajustes-programacion-fecha.md
```

No debe modificar:

```text
src/main/java
src/test
migraciones
frontend
mobile
```

No debe crear commit antes del re-review.

---

# Stop conditions

Detenerse si:

- branch/HEAD no coinciden razonablemente con el corte esperado;
- working tree contiene cambios adicionales desconocidos;
- falta cualquiera de los tres artefactos F2D;
- el checkpoint fue modificado después del review;
- el baseline falla;
- existe evidencia de que F2D.1.1 ya se ejecutó;
- aparece un commit posterior que altera sustancialmente programación;
- aplicar la corrección exige modificar código;
- aplicar la corrección exige crear migraciones;
- el review ya no corresponde al checkpoint actual.

---

# Condición de salida

Después de ejecutar F2D.1.1:

F2D.1 **todavía no se cierra automáticamente**.

Debe ocurrir un re-review.

Sólo un gate posterior puede cambiar:

```text
F2D.1:
REQUIERE_AJUSTE
```

a:

```text
F2D.1:
DISEÑO_APROBADO
```

Hasta entonces, el estado contractual sigue siendo:

```text
F2D.1:
REQUIERE_AJUSTE

F2D.1.1:
PREPARADA / NO EJECUTADA
```

mientras la intervención no se haya realizado físicamente.
