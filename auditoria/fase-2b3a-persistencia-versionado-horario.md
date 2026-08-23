# Fase 2B.3a — Persistencia versionada de HorarioOperacion

## Base

- Branch base: `operacion/horario-consumidores-temporales`
- Commit base: `4a7cd699bfeae46d6c7976674d5ae1ace0f9d00f`
- Branch de trabajo: `operacion/horario-versionado-persistencia`
- Baseline verificado antes de tocar código: **240/240 tests PASS**, working tree limpio, sin
  divergencia con origin, build y Flyway (V1→V43) y JPA en PASS.

## Objetivo

Cambiar **exclusivamente** la garantía física de persistencia de `horario_operacion`: de "a lo
sumo una fila por `(salon_id, dia_semana)`" a "N versiones por `(salon_id, dia_semana)`, siempre
que sus vigencias no se intersecten". Sin lógica de negocio nueva, sin writers, sin frontend.

---

## 1. Precheck de schema/JPA (§4)

Se revisó `HorarioOperacion.java` **antes** de escribir cualquier migración. No tiene
`@Table(uniqueConstraints = ...)` ni `@Column(unique = true)`: la unicidad vivía únicamente en
Flyway (V11). **No hubo metadata JPA contradictoria** → no se activó la condición de parada del
§4, y no se tocó ningún archivo de `src/main/java/**`.

## 2. Constraint actual (§5)

El `UNIQUE (salon_id, dia_semana)` de V11 se declaró inline, sin nombre explícito
(`src/main/resources/db/migration/V11__salones_gestion.sql:28`), por lo que PostgreSQL le asigna
el nombre autogenerado `horario_operacion_salon_id_dia_semana_key`. Se confirmó contra
PostgreSQL real (vía `pg_constraint`, ver §5 de este documento más abajo) que ese es
efectivamente el nombre — coincide con el documentado, no hubo que corregirlo.

## 3. Migraciones nuevas

### V44 — `V44__btree_gist_extension.sql`

```sql
CREATE EXTENSION IF NOT EXISTS btree_gist;
```

Sin `DROP`/`UPDATE`/`DELETE`/backfill/`ALTER horario_operacion`. Requisito de V45: el `EXCLUDE
USING gist` necesita operadores `=` sobre `uuid`/`smallint` disponibles vía `btree_gist`.

### V45 — `V45__horario_operacion_exclude_vigencia.sql`

```sql
ALTER TABLE horario_operacion
    ADD CONSTRAINT ex_horario_operacion_vigencia
        EXCLUDE USING gist (
            salon_id WITH =,
            dia_semana WITH =,
            daterange(vigente_desde, vigente_hasta, '[]') WITH &&
        );
```

No usa generated column, no trigger, no daterange persistido adicional. El `UNIQUE` histórico
**no** se retira en esta migración: coexiste con el `EXCLUDE` (ver §5 abajo).

### V46 — `V46__horario_operacion_drop_unique_dia.sql`

```sql
ALTER TABLE horario_operacion
    DROP CONSTRAINT horario_operacion_salon_id_dia_semana_key;
```

Elimina exclusivamente ese `UNIQUE`. No toca PK, CHECK de horas, CHECK de `dia_semana`, CHECK de
vigencia, ni el `EXCLUDE`.

## 4. Semántica PostgreSQL de límites (§9)

- `vigente_desde IS NULL` → límite inferior abierto (`-infinity`).
- `vigente_hasta IS NULL` → límite superior abierto (`+infinity`).
- Ambos `NULL` → rango universal (semántica de fila legada, F2A/F2B.1).
- Los extremos de negocio son **inclusivos**; se expresan con `daterange(desde, hasta, '[]')`
  para que PostgreSQL los canonicalice a la forma semiabierta interna `[asc, desc)` propia de
  `daterange` (tipo discreto). Consecuencia verificada empíricamente (mutación 7, §9 más abajo):
  `NULL → 2026-08-31` y `2026-09-01 → NULL` **no** intersectan (canonicalizan a
  `[-inf, 2026-09-01)` y `[2026-09-01, +inf)`), pero `NULL → 2026-09-01` y `2026-09-01 → NULL`
  **sí** intersectan en el 1 de septiembre.

## 5. Constraints antes/después, por etapa

