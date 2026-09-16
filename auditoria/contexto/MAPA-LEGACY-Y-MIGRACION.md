# FeelingPilates — Mapa legacy y migración

Status: CANONICAL
Last updated: 2026-09-15
Repository verification: VERIFIED
Last verified against commit:
a0ec85818b771d4ac924b427fa1e90244ea9fe8e
Verification scope: autoridad productiva, F2D preservado y transición PN-13 diseñada/no implementada

## Propósito

Este documento responde:

> ¿Qué sistema es actualmente la autoridad productiva y qué falta para sustituirlo?

La existencia de código nuevo no implica automáticamente que ese código sea la fuente de verdad.

---

# Área: Programación

## Autoridad productiva actual

**LEGACY —** **`TurnoInstructor`**

## Legacy productivo

`TurnoInstructor`

Tipos conocidos:

- `RECURRENTE`
- `EXCEPCION`
- `CANCELACION`

## Modelo nuevo

- `BloqueProgramacion`
- `Asignacion`

## Estado del modelo nuevo

**IMPLEMENTADO\_NO\_PRODUCTIVO**

## Writers activos conocidos

### Legacy

Existe flujo de escritura productivo de `TurnoInstructor`.

El frontend web consume esos contratos.

### Nuevo

Existe infraestructura interna para `BloqueProgramacion + Asignacion` y para los ajustes F2D.2, pero no constituye la autoridad productiva.

El diseño F2D está aprobado y F2D.2 está `CERRADA` como dark launch interno, con aprobación técnica y documental, publicación verificada y cierre de publicación `PASS`. La afirmación histórica de que su implementación no había iniciado queda obsoleta. El estado exacto debe volver a verificarse contra repositorio y datos antes de cualquier activación.

## Readers activos conocidos

### Legacy

- frontend web;
- `ReservaService`;
- servicios/backend relacionados con `TurnoInstructor`.

### Nuevo

Uso interno limitado del modelo recurrente y de los ajustes F2D.2 implementados en dark launch; no incluye consumers productivos nuevos.

No existe evidencia de que frontend/reservas sean consumers productivos de `BloqueProgramacion + Asignacion`.

## Consumers

- frontend web: legacy;
- reservas actuales: legacy;
- mobile: no confirmado como consumer del modelo nuevo.

## ¿Puede coexistir productivamente?

**NO COMO DOS AUTORIDADES SIMULTÁNEAS**

No existe un cutover ejecutado que permita considerar ambos modelos equivalentes.

## Estado de migración

**F2D.2 CERRADA / DARK LAUNCH INTERNO / NOT_PRODUCTIVE**

F2D.2 no representa estado `MIGRANDO` ni autoridad `NUEVA`. Su documentación está cerrada, pero cualquier activación continúa pendiente de autoridad expresa.

## Condiciones de cutover

El diseño F2D ya fue aprobado y su implementación interna F2D.2 está materializada. Antes del cutover todavía deben resolverse, como mínimo:

- identidad inequívoca de programación futura;
- estrategia sobre reservas;
- auditoría de datos;
- estrategia de writers/consumers;
- gate de activación.

## Fence / estrategia

**FUTURO / NO IMPLEMENTADO**

F2D.2 está en dark launch: sin controllers públicos, consumers productivos, adapters sobre writers legacy, `ImpactoAjustesEnExcepcionHorario`, `Reserva` legacy, frontend/mobile, cutover ni fence persistido.

El fence y el cutover permanecen futuros. `TurnoInstructor` sigue siendo la autoridad productiva única.

## Bloqueadores / deuda

- dualidad legacy/nuevo;
- F2D.2 cerrada documentalmente e implementada internamente, pendiente exclusivamente de cualquier activación futura que llegue a autorizarse;
- programación nueva no es autoridad;
- relación de reservas con la fuente nueva no resuelta;
- activación futura de las invariantes de serie/concurrencia ya materializadas internamente.

## Fase relacionada

F2D.

---

# Área: Horario operativo del salón

## Autoridad productiva actual

**MODELO TEMPORAL ENDURECIDO**

## Legacy productivo

Existían contratos y modelos previos de salón/horarios.

## Modelo nuevo/endurecido

- horarios operativos con vigencia;
- resolución por fecha;
- excepciones de horario;
- `HorarioEfectivoSalon`.

## Estado del modelo nuevo

**PRODUCTIVO**

## Writers activos

- writers versionados de horario;
- writers endurecidos de excepciones por fecha.

## Readers activos

Consumers internos y frontend adaptado donde corresponde.

## Consumers

Frontend web y servicios backend.

## ¿Puede coexistir productivamente?

**SÍ, MEDIANTE COMPATIBILIDAD DELIBERADA DE API**

Se preservaron endpoints legacy cuando eran consumidos.

## Estado de migración

**MIGRACION INTERNA CERRADA EN F2B/F2C**

## Condiciones de cutover

No existe una necesidad inmediata de retirar todos los contratos legacy mientras continúen siendo compatibles y consumidos.

## Fence / estrategia

Compatibilidad de endpoints y una única lógica interna endurecida.

## Bloqueadores / deuda

Retiro futuro de endpoints legacy sólo después de confirmar cero consumidores.

## Fase relacionada

F2B / F2C.

---

# Área: Reservas

## Autoridad productiva actual

**LEGACY**

## Legacy productivo

`Reserva` / `ReservaService` vinculados funcionalmente al universo `TurnoInstructor`.

## Modelo nuevo

Una futura reserva vinculada a programación/sesión nueva.

## Estado del modelo nuevo

**PENDIENTE**

## Writers activos

`ReservaService` actual.

## Readers activos

Flujos actuales de reserva/cliente.

## Consumers

Backend y experiencias existentes que utilizan reservas.

## ¿Puede coexistir productivamente?

No existe todavía un diseño aprobado para dos autoridades de reserva.

## Estado de migración

**NO\_INICIADA PARA SESIONES NUEVAS**

