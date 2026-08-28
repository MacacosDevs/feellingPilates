# FeelingPilates — Checkpoint de implementación F2D.2

Estado máximo autorizado:

```
F2D.2: IMPLEMENTADA_EN_REVIEW
```

Este checkpoint no declara F2D.2 cerrada, productiva ni activa. La autoridad productiva sigue
siendo `TurnoInstructor`; la infraestructura nueva permanece en dark launch.

La declaración anterior conserva el estado máximo autorizado en el corte de materialización técnica de este checkpoint. Posteriormente, una auditoría documental fresh e independiente cerró la documentación y el cierre de publicación sin cambiar runtime, productividad, cutover ni autoridad.

## Cierre documental posterior

- Audit persistido: `auditoria/reviews/F2D.2-REVIEW-DOCUMENTAL.md`.
- Rol del audit: `DOCUMENT_AUDITOR`, fresh e independiente.
- Modo: `READ_ONLY`.
- Branch auditada: `operacion/excepciones-horario-fecha`.
- HEAD auditado: `f6456310454a297397a63dac0c7b4c418bde9f5c`.
- Resultado: `P0=0 / P1=0 / P2=1 editorial`.
- Documentation gate: `PASS`.
- Publication closure gate: `PASS`.
- Estado documental F2D.2: `CLOSED`.
- Runtime: `DARK_LAUNCH`.
- Productive: `NOT_PRODUCTIVE`.
- Cutover: `false`.
- Autoridad: `TurnoInstructor / LEGACY_VIVO / PRODUCTIVO`, sin cambios.

El audit no autoriza F2E. El delta que materializa este cierre documental requiere su propio audit fresh y no queda autoaprobado por este checkpoint.

## Trazabilidad

- Base commit: `6db7d5fd7be12384b111b84bd19291e2ee515944`.
- Branch: `operacion/excepciones-horario-fecha`.
- Intervención ejecutada: `auditoria/intervenciones/F2D.2.2-CIERRE-CARRERA-AJUSTE-ID.md`.
- Review autorizante: `auditoria/reviews/F2D.2.2-RE-REVIEW-FINAL.md`.
- Checkpoint F2D.1 verificado: SHA-256
  `58af39f41b3bc089ebbd4ec67f684e270087ddf4eb695f2c7b55276d0aff352e`.
- Pre-flight: branch, HEAD local/remoto, ahead/behind, working tree inicial, migraciones y ausencia
  de implementación F2D.2 previa conformes.
- Baseline inicial: `493 tests`, `0 failures`, `0 errors`, `0 skipped`, `BUILD SUCCESS`.
- Commit Git posterior verificado: `95900d8a1d787a24aff4ee4e10f69d540ce81339`,
  branch y upstream `operacion/excepciones-horario-fecha` sincronizados, staging vacío y working
  tree limpio. Por sí solo, este hecho no completaba el review documental, no realizaba cutover y
  no alteraba la autoridad productiva.
- Commit de documentación de cierre publicado:
  `5c5d67e590260476372e5c8166062c0fb7429da1`.
- Ancestry verificada:
  `95900d8a1d787a24aff4ee4e10f69d540ce81339` →
  `5c5d67e590260476372e5c8166062c0fb7429da1` →
  `f6456310454a297397a63dac0c7b4c418bde9f5c`.

## V47 y persistencia

Se añadió exclusivamente `V47__programacion_ajustes_fecha.sql`. La migración:

- pre-audita solapes activos por `serie_id` en `programacion_asignacion` y aborta sin reparar;
- añade `ex_programacion_asignacion_serie_vigencia` mediante EXCLUDE temporal;
- permite vigencias consecutivas y excluye filas inactivas;
- crea `programacion_ajuste_fecha` con CHECKs de tipo, forma y rango;
- declara literalmente `CONSTRAINT programacion_ajuste_fecha_pkey PRIMARY KEY (id)`;
- crea el unique target activo y los tres índices parciales de lectura;
- incorpora únicamente las tres FKs de resultado y ninguna FK para `asignacion_serie_id`;
- no contiene trigger, sentinel, auto-repair, upsert, extensión nueva ni migración legacy.

