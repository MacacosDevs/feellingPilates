# FeelingPilates — F2D.2.1

P0 review:
`0`

P1 review:
`2`

P2 review:
`2`

P1-1 identidad adición:
**CORREGIDO**

Fecha inmutable:
**SÍ**

Movimiento de fecha:
**RETIRO + NUEVA ADICIÓN CON NUEVO UUID**

UUID retirado reutilizable:
**NO**

Persistencia create:
Entidad nueva con UUID asignado, comprobación histórica por PK y `EntityManager.persist`.

Persistencia update:
Entidad activa cargada como managed; fecha e identidad intactas; mutación de campos editables y `flush`.

Merge/upsert para create:
**NO**

P1-2 mismo ajusteId concurrente:
**CORREGIDO**

Mecanismo ganador:
**PRIMARY KEY**

Perdedor:
Abortar con `CONFLICTO_AJUSTE_PROGRAMACION` únicamente ante SQLSTATE `23505` y constraint `programacion_ajuste_fecha_pkey`.

Retry interno:
**NO**

Retry externo:
Nueva transacción desde discovery; distingue fila activa/inactiva, toma recursos anteriores+nuevos, relee y aplica el protocolo normal.

Constraint translation PK:
`23505 + programacion_ajuste_fecha_pkey`, sin traducción genérica de otros `23505`.

P2 query nativa:
**CORREGIDO**

Tecnología obligatoria:
**NINGUNA**

P2 señal técnica:
**CORREGIDO**

Implementación:
**PORT INTERNO + SLF4J**

Tests identidad añadidos:

- fecha inmutable;
- movimiento por retiro+nuevo UUID;
- UUID histórico no reutilizable;
- persist/update/retiro JPA;
- preservación de `id`, `fecha` y `creadoEn`;
- no reactivación;
- no-op real;
- conflicto concurrente de PK;
- retry externo completo.

Test concurrencia recursos disjuntos:
**SÍ**

Mutaciones:
**15/15 PRESERVADAS**

P2-1:
**RESUELTO**

P2-3:
**RESUELTO**

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

Si el baseline falla o cambia sin explicación verificable, detenerse antes de modificar.

### 4. Autoridad y dark launch

Durante toda F2D.2:

```
TurnoInstructor = autoridad productiva legacy
programacion_*   = infraestructura interna aislada
```

Ningún estado contenido exclusivamente en la programación nueva puede:

- permitir;
- rechazar;
- modificar;
- ocultar;
- transformar;

un flujo productivo legacy.

F2D.2 no incluye:

- controllers;
- endpoints;
- DTOs HTTP;
- frontend;
- mobile;
- consumers productivos;
- `TurnoInstructor`;
- `Reserva`;
- adapters sobre writers legacy;
- `ImpactoAjustesEnExcepcionHorario`;
- cutover;
- fence `LEGACY/MIGRANDO/NUEVA`;
- migración de datos legacy;
- permisos/API futura;
- sesiones;
- capacidad;
- broker;
- outbox.

No modificar `SalonHorarioExcepcionService`.

### 5. Scope permitido

- Una migración V47.
- Entidad y repositorio de ajustes.
- Componente interno de persistencia JPA asignada.
- Value objects y records de programación.
- Consultas nominales.
- Service interno de ajustes.
- Hardening de los writers físicos actuales de `BloqueProgramacion + Asignacion`.
- `SalonLocks`.
- `InstructorLocks`.
- Policy A inversa.
- `ProgramacionEfectiva`.
- Port interno de diagnóstico con implementación SLF4J.
- Tests.
- Checkpoint de implementación F2D.2.

No añadir dependencias.

### 6. V47

Crear:

```
src/main/resources/db/migration/V47__programacion_ajustes_fecha.sql
```

Tiene exactamente dos responsabilidades:

1. auditar y endurecer las vigencias activas de `programacion_asignacion`;
2. crear `programacion_ajuste_fecha`.

