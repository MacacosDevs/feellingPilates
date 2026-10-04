# Fase 2B.3b.0 — Diseño de writers, validación inversa y concurrencia

## Base

Branch `operacion/horario-versionado-persistencia`, HEAD `a6335be22ab8c646f07ff515c417be0f6ff98ce0`,
working tree limpio, sin divergencia con `origin`. Persistencia vigente: V44 `btree_gist`,
V45 `ex_horario_operacion_vigencia`, V46 `DROP UNIQUE`.

Tarea de **diseño**: sin código productivo, migraciones, endpoints ni tests. Único archivo tocado,
este documento. Checkpoint autoritativo: `auditoria/fase-2b-diseno-versionado-horario.md`
(§8, §9, §10, §16, §23); aquí no se reabre ninguna decisión cerrada, se aterrizan contra el código
real y se marca lo que quedó sin decidir.

**Revisiones de este documento**

| Revisión | Cambio |
|---|---|
| F2B.3b.0 | Diseño inicial. Un bloqueador abierto: cierre con versiones futuras planificadas |
| F2B.3b.0.1 | Bloqueador **cerrado por ratificación**: `CIERRE_CON_VERSIONES_FUTURAS` (§9, §10, §18, §21). Sin bloqueadores restantes |
| F2B.3b.0.2 | Se elimina la alternativa contradictoria para cierre en inicio de versión: el único código público es `CANCELACION_DE_VERSION_NO_SOPORTADA` |

---

## 1. Executive Summary

Diseño **completo y sin bloqueadores**. El único punto que quedaba abierto —qué hace
`CerrarHorarioOperacion` cuando existen versiones futuras planificadas— fue ratificado en
F2B.3b.0.1 y está incorporado (§9, §10, §21).

Resuelto y verificado contra el código y contra PostgreSQL 16.14:

- **Inventario cerrado y pequeño** (§2): un solo punto de creación de `HorarioOperacion`, uno de
  `BloqueProgramacion` (sin controller), tres escrituras de `TurnoInstructor`. **No existe ninguna
  operación de reactivación** y **ninguna entidad puede cambiar de salón**, lo que elimina de raíz
  el riesgo de deadlock multi-salón (§16).
- **Dependencias sin ciclo** (§4): port en `ubicaciones`, adapters en `calendario`/`programacion`
  que dependen **sólo de repositorios**. `ubicaciones` no importa nada de los otros dos módulos.
- **Verificado que `FOR UPDATE` sobre `horario_operacion` no basta** (§17-C): con dos sesiones
  reales sobre un día **sin filas de horario**, la segunda esperó **19.55 s** por el lock de la
  fila `Salon`. Sin ese lock padre no habría serialización, sólo el EXCLUDE como red final.
- **El hazard de flush es determinista, no teórico** (§11): Hibernate 7.4.1.Final declara
  `EntityInsertAction` **antes** que `EntityUpdateAction` (verificado con `javap` sobre el jar del
  proyecto), y en PostgreSQL el INSERT de `D/NULL` con el UPDATE pendiente falla con `23P01`
  (verificado). La secuencia `UPDATE → flush → INSERT → flush` no confía en el orden de Hibernate:
  lo impone. El flush intermedio **no** compromete la atomicidad (verificado el rollback íntegro).
- **Una sola query existente clasifica todos los edge cases** (§10):
  `findVersionesQueIntersectan(salonId, dia, D, null)` devuelve "la versión que contiene D más
  todas las posteriores". `findVersionesOrdenadas` **no hace falta** y sigue diferida.
- **Semántica de cierre ratificada** (§9): `CerrarHorarioOperacion` significa "dejar de operar
  recurrentemente ese día desde `efectivoDesde` hacia +∞", así que **no puede** producir un cierre
  temporal seguido de una reapertura automática causada por una versión futura ya planificada. Si
  existe cualquier versión del mismo salón/día con `vigenteDesde > efectivoDesde`, el comando
  **rechaza** con `CIERRE_CON_VERSIONES_FUTURAS` y **no toca** esas versiones. Los dos comandos
  iniciales quedan así igual de conservadores: ninguno reorganiza el futuro planificado (§21).

**DISEÑO COMPLETADO / F2B.3b.1 LISTA.** Sin bloqueadores: todo está especificado a nivel de clases,
queries, orden transaccional y tests.

---

## 2. Inventario de writers

Barrido repo-wide de `src/main/java`, incluidos INSERT/UPDATE/DELETE nativos y `@Modifying`.
**No hay ningún camino de escritura fuera de los services listados**, ni JDBC ni SQL nativo de
mutación sobre las tres tablas.

### `HorarioOperacion`

| Ruta | Método | `@Transactional` | Repositorio | ¿Crea incompatibilidad? |
|---|---|---|---|---|
| `ubicaciones/servicio/SalonService.java:139` | `crearHorariosIniciales` (desde `crear`) | sí, de clase | `HorarioOperacionRepository.save` | **No**: el salón se inserta en la misma TX, ninguna otra la ve, y no hay programación asociada |
| `ubicaciones/servicio/SalonService.java:104` | `actualizar` → `validarHorariosSinCambios` | sí, de clase | sólo lectura | **No**: cuarentena F2B.2, un cambio real se rechaza con `HORARIOS_REQUIEREN_VERSIONADO` |

`deleteBySalonId` existe pero **no tiene llamador productivo** (sólo aserciones `never()` en
`SalonServiceTest`). No hay endpoint de borrado de salón ni cascada JPA desde `Salon`.

Observación fuera de alcance: `crearHorariosIniciales` sigue insertando `NULL/NULL`, es decir la
forma *legacy* no es sólo histórica. Se registra como P2 (§19).

### `BloqueProgramacion`

| Ruta | Método | `@Transactional` | Repositorio | ¿Crea incompatibilidad? |
|---|---|---|---|---|
| `programacion/servicio/BloqueProgramacionService.java:56` | `crearBloque` | sí, de clase | `BloqueProgramacionRepository.save` | **Sí**: inserta bloque activo validado contra el horario leído en esa TX |

**No existe** `actualizarBloque`, ni desactivación, ni reactivación, ni cambio de salón: la entidad
sólo se escribe en el alta. El service **no tiene controller** (hoy sólo lo alcanzan los tests),
lo que reduce el riesgo operativo pero **no** justifica excluirlo: el lock debe estar antes de que
exista el endpoint. `crearAsignacion` no toca el horario (decisión cerrada 12) y no participa.

### `TurnoInstructor`

| Ruta | Método | `@Transactional` | Repositorio | ¿Crea incompatibilidad? |
|---|---|---|---|---|
| `calendario/servicio/TurnoInstructorService.java:157` | `crear` RECURRENTE | sí, de clase | `TurnoInstructorRepository.save` | **Sí**: valida contra `findVersionesQueIntersectan(hoy, +∞)` y persiste |
| `…:157` | `crear` EXCEPCION | sí | ídem | Lee `HorarioEfectivoSalon` de una fecha — fuera de Política A (§13) |
| `…:157` | `crear` CANCELACION | sí | ídem | **No**: no valida horario (marcador de día completo) |
| `…:204` | `actualizarTurno` (sólo RECURRENTE) | sí | ídem | **Sí**: mueve día y horas dentro del **mismo** salón |
| `…:168` | `eliminar` | sí | ídem | **No**: `activo = false`, sólo retira restricciones |

`actualizarTurno` rechaza todo lo que no sea RECURRENTE y toma el salón de `turno.getSalon()`;
`ActualizarTurnoRequest` no lleva `salonId`. **El salón de un turno no puede cambiarse.**

