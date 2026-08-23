# FeelingPilates — Fase 1C.2: corregir binding PostgreSQL de vigencias

Fecha: 2026-08-22

Branch base: `programacion/modelo-base-safety-net`

Commit base: `622d3b51f0763940a98b93a42609797dbdba2a11`

Branch de trabajo: `programacion/modelo-base-query-binding-fix`

Alcance: bugfix acotado en dos queries nativas y regresiones de persistencia contra PostgreSQL
real. No se modificaron reglas de negocio, servicio, entidades, configuración ni migraciones.

## Pre-flight

- Branch base: `programacion/modelo-base-safety-net`.
- HEAD base: `622d3b51f0763940a98b93a42609797dbdba2a11`.
- Working tree inicial: limpio.
- La rama base estaba respaldada por `origin/programacion/modelo-base-safety-net`.
- Se creó `programacion/modelo-base-query-binding-fix` desde el commit base.
- No se tocó `main`.

## Excepción reproducida (RED)

Se agregaron primero dos tests de integración que pasan `vigenteHasta` explícitamente no nulo y
ejecutan los repositorios reales mediante Hibernate/pgjdbc contra PostgreSQL 16.14 en
Testcontainers:

- `asignacionDetectaConflictoCrossSalonConVigenciasAcotadas`.
- `bloqueDetectaTraslapeConVigenciasAcotadas`.

Comando:

```text
./mvnw -Dtest='ProgramacionPersistenciaTest#asignacionDetectaConflictoCrossSalonConVigenciasAcotadas+bloqueDetectaTraslapeConVigenciasAcotadas' test
```

Resultado antes del fix:

```text
Tests run: 2, Failures: 0, Errors: 2, Skipped: 0
org.postgresql.util.PSQLException
SQLState: 42P18
ERROR: could not determine data type of parameter $5
```

El error se reprodujo tanto en
`AsignacionRepository.buscarConflictosRecurrentesDelInstructor` como en
`BloqueProgramacionRepository.buscarTraslapesActivos`.

## Causa técnica confirmada

Hibernate convirtió cada aparición del parámetro nombrado repetido `:vigenteHasta` en un
placeholder JDBC distinto. El SQL enviado a PostgreSQL contenía el patrón:

```sql
(? is null or vigente_desde <= ?)
```

Aunque la segunda aparición obtiene contexto de tipo por su comparación con una columna `date`,
el placeholder de `? is null` no tiene contexto de tipo propio. PostgreSQL no puede inferirlo y
rechaza la consulta con SQLState `42P18`, incluso cuando Java suministra un `LocalDate` no nulo.

La lógica de intersección de vigencias no era la causa y se conservó sin cambios.

## Cambio aplicado

En la aparición sin contexto de tipo de ambas queries se agregó únicamente un cast SQL estándar:

```sql
cast(:vigenteHasta as date) is null
```

No se usó la sintaxis `:vigenteHasta::date`. No se alteraron las comparaciones de fechas ni de
horas. La consulta conserva:

- `vigenteDesde` inclusivo.
- `vigenteHasta` inclusivo.
- `vigenteHasta = null` como infinito.
- intervalos horarios `[inicio, fin)`.
- conflicto de instructor global entre salones.
- filtros por activo y día de semana.

## Resultado después del fix (GREEN)

El mismo comando RED terminó después del cambio con:

```text
Tests run: 2, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

Los logs de Hibernate confirmaron el SQL corregido:

```sql
(cast(? as date) is null or vigente_desde <= ?)
```

## Casos de regresión

Todos los casos nuevos o reforzados se ejecutan contra PostgreSQL real, sin Mockito para los
repositorios:

### Asignación

- Vigencias acotadas e intersectadas, conflicto cross-salón: existente 2026-01-01/2026-03-31 y
  consulta 2026-02-01/2026-02-28; detecta conflicto.
- Vigencias acotadas disjuntas: existente hasta 2026-01-31 y consulta
  2026-02-01/2026-03-31; no detecta conflicto.
- Inclusividad de vigencia: existente hasta 2026-01-31 y consulta desde 2026-01-31; detecta
  intersección en el día compartido.
- Contigüidad horaria: existente 08:00-10:00 y consulta 10:00-12:00 con vigencias acotadas
  intersectadas; no detecta conflicto.
- Vigencia abierta: se conservaron llamadas existentes con `vigenteHasta = null`, que continúan
  funcionando como infinito.

### Bloque

- Mismo salón/día y horario traslapado con vigencias acotadas intersectadas; detecta traslape.
- Mismo salón/día y horario traslapado con vigencias acotadas disjuntas; no detecta traslape.
- Horarios contiguos 08:00-10:00 y 10:00-12:00 con vigencias acotadas intersectadas; no detecta
  traslape.
- Vigencia abierta: se conservaron llamadas existentes con `vigenteHasta = null`, que continúan
  funcionando como infinito.

## Tests, build, Flyway y JPA

- Reproducción RED: **0/2 PASS; 2 ERROR** por `PSQLException`/SQLState `42P18`.
- Reproducción GREEN: **2/2 PASS**.
- Tests de Programación: **50/50 PASS**
  (13 en `ProgramacionPersistenciaTest`, 37 en `BloqueProgramacionServiceTest`).
- Suite completa (`./mvnw test`): **120/120 PASS**.
- Build (`./mvnw clean compile`): **BUILD SUCCESS**.
- Flyway: **PASS**; 44 migraciones validadas y aplicadas desde esquema vacío, versión final V41.
- JPA: **PASS**; `jpaValidaYRegistraLasEntidadesDeProgramacion` pasó y Hibernate inicializó el
  `EntityManagerFactory` validando las entidades de Programación.

## Diff final

Rutas modificadas:

```text
src/main/java/com/feelingpilates/programacion/repositorio/AsignacionRepository.java
src/main/java/com/feelingpilates/programacion/repositorio/BloqueProgramacionRepository.java
src/test/java/com/feelingpilates/programacion/ProgramacionPersistenciaTest.java
auditoria/fase-1c2-query-binding-vigencias.md
```

- `BloqueProgramacionService`: sin cambios.
- Entidades y V41: sin cambios.
- V1-V40: sin cambios.
- Calendario legado: sin cambios.
- Reserva: sin cambios.
- `Salon`, `HorarioOperacion` y `TipoActividad`: sin cambios.
- `pom.xml` y configuración: sin cambios.

Conclusión semántica: **SÍ, la corrección sólo resolvió el binding PostgreSQL**. No se modificó la
regla de overlap, la inclusividad de vigencias, el alcance cross-salón, los filtros de activo o día
de semana ni reglas de ownership.
