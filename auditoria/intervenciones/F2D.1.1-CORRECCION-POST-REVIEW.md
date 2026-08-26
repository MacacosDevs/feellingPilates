# FeelingPilates — F2D.1.1

## Corrección post-review del diseño de ajustes puntuales de programación

Artifact type: INTERVENTION
Materialization status: MATERIALIZED
Execution status: PREPARADA
Repository verification: VERIFIED
Last verified against commit:
8c40594d2caf8b5230b364cb76cd8f48fe5ed98a

## Estado

```text
Intervención:
F2D.1.1

Estado:
PREPARADA
NO EJECUTADA
```

Este documento conserva la intervención operativa preparada después del review adversarial F2D.1.

No demuestra que sus instrucciones hayan sido aplicadas.

---

# Identidad de la intervención

## Objetivo

Corregir exclusivamente el checkpoint de diseño F2D.1 conforme al review que produjo:

```text
P0: 0
P1: 8
P2: 3
```

Review asociado:

`auditoria/reviews/F2D.1-REVIEW-AJUSTES-PUNTUALES.md`

Checkpoint objetivo:

`auditoria/fase-2d1-diseno-ajustes-programacion-fecha.md`

---

# Repositorio esperado conocido

Los siguientes datos corresponden al último estado reportado y **NO sustituyen pre-flight**.

Última ruta local verificada durante 3B.0:

```text
/Users/jesusaldaircruzortiz/Desktop/Feelingpilates/feelingpilates
```

Branch esperada:

```text
operacion/excepciones-horario-fecha
```

HEAD esperado:

```text
8c40594d2caf8b5230b364cb76cd8f48fe5ed98a
```

Working tree esperado:

```text
?? auditoria/fase-2d1-diseno-ajustes-programacion-fecha.md
```

Si el estado real difiere materialmente:

**DETENERSE.**

---

# Reglas absolutas

Esta intervención es EXCLUSIVAMENTE DE DISEÑO.

NO implementar código productivo.

NO modificar tests.

NO crear migraciones.

NO crear controllers.

NO modificar frontend.

NO modificar mobile.

NO hacer commit.

Únicamente actualizar:

```text
auditoria/fase-2d1-diseno-ajustes-programacion-fecha.md
```

para cerrar todos los P1/P2 del review Codex.

---

# Review autoritativo

Veredicto:

```text
REQUIERE AJUSTE F2D.1.1
```

P0:

```text
NINGUNO
```

P1:

1. Activación productiva sin cutover/fence por salón.
2. Target por serie ambiguo sin constraint de vigencias.
3. Ausencia de Policy A inversa para ajustes huérfanos.
4. Orden incorrecto de `ProgramacionEfectiva`.
5. `BloqueProgramacionService` no participa de `SalonLock`/`InstructorLock`.
6. Discovery de locks con TOCTOU no resuelto.
7. Reservas legacy atribuidas heurísticamente a fuente nueva.
8. Cambios posteriores de especialidad/rol/estatus/oferta/actividad pueden invalidar programación nueva.

P2:

1. API/idempotencia incompleta.
2. Permisos demasiado amplios.
3. Multi-lock/ordering insuficientemente especificado.

Actualizar TODO el checkpoint para que exista una sola especificación vigente.

---

# 1. Decisión de rollout — dark launch

Cerrar explícitamente:

```text
F2D.2 es DARK LAUNCH.
```

Incluye:

- migración;
- entidades;
- constraints;
- resolver;
- writers internos;
- locks;
- hardening de writers recurrentes;
- tests.

NO incluye:

- controllers públicos;
- consumidores productivos nuevos;
- frontend;
- mobile;
- migración de reservas;
- activación por salón;
- modificación de `TurnoInstructor`.

Durante F2D.2:

```text
TurnoInstructor sigue siendo la fuente productiva legacy.
```

`programacion_*` permanece aislado de consumidores productivos.

No debe existir flujo donde ambos modelos sean escribibles/productivos para el mismo salón.

---

# 2. No controllers en F2D.2

Eliminar del alcance F2D.2 cualquier controller/API pública nueva.

Los contratos REST pueden permanecer como DISEÑO para la futura fase de activación, pero deben marcarse claramente:

```text
NO IMPLEMENTAR EN F2D.2
```

Esto elimina el riesgo de una `ADICION` productiva duplicando `TurnoInstructor` legacy.

---

# 3. Cutover por salón

Diseñar rollout futuro explícito:

