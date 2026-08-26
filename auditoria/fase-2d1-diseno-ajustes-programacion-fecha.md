# F2D.1 — Diseño ajustes puntuales de programación por fecha

Fase **exclusivamente de análisis y diseño**. No se ha escrito código productivo, no se han
modificado tests, no se han creado migraciones ni endpoints, no se ha tocado frontend ni mobile.

---

## Estado base

| Elemento | Valor |
|---|---|
| Branch | `operacion/excepciones-horario-fecha` |
| HEAD | `8c40594d2caf8b5230b364cb76cd8f48fe5ed98a` |
| Working tree previo | limpio |
| Suite base | **493 tests / 0 failures / 0 errors / 0 skipped** (44 clases) |
| Última migración Flyway | `V46__horario_operacion_drop_unique_dia.sql` |
| Siguiente número libre | **V47** (confirmado libre, F2C.2 no consumió ninguno) |

Diseños autoritativos previos respetados sin cambios:

- `auditoria/fase-2c1-diseno-excepciones-horario-fecha.md`
- `auditoria/fase-2c2-implementacion-excepciones-horario-fecha.md`

---

## Inventario legacy

El hallazgo que condiciona F2D es el mismo patrón que condicionó F2C, pero con signo distinto:
**no hay un modelo de programación, hay dos, y sólo uno está vivo.**

### Modelo A — `calendario.TurnoInstructor` (legacy, EN PRODUCCIÓN)

| Pieza | Ruta | Estado |
|---|---|---|
| Entidad | `calendario/entidad/TurnoInstructor.java` | Viva |
| Entidad hija | `calendario/entidad/TurnoInstructorAsignacion.java` | Viva |
| Repositorio | `calendario/repositorio/TurnoInstructorRepository.java` | Vivo |
| Writer | `calendario/servicio/TurnoInstructorService.java` | Vivo, endurecido en F2C.2 |
| Controller | `/api/turnos-instructor` | Vivo |
| Consumo web | `web/src/api/calendario.ts`, `SalonHorarios.tsx`, `CalendarioHorariosInstructor.tsx` | Vivo |
| Consumo backend | `ReservaService.turnosVigentes` | Vivo |

Forma real, verificada sobre el código:

- `tipo ∈ {RECURRENTE, EXCEPCION, CANCELACION}`, un solo enum sobre **una sola tabla**.
- `diaSemana` sólo si `RECURRENTE`; `fecha` sólo si `EXCEPCION`/`CANCELACION`. Sin vigencias:
  un `RECURRENTE` es una regla abierta al futuro, sin `vigenteDesde/Hasta`.
- **Salón dentro del turno** (`@ManyToOne Salon`), **instructor N:M** (`turno_instructor_usuario`),
  **actividad en la entidad hija** `TurnoInstructorAsignacion (turnoId, usuarioId, tipoActividadId)`
  con rango horario propio opcional dentro del bloque.
- Un turno puede tener **varios instructores y varias actividades a la vez**.

### Modelo B — `programacion.BloqueProgramacion` + `Asignacion` (F1C, LATENTE)

| Pieza | Ruta | Estado |
|---|---|---|
| Entidades | `programacion/entidad/{BloqueProgramacion,Asignacion}.java` | Existen |
| Tabla | `V41__programacion_bloque_asignacion.sql` | Existe, **nace vacía** |
| Repositorios | `programacion/repositorio/*` | Existen |
| Writer | `programacion/servicio/BloqueProgramacionService.java` | Existe |
| Controller | **NO EXISTE** | — |
| Consumo web | **NINGUNO** | — |
| Consumo backend | Sólo `ImpactoBloquesEnHorario` (lectura) y tests | — |

Verificado con `grep -rl "BloqueProgramacionService" src/`: sus únicos usuarios son él mismo, su
adapter de impacto y **dos clases de test**. **No hay ningún camino de escritura expuesto por API
hacia `programacion_bloque` / `programacion_asignacion` en producción.**

Forma real:

- `BloqueProgramacion`: `serieId`, `salonId`, `diaSemana`, `[horaInicio, horaFin)`,
  `vigenteDesde` (NOT NULL) / `vigenteHasta` (nullable = abierta), `activo`.
- `Asignacion`: `serieId`, `bloqueId`, **`instructorId` (uno)**, **`tipoActividadId` (una)**,
  `[horaInicio, horaFin)`, vigencia propia contenida en la del bloque, `activo`.
- `serieId` está documentado en ambas como *"identidad lógica compartida por todas las versiones
  de la regla"*.

### Resto del inventario relevante

| Pieza | Hecho verificado |
|---|---|
| `HorarioEfectivoSalon.resolver(salonId, fecha)` | Excepción activa → semanal versionado → `NO_OPERATIVO`. No usa `LocalDate.now()`. **No se toca en F2D.** |
| `HorarioEfectivo` | `record(Estado{ABIERTO,CERRADO,NO_OPERATIVO}, Origen, apertura, cierre)`; `contiene()` exige **contención completa** con extremos inclusivos |
| `SalonLock` | `FOR UPDATE` sobre `salon`, `Propagation.MANDATORY`, protocolo *lock → leer → validar → persistir* |
| `SalonHorarioExcepcion` | Una fila activa por `(salon, fecha)` (`idx_salon_horario_excepcion_unica`), soft delete, upsert REPLACEMENT |
| `ValidadorImpactoExcepcionHorario` + `ConflictoProgramacionPuntual` | Port neutral en `ubicaciones`; adapters en `calendario` (`ImpactoPuntualEnExcepcionHorario`) y en `programacion` (`ImpactoBloquesEnHorario`) |
| `Reserva` | `salon`, `instructor`, `cliente`, `tipoActividad`, `fecha`, `[horaInicio,horaFin)`, `estado ∈ {CONFIRMADA, CANCELADA}`. **Sin FK a turno ni a asignación** |
| `Clock` | `config/RelojConfig.reloj()`, ya inyectado en `TurnoInstructorService` y `SalonHorarioExcepcionService` |
| Permisos existentes | `calendario.{leer,gestionar,editar,cancelar,administrar}`, `salon.{leer,administrar}`, `reserva.{administrar,checkin}` |
| `btree_gist` | Instalado en `V44`; `EXCLUDE USING gist` ya en uso en `V45` |

---

## Hallazgo legacy — validación de §7

El brief pide determinar exactamente qué representan `EXCEPCION` y `CANCELACION`. Respuesta
verificada, y **no coincide con lo que F2D necesita**:

### `TurnoInstructor.Tipo.EXCEPCION` — *añade*, no sustituye

`TurnoInstructorService.validarSinTraslape` compara una `EXCEPCION` contra
**las `EXCEPCION` de esa fecha Y los `RECURRENTE` de ese día de la semana**, y rechaza cualquier
solape:

```java
List<TurnoInstructor> existentes = new ArrayList<>(buscarRecurrentesPorSalonYDia(salonId, diaSemana));
if (tipo == EXCEPCION) existentes.addAll(buscarExcepcionesPorSalonYFecha(salonId, fecha));
// ... rechaza si inicio < t.horaFin && fin > t.horaInicio
```

Su Javadoc lo dice explícitamente: *"un recurrente que no fue reemplazado explícitamente sigue
ocupando el salón esa fecha"*. **Pero no existe ningún mecanismo de "reemplazo explícito":** no hay
columna, ni FK, ni comando que apunte de una `EXCEPCION` al `RECURRENTE` que sustituye.

Consecuencia dura, medible contra el caso B del brief:

```
RECURRENTE lunes 08:00–10:00   (Ariadna, Reformer)
EXCEPCION  14/09 09:00–11:00   → RECHAZADO por traslape con el recurrente
```

**El caso B del enunciado es hoy imposible de expresar.** Lo mismo para C y D con solape
(cambiar instructor o actividad manteniendo la hora es un solape exacto → rechazo).

### `ReservaService.turnosVigentes` contradice al writer

El único consumidor que resuelve por fecha implementa la semántica **opuesta**:

```java
if (hayCANCELACION para (instructor, salon, fecha)) return List.of();
if (hay EXCEPCION  para (instructor, salon, fecha)) return excepciones;   // SUSTITUYE
return recurrentes de ese diaSemana;
```

Es decir: **el writer prohíbe el solape porque asume que la excepción convive con el recurrente; el
resolver asume que la excepción lo sustituye.** Son dos semánticas incompatibles conviviendo hoy.
No es un defecto que F2D deba corregir en el legacy (nadie puede crear el estado contradictorio,
precisamente porque el writer lo rechaza), pero **descarta reutilizar `EXCEPCION` como base de F2D**.

### `TurnoInstructor.Tipo.CANCELACION` — marcador de día, no de ocurrencia

- No apunta a ninguna regla recurrente concreta: su identidad efectiva es
  `(salon, fecha, conjunto de instructores)`.
- Cubre el día completo como `00:00–23:59` (así lo crea el frontend en `SalonHorarios.tsx:379`).
- No se valida contra horario, no toma `SalonLock`, y F2C.1 lo clasificó explícitamente como
  **no operativo**.
