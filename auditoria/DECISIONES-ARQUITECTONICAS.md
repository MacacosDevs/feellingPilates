# FeelingPilates — Decisiones arquitectónicas

Status: CANONICAL
Last updated: 2026-09-15
Repository verification: VERIFIED
Last verified against commit:
a0ec85818b771d4ac924b427fa1e90244ea9fe8e
Verification scope: decisiones aceptadas, F2D preservado y contrato PN-13 diseñado/no implementado

## Estados

### Estado de decisión

- `PROPUESTA`
- `ACEPTADA`
- `SUPERSEDIDA`
- `DESCARTADA`

### Estado de materialización

- `NO_APLICA`
- `NO_INICIADA`
- `PARCIAL`
- `IMPLEMENTADA`
- `VERIFICADA`

La aceptación de una decisión y su implementación son dimensiones independientes.

---

# DA-001 — Monolito modular antes que microservicios prematuros

**Estado de decisión:** ACEPTADA
**Estado de materialización:** PARCIAL

**Decisión**

FeelingPilates evoluciona inicialmente como monolito modular.

No se introduce por defecto:

- arquitectura distribuida;
- broker;
- microservicios;
- coordinación remota entre dominios.

**Motivo breve**

Los principales problemas actuales son de límites de dominio, integridad y transición legacy, no de escala operacional que requiera distribución.

**Impacta a**

Backend completo y futuras separaciones de módulos.

**Origen**

Dirección arquitectónica general de la reestructuración.

---

# DA-002 — Horario operativo y programación son conceptos independientes

**Estado de decisión:** ACEPTADA
**Estado de materialización:** VERIFICADA

**Decisión**

El horario durante el cual un salón puede operar no constituye por sí mismo programación de actividades.

La programación debe respetar el horario operativo, pero ambos dominios se modelan separadamente.

**Motivo breve**

Permite representar correctamente aperturas, cierres, excepciones y múltiples programaciones dentro de la misma ventana operativa.

**Impacta a**

Salones, horarios, programación, reservas.

**Origen**

F1/F2; materializado especialmente en F2A–F2C.

---

# DA-003 — Regla recurrente no equivale a sesión concreta

**Estado de decisión:** ACEPTADA
**Estado de materialización:** PARCIAL

**Decisión**

Una regla recurrente define programación nominal/repetitiva.

Una futura sesión representa una ocurrencia concreta.

No deben conflarse.

**Motivo breve**

Confirmaciones, capacidad, historial y reservas necesitan identidad concreta sin mutar la recurrencia.

**Impacta a**

Programación, futuras sesiones, reservas.

**Origen**

Diseño de programación de F1/F2.

---

# DA-004 — Migración incremental y preservación de consumidores

**Estado de decisión:** ACEPTADA
**Estado de materialización:** VERIFICADA

**Decisión**

Las áreas legacy se sustituyen de forma incremental.

Un contrato consumido no se elimina hasta disponer de una estrategia de migración/cutover validada.

**Motivo breve**

Evitar Big Bang rewrites y desacoplar evolución interna del despliegue de consumidores.

**Impacta a**

Backend, frontend, mobile y migraciones.

**Origen**

Aplicada explícitamente en F2B/F2C.

---

# DA-005 — PostgreSQL protege invariantes temporales críticas

**Estado de decisión:** ACEPTADA
**Estado de materialización:** VERIFICADA

**Decisión**

Cuando una invariante temporal/concurrente puede violarse pese a prechecks de aplicación, PostgreSQL participa como defensa de integridad mediante constraints apropiadas.

**Motivo breve**

Un precheck Java aislado no cierra carreras concurrentes.

**Impacta a**

Horarios operativos y futuros modelos temporales.

**Origen**

F2B.

---

# DA-006 — Vigencias explícitas, sin sentinels artificiales

**Estado de decisión:** ACEPTADA
**Estado de materialización:** VERIFICADA

**Decisión**

Las vigencias temporales se representan mediante límites de fecha explícitos y `null` cuando corresponde representar un extremo abierto.

No se utilizan fechas centinela como sustituto de infinito.

**Motivo breve**

Semántica temporal más precisa y constraints PostgreSQL coherentes.

**Impacta a**

Horario operativo y programación versionada.

**Origen**

F2B.

---

# DA-007 — Resolver temporal único para el horario operativo

**Estado de decisión:** ACEPTADA
**Estado de materialización:** VERIFICADA

**Decisión**

Los consumidores no deben recomponer de forma independiente:

```text
excepción puntual
+
horario semanal
```

El horario efectivo se obtiene mediante una única composición temporal.

**Motivo breve**

Evitar semánticas divergentes entre consumidores.

**Impacta a**

Programación, reservas y salones.

**Origen**

F2B/F2C.

---

# DA-008 — Locking compartido por todos los writers que compiten

