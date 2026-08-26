# FeelingPilates — F2D.2.2

P0 re-review:
`0`

P1 re-review:
`1`

P2 re-review:
`0`

P1 restante:
`CAMINO BIFURCADO DEL PERDEDOR ajusteId`

Corregido:
**SÍ**

RAMA A:
`IDENTIDAD VISIBLE EN RELECTURA → ABORTAR INMEDIATAMENTE CON CONFLICTO_AJUSTE_PROGRAMACION`

Persist en RAMA A:
**NO**

RAMA B:
`IDENTIDAD AUSENTE → PROYECTAR/VALIDAR → CONSTRUIR ENTIDAD NUEVA → EntityManager.persist → flush`

Backstop RAMA B:
Primary key física `programacion_ajuste_fecha_pkey`.

Constraint PK explícita:
`CONSTRAINT programacion_ajuste_fecha_pkey PRIMARY KEY (id)`

SQLSTATE:
`23505`

Error estable:
`CONFLICTO_AJUSTE_PROGRAMACION`

Retry interno:
**NO**

Retry externo:
Nueva transacción con discovery histórico completo y protocolo normal de update/no-op/rechazo.

PK presentada como lock:
**NO**

Test stale discovery:
**SÍ**

Test PK real:
**SÍ**

Test retry externo:
**SÍ**

Test PK exige 23505+constraint exacta:
**SÍ**

P1-1 identidad previo:
**CERRADO**

P2:
**RESUELTOS**

V47:
**PRESERVADO**

Policy A:
**PRESERVADA**

TOCTOU target/update:
**CERRADO**

ProgramacionEfectiva:
**PRESERVADA**

Dark launch:
**PRESERVADO**

P1 F2D.1:
**8/8 CERRADOS**

Mutaciones:
**15/15**

Decisiones abiertas:
**NINGUNA**

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

F2D.2:
**NO INICIADA**

## INTERVENCIÓN F2D.2 CORREGIDA

# FeelingPilates — F2D.2

## Implementación dark-launch de ajustes puntuales

### 1. Objetivo

Implementar internamente el diseño aprobado de ajustes puntuales de programación por fecha sobre:

```
BloqueProgramacion
+
Asignacion
```

La entrega debe quedar en:

```
F2D.2:
IMPLEMENTADA_EN_REVIEW
```

No queda cerrada ni productiva. No hacer commit inicialmente.

Debe implementar:

- `CANCELACION`;
- `REEMPLAZO`;
- `ADICION`;
- persistencia e identidad;
- resolución nominal;
- `ProgramacionEfectiva`;
- Policy A inversa;
- locks por salón e instructor;
- protección TOCTOU;
- fail-closed;
- constraints PostgreSQL;
- tests unitarios, de servicio, persistencia, concurrencia y arquitectura.

### 2. Base exacta

Repositorio:

```
/Users/jesusaldaircruzortiz/Desktop/Feelingpilates/feelingpilates
```

Branch:

```
operacion/excepciones-horario-fecha
```

HEAD local/remoto esperado:

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

Estados iniciales:

```
F2D.1:
DISEÑO_APROBADO / CERRADA / PUBLICADA

F2D.2:
NO INICIADA
```

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

Leer el handoff, los canónicos y la cadena F2D.1 indicada en `HANDOFF-F2D2.md`.

Ejecutar el baseline antes de modificar:

```
./mvnw test
```

Baseline histórico esperado:

```
493 tests
0 failures
0 errors
0 skipped
BUILD SUCCESS
```

Si falla o cambia sin explicación verificable, detenerse antes de modificar.

### 4. Autoridad y dark launch

Durante F2D.2:

```
TurnoInstructor = autoridad productiva legacy
programacion_*   = infraestructura interna aislada
```

Ningún estado exclusivo de la programación nueva puede permitir, rechazar, modificar, ocultar o transformar un flujo productivo legacy.

F2D.2 no incluye:

- controllers;
- endpoints o DTOs HTTP;
- frontend/mobile;
- consumers productivos;
- `TurnoInstructor`;
- `Reserva`;
- adapters sobre writers legacy;
- `ImpactoAjustesEnExcepcionHorario`;
- cutover;
- fence `LEGACY/MIGRANDO/NUEVA`;
- migración de datos legacy;
- permisos/API futura;
- sesiones, capacidad, broker u outbox.

