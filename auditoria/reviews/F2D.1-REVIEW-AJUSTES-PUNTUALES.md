# FeelingPilates — Review adversarial F2D.1

Artifact type: REVIEW
Materialization status: MATERIALIZED
Repository verification: VERIFIED
Historical status: IMMUTABLE
Last verified against commit:
8c40594d2caf8b5230b364cb76cd8f48fe5ed98a
Review result: REQUIERE\_AJUSTE
Target correction: F2D.1.1

## 1. Identidad del review

### Fase revisada

`F2D.1 — Diseño de ajustes puntuales de programación por fecha`

### Checkpoint revisado

`auditoria/fase-2d1-diseno-ajustes-programacion-fecha.md`

### Base reportada durante el review

Branch reportada:

`operacion/excepciones-horario-fecha`

HEAD reportado:

`8c40594d2caf8b5230b364cb76cd8f48fe5ed98a`

Working tree reportado:

```text
?? auditoria/fase-2d1-diseno-ajustes-programacion-fecha.md
```

Estos datos corresponden al estado reportado durante F2D.1 y deben volver a verificarse antes de cualquier intervención futura.

## 2. Objetivo de F2D.1

F2D.1 buscaba diseñar los ajustes de programación para una fecha concreta sin modificar las reglas recurrentes de semanas posteriores.

Casos funcionales estudiados:

- cancelar una ocurrencia recurrente sólo en una fecha;
- reemplazar una ocurrencia;
- agregar programación puramente puntual;
- cambiar instructor;
- cambiar actividad;
- cambiar salón;
- construir una futura programación efectiva a partir de recurrencia + ajustes.

El diseño debía además convivir temporalmente con el modelo legacy existente.

## 3. Metodología del review

El review fue adversarial y de sólo lectura.

Se contrastó el checkpoint F2D.1 contra el repositorio y los consumidores reales conocidos, incluyendo:

- `TurnoInstructor`;
- `TurnoInstructorService`;
- frontend web;
- `Reserva`;
- `ReservaService`;
- `BloqueProgramacion`;
- `Asignacion`;
- `BloqueProgramacionService`;
- migración V41;
- modelo de vigencias;
- locking existente;
- contratos de permisos;
- futura persistencia propuesta;
- riesgos de concurrencia;
- estrategia de coexistencia legacy/nueva.

El objetivo no fue validar que el documento fuera internamente coherente, sino determinar si F2D.2 podía implementarse sin contradicciones de:

- identidad;
- concurrencia;
- activación;
- reservas;
- persistencia;
- cutover.

## 4. Veredicto

**REQUIERE AJUSTE F2D.1.1**

Resultado:

```text
P0: 0
P1: 8
P2: 3
```

El modelo nuevo se consideró justificado, pero F2D.2 no podía implementarse todavía de forma segura con el checkpoint revisado.

---

# 5. Legacy y coexistencia

Se confirmó:

- `TurnoInstructor` sigue vivo.
- Existe controller legacy.
- El frontend web lee y escribe `/turnos-instructor`.
- El frontend crea incluso `EXCEPCION` y `CANCELACION`.
- `ReservaService` sigue dependiendo del universo `TurnoInstructor`.
- `BloqueProgramacion + Asignacion` no constituyen todavía la autoridad productiva de programación.
- `BloqueProgramacionService` es un writer interno de `programacion_*`.
- El uso productivo conocido de `programacion_*` es limitado.
- La existencia histórica de tablas creadas vacías en V41 no permite afirmar que una base actual siga vacía sin auditar datos.

El checkpoint justificaba la coexistencia basándose en que el modelo nuevo no tenía writers públicos productivos.

Sin embargo, el propio alcance propuesto de F2D.2 incluía un controller de escritura.

Eso hacía insegura la coexistencia: una `ADICION` no requiere una serie recurrente previa y podría duplicar inmediatamente programación legacy.

Conclusión del review:

`programacion_*` puede evolucionar de forma segura sólo si F2D.2 permanece aislada de la autoridad productiva mientras no exista cutover.

---

# 6. Modelo elegido

Se mantuvo como válido el modelo conceptual:

- entidad nueva para ajustes;
- construida sobre `BloqueProgramacion + Asignacion`;
- sin reutilizar `TurnoInstructor` como mecanismo de ajustes;
- tipos:
  - `CANCELACION`;
  - `REEMPLAZO`;
  - `ADICION`;
