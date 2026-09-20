# Fase 2B.3b.1 — Writers transaccionales de HorarioOperacion, validación inversa y lock compartido

## Base

Branch de trabajo `operacion/horario-versionado-writers`, creada desde
`operacion/horario-versionado-persistencia`, HEAD base
**`edea665e0c707751c6aca28ded4c497deeb89c32`**, working tree limpio y sin divergencia con `origin`
al empezar. Diseño autoritativo seguido sin reinterpretar decisiones cerradas:
`auditoria/fase-2b3b-diseno-writers-concurrencia.md` (revisión F2B.3b.0.2).

Persistencia vigente **sin cambios**: V44 `btree_gist`, V45 `ex_horario_operacion_vigencia`,
V46 `DROP UNIQUE(salon_id, dia_semana)`.

---

## 1. Inventario real de writers (verificado contra el código, no heredado del diseño)

Barrido repo-wide de `src/main/java` sobre `save/saveAll/saveAndFlush/@Modifying/delete` y sobre
SQL nativo de mutación. **El inventario del diseño §2 sigue siendo exacto**; no se encontró
ninguna diferencia.

| Entidad | Ruta | Método | ¿Puede crear incompatibilidad? | Lock |
|---|---|---|---|---|
| `HorarioOperacion` | `ubicaciones/servicio/SalonService.java:149` | `crearHorariosIniciales` (desde `crear`) | No: el salón se inserta en la misma TX y ninguna otra lo ve | no |
| `HorarioOperacion` | `ubicaciones/servicio/SalonService.java` | `actualizar` → `validarHorariosSinCambios` | No: cuarentena F2B.2, sólo lectura | no |
| `BloqueProgramacion` | `programacion/servicio/BloqueProgramacionService.java:crearBloque` | `crearBloque` | **Sí** | **sí** |
| `BloqueProgramacion` | — | `crearAsignacion` | No: no toca el horario (decisión cerrada 12) | no |
| `TurnoInstructor` | `calendario/servicio/TurnoInstructorService.java:crear` RECURRENTE | `crear` | **Sí** | **sí** |
| `TurnoInstructor` | `…crear` EXCEPCION / CANCELACION | `crear` | Fuera de Política A (§13 diseño) | no |
| `TurnoInstructor` | `calendario/servicio/TurnoInstructorService.java:actualizarTurno` | `actualizarTurno` (sólo RECURRENTE) | **Sí** | **sí** |
| `TurnoInstructor` | `…eliminar` | `eliminar` | No: `activo = false`, sólo retira restricciones | no |

Confirmado además:

- **No existe ninguna operación de reactivación** de `TurnoInstructor` ni de `BloqueProgramacion`:
  `activo` se pone a `true` una sola vez al construir la entidad. No hay camino que vuelva visible
  programación previamente desactivada.
- **Ninguna entidad puede cambiar de salón** (§40 del encargo): `actualizarTurno` toma el salón de
  `turno.getSalon()` y `ActualizarTurnoRequest` no lleva `salonId`; `BloqueProgramacion` no tiene
  operación de actualización. Por tanto **ningún comando necesita más de un lock de salón** y no se
  implementó protocolo multi-lock. La regla de guardia para el día que aparezca (adquirir ordenado
  por `UUID.compareTo`) queda registrada en el diseño §16.
- `HorarioOperacionRepository.deleteBySalonId` sigue sin llamador productivo.

---

## 2. Archivos productivos tocados

**Nuevos (`ubicaciones`)**

- `dominio/CambioHorarioOperacion.java` — tipo neutral del cambio (`ABIERTO(apertura,cierre)` / `CERRADO`).
- `dominio/ConflictoProgramacion.java` — conflicto neutral (`Origen`, `id`, `detalle`).
- `dominio/ValidadorImpactoCambioHorarioOperacion.java` — **port** de validación inversa.
- `servicio/SalonLock.java` — lock pesimista compartido.
- `servicio/VersionarHorarioOperacion.java`
- `servicio/CerrarHorarioOperacion.java`
- `servicio/HorarioOperacionErrores.java` — códigos de dominio + ejecución común de Política A.
- `servicio/ConflictoVigenciaHorarioTranslator.java` — traducción defensiva de `23P01`.

**Nuevos (adapters)**

- `calendario/servicio/ImpactoTurnosRecurrentesEnHorario.java`
- `programacion/servicio/ImpactoBloquesEnHorario.java`

