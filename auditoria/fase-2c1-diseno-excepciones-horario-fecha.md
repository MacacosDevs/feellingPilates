# F2C.1 — Diseño excepciones de horario por fecha

Fase **exclusivamente de análisis y diseño**. No se creó código productivo, migraciones,
endpoints ni tests. Único artefacto: este documento.

Revisión vigente: **F2C.1.1**, tras review adversarial. Ver
"[F2C.1.1 — Correcciones post-review](#f2c11--correcciones-post-review)". Las secciones
principales están reescritas para reflejar esas decisiones: **el documento tiene una sola
especificación, no dos**.

---

## Estado base

- Branch: `operacion/horario-versionado-api`
- HEAD base: `78ba509e62cb8471bee3abce9089ef709e6f89ea` (`test: proteger extractor unico de codigos`)
- Working tree base: **limpio** salvo este checkpoint
- Suite actual: **398/398 PASS** (`./mvnw test`, exit 0; 0 failures, 0 errors, 0 skipped)
- Flyway actual: **V46** es la versión numérica más alta
  (`V46__horario_operacion_drop_unique_dia.sql`); 49 migraciones validadas en total,
  incluidas las intercaladas `V22.1` y `V22.2`. Primer número libre: **V47**.

---

## F2C.1.1 — Correcciones post-review

El review adversarial concluyó **REQUIERE AJUSTE**, con **cero P0** y seis P1. Todos quedan
cerrados aquí; ninguna decisión queda abierta.

### P1 corregidos

