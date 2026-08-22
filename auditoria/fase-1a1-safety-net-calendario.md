# FeelingPilates — Fase 1A.1: cerrar gaps críticos del safety net

Fecha: 2026-08-22

Branch base: `programacion/safety-net-calendario`

Commit base: `755d958bffbc8953e30ebfcd0f0f25971172f525`

Branch de trabajo: `programacion/safety-net-calendario-1a1`

## Pre-flight

- Branch y commit coincidieron con lo esperado.
- Working tree limpio.
- `programacion/safety-net-calendario` seguía a `origin/programacion/safety-net-calendario` en el mismo commit (respaldada en origin).
- `programacion/safety-net-calendario-1a1` no existía y se creó desde el commit base.
- No se tocó `main`.

## Contexto: por qué esta fase

La Fase 1A (ver `auditoria/fase-1a-safety-net-calendario.md`) fue revisada
independientemente. El veredicto fue **APROBADO CON AJUSTES**: el safety net
protegía comportamiento real, pero tenía huecos donde un refactor incorrecto
podía pasar con tests en verde. Esta fase (1A.1) sigue siendo **TEST-ONLY**:
no se implementó `BloqueProgramacion`, `Sesion`, `ProgramacionEfectiva`,
`ConfirmacionInstructor`, capacidad, recursos compartidos ni beneficios.

## Gaps recibidos de la revisión y cómo se cerraron

| # | Gap señalado | Cómo se cerró |
|---|---|---|
| P0 | Exclusividad actual de reserva nunca se ejercitaba (`existeTraslape` siempre stubbeado en `false`) | `caracterizaLimitacionActualInstructorExclusivoPorReserva` |
| P0 | Granularidad real de CANCELACION no probada (el test viejo usaba 00:00-23:59, ya "todo el día" por construcción) | `caracterizaLimitacionActualCancelacionDeUnRangoPuntualAnulaTodoElDia` |
| P0 | `TurnoInstructorService.crear` con tipo CANCELACION sin cobertura directa de que salta horario/traslape | `permiteCancelacionFueraDelHorarioOperativoDelSalonPorqueNoValidaHorario`, `permiteCancelacionQueSeTraslapaConUnRecurrenteExistentePorqueNoValidaTraslape` |
| P0 | Múltiples instructores con actividades distintas en el mismo bloque, sin test | `permiteInstructoresConActividadesDistintasEnElMismoBloque` |
| P0 | Múltiples instructores con rangos parciales distintos dentro del mismo bloque, sin test | `permiteRangosParcialesDistintosPorInstructorDentroDelMismoBloque` |
| P0 | Intervalo inválido (`horaFin == horaInicio`, `horaFin < horaInicio`) sin test | `rechazaIntervaloConHoraFinIgualAHoraInicio`, `rechazaIntervaloConHoraFinAnteriorAHoraInicio` |
| P0 | Rango exactamente idéntico a uno existente sin test explícito | `rechazaRangoExactamenteIdenticoAUnoExistente` |
| P0 | EXCEPCION vs EXCEPCION traslapadas (misma fecha) sin test | `rechazaDosExcepcionesDeLaMismaFechaQueSeTraslapan` |
| P0 | EXCEPCION vs RECURRENTE (bug de sustitución) sin test caracterizado explícitamente | `caracterizaLimitacionActualExcepcionQuePretendeSustituirElRecurrenteEsRechazadaPorTraslape` |
| P1 | Filtro por día de semana (`h.getDiaSemana() == diaSemana`) sin test | `rechazaTurnoParaUnDiaSinHorarioOperativoConfiguradoAunqueOtroDiaSiLoTenga`, `rechazaTurnoCuandoElSalonNoTieneNingunHorarioOperativoConfigurado` |
| P1 | `actualizarTurno` / autoexclusión (`excluirTurnoId`) sin cobertura | `actualizarTurnoConservandoSuPropioRangoNoSeConsideraTraslapadoConsigoMismo`, `actualizarTurnoRechazaTraslapeContraOtroTurnoRealDistinto` |
| P1 | Convención "domingo = 0" sin test directo | `diaSemanaIsoMapeaDomingoAlValorCero` (ReservaService), `excepcionEnDomingoUsaDiaSemanaCeroParaValidarHorarioSemanal` (TurnoInstructorService) |
| P1 | Matriz de permisos por tipo (RECURRENTE/EXCEPCION/CANCELACION) sin protección | `solicitaPermisoDeGestionarParaTurnoRecurrente`, `solicitaPermisoDeGestionarOEditarParaExcepcion`, `solicitaPermisoDeGestionarOCancelarParaCancelacion` |
| Renombre | `cancelacionDeFechaTienePrecedenciaSobreExcepcionYRecurrente` no dejaba explícito que es limitación actual | Renombrado a `caracterizaLimitacionActualCancelacionDeFechaTienePrecedenciaSobreExcepcionYRecurrente` |
| Renombre | `excepcionDeFechaTienePrecedenciaSobreElRecurrente` sonaba a regla deseada | Renombrado a `caracterizaLimitacionActualExcepcionTienePrecedenciaSobreElRecurrente` |

