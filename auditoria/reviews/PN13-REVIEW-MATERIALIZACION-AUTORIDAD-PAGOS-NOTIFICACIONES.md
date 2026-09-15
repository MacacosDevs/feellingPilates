# FeelingPilates — Review PN-13 — Materialización de autoridad de Pagos y Notificaciones

## 1. Clasificación, rol y alcance

```text
Audit: PN-13.1 — FRESH INDEPENDENT AUTHORITY AUDIT
Evidence materialization: PN-13.1.1
Audit result: FAIL

Materialization role:
AUDIT_EVIDENCE_DOCUMENTER / PROVENANCE_PRESERVER

Materialization mode:
EVIDENCE_ONLY / WRITE_ALLOWLIST_EXACT / NO_REAUDIT / NO_CORRECTION /
NO_AUTHORITY_MUTATION / NO_IMPLEMENTATION / NO_GIT_STATE_MUTATION

EVIDENCE_ONLY
NOT_NORMATIVE_AUTHORITY
NOT_OPERATIONAL_AUTHORITY
NOT_IMPLEMENTATION_AUTHORITY
```

Este archivo persiste la sustancia exacta del audit PN-13.1 ya completado. No repite el audit, no
corrige sus hallazgos, no modifica los seis canónicos auditados y no ejecuta una transición de
lifecycle. Tampoco acepta PN-13, autoriza PN-14, publica, implementa ni cambia autoridad
productiva.

## 2. Pre-flight físico auditado

```text
Repository:
/Users/jesusaldaircruzortiz/Desktop/Feelingpilates/feelingpilates-payments-notifications

Branch:
pagos/pagos-notificaciones-r1

HEAD:
a0ec85818b771d4ac924b427fa1e90244ea9fe8e

Staging:
EMPTY

Expected delta paths at audit start:
8

Source/test/migration/runtime-config delta:
NONE

git diff --check:
PASS

Mutations during PN-13.1 audit:
NONE
```

Los ocho paths observados al comenzar y terminar PN-13.1 fueron exactamente:

```text
auditoria/ARQUITECTURA-ACTUAL.md
auditoria/DECISIONES-ARQUITECTONICAS.md
auditoria/ESTADO-ACTUAL.md
auditoria/contexto/DOMINIO-FUNCIONAL.md
auditoria/contexto/MAPA-LEGACY-Y-MIGRACION.md
auditoria/fase-pn13-materializacion-autoridad-pagos-notificaciones.md
auditoria/handoffs/HANDOFF-PN13-MATERIALIZACION-AUTORIDAD-PAGOS-NOTIFICACIONES.md
auditoria/reviews/HANDOFF-PN13-MATERIALIZACION-AUTORIDAD-PAGOS-NOTIFICACIONES-REVIEW.md
```

Los dos últimos paths eran evidencia bootstrap preexistente y los otros seis componían la
materialización candidata PN-13. No se atribuyó al auditor ninguna mutación del baseline dirty.
No se ejecutaron tests, implementación, proveedores ni comandos Git mutantes.

## 3. Hashes verificados por PN-13.1

| Artefacto | SHA-256 observado | Resultado |
| --- | --- | --- |
| Handoff PN-13 | `601d285a23c87b131b4b4946d2f858ad918faea12e492d76f7e9da7acd073e22` | MATCH |
| Review independiente del handoff | `26a0e67588f7a9e5bd13c79aa9006a833d63834cf3cf1a1a19467194e27df5f5` | MATCH |
| Checkpoint PN-13 | `5f002e4c7636c472047af3ec6a266f76739030fe01aee3c03e655258ba245b86` | MATCH |
| `DOMINIO-FUNCIONAL.md` | `483ecca45aadd5e7577d63863c884c446cc3103a36fd797a400fe456114ad081` | MATCH |
| `DECISIONES-ARQUITECTONICAS.md` | `33f62338b60d7c73957291549793672a3885cc353a27d1b5543e00769d94d355` | MATCH |
| `ARQUITECTURA-ACTUAL.md` | `bf353b4cacf2e7794feed1b1e43f4500fb64c0bbc2c33bc7672665e079cfa1ce` | MATCH |
| `MAPA-LEGACY-Y-MIGRACION.md` | `e2b63183324572d1512bc00ec52a312fcd859c1b0aee450de534cc79f85ff8ce` | MATCH |
| `ESTADO-ACTUAL.md` | `3e4cf86be55a09d76f3f7219403fd21bef830938ea6fb7efcf1894a5b4f736a6` | MATCH |

```text
ALL_CANONICAL_SHA256_MATCH = YES
```

