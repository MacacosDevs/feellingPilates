# FeelingPilates — Fase 1B: Diseño técnico de Programación

Fecha: 2026-08-22 · Branch: `programacion/safety-net-calendario-1a1` · Commit base: `ef3d3da38da06d6d62bb1469df93ee8284521d52` · Tests: 70/70 PASS · Productivo y Flyway sin cambios.

Fase de DISEÑO. No se implementó nada; el único archivo creado es este checkpoint.

## 1. Executive Summary

- El dominio actual no evoluciona por renombre: `TurnoInstructor` es a la vez bloque, regla recurrente, excepción y cancelación, y la PK `(turno, usuario, actividad)` de `TurnoInstructorAsignacion` hace **irrepresentable** el requisito central de segmentos disjuntos con la misma actividad.
- **DECISIÓN:** `programacion_bloque` y `programacion_asignacion` nacen como tablas nuevas y **coexisten** con `turno_instructor`; no hay mutación in situ (§6, §7).
- **DECISIÓN:** la recurrencia semanal **no es una entidad**: es `diaSemana` + `[vigenteDesde, vigenteHasta]` sobre bloque y asignación. No se crean filas por fecha.
- **DECISIÓN:** los ajustes por fecha son **una** entidad con `(ambito, operacion, target, valores)` explícitos — ni `payloadJson`, ni una tabla por acción. Cubre las 11 operaciones pedidas (§9).
- **DECISIÓN:** la programación efectiva se **calcula**, con **una** materialización parcial, `AsignacionFecha`: único ancla posible para la confirmación y para el constraint de conflicto global (§10, §11).
- **DECISIÓN:** `confirmacion_instructor` **no es tabla**; la confirmación es el estado de `AsignacionFecha`, con la que sería 1:1.
- **CONFIRMADO:** `Sesion` debe ser entidad persistente, y pertenece a **Programación** (§15, §20). Cerrar/ampliar/reducir horario se queda en `SalonHorarioExcepcion`, no se duplica como ajuste.
- Primera intervención 1C: crear el package `programacion` con `BloqueProgramacion` y `Asignacion`, tablas vacías, **sin conectar ningún endpoint** (§32).

## 2. Estado actual relevante

Verificado en código, no en documentos.

| Elemento | Hecho verificado | Fuente |
|---|---|---|
| `turno_instructor` | `salon_id, tipo, dia_semana, fecha, hora_inicio, hora_fin, activo`. Sin vigencia. CHECK acopla `tipo`↔(`dia_semana` XOR `fecha`) | `V15` |
| `turno_instructor_usuario` | M:N turno↔usuario: el **bloque** posee a los instructores | `V19` |
| `turno_instructor_asignacion` | PK `(turno_id, usuario_id, tipo_actividad_id)` + rango nullable (`null` en ambas = bloque completo) | `V20`, `V22` |
| Un rango por instructor | `resolverAsignaciones` → `Map<Usuario, AsignacionResuelta>`: **un** rango y un **Set** de actividades; instructor repetido sobrescribe en silencio | `TurnoInstructorService.java:206,254` |
| `horario_operacion` | `UNIQUE (salon_id, dia_semana)`: un intervalo por día, sin vigencia | `V11` |
| `salon_horario_excepcion` | `cerrado` XOR `(apertura, cierre)`; único por `(salon, fecha) WHERE activo` | `V18` |
| `salon` | **sin ninguna columna de política**: ni confirmación ni ventanas de reserva | `Salon.java` |
| `reserva` | copia salón, instructor, cliente, actividad, fecha y horas; sin referencia a ocurrencia | `V15` |
| Exclusividad legada | `existeTraslape(instructor, fecha, rango, CONFIRMADA)` bloquea al segundo cliente | `ReservaRepository.java:22` |
| `tipo_actividad` | `duracion_minutos`, `participantes_por_reserva`, `etiquetas` | `V15`, `V38`, `V40` |
| Recursos | `actividad_recurso.cantidad` = unidades **totales** por reserva (tabla vacía); `salon_recurso.cantidad` = inventario por `salon+tipo_recurso` | `V36` |
| Autorización | `verificarAccesoSalon(actorId, salonId, permisos…)`; permiso y scope de la **misma** asignación `UsuarioRol`. Permisos `calendario.leer/gestionar/editar/cancelar`, `reserva.administrar` | `AutorizadorSalon.java`, `V15`, `V16` |
| Día de semana | `0 = domingo … 6 = sábado`, consistente en ambos servicios y en `extract(dow …)` | `V15` |

Bugs conocidos (1A §C, caracterizados en 1A.1, **no** congelados): EXCEPCION rechazada por traslape contra el recurrente que pretende sustituir; CANCELACION anula el día completo; la reserva no valida actividad/rango asignado ni revalida horario efectivo; instructor repetido se sobrescribe; traslape sin protección concurrente.

## 3. Principios

1. La recurrencia es un atributo (`diaSemana` + vigencia), no una entidad.
2. Un sustantivo se vuelve tabla sólo si necesita identidad estable, estado propio, o ser bloqueado en una transacción.
3. Intervalos `[inicio, fin)` en todo el stack: tocar bordes es válido, intersección positiva no.
4. La historia no se reescribe: una regla se cierra por vigencia y nace otra fila.
5. Se calcula la composición efectiva; se materializa sólo lo que necesita estado o lock.
6. Aditivo antes que destructivo: el modelo nuevo se completa en paralelo antes de mover una sola lectura.
7. Ninguna dependencia de Programación hacia Reservas, en ninguna dirección de import.

## 4. Modelo de dominio objetivo

Clasificación de cada concepto del pipeline. Esto es lo que evita convertir cada sustantivo en tabla.

| Concepto | Naturaleza | Por qué |
|---|---|---|
| Horario operativo semanal | **ENTIDAD** (`HorarioOperacion`, evoluciona) | ya existe; necesita historia |
| Excepción de horario | **ENTIDAD** (`SalonHorarioExcepcion`, se conserva) | hecho por fecha, con identidad y `activo` |
| Horario efectivo de una fecha | **VO calculado** | derivable de las dos anteriores |
| Políticas del salón | **VO** sobre columnas de `salon` | configuración, no agregado |
| Programación recurrente | **NO ES TABLA** | es `diaSemana` + vigencia en bloque y asignación |
| Bloque | **ENTIDAD** | identidad, vigencia, invariante de no traslape |
| Asignación | **ENTIDAD** | identidad propia obligatoria (§7) |
| Ajuste por fecha | **ENTIDAD** | hecho auditable, con autor y motivo |
| Programación efectiva | **SERVICIO** + **VO** (`ResolverProgramacionFecha` → `ProgramacionEfectiva`) | composición pura |
| Asignación efectiva de una fecha | **ENTIDAD materializada** (`AsignacionFecha`) | único soporte posible de confirmación y EXCLUDE (§10) |
| Confirmación del instructor | **ESTADO de `AsignacionFecha`** | una tabla aparte sería 1:1 |
| Publicable | **POLÍTICA derivada**, no estado | depende del reloj; guardarlo exige un worker que lo voltee |
| Sesión | **ENTIDAD** | §15 |
| Reserva | **ENTIDAD** (existe, evoluciona) | — |
| Capacidad disponible | **PROYECCIÓN CALCULADA** | inventario − consumos vigentes |
| Consumo de recurso | **ENTIDAD** | debe sumarse bajo lock |

## 5. Horario operativo

**`HorarioOperacion` se conserva y EVOLUCIONA con vigencia:** `vigente_desde DATE NOT NULL` (inclusivo) y `vigente_hasta DATE NULL` (inclusivo; `null` = indefinido). El `UNIQUE (salon_id, dia_semana)` actual **impide versionar** y debe ceder a "no dos reglas del mismo salón/día con vigencias intersectadas" — candidato `EXCLUDE USING gist (salon_id =, dia_semana =, daterange &&)` con `btree_gist`. **Se mantiene un intervalo por día:** las jornadas partidas no hacen falta a este nivel porque los bloques ya expresan "no hay clases de 12 a 14"; un segundo intervalo operativo duplicaría esa expresividad.

**`SalonHorarioExcepcion` se CONSERVA sin cambio de modelo.** Ya expresa exactamente lo pedido — `CERRADO`, `07:00-22:00`, `08:00-14:00` — y **no reescribe el semanal**: es una fila aparte por fecha, con `activo` y un único registro vigente por `(salon, fecha)`. Lo que cambia es **quién la respeta**: hoy sólo la consulta `validarDentroDeHorarioSalon` cuando hay fecha; el resolver y la materialización deben consultarla siempre (hoy una reserva sobre un recurrente ignora que el salón está cerrado). `HorarioEfectivo(fecha)` es un VO calculado: `CERRADO`, o `[apertura, cierre)`; si hay excepción activa para la fecha gana entera, si no, la regla semanal vigente de ese `diaSemana`.

