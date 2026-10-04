# Fase 2B.2 — Migrar consumers a horario temporal

## Base

- Branch base: `operacion/horario-versionado-core`
- Commit base: `56504b145165c324e58b26974471fd62bfde7fc1`
- Branch de trabajo: `operacion/horario-consumidores-temporales`
- Baseline verificado antes de tocar código: **169/169 tests PASS**, working tree limpio,
  sin divergencia con origin.

## Objetivo

Que los consumers de `HorarioOperacion` dejen de depender semánticamente de "una fila por
salón/día", **sin retirar todavía** `UNIQUE(salon_id, dia_semana)`. Esta fase no introduce
writers de versionado ni migraciones.

---

## 1. Clock central

**IMPLEMENTADO** (`config/RelojConfig.java`).

Antes de crearlo se buscó una fuente temporal existente: el proyecto **no** tenía ningún
`Clock`, `ZoneId`, ni configuración de zona horaria (`application.properties`,
`application-dev/prod.properties` y todo `src/main/java` no declaran ninguna). Los únicos
`now()` del código productivo eran `Instant.now()` en `JwtService` y `GlobalExceptionHandler`,
ajenos a horarios de negocio.

Por eso el bean es `Clock.systemDefaultZone()`: preserva exactamente la semántica actual (la
zona por defecto de la JVM del servidor). Fijar `America/Mexico_City` o `UTC` habría sido un
cambio de zona de negocio encubierto, fuera del alcance de F2B.2, cuya finalidad es
**centralizar** la fuente temporal, no cambiarla.

`fechaNegocio = LocalDate.now(reloj)` se usa sólo en los dos puntos que necesitan un "hoy":

| Consumer | Usa Clock | Por qué |
|---|---|---|
| `SalonService.mapDetalle` / comparación de horarios | Sí | necesita "la configuración vigente hoy" |
| `TurnoInstructorService` RECURRENTE | Sí | su objetivo temporal es `[hoy, +∞)` |
| `BloqueProgramacionService` | **No** | el comando ya trae vigencia explícita |
| `HorarioEfectivoSalon` | **No** | recibe la fecha como argumento |

No quedan `LocalDate.now()` sin reloj en las rutas migradas.

## 2. Cobertura temporal

**IMPLEMENTADA** — `ubicaciones/dominio/CoberturaVigencia.java`.

Dominio neutral: no conoce horarios, turnos ni bloques. Entrada: un `RangoVigencia` objetivo y
una colección de `RangoVigencia` disponibles. Salida: `Resultado(completa, gapDesde, gapHasta)`;
el gap es informativo para mensajes y tests.

- Barrido de una sola pasada sobre los rangos ordenados por `desde` (NULLS FIRST):
  **O(n log n)** por el orden, O(n) si ya viniera ordenado. **Nunca itera fecha por fecha.**
- Los rangos que no intersectan el objetivo se descartan antes del barrido.
- El infinito es estado lógico (`null`), nunca `LocalDate.MIN`/`MAX` como centinela.
- La aritmética de fronteras usa `ChronoUnit.DAYS.between` en vez de `plusDays`/`minusDays`
  sobre valores que podrían ser `LocalDate.MIN`/`MAX`, para no desbordar. `LocalDate.MAX` se
  trata como fecha Java válida y distinta de +∞.

Una sola implementación compartida por `TurnoInstructorService` y `BloqueProgramacionService`.

## 3. HorarioEfectivoSalon

**IMPLEMENTADO** — `ubicaciones/servicio/HorarioEfectivoSalon.java`, con el tipo cerrado
`ubicaciones/dominio/HorarioEfectivo.java`.

Composición de `SalonHorarioExcepcion` + `HorarioOperacionResolver`. Prioridad:

1. excepción activa CERRADO → `CERRADO`
2. excepción activa con apertura/cierre → `ABIERTO` (horario de la excepción)
3. sin excepción, versión semanal vigente → `ABIERTO` (horario base)
4. sin excepción ni versión vigente → `NO_OPERATIVO`