- No puede cancelar **una** ocurrencia cuando el instructor tiene dos bloques ese día.

### Otras incompatibilidades estructurales del legacy con las invariantes cerradas

| Invariante cerrada | `TurnoInstructor` | `Bloque` + `Asignacion` |
|---|---|---|
| §13 — 1 instructor, 1 actividad, 1 rango, 1 salón por asignación | **No**: N instructores × M actividades por turno | **Sí**, por construcción |
| §23 — varios instructores pueden coexistir en el mismo salón y horario | **No**: `validarSinTraslape` trata el salón como espacio físico exclusivo entre turnos | **Sí**: un bloque contiene varias `Asignacion` de distintos instructores |
| §10 — existe un ID estable apto como target | **No**: el turno agrega instructores y actividades; no hay identidad por ocurrencia individual | **Sí**: `Asignacion.serieId` |
| Versionado temporal | **No**: sin vigencias | **Sí**: `vigenteDesde/vigenteHasta` |

**Conclusión del inventario: `TurnoInstructor` no puede ser el soporte de F2D sin rediseñarlo, y
rediseñarlo significa romper la API y el frontend en producción.**

---

## Problema de dominio

Se necesita expresar, para **una fecha concreta `D`**, sin tocar el template recurrente:

1. **CANCELAR** una ocurrencia recurrente concreta (sólo `D`; el lunes siguiente intacto).
2. **REEMPLAZAR** una ocurrencia recurrente concreta: hora, instructor, actividad y/o salón.
3. **AGREGAR** programación puramente puntual sin template detrás.

Y componer todo eso con lo ya cerrado en F2B/F2C: `HorarioEfectivoSalon` manda, la ocurrencia
recurrente parcialmente fuera del horario operativo **se omite, nunca se recorta**, y el template
recurrente **nunca se escribe** por causa de una fecha.

---

## Modelo elegido

**Opción C del §9: nueva entidad de ajuste sobre el modelo `programacion` (Modelo B), con ventana
de coexistencia explícita y congelada frente a `TurnoInstructor` legacy (Modelo A).**

Se descartan, con motivo verificable:

| Opción | Por qué no |
|---|---|
| **A** — evolucionar `TurnoInstructor.EXCEPCION/CANCELACION` | Requiere añadir target, romper la exclusividad de salón (§23), partir el turno en 1-instructor/1-actividad (§13) y añadir vigencias. Es reescribir la entidad viva que consumen el frontend y `ReservaService`. Big Bang prohibido por §28 |
| **B** — `AjusteProgramacionFecha` "separado" sin decidir sobre qué modelo | No cierra el target: un ajuste necesita apuntar a algo, y sólo `Asignacion` tiene identidad apta |
| **D** — modelo propio de ocurrencias materializadas | Crearía `Sesion` de facto. Prohibido por §50 |

### Forma del modelo

Una entidad nueva, `programacion.entidad.AjusteProgramacionFecha`, tabla
`programacion_ajuste_fecha`, con tres tipos: `CANCELACION`, `REEMPLAZO`, `ADICION`.

- **`CANCELACION` y `REEMPLAZO`** apuntan a `asignacion_serie_id` + `fecha`.
- **`ADICION`** no apunta a nada: identidad propia por UUID.
- Un ajuste **nunca** escribe en `programacion_bloque` ni en `programacion_asignacion`. Cero
  `insert`, `update`, `delete`, `activo = false` o cierre de vigencia sobre esas tablas. Es la
  misma regla que F2C.1 fijó para las excepciones de horario, aplicada ahora en el eje de
  programación.

### Por qué el target es `serieId` y no `asignacion.id`

`Asignacion.serieId` está documentado en la propia entidad como la identidad lógica **compartida
por todas las versiones de la regla**. Un ajuste para el 14/09 se refiere conceptualmente a *"la
asignación de Ariadna los lunes"*, no a *"la fila versión 3 de esa asignación"*. Si el target fuera
`asignacion.id`, versionar el template en octubre (una operación legítima y ajena a septiembre)
dejaría huérfana la cancelación del 14/09 — exactamente el tipo de acoplamiento que §5 prohíbe.

La ocurrencia de la serie `S` en la fecha `D` está bien definida: es la versión de `S` cuya
vigencia contiene `D`. Si hubiera más de una, el resolver **lanza `IllegalStateException`**, mismo
patrón y misma justificación que `HorarioOperacionResolver.resolver` ante versiones semanales
solapadas. No se elige "la primera": eso enmascararía corrupción.

No hay FK posible sobre `serie_id` (no es única por diseño). La integridad la sostienen el writer
(verifica que exista una versión activa y vigente de la serie en `D`, y que su bloque pertenezca al
salón del comando) y los tests de persistencia. Las FKs reales del modelo (`salon_id`,
`instructor_id`, `tipo_actividad_id`) sí existen.

---

## Relación con `TurnoInstructor` legacy

**Decisión cerrada: coexistencia con fuentes y consumidores DISJUNTOS. Sin Big Bang, sin doble
mecanismo sobre el mismo dato.**

| Aspecto | Regla |
|---|---|
| `ProgramacionEfectiva` (resolver nuevo) | Lee **sólo** `programacion_bloque`, `programacion_asignacion`, `programacion_ajuste_fecha` y `HorarioEfectivoSalon`. **Nunca** lee `turno_instructor` |
| `ReservaService.turnosVigentes` (resolver legacy) | **No se toca en F2D.** Sigue leyendo sólo `turno_instructor` |
| `TurnoInstructorService` | **No se toca en F2D.** Ni sus tipos, ni su API, ni su semántica |
| Frontend / mobile | **No se tocan en F2D** |
| Migración legacy → `programacion` | **Fuera de F2D.** Es su propia fase (F2E) |

La razón por la que esta ventana es segura y no es una promesa: **`programacion_bloque` y
`programacion_asignacion` no tienen writer expuesto por API.** No existe controller en el paquete
`programacion`. En producción esas tablas están vacías y sólo pueden poblarse desde tests. Por
tanto **no puede existir, hoy, un instructor con datos simultáneos en ambos modelos**, y la doble
ocurrencia (mutación K) es imposible por construcción, no por convención.

Consecuencia aceptada y explícita: mientras dure la ventana, el nuevo modelo **no valida solapes
contra turnos legacy**, y el legacy no valida contra ajustes nuevos. Es correcto exactamente
mientras `programacion_*` no tenga camino de escritura en producción. **La condición de salida de
la ventana es: antes de exponer cualquier escritura de `programacion_*` a datos reales, o se migra
el legacy, o se añade validación cruzada.** F2D.2 fija esa condición con un test de arquitectura
(mutación K) que falla si `ProgramacionEfectiva` adquiere una dependencia hacia `calendario`.

---

## Tipos de ajuste

Enum cerrado, sin flags combinados:

| Tipo | Target | Campos que porta | Efecto sobre la fecha `D` |
|---|---|---|---|
| `CANCELACION` | `asignacion_serie_id` | ninguno (instructor/actividad/horas **NULL**) | Suprime la ocurrencia de esa serie en `D` |
| `REEMPLAZO` | `asignacion_serie_id` | `salon_id`, `instructor_id`, `tipo_actividad_id`, `[horaInicio, horaFin)` | Suprime la ocurrencia original en `D` **y** produce una ocurrencia nueva con esos datos |
| `ADICION` | — (`asignacion_serie_id` NULL) | `salon_id`, `instructor_id`, `tipo_actividad_id`, `[horaInicio, horaFin)` | Produce una ocurrencia nueva sin template detrás |

`CANCELACION` no porta horas **a propósito**: cancela la ocurrencia completa, no un subrango.
Permitir un subrango sería recorte por la puerta de atrás, prohibido por §17. Quien quiera media
ocurrencia usa `REEMPLAZO`, que es una decisión administrativa explícita.

---

## Target e identidad

### Target del ajuste

`(asignacion_serie_id, fecha)` para `CANCELACION` y `REEMPLAZO`. `ADICION` no tiene target.

No se usa `instructor + fecha + hora` como identidad: existe un ID estable mejor (§10).

### Identidad de la ocurrencia efectiva

No se persiste ninguna ocurrencia. La identidad es calculada y estable:

| Origen de la ocurrencia | Referencia estable |
|---|---|
| `RECURRENTE` | `(asignacionSerieId, fecha)` |
| `REEMPLAZO` | `(asignacionSerieId, fecha)` — **la misma que tenía antes del ajuste** |
| `ADICION` | `(ajusteId, fecha)` |

Que un `REEMPLAZO` **conserve** la referencia del target es deliberado y es lo que hace posible §49:
una futura confirmación de instructor emitida antes del reemplazo sigue apuntando a la misma
ocurrencia después de él. Si la identidad cambiara al `ajusteId`, todo reemplazo invalidaría
silenciosamente las confirmaciones ya emitidas.

Value object propuesto: `programacion.dominio.ReferenciaOcurrencia(Tipo tipo, UUID id, LocalDate fecha)`
con `Tipo ∈ {SERIE_ASIGNACION, AJUSTE}`.

