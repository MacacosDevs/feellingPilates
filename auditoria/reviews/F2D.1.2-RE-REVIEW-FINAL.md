# FeelingPilates — Re-review final F2D.1 post F2D.1.2

## 1. Pre-flight

Branch:
`operacion/excepciones-horario-fecha`

HEAD:
`e5bcf2e229ac018be3f30c55c517865fb4d80f06`

Upstream:
`0 ahead / 0 behind`

Working tree:
Sólo `M auditoria/fase-2d1-diseno-ajustes-programacion-fecha.md`, sin staging.

SHA-256 checkpoint:
`58af39f41b3bc089ebbd4ec67f684e270087ddf4eb695f2c7b55276d0aff352e`

`git diff --check`:
Sin errores.

Scope:
PASS

## 2. P1 F2D.1.2

ImpactoAjustesEnExcepcionHorario fuera de F2D.2:
SI

Consumer productivo indirecto restante:
NINGUNO

Invariante dark-launch coherente:
SI

Fence sigue futuro:
SI

Necesidad funcional futura preservada:
SI

Resultado:
CERRADO

## 3. Regresión 8 P1

P1-1:
CERRADO — F2D.2 es dark launch, sin controllers ni consumers productivos; el fence por salón queda para activación.

P1-2:
CERRADO — target nominal `serieId + fecha`, cardinalidad exacta y EXCLUDE temporal incluida en V47.

P1-3:
CERRADO — Policy A inversa obligatoria y fallo explícito ante 0 o más de un target.

P1-4:
CERRADO — nominales → ajustes → resultados → horario del salón final → maestros/invariantes → filtro.

P1-5:
CERRADO — todos los writers recurrentes competidores participan de SalonLocks e InstructorLocks.

P1-6:
CERRADO — discovery, locks ordenados, relectura y conflicto reintentable si cambia el lock set.

P1-7:
CERRADO — reservas legacy fuera de F2D.2; no se utiliza atribución heurística.

P1-8:
CERRADO — `ProgramacionEfectiva` mantiene validación read-time fail-closed contra horario y maestros vigentes. La protección writer-time futura no fue confundida con el dark launch.

Cerrados:
8/8

## 4. P2

P2-1:
RESUELTO — recursos naturales con `PUT`, identidad propia para adiciones y no-op real; API fuera de F2D.2.

P2-2:
RESUELTO — `editar` queda acotado a horario; cambios estructurales y adiciones requieren `gestionar`.

P2-3:
RESUELTO — helpers, sets por operación y orden global completamente definidos.

## 5. Mutaciones A-O

A:
DETECTA — las nominales no se filtran antes del reemplazo.

B:
DETECTA — hardening obligatorio de Asignación y writers recurrentes.

C:
DETECTA — Policy A inversa y fail-fast ante target inexistente.

D:
DETECTA — EXCLUDE PostgreSQL más invariante runtime.

E:
DETECTA — relectura y comparación del lock set.

F:
DETECTA — ausencia deliberada del adapter de reservas legacy.

G:
DETECTA — reserva sin identidad inequívoca bloquea el cutover.

H:
DETECTA — writer rechaza duplicados y resolver falla ante corrupción persistida.

I:
DETECTA — el horario del destino se evalúa después del reemplazo.

J:
DETECTA — revalidación fail-closed de maestros vigentes.

K:
DETECTA — se prohíbe FK directa a `serieId`.

L:
DETECTA — índice único parcial y serialización concurrente.

M:
DETECTA — salones ascendentes → instructores ascendentes.

N:
DETECTA — no existe integración de ajustes con consumers, writers ni validadores productivos legacy; el adapter de impacto queda diferido.

O:
DETECTA — ausencia deliberada de controllers públicos en F2D.2.

Detectadas:
15/15

Parciales:
0

No detectadas:
0

## 6. Dark launch

TurnoInstructor autoridad productiva:
SI

Programacion nueva aislada:
SI

Writer legacy afectado por ajustes:
NO

Controllers nuevos:
NO

Consumers productivos nuevos:
NO

Doble autoridad:
NO

Resultado:
PASS

## 7. ProgramacionEfectiva

Orden:
NOMINALES → AJUSTES → RESULTADOS → HORARIO EFECTIVO DEL SALÓN FINAL → MASTER-DATA/FAIL-CLOSED → INVARIANTES GLOBALES → FILTRO → ORDEN

Fail-closed:
SI — horario, salón, instructor, rol, actividad, especialidad y oferta vigentes.

Horario salón final:
SI

Scoping post-ajustes:
SI — replacements salientes desaparecen del origen y entrantes aparecen en destino.

Resultado:
PASS

## 8. Persistencia / identidad

EXCLUDE:
SI — vigencias activas por `serieId`, sin fechas centinela.

Target:
`serieId + fecha` sobre ocurrencia nominal; 0/>1 es ruptura de invariante.

Policy A:
SI

Identidad:
Recurrente/reemplazo: `serieId + fecha`.
Adición: `ajusteId + fecha`.
Soft delete/recreate definido.

Resultado:
PASS

## 9. Concurrencia

Locks:
SalonLocks ordenados e InstructorLocks ordenados; orden global SALONES → INSTRUCTORES.

Writers recurrentes:
PARTICIPAN

TOCTOU:
CERRADO mediante prelectura → locks → relectura → comparación → conflicto/retry.

Cross-salon:
CERRADO

Resultado:
PASS

## 10. Reservas

Legacy participa:
NO

Heurística:
NO

Cutover blocker:
SI — reserva sin asociación inequívoca bloquea activación.

Resultado:
PASS

## 11. Fase futura

Fence:
Persistido por salón `LEGACY/MIGRANDO/NUEVA`, fuera de F2D.2.

Horario:
Auditoría/revalidación y protección writer-time antes de activar `NUEVA`.

Reservas:
Identidad o migración inequívoca obligatoria.

Doble autoridad:
Prohibida.

Resultado:
PASS

## 12. Decisiones abiertas

Checkpoint:
NINGUNA

Auditor:
NINGUNA

Resultado:
PASS

## 13. Scope

Sólo checkpoint:
SI

Código:
SIN CAMBIOS

Tests:
SIN CAMBIOS

Migraciones:
SIN CAMBIOS

Otros docs:
SIN CAMBIOS

Resultado:
PASS

## 14. Hallazgos nuevos

### P0

NINGUNO

### P1

NINGUNO

### P2

NINGUNO

## 15. Gate

P0:
0

P1:
0

P2:
0

P1 F2D.1.2 cerrado:
SI

8 P1 originales cerrados:
SI

15 mutaciones detectadas:
SI

¿F2D.1 puede pasar a DISEÑO_APROBADO?:
SI

¿F2D.2 puede iniciarse después del cierre documental/commit?:
SI

## 16. Veredicto

**A. PASS — F2D.1 LISTA PARA CIERRE COMO DISEÑO_APROBADO**

Justificación:
F2D.1.2 elimina de manera coherente toda integración productiva de `ImpactoAjustesEnExcepcionHorario` durante F2D.2 sin reabrir P1-8. El resolver interno conserva el fail-closed contra el horario final y maestros actuales, mientras ningún estado de `programacion_ajuste_fecha` puede vetar o transformar un flujo legacy. Los ocho P1, los tres P2 y las quince mutaciones quedan cerrados documentalmente. No se ejecutaron tests ni se modificó el repositorio.