### Activación / reactivación

**No existen.** `activo` se pone a `true` una sola vez al construir la entidad y a `false` en
`eliminar`; ninguna ruta reactiva una fila desactivada. La pregunta del encargo queda contestada:
no hay tal camino y no hay nada que proteger. Si se añadiera, sería un writer que vuelve visible
programación activa y **debería** entrar en el protocolo (§20).

---

## 3. Grafo de dependencias actual

Beans reales, leídos de los constructores:

```
ubicaciones
  SalonService                 -> SalonRepository, HorarioOperacionRepository, TipoActividadRepository,
                                  TipoRecursoRepository, SalonRecursoRepository, MunicipioRepository, Clock
  HorarioOperacionResolver     -> HorarioOperacionRepository
  HorarioEfectivoSalon         -> SalonHorarioExcepcionRepository, HorarioOperacionResolver
  SalonHorarioExcepcionService -> SalonHorarioExcepcionRepository, SalonRepository, AutorizadorSalon
calendario
  TurnoInstructorService       -> TurnoInstructorRepository, TurnoInstructorAsignacionRepository,
                                  UsuarioRepository, SalonRepository, HorarioOperacionRepository,
                                  TipoActividadRepository, HorarioEfectivoSalon, AutorizadorSalon, Clock
  ReservaService               -> ..., TurnoInstructorRepository (sólo lectura)
programacion
  BloqueProgramacionService    -> BloqueProgramacionRepository, AsignacionRepository, SalonRepository,
                                  HorarioOperacionRepository, UsuarioRepository, TipoActividadRepository
```

Dirección actual: `calendario → ubicaciones` y `programacion → ubicaciones`. **Ninguna clase de
`ubicaciones` importa `calendario` ni `programacion`** (verificado por grep de imports).

---

## 4. Diseño de ports

El writer vive en `ubicaciones` y necesita preguntar por programación de los otros dos módulos. La
inversión se hace con un port declarado en `ubicaciones`:

```
ubicaciones/dominio/ValidadorImpactoCambioHorarioOperacion   (interface)
    List<ConflictoProgramacion> evaluar(CambioHorarioOperacion cambio);

ubicaciones/dominio/ConflictoProgramacion                    (record neutral)
    Origen origen   // { TURNO_RECURRENTE, BLOQUE_PROGRAMACION }
    UUID   id
    String detalle  // texto corto para el mensaje; nunca la entidad
```

El writer inyecta `List<ValidadorImpactoCambioHorarioOperacion>`; Spring resuelve en runtime. Si la
lista llega vacía (contexto de test recortado) el writer **no valida nada**, así que los tests que
ejerciten Política A deben declarar los adapters explícitamente (§18).

Adapters — beans propios, **no** los services existentes:

```
calendario/servicio/ImpactoTurnosRecurrentesEnHorario   -> TurnoInstructorRepository
programacion/servicio/ImpactoBloquesEnHorario           -> BloqueProgramacionRepository
```

Grafo final:

```
HorarioOperacionWriter (ubicaciones)
  ├─ SalonLock (ubicaciones) ──────────── SalonRepository            [hoja]
  ├─ HorarioOperacionRepository                                      [hoja]
  ├─ Clock                                                           [hoja]
  └─ List<ValidadorImpactoCambioHorarioOperacion>
       ├─ ImpactoTurnosRecurrentesEnHorario (calendario) ─ TurnoInstructorRepository      [hoja]
       └─ ImpactoBloquesEnHorario (programacion) ───────── BloqueProgramacionRepository   [hoja]
```

**Sin ciclo Spring**: todo camino desde el writer termina en un repositorio, que no depende de
ningún service. En particular el adapter de `calendario` **no** depende de `TurnoInstructorService`,
así que la cadena `writer → TurnoInstructorService → HorarioEfectivoSalon → …` nunca se forma. Esa
restricción es deliberada: llamar a los services arrastraría autorización y validaciones ajenas al
impacto.

`BloqueProgramacionService` y `TurnoInstructorService` pasan a depender de `SalonLock`
(`programacion/calendario → ubicaciones`, dirección permitida); `SalonLock` sólo depende de
`SalonRepository`, así que tampoco cierra ciclo.

---

## 5. `CambioHorarioOperacion` neutral

Tipo de `ubicaciones/dominio`, sin ninguna referencia a Turno ni a Bloque:

```
record CambioHorarioOperacion(UUID salonId, short diaSemana, LocalDate efectivoDesde,
                              Estado estado,        // ABIERTO | CERRADO
                              LocalTime horaApertura, LocalTime horaCierre) {
    static CambioHorarioOperacion abierto(salonId, diaSemana, efectivoDesde, apertura, cierre)
    static CambioHorarioOperacion cerrado(salonId, diaSemana, efectivoDesde)
    boolean admite(LocalTime inicio, LocalTime fin)   // false si CERRADO
}
```

Invariante en el constructor compacto, como en `HorarioEfectivo`: sólo `ABIERTO` porta horas. **No
se reutiliza `HorarioEfectivo`**: ese tipo modela "qué pasa una fecha concreta" y lleva
`Origen.EXCEPCION`, semántica que aquí no aplica y confundiría al adapter.

Sin evento, sin bus, sin publicación asíncrona: la validación es **síncrona y dentro de la misma
transacción** del writer; si un adapter falla, se revierte todo.

---

## 6. Lock compartido sobre `Salon`

Único mecanismo reutilizable, en `ubicaciones`:

```
SalonRepository
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Salon s where s.id = :salonId")
    Optional<Salon> bloquearParaActualizar(@Param("salonId") UUID salonId);

SalonLock
    @Transactional(propagation = Propagation.MANDATORY)
    public void adquirir(UUID salonId) {
        salonRepository.bloquearParaActualizar(salonId)
            .orElseThrow(() -> new ResourceNotFoundException("Salón no encontrado"));
    }
```

- **`Propagation.MANDATORY`, no `REQUIRED`**: el lock debe vivir en la transacción del llamante. Si
  alguien lo invoca fuera de una transacción debe romper ruidosamente, no abrir una transacción
  propia, tomar el lock y soltarlo — que es el fallo silencioso que haría inútil el protocolo.
- **Falla si el salón no existe**, con `ResourceNotFoundException`. Es además la única comprobación
  de existencia que necesitan los writers de horario: sobra un `findById` adicional.
- `@Lock` sobre **JPQL** sí aplica `FOR UPDATE`; sobre `nativeQuery = true` **no es fiable** (JPA no
  define `setLockMode` para queries nativas). Por eso esta query es JPQL, y donde haga falta
  `FOR UPDATE` sobre una nativa existente (§8) se escribe `for update` literal en el SQL.
- Sin advisory locks, sin locks distribuidos, sin locks en memoria: el `FOR UPDATE` vive en la base
  y funciona igual con varias instancias.

---

## 7. Orden global de locks

```
[autorización]                    -- barata, antes del lock: no retener por peticiones sin permiso
1. SalonLock.adquirir(salonId)    -- SELECT ... FOR UPDATE sobre salon
2. lectura de HorarioOperacion    -- ... for update sobre las versiones del par (salon, dia)
3. lectura/validación de programación dependiente
4. persistencia (con los flush de §11)
5. commit                          -- el lock se libera aquí, nunca antes
```

