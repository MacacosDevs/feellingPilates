# FeelingPilates — Dominio funcional

Status: CANONICAL
Last updated: 2026-09-15
Repository verification: VERIFIED
Last verified against commit:
a0ec85818b771d4ac924b427fa1e90244ea9fe8e
Verification scope: invariantes funcionales conocidas y autoridad funcional PN-13 diseñada/no implementada

## 1. Propósito

Este documento conserva reglas funcionales relativamente estables del producto.

No describe:

- clases Java;
- repositorios;
- locks;
- SQL;
- commits;
- branches;
- prompts;
- secuencia histórica de fases.

---

# 2. Salones

## VIGENTE — Cada salón posee su propio contexto operativo

Los horarios y la programación pertenecen a un salón concreto.

Dos salones pueden tener horarios y programación diferentes.

## VIGENTE — Un salón puede tener múltiples instructores simultáneamente

El salón no es por sí solo un recurso exclusivo entre instructores.

La coincidencia temporal de dos instructores dentro del mismo salón es válida si las demás reglas lo permiten.

---

# 3. Instructores

## VIGENTE — Un instructor puede trabajar en distintos salones según el día

No existe una asignación permanente obligatoria a un único salón.

Ejemplo funcional válido:

```text
lunes:
Juriquilla

martes:
Cimatario
```

## VIGENTE — Un instructor no puede solaparse consigo mismo

El mismo instructor no puede estar asignado a intervalos simultáneos incompatibles.

La restricción aplica incluso entre salones diferentes.

## VIGENTE — La adyacencia está permitida

Conceptualmente:

```text
08:00–10:00
10:00–12:00
```

no constituye solapamiento.

---

# 4. Actividades y especialidades

## VIGENTE — Una asignación representa una actividad

La programación nueva debe representar exactamente una actividad por asignación/rango.

No se mantienen múltiples actividades simultáneas dentro de una sola asignación.

## VIGENTE — Especialidad obligatoria

Un instructor sólo puede impartir una actividad para la que tenga la especialidad/capacidad correspondiente.

---

# 5. Horario operativo del salón

## VIGENTE — Operación y programación son independientes

Que un salón esté abierto no significa que exista una actividad programada.

La programación debe respetar el horario operativo.

## VIGENTE — Excepción de fecha sustituye al horario semanal

Para una fecha concreta, una excepción operativa tiene prioridad sobre la regla semanal.

Puede representar:

- salón cerrado;
- horario especial.

No constituye un delta parcial sobre el horario semanal.

## VIGENTE — Un cierre operativo impide programación efectiva

Una programación puntual o recurrente no reabre por sí misma un salón declarado no operativo.

---

# 6. Programación

## VIGENTE — La recurrencia no se modifica por una sola fecha

Un evento puntual de una fecha no debe alterar automáticamente la regla de semanas posteriores.

## VIGENTE — No recortar automáticamente una ocurrencia recurrente

Si una ocurrencia recurrente queda parcialmente fuera del horario operativo efectivo:

```text
programación:
08:00–12:00

horario efectivo:
10:00–16:00
```

no se convierte automáticamente en:

```text
10:00–12:00
```

La ocurrencia es incompatible para esa fecha.

## Ajustes puntuales de programación

Se requiere funcionalmente soportar cambios de una fecha concreta sin modificar la recurrencia.

Los ajustes puntuales deben poder expresar:

- cancelación puntual;
- reemplazo;
- adición puntual;
- cambio de instructor;
- cambio de actividad;
- cambio de salón.

El estado de diseño y materialización de la arquitectura que implementa estas reglas se consulta en `auditoria/ESTADO-ACTUAL.md` y `auditoria/DECISIONES-ARQUITECTONICAS.md`.

---

# 7. Reservas

## VIGENTE — Una reserva debe respetar el horario operativo efectivo

No debe crearse una reserva fuera de las horas en que el salón opera para esa fecha.

## VIGENTE — Las reservas actuales pertenecen al flujo legacy conocido

La transición a una identidad futura de sesión/programación aún no está terminada.

## PENDIENTE — Reserva contra sesión concreta

El objetivo futuro es que una reserva pueda referirse inequívocamente a una ocurrencia/sesión concreta.

No está materializado todavía como parte de F2D.

---

# 8. Visibilidad al cliente

## PENDIENTE — Búsqueda por actividad, fecha, salón e instructor

La experiencia objetivo contempla que el cliente pueda buscar una actividad y obtener la programación efectiva disponible por:

- fecha;
- salón;
- horario;
- instructor;
- disponibilidad.

La implementación completa depende de fases posteriores de programación/sesiones.

## PENDIENTE — Confirmación de instructor

Se ha previsto una futura confirmación de determinadas ocurrencias antes de hacerlas reservables.

La lógica y estados finales no forman todavía parte de la implementación cerrada.

---

# 9. Equipamiento y capacidad

## VIGENTE — Algunas actividades consumen equipamiento físico

La capacidad futura no depende sólo de personas o plazas.

Debe poder considerar recursos físicos del salón.

## VIGENTE — Reformer individual consume una unidad

Una reserva individual que utiliza Reformer consume una unidad disponible durante el intervalo correspondiente.

## VIGENTE — Duo Reformer consume dos unidades

Una reserva Duo Reformer utiliza dos reformers.

Ejemplo:

```text
8 reformers disponibles

3 reservas Duo
→ 6 reformers ocupados
→ 2 reformers restantes
```

Los recursos restantes pueden servir a otras reservas compatibles.

## PENDIENTE — Cálculo completo de capacidad compartida

La lógica final de inventario, recursos, sesiones y reservas todavía no está implementada de extremo a extremo.

---

# 10. “Actividades por sesión”

## SUPERSEDIDA

El concepto de clasificar actividades mediante una modalidad independiente de “actividades por sesión” fue descartado.

No debe reintroducirse como modelo paralelo.

---

# 11. Compras y pagos

## VIGENTE — Existen compras por distintos canales

El producto contempla:

- compras desde app;
- ventas desde web/caja;
- efectivo;
- transferencia;
- pago electrónico/Stripe.

## VIGENTE — Beneficios sólo deben otorgarse tras validación del pago

La compra de paquetes/clases no debe conceder derechos definitivos antes de confirmar el pago aplicable.

## PENDIENTE — Rediseño comercial completo

La separación definitiva entre venta, pago y beneficios pertenece a trabajo futuro.

---

# 12. Notificaciones

## VIGENTE COMO REQUISITO

El producto necesita notificaciones asociadas a eventos como:

- compras;
- reserva confirmada;
- recordatorios;
- cancelaciones;
- promociones;
- expiración de clases.

## PENDIENTE

La arquitectura final de notificación no está implementada como parte de las fases cerradas actuales.

---

# 13. Pagos y Notificaciones — autoridad funcional PN-13

## Estado y ownership

**AUTORIDAD FUNCIONAL PN-13 ACEPTADA / NO IMPLEMENTADO**

Esta sección es la autoridad funcional de producto para Pagos y Notificaciones. El contrato
técnico está en `auditoria/DECISIONES-ARQUITECTONICAS.md`, la verdad física en
`auditoria/ARQUITECTURA-ACTUAL.md`, la transición en
`auditoria/contexto/MAPA-LEGACY-Y-MIGRACION.md` y el detalle trazable PN-13 en
`auditoria/fase-pn13-materializacion-autoridad-pagos-notificaciones.md`.

## 13.1 Productos y paquetes

- Un producto comercial es una clase individual o un paquete.
- Cada componente referencia una actividad existente y declara una cantidad entera positiva.
- No existe un crédito universal anónimo: cada unidad pertenece a una actividad y conserva la
  procedencia del producto adquirido.
- Todo producto tiene precio de venta contractual y moneda explícitos. Un precio sugerido o de
  referencia puede mostrarse, pero no altera el precio contractual.
- Un paquete con varias actividades es un único producto comercial indivisible. Sus componentes
  no se cancelan ni se reembolsan monetariamente por separado.
- Duo Reformer es `FUTURE_CAPABILITY / NOT_MVP_OPERATIVE`: su existencia como regla de capacidad
  no autoriza reglas de pago operativas en el MVP.

## 13.2 Contrato histórico de compra

Al adquirir un producto se congela un snapshot inmutable que incluye, como mínimo:

- identidad del producto fuente;
- nombre y tipo comprados;
- precio de venta y moneda;
- actividades y cantidades compradas;
- política de vigencia aplicable;
- política de elegibilidad temporal de reserva;
- términos de reembolso aplicables cuando correspondan;
- identificadores y versiones de las políticas relevantes.

Cambiar, desactivar o eliminar lógicamente el catálogo vigente nunca reinterpreta una compra
histórica. La lectura histórica no depende del estado actual de `Paquete`.

