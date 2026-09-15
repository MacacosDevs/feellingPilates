# FeelingPilates — PN-13 — Materialización de autoridad de Pagos y Notificaciones

Status: `MATERIALIZED / ACCEPTED / AUTHORITY_AUDIT_PASS / P0=0 / P1=0 / P2=1`

Type: `DOCUMENTATION_ONLY / NORMATIVE_AUTHORITY_MATERIALIZATION`

Implementation: `NOT_STARTED / NOT_AUTHORIZED`

## 1. Identidad, provenance y aislamiento

```text
Repository: /Users/jesusaldaircruzortiz/Desktop/Feelingpilates/feelingpilates-payments-notifications
Branch: pagos/pagos-notificaciones-r1
Frozen baseline / HEAD: a0ec85818b771d4ac924b427fa1e90244ea9fe8e

Historical PN-13 handoff (completed for authority materialization / not active):
auditoria/handoffs/HANDOFF-PN13-MATERIALIZACION-AUTORIDAD-PAGOS-NOTIFICACIONES.md
SHA-256: 601d285a23c87b131b4b4946d2f858ad918faea12e492d76f7e9da7acd073e22

Independent handoff audit evidence:
auditoria/reviews/HANDOFF-PN13-MATERIALIZACION-AUTORIDAD-PAGOS-NOTIFICACIONES-REVIEW.md
SHA-256: 26a0e67588f7a9e5bd13c79aa9006a833d63834cf3cf1a1a19467194e27df5f5

Fresh authority re-audit R1.2 evidence:
auditoria/reviews/PN13-R1.2-REVIEW-MATERIALIZACION-AUTORIDAD-PAGOS-NOTIFICACIONES.md
SHA-256: da21981cbb4d0925fc7732e645580dd22e5e369938f6b165a0e7009d0eb4510b
```

Único worktree autorizado: el repository anterior. Los worktrees
`/Users/jesusaldaircruzortiz/Desktop/Feelingpilates/feelingpilates` y
`/Users/jesusaldaircruzortiz/Desktop/Feelingpilates/feelingpilates-autopilot-r1` no fueron
inspeccionados ni modificados. La branch histórica `feature/calendario-reservas-pagos` en
`e515152671dc5b2801f4fd7ef3e1e608bfc55a0a` permanece `CONCEPTUAL_EVIDENCE_ONLY / NO_MERGE /
NO_CHERRY_PICK / NO_IMPLEMENTATION_SOURCE`.

## 2. Precedencia y ownership documental

| Fuente | Autoridad |
| --- | --- |
| `ESTADO-ACTUAL.md` | Lifecycle, handoff, gates y autorización. |
| `contexto/DOMINIO-FUNCIONAL.md` | Reglas de producto y dominio. |
| `DECISIONES-ARQUITECTONICAS.md` | Decisiones técnicas aceptadas. |
| `ARQUITECTURA-ACTUAL.md` | Verdad física implementada/legacy frente a diseñada. |
| `contexto/MAPA-LEGACY-Y-MIGRACION.md` | REUSE/REWORK/RETIRE, coexistencia, migración y cutover. |
| Este checkpoint | Contrato PN-13 detallado y trazabilidad A–J. |
| Handoff | Scope/allowlist/prohibiciones; no reglas de producto. |
| Review | Evidencia del gate; nunca autoridad normativa u operacional. |
| Código/migraciones actuales | Evidencia legacy/evolutiva; no autoridad del diseño objetivo. |

Ante contradicción se falla cerrado. No se completa una regla desde preferencia personal, chat o
código legacy.

## 3. Resumen de diseño materializado

El catálogo ofrece clase individual o paquete indivisible con componentes actividad/cantidad. La
compra congela términos comerciales. `OrdenVenta`, `Compra`, `Pago` y `Acreditacion` se separan;
todos los pagos confirmados convergen en una acreditación durable. Los derechos son lotes por
actividad y su saldo se deriva de movimientos inmutables. Cada reserva posee un compromiso exacto.
Stripe entra por Inbox durable; los hechos salen por Outbox. Reembolso monetario, restauración de
crédito y disputa son procesos distintos. Reservas conserva scheduling; Pagos conserva crédito.

El detalle funcional vinculante está en `DOMINIO-FUNCIONAL.md` §13 y las decisiones técnicas en
DA-014–DA-022. Nada de este diseño está implementado por PN-13.

## 4. Modelo de dominio y responsabilidades finales

| Nombre final | Identidad/cardinalidad | Responsabilidad | Responsabilidad prohibida |
| --- | --- | --- | --- |
| `OrdenVenta` | UUID; 1 cliente, 1..N compras, 0..N pagos | Ticket/checkout, total/moneda, settlement único. | No contiene saldo ni confirma proveedor por sí sola. |
| `Compra` | UUID; N:1 orden | Producto adquirido y snapshot raíz. | No es intento de pago ni catálogo mutable. |
| `CompraComponenteSnapshot` | UUID; N:1 compra | Actividad, cantidad y términos congelados. | No lee composición actual del catálogo. |
| `Pago` | UUID; N:1 orden | Método, importe y máquina monetaria; referencia externa. | No crea derechos directamente. |
| `Acreditacion` | UUID; 1:1 pago efectivo | Aplicación idempotente de compra(s) a derechos. | No redefine precio/política. |
| `DerechoActividad` | UUID; N:1 componente | Lote por actividad/provenance y proyección de saldo/vigencia. | No admite edición manual arbitraria. |
| `MovimientoCredito` | UUID; N:1 derecho | Hecho inmutable de cantidad, causa e idempotencia. | No se actualiza/elimina para corregir historia. |
| `MovimientoVigencia` | UUID; N:1 derecho | Hecho inmutable de vigencia anterior/nueva y causa. | No acredita cantidades. |
| `CompromisoReserva` | UUID; reserva única + derecho | Compromiso exacto y lifecycle de una unidad. | No posee horario/capacidad/estado operacional. |
| `Reembolso` | UUID; N:1 pago/orden, alcance de compra(s) | Lifecycle monetario y bloqueo/compensación. | No borra consumo histórico. |
| `EventoStripeRecibido` | UUID interno + `event.id` único | Inbox, firma verificada, payload, procesamiento/recovery. | No es evento de dominio ni notificación. |
| `EventoDominio` | identidad causal | Hecho interno posterior a mutación de dominio. | No implica entrega externa. |
| `MensajeOutbox` | UUID + clave dedupe | Transporte durable de hecho ya committed. | No envía dentro de transacción fuente. |
| `Notificacion` | UUID + clave lógica | Mensaje lógico, audiencia, template/agenda/preferencia. | No representa entrega ni intento de proveedor. |
| `EntregaLogica` | UUID + notificación/canal/endpoint | Una entrega estable por canal/endpoint; push por dispositivo. | No se recicla como intento. |
| `IntentoEntrega` | UUID + entrega/secuencia | Outcome inmutable de una ejecución numerada. | No altera el hecho de negocio ni se reabre. |

Tipos complementarios: `DispositivoPush`, `PoliticaComercialVersion` y
`UsoCancelacionReembolsable`. `ProductoComercial` y `ProductoComponente` son el catálogo mutable;
no sustituyen snapshots.

## 5. Ledger, vigencia y reservas

### 5.1 Invariantes contables

Por derecho, los seis buckets separados `D disponible`, `K comprometido`, `B contención
financiera`, `C consumido`, `E expirado` y `R revocado`, junto con emitido `G`, son enteros no
negativos y cumplen siempre `G=D+K+B+C+E+R`. La tabla completa de vectores, recovery y compensación
está en §18.1 y exige que expiración y revocación permanezcan como resultados contables distintos.
La proyección se actualiza en la misma transacción que el movimiento y se verifica contra el fold
íntegro del ledger. Corrección = movimiento compensatorio, nunca `UPDATE` histórico.