estado conceptual persistido por salón:

```text
LEGACY
MIGRANDO
NUEVA
```

NO implementar todavía.

Regla:

### LEGACY

Sólo writers/consumers legacy.

### MIGRANDO

Writers externos detenidos.

Migración/auditoría interna.

### NUEVA

Sólo programación nueva.

Nunca:

```text
legacy + nueva
```

productivos simultáneamente para el mismo salón.

Documentar que el fence se implementará en una fase posterior, NO en F2D.2.

---

# 4. Auditoría de datos

Corregir la afirmación:

V41 creó tablas vacías originalmente, pero NO se puede asumir que sigan vacías en una base existente.

Antes de activar un salón:

auditar datos reales de:

- `programacion_bloque`;
- `programacion_asignacion`;
- `TurnoInstructor`;
- reservas.

Esto pertenece al rollout posterior.

---

# 5. Modelo nuevo

Mantener:

```text
AjusteProgramacionFecha
```

sobre:

```text
BloqueProgramacion
+
Asignacion
```

NO reutilizar `TurnoInstructor`.

Tipos:

```text
CANCELACION
REEMPLAZO
ADICION
```

`REEMPLAZO`:

UNA fila.

No flags combinables.

---

# 6. Target nominal

Cerrar definición exacta:

target de `CANCELACION`/`REEMPLAZO`:

```text
(asignacionSerieId, fecha)
```

sobre una **OCURRENCIA NOMINAL**.

Para fecha D debe existir EXACTAMENTE UNA versión nominal que cumpla:

- asignación activa;
- bloque activo;
- vigencia `Asignacion` contiene D;
- vigencia `Bloque` contiene D;
- `bloque.diaSemana == dayOfWeek(D)`;
- relaciones coherentes;
- `serieId` correspondiente.

0:

invariante/orfandad según contexto.

> 1:

invariante rota.

No resolver target a partir de una ocurrencia ya filtrada por horario operativo.

---

# 7. Semántica de `serieId`

Cerrar:

`serieId` representa una REGLA LÓGICA recurrente versionable.

Si una nueva versión conserva `serieId`:

ajustes futuros siguen esa serie y se aplican a la nueva versión nominal correspondiente.

`CANCELACION`:

cancela la ocurrencia de esa serie/fecha.

`REEMPLAZO`:

conserva la misma identidad efectiva serie/fecha, pero su snapshot final NO cambia aunque después cambie la asignación base.

Si el negocio crea otra regla lógica:

nuevo `serieId`.

---

# 8. EXCLUDE de vigencias

F2D depende de unicidad temporal por serie.

Por tanto, la futura V47 propuesta DEBE incluir hardening de `programacion_asignacion`.

`btree_gist`:

reutilizar extensión existente; no duplicarla innecesariamente.

EXCLUDE conceptual:

```text
serie_id WITH =,
daterange(vigente_desde, vigente_hasta, '[]') WITH &&
```

con predicado:

```text
WHERE activo
```

Usar sintaxis PostgreSQL exacta compatible con columnas nullable de vigencia.

Debe garantizar:

máximo una versión activa de una serie aplicable a una fecha.

No relegar a F1C.

---

# 9. Sin FK a `serieId`

Mantener:

NO FK directa desde ajuste a `programacion_asignacion.serie_id`.

Razón:

`serie_id` no es una PK/UNIQUE simple porque existen múltiples versiones de la serie.

La integridad se garantiza mediante:

- EXCLUDE temporal;
- Policy A inversa;
- resolver fail-fast.

No inventar FK inválida.

---

# 10. Policy A inversa — obligatoria

Todo writer que pueda alterar la existencia nominal futura de una serie debe impedir dejar ajustes activos huérfanos.

Aplica como mínimo a cambios sobre:

## `Asignacion`

- activo;
- vigencia;
- `serieId`;
- bloque asociado;
- datos que cambien identidad lógica.

## `BloqueProgramacion`

- activo;
- vigencia;
- `diaSemana`;
- salón/rango cuando afecte la ocurrencia.

Regla:

si después del cambio un ajuste activo de hoy/futuro tendría:

```text
0 targets nominales
```

o:

```text
>1 target
```

→ RECHAZAR antes de persistir.

No:

- cancelar ajuste automáticamente;
- reasignarlo;
- ignorarlo.

Salida administrativa:

retirar/modificar primero el ajuste.

---

# 11. Resolver ante orfandad

`ProgramacionEfectiva`:

si encuentra ajuste activo `CANCELACION`/`REEMPLAZO` con 0 targets nominales:

```text
INVARIANTE ROTA
```

No omitir silenciosamente.

Si >1:

```text
INVARIANTE ROTA
```

Elegir excepción/invariante técnica concreta coherente con el proyecto.

No inventar resultado parcial.

---

# 12. Ocurrencia nominal vs efectiva

Introducir formalmente dos conceptos.

## OCURRENCIA NOMINAL

Resultado de reglas recurrentes aplicables por:

- serie;
- vigencia;
- día.

SIN considerar todavía:

- `HorarioEfectivoSalon`;
- ajustes.

## OCURRENCIA EFECTIVA

Resultado después de:

- ajustes;
- horario operativo final;
- validaciones defensivas.

---

# 13. Algoritmo `ProgramacionEfectiva`

Reemplazar completamente el orden anterior.

Orden definitivo propuesto para el checkpoint corregido:

1. cargar todas las ocurrencias NOMINALES aplicables a D;
2. indexarlas inequívocamente por `serieId`;
3. cargar ajustes activos aplicables a D;
4. validar targets `CANCELACION`/`REEMPLAZO`;
5. aplicar CANCELACIONES sobre nominales;
6. aplicar REEMPLAZOS sobre nominales;
7. incorporar ADICIONES;
8. para cada resultado candidato, consultar/usar `HorarioEfectivoSalon` de SU SALÓN FINAL;
9. omitir candidatos incompatibles con operación;
10. aplicar validaciones defensivas de entidades/estado/especialidad;
11. validar invariantes globales de solapamiento;
12. filtrar por salón/instructor solicitado;
13. ordenar determinísticamente.

No filtrar nominales por horario antes de ajustes.

---

# 14. Reemplazo rescata nominal

Cerrar casos:

## Caso A

Original:

```text
Juriquilla
08:00–10:00
```

Juriquilla D:

```text
CERRADO
```

REEMPLAZO:

```text
Cimatario
09:00–11:00
```

Cimatario:

```text
ABIERTO
```

Resultado:

```text
APARECE
```

## Caso B

Original:

```text
08:00–12:00
```

Horario final original:

```text
10:00–16:00
```

Sin ajuste:

```text
OMITIDO
```

REEMPLAZO explícito:

```text
10:00–12:00
```

Resultado:

```text
APARECE
```

Esto NO es recorte automático.

Es decisión administrativa explícita.

---

# 15. Cancelación de nominal ya inoperativa

Si la nominal ya sería omitida por horario operativo:

`CANCELACION` sigue siendo un ajuste válido.

Resultado efectivo:

omitido.

La cancelación conserva intención administrativa.

No requiere que el salón esté operativo.

Pero el target nominal debe existir.

---

# 16. Reemplazo

Una fila con snapshot FINAL completo:

```text
salonResultadoId
instructorResultadoId
tipoActividadResultadoId
horaInicioResultado
horaFinResultado
```

Target:

```text
serieId + fecha
```

Identidad efectiva:

```text
(SERIE_ASIGNACION, serieId, fecha)
```

El snapshot no se reinterpreta si después cambia la asignación base.

---

# 17. Adición

Sin target de serie.

Identidad:

```text
(AJUSTE, ajusteId, fecha)
```

Snapshot completo:

- salón;
- instructor;
- actividad;
- inicio;
- fin.

Writer interno valida al crear/editar:

- salón operativo;
- rango dentro de `HorarioEfectivoSalon`;
- entidades activas;
- especialidad;
- reglas correspondientes;
- no overlap instructor;
- demás invariantes cerradas.

Resolver:

fail-closed ante cambios posteriores.

No deduplicar silenciosamente.

---

# 18. Duplicados efectivos

Cerrar:

si una `ADICION` genera exactamente la misma ocurrencia efectiva que otra ocurrencia:

NO deduplicar silenciosamente.

Debe considerarse conflicto/invariante según writer.

Definir igualdad operativa mínima:

- salón;
- instructor;
- actividad;
- fecha;
- inicio;
- fin.

Writer debe rechazar duplicado activo equivalente.

Resolver defensivo:

preferencia del diseño preparado:

```text
INVARIANTE ROTA
```

no deduplicación heurística.

---

# 19. Horario operativo final

Sólo validar contra:

`HorarioEfectivoSalon`

del salón RESULTANTE.

No importa que el salón origen esté cerrado si `REEMPLAZO` mueve la ocurrencia.

