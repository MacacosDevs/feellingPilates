# FeelingPilates — Re-review F2D.2.1

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

## 2. P1-1 identidad

Fecha inmutable:
SI

Movimiento:
RETIRO → NUEVA ADICIÓN → NUEVO UUID

UUID retirado:
NUNCA REUTILIZABLE

Persist create:
Entidad nueva con UUID asignado → `EntityManager.persist` → `flush`

Persist update:
Entidad activa managed; conserva `id`, `fecha` y `creadoEn`; muta sólo campos editables

No reactivación:
SI

No-op:
Real; se decide antes de mutar la entidad, sin `persist`, `merge`, `save` ni `flush` explícito

Resultado:
CERRADO

## 3. JPA / exceptions

EntityManager.persist:
PASS

Managed update:
PASS

Timestamp:
PASS — coincide con `EntidadBase`: `OffsetDateTime`, `@CreationTimestamp`, `@UpdateTimestamp` y `creadoEn updatable=false`.

Constraint target:
`idx_programacion_ajuste_target_activo`

Constraint PK:
`programacion_ajuste_fecha_pkey`

SQLSTATE:
`23505`, exigido simultáneamente con el nombre exacto

Spring/Hibernate translation:
El mecanismo es implementable. El proyecto ya captura el `flush` dentro del traductor e inspecciona `ConstraintViolationException.getSQLState()` y `getConstraintName()` en [ConflictoExcepcionHorarioTranslator.java (line 36)](/Users/jesusaldaircruzortiz/Desktop/Feelingpilates/feelingpilates/src/main/java/com/feelingpilates/ubicaciones/servicio/ConflictoExcepcionHorarioTranslator.java:36). `@Repository` no es imprescindible si se inspecciona directamente la causa Hibernate; sí puede usarse si la traducción Spring ocurre al cruzar el proxy.

No obstante, el protocolo concurrente permite una salida que evita por completo esta evidencia PostgreSQL.

Resultado:
P1

## 4. P1-2 concurrencia ajusteId

Recursos disjuntos:
Correctamente reconocidos

Locks compartidos:
NO

PK backstop:
Correctamente identificada como protección global de identidad

Ganador:
Una única fila y un único snapshot

Perdedor:
NO queda determinado inequívocamente. El contrato general exige `23505 + programacion_ajuste_fecha_pkey`, pero el protocolo permite alternativamente abortar durante la relectura por “conflicto de discovery”.

Retry interno:
NO

Retry externo:
Nueva transacción, discovery completo, recursos persistidos+nuevos, locks, relectura y decisión normal

Resultado:
PARCIAL

## 5. P2

Query nominal:
RESUELTO — exige semántica, no tecnología

Diagnóstico:
RESUELTO — port interno inyectable con única implementación SLF4J

P2-1 histórico:
RESUELTO

P2-3 histórico:
RESUELTO — recursos naturales bajo `SALONES → INSTRUCTORES`; la PK no se presenta como lock

## 6. V47 regresión

EXCLUDE:
PASS

CHECKs:
PASS

Unique target:
PASS

FK serie ausente:
SI

Resultado:
PASS

## 7. Policy A / locks

Writers:
Sólo `BloqueProgramacionService.crearBloque` y `crearAsignacion`, coincidente con el código físico

Policy A:
PASS

SalonLocks:
Deduplicados, UUID ascendente y `Propagation.MANDATORY`

InstructorLocks:
Deduplicados, UUID ascendente, `PESSIMISTIC_WRITE` sobre `usuario` y `Propagation.MANDATORY`

Orden:
SALONES → INSTRUCTORES

Resultado:
PASS

## 8. TOCTOU

Target/update:
CERRADO — discovery, locks, relectura y comparación

Nueva adición:
PARCIAL — la PK protege integridad, pero el camino observable del perdedor sigue teniendo dos alternativas

Cross-salon:
CERRADO

Recurrente vs ajuste:
CERRADO

Resultado:
PARCIAL

## 9. ProgramacionEfectiva

Pipeline:
NOMINALES → TARGETS/AJUSTES → RESULTADOS → HORARIO FINAL → MASTER-DATA → INVARIANTES → FILTRO → ORDEN

Scoping:
Global antes de filtrar

Fail-closed:
PASS

Invariantes:
Duplicado y solape global; adyacencia permitida; corrupción produce excepción sin resultado parcial

Resultado:
PASS

## 10. Dark launch

TurnoInstructor:
AUTORIDAD PRODUCTIVA, SIN MODIFICAR

Reserva:
FUERA

SalonHorarioExcepcionService:
SIN MODIFICAR

Controllers:
NINGUNO

Consumers:
NINGUNO

ImpactoAjustesEnExcepcionHorario:
NO IMPLEMENTADO

