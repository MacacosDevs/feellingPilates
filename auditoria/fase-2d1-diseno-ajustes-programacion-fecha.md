# F2D.1 — Diseño corregido de ajustes puntuales de programación por fecha

Fase exclusivamente de análisis y diseño. Este checkpoint incorpora F2D.1.1 y F2D.1.2, las
correcciones post-review y post re-review de F2D.1. No se ha escrito código productivo, no se han
modificado tests, no se han creado migraciones ni endpoints y no se ha tocado frontend ni mobile.

Estado contractual después de esta corrección:

```text
F2D.1:
REQUIERE_AJUSTE / LISTA PARA RE-REVIEW

F2D.1.1:
EJECUTADA DOCUMENTALMENTE

F2D.1.2:
EJECUTADA DOCUMENTALMENTE

F2D.2:
NO INICIADA
```

Este documento no aprueba F2D.1 ni autoriza F2D.2. El siguiente gate es un re-review
independiente con condición `P0 = 0` y `P1 = 0`.

---

## Estado base y pre-flight F2D.1.1

| Elemento | Valor |
|---|---|
| Branch | `operacion/excepciones-horario-fecha` |
| Base de código/estado verificada en 3B.0 | `8c40594d2caf8b5230b364cb76cd8f48fe5ed98a` |
| HEAD operativo al iniciar F2D.1.1 | `e5bcf2e229ac018be3f30c55c517865fb4d80f06` |
| Upstream al iniciar F2D.1.1 | `origin/operacion/excepciones-horario-fecha`, `0/0` |
| Working tree al iniciar F2D.1.1 | limpio |
| Baseline reproducido | **493 tests / 0 failures / 0 errors / 0 skipped** |
| Build | **SUCCESS** |
| Última migración Flyway | `V46__horario_operacion_drop_unique_dia.sql` |
| Siguiente número libre observado | **V47** |

La primera ejecución del baseline dentro del sandbox no pudo acceder a
`/var/run/docker.sock`. La ejecución real con Docker/Testcontainers disponible fue la que produjo
el resultado verde anterior.

Diseños autoritativos previos respetados sin cambios:

- `auditoria/fase-2c1-diseno-excepciones-horario-fecha.md`;
- `auditoria/fase-2c2-implementacion-excepciones-horario-fecha.md`.

Review que origina esta corrección:

`auditoria/reviews/F2D.1-REVIEW-AJUSTES-PUNTUALES.md`

---

## Alcance de diseño

F2D diseña cómo expresar, para una fecha concreta `D`, sin modificar la recurrencia posterior:

1. cancelar una ocurrencia recurrente concreta;
2. reemplazar una ocurrencia concreta, incluido cambiar horario, instructor, actividad o salón;
3. agregar programación puramente puntual sin template.

Se conservan estas invariantes:

- una asignación representa un instructor, una actividad, un rango y un salón;
- el mismo instructor no se solapa consigo mismo, incluso entre salones;
- la adyacencia `[08:00,10:00)` / `[10:00,12:00)` es válida;
- varios instructores pueden coexistir en el mismo salón y horario;
- `HorarioEfectivoSalon` es la autoridad operativa por fecha;
- una ocurrencia recurrente incompatible se omite, nunca se recorta automáticamente;
- un ajuste puntual nunca escribe el template recurrente;
- no se crea `Sesion`, confirmación, capacidad, recursos, broker ni outbox en F2D.

---

## Inventario y autoridad actual

### `calendario.TurnoInstructor`

Estado: **LEGACY_VIVO / PRODUCTIVO / AUTORIDAD ACTUAL**.

Tiene controller, writers, frontend y consumo desde reservas. Sus tipos conocidos son:

```text
RECURRENTE
EXCEPCION
CANCELACION
```

No sirve como soporte del modelo futuro:

- `EXCEPCION` carece de target inequívoco hacia una ocurrencia recurrente;
- el writer la trata como adición que no puede solaparse, mientras el reader de reservas la
  interpreta como sustitución;
- `CANCELACION` es un marcador amplio de día/instructores, no de una ocurrencia individual;
- un turno agrega múltiples instructores y actividades;
- no tiene vigencias ni identidad individual equivalente a una serie de asignación.

F2D no modifica `TurnoInstructor`, su API, sus writers ni sus consumers.

### `programacion.BloqueProgramacion + Asignacion`

Estado: **IMPLEMENTADO_NO_PRODUCTIVO**.

V41 creó las tablas y originalmente nacieron vacías, pero ese hecho histórico no demuestra que
sigan vacías en una base existente. El repositorio contiene un writer interno,
`BloqueProgramacionService`, sin controller público conocido. `crearBloque` ya participa de
`SalonLock`; `crearAsignacion` todavía no participa de `SalonLock` ni de un lock de instructor.

El modelo nuevo no es autoridad productiva. Su activación exige auditoría de datos y cutover.

---

## Rollout y autoridad — decisión cerrada

### F2D.2 es DARK LAUNCH

F2D.2 puede implementar infraestructura backend interna:

- migración y constraints;
- entidad y repositorio de ajustes;
- resolución nominal/efectiva;
- writers internos;
- locks y hardening de writers recurrentes;
- tests unitarios, PostgreSQL, concurrencia y arquitectura.

F2D.2 no incluye:

- controllers o endpoints públicos nuevos;
- consumers productivos nuevos;
- frontend o mobile;
- adaptación/migración de reservas;
- activación por salón;
- modificación de `TurnoInstructor`.

Durante F2D.2:

```text
TurnoInstructor = autoridad productiva legacy
programacion_*   = infraestructura nueva aislada en dark launch
```