`ADICION` siempre usa salón resultado.

`CANCELACION` no necesita horario operativo.

---

# 20. Reservas legacy — fuera de F2D.2

Eliminar completamente de F2D.2:

```text
ImpactoReservasEnAjusteProgramacion
```

Durante dark launch:

`Reserva` legacy pertenece causalmente a `TurnoInstructor`.

No debe bloquear:

`AjusteProgramacionFecha`.

No usar heurística:

```text
covered before / not after
```

No intentar atribuir una `Reserva` legacy a una ocurrencia nueva por coincidencia de:

- salón;
- instructor;
- actividad;
- fecha;
- horas.

---

# 21. Reservas después de cutover

Documentar para fase futura:

antes de activar un salón en `NUEVA`:

`Reserva` debe obtener referencia explícita a:

- fuente/ocurrencia nueva;

o migración inequívoca.

Una reserva sin mapeo único:

```text
BLOQUEA CUTOVER
```

Después del cutover:

- CANCELACION con reservas asociadas → bloquea;
- cambio instructor → bloquea;
- cambio actividad → bloquea;
- cambio salón → bloquea;
- cambio de horario → bloquea si la reserva deja de quedar contenida.

No implementar ahora.

---

# 22. `InstructorLock`

Mantener nuevo concepto en el diseño corregido.

Implementación conceptual:

```text
PESSIMISTIC_WRITE sobre Usuario instructor
```

`Propagation.MANDATORY`.

No advisory locks.

Helper conceptual:

```text
InstructorLocks.adquirirOrdenados(Collection<UUID>)
```

Instructor inexistente:

404 según contexto.

---

# 23. `SalonLocks` multi

El `SalonLock` actual de un elemento no basta para movimientos.

Diseñar helper:

```text
SalonLocks.adquirirOrdenados(Collection<UUID> salonIds)
```

que:

- deduplica;
- ordena UUID ascendente;
- adquiere siempre en ese orden.

No depender del orden recibido.

Puede reutilizar internamente el `SalonLock` existente o su repositorio.

---

# 24. Orden global de locks

Única regla:

```text
SALONES ordenados ascendente
→
INSTRUCTORES ordenados ascendente
```

Nunca:

```text
Instructor → Salon
```

Todos los writers nuevos/recurrentes que puedan tocar ambos deben seguirlo.

---

# 25. Lock set por tipo

## CANCELACION

Salones:

```text
origen nominal
```

Instructores:

```text
original nominal
```

## ADICION

Salones:

```text
resultado
```

Instructores:

```text
resultado
```

## REEMPLAZO

Salones:

```text
origen nominal + resultado
```

Instructores:

```text
original nominal + resultado
```

Deduplicar IDs antes de adquirir.

---

# 26. Writers recurrentes — en scope F2D.2

Corregir alcance.

F2D.2 DEBE endurecer `BloqueProgramacionService` / writers de `Asignacion` necesarios.

Especialmente:

```text
crearAsignacion
```

y cualquier writer que cambie:

- instructor;
- salón;
- actividad;
- rango;
- vigencia;
- serie;
- activo;
- bloque/día.

Deben participar en:

- `SalonLocks`;
- `InstructorLocks`;
- Policy A inversa.

No basta que sólo `AjusteProgramacionFecha` tome `InstructorLock`.

---

# 27. Concurrencia cross-salon

Invariante:

mismo instructor no puede solaparse:

- en mismo salón;
- entre salones.

Writers recurrentes y puntuales deben compartir `InstructorLock`.

Adyacencia:

```text
08:00–10:00
10:00–12:00
```

permitida.

Overlap real:

rechazado.

---

# 28. Discovery / TOCTOU

Cerrar protocolo exacto para `CANCELACION`/`REEMPLAZO`.

1. Validación sintáctica y temporal pura.
2. Autorizar salón contextual del path y salón destino declarado si existe.
3. Lectura preliminar mínima target `serieId+fecha`.
4. Verificar que salón origen nominal coincide con `salonId` contextual.
   Si no:
   `404`.
5. Derivar lock set:
   - salones origen/destino;
   - instructores original/nuevo.
6. Adquirir `SalonLocks` ordenados.
7. Adquirir `InstructorLocks` ordenados.
8. RELEER target nominal bajo locks.
9. Comparar recursos relevantes con discovery:
   - origen;
   - instructor;
   - serie/version aplicable.