**No se crea `Sesion`, ni estado de confirmación, ni UUID persistido de ocurrencia.** F2D sólo
garantiza identidad suficiente.

---

## Cancelación

**Semántica**: suprime la ocurrencia de `asignacion_serie_id` en `fecha`, y sólo esa.

- El lunes siguiente reaparece porque **no se escribió nada** sobre el template. Es la misma
  garantía por construcción que F2C.1: *"derivar es, literalmente, no escribir nada hoy"*.
- **No cancela "todo lo del instructor ese día".** Si un instructor tiene dos asignaciones ese día
  y se quiere cancelar ambas, son **dos** comandos, cada uno con su target. No existe comando
  masivo en F2D.
- **No valida contra `HorarioEfectivoSalon`**, por el mismo motivo por el que F2C.1 clasificó la
  `CANCELACION` legacy como no operativa: no introduce ningún intervalo que pueda quedar fuera de
  un horario. Cancelar en un día `CERRADO` es admisible y es un no-op semántico (la ocurrencia ya
  estaba omitida); rechazarlo obligaría al operador a razonar sobre el orden de dos operaciones
  independientes.
- **Sí valida temporalidad** (`fecha >= hoy`), **autorización**, y **reservas confirmadas** (abajo).

**Interacción con `TurnoInstructor.CANCELACION` legacy: ninguna.** Son mecanismos sobre modelos
distintos con consumidores distintos. `ProgramacionEfectiva` no lee `turno_instructor`, y
`ReservaService` no lee `programacion_ajuste_fecha`.

---

## Reemplazo

**Se persiste como UNA fila `REEMPLAZO`, no como `CANCELACION` + `ADICION`.** Decisión cerrada.

| Criterio | Fila única `REEMPLAZO` | `CANCELACION` + `ADICION` |
|---|---|---|
| §32 (máximo un ajuste activo por target) | Índice único parcial trivial sobre `(asignacion_serie_id, fecha) WHERE activo` | Requiere que la `ADICION` también apunte al target, contradiciendo §20 |
| Idempotencia | Un upsert sobre una fila | Dos escrituras que hay que mantener atómicas y consistentes entre sí |
| Histórico | Una fila = una decisión administrativa | Dos filas sin vínculo persistido; imposible saber que fueron un solo acto |
| API | Un comando | Un comando que se parte en dos, o dos llamadas no atómicas |
| Resolución | Un paso | Dos pasos con orden acoplado |
| Validación | Un objeto que se valida entero | El par puede validarse individualmente bien y ser incoherente junto |

Campos modificables por un `REEMPLAZO`: **`instructor`, `actividad`, `horaInicio`, `horaFin`,
`salon`**. Todos.

El resultado de un `REEMPLAZO` sigue siendo exactamente **1 instructor, 1 actividad, 1 rango,
1 salón** (§13). No se reintroduce multiplicidad.

Un `REEMPLAZO` cuyo target estaba **omitido** ese día (por caer fuera del horario efectivo) **sí
produce su ocurrencia nueva**, siempre que ésta encaje en `HorarioEfectivoSalon(salonDestino, D)`.
Esto es precisamente lo que §17 autoriza: el `10:00–12:00` no aparece por recorte automático, sino
porque un administrador lo escribió explícitamente.

---

## Adición

**Sí se soporta** (§4.E, confirmado contra el modelo real: `Asignacion` no admite una
"asignación sin bloque", y forzar un bloque sintético de un solo día sería crear una versión
semanal de un día — prohibido por §5).

- **No lleva referencia a template.** Identidad propia por UUID.
- Debe respetar: horario efectivo del salón, especialidad del instructor, rol de instructor en el
  salón, actividad ofrecida por el salón, actividad activa, no-solape global del instructor,
  temporalidad, autorización.
- **Capacidad/recursos queda fuera de F2D** (§51). La ocurrencia conserva `actividad`, `salón` y
  `rango` para que el cálculo futuro sea posible.

### Duplicados (§33)

**No existe un concepto de "duplicado" por `(salón, hora)`**, y no se introduce: §23 exige que
varios instructores coexistan en el mismo salón y horario.

La identidad de una `ADICION` es su UUID. La única exclusión real es el **solape global del mismo
instructor**, que ya rechaza el caso degenerado: repetir el mismo POST con el mismo instructor y el
mismo rango produce solape consigo mismo y devuelve `409`. Dos adiciones del mismo salón y hora con
instructores distintos son legítimas y se aceptan.

---

## Cambio instructor / actividad / salón

| Cambio | ¿Permitido? | Validación adicional obligatoria |
|---|---|---|
| **Instructor** | **SÍ** | Instructor activo; rol `INSTRUCTOR` en el salón destino; **actividad ∈ especialidades del instructor**; sin solape global del instructor nuevo |
| **Actividad** | **SÍ** | Actividad activa; ofrecida por el salón destino; **∈ especialidades del instructor resultante** |
| **Salón** | **SÍ** | Salón destino activo; `HorarioEfectivoSalon(destino, D)` contiene el rango; instructor con rol en el destino; actividad ofrecida por el destino; **autorización sobre AMBOS salones** |

**Regla dura (§21)**: un ajuste **no puede evadir ninguna validación que el camino recurrente sí
exige**. Las validaciones se extraen de `BloqueProgramacionService.crearAsignacion` y se aplican
íntegras al `REEMPLAZO`/`ADICION`:

- instructor activo (`Usuario.EstatusUsuario.activo`);
- rol `INSTRUCTOR` global o del salón (`validarInstructorHabilitado`);
- `TipoActividad.activo`;
- actividad ∈ `salon.getTiposActividad()`;
- actividad ∈ `instructor.getEspecialidades()`.

**Nota deliberada**: un ajuste **no** exige contención dentro de un `BloqueProgramacion`. El bloque
es un contenedor del template recurrente, y un ajuste puntual es por definición una excepción a ese
template. La contención que sí se exige es contra `HorarioEfectivoSalon`, que es la autoridad
operativa (§14). Un `REEMPLAZO` que se sale del bloque original pero cabe en el horario efectivo es
válido; ése es exactamente el caso B del enunciado.

---

## Horario operativo efectivo

**`HorarioEfectivoSalon` se REUTILIZA sin ningún cambio**: ni firma, ni comportamiento, ni
precedencia. F2D no rediseña el resolver de F2C (§3).

Toda validación de rango de un ajuste se hace contra `HorarioEfectivoSalon(salon, fecha)`, **nunca**
contra `HorarioOperacion` semanal directamente.

| Estado efectivo en `D` | `CANCELACION` | `REEMPLAZO` / `ADICION` |
|---|---|---|
| `ABIERTO` y `contiene(inicio, fin)` | Permitido | **Permitido** |
| `ABIERTO` y no contiene | Permitido | **Rechazado** |
| `CERRADO` | Permitido (no-op semántico) | **Rechazado** |
| `NO_OPERATIVO` | Permitido (no-op semántico) | **Rechazado** |

Ejemplo canónico del §14, cerrado: salón recurrente 08–20, excepción 14/09 → 10–16.
Ajuste `09–11` → **inválido**. Ajuste `11–13` → **válido**.

### Cierre de salón (§15)

Si existe `SalonHorarioExcepcion` **CERRADO** para `D`:

- la programación recurrente de ese día se **deriva como omitida** (no se escribe nada);
- **ningún ajuste puntual puede reabrir programación por sí solo**: `REEMPLAZO` y `ADICION` se
  rechazan mientras el estado efectivo no sea `ABIERTO`.

Para programar excepcionalmente ese día, el operador debe crear primero un `HORARIO_ESPECIAL` del
salón, vía `SalonHorarioExcepcionService`. **Programación nunca contradice Operación.**

### Protección simétrica — nueva pieza de F2D.2

Hoy, `ImpactoPuntualEnExcepcionHorario` (en `calendario`) impide que un cierre de salón deje fuera
de horario a un `TurnoInstructor.EXCEPCION` o a una `Reserva.CONFIRMADA`. Los ajustes nuevos
necesitan la misma protección, o cerrar un día dejaría ajustes activos fuera de horario.

**F2D.2 añade `programacion/servicio/ImpactoAjustesEnExcepcionHorario implements
ValidadorImpactoExcepcionHorario`**, adapter en `programacion` que lee sólo
`AjusteProgramacionFechaRepository` y reporta los ajustes activos `REEMPLAZO`/`ADICION` de
`(salonId, fecha)` que no caben en el `CambioExcepcionHorario` propuesto.

- Se registra por el mismo `List<ValidadorImpactoExcepcionHorario>` que ya inyecta
  `SalonHorarioExcepcionService`: **cero cambios en ese writer**.
- Va en `programacion`, no en `calendario`, para conservar la dirección de dependencias
  (`programacion → ubicaciones`), igual que `ImpactoBloquesEnHorario`.
- Requiere un valor nuevo en `ConflictoProgramacionPuntual.Origen`: **`AJUSTE_PROGRAMACION`**, con
  su factory `ajusteProgramacion(UUID, String)`. Es la única modificación de F2D a una clase
  existente de `ubicaciones`, y es puramente aditiva.