- `REEMPLAZO` representado como una sola fila;
- sin flags combinables.

Razones:

- legacy no puede identificar una ocurrencia recurrente individual de forma inequívoca;
- `TurnoInstructor.CANCELACION` representa un marcador mucho más amplio;
- `TurnoInstructor.EXCEPCION` no tiene semántica suficiente de reemplazo;
- el legacy permite multiplicidades incompatibles con el modelo nuevo.

La creación de un modelo nuevo no fue considerada el problema.

Los problemas estaban en cómo activarlo y protegerlo.

---

# 7. Target `serieId + fecha`

El diseño utilizaba:

```text
(asignacionSerieId, fecha)
```

como identidad del target para `CANCELACION` y `REEMPLAZO`.

El review consideró que puede mantenerse sólo si representa una **ocurrencia nominal** y si existe exactamente una versión aplicable de la serie en la fecha.

Debe poder determinarse inequívocamente una asignación que cumpla:

- asignación activa;
- bloque activo;
- vigencia de asignación contiene `D`;
- vigencia de bloque contiene `D`;
- `bloque.diaSemana == dayOfWeek(D)`;
- bloque y asignación coherentes;
- misma serie lógica.

Problema detectado:

`programacion_asignacion.serie_id` no estaba protegido por una exclusión temporal equivalente a la ya utilizada en horarios.

Podían existir:

- cero versiones;
- una versión;
- varias versiones aplicables a la misma fecha.

El diseño F2D dependía directamente de una identidad que todavía podía ser ambigua.

---

# 8. Vigencias de serie

El review determinó que no era suficiente dejar ese problema como deuda de una fase anterior.

La futura implementación de F2D depende de que una serie tenga como máximo una versión aplicable a una fecha.

Se concluyó que el diseño debía incorporar una protección temporal PostgreSQL equivalente conceptualmente a:

```text
serie_id WITH =
daterange(vigente_desde, vigente_hasta, '[]') WITH &&
```

para filas activas.

El review no autorizó crear la migración en ese momento.

---

# 9. Orfandad de ajustes

Se detectó una ausencia de política inversa.

Caso:

```text
Serie S:
vigente hasta 30/09

Ajuste:
S + 25/09
```

Posteriormente un writer cambia la vigencia:

```text
Serie S:
vigente hasta 20/09
```

El ajuste del 25/09 queda sin ocurrencia recurrente objetivo.

El checkpoint no definía cómo impedir o resolver ese estado.

El review exigió que el diseño especificara una política fuerte:

- no reasignar heurísticamente;
- no cancelar automáticamente;
- no ignorar silenciosamente;
- impedir que writers recurrentes dejen ajustes futuros sin target válido;
- considerar 0 o múltiples targets como ruptura de invariante.

---

# 10. Semántica de versionado de serie

El review conservó la interpretación:

- `serieId` representa una regla lógica recurrente;
- una nueva versión puede conservar el mismo `serieId`;
- un ajuste futuro sobre esa serie sigue a la versión nominal aplicable en la fecha;
- un `REEMPLAZO`, una vez definido, conserva su resultado explícito;
- una regla lógicamente distinta debe utilizar otro `serieId`.

---

# 11. Programación efectiva

El orden propuesto por F2D.1 era problemático.

El checkpoint filtraba las ocurrencias recurrentes incompatibles con horario operativo antes de aplicar determinados ajustes.

Caso crítico:

```text
Original:
Juriquilla
08:00–10:00

Juriquilla:
CERRADO

REEMPLAZO:
Cimatario
09:00–11:00

Cimatario:
ABIERTO
```

Si la nominal se elimina primero porque Juriquilla está cerrado, el reemplazo pierde su target.

El review determinó que el diseño debía distinguir:

```text
OCURRENCIA NOMINAL
```

de:

```text
OCURRENCIA EFECTIVA
```

y revisar el orden de composición.

---

# 12. Orden nominal → ajustes → operación final

El review exigió cerrar una secuencia donde:

1. se obtengan primero las ocurrencias nominales aplicables a `D`;
2. se resuelvan targets por serie;
3. se apliquen cancelaciones y reemplazos;
4. se incorporen adiciones;
5. cada resultado se evalúe contra el horario efectivo del salón final;
6. se validen invariantes globales;
7. finalmente se filtre/ordene la consulta.

