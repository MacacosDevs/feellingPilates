# FeelingPilates — F2E / preparación de migración controlada

## 1. Identidad y estado

```text
Unidad: F2E / preparación
Tipo: PREPARATION / DESIGN ONLY
Role original: DESIGN_EXECUTOR / PREPARATION_EXECUTOR
Correction role F2E.1.1: DESIGN_CORRECTOR / DOCUMENT_CORRECTOR
Correction role F2E.1.2: DESIGN_CORRECTOR / DOCUMENT_CORRECTOR
Fresh execution: SI
Checkpoint: auditoria/fase-2e-preparacion-migracion-controlada.md
Audit previo a F2E.1.1: P0=0 / P1=3 / P2=1
Audit previo a F2E.1.2: P0=0 / P1=1 / P2=0
Design/documentation gate previo a F2E.1.1/F2E.1.2: FAIL
Estado histórico tras F2E.1.2: CORRECTION_MATERIALIZED / READY_FOR_FRESH_RE-AUDIT
Design/documentation gate final: PASS
Estado final de F2E.1: DESIGN_APPROVED / CLOSED
Review final persistido: auditoria/reviews/F2E.1-REVIEW-DISENO-PREPARACION.md
Siguiente acción: determinar un nuevo scope/handoff desde los canónicos, decisiones y blockers preservados
```

Este checkpoint materializa únicamente el diseño preparatorio autorizado por
`auditoria/handoffs/HANDOFF-F2E-PREPARACION.md`. No aprueba implementación, no ejecuta auditoría
material de datos, no implementa componentes, no migra, no activa programación nueva y no realiza
cutover.
F2E.1.1 corrige el contrato documental observado por la auditoría adversarial; no reaudita ni
aprueba el checkpoint. F2E.1.2 corrige exclusivamente la cardinalidad y la diferencia entre
ausencia anómala y supresión intencional de una ocurrencia efectiva; tampoco reaudita ni aprueba el
checkpoint. Ese estado histórico quedó resuelto por el re-audit fresh e independiente final
registrado en `auditoria/reviews/F2E.1-REVIEW-DISENO-PREPARACION.md`. La persistencia de este
lifecycle closure es un delta documental que requiere por sí mismo una auditoría documental fresh e
independiente; no constituye autoauditoría del DOCUMENTER.

El nombre sigue la ruta exacta exigida por el handoff y por la intervención F2E.1. No existía otro
checkpoint F2E físico al iniciar, por lo que éste es el único checkpoint nuevo.

## 2. Base Git y pre-flight fresh

Verificación física realizada antes del análisis:

```text
Repository: /Users/jesusaldaircruzortiz/Desktop/Feelingpilates/feelingpilates
Branch: operacion/excepciones-horario-fecha
HEAD inicial: 4a6cd5eb571de00274a3b82f7c661a1dfd34fa2d
Staging inicial: VACÍO
Working tree inicial: CLEAN
Baseline dirty autorizado: NINGUNO
```

Pre-flight físico de la corrección F2E.1.1, preservando el corte original anterior:

```text
Branch: operacion/excepciones-horario-fecha
HEAD: 4a6cd5eb571de00274a3b82f7c661a1dfd34fa2d
Staging: VACÍO
Working tree / baseline autorizado:
?? auditoria/fase-2e-preparacion-migracion-controlada.md
Unexpected paths: NINGUNO
```

Pre-flight físico de la corrección F2E.1.2, sobre el mismo baseline documental autorizado:

```text
Branch: operacion/excepciones-horario-fecha
HEAD: 4a6cd5eb571de00274a3b82f7c661a1dfd34fa2d
Staging: VACÍO
Working tree / baseline autorizado:
?? auditoria/fase-2e-preparacion-migracion-controlada.md
Unexpected paths: NINGUNO
```

El SHA-256 del checkpoint F2D.1 se verificó físicamente:

```text
58af39f41b3bc089ebbd4ec67f684e270087ddf4eb695f2c7b55276d0aff352e
```

No se ejecutaron Maven, Docker, tests, migraciones ni conexiones a datos. Los tests se leyeron como
evidencia; sus resultados históricos no se reejecutaron ni reinterpretaron.

## 3. Autoridad y profile aplicable

Las fuentes físicas competentes establecen de forma consistente:

```text
HANDOFF-F2E-PREPARACION: APPROVED / ACTIVE / AUTHORIZED_FOR_F2E_PREPARATION
F2D.2: CLOSED / MATERIALIZED
technical gate F2D.2: PASS
documentation gate F2D.2: PASS
publication closure F2D.2: PASS
runtime: DARK_LAUNCH
productive: NOT_PRODUCTIVE
cutover: false
product authority: TurnoInstructor / LEGACY_VIVO / PRODUCTIVO
F2E checkpoint al iniciar: PENDING
F2E design/documentation gate: PENDING
implementation: NOT_AUTHORIZED
migration: NOT_AUTHORIZED
```

Workflow profile:

```text
F2E_PREPARATION_DESIGN_ONLY
MATERIALIZACIÓN TÉCNICA: NOT_APPLICABLE
IMPLEMENTATION GATE: NOT_APPLICABLE
TESTS GATE: NOT_APPLICABLE
HOST VALIDATION: NOT_APPLICABLE
SCOPE/SECURITY CONTROLS: APPLICABLE
DESIGN/DOCUMENTATION GATE: APPLICABLE / PENDING
```

La autoridad productiva no se deduce de la existencia de tablas, servicios o tests. En todo este
checkpoint continúa siendo exclusivamente `TurnoInstructor`.

## 4. Scope autorizado y prohibido

### 4.1 Autorizado y realizado

- inspección read-only de canónicos, handoff, reviews y predecesores;
- inspección read-only de código, entidades, repositorios, servicios, controllers y tests;
- inspección read-only de Flyway, esquema declarado y configuración;
- inspección read-only del frontend web y del mobile accesibles en el workspace;
- inventario legacy/nuevo y de writers/readers/consumers;
- diseño de auditoría de datos, mapping, migración, resolver, transición, fence, hardening,
  observabilidad, abort conditions y reconciliación;
- creación exclusiva de este checkpoint documental.

### 4.2 Prohibido y no realizado

- código productivo, tests, migraciones Flyway, SQL mutante o datos;
- controllers, endpoints, frontend o mobile;
- cambios en `Reserva`, `ReservaService`, writers, readers o consumers productivos;
- implementación del resolver, mapping persistente, auditor, migrador, observabilidad o fence;
- entrada real en `MIGRANDO` o `NUEVA`;
- activación, doble autoridad, cutover o retiro de `TurnoInstructor`;
- `git add`, commit o push.

## 5. Evidencia inspeccionada

Se leyeron completos los canónicos, el protocolo, el handoff activo, su review, F2D.1 y su review
final, F2D.2 y su review documental, y el handoff histórico F2D.2 exigidos por la intervención.

La evidencia física principal se concentró en:

- legacy: `calendario/entidad/{TurnoInstructor,TurnoInstructorAsignacion,Reserva}.java`, sus DTOs,
  repositorios, services y controllers;
- nuevo: `programacion/entidad/*`, `programacion/dominio/*`, repositorios y services;
- operación/maestros: entidades, repositories y services de `ubicaciones` y `usuarios`;
- esquema: V9, V11, V15, V17–V22, V36, V38–V47;
- tests de caracterización de reservas, dark launch, persistencia, concurrencia, resolver,
  Policy A, locks y fail-closed;
- web sibling `/Users/jesusaldaircruzortiz/Desktop/Feelingpilates/web`;
- mobile sibling `/Users/jesusaldaircruzortiz/Desktop/Feelingpilates/FeelingPiltaesAppMobile`.

## 6. Inventario físico autocontenido

### 6.1 Entidades y representaciones

| Área | Artefactos físicos | Estado y función actual |
| --- | --- | --- |
| Programación legacy | `TurnoInstructor`, `TurnoInstructorAsignacion` | Autoridad productiva. Bloque recurrente o puntual, varios instructores y filas instructor/actividad con rango opcional. |
| Reservas legacy | `Reserva` | Productiva. Snapshot relacional de salón, instructor, cliente, actividad, fecha, inicio, fin y estado. |
| Programación nueva recurrente | `BloqueProgramacion`, `Asignacion` | Materializada, interna, no productiva. Vigencias explícitas y `serieId`; una `Asignacion` representa una actividad. |
| Ajustes nuevos | `AjusteProgramacionFecha` | Materializado en dark launch. `CANCELACION`, `REEMPLAZO`, `ADICION`; soft delete. |
| Proyecciones nuevas | `OcurrenciaNominal`, `OcurrenciaEfectiva`, `ReferenciaOcurrencia` | Records de runtime, no tablas ni sesiones materializadas. |
| Operación | `HorarioOperacion`, `SalonHorarioExcepcion`, `HorarioEfectivoSalon` | Productiva. Vigencias semanales más excepción exacta por fecha. |
| Maestros | `Salon`, `Usuario`, `TipoActividad`, roles, especialidades y oferta del salón | Productivos y compartidos por ambos modelos. |

### 6.2 Tablas, claves y relaciones relevantes

| Tabla | Identidad y relaciones físicas | Semántica temporal/relevante |
| --- | --- | --- |
| `turno_instructor` | PK `id`; FK `salon_id`; no `serie_id`; no FK desde `reserva` | `RECURRENTE` usa `dia_semana`; `EXCEPCION/CANCELACION` usan `fecha`; sin vigencias. |
| `turno_instructor_usuario` | PK `(turno_id, usuario_id)`; FKs a turno/usuario | Puede existir un instructor en el bloque aun sin fila de actividad. |
| `turno_instructor_asignacion` | PK `(turno_id, usuario_id, tipo_actividad_id)`; FKs a turno/usuario/actividad | `hora_inicio/hora_fin` nullable; el service interpreta ambos null como bloque completo. |
| `reserva` | PK `id`; FKs a salón, instructor, cliente y actividad | Guarda fecha y `[hora_inicio,hora_fin)`; no guarda turno, asignación, serie, ajuste ni ocurrencia. |
| `programacion_bloque` | PK `id`; `serie_id` indexado; FK `salon_id` | Día, rango y vigencia inclusiva de fechas con extremos explícitos. |
| `programacion_asignacion` | PK `id`; `serie_id`; FKs a bloque, usuario y actividad | V47 aplica EXCLUDE a vigencias activas de la misma serie. |
| `programacion_ajuste_fecha` | PK `id`; FKs sólo para salón/instructor/actividad resultado | Target `asignacion_serie_id + fecha` sin FK directa a serie; unique parcial para target activo. |
| `horario_operacion` | PK `id`; FK salón; EXCLUDE por salón/día/vigencia | `null` es extremo abierto; negocio usa fechas inclusivas. |
| `salon_horario_excepcion` | PK `id`; FK salón; unique parcial `(salon_id,fecha)` activo | Reemplaza el horario semanal de una fecha; cierre o horario especial. |
| `instructor_actividad` | PK `(usuario_id,tipo_actividad_id)` | Especialidad maestra actual. |
| `salon_tipo_actividad` | PK `(salon_id,tipo_actividad_id)` | Oferta maestra actual del salón. |
| `usuario_rol` | Relación usuario/rol/salón | Determina rol `INSTRUCTOR` global o por salón. |

No existe físicamente `Sesion`, tabla de ocurrencias, crosswalk legacy/nuevo, identidad de
programación en `Reserva` ni fence por salón.

### 6.3 Migraciones relevantes

| Migración | Evidencia material relevante para F2E |
| --- | --- |
| V9/V11 | Crean `salon`, relación de sede en `usuario_rol`, horario, actividad y oferta por salón; son identidades/maestros compartidos, no un mapping de programación. |
| V15 | Crea `turno_instructor`, `reserva` y especialidades. Reserva nace sin FK a turno. |
| V17–V22 | Evoluciona turno a múltiples instructores y filas instructor/actividad con rango opcional. |
| V18 | Crea excepción operativa exacta por fecha. |
| V38 | Añade participantes por reserva a actividad; no añade identidad de ocurrencia. |
| V41 | Crea aditivamente bloque/asignación, sin backfill legacy. |
| V42 | Añade políticas futuras del salón, todavía no equivalentes a una sesión. |
| V43–V46 | Versiona y endurece el horario operativo. |
| V47 | Preaudita solapes de serie, añade EXCLUDE y crea ajustes; no migra datos legacy ni los repara. |

### 6.4 Controllers, jobs y superficies públicas