| # | Hallazgo | Corrección | Sección |
|---|---|---|---|
| 1 | `DELETE /{id}` y `DELETE /{fecha}` no pueden coexistir: Spring no distingue dos plantillas de un solo segmento | La API nueva se cuelga de un prefijo literal: `.../excepciones-horario/por-fecha/{fecha}`. El `DELETE /{id}` legacy se conserva sin tocar | [API](#api) |
| 2 | `TurnoInstructor.EXCEPCION` no participa hoy del `SalonLock` | F2C.2 le añade `SalonLock` en `TurnoInstructorService.crear`, antes de resolver el horario efectivo | [Protocolo de lock](#protocolo-de-lock) |
| 3 | El port neutral de impacto puntual no estaba cerrado | Se cierra **una** forma: `ValidadorImpactoExcepcionHorario.evaluar(CambioExcepcionHorario) → List<ConflictoProgramacionPuntual>`. No queda "boolean vs lista" | [Port neutral](#port-neutral-de-impacto-puntual) |
| 4 | `Reserva.CONFIRMADA` ya existe y fue omitida del alcance | Las reservas confirmadas son objeto puntual de primera clase: las valida el adapter, y `ReservaService.crear` pasa a tomar lock y validar `HorarioEfectivoSalon` | [Programación puntual](#programación-puntual-real), [ReservaService](#reservaservicecrear) |
| 5 | La traducción de `23505` necesita `saveAndFlush` **dentro** del traductor | Se especifica `saveAndFlush` dentro del `try`; con `save` la violación saltaría en el commit del proxy, fuera de cualquier `catch` | [Traducción de 23505](#traducción-de-23505) |
| 6 | Debe preservarse firmemente la API legacy que consume el frontend | Los tres endpoints actuales se conservan con path y DTO intactos; el retiro es una fase posterior, nunca F2C.2 | [API](#api), [Retiro de legacy](#retiro-de-legacy) |

### Afirmaciones del checkpoint anterior que se eliminan o corrigen

| Afirmación anterior | Estado real | Corrección |
|---|---|---|
| "una violación del índice único escapa sin traducir y produce **500**" | `GlobalExceptionHandler:68` ya mapea `DataIntegrityViolationException` a **409 genérico** | El defecto no es el status: es que no hay `codigo` estable ni atribución por constraint |
| "el writer actual ya respeta 403 antes de 404 en `DELETE`" | `SalonHorarioExcepcionService.eliminar` hace `findById` **antes** de `verificarAccesoSalon` | Es un defecto real de orden; F2C.2 lo corrige |
| "el bloque recurrente se materializa **recortado** a la intersección" | — | Se elimina. La ocurrencia recurrente se **omite**, nunca se recorta |
| "la respuesta informa cuántos bloques/turnos recurrentes quedan fuera" | — | Se elimina del alcance: no hay contrato para ello y no hace falta para correctness |
| "`SalonLock` en el writer de excepción deja mutuamente excluidos a los writers puntuales" | Turno `EXCEPCION` y `ReservaService` no toman el lock | Un lock de un solo lado no serializa nada. Ambos lados deben tomarlo |
| "soft-delete preserva auditoría" (aplicado a todo) | Un update sobre la fila activa la sobrescribe | Sólo la cancelación deja rastro. **No hay historial versionado de ediciones activas** |
| Plan de 35 tests con capa API especulativa | — | Plan reducido y reorientado por comportamiento |

### P2 corregidos

- El DDL citado se alinea literalmente con `V18` (formato del `CHECK` incluido).
- Se elimina toda insinuación de UNIQUE sobre filas inactivas: el índice es **parcial**,
  `WHERE activo`, y N filas inactivas por `(salon_id, fecha)` son legales por diseño.
- Se cierra explícitamente que **F2C no modela cambios intra-día**.
- Se documenta por qué `SalonLock` en reservas es una decisión de consistencia del modelo legacy
  y no el diseño final de concurrencia de reservas.

---

## Hallazgo que condiciona toda la fase

**La funcionalidad ya existe parcialmente en el repositorio y F2C.1 no es un diseño en verde.**

El brief plantea "necesitamos representar excepciones por fecha" como si el concepto no
existiera. No es así: existe desde `V18__salon_horario_excepcion.sql`, es decir **anterior a
todo F2A/F2B**. Está implementada de punta a punta y el frontend la consume en producción.

| Pieza | Ruta | Estado |
|---|---|---|
| Entidad | `ubicaciones/entidad/SalonHorarioExcepcion.java` | Existe |
| Tabla + constraints | `db/migration/V18__salon_horario_excepcion.sql` | Existe |
| Repositorio | `ubicaciones/repositorio/SalonHorarioExcepcionRepository.java` | Existe |
| Writer | `ubicaciones/servicio/SalonHorarioExcepcionService.java` | Existe, **pre-F2B** |
| Controller | `ubicaciones/controlador/SalonHorarioExcepcionController.java` | Existe |
| DTOs | `GuardarExcepcionSalonRequest`, `SalonHorarioExcepcionResponse` | Existen |
| Resolver compuesto | `ubicaciones/servicio/HorarioEfectivoSalon.java` | Existe, **F2B, correcto** |
| Value object | `ubicaciones/dominio/HorarioEfectivo.java` | Existe, **F2B, correcto** |
| Consumo web | `web/src/api/salones.ts`, `web/src/pages/salones/SalonHorarios.tsx` | Existe |

Consecuencia sobre el alcance real de F2C:

- **El lado de LECTURA ya está resuelto y es correcto.** `HorarioEfectivoSalon` ya implementa
  exactamente la precedencia que pide §2 del brief, y `HorarioEfectivoSalonTest` ya cubre los
  cuatro casos A/B/C/D del enunciado. No hay nada que rediseñar ahí.
- **El lado de ESCRITURA es el trabajo real.** `SalonHorarioExcepcionService` se escribió antes
  de que existieran `SalonLock`, el `Clock` central y la Política A. No participa de ninguno de
  los tres protocolos.
- **El alcance no se agota en ese writer.** Los writers que crean objetos puntuales sobre una
  fecha —`TurnoInstructorService.crear(EXCEPCION)` y `ReservaService.crear`— son la otra mitad de
  la carrera y hoy no participan del protocolo. Cerrar sólo un lado no cierra nada.

Por tanto: **F2C no introduce un modelo nuevo; alinea con el protocolo F2B a los tres writers que
comparten la invariante.**

### Conflictos detectados contra lo que el brief pide evitar

| Anti-práctica | ¿Presente? | Evidencia |
|---|---|---|
| Siete booleanos | No | Modelo por fecha, no por día |
| Flags dentro de `Salon` | No | Tabla propia |
| Columna "fecha especial" dentro de `HorarioOperacion` | No | Tablas separadas |
| Modificar la versión semanal | No | El writer nunca toca `horario_operacion` |
| Nueva vigencia semanal de un solo día | No | No se crea `HorarioOperacion` |
| Sentinel hours | No | `hora_apertura`/`hora_cierre` son `NULL` si cerrado |
| `apertura == cierre` para cerrado | No | `CHECK` lo prohíbe: exige `hora_cierre > hora_apertura` |

**Ninguna anti-práctica está presente.** El modelo de datos heredado es sano; lo desalineado es
el comportamiento de los writers.

### Huecos reales, verificados sobre el código

1. **`SalonHorarioExcepcionService` no adquiere `SalonLock`.** Es un writer con capacidad de
   volver incompatible el horario de un salón, fuera del protocolo de serialización de F2B.
2. **No usa el `Clock` central.** No existe noción de pasado/hoy/futuro: hoy se puede reescribir
   la historia operativa de una fecha ya transcurrida.
3. **No evalúa impacto sobre programación puntual.** Hueco ya documentado en el Javadoc de
   `ImpactoTurnosRecurrentesEnHorario`: *"`SalonHorarioExcepcionService.guardar` puede cerrar un
   día sin mirar los turnos"*.
4. **No traduce la violación del índice único.** Una violación de `idx_salon_horario_excepcion_unica`
   cae en `GlobalExceptionHandler:68` y sale como **409 genérico y sin `codigo`**
   (`"Conflicto de datos: el recurso ya existe o viola una restricción"`). El defecto **no es un
   500**: es la ausencia de código estable y de atribución por constraint.
5. **Sin ningún test propio.** No existe `SalonHorarioExcepcionServiceTest`.
6. **`eliminar` lee antes de autorizar.** `findById(id)` y la comparación de salón ocurren **antes**
   de `autorizadorSalon.verificarAccesoSalon`. Un actor sin scope sobre el salón puede distinguir
   por el status si un UUID existe en otro salón. Es un defecto real, no una preferencia REST.
7. **`TurnoInstructorService.crear` excluye deliberadamente a `EXCEPCION` del lock.** El comentario
   en el código lo dice y da su razón (la invariante no la mantenía nadie). F2C.2 elimina la causa,
   así que la exclusión deja de estar justificada.
8. **`ReservaService.crear` no toma lock ni consulta `HorarioEfectivoSalon`.** Valida contra turnos
   del instructor y traslapes, nunca contra el horario operativo del salón.

---

## Inventario

### Núcleo temporal (F2B, intacto)

- `HorarioOperacion` — `salon`, `diaSemana` (`0=domingo..6=sábado`), horas, `vigenteDesde`,
  `vigenteHasta`. `NULL` = infinito natural en ambos bordes.
- `HorarioOperacionRepository` — `findVigente`, `findVersionesQueIntersectan`,
  `bloquearVersionesQueIntersectan` (`FOR UPDATE` literal), `findVersionesOrdenadas`.
- `HorarioOperacionResolver` — resuelve **sólo** la plantilla semanal.
- `HorarioEfectivoSalon` — **único resolver público compuesto**; excepción sobre semanal.
- `HorarioEfectivo` — `record(Estado, Origen, horaApertura, horaCierre)`;
  `Estado ∈ {ABIERTO, CERRADO, NO_OPERATIVO}`, `Origen ∈ {EXCEPCION, SEMANAL, NINGUNO}`.
  `contiene(inicio, fin)` es **contención completa**, no solape.
- `SalonLock` — `PESSIMISTIC_WRITE` sobre `Salon`, `Propagation.MANDATORY`.
- `VersionarHorarioOperacion` / `CerrarHorarioOperacion` — writers semanales; los dos con lock,
  `Clock` y Política A.
- `HorarioOperacionErrores` — códigos estables + whitelist 409 (`esConflictoDeEstado`).
- `ConflictoVigenciaHorarioTranslator` — traducción defensiva del `23P01`. Su Javadoc ya declara
  la regla que F2C.2 debe repetir: *el flush que dispara la violación debe ocurrir dentro del
  bloque*.
- `RelojConfig` — bean `Clock` central (`systemDefaultZone()`).

### Excepciones por fecha (pre-F2B)

- `SalonHorarioExcepcion` — `salon`, `fecha`, `cerrado` (boolean), `horaApertura`, `horaCierre`,
  `activo`. **No persiste `diaSemana`** (§3 ya cumplido).
- `SalonHorarioExcepcionRepository` — `findBySalonIdAndFechaAndActivoTrue`,
  `findBySalonIdAndFechaBetweenAndActivoTrueOrderByFecha`.
- `SalonHorarioExcepcionService` — writer desalineado.

### Programación

- `TurnoInstructor` — `salon`, `tipo ∈ {RECURRENTE, EXCEPCION, CANCELACION}`, `diaSemana` (sólo
  RECURRENTE), `fecha` (sólo EXCEPCION/CANCELACION), horas, `activo`.
- `TurnoInstructorRepository.buscarExcepcionesPorSalonYFecha(salonId, fecha)` — **ya existe** y
  filtra exactamente `activo = true AND tipo = 'EXCEPCION'`. Es la consulta que necesita el adapter.
- `BloqueProgramacion` — regla **recurrente versionada**; **no tiene variante puntual**.
- `Asignacion` — `serieId`, `bloqueId`, `instructorId`, `tipoActividadId`, horas, vigencias.
- Port `ValidadorImpactoCambioHorarioOperacion` + adapters `ImpactoBloquesEnHorario` y
  `ImpactoTurnosRecurrentesEnHorario`. Dirección de dependencias:
  `calendario → ubicaciones`, `programacion → ubicaciones`, nunca al revés.

### Reservas

- `Reserva` — `salon`, `instructor`, `cliente`, `tipoActividad`, `fecha`, `horaInicio`, `horaFin`,
  `estado ∈ {CONFIRMADA, CANCELADA}`. **Sólo dos estados**; no hay pendiente ni no-show.
- `ReservaRepository.findBySalonIdAndFechaAndEstado(salonId, fecha, estado)` — **ya existe**. Es la
  consulta que necesita el adapter, con `CONFIRMADA`.
- `ReservaService.crear` — valida capacitación del instructor, encaje en un turno vigente del
  instructor y traslape. **No mira el horario del salón y no toma lock.**

### Consumidores reales de `HorarioEfectivoSalon`

Búsqueda global: **un solo consumidor productivo hoy**.

- `TurnoInstructorService` → `validarContraHorarioEfectivo(salonId, fecha, inicio, fin)`, usado
  **sólo** para turnos `EXCEPCION`. Distingue `estaCerrado()` y usa `vieneDeExcepcion()` para
  redactar el mensaje.

F2C.2 añade el segundo: `ReservaService.crear`.

Quien **no** lo usa, deliberadamente:

- `BloqueProgramacionService` valida contra el semanal versionado: un bloque es una regla
  recurrente y no debe depender de la excepción puntual de una fecha concreta.
- `SalonService` usa el semanal vigente para el detalle del salón.
- Los adapters de Política A dependen sólo de sus repositorios (evitan ciclo de beans).

---

## Problema

Representar el horario operativo real de **una fecha calendario concreta** cuando difiere del
template semanal, sin modificar el template.

| Caso | Escenario | ¿Resuelto hoy? |
|---|---|---|
| A | Lunes 08–20; `2026-09-14` CERRADO → cerrado sólo ese día | Sí (lectura) |
| B | Lunes 08–20; `2026-09-14` 10–16 → reducción sólo ese día | Sí (lectura) |
| C | Domingo cerrado; `2026-09-20` 09–14 → apertura excepcional | Sí (lectura) |
| D | Sin `HorarioOperacion` recurrente + HORARIO_ESPECIAL → abre | Sí (lectura) |

Los cuatro están cubiertos por `HorarioEfectivoSalonTest`. Lo que falta es que **escribirlos** sea
seguro, serializado, temporalmente coherente y consciente de la programación puntual.

---

## Modelo elegido

**Se conserva el agregado existente `SalonHorarioExcepcion` sobre la tabla
`salon_horario_excepcion`.** No se renombra.

1. El naming actual es coherente con la convención del módulo (`SalonRecurso`,
   `SalonHorarioOperacionService`), que antepone el agregado raíz.
2. El nombre ya está en entidad, repositorio, resolver, dos Javadoc de F2B, los DTOs y el frontend.
3. El renombrado no aporta capacidad de dominio alguna.

**Semántica: REPLACEMENT, no delta.** Ya implementada así en `HorarioEfectivoSalon`: cuando existe
excepción activa, el semanal **no se consulta siquiera** (test explícito:
`excepcionCerradaNiSiquieraConsultaElHorarioSemanal`). Un delta exigiría un template base que en
los casos C/D no existe.

**Estados conceptuales: exactamente dos** — `CERRADO` y `HORARIO_ESPECIAL`.

**Representación: se conserva el booleano `cerrado`. NO se introduce enum persistido ni columna
`tipo`.**

- El dominio tiene exactamente dos estados y es cerrado; un booleano es isomorfo a ese enum.
- El `CHECK` de `V18` ya liga estado y horas en ambas direcciones, que es la garantía real.
- Migrar a `tipo` obliga a tocar migración, entidad, DTO de request, DTO de response y frontend en
  producción, a cambio de cero capacidad nueva.
- Si apareciera un tercer estado, la migración es aditiva y mecánica.

**No se persiste `diaSemana`** (§3): la fecha lo determina, y `HorarioOperacionResolver` ya lo
deriva vía `DiaSemanaOperacion.desde(fecha.getDayOfWeek())`.

**No se modifica `HorarioEfectivoSalon`.**

---

## Semántica temporal del horario

**Horario especial** (§4): `hora_cierre > hora_apertura`, estricto. **No se soporta horario
nocturno cruzando medianoche**, porque el sistema no lo soporta en ningún sitio
(`HorarioOperacion`, `BloqueProgramacion`, `TurnoInstructor` y el `CHECK` de `V18` asumen todos
rango intra-día). Levantarlo sería un cambio transversal fuera de F2C.

**Cerrado** (§5): `hora_apertura IS NULL AND hora_cierre IS NULL`, obligatorio, ya garantizado por
el `CHECK`. Nunca `00:00–00:00`, nunca `00:00–23:59`, nunca `apertura == cierre`.

**Apertura excepcional** (§6): **soportada, sin requisito previo.** El writer no consulta
`HorarioOperacion` al guardar y el resolver da precedencia absoluta a la excepción. Casos C y D,
con test propio (`excepcionAbiertaAplicaAunqueNoHayaHorarioSemanalParaEseDia`).

**Reducción** (§7): `template 08–20` + `excepción 10–16` → `10–16`. El template no se toca.

**Extensión** (§8): **PERMITIDA.** `template 08–20` + `excepción 07–22` → `07–22`. Consecuencia
directa de replacement. Extender sólo **añade** ventana operativa, luego no puede dejar
programación existente fuera: es el caso trivialmente seguro. Aun así **la validación de impacto
se ejecuta igual**, sin caso especial: una extensión produce lista de conflictos vacía por
construcción, y no se introduce una rama que pueda desincronizarse.

---

## Mutabilidad temporal

Regla final, con `Clock` central inyectado. **Nunca `LocalDate.now()` directo**, en ningún punto
del writer ni de sus colaboradores.

| Fecha de la excepción | Crear | Modificar | Cancelar |
|---|---|---|---|
| **Pasado** (`fecha < hoy`) | **Rechazo** | **Rechazo** | **Rechazo** |
| **Hoy** (`fecha == hoy`) | Permitido | Permitido | Permitido |
| **Futuro** (`fecha > hoy`) | Permitido | Permitido | Permitido |

El pasado es **INMUTABLE** para las tres operaciones, con `EXCEPCION_HORARIO_EN_EL_PASADO`.
La frontera es `fecha.isBefore(LocalDate.now(reloj))`, idéntica a la de los writers semanales.

### Hoy es atómico

Decisión cerrada, y es la razón de que no exista ninguna regla basada en "ya ocurrió":

**F2C no modela cambios intra-día.** No existe la semántica "cerrar desde las 15:00". Una
excepción describe el estado operativo de **la fecha completa**.

Consecuencia directa, aceptada:

> Para una excepción de **hoy**, cualquier objeto puntual incompatible **de la fecha completa**
> bloquea la mutación, aunque su hora ya haya transcurrido.

- **No se usa `LocalTime.now()`** en ninguna validación de este diseño.
- No se introduce ninguna regla de "esto ya pasó, no cuenta". Sería una fuente de verdad nueva
  sobre el estado de una sesión, y el sistema no modela sesiones ni su estado.
- Un turno de las 09:00 incompatible impide cerrar hoy a las 18:00. Es conservador y es
  **determinista**: el resultado de la operación no depende del minuto en que se ejecute.

Esta decisión podrá revisarse cuando exista un modelo de sesiones/eventos con estado. Hasta
entonces, cualquier alternativa haría el comportamiento dependiente del reloj de pared, lo que es
intestable y sorprendente para el operador.

---

## Precedencia

El algoritmo de §2 del brief es correcto y **ya está implementado**:

```
ResolverHorarioSalon(salon, fecha):
  1. excepción activa exacta (salon, fecha)?
       CERRADO           -> HorarioEfectivo.cerrado()                     [Estado=CERRADO,      Origen=EXCEPCION]
       HORARIO_ESPECIAL  -> HorarioEfectivo.abiertoPorExcepcion(ap, ci)   [Estado=ABIERTO,      Origen=EXCEPCION]
  2. sin excepción -> HorarioOperacionResolver.resolver(salon, fecha)
       versión vigente   -> abiertoPorHorarioSemanal(ap, ci)              [Estado=ABIERTO,      Origen=SEMANAL]
       sin versión       -> noOperativo()                                 [Estado=NO_OPERATIVO,  Origen=NINGUNO]
```

No deja dos fuentes de verdad simultáneas: cuando hay excepción, el semanal no se consulta. La
composición está encapsulada en `HorarioEfectivoSalon`; ningún consumidor tiene que saber "primero
mira excepción, luego semanal" (§16 satisfecho).

Orden conceptual completo (§10):

```
horario semanal versionado (HorarioOperacion)
  → excepción de salón por fecha (SalonHorarioExcepcion)
  → HORARIO OPERATIVO EFECTIVO  ......................... HorarioEfectivoSalon  [frontera pública]
  → programación recurrente (BloqueProgramacion / TurnoInstructor RECURRENTE)
  → ajustes de programación por fecha (capa futura, fuera de F2C)
  → programación efectiva
```

La frontera pública está en el tercer escalón: todo lo que está por debajo consume
`HorarioEfectivo`, nunca las dos tablas por separado.

---

## Persistencia

**No se crea tabla nueva y no se requiere ninguna migración.** `V18` ya modela exactamente lo que
§13 pide. DDL real, literal:

```sql
CREATE TABLE salon_horario_excepcion (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    salon_id        UUID NOT NULL REFERENCES salon (id),
    fecha           DATE NOT NULL,
    cerrado         BOOLEAN NOT NULL DEFAULT false,
    hora_apertura   TIME,
    hora_cierre     TIME,
    activo          BOOLEAN NOT NULL DEFAULT true,
    creado_en       TIMESTAMPTZ NOT NULL DEFAULT now(),
    actualizado_en  TIMESTAMPTZ NOT NULL DEFAULT now(),
    CHECK (
        (cerrado = true AND hora_apertura IS NULL AND hora_cierre IS NULL)
        OR (cerrado = false AND hora_apertura IS NOT NULL AND hora_cierre IS NOT NULL AND hora_cierre > hora_apertura)
    )
);

CREATE UNIQUE INDEX idx_salon_horario_excepcion_unica ON salon_horario_excepcion (salon_id, fecha) WHERE activo;
CREATE INDEX idx_salon_horario_excepcion_fecha ON salon_horario_excepcion (fecha);
```

| Requisito | Estado |
|---|---|
| PK | Cumple (`id UUID`) |
| `salon_id` FK | Cumple |
| `fecha` | Cumple |
| Estado CERRADO / HORARIO_ESPECIAL | Cumple vía `cerrado BOOLEAN` |
| `hora_apertura` / `hora_cierre` | Cumple |
| Unicidad de override efectivo | Cumple como **índice único PARCIAL** `WHERE activo` |
| CHECK coherente CERRADO / ESPECIAL | Cumple, bidireccional |
| CHECK `hora_cierre > hora_apertura` | Cumple, estricto |

**El índice es PARCIAL y esto es central para el ciclo de vida.** Lo que garantiza es
exactamente esto y nada más:

```
por (salon_id, fecha):  como máximo 1 fila con activo = true
                        + N filas con activo = false, sin límite
```

**No existe ninguna restricción de unicidad sobre las filas inactivas**, y el diseño depende de
ello: sin esa holgura, cancelar y volver a crear la misma fecha fallaría con `23505`.

---

## Ciclo de vida de una fila: upsert y soft delete

Semántica exacta del writer, cerrada caso por caso.

### 1. Existe fila activa y el contenido solicitado es idéntico

**NO-OP real.** No se llama `save` ni `saveAndFlush`; no se emite `UPDATE`; `actualizado_en` no
cambia. Se devuelve la representación actual.

HTTP conceptual: **200** con la representación actual. Es observable en test como ausencia de
interacción de escritura sobre el repositorio, no sólo como "misma respuesta".

### 2. Existe fila activa y el contenido difiere

Si `fecha >= hoy`: se actualiza **LA MISMA fila activa**. Su configuración anterior queda
**sobrescrita**.

> **NO existe historial versionado de cada edición activa.** Editar tres veces la excepción del
> `2026-09-14` deja **una** fila, con el último contenido. Las ediciones intermedias no se
> conservan en ninguna parte.

Esto se declara explícitamente porque el checkpoint anterior insinuaba lo contrario al llamar
"auditoría" al soft-delete en general. Si en el futuro se quisiera trazabilidad de ediciones,
sería una capacidad nueva (tabla de auditoría o versionado explícito), no un efecto lateral del
modelo actual.

Si `fecha < hoy`: rechazo con `EXCEPCION_HORARIO_EN_EL_PASADO`, sin escritura.

### 3. Sólo existen filas inactivas para esa fecha

Se crea una **fila NUEVA** con `activo = true`.

**NO se reactiva una fila vieja.** Reactivar mezclaría dos decisiones operativas distintas en la
misma fila y borraría el rastro de la cancelación. El índice parcial hace legal la coexistencia
(N inactivas + 1 activa), así que no hay razón técnica para reutilizar.

### 4. Cancelación

`activo = false` sobre la fila activa. No hay `DELETE` físico.

**La cancelación/recreación sí deja rastro**: las filas anteriores permanecen en la tabla, con
`activo = false`. Es el único rastro histórico que el modelo produce, y sólo cubre el eje
"estuvo activa / dejó de estarlo" — nunca "cómo era antes de la última edición".

### Resumen

| Estado previo | Solicitud | Efecto en BD | Filas activas después |
|---|---|---|---|
| Ninguna fila | upsert | INSERT | 1 |
| Activa, contenido igual | upsert | **ninguno** | 1 (la misma) |
| Activa, contenido distinto | upsert | UPDATE de esa fila | 1 (la misma, sobrescrita) |
| Sólo inactivas | upsert | **INSERT nueva** | 1 (nueva) |
| Activa | cancelar | UPDATE `activo=false` | 0 |
| Ninguna activa | cancelar | ninguno → **404** | 0 |

---

## Programación recurrente

Es la decisión más delicada de la fase (§9). El brief ofrece A (supresión automática), B (rechazar
hasta que existan ajustes de programación por fecha) y C (otra).

**Se elige C, con una regla única: la Política A se aplica por granularidad temporal coincidente.**

### Por qué no B

B es inviable por aritmética del caso de uso: **el caso A del propio enunciado —cerrar por
festivo— es precisamente un día que casi siempre tiene bloques recurrentes.** Un lunes festivo
tiene los bloques de todos los lunes. Si un bloque recurrente incompatible bloqueara el cierre,
cerrar por festivo sería imposible en la práctica, o exigiría desactivar y recrear la programación
semanal para un solo día — exactamente lo que el brief prohíbe.

### Por qué no A tal cual

"Supresión automática" sugiere una escritura. No debe haber ninguna.

### Regla definitiva

**Una excepción puntual NO bloquea ni escribe templates recurrentes.**

Sobre `TurnoInstructor.RECURRENTE`, `BloqueProgramacion` y `Asignacion`, el writer de excepciones
**nunca** ejecuta:

- `delete`
- `update`
- `activo = false`
- cierre de vigencia

Cero escrituras, de cualquier clase. El template del siguiente lunes permanece intacto por
construcción: nada se escribió.

Y en el otro sentido: un template recurrente incompatible **nunca** rechaza la excepción.

---

## Ocurrencia recurrente efectiva

Corrige el diseño anterior, que hablaba de recorte automático.

**NO SE RECORTA.** Dado el horario efectivo del salón para la fecha `D`:

| Ocurrencia derivada del template para `D` | Resultado para `D` |
|---|---|
| Completamente contenida en el horario efectivo | **Se conserva INTACTA** |
| Parcialmente fuera | **Se OMITE** |
| Totalmente fuera | **Se OMITE** |
| Horario efectivo `CERRADO` o `NO_OPERATIVO` | **Se OMITE** |

Ejemplo canónico:

```
template recurrente : 08:00–12:00
horario especial D  : 10:00–16:00

NO se deriva          10:00–12:00
La ocurrencia 08:00–12:00 se OMITE ese día
El template del siguiente lunes permanece 08:00–12:00, intacto
```

Motivo: **recortar silenciosamente cambiaría duración, asignaciones y actividad.** Un bloque de
cuatro horas con dos instructores asignados por tramos no es "el mismo bloque, más corto": es otro
objeto, que nadie planificó y que el sistema no puede validar. Omitir es honesto; recortar es
inventar programación.

La derivación no se materializa hoy: `BloqueProgramacion` es una **regla**, no una ocurrencia, y
las ocurrencias no existen como filas. Esta regla es el contrato que deberá respetar la futura
fase de materialización de sesiones. **Derivar es, literalmente, no escribir nada hoy.**

### Sin conteo de impacto recurrente

Queda **fuera del alcance de F2C.2**, y se elimina del diseño anterior:

- conteo de ocurrencias recurrentes afectadas;
- warning en la respuesta;
- cualquier metadata de impacto recurrente en el DTO de salida.

No existe contrato actual para ello, ningún consumidor lo pide, y **no es necesario para
correctness**. Añadirlo sería inventar superficie de API en una fase de endurecimiento.

---

## Programación puntual real

La asimetría con lo recurrente es deliberada y el criterio es nítido: **un objeto puntual de fecha
choca con una excepción de fecha; un template recurrente no.**

Objetos puntuales que existen **hoy** en el sistema, verificados:

| Objeto | Puntual | ¿Bloquea la excepción? | Justificación |
|---|---|---|---|
| `TurnoInstructor.Tipo.EXCEPCION` (activo, con `fecha`) | Sí | **Sí** | Lo creó un administrador para ese día concreto y es directamente ajustable |
| `Reserva` con `estado = CONFIRMADA` | Sí | **Sí** | Es un compromiso con un cliente en una fecha y hora concretas |
| `TurnoInstructor.Tipo.CANCELACION` | Sí | **No** | **No es operativo**: es un marcador de "no se atiende" que cubre el día como `00:00–23:59`. `TurnoInstructorService` ni siquiera lo valida contra el horario, y `ReservaService.turnosVigentes` lo trata como ausencia de turnos. No hay nada que pueda quedar fuera de horario |
| `Reserva` con `estado = CANCELADA` | Sí | **No** | Ya no compromete nada |
| `BloqueProgramacion` | **No existe variante puntual** | No aplica | Es siempre regla recurrente versionada |

`Reserva.Estado` tiene **exactamente** `{CONFIRMADA, CANCELADA}`. No se inventan estados
intermedios: no hay pendiente, ni no-show, ni reservada-sin-confirmar.

Esto **restaura una invariante que hoy el sistema no mantiene**: "todo objeto puntual operativo de
una fecha cabe en el horario efectivo de esa fecha". Hoy se puede violar cerrando el día por
excepción.

---

## Port neutral de impacto puntual

El módulo `ubicaciones` **no debe importar** `TurnoInstructor`, `Reserva`, sus repositorios ni
ningún internal de calendario. La dirección de dependencias es `calendario → ubicaciones`, y no se
invierte por esto.

Se declara en `ubicaciones/dominio`, en paralelo exacto a
`ValidadorImpactoCambioHorarioOperacion`.

### Value object neutral

`CambioExcepcionHorario` — descripción neutral del estado operativo que va a regir en
`(salonId, fecha)`:

| Campo | Tipo | Nota |
|---|---|---|
| `salonId` | `UUID` | no nulo |
| `fecha` | `LocalDate` | no nula; la fecha completa, sin componente horario |
| `estado` | `Estado ∈ {CERRADO, HORARIO_ESPECIAL}` | no nulo |
| `horaApertura` | `LocalTime` nullable | sólo si `HORARIO_ESPECIAL` |
| `horaCierre` | `LocalTime` nullable | sólo si `HORARIO_ESPECIAL` |

Invariante del constructor, igual que en `CambioHorarioOperacion`: **sólo `HORARIO_ESPECIAL` porta
horas**, y `HORARIO_ESPECIAL` las exige ambas. Un `CambioExcepcionHorario` mal formado no se puede
construir.

**No se reutiliza `HorarioEfectivo`.** Ese tipo modela "qué pasa en una fecha" tras resolver la
precedencia y porta `Origen`, semántica que aquí no aplica: esto describe un cambio **solicitado**,
todavía no vigente.

### Operación de dominio

```
admite(inicio, fin) : boolean
```

| Estado | Regla |
|---|---|
| `CERRADO` | **No admite ningún intervalo.** Siempre `false` |
| `HORARIO_ESPECIAL` | Admite **únicamente** intervalos completamente contenidos: `inicio >= horaApertura` **y** `fin <= horaCierre` |

**No es intersección ni solape.** Un turno `07:00–09:00` contra un horario `08:00–16:00` **solapa**
pero **no cabe**: es conflicto. Es la misma semántica que `HorarioEfectivo.contiene` y que
`CambioHorarioOperacion.admite`, que ya existen y ya se comportan así. Se exige además que el
intervalo sea válido (`fin > inicio`).

### Forma del port — decisión cerrada

```java
public interface ValidadorImpactoExcepcionHorario {
    List<ConflictoProgramacionPuntual> evaluar(CambioExcepcionHorario cambio);
}
```

**Lista, no boolean.** La decisión queda cerrada aquí y no se deja al implementador:

- es la forma que ya tiene `ValidadorImpactoCambioHorarioOperacion`, y dos ports gemelos con
  firmas distintas divergen;
- un `boolean` haría el rechazo indiagnosticable: el operador recibiría "hay algo que estorba" sin
  poder saber qué;
- lista vacía significa "sin impacto"; **nunca `null`**.

Contrato, idéntico al port existente:

- evaluación **síncrona**, dentro de la transacción del writer;
- se invoca **antes** de cualquier escritura;
- si un adapter falla, la operación completa revierte;
- el writer inyecta `List<ValidadorImpactoExcepcionHorario>` y no conoce ninguna implementación.

### `ConflictoProgramacionPuntual`

Tipo **neutral**, en `ubicaciones/dominio`. No importa entidades de calendario. Contiene
únicamente lo mínimo para (a) saber que hay conflicto y (b) diagnosticar en test o log controlado:

| Campo | Para qué |
|---|---|
| `origen` | enum neutral `{TURNO_EXCEPCION, RESERVA_CONFIRMADA}` |
| `id` | `UUID` del objeto que estorba, para diagnóstico |
| `detalle` | texto corto opcional (p. ej. el rango que no cabe) |

Es el mismo patrón que `ConflictoProgramacion`, cuyo Javadoc ya declara: *"`detalle` es texto de
diagnóstico para el mensaje de error, no un DTO HTTP"*.

**Estos detalles no se exponen en la API** salvo necesidad cerrada. El mensaje de error lleva el
código estable; los IDs viajan en el texto de diagnóstico, como ya hace
`HorarioOperacionErrores.verificarSinImpacto`. No se construye un DTO de conflictos.

### Uso por el writer

```
evaluar(cambio)
  → lista vacía   → continuar
  → lista no vacía → PROGRAMACION_PUNTUAL_INCOMPATIBLE_CON_EXCEPCION   (409)
```

Sin ramas condicionales por estado: **CERRADO y HORARIO_ESPECIAL recorren el mismo camino**, y la
diferencia vive dentro de `admite`.

---

## Adapter de impacto puntual

`TurnoInstructor` y `Reserva` viven hoy en `calendario`. Por tanto **un solo adapter**, del lado
de `calendario`, implementa el port neutral y se registra como bean de Spring.

**No se importa ningún repositorio de calendario dentro del dominio de `ubicaciones`.**

Consultas, ambas ya existentes en el repositorio:

**A. Turnos puntuales operativos**

`TurnoInstructorRepository.buscarExcepcionesPorSalonYFecha(salonId, fecha)` — ya filtra
`activo = true AND tipo = 'EXCEPCION'`.

- **NO** consulta `RECURRENTE`.
- **NO** consulta `CANCELACION`.

**B. Reservas comprometidas**

`ReservaRepository.findBySalonIdAndFechaAndEstado(salonId, fecha, Reserva.Estado.CONFIRMADA)`.

- **NO** consulta reservas `CANCELADA`.

Para cada objeto encontrado, el adapter evalúa `cambio.admite(inicio, fin)` y añade un
`ConflictoProgramacionPuntual` si no cabe. El adapter **no decide la política**: sólo reporta.

Nota de diseño: podría dividirse en dos adapters (uno por objeto), lo que encaja igual con
`List<ValidadorImpactoExcepcionHorario>`. Se elige **uno solo** porque ambas consultas viven en el
mismo módulo, comparten `(salonId, fecha)` y separarlas duplicaría el bean sin ganancia. La forma
del port admite dividirlo después sin tocar el writer.

---

## Regla de impacto

**Para `CERRADO`:**

cualquier `TurnoInstructor.EXCEPCION` operativo **o** cualquier `Reserva.CONFIRMADA` de esa fecha
→ **conflicto**. Un día cerrado no admite ningún intervalo.

**Para `HORARIO_ESPECIAL`:**

cada intervalo puntual debe estar **COMPLETAMENTE contenido**.

Con horario `08:00–16:00`:

| Turno / reserva | Resultado |
|---|---|
| `10:00–12:00` | **Compatible** |
| `08:00–16:00` | **Compatible** — tocar exactamente ambos bordes está permitido |
| `08:00–09:00` | **Compatible** — borde de apertura |
| `15:00–16:00` | **Compatible** — borde de cierre |
| `16:00–17:00` | **Incompatible** |
| `07:00–09:00` | **Incompatible** |
| `07:00–18:00` | **Incompatible** |

Los bordes son inclusivos en ambos extremos: `inicio >= apertura`, `fin <= cierre`. Coincide con
`HorarioEfectivo.contiene` y con la validación que ya aplica `TurnoInstructorService` a los turnos
`EXCEPCION`.

---

## Protocolo de lock

**Se reutiliza `SalonLock`. No se crea un segundo lock ni un segundo protocolo.**

Regla unificada, para todo writer que participe de la invariante:

```
lock(Salon)  ->  lecturas dependientes  ->  validación  ->  escritura  ->  commit
```

Validar antes y bloquear después no serializa nada: las dos transacciones habrían leído estado
viejo y ambas podrían commitear lados incompatibles. **No se invierte el orden.**

F2C.2 debe incorporar `SalonLock` a los tres writers:

| # | Writer | Cuándo | Estado hoy |
|---|---|---|---|
| **A** | `SalonHorarioExcepcionService` | `create` / `update` / `cancel` | No lo toma |
| **B** | `TurnoInstructorService.crear` | **sólo** cuando `tipo == EXCEPCION` | No lo toma (excluido explícitamente) |
| **C** | `ReservaService.crear` | siempre | No lo toma |

**Lock ordering: `Salon` primero, siempre y único.** Con un solo recurso de bloqueo el deadlock es
estructuralmente imposible, que es la propiedad que ya tienen `VersionarHorarioOperacion`,
`CerrarHorarioOperacion` y `BloqueProgramacionService`.

`SalonLock` es `Propagation.MANDATORY`: cada writer debe correr en transacción de escritura propia
(`@Transactional` sin `readOnly`). `adquirir` devuelve el `Salon` ya bloqueado y sirve también como
comprobación de existencia, eliminando el `findById` redundante que hoy hacen
`SalonHorarioExcepcionService.guardar` y `ReservaService.crear`.

**La autorización va antes del lock**, como ya hace `TurnoInstructorService`: no se retiene un lock
por peticiones que no tienen permiso.

### Por qué el lock también en reservas

Se documenta explícitamente para que no se lea como más de lo que es:

> Usar `SalonLock` en `ReservaService` es una **decisión de consistencia del modelo legacy**, no el
> diseño final de concurrencia de reservas.

Objetivo actual, y sólo ese: **serializar "excepción de horario" contra "creación de reserva" sobre
el mismo salón**, para que no puedan commitear a la vez una reserva y un cierre que la deja fuera
de horario.

Lo que **no** se afirma:

- que la fila de `Salon` sea la granularidad correcta para el volumen real de reservas;
- que esto resuelva capacidad, aforo o contención de recursos — no hay modelo de ninguno.

Cuando existan `Sesion`, capacidad y recursos, la granularidad podrá evolucionar (lock por sesión,
por recurso, o control optimista sobre aforo). **Fuera de F2C.** Contención esperada hoy: baja, y
el coste de no serializar es una invariante rota.

---

## `TurnoInstructorService.crear(EXCEPCION)`

Cambio requerido en F2C.2:

```
1. resolver permisos por tipo            (ya existe)
2. autorizar                             (ya existe, va primero a propósito)
3. si tipo == RECURRENTE  -> SalonLock   (ya existe, protocolo F2B)
   si tipo == EXCEPCION   -> SalonLock   (NUEVO)
4. resolver HorarioEfectivoSalon para la fecha
5. validar traslapes y demás reglas
6. persistir
```

El lock debe adquirirse **ANTES** de resolver `HorarioEfectivoSalon` y antes de persistir. Así el
turno `EXCEPCION` queda serializado con crear/modificar/cancelar excepción de salón: el que llega
segundo ve el estado del primero.

- **`RECURRENTE`: se mantiene el protocolo F2B ya existente, sin cambios.**
- **`CANCELACION`: sigue sin tomar el lock.** No valida horario y no es operativo; tomarlo daría
  falsa sensación de protección.
- **Revisar con cuidado que no se introduzca doble adquisición ni reordenamiento accidental.** La
  condición actual es un `if (tipo == RECURRENTE)`; convertirla en `if (tipo == RECURRENTE ||
  tipo == EXCEPCION)` mantiene una sola llamada. Adquirir dos veces es reentrante en Postgres pero
  señala confusión de protocolo, y **la posición relativa a la autorización no debe moverse**.
- **Actualizar el comentario y el Javadoc** que hoy justifican la exclusión de `EXCEPCION`: dejarán
  de ser ciertos.

---

## `ReservaService.crear`

F2C.2 **debe modificar también este writer**. Hoy una reserva puede crearse en un salón cerrado por
excepción, porque el horario del salón no se consulta en absoluto.

Orden conceptual:

```
1. validar el request sintácticamente
2. determinar el salón
3. adquirir SalonLock(salonId)                                   [NUEVO]
4. resolver HorarioEfectivoSalon para la fecha                   [NUEVO]
5. comprobar cobertura COMPLETA de [horaInicio, horaFin]         [NUEVO]
6. validar el resto de reglas existentes
     - capacitación del instructor en la actividad
     - encaje en un turno vigente del instructor
     - ausencia de traslape con otra reserva confirmada
7. persistir
```

Reglas cerradas:

- **No se crea una reserva fuera del horario operativo efectivo.**
- **NO basta con resolver el horario semanal**: debe usarse `HorarioEfectivoSalon`, que considera la
  excepción de la fecha. Resolver sólo el semanal reintroduciría el defecto por otro camino.
- Cobertura **completa**, no solape: `inicio >= apertura` y `fin <= cierre`.
- `CERRADO` y `NO_OPERATIVO` rechazan cualquier reserva.
- La autorización sigue siendo lo primero, antes del lock.

La validación 5 es independiente de la 6: una reserva puede caber en el turno del instructor y aun
así quedar fuera del horario del salón si ese día hay excepción.

---

## Race conditions

| # | Escenario | Protección |
|---|---|---|
| A | Dos upserts para la misma fecha, a la vez | `SalonLock` serializa. El segundo, al despertar, relee y encuentra la fila del primero → **update** sobre ella (o no-op si el contenido coincide), no insert. **Defensa DB**: índice único parcial |
| B | Excepción vs. versionado del horario semanal | `SalonLock` **compartido** con `VersionarHorarioOperacion` y `CerrarHorarioOperacion` → serializados |
| C | Modificar excepción vs. cancelarla | `SalonLock` compartido. Si cancelar va primero, el upsert posterior no encuentra activa y crea fila nueva. Si el upsert va primero, la cancelación desactiva la fila ya actualizada. Ningún orden deja estado inconsistente ni dos filas activas |
| D | Excepción vs. `TurnoInstructor.EXCEPCION` en esa fecha | **Ambos** adquieren `SalonLock` (B del protocolo). El segundo ve el estado del primero y es rechazado con su código estable |
| E | Excepción vs. `ReservaService.crear` | **Ambos** adquieren `SalonLock` (C del protocolo). Idem |

**Los casos D y E requieren el lock en los dos lados.** El checkpoint anterior afirmaba que
`SalonLock` en el writer de excepción dejaba excluidos a los writers puntuales: es falso, porque
esos writers no lo tomaban. Un lock que sólo adquiere una parte no serializa nada; la otra
transacción pasa de largo. Esa afirmación queda eliminada.

---

## Traducción de 23505

### Estado real, corregido

`GlobalExceptionHandler:68` ya mapea `DataIntegrityViolationException` a **409** con el mensaje
genérico `"Conflicto de datos: el recurso ya existe o viola una restricción"` y `codigo = null`.

> El defecto **no es** que produzca 500.
> El defecto es que sale **sin código estable** y **sin traducción específica por constraint**:
> el cliente no puede distinguir un choque de excepción de horario de cualquier otra violación de
> integridad del sistema.

F2C.2 debe traducir **defensivamente** el constraint único, siguiendo el patrón ya establecido por
`ConflictoVigenciaHorarioTranslator`.

### `saveAndFlush`, no `save`

Para que la violación ocurra **dentro** del traductor:

```java
try {
    return repository.saveAndFlush(excepcion);
} catch (DataIntegrityViolationException e) {
    // ... clasificar y traducir, o relanzar
}
```

**No dejar `save(...)`**: con `save`, Hibernate difiere el `INSERT` al flush del commit, la
excepción se lanza **fuera** del método a través del proxy transaccional de Spring, y ningún
`catch` del writer la ve. El Javadoc de `ConflictoVigenciaHorarioTranslator` ya documenta
exactamente este fallo para el `23P01`.

### Clasificación estricta

Se traduce **únicamente** cuando en la cadena de causas se identifica:

- SQLSTATE **`23505`** (`unique_violation`), **y**
- constraint `idx_salon_horario_excepcion_unica`;
- **o bien** el constraint no está disponible pero la evidencia es inequívoca, con el mismo criterio
  de convención que ya usa el traductor existente.

Reglas firmes, copiadas del patrón vigente:

- Se inspecciona primero el `ConstraintViolationException` de Hibernate, **no** la `PSQLException`
  del driver, para no acoplar `ubicaciones` a PostgreSQL.
- Si Hibernate identificó el constraint y **no** es el esperado → **no se traduce**, y no se cae al
  barrido de `SQLException`.
- El barrido de `SQLException` existe sólo para el caso en que Hibernate no envolvió el error.
- **Revisar en implementación qué metadata expone realmente pgjdbc/Hibernate** en esta ruta: el
  nombre de constraint de un índice único parcial debe verificarse contra el driver real, no
  asumirse. Es la razón de que la regla admita el caso "constraint no disponible".
- **Nunca** se convierte un `DataIntegrityViolationException` genérico en
  `CONFLICTO_EXCEPCION_HORARIO`. Cualquier otra violación **se relanza intacta**: ocultarla haría
  indiagnosticable un bug distinto.

Con `SalonLock` el camino normal ya no llega aquí. El traductor es **backstop**, no control de
flujo.

---

## API

### Legacy — se preserva

Endpoints reales existentes, consumidos hoy por `web/src/api/salones.ts`:

```
GET    /api/salones/{salonId}/excepciones-horario?desde&hasta
PUT    /api/salones/{salonId}/excepciones-horario            (fecha en el body)
DELETE /api/salones/{salonId}/excepciones-horario/{id}       (UUID)
```

En F2C.2:

- **NO se eliminan.**
- **NO se cambian sus paths.**
- **NO se rompen sus DTOs** sin necesidad.

El comportamiento **sí** cambia por debajo (temporalidad, lock, impacto puntual, códigos estables):
eso es el objeto de la fase. Lo que se preserva es el contrato de transporte.

### Nueva API por fecha

**No puede haber `DELETE /{id}` y `DELETE /{fecha}` simultáneos.** Spring no puede diferenciar dos
plantillas de un solo segmento variable: `/{id}` y `/{fecha}` compiten por el mismo patrón y el
mapeo es ambiguo. No es cuestión de orden de declaración ni de tipo de `@PathVariable`.

Contrato nuevo, cerrado, bajo un prefijo literal que elimina la ambigüedad:

```
PUT    /api/salones/{salonId}/excepciones-horario/por-fecha/{fecha}
DELETE /api/salones/{salonId}/excepciones-horario/por-fecha/{fecha}
```

El `GET` existente permanece tal cual; no se duplica.

Para el `PUT` nuevo: **la fecha del PATH es autoritativa.** El body **no** necesita repetirla.

DTO conceptual (propuesto, **no implementado**):

```json
{
  "cerrado": true,
  "horaApertura": null,
  "horaCierre": null
}
```

```json
{
  "cerrado": false,
  "horaApertura": "10:00",
  "horaCierre": "16:00"
}
```

Si una fase posterior decidiera aceptar `fecha` también en el body por compatibilidad, debe
rechazarse la discrepancia con el path, nunca resolverse en silencio. Hoy: el body no la lleva.

### Convergencia obligatoria

- **Legacy `PUT` y nuevo `PUT` convergen en UN MISMO caso de uso.** El controller legacy extrae la
  fecha del body, el nuevo la extrae del path, y ambos llaman al mismo método de servicio.
- **Legacy `DELETE` y `DELETE` por fecha convergen en LA MISMA lógica de cancelación.** El legacy
  resuelve primero `id → (salon, fecha)` respetando el orden de autorización de abajo, y a partir
  de ahí es el mismo camino.

Dos implementaciones paralelas divergirían en la primera corrección que sólo se aplicase a una.

Códigos HTTP: `200` en ambos `PUT` (también en alta), `400` validación, `403` autorización,
`404` recurso ausente, `409` conflicto de estado.

### Retiro de legacy

Rollout explícito:

| Fase | Backend | Frontend |
|---|---|---|
| **F2C.2** | legacy **+** `por-fecha`, ambos vivos | sin cambios, sigue en legacy |
| **F2C frontend (posterior)** | ambos vivos | migra a `por-fecha` |
| **Fase separada, tras validación** | puede retirar legacy | ya migrado |

**NO se retira legacy durante F2C.2.** El retiro es una fase propia, con su propia validación.

### Orden de autorización del `DELETE` legacy

Corrige el defecto verificado en `SalonHorarioExcepcionService.eliminar`, que hoy hace `findById`
antes de autorizar.

Orden obligatorio:

```
1. validar sintaxis (UUID bien formado)
2. AutorizadorSalon.verificarAccesoSalon sobre el salonId CONTEXTUAL (el del path)
3. adquirir SalonLock(salonId)
4. buscar la excepción activa por id
5. comprobar que excepcion.salonId == salonId contextual
6. aplicar la regla de temporalidad
7. soft delete
```

**NO ejecutar `findById` antes de la autorización.**

Objetivo: **no revelar la existencia de una excepción de otro salón.** Con el orden actual, un
actor con scope sobre el salón A puede distinguir, por el status de respuesta, si un UUID existe en
el salón B. Con el orden corregido, un actor sin scope recibe `403` antes de que se lea nada, y un
actor con scope que pasa un id de otro salón recibe `404` (paso 5) sin poder inferir más.

El `DELETE` por fecha no tiene este problema por construcción: `(salonId, fecha)` hace el scope
estructural.

---

## Idempotencia

- **Upsert de la misma fecha con el mismo contenido → `200`, misma representación, CERO escritura.**
  No `409`. `PUT` declara un estado deseado; si el estado ya es ése, la operación tuvo éxito.
  "Cero escritura" es parte del contrato y es observable en test, no un detalle de implementación.
- **El `PUT` que crea también devuelve `200`**, no `201`. El cliente no necesita saber si existía,
  la URL no cambia, y el frontend actual ya espera `200`.
- **`CERRADO → HORARIO_ESPECIAL` en fecha futura es un update legítimo**, `200`. Igual en sentido
  inverso y para cambiar las horas.
- **Cancelación de una excepción inexistente → `404`.** **Decisión cerrada: no se trata como `204`
  idempotente.** Es coherente con `ResourceNotFoundException` en el resto del proyecto, y la
  alternativa ocultaría al operador que su cancelación no hizo nada.

---

## Errores

Convención del repositorio, respetada: **el código estable es el prefijo del mensaje** de la
`ValidacionException`, extraído por `CodigoErrorExtractor` y expuesto en `ErrorResponse.codigo`.
Se reutiliza esa infraestructura tal cual. **Ningún cliente parsea textos.**

Catálogo mínimo, en una clase nueva `SalonHorarioExcepcionErrores` paralela a
`HorarioOperacionErrores`:

| Código | Cuándo | HTTP |
|---|---|---|
| `EXCEPCION_HORARIO_EN_EL_PASADO` | `fecha` anterior al hoy de negocio, en crear / modificar / cancelar | 400 |
| `HORARIO_ESPECIAL_INCOMPLETO` | `cerrado = false` sin `horaApertura` u `horaCierre` | 400 |
| `HORA_CIERRE_DEBE_SER_POSTERIOR` | `horaCierre <= horaApertura`. **REUTILIZADO** de `HorarioOperacionErrores` | 400 |
| `EXCEPCION_HORARIO_NO_EXISTE` | cancelación sin excepción activa para `(salon, fecha)`, o id ajeno al salón | 404 |
| `PROGRAMACION_PUNTUAL_INCOMPATIBLE_CON_EXCEPCION` | el port reporta al menos un conflicto puntual | 409 |
| `CONFLICTO_EXCEPCION_HORARIO` | backstop del índice único (`23505`) | 409 |

Decisiones sobre el catálogo:

- **`HORA_CIERRE_DEBE_SER_POSTERIOR` se reutiliza literalmente**, no se duplica: es exactamente la
  misma condición de dominio que en el writer semanal.
- **No se añaden códigos sin necesidad.** Se descarta el `PROGRAMACION_INCOMPATIBLE_CON_EXCEPCION`
  genérico del brief: sería engañoso, porque la programación **recurrente** nunca produce rechazo.
  El nombre elegido dice "puntual" para que el código no prometa una protección inexistente.
- La **whitelist 409** se construye igual que `HorarioOperacionErrores.CONFLICTOS_DE_ESTADO`:
  cerrada y explícita, con los dos códigos 409 y nada más. Todo lo demás sigue siendo 400.

---

## Seguridad

- `salon.administrar` para `PUT` y `DELETE` (ambas variantes); `salon.leer` para `GET`.
- `@PreAuthorize` en el controller como permiso de borde.
- **`AutorizadorSalon.verificarAccesoSalon` como primera sentencia del método de servicio**, antes
  de cualquier lectura y antes del lock, para que **403 preceda a 404**.
- El `DELETE` legacy corrige su orden actual (ver [orden de autorización](#orden-de-autorización-del-delete-legacy)).
- El `DELETE` por fecha tiene el scope estructural en la clave natural `(salonId, fecha)`.

---

## Resolver efectivo

**`HorarioEfectivoSalon` no cambia**: ni firma, ni comportamiento, ni precedencia. Sigue siendo el
resolver público único y su composición está encapsulada (§16 satisfecho).

Precedencia existente, conservada: excepción activa exacta → semanal versionado → `NO_OPERATIVO`.

**Forma del resultado (§17): se conserva `HorarioEfectivo`, no se usa `Optional`.** El
`record(Estado, Origen, horaApertura, horaCierre)` distingue exactamente lo que §17 exige no
colapsar:

- `NO_OPERATIVO` / `Origen.NINGUNO` → **no existe template** para ese día;
- `CERRADO` / `Origen.EXCEPCION` → **cerrado por excepción**.

Un `Optional` vacío fusionaría ambos. `Origen` es la única metadata presente y está justificada:
`TurnoInstructorService` ya la usa vía `vieneDeExcepcion()`. **No se añade metadata adicional.**

**Se conservan los tests existentes de `HorarioEfectivoSalonTest` y no se duplica su cobertura.**

Punto de atención documentado, no defecto: `HorarioEfectivo.cerrado()` fija `Origen.EXCEPCION`
incondicionalmente. Es correcto hoy porque `CERRADO` sólo puede originarse en una excepción.

---

## Migraciones

**F2C.2: CERO MIGRACIONES.**

- El esquema de `V18` soporta el diseño completo.
- **V47 PERMANECE LIBRE.**
- Si la implementación descubriera una necesidad real de DDL: **DETENER y reabrir el diseño.** No
  se improvisa una migración dentro de una fase de endurecimiento.

---

## Test plan

Safety net para F2C.2. **Ninguno de estos tests existe hoy**: `SalonHorarioExcepcionService` no
tiene cobertura directa.

Plan reducido respecto al anterior (35 → **31 métodos**, varios parametrizados). El objetivo del
review era ~29; no es cuota rígida y la cobertura se justifica **por comportamiento**, no por
alcanzar un número. Todo caso listado protege una decisión de este documento o detecta una mutación
del catálogo de abajo.

### Unit, Mockito sin BD — `SalonHorarioExcepcionServiceTest` (10)

| # | Caso | Protege |
|---|---|---|
| W1 | Temporalidad de alta, parametrizado: **pasado rechazado** / **hoy aceptado** / **futuro aceptado** | Pasado inmutable; hoy mutable |
| W2 | Sin fila previa → INSERT de fila activa | Alta |
| W3 | Activa + **mismo contenido** → **no-op**: `verify(repo, never()).save(any())` y `never()).saveAndFlush(any())`, y se devuelve la representación actual | No-op real, no "misma respuesta" |
| W4 | Activa + contenido distinto → UPDATE sobre **la misma fila** (mismo `id`), sin INSERT | Update destructivo, una sola activa |
| W5 | Temporalidad de modificación y cancelación, parametrizado: pasado rechazado / hoy y futuro permitidos (cancelar deja `activo=false`) | Pasado inmutable en las tres operaciones |
| W6 | Cancelar sin fila activa → `EXCEPCION_HORARIO_NO_EXISTE` (404), no 204 | Decisión cerrada de no-idempotencia |
| W7 | Sólo filas inactivas → **INSERT de fila nueva**, ninguna reactivación (`setActivo(true)` sobre la vieja nunca ocurre) | Recreación tras cancelación |
| W8 | Validación y normalización, parametrizado: `CERRADO` con horas → se normalizan a `null`; especial sin horas → `HORARIO_ESPECIAL_INCOMPLETO`; `cierre <= apertura` (incluido `==`) → `HORA_CIERRE_DEBE_SER_POSTERIOR` | Forma del estado |
| W9 | `InOrder`: `verificarAccesoSalon` → `SalonLock.adquirir` → **primera** lectura de repositorio | Protocolo completo, en un solo test |
| W10 | `Clock.fixed` en una fecha **distinta de la del sistema**: una excepción que es futura según el reloj fijo y pasada según el reloj real (o al revés) se resuelve por el reloj fijo | **Falla si la implementación usa `LocalDate.now()` directo** |

Todos usan `Clock.fixed`. W10 existe específicamente para que un `LocalDate.now()` sin `Clock` no
pueda pasar verde.

### Impacto puntual — adapter + integración con el writer (5)

| # | Caso | Protege |
|---|---|---|
| I1 | `CERRADO`, parametrizado por tipo de turno: `EXCEPCION` activo → **conflicto**; `CANCELACION` → **sin conflicto**; y se verifica que la consulta de `RECURRENTE` **no se invoca** | Puntual bloquea, cancelación no, recurrente ni se consulta |
| I2 | `HORARIO_ESPECIAL` `08–16` contra turno `EXCEPCION`, parametrizado: `10–12` ok, `08–16` ok (bordes), `16–17` conflicto, `07–09` conflicto, `07–18` conflicto | Contención completa, no solape |
| I3 | `Reserva.CONFIRMADA`, parametrizado: contenida → ok; parcialmente fuera → conflicto; con `CERRADO` → conflicto | Reservas como objeto puntual |
| I4 | `Reserva.CANCELADA` en la fecha → **no bloquea** en ningún estado del cambio | No se inventan estados |
| I5 | El writer, con lista de conflictos no vacía, lanza `PROGRAMACION_PUNTUAL_INCOMPATIBLE_CON_EXCEPCION` y **no escribe nada**; con lista vacía, persiste | Política A puntual y orden validar→escribir |

### Integration PostgreSQL, Testcontainers (7)

| # | Caso |
|---|---|
| P1 | `CHECK` real, parametrizado: `cerrado = true` con horas → rechazado; `cerrado = false` sin horas → rechazado; `cerrado = false` con `cierre == apertura` → rechazado |
| P2 | Índice parcial: dos filas **activas** con el mismo `(salon_id, fecha)` → `23505` |
| P3 | Índice parcial: **N filas inactivas + 1 activa** con el mismo `(salon_id, fecha)` → **permitido**. Es lo que hace viable el soft-delete |
| P4 | Cancelar y recrear la misma fecha, contra BD real: la vieja queda `activo=false`, la nueva es una fila distinta y activa, sin violar el índice |
| P5 | Traducción específica: violación del índice → **409 con `codigo = CONFLICTO_EXCEPCION_HORARIO`**. Se asserta el `codigo`, no sólo el status, porque el status ya era 409 antes del cambio |
| P6 | Otra violación de integridad (p. ej. FK a salón inexistente) **no** se traduce: sale como el genérico actual, no como `CONFLICTO_EXCEPCION_HORARIO` |
| P7 | Tras cerrar un día con programación recurrente, los `BloqueProgramacion` y los `TurnoInstructor` RECURRENTE quedan **idénticos** (`activo`, vigencias y horas sin cambios) |

### Concurrency, patrón de `HorarioOperacionConcurrenciaTest` (5)

Dos hilos, `TransactionTemplate` propio por hilo, `CountDownLatch` para forzar el orden.
Deterministas donde sea viable; **`Thread.sleep` no se usa como mecanismo de sincronización
principal** — sólo, si acaso, como timeout de seguridad.

| # | Caso |
|---|---|
| C1 | Dos upserts simultáneos para la misma fecha → se serializan; resultado final **una sola fila activa** |
| C2 | Modificar vs. cancelar la misma excepción → ningún orden deja dos activas ni pierde la operación en silencio |
| C3 | Excepción vs. writer del horario semanal → se serializan sobre `SalonLock`; el efectivo resultante es coherente con el orden real |
| C4 | Excepción de cierre vs. `TurnoInstructorService.crear(EXCEPCION)` en esa fecha → el segundo ve el estado del primero y es rechazado con su código estable |
| C5 | Excepción de cierre vs. `ReservaService.crear` en esa fecha → idem |

C4 y C5 fallan si el lock se añade sólo al writer de excepción.

### API (4)

| # | Caso |
|---|---|
| A1 | Los cuatro endpoints de escritura, parametrizado: legacy `PUT` (fecha en body), legacy `DELETE /{id}`, nuevo `PUT /por-fecha/{fecha}`, nuevo `DELETE /por-fecha/{fecha}` — todos operan sobre el mismo estado y producen el mismo efecto |
| A2 | Los mappings **no colisionan**: el contexto arranca, un `DELETE` con UUID llega al handler legacy y uno con fecha ISO al de `por-fecha` |
| A3 | `403` antes de revelar el recurso: actor sin scope sobre el salón recibe 403 en el `DELETE` legacy **sin** que se lea la excepción; actor con scope que pasa un id de otro salón recibe `404` |
| A4 | Códigos estables en el body: cada error del catálogo llega con su `codigo`; whitelist 409 cerrada — los dos códigos listados dan 409, cualquier otro de este dominio da 400 |

**Total: 31 métodos** (10 + 5 + 7 + 5 + 4), con parametrizados en W1, W5, W8, I1, I2, I3, P1 y A1.

---

## Mutaciones que el plan debe detectar

Cada mutación queda **DETECTADA** por al menos un test nominal o por un invariante especificado.

| # | Mutación | Detectada por |
|---|---|---|
| **A** | Writer sin `SalonLock` | W9 (`InOrder`), C1, C4, C5 |
| **B** | `LocalDate.now()` directo en vez del `Clock` inyectado | **W10** (reloj fijo desalineado del real) |
| **C** | Se permite editar o cancelar una excepción pasada | W1, W5 |
| **D** | Se persiste `CERRADO` con horas | W8 (normalización), P1 (`CHECK` real) |
| **E** | Se acepta `cierre <= apertura` | W8, P1 |
| **F** | `23505` termina sólo en 409 genérico, sin código estable | **P5**, que assertea `codigo`, no el status |
| **G** | Cancelar y recrear falla por el índice único (se intenta INSERT sin respetar el parcial, o se reactiva la vieja) | W7, P3, P4 |
| **H** | La excepción modifica un template recurrente | P7, e I1 verificando que no hay escrituras sobre programación |
| **I** | Un recurrente incompatible bloquea el cierre por festivo | I1 (la consulta de recurrentes no se invoca) |
| **J** | Un turno puntual fuera del horario es permitido | I1, I2, I5 |
| **K** | La API nueva rompe el `DELETE` legacy (mapeo ambiguo o path cambiado) | **A2**, y A1 |
| **L** | Una reserva confirmada queda fuera del nuevo horario y el writer lo permite | I3, I5 |
| **M** | `ReservaService.crear` crea fuera de `HorarioEfectivoSalon` (o valida sólo contra el semanal) | Test de `ReservaService` con excepción activa en la fecha: la reserva que cabría en el semanal es rechazada. Cubierto por A1/C5 en integración y por un unit del propio `ReservaService` |
| **N** | `TurnoInstructor.EXCEPCION` no toma el lock y compite con la excepción | **C4** |

Nota sobre **M**: el unit correspondiente vive en `ReservaServiceTest` (suite existente), no en el
plan nuevo de arriba, porque es cobertura de un writer ya testeado al que F2C.2 añade una regla.

---

## Riesgos

1. **El brief asume un diseño en verde que no lo es.** El riesgo principal de F2C.2 es implementar
   un modelo paralelo y dejar dos representaciones de excepción. Mitigación: este documento cierra
   que **se evoluciona el agregado existente**.
2. **Tres writers cambian a la vez.** F2C.2 toca `SalonHorarioExcepcionService`,
   `TurnoInstructorService` y `ReservaService`. Es irreducible: la invariante es compartida y
   cerrarla en uno solo no cierra nada. Mitigación: el orden de implementación de abajo, y los
   tests de concurrencia C4/C5 como criterio de aceptación.
3. **Añadir `SalonLock` a reservas amplía la superficie de serialización.** Las reservas pasan a
   competir por la fila de `Salon` con los writers de horario. Es el precio de la consistencia y no
   introduce deadlock (lock único). Riesgo real: contención si el volumen de reservas crece; es
   justo el punto que la sección de justificación deja abierto a evolucionar fuera de F2C.
4. **Cerrar el hueco puntual rechaza operaciones que hoy pasan.** Cerrar un día con turnos
   `EXCEPCION` o reservas confirmadas incompatibles hoy funciona y pasará a dar 409. Es la
   corrección de un defecto, pero **debe anunciarse**, no colarse.
5. **"Hoy es atómico" puede sorprender.** Un turno de las 09:00 impide cerrar hoy a las 18:00. Es
   deliberado y determinista; conviene que el mensaje de error sea claro sobre qué estorba.
6. **Excepciones pasadas ya existentes.** Al volver inmutable el pasado, las filas históricas
   quedan congeladas. Es lo deseado, pero datos erróneos en producción sólo serán corregibles por
   intervención manual. Verificar antes de desplegar.
7. **Metadata del constraint en el traductor.** Que pgjdbc/Hibernate reporten el nombre de un
   índice único **parcial** debe verificarse contra el driver real. Si no lo reportan, aplica la
   vía de "evidencia inequívoca", que es más débil. Es la razón de que la regla la contemple.
8. **`Clock.systemDefaultZone()` sin zona de negocio declarada.** Riesgo heredado, ya documentado
   en `RelojConfig`: la frontera "hoy" depende de la zona de la JVM. **Fuera del alcance de F2C**,
   pero es deuda real.

---

## Decisiones cerradas

1. **Agregado/entidad:** `SalonHorarioExcepcion` sobre `salon_horario_excepcion`. **CONSERVADO**,
   sin renombrado.
2. **Replacement vs delta:** **REPLACEMENT** absoluto.
3. **Estados:** exactamente dos, `CERRADO` y `HORARIO_ESPECIAL`, sobre el booleano `cerrado`.
   **No se introduce enum persistido.**
4. **Apertura excepcional de día normalmente cerrado:** **SÍ**, sin exigir semanal previo.
5. **Precedencia:** excepción activa exacta → semanal versionado → `NO_OPERATIVO`. Sin cambios en
   `HorarioEfectivoSalon`.
6. **Pasado:** **INMUTABLE** para crear, modificar y cancelar.
7. **Hoy:** **MUTABLE**, y la **fecha es atómica**: no hay semántica intra-día, no se usa
   `LocalTime.now()`, un objeto puntual incompatible bloquea aunque su hora haya pasado.
8. **Futuro:** **MUTABLE**.
9. **Upsert:** activa+igual → **no-op sin escritura, 200**; activa+distinto → **update destructivo
   de la misma fila**; sólo inactivas → **fila nueva**, nunca reactivación.
10. **Historial de ediciones activas:** **NO EXISTE.** Sólo la cancelación deja rastro.
11. **Cancelación:** soft delete `activo=false`. Cancelar lo inexistente → **404**, no 204.
12. **Recurrentes:** **derivados, nunca modificados.** Cero escrituras sobre `BloqueProgramacion`,
    `Asignacion` y `TurnoInstructor` RECURRENTE, y nunca bloquean.
13. **Ocurrencia recurrente parcialmente fuera:** **OMITIDA, NO RECORTADA.**
14. **Conteo/warning de impacto recurrente:** **eliminado del alcance.**
15. **Objetos puntuales que bloquean:** `TurnoInstructor.EXCEPCION` activo y `Reserva.CONFIRMADA`.
    `CANCELACION` y `Reserva.CANCELADA` no bloquean. `BloqueProgramacion` no tiene variante puntual.
16. **Port neutral:** **CERRADO.** `ValidadorImpactoExcepcionHorario.evaluar(CambioExcepcionHorario)
    → List<ConflictoProgramacionPuntual>`. Lista, no boolean. `CambioExcepcionHorario.admite` exige
    **contención completa**, no solape.
17. **Adapter:** **uno**, del lado de `calendario`, consultando `buscarExcepcionesPorSalonYFecha` y
    `findBySalonIdAndFechaAndEstado(CONFIRMADA)`.
18. **Lock:** `SalonLock` único, adquirido tras autorizar y **antes** de leer, en los **tres**
    writers: excepción, `TurnoInstructor.EXCEPCION` y `ReservaService.crear`.
19. **`ReservaService` valida `HorarioEfectivoSalon`:** **SÍ**, cobertura completa, no sólo semanal.
20. **`23505`:** `saveAndFlush` dentro del traductor, clasificación estricta por SQLSTATE +
    constraint, resto **relanzado**. El defecto corregido es la ausencia de código estable, no un 500.
21. **API legacy:** **CONSERVADA** íntegra en F2C.2; retiro en fase separada posterior.
22. **API por fecha:** `PUT`/`DELETE .../excepciones-horario/por-fecha/{fecha}`; fecha del path
    autoritativa; convergencia con legacy en un mismo caso de uso.
23. **Orden de autorización del `DELETE` legacy:** **CORREGIDO EN DISEÑO** — autorizar antes de leer.
24. **Errores:** seis códigos, uno reutilizado; whitelist 409 cerrada de dos; sobre
    `ErrorResponse.codigo` existente.
25. **Migraciones:** **NINGUNA.** V47 libre. Si aparece necesidad real de DDL: detener y reabrir.
26. **Tests:** 31 métodos en cinco grupos, con parametrizados; catálogo de mutaciones A–N cubierto.

**Decisiones abiertas: NINGUNA.**

---

## Alcance F2C.2

Implementación, en este orden:

1. **`SalonHorarioExcepcionErrores`** con el catálogo de seis códigos (reutilizando
   `HORA_CIERRE_DEBE_SER_POSTERIOR`) y la whitelist 409 cerrada.
2. **Port neutral** en `ubicaciones/dominio`: `CambioExcepcionHorario`,
   `ConflictoProgramacionPuntual`, `ValidadorImpactoExcepcionHorario`.
3. **Adapter** en `calendario`: `TurnoInstructor.EXCEPCION` + `Reserva.CONFIRMADA`.
4. **Endurecer `SalonHorarioExcepcionService`**: `Clock`, `SalonLock` tras autorizar y antes de
   leer, temporalidad pasado/hoy/futuro, upsert con no-op / update / recreación, cancelación.
5. **Traductor estricto de `23505`** con `saveAndFlush` dentro del bloque.
6. **API coexistente**: legacy intacta + `por-fecha`, convergiendo en el mismo caso de uso; y el
   orden de autorización del `DELETE` legacy corregido.
7. **`SalonLock` en `TurnoInstructorService.crear(EXCEPCION)`**, sin tocar el camino `RECURRENTE`.
8. **`SalonLock` + validación de `HorarioEfectivoSalon` en `ReservaService.crear`.**
9. **Tests**: unit, integration PostgreSQL, concurrency y API.
10. **Javadocs afectados**: `ImpactoTurnosRecurrentesEnHorario` (el hueco que documenta deja de
    existir) y el comentario de `TurnoInstructorService.crear` que justifica excluir `EXCEPCION`
    del lock.

Explícitamente **FUERA** de F2C.2:

- **migraciones** (V47 sigue libre);
- **frontend**;
- **mobile**;
- **sesiones** y su modelo de estado;
- **materialización** y rematerialización;
- **recursos y capacidad**;
- **modificación de templates recurrentes**, en cualquier forma;
- **recorte de ocurrencias** recurrentes;
- **conteos o warnings informativos de impacto**;
- **cambios en `HorarioEfectivoSalon`**;
- retiro de los endpoints legacy;
- zona horaria de negocio en `RelojConfig`.

### Puntos de extensión futuros (documentados, no implementados)

- **Sesiones materializadas (§23).** Cuando existan, la regla de derivación de este documento
  ("contenida → intacta; parcial o fuera → omitida") es el contrato que la materialización debe
  respetar. El port `ValidadorImpactoExcepcionHorario` ya es el punto de enganche.
- **Granularidad de locks en reservas.** Con `Sesion`, capacidad y recursos, el `SalonLock` podrá
  sustituirse por una granularidad más fina. Hoy es la decisión correcta para el modelo legacy.
- **Semántica intra-día.** Si alguna vez se necesita "cerrar desde las 15:00", requiere un modelo de
  sesiones con estado, no una regla basada en `LocalTime.now()`.
- **Rematerialización (§24).** Si una excepción debe dispararla, se hará por el mismo port síncrono
  dentro de la transacción del writer. **No se introduce broker ni outbox por F2C.**
- **Frontend (§22).** La UX futura debe ofrecer "Excepción para una fecha" con las dos acciones
  ("Cerrar este día" / "Usar horario especial") **sin editar el template semanal**. El contrato
  `PUT /por-fecha/{fecha}` es exactamente esa operación.