## Condiciones de cutover

- identidad inequívoca de ocurrencia/sesión;
- migración o mapeo seguro de reservas pertinentes;
- programación nueva activada;
- validación de capacidad cuando corresponda.

## Fence / estrategia

**PENDIENTE**

## Bloqueadores / deuda

Las reservas actuales no tienen una relación confirmada con `Asignacion` o futuros ajustes F2D.

## Fase relacionada

Posterior a programación efectiva / sesiones.

---

# Área: Frontend web — programación

## Autoridad productiva actual

**LEGACY**

## Legacy productivo

APIs de `TurnoInstructor`.

## Modelo nuevo

Futura programación efectiva/ajustes.

## Estado modelo nuevo

**NO CONSUMIDO PRODUCTIVAMENTE**

## Writers activos

El frontend puede escribir operaciones legacy de programación.

## Readers activos

El frontend lee programación legacy.

## Consumers

Usuario administrativo web.

## ¿Puede coexistir productivamente?

No debe habilitarse una segunda autoridad sin estrategia explícita.

## Estado de migración

**PENDIENTE**

## Condiciones de cutover

Backend nuevo aprobado, activación controlada y migración de consumers.

## Fence / estrategia

No implementado.

## Bloqueadores / deuda

La implementación interna F2D.2 ya existe, pero la activación controlada y la migración de consumers no han iniciado.

## Fase relacionada

Posterior al cierre backend de programación nueva.

---

# Área: App móvil — programación/reservas

## Autoridad productiva actual

**NO\_RECUPERADA\_CON\_CERTEZA PARA PROGRAMACION DINAMICA**

## Legacy productivo

No se confirmó un consumo equivalente a la programación legacy durante el último inventario F2D.

## Modelo nuevo

Futura programación efectiva/sesiones.

## Estado modelo nuevo

**PENDIENTE**

## Writers activos

NO\_RECUPERADO\_CON\_CERTEZA.

## Readers activos

Parte de la experiencia conocida seguía sin depender de la programación nueva.

## Consumers

Clientes móviles.

## ¿Puede coexistir productivamente?

No aplicable todavía.

## Estado de migración

**PENDIENTE**

## Condiciones de cutover

API estable de sesiones/programación efectiva y flujo de reservas definitivo.

## Fence / estrategia

PENDIENTE.

## Bloqueadores / deuda

Auditoría reciente del mobile pendiente.

## Fase relacionada

Fases posteriores de sesiones/reservas.

---

# Regla general de este mapa

Antes de habilitar un nuevo writer o consumer:

1. identificar la autoridad actual;
2. comprobar si existe consumer legacy;
3. definir explícitamente si ambas fuentes pueden coexistir;
4. si no pueden, establecer cutover/fence;
5. ejecutar migración;
6. actualizar este documento sólo cuando la autoridad cambie realmente.

---

# Área: Pagos y Notificaciones — transición PN-13

## Autoridad actual y objetivo

```text
ACTUAL: EXISTING / LEGACY_EVOLUTION_SOURCE / PRODUCTIVO_PARCIAL
OBJETIVO PN: DESIGNED_NOT_IMPLEMENTED / NOT_PRODUCTIVE
CUTOVER: NOT_AUTHORIZED
IMPLEMENTATION: NOT_AUTHORIZED
```

El código legacy sigue atendiendo sus consumidores actuales. PN-13 define una transición; no
cambia autoridad productiva. La branch histórica `feature/calendario-reservas-pagos` en
`e515152671dc5b2801f4fd7ef3e1e608bfc55a0a` es sólo `CONCEPTUAL_EVIDENCE_ONLY`: no se mezcla, no
se cherry-pickea y no es fuente de implementación.

## Matriz REUSE / REWORK / RETIRE

`REUSE` significa conservar una capacidad o contrato bajo su boundary correcto; `REWORK`, migrar
o envolver antes de ser objetivo; `RETIRE`, retirar sólo después de cutover y cero consumidores.
Ninguna clasificación autoriza trabajo físico.