**Estado de decisión:** ACEPTADA
**Estado de materialización:** PARCIAL

**Decisión**

Un lock sólo cierra una carrera si todos los writers que compiten por la misma invariante participan del protocolo compatible.

**Motivo breve**

Un writer protegido y otro no protegido siguen permitiendo TOCTOU y estados imposibles.

**Impacta a**

Horarios, excepciones, reservas y futuras operaciones concurrentes.

**Origen**

F2B/F2C y reafirmado por reviews posteriores.

---

# DA-009 — `Clock` inyectado para reglas temporales

**Estado de decisión:** ACEPTADA
**Estado de materialización:** VERIFICADA EN ÁREAS REFACTORIZADAS

**Decisión**

Las reglas de negocio que dependen de la fecha utilizan un `Clock` inyectado.

No deben depender de llamadas dispersas al reloj real del sistema.

Para Payments & Notifications, la zona backend/de negocio es obligatoriamente
`America/Mexico_City`: cada comando captura un solo instante causal y falla cerrado si faltan o son
inválidos `Clock`, zona, duración, versión de política o resolución local. Los instantes se
persisten normalizados a UTC con precisión de microsegundos, mientras zona y versión se conservan
como provenance para reproducir aritmética calendario.

En la vigencia de derechos, `fechaVencimiento` es el último día calendario válido incluido. Un
compromiso nuevo exige `instanteCompromiso < inicioDelDia(fechaVencimiento + 1)` en esa zona. Si el
compromiso fue válido, su sesión puede iniciar hasta `limitePostVencimientoDias = N` días
calendario después, inclusive: `inicioSesion < inicioDelDia(fechaVencimiento + N + 1)`. La
igualdad con cualquiera de esos límites exclusivos se rechaza.

**Motivo breve**

Determinismo, testabilidad y semántica temporal consistente.

**Impacta a**

Horarios, excepciones y futuros writers temporales.

**Origen**

F2B/F2C.

---

# DA-010 — Traducción de constraints sólo con evidencia inequívoca

**Estado de decisión:** ACEPTADA
**Estado de materialización:** VERIFICADA EN F2C

**Decisión**

Una excepción de integridad se traduce a un código de negocio específico únicamente cuando existe evidencia estructurada suficiente de la constraint concreta.

No basta un SQLSTATE genérico.

**Motivo breve**

Evitar ocultar violaciones de integridad no relacionadas bajo un error incorrecto.

**Impacta a**

Persistencia y errores API.

**Origen**

F2C.2.1.

---

# DA-011 — Códigos de error estables separados del mensaje humano

**Estado de decisión:** ACEPTADA
**Estado de materialización:** VERIFICADA EN ÁREAS MIGRADAS

**Decisión**

Los consumidores programáticos deben depender de códigos de error estables, no de análisis textual de mensajes humanos.

**Motivo breve**

Mantener contratos API robustos frente a cambios de wording.

**Impacta a**

Backend y frontend.

**Origen**

F2B/F2C.

---

# DA-012 — No modificar automáticamente templates recurrentes por eventos puntuales

**Estado de decisión:** ACEPTADA
**Estado de materialización:** PARCIAL

**Decisión**

Una excepción puntual de operación o programación no debe modificar, recortar o reversionar automáticamente la plantilla recurrente de semanas posteriores.

**Motivo breve**

Preservar la intención de una regla recurrente y separar temporalidades.

**Impacta a**

Programación y horario operativo.

**Origen**

F2C y diseño de F2D.

---

# Decisiones supersedidas relevantes

## DA-S-001 — Recortar automáticamente una ocurrencia recurrente al horario disponible

**Estado de decisión:** SUPERSEDIDA
**Estado de materialización:** NO\_APLICA

**Decisión anterior**

Una ocurrencia recurrente parcialmente fuera del horario efectivo podía considerarse recortable.

**Superseded by**

DA-012 y regla funcional correspondiente.

**Estado actual**

No se recorta automáticamente.

---

## DA-S-002 — Utilizar `TurnoInstructor.EXCEPCION` como mecanismo suficiente de reemplazo puntual futuro

**Estado de decisión:** SUPERSEDIDA
**Estado de materialización:** NO\_APLICA

**Decisión anterior**

Se consideró que el mecanismo legacy podría cubrir ajustes puntuales.

**Estado actual**

F2D.1 demostró que su semántica no permite expresar de forma adecuada el modelo futuro de reemplazo individual.

El diseño sustituto F2D fue aprobado posteriormente por el gate final de F2D.1. La afirmación histórica de que su materialización no había iniciado corresponde al corte anterior: F2D.2 ya está implementada internamente en dark launch y cerrada documentalmente.

---

# DA-013 — Ajustes F2D y F2D.2 como dark launch aislado