## 13.3 Vigencia y extensión

- La vigencia es configurable y versionada por contrato comprado.
- La duración admite `DIAS` y `MESES`. Un mes se suma como mes calendario en la zona de negocio;
  nunca se reduce arbitrariamente a 30 días.
- El alcance de extensión es `MISMA_ACTIVIDAD` o `TODAS_LAS_ACTIVIDADES_ACTIVAS`.
- La compra acredita cantidades sólo a sus componentes. Una extensión global puede mover la
  vigencia de derechos existentes elegibles, pero no agrega cantidades a actividades no compradas.
- La extensión normal sólo alcanza derechos con cantidad disponible mayor que cero. Un derecho
  con saldo disponible cero está concluido para extensión normal aunque conserve expiración
  nominal futura.
- Una compra nueva después de agotar el saldo crea derechos con la vigencia del contrato nuevo.
- Una compra nunca acorta una vigencia válida ya existente.
- Los créditos expirados no reviven automáticamente y una política nueva no reescribe contratos
  históricos.
- Toda extensión, reactivación o corrección de vigencia deja un movimiento auditable.
- Una extensión o reactivación administrativa excepcional, si se habilita, requiere permiso,
  causa explícita e historia inmutable; nunca es edición directa.

La autoridad temporal es un `Clock` inyectado de backend y la zona de negocio/backend obligatoria
`America/Mexico_City`. Cada comando causal captura una sola vez su instante `S`; todos sus efectos
usan ese mismo valor. Los instantes se normalizan a UTC con precisión de microsegundos, se
serializan ISO-8601 con offset o `Z` y conservan zona y versión de política. Falta de `Clock`, zona,
duración, versión o resolución local válida falla cerrado; el reloj o zona del dispositivo/cliente
nunca decide elegibilidad, corte, periodo ni expiración.

La vigencia de cada derecho de compra comienza en el instante `S` en que la única transacción
causal confirma por primera vez `Acreditacion.COMPLETADA`. Sea `D0` la fecha local de `S` en
`America/Mexico_City`: para `N DIAS`, `Dend = D0 + N` días calendario; para `N MESES`,
`Dend = D0 + N` meses calendario con ajuste natural de fin de mes. El límite exclusivo `E` es el
inicio de `Dend` en esa zona. La vigencia es `[S,E)`: `fechaVencimiento = Dend - 1 día` es el
último día calendario válido y está incluido; `E = inicioDelDia(fechaVencimiento + 1)` y `t = E`
ya está expirado aunque el job no haya corrido. `N` debe ser positivo y `E > S`.

Una extensión normal bloquea y revalida el derecho: sólo si `disponible > 0` y el instante causal
es anterior a su `E` actual suma el nuevo periodo calendario positivo a su propio `Dend`; conserva
el inicio original y nunca acorta. Si el saldo es cero o el derecho ya expiró no se muta: la compra
genera derechos nuevos anclados a su `Acreditacion.COMPLETADA`. Un derecho de recuperación es
nuevo y su `S` es el instante único de la transacción ganadora que lo crea/procesa; su límite se
calcula con la política de recuperación y un replay devuelve el mismo derecho e instante.

## 13.4 Reserva frente a expiración

La política versionada fija `fechaVencimiento` como último día calendario válido incluido y
`limitePostVencimientoDias = N` como cantidad configurable de días calendario. Todo derecho debe
pasar a `COMPROMETIDA` mientras aún está vigente; queda prohibido crear un compromiso nuevo desde
el inicio del día siguiente. La frontera exacta es:

```text
instanteCompromiso < inicioDelDia(fechaVencimiento + 1, America/Mexico_City)
```

Un compromiso creado válidamente sobrevive a la expiración nominal mientras siga ligado a esa
reserva. El inicio de su sesión puede ocurrir hasta `N` días calendario después de
`fechaVencimiento`, inclusive, y debe cumplir:

```text
inicioSesion < inicioDelDia(fechaVencimiento + N + 1, America/Mexico_City)
```

Ejemplo vinculante: con `fechaVencimiento = 30 de septiembre` y `N = 3`, se permiten sesiones que
inicien el 1, 2 o 3 de octubre; una sesión que inicia el 4 de octubre se rechaza. Una cancelación
post-vencimiento válida puede crear el crédito de recuperación de §13.5, con vigencia propia; una
compra posterior nunca revive el derecho expirado anterior.

## 13.5 Cancelación, asistencia y crédito

### Cancelación del cliente

