# FeelingPilates — Arquitectura objetivo consolidada

> Base autoritativa: working tree inspeccionado y `auditoria/00-revalidacion-repositorio-completo.md`.
> Este documento describe un objetivo evolutivo; no afirma que los conceptos objetivo ya estén implementados.

## 1. Executive Summary

- **CONFIRMADO POR CÓDIGO ACTUAL:** el backend es un monolito Spring Boot con PostgreSQL, 150 fuentes Java, 21 entidades JPA y una base funcional de identidad, sedes, programación, reservas, ventas presenciales y Stripe.
- **CONFIRMADO POR CÓDIGO ACTUAL:** `calendario` mezcla plantilla, ajustes puntuales y reservas; no hay sesión/ocurrencia estable, capacidad grupal, consumo de recursos ni beneficios consumibles.
- **DECISIÓN ARQUITECTÓNICA:** evolucionar a un monolito modular, sin microservicios, broker, Spring Modulith ni reescritura Big Bang.
- **DECISIÓN ARQUITECTÓNICA:** `calendario` evolucionará conceptualmente a `programacion`; las reservas serán un módulo separado y las sesiones un componente con identidad persistente propiedad de Programación.
- **DECISIÓN ARQUITECTÓNICA:** la disponibilidad se resuelve en backend desde horario operativo, recurrencia, ajustes, confirmación, ventana, sesión, reservas y recursos compartidos.
- **DECISIÓN ARQUITECTÓNICA:** Catálogo, Ventas, Pagos y Beneficios tendrán ownership distinto; `Compra` coexistirá como modelo legado durante una transición aditiva.
- **DECISIÓN ARQUITECTÓNICA:** Stripe será infraestructura de Pagos con Inbox durable; Outbox y Notificaciones serán conceptos separados sobre PostgreSQL y workers `@Scheduled`.
- Antes de grandes cambios deben resolverse la no reproducibilidad del working tree, el riesgo de V36, el test fallido, la cobertura crítica y los defectos de seguridad.

## 2. Principios

1. Un solo deployment y una sola base PostgreSQL no implican un grafo de entidades o repositorios global.
2. Cada módulo modifica sus propios datos; otro módulo usa una API interna, un evento durable o una proyección explícita.
3. Las invariantes que evitan sobreventa son fuertes y transaccionales; email, push, recordatorios y proyecciones toleran consistencia eventual.
4. Se preserva lo que ya funciona y se agrega el modelo nuevo en paralelo antes de retirar responsabilidades de `Compra`, `TurnoInstructor` o `Reserva`.
5. No se crean interfaces, eventos, capas o módulos sin una variación, consumidor o frontera real.
6. Los intervalos usan semántica `[inicio, fin)`: tocar bordes es válido; traslaparlos no.
7. Reglas recurrentes no se expanden infinitamente; sólo se materializa el horizonte operativo necesario.
8. Toda decisión sensible a sede usa el principal y el alcance vigentes en BD, no parámetros del cliente ni claims JWT como autoridad.
9. Los nombres del dominio se mantienen en español; nombres oficiales como Stripe permanecen en adaptadores de infraestructura.

## 3. Module Map

**Módulo: Identidad y Acceso**  
Responsabilidad: autenticación, usuarios, perfiles, RBAC, scope por sede, principal e invitaciones. Owns: `Usuario`, `PerfilInstructor`, `UsuarioRol`, `Rol`, `Permiso`, `InvitacionUsuario` y credenciales.  
Expone: principal/permisos/scopes vigentes, elegibilidad e identidad/contacto controlado.  
Puede depender de: IDs de Salón y Outbox. NO debe depender de: Programación, Reservas, Ventas, Pagos, Beneficios ni email.
**Módulo: Operación y Oferta**  
Responsabilidad: sedes, horario, actividades, recursos e inventario. Owns: `Salon`, horarios/excepciones, `TipoActividad`, `TipoRecurso`, `SalonRecurso`, `ActividadRecurso`.  
Expone: horario efectivo, duración/oferta, requisitos, inventario y políticas por salón.  
Puede depender de: API de Identidad para autorizar comandos. NO debe depender de: Programación, Reservas, Ventas, Pagos o Notificaciones.
**Módulo: Programación y Sesiones**  
Responsabilidad: plantilla, bloques, asignaciones, ajustes, confirmación, resolución y ocurrencias. Owns: `BloqueProgramacion`, `Asignacion`, `AjusteProgramacionFecha`, `ConfirmacionInstructor`, `Sesion`.  
Expone: programación efectiva, sesiones estables y estado publicable.  
Puede depender de: APIs de Operación e Identidad y Outbox. NO debe depender de: repositorios de Reservas, Pagos, Ventas o Notificaciones.
**Módulo: Reservas**  
Responsabilidad: booking, estado, cupo, recurso y cancelación. Owns: `Reserva` futura y `ConsumoRecursoReserva`; referencia `sesionId`, `clienteId` y movimientos por ID.  
Expone: reservar, cancelar, consultar reservas del principal y ocupación.  
Puede depender de: APIs de Sesiones, Operación, Identidad y Beneficios. NO debe depender de: `PagoRepository`, Stripe, recurrencia ni entidades JPA ajenas.
**Módulo: Catálogo Comercial**  
Responsabilidad: productos/paquetes y derechos prometidos. Owns: `Paquete`, `PaqueteActividad`, precio, vigencia y disponibilidad comercial.  
Expone: producto vigente y snapshot de línea.  
Puede depender de: IDs/lecturas de actividades. NO debe depender de: Pagos, Stripe, Reservas ni beneficios adquiridos.
**Módulo: Ventas**  
Responsabilidad: checkout, ticket, líneas, canal, totales, cliente, sede y vendedor. Owns: `Venta`, `LineaVenta`, snapshots y adaptación temporal de `Compra`.  
Expone: iniciar/completar/cancelar y consultar ticket.  
Puede depender de: Catálogo, Identidad y Operación. NO debe depender de: Stripe SDK, `PagoRepository`, Beneficios ni plantillas de mensaje.
**Módulo: Pagos**  
Responsabilidad: cobros, método/proveedor, estado financiero, reembolsos y conciliación. Owns: `Pago`, `Reembolso`, idempotencia y `StripeInboxEvent`.  
Expone: registrar/confirmar/fallar/reembolsar con transiciones monotónicas.  
Puede depender de: `ventaId` como contrato y adapters internos. NO debe depender de: Catálogo, Reservas, Beneficios, Notificaciones ni entidades de Ventas.
**Módulo: Beneficios**  
Responsabilidad: derechos, vigencia, saldo, ledger, consumo y reversión. Owns: `BeneficioAdquirido`, `MovimientoBeneficio`.  
Expone: otorgar, consumir, revertir y consultar saldo.  
Puede depender de: IDs/snapshots de línea. NO debe depender de: Stripe, recurrencia, repositorios de Venta/Reserva ni proveedores de mensajes.
**Módulo: Notificaciones**  
Responsabilidad: mensajes durables e intentos/canales. Owns: `Notificacion`, snapshot de destinatario/plantilla e `IntentoEntrega`.  
Expone: materializar desde evento y consultar entrega.  
Puede depender de: Outbox y adapters Email/Push. NO debe depender de: entidades/repositorios JPA internos de otros módulos.