| Comando | Lock | Motivo |
|---|---|---|
| `VersionarHorarioOperacion` | **sí** | modifica el horario |
| `CerrarHorarioOperacion` | **sí** | modifica el horario |
| `BloqueProgramacionService.crearBloque` | **sí** | vuelve visible programación activa nueva |
| `TurnoInstructorService.crear` RECURRENTE | **sí** | ídem |
| `TurnoInstructorService.actualizarTurno` | **sí** | mueve día/horas de un recurrente activo |
| `crear` EXCEPCION | no | fuera de Política A (§13) |
| `crear` CANCELACION | no | no lee ni afecta el horario |
| `eliminar` | no | sólo desactiva |
| `crearAsignacion` | no | contención transitiva vía Bloque (decisión 12) |
| `SalonService.crear` → `crearHorariosIniciales` | no | el salón aún no es visible para otras TX |
| `SalonService.actualizar` | no | cuarentena: no escribe horarios |
| `SalonHorarioExcepcionService.*` | no | decisión cerrada 11 |

Sobre las desactivaciones: `eliminar` sin lock introduce una carrera **benigna** — si A versiona y
lee un turno todavía activo mientras B lo desactiva, A rechaza por un turno a punto de desaparecer.
Es un falso negativo; el operador reintenta. El lock no compraría integridad, sólo contención.

Detalle de secuencia en `actualizarTurno`: el `salonId` sólo se conoce tras cargar el turno.
Cargarlo antes del lock es correcto porque el salón **no puede cambiar** (§2), así que esa lectura
es de identidad, no de estado de la invariante; el lock se toma inmediatamente después y **antes**
de `validarDentroDeHorarioSalon` y `validarSinTraslape`.

---

## 8. Writer `VersionarHorarioOperacion`

Comando en `ubicaciones/servicio`, `@Transactional` de clase.
Input: `record VersionarHorario(UUID salonId, short diaSemana, LocalDate efectivoDesde, LocalTime horaApertura, LocalTime horaCierre)`.

| Regla | Error | Tipo |
|---|---|---|
| salón existe | — | `ResourceNotFoundException` (404) |
| `diaSemana` en 0..6 | `DIA_SEMANA_INVALIDO` | `ValidacionException` |
| `horaCierre > horaApertura` | `HORA_CIERRE_DEBE_SER_POSTERIOR` | `ValidacionException` |
| `efectivoDesde >= fechaNegocio` | `EFECTIVO_DESDE_EN_EL_PASADO` | `ValidacionException` |

`fechaNegocio = LocalDate.now(reloj)` con el bean `Clock` de `config/RelojConfig` (F2B.2). **No se
introduce zona nueva**: sigue `systemDefaultZone()`, con la deuda ya documentada allí.

```
1. validar entrada (día, horas)
2. SalonLock.adquirir(salonId)
3. versiones = bloquearVersionesQueIntersectan(salonId, dia, D, null)   // con `for update`
4. clasificar edge case (§10): alta/reapertura | append | rechazo
5. validación inversa Política A con CambioHorarioOperacion.abierto(...)  // §13, §14
6. si append: antigua.vigenteHasta = D-1 ; saveAndFlush(antigua)        // §11
7. persistir nueva (D, NULL) ; saveAndFlush(nueva)                      // §11, try/catch de §12
8. commit
```

El paso 5 va **antes** de cualquier escritura: un rechazo de Política A no persiste nada y ni
siquiera emite el UPDATE.

**Estado temporal a leer.** Basta **una** query, y ya existe:
`findVersionesQueIntersectan(salonId, diaSemana, D, null)`. Con `hasta = null` el SQL se reduce a
`vigente_hasta is null or vigente_hasta >= D`, ordenado por `vigente_desde asc nulls first`: devuelve
**la versión que contiene D (si existe) más todas las posteriores, y nada del pasado**. Verificado en
PostgreSQL 16.14 con el SQL literal del repositorio, incluido el `cast(:hasta as date)`. Para el paso
3 se añade una variante con `for update` literal (§6); no se traen otros días ni el salón entero.

**`findVersionesOrdenadas` NO se añade en F2B.3b.1**: el writer no la necesita, la clasificación
completa sale de la query anterior. Sigue diferida al endpoint de historial, como la dejó §16.

**Consistencia con `Cerrar`.** `Versionar` no admite inserción intermedia ni reorganización del
futuro planificado: casos C, D y E se rechazan con código estable y ninguna versión posterior se
toca. `Cerrar` sigue exactamente la misma política (§9, §21 decisión 22). Los dos comandos iniciales
operan sólo sobre el borde abierto del historial; manipular planificación existente son comandos
separados (§20).

---

## 9. Writer `CerrarHorarioOperacion`

Input: `record CerrarHorario(UUID salonId, short diaSemana, LocalDate efectivoDesde)`.

### Semántica (ratificada en F2B.3b.0.1)

`CerrarHorarioOperacion(salonId, diaSemana, D)` significa **"dejar de operar recurrentemente ese
día desde `D` hacia +∞"**. De ahí se deriva la regla que cierra el caso que quedaba abierto: el
comando **no puede** producir un cierre temporal más una reapertura automática causada por una
versión futura ya planificada. Si tras el cierre el día volviera a abrir en una fecha posterior, lo
ejecutado no sería lo que el comando afirma.

Por tanto: **si existe cualquier `HorarioOperacion` del mismo salón/día con
`vigenteDesde > efectivoDesde`, el comando RECHAZA con `CIERRE_CON_VERSIONES_FUTURAS`.** Esas
versiones futuras **no se borran, no se recortan, no se cancelan y no se alteran** de ninguna forma
automática. Reorganizar planificación futura es otra operación, fuera de F2B.3b.1 (§20, §21).

### Secuencia

```
1. validar diaSemana 0..6 y efectivoDesde >= fechaNegocio  -> EFECTIVO_DESDE_EN_EL_PASADO
2. SalonLock.adquirir(salonId)
3. versiones = bloquearVersionesQueIntersectan(salonId, dia, D, null)
4. clasificar, en este orden exacto:
     a) ninguna versión contiene D                 -> NO_EXISTE_VERSION_VIGENTE_EN_ESA_FECHA
        (lista vacía, o la primera tiene vigenteDesde > D: D cae en un gap)
     b) D == version.vigenteDesde                  -> CANCELACION_DE_VERSION_NO_SOPORTADA
     c) existe v con v.vigenteDesde != null
        && v.vigenteDesde > D                      -> CIERRE_CON_VERSIONES_FUTURAS
5. validación inversa Política A con CambioHorarioOperacion.cerrado(...)
6. version.vigenteHasta = D-1 ; saveAndFlush(version)
7. commit   -- NO se inserta versión sucesora, NO se toca ninguna otra fila
```

**El orden a → b → c es normativo**, no incidental: primero las comprobaciones sobre la versión a la
que apunta el comando, después la del estado del día alrededor. Lo fija el propio caso ratificado
"D en gap con versión futura", que se rechaza como `NO_EXISTE_VERSION_VIGENTE_EN_ESA_FECHA` y no
como `CIERRE_CON_VERSIONES_FUTURAS` pese a que ambas condiciones se cumplen. Con ese orden, cada
estado de entrada produce **un** código estable y los tests son deterministas.

### Detección de versiones futuras

La existencia de futuro se determina sobre las versiones ordenadas del mismo salón/día como
`v.vigenteDesde != null && v.vigenteDesde > D`. **No se usa `vigente_hasta IS NULL` como sinónimo
de "actual" ni de "futura"**: es el mismo predicado inseguro que ya descartó la decisión cerrada 3
del checkpoint F2B.0.1 — una fila `NULL/NULL` legacy tiene `vigente_hasta IS NULL` y no es futura,
y una versión futura acotada (`[Y, Z]`) no lo tiene y sí lo es. La guarda `!= null` importa: la fila
legacy con `vigenteDesde = NULL` es `-∞` y nunca es posterior a `D`.

