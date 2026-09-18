# FeelingPilates — Arquitectura actual

Status: CANONICAL
Last updated: 2026-09-15
Repository verification: VERIFIED
Last verified against commit:
a0ec85818b771d4ac924b427fa1e90244ea9fe8e
Verification scope: arquitectura actual, F2D preservado y contraste físico PN-13 frente a diseño no implementado

## 1. Visión general

FeelingPilates se encuentra estructurado actualmente como un sistema principalmente monolítico con:

```text
Frontend web React/Vite
          ↓
Backend Java / Spring Boot
          ↓
PostgreSQL
          ↓
Flyway
```

Existe además una aplicación móvil independiente.

La reestructuración está evolucionando el backend hacia límites de dominio más claros sin sustituir de forma abrupta el comportamiento legacy.

## 2. Estados utilizados

### PRODUCTIVO

Componente utilizado por flujos reales conocidos.

### LEGACY\_VIVO

Componente heredado que sigue siendo funcionalmente autoritativo o consumido y no puede retirarse todavía.

### IMPLEMENTADO\_NO\_PRODUCTIVO

Código/persistencia existente que todavía no es la autoridad productiva principal.

### EN\_TRANSICION

Componente o área que mantiene compatibilidad entre arquitectura antigua y nueva.

### DISEÑADO\_NO\_IMPLEMENTADO

Diseño existente pero no materializado en código.

### PENDIENTE

Área futura todavía no diseñada o implementada completamente.

---

# 3. Backend

## Estado

**PRODUCTIVO**

Aplicación Spring Boot con persistencia PostgreSQL y migraciones Flyway.

El sistema todavía contiene límites de paquete y conceptos legacy que están siendo sustituidos progresivamente.

No se ha realizado una migración Big Bang.

---

# 4. PostgreSQL y Flyway

## Estado

**PRODUCTIVO**

PostgreSQL no actúa únicamente como almacenamiento.

En las fases cerradas se utiliza también para proteger invariantes como:

- unicidad;
- rangos temporales;
- exclusiones;
- concurrencia defensiva.

Flyway es la autoridad sobre la evolución del esquema versionado.

La migración V47 de F2D.2 está presente únicamente como infraestructura de dark launch; no cambia la
autoridad productiva ni implica integración con flujos legacy.

---

# 5. Salones y horario operativo

## Salón

Estado:

**PRODUCTIVO**

Representa el contexto físico/operativo donde ocurren actividades y reservas.

## `HorarioOperacion`

Estado:

**PRODUCTIVO**

El horario operativo fue endurecido durante F2A/F2B.

Soporta vigencias temporales y resolución por fecha.

La persistencia protege solapamientos temporales mediante restricciones PostgreSQL introducidas durante las fases cerradas.

## `SalonHorarioExcepcion`

Estado:

**PRODUCTIVO**

Modelo anterior a F2C que fue preservado y endurecido en lugar de reemplazarse.

Representa una excepción del horario operativo para una fecha concreta.

Puede expresar conceptualmente:

- cierre;
- horario especial.

La representación persistida sigue usando un booleano `cerrado`.

## `HorarioEfectivoSalon`

Estado:

**PRODUCTIVO**

Resuelve el horario operativo aplicable a una fecha con precedencia:

```text
excepción activa exacta
→ horario semanal vigente
→ NO_OPERATIVO
```

F2C confirmó que este resolver no necesitaba ser sustituido.

---

# 6. Actividades y especialidades

## Actividades

Estado:

**PRODUCTIVO**

Representan los tipos de práctica ofrecidos por el estudio, por ejemplo Reformer o Mat.

## Especialidades de instructor

Estado:

**PRODUCTIVO**

Determinan qué actividades puede impartir un instructor.

Constituyen una restricción funcional relevante para programación.

---

# 7. Programación legacy — `TurnoInstructor`

## Estado

**LEGACY\_VIVO / PRODUCTIVO**

`TurnoInstructor` continúa siendo la programación productiva conocida.

Dispone de infraestructura real de servicio/controller y es consumido por frontend y reservas.

Tipos conocidos:

```text
RECURRENTE
EXCEPCION
CANCELACION
```

La semántica legacy no coincide completamente con el modelo futuro deseado.

En particular, F2D.1 identificó que `EXCEPCION` no constituye un mecanismo adecuado de reemplazo individual de una regla recurrente.

`TurnoInstructor` no debe considerarse retirado.

---

# 8. Reservas legacy

## `Reserva`

Estado:

**PRODUCTIVO**

Representa reservas concretas de clientes.

Mantiene información funcional como:

- salón;
- instructor;
- actividad;
- fecha;
- rango;
- estado.

No dispone de una relación confirmada con `programacion.Asignacion` ni con futuros ajustes F2D.

## `ReservaService`

Estado:

**PRODUCTIVO / LEGACY\_VIVO**

Sigue dependiendo funcionalmente del universo de `TurnoInstructor`.

Después de F2C también valida el horario operativo mediante `HorarioEfectivoSalon` y participa del locking por salón para evitar carreras con excepciones operativas.

Esto no lo convierte todavía en consumer de la programación nueva.

---

# 9. Nueva programación recurrente

## `BloqueProgramacion`

Estado:

**IMPLEMENTADO\_NO\_PRODUCTIVO**

Representa un contenedor recurrente dentro del nuevo modelo de programación.

Según el último inventario conocido:

- no es la autoridad productiva;
- no tiene un flujo público equivalente al legacy;
- existe infraestructura interna y persistencia.

## `Asignacion`

Estado:

**IMPLEMENTADO\_NO\_PRODUCTIVO**

Representa conceptualmente la asignación recurrente de:

```text
instructor
+
actividad
+
intervalo
```

dentro de programación.

Dispone de conceptos de serie y vigencia.

F2D.1 cerró documentalmente el diseño del hardening de unicidad temporal de las versiones de una
serie. F2D.2 lo materializa internamente en dark launch; `Asignacion` sigue sin ser autoridad
productiva.

## Autoridad

`BloqueProgramacion + Asignacion` **NO son actualmente la autoridad productiva de programación**.

Consultar `contexto/MAPA-LEGACY-Y-MIGRACION.md` para la autoridad de la transición.

---

# 10. Ajustes puntuales F2D

Estado:

**IMPLEMENTADO_NO_PRODUCTIVO / DARK_LAUNCH**

El diseño de la futura capa de ajustes puntuales fue aprobado y F2D.1 quedó cerrada después de un gate final `P0=0 / P1=0 / P2=0`.

F2D.2 está cerrada documentalmente, materializada como dark launch aislado y cuenta con aprobación
técnica y documental. Su publicación y cierre de publicación están verificados. Ningún estado
exclusivo de la programación nueva puede alterar flujos productivos legacy.

La afirmación anterior de que la materialización «NO HA INICIADO» corresponde al corte anterior y
queda obsoleta. La evidencia física autorizada confirma exclusivamente la implementación interna
de `AjusteProgramacionFecha`, V47, persistencia de ajustes, `InstructorLocks`, multi-locks y
`ProgramacionEfectiva`.

No existen API, controllers ni consumers F2D productivos, ni cutover o fence F2D.

`TurnoInstructor` continúa como autoridad productiva única. `BloqueProgramacion + Asignacion` permanece `IMPLEMENTADO_NO_PRODUCTIVO`.

---

# 11. Frontend web

Estado:

**PRODUCTIVO**

Tecnología conocida:

- React;
- Vite;
- TypeScript;
- MUI;
- Axios;
- Zustand;
- React Router.

El frontend fue estabilizado y migrado parcialmente durante F2B para los horarios operativos versionados.

En programación sigue existiendo consumo del modelo legacy `TurnoInstructor`.

No debe asumirse que el frontend consume `BloqueProgramacion`, `Asignacion` o futuros ajustes F2D.

---

# 12. App móvil

Estado:

**PRODUCTIVO**

Existe como aplicación separada.

La nueva arquitectura de programación todavía no puede considerarse integrada con ella.

En el inventario más reciente sobre programación no se identificó un consumo equivalente de los nuevos modelos.

Las futuras búsquedas de sesiones/programación efectiva pertenecen a fases posteriores.

---

# 13. Equipamiento y capacidad

Estado:

**PENDIENTE / PARCIALMENTE EXISTENTE**

Existe un concepto funcional de máquinas/equipamiento.

El modelo final para disponibilidad compartida y consumo por actividad todavía no forma parte de una fase cerrada.

La arquitectura futura deberá permitir capacidad por:

```text
salón
+
recurso
+
intervalo
```

pero esta lógica no debe darse por implementada.

---

# 14. Pagos

Estado:

**PRODUCTIVO PARCIAL / EN TRANSICION**

Existe funcionalidad comercial previa y una integración Stripe parcial conocida, incluyendo elementos como PaymentIntent/webhook/reconciliación.

La arquitectura comercial futura de venta/pago/beneficios ha sido discutida pero no está materializada íntegramente.

No debe tratarse como una fase cerrada de la actual reestructuración.

---

# 15. Notificaciones

Estado:

**PENDIENTE / IMPLEMENTACION ACTUAL LIMITADA**

Existe infraestructura limitada/stub.

Se ha discutido una futura arquitectura asíncrona, pero no debe considerarse implementada.

---

# 16. Resumen de autoridades estructurales