Invariante general de dark launch:

> Durante F2D.2 ningún estado contenido exclusivamente en la nueva programación puede alterar el
> resultado observable de un flujo productivo legacy.

Esto incluye permitir, rechazar, modificar, ocultar o transformar una operación productiva. Por
tanto, `programacion_ajuste_fecha` no participa directa ni indirectamente en:

- controllers productivos;
- writers productivos legacy;
- validadores ejecutados por writers productivos;
- consumers productivos.

Los services internos y los tests de F2D.2 pueden leer y escribir la infraestructura nueva de
forma aislada. La ausencia deliberada de integración productiva —no sólo la ausencia de un
controller nuevo— es el fence efectivo durante el dark launch.

### Cutover futuro por salón

Una fase posterior implementará un estado persistido y autoritativo por salón:

```text
LEGACY
MIGRANDO
NUEVA
```

Semántica:

| Estado | Writers externos | Readers/consumers productivos |
|---|---|---|
| `LEGACY` | sólo legacy | sólo legacy |
| `MIGRANDO` | detenidos | migración/auditoría interna; sin doble autoridad |
| `NUEVA` | sólo nuevos | sólo fuente nueva |

En particular, la semántica futura de la protección de ajustes frente a cambios de horario es:

- `LEGACY`: la programación nueva no afecta el writer productivo legacy;
- `MIGRANDO`: los writers externos están bloqueados conforme a la estrategia de cutover;
- `NUEVA`: la programación nueva puede participar en validaciones productivas según el contrato
  aprobado para esa activación.

`ImpactoAjustesEnExcepcionHorario` sólo podrá incorporarse en esa **FASE FUTURA DE
ACTIVACIÓN/CUTOVER**, cuando el fence por salón ya sea efectivo. F2D.2 no implementa ni adelanta el
fence `LEGACY/MIGRANDO/NUEVA` para proteger este adapter.

Nunca se permite `legacy + nueva` como dos autoridades escribibles/productivas simultáneas para
el mismo salón.

Antes de mover un salón a `MIGRANDO`/`NUEVA` se deben auditar datos reales de:

- `programacion_bloque`;
- `programacion_asignacion`;
- `turno_instructor` y sus asignaciones;
- reservas;
- maestros de salón, instructor, actividad, roles y especialidades.

La fase de activación bloquea si no existe equivalencia/mapeo inequívoco. F2D.2 no implementa el
fence persistido ni asume tablas vacías.

Secuencia futura cerrada:

```text
F2D.2 dark launch
→ F2E/preparación: auditoría, normalización/migración y resolver comparativo
→ LEGACY → MIGRANDO: detener writers externos y migrar
→ validar equivalencia e identidad de reservas
→ MIGRANDO → NUEVA: habilitar nuevos controllers/consumers y rechazar legacy
→ retirar TurnoInstructor sólo con cero consumers
```

---

## Modelo elegido

Se crea conceptualmente `programacion.entidad.AjusteProgramacionFecha` sobre
`BloqueProgramacion + Asignacion`. No se reutiliza `TurnoInstructor`.

Tipos cerrados, sin flags combinables:

| Tipo | Target | Resultado persistido | Efecto en `D` |
|---|---|---|---|
| `CANCELACION` | `asignacionSerieId + fecha` | ninguno | suprime una ocurrencia nominal |
| `REEMPLAZO` | `asignacionSerieId + fecha` | snapshot completo | sustituye una nominal por un resultado explícito |
| `ADICION` | ninguno | snapshot completo | añade una ocurrencia puntual |

`REEMPLAZO` es una sola fila. `CANCELACION + ADICION` no representa un reemplazo.

Un ajuste nunca inserta, actualiza, cierra vigencia, desactiva ni elimina filas de
`programacion_bloque` o `programacion_asignacion`.

---

## Target nominal e identidad de serie

El target de `CANCELACION`/`REEMPLAZO` es:

```text
(asignacionSerieId, fecha)
```

y siempre representa una **OCURRENCIA NOMINAL**, no una ocurrencia ya filtrada por horario
operativo.

Para una fecha `D` debe existir exactamente una versión nominal que cumpla simultáneamente:

- `Asignacion.activo`;
- `BloqueProgramacion.activo`;
- vigencia de asignación contiene `D`;
- vigencia de bloque contiene `D`;
- `bloque.diaSemana == dayOfWeek(D)`;
- la asignación pertenece al bloque;
- `serieId` coincide.

Cardinalidad:

```text
0  → target inexistente al escribir; invariante rota si ya existe un ajuste activo
1  → target válido
>1 → invariante rota
```

`serieId` identifica una regla lógica recurrente versionable. Si una nueva versión conserva la
misma regla, conserva `serieId` y los ajustes futuros siguen la versión nominal aplicable. Una
regla lógicamente distinta recibe un `serieId` nuevo.

Un `REEMPLAZO` conserva la identidad de la ocurrencia target, pero su snapshot final no se
reinterpreta si después cambia la asignación base.

No existe FK directa desde ajuste a `programacion_asignacion.serie_id`: esa columna no es una PK
ni un `UNIQUE` simple porque una serie admite varias versiones. La integridad se sostiene mediante
EXCLUDE temporal, Policy A inversa y resolución fail-fast.

---

## Ocurrencia nominal y ocurrencia efectiva

### Ocurrencia nominal

Es el resultado de las reglas recurrentes aplicables por serie, vigencia y día de semana, sin
considerar todavía ajustes ni `HorarioEfectivoSalon`.

Forma conceptual:

```java
record OcurrenciaNominal(
        LocalDate fecha,
        UUID serieId,
        UUID asignacionVersionId,
        UUID bloqueVersionId,
        UUID salonId,
        UUID instructorId,
        UUID tipoActividadId,
        LocalTime horaInicio,
        LocalTime horaFin) {}
```

### Ocurrencia efectiva

Es un candidato nominal transformado por ajustes y después validado contra el salón final,
maestros actuales e invariantes globales.

```java
record OcurrenciaEfectiva(
        LocalDate fecha,
        UUID salonId,
        UUID instructorId,
        UUID tipoActividadId,
        LocalTime horaInicio,
        LocalTime horaFin,
        Origen origen,
        ReferenciaOcurrencia referencia) {

    enum Origen { RECURRENTE, REEMPLAZO, ADICION }
}
```

Identidad estable:

| Origen | Referencia |
|---|---|
| recurrente | `(SERIE_ASIGNACION, serieId, fecha)` |
| reemplazo | `(SERIE_ASIGNACION, serieId, fecha)` |
| adición | `(AJUSTE, ajusteId, fecha)` |

Editar un reemplazo o una adición activa conserva su identidad. Retirar y recrear una adición
usa un UUID nuevo. No se persiste una `Sesion` ni una ocurrencia materializada.

---

## Semántica de ajustes

### Cancelación

- suprime una nominal concreta sólo en `D`;
- no cancela todo el día del instructor;
- no porta rango ni resultado;
- exige que el target nominal exista;
- no consulta horario operativo: conserva intención incluso si la nominal sería omitida;
- no modifica semanas posteriores.

### Reemplazo

Porta un snapshot final completo:

```text
salonResultadoId
instructorResultadoId
tipoActividadResultadoId
horaInicioResultado
horaFinResultado
```

Puede cambiar cualquiera de esos campos. El resultado conserva exactamente un salón, un
instructor, una actividad y un rango.

La validez operativa se evalúa sólo contra `HorarioEfectivoSalon` del **salón resultado**. El
estado del salón origen no elimina el target nominal.

Casos cerrados:

```text
Original Juriquilla 08:00–10:00
Juriquilla cerrada
REEMPLAZO Cimatario 09:00–11:00
Cimatario abierto y compatible
→ APARECE
```

```text
Original 08:00–12:00
Horario final 10:00–16:00
Sin ajuste → omitido
REEMPLAZO explícito 10:00–12:00
→ APARECE; no es recorte automático
```

### Adición

- no tiene target de serie;
- usa UUID aportado por el cliente interno/futuro contrato;
- porta snapshot completo;
- valida horario final, entidades activas, rol, especialidad, oferta y solape global;
- no se deduplica silenciosamente.

Igualdad operativa mínima para detectar duplicado efectivo:

```text
salón + instructor + actividad + fecha + inicio + fin
```

El writer rechaza una ocurrencia activa equivalente. El resolver que encuentre un duplicado
persistido lanza una excepción técnica de invariante; nunca elige ni deduplica heurísticamente.

No existe `UNIQUE(salon, fecha, hora)`: varios instructores simultáneos son válidos.

---

## `ProgramacionEfectiva` — algoritmo definitivo

Casos de uso internos:

```java
List<OcurrenciaEfectiva> porSalonYFecha(UUID salonId, LocalDate fecha);
List<OcurrenciaEfectiva> porInstructorYFecha(UUID instructorId, LocalDate fecha);
```

Orden único de composición:

1. cargar todas las ocurrencias **NOMINALES** aplicables a `D`;
2. indexarlas inequívocamente por `serieId` y fallar si una serie tiene más de una nominal;
3. cargar todos los ajustes activos aplicables a `D` necesarios para la consulta;
4. validar que cada `CANCELACION`/`REEMPLAZO` tenga exactamente un target nominal;
5. aplicar cancelaciones sobre el conjunto nominal;
6. aplicar reemplazos sobre el conjunto nominal, usando su snapshot final;
7. incorporar adiciones;
8. para cada candidato resultante, resolver `HorarioEfectivoSalon` de su **salón final**;
9. omitir candidatos no contenidos completamente o cuyo estado sea `CERRADO`/`NO_OPERATIVO`;
10. revalidar defensivamente salón, instructor, actividad, rol, especialidad y oferta actuales;
11. validar solape global y duplicados efectivos; una violación persistida es invariante rota;
12. filtrar por el salón o instructor solicitado;
13. ordenar por `(horaInicio, horaFin, instructorId, referencia)`.

Nunca se filtran nominales por horario operativo antes de aplicar ajustes.

`porSalonYFecha` incluye resultados finales del salón consultado:

- recurrentes que permanecen en él;
- reemplazos entrantes;
- adiciones del salón.

Un reemplazo saliente suprime el original y sólo aparece en el salón destino.

`porInstructorYFecha` aplica ajustes antes de filtrar: un cambio `Ariadna → Alberto` desaparece
de Ariadna y aparece para Alberto.

El resolver no usa `Clock`: recibe `fecha` y es determinista. Para estado persistido corrupto usa
una excepción técnica propuesta `ProgramacionInvarianteException` con código estable; no produce
resultados parciales.

---

## Fail-closed ante cambios posteriores

Una ocurrencia válida puede quedar inválida si posteriormente se suspende un instructor, se
desactiva una actividad/salón, se retira una especialidad/rol o el salón deja de ofrecer la
actividad.

Política cerrada para F2D.2:

- `ProgramacionEfectiva` revalida cada candidato contra el estado maestro actual;
- un candidato inválido se **OMITE FAIL-CLOSED** y emite una señal técnica estructurada con la
  referencia y la causa;