El detector V46→V47 ejecutó la pre-auditoría sobre PostgreSQL real, comprobó rollback y
conservación de datos, retiró únicamente el fixture conflictivo y verificó después el catálogo,
EXCLUDE, consecutividad, CHECKs, unique parcial, filas inactivas, FKs, ausencia de FK de serie,
índices y nombre explícito de la PK.

`AjusteProgramacionFecha` usa UUID asignado sin `@GeneratedValue` y no hereda `EntidadBase`.
Create usa `EntityManager.persist` y `flush`; update y retiro mutan la entidad managed y hacen
`flush`. `id`, `fecha` y `creadoEn` se conservan. El retiro es lógico (`activo=false`). No se usa
`merge`, `save` detached, reactivación ni upsert. El no-op no ensucia la entidad, no llama persist
ni flush explícito y conserva `actualizadoEn`.

La identidad implementada es:

- recurrente/reemplazo: `serieId + fecha`;
- adición: `ajusteId + fecha`.

La fecha de una adición activa es inmutable. Moverla exige retiro, nueva adición y nuevo UUID. Una
PK histórica inactiva se consulta sin filtrar `activo`, se rechaza y nunca se reutiliza.

## Protocolo de concurrencia

En create de adición, después del discovery histórico inexistente, el writer adquiere salones y
después instructores, relee la PK exactamente una vez y ejecuta:

- RAMA A: si la identidad apareció activa o inactiva, produce
  `CONFLICTO_AJUSTE_PROGRAMACION` antes de proyección/persist/flush, sin update ni retry;
- RAMA B: si sigue ausente, proyecta toda la fecha, valida, construye una entidad nueva y ejecuta
  `persist`/`flush`, sin otra consulta de PK previa al INSERT.

El backstop de RAMA B acepta únicamente `SQLSTATE 23505` junto con
`programacion_ajuste_fecha_pkey`. El traductor del target recurrente es separado y acepta
únicamente `23505` junto con `idx_programacion_ajuste_target_activo`.

Test A cubre de forma determinista fila aparecida activa e inactiva y demuestra ausencia de
persist, flush, update, mutación y proyección posterior. Test B usa PostgreSQL real, dos
transacciones, el mismo UUID, salones e instructores disjuntos, snapshots distinguibles y latches;
ambas completan la relectura ausente antes de persistir. Una gana, una pierde por
`23505 + programacion_ajuste_fecha_pkey`, queda una sola fila y el snapshot ganador permanece
íntegro. Test C abre una transacción nueva, ejecuta discovery completo, locks, relectura y no-op;
después comprueba que una fila retirada no puede reactivarse.

No existe retry interno, lock global, advisory lock, lock JVM, tabla de locks, upsert ni fence.

## Locks, hardening y Policy A

`SalonLocks` e `InstructorLocks` reciben colecciones, eliminan duplicados, ordenan por UUID natural
ascendente y exigen transacción `MANDATORY`. El orden global es siempre:

```
SALONES UUID ascendente → INSTRUCTORES UUID ascendente
```

Para update/retiro se usa la unión de recursos persistidos y solicitados, seguida de relectura y
comparación contra un snapshot inmutable capturado antes de los locks. La relectura de entidades
de ajuste fuerza `EntityManager.refresh`, de modo que no puede satisfacerse con la misma instancia
obsoleta del first-level cache. El alta sin fila queda cubierta por RAMA A/B y la PK.

El hardening se limitó a los dos writers físicos autorizados:

- `BloqueProgramacionService.crearBloque` usa `SalonLocks` antes de releer/validar horario;
- `BloqueProgramacionService.crearAsignacion` hace discovery, SALONES, INSTRUCTORES, relectura,
  comparación, maestros/programación, validaciones, Policy A y persistencia.

Policy A proyecta los targets activos de hoy/futuro afectados por una creación recurrente: sólo
permite cardinalidad exactamente uno; rechaza cero y más de uno sin modificar ajustes.

