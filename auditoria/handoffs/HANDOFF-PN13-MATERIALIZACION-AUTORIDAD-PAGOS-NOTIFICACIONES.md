# FeelingPilates — Handoff PN-13 — Materialización de autoridad de Pagos y Notificaciones

Handoff status: `MATERIALIZED / READY_FOR_FRESH_INDEPENDENT_HANDOFF_DOCUMENT_AUDIT / NOT_APPROVED / NOT_ACTIVE / TARGET_NOT_STARTED / IMPLEMENTATION_NOT_STARTED / IMPLEMENTATION_NOT_AUTHORIZED`

Target: `PN-13 / materialización documental de autoridad de Payments & Notifications`

Type: `DOCUMENTATION_ONLY / NORMATIVE_AUTHORITY_MATERIALIZATION`

Materialization role: `HANDOFF_DOCUMENTER / DOCUMENTATION_SCOPE_GUARD / PROVENANCE_MATERIALIZER`

## 1. Autoridad, lifecycle y entry gate

El repositorio, sus canónicos y la evidencia física Git son autoridad. El chat y los paquetes de
entrada externos sólo coordinan y aportan provenance; no son autoridad persistente por sí mismos.

Este handoff materializa exclusivamente el bootstrap documental necesario para que PN-13 pueda
ser evaluada como futura unidad documental. No activa PN-13, no aprueba su contenido por
anticipado y no autoriza implementación.

```text
HANDOFF: MATERIALIZED
HANDOFF DOCUMENT AUDIT: PENDING
HANDOFF: NOT_APPROVED / NOT_ACTIVE
PN-13 TARGET: NOT_STARTED / NOT_AUTHORIZED_TO_START
PN-13 AUTHORITY MATERIALIZATION: NOT_STARTED
PN-14: NOT_REACHED / NOT_AUTHORIZED
IMPLEMENTATION: NOT_STARTED / NOT_AUTHORIZED
```

El único siguiente gate permitido para este artefacto es:

```text
FRESH_INDEPENDENT_HANDOFF_DOCUMENT_AUDIT
```

Sólo después de ese audit, una transición documental separada y explícita en el canónico
competente puede aprobar y activar el hash exacto de este handoff. Su mera existencia física no
autoriza a un DOCUMENTER a comenzar PN-13.

```text
MATERIALIZED != AUDITED
AUDITED != APPROVED
APPROVED != ACTIVE
ACTIVE DOCUMENTARY HANDOFF != IMPLEMENTATION AUTHORITY
DOCUMENTATION PUBLISHED != PRODUCTIVE
```

## 2. Identidad y provenance congelada

```text
Repository:
/Users/jesusaldaircruzortiz/Desktop/Feelingpilates/feelingpilates-payments-notifications

Lane:
Payments & Notifications

Branch:
pagos/pagos-notificaciones-r1

Frozen baseline:
a0ec85818b771d4ac924b427fa1e90244ea9fe8e

Initial staging:
EMPTY

Initial working tree:
CLEAN

Upstream:
NONE / ACCEPTED_FOR_THIS_LIFECYCLE

PN accepted input packet SHA-256:
9877d7093a925b3288c9e797d1359401ce06dfb55567390e072d8afacf3edaf4

Input classification:
EXTERNAL_ACCEPTED_INPUT /
NOT_REPOSITORY_AUTHORITY_UNTIL_AUDITED_AND_MATERIALIZED
```

El baseline congelado es la base física de este carril y no autoriza a reinterpretar el lifecycle
F2E heredado. El packet hash identifica los términos aceptados que PN-13 debe materializar; no
convierte el archivo externo, este handoff ni una conversación en autoridad de producto.

## 3. Contrato de aislamiento físico

Único worktree autorizado para cualquier lectura o futura escritura PN:

```text
/Users/jesusaldaircruzortiz/Desktop/Feelingpilates/feelingpilates-payments-notifications
```

Worktrees expresamente prohibidos:

```text
/Users/jesusaldaircruzortiz/Desktop/Feelingpilates/feelingpilates
/Users/jesusaldaircruzortiz/Desktop/Feelingpilates/feelingpilates-autopilot-r1
```

No se inspeccionan, modifican, formatean, stagean, limpian, resetean, stashean, integran ni
publican archivos de esos worktrees. La existencia de trabajo F2E o Autopilot no concede scope a
esta lane.

La branch histórica divergente:

```text
feature/calendario-reservas-pagos
e515152671dc5b2801f4fd7ef3e1e608bfc55a0a
```

queda clasificada exactamente como:

```text
CONCEPTUAL_EVIDENCE_ONLY
NO_MERGE
NO_CHERRY_PICK
NO_IMPLEMENTATION_SOURCE
```

