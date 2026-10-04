# FeelingPilates — Fase 2B: Diseño de versionado de Horario Operativo

Documento de DISEÑO. No implementa código, migraciones ni tests.

Consolidado en F2B.0.1 tras revisión independiente (veredicto: REQUIERE AJUSTE F2B.0.1).
Los cambios de esta pasada cierran: dirección inversa (Política A), lock padre `Salon` +
protocolo compartido, política P0 de `SalonService` (sin inferir `efectivoDesde`), cobertura
completa de `TurnoInstructor` RECURRENTE, renumeración fija de Flyway V44–V46, y la corrección
del requisito de `btree_gist`. Ver §23 (Decisiones cerradas) y §24 (Decisiones abiertas)
para el detalle sección por sección.

Pre-flight verificado: branch `operacion/horario-vigencia-politicas-safety-net`, HEAD
`33aaaeec21b994ac15dfd5ddbdb69519d96f87cb`, working tree limpio. Baseline previo:
131/131 tests PASS, Flyway V1→V43 PASS.

Las afirmaciones sobre PostgreSQL de §5 y §10 fueron **verificadas empíricamente** contra
`postgres:16-alpine` (16.14), la misma imagen de `TestcontainersConfiguration`; se citan
como EVIDENCIA. El contenedor era efímero y fue eliminado.

## 1. Executive Summary

Hoy `HorarioOperacion` es configuración: una fila por `(salon, dia_semana)`, garantizada
por `UNIQUE (salon_id, dia_semana)`. V43 añadió `vigente_desde`/`vigente_hasta` de forma
aditiva, pero **ningún consumidor los lee**.

El riesgo dominante no es el esquema. Es que tres consumidores productivos **degradan su
semántica en silencio** en cuanto exista una segunda fila por día:

- `TurnoInstructorService.validarDentroDeHorarioSalon` y
  `BloqueProgramacionService.validarDentroDelHorarioOperacion` usan
  `filter(diaSemana).anyMatch(cabe)`. Bajo el UNIQUE eso recorre ≤1 fila y significa "cabe
  en el horario del día". Con N versiones pasa a significar "cabe en ALGUNA versión
  histórica o futura": estrictamente más permisivo. **Falla abriendo**, sin excepción y
  sin test rojo.
- `SalonService.reemplazarHorarios` hace `deleteBySalonId` + reinsert en **cada**
  actualización de salón, aunque sólo cambie el teléfono. Bajo versionado destruiría el
  historial completo de todos los días y las versiones futuras planificadas. Ningún
  constraint lo impide: un DELETE nunca viola un EXCLUDE.

De ahí el orden: resolver temporal y consumidores deterministas primero (F2B.1 y F2B.2,
ambas **bajo el UNIQUE vigente**), y sólo al final `btree_gist` + EXCLUDE + DROP UNIQUE.

Estado del diseño: **CONSOLIDADO** (F2B.0.1). F2B.1: **REALMENTE LISTA**, sin blockers
arquitectónicos (§19, §24).

## 2. Estado actual

`horario_operacion` (V11 + V43): `id`, `salon_id`, `dia_semana SMALLINT CHECK 0..6`
(0 = domingo .. 6 = sábado), `hora_apertura`, `hora_cierre`, `vigente_desde DATE NULL`,
`vigente_hasta DATE NULL`.

Nombres reales de constraints — V11 los declara inline y sin nombre, así que PostgreSQL
los autonombra. Verificados recreando el DDL en PG16, no inferidos:

| Constraint | Nombre real |
|---|---|
| PK | `horario_operacion_pkey` |
| UNIQUE(salon_id, dia_semana) | `horario_operacion_salon_id_dia_semana_key` |
| CHECK (hora_cierre > hora_apertura) | `horario_operacion_check` |
| CHECK (dia_semana BETWEEN 0 AND 6) | `horario_operacion_dia_semana_check` |
| CHECK vigencia (V43) | `chk_horario_operacion_vigencia` |

`horario_operacion_check` ya prohíbe en BD `apertura == cierre` y `apertura > cierre`: los
edge cases G y H de §9 están cerrados por el esquema existente.

La entidad ya mapea ambas vigencias como `LocalDate` nullable. El repositorio expone sólo
`findBySalonIdOrderByDiaSemana` y `deleteBySalonId`: **no existe ninguna query por fecha ni
por día**; los tres consumidores traen todos los días y filtran en memoria.

| Consumidor | Consulta | Por diaSemana | Fecha concreta | Rango vigencia | Supone del UNIQUE | Cambio previo a retirarlo |
|---|---|---|---|---|---|---|
| `SalonService.mapDetalle` | `findBySalonIdOrderByDiaSemana` | sí (mapea todo) | no | no | ≤1 fila/día ⇒ DTO "un horario por día" | Filtrar a la versión vigente hoy (§11) |
| `SalonService.reemplazarHorarios` | `deleteBySalonId` + save | sí | no | no | Borrar+reinsertar ≡ editar configuración | **Crítico**: dejar de ser destructivo (§11) |
| `TurnoInstructorService.validarDentroDeHorarioSalon` | `findBySalonIdOrderByDiaSemana` | sí, `filter` | sólo EXCEPCION; RECURRENTE = `null` | no | `anyMatch` sobre ≤1 fila ≡ "la fila del día" | Contra qué versiones valida una recurrencia sin vigencia (§12) |
| `BloqueProgramacionService.validarDentroDelHorarioOperacion` | `findBySalonIdOrderByDiaSemana` | sí, `filter` | no | **sí** | ídem | `allMatch` + cobertura (§13, §14) |

Ningún controller toca `HorarioOperacionRepository` directamente: el acceso HTTP pasa
siempre por `SalonService`. `Asignacion` **no** consulta horarios (§16).

**Precedente ya en el código**: `BloqueProgramacionService.vigenciasSeIntersectan` ya
implementa intervalo cerrado con NULL ilimitado. La semántica de §3 **no es nueva en el
proyecto**: es la que ya rige `BloqueProgramacion` y `Asignacion`. F2B la reutiliza, no
define una segunda.

## 3. Semántica temporal

`[vigenteDesde, vigenteHasta]` es un intervalo de fechas **cerrado en ambos extremos**.
`vigenteDesde = NULL` ⇒ −∞; `vigenteHasta = NULL` ⇒ +∞.
Contención: `(desde == null || !f.isBefore(desde)) && (hasta == null || !f.isAfter(hasta))`.

Confirmado: A hasta `2026-08-31` + B desde `2026-09-01` ⇒ **contiguos, sin overlap**;
A hasta `2026-09-01` + B desde `2026-09-01` ⇒ **overlap el día 1**.

VO conceptual, sólo `java.time`, sin librería externa: `RangoVigencia(desde, hasta)` con
`contiene(fecha)`, `intersecta(otro)`, `esContiguoCon(otro)` y orden por `desde` NULLS
FIRST. Debe ser el **único** lugar con aritmética de fronteras: `vigenciasSeIntersectan` se
migra a él sin cambio observable, para no tener dos implementaciones divergentes.

`+∞`/`−∞` (`hasta`/`desde` `NULL`) son **estado lógico, no fecha real**: `esContiguoCon` sólo
hace aritmética (`plusDays(1)`/`minusDays(1)`) cuando **ambos** límites relevantes son
finitos. Si `a.hasta == null`, `a` es abierto hacia +∞ y por definición **no puede existir**
una versión posterior no solapada — `esContiguoCon` devuelve `false` sin evaluar
`plusDays(1)` sobre un sentinel. Nunca se representa infinito con `LocalDate.MIN`/`MAX`: el
algoritmo de cobertura (§14) trata `+∞` como caso aparte, no como valor sobre el que operar.

## 4. Legacy NULL/NULL

La estrategia propuesta se **confirma sin cambios**. `NULL/NULL` = `(−∞, +∞)`: aplica a
cualquier fecha, que es exactamente lo que hace hoy el código al ignorar las vigencias.
Al versionar desde `D`: legacy pasa a `NULL / D−1`; nueva queda `D / NULL`.

- **Historia**: no inventa una fecha de inicio que nunca existió. La legacy afirma "no sé
  desde cuándo, pero hasta D−1 fue así": la única afirmación verdadera disponible.
- **Queries**: NULL inferior es −∞, así que la legacy resuelve cualquier fecha pasada,
  incluso anterior a la creación del salón. Deseable: un salón nunca queda sin horario
  hacia atrás.