No modificar `SalonHorarioExcepcionService`.

### 5. Scope permitido

- Migration V47.
- Entidad y repositorio de ajustes.
- Componente interno de persistencia JPA.
- Records/value objects de programación.
- Consultas nominales.
- Service interno de ajustes.
- Hardening de los writers físicos actuales de `BloqueProgramacion + Asignacion`.
- `SalonLocks`.
- `InstructorLocks`.
- Policy A.
- `ProgramacionEfectiva`.
- Port diagnóstico interno e implementación SLF4J.
- Traductores estrictos de constraints.
- Tests.
- Checkpoint F2D.2.

No añadir dependencias.

### 6. V47

Crear:

```
src/main/resources/db/migration/V47__programacion_ajustes_fecha.sql
```

Responsabilidades:

1. auditar y endurecer vigencias activas de `programacion_asignacion`;
2. crear `programacion_ajuste_fecha`.

No volver a crear `btree_gist`.

#### 6.1 Pre-auditoría

Antes del `ALTER TABLE`, comprobar que no existan dos filas activas de la misma `serie_id` con rangos solapados:

```
daterange(vigente_desde, vigente_hasta, '[]')
```

Si existe un par:

- abortar la migración;
- no corregir;
- no cerrar vigencias;
- no desactivar;
- no eliminar.

#### 6.2 EXCLUDE

```
ALTER TABLE programacion_asignacion
    ADD CONSTRAINT ex_programacion_asignacion_serie_vigencia
        EXCLUDE USING gist (
            serie_id WITH =,
            daterange(vigente_desde, vigente_hasta, '[]') WITH &&
        )
        WHERE (activo);
```

Debe permitir vigencias consecutivas sin fecha compartida y rechazar cualquier fecha común.

`vigente_hasta NULL` representa extremo superior abierto. No usar sentinels.

#### 6.3 Tabla y PK explícita

```
CREATE TABLE programacion_ajuste_fecha (
    id                           UUID NOT NULL,
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
    actualizado_en               TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT programacion_ajuste_fecha_pkey
        PRIMARY KEY (id)
);
```

La única verdad física para la PK será:

```
programacion_ajuste_fecha_pkey
```

Código y tests deben usar exactamente ese nombre.

#### 6.4 CHECKs

Añadir:

```
chk_programacion_ajuste_tipo
chk_programacion_ajuste_forma
chk_programacion_ajuste_rango
```

Formas:

- `CANCELACION`: serie no nula y campos resultado nulos.
- `REEMPLAZO`: serie no nula y snapshot resultado completo.
- `ADICION`: serie nula y snapshot resultado completo.
- Si existe resultado: `hora_fin_resultado > hora_inicio_resultado`.

No permitir estados híbridos.

#### 6.5 Índices

```
CREATE UNIQUE INDEX idx_programacion_ajuste_target_activo
    ON programacion_ajuste_fecha (asignacion_serie_id, fecha)
    WHERE activo AND tipo IN ('CANCELACION', 'REEMPLAZO');
```

Añadir índices parciales activos para:

- `salon_resultado_id + fecha`;
- `instructor_resultado_id + fecha`;
- `fecha`.

No añadir:

- FK a `asignacion_serie_id`;
- salón origen persistido;
- unique por salón/fecha/hora;
- trigger;
- upsert.

### 7. Entidad e identidad

Crear:

```
programacion.entidad.AjusteProgramacionFecha
```

Tipos:

```
CANCELACION
REEMPLAZO
ADICION
```

La entidad:

- no hereda `EntidadBase`;
- declara `@Id UUID id`;
- no usa `@GeneratedValue`;
- replica `creadoEn` con `@CreationTimestamp` y `updatable=false`;
- replica `actualizadoEn` con `@UpdateTimestamp`;
- usa columnas y nulabilidad equivalentes a `EntidadBase`;
- nunca reescribe `creadoEn`.

Identidad efectiva:

```
RECURRENTE/REEMPLAZO:
serieId + fecha

ADICION:
ajusteId + fecha
```

#### 7.1 Fecha inmutable

Para una adición activa existente:

```
requested.fecha == persisted.fecha
```