## 4. Ownership canónico observado

| Fuente | Ownership competente | Resultado PN-13.1 |
| --- | --- | --- |
| `ESTADO-ACTUAL.md` | Lifecycle, gates, handoff y autorización operacional. | CLARO |
| `contexto/DOMINIO-FUNCIONAL.md` | Producto y reglas funcionales. | CLARO, CON CONTRADICCIÓN DE REFUND |
| `DECISIONES-ARQUITECTONICAS.md` | Decisiones técnicas aceptadas. | CLARO |
| `ARQUITECTURA-ACTUAL.md` | Verdad física implementada/legacy frente a diseñada. | CLARO Y FIEL AL CÓDIGO |
| `contexto/MAPA-LEGACY-Y-MIGRACION.md` | Coexistencia, REUSE/REWORK/RETIRE, migración y cutover. | CLARO |
| Checkpoint PN-13 | Contrato detallado y trazabilidad A–J. | COMPETENTE, PERO INCOMPLETO |
| Handoff PN-13 | Scope, allowlist, prohibiciones y gates. | CORRECTO |
| Reviews | Evidencia de gates; no autoridad normativa u operacional. | CORRECTAMENTE DELIMITADO |

El audit no observó promoción incorrecta del código legacy a autoridad del diseño objetivo.

## 5. Secciones que pasaron

### 5.1 Producto y snapshot base

Pasaron la autoridad de catálogo, actividades/cantidades explícitas, precio contractual, snapshot
histórico inmutable, indivisibilidad de paquetes mixtos, ausencia de refund por componente y
exclusión operativa de Duo Reformer del MVP.

### 5.2 Autoridad Stripe núcleo

Pasaron PaymentSheet/PaymentIntent, autoridad de amount/currency en backend, raw-body signature,
Inbox durable diseñada, deduplicación de eventos, monotonicidad, reconciliación e idempotencia de
proveedor como defensa secundaria.

### 5.3 Verdad arquitectónica actual

El contraste read-only confirmó que el baseline actual mezcla compra/pago/vigencia, procesa
webhooks sin Inbox durable, no posee ledger/Outbox/push/derechos/compromisos, no descuenta compras
desde `Reserva` y sólo dispone de un stub limitado de email. `ARQUITECTURA-ACTUAL.md` no presentó
el target PN como implementado.

### 5.4 REUSE / REWORK / RETIRE

Pasaron la clasificación de las piezas mínimas, la boundary de Reservas, la inmutabilidad de
V22.1–V35, la coexistencia incremental y la clasificación de `e515152671dc5b2801f4fd7ef3e1e608bfc55a0a`
como `CONCEPTUAL_EVIDENCE_ONLY / NO_MERGE / NO_CHERRY_PICK / NO_IMPLEMENTATION_SOURCE`.

### 5.5 Slices y safety net

La estructura de doce slices resultó ordenada, dependency-aware y precedida por caracterización.
La safety net futura incluye unit, PostgreSQL, transacciones, concurrencia, Stripe, webhook,
idempotencia, recovery, Flyway, reserva-crédito, refund, Outbox y notificaciones.

### 5.6 Exclusiones MVP

Se preservaron fuera del MVP las reglas operativas Duo Reformer, suscripciones, auto-renew, gift
cards, promociones/cupones avanzados, split payment, conciliación bancaria automática, refund
prorrateado o por componente, evidencia completa de disputas, métodos Stripe diferidos, métodos
guardados, segundo dominio de reservas y edición manual de saldo.

### 5.7 Lifecycle e aislamiento

El lifecycle PN quedó correctamente separado de F2E. PN-13 permaneció materializada pero no
aceptada; PN-14 no alcanzada/no autorizada; implementación no iniciada/no autorizada. Los
worktrees prohibidos no fueron inspeccionados ni modificados.

## 6. Hallazgos exactos del audit PN-13.1