**Modificados**

- `ubicaciones/repositorio/SalonRepository.java` — `bloquearParaActualizar` (JPQL + `PESSIMISTIC_WRITE`).
- `ubicaciones/repositorio/HorarioOperacionRepository.java` — `bloquearVersionesQueIntersectan`
  (variante con `for update` literal) y **corrección del Javadoc obsoleto de `findVigente`**, que
  atribuía la unicidad al `UNIQUE(salon_id, dia_semana)` retirado por V46 (deuda P2 §71 del encargo;
  se corrige porque el archivo se toca aquí).
- `calendario/servicio/TurnoInstructorService.java` — lock en `crear` RECURRENTE y en `actualizarTurno`.
- `programacion/repositorio/BloqueProgramacionRepository.java` — `buscarActivosVigentesDesde`.
- `programacion/servicio/BloqueProgramacionService.java` — lock en `crearBloque`.

**No se tocó**: ninguna migración, ningún controller, ningún DTO HTTP, routing, seguridad HTTP,
`SalonService`, `SalonHorarioExcepcionService`, `ReservaService`, `HorarioEfectivoSalon`,
`HorarioOperacionResolver`, frontend.

---

## 3. Salon lock

`SalonRepository.bloquearParaActualizar` es **JPQL** con `@Lock(PESSIMISTIC_WRITE)` — sobre query
nativa `@Lock` no es fiable, así que donde hace falta `FOR UPDATE` en una nativa se escribe literal.

`SalonLock.adquirir(UUID)` usa **`Propagation.MANDATORY`**, no `REQUIRED`: el lock debe vivir en la
transacción del llamante y liberarse en su commit. Con `REQUIRED`, invocarlo fuera de transacción
abriría una propia, tomaría el lock y lo soltaría de inmediato — el fallo silencioso que dejaría el
protocolo inútil. Falla con `ResourceNotFoundException` si el salón no existe, y devuelve la
entidad ya bloqueada para que el writer no repita un `findById`.

Se bloquea la fila **padre** `salon` y no las de `horario_operacion` porque un día puede no tener
ninguna versión: un `FOR UPDATE` sobre cero filas no bloquea nada. Ése es exactamente el escenario
que el test `altaConcurrenteSinHorarioPrevioLaSerializaElLockDeSalon` demuestra.

Sin `synchronized`, sin `ReentrantLock`, sin advisory locks, sin Redis, sin locks distribuidos.

**Orden global respetado en todos los writers**: `[autorización] → SalonLock → lectura de horario →
validación de programación → persistencia → commit`.

---

## 4. Ports y adapters — dirección de dependencias

```
HorarioOperacionWriter (ubicaciones)
  ├─ SalonLock (ubicaciones) ──────────── SalonRepository                          [hoja]
  ├─ HorarioOperacionRepository                                                    [hoja]
  ├─ Clock
  └─ List<ValidadorImpactoCambioHorarioOperacion>
       ├─ ImpactoTurnosRecurrentesEnHorario (calendario) ─ TurnoInstructorRepository     [hoja]
       └─ ImpactoBloquesEnHorario (programacion) ───────── BloqueProgramacionRepository  [hoja]
```

- **Invariante verificado**: `grep -rn 'import com.feelingpilates.(calendario|programacion)'
  src/main/java/com/feelingpilates/ubicaciones/` → **0 resultados**. `ubicaciones` no importa
  `TurnoInstructor` ni `BloqueProgramacion`.
- **Sin ciclo de beans**: los adapters dependen **sólo de repositorios**, nunca de los services de
  su módulo. Evidencia real: el contexto completo de Spring arranca en
  `HorarioOperacionWritersPersistenciaTest` y `HorarioOperacionConcurrenciaTest`.
- Validación **síncrona**, dentro de la misma transacción. Sin bus de eventos, sin asincronía.

---

## 5. VersionarHorarioOperacion

Input: `VersionarHorario(salonId, diaSemana, efectivoDesde, horaApertura, horaCierre)` — comando
interno, no DTO HTTP.

Validaciones previas: `diaSemana ∈ 0..6` (`DIA_SEMANA_INVALIDO`), `horaCierre > horaApertura`
(`HORA_CIERRE_DEBE_SER_POSTERIOR`), `efectivoDesde` no nulo y `>= LocalDate.now(reloj)` con el
`Clock` central de `config/RelojConfig` (`EFECTIVO_DESDE_EN_EL_PASADO`). No se introduce zona nueva.