**Estado de decisión:** ACEPTADA
**Estado de materialización:** VERIFICADA (F2D.2 `CERRADA / DARK_LAUNCH`)

**Decisión**

F2D.2 materializa internamente el diseño aprobado de ajustes puntuales como dark launch. Su estado vigente es `CERRADA`: la implementación cuenta con aprobación técnica y documental, publicación verificada y cierre de publicación `PASS`. Este cierre no cambia runtime, productividad, cutover ni autoridad. Ningún estado exclusivo de `programacion_*` puede permitir, rechazar, modificar, ocultar o transformar un flujo productivo legacy durante F2D.2.

Por tanto, F2D.2 excluye controllers públicos, consumers productivos, adapters sobre writers legacy, `ImpactoAjustesEnExcepcionHorario`, `Reserva` legacy, frontend/mobile, cutover y fence persistido. `ImpactoAjustesEnExcepcionHorario` queda diferido a una futura fase de activación/cutover con fence efectivo.

La identidad y composición aprobadas son:

- target de `CANCELACION`/`REEMPLAZO`: `serieId + fecha` sobre la ocurrencia nominal;
- identidad recurrente/reemplazo: `serieId + fecha`;
- identidad de adición: `ajusteId + fecha`;
- `ProgramacionEfectiva`: `NOMINAL → AJUSTES → OPERATIVO FINAL`, aplicando el horario del salón resultado y validación fail-closed de maestros vigentes.

V47 incluye el EXCLUDE temporal por serie requerido y no introduce una FK directa desde el ajuste a `serieId`.

La concurrencia aprobada exige:

- Policy A inversa;
- `SalonLocks` deduplicados y ordenados;
- `InstructorLocks` deduplicados y ordenados;
- orden global `SALONES → INSTRUCTORES`;
- participación de todos los writers recurrentes competidores;
- relectura bajo locks y comparación del lock set para cerrar TOCTOU.

`Reserva` legacy permanece fuera de F2D.2. Se prohíben la doble autoridad productiva y cualquier activación implícita de la programación nueva.

**Motivo breve**

Permitir la materialización interna y verificable del modelo nuevo sin que datos dark-launch alteren la autoridad legacy antes de una activación controlada.

**Impacta a**

F2D.2, programación efectiva, persistencia futura, concurrencia y futura activación/cutover.

**Origen**

Checkpoint F2D.1, F2D.1.1, F2D.1.2, intervención F2D.2.2, checkpoint de implementación F2D.2, gate técnico final `P0=0 / P1=0 / P2=0` y review documental F2D.2 `P0=0 / P1=0 / P2=1 editorial` con ambos gates documentales en `PASS`.

---

# DA-014 — Separación de orden, compra, pago y acreditación

**Estado de decisión:** ACEPTADA
**Estado de materialización:** NO_INICIADA (`PN-13 / DISEÑADO_NO_IMPLEMENTADO`)

**Decisión**

El modelo futuro separa:

| Tipo final | Responsabilidad exclusiva |
| --- | --- |
| `OrdenVenta` | Checkout/ticket, cliente, líneas comerciales y agrupación de intentos. |
| `Compra` | Producto comercial adquirido y su contrato histórico inmutable. |
| `CompraComponenteSnapshot` | Actividad y cantidad adquiridas, sin lectura normativa del catálogo actual. |
| `Pago` | Intento/estado de liquidación monetaria por un método. |
| `Acreditacion` | Aplicación durable e idempotente de derechos después del pago confirmado. |

Una orden puede tener varios intentos de pago, pero como máximo un settlement efectivo. Split
payment queda fuera del MVP. `orden_venta.cliente_id` es la identidad canónica `NOT NULL`;
`Compra` y `DerechoActividad` copian ese `cliente_id` de forma inmutable y la persistencia futura
lo protege con FKs compuestas, no sólo con validación de aplicación.

El settlement se representa mediante `orden_venta.pago_settlement_id` nullable, único, y FK
compuesta al `Pago` de la misma orden. Confirmar dinero bloquea primero `OrdenVenta`; sólo un
pointer nulo puede asignarse, el mismo pago es replay y otro pago conserva la verdad monetaria pero
entra como excepción financiera sin acreditación. El slot no se libera por cambios de estado
posteriores: un éxito tardío o segundo nunca crea otro settlement, no sustituye al válido ni
duplica derechos. Se concilia y reembolsa el `Pago` anómalo bajo scope exclusivo de ese pago; este
proceso no bloquea, revoca ni altera derechos del settlement válido. Sólo derechos que hubieran
nacido indebidamente del pago anómalo pueden contenerse/revocarse por esa provenance.

`PAGADA != ACREDITADA`: confirmar dinero crea trabajo durable de acreditación; sólo la transición
causal única `Acreditacion.COMPLETADA` materializa el beneficio. Esta denominación supersede el
estado `APLICADA` usado previamente en PN-13.