- La anticipación reembolsable se mide en horas naturales exactas desde el instante de solicitud
  hasta el inicio de la clase: es válida si `instanteCancelacion <= instanteInicioClase -
  anticipacionConfigurada`. La igualdad exacta al corte es válida.
- La anticipación es configurable/versionada; la zona de negocio/backend para este contrato es
  obligatoriamente `America/Mexico_City`.
- El cliente siempre puede cancelar y liberar capacidad operacional.
- Una cancelación dentro del corte restaura el crédito sólo cuando queda cupo en la cuota de
  cancelaciones reembolsables del periodo.
- La cuota es configurable por periodo; el MVP usa la clave exacta `clienteId + YearMonth` del
  inicio programado autoritativo de la sesión en `America/Mexico_City + policyVersion`, nunca el
  mes de solicitud. Una carrera entre versión/horario de sesión y contador se serializa y revalida;
  cualquier ambigüedad falla cerrado sin restaurar crédito ni consumir una cuota equivocada.
- Una cancelación tardía o después de agotar la cuota libera la plaza, pero consume el crédito.
- `NO_ASISTIDA` es distinta de cancelación tardía, aunque también consume el crédito.
- Una cancelación válida que restaura completamente la unidad no destruye por sí sola la
  integridad del producto para un posible reembolso.

### Asistencia pendiente y corrección tardía

Reservas/Asistencia posee el lifecycle: sólo una reserva activa, no cancelada, cuya sesión terminó
y cuya asistencia sigue sin resolver entra en `PENDIENTE_ASISTENCIA`. Mientras permanezca allí,
el `CompromisoReserva` y su cantidad `K` continúan comprometidos indefinidamente. No hay timer,
plazo máximo ni inferencia por tiempo de `NO_ASISTIDA`.

Sólo un actor `ADMIN`, nunca el instructor, puede resolverla a `ASISTIDA` o `NO_ASISTIDA`; la
transición y el consumo correspondiente ocurren atómicamente. Cancelación válida del cliente,
cancelación tardía y cancelación del estudio son terminales previos que prohíben entrar a
`PENDIENTE_ASISTENCIA`. Ausencia de actor, estado esperado, evidencia o versión inequívoca conserva
`PENDIENTE_ASISTENCIA` y `K` sin consumir ni liberar.

Una corrección tardía exige `ADMIN`, razón no vacía, actor, instante, estado anterior/nuevo, clave
idempotente y hash de payload. Nunca reescribe el historial ni el ledger; normalmente preserva el
consumo definitivo y, sólo cuando la consecuencia crediticia cambia de forma explícita, agrega el
movimiento compensatorio autorizado.

### Cancelación del estudio

- Siempre restaura el crédito del cliente y nunca consume su cuota de cancelación reembolsable.
- Programación/Reservas decide si la causa alcanza día completo, periodo parcial o sesión única.
- Pagos sólo decide y registra la consecuencia de crédito.

### Crédito de recuperación

Si una reserva originalmente válida se cancela correctamente después de que venció el derecho
original:

- no se revive el paquete ni créditos expirados ajenos;
- se crea un derecho de recuperación para la misma actividad;
- usa vigencia configurable/versionada de recuperación;
- referencia el compromiso, reserva y derecho originales;
- es idempotente, no es compra nueva y no dispara extensiones propias de compra.

### Expiración

- Cantidad disponible no usada deja de ser elegible al expirar y su historia permanece auditable.
- Un crédito comprometido válidamente no desaparece por el simple paso de la fecha nominal.
- Una compra posterior no revive cantidad expirada.
- El retraso de un job no prolonga elegibilidad: la regla temporal se aplica al leer/comprometer.

## 13.6 Reintegro de crédito y reembolso monetario

`REINTEGRO_CREDITO` o `RESTAURACION_CREDITO` modifica derechos mediante movimientos compensatorios.
`REEMBOLSO_MONETARIO` devuelve dinero mediante un lifecycle propio. No son sinónimos ni uno implica
automáticamente al otro.

El reembolso total normal del MVP exige siempre `PRODUCTO_ENTERO`; no es una opción de política.
Para cada `Compra` seleccionada, el predicado exige simultáneamente:

- la `Compra` está `ACREDITADA`, deriva del mismo pago efectivo confirmado y su importe completo
  participa en el reembolso;
- para cada componente, la suma disponible de los derechos originales elegibles coincide
  exactamente con la cantidad del snapshot;