### Notas

Nunca hay no-op silencioso: los tres rechazos de clasificación (a, b, c) son explícitos y con
código estable. `D == vigenteDesde` se rechaza porque
`vigenteHasta = D-1` violaría `chk_horario_operacion_vigencia` y, semánticamente, sería cancelar la
versión completa — otra operación (`CancelarVersionHorarioOperacion`, §20). Se conserva el código
`CANCELACION_DE_VERSION_NO_SOPORTADA` ya usado en este documento: es el único código de dominio
público para este caso y no admite alias ni alternativas de implementación. En esta rama no se
ejecuta `vigenteHasta = D-1`, no se borra ni modifica la versión y no se crea otra versión.

En el camino permitido sólo hay un UPDATE, así que no aplica el hazard de §11; el `saveAndFlush` se
mantiene para que un eventual `23P01` sea capturable dentro del método (§12) y no escape en el
commit del proxy. En cualquiera de los tres rechazos de clasificación **no se emite ninguna sentencia de escritura**:
la clasificación ocurre antes del paso 6 y antes incluso de la validación inversa.

---

## 10. Edge cases

Clasificación a partir de `versiones = findVersionesQueIntersectan(salonId, dia, D, null)`, con
`contieneD(v) ≡ v.vigenteDesde == null || v.vigenteDesde <= D`.

| Estado observado | Caso §9 checkpoint | Versionar | Cerrar |
|---|---|---|---|
| `versiones` vacía | A (alta) / J (reapertura) | insertar `D/NULL`, sin tocar historia | `NO_EXISTE_VERSION_VIGENTE_EN_ESA_FECHA` |
| 1 versión, `contieneD`, `D > vigenteDesde` | B (append) | cerrar en `D-1` + insertar `D/NULL` | cerrar en `D-1` |
| 1+ versiones, la primera **no** contiene D | D (gap con futura) | `VERSIONADO_INTERMEDIO_NO_SOPORTADO` | `NO_EXISTE_VERSION_VIGENTE_EN_ESA_FECHA` |
| `D == vigenteDesde` de alguna versión | C | `YA_EXISTE_VERSION_EN_ESA_FECHA` | `CANCELACION_DE_VERSION_NO_SOPORTADA` |
| >1: contiene D **y** hay posteriores | E | `VERSIONADO_INTERMEDIO_NO_SOPORTADO` | `CIERRE_CON_VERSIONES_FUTURAS` |
| `D < fechaNegocio` | F | `EFECTIVO_DESDE_EN_EL_PASADO` | `EFECTIVO_DESDE_EN_EL_PASADO` |
| `apertura >= cierre` | G/H | `HORA_CIERRE_DEBE_SER_POSTERIOR` | n/a |

**A y J no se distinguen y no hace falta**: el tratamiento es idéntico (insertar `D/NULL` sin tocar
nada anterior) y la query no devuelve historia pasada. El gap histórico previo queda permitido,
como fija la decisión cerrada 20.

Verificado en PostgreSQL 16.14 con la SQL literal del repositorio. Día con `[-∞, 2026-03-31]` y
`[2026-07-01, 2026-07-31]`: `D = 2026-05-01` devuelve sólo la versión de julio (`vigenteDesde > D`
⇒ caso D, rechazo); `D = 2026-09-01` devuelve 0 filas (⇒ reapertura) y el `INSERT` de
`2026-09-01/NULL` entra sin tocar la historia. Con una versión abierta (`vigenteHasta = NULL`),
cualquier `D` posterior cae en append, no en reapertura — correcto.

**No se implementa split de una versión en tres.** Ninguna rama reescribe el pasado.

### Escenarios de cierre ratificados en F2B.3b.0.1

| # | Estado del día | `Cerrar(D)` | Resultado |
|---|---|---|---|
| 1 | `NULL / NULL` (legacy, sin futuro) | `D` | **permitido** → `NULL / D-1` |
| 2 | A `NULL → 31-ago`, B `01-sep → NULL` | `15-sep` | **permitido** → A intacta, B `01-sep → 14-sep`; después, sin versión vigente |
| 3 | A `NULL → 31-ago`, B `01-sep → NULL` | `25-ago` | **RECHAZAR** `CIERRE_CON_VERSIONES_FUTURAS`; no se produce el cierre temporal 25–31 ago |
| 4 | A `NULL → 31-ago`, B `01-sep → NULL` | `01-sep` | **RECHAZAR** `CANCELACION_DE_VERSION_NO_SOPORTADA`; no se produce `B.vigenteHasta = 31-ago` |
| 5 | A `NULL → 31-ago`, B `15-sep → NULL` | `05-sep` | **RECHAZAR** `NO_EXISTE_VERSION_VIGENTE_EN_ESA_FECHA`; el gap existente no se transforma |

El caso 2 es exactamente el caso 3 con `D` situado **dentro** de la última versión en vez de dentro
de una anterior: la diferencia entre permitir y rechazar es la existencia de versiones con
`vigenteDesde > D`, nada más. En los casos 3, 4 y 5 **ninguna fila se modifica** (§18, test 6).

---

## 11. Flush / EXCLUDE

**Evidencia 1 — orden interno de Hibernate.** `javap` sobre `hibernate-core-7.4.1.Final.jar` (el que
resuelve el `pom.xml`), `org.hibernate.engine.spi.ActionQueue$OrderedActions`:

```
OrphanCollectionRemoveAction, OrphanRemovalAction,
EntityInsertAction, EntityUpdateAction,          <-- INSERT antes que UPDATE
QueuedOperationCollectionAction, CollectionRemoveAction,
CollectionUpdateAction, CollectionRecreateAction, EntityDeleteAction
```

Es el mismo fenómeno que el repo ya documenta en `TurnoInstructorService.reemplazarAsignaciones`
("encola los INSERT antes que los DELETE").

**Evidencia 2 — PostgreSQL.** Esquema real (V43 CHECK + V45 EXCLUDE), fila legacy `NULL/NULL`:

```
BEGIN;
  INSERT ... (2026-09-01, NULL);      -- ERROR 23P01: conflicting key value violates ...
  UPDATE ... SET vigente_hasta='2026-08-31' WHERE id = legacy;   -- nunca se ejecuta
ROLLBACK;                              -- final: 1 fila, la legacy intacta NULL/NULL

BEGIN;
  UPDATE ... SET vigente_hasta='2026-08-31' WHERE id = legacy;   -- OK
  INSERT ... (2026-09-01, NULL);                                  -- OK
COMMIT;                                -- final: NULL/2026-08-31 + 2026-09-01/NULL
```

Además `ex_horario_operacion_vigencia` es `condeferrable = f, condeferred = f`: **no es diferible**,
así que aplazar la comprobación al commit no es una salida — y V45 no se toca.

**Secuencia productiva obligatoria**, en **una única** `@Transactional`:

```
1. antigua.setVigenteHasta(D.minusDays(1));
2. horarioOperacionRepository.saveAndFlush(antigua);   // FLUSH explícito
3. nueva = ...;
4. horarioOperacionRepository.saveAndFlush(nueva);     // FLUSH explícito, dentro del try de §12
5. commit
```

Se usa `saveAndFlush` y no dirty checking: **no depende del orden de la `ActionQueue`, lo impone**, y
sigue siendo determinista si Hibernate lo cambia en una versión futura. Sin `REQUIRES_NEW`, sin
segunda transacción.