**Nuevas columnas de política en `salon`** (owner Operación), expuestas como VO y nunca como entidad: `requiere_confirmacion_instructor BOOLEAN NOT NULL DEFAULT false`, `plazo_confirmacion_horas SMALLINT`, `anticipacion_maxima_reserva_horas INTEGER NOT NULL`, `anticipacion_minima_reserva_horas INTEGER NOT NULL DEFAULT 0`, `margen_materializacion_dias SMALLINT NOT NULL DEFAULT 1`. Se eligen columnas y no una tabla `salon_politica` porque hoy no hay requisito de historia sobre la política; si el negocio pide "esta política aplica desde marzo", se extrae con la misma técnica de vigencia de §8.

## 6. BloqueProgramacion

**Identidad:** `id UUID` propio (`EntidadBase`) — identidad de **esta versión** persistente. **Atributos:** `salonId`, `diaSemana SMALLINT (0..6)`, `horaInicio`, `horaFin`, `vigenteDesde`, `vigenteHasta` (nullable), `activo`, `serieId UUID NOT NULL` — identidad lógica estable de la regla a través de todas sus versiones (§8); dos filas con `id` distinto comparten `serieId` cuando una versiona a la otra. **Ownership:** pertenece al salón; **no** al instructor, **no** a una actividad, **no** a una sesión, **no** a una reserva.

**Invariantes:** (1) `horaFin > horaInicio` con semántica `[inicio, fin)` — `08:00-12:00` + `12:00-14:00` válido, `08:00-12:01` + `12:00-14:00` inválido; (2) contenido en el horario operativo de la regla semanal vigente de ese `diaSemana`, y el resolver **revalida** por fecha contra el horario efectivo, porque un bloque nacido válido puede quedar fuera si ese día se reduce el horario; (3) no traslape con otro bloque activo del mismo salón y día **cuya vigencia se intersecte** — dos bloques `08:00-12:00` de lunes son legítimos si sus vigencias son disjuntas; (4) `vigenteHasta >= vigenteDesde` cuando no es nulo.

**¿`TurnoInstructor` puede evolucionar aditivamente? No — COEXISTENCIA TEMPORAL.** Su CHECK de tabla acopla `tipo` con `(dia_semana XOR fecha)`, y separar bloque de ajuste exige romperlo, es decir, cambiar el significado de cada fila existente. Los instructores cuelgan del turno (`turno_instructor_usuario`) y en el objetivo cuelgan de la asignación: es un cambio de ownership, no una columna nueva. La PK de asignación debe desaparecer (§7), y eso no es aditivo. Y 44 de los 70 tests fijan la semántica actual de esa tabla: mutarla in situ los rompe antes de que exista un modelo que los reemplace. Coexistir significa `turno_instructor` intacto sirviendo los endpoints actuales, tablas nuevas vacías, backfill y switch después (§26 F4/F5) y retiro en F9.

## 7. Asignacion

Nombre `Asignacion`, tabla `programacion_asignacion`; se descarta `AsignacionProgramacion` por redundante dentro del package `programacion`.

**Identidad: UUID propio, no negociable.** La PK actual `(turno_id, usuario_id, tipo_actividad_id)` hace *irrepresentable* el requisito del prompt §5: Ariadna 08:00-10:00 REFORMER y Ariadna 12:00-14:00 REFORMER en el mismo bloque colisionan en PK. No es un fallo de validación, es del esquema.

**Atributos:** `id` (identidad de esta versión), `bloqueId` (FK), `instructorId`, `tipoActividadId` (**NOT NULL, exactamente uno**), `horaInicio` y `horaFin` (**ambas NOT NULL**), `vigenteDesde`, `vigenteHasta` (nullable), `activo`, `serieId UUID NOT NULL` (identidad lógica estable de la regla a través de sus versiones, igual que en `BloqueProgramacion` — §8; distinto de un futuro `origenAsignacionId` de linaje hacia una regla base, que tendría otra semántica y no se crea en esta fase). Se elimina el "null en ambas = bloque completo" de `V22`: obliga a leer el padre para conocer el rango, y el resolver y la materialización necesitan intervalos concretos. **La vigencia propia, además de la del bloque, hace falta:** "Ariadna deja de dar Reformer el 1 de febrero, el bloque sigue igual" no se expresa versionando el bloque; restricción de cordura, la vigencia de la asignación debe estar contenida en la del bloque, y `programacion_asignacion` declara `CHECK (vigente_hasta IS NULL OR vigente_hasta >= vigente_desde)` como cordura básica (§24).

**Constraints:** `CHECK (hora_fin > hora_inicio)`; contención en `[bloque.horaInicio, bloque.horaFin)`; no traslape del **mismo instructor** dentro del bloque con vigencias intersectadas — dos instructores distintos en el mismo rango sí, el mismo instructor dos veces no, porque no puede impartir dos actividades a la vez; actividad **activa**, **ofrecida por el salón** (`salon_tipo_actividad`, hoy no se valida) y **especialidad del instructor** (`instructor_actividad`). El conflicto global entre salones no vive aquí: §11.

**Lo que explícitamente NO se crea:** exclusividad `salón + hora` ni `actividad + salón + hora`. Ariadna REFORMER y Alberto MAT a las 08:00-10:00 en Juriquilla es válido, y Ariadna REFORMER + Alberto REFORMER simultáneos también. La capacidad la resuelven los recursos (§19), no una unicidad de programación.

## 8. Recurrencia y vigencia

`vigenteDesde DATE NOT NULL` inclusivo y `vigenteHasta DATE NULL` inclusivo (`null` = sigue vigente), sobre `horario_operacion`, `programacion_bloque` y `programacion_asignacion`. Editar hacia el futuro = **cerrar** la versión vigente (`vigenteHasta = nuevaVigencia − 1 día`) e **insertar** una fila nueva; nunca `UPDATE` sobre el pasado. El ejemplo del prompt (08-12 hasta el 31 de enero, 09-13 desde el 1 de febrero) son dos filas. **No se generan filas por fecha:** la expansión ocurre sólo en la materialización acotada (§16).

`serieId` encadena versiones de una misma regla lógica: identidad lógica estable de la regla a través de todas sus versiones persistentes, mientras que `id` identifica sólo la versión concreta (§6, §7). Por ejemplo, la regla 08:00-12:00-de-enero y la regla 09:00-13:00-desde-febrero son dos filas con `id` distinto que comparten el mismo `serieId`; permite a la UI mostrar "esta regla cambió" y a `Sesion` conservar linaje aunque la regla se versione. **No se crea `versionAnteriorId` en 1C** — se agrega después sólo si aparece una necesidad concreta de navegar la cadena en reversa. `serieId` es distinto de un futuro `origenAsignacionId`, que representaría linaje hacia una regla base (por ejemplo, una asignación nacida de un ajuste `REEMPLAZAR`) y tiene otra semántica; no se confunden. Enmendar un error del pasado (no versionar) es un `UPDATE` sobre la fila vigente: operación distinta, permiso distinto, y debe rechazarse si ya hay sesiones materializadas en ese rango. Toda validación de traslape debe intersectar **tiempo Y vigencia** — es el error más fácil de cometer al portar la lógica actual, que sólo compara horas.

## 9. Ajustes por fecha

Una sola entidad con vocabulario explícito. Ni `Excepcion(tipo, payloadJson)`, ni once tablas. **`AjusteProgramacionFecha`**: `id`, `salonId`, `fecha`, `ambito ∈ {BLOQUE, ASIGNACION}`, `operacion ∈ {AGREGAR, CANCELAR, REEMPLAZAR}`, `bloqueTargetId`/`asignacionTargetId` (nullable), valores efectivos nuevos `horaInicio`, `horaFin`, `instructorId`, `tipoActividadId` (nullable, obligatorios según operación), `motivo`, `creadoPor`, `activo`.