- `TurnoInstructorController` publica GET/POST/PATCH/DELETE en `/api/turnos-instructor` y GET de
  puntuales. Es la superficie productiva de programación.
- `ReservaController` publica GET, GET `/mias`, POST y DELETE en `/api/reservas`.
- controllers de horario operativo y excepciones publican writers/readers productivos.
- controllers de salón, actividad, usuario y especialidades modifican maestros consumidos por la
  programación.
- no existe controller ni endpoint bajo el paquete `programacion`.
- el único `@Scheduled` encontrado está en `PagoService` y es ajeno a programación/reservas. No
  existe job/worker de programación, migración, reconciliación o cutover.

### 6.5 Frontend web y mobile

El frontend web accesible físicamente:

- usa `/turnos-instructor` para listar, crear, actualizar, eliminar y consultar puntuales;
- usa horarios versionados y excepciones de salón;
- no contiene llamadas a `ProgramacionEfectiva`, bloques, asignaciones ni ajustes nuevos;
- sus pantallas de reservas son placeholders `Próximamente` y no llaman `/reservas`.

El mobile accesible físicamente:

- sólo integra backend para autenticación y perfil;
- obtiene clases desde `src/data/clases.ts`, datos estáticos generados respecto de la fecha local;
- no llama endpoints de turnos, reservas ni programación nueva;
- por ello es consumer visual potencial, no consumer productivo confirmado de ninguna autoridad de
  programación backend.

### 6.6 Services y repositories de programación/reservas

El inventario backend relevante queda delimitado así:

| Modelo | Services/records | Repositories físicos |
| --- | --- | --- |
| Legacy | `TurnoInstructorService`, `ReservaService`, `EspecialidadInstructorService`, `ImpactoTurnosRecurrentesEnHorario`, `ImpactoPuntualEnExcepcionHorario` | `TurnoInstructorRepository`, `TurnoInstructorAsignacionRepository`, `ReservaRepository` |
| Nuevo recurrente | `BloqueProgramacionService`, `ProgramacionNominal`, `ProgramacionPolicyA` | `BloqueProgramacionRepository`, `AsignacionRepository` |
| Nuevo puntual/efectivo | `AjusteProgramacionFechaService`, `AjusteProgramacionFechaPersistence`, `AplicadorAjustesProgramacion`, `ProgramacionEfectiva`, `ProgramacionValidador`, `ProgramacionDiagnostico` y records de ocurrencia | `AjusteProgramacionFechaRepository` |
| Operación compartida | `HorarioEfectivoSalon`, `VersionarHorarioOperacion`, `CerrarHorarioOperacion`, `SalonHorarioExcepcionService`, locks de salón/instructor e impactos de horario | repositorios de horario, excepción, salón y maestros |

Los repositories nuevos son readers/writers internos según el service que los invoque; ninguno está
expuesto por controller. No existe repository de sesión, ocurrencia persistida, crosswalk o fence.

## 7. Matriz legacy / nuevo

Abreviaturas: `TI` = `TurnoInstructor`; `BP` = `BloqueProgramacion`; `ASG` = `Asignacion`; `APF` =
`AjusteProgramacionFecha`; `PE` = `ProgramacionEfectiva`.

| Concepto | Legacy representation | New representation | Current authority | Current writer | Current reader | Current consumers | Materialized? | Productive? | Mapping available? | Migration status | Cutover blocker? | Evidence / notes |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| Programación recurrente | TI `RECURRENTE` + usuarios/asignaciones | BP + ASG | TI | `TurnoInstructorService` | TI repo/service | Web, `ReservaService`, validadores de horario | Ambos | Sólo legacy | No crosswalk | No iniciada | Sí | V15–V22 vs V41; granularidad distinta. |
| Bloque | Una fila TI agrupa rango e instructores | BP agrupa salón/día/rango/vigencia | TI | TI service / BP service interno | TI service / repositorio BP | Web legacy / internos | Sí | TI sí; BP no | No | Coexistencia | Sí | BP tiene vigencia/serie; TI no. |
| Asignación | PK turno+usuario+actividad, rango opcional | ASG con UUID, serie, versión y rango requerido | TI | TI service / BP service interno | TI service / `ProgramacionNominal` | Web/reservas legacy / PE interno | Sí | Legacy sí | No | No iniciada | Sí | No hay relación entre las filas. |
| Ajuste puntual | TI `EXCEPCION`/`CANCELACION` sin target individual | APF target o adición | TI | TI service / APF service interno | `ReservaService` / PE | Web-reservas legacy / ninguno productivo nuevo | Sí | Legacy sí; APF no | No | No iniciada | Sí | Semánticas no isomorfas. |
| Ocurrencia nominal | Derivada de TI por fecha | `OcurrenciaNominal` por serie/fecha | TI | No aplica | `ReservaService` deriva ventanas; `ProgramacionNominal` deriva nueva | Internos | Runtime | Sólo derivación legacy productiva | No | No iniciada | Sí | No se persiste identidad de ocurrencia. |
| Programación efectiva | Precedencia en `ReservaService` y UI de TI | PE: nominal→ajustes→operación→maestros | TI | No aplica | `ReservaService` / PE | Backend y web legacy / interno nuevo | Sí | Sólo legacy | No | Dark launch | Sí | Dos algoritmos distintos; no dual-read productivo. |
| Horario operativo | N/A como programación | `HorarioOperacion` versionado | Modelo operativo endurecido | services de horario | `HorarioEfectivoSalon` | TI, reserva, PE, web | Sí | Sí | No aplica | Cerrada | Condicional | Es input compartido, no programación. |
| Excepción operativa | `SalonHorarioExcepcion` | La misma entidad endurecida | Modelo operativo endurecido | service de excepciones | `HorarioEfectivoSalon` | TI, reserva, PE, web | Sí | Sí | No aplica | Cerrada | Condicional | APF no protege todavía este writer. |
| Reserva | Snapshot sin identidad de turno/ventana | Referencia futura explícita todavía por elegir en D04: occurrence, ventana, occurrence+subintervalo u otra identidad diseñada | `Reserva` legacy | `ReservaService` | Reserva repo/service | Backend; web aún placeholder | Legacy sí; futura no | Sí legacy | No | No iniciada | Sí, absoluto | Una reserva que requiera occurrence presente necesita `EXACT`/`EQUIVALENT` y selección única; `EXPECTED_ABSENCE` del ajuste no autoriza descartar una reserva ni evita resolver D04. |
| Instructor | FK usuario + rol/especialidad | Mismos maestros por UUID | Maestros actuales | Usuario/especialidad | Ambos modelos | Todos | Sí | Sí | UUID compartido | No aplica | Sí si inconsistente | La identidad se comparte; la validez puede cambiar. |
| Salón | FK salón | UUID salón | Maestro actual | `SalonService` | Ambos modelos | Todos | Sí | Sí | UUID compartido | No aplica | Sí si inconsistente | Fence futuro debe ser por salón/cohorte. |
| Actividad | FK actividad; asignaciones legacy pueden faltar | UUID actividad obligatorio por ASG/resultado | Maestro actual | controller de actividad y salón | Ambos modelos | Todos | Sí | Sí | UUID compartido | No aplica | Sí si inconsistente | Mismo UUID no demuestra misma asignación. |
| Serie | No existe | `serie_id` de BP/ASG | Ninguna productiva | BP service interno | nominal/PE | Internos | Sí en nuevo | No | No | No iniciada | Sí | Debe crearse y persistirse crosswalk. |
| Reemplazo/cancelación | Precedencia global por instructor/salón/fecha | Target exacto `serieId+fecha` | TI | TI service / APF interno | Reserva legacy / PE | Legacy / interno | Sí | Legacy sí | No general | No iniciada | Sí | TI no identifica la ocurrencia sustituida. |
| Adición | TI `EXCEPCION`, sin identidad de adición separada | `ajusteId+fecha` | TI | TI service / APF interno | Reserva legacy / PE | Legacy / interno | Sí | Legacy sí | No | No iniciada | Sí | Coincidencia de campos no crea identidad. |

## 8. Writers físicos

| Writer / entry point | Escribe | Autoridad | Transacción y locks | Coexistencia / riesgo | Condición futura |
| --- | --- | --- | --- | --- | --- |
| `TurnoInstructorService.crear/actualizarTurno/eliminar` vía controller | TI, usuarios del turno, asignaciones legacy | Legacy productiva | `@Transactional`; `SalonLock` en recurrente/excepción y update; delete sin lock; sin `InstructorLocks` | Puede divergir del nuevo modelo; reemplaza filas de asignación; no conoce fence/crosswalk | Rechazo por fence y coordinación cross-model antes de activar `NUEVA`. |
| `ReservaService.crear/cancelar` vía controller | `reserva` | Reserva legacy productiva | `@Transactional`; create usa `SalonLock`; no lock de instructor ni identidad de ocurrencia | Precheck de traslape y lookup TI sin backstop de identidad | Writer debe exigir referencia inequívoca nueva en `NUEVA`; no dual-write. |
| `BloqueProgramacionService.crearBloque` | BP | Nuevo interno, no productivo | `@Transactional`; `SalonLocks` | No controller; sólo create; una fila puede afectar validación inversa del horario | Fence interno y autoridad explícita antes de exponer. |
| `BloqueProgramacionService.crearAsignacion` | ASG | Nuevo interno, no productivo | `@Transactional`; salones→instructores, relectura, Policy A | Sólo create; no update/deactivate/version writer físico | Completar lifecycle bajo el mismo protocolo en intervención posterior. |
| `AjusteProgramacionFechaService.guardarAdicion/retirarAdicion/guardarTarget/retirarTarget` | APF | Nuevo interno dark launch | `@Transactional`; locks ordenados, relectura y flush explícito | No controller; puede escribir sólo por invocación interna | Mantener no productivo hasta fence y gate. |
| `SalonHorarioOperacionService.versionar/cerrar` → `VersionarHorarioOperacion` / `CerrarHorarioOperacion` | `horario_operacion` | Operación productiva | Una TX, `SalonLock`, filas `FOR UPDATE`, EXCLUDE | Endpoints POST `versiones/cierres`; consulta impactos TI y BP; BP no productivo puede vetar este writer | Resolver la interacción con fence; nunca dejar que autoridad no elegida vete silenciosamente. |
| `SalonHorarioExcepcionService.guardar/guardarPorFecha/eliminar/eliminarPorFecha` | excepción operativa | Operación productiva | `@Transactional`, `SalonLock`, unique parcial | Endpoints PUT/DELETE; valida TI puntual y reservas; no APF/PE | Adapter nuevo sólo en `NUEVA`, detrás de fence aprobado. |
| `EspecialidadInstructorService.actualizar` | `instructor_actividad` | Maestro productivo | TX; sin locks de programación ni análisis de impacto | Puede hacer que PE omita ocurrencias y afectar reservas futuras | Hardening inverso `REQUIRED` antes de cutover. |
| `UsuarioService.cambiarEstatus/actualizarSedesRol` | usuario/rol/salón | Maestro productivo | TX; sin `InstructorLocks`/impacto PE | Puede invalidar instructor, rol o alcance de salón | Hardening inverso `REQUIRED`. |
| `SalonService.actualizar` | salón y oferta de actividad | Maestro productivo | TX; sin `SalonLocks` de programación para oferta | Puede retirar actividad ofrecida y hacer fail-closed en PE | Hardening inverso `REQUIRED`. |
| `TipoActividadController.crear/actualizar/desactivar` | actividad | Maestro productivo | Escritura directa por repositorio; sin protocolo cross-model | Puede invalidar ASG/APF/PE | Extraer writer endurecido; `REQUIRED`. |

No se encontró writer físico de actualización/desactivación/versionado para BP/ASG ni writer de
estado `activo` del salón. Su existencia no se presupone; cualquier entry point futuro debe nacer
dentro del fence, locks, Policy A y trazabilidad aprobados.

## 9. Readers y consumers físicos