**Atomicidad con flush intermedio — verificado.** Día con `[-∞, 2026-11-30]` y `[2026-12-01, +∞)`;
en una TX: `UPDATE` de la primera a `2026-09-30` (visible dentro de la TX), luego `INSERT` de
`2026-10-01/NULL` que choca con la futura → `23P01` → rollback. Leído después: la primera conserva
`vigente_hasta = 2026-11-30`. El flush intermedio **no** compromete la atomicidad. (Ese escenario
concreto lo rechaza antes el paso 4 del writer; aquí sirve como fixture de fallo que no exige
romper el diseño productivo — §18.)

---

## 12. Traducción de `23P01`

`ex_horario_operacion_vigencia` sigue siendo la **autoridad final** de no-solape. El lock serializa
a los writers correctos, pero no garantiza que toda escritura futura pase por el service; el
EXCLUDE sí. **No se sustituye por prechecks.**

Cadena real observada:

```
DataIntegrityViolationException
  └─ org.hibernate.exception.ConstraintViolationException   (getSQLState(), getConstraintName())
       └─ org.postgresql.util.PSQLException                 (SQLSTATE 23P01, ServerErrorMessage.getConstraint())
```

Verificado que PostgreSQL reporta `SQLSTATE 23P01` con `CONSTRAINT NAME:
ex_horario_operacion_vigencia`.

```
recorrer la cadena de causas:
  si hay un ConstraintViolationException con "23P01".equals(getSQLState())
     y ("ex_horario_operacion_vigencia".equals(getConstraintName()) || getConstraintName() == null)
  -> ConflictException("CONFLICTO_VIGENCIA_HORARIO: ...")
  en cualquier otro caso -> relanzar la excepción original sin tocarla
```

Se inspecciona el `ConstraintViolationException` de Hibernate y no `PSQLException`, para no acoplar
`ubicaciones` al driver; fallback: recorrer hasta un `java.sql.SQLException` y mirar
`getSQLState()`. **Nunca se traduce un `DataIntegrityViolationException` genérico**: sólo `23P01`, y
preferentemente con el constraint esperado.

`ConflictException` ya está mapeada a **409** por `GlobalExceptionHandler`. Nota: hoy un
`DataIntegrityViolationException` sin traducir tampoco daría 500 — el handler ya lo mapea a 409 con
mensaje genérico; la traducción aporta un **código estable**, no evita un 500.

**El flush del §11 es lo que hace capturable el error**: si se dejara al commit del proxy de Spring,
la excepción ocurriría fuera del método y el `try/catch` no la vería. Por eso
`persist → flush explícito → catch → traducir`. En el `catch` sólo se lanza; **no se sigue usando la
sesión**, que tras un error JDBC queda inconsistente. Lanzar una `RuntimeException` propia mantiene
el rollback por defecto de Spring.

---

## 13. Adapter de Turnos (`calendario`)

```
ImpactoTurnosRecurrentesEnHorario implements ValidadorImpactoCambioHorarioOperacion
    -> TurnoInstructorRepository.buscarRecurrentesPorSalonYDia(salonId, diaSemana)
```

Esa query **ya existe** y filtra exactamente lo necesario (`activo = true`, `tipo = 'RECURRENTE'`,
salón, día). **No hace falta ninguna query nueva en `calendario`.**

- **Versionar (ABIERTO)**: cada turno debe cumplir
  `!horaInicio.isBefore(apertura) && !horaFin.isAfter(cierre)`; el que no quepa se acumula como
  `ConflictoProgramacion(TURNO_RECURRENTE, turno.getId(), "08:00-09:00")`.
- **Cerrar**: cualquier turno RECURRENTE activo del salón/día es conflicto, sin más análisis.

`TurnoInstructor` RECURRENTE **no recibe vigencia propia**: sigue siendo compatibility surface, y por
eso todo recurrente activo se considera aplicable hacia el futuro. El error identifica los IDs.
**Nunca se degrada ni se desactiva el turno**: se rechaza el cambio de horario.

### Turno EXCEPCION — análisis sin ampliar la Política A

Un turno EXCEPCION vive en una fecha concreta y se validó contra `HorarioEfectivoSalon` al crearse.
Un `Versionar` con `D` anterior a esa fecha podría dejarlo fuera sin que nadie lo detecte.

¿Es una **contradicción transaccional nueva**? **No**, y es verificable en el código: la invariante
"todo turno EXCEPCION activo cabe en el horario efectivo de su fecha" **ya no se mantiene hoy**.
`SalonHorarioExcepcionService.guardar` graba una excepción `cerrado = true` para cualquier fecha
**sin consultar turnos existentes**, y es una ruta viva con controller. El writer de horario no
rompe una invariante que el sistema mantenga: entra en un hueco preexistente y deliberadamente
fuera de alcance (decisión cerrada 11).

Consecuencia: **no se añade lock ni validación inversa a EXCEPCION en F2B.3b.1**. Añadir el lock sin
ampliar la Política A daría falsa sensación de protección — el resultado serie y el concurrente
serían idénticos, porque en ninguno se comprueba nada. **No es un bloqueador**; queda como hueco
conocido (§20).

---

## 14. Adapter de Bloques (`programacion`)

Query nueva (la existente `findBySalonIdAndDiaSemanaAndActivoTrueOrderByHoraInicio` traería también
historia ya vencida):

```sql
select b.* from programacion_bloque b
where b.salon_id = :salonId and b.dia_semana = :diaSemana and b.activo = true
  and (b.vigente_hasta is null or b.vigente_hasta >= :desde)
```

Como `programacion_bloque.vigente_desde` es `NOT NULL` y el objetivo es `[D, +∞)`, esa única
condición **es** "la vigencia del bloque intersecta `[D, +∞)`"; no hay que replicar
`RangoVigencia.intersecta` en SQL.

- **Versionar (ABIERTO)**: cada bloque devuelto debe caber en `[apertura, cierre]`. Los bloques que
  empiezan antes de `D` y terminan después **sí** entran, pero sólo se les exige encajar en el
  horario nuevo; nunca se re-evalúa el tramo anterior a `D`.
- **Cerrar**: cualquier bloque devuelto es conflicto.

**No hace falta análisis de cobertura/gaps aquí**: tras un `Versionar` permitido, el tramo `[D, +∞)`
queda cubierto **exactamente** por la versión nueva `D/NULL`, porque el append sólo se permite
cuando no existen versiones posteriores (§10). Basta la contención horaria. `CoberturaVigencia`
sigue siendo de la validación directa, no de la inversa.

**No se cambia el estado del bloque**: no se recorta, no se desactiva, no se marca `INVALIDO`.
Política A: se rechaza el horario.

### No sobrevalidar el pasado

La validación inversa sólo compara contra el horario **resultante**, que sólo rige desde `D`; nunca
contra versiones anteriores. Un bloque vigente desde enero, con versionado efectivo en septiembre,
sólo se evalúa por si cabe en el horario de septiembre en adelante: una hipotética inconsistencia de
febrero no puede rechazar el cambio, porque el cambio no la causó ni la modifica.

Consecuencia aceptada: en **alta (A)** y **reapertura (J)** el cambio sólo **añade** cobertura donde
no había ninguna, y aun así un bloque activo cuyo horario no quepa se rechaza. Puede parecer
contraintuitivo (ese bloque ya estaba sin respaldo), pero es la lectura fail-closed coherente con la
Política A; la salida del operador es desactivar o ajustar el bloque primero. Se documenta, no se
excepciona.

---

## 15. Protocolo en los writers de Bloque y Turno

### `BloqueProgramacionService.crearBloque` — único entry point que crea un bloque activo (§2)