- **Constraints**: `chk_horario_operacion_vigencia` acepta `NULL / D−1`; el EXCLUDE de §5
  también (EVIDENCIA: cerrar la legacy en `2026-08-31` e insertar `[2026-09-01, ∞)`
  conviven sin violación).
- **JPA**: `LocalDate` nullable ya mapeado, sin cambios. **Flyway**: sin backfill, es el
  estado que V43 dejó. **UI**: `HorarioOperacionResponse` no expone vigencias, así que no
  ve diferencia si `SalonService` devuelve la versión vigente hoy (§11).

**Problema real detectado** (no invalida la estrategia, pero bloquea F2B.2): la fila legacy
es indistinguible por predicado de "versión actual". `vigente_desde IS NULL` **no**
identifica a la versión vigente — sigue siendo NULL tras cerrarla. Un writer concurrente
que use ese predicado re-selecciona la fila ya cerrada y la vuelve a modificar (EVIDENCIA
en §10). La versión vigente se localiza **por contención de fecha**, con el predicado
canónico que contiene una fecha `D`:

```
(vigente_desde IS NULL OR vigente_desde <= :D)
AND
(vigente_hasta IS NULL OR vigente_hasta >= :D)
```

Dos derivaciones de ese mismo predicado, usadas en §10/§12/§13: una versión está **abierta
hacia el futuro** cuando `vigente_hasta IS NULL`; está **abierta y ya vigente en `D`** cuando
además `(vigente_desde IS NULL OR vigente_desde <= :D)`. Ninguna variante sustituye a la
otra: "abierta" no implica "vigente en `D`" si `vigente_desde` es posterior a `D`.

No se sustituye por fecha sentinel: no hay evidencia que lo justifique y destruiría la
distinción entre "desconocido" y "conocido e igual a X".

## 5. Constraint objetivo

```
CREATE EXTENSION IF NOT EXISTS btree_gist;

ALTER TABLE horario_operacion
  ADD CONSTRAINT ex_horario_operacion_vigencia
  EXCLUDE USING gist (
      salon_id   WITH =,
      dia_semana WITH =,
      daterange(vigente_desde, vigente_hasta, '[]') WITH &&
  );
```

EVIDENCIA (PostgreSQL 16.14):

1. **La expresión se acepta directamente: NO hace falta generated column.**
   `daterange(date,date,text)` es IMMUTABLE y sirve como columna de índice del EXCLUDE.
2. **UUID + SMALLINT con `=` requieren `btree_gist`**: sin la extensión, GiST no tiene
   clase de operadores para `uuid`/`int2`. Prerrequisito duro, no un adorno.
3. **NULL/unbounded correcto**: `daterange(NULL,NULL,'[]')` → `(,)` universal. Dos filas
   legacy del mismo `(salon,dia)` ⇒ rechazo. Legacy + nueva sin cerrar la legacy ⇒ rechazo.
4. **Canonicalización**: `daterange(NULL,'2026-08-31','[]')` → `(,2026-09-01)`, superior
   exclusivo +1 día. Es interna del tipo discreto; **las columnas conservan su valor**
   (`vigente_hasta` se relee `2026-08-31`), así que el round-trip JPA no se ve afectado.
5. **Fronteras**: legacy hasta `08-31` + nueva desde `08-31` ⇒ rechazo; desde `09-01` ⇒
   acepta. Coincide exactamente con §3.
6. Otro `dia_semana` u otro `salon_id` no interfieren.
7. **Dispara también en UPDATE**, no sólo en INSERT (EVIDENCIA §10).

Alternativas descartadas: **generated column** `daterange STORED` (funciona, pero obliga a
`insertable=false/updatable=false` en JPA y duplica la fuente de verdad — innecesario dado
1); **trigger** (no atómico frente a concurrencia sin lock adicional; es el patrón que
produce los bugs que EXCLUDE elimina, y el encargo lo prohíbe si EXCLUDE resuelve la
invariante, **y la resuelve**); **UNIQUE parcial** `WHERE vigente_hasta IS NULL` (no
equivalente: no impide solapes entre versiones cerradas; sirve como refuerzo posterior, no
como sustituto).

## 6. HorarioOperacionResolver

Componente nuevo en `ubicaciones`.
`Optional<HorarioOperacion> resolver(UUID salonId, LocalDate fecha)`: calcula el
`diaSemana` de la fecha (la conversión ya existe en `TurnoInstructorService.diaSemanaIso`
y debe extraerse a utilidad compartida, no duplicarse), consulta `findVigente` (§16) y
devuelve 0 o 1.

`> 1` es **imposible por construcción**: antes de F2B.3 lo garantiza el UNIQUE, después el
EXCLUDE; nunca hay ventana sin garantía (§17). Aun así el resolver debe **fallar
ruidosamente** (`IllegalStateException`) si la BD devuelve más de una fila: ese assert es
la red que evita repetir el bug de `anyMatch` a otro nivel.

Con 0 resultados **no se inventa horario**: resolver ⇒ `Optional.empty()`; servicio que
necesita horario para operar ⇒ `ValidacionException` con código estable
`SALON_SIN_HORARIO_OPERATIVO`. Nunca "abierto 00:00–23:59" ni "cerrado" implícito: son
decisiones distintas y la ausencia de dato no es ninguna de las dos.

## 7. HorarioEfectivoSalon

`HorarioOperacion` versionado y `SalonHorarioExcepcion` son cosas distintas y no se
mezclan. `SalonHorarioExcepcion` **no se mueve** de `ubicaciones`.

```
resolver(salonId, fecha):
  excepcion = findBySalonIdAndFechaAndActivoTrue(salonId, fecha)
  si presente: cerrado -> CERRADO ; si no -> ABIERTO(exc.apertura, exc.cierre)
  si no: semanal = horarioOperacionResolver.resolver(salonId, fecha)
         presente -> ABIERTO(semanal.apertura, semanal.cierre) ; vacio -> NO_OPERATIVO
```

Resultado como tipo cerrado, no `Optional` anónimo: `Abierto(apertura, cierre)`, `Cerrado`
(el salón opera pero **ese día** está cerrado por excepción explícita) y `NoOperativo` (no
hay horario semanal ni excepción). La distinción importa: la primera es dato de negocio, la
segunda un hueco de configuración que probablemente deba reportarse al administrador.
Colapsarlas oculta datos faltantes.

Ownership: Operación (`ubicaciones`); `calendario` y `programacion` consumen.
`TurnoInstructorService.validarDentroDeHorarioSalon` ya implementa esta composición a mano
(excepción primero; si `cerrado` falla; si hay horario especial valida contra él y retorna;
si no, cae al patrón semanal). El diseño no inventa la regla: **la extrae** y la hace
reutilizable.

## 8. VersionarHorarioOperacion / CerrarHorarioOperacion

Comandos de aplicación transaccionales en `ubicaciones`. Ambos participan del protocolo de
lock de §10 (fila `Salon` primero) y de la validación de dirección inversa de esta sección
— **decisión cerrada**, no abierta (§23, §24).

### Dirección inversa — Política A

`VersionarHorarioOperacion` y `CerrarHorarioOperacion` **rechazan** el cambio si el nuevo
estado operativo vuelve incompatible programación recurrente activa que aplica en cualquier
fecha desde `efectivoDesde` hacia adelante. Aplica a:

- `TurnoInstructor` RECURRENTE — como no tiene vigencia propia (§12), **todo recurrente
  activo se considera aplicable hacia futuro mientras siga activo**; cualquier turno
  recurrente activo del salón/día es candidato a conflicto.
- `BloqueProgramacion` — cuya vigencia sí es explícita; conflicto si su vigencia intersecta
  `[efectivoDesde, +∞)` y deja de estar contenido en el horario resultante.

**No degrada silenciosamente programación previamente aceptada.** Es la misma filosofía que
cierra el bug de §1 (Executive Summary) — "falla cerrando" — aplicada a la dirección
contraria: no sólo un Bloque/Turno nuevo debe caber en el horario, un horario nuevo tampoco
puede dejar fuera a un Bloque/Turno ya aceptado.

`SalonHorarioExcepcion` **no** sigue esta política: es una excepción puntual de una fecha
concreta, no modifica la plantilla, no cierra `HorarioOperacion` ni `TurnoInstructor`
recurrente, y es el resolver de esa fecha (`HorarioEfectivoSalon`, §7) el que decide
publicabilidad ese día. Versionar u cerrar el horario semanal **no** consulta
`SalonHorarioExcepcion` ni al revés (§15).

### VersionarHorarioOperacion

Entrada: `salonId, diaSemana, efectivoDesde, horaApertura, horaCierre`.