| Pieza física | Clase | Rol actual | Rol objetivo y razón | Coexistencia / compatibilidad | Condición de migración o cutover |
| --- | --- | --- | --- | --- | --- |
| `Paquete` | REWORK | Catálogo mutable y vigencia/precio actuales. | `ProductoComercial` mutable sólo para catálogo; compra usa snapshot. | Preservar IDs y APIs de catálogo durante migración. | Snapshot/backfill validados; readers históricos dejan de consultar catálogo. |
| `PaqueteActividad` | REUSE + REWORK | Composición actividad/cantidad. | Base del componente de catálogo con cantidad positiva; no snapshot histórico. | Convivir con `CompraComponenteSnapshot`. | Writers nuevos congelan componentes; backfill legacy cerrado. |
| `Compra` | REWORK | Mezcla compra, pago, Stripe, caja, vigencia y estado. | `Compra` adquirida separada de `OrdenVenta`, `Pago`, `Acreditacion` y derechos. | Adaptador/API legacy temporal; no doble settlement. | Datos descompuestos, reconciliados y consumers migrados. |
| `PagoService` | REWORK | PaymentIntent, webhook directo y reconciliación básica. | Casos de uso separados + puerto Stripe + Inbox/reconciliador. | Mantener endpoints mientras delegan al nuevo núcleo. | Inbox y máquina monotónica auditadas; webhook antiguo deja de mutar directo. |
| `VentaService` | REWORK | Efectivo/transferencia acreditados inmediatamente; ticket por UUID. | Crea `OrdenVenta`, registra/valida `Pago`, dispara acreditación común. | API de caja compatible; respuesta puede evolucionar aditivamente. | Transferencia pendiente y permisos separados activos; backfill conciliado. |
| `PagoController` | REWORK | Intento, historial, paquetes activos y webhook. | Controllers delgados sobre aplicación; raw body hacia Inbox. | Preservar rutas iniciales; versionar cambios incompatibles. | Clientes migrados y observabilidad/recovery verificados. |
| `VentaController` | REWORK | Caja, consulta y cambio de estado/reembolso. | Operaciones distintas de orden, efectivo, transferencia y reembolso. | Preservar rutas de venta/consulta temporalmente; deprecar mutación ambigua. | Nuevos permisos/endpoints adoptados; ruta legacy cerrada. |
| `PaqueteController` / `PaqueteGestionController` | REUSE + REWORK | Lectura pública y CRUD lógico. | API de catálogo, con política/versiones y DTOs explícitos. | Mantener rutas `/api/publico/paquetes` y `/api/ventas/servicios` inicialmente. | Consumers confirman DTO objetivo; compatibilidad retirada aparte. |
| `StripeConfig` | REUSE + REWORK | Configura clave global del SDK. | Adaptador Stripe configurado, validado y sin autoridad de dominio. | Puede coexistir detrás del puerto. | Config/secret handling y tests de adapter aprobados. |
| `CompraRepository` | RETIRE + REPLACE | Persistencia monolítica de la entidad mezclada. | Repositorios por agregado/Inbox/ledger; consultas históricas sobre snapshot. | Adaptador legacy read-only temporal. | Backfill y equivalencia verificados; writers legacy apagados. |
| `PaqueteRepository` | REUSE + REWORK | Persistencia de catálogo. | Repositorio de catálogo separado de contratos comprados. | Mantener mientras se migra el package. | Nuevo catálogo/API auditados. |
| `EmailService` | REUSE + REWORK | Puerto mínimo sólo para invitación. | Puerto genérico de entrega email en infraestructura de notificaciones. | Invitaciones siguen funcionando mediante adaptador compatible. | Outbox/notificación y proveedor real verificados. |
| `EmailServiceConsola` | RETIRE | Simulación de desarrollo en logs. | Sólo fake de desarrollo/test; no proveedor productivo. | Conservar hasta tener adapter sustituto. | Proveedor elegido/configurado y fallback operacional aprobado. |
| `Reserva` | REUSE + REWORK | Reserva operacional sin crédito ni asistencia económica. | Sigue siendo entidad de Reservas; obtiene integración por `CompromisoReserva`. | ID actual es correlación; no duplicar reserva en Pagos. | Boundary atómico y estados necesarios auditados. |
| `ReservaService` | REWORK | Crea/cancela contra turnos y horario; sin crédito. | Orquesta operación con `GestorCreditoReserva` en la misma transacción. | Endpoints actuales se preservan hasta migrar consumidores. | Tests de integración, concurrencia y rollback pasan. |
| `ReservaRepository` | REUSE + REWORK | Persistencia/consultas de reserva. | Sigue bajo Reservas; añade sólo locks/consultas autorizados por su slice. | Sí; Pagos referencia `reserva_id`, no posee la fila. | Contrato de integración aprobado. |
| `ReservaController` | REUSE + REWORK | API administrativa de reserva/cancelación. | Conserva autoridad operacional; expone consecuencias sin decidir saldo. | Compatibilidad obligatoria hasta migración de clientes. | DTO/API objetivo versionado y consumers migrados. |

## Migraciones V22.1–V35

Todas coexisten físicamente y permanecen inmutables; `REWORK/RETIRE` describe su modelo o permiso,
no autoriza editar el archivo histórico. La compatibilidad indicada se conserva hasta el gate de
consumer/cutover correspondiente.

| Migración | Clase | Rol actual y target/razón | Coexistencia, API y condición de cutover |
| --- | --- | --- | --- |
| V22.1 | REUSE + REWORK | Crea `paquete/compra`; sus datos alimentan catálogo, orden, snapshot y pago separados. | Tablas legacy conviven; APIs se preservan hasta backfill y readers históricos validados. |
| V22.2 | REUSE + REWORK | Clave única legacy; target añade operación/contexto/hash contradictorio. | Se reconoce durante transición; writer nuevo usa contrato fuerte antes del cutover. |
| V22.3 | RETIRE HISTÓRICO | Crea permiso refund Stripe luego retirado; no es autoridad del refund nuevo. | Sin API que reactivar; permisos nuevos llegan aditivamente y con gate. |
| V23 | REUSE + REWORK | Añade composición actividad/cantidad y método; fuente para snapshots y pagos. | Catálogo/caja compatibles hasta congelar todas las compras y separar transferencia. |
| V24 | REUSE HISTÓRICO | Retira semillas conocidas con salvaguardas. | No tiene API target ni acción de cutover; sólo se preserva. |
| V25 | REUSE + REWORK | Sede de venta; target queda en orden/pago/provenance según el caso. | DTO legacy conserva sede hasta mapping/backfill validado. |
| V26 | REUSE + REWORK | `grupo_compra_id/numero_item` simula ticket; target es `OrdenVenta`+línea. | API carrito preservada hasta equivalencia y unicidad de orden verificadas. |
| V27 | REUSE + REWORK | Motivo libre de estado; target son causas tipadas/auditables. | Lectura histórica conserva texto; writers nuevos dejan de usarlo tras cutover. |
| V28 | REUSE + REWORK | Primeros permisos granulares de caja. | IDs/asignaciones se preservan; target agrega autoridades separadas sin romper roles. |
| V29 | REUSE + REWORK | Permiso de vista de caja. | Compatibilidad UI preservada hasta inventario de consumers/permisos. |
| V30 | REUSE HISTÓRICO | Ajusta descripciones de permisos. | Sin efecto de modelo; permanece por historia Flyway. |
| V31 | REUSE + REWORK | Reestructura permisos por pantallas/acciones. | Asignaciones por ID coexisten; no se renombran otra vez sin migración aditiva. |
| V32 | REUSE + REWORK | Renombra Caja a Ventas y crea vocabulario API actual. | Códigos actuales se preservan mientras controllers legacy estén activos. |
| V33 | REUSE + REWORK | Granulariza catálogo. | Se conservan permisos UI; políticas comerciales agregan permisos nuevos aparte. |
| V34 | REUSE + REWORK | Renombra catálogo a servicios. | Rutas `/api/ventas/servicios` y permisos se preservan hasta migrar consumers. |
| V35 | REUSE HISTÓRICO | Elimina permiso/endpoint refund Stripe legacy. | No se revierte; lifecycle `Reembolso` usa nuevos permisos/API tras su propio gate. |

