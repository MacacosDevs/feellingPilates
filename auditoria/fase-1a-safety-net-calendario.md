# FeelingPilates — Fase 1A: safety net de calendario

Fecha: 2026-08-22

Branch base: `seguridad/redaccion-secretos`

Commit base: `c36fc2f7a0893c3c94a766280f8425764527f6ad`

Branch de trabajo: `programacion/safety-net-calendario`

## Pre-flight

- La branch base y el commit coincidieron con los valores esperados.
- El working tree estaba limpio.
- `seguridad/redaccion-secretos` seguía a `origin/seguridad/redaccion-secretos` en el mismo commit; la base estaba respaldada en origin.
- `programacion/safety-net-calendario` no existía y se creó desde el commit base.
- No se tocó `main`.

## Alcance inspeccionado

La inspección se limitó a `calendario`, los horarios y excepciones de
`ubicaciones`, `TipoActividad`, y las relaciones de roles/especialidades de
`Usuario` necesarias para programar. Se usaron como fuentes autoritativas
`auditoria/00-revalidacion-repositorio-completo.md` y
`auditoria/04-arquitectura-objetivo.md`, además de los checkpoints recientes de
estabilización.

No se introdujo la arquitectura objetivo, no se renombraron packages y no se
crearon modelos de Programación, Sesiones, capacidad, recursos o Beneficios.

## A. Comportamiento válido a preservar

- Un turno recurrente debe quedar contenido en el horario semanal del salón.
- Apertura y cierre son bordes válidos: el turno puede iniciar exactamente en
  apertura y finalizar exactamente en cierre.
- El horario especial activo de una fecha sustituye al semanal para validar un
  turno puntual; puede ampliar o reducir el intervalo.
- Un cierre excepcional del salón rechaza turnos puntuales en esa fecha.
- El traslape de bloques usa semántica `[inicio, fin)`: los intervalos contiguos
  son válidos y cualquier intersección positiva se rechaza, incluidos los casos
  de contención total.
- Un bloque admite varios instructores y ambos pueden impartir la misma
  actividad. La actividad no hace exclusivo al salón.
- La asignación actual conserva instructor, actividad, rango opcional y
  pertenencia al turno; su rango debe quedar dentro del turno y la actividad
  debe ser especialidad del instructor.
- La reserva actual selecciona turnos por instructor, salón y fecha, calcula el
  fin con `TipoActividad.duracionMinutos`, exige que el rango calculado quepa en
  un turno vigente y guarda una copia de salón, instructor, cliente, actividad,
  fecha y horas.
- Los puntuales se consultan por fecha exacta; sin puntuales aplica el recurrente
  del día de la semana.

## B. Limitaciones actuales que serán reemplazadas

- `HorarioOperacion` sólo admite un intervalo por salón/día y no tiene vigencia
  o historia.
- `TurnoInstructor` mezcla bloque, recurrencia, instructores, excepción y
  cancelación; los recurrentes tampoco tienen vigencia.
- `AsignacionInstructorRequest` agrupa varias actividades bajo un único rango,
  mientras que el modelo objetivo requiere exactamente una actividad por
  asignación.
- La PK `(turno, instructor, actividad)` de
  `TurnoInstructorAsignacion` impide repetir una actividad en segmentos
  disjuntos del mismo turno. Además, el mapa del servicio sólo conserva un rango
  por instructor. No se añadió un test rojo ni se corrigió el modelo.
- No se valida el traslape global de un instructor entre salones al programar.
- La precedencia vigente de reservas es
  `CANCELACION > EXCEPCION > RECURRENTE`. Cualquier `EXCEPCION` del instructor,
  salón y fecha sustituye todos sus recurrentes de ese día; cualquier
  `CANCELACION` elimina todo el día, sin distinguir bloque, rango o actividad.
  Los tests que lo observan están nombrados como caracterización de la
  limitación actual, no como regla objetivo.