| Reader / consumer | Lee hoy | Productivo | Identidad usada | Riesgo antes de moverlo |
| --- | --- | --- | --- | --- |
| `TurnoInstructorController/Service` | TI y asignaciones legacy | Sí | `turno.id` y campos; sin serie | Cambiarlo sin crosswalk rompe web y autoridad. |
| `ReservaService` | puntuales y recurrentes TI + horario efectivo | Sí | salón+instructor+fecha y ventanas; no turno/serie | Dual-read puede aceptar/rechazar distinto; requiere identidad explícita. |
| `ReservaRepository` | reservas por salón, instructor, cliente y fecha | Sí | `reserva.id` y snapshots | No puede agrupar por ocurrencia real. |
| `ImpactoTurnosRecurrentesEnHorario` | TI recurrente | Sí, como validador de writer operativo | `turno.id` | Debe seguir sólo la autoridad elegida por fence. |
| `ImpactoPuntualEnExcepcionHorario` | TI excepción + reserva confirmada | Sí | IDs legacy y fecha | En `NUEVA` necesita equivalente nuevo/reservas asociadas. |
| `ProgramacionNominal` | ASG+BP vigentes | No | `serieId+fecha`, IDs de versiones | No detecta equivalencia legacy. |
| `AplicadorAjustesProgramacion` | APF activos y nominales | No | target `serieId+fecha` o adición `ajusteId+fecha` | Precedencia nueva no es isomorfa a puntuales TI. |
| `ProgramacionEfectiva` | nominales + APF + horario + maestros | No | `ReferenciaOcurrencia` | Sin API/fence; fail-closed puede omitir sin métrica durable. |
| `ProgramacionValidador` | salón, horario, usuario/rol, actividad, especialidad y oferta | No | referencia de ocurrencia y UUIDs maestros | Una mutación maestra posterior puede invalidar u omitir una ocurrencia. |
| `ImpactoBloquesEnHorario` | BP activos | Sí como validador indirecto del writer de horario | `bloque.id` | Acoplamiento productivo parcial del modelo no productivo; debe ser consciente del fence. |
| Web `calendario.ts` / `SalonHorarios` | TI y horario/excepciones | Sí | IDs TI | Debe cambiar en un switch único por salón/cohorte; no mezclar fuentes. |
| Web reservas | Sólo placeholders | No como cliente API | Ninguna | No asumir consumidor migrado. |
| Mobile clases | Dataset local `clases.ts` | No como cliente API | Strings/IDs estáticos | API e identidad futuras no existen. |

Riesgo de dual-read: alto. Los algoritmos legacy y nuevo no comparten identidad ni semántica de
puntuales. Una comparación shadow puede leer ambos; una decisión productiva nunca puede combinar
sus resultados ni hacer fallback a la fuente que “tenga datos”.

## 10. Divergencias físicas clasificadas

Estas divergencias no se corrigen en F2E/preparación:

1. `Reserva` se documenta y comenta como reserva dentro de un turno, pero no persiste `turno_id` ni
   otra referencia de programación. Clasificación: `ARCHITECTURAL / CUTOVER_BLOCKER`.
2. `ImpactoBloquesEnHorario` es un bean físico y las listas de validadores del writer operativo lo
   consumen. Por tanto filas BP no productivas ya pueden rechazar un cambio productivo de horario.
   Esto no convierte BP en reader authority, pero demuestra coexistencia con efecto inverso que el
   fence futuro debe gobernar. Clasificación: `ARCHITECTURAL / TRANSITION_RISK`.
3. El dark launch está aislado por ausencia de controllers/consumers, no por fence persistido.
   Clasificación: `TECHNICAL / FUTURE_BLOCKER`.
4. Web programa con TI; mobile muestra programación local estática; web reservas no usa el backend
   aunque éste publica endpoints. Clasificación: `CONSUMER_DIVERGENCE / FUTURE_BLOCKER`.
5. Los maestros pueden invalidar PE y hoy sólo provocan omisión logueada; sus writers no aplican
   hardening inverso. Clasificación: `TECHNICAL / FUTURE_BLOCKER`.
6. TI no tiene vigencias ni series; sus tipos legacy `EXCEPCION` y `CANCELACION` no targetean una
   ocurrencia. No hay transformación automática total hacia APF. Clasificación:
   `DATA_AND_BUSINESS_POLICY`.

Ninguna divergencia cambia el authority check de arranque: la ruta productiva de programación y
reservas continúa leyendo TI. Sí bloquean fases futuras hasta su resolución.

## 11. Reservas — inspección física exacta

`Reserva` persiste:

```text
id
salon_id -> salon
instructor_id -> usuario
cliente_id -> usuario
tipo_actividad_id -> tipo_actividad
fecha
hora_inicio
hora_fin
estado CONFIRMADA|CANCELADA
timestamps heredados
```

No persiste:

```text
turno_instructor_id
turno_instructor_asignacion
programacion_asignacion_id
serie_id
ajuste_id
ReferenciaOcurrencia
sesion_id
```

`ReservaService.crear`:

1. autoriza salón y adquiere `SalonLock`;
2. carga salón, instructor, cliente y actividad;
3. calcula `horaFin` desde la duración actual de la actividad y la guarda como snapshot;
4. valida `HorarioEfectivoSalon`;
5. valida especialidad actual;
6. deriva turnos por `instructor+salon+fecha` con precedencia
   `CANCELACION → EXCEPCION → RECURRENTE`;
7. sólo exige que el intervalo quepa en algún turno;
8. no verifica que la actividad o el rango de `TurnoInstructorAsignacion` correspondan a ese turno;
9. rechaza cualquier reserva confirmada solapada del mismo instructor;
10. guarda el snapshot, no la identidad del turno que permitió reservar.

Queries dependientes: por instructor/fecha/estado, salón/fecha/estado, cliente/estado y traslape
half-open (`inicio < otroFin AND fin > otroInicio`). No hay query por turno, serie, ajuste u
ocurrencia.

Consecuencia: ni siquiera la identidad fuente legacy de una reserva histórica puede reconstruirse
siempre de forma única. La coincidencia de salón, instructor, actividad, fecha e intervalo es una
clave candidata de evidencia, nunca identidad.

## 12. Estrategia preparatoria de identidad y mapping de reservas

### 12.1 Identidades

El crosswalk no relaciona una única clase universal de objeto. Debe conservar explícitamente el
átomo de origen y el átomo target; coincidencia de campos entre átomos distintos no crea identidad.

| Átomo | Identidad físicamente disponible o identidad de diseño | Observación contractual |
| --- | --- | --- |
| A. Turno legacy | `turno_instructor.id` | Puede agrupar varios instructores, actividades y ventanas; no tiene serie ni vigencia. |
| B. Asignación legacy | `(turno_id, usuario_id, tipo_actividad_id)` + rango nullable | Es una fila distinta del turno. Ambos rangos null significan fallback operativo al rango del turno, no identidad de Reserva. |
| C. Serie nueva | `serie_id` tipado como serie de bloque o serie de asignación | Identidad lógica versionable; no es por sí sola una ocurrencia fechada. |
| D. Asignación nueva | `programacion_asignacion.id`, con `serie_id` y vigencia | La fila física/versionada no se confunde con su serie lógica. |
| E. Ocurrencia efectiva en fecha | `ReferenciaOcurrencia + fecha`: serie de asignación para nominal/reemplazo o ajuste para adición | Proyección runtime; no existe tabla `sesion` ni ocurrencia persistida. |
| F. Ajuste puntual | `programacion_ajuste_fecha.id`; target de serie+fecha o identidad de adición | Cancelación, reemplazo y adición no comparten la misma cardinalidad. |
| G. Ventana reservable | Ventana derivada que permitió reservar en legacy; identidad futura todavía abierta | Puede ser una ventana padre mayor que el intervalo reservado y hoy no se persiste. |
| H. Reserva | `reserva.id` + snapshot persistido de salón, instructor, actividad, fecha e intervalo | Es un hecho reservado, no el turno, la asignación, la ventana ni la ocurrencia completa. |

Identidades runtime nuevas ya conocidas:

```text
RECURRENTE/REEMPLAZO: (SERIE_ASIGNACION, asignacion.serie_id, fecha)
ADICION:              (AJUSTE, ajuste.id, fecha)
```

Esto cierra la identidad de una ocurrencia nueva proyectada, pero no decide todavía cuál de los
átomos E/G —o qué composición de ellos— debe ser el target futuro de H. En particular:

```text
Reserva contenida en turno o ventana != Reserva idéntica al turno, ventana u ocurrencia
```

Una reserva `09:00–10:00` dentro de un turno `08:00–12:00` es físicamente válida. La contención
puede generar un candidato; no demuestra identidad ni equivalencia temporal exacta.

### 12.2 Crosswalk futuro

Se requiere un contrato `PROPOSED / FUTURE / NOT_MATERIALIZED` compuesto conceptualmente por un
resultado y su lista íntegra de candidatos. Los nombres y DDL no quedan autorizados.

Resultado de crosswalk:

```text
crosswalk_result_id
source_atom_kind (TURNO|ASIGNACION_LEGACY|RESERVA|...)
source_stable_identity
target_atom_kind esperado
mapping_status
candidate_count
selection_status
selected_target_identity nullable
selection_rationale nullable
ambiguity_status
evidence_snapshot_id
transform_version / resolver_version
created_at / verified_at
```

Para la relación específica `F ajuste puntual → E ocurrencia efectiva`, el resultado debe hacer
explícitos ambos ejes y no reutilizar un solo conteo para ellos:

```text
source_adjustment_identity
adjustment_type (CANCELACION|REEMPLAZO|ADICION)
nominal_target_identity nullable
nominal_target_candidate_count
nominal_target_mapping_status (NOT_APPLICABLE|EXACT|EQUIVALENT|MULTIPLE_CANDIDATES|AMBIGUOUS|MISSING|DIVERGENT_INCOMPATIBLE)
effective_result_status (PRESENT|EXPECTED_ABSENCE|MISSING|DIVERGENT_INCOMPATIBLE)
effective_occurrence_count
effective_target_identity nullable
suppression_reason nullable
evidence + provenance
mapping_status
```

Estados auxiliares de esos dos ejes:

| Eje | Estado | Definición |
| --- | --- | --- |
| Target nominal | `NOT_APPLICABLE` | El tipo no usa target nominal; sólo corresponde a `ADICION`. |
| Resultado efectivo | `PRESENT` | El ajuste válido produce exactamente una occurrence efectiva identificada. |
| Resultado efectivo | `EXPECTED_ABSENCE` | La cancelación válida suprime intencionalmente la nominal demostrada y produce cero occurrences. |
| Target/resultado | `MISSING` | El tipo exige esa entidad o presencia y no existe; es anomalía y blocker. |
| Target/resultado | `DIVERGENT_INCOMPATIBLE` | Existe evidencia relacionada, pero incumple la forma, cardinalidad, maestros, horario o contenido obligatorio. |

`nominal_target_candidate_count` cuenta candidatos a la nominal que un ajuste targetea;
`effective_occurrence_count` cuenta resultados después de aplicar el ajuste. Un cero en el segundo
campo no permite inferir un cero en el primero ni clasificar automáticamente `MISSING`. Para una
cancelación válida se conserva `nominal_target_identity=(SERIE_ASIGNACION, serieId, fecha)`,
`nominal_target_candidate_count=1`, `effective_result_status=EXPECTED_ABSENCE`,
`effective_occurrence_count=0`, `effective_target_identity=null` y una `suppression_reason`
versionada; el `mapping_status` de esa relación es `EXPECTED_ABSENCE`. Para una adición, la ausencia
de target nominal es `NOT_APPLICABLE`, no `MISSING` ni `EXPECTED_ABSENCE`. Un `REEMPLAZO` válido
conserva `nominal_target_identity=(SERIE_ASIGNACION, serieId, fecha)` y usa esa misma referencia como
`effective_target_identity`; una `ADICION` válida usa
`effective_target_identity=(AJUSTE, ajusteId, fecha)`. Ambos registran `PRESENT` y conteo efectivo
uno cuando satisfacen horario y maestros.

Lista de candidatos/evidencia, con `0..N` filas por resultado:

```text
crosswalk_result_id
candidate_target_atom_kind
candidate_target_identity
relationship_evidence (IDENTITY|EQUIVALENCE_RULE|CONTAINMENT|FIELD_MATCH|EXTERNAL_EVIDENCE)
normalized_fields + evidence hash
eligibility/rejection reason
```

La lista nunca se reduce de `N` a `1` por orden, proximidad, primera fila o score. `selection_status`
distingue al menos `NO_SELECTION`, `UNIQUE_SELECTED`, `EXPLICIT_EVIDENCE_SELECTED` y `BLOCKED`; la
selección exige una regla inequívoca ya aprobada y una rationale reproducible.

Vocabulario canónico de `mapping_status` para todo el checkpoint:

| Estado | Significado |
| --- | --- |
| `EXACT` | La identidad del átomo está demostrada por crosswalk/provenance estable y sus campos obligatorios coinciden. |
| `EQUIVALENT` | Una regla de transformación allowlisted demuestra equivalencia del átomo sin cambiar intención. |
| `EXPECTED_ABSENCE` | La identidad nominal requerida y la operación válida están demostradas, y esa operación produce intencionalmente cero ocurrencias efectivas. Es un resultado válido y reconciliable, no un fallo de candidatos. |
| `MULTIPLE_CANDIDATES` | Existen dos o más targets plausibles; se conservan todos y no hay selección automática. |
| `AMBIGUOUS` | La evidencia o semántica no permite una única interpretación, incluso si el conteo sintáctico es uno. |
| `MISSING` | El contrato exige una entidad, target nominal o resultado presente y no existe ningún candidato elegible. Es una anomalía fail-closed y `BLOCKER`; nunca representa una supresión válida. |
| `UNSUPPORTED` | La forma source se reconoce, pero no existe semántica de transformación aprobada. |
| `DIVERGENT_INCOMPATIBLE` | Existe candidato o vínculo previo, pero difiere materialmente o viola precondiciones. |
| `INVALIDATED` | Un mapping antes válido dejó de verificar contra source, target o evidencia vigente. |

`MULTIPLE_CANDIDATES`, `AMBIGUOUS`, `MISSING`, `UNSUPPORTED`, `DIVERGENT_INCOMPATIBLE` e `INVALIDATED` son
`BLOCKER`; no son variantes de éxito. `EXPECTED_ABSENCE` no pertenece a esa lista: su validez
exige haber demostrado primero el target nominal y la semántica de la operación.

Cardinalidades que el contrato debe representar sin asumir isomorfismo:

| Relación | Cardinalidades posibles | Precondición física exacta | Cuándo deja de ser válida / evidencia requerida |
| --- | --- | --- | --- |
| A turno legacy → C serie nueva | `1:1`, `1:N`, `N:1`, `MISSING`, `AMBIGUOUS`, `DIVERGENT_INCOMPATIBLE` | Sólo para un turno recurrente con intención única, vigencia explícita, granularidad compatible y regla de agrupación aprobada | Varias filas/rangos, consolidación entre turnos, vigencia desconocida o puntuales no targeteadas; evidencia: source snapshot, regla versionada y lista de candidatos. |
| B asignación legacy → D asignación/serie nueva | `1:1`, `1:N`, `N:1`, `MISSING`, `AMBIGUOUS`, `DIVERGENT_INCOMPATIBLE` | Sólo con instructor, actividad, rango completo/intención demostrada, vigencia y serie inequívocos | Rango nullable usado sólo como fallback, versiones/series múltiples o consolidación; evidencia física de la fila, vigencia y provenance. |
| A/B → E ocurrencias por fecha | Normalmente `1:N` a través de fechas; también `EXPECTED_ABSENCE`, `MISSING` o `DIVERGENT_INCOMPATIBLE` | `1:1` sólo después de fijar una fecha concreta y demostrar una referencia efectiva única | Horizonte múltiple, ajuste, maestro u horario incompatible; una cancelación válida explica `EXPECTED_ABSENCE`, mientras ausencia sin esa provenance es `MISSING`. |
| F `CANCELACION` → target nominal | Exactamente `1:1` por `(serieId,fecha)` | Siempre que el ajuste activo sea válido: la implementación exige una única `OcurrenciaNominal` y una única operación target activa | Cero nominales = `MISSING`; más de una nominal o más de un target activo = invariant violation/ambigüedad. Evidencia: referencia de serie+fecha y snapshot nominal. |
| F `CANCELACION` → E ocurrencia efectiva | Exactamente `1:0` esperado | Target nominal `1:1` demostrado y forma de cancelación válida sin campos de resultado | `EXPECTED_ABSENCE`, con provenance del target nominal y motivo de supresión. Una occurrence presente después de aplicar la misma cancelación es `DIVERGENT_INCOMPATIBLE`. |
| F `REEMPLAZO` → target nominal | Exactamente `1:1` por `(serieId,fecha)` | La implementación exige una única `OcurrenciaNominal` y un único target activo | Cero nominales = `MISSING`; múltiples nominales/targets = invariant violation/ambigüedad. El snapshot resultado completo y de rango positivo es obligatorio. |
| F `REEMPLAZO` → E ocurrencia efectiva | Exactamente `1:1` esperado | La composición sustituye la nominal por una occurrence con `Origen.REEMPLAZO`, snapshot resultado y referencia estable `serieId+fecha` | Cero resultados finales por incompatibilidad de horario/maestros es `DIVERGENT_INCOMPATIBLE`, no supresión esperada; duplicados bloquean. |
| F `ADICION` → target nominal | `NOT_APPLICABLE` | La forma física exige `asignacion_serie_id=null`; no consulta nominal ni requiere template | La ausencia de target nominal es correcta para este tipo y no se clasifica como `MISSING` ni como `EXPECTED_ABSENCE`. |
| F `ADICION` → E ocurrencia efectiva | Exactamente `1:1` esperado | La forma física exige snapshot resultado completo y rango positivo; la occurrence usa `Origen.ADICION` e identidad `ajusteId+fecha` | Cero resultados finales por incompatibilidad de horario/maestros es `DIVERGENT_INCOMPATIBLE`; un mismo ajuste no autoriza `1:N`. |
| H Reserva → G ventana reservable | `N:1`, `1:1`, `1:N`, `MISSING`, `AMBIGUOUS` | `1:1` sólo si la identidad futura de la ventana y la asociación están persistidas o demostradas inequívocamente | Varias ventanas contenedoras o ninguna referencia persistida; contención sola sólo genera candidatos. |
| H Reserva → E ocurrencia efectiva | `1:1`, `N:1`, `1:N`, `MISSING`, `AMBIGUOUS`, `DIVERGENT_INCOMPATIBLE` | Sólo después de cerrar D04 y demostrar la regla elegida para target/subintervalo | Una occurrence puede admitir varias reservas; una reserva puede quedar contenida en varios candidates; evidencia de identidad, no sólo campos. |

No se propone FK directa a `asignacion.serie_id`: admite versiones y no es unique simple. El
target seleccionado, cuando la decisión correspondiente esté cerrada, debe verificarse mediante el
resolver de ocurrencias. Si se materializa una `Sesion` en otra fase, cualquier evolución por
expand/contract debe conservar el resultado, todos sus candidatos y su provenance.

### 12.3 Regla de mapping

Antes de materializar un resolver/crosswalk de reservas debe cerrarse D04 mediante evidencia y
audit independiente. La referencia futura de H debe elegir explícitamente uno de estos modelos:

```text
A) occurrence/session exacta;
B) ventana padre reservable;
C) occurrence + subintervalo reservado;
D) otra identidad diseñada explícitamente con iguales garantías.
```

Estado actual de esa decisión: `OPEN / BLOCKING_FOR_NEXT_GATE`. La identidad runtime de E no
resuelve por intuición la identidad de G ni la referencia de H.

Regla fail-closed mientras D04 permanece abierta y, después, para todo caso individual:

1. resolver y conservar todos los source candidates que reproduzcan la decisión legacy en la
   fecha;
2. cuando una asignación legacy tenga ambas horas null, usar el rango del turno sólo para generar
   candidato o representar G; nunca como prueba universal de identidad exacta de H;
3. generar y conservar todos los target candidates con su átomo y evidencia;
4. aplicar únicamente reglas de identidad/equivalencia aprobadas para la cardinalidad concreta;
5. tratar igualdad o contención de fecha, salón, instructor, actividad e intervalo half-open como
   evidencia de contenido, no como identidad por sí sola;
6. exigir target presente, válido y no cancelado cuando el modelo elegido requiera occurrence;
7. persistir/emitir el resultado, la lista completa, selección, rationale y hashes reproducibles.

Resultado canónico:

```text
identidad demostrada y campos válidos -> EXACT
equivalencia allowlisted demostrada -> EQUIVALENT
operación válida que suprime intencionalmente el resultado -> EXPECTED_ABSENCE / RECONCILIABLE
0 target candidates cuando el tipo exige target -> MISSING / BLOCKER
0 effective occurrences cuando el tipo exige presencia -> MISSING o DIVERGENT_INCOMPATIBLE / BLOCKER
0 nominal target candidates para ADICION -> NOT_APPLICABLE
>1 candidates -> MULTIPLE_CANDIDATES + ambiguity_status=AMBIGUOUS / BLOCKER
semántica no decidible -> AMBIGUOUS / BLOCKER
forma reconocida sin regla aprobada -> UNSUPPORTED / BLOCKER
diferencia material -> DIVERGENT_INCOMPATIBLE / BLOCKER
```

Un reemplazo conserva `serieId+fecha`; una adición usa `ajusteId+fecha`; una cancelación nueva
válida conserva el mismo target nominal y registra `EXPECTED_ABSENCE`. Esa supresión no es blocker
de mapping por sí misma. Si existe una reserva confirmada cuyo contrato exige una occurrence
presente, la incompatibilidad reserva/supresión se clasifica y bloquea por separado; no convierte la
cancelación en `MISSING` ni puede repararse como adición implícita. No se inventa sesión, no se
divide o fusiona automáticamente una reserva y no se elige por proximidad.

Cobertura mínima de cutover: `100%` de reservas `CONFIRMADA` relevantes del salón/cohorte y ventana
de activación. El tratamiento histórico de canceladas/pasadas requiere política explícita, pero su
exclusión nunca puede ocultar una reserva futura confirmada.

## 13. Fuente de datos

Clasificación fresh:

```text
DATA_SOURCE_STATUS: DATA_SOURCE_NOT_AVAILABLE
OUTPUT_ENUM: NOT_AVAILABLE
```

Evidencia:

- no existe dump, backup, snapshot, CSV, SQLite ni artefacto de datos dentro del repositorio;
- `application.properties`, `docker-compose.yml` y metadata local describen posibles conexiones o
  volúmenes, pero no identifican una fuente concreta autorizada read-only para esta intervención;
- el handoff no concede acceso a una base ni convierte una configuración local en autorización.

No se intentó conectar a `localhost`, inspeccionar un volumen ni ejecutar queries. Este estado no
es `HUMAN_STOP`: el diseño puede completarse sin resultados materiales.

```text
AUDIT DESIGN: COMPLETADO EN ESTE CHECKPOINT
AUDIT EXECUTION: NO EJECUTADA / NO AUTORIZADA
```

## 14. Contrato futuro de auditoría de datos

Todo lo descrito en esta sección es `PROPOSED / FUTURE / NOT_MATERIALIZED`.

### 14.1 Source y permisos

- source admisible: snapshot restaurado, réplica o export coherente identificado por nombre,
  ambiente, timestamp y checksum;
- autorización expresa que nombre la fuente, owner, operador y alcance por salón/fecha;
- credencial exclusiva con `SELECT` sobre tablas allowlisted y sin permisos DDL/DML;
- transacción declarada read-only, sin funciones mutantes ni acceso a secretos en outputs;
- schema/Flyway fingerprint registrado antes de auditar;
- abortar si no puede demostrarse read-only o si la fuente cambia durante la corrida.

### 14.2 Schema assumptions a verificar

- Flyway aplicado hasta V47 y sin checksums inválidos;
- tablas y columnas de la sección 6 presentes;
- FKs, CHECKs, unique parciales y EXCLUDE con nombres/definiciones esperadas;
- timezone y fecha de negocio declaradas; intervalos de hora interpretados como `[inicio,fin)`;
- `dia_semana` 0=domingo;
- vigencias de fecha inclusivas; `null` como extremo abierto;
- snapshot consistente entre todas las tablas.

### 14.3 Tablas, joins e identidades

La auditoría debe leer como mínimo:

```text
turno_instructor
turno_instructor_usuario
turno_instructor_asignacion
reserva
programacion_bloque
programacion_asignacion
programacion_ajuste_fecha
salon
horario_operacion
salon_horario_excepcion
usuario
usuario_rol
rol
tipo_actividad
instructor_actividad
salon_tipo_actividad
flyway_schema_history
```

Joins clave: turno→salón→usuarios→asignaciones; reserva→maestros; ASG→BP→maestros; APF target por
serie/fecha y resultados por maestros; horario/excepción por salón/fecha; crosswalk generado por la
corrida, separado de las tablas productivas.

### 14.4 Checks conceptuales ejecutables