Queda prohibido que `Compra` vuelva a ser simultáneamente catálogo mutable, intento Stripe,
estado monetario, saldo y vigencia editable.

**Impacta a**

Pagos, ventas de caja, catálogo, Stripe, derechos y comunicación al cliente.

**Origen**

PN-13; obligaciones A–D de PN-12.1.

---

# DA-015 — Derechos por actividad y ledger inmutable

**Estado de decisión:** ACEPTADA
**Estado de materialización:** NO_INICIADA (`PN-13 / DISEÑADO_NO_IMPLEMENTADO`)

**Decisión**

`DerechoActividad` es un lote por actividad con procedencia a compra/componente, cantidad emitida
`G` y política de vigencia. Su proyección tiene seis buckets enteros no negativos: `D` disponible,
`K` comprometido por compromiso activo, `B` bajo contención refund/disputa, `C` consumido,
`E` expirado y `R` revocado. Después de cada append y del fold íntegro del ledger debe cumplirse:

```text
G = D + K + B + C + E + R
```

Para cantidad entera positiva `q`, los vectores exactos en orden `(D,K,B,C,E,R)` son:

| Movimiento | Delta buckets | Delta G |
| --- | --- | --- |
| `ACREDITACION_COMPRA` | `(+q,0,0,0,0,0)` | `+q` |
| `COMPROMISO_RESERVA` | `(-q,+q,0,0,0,0)` | `0` |
| `LIBERACION_CANCELACION_CLIENTE_VALIDA` / `RESTAURACION_CANCELACION_ESTUDIO` antes de expirar | `(+q,-q,0,0,0,0)` | `0` |
| los cuatro `CONSUMO_*` | `(0,-q,0,+q,0,0)` | `0` |
| `EXPIRACION` | `(-q,0,0,0,+q,0)` | `0` |
| `BLOQUEO_REEMBOLSO` / `BLOQUEO_DISPUTA` | `(-q,0,+q,0,0,0)` | `0` |
| `RESTAURACION_BAJO_CONTENCION` | `(0,-q,+q,0,0,0)` | `0` |
| `RESTAURACION_TRAS_REVOCACION_FINANCIERA` | `(0,-q,0,0,0,+q)` | `0` |
| `REVOCACION_REEMBOLSO` / `REVOCACION_DISPUTA` | `(0,0,-q,0,0,+q)` | `0` |
| `DESBLOQUEO_REEMBOLSO_FALLIDO` / `DESBLOQUEO_DISPUTA_GANADA_O_RETIRADA` | `(+q,0,-q,0,0,0)` | `0` |

Si al desbloquear el `Clock` ya alcanzó el límite exclusivo, la misma transacción agrega después
`EXPIRACION`; nunca devuelve cantidad expirada a elegibilidad. La recuperación es un par causal y
atómico con igual `q`: en el derecho original `TRANSFERENCIA_RECUPERACION =
(0,-q,0,0,0,+q)` y en un derecho nuevo `ACREDITACION_RECUPERACION = (+q,0,0,0,0,0)` con
`delta G=+q`. `compromiso_origen_id` y `causal_operation_id` son únicos; el valor vivo agregado
`V=D+K+B` no cambia y no existe revival ni doble crédito.

`MovimientoCredito` es el hecho inmutable que acredita, compromete, libera, consume, expira,
recupera, bloquea, revoca o compensa. `MovimientoVigencia` conserva toda alteración de vigencia.
`CompromisoReserva` enlaza una reserva exacta con un derecho exacto.

El saldo no es un contador arbitrariamente editable. Las proyecciones `disponible`,
`comprometido`, `bajo contención financiera`, `consumido`, `expirado` y `revocado` deben
reconciliarse por separado con el ledger. Toda
corrección usa movimientos compensatorios con causa e idempotencia. Una
`COMPENSACION_ADMINISTRATIVA` es exclusivamente el inverso exacto de un movimiento objetivo
reversible, sobre mismo derecho/cantidad, con `movimiento_objetivo_id` único, actor, permiso y causa;
se aplica en orden causal inverso y se rechaza si vuelve negativo un bucket o contradice estado
dependiente. `C`, `E` y `R` son terminales salvo esa compensación explícita. Cada causa tiene
identidad única y hash de payload: replay idéntico no agrega, payload contradictorio falla cerrado.
La selección FEFO y las transiciones preservan no negatividad y conservación.

Categorías mínimas de `MovimientoCredito`:

```text
ACREDITACION_COMPRA
COMPROMISO_RESERVA
LIBERACION_CANCELACION_CLIENTE_VALIDA
RESTAURACION_CANCELACION_ESTUDIO
CONSUMO_ASISTENCIA
CONSUMO_NO_ASISTIDA
CONSUMO_CANCELACION_TARDIA
CONSUMO_LIMITE_CANCELACIONES
EXPIRACION
ACREDITACION_RECUPERACION
BLOQUEO_REEMBOLSO
RESTAURACION_BAJO_CONTENCION
RESTAURACION_TRAS_REVOCACION_FINANCIERA
REVOCACION_REEMBOLSO
DESBLOQUEO_REEMBOLSO_FALLIDO
BLOQUEO_DISPUTA
REVOCACION_DISPUTA
DESBLOQUEO_DISPUTA_GANADA_O_RETIRADA
TRANSFERENCIA_RECUPERACION
COMPENSACION_ADMINISTRATIVA
```

**Impacta a**

Pagos, reservas, asistencia, cancelación, expiración y reembolsos.

**Origen**

PN-13; obligaciones E–H de PN-12.1.

---

# DA-016 — Monolito transaccional y fronteras con Reservas

**Estado de decisión:** ACEPTADA
**Estado de materialización:** NO_INICIADA (`PN-13 / DISEÑADO_NO_IMPLEMENTADO`)

**Decisión**

Se conserva DA-001: Pagos y Notificaciones se implementan inicialmente dentro del monolito Spring
y PostgreSQL. No se crea un segundo dominio de reservas o clases.

Reservas/Programación conserva fecha, horario, salón, instructor, capacidad, estado operacional y
alcance de cancelación del estudio. Pagos conserva elegibilidad, FEFO, compromiso, liberación,
consumo, recuperación y reembolso de derechos. La integración usa el puerto
`GestorCreditoReserva` con operaciones idempotentes `verificarElegibilidad`, `comprometer`,
`liberarPorCancelacionValida`, `consumirPorAsistencia`, `consumirPorNoAsistencia`,
`consumirPorCancelacionTardia` y `restaurarPorCancelacionEstudio`.

Reservas/Asistencia también posee `PENDIENTE_ASISTENCIA`: sólo una reserva activa/no cancelada y
una sesión terminada sin resolución puede entrar; `K` permanece comprometido indefinidamente. Sólo
`ADMIN`, nunca instructor, sale a `ASISTIDA` o `NO_ASISTIDA` en la misma transacción que consume.
Los tres terminales de cancelación tienen precedencia y prohíben pending. No hay timer ni inferencia
por tiempo. Corrección tardía exige ADMIN, razón/auditoría/idempotencia y no reescribe ledger;
normalmente conserva consumo y sólo agrega compensación cuando cambia explícitamente el crédito.

Mientras comparten base de datos, son atómicas las siguientes unidades:

- reserva + compromiso;
- cancelación + liberación/consumo;
- asistencia/no-asistencia + consumo final;
- pago confirmado + creación durable del trabajo de acreditación;
- aplicación de acreditación + derechos + ledger + evento/outbox;
- hecho de dominio + `MensajeOutbox`.

Email y push nunca ocurren dentro de una transacción de negocio.

**Impacta a**

Reservas legacy y futuras, Pagos, transacciones y Notificaciones.

**Origen**

PN-13; obligación F de PN-12.1.

---

# DA-017 — Estrategia de concurrencia PostgreSQL para Pagos

**Estado de decisión:** ACEPTADA
**Estado de materialización:** NO_INICIADA (`PN-13 / DISEÑADO_NO_IMPLEMENTADO`)

**Decisión**

El MVP usa aislamiento `READ COMMITTED`, constraints únicas/check como última defensa y locks
pesimistas de filas canónicas para operaciones contenciosas. No se depende de un precheck Java.

- último crédito: bloquear la fila ancla `cuenta_credito_actividad(cliente_id,actividad_id)`,
  revalidar cliente/tiempo/política, y después derechos FEFO en orden `(expira_en,id)`; nunca usar
  `SKIP LOCKED` en el path foreground;
- cuota mensual: insertar idempotentemente si falta y bloquear la fila única
  `cliente + YearMonth(inicio_sesion en America/Mexico_City) + politica_version`; revalidar la
  versión de sesión antes de incrementar;
- extensión: bloquear derechos del cliente/actividad en orden estable;
- settlement: bloquear `OrdenVenta` antes de confirmar cualquier `Pago`;
- acreditación: unicidad por `pago_id` y por identidad idempotente;
- reembolso/disputa: compartir `OrdenVenta` como mutex y el `Pago` de origen como boundary de
  ownership; después Compras UUID, proceso financiero y Derechos `(expira_en,id)` antes de decidir
  contención/producto entero;
- Inbox/Outbox/workers: claim condicional o `FOR UPDATE SKIP LOCKED`, lease recuperable y transición
  idempotente;
- toda clave de idempotencia combina operación + contexto + hash de payload; reutilización con
  payload contradictorio falla cerrada.

Órdenes de lock suficientes y no contradictorios:

- settlement/acreditación/transferencia/Stripe: `OrdenVenta -> Pago -> Acreditacion -> Compras UUID -> Derechos UUID`;
- refund/disputa: `OrdenVenta -> Compras UUID -> Pago -> Reembolso|Disputa UUID -> Derechos (expira_en,id)`;
- reserva/asistencia/cancelación: `Reserva/sesión -> cuenta_credito_actividad -> Derechos (expira_en,id) -> CompromisoReserva -> allowance`;
- operaciones multi-cliente/actividad ordenan primero `(cliente_id,actividad_id)` globalmente.

Cada transacción usa sólo su secuencia y nunca toma una fila anterior después de una posterior.
Ningún writer competidor puede omitir el protocolo, en coherencia con DA-008.

**Impacta a**

Persistencia, repositorios, workers, reservas y refunds.

**Origen**

PN-13 y DA-005/DA-008.

---

# DA-018 — Máquina monetaria monotónica, Stripe Inbox y reconciliación

**Estado de decisión:** ACEPTADA
**Estado de materialización:** NO_INICIADA (`PN-13 / DISEÑADO_NO_IMPLEMENTADO`)

**Decisión**

React Native usa PaymentSheet y PaymentIntent. El servidor fija monto, moneda, orden y claves
estables. El éxito cliente sólo es UX. El webhook verifica `Stripe-Signature` contra el raw body y,
si es válido, persiste `EventoStripeRecibido` antes de procesamiento complejo. `event.id` es único;
la idempotencia de dominio sigue siendo la defensa final.

Se soportan `payment_intent.succeeded`, `payment_intent.payment_failed`,
`payment_intent.processing` y cancelación. Un fallo de intento no vuelve terminal a toda la orden.
Un evento antiguo nunca regresa un pago confirmado. Estado ambiguo se reconcilia consultando el
objeto Stripe actual.

Contradicciones de monto, moneda, asociación, segundo pago confirmado, webhook no correlacionable,
transición imposible o ledger inválido pasan a `REQUIERE_REVISION`; no se adivina una transición.
Métodos Stripe de notificación diferida y métodos guardados quedan post-MVP.

`Pago.CANCELADO` sólo representa cancelación/no-capture confirmada por proveedor; una disposición
local técnica o comercial de `OrdenVenta` no demuestra verdad monetaria. Excepción monotónica:
`CANCELADO -> CONFIRMADO` sólo al reconciliar el mismo PaymentIntent `succeeded` con orden, importe
y moneda exactos. Bajo lock `OrdenVenta -> Pago`, una causa técnica asigna el settlement sólo si el
slot está vacío, avanza orden/compras y crea una única acreditación; una cancelación comercial
explícita conserva el hecho monetario, lleva orden/compra a
`REQUIERE_REVISION_PAGO_TARDIO_REEMBOLSO`, no acredita y crea el refund idempotente del pago
anómalo. Si ya existe otro settlement efectivo, se preserva íntegramente: el segundo éxito se
clasifica como excepción financiera, no ocupa el slot, no acredita y exige conciliación/refund
payment-scoped del `Pago` anómalo. Ese refund nunca bloquea, revoca o altera derechos del
settlement válido; sólo contiene/revoca derechos atribuibles al pago anómalo si se produjeron sin
autorización. Mismatch o derechos incompatibles falla cerrado. `event.id`, referencia de
proveedor, efecto causal, `pago_settlement_id` y `acreditacion.pago_id` únicos impiden regresión y
doble efecto; la historia financiera/ledger permanece inmutable.

Transferencia se persiste como `transferencia_pago` 1:1 con `Pago`, ciclo, versión y evidencia
vigente; `evidencia_transferencia` e `intento_validacion_transferencia` son append-only e
independientes. Registrar requiere permiso propio y no acredita; confirmar/rechazar exige permiso
de validación y estado esperado. Sólo `RECHAZADA -> PENDIENTE_VALIDACION` reabre con permiso,
razón, actor e incremento de ciclo; `CONFIRMADA` nunca reabre. Confirmar serializa con el settlement
común; si la orden ya fue liquidada por otro `Pago`, la transferencia termina en resultado no
acreditante `RECHAZADA`/contenida con razón auditable `ORDEN_YA_LIQUIDADA`, y el `Pago` registra el
incidente financiero operacional sin convertirse en settlement efectivo. No pasa a
`transferencia_pago.CONFIRMADA`, no crea `Acreditacion` ni derechos y no sustituye el settlement
válido. Si existe dinero externo se concilia/reembolsa bajo scope de ese `Pago`, pero nunca se
acredita. Unicidades por pago/hash, referencia, operación+ciclo+clave y payload hash hacen replay
no-op y contradicción fail-closed.

**Impacta a**

Stripe, webhook, pagos, recovery y soporte operacional.

**Origen**