No puede usarse como fuente de archivos, diff aplicable, baseline, implementación implícita ni
atajo de migración. Una idea sólo puede incorporarse si PN-13 la formula de forma autocontenida y
la somete a su propio gate documental.

## 4. Lifecycle PN preservado

El input externo reporta este lifecycle previo, que PN-13 deberá materializar y reconciliar con
las fuentes físicas competentes sin presentarlo como autoridad previa del repositorio:

```text
PN-0  Lane Charter: COMPLETED / ACCEPTED OUTSIDE CODEX
PN-1  Product Authority: COMPLETED / ACCEPTED OUTSIDE CODEX
PN-2  Repository Recon: COMPLETED / ACCEPTED OUTSIDE CODEX
PN-3  Domain Model Authority: COMPLETED / ACCEPTED OUTSIDE CODEX
PN-4  Payment Architecture: COMPLETED / ACCEPTED OUTSIDE CODEX
PN-5  Stripe Authority: COMPLETED / ACCEPTED OUTSIDE CODEX
PN-6  Failure & Recovery Design: COMPLETED / ACCEPTED OUTSIDE CODEX
PN-7  Notifications Architecture: COMPLETED / ACCEPTED OUTSIDE CODEX
PN-8  MVP Scope Freeze: COMPLETED / ACCEPTED OUTSIDE CODEX
PN-9  Git Baseline Freeze: COMPLETED / ACCEPTED OUTSIDE CODEX
PN-10 Branch + Worktree Materialization: PHYSICALLY COMPLETED
PN-11 Isolation Verification: COMPLETED
PN-12 Project/lane creation: COMPLETED
PN-12.1 Authority materialization preflight: FAIL / P0=0 / P1=10 / P2=0
PN-12.2 Bootstrap handoff materialization: THIS DOCUMENT ONLY
PN-13 Authority materialization: NOT_ACTIVE / NOT_STARTED
PN-14 Implementation authorization: NOT_REACHED / NOT_AUTHORIZED
```

PN-12.1 confirmó path, branch, baseline, staging, working tree e aislamiento, pero falló el gate
documental porque no existía autoridad PN persistida ni un handoff PN-13 auditado y activo. Los
diez P1 son obligaciones documentales/de diseño; no autorizan correcciones de código durante
PN-12.2 ni PN-13.

El handoff F2E activo que existe en el baseline permanece fuera del scope de esta materialización.
Este archivo no lo cierra, modifica, sustituye ni ejecuta. Una futura activación PN debe registrar
la semántica de lane en `auditoria/ESTADO-ACTUAL.md` sin atribuir a este handoff una transición que
todavía no ocurrió.

## 5. Workflow profile futuro de PN-13

Una vez que este handoff haya sido auditado, aprobado y activado de forma separada, PN-13 usará:

```text
WORKFLOW_PROFILE: DOCUMENTATION_ONLY / NORMATIVE_AUTHORITY_MATERIALIZATION

PREPARE: APPLICABLE
DOCUMENT: APPLICABLE
DOCUMENT AUDIT: APPLICABLE
READY TO PUBLISH: APPLICABLE AFTER DOCUMENTATION GATE PASS
PUBLICATION: APPLICABLE / SEPARATELY AUTHORIZED
PUBLICATION CLOSURE: APPLICABLE
PUBLICATION CLOSURE AUDIT: APPLICABLE

SCOPE GATE: APPLICABLE / PENDING
DOCUMENTATION GATE: APPLICABLE / PENDING
PUBLICATION GATE: APPLICABLE / PENDING
PUBLICATION CLOSURE GATE: APPLICABLE / PENDING

IMPLEMENTATION GATE: NOT_APPLICABLE
TESTS GATE: NOT_APPLICABLE
HOST VALIDATION: NOT_APPLICABLE
```

`NOT_APPLICABLE` no significa `PASS`. PN-13 no debe ejecutar ni inventar evidencia de código,
tests, Maven, Docker, Testcontainers, SQL, migraciones, Stripe, email, push o runtime.

La futura materialización debe separar roles:

```text
DOCUMENTER -> materializa sólo el allowlist documental de PN-13
DOCUMENT_AUDITOR fresh -> audita sin modificar ese delta
PUBLISHER -> publica sólo bajo gate y política explícitos
DOCUMENT_AUDITOR fresh -> audita el cierre de publicación si aplica
```

El DOCUMENTER no se autoaudita y no escribe su propio review independiente.

## 6. Propósito exacto de PN-13

PN-13 materializará en autoridad del repositorio el diseño ya aceptado para Payments &
Notifications. Debe cerrar un contrato autocontenido y auditable que cubra, como mínimo:

1. autoridad de producto, catálogo, compras y snapshots comerciales;
2. ledger de derechos, créditos, vigencias y compromisos;
3. entidades conceptuales y máquinas de estado cerradas;
4. ownership y fronteras transaccionales con Reservas/Programación;
5. arquitectura de órdenes, pagos, acreditación y métodos de pago;
6. autoridad Stripe, PaymentIntent, webhooks, Inbox y reconciliación;
7. idempotencia, concurrencia, fallos, retries y recovery;
8. refund monetario, reversión/contención de derechos y conciliación externa;
9. Outbox, notificación lógica, intentos de entrega, canales y scheduling;
10. scope MVP y exclusiones explícitas;
11. clasificación legacy y estrategia de migración/coexistencia/cutover;
12. contrato físico futuro de implementación y gates técnicos requeridos.

PN-13 no implementa ninguno de esos contratos. Su resultado es exclusivamente autoridad
documental candidata a audit.

## 7. Autoridad de producto y dominio que PN-13 debe materializar

### 7.1 Productos, paquetes y snapshots

- se soportan productos de clase individual y paquetes de una o más actividades;
- cada componente de actividad tiene cantidad explícita;
- un paquete es un producto comercial indivisible;
- no existe cancelación o refund monetario parcial por componente;
- Duo Reformer no es operacional en MVP, pero el diseño no impide soporte futuro;
- el precio contractual del producto/paquete es explícito;
- un precio sugerido o de referencia puede existir, pero no es contractual;
- la compra conserva un snapshot comercial inmutable de precio, moneda, composición, cantidades,
  vigencia y políticas aplicables;
- cambios posteriores de catálogo no reinterpretan compras históricas.

### 7.2 Conceptos separados

PN-13 debe fijar responsabilidades, cardinalidades, identidad, estados y ownership para:

```text
OrdenVenta
Compra
CompraComponenteSnapshot
Pago
Acreditacion
DerechoActividad
MovimientoCredito
MovimientoVigencia
CompromisoReserva
Reembolso
```

Los nombres Java definitivos pueden ajustarse sólo si PN-13 documenta una equivalencia inequívoca
y conserva nomenclatura española donde sea técnicamente razonable. `Compra`, `Pago`,
`Acreditacion` y `DerechoActividad` no pueden conflarse.

Una `OrdenVenta` representa checkout/ticket; una `Compra`, cada producto adquirido. Una orden
puede tener varios intentos de pago, pero a lo sumo un pago efectivo confirmado. Split payments
quedan fuera del MVP.

### 7.3 Ledger y derechos

- el saldo se deriva de movimientos inmutables/auditables, no de un contador editable;
- los derechos se agrupan por actividad/lote y conservan provenance común de la compra;
- las correcciones son movimientos compensatorios, nunca edición de historia;
- FEFO compromete primero el derecho elegible con expiración más cercana;
- cada reserva usa un `CompromisoReserva` explícito e idempotente;
- el saldo disponible nunca puede ser negativo;
- dos reservas por el último crédito se serializan y exactamente una puede ganar;
- un paquete mixto mantiene provenance comercial común y sigue siendo indivisible.

### 7.4 Vigencia

- las políticas son configurables y versionadas;
- se soportan `DAYS` y `MONTHS`; un mes calendario no se reduce a días arbitrarios;
- la extensión puede ser `SAME_ACTIVITY` o `ALL_ACTIVE_ACTIVITIES`;
- sólo derechos con cantidad disponible mayor a cero participan en extensión normal;
- saldo cero concluye el derecho para extensión aunque su expiración nominal sea futura;
- una compra posterior inicia nueva vigencia bajo sus propios términos cuando el derecho previo
  quedó concluido;
- créditos expirados no reviven automáticamente;
- elegibilidad de reserva respecto de expiración es política versionada;
- puede permitirse reservar después de expiración con grace period opcional y acotado;
- créditos ya comprometidos válidamente sobreviven expiración mientras permanezcan asociados;
- una cancelación válida después de la expiración original puede crear un crédito de recuperación
  corto, sin revivir el paquete anterior ni constituir compra nueva;
- backend `Clock` y zona de negocio son autoridad temporal, nunca el reloj del dispositivo.

### 7.5 Reservas, cancelaciones y asistencia

- no se crea un segundo dominio paralelo de reservas o clases;
- Reservas/Programación conserva autoridad de fecha, hora, salón, instructor y disponibilidad;
- Payments conserva autoridad de elegibilidad y consecuencias de crédito;
- mientras ambos vivan en el mismo monolito Spring/PostgreSQL, reserva y compromiso de crédito son
  atómicos;