Todas las migraciones históricas permanecen inmutables. La implementación revalida la versión
Flyway máxima en ese momento; PN-13 no reserva un número.

## Coexistencia y secuencia obligatoria

1. Añadir tablas y constraints objetivo sin cambiar readers/writers productivos.
2. Caracterizar el comportamiento legacy y crear snapshot/backfill determinista con reporte de
   filas ambiguas en `REQUIERE_REVISION`.
3. Introducir escritura nueva idempotente detrás de adapters compatibles; ningún pago puede
   liquidar dos veces ni acreditar por dos rutas.
4. Validar contabilidad, snapshots, permisos, API y observabilidad en dark launch.
5. Migrar consumers de historial, catálogo, caja, Stripe y Reservas por slices auditados.
6. Activar un único writer/reader autoritativo mediante fence/cutover expresamente autorizado.
7. Retirar servicios, columnas y rutas legacy sólo en fase `contract` posterior y con cero
   consumers demostrados.

Antes del cutover, rollback significa desactivar el path nuevo sin perder datos. Después del
cutover sólo se permite corrección forward/compensatoria; nunca se borra ledger o evidencia.

La futura expansión debe materializar antes de cualquier writer objetivo, como un conjunto
coherente y auditado: `cliente_id` inmutable con FKs compuestas Orden/Compra/Derecho; pointer único
de settlement en Orden; ledger de seis buckets y constraints de conservación; ancla
cliente+actividad y FEFO sin `SKIP LOCKED` foreground; vigencia UTC +
`America/Mexico_City`/versión, `fechaVencimiento` incluida y límites exclusivos de compromiso y
sesión; allowance por mes de inicio de sesión; identidades separadas de
transferencia/evidencia/intento y rechazo `ORDEN_YA_LIQUIDADA`; scopes de refund/disputa
normalizados por origen con anomalías payment-scoped; y `Notificacion -> EntregaLogica ->
IntentoEntrega` con intentos append-only y agregación terminal por precedencia. Una migración
parcial no habilita el writer correspondiente y toda fila ambigua queda fuera del cutover en
`REQUIERE_REVISION`.

Durante coexistencia, el adapter legacy no puede confirmar una transferencia al registrarla,
asignar settlement por una segunda ruta, crear saldo fuera del ledger ni enviar notificaciones
como sustituto del Outbox. Stripe y transferencia deben converger en el mismo guard de settlement;
una transferencia validada después de otro settlement se contiene con `ORDEN_YA_LIQUIDADA` y no
acredita. Un pago tardío/segundo se reconcilia o reembolsa sólo en el scope del `Pago` anómalo y no
afecta derechos del settlement válido. Reservas conserva sesión/asistencia y llama la consecuencia
crediticia transaccional; refund y disputa del mismo origen comparten mutex/ownership y nunca se
amplían a todos los derechos del cliente. La corrección residual PN-13 sólo diseñó este
enforcement: no creó SQL, migraciones, dual-write, backfill, fence ni código.

## Compatibilidad API

- **Preservar/evolucionar compatible:** catálogo público, gestión de servicios, creación/consulta de
  ventas, inicio de PaymentIntent, historial propio, webhook y APIs de reserva.
- **Deprecar después de reemplazo:** `mis-paquetes` como vigencia sin saldo real y cambios de estado
  de venta que pretendan ser refund.
- **Reemplazar:** refund como simple `Compra.estado`, webhook con mutación compleja directa y
  transferencia confirmada al registrarse.
- Cambios incompatibles usan ruta/versión nueva; una respuesta legacy puede enriquecerse sólo de
  forma aditiva mientras existan consumidores.

## Fence, autoridad y bloqueadores

No existe fence ni cutover PN. El código legacy permanece autoridad física parcial hasta una fase
de implementación, migración y activación futura. La corrección canónica y PN-13 están
`ACCEPTED` después del re-audit fresh R1.2 `PASS / P0=0 / P1=0 / P2=1`; el único P2 permanece
`NEW-PN13-017 / OPEN / EDITORIAL / NON_BLOCKING / IMPLEMENTATION_INDEPENDENT`. PN-14 y cualquier
implementación siguen `NOT_AUTHORIZED`. Esta aceptación documental no crea SQL, migraciones,
dual-write, backfill, fence, runtime productivo ni cutover. Las condiciones mínimas de cutover son:
safety net, backfill sin ambigüedades no resueltas,
invariantes/locks probados en PostgreSQL, Stripe/Inbox/recovery verificados, boundary de Reservas
atómico, Outbox operativo, consumers inventariados y gate independiente en PASS.

### Aclaración vigente de lifecycle PN14 — aceptación limitada del primer safety net

PN13 permanece `MATERIALIZED / ACCEPTED / PUBLISHED / CLOSED`, workflow terminal `PUBLISHED`,
gates documentación/publicación/cierre PASS, P0=0/P1=0/P2=1 sólo NEW-PN13-017
`OPEN / P2 / EDITORIAL / NON_BLOCKING / IMPLEMENTATION_INDEPENDENT`. PN14 está `ACCEPTED`
con handoff de lane `ACTIVE` exclusivamente para `SAFETY_NET / CHARACTERIZATION`:
`IMPLEMENTATION AUTHORIZED_ONLY_FOR_FIRST_SLICE / NOT_STARTED`; slices 2–12 `NOT_AUTHORIZED`.
El audit independiente y gate real de autorización son PASS; evidencia y hash inmutable exacto
constan en ESTADO-ACTUAL, checkpoint PN14 y review de autorización allí referenciados.
Las marcas candidate anteriores y la frase pre-PN14 de no autorización conservan su snapshot
histórico; esta transición las supersede únicamente para el contrato del primer slice.
El target productivo PN13 sigue `DESIGNED_NOT_IMPLEMENTED / NOT_AUTHORIZED`.