Categorías finales: `ACREDITACION_COMPRA`, `COMPROMISO_RESERVA`,
`LIBERACION_CANCELACION_CLIENTE_VALIDA`, `RESTAURACION_CANCELACION_ESTUDIO`,
`CONSUMO_ASISTENCIA`, `CONSUMO_NO_ASISTIDA`, `CONSUMO_CANCELACION_TARDIA`,
`CONSUMO_LIMITE_CANCELACIONES`, `EXPIRACION`, `ACREDITACION_RECUPERACION`,
`REVOCACION_REEMBOLSO` y `COMPENSACION_ADMINISTRATIVA`.

Cada movimiento declara `tipo`, cantidad con signo/semántica inequívoca, derecho, compra,
reserva/causa cuando aplique, actor, instante, operación idempotente y hash de payload. FEFO elige
el derecho elegible con menor expiración y desempata por UUID estable.

### 5.2 Vigencia

Duración versionada usa `DIAS | MESES`; meses son calendario. Alcance de extensión usa
`MISMA_ACTIVIDAD | TODAS_LAS_ACTIVIDADES_ACTIVAS`. Sólo derechos con disponible > 0 se extienden;
una extensión no acredita cantidades ajenas, no acorta vigencia y no revive expirados. La reserva
usa el contrato único de compromiso dentro de vigencia más `limitePostVencimientoDias=N`
configurable para el inicio de la sesión.

La elegibilidad se evalúa siempre con `Clock` backend y zona de negocio, aun si el job de
expiración se retrasa. Un compromiso válido sobrevive expiración. Su cancelación restaurable
posterior genera, idempotentemente, derecho de recuperación de la misma actividad y vigencia
versionada, sin revivir el lote viejo ni disparar extensión por compra.

`fechaVencimiento` es el último día calendario válido incluido. Todo compromiso nuevo debe ocurrir
antes de `inicioDelDia(fechaVencimiento + 1)` en `America/Mexico_City`; después queda prohibido.
Un compromiso válido conserva su derecho y la sesión puede iniciar hasta
`limitePostVencimientoDias=N` días calendario posteriores, inclusive, con límite exclusivo
`inicioDelDia(fechaVencimiento + N + 1)` en la misma zona.

### 5.3 Puerto con Reservas

`GestorCreditoReserva` expone:

```text
verificarElegibilidad(clienteId, actividadId, inicioClase)
comprometer(reservaId, clienteId, actividadId, inicioClase, idempotencia)
liberarPorCancelacionValida(reservaId, instante, idempotencia)
consumirPorAsistencia(reservaId, idempotencia)
consumirPorNoAsistencia(reservaId, idempotencia)
consumirPorCancelacionTardia(reservaId, instante, idempotencia)
restaurarPorCancelacionEstudio(reservaId, causa, idempotencia)
```

Reservas decide horario, sesión, plaza, actor y alcance operacional. Pagos decide derecho,
allowance y movimiento. Crear reserva+comprometer, cancelar+liberar/consumir y resolver
asistencia+consumir son transacciones locales únicas. Cancelación cliente siempre libera plaza;
el corte exacto es inclusivo (`cancelacion <= inicio - horas`). Allowance MVP = cliente + mes
calendario de zona de negocio + versión de política, serializado. Cancelación de estudio restaura
sin allowance. No hay timer que marque automáticamente `NO_ASISTIDA`.

## 6. Máquinas de estado

Toda transición exige estado esperado, idempotencia y evidencia causal. Una solicitud repetida con
misma identidad/payload devuelve el resultado anterior; payload contradictorio falla cerrado.

### `OrdenVenta`

```text
BORRADOR -> PENDIENTE_PAGO | CANCELADA
PENDIENTE_PAGO -> PAGADA | CANCELADA | REQUIERE_REVISION
PAGADA -> REQUIERE_REVISION (sólo contradicción; no regresión monetaria)
```

`PAGADA` es terminal normal para settlement. Segundo pago confirmado =>
excepción financiera/`REQUIERE_REVISION`, nunca segundo settlement ni doble crédito. Se preservan
el settlement y sus derechos válidos; el `Pago` anómalo se concilia y reembolsa bajo scope propio.
`CANCELADA -> PAGADA` está prohibido salvo una reconciliación explícita que pruebe el primer y único
settlement externo y deje auditoría/revisión.

### `Compra`

```text
PENDIENTE_PAGO -> PAGADA -> PENDIENTE_ACREDITACION -> ACREDITADA
ACREDITADA -> BLOQUEADA_REEMBOLSO -> REEMBOLSADA
BLOQUEADA_REEMBOLSO -> ACREDITADA (refund fallido, compensación auditable)
cualquier estado no terminal -> REQUIERE_REVISION
```

`REEMBOLSADA` es terminal. `PAGADA` nunca concede derechos por sí sola. No se permite
`ACREDITADA -> PENDIENTE_ACREDITACION`.

### `Pago`

```text
CREADO -> REQUIERE_ACCION | PROCESANDO | CONFIRMADO | CANCELADO
REQUIERE_ACCION -> PROCESANDO | FALLIDO_REINTENTABLE | CONFIRMADO | CANCELADO
PROCESANDO -> REQUIERE_ACCION | FALLIDO_REINTENTABLE | CONFIRMADO | CANCELADO
FALLIDO_REINTENTABLE -> REQUIERE_ACCION | PROCESANDO | CONFIRMADO | CANCELADO
cualquier no terminal -> REQUIERE_REVISION
```

`CONFIRMADO` y `CANCELADO` son terminales normales. Un evento anterior no regresa
`CONFIRMADO`. `payment_intent.payment_failed` produce fallo/reintento del intento, no fallo terminal
de la orden. Efectivo sólo llega a `CONFIRMADO` tras recepción; transferencia usa subestado
`PENDIENTE_VALIDACION -> CONFIRMADA | RECHAZADA`; reapertura de rechazada exige permiso, causa y
nuevo evento auditable.

### `Acreditacion`

```text
PENDIENTE -> APLICANDO -> COMPLETADA
APLICANDO -> FALLIDA_REINTENTABLE | REQUIERE_REVISION
FALLIDA_REINTENTABLE -> APLICANDO
```

`COMPLETADA` es terminal/idempotente y supersede la denominación previa `APLICADA`. El claim
`APLICANDO` tiene lease recuperable; dos workers no
pueden aplicar la misma acreditación por unicidad/lock.

### `DerechoActividad`

```text
ACTIVO -> AGOTADO | EXPIRADO | BLOQUEADO | REVOCADO
BLOQUEADO -> ACTIVO | REVOCADO | REQUIERE_REVISION
```

Estado y elegibilidad derivan de ledger, proyección y tiempo. Expirado/agotado no reviven por
compra nueva. Reactivación excepcional crea movimientos y estado explícitos, nunca regresión
silenciosa.

### `CompromisoReserva`

```text
COMPROMETIDO -> LIBERADO | CONSUMIDO | RECUPERADO
```

Estados terminales e idempotentes. `RECUPERADO` significa que la restauración se materializó en un
nuevo derecho de recuperación; no revive el original. No se permite más de un terminal ni más de
un compromiso activo por reserva.

### `Reembolso`

```text
SOLICITADO -> APROBADO | RECHAZADO | CANCELADO
APROBADO -> ENVIADO | REQUIERE_REVISION
ENVIADO -> PENDIENTE_CONFIRMACION | CONFIRMADO | FALLIDO | REQUIERE_REVISION
PENDIENTE_CONFIRMACION -> CONFIRMADO | FALLIDO | REQUIERE_REVISION
```

`RECHAZADO`, `CANCELADO` y `CONFIRMADO` son terminales. `FALLIDO` sólo permite nueva operación
explícita o compensación; no simula éxito. Durante outcome incierto los derechos siguen bloqueados.

### `EventoStripeRecibido`

