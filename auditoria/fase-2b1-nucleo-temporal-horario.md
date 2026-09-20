# FeelingPilates — Fase 2B.1: Núcleo temporal de Horario Operativo

Checkpoint de implementación. Alcance: núcleo temporal aditivo para resolver
`HorarioOperacion` por fecha, sin cambiar comportamiento observable, sin tocar el UNIQUE,
sin consumers, sin writers, sin migraciones. Diseño base: F2B.0.1
(`auditoria/fase-2b-diseno-versionado-horario.md`).

Base:
`c07b1d00934b3ba6492f102fca910cf6e421f781` (docs: consolidar diseño de versionado de
horarios), branch `operacion/horario-vigencia-politicas-safety-net`.

Branch de implementación: `operacion/horario-versionado-core`.

## 1. Componentes introducidos

- `com.feelingpilates.ubicaciones.dominio.RangoVigencia` — VO inmutable `record(desde, hasta)`
  con `contiene(fecha)`, `intersecta(otro)`, `esContiguoCon(otro)`. Único lugar con aritmética
  de fronteras del núcleo temporal (§3, §25 del diseño). No migra todavía
  `BloqueProgramacionService.vigenciasSeIntersectan` (eso es F2B.2, §11 del encargo).
- `com.feelingpilates.ubicaciones.dominio.DiaSemanaOperacion` — utilidad estática
  `desde(DayOfWeek)`, misma convención que `TurnoInstructorService.diaSemanaIso` (domingo=0,
  resto = `getValue()`). No reemplaza aún esa duplicación en `TurnoInstructorService` (F2B.2).
- `HorarioOperacionRepository.findVigente(salonId, diaSemana, fecha)` — `List`, no `Optional`.
- `HorarioOperacionRepository.findVersionesQueIntersectan(salonId, diaSemana, desde, hasta)` —
  `hasta` nullable, `desde` obligatorio, `ORDER BY vigente_desde ASC NULLS FIRST`.
- `com.feelingpilates.ubicaciones.servicio.HorarioOperacionResolver` — `Optional<HorarioOperacion>
  resolver(salonId, fecha)`. 0 filas ⇒ `Optional.empty()`; 1 ⇒ esa fila; >1 ⇒
  `IllegalStateException` (fallo ruidoso, no HTTP, no controller lo consume).

## 2. Semántica temporal

`[vigenteDesde, vigenteHasta]` cerrado en ambos extremos. `NULL` = infinito lógico, nunca
`LocalDate.MIN`/`MAX`. `esContiguoCon` sólo hace `plusDays(1)` cuando el límite relevante de
ambos rangos es finito; un rango abierto hacia `+∞` (`hasta == null`) nunca es contiguo con
nada por ese lado — se corta antes de tocar el sentinel, sin overflow.

## 3. Queries

`findVigente`: predicado de contención de fecha sobre `vigente_desde`/`vigente_hasta`
nullable, sin filtrar por hora, sin `ORDER BY` (legalmente ≤1 fila bajo el UNIQUE).

`findVersionesQueIntersectan`: nativa, `cast(:hasta as date) is null` para el parámetro
nullable comparado contra `IS NULL` sin contexto de tipo — mismo patrón ya usado en
`AsignacionRepository.buscarConflictosRecurrentesDelInstructor` y
`BloqueProgramacionRepository.buscarTraslapesActivos`. No se reintrodujo el defecto de 1C.2
(`PSQLException: could not determine data type of parameter`); probado con PostgreSQL real en
la ruta `hasta == null` y `hasta != null` por separado
(`HorarioOperacionRepositoryTest.findVersionesQueIntersectanConsultaAbiertaHastaNull` y
`...RangoAcotadoQueIntersecta`).

## 4. Comportamiento del resolver

Convierte `fecha.getDayOfWeek()` con `DiaSemanaOperacion.desde` (sin duplicar la conversión
dentro del resolver — verificado con `verify(...)` en el test unitario), llama a
`findVigente` y interpreta el tamaño de la lista devuelta. No conoce `SalonHorarioExcepcion`.
No se crea `HorarioEfectivoSalon` en esta fase.

## 5. Tests

Nuevos: 38 (14 `RangoVigenciaTest` + 7 `DiaSemanaOperacionTest` + 5
`HorarioOperacionResolverTest` [mock] + 12 `HorarioOperacionRepositoryTest` [PostgreSQL real,
Testcontainers]).

`HorarioOperacionRepositoryTest` respeta el UNIQUE: cada test method corre en su propia
transacción (`@Transactional`, revertida por Spring al final de cada test), y dentro de cada
test sólo se inserta una fila por `(salon, dia)` — se varía `diaSemana` entre escenarios y se
usan los dos salones sembrados por `V10__salones_semilla.sql` para los casos "distinto salón".
No se intentó nunca persistir dos versiones reales del mismo `(salon, dia)`.

Total suite: **169/169 PASS** (131 baseline + 38 nuevos). Ningún test existente se modificó.

## 6. Flyway / JPA

Sin migraciones nuevas. `HorarioOperacionMigracionV42V43Test` y `UbicacionesPersistenciaTest`
(parte de los 169) siguen verdes: Flyway V1→V43 PASS, JPA `validate` PASS (metamodel de
`HorarioOperacion` sin cambios, ninguna columna ni constraint tocada).