Además, `crearAsignacion` descubre los ajustes relevantes de su rango, incorpora sus recursos al
lock set, los relee con refresh efectivo y compara snapshots antes de persistir. Para cada fecha
afectada proyecta la nueva nominal junto con todos los ajustes activos de la fecha y valida el
resultado global; así una adición o reemplazo de otra serie para el mismo instructor se serializa y
se rechaza tanto en el mismo salón como cross-salon.

## Programación efectiva y fail-closed

`ProgramacionEfectiva` es un servicio interno con el pipeline aprobado:

```
NOMINALES → TARGETS/AJUSTES → RESULTADOS → HORARIO DEL SALÓN FINAL
→ MASTER-DATA → INVARIANTES → FILTRO → ORDEN
```

Resuelve globalmente antes de filtrar por salón o instructor. El aplicador puro conserva
cardinalidad, aplica cancelaciones/reemplazos/adiciones, elimina el reemplazo del origen y lo hace
aparecer en destino. Sólo consulta el horario del salón final: un origen cerrado no bloquea un
destino válido. No recorta rangos; permite adyacencia; duplicados exactos y solapes globales del
mismo instructor son invariantes y fallan sin entregar resultado parcial.

La validación fail-closed usa el estado actual de salón, `HorarioEfectivoSalon`, instructor, rol
`INSTRUCTOR`, actividad, especialidad y oferta del salón. Las omisiones se reportan mediante el port
interno `ProgramacionDiagnostico` y su implementación SLF4J, sin persistencia, eventos, broker,
outbox ni API.

## Dark launch

- `TurnoInstructor`, `TurnoInstructorService` y sus controllers: no modificados.
- `Reserva` y `ReservaService`: no modificados.
- `SalonHorarioExcepcionService`: no modificado.
- Controllers/endpoints nuevos: ninguno.
- Consumers o adapters legacy nuevos: ninguno.
- Frontend/mobile: no modificados.
- `ImpactoAjustesEnExcepcionHorario`: no implementado.
- Fence/cutover: no implementados.
- `programacion` no depende de `calendario`.

Los detectores de arquitectura inspeccionan estas fronteras, la ausencia de reloj global,
controllers, merge/upsert e integración productiva.

## Evidencia de tests

Continuación correctiva sobre la validación PostgreSQL de host:

- V47 conserva físicamente `chk_programacion_ajuste_tipo`, `chk_programacion_ajuste_forma` y
  `chk_programacion_ajuste_rango`. El caso con tipo `OTRO` violaba simultáneamente tipo y forma,
  por lo que no aislaba qué CHECK reportaría PostgreSQL primero. El test ahora verifica en catálogo
  la definición de `chk_programacion_ajuste_tipo` y mantiene casos de forma aislados;
- `buscarActivosEnRango` tipa explícitamente el parámetro nullable mediante
  `cast(:hasta as date)`, siguiendo el patrón ya usado por los repositorios de bloques,
  asignaciones y horarios, para eliminar el SQLSTATE `42P18`;
- servicios/aplicador/resolución: PASS (`73 tests`, `0/0/0`); stale discovery: PASS
  (`2 tests`, `0/0/0`); arquitectura/dark launch: PASS (`4 tests`, `0/0/0`);
- la reejecución local de los dos tests PostgreSQL afectados quedó bloqueada antes de sus cuerpos:
  el sandbox denegó los sockets de Docker (`Operation not permitted`). La batería
  migración/persistencia descubrió `20 tests`, todos con error ambiental de contexto, y la suite
  completa descubrió `553 tests`, `0 failures`, `118 errors` ambientales, `0 skipped`.

Evidencia ejecutada durante la corrección P1:

- servicios/aplicador/resolución/Policy A/locks: PASS (`91 tests`, `0/0/0`);
- stale discovery Rama A: PASS (`2 tests`, `0/0/0`);
- arquitectura/dark launch: PASS (`4 tests`, `0/0/0`);
- compilación completa de main y tests: PASS (`207` fuentes main y `59` fuentes test);
- migración/persistencia PostgreSQL: ejecución intentada, `20` errores de arranque de contexto
  porque el sandbox negó acceso a `/var/run/docker.sock` (`Operation not permitted`); ningún cuerpo
  PostgreSQL se ejecutó en esta corrección;