```text
Horario operativo:
modelo temporal nuevo/endurecido — PRODUCTIVO

Excepciones de horario:
SalonHorarioExcepcion endurecido — PRODUCTIVO

Programación:
TurnoInstructor — LEGACY_VIVO / PRODUCTIVO

Programación nueva:
BloqueProgramacion + Asignacion — IMPLEMENTADO_NO_PRODUCTIVO

Ajustes F2D:
IMPLEMENTADO_NO_PRODUCTIVO — F2D.2 cerrada documentalmente; dark launch interno

Reservas:
modelo legacy actual — PRODUCTIVO
```

La autoridad concreta de cada transición se mantiene en:

`contexto/MAPA-LEGACY-Y-MIGRACION.md`

---

# 17. Pagos — verdad física PN-13

## 17.1 Actualmente implementado / legacy vivo

Estado:

**PRODUCTIVO PARCIAL / LEGACY_EVOLUTION_SOURCE / EN_TRANSICION**

La evidencia física del baseline PN contiene `com.feelingpilates.pagos` con:

- `Paquete` mutable (`precioCentavos`, `vigenciaDias`, estado activo y metadata de presentación);
- `PaqueteActividad`, que ya relaciona un paquete con actividades existentes y cantidad explícita;
- `Compra`, que hoy mezcla producto adquirido, estado monetario, método, PaymentIntent, expiración,
  venta de caja, agrupación de ticket y motivo de estado;
- `PagoService`, que crea/reutiliza PaymentIntent, procesa directamente webhooks firmados y
  reconcilia compras pendientes;
- `VentaService`, que registra efectivo o transferencia como `pagada` inmediatamente y calcula
  expiración desde el paquete actual;
- `PaqueteGestionService`, catálogo mutable;
- `CompraRepository`, `PaqueteRepository`, `PagoController`, `VentaController`,
  `PaqueteController`, `PaqueteGestionController` y `StripeConfig`;
- dependencia `stripe-java` y claves/configuración Stripe existentes.

Endpoints físicos relevantes:

```text
GET    /api/publico/paquetes
POST   /api/pagos/paquetes/{paqueteId}/intento
GET    /api/pagos/mis-paquetes
GET    /api/pagos/mis-compras
POST   /api/pagos/webhook
GET    /api/ventas/sedes
POST   /api/ventas
POST   /api/ventas/carrito
PATCH  /api/ventas/{id}/reembolsar
GET    /api/ventas
GET    /api/ventas/buscar
GET    /api/ventas/servicios
POST   /api/ventas/servicios
PUT    /api/ventas/servicios/{id}
PATCH  /api/ventas/servicios/{id}/deshabilitar
PATCH  /api/ventas/servicios/{id}/habilitar
```

La firma Stripe se verifica contra el raw body recibido por el controller, existe unicidad para
`stripe_payment_intent_id` e `idempotency_key`, y existe reconciliación programada básica. Estas
capacidades son evidencia reutilizable, no prueban el contrato PN objetivo.

Limitaciones físicas confirmadas:

- no existe `OrdenVenta` propia; `grupo_compra_id` sólo agrupa líneas de caja;
- no hay snapshot inmutable completo: lecturas y vigencia dependen de `Paquete` mutable;
- no existe entidad `Pago` separada ni varios intentos por orden;
- transferencia carece de `PENDIENTE_VALIDACION` y autoridad separada;
- `pagada` acredita de hecho sólo una fecha, sin `Acreditacion` durable;
- `payment_intent.payment_failed` puede escribir `fallida` sin una máquina monotónica cerrada;
- no hay Inbox Stripe durable, deduplicación por `event.id` ni estado de procesamiento;
- no hay ledger, derecho por actividad, compromiso de reserva, cuota de cancelación, crédito de
  recuperación, bloqueo de derechos, reembolso con lifecycle propio ni contención de disputas;
- la ruta legacy de reembolso sólo cambia `Compra.estado`; V35 retiró el permiso/ruta Stripe
  anterior, mientras caja conserva `/api/ventas/{id}/reembolsar`;
- no hay suite robusta de Pagos/Stripe: el inventario de tests no contiene tests focalizados del
  paquete `pagos`.

Las migraciones V22.1–V35 son historia física: crean/evolucionan catálogo, compra, idempotencia,
actividades, caja, agrupación, sede, motivos y permisos. No deben renombrarse ni reinterpretarse
como implementación PN.

## 17.2 Diseñado por PN-13 / no implementado

Estado:

**OBJETIVO RESTANTE: DISEÑADO_NO_IMPLEMENTADO / NOT_PRODUCTIVE / IMPLEMENTATION_NOT_AUTHORIZED**

La ausencia física de orden/snapshot comercial indicada por el inventario legacy §17.1 se
limita a su modelo productivo; la fundación interna existente se clasifica en §17.3.
La ausencia de tests pagos de aquel inventario es histórica: existe safety net de caracterización.
Fuera de esa fundación, siguen sin existir los límites, tipos, tablas o servicios objetivo de
pago/acreditación separados, derechos y ledgers, compromisos, Inbox, reembolso, disputa,
Outbox ni workers idempotentes definidos por DA-014–DA-022 y el checkpoint PN-13.

