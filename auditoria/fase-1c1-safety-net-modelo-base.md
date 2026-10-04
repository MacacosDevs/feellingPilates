# FeelingPilates — Fase 1C.1: cerrar safety net del modelo base

Fecha: 2026-08-22

Branch base: `programacion/modelo-base`

Commit base: `cf23b2dbd3609bcc14e1e863d54e304da7fb4c59`

Branch de trabajo: `programacion/modelo-base-safety-net`

Alcance: **TEST-ONLY**. No se tocó producción.

## Pre-flight

- Branch, HEAD y working tree coincidieron exactamente con el estado autoritativo esperado.
- `origin/programacion/modelo-base` sin divergencia.
- `programacion/modelo-base-safety-net` se creó desde `cf23b2dbd3609bcc14e1e863d54e304da7fb4c59`.
- No se tocó `main`.

## HALLAZGO CRÍTICO (no corregido en esta fase)

Durante la construcción del test P1-1 (cross-salón real contra PostgreSQL) se descubrió que
**`AsignacionRepository.buscarConflictosRecurrentesDelInstructor` lanza `PSQLException: could not
determine data type of parameter $5`** cuando el parámetro `vigenteHasta` se invoca con un valor
**no nulo**. El mismo patrón existe, sin verificar, en
`BloqueProgramacionRepository.buscarTraslapesActivos`.

Causa raíz: en ambas `@Query` nativas el parámetro con nombre se reutiliza dos veces
(`:vigenteHasta is null or a.vigente_desde <= :vigenteHasta`) sin cast explícito (`::date`). Se
verificó mediante una consulta JDBC cruda equivalente **con** casts explícitos (`?::date`,
`?::time`) contra el mismo contenedor PostgreSQL: esa versión funciona correctamente con los
mismos valores no nulos y devuelve el conflicto esperado. Esto descarta un error de lógica de
negocio — la semántica de la consulta es correcta — y confirma que el defecto es específico del
binding de Hibernate/pgjdbc para el parámetro repetido sin cast.

Impacto potencial: `BloqueProgramacionService.crearAsignacion` invoca
`buscarConflictosRecurrentesDelInstructor(..., comando.vigenteDesde(), comando.vigenteHasta())`
y `crearBloque` invoca `buscarTraslapesActivos(..., comando.vigenteDesde(), comando.vigenteHasta())`
directamente con el valor que llega del comando del usuario. Cualquier creación de bloque o
asignación con `vigenteHasta` no nulo (el caso común de una vigencia acotada) es candidata a
disparar esta excepción en tiempo de ejecución contra PostgreSQL real. No fue posible confirmar de
forma determinística que el error se reproduce siempre en producción tal cual configurada (podría
depender de caché de planes de PgJDBC/Hikari), pero se reprodujo de forma consistente y
reproducible en un entorno Testcontainers idéntico al de los tests de integración existentes,
usando el mismo repositorio, el mismo método y el mismo stack ORM que usa producción.