Clasificación sobre `bloquearVersionesQueIntersectan(salonId, dia, D, null)` — una sola query, que
devuelve la versión que contiene D más todas las posteriores y nada del pasado:

| Estado | Resultado |
|---|---|
| sin versiones | **alta / reapertura**: insertar `D/NULL`, sin tocar historia; el gap previo se preserva |
| la primera no contiene D | `VERSIONADO_INTERMEDIO_NO_SOPORTADO` |
| alguna versión con `vigenteDesde == D` | `YA_EXISTE_VERSION_EN_ESA_FECHA` (sin UPDATE in-place, sin cerrar en `D-1`) |
| contiene D y hay posteriores | `VERSIONADO_INTERMEDIO_NO_SOPORTADO` |
| contiene D, sin posteriores | **append**: cerrar en `D-1` conservando su `vigenteDesde` + insertar `D/NULL` |

Legacy `NULL/NULL` → `NULL/D-1` + `D/NULL`. Ninguna rama reescribe el pasado ni parte una versión
en tres.

---

## 6. CerrarHorarioOperacion

Input: `CerrarHorario(salonId, diaSemana, efectivoDesde)`.

Semántica ratificada: **dejar de operar recurrentemente ese día desde D hacia +∞**. No es
suspensión temporal y no puede producir reapertura automática.

Fecha pasada se rechaza **antes** de cualquier clasificación temporal. Después: lock + relectura.

**Orden temporal normativo a → b → c** (contractual, no incidental):

| # | Condición | Código |
|---|---|---|
| a | ninguna versión contiene D | `NO_EXISTE_VERSION_VIGENTE_EN_ESA_FECHA` |
| b | `D == vigenteDesde` de esa versión | `CANCELACION_DE_VERSION_NO_SOPORTADA` |
| c | existe `v` con `v.vigenteDesde != null && v.vigenteDesde > D` | `CIERRE_CON_VERSIONES_FUTURAS` |

El orden protege el caso "D en un gap habiendo además una versión futura": cumple (a) y (c) a la
vez y debe responder **(a)**. Test dedicado con `hasMessageNotContaining("CIERRE_CON_VERSIONES_FUTURAS")`.

La existencia de futuro se determina con `vigenteDesde != null && vigenteDesde > D`. **No** se usa
`vigente_hasta IS NULL` como sinónimo de "actual" ni de "futura".

Si pasa todo: `vigenteHasta = D-1` + flush. **No inserta versión sucesora** y **no toca ninguna otra
fila** (si existían futuras, el comando ya fue rechazado). En los tres rechazos no se emite ninguna
sentencia de escritura.

### Código descartado

**`CIERRE_COINCIDE_CON_INICIO_VERSION` NO existe** en el código, no se emite y no se usa como
resultado esperado en ningún test. El único código público para `D == vigenteDesde` es
`CANCELACION_DE_VERSION_NO_SOPORTADA`, con un test que lo afirma explícitamente
(`dIgualAVigenteDesdeNoEmiteElCodigoDescartado`).

---

## 7. Orden de flush UPDATE → flush → INSERT → flush

Todo en **una única** `@Transactional`. Sin `REQUIRES_NEW`, sin commit intermedio, sin dos
transacciones.

```
1. antigua.setVigenteHasta(D-1)
2. saveAndFlush(antigua)      <-- FLUSH EXPLÍCITO
3. nueva = D/NULL
4. saveAndFlush(nueva)        <-- FLUSH EXPLÍCITO, dentro del try de traducción
5. commit
```

Hibernate ordena su `ActionQueue` con `EntityInsertAction` **antes** que `EntityUpdateAction`. Sin
el primer flush, el INSERT de `D/NULL` sale con la versión anterior todavía abierta en PostgreSQL,
se solapa con ella y `ex_horario_operacion_vigencia` lo rechaza — legítimamente. La secuencia no
confía en el orden interno de Hibernate: **lo impone**.

**Verificado empíricamente**: sustituir `saveAndFlush(aCerrar)` por `save(aCerrar)` hace fallar
`versionarSobreLegacyCierraLaViejaEInsertaLaNueva` y `elResolverDistingueLaVersionViejaDeLaNueva`
con `CONFLICTO_VIGENCIA_HORARIO` real de PostgreSQL, más el unitario de orden
`cierraLaVersionViejaConFlushAntesDeInsertarLaNueva`.

