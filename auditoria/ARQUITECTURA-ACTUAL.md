# FeelingPilates — Arquitectura actual

Status: CANONICAL
Last updated: 2026-08-26
Repository verification: VERIFIED
Last verified against commit:
95900d8a1d787a24aff4ee4e10f69d540ce81339
Verification scope: arquitectura actual; F2D.2 materializada en commit Git verificado, en dark launch y en revisión documental

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

**IMPLEMENTADA_EN_REVIEW / PENDIENTE_DE_DOCUMENTACION**

El diseño de la futura capa de ajustes puntuales fue aprobado y F2D.1 quedó cerrada después de un gate final `P0=0 / P1=0 / P2=0`.

F2D.2 está implementada como dark launch aislado y su implementación cuenta con aprobación
técnica; la documentación permanece en revisión. Durante esta fase ningún estado exclusivo de la
programación nueva puede alterar flujos productivos legacy.

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
IMPLEMENTADA_EN_REVIEW — dark launch interno; documentación en revisión

Reservas:
modelo legacy actual — PRODUCTIVO
```

La autoridad concreta de cada transición se mantiene en:

`contexto/MAPA-LEGACY-Y-MIGRACION.md`