1. Lock de fila `Salon` (§10) — precondición de todo el comando, no sólo de la lectura de
   `horario_operacion`.
2. `SELECT ... FOR UPDATE` de las versiones de `(salonId, diaSemana)`.
3. Validar `horaCierre > horaApertura` (ya lo exige `horario_operacion_check`, pero se
   valida en aplicación para dar mensaje de dominio, no un 500).
4. Localizar la versión que contiene `efectivoDesde` y resolver el edge case aplicable
   (§9).
5. **Validación inversa (Política A)**: consultar Bloques del mismo salón/día cuya vigencia
   intersecte `[efectivoDesde, +∞)` y Turnos recurrentes activos del mismo salón/día. Si el
   horario resultante deja alguno fuera de contención → **RECHAZAR**, error de dominio que
   identifica los Bloques/Turnos afectados. No se persiste nada de los pasos 6–7.
6. Cerrarla: `vigenteHasta = efectivoDesde.minusDays(1)`. No se toca su `vigenteDesde` (si
   era NULL, sigue NULL — §4).
7. Insertar la nueva: `vigenteDesde = efectivoDesde`, `vigenteHasta = NULL`.
8. Todo en **una** transacción.

### CerrarHorarioOperacion

Entrada: `salonId, diaSemana, efectivoDesde` (`D`) — deja de operar ese día desde `D`, sin
insertar sucesora (§9-I).

1. Lock de fila `Salon`, luego `SELECT ... FOR UPDATE` de las versiones del día (§10).
2. Localizar la versión que contiene `D`.
   - Si ninguna versión contiene `D` → **RECHAZAR** con error de dominio explícito; nunca
     no-op silencioso (§11 edge cases de borde).
   - Si `D` coincide exactamente con el `vigenteDesde` de la versión localizada → **no**
     producir `vigenteHasta = D−1` sobre esa misma fila (equivaldría a cancelar la versión
     completa, operación distinta). **RECHAZAR** y tratarlo como operación futura de
     cancelación de versión planificada, no como cierre (§11).
3. **Validación inversa (Política A)**: cualquier Bloque activo cuya vigencia alcance el
   periodo que quedaría sin horario desde `D` → **RECHAZAR**, identificando los Bloques
   afectados. Cualquier Turno recurrente activo del mismo salón/día → conflicto,
   **RECHAZAR** (no tiene vigencia propia: se asume aplicable hacia futuro mientras esté
   activo). No se da vigencia nueva a `TurnoInstructor`: sigue siendo compatibility surface
   (§12).
4. Cerrar: `vigenteHasta = D.minusDays(1)`.
5. Todo en **una** transacción.

Invariantes de ambos comandos: **no reescriben el pasado** (cerrar en `D−1` no altera lo que
esa versión afirmaba sobre fechas anteriores) y **no borran filas** (el historial es
acumulativo — lo que choca frontalmente con `reemplazarHorarios`, §11). Ningún estado
"INVALIDO" ni degradación silenciosa de Bloques/Turnos: el conflicto se resuelve rechazando
el comando que lo introduce, nunca marcando la programación existente como inválida después
del hecho.

## 9. Edge cases

**F2B** = permitido en la primera implementación. **DIFERIDO** = rechazado con error de
dominio explícito, se decide después.

| # | Caso | Decisión | Justificación |
|---|---|---|---|
| A | No existe horario previo (alta inicial) | **F2B**: insertar sola la nueva versión `D → NULL`, sin cierre previo | Caso de alta. Deja el día sin cobertura antes de `efectivoDesde`, lo cual es legítimo: el salón no operaba ese día |
| B | Versión actual legacy NULL/NULL contiene `D`, sin versiones futuras (append normal) | **F2B**: cerrar `actual → D−1` dejando `vigenteDesde = NULL`, insertar `D → NULL` | §4, verificado en PG16. Caso general de "append": la misma mecánica aplica si la versión que contiene `D` no es la legacy, siempre que no existan versiones posteriores |
| J | Reapertura tras cierre: `D` es **posterior** a toda versión existente y **no** existe ninguna versión futura | **F2B**: insertar `D → NULL` **sin modificar** historia anterior | Puede existir un gap histórico entre el cierre anterior y `D`: representa un periodo donde el salón no operó ese día. No es lo mismo que el caso A (sí hay historia, sólo no hay versión vigente en `D`) ni que D (sí hay versión futura) |
| D | Gap con historia futura: `D` cae en un **gap** (no hay versión que lo contenga) **y existe** una versión posterior | **DIFERIDO**: `VERSIONADO_INTERMEDIO_NO_SOPORTADO` | Insertar `D → NULL` solaparía el futuro; acotarla a `[D, futura.desde)` sería una inserción intermedia (E). Se distingue de J únicamente por la existencia de versión futura |
| C | `efectivoDesde` == `vigenteDesde` de una versión existente | **DIFERIDO**: `YA_EXISTE_VERSION_EN_ESA_FECHA` | Cerrarla daría `vigenteHasta < vigenteDesde`, violando `chk_horario_operacion_vigencia`. Lo correcto es *corregir* esa versión (UPDATE in-place), semánticamente distinto de versionar |
| E | Inserción en medio de una versión existente que sigue conteniendo fechas después de `D` (partir una versión en dos) | **DIFERIDO**: `VERSIONADO_INTERMEDIO_NO_SOPORTADO` | Exige partir una versión en dos y decidir qué pasa con Bloques ya validados contra ella |
| F | `efectivoDesde` en el pasado | **DIFERIDO**: `EFECTIVO_DESDE_EN_EL_PASADO`. `== hoy` **sí** se permite | Reescribe historia ya usada para validar Bloques y Turnos; podría invalidar retroactivamente programación publicada |
| G | `apertura == cierre` | Rechazar: `HORA_CIERRE_DEBE_SER_POSTERIOR` | Ya lo prohíbe `horario_operacion_check`; en aplicación para no exponer un 500. Consistente con la validación ya presente en `reemplazarHorarios` |
| H | `apertura > cierre` | Rechazar, mismo error | Ídem. No se modela horario cruzando medianoche: sería cambio de modelo, no de versionado |
| I | Dejar de operar ese día de forma recurrente | **F2B**: cerrar la versión vigente en `D−1` **sin insertar** nueva. Comando `CerrarHorarioOperacion(salonId, diaSemana, efectivoDesde)`, con validación inversa previa (Política A, §8) | La ausencia de versión vigente ya significa "no opera". Evita añadir un flag `cerrado` a `horario_operacion`, que duplicaría el rol de `SalonHorarioExcepcion.cerrado`. Detalle completo, incluidos los casos de borde de cierre, en §8 y §11 |

**Alcance de la primera implementación**: sólo append hacia el futuro (A, B, J) y cierre
(I); C, D, E, F se rechazan explícitamente. Esto reduce drásticamente el riesgo: **ningún
camino de código reescribe el pasado**, así que ningún Bloque o Turno ya validado puede
quedar retroactivamente inválido por una operación de versionado hacia adelante — el único
mecanismo que puede invalidar programación existente es el rechazo explícito de la
Política A (§8), nunca una degradación silenciosa.

## 10. Concurrencia

Verificado en PG16, READ COMMITTED, con EXCLUDE instalado y UNIQUE ya retirado (estado
post-F2B.3), dos sesiones simultáneas sobre el mismo `(salon, dia)`:

- **A** — ambos versionan desde `2026-09-01`: A commitea; B se bloquea en el INSERT y falla
  con `SQLSTATE 23P01`. Estado final: 2 filas, sin solape.
- **B** — A desde `2026-09-01`, B desde `2026-10-01`: el `UPDATE` de B (con predicado
  `vigente_desde IS NULL`) se reevaluó tras el commit de A, **volvió a matchear la fila
  legacy ya cerrada** e intentó reabrirla hasta `2026-09-30`, lo que habría solapado con la
  versión de A. **El EXCLUDE disparó en el propio UPDATE** y abortó B. Estado final: 2
  filas, sin solape.

Conclusiones directas: el EXCLUDE es un backstop real **también en UPDATE** (lo que un
trigger no garantizaría sin lock adicional), y **`vigente_desde IS NULL` es un predicado
inseguro** para localizar la versión vigente (§4).

**Corrección de diseño respecto al borrador previo**: `FOR UPDATE` sobre `horario_operacion`
por sí solo **no** es la estrategia de lock — si todavía no existen versiones para
`(salonId, diaSemana)` (§9-A), el `SELECT ... FOR UPDATE` recorre cero filas y no bloquea
nada; dos altas iniciales concurrentes sólo quedan protegidas por el EXCLUDE como backstop
final, sin serialización previa que produzca un error de dominio legible.