`CERRADO` y `NO_OPERATIVO` **no se colapsan**: son semánticas distintas y el tipo lo impone
(no se devuelve `Optional`). `ABIERTO` porta siempre `horaApertura`/`horaCierre`; el
constructor canónico rechaza las combinaciones imposibles.

**Recibe la fecha explícitamente y nunca llama a `LocalDate.now()`.** Eso mantiene los tests
deterministas, habilita consultas de fechas arbitrarias y deja la puerta abierta a la
materialización futura.

### Decisión de diseño: `Origen`

El record lleva, además del `Estado`, un `Origen` (`EXCEPCION` / `SEMANAL` / `NINGUNO`).
Fue necesario para **preservar los mensajes existentes** de `TurnoInstructorService`, que
distingue "horario especial del salón ese día" de "horario de atención del salón ese día".
Sin `Origen`, la migración habría cambiado mensajes ya caracterizados por tests.

### Caso 5 confirmado contra el código

`SalonHorarioExcepcionService.guardar` **nunca consulta `HorarioOperacion`**: una excepción con
horario especial puede existir en un día sin plantilla semanal. Por tanto una excepción abierta
aplica aunque no haya horario semanal ese día — es la semántica actual del modelo, no una
invención de esta fase. `SalonHorarioExcepcion` **no se modificó**.

## 4. SalonService

**MIGRADO.**

### Update destructivo: ELIMINADO

El patrón anterior era literalmente destructivo y peor de lo documentado:

```java
private void reemplazarHorarios(Salon salon, List<HorarioOperacionRequest> horarios) {
    horarioOperacionRepository.deleteBySalonId(salon.getId());
    horarioOperacionRepository.flush();
    if (horarios == null) return;   // <-- el delete ya ocurrió
    ...
}
```

El `return` por `horarios == null` estaba **después** del `deleteBySalonId`: un PUT sin horarios
borraba todos los horarios del salón. Eso ya no ocurre; `actualizar` no invoca `deleteBySalonId`
en ningún camino.

### Reglas de `actualizar`

| `request.horarios()` | Comportamiento |
|---|---|
| `null` | no se tocan los horarios. Sin delete, sin reinsert, sin limpieza |
| equivalente a la configuración semanal efectiva de hoy | **no-op**; el resto del salón sí se actualiza |
| distinto | **RECHAZO** (`ValidacionException`) |
| `[]` con horarios actuales | **RECHAZO** (mutación explícita "quitar todos") |
| `[]` sin horarios actuales | no-op (cae naturalmente de la comparación) |

### Comparación semántica

Independiente del orden de la lista: se canonicaliza por `diaSemana` en un `Map` y se comparan
`(diaSemana, horaApertura, horaCierre)`. **No se comparan IDs**: el request no modela el id como
identidad de negocio. Se detectan duplicados de día en el request y se preservan las validaciones
existentes (`horaCierre > horaApertura` se valida **antes** que la comparación, para conservar
el mensaje actual).

### Código/mensaje de rechazo

Constante `SalonService.HORARIOS_REQUIEREN_VERSIONADO`:

```
HORARIOS_REQUIEREN_VERSIONADO: los horarios de operación del salón se versionan en el
tiempo y no pueden modificarse desde la actualización del salón
```

Se usa la estrategia de excepción existente (`ValidacionException`); **no** se introdujo ningún
framework global de errores nuevo.

### Creación

`crearHorariosIniciales` está separado conceptualmente de la actualización: sólo inserta, no
borra. Un salón nuevo no tiene historia que preservar ni versión previa con la cual comparar.

### mapDetalle

De las filas del salón conserva únicamente aquellas cuyo `RangoVigencia(vigenteDesde,
vigenteHasta)` contiene `fechaNegocio`. Después debe haber **como máximo una por `diaSemana`**;
dos versiones vigentes el mismo día lanzan `IllegalStateException` (**fallo ruidoso**, nunca
`findFirst` silencioso). Los días sin versión vigente no aparecen. No se expone historia.