| Requisito | ambito | operacion | target | valores |
|---|---|---|---|---|
| agregar instructor | ASIGNACION | AGREGAR | bloque | instructor, actividad, rango |
| quitar instructor · cancelar una asignación | ASIGNACION | CANCELAR | asignación | — |
| reemplazar instructor | ASIGNACION | REEMPLAZAR | asignación | instructor |
| modificar horario | ASIGNACION | REEMPLAZAR | asignación | rango |
| cambiar actividad | ASIGNACION | REEMPLAZAR | asignación | actividad |
| crear bloque especial | BLOQUE | AGREGAR | — | rango |
| cancelar bloque recurrente | BLOQUE | CANCELAR | bloque | — |
| modificar bloque temporalmente | BLOQUE | REEMPLAZAR | bloque | rango |
| cerrar salón · ampliar · reducir horario | — | — | — | **`SalonHorarioExcepcion`** |

Las tres últimas quedan deliberadamente fuera: ya tienen dueño en Operación, y duplicarlas crearía dos fuentes de verdad para el mismo hecho.

**Gap confirmado, F3 — bloque especial + asignación especial.** `AjusteProgramacionFecha` **no** resuelve completamente la fila "crear bloque especial". Un bloque agregado por fecha (`BLOQUE/AGREGAR`, ej. `18:00-20:00` en una fecha específica) no tiene un `programacion_bloque.id`: es una fila de ajuste, no una fila de bloque recurrente. Por tanto, un `ASIGNACION/AGREGAR` que quiera colgar de ese bloque especial (ej. Fernando, Reformer, `18:00-20:00`, esa misma fecha) no puede referenciarlo mediante el FK `bloqueTargetId`, que apunta a `programacion_bloque`. **Diseño preferido para F3:** agregar `bloqueAjusteTargetId`, un self-FK de `AjusteProgramacionFecha` hacia otro ajuste con `ambito = BLOQUE` y `operacion = AGREGAR`. Para un `ASIGNACION/AGREGAR` debe existir **exactamente uno** de `bloqueTargetId` o `bloqueAjusteTargetId` — nunca ambos, nunca ninguno. Validaciones adicionales para F3: mismo salón, misma fecha, target activo, y que el target sea realmente un `BLOQUE/AGREGAR` (no un `REEMPLAZAR` ni un `CANCELAR`). Orden de resolución al aplicar los ajustes de una fecha: primero los bloques agregados, después las asignaciones agregadas, para que estas últimas puedan resolver su target. **No se implementa en 1C** — queda registrado como diseño para F3 (§31).

**Composabilidad:** los ajustes son de grano fino y se acumulan; nunca ocurre lo de hoy, donde una EXCEPCION sustituye todos los recurrentes del instructor ese día por su mera presencia. **Sin adivinar precedencias:** el resolver aplica sobre la base recurrente `CANCELAR → REEMPLAZAR → AGREGAR`, en ese orden fijo, y las combinaciones contradictorias (dos `REEMPLAZAR` sobre el mismo target y fecha, o `CANCELAR` + `REEMPLAZAR`) se **rechazan al escribir**, con índice único parcial sobre `(fecha, asignacion_target_id, operacion) WHERE activo`. Un `BLOQUE/CANCELAR` cancela en cascada las asignaciones efectivas de ese bloque esa fecha: es la única cascada, y es determinista.

## 10. Programación efectiva

**`ResolverProgramacionFecha`** — servicio de dominio en `programacion.consulta`. Entrada `salonId + fecha` (opcionalmente `instructorId`); salida VO `ProgramacionEfectiva`. Secuencia: (1) `HorarioEfectivo(salon, fecha)`, y si es `CERRADO` el resultado es vacío con `motivo = SALON_CERRADO`; (2) bloques activos de `(salon, diaSemana(fecha))` cuya vigencia contiene la fecha; (3) asignaciones activas de esos bloques cuya vigencia contiene la fecha; (4) ajustes activos de `(salon, fecha)` en el orden fijo de §9; (5) revalidación contra el horario efectivo, marcando `FUERA_DE_HORARIO` en vez de desaparecer en silencio, para que la operación entienda por qué no hay clases; (6) validación del conflicto global de instructor (§11).

**¿Se persiste, se calcula o se materializa parcialmente? Se calcula, con UNA materialización parcial.** Persistir la resolución completa como autoridad crea una segunda fuente de verdad que hay que invalidar ante cada cambio de horario, regla o ajuste: es el "JSON diario opaco" que hay que evitar. Pero la confirmación del instructor es un **estado** de la asignación efectiva de una fecha, y un estado no se cuelga de un valor calculado. **Y hay un caso que lo vuelve obligatorio:** un ajuste `AGREGAR` produce una asignación efectiva **sin `asignacionId` base**, y un `REEMPLAZAR` de instructor cambia a quién pertenece la confirmación; la clave `(asignacionId, fecha)` no alcanza.

**Se materializa exactamente una cosa: `AsignacionFecha`** (`programacion_asignacion_fecha`), la asignación efectiva de una fecha. Es derivada y reconstruible desde reglas + ajustes; es el ancla del estado de confirmación; es el sujeto del EXCLUDE de conflicto global, que necesita filas concretas; es el linaje de `Sesion`; y se materializa **sólo dentro del horizonte** (§16). Bloques, asignaciones recurrentes y horario efectivo **no** se materializan. El VO completo puede cachearse como proyección descartable invalidada por versión; nunca es autoridad.

## 11. Conflicto global de instructor

Invariante: para un instructor y una **fecha**, sus intervalos efectivos no se traslapan **entre salones**. Lunes Juriquilla 08-10 + martes Cimatario 08-12 es válido; Juriquilla 10-12 + Cimatario 11-13 el mismo día no. Se valida en tres niveles, todos dentro de Programación.

**Nivel 1 — plantilla, síncrono al escribir una asignación recurrente.** Decidible sin expandir fechas: dos asignaciones recurrentes del mismo instructor, mismo `diaSemana`, con vigencias intersectadas y horas intersectadas **colisionarán con certeza** en alguna fecha, sin importar el salón; se rechaza al escribir. El dato necesario es una lectura cross-salón por instructor: está dentro de Programación, no cruza módulos.

**Nivel 2 — ajuste por fecha, síncrono al escribirlo.** Se resuelve la programación efectiva de ese instructor en esa fecha en **todos** los salones y se rechaza si hay intersección.

**Nivel 3 — ocurrencias materializadas, garantía dura, F5.** Sobre `programacion_asignacion_fecha`, que ya tiene `(instructor_id, fecha, [hora_inicio, hora_fin))` concretos. **Aquí sí el EXCLUDE constraint de PostgreSQL es la herramienta correcta**, precisamente porque las filas son ocurrencias finitas y no reglas. PostgreSQL **no** tiene un tipo `timerange` nativo; el rango candidato correcto compone fecha y hora en un `tsrange`: `EXCLUDE USING gist (instructor_id =, tsrange(fecha + hora_inicio, fecha + hora_fin, '[)') &&) WHERE estado <> 'NO_DISPONIBLE'`, con `btree_gist`. Este EXCLUDE **no se crea en 1C**: nace en F5, cuando existe `programacion_asignacion_fecha` (§26). No se decide por dogma en el nivel de recurrencia, donde la fila es una regla con rango de fechas abierto y el constraint sería inexpresable o inútilmente restrictivo.

**Concurrencia (conceptual, no se implementa):** los niveles 1 y 2 son check-then-write y necesitan serialización por instructor — advisory lock transaccional sobre `instructorId`, o `SELECT … FOR UPDATE` sobre una fila del instructor. El nivel 3 es el backstop real. Regla de diseño: **la validación de aplicación explica el error al usuario; el constraint garantiza la invariante.**

## 12. Confirmación

La **política** del salón, en Operación, se expresa con tres cantidades sin ambigüedad de nombre: `requiereConfirmacionInstructor BOOLEAN`, `anticipacionMaximaReservaHoras` (**A**, horas antes del inicio en que se abre la ventana pública de reserva) y `plazoRespuestaConfirmacionHoras` (**P**, cuánto antes de esa apertura debe resolverse la confirmación del instructor). Se reemplaza el nombre ambiguo `plazoConfirmacionHoras` por `plazoRespuestaConfirmacionHoras` para que no se confunda con la ventana de reserva del cliente.

Con `S` = inicio de la asignación efectiva (sesión / `AsignacionFecha`):

```
aperturaCliente        = S − A
deadlineConfirmacion   = aperturaCliente
solicitudConfirmacion <= deadlineConfirmacion − P
```

Es decir: la solicitud de confirmación al instructor debe salir con margen `P` **antes** de que se abra la ventana pública, y el deadline de confirmación coincide con esa apertura. **Consecuencia dura:** cuando se abre la ventana pública (`ahora = aperturaCliente`), la asignación **ya debe estar** `CONFIRMADA` o `NO_DISPONIBLE` — nunca `PENDIENTE_CONFIRMACION` en ese instante. Si eso ocurre es un defecto del horizonte de materialización o del worker de expiración, no un estado válido de negocio.