Mensajería técnica no es bounded context: `OutboxEvent` es infraestructura compartida. Coordinadores de caso de uso, por encima de módulos y sin repositorios propios, pueden invocar varias APIs en una transacción local sin crear dependencias recíprocas.

## 4. Ownership

| Dato/regla | Owner | Referencia desde otros módulos |
|---|---|---|
| Usuario, rol, permiso, scope de sede | Identidad y Acceso | `usuarioId` + API de autorización |
| Salón, horario y política operativa | Operación y Oferta | `salonId` + API/lectura |
| Actividad, duración y recurso requerido | Operación y Oferta | `tipoActividadId` + snapshot cuando haga falta historia |
| Inventario físico | Operación y Oferta | lock/API de capacidad, nunca copia por instructor |
| Bloque, asignación y ajuste | Programación | IDs propios y resolver por fecha |
| Sesión concreta | Programación/Sesiones | `sesionId`; Reservas no reconstruye la recurrencia |
| Reserva y asignación de recurso | Reservas | `reservaId`; otros reciben hechos |
| Producto vendible | Catálogo | snapshot inmutable en `LineaVenta` |
| Ticket y líneas | Ventas | `ventaId`/`lineaVentaId` |
| Cobro y reembolso | Pagos | `pagoId` vinculado a `ventaId` |
| Derecho, saldo y movimiento | Beneficios | `beneficioId`/`movimientoId` |
| Evento técnico pendiente | Outbox | payload versionado, no entidad de dominio compartida |
| Mensaje e intentos | Notificaciones | IDs del hecho y destinatario; sin joins al grafo global |

La frontera de Sesiones permanece dentro de Programación porque la ocurrencia nace de una asignación efectiva y conserva su identidad aun sin reservas. Reservas sólo ocupa esa ocurrencia.

## 5. Dependency Rules

Leyenda: `D` contrato directo estable (ID/value/event schema), `A` API síncrona de módulo, `E` evento durable, `L` lectura/proyección, `—` prohibida/no necesaria. Ninguna celda permite importar repositorios ajenos.

| Desde \ Hacia | Identidad | Operación | Programación | Reservas | Catálogo | Ventas | Pagos | Beneficios | Notificaciones |
|---|---:|---:|---:|---:|---:|---:|---:|---:|---:|
| Identidad | — | D | — | — | — | — | — | — | E |
| Operación | A | — | — | — | — | — | — | — | — |
| Programación | A | A | — | E | — | — | — | — | E |
| Reservas | A | A | A | — | — | — | — | A | E |
| Catálogo | A | A/L | — | — | — | — | — | — | — |
| Ventas | A | A | — | — | A | — | — | — | E |
| Pagos | A | — | — | — | — | D | — | — | E |
| Beneficios | A | D | — | — | D | D | — | — | — |
| Notificaciones | L | — | — | — | — | — | — | — | — |

- `reservas -> PagoRepository` queda prohibido: reservar consume un beneficio, no consulta cómo fue pagado.
- `ventas -> Stripe SDK` queda prohibido: el coordinador solicita el Pago; sólo el adapter Stripe conoce el SDK.
- `programacion -> ReservaRepository` queda prohibido: una cancelación de sesión publica `SesionCancelada`; el workflow consumidor aplica la política de reservas idempotentemente.
- `actividades -> reservas` queda prohibido: actividad define requisitos, no observa bookings.
- Los dashboards pueden usar proyecciones SQL de lectura explícitas; no autorizan escritura ni devuelven entidades internas.
- Un evento se usa sólo cuando el consumidor tolera eventualidad; una API transaccional se usa para cupo, saldo y estados financieros.
- Un coordinador transaccional de checkout llama Ventas, Pagos y Beneficios; esos módulos no se llaman en ciclo ni comparten repositorios.

## 6. Identidad y Seguridad