Con los datos legados actuales (filas `NULL/NULL`) la respuesta es **idéntica** a la anterior.

### Transaccionalidad

La validación de horarios ocurre **antes** de `aplicarDatosBase`, de modo que un rechazo no deja
cambios parciales de otros campos pendientes de dirty checking. Hay test explícito de que tras el
rechazo ni el teléfono ni el nombre quedan aplicados y `salonRepository.save` no se invoca.

## 5. TurnoInstructorService

**MIGRADO** (ambos tipos).

- **diaSemana**: se eliminó el `diaSemanaIso` local duplicado; ahora delega en
  `DiaSemanaOperacion.desde` (domingo=0 … sábado=6). Resultados sin cambio.
- **EXCEPCION** (fecha concreta): valida contra `HorarioEfectivoSalon`. CERRADO → rechazo;
  horario especial → se valida contra ese intervalo; sin excepción → versión semanal vigente
  **esa fecha**; sin nada → rechazo. Mensajes preservados exactamente.
- **RECURRENTE** (`fecha == null`): no tiene vigencia propia, es una regla abierta al futuro.
  Objetivo temporal `RangoVigencia(fechaNegocio, null)`, consultado con
  `findVersionesQueIntersectan(salonId, diaSemana, fechaNegocio, null)`. Se exige, en este orden:
  1. **cobertura completa** de `[hoy, +∞)` — vacío, gap o última versión finita ⇒ rechazo;
  2. **`allMatch`** (nunca `anyMatch`): todas las versiones aplicables deben contener el rango
     horario del turno.

  Ejemplo protegido por test: 08-20 hasta agosto + 09-20 desde septiembre rechaza un recurrente
  08-09, aunque hoy quepa.

Compatibilidad legacy: con la única fila `NULL/NULL` el resultado es idéntico al anterior.
Ningún test legacy cambió su expectativa; sólo se adaptó el *setup* de mocks al nuevo método de
repositorio (cambio de dependencias, no de resultado).

## 6. BloqueProgramacionService

**MIGRADO.**

`validarDentroDelHorarioOperacion` usa `findVersionesQueIntersectan` + `RangoVigencia` +
`CoberturaVigencia`. **No necesita Clock**: el comando ya trae `vigenteDesde` obligatorio y
`vigenteHasta` nullable, así que el objetivo es exactamente esa vigencia.

Se exige cobertura completa de la vigencia del bloque (vacío / gap / bloque abierto con última
versión finita ⇒ rechazo) y después `allMatch` de contención horaria. Sin iteración día a día.
Mensaje preservado: `"El bloque debe estar contenido en el horario de operación del salón"`.

`vigenciasSeIntersectan` ahora **delega** en `RangoVigencia.intersecta` (equivalencia verificada
término a término); no quedan dos implementaciones que puedan divergir. Los tests de 1C siguen
protegiendo la semántica sin cambios.

## 7. SalonHorarioExcepcion

**SIN CAMBIOS.** No se modificó su modelo, no se convirtieron excepciones puntuales en versiones
de `HorarioOperacion`, y las excepciones puntuales **no** se usan para invalidar bloques
recurrentes al crearlos: el bloque recurrente sólo mira `HorarioOperacion` versionado.

## 8. Dependencias entre paquetes

Dirección preservada: `calendario → ubicaciones`, `programacion → ubicaciones`.
No hay `ubicaciones → calendario` ni `ubicaciones → programacion`. `HorarioEfectivoSalon` no
conoce `Turno` ni `Bloque`; `CoberturaVigencia` es dominio neutral.

## 9. Compatibilidad con el frontend real