Writer objetivo: **lock de la fila `Salon` primero**, porque esa fila **siempre existe** —
incluso cuando `horario_operacion` está vacío para ese día:

```
1. Lock fila Salon (SELECT ... FOR UPDATE sobre salon WHERE id = :salonId)
2. Lock/lectura de las versiones de (salonId, diaSemana) (SELECT ... FOR UPDATE)
3. Validar (horas, edge case de §9)
4. Validar programación dependiente (Política A, §8)
5. Close/insert
6. Commit
```

Orden global obligatorio, el mismo para todo comando que toque cualquiera de los dos lados
de la invariante (§8, y ver protocolo compartido más abajo):

```
SALON → HORARIO_OPERACION → validaciones/lecturas dependientes → persistencia
```

Serializa a los writers del mismo salón (no sólo del mismo día) y hace que un segundo writer
concurrente lea el estado ya versionado por el primero y falle con error de dominio
comprensible (§9-C/D/E, según el caso) en vez de depender únicamente de un 23P01. El
EXCLUDE **sigue siendo
backstop**: si por cualquier razón dos transacciones evitan el lock de `Salon` (código
futuro, ruta no revisada), la invariante de no-solape la garantiza igualmente la base de
datos. Todo `23P01` sobre `ex_horario_operacion_vigencia` se traduce a
`CONFLICTO_VIGENCIA_HORARIO` (409) y nunca escapa como 500.

Reparto: **aplicación explica el error; BD garantiza la invariante.** Sin locks distribuidos
ni advisory locks: el `FOR UPDATE` sobre `Salon` vive en la BD y sirve también con varias
instancias.

### Concurrencia inversa — protocolo compartido (diseño para F2B.3b, no implementar en F2B.1)

Invariante nueva que el lock de fila `Salon` debe cubrir, no sólo el propio
`horario_operacion`. Existe una carrera si:

- **TX A**: `VersionarHorarioOperacion` valida (Política A, §8) que no hay Bloques ni
  Turnos incompatibles con el horario nuevo.
- **TX B** (simultánea): crea un `BloqueProgramacion` (o `TurnoInstructor` RECURRENTE) bajo
  el horario **viejo**, que sería incompatible con el horario que A está a punto de dejar
  vigente.

Si A y B validan cada una contra el estado que veían al empezar y ambas comprometen, el
resultado es: horario nuevo incompatible + Bloque/Turno nuevo — exactamente la invariante
que la Política A pretende impedir, colada por la ventana entre lectura y commit.

Por tanto, cuando los writers versionados se habiliten en F2B.3b, **todos** los comandos que
modifican cualquiera de los dos lados de la invariante deben participar del mismo lock de
`Salon` antes de validar y persistir, como mínimo:

- `VersionarHorarioOperacion`
- `CerrarHorarioOperacion`
- crear/actualizar `BloqueProgramacion`
- crear/actualizar `TurnoInstructor` RECURRENTE

`Asignacion` no necesita duplicar este lock por horario: ya está contenida en la validación
de `Bloque` (§16), y el lock de `Salon` que adquiere el writer de `Bloque` cubre la cadena.
**No se implementa en F2B.1 ni en F2B.2** — se documenta aquí como prerrequisito de diseño
de F2B.3b (§22, §24).

## 11. SalonService — P0 cerrado

**Se elimina por completo la propuesta previa de inferir `efectivoDesde = hoy` en
silencio desde el PUT legacy.** El contrato actual de `SalonRequest` no contiene
`efectivoDesde` y el backend no debe inventarlo: traducir cada entrada de `horarios()` a un
versionado con fecha implícita repite exactamente el patrón que el resto de este documento
prohíbe — una mutación con efecto de dominio (abrir una versión nueva) disparada por un
campo que el cliente nunca pidió versionar. La política objetivo queda explícita más abajo.

**Lectura (`mapDetalle`)** — devuelve `List<HorarioOperacionResponse>` sin vigencias, ni
tampoco expone historial: sigue mostrando una versión por día, **la vigente en la fecha de
negocio**. ¿Horario efectivo hoy o configuración base? Derivado de la semántica real del
endpoint, no de conveniencia: hoy la lista contiene, por día, la única fila existente, que es
a la vez "la configuración" y "lo que rige hoy" porque no hay versiones; la UI de detalle la
lee como **"a qué hora abre este salón"**, en presente. La continuación fiel es devolver la
**versión vigente en la fecha de negocio** (`resolver(salonId, fechaNegocio)` por día); los
días sin versión vigente no aparecen, exactamente como hoy no aparece un día sin fila. Con
datos legacy el resultado es **idéntico al actual**. No debe mezclarse historial en
`SalonDetalleResponse`: el DTO no tiene lugar para vigencias y añadirlas cambiaría el
contrato sin que ningún consumidor lo pida.

Fecha de negocio = `LocalDate.now(clock)` con `Clock` inyectable cuando se cablee en F2B.2 —
**no** `LocalDate.now()` disperso por el código. La zona debe provenir de configuración/Clock
centralizado; si el proyecto todavía no tiene una zona de negocio explícita, eso queda como
dependencia real de F2B.2 (§24), no se hardcodea aquí.

**Creación de salón** — puede seguir insertando su configuración inicial de horarios
directamente (caso §9-A, alta): no existe historia previa que preservar, así que no hay
versionado que respetar ni conflicto de dirección inversa posible.

**Actualización de salón** — separar claramente dos casos según `horarios()`:

- `horarios == null` → **no tocar** horarios. Actualizar teléfono/nombre/etc. no puede
  borrar horarios. Corrige el bug latente actual: hoy el `if (horarios == null) return;`
  vive **después** del `deleteBySalonId`, así que `null` significa "borra todos".
- `horarios != null` (incluida **lista vacía**) → mutación explícita de horarios. **No** se
  convierte silenciosamente en versionado con "hoy". Lista vacía tampoco significa "borra
  toda la historia": es una mutación explícita igual que cualquier otra, y debe tratarse con
  la misma política.

**Política objetivo del backend**, una vez exista el endpoint explícito de versionado
(F2B.3b): actualización de salón con `horarios` presente → **rechazar** con error/código
estable equivalente a `HORARIOS_REQUIEREN_VERSIONADO`. El caso vacío no es una excepción a
esa regla.

**Inventario read-only del cliente web actual** (`Feelingpilates/web`, sin modificar
frontend): `horarios` es un campo **obligatorio, no opcional**, de `SalonRequest`
(`web/src/api/types.ts:283`, `horarios: HorarioOperacionRequest[]`, sin `?`).
`actualizarSalon(id, request)` (`web/src/api/salones.ts:22-24`) exige un `SalonRequest`
completo y hace `PUT /salones/{id}`. Los dos flujos de edición existentes —
`DialogoSalon.tsx` (wizard completo: info, actividades, horarios, equipamiento) y
`EditarHorarioSemanalDialog.tsx` (diálogo dedicado a horarios semanales) — reconstruyen
**siempre** el `SalonRequest` completo, incluido `horarios`, y llaman al mismo
`actualizarSalon`; no existe un endpoint ni un flujo separado que edite sólo datos básicos
sin tocar horarios. `EditarHorarioSemanalDialog.tsx` puede enviar `horarios: []` (todos los
días desactivados) sin bloqueo — lista vacía es un estado alcanzable hoy.

**Consecuencia — dependencia de despliegue registrada**: el cliente actual **siempre** envía
`horarios` en cada `PUT /salones/{id}`. Activar el rechazo `HORARIOS_REQUIEREN_VERSIONADO`
tal cual **rompería ambos flujos de edición existentes** del cliente web. **No se afirma
"compatibilidad observable"** para esta política mientras esa dependencia no esté resuelta:
el cliente debe migrarse (a un flujo que separe edición de datos básicos del versionado
explícito de horarios) **antes** de activar el rechazo en producción. Esto bloquea la
activación de la política, no su diseño ni F2B.1/F2B.2 (§24).

`deleteBySalonId` debe **desaparecer de toda actualización de salón antes de retirar el
UNIQUE** (F2B.3a). Puede permanecer exclusivamente donde tenga sentido para borrado real del
agregado `Salon`, nunca como estrategia para editar horarios.