`btree_gist` ya existe desde V44. No volver a crearlo.

#### 6.1 Pre-auditoría

Antes del `ALTER TABLE`, comprobar que no existan dos filas activas de la misma `serie_id` cuyos `daterange(vigente_desde, vigente_hasta, '[]')` se intersecten.

Si existe cualquier par:

- lanzar excepción;
- abortar la migración;
- no corregir;
- no desactivar;
- no cerrar vigencias;
- no eliminar datos.

#### 6.2 EXCLUDE

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

Debe permitir:

```
2027-01-01 / 2027-01-31
2027-02-01 / NULL
```

Debe rechazar vigencias que compartan al menos una fecha.

`vigente_hasta NULL` conserva el extremo superior abierto. No usar sentinels.

#### 6.3 Tabla

Crear:

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

El nombre físico esperado de la PK generada por PostgreSQL es:

```
programacion_ajuste_fecha_pkey
```

Los traductores deben fijar este nombre mediante test de catálogo. Si PostgreSQL/Flyway materializa otro nombre, nombrar explícitamente la PK en V47 y usar ese nombre estable en código y tests.

#### 6.4 CHECKs

Añadir:

```
chk_programacion_ajuste_tipo
chk_programacion_ajuste_forma
chk_programacion_ajuste_rango
```

Formas:

- `CANCELACION`: serie no nula; todos los campos resultado nulos.
- `REEMPLAZO`: serie no nula; snapshot resultado completo.
- `ADICION`: serie nula; snapshot resultado completo.
- Si existe resultado, `hora_fin_resultado > hora_inicio_resultado`.

No permitir formas híbridas.

#### 6.5 Índices

Crear:

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
- salón origen;
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
- replica `creadoEn` con `@CreationTimestamp`, `updatable=false`;
- replica `actualizadoEn` con `@UpdateTimestamp`;
- usa los mismos nombres y nulabilidad reales que `EntidadBase`;
- no permite actualizar `creadoEn`.

Identidad efectiva:

```
RECURRENTE/REEMPLAZO:
serieId + fecha

ADICION:
ajusteId + fecha
```

#### 7.1 Fecha inmutable de adición

Para una adición activa existente:

```
requested.fecha == persisted.fecha
```

es obligatorio.

Si difieren, rechazar con error estable de forma/identidad inválida.

Mover una adición de fecha no es un update. Debe hacerse como:

```
retirar adición actual
→ crear nueva adición
→ usar nuevo ajusteId
```

Editar otros campos conserva `ajusteId + fecha`.

#### 7.2 UUID histórico no reutilizable

Antes de crear una adición con UUID suministrado:

```
buscar por PK sin filtrar activo
```

Si existe cualquier fila física:

- activa: tratar únicamente como posible update;
- inactiva: rechazar reutilización;
- nunca insertar otra;
- nunca reactivar;
- nunca sobrescribir mediante merge.

Un UUID retirado no vuelve a utilizarse.

Para cancelación/reemplazo nuevos, el service genera un UUID físico nuevo. Su identidad lógica continúa siendo `serieId + fecha`.

### 8. Persistencia JPA concreta

Crear un componente interno acotado, por ejemplo:

```
AjusteProgramacionFechaPersistence
```

Puede encapsular `EntityManager`, `persist` y `flush`. No crear una abstracción genérica.

#### 8.1 Create

Para una fila nueva:

1. comprobar por PK histórica que el UUID no existe;
2. construir una entidad realmente nueva;
3. llamar `EntityManager.persist`;
4. llamar `flush` dentro del traductor de constraints.

No usar:

- `merge`;
- `repository.save` de entidad detached;
- upsert;
- reactivación.

#### 8.2 Update

Para update:

1. cargar por PK la entidad activa;
2. mantenerla managed;
3. verificar identidad;
4. mantener `id`, `fecha` y `creadoEn`;
5. mutar sólo campos editables;
6. hacer `flush`.

No construir una entidad detached desde el request.

