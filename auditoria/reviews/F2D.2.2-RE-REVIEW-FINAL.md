# FeelingPilates — Re-review final F2D.2.2

## 1. Pre-flight

Branch:
`operacion/excepciones-horario-fecha`

HEAD:
`6a8ffaa104de9a6b707982e679e78cda8aeb433c`

Remote:
`6a8ffaa104de9a6b707982e679e78cda8aeb433c`

Working tree:
LIMPIO

Checkpoint SHA:
`58af39f41b3bc089ebbd4ec67f684e270087ddf4eb695f2c7b55276d0aff352e`

Resultado:
PASS

## 2. P1 final

RAMA A determinista:
SI — identidad visible en la relectura implica aborto inmediato.

Persist RAMA A:
NO

RAMA B determinista:
SI — identidad ausente implica proyección, validación, entidad nueva, `persist` y `flush`.

Nueva lectura PK antes de persist:
NO

PK backstop:
PRIMARY KEY física definitiva para carreras todavía invisibles; no se presenta como lock.

Constraint:
`programacion_ajuste_fecha_pkey`

SQLSTATE:
`23505`

Perdedor:
`CONFLICTO_AJUSTE_PROGRAMACION`

Resultado:
CERRADO

## 3. Tests A/B/C

Test A:
PASS — exige ambas variantes, activa e inactiva, y verifica ausencia de persist, flush, update, mutación y proyección posterior.

Test B:
PASS — exige PostgreSQL real, dos transacciones, recursos disjuntos y snapshots distinguibles.

Test B fuerza ambas relecturas ausentes:
SI

Test B demuestra 23505+PK exacta:
SI — exige `23505` y `programacion_ajuste_fecha_pkey`, además de una única fila íntegra del ganador.

Test C:
PASS — exige nueva transacción y discovery completo; no interpreta el conflicto anterior como éxito idempotente.

Resultado:
PASS

Los tests se evaluaron como requisitos de la intervención; no fueron ejecutados durante este review.

## 4. Identidad/JPA

Identidad:
Recurrente/reemplazo: `serieId + fecha`. Adición: `ajusteId + fecha`.

Fecha:
INMUTABLE para una adición activa.

UUID histórico:
NO REUTILIZABLE; una fila inactiva nunca se reactiva.

Create:
Entidad nueva + `EntityManager.persist` + `flush`.

Update:
Entidad managed + mutación exclusiva de campos editables + `flush`.

Retiro:
Entidad managed + `activo=false` + `flush`.

No-op:
Sin dirty state, escritura, flush explícito ni cambio de `actualizadoEn`.

Merge/upsert:
NO

Resultado:
PASS

## 5. V47

EXCLUDE:
Correcto sobre `serie_id` y `daterange(vigente_desde, vigente_hasta, '[]')`, condicionado por `activo`.

CHECKs:
Tipo, forma completa por modalidad y rango válido.

Unique target:
`idx_programacion_ajuste_target_activo`

FK serie:
NO

PK explícita:
`CONSTRAINT programacion_ajuste_fecha_pkey PRIMARY KEY (id)`

Resultado:
PASS

## 6. Policy A / Locks / TOCTOU

Writers:
El código físico confirma únicamente `crearBloque` y `crearAsignacion` como writers recurrentes nuevos actuales; ambos quedan expresamente cubiertos.

Policy A:
Proyecta ajustes activos afectados de hoy/futuro; exige exactamente un target y nunca modifica automáticamente el ajuste.

Orden:
SALONES UUID ascendente → INSTRUCTORES UUID ascendente.

Target/update:
Discovery → unión de recursos persistidos/solicitados → locks → relectura → comparación → proyección/validación.

Nueva adición:
RAMA A/B determinista con PK como backstop exclusivo de la RAMA B.

Resultado:
PASS — TOCTOU CERRADO.

## 7. ProgramacionEfectiva

Pipeline:
NOMINALES → TARGETS/AJUSTES → RESULTADOS → HORARIO DEL SALÓN FINAL → MASTER-DATA → INVARIANTES → FILTRO → ORDEN.

