# FeelingPilates — Preparación F2D.2

## 1. Pre-flight

Branch:
`operacion/excepciones-horario-fecha`

HEAD:
`6a8ffaa104de9a6b707982e679e78cda8aeb433c`

Remote:
`origin/operacion/excepciones-horario-fecha`
`6a8ffaa104de9a6b707982e679e78cda8aeb433c`

Ahead/behind:
`0 / 0`

Working tree:
`LIMPIO`

Checkpoint SHA:
`58af39f41b3bc089ebbd4ec67f684e270087ddf4eb695f2c7b55276d0aff352e`

Resultado:
**PASS**

## 2. Estado físico confirmado

TurnoInstructor:
`LEGACY_VIVO / PRODUCTIVO / AUTORIDAD ACTUAL`. Tiene entidad, repositorios, servicio transaccional, controller público `/api/turnos-instructor` y participación real en reservas.

BloqueProgramacion:
`IMPLEMENTADO_NO_PRODUCTIVO`. Entidad, tabla, repositorio, service interno y tests existentes. `crearBloque` usa `SalonLock`.

Asignacion:
`IMPLEMENTADO_NO_PRODUCTIVO`. Entidad, tabla, repositorio y writer interno. `crearAsignacion` todavía no usa locks de salón/instructor.

AjusteProgramacionFecha:
`NO IMPLEMENTADO`. No existe clase, repositorio, tabla ni servicio.

ProgramacionEfectiva:
`NO IMPLEMENTADA`. No existen ocurrencias nominales/efectivas ni resolver F2D.

Última Flyway:
`V46__horario_operacion_drop_unique_dia.sql`

V47:
Siguiente número disponible confirmado.

F2D.2 ya implementada:
**NO**

## 3. Autoridad

Autoridad productiva:
`TurnoInstructor`

Doble autoridad:
**NO**

`BloqueProgramacion + Asignacion` existe sólo como infraestructura interna. No hay controller público de `programacion`.

Resultado:
**PASS**

## 4. Dark launch

Consumers productivos nuevos:
**NINGUNO**

Controllers públicos nuevos:
**NINGUNO**

Reserva legacy:
Fuera de F2D.2. Continúa vinculada causalmente a `TurnoInstructor`.

ImpactoAjustesEnExcepcionHorario:
No existe y debe permanecer fuera de F2D.2.

Fence:
No implementado; fuera de F2D.2.

Cutover:
No implementado; fuera de F2D.2.

Resultado:
**PASS**

Existe `ImpactoBloquesEnHorario`, creado anteriormente para el horario operativo endurecido. No es el adapter prohibido de ajustes y F2D.2 no debe ampliarlo para leer `programacion_ajuste_fecha`.

## 5. Código relevante localizado