La **granularidad es la asignación efectiva de una fecha**, no la sesión individual: `31 ago, Ariadna, 08:00-10:00, Reformer` es UN objeto de confirmación, y al confirmarse sus slots `08-09` y `09-10` pasan a ser publicables. **No hay entidad nueva:** la confirmación es el estado de `AsignacionFecha`; una tabla `confirmacion_instructor` sería exactamente 1:1 y obligaría a un join en toda lectura de disponibilidad. Campos: `estado`, `deadline_confirmacion TIMESTAMPTZ NULL`, `confirmado_en`, `confirmado_por`, `version`.

Estados `PENDIENTE_CONFIRMACION` → `CONFIRMADA` | `NO_DISPONIBLE`. Si el salón **no** requiere confirmación, la fila nace `CONFIRMADA` con `deadline` nulo: ni cuarto estado, ni `null` como estado. El `deadline_confirmacion` es `aperturaCliente` (`S − A`), calculado al materializar, y un worker idempotente transiciona a `NO_DISPONIBLE` al vencer. Una confirmación que llega **después** del deadline debe rechazarse, o resolverse de forma serializada con `ExpirarConfirmaciones` para que ambos no compitan sobre la misma fila — no se implementa ahora, queda registrado como restricción de diseño para cuando exista el worker (F5). **`PUBLICABLE` no es un estado, es un predicado:** `sesion.estado = PROGRAMADA` ∧ `asignacionFecha.estado = CONFIRMADA` ∧ dentro de ventana ∧ salón abierto ese día. Una asignación `PENDIENTE` nunca aparece en la disponibilidad del cliente. Qué ocurre si la asignación cambia materialmente tras confirmarse: **DECISIÓN ABIERTA** (§31).

## 13. Ventana de reservas

**Owner: Operación**, como política del salón (`anticipacionMaximaReserva`, `anticipacionMinimaReserva`), expuesta como VO `PoliticaReservaSalon`. **Consumidor: Reservas**, que la evalúa al listar disponibilidad y la **revalida** al ejecutar el comando. **Programación** la usa para una sola cosa: calcular el horizonte de materialización (§16). **No se mezcla con `Reserva`:** la entidad no guarda la ventana, y la política puede cambiar sin invalidar reservas ya hechas. Una sesión es publicable si existe, no está cancelada, su `AsignacionFecha` está `CONFIRMADA` (o el salón no lo exige), y `ahora ∈ [inicio − antMax, inicio − antMin]`.

## 14. Duración y slots

`TipoActividad.duracionMinutos` ya existe y es la base. Slot `k` = `[inicio + k·d, inicio + (k+1)·d)`. Asignación 10:00-12:00 con Reformer 60 → `10:00-11:00`, `11:00-12:00`; con 30 → cuatro slots. La duración se **snapshotea** en `Sesion`: cambiar el catálogo después no debe mover sesiones ya creadas.

**Rango no divisible (ej. 10:00-11:30 con duración 60): DECISIÓN PENDIENTE.** No se inventa. Opciones, sin elegir: (a) **rechazar al escribir la asignación** — única que no puede perder ni crear capacidad en silencio, y la que el diseño soporta como guard-rail por defecto si el negocio no decide otra cosa; (b) generar `floor(n)` slots y dejar el remanente sin usar; (c) permitir un último slot corto, lo que implica que la duración deja de ser una garantía para el cliente.

## 15. Sesion

**CONFIRMADO: entidad persistente**, nombre `Sesion`. Se descartan `Ocurrencia` (opaco para la UI) y `SesionReservable` (mezcla el sustantivo con un predicado que además es derivado). Evidencia, no autoridad: (1) **varias reservas concurrentes sobre la misma clase necesitan una fila común para bloquear** — sin ella la carrera por la última plaza no tiene sujeto, y esto por sí solo cierra la discusión; (2) `Reserva` necesita una referencia estable, porque hoy copia seis campos y no puede responder "¿estas dos reservas son de la misma clase?" sin compararlos todos; (3) cancelar una ocurrencia es un hecho con historia y no cabe en un valor calculado; (4) sustituir al instructor debe conservar el id para que las reservas existentes sigan colgadas; (5) Notificaciones y Outbox necesitan un `aggregateId` durable; (6) la capacidad debe enumerar sesiones traslapadas de un salón sin re-ejecutar el resolver por petición. Alternativa considerada y **refutada**: un id determinista derivado de `(fecha, salón, hora, asignación)` — se rompe al sustituir instructor, no admite estado y no se puede bloquear.

**Ownership: Programación** (`programacion.sesion`). La ocurrencia nace de una asignación efectiva y **existe con cero reservas**. Si perteneciera a Reservas, Reservas tendría que conocer recurrencia, ajustes, confirmación y horario efectivo, y `programacion → reservas` se volvería inevitable; Reservas sólo **ocupa** la sesión.

**Campos:** `id`, `asignacionFechaId` (linaje), `salonId`, `fecha`, `horaInicio`, `horaFin`, `instructorId`, `tipoActividadId`, `duracionMinutosSnapshot`, `estado`, `canceladaEn`, `motivoCancelacion`, `version`. **Idempotencia:** `UNIQUE (asignacion_fecha_id, hora_inicio)` — un reintento del job no duplica.

## 16. Materialización

Horizonte **por salón**, no global ni hardcodeado, en función de `A = anticipacionMaximaReservaHoras` y `P = plazoRespuestaConfirmacionHoras` (§12):

```
horizonteDias(salon) = ceil((A + P) / 24) + margenMaterializacionDias
```

Con reservas a 7 días (`A = 168h`), plazo de respuesta a 2 días (`P = 48h`) y margen 1 → `ceil(216/24) + 1 = 10` días. Un salón con ventana de 14 materializa más lejos: la fórmula sale de la política, no de una constante. La conversión horas/días es exactamente esta fórmula; la implementación futura debe respetar el redondeo hacia arriba (`ceil`) para no dejar fechas sin materializar dentro del horizonte real.

Dos etapas, dos workers, ambos idempotentes y por lotes acotados: **`MaterializarAsignacionesFecha`** resuelve cada fecha del horizonte y escribe `programacion_asignacion_fecha` con su deadline y estado inicial; **`MaterializarSesiones`** parte cada `AsignacionFecha` en slots por duración. Más **`ExpirarConfirmaciones`**, que transiciona vencidas a `NO_DISPONIBLE`. **Reconciliación:** cuando una regla o un ajuste cambia **dentro** del horizonte ya materializado, las fechas afectadas se re-resuelven — crear lo nuevo, cancelar lo obsoleto. Una sesión **con reservas** no se borra en silencio; qué hacer exactamente es decisión abierta (§31). El pasado nunca se regenera. No se implementa worker en esta fase.

## 17. Estados

| Entidad | Estados | Justificación |
|---|---|---|
| `AsignacionFecha` | `PENDIENTE_CONFIRMACION`, `CONFIRMADA`, `NO_DISPONIBLE` | disponibilidad del instructor |
| `Sesion` | `PROGRAMADA`, `CANCELADA` | existencia de la ocurrencia |
| `Reserva` | `CONFIRMADA`, `CANCELADA` (ya existen) | ocupación del cliente |

**Se rechaza `BORRADOR/PUBLICABLE/PUBLICADA` en `Sesion`:** la publicabilidad depende del reloj y del estado de la confirmación, así que guardarla duplicaría `AsignacionFecha.estado` y exigiría un worker que la voltee continuamente. Confirmación del instructor y estado de sesión quedan en entidades distintas, que es exactamente la distinción que el prompt §16 pide no perder.

## 18. Reservas grupales

`existeTraslape(instructor, …)` es **legado** y es hoy la única barrera contra la sobreventa. Su reemplazo no es una regla, son tres invariantes distintas:

| Qué protege hoy (mal) | Qué lo reemplaza |
|---|---|
| que el instructor no se duplique | el conflicto global sobre `AsignacionFecha` (§11), en programación, no en reserva |
| implícitamente, el sobrecupo | capacidad por recurso compartido (§19) |
| nada sobre el cliente | `UNIQUE (cliente_id, sesion_id) WHERE estado = 'CONFIRMADA'` |

Varias reservas apuntan a la misma `Sesion`; la primera **no** ocupa al instructor frente a las siguientes. **Restricción de secuencia:** `existeTraslape` no puede retirarse hasta que la capacidad por recursos esté en producción, porque hacerlo antes deja una ventana sin ninguna protección contra sobreventa; queda fijado en F7 (§26). Que un cliente reserve dos sesiones **distintas** traslapadas es una política aparte: decisión abierta (§31).