### Rollback

El primer flush **no** es un commit. `rollbackTrasElPrimerFlushDejaLaVersionViejaConSuVigenteHastaOriginal`
comprueba el UPDATE aplicado dentro de la transacción, provoca el fallo del INSERT contra el EXCLUDE
y verifica **desde fuera de la transacción fallida** (`JdbcTemplate` tras el rollback) que la fila
vieja conserva su `vigenteHasta` original y que sigue habiendo 2 filas.

---

## 8. Traducción de 23P01

`ConflictoVigenciaHorarioTranslator` recorre la cadena de causas y traduce a
`ConflictException("CONFLICTO_VIGENCIA_HORARIO: …")` **sólo** cuando identifica SQLSTATE `23P01`.
Inspecciona el `ConstraintViolationException` de Hibernate (no la `PSQLException`, para no acoplar
`ubicaciones` al driver); si Hibernate identificó el constraint y **no** es
`ex_horario_operacion_vigencia`, la respuesta es firme: no se traduce. El barrido de `SQLException`
es sólo respaldo para cuando Hibernate no envolvió el error, y `23P01` es `exclusion_violation` con
un único EXCLUDE en el esquema (V45), así que la atribución es inequívoca.

**Nunca** se traduce un `DataIntegrityViolationException` genérico ni otro SQLSTATE: se relanza
intacto. El flush que puede dispararlo ocurre **dentro** del bloque de traducción; dejarlo al commit
del proxy lo lanzaría fuera del método. En el `catch` sólo se lanza: tras un error JDBC la sesión
queda inconsistente y no se sigue usando.

**23P01 es backstop, no control de flujo.** El camino normal produce errores de dominio legibles;
el test de concurrencia `altaConcurrenteSinHorarioPrevio…` afirma explícitamente que el segundo
writer falla con `YA_EXISTE_VERSION_EN_ESA_FECHA` y **no** con `CONFLICTO_VIGENCIA_HORARIO`.

---

## 9. Validación inversa — Política A

Antes de persistir, ambos writers consultan **todos** los `ValidadorImpactoCambioHorarioOperacion`
registrados. Si alguno reporta conflicto se **rechaza** con
`PROGRAMACION_INCOMPATIBLE_CON_HORARIO` más tipo e IDs afectados. Nunca se modifica, recorta,
desactiva ni marca inválida la programación.

**Adapter de Turnos** (`calendario`): usa `buscarRecurrentesPorSalonYDia`, que ya filtra
`activo = true`, `tipo = RECURRENTE`, salón y día. No hizo falta query nueva.
- ABIERTO: conflicto el turno que no cabe en la nueva apertura/cierre.
- CERRADO: cualquier recurrente activo es conflicto.
- Un RECURRENTE no tiene vigencia propia: mientras siga activo se considera aplicable al futuro.
- **Turno EXCEPCION queda fuera de Política A**, con test que lo fija en los dos niveles: el adapter
  no consulta ninguna otra query (`verifyNoMoreInteractions`) y, contra PostgreSQL real, un turno
  EXCEPCION incompatible **no** bloquea el versionado
  (`unTurnoExcepcionIncompatibleNoBloqueaElVersionado`). No se amplió la política para hacer pasar
  ningún test.

**Adapter de Bloques** (`programacion`): query nueva `buscarActivosVigentesDesde`, con predicado
"la vigencia del bloque intersecta `[D, +∞)`" (basta `vigente_hasta is null or vigente_hasta >= D`
porque `vigente_desde` es NOT NULL).
- Sólo se evalúa la porción afectada; **nunca se re-evalúa el tramo anterior a D**. Test de base de
  datos dedicado (`BloqueProgramacionRepositoryVigenciaTest`) que fija la mitad negativa: un bloque
  que termina el día antes de D **no** se devuelve.
- No hace falta análisis de cobertura: tras un append permitido, `[D, +∞)` queda cubierto
  exactamente por la nueva versión.
- Consecuencia aceptada y documentada: también en alta y reapertura un bloque activo que no quepa
  rechaza la operación (lectura fail-closed).

---

## 10. Concurrencia