- La reserva bloquea cualquier segunda reserva traslapada del instructor. Esto
  impide varios clientes en una clase compartida porque no existe `Sesion` ni
  capacidad.
- No existen sesión persistente, confirmación del instructor, ventanas de
  reserva, capacidad, consumo de recursos ni consumo de beneficios.

## C. Bugs actuales encontrados y no congelados

- Al crear una `EXCEPCION`, `TurnoInstructorService` la compara contra los
  recurrentes del salón. Una excepción que debería sustituir un bloque normal
  puede ser rechazada por traslaparse con ese mismo horario recurrente.
- `ReservaService` valida especialidad, pero no exige que la actividad esté
  asignada al instructor en el turno ni respeta el rango de
  `TurnoInstructorAsignacion`.
- `ReservaService` no revalida `SalonHorarioExcepcion`; una reserva sobre un
  recurrente puede crearse aunque el salón esté cerrado o el horario especial
  ya no contenga el rango.
- Repetir el mismo instructor en varias entradas del request no genera segmentos:
  `resolverAsignaciones` sobrescribe silenciosamente la entrada anterior en su
  mapa por instructor.
- El no traslape de bloques es `check-then-insert` en Java y no tiene protección
  transaccional o constraint de exclusión frente a carreras concurrentes.

Estos casos no tienen tests que expresen que el resultado defectuoso sea el
contrato deseado y no se corrigieron en esta fase.

## Tests añadidos

### `TurnoInstructorServiceCaracterizacionTest` — 16 tests

- Horario semanal: dentro, fuera, apertura exacta y cierre exacto.
- Horario especial: ampliación, reducción y salón cerrado.
- Traslapes: contiguo, intersección de un minuto, contenido, contenedor y
  separado.
- Múltiples instructores con la misma actividad en un bloque.
- Asignación: instructor/actividad/rango/pertenencia, rango fuera del turno y
  especialidad faltante.

### `ReservaServiceCaracterizacionTest` — 8 tests

- Consumo del recurrente y copia de los datos actuales de la reserva.
- Duración de actividad y contención del rango calculado.
- Rechazo fuera de turno y por especialidad faltante.
- Precedencia y granularidad real de `EXCEPCION` y `CANCELACION`.
- Consulta de puntuales por fecha exacta.

### Infraestructura exclusiva de test

- `src/test/resources/mockito-extensions/org.mockito.plugins.MockMaker`
  selecciona el mock maker basado en subclases para que los tests unitarios no
  dependan del auto-attach de agentes de la JVM.
- Los servicios reales ejecutan las reglas; los repositorios se simulan sólo
  como fronteras para entregar estados concretos y observar coordinación. La
  suite existente mantiene el arranque contra PostgreSQL 16 con Testcontainers
  y la validación completa de Flyway.

## Comportamiento que no debe congelarse

- Una excepción que sustituye indiscriminadamente todos los recurrentes.
- Una cancelación de turno interpretada como `SesionCancelada`.
- La ocupación exclusiva del instructor por el primer cliente.
- Reservar una actividad no asignada o fuera del rango de asignación.
- Reservar sobre un salón cerrado o fuera de su horario efectivo.
- Programar al mismo instructor simultáneamente en dos salones.
- Agrupar varias actividades en una asignación o impedir segmentos repetidos de
  la misma actividad.

## Gap matrix

