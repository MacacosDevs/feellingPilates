# F2C.2 — Implementación excepciones de horario por fecha

## Base
2d4ab56a8190f797b8d83b46d425033bfd9971cb

## Modelo
CONSERVADO. `SalonHorarioExcepcion` sobre `salon_horario_excepcion` (V18), sin renombrar. El
booleano `cerrado` sigue siendo la representación de los dos estados conceptuales. No se persiste
`diaSemana`.

## Migraciones
NINGUNA. V47 permanece libre. No se tocó `src/main/resources/db/migration/`.

## Writer excepción
`SalonHorarioExcepcionService` endurecido: pasa de escribir directo sin protocolo a seguir
exactamente el patrón F2B (`autorización → validación sintáctica/temporalidad → SalonLock →
lecturas dependientes → validación de impacto → persistencia`). Un único caso de uso interno
`upsert(...)` sirve tanto al `PUT` legacy como al `PUT /por-fecha/{fecha}`; un único `cancelar(...)`
sirve tanto al `DELETE` legacy como al `DELETE /por-fecha/{fecha}`.

## Clock
Inyectado (`java.time.Clock`), igual que en `VersionarHorarioOperacion`/`CerrarHorarioOperacion`.
`LocalDate.now(reloj)` en los dos puntos de temporalidad (alta/edición y cancelación). Cero usos de
`LocalDate.now()`/`LocalTime.now()` directos en el código de producción tocado.

## Temporalidad
`fecha < hoy` rechaza crear/modificar/cancelar con `EXCEPCION_HORARIO_EN_EL_PASADO` (400). `hoy` y
futuro, permitidos. "Hoy" es atómico: no se usa `LocalTime.now()` en ninguna validación; un objeto
puntual incompatible de la fecha completa bloquea aunque su hora ya haya pasado.

## Upsert
- Activa + mismo contenido → NO-OP real: ni `save` ni `saveAndFlush`; se devuelve la representación
  existente. Verificado con `verify(..., never())` en el test unitario.
- Activa + contenido distinto (hoy/futuro) → UPDATE de la MISMA fila (mismo `id`).
- Sin fila activa (exista o no historial inactivo) → INSERT de una fila NUEVA. Nunca se reactiva
  una fila vieja (`findBySalonIdAndFechaAndActivoTrue` solo ve activas).

## Cancelación
SOFT DELETE (`activo=false`), nunca `DELETE` físico. Cancelar sin fila activa → 404
(`EXCEPCION_HORARIO_NO_EXISTE`), no 204. Cancelar+recrear la misma fecha inserta una fila nueva
(el índice parcial lo permite).

**Crítico**: la validación de impacto al cancelar se evalúa contra el horario que **regiría
después** de retirar la excepción (semanal vigente vía `HorarioOperacionResolver`, o `CERRADO`
equivalente a `NO_OPERATIVO` si no hay versión semanal) — nunca contra la excepción que se está
retirando. Implementado en `SalonHorarioExcepcionService.cambioTrasCancelar`.

## API legacy
PRESERVADA íntegra: mismos paths (`GET`/`PUT`/`DELETE /{id}`), mismos DTOs
(`GuardarExcepcionSalonRequest`, `SalonHorarioExcepcionResponse`). El comportamiento por debajo
cambió (lock, temporalidad, impacto, códigos), el contrato de transporte no.

## API por fecha
Implementada bajo prefijo literal `por-fecha/`, que evita la colisión de mapeo entre
`DELETE /{id}` y `DELETE /{fecha}`:

```
PUT    /api/salones/{salonId}/excepciones-horario/por-fecha/{fecha}
DELETE /api/salones/{salonId}/excepciones-horario/por-fecha/{fecha}
```

Fecha del path autoritativa; nuevo DTO `GuardarExcepcionSalonPorFechaRequest` sin campo `fecha`.

## Legacy DELETE seguridad
CORREGIDO. Orden anterior: `findById` antes de autorizar (IDOR-style). Orden nuevo:
`AutorizadorSalon.verificarAccesoSalon` (sobre el salón contextual) → `SalonLock.adquirir` →
`findById` filtrado por `activo` y por pertenencia al salón contextual, con una única rama 404 que
no distingue "no existe" de "existe en otro salón" (sin revelar información cross-salon).

## SalonLock
`SalonLock.adquirir` reemplaza el `findById` redundante en los tres writers y sirve como única
comprobación de existencia.

- Excepción: SÍ (upsert y cancelación, los dos caminos legacy y por-fecha).
- `TurnoInstructorService.crear`: SÍ, extendido de `tipo == RECURRENTE` a
  `tipo == RECURRENTE || tipo == EXCEPCION`. `CANCELACION` sigue sin tomarlo (no valida horario).
- `ReservaService.crear`: SÍ, nuevo. Sustituye el `findById` de `Salon` que hacía antes.