Infraestructura: PostgreSQL real vía Testcontainers, dos transacciones independientes en dos hilos
con `TransactionTemplate` propio, coordinadas con `CountDownLatch`. **`Thread.sleep` no se usa como
mecanismo de orden en ningún punto.** El único temporizador es una **ventana de observación**
(700 ms) con la que el primer hilo afirma que el segundo **no terminó** mientras retenía el lock:
es una aserción de no-progreso, no sincronización. Guarda de test `SET LOCAL lock_timeout = '20s'`
en el hilo que espera (§61: no es configuración productiva) y `future.get(60s)` para que un
protocolo roto falle en vez de colgar CI.

| Escenario | Resultado |
|---|---|
| Horario ↔ Horario (día con legacy) | la 2.ª espera, relee y falla con `YA_EXISTE_VERSION_EN_ESA_FECHA`; nunca `23P01`; sin solapes |
| **Alta concurrente sin horario previo** | serializadas por el lock de `Salon`; la 2.ª rechaza por dominio, explícitamente **no** por `CONFLICTO_VIGENCIA_HORARIO` |
| Versionar futuro ↔ Cerrar | `Cerrar` reevalúa **después** del lock y rechaza con `CIERRE_CON_VERSIONES_FUTURAS` |
| Horario primero ↔ Bloque | el bloque espera, lee el horario nuevo y es **rechazado**; 0 bloques persistidos |
| Bloque primero ↔ Horario | el versionado espera, la validación inversa ve el bloque y **rechaza**; horario sin cambio, bloque intacto |
| Horario primero ↔ Turno RECURRENTE | el turno espera y es **rechazado**; 0 turnos persistidos |
| Turno primero ↔ Horario | el versionado espera y es **rechazado** identificando el `TURNO_RECURRENTE`; horario sin cambio |

**En ningún orden se alcanza el estado prohibido** "horario nuevo + programación incompatible".

---

## 11. Mutaciones conceptuales (§62)

| # | Mutación | Estado | Detectada por |
|---|---|---|---|
| 1 | Versionar no toma Salon lock | **DETECTADO** (verificado empíricamente) | `VersionarHorarioOperacionTest.elLockDeSalonSeAdquiereAntesDeLeerLasVersiones` + los 7 tests de concurrencia |
| 2 | Cerrar no toma Salon lock | **DETECTADO** (verificado) | `CerrarHorarioOperacionTest.elLockDeSalonSeAdquiereAntesDeLeerLasVersiones` + `versionarFuturoPrimero…` |
| 3 | Bloque crea sin Salon lock | **DETECTADO** (verificado) | `BloqueProgramacionServiceTest.crearBloqueTomaElLockDeSalonAntesDeLeerElHorario` + concurrencia Horario↔Bloque |
| 4 | Turno RECURRENTE crea sin Salon lock | **DETECTADO** (verificado) | `TurnoInstructorServiceCaracterizacionTest.crearRecurrenteTomaElLock…` + concurrencia Horario↔Turno |
| 5 | INSERT antes del flush del UPDATE | **DETECTADO** (verificado) | `versionarSobreLegacyCierraLaViejaEInsertaLaNueva` (23P01 real) |
| 6 | se elimina el primer flush | **DETECTADO** (verificado) | ídem + `cierraLaVersionViejaConFlushAntesDeInsertarLaNueva` |
| 7 | error tras el primer flush deja el UPDATE committed | **DETECTADO** | `rollbackTrasElPrimerFlushDejaLaVersionViejaConSuVigenteHastaOriginal` |
| 8 | Versionar acepta pasado | **DETECTADO** | `efectivoDesdeEnElPasadoSeRechazaAntesDeTocarLaBase` |
| 9 | Versionar permite inserción intermedia | **DETECTADO** | `gapConVersionFuturaRechazaInsercionIntermedia`, `versionQueContieneDConSucesoraPlanificadaRechazaElAppend` |
| 10 | Cerrar gap+futuro devuelve `CIERRE_CON_VERSIONES_FUTURAS` | **DETECTADO** | `dEnGapConVersionFuturaRespondeNoExisteVigenteYNoCierreConFuturas` |
| 11 | Cerrar `D==vigenteDesde` usa otro código | **DETECTADO** | `dIgualAVigenteDesdeEsCancelacionDeVersionNoSoportada` + `…NoEmiteElCodigoDescartado` |
| 12 | Cerrar permite versiones futuras | **DETECTADO** (verificado) | `versionFuturaPlanificadaRechazaElCierre`, `elRechazoPorFuturaNoModificaNingunaFila`, concurrencia |
| 13 | adapter Turnos ignora Turno incompatible | **DETECTADO** (verificado) | `ImpactoTurnosRecurrentesEnHorarioTest` + concurrencia `turnoRecurrentePrimero…` |
| 14 | adapter Bloques ignora Bloque incompatible | **DETECTADO** | `ImpactoBloquesEnHorarioTest` + concurrencia `bloquePrimero…` |
| 15 | se revalida/rechaza por pasado anterior a D | **DETECTADO** | `BloqueProgramacionRepositoryVigenciaTest.bloqueQueTerminaElDiaAntesDeDNoIntersecta` + `soloSeConsultaLaPorcionDelBloqueDesdeLaFechaEfectiva` |
| 16 | Turno EXCEPCION entra en Policy A | **DETECTADO** | `noConsultaTurnosExcepcionNiCancelacion`, `crearExcepcionNoTomaElLockDeSalon`, `unTurnoExcepcionIncompatibleNoBloqueaElVersionado` |
| 17 | cualquier `DataIntegrityViolationException` se traduce | **DETECTADO** | `noTraduceUnDataIntegrityViolationGenerico`, `noTraduceOtraViolacionDeIntegridadConOtroSqlstate`, `noTraduce23P01DeOtroConstraintDistinto` |
| 18 | `23P01` real no se traduce | **DETECTADO** | `insercionSolapadaRealSeTraduceAConflictoDeVigencia` (PostgreSQL real) |
| 19 | Horario vs Bloque puede commitear ambos | **DETECTADO** (verificado) | `horarioPrimero…Bloque…` + `bloquePrimero…` |
| 20 | Horario vs Turno puede commitear ambos | **DETECTADO** (verificado) | `horarioPrimero…Turno…` + `turnoRecurrentePrimero…` |
| 21 | dos altas iniciales no serializadas por Salon | **DETECTADO** (verificado) | `altaConcurrenteSinHorarioPrevioLaSerializaElLockDeSalon` |