- **CONFIRMADO POR CÓDIGO ACTUAL:** `JwtAuthFilter` valida el JWT y recalcula roles/permisos desde BD; se conserva esa dirección.
- Autenticación valida credenciales/proveedor; `JwtService` emite y verifica firma, expiración, issuer y audience; ninguno decide por sí solo permisos de negocio.
- El principal objetivo contiene `usuarioId`, identidad mínima y un contexto de autorización resuelto en la petición; claims de roles/scopes son informativos, no autoritativos.
- `UsuarioRol` posee el alcance por salón. Una política central `puedeOperarSalon(actorId, permiso, salonId)` vive en Identidad/Seguridad y consulta asignaciones vigentes.
- Cada caso de uso contextual invoca esa política antes de cargar/modificar el agregado; ADMIN/SUPER_ADMIN globales se resuelven allí, no en cada service.
- “Mis reservas” toma `clienteId` del principal. Un ID administrativo requiere permiso explícito y scope sobre la sede de la reserva; esto elimina el IDOR actual.
- Invitaciones usan token aleatorio de un solo uso, expiración, hash persistido y redacción en logs; el enlace completo no se registra.
- No se registran passwords, extremos/longitud de secretos, JWT, tokens, `clientSecret` ni payloads sensibles sin redacción.
- Google Auth continúa deshabilitado hasta definir contrato, configuración y tests; no se presenta como capacidad existente.
- Registro público requiere rate limiting, antiabuso y observabilidad antes de exponerse ampliamente.

## 7. Salones y operación

- `HorarioOperacion` se conserva como concepto y evoluciona a regla semanal con `vigenteDesde`/`vigenteHasta`; `null` significa vigencia indefinida.
- El horario efectivo de una fecha selecciona la regla vigente del día y luego aplica una `SalonHorarioExcepcion` activa para esa fecha.
- La excepción representa `CERRADO` o un intervalo especial completo; puede ampliar 08:00–20:00 a 07:00–22:00 o reducirlo sólo ese día.
- No se infiere que haya clases en todo el horario: únicamente limita bloques y sesiones.
- Se mantiene un intervalo por día mientras el negocio no confirme jornadas partidas; el contrato puede evolucionar sin mezclarlo con bloques.
- Toda publicación/reserva revalida que la sesión esté dentro del horario efectivo, incluso si nació de una plantilla válida en el pasado.
- Políticas por salón: `requiereConfirmacionInstructor`, plazo configurable, `anticipacionMaximaReserva` y `anticipacionMinimaReserva`.

## 8. Actividades y recursos

- **CONFIRMADO POR CÓDIGO ACTUAL:** `TipoActividad.duracionMinutos`, `TipoRecurso`, `SalonRecurso` y `ActividadRecurso.cantidad` son bases válidas.
- Operación posee catálogo/duración, oferta por salón, recurso requerido e inventario; Programación sólo referencia actividades.
- Una asignación debe usar una actividad activa, ofrecida por el salón y compatible con la especialidad del instructor.
- `participantesPorReserva` describe personas representadas por un booking; no es capacidad de sesión ni unidades de equipo.
- `ActividadRecurso.cantidad` es consumo por reserva: Reformer=1 y Duo Reformer=2, independientemente del número de instructores.
- Inventario y Programación no se mezclan; la comprobación transaccional ocurre al reservar y la definición permanece en Operación.

## 9. Programación

- **DECISIÓN ARQUITECTÓNICA:** `programacion` es el nombre objetivo; “Agenda/Calendario” puede seguir en UI.
- Su secuencia es: horario operativo → bloques recurrentes → asignaciones → ajustes por fecha → programación efectiva → confirmación → sesiones → reservas.
- Persisten reglas con identidad/historia (`Bloque`, `Asignacion`, `Ajuste`), confirmaciones con estado y sesiones con efectos externos.
- Se calculan la composición efectiva y elegibilidad; se materializan sólo sesiones dentro de un horizonte finito.

## 10. Bloques y asignaciones

- `BloqueProgramacion` pertenece a un salón, día y vigencia, con `[horaInicio,horaFin)` contenido en el horario operativo.
- N bloques pueden existir por día; no se traslapan en el mismo salón, pero `08:00–12:00` y `12:00–14:00` son válidos.
- El bloque es contenedor operativo: no tiene instructor, actividad, sesión, cupo ni reserva.
- `TurnoInstructor` actual evoluciona gradualmente hacia ese rol; sus tipos EXCEPCION/CANCELACION dejan de cargar semánticas heterogéneas.
- `Asignacion` tiene ID propio, `bloqueId`, `instructorId`, `[inicio,fin)` y exactamente un `tipoActividadId`.
- Cambiar actividad divide filas: Ariadna 08:00–10:00 Reformer y 10:00–14:00 Mat son dos asignaciones.
- El ID propio elimina la PK actual que impide repetir la misma actividad en rangos disjuntos.
- Varios instructores y la misma actividad pueden coexistir; la actividad no vuelve exclusivo al salón.
- Invariante global: las asignaciones efectivas de un instructor no traslapan entre ningún salón para la misma fecha.
- La validación pertenece a Programación y se protege en persistencia/transacción, no al crear la primera reserva.

## 11. Recurrencia y ajustes por fecha

- Bloques/asignaciones recurrentes tienen `vigenteDesde` inclusivo y `vigenteHasta` inclusivo o nulo; una edición futura cierra la versión previa.
- No se generan filas infinitas para la recurrencia semanal.
- `AjusteProgramacionFecha` tiene identidad, fecha, salón, operación y target opcional (`bloqueId` o `asignacionId`).
- Operaciones mínimas: `AGREGAR`, `CANCELAR`, `REEMPLAZAR`; reemplazar referencia el origen y guarda el valor efectivo nuevo.
- Un ajuste puede agregar/quitar/reemplazar instructor, rango o actividad; cancelar sólo una asignación; crear/cancelar/modificar un bloque.
- Cierre/ampliación/reducción de sede permanece en `SalonHorarioExcepcion`, no se duplica como ajuste de programación.
- Los ajustes son composables y de grano fino; no sustituyen todo el día de un instructor por mera presencia.
- Orden conflictivo y ajustes incompatibles se rechazan al escribir; el resolver no adivina precedencias ambiguas.