PN-13; obligación D de PN-12.1.

---

# DA-019 — Reembolso y disputa contienen derechos sin borrar historia

**Estado de decisión:** ACEPTADA
**Estado de materialización:** NO_INICIADA (`PN-13 / DISEÑADO_NO_IMPLEMENTADO`)

**Decisión**

`Reembolso` tiene UUID, identidad causal y permisos y siempre conserva el `Pago` financiero de
origen. El alcance se determina por tipo y nunca se amplía a todos los derechos del cliente:

- `NORMAL_PRODUCTO_ENTERO`: requiere el settlement efectivo y `reembolso_compra` normaliza 1..N
  Compras completas de la misma orden; el importe es su suma contractual completa, nunca
  componentes, y sólo contiene derechos atribuibles a esas Compras tras verificar obligatoriamente
  `PRODUCTO_ENTERO`;
- `PAGO_ANOMALO`: queda payment-scoped al `Pago` tardío/segundo; no contiene, revoca ni altera
  derechos del settlement válido, salvo derechos no autorizados atribuibles específicamente al
  pago anómalo;
- `EXTERNO_DASHBOARD`: correlaciona primero el `Pago` originario y deriva desde él las Compra(s) y
  derechos exactos; correlación ambigua queda `REQUIERE_REVISION` sin ampliar el scope.

El reembolso normal no tiene ventana temporal adicional. Al aprobarlo se bloquean sólo derechos
elegibles dentro de su scope exacto. Stripe usa Refund e Inbox/reconciliación; un timeout queda
`PENDIENTE_CONFIRMACION`, conserva la contención y reutiliza la misma identidad estable.

Éxito revoca mediante ledger y confirma el reembolso. Fallo desbloquea mediante compensación
auditable. Un reembolso externo se concilia. Servicio ya consumido nunca se elimina de la historia.

Refund y disputa sobre el mismo origen financiero comparten `contencion_financiera`, el mismo
mutex `OrdenVenta` y boundary de ownership del `Pago`, con scopes normalizados por
Pago/Compra/Derecho y unicidad activa. Éxito de refund agrega una sola revocación; fallo definitivo
agrega el desbloqueo idempotente. Refund de Dashboard se correlaciona por `stripe_refund_id` único;
mapping parcial, ambiguo o con consumo preserva verdad externa y entra a `REQUIERE_REVISION`.

`Disputa` tiene UUID, `stripe_dispute_id` único y se identifica por el cargo externo/`Pago`
disputado. Su scope sólo incluye Compra(s) y derechos atribuibles a ese settlement efectivo. Su
máquina monotónica es `DETECTADA -> CONTENIDA -> EN_SEGUIMIENTO -> GANADA | PERDIDA | RETIRADA |
REQUIERE_REVISION`. Contiene sólo `D` elegible del origen, no cancela reservas, no altera `C` ni
afecta compras/settlements ajenos; `GANADA` o `RETIRADA` desbloquea, `PERDIDA` revoca con
`REVOCACION_DISPUTA`. Toda carrera, ambigüedad de ownership/scope o segundo owner se resuelve tras
el lock común como replay/correlación o `REQUIERE_REVISION`; nunca hay ampliación de scope, doble
revocación o compensación. El historial consumido se preserva. El workflow completo de evidencia
queda post-MVP.

**Impacta a**

Pagos, derechos, Stripe, auditoría y notificaciones internas.

**Origen**

PN-13; obligación H de PN-12.1.

---

# DA-020 — Outbox y entrega at-least-once deduplicada

**Estado de decisión:** ACEPTADA
**Estado de materialización:** NO_INICIADA (`PN-13 / DISEÑADO_NO_IMPLEMENTADO`)

**Decisión**

`EventoDominio` y `MensajeOutbox` preceden tres niveles distintos: `Notificacion` (mensaje lógico),
`EntregaLogica` (una por canal/endpoint lógico y por dispositivo push) e `IntentoEntrega` (fila
inmutable por ejecución). Outbox se escribe atómicamente con el hecho fuente y sólo se procesa
después del commit. Las dedupe keys de Notificacion y EntregaLogica son independientes;
`IntentoEntrega` es único por `(entrega_logica_id,secuencia)`, empieza en 1, crece monótonamente y
nunca se recicla. La idempotencia del proveedor, cuando existe, se mantiene estable al scope de
EntregaLogica.

La garantía es at-least-once con deduplicación, nunca exactly-once end-to-end. Existen claves
únicas independientes a nivel notificación y entrega. Retries tienen backoff configurable, máximo
finito y clasificación transitoria/permanente/incierta. Un resultado transitorio termina ese
intento y crea el siguiente sólo si queda presupuesto; uno permanente termina la entrega; uno
incierto se reconcilia y, si el retry pudiera duplicar, pasa a revisión. Workers usan claim
condicional y lease recuperable.