- concurrencia PostgreSQL nueva: materializada físicamente en
  `ProgramacionConcurrenciaTest` y `RelecturaAjustePostLocksTest`, pero su ejecución quedó bloqueada
  por la misma indisponibilidad de Testcontainers/Docker.

La suite completa se intentó y descubrió `553 tests`: `0 failures`, `118 errors`, `0 skipped`.
Los `118 errors` corresponden a clases que requieren el contexto PostgreSQL/Testcontainers y no
pudieron abrir el socket Docker en el sandbox; los tests que sí pudieron arrancar no reportaron
failures. Este resultado es `BUILD FAILURE` ambiental y no sustituye una corrida PostgreSQL.

La evidencia anterior describe exclusivamente la ejecución histórica del sandbox Codex, donde
Docker no estaba disponible. En una ejecución posterior y distinta, realizada desde terminal host
con Docker disponible, se validaron `28/28 PASS` focalizados y `553/553 PASS` en la suite completa
(`0 failures`, `0 errors`, `0 skipped`). Esta validación host cerró el gate de tests; no convierte
retrospectivamente la corrida del sandbox en verde ni altera su carácter de `BUILD FAILURE`
ambiental.

La safety net física añadida cubre mismo target, mismo instructor mismo salón y cross-salon,
recurrente contra ajuste, cambio de salón/instructor, swap sin deadlock y requests en orden
inverso. También se ampliaron casos de resolver, fail-closed y todas las formas inválidas de los
CHECKs. La corrida histórica del sandbox no podía declararse verde sin reejecución con PostgreSQL
disponible; la validación host posterior descrita arriba proporcionó esa ejecución. El claim
histórico `529/529 PASS` queda sustituido por la evidencia real del sandbox y la validación host
posterior, que son ejecuciones distintas.

La matriz A–O permanece materializada `15/15 DETECTADAS`. La frase histórica «pendiente de revalidación PostgreSQL en este working tree» describía el corte previo del sandbox Codex, donde Docker no estaba disponible. La ejecución posterior y distinta en terminal host resolvió ese pendiente con `28/28 PASS` focalizados y `553/553 PASS` en la suite completa. No queda revalidación PostgreSQL pendiente por esa evidencia; el `BUILD FAILURE` ambiental del sandbox conserva su significado histórico. Test A/B/C son regresiones adicionales; G continúa siendo evidencia documental y blocker futuro de cutover, no un test runtime simulado.

## Archivos

Archivos creados:

- `src/main/resources/db/migration/V47__programacion_ajustes_fecha.sql`;
- entidad y repositorio de ajustes;
- cuatro tipos de dominio de ocurrencias/invariantes;
- services de persistencia, writer, nominales, aplicador, resolución efectiva, validación, Policy A,
  diagnóstico, errores y traducción estricta de constraints;
- `SalonLocks` e `InstructorLocks`;
- tests de migración, persistencia, concurrencia A/B/C, arquitectura, dark launch, locks, Policy A,
  aplicador, resolución y fail-closed;
- tests PostgreSQL de relectura real del first-level cache y matriz concurrente de recursos;
- este checkpoint.

Archivos modificados:

- `AsignacionRepository.java`;
- `BloqueProgramacionService.java`;
- `UsuarioRepository.java`;
- `ProgramacionPersistenciaTest.java`;
- `BloqueProgramacionServiceTest.java`.

En el corte técnico original no se modificaron canónicos para declarar cierre. La afirmación
anterior de working tree sin stage, commit ni push describe ese corte histórico de materialización. Posteriormente, el commit Git
manual fue verificado como `95900d8a1d787a24aff4ee4e10f69d540ce81339` y la documentación de
cierre publicada como `5c5d67e590260476372e5c8166062c0fb7429da1`. En ese corte F2D.2 continuaba
`IMPLEMENTADA_EN_REVIEW`; el audit documental posterior la cerró como documentación `CLOSED`,
siempre en dark launch, `NOT_PRODUCTIVE`, `cutover=false` y sin cambio de autoridad productiva.