Casos obligatorios señalados por el review:

### Origen cerrado, destino abierto

Debe ser posible que un reemplazo explícito traslade una ocurrencia desde un salón cerrado hacia otro salón operativo.

### Recurrente original fuera, reemplazo válido dentro

Ejemplo:

```text
recurrente:
08:00–12:00

horario efectivo:
10:00–16:00

reemplazo explícito:
10:00–12:00
```

El reemplazo puede ser válido porque no constituye recorte automático del recurrente.

---

# 13. Cancelación

El review mantuvo que una cancelación:

- debe referirse a una ocurrencia nominal;
- afecta sólo una fecha;
- no debe cancelar todo el día del instructor;
- no necesita producir un rango resultado;
- puede conservar intención incluso si la ocurrencia original quedaría omitida operativamente.

Si la nominal target no existe, no debe ignorarse silenciosamente.

---

# 14. Reemplazo

El review mantuvo:

- una fila;
- target por serie + fecha;
- snapshot completo del resultado;
- salón final;
- instructor final;
- actividad final;
- hora inicial;
- hora final.

El reemplazo conserva la identidad lógica de la ocurrencia recurrente objetivo.

---

# 15. Adición

Una adición:

- no necesita serie;
- tiene identidad propia;
- representa una ocurrencia puramente puntual;
- debe respetar horario, especialidad, estado y solapamientos.

El review señaló además que no debe convertirse en un writer productivo mientras no exista una estrategia de autoridad/cutover.

---

# 16. Reservas

Se identificó un problema estructural.

`Reserva` contiene información funcional como:

- salón;
- instructor;
- actividad;
- fecha;
- intervalo.

Pero no tiene referencia a:

- `Asignacion`;
- serie;
- ajuste;
- futura ocurrencia nueva.

Las reservas actuales nacen a partir de la lógica legacy de `TurnoInstructor`.

Por ello el criterio propuesto:

```text
cubierta antes
y
no cubierta después
```

no establece identidad causal.

Podría atribuir una reserva legacy a una ocurrencia nueva simplemente porque sus campos coinciden.

El review determinó que F2D.1 debía corregir esa mezcla de fuentes.

Antes de un futuro cutover, las reservas necesitarán una asociación inequívoca con la nueva fuente o una migración que pueda demostrarla.

---

# 17. `InstructorLock`

El diseño propuso correctamente que `SalonLock` no basta para el invariante global de instructor.

Caso:

```text
Tx A:
Ariadna
Juriquilla
09:00–11:00

Tx B:
Ariadna
Cimatario
10:00–12:00
```

Los dos writers pueden tomar locks de salones distintos.

Por tanto, ambos pueden validar simultáneamente si no existe una dimensión de lock compartida por instructor.

El review consideró necesario un lock compatible sobre instructor.

El problema no fue la idea del lock sino su participación.

---

# 18. Writers de `Asignacion`

P1 directo.

`BloqueProgramacionService.crearAsignacion` no participaba en el protocolo propuesto de:

- `SalonLock`;
- `InstructorLock`.

Si un writer de ajuste toma `InstructorLock`, pero un writer recurrente que modifica la misma programación no lo hace, la carrera permanece.

Por ello F2D.2 no podía excluir el hardening de los writers recurrentes que compiten por esos invariantes.

---

# 19. Multi-lock / ordering

Un ajuste puede involucrar:

- salón origen;
- salón destino;
- instructor original;
- instructor nuevo.

El review exigió adquisición determinista.

La propuesta revisada debía contemplar:

```text
salones ordenados
→ instructores ordenados
```

y no depender del orden del request.

También debía soportar operaciones cross-salon sin deadlocks por adquisición inversa.

---

# 20. Discovery TOCTOU

Problema detectado:

para conocer los locks necesarios se necesita conocer primero:

- salón origen;
- instructor original.

Pero esos valores provienen del target recurrente.

Si se leen antes de los locks y cambian después:

```text
Tx A:
lee Juriquilla / Ariadna

Tx B:
versiona a Cimatario / Alberto

Tx A:
continúa con locks de Juriquilla / Ariadna
```

el lock set queda obsoleto.

La propuesta inicial no cerraba esta carrera.

El diseño requería:

- discovery preliminar;
- adquisición determinista;
- relectura bajo locks;
- detección de cambio en el lock set;
- abortar/reintentar si el target ya no coincide;
- y que los writers recurrentes participaran del mismo protocolo.

---

# 21. Persistencia F2D / V47 propuesta

El review consideró que la propuesta original no podía implementarse literalmente.

Hallazgos:

- faltaba protección temporal de `programacion_asignacion` por serie;
- no debía existir una FK simple hacia `serie_id` si éste no es único;
- una columna `salon_id` con significados diferentes entre cancelación y reemplazo era ambigua;
- cancelación no necesita persistir resultado;
- reemplazo/adición necesitan snapshot completo.

Se planteó que el diseño corregido diferenciara claramente datos de target y datos de resultado.

No se creó ninguna migración durante el review.

---

# 22. API

El API preliminar de F2D.1 era demasiado genérico y no cerraba correctamente idempotencia, especialmente para adiciones.

El review propuso que el diseño futuro diferenciara recursos naturales para:

- ajuste sobre una ocurrencia recurrente;
- adición puntual con identidad propia.

Sin embargo, el problema principal era anterior:

F2D.2 no debía exponer writers productivos mientras el legacy siguiera siendo autoridad sin cutover.

---

# 23. Seguridad y permisos

El review detectó que la matriz propuesta ampliaba demasiado el significado de `calendario.editar`.

Cambiar:

- salón;
- instructor;
- actividad;

es una operación más fuerte que editar únicamente el intervalo de un elemento existente.

Los permisos debían distinguir mejor:

- lectura;
- edición acotada;
- cancelación;
- gestión completa.

En movimientos cross-salon se requiere autorización sobre ambos contextos antes del locking.

---

# 24. Identidad efectiva

El review consideró coherente distinguir:

### Recurrente / reemplazo

Identidad lógica:

```text
serieId + fecha
```

### Adición

Identidad propia del ajuste + fecha.

Esto debía quedar cerrado en el diseño corregido, no implementado todavía.

---

# 25. Rollout

El checkpoint original no impedía de forma suficientemente fuerte que la nueva programación se hiciera productiva antes de retirar/controlar el legacy.

El review exigió una estrategia explícita de activación.

La conclusión fundamental fue:

> no debe existir una fase donde legacy y nueva programación sean dos autoridades productivas escribibles para el mismo salón.

Los detalles debían incorporarse en F2D.1.1 y volver a revisión antes de ser considerados aceptados.

---

# 26. Tests adicionales requeridos por el review

El review señaló que el futuro plan de F2D debería cubrir explícitamente, entre otros:

- reemplazo con origen cerrado y destino abierto;
- reemplazo de original fuera hacia resultado válido;
- target con cero versiones;
- protección frente a dos versiones solapadas;
- cambio de vigencia que dejaría ajuste huérfano;
- writer recurrente concurrente contra writer puntual;
- discovery stale con cambio de salón/instructor;
- reemplazos entrantes/salientes en consultas por salón;
- comportamiento respecto a reservas legacy;
- cambios posteriores de especialidad/estado;
- duplicados efectivos;
- orden multi-salón/multi-instructor.

---

# 27. Matriz de mutaciones del review

| #MutaciónEstado en F2D.1Evidencia |                                                        |              |                                                                        |
| --------------------------------- | ------------------------------------------------------ | ------------ | ---------------------------------------------------------------------- |
| A                                 | Omitir antes del reemplazo y perderlo                  | PARCIAL      | Se afirmaba que reaparecía, pero el orden filtraba primero la nominal. |
| B                                 | Ajuste toma `InstructorLock`, Asignacion no            | NO DETECTADO | Writer recurrente no participaba del lock.                             |
| C                                 | Serie target queda sin versión                         | NO DETECTADO | No existía Policy A inversa.                                           |
| D                                 | Dos versiones cubren D                                 | PARCIAL      | Sólo detección runtime; sin constraint DB propuesta en F2D.            |
| E                                 | Target cambia durante discovery                        | NO DETECTADO | Lock set dependía de lectura no estabilizada.                          |
| F                                 | Reserva legacy bloquea ajuste no relacionado           | NO DETECTADO | Atribución por tupla, sin identidad causal.                            |
| G                                 | Reserva relacionada no bloquea ajuste destructivo      | PARCIAL      | La cobertura funcional detectaba algunos casos, no identidad.          |
| H                                 | Dos ocurrencias efectivas idénticas                    | NO DETECTADO | Writers recurrentes podían competir sin protocolo suficiente.          |
| I                                 | Reemplazo destino abierto se pierde por origen cerrado | PARCIAL      | El orden del resolver podía eliminar la nominal primero.               |
| J                                 | Ajuste queda inválido por especialidad                 | NO DETECTADO | Resolver no protegía cambios posteriores.                              |
| K                                 | V47 intenta FK inválida a serie no unique              | DETECTADO    | La propuesta evitaba esa FK.                                           |
| L                                 | Dos reemplazos activos mismo target                    | DETECTADO    | Índice único parcial propuesto.                                        |
| M                                 | Swap cross-salon deadlock                              | DETECTADO    | Se había propuesto orden determinista.                                 |
| N                                 | Consumer mezcla legacy y nueva fuente                  | NO DETECTADO | Separación de imports no implica separación productiva.                |
| O                                 | Ajuste se vuelve productivo antes del cutover          | NO DETECTADO | F2D.2 proponía controller público.                                     |

