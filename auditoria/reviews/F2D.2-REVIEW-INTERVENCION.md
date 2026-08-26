# FeelingPilates — Review adversarial intervención F2D.2

## 1. Pre-flight

Branch:
`operacion/excepciones-horario-fecha`

HEAD:
`6a8ffaa104de9a6b707982e679e78cda8aeb433c`

Remote:
`origin/operacion/excepciones-horario-fecha` → `6a8ffaa104de9a6b707982e679e78cda8aeb433c`

Working tree:
`LIMPIO`

Checkpoint SHA:
`58af39f41b3bc089ebbd4ec67f684e270087ddf4eb695f2c7b55276d0aff352e`

Resultado:
**PASS**

No se ejecutaron tests ni comandos Git de escritura.

## 2. Contraste físico

Última Flyway:
`V46__horario_operacion_drop_unique_dia.sql`

V47 libre:
**SÍ**

Writers `BloqueProgramacion`:
Únicamente `BloqueProgramacionService.crearBloque`.

Writers `Asignacion`:
Únicamente `BloqueProgramacionService.crearAsignacion`.

La búsqueda integral no encontró otros writers productivos hacia `programacion_bloque` o `programacion_asignacion`. Los INSERT/DELETE adicionales están limitados a fixtures de tests.

Clock bean:
**EXISTE**, en [RelojConfig.java (line 19)]\(/Users/jesusaldaircruzortiz/Desktop/Feelingpilates/feelingpilates/src/main/java/com/feelingpilates/config/RelojConfig.java:19).

`btree_gist`:
**EXISTE desde V44**.

Tablas/FKs:
Los nombres y tipos propuestos coinciden con el esquema físico: `salon(id UUID)`, `usuario(id UUID)` y `tipo_actividad(id UUID)`.

Baseline histórico comprobable sin ejecutar:
Los 44 reportes existentes suman `493 tests / 0 failures / 0 errors / 0 skipped`.

Afirmaciones falsas del preparador:
La afirmación `Riesgos / decisiones abiertas: NINGUNA` es falsa. Las afirmaciones puramente físicas principales sí resultaron correctas.

## 3. V47

EXCLUDE:
**PASS**

La expresión:

```
serie_id WITH =,
daterange(vigente_desde, vigente_hasta, '[]') WITH &&
WHERE (activo)
```

coincide con las columnas físicas de [V41 (line 26)]\(/Users/jesusaldaircruzortiz/Desktop/Feelingpilates/feelingpilates/src/main/resources/db/migration/V41\_\_programacion\_bloque\_asignacion.sql:26) y con la semántica aprobada.

Pre-auditoría:
Es implementable transaccionalmente mediante una comprobación previa que lance excepción. Una inserción concurrente entre la comprobación y el `ALTER TABLE` no deja corrupción: PostgreSQL valida nuevamente todos los datos al crear el EXCLUDE bajo el lock DDL. La migración completa abortaría si el conflicto alcanzara a commit.

Rangos:
Inclusivos en negocio. PostgreSQL canonicaliza el `daterange` discreto correctamente.

NULL `vigente_hasta`:
Representa límite superior abierto correctamente.

Vigencias consecutivas:
`hasta = 31/01`, siguiente `desde = 01/02` no se solapan. Compartir el mismo día sí se rechaza.

Filas inactivas:
Quedan fuera del EXCLUDE.

CHECKs:
**PASS**. La combinación de `chk_tipo`, `chk_forma` y `chk_rango` impide cancelaciones con resultado, reemplazos/adiciones parciales y rangos no positivos.

Unique parcial:
**PASS** para un único target activo `serieId + fecha`.

FKs:
**PASS**. Correctas para resultado y correctamente ausente para `asignacion_serie_id`.

Resultado:
**PASS**

## 4. Identidad/JPA

Entidad:
Es correcto no heredar sin cambios [EntidadBase.java (line 18)]\(/Users/jesusaldaircruzortiz/Desktop/Feelingpilates/feelingpilates/src/main/java/com/feelingpilates/comun/entidad/EntidadBase.java:18), porque su ID lleva `@GeneratedValue`.

PK física:
Usar `ajusteId` como PK física de una adición es compatible con el UUID aportado por el caller, pero el ciclo JPA queda insuficientemente cerrado.

Identidad recurrente:
`serieId + fecha` — correcta.

Identidad adición:
`ajusteId + fecha` — declarada, pero la intervención no prohíbe explícitamente cambiar `fecha` durante un update por `ajusteId`.