- `CANCELACION` **no** se reporta: no es operativa, exactamente por el mismo motivo por el que
  F2C.1 excluyó a `TurnoInstructor.CANCELACION`.

---

## Programación recurrente

Regla de F2C.1 conservada íntegra y sin reinterpretación:

| Ocurrencia derivada del template para `D` | Resultado |
|---|---|
| Completamente contenida en `HorarioEfectivo(D)` | **Se conserva INTACTA** |
| Parcialmente fuera | **Se OMITE** |
| Totalmente fuera | **Se OMITE** |
| `CERRADO` o `NO_OPERATIVO` | **Se OMITE** |

```
template recurrente : 08:00–12:00
horario efectivo D  : 10:00–16:00
→ la ocurrencia se OMITE. NO se deriva 10:00–12:00.
→ el lunes siguiente sigue siendo 08:00–12:00.
```

**La contención se evalúa sobre el rango de la `Asignacion`, no del `BloqueProgramacion`.** La
ocurrencia efectiva *es* la asignación (1 instructor, 1 actividad, 1 rango); el bloque es su
contenedor de template. El invariante de F1C garantiza que el rango de la asignación está contenido
en el del bloque, así que evaluar la asignación no puede producir ocurrencias que el bloque no
admitiera.

---

## Programación efectiva

Caso de uso único y **única fuente de verdad**:
`programacion/servicio/ProgramacionEfectiva`.

```java
List<OcurrenciaEfectiva> porSalonYFecha(UUID salonId, LocalDate fecha);
List<OcurrenciaEfectiva> porInstructorYFecha(UUID instructorId, LocalDate fecha); // CROSS-SALON
```

Ningún consumidor futuro (confirmación de instructor, materialización de sesiones, app móvil,
reservas) recompone por su cuenta recurrentes + ajustes + excepciones de salón.

### Orden de composición — cerrado, sin ambigüedad

```
1. HorarioEfectivoSalon(salon, fecha)                      ← SIEMPRE PRIMERO
2. Cargar asignaciones recurrentes vigentes en `fecha`
   (bloque.activo ∧ asignacion.activo ∧ bloque.diaSemana = dow(fecha)
    ∧ vigencia(bloque) ∋ fecha ∧ vigencia(asignacion) ∋ fecha)
3. Derivar ocurrencia por asignación, con su rango propio
4. OMITIR toda ocurrencia recurrente no contenida en el horario efectivo   ← ANTES de los ajustes
5. Cargar ajustes ACTIVOS de (salon, fecha)
6. Aplicar CANCELACION: suprimir la ocurrencia cuyo serieId coincide
7. Aplicar REEMPLAZO:  suprimir la ocurrencia original + añadir la ocurrencia del ajuste
8. Aplicar ADICION:    añadir la ocurrencia del ajuste
9. OMITIR toda ocurrencia puntual (7 y 8) no contenida en el horario efectivo  ← defensa en profundidad
10. Ordenar determinísticamente por (horaInicio, horaFin, instructorId, referencia)
11. Resultado = List<OcurrenciaEfectiva>
```

Precisiones que cierran ambigüedad:

- El paso **1 va antes que todo**: el resolver nunca aplica ajustes sin saber el horario operativo
  (detector de la mutación M).
- El paso **4 va antes que 5–8**, y no al revés. Aplicar una cancelación sobre una ocurrencia ya
  omitida es un no-op; aplicar un reemplazo sobre una ocurrencia omitida sigue produciendo la
  ocurrencia nueva (§17).
- El paso **9** existe aunque `ImpactoAjustesEnExcepcionHorario` debería hacerlo imposible: es
  fail-closed y hace al resolver correcto con independencia del writer.
- El resolver **no valida** solape de instructor ni especialidades: eso es responsabilidad del
  writer. El resolver es una función pura sobre el estado persistido, y debe ser total.
- **Determinismo (§50)**: mismas reglas + mismos ajustes + misma fecha ⇒ mismo resultado, incluido
  el orden. No hay `now()` en el resolver: la fecha entra por parámetro, igual que en
  `HorarioEfectivoSalon`.

### Forma del resultado

```java
public record OcurrenciaEfectiva(
        LocalDate fecha,
        UUID salonId,
        UUID instructorId,
        UUID tipoActividadId,
        LocalTime horaInicio,
        LocalTime horaFin,
        Origen origen,
        ReferenciaOcurrencia referencia) {

    public enum Origen { RECURRENTE, REEMPLAZO, ADICION }
}
```

`Origen` refina los tres valores sugeridos por §30 (`RECURRENTE` / `AJUSTE` / `PUNTUAL`):
`REEMPLAZO` y `ADICION` distinguen dos casos que la UI y la trazabilidad necesitan separar y que
colapsar en "AJUSTE" perdería. `referencia` cubre la trazabilidad de §30/§31 sin campos sueltos
redundantes (`templateId` / `asignacionOrigenId` viven dentro de ella).

**No se persiste metadata adicional**: ni conteos de impacto, ni warnings, ni estado de
confirmación (§49 prohíbe estados de confirmación en F2D).

---

## Reservas

**Política cerrada: RECHAZAR con `409` mientras existan reservas confirmadas incompatibles.
Nunca migrar, mover, cancelar ni borrar una reserva en silencio.**

### Criterio de incompatibilidad — uniforme para los cinco casos del §27

`Reserva` no tiene FK a turno ni a asignación, así que no hay "reserva de esta ocurrencia". El
criterio es funcional y se evalúa comparando **antes y después**:

> Una reserva `R` con `estado = CONFIRMADA` en `(salon, fecha)` es **incompatible** con el ajuste si
> estaba **cubierta** por la programación efectiva anterior y **no lo está** por la resultante.
>
> `R` está *cubierta* ⟺ existe una `OcurrenciaEfectiva` `O` tal que
> `O.instructorId = R.instructor` ∧ `O.tipoActividadId = R.tipoActividad` ∧ `O.salonId = R.salon`
> ∧ `!R.horaInicio.isBefore(O.horaInicio)` ∧ `!R.horaFin.isAfter(O.horaFin)`.

Un solo criterio cubre los cinco escenarios del brief:

| Escenario | Resultado |
|---|---|
| **A** — cancelar ocurrencia con reservas confirmadas | Deja de haber ocurrencia que la cubra → **409** |
| **B** — cambiar horario dejando la reserva fuera | No hay contención → **409**. Si la reserva sigue contenida en el rango nuevo → **permitido** |
| **C** — cambiar instructor | El instructor de la reserva ya no coincide → **409** |
| **D** — cambiar actividad | La actividad ya no coincide → **409** |
| **E** — mover de salón | El salón ya no coincide → **409** |

La cobertura exige **contención completa**, no solape, con la misma semántica que
`HorarioEfectivo.contiene` y `CambioExcepcionHorario.admite`: es la convención del proyecto y no se
introduce una tercera.

Reservas `CANCELADA` no se consultan: ya no comprometen nada (criterio idéntico al de
`ImpactoPuntualEnExcepcionHorario`).

### Port neutral, sin ciclo de beans

`Reserva` vive en `calendario`; el writer de ajustes vive en `programacion`. Para no crear la
dependencia `programacion → calendario` se replica el patrón ya probado en F2C:

```
programacion/dominio/ValidadorImpactoAjusteProgramacion   (port, en programacion)
programacion/dominio/CambioProgramacionFecha              (value object neutral)
programacion/dominio/ConflictoOcurrenciaComprometida      (value object neutral)
calendario/servicio/ImpactoReservasEnAjusteProgramacion   (adapter, en calendario)
```

Dirección de dependencias resultante: `calendario → programacion → ubicaciones`. No hay ciclo.
El adapter depende **sólo de `ReservaRepository`**, nunca de `ReservaService` — la misma razón
documentada en `ImpactoTurnosRecurrentesEnHorario`.

Se reutiliza `ReservaRepository.findBySalonIdAndFechaAndEstado(salonId, fecha, CONFIRMADA)`, que ya
existe. Para el `REEMPLAZO` con cambio de salón se consulta también el salón destino.

### Dependencia circular writer ↔ resolver (§43)

No existe. La dirección es única:

```
AjusteProgramacionFechaService  ──depende de──▶  ProgramacionEfectiva  ──depende de──▶  repositorios + HorarioEfectivoSalon
```

`ProgramacionEfectiva` **no conoce al writer**. El writer valida llamando dos veces al resolver
dentro de su transacción, con los locks ya tomados:

1. `efectivaAntes = resolver(...)` sobre el estado persistido;
2. `efectivaDespues = simular(efectivaAntes, ajustePropuesto)` — proyección **en memoria**, sin
   escribir nada, aplicando los pasos 6–9 del orden de composición al ajuste propuesto.

Sobre `efectivaDespues` se validan: contención en horario efectivo, solape global de instructor y
reservas comprometidas. **Nada se persiste antes de que todas las validaciones pasen** — mismo
principio que `SalonHorarioExcepcionService` ("un rechazo no persiste nada").