10. Si lock set cambió:
    abortar con conflicto reintentable.
11. Validar sobre relectura.
12. Persistir.

No seguir con un lock set obsoleto.

---

# 29. Writer recurrente y discovery

Writers de `Asignacion` deben participar de los mismos locks para que el paso anterior sea efectivo.

La relectura sola NO cierra la carrera si el otro writer no toma locks.

Documentarlo explícitamente.

---

# 30. Cambio de asignación

Si writer recurrente cambia:

```text
Ariadna → Alberto
```

debe lockear ambos instructores ordenados.

Si cambia:

```text
Juriquilla → Cimatario
```

debe lockear ambos salones ordenados.

Si cambia ambos:

unión completa.

Aplicar Policy A inversa antes de persistir.

---

# 31. Hardening de Asignacion

Definir alcance mínimo F2D.2:

- constraints de vigencia por serie;
- locks;
- overlap global instructor;
- Policy A inversa;
- queries necesarias;
- tests.

NO crear controllers si no existen.

No convertir toda Programación en módulo productivo todavía.

---

# 32. Especialidad / rol / estatus / actividad

El review encontró que un ajuste válido puede quedar inválido por cambios posteriores.

Cerrar política defensiva.

## Resolver

`ProgramacionEfectiva` debe validar cada resultado actual contra estado vigente:

- instructor activo;
- actividad activa;
- salón activo según modelo real;
- especialidad compatible;
- cualquier relación de oferta requerida por modelo real.

Si deja de ser válida:

```text
OMITIR FAIL-CLOSED
```

y registrar/elevar señal técnica según patrón existente.

No mostrar programación inválida.

---

# 33. Policy A inversa en datos maestros

Distinguir dos fases.

F2D.2:

si existe writer fácilmente identificable y acotado para:

- especialidades;
- estatus;
- actividad;

evaluar añadir validación inversa sólo si no amplía desproporcionadamente scope.

Si cerrarlo requiere refactor amplio:

documentar como precondición de activación.

PERO el resolver DEBE ser fail-closed desde F2D.2.

No permitir que programación inválida quede visible.

Cerrar exactamente qué writers se endurecen ahora y cuáles quedan como fence de activación futuro.

No dejar:

```text
puede hacerse
```

---

# 34. Propuesta preparada para el punto anterior

Para mantener scope controlado:

## F2D.2

- resolver fail-closed obligatorio;
- hardening completo de Bloque/Asignacion;
- NO tocar módulos maestros adicionales salvo query mínima.

## Antes de activar salón `NUEVA`

auditoría/revalidación obligatoria.

## Después

una fase específica puede añadir Policy A inversa a maestros si sigue siendo necesario.

Adoptar esta opción como final en el checkpoint corregido salvo que el repositorio demuestre una contradicción.

---

# 35. Persistencia V47 — dos responsabilidades

La futura V47 propuesta puede contener:

### A

hardening `programacion_asignacion`.

### B

`programacion_ajuste_fecha`.

Confirmar orden DDL.

NO crear la migración durante F2D.1.1.

---

# 36. DDL `programacion_asignacion`

Proponer EXCLUDE exacto en el diseño.

Si `btree_gist` ya existe por migración previa:

no volver a crear extensión innecesariamente salvo `IF NOT EXISTS`.

Constraint con nombre estable.

Ejemplo conceptual:

```sql
ALTER TABLE programacion_asignacion
ADD CONSTRAINT ex_programacion_asignacion_serie_vigencia
EXCLUDE USING gist (
  serie_id WITH =,
  daterange(vigente_desde, vigente_hasta, '[]') WITH &&
)
WHERE (activo);
```

Adaptar casts/coalesce según tipos reales.

No usar sentinel.

---

# 37. DDL ajuste — columnas

Corregir modelo.

Mínimo conceptual:

```text
id UUID PK

tipo

fecha

asignacion_serie_id NULL

salon_resultado_id NULL

instructor_resultado_id NULL

tipo_actividad_resultado_id NULL

hora_inicio_resultado NULL

hora_fin_resultado NULL

activo
```

`created_at` / `updated_at` sólo si patrón existente lo exige.

## CANCELACION

`serie_id` NOT NULL.

Todos los campos resultado NULL.

## REEMPLAZO

`serie_id` NOT NULL.

Todos los campos resultado NOT NULL.

## ADICION

`serie_id` NULL.

Todos los campos resultado NOT NULL.

No persistir salón origen:

se deriva del target nominal.

