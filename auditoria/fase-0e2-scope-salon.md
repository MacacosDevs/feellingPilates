# Fase 0E.2 — Autorización contextual por salón

## Checkpoint

- Branch base verificada: `seguridad/reservas-idor`
- Commit base verificado: `0960657ae975ffe946872f182b15d3c886b27924`
- Branch de trabajo: `seguridad/scope-salon`
- Flyway: sin cambios

## Problema

Los `@PreAuthorize` existentes validaban authorities funcionales globales, pero no vinculaban el permiso con el salón real del recurso. Un actor con un permiso válido para una sede podía operar otra sede si conocía un UUID válido o enviaba otro `salonId`.

## Modelo actual y regla adoptada

`UsuarioRol` asocia usuario, rol y un salón opcional. El modelo existente documenta y aplica estas reglas mediante `SedeRolValidador`, `AltaUsuarioService` y `UsuarioService`:

- `PERSONAL` e `INSTRUCTOR` se asignan a uno o más salones concretos;
- `ADMIN` y `SUPER_ADMIN` son globales y se asignan sin salón;
- `CLIENTE` sin salón no constituye acceso administrativo global.

Se creó `AutorizadorSalon`, que consulta el `Usuario` y sus asignaciones vigentes desde BD dentro de una transacción de lectura. La autorización exige que una misma asignación `UsuarioRol` conceda uno de los permisos requeridos y tenga scope sobre el salón objetivo. No se combinan el permiso de un rol con el scope de otro. `SUPER_ADMIN` conserva la semántica existente de todos los permisos sólo cuando su asignación es global legítima.

La política deniega explícitamente usuario inexistente, usuario inactivo, ausencia de permiso y ausencia de scope mediante `AccessDeniedException` (HTTP 403 por el handler existente). Los `@PreAuthorize` se conservan como primera barrera y el JWT no se usa como fuente de scope.

## Operaciones protegidas

### Horarios y excepciones

- listar y guardar autorizan el `salonId` objetivo antes de consultar o modificar;
- eliminar carga primero `SalonHorarioExcepcion`, obtiene su salón persistido, verifica que coincida con el salón contextual de la ruta y autoriza contra el salón real;
- un UUID de excepción perteneciente a otra sede no puede eliminarse con scope local.

### Calendario y turnos

- crear autoriza el salón del comando con los permisos alternativos ya admitidos por el endpoint según el tipo de turno;
- actualizar y eliminar cargan primero `TurnoInstructor` y autorizan contra `turno.salon.id` persistido;
- no se confía en un salón enviado por el cliente para recursos existentes.

### Reservas administrativas

- listar por salón y crear autorizan el salón solicitado;
- cancelar carga primero `Reserva` y autoriza contra `reserva.salon.id` persistido;
- el flujo `GET /api/reservas/mias` de 0E.1 conserva ownership por principal y no se convirtió en un flujo administrativo.

## Tests

La política central cubre:

1. permiso y scope del salón: permitido;
2. mismo permiso en otro salón: denegado;
3. salón correcto sin permiso: denegado;
4. `ADMIN` global legítimo en dos salones: permitido;
5. usuario inactivo: denegado;
6. usuario inexistente: denegado.

Los tests HTTP ejercitan bypass cross-salón con UUID válido para:

- eliminación de excepción de horario;
- eliminación de turno;
- cancelación administrativa de reserva.

En los tres casos el actor posee la authority funcional en el borde, pero su asignación vigente pertenece a otro salón: la respuesta es 403 y no se persiste la modificación.

## Gaps pendientes

- lecturas de turnos por salón;
- horario semanal general y otros controllers de salón;
- `VentaService`, caja y reembolsos;
- inventario y catálogo por sede;
- usuarios/personal por sede;
- futuros módulos de Programación;
- futuros módulos y políticas de Reservas administrativas;
- adopción transversal del patrón en el resto del proyecto.

No se modificaron Stripe, pagos, Programación futura, sesiones, capacidad, Beneficios, Outbox, notificaciones ni migraciones SQL.

## Resultado

MECANISMO CENTRAL:
`AutorizadorSalon`

SCOPE:
BD (`Usuario` → `UsuarioRol` → `Rol`/`Permiso` y `Salon`)

HORARIOS/EXCEPCIONES:
PROTEGIDO

TURNOS:
PROTEGIDO

CANCELACION RESERVA ADMIN:
PROTEGIDA

TEST CROSS-SALON:
PASS

TESTS:
23/23 PASS

BUILD:
PASS

FLYWAY:
SIN CAMBIOS

Branch:
`seguridad/scope-salon`

Commit:
el commit que contiene este checkpoint, reportado al finalizar la fase