## 12. Programación efectiva

- `ProgramacionEfectivaResolver` pertenece a Programación y recibe una fecha/salón.
- Resuelve: horario semanal vigente → excepción operativa → reglas recurrentes vigentes → ajustes dirigidos → validaciones globales.
- El resultado calculado contiene bloques/asignaciones efectivos, origen, versión y razones de indisponibilidad; clientes nunca ejecutan esa composición.
- Se puede cachear como proyección descartable, invalidada por versión, sin convertirla en autoridad.
- La identidad/historia se mantiene en reglas, ajustes y `Sesion`; no en un JSON diario opaco.

## 13. Confirmación de instructor

- La política y deadline pertenecen al salón; la confirmación de una ocurrencia/asignación pertenece a Programación.
- Si no se requiere confirmación, la sesión nace confirmada; si se requiere, nace `PENDIENTE_CONFIRMACION` con deadline calculado.
- Confirmar registra actor, instante y versión de asignación; un cambio material invalida o renueva confirmación según política pendiente.
- Al vencer el plazo, un worker hace transición idempotente a `NO_DISPONIBLE` y publica el hecho correspondiente.
- `PUBLICABLE` es elegibilidad derivada: confirmada, dentro de ventana, activa, no cancelada y con capacidad consultable.
- Una sesión pendiente nunca aparece en disponibilidad del cliente.

## 14. Sesiones / ocurrencias

- **DECISIÓN ARQUITECTÓNICA:** existirá `Sesion` persistente con ID estable, propiedad del componente Sesiones dentro de Programación.
- Guarda origen/versiones, fecha, `[inicio,fin)`, salón, instructor, actividad y estado; son snapshots históricos, no una copia mutable del catálogo.
- Una asignación 10:00–12:00 con duración 60 produce 10:00–11:00 y 11:00–12:00; con duración 30 produce cuatro sesiones.
- Se materializa un horizonte acotado por la mayor ventana de reserva más margen operativo; un job incremental lo extiende.
- Una restricción natural evita duplicar el mismo slot/origen/versión cuando el job se reintenta.
- Reservas referencia `sesionId`; salón/instructor/actividad/horas pueden conservarse como snapshot de lectura, no como identidad alternativa.
- Cancelación de sesión es distinta de cancelar plantilla/asignación: bloquea nuevas reservas y emite `SesionCancelada`.
- Cambiar instructor conservando identidad y el tratamiento de reservas existentes quedan como decisión de negocio explícita.

## 15. Capacidad y recursos compartidos

- La unidad de inventario es `salonId + tipoRecursoId`, nunca instructor.
- Disponibilidad para un intervalo = inventario operativo − consumo de todas las reservas activas de sesiones traslapadas.
- Dos sesiones Reformer simultáneas con seis y cuatro reservas consumen los diez reformers del salón.
- Una reserva Duo consume dos unidades según `ActividadRecurso`, no dos “lugares” asumidos.
- Reservas posee las asignaciones/consumos realizados; Operación posee inventario y requisitos.
- El caso de uso bloquea en orden canónico los `SalonRecurso` implicados y suma consumos activos antes de insertar.
- La capacidad mostrada es una lectura momentánea, no una garantía; el comando vuelve a validar bajo lock.
- **DECISIÓN DE NEGOCIO PENDIENTE:** capacidad máxima cuando una actividad no usa recurso limitante y si existe límite humano adicional por sesión.

## 16. Reservas

- Programación responde qué sesión existe; Reservas responde quién la ocupa y qué consumió.
- El comando objetivo recibe `sesionId`; para cliente final obtiene `clienteId` del principal, no del body.
- Valida sesión publicable/ventana, ausencia de doble reserva del cliente, recursos, beneficio y reglas de cancelación.
- Crea `Reserva`, `ConsumoRecursoReserva`, consumo de beneficio y Outbox en una transacción fuerte.
- Varias reservas pueden apuntar a la misma sesión; la primera no vuelve ocupado al instructor.
- Cancelar cambia estado, libera consumos y revierte beneficio según política, sin borrar historia.
- La idempotencia del comando pertenece a Reservas y liga clave + actor + operación + payload normalizado.
- La cancelación masiva por `SesionCancelada` es un consumidor durable, idempotente y observable.

## 17. API móvil de disponibilidad

- Lectura conceptual: `GET /disponibilidad?actividadId&fecha` agrupa salones y sesiones publicables.
- Cada sesión expone `sessionId`, salón, inicio/fin, instructor, lugares/unidades disponibles y estado reservable.
- Filtros opcionales pueden acotar salón; el contrato no devuelve turnos recurrentes ni ajustes crudos.
- Escritura conceptual: `POST /reservas` con `sessionId` e idempotency key; identidad del cliente sale del principal.
- Cancelación conceptual: `POST /reservas/{id}/cancelacion` con motivo permitido y ownership contextual.
- El backend resuelve horario, recurrencia, excepción, confirmación, slots, ventana y capacidad.
- La respuesta puede quedar obsoleta entre lectura y comando; conflicto de última plaza se devuelve como resultado de negocio consistente.

## 18. Catálogo

- `Paquete` y `PaqueteActividad` se conservan como catálogo comercial, fuera del proveedor de pagos.
- Un paquete describe uno o varios derechos por actividad, cantidad, vigencia ofrecida, precio y estado.
- Venta captura snapshot de nombre, composición, precio, moneda y reglas relevantes; cambios posteriores no reescriben tickets.
- No se diseña catálogo ecommerce, promociones o impuestos hasta que el negocio los confirme.

## 19. Ventas