#### 8.3 Retiro

Para retiro:

1. cargar la entidad activa managed;
2. marcar `activo=false`;
3. hacer `flush`.

No eliminar físicamente. No reactivar posteriormente.

#### 8.4 No-op

Si identidad y contenido son iguales:

- devolver no-op;
- no llamar `persist`;
- no llamar `merge`;
- no llamar `save`;
- no llamar `flush` explícito;
- no ensuciar la entidad managed;
- no modificar `actualizadoEn`.

### 9. Repositorio

Crear `AjusteProgramacionFechaRepository` con consultas para:

- búsqueda histórica por PK, incluyendo activas e inactivas;
- búsqueda activa por PK;
- target activo por `serieId + fecha`;
- ajustes activos de una fecha;
- targets activos de una serie/rango;
- resultados activos por salón/instructor/fecha cuando sean necesarios para locks y validación.

No definir `findById` como si filtrara `activo`. La búsqueda histórica por PK debe ser inequívoca.

### 10. Nominales

Implementar una consulta/proyección lógicamente completa que produzca:

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

Debe preservar:

- asignación activa;
- bloque activo;
- vigencia de asignación contiene D;
- vigencia de bloque contiene D;
- día del bloque coincide con D;
- relación real bloque/asignación;
- cardinalidad 0/1/>1 por serie.

No imponer una tecnología concreta. Puede utilizar:

- JPQL;
- query nativa;
- projection de Spring Data;
- lecturas coordinadas;

según el mejor encaje con el modelo real.

Si utiliza múltiples lecturas:

- no filtrar por horario entre ellas;
- no perder duplicados;
- no colapsar cardinalidad;
- operar dentro de una transacción coherente.

No filtrar nominales por horario operativo o master-data.

### 11. Locks

Crear:

```
ubicaciones.servicio.SalonLocks
usuarios.servicio.InstructorLocks
```

Ambos:

- reciben `Collection<UUID>`;
- deduplican;
- ordenan determinísticamente;
- adquieren locks pesimistas uno a uno;
- exigen `Propagation.MANDATORY`;
- no dependen del orden del request.

`SalonLocks` reutiliza `SalonLock`/`SalonRepository`.

Añadir en `UsuarioRepository` una query `PESSIMISTIC_WRITE` equivalente a `SalonRepository.bloquearParaActualizar`.

Orden global obligatorio:

```
TODOS LOS SALONES, UUID ascendente
→ TODOS LOS INSTRUCTORES, UUID ascendente
```

Nunca adquirir salón después de instructor.

En updates y retiros, usar la unión de recursos persistidos anteriores y solicitados nuevos.

No añadir:

- advisory locks;
- locks JVM;
- tabla de locks;
- lock global artificial;
- fence;
- retry interno.

### 12. Hardening recurrente y Policy A

Modificar únicamente los writers físicos actuales:

```
BloqueProgramacionService.crearBloque
BloqueProgramacionService.crearAsignacion
```

`crearBloque` debe utilizar `SalonLocks`.

`crearAsignacion`:

1. valida sintaxis pura;
2. descubre mínimamente el bloque;
3. deriva salón e instructor;
4. bloquea salón;
5. bloquea instructor;
6. relee bloque;
7. compara discovery y lock set;
8. relee maestros y programación;
9. valida rol, especialidad y oferta;
10. valida contención;
11. valida solape global;
12. aplica Policy A;
13. persiste en la misma transacción.

Corregir el test actual que afirma que `crearAsignacion` no toma lock de salón.

Policy A:

- si la creación afecta un ajuste activo de hoy/futuro, proyectar su cardinalidad;
- exactamente un target: continuar;
- cero o más de uno: rechazar;
- no cancelar, reasignar ni ignorar ajustes.

La creación aislada de un bloque todavía sin asignaciones no cambia targets.

No introducir writers de update/desactivación/versionado sólo para ampliar F2D.2.

Si aparece otro writer real de `programacion_bloque` o `programacion_asignacion`, STOP.

