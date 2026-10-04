# FeelingPilates — Fase 1C: modelo base de Programación

Fecha: 2026-08-22

Branch base: `programacion/safety-net-calendario-1a1`

Commit base: `a7fed2dad7702eb0773d38896e84ba33548e936e`

Branch de trabajo: `programacion/modelo-base`

## Pre-flight

- La branch base y el commit coincidieron con el checkpoint 1B.1.
- El working tree estaba limpio.
- La branch base seguía a `origin/programacion/safety-net-calendario-1a1` en el mismo commit.
- `programacion/modelo-base` no existía y se creó desde el commit base.
- No se tocó `main`.

## Tablas creadas — V41

`V41__programacion_bloque_asignacion.sql` es expand puro y crea dos tablas vacías:

- `programacion_bloque`: UUID físico propio, `serie_id`, salón, día semanal, rango horario obligatorio, vigencia inclusiva, estado activo y timestamps.
- `programacion_asignacion`: UUID físico propio, `serie_id`, bloque, instructor, exactamente un `tipo_actividad_id`, rango horario obligatorio, vigencia inclusiva, estado activo y timestamps.

Se agregaron FKs normales e índices para salón/día/vigencia, serie, bloque e instructor/vigencia. Los CHECK protegen día `0..6`, `hora_fin > hora_inicio` y vigencia final no anterior a la inicial.

V41 no contiene `ALTER`, backfill, mutación de datos, `DROP`, `btree_gist`, `EXCLUDE`, tipos range ni columnas range generadas. Las 43 migraciones anteriores permanecen intactas.

## Clases incorporadas

- `BloqueProgramacion` y `Asignacion`, ambas entidades JPA con UUID propio mediante `EntidadBase` y referencias cruzadas como UUID, sin crear un grafo JPA global.
- `BloqueProgramacionRepository` y `AsignacionRepository`, con las consultas mínimas para salón/día, asignaciones del bloque, traslape con vigencia y conflicto recurrente global del instructor.
- `BloqueProgramacionService`, con los casos de uso `crearBloque` y `crearAsignacion` y dos commands internos acotados.

No se creó ningún controller, endpoint o DTO HTTP.

## Invariantes protegidas

### BloqueProgramacion

- Día semanal entre 0 y 6.
- Rango obligatorio con semántica `[inicio, fin)` y fin posterior al inicio.
- Vigencia inclusiva `[desde, hasta]`, con `hasta = null` como infinito.
- Contención completa en el `HorarioOperacion` actual del salón/día; apertura y cierre exactos son válidos.
- No traslape de bloques activos del mismo salón/día cuando también se intersectan sus vigencias.
- Los rangos contiguos se permiten y una intersección positiva se rechaza.

### Asignacion

- Bloque existente y activo.
- Instructor existente, con estado activo y rol `INSTRUCTOR` global o aplicable al salón.
- Tipo de actividad existente y activo.
- Actividad ofrecida por el salón mediante `salon_tipo_actividad`.
- Especialidad del instructor mediante `instructor_actividad`.
- Rango obligatorio contenido completamente en el bloque.
- Vigencia contenida completamente en la vigencia del bloque.
- Una fila contiene exactamente una actividad por construcción: un único UUID `tipoActividadId`, NOT NULL en V41.
- El mismo instructor puede ocupar segmentos disjuntos o contiguos, incluso repitiendo actividad, pero no segmentos físicamente traslapados con vigencias intersectadas.
- Instructores distintos pueden compartir hora y actividad; no existe exclusividad de salón ni actividad.
- Precheck recurrente global contra todos los salones: mismo instructor + mismo día + horas traslapadas + vigencias intersectadas produce rechazo.

### Identidad de versión

- `id` identifica la fila física de esta versión.
- `serieId` identifica la regla lógica y puede compartirse entre filas con ids distintos.
- No se crearon `origenId` ni `versionAnteriorId`.

## Tests

Se agregaron 37 tests:

- 33 tests unitarios de `BloqueProgramacionService`: horario operativo y bordes, rangos y vigencias, contigüidad y traslape, elegibilidad, actividad única, segmentos disjuntos/contiguos, múltiples instructores, conflicto global y semántica de `serieId`.
- 4 tests integrados con PostgreSQL 16: Flyway V1→V41, tablas nuevas vacías, validación JPA y ejecución real de las consultas de traslape/vigencia/día.

Los 70 tests legados permanecen sin cambios y pasaron. Resultado total: **107/107 PASS**, 0 failures, 0 errors, 0 skipped.

## Flyway y JPA

- PostgreSQL efímero: **PASS**.
- Flyway validó y aplicó 44 migraciones (43 anteriores + V41) desde esquema vacío: **PASS**, versión final 41.
- Hibernate con `ddl-auto=validate` registró ambas entidades y validó V41: **PASS**.
- Las consultas nativas de repositorio se ejecutaron contra PostgreSQL y comprobaron `[inicio, fin)`, vigencia y día semanal: **PASS**.

## Decisiones que no se implementaron

- No se conectó el modelo a calendario legado, reservas, sesiones, confirmaciones, ajustes por fecha ni disponibilidad móvil.
- No se implementó edición/versionado complejo; sólo pueden insertarse filas/versiones con el `serieId` indicado.
- No se agregaron advisory locks, `EXCLUDE` ni garantía concurrente absoluta. El precheck explica el conflicto, pero la protección dura queda **PENDIENTE F5** sobre `AsignacionFecha`.
- No se agregaron controllers, endpoints, backfill, workers ni dependencias.
- No se agregó ArchUnit porque no está disponible y 1C prohíbe modificar `pom.xml`.

## Validación final

- `./mvnw clean compile`: **BUILD SUCCESS**.
- `./mvnw test`: **107/107 PASS**.
- `git diff -- src/main/java/com/feelingpilates/calendario`: vacío.
- Reserva (`Reserva`, service, repository y controller): sin cambios.
- Esquema y entidades existentes de Operación: sin cambios; Programación sólo los lee.
- Diff limitado a `programacion/**`, V41, tests de Programación y este checkpoint.

## Resultado

V41:

PASS

BloqueProgramacion:

IMPLEMENTADO

Asignacion:

IMPLEMENTADA

serieId:

IMPLEMENTADO

Actividad única:

PROTEGIDA

Conflicto global recurrente:

PROTEGIDO

Endpoints:

NINGUNO

Calendario legado:

SIN CAMBIOS

Reserva:

SIN CAMBIOS

Tests legado:

70/70 PASS

Tests totales:

107/107 PASS

Build:

PASS

Flyway V41:

PASS

JPA:

PASS

Concurrencia dura:

PENDIENTE F5

Commit:

el commit `feat: introducir modelo base de programacion` que contiene este checkpoint, reportado por hash al finalizar la fase