| Regla futura | Estado actual | Test | Clasificación |
|---|---|---|---|
| Horario semanal por salón | SOPORTADO, un intervalo por día | `permiteBloqueDentroDelHorarioOperativo` y bordes | PRESERVAR / EVOLUCIONAR VIGENCIA |
| Turno contenido en horario | SOPORTADO | `permiteBloqueDentroDelHorarioOperativo`, `rechazaBloqueFueraDelHorarioOperativo` | PRESERVAR |
| Horario especial amplía o reduce | SOPORTADO al crear turno puntual | tests `horarioEspecial*` | PRESERVAR |
| Salón cerrado por fecha | SOPORTADO al crear turno puntual | `salonCerradoPorExcepcionRechazaElBloqueDeEsaFecha` | PRESERVAR |
| Intervalos `[inicio, fin)` | SOPORTADO | cinco tests de traslape | PRESERVAR |
| Bloques no traslapados por salón | SOPORTADO en Java, sin protección concurrente | tests de traslape | PRESERVAR / ENDURECER POSTERIORMENTE |
| Varios instructores en el mismo bloque | SOPORTADO | `permiteVariosInstructoresConLaMismaActividadEnElMismoBloque` | PRESERVAR |
| Misma actividad para varios instructores | SOPORTADO | mismo test | PRESERVAR |
| Asignación instructor+rango+actividad | SOPORTADO PARCIALMENTE | `conservaInstructorActividadRangoYPertenenciaAlTurnoEnLaAsignacion` | PRESERVAR BASE / EVOLUCIONAR IDENTIDAD |
| Exactamente una actividad por asignación | NO SOPORTADO por el DTO | — | EVOLUCIONAR |
| Misma actividad en dos segmentos | NO SOPORTADO por PK y mapa por instructor | — | EVOLUCIONAR |
| Especialidad del instructor | SOPORTADO al programar y reservar | tests `rechaza*Especialidad*` | PRESERVAR |
| Instructor global sin overlap | NO SOPORTADO al programar | — | IMPLEMENTAR POSTERIORMENTE |
| Excepción composable y dirigida | NO SOPORTADO; sustitución global por instructor/día | tests de precedencia actual | EVOLUCIONAR |
| Cancelación por bloque/asignación | NO SOPORTADO; cancelación de día completo | `cancelacionDeFechaTienePrecedenciaSobreExcepcionYRecurrente` | EVOLUCIONAR |
| Reserva respeta actividad/rango asignado | NO SOPORTADO | — | BUG A CORREGIR POSTERIORMENTE |
| Reserva revalida horario efectivo | NO SOPORTADO | — | BUG A CORREGIR POSTERIORMENTE |
| Varias reservas por sesión compartida | NO SOPORTADO; overlap por instructor bloquea al segundo cliente | — | EVOLUCIONAR |
| Sesión persistente | INEXISTENTE | — | NUEVO MODELO |
| Confirmación/ventanas/capacidad/recursos/beneficios | INEXISTENTE | — | IMPLEMENTAR POSTERIORMENTE |

## Validación

Tests antes: **26**.

Tests específicos nuevos:

- Comando: `./mvnw -Dtest=TurnoInstructorServiceCaracterizacionTest,ReservaServiceCaracterizacionTest test`
- Resultado: **24/24 PASS**, 0 failures, 0 errors, 0 skipped.

Suite completa:

- Comando: `./mvnw test`.
- Resultado: **50/50 PASS**, 0 failures, 0 errors, 0 skipped.
- PostgreSQL 16.14 mediante Testcontainers.
- Flyway validó y aplicó las 43 migraciones hasta V40.

Build:

- Comando: `./mvnw clean compile`.
- Resultado: **BUILD SUCCESS**; 152 fuentes productivas compiladas.

## Resultado final

TESTS ANTES:

26

TESTS DESPUÉS:

50

HORARIO OPERATIVO:

CARACTERIZADO

TRASLAPES:

CARACTERIZADOS

MÚLTIPLES INSTRUCTORES:

CARACTERIZADO

ASIGNACIONES:

CARACTERIZADAS

EXCEPCIONES:

CARACTERIZADAS

RESERVA ACTUAL:

CARACTERIZADA

PRODUCTIVO:

SIN CAMBIOS

FLYWAY:

SIN CAMBIOS

Branch:

`programacion/safety-net-calendario`

Commit:

el commit `test: caracterizar programacion actual` que contiene este checkpoint,
reportado por hash al finalizar la fase