```text
RECIBIDO -> PENDIENTE -> PROCESANDO -> PROCESADO | IGNORADO
PROCESANDO -> REINTENTO | REQUIERE_REVISION
REINTENTO -> PROCESANDO
```

`PROCESADO/IGNORADO` son terminales por `event.id`. Firma inválida no crea inbox ni mutación
financiera. Evento válido no correlacionable queda en revisión, no ignorado.

### `Notificacion`, `EntregaLogica` e `IntentoEntrega`

```text
Notificacion: PROGRAMADA -> PENDIENTE -> EN_PROCESO -> ENTREGADA |
PARCIALMENTE_ENTREGADA | FALLA_PERMANENTE | SIN_CANALES_DISPONIBLES | REQUIERE_REVISION

EntregaLogica: PENDIENTE -> EN_PROCESO -> ENTREGADA | FALLA_PERMANENTE | REQUIERE_REVISION

IntentoEntrega inmutable: ENVIANDO -> ENTREGADO | FALLO_TRANSITORIO |
FALLO_PERMANENTE | OUTCOME_INCIERTO
```

Estados terminales no regresan. Un retry crea un nuevo `IntentoEntrega` con secuencia siguiente;
nunca devuelve ni recicla una fila a `PENDIENTE`. Un outcome incierto que no puede reintentarse sin
riesgo de duplicación exige revisión y nunca se proclama exactly-once.

## 7. Fronteras transaccionales y concurrencia

Transacciones obligatorias:

1. confirmar pago + crear durablemente acreditación pendiente;
2. aplicar acreditación + compras + derechos + movimientos + evento/outbox;
3. reserva + compromiso;
4. cancelación + liberación/consumo + allowance;
5. asistencia/no-show + consumo;
6. aprobación/bloqueo de refund y compensación posterior;
7. cualquier hecho fuente + Outbox.

Email/push y llamadas Stripe nunca se mantienen dentro de la transacción de negocio. Una llamada
externa usa identidad persistida; su outcome se aplica en otra transacción reconciliable.

Estrategia MVP: PostgreSQL `READ COMMITTED`, constraints como defensa final, `PESSIMISTIC_WRITE`
en agregados contenciosos, selección ordenada; `SKIP LOCKED`/claim condicional se reserva a workers
y está prohibido en la selección FEFO foreground de una reserva. Orden
de locks: orden/compra, pago/reembolso, derechos `(expira_en,id)`, allowance. La misma regla alcanza
a todos los writers.

## 8. Contrato físico propuesto

### 8.1 Packages

```text
com.feelingpilates.pagos.catalogo.{dominio,aplicacion,infraestructura}
com.feelingpilates.pagos.ventas.{dominio,aplicacion,infraestructura}
com.feelingpilates.pagos.derechos.{dominio,aplicacion,infraestructura}
com.feelingpilates.pagos.reembolsos.{dominio,aplicacion,infraestructura}
com.feelingpilates.pagos.stripe.{aplicacion,infraestructura}
com.feelingpilates.notificaciones.{dominio,aplicacion,infraestructura}
```

Dominio no importa Spring/JPA/Stripe; aplicación orquesta; infraestructura adapta. Pagos referencia
`reserva_id`, pero no define una segunda `Reserva`.

### 8.2 Tablas, identidades y constraints

| Tabla propuesta | Datos/relaciones críticas | Constraints/índices críticos |
| --- | --- | --- |
| `orden_venta` | UUID, `cliente_id NOT NULL`, moneda, total, estado, `pago_settlement_id`, timestamps/version. | total >= 0; unique `(id,cliente_id)` y settlement pointer; lock primero. |
| `compra` evolucionada | UUID, orden/cliente, producto fuente nullable histórico, snapshot raíz, estado. | orden+numero_linea único; FK compuesta orden/cliente; no cascade destructivo. |
| `compra_componente_snapshot` | compra FK, actividad FK, nombre snapshot, cantidad, política version. | compra+numero único; cantidad > 0; inmutable. |
| `pago` | orden FK, método, importe/moneda, estado, referencia externa. | importe > 0; referencia proveedor única; FK compuesta desde `orden_venta.pago_settlement_id`; un settlement. |
| `transferencia_pago` | pago 1:1, estado, ciclo, evidencia vigente, versión. | pago único; ciclo no negativo; estado esperado. |
| `evidencia_transferencia` | UUID, pago, actor/instante, ref bancaria, importe/moneda/fecha, ubicación/hash. | pago+hash único; payload inmutable; reuse cross-pago rechazado. |
| `intento_validacion_transferencia` | UUID, pago/ciclo/evidencia, decisión, actor/permiso/razón/instante/idempotencia. | operación+pago+ciclo+clave único; payload hash; append-only. |
| `idempotencia_operacion_pago` | operación, contexto, clave, hash payload, resultado. | único operación+contexto+clave; hash contradictorio rechazado. |
| `acreditacion` | pago FK, estado, lease, intentos, error. | pago único; identidad causal única. |
| `derecho_actividad` | compra/cliente/componente/actividad, G/D/K/B/C/E/R, inicio/límite exclusivo/zona/política, estado. | FK compuesta compra/cliente; checks no negativos y conservación; FEFO parcial cliente+actividad+end+id sobre D>0. |
| `movimiento_credito` | derecho FK, tipo, cantidad, causa/reserva, actor, payload hash. | identidad causal única; append-only; cantidad no cero. |
| `movimiento_vigencia` | derecho FK, anterior/nueva, tipo/causa/actor. | identidad causal única; append-only; rango coherente. |
| `compromiso_reserva` | reserva UUID, derecho FK, estado, instantes. | reserva única; un terminal; FK derecho. |
| `uso_cancelacion_reembolsable` | cliente, `periodo_mes` first-day de inicio de sesión, política, contador/proyección. | check day=1; único cliente+periodo+versión; contador entre 0 y límite. |
| `reembolso` / `reembolso_compra` | pago/orden; scope `NORMAL_PRODUCTO_ENTERO | PAGO_ANOMALO | EXTERNO_DASHBOARD`; 1..N Compras completas sólo cuando el scope las deriva; importe/moneda, estado, ref externa. | pago de origen obligatorio; identidad causal; whole-product para normal; referencia Stripe única; el anómalo es payment-scoped. |
| `disputa` / scopes | cargo externo/pago disputado, Compras y derechos atribuibles a ese settlement, estado; contención común por origen. | stripe_dispute_id único; unicidad parcial de owner activo; scopes normalizados; ambigüedad a revisión. |
| `evento_stripe_recibido` | event id/type, raw payload protegido, versión, estado, intentos/lease. | `stripe_event_id` único; índices estado/próximo intento. |
| `mensaje_outbox` | aggregate, evento, payload/version, estado/agenda. | clave causal única; índices claim/próximo intento. |
| `notificacion` | tipo, destinatario lógico, template/version, agenda/version, estado. | clave dedupe lógica única; índices agenda/estado. |
| `entrega_logica` | notificación, canal, endpoint estable/dispositivo, estado, lease. | única por notificación+canal+endpoint; dedupe independiente. |
| `intento_entrega` | entrega lógica, secuencia desde 1, outcome inmutable. | unique entrega+secuencia; secuencia nunca reutilizada; no secreto en diagnóstico. |
| `dispositivo_push` | cliente, instalación/dispositivo, token cifrado/protegido, estado. | token/fingerprint único; índice cliente/activo. |
| `politica_comercial_version` | tipo, versión, vigencia de configuración, JSON/campos validados. | tipo+versión único; inmutable tras uso; checks por tipo. |

Las FK financieras usan restricción, no borrado en cascada. Ledger, snapshots, inbox y outbox son
append-only o de payload inmutable; sólo cambian metadata de procesamiento permitida. Importes usan
unidad mínima entera y moneda ISO normalizada.

### 8.3 Flyway

