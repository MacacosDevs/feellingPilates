# FeelingPilates — Fase 2A: expand de horario operativo y políticas de salón

Fecha: 2026-08-22

Branch base: `programacion/modelo-base-query-binding-fix`

Commit base: `f1337cf058d53a589b745f046a99daf8793ea494`

Branch de trabajo: `operacion/horario-vigencia-politicas`

Alcance: infraestructura ADITIVA para vigencia temporal futura de
`HorarioOperacion` y políticas de confirmación/reserva/materialización por
salón. F2A es EXPAND, no SWITCH: no habilita versionado múltiple de horarios,
no conecta confirmación, no conecta ventana de reserva, no materializa
sesiones.

## Pre-flight

- Branch: `programacion/modelo-base-query-binding-fix`.
- HEAD: `f1337cf058d53a589b745f046a99daf8793ea494`.
- Working tree inicial: limpio.
- `git branch -vv`: sin divergencia con `origin/programacion/modelo-base-query-binding-fix`.
- Se creó `operacion/horario-vigencia-politicas` desde ese commit exacto.
- No se tocó `main`.

## Inventario previo de consumidores (obligatorio, paso 4)

Búsqueda exhaustiva de `HorarioOperacion`, `HorarioOperacionRepository` y
`horario_operacion` antes de modificar código:

- `HorarioOperacionRepository` (`ubicaciones/repositorio`): `JpaRepository`
  con `findBySalonIdOrderByDiaSemana` y `deleteBySalonId`. Sin queries nativas
  que asuman una sola fila por salón/día.
- `SalonService` (`ubicaciones/servicio`): crea/reemplaza horarios en
  `reemplazarHorarios` (borra todos los del salón y reinserta desde
  `SalonRequest.horarios()`); lee horarios en `mapDetalle` para armar
  `SalonDetalleResponse`. Es el único punto de escritura/lectura vía HTTP.
- `HorarioOperacionRequest` / `HorarioOperacionResponse` (`ubicaciones/dto`):
  DTOs explícitos, **no** serializan la entidad directamente. No incluyen
  `vigenteDesde`/`vigenteHasta`.
- `BloqueProgramacionService.validarDentroDelHorarioOperacion`
  (`programacion/servicio`): consulta `findBySalonIdOrderByDiaSemana`, filtra
  por `diaSemana` con `stream().filter(...).anyMatch(...)`. No asume una sola
  fila; itera la lista completa. No se tocó.
- `TurnoInstructorService` (`calendario/servicio`), método de validación de
  horario (~línea 332): mismo patrón `stream().filter().anyMatch()` sobre
  `findBySalonIdOrderByDiaSemana`. No se tocó.
- Tests que referencian `HorarioOperacion`/`horario_operacion`:
  `TurnoInstructorServiceCaracterizacionTest`,
  `AutorizacionContextualControllerTest`, `BloqueProgramacionServiceTest`
  (mocks de `HorarioOperacionRepository`, no de PostgreSQL real). No se
  tocaron.

Ningún consumidor productivo asume explícitamente `UNIQUE(salon_id,
dia_semana)` en código Java; la unicidad la garantiza únicamente la
restricción de base de datos creada en `V11__salones_gestion.sql`. Esa
restricción **no se tocó**.

Contrato HTTP: `SalonDetalleResponse`/`SalonRequest`/
`HorarioOperacionResponse`/`HorarioOperacionRequest` son DTOs manuales
mapeados a mano en `SalonService`. Los campos nuevos en las entidades
`Salon`/`HorarioOperacion` **no se filtran** a JSON. No fue necesario tocar
DTOs ni controladores.

## V42 — políticas de confirmación/reserva/materialización del salón

Archivo: `src/main/resources/db/migration/V42__salon_politicas_programacion.sql`

Columnas agregadas a `salon` (todas aditivas):

| columna | tipo | default/nullability |
|---|---|---|
| `requiere_confirmacion_instructor` | `BOOLEAN` | `NOT NULL DEFAULT FALSE` |
| `plazo_respuesta_confirmacion_horas` | `SMALLINT` | `NULL` |
| `anticipacion_maxima_reserva_horas` | `SMALLINT` | `NULL` |
| `anticipacion_minima_reserva_horas` | `SMALLINT` | `NOT NULL DEFAULT 0` |
| `margen_materializacion_dias` | `SMALLINT` | `NULL` |

Se usó `SMALLINT` en vez de `INTEGER` porque es la convención real del
proyecto para campos numéricos acotados (`dia_semana`, `estado_id`,
`municipio_id`, `capacidad` en `espacio`), y el mapeo Java correspondiente
(`Short`) ya es el patrón existente en `HorarioOperacion.diaSemana` y
`Salon.estadoId`/`municipioId`. No introduce conversiones incómodas.

Checks agregados:

- `anticipacion_minima_reserva_horas >= 0`
- `anticipacion_maxima_reserva_horas IS NULL OR anticipacion_maxima_reserva_horas > 0`
- `plazo_respuesta_confirmacion_horas IS NULL OR plazo_respuesta_confirmacion_horas >= 0`
- `margen_materializacion_dias IS NULL OR margen_materializacion_dias >= 0`
- `NOT requiere_confirmacion_instructor OR plazo_respuesta_confirmacion_horas IS NOT NULL`
  (coherente y no rompe estado transicional: el default de
  `requiere_confirmacion_instructor` es `FALSE`, así que ningún salón
  existente puede violarlo al aplicar la migración).

No se creó entidad `SalonPolitica` ni tabla adicional; las políticas se
agregaron directamente a la entidad `Salon` (`src/main/java/.../ubicaciones/entidad/Salon.java`)
como columnas simples, sin VO de lectura (`PoliticaProgramacionSalon`) porque
no existe consumidor todavía.

## V43 — vigencia de `horario_operacion`

Archivo: `src/main/resources/db/migration/V43__horario_operacion_vigencia.sql`

Columnas agregadas a `horario_operacion`:

- `vigente_desde DATE` (nullable)
- `vigente_hasta DATE` (nullable)

Semántica transicional: filas existentes/nuevas sin especificar estas
columnas quedan `NULL`/`NULL`, lo que significa "vigencia histórica
abierta / horario legado sin límites temporales". No se inventó fecha de
inicio para datos existentes.

Check agregado:

```sql
CHECK (vigente_hasta IS NULL OR vigente_desde IS NULL OR vigente_hasta >= vigente_desde)
```

Permite `vigenteDesde = NULL` durante la transición; no impone todavía
`vigente_desde NOT NULL`.

Entidad `HorarioOperacion`
(`src/main/java/.../ubicaciones/entidad/HorarioOperacion.java`): se agregaron
`LocalDate vigenteDesde` y `LocalDate vigenteHasta`, nullable, sin tocar
ownership, endpoints ni DTOs públicos.

## UNIQUE(salon_id, dia_semana)

**CONSERVADO.** No se tocó `V11__salones_gestion.sql`. No se creó `EXCLUDE`,
`btree_gist` ni `daterange constraint`. Test
`uniqueSalonDiaSemanaSigueVigenteEnF2A` en `UbicacionesPersistenciaTest`
demuestra contra PostgreSQL real que insertar dos `horario_operacion` para el
mismo `salon_id` + `dia_semana` sigue fallando por la restricción existente.

## Semántica de vigencia transicional (documental, no consumida)

Una fecha `F` está contenida en la vigencia de un `HorarioOperacion` cuando:

```
(vigenteDesde == null || F >= vigenteDesde)
AND
(vigenteHasta == null || F <= vigenteHasta)
```

No se implementó ningún resolver ni se cambió ningún consumidor a esta
resolución temporal. Queda documentada para F2B.

## P2 arrastrado de 1C: dirección simétrica de vigencias

Se agregaron dos tests a `ProgramacionPersistenciaTest`:

- `asignacionNoDetectaConflictoCuandoVigenciaExistenteEsPosteriorAlaConsulta`
- `bloqueNoDetectaTraslapeCuandoVigenciaExistenteEsPosteriorAlaConsulta`

Caso probado: fila existente con `vigente_desde = 2026-03-01`,
`vigente_hasta = NULL`; consulta con `vigenteDesde = 2026-01-01`,
`vigenteHasta = 2026-02-28`. Mismo horario/día (para que sólo la vigencia
pueda excluir el resultado). Resultado esperado y obtenido: **sin
intersección** (`isEmpty()`), confirmando que la condición
`cast(:vigenteHasta as date) is null or a.vigente_desde <= :vigenteHasta`
realmente excluye la fila cuando corresponde.

Resultado: **2/2 PASS**. No se encontró bug previo — no fue necesario
detenerse. No se modificaron las queries de `AsignacionRepository` ni
`BloqueProgramacionRepository`.

## Tests de migración

`UbicacionesPersistenciaTest` (nuevo, PostgreSQL real vía Testcontainers):

- `jpaValidaYRegistraLasEntidadesDeUbicaciones`: Hibernate reconoce `Salon` y
  `HorarioOperacion` actualizadas.
- `v42AgregaPoliticasDeSalonConValoresTransicionales`: un salón de la
  semilla (`V10`) tiene `requiere_confirmacion_instructor = false`,
  `anticipacion_minima_reserva_horas = 0`, y el resto (`plazo_respuesta_...`,
  `anticipacion_maxima_...`, `margen_materializacion_dias`) en `NULL`.
- `v42RechazaConfirmacionRequeridaSinPlazoDeRespuesta`: activar
  `requiere_confirmacion_instructor` sin `plazo_respuesta_confirmacion_horas`
  falla por el check.