**Ninguna queda NO DETECTADO.**

Mutaciones ejecutadas de verdad sobre el código (no sólo razonadas), con la suite en rojo y después
revertidas:

1. `SalonLock` usando `findById` en vez de `bloquearParaActualizar` → **7/7** tests de concurrencia
   FALLAN (cubre 1, 2, 3, 4, 19, 20, 21).
2. `saveAndFlush(aCerrar)` → `save(aCerrar)` → 2 errores en persistencia real + 1 unitario (5, 6).
3. `if (hayFuturas && false)` en `Cerrar` → 3 fallos (12).
4. `.filter(turno -> false)` en el adapter de Turnos → 4 fallos (13).

---

## 12. Tests

| Archivo | Tipo | Nº |
|---|---|---|
| `ubicaciones/servicio/VersionarHorarioOperacionTest` | unitario (mocks) | 20 |
| `ubicaciones/servicio/CerrarHorarioOperacionTest` | unitario (mocks) | 15 |
| `ubicaciones/servicio/ConflictoVigenciaHorarioTranslatorTest` | unitario | 8 |
| `calendario/servicio/ImpactoTurnosRecurrentesEnHorarioTest` | unitario | 7 |
| `programacion/servicio/ImpactoBloquesEnHorarioTest` | unitario | 5 |
| `programacion/repositorio/BloqueProgramacionRepositoryVigenciaTest` | PostgreSQL real | 6 |
| `ubicaciones/HorarioOperacionWritersPersistenciaTest` | PostgreSQL real | 8 |
| `ubicaciones/HorarioOperacionConcurrenciaTest` | PostgreSQL real, 2 TX | 7 |

Tests existentes modificados **sólo para wiring** (nuevo colaborador `SalonLock` en el constructor
o en el slice de contexto), **sin cambiar ninguna expectativa**:
`TurnoInstructorServiceCaracterizacionTest`, `TurnoInstructorServiceHorarioVersionadoTest`,
`BloqueProgramacionServiceTest`, `AutorizacionContextualControllerTest`. A los tres primeros se les
**añadieron** aserciones de protocolo de lock; ninguna aserción previa se relajó.

---

## 13. Verificaciones finales