- nunca se muestra programación inválida;
- el hardening inverso obligatorio de F2D.2 se concentra en Bloque/Asignacion;
- F2D.2 no amplía el alcance para modificar los writers maestros actuales
  (`UsuarioService.cambiarEstatus`, `UsuarioService.actualizarSedesRol`,
  `EspecialidadInstructorService`, `SalonService.actualizar` y desactivación de actividad);
- la auditoría/revalidación de esos maestros es precondición bloqueante antes de activar un
  salón `NUEVA`;
- una fase posterior podrá añadir Policy A inversa a maestros si el gate de activación demuestra
  que sigue siendo necesario.

Esta separación es definitiva para F2D.2: resolver fail-closed ahora; hardening de maestros fuera
del dark launch y antes/después del cutover según su fase propia.

El fail-closed read-time de `ProgramacionEfectiva` es distinto de una protección writer-time sobre
excepciones productivas. Durante F2D.2 el resolver interno debe seguir consultando
`HorarioEfectivoSalon` y maestros actuales para no devolver programación nueva inválida, pero esa
validación aislada no puede conectarse a `SalonHorarioExcepcionService` ni alterar ningún flujo
legacy. Esta distinción preserva el cierre de P1-8 / mutación J.

---

## Reservas

### Reservas legacy en F2D.2

`Reserva`/`ReservaService` pertenecen causalmente al universo `TurnoInstructor` y no tienen FK ni
referencia a `Asignacion`, serie, ajuste u ocurrencia nueva.

Por tanto F2D.2 elimina por completo:

```text
ImpactoReservasEnAjusteProgramacion
covered-before / not-covered-after
```

Las reservas legacy no bloquean ajustes en dark launch y no se atribuyen a la fuente nueva por
coincidencia de salón, instructor, actividad, fecha u horas.

### Reservas antes del cutover

Antes de activar un salón `NUEVA`, cada reserva relevante debe obtener una referencia explícita a
la fuente/ocurrencia nueva o una migración que demuestre un mapeo único. Una reserva sin mapeo
inequívoco bloquea el cutover.

Después del cutover, una cancelación o cambio de instructor/actividad/salón debe bloquearse si
existen reservas asociadas; un cambio de horario bloquea cuando deja de contener la reserva. Nada
de esto se implementa en F2D.2.

---

## Policy A inversa

Todo writer que pueda alterar la existencia nominal futura de una serie debe impedir que un
ajuste activo de hoy/futuro quede con cero o múltiples targets.

Recursos cubiertos:

- `Asignacion`: `activo`, vigencia, `serieId`, bloque, instructor, actividad y rango cuando
  cambien identidad o aplicabilidad;
- `BloqueProgramacion`: `activo`, vigencia, día, salón, rango y cualquier dato que cambie la
  ocurrencia nominal.

Antes de persistir, el writer proyecta el estado resultante y consulta ajustes activos afectados:

```text
exactamente 1 target nominal por ajuste → continuar
0 o >1 targets                         → rechazar
```

No cancela, reasigna ni ignora ajustes. La salida administrativa es retirar/modificar primero el
ajuste conflictivo.

El repositorio actual sólo expone `crearBloque` y `crearAsignacion` en
`BloqueProgramacionService`; F2D.2 endurece ambos. Si F2D.2 necesita introducir un writer de
actualización/desactivación/versionado, ese entry point nace dentro de los mismos locks y Policy A;
no se admite un writer alterno fuera del protocolo.

---

## Concurrencia y locks

`SalonLock` no protege el solape global del mismo instructor entre dos salones. F2D.2 incorpora:

```text
SalonLocks.adquirirOrdenados(Collection<UUID>)
InstructorLocks.adquirirOrdenados(Collection<UUID>)
```

Ambos helpers:

- deduplican UUIDs;
- ordenan ascendentemente;
- adquieren `PESSIMISTIC_WRITE`/`SELECT ... FOR UPDATE`;
- exigen transacción con `Propagation.MANDATORY`;
- no dependen del orden del request.

`SalonLocks` puede reutilizar `SalonLock`/`SalonRepository`. `InstructorLocks` bloquea filas de
`usuario` mediante una query aditiva en `UsuarioRepository`. No se usan locks JVM, advisory ni
distribuidos.

Orden global único:

```text
TODOS LOS SALONES, UUID ascendente
→ TODOS LOS INSTRUCTORES, UUID ascendente
```

Nunca `Instructor → Salon`.

### Lock set por operación

| Operación | Salones | Instructores |
|---|---|---|
| `CANCELACION` | origen nominal | original nominal |
| `ADICION` | resultado | resultado |
| `REEMPLAZO` | origen nominal + resultado | original nominal + resultado |
| cambio recurrente de instructor | salón(es) implicados | original + nuevo |
| cambio recurrente de salón | origen + destino | instructor(es) implicados |

Los IDs se deduplican antes de adquirir.

### Writers recurrentes participantes

F2D.2 endurece `BloqueProgramacionService.crearBloque` y, especialmente,
`crearAsignacion`. Todos los writers presentes o introducidos en F2D.2 que creen/cambien
instructor, salón, actividad, rango, vigencia, serie, activo, bloque o día deben:

1. usar `SalonLocks`/`InstructorLocks` según el recurso;
2. validar solape global bajo locks;
3. aplicar Policy A inversa;
4. persistir en la misma transacción.

Un lock unilateral en el writer de ajustes está prohibido.

### Discovery/TOCTOU para target recurrente

Protocolo definitivo de `CANCELACION`/`REEMPLAZO`:

1. validar sintaxis y temporalidad sin BD;
2. autorizar el salón contextual y destino declarado antes de retener locks;
3. hacer una lectura preliminar mínima de `serieId + fecha`;
4. exigir que el origen nominal coincida con el salón contextual; si no, `404`;
5. derivar salones/instructores original y resultado;
6. adquirir salones ordenados;
7. adquirir instructores ordenados;
8. releer la nominal bajo locks;
9. comparar origen, instructor, versión aplicable y lock set con discovery;
10. si cambiaron, abortar con conflicto reintentable estable;
11. validar sobre la relectura y proyectar el resultado;
12. persistir en la misma transacción.

La relectura es efectiva porque los writers recurrentes participan de los mismos locks. No se
continúa con un lock set obsoleto ni se reintenta silenciosamente dentro de una transacción.

---

## Temporalidad e idempotencia

La fecha es atómica y el writer usa `Clock` inyectado:

| Fecha | Política |
|---|---|
| pasado | inmutable |
| hoy | mutable con todas las validaciones |
| futuro | mutable con todas las validaciones |

No se usa `LocalDate.now()`/`LocalTime.now()` directo.

Ciclo de vida:

- soft delete mediante `activo = false`;
- un ajuste activo puede actualizarse;
- no se reactiva una fila inactiva;
- recrear target recurrente inserta fila física nueva, con la misma identidad lógica
  `serieId + fecha`;
- recrear adición usa UUID nuevo.

Idempotencia natural:

| Recurso | Operación repetida |
|---|---|
| target `serieId + fecha` | mismo contenido: `200` no-op; primera creación: `201`; cambio: `200` |
| adición `ajusteId` | `PUT`: `201` primera creación; `200` mismo contenido/update |
| delete | soft delete; inexistente/inactivo: `404` |

El no-op real no llama `save`/`flush` ni modifica `actualizado_en`.

---

## Persistencia propuesta para V47

**PROPUESTA DE DISEÑO. EL ARCHIVO NO SE CREA EN F2D.1.1.**

V47 tiene dos responsabilidades ordenadas:

1. endurecer vigencias de `programacion_asignacion`;
2. crear `programacion_ajuste_fecha`.

`btree_gist` ya existe desde V44 y no se vuelve a crear.

### EXCLUDE temporal por serie

V41 confirma `vigente_desde DATE NOT NULL` y `vigente_hasta DATE` nullable. La sintaxis propuesta,
coherente con V45 y sin sentinels, es:

```sql
ALTER TABLE programacion_asignacion
    ADD CONSTRAINT ex_programacion_asignacion_serie_vigencia
        EXCLUDE USING gist (
            serie_id WITH =,
            daterange(vigente_desde, vigente_hasta, '[]') WITH &&
        )
        WHERE (activo);
```

Los extremos de negocio son inclusivos; PostgreSQL canonicaliza `daterange` a su forma interna.
`vigente_hasta NULL` representa extremo superior abierto. La constraint garantiza como máximo
una versión activa de una serie aplicable a una fecha. Vigencias consecutivas sin compartir fecha
son válidas.

Antes de aplicar la constraint en un entorno existente, la fase de implementación debe auditar y
detenerse ante datos solapados; no puede borrar ni corregir datos silenciosamente.

### Tabla de ajustes

```sql
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
    actualizado_en               TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT chk_programacion_ajuste_tipo
        CHECK (tipo IN ('CANCELACION', 'REEMPLAZO', 'ADICION')),

    CONSTRAINT chk_programacion_ajuste_forma CHECK (
        (tipo = 'CANCELACION'
            AND asignacion_serie_id IS NOT NULL
            AND salon_resultado_id IS NULL
            AND instructor_resultado_id IS NULL
            AND tipo_actividad_resultado_id IS NULL
            AND hora_inicio_resultado IS NULL
            AND hora_fin_resultado IS NULL)
     OR (tipo = 'REEMPLAZO'
            AND asignacion_serie_id IS NOT NULL
            AND salon_resultado_id IS NOT NULL
            AND instructor_resultado_id IS NOT NULL
            AND tipo_actividad_resultado_id IS NOT NULL
            AND hora_inicio_resultado IS NOT NULL
            AND hora_fin_resultado IS NOT NULL)
     OR (tipo = 'ADICION'
            AND asignacion_serie_id IS NULL
            AND salon_resultado_id IS NOT NULL
            AND instructor_resultado_id IS NOT NULL
            AND tipo_actividad_resultado_id IS NOT NULL
            AND hora_inicio_resultado IS NOT NULL
            AND hora_fin_resultado IS NOT NULL)
    ),

    CONSTRAINT chk_programacion_ajuste_rango CHECK (
        hora_fin_resultado IS NULL
        OR hora_fin_resultado > hora_inicio_resultado
    )
);

CREATE UNIQUE INDEX idx_programacion_ajuste_target_activo
    ON programacion_ajuste_fecha (asignacion_serie_id, fecha)
    WHERE activo AND tipo IN ('CANCELACION', 'REEMPLAZO');

CREATE INDEX idx_programacion_ajuste_salon_fecha_activo
    ON programacion_ajuste_fecha (salon_resultado_id, fecha)
    WHERE activo AND salon_resultado_id IS NOT NULL;

CREATE INDEX idx_programacion_ajuste_instructor_fecha_activo
    ON programacion_ajuste_fecha (instructor_resultado_id, fecha)
    WHERE activo AND instructor_resultado_id IS NOT NULL;

CREATE INDEX idx_programacion_ajuste_fecha_activo
    ON programacion_ajuste_fecha (fecha)
    WHERE activo;
```

No se persiste salón origen: se deriva de la nominal. No hay FK a `serieId`. Los campos resultado
son inequívocos y sólo existen para reemplazo/adición.