1. conteos totales/activos por tabla, salón, tipo y horizonte;
2. forma de TI por tipo, asignaciones faltantes, rangos nulos parciales y rangos fuera del bloque;
3. duplicados y overlaps de TI por semántica legacy;
4. expansión determinista de cada TI recurrente a candidatos BP/ASG;
5. unicidad y vigencias de series BP/ASG, incluida cardinalidad nominal por fecha;
6. forma, target nominal y cardinalidad efectiva por tipo, duplicados y referencias de APF;
7. maestros activos, rol, especialidad y oferta para cada ocurrencia;
8. cobertura del horario efectivo y gaps por fecha/vigencia;
9. comparación legacy/nuevo por salón, fecha e instructor;
10. mapping de cada reserva y distribución 0/1/>1;
11. writers detectados frente a estado de fence esperado;
12. huérfanos lógicos aunque las FKs físicas estén íntegras.

### 14.5 Outputs y reproducibilidad

Cada corrida debe producir artefactos inmutables con:

```text
run_id
tool/resolver version + commit
source fingerprint + snapshot timestamp
schema/Flyway fingerprint
scope por salón y ventana temporal
query/check id y versión
conteos de entrada/salida
anomalías sin PII innecesaria
hashes de outputs
started_at / completed_at
verdict por check
```

Los identificadores se conservan; correo, teléfono, foto y demás PII no son necesarios. Si una
evidencia requiere usuario, se usa UUID y acceso restringido. No se publican dumps ni credenciales.

## 15. Taxonomía de anomalías

Severidad futura: `BLOCKER` impide migración/cutover; `ERROR` exige remediación o exclusión aprobada;
`WARN` requiere evidencia pero no bloquea por sí sola.

| ID | Categoría física | Severidad | Evidencia | Detección automática | Tratamiento automático | Human review | Cutover blocker |
| --- | --- | --- | --- | --- | --- | --- | --- |
| A01 | Identidad source/target requerida ausente (`TARGET_REQUIRED_BUT_MISSING`) | BLOCKER | Tipo, IDs, joins y cardinalidad del eje nominal | Sí | No | Si no hay fuente externa | Sí |
| A02 | Identidad ambigua / múltiples mappings | BLOCKER | candidatos 0/1/>1 | Sí | No | Sí para resolver política | Sí |
| A03 | Duplicado físico o efectivo | BLOCKER | clave física o multiset normalizado | Sí | No | Sólo si negocio afirma diferencia | Sí |
| A04 | Series ASG activas solapadas | BLOCKER | serie + vigencias | Sí | No | No para detectar | Sí |
| A05 | Series BP duplicadas/fragmentadas | ERROR/BLOCKER | serie, salón, día, rango, vigencia | Sí | No | Sí si intención no está persistida | Sí si afecta scope |
| A06 | Vigencias incompatibles, gaps u overlaps | BLOCKER | intervalos inclusivos y cobertura | Sí | No | Política sólo para gap intencional | Sí |
| A07 | Referencia física o lógica rota | BLOCKER | FK/catálogo o `CANCELACION`/`REEMPLAZO` nueva sin la nominal única requerida | Sí | No | No para detectar | Sí |
| A08 | Actividad incompatible o ausente | BLOCKER | asignación/reserva vs maestro | Sí | No | Sí si requiere corrección de catálogo | Sí |
| A09 | Salón/instructor incompatible | BLOCKER | estado, rol, sede, oferta | Sí | No | Sí si intención dudosa | Sí |
| A10 | Reserva sin mapping | BLOCKER | reserva + cero targets | Sí | No | Sí | Sí absoluto |
| A11 | Reserva con múltiples mappings | BLOCKER | reserva + >1 targets | Sí | No | Sí | Sí absoluto |
| A12 | Legacy sin equivalente nuevo no explicado por supresión concordante | BLOCKER | diff resolver `MISSING_NEW` sin `EXPECTED_ABSENCE` válida en ambos universos | Sí | No | Sí si se descarta | Sí |
| A13 | Nuevo sin equivalente legacy no explicado por operación aprobada | ERROR/BLOCKER | diff `MISSING_LEGACY`; procedencia APF/migración | Sí | No | Sí | Sí salvo adición o supresión concordante aprobada |
| A14 | Divergencia de writer tras snapshot | BLOCKER | watermark/fingerprint/counters | Sí | No | No | Sí |
| A15 | TI instructor sin asignación o rango inconsistente | ERROR/BLOCKER | tablas legacy y reglas service | Sí | No | Sí | Sí si se migra |
| A16 | EXCEPCION/CANCELACION legacy sin target inequívoco | BLOCKER | candidatos de serie por fecha | Sí | No | Sí | Sí |
| A17 | Dato imposible de normalizar determinísticamente | BLOCKER | input, candidatos y regla fallida | Sí | No | Sí | Sí |
| A18 | Ocurrencia nueva omitida por fail-closed | BLOCKER | diagnóstico + causa maestra/horario | Sí | No | Según causa | Sí |
| A19 | Ocurrencia efectiva inesperada después de `CANCELACION` válida | BLOCKER | ajuste, nominal target, resultado efectivo y resolver versionado | Sí | No | No para detectar | Sí |

`Detección automática` significa clasificar y conservar evidencia. `Tratamiento automático`
significa reparar, elegir, deduplicar, dividir, fusionar o migrar; es `NO` para todas estas
anomalías. El bloqueo fail-closed puede ejecutarse automáticamente, pero no cuenta como remediación
del dato. En especial, una ambigüedad puede detectarse automáticamente y nunca tratarse eligiendo un
candidato. `EXPECTED_ABSENCE` con target nominal inequívoco no es una anomalía y no aparece como
fila de esta taxonomía; sí lo son el target requerido ausente y una occurrence presente pese a la
cancelación. No se declara que ninguna categoría esté ausente: no hubo fuente material.

## 16. Contrato futuro de normalización/migración

Todo componente de esta sección es `PROPOSED / FUTURE / NOT_MATERIALIZED`.

### 16.1 Source, target y elegibilidad

Source: snapshot autorizado de TI, usuarios/asignaciones, reservas y maestros. Target: BP, ASG,
APF y crosswalks explícitos. No se migra directamente desde resultados de UI.

Elegible automáticamente:

- TI `RECURRENTE` activo con exactamente un salón/día/rango válido;
- cada fila legacy instructor/actividad con rango completo determinable;
- maestros compartidos existentes y compatibles;
- política explícita de `vigenteDesde` aplicada;
- ausencia de overlaps, gaps o mappings ambiguos;
- identidad lógica estable reconciliada como nueva o ya vinculada al mismo target lógico con
  evidencia compatible.

Para un APF `CANCELACION` ya válido, la unidad elegible de provenance es el ajuste junto con su
target nominal único; el resultado normalizado es `EXPECTED_ABSENCE`. No se exige ni se crea una
occurrence efectiva para forzar cardinalidad `1:1`.

No elegible automáticamente:

- `EXCEPCION`/`CANCELACION` legacy sin target exacto;
- instructor sin actividad asignada;
- vigencia inicial inferida por “fecha probable”;
- reserva con cero o varios source/target candidates;
- cualquier transformación que cambie salón, instructor, actividad, intervalo o intención.

### 16.2 Transformaciones deterministas

- un TI recurrente elegible crea o referencia un BP con salón, día y rango;
- cada fila `(turno,usuario,actividad)` crea una ASG exactamente una actividad;
- el rango propio de la asignación sólo es exacto si ambos extremos existen; si ambos son null, el
  rango TI puede usarse para generar un candidato de ASG o representar una ventana padre, nunca
  para demostrar automáticamente la identidad exacta de una Reserva;
- series BP/ASG se generan de forma determinista desde una namespace de identidad estable,
  operación y source identity —no desde `run_id` ni desde una versión mutable de transformación—,
  o se almacenan en crosswalk antes de escribir;
- UUIDs compartidos de salón/instructor/actividad se conservan;
- ninguna EXCEPCION se convierte automáticamente en REEMPLAZO/ADICION sin target/provenance;
- ninguna CANCELACION se expande automáticamente a todas las series;
- una CANCELACION nueva con target nominal demostrado se preserva como supresión intencional en el
  crosswalk, con `effective_target_identity=null` y sin crear una occurrence sintética;
- `EXPECTED_ABSENCE` nunca se normaliza como registro faltante a reparar; sólo `MISSING` identifica
  una ausencia anómala cuando el contrato exige presencia;
- no se corrigen overlaps, maestros ni reservas en silencio.

### 16.3 Idempotencia, transacciones y batching

El contrato separa dos conceptos que no pueden compartir clave:

```text
A) IDEMPOTENCY / STABLE LOGICAL IDENTITY
B) RUN PROVENANCE
```

Identidad lógica estable conceptual:

```text
source_system
+ source_atom_kind
+ source_stable_identity
+ operation_kind
+ target_logical_kind
+ semantic cohort/scope cuando distinga unidades legítimas
+ stable target partition/role cuando una descomposición `1:N` aprobada cree unidades distintas
+ target_logical_identity cuando ya exista una selección aprobada
```

Para una asignación legacy, `source_stable_identity` es su PK compuesta; para un turno o Reserva es
su PK estable. La identidad no contiene `run_id`, timestamp, batch ni orden de lectura. Tampoco se
crea una identidad target nueva sólo porque cambie `transform_version`.

Una cardinalidad `1:N` exige un discriminador semántico estable por target (por ejemplo, la
identidad de la asignación source o un rol de partición aprobado), nunca un índice derivado del orden
de la corrida. En `N:1`, todas las sources conservan sus vínculos de provenance hacia una identidad
target grupal única. Así se evitan tanto colisiones legítimas como duplicados entre runs.

`transform_version` identifica la evaluación/regla aplicada a la identidad estable. Puede formar
parte de una clave de evaluación versionada, pero no evade el vínculo único entre la unidad lógica y
su target. `run_id`/`migration_run` registra exclusivamente provenance operacional, observabilidad y
audit trail: nunca decide si la unidad ya fue procesada.

Comportamiento obligatorio:

| Caso | Resultado |
| --- | --- |
| Misma identidad estable + misma versión + mismo input/output hash | `NO_OP + VERIFY`; puede agregar evidencia de la nueva corrida, nunca otro target lógico. |
| Misma identidad estable + misma versión + hash distinto | `DIVERGENT_INCOMPATIBLE / ABORT`; no sobrescribir ni duplicar. |
| Misma source + nueva `transform_version` + output lógico equivalente | `RE_EVALUATE + VERIFY`; conservar evaluación y run provenance nuevos, reutilizar el target existente. |
| Misma source + nueva `transform_version` + output lógico diferente | `BLOCKER`; exige política explícita de remigración/versionado, reconciliación y autoridad separada. No reescribe ni crea target automáticamente. |
| Target existente + provenance completa y compatible | `RECONCILE + VERIFY`; no crear duplicado. |
| Target existente + provenance ausente | Detectar y conservar evidencia; sólo vincular mediante política de adopción y prueba inequívoca autorizadas. En otro caso, `AMBIGUOUS / BLOCKER`. |

Así, si la corrida A procesa source X y la corrida B vuelve a procesar X con la misma
transformación, B localiza por identidad estable, verifica hashes/target y termina en `NO_OP`,
`VERIFY`, `RECONCILE` o divergencia; nunca crea un segundo target lógico.

Los hashes son evidencia de contenido normalizado, no sustitutos de `source_stable_identity` ni de
`target_logical_identity`. Deben distinguir input, transformación y output para diagnosticar el
cambio sin convertir una colisión o un cambio material en una identidad nueva.

Constraints conceptuales futuras, todas `PROPOSED / NOT_MATERIALIZED`:

- unicidad del vínculo activo entre identidad lógica estable + `operation_kind` y target lógico;
- unicidad de la evaluación para identidad estable + `transform_version` + input hash;
- unicidad de target lógico cuando el átomo target y su cardinalidad aprobada la requieran;
- FKs/constraints sólo después de cerrar el modelo de identidad correspondiente, especialmente
  D03/D04 para reservas.

No se autoriza SQL, Flyway ni materialización de estas constraints.

Semántica operacional:

- `retry`: repite un intento transitorio de la misma operación/run; relee por identidad estable en
  una transacción nueva;
- `resume`: continúa unidades pendientes de la misma corrida y verifica las ya completadas;
- `new independent run`: crea provenance nueva, pero reconcilia las mismas identidades estables;
- `reconciliation`: no presupone escritura; compara source, vínculo, target, hashes y provenance;
- nunca se reintenta automáticamente ambigüedad, conflicto semántico, checksum distinto o cambio de
  transformación con output material distinto;
- batch por unidad de fence aprobada, no por cantidad arbitraria que divida invariantes;
- una transacción por batch lógico con salones e instructores bloqueados en orden global;
- writers externos detenidos antes de la lectura final y durante cualquier escritura autorizada.

### 16.4 Audit trail y verificación