- [BloqueProgramacion.java]\(/Users/jesusaldaircruzortiz/Desktop/Feelingpilates/feelingpilates/src/main/java/com/feelingpilates/programacion/entidad/BloqueProgramacion.java): regla recurrente por salón, día, rango y vigencia.
- [Asignacion.java]\(/Users/jesusaldaircruzortiz/Desktop/Feelingpilates/feelingpilates/src/main/java/com/feelingpilates/programacion/entidad/Asignacion.java): serie de instructor/actividad/rango dentro de un bloque.
- [BloqueProgramacionService.java]\(/Users/jesusaldaircruzortiz/Desktop/Feelingpilates/feelingpilates/src/main/java/com/feelingpilates/programacion/servicio/BloqueProgramacionService.java): únicos writers actuales `crearBloque` y `crearAsignacion`.
- [AsignacionRepository.java]\(/Users/jesusaldaircruzortiz/Desktop/Feelingpilates/feelingpilates/src/main/java/com/feelingpilates/programacion/repositorio/AsignacionRepository.java): conflicto recurrente global por instructor.
- [BloqueProgramacionRepository.java]\(/Users/jesusaldaircruzortiz/Desktop/Feelingpilates/feelingpilates/src/main/java/com/feelingpilates/programacion/repositorio/BloqueProgramacionRepository.java): traslapes y vigencias de bloques.
- [V41\_\_programacion\_bloque\_asignacion.sql]\(/Users/jesusaldaircruzortiz/Desktop/Feelingpilates/feelingpilates/src/main/resources/db/migration/V41\_\_programacion\_bloque\_asignacion.sql): esquema físico actual.
- [SalonLock.java]\(/Users/jesusaldaircruzortiz/Desktop/Feelingpilates/feelingpilates/src/main/java/com/feelingpilates/ubicaciones/servicio/SalonLock.java): lock pesimista singular con `Propagation.MANDATORY`.
- [SalonRepository.java]\(/Users/jesusaldaircruzortiz/Desktop/Feelingpilates/feelingpilates/src/main/java/com/feelingpilates/ubicaciones/repositorio/SalonRepository.java): `bloquearParaActualizar`.
- [UsuarioRepository.java]\(/Users/jesusaldaircruzortiz/Desktop/Feelingpilates/feelingpilates/src/main/java/com/feelingpilates/usuarios/repositorio/UsuarioRepository.java): requiere query aditiva equivalente para instructor.
- [HorarioEfectivoSalon.java]\(/Users/jesusaldaircruzortiz/Desktop/Feelingpilates/feelingpilates/src/main/java/com/feelingpilates/ubicaciones/servicio/HorarioEfectivoSalon.java): autoridad operativa exacta por fecha.
- [SalonHorarioExcepcionService.java]\(/Users/jesusaldaircruzortiz/Desktop/Feelingpilates/feelingpilates/src/main/java/com/feelingpilates/ubicaciones/servicio/SalonHorarioExcepcionService.java): writer productivo que no debe recibir adapters F2D.
- [TurnoInstructorService.java]\(/Users/jesusaldaircruzortiz/Desktop/Feelingpilates/feelingpilates/src/main/java/com/feelingpilates/calendario/servicio/TurnoInstructorService.java): writer legacy productivo.
- [TurnoInstructorController.java]\(/Users/jesusaldaircruzortiz/Desktop/Feelingpilates/feelingpilates/src/main/java/com/feelingpilates/calendario/controlador/TurnoInstructorController.java): API productiva legacy.
- [ReservaService.java]\(/Users/jesusaldaircruzortiz/Desktop/Feelingpilates/feelingpilates/src/main/java/com/feelingpilates/calendario/servicio/ReservaService.java): consumer legacy.
- [ProgramacionPersistenciaTest.java]\(/Users/jesusaldaircruzortiz/Desktop/Feelingpilates/feelingpilates/src/test/java/com/feelingpilates/programacion/ProgramacionPersistenciaTest.java): actualmente fija Flyway V46 y 49 migraciones aplicadas.
- [HorarioOperacionConcurrenciaTest.java]\(/Users/jesusaldaircruzortiz/Desktop/Feelingpilates/feelingpilates/src/test/java/com/feelingpilates/ubicaciones/HorarioOperacionConcurrenciaTest.java): patrón reutilizable de concurrencia real, latches y `TransactionTemplate`.
- [TestcontainersConfiguration.java]\(/Users/jesusaldaircruzortiz/Desktop/Feelingpilates/feelingpilates/src/test/java/com/feelingpilates/TestcontainersConfiguration.java): PostgreSQL 16 Alpine.

## 6. Persistencia propuesta

Tablas:
`programacion_ajuste_fecha`

Constraints:

- `chk_programacion_ajuste_tipo`
- `chk_programacion_ajuste_forma`
- `chk_programacion_ajuste_rango`
- FKs sólo para salón, usuario y tipo de actividad resultado
- sin FK a `asignacion_serie_id`

EXCLUDE:

`ex_programacion_asignacion_serie_vigencia` sobre:

```
serie_id WITH =,
daterange(vigente_desde, vigente_hasta, '[]') WITH &&
```

Predicado:

```
WHERE (activo)
```

V47 debe verificar primero que no existan solapes activos preexistentes y abortar sin corregir datos.

Identidad:

- Recurrente/reemplazo: `serieId + fecha`
- Adición: `ajusteId + fecha`
- Fila física: UUID
- Soft delete mediante `activo = false`
- No reactivar filas inactivas

Por la identidad asignable de las adiciones, `AjusteProgramacionFecha` no debe heredar sin cambios el `@GeneratedValue` de `EntidadBase`. Debe declarar UUID asignable y timestamps equivalentes sin modificar la base común.

Índices:

- unique parcial por target activo
- salón resultado + fecha
- instructor resultado + fecha
- fecha activa

## 7. Concurrencia

SalonLocks:
Helper plural que deduplica, ordena UUID y delega en el lock pesimista existente.

InstructorLocks:
Nuevo helper equivalente sobre filas `usuario`, con query aditiva en `UsuarioRepository`.

Orden:

```
TODOS LOS SALONES, UUID ascendente
→ TODOS LOS INSTRUCTORES, UUID ascendente
```

Writers participantes:

- `BloqueProgramacionService.crearBloque`
- `BloqueProgramacionService.crearAsignacion`
- writer interno de ajustes
- cualquier writer recurrente nuevo que se introdujera dentro de F2D.2

`TurnoInstructor`, `ReservaService` y otros writers legacy no se conectan a este protocolo: hacerlo rompería el aislamiento.

TOCTOU:

```
discovery mínimo
→ locks completos y ordenados
→ relectura
→ comparación de nominal/ajuste/lock set
→ conflicto estable si cambió
→ validación
→ persistencia en la misma transacción
```