La agregación de `Notificacion` es mutuamente exclusiva y aplica esta precedencia:

1. `REQUIERE_REVISION` si cualquier `EntregaLogica` agotó retries automáticos con outcome incierto;
   precede la agregación terminal y no se reintenta cuando podría duplicar;
2. `SIN_CANALES_DISPONIBLES` sólo al planificar sin canal/endpoint usable, antes de crear una
   `EntregaLogica` usable; nunca describe una entrega ya creada que después falló;
3. `ENTREGADA` si todas las entregas usables creadas tuvieron éxito;
4. `PARCIALMENTE_ENTREGADA` si al menos una tuvo éxito, ninguna quedó incierta y cada no exitosa
   terminó en fallo conocido;
5. `FALLA_PERMANENTE` si se creó al menos una entrega usable, ninguna tuvo éxito ni quedó incierta
   y todas terminaron en fallo conocido.

Un endpoint inválido descubierto después de crear la entrega se desactiva cuando corresponda y
esa entrega queda en fallo terminal conocido; no se reclasifica retroactivamente como
`SIN_CANALES_DISPONIBLES`. Los `IntentoEntrega` inmutables no cambian. Los estados terminales son
monotónicos.

Recordatorios y expiraciones se revalidan antes de enviar. Cambio de reserva, vigencia o versión
de agenda invalida trabajo obsoleto. Push token pertenece al dispositivo y se desactiva si el
proveedor lo declara inválido. Email/push se esconden tras puertos; el proveedor concreto es una
decisión de infraestructura posterior. Promocional y transaccional tienen preferencias separadas.

**Impacta a**

Notificaciones, todos los eventos fuente, privacidad y operación.

**Origen**

PN-13; obligación I de PN-12.1.

---

# DA-021 — Packages y nombres físicos futuros de Pagos y Notificaciones

**Estado de decisión:** ACEPTADA
**Estado de materialización:** NO_INICIADA (`PN-13 / DISEÑADO_NO_IMPLEMENTADO`)

**Decisión**

La estructura objetivo usa nombres españoles y dependencias hacia adentro:

```text
com.feelingpilates.pagos.catalogo.{dominio,aplicacion,infraestructura}
com.feelingpilates.pagos.ventas.{dominio,aplicacion,infraestructura}
com.feelingpilates.pagos.derechos.{dominio,aplicacion,infraestructura}
com.feelingpilates.pagos.reembolsos.{dominio,aplicacion,infraestructura}
com.feelingpilates.pagos.stripe.{aplicacion,infraestructura}
com.feelingpilates.notificaciones.{dominio,aplicacion,infraestructura}
```

Tipos finales autoritativos: `OrdenVenta`, `Compra`, `CompraComponenteSnapshot`, `Pago`,
`Acreditacion`, `DerechoActividad`, `MovimientoCredito`, `MovimientoVigencia`,
`CompromisoReserva`, `Reembolso`, `EventoStripeRecibido`, `EventoDominio`, `MensajeOutbox`,
`Notificacion`, `EntregaLogica`, `IntentoEntrega`, `DispositivoPush`, `PoliticaComercialVersion` y
`UsoCancelacionReembolsable`.

Dominio no depende de Spring/JPA/Stripe. Aplicación orquesta puertos y transacciones.
Infraestructura adapta JPA, PostgreSQL, Stripe y proveedores. `calendario` llama el puerto de
crédito; Pagos no duplica entidades operacionales de reserva.

**Impacta a**

Implementación física PN futura.

**Origen**

PN-13; obligación J de PN-12.1.

---

# DA-022 — Migración expand/backfill/cutover sin renumerar Flyway

**Estado de decisión:** ACEPTADA
**Estado de materialización:** NO_INICIADA (`PN-13 / DISEÑADO_NO_IMPLEMENTADO`)

**Decisión**

Las migraciones históricas no se renombran ni renumeran. Al iniciar cada slice se revalida la
mayor versión Flyway física y sólo entonces se asignan versiones nuevas; PN-13 no reserva números.

La transición sigue `expand -> backfill auditable -> validación -> coexistencia compatible ->
switch de writers/readers autorizado -> cutover/fence -> contract posterior`. Los snapshots
legacy se generan de forma determinista, conservan provenance y reportan ambigüedades como
`REQUIERE_REVISION`. No hay doble autoridad silenciosa ni rollback destructivo: antes del cutover
se desactiva el nuevo path; después se corrige hacia adelante y se preserva ledger/historia.

Cada slice tendrá allowlist y audit propios. No se retira API o tabla legacy hasta demostrar cero
consumers o compatibilidad/migración completa y obtener autorización de cutover.

**Impacta a**

Flyway, datos legacy, APIs, despliegue y retiro.

**Origen**

PN-13, DA-004 y obligación J de PN-12.1.