## 19. Recursos y capacidad

```
disponible(salon, tipoRecurso, [t1,t2)) =
      SalonRecurso.cantidad(salon, tipoRecurso)
    − Σ ActividadRecurso.cantidad(sesion.tipoActividad, tipoRecurso)
      sobre las reservas CONFIRMADAS de sesiones de ese salón
      cuyo [inicio,fin) intersecta [t1,t2)
```

El ejemplo del prompt sale directo: 6 reservas de Ariadna + 4 de Alberto, Reformer con `cantidad = 1`, consumen los 10 reformers del salón, y ninguna sesión traslapada tiene reformer disponible. La capacidad **no** es 10 por instructor. **Fronteras, sin ciclos:** Operación posee inventario (`SalonRecurso`) y requisito (`ActividadRecurso`); Programación define qué sesión existe y **no consulta reservas jamás**; Reservas determina qué está comprometido y posee `ConsumoRecursoReserva`. **Concurrencia:** el comando de reserva bloquea las filas `salon_recurso` implicadas en orden canónico de id (evita deadlocks), suma consumos vigentes traslapados y sólo entonces inserta; la capacidad mostrada en la API es una lectura momentánea y el comando revalida bajo lock.

## 20. Participantes

Dos ejes independientes: **`TipoActividad.participantesPorReserva`** = cuántas **personas** representa un booking (listas de asistencia, eventual límite humano por sesión); **`ActividadRecurso.cantidad`** = cuántas **unidades de equipo** consume (Reformer normal 1, Duo Reformer 2). Coinciden en Duo Reformer (2 personas, 2 unidades) y por eso se confunden, pero son independientes: una actividad en pareja sobre un solo Cadillac serían 2 participantes y 1 unidad. La capacidad se calcula **siempre** con `ActividadRecurso.cantidad`; `participantesPorReserva` no entra en esa fórmula. No se inventan reglas de Duo.

## 21. API móvil

`GET /api/disponibilidad?tipoActividadId={id}&fecha={ISO}[&salonId={id}]`. Se conserva la forma sugerida: el flujo real del cliente es actividad-primero, y `GET /salones/{id}/disponibilidad` obligaría a elegir salón antes de saber dónde hay cupo; `salonId` queda como filtro opcional.

```
salones[]: salonId, nombre, direccionCorta
  sesiones[]: sesionId, horaInicio, horaFin
              instructor { id, nombre }
              actividad  { id, nombre, duracionMinutos }
              lugaresDisponibles     # mínimo sobre los recursos requeridos
              reservable             # boolean
              motivoNoReservable?    # SIN_CUPO | FUERA_DE_VENTANA
```

El móvil **no** resuelve recurrencia, excepciones, confirmaciones, recursos ni slots, y el contrato **no** expone turnos recurrentes ni ajustes crudos. **Una asignación `PENDIENTE_CONFIRMACION`, o `NO_DISPONIBLE` por falta de confirmación, no aparece en absoluto en este contrato** — ni como sesión con `reservable = false` ni con un motivo público: no hay sesión que mostrar porque la asignación todavía no es fuente confiable de horario. `INSTRUCTOR_NO_CONFIRMADO` **no existe** como valor de `motivoNoReservable`; ese dato sólo pertenece a la API del instructor, a administración y a herramientas internas — nunca al contrato público. Una sesión que sí existe y estuvo `CONFIRMADA` puede, según política de lectura, mostrarse con `SIN_CUPO` o `FUERA_DE_VENTANA`: no se confunde disponibilidad comercial con confirmación pendiente. `reservable = false` con uno de esos dos motivos es preferible a omitir la sesión: el cliente ve que la clase existe y por qué no puede tomarla. Escritura: `POST /api/reservas` con `{ sesionId }` + idempotency key, y el `clienteId` sale del principal, nunca del body. **Dónde vive:** lee `Sesion` (Programación) y capacidad (Reservas/Operación), así que es un **coordinador de lectura por encima de los módulos**, no un endpoint dentro de `programacion`, que si no crearía `programacion → reservas`. No se diseñan todos los DTOs en esta fase.

## 22. Ownership de módulos

| Módulo | Owns |
|---|---|
| **OPERACION** | `Salon` (+ políticas), `HorarioOperacion`, `SalonHorarioExcepcion`, `TipoActividad`, `TipoRecurso`, `SalonRecurso`, `ActividadRecurso` |
| **PROGRAMACION** | `BloqueProgramacion`, `Asignacion`, `AjusteProgramacionFecha`, `AsignacionFecha` (incluye confirmación), `Sesion`, `ResolverProgramacionFecha` |
| **RESERVAS** | `Reserva`, `ConsumoRecursoReserva`, cancelación de reserva, cálculo de ocupación |

Tres precisiones sobre la propuesta inicial: (1) **`Sesion` pertenece a Programación** (§15), que es la pregunta que el prompt §22 pedía resolver explícitamente; (2) **`confirmacion_instructor` no existe como tabla** — es estado de `AsignacionFecha`, en Programación, mientras que las **políticas** de confirmación y ventana son de Operación por ser configuración del salón; (3) **la API de disponibilidad no pertenece a ningún módulo**: es un coordinador de lectura.

## 23. Dependency Rules

```
programacion → operacion    (horario efectivo, duración, oferta, políticas)
programacion → identidad    (autorización contextual por salón)
reservas     → programacion (leer Sesion, ocuparla)
reservas     → operacion    (inventario y requisito de recurso)
reservas     → identidad
coordinador de disponibilidad → programacion + reservas + operacion
```

Prohibido explícitamente: `programacion → ReservaRepository` en cualquier dirección de import — **decisión cerrada** (§30). Al cancelar una sesión, la transacción de Programación cambia `Sesion` y escribe en el mismo commit un evento `SesionCancelada` en Outbox: eso sí puede darse por sentado. **Lo que NO está cerrado es la consistencia final** entre `Sesion`, `Reserva`, `Beneficio` y `ConsumoRecurso` ante esa cancelación — el Outbox es sólo el mecanismo de entrega del evento, no equivale a decidir que todo el proceso downstream será eventual; esa política queda **DECISIÓN ABIERTA** (§31). También prohibido: `operacion → programacion` en cualquier forma; `reservas → PagoRepository`; e importar el repositorio o la entidad JPA de otro módulo, porque las referencias cruzadas son UUID + snapshot. No se introduce broker: los eventos son Outbox sobre PostgreSQL, y sólo donde el consumidor tolera eventualidad. Protección barata desde la primera fase: una regla ArchUnit `programacion` ↛ `reservas`, que cuesta un test y evita el ciclo que más fácil se cuela.

## 24. Modelo de persistencia

Nombres candidatos. No se escriben migraciones en esta fase.

**`programacion_bloque`** — owner Programación. PK `id UUID`; FK `salon_id`. Campos `dia_semana SMALLINT`, `hora_inicio TIME WITHOUT TIME ZONE`, `hora_fin TIME WITHOUT TIME ZONE`, `vigente_desde`, `vigente_hasta NULL`, `activo`, `serie_id UUID NOT NULL`, timestamps. Constraints `CHECK (dia_semana BETWEEN 0 AND 6)`, `CHECK (hora_inicio < hora_fin)`, `CHECK (vigente_hasta IS NULL OR vigente_hasta >= vigente_desde)`. **Sin EXCLUDE en 1C** (§26 F1): PostgreSQL no tiene un tipo `timerange` nativo, y un hard constraint sobre traslape de horas + vigencia exigiría `btree_gist` y un rango compuesto que no se justifica sin el modelo concreto que lo necesite. El no-traslape con vigencia intersectada se valida en aplicación: `existente.hora_inicio < nuevo.hora_fin AND nuevo.hora_inicio < existente.hora_fin`, combinado con intersección de `[vigente_desde, vigente_hasta]`. Índice `(salon_id, dia_semana, vigente_desde)`.

**`programacion_asignacion`** — owner Programación. PK `id UUID`; FK `bloque_id`, `instructor_id → usuario`, `tipo_actividad_id`. Campos `hora_inicio TIME WITHOUT TIME ZONE NOT NULL`, `hora_fin TIME WITHOUT TIME ZONE NOT NULL`, `vigente_desde`, `vigente_hasta NULL`, `activo`, `serie_id UUID NOT NULL`, timestamps. `CHECK (hora_inicio < hora_fin)`, `CHECK (vigente_hasta IS NULL OR vigente_hasta >= vigente_desde)`; contención en el bloque, contención de vigencia en la del bloque, y no traslape del mismo instructor, todo en aplicación. Índices `(bloque_id)`, `(instructor_id, vigente_desde)`. **Sin PK compuesta** — es el punto de todo el rediseño.