### 13. Aplicación pura de ajustes

Crear un componente puro que reciba:

- nominales de una fecha;
- ajustes activos de esa fecha.

Orden:

1. indexar nominales por `serieId`;
2. fallar si una serie tiene más de una nominal;
3. validar exactamente un target para cada cancelación/reemplazo;
4. aplicar cancelaciones;
5. aplicar reemplazos;
6. incorporar adiciones.

No consulta:

- repositorios;
- reloj;
- horario;
- maestros.

Referencias:

```
RECURRENTE/REEMPLAZO:
SERIE_ASIGNACION + serieId + fecha

ADICION:
AJUSTE + ajusteId + fecha
```

No deduplicar.

### 14. Writer interno

Crear un service interno, sin controller, para:

- guardar/editar ajuste target por `serieId + fecha`;
- retirar ajuste target;
- guardar/editar adición por `ajusteId`;
- retirar adición.

Usar `Clock`:

```
pasado → inmutable
hoy    → mutable
futuro → mutable
```

#### 14.1 Target recurrente

Protocolo:

```
validación pura
→ discovery nominal
→ discovery de ajuste activo
→ recursos anteriores+nuevos
→ SalonLocks
→ InstructorLocks
→ relectura nominal/ajuste
→ comparación
→ proyección completa de la fecha
→ validación
→ persist/flush, update/flush o no-op
```

Si cambia discovery:

```
CONFLICTO_LOCK_SET_DESACTUALIZADO
```

No reintentar en esa transacción.

#### 14.2 Adición nueva

Protocolo:

1. validar forma y temporalidad;
2. buscar por PK histórica;
3. si existe inactiva, rechazar reutilización;
4. si existe activa, pasar al protocolo de update;
5. si no existe, derivar recursos solicitados;
6. tomar `SalonLocks`;
7. tomar `InstructorLocks`;
8. releer por PK histórica;
9. si ahora existe, abortar por conflicto de discovery o dejar que la PK actúe como backstop;
10. proyectar la fecha completa;
11. validar;
12. `persist + flush`.

La relectura no sustituye la PK. Dos operaciones con mismo UUID y recursos disjuntos pueden continuar concurrentemente después de descubrir inexistencia.

#### 14.3 Update de adición activa

1. discovery por PK activa;
2. comprobar fecha inmutable;
3. derivar recursos persistidos anteriores + solicitados;
4. tomar salones;
5. tomar instructores;
6. releer la fila activa;
7. comparar identity/snapshot/lock set;
8. volver a comprobar fecha;
9. si contenido idéntico, no-op real;
10. proyectar y validar;
11. mutar la entidad managed;
12. flush.

#### 14.4 Retiro de adición

1. discovery activo;
2. derivar recursos persistidos;
3. locks;
4. relectura;
5. proyección del estado sin la adición;
6. validar invariantes;
7. marcar `activo=false`;
8. flush.

La fila permanece histórica y su UUID queda inutilizable.

### 15. Concurrencia del mismo ajusteId

Caso obligatorio:

```
Tx A:
ajusteId = X
salón = A
instructor = I1

Tx B:
ajusteId = X
salón = B
instructor = I2
```

con recursos disjuntos.

No afirmar que los locks serializan ambas transacciones.

Protecciones diferentes:

```
SalonLocks/InstructorLocks:
conflictos naturales de programación

PRIMARY KEY:
identidad global del ajusteId
```

La PK decide el ganador.

El perdedor:

- recibe `CONFLICTO_AJUSTE_PROGRAMACION`;
- aborta;
- no relee dentro de la transacción fallida;
- no se convierte en update;
- no modifica al ganador;
- no realiza retry interno;
- no interpreta la colisión como éxito idempotente.

Un retry externo usa una transacción nueva:

1. discovery desde cero;
2. encuentra la fila;
3. distingue activa/inactiva;
4. valida fecha;
5. deriva recursos anteriores+nuevos;
6. toma salones;
7. toma instructores;
8. relee;
9. decide no-op/update/rechazo;
10. valida y persiste si corresponde.