Esto también resuelve §43 en su fondo: el writer **no consulta sólo la tabla de ajustes**; valida
contra la vista efectiva completa, que incluye recurrentes aplicables, otros ajustes, el horario
operativo y —vía `porInstructorYFecha`— los otros salones.

---

## Temporalidad

**Fecha atómica, como F2C. No se usa `LocalTime.now()` en ninguna regla.** El modelo no tiene
concepto intra-día y `Sesion` no existe todavía; introducir una hora de corte inventaría semántica
que nadie puede validar.

| Momento | Política |
|---|---|
| **Pasado** (`fecha < LocalDate.now(reloj)`) | **INMUTABLE**. Crear, modificar y retirar un ajuste se rechazan con código estable |
| **Hoy** (`fecha == LocalDate.now(reloj)`) | **MUTABLE**, con las mismas validaciones que el futuro. El único freno adicional es el de reservas confirmadas incompatibles → `409` |
| **Futuro** | **MUTABLE** |

Es exactamente la política que `SalonHorarioExcepcionService` ya aplica
(`EXCEPCION_HORARIO_EN_EL_PASADO`), y mantenerla idéntica evita que dos objetos por fecha del mismo
sistema tengan reglas temporales distintas.

### Clock

`java.time.Clock` inyectado desde `config/RelojConfig`. **Cero `LocalDate.now()` sin `Clock`** en
todo el código nuevo. El resolver `ProgramacionEfectiva` **no recibe `Clock`**: la fecha entra
siempre por parámetro, igual que en `HorarioEfectivoSalon`, para que sea determinista y consultable
en fechas arbitrarias. El `Clock` vive únicamente en el writer.

---

## Persistencia

**Se necesita tabla nueva.** Comprobado contra el modelo real: `turno_instructor` no puede cubrir
el dominio sin romper API viva (ver *Hallazgo legacy*), y `programacion_asignacion` no puede
albergar un ajuste sin convertir una regla recurrente en una ocurrencia de un solo día — prohibido
por §5.

### Propuesta de DDL — `V47__programacion_ajuste_fecha.sql`

**PROPUESTA. NO SE HA CREADO EL ARCHIVO.** `V47` verificado libre (última existente: `V46`).

```sql
CREATE TABLE programacion_ajuste_fecha (
    id                    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    fecha                 DATE NOT NULL,
    tipo                  VARCHAR(16) NOT NULL,
    asignacion_serie_id   UUID,
    salon_id              UUID NOT NULL REFERENCES salon (id),
    instructor_id         UUID REFERENCES usuario (id),
    tipo_actividad_id     UUID REFERENCES tipo_actividad (id),
    hora_inicio           TIME WITHOUT TIME ZONE,
    hora_fin              TIME WITHOUT TIME ZONE,
    activo                BOOLEAN NOT NULL DEFAULT true,
    creado_en             TIMESTAMPTZ NOT NULL DEFAULT now(),
    actualizado_en        TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT chk_ajuste_tipo
        CHECK (tipo IN ('CANCELACION', 'REEMPLAZO', 'ADICION')),

    -- Forma por tipo. Un solo CHECK excluyente: no hay combinación válida fuera de estas tres.
    CONSTRAINT chk_ajuste_forma_por_tipo CHECK (
        (tipo = 'CANCELACION'
            AND asignacion_serie_id IS NOT NULL
            AND instructor_id IS NULL AND tipo_actividad_id IS NULL
            AND hora_inicio IS NULL AND hora_fin IS NULL)
     OR (tipo = 'REEMPLAZO'
            AND asignacion_serie_id IS NOT NULL
            AND instructor_id IS NOT NULL AND tipo_actividad_id IS NOT NULL
            AND hora_inicio IS NOT NULL AND hora_fin IS NOT NULL)
     OR (tipo = 'ADICION'
            AND asignacion_serie_id IS NULL
            AND instructor_id IS NOT NULL AND tipo_actividad_id IS NOT NULL
            AND hora_inicio IS NOT NULL AND hora_fin IS NOT NULL)
    ),

    CONSTRAINT chk_ajuste_rango
        CHECK (hora_fin IS NULL OR hora_fin > hora_inicio)
);

-- §32: MAXIMO UN AJUSTE ACTIVO por (target, fecha). No puede haber CANCELACION y REEMPLAZO
-- activos a la vez sobre el mismo target. Parcial sobre asignacion_serie_id IS NOT NULL,
-- de modo que NO restringe multiples ADICIONES legitimas (§33).
CREATE UNIQUE INDEX idx_programacion_ajuste_target_unico
    ON programacion_ajuste_fecha (asignacion_serie_id, fecha)
    WHERE activo AND asignacion_serie_id IS NOT NULL;

CREATE INDEX idx_programacion_ajuste_salon_fecha
    ON programacion_ajuste_fecha (salon_id, fecha) WHERE activo;

CREATE INDEX idx_programacion_ajuste_instructor_fecha
    ON programacion_ajuste_fecha (instructor_id, fecha) WHERE activo;
```

### Decisiones de persistencia justificadas

- **Sin FK sobre `asignacion_serie_id`**: `serie_id` no es única en `programacion_asignacion` por
  diseño (una serie tiene N versiones). La integridad la sostienen el writer y los tests.
- **Sin `EXCLUDE USING gist` para el solape de instructor.** Se evaluó (`btree_gist` está
  disponible desde `V44`) y **se descarta**: una constraint sólo puede excluir filas de *esta*
  tabla entre sí, y el solape real que hay que impedir es contra **ocurrencias derivadas de reglas
  recurrentes**, que no son filas. Una constraint parcial daría una falsa sensación de garantía
  sobre el caso más peligroso. La garantía la da `InstructorLock` (abajo).
- **`tipo` como `VARCHAR` + `CHECK`**, no `ENUM` de PostgreSQL: es la convención del proyecto
  (`turno_instructor.tipo`, `reserva.estado`) y evita DDL para añadir un valor.
- **`salon_id` NOT NULL también en `CANCELACION`**: se deriva del bloque del target al escribir y
  sostiene el índice de scoping, la autorización y el protocolo de lock.

### Ciclo de vida — mutabilidad e histórico (§34)

Semántica **REPLACEMENT por target**, idéntica a la de `SalonHorarioExcepcion`, para no tener dos
modelos de ciclo de vida distintos para objetos por fecha en el mismo sistema:

| Situación | Comportamiento |
|---|---|
| Existe fila activa para `(serie, fecha)` y el contenido es **idéntico** | **NO-OP real**: ni `save` ni `flush`. `actualizado_en` no cambia. `200` con la fila existente |
| Existe fila activa y el contenido **difiere** (incluido cambio de `tipo`) | **UPDATE de la misma fila**. Sin historial de ediciones intermedias |
| Sólo existen filas **inactivas** para `(serie, fecha)` | **INSERT de fila nueva**. Nunca se reactiva una vieja |
| Retirada del ajuste | **Soft delete** (`activo = false`). Nunca `DELETE` físico |
| `ADICION` | No tiene target: siempre `INSERT`. Se retira por `ajusteId` |

- **Pasado inmutable**, futuro y hoy mutables.
- **No se versiona** el ajuste (no hay `serieId` de ajuste). Un ajuste es una decisión puntual sobre
  una fecha concreta; versionarlo sería modelar historia de ediciones que ningún consumidor pide y
  que F2C decidió explícitamente no conservar para el objeto análogo.

---

## Concurrencia

### `SalonLock` NO basta — demostración

```
Tx A: ADICION  Ariadna 09–11 en Juriquilla  →  SalonLock(Juriquilla)
Tx B: ADICION  Ariadna 10–12 en Cimatario   →  SalonLock(Cimatario)
```

Locks disjuntos. Ambas leen un estado en el que la otra no existe, ambas validan sin solape, ambas
commitean. **Ariadna queda en dos salones a la vez.** `SalonLock` no puede resolverlo: el recurso
en disputa no es el salón, es el instructor.

### Protocolo cerrado

**`SalonLock` sigue siendo la raíz para el eje operativo** (compatibilidad con
`HorarioOperacion`/`SalonHorarioExcepcion`, que es donde vive la invariante de F2B/F2C), y **se
añade `InstructorLock` para el eje de solape físico del instructor.**

```java
// programacion/servicio/InstructorLock.java  — mismo patrón exacto que SalonLock
@Transactional(propagation = Propagation.MANDATORY)
public Usuario adquirir(UUID instructorId)   // SELECT ... FOR UPDATE sobre usuario
```

- `Propagation.MANDATORY` por la misma razón documentada en `SalonLock`: con `REQUIRED`, invocarlo
  fuera de transacción abriría una propia, tomaría el lock y lo soltaría de inmediato — un fallo
  silencioso que ningún test notaría.
- Requiere `UsuarioRepository.bloquearParaActualizar(UUID)` (`@Lock(PESSIMISTIC_WRITE)`), que **hoy
  no existe** y es aditivo.
- **No** se usan `synchronized`, `ReentrantLock`, advisory locks ni locks distribuidos: el
  `FOR UPDATE` vive en la base y funciona con varias instancias.