---

# 38. CHECKs DDL

Diseñar CHECK por tipo que garantice forma válida.

## CANCELACION

target sí / resultado no.

## REEMPLAZO

target sí / resultado completo.

## ADICION

target no / resultado completo.

Para resultado:

```text
hora_fin > hora_inicio
```

No permitir estado híbrido.

---

# 39. UNIQUE target

Índice único parcial conceptual:

```text
(asignacion_serie_id, fecha)
WHERE activo
AND tipo IN ('CANCELACION','REEMPLAZO')
```

o equivalente exacto.

Objetivo:

máximo un ajuste target activo por `serie+fecha`.

---

# 40. Adiciones

No imponer unique por:

```text
salon+fecha+hora
```

porque múltiples instructores son válidos.

La identidad es UUID.

Duplicado operativo se evita por writer/invariante, no por constraint demasiado amplio.

---

# 41. Índices

Proponer:

- target `serie+fecha`;
- `salon_resultado+fecha`;
- `instructor_resultado+fecha`;
- activo/fecha según queries reales.

No sobreindexar.

---

# 42. Identidad efectiva

Cerrar:

## Recurrente

```text
(SERIE_ASIGNACION, serieId, fecha)
```

## Reemplazo

MISMA identidad.

## Adición

```text
(AJUSTE, ajusteId, fecha)
```

Editar reemplazo:

identidad efectiva permanece.

Editar adición:

`ajusteId` permanece.

Soft delete + recreate target:

nueva fila física puede existir, pero identidad recurrente sigue `serie+fecha`.

Soft delete + nueva adición:

nuevo `ajusteId` → nueva identidad.

---

# 43. Mutabilidad / soft delete

Pasado:

```text
INMUTABLE
```

Hoy:

```text
MUTABLE, FECHA ATOMICA
```

Futuro:

```text
MUTABLE
```

Clock central.

Ajustes activos pueden actualizarse.

Retirar:

```text
activo=false
```

Recrear `CANCELACION`/`REEMPLAZO`:

nueva fila física, misma identidad lógica `serie+fecha`.

ADICION:

si se quiere la misma identidad, usar PUT con mismo `ajusteId` mientras activa.

Tras retiro definitivo, nueva adición debe usar nuevo `ajusteId`.

---

# 44. Idempotencia

Para target recurrente:

```text
PUT sobre recurso natural serieId+fecha
```

Mismo contenido:

```text
200 no-op
```

Nuevo:

```text
201
```

Cambio:

```text
200
```

DELETE:

soft delete.

Inexistente:

```text
404
```

ADICION:

cliente provee `ajusteId` UUID.

PUT:

- 201 primera creación;
- 200 mismo contenido/update.

DELETE:

soft delete.

Esto es diseño para fase de activación, NO controller de F2D.2.

---

# 45. API futura

Conservar como contrato NO IMPLEMENTADO:

```text
PUT
/api/salones/{origen}/programacion/ocurrencias/{serieId}/fechas/{fecha}/ajuste
```

```text
DELETE
/api/salones/{origen}/programacion/ocurrencias/{serieId}/fechas/{fecha}/ajuste
```

```text
PUT
/api/salones/{contexto}/programacion/adiciones/{ajusteId}
```

```text
DELETE
/api/salones/{contexto}/programacion/adiciones/{ajusteId}
```

```text
GET
/api/salones/{salonId}/programacion/efectiva?fecha=...
```

```text
GET
/api/instructores/{instructorId}/programacion/efectiva?fecha=...
```

Marcar explícitamente:

```text
FUERA DE F2D.2
```

---

# 46. Scoping por salón

`porSalonYFecha` debe considerar:

- nominales cuyo origen es salón consultado;
- reemplazos cuyo RESULTADO está en salón consultado;
- adiciones cuyo resultado está en salón consultado.

Pero no duplicar un reemplazo saliente.

Ejemplo:

```text
origen = Juriquilla
resultado = Cimatario
```

Consulta Juriquilla:

original suprimido; no mostrar replacement.

Consulta Cimatario:

mostrar replacement.

Para evaluar correctamente ajustes:

el resolver puede necesitar un conjunto nominal/global más amplio antes de filtrar.

Cerrar algoritmo.

---

# 47. Consulta por instructor

Mismo principio:

aplicar ajustes antes de filtrar.

Cambio:

```text
Ariadna → Alberto
```

Consulta Ariadna:

no mostrar original.

Consulta Alberto:

mostrar replacement.

---

# 48. Autorización

Contratos futuros:

### Lectura

`calendario.leer`

### CANCELACION

`calendario.gestionar`
o
`calendario.cancelar`

### REEMPLAZO sólo horario manteniendo salón/instructor/actividad

`calendario.gestionar`
o
`calendario.editar`

### REEMPLAZO que cambia salón/instructor/actividad

`calendario.gestionar`

### ADICION

`calendario.gestionar`

Cross-salon:

autorización sobre origen y destino ANTES de locks.

Retirar ajuste:

autorizar según efecto inverso.

Cerrar reglas concretas en checkpoint.

No implementar API en F2D.2.

---

# 49. Rollout completo

Documentar secuencia futura:

## F2D.2

Dark launch backend.

## F2E / preparación

- auditoría de datos;
- normalización/migración de series;
- resolver comparativo;
- preparar identidad de reservas.

## Fence por salón

```text
LEGACY
MIGRANDO
NUEVA
```

## MIGRANDO

- bloquear writers externos;
- migrar;
- validar equivalencia.

## NUEVA

- habilitar controllers/consumers nuevos;
- legacy writers rechazados para ese salón.

## Retiro

`TurnoInstructor` después de cero consumers.

No permitir unión productiva.

---

# 50. Test plan F2D.2

Actualizar.

Debe cubrir como mínimo:

## Persistencia

- EXCLUDE serie/vigencias;
- dos versiones solapadas rechazadas;
- adyacencia de vigencias válida donde corresponda;
- CHECKs por tipo;
- unique target activo;
- múltiples inactivas;
- recreate.

## Resolver

- recurrente nominal sin ajuste;
- cancelación;
- replacement;
- adición;
- origen cerrado → replacement destino abierto;
- original fuera → replacement dentro;
- cancelación sobre nominal operativamente omitida;
- replacement entrante/saliente;
- queries por salón/instructor después de ajustes;
- determinismo;
- duplicado efectivo → invariante.

## Orfandad

- 0 target;
-

> 1 protegido por DB/invariante;

1. cambio vigencia que intenta huérfano;
2. cambio día/bloque que intenta huérfano.

## Locks

- ajuste same instructor same salon;
- cross-salon;
- recurrent writer vs ajuste;
- cambio instructor;
- cambio salón;
- swap cross-salon;
- discovery stale;
- lock order deterministic.

## Dark launch

- sin controllers nuevos;
- legacy no modificado;
- Reserva legacy no participa;
- `ProgramacionEfectiva` no consume `TurnoInstructor`.

## Fail-closed

- instructor desactivado;
- actividad desactivada;
- especialidad removida;
- salón inválido/no operativo.

No fijar número artificial.

---

# 51. Mutaciones A-O

Actualizar para que TODAS queden detectadas.

### A

omitir nominal antes de replacement.

### B

ajuste toma `InstructorLock` pero `Asignacion` no.

### C

target sin versión.

### D

dos versiones cubren D.

### E

target cambia entre discovery/lock.

### F

Reserva legacy bloquea ajuste.

### G

Reserva nueva futura no puede asociarse inequívocamente.

### H

duplicado efectivo.

### I

replacement destino abierto se pierde.

### J

especialidad removida deja ajuste visible.

### K

FK inválida `serieId`.

### L

dos ajustes target activos.

### M

swap deadlock.

### N

consumer mezcla legacy/nueva.

### O

API nueva pública antes de cutover.

Para F2D.2:

F y O deben detectarse por AUSENCIA deliberada de adapters/controllers.

G:

documentar como blocker de activación, no implementación F2D.2.

---

# 52. Alcance F2D.2 — final

## INCLUYE

- V47 con EXCLUDE de vigencias + tabla ajuste;
- entidad/repo `AjusteProgramacionFecha`;
- value objects;
- `ProgramacionEfectiva`;
- resolver nominal;
- writers internos;
- `SalonLocks` ordenados;
- `InstructorLocks` ordenados;
- hardening `BloqueProgramacionService`/`Asignacion`;
- Policy A inversa;
- fail-closed estado/especialidad;
- tests unit;
- PostgreSQL real;
- concurrency;
- arquitectura;
- checkpoint.

## EXCLUYE

- controllers públicos;
- Reserva legacy adapter;
- frontend;
- mobile;
- migración de datos;
- cutover;
- fence persistido;
- `TurnoInstructor`;
- sesiones;
- confirmación;
- capacidad/recursos;
- notificaciones;
- pagos.