El índice target permite múltiples filas inactivas y prohíbe cancelación/reemplazo activos
simultáneos para la misma serie/fecha. Las adiciones no se restringen por salón/hora.

---

## Horario operativo y protección durante dark launch/cutover

`HorarioEfectivoSalon` se reutiliza sin cambios.

| Estado final | Recurrente/reemplazo/adición | Cancelación |
|---|---|---|
| abierto y contiene | aparece | válida |
| abierto y no contiene | omitido/rechazado por writer | válida |
| cerrado/no operativo | omitido/rechazado por writer | válida |

Durante F2D.2 la tabla anterior rige exclusivamente la validación aislada del writer interno y de
`ProgramacionEfectiva`. `ImpactoAjustesEnExcepcionHorario` **NO pertenece a F2D.2** y no se registra
como bean/adapter de `ValidadorImpactoExcepcionHorario`: hacerlo permitiría que datos de
`programacion_ajuste_fecha` vetaran la modificación o cancelación productiva de una excepción
legacy antes del cutover.

La necesidad funcional no se elimina. Antes de activar un salón en `NUEVA` debe existir una
estrategia que impida que cambios de `HorarioEfectivoSalon` dejen programación nueva productiva en
estado inválido. En la fase futura de activación/cutover, con fence efectivo, el adapter podrá
validar reemplazos/adiciones activos —no cancelaciones— conforme al contrato aprobado entonces.

---

## API futura — fuera de F2D.2

Los siguientes contratos son diseño para la fase de activación. **NO SE IMPLEMENTAN EN F2D.2.**

```text
PUT    /api/salones/{origen}/programacion/ocurrencias/{serieId}/fechas/{fecha}/ajuste
DELETE /api/salones/{origen}/programacion/ocurrencias/{serieId}/fechas/{fecha}/ajuste

PUT    /api/salones/{contexto}/programacion/adiciones/{ajusteId}
DELETE /api/salones/{contexto}/programacion/adiciones/{ajusteId}

GET    /api/salones/{salonId}/programacion/efectiva?fecha=...
GET    /api/instructores/{instructorId}/programacion/efectiva?fecha=...
```

No cuelgan de `/horarios`: horario operativo y programación son dominios distintos.

---

## Seguridad futura — fuera de F2D.2

No se inventan permisos:

| Operación | Permiso |
|---|---|
| lectura | `calendario.leer` |
| cancelación | `calendario.gestionar` o `calendario.cancelar` |
| reemplazo sólo de horario, mismo salón/instructor/actividad | `calendario.gestionar` o `calendario.editar` |
| reemplazo que cambia salón/instructor/actividad | `calendario.gestionar` |
| adición | `calendario.gestionar` |
| retirar cancelación | `calendario.gestionar` o `calendario.cancelar` |
| retirar reemplazo sólo horario | `calendario.gestionar` o `calendario.editar` |
| retirar reemplazo estructural o adición | `calendario.gestionar` |

Un movimiento cross-salon autoriza origen y destino antes de locks mediante
`AutorizadorSalon`. Al retirar un reemplazo cross-salon se vuelven a autorizar origen y destino,
porque la operación suprime el resultado en destino y restaura la nominal en origen.

Estas reglas se implementan sólo junto con el fence de activación; F2D.2 no tiene controller.

---

## Errores e invariantes

Los futuros errores de negocio usan códigos estables. Como mínimo:

| Código | HTTP futuro | Uso |
|---|---|---|
| `AJUSTE_PROGRAMACION_EN_EL_PASADO` | 400 | mutación de fecha pasada |
| `AJUSTE_PROGRAMACION_FORMA_INVALIDA` | 400 | campos incompatibles con tipo |
| `ASIGNACION_OBJETIVO_NO_EXISTE` | 404 | cero nominales al escribir |
| `AJUSTE_FUERA_DE_HORARIO_EFECTIVO` | 400 | resultado fuera del horario final |
| `SALON_NO_OPERATIVO_EN_FECHA` | 400 | resultado en cerrado/no operativo |
| `INSTRUCTOR_CON_PROGRAMACION_TRASLAPADA` | 409 | overlap global |
| `OCURRENCIA_EFECTIVA_DUPLICADA` | 409 | duplicado operativo al escribir |
| `CONFLICTO_AJUSTE_PROGRAMACION` | 409 | backstop de unique target |
| `CONFLICTO_LOCK_SET_DESACTUALIZADO` | 409 | discovery difiere de relectura |
| `AJUSTE_PROGRAMACION_NO_EXISTE` | 404 | retiro inexistente/ajeno |

`ProgramacionInvarianteException` cubre corrupción persistida como cero/múltiples targets,
duplicados o solapes que escaparon a writers. No se traduce a un resultado parcial.

---

## Plan de tests F2D.2

No se fija un número artificial. La safety net debe demostrar estos hechos.

### Persistencia PostgreSQL

- EXCLUDE rechaza dos versiones activas solapadas de una serie;
- vigencias consecutivas sin fecha compartida son válidas;
- 0/>1 target se detecta como corresponde;
- CHECKs rechazan toda forma híbrida de ajuste;
- unique target impide cancelación/reemplazo activos simultáneos;
- múltiples inactivas y recreate funcionan;
- adiciones legítimas de distintos instructores coexisten;
- FKs de resultado y traducción inequívoca de constraints.

### Resolver