La entrada `LOCAL_UNCOMMITTED_AUDITED_DOCUMENTATION_ENTRY` se autoriza condicionalmente sobre
HEAD `12f52781177694693be7d6dc2efc71009c5f45b3`, staging EMPTY y el manifest de seis paths
`auditoria/reviews/PN14-MANIFEST-ENTRADA-LOCAL-SAFETY-NET-CARACTERIZACION.md`.
Sólo será efectiva con Task `task_8f1a6c481d15` COMPLETED/succeeded y resultado único
`PN14_FINAL_MATERIALIZATION_VERIFICATION=PASS`, más Task `task_2b415c7bb7c0` /
gate `gate_3e9c24443aa9` COMPLETED / RESOLVED / PASS y preflight físico exacto.
Verificación/confirmación siguen pendientes al materializar; no son PASS declarado aquí.
Toda discrepancia falla cerrado; manifest/HEAD revisados exigen nueva autorización.

La secuencia general expand/backfill de este mapa no supersede PN13 §13: el primer safety net
precede cualquier SQL/tabla/writer objetivo y caracteriza el legacy, incluidos quirks
`LEGACY_NOT_TARGET`, sin corregirlos ni crear adapter. La matriz REUSE/REWORK/RETIRE y
autoridad productiva anteriores permanecen intactas. No implementación realizada, publicación
PN14, migración, fence, cutover ni modificación runtime/F2D/F2E. Cada slice posterior requiere
su propio handoff, allowlist, audits y gates; PASS del primero no concede continuidad.

### Payments & Notifications — PN14 Slice1 current lifecycle posterior al gate técnico

Se preserva íntegro como prefix el mapa de entrada, incluida su matriz REUSE/REWORK/RETIRE,
migraciones/coexistencia/API y lifecycle previo. Esta transición posterior supersede únicamente
NOT_STARTED del primer safety net y sus pendientes técnicos ya resueltos, sin cambio de autoridad.

PN13 permanece `MATERIALIZED / ACCEPTED / PUBLISHED / CLOSED`, workflow `PUBLISHED / TERMINAL`;
sus gates documental/publicación/cierre continúan PASS. PN13-001..010 y NEW-PN13-011..016 CLOSED;
`NEW-PN13-017 OPEN / P2 / EDITORIAL / NON_BLOCKING / IMPLEMENTATION_INDEPENDENT` es el único
residual combinado, sin corrección: P0=0/P1=0/P2=1. PN14 contrato permanece `ACCEPTED / ACTIVE`;
autoridad de implementación `SLICE_1_ONLY`, Slice1 safety net `IMPLEMENTED / VALIDATED / AUDITED /
TECHNICAL_GATE_PASS`; slices 2–12 `NOT_AUTHORIZED`, sin continuidad automática ni nueva fase.
Target production PN13 `DESIGNED_NOT_IMPLEMENTED / NOT_AUTHORIZED`; fuente productiva, tests
anteriores, configuración, pom, resources, wrappers, DB/migraciones, runtime, autoridad legacy,
Reservas/Programación, F2D/F2E, fence y cutover `UNCHANGED`. No inspección de worktrees/candidates
F2E ni integración. Publicar tests no activa comportamiento productivo.

Gate técnico real `task_ae6dd88b4b33 / gate_bc4ce966cb51` COMPLETED/RESOLVED/PASS en
run_12b33800d7e1; auditor fresh `task_2f0ba72484a2 / ctx_f2874d4903db`, uniqueDone
msg_4eb8fcb726e4 y structuredstatus msg_3a4969ac4c4a. M01–M12 conforme, TA-001/002/003 CLOSED,
scope/tests/implementation/host PASS, baseline590 anterior a writers y finalfocal67/full638
sin failures/errors/requiredskips; JDK21/Docker/PostgreSQL efímero real y actualconstraints/rollback.
Evidencia técnica AJENA íntegra y hashes históricos/finales en
`auditoria/reviews/PN14-SLICE1-REVIEW-TECNICO-SAFETY-NET-CARACTERIZACION.md`; checkpoint/profile/publicación21/condición live en
`auditoria/fase-pn14-slice1-safety-net-caracterizacion.md` §§4–6 y en el nuevo bloque de ESTADO-ACTUAL.
Materializador documental run_190c06410cef/task_4a2e70c6bafd/ctx_749d90b963c8, documentación
MATERIALIZED/NOT_SELF_AUDITED; DOCUMENTATION_GATE=PENDING, aceptación Slice1=PENDING y
READY_TO_PUBLISH=NO al escribir. Publicación/cierre APPLICABLE/PENDING, NOT_PERFORMED/NOT_CLOSED.

La transición competente queda expresamente definida, siguiendo el precedente temporal del
checkpoint PN14 original §6. En el Run `run_190c06410cef`, Slice1 pasa determinísticamente a
`ACCEPTED / READY_TO_PUBLISH` **si y sólo si** se cumplen conjuntamente estas condiciones:

1. El Task `task_f094cad89d53`, rol `DOCUMENT_AUDITOR` fresh, independiente del ejecutor,
   correctores y este DOCUMENTER, está `COMPLETED / succeeded`; su Dispatch competente posee
   un único `worker_done` aceptado, `DOCUMENTATION_AUDIT=PASS`, P0=0/P1=0,
   `filesModified=[]`, sin decisión humana ni SECURITY_STOP pendientes, y verifica físicamente
   los 21 paths exactos de la allowlist de publicación del checkpoint Slice1 §5.
