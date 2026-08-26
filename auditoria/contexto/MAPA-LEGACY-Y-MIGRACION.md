# FeelingPilates — Mapa legacy y migración

Status: CANONICAL
Last updated: 2026-08-25
Repository verification: VERIFIED
Last verified against commit:
8c40594d2caf8b5230b364cb76cd8f48fe5ed98a
Verification scope: autoridad productiva reconstruida hasta F2C; F2D pendiente

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

Existe infraestructura interna para el nuevo modelo, pero no constituye la autoridad productiva.

El estado exacto debe volver a verificarse contra repositorio y datos antes de cualquier activación.

## Readers activos conocidos

### Legacy

- frontend web;
- `ReservaService`;
- servicios/backend relacionados con `TurnoInstructor`.

### Nuevo

Uso interno limitado conocido, incluido impacto sobre horarios.

No existe evidencia de que frontend/reservas sean consumers productivos de `BloqueProgramacion + Asignacion`.

## Consumers

- frontend web: legacy;
- reservas actuales: legacy;
- mobile: no confirmado como consumer del modelo nuevo.

## ¿Puede coexistir productivamente?

**NO COMO DOS AUTORIDADES SIMULTÁNEAS**

No existe un cutover ejecutado que permita considerar ambos modelos equivalentes.

## Estado de migración

**EN\_PREPARACION**

## Condiciones de cutover

Todavía deben cerrarse y materializarse, como mínimo:

- diseño F2D aprobado;
- identidad inequívoca de programación futura;
- estrategia sobre reservas;
- auditoría de datos;
- estrategia de writers/consumers;
- gate de activación.

## Fence / estrategia

**NO IMPLEMENTADO**

F2D.1 fue revisada y requiere ajustes.

Se discutieron estrategias futuras de aislamiento/cutover, pero ninguna debe tratarse como implementada o aprobada hasta ejecutar F2D.1.1 y su gate.

## Bloqueadores / deuda

- dualidad legacy/nuevo;
- F2D.1 todavía no aprobada;
- programación nueva no es autoridad;
- relación de reservas con la fuente nueva no resuelta;
- potenciales invariantes de serie/concurrencia pendientes.

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

F2D no cerrada.

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