No renombrar/renumerar V22.1–V35 ni migraciones posteriores. Antes de implementar, obtener de nuevo
la versión máxima y asignar la siguiente disponible; ninguna versión queda reservada aquí. Secuencia:

1. expand aditivo de tablas/constraints nullable seguros;
2. backfill determinista de orden/snapshots/pagos con provenance y reporte de ambigüedad;
3. validar conteos, importes, relaciones e invariantes;
4. escritura compatible/dark launch sin doble autoridad;
5. migrar readers y writers con fence autorizado;
6. cutover explícito;
7. contract/retire posterior con cero consumers probado.

Rollback previo al cutover desactiva path nuevo. Posterior: corrección forward/compensatoria; nunca
borrar historia.

## 9. Contrato API y permisos

Inventario/clasificación:

| API actual | Decisión |
| --- | --- |
| `GET /api/publico/paquetes` | PRESERVAR y evolucionar aditivamente al catálogo objetivo. |
| `POST /api/pagos/paquetes/{id}/intento` | PRESERVAR ruta inicialmente; delegar a orden/pago e idempotencia fuerte. |
| `GET /api/pagos/mis-compras` | EVOLUCIONAR compatible sobre snapshots/estados separados. |
| `GET /api/pagos/mis-paquetes` | DEPRECAR; reemplazar por consulta de derechos/saldos reales versionada. |
| `POST /api/pagos/webhook` | PRESERVAR path; REWORK para firma + Inbox antes de proceso. |
| `/api/ventas` y `/api/ventas/carrito` | EVOLUCIONAR a orden; transferencia ya no confirma al registrar. |
| consultas `/api/ventas*` | PRESERVAR temporalmente con DTOs aditivos. |
| `PATCH /api/ventas/{id}/reembolsar` | DEPRECAR/REEMPLAZAR por lifecycle `Reembolso`; no simple cambio de estado. |
| `/api/ventas/servicios*` | PRESERVAR/evolucionar catálogo y políticas. |
| `/api/reservas*` | PRESERVAR; integración de crédito interna y DTOs compatibles/versionados. |

Nuevos contratos separan: crear orden, crear/reintentar pago, registrar efectivo recibido,
registrar transferencia, validar/rechazar/reabrir transferencia, consultar acreditación/derechos y
solicitar/consultar reembolso. DTOs usan nombres españoles, importes/moneda explícitos, estado y
clave idempotente. Cambios incompatibles requieren versión/ruta nueva.

Permisos distintos mínimos: `pago.efectivo.confirmar`, `pago.transferencia.registrar`,
`pago.transferencia.validar`, `pago.transferencia.reabrir`, `pago.reembolso.solicitar`,
`pago.reembolso.aprobar`, `pago.correccion.administrativa` y lectura operacional correspondiente.

## 10. Stripe, reembolso, disputas y recovery

PaymentSheet/PaymentIntent son MVP. Servidor fija importe/moneda/metadata. Firma inválida produce
cero mutación. Inbox deduplica event ID y dominio deduplica efecto. Se contemplan success, failed,
processing y canceled; estados fuera de orden son monotónicos. Ambigüedad consulta el objeto actual.
Las idempotency keys enviadas a Stripe derivan de identidades internas estables. La idempotencia
del proveedor es defensa secundaria y nunca sustituye la identidad durable de dominio.

Refund usa `Reembolso` + Stripe Refund y sincroniza, como mínimo, `refund.created`,
`refund.updated` y `refund.failed`, además del evento equivalente que Stripe exija para reconocer
un refund externo aplicable. Derechos permanecen bloqueados en outcome incierto. Dashboard refund
se concilia.
Disputa se detecta/persiste, bloquea remanente elegible y alerta; evidencia completa post-MVP.

Recovery determinista:

| Escenario | Respuesta obligatoria |
| --- | --- |
| Timeout al crear PaymentIntent | Reintentar con identidad estable; recuperar/buscar el mismo objeto, no crear otro. |
| App cerrada tras pagar / webhook antes del HTTP | Webhook/Inbox confirma; cliente consulta estado; orden idempotente. |
| Webhook duplicado o eventos equivalentes | `event.id` + identidad de efecto evitan doble settlement/acreditación. |
| Evento fuera de orden | Ignorar regresión; reconciliar contradicción. |
| Backend caído tras éxito Stripe | Reentrega Inbox/reconciliador recupera y crea acreditación. |
| Inbox committed y worker cae | Lease/retry retoma desde estado durable. |
| Pago confirmado, acreditación falla | Mantener `PENDIENTE/FALLIDA_REINTENTABLE`; no comunicar acreditada. |
| Crash después de acreditar / dos workers | Transacción atómica + unicidad devuelve resultado aplicado. |
| Dos reservas por último crédito | Locks FEFO; sólo una confirma, otra falla sin reserva huérfana. |
| Cancelación duplicada | Compromiso/causa únicos; una sola liberación/consumo. |
| Carrera de allowance | Fila cliente+periodo bloqueada/única. |
| Job de expiración retrasado | Elegibilidad temporal fail-closed en lectura; job sólo materializa ledger/proyección. |
| Dos extensiones concurrentes | Locks de derechos ordenados + causa única. |
| Settlement Stripe vs timeout/cancelación | Aplicar exactamente §18.5: verdad monetaria, branch técnica/comercial/segundo settlement y acreditación única. |
| Transferencia confirmada dos veces | Identidad causal y estado esperado: replay del mismo resultado, un settlement/acreditación. |
| Transferencia tras otro método | `RECHAZADA`/contenida con `ORDEN_YA_LIQUIDADA`; incidente financiero; no `CONFIRMADA`, settlement, acreditación ni derechos. Si existe dinero externo, conciliar/reembolsar el `Pago` anómalo. |
| Reabrir transferencia rechazada | Permiso, causa y transición auditada; nunca editar historia. |
| Efectivo duplicado | Clave operación/contexto/payload + lock de orden. |
| Timeout de refund / éxito con backend caído | Estado incierto+bloqueo; Inbox/reconciliación completa. |
| Refund fallido | Compensación auditable desbloquea; no borrar solicitud. |
| Refund Dashboard | Correlacionar al `Pago` originario, derivar Compra(s)/derechos exactos y contener sólo ese origen; ambigüedad a revisión. |
| Disputa o conflicto refund/disputa | Identificar cargo/Pago, serializar por el mismo origen y contener sólo derechos atribuibles; ambigüedad a revisión. |
| Cancelación masiva estudio | Una causa por lote y operación idempotente por reserva; restauración atómica individual. |
| Timeout proveedor tras posible entrega | `OUTCOME_INCIERTO`, dedupe del proveedor si existe y retry finito estable. |

Monto/moneda inconsistentes, asociación PaymentIntent errónea, segundo settlement, webhook no
correlacionable, transición imposible o ledger inválido => `REQUIERE_REVISION` + alerta; nunca
transición inferida.

## 11. Notificaciones

Outbox nace con el hecho y el dispatcher opera después del commit. Notificación y entrega tienen
dedupe distinto. Fan-out por email/push/dispositivos es permitido. Retry usa backoff configurable,
jitter cuando corresponda y máximo finito. Fallo permanente queda consultable/alertable. Adapter
de proveedor queda detrás de puertos y no filtra PII/tokens a logs.

Jobs programados conservan versión de reserva/vigencia/política/template. Antes de enviar:

- recordatorio: reserva aún activa y versión de agenda vigente;
- expiración: derecho aún elegible, disponible > 0 y fecha no extendida;
- cancelación estudio: restauración de crédito ya committed;
- compra: `Acreditacion.COMPLETADA`, no sólo `Pago.CONFIRMADO`.

Promocional usa consentimiento/preferencias propios; transaccional usa preferencias por canal sin
ser bloqueado por opt-out promocional. MVP y exclusiones son los de `DOMINIO-FUNCIONAL.md` §13.8–13.9.