2. El Task exclusivamente coordinador `task_1054afbc3810` está `COMPLETED`, y su gate
   `gate_f163c0193bdb` está `RESOLVED / PASS`, provenance `coordinator_gate_resolution`.
   El resultado competente contiene `candidateFileSHA256` con el mapa exacto path→SHA-256 raw
   de los 21 archivos realmente auditados y `acceptedPublishPaths` con el set exacto de esos
   21 paths, sin omisiones ni paths extra; ambos coinciden con el snapshot final del
   DOCUMENTER, el snapshot independiente del DOCUMENT_AUDITOR y los bytes físicos actuales.
3. Se preservan los gates técnicos ya PASS, los trece hashes finales de tests/helpers y los
   cuatro documentos PN14 originales no editables, los prefixes completos de entrada de
   ESTADO/mapa y todos los demás archivos protegidos; branch/HEAD/upstream/live origin
   siguen en el baseline exacto, staging EMPTY y delta documental limitado a cuatro paths.

En este corte el auditor está READY sin resultado y el Task/gate coordinador está
BLOCKED/PENDING: `DOCUMENTATION_GATE=PENDING`, `SLICE1_ACCEPTANCE=PENDING`,
`READY_TO_PUBLISH=NO`. No se fabrica un futuro PASS, Dispatch, mensaje ni SHA final.
Si la condición real se satisface posteriormente sobre los mismos bytes, el lifecycle vivo es
`ACCEPTED / READY_TO_PUBLISH` y `DOCUMENTATION_GATE=PASS` sin reescribir el snapshot auditado.
Los hashes finales de documentos se fijan externamente en resultados estructurados únicos y
en el gate competente: ningún documento contiene su propio SHA ni un ciclo criptográfico.
Un título PASS, chat, journal o existencia de archivos no satisface esta regla; se recuperan
task-list/worker-show/gate-list/inbox y se cruzan outcome, Dispatch, mensajes y hashes reales.
Mismatch, evidencia ausente/stale, FAIL/UNKNOWN/SKIPPED/BLOCKED o mutación posterior falla cerrado;
no se publica ni se infiere un HEAD descendiente sin nueva autorización pertinente.

La aceptación es una **transición única** evaluada sobre el baseline auditado antes de publicar;
HEAD `12f52781177694693be7d6dc2efc71009c5f45b3` y staging EMPTY son precondiciones de esa
transición, no requisitos perpetuos después de ella. Una publicación posterior del scope exacto,
autorizada por el gate competente de este Run, no revoca la aceptación ya adquirida por cambiar
HEAD o staging durante sus operaciones autorizadas. Los nuevos HEAD, igualdad local/remota y
staging se verifican en publicación y cierre con sus propios profiles/gates y evidencia física,
sin inventar aquí un SHA descendiente; esta regla no concede permiso a cambios fuera de scope.

La condición sólo acepta el safety net y habilita su PUBLISHER separado dentro del scope
autorizado. `PUBLICATION_GATE=APPLICABLE/PENDING` y
`PUBLICATION_CLOSURE_GATE=APPLICABLE/PENDING` permanecen así hasta sus etapas competentes.
No equivale a publicación, cierre, runtime productivo, migración, fence o cutover; no concede
otra ejecución de implementación ni autoriza slices 2–12.

Profile original LOCAL_UNCOMMITTED_AUDITED_DOCUMENTATION_ENTRY permitió ejecutar sin publicación
PN14 previa; autorización del usuario postécnica y handoff§8 componen ahora documentación/audit,
publicación21 por PUBLISHER separado y cierre/audit/gates. Se incluyen los seis docs originales
de autoridad aceptada/evidencia de entrada con los trece tests y dos nuevos documentos; cuatro
originales inmutables, ESTADO/mapa append-only. No se impone publicación PN14 previa separada
ni se reescriben snapshots auditados. Este worker no stagea/commitea/pushea ni concede runtime.
El safety net precede todo SQL/tabla/writer objetivo; transferencias inmediatas, webhook que
regresa estado, historial mutable/null, refund como estado y reservas sin crédito se caracterizan
como LEGACY_NOT_TARGET. No snapshots, ledger, Inbox/Outbox, boundary crédito ni nuevos permisos
PN implementados. Sensibilidad analítica sin mutation testing; sin igualdad exacta moving now;
M12 no implica atomicidad externa Stripe/DB ni control de último crédito.
Siguiente acción actual fresh DOCUMENT_AUDITOR y gate coordinador; sólo tras condición real,
publicación/verificación y cierre documental/audit separados. No continuidad funcional inferida.


## PN14 Slice1 — publicación física verificada y cierre documental pendiente

Lifecycle vigente limitado a safety net/caracterización: `IMPLEMENTED / VALIDATED / AUDITED /
ACCEPTED / PUBLISHED`, publicación `PUBLISHED_PENDING_CLOSURE`, documentación de cierre
`MATERIALIZED / READY_FOR_FRESH_INDEPENDENT_AUDIT / NOT_SELF_AUDITED`, workflow
`AUDITING_PUBLICATION_CLOSURE`, cierre `NOT_CLOSED`. Los bloques previos son snapshots
históricos; se conserva todo el mapa publicado de 30628 bytes como prefix íntegro.

Document audit task_f094cad89d53 / ctx_91c146a73d2b / msg_2ae86643da92 / status
msg_69234d45aec6 PASS y DocumentationGate task_1054afbc3810 / gate_f163c0193bdb
COMPLETED/RESOLVED/PASS sustentan aceptación única prepublicación. Publisher task_51f511e87b0e /
ctx_9b912a0cea99 / msg_c38f52b1272e / status msg_a89a98038473 publicó normalmente exact21
en 6a256f0060417533c08c8763c90bb013cf6b3aea, sole parent
12f52781177694693be7d6dc2efc71009c5f45b3. Fresh verifier task_e43c9910482e /
ctx_16c626a83113 / msg_e0a594456e26 / status msg_684b3f61af80 reportó PASS P0=P1=0,
filesModified=[]; PublicationGate task_beb02b174572 / gate_2ae21ec12eac está
COMPLETED/RESOLVED/PASS. Entrada propia localHEAD=upstream=ls-remote live origin=6a,0/0,
CLEAN WT/EMPTY staging y raw464
390bbea099fd4b514a2a6c30baf5119b5c8deef5c5279e77ebc9e4e3d39ec05e exacto.
Pins y gate result sections exactos se persisten como evidencia AJENA en
`auditoria/reviews/PN14-SLICE1-REVIEW-PUBLICACION-SAFETY-NET-CARACTERIZACION.md` §§2–4,
sin autoaudit ni autoresolución del DOCUMENTER.