## Port neutral
`ubicaciones/dominio`: `CambioExcepcionHorario` (análogo a `CambioHorarioOperacion`, estados
`CERRADO`/`HORARIO_ESPECIAL`, `admite(inicio, fin)` de contención completa), `ConflictoProgramacionPuntual`
(análogo a `ConflictoProgramacion`), `ValidadorImpactoExcepcionHorario.evaluar(cambio) →
List<ConflictoProgramacionPuntual>`. El paquete `ubicaciones` no importa `TurnoInstructor`,
`Reserva` ni sus repositorios.

## Adapter puntual
Uno solo, en `calendario`: `ImpactoPuntualEnExcepcionHorario`. Consulta
`TurnoInstructorRepository.buscarExcepcionesPorSalonYFecha` (ya filtra `activo=true AND
tipo='EXCEPCION'`; nunca consulta RECURRENTE ni CANCELACION) y
`ReservaRepository.findBySalonIdAndFechaAndEstado(..., CONFIRMADA)` (nunca CANCELADA). No decide
política, solo reporta.

## TurnoInstructor EXCEPCION
`TurnoInstructorService.crear` extiende la condición del lock a `RECURRENTE || EXCEPCION`. Javadoc
y comentario actualizados (ya no afirman que la invariante de EXCEPCION esté sin proteger).
`ImpactoTurnosRecurrentesEnHorario` (el otro port, del lado del horario semanal) actualizó su
Javadoc para reflejar que la invariante de EXCEPCION ahora la protege el protocolo de
`SalonHorarioExcepcionService`, no ese validador — su exclusión de la Política A del horario
semanal sigue siendo correcta (EXCEPCION no es programación recurrente).

## ReservaService
`crear` adquiere `SalonLock` tras autorizar, resuelve `HorarioEfectivoSalon(salón, fecha)` y exige
cobertura COMPLETA de `[horaInicio, horaFin]` (no solo semanal: la excepción de la fecha se
considera). `CERRADO`/`NO_OPERATIVO` rechazan cualquier reserva. El resto de reglas existentes
(capacitación, turno vigente, traslape) se conservan sin refactor adicional.

## HorarioEfectivoSalon
SIN CAMBIOS. Ni firma, ni comportamiento, ni precedencia.

## 23505
`ConflictoExcepcionHorarioTranslator`, análogo exacto de `ConflictoVigenciaHorarioTranslator`:
`saveAndFlush` dentro del bloque `try`, clasificación estricta por SQLSTATE `23505` +
`idx_salon_horario_excepcion_unica` (o evidencia inequívoca si el driver no reporta el nombre).
Confirmado contra PostgreSQL real (Testcontainers) que Hibernate SÍ reporta el nombre del índice
único parcial. Cualquier otra violación de integridad se relanza intacta (verificado con FK a
salón inexistente).

## Errores
`SalonHorarioExcepcionErrores`: seis códigos (`EXCEPCION_HORARIO_EN_EL_PASADO`,
`HORARIO_ESPECIAL_INCOMPLETO`, `HORA_CIERRE_DEBE_SER_POSTERIOR` reutilizado de
`HorarioOperacionErrores`, `EXCEPCION_HORARIO_NO_EXISTE`,
`PROGRAMACION_PUNTUAL_INCOMPATIBLE_CON_EXCEPCION`, `CONFLICTO_EXCEPCION_HORARIO`), whitelist 409
cerrada de dos (los dos últimos). La traducción whitelist→`ConflictException` vive dentro del
propio `SalonHorarioExcepcionService` (no existe una capa de aplicación separada para excepciones,
a diferencia de horarios versionados). `ErrorResponse.codigo` se puebla igual que en el resto del
proyecto, vía `CodigoErrorExtractor` (ningún parser nuevo; guard `CodigoErrorExtractorArquitecturaTest`
extendido para cubrir el archivo nuevo).

## Tests
77 métodos nuevos/extendidos sobre la baseline (398 → 477):

- Unit (Mockito): `SalonHorarioExcepcionServiceTest` (25, W1-W10 + no-op cerrado + convergencia
  por-fecha + impacto/cancelación resultante), `ConflictoExcepcionHorarioTranslatorTest` (8),
  `ImpactoPuntualEnExcepcionHorarioTest` (12, I1-I4).
- PostgreSQL/Testcontainers: `SalonHorarioExcepcionPersistenciaTest` (9, P1-P7).
- Concurrencia real: `SalonHorarioExcepcionConcurrenciaTest` (5, C1-C5), mismo patrón que
  `HorarioOperacionConcurrenciaTest` (`TransactionTemplate` + `CountDownLatch`, sin `Thread.sleep`
  como sincronización).
- API (`@WebMvcTest`): `SalonHorarioExcepcionControllerTest` (14, A1-A4), con el
  `SalonHorarioExcepcionService` real importado (no mockeado) porque la traducción whitelist vive
  ahí.