### Conjunto de locks por tipo de ajuste

| Tipo | Salones a bloquear | Instructores a bloquear |
|---|---|---|
| `CANCELACION` | salón del target | **ninguno** |
| `ADICION` | salón del ajuste | instructor del ajuste |
| `REEMPLAZO` sin cambio de salón | ese salón | instructor **resultante** |
| `REEMPLAZO` con cambio de salón | salón origen **y** destino | instructor **resultante** |

**Por qué `CANCELACION` no toma `InstructorLock`**: sólo suprime una ocurrencia. No puede crear un
solape. Tomar el lock daría falsa sensación de protección — el mismo razonamiento por el que
`TurnoInstructorService` excluye a `CANCELACION` de `SalonLock`.

**Por qué se bloquea sólo el instructor RESULTANTE y no también el original** de un `REEMPLAZO` que
cambia de instructor: liberar un hueco no puede crear un conflicto. El escenario adverso —A libera
un hueco de `I2` mientras B intenta ocuparlo— es **fail-closed**: B lee estado ya commiteado, ve la
ocurrencia todavía presente y rechaza conservadoramente. Nunca produce un solape real. Añadir el
lock del instructor original agrandaría el lock set sin ganar ninguna invariante.

### Lock ordering — orden global único

> **Primero TODOS los salones implicados, en orden ascendente de `UUID`.
> Después TODOS los instructores implicados, en orden ascendente de `UUID`.**

Este orden es **global para todo el sistema**, no sólo para F2D:

- Es compatible con los writers existentes sin tocarlos: `SalonHorarioExcepcionService`,
  `VersionarHorarioOperacion`, `CerrarHorarioOperacion`, `TurnoInstructorService`,
  `BloqueProgramacionService` y `ReservaService` toman **únicamente** `SalonLock`. Ninguno toma un
  lock de instructor, así que **nadie puede adquirir un instructor antes que un salón**. La clase
  "salones antes que instructores" nunca se invierte.
- Cubre el swap de salones del §42:
  ```
  Tx A: REEMPLAZO Juriquilla → Cimatario
  Tx B: REEMPLAZO Cimatario → Juriquilla
  ```
  Ambas ordenan `{Juriquilla, Cimatario}` por UUID y adquieren en el mismo orden. **Sin deadlock.**
- Cubre el cambio de instructor del §41: el lock set se calcula a partir del comando y se ordena.
- El writer expone `Set<UUID> salones`/`Set<UUID> instructores` derivados del comando y una única
  rutina `adquirirEnOrden(...)`. **No queda para implementación**: es parte del diseño.

### Protocolo completo, en orden estricto

```
1. AUTORIZAR   sobre salón(es) implicado(s)        ← antes de retener ningún lock
2. VALIDAR forma/temporalidad del comando          ← puro, sin BD, sin locks
3. LOCKS       salones (asc UUID), luego instructores (asc UUID)
4. LEER        target, salones, instructor, actividad, horario efectivo, ajustes activos
5. RESOLVER    programación efectiva actual + proyección con el ajuste propuesto
6. VALIDAR     horario efectivo, especialidad/rol/actividad, solape global de instructor,
               reservas confirmadas comprometidas
7. PERSISTIR   upsert / no-op / soft delete
8. COMMIT      (libera todos los locks)
```

Los pasos 1 y 2 van antes del lock deliberadamente: **no se retienen locks por peticiones sin
permiso ni por comandos malformados** (mismo criterio que `TurnoInstructorService.crear`). El lock
se toma **antes de leer el estado sobre el que se decide** (paso 3 antes del 4), que es el
protocolo obligatorio de `SalonLock`.

---

## API preliminar

**NO SE IMPLEMENTA EN F2D.1.** Diseño preliminar, sujeto a cierre en F2D.2.

Las rutas **no** cuelgan de `/api/salones/{id}/horarios` ni de `/api/salones/{id}/excepciones-horario`:
esto es **Programación**, no horario operativo, y mezclarlos volvería a acoplar dos ejes que F2C
separó. Sí cuelgan de `/api/salones/{salonId}/...` porque el salón es la unidad de autorización y de
lock, y tenerlo en el path permite autorizar y bloquear **antes** de leer el target.

### Escritura

| Método | Ruta | Semántica |
|---|---|---|
| `POST` | `/api/salones/{salonId}/programacion/ajustes` | Crear/upsert un ajuste. Body lleva `tipo`, `fecha`, y según tipo `asignacionSerieId` / `instructorId` / `tipoActividadId` / `horaInicio` / `horaFin` / `salonDestinoId` |
| `DELETE` | `/api/salones/{salonId}/programacion/ajustes/{ajusteId}` | Retirar un ajuste (soft delete). `404` indistinguible entre "no existe" y "existe en otro salón" — corrección de seguridad ya aplicada en F2C.2 |
| `GET` | `/api/salones/{salonId}/programacion/ajustes?desde&hasta` | Listar ajustes activos del rango |

### Lectura efectiva (§46)

| Método | Ruta | Semántica |
|---|---|---|
| `GET` | `/api/salones/{salonId}/programacion/efectiva?fecha=` | `List<OcurrenciaEfectivaResponse>` del salón en esa fecha |
| `GET` | `/api/instructores/{instructorId}/programacion/efectiva?fecha=` | Ocurrencias del instructor **en todos los salones** |

Mobile futuro consume **programación efectiva**, nunca reglas recurrentes: es el detector directo
de la mutación N.

---

## Seguridad

**No se inventa ningún permiso.** El catálogo actual ya tiene el vocabulario exacto, y usar
`salon.administrar` sería incorrecto: administrar el horario **operativo** de un salón y programar
**quién imparte qué** son autorizaciones distintas, hoy ya separadas.

| Operación | Permisos aceptados |
|---|---|
| `ADICION` / `REEMPLAZO` | `calendario.gestionar` **o** `calendario.editar` |
| `CANCELACION` | `calendario.gestionar` **o** `calendario.cancelar` |
| Retirar un ajuste | mismo permiso que el tipo del ajuste retirado |
| Lectura de ajustes y de programación efectiva | `calendario.leer` |

Es la misma matriz que `TurnoInstructorController.crear` ya aplica por tipo, lo que mantiene una
sola política de permisos para "programación puntual" en todo el sistema.

### Scope

Vía `AutorizadorSalon.verificarAccesoSalon(actorId, salonId, permisos...)`, que ya exige que el
permiso **y** el alcance provengan de la misma asignación `UsuarioRol`.

**Cross-salon (§12/§47): un `REEMPLAZO` que mueve de salón exige `verificarAccesoSalon` sobre
AMBOS salones, origen y destino, ANTES de tomar ningún lock.** Un actor con scope sólo sobre
Juriquilla no puede mover programación a Cimatario. Es el detector de la mutación I.

Orden cerrado: **autorización → locks → lecturas → validación → persistencia.**

---

## Idempotencia

Semántica cerrada y coherente con F2C:

| Comando repetido | Resultado |
|---|---|
| `CANCELACION` idéntica sobre `(serie, fecha)` ya cancelada | **NO-OP real**, `200` con la fila existente. Ni `save` ni `flush`; `actualizado_en` no cambia |
| `REEMPLAZO` con contenido idéntico sobre el mismo target | **NO-OP real**, `200` |
| `REEMPLAZO` con contenido distinto sobre un target ya ajustado | **UPDATE de la misma fila**, `200`. Re-valida todo |
| `CANCELACION` sobre un target con `REEMPLAZO` activo (o viceversa) | **UPDATE de la misma fila**, cambiando `tipo`. Nunca coexisten dos ajustes activos (§32) |
| `ADICION` repetida con mismo instructor y mismo rango | **`409`** por solape global del instructor consigo misma |
| `ADICION` en mismo salón y hora con **otro** instructor | **`201`**, legítima (§23) |
| `DELETE` de un ajuste ya inactivo | **`404`** con código estable |

No se usa `409` para el caso idempotente puro: repetir exactamente el mismo comando es la
definición de idempotencia, y devolver conflicto obligaría a los clientes a distinguir "conflicto
real" de "reintento", que es justo lo que F2C decidió evitar.

### Códigos de error estables

Convención del proyecto: el código es el **prefijo del mensaje** de la `ValidacionException`, y la
whitelist de traducción a `409` es **cerrada**; todo lo demás sigue siendo `400`.

Clase propuesta: `programacion/servicio/AjusteProgramacionErrores`.