- recurrente nominal sin ajuste;
- cancelación sólo en `D` y reaparición en `D+7`;
- reemplazos de horario/instructor/actividad/salón;
- adición sin template;
- origen cerrado → reemplazo en destino abierto aparece;
- original fuera → reemplazo explícito dentro aparece;
- cancelación de nominal operativamente omitida es válida;
- reemplazos entrantes/salientes y consultas por salón/instructor;
- horario final, no origen, gobierna;
- ningún recorte automático;
- determinismo de contenido y orden;
- duplicado/solape persistido produce invariante, no deduplicación.

### Policy A y writers recurrentes

- crear una versión ambigua falla por DB;
- cambio de vigencia/día/bloque que dejaría ajuste huérfano se rechaza;
- `crearAsignacion` participa de locks de salón/instructor;
- todo writer de mutación introducido por F2D.2 participa de locks y Policy A;
- el template queda byte a byte intacto tras crear/modificar/retirar ajustes.

### Concurrencia

- mismo target concurrente serializado;
- mismo instructor en el mismo salón y cross-salon serializado;
- writer recurrente contra ajuste;
- cambio de instructor bloquea original+nuevo;
- cambio de salón bloquea origen+destino;
- swap cross-salon sin deadlock;
- discovery stale aborta con conflicto reintentable;
- orden de locks independiente del orden del request;
- concurrencia interna de ajustes y writers recurrentes permanece serializada sin conectarse a
  writers legacy de excepciones.

### Dark launch y arquitectura

- no existe controller F2D.2;
- `TurnoInstructor` no se modifica;
- `Reserva` legacy no participa;
- `ProgramacionEfectiva` no depende de `calendario` ni mezcla fuentes;
- no existe consumer productivo nuevo;
- no existe bean/adapter F2D conectado al writer productivo de excepciones;
- `programacion_ajuste_fecha` no puede vetar la creación, modificación o cancelación de una
  excepción legacy;
- `SalonHorarioExcepcionService` conserva únicamente los adapters productivos previamente
  existentes; F2D.2 no añade `ImpactoAjustesEnExcepcionHorario`;
- `ProgramacionEfectiva` continúa validando `HorarioEfectivoSalon` de forma aislada;
- no hay `LocalDate.now()`/`LocalTime.now()` directo en writers nuevos.

### Fail-closed

- instructor suspendido/eliminado;
- rol de instructor retirado;
- actividad desactivada;
- especialidad removida;
- actividad retirada de la oferta del salón;
- salón inactivo/cerrado/no operativo;
- cada caso omite y emite señal técnica, nunca muestra programación inválida.

### Activación futura

- un salón no puede tener writers legacy/nuevos productivos simultáneos;
- reserva sin mapeo inequívoco bloquea cutover;
- `MIGRANDO` detiene writers externos;
- `NUEVA` rechaza writers legacy.

---

## Matriz adversarial A–O corregida

| Mutación | Detector/cierre |
|---|---|
| A — omitir nominal antes de reemplazo | algoritmo nominal → ajustes → operativo + casos origen cerrado/original fuera |
| B — ajuste toma lock pero Asignacion no | hardening obligatorio de `crearAsignacion` y todo writer recurrente |
| C — target sin versión | Policy A inversa + resolver fail-fast |
| D — dos versiones cubren `D` | EXCLUDE PostgreSQL + invariante runtime |
| E — target cambia durante discovery | relectura bajo locks + comparación de lock set + 409 reintentable |
| F — reserva legacy bloquea ajuste | ausencia deliberada del adapter de reservas en F2D.2 |
| G — reserva futura sin asociación | blocker contractual de cutover |
| H — duplicado efectivo | writer rechaza + resolver invariante, sin deduplicación |
| I — destino abierto se pierde por origen cerrado | horario sólo del resultado después del reemplazo |
| J — especialidad/estado removido queda visible | resolver fail-closed + gate de activación |
| K — FK inválida a `serieId` | diseño prohíbe esa FK |
| L — dos ajustes target activos | índice único parcial + concurrencia |
| M — swap cross-salon deadlock | salones asc → instructores asc |
| N — consumer mezcla legacy/nueva | **DETECTADA**: durante dark launch no existe integración de ajustes nuevos con consumers, writers ni validadores productivos legacy; `ImpactoAjustesEnExcepcionHorario` queda diferido hasta existir fence |
| O — API nueva antes de cutover | ausencia deliberada de controllers en F2D.2 |

---

## Riesgos residuales aceptados

1. F2D.2 amplía locks internos sobre filas de salón/usuario; se mitiga con orden global y tests
   de concurrencia.
2. El resolver puede omitir datos por degradación de maestros; es fail-closed deliberado y debe
   emitir señal técnica para remediación.
3. Bases existentes pueden contener datos `programacion_*` no auditados; V47/activación se
   detienen ante solapes o ambigüedad, sin corrección silenciosa.
4. El fence por salón y la identidad de reservas aún no están implementados; por eso F2D.2 es dark
   launch y no puede exponerse productivamente.
5. La protección writer-time de ajustes frente a cambios de horario queda pendiente para la fase
   futura de activación/cutover; no puede conectarse al writer legacy hasta que el fence por salón
   sea efectivo. El resolver interno reduce el riesgo durante dark launch mediante fail-closed.

---

## Decisiones cerradas