En una edición, los sets incluyen los recursos persistidos anteriores y los solicitados nuevos.

## 8. ProgramacionEfectiva

Pipeline:

```
NOMINALES
→ validar cardinalidad de targets
→ CANCELACIONES
→ REEMPLAZOS
→ ADICIONES
→ horario del salón final
→ maestros vigentes/fail-closed
→ duplicados y solapes globales
→ filtro de consulta
→ orden determinista
```

Fail-closed:

- salón inexistente/inactivo;
- horario cerrado/no operativo;
- rango fuera del horario;
- instructor suspendido/eliminado;
- rol de instructor ausente en el salón final;
- actividad inexistente/inactiva;
- especialidad ausente;
- actividad no ofrecida por el salón.

El candidato se omite y se registra una señal técnica estructurada. Corrupción de targets, duplicados o solapes persistidos lanza `ProgramacionInvarianteException`; no devuelve resultados parciales.

Scoping:

- Resolver globalmente la fecha antes de filtrar.
- Un reemplazo saliente desaparece del origen.
- Un reemplazo entrante aparece en destino.
- Igual para cambios de instructor.
- Nunca consultar `TurnoInstructor`.

## 9. Tests propuestos

Unitarios:

- aplicación pura de cancelación/reemplazo/adición;
- identidad, orden y no recorte;
- cardinalidad 0/1/>1;
- temporalidad con `Clock`;
- no-op real;
- deduplicación y orden de locks;
- fail-closed y señal técnica.

Servicio:

- writer interno y proyección antes de persistir;
- Policy A sobre los writers recurrentes existentes;
- stale discovery;
- recursos anteriores+nuevos en updates;
- creación/retiro que introduciría solape;
- `crearAsignacion` toma salón e instructor antes de validar.

PostgreSQL:

- migración real V46→V47;
- auditoría aborta ante datos solapados;
- EXCLUDE;
- CHECKs;
- unique parcial;
- múltiples inactivas;
- FKs;
- ausencia de FK a serie;
- identidad asignada de adición;
- traducción inequívoca de constraints.

Concurrencia:

- mismo target;
- mismo instructor mismo salón;
- mismo instructor cross-salon;
- recurrente contra ajuste en ambos órdenes;
- update que cambia instructor/salón;
- swap cross-salon sin deadlock;
- lock order independiente del request;
- relectura bajo locks.

Arquitectura/dark-launch:

- no controller `programacion`;
- no dependencia `programacion → calendario`;
- no bean F2D implementa `ValidadorImpactoExcepcionHorario`;
- el único adapter puntual productivo continúa siendo el existente;
- una fila de ajuste no veta una excepción legacy;
- `TurnoInstructor` y `ReservaService` no leen ajustes;
- sin frontend/mobile/cutover/fence.

Regresión:

- actualizar la expectativa V46/49 a V47/50;
- preservar la suite completa;
- baseline histórico inicial esperado: `493/493 PASS`;
- después de implementar, reportar el nuevo total real, no exigir que siga siendo 493.

## 10. Orden de implementación

1. Pre-flight, checkpoint y baseline.
2. Test de migración por etapas y V47.
3. Entidad, repositorio, identidad y constraints.
4. `SalonLocks`/`InstructorLocks`.
5. Hardening de `crearBloque`/`crearAsignacion` y Policy A.
6. Resolución nominal global por fecha.
7. Aplicador puro de ajustes.
8. Writer interno con `Clock`, locks, relectura y proyección.
9. `ProgramacionEfectiva`, horario final y fail-closed.
10. Tests PostgreSQL/concurrencia/arquitectura.
11. Suite completa, diff y checkpoint `IMPLEMENTADA_EN_REVIEW`.
12. Revisión adversarial independiente.

## 11. Archivos previstos

Crear:

- `src/main/resources/db/migration/V47__programacion_ajustes_fecha.sql`
- `programacion/entidad/AjusteProgramacionFecha.java`
- `programacion/repositorio/AjusteProgramacionFechaRepository.java`
- records/excepciones bajo `programacion/dominio/`
- servicios internos de nominales, aplicación de ajustes, Policy A y programación efectiva
- `ubicaciones/servicio/SalonLocks.java`
- `usuarios/servicio/InstructorLocks.java`
- traductor estricto del unique de ajustes
- tests unitarios, PostgreSQL, concurrencia y arquitectura
- `auditoria/fase-2d2-implementacion-dark-launch-ajustes-programacion-fecha.md`

Modificar:

- `AsignacionRepository.java`
- `BloqueProgramacionRepository.java`, sólo si las consultas nominales/Policy A lo requieren
- `UsuarioRepository.java`
- `BloqueProgramacionService.java`
- tests actuales de programación, especialmente la expectativa que hoy afirma que `crearAsignacion` no toma `SalonLock`
- expectativa Flyway V46/49
- documentación/comentario del traductor de EXCLUDE de horario si conserva la afirmación hoy obsoleta de que existe un único EXCLUDE en todo el esquema