| Código | HTTP | Significado |
|---|---|---|
| `AJUSTE_PROGRAMACION_EN_EL_PASADO` | 400 | `fecha < hoy` |
| `AJUSTE_PROGRAMACION_FORMA_INVALIDA` | 400 | Campos incoherentes con el `tipo` |
| `ASIGNACION_OBJETIVO_NO_EXISTE` | 404 | Sin versión activa y vigente de la serie en `fecha`, o su bloque no pertenece al salón |
| `AJUSTE_FUERA_DE_HORARIO_EFECTIVO` | 400 | No cabe en `HorarioEfectivoSalon(salon, fecha)` |
| `SALON_NO_OPERATIVO_EN_FECHA` | 400 | `CERRADO` o `NO_OPERATIVO` |
| `INSTRUCTOR_SIN_ESPECIALIDAD` | 400 | Actividad ∉ especialidades |
| `INSTRUCTOR_NO_HABILITADO_EN_SALON` | 400 | Sin rol `INSTRUCTOR` en el salón destino |
| `ACTIVIDAD_NO_OFRECIDA_EN_SALON` | 400 | Actividad ∉ `salon.tiposActividad` |
| **`INSTRUCTOR_CON_PROGRAMACION_TRASLAPADA`** | **409** | Solape global del instructor, incluido cross-salon |
| **`RESERVA_CONFIRMADA_INCOMPATIBLE_CON_AJUSTE`** | **409** | Reservas confirmadas quedarían sin cobertura |
| **`CONFLICTO_AJUSTE_PROGRAMACION`** | **409** | Backstop de `idx_programacion_ajuste_target_unico` (23505) |
| `AJUSTE_PROGRAMACION_NO_EXISTE` | 404 | Retirada sin ajuste activo, o ajeno al salón |

Los tres códigos `409` se traducen con el mismo patrón `traduciendoConflictosDeEstado` +
`saveAndFlush` dentro del traductor que ya usa `SalonHorarioExcepcionService`, para que el `23505`
sea capturable.

---

## Integración futura con confirmación / sesión

F2D **no** implementa `PENDIENTE_CONFIRMACION`, `CONFIRMADA` ni `NO_DISPONIBLE`, y **no** crea
`Sesion` (§49, §50). Sólo garantiza lo necesario para que existan después:

1. **Identidad suficiente**: `ReferenciaOcurrencia` es estable y calculable sin materializar nada.
   Un `REEMPLAZO` conserva la referencia del target, así que una confirmación previa no se
   invalida.
2. **Determinismo sobre un horizonte**: reglas + ajustes + fecha ⇒ el mismo resultado, siempre.
   Sin `now()` en el resolver.
3. **Fuente única**: la materialización futura de `Sesion` consumirá `ProgramacionEfectiva`, no
   recompondrá recurrentes + ajustes por su cuenta.
4. **Datos conservados para capacidad** (§51): `actividad`, `salón` y `rango` viajan en cada
   `OcurrenciaEfectiva`, para que el cálculo de recursos sea posible después sin cambiar el
   resolver.

**Punto de extensión documentado (§52)**: no hay broker ni outbox. Si una fase futura necesita
rematerializar sesiones al crear/retirar un ajuste, el punto es **el final de
`AjusteProgramacionFechaService`, tras la persistencia y dentro de la misma transacción**. No se
implementa nada hoy.

---

## Test plan

Safety net posterior, separada por naturaleza. Sin número artificial de métodos: la lista es de
**hechos que deben quedar demostrados**.

### Unit — Mockito, sin BD

`ProgramacionEfectivaTest`, `AjusteProgramacionFechaServiceTest`, `OcurrenciaEfectivaTest`,
`ReferenciaOcurrenciaTest`, `AjusteProgramacionErroresTest`

1. Recurrente sin ajuste **aparece** en la fecha.
2. `CANCELACION` puntual **suprime sólo `D`**.
3. El **lunes siguiente reaparece** (misma serie, `D+7`).
4. `REEMPLAZO` cambia **hora**.
5. `REEMPLAZO` cambia **instructor**.
6. `REEMPLAZO` cambia **actividad**.
7. `ADICION` puntual **sin template** aparece.
8. Salón `CERRADO` → recurrentes omitidos y `ADICION`/`REEMPLAZO` rechazados.
9. Salón `NO_OPERATIVO` → idéntico a 8.
10. Horario especial **limita** el rango admisible del ajuste (09–11 inválido, 11–13 válido).
11. Recurrente **parcialmente fuera → OMITIDA**.
12. Recurrente **NO recortada**: el resultado nunca contiene un rango distinto al del template.
13. `REEMPLAZO` sobre un target omitido **sí** produce su ocurrencia (§17).
14. Solape de instructor en el **mismo** salón → `409`.
15. Solape de instructor **cross-salon** → `409`.
16. Cambio de salón puntual válido.
17. Cambio de instructor **sin especialidad** → `400`.
18. Cambio de actividad **no ofrecida por el salón** → `400`.
19. `Clock` fijo: pasado inmutable / hoy / futuro.
20. Orden de composición: el horario efectivo se consulta **antes** de aplicar ajustes.
21. Determinismo: dos invocaciones idénticas devuelven listas **iguales en orden y contenido**.
22. Adyacencia permitida: `08–10` y `10–12` no se consideran solape.

### Impacto — adapter + integración con el writer

23. Reservas confirmadas **bloquean** la cancelación incompatible.
24. Reserva bloquea el cambio de horario **incompatible**; el cambio **compatible** (la reserva
    sigue contenida) **se permite**.
25. Reserva bloquea cambio de **instructor**, de **actividad** y de **salón**.
26. Reserva `CANCELADA` **no** bloquea.
27. `ImpactoAjustesEnExcepcionHorario`: cerrar el salón con un `REEMPLAZO`/`ADICION` activo fuera de
    horario → `409` en `SalonHorarioExcepcionService`, **sin tocar ese writer**.
28. `CANCELACION` **no** bloquea un cierre de salón.

### Integration PostgreSQL — Testcontainers

`AjusteProgramacionFechaPersistenciaTest`

29. `chk_ajuste_forma_por_tipo` rechaza cada combinación inválida de los tres tipos.
30. `idx_programacion_ajuste_target_unico` impide dos ajustes activos para el mismo target.
31. El mismo índice **permite** múltiples `ADICION` en el mismo `(salón, fecha)`.
32. Soft delete libera el índice: retirar y volver a crear funciona e inserta **fila nueva**.
33. Upsert REPLACEMENT: activa + idéntico = no-op (`actualizado_en` intacto); activa + distinto =
    misma fila; sólo inactivas = fila nueva.
34. `23505` se traduce a `CONFLICTO_AJUSTE_PROGRAMACION`, no a `409` genérico sin código.
35. FKs a `salon`, `usuario`, `tipo_actividad` rechazan ids inexistentes.

### Concurrency — patrón de `HorarioOperacionConcurrenciaTest` / `SalonHorarioExcepcionConcurrenciaTest`

36. Dos ajustes concurrentes sobre el **mismo target** → uno gana, el otro ve el estado del primero.
37. Dos `ADICION` concurrentes del **mismo instructor en dos salones distintos** → exactamente una
    commitea. **Es el test que falla sin `InstructorLock`.**
38. Swap `A→B` / `B→A` entre dos salones **sin deadlock**, con ventana de observación.
39. Ajuste concurrente con cierre de salón de esa fecha → serializados por `SalonLock`.
40. `InstructorLock` con `Propagation.MANDATORY` falla fuera de transacción.

### API / resolver

41. Autorización cross-salon: mover a un salón sin scope → `403`.
42. `calendario.leer` no permite escribir; `calendario.cancelar` no permite `ADICION`.
43. `GET .../programacion/efectiva` devuelve `origen` y `referencia` correctos para los tres casos.
44. `GET /api/instructores/{id}/programacion/efectiva` devuelve ocurrencias de **varios salones**.
45. Idempotencia HTTP: repetir el mismo `POST` devuelve `200` no-op, no `409`.

### Arquitectura

46. `ProgramacionEfectiva` **no** depende de `calendario` (detector de la mutación K).
47. Cero `LocalDate.now()` / `LocalTime.now()` sin `Clock` en el código nuevo (detector de P).

---

## Mutaciones adversariales

Cada mutación del §55, con su detector concreto:

| # | Mutación | Detector |
|---|---|---|
| **A** | Un ajuste modifica el template recurrente | Tests 2/3 + aserción directa: tras cualquier ajuste, las filas de `programacion_bloque` y `programacion_asignacion` (incluido `actualizado_en`) son **byte a byte idénticas** |
| **B** | La cancelación afecta al lunes siguiente | Test 3: `D+7` sigue produciendo la ocurrencia |
| **C** | Un reemplazo parcial recorta el template implícitamente | Test 12: el resolver nunca emite un rango que no sea o el del template, o el escrito explícitamente en un ajuste |
| **D** | Salón cerrado acepta una adición puntual | Tests 8/9 |
| **E** | Ajuste fuera de `HorarioEfectivoSalon` | Test 10 + paso 9 del orden de composición (test 20) |
| **F** | Instructor en dos salones simultáneos | Test 15 (secuencial) + test 37 (concurrente) |
| **G** | Dos writers en salones distintos ganan la carrera del mismo instructor | **Test 37**. Rojo determinista si se elimina `InstructorLock` |
| **H** | Cambio de instructor no valida especialidad | Test 17 |
| **I** | Cambio de salón no autoriza el salón destino | Test 41 |
| **J** | Reserva confirmada queda huérfana | Tests 23/24/25 |
| **K** | `TurnoInstructor` legacy y ajustes nuevos producen doble ocurrencia | **Test 46** (arquitectura): `ProgramacionEfectiva` no importa nada de `calendario`. Además, imposible por construcción: `programacion_*` no tiene writer expuesto |
| **L** | Dos ajustes activos para el mismo target | Test 30 (constraint real de BD) + test 36 (concurrente) |
| **M** | El resolver aplica ajustes antes de conocer el horario operativo | Test 20: con un mock de `HorarioEfectivoSalon`, se verifica el orden de invocación |
| **N** | Mobile futuro necesita interpretar recurrencia | Tests 43/44: los endpoints efectivos devuelven ocurrencias resueltas, sin `diaSemana` ni vigencias en el DTO |
| **O** | Pasado editable | Test 19 con `Clock` fijo, sobre crear / modificar / retirar |
| **P** | `LocalDate.now()` directo | **Test 47**: barrido del código nuevo |