Registrar identidad lógica estable, `run_id` como provenance, input/output hashes, versión y reglas
aplicadas, output IDs, crosswalk y candidatos, conteos, timestamps, actor, batch, errores y
reconciliación. Después de cada batch: releer source congelado y target, ejecutar resolver,
verificar reservas y exigir cero divergencias materiales.

### 16.5 Rollback conceptual

Antes de habilitar writers nuevos, un batch puede volver a `LEGACY` sólo si:

- los writers externos siguieron detenidos;
- ninguna escritura nueva productiva ocurrió;
- todas las filas creadas se identifican inequívocamente por identidad lógica estable y provenance;
- la eliminación/retiro reversible no afecta datos preexistentes;
- se reconcilia otra vez legacy.

Después de una escritura productiva en `NUEVA`, no hay rollback automático a legacy. Se requiere
nueva intervención, reconciliación inversa y decisión humana; `NUEVA → LEGACY` directo está
prohibido.

## 17. Resolver comparativo legacy / nuevo

Componente: `PROPOSED / FUTURE / NOT_MATERIALIZED`.

### 17.1 Purpose e inputs

Comparar, sin decidir autoridad, dos proyecciones para el mismo `snapshot_id`, salón/cohorte,
fecha y ventana:

1. `LEGACY_ADMIN_OCCURRENCE`: TI efectivos expandidos por filas instructor/actividad;
2. `LEGACY_RESERVABILITY_WINDOW`: comportamiento exacto de `ReservaService`, separado porque no
   valida fila de asignación/actividad;
3. `NEW_EFFECTIVE_OCCURRENCE`: PE global después de ajustes, horario final y maestros;
4. crosswalk source→target y mappings de reserva.

Separar las dos proyecciones legacy evita declarar equivalencia falsa entre lo que muestra la
administración y lo que hoy permite reservar.

### 17.2 Lookup keys e identidad temporal

Lookup primario: `salon/cohorte + fecha`. Índices secundarios: instructor y referencia. La fecha se
resuelve con día 0=domingo, vigencias inclusivas y rangos de hora `[inicio,fin)`. La identidad se
obtiene del crosswalk; la tupla de campos sólo sirve para comparar contenido.

### 17.3 Output por elemento

El resolver usa `comparison_status`, distinto del `mapping_status` canónico de 12.2. Un resultado de
mapping no se renombra como divergencia del resolver y viceversa.

```text
EXACT_MATCH
EQUIVALENT_MATCH
EXPECTED_ABSENCE_MATCH
ACCEPTABLE_DIVERGENCE
MATERIAL_DIVERGENCE
AMBIGUOUS
MISSING_LEGACY
MISSING_NEW
INVALID_SOURCE
INVALID_TARGET
```

- `EXACT_MATCH`: el mapping del átomo comparado es `EXACT`, con la cardinalidad y precondiciones
  definidas para esa relación; no afirma un crosswalk universal `1:1`.
- `EQUIVALENT_MATCH`: el mapping es `EQUIVALENT`; una regla allowlisted explica la agrupación física
  y el multiset efectivo coincide en las dimensiones exigibles para ese átomo.
- `EXPECTED_ABSENCE_MATCH`: ambos universos preservan la misma unidad nominal inequívoca como
  cancelada/suprimida y producen cero occurrences efectivas. Deriva del estado canónico
  `EXPECTED_ABSENCE`; no deriva de observar dos listas vacías sin provenance.
- `ACCEPTABLE_DIVERGENCE`: sólo metadata no funcional explícitamente allowlisted; nunca tiempo,
  salón, instructor, actividad, presencia, estado o reservabilidad.
- `MATERIAL_DIVERGENCE`: cualquier diferencia funcional.
- `AMBIGUOUS`/`MISSING_*`: fail-closed; nunca escoger el más cercano.

Si un resultado de crosswalk conserva varios candidatos, el resolver emite `AMBIGUOUS` y adjunta la
lista completa. El resolver de reservas no puede producir `EXACT_MATCH`/`EQUIVALENT_MATCH` hasta que
D04 cierre la identidad de referencia de H y la regla de subintervalo/ventana.

Regla de cancelación:

```text
legacy nominal target inequívoco + legacy outcome cancelled/absent
+ new same nominal target inequívoco + new outcome EXPECTED_ABSENCE
-> EXPECTED_ABSENCE_MATCH

target nominal requerido no encontrado o ambiguo en cualquier universo
-> MISSING_* o AMBIGUOUS / BLOCK
```

La mera ausencia de occurrence no demuestra cancelación. El resolver conserva el adjustment ID,
la referencia nominal cancelada, la regla de equivalencia y la evidencia de ambos outcomes.

### 17.4 Equivalencia temporal exacta

Dos resultados presentes sólo son equivalentes cuando coinciden simultáneamente:

```text
fecha
salón final
instructor final
actividad final
hora_inicio
hora_fin
presencia `PRESENT` en ambos universos después de puntuales
maestros vigentes
horario efectivo del salón final
mapping `EXACT`/`EQUIVALENT` permitido para el átomo y cardinalidad concreta
provenance estable e íntegra
```

`REEMPLAZO` y `ADICION` esperan exactamente una occurrence por ajuste válido. Dos outcomes ausentes
sólo son equivalentes si ambos preservan el mismo target nominal `EXACT`/`EQUIVALENT`, la operación
de cancelación está demostrada, el conteo efectivo es cero y la provenance está completa; entonces
el resultado es `EXPECTED_ABSENCE_MATCH`. Una desaparición por horario/maestros fail-closed no se
reclasifica como cancelación.

La comparación de intervalos es exacta y half-open. Adyacencia no es overlap. Un rango recortado,
una fecha con diferente vigencia, un reemplazo evaluado contra el salón origen, una omisión
fail-closed o una precedencia puntual distinta son divergencias materiales.

Para H Reserva, “comparación exacta del intervalo” se aplica al subintervalo reservado respecto del
modelo que cierre D04; no exige que H ocupe todo el intervalo de E o G. La contención puede demostrar
compatibilidad de rango, nunca identidad target por sí sola.

### 17.5 Diagnóstico, observabilidad y criterios

Cada resultado incluye source IDs, target reference, campos normalizados, rule version, snapshot,
causa y hash. Métricas: total por estado, ratio material, 0/1/>1 mappings, series/ajustes y cobertura
de reservas por salón/fecha. Los conteos de ausencia se separan como mínimo en
`suppressed_expected_count`, `missing_unexpected_count`, `ambiguous_count` y `divergent_count`.

PASS de comparación para una unidad:

```text
MATERIAL_DIVERGENCE = 0
AMBIGUOUS = 0
MISSING inesperado = 0
INVALID_SOURCE/TARGET = 0
reservas confirmadas relevantes con mapping inequívoco = 100%
writer drift = 0
```

`EXPECTED_ABSENCE_MATCH` con provenance completa satisface el eje de presencia/ausencia y no
incrementa `MISSING inesperado`. Cualquier incumplimiento es `BLOCK`, no resultado parcial.

## 18. Diseño de transición writers/readers/consumers

### 18.1 Estado actual

- writer/read authority: TI;
- candidate future authority: BP+ASG+APF+PE, todavía no autorizada;
- reservas: writer/reader legacy;
- web: TI;
- mobile: sin consumer backend de programación;
- horario/maestros: compartidos y productivos.

### 18.2 Coexistencia peligrosa

- dual-write TI↔nuevo genera series y puntuales sin identidad común;
- dual-read con fallback oculta missing/divergencias;
- transición aislada por salón puede romper el no-solape global de un instructor que trabaja en
  varios salones;
- `TurnoInstructorService` no participa de `InstructorLocks` ni consulta programación nueva;
- `ImpactoBloquesEnHorario` ya crea un efecto inverso desde BP al writer operativo;
- reservas pueden crearse durante una migración contra una ventana que luego cambie.

### 18.3 Orden futuro obligatorio

1. re-audit fresh e independiente finalizado con `P0=0 / P1=0 / P2=0` y
   `F2E1_DESIGN_DOCUMENT_GATE=PASS`; ese audit aprobó el diseño/preparación, no una fase material;
2. con nuevo handoff exclusivamente de diseño/evidencia, cerrar D03, D04 y D08–D11 o declarar el
   subset detector-only que permanece seguro y fail-closed;
3. auditar fresh ese cierre antes de entregar arquitectura a un executor;
4. sólo con otro handoff material, implementar el subconjunto read-only cuya forma física, inputs,
   outputs y límites ya estén decididos; si D09/D10 siguen abiertas, sólo puede detectar y
   clasificar casos unsupported, sin producir mapping seleccionado/productivo;
5. ejecutar auditoría material únicamente sobre fuente nombrada y autorizada read-only;
6. materializar crosswalk/selección y resolver sólo después de D03/D04/D09/D10 y su gate; probar
   shadow comparison sin usarla para decisiones productivas;
7. materializar fence/enforcement en intervención separada sólo después de cerrar D08 y el contrato
   físico correspondiente de D11; el estado inicial sigue `LEGACY`;
8. cerrar anomalías, decisiones humanas aplicables, hardening y gates de writers/maestros;
9. seleccionar una cohorte segura y, bajo autoridad futura expresa, entrar a `MIGRANDO` deteniendo
   writers externos;
10. tomar snapshot final, migrar idempotentemente, mapear reservas y reconciliar;
11. obtener gate explícito y cambiar readers/writers como una sola authority transition;
12. entrar a `NUEVA`, rechazar legacy y retirar TI sólo tras cero consumers y contrato separado.

No se permite activar readers nuevos antes del fence ni writers nuevos antes de que los readers y
reservas estén preparados para la misma unidad.

## 19. Fence futuro por salón/cohorte

Componente: `PROPOSED / FUTURE / NOT_MATERIALIZED`.

| Estado | Writers externos | Readers productivos | Internos permitidos | Entry guard | Exit guard |
| --- | --- | --- | --- | --- | --- |
| `LEGACY` | Sólo TI/reserva legacy | Sólo legacy | Shadow read no autoritativo; ningún dato nuevo decide producto | Estado inicial o rollback verificado | Audit autorizado, cohorte definida, freeze plan y blockers cerrados |
| `MIGRANDO` | Detenidos/fail-closed para programación y reservas de la unidad | Sin dual-read; respuesta de mantenimiento o snapshot legacy definido | Auditor, migrador, resolver y reconciliador allowlisted | Freeze confirmado, watermarks iguales, locks/cohorte adquiridos | PASS total de migración, resolver, reservas y consumers |
| `NUEVA` | Sólo writers nuevos; legacy rechazado | Sólo PE/nueva y reservas con referencia nueva | Reconciliación/observabilidad | Gate de cutover explícito | No retorno automático; reverse migration autorizada |

Transiciones admitidas:

```text
LEGACY -> MIGRANDO -> NUEVA
MIGRANDO -> LEGACY sólo antes de writes productivos nuevos y tras rollback/reconciliación
```

Transiciones imposibles automáticamente:

```text
LEGACY -> NUEVA
NUEVA -> LEGACY
NUEVA -> MIGRANDO como rollback implícito
dos estados autoritativos para el mismo salón/cohorte
```

### 19.1 Restricción cross-salon

El fence nominal por salón no basta por sí solo: el no-solape de instructor es global y un
instructor puede trabajar en varios salones. Antes de implementar el fence debe elegirse y aprobarse
una de estas estrategias:

- cohorte atómica igual al componente conexo `salón↔instructor`; o
- protocolo cross-model que congele/valide todos los salones del instructor y haga participar a
  ambos writers en locks y checks compartidos.

Estado de la decisión: `D08 / BLOCKING_FOR_NEXT_GATE`. Mientras D08 siga abierta:

```text
FENCE MATERIALIZATION: NOT_AUTHORIZED
ENTRY MIGRANDO: NOT_AUTHORIZED
```

Hasta cerrar y auditar esa decisión, no puede entregarse el fence a un executor ni entrar ningún
salón realmente en `MIGRANDO`.

Observabilidad del fence: estado, versión, actor, motivo, timestamps, transición previa, snapshot,
watermarks de writers, batch de migración, último resolver PASS y reservas pendientes. Ninguno de
estos campos existe hoy.

## 20. Assessment de hardening inverso