---

# 28. P0

**NINGUNO**

---

# 29. P1

## P1-1 — Activación productiva sin cutover ni fence por salón

El diseño permitía introducir writers/API nuevos mientras `TurnoInstructor` seguía siendo autoridad productiva.

## P1-2 — Target por serie ambiguo sin constraint de vigencias

`serieId + fecha` no era suficientemente inequívoco con la persistencia actual.

## P1-3 — Ausencia de Policy A inversa para ajustes huérfanos

Writers recurrentes podían eliminar la ocurrencia target de ajustes futuros.

## P1-4 — Resolver incorrectamente ordenado y con scoping incompleto

La nominal podía desaparecer antes de un reemplazo válido.

## P1-5 — `BloqueProgramacionService` no participa en el locking requerido

Un lock unilateral en el writer nuevo no protege contra writers recurrentes.

## P1-6 — Discovery de locks con TOCTOU no resuelto

El lock set podía derivarse de una lectura obsoleta.

## P1-7 — Reservas legacy atribuidas heurísticamente a una fuente distinta

La coincidencia funcional no establece relación causal con programación nueva.

## P1-8 — Cambios posteriores de especialidad/rol/estatus/oferta/actividad

Podían dejar programación nueva futura inválida y todavía visible.

---

# 30. P2

## P2-1 — API e idempotencia incompletas

Especialmente para adiciones puntuales.

## P2-2 — Matriz de permisos demasiado amplia

`calendario.editar` no debía convertirse en permiso general de modificación estructural.

## P2-3 — Multi-lock y orden determinista insuficientemente especificados

La idea existía, pero debía cerrarse de manera operable.

---

# 31. Decisiones finales del review

El review mantuvo como dirección para la corrección:

- entidad de ajuste nueva: justificada;
- target sobre serie/fecha nominal;
- recurrencias y ajustes deben distinguir nominal de efectivo;
- la unicidad temporal de serie debe reforzarse;
- los writers recurrentes deben participar del locking relevante;
- el lock de instructor debe ser compartido por todos los writers competidores;
- no debe atribuirse una reserva legacy a programación nueva por heurística;
- el diseño necesita una estrategia explícita de cutover antes de permitir writers productivos nuevos.

Estas conclusiones constituyen **requisitos para corregir F2D.1**, no evidencia de que ya estén implementados.

---

# 32. Alcance esperado de la corrección

El resultado del review fue:

```text
CREAR F2D.1.1
```

F2D.1 no debía cerrarse.

F2D.2 no debía iniciarse con el checkpoint existente.

---

# 33. Condición de cierre

Para convertir F2D.1 en `DISEÑO_APROBADO` era necesario:

1. ejecutar la intervención documental F2D.1.1;
2. corregir el checkpoint;
3. resolver documentalmente todos los P1;
4. someter el documento corregido a re-review;
5. obtener un gate sin P0/P1 bloqueantes;
6. sólo entonces crear el checkpoint/commit de cierre correspondiente.

---

# 34. Veredicto final

```text
REQUIERE AJUSTE F2D.1.1

P0: 0
P1: 8
P2: 3
```

Al momento de materializar este artefacto lógico:

```text
F2D.1:
REQUIERE_AJUSTE

F2D.1.1:
PREPARADA
NO EJECUTADA
```