- cancelación y liberación/consumo son atómicos;
- asistencia/no-asistencia y consumo final son atómicos;
- la asistencia puede permanecer `PENDIENTE_ASISTENCIA` hasta resolución autorizada;
- ningún timer convierte automáticamente una asistencia en `NO_ASISTIDA`;
- el cliente siempre puede cancelar para liberar la plaza;
- la anticipación mínima usa horas naturales transcurridas antes del inicio en MVP y es
  configurable/versionada;
- una cancelación en tiempo restaura crédito sólo si queda allowance reembolsable;
- el máximo de cancelaciones reembolsables es configurable por periodo; MVP usa mes calendario;
- cancelación tardía o allowance agotado libera plaza pero consume la clase;
- `NO_ASISTIDA` es distinta de cancelación tardía y también consume la clase;
- cancelación del estudio siempre restaura la clase y no consume allowance del cliente;
- cancelaciones operativas de día completo, parcial o sesión individual pertenecen a
  Programación/Reservas;
- correcciones administrativas excepcionales son explícitas y auditables.

## 8. Autoridad de pagos, Stripe y recovery que PN-13 debe materializar

### 8.1 Métodos y acreditación

Métodos MVP:

```text
STRIPE
EFECTIVO
TRANSFERENCIA
```

- la UI cliente nunca confirma autoritativamente un pago;
- efectivo se confirma por operador autorizado después de recibir dinero;
- transferencia usa `PENDIENTE_VALIDACION -> CONFIRMADA | RECHAZADA`;
- transferencia no acredita derechos antes de validación;
- registrar transferencia y validarla son permisos distintos, aunque un operador pueda poseer
  ambos inicialmente;
- todos los métodos confirmados convergen en una única pipeline durable e idempotente de
  acreditación;
- la acreditación efectiva del producto, no sólo `PAGO_CONFIRMADO`, es el éxito comercial
  comunicado al cliente.

### 8.2 Idempotencia

- identidad interna = operación + contexto + payload relevante;
- reutilizar una clave con payload contradictorio falla de forma cerrada;
- idempotencia del proveedor complementa, pero nunca reemplaza, idempotencia durable interna;
- acreditación, ledger, compromisos, Inbox, Outbox y refunds son retryables e idempotentes.

### 8.3 Stripe

- React Native usa Stripe PaymentSheet;
- PaymentIntent es el objeto de pago del MVP;
- backend fija amount y currency;
- success del PaymentSheet es sólo UX;
- `payment_intent.succeeded` puede confirmar el pago interno;
- `payment_intent.payment_failed` representa un intento fallido y no necesariamente un pago
  completo terminal;
- `Stripe-Signature` se verifica contra el raw request body;
- cada evento se persiste en Inbox durable antes de procesamiento complejo;
- duplicados y eventos fuera de orden son tolerados;
- un evento antiguo no regresa `CONFIRMADO -> FALLIDO`;
- estados pendientes ambiguos requieren reconciliación contra Stripe;
- la idempotency key Stripe deriva de identidad interna estable del pago/refund;
- métodos Stripe con notificación diferida y tarjetas guardadas quedan post-MVP;
- refund tiene lifecycle externo propio y sincroniza al menos `refund.created`, `refund.updated` y
  `refund.failed`;
- disputas MVP requieren detección, contención/bloqueo, auditoría y alerta interna; evidencia
  completa queda post-MVP.

### 8.4 Fallo, recovery y concurrencia

PN-13 debe cerrar estados, transiciones válidas, owner transaccional y recovery para, como mínimo:

- outcome externo incierto mediante identidad estable y reconciliación;
- duplicado de webhook sin duplicar dinero o créditos;
- evento Stripe fuera de orden sin regresión;
- crash después de éxito Stripe y antes de acreditar;
- crash después de commit de acreditación sin reacreditar;
- dos reservas concurrentes por el último crédito;
- cancelación duplicada con a lo sumo una restauración;
- allowance mensual serializado por cliente+periodo;
- extensión de vigencia por actividad serializada;
- máximo un pago efectivo por orden;
- rechazo/reapertura de transferencia mediante corrección explícita auditable;
- refund incierto con derechos bloqueados;
- refund/disputa externa con historia consumida y estado `REQUIERE_REVISION`;
- cancelaciones masivas del estudio procesadas idempotentemente;
- expiración fail-closed aunque un job de materialización se retrase;
- contradicciones no resolubles en `REQUIERE_REVISION`, nunca transición adivinada.

## 9. Refund que PN-13 debe materializar

- refund monetario y restauración de crédito son conceptos distintos;
- `Reembolso` tiene entidad, identidad, permisos y lifecycle propios;
- el refund total normal MVP es configurable y sólo aplica si el producto adquirido está entero;
- entero significa cero unidades consumidas, penalizadas, expiradas o comprometidas y todos los
  derechos originales restaurados;