```
  validarComandoBloque(comando);
+ salonLock.adquirir(comando.salonId());        // antes de leer el horario
  Salon salon = salonRepository.findById(...)   // sigue igual: valida activo
  validarDentroDelHorarioOperacion(comando);
  buscarTraslapesActivos(...)
  bloqueRepository.save(bloque);
```

El lock queda en la **misma** `@Transactional` que el `save` (la de clase). Con
`Propagation.MANDATORY`, invocarlo fuera de transacción rompe en el acto. `crearAsignacion` no se
toca.

### `TurnoInstructorService`

```
crear:
  autorizadorSalon.verificarAccesoSalon(...)    // primero: no retener lock sin permiso
+ if (request.tipo() == RECURRENTE) salonLock.adquirir(request.salonId());
  validarDentroDeHorarioSalon(...); validarSinTraslape(...); turnoRepository.save(turno);

actualizarTurno:   (ya rechaza todo lo que no sea RECURRENTE)
  turno = turnoRepository.findById(id) ...      // lectura de identidad; el salón no puede cambiar
  autorizadorSalon.verificarAccesoSalon(...); validaciones de forma (día 0..6, horas)
+ salonLock.adquirir(turno.getSalon().getId());
  validarDentroDeHorarioSalon(...); validarSinTraslape(...); turnoRepository.save(turno);
```

`actualizarTurno` puede mover el turno **entre días del mismo salón**; como el lock es por salón,
una sola adquisición cubre el día de origen y el de destino.

---

## 16. Cambios de salón y deadlock

Verificado en el código: **`BloqueProgramacion` no puede cambiar de salón** (no existe operación de
actualización) y **`TurnoInstructor` tampoco** (`actualizarTurno` toma el salón de la entidad y el
request no lo lleva).

Por tanto **ningún comando de F2B.3b.1 necesita más de un lock de salón**, y el riesgo de deadlock
por orden de adquisición **no existe hoy**. No se diseña un protocolo multi-lock sin usuario: sería
complejidad sin invariante que proteger.

Regla de guardia, por escrito para que no se resuelva ingenuamente el día que aparezca: si alguna
vez un update puede mover una entidad entre salones, los locks se adquieren **ordenados de forma
determinista por el `UUID` del salón** (`compareTo` natural), nunca en el orden del request.
Adquirir `viejo → nuevo` en una ruta y `nuevo → viejo` en otra es exactamente el deadlock que ese
orden evita.

---

## 17. Concurrencia

### A — la carrera que hay que impedir

```
TX A: lock Salon -> lee horario -> valida: no hay Bloque incompatible
TX B:                                       crea Bloque compatible con el horario VIEJO
TX A: versiona horario -> commit
TX B: commit
Resultado prohibido: horario nuevo + Bloque incompatible
```

Con el lock compartido de `Salon`, **una pasa primero y la otra espera**:

- **A primero**: B queda bloqueada en `SalonLock.adquirir`; al commitear A, B despierta, lee el
  horario **ya versionado** y su propia `validarDentroDelHorarioOperacion` rechaza el bloque.
- **B primero**: A espera; al commitear B, A despierta, su validación inversa (§14) ve el bloque
  recién creado y **rechaza el versionado**.

En ningún orden se alcanza el estado prohibido. Propiedad idéntica para `TurnoInstructor`
RECURRENTE, sustituyendo §14 por §13.

### B — Horario vs Horario

La segunda transacción espera el lock y al despertar relee y clasifica contra el estado **ya
committeado** por la primera; cae en `C` (`YA_EXISTE_VERSION_EN_ESA_FECHA`) o `E/D`
(`VERSIONADO_INTERMEDIO_NO_SOPORTADO`) — error de dominio legible, no un `23P01` crudo. El EXCLUDE
permanece como backstop.

### C — el caso que justifica el cambio de lock (verificado)

Salón existente, día **sin ninguna fila** de `horario_operacion`, dos sesiones reales de
PostgreSQL 16.14:

```
A: BEGIN; SELECT id FROM salon WHERE id=... FOR UPDATE; pg_sleep(25); INSERT ...; COMMIT;
B: BEGIN; SELECT id FROM salon WHERE id=... FOR UPDATE;   -->  Time: 19551.474 ms
   SELECT count(*) ... dia_semana=3                        -->  1   (ve la fila de A)
```

B esperó **19.55 s** por una fila `salon`, con cero filas de horario que bloquear. Un
`SELECT ... FOR UPDATE` sobre `horario_operacion` habría recorrido cero filas y **no habría
bloqueado nada**: las dos altas iniciales habrían corrido en paralelo y sólo el EXCLUDE las habría
separado, con un `23P01` en vez de un error de dominio. Ésta es la justificación empírica del salto
desde el `FOR UPDATE` sobre horarios al lock de la fila padre.

En una corrida sin contención (A ya committeada), B leyó la fila de A y su INSERT ciego falló con
`23P01` sobre `ex_horario_operacion_vigencia`: el backstop responde también cuando el precheck no se
hace.

---

## 18. Tests

### Unitarios de writer (mocks)

**Versionar**: alta sin historia · legacy `NULL/NULL` · append sobre versión no legacy · reapertura
tras cierre · `D` en el pasado · `D == vigenteDesde` · gap con versión futura · append con versión
posterior existente · `apertura == cierre` · `apertura > cierre` · `diaSemana = -1` y `= 7` · salón
inexistente · conflicto Turno RECURRENTE · conflicto Bloque.

**Cerrar**: `D` en el pasado · `diaSemana` inválido · salón inexistente · conflicto Turno ·
conflicto Bloque, más los siete casos ratificados en F2B.3b.0.1:

| # | Fixture | Esperado |
|---|---|---|
| 1 | legacy `NULL/NULL`, sin futuro | `Cerrar(D)` **permitido** → `NULL / D-1` |
| 2 | historia cerrada + versión actual abierta, `D` dentro de la actual, **sin** futuro posterior | **permitido** → la actual queda `vigenteDesde / D-1`, la historia intacta |
| 3 | existe versión con `vigenteDesde > D` | `CIERRE_CON_VERSIONES_FUTURAS` |
| 4 | `D == vigenteDesde` de una versión | `CANCELACION_DE_VERSION_NO_SOPORTADA` |
| 5 | `D` cae en un gap | `NO_EXISTE_VERSION_VIGENTE_EN_ESA_FECHA` |
| 6 | rechazo por versión futura (caso 3) | **ninguna fila modificada**: se releen todas las versiones y se comparan con el estado previo, incluida la futura, que no debe haberse borrado, recortado ni cancelado |
| 7 | concurrencia: otra TX planifica una versión futura bajo el mismo lock de `Salon` | `Cerrar` **reevalúa después de adquirir el lock** y rechaza con `CIERRE_CON_VERSIONES_FUTURAS` |

Los casos 1–6 se prueban tanto en unitario (mocks) como en PostgreSQL real; el 6 sólo tiene valor
contra la base, porque lo que afirma es la ausencia de escrituras. El 7 es un test de concurrencia
y va con los de más abajo: es la contraparte de cierre del escenario "Horario vs Horario" y prueba
que la lectura de versiones ocurre **después** del lock, no antes.

Dos tests de protocolo que valen por sí solos: **que la validación inversa se invoque antes de
cualquier `save`** (`InOrder` sobre los mocks) y **que `SalonLock.adquirir` sea la primera
interacción con la base**.

### PostgreSQL/Testcontainers (V46 real)

Patrón existente en el repo: `@SpringBootTest` + `@Import(TestcontainersConfiguration.class)`.