Si difieren, rechazar.

Mover una adición requiere:

```
retirar adición actual
→ crear nueva adición
→ nuevo ajusteId
```

#### 7.2 UUID histórico no reutilizable

Antes de crear una adición:

```
buscar por PK sin filtrar activo
```

Si existe:

- activa: sólo puede entrar al protocolo normal de update/no-op;
- inactiva: rechazar reutilización;
- nunca insertar otra fila;
- nunca reactivar;
- nunca sobrescribir mediante merge.

Un UUID retirado no vuelve a usarse.

### 8. Persistencia JPA

Crear un componente interno acotado, por ejemplo:

```
AjusteProgramacionFechaPersistence
```

Puede encapsular `EntityManager`, `persist` y `flush`. No crear una abstracción genérica.

#### 8.1 Create

Para una fila realmente nueva:

1. comprobar históricamente que la PK no existe;
2. completar el protocolo de locks y relectura;
3. construir una entidad nueva;
4. `EntityManager.persist`;
5. `flush` dentro del traductor aplicable.

No usar:

- `merge`;
- `repository.save` de una instancia detached;
- upsert;
- reactivación.

#### 8.2 Update

1. cargar la entidad activa;
2. mantenerla managed;
3. conservar `id`, `fecha` y `creadoEn`;
4. mutar únicamente campos editables;
5. `flush`.

No construir una entidad detached desde el comando.

#### 8.3 Retiro

1. cargar entidad activa managed;
2. establecer `activo=false`;
3. `flush`.

No eliminar físicamente.

#### 8.4 No-op

Si identidad y contenido coinciden:

- no `persist`;
- no `merge`;
- no `save`;
- no `flush` explícito;
- no dirty state;
- no cambio de `actualizadoEn`.

### 9. Repositorio

Crear `AjusteProgramacionFechaRepository` con consultas para:

- PK histórica, activa o inactiva;
- PK activa;
- target activo por `serieId + fecha`;
- ajustes activos de una fecha;
- targets activos de una serie/rango;
- resultados activos por salón/instructor/fecha cuando sean necesarios.

La consulta histórica nunca debe filtrar `activo`.

### 10. Consulta nominal

Obtener una proyección lógicamente completa con:

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

Preservar:

- asignación activa;
- bloque activo;
- ambas vigencias contienen D;
- día del bloque coincide con D;
- relación bloque/asignación;
- cardinalidad 0/1/>1.

No imponer tecnología. Puede usarse JPQL, native query, projection Spring Data o lecturas coordinadas.

Si hay varias lecturas:

- no filtrar por horario entre ellas;
- no colapsar duplicados;
- no perder cardinalidad;
- mantener coherencia transaccional.

No filtrar por horario/master-data en la resolución nominal.

### 11. Locks

Crear:

```
ubicaciones.servicio.SalonLocks
usuarios.servicio.InstructorLocks
```

Ambos:

- reciben colecciones;
- deduplican;
- ordenan determinísticamente;
- adquieren locks pesimistas;
- exigen `Propagation.MANDATORY`;
- no dependen del orden del request.

`SalonLocks` reutiliza la infraestructura existente.

Añadir en `UsuarioRepository` una query `PESSIMISTIC_WRITE`.

Orden global:

```
SALONES, UUID ascendente
→ INSTRUCTORES, UUID ascendente
```

Para updates/retiros usar la unión de recursos persistidos y solicitados.

No añadir:

- advisory locks;
- locks JVM;
- tabla de locks;
- lock global;
- fence;
- retry interno.

### 12. Hardening recurrente y Policy A

Modificar únicamente:

```
BloqueProgramacionService.crearBloque
BloqueProgramacionService.crearAsignacion
```

`crearBloque` utiliza `SalonLocks`.

`crearAsignacion`:

1. valida sintaxis;
2. descubre bloque;
3. deriva salón/instructor;
4. bloquea salones;
5. bloquea instructores;
6. relee bloque;
7. compara discovery/lock set;
8. relee maestros y programación;
9. valida rol, especialidad y oferta;
10. valida contención;
11. valida solape global;
12. aplica Policy A;
13. persiste.

Corregir el test que actualmente afirma que `crearAsignacion` no toma lock de salón.

Policy A:

- proyectar ajustes activos afectados de hoy/futuro;
- exactamente un target: continuar;
- cero o más de uno: rechazar;
- nunca modificar automáticamente el ajuste.

No introducir writers recurrentes adicionales.

Si aparece otro writer físico real, STOP.

### 13. Aplicador puro

Crear un componente puro que reciba nominales y ajustes activos de una fecha.

Secuencia:

1. indexar nominales por `serieId`;
2. fallar ante más de una nominal;
3. exigir exactamente un target para cada cancelación/reemplazo;
4. aplicar cancelaciones;
5. aplicar reemplazos;
6. incorporar adiciones.

No consulta repositorios, reloj, horario ni maestros.

No deduplica.

### 14. Writer interno: clasificación inicial de adición

El service interno debe clasificar determinísticamente el estado histórico antes de elegir operación.

Para `guardarAdicion(ajusteId, fecha, snapshot)`:

#### Estado inicial activo

Si el discovery histórico encuentra una fila activa:

```
UPDATE/NO-OP
```

Aplicar el protocolo completo de update:

- fecha inmutable;
- recursos anteriores+nuevos;
- locks;
- relectura;
- comparación;
- proyección;
- validación;
- update managed o no-op.

#### Estado inicial inactivo

Si encuentra una fila inactiva:

```
RECHAZAR UUID HISTÓRICO NO REUTILIZABLE
```

No adquirir locks para intentar reactivarla. No persistir.

#### Estado inicial inexistente

Entrar exclusivamente al protocolo CREATE descrito en la sección siguiente.

No convertir una rama en otra dentro de la misma transacción salvo lo especificado expresamente para stale discovery: stale discovery siempre aborta.

### 15. CREATE de adición: protocolo determinista

Cuando el discovery histórico inicial devuelve `NO EXISTE`:

1. validar forma y temporalidad;
2. derivar salón/instructor solicitados;
3. adquirir `SalonLocks`;
4. adquirir `InstructorLocks`;
5. releer históricamente por PK exactamente una vez;
6. ejecutar obligatoriamente RAMA A o RAMA B según el resultado.

No existe elección del implementador.

#### RAMA A — identidad visible en relectura

Condición:

```
discovery inicial:
NO EXISTE

relectura bajo locks:
EXISTE
```

La fila puede estar activa o inactiva.

Comportamiento obligatorio:

```
ABORTAR INMEDIATAMENTE
CONFLICTO_AJUSTE_PROGRAMACION
```

No:

- llamar `persist`;
- construir otra entidad con ese UUID;
- incorporar otra entidad con la misma identidad al persistence context;
- convertir create en update;
- reutilizar la entidad encontrada;
- mutarla;
- continuar a proyección;
- intentar deliberadamente alcanzar el INSERT;
- hacer retry interno;
- interpretar el conflicto como éxito idempotente.

Esta rama es:

```
IDENTIDAD APARECIDA DESPUÉS DEL DISCOVERY
```

No necesita SQLSTATE ni `DataIntegrityViolationException`: el conflicto se conoce antes de persistir.

#### RAMA B — identidad ausente en relectura

Condición:

```
discovery inicial:
NO EXISTE

relectura bajo locks:
NO EXISTE
```

Comportamiento obligatorio:

1. continuar con proyección;
2. validar programación/maestros/horario;
3. construir una entidad nueva;
4. `EntityManager.persist`;
5. `flush`.

Después de la relectura ausente:

```
NO volver a consultar la PK antes de persist
```

Si una carrera de identidad todavía invisible existe, la PK actúa como backstop físico.

El perdedor se traduce únicamente cuando existe:

```
SQLSTATE 23505
AND
constraint programacion_ajuste_fecha_pkey
```

Resultado:

```
CONFLICTO_AJUSTE_PROGRAMACION
```

Después del fallo:

- abortar;
- no releer;
- no actualizar;
- no reintentar;
- no continuar con la sesión JPA.

### 16. Función exacta de la PK

No presentar la PK como lock.

La especificación es:

```
Relectura bajo locks:
detecta identidades concurrentes que ya son visibles.

PRIMARY KEY:
backstop físico definitivo para carreras todavía no visibles,
especialmente con recursos naturales disjuntos.
```