Verificado con PostgreSQL real (Testcontainers) y Flyway por etapas
(`HorarioOperacionMigracionV43V46Test`), inspeccionando `pg_extension`/`pg_constraint` en cada
paso — no se asumió el efecto por el nombre de la migración.

| Etapa | `btree_gist` | `UNIQUE(salon,dia)` | `EXCLUDE` vigencia | 2ª fila mismo salon/día (NULL/NULL) |
|---|---|---|---|---|
| V43 | ausente | presente | ausente | RECHAZADA (UNIQUE) |
| V44 | presente | presente | ausente | — |
| V45 | presente | presente | presente | RECHAZADA (coexistencia, sin distinguir cuál dispara) |
| V46 | presente | **ausente** | presente | dos versiones **no solapadas** → PERMITIDAS |

En V45 se confirmó explícitamente contra `pg_constraint` que ambas constraints (`UNIQUE` y
`ex_horario_operacion_vigencia`) existen simultáneamente — no hay ventana sin garantía entre V44
y V46.

## 6. Tests nuevos/modificados

| Archivo | Qué prueba | Tests |
|---|---|---|
| `HorarioOperacionMigracionV43V46Test` | Migración real por etapas V43→V44→V45→V46 (Flyway + Testcontainers puro, sin Spring), estado de extensión/constraints e inserción real en cada etapa | 1 |
| `HorarioOperacionVersionadoPersistenciaTest` | Invariante física post-V46: contiguas OK, solape de frontera/interior rechazado, legacy universal bloquea, cerrar+insertar manual, UPDATE que crea solape rechazado, distinto salón/día permitido, gap permitido, CHECKs de hora/día siguen activos | 11 |
| `HorarioOperacionRepositoryTest` | `findVigente` y `findVersionesQueIntersectan` contra **dos versiones reales persistidas** (no mock) cruzando la frontera | +2 |
| `HorarioOperacionResolverPersistenciaTest` | `HorarioOperacionResolver` contra dos versiones contiguas reales en PostgreSQL: cada fecha resuelve a su versión, ninguna ambigüedad | 1 |
| `UbicacionesPersistenciaTest` | `uniqueSalonDiaSemanaSigueVigenteEnF2A` → **reemplazado** por `mismoSalonDiaSemanaConVigenciasQueIntersectanEsRechazado` (mismo resultado observable, causa distinta: ahora `ex_horario_operacion_vigencia`, no `UNIQUE`) | 0 (reemplazo) |
| `ProgramacionPersistenciaTest` | `flywayMigraDesdeV1HastaV43` → renombrado `flywayMigraDesdeV1HastaV46`, versión/`applied()` actualizados a 46/49 (colateral esperado de añadir V44-V46, sin relación con horarios) | 0 (ajuste de baseline) |

**TEST DE UNIQUE: REEMPLAZADO INTENCIONALMENTE** (§27 del encargo). La prueba legada insertaba
dos filas `NULL/NULL` para el mismo salón/día; eso sigue siendo rechazado post-V46 porque
`NULL/NULL` es un rango universal que se intersecta consigo mismo — pero ahora la causa es el
`EXCLUDE`, no el `UNIQUE` (que ya no existe). Se verificó con
`.hasMessageContaining("ex_horario_operacion_vigencia")`.

Total: **15 tests nuevos** (240 → 255), ningún test histórico cambió su expectativa salvo los dos
reemplazos documentados arriba.

## 7. Separación intencional BD/negocio (§25)

`ex_horario_operacion_vigencia` garantiza **no-overlap**, no continuidad. Un *gap* entre
versiones del mismo salón/día (p. ej. `NULL→31 ago` y `15 sep→NULL`) es **permitido por la base
de datos** (`gapEntreVersionesEsPermitidoPorLaBaseDeDatos`, PASS). Decidir si ese gap es aceptable
es responsabilidad de la capa de negocio/consumers (F2B.3b en adelante), **no** de este
constraint. No se convirtió el `EXCLUDE` en una regla de continuidad.

## 8. Consumers y frontend

- **Consumers** (`SalonService`, `TurnoInstructorService`, `BloqueProgramacionService`,
  `HorarioEfectivoSalon`, `CoberturaVigencia`, `Clock`): **SIN CAMBIOS**. Ninguno requirió ajuste
  para que V46 funcionara → confirma que F2B.2 estaba realmente cerrada.