- una reserva liberada correctamente no destruye elegibilidad si el producto vuelve a estar entero;
- la ventana de refund es configurable/versionada;
- prorateo automático y refund parcial de paquete quedan fuera del MVP;
- componentes del paquete no pueden reembolsarse independientemente;
- derechos elegibles se bloquean mientras el outcome aprobado esté pendiente;
- si falla el refund, los derechos se desbloquean/restauran mediante compensación auditable;
- refunds iniciados fuera del sistema se reconcilian;
- historia de servicio consumido nunca se elimina silenciosamente.

## 10. Autoridad de notificaciones que PN-13 debe materializar

Conceptos separados:

```text
EventoDominio
MensajeOutbox
Notificacion
IntentoEntrega
```

Reglas obligatorias:

- Outbox se persiste en la misma transacción que el hecho de dominio;
- email y push nunca se envían dentro de la transacción de negocio;
- una notificación lógica puede producir múltiples canales/dispositivos;
- notificación e intento de entrega tienen identidades de deduplicación;
- semántica de entrega = at-least-once + deduplicación, no exactly-once ficticio;
- retries usan backoff configurable y terminan en `FALLA_PERMANENTE`;
- fallos de proveedor se clasifican en transitorios/permanentes;
- recordatorios y expiraciones se revalidan contra estado actual antes de enviar;
- cambios de reserva/vigencia invalidan notificaciones programadas obsoletas;
- promociones se separan de notificaciones transaccionales y respetan preferencias/consentimiento;
- push tokens pertenecen a dispositivos;
- `COMPRA_ACREDITADA` es el evento de éxito visible al cliente;
- cancelación del estudio se notifica después de restaurar efectivamente el crédito;
- próxima expiración sólo se notifica mientras quede saldo elegible;
- templates y datos renderizados preservan versión/auditabilidad histórica;
- PII, tokens y secretos no aparecen en logs;
- alertas internas cubren disputas, webhooks no conciliados y fallos críticos permanentes;
- proveedor email/push es infraestructura, no autoridad de dominio.

Notificaciones MVP iniciales:

```text
reserva creada
recordatorio configurable
cancelación del estudio
cancelación válida del cliente (push mínimo; email opcional/configurable)
compra acreditada
próxima expiración
refund confirmado
soporte promocional básico separado de preferencias transaccionales
```

## 11. Exclusiones MVP vinculantes

PN-13 debe conservar explícitamente fuera del scope inicial:

```text
reglas operativas de pago Duo Reformer
suscripciones / auto-renew
gift cards
motor avanzado de promociones/cupones
split payments
conciliación bancaria automática
refund automático prorrateado o parcial de paquete
workflow completo de evidencia de disputas Stripe
métodos Stripe con notificación diferida
tarjetas/métodos de pago guardados
segundo módulo de clases/reservas
edición manual directa de saldo
```

Una abstracción futura-compatible no autoriza implementar una exclusión.

## 12. Allowlist física futura de materialización PN-13

Después de audit y activación explícita de este handoff, el DOCUMENTER PN-13 podrá crear o
modificar estos y sólo estos seis archivos:

```text
auditoria/fase-pn13-materializacion-autoridad-pagos-notificaciones.md
auditoria/contexto/DOMINIO-FUNCIONAL.md
auditoria/DECISIONES-ARQUITECTONICAS.md
auditoria/ARQUITECTURA-ACTUAL.md
auditoria/contexto/MAPA-LEGACY-Y-MIGRACION.md
auditoria/ESTADO-ACTUAL.md
```

```text
PN13_AUTHORIZED_DOCUMENT_PATHS_EXHAUSTIVE: EXACTLY_SIX_FILES
DIRECTORY_GLOBS: NON_AUTHORIZING
ANY OTHER PATH: FORBIDDEN
```

El DOCUMENTER puede usar `ESTADO-ACTUAL.md` para registrar que el delta PN-13 fue materializado y
queda pendiente de audit. No puede registrar por sí mismo `DOCUMENTATION_GATE=PASS`, aprobar su
propio checkpoint, publicar, cerrar publicación ni autorizar PN-14.

Queda expresamente fuera de la allowlist del DOCUMENTER:

```text
auditoria/reviews/PN13-REVIEW-MATERIALIZACION-AUTORIDAD-PAGOS-NOTIFICACIONES.md
auditoria/handoffs/**
auditoria/intervenciones/**
src/**
pom.xml
application*.properties
application*.yaml
db/migration/**
tests
frontend
mobile
```

## 13. Ownership canónico y precedencia