## 7. Scope

Diff real (`git status --short` en `operacion/horario-versionado-core`):

```
M  src/main/java/com/feelingpilates/ubicaciones/repositorio/HorarioOperacionRepository.java
?? src/main/java/com/feelingpilates/ubicaciones/dominio/
?? src/main/java/com/feelingpilates/ubicaciones/servicio/HorarioOperacionResolver.java
?? src/test/java/com/feelingpilates/ubicaciones/dominio/
?? src/test/java/com/feelingpilates/ubicaciones/repositorio/
?? src/test/java/com/feelingpilates/ubicaciones/servicio/
```

Verificado vacío: `git diff -- src/main/resources/db/migration`,
`git diff -- src/main/java/com/feelingpilates/calendario`,
`git diff -- src/main/java/com/feelingpilates/programacion`.

No se tocó `HorarioOperacion.java` (entidad), `SalonService`, `TurnoInstructorService`,
`BloqueProgramacionService`, `SalonHorarioExcepcion`, ningún controller/DTO HTTP, `pom.xml`,
ni V1–V43. `HorarioOperacionRepository` sólo recibió adiciones — ninguna firma existente se
tocó.

## 8. Decisiones deliberadamente diferidas (fuera de alcance de F2B.1)

- Migrar `BloqueProgramacionService.vigenciasSeIntersectan` a `RangoVigencia` → F2B.2.
- Migrar `TurnoInstructorService.diaSemanaIso` a `DiaSemanaOperacion` → F2B.2.
- Crear y cablear `HorarioEfectivoSalon` → F2B.2.
- `VersionarHorarioOperacion` / `CerrarHorarioOperacion`, lock de `Salon`, `23P01` → F2B.3b.
- `findVersionesOrdenadas` → sin consumidor real hasta el endpoint de historial (F2B.3b).
- `btree_gist`, `EXCLUDE`, `DROP UNIQUE` (V44–V46) → F2B.3a.
- `Clock`/zona horaria de negocio para `LocalDate.now(clock)` → dependencia real de F2B.2, no
  resuelta aquí porque F2B.1 no necesita fecha implícita (toda fecha es argumento explícito).

## 9. Mutaciones conceptuales (§37 del encargo)

| # | Mutación | Resultado |
|---|---|---|
| 1 | `vigenteDesde` exacto deja de ser inclusivo | DETECTADO (`boundedContieneDesdeExacto`, `findVigenteIncluyeFechaDesdeExacta`) |
| 2 | `vigenteHasta` exacto deja de ser inclusivo | DETECTADO (`boundedContieneHastaExacto`, `findVigenteIncluyeFechaHastaExacta`) |
| 3 | NULL inferior deja de representar -∞ | DETECTADO (`nullNullContieneCualquierFecha`, `findVigenteLegacyResuelvePasadoPresenteYFuturo`) |
| 4 | NULL superior deja de representar +∞ | DETECTADO (`fechaDesdeNullHastaContieneCualquierFechaDesdeElLimite`, ídem legacy futuro) |
| 5 | 31-ago y 1-sep dejan de ser contiguos | DETECTADO (`rangosSeparadosPorUnDiaSonContiguos`) |
| 6 | Dos rangos que comparten 1-sep se consideran disjuntos | DETECTADO (`intersectanEnUnaFronteraComun`) |
| 7 | Sunday se mapea a 7 | DETECTADO (`domingoEsCero`) |
| 8 | `findVigente` ignora `vigenteHasta` | DETECTADO (`findVigenteExcluyeFechaFueraDeVigencia`) |
| 9 | `findVigente` ignora `vigenteDesde` | DETECTADO (`findVigenteExcluyeFechaFueraDeVigencia`) |
| 10 | `findVersionesQueIntersectan` falla con `hasta=null` | DETECTADO (`findVersionesQueIntersectanConsultaAbiertaHastaNull`) |
| 11 | Se reintroduce el bug de binding PostgreSQL con `hasta` no-null | DETECTADO (`findVersionesQueIntersectanRangoAcotadoQueIntersecta` + la ruta `hasta=null` cubren ambos caminos exigidos por §18) |
| 12 | Resolver devuelve arbitrariamente uno cuando repository retorna >1 | DETECTADO (`repositoryConDosFilasFallaRuidosamente`) |

Las 12 mutaciones: **DETECTADO**.

## 10. Checkpoint

Base: `c07b1d00934b3ba6492f102fca910cf6e421f781`

RangoVigencia: IMPLEMENTADO
DiaSemana: IMPLEMENTADO
findVigente: IMPLEMENTADO
findVersionesQueIntersectan: IMPLEMENTADO
Binding hasta nullable: PROBADO PG
HorarioOperacionResolver: IMPLEMENTADO
UNIQUE salon+dia: CONSERVADO
Consumers: SIN CAMBIOS
Writers: NO IMPLEMENTADOS
Migraciones: SIN CAMBIOS
Tests: 169/169 PASS
Build: PASS
Flyway: V1→V43 PASS
JPA: PASS
F2B.2: LISTA