La corrección residual de autoridad especifica además seis buckets separados de ledger,
settlement pointer y FKs de cliente, FEFO serializado, vigencia con `fechaVencimiento` incluida y
límites exclusivos en `America/Mexico_City`, asistencia pendiente,
transferencia/evidencia/validación separadas, rechazo `ORDEN_YA_LIQUIDADA`, refund excepcional
payment-scoped, contención exacta refund/disputa por origen y precedencia terminal de
Notificación. Todo ello permanece **DISEÑADO_NO_IMPLEMENTADO**: no existen sus tablas, columnas,
constraints, índices, migraciones, jobs ni código runtime en este worktree. La mayor precisión
documental no cambia autoridad productiva ni autoriza PN-14.

## 17.3 Fundación interna Orden + snapshot comercial

Estado: **IMPLEMENTADO_NO_PRODUCTIVO / INTERNAL_FOUNDATION / NOT_PRODUCTIVE**.

`com.feelingpilates.pagos.ventas.dominio` contiene OrdenVenta, Compra,
CompraComponenteSnapshot, ImporteMonetario, PoliticaComercialSnapshot, ProvenienciaSnapshot y
ContenidoSnapshotCanonico: identidad comercial, importes checked, políticas/versiones,
componentes defensivos inmutables y representación canónica/hash, sin Spring/JPA/Stripe.
Aplicación contiene CongelarOrdenSnapshot, sus puertos, BackfillOrdenSnapshot e informe
append-only, ConsultaHistoricaSnapshot y CompraHistoricaSnapshot. Infraestructura contiene
cuatro adapters JDBC para freeze transaccional, fuente legacy raw, informe y consulta frozen.
Las dependencias apuntan hacia dominio/puertos; no hay controller, scheduler, configuración,
bean o wiring productivo que active esta fundación.

V48 añade orden_venta, compra_componente_snapshot e informe_backfill_snapshot y columnas
snapshot nullable aditivas en compra; V49 protege canon, ownership por FKs compuestas,
inmutabilidad y sello atómico. La Compra legacy sigue siendo el único mapping JPA writable
sobre compra. JDBC escribe exclusivamente columnas snapshot nuevas de compras existentes;
los writers legacy mantienen sus campos financieros/estado/vigencia/motivo. La consulta
histórica interna proyecta valores congelados sin catálogo ni fallback para bundle NULL.
Los adapters se instancian explícitamente; persistencia existente no implica uso productivo,
backfill live, cambio de readers/writers, activación de beneficios o cutover.

# 18. Reservas y Notificaciones — verdad física PN-13

## 18.1 Reservas actuales

`com.feelingpilates.calendario.entidad.Reserva` es **PRODUCTIVO / LEGACY_VIVO** y conserva salón,
instructor, cliente, actividad, fecha, horas y sólo `CONFIRMADA | CANCELADA`.
`ReservaService` valida horario efectivo y turno legacy, usa `SalonLock`, crea/cancela reservas y
no consulta compras, no compromete créditos y no registra asistencia/no-asistencia o consecuencia
económica. Sus endpoints actuales son:

```text
GET    /api/reservas
GET    /api/reservas/mias
POST   /api/reservas
DELETE /api/reservas/{id}
```

La integración `GestorCreditoReserva` de DA-016 y el lifecycle
`PENDIENTE_ASISTENCIA -> ASISTIDA | NO_ASISTIDA` administrado sólo por ADMIN están
**DISEÑADOS_NO_IMPLEMENTADOS**. No autorizan alterar la autoridad de Programación/Reservas ni crean
un segundo módulo de reservas; `Reserva` física actual continúa sin esos estados ni movimientos.

## 18.2 Notificaciones actuales

`EmailService` sólo declara envío de invitación de cliente y `EmailServiceConsola` lo simula en
logs sin PII. Existe un test acotado de ese adaptador. No hay proveedor real, push, dispositivos,
templates versionados, notificación lógica, intento de entrega, scheduling durable, reintentos,
Outbox o deduplicación.

La arquitectura de DA-020, incluidos los tres niveles `Notificacion -> EntregaLogica ->
IntentoEntrega` y su agregación terminal, está **DISEÑADA_NO_IMPLEMENTADA / NOT_PRODUCTIVE**. No
existen tablas, Outbox, workers ni migraciones que la materialicen. Ningún documento PN-13 afirma
que email/push o los eventos de pago/reserva estén activos. En particular, tampoco existen la
precedencia `REQUIERE_REVISION` por outcome incierto ni la distinción de planificación
`SIN_CANALES_DISPONIBLES` frente al fallo posterior de una entrega ya creada.