No se tocó la lógica de estos dos tests renombrados, sólo el nombre (y un
comentario explicando la corrección de interpretación, ver sección
"Corrección de interpretación" más abajo).

## Qué se protege (regla válida, comportamiento a preservar)

- El instructor global no se toca aquí, pero dentro de un mismo bloque: dos
  instructores pueden dar actividades distintas y/o cubrir rangos parciales
  distintos del mismo turno. La actividad y el rango pertenecen a la
  asignación del instructor, no al bloque.
- Intervalo inválido (`horaFin <= horaInicio`) se rechaza siempre, tanto para
  `crear` (turnos nuevos) como ya estaba cubierto en `actualizarTurno` por la
  misma condición de código (no se agregó test específico ahí porque la regla
  y el mensaje son idénticos a `crear`; ver "Limitación de alcance" abajo).
- Un rango exactamente idéntico a uno existente se rechaza por traslape.
- Dos EXCEPCION de la misma fecha que se traslapan se rechazan: coincide con
  la invariante física del salón (un espacio, un bloque a la vez).
- El filtro por día de la semana (`HorarioOperacion.diaSemana == diaSemana`)
  es real: un salón con horario sólo para lunes rechaza un turno para martes,
  y un salón sin ningún `HorarioOperacion` rechaza cualquier turno.
- `actualizarTurno` no se autotraslapa (excluye su propio id), pero sigue
  detectando conflicto real contra otro turno distinto.
- La convención `domingo = 0` (no `7`) se usa consistentemente en
  `ReservaService.diaSemanaIso` y `TurnoInstructorService.diaSemanaIso`.
- La matriz de permisos por tipo de turno (`RECURRENTE` →
  `calendario.gestionar`; `EXCEPCION` → `calendario.gestionar` +
  `calendario.editar`; `CANCELACION` → `calendario.gestionar` +
  `calendario.cancelar`) queda protegida contra cambios accidentales.

## Qué sigue siendo limitación actual (NO corregida, caracterizada explícitamente)

- **Exclusividad de reserva por instructor**: una segunda reserva traslapada
  del mismo instructor se rechaza sin importar el cliente
  (`caracterizaLimitacionActualInstructorExclusivoPorReserva`). El modelo
  futuro de sesiones grupales eliminará esta exclusividad.
- **Granularidad de CANCELACION**: cualquier `CANCELACION` para una fecha —
  aunque cubra sólo un rango puntual como 14:00-15:00 — anula TODOS los
  turnos vigentes de ese instructor/salón ese día, incluso para horas fuera
  del rango cancelado (`caracterizaLimitacionActualCancelacionDeUnRangoPuntualAnulaTodoElDia`).
  El modelo futuro de cancelación por bloque/asignación debe operar sobre el
  rango real.
- **CANCELACION no valida horario ni traslape al crearse**: es un marcador de
  "no atiende" de 00:00-23:59 (por convención del propio código, ver
  comentario en `TurnoInstructorService.crear`), no un rango real de horas.
  Confirmado con `permiteCancelacionFueraDelHorarioOperativoDelSalonPorqueNoValidaHorario`
  y `permiteCancelacionQueSeTraslapaConUnRecurrenteExistentePorqueNoValidaTraslape`.
- **EXCEPCION sustituye TODOS los recurrentes del instructor ese día** (no
  sólo el bloque que se pretendía reemplazar):
  `caracterizaLimitacionActualExcepcionTienePrecedenciaSobreElRecurrente`.
- **BUG conocido, no corregido**: al crear una EXCEPCION,
  `validarSinTraslape` la compara también contra los RECURRENTE de ese día de
  la semana. Si la EXCEPCION pretende sustituir/ocupar el mismo horario que un
  recurrente vigente, HOY es rechazada por "traslape" con ese mismo
  recurrente, aunque la intención sea reemplazarlo para esa fecha puntual
  (`caracterizaLimitacionActualExcepcionQuePretendeSustituirElRecurrenteEsRechazadaPorTraslape`).
  Ya estaba documentado en la sección C de la Fase 1A; ahora tiene test que lo
  caracteriza explícitamente sin congelarlo como deseado.

## Corrección de interpretación (respecto al checkpoint de Fase 1A)

El checkpoint de Fase 1A afirmaba que
`cancelacionDeFechaTienePrecedenciaSobreExcepcionYRecurrente` caracterizaba la
"granularidad real" de CANCELACION. Al revisar el test, la CANCELACION usada
ahí cubre 00:00-23:59 — es decir, ya es "todo el día" por construcción del
propio caso de prueba, y no distingue "cancelar un rango" de "cancelar el
día completo". El test se mantiene (renombrado) porque sigue protegiendo la
precedencia `CANCELACION > EXCEPCION > RECURRENTE`, pero la granularidad real
—que un rango puntual de CANCELACION también anula el día completo— ahora la
caracteriza `caracterizaLimitacionActualCancelacionDeUnRangoPuntualAnulaTodoElDia`,
que usa una CANCELACION de sólo 14:00-15:00 y una reserva solicitada a las
09:00 (fuera del rango cancelado, y aun así rechazada).