| ID | Severidad | Owner canónico | Problema exacto | Evidencia auditada | Corrección requerida |
| --- | --- | --- | --- | --- | --- |
| PN13-001 | P1 | Checkpoint / DA-015 | Las categorías de movimientos del ledger carecen de efectos contables exactos y reglas completas de conservación. | Se enumeran tipos y proyecciones, pero no el vector origen/destino ni el before/after obligatorio de cada movimiento. | Definir por tipo el efecto exacto sobre `disponible`, `comprometido`, `consumido` y `expirado/revocado`, junto con la ecuación completa de conservación. |
| PN13-002 | P1 | `DOMINIO-FUNCIONAL.md` | La vigencia carece de ancla exacta, fórmula calendario, inclusividad, fórmula de extensión y ancla de vigencia de recuperación. | Se fijan `DIAS/MESES`, scopes y reglas negativas, pero no esos límites operacionales. | Fijar instante inicial, operación calendario, endpoints inclusivos/exclusivos y cálculo exacto de extensión/recuperación. |
| PN13-003 | P1 | Dominio / boundary Reservas | `PENDIENTE_ASISTENCIA` carece de owner canónico y contrato de transición. | El handoff lo exige, pero los seis outputs no fijan su estado, transición ni relación con `CompromisoReserva`. | Definir ownership en Reservas, transiciones autorizadas y consecuencias de crédito. |
| PN13-004 | P1 | `DOMINIO-FUNCIONAL.md` | La cuota mensual de cancelaciones carece de clave temporal exacta/ancla de periodo. | “Mes calendario” no determina si la clave deriva del instante de cancelación, fecha de clase u otro instante. | Definir instante, zona, boundary e identidad exacta del periodo. |
| PN13-005 | P1 | Checkpoint / DA-018 | El estado terminal `Pago.CANCELADO` contradice la reconciliación de un `payment_intent.succeeded` tardío. | La máquina prohíbe salir de `CANCELADO`, mientras recovery afirma que Stripe confirmado prevalece. | Definir transición/reconciliación determinista para Pago, OrdenVenta, Compra y Acreditacion sin regresión ni doble crédito. |
| PN13-006 | P1 | Checkpoint / DA-014 | `PENDIENTE_VALIDACION / CONFIRMADA / RECHAZADA` de transferencia carece de modelo físico, máquina completa y reapertura exacta. | Se presenta como subestado, sin entidad/campos ni transición de reapertura. | Fijar persistencia, estados, evidencia, identidad de intento y transición de reapertura. |
| PN13-007 | P1 | `DOMINIO-FUNCIONAL.md` | El wording funcional hace opcional `PRODUCTO_ENTERO`, mientras la autoridad arquitectónica lo exige para el refund normal MVP. | “Puede exigir” contradice “sólo para producto entero”. | Hacer obligatoria la condición para el refund normal MVP o reconciliar explícitamente todo el scope. |
| PN13-008 | P1 | DA-019 / contrato físico | La relación refund-compra y `Disputa` carecen de identidad física, lifecycle, constraints y serialización común completos. | `Reembolso` usa un alcance no normalizado y `Disputa` no posee tipo/tabla/máquina/constraints propios. | Definir relación normalizada, unicidades activas, lifecycle de disputa y lock ordering refund/disputa. |
| PN13-009 | P1 | Contrato físico | El enforcement de settlement único y la identidad de cliente/FEFO de `DerechoActividad` permanecen ambiguos. | “Guard/índice compatible” delega la elección; se exige índice cliente+actividad sin columna/identidad de cliente definida. | Elegir constraint/índice exacto y fijar la identidad/columna de cliente del derecho. |
| PN13-010 | P1 | DA-020 / checkpoint | La identidad de `IntentoEntrega` contradice el modelo de retry; no se separan entrega lógica e intentos inmutables. | La identidad incluye número de intento, pero la máquina recicla la misma entidad a `PENDIENTE`. | Definir entrega lógica, filas inmutables por intento, dedupe y agregación terminal de `Notificacion`. |

## 7. Conteos y veredicto persistidos

```text
P0 = 0
P1 = 10
P2 = 0

PN_13_1_RESULT = FAIL
PN_13_ACCEPTABLE = NO
PN_14_AUTHORIZED = NO
IMPLEMENTATION_AUTHORIZED = NO
```

Los diez P1 son bloqueantes del documentation gate PN-13. La coincidencia de hashes, la fidelidad
de arquitectura actual y las secciones aprobadas no compensan ambigüedades que obligarían a un
futuro executor a inventar reglas contables, temporales, transiciones o constraints financieras.

## 8. Lifecycle siguiente permitido

Esta intervención materializa evidencia solamente. No corrige, acepta ni publica PN-13.

Una corrección posterior debe:

1. comenzar en un **NEW CHAT** separado;
2. conservar scope `DOCUMENTATION_ONLY`;
3. recibir allowlist y autoridad correctiva explícitas;
4. cerrar exclusivamente los P1 autorizados sin implementar código;
5. someter el resultado a un re-audit fresh e independiente;
6. mantener PN-14 e implementación no autorizadas hasta que los gates competentes pasen y una
   transición canónica posterior lo declare expresamente.

La creación de este archivo no actualiza `ESTADO-ACTUAL.md` ni constituye por sí misma una
transición operacional.