- `K=B=C=E=R=0`, no existe contención financiera activa ni expiración efectiva según `Clock` aunque
  el job no haya corrido;
- sólo existen compromisos completamente liberados/restaurados compatibles y no hay alteración
  histórica incompatible con devolver la totalidad.

Cualquier consumo, penalización, expiración, reemplazo por recuperación, compensación
administrativa cuantitativa, mismatch o ambigüedad falla el camino normal cerrado hacia
`REQUIERE_REVISION`. No existe ventana temporal adicional para el reembolso normal: esta frase
supersede la posibilidad de una ventana configurable previamente descrita en PN-13. El MVP no
ofrece reembolso automático prorrateado ni reembolso de componentes individuales de un paquete
mixto.

La contención y revocación nunca se amplían a todos los derechos del cliente. Los alcances son:

- reembolso normal MVP: el settlement efectivo y la `Compra` completa que cumple
  `PRODUCTO_ENTERO`; sólo sus derechos atribuibles;
- pago tardío o segundo pago anómalo: exclusivamente ese `Pago`; no bloquea, revoca ni modifica
  derechos del settlement válido, salvo contener/revocar derechos no autorizados que hayan nacido
  específicamente del pago anómalo;
- refund externo/Dashboard: se correlaciona con el `Pago` originario y desde él se derivan las
  `Compra(s)` y derechos exactos; correlación ambigua queda `REQUIERE_REVISION`;
- disputa: se identifica por el cargo externo y `Pago` disputados y sólo alcanza derechos del
  settlement efectivo atribuible a ese origen.

Refund y disputa del mismo origen financiero se serializan bajo la misma frontera canónica. En
todos los casos se preserva el historial consumido y una ambigüedad de ownership/scope falla
cerrada en `REQUIERE_REVISION`.

## 13.7 Métodos y autoridad de pago

Los métodos MVP son `STRIPE`, `EFECTIVO` y `TRANSFERENCIA`.

- La UI cliente nunca es autoridad final del pago.
- Efectivo se confirma sólo por operador autorizado después de recibir físicamente el dinero.
- Transferencia sigue `PENDIENTE_VALIDACION -> CONFIRMADA | RECHAZADA`.
- Registrar evidencia y validar una transferencia son permisos distintos; una misma persona puede
  recibir ambos permisos en MVP, pero las autoridades no se fusionan.
- Una transferencia pendiente no acredita derechos.
- Todos los métodos confirmados convergen en la misma acreditación durable e idempotente.
- Una orden admite varios intentos y como máximo un pago asignado como settlement; cualquier otro
  pago exitoso por verdad externa es una excepción financiera, nunca un segundo settlement ni una
  segunda acreditación. El settlement válido se preserva y el pago anómalo se reconcilia y
  reembolsa explícitamente. No hay split payment en MVP.
- `PAGADA` no significa `ACREDITADA`. El éxito comercial comunicado al cliente es la compra
  efectivamente acreditada.

Una cancelación local de checkout no prueba cancelación monetaria. Si el mismo PaymentIntent se
reconcilia posteriormente como `succeeded` con orden, importe y moneda exactos, la verdad del
proveedor se conserva: una cancelación técnica permite el settlement/acreditación únicos; una
cancelación comercial explícita confirma el hecho monetario pero deja orden/compra en revisión y
abre un reembolso idempotente sin acreditar; un segundo pago confirmado conserva ambos hechos,
mantiene el settlement original, no acredita de nuevo y abre revisión/reembolso limitado al
`Pago` anómalo. Ese reembolso payment-scoped no bloquea, revoca ni altera derechos del settlement
válido; si el pago anómalo produjo derechos sin autorización, sólo éstos se contienen/revocan por
su provenance. La historia financiera y del ledger permanece inmutable.

Para transferencia, `Pago`, evidencia y `IntentoValidacionTransferencia` tienen identidades
separadas. La evidencia es inmutable, cada intento es append-only y pertenece a un ciclo. Sólo
`RECHAZADA -> PENDIENTE_VALIDACION` puede reabrirse con permiso, razón, actor e incremento de ciclo;
`CONFIRMADA` no se reabre. Registrar, validar y reabrir exigen permisos distintos. Replays iguales
devuelven el resultado previo; payload contradictorio, evidencia reutilizada de forma incompatible
fallan cerrado. Si la orden ya tiene settlement efectivo por otro `Pago`, la transferencia bajo
validación termina `RECHAZADA`/contenida con razón auditable `ORDEN_YA_LIQUIDADA`; no pasa a
`CONFIRMADA`, no se convierte en settlement, no crea `Acreditacion` ni derechos y no reemplaza el
settlement válido. Se registra el incidente financiero operacional y, si el dinero existe
externamente, se reconcilia/reembolsa sin acreditarlo.