Soft delete/recreate:
La regla conceptual es correcta, pero falta exigir una consulta por PK que también vea filas inactivas. Consultar sólo “por ID activo” y llamar `saveAndFlush` con ID asignado puede terminar haciendo `merge` sobre una fila inactiva y reactivándola.

No-op:
Correctamente definido como ausencia total de `save`/`flush`.

Decisiones nuevas pendientes:

- si `fecha` es inmutable durante la vida de una adición;
- cómo distingue JPA una creación con ID asignado de una actualización;
- cómo se rechaza la reutilización de un UUID retirado.

Resultado:
**P1**

## 5. Target / nominales

Target `serieId + fecha`:
**PASS**

Cardinalidad:
0/1/>1 está correctamente definida y debe preservarse sin convertir corrupción en ausencia.

Consulta nominal:
Los campos y predicados propuestos son suficientes. El join físico `asignacion.bloque_id = bloque.id` es inequívoco.

Corrupción:
La proyección permite devolver varias filas y detectarlas antes de indexar.

Observación:
“Una sola query nativa” no proviene del diseño aprobado y es una restricción innecesaria. Una implementación equivalente con proyección no debería considerarse inválida sólo por no ser nativa.

Resultado:
**PASS con P2**

## 6. Policy A

Writers reales:
`crearBloque` y `crearAsignacion`, confirmado en [BloqueProgramacionService.java (line 69)]\(/Users/jesusaldaircruzortiz/Desktop/Feelingpilates/feelingpilates/src/main/java/com/feelingpilates/programacion/servicio/BloqueProgramacionService.java:69).

Writers cubiertos:
Ambos.

Discovery de ajustes futuros:
La consulta propuesta por serie/rango, combinada con el `Clock` existente, permite localizar ajustes activos de hoy/futuro. Deben intersectarse las vigencias de asignación y bloque y respetarse el día del bloque.

Locks necesarios:
Para los únicos writers actuales de creación, salón del bloque e instructor nuevo son suficientes. `crearBloque` aislado no crea nominales y no puede alterar cardinalidad.

0/>1:
Rechazado por proyección; el EXCLUDE es backstop físico para duplicidad temporal de serie.

Resultado:
**PASS para los writers físicos actuales**

## 7. Locking

SalonLocks:
Reutilizable correctamente mediante el bean separado `SalonLock`, con `Propagation.MANDATORY`.

InstructorLocks:
Implementable con una query JPQL `PESSIMISTIC_WRITE` equivalente en `UsuarioRepository`.

Orden global:
`SALONES UUID asc → INSTRUCTORES UUID asc` — correcto.

`crearAsignacion`:
El orden discovery mínimo → salón → instructor → relectura del bloque está correctamente planteado.

Writer ajustes:
Correcto para targets recurrentes y updates que incluyan recursos anteriores+nuevos.

Recurrente vs ajuste:
Cerrado por locks compartidos y EXCLUDE.

Cross-salon:
Cerrado para conflictos por instructor y movimientos conocidos.

Hueco:
Dos altas concurrentes de la misma adición con el mismo `ajusteId`, pero salón e instructor completamente distintos, no comparten ningún lock. Al no existir todavía fila de ajuste, tampoco hay fila que bloquear. Ambas pueden descubrir “inexistente” y llegar al INSERT.

Resultado:
**P1**

## 8. TOCTOU

Discovery:
Suficiente para target recurrente y actualizaciones existentes.

Relectura:
Nominal y ajuste deben releerse después de todos los locks.

Comparación:
Debe incluir versiones, recursos originales/nuevos y contenido persistido del ajuste.

Phantoms/races:
Los phantoms de programación por instructor quedan serializados por `InstructorLocks`; horario y salón por `SalonLocks`.

Permanece abierta la carrera de creación de la misma adición con lock sets disjuntos. La PK mantiene integridad física, pero el perdedor recibe un `23505` de PK no traducido, porque la intervención ordena traducir únicamente `idx_programacion_ajuste_target_activo`.

Resultado:
**PARCIAL**

## 9. ProgramacionEfectiva

Pipeline:
Correcto.

Scoping:
Global antes del filtro.

Horario destino:
Correcto; el origen cerrado no elimina un reemplazo válido.

Fail-closed:
Coincide con el modelo físico: salón activo, instructor activo, rol global o del salón final, actividad activa, especialidad, oferta y horario efectivo.

Invariantes:
Duplicado exacto y solape global después del fail-closed; adyacencia permitida.

Filtro:
Posterior a todos los ajustes e invariantes.

Resultado:
**PASS**

## 10. Dark launch

TurnoInstructor:
Sin modificación.

Reserva:
Sin modificación ni atribución heurística.