- legacy `NULL/NULL` → `Versionar(D)` → **dos filas**: `NULL/D-1` y `D/NULL`.
- `HorarioOperacionResolver` en `D-1` resuelve la **vieja**; en `D`, la **nueva**.
- `Cerrar(D)` → `vigenteHasta = D-1`, y el resolver en `D` devuelve **vacío**.
- reapertura tras cierre → nueva fila `D/NULL`, historia intacta, gap preservado.
- `23P01` → `ConflictException` `CONFLICTO_VIGENCIA_HORARIO`, provocada por inserción solapada vía
  `JdbcTemplate`.

### Rollback

Fixture: día con `[-∞, X]` y versión futura `[Y, +∞)`, de modo que el `UPDATE` (primer flush) tenga
éxito y el `INSERT` posterior choque con el EXCLUDE. Aserción: tras el rollback la fila antigua
conserva su `vigenteHasta` **original**. Verificado a nivel SQL que el escenario se comporta así
(§11). Para no depender de saltarse la clasificación del writer, la versión futura se inserta
**después** de la lectura del writer, o el test ejercita el componente de persistencia
directamente. Lo que no se hará es debilitar el writer para que el test pase.

### Concurrencia real (dos transacciones, sin `sleep` como sincronización)

Sin `@Transactional` de clase: dos hilos con `TransactionTemplate` propio, coordinados con
`CountDownLatch`/`CyclicBarrier`, y limpieza explícita.

| Escenario | Orden | Esperado |
|---|---|---|
| Horario vs Bloque A | horario toma el lock primero | Bloque **espera**; tras el commit del horario lee el nuevo y **rechaza** |
| Horario vs Bloque B | Bloque toma el lock primero | horario **espera**; tras el commit del bloque la validación inversa **rechaza** |
| Horario vs Turno A/B | ambos órdenes | simétrico, con `TurnoInstructor` RECURRENTE |
| Horario vs Horario | ambos | la segunda falla con error de dominio, nunca con solape |
| Versionar vs Cerrar | `Versionar(futuro)` toma el lock primero | `Cerrar(D)` **espera**; al despertar relee y rechaza con `CIERRE_CON_VERSIONES_FUTURAS` (test 7 de arriba) |
| Alta inicial concurrente | día **sin** filas de horario | serializadas por el lock de `Salon`; la segunda rechaza por dominio, **no** por `23P01` |

Dos exigencias, aprendidas de §17-C: **`SET LOCAL lock_timeout`** (p. ej. 5 s) en el hilo que debe
esperar, para que un protocolo roto falle en segundos en vez de colgar CI; y **la espera se mide, no
se asume** — se afirma que el hilo bloqueado no progresó hasta liberarse el latch del otro. El
`sleep` puede ampliar la ventana, nunca ordenar los eventos. La última fila de la tabla justifica el
diseño de §6 y no puede omitirse.

---

## 19. Alcance de F2B.3b.1

**`ubicaciones`** — nuevos: `dominio/CambioHorarioOperacion`, `dominio/ConflictoProgramacion`,
`dominio/ValidadorImpactoCambioHorarioOperacion` (port), `servicio/SalonLock`,
`servicio/VersionarHorarioOperacion`, `servicio/CerrarHorarioOperacion`,
`servicio/ConflictoVigenciaHorarioTranslator`. Modificados: `repositorio/SalonRepository`
(`bloquearParaActualizar`), `repositorio/HorarioOperacionRepository` (variante con `for update` de
`findVersionesQueIntersectan`, y corrección del Javadoc obsoleto de `findVigente`).

**`calendario`** — nuevo: `servicio/ImpactoTurnosRecurrentesEnHorario`. Modificado:
`servicio/TurnoInstructorService` (lock en `crear` RECURRENTE y en `actualizarTurno`).

**`programacion`** — nuevo: `servicio/ImpactoBloquesEnHorario`. Modificados:
`repositorio/BloqueProgramacionRepository` (query de bloques activos vigentes desde `D`),
`servicio/BloqueProgramacionService` (lock en `crearBloque`).

**Tests**: los de §18.

**NO se toca**: ninguna migración (V44–V46 incluidas), ningún controller, ningún DTO HTTP,
`SalonService`, `SalonHorarioExcepcionService`, `ReservaService`, `HorarioEfectivoSalon`,
`HorarioOperacionResolver`, ni el frontend.

### Deuda documental P2

| Archivo | Problema |
|---|---|
| `ubicaciones/repositorio/HorarioOperacionRepository.java:20` | Javadoc de `findVigente` atribuye la unicidad a `UNIQUE(salon_id, dia_semana)`, retirado por V46. **Se corrige**: el archivo se toca en F2B.3b.1 |
| `ubicaciones/servicio/SalonService.java:213` | Javadoc de `horariosVigentes`: "hoy lo impide el UNIQUE… y después de retirarlo lo impedirá el versionado". Ya está retirado. **No se corrige aquí**: F2B.3b.1 no toca `SalonService` y no se amplía alcance por estilo |
| `ubicaciones/servicio/SalonService.java:139` | `crearHorariosIniciales` sigue generando `NULL/NULL` para salones nuevos (§2). Observación, no defecto |

---

## 20. Qué queda para F2B.3b.2

- `POST` de versionado y de cierre, con `efectivoDesde` **explícito** en el request.
- `GET /salones/{id}/horarios/historial`, y con él `findVersionesOrdenadas` (§8: antes no tiene
  consumidor).
- DTOs HTTP, seguridad y autorización de los endpoints administrativos.
- Migración del cliente web para dejar de enviar `horarios` en el `PUT /salones/{id}`. Hasta
  entonces sigue vigente `HORARIOS_REQUIEREN_VERSIONADO`.
- Decisión sobre el hueco de Turno EXCEPCION (§13) y sobre `SalonHorarioExcepcionService.guardar`,
  que puede cerrar un día sin mirar los turnos existentes.
- Si se añade una operación de reactivación de Bloque o Turno (hoy no existe, §2), debe entrar en el
  protocolo de lock antes de tener endpoint.

### Comandos separados que manipulan planificación existente

`Versionar` y `Cerrar` sólo operan sobre el borde abierto del historial (§21, decisión 22). Todo lo
que toque planificación ya existente son comandos distintos, **fuera de F2B.3b.1** y sin fase
asignada todavía:

| Comando futuro | Cubre el rechazo de | Notas |
|---|---|---|
| `CancelarVersionHorarioOperacion` | `CANCELACION_DE_VERSION_NO_SOPORTADA` (§9-b) | Retirar una versión planificada completa. Debe decidir si cierra o borra la fila, contra el invariante de historial acumulativo |
| `CorregirVersionHorarioOperacion` | `YA_EXISTE_VERSION_EN_ESA_FECHA` (§10, caso C) | UPDATE in-place de horas sobre una versión existente; semánticamente distinto de versionar |
| `InsertarVersionIntermedia` | `VERSIONADO_INTERMEDIO_NO_SOPORTADO` (§10, casos D/E) | Partir una versión y decidir qué pasa con la programación ya validada contra ella |
| `ReemplazarPlanificacionFutura` | `CIERRE_CON_VERSIONES_FUTURAS` (§9-c) | Cerrar desde `D` **y** retirar el futuro planificado, en una sola operación explícita |

Cada uno reabre preguntas de Política A propias (qué pasa con Bloques y Turnos ya aceptados contra
la versión afectada) que los dos comandos conservadores evitan por construcción. Diseñarlos exige su
propio checkpoint.