| Artefacto | Ownership competente |
| --- | --- |
| `auditoria/ESTADO-ACTUAL.md` | Lifecycle, handoff activo, gates, siguiente acción permitida y autorización operacional. |
| `auditoria/contexto/DOMINIO-FUNCIONAL.md` | Reglas funcionales estables de producto y dominio. |
| `auditoria/DECISIONES-ARQUITECTONICAS.md` | Decisiones técnicas/arquitectónicas aceptadas y estado de materialización. |
| `auditoria/ARQUITECTURA-ACTUAL.md` | Verdad física del estado actual de implementación y relaciones existentes. |
| `auditoria/contexto/MAPA-LEGACY-Y-MIGRACION.md` | Autoridad productiva, clasificación legacy, coexistencia, migración, fence y cutover. |
| `auditoria/fase-pn13-materializacion-autoridad-pagos-notificaciones.md` | Checkpoint/contrato PN-13 detallado, únicamente en su hash auditado. |
| Handoff PN-13 | Scope, allowlist, precondiciones, prohibiciones y gates; no sustituye reglas de dominio. |
| Review PN-13 | Evidencia histórica del audit; `EVIDENCE_ONLY`, no autoridad normativa ni operacional. |
| Código/migraciones actuales | Evidencia física legacy/evolutiva; no autoridad para el diseño nuevo. |

El checkpoint PN-13 debe enlazar cada decisión a su canónico competente y evitar duplicar
autoridad contradictoria. Si dos fuentes competentes discrepan, el gate falla de forma cerrada:
no se elige silenciosamente una versión. Un canónico stale se marca y reconcilia conforme al
protocolo.

Un audit posterior determina autoridad mediante:

1. path exacto y contenido físico;
2. SHA-256 exacto del checkpoint/handoff objetivo;
3. branch, baseline y commit de publicación cuando exista;
4. `ESTADO-ACTUAL.md` para lifecycle y activación;
5. canónico competente para cada dimensión;
6. review como evidencia del gate, nunca como sustituto del canónico;
7. delta before/after y allowlist exacta.

Ningún chat, resumen, código legacy o archivo no registrado puede auto-declararse autoritativo.

## 14. Obligación de clasificación legacy

PN-13 debe inspeccionar y clasificar cada pieza relevante como `REUSE`, `REWORK` o `RETIRE`. La
clasificación no autoriza cambios y debe incluir rationale, target conceptual, compatibilidad,
migración de datos/API, coexistencia, orden de cutover y safety net.

Inventario mínimo obligatorio:

```text
src/main/java/com/feelingpilates/pagos/entidad/Paquete.java
src/main/java/com/feelingpilates/pagos/entidad/PaqueteActividad.java
src/main/java/com/feelingpilates/pagos/entidad/Compra.java
src/main/java/com/feelingpilates/pagos/servicio/PagoService.java
src/main/java/com/feelingpilates/pagos/servicio/VentaService.java
src/main/java/com/feelingpilates/pagos/controlador/PagoController.java
src/main/java/com/feelingpilates/pagos/StripeConfig.java
src/main/java/com/feelingpilates/pagos/repositorio/CompraRepository.java
src/main/java/com/feelingpilates/notificaciones/EmailService.java
src/main/java/com/feelingpilates/notificaciones/EmailServiceConsola.java
```

Migraciones mínimas:

```text
V22_1__paquetes_y_compras.sql
V22_2__compra_idempotencia.sql
V22_3__permiso_reembolsar_pagos.sql
V23__caja_paquete_actividades.sql
V24__eliminar_paquetes_semilla.sql
V25__compra_salon.sql
V26__compra_grupo.sql
V27__compra_motivo_estado.sql
V28__permisos_caja_granulares.sql
V29__permiso_vista_caja.sql
V30__simplificar_descripcion_permisos_caja.sql
V31__reestructurar_permisos_caja.sql
V32__renombrar_permisos_caja_a_venta.sql
V33__granularizar_permisos_catalogo_venta.sql
V34__renombrar_permisos_catalogo_a_servicios.sql
V35__eliminar_permiso_venta_reembolsar.sql
```

La boundary actual de `Reserva`, `ReservaService`, `ReservaRepository` y `ReservaController` debe
mapearse sin crear un dominio paralelo. PN-13 debe declarar qué ownership conserva Reservas y qué
operaciones de crédito pertenecen a Payments, junto con la atomicidad y locks/constraints
necesarios para la implementación futura.

La matriz debe distinguir explícitamente:

```text
EXISTING / LEGACY_EVOLUTION_SOURCE
REUSE | REWORK | RETIRE
DESIGNED_NOT_IMPLEMENTED
IMPLEMENTED_NOT_PRODUCTIVE
PRODUCTIVE_AUTHORITY
CUTOVER_NOT_AUTHORIZED
```

