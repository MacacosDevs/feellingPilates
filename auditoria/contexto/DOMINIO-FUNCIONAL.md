# FeelingPilates — Dominio funcional

Status: CANONICAL
Last updated: 2026-08-25
Repository verification: VERIFIED
Last verified against commit:
8c40594d2caf8b5230b364cb76cd8f48fe5ed98a
Verification scope: invariantes funcionales conocidas

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