- **Writers** (`VersionarHorarioOperacion`, `CerrarHorarioOperacion`, command handlers, lock de
  `Salon`, `FOR UPDATE`, manejo de `23P01`, endpoint): **NO IMPLEMENTADOS**. Reservado a F2B.3b.
- **Frontend** (`Feelingpilates/web`): **SIN CAMBIOS**. El contrato HTTP no se tocó.
- **JPA** (`HorarioOperacion.java`): **SIN CAMBIOS** (confirmado en precheck §4).

## 9. Verificación de mutaciones conceptuales (§38)

Cada mutación marcada con **(aplicada)** se introdujo realmente sobre el archivo de migración
correspondiente, se ejecutó la suite relevante, se confirmó el fallo y se revirtió el archivo
(diff verificado contra el original tras cada reversión). Las marcadas **(por construcción)** no
requieren mutación de SQL porque el test objetivo ya se ejecutó exitosamente contra la
implementación correcta y falla estructuralmente ante exactamente esa desviación (propiedad
directamente asertada, sin lógica intermedia que pueda enmascararla).

| # | Mutación | Método de verificación | Estado |
|---|---|---|---|
| 1 | V44 no instala `btree_gist` | aplicada — vaciar V44 | DETECTADO (V45 falla al migrar: faltan opclasses gist para uuid/smallint) |
| 2 | V45 no crea `EXCLUDE` | aplicada — vaciar V45 | DETECTADO |
| 3 | V45 elimina prematuramente el `UNIQUE` | aplicada — añadir DROP en V45 | DETECTADO (V46 falla: constraint ya no existe) |
| 4 | V46 no elimina el `UNIQUE` | aplicada — vaciar V46 | DETECTADO |
| 5 | V46 elimina accidentalmente el `EXCLUDE` | aplicada — añadir DROP extra en V46 | DETECTADO |
| 6 | rangos contiguos son rechazados | por construcción (`versionesContiguasSonAmbasInsertadas`) | DETECTADO |
| 7 | frontera compartida deja de ser overlap | aplicada — quitar `'[]'` de `daterange(...)` | DETECTADO (fallan `solapeDeUnDiaEnFronteraCompartidaEsRechazado` y `updateQueCreaSolapeEsRechazado`) |
| 8 | `NULL/NULL` deja de cubrir todo | por construcción (`legacyUniversalBloqueaNuevaVersionReal`) | DETECTADO |
| 9 | `EXCLUDE` sólo protege INSERT pero no UPDATE | por construcción (`updateQueCreaSolapeEsRechazado` ejecuta un UPDATE real) | DETECTADO |
| 10 | constraint mezcla salones distintos | aplicada — quitar `salon_id WITH =` | DETECTADO (`distintoSalonPermiteMismasVigencias` falla con `DataIntegrityViolationException`) |
| 11 | constraint mezcla días distintos | aplicada — quitar `dia_semana WITH =` | DETECTADO (`distintoDiaPermiteMismasVigencias` falla) |
| 12 | BD empieza a prohibir gaps | por construcción (`gapEntreVersionesEsPermitidoPorLaBaseDeDatos`) | DETECTADO |
| 13 | `findVigente` falla con dos versiones reales contiguas | por construcción (`findVigenteConDosVersionesContiguasResuelveCadaFechaASuVersion`) | DETECTADO |
| 14 | `findVersionesQueIntersectan` no devuelve ambas al cruzar frontera | por construcción (`findVersionesQueIntersectanConDosVersionesContiguasDevuelveAmbasAlCruzarFrontera`) | DETECTADO |
| 15 | V1-V43 fueron modificadas | `git diff <base> -- src/main/resources/db/migration` vacío salvo V44/V45/V46 añadidos | DETECTADO |

**Los 15 quedan DETECTADO.**

## 10. Riesgos operativos pendientes

- **Disponibilidad de `btree_gist` en el entorno de despliegue real**: es una extensión
  `contrib` estándar de PostgreSQL, pero requiere que el rol de la aplicación tenga privilegio
  para `CREATE EXTENSION` (o que un DBA la instale de antemano) en el Postgres de producción.
  No verificado fuera de Testcontainers/`postgres:16-alpine`.