| Componente | Resultado | Riesgo/evidencia | Gate afectado |
| --- | --- | --- | --- |
| `TurnoInstructorService` | `REQUIRED` | Writer productivo sin fence, `InstructorLocks` ni check cross-model | Entry a `MIGRANDO` y partial cutover |
| `ReservaService` | `REQUIRED` | Crea sin identidad persistida de turno/ventana/target y sólo contra TI | Migration/cutover de reservas |
| `EspecialidadInstructorService.actualizar` | `REQUIRED` | Puede retirar especialidad y hacer desaparecer PE | Cutover |
| `UsuarioService.cambiarEstatus` | `REQUIRED` | Puede suspender/eliminar instructor con ocurrencias/reservas | Cutover |
| `UsuarioService.actualizarSedesRol` | `REQUIRED` | Puede retirar rol/salón y activar fail-closed | Cutover |
| `SalonService.actualizar` | `REQUIRED` | Reemplaza oferta de actividad sin impacto PE/reservas | Cutover |
| `TipoActividadController.actualizar/desactivar` | `REQUIRED` | Writer directo puede invalidar actividad y duración | Cutover |
| `SalonHorarioExcepcionService` | `REQUIRED` en `NUEVA` | No consulta APF/PE; sólo objetos puntuales legacy | Cutover y consistencia operativa |
| Writers BP/ASG existentes | `REQUIRED` de completar lifecycle | Sólo existen creates; update/deactivate/version no materializados | Implementation/migration |
| Writer de `Salon.activo` | `DEFERRED_PENDING_EVIDENCE` | No se encontró entry point físico en el scope inspeccionado | Gate si aparece otro writer |

`REQUIRED` no autoriza implementar. El hardening debe respetar fence, locks globales, reservas
asociadas y no doble autoridad. El fail-closed read-time actual es necesario pero insuficiente para
un sistema nuevo productivo con reservas: una omisión silenciosa puede esconder una clase ya
reservada.

## 21. Observabilidad futura

Componentes `PROPOSED / FUTURE / NOT_MATERIALIZED`:

- conteos legacy/nuevo por salón, fecha, instructor y tipo de origen;
- `exact_count`, `equivalent_count`, `suppressed_expected_count`, `missing_unexpected_count`,
  `ambiguous_count` y `divergent_count`, sin sumar supresiones válidas a datos faltantes;
- cardinalidad de crosswalks 0/1/>1;
- cobertura de reservas total y confirmadas futuras;
- omisiones fail-closed por causa y referencia;
- writer attempts accepted/rejected por authority/fence;
- drift de watermarks durante auditoría/migración;
- batches pending/running/reconciled/rolled_back;
- estado y edad del último PASS por salón/cohorte;
- intentos de writer legacy en `NUEVA` y writer nuevo en `LEGACY`;
- transiciones de fence y actor.

Los logs SLF4J actuales de `ProgramacionDiagnostico` sólo registran omisiones. No son evidencia
durable suficiente de migración, equivalencia ni cutover.

## 22. Fail-closed, abort conditions y reconciliación

### 22.1 Reglas centrales

```text
dato ambiguo -> NO migrar automáticamente
mapping AMBIGUOUS o MULTIPLE_CANDIDATES -> BLOCKER
mapping MISSING, UNSUPPORTED, DIVERGENT_INCOMPATIBLE o INVALIDATED -> BLOCKER
mapping EXPECTED_ABSENCE con target nominal demostrado -> VÁLIDO / RECONCILIABLE
CANCELACION válida: nominal target count=1 + effective occurrence count=0 -> NO BLOCKER
CANCELACION/REEMPLAZO con nominal target count=0 -> MISSING / BLOCKER
ADICION con nominal target count=0 -> NOT_APPLICABLE; effective occurrence count esperado=1
reserva sólo contenida, sin identidad demostrada -> BLOCKER DE MAPPING/CUTOVER
equivalencia no demostrada -> BLOCKER
reserva sin referencia inequívoca -> BLOCKER DE CUTOVER
writer conflict/drift -> BLOCKER
authority inconsistente -> BLOCKER
sin snapshot coherente -> ABORT
sin fence efectivo -> NO MIGRANDO / NO NUEVA
```

No existe reparación silenciosa, deduplicación heurística, match por cercanía, “primera fila”,
fallback a la otra fuente ni selección de “lo más probable”.

### 22.2 Abort conditions

- fuente no identificada/autorizada/read-only o fingerprint cambiante;
- schema/Flyway distinto del contrato;
- writers externos no detenidos o watermarks que avanzan;
- locks/cohorte incompletos;
- `MISSING`, `MULTIPLE_CANDIDATES`, `AMBIGUOUS`, `UNSUPPORTED`, `DIVERGENT_INCOMPATIBLE` o
  `INVALIDATED` en
  mapping de serie, target nominal requerido, ocurrencia que debe estar presente o reserva;
- `CANCELACION` cuyo target nominal no puede identificarse exactamente o cuyo resultado conserva
  una occurrence efectiva;
- master data incompatible;
- overlaps/gaps/duplicados no clasificados;
- resolver material/ambiguous/missing inesperado no aprobado;
- fallo de batch con atomicidad no demostrada;
- audit trail incompleto;
- authority/fence distinto del esperado;
- necesidad de mutar fuera del scope autorizado;
- cualquier reserva confirmada futura sin target válido.

Una `CANCELACION` válida representada como `EXPECTED_ABSENCE` no activa por sí sola ninguna abort
condition. Su relación con reservas se evalúa separadamente bajo el contrato de D04.

### 22.3 Reconciliación

Pre: fingerprints, conteos, anomaly report y watermarks. Por batch: source IDs/hashes contra outputs
y crosswalks. Post: multiset resolver, mappings de reservas, masters, horario, writers y fence. La
reconciliación es repetible e idempotente; dos corridas sobre el mismo snapshot deben producir los
mismos hashes. Cualquier diferencia deja la unidad en `MIGRANDO` o vuelve a `LEGACY` si el rollback
seguro todavía es posible; nunca adelanta a `NUEVA`.

La reconciliación trata `EXPECTED_ABSENCE` como estado válido: comprueba el adjustment, el target
nominal y que no exista occurrence efectiva, preserva provenance y no crea una occurrence
sintética. Trata `MISSING` como anomalía cuando el tipo exige target o presencia; trata
`AMBIGUOUS` y `DIVERGENT_INCOMPATIBLE` como blockers. Dos universos que suprimen inequívocamente la
misma unidad nominal pueden reconciliar como `EXPECTED_ABSENCE_MATCH`; dos listas vacías sin esa
evidencia no.

## 23. Decisiones de F2E/preparación

Cada decisión aparece una sola vez con una de las categorías autorizadas.
`BLOCKING_FOR_NEXT_GATE` identifica aquí una decisión que puede permanecer abierta para el re-audit
documental de esta corrección, pero debe cerrarse antes del handoff/gate material del componente al
que afecta. No se difiere su arquitectura al executor.

| ID | Categoría | Decisión | Evidencia |
| --- | --- | --- | --- |
| D01 | `CLOSED_BY_F2E_PREPARATION` | TI sigue siendo única autoridad; PE no participa productivamente | Canónicos, controllers y consumers físicos |
| D02 | `CLOSED_BY_F2E_PREPARATION` | Auditoría y migración se separan; sin fuente no hay resultados | Handoff y ausencia de source autorizado |
| D03 | `BLOCKING_FOR_NEXT_GATE` | El crosswalk debe cerrarse por átomos A–H, cardinalidad por relación/tipo y evidencia; no existe mapping universal `1:1` ni selección silenciosa | Reserva sin FK, granularidad legacy/nueva, `CANCELACION` `1:0` efectiva y D04 abierta |
| D04 | `BLOCKING_FOR_NEXT_GATE` | La identidad de occurrence nueva es serie+fecha o ajuste+fecha, pero el target futuro de Reserva debe elegir occurrence, ventana padre, occurrence+subintervalo u otra identidad diseñada | `ReservaService` permite subintervalo contenido y no persiste ventana/turno |
| D05 | `CLOSED_BY_F2E_PREPARATION` | Resolver compara multiset efectivo, supresión esperada y proyección de reservabilidad por separado | Diferencia física de `ReservaService` y composición de `AplicadorAjustesProgramacion` |
| D06 | `CLOSED_BY_F2E_PREPARATION` | Fence futuro es fail-closed y prohíbe doble autoridad | Handoff/F2D.1 + inventario de writers |
| D07 | `CLOSED_BY_F2E_PREPARATION` | Hardening inverso es requerido para writers identificados | Fail-closed actual y writers maestros sin impacto |
| D08 | `BLOCKING_FOR_NEXT_GATE` | Elegir y aprobar cohorte conexa salón-instructor o protocolo cross-model; hasta entonces el fence no puede materializarse | Invariante cross-salon + locks asimétricos |
| D09 | `BLOCKING_FOR_NEXT_GATE` | Definir con evidencia la semántica de `vigenteDesde` para TI sin vigencia; mientras esté abierta sólo se permite detección read-only, no reconstrucción temporal | V15/V41 |
| D10 | `BLOCKING_FOR_NEXT_GATE` | Cerrar la intención de EXCEPCION/CANCELACION legacy no targeteada; mientras esté abierta sólo se clasifica `UNSUPPORTED/AMBIGUOUS` sin mapping productivo | Semántica física legacy vs APF; no afecta la `CANCELACION` APF con target nominal obligatorio |
| D11 | `BLOCKING_FOR_NEXT_GATE` | La composición se divide en auditor read-only, generador de candidatos/evidencia, selección/crosswalk, resolver, métricas y fence; sus contratos físicos exactos deben cerrarse y auditarse en una unidad de diseño previa, nunca decidirlos el executor | Dependencias D03/D04/D08–D10 y separación de autoridad |
| D12 | `DEFERRED_TO_IMPLEMENTATION` | Extraer/endurecer writers maestros y completar lifecycle BP/ASG | Inventario de services |
| D13 | `DEFERRED_TO_MIGRATION` | Batch size, snapshot concreto y disposición por anomalía real | Sin fuente material |
| D14 | `DEFERRED_TO_MIGRATION` | Backfill concreto de reservas y series | Requiere datos y crosswalk ejecutado |
| D15 | `DEFERRED_TO_CUTOVER` | Fecha/ventana/cohorte de activación y rollback operativo | Requiere gates previos PASS |
| D16 | `DEFERRED_TO_CUTOVER` | Retiro de TI y contratos web legacy | Requiere cero consumers verificado |
| D17 | `HUMAN_DECISION_REQUIRED` | Política para reservas pasadas/canceladas e historia a conservar | No está cerrada por canónicos |
| D18 | `HUMAN_DECISION_REQUIRED` | Política de negocio para legacy ambiguo no normalizable | No se puede inferir sin cambiar intención |

Las decisiones `HUMAN_DECISION_REQUIRED` no impiden completar este diseño; deben resolverse antes
del gate futuro al que afectan.

## 24. Blockers clasificados

Invariante de clasificación: una `CANCELACION` APF válida con target nominal único y
`EXPECTED_ABSENCE` no es blocker. Una `CANCELACION` o un `REEMPLAZO` APF sin la nominal única que
exige la implementación es `TARGET_REQUIRED_BUT_MISSING / BLOCKER`; múltiples nominales o targets
activos son invariant violation/ambigüedad y también bloquean.