SalonHorarioExcepcionService:
Sin nuevos adapters F2D.

ImpactoBloquesEnHorario:
Permanece como adapter preexistente; no debe ampliarse para leer ajustes.

ImpactoAjustesEnExcepcionHorario:
No implementado.

Controllers:
Ninguno nuevo.

Consumers:
Ninguno nuevo.

Fence/cutover:
No implementados.

Resultado:
**PASS**

## 11. Tests

Unitarios:
Amplios, pero faltan detectores explícitos para:

- intento de cambiar `fecha` de una adición activa;
- reutilización del UUID de una adición retirada;
- comportamiento JPA de create/update con ID asignado.

Servicio:
Adecuado para targets recurrentes, Policy A y updates con recursos anteriores+nuevos.

PostgreSQL:
La migración V46→V47 es técnicamente comprobable. Ya existe un patrón real de Flyway por etapas en [HorarioOperacionMigracionV43V46Test.java (line 29)]\(/Users/jesusaldaircruzortiz/Desktop/Feelingpilates/feelingpilates/src/test/java/com/feelingpilates/ubicaciones/HorarioOperacionMigracionV43V46Test.java:29).

Concurrencia:
Falta el caso `mismo ajusteId de adición + recursos disjuntos`. Los tests deben afirmar bloqueo/relectura o error estable, no únicamente que al final quedó una fila.

Arquitectura:
Suficiente para dark launch.

Tests que pueden dar falso positivo:
Un test de “mismo target” limitado a cancelación/reemplazo no detectaría la carrera de identidad de una adición nueva.

Resultado:
**P1**

## 12. Mutaciones A–O

A: **DETECTADA**
B: **DETECTADA**
C: **DETECTADA**
D: **DETECTADA**
E: **DETECTADA**
F: **DETECTADA**
G: **DETECTADA DOCUMENTALMENTE** — no debe presentarse como test runtime de F2D.2.
H: **DETECTADA**
I: **DETECTADA**
J: **DETECTADA**
K: **DETECTADA**
L: **DETECTADA**
M: **DETECTADA**
N: **DETECTADA**
O: **DETECTADA**

Total detectadas:
**15/15**

Parciales:
0 dentro de A–O.

No detectadas:
0 dentro de A–O.

Los huecos encontrados afectan identidad/concurrencia de adiciones y no están representados en A–O.

## 13. Regresión P1

P1-1: **CERRADO**
P1-2: **CERRADO**
P1-3: **CERRADO**
P1-4: **CERRADO**
P1-5: **CERRADO**
P1-6: **CERRADO para targets recurrentes**
P1-7: **CERRADO**
P1-8: **CERRADO**

## 14. Regresión P2

P2-1: **REABIERTO** — identidad/idempotencia de adición incompleta respecto a cambios de fecha, UUID retirado y concurrencia de alta.

P2-2: **CERRADO**

P2-3: **PARCIAL** — el orden multi-lock está cerrado, pero no existe punto común para dos altas del mismo `ajusteId` con recursos disjuntos.

## 15. Scope

Archivos necesarios no contemplados:
**NINGUNO fuera de las áreas permitidas**. Las correcciones caben en entidad, repositorio, writer y tests previstos.

Archivos indebidamente incluidos:
**NINGUNO**

Scope creep:
**NO**

Resultado:
**PASS**

## 16. Decisiones abiertas reales

1. Fecha de una adición activa
   - Pregunta: ¿puede cambiarse `fecha` en un update por `ajusteId`?
   - Alternativas: fecha inmutable; o movimiento como cambio de identidad.
   - Evidencia: el checkpoint define identidad `(ajusteId, fecha)` y afirma que editar conserva identidad ([checkpoint (line 322)]\(/Users/jesusaldaircruzortiz/Desktop/Feelingpilates/feelingpilates/auditoria/fase-2d1-diseno-ajustes-programacion-fecha.md:322)).
   - Impacto: locks, proyección, temporalidad, respuesta idempotente e identidad efectiva.
2. Alta concurrente del mismo `ajusteId`
   - Pregunta: ¿cómo responde el perdedor cuando los comandos tienen recursos disjuntos?
   - Alternativas: conflicto estable por PK exacta y retry externo; o un punto adicional de serialización compatible.
   - Impacto: TOCTOU, error estable e idempotencia. No debe resolverse mediante upsert/retry interno que omita los locks de recursos anteriores.
3. Persistencia JPA con ID asignado
   - Pregunta: ¿cómo se distingue creación de update y cómo se impide reutilizar una PK inactiva?
   - Alternativas: estrategia explícita `Persistable`, persistencia dedicada o `merge` acompañado de comprobación histórica completa.
   - Impacto: no-reactivación, timestamps e identidad física.