Ambos caminos producen `CONFLICTO_AJUSTE_PROGRAMACION`, pero por detectores técnicos diferentes:

```
RAMA A:
stale discovery visible, antes del INSERT.

RAMA B:
violación física 23505 + programacion_ajuste_fecha_pkey.
```

### 17. Update y retiro de adición

#### Update

1. discovery activo;
2. comprobar fecha inmutable;
3. derivar recursos anteriores+nuevos;
4. salones;
5. instructores;
6. relectura;
7. comparar identidad/snapshot/lock set;
8. volver a comprobar fecha;
9. contenido igual: no-op;
10. contenido distinto: proyectar/validar;
11. mutar entidad managed;
12. flush.

#### Retiro

1. discovery activo;
2. derivar recursos persistidos;
3. locks;
4. relectura;
5. comparar;
6. proyectar sin la adición;
7. validar;
8. `activo=false`;
9. flush.

La fila permanece histórica y el UUID queda inutilizable.

### 18. Target recurrente

Para cancelación/reemplazo:

```
validación
→ discovery nominal
→ discovery ajuste activo
→ recursos anteriores+nuevos
→ SalonLocks
→ InstructorLocks
→ relectura
→ comparación
→ proyección completa
→ validación
→ persist/update/retiro/no-op
```

Si cambia discovery:

```
CONFLICTO_LOCK_SET_DESACTUALIZADO
```

No reintentar dentro de la transacción.

Cancelación:

- exige target nominal;
- no consulta horario para validar la cancelación;
- no porta resultado;
- puede suprimir una nominal operativamente omitida.

Reemplazo:

- snapshot completo;
- usa horario del salón resultado;
- un origen cerrado no bloquea un destino válido.

### 19. Traducción estricta de constraints

Implementar detectores separados.

#### Target recurrente

```
SQLSTATE:
23505

Constraint/index:
idx_programacion_ajuste_target_activo

Resultado:
CONFLICTO_AJUSTE_PROGRAMACION
```

#### Identidad de adición — sólo RAMA B

```
SQLSTATE:
23505

Constraint:
programacion_ajuste_fecha_pkey

Resultado:
CONFLICTO_AJUSTE_PROGRAMACION
```

RAMA A no usa el traductor de PK.

No traducir:

- cualquier `23505`;
- EXCLUDE de asignaciones;
- FK;
- CHECK;
- otro unique;
- `DataIntegrityViolationException` sin nombre exacto.

Tras un error JDBC, relanzar y dejar rollback. No seguir usando la transacción.

### 20. Proyección y validación del writer

Antes de persistir una mutación válida, proyectar toda la fecha.

Validar:

- horario del salón final;
- salón activo;
- instructor activo;
- rol `INSTRUCTOR`;
- actividad activa;
- especialidad;
- oferta del salón;
- duplicado operativo;
- solape del instructor cross-salon.

Permitir adyacencia.

No recortar automáticamente.

### 21. ProgramacionEfectiva

Crear service interno:

```
List<OcurrenciaEfectiva> porSalonYFecha(UUID salonId, LocalDate fecha);
List<OcurrenciaEfectiva> porInstructorYFecha(UUID instructorId, LocalDate fecha);
```

Pipeline:

```
NOMINALES
→ TARGETS
→ CANCELACIONES
→ REEMPLAZOS
→ ADICIONES
→ HORARIO DEL SALÓN FINAL
→ MASTER-DATA
→ INVARIANTES
→ FILTRO
→ ORDEN
```

Resolver globalmente antes de filtrar.

Fail-closed:

- salón inexistente/inactivo;
- cerrado/no operativo;
- fuera de horario;
- instructor suspendido/eliminado;
- rol ausente;
- actividad inexistente/inactiva;
- especialidad ausente;
- actividad no ofrecida.

Después:

- duplicado exacto: invariante;
- solape mismo instructor: invariante;
- corrupción target: invariante;
- no devolver resultados parciales.

Orden:

```
horaInicio
horaFin
instructorId
referencia
```

No usa `Clock` ni consulta `calendario`.

### 22. Diagnóstico fail-closed

Crear port interno:

```
ProgramacionDiagnostico
```

con operación para registrar una omisión.

Datos:

- causa estable;
- referencia;
- fecha;
- salón;
- instructor;
- actividad.