Si la fila encontrada está inactiva, rechaza reutilización.

### 16. Traducción de constraints

Implementar traducción estricta y separada.

#### A. Target recurrente

```
SQLSTATE:
23505

Constraint/index:
idx_programacion_ajuste_target_activo
```

Resultado:

```
CONFLICTO_AJUSTE_PROGRAMACION
```

#### B. Identidad física

```
SQLSTATE:
23505

Constraint:
programacion_ajuste_fecha_pkey
```

Resultado:

```
CONFLICTO_AJUSTE_PROGRAMACION
```

Exigir simultáneamente SQLSTATE y nombre exacto.

No traducir genéricamente:

- cualquier `23505`;
- EXCLUDE de asignaciones;
- FKs;
- CHECKs;
- otro índice unique;
- `DataIntegrityViolationException` sin evidencia inequívoca.

Después de cualquier error JDBC, relanzar y dejar que Spring revierta. No continuar usando la sesión/transacción.

### 17. Validaciones del writer

Antes de persistir cualquier mutación, proyectar la programación completa de la fecha.

Validar:

- horario efectivo del salón final;
- salón activo;
- instructor activo;
- rol `INSTRUCTOR` global o del salón final;
- actividad activa;
- especialidad;
- oferta del salón;
- duplicado operativo;
- solape del instructor, incluso cross-salon.

Cancelación:

- exige target nominal;
- no exige horario operativo;
- no porta snapshot;
- puede cancelar una nominal que sería omitida.

Reemplazo/adición:

- snapshot completo;
- horario sólo del salón resultado;
- origen cerrado no bloquea destino abierto;
- no recortar automáticamente.

### 18. ProgramacionEfectiva

Crear service interno con:

```
List<OcurrenciaEfectiva> porSalonYFecha(UUID salonId, LocalDate fecha);
List<OcurrenciaEfectiva> porInstructorYFecha(UUID instructorId, LocalDate fecha);
```

Resolver globalmente antes de filtrar:

```
NOMINALES
→ TARGETS
→ CANCELACIONES
→ REEMPLAZOS
→ ADICIONES
→ HORARIO FINAL
→ MASTER-DATA
→ INVARIANTES
→ FILTRO
→ ORDEN
```

Para cada candidato consultar `HorarioEfectivoSalon` del salón final.

Omitir fail-closed cuando:

- salón no existe/inactivo;
- cerrado/no operativo;
- rango fuera del horario;
- instructor suspendido/eliminado;
- rol ausente;
- actividad inexistente/inactiva;
- especialidad ausente;
- actividad no ofrecida.

Después:

- detectar duplicado exacto por salón/instructor/actividad/fecha/inicio/fin;
- detectar solape del mismo instructor;
- permitir adyacencia;
- ante corrupción persistida lanzar `ProgramacionInvarianteException`;
- no producir resultado parcial.

Orden final:

```
horaInicio
horaFin
instructorId
referencia
```

No usa `Clock`.

No consulta `TurnoInstructor`, `Reserva` ni paquete `calendario`.

### 19. Diagnóstico fail-closed

Crear un port interno inyectable, semánticamente equivalente a:

```
ProgramacionDiagnostico
```

Operación:

```
registrarOmision(...)
```

El dato debe contener:

- causa estable;
- referencia;
- fecha;
- salón;
- instructor;
- actividad.

Crear una única implementación productiva interna basada en SLF4J.

No usar:

- persistencia;
- broker;
- outbox;
- eventos de dominio;
- API pública.

Los tests deben inyectar fake/spy y verificar determinísticamente todos los campos.

### 20. Errores estables

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

No añadir error HTTP.

No modificar `GlobalExceptionHandler`.

`ProgramacionInvarianteException` debe conservar código y referencia estructurados.

### 21. Tests unitarios

Cubrir:

- formas válidas de los tres tipos;
- todas las formas híbridas;
- target 0/1/>1;
- cancelación;
- reemplazo de horario/instructor/actividad/salón;
- adición;
- identidad;
- no recorte;
- orden;
- duplicado y solape;
- pasado/hoy/futuro con `Clock`;
- locks deduplicados/ordenados;
- stale discovery;
- fail-closed y port diagnóstico.

### 22. Tests obligatorios de identidad/JPA

Añadir expresamente:

1. Update de adición activa con la misma fecha: permitido si pasa las demás invariantes.
2. Update intentando cambiar fecha: rechazado.
3. Movimiento de fecha: sólo retiro + nueva adición con UUID nuevo.
4. Crear usando UUID de fila inactiva: rechazado.
5. Retirar y volver a crear con el mismo UUID: rechazado.
6. Retiro + nueva adición con UUID diferente: permitido.
7. Creación con UUID asignado usa `persist` y genera `INSERT`.
8. Update de entidad managed no crea una segunda fila.
9. Update conserva `id`, `fecha` y `creadoEn`.
10. Retiro establece `activo=false`.
11. Ninguna operación reactiva una fila inactiva.
12. No-op no ejecuta escritura ni modifica `actualizadoEn`.

Probar con PostgreSQL real cuando la propiedad dependa de JPA/timestamps.

### 23. Tests PostgreSQL

Cubrir:

- migración real V46→V47;
- pre-auditoría aborta ante solape y conserva datos;
- EXCLUDE;
- vigencias consecutivas;
- solape de frontera;
- filas inactivas;
- CHECKs;
- unique target;
- múltiples targets inactivos;
- recreate target;
- FKs resultado;
- ausencia de FK serie;
- nombre físico de la PK;
- colisión PK;
- metamodelo JPA;
- Flyway en `47`;
- total aplicado esperado `50`.

Actualizar el test que actualmente espera V46/49.

### 24. Tests de concurrencia

Usar PostgreSQL real, transacciones independientes, `TransactionTemplate`, latches y coordinación determinista. No usar sleeps como mecanismo.

Cubrir:

- mismo target recurrente;
- mismo instructor/salón;
- mismo instructor cross-salon;
- writer recurrente contra ajuste, ambos órdenes;
- cambio de instructor;
- cambio de salón;
- swap cross-salon sin deadlock;
- requests con orden inverso;
- stale discovery.

Añadir obligatoriamente:

#### Alta concurrente mismo ajusteId, recursos disjuntos

Dos adiciones con:

```
mismo ajusteId
salones distintos
instructores distintos
snapshots distinguibles
```

Comprobar:

- una gana;
- la otra obtiene `CONFLICTO_AJUSTE_PROGRAMACION`;
- queda exactamente una fila;
- el snapshot persistido es íntegramente el del ganador;
- no existe mezcla de campos;
- no existe upsert;
- no existe actualización silenciosa;
- no hay retry interno.

#### Retry externo

Después de la carrera:

- iniciar una transacción nueva;
- redescubrir la fila;
- aplicar locks de recursos persistidos+nuevos;
- demostrar no-op o update sólo después del protocolo completo.

Si la fila se retira antes del retry:

- el mismo UUID no puede reaparecer.

### 25. Tests de arquitectura/dark-launch

Demostrar:

- ninguna clase `programacion` es controller;
- ningún bean F2D implementa `ValidadorImpactoExcepcionHorario`;
- `programacion` no depende de `calendario`;
- no existe `ImpactoAjustesEnExcepcionHorario`;
- `SalonHorarioExcepcionService` conserva adapters productivos previos;
- una fila de ajuste no veta una excepción legacy;
- `TurnoInstructorService` no lee F2D;
- `ReservaService` no lee F2D;
- no existe frontend/mobile nuevo;
- no hay fence ni cutover;
- writers nuevos usan `Clock`;
- resolver no usa `Clock`.

### 26. Mutaciones A–O