**Endpoints administrativos (propuesta separada, no F2B.1)**:
`GET /salones/{id}/horarios/historial`, `POST /salones/{id}/horarios/versiones`,
`POST /salones/{id}/horarios/cierres`. Se listan para que la decisión de no contaminar el
DTO actual tenga salida clara.

## 12. TurnoInstructorService legacy

`TurnoInstructor` RECURRENTE tiene `diaSemana`, `fecha = null` y **no tiene vigencia**: es
una regla que aplica a todos los lunes indefinidamente. Se valida con
`filter(diaSemana).anyMatch(cabe)`. Sin el UNIQUE eso pasa a significar "cabe en al menos
una versión": un turno 08:00–09:00 se aceptaría porque en 2024 el salón abría a las 08:00,
aunque desde el mes que viene abra a las 10:00. La sustitución ingenua `resolver(hoy)` es
igualmente incorrecta en dirección opuesta: valida una recurrencia infinita contra un único
punto, aceptando turnos que serán inválidos desde la próxima versión.

| Opción | Semántica | Veredicto |
|---|---|---|
| `anyMatch` sobre todas las versiones | "cabe en alguna versión" | **No.** Falla abriendo; es el bug |
| `resolver(hoy)` | "cabe hoy" | **No.** Valida regla infinita contra un punto |
| `allMatch` sobre versiones desde hoy, **sin verificar cobertura** | "cabe en todas las versiones que la query devolvió" | **No, insuficiente por sí solo.** Ver corrección abajo |
| `allMatch` + cobertura temporal completa | "cabe hoy y en todo futuro **conocido y sin huecos**" | **Sí** |
| Congelar escritura recurrente legacy (409) | — | Correcto pero destructivo; sólo si lo anterior no fuera viable |
| Dar vigencia a `TurnoInstructor` | — | Es rediseñarlo, no compatibilizarlo. Fuera de F2B |

**Corrección**: no basta `allMatch` sobre las versiones futuras. `allMatch` sobre un
conjunto de versiones sólo es una validación honesta si ese conjunto **cubre por completo**
el rango que el RECURRENTE afirma ocupar — `[fechaNegocio, +∞)`, porque no tiene vigencia
propia. Deben cumplirse las cinco condiciones, en este orden:

1. Existen versiones aplicables (`findVersionesQueIntersectan(salonId, diaSemana,
   fechaNegocio, null)` no vacío).
2. La unión de sus vigencias cubre **completamente** `[fechaNegocio, +∞)`.
3. No existen gaps entre ellas.
4. La última versión de la cobertura es abierta hacia `+∞` (`vigenteHasta IS NULL`) — si la
   cobertura termina en una fecha finita sin versión sucesora, **no** hay validación honesta
   posible del "para siempre" del recurrente.
5. El rango horario del Turno cabe en **todas** las versiones que forman la cobertura
   (`allMatch`).

```
versiones = findVersionesQueIntersectan(salonId, diaSemana, fechaNegocio, null)
vacio                          -> SALON_SIN_HORARIO_OPERATIVO
gap en la cobertura            -> TURNO_FUERA_DE_HORARIO_EN_ALGUNA_VIGENCIA
cobertura termina (no +∞)      -> TURNO_FUERA_DE_HORARIO_EN_ALGUNA_VIGENCIA
!versiones.allMatch(cabe)      -> TURNO_FUERA_DE_HORARIO_EN_ALGUNA_VIGENCIA
```

`allMatch` + cobertura temporal completa: si la cobertura termina, **rechazar**; si existe un
gap, **rechazar**. **No se itera día a día** — se reutiliza el mismo barrido por intervalos
(la misma política/algoritmo conceptual) que ya cubre `BloqueProgramacion` en §14, con
`bDesde = fechaNegocio` y `bHasta = null` (abierto).

Un RECURRENTE afirma "esto vale para siempre"; la única validación honesta de una afirmación
infinita contra un futuro parcialmente conocido es exigir que se cumpla en **todo el futuro
conocido y sin huecos**. Es conservadora, pero **falla cerrando**, que es la dirección
correcta.

`fechaNegocio` = `LocalDate.now(clock)` con `Clock` inyectable — no se hardcodea zona horaria
en este documento porque el proyecto todavía no posee una zona de negocio explícita; la zona
debe provenir de configuración/Clock centralizado antes de cablear esto en F2B.2 (§24).

Compatibilidad: con una sola fila NULL/NULL, `findVersionesQueIntersectan(salonId, dia,
fechaNegocio, null)` devuelve esa fila, la cobertura es trivialmente completa (`(−∞,+∞)`,
abierta hacia `+∞`, sin gaps) y `allMatch` sobre un elemento ≡ el `anyMatch` actual.
**Comportamiento idéntico, 0 tests rojos** — por eso el cambio puede hacerse y verificarse en
F2B.2, bajo UNIQUE, antes de que existan versiones reales.

El camino EXCEPCION (con `fecha`) no cambia de forma: delega en `HorarioEfectivoSalon` (§7),
que ya reproduce la lógica excepción-primero hoy escrita a mano. `TurnoInstructor` queda
marcado como **compatibility surface**: F2B lo mantiene correcto, no lo extiende.
**Debe estar cerrado y verde antes de retirar el UNIQUE.**

## 13. BloqueProgramacionService

`BloqueProgramacion` **sí** tiene vigencia (`vigenteDesde` obligatorio, `vigenteHasta`
opcional). Un bloque es válido sólo si está contenido en el horario operativo aplicable en
**cada** tramo de su vigencia:

1. `versiones = findVersionesQueIntersectan(salonId, diaSemana, bDesde, bHasta)`.
2. Vacío ⇒ `SALON_SIN_HORARIO_OPERATIVO`.
3. Verificar cobertura sin gaps (§14); si hay gap ⇒ rechazar.
4. Para **cada** versión: `!horaInicio.isBefore(v.apertura) && !horaFin.isAfter(v.cierre)`.
   Es decir `allMatch`, **no** `anyMatch`.

Ejemplo del encargo — Horario A ene–ago 08–20, Horario B sep–dic 09–20, Bloque ene–dic
08–12: `versiones = {A, B}`; A cumple, B no (`08:00 < 09:00`); `allMatch` falla ⇒
**RECHAZAR**. Correcto: desde septiembre el bloque no está contenido.

Compatibilidad: con una sola fila NULL/NULL no hay gap posible (cubre `(−∞,+∞)`) y
`allMatch` sobre un elemento ≡ el `anyMatch` actual. **Idéntico, 0 tests rojos.**

Los tests de caracterización fijan el mensaje actual (`"El bloque debe estar contenido en el
horario de operación del salón"`), así que ese texto se conserva literal para el caso legacy
y los mensajes nuevos (gap, sin horario) se añaden sólo para los casos nuevos.

## 14. Cobertura temporal y gaps

Dos versiones pueden ser individualmente compatibles con el bloque y aun así dejar un
intervalo sin horario operativo (A hasta 31-ago, B desde 15-sep, bloque todo septiembre ⇒
`1–14 sep` descubierto). Barrido por intervalos, **sin iterar día a día**:

```
cubre(versiones, bDesde, bHasta):
  cursor = bDesde
  para cada v en versiones (orden por vigenteDesde, NULLS FIRST):
      inicio = v.vigenteDesde ?? -INF ; fin = v.vigenteHasta ?? +INF
      si fin < cursor:    continuar                  // version enteramente anterior
      si inicio > cursor: return GAP(cursor, inicio - 1 dia)
      cursor = fin + 1 dia                           // +INF + 1 = +INF
      si cursor > bHasta: return CUBIERTO
  return (bHasta == null) ? GAP(cursor, null) : GAP(cursor, bHasta)
```

O(n) tras el orden, y la query ya devuelve ordenado (§16) ⇒ una sola pasada. La precondición
que lo simplifica es que las versiones **no se solapan** (§5): ordenadas por `vigenteDesde`
lo están también por `vigenteHasta`, sin normalización previa. Antes de F2B.3 lo garantiza
el UNIQUE; después, el EXCLUDE.

**Caso `bloque.vigenteHasta = NULL`**: si la última versión aplicable tiene
`vigenteHasta = NULL` y el barrido llegó sin gap ⇒ **CUBIERTO** (ambos son +∞). Si la última
tiene `vigenteHasta` finito y el bloque es indefinido ⇒ **GAP abierto** ⇒ rechazar. No es
sobre-restrictivo: un bloque indefinido afirma que se imparte para siempre; si el salón
tiene declarado un fin de operación ese día sin versión sucesora, la afirmación es falsa. El
administrador puede acotar el bloque o versionar con continuidad.