## 12. Matriz legacy resumida

- **REUSE + REWORK:** `PaqueteActividad`, catálogo/controllers, `StripeConfig`, `PaqueteRepository`,
  `EmailService`, `Reserva`, `ReservaRepository`, `ReservaController`.
- **REWORK:** `Paquete`, `Compra`, `PagoService`, `VentaService`, `PagoController`,
  `VentaController`, `ReservaService`.
- **RETIRE/REPLACE tras cutover:** `CompraRepository` mezclado, `EmailServiceConsola` como proveedor,
  refund por cambio de estado y webhook con mutación compleja directa.
- V22.1–V35 se preservan inmutables y alimentan backfill; nunca se renombran.

La matriz exhaustiva, coexistencia y condiciones están en `MAPA-LEGACY-Y-MIGRACION.md`.

## 13. Slices de implementación futuros

Cada slice requiere handoff/allowlist, preflight, tests y audit independientes. PN-13 no los
autoriza.

| # | Slice | Entrada | Salida auditable |
| --- | --- | --- | --- |
| 1 | Safety net/caracterización | Baseline y APIs inventariados. | Tests legacy de pagos/caja/Stripe/reserva y contratos API sin refactor destructivo. |
| 2 | Orden + snapshot | Slice 1 PASS; Flyway máximo revalidado. | Tablas/tipos aditivos, backfill determinista y lecturas históricas independientes del catálogo. |
| 3 | Derechos + ledger | Snapshots confiables. | Invariantes, FEFO, vigencia y movimientos con PostgreSQL/concurrencia probados. |
| 4 | Pago interno + efectivo/transferencia | Orden/ledger estables. | Máquina monotónica, permisos separados y acreditación común idempotente. |
| 5 | Stripe Inbox | Pago interno estable. | PaymentIntent adapter, raw signature, Inbox, dedupe/reconciliación/recovery. |
| 6 | Boundary Reserva-crédito | Ledger estable y contrato Reserva autorizado. | Crear+comprometer atómico, rollback e idempotencia/último crédito probados. |
| 7 | Cancelación/asistencia/expiración | Boundary activo en dark launch. | Allowance, consumo, estudio, recovery y jobs fail-closed. |
| 8 | Reembolso | Ledger y settlement confiables. | Producto entero, bloqueo, Stripe Refund, compensación y conciliación externa. |
| 9 | Outbox | Eventos fuente estables. | Escritura atómica, dispatcher, dedupe y recovery. |
| 10 | Email/push | Outbox auditado; proveedor decidido. | Notificación, fan-out, dispositivos, retry y scheduled revalidation. |
| 11 | Disputas/alertas | Inbox/Outbox operativos. | Contención, reconciliación y alertas internas auditadas. |
| 12 | Cleanup/cutover | Todos los anteriores + consumers/data audit. | Fence único, migración de APIs, retiro separado; autoridad actualizada sólo tras gate. |

No se mezclan reescrituras ajenas; fallar exit criteria bloquea el slice siguiente.

## 14. Safety net y gates técnicos futuros

Antes de cambio destructivo son obligatorios tests de caracterización de: creación/reuso de
PaymentIntent, firma webhook, transiciones actuales, caja/carrito, catálogo/snapshots, historial,
refund legacy, expiración actual y crear/cancelar reserva. Categorías posteriores:

- unitarias de máquinas, políticas, FEFO y cálculo calendario;
- integración repository/PostgreSQL con constraints reales;
- transacción/concurrencia (último crédito, allowance, settlement, extensión, refund);
- adapter Stripe/webhook, firma, eventos duplicados/fuera de orden y reconciliación;
- idempotencia y payload contradictorio;
- crash/retry/recovery e Inbox/Outbox;
- migración/backfill/compatibilidad API;
- integración reserva-crédito y rollback;
- reembolso/disputa;
- notificación dedupe, retry, outcome incierto y revalidación scheduled.

Cada slice exige scope gate y audit técnico fresh; tests verdes no sustituyen arquitectura. Host
validation aplica cuando PostgreSQL/Testcontainers/proveedores simulados lo requieran. No se
ejecutaron ni escribieron tests en PN-13.

## 15. Cierre A–J

| Obligación | Resolución materializada |
| --- | --- |
| A | Snapshot inmutable y catálogo histórico independiente: §§3–5, 8.2. |
| B | Identidad, cardinalidad, ownership y máquinas separadas: §§4, 6. |
| C | Efectivo/transferencia, permisos y pipeline común: §§6, 9. |
| D | Stripe Inbox, idempotencia, monotonicidad y recovery: §§6, 10. |
| E | Unidades calendario, Clock, scopes, expiración y recovery: §5.2. |
| F | Boundary Reserva-crédito, FEFO, atomicidad y locks: §§5.3, 7. |
| G | Corte, allowance, late/no-show/estudio y recovery: §§5.2–5.3. |
| H | Lifecycle refund, producto entero, bloqueo, disputa/revisión: §§6, 10. |
| I | Outbox, notificación/entrega, retries, privacidad y scheduled: §11. |
| J | Packages, tablas/constraints, APIs, Flyway, slices y safety net: §§8–14. |

## 16. Scope MVP y estado de aceptación

MVP y exclusiones vinculantes quedan materializados en `DOMINIO-FUNCIONAL.md` §13.9 y resumidos en
§§3, 10 y 11 de este checkpoint. El audit PN-13.1 posterior identificó diez P1 de precisión. La
primera corrección fue auditada después con `FAIL / P0=0 / P1=5 / P2=1`; la corrección residual
autorizada se materializó en §18 sin ampliar producto. El re-audit fresh R1.2 cerró todos los P1,
reportó un único P2 editorial no bloqueante e independiente de implementación y pasó el gate de
autoridad. La transición canónica competente acepta PN-13 sin autorizar implementación.

```text
Historical PN-13.1: FAIL / P0=0 / P1=10 / P2=0
Residual audit: FAIL / P0=0 / P1=5 / P2=1
Fresh authority re-audit R1.2: PASS / P0=0 / P1=0 / P2=1
R1.2 evidence: auditoria/reviews/PN13-R1.2-REVIEW-MATERIALIZACION-AUTORIDAD-PAGOS-NOTIFICACIONES.md
R1.2 evidence SHA-256: da21981cbb4d0925fc7732e645580dd22e5e369938f6b165a0e7009d0eb4510b
Authority correction: ACCEPTED
Findings PN13-001..010: CLOSED
Residual findings NEW-PN13-011..016: CLOSED
NEW-PN13-017: OPEN / P2 / EDITORIAL / NON_BLOCKING / IMPLEMENTATION_INDEPENDENT
Requires human decision: NO
```

El review R1.2 conserva la evidencia del audit independiente; este checkpoint registra la
transición canónica posterior sin convertir el review en autoridad normativa u operacional.

## 17. Resultado de audit y lifecycle

El `DOCUMENT_AUDITOR` fresh, independiente y read-only R1.2 contrastó:

- branch/HEAD/staging y delta exacto;
- hashes del handoff y su review preservados;
- sólo seis outputs PN-13, más los dos deltas bootstrap preexistentes;
- coherencia entre los cinco canónicos y este checkpoint;
- cobertura A–J, nombres, estados, transacciones, constraints, API, migración, safety net y scope;
- aislamiento F2E y ausencia de fuente/tests/migración/config;
- `P0=0`, `P1=0`, scope gate y documentation gate antes de esta aceptación.

El resultado quedó persistido en el review R1.2 exacto identificado en §1. El P2
`NEW-PN13-017` permanece abierto como editorial, no bloqueante e independiente de implementación;
no se corrige en esta transición. Publication y publication closure siguen pendientes. La
declaración exacta es:

```text
PN-13: MATERIALIZED / ACCEPTED
CORRECTED AUTHORITY: ACCEPTED
AUTHORITY AUDIT R1.2: PASS / P0=0 / P1=0 / P2=1
DOCUMENTATION GATE: PASS
PUBLICATION GATE: PENDING
PUBLICATION CLOSURE GATE: PENDING
PN-14: NOT_AUTHORIZED
IMPLEMENTATION: NOT_STARTED / NOT_AUTHORIZED
IMPLEMENTATION GATE: NOT_APPLICABLE / NOT_AUTHORIZED
TESTS GATE: NOT_APPLICABLE
HOST VALIDATION: NOT_APPLICABLE
RUNTIME / PRODUCTIVE AUTHORITY / MIGRATION / CUTOVER / F2E: UNCHANGED
```

**La implementación permanece no autorizada.**

## 18. Corrección de autoridad posterior a PN-13.1

Esta sección materializa la corrección autorizada de `PN13-001..010` y la corrección residual
autorizada de `NEW-PN13-011..016`. Supersede, para esos hallazgos, cualquier frase anterior de este
checkpoint que sea menos precisa —incluidas una proyección que no separe `expirado` de `revocado`,
`Acreditacion.APLICADA`, el settlement por “guard/índice compatible”, la transferencia como mero
subestado, el retry que reabre un `IntentoEntrega`, la condición opcional o ventana temporal del
refund normal y cualquier uso foreground de `SKIP LOCKED`—. No reescribe el review histórico, no
declara hallazgos cerrados y requiere un audit fresh independiente.

### 18.1 PN13-001 — ledger conservativo y recovery

Owner: Payments/Derechos; `MovimientoCredito` es append-only y la proyección se actualiza en la
misma transacción. Para cada `DerechoActividad`, `G,D,K,B,C,E,R` son enteros no negativos: `D`
disponible elegible, `K` comprometido por compromiso activo, `B` en contención financiera,
`C` consumido/penalizado, `E` expirado, `R` revocado y `G` emitido. Después de todo append y replay:

```text
G = D + K + B + C + E + R
V = D + K + B >= 0
```

Con `q > 0`, vectores `(D,K,B,C,E,R)`:

| Causa | Vector | `delta G` |
| --- | --- | --- |
| `ACREDITACION_COMPRA` | `(+q,0,0,0,0,0)` | `+q` |
| `COMPROMISO_RESERVA` | `(-q,+q,0,0,0,0)` | `0` |
| `LIBERACION_CANCELACION_CLIENTE_VALIDA` / `RESTAURACION_CANCELACION_ESTUDIO` pre-expiración | `(+q,-q,0,0,0,0)` | `0` |
| `CONSUMO_ASISTENCIA` / `CONSUMO_NO_ASISTIDA` / `CONSUMO_CANCELACION_TARDIA` / `CONSUMO_LIMITE_CANCELACIONES` | `(0,-q,0,+q,0,0)` | `0` |
| `EXPIRACION` | `(-q,0,0,0,+q,0)` | `0` |
| `BLOQUEO_REEMBOLSO` / `BLOQUEO_DISPUTA` | `(-q,0,+q,0,0,0)` | `0` |
| `RESTAURACION_BAJO_CONTENCION` | `(0,-q,+q,0,0,0)` | `0` |
| `RESTAURACION_TRAS_REVOCACION_FINANCIERA` | `(0,-q,0,0,0,+q)` | `0` |
| `REVOCACION_REEMBOLSO` / `REVOCACION_DISPUTA` | `(0,0,-q,0,0,+q)` | `0` |
| `DESBLOQUEO_REEMBOLSO_FALLIDO` / `DESBLOQUEO_DISPUTA_GANADA_O_RETIRADA` | `(+q,0,-q,0,0,0)` | `0` |

Un desbloqueo vencido agrega después `EXPIRACION` en la misma transacción. Recovery es un par
atómico con igual `q` y `causal_operation_id`: origen `TRANSFERENCIA_RECUPERACION =
(0,-q,0,0,0,+q)`, derecho nuevo `ACREDITACION_RECUPERACION = (+q,0,0,0,0,0)` y `delta G=+q` sólo
en el nuevo. El `compromiso_origen_id` único hace que el cambio agregado de `V` sea cero y prohíbe
revival o doble crédito. Corrección administrativa es el inverso exacto de un movimiento objetivo
reversible, con objetivo único, mismo derecho/cantidad, actor/permiso/causa y orden causal inverso;
`C/E/R` no regresan salvo esa compensación explícita. Identidad causal única + payload hash hace
replay no-op; contradicción, bucket negativo, ecuación inválida o estado dependiente incompatible
aborta todo y exige revisión.

### 18.2 PN13-002 / NEW-PN13-011 — semántica temporal exacta

Owner: Payments/Política; `Clock` inyectado y `ZoneId America/Mexico_City` son obligatorios. Cada
comando captura un instante causal `S`; persiste instantes UTC (`TIMESTAMPTZ`, microsegundos), zona
y versión, y serializa ISO-8601 con offset o `Z`. Para compra, `S` es el commit único de
`Acreditacion.COMPLETADA`; `D0=LocalDate(S,zona)`, `Dend=D0+N días` o `D0+N meses` calendario con
clamping, y `E=startOfDay(Dend,zona)`. `N>0`, `E>S`, vigencia `[S,E)`, último día local incluido
`Dend-1`; `t=E` está expirado aun con job tardío.

Extensión bloquea/revalida: sólo derecho no expirado con `D>0` suma el periodo comprado a su
`Dend` actual, conserva inicio y nunca acorta. Derecho agotado o expirado no cambia; nacen nuevos
derechos con el `S` de la acreditación nueva. Recovery crea un derecho nuevo cuyo `S` es el instante
de la transacción ganadora que lo procesa; replay devuelve mismo derecho/instante. Datos temporales
ausentes/inválidos o local time irresoluble fallan cerrado. Ejemplos vinculantes: tres días desde
`2026-09-14T10:30-06:00` expiran en `2026-09-17T00:00-06:00`; un mes desde 2027-01-31 tiene
boundary `2027-02-28T00:00-06:00`; recovery de dos días creado `2026-12-05T15:00-06:00` expira
`2026-12-07T00:00-06:00`.

`fechaVencimiento = Dend - 1 día` es el último día calendario válido incluido y
`inicioDelDia(fechaVencimiento + 1, America/Mexico_City) = E` es el límite exclusivo. La política
guarda `limitePostVencimientoDias=N` configurable en días calendario. Un derecho debe pasar a
`COMPROMETIDA` mientras siga válido y queda prohibido comprometer desde `E`:

```text
instanteCompromiso < inicioDelDia(fechaVencimiento + 1, America/Mexico_City)
inicioSesion < inicioDelDia(fechaVencimiento + N + 1, America/Mexico_City)
```

El compromiso válido sobrevive la expiración nominal; su sesión puede iniciar hasta `N` días
calendario después de `fechaVencimiento`, inclusive. Con vencimiento 30 de septiembre y `N=3`, el
1, 2 y 3 de octubre están permitidos y el 4 se rechaza. Una cancelación post-vencimiento válida
puede crear el derecho de recuperación nuevo con su propia vigencia; una compra posterior no
revive el derecho expirado.

Enforcement futuro: `start_instant`, `end_exclusive`, `fecha_vencimiento`,
`limite_post_vencimiento_dias`, `business_zone`, policy/type/version, checks de rango,
inmutabilidad y unicidad causal de acreditación/extensión/recovery. No existe ventana de tiempo
normal adicional para refund.

### 18.3 PN13-003 — asistencia pendiente