## Limitación de alcance de esta fase (no un gap sin resolver, una decisión de foco)

- No se agregó test específico de intervalo inválido para `actualizarTurno`
  (la validación es la misma línea de código y el mismo mensaje que en
  `crear`; agregar un test ahí sería duplicar cobertura, no cerrar un hueco).
- No se congelaron los bugs P1 señalados por la revisión (instructor
  repetido se sobrescribe silenciosamente en `resolverAsignaciones`;
  `tipoActividadIds` vacío/null; rangos anómalos de asignación; mocks de
  resolución de ID permisivos). Quedan documentados aquí como GAP conocido,
  no como comportamiento a preservar, y se dejan para diseño/implementación
  futura del modelo nuevo.
- No se probó el traslape global de un instructor entre distintos salones
  (ya documentado como NO SOPORTADO en Fase 1A, sección B); sigue fuera de
  alcance de esta fase de safety net.
- No se tocaron `verify(..., never())` existentes salvo los ya presentes en
  los dos tests renombrados; no se identificaron en el diff original casos
  claramente frágiles adicionales que ameritaran retirar aserciones
  estructurales, así que no se removió ningún `verify` existente.

## Tests añadidos

### `TurnoInstructorServiceCaracterizacionTest` — 16 → 33 tests (+17)

- CANCELACION no valida horario ni traslape (2 tests).
- Múltiples instructores: actividades distintas y rangos parciales distintos
  en el mismo bloque (2 tests).
- Intervalos inválidos: fin == inicio, fin < inicio (2 tests).
- Rango exactamente idéntico a uno existente (1 test).
- EXCEPCION vs EXCEPCION traslapadas (1 test).
- EXCEPCION vs RECURRENTE — bug de sustitución (1 test).
- Filtro por día de la semana: día sin horario propio, salón sin ningún
  horario (2 tests).
- `actualizarTurno`: autoexclusión y conflicto real contra otro turno
  (2 tests).
- Domingo = 0 en `TurnoInstructorService` (1 test).
- Matriz de permisos por tipo: RECURRENTE, EXCEPCION, CANCELACION (3 tests).

### `ReservaServiceCaracterizacionTest` — 8 → 11 tests (+3)

- Exclusividad actual de reserva por instructor (`existeTraslape = true`).
- Granularidad real de CANCELACION (rango puntual anula todo el día).
- Domingo = 0 en `ReservaService`.

Dos tests existentes renombrados (sin cambiar lógica) para dejar explícito
que caracterizan limitación actual, no regla deseada (ver tabla arriba).

## Validación

Tests específicos nuevos/tocados:

- Comando: `./mvnw -Dtest=TurnoInstructorServiceCaracterizacionTest,ReservaServiceCaracterizacionTest test`
- Resultado: **44/44 PASS** (33 + 11), 0 failures, 0 errors, 0 skipped.

Suite completa:

- Comando: `./mvnw test`.
- Resultado: **70/70 PASS**, 0 failures, 0 errors, 0 skipped.
- Baseline previo (Fase 1A): 50/50 PASS. Incremento: +20 tests.

Build:

- Comando: `./mvnw clean compile`.
- Resultado: **BUILD SUCCESS**.

Productivo:

- Comando: `git diff -- src/main/java`.
- Resultado: **vacío**.

Flyway:

- Comando: `git diff -- src/main/resources/db/migration`.
- Resultado: **vacío**.

Diff:

- Único cambio: `src/test/java/com/feelingpilates/calendario/TurnoInstructorServiceCaracterizacionTest.java`,
  `src/test/java/com/feelingpilates/calendario/ReservaServiceCaracterizacionTest.java`, y este checkpoint.

## Resultado final

P0 EXCLUSIVIDAD RESERVA:

CUBIERTO

P0 CANCELACION GRANULARIDAD:

CUBIERTO

P0 CREAR CANCELACION:

CUBIERTO

P0 MULTIPLES INSTRUCTORES:

CUBIERTO

P0 INTERVALOS INVALIDOS:

CUBIERTO

P0 EXCEPCIONES:

CUBIERTO

DIA SEMANA:

CUBIERTO

ACTUALIZACION:

CUBIERTO

DOMINGO:

CUBIERTO

TESTS:

70/70 PASS

PRODUCTIVO:

SIN CAMBIOS

FLYWAY:

SIN CAMBIOS

Branch:

`programacion/safety-net-calendario-1a1`

Commit:

el commit `test: reforzar safety net de programacion` que contiene este
checkpoint, reportado por hash al finalizar la fase