- Safety nets añadidos a suites existentes: `ReservaServiceCaracterizacionTest` (+4: lock, uso de
  `HorarioEfectivoSalon`, rechazo por CERRADO, rechazo por horario especial más estrecho),
  `TurnoInstructorServiceCaracterizacionTest` (reemplazo de
  `crearExcepcionNoTomaElLockDeSalon`, que caracterizaba el comportamiento PRE-F2C.2, por
  `crearExcepcionTomaElLockDeSalonAntesDeLeerElHorario`).
- Javadoc de test actualizado en `HorarioOperacionWritersPersistenciaTest` (comentario que ya no
  era cierto tras F2C.2).

No se duplicó cobertura de `HorarioEfectivoSalonTest` (sigue intacto, sin cambios).

## PostgreSQL
PASS (9/9). Confirma CHECK, índice único parcial (N inactivas + 1 activa permitido, dos activas
rechazado), cancelar+recrear real, traducción específica del 23505 con código estable, no
traducción de otra violación de integridad (FK), y que cerrar un día con `BloqueProgramacion` y
`TurnoInstructor` RECURRENTE existentes los deja bit-a-bit idénticos.

## Concurrencia
PASS (5/5). C4 y C5 confirman que el lock añadido a `TurnoInstructorService.crear(EXCEPCION)` y a
`ReservaService.crear` sí serializa contra el writer de excepción (fallarían si el lock solo
viviera en un lado).

## Mutaciones A-N
TODAS DETECTADAS POR TEST:

| # | Mutación | Detectada por |
|---|---|---|
| A | Writer sin `SalonLock` | `SalonHorarioExcepcionServiceTest.protocoloCompletoEnOrdenAutorizarLockLeer` (W9, `InOrder`), `SalonHorarioExcepcionConcurrenciaTest` (C1) |
| B | `LocalDate.now()` directo | `SalonHorarioExcepcionServiceTest.usaElRelojFijoNoElRelojDelSistema` (W10) |
| C | Editar/cancelar pasado | `temporalidadDeAlta`, `temporalidadDeModificacion`, `temporalidadDeCancelacion` (W1, W5) |
| D | `CERRADO` con horas | `cerradoConHorasSeNormalizaANull` (W8), `SalonHorarioExcepcionPersistenciaTest.checkRechazaCerradoConHoras` (P1) |
| E | `cierre <= apertura` | `cierreIgualAAperturaLanzaHoraCierreDebeSerPosterior`/`cierreAnteriorA...` (W8), P1 |
| F | 23505 solo 409 genérico sin código | `SalonHorarioExcepcionPersistenciaTest.violacionRealDelIndiceSeTraduceConCodigoEstable` (P5), `SalonHorarioExcepcionControllerTest.violacionDelIndiceUnicoDevuelve409ConCodigoEstable` |
| G | Cancelar/recrear choca con el índice | `soloFilasInactivasInsertaFilaNuevaSinReactivar` (W7), P3/P4, `SalonHorarioExcepcionConcurrenciaTest.cancelarPrimeroHaceQueModificarCreeUnaFilaNueva` (C2) |
| H | Excepción modifica template recurrente | `SalonHorarioExcepcionPersistenciaTest.cerrarUnDiaConProgramacionRecurrenteLaDejaIdentica` (P7) |
| I | Recurrente bloquea festivo | `ImpactoPuntualEnExcepcionHorarioTest.laConsultaDeRecurrentesNuncaSeInvoca` (I1) — invariante arquitectónica: el adapter no tiene dependencia a esa consulta |
| J | Turno puntual fuera del horario permitido | `ImpactoPuntualEnExcepcionHorarioTest` (I2 parametrizado), `impactoPuntualIncompatibleLanzaConflictException` (I5) |
| K | API nueva rompe el `DELETE` legacy | `SalonHorarioExcepcionControllerTest.deleteConUuidLlegaAlHandlerLegacyYConFechaAlDePorFecha` (A2) |
| L | Reserva confirmada incompatible permitida | `ImpactoPuntualEnExcepcionHorarioTest` (I3), `ReservaServiceCaracterizacionTest.rechazaReservaFueraDelHorarioEfectivoPor...`, `SalonHorarioExcepcionConcurrenciaTest.excepcionDeCierrePrimeroHaceQueLaReservaSeaRechazada` (C5) |
| M | `ReservaService.crear` no usa `HorarioEfectivoSalon` | `ReservaServiceCaracterizacionTest.crearReservaResuelveHorarioEfectivoDeLaFechaSolicitada` + los dos rechazos, C5 |
| N | Turno EXCEPCION no participa del lock | `TurnoInstructorServiceCaracterizacionTest.crearExcepcionTomaElLockDeSalonAntesDeLeerElHorario`, `SalonHorarioExcepcionConcurrenciaTest.excepcionDeCierrePrimeroHaceQueElTurnoExcepcionSeaRechazado` (C4) |

## Build
PASS (`./mvnw package -DskipTests`, exit 0).

## Scope
Solo `ubicaciones`, `calendario` (backend) y sus tests. Sin migraciones. Sin cambios en `pom.xml`.
Sin frontend. Sin mobile.