- `Venta` es cabecera: cliente, canal, salón opcional/obligatorio según canal, vendedor, moneda, totales, estado e instantes.
- `LineaVenta` tiene producto, cantidad, importes y snapshot; es el origen de beneficios otorgados.
- El flujo actual `grupoCompraId` se adapta como ticket legado mientras nuevas ventas se escriben de forma aditiva.
- Una tabla de correspondencia o referencia legado→nuevo permite doble lectura temporal sin reescribir historia a ciegas.
- Completar una venta exige pago confirmado conforme a su política; efectivo/transferencia pueden confirmarse en el mismo workflow.
- Pagos parciales, múltiples métodos, descuentos e impuestos permanecen decisiones abiertas, no campos especulativos.

## 20. Pagos

- `Pago` separa `MetodoPago` (efectivo, transferencia, tarjeta) de `ProveedorPago` (ninguno, Stripe u otro).
- Mantiene importe, moneda, `ventaId`, estado monotónico, referencias externas e idempotencia ligada a actor/venta.
- `Reembolso` tiene identidad/estado/importe/motivo/proveedor; no se modela sólo cambiando el estado de Venta.
- Efectivo y transferencia se confirman localmente; Stripe se confirma por Inbox/reconciliación, no por respuesta del móvil.
- Stripe queda detrás de `StripePaymentGateway`; sólo infraestructura usa el SDK.
- La creación remota de PaymentIntent usa transacciones locales cortas y reconciliación; nunca mantiene un lock DB durante red.

## 21. Beneficios

- `BeneficioAdquirido` representa el derecho originado por una línea: cliente, actividad/regla, cantidad inicial, vigencia y saldo.
- Paquetes mixtos generan componentes de beneficio por actividad sin perder `lineaVentaId` de origen.
- `MovimientoBeneficio` es ledger inmutable: OTORGAMIENTO, CONSUMO, REVERSIÓN, EXPIRACIÓN y AJUSTE auditado.
- El saldo es una proyección protegida/derivable del ledger, actualizada con versión o lock; nunca se infiere de compras pagadas.
- Reserva conserva el `movimientoConsumoId`; cancelación crea reversión, no borra el consumo.
- Venta+otorgamiento y Reserva+consumo son fuertes cuando la regla exige saldo para completar la operación.
- Inicio de vigencia, combinación de paquetes y refund con clases usadas son decisiones de negocio pendientes.

## 22. Stripe e Inbox

- Flujo inbound: firma válida → inserción `StripeInboxEvent` con `eventId` UNIQUE, tipo, payload, recibidoEn y estado → COMMIT.
- Un worker reclama eventos con lock/lease, procesa transiciones idempotentes y registra intentos/error.
- Estados `RECIBIDO`, `PROCESANDO`, `PROCESADO`, `FALLIDO_REINTENTABLE`, `FALLIDO_FINAL`; el pago aplica una máquina monotónica.
- Eventos repetidos se reconocen por Event ID; eventos desordenados no regresan un Pago confirmado a pendiente/fallido.
- El handler delega al coordinador transaccional: confirma Pago, completa Venta, crea Beneficios y Outbox sin llamada remota.
- Reconciliación consulta Stripe fuera de la transacción de aplicación y convierte el resultado en el mismo comando idempotente.
- **DECISIÓN ARQUITECTÓNICA:** Inbox pertenece a Pagos/infraestructura Stripe; no es Outbox.

## 23. Outbox y Notificaciones

- Cada business transaction inserta `OutboxEvent` en la misma transacción que el cambio de dominio.
- El procesador reclama lotes con `SKIP LOCKED`/lease, entrega al consumidor local y marca resultado idempotentemente.
- `OutboxEvent` representa un hecho pendiente; `Notificacion` representa un mensaje para un destinatario y puede sobrevivir al evento.
- El consumidor materializa Notificación; un delivery worker llama Email/Push fuera de la transacción de negocio.
- Entrega at-least-once: proveedores pueden recibir reintentos y requieren clave idempotente cuando la soporten.
- Política inicial: inmediato, +1 minuto, +5 minutos, +30 minutos y luego `FAILED`.
- PostgreSQL + `@Scheduled` es suficiente inicialmente; no hay broker ni microservicio.

## 24. Eventos

| Evento | Productor | Consumidor | Durable | Por qué existe |
|---|---|---|---|---|
| `VentaCompletada` | Ventas/Pagos workflow | Notificaciones/proyecciones | Sí | comunicar ticket completado; Beneficios ya se crearon fuerte |
| `ReservaCreada` | Reservas | Notificaciones/recordatorios | Sí | confirmación y agenda derivada |
| `ReservaCancelada` | Reservas | Notificaciones/proyecciones | Sí | comunicar liberación/cancelación |
| `InvitacionUsuarioCreada` | Identidad | Notificaciones | Sí | sacar email de la transacción de alta |
| `ReembolsoConfirmado` | Pagos | Notificaciones/proyecciones | Sí | informar hecho financiero confirmado |
| `SesionCancelada` | Programación/Sesiones | Reservas y Notificaciones | Sí | cancelar bookings según política; no equivale a `TurnoInstructor.CANCELACION` |

No se crea un evento para validaciones síncronas ni sin consumidor identificado. Payloads tienen `eventId`, tipo, versión, ocurridoEn, aggregateId y datos mínimos no sensibles.

## 25. Transacciones y consistencia