Fence:
NO IMPLEMENTADO

Cutover:
NO IMPLEMENTADO

Resultado:
PASS

## 11. Tests

Identidad/JPA:
Los doce detectores exigidos están definidos, con PostgreSQL real para persistencia y timestamps.

Concurrencia mismo UUID:
El escenario requerido está definido con PostgreSQL, transacciones independientes, latches, recursos disjuntos y snapshots distinguibles.

Retry externo:
Cubierto en transacción nueva, incluyendo fila retirada.

PostgreSQL:
Cubre V46→V47, catálogo PK, colisión, EXCLUDE, CHECKs, índices, FKs y metamodelo.

Arquitectura:
Cubre aislamiento legacy, ausencia de controller/adapter/fence/cutover y uso correcto de `Clock`.

Falsos positivos posibles:
El test concurrente puede aprobar si el perdedor recibe `CONFLICTO_AJUSTE_PROGRAMACION` desde el precheck posterior a locks, mientras un test separado demuestra que el traductor de PK funciona. Eso no demuestra que el perdedor real haya sido clasificado mediante `23505 + programacion_ajuste_fecha_pkey`.

Resultado:
P1

## 12. Mutaciones A-O

A:
DETECTADA

B:
DETECTADA

C:
DETECTADA

D:
DETECTADA

E:
DETECTADA

F:
DETECTADA

G:
DETECTADA — EVIDENCIA DOCUMENTAL / BLOCKER FUTURO

H:
DETECTADA

I:
DETECTADA

J:
DETECTADA

K:
DETECTADA

L:
DETECTADA

M:
DETECTADA

N:
DETECTADA

O:
DETECTADA

Detectadas:
15/15

Parciales:
NINGUNA

No detectadas:
NINGUNA

## 13. Regresión P1 F2D.1

P1-1:
CERRADO

P1-2:
CERRADO

P1-3:
CERRADO

P1-4:
CERRADO

P1-5:
CERRADO

P1-6:
CERRADO

P1-7:
CERRADO

P1-8:
CERRADO

Cerrados:
8/8

## 14. Scope

Archivos faltantes:
NINGUNO

Scope creep:
NO

Resultado:
PASS

## 15. Decisiones abiertas

Una:

En la relectura de una adición inicialmente inexistente que ahora aparece, la intervención permite:

- abortar por conflicto de discovery; o
- continuar hasta que la PK produzca la colisión.

La alternativa está explícita en [F2D.2.1 corregida (line 761)](/Users/jesusaldaircruzortiz/.codex/attachments/fafdee46-0baa-47fb-930c-ea821ad43577/pasted-text.txt:761), pero contradice el contrato posterior de que la PK decide y de que `CONFLICTO_AJUSTE_PROGRAMACION` sólo se traduce con SQLSTATE y constraint exactos.

Además, si esa relectura carga la fila ganadora como entidad managed, intentar `persist` de otra instancia con el mismo ID puede fallar en JPA antes del `INSERT`, sin producir el `23505` exigido.

## 16. Hallazgos

### P0

NINGUNO

### P1

P1 — El camino del perdedor de la carrera por `ajusteId` sigue siendo bifurcado.

La sección 14.2 permite abortar por discovery o dejar actuar a la PK. Las secciones 15–16, en cambio, establecen que la PK decide y que el conflicto sólo se reconoce mediante:

```
SQLSTATE 23505
+
programacion_ajuste_fecha_pkey
```

Por ello:

- P1-2 no está completamente cerrado;
- otro agente debe elegir una semántica;
- el test concurrente puede producir un falso positivo;
- una relectura managed puede impedir que la colisión alcance PostgreSQL.

### P2

NINGUNO

## 17. Conteo

P0:
0

P1:
1

P2:
0

## 18. Gate

¿P1-1 original cerrado?:
SI

¿P1-2 original cerrado?:
NO

¿P2-1/P2-2 del review cerrados?:
SI

¿8 P1 F2D.1 preservados?:
SI

¿15/15 mutaciones preservadas?:
SI

¿Intervención ejecutable sin decisiones nuevas?:
NO

¿F2D.2 puede pasar a materialización documental previa a ejecución?:
NO

## 19. Veredicto

**B. REQUIERE F2D.2.2 — PERSISTEN DEFECTOS**

Justificación:
La corrección cierra identidad, persistencia asignada, query nominal, diagnóstico, V47, Policy A, locking, ProgramacionEfectiva y dark launch. Sin embargo, el contrato del perdedor concurrente sigue admitiendo dos caminos incompatibles y no garantiza que el conflicto observado proceda de la PK exacta. Es un P1 material antes de permitir una ejecución literal. No se modificaron archivos ni se ejecutaron tests.