Inventario confirmado en `Feelingpilates/web`: `SalonRequest.horarios` es obligatorio en
`src/api/types.ts`, y tanto `DialogoSalon.tsx` como `EditarHorarioSemanalDialog.tsx` **siempre**
envían `horarios` en el PUT del salón — incluso al cambiar sólo el teléfono. Por eso no se puede
rechazar todo request con `horarios != null`: rompería hasta la actualización de teléfono.

La cuarentena backend permite el payload idéntico como no-op, así que **el frontend actual sigue
funcionando sin cambios** para todo lo que no sea editar horarios.

**Limitación declarada, no oculta:** la edición real de horarios desde la UI legacy
(`EditarHorarioSemanalDialog`) **queda bloqueada** — devolverá `HORARIOS_REQUIEREN_VERSIONADO` —
hasta que exista el endpoint de versionado y la migración del frontend correspondiente.

`Feelingpilates/web` **no se modificó** en esta fase.

## 10. Contrato HTTP

**SIN CAMBIOS.** No se tocaron DTOs de Salón ni de Horario, ni los controllers.
`SalonDetalleResponse` sigue sin exponer `vigenteDesde`, `vigenteHasta` ni historial; hay un test
por reflexión que fija los componentes de `HorarioOperacionResponse` en exactamente
`(id, diaSemana, horaApertura, horaCierre)`.

## 11. Alcance NO implementado (reservado a F2B.3)

Deliberadamente fuera de esta fase:

- `VersionarHorarioOperacion` / `CerrarHorarioOperacion` (writers) — F2B.3b
- validación en dirección inversa Horario → Bloques/Turnos (POLÍTICA A, diseñada y cerrada) — F2B.3b
- `SELECT FOR UPDATE` sobre `Salon` — el protocolo compartido se activa cuando existan writers;
  F2B.2 sigue protegido físicamente por `UNIQUE(salon_id, dia_semana)`
- endpoints de historial/versiones, manejo de `23P01`, `btree_gist`, `EXCLUDE`
- retirar el UNIQUE
- migraciones nuevas

---

## 12. Verificación de mutaciones conceptuales

Cada mutación se aplicó **realmente** sobre el código productivo, se ejecutaron los tests
correspondientes y se revirtió. Estado = `DETECTADO` si la suite falló con la mutación aplicada.

| # | Mutación | Estado |
|---|---|---|
| 1 | `SalonService` vuelve a ejecutar `deleteBySalonId` en update | DETECTADO |
| 2 | `horarios == null` vuelve a borrar horarios | DETECTADO |
| 3 | payload idéntico se interpreta como mutación y falla | DETECTADO |
| 4 | payload de horario distinto se acepta silenciosamente | DETECTADO |
| 5 | `mapDetalle` devuelve una versión histórica/futura no vigente | DETECTADO |
| 6 | Turno recurrente vuelve a usar `anyMatch` | DETECTADO |
| 7 | Turno recurrente usa `allMatch` pero ignora gaps | DETECTADO |
| 8 | Turno recurrente acepta cobertura futura finita | DETECTADO |
| 9 | Bloque usa `anyMatch` | DETECTADO |
| 10 | Bloque usa `allMatch` pero ignora gaps | DETECTADO |
| 11 | Bloque abierto acepta horario cuya cobertura termina | DETECTADO |
| 12 | `HorarioEfectivo` ignora excepción CERRADO | DETECTADO |
| 13 | `HorarioEfectivo` ignora horario especial de la excepción | DETECTADO |
| 14 | `HorarioEfectivo` cae al semanal cuando no debe | DETECTADO |
| 15 | se reintroduce una conversión `diaSemana` distinta | DETECTADO |
| 16 | `RangoVigencia` deja de usarse y reaparece semántica de intersección divergente | DETECTADO |

**Los 16 quedan DETECTADO.**

### Hallazgo durante la verificación (mutación 15)