Crear implementación productiva SLF4J.

No persistencia, broker, outbox, eventos ni API pública.

Los tests inyectan fake/spy determinista.

### 23. Errores estables

Utilizar como mínimo:

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

No añadir errores HTTP ni modificar `GlobalExceptionHandler`.

### 24. Tests de identidad/JPA

Añadir:

1. Update activo con misma fecha permitido.
2. Cambio de fecha rechazado.
3. Movimiento sólo mediante retiro+nuevo UUID.
4. UUID de fila inactiva rechazado.
5. Retiro+recreate mismo UUID rechazado.
6. Retiro+nuevo UUID permitido.
7. Create usa `EntityManager.persist` y genera INSERT.
8. Update managed no crea otra fila.
9. Update conserva `id`, `fecha` y `creadoEn`.
10. Retiro deja `activo=false`.
11. Nunca se reactiva.
12. No-op no escribe ni cambia timestamp.

### 25. TEST A — stale discovery visible

Test de service determinista.

Preparar:

```
primera lectura histórica:
NO EXISTE

relectura después de locks:
EXISTE
```

Representa que otra transacción creó y confirmó X entre ambas lecturas.

Comprobar:

- `CONFLICTO_AJUSTE_PROGRAMACION`;
- no se llama `persist`;
- no se llama al flush de creación;
- no se crea una segunda entidad con X;
- no se proyecta;
- no se convierte en update;
- no se muta la fila observada.

Cubrir fila aparecida activa e inactiva. Ambas abortan el CREATE.

### 26. TEST B — colisión física real de PK

Usar PostgreSQL real y dos transacciones.

Mismo:

```
ajusteId = X
```

Recursos:

```
salón A != salón B
instructor I1 != instructor I2
snapshots distinguibles
```

Coordinar con latches para que ambas transacciones completen:

1. discovery histórico inexistente;
2. locks disjuntos;
3. relectura histórica inexistente;

antes de permitir `persist/flush`.

No volver a consultar la PK después de esa barrera.

Liberar ambas hacia `persist/flush`.

Resultado obligatorio:

- una transacción gana;
- una falla;
- el perdedor falla específicamente por:
  - SQLSTATE `23505`;
  - constraint `programacion_ajuste_fecha_pkey`;
- el traductor produce `CONFLICTO_AJUSTE_PROGRAMACION`;
- queda exactamente una fila;
- snapshot íntegro del ganador;
- ninguna mezcla;
- ningún upsert;
- ningún update silencioso;
- ningún retry interno.

El test es inválido si el perdedor salió por RAMA A. Debe demostrar RAMA B y la traducción física de PK.

La coordinación puede ejercitar el componente real de persistencia dentro de `TransactionTemplate`; no introducir hooks productivos si el mismo protocolo puede probarse coordinando repositorios, locks y persistence component desde el test.

### 27. TEST C — retry externo

Después del escenario de TEST B, abrir una transacción nueva.

Discovery desde cero:

#### Fila ganadora activa

- validar fecha;
- derivar recursos persistidos+solicitados;
- tomar `SalonLocks`;
- tomar `InstructorLocks`;
- releer;
- decidir no-op/update/rechazo;
- nunca asumir éxito por el conflicto anterior.

#### Fila ganadora inactiva

- rechazar reutilización histórica;
- no persistir;
- no reactivar.

El test debe demostrar que el retry pertenece a una transacción nueva.

### 28. Otros tests PostgreSQL y concurrencia

Cubrir:

- migración real V46→V47;
- pre-auditoría aborta y conserva datos;
- EXCLUDE;
- vigencias consecutivas;
- CHECKs;
- unique target;
- filas inactivas;
- FKs;
- ausencia de FK serie;
- nombre explícito de PK en catálogo;
- Flyway V47/50;
- mismo target;
- mismo instructor mismo salón;
- mismo instructor cross-salon;
- recurrente contra ajuste;
- cambio de salón/instructor;
- swap sin deadlock;
- orden inverso;
- stale discovery de target.

Usar latches y transacciones independientes. No usar sleeps como mecanismo de sincronización.

### 29. Tests de arquitectura

Demostrar:

- ningún controller en `programacion`;
- ningún bean F2D implementa `ValidadorImpactoExcepcionHorario`;
- no existe `ImpactoAjustesEnExcepcionHorario`;
- `programacion` no depende de `calendario`;
- ajustes no vetan excepciones legacy;
- `TurnoInstructorService` no lee F2D;
- `ReservaService` no lee F2D;
- sin frontend/mobile;
- sin fence/cutover;
- writers usan `Clock`;
- resolver no usa `Clock`;
- no existe `merge` ni upsert en persistencia de ajustes.

### 30. Mutaciones A–O

Mantener:

```
15/15 DETECTADAS
```

- A: nominal antes del reemplazo.
- B: writers recurrentes participan en locks.
- C: target cero.
- D: EXCLUDE.
- E: relectura/lock set.
- F: Reserva legacy ausente.
- G: blocker documental futuro de cutover.
- H: duplicado efectivo.
- I: destino abierto tras origen cerrado.
- J: fail-closed.
- K: sin FK serie.
- L: unique target.
- M: orden global.
- N: sin integración legacy.
- O: sin controller.

G es evidencia documental, no un test runtime F2D.2.

TEST A/B/C son regresiones adicionales y no alteran la matriz.

### 31. Archivos esperados

Crear, ajustando sólo nombres no arquitectónicos:

```
src/main/resources/db/migration/V47__programacion_ajustes_fecha.sql

programacion/entidad/AjusteProgramacionFecha.java
programacion/repositorio/AjusteProgramacionFechaRepository.java
programacion/dominio/OcurrenciaNominal.java
programacion/dominio/OcurrenciaEfectiva.java
programacion/dominio/ReferenciaOcurrencia.java
programacion/dominio/ProgramacionInvarianteException.java

programacion/servicio/AjusteProgramacionFechaService.java
programacion/servicio/AjusteProgramacionFechaPersistence.java
programacion/servicio/ProgramacionNominal.java
programacion/servicio/AplicadorAjustesProgramacion.java
programacion/servicio/ProgramacionEfectiva.java
programacion/servicio/ProgramacionPolicyA.java
programacion/servicio/ProgramacionDiagnostico.java
programacion/servicio/ProgramacionDiagnosticoSlf4j.java
programacion/servicio/ProgramacionErrores.java
programacion/servicio/ConflictoAjusteProgramacionTranslator.java

ubicaciones/servicio/SalonLocks.java
usuarios/servicio/InstructorLocks.java
```

Modificar según necesidad:

```
AsignacionRepository.java
BloqueProgramacionRepository.java
UsuarioRepository.java
BloqueProgramacionService.java
tests actuales de programación
expectativas Flyway
```

Crear:

```
auditoria/fase-2d2-implementacion-dark-launch-ajustes-programacion-fecha.md
```

No actualizar canónicos para declarar cierre.

### 32. Orden de implementación

1. Pre-flight y baseline.
2. Tests de migración.
3. V47 con PK explícita.
4. Entidad asignada y persistence component.
5. Repositorio histórico/activo.
6. Tests identidad/JPA.
7. Locks.
8. Hardening recurrente y Policy A.
9. Nominales.
10. Aplicador puro.
11. Writer target/update/retiro.
12. CREATE determinista RAMA A/RAMA B.
13. Traductores estrictos.
14. TEST A.
15. TEST B.
16. TEST C.
17. `ProgramacionEfectiva`.
18. Diagnóstico.
19. Tests restantes.
20. Suite completa.
21. Checkpoint `IMPLEMENTADA_EN_REVIEW`.
22. Review adversarial.

### 33. Comandos de validación

```
./mvnw -Dtest='*MigracionV46V47*,*Programacion*Persistencia*,*Ajuste*Persistence*' test

./mvnw -Dtest='*BloqueProgramacionServiceTest,*AjusteProgramacionFechaServiceTest,*AplicadorAjustesProgramacionTest,*ProgramacionEfectivaTest' test

./mvnw -Dtest='*StaleDiscovery*,*AjusteId*Concurrencia*,*Programacion*Concurrencia*' test

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

rg -n "merge\\(|ON CONFLICT|on conflict" \
  src/main/java/com/feelingpilates/programacion \
  src/main/resources/db/migration/V47__programacion_ajustes_fecha.sql || true
```

Reportar el total real de tests. No exigir que permanezca en 493.