Los 21 pins/6a/raw464 describen el snapshot histórico aceptado/publicado, no HEAD/hash documental
perpetuos: posteriores cambios documentales competentes de cierre conservan sus propios
profiles/allowlists/snapshots. Se preservan13 tests/helpers y originales PN14/review técnico,
tests anteriores/main/pom/config/resources/wrappers/SQL/migraciones e historia PN13.
Gates técnicos PASS gate_bc4ce966cb51: baseline590/finalfocal67/full638,0failure/error/skip,
requiredSkips0 y PostgreSQL real; ningún nuevo test/run/host ejecutado por este DOCUMENTER.

Fresh closure audit task_ac50b49949e6 PENDING, sólo después del settlement del DOCUMENTER;
PublicationClosureGate task_bdeb3de4e216 / gate_d1154275fd3e APPLICABLE/PENDING,
NOT_CLOSED. Siguiente acción: fresh independiente audit del cierre, sin fase funcional nueva.
Allowlist inicial4 append-only ESTADO/mapa/checkpoint Slice1 + review publicación nuevo;
allowlist futura exhaustiva5 en checkpoint Slice1 §8 / review publicación §6, derivada de
PN14§8 + autorización separada del usuario postécnica/publicación/cierre + convenciones PN13.
No review final creado ni PASS de cierre fabricado: lo materializará posteriormente un
DOCUMENTER separado con evidencia AJENA emitida y gate competente real; publicación/verificación
final documentales separadas, sin Git writes ni documentos extra en esta tarea.

PN13 `MATERIALIZED / ACCEPTED / PUBLISHED / CLOSED`, workflow `PUBLISHED / TERMINAL` y gates
PASS; PN14 `ACCEPTED / ACTIVE`, implementación `SLICE1_ONLY`, slices2–12 `NOT_AUTHORIZED`.
PN13-001..010 / NEW-PN13-011..016 CLOSED; combinado P0=0/P1=0/P2=1 sólo
`NEW-PN13-017 OPEN / P2 / EDITORIAL / NON_BLOCKING / IMPLEMENTATION_INDEPENDENT`, sin fix.
La matriz REUSE/REWORK/RETIRE permanece intacta. Autoridad productiva Pagos legacy parcial y
Reservas/TurnoInstructor, runtime, target PN diseñado/no implementado/no autorizado,
Programación/F2D/F2E, migración, fence y cutover UNCHANGED. Publicar safety net no activa producción.


## PN14 Slice1 — cierre auditado competente y recibo final documental local

Date2026-09-16; Run/task/dispatch run_190c06410cef / task_ce81ab0afbd3 / ctx_7609aea707f5.
Rol DOCUMENTER / PAYMENTS_SLICE1_CLOSURE_AUDIT_EVIDENCE_MATERIALIZER, SINGLE_WRITER,
DOCUMENTATION_ONLY, no auditor ni publisher. La transición competente del cierre ya ocurrió
por audit AJENO fresh y gate REAL: este append persiste su resultado, sin self-audit ni resolución
propia. Supersede únicamente los pendientes de cierre Slice1 de snapshots anteriores.

```text
PN13: MATERIALIZED / ACCEPTED / PUBLISHED / CLOSED; workflow PUBLISHED / TERMINAL; gates PASS
PN14 CONTRACT / HANDOFF: ACCEPTED / ACTIVE
SLICE1: SAFETY_NET / CHARACTERIZATION / IMPLEMENTED / VALIDATED / AUDITED / ACCEPTED / PUBLISHED / CLOSED
SLICE1 NORMATIVE WORKFLOW: PUBLISHED / TERMINAL — no functional continuation
DOCUMENTATION_GATE: PASS — task_1054afbc3810 / gate_f163c0193bdb
TECHNICAL SCOPE / TESTS / IMPLEMENTATION / HOST: PASS — task_ae6dd88b4b33 / gate_bc4ce966cb51
PUBLICATION_GATE: PASS — task_beb02b174572 / gate_2ae21ec12eac
PUBLICATION_CLOSURE_AUDIT: PASS — task_ac50b49949e6 / ctx_af03b1f26b0d / msg_9c46fc825592 / status msg_463efc9c06b4
PUBLICATION_CLOSURE_GATE: PASS — task_bdeb3de4e216 / gate_d1154275fd3e COMPLETED / RESOLVED / PASS
FINAL AJENO RECEIPT: MATERIALIZED / PENDING_FRESH_INDEPENDENT_DOCUMENT_VERIFICATION / NOT_SELF_AUDITED
FINAL RECEIPT PUBLICATION: LOCAL_UNCOMMITTED_DOCUMENTARY_CLOSURE_RECEIPT / NOT_YET_PUBLISHED
FINAL FRESH VERIFIER: task_8ef14af3963e PENDING / no executed result at materialization
FINAL DOCUMENTARY PUBLICATION AUTHORIZATION: task_f4f5de6333dd / gate_3a084176596d PENDING
IMPLEMENTATION AUTHORITY: SLICE1_ONLY / NO_FURTHER_WRITES
SLICES2–12: NOT_AUTHORIZED / NO_AUTOMATIC_NEXTSLICE
PN13-001..PN13-010 / NEW-PN13-011..NEW-PN13-016: CLOSED / CLOSED
NEW-PN13-017: OPEN / P2 / EDITORIAL / NON_BLOCKING / IMPLEMENTATION_INDEPENDENT
NEW CLOSURE FINDINGS P0/P1/P2: 0/0/0; COMBINED OPEN P0/P1/P2: 0/0/1 solely NEW-PN13-017
TARGET PN13 PRODUCTION: DESIGNED_NOT_IMPLEMENTED / NOT_AUTHORIZED
PRODUCTIVE AUTHORITY / LEGACY / RESERVAS / PROGRAMACION / RUNTIME / SQL / MIGRATION / F2D / F2E / FENCE / CUTOVER: UNCHANGED / NO_NEW_AUTHORIZATION
TESTS / HOST RUNS BY THIS DOCUMENTER: NOT_APPLICABLE / NOT_EXECUTED
```