**Despliegue.** F2B.3b.1 **no despliega frontend nuevo** y no cambia ningún comportamiento
observable por HTTP: los writers no tienen controller. La UI legacy sigue igual — payload de horario
idéntico: permitido; cambio real: `HORARIOS_REQUIEREN_VERSIONADO`. La UI se conecta al writer
explícito sólo tras F2B.3b.2, lo que mantiene separada la integridad transaccional del contrato
externo y su seguridad.

---

## 21. Decisiones cerradas

Se respetan sin reabrir las 1–20 de §23 del checkpoint, en particular 7 (append futuro + cierre;
C/D/E/F rechazados), 11 (`SalonHorarioExcepcion` fuera de Política A), 12 (`Asignacion` no revalida
horario), 14 (lock de `Salon` primero, EXCLUDE backstop, `23P01` traducido), 15 (Política A), 16
(protocolo compartido) y 20 (reapertura vs gap intermedio).

### 21 — Semántica de cierre (ratificada en F2B.3b.0.1)

`CerrarHorarioOperacion(salonId, diaSemana, efectivoDesde)` significa **"dejar de operar
recurrentemente ese día desde `efectivoDesde` hacia +∞"**. En consecuencia:

- **no** puede generar un cierre temporal más una reapertura automática causada por una versión
  futura ya planificada;
- si existe cualquier `HorarioOperacion` del mismo salón/día con `vigenteDesde > efectivoDesde`, el
  comando **rechaza** con el código estable `CIERRE_CON_VERSIONES_FUTURAS`;
- esas versiones futuras **no se borran, no se recortan, no se cancelan y no se alteran**
  automáticamente;
- la existencia de futuro se determina con `vigenteDesde != null && vigenteDesde > D`. **No** se usa
  `vigente_hasta IS NULL` como sinónimo de "actual" ni de "futura" — mismo predicado inseguro que
  descarta la decisión cerrada 3 del checkpoint.

Esta decisión sustituye y cierra el antiguo BLOQUEADOR-1 de la versión F2B.3b.0 de este documento.

### 22 — Consistencia entre los dos comandos iniciales

Queda registrado expresamente:

- **`VersionarHorarioOperacion`** no admite inserción intermedia ni reorganización de futuro
  planificado (casos C/D/E → rechazo explícito).
- **`CerrarHorarioOperacion`** tampoco reorganiza ni elimina futuro planificado (nueva decisión 21).

Ambos comandos iniciales son **conservadores por simetría**: operan sobre el borde abierto del
historial y rechazan todo lo demás. Las operaciones que sí manipulan planificación existente
—cancelar una versión planificada, corregir una versión, insertar en medio de la historia,
reemplazar planificación futura— son comandos separados y **no se implementan en F2B.3b.1** (§20).

### Decisiones de diseño de este documento

Todas dentro de lo ya cerrado:

1. El port vive en `ubicaciones`; los adapters dependen **sólo de repositorios**, nunca de los
   services de su módulo.
2. `SalonLock` usa `Propagation.MANDATORY`.
3. `findVersionesQueIntersectan(salonId, dia, D, null)` basta para clasificar todos los edge cases;
   `findVersionesOrdenadas` sigue diferida.
4. La persistencia usa `saveAndFlush` explícito en orden UPDATE→INSERT: impone el orden en vez de
   confiar en el de Hibernate.
5. `@Lock` sólo sobre JPQL; sobre queries nativas se escribe `for update` en el SQL.
6. Sólo se traduce `23P01` con el constraint esperado, nunca un `DataIntegrityViolationException`
   genérico.
7. `eliminar`, `crearAsignacion`, EXCEPCION y CANCELACION **no** toman el lock (§7, §13).
8. No se diseña protocolo multi-lock: ninguna entidad puede cambiar de salón (§16).

---

## 22. Bloqueadores

**Ninguno.**

El único bloqueador que registraba la versión F2B.3b.0 de este documento —qué hace
`CerrarHorarioOperacion` cuando existen versiones futuras planificadas— quedó **resuelto por
ratificación en F2B.3b.0.1** y está incorporado como decisión cerrada 21 (§21), con su regla en §9,
sus casos en §10 y sus tests en §18. Se elimina de esta sección: ya no es una pregunta abierta.

Para dejar rastro de por qué existía: la letra del checkpoint F2B.0.1 §8 habría producido un cierre
temporal seguido de reapertura automática (verificado en PostgreSQL: cerrar el `2026-08-25` con una
versión futura en `2026-12-01` dejaba el día sin horario hasta el `2026-11-30` y reabriéndolo
solo), lo que contradice la semántica del comando. La ratificación adopta el rechazo explícito con
`CIERRE_CON_VERSIONES_FUTURAS`, coherente con cómo `Versionar` ya trata el futuro planificado.

### Condiciones de parada evaluadas y **no** disparadas

| Condición (§41) | Estado |
|---|---|
| la validación inversa obliga a `ubicaciones → calendario/programacion` | **no** — port en `ubicaciones` (§4) |
| ciclo Spring no resoluble | **no** — grafo acíclico verificado (§4) |
| no puede existir protocolo compartido de lock | **no** — `SalonLock` (§6), verificado (§17-C) |
| updates mueven entidades entre salones sin orden seguro | **no** — no pueden cambiar de salón (§16) |
| Turno EXCEPCION contradice invariantes actuales | **no** — la invariante ya no se mantiene hoy vía `SalonHorarioExcepcion` (§13) |
| JPA no permite `update → flush → insert` seguro | **no** — verificado, con rollback íntegro (§11) |
| hace falta modificar V44–V46 | **no** |
| hace falta broker / event bus | **no** — validación síncrona en la misma TX (§5) |
| la concurrencia sólo se prueba con `sleep` frágiles | **no** — latches + `lock_timeout`, y la espera se mide (§18) |
| cierre con versiones futuras ambiguo en el checkpoint | **no** — ratificado en F2B.3b.0.1, decisión cerrada 21 (§21) |

Ninguna condición de parada de §41 del encargo está disparada. **F2B.3b.1 queda LISTA.**

---

## 23. Primera implementación

Orden recomendado, cada paso verde antes del siguiente:

1. **`SalonLock` + `bloquearParaActualizar`**, con el test PostgreSQL de dos hilos sobre un día
   **sin** filas de horario (§18, última fila). Es la pieza que sostiene todo lo demás y la única
   cuyo fallo sería silencioso.
2. **Tipos neutrales y port**, sin implementaciones: sólo compila y fija la dirección de
   dependencias.
3. **Adapters** de `calendario` y `programacion`, con tests unitarios propios. El de Turnos no
   necesita query nueva; el de Bloques sí.
4. **`VersionarHorarioOperacion`**: clasificación de edge cases (§10), Política A y la secuencia
   `saveAndFlush` de §11. Tests unitarios + los de PostgreSQL.
5. **Traducción `23P01`** (§12), con su test de conflicto provocado por `JdbcTemplate`.
6. **`CerrarHorarioOperacion`** con la clasificación `a → b → c` de §9 y los siete casos de §18.
   Reutiliza la misma query y el mismo lock que el paso 4; lo único propio es el orden de rechazos.
7. **Lock en `BloqueProgramacionService.crearBloque` y en `TurnoInstructorService`** (§15), con los
   tests de concurrencia inversa, incluido `Versionar` vs `Cerrar`.

**La fase se declara LISTA**: no quedan decisiones abiertas y el par versionar/cerrar puede
entregarse completo y coherente.

Verificaciones finales de esta tarea de diseño:

```
git diff --check   -> sin salida
git status --short -> únicamente auditoria/fase-2b3b-diseno-writers-concurrencia.md
```

Sin commit, sin push, sin merge, sin tocar `main`.