**`programacion_ajuste_fecha`** — owner Programación. PK `id UUID`; FK `salon_id`, `bloque_target_id NULL`, `asignacion_target_id NULL`, `instructor_id NULL`, `tipo_actividad_id NULL`. Campos `fecha`, `ambito`, `operacion`, `hora_inicio NULL`, `hora_fin NULL`, `motivo`, `creado_por`, `activo`, timestamps. CHECK de coherencia ambito↔target↔valores; `UNIQUE (fecha, asignacion_target_id, operacion) WHERE activo`. Índice `(salon_id, fecha)`.

**`programacion_asignacion_fecha`** — owner Programación, **F5** (§26), no se crea en 1C. PK `id UUID`; FK `salon_id`, `instructor_id`, `tipo_actividad_id`, `origen_asignacion_id NULL` (nulo cuando nace de un ajuste `AGREGAR`; distinto de `serie_id`, que es versionado de la regla — §7). Campos `fecha`, `hora_inicio`, `hora_fin`, `estado`, `deadline_confirmacion NULL`, `confirmado_en NULL`, `confirmado_por NULL`, `version`, timestamps. `CHECK (hora_fin > hora_inicio)` y, como hard backstop de F5, **`EXCLUDE USING gist (instructor_id =, tsrange(fecha + hora_inicio, fecha + hora_fin, '[)') &&) WHERE estado <> 'NO_DISPONIBLE'`** (requiere `btree_gist`; PostgreSQL no tiene `timerange`, el rango candidato compone fecha y hora en `tsrange`). Índices `(salon_id, fecha)`, `(estado, deadline_confirmacion)` para el worker.

**`sesion`** — owner Programación. PK `id UUID`; FK `asignacion_fecha_id`, `salon_id`, `instructor_id`, `tipo_actividad_id`. Campos `fecha`, `hora_inicio`, `hora_fin`, `duracion_minutos_snapshot`, `estado`, `cancelada_en NULL`, `motivo_cancelacion NULL`, `version`, timestamps. `UNIQUE (asignacion_fecha_id, hora_inicio)`, `CHECK (hora_fin > hora_inicio)`. Índices `(salon_id, fecha, hora_inicio)`, `(tipo_actividad_id, fecha)` para disponibilidad.

**Evolutivas:** `reserva` (Reservas) suma `sesion_id UUID NULL → sesion(id)` + índice, conservando los campos actuales como snapshot; `reserva_consumo_recurso` (Reservas, fase posterior) con PK `(reserva_id, tipo_recurso_id)` y `cantidad SMALLINT`; `salon` (Operación) suma las columnas de política de §5; `horario_operacion` (Operación) suma vigencia y su `UNIQUE (salon_id, dia_semana)` cede; `salon_horario_excepcion` (Operación) se conserva sin cambios.

**Tablas que se decidió NO crear:** `programacion_recurrencia` (es vigencia + `diaSemana`), `confirmacion_instructor` (es estado de `AsignacionFecha`), `programacion_efectiva` (se calcula), `sesion_publicacion` (es un predicado).

## 25. Current → Target

| Actual | Target | Acción | Compatibilidad |
|---|---|---|---|
| `TurnoInstructor` RECURRENTE | `programacion_bloque` (+ vigencia) | **COEXISTIR** → REEMPLAZAR EVENTUALMENTE | tabla nueva vacía; backfill F4, switch F5, retiro F9 |
| `TurnoInstructor` EXCEPCION | `programacion_ajuste_fecha` (REEMPLAZAR) | **COEXISTIR** → REEMPLAZAR | el mapeo pierde el "sustituye todo el día", que es el bug; backfill asistido |
| `TurnoInstructor` CANCELACION | `programacion_ajuste_fecha` (CANCELAR) | **COEXISTIR** → REEMPLAZAR | hoy es 00:00-23:59 día completo; el target opera sobre el rango real |
| `turno_instructor_usuario` | ownership pasa a la asignación | **REEMPLAZAR EVENTUALMENTE** | sin equivalente; se resuelve al backfillear asignaciones |
| `TurnoInstructorAsignacion` | `programacion_asignacion` con PK propia | **REEMPLAZAR EVENTUALMENTE** | la PK compuesta es incompatible con el requisito; no hay evolución aditiva |
| `HorarioOperacion` | regla semanal con vigencia | **EVOLUCIONAR** | expand aditivo con `DEFAULT`, luego relevo del UNIQUE |
| `SalonHorarioExcepcion` | excepción operativa por fecha | **CONSERVAR** | sin cambio de esquema; cambia quién la consulta |
| `Salon` | + políticas de confirmación y ventana | **EVOLUCIONAR** | columnas `NOT NULL DEFAULT`; aditivo puro |
| `Reserva` | reserva sobre `sesionId` | **EVOLUCIONAR** (expand/contract) | `sesion_id` nullable; los campos actuales **no se eliminan**, pasan a snapshot |
| `existeTraslape` | capacidad + unique cliente/sesión | **DEPRECAR** | sólo tras F7; antes deja la sobreventa sin barrera |
| `TipoActividad` | catálogo operativo | **CONSERVAR** | `duracionMinutos` y `participantesPorReserva` ya sirven |
| `ActividadRecurso` | requisito por reserva | **CONSERVAR** | `cantidad` ya significa unidades totales |
| `SalonRecurso` | inventario bloqueable | **EVOLUCIONAR** | el modelo basta; falta el uso bajo lock |
| `TipoRecurso` | catálogo de recursos | **CONSERVAR** | — |

Nada se elimina de inmediato.

## 26. Migración incremental

- **F1 — Modelo base aditivo (= 1C).** `programacion_bloque` + `programacion_asignacion`: tablas, entidades, repositorios y servicio de escritura con invariantes, **incluido el precheck de conflicto recurrente global de instructor (Nivel 1, §11)**. Nadie lee estas tablas; `calendario` intacto.
- **F2 — Operación.** Vigencia en `horario_operacion` + columnas de política en `salon`. Va después de F1 porque F1 no depende de ella: usa el horario actual, que la vigencia sólo ensancha.
- **F3 — Ajustes y resolver.** `programacion_ajuste_fecha` + `ResolverProgramacionFecha` puro, probado contra fixtures. Sin lectores en producción.
- **F4 — Backfill y comparación.** Migrar `turno_instructor` → bloques/asignaciones/ajustes y un arnés de **dual-read**: resolver la misma fecha por ambos caminos y comparar. Sin switch. Es la fase que da confianza de que el modelo nuevo representa lo que el viejo hacía.
- **F5 — `AsignacionFecha` + confirmación.** Materialización de asignaciones efectivas, deadline, worker de expiración, `btree_gist` y el EXCLUDE de conflicto global (Nivel 3, backstop duro sobre ocurrencias concretas — §11).
- **F6 — `Sesion`.** Materialización por duración, horizonte, reconciliación, cancelación de sesión.
- **F7 — Reservas sobre sesión + capacidad.** `reserva.sesion_id`, comando por `sesionId`, `ConsumoRecursoReserva`, locks de inventario, unique cliente/sesión. **Sólo aquí** se retira `existeTraslape`.
- **F8 — API de disponibilidad + móvil.** Endpoint nuevo, migración de la app; el viejo sigue vivo.
- **F9 — Contract.** Deprecar y retirar endpoints y tablas de `turno_instructor*`.

Dependencias duras: F3 necesita F1; F5 necesita F2 y F3; F6 necesita F5; F7 necesita F6; F9 necesita F8.

## 27. Compatibilidad API

**Siguen usando `TurnoInstructor` sin cambios hasta F8/F9:** `GET/POST/PATCH/DELETE /api/turnos-instructor`, `/api/turnos-instructor/puntuales`, y los cuatro endpoints de `/api/reservas`. **Debe migrar primero** `POST /api/reservas`, único que necesita `sesionId` para habilitar reservas grupales — pero sólo puede hacerlo tras F6: es consecuencia, no prioridad.