En la primera pasada, la mutación 15 quedó **NO DETECTADO**. Causa real: tras migrar la ruta por
fecha a `HorarioEfectivoSalon` —que deriva el día de la semana por su cuenta vía
`HorarioOperacionResolver`— el `diaSemana` calculado en `crear` ya no alimenta la validación de
horario, sino **sólo la detección de traslapes** (`buscarRecurrentesPorSalonYDia`). El test de
domingo existente pasaba con ambas convenciones porque en él ese lookup devolvía vacío en los dos
casos.

El riesgo es real y no cosmético: con la convención ISO (domingo = 7) una EXCEPCION dominical
consultaría un día inexistente, no encontraría el recurrente que sí ocupa el salón y aceptaría un
traslape. Se añadió
`excepcionEnDomingoDetectaTraslapeContraElRecurrenteDeDiaSemanaCero`, que ejercita justamente esa
ruta; con la mutación aplicada, falla. Ningún test cambió su expectativa.

---

## 13. Verificaciones finales

| Verificación | Resultado |
|---|---|
| `./mvnw test` | **240/240 PASS** (baseline 169/169; +71 tests, ninguno modificado en su expectativa) |
| `./mvnw clean compile` | **BUILD SUCCESS** |
| Flyway | **V1 → V43 PASS** (46 migraciones aplicadas, schema en v43) |
| JPA `validate` | **PASS** (sin `SchemaManagementException`) |
| `UNIQUE(salon_id, dia_semana)` | **CONSERVADO** (V11 intacto; no se deshabilita en tests) |
| `git diff -- src/main/resources/db/migration` | **vacío** |
| `git diff -- .../pagos`, reservas productivo, ventas | **vacío** |
| `Feelingpilates/web` | **sin cambios** |
| Writers de versionado | **no implementados** |
| Escenarios multiversión | sólo por mock/unit test; **ninguna fila duplicada real en PostgreSQL** |

### Desglose de tests nuevos

| Suite | Antes | Después |
|---|---|---|
| `CoberturaVigenciaTest` | — | 18 |
| `HorarioEfectivoSalonTest` | — | 10 |
| `SalonServiceTest` | — | 18 |
| `TurnoInstructorServiceHorarioVersionadoTest` | — | 14 |
| `TurnoInstructorServiceCaracterizacionTest` | 33 | 34 |
| `BloqueProgramacionServiceTest` | 37 | 47 |

---

## 14. Estado de cierre

| Concepto | Estado |
|---|---|
| Base | `56504b145165c324e58b26974471fd62bfde7fc1` |
| Clock | **IMPLEMENTADO** (no existía ninguno; `systemDefaultZone`, sin inventar zona) |
| Cobertura temporal | **IMPLEMENTADA** (`CoberturaVigencia`, dominio neutral) |
| `HorarioEfectivoSalon` | **IMPLEMENTADO** |
| `SalonService` | **MIGRADO** |
| Update destructivo | **ELIMINADO** |
| Compatibilidad frontend payload idéntico | **PROBADA** |
| Cambio horario legacy | **RECHAZADO** (`HORARIOS_REQUIEREN_VERSIONADO`) |
| Turno RECURRENTE | **MIGRADO** |
| Turno EXCEPCION | **MIGRADO** |
| `BloqueProgramacion` | **MIGRADO** |
| `UNIQUE(salon, dia)` | **CONSERVADO** |
| Writers versionados | **NO IMPLEMENTADOS** |
| Migraciones | **SIN CAMBIOS** |
| Frontend | **SIN CAMBIOS** |
| Tests | **240/240 PASS** |
| Build | **PASS** |
| Flyway | **V1→V43 PASS** |
| JPA | **PASS** |
| F2B.3a | **LISTA** |

Ninguna stop condition se activó: no hizo falta volver a delete+insert, ni inferir
`efectivoDesde`, ni quitar el UNIQUE, ni schema nuevo, ni writers, ni modificar
`SalonHorarioExcepcion`, ni inventar zona horaria. El payload idéntico y el cambio real de
horarios se distinguen limpiamente por comparación semántica. No aparecieron ciclos
`ubicaciones → calendario/programacion`.