No tocar:

- controllers;
- `TurnoInstructor`;
- `TurnoInstructorService`;
- `Reserva`;
- `ReservaService`;
- `SalonHorarioExcepcionService`;
- frontend/mobile;
- canónicos de cierre;
- fence/cutover;
- permisos/API pública;
- writers maestros.

## 12. Riesgos / decisiones abiertas

**NINGUNA**

Las adaptaciones físicas quedaron cerradas:

- V47 está disponible.
- La entidad de ajuste tendrá UUID asignable propio.
- No se añade dependencia.
- No se introduce controller.
- No se modifica ningún writer legacy.
- La señal fail-closed será síncrona/local y estructurada, sin broker ni outbox.

## 13. Intervención F2D.2 preparada

# FeelingPilates — F2D.2

## Implementación dark-launch de ajustes puntuales

### 1. Objetivo

Implementar internamente el diseño aprobado de ajustes puntuales de programación por fecha sobre `BloqueProgramacion + Asignacion`.

La entrega queda en:

```
F2D.2:
IMPLEMENTADA_EN_REVIEW
```

No cerrada, no productiva y sin commit inicial.

Debe soportar internamente:

- `CANCELACION`;
- `REEMPLAZO`;
- `ADICION`;
- resolución nominal;
- composición de `ProgramacionEfectiva`;
- Policy A inversa;
- locking cross-salon/cross-instructor;
- fail-closed;
- constraints PostgreSQL;
- pruebas unitarias, de integración, PostgreSQL, concurrencia y arquitectura.

### 2. Base exacta

Repositorio:

```
/Users/jesusaldaircruzortiz/Desktop/Feelingpilates/feelingpilates
```

Branch:

```
operacion/excepciones-horario-fecha
```

HEAD local y remoto esperado:

```
6a8ffaa104de9a6b707982e679e78cda8aeb433c
```

Ahead/behind:

```
0 / 0
```

Working tree:

```
LIMPIO
```

Checkpoint aprobado:

```
auditoria/fase-2d1-diseno-ajustes-programacion-fecha.md
```

SHA-256:

```
58af39f41b3bc089ebbd4ec67f684e270087ddf4eb695f2c7b55276d0aff352e
```

Última Flyway:

```
V46__horario_operacion_drop_unique_dia.sql
```

V47 debe continuar libre.

### 3. Pre-flight obligatorio

Ejecutar:

```
git branch --show-current
git rev-parse HEAD
git status --short
git status --branch --short

git fetch origin

git rev-parse HEAD
git rev-parse origin/operacion/excepciones-horario-fecha
git rev-list --left-right --count \
  origin/operacion/excepciones-horario-fecha...HEAD

shasum -a 256 \
  auditoria/fase-2d1-diseno-ajustes-programacion-fecha.md

find src/main/resources/db/migration \
  -maxdepth 1 -type f -name 'V*.sql' -print | sort -V | tail

rg -n \
  "AjusteProgramacionFecha|ProgramacionEfectiva|programacion_ajuste_fecha|InstructorLocks|ImpactoAjustesEnExcepcionHorario" \
  src || true
```

Leer el handoff, los canónicos y toda la cadena F2D.1 indicada en `HANDOFF-F2D2.md`.

Después ejecutar el baseline:

```
./mvnw test
```

Esperado histórico:

```
493 tests
0 failures
0 errors
0 skipped
BUILD SUCCESS
```

Si el total difiere pero la suite está verde, reportar y detenerse antes de modificar para reconciliar el baseline. No reinterpretarlo silenciosamente.

### 4. Scope permitido

- Una migration V47.
- Modelo, persistencia y services internos de ajustes.
- Nuevos value objects/records de programación.
- Consultas internas de nominales.
- Hardening de los writers actuales de `BloqueProgramacion + Asignacion`.
- Locks sobre salón/instructor.
- Policy A inversa.
- `ProgramacionEfectiva`.
- Fail-closed read-time.
- Tests.
- Checkpoint de implementación F2D.2.

No añadir dependencias.

### 5. Scope prohibido

No crear ni modificar:

- controllers o endpoints;
- DTOs HTTP;
- frontend;
- mobile;
- `TurnoInstructor`, su service, repositorios o controller;
- `Reserva`, `ReservaService` o repositorios de reserva;
- adapters sobre writers legacy;
- `ImpactoAjustesEnExcepcionHorario`;
- beans F2D de `ValidadorImpactoExcepcionHorario`;
- cutover;
- fence `LEGACY/MIGRANDO/NUEVA`;
- migración de datos legacy;
- writers maestros de salón, usuario, actividad, rol o especialidad;
- API/permisos futuros;
- sesiones, capacidad, recursos, broker u outbox.