| Flujo | Límite | Consistencia |
|---|---|---|
| Stripe confirmado | Inbox procesable + Pago CONFIRMADO + Venta COMPLETADA + Beneficios + Outbox | STRONG |
| Venta efectivo | Venta + Pago confirmado + Beneficios + Outbox | STRONG |
| Reserva | Reserva + recursos + beneficio + Outbox | STRONG |
| Cancelar reserva | estado + liberaciones + reversión según política + Outbox | STRONG |
| Cancelar sesión | sesión bloqueada primero; cascada durable e idempotente a reservas | STRONG para no vender; EVENTUAL para comunicaciones/cascada |
| Invitación | Usuario + Invitación + Outbox | STRONG; entrega EVENTUAL |
| Confirmación instructor | estado/versión de sesión | STRONG; publicación derivada inmediata o convergente |
| Disponibilidad mostrada | proyección/cálculo | EVENTUAL/instantánea; comando siempre revalida STRONG |
| Email, push, recordatorio | Notificación/Intentos | EVENTUAL |

No hay llamadas a Stripe, Email o FCM dentro de esas transacciones críticas.

## 26. Concurrencia

- UNIQUE protege email, Stripe Event ID, idempotency keys acotadas y una reserva activa por `clienteId+sesionId`.
- Un conditional update/versionado protege transiciones monotónicas y evita confirmar/cancelar dos veces.
- Locks de filas `Sesion`, saldos de Beneficio y `SalonRecurso` serializan última plaza/reformer y consumo/reversión.
- Recursos múltiples se bloquean por ID ordenado para evitar deadlocks; luego se suma consumo traslapado `[inicio,fin)`.
- La doble reserva temporal del cliente requiere exclusion constraint PostgreSQL sobre rango/cliente para estados activos si la política prohíbe traslapes globales.
- El conflicto global de instructor puede usar exclusion constraint sobre rango/instructor en ocurrencias/asignaciones efectivas materializadas, o una estrategia equivalente transaccional.
- Bloques recurrentes requieren constraint/lock apropiado además de validación de aplicación; una exclusion simple debe considerar día y vigencia.
- Modificar sesión con reservas exige lock, versión y política; nunca check-then-insert como única barrera.
- Testcontainers valida las garantías PostgreSQL reales, incluidos dos requests concurrentes.

## 27. Persistencia y JPA

- Relaciones JPA son razonables dentro del módulo: Venta→Líneas, Beneficio→Movimientos, Bloque→Asignaciones, Sesión→Confirmación propia.
- Referencias cross-module se guardan como UUID (`sesionId`, `clienteId`, `ventaId`, `lineaVentaId`, `beneficioId`) y snapshots necesarios.
- No se navega Reserva→Sesion→Asignacion→Usuario→Roles ni Compra→Paquete mutable como grafo global.
- APIs internas cargan/validan agregados; proyecciones de lectura unen tablas sólo con ownership y propósito explícitos.
- Constraints, índices y locks complementan JPA; `@Version` sirve donde detecta conflicto, no sustituye locks de capacidad agregada.
- La base es compartida, pero nombres/esquemas/tablas deben permitir reconocer ownership y evitar escrituras accidentales.

## 28. Package Structure

```text
com.feelingpilates
├── identidad/                 # auth, principal, usuarios, roles, invitaciones
│   ├── api/                   # contratos internos de identidad/autorización
│   └── infraestructura/       # JWT y proveedores de identidad
├── operacion/
│   ├── salones/               # sede, horario, excepciones y políticas
│   ├── actividades/           # duración, oferta, especialidades consultables
│   └── recursos/              # catálogo, inventario y requisitos
├── programacion/
│   ├── bloque/                # recurrencia y asignaciones
│   ├── ajuste/                # cambios por fecha
│   ├── sesion/                # confirmación, ocurrencia y publicación
│   └── consulta/              # resolver efectivo
├── reservas/                  # booking, capacidad, cancelación
├── comercial/
│   ├── catalogo/              # Paquete y composición
│   └── ventas/                # Venta, Línea y checkout
├── pagos/
│   ├── aplicacion/            # estados financieros/reembolsos
│   └── infraestructura/stripe/# gateway, webhook, Inbox, reconciliación
├── beneficios/                # derechos, saldo y ledger
├── notificaciones/            # mensaje, intento y adapters de canal
├── mensajeria/outbox/         # infraestructura durable local
└── comun/                     # IDs/value objects realmente compartidos
```

No se exige `api/application/domain/infrastructure` en todos los módulos; sólo se usan capas donde separan reglas complejas o adaptadores.

## 29. Naming

- `calendario` → `programacion`; Agenda/Calendario queda como lenguaje de UI.
- `BloqueProgramacion` evita “turno” cuando no representa jornada laboral del instructor.
- `Asignacion` significa instructor+rango+una actividad; `Sesion` es la ocurrencia reservable; `Reserva` es la ocupación del cliente.
- `Venta`, `LineaVenta`, `Pago`, `Reembolso`, `BeneficioAdquirido` y `MovimientoBeneficio` expresan responsabilidades distintas.
- Se evitan prefijos redundantes (`FeelingPilatesVentaService`, `PagoPilatesController`); adaptadores conservan nombres como `StripePaymentGateway`.
- El renombre físico de packages/clases es posterior y gradual; este documento no lo implementa.

## 30. Workers

- Un deployment aloja API HTTP, reconciliación Stripe, Inbox, Outbox, delivery, confirmaciones, materialización/publicación de sesiones, expiraciones y recordatorios.
- Cada worker tiene responsabilidad, lote, lock/lease, timeout, retry, idempotencia y métricas separados aunque compartan scheduler/thread pool inicialmente.
- Jobs no procesan filas sin límite ni mantienen transacciones durante llamadas remotas.
- Fallo de email no bloquea reservas; backlog de sesión/Inbox sí puede degradar publicación/pagos y debe alertar.

## 31. Seguridad