- **Suite completa**: `./mvnw test` → **337/337 PASS** (baseline 255/255; +82, todos los previos
  siguen verdes).
- **Build**: `./mvnw clean compile` → **BUILD SUCCESS**.
- **Flyway**: V1 → V46 PASS (`ProgramacionPersistenciaTest.flywayMigraDesdeV1HastaV46`, 49
  migraciones aplicadas).
- **JPA**: validate PASS (contexto arranca en todos los `@SpringBootTest`;
  `jpaValidaYRegistraLasEntidadesDeProgramacion`).
- **Migraciones**: `git diff <BASE> -- src/main/resources/db/migration` → **vacío**. V44, V45 y V46
  verificadas individualmente sin cambios.
- **Dependencias**: `grep -rn 'import com.feelingpilates.(calendario|programacion)' ubicaciones/` →
  **0**.
- **HTTP**: SIN CAMBIOS (ningún controller, DTO, routing ni seguridad HTTP).
- **Frontend**: SIN CAMBIOS.

---

## 14. Alcance y estado

Sin condiciones de parada disparadas. Ninguna de las de §41 del encargo se activó:

| Condición | Estado |
|---|---|
| `ubicaciones` necesita importar `calendario`/`programacion` | no — port neutral, 0 imports |
| ciclo de beans Spring | no — contexto completo arranca en los tests de integración |
| el lock compartido no cubre todos los writers relevantes | no — los 3 entry points reales lo toman |
| carrera Horario↔Bloque puede commitear ambos lados | no — probado en ambos órdenes |
| carrera Horario↔Turno puede commitear ambos lados | no — probado en ambos órdenes |
| JPA exige dos transacciones para update/insert | no — una sola `@Transactional` con flush explícito |
| rollback tras el primer flush no es atómico | no — demostrado contra PostgreSQL real |
| hace falta modificar V44–V46 / schema nuevo | no |
| hace falta endpoint HTTP para probar los writers | no — todo se ejercita por service |
| Turno EXCEPCION debe entrar en Policy A para pasar tests | no — se mantiene fuera, con tests que lo fijan |
| la concurrencia sólo se prueba con sleeps | no — latches; el temporizador sólo afirma no-progreso |

### Estado F2B.3b.2: LISTA

Queda para la siguiente fase, sin cambios respecto al diseño §20: `POST` de versionado y de cierre
con `efectivoDesde` explícito, `GET /salones/{id}/horarios/historial` (y con él
`findVersionesOrdenadas`, que sigue diferida por no tener consumidor), DTOs HTTP y autorización de
esos endpoints, migración del cliente web para dejar de enviar `horarios` en el `PUT /salones/{id}`
(hasta entonces sigue vigente `HORARIOS_REQUIEREN_VERSIONADO`), y la decisión sobre el hueco de
Turno EXCEPCION y `SalonHorarioExcepcionService.guardar`.

Deuda P2 pendiente y **no** corregida aquí por no ampliar alcance: el Javadoc de
`SalonService.horariosVigentes` sigue mencionando el UNIQUE retirado, y `crearHorariosIniciales`
sigue generando filas `NULL/NULL` para salones nuevos (observación, no defecto).

---

## Resumen declarativo

```
Base:                                edea665e0c707751c6aca28ded4c497deeb89c32
Salon lock:                          IMPLEMENTADO
Ports inversos:                      IMPLEMENTADOS
Adapter Turnos:                      IMPLEMENTADO
Adapter Bloques:                     IMPLEMENTADO
VersionarHorarioOperacion:           IMPLEMENTADO
CerrarHorarioOperacion:              IMPLEMENTADO
Update→flush→insert→flush:           PROBADO
Rollback primer flush:               PROBADO
23P01:                               TRADUCIDO
Policy A:                            IMPLEMENTADA
Lock Bloque:                         IMPLEMENTADO
Lock Turno recurrente:               IMPLEMENTADO
Concurrencia Horario↔Horario:        PROBADA
Concurrencia Horario↔Bloque:         PROBADA
Concurrencia Horario↔Turno:          PROBADA
Alta sin horario concurrente:        PROBADA
Migrations:                          SIN CAMBIOS
HTTP:                                SIN CAMBIOS
Frontend:                            SIN CAMBIOS
Tests:                               337/337 PASS
Build:                               PASS
Flyway:                              V1→V46 PASS
JPA:                                 PASS
F2B.3b.2:                            LISTA
```