---

## Riesgos

1. **Coexistencia de dos modelos de programación.** Mitigado por consumidores disjuntos y por el
   hecho verificado de que `programacion_*` no tiene camino de escritura en producción. **Riesgo
   residual: si una fase futura expone escritura de `programacion_*` sin migrar el legacy, la
   validación cruzada de solape de instructor faltará.** La condición de salida está escrita arriba
   y el test 46 es su recordatorio ejecutable.
2. **`programacion_asignacion` no tiene constraint de no-solape de vigencias por serie.** A
   diferencia de `horario_operacion` (`ex_horario_operacion_vigencia`, `V45`), nada impide dos
   versiones vigentes de la misma serie a la vez. El resolver lo detecta y lanza
   `IllegalStateException` (patrón de `HorarioOperacionResolver`), pero es **detección, no
   prevención**. Endurecer `programacion_asignacion`/`programacion_bloque` con `EXCLUDE USING gist`
   es una tarea de F1C, **fuera de F2D**, y se deja anotada aquí explícitamente.
3. **`InstructorLock` sobre la fila `usuario`.** Bloquea al usuario para cualquier otro writer que
   llegue a tomar ese lock en el futuro. Hoy no hay ninguno, así que la contención es nula, pero
   crece la superficie de lock del sistema. Mitigado por el orden global "salones → instructores".
4. **El criterio de cobertura de reservas es funcional, no referencial.** Al no haber FK de
   `Reserva` hacia la programación, la comparación es por `(salon, instructor, actividad, rango)`.
   Es **conservador** (fail-closed: ante la duda rechaza), pero si en el futuro dos ocurrencias del
   mismo instructor y actividad coexisten en el mismo salón, una reserva podría considerarse
   cubierta por la ocurrencia "equivocada". No produce huérfanas; sí puede permitir un ajuste que un
   humano consideraría discutible. Aceptado y documentado.
5. **`ConflictoProgramacionPuntual.Origen` gana un valor nuevo.** Es aditivo y `ubicaciones` sigue
   sin conocer `programacion`, pero es la única clase existente que F2D toca.
6. **Volumen de validación por comando.** Un `REEMPLAZO` cross-salon resuelve la programación
   efectiva de dos salones y de un instructor a nivel global. Con los índices parciales propuestos
   son consultas por `(salon_id, fecha)` / `(instructor_id, fecha)`, acotadas por día. Aceptable.

---

## Decisiones cerradas

Ninguna queda como "podría ser A o B".

| # | Decisión | Cierre |
|---|---|---|
| 1 | Legacy vs entidad nueva | **Entidad nueva** `AjusteProgramacionFecha` sobre el modelo `programacion`. `TurnoInstructor` **congelado**, no se toca |
| 2 | Target del ajuste | **`(asignacion_serie_id, fecha)`**. No `asignacion.id`, no `instructor+fecha+hora` |
| 3 | Tipos de ajuste | **`CANCELACION`, `REEMPLAZO`, `ADICION`**. Sin flags combinados |
| 4 | Cancelación | Suprime **una** ocurrencia por target. No valida horario. No cancela "el día del instructor" |
| 5 | Reemplazo | **Una sola fila `REEMPLAZO`**, no `CANCELACION` + `ADICION` |
| 6 | Adición | **Soportada**, sin referencia a template, identidad por UUID |
| 7 | Cambio de instructor | **SÍ**, validando especialidad y rol |
| 8 | Cambio de actividad | **SÍ**, validando especialidad y oferta del salón |
| 9 | Cambio de salón | **SÍ**, con autorización sobre **ambos** salones |
| 10 | Interacción con `TurnoInstructor` legacy | **Ninguna**. Fuentes y consumidores disjuntos. Migración = F2E |
| 11 | Interacción con `Reserva.CONFIRMADA` | **`409` `RESERVA_CONFIRMADA_INCOMPATIBLE_CON_AJUSTE`**. Nunca mover, cancelar ni borrar reservas |
| 12 | Orden de `ProgramacionEfectiva` | Horario efectivo → recurrentes → omisión → cancelaciones → reemplazos → adiciones → omisión puntual → orden determinista |
| 13 | Identidad de la ocurrencia efectiva | `(serieId, fecha)` para `RECURRENTE` y `REEMPLAZO`; `(ajusteId, fecha)` para `ADICION` |
| 14 | Persistencia | **Tabla nueva** `programacion_ajuste_fecha`. Propuesta `V47`, **no creada** |
| 15 | Histórico / mutabilidad | Soft delete + upsert REPLACEMENT por target. Pasado inmutable, hoy y futuro mutables. Sin versionado del ajuste |
| 16 | Lock cross-salon / instructor | **`SalonLock` + `InstructorLock` nuevo**. `SalonLock` solo es insuficiente y está demostrado |
| 17 | Lock ordering | **Salones (asc UUID) → Instructores (asc UUID)**, orden global del sistema |
| 18 | Idempotencia | Repetición idéntica = **no-op real `200`**. Contenido distinto = update de la misma fila. Nunca dos ajustes activos por target |
| 19 | API preliminar | `/api/salones/{salonId}/programacion/ajustes` + `/programacion/efectiva`. **Fuera** de `/horarios` |
| 20 | Permisos | **Ninguno nuevo**: `calendario.{gestionar,editar,cancelar,leer}`. Cross-salon exige ambos |
| 21 | Tests | 47 hechos demostrables, separados en unit / impacto / PostgreSQL / concurrencia / API / arquitectura |
| 22 | `HorarioEfectivoSalon` | **REUTILIZADO SIN CAMBIOS** |
| 23 | Convención de intervalos | Solape half-open `[inicio, fin)` (`a.inicio < b.fin ∧ b.inicio < a.fin`); contención con extremos inclusivos (`HorarioEfectivo.contiene`). **Convención existente del proyecto, no se introduce ninguna nueva** |
| 24 | Recorte | **Nunca automático.** Un ajuste explícito sí puede escribir un rango menor |

---

## Alcance F2D.2

**Incluido:**

1. `programacion/entidad/AjusteProgramacionFecha.java` + `Tipo`.
2. `programacion/repositorio/AjusteProgramacionFechaRepository.java`.
3. Migración **`V47__programacion_ajuste_fecha.sql`** exactamente como se propone arriba.
4. `programacion/dominio/{OcurrenciaEfectiva, ReferenciaOcurrencia, CambioProgramacionFecha,
   ConflictoOcurrenciaComprometida, ValidadorImpactoAjusteProgramacion}`.
5. `programacion/servicio/ProgramacionEfectiva` (resolver único, sin `Clock`).
6. `programacion/servicio/AjusteProgramacionFechaService` (writer, con `Clock`).
7. `programacion/servicio/AjusteProgramacionErrores` (códigos estables + whitelist `409`).
8. `programacion/servicio/InstructorLock` + `UsuarioRepository.bloquearParaActualizar`.
9. `programacion/servicio/ImpactoAjustesEnExcepcionHorario` + valor `AJUSTE_PROGRAMACION` en
   `ConflictoProgramacionPuntual.Origen` (única modificación de una clase existente).
10. `calendario/servicio/ImpactoReservasEnAjusteProgramacion` (adapter del port de reservas).
11. Consultas nuevas en `AsignacionRepository`: vigentes por `(salon, fecha)` y por
    `(instructor, fecha)` cross-salon.
12. Controller `/api/salones/{salonId}/programacion/...` + DTOs.
13. Toda la safety net del test plan.

**Excluido explícitamente:**

- Cualquier cambio en `TurnoInstructor`, `TurnoInstructorService`, `TurnoInstructorController` o su
  API.
- Cualquier cambio en `ReservaService`, `HorarioEfectivoSalon`, `HorarioOperacionResolver`,
  `SalonHorarioExcepcionService`, `SalonLock`, `BloqueProgramacionService`.
- Migración de datos legacy → `programacion` (**F2E**).
- Estados de confirmación de instructor (§49).
- `Sesion` y materialización (§50).
- Capacidad y recursos (§51).
- Broker / outbox (§52).
- `EXCLUDE` de vigencias en `programacion_asignacion` / `programacion_bloque` (endurecimiento de
  F1C).
- Frontend y mobile.