- Corregir `GET mis reservas` para ignorar `clienteId` externo y comprobar ownership en detalle/cancelación.
- Aplicar `puedeOperarSalon` a programación, horario excepcional, inventario, ventas, historial y reembolso.
- Reembolso valida sede de la Venta/Pago y permiso contextual, incluso con IDs válidos de otra sede.
- Idempotency key se liga a actor, operación y fingerprint; una coincidencia distinta se rechaza.
- Webhook sigue público pero exige firma, límite de tamaño, Event ID UNIQUE y respuesta sin filtrar internals.
- Tokens/secretos se redactan; correlation ID no sustituye controles de acceso.
- Mantener permisos autoritativos en BD y documentar issuer/audience/rotación JWT.
- Registro público y recuperación de cuenta requieren rate limiting, enumeración segura y auditoría.

## 32. Testing

- Primero corregir el baseline 9/10 definiendo explícitamente el comportamiento del Google endpoint deshabilitado.
- Caracterización antes de refactor: horario efectivo, `[inicio,fin)`, recurrencia, vigencias, ajustes y conflicto global instructor.
- Sesiones: partición por duración, identidad estable, confirmación/deadline, materialización idempotente y cancelación.
- Reservas: grupo, doble booking, última plaza, recursos compartidos, Duo, cancelación y saldo/reversión.
- Pagos: firma webhook, Event ID, orden/repetición, máquina monotónica, idempotencia, conciliación y reembolso.
- Mensajería: atomicidad Outbox, reclaim tras caída, at-least-once y retries de Notificación.
- Seguridad: IDOR, cada scope de sede, reembolso cruzado, invitaciones/log redaction y registro público.
- PostgreSQL/Testcontainers es obligatorio para locks, exclusion constraints, Flyway y carreras; tests unitarios cubren resolvers/políticas.
- ArchUnit puede proteger dependencias de packages con bajo costo; Spring Modulith no se agrega ahora.

## 33. Observabilidad

- Propagar `correlationId` y usar logs estructurados con actorId/salonId y resultado, sin PII/secreto innecesario.
- Correlacionar `ventaId`, `pagoId`, `stripeEventId`, `sessionId`, `reservaId`, `beneficioId` y `outboxEventId`.
- Métricas: backlog/edad de Inbox y Outbox, backlog de Notificaciones, intentos/fallos y latencia de workers.
- Medir conciliaciones Stripe, deadlines vencidos, sesiones no publicadas, conflictos de capacidad, reservas rechazadas y lock contention.
- Auditoría funcional registra quién cambió horario/programación, confirmó instructor, canceló sesión/reserva o reembolsó.
- Alertas se basan en SLO/edad de backlog; no se selecciona todavía un stack externo.

## 34. Migraciones y compatibilidad

- Precondición: congelar en Git el baseline exacto y reproducible que hoy vive parcialmente como archivos no rastreados.
- Inventariar ambientes y `flyway_schema_history` antes de decidir sobre V22.1–V40 o V36.
- Flyway es forward-only: no editar una migración aplicada sin conocer todos los ambientes y checksums.
- V36 no se valida sólo en esquema vacío: requiere ensayo sobre copia representativa y estrategia explícita de preservación.
- Migraciones futuras son pequeñas: expand schema → escribir compatible → backfill verificado → cambiar lecturas → contract posterior.
- Backfills corren separados, reanudables y medibles; las tablas legadas coexisten durante doble lectura/escritura controlada.
- No se mezclan renombre masivo de packages, migración de datos y cambio funcional en una sola entrega.

## 35. Current → Target

| Actual | Target | Acción conceptual | Motivo |
|---|---|---|---|
| `calendario` | `programacion` + `reservas` | DIVIDIR RESPONSABILIDAD | mezcla reglas, ocurrencias y bookings |
| `TurnoInstructor` | `BloqueProgramacion` + ajustes | EVOLUCIONAR | mezcla bloque, recurrencia, excepción y cancelación |
| `TurnoInstructorAsignacion` | `Asignacion` con ID propio | REEMPLAZAR EVENTUALMENTE | una actividad por rango y segmentos repetibles |
| `Reserva` | reserva por `sesionId` | EVOLUCIONAR | hoy copia ocurrencia y bloquea grupo |
| `HorarioOperacion` | regla semanal vigente | EVOLUCIONAR | requiere historia/vigencia |
| `SalonHorarioExcepcion` | excepción operativa por fecha | CONSERVAR | ya expresa cerrado/horario especial |
| `TipoActividad` | catálogo operativo | CONSERVAR | duración y participantes ya existen |
| `TipoRecurso` | catálogo de recursos | CONSERVAR | generalización correcta |
| `SalonRecurso` | inventario bloqueable por sede | EVOLUCIONAR | falta concurrencia/uso operativo |
| `ActividadRecurso` | requisito por reserva | CONSERVAR | representa Reformer/Duo |
| `Compra` | adapter legado hacia Venta/Pago/Beneficio | DEPRECAR GRADUALMENTE | concentra responsabilidades |
| `PagoService` | aplicación Pagos + adapters/Inbox | DIVIDIR RESPONSABILIDAD | mezcla SDK, webhook, estado y scheduler |
| `VentaService` | casos de uso de Venta | EVOLUCIONAR | conservar caja; dejar de persistir ticket como compras sueltas |
| `Paquete` | Catálogo Comercial | CONSERVAR | no pertenece a Stripe |
| `PaqueteActividad` | composición del producto | CONSERVAR | base de beneficios por actividad |
| `EmailService` | puerto de entrega | EVOLUCIONAR | llamado por worker, no business TX |
| `EmailServiceConsola` | adapter local/dev | CONSERVAR | útil fuera de producción con redacción |

No hay borrado inmediato: cada reemplazo exige compatibilidad, backfill, comparación y retirada posterior.

## 36. Decisiones arquitectónicas cerradas