| ID | Clase | Descripción / evidencia | Fase actual | Gate afectado | ¿Bloquea preparación? | ¿Sólo futuro? | ¿Humano? | Resolution path |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| B01 | `ENVIRONMENT` | No hay fuente/snapshot autorizado read-only | Diseño completo | Material data audit | No | Sí | Sí, autorización | Nombrar fuente, permisos y snapshot |
| B02 | `DATA` | Anomalías y conteos reales desconocidos | Diseño completo | Migration/cutover | No | Sí | Según hallazgo | Ejecutar contrato de auditoría |
| B03 | `ARCHITECTURAL` | No existe contrato cerrado/materializado de crosswalk por átomos ni target de Reserva; representa D03/D04 | Diseño corregido; decisión abierta | Handoff material de crosswalk/resolver, migration y cutover | No | No: bloquea el próximo scope material afectado | No para corregir; decisión futura según evidencia | Cerrar D03/D04, auditar el diseño y sólo después autorizar materialización |
| B04 | `ARCHITECTURAL` | Reserva no tiene identidad de target persistida; D04 debe elegir occurrence, ventana, composición u otra identidad explícita | Diseño corregido; decisión abierta | Crosswalk de reservas, migration y cutover | No | No: bloquea el próximo scope material afectado | No para corregir; futura según evidencia | Cerrar D04 y materializar mapping inequívoco bajo otro gate |
| B05 | `BUSINESS_POLICY` | TI no tiene `vigenteDesde`; representa D09 | Diseño corregido; decisión abierta | Resolver histórico, migration y cutover | No | No: limita el próximo resolver a detección | No para corregir; futura si evidencia no basta | Cerrar D09 o clasificar `UNSUPPORTED` fail-closed |
| B06 | `BUSINESS_POLICY` | EXCEPCION/CANCELACION legacy no targetean serie; representa D10 y no convierte una cancelación APF válida en blocker | Diseño corregido; decisión abierta | Crosswalk/resolver, migration y cutover | No | No: limita el próximo resolver a detección | No para corregir; futura si evidencia no basta | Cerrar D10 o clasificar `UNSUPPORTED/AMBIGUOUS` fail-closed |
| B07 | `ARCHITECTURAL` | Fence por salón no cubre solo el conflicto global de instructor; representa D08 | Diseño corregido; decisión abierta | Fence material, entry `MIGRANDO`, migration y cutover | No | No: bloquea el próximo scope material de fence | No para corregir | Cohorte conexa o protocolo cross-model auditado |
| B08 | `TECHNICAL` | Fence y enforcement de writers/readers no existen; D08 impide definir aún su unidad atómica | Diseño corregido; materialización prohibida | Handoff material de fence, migration y cutover | No | No: bloquea el próximo scope material de fence | No para corregir; D08 debe resolverse por diseño/evidencia | Cerrar D08/D11, audit fresh e intervención técnica separada |
| B09 | `TECHNICAL` | Hardening inverso de maestros/reservas/legacy no existe | Diseño completo | Cutover | No | Sí | Puede requerir política | Implementar scope aprobado |
| B10 | `ARCHITECTURAL` | BP no productivo ya veta writer de horario por adapter inverso | Diseño completo | Fence/authority gate | No | Sí | Sí para semántica por estado | Hacer adapter fence-aware o aislarlo |
| B11 | `TECHNICAL` | Web sigue TI; mobile estático; web reservas placeholder | Diseño completo | Consumer cutover | No | Sí | Producto para rollout | Contratos y migración de consumers |
| B12 | `AUTHORITY` | Implementación, migración y cutover no autorizados | Diseño completo | Cualquier fase material | No | Sí | Sí, nuevo handoff | Nueva autoridad + scope + gate |

Detalle contractual de B03 y B08:

| Blocker | Decisión representada | ¿Bloquea diseño/corrección F2E.1? | ¿Bloquea implementación read-only? | ¿Bloquea fence? | ¿Bloquea resolver? | ¿Bloquea migration? | ¿Bloquea cutover? | Momento de cierre |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| B03 | D03: átomos/cardinalidades/crosswalk; D04: target de Reserva | No; debe quedar explícitamente abierto | No para auditor/generador de candidatos detector-only; sí para selección/persistencia de mappings | No de forma directa | Sí para resolver de identidad/equivalencia; no para detector que sólo emite candidates y falla cerrado | Sí | Sí | D03/D04 durante la siguiente unidad de diseño/evidencia; auditadas antes de cualquier handoff material de crosswalk/resolver y antes de migration/cutover |
| B08 | Ausencia de fence/enforcement y dependencia de D08/D11 | No | No para auditor/detector aislado; sí para cualquier componente que haga enforcement o asuma authority state | Sí; `FENCE MATERIALIZATION: NOT_AUTHORIZED` mientras D08 esté abierta | No para shadow resolver read-only; sí si el resolver decide authority o transición | Sí | Sí | D08/D11 durante la unidad de diseño; fence sólo en intervención separada después de gate fresh y antes de migration/cutover |

Momento de cierre de blockers/gates:

| Momento | Requisito |
| --- | --- |
| Antes del próximo handoff | Re-audit fresh de F2E.1.2 con P0=0/P1=0 y nueva autoridad que limite el siguiente trabajo a diseño/evidencia; no se exige fingir cerradas D03/D04/D08–D11 para autorizar esa unidad no material. |
| Durante la siguiente unidad candidata | Cerrar D03, D04 y D08–D11 con evidencia; si alguna sigue abierta, delimitar sólo detección read-only `UNSUPPORTED/AMBIGUOUS`, sin selección, resolver completo ni fence. |
| Antes de cualquier handoff de implementación | Audit fresh del cierre arquitectónico y composición física exacta de D11; el executor recibe decisiones, no las toma. |
| Antes de migration | Fuente autorizada, B02–B10 aplicables cerrados, mapping/resolver materializados y auditados, idempotencia estable, fence/hardening y autorización expresa. |
| Antes de cutover | Todos los blockers de migration más consumers/reservas, gate de activación, cohorte/ventana y autoridad explícita; cero ambigüedad bloqueante. |

No existe blocker que impida materializar esta corrección del diseño F2E. Sí existen blockers duros
para componentes del próximo scope material, migration y cutover; ya no se clasifican genéricamente
como “sólo futuros”.

## 25. Human decisions

```text
Human decision required para completar F2E/preparación: NO
Human decisions diferidas para fases futuras: SI
```

Pendientes humanos futuros:

- fuente read-only y custodio;
- `vigenteDesde` y horizonte de migración de TI;
- disposición de EXCEPCION/CANCELACION legacy ambiguas;
- alcance histórico de reservas canceladas/pasadas;
- unidad atómica de cutover frente a instructores cross-salon;
- política de operación/UX durante `MIGRANDO`;
- fecha, cohorte y autorización de cutover.

## 26. Candidate next scope

### 26.1 Estado tras el audit final

```text
F2E.1 DESIGN/PREPARATION: APPROVED / CLOSED
F2E.2: NOT_AUTHORIZED
NEXT ACTION: READ-NEXT / DETERMINE-NEXT-HANDOFF-SCOPE
```

El re-audit final fue fresh, independiente y read-only, y contrastó el checkpoint con Git,
canónicos, código, migraciones y consumers. Su resultado se preserva en
`auditoria/reviews/F2E.1-REVIEW-DISENO-PREPARACION.md`. El candidato posterior no queda
autorizado: requiere determinación read-only desde los canónicos, un nuevo handoff/intervención,
pre-flight fresh y gate explícito.

### 26.2 Unidad candidata posterior a un PASS

No se declara ni se autoriza `F2E.2`. La secuencia siguiente sólo define `CANDIDATE NEXT UNIT(S)`;
cada unidad requiere handoff propio, pre-flight fresh y gate independiente.

**Candidate Unit A — cierre de decisiones y arquitectura, DESIGN/READ-ONLY**

Prerequisites:

- re-audit F2E.1.2 con P0=0/P1=0;
- nuevo handoff limitado a diseño/evidencia;
- código, datos y sistemas productivos en read-only.

Allowed scope:

- cerrar D03/D04 con el átomo target de Reserva, cardinalidades y reglas de selección;
- cerrar D08 con cohorte conexa o protocolo cross-model;
- cerrar D09/D10 mediante evidencia; si la evidencia no alcanza, definir exactamente la detección
  `UNSUPPORTED/AMBIGUOUS` que falla cerrado;
- cerrar D11: interfaces, schemas de resultado/evidencia, ownership, transacciones permitidas,
  dependencias y límites físicos entre auditor, candidate generator, selección/crosswalk, resolver,
  métricas y fence;
- diseñar queries/read models contra una fuente sólo si está identificada y autorizada read-only.

Forbidden scope:

- código, SQL/Flyway, crosswalk persistido, selección material, resolver implementado, enforcement,
  fence, migración, `MIGRANDO`, `NUEVA`, consumer switching o cutover.

Required gate: `FRESH_INDEPENDENT_DESIGN_DOCUMENT_AUDIT` sobre las decisiones cerradas. Si D03,
D04, D08 o D11 siguen abiertas, no existe candidate material unit para su componente.

**Candidate Unit B — subconjunto read-only ya determinado, posterior y condicional**

Prerequisites:

- Unit A documentada y auditada con P0=0/P1=0;
- D11 cerrada para cada componente incluido;
- nuevo handoff material con allowlist exacta;
- fuente nombrada si la unidad ejecuta auditoría material.

Allowed scope máximo:

- auditor reproducible estrictamente read-only;
- generador de candidatos y evidencia que conserve `0..N` candidates;
- métricas/artefactos inmutables sin PII innecesaria;
- shadow comparison no autoritativa sólo para las semánticas cerradas.

Si D09/D10 no quedaron cerradas, esos casos sólo pueden detectarse, clasificarse
`UNSUPPORTED/AMBIGUOUS` y bloquearse: quedan excluidos mapping seleccionado y output productivo. Si
D03/D04 no están cerradas, se excluyen selección/crosswalk de reservas y resolver de identidad de
reservas. El fence y su enforcement están siempre fuera de Unit B.

Required gate: auditoría técnica y documental fresh del subconjunto materializado. Esta unidad no
autoriza datos mutantes, migration, readers/writers productivos ni authority change.

**Candidate Unit C — fence/enforcement, posterior y separado**

Sólo puede proponerse después de D08 y D11 cerradas y auditadas, con contrato físico ya decidido,
handoff técnico separado y evidencia de interacción cross-salon. Su máximo estado inicial sería
`LEGACY`; no autoriza entrar a `MIGRANDO`. Resolver/crosswalk material, migration y cutover conservan
sus propios prerequisites y gates.

Esta secuencia no entrega decisiones arquitectónicas a un executor, no salta D08–D10 y no autoriza
automáticamente ninguna fase.

## 27. No-autorizaciones explícitas

```text
Implementation authorized by F2E/preparación: NO
F2E.2 authorized: NO
Candidate Unit A/B/C authorized automatically: NO
Material data audit authorized without named source: NO
Migration/normalization execution authorized: NO
Flyway authorized: NO
Resolver implementation authorized: NO
Fence persistence authorized: NO
MIGRANDO authorized: NO
NUEVA authorized: NO
Productive writer/reader/consumer switch authorized: NO
Cutover authorized: NO
Product authority change authorized: NO
TurnoInstructor retirement authorized: NO
```

## 28. Exit conditions de este checkpoint

Para que el diseño pueda presentarse al gate independiente debe verificarse:

- este archivo es el único delta;
- HEAD y staging permanecen invariantes;
- código, tests, migraciones, datos, frontend y mobile no cambiaron;
- no se inventaron resultados de datos;
- componentes futuros están marcados no materializados;
- inventario, contratos, decisiones, blockers y next scope son autocontenidos;
- `DARK_LAUNCH`, `NOT_PRODUCTIVE`, `cutover=false` y TI authority permanecen.

El re-audit fresh e independiente final reportó `P0=0`, `P1=0` y `P2=0`; por tanto el diseño de
F2E.1 queda aprobado y cerrado. Este cierre no emite autorización de implementación, migración,
F2E.2, cutover ni cambio de autoridad. El lifecycle previo permanece como historia:

```text
F2E.1.2 CORRECTION MATERIALIZED
READY FOR FRESH RE-AUDIT
DESIGN GATE: LAST EVALUATED FAIL / RE-AUDIT PENDING
```

## 29. Cierre canónico de F2E.1

El resultado final independiente quedó persistido en
`auditoria/reviews/F2E.1-REVIEW-DISENO-PREPARACION.md`:

```text
Role: DESIGN_AUDITOR / DOCUMENT_AUDITOR
Mode: READ_ONLY
Fresh independent: SI
Branch auditada: operacion/excepciones-horario-fecha
HEAD auditado: 4a6cd5eb571de00274a3b82f7c661a1dfd34fa2d
P0=0 / P1=0 / P2=0
F2E1_DESIGN_DOCUMENT_GATE=PASS
READY_FOR_F2E1_CLOSURE=SI
Requires human decision=NO
F2E.1: DESIGN_APPROVED / CLOSED
```

El cierre significa exclusivamente que el diseño/preparación autorizado por el handoff fue
completado y auditado. Conserva `DATA_SOURCE_NOT_AVAILABLE`; el diseño de auditoría de datos está
aprobado, mientras que su ejecución permanece `NOT_PERFORMED`. No cierra D03, D04, D08, D09, D10
ni D11, ni modifica sus clasificaciones finales. `D03/D04` siguen `BLOCKING_FOR_NEXT_GATE` y
`D08-D11` preservan las clasificaciones de las secciones 23 y 24. El siguiente scope es sólo
candidato y exige nuevo handoff y gate.

```text
Implementation authorized: NO
Migration authorized: NO
F2E.2 authorized: NO
Cutover: false
Product authority change: NO
Runtime: DARK_LAUNCH
Productive: NOT_PRODUCTIVE
Authority: TurnoInstructor / LEGACY_VIVO / PRODUCTIVO
```