Mantener:

```
15/15 DETECTADAS
```

- A: nominal antes de reemplazo.
- B: writers recurrentes participan en locks.
- C: target cero.
- D: EXCLUDE.
- E: relectura/lock set.
- F: Reserva legacy ausente.
- G: reserva sin identidad inequívoca bloquea el cutover futuro.
- H: duplicado efectivo.
- I: destino abierto tras origen cerrado.
- J: fail-closed.
- K: sin FK serie.
- L: unique target.
- M: orden global.
- N: sin integración productiva legacy.
- O: sin controller.

G es evidencia documental de un blocker futuro de cutover, no un test runtime F2D.2.

Los nuevos casos de identidad y colisión concurrente de adiciones son regresiones adicionales; no modificar la matriz A–O para incorporarlos artificialmente.

### 27. P2 preservados

P2-1 queda resuelto porque:

- identidad de adición es `ajusteId + fecha`;
- fecha es inmutable;
- movimiento exige retiro+nuevo UUID;
- UUID histórico no se reutiliza;
- no-op es real;
- colisión concurrente tiene resultado estable.

P2-2 permanece fuera de F2D.2: permisos/API continúan diseñados para activación futura.

P2-3 queda resuelto sin lock global:

```
SALONES → INSTRUCTORES
```

Los recursos naturales protegen programación. La PK protege la identidad UUID cuando no existe recurso común.

No afirmar que dos altas disjuntas del mismo UUID fueron serializadas por locks.

### 28. Archivos esperados

Crear, ajustando sólo nombres no arquitectónicos a las convenciones reales:

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

Modificar sólo según necesidad:

```
AsignacionRepository.java
BloqueProgramacionRepository.java
UsuarioRepository.java
BloqueProgramacionService.java
tests actuales de programacion
expectativas Flyway
```

Crear tests específicos de:

- migración;
- persistencia;
- servicio;
- resolver;
- locks;
- identidad JPA;
- concurrencia;
- arquitectura/dark-launch.

Crear al final:

```
auditoria/fase-2d2-implementacion-dark-launch-ajustes-programacion-fecha.md
```

No modificar canónicos para declarar cierre.

### 29. Orden de implementación

1. Pre-flight y baseline.
2. Tests por etapas de V47.
3. V47.
4. Entidad con UUID asignado y persistencia `EntityManager.persist`.
5. Repositorio histórico/activo.
6. Tests de identidad JPA.
7. `SalonLocks` e `InstructorLocks`.
8. Hardening recurrente y Policy A.
9. Resolución nominal.
10. Aplicador puro.
11. Writer interno.
12. Traductores estrictos de target y PK.
13. `ProgramacionEfectiva`.
14. Port diagnóstico + SLF4J.
15. Tests de concurrencia, incluida PK con recursos disjuntos.
16. Tests de arquitectura.
17. Suite completa.
18. Checkpoint `IMPLEMENTADA_EN_REVIEW`.
19. Revisión adversarial independiente.

### 30. Comandos de validación

Ejecutar incrementalmente:

```
./mvnw -Dtest='*MigracionV46V47*,*Programacion*Persistencia*,*Ajuste*Persistence*' test

./mvnw -Dtest='*BloqueProgramacionServiceTest,*AjusteProgramacionFechaServiceTest,*AplicadorAjustesProgramacionTest,*ProgramacionEfectivaTest' test

./mvnw -Dtest='*Programacion*Concurrencia*,*AjusteId*Concurrencia*' test

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

La última búsqueda no debe encontrar merge/upsert en la persistencia de ajustes. Un `on conflict` ajeno fuera de estos paths no pertenece a esta intervención.

Reportar el total real de tests después de implementar. No exigir que permanezca en 493.

### 31. STOP conditions

Detener sin limpiar ni improvisar si:

- branch, HEAD, remoto o working tree difieren;
- checkpoint SHA difiere;
- V47 existe o hay una migración posterior;
- baseline falla o cambia sin reconciliación;
- existe implementación F2D.2 inesperada;
- aparecen solapes activos preexistentes;
- aparece otro writer físico recurrente no inventariado;
- se necesita modificar `TurnoInstructor`, Reserva o writers legacy;
- se necesita controller, frontend/mobile, fence o cutover;
- se necesita modificar writers maestros;
- se necesita una dependencia nueva;
- no se puede verificar PostgreSQL/Testcontainers;
- JPA no permite cerrar create/update sin merge/upsert y haría falta cambiar la estrategia aprobada;
- el nombre de la PK no puede verificarse inequívocamente;
- la traducción exige aceptar cualquier `23505`;
- se necesitaría un lock global/advisory/tabla de locks;
- se necesitaría retry interno;
- surge una decisión arquitectónica nueva.

No usar:

- reset;
- clean;
- stash;
- pull;
- merge;
- rebase;

para superar un STOP.

### 32. Checkpoint de implementación

Crear:

```
auditoria/fase-2d2-implementacion-dark-launch-ajustes-programacion-fecha.md
```

Registrar:

- base exacta;
- checkpoint F2D.1;
- archivos;
- V47;
- identidad y fecha inmutable;
- persist/managed/flush;
- UUID histórico no reutilizable;
- colisión PK y retry externo;
- locks;
- TOCTOU;
- ProgramacionEfectiva;
- fail-closed;
- port diagnóstico;
- tests y conteos reales;
- ausencia de integración legacy;
- mutaciones 15/15.

Estado:

```
F2D.2:
IMPLEMENTADA_EN_REVIEW
```

No actualizar F2D.2 a `CERRADA`.

### 33. No commit

No ejecutar:

```
git add
git commit
git push
```

La implementación debe pasar primero por review adversarial.

### 34. Salida obligatoria

```
# F2D.2 — Implementación dark-launch

Branch:
...

Base:
...

Checkpoint F2D.1 SHA:
...

Baseline inicial:
...

V47:
...

Pre-auditoría:
PASS / STOP

EXCLUDE:
...

AjusteProgramacionFecha:
IMPLEMENTADO / NO

Identidad adición:
ajusteId + fecha

Fecha de adición:
INMUTABLE / ...

Movimiento de fecha:
RETIRO + NUEVA ADICION CON NUEVO UUID / ...

UUID retirado reutilizable:
NO / ...

Create JPA:
EntityManager.persist / ...

Update JPA:
ENTIDAD MANAGED + FLUSH / ...

Merge/upsert:
NO / ...

Constraint target:
idx_programacion_ajuste_target_activo

Constraint identidad:
programacion_ajuste_fecha_pkey / <nombre verificado>

Colisión concurrente mismo ajusteId:
...

Retry interno:
NO

Retry externo:
...

ProgramacionEfectiva:
IMPLEMENTADA / NO

Policy A:
IMPLEMENTADA / NO

SalonLocks:
...

InstructorLocks:
...

Orden:
SALONES → INSTRUCTORES

TOCTOU target/update:
CERRADO / ABIERTO

TOCTOU alta UUID sin fila:
PK DECIDE GANADOR / ...

Diagnóstico fail-closed:
PORT INTERNO + SLF4J / ...

TurnoInstructor modificado:
NO

Reserva modificada:
NO

SalonHorarioExcepcionService modificado:
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

Tests identidad/JPA:
...

Tests PostgreSQL:
...

Tests concurrencia:
...

Test mismo UUID recursos disjuntos:
PASS / FAIL

Tests arquitectura:
...

Suite completa:
<total>, failures, errors, skipped

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

Decisiones abiertas:
NINGUNA / ...

F2D.2:
IMPLEMENTADA_EN_REVIEW / BLOQUEADA
```

Detenerse. No hacer commit ni ejecutar cutover.

## VEREDICTO

**A. F2D.2.1 COMPLETADA — INTERVENCIÓN CORREGIDA LISTA PARA RE-REVIEW**