**Endpoints nuevos, en rutas nuevas** (nunca mutando la forma de una respuesta que el móvil ya consume): `/api/programacion/bloques`, `/api/programacion/asignaciones`, `/api/programacion/ajustes`, `/api/programacion/confirmaciones`, `/api/disponibilidad`, y `POST /api/reservas` v2 con `sesionId` + idempotency key. **Deprecated en F8 y retirados en F9:** los `POST/PATCH/DELETE` de `/api/turnos-instructor`, cuando el admin web opere sobre bloques y asignaciones. Los endpoints nuevos reutilizan `calendario.gestionar/editar/cancelar/leer` y `AutorizadorSalon`; no se inventan permisos hasta que haya una operación que hoy no exista — la confirmación del instructor sí necesitará uno propio, porque el actor es el instructor y no un administrador.

## 28. Compatibilidad de datos

Ambiente pre-release con BD local desechable, pero la forma debe ser correcta para producción. **No se editan las migraciones V1–V40** (Flyway es forward-only) y va **un concern por migración**, en orden `expand → backfill → switch → contract`: *expand* crea tabla o columna nullable/con `DEFAULT` y nunca rompe al código viejo; *backfill* puebla y debe ser **reanudable, idempotente y medible** — para pre-release puede ir en una migración versionada, pero la forma correcta para producción es un job por lotes y así debe escribirse; *switch* cambia la lectura en un despliegue separado del backfill; *contract* retira sólo tras verificar que nadie lee. Las tablas nuevas nacen **vacías**, siguiendo el precedente de `V36` con `actividad_recurso`. `CREATE EXTENSION IF NOT EXISTS btree_gist` va en su propia migración, antes de cualquier EXCLUDE. Testcontainers ya valida las 43 migraciones en cada corrida, así que cualquier expand nuevo queda cubierto desde el primer commit.

## 29. Safety net

**Deben seguir pasando intactos durante F1–F6: los 70.** Nada en esas fases toca lo que observan. Si uno se pone rojo en F1–F4, la intervención se revierte: es la señal de que se filtró un cambio no aditivo.

**Caracterizan legado y se retiran conscientemente, con fecha.** En **F7** cae `caracterizaLimitacionActualInstructorExclusivoPorReserva`, porque la capacidad reemplaza la exclusividad. En **F9** caen los que dependen del modelo legado de turnos: `…CancelacionDeUnRangoPuntualAnulaTodoElDia` y `…CancelacionDeFechaTienePrecedenciaSobreExcepcionYRecurrente` (el ajuste opera sobre el rango real y desaparece la precedencia global); `…ExcepcionTienePrecedenciaSobreElRecurrente` y `…ExcepcionSustituyeTodosLosRecurrentesDelInstructorEseDia` (los ajustes son composables); `…ExcepcionQuePretendeSustituirElRecurrenteEsRechazadaPorTraslape` (el bug deja de existir por construcción); y `permiteCancelacionFueraDelHorarioOperativo…` junto con `permiteCancelacionQueSeTraslapaConUnRecurrente…` (CANCELACION deja de ser marcador de día completo).

**Se re-expresan contra el modelo nuevo** (la regla sobrevive, cambia el sujeto): los 5 de traslape `[inicio,fin)`, los 4 de contención y bordes exactos, los 3 de horario especial y salón cerrado, los 3 de múltiples instructores, los 2 de especialidad, los 2 de `domingo = 0`, los 2 de filtro por día, los 2 de autoexclusión al actualizar, los 2 de intervalo inválido y el de rango idéntico. Los 3 de matriz de permisos se conservan y se replican en los endpoints nuevos.

**Tests nuevos por concepto.** *Bloque*: contención, bordes, traslape con vigencias intersectadas vs disjuntas, cierre de versión. *Asignación*: **dos segmentos disjuntos con la misma actividad** (hoy irrepresentable), una actividad por asignación, contención en el bloque, mismo instructor duplicado en el mismo rango, actividad no ofrecida por el salón. *Recurrencia*: vigencia abierta, versionado sin reescribir el pasado, regla que ya no aplica a una fecha. *Ajustes*: las 9 operaciones, composición de dos ajustes, rechazo de contradictorios, cascada de `BLOQUE/CANCELAR`. *Resolver*: cada paso y su orden, salón cerrado, regla válida en el pasado que hoy cae fuera de horario. *Conflicto global*: nivel plantilla, nivel ajuste y el EXCLUDE bajo dos transacciones concurrentes (Testcontainers). *Confirmación*: nace confirmada si la política no la exige, deadline, expiración idempotente, pendiente invisible en disponibilidad. *Sesión*: partición por duración 60 y 30, idempotencia ante reintento, identidad estable. *Reservas grupales*: dos clientes en la misma sesión, mismo cliente dos veces, última plaza concurrente. *Recursos*: el escenario 6+4 reformers de §19, Duo consumiendo 2, sesiones traslapadas. No se elimina ningún test en esta fase.

## 30. Decisiones cerradas

1. Package futuro `programacion`; `calendario` queda como lenguaje de UI.
2. **Exactamente una actividad por asignación**, con id propio. La PK compuesta se reemplaza; no admite evolución aditiva.
3. Varios instructores coexisten en el mismo salón y horario, con la misma o distinta actividad. Sin exclusividad `salón+hora` ni `actividad+salón+hora`.
4. Intervalos `[inicio, fin)` en todo el modelo.
5. Programación semanal con `vigenteDesde`/`vigenteHasta`; la recurrencia **no es una tabla**.
6. Ajustes por fecha en una entidad con `(ambito, operacion, target, valores)`; cierre y horario especial permanecen en `SalonHorarioExcepcion`.
7. La confirmación es sobre la **asignación efectiva de una fecha** y es el estado de `AsignacionFecha`; no hay tabla de confirmación.
8. `Sesion` es entidad persistente y pertenece a **Programación**.
9. Reservas grupales: varias reservas por sesión; la primera no ocupa al instructor.
10. Recursos compartidos por `salon + tipoRecurso + intervalo`, nunca por instructor.
11. La programación efectiva se calcula; la única materialización parcial es `AsignacionFecha`, y aguas abajo `Sesion`.
12. `programacion` nunca depende de `reservas`.
13. Bloques y asignaciones **coexisten** con `turno_instructor`; sin mutación in situ ni Big Bang.

## 31. Decisiones abiertas

Sólo las que realmente faltan. Ninguna bloquea F1–F4.

1. **Rango no divisible por la duración** (§14) — bloquea F6.
2. **Sesión con reservas que cambia de instructor**: ¿se conserva el id y se notifica, o se cancela y se recrea? — bloquea F6.
3. **El instructor revoca disponibilidad con reservas hechas**: ¿se permite? ¿quién asume la cancelación? — bloquea F6.
4. **Política de cancelación de sesión**: consistencia final entre `Sesion`, `Reserva`, `Beneficio` y `ConsumoRecurso` ante una cancelación — el Outbox (`SesionCancelada`) es sólo el mecanismo de entrega, no resuelve por sí mismo si el proceso downstream es eventual o transaccional — bloquea F6/F7.
5. **Confirmación invalidada por cambio material**: si tras confirmar cambia el rango o la actividad, ¿se revoca? — bloquea F5.
6. **Cliente con dos reservas traslapadas en sesiones distintas**: ¿prohibido, o legítimo con `participantesPorReserva > 1`? — bloquea F7.
7. **Lista de espera** y **sobrecupo**: no se modelan hasta que existan.
8. **Recurso fuera de servicio**: ¿inventario con vigencia, o descuento temporal? — bloquea F7 parcialmente.
9. **Duo**: ¿una reserva con 2 participantes, o dos reservas ligadas? Afecta el unique cliente/sesión — bloquea F7.
10. **Límite humano por sesión** cuando la actividad no consume recurso limitante: hoy la capacidad sería infinita.
11. **Cambio de `duracionMinutos`** con sesiones ya materializadas.
12. **Representación del bloque especial + asignación especial** (§9): ya existe un candidato de diseño para F3 (`bloqueAjusteTargetId`, self-FK entre ajustes `BLOQUE/AGREGAR` y `ASIGNACION/AGREGAR`), pendiente de validar al implementar F3 — no bloquea F1–F4.

## 32. Primera implementación 1C

**Crear el package `programacion` con el modelo base de bloque y asignación, sin conectarlo a nada.** Es la primera intervención correcta porque es la única que demuestra lo que el modelo actual no puede representar — dos segmentos disjuntos con la misma actividad — sin tocar una sola fila, columna o endpoint existente.

**Crear:**