Audit AJENO fresh READ_ONLY/ADVERSARIAL/INDEPENDENT task_ac50b49949e6/ctx_af03b1f26b0d,
uniqueDone msg_9c46fc825592 y status msg_463efc9c06b4, COMPLETED/succeeded/settled/accepted/
released, filesModified=[], nuevosP0=P1=P2=0. ClosureGate task_bdeb3de4e216/gate_d1154275fd3e
COMPLETED/RESOLVED/PASS, provenance coordinator_gate_resolution, resuelto2026-09-16 18:37:11.
Binding exact4 initialclosurecandidate raw465
53491b210644ba452167b81e0da44b68156d6e75728861cbdd60506559aae308:
**AUDITED_HISTORICAL_CLOSURE_SNAPSHOT**, con sus cuatro pins y resultados JSON relevantes
decodificados literalmente en
`auditoria/reviews/PN14-SLICE1-REVIEW-CIERRE-PUBLICACION-SAFETY-NET-CARACTERIZACION.md`.
Las marcas internas PENDING/NOT_CLOSED del audit y review de publicación describen aquel
corte previo a la resolución real; quedan inmutables como historia. No son el lifecycle vigente.
Los pins/raw465/6a son snapshots de provenance, no requisitos perpetuos de currentdocHEAD/hash;
estos nuevos append competentes tienen scope y verificación final independientes.

Preflight propio anterior a cualquier write: worktree/branch exactos, HEAD=upstream=liveorigin
6a256f0060417533c08c8763c90bb013cf6b3aea,0/0,EMPTY staging, índice67ee2cdc… intacto,
raw465 MATCH y WT exact4dirty MATCH contra gate/audit actuales, source+pom362 raw
b8da272df924b885c4466ba3a92fc7c6a90ab5ddb296bc1d31fe5e570b98315a.
Se preservan TODOS los bytes de entrada propios (ESTADO44209/mapa34215/checkpoint21552),
y prefixes publicados39480/30628/16280; review publicación bdceb0a0418e8895a2b8276a939a264e51d88cba84a5f701cd7739aa67954185 inmutable.
13tests/helpers, cuatro originalesPN14, technicalreview, historiaPN13, dominio/arquitectura/
decisiones y TODOS los otros archivos quedan intactos. Scope propio EXACT FOUR WRITES:
append ESTADO/mapa/checkpoint y crear sólo reviewCIERRE; apply_patch únicamente, sin Git writes.

Evidencia técnica AJENA preservada baseline590/finalfocal67/full638 PASS,0failure/error/skipped,
requiredSkips0 y PostgreSQL real M12; TA-001/002/003 CLOSED. No nuevos runs, fixes/reaperturas
PN13, cambio de reglas/legacy ni autorización target/runtime/migración/F2E/fence/cutover.
El safety net precede SQL y conserva LEGACY_NOT_TARGET; no continuidad slices2–12.

Publicación final documental — condición competente protectora exact5:

La publicación de este recibo documental final sólo se autoriza **si y sólo si**:

1. task_8ef14af3963e completa succeeded con un único worker_done competente aceptado,
   FINAL_CLOSURE_MATERIALIZATION_VERIFICATION=PASS, P0=0/P1=0 y filesModified=[],
   fresh e independiente de todos los escritores, preservando autoridad y gates anteriores.
2. task_f4f5de6333dd completa y gate_3a084176596d se resuelve PASS realmente por el coordinador,
   provenance coordinator_gate_resolution; candidateFileSHA256 y acceptedPublishPaths enlazan
   exhaustivamente los CINCO paths de abajo y TODOS sus bytes actuales exactos, iguales al
   snapshot final del DOCUMENTER, verificador fresh y comprobación independiente de integridad
   del coordinador, sin omisiones, extras, mutación posterior, decisión humana ni SECURITY_STOP.
3. Un PUBLISHER separado verifica ese binding y el preflight físico pertinente antes del
   stage exacto, commit normal y push normal en la branch actual; publicación/verificación
   posteriores son etapas separadas. Cualquier mismatch falla cerrado; no se inventa SHA futuro.

Este gate adicional es autorización de scope/lifecycle de publicación documental, no un nuevo
gate de producto/dominio ni reapertura de Slice1. Protege TODOS los bytes añadidos después del
ClosureGate inicial: ningún byte postgate puede publicarse sin su verificación independiente.
En este corte verifier PENDING/no resultado y gate final PENDING/no resolución; READY_TO_PUBLISH
del recibo final=NO. Los SHA finales se entregan externamente, sin self-hash o ciclo criptográfico.

```json
[
  "auditoria/ESTADO-ACTUAL.md",
  "auditoria/contexto/MAPA-LEGACY-Y-MIGRACION.md",
  "auditoria/fase-pn14-slice1-safety-net-caracterizacion.md",
  "auditoria/reviews/PN14-SLICE1-REVIEW-PUBLICACION-SAFETY-NET-CARACTERIZACION.md",
  "auditoria/reviews/PN14-SLICE1-REVIEW-CIERRE-PUBLICACION-SAFETY-NET-CARACTERIZACION.md"
]
```

La matriz REUSE/REWORK/RETIRE, coexistencia y compatibilidad API anteriores permanecen íntegros.
