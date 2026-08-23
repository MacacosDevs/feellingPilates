# Fase 2A.1 — Safety net de migracion V42→V43 (horario_operacion.vigencia)

Base: `eda09cd97d9f325f27555ce16743518fd76a39b7`

## Objetivo

Cerrar los dos gaps P1 detectados en la revision independiente de Fase 2A:

1. La prueba existente no demostraba que una fila de `horario_operacion`
   creada antes de V43 sobreviviera a una migracion REAL V42→V43 con
   `vigente_desde = NULL` y `vigente_hasta = NULL`.
2. Faltaba proteger explicitamente el intervalo de vigencia cerrado inclusivo
   (`vigente_desde == vigente_hasta`).

Alcance: solo `src/test/**`. Cero cambios en produccion, entidades, servicios,
DTOs, controllers o migraciones.

## Resultado

| Verificacion | Resultado |
|---|---|
| Upgrade real V42→V43 | CUBIERTO |
| Fila pre-V43 preservada | SI |
| ID preservado | SI |
| Horario preservado (dia_semana/apertura/cierre) | SI |
| vigente_desde legado | NULL |
| vigente_hasta legado | NULL |
| Cantidad de filas | PRESERVADA |
| desde == hasta | PERMITIDO |
| Produccion | SIN CAMBIOS |
| Migraciones | SIN CAMBIOS |
| Tests | 131/131 PASS |
| Build (`clean compile`) | PASS |

## Como se cerro el gap 1 (upgrade real por etapas)

Nuevo test aislado, sin contexto de Spring:
[`HorarioOperacionMigracionV42V43Test.java`](../src/test/java/com/feelingpilates/ubicaciones/HorarioOperacionMigracionV42V43Test.java)

- Levanta un PostgreSQL efimero propio (Testcontainers, `postgres:16-alpine`),
  independiente del `TestcontainersConfiguration` que usan los tests con
  `@SpringBootTest` (esos ya arrancan migrados hasta V43 y no sirven para este
  caso).
- Usa la API fluida de Flyway (`Flyway.configure().target("42").load().migrate()`)
  para migrar **realmente** solo hasta V42 — no se copia SQL de V43 a mano ni
  se simula la migracion.
- Confirma antes de V43:
  - version actual = "42".
  - V43 aparece como pendiente (verificado con una segunda instancia de Flyway
    sin `target`, ya que una instancia configurada con `target("42")` no
    reporta pendientes mas alla de su propio target).
  - las columnas `vigente_desde` / `vigente_hasta` todavia NO existen en
    `horario_operacion` (consulta a `information_schema.columns`).
- Inserta por JDBC plano una fila real de `horario_operacion` en ese estado
  V42 (salon existente sembrado por V10, `dia_semana = 1`,
  `hora_apertura = 08:00`, `hora_cierre = 20:00`) y captura su `id`.
- Avanza Flyway al mismo `DataSource` con `target("43")` (misma base, mismo
  `flyway_schema_history`, migracion versionada real, no reejecutada a mano).
- Vuelve a consultar exactamente esa fila por `id` y verifica:
  `salon_id`, `dia_semana`, `hora_apertura`, `hora_cierre` sin cambios;
  `vigente_desde` y `vigente_hasta` en `NULL`; y que el conteo total de filas
  de `horario_operacion` no cambio.

## Como se cerro el gap 2 (vigencia cerrada inclusiva)

Nuevo metodo `checkVigenciaPermiteDesdeIgualAHasta` agregado a
[`UbicacionesPersistenciaTest.java`](../src/test/java/com/feelingpilates/ubicaciones/UbicacionesPersistenciaTest.java),
siguiendo el mismo patron que los tests de vigencia ya existentes en ese
archivo. Inserta un horario con `vigente_desde = vigente_hasta = 2026-08-22`
contra el CHECK real de V43 aplicado por Spring/Flyway y confirma que se
acepta (protege que la regla sea `>=` y no `>`).

Los casos ya cubiertos previamente (`desde > hasta` rechazado; `null/null`,
`null/fecha`, `fecha/null` permitidos) no se duplicaron.

## Mutaciones futuras protegidas

| # | Mutacion hipotetica en V43 | Detectado |
|---|---|---|
| 1 | `UPDATE horario_operacion SET vigente_desde = DATE '2000-01-01'` | DETECTADO — assert `vigente_desde` es NULL sobre la fila legado tras la migracion |
| 2 | Eliminar/reinsertar horarios cambiando el `id` | DETECTADO — se reconsulta exactamente por el `id` capturado antes de V43; si cambia, la fila no aparece y el `assertThat(rs.next()).isTrue()` falla |
| 3 | V43 elimina una fila existente | DETECTADO — comparacion de conteo total de filas antes/despues, mas la busqueda por `id` |
| 4 | V43 cambia `apertura`/`cierre`/`dia_semana` | DETECTADO — comparacion explicita de esos tres valores antes/despues |
| 5 | V43 define `vigente_desde NOT NULL` sin backfill valido | DETECTADO — con una fila preexistente, el `ALTER TABLE ADD COLUMN ... NOT NULL` sin `DEFAULT` fallaria durante `migrate()`, y el test (sin capturar excepcion) fallaria |
| 6 | El CHECK cambia de `>=` a `>` | DETECTADO — `checkVigenciaPermiteDesdeIgualAHasta` fallaria porque el insert con `desde == hasta` seria rechazado |

Los 6 casos quedan DETECTADO.

## Verificacion ejecutada

- Test nuevo en aislamiento (`HorarioOperacionMigracionV42V43Test`): PASS.
- Test nuevo en aislamiento (`UbicacionesPersistenciaTest`): PASS.
- Suite completa (`./mvnw test`): 131/131 PASS (baseline previo: 129/129).
- Build (`./mvnw clean compile`): PASS.
- `git diff -- src/main/java`: vacio.
- `git diff -- src/main/resources`: vacio.
- Diff total: solo `src/test/**` y este archivo de auditoria.

## P2 no bloqueantes (fuera de alcance de esta fase)

- Contrato HTTP de Salon / horarios (pruebas de serializacion, DTOs).
- Consumidores actuales de `horario_operacion` que todavia ignoran
  `vigente_desde`/`vigente_hasta` (logica de Fase 2B).
- El CHECK de confirmacion (`requiere_confirmacion_instructor` con
  `plazo_respuesta_confirmacion_horas` NULL) se mantiene deliberadamente
  como esta; no se toco en esta fase.