- `v43AgregaVigenciaAHorarioOperacionYPreservaFilasLegadas`: insertar un
  horario "legado" (sin especificar vigencia) preserva exactamente una fila
  y ambas columnas de vigencia quedan `NULL`.
- `uniqueSalonDiaSemanaSigueVigenteEnF2A`: ver sección UNIQUE arriba.
- `checkVigenciaRechazaHastaAnteriorADesde`: `vigenteDesde = 2026-02-01`,
  `vigenteHasta = 2026-01-31` → rechazado.
- `checkVigenciaPermiteCombinacionesConLimitesAbiertos`: `NULL/NULL`,
  `NULL/fecha`, `fecha/NULL` → las tres permitidas.

Resultado: **7/7 PASS**.

No hubo backfill destructivo ni `DELETE` en ninguna migración ni test.

## HTTP compatibility (paso 25)

Revisados `SalonController`/`SalonRequest`/`SalonDetalleResponse`/
`HorarioOperacionRequest`/`HorarioOperacionResponse`. Son DTOs explícitos
construidos a mano en `SalonService`, no serializan la entidad directamente.
Los campos nuevos de `Salon` y `HorarioOperacion` **no se exponen** en
ninguna respuesta HTTP existente. No fue necesario ningún cambio en
DTOs/controladores para preservar compatibilidad — ya estaban aislados de la
entidad. **Resultado: contrato HTTP sin cambios, confirmado por inspección de
código, no fue necesario detenerse.**

## No implementado (por diseño de F2A)

- Confirmación de instructor (`AsignacionFecha`, `ConfirmacionInstructor`,
  worker, deadline, `ExpirarConfirmaciones`): no creado.
- Ventana de reserva: `ReservaService`/`ReservaController`/disponibilidad sin
  cambios.
- Materialización: `Sesion`, jobs, scheduler: no creado.
- `ResolverProgramacionFecha`: no implementado.
- Versionado múltiple de horarios por salón/día: no habilitado.
- `SalonPolitica` como entidad/tabla separada: no creada (las políticas
  viven en `salon`, según preferencia de modelo simple del encargo).

## Resultados

- V42: **PASS**
- V43: **PASS**
- Salon políticas: **IMPLEMENTADAS**
- Horario vigencia: **IMPLEMENTADA**
- UNIQUE salon+dia: **CONSERVADO**
- Versionado múltiple: **NO HABILITADO**
- Confirmación: **NO IMPLEMENTADA**
- Ventana reserva: **NO CONSUMIDA**
- Calendario legado: **SIN CAMBIOS**
- Reserva: **SIN CAMBIOS**
- Programación productivo: **SIN CAMBIOS**
- P2 vigencia simétrica 1C: **CUBIERTO**
- Tests: **129/129 PASS** (120 previos + 9 nuevos: 2 de P2 en
  `ProgramacionPersistenciaTest`, 7 en `UbicacionesPersistenciaTest`)
- Build (`./mvnw clean compile`): **PASS**
- Flyway V1→V43: **PASS** (46 migraciones aplicadas, versión final `43`,
  verificado en `flywayMigraDesdeV1HastaV43`)
- JPA (`ddl-auto=validate`): **PASS**

## Diff final

```text
src/main/resources/db/migration/V42__salon_politicas_programacion.sql
src/main/resources/db/migration/V43__horario_operacion_vigencia.sql
src/main/java/com/feelingpilates/ubicaciones/entidad/Salon.java
src/main/java/com/feelingpilates/ubicaciones/entidad/HorarioOperacion.java
src/test/java/com/feelingpilates/ubicaciones/UbicacionesPersistenciaTest.java
src/test/java/com/feelingpilates/programacion/ProgramacionPersistenciaTest.java
auditoria/fase-2a-horario-vigencia-politicas.md
```

`ProgramacionPersistenciaTest.java` se tocó únicamente para: (1) actualizar
`flywayMigraDesdeV1HastaV41` → `flywayMigraDesdeV1HastaV43` reflejando el
nuevo estado final de Flyway (versión y conteo de migraciones aplicadas), y
(2) agregar los dos tests P2 de dirección simétrica de vigencia. No se tocó
ninguna query ni el servicio de programación productivo.

Sin cambios en: calendario, `Reserva`, pagos, `BloqueProgramacionService`,
`AsignacionRepository` (queries), `BloqueProgramacionRepository` (queries),
`BloqueProgramacion`, `Asignacion`.

## F2B

**LISTA.** F2A deja la infraestructura de datos (columnas + checks +
semántica transicional documentada) sin activar ningún comportamiento nuevo.
F2B puede: reemplazar conscientemente el UNIQUE actual por versionado real
(`EXCLUDE`/`daterange` o equivalente), implementar `ResolverProgramacionFecha`
usando la semántica de contención de fecha ya documentada, y decidir
conscientemente los valores de negocio (anticipación, plazos, márgenes) que
F2A dejó explícitamente sin inventar.