Fail-closed:
SI — salón, horario, instructor, rol, actividad, especialidad y oferta.

Scoping:
Resolución global previa al filtro; reemplazos entrantes/salientes correctos, origen cerrado no bloquea destino válido, cancelación puede suprimir una nominal operativamente omitida y no existe trim.

Resultado:
PASS

## 8. Dark launch

TurnoInstructor:
AUTORIDAD PRODUCTIVA

Reserva:
FUERA

Controllers:
NINGUNO

Consumers:
NINGUNO PRODUCTIVO

ImpactoAjustesEnExcepcionHorario:
NO

Fence:
NO

Cutover:
NO

Resultado:
PASS

## 9. Mutaciones

A:
DETECTADA — nominal antes del reemplazo.

B:
DETECTADA — hardening de ambos writers recurrentes.

C:
DETECTADA — Policy A y cardinalidad exacta.

D:
DETECTADA — EXCLUDE PostgreSQL.

E:
DETECTADA — relectura y comparación del lock set.

F:
DETECTADA — Reserva legacy ausente.

G:
DETECTADA — blocker documental futuro de cutover.

H:
DETECTADA — rechazo writer e invariante del resolver.

I:
DETECTADA — gobierna el horario del destino.

J:
DETECTADA — revalidación fail-closed.

K:
DETECTADA — ausencia obligatoria de FK a serie.

L:
DETECTADA — índice único parcial.

M:
DETECTADA — orden global salones → instructores.

N:
DETECTADA — ausencia de integración con flujos legacy productivos.

O:
DETECTADA — ausencia de controller.

Detectadas:
15/15

## 10. Regresión F2D.1

P1-1:
CERRADO — dark launch sin integración productiva ni cutover implícito.

P1-2:
CERRADO — target nominal exacto y EXCLUDE temporal.

P1-3:
CERRADO — Policy A inversa.

P1-4:
CERRADO — pipeline y scoping definitivos.

P1-5:
CERRADO — ambos writers recurrentes participan en locks.

P1-6:
CERRADO — discovery, locks, relectura y comparación.

P1-7:
CERRADO — reservas legacy fuera del alcance.

P1-8:
CERRADO — horario y maestros revalidados fail-closed.

Cerrados:
8/8

## 11. P2

P2-1:
RESUELTO — identidad, fecha inmutable, ciclo JPA y no-op determinados.

P2-2:
RESUELTO / fuera de implementación productiva F2D.2.

P2-3:
RESUELTO — helpers, deduplicación, sets completos y orden global.

## 12. Scope

Scope suficiente:
SI — JPA/EntityManager, Flyway, PostgreSQL, Testcontainers, traducción de constraints y SLF4J ya caben en la base tecnológica existente.

Scope creep:
NO

## 13. Decisiones abiertas

NINGUNA

## 14. Hallazgos

### P0

NINGUNO

### P1

NINGUNO

### P2

NINGUNO

## 15. Conteo

P0:
0

P1:
0

P2:
0

## 16. Gate

¿Último P1 cerrado?:
SI

¿8/8 P1 F2D.1 preservados?:
SI

¿15/15 mutaciones preservadas?:
SI

¿P2 resueltos?:
SI

¿Intervención ejecutable literalmente sin decisiones nuevas?:
SI

¿Puede materializarse documentalmente como intervención aprobada?:
SI

## 17. Veredicto

**A. PASS — INTERVENCIÓN F2D.2 APROBADA PARA MATERIALIZACIÓN PREVIA**

Justificación:
F2D.2.2 elimina la bifurcación discrecional del perdedor por `ajusteId`. La RAMA A aborta antes de crear o persistir una segunda instancia, mientras la RAMA B llega obligatoriamente a `persist/flush` y usa como backstop exclusivamente la PK explícita `programacion_ajuste_fecha_pkey`, traducida sólo con `SQLSTATE 23505` y el nombre exacto. Los detectores A/B/C fijan los dos caminos técnicos y el retry externo, sin reabrir identidad/JPA, Policy A, TOCTOU, dark launch, ProgramacionEfectiva ni el scope aprobado.