| # | Decisión | Cierre |
|---|---|---|
| 1 | rollout F2D.2 | **DARK LAUNCH**, sin controllers/consumers productivos |
| 2 | autoridad durante F2D.2 | `TurnoInstructor` legacy; modelo nuevo aislado |
| 3 | cutover | fence futuro por salón `LEGACY/MIGRANDO/NUEVA` |
| 4 | modelo | `AjusteProgramacionFecha` sobre Bloque/Asignacion |
| 5 | tipos | `CANCELACION`, `REEMPLAZO`, `ADICION`; reemplazo = una fila |
| 6 | target | `serieId + fecha` sobre ocurrencia nominal exacta |
| 7 | vigencias | EXCLUDE de `programacion_asignacion` dentro de V47 |
| 8 | FK serie | ninguna FK directa a `serieId` |
| 9 | orfandad | Policy A inversa; 0/>1 rechaza/falla |
| 10 | resolver | nominales → ajustes → operativo final → maestros/invariantes → filtro |
| 11 | reemplazo cross-salon | origen cerrado no impide resultado válido en destino abierto |
| 12 | reservas legacy | no participan en F2D.2; mapeo inequívoco bloquea cutover |
| 13 | locks | salones asc → instructores asc; sets completos original+resultado |
| 14 | writers recurrentes | participan de locks, overlap global y Policy A |
| 15 | discovery | preliminar → locks → relectura → comparar/abortar |
| 16 | cambios maestros | resolver fail-closed en F2D.2; auditoría bloqueante para activación |
| 17 | identidad | recurrente/reemplazo `serieId+fecha`; adición `ajusteId+fecha` |
| 18 | DDL ajuste | target separado de snapshot resultado; soft delete |
| 19 | idempotencia | `PUT` natural; mismo contenido = no-op real |
| 20 | API/permisos | diseñados para activación futura; no implementados en F2D.2 |
| 21 | `HorarioEfectivoSalon` | reutilizado sin cambios; salón final gobierna |
| 22 | recorte | nunca automático; reemplazo explícito puede escribir rango distinto |
| 23 | impacto de ajustes en excepción productiva | `ImpactoAjustesEnExcepcionHorario` fuera de F2D.2; sólo en activación/cutover con fence efectivo |

---

## Alcance final F2D.2

### Incluye

- V47 con EXCLUDE de vigencias y tabla de ajustes;
- `AjusteProgramacionFecha` y repositorio;
- `OcurrenciaNominal`, `OcurrenciaEfectiva`, `ReferenciaOcurrencia` y errores/invariantes;
- resolver nominal y `ProgramacionEfectiva`;
- writer interno de ajustes con `Clock`;
- `SalonLocks` e `InstructorLocks` ordenados;
- hardening de `BloqueProgramacionService`/Asignacion;
- Policy A inversa;
- fail-closed de estado/rol/especialidad/oferta;
- tests unitarios, PostgreSQL real, concurrencia, arquitectura y checkpoint de implementación.

### Excluye

- controllers/endpoints públicos;
- adapter de reservas legacy;
- frontend y mobile;
- migración de datos/cutover/fence persistido;
- cambios en `TurnoInstructor` o `ReservaService`;
- consumers productivos y adapters que alteren writers productivos legacy;
- `ImpactoAjustesEnExcepcionHorario`;
- hardening de writers maestros fuera de Programacion;
- sesiones, confirmación, capacidad/recursos, notificaciones, pagos, broker/outbox.

---

## F2D.1.1 — Correcciones post-review

| Hallazgo | Corrección incorporada |
|---|---|
| P1-1 activación sin cutover | F2D.2 dark launch; sin controllers; fence futuro por salón |
| P1-2 target ambiguo | ocurrencia nominal exacta + EXCLUDE temporal en V47 |
| P1-3 ajustes huérfanos | Policy A inversa + resolver fail-fast |
| P1-4 orden/scoping del resolver | nominal → ajustes → horario del resultado → filtro |
| P1-5 locking unilateral | hardening de Bloque/Asignacion y locks compartidos |
| P1-6 discovery TOCTOU | discovery, locks ordenados, relectura y abortar si cambia el set |
| P1-7 reservas heurísticas | reservas legacy fuera de F2D.2; identidad bloquea cutover |
| P1-8 cambios maestros | resolver fail-closed + auditoría de activación |
| P2-1 API/idempotencia | recursos naturales con `PUT`, diseñados fuera de F2D.2 |
| P2-2 permisos amplios | `editar` sólo horario; cambios estructurales/adición requieren `gestionar` |
| P2-3 multi-lock | helpers, sets por tipo y orden global completamente especificados |

El orden viejo del resolver, los controllers F2D.2, la atribución de reservas legacy, el lock
unilateral y la V47 sin EXCLUDE dejan de ser especificaciones vigentes.

## F2D.1.2 — Corrección post re-review de aislamiento dark-launch

| Hallazgo | Corrección incorporada |
|---|---|
| P1 — `ImpactoAjustesEnExcepcionHorario` rompe aislamiento dark-launch | adapter eliminado del alcance, wiring, integración y test plan de F2D.2; diferido a activación/cutover con fence efectivo |
| Invariante dark-launch incompleto | ningún estado exclusivo de programación nueva puede permitir, rechazar, modificar, ocultar ni transformar un flujo productivo legacy |
| Mutación N | **DETECTADA** por ausencia de integración con consumers, writers y validadores productivos legacy |
| Riesgo sobre cambios posteriores de horario | necesidad conservada para activación futura; `ProgramacionEfectiva` mantiene validación read-time fail-closed aislada en F2D.2 |

No queda vigente ninguna especificación que implemente, registre o pruebe la integración de
`ImpactoAjustesEnExcepcionHorario` con `SalonHorarioExcepcionService` durante F2D.2.

## Decisiones abiertas

**NINGUNA**

## Condición de salida

```text
F2D.1:
LISTA PARA RE-REVIEW

F2D.2:
NO INICIADA
```

No iniciar F2D.2 antes del re-review independiente y del gate `P0 = 0 / P1 = 0`.