---

# 53. Detalle de F2D.2 dark launch

Aunque existan writers internos en servicios:

NO debe existir endpoint productivo que permita crear ajustes.

Los tests pueden invocar directamente servicios.

F2D.2 entrega infraestructura lista pero inactiva externamente.

---

# 54. Decisiones abiertas

El checkpoint debe terminar:

```text
Decisiones abiertas:
NINGUNA
```

Si surge alguna contradicción nueva:

NO inventar implementación.

Declarar:

```text
F2D.1 BLOQUEADA
```

y documentarla.

---

# 55. Corrección del documento

Agregar:

```text
## F2D.1.1 — Correcciones post-review
```

Pero también modificar las secciones principales.

NO dejar como especificaciones activas:

- orden viejo del resolver;
- controller F2D.2;
- reservas legacy bloqueando;
- `InstructorLock` unilateral;
- V47 sin EXCLUDE.

Debe existir una sola especificación vigente dentro del checkpoint corregido.

---

# 56. Validación

Ejecutar, sólo después de recuperar el repositorio y superar pre-flight:

```text
git status --short
git diff --check
git diff --stat
git diff -- auditoria/fase-2d1-diseno-ajustes-programacion-fecha.md
```

Esperado:

SOLO checkpoint F2D.1 modificado.

NO código.

NO tests.

NO migración.

---

# 57. Regla de no commit

NO hacer commit.

Primero habrá gate final/re-review.

---

# Stop conditions

DETENER sin completar la intervención si:

- branch/HEAD no coinciden con el corte esperado;
- working tree contiene cambios adicionales desconocidos;
- falta cualquiera de los artefactos:
  - checkpoint F2D.1;
  - review F2D.1;
  - esta intervención F2D.1.1;
- baseline falla antes de modificar;
- el checkpoint fue alterado por otra intervención;
- se descubre que F2D.1.1 ya fue ejecutada;
- resolver los P1 requiere modificar código;
- resolver los P1 requiere crear migraciones;
- aparece una contradicción nueva que no puede cerrarse documentalmente;
- resulta necesario improvisar arquitectura fuera del review.

---

# Resultado esperado de la intervención

Responder al finalizar:

```text
# F2D.1.1 — Diseño corregido

Base:
8c40594d2caf8b5230b364cb76cd8f48fe5ed98a

Checkpoint:
auditoria/fase-2d1-diseno-ajustes-programacion-fecha.md

F2D.2:
DARK LAUNCH / PRODUCTIVO

Modelo:
AjusteProgramacionFecha

Legacy TurnoInstructor:
AISLADO / MEZCLADO

Controllers F2D.2:
NINGUNO / ...

Target:
serieId + fecha NOMINAL

EXCLUDE vigencia:
SI / NO

Policy A inversa:
SI / NO

ProgramacionEfectiva:
NOMINAL → AJUSTES → OPERATIVO FINAL

Origen cerrado → destino abierto:
APARECE / NO

Reservas legacy:
NO PARTICIPAN / PARTICIPAN

SalonLocks:
ORDENADOS / NO

InstructorLocks:
ORDENADOS / NO

Writers Asignacion:
PARTICIPAN / NO

Discovery TOCTOU:
CERRADO / ABIERTO

V47:
<resumen>

FK serieId:
NO

Identidad recurrente/replacement:
serieId + fecha

Identidad adicion:
ajusteId + fecha

API:
DISEÑADA PARA FUTURA ACTIVACION / IMPLEMENTADA EN F2D.2

Permisos:
...

Fail-closed especialidad/estado:
...

Rollout:
...

Tests:
...

src/main:
SIN CAMBIOS

src/test:
SIN CAMBIOS

Migraciones:
SIN CAMBIOS

Working tree:
SOLO CHECKPOINT

Decisiones abiertas:
NINGUNA / ...

F2D.1:
LISTA PARA RE-REVIEW / BLOQUEADA
```

---

# Próximo gate

Después de ejecutar esta intervención:

**NO iniciar F2D.2.**

Debe realizarse un re-review del checkpoint F2D.1 corregido.

Sólo si el gate posterior permite cerrar los P1 podrá F2D.1 pasar a:

```text
DISEÑO_APROBADO
```

---

# Estado al materializar este artefacto

```text
F2D.1:
REQUIERE_AJUSTE

F2D.1.1:
PREPARADA
NO EJECUTADA
```