## 15. SalonHorarioExcepcion

Regla **preservada sin cambios**: una excepción de fecha concreta **no invalida ni reescribe**
la plantilla recurrente. Bloque semanal lunes 08–12 + un lunes concreto con apertura 09–20:
el `BloqueProgramacion` no se cierra, no se recorta y no se marca inválido — sigue siendo una
plantilla correcta; en la **programación efectiva de esa fecha** la asignación derivada cae
fuera del horario efectivo (`08:00 < 09:00`) y queda no publicable **sólo ese día**.

`HorarioOperacion` versionado responde *"¿cómo es la regla semanal en este periodo?"* y valida
**plantillas**. `SalonHorarioExcepcion` responde *"¿qué pasa este día concreto?"* y participa
en la resolución de **fechas materializadas**.

Consecuencia explícita: **la validación de un Bloque NO consulta `SalonHorarioExcepcion`.** Si
lo hiciera, una sola excepción futura impediría crear una plantilla recurrente perfectamente
válida. `HorarioEfectivoSalon` se usa en resolución por fecha, no en validación de plantillas.

## 16. Repositories

Sólo lo que §6–§14 usan realmente; sin queries preventivas.

**`findVigente(salonId, diaSemana, fecha)`** — resolver (§6).
`AND (vigente_desde IS NULL OR vigente_desde <= :fecha) AND (vigente_hasta IS NULL OR
vigente_hasta >= :fecha)`. Devuelve `List` en la firma y el resolver hace el assert de "≤1"
(§6), en vez de un `Optional` que ocultaría el estado imposible tras una
`NonUniqueResultException` genérica.

**`findVersionesQueIntersectan(salonId, diaSemana, desde, hasta)`** — Bloque (§13, §14) y
Turno (§12, con `hasta = null`).
`AND (:hasta IS NULL OR vigente_desde IS NULL OR vigente_desde <= :hasta) AND (vigente_hasta
IS NULL OR vigente_hasta >= :desde) ORDER BY vigente_desde ASC NULLS FIRST`. Es la misma
condición que ya implementa `vigenciasSeIntersectan` en Java, trasladada a SQL. `NULLS FIRST`
es obligatorio para que la fila legacy quede primera y §14 funcione en una pasada.

**`findVersionesOrdenadas(salonId, diaSemana)`** — **no se crea en F2B.1**: sin el endpoint
`GET /salones/{id}/horarios/historial` (§11, F2B.3b) no tiene consumidor real. Se documenta
aquí como query prevista para ese endpoint, no como entregable de F2B.1 (§19, §24).
**`bloquearVersiones(salonId, diaSemana)`** — `@Lock(PESSIMISTIC_WRITE)` para el lock de
`horario_operacion` en §10 (segundo paso del protocolo, después del lock de fila `Salon`).

Se conservan `findBySalonIdOrderByDiaSemana` (mientras `SalonService` la necesite) y
`deleteBySalonId` (borrado en cascada de salón), pero éste deja de usarse desde
`reemplazarHorarios` (§11).

**Asignacion**: `validarContencionEnBloque` ya exige contención de horas y de vigencia
respecto al Bloque. **Confirmado: `Asignacion` no debe validar `HorarioOperacion`
directamente** — si el Bloque está contenido en el horario de toda su vigencia (§13) y la
Asignación en el Bloque, la contención es transitiva; duplicarlo crearía dos fuentes de verdad
que pueden divergir. Los ajustes por fecha posteriores (materialización) **sí** revalidan
contra `HorarioEfectivoSalon`, porque ahí entra `SalonHorarioExcepcion`, que la transitividad
no cubre (§15).

## 17. Persistencia/Flyway futura y orden respecto al UNIQUE

No se escribe SQL. **V42 y V43 no se editan.**

Numeración **fijada** desde ahora — no se inserta una V44 opcional después de haber
publicado V45, ni se reordena una vez asignada:

| Versión | Nombre | Objetivo | Fase |
|---|---|---|---|
| V44 | `btree_gist_extension` | `CREATE EXTENSION IF NOT EXISTS btree_gist`, **aislada** | F2B.3a |
| V45 | `horario_operacion_exclude_vigencia` | `ADD CONSTRAINT ex_horario_operacion_vigencia EXCLUDE USING gist (...)`. Coexiste con el UNIQUE | F2B.3a |
| V46 | `horario_operacion_drop_unique_dia` | `DROP CONSTRAINT horario_operacion_salon_id_dia_semana_key` | F2B.3a |
| V47+ | `horario_operacion_indices_vigencia` (u otro nombre) | Índice `(salon_id, dia_semana, vigente_desde)`. Sólo rendimiento, opcional, sólo si perfiles reales lo justifican | Sin fase fija, posterior a V46 |

Orden dentro de la migración **nunca** se invierte: `DROP UNIQUE` (V46) no puede ir antes de
`ADD EXCLUDE` (V45) — dejaría una ventana sin ninguna garantía de no-solape.

**Corrección sobre el privilegio de `btree_gist`**: no requiere necesariamente superusuario
en PostgreSQL 16 — es una extensión **trusted**. El requisito operativo real es: (1) los
archivos de la extensión estén instalados en el servidor; (2) el rol de despliegue tenga
privilegios suficientes, normalmente `CREATE` sobre la base de datos; (3) verificarlo en el
entorno real antes de desplegar V44, porque es el único paso de V44–V46 que puede fallar por
permisos y no por datos — Testcontainers ya lo verificó (usa un rol con privilegios amplios
por defecto, lo que no es evidencia sobre el rol de producción). Separar V44 de V45 permite
diagnosticar ese fallo aislado. V45 no requiere generated column (§5-1) y **puede aplicarse
con el UNIQUE presente**: verificado, ambos coexisten, y con el UNIQUE activo el EXCLUDE es
trivialmente satisfecho, así que V45 no puede fallar por datos legacy. V46 es el único paso
irreversible en la práctica (recrear el UNIQUE fallaría si ya hay versiones múltiples): debe
ser el último de F2B.3a.

**Aspecto operativo de `ADD CONSTRAINT ... EXCLUDE` (V45)**: además de ser la verificación
exhaustiva en sí misma (ver abajo), `ALTER TABLE ... ADD CONSTRAINT ... EXCLUDE` puede tomar
un lock fuerte (`ACCESS EXCLUSIVE`) sobre `horario_operacion` mientras construye el índice
GiST. Para el despliegue real futuro — no diseño adicional ahora, sólo registrar el
requisito —: medir el tamaño de `horario_operacion`, considerar `lock_timeout`, y planificar
una ventana de despliegue. Una pre-query de diagnóstico previa es opcional; no es necesidad
técnica, porque `ADD CONSTRAINT` ya valida todas las filas existentes.

**Orden**, con el principio de que **nunca existe una ventana sin ninguna garantía**:

```
1. F2B.1   Resolver + queries temporales.       Garantia: UNIQUE
2. F2B.2   Consumers deterministas.             Garantia: UNIQUE
3. V44     btree_gist.                          Garantia: UNIQUE
4. V45     ADD EXCLUDE (coexiste con UNIQUE).   Garantia: UNIQUE + EXCLUDE
5.         Verificacion de datos.               Garantia: UNIQUE + EXCLUDE
6. V46     DROP UNIQUE.                         Garantia: EXCLUDE
   -- fin de F2B.3a --
7. F2B.3b  Habilitar escritores versionados.    Garantia: EXCLUDE
```

Se **corrige** el orden del encargo en un punto: "verificar datos" va **después** de instalar
el EXCLUDE, no antes. Con el UNIQUE activo es imposible que existan solapes, así que una
verificación previa no puede encontrar nada; y `ADD CONSTRAINT ... EXCLUDE` **es en sí mismo**
la verificación exhaustiva, porque valida toda la tabla al crearse. El paso 7 va tras el 6
porque habilitar escritores versionados bajo UNIQUE haría fallar el segundo insert de
cualquier versionado real, y por eso F2B.3b es una fase separada de F2B.3a (§21).

## 18. Safety net (a diseñar, no implementar)

**Resolver y semántica**: legacy NULL/NULL resuelve fecha pasada/hoy/futura; frontera `D−1`
→vieja y `D`→nueva; fecha == `vigenteHasta` incluida; fecha == `vigenteDesde` incluida; fecha
en gap ⇒ vacío; mapeo domingo=0 / sábado=6.

**Constraint (integración PG16)**: versiones contiguas aceptadas; solape de un solo día
rechazado; segunda fila NULL/NULL rechazada; nueva versión sin cerrar la legacy rechazada;
distinto salón o distinto día aceptados; UPDATE que provoca solape rechazado.