## 13.8 Reglas funcionales de notificación

Se distinguen exactamente `EventoDominio`, `MensajeOutbox`, `Notificacion`, `EntregaLogica` e
`IntentoEntrega`. `Notificacion` es el mensaje lógico; `EntregaLogica` representa un canal y
endpoint lógico, con una fila por dispositivo push; `IntentoEntrega` es una fila inmutable por
ejecución. Notificación y entrega tienen deduplicación independiente. Los intentos se numeran desde
1, aumentan monótonamente y nunca se reutilizan; cuando el proveedor lo soporta, su idempotencia
estable usa la identidad de `EntregaLogica`.

- Las notificaciones operacionales/transaccionales no dependen del consentimiento promocional.
- Las promociones usan preferencias y consentimiento separados.
- Recordatorios son configurables y revalidan que la reserva siga vigente antes de enviarse.
- Avisos de próxima expiración sólo se envían si aún existe saldo disponible elegible.
- Cambiar reserva, horario, versión de recordatorio o vigencia vuelve obsoleta la programación
  anterior.
- El token push pertenece a un dispositivo; un token inválido se desactiva.
- Un fallo permanente de entrega es observable. Datos personales, tokens y secretos no se
  registran innecesariamente en logs.

El MVP orientado al cliente incluye: reserva creada, recordatorio configurable, cancelación del
estudio después de restaurar el crédito, cancelación válida del cliente por push como mínimo y por
email cuando esté configurado, compra acreditada, próxima expiración y reembolso
confirmado. No exige aviso al
cliente por `NO_ASISTIDA` o cancelación tardía, salvo configuración futura. Alertas internas
pueden cubrir disputas, webhooks no conciliados, estados imposibles, fallos permanentes críticos y
asistencia sin resolver.

Un resultado transitorio termina el intento y permite crear el siguiente sólo si quedan retries;
uno permanente termina la entrega; uno incierto se reconcilia con proveedor/idempotencia y, si un
retry pudiera duplicar, detiene automatización. Token inválido desactiva el endpoint y termina la
entrega ya creada como fallo terminal conocido. La agregación mutuamente exclusiva de
`Notificacion` aplica esta precedencia:

1. `REQUIERE_REVISION` si cualquier `EntregaLogica` agotó retries automáticos con outcome de
   proveedor incierto; prevalece sobre la agregación terminal normal y no se reintenta si hay
   riesgo de duplicar;
2. `SIN_CANALES_DISPONIBLES` sólo durante planificación, cuando no existe canal/endpoint usable y
   por ello no se crea ninguna `EntregaLogica` usable; nunca se reutiliza si una entrega ya creada
   falla después;
3. `ENTREGADA` si todas las entregas lógicas usables creadas tuvieron éxito;
4. `PARCIALMENTE_ENTREGADA` si al menos una tuvo éxito, ninguna está incierta y todas las demás
   terminaron en fallo conocido;
5. `FALLA_PERMANENTE` si se creó al menos una entrega usable, ninguna tuvo éxito ni quedó incierta
   y todas terminaron en fallo conocido.

Los estados son monotónicos; el retry nunca muta el hecho de negocio y la garantía sigue siendo
at-least-once, no exactly-once.

## 13.9 Scope MVP y exclusiones

El MVP incluye catálogo, paquetes por actividad, snapshot comercial, orden/compra/pago separados,
Stripe/efectivo/transferencia, acreditación, derechos y ledger, vigencia, compromisos de reserva,
cancelación/asistencia/expiración/crédito de recuperación, reembolso total de producto entero,
Inbox Stripe, reconciliación, Outbox, email, push, recordatorios, avisos de expiración, confirmación
de reembolso, separación promocional básica y contención de disputas.

Quedan fuera inicialmente: reglas operativas de pago Duo Reformer, suscripciones, renovación
automática, gift cards, motor avanzado de cupones/promociones, split payments, conciliación
bancaria automática, reembolso prorrateado o por componente, workflow completo de evidencia de
disputas Stripe, métodos Stripe de notificación diferida, tarjetas guardadas, un segundo dominio de
clases/reservas y edición manual directa de saldo.