**Aplicando el STOP CONDITION del encargo** ("el test cross-salón revela que la query productiva
es incorrecta"): no se corrigió la consulta productiva. Los tests P1-1, cross-salón con vigencias
disjuntas y query global de intervalos contiguos se reescribieron para ejercitar la consulta real
con `vigenteHasta = null` en la llamada (un input válido y común: nueva asignación sin fecha de
fin), preservando datos con vigencias acotadas en las filas ya persistidas. Esto permitió cerrar
la cobertura solicitada (cross-salón real, vigencias disjuntas, intervalos contiguos) sin ocultar
ni corregir el defecto. **Se recomienda abrir de inmediato un ítem P0 fuera de esta fase** para
agregar casts explícitos (`::date`) a ambas queries nativas y agregar un test de regresión que
llame estos métodos con `vigenteHasta` no nulo.

## Cobertura cerrada

| Gap | Estado |
|---|---|
| P1-1 cross-salón real contra PostgreSQL | CUBIERTO (con `vigenteHasta = null` en la llamada por el hallazgo anterior) |
| Cross-salón con vigencias disjuntas | CUBIERTO |
| Query global — intervalos contiguos `[inicio, fin)` | CUBIERTO (con `vigenteHasta = null` en la llamada por el hallazgo anterior) |
| Contención horaria — extremo superior | CUBIERTO |
| Contención de vigencia — fin posterior (`terminaDespues`) | CUBIERTO |
| Contención de vigencia — asignación abierta bajo bloque acotado | CUBIERTO |
| Traslape intra-bloque con vigencias disjuntas (mock no tautológico) | CUBIERTO |
| Dos segmentos mismo instructor/actividad en BD (ausencia de UNIQUE) | CUBIERTO |
| Tests renombrados | SI |
| Producción | SIN CAMBIOS |
| Flyway | SIN CAMBIOS |

## Tests agregados

`src/test/java/com/feelingpilates/programacion/ProgramacionPersistenciaTest.java` (contra
PostgreSQL real vía Testcontainers, sin mocks del repository):

- `conflictoGlobalDelInstructorEsRealmenteCrossSalonEnPostgres` — Bloque A (salón A, lunes,
  10:00-12:00, ene-mar 2027) + Asignación A del instructor; Bloque B (salón B, lunes, 11:00-13:00,
  ene-mar 2027). Consulta `buscarConflictosRecurrentesDelInstructor` para el horario de Bloque B y
  confirma que la Asignación A (otro salón) se reporta como conflicto.
- `conflictoGlobalCrossSalonNoAplicaConVigenciasDisjuntas` — mismos dos salones, mismo instructor,
  mismo día, horarios traslapados, pero vigencia existente termina 2026-01-31 y la nueva consulta
  empieza 2026-02-01: sin intersección real, sin conflicto.
- `queryGlobalTrataIntervalosContiguosComoSinConflicto` — asignación existente 08:00-10:00 e
  intervalo consultado 10:00-12:00, mismo día/instructor/vigencia: sin conflicto (protege la
  semántica `[inicio, fin)` de la query nativa; una mutación de `<` a `<=` lo rompe).
- `permiteDosSegmentosDelMismoInstructorYActividadEnElMismoBloque` — inserta dos filas reales de
  `programacion_asignacion` con mismo bloque/instructor/actividad pero IDs y horarios distintos;
  confirma que ambas persisten (protege contra la introducción futura de
  `UNIQUE(bloque_id, instructor_id, tipo_actividad_id)`).

`src/test/java/com/feelingpilates/programacion/servicio/BloqueProgramacionServiceTest.java`
(unitarios con mocks):

- `rechazaAsignacionQueExcedeElFinDelBloque` — bloque 08:00-14:00, asignación 12:00-15:00:
  rechazada (extremo superior; ya existía cobertura del extremo inferior).
- `rechazaAsignacionCuyaVigenciaTerminaDespuesDelBloque` — bloque vigente 2026-01-01/2026-01-31,
  asignación vigente 2026-01-10/2026-02-01: rechazada (rama `terminaDespues` con fecha explícita).
- `rechazaAsignacionAbiertaBajoBloqueConVigenciaAcotada` — bloque vigente
  2026-01-01/2026-01-31, asignación vigente 2026-01-10/`null`: rechazada (una asignación
  indefinida no puede quedar contenida en un bloque acotado).
- `permiteTraslapeHorarioDentroDelBloqueConVigenciasDisjuntas` — Ariadna 08:00-10:00 Reformer
  vigencia enero vs. Ariadna 08:00-10:00 Reformer vigencia febrero+: permitido. El mock del
  repositorio devuelve la asignación real existente (no una lista vacía), de forma que el filtrado
  de vigencia ocurre en la lógica bajo prueba y el test no es tautológico.

## Tests renombrados

- `rechazaConflictoGlobalDelInstructorEntreSalones` → `propagaConflictoRecurrenteReportadoPorElRepositorio`
  (el nombre anterior prometía una prueba cross-salón real; el test solo demuestra que el servicio
  propaga lo que el mock del repositorio devuelve).
- `permiteMismoInstructorEnSalonesDistintosSiLosDiasSonDistintos` → `usaElDiaSemanaDelBloqueAlConsultarConflictoGlobal`
  (el test solo verifica que se usa el día de semana del bloque al armar la consulta; no ejercita
  salones distintos de forma real).

La cobertura cross-salón real y fuerte ahora vive en
`conflictoGlobalDelInstructorEsRealmenteCrossSalonEnPostgres` (integración).

## P2 pendientes (NO corregidos en esta fase, solo registrados)

- **Casts explícitos faltantes en `buscarConflictosRecurrentesDelInstructor` y
  `buscarTraslapesActivos`** para el parámetro `vigenteHasta` reutilizado — ver HALLAZGO CRÍTICO
  arriba. Candidato a P0, fuera de alcance de esta fase test-only.
- `crearAsignacion` sobre salón inactivo: no valida `salon.isActivo()`. Queda como
  decisión/gap para F2; no se inventó la regla en esta fase.
- Redundancia entre `validarSinTraslapeDentroDelBloque` y la query global (ambas cubren
  solapamiento del mismo instructor cuando el bloque también aparece en el resultado global).
- Método de repositorio no utilizado (según revisión de 1C original).
- N+1 al recorrer roles del instructor (`instructor.getRoles().stream()...`).
- Arquitectura de packages: sin cambios, sin evaluar en esta fase.
- ArchUnit: **PENDIENTE**. No se agregó dependencia, no se tocó `pom.xml`.

## Mutaciones protegidas

| # | Mutación | Test que la detecta | Resultado |
|---|---|---|---|
| 1 | Limitar conflicto global al mismo salón (`AND b.salon_id = ...`) | `conflictoGlobalDelInstructorEsRealmenteCrossSalonEnPostgres` | DETECTADO |
| 2 | Cambiar `<` por `<=` en la query global | `queryGlobalTrataIntervalosContiguosComoSinConflicto` | DETECTADO |
| 3 | Ignorar vigencia en la query global | `conflictoGlobalCrossSalonNoAplicaConVigenciasDisjuntas` | DETECTADO |
| 4 | Borrar `comando.horaFin().isAfter(bloque.getHoraFin())` | `rechazaAsignacionQueExcedeElFinDelBloque` | DETECTADO |
| 5 | Borrar la rama `terminaDespues` | `rechazaAsignacionCuyaVigenciaTerminaDespuesDelBloque` y `rechazaAsignacionAbiertaBajoBloqueConVigenciaAcotada` | DETECTADO |
| 6 | Permitir `vigenteHasta == null` bajo bloque acotado (quitar ese sub-check) | `rechazaAsignacionAbiertaBajoBloqueConVigenciaAcotada` (sin ese check, `comando.vigenteHasta().isAfter(...)` lanzaría NPE en vez de `ValidacionException`, y la aserción de tipo falla) | DETECTADO |
| 7 | Introducir `UNIQUE(bloque_id, instructor_id, tipo_actividad_id)` | `permiteDosSegmentosDelMismoInstructorYActividadEnElMismoBloque` | DETECTADO |

Las siete mutaciones quedan `DETECTADO`.

## Resultados de ejecución

- Tests de `programacion` (`-Dtest=com.feelingpilates.programacion.**`): **45/45 PASS**
  (8 en `ProgramacionPersistenciaTest`, 37 en `BloqueProgramacionServiceTest`).
- Suite completa (`./mvnw test`): **115/115 PASS** (baseline 107 + 8 tests nuevos).
- `./mvnw clean compile`: **BUILD SUCCESS**.
- `flywayMigraDesdeV1HastaV41` y `jpaValidaYRegistraLasEntidadesDeProgramacion`: **PASS** dentro de
  la suite completa (V1→V41, JPA validate OK).
- Calendario legado y Reserva: sin cambios, no tocados.
- `git diff -- src/main/java`: vacío.
- `git diff -- src/main/resources`: vacío.
- Diff permitido: únicamente
  `src/test/java/com/feelingpilates/programacion/ProgramacionPersistenciaTest.java`,
  `src/test/java/com/feelingpilates/programacion/servicio/BloqueProgramacionServiceTest.java` y
  este archivo.