No commit y no push.

### 6. V47

Crear:

```
src/main/resources/db/migration/V47__programacion_ajustes_fecha.sql
```

Responsabilidades exclusivas:

1. auditar y endurecer vigencias activas de `programacion_asignacion`;
2. crear `programacion_ajuste_fecha`.

No crear nuevamente `btree_gist`; ya existe desde V44.

Antes del `ALTER TABLE`, incluir una comprobación SQL que detecte pares activos de la misma `serie_id` con rangos solapados. Ante alguno, la migración debe lanzar excepción y abortar. No borrar, cerrar, desactivar ni corregir filas.

Añadir:

```
ALTER TABLE programacion_asignacion
    ADD CONSTRAINT ex_programacion_asignacion_serie_vigencia
        EXCLUDE USING gist (
            serie_id WITH =,
            daterange(vigente_desde, vigente_hasta, '[]') WITH &&
        )
        WHERE (activo);
```

Crear la tabla conforme al checkpoint:

```
CREATE TABLE programacion_ajuste_fecha (
    id                           UUID PRIMARY KEY,
    tipo                         VARCHAR(16) NOT NULL,
    fecha                        DATE NOT NULL,
    asignacion_serie_id          UUID,
    salon_resultado_id           UUID REFERENCES salon (id),
    instructor_resultado_id      UUID REFERENCES usuario (id),
    tipo_actividad_resultado_id  UUID REFERENCES tipo_actividad (id),
    hora_inicio_resultado        TIME WITHOUT TIME ZONE,
    hora_fin_resultado           TIME WITHOUT TIME ZONE,
    activo                       BOOLEAN NOT NULL DEFAULT true,
    creado_en                    TIMESTAMPTZ NOT NULL DEFAULT now(),
    actualizado_en               TIMESTAMPTZ NOT NULL DEFAULT now()
);
```

Añadir los CHECKs aprobados:

- tipo limitado a `CANCELACION`, `REEMPLAZO`, `ADICION`;
- cancelación con target y sin resultado;
- reemplazo con target y snapshot completo;
- adición sin target y con snapshot completo;
- `hora_fin_resultado > hora_inicio_resultado` cuando existe resultado.

Índice único parcial:

```
CREATE UNIQUE INDEX idx_programacion_ajuste_target_activo
    ON programacion_ajuste_fecha (asignacion_serie_id, fecha)
    WHERE activo AND tipo IN ('CANCELACION', 'REEMPLAZO');
```

Índices de lectura:

```
salon_resultado_id + fecha, parcial activo
instructor_resultado_id + fecha, parcial activo
fecha, parcial activo
```

No añadir:

- FK a `serie_id`;
- salón origen persistido;
- unique por salón/fecha/hora;
- trigger;
- sentinel temporal.

### 7. Modelo e identidad

Crear `AjusteProgramacionFecha`.

Campos exactamente equivalentes al DDL. Tipo mediante enum string.

No reutilizar sin adaptación `EntidadBase`: su `@GeneratedValue` contradice el UUID asignado por el caller para adiciones.

La entidad debe declarar:

- `@Id UUID id` sin generación automática JPA;
- `creadoEn`;
- `actualizadoEn`;
- timestamps equivalentes a la convención existente.

El service genera un UUID físico para cancelaciones/reemplazos nuevos. Para adiciones utiliza el `ajusteId` recibido.

Reglas:

- no reactivar filas inactivas;
- target recurrente retirado se recrea con nueva fila física;
- adición retirada exige nuevo UUID;
- no-op real no llama `save`, `saveAndFlush` ni cambia timestamp;
- update conserva identidad.

### 8. Repositorios y nominales

Crear `AjusteProgramacionFechaRepository` con consultas para:

- ajuste target activo por `serieId + fecha`;
- ajustes activos de una fecha;
- ajuste por ID activo;
- ajustes target activos de una serie/rango;
- resultados activos por salón/instructor/fecha cuando se necesiten para writer/locks.

Añadir en programación una proyección nominal nativa que devuelva en una sola consulta:

```
fecha
serieId
asignacionVersionId
bloqueVersionId
salonId
instructorId
tipoActividadId
horaInicio
horaFin
```

Debe exigir:

- asignación activa;
- bloque activo;
- ambas vigencias contienen `D`;
- día del bloque coincide con `D`;
- relación bloque/asignación real.

No filtrar por horario operativo ni maestros en esta consulta.

Usar la conversión compartida `DiaSemanaOperacion.desde`.

### 9. Locks

Crear:

```
ubicaciones.servicio.SalonLocks
usuarios.servicio.InstructorLocks
```

Ambos:

- reciben `Collection<UUID>`;
- rechazan/null según contrato;
- deduplican;
- ordenan con el mismo comparador determinista;
- bloquean uno a uno;
- usan transacción `Propagation.MANDATORY`;
- devuelven las entidades bloqueadas si resulta útil;
- no usan locks JVM/advisory.

`SalonLocks` debe reutilizar el lock/repositorio existente.

Añadir a `UsuarioRepository`:

```
bloquearParaActualizar(UUID)
```

con `PESSIMISTIC_WRITE`, equivalente al patrón de `SalonRepository`.

Orden global obligatorio:

```
SALONES → INSTRUCTORES
```

Nunca adquirir un salón después de haber adquirido un instructor.

Para updates/retiros, el lock set incluye la unión de recursos persistidos anteriores y solicitados nuevos.

### 10. Hardening recurrente y Policy A

Modificar únicamente los writers actuales:

```
BloqueProgramacionService.crearBloque
BloqueProgramacionService.crearAsignacion
```

`crearBloque` debe usar `SalonLocks`.

`crearAsignacion`:

1. valida sintaxis pura;
2. hace discovery mínimo del bloque;
3. deriva salón e instructor;
4. bloquea salón;
5. bloquea instructor;
6. relee bloque;
7. aborta si cambió identidad/lock set;
8. relee maestros y programación;
9. valida especialidad/oferta/rol;
10. valida contención y solape global;
11. aplica Policy A;
12. persiste en la misma transacción.

Eliminar/corregir el test actual que afirma que `crearAsignacion` no toma `SalonLock`.

Policy A para los entry points existentes:

- una creación de asignación que afecte un ajuste activo de hoy/futuro debe dejar exactamente un target;
- 0 o más de 1 después de proyectar: rechazar;
- no modificar el ajuste;
- no reasignar;
- no cancelar automáticamente.

La creación aislada de un bloque sin asignaciones no cambia cardinalidad de targets. No introducir writers de update/desactivación sólo para ampliar scope.

Si se descubre otro writer real de `programacion_bloque` o `programacion_asignacion`, STOP.

### 11. Aplicador de ajustes

Implementar una función/componente puro que reciba:

- nominales de una fecha;
- ajustes activos de esa fecha.

Debe:

1. indexar nominales por `serieId`;
2. fallar si una serie tiene más de una nominal;
3. exigir exactamente un target para cada cancelación/reemplazo;
4. eliminar canceladas;
5. sustituir reemplazadas por snapshot;
6. añadir adiciones;
7. conservar referencias aprobadas.

No consulta repositorios, reloj, horario ni maestros.

Identidad:

```
RECURRENTE/REEMPLAZO → SERIE_ASIGNACION + serieId + fecha
ADICION              → AJUSTE + ajusteId + fecha
```

### 12. Writer interno de ajustes

Crear un service interno, sin controller.

Operaciones mínimas:

- guardar/actualizar ajuste de una serie+fecha;
- retirar ajuste de una serie+fecha;
- guardar/actualizar adición por `ajusteId`;
- retirar adición por `ajusteId`.

Usar `Clock` inyectado:

- pasado: rechazo;
- hoy: mutable;
- futuro: mutable.

Para target recurrente:

```
validación pura
→ discovery nominal
→ discovery ajuste activo
→ derivar recursos anteriores+nuevos
→ SalonLocks
→ InstructorLocks
→ releer nominal y ajuste
→ comparar snapshots/lock set
→ proyectar fecha completa
→ validar
→ saveAndFlush/no-op
```

Si cambia discovery:

```
CONFLICTO_LOCK_SET_DESACTUALIZADO
```

No reintentar dentro de la misma transacción.

Para adiciones nuevas no hay discovery nominal. Para update/retiro sí se relee el estado persistido bajo los locks derivados de recursos anteriores+nuevos.

Antes de persistir cualquier mutación, proyectar la programación completa de esa fecha y comprobar:

- resultado dentro del horario efectivo final;
- maestros válidos;
- sin duplicado operativo;
- sin solape del mismo instructor, incluso cross-salon.

Cancelación:

- exige target nominal;
- no exige horario operativo;
- puede cancelar una nominal que sería omitida;
- no valida snapshot resultado.

Reemplazo/adición:

- snapshot completo;
- horario sólo del salón resultado;
- origen cerrado no bloquea un destino válido;
- sin recorte automático.

Usar `saveAndFlush` dentro de un traductor que sólo convierta `idx_programacion_ajuste_target_activo` cuando el SQLSTATE y nombre de constraint sean inequívocos. Cualquier otra integridad se relanza.

### 13. ProgramacionEfectiva

Crear service interno con:

```
List<OcurrenciaEfectiva> porSalonYFecha(UUID salonId, LocalDate fecha);
List<OcurrenciaEfectiva> porInstructorYFecha(UUID instructorId, LocalDate fecha);
```