### 34. STOP conditions

Detener si:

- branch/HEAD/remoto/working tree difieren;
- checkpoint SHA difiere;
- V47 existe o hay una posterior;
- baseline falla o cambia sin reconciliación;
- existe F2D.2 inesperada;
- aparecen solapes activos;
- aparece otro writer recurrente;
- se requiere tocar legacy/controllers/frontend/mobile;
- se requiere fence/cutover;
- se requiere modificar writers maestros;
- se requiere nueva dependencia;
- no puede verificarse PostgreSQL;
- create exige merge/upsert;
- no puede nombrarse/verificarse la PK;
- el traductor sólo puede aceptar cualquier `23505`;
- se requeriría lock global/advisory/tabla de locks;
- se requeriría retry interno;
- RAMA A no puede abortar antes de persist;
- TEST B no consigue demostrar ambas relecturas ausentes antes del flush;
- surge una decisión arquitectónica nueva.

No limpiar ni reescribir el estado con reset/clean/stash/pull/merge/rebase.

### 35. Checkpoint de implementación

Crear:

```
auditoria/fase-2d2-implementacion-dark-launch-ajustes-programacion-fecha.md
```

Registrar:

- base;
- V47;
- PK explícita;
- identidad/fecha;
- persist/managed/flush;
- UUID histórico;
- RAMA A;
- RAMA B;
- traductor PK;
- retry externo;
- locks;
- Policy A;
- ProgramacionEfectiva;
- diagnóstico;
- tests y conteos;
- ausencia de integración legacy;
- mutaciones 15/15.

Estado:

```
F2D.2:
IMPLEMENTADA_EN_REVIEW
```

### 36. No commit

No ejecutar:

```
git add
git commit
git push
```

Esperar review adversarial.

### 37. Salida obligatoria

```
# F2D.2 — Implementación dark-launch

Branch:
...

Base:
...

Checkpoint F2D.1 SHA:
...

Baseline:
...

V47:
...

PK explícita:
programacion_ajuste_fecha_pkey

Pre-auditoría:
PASS / STOP

EXCLUDE:
...

AjusteProgramacionFecha:
IMPLEMENTADO / NO

Identidad adición:
ajusteId + fecha

Fecha:
INMUTABLE / ...

Movimiento:
RETIRO + NUEVA ADICION + NUEVO UUID / ...

UUID retirado:
NO REUTILIZABLE / ...

Create JPA:
EntityManager.persist / ...

Update:
MANAGED + FLUSH / ...

Merge/upsert:
NO / ...

RAMA A:
IDENTIDAD VISIBLE → CONFLICTO ANTES DE PERSIST / ...

Persist RAMA A:
NO / ...

RAMA B:
IDENTIDAD AUSENTE → PROYECCION → PERSIST/FLUSH / ...

Backstop RAMA B:
23505 + programacion_ajuste_fecha_pkey / ...

Retry interno:
NO

Retry externo:
NUEVA TRANSACCION + DISCOVERY COMPLETO / ...

ProgramacionEfectiva:
...

Policy A:
...

SalonLocks:
...

InstructorLocks:
...

Orden:
SALONES → INSTRUCTORES

Diagnóstico:
PORT INTERNO + SLF4J / ...

TurnoInstructor modificado:
NO

Reserva modificada:
NO

SalonHorarioExcepcionService modificado:
NO

Controllers:
NINGUNO

Consumers productivos:
NINGUNO

ImpactoAjustesEnExcepcionHorario:
NO

Fence:
NO

Cutover:
NO

Test A stale discovery:
PASS / FAIL

Test B PK real:
PASS / FAIL

Test B evidencia:
23505 + programacion_ajuste_fecha_pkey / ...

Test C retry externo:
PASS / FAIL

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

Suite:
<total>, failures, errors, skipped

Mutaciones:
15/15 / ...

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

Decisiones abiertas:
NINGUNA / ...

F2D.2:
IMPLEMENTADA_EN_REVIEW / BLOQUEADA
```

Detenerse. No hacer commit ni ejecutar F2D.2 fuera del dark launch.

## VEREDICTO

**A. F2D.2.2 COMPLETADA — INTERVENCIÓN LISTA PARA RE-REVIEW FINAL**