## 15. Obligaciones de cierre derivadas de PN-12.1

PN-13 no puede superar su documentation gate mientras cualquiera de estas obligaciones A–J siga
ambigua o delegada al futuro executor:

### A. Snapshot comercial inmutable

Cerrar catálogo mutable versus compra histórica, composición, precio, moneda, cantidades,
vigencia, políticas y provenance. Ningún read histórico puede depender del estado actual de
`Paquete`.

### B. Separación de dominio

Cerrar identidad, cardinalidad, ownership y máquinas de estado de `OrdenVenta`, `Compra`, `Pago`,
`Acreditacion`, `DerechoActividad`, `MovimientoCredito`, `MovimientoVigencia`,
`CompromisoReserva` y `Reembolso`, incluyendo `CompraComponenteSnapshot`.

### C. Transferencia y efectivo

Cerrar `PENDIENTE_VALIDACION -> CONFIRMADA | RECHAZADA`, permisos distintos de registro/validación,
momento de confirmación de efectivo y convergencia a la pipeline de acreditación.

### D. Stripe, Inbox e idempotencia

Cerrar Inbox durable, raw-body signature, eventos duplicados/fuera de orden, failure no terminal,
transiciones monotónicas, operación+contexto+payload, contradictory-key rejection, retries y
reconciliación.

### E. Vigencia versionada

Cerrar unidades calendario, `Clock`, zona de negocio, scopes de extensión, elegibilidad,
expiración fail-closed, grace period y créditos de recuperación.

### F. Frontera Reserva-crédito

Cerrar atomicidad de compromiso, liberación, restauración y consumo final; FEFO; último crédito;
idempotencia y ownership sin segundo módulo de Reservas.

### G. Cancelación y asistencia

Cerrar anticipación, allowance por mes calendario, cancelación tardía, `NO_ASISTIDA`, cancelación
del estudio, correcciones administrativas y recovery credit.

### H. Reembolso

Cerrar entidad/lifecycle, producto entero, ventana, bloqueo de derechos, outcome incierto,
compensación, conciliación externa, disputas y `REQUIERE_REVISION`.

### I. Notificaciones

Cerrar Outbox, notificación lógica, intentos de entrega, canales, deduplicación, retries/backoff,
revalidación de scheduled work, preferencias, dispositivos, versionado de templates, privacidad y
alertas internas.

### J. Contrato físico de implementación

Cerrar lo suficiente para que ningún futuro executor decida arquitectura sustancial:

- nombres finales o equivalencias explícitas;
- packages/módulos y dependencias permitidas/prohibidas;
- entidades, tablas, columnas, claves, constraints e índices;
- máquinas de estado y transiciones monotónicas;
- fronteras transaccionales, locks, niveles de aislamiento y orden de locking;
- contratos de repositorio, jobs, Inbox/Outbox y reconciliadores;
- endpoints/DTOs, permisos, idempotency contract y compatibilidad API;
- estrategia expand/migrate/contract, backfill, rollback, coexistencia y cutover;
- slices de implementación ordenados con allowlists futuras separadas;
- safety net unitario, integración PostgreSQL, concurrencia, Stripe/webhook y notificaciones;
- gates técnicos y evidencia requerida por slice;
- matriz `REUSE/REWORK/RETIRE` completa.

Los nombres físicos no deben inventarse durante implementación. Si PN-13 conserva alguna decisión
abierta, debe clasificarla como blocker de PN-14 y prohibir el slice afectado.

## 16. Contrato de review independiente

El audit de este handoff y el audit posterior de PN-13 son ejecuciones distintas. Ambos requieren
rol `DOCUMENT_AUDITOR`, modo `READ_ONLY`, contexto fresh e independencia del DOCUMENTER.

Artefacto de evidencia esperado después de que PN-13 sea materializada:

```text
auditoria/reviews/PN13-REVIEW-MATERIALIZACION-AUTORIDAD-PAGOS-NOTIFICACIONES.md
```

Clasificación:

```text
EVIDENCE_ONLY
NOT_NORMATIVE_AUTHORITY
NOT_OPERATIONAL_AUTHORITY
NOT_IMPLEMENTATION_AUTHORITY
```

El review debe registrar target, SHA-256 auditado, baseline/branch/HEAD, role, mode, delta,
P0/P1/P2, ownership canónico, cobertura A–J, isolation, allowlist, gates y veredicto. No puede
editar el checkpoint/canónicos ni convertir su propia recomendación en activación.

Para aprobar PN-13 se requiere, como mínimo:

```text
P0 = 0
P1 = 0
SCOPE GATE = PASS
DOCUMENTATION GATE = PASS
IMPLEMENTATION GATE = NOT_APPLICABLE
TESTS GATE = NOT_APPLICABLE
HOST VALIDATION = NOT_APPLICABLE
IMPLEMENTATION_AUTHORIZED = NO
```

Los publication gates continúan `PENDING` hasta sus etapas competentes. Un PASS documental no
autoriza publicación automática, PN-14, implementación, activación productiva ni cutover.

## 17. Prohibición expresa de implementación

```text
IMPLEMENTATION_AUTHORIZED = NO
```

Durante PN-13 permanecen prohibidos:

```text
source code changes
test changes
migrations / schema / SQL changes
runtime configuration changes
dependency changes
Stripe/provider operations
email/push delivery
data audit or data mutation
frontend/mobile changes
productive activation
cutover/fence activation
```

No puede existir implementación hasta que:

1. PN-13 sea materializada;
2. sea auditada fresh e independientemente;
3. sea aceptada por la transición canónica competente;
4. complete el lifecycle de publicación aplicable;
5. PN-14 autorice explícitamente un slice físico y su allowlist.

PN-14 no se infiere como siguiente acción por el cierre documental. Requiere autoridad separada.

## 18. Git y publicación

Durante PN-13:

- no `git add`, commit o push por parte del DOCUMENTER salvo una etapa posterior con rol/política
  explícitamente autorizados;
- no pull, fetch, merge, rebase, cherry-pick, checkout, reset, clean o stash;
- no force push;
- no integración de branches;
- no interacción con archivos inéditos F2E o Autopilot;
- snapshot before/after de `HEAD`, staging y working tree;
- un baseline dirty sólo se acepta si está previamente identificado y autorizado;
- cualquier touched path fuera del allowlist exacto es `SECURITY_STOP`;
- publicación y cierre siguen gates separados y nunca implican runtime, productividad o cutover.

## 19. Pre-flight y stop conditions para PN-13

Antes de materializar PN-13, el futuro DOCUMENTER debe verificar físicamente:

1. worktree exacto autorizado;
2. branch `pagos/pagos-notificaciones-r1`;
3. baseline/HEAD exigido por la activación posterior;
4. staging y working tree contra el baseline autorizado;
5. SHA-256 de este handoff igual al hash que haya sido auditado y activado;
6. review fresh de este handoff en PASS;
7. activación expresa en `ESTADO-ACTUAL.md`;
8. allowlist exacta de seis archivos;
9. ausencia de otra mutación concurrente en este worktree;
10. canónicos y evidencia física aplicables.

Debe detenerse sin escribir si:

- este handoff no está auditado, aprobado o activo;
- branch, HEAD, staging o baseline no coinciden;
- aparece un path dirty no autorizado;
- la activación intenta cerrar o ejecutar F2E desde esta lane;
- falta una decisión necesaria para A–J;
- la autoridad aceptada contradice un canónico sin reconciliación explícita;
- se requiere tocar código, tests, migraciones, configuración, datos u otro repositorio;
- el allowlist no basta para materializar autoridad autocontenida;
- se intenta convertir un gate no aplicable en PASS.

No se usa reset, clean, stash, checkout o edición fuera de scope para eludir una stop condition.

## 20. Exit conditions de este handoff bootstrap

Este handoff bootstrap queda correctamente materializado sólo si:

- es el único path creado/modificado por PN-12.2;
- branch y `HEAD` permanecen en el baseline congelado;
- staging permanece vacío;
- contiene provenance, aislamiento, lifecycle, workflow, allowlist y prohibiciones completas;
- PN-13 sigue `NOT_ACTIVE / NOT_STARTED`;
- implementación sigue `NOT_AUTHORIZED`;
- no declara audit, aprobación, activación, publicación o cierre inexistentes;
- su SHA-256 se calcula sobre el archivo físico final;
- `git diff --check` no reporta errores;
- el siguiente gate es exclusivamente un audit fresh, independiente y read-only en una nueva
  conversación.

Estado que este artefacto preserva:

```text
PN-12.2 HANDOFF DOCUMENT: MATERIALIZED
PN-12.2 SELF-AUDIT: NOT_PERFORMED
HANDOFF DOCUMENT AUDIT: PENDING
HANDOFF: NOT_APPROVED / NOT_ACTIVE
PN-13: NOT_ACTIVE / NOT_STARTED
PN-14: NOT_REACHED / NOT_AUTHORIZED
IMPLEMENTATION: NOT_STARTED / NOT_AUTHORIZED
PRODUCTIVE AUTHORITY: UNCHANGED
CUTOVER: false
```

No se ejecutan Maven, tests, Docker, SQL, Stripe, email, push, data audit o implementación por
esta materialización. No se autoriza `git add`, commit ni push.