- Monolito modular, PostgreSQL compartido, un deployment inicial; sin microservicios ni broker.
- `calendario` evoluciona a `programacion`; Reservas es frontera conceptual separada.
- Sesión/ocurrencia persistente y estable pertenece a Programación/Sesiones; Reserva la referencia.
- Bloque es contenedor; asignación tiene exactamente una actividad; varios instructores/sesiones pueden coexistir.
- Conflicto de instructor es global entre salones y la capacidad de recurso es compartida por salón+intervalo.
- Catálogo → Ventas → Pagos → Beneficios son responsabilidades distintas; `Compra` migra aditivamente.
- Stripe es adapter de Pagos; entrada durable por Stripe Inbox; salida durable por Transactional Outbox.
- Notificación persistente es distinta de Outbox; workers PostgreSQL `@Scheduled` y entrega at-least-once.
- Disponibilidad se resuelve en backend y el comando de reserva revalida capacidad fuerte.

## 37. Decisiones de negocio abiertas

- **Ventas:** pagos parciales, múltiples métodos por venta, descuentos, impuestos y cancelación parcial de líneas.
- **Beneficios:** inicio de vigencia, combinación/prioridad de saldos, transferibilidad y refund cuando existen clases usadas.
- **Programación:** plazo exacto de confirmación, sustitución, sesión reservada ante indisponibilidad y cambio de instructor conservando ID.
- **Operación:** jornadas partidas, límite humano sin recurso y vigencia de inventario/capacidad fuera de servicio.
- **Reservas:** anticipación concreta por sede, cancelación/reversión, no-show, lista de espera y eventual sobrecupo.
- **Sesiones:** política cuando cambia duración/actividad con reservas y mínimo de participantes para impartir.
- **Notificaciones:** canales, recordatorios, preferencias, plantillas, marketing y consentimiento.
- **Pagos:** proveedor/flujo real de transferencia, reembolso parcial y autoridad para iniciar Stripe refunds.

## 38. Dependencias de implementación

1. Hacer reproducible el working tree y registrar un baseline Git autoritativo sin perder archivos actuales.
2. Inventariar historiales Flyway/ambientes y decidir tratamiento seguro de V36 y versiones no rastreadas.
3. Restablecer baseline verde de tests aclarando el endpoint Google deshabilitado.
4. Agregar safety net de programación, reservas, pagos y semántica PostgreSQL antes de refactors.
5. Corregir IDOR, autorización contextual por sede, logs/tokens e idempotencia cruzada.
6. Introducir contratos de módulo y luego Sesión/Programación efectiva de forma aditiva.
7. Añadir capacidad/recursos y Beneficios antes de habilitar reservas grupales cobrables.
8. Separar Venta/Pago/Beneficio e introducir Inbox/Outbox/Notificaciones por compatibilidad, no Big Bang.

Este orden es de dependencias, no un roadmap de entregas o fechas.

## 39. Primera intervención recomendada

No debe comenzar Programación ni una migración de dominio. Antes de tocar código se debe crear/fijar el baseline reproducible y completar la revisión de historiales Flyway/V36; sin esas dos condiciones no existe una PR verificable contra el sistema inspeccionado.

Cumplidas esas precondiciones, la primera intervención de código debe ser pequeña y sin cambio de esquema: corregir el IDOR de “mis reservas” para tomar el cliente del principal, aplicar ownership/scope en consulta y cancelación, y añadir tests de cliente propio, cliente ajeno, admin autorizado y sede ajena. Es reversible, verifica un riesgo crítico y crea el primer patrón reutilizable de autorización contextual. El test Google debe estar verde antes de aceptar esa PR para no ocultar regresiones.

## 40. Target Architecture Diagram

```text
 Mobile / Web / Admin
          |
          v
       API HTTP ---------------> Identidad y Acceso
          |                         | principal + scope BD
          +------------+------------+--------------------+
                       |                                 |
                       v                                 v
          Operación y Oferta                       Catálogo Comercial
      Salones / Actividades / Recursos                    |
          | horario, duración, inventario                 v
          v                                          Ventas --------+
      Programación                                      |            |
 Bloques -> Asignaciones -> Ajustes                     v            v
          |                                      Pagos <------+ Beneficios
          v                                        |              ^
 Sesiones/Confirmación -- consulta API --> Reservas+--------------+
          |                               |  cupo/recurso/saldo
          | SesionCancelada               |
          +------------- durable ---------+
                                                        Stripe
                                                          ^ |
                                     Stripe Adapter ------+ |
                                     Stripe Inbox <---------+
 Business TX de cada módulo
          |
          v
 Transactional Outbox --> Event Processor --> Notificaciones
                                                |
                                                v
                                         Delivery Workers
                                       Email / Push / otros
 API + módulos + Inbox + Outbox + workers
                       |
                       v
                  PostgreSQL compartido
              (ownership lógico por módulo)
```

Las flechas síncronas de Reserva hacia Sesiones/Operación/Beneficios protegen invariantes; las flechas durables sólo transportan efectos tolerantes a retry/eventualidad.

## 41. Áreas que NO deben tocarse aún

- No dividir deployments, crear microservicios, introducir broker ni agregar Spring Modulith.
- No renombrar packages/clases en masa ni eliminar relaciones JPA por dogma.
- No reemplazar `Compra`, `TurnoInstructor` o `Reserva` antes de coexistencia y backfill verificados.
- No editar migraciones aplicadas ni ejecutar V36 sobre datos reales sin inventario de ambientes y ensayo representativo.
- No implementar pagos parciales, waitlist, sobrecupo, marketing o proveedor push sin decisiones de negocio.
- No mover disponibilidad al móvil ni exponer reglas recurrentes como contrato cliente.
- No hacer llamadas a Stripe/email dentro de transacciones críticas ni crear eventos sin consumidor.
- No iniciar refactor de Programación hasta contar con baseline reproducible, tests de seguridad y safety net del dominio.