**Versionado**: legacy NULL/NULL queda `NULL / D−1` y nueva `D / NULL`; versionar sin horario
previo (§9-A); cierre recurrente sin nueva versión (§9-I) ⇒ resolver posterior vacío; casos
C/D/E/F rechazados con su error específico; **concurrencia**: dos writers simultáneos,
exactamente uno commitea, el otro recibe error de dominio (no 500), estado final sin solape.

**Consumidores**: Bloque contenido en todas las versiones ⇒ aceptado; Bloque con versiones
contiguas que cubren toda su vigencia ⇒ aceptado; Bloque inválido tras cambio de apertura en
versión posterior (§13) ⇒ rechazado; Bloque sobre gap (§14) ⇒ rechazado; Bloque con
`vigenteHasta = NULL` sobre horario finito ⇒ rechazado y sobre horario abierto ⇒ aceptado;
RECURRENTE válido hoy pero inválido en versión futura ⇒ rechazado (§12); EXCEPCION con
excepción CERRADO ⇒ rechazado; excepción que amplía ⇒ turno aceptado aunque no quepa en el
semanal; excepción que reduce ⇒ turno rechazado; excepción puntual **no** invalida el Bloque
recurrente (§15).

**Compatibilidad legacy (los decisivos)**: con datos exclusivamente NULL/NULL,
`obtenerDetalle` devuelve exactamente la misma respuesta que antes, y todas las validaciones
de Turno y Bloque producen el mismo resultado y el mismo mensaje que hoy; **los 131 tests
existentes siguen verdes sin modificación en F2B.1 y F2B.2**. Este último es el criterio de
aceptación duro de ambas subfases.

**Dirección inversa — Política A (§8, F2B.3b)**:
- Versionar horario y existe Turno recurrente compatible con el resultado ⇒ **permitido**.
- Versionar horario y el resultado deja un Turno recurrente fuera ⇒ **rechazo**.
- Cerrar horario y existe Turno recurrente activo ese día ⇒ **rechazo**.
- Versionar horario y un Bloque futuro sigue contenido en el resultado ⇒ **permitido**.
- Versionar horario y un Bloque futuro deja de estar contenido ⇒ **rechazo**.
- Cerrar horario y un Bloque intersecta el periodo que quedaría sin horario ⇒ **rechazo**.

**Concurrencia inversa (§10, protocolo compartido, F2B.3b)**:
- TX A versiona horario, TX B crea un Bloque incompatible con el resultado de A ⇒ **no
  pueden cometer ambas**.
- TX A versiona horario, TX B crea un Turno recurrente incompatible con el resultado de A ⇒
  **no pueden cometer ambas**.
- Ambos escenarios requieren el protocolo compartido de lock de fila `Salon` (§10); no basta
  con el `FOR UPDATE` sobre `horario_operacion` por sí solo.

## 19. F2B.1

**Alcance: núcleo temporal estrictamente ADITIVO.** Introducir la capacidad de resolver
horario por fecha **sin que ningún consumidor cambie de comportamiento** y **sin tocar el
UNIQUE**.

**Crear**:
- `RangoVigencia` (§3): `contiene`, `intersecta`, `esContiguoCon` con guarda de infinitos.
- Utilidad compartida `DayOfWeek → diaSemana` (0 = domingo).
- `HorarioOperacionRepository`: `findVigente`, `findVersionesQueIntersectan` (§16). Sólo
  adiciones; ninguna firma existente se toca.
- `HorarioOperacionResolver` (§6): entrada `salonId + fecha`; query por fecha; 0 resultados
  ⇒ `Optional.empty()`; 1 ⇒ resultado; `>1` ⇒ fallo ruidoso.

**Tests**: NULL/NULL; límites inclusivos; intersección; continuidad; gaps a nivel de
`RangoVigencia`; domingo/sábado; resolver con 0/1/`>1` resultados mediante unit tests/mocks
apropiados; repositorio con PostgreSQL real sólo en escenarios compatibles con el UNIQUE
(pares `(salon, dia)` distintos, o inserción directa donde sea imprescindible); **los 131
tests existentes siguen verdes sin modificación**.

**NO crear todavía**: `HorarioEfectivoSalon` (se crea y cablea recién en F2B.2, §20 — no
antes); `VersionarHorarioOperacion`; `CerrarHorarioOperacion`; `Clock` para consumers;
migraciones; `btree_gist`; EXCLUDE; DROP UNIQUE; endpoints; cambios a consumers.
`findVersionesOrdenadas` tampoco se crea en F2B.1 (§16): no tiene consumidor real hasta el
endpoint de historial de F2B.3b. No se intenta persistir dos versiones del mismo
`(salon, dia)`: el UNIQUE todavía lo prohíbe.

**Resultado**: F2B.1 puede implementarse sin: quitar el UNIQUE; crear una segunda versión
real; modificar consumers; modificar el schema; introducir writers. Su finalidad es preparar
y probar semántica temporal + queries + resolver, nada más. Todo aditivo y no invocado desde
rutas productivas: revertir = borrar los archivos nuevos.

## 20. F2B.2

Que ningún consumidor asuma "una fila por día", **con el UNIQUE presente** y comportamiento
observable equivalente:

- **Crear y cablear** `HorarioEfectivoSalon` (§7) — aquí, no en F2B.1.
- `SalonService.mapDetalle` → versión vigente en la fecha de negocio (§11), con
  `LocalDate.now(clock)` y `Clock` inyectable.
- `TurnoInstructorService`: RECURRENTE → `allMatch` + cobertura completa
  `[fechaNegocio, +∞)` (§12, corregido); EXCEPCION → vía `HorarioEfectivoSalon`.
- `BloqueProgramacionService` → `allMatch` + cobertura de toda su vigencia (§13, §14).
- Migrar la semántica temporal duplicada (`vigenciasSeIntersectan`) a `RangoVigencia`.
- `SalonService` update → retirar el comportamiento destructivo de `reemplazarHorarios`
  (§11): `horarios == null` no toca horarios; escritura legacy explícita de horarios sigue
  el plan de compatibilidad definido en §11, **nunca** delete+insert destructivo.

**Antes de declarar F2B.2 desplegable** debe resolverse la compatibilidad real del cliente
web según el inventario de §11: el cliente actual envía `horarios` siempre, así que activar
el rechazo `HORARIOS_REQUIEREN_VERSIONADO` en producción queda bloqueado hasta migrar el
cliente — F2B.2 puede diseñarse y probarse igualmente bajo el UNIQUE, pero esa activación es
una dependencia real de despliegue, no de diseño.

**Tests requeridos**: actualizar sólo teléfono → no toca horarios; `horarios == null` → no
toca horarios; escritura legacy explícita de horarios → comportamiento definido según el plan
de compatibilidad, nunca delete+insert destructivo; datos exclusivamente NULL/NULL →
comportamiento observable equivalente al baseline. El UNIQUE sigue activo durante toda la
fase.

Con datos legacy todos estos cambios son no-ops observables: ése es el punto.

## 21. F2B.3a — Persistencia

Únicamente migraciones y verificación de constraint, **sin habilitar endpoints/writer todavía**
si el despliegue se separa de la implementación: V44 `btree_gist`, V45 `ADD EXCLUDE`, V46
`DROP UNIQUE`, en ese orden (§17).

**Tests PostgreSQL/Testcontainers**: coexistencia UNIQUE + EXCLUDE antes de V46; frontera
contigua; overlap de un día; NULL bounds; UPDATE que provoca conflicto; distinto salón;
distinto día; Flyway V1→V46; JPA `validate`.

**Prerrequisito duro: §12 cerrado y verde** — sin eso, retirar el UNIQUE convierte la
validación de turnos recurrentes en permisiva sin aviso.

## 22. F2B.3b — Writers

Después de F2B.3a. `VersionarHorarioOperacion` y `CerrarHorarioOperacion` (§8) con lock de
`Salon` primero (§10), validación inversa Política A contra Turnos y Bloques (§8, §20), y
endpoint/API administrativa explícita con `efectivoDesde` (§11).

Actualizar también los writers de `BloqueProgramacion` y `TurnoInstructor` RECURRENTE para
participar en el mismo protocolo de lock de `Salon` (§10, protocolo compartido).

Traducir la violación `SQLSTATE 23P01` del constraint `ex_horario_operacion_vigencia` a
conflicto de dominio / HTTP 409 (§10).