Owner de lifecycle: Reservas/Asistencia; Payments sólo ejecuta consecuencia por
`GestorCreditoReserva`. Los terminales previos son `CANCELADA_CLIENTE_VALIDA`,
`CANCELADA_CLIENTE_TARDIA` y `CANCELADA_ESTUDIO`. Entrada única: reserva activa/no cancelada + sesión terminada + asistencia
sin resolver -> `PENDIENTE_ASISTENCIA`; durante pending `K` permanece comprometido indefinidamente.
Salidas únicas: actor `ADMIN` -> `ASISTIDA` o `NO_ASISTIDA`, atómicas con el consumo ya definido.
Instructor no gestiona ni resuelve. Cancelación válida, tardía o del estudio son terminales previos
y prohíben pending. No existe timer, máximo ni inferencia por elapsed time.

Cada comando bloquea y revalida `Reserva -> CompromisoReserva`, exige estado esperado, ADMIN,
idempotency key y payload hash; replay igual retorna resultado, contradicción/ambigüedad conserva
pending y `K`. Corrección tardía sólo ADMIN, razón no vacía y audit trail actor/instante/antes/después;
no reescribe historial/ledger, normalmente conserva consumo y sólo usa compensación explícita si
cambia crédito.

### 18.4 PN13-004 — allowance y cutoff

Owner de horario: Reserva/Programación; owner de cuota: Payments. La clave es exactamente
`cliente_id + YearMonth(inicio_programado_autoritativo en America/Mexico_City) + policy_version`,
nunca el mes de solicitud. El cutoff compara instantes: restaurable si
`cancelacionInstant <= sessionStartInstant - Duration(horasConfiguradas)`; exactitud al corte es
inclusiva.

Enforcement futuro: `periodo_mes DATE` igual al primer día, check `day=1`, unique cliente+mes+policy;
crear missing row por insert-on-conflict y bloquearla. Orden `Reserva/sesión -> cuenta de crédito ->
Derechos -> Compromiso -> allowance`; revalidar versión/inicio antes del contador. Race de sesión,
duplicado, payload contradictorio o incertidumbre hace rollback de capacidad y crédito y falla
cerrado.

### 18.5 PN13-005 / NEW-PN13-012 — Stripe succeeded tardío

Owner monetario: Payments/Stripe; cancelación comercial pertenece a Orden/Compra. Una disposición
local `CANCELACION_TECNICA | CANCELACION_EXPLICITA` no prueba cancelación monetaria;
`Pago.CANCELADO` significa provider-confirmed no-capture. Única excepción monotónica:
`CANCELADO -> CONFIRMADO` al reconciliar el mismo PaymentIntent actual como `succeeded` con orden,
importe y moneda exactos.

Bajo lock `OrdenVenta -> Pago` y causa única `confirmar-pago:pagoId:paymentIntentId`: mismo
settlement es replay; slot vacío + cancelación técnica avanza `OrdenVenta CANCELADA -> PAGADA`,
Compra a `PENDIENTE_ACREDITACION` y crea exactamente una Acreditacion; cancelación comercial
explícita conserva el hecho monetario, lleva Orden/Compra a
`REQUIERE_REVISION_PAGO_TARDIO_REEMBOLSO`, no acredita y crea Reembolso
`SOLICITADO/PAGO_POST_CANCELACION` payment-scoped. Si ya existe settlement efectivo por otro
`Pago`, éste y todos sus derechos se preservan; el segundo éxito es una excepción financiera, no
ocupa el slot, no acredita y exige reconciliar/reembolsar explícitamente sólo el `Pago` anómalo.
Ese refund no bloquea, revoca ni altera derechos del settlement válido. Si se generaron derechos
sin autorización desde el pago anómalo, sólo los atribuibles a ese pago se contienen/revocan.
Nunca se revoca por el mero hecho de que la misma Orden tenga otro settlement válido y la historia
financiera/ledger permanece inmutable. Unicidades `event.id`, provider ref, efecto causal,
settlement y `Acreditacion(pago_id)` hacen duplicados no-op. Mismatch, transición imposible o
derechos incompatibles falla cerrado.

### 18.6 PN13-006 / NEW-PN13-013 — transferencia

Owner: Payments; `Pago` identifica el intento monetario. `transferencia_pago` es 1:1 con Pago y
guarda `PENDIENTE_VALIDACION | CONFIRMADA | RECHAZADA`, ciclo, evidencia vigente y versión.
`evidencia_transferencia` tiene UUID, pago, actor, instante, referencia normalizada opcional,
importe/moneda/fecha, ubicación opaca y hash inmutable; unique pago+hash y detección cross-pago.
`intento_validacion_transferencia` es append-only con UUID, pago, ciclo, evidencia, decisión,
actor/permiso/razón/instante/idempotencia/payload hash.

Registrar requiere `pago.transferencia.registrar` y no acredita; confirmar/rechazar requiere
`pago.transferencia.validar` y estado esperado. Reabrir sólo `RECHAZADA -> PENDIENTE_VALIDACION`
requiere `pago.transferencia.reabrir`, razón, actor, nuevo ciclo y evento; `CONFIRMADA` nunca reabre.
Confirmación bloquea Orden/Pago, usa settlement/acreditación comunes y es idempotente. Si la Orden
ya tiene settlement efectivo por otro `Pago`, la transferencia bajo validación no pasa a
`CONFIRMADA`: termina `RECHAZADA`/contenida con razón auditable `ORDEN_YA_LIQUIDADA`, registra el
incidente financiero operacional y nunca crea segundo settlement, `Acreditacion` ni derechos o
reemplaza el settlement válido. Si el dinero existe externamente, se concilia/reembolsa como
anomalía del `Pago`, sin acreditarlo. Constraints por operación+pago+ciclo+clave, hash, referencia
y settlement; contradicción o evidencia incompatible falla cerrado sin doble
confirmación/acreditación.

### 18.7 PN13-007 — `PRODUCTO_ENTERO` obligatorio

Owner: Payments/Reembolsos. Para cada Compra seleccionada el refund normal exige: Compra
`ACREDITADA`; mismo Pago efectivo `CONFIRMADO`; por cada componente, suma `D` de derechos originales
elegibles exactamente igual a cantidad snapshot; `K=B=C=E=R=0`; sin compromiso ni contención activa
ni expiración efectiva según Clock; sólo compromisos íntegramente liberados/restaurados. Consumo,
penalización, expiración, recovery replacement, compensación administrativa cuantitativa, mismatch
o ambigüedad falla cerrado a `REQUIERE_REVISION`. No hay ventana temporal normal adicional.

El predicado se evalúa bajo los locks comunes y sobre ledger/provenance completo; no es un precheck
optimista ni permite refund parcial/componente.

### 18.8 PN13-008 / NEW-PN13-014 — refund, disputa y contención común

Owner: Payments/Reembolsos. `Reembolso` UUID conserva siempre el `Pago` de origen y su Orden.
`contencion_financiera` tiene tipo `REFUND|DISPUTA`, scope normalizado por Pago/Compra/Derecho y
unicidad parcial activa. Los scopes exactos son:

| Proceso | Origen/scope vinculante | Derechos que puede contener/revocar |
| --- | --- | --- |
| Refund normal MVP | Settlement efectivo + 1..N Compras completas de la misma Orden que satisfacen obligatoriamente `PRODUCTO_ENTERO`; importe contractual total. | Sólo los atribuibles al producto/Compra completos seleccionados. |
| Refund tardío/segundo pago | `Pago` anómalo, payment-scoped. | Ninguno del settlement válido; sólo derechos no autorizados atribuibles al Pago anómalo, si existen. |
| Refund externo/Dashboard | `Pago` originario correlacionado; desde él se derivan Compra(s)/derechos exactos. | Sólo el origen derivado; ambigüedad => `REQUIERE_REVISION`. |
| Disputa | Cargo externo/`Pago` disputado y su settlement efectivo. | Sólo derechos atribuibles a ese settlement; nunca compras o settlements ajenos. |

Refund y disputa del mismo origen financiero se serializan bajo el mismo mutex `OrdenVenta` y
boundary de ownership `Pago`, con locks `Orden -> Compras UUID -> Pago -> proceso UUID ->
Derechos(expira_en,id)`. Ownership/scope ambiguo falla cerrado `REQUIERE_REVISION`; nunca se amplía
a todos los derechos del cliente y siempre se preserva la historia consumida.