Resolver globalmente toda la fecha antes de filtrar.

Orden:

```
NOMINAL
→ AJUSTES
→ HORARIO FINAL
→ MASTER-DATA
→ INVARIANTES
→ FILTRO
→ ORDEN
```

Para cada candidato:

- consultar `HorarioEfectivoSalon` del salón final;
- omitir cerrado/no operativo/fuera de rango;
- validar salón activo;
- instructor activo;
- rol `INSTRUCTOR` global o del salón final;
- actividad activa;
- especialidad;
- oferta del salón.

Una omisión fail-closed debe emitir señal técnica local y estructurada con:

```
causa estable
referencia
fecha
salonId
instructorId
actividadId
```

Usar SLF4J o un pequeño port inyectable con implementación SLF4J. No introducir eventos, broker, outbox ni persistencia diagnóstica.

Después de omitir inválidos:

- detectar duplicado exacto por salón/instructor/actividad/fecha/inicio/fin;
- detectar solape del mismo instructor entre salones;
- permitir adyacencia;
- ante corrupción persistida lanzar `ProgramacionInvarianteException`;
- no deduplicar ni devolver parcialmente.

Orden final:

```
horaInicio
horaFin
instructorId
referencia
```

No usar `Clock`.

No importar ni consultar `calendario`, `TurnoInstructor` o `Reserva`.

### 14. Errores estables

Implementar al menos:

```
AJUSTE_PROGRAMACION_EN_EL_PASADO
AJUSTE_PROGRAMACION_FORMA_INVALIDA
ASIGNACION_OBJETIVO_NO_EXISTE
AJUSTE_FUERA_DE_HORARIO_EFECTIVO
SALON_NO_OPERATIVO_EN_FECHA
INSTRUCTOR_CON_PROGRAMACION_TRASLAPADA
OCURRENCIA_EFECTIVA_DUPLICADA
CONFLICTO_AJUSTE_PROGRAMACION
CONFLICTO_LOCK_SET_DESACTUALIZADO
AJUSTE_PROGRAMACION_NO_EXISTE
```

`ProgramacionInvarianteException` debe transportar código estable y referencia.

No añadir endpoints ni modificar el handler HTTP por errores que sólo puede producir infraestructura interna.

### 15. Estrategia de tests

#### A. Unitarios

Crear tests para:

- las tres formas válidas y todas las híbridas inválidas;
- cancelación sólo en D y reaparición D+7;
- reemplazo de cada campo;
- adición;
- identidad;
- no recorte;
- cardinalidad 0/1/>1;
- orden determinista;
- no-op real;
- pasado/hoy/futuro con `Clock`;
- locks deduplicados/ordenados;
- stale discovery;
- fail-closed y señal.

#### B. Servicio

Cubrir:

- `crearAsignacion` adquiere salón antes de instructor y antes de lecturas decisorias;
- conflictividad recurrente cross-salon;
- Policy A;
- writer puntual proyecta antes de guardar;
- retiro que restauraría una nominal conflictiva;
- update incluye recursos previos+nuevos;
- ningún rechazo modifica templates.

#### C. PostgreSQL

Crear migración real V46→V47 y persistencia V47:

- V47 aborta ante solape preexistente sin cambiar datos;
- EXCLUDE rechaza solape activo;
- vigencias consecutivas sin fecha compartida pasan;
- inactivas solapadas pasan;
- CHECKs;
- unique target;
- recreate;
- FKs;
- sin FK a serie;
- UUID asignado de adición;
- metamodelo JPA;
- Flyway actual `47`, migraciones aplicadas `50`.

Actualizar `ProgramacionPersistenciaTest`.

#### D. Concurrencia

Usar PostgreSQL real, dos transacciones, `TransactionTemplate`, latches y ambos órdenes:

- mismo target;
- mismo instructor mismo salón;
- mismo instructor cross-salon;
- recurrente vs ajuste;
- cambio instructor;
- cambio salón;
- swap cross-salon;
- lock order inverso en requests;
- stale discovery.

No utilizar sleeps como mecanismo de sincronización.

#### E. Arquitectura/dark-launch

Crear guards que demuestren:

- ninguna clase `programacion` es `@RestController`;
- ningún bean F2D implementa `ValidadorImpactoExcepcionHorario`;
- `programacion` no depende de `calendario`;
- `SalonHorarioExcepcionService` conserva sólo adapters productivos preexistentes;
- una fila conflictiva en `programacion_ajuste_fecha` no veta guardar/cancelar una excepción legacy;
- `TurnoInstructorService` y `ReservaService` no importan/inyectan F2D;
- no existe `ImpactoAjustesEnExcepcionHorario`;
- no hay `LocalDate.now()` directo en writers nuevos.

#### F. Mutaciones A–O

Cada mutación debe tener detector explícito:

- A: nominal antes de reemplazo.
- B: Asignacion participa en locks.
- C: target cero.
- D: EXCLUDE.
- E: relectura/lock set.
- F: Reserva legacy ausente.
- G: blocker de cutover documentado, no implementado.
- H: duplicado efectivo.
- I: destino abierto después de origen cerrado.
- J: fail-closed de maestros.
- K: sin FK serie.
- L: unique target.
- M: orden global.
- N: sin integración productiva legacy.
- O: sin controller.

### 16. Validaciones y comandos

Ejecutar incrementalmente:

```
./mvnw -Dtest='*Programacion*Persistencia*,*MigracionV46V47*' test
./mvnw -Dtest='*BloqueProgramacionServiceTest,*AjusteProgramacionFechaServiceTest,*ProgramacionEfectivaTest' test
./mvnw -Dtest='*Programacion*Concurrencia*' test
./mvnw -Dtest='*DarkLaunch*Arquitectura*,*DarkLaunch*Integracion*' test
./mvnw test
```

Después:

```
git diff --check
git status --short
git diff --stat
git diff --name-only

rg -n "@RestController|@RequestMapping" \
  src/main/java/com/feelingpilates/programacion || true

rg -n \
  "TurnoInstructor|Reserva|ImpactoAjustesEnExcepcionHorario|ValidadorImpactoExcepcionHorario" \
  src/main/java/com/feelingpilates/programacion || true

rg -n "LocalDate\\.now\\(|LocalTime\\.now\\(" \
  src/main/java/com/feelingpilates/programacion || true
```

Crear el checkpoint:

```
auditoria/fase-2d2-implementacion-dark-launch-ajustes-programacion-fecha.md
```

Debe registrar base, diff, migration, tests, conteos reales, invariantes, ausencia de integración productiva y estado:

```
F2D.2:
IMPLEMENTADA_EN_REVIEW
```

No modificar todavía los canónicos para declarar cierre.

### 17. STOP conditions

Detener sin limpiar ni corregir si:

- branch/HEAD/remoto/working tree difieren;
- checkpoint SHA difiere;
- V47 ya existe o aparece una migration posterior;
- baseline falla o cambia sin explicación;
- existe implementación F2D.2 inesperada;
- hay datos activos solapados y sería necesario corregirlos;
- aparece otro writer recurrente no inventariado;
- el trabajo exige tocar `TurnoInstructor`, Reserva o un writer legacy;
- hace falta controller, frontend/mobile, fence o cutover;
- hace falta modificar writers maestros;
- hace falta una dependencia nueva;
- no se puede ejecutar PostgreSQL/Testcontainers;
- una constraint requiere reinterpretar el diseño;
- no puede cerrarse TOCTOU con el protocolo aprobado;
- surge una decisión arquitectónica nueva.

No usar reset, clean, stash, pull, merge o rebase para superar un STOP.

### 18. Salida obligatoria

Reportar:

```
# F2D.2 — Implementación dark-launch

Branch:
...

Base:
...

Checkpoint SHA:
...

Baseline inicial:
...

V47:
...

Auditoría de datos V47:
PASS / STOP

AjusteProgramacionFecha:
IMPLEMENTADO / NO

ProgramacionEfectiva:
IMPLEMENTADA / NO

Policy A:
IMPLEMENTADA / NO

SalonLocks:
...

InstructorLocks:
...

Orden global:
SALONES → INSTRUCTORES

TOCTOU:
CERRADO / ABIERTO

Fail-closed:
...

TurnoInstructor modificado:
NO

Reserva legacy modificada:
NO

Controllers nuevos:
NINGUNO

Consumers productivos nuevos:
NINGUNO

ImpactoAjustesEnExcepcionHorario:
NO IMPLEMENTADO

Fence:
NO IMPLEMENTADO

Cutover:
NO IMPLEMENTADO

Tests unitarios:
...

Tests servicio:
...

Tests PostgreSQL:
...

Tests concurrencia:
...

Tests arquitectura:
...

Suite completa:
<total>/PASS, failures, errors, skipped

Mutaciones:
15/15 DETECTADAS / ...

git diff --check:
PASS / FAIL

Archivos creados:
...

Archivos modificados:
...

Working tree:
...

Commit:
NO CREADO

Push:
NO REALIZADO

F2D.2:
IMPLEMENTADA_EN_REVIEW / BLOQUEADA
```

No hacer commit. Detenerse para revisión adversarial.

## 14. Estado final

Working tree:
**LIMPIO**

Archivos modificados:
**NINGUNO**

Tests:
**NO EJECUTADOS**

Commit:
**NO CREADO**

Push:
**NO REALIZADO**

## 15. Veredicto

**A. F2D.2 PREPARADA — INTERVENCIÓN LISTA PARA REVISIÓN**