- `src/main/resources/db/migration/V41__programacion_bloque_asignacion.sql` — expand puro: `CREATE TABLE programacion_bloque` y `CREATE TABLE programacion_asignacion`, vacías, con los CHECK e índices de §24. **V41 explícitamente NO instala `btree_gist`, NO crea `EXCLUDE`, NO crea tipos range personalizados (`timerange` no existe en PostgreSQL), NO crea columnas range generadas, y NO hace backfill.** Los hard constraints de intervalos quedan para la fase que tenga el modelo concreto que los necesita — el EXCLUDE sobre `AsignacionFecha` es F5 (§11, §26). Sin `ALTER` sobre ninguna tabla existente.
- `com.feelingpilates.programacion.bloque.BloqueProgramacion` (entidad, extiende `EntidadBase`, con `serieId UUID NOT NULL`) y `com.feelingpilates.programacion.bloque.Asignacion` (entidad con `id UUID` propio, `serieId UUID NOT NULL`, `bloqueId`, `instructorId`, un `tipoActividadId`, rango obligatorio y vigencia).
- `BloqueProgramacionRepository`, `AsignacionRepository`.
- `BloqueProgramacionService` con las invariantes de §6 y §7: `[inicio,fin)`, contención en el horario operativo (lee `HorarioOperacionRepository`; dirección permitida `programacion → operacion`), no traslape con vigencia intersectada, una actividad por asignación, contención de la asignación en el bloque, contención de vigencia de la asignación en la del bloque, `TipoActividad` existe y está activa, el salón ofrece esa actividad (`salon_tipo_actividad`), el instructor existe y está habilitado (según el modelo real de `usuario`/rol), especialidad del instructor (`instructor_actividad`), y el **conflicto recurrente global de instructor cross-salón (Nivel 1, §11)**: mismo instructor, mismo `diaSemana`, vigencias intersectadas y horas intersectadas, sin importar el salón. Esto **sí** entra en 1C porque es decidible sobre la plantilla, sin expandir fechas. **No se valida todavía:** capacidad, recursos, sesiones, confirmación, reservas — eso llega en fases posteriores (§26). Lo que **no** entra en 1C es el backstop duro de PostgreSQL (Nivel 3, EXCLUDE sobre `AsignacionFecha`): eso es F5 y necesita ocurrencias materializadas concretas.
- `src/test/java/com/feelingpilates/programacion/BloqueProgramacionServiceTest.java` (o el conjunto equivalente de tests unitarios), mismo patrón que los tests de caracterización existentes. **Mínimo exigido para 1C:** bloques contiguos permitidos (`08:00-12:00` + `12:00-14:00`); traslape positivo rechazado; rango inválido rechazado (`hora_fin <= hora_inicio`); vigencias disjuntas permiten el mismo rango horario; vigencias intersectadas rechazan el traslape; dos segmentos disjuntos con **mismo instructor y misma actividad** en el mismo bloque (hoy irrepresentable, §7); exactamente una actividad por asignación; contención temporal de la asignación en el bloque; contención de vigencia de la asignación en la del bloque; actividad activa; actividad ofrecida por el salón; especialidad del instructor; conflicto recurrente cross-salón (Nivel 1, §11); el mismo conflicto pero con vigencias disjuntas permitido; dos versiones de la misma regla compartiendo `serieId` con `id` distinto; migración Flyway `V1→V41` PASS (Testcontainers); validación de esquema JPA (`hibernate.hbm2ddl.auto=validate` o equivalente) PASS; y **los 70 tests existentes continúan verdes sin modificar una sola línea de esos tests.** Opcional y barato: una regla ArchUnit que prohíba `programacion → reservas`/`calendario`.

**Qué NO debe tocar:** nada bajo `com.feelingpilates.calendario` (entidades, servicios, controladores, DTOs); las tablas `turno_instructor`, `turno_instructor_usuario`, `turno_instructor_asignacion` y `reserva`, ni en esquema ni en datos; `HorarioOperacion` y `Salon`, que son **sólo lectura** porque su vigencia y sus políticas son F2; las migraciones V1–V40; los 70 tests existentes, que deben seguir 70/70 verdes sin editar una línea; cualquier controlador o ruta HTTP, porque nada se expone; y la app móvil, que no se entera de esta fase.

**Por qué cumple los criterios:** pequeña (dos tablas, ~5 clases, una migración); aditiva (ningún `ALTER`, ningún endpoint); reversible (`DROP TABLE` de dos tablas vacías y borrar un package); protegida por tests (los 70 como regresión, los nuevos como especificación); sin Big Bang (el modelo viejo sigue sirviendo el 100% del tráfico); sin cambio en el móvil.

## 33. Diagrama objetivo

```
┌─ OPERACION ───────────────────────────────────────────────────────┐
│ Salon (+politicas: confirmacion, ventanas, margen)                │
│ HorarioOperacion (+vigencia) ─┐                                   │
│ SalonHorarioExcepcion ────────┴─> HorarioEfectivo(fecha)    [VO]  │
│ TipoActividad (duracion, participantes)                           │
│ TipoRecurso / SalonRecurso / ActividadRecurso        [inventario] │
└──────────┬──────────────────────────────────┬─────────────────────┘
           │ horario/actividad/politica       │ inventario
           v                                  │
┌─ PROGRAMACION ──────────────────────────────┼─────────────────────┐
│ BloqueProgramacion  [entidad+vigencia]  salon, diaSemana, [ini,fin)
│   id (esta version) / serieId (regla logica estable)                │
│         v                                   │                     │
│ Asignacion          [entidad+vigencia]  instructor + rango + 1 act │
│   id (esta version) / serieId (regla logica estable)                │
│   conflicto recurrente global Nivel 1 (cross-salon) -- YA EN 1C     │
│         v                                   │                     │
│ AjusteProgramacionFecha  [entidad]  ambito/operacion/target/valores│
│   bloque especial: bloqueAjusteTargetId (candidato F3, no en 1C)    │
│         v                                   │                     │
│ ResolverProgramacionFecha [servicio] -> ProgramacionEfectiva  [VO] │
│         v          (materializacion parcial, dentro del horizonte) │
│ AsignacionFecha     [ENTIDAD]  -- F5, no existe en 1C               │
│   PENDIENTE_CONFIRMACION -> CONFIRMADA | NO_DISPONIBLE             │
│   ventana publica abre: nunca PENDIENTE (S-A ya resuelto)           │
│   EXCLUDE Nivel 3 (F5, backstop): instructor + tsrange(fecha+hora)  │
│         v          (particion por duracionMinutos)                 │
│ Sesion              [ENTIDAD]  PROGRAMADA | CANCELADA · id estable │
└──────────┬──────────────────────────────────┼─────────────────────┘
           │ lee Sesion (API)                 │
           │  X NUNCA programacion -> reservas│
           v                                  v
┌─ RESERVAS ────────────────────────────────────────────────────────┐
│ Reserva -> sesionId (+snapshot comercial)   CONFIRMADA | CANCELADA │
│   UNIQUE (cliente, sesion) WHERE CONFIRMADA                        │
│ ConsumoRecursoReserva                                              │
│ capacidad(salon, recurso, [t1,t2)) =                               │
│   SalonRecurso.cantidad - SUM ActividadRecurso.cantidad            │
│   de reservas activas de sesiones traslapadas                      │
│ lock de salon_recurso en orden canonico de id                      │
└──────────┬─────────────────────────────────────────────────────────┘
           │ Sesion (programacion) + capacidad (reservas) + salon (operacion)
           v
  COORDINADOR DE LECTURA — GET /api/disponibilidad?tipoActividadId=&fecha=
           v
  App movil  (no resuelve nada)

Efectos desacoplados:  Programacion --SesionCancelada--> Outbox --> efecto a definir
  (consistencia Sesion/Reserva/Beneficio/ConsumoRecurso: DECISION ABIERTA §31)
```

## Resultado

Diseño 1B.1: **CONSOLIDADO / BLOQUEADO**. Correcciones de la revisión independiente incorporadas: PostgreSQL sin `timerange`, V41 sin EXCLUDE/`btree_gist`/tipos range, versionado por `serieId`, CHECK de vigencia en `programacion_asignacion`, confirmación con semántica única (`plazoRespuestaConfirmacionHoras`), API pública sin `INSTRUCTOR_NO_CONFIRMADO`, gap de bloque especial + asignación especial registrado para F3, conflicto recurrente global de instructor (Nivel 1) confirmado en 1C, y cancelación de sesión presentada como decisión parcialmente abierta.

V41: **LISTA PARA IMPLEMENTACIÓN** — modelo base aditivo, sin backfill, sin `ALTER`, sin extensiones PostgreSQL.

Productivo: SIN CAMBIOS. Flyway: SIN CAMBIOS. Tests: 70/70, sin tocar.

Working tree: sólo `auditoria/fase-1b-diseno-programacion.md`.