**Tests de concurrencia reales** (dos transacciones efectivas, no sólo precheck en
aplicación) para los escenarios de §18 (dirección inversa y concurrencia inversa). No se
depende sólo del precheck de aplicación: el EXCLUDE y el lock de `Salon` deben demostrarse
bajo concurrencia real.

## 23. Decisiones cerradas

1. `[vigenteDesde, vigenteHasta]` cerrado en ambos extremos; NULL = ilimitado. Idéntica a la
   que ya usa `vigenciasSeIntersectan`.
2. Legacy NULL/NULL significa `(−∞, +∞)`; no se normaliza con fecha inventada. Al versionar
   desde `D`: legacy → `NULL / D−1`, nueva → `D / NULL`. Verificado en PG16.
3. `vigente_desde IS NULL` **no** identifica la versión vigente y no se usa como predicado de
   escritura; se localiza por contención de fecha.
4. Resolver por fecha devuelve 0 o 1; 0 ⇒ `Optional.empty()` /
   `SALON_SIN_HORARIO_OPERATIVO`; nunca se inventa horario; >1 falla ruidosamente.
5. No-overlap con `EXCLUDE USING gist` + `btree_gist`, expresión
   `daterange(vigente_desde, vigente_hasta, '[]')` **directa**, sin generated column y sin
   trigger. Verificado en 16.14.
6. UNIQUE a retirar: `horario_operacion_salon_id_dia_semana_key`.
7. Versionar = close + insert en una transacción, sin borrar filas ni reescribir el pasado.
   F2B soporta sólo append futuro (incluida reapertura, §9-J) y cierre; C/D/E/F se rechazan.
8. Orden: consumidores primero (F2B.1, F2B.2 bajo UNIQUE), luego V44 btree_gist → V45 ADD
   EXCLUDE → verificación → V46 DROP UNIQUE (F2B.3a) → escritores versionados (F2B.3b).
   Coexistencia verificada.
9. Bloque: `allMatch` sobre todas las versiones que intersectan, nunca `anyMatch`.
10. Gaps: la unión de vigencias aplicables debe cubrir por completo la del bloque (y, para
    Turno RECURRENTE, `[fechaNegocio, +∞)`, §12); barrido lineal por intervalos. Cobertura
    finita sin sucesora ⇒ rechazado.
11. `SalonHorarioExcepcion` no invalida plantillas recurrentes; participa sólo en resolución
    por fecha. No se mueve de `ubicaciones`. No sigue la Política A de dirección inversa.
12. `Asignacion` no revalida `HorarioOperacion`: la contención es transitiva vía Bloque.
13. `SalonService` devuelve el horario vigente en la fecha de negocio; `SalonDetalleResponse`
    no se amplía. `SalonService` **no infiere** `efectivoDesde`: actualización con `horarios`
    presente se rechaza (`HORARIOS_REQUIEREN_VERSIONADO`) una vez exista el endpoint
    explícito, sujeto a la dependencia de migración del cliente web (§11).
14. Concurrencia: lock de fila `Salon` primero, luego `FOR UPDATE` sobre las versiones del
    par `(salon, dia)`, con EXCLUDE como backstop; 23P01 traducido a error de dominio. Sin
    locks distribuidos.
15. **Dirección inversa — Política A** (§8): `VersionarHorarioOperacion` y
    `CerrarHorarioOperacion` rechazan si dejan incompatible programación recurrente
    (`TurnoInstructor` RECURRENTE o `BloqueProgramacion`) activa desde `efectivoDesde` hacia
    adelante. No hay degradación silenciosa.
16. **Protocolo compartido de lock**: en F2B.3b, `VersionarHorarioOperacion`,
    `CerrarHorarioOperacion` y los writers de `BloqueProgramacion`/`TurnoInstructor`
    RECURRENTE adquieren la fila `Salon` antes de validar y persistir (§10).
17. F2B.1 no crea writer ni `VersionarHorarioOperacion`/`CerrarHorarioOperacion` (§19).
18. F2B.1 no crea `HorarioEfectivoSalon` "muerta" (sin cablear): se crea y cablea en F2B.2
    (§20), no antes.
19. V44/V45/V46 fijados: `btree_gist` / `ADD EXCLUDE` / `DROP UNIQUE`, en ese orden (§17). No
    se inserta una V44 opcional después de haber publicado V45.
20. Reapertura sin historia futura (§9-J): `D → NULL` sin modificar historia anterior, gap
    histórico permitido. Gap intermedio **con** versión futura (§9-D): rechazado con
    `VERSIONADO_INTERMEDIO_NO_SOPORTADO`.

## 24. Decisiones abiertas

**BLOQUEA F2B.1** — ninguna. F2B.1 está completamente especificada, sin blockers
arquitectónicos.

**BLOQUEA F2B.2** — únicamente dependencias reales, no de diseño:
- Compatibilidad real del cliente web: el inventario de §11 confirma que
  `Feelingpilates/web` envía `horarios` siempre en cada `PUT /salones/{id}`; activar el
  rechazo `HORARIOS_REQUIEREN_VERSIONADO` requiere migrar el cliente primero. No bloquea
  implementar F2B.2 bajo el UNIQUE, sí bloquea activar esa política en producción.
- Definición de `Clock`/zona horaria de negocio: el proyecto todavía no tiene una fuente
  temporal central; debe resolverse antes de cablear `LocalDate.now(clock)` en `SalonService`
  y `TurnoInstructorService` (§11, §12).
- Si los mensajes nuevos de `BloqueProgramacionService` (gap, sin horario) reutilizan el texto
  actual o se añaden nuevos; determina qué tests hay que tocar.

**BLOQUEA F2B.3a**
- Privilegio para `CREATE EXTENSION btree_gist` en el entorno real: es extensión *trusted*,
  no requiere superusuario necesariamente (§17), pero la disponibilidad de archivos de
  extensión y el privilegio `CREATE` del rol de despliegue deben confirmarse en el entorno
  real (funciona en Testcontainers, que no es evidencia sobre el rol de producción).

**BLOQUEA F2B.3b**
- UX exacta de los endpoints administrativos (§11) puede refinarse, pero las reglas de
  dominio (Política A, lock, edge cases de §9) ya están cerradas y no son parte de lo
  abierto.

**No queda abierto**: qué ocurre con Bloques/Turnos existentes al versionar u cerrar un
horario — queda **cerrado por la Política A** (§8, decisión 15 arriba). No es una decisión
pendiente de F2B.3.

**PUEDE ESPERAR** — UX y forma de los endpoints administrativos (§11); casos C/D/E/F de §9;
dar vigencia propia a `TurnoInstructor` y retirarlo como compatibility surface; índice V47+
según volumen; UNIQUE parcial de refuerzo `WHERE vigente_hasta IS NULL`.

## 25. Primera implementación

Pequeña, aditiva, reversible. **No retira el UNIQUE. No cambia comportamiento observable.**
Alcance de F2B.1 — ver también §19 para el detalle completo.

**Nuevo**: `ubicaciones/dominio/RangoVigencia` (`contiene`, `intersecta`, `esContiguoCon` con
guarda de infinitos, §3); `ubicaciones/servicio/HorarioOperacionResolver` con assert de ≤1
resultado; utilidad compartida `DayOfWeek → diaSemana` (0 = domingo). **No** se crea
`HorarioEfectivoSalon` en esta fase (§19, §20).

**Modificado**: `HorarioOperacionRepository` — añadir `findVigente`,
`findVersionesQueIntersectan`. **Sólo adiciones**; ninguna firma existente se toca.
`findVersionesOrdenadas` **no** se añade aquí (§16): sin consumidor real hasta el endpoint de
historial de F2B.3b.

**Tests**: resolver y fronteras; escenarios de versionado limitados a lo montable bajo UNIQUE
(sobre `(salon, dia)` distintos, o inserción directa donde sea imprescindible); y los de
compatibilidad, incluido "los 131 existentes verdes sin modificación".

**Migraciones**: ninguna obligatoria en F2B.1. V44/V45/V46 quedan reservados para `btree_gist`
/ EXCLUDE / DROP UNIQUE en F2B.3a (§17); no se toca esa numeración desde F2B.1.

**NO toca**: `SalonService`, `TurnoInstructorService`, `BloqueProgramacionService`, la entidad
`HorarioOperacion`, `SalonHorarioExcepcion`, V42/V43 ni ninguna migración existente. No
instala `btree_gist`, no crea el EXCLUDE, no retira el UNIQUE, no modifica ninguno de los 131
tests.