4. Señal técnica fail-closed
   - Pregunta: SLF4J estructurado o port inyectable.
   - Impacto: menor, pero la intervención ofrece dos arquitecturas en vez de seleccionar una.

## 17. Hallazgos

### P0

NINGUNO

### P1

#### P1-1 — Ciclo de identidad de adiciones insuficientemente cerrado

Evidencia:
El checkpoint establece que la identidad es `ajusteId + fecha`, que editar conserva identidad y que recrear usa un UUID nuevo. La intervención identifica la adición sólo por `ajusteId`, no declara `fecha` inmutable y sólo exige queries por ID activo. Además, el modelo base actual usa ID generado y timestamps Hibernate ([EntidadBase.java (line 20)]\(/Users/jesusaldaircruzortiz/Desktop/Feelingpilates/feelingpilates/src/main/java/com/feelingpilates/comun/entidad/EntidadBase.java:20)), por lo que el nuevo ID asignado requiere cerrar expresamente la semántica de persist/merge.

Riesgo:
Cambiar silenciosamente la identidad efectiva, reactivar una fila inactiva o sobrescribir sus timestamps.

Corrección conceptual requerida:
Declarar `fecha` inmutable en una adición activa; un movimiento debe ser retiro + nueva adición/UUID. Antes de crear, comprobar cualquier fila física con esa PK, activa o inactiva; actualizar únicamente la entidad activa ya localizada. Especificar y probar la estrategia JPA para IDs asignados.

#### P1-2 — La identidad de una adición nueva no participa del protocolo concurrente

Evidencia:
El lock set aprobado para ADICIÓN sólo contiene salón e instructor resultado ([checkpoint (line 567)]\(/Users/jesusaldaircruzortiz/Desktop/Feelingpilates/feelingpilates/auditoria/fase-2d1-diseno-ajustes-programacion-fecha.md:567)). Dos comandos con el mismo `ajusteId` y resultados completamente distintos no comparten locks ni fila preexistente. La intervención traduce únicamente el índice target, no la PK de la adición.

Riesgo:
El comportamiento depende de un `23505` crudo y no cumple un contrato estable de conflicto/idempotencia. Una solución ingenua con upsert podría actualizar sin haber bloqueado los recursos persistidos por la transacción ganadora.

Corrección conceptual requerida:
Definir explícitamente el comportamiento del perdedor. Como mínimo: reconocer inequívocamente la colisión de PK, abortar con código estable sin retry/upsert interno, y exigir que un retry externo redescubra la fila y bloquee la unión de recursos anteriores+nuevos. Añadir concurrencia real con mismo `ajusteId` y recursos disjuntos.

### P2

#### P2-1 — Restricción innecesaria de “una sola query nativa”

El diseño exige resultados y cardinalidad, no una tecnología de query concreta. Debe permitirse cualquier proyección equivalente que preserve la detección 0/1/>1.

#### P2-2 — Señal técnica con dos implementaciones alternativas

“SLF4J o port inyectable” deja una elección de arquitectura menor. La intervención debería seleccionar una; un port inyectable con implementación SLF4J ofrece prueba determinista sin persistencia ni broker.

## 18. Conteo

P0:
**0**

P1:
**2**

P2:
**2**

## 19. Ejecutabilidad

¿La intervención puede ejecutarse literalmente por otro Codex sin tomar decisiones nuevas?:
**NO**

¿Preserva dark launch?:
**SÍ**

¿Preserva los 8 P1 cerrados?:
**SÍ**, aunque introduce un hueco concurrente nuevo fuera de la matriz original.

¿Las 15 mutaciones tienen detector suficiente?:
**SÍ**, considerando G como evidencia documental y no como test runtime.

¿Scope suficiente?:
**SÍ**

## 20. Gate

**B. REQUIERE F2D.2.1 — CORREGIR INTERVENCIÓN ANTES DE EJECUTAR**

Justificación:
La propuesta es correcta en SQL, target nominal, Policy A, composición efectiva y aislamiento dark-launch. No requiere rediseño global. Sin embargo, no cierra de forma literal la identidad y persistencia de las adiciones ni la carrera de creación del mismo `ajusteId` con recursos disjuntos. Esos dos P1 deben corregirse en la intervención antes de materializar F2D.2.

Estado final del repositorio:
`LIMPIO`

Tests ejecutados:
`NINGUNO`

Archivos modificados:
`NINGUNO`

Commit:
`NO CREADO`

Push:
`NO REALIZADO`