Aprobar contiene `D` antes del call. Timeout -> `PENDIENTE_CONFIRMACION`, conserva `B` y misma
identidad proveedor; éxito agrega una sola revocación; fallo definitivo desbloquea idempotentemente.
Dashboard se correlaciona/crea por `stripe_refund_id` único; mapping parcial/ambiguo o consumo
preserva verdad externa y va a revisión. `Disputa` UUID/`stripe_dispute_id` único, ligada al cargo y
Pago: `DETECTADA -> CONTENIDA -> EN_SEGUIMIENTO -> GANADA|PERDIDA|RETIRADA|REQUIERE_REVISION`.
Sólo contiene `D` de su origen, no cancela Reservas ni altera `C`; GANADA/RETIRADA desbloquea y
PERDIDA revoca con causa propia. Una restauración concurrente de `K` se dirige a `B` si existe
contención activa o a `R` si la revocación financiera ya es definitiva; nunca reaparece en `D`.
Carrera o segundo owner se resuelve tras el mutex como replay/correlación o revisión; nunca doble
revocación/compensación.

### 18.9 PN13-009 — identidad, settlement y FEFO

Owner: Payments/Ventas-Derechos. `orden_venta.cliente_id NOT NULL` es canónico; Compra y Derecho
copian `cliente_id` inmutable. Enforcement futuro: uniques `(id,cliente_id)` y FKs compuestas
`compra(orden_id,cliente_id)->orden_venta(id,cliente_id)` y
`derecho_actividad(compra_id,cliente_id)->compra(id,cliente_id)`.

`orden_venta.pago_settlement_id` nullable/unique más FK compuesta `(pago_settlement_id,id) ->
pago(id,orden_venta_id)` es el guard elegido. Se bloquea Orden primero; sólo null asigna, mismo pago
es replay, otro pago va a revisión y el pointer nunca se libera. FEFO serializa por
`cuenta_credito_actividad PK(cliente_id,actividad_id)`, revalida Reserva/tiempo/política, selecciona
`FOR UPDATE` por `end_exclusive ASC,id ASC` sobre `D>0` y crea
`UNIQUE(compromiso_reserva.reserva_id)`; no `SKIP LOCKED` foreground. Varias claves se ordenan
globalmente por cliente+actividad. Mismatch/FK, carrera o falta de elegibilidad revierte reserva y
compromiso, sin fallback a otro cliente/derecho.

### 18.10 PN13-010 / NEW-PN13-015 — tres niveles de entrega

Owner: Notifications. `Notificacion` UUID/dedupe = causa EventoDominio/MensajeOutbox + tipo +
audiencia + template/version + agenda/version. `EntregaLogica` UUID/dedupe = Notificacion + canal +
endpoint estable, una por dispositivo push. `IntentoEntrega` UUID inmutable y unique
`(entrega_logica_id,secuencia)`, secuencia desde 1 monotónica/nunca reutilizada. Provider
idempotency, si existe, usa EntregaLogica. Claim condicional + lease recupera concurrencia.

TRANSITORIO termina intento y crea el siguiente sólo si queda retry; PERMANENTE termina entrega;
token inválido desactiva endpoint y deja la entrega creada en fallo terminal conocido; INCIERTO
reconcilia y, si retry puede duplicar, detiene automatización. La agregación mutuamente exclusiva
aplica en este orden:

1. `REQUIERE_REVISION` si alguna `EntregaLogica` agotó retries automáticos con outcome incierto;
2. `SIN_CANALES_DISPONIBLES` sólo al planificar sin canal/endpoint usable, de modo que no pudo
   crearse ninguna `EntregaLogica` usable; nunca se reutiliza después del fallo de una creada;
3. `ENTREGADA` si todas las entregas usables creadas tuvieron éxito;
4. `PARCIALMENTE_ENTREGADA` si al menos una tuvo éxito, ninguna quedó incierta y toda no exitosa
   es fallo terminal conocido;
5. `FALLA_PERMANENTE` si se creó al menos una entrega usable, ninguna tuvo éxito ni quedó incierta
   y todas son fallo terminal conocido.

`REQUIERE_REVISION` precede cualquier agregación terminal normal y no se reintenta si arriesga
duplicado. Invalidar un endpoint después de crear su entrega no produce
`SIN_CANALES_DISPONIBLES`. Estados terminales no regresan, `IntentoEntrega` permanece inmutable,
Outbox nace atómico con el hecho y delivery es at-least-once; un retry nunca modifica el hecho de
negocio.

### 18.11 Lock ordering transversal y enforcement

Las secuencias canónicas, todas iniciadas por su primer mutex y sin adquirir después una fila
anterior, son:

```text
settlement/Stripe/transferencia: OrdenVenta -> Pago -> Acreditacion -> Compras -> Derechos
refund/disputa: OrdenVenta -> Compras UUID -> Pago -> Reembolso|Disputa UUID -> Derechos FEFO
reserva/asistencia/cancelación: Reserva/sesión -> cuenta_credito_actividad -> Derechos FEFO -> Compromiso -> allowance
workers Inbox/Outbox/Notifications: claim condicional -> lease -> agregado propio
```

Stripe y transferencia convergen en el mismo guard; refund/disputa comparten contención;
Reservas posee scheduling/asistencia y Payments movimientos; una corrección tardía normalmente
preserva consumo; notificación no muta negocio. El contrato físico son tablas/FKs/uniques/checks,
version/expected-state, locks y causal payload hashes descritos aquí: no existe todavía SQL,
migración ni código que los implemente.

### 18.12 Mapeo de hallazgos y lifecycle

| Hallazgo | Sección correctiva y canónico principal | Estado |
| --- | --- | --- |
| PN13-001 | §18.1; DA-015/016/017/019 | `CLOSED` |
| PN13-002 / NEW-PN13-011 | §18.2; Dominio §§13.3–13.5; DA-009 | `CLOSED / CLOSED` |
| PN13-003 | §18.3; Dominio §13.5; DA-016 | `CLOSED` |
| PN13-004 | §18.4; Dominio §13.5; DA-017 | `CLOSED` |
| PN13-005 / NEW-PN13-012 | §18.5; Dominio §§13.6–13.7; DA-014/018/019 | `CLOSED / CLOSED` |
| PN13-006 / NEW-PN13-013 | §18.6; Dominio §13.7; DA-014/018 | `CLOSED / CLOSED` |
| PN13-007 | §18.7; Dominio §13.6; DA-019 | `CLOSED` |
| PN13-008 / NEW-PN13-014 | §18.8; Dominio §13.6; DA-015/017/019 | `CLOSED / CLOSED` |
| PN13-009 | §18.9; DA-014/017 | `CLOSED` |
| PN13-010 / NEW-PN13-015 | §18.10; Dominio §13.8; DA-020 | `CLOSED / CLOSED` |
| NEW-PN13-016 | §5.1/§18.1; DA-015 | `CLOSED` |
| NEW-PN13-017 | §5.1 frente a §18.1 y DA-015 | `OPEN / P2 / EDITORIAL / NON_BLOCKING / IMPLEMENTATION_INDEPENDENT` |

El review PN-13.1 y el audit residual posterior conservan sus resultados históricos `FAIL`. El
re-audit fresh R1.2 reportó `AUTHORITY_AUDIT=PASS / P0=0 / P1=0 / P2=1`, cerró
PN13-001..PN13-010 y NEW-PN13-011..NEW-PN13-016, y dejó exclusivamente NEW-PN13-017 abierto sin
bloquear el documentation gate. Publication/closure quedan `PENDING`; PN-14 e implementación
permanecen `NOT_AUTHORIZED`, y runtime, autoridad productiva, migración, cutover y F2E no cambian.