- **Lock de `ADD CONSTRAINT ... EXCLUDE USING gist`**: en PostgreSQL, agregar un `EXCLUDE`
  constraint sobre una tabla con filas existentes toma `ACCESS EXCLUSIVE LOCK` mientras construye
  el índice GiST subyacente y valida todas las filas. Sobre una tabla pequeña (como
  `horario_operacion`, decenas/cientos de filas por salón) el impacto es mínimo, pero debe
  tenerse en cuenta en una ventana de despliegue si la tabla creciera significativamente.
- **Javadoc desactualizado (no corregido, fuera de alcance):**
  `HorarioOperacionRepository.findVigente` (línea 20-21) dice *"Legalmente hay a lo sumo una
  (UNIQUE(salon_id, dia_semana))"* — ya no es cierto post-V46. No se tocó porque el alcance de
  F2B.3a excluye explícitamente `src/main/java/**` salvo metadata JPA contradictoria (que no
  aplicó, §4). Se deja registrado para corregirse en F2B.3b.

## 11. Alcance deliberadamente excluido

- Writers (`VersionarHorarioOperacion`, `CerrarHorarioOperacion`), command handlers, lock de
  `Salon`, `FOR UPDATE`, validación inversa, endpoint, manejo de `23P01` → **F2B.3b**.
- Frontend (`Feelingpilates/web`) → sin cambios, edición real de horarios sigue bloqueada.
- Índice adicional (V47) → el `EXCLUDE` ya crea su propia estructura GiST; no se justifica sin
  perfil de uso real.
- Corrección del Javadoc desactualizado en `HorarioOperacionRepository` (ver §10).

---

## 12. Verificaciones finales

| Verificación | Resultado |
|---|---|
| `./mvnw test` | **255/255 PASS** (baseline 240/240; +15 tests, dos reemplazos documentados) |
| `./mvnw clean compile` | **BUILD SUCCESS** |
| Flyway | **V1 → V46 PASS** (49 migraciones aplicadas, schema en v46) |
| JPA `validate` | **PASS** (sin `SchemaManagementException` en ningún `@SpringBootTest`) |
| `UNIQUE(salon_id, dia_semana)` | **ELIMINADO** en V46 |
| `EXCLUDE ex_horario_operacion_vigencia` | **PRESENTE** desde V45 |
| Versiones contiguas reales | **PROBADAS** (staged test + repository test + resolver test) |
| Overlap (frontera/interior) | **RECHAZADO** |
| Límites `NULL` (abierto/universal) | **PROBADOS** |
| UPDATE que crea overlap | **RECHAZADO** |
| Gap entre versiones | **PERMITIDO** (por diseño, ver §7) |
| `findVigente` multiversión | **PROBADO** (PostgreSQL real) |
| `findVersionesQueIntersectan` multiversión | **PROBADO** (PostgreSQL real) |
| `git diff <base> -- src/main/resources/db/migration` | vacío salvo V44/V45/V46 nuevos |
| Consumers | **SIN CAMBIOS** |
| Writers | **NO IMPLEMENTADOS** |
| Frontend | **SIN CAMBIOS** |
| `src/main/java/**` | **SIN CAMBIOS** |

## 13. Estado de cierre

| Concepto | Estado |
|---|---|
| Base | `4a7cd699bfeae46d6c7976674d5ae1ace0f9d00f` |
| V44 `btree_gist` | **PASS** |
| V45 `EXCLUDE` | **PASS** |
| V46 `DROP UNIQUE` | **PASS** |
| `UNIQUE salon+dia` | **ELIMINADO** |
| Versiones contiguas | **PROBADAS** |
| Overlap | **RECHAZADO** |
| Tests | **255/255 PASS** |
| Build | **PASS** |
| Flyway | **V1→V46 PASS** |
| JPA | **PASS** |
| Consumers | **SIN CAMBIOS** |
| Writers | **NO IMPLEMENTADOS** |
| Frontend | **SIN CAMBIOS** |
| F2B.3b | **LISTA** |

Ninguna stop condition se activó: `btree_gist` se instaló sin problema en PostgreSQL 16, el
`EXCLUDE` no requirió generated column, no fue necesario modificar consumers, no hubo que tocar
la entidad JPA, el `DROP UNIQUE` no rompió `ddl-auto=validate`, no había filas legadas que
impidieran V45, V45 coexistió con el `UNIQUE` sin problema, ninguna migración histórica necesitó
editarse, y ningún test requirió implementar un writer para pasar.
