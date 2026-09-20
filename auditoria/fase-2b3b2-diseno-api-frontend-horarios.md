# Fase 2B.3b.2.0.2 — Diseño de API administrativa y migración frontend para horarios versionados

> **Revisión F2B.3b.2.0.2.** Sustituye a F2B.3b.2.0.1 en este mismo archivo. Cierra **exclusivamente
> las dos contradicciones UX (P1)** de la revisión de F2B.3b.2.0.1, ambas documentales y ambas en el
> frontend:
>
> - **P1-1 — afirmación falsa sobre D−1.** El texto de ayuda del selector de fecha (§17) presentaba
>   como regla universal que *"el horario anterior se conserva en el historial hasta el día previo"*.
>   Es falso en la **reapertura después de un gap** (día cerrado hoy, versionado con fecha futura):
>   ahí no hay horario anterior vigente hasta D−1. Sustituido por texto **neutral** (§17, §29).
> - **P1-2 — refresco condicional del historial.** §29 decía "después del éxito, siempre" y su propio
>   paso 5 lo condicionaba a que el panel estuviera visible. Regla única en **§29.1**: tras cada
>   operación con éxito se refrescan **siempre** detalle e historial, y **después** el modal.
>
> **No se reabre ninguna decisión de backend** (endpoints, 201/200, whitelist 400/409,
> `CodigoErrorExtractor`, `ErrorResponse.codigo`, request malformado, historial 403/400/404/`200 []`,
> seguridad, `AutorizadorSalon`, `PUT` legacy), **ni** las de `DialogoSalon` (§27.3), **ni** la de
> source control del frontend (§45).
>
> **Revisión previa F2B.3b.2.0.1.** Sustituyó a F2B.3b.2.0 en este mismo archivo. Cierra exclusivamente
> los P0/P1/P2 de la revisión de contrato: manejo de request malformado → 400 (§11.6), **un solo**
> extractor de código de error (§11.5, §12), orden de operaciones y validación de `diaSemana` del
> historial (§7.4), 404 de salón inexistente en historial (§7.2), corrección de la afirmación sobre
> `SalonHorarioExcepcionService` (§2.2), mensaje de éxito neutral en frontend (§29), `DialogoSalon`
> en modo edición (§27.3), source control del frontend (§45), y las afirmaciones falsas sobre FK
> indexada (§36) y working tree limpio (esta cabecera). **No se reabre** ninguna decisión ya
> aprobada: paths, permisos, `AutorizadorSalon`, `PUT` legacy tolerante, historial read-only,
> rollout backend-first y la separación F2B.3b.2a / F2B.3b.2b siguen tal cual.

## Base

Branch `operacion/horario-versionado-writers`, HEAD `b9e6aa875bce6676b4884517d54ae6ac9655ec21`,
working tree **SOLO CHECKPOINT**: la única entrada es
`?? auditoria/fase-2b3b2-diseno-api-frontend-horarios.md`. Mientras el checkpoint no esté
committed, "working tree limpio" sería una afirmación falsa. Persistencia vigente V44–V46 (`btree_gist`, EXCLUDE
`ex_horario_operacion_vigencia`, `DROP UNIQUE(salon_id, dia_semana)`). Writers internos disponibles
y revisados: `VersionarHorarioOperacion`, `CerrarHorarioOperacion`.

Tarea **exclusivamente de diseño**: sin código productivo, sin migraciones, sin tests, sin tocar
frontend. Único archivo escrito, este documento.

Checkpoints autoritativos previos: `auditoria/fase-2b-diseno-versionado-horario.md`,
`auditoria/fase-2b3b-diseno-writers-concurrencia.md`,
`auditoria/fase-2b3b1-writers-concurrencia-horario.md`. Aquí **no se reabre** ninguna semántica de
Versionar/Cerrar: se expone por HTTP lo que ya existe y se migra el frontend a ello.

---

## 1. Executive Summary

Diseño **completo y sin bloqueadores de backend**. Todo lo que afecta al contrato público queda
cerrado en §42.

Hallazgos que condicionan el diseño, todos verificados contra el código real:

- **Existe un sibling que fija el patrón, con una excepción que NO debe copiarse** (§2.2, §13,
  §14): `SalonHorarioExcepcionController` + `SalonHorarioExcepcionService` ya son un sub-recurso de
  `/api/salones/{salonId}` con `@PreAuthorize` en el borde y `AutorizadorSalon` (permiso **+**
  scope por salón). En `listarPorRango` y `guardar` la autorización es la **primera sentencia** —
  ése es el patrón correcto y el que se replica. En `eliminar`, en cambio, se lee la entidad
  **antes** de autorizar: ese orden **no se copia** (§2.2). No hay que inventar mecanismo de scope,
  sólo elegir el orden correcto de los dos que hay.
- **Hoy todo código de dominio de horario respondería 400, no 409** (§11). `GlobalExceptionHandler`
  mapea `ValidacionException → 400` sin excepciones, y los writers lanzan `ValidacionException`
  para *todos* sus rechazos — incluidos los de estado (`CIERRE_CON_VERSIONES_FUTURAS`,
  `CANCELACION_DE_VERSION_NO_SOPORTADA`, …). El único que ya da 409 es `CONFLICTO_VIGENCIA_HORARIO`,
  porque el traductor de `23P01` lanza `ConflictException`. Se cierra con una **traducción en la
  capa de aplicación** (§11.3): sin tocar los writers, sin nuevos `@ExceptionHandler` y sin
  contradecir el handler existente.
- **El contrato de error hoy sólo tiene `message`** (§12). El código estable ya viaja como prefijo
  `CODIGO: texto` por convención del proyecto. El cambio mínimo es **un único componente neutral**,
  `CodigoErrorExtractor` (§11.5), que hace una sola cosa — `message → codigo | null` — y que usan
  **los dos** consumidores: la capa de aplicación de horarios, para decidir si una
  `ValidacionException` está en la whitelist de 409, y `GlobalExceptionHandler.build(...)`, para
  llenar el campo `codigo` aditivo de `ErrorResponse`. **Una sola regex en todo el proyecto, un
  solo parsing.** El extractor **no** conoce HTTP, ni `HorarioOperacion`, ni turnos, ni bloques: la
  clasificación `codigo → 400/409` es una whitelist explícita que vive en la capa de aplicación de
  horarios (§11.5). Todos los códigos estables existentes (incluido `HORARIOS_REQUIEREN_VERSIONADO`)
  ganan el campo `codigo` gratis.

- **Hoy un request malformado devuelve 500, y eso se arregla dentro de esta fase** (§11.6). Sin
  `@ExceptionHandler` propio, `HttpMessageNotReadableException` (JSON ilegible, fecha u hora no
  parseable) y `MethodArgumentTypeMismatchException` (`{salonId}` que no es UUID, `?diaSemana=abc`)
  caen en el handler genérico `Exception` → **500 "Ocurrió un error inesperado"**. Una API nueva no
  puede prometer 400 para request malformado mientras el handler devuelve 500, así que F2B.3b.2a
  incorpora **exactamente esos dos handlers**, con mensaje fijo y `codigo: null`. Los errores
  inesperados siguen siendo 500.
- **El frontend rompe por dos sitios, no por uno** (§3, §27). Además de
  `EditarHorarioSemanalDialog`, el wizard `DialogoSalon` tiene un paso "Horarios de atención"
  editable **también en modo edición** y manda la semana entera en el `PUT`. Cualquier cambio ahí
  choca hoy contra `HORARIOS_REQUIEREN_VERSIONADO`. Y hay un segundo defecto en el mismo paso: la
  validación `errorDelPaso(2)` — *"Activa al menos un día de atención."* — se ejecuta **también al
  editar** (`handleSubmit` recorre los pasos 0..3), de modo que un salón cerrado los siete días
  **no podría ni cambiar su teléfono**. Con horarios versionados, "cerrado los siete días" es un
  estado alcanzable y legítimo. La migración cubre los dos consumidores y separa explícitamente
  CREAR de EDITAR (§27.3).
- **El historial es necesario, no opcional** (§7). Con `efectivoDesde` futuro, el detalle del salón
  (horario vigente *hoy*) no cambia: sin historial la UI parecería no haber guardado nada, y
  `CIERRE_CON_VERSIONES_FUTURAS` sería inexplicable para el operador.
- **El frontend no tiene suite de tests** (§34). `package.json` no declara vitest/jest/testing-library.
  No se inventa framework: red automatizada = `tsc -b` + `oxlint`, más un plan manual reproducible.

- **El frontend `web` no está bajo control de versiones** (§45). `/Feelingpilates/web` no pertenece
  a ningún repositorio Git. No se mueve dentro del repo backend ni se convierte éste en monorepo
  durante F2B: `web` tendrá su **propio repositorio autoritativo**, y crearlo/conectarlo es
  prerrequisito **operativo** de F2B.3b.2b. No es una decisión de contrato abierta.

**DISEÑO COMPLETADO.**
**F2B.3b.2a (backend): LISTA**, sin bloqueadores.
**F2B.3b.2b (frontend): LISTA EN DISEÑO / BLOQUEADA PARA IMPLEMENTACIÓN** — el contrato está
cerrado; la implementación espera a que `/Feelingpilates/web` tenga repositorio Git autoritativo y
baseline limpio (§45), además del orden de despliegue de §39.

---

## 2. Inventario HTTP actual (verificado)

### 2.1 `SalonController` — `/api/salones`

| Método | Path | Request | Response | Status | Autorización | Scope por salón |
|---|---|---|---|---|---|---|
| GET | `/api/salones` | — | `List<SalonResponse>` | 200 | **ninguna** (`@PreAuthorize` ausente; sólo autenticado) | no |
| GET | `/api/salones/{id}` | — | `SalonDetalleResponse` | 200 | `hasAuthority('salon.leer')` | **no** |
| POST | `/api/salones` | `SalonRequest` | `SalonDetalleResponse` | **201** | `hasAuthority('salon.administrar')` | n/a |
| PUT | `/api/salones/{id}` | `SalonRequest` | `SalonDetalleResponse` | 200 | `hasAuthority('salon.administrar')` | **no** |

`SalonRequest.horarios: List<HorarioOperacionRequest>` (`diaSemana`, `horaApertura`, `horaCierre`;
sin fechas de vigencia). `SalonDetalleResponse.horarios: List<HorarioOperacionResponse>`
(`id`, `diaSemana`, `horaApertura`, `horaCierre`; **sin vigencias**).

### 2.2 `SalonHorarioExcepcionController` — `/api/salones/{salonId}/excepciones-horario`

| Método | Path | Request | Response | Status | Autorización | Scope |
|---|---|---|---|---|---|---|
| GET | `…/excepciones-horario?desde&hasta` | — | `List<SalonHorarioExcepcionResponse>` | 200 | `salon.leer` | **sí**, `AutorizadorSalon` |
| PUT | `…/excepciones-horario` | `GuardarExcepcionSalonRequest` | `SalonHorarioExcepcionResponse` | 200 | `salon.administrar` | **sí** |
| DELETE | `…/excepciones-horario/{id}` | — | vacío | 200 | `salon.administrar` | **sí** |

Recibe el actor con `@AuthenticationPrincipal UsuarioAutenticado actor` y pasa `actor.id()` al
service.

**Corrección respecto de F2B.3b.2.0** (afirmación documental incorrecta, ya retirada): **no** es
cierto que todos los métodos de `SalonHorarioExcepcionService` autoricen antes de leer. El estado
real, verificado línea a línea:

| Método | Orden real | Uso como patrón |
|---|---|---|
| `listarPorRango` | `autorizadorSalon.verificarAccesoSalon(...)` es la **primera sentencia**; después consulta | ✅ **patrón correcto**, se replica en `GET historial` |
| `guardar` | `verificarAccesoSalon(...)` primera sentencia; después `salonRepository.findById` | ✅ **patrón correcto**, se replica en versionar/cerrar |
| `eliminar` | `excepcionRepository.findById(id)` **primero**, luego `excepcion.getSalon().getId()`, y **sólo entonces** `verificarAccesoSalon(...)` | ❌ **lee antes de autorizar; NO se copia este orden** |

`eliminar` funciona porque necesita resolver el salón real de la excepción antes de poder
autorizar contra él, pero deja que un actor sin scope provoque una lectura y distinga
"existe/no existe" por el 404 previo. **No se modifica `SalonHorarioExcepcionService` en
F2B.3b.2a** (fuera de alcance, §41); se registra como observación y se evita reproducir el orden.
Los endpoints nuevos no tienen ese problema: se direccionan por `salonId` de la URL, así que
autorizar primero es siempre posible.

### 2.3 Seguridad global

`SecurityConfig`: `/api/auth/**`, swagger, `GET /api/publico/**`, `GET /api/usuarios/*/foto` y
`POST /api/pagos/webhook` son `permitAll`; **`anyRequest().authenticated()`**. Todo `/api/salones/**`
exige autenticación; el detalle fino lo pone `@PreAuthorize`. **No hay que tocar `SecurityConfig`.**

### 2.4 Catálogo de permisos (V… seed)

Sólo dos permisos en el dominio SALONES: `salon.leer` ("Ver detalle de salones, espacios, **horarios**
y catálogos") y `salon.administrar` ("Crear y editar salones, espacios, **horarios**, actividades y
máquinas"). **No existe permiso más fino de horario**, así que no hay nada más preciso que derivar.

### 2.5 Manejo de errores

`GlobalExceptionHandler` (`@RestControllerAdvice`), único punto de construcción de `ErrorResponse`
en todo el proyecto (verificado por grep):

| Excepción | Status |
|---|---|
| `ResourceNotFoundException` | 404 |
| `MethodArgumentNotValidException` | 400 + `fieldErrors` |
| `ConflictException` | **409** |
| `ValidacionException` | **400** |
| `AccessDeniedException` / `AuthorizationDeniedException` | **403** |
| `DataIntegrityViolationException` | 409 (genérico) |
| `Exception` | 500 |

`ErrorResponse(timestamp, status, error, message, path, fieldErrors)`.

**Excepciones Spring MVC que hoy NO tienen handler propio** (derivado del stack real, no supuesto:
Spring Boot 4.1.0 / Spring Framework 7.0.8, verificado en `pom.xml` y en los jars resueltos). El
advice declara `@ExceptionHandler(Exception.class)`, y `ExceptionHandlerExceptionResolver` se
consulta **antes** que `DefaultHandlerExceptionResolver`/`ResponseStatusExceptionResolver`, así que
ese handler genérico gana y **todas** terminan en **500 "Ocurrió un error inesperado"**:

| Excepción | Cuándo ocurre en los endpoints nuevos | Hoy | Debe ser |
|---|---|---|---|
| `HttpMessageNotReadableException` (`org.springframework.http.converter`) | JSON sintácticamente inválido; `"efectivoDesde": "01/09/2026"`; `"horaApertura": "8am"` | 500 | **400** (§11.6) |
| `MethodArgumentTypeMismatchException` (`org.springframework.web.method.annotation`) | `{salonId}` que no es UUID; `?diaSemana=abc` en el historial | 500 | **400** (§11.6) |

Verificado además, para **no** asumir excepciones que este proyecto no produce:

- `MethodArgumentNotValidException` **sí existe** en el stack y **ya tiene handler** → 400 con
  `fieldErrors`. No se toca.
- `HandlerMethodValidationException` (`ResponseStatusException` en Spring 6.1+/7) sólo se lanza si
  hay **constraints sobre parámetros del método del controller**. El diseño **no** los usa: el
  rango `0..6` de `diaSemana` se valida en la capa de aplicación (§5, §7.4), así que esta excepción
  **no es alcanzable** y **no se le añade handler**. Si alguna vez se añadieran constraints a
  parámetros de controller, habría que revisitar esto — queda escrito para que no sea una sorpresa.
- `jakarta.validation.ConstraintViolationException` **no es alcanzable**: `grep -rn "@Validated"
  src/main/java` no devuelve **ninguna** ocurrencia, así que no hay validación por proxy de método.
  La única `ConstraintViolationException` del proyecto es la de **Hibernate**
  (`org.hibernate.exception`), que ya consume `ConflictoVigenciaHorarioTranslator` y llega envuelta
  en `DataIntegrityViolationException`. Son clases distintas; no se confunden.
- `MissingServletRequestParameterException` **no es alcanzable**: el único parámetro de query es
  `diaSemana`, y es **opcional**.

### 2.6 Writers y piezas de dominio disponibles

- `VersionarHorarioOperacion.ejecutar(VersionarHorario(salonId, diaSemana, efectivoDesde, horaApertura, horaCierre)) → HorarioOperacion`
- `CerrarHorarioOperacion.ejecutar(CerrarHorario(salonId, diaSemana, efectivoDesde)) → HorarioOperacion`
- Ambos `@Transactional`, ambos empiezan por `SalonLock.adquirir(salonId)` — que es además su
  **única comprobación de existencia** y lanza `ResourceNotFoundException` si el salón no existe.
- `HorarioOperacionErrores`: 9 códigos estables + `verificarSinImpacto` (Política A).
- `ConflictoVigenciaHorarioTranslator`: `23P01 → ConflictException(CONFLICTO_VIGENCIA_HORARIO)`.
- `HorarioOperacionRepository`: `findVigente`, `findVersionesQueIntersectan`,
  `bloquearVersionesQueIntersectan`, `findBySalonIdOrderByDiaSemana`, `deleteBySalonId`.
  **No existe** una query de historial completo ordenada por día + vigencia.

---

## 3. Inventario frontend (verificado, read-only)

Stack: React 19 + TypeScript + MUI v9 + axios + zustand + react-router. Build `tsc -b && vite build`,
lint `oxlint`. **Sin dependencias de test.**

### 3.1 `src/api/salones.ts`
`listarSalones`, `obtenerSalon(id)`, `crearSalon`, `actualizarSalon(id, SalonRequest)`,
`listarExcepcionesSalon`, `guardarExcepcionSalon`, `eliminarExcepcionSalon`. Cliente axios con
`baseURL = VITE_API_URL ?? 'http://localhost:8080/api'`, interceptor que inyecta el Bearer y otro
que limpia el token en 401.

### 3.2 `EditarHorarioSemanalDialog.tsx` (`src/pages/salones/components/`)

- Obtiene los horarios de `salon.horarios` (prop `SalonDetalleResponse`), normalizando con
  `.slice(0, 5)`.
- Estado local: array de 7 posiciones `HorarioOperacionRequest | null` indexado por `diaSemana`.
- Campos: `Switch` por día (activo/cerrado) + dos `TextField type="time"`.
- **No existe selector de fecha.** El cambio se asume inmediato.
- Guardar: **`actualizarSalon(salon.id, {...todo el salón..., horarios: nuevosHorarios})`** — el
  `PUT` destructivo que la cuarentena F2B.2 bloquea si algo cambió.
- Errores: `Alert severity="error"` con `err.response?.data?.message`.
- Refresh: `onGuardado(actualizado)` → el padre hace `setSalon(actualizado)` con la respuesta del
  `PUT`; no re-consulta.

### 3.3 `SalonHorarios.tsx` (pantalla contenedora)

`obtenerSalon(id)` al montar; permisos vía `usePermisos().tiene(...)`; el botón "Horario habitual"
se muestra con `salon.administrar` (`puedeAdministrarSalon`). Pasa `salon.horarios` a
`CalendarioHorariosInstructor` como rejilla de fondo. Ya tiene helpers locales `aIso(Date)` (fecha
local, no UTC — con comentario explicando por qué no `toISOString()`), `diaSemanaIndice`, `DIAS`.
Feedback por `Snackbar` + `Alert`.

### 3.4 `DialogoSalon.tsx` — **segundo consumidor, no previsto en el enunciado**

Wizard de 4 pasos: `['Información y ubicación', 'Actividades', 'Horarios de atención', 'Equipamiento']`.
El paso 2 es **editable también en modo edición** (mismo `Switch` + `TextField type="time"`), y al
guardar manda `horarios: horarios.filter(...)` dentro del `SalonRequest` a `crearSalon` o
`actualizarSalon`. Alta y edición comparten formulario.

### 3.5 Convenciones reutilizables

- Fechas: `TextField type="date"` con valor ISO `aaaa-mm-dd` (`VentaGestion.tsx`,
  `CalendarioHorariosInstructor.tsx`). **No hay date-picker de MUI X**; no se introduce.
- Errores: helper local `extraerMensajeError(err, porDefecto)` duplicado en varias páginas, siempre
  sobre `ApiErrorBody.message`. `ApiErrorBody` tiene índice `[key: string]: unknown`, así que un
  campo nuevo en el body **no rompe tipos**.

---

## 4. Principio de API

`PUT /api/salones/{id}` **no vuelve a ser** el canal de cambios reales de `HorarioOperacion`. Queda
para datos generales del salón y, transitoriamente, para payload de horarios idéntico (§27).

Los cambios de horario son **commands explícitos**: el cliente declara *qué operación temporal*
quiere (versionar / cerrar) y *desde cuándo*, no un estado semanal deseado. Un `PUT` de estado
completo no puede expresar `efectivoDesde` sin inventarlo, que es exactamente lo que la cuarentena
F2B.2 impide.

---

## 5. Endpoint Versionar — **DEFINIDO**

```
POST /api/salones/{salonId}/horarios/versiones
```

Coherente con `/api/salones/{salonId}/excepciones-horario`: sub-recurso anidado bajo el salón, en
plural, sin verbo en la URL. `versiones` es un sustantivo colectivo y `POST` sobre la colección
**crea** una versión, que es literalmente lo que hace el writer.

**Request** — `VersionarHorarioSalonRequest`:

```json
{
  "diaSemana": 1,
  "efectivoDesde": "2026-09-01",
  "horaApertura": "08:00",
  "horaCierre": "20:00"
}
```

```java
public record VersionarHorarioSalonRequest(
        @NotNull Short diaSemana,
        @NotNull LocalDate efectivoDesde,
        @NotNull LocalTime horaApertura,
        @NotNull LocalTime horaCierre) {
}
```

- `Short` **envuelto, no `short` primitivo** — igual que `HorarioOperacionRequest`: con primitivo,
  un `diaSemana` ausente se deserializa como `0` (domingo) y `@NotNull` no dispara nunca. Es un
  silencio peligroso, no un detalle de estilo.
- **No lleva `vigenteHasta`**: la continuidad la decide el command (append cierra la anterior en
  `D-1`, la nueva queda abierta). Aceptarlo permitiría al cliente describir historia.
- **No lleva id de versión**: no existe edición ni cancelación por versión (§21).
- El rango `0..6` **no** se valida con `@Min/@Max` en el DTO: lo valida el writer con
  `DIA_SEMANA_INVALIDO`, y duplicarlo produciría dos formatos de error distintos para el mismo
  fallo (uno con `fieldErrors`, otro con `codigo`). Bean Validation cubre sólo obligatoriedad.
- `LocalTime` acepta `"08:00"` y `"08:00:00"` (deserializador ISO por defecto de Jackson; el
  proyecto no configura `ObjectMapper`). El frontend puede enviar el valor nativo del input.

Mapea **exclusivamente** a `VersionarHorarioOperacion.ejecutar(...)`.

---

## 6. Endpoint Cerrar — **DEFINIDO**

```
POST /api/salones/{salonId}/horarios/cierres
```

**Request** — `CerrarHorarioSalonRequest`:

```json
{ "diaSemana": 1, "efectivoDesde": "2026-09-01" }
```

```java
public record CerrarHorarioSalonRequest(
        @NotNull Short diaSemana,
        @NotNull LocalDate efectivoDesde) {
}
```

### 6.1 Por qué `POST /cierres` y **no** `DELETE`

1. **No borra nada.** `CerrarHorarioOperacion` hace un `UPDATE` que pone `vigenteHasta = D-1`. Toda
   la historia previa sigue en la tabla y sigue siendo consultable (§7). `DELETE` prometería una
   desaparición que no ocurre.
2. **`DELETE` necesita un recurso identificable, y aquí no lo hay.** `DELETE /horarios/{diaSemana}`
   sugiere que el día es un recurso singular — precisamente el modelo `UNIQUE(salon, dia)` que V46
   retiró. `DELETE /versiones/{id}` sería cancelar una versión, que es el command **diferido** de
   §21 y tiene semántica distinta.
3. **`DELETE` no tiene cuerpo natural**, y `efectivoDesde` es obligatorio. Meterlo en query string
   convierte un dato de negocio en parámetro de URL para nada.
4. **El cierre es un hecho fechado, no una supresión.** "Dejar de operar los lunes a partir del
   1-sep" es un evento del historial. `POST` sobre la colección de cierres lo modela tal cual.

Mapea **exclusivamente** a `CerrarHorarioOperacion.ejecutar(...)`.

---

## 7. Historial — **DEFINIDO: SÍ, es necesario**

```
GET /api/salones/{salonId}/horarios/historial[?diaSemana=1]
```

### 7.1 Por qué no puede diferirse

- Con `efectivoDesde` futuro, `SalonDetalleResponse.horarios` (vigente **hoy**) **no cambia**. Tras
  un `201` la UI se vería idéntica: indistinguible de un fallo silencioso. El historial es la única
  forma de que el operador vea lo que acaba de programar.
- `CIERRE_CON_VERSIONES_FUTURAS` y `VERSIONADO_INTERMEDIO_NO_SOPORTADO` sólo son comprensibles si
  el operador **puede ver** esas versiones futuras. Sin historial, el 409 es un callejón sin salida
  (§20): el usuario no puede ni diagnosticar ni resolver.
- Es lectura pura sobre columnas existentes: coste bajo, sin migración, sin invariantes nuevas.

### 7.2 Contrato

- `diaSemana` opcional. Ausente ⇒ semana completa. **Si viene informado, debe estar en `0..6`**; un
  valor fuera de rango es **400**, nunca `200 []` (§7.4).
- Response `200` con `List<HorarioOperacionVersionResponse>` (§25), ordenada según §26.
- **Salón inexistente ⇒ `404`**, no `200 []`: el historial se direcciona por un salón concreto y
  "ese salón no existe" es un fallo de direccionamiento, exactamente el uso que el proyecto le da a
  `ResourceNotFoundException`. Aplica sólo a actores que pasan el scope (§7.4): un actor fuera de
  scope recibe **403** y el endpoint no funciona como oráculo de existencia.
- **Salón existente sin horarios (o día sin versiones) ⇒ `200 []`**, no 404: es el mismo criterio
  que `listarPorRango` de excepciones, que tampoco convierte "sin filas" en 404.

Los tres casos son **distintos y se testean por separado** (§32): 403 ≠ 404 ≠ `200 []`.

### 7.4 Orden de operaciones en la capa de aplicación — **OBLIGATORIO**

`SalonHorarioOperacionService.consultarHistorial(actorId, salonId, diaSemana)` ejecuta, en este
orden y sin excepciones:

```java
@Transactional(readOnly = true)
public List<HorarioOperacionVersionResponse> consultarHistorial(
        UUID actorId, UUID salonId, Short diaSemana) {

    // 1. scope: nada se lee antes de saber que el actor puede mirar este salón
    autorizadorSalon.verificarAccesoSalon(actorId, "salon.leer", salonId);

    // 2. validación sintáctica del filtro, si viene informado
    if (diaSemana != null && (diaSemana < 0 || diaSemana > 6)) {
        throw new ValidacionException(HorarioOperacionErrores.DIA_SEMANA_INVALIDO);
    }

    // 3. existencia del salón
    if (!salonRepository.existsById(salonId)) {
        throw new ResourceNotFoundException("Salón no encontrado");
    }

    // 4. consulta
    return horarioOperacionRepository.findVersionesOrdenadas(salonId, diaSemana).stream()
            .map(this::aVersionResponse)
            .toList();
}
```

Semántica resultante, **cerrada**:

| Situación | Resultado |
|---|---|
| Actor fuera de scope (salón ajeno) | **403** — y no revela si el salón existe |
| Actor con scope/global + salón inexistente | **404** |
| Actor con scope + salón existente sin horarios | **200 `[]`** |
| `diaSemana` fuera de `0..6` | **400** `DIA_SEMANA_INVALIDO` |

Justificación de cada posición:

1. **Autorización primero**, como `listarPorRango`/`guardar` (§2.2) y como exige F0E2. Un actor sin
   scope no debe provocar ni una lectura, ni distinguir salones existentes de inexistentes.
2. **Validar `diaSemana` antes de tocar la BD**: un filtro inválido no merece viaje a base de
   datos, y devolver `200 []` para `diaSemana=7` sería mentir — la lista está vacía porque la
   pregunta era inválida, no porque no haya horarios. Se reutiliza el código estable
   `DIA_SEMANA_INVALIDO` que ya emiten los writers para el mismo concepto: **un solo código para un
   solo error**, y el frontend ya lo tiene mapeado (§30). Va **después** del scope para que un
   actor ajeno reciba 403 y no 400 (el 400 confirmaría que llegó a la lógica del salón).
   No se usa `@Min/@Max` en el `@RequestParam` justamente por esto: Bean Validation se ejecutaría
   **antes** que la autorización y produciría 400 donde debe haber 403, además de un segundo
   formato de error (`HandlerMethodValidationException`, §2.5) para el mismo fallo.
3. **Existencia después de validar el filtro**: barata (`existsById`), y así un `diaSemana`
   inválido no depende de que el salón exista para dar 400.
4. **La query es lo último.** Prohibido consultar primero y deducir el 404 de una lista vacía: eso
   confundiría "salón inexistente" con "salón sin horarios", que son los dos casos que §7.2 separa.

**No se usa `SalonLock`** para la existencia: es `Propagation.MANDATORY` y toma `FOR UPDATE`, es
decir, un lock de escritura dentro de una transacción de escritura. El historial es lectura pura
(§24).

### 7.3 Sobre exponer el UUID de `HorarioOperacion` — **DECISIÓN: NO**

Ningún endpoint de esta fase **acepta** un id de versión: versionar y cerrar se direccionan por
`(salonId, diaSemana, efectivoDesde)`. Publicar el UUID sería publicar una dirección que no lleva a
ninguna operación, e invitaría a construir `DELETE /versiones/{id}` — el command diferido de §21 —
por la puerta de atrás. La clave natural de una versión es `(diaSemana, vigenteDesde)`, única por
el EXCLUDE `ex_horario_operacion_vigencia`, y le sirve al frontend como `key` de lista.

`HorarioOperacionResponse` (el legacy, **con** `id`) **no se toca**: lo consume
`SalonDetalleResponse` y el calendario del frontend.

---

## 8. Horario actual vs historial — separación **DEFINIDA**

`GET /api/salones/{id}` sigue devolviendo `SalonDetalleResponse.horarios` = configuración semanal
**vigente en la fecha de negocio**, sin vigencias y sin historia. **No se modifica** ese DTO ni
`SalonService.mapDetalle`.

| | vista actual | historial |
|---|---|---|
| endpoint | `GET /api/salones/{id}` | `GET /api/salones/{id}/horarios/historial` |
| DTO | `HorarioOperacionResponse` (con `id`, sin vigencias) | `HorarioOperacionVersionResponse` (sin `id`, con vigencias) |
| filas | ≤ 1 por día (vigente hoy) | N por día |
| consumidor | rejilla del calendario, `DialogoSalon` | diálogo de horario semanal |

Motivo: `CalendarioHorariosInstructor` recibe `salon.horarios` como rejilla de fondo de **una**
semana concreta. Convertirlo en historial obligaría a que cada consumidor resolviera vigencias por
su cuenta — exactamente lo que `HorarioOperacionResolver` centraliza en backend.

---

## 9. Resultado de Versionar — **201 Created + cuerpo**

`POST /api/salones` ya devuelve `201` con cuerpo (único sitio del proyecto que fija status
explícito). Versionar **siempre inserta una fila nueva**, así que `201` es literal, no decorativo.

```
HTTP/1.1 201 Created
Content-Type: application/json
```
```json
{ "diaSemana": 1, "horaApertura": "08:00", "horaCierre": "20:00",
  "vigenteDesde": "2026-09-01", "vigenteHasta": null }
```

- **Sin cabecera `Location`**: el proyecto no la emite en `POST /api/salones`, y además no hay URL
  canónica para una versión individual (§7.3). Inventarla sería inconsistente en dos frentes.
- El cuerpo devuelve **la versión creada**, no el salón: el frontend refresca el salón con
  `GET /api/salones/{id}` (§29), que es la fuente de verdad de "vigente hoy".
- La entidad que devuelve el writer queda **detached** al salir de su transacción. El mapeo a DTO
  toca sólo escalares ya cargados; **nunca `getSalon()`** (`FetchType.LAZY`, provocaría
  `LazyInitializationException` fuera de la transacción). El mapper vive en el service y sólo lee
  `diaSemana`, `horaApertura`, `horaCierre`, `vigenteDesde`, `vigenteHasta`.

---

## 10. Resultado de Cerrar — **200 OK + cuerpo**

Cerrar **no crea recurso**: hace `UPDATE` sobre una versión existente. `201` sería mentira y `204`
tiraría información útil. `200` con la representación del recurso modificado es lo que ya hace
`PUT /excepciones-horario`.

```json
{ "diaSemana": 1, "horaApertura": "08:00", "horaCierre": "20:00",
  "vigenteDesde": "2026-06-01", "vigenteHasta": "2026-08-31" }
```

Devuelve **la versión cerrada**, con su `vigenteHasta = efectivoDesde - 1` ya aplicado. Es la
confirmación exacta del efecto y evita que la UI tenga que recalcular `D-1` para el mensaje. Mismo
DTO que versionar e historial: un solo tipo para el concepto "versión de horario".

---

## 11. Códigos HTTP — **DEFINIDOS**

### 11.1 Situación de partida

Los writers lanzan `ValidacionException` para **todos** sus rechazos ⇒ hoy serían **400** sin
excepción. El único 409 existente es `CONFLICTO_VIGENCIA_HORARIO` (`ConflictException`).

### 11.2 Criterio

**400 = el request está mal formado o es inválido por sí mismo** (lo puede arreglar el cliente
cambiando el payload sin consultar el servidor). **409 = el request es válido pero choca con el
estado actual del historial o de la programación** (el cliente necesita mirar el estado para
resolverlo). Es el mismo criterio con el que `23P01` ya produce 409.

### 11.3 Tabla definitiva

| Situación / código estable | Excepción origen | HTTP | Justificación |
|---|---|---|---|
| Campo obligatorio ausente (`@NotNull`) | `MethodArgumentNotValidException` | **400** + `fieldErrors` | handler existente |
| JSON ilegible / fecha u hora no parseable | `HttpMessageNotReadableException` | **400** | handler **nuevo** (§11.6); `codigo: null` |
| `{salonId}` no UUID / `?diaSemana=abc` | `MethodArgumentTypeMismatchException` | **400** | handler **nuevo** (§11.6); `codigo: null` |
| `diaSemana` fuera de `0..6` en el historial | `ValidacionException(DIA_SEMANA_INVALIDO)` | **400** | validado en la capa de aplicación (§7.4) |
| `DIA_SEMANA_INVALIDO` | `ValidacionException` | **400** | input inválido en sí mismo |
| `HORA_CIERRE_DEBE_SER_POSTERIOR` | `ValidacionException` | **400** | input inválido en sí mismo |
| `EFECTIVO_DESDE_EN_EL_PASADO` | `ValidacionException` | **400** | input inválido contra el reloj, no contra el historial |
| `YA_EXISTE_VERSION_EN_ESA_FECHA` | `ValidacionException` | **409** | conflicto con el historial |
| `VERSIONADO_INTERMEDIO_NO_SOPORTADO` | `ValidacionException` | **409** | conflicto con el historial |
| `NO_EXISTE_VERSION_VIGENTE_EN_ESA_FECHA` | `ValidacionException` | **409** | ver §11.4 |
| `CANCELACION_DE_VERSION_NO_SOPORTADA` | `ValidacionException` | **409** | conflicto con el historial |
| `CIERRE_CON_VERSIONES_FUTURAS` | `ValidacionException` | **409** | conflicto con el historial |
| `PROGRAMACION_INCOMPATIBLE_CON_HORARIO` | `ValidacionException` | **409** | conflicto con programación existente |
| Cualquier **otra** `ValidacionException` | `ValidacionException` | **400** | **no** se traduce: la whitelist es explícita (§11.5) |
| `CONFLICTO_VIGENCIA_HORARIO` (23P01) | `ConflictException` | **409** | ya lo era; no cambia |
| Salón inexistente (versionar/cerrar) | `ResourceNotFoundException` (desde `SalonLock`) | **404** | handler existente |
| Salón inexistente (historial) | `ResourceNotFoundException` (desde `existsById`, §7.4) | **404** | handler existente |
| Cualquier otro fallo no previsto | `Exception` | **500** | handler genérico, **sin cambios** |
| Sin permiso funcional / fuera de scope | `AccessDeniedException` | **403** | handler existente (§14.3) |
| No autenticado | filtro JWT | **401** | `SecurityConfig` |

### 11.4 `NO_EXISTE_VERSION_VIGENTE_EN_ESA_FECHA`: **409, no 404**

El recurso direccionado por la URL (`/api/salones/{salonId}/horarios/cierres`) **existe**; el salón
existe (si no, ya habría 404 desde `SalonLock`). Lo que no existe es una versión vigente **en la
fecha que el cliente eligió** — un hecho del estado, no del direccionamiento. Devolver 404 haría
indistinguible "el salón no existe" (fallo de ruta, sesión rota, id mal copiado) de "ese día ya
está cerrado en esa fecha" (situación de negocio normal, resoluble cambiando la fecha). El proyecto
usa `ResourceNotFoundException` estrictamente para "entidad por id no encontrada"; se respeta.

### 11.5 Cómo se implementa el 409 **sin tocar los writers**

Tres piezas con responsabilidades **disjuntas**. La distinción es deliberada y es lo que evita el
diseño anterior con dos parsers:

| Pieza | Responsabilidad | Lo que **no** sabe |
|---|---|---|
| `CodigoErrorExtractor` (neutral) | `message → codigo \| null` | HTTP, `HorarioOperacion`, turnos, bloques, whitelists |
| Whitelist de `HorarioOperacion` (capa de aplicación) | `codigo → ¿es conflicto de estado?` | cómo se extrae el código, qué status HTTP se emite |
| `SalonHorarioOperacionService` | traducir `ValidacionException` whitelisted → `ConflictException` | qué status produce `ConflictException` (eso es del handler) |

**1. El extractor: UNO, neutral, y el único sitio con regex.**

```java
package com.feelingpilates.exception;

/**
 * Extrae el codigo estable de un mensaje de error con la convencion del proyecto
 * {@code "CODIGO: texto humano"}. Neutral: no conoce HTTP, ni modulos de dominio, ni
 * clasificaciones. Es el UNICO sitio del proyecto que parsea ese prefijo.
 */
public final class CodigoErrorExtractor {

    private static final Pattern CODIGO_ESTABLE = Pattern.compile("^([A-Z][A-Z0-9_]{2,}):");

    private CodigoErrorExtractor() {
    }

    /** @return el codigo estable, o {@code null} si el mensaje es null o no sigue la convencion. */
    public static String extraer(String mensaje) {
        if (mensaje == null) {
            return null;
        }
        Matcher m = CODIGO_ESTABLE.matcher(mensaje);
        return m.find() ? m.group(1) : null;
    }
}
```

Vive en `com.feelingpilates.exception` porque es donde ya viven `ErrorResponse`,
`ValidacionException` y `ConflictException`: paquete neutral que **ya** importan todos los módulos,
así que no crea acoplamiento nuevo en ninguna dirección.

**Consumidor A — capa de aplicación de horarios** (decide si un `ValidacionException` es 409):

```java
// HorarioOperacionErrores: añadido puro, junto a los codigos que ya define.
private static final Set<String> CONFLICTOS_DE_ESTADO = Set.of(
        CodigoErrorExtractor.extraer(YA_EXISTE_VERSION_EN_ESA_FECHA),
        CodigoErrorExtractor.extraer(VERSIONADO_INTERMEDIO_NO_SOPORTADO),
        CodigoErrorExtractor.extraer(NO_EXISTE_VERSION_VIGENTE_EN_ESA_FECHA),
        CodigoErrorExtractor.extraer(CANCELACION_DE_VERSION_NO_SOPORTADA),
        CodigoErrorExtractor.extraer(CIERRE_CON_VERSIONES_FUTURAS),
        CodigoErrorExtractor.extraer(PROGRAMACION_INCOMPATIBLE_CON_HORARIO));

/** true si el mensaje corresponde a un choque con el estado (historial o programacion). */
public static boolean esConflictoDeEstado(String mensaje) {
    String codigo = CodigoErrorExtractor.extraer(mensaje);
    return codigo != null && CONFLICTOS_DE_ESTADO.contains(codigo);
}
```

**Consumidor B — `GlobalExceptionHandler`** (llena `ErrorResponse.codigo`, §12): la misma llamada,
`CodigoErrorExtractor.extraer(message)`, dentro de `build(...)`. **Ni una segunda regex, ni un
segundo parsing, ni una segunda convención.**

**2. La traducción, en el service y sólo para estos endpoints:**

```java
private <T> T traduciendoConflictosDeEstado(Supplier<T> operacion) {
    try {
        return operacion.get();
    } catch (ValidacionException e) {
        if (HorarioOperacionErrores.esConflictoDeEstado(e.getMessage())) {
            throw new ConflictException(e.getMessage());   // mismo mensaje, mismo codigo
        }
        throw e;                                           // el resto sigue siendo 400
    }
}
```

**La whitelist es cerrada y explícita.** Los seis códigos de arriba son 409; **cualquier otra**
`ValidacionException` — de estos writers, de `SalonService`, de donde sea — **sigue siendo 400**.
No existe ninguna regla del tipo "`ValidacionException` → 409": eso convertiría en conflicto
errores de input y rompería endpoints ajenos. El test 9 de §32 existe precisamente para fijar esa
frontera.

**Se comparan códigos, no mensajes completos**, así que el mapeo sobrevive a cualquier reescritura
del texto humano. Nótese que `PROGRAMACION_INCOMPATIBLE_CON_HORARIO` se lanza como
`CONSTANTE + ": " + conflictos`, es decir el mensaje real lleva **dos** `:`; el extractor toma el
prefijo hasta el **primero**, que es el código. Comportamiento correcto y verificado contra
`HorarioOperacionErrores.verificarSinImpacto`.

**Por qué la traducción vive aquí y no en `GlobalExceptionHandler`**: un `@ExceptionHandler` global
que mirase el prefijo de toda `ValidacionException` cambiaría el status de endpoints ajenos a esta
fase. La traducción local sólo afecta a los dos endpoints nuevos.

**Por qué no en los writers**: §35. Cambiar el tipo de excepción que lanzan alteraría el contrato
que F2B.3b.1 ya validó con tests
(`assertThatThrownBy(...).isInstanceOf(ValidacionException.class)`).

**Por qué el extractor no clasifica**: extraer es `message → codigo`; clasificar es
`codigo → 400/409`. Son dos preguntas distintas, y la segunda depende del módulo (para horarios,
`CIERRE_CON_VERSIONES_FUTURAS` es 409; otro módulo podría tener otra política para sus códigos).
Si el extractor conociera la whitelist, el paquete `exception` acabaría importando conceptos de
`ubicaciones`, y cada módulo nuevo tendría que editarlo. Por eso el extractor es tonto a propósito.

### 11.6 Request malformado → **400** (cierre de la contradicción)

**Problema que se cierra:** la API nueva promete 400 para JSON/fecha/hora mal formada, pero
`GlobalExceptionHandler` no tiene handler para esas excepciones y su `@ExceptionHandler(Exception.class)`
las convierte en **500** (§2.5). Prometer 400 y devolver 500 es una contradicción de contrato, no
una deuda cosmética: `"efectivoDesde": "01/09/2026"` daría hoy 500.

**Alcance del arreglo — el mínimo necesario, derivado del stack real** (Spring Boot 4.1.0 / Spring
Framework 7.0.8), **no** de una lista supuesta:

```java
@ExceptionHandler(HttpMessageNotReadableException.class)
public ResponseEntity<ErrorResponse> handleNoLegible(
        HttpMessageNotReadableException ex, HttpServletRequest request) {
    return build(HttpStatus.BAD_REQUEST, "Solicitud mal formada", request, null);
}

@ExceptionHandler(MethodArgumentTypeMismatchException.class)
public ResponseEntity<ErrorResponse> handleTipoInvalido(
        MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
    return build(HttpStatus.BAD_REQUEST, "Parámetro con formato inválido: " + ex.getName(), request, null);
}
```

Reglas que acotan el cambio:

- **Sólo estas dos.** `MethodArgumentNotValidException` ya tiene handler (400 + `fieldErrors`) y no
  se toca. `HandlerMethodValidationException` y `jakarta.validation.ConstraintViolationException`
  **no son alcanzables** en este proyecto (§2.5) y **no se les añade handler**: añadir handlers para
  excepciones que nadie lanza es contrato muerto que después nadie sabe si puede borrar.
- **El handler genérico no se modifica.** Sigue devolviendo **500** para todo lo inesperado. Este
  cambio **resta** casos del genérico; no cambia su comportamiento.
- **No se convierten a 400 excepciones arbitrarias de aplicación.** Nada de mapear
  `IllegalArgumentException`, `DateTimeParseException` suelta ni `RuntimeException`: sólo las dos
  excepciones de **binding/deserialización de Spring MVC**, que por definición significan "el
  cliente mandó algo que no se puede leer".
- **Mensaje fijo, nunca `ex.getMessage()`.** El mensaje de `HttpMessageNotReadableException` incluye
  detalles de Jackson (clases, offsets, nombres de campo internos); publicarlo filtraría estructura
  interna y sería ruido para el operador. `MethodArgumentTypeMismatchException` sí aporta
  `ex.getName()` — el nombre del parámetro, que es dato público de la API.
- **`codigo: null`.** Ninguno de los dos mensajes lleva prefijo `CODIGO:`, así que
  `CodigoErrorExtractor.extraer` devuelve `null` y el frontend cae a su fallback (§30). Es
  coherente: no son errores de negocio, son requests ilegibles.
- **Sin riesgo de regresión sobre lo existente**: `grep -rn "isInternalServerError\|is(500)"
  src/test/java` no devuelve **ninguna** ocurrencia, es decir ningún test del proyecto depende hoy
  de que un request malformado dé 500. El cambio no rompe la suite; los tests 1 y 2 de §32 la
  amplían.

**Beneficio colateral aceptado, no buscado:** el arreglo vive en el handler global, así que **todos**
los endpoints del proyecto pasan de 500 a 400 ante un request ilegible. Es la corrección de un
defecto, no una ampliación de alcance: no hay forma de arreglarlo sólo para tres rutas sin duplicar
el handler por controller, que sería peor. Se hace **primero y solo** en la secuencia de §44 para
que cualquier efecto se detecte antes de montar los endpoints encima.

---

## 12. Contrato de error — **REUTILIZADO + un campo aditivo**

Contrato definitivo del cuerpo de error, desde el punto de vista del cliente:

```json
{
  "message": "...",
  "codigo": "..." | null
}
```

(`timestamp`, `status`, `error`, `path` y `fieldErrors` siguen exactamente como hoy; se listan
aparte para no dar a entender que desaparecen.)

```java
public record ErrorResponse(
        Instant timestamp, int status, String error, String message,
        String path, Map<String, String> fieldErrors,
        String codigo) {          // NUEVO: codigo estable, o null
}
```

**Es un cambio estrictamente aditivo:**

- **`message` no cambia de contenido.** Sigue siendo el mensaje completo actual, **con el prefijo
  `CODIGO:` incluido**. Retirar el prefijo del `message` en F2B.3b.2a rompería a todo cliente que
  hoy lo muestre o lo compare, y no aporta nada: el campo `codigo` ya da la vía limpia. Una
  eventual limpieza del prefijo sería una fase propia y posterior, con clientes ya migrados a
  `codigo`.
- **`codigo` es nuevo y nullable.** `null` cuando el mensaje no sigue la convención (mensajes
  humanos sueltos, "Ocurrió un error inesperado", "Solicitud mal formada", "Error de validación").
- Ningún cliente existente rompe por un campo extra en JSON. `ApiErrorBody` del frontend ya declara
  índice `[key: string]: unknown`, así que ni siquiera rompe la compilación TS antes de declararlo.

Poblado en **un único sitio**, `GlobalExceptionHandler.build(...)`, delegando en el **único**
extractor del proyecto (§11.5) — sin regex propia, sin parsing propio:

```java
private ResponseEntity<ErrorResponse> build(HttpStatus status, String message,
                                            HttpServletRequest request, Map<String, String> fieldErrors) {
    ErrorResponse body = new ErrorResponse(
            Instant.now(), status.value(), status.getReasonPhrase(), message,
            rutaSinSecretos(request.getRequestURI()), fieldErrors,
            CodigoErrorExtractor.extraer(message));   // unica fuente de codigo
    return ResponseEntity.status(status).body(body);
}
```

**Por qué es el cambio mínimo:**

- **Cero formatos nuevos.** No se inventa otro JSON de error para horarios; se extiende el único
  que hay.
- **Un solo punto de construcción** en todo el proyecto (verificado por grep: sólo
  `GlobalExceptionHandler.build`). No hay que tocar ningún service ni pasar el código a mano.
- **Un solo extractor** (§11.5), compartido con la whitelist de 409. La convención "el código es el
  prefijo del mensaje" pasa de acuerdo tácito a contrato explícito **implementado una vez**.
- **No duplica información**: el código ya viaja hoy dentro de `message`; sólo se publica también
  en un campo propio para que el cliente no tenga que partir cadenas.
- **Beneficio inmediato en toda la app**: `HORARIOS_REQUIEREN_VERSIONADO`,
  `CONFLICTO_VIGENCIA_HORARIO` y cualquier código futuro lo obtienen sin trabajo extra.

El frontend distingue entonces **`codigo` (estable, para lógica)** de **`message` (humano, para
fallback)**, sin parsear texto libre (§30).

---

## 13. Seguridad — **DEFINIDA**

| Endpoint | `@PreAuthorize` |
|---|---|
| `POST …/horarios/versiones` | `hasAuthority('salon.administrar')` |
| `POST …/horarios/cierres` | `hasAuthority('salon.administrar')` |
| `GET …/horarios/historial` | `hasAuthority('salon.leer')` |

Derivación, no suposición:

- `PUT /api/salones/{id}` (la edición de salón que hoy incluye horarios) exige `salon.administrar`.
  Los endpoints nuevos **no pueden pedir menos**, y no hay razón para pedir más.
- `PUT/DELETE /excepciones-horario` — el otro camino de escritura sobre el horario de un salón —
  también exige `salon.administrar`.
- **No se asume "sólo ADMIN"**: el proyecto autoriza por *permiso funcional*, no por nombre de rol.
  `AutorizadorSalon` trata `ADMIN`/`SUPER_ADMIN` como roles de alcance global, pero el permiso se
  evalúa siempre por código. Un rol de sede con `salon.administrar` sobre su salón puede operar sus
  propios horarios, que es el comportamiento correcto.
- **No se crea permiso nuevo** (`salon.horario.administrar` o similar): exigiría migración de
  catálogo (§36 lo prohíbe) y dejaría a los roles existentes sin él, rompiendo a usuarios que hoy
  pueden editar horarios.
- `GET historial` usa `salon.leer` porque su descripción de catálogo ya dice "Ver detalle de
  salones, espacios, **horarios** y catálogos".

---

## 14. Scope por salón — **DEFINIDO** (crítico)

### 14.1 Mecanismo existente

`AutorizadorSalon.verificarAccesoSalon(actorId, permiso, salonId)`: relee el usuario de BD,
comprueba que esté `activo`, que **alguna asignación `UsuarioRol`** conceda el permiso, y que **esa
misma asignación** aplique al salón objetivo (salón propio, o rol global `ADMIN`/`SUPER_ADMIN` con
`salon == null`). Permiso y alcance **no se mezclan entre roles distintos**. Lanza
`AccessDeniedException` → **403**.

### 14.2 Dónde ocurre

**En la capa de aplicación (`SalonHorarioOperacionService`), como primera sentencia del método**,
antes de cualquier lectura, lock o llamada al writer. Es exactamente lo que hace
`SalonHorarioExcepcionService.guardar/listarPorRango`.

```java
public HorarioOperacionVersionResponse versionar(UUID actorId, UUID salonId, VersionarHorarioSalonRequest req) {
    autorizadorSalon.verificarAccesoSalon(actorId, "salon.administrar", salonId);
    ...
}
```

- **No en el controller**: `@PreAuthorize` no conoce el `salonId` de BD y el proyecto ya decidió que
  el scope contextual vive en el service (F0E2).
- **No en el writer**: §15.
- **Nunca `salonRepository.findById(salonId)` antes del scope.** La existencia del salón la resuelve
  `SalonLock.adquirir` **dentro** del writer, ya con el scope verificado.

El controller obtiene el actor con `@AuthenticationPrincipal UsuarioAutenticado actor` y pasa
`actor.id()`, igual que `SalonHorarioExcepcionController`.

### 14.3 Orden 403 antes que 404 — decisión explícita

El scope se verifica **antes** de saber si el salón existe. Un actor con alcance de sede que pruebe
un UUID ajeno recibe **403**, no 404: el endpoint no funciona como oráculo de existencia de salones
fuera de su alcance. Es el mismo orden que ya tiene `SalonHorarioExcepcionService.guardar`. Un actor
con rol global recibe 404 para un UUID inexistente, que es correcto porque para él no hay nada que
ocultar.

### 14.4 Observación registrada (no se corrige aquí)

`GET /api/salones/{id}` (`SalonService.obtenerDetalle`) exige `salon.leer` pero **no aplica
`AutorizadorSalon`**: cualquier usuario con ese permiso puede leer el detalle de **cualquier** salón.
Es preexistente y ajeno a esta fase. Se registra en §43 como **PUEDE ESPERAR**; los endpoints nuevos
**sí** aplican scope, incluido el `GET historial`, y por tanto no amplían el hueco.

---

## 15. El writer no conoce autenticación — **RATIFICADO**

`VersionarHorarioOperacion` y `CerrarHorarioOperacion` **no** reciben ni consultan `SecurityContext`,
JWT, roles, `actorId` ni nada de HTTP. Sus records de comando (`VersionarHorario`, `CerrarHorario`)
se quedan **exactamente como están** — su Javadoc ya dice "Comando interno: no es un DTO HTTP".

La cadena queda:

```
SalonHorarioOperacionController   HTTP, @PreAuthorize, @Valid, status
        │  actor.id(), salonId, request
        ▼
SalonHorarioOperacionService      scope (AutorizadorSalon), mapeo DTO↔comando,
        │                         traducción de conflictos a ConflictException
        ▼
VersionarHorarioOperacion / CerrarHorarioOperacion    dominio: lock, clasificación,
                                                      Política A, persistencia
```

Cada capa hace una cosa y sólo una.

---

## 16. Idempotencia / doble click — **DEFINIDO: sin infraestructura de idempotencia**

Comportamiento real ante doble envío idéntico de versionar:

1. La primera petición inserta `D/NULL` y devuelve `201`.
2. La segunda entra al writer, `clasificar` encuentra una versión con `vigenteDesde == D` y lanza
   **`YA_EXISTE_VERSION_EN_ESA_FECHA`** → **409**. **No duplica nada, no corrompe nada**: el
   rechazo ocurre antes de cualquier escritura.

Doble envío de cerrar: la primera pone `vigenteHasta = D-1`; la segunda ya no encuentra versión
vigente en `D` y lanza **`NO_EXISTE_VERSION_VIGENTE_EN_ESA_FECHA`** → **409**. También seguro.

**Decisión: no se introduce `Idempotency-Key` ni tabla de deduplicación.** Justificación:

- La invariante ya está protegida en la capa correcta (lock de `Salon` + clasificación + EXCLUDE).
  Una capa de idempotencia HTTP no añadiría seguridad, sólo cambiaría el mensaje de error.
- Es una operación **administrativa de baja frecuencia** (un operador, un salón, un día).
- Infraestructura genérica de idempotencia es una decisión de plataforma, no de esta fase (§16 lo
  prohíbe explícitamente).

**Mitigación en UI:** el botón se deshabilita mientras `guardando` (patrón ya usado en
`EditarHorarioSemanalDialog` y `DialogoSalon`), y el 409 se muestra con mensaje propio por `codigo`
+ el refresco de recuperación del historial (§30 para el mensaje, §29.2 para el refresco), para que
el operador vea que su cambio **sí** se aplicó. No es un tratamiento de éxito.

---

## 17. `efectivoDesde` en la UI — **DEFINIDO**

La UI **deja de asumir** que editar horario = aplicar ahora. Toda operación de horario pide fecha.

- **Control**: `TextField type="date"`, valor ISO `aaaa-mm-dd`, con `slotProps.inputLabel.shrink`,
  igual que `VentaGestion.tsx` y `CalendarioHorariosInstructor.tsx`. **No** se añade MUI X Date
  Pickers (dependencia nueva, fuera de alcance).
- **Default**: **hoy**, calculado con el helper local `aIso(new Date())` que ya existe en
  `SalonHorarios.tsx` (fecha **local**, no `toISOString()`, que adelanta el día en husos detrás de
  UTC). Se extrae a `src/pages/salones/fechas.ts` para compartirlo con el diálogo en vez de
  duplicarlo.
- **Mínimo**: `min = hoy` en el input (§31: sólo UX; el backend sigue siendo la autoridad).
- **Etiqueta**: "Aplicar a partir de" + texto de ayuda **neutral**: *"El cambio se aplicará a partir
  de esa fecha. Consulta el historial para ver la vigencia de los horarios."*
  **Prohibido** el texto que esta revisión retira —*"El horario anterior se conserva en el historial
  hasta el día previo"*— y cualquier variante que presente esa continuidad como **regla universal**.
  Sólo es cierta en el **append** sobre un borde abierto. Si el día está cerrado hoy, o si se
  reabre después de un gap, **no existe** ningún horario anterior vigente hasta D−1. El diálogo no
  sabe —ni debe inferir— en cuál de los tres casos está (§29): la vigencia real la explica el
  historial (§21.1).
- **Advertencia de futuro**: si el historial del día ya tiene versiones con `vigenteDesde > hoy`, se
  muestra un `Alert severity="warning"` antes de guardar: *"Este día ya tiene un cambio programado
  para el <fecha>. Sólo se puede programar a partir de la última versión."* No bloquea el botón: la
  autoridad es el backend, que responde `VERSIONADO_INTERMEDIO_NO_SOPORTADO`.
- **Formato/locale**: el input nativo muestra el formato del navegador y entrega ISO; los textos de
  confirmación usan `toLocaleDateString('es-MX', …)`, como ya hace `SalonHorarios.tsx`.

⚠ **Limitación conocida** (§43): `RelojConfig` usa `Clock.systemDefaultZone()` y el proyecto no
declara zona horaria de negocio. Si la zona del navegador y la del servidor difieren, "hoy" puede no
coincidir cerca de medianoche y una fecha aceptada por el input puede rechazarse con
`EFECTIVO_DESDE_EN_EL_PASADO`. Por eso ese código **tiene mensaje propio en el frontend** en vez de
tratarse como imposible. Fijar zona de negocio es una decisión aparte, fuera de esta fase.

---

## 18. Edición de un día — **DEFINIDO**

El diálogo pasa de "editar la semana entera y enviarla" a **una operación por día**.

**Lista (estado por defecto)** — 7 filas, cada una con:
- nombre del día;
- horario **vigente hoy** (de `salon.horarios`) o "Cerrado" si el día no aparece;
- acciones (sólo con `salon.administrar`):
  - día abierto → **"Cambiar horario"** y **"Dejar de operar"**;
  - día cerrado → **"Abrir este día"** (es también un versionar: alta/reapertura);
- indicador "Cambio programado para el <fecha>" si el historial tiene versiones futuras de ese día.

**Formulario de versionar** (un día, `diaSemana` **fijo** y no editable):
`horaApertura`, `horaCierre`, `efectivoDesde` → `POST …/horarios/versiones`.

**Nunca se envía la semana completa**, y **nunca se toca `PUT /api/salones/{id}`** desde este
diálogo. Se elimina el import de `actualizarSalon` en `EditarHorarioSemanalDialog.tsx`; esa ausencia
es en sí misma la comprobación de la migración (§34, caso 1).

**Decisión: una operación por envío.** Si el operador quiere cambiar tres días, hace tres
operaciones. No se introduce endpoint por lotes ni se encadenan N peticiones tras un botón único:
un lote parcialmente fallido (día 1 OK, día 2 en conflicto) dejaría un estado a medias imposible de
explicar y sin atomicidad real, porque cada writer commitea en su propia transacción.

---

## 19. Cerrar un día — **DEFINIDO**

Acción explícita **"Dejar de operar este día"**, con el mismo tono que la UI actual ("Horario
habitual", "Día cancelado").

Formulario: **sólo `efectivoDesde`** (default hoy, min hoy) + confirmación en línea:
*"El salón dejará de abrir los <día> a partir del <fecha>. Las fechas anteriores se conservan en el
historial."* → `POST …/horarios/cierres`.

**No se modela como:**
- `horaApertura == horaCierre` — el writer lo rechaza (`HORA_CIERRE_DEBE_SER_POSTERIOR`) y sería un
  estado sin sentido;
- borrar la fila — no es lo que hace el command (§6.1);
- el `Switch` actual — un toggle esconde una operación temporal irreversible-en-apariencia detrás de
  un gesto de un clic, sin fecha. **El `Switch` desaparece del diálogo.**

Si el día ya está cerrado hoy, la acción no se ofrece (sólo "Abrir este día").

---

## 20. Versiones futuras — **UX definida, comando diferido**

Ante `409` con `codigo = CIERRE_CON_VERSIONES_FUTURAS`, el frontend muestra:

> **No se puede cerrar este día todavía.** Hay un cambio de horario ya programado para más adelante.
> Revisa el historial del día y resuélvelo primero.

y **abre/desplaza el foco al historial de ese día**, para que el operador vea la versión futura.

**Limitación documentada**: en F2B.3b.2 **no hay forma de cancelar esa versión futura desde la UI**.
El command está diferido (§21). Si el operador necesita deshacerla, la única salida en esta fase es
esperar o intervenir en base de datos, y el mensaje **no debe prometer** una acción que no existe
(nada de "Cancelar programación"). Esto es un **argumento explícito** para priorizar el command de
cancelación en una fase posterior; se registra en §43 como PUEDE ESPERAR pero con impacto de UX real.

Lo mismo aplica a `VERSIONADO_INTERMEDIO_NO_SOPORTADO`.

---

## 21. Cancelación de versiones — **NO SE IMPLEMENTA**

Sin `DELETE /versiones/{id}`, sin endpoint de cancelación, sin edición in-place de una versión, sin
reescritura de historia. Los commands correspondientes siguen diferidos. El historial es
**estrictamente read-only**: se renderiza como lista sin acciones. Es también la razón de no
publicar el UUID de versión (§7.3).

### 21.1 Panel de historial en el frontend — **read-only, y qué debe dejar entender**

El panel (dentro de `EditarHorarioSemanalDialog`, o adyacente a él en `SalonHorarios`) consume
`GET …/horarios/historial` y **sólo pinta**. Debe permitir al operador entender, sin conocimientos
del modelo temporal:

| Debe quedar claro | Cómo se representa |
|---|---|
| **La historia** del día | Filas en orden cronológico (§26), cada una `<horaApertura>–<horaCierre>`, con su rango de vigencia |
| **Qué versión está vigente hoy** | Marca visual (`Chip` "Vigente") en la fila cuyo rango contiene hoy |
| **Qué versiones son futuras** | Marca "Programada" en las filas con `vigenteDesde > hoy` |
| **Los gaps** (periodos cerrados) | Fila sintética *"Cerrado"* entre dos versiones cuando `vigenteHasta` de una y `vigenteDesde` de la siguiente no son consecutivos, y también al final si la última versión está cerrada (`vigenteHasta != null`) |
| **Por qué puede salir `CIERRE_CON_VERSIONES_FUTURAS`** | Las filas "Programada" son la explicación visible: cerrar no reorganiza lo que ya está planificado (§20) |
| Extremos abiertos | `vigenteDesde: null` → "Desde el inicio"; `vigenteHasta: null` → "Sin fecha de fin" (§25) |

- Las filas sintéticas de gap son **presentación derivada de los datos recibidos**, no una
  interpretación de negocio ni una petición extra: se calculan comparando fechas consecutivas de la
  lista que el backend ya ordenó. No se inventan versiones ni se rellenan huecos con horarios.
- **Sin ninguna acción**: no hay editar versión, no hay cancelar versión, no hay borrar versión, no
  hay menú contextual, no hay selección. Ni siquiera deshabilitadas: un botón gris invita a pedir
  la función; su ausencia es la señal correcta mientras el command no exista (§20, §21).
- Se refresca tras cada operación con éxito (§29.1) —**siempre, esté visible o no**— y al abrir el
  panel. Nunca se muta en local.

---

## 22. Programación incompatible — **UX definida**

Cuando `Turnos recurrentes` o `Bloques` impiden el cambio, la Política A lanza
`PROGRAMACION_INCOMPATIBLE_CON_HORARIO` → **409**. El mensaje del backend incluye tipo e IDs
(`TURNO_RECURRENTE[uuid detalle], BLOQUE_PROGRAMACION[uuid …]`) — diagnóstico para logs, **no un DTO
HTTP** (así lo dice el Javadoc de `HorarioOperacionErrores`).

**Decisión: el frontend muestra sólo el mensaje mapeado por `codigo` y NO renderiza el `message`
crudo:**

> **No se puede aplicar el cambio.** Hay clases o bloques programados que quedarían fuera del nuevo
> horario. Ajusta primero esa programación en el calendario del salón.

- No se listan UUIDs: el operador no tiene forma útil de resolver un UUID en pantalla, y §22 prohíbe
  exponer detalles técnicos del port.
- **No se enriquece el error** con un DTO de conflictos (nombres, fechas, enlaces): eso convertiría
  F2B.3b.2 en un gestor de conflictos y obligaría a que `ubicaciones` conociera `calendario` y
  `programacion`, rompiendo el aislamiento de módulos que F2B.3b.0 preservó.
- El `message` completo, con IDs, sigue llegando en la respuesta y se registra en el log del
  servidor: la diagnosticabilidad no se pierde, sólo no se pinta.

Enriquecer el conflicto es candidato a fase propia (§43, PUEDE ESPERAR).

---

## 23. Transacciones — **DEFINIDO**

- **Controller sin `@Transactional`.** Sólo HTTP.
- **`SalonHorarioOperacionService` sin `@Transactional` de clase** en los métodos de escritura. El
  writer ya es `@Transactional` (`REQUIRED`) y abre la suya; `SalonLock` es
  `Propagation.MANDATORY` y vive dentro de ella.
- ⚠ **Prohibido marcar el service `@Transactional(readOnly = true)` a nivel de clase**: el writer
  se uniría a esa transacción de sólo lectura y `saveAndFlush` fallaría. Si el método de historial
  necesita `readOnly`, se anota **sólo ese método**.
- `AutorizadorSalon.verificarAccesoSalon` es `@Transactional(readOnly = true)` y se ejecuta en su
  propia transacción antes del writer. Correcto y deliberado: la autorización no debe compartir
  transacción con la escritura.
- **No se replica ninguna validación de dominio** en controller ni service: ni rango de día, ni
  orden de horas, ni fecha pasada, ni clasificación temporal. Bean Validation cubre sólo
  obligatoriedad (§5).

---

## 24. Query de historial — **DEFINIDA**

Añadido a `HorarioOperacionRepository` (repositorio existente; **no** es migración ni cambio de
esquema):

```java
@Query(value = """
        select h.*
        from horario_operacion h
        where h.salon_id = :salonId
          and (cast(:diaSemana as smallint) is null or h.dia_semana = :diaSemana)
        order by h.dia_semana asc, h.vigente_desde asc nulls first
        """, nativeQuery = true)
List<HorarioOperacion> findVersionesOrdenadas(
        @Param("salonId") UUID salonId,
        @Param("diaSemana") Short diaSemana);
```

- **`cast(:diaSemana as smallint) is null`**: mismo patrón que el `cast(:hasta as date) is null` ya
  presente en `findVersionesQueIntersectan`; PostgreSQL no puede inferir el tipo de un parámetro
  nulo. Parámetro `Short` (envuelto) para poder ser `null`.
- **`nulls first`** explícito: en PostgreSQL, `ASC` pone `NULL` al final por defecto, y la fila
  legada `vigente_desde = NULL` es `-∞` y debe ir **primera** (§26).
- **Sin `for update`**: lectura pura, sin lock (§24 lo exige y `SalonLock` sería inaplicable fuera
  de transacción de escritura).
- **No sustituye ni modifica** `findVersionesQueIntersectan` ni `bloquearVersionesQueIntersectan`:
  aquellas filtran por intersección con un rango; ésta devuelve **toda** la historia, incluido el
  pasado cerrado, que es justo lo que ninguna query existente hace.
- **Sin invariantes nuevas**: no comprueba solapes ni unicidad; eso lo garantizan el EXCLUDE y los
  writers.

---

## 25. DTO de historial / versión — **DEFINIDO**

```java
public record HorarioOperacionVersionResponse(
        short diaSemana,
        LocalTime horaApertura,
        LocalTime horaCierre,
        LocalDate vigenteDesde,
        LocalDate vigenteHasta) {
}
```

TypeScript:

```ts
export interface HorarioOperacionVersionResponse {
  diaSemana: number;
  horaApertura: string;
  horaCierre: string;
  vigenteDesde: string | null;
  vigenteHasta: string | null;
}
```

Ejemplos (fila legada partida por un versionado):

```json
[
  { "diaSemana": 1, "horaApertura": "08:00", "horaCierre": "20:00",
    "vigenteDesde": null, "vigenteHasta": "2026-08-31" },
  { "diaSemana": 1, "horaApertura": "09:00", "horaCierre": "20:00",
    "vigenteDesde": "2026-09-01", "vigenteHasta": null }
]
```

- **`null` se preserva como `null`.** Nada de `"0001-01-01"`, `"9999-12-31"` ni `""`. `null` es
  `-∞`/`+∞` y es semánticamente distinto de cualquier fecha concreta; un sentinel obligaría a cada
  consumidor a conocer la convención y reintroduciría por la puerta de atrás el bug de comparar
  fechas mágicas.
- El frontend renderiza `vigenteDesde: null` como **"Desde el inicio"** y `vigenteHasta: null` como
  **"Sin fecha de fin"** — presentación, no transformación del dato.
- **Mismo DTO** para respuesta de versionar (§9), de cerrar (§10) y para cada elemento del historial
  (§7). Un solo tipo para un solo concepto.
- **Sin `id`** (§7.3). **Sin `salonId`**: ya está en la URL.

---

## 26. Orden del historial — **DEFINIDO**

- **Semana completa** (`?diaSemana` ausente): `dia_semana ASC`, luego `vigente_desde ASC NULLS FIRST`.
- **Filtrado por día**: `vigente_desde ASC NULLS FIRST` (el orden por día es trivial). Es el mismo
  `ORDER BY`; no hacen falta dos queries.

Cronológico ascendente dentro de cada día, con la fila legada `NULL` (`-∞`) primera. Coincide con el
orden que ya usan `findVersionesQueIntersectan` / `bloquearVersionesQueIntersectan`, así que backend
y UI leen la historia en la misma dirección. El orden lo garantiza SQL, **no** el service: reordenar
en Java sobre `LocalDate` nulos exigiría un comparador con `nullsFirst` y sería una segunda fuente
de verdad.

---

## 27. `SalonService` legacy y el `PUT` general — **DEFINIDO: se mantiene la tolerancia**

### 27.1 Estado actual de la cuarentena (F2B.2)

`SalonService.validarHorariosSinCambios`:

| `horarios` en el request | comportamiento |
|---|---|
| `null` | no se tocan los horarios; el resto del salón sí se actualiza |
| equivalente a la configuración semanal efectiva **hoy** | no-op; el resto se actualiza |
| distinto (incluida lista vacía sobre salón con horarios) | `ValidacionException(HORARIOS_REQUIEREN_VERSIONADO)` → 400 |

La comparación es semántica y por tanto independiente del orden.

### 27.2 Decisión

**NO se retira la tolerancia de payload idéntico en F2B.3b.2.** Razones:

1. **Retirarla convertiría un despliegue de backend en un despliegue acoplado**: cualquier cliente
   antiguo (pestaña abierta, caché de bundle, app móvil) seguiría enviando la semana entera en el
   `PUT` y pasaría de "funciona" a "no puedo ni cambiar el teléfono". §39 lo prohíbe. **Éste es el
   único motivo por el que la tolerancia sigue viva: compatibilidad hacia atrás.**
2. **No aporta seguridad retirarla**: el payload idéntico ya es un no-op verificado; lo que protege
   la invariante es el rechazo del payload **distinto**, que se mantiene intacto.
3. **El frontend nuevo NO se apoya en ella.** En edición envía `horarios: null` (§27.3.2), que es
   una vía distinta y explícita; en alta envía la lista, pero eso es `POST /api/salones` →
   `crearHorariosIniciales`, que no es versionado y no tiene historia que preservar. Es decir: tras
   F2B.3b.2b **ningún cliente propio depende de la equivalencia semántica**, sólo los antiguos.

### 27.3 `DialogoSalon` — **CREAR vs EDITAR, diferencia explícita**

El wizard comparte formulario entre alta y edición, y ahí es donde se cuelan los dos defectos de
§1. La diferencia entre modos queda **explícita**:

| | **CREAR** (`salon == null` → `POST /api/salones`) | **EDITAR** (`salon != null` → `PUT /api/salones/{id}`) |
|---|---|---|
| Paso "Horarios de atención" | **editable**, sin cambios respecto de hoy | **read-only**: muestra el horario vigente + enlace a la pantalla de horarios del salón |
| Validación *"Activa al menos un día de atención."* (`errorDelPaso(2)`) | **se conserva** | **se OMITE** |
| `horarios` en el payload | **se envían** los días activados | **`horarios: null`** (§27.3.2) |
| Efecto backend | `crearHorariosIniciales` (no es versionado; no hay historia que preservar) | `validarHorariosSinCambios` retorna de inmediato: **no toca `HorarioOperacion`** |

#### 27.3.1 Por qué la validación se omite al editar

`errorDelPaso(2)` devuelve *"Activa al menos un día de atención."* cuando los siete días están
cerrados, y `handleSubmit` **la vuelve a ejecutar para los pasos 0..3 antes de guardar**, también
en modo edición. Consecuencia real hoy: **un salón cerrado los siete días no puede guardar ni un
cambio de teléfono**, porque el wizard se niega antes de llegar al `PUT`.

Con horarios versionados, "cerrado los siete días" **es un estado alcanzable y legítimo**: basta
cerrar cada día con `POST …/horarios/cierres`, o que una reapertura futura aún no haya entrado en
vigor. La validación es correcta en **alta** (crear un salón que no abre nunca no tiene sentido) y
es un **bloqueo indebido** en **edición**, donde el paso ya no edita horarios en absoluto.

Regla: en modo edición, `errorDelPaso(2)` devuelve siempre `null`. **Editar teléfono, nombre,
dirección, actividades o equipamiento debe funcionar aunque el salón esté cerrado los siete días.**

#### 27.3.2 Payload de edición: `horarios: null` — **DECISIÓN DEFINITIVA**

En modo **EDITAR**, el `PUT` envía `horarios: null`. **No** se reenvía la fotografía de horarios
cargada al abrir el modal.

- **El snapshot puede quedar obsoleto.** El diálogo carga `salon.horarios` al abrirse (vigente
  *en ese instante*). Si mientras está abierto entra en vigor una versión programada — o alguien
  versiona desde la otra pantalla — el snapshot deja de coincidir con la configuración efectiva y
  `validarHorariosSinCambios` responde `HORARIOS_REQUIEREN_VERSIONADO` → 400, con el operador
  incapaz de entender por qué no puede guardar su teléfono. La ventana es pequeña pero real, y con
  fechas de vigencia deja de ser hipotética.
- **`horarios == null` es contrato F2B.2 ya existente y verificado**: `validarHorariosSinCambios`
  hace `if (horarios == null) return;` — no toca `HorarioOperacion` y actualiza el resto del salón.
  Es la vía diseñada para "no quiero tocar horarios", y no depende de ninguna comparación.
- **No se depende de la equivalencia semántica** para este frontend nuevo. La tolerancia de payload
  idéntico (§27.2) permanece **únicamente como compatibilidad hacia atrás para clientes antiguos**;
  el cliente nuevo no la usa. Un cliente que se apoya en "mándalo igual y no pasa nada" depende de
  que el estado no cambie entre lectura y escritura, que es justo lo que el versionado rompe.
- Requiere un ajuste de tipo en el frontend: `SalonRequest.horarios` pasa de
  `HorarioOperacionRequest[]` a `HorarioOperacionRequest[] | null` (§28). El backend ya lo acepta:
  `SalonRequest.horarios` es una `List` sin `@NotNull`.

En modo **CREAR** no aplica nada de lo anterior: se sigue enviando la lista de días activados.

### 27.4 Retirada futura

Fase posterior y separada (candidata: **F2B.3b.3**), cuando se verifique en producción que ningún
cliente envía `horarios` con contenido en el `PUT`. Entonces `SalonRequest.horarios` puede pasar a
ignorarse en actualización (o rechazarse siempre). **Fuera de alcance aquí.**

### 27.5 Javadoc a corregir (§38)

F2B.3b.2a **sí toca `SalonService`**, sólo documentación, sin cambio de comportamiento:

1. El Javadoc de `HORARIOS_REQUIEREN_VERSIONADO` afirma *"todavía no existe el writer de versionado
   (F2B.3b)"* — **falso** desde F2B.3b.1 y doblemente falso al existir endpoint. Debe decir que el
   versionado existe y vive en `POST /api/salones/{salonId}/horarios/versiones`.
2. El Javadoc de `horariosVigentes` afirma *"hoy lo impide el UNIQUE(salon_id, dia_semana)"* —
   retirado en V46 (pendiente P2 del review de F2B.3b.1). Debe decir que la unicidad de versión
   vigente la garantiza el EXCLUDE `ex_horario_operacion_vigencia`.

Como el archivo se modifica, se corrigen **los dos**. **No** se hace limpieza general de Javadoc en
otros archivos.

---

## 28. Servicio de API en el frontend — **DEFINIDO**

Se añaden tres funciones a `src/api/salones.ts` (mismo módulo que ya alberga las de excepciones;
funciones **independientes**, sin reutilizar `SalonRequest` ni `actualizarSalon`):

```ts
export function versionarHorarioSalon(salonId: string, request: VersionarHorarioSalonRequest) {
  return apiClient
    .post<HorarioOperacionVersionResponse>(`/salones/${salonId}/horarios/versiones`, request)
    .then((res) => res.data);
}

export function cerrarHorarioSalon(salonId: string, request: CerrarHorarioSalonRequest) {
  return apiClient
    .post<HorarioOperacionVersionResponse>(`/salones/${salonId}/horarios/cierres`, request)
    .then((res) => res.data);
}

export function obtenerHistorialHorarios(salonId: string, diaSemana?: number) {
  return apiClient
    .get<HorarioOperacionVersionResponse[]>(`/salones/${salonId}/horarios/historial`, {
      params: diaSemana === undefined ? undefined : { diaSemana },
    })
    .then((res) => res.data);
}
```

Tipos nuevos en `src/api/types.ts`: `VersionarHorarioSalonRequest`, `CerrarHorarioSalonRequest`,
`HorarioOperacionVersionResponse`. `ApiErrorBody` gana `codigo?: string` (§12).
`HorarioOperacionRequest`/`HorarioOperacionResponse` **no se tocan**.

Único cambio en `SalonRequest`: `horarios: HorarioOperacionRequest[]` →
`horarios: HorarioOperacionRequest[] | null`, para poder enviar `null` en edición (§27.3.2). El
backend ya lo admite (`SalonRequest.horarios` es una `List` sin `@NotNull`), y `tsc -b` señalará
cualquier consumidor que asumiera no-null.

---

## 29. Flujo de guardado en el frontend — **DEFINIDO**

Orden estricto, sin excepciones:

1. Validación local mínima (campos presentes, cierre > apertura, fecha ≥ hoy) — sólo para evitar
   viajes obviamente inútiles.
2. `setGuardando(true)`; el botón queda deshabilitado (§16).
3. `await versionarHorarioSalon(...)` / `await cerrarHorarioSalon(...)`.
4. **Sólo si resuelve**: `await onAplicado()`, que en `SalonHorarios` hace
   `const actualizado = await obtenerSalon(id); setSalon(actualizado);`
5. **Siempre, sin condición**: `await obtenerHistorialHorarios(id)` y refrescar el panel de
   historial — **también cuando el panel no está visible** en ese momento (§29.1).
6. Volver a la lista de días (o cerrar el diálogo si el operador terminó); `Snackbar` de éxito.
7. `setGuardando(false)` en `finally`.

**Prohibido el estado optimista**: nada de `setHorarios(nuevos)` antes de la respuesta. Con
`PROGRAMACION_INCOMPATIBLE_CON_HORARIO` o `CIERRE_CON_VERSIONES_FUTURAS` la UI mostraría un horario
que el backend rechazó.

**Cambio respecto de hoy**: `onGuardado(salonActualizado)` desaparece. El endpoint devuelve una
*versión*, no un `SalonDetalleResponse`, así que el padre **re-consulta** el salón en vez de
asignar la respuesta. Es además más correcto: sólo el backend sabe qué versión está vigente hoy.

**Mensaje de éxito, sensible a la fecha** — necesario porque con fecha futura la pantalla no cambia:

- `efectivoDesde == hoy` → "Horario actualizado."
- `efectivoDesde > hoy` → **"Cambio programado para el <fecha>."** — y nada más.

**Prohibido cualquier mensaje equivalente a "el horario actual seguirá vigente hasta D−1".** Es
falso en un caso perfectamente normal: si el día está **cerrado** hoy (cierre previo, o gap entre
versiones) y se versiona con fecha futura, no hay ningún "horario actual" que siga vigente hasta
D−1 — la operación es una **reapertura**, y entre hoy y D−1 el salón sigue cerrado. Afirmar
continuidad sería mentirle al operador justo en el caso que más le cuesta entender.

**Tampoco se intenta inferir "append" vs "reapertura" a partir del response de Versionar.** El
cuerpo devuelve la versión **creada** (§9); no dice si había una versión previa contigua, si se
cerró en D−1 o si venía de un hueco. Deducirlo obligaría a reimplementar en el frontend la
clasificación temporal del writer — exactamente lo que §31 prohíbe — y a acertar en el borde donde
más fácil es equivocarse.

**Después del éxito, siempre**: refrescar el **detalle** del salón (paso 4) **y el historial**
(paso 5), y dejar que el historial explique la situación temporal real (§21.1) — gaps incluidos.
La UI no narra la historia: la muestra.

### 29.1 Refresco tras éxito — **una sola regla, sin condiciones**

Tras **cada** `Versionar` o `Cerrar` con éxito, y en este orden:

1. refrescar el **detalle del salón** (`obtenerSalon(id)`);
2. refrescar el **historial de horarios** (`obtenerHistorialHorarios(id)`);
3. **después** actualizar o cerrar el modal según la UX (volver a la lista de días, `Snackbar`).

Los **dos** refrescos ocurren **siempre**. Queda **eliminada** cualquier regla del tipo *"refrescar
el historial sólo si el panel está visible"*: era una condición de rendimiento disfrazada de regla
de sincronización, y contradecía el "siempre" de esta misma sección.

Por qué incondicional:

- **Evita estado temporal obsoleto.** El historial cacheado en memoria queda desfasado en el
  instante de la mutación, esté o no pintado.
- **Una única regla de sincronización.** "Siempre los dos" se puede verificar y revisar; "depende de
  lo que esté abierto" produce dos caminos y un borde donde nadie mira.
- **Permite mostrar de inmediato** versiones futuras, reaperturas y gaps: exactamente los casos en
  que la rejilla del calendario **no** cambia y el historial es la única evidencia del efecto.
- **Evita que abrir el panel después** muestre datos anteriores a la mutación, que es el escenario
  donde el operador concluye que su cambio no se aplicó.

**Sin caching sofisticado y sin actualización optimista.** No hay invalidación selectiva, ni claves
de caché, ni `staleTime`: dos `GET` tras cada operación con éxito. Es una pantalla administrativa de
baja frecuencia (§16); el coste es irrelevante frente a la ambigüedad que elimina.

**En error, nada de esto se ejecuta como éxito** (§29.2).

### 29.2 Flujo de error — **sin cambios respecto de F2B.3b.2.0.1**

Si `Versionar` o `Cerrar` **falla**:

- **no** se modifica el estado local como si hubiera habido éxito;
- **no** se muestra mensaje ni `Snackbar` de éxito;
- **no** se asume ningún cambio temporal (ni vigencia, ni cierre, ni programación);
- se muestra el error mapeado por `codigo` (§30);
- se conservan los datos ya cargados. Un refresco sólo procede si el propio diseño ya lo exige como
  **recuperación** —el caso de §16: `YA_EXISTE_VERSION_EN_ESA_FECHA` refresca el historial para que
  el operador vea que su cambio sí se aplicó—, **nunca** como tratamiento de éxito.

---

## 30. Errores en el frontend — **DEFINIDO**

Mapa por **`codigo`** (nunca por texto libre ni por status):

| `codigo` | Mensaje al operador |
|---|---|
| `EFECTIVO_DESDE_EN_EL_PASADO` | "La fecha debe ser hoy o posterior." |
| `HORA_CIERRE_DEBE_SER_POSTERIOR` | "La hora de cierre debe ser posterior a la de apertura." |
| `DIA_SEMANA_INVALIDO` | "Día de la semana inválido." *(no alcanzable desde la UI; red de seguridad)* |
| `YA_EXISTE_VERSION_EN_ESA_FECHA` | "Ya hay un cambio de horario que empieza ese día. Revisa el historial." |
| `VERSIONADO_INTERMEDIO_NO_SOPORTADO` | "Este día ya tiene un cambio programado. Sólo se puede programar a partir de la última versión." |
| `NO_EXISTE_VERSION_VIGENTE_EN_ESA_FECHA` | "Ese día no está abierto en la fecha indicada." |
| `CANCELACION_DE_VERSION_NO_SOPORTADA` | "Ese cambio de horario empieza justo ese día; deshacerlo aún no está disponible. Elige otra fecha." |
| `CIERRE_CON_VERSIONES_FUTURAS` | §20 |
| `PROGRAMACION_INCOMPATIBLE_CON_HORARIO` | §22 |
| `CONFLICTO_VIGENCIA_HORARIO` | "Otro cambio se guardó al mismo tiempo. Vuelve a intentarlo." |
| `HORARIOS_REQUIEREN_VERSIONADO` | "Los horarios se cambian desde la pantalla de horarios del salón." *(red durante la transición)* |

Reglas:

- **Fallback**: `codigo` desconocido o ausente → `message` del backend; si tampoco hay → texto
  genérico ("No se pudo guardar el horario."). Siempre hay mensaje.
- **403** → "No tienes permiso para cambiar el horario de este salón." **404** → "El salón ya no
  existe." **401** lo maneja el interceptor existente.
- **Nunca** se muestra stacktrace ni el campo `error`/`path`. `ErrorResponse` no expone stacktrace.
- El helper de mapeo vive en un módulo propio (p. ej. `src/pages/salones/erroresHorario.ts`), no
  inline en el componente, para poder ejercitarlo en el plan de verificación (§34).

---

## 31. "Hoy" en el frontend — **DEFINIDO**

El frontend **no replica** ninguna regla de dominio: ni clasificación temporal, ni detección de
solapes, ni Política A, ni qué versión contiene una fecha. Sólo:

- pone `min = hoy` en el input de fecha (UX, evita un viaje seguro al fallo);
- avisa si ve versiones futuras en el historial (§17), sin bloquear.

El backend es la autoridad: `EFECTIVO_DESDE_EN_EL_PASADO` **debe** estar mapeado (§30) porque es
alcanzable — desfase de zona horaria (§17), pestaña abierta desde ayer, reloj del cliente mal
puesto. Un frontend que asuma que ese error es imposible mostraría un fallback genérico justo cuando
más falta hace el mensaje concreto.

---

## 32. Tests backend — controller/aplicación

`SalonHorarioOperacionControllerTest`, slice `@WebMvcTest` + `@Import({SecurityConfig.class,
AutorizadorSalon.class, SalonHorarioOperacionService.class, GlobalExceptionHandler.class, …})`,
autenticando con `SecurityMockMvcRequestPostProcessors.authentication(...)` y
`UsuarioAutenticado` como principal — exactamente el montaje de
`AutorizacionContextualControllerTest` y `ReservaControllerSecurityTest`.

Los writers se sustituyen por **dobles** que devuelven una `HorarioOperacion` o lanzan la
`ValidacionException` del código bajo prueba: aquí se verifica **la traducción HTTP**, no el
dominio.

| # | Escenario | Esperado |
|---|---|---|
| 1 | POST versiones, `salon.administrar` + scope OK | **201**, cuerpo con `vigenteDesde`/`vigenteHasta`, sin `id` |
| 2 | POST cierres, autorizado | **200**, cuerpo con `vigenteHasta = efectivoDesde − 1` |
| 3 | POST versiones sin `efectivoDesde` | **400** + `fieldErrors.efectivoDesde` |
| 4 | POST versiones sin `diaSemana` | **400** + `fieldErrors.diaSemana` *(regresión del `Short` vs `short`, §5)* |
| 5 | Autenticado sin `salon.administrar` | **403**, writer **no invocado** |
| 6 | Con permiso pero salón fuera de scope | **403**, writer **no invocado** |
| 7 | GET historial con sólo `salon.leer` | **200** |
| 8 | GET historial sin `salon.leer` | **403** |
| 9 | `PROGRAMACION_INCOMPATIBLE_CON_HORARIO` | **409**, `codigo` presente, IDs en `message` |
| 10 | `CIERRE_CON_VERSIONES_FUTURAS` | **409** |
| 11 | `CANCELACION_DE_VERSION_NO_SOPORTADA` | **409** |
| 12 | `NO_EXISTE_VERSION_VIGENTE_EN_ESA_FECHA` | **409** (§11.4) |
| 13 | `YA_EXISTE_VERSION_EN_ESA_FECHA` | **409** |
| 14 | `VERSIONADO_INTERMEDIO_NO_SOPORTADO` | **409** |
| 15 | `ConflictException(CONFLICTO_VIGENCIA_HORARIO)` (23P01 ya traducido) | **409** |
| 16 | `EFECTIVO_DESDE_EN_EL_PASADO` | **400** *(prueba de que la traducción a 409 no es indiscriminada)* |
| 17 | `HORA_CIERRE_DEBE_SER_POSTERIOR` | **400** |
| 18 | `ResourceNotFoundException` desde `SalonLock` | **404** |
| 19 | Todos los anteriores con cuerpo de error | `codigo` == prefijo esperado, `message` conservado |
| 20 | **JSON inválido** (`{"diaSemana":` sin cerrar) | **400**, `codigo: null` *(§11.6; hoy 500)* |
| 21 | **Fecha no parseable** (`"efectivoDesde": "01/09/2026"`) | **400**, `codigo: null` *(§11.6)* |
| 22 | **Hora no parseable** (`"horaApertura": "8am"`) | **400**, `codigo: null` *(§11.6)* |
| 23 | `{salonId}` que no es UUID | **400**, `codigo: null` *(§11.6)* |
| 24 | `GET historial?diaSemana=7` | **400** `DIA_SEMANA_INVALIDO` *(§7.4; nunca `200 []`)* |
| 25 | `GET historial?diaSemana=-1` | **400** `DIA_SEMANA_INVALIDO` |
| 26 | `GET historial?diaSemana=abc` | **400**, `codigo: null` *(type mismatch, §11.6)* |
| 27 | **Historial, actor autorizado (scope o global) + salón inexistente** | **404**, writer/query **no invocados** más allá de `existsById` |
| 28 | **Historial, salón existente sin ninguna fila** | **200 `[]`** *(≠ caso 27)* |
| 29 | **Historial, actor fuera de scope** | **403**, `existsById` **no invocado** *(≠ casos 27 y 28)* |
| 30 | `ValidacionException` **desconocida** (p. ej. `"OTRA_COSA: texto"` o mensaje sin prefijo) desde el writer | **400** — **NO** se convierte en 409 *(frontera de la whitelist, §11.5)* |

Los casos **27, 28 y 29** se escriben como tres tests separados y explícitos: son los tres
resultados que §7.2 distingue (403 / 404 / `200 []`) y confundirlos es precisamente el defecto que
esta revisión cierra. El caso 29 asserta además que **no se llega a consultar existencia**, para
que el endpoint no funcione como oráculo de salones ajenos.

El caso **30** es la contraparte del 16/17: juntos fijan que la traducción a 409 usa una
**whitelist cerrada** y que ninguna `ValidacionException` se convierte en conflicto por defecto.

**No se re-testea** la concurrencia de F2B.3b.1 a nivel controller: locks, orden de flush y `23P01`
real ya están cubiertos por `HorarioOperacionConcurrenciaTest` y
`HorarioOperacionWritersPersistenciaTest`. El caso 15 sólo comprueba el mapeo del status.

**Tests adicionales del contrato de error**, para no dejar sin red ni el campo nuevo ni el
extractor:

`CodigoErrorExtractorTest` (unitario, sin Spring):

| Entrada | `extraer(...)` |
|---|---|
| `"CIERRE_CON_VERSIONES_FUTURAS: existen versiones…"` | `"CIERRE_CON_VERSIONES_FUTURAS"` |
| `"PROGRAMACION_INCOMPATIBLE_CON_HORARIO: …: TURNO_RECURRENTE[…]"` (dos `:`) | `"PROGRAMACION_INCOMPATIBLE_CON_HORARIO"` — prefijo hasta el **primer** `:` |
| `"Salón no encontrado"` (sin prefijo) | `null` |
| `"minusculas: texto"` / `"AB: texto"` (código demasiado corto) | `null` |
| `null` | `null` |

`GlobalExceptionHandler` (slice):

- `ErrorResponse.message` **conservado íntegro, con su prefijo**, y `codigo` extraído (§12).
- Error sin código estable (`"Ocurrió un error inesperado"`, `"Solicitud mal formada"`,
  `"Error de validación"`) → **`codigo == null`**, `message` intacto.
- `message == null` → `codigo == null`, sin `NullPointerException`.
- **Un endpoint ajeno a horarios sigue devolviendo el mismo status y `message` que antes**
  (no-regresión del contrato global), incluida una `ValidacionException` cualquiera → **400**.

`HorarioOperacionErroresTest` (unitario): `esConflictoDeEstado` devuelve `true` para los seis
códigos de la whitelist, y `false` para `EFECTIVO_DESDE_EN_EL_PASADO`, `DIA_SEMANA_INVALIDO`,
`HORA_CIERRE_DEBE_SER_POSTERIOR`, un código inventado y un mensaje sin prefijo.

---

## 33. Tests de historial (PostgreSQL real)

`SalonHorarioOperacionHistorialPersistenciaTest`, con `TestcontainersConfiguration` (patrón de
`HorarioOperacionWritersPersistenciaTest`), esquema V46 completo:

1. **Legacy + versión nueva**: fila `NULL/NULL`, se ejecuta `VersionarHorarioOperacion` con `D`
   futuro, `GET historial` devuelve **dos** elementos del día, en orden: `(null, D−1)` y
   `(D, null)`. **Los `null` se preservan** en el JSON — se asserta sobre el cuerpo serializado, no
   sólo sobre las entidades.
2. **Orden semana completa**: varios días con varias versiones ⇒ `dia_semana ASC`, y dentro de cada
   día `vigente_desde ASC` con la fila `NULL` **primera**.
3. **Filtro por día**: `?diaSemana=1` devuelve sólo las versiones del lunes, mismo orden.
4. **Aislamiento entre salones**: dos salones con horarios; el historial de A **no** contiene ni una
   fila de B.
5. **Scope**: actor con alcance sólo sobre B pide el historial de A → **403**, y el cuerpo no revela
   nada de A.
6. **Salón sin horarios / día sin versiones** → **200 `[]`** (no 404), y **salón inexistente** →
   **404** (no `200 []`), como dos aserciones distintas del mismo test de contorno (§7.2).
7. **Cierre reflejado**: tras `CerrarHorarioOperacion`, el historial muestra la versión con
   `vigenteHasta = D−1` y **ninguna versión abierta** de ese día.

---

## 34. Tests frontend — **no hay stack; plan manual + red de compilación**

`package.json` no declara vitest, jest, @testing-library ni playwright. §34 prohíbe inventar
framework, así que **F2B.3b.2b no introduce uno**.

### 34.1 Red automatizada disponible

- `npm run build` (`tsc -b && vite build`) — atrapa el grueso de la migración: quitar
  `horarios: HorarioOperacionRequest[]` del flujo del diálogo, tipos nuevos, `codigo?: string`.
- `npm run lint` (`oxlint`) — imports muertos, incluido `actualizarSalon` si quedara sin uso.

### 34.2 Verificación manual reproducible

Preparación: backend en local con Postgres, sesión con `salon.administrar` sobre el salón de prueba,
DevTools → Network abierto. Salón con horario `Lunes 08:00–20:00`.

| # | Caso (§34) | Pasos | Resultado esperado |
|---|---|---|---|
| 1 | Editar horario NO usa `PUT /salones` | Horarios → "Horario habitual" → Lunes → Cambiar → `09:00–20:00`, fecha hoy → Guardar | **Una** petición: `POST /api/salones/{id}/horarios/versiones` → **201**. **Cero** `PUT /api/salones/{id}` |
| 2 | Se envía `efectivoDesde` | Misma petición, pestaña Payload | Cuerpo con `diaSemana`, `efectivoDesde`, `horaApertura`, `horaCierre`. Sin `vigenteHasta`, sin `id`, sin lista semanal |
| 3 | Cerrar usa el endpoint de cierre | Lunes → "Dejar de operar" → fecha hoy → Confirmar | `POST …/horarios/cierres` → **200**. Ningún `DELETE`, ningún `PUT` |
| 4 | 409 no muta la UI como éxito | Crear un turno recurrente el lunes 08:00–09:00; luego versionar lunes `10:00–20:00` desde hoy | **409**; `Alert` con el texto de §22; la fila del lunes **sigue mostrando el horario anterior**; sin `Snackbar` de éxito |
| 5 | El teléfono sigue usando `PUT` | Salones → Editar salón → cambiar teléfono → Guardar | `PUT /api/salones/{id}` → **200**; teléfono actualizado |
| 6 | Editar horario no depende de la semana | Payload del caso 1 | Sin campo `horarios`, sin días distintos del editado |
| 7 | Refresco tras éxito | Repetir caso 1 | Tras el `201`, **siempre las dos peticiones**: `GET /api/salones/{id}` **y** `GET …/horarios/historial`, **aunque el panel de historial esté cerrado** (§29.1). La rejilla del calendario muestra el nuevo horario |
| 8 | Fecha futura | Versionar lunes `07:00–20:00` con fecha **hoy + 7** | **201**; mensaje "Cambio programado para el …"; la rejilla **no** cambia; el historial muestra la versión futura |
| 9 | Cierre bloqueado por futuro | Con la versión futura del caso 8, "Dejar de operar" el lunes desde hoy | **409** `CIERRE_CON_VERSIONES_FUTURAS`; mensaje de §20; el foco se desplaza al historial del día, que queda a la vista (§20). Sin `Snackbar` de éxito y sin cambio en la rejilla (§29.2) |
| 10 | Doble click | Versionar y pulsar Guardar dos veces rápido | Botón deshabilitado tras el primer clic; si llegara la segunda, **409** `YA_EXISTE_VERSION_EN_ESA_FECHA` con su mensaje; **una sola** versión en el historial |
| 11 | Paso de horarios read-only | Editar salón → paso "Horarios de atención" | Campos no editables + enlace a la pantalla de horarios; al guardar, `PUT` → **200** con `horarios: null` en el cuerpo (§27.3.2), **no** una lista |
| 12 | Fecha pasada | Escribir manualmente `efectivoDesde` de ayer | Backend **400** `EFECTIVO_DESDE_EN_EL_PASADO`; mensaje de §30 (no genérico) |
| 13 | **`DialogoSalon` EDIT con cero días abiertos** | Cerrar los 7 días del salón (7 × "Dejar de operar", fecha hoy) → Salones → Editar salón → cambiar teléfono → Guardar | **Se guarda**: `PUT /api/salones/{id}` → **200**, teléfono actualizado. **No** aparece "Activa al menos un día de atención." (§27.3.1) |
| 14 | **`DialogoSalon` EDIT envía `horarios: null`** | Editar salón → Guardar → pestaña Payload | El cuerpo contiene `"horarios": null`. **No** contiene una lista de días (§27.3.2) |
| 15 | **Reapertura: mensaje neutral** | Cerrar el lunes desde hoy; después "Abrir este día" con fecha **hoy + 7** | **201**; el mensaje dice exactamente **"Cambio programado para el \<fecha\>"** y **no** afirma que ningún horario siga vigente hasta D−1 (§29). El historial muestra el gap: cerrado desde hoy hasta D−1 |
| 16 | **El historial explica el estado** | Con el caso 15 aplicado, abrir el historial del lunes | Se ven: versión pasada cerrada, tramo **"Cerrado"** (gap), y versión **"Programada"**. Ninguna acción de editar/cancelar/borrar (§21.1) |

### 34.3 Recomendación registrada

Introducir **Vitest + @testing-library/react** en fase propia, con los casos 1–4 y 7 como primera
suite. **PUEDE ESPERAR** (§43): no bloquea F2B.3b.2b, pero cada fase que toque este diálogo sin red
automatizada acumula riesgo.

---

## 35. Writers: **NO SE MODIFICAN**

`VersionarHorarioOperacion`, `CerrarHorarioOperacion`, `SalonLock`,
`ConflictoVigenciaHorarioTranslator`, `ValidadorImpactoCambioHorarioOperacion` y sus adapters,
`HorarioOperacionResolver`, `HorarioEfectivoSalon`: **sin cambios**. Los records `VersionarHorario`
y `CerrarHorario` siguen siendo comandos internos.

Único añadido en `HorarioOperacionErrores`: **un** método estático de consulta,
`esConflictoDeEstado`, más el `Set` de códigos que usa — y ambos delegan la extracción en
`CodigoErrorExtractor` (§11.5), sin regex ni parsing propios. No modifican constantes, no alteran
`verificarSinImpacto`, no cambian qué lanza ningún writer ni cuándo. Es contract mapping, admitido
por §35.

**El diseño no exige ningún cambio de semántica de los writers.** Si durante F2B.3b.2a apareciera
uno, **DETENERSE** y volver a diseño.

---

## 36. Migraciones: **NINGUNA**

Sin V47. Sin cambios de esquema. V44/V45/V46 intactas. El historial usa exclusivamente
`salon_id`, `dia_semana`, `hora_apertura`, `hora_cierre`, `vigente_desde`, `vigente_hasta`, que ya
existen.

**Ningún índice nuevo**, y la justificación **no** se apoya en ninguna propiedad de la FK.
Corrección de una afirmación falsa de F2B.3b.2.0: *"la FK ya está indexada"* es **incorrecto** —
**PostgreSQL no crea índice automáticamente al definir una FOREIGN KEY** (sólo lo hace para PRIMARY
KEY y UNIQUE). Evidencia real del esquema:

- V11 declara `salon_id UUID NOT NULL REFERENCES salon (id)`: **sin índice propio**.
- V11 declaraba también `UNIQUE (salon_id, dia_semana)`, cuyo índice implícito sí cubría
  `salon_id`… pero **V46 retiró esa constraint**, y con ella su índice.
- Lo que queda cubriendo la columna es el índice de exclusión que crea V45:
  `ex_horario_operacion_vigencia`, `EXCLUDE USING gist (salon_id WITH =, dia_semana WITH =,
  daterange(vigente_desde, vigente_hasta, '[]') WITH &&)`. PostgreSQL materializa esa constraint
  **como un índice GiST** cuya primera clave es `salon_id` (posible gracias a `btree_gist`, V44).

Ésa es la única evidencia que se invoca. Se documenta como estructura **existente**, no como
garantía de plan de ejecución: un GiST no es un btree y no se afirma que el planner lo elija.
**No se crea índice nuevo en F2B.3b.2a sin evidencia** — y no la hay: `findVersionesOrdenadas`
recorre decenas de filas por salón, un volumen en el que incluso un seq scan es irrelevante. Si
alguna vez apareciera evidencia real (un `EXPLAIN ANALYZE` sobre datos de producción), un
`CREATE INDEX` sería una migración propia, con su medición.

**No se añade permiso al catálogo** (§13), que también habría exigido migración.

---

## 37. Reservas: **INTACTAS**

No se toca `Reserva`, `Sesion`, capacidad ni materialización. Los endpoints nuevos no las leen ni
las escriben. El único acoplamiento con `calendario`/`programacion` es la Política A **ya existente**
dentro de los writers, que sólo **consulta** para rechazar.

---

## 38. Javadoc P2 — se corrige (§27.5)

F2B.3b.2a modifica `SalonService` sólo en documentación (cuarentena + `horariosVigentes`). Sin
limpieza general de Javadoc en otros archivos.

---

## 39. Rollout — **DEFINIDO**

**A. Backend (F2B.3b.2a)** — desplegable solo, sin frontend nuevo.
   - 3 endpoints, DTOs, service de aplicación, `findVersionesOrdenadas`, `CodigoErrorExtractor`,
     mapeo de conflictos por whitelist, `codigo` en `ErrorResponse`, handlers de request malformado
     (§11.6), Javadoc, tests.
   - `PUT /api/salones/{id}` **sin cambios de comportamiento**: sigue tolerando payload idéntico.
   - Efecto sobre el frontend actual: **ninguno**. Nadie llama a los endpoints nuevos todavía, el
     campo `codigo` es aditivo, y el único cambio observable en endpoints existentes es que un
     request ilegible pasa de 500 a 400 — de error falso a error correcto.
   - **No queda bloqueada por el repositorio del frontend** (§45): se implementa, revisa y despliega
     de forma independiente.

**B. Repositorio Git autoritativo de `web`** — prerrequisito **operativo** de C, no de A.
   - Crear/conectar el repositorio propio del frontend y establecer un baseline limpio (§45).
   - No hay nada que implementar aquí en términos de producto; es trabajo de source control.

**C. Frontend (F2B.3b.2b)** — requiere A desplegado **y** B resuelto.
   - `EditarHorarioSemanalDialog` reescrito (§18, §19), panel de historial read-only (§21.1),
     `SalonHorarios` re-consulta el salón (§29), `DialogoSalon` CREAR vs EDITAR (§27.3), servicio de
     API (§28), mapa de errores (§30), tipos.

**D. Verificación en el entorno real.**
   - Un cambio de horario end-to-end (hoy y con fecha futura), un cierre, una reapertura tras gap,
     y un 409 provocado.
   - Confirmar en logs de acceso que llegan `POST …/horarios/versiones` y que **dejan de llegar**
     `PUT /api/salones/{id}` con horarios distintos.
   - Confirmar que `HORARIOS_REQUIEREN_VERSIONADO` no aparece en logs (si aparece, queda un cliente
     sin migrar).

**E. Retirada de compatibilidad legacy** — fase separada y posterior (§27.4), **si se decide**, y
   sólo si D confirma que ningún cliente depende de ella.

**Nunca A y C en el orden inverso**, y **nunca** un despliegue que primero rompa el frontend
existente. **Backend-first sigue siendo seguro**: A es inerte para los clientes actuales.

---

## 40. Seguridad del rollout y rollback — **DEFINIDO**

**Por qué A (backend) es seguro sin C (frontend)** — las letras son las de §39, donde **B** es el
repositorio Git de `web` y **C** el frontend:
- No modifica ningún endpoint existente: sólo añade tres rutas nuevas bajo un prefijo que hoy no
  existe (`/api/salones/{salonId}/horarios/**`) — verificado que no colisiona con
  `/api/salones/{salonId}/excepciones-horario` ni con `/api/salones/{id}`.
- `PUT /api/salones/{id}` conserva request, response y comportamiento.
- El campo `codigo` es aditivo; ningún cliente rompe por un campo extra en JSON.
- Los endpoints nuevos sin cliente son código inerte, protegido por `@PreAuthorize` + scope.
- El único cambio observable sobre endpoints existentes es el de §11.6: un request ilegible pasa de
  **500** a **400**. Ningún cliente puede depender de recibir 500 ante su propio JSON roto, y
  ningún test del proyecto lo asserta (verificado por grep, §11.6).

**Rollback de C (frontend):** volver al bundle anterior es seguro y no corrompe datos. Consecuencia
aceptada: la edición real de horarios vuelve a estar **bloqueada** por
`HORARIOS_REQUIEREN_VERSIONADO`, es decir se vuelve al estado de F2B.2 (no se puede editar horarios
desde la UI), **no** a un estado destructivo. Las versiones ya creadas siguen íntegras y siguen
resolviéndose bien: `HorarioOperacionResolver` sabe leer historia versionada.

**Rollback de B (repositorio de `web`):** no aplica — no despliega nada; es trabajo de source
control (§45).

**Rollback de A (backend):** las filas versionadas ya creadas permanecen. `SalonService` sigue
resolviendo la vigente por fecha (F2B.2 ya lo soporta). Se pierde la capacidad de crear versiones
nuevas, no la historia.

**Prohibido en cualquier rollback:** reactivar el `delete + insert` de horarios. Es la regresión que
toda la fase 2B existe para impedir.

---

## 41. Alcance propuesto

**F2B.3b.2a — backend**

| Archivo | Acción |
|---|---|
| `ubicaciones/controlador/SalonHorarioOperacionController.java` | **nuevo** |
| `ubicaciones/servicio/SalonHorarioOperacionService.java` | **nuevo** (scope, mapeo, traducción 409) |
| `ubicaciones/dto/VersionarHorarioSalonRequest.java` | **nuevo** |
| `ubicaciones/dto/CerrarHorarioSalonRequest.java` | **nuevo** |
| `ubicaciones/dto/HorarioOperacionVersionResponse.java` | **nuevo** |
| `ubicaciones/repositorio/HorarioOperacionRepository.java` | `findVersionesOrdenadas` (§24) |
| `ubicaciones/servicio/HorarioOperacionErrores.java` | `esConflictoDeEstado` + whitelist (§11.5) |
| `exception/CodigoErrorExtractor.java` | **nuevo**, extractor único y neutral (§11.5) |
| `exception/ErrorResponse.java` | campo `codigo` (§12) |
| `exception/GlobalExceptionHandler.java` | `codigo` en `build(...)` (§12) + handlers de `HttpMessageNotReadableException` y `MethodArgumentTypeMismatchException` (§11.6) |
| `ubicaciones/servicio/SalonService.java` | **sólo Javadoc** (§27.5) |
| tests | §32, §33 + no-regresión del contrato de error |

**F2B.3b.2b — frontend**

| Archivo | Acción |
|---|---|
| `src/api/types.ts` | 3 tipos nuevos + `codigo?` en `ApiErrorBody` + `SalonRequest.horarios` nullable (§27.3.2) |
| `src/api/salones.ts` | 3 funciones nuevas (§28) |
| `src/pages/salones/components/EditarHorarioSemanalDialog.tsx` | reescritura (§18, §19); se conserva el nombre de archivo para no añadir ruido al diff |
| `src/pages/salones/SalonHorarios.tsx` | re-consulta del salón, historial, snackbars (§29) |
| `src/pages/salones/DialogoSalon.tsx` | paso 2 read-only en edición, `errorDelPaso(2)` omitida al editar, `horarios: null` en el `PUT` (§27.3) |
| `src/pages/salones/components/HistorialHorarioDia.tsx` | **nuevo**, panel read-only de historial (§21.1) |
| `src/pages/salones/erroresHorario.ts` | **nuevo**, mapa `codigo → mensaje` (§30) |
| `src/pages/salones/fechas.ts` | **nuevo**, `aIso` extraído de `SalonHorarios` (§17) |

**Fuera de alcance:** migraciones, writers core, `SalonLock`, adapters de Turnos/Bloques, reservas,
capacidad, materialización, retirada de la tolerancia del `PUT`, comandos de cancelación, framework
de tests frontend, zona horaria de negocio, **`SalonHorarioExcepcionService.eliminar`** (§2.2: su
orden lee-antes-de-autorizar se registra pero no se corrige aquí), **scope de
`GET /api/salones/{id}`** (§14.4), y **mover `web` al repositorio backend** (§45).

---

## 42. Decisiones cerradas

| # | Decisión | Valor |
|---|---|---|
| 1 | Path versionar | `POST /api/salones/{salonId}/horarios/versiones` |
| 2 | Path cerrar | `POST /api/salones/{salonId}/horarios/cierres` |
| 3 | Path historial | `GET /api/salones/{salonId}/horarios/historial[?diaSemana]` |
| 4 | Request versionar | `{diaSemana, efectivoDesde, horaApertura, horaCierre}`, `Short` envuelto, sin `vigenteHasta`, sin id |
| 5 | Request cerrar | `{diaSemana, efectivoDesde}` |
| 6 | Status versionar | **201** + `HorarioOperacionVersionResponse`, sin `Location` |
| 7 | Status cerrar | **200** + `HorarioOperacionVersionResponse` (versión cerrada) |
| 8 | Historial | **SÍ**, en F2B.3b.2a |
| 9 | DTO historial | `{diaSemana, horaApertura, horaCierre, vigenteDesde, vigenteHasta}`, `null` preservado, **sin `id`** |
| 10 | Orden historial | `dia_semana ASC`, `vigente_desde ASC NULLS FIRST` |
| 11 | Seguridad | `salon.administrar` (escritura) / `salon.leer` (historial), vía `@PreAuthorize` |
| 12 | Scope | `AutorizadorSalon.verificarAccesoSalon` como primera sentencia del service; 403 antes que 404 |
| 13 | Error mapping | input → 400; estado/historial/programación → **409** vía `ConflictException` traducida en el service con **whitelist cerrada de 6 códigos**; cualquier otra `ValidacionException` → **400**; salón inexistente → 404; scope → 403 |
| 14 | `NO_EXISTE_VERSION_VIGENTE_EN_ESA_FECHA` | **409** |
| 15 | Contrato de error | `{message, codigo\|null}`: `ErrorResponse` + campo aditivo `codigo`; `message` **conserva su prefijo**; sin romper clientes |
| 15b | Extractor de código | **UNO**, `CodigoErrorExtractor` neutral en `com.feelingpilates.exception`; usado por la whitelist de horarios **y** por `GlobalExceptionHandler`. Sin regex ni parsing duplicados. Extraer ≠ clasificar |
| 15c | Request malformado | `HttpMessageNotReadableException` y `MethodArgumentTypeMismatchException` → **400** con mensaje fijo y `codigo: null`; el handler genérico sigue dando **500** para lo inesperado |
| 15d | Historial: orden de operaciones | 1) `AutorizadorSalon` → 2) validar `diaSemana` `0..6` → 3) `existsById` → 4) query. **403 / 400 / 404 / `200 []`** son cuatro resultados distintos |
| 16 | Duplicate submit | Sin idempotencia HTTP; botón deshabilitado + 409 manejado |
| 17 | `efectivoDesde` UX | Obligatorio, `type="date"`, default hoy, `min` hoy, aviso si hay futuro |
| 18 | Flujo editar | Una operación por día; `diaSemana` fijo; nunca la semana completa; nunca `PUT /salones` |
| 19 | Flujo cerrar | Acción explícita "Dejar de operar este día" + fecha; sin `Switch`, sin borrado, sin `apertura == cierre` |
| 20 | `PUT` legacy | **Se mantiene** la tolerancia de payload idéntico, **sólo como compatibilidad hacia atrás**; el frontend nuevo no la usa; retirada en fase separada |
| 20b | `DialogoSalon` CREAR | Horarios editables, validación "al menos un día activo" **se conserva**, se envían en la creación |
| 20c | `DialogoSalon` EDITAR | Paso read-only, validación "al menos un día activo" **OMITIDA**, payload **`horarios: null`** |
| 20d | Mensaje de éxito futuro | **"Cambio programado para el \<fecha\>"**, neutral. Prohibido afirmar vigencia hasta D−1; no se infiere append vs reapertura |
| 20e | Ayuda del selector de fecha | Texto **neutral** (§17); prohibido *"el horario anterior se conserva hasta el día previo"* y cualquier variante universal |
| 20f | Refresco tras éxito | **Siempre** detalle del salón **y** historial, en ese orden, y **después** el modal (§29.1). **Nunca** condicionado a que el panel esté visible. Sin caching ni optimistic update |
| 21 | Rollout | A backend → B frontend → C verificación → D retirada |
| 22 | Writers | **Sin cambios** (salvo dos helpers de consulta en `HorarioOperacionErrores`) |
| 23 | Migraciones | **Ninguna** |
| 24 | Tests frontend | Sin framework nuevo; `tsc -b` + `oxlint` + plan manual de 16 casos |
| 25 | Source control del frontend | `web` tendrá **repositorio Git propio**; no se mueve al repo backend ni se hace monorepo en F2B; prerrequisito **operativo** de F2B.3b.2b (§45) |

**Ninguna decisión que afecte al contrato público queda abierta.**

---

## 43. Bloqueadores

**Estado tras F2B.3b.2.0.2: P0 NINGUNO, P1 NINGUNO.** Las dos contradicciones UX de la revisión
anterior están cerradas (§17, §29.1) y no quedan decisiones de contrato abiertas.

**F2B.3b.2a — BACKEND: BLOQUEADORES NINGUNO.**
Todo el diseño se apoya en código, permisos y patrones ya existentes y verificados. No depende del
frontend, ni de su repositorio, ni de ninguna decisión pendiente.

**F2B.3b.2b — DISEÑO: BLOQUEADORES DE CONTRATO NINGUNO.**
Paths, DTOs, statuses, códigos, textos, flujos, payload de edición y comportamiento del historial
están cerrados en §42.

**F2B.3b.2b — IMPLEMENTACIÓN: BLOQUEO OPERATIVO.**
`/Feelingpilates/web` todavía **no tiene repositorio Git autoritativo** (§45). Escribir la migración
del frontend sobre un directorio sin control de versiones significaría trabajar sin diff, sin
revisión, sin baseline y sin rollback. Estado: **LISTA EN DISEÑO / BLOQUEADA PARA IMPLEMENTACIÓN**.

⚠ **Este bloqueo es operativo, no de contrato.** No hay ninguna decisión de diseño esperando
respuesta: se desbloquea creando/conectando el repositorio, sin volver a diseño.

**PUEDE ESPERAR** (registrado, fuera de alcance, **deuda separada**)
1. **`GET /api/salones/{id}` sin scope por salón** (§14.4). Preexistente; los endpoints nuevos
   **no** copian esa debilidad y **no** amplían la exposición. F2B.3b.2a **no** es una remediación
   de endpoints legacy. Merece fase propia de scope.
2. **`SalonHorarioExcepcionService.eliminar` lee antes de autorizar** (§2.2). Preexistente; el
   patrón correcto (`listarPorRango`/`guardar`) es el que se replica. No se corrige aquí.
3. **Sin suite de tests frontend** (§34.3). Vitest + testing-library en fase propia.
4. **Command de cancelación de versión futura** (§20, §21). Sin él, `CIERRE_CON_VERSIONES_FUTURAS`
   no tiene salida desde la UI. Impacto de UX real; primer candidato tras F2B.3b.2.
5. **Zona horaria de negocio** (§17). `Clock.systemDefaultZone()` sin zona declarada; desajuste
   posible cliente/servidor cerca de medianoche. Mitigado por el mensaje de
   `EFECTIVO_DESDE_EN_EL_PASADO`.
6. **Retirada de la tolerancia del `PUT`** (§27.4), tras verificar D, **si se decide**.
7. **Conflictos de programación enriquecidos** (§22): pasar de mensaje genérico a lista accionable
   exigiría un DTO de conflictos y romper el aislamiento de módulos. Fase propia.
8. **Limpieza del prefijo `CODIGO:` en `message`** (§12), sólo cuando todos los clientes consuman
   `codigo`. No se hace en F2B.3b.2a.

**Ya NO está en esta lista** (era el punto 2 de F2B.3b.2.0): *"`HttpMessageNotReadableException` →
500"*. Deja de ser deuda diferida porque **entra en alcance** en §11.6: la API nueva no puede
prometer 400 y devolver 500.

---

## 44. Primera implementación — secuencia recomendada

**Dos tareas, dos ramas, dos revisiones.**

**F2B.3b.2a — backend** (rama `operacion/horario-versionado-api`, desde
`operacion/horario-versionado-writers`), en este orden:

1. `CodigoErrorExtractor` + `codigo` en `ErrorResponse` + `GlobalExceptionHandler.build` + handlers
   de request malformado (§11.6) + tests de no-regresión del contrato de error. **Primero y solo**,
   porque toca el contrato global: si algo se rompe, se detecta antes de meter tres endpoints
   encima.
2. `esConflictoDeEstado` + whitelist en `HorarioOperacionErrores` (delegando en el extractor) +
   tests unitarios, incluido el caso "código desconocido → NO es conflicto".
3. DTOs + `SalonHorarioOperacionService` (scope + mapeo + traducción) +
   `SalonHorarioOperacionController`.
4. `findVersionesOrdenadas` + endpoint de historial con el orden de operaciones de §7.4.
5. Tests §32 y §33.
6. Javadoc de `SalonService` (§27.5).

**F2B.3b.2b — frontend** (rama `operacion/horario-versionado-frontend` **en el repositorio propio de
`web`**, §45), después de que 2a esté desplegada y de que el repositorio exista:

1. Tipos + funciones de API (§28) — sin consumidor todavía, compila solo.
2. `erroresHorario.ts` + `fechas.ts`.
3. Reescritura de `EditarHorarioSemanalDialog` + panel de historial (§21.1) + ajustes de
   `SalonHorarios`.
4. `DialogoSalon`: paso 2 read-only en edición, `errorDelPaso(2)` omitida al editar, `horarios: null`
   en el `PUT` (§27.3).
5. `npm run build`, `npm run lint`, plan manual §34.2 completo (16 casos).

**Por qué separadas y no un solo commit/rama:**
- **Reversibilidad independiente**: revertir el frontend sin revertir el backend es un escenario
  realista (§40); en una sola rama, revertir arrastra los endpoints.
- **Revisión de naturaleza distinta**: 2a se revisa contra concurrencia, seguridad, scope y contrato
  HTTP; 2b contra UX, estado y peticiones de red. Mezcladas, una tapa a la otra.
- **El orden de despliegue es un requisito** (§39), y ramas separadas lo hacen explícito en vez de
  confiarlo a la disciplina del que despliega.
- **Están en repositorios distintos** (§45), así que ni siquiera podrían compartir rama.
- 2a es verificable de punta a punta **sin** frontend (tests + `curl`); ese es precisamente el
  criterio de salida de 2a.

**Criterio de salida de 2a**: suite verde (337 previos + nuevos), build PASS, Flyway V1→V46 PASS
(sin V47), y los tres endpoints ejercitados manualmente con `curl` incluyendo un 409, un 403, un
404 de historial y un 400 de JSON malformado.

---

## 45. Source control del frontend — **DECISIÓN RATIFICADA**

### 45.1 Situación real (verificada)

| Ruta | Estado |
|---|---|
| `/Users/jesusaldaircruzortiz/Desktop/Feelingpilates/feelingpilates` | Repositorio Git del **backend**; rama `operacion/horario-versionado-writers` |
| `/Users/jesusaldaircruzortiz/Desktop/Feelingpilates/web` | **Frontend sin repositorio Git**: no pertenece a ningún repositorio |

Todo el trabajo de F2B.3b.2b vive en `web`.

### 45.2 Decisión

- **NO se mueve `web` dentro del repositorio del backend durante F2B.**
- **NO se convierte el repositorio backend en monorepo en esta fase.**
- **`web` tendrá su PROPIO repositorio Git autoritativo.**
- Crear/conectar ese repositorio es un **prerrequisito operativo** para comenzar F2B.3b.2b.

Motivo: mover el frontend a mitad de una fase de horarios mezclaría dos cambios de naturaleza
distinta —una migración de contrato HTTP y una reorganización de source control— en el mismo
historial, y volvería ilegible la revisión de ambos. Y un monorepo es una decisión de plataforma
(CI, versionado, releases, permisos), no un efecto colateral de F2B.

### 45.3 Lo que deliberadamente NO se decide aquí

No se inventa todavía: **nombre del repositorio remoto**, **URL remota**, **hosting**, **rama por
defecto**, ni **migración de historia**. Nada de eso afecta al contrato de F2B.3b.2b, y fijarlo por
escrito sin poder verificarlo produciría documentación falsa. Se resuelve **operativamente antes**
de empezar F2B.3b.2b.

### 45.4 Consecuencia para las fases

| Fase | Estado |
|---|---|
| **F2B.3b.2a — backend** | **NO bloqueada** por el repositorio del frontend. Se implementa, revisa y despliega de forma independiente |
| **F2B.3b.2b — diseño** | **CERRADO**. Sin bloqueadores de contrato |
| **F2B.3b.2b — implementación** | **BLOQUEADA OPERATIVAMENTE** hasta que `/Feelingpilates/web` tenga repositorio Git autoritativo y baseline limpio |

Terminología para no confundir estados: **LISTA EN DISEÑO / BLOQUEADA PARA IMPLEMENTACIÓN**.

**No se finge que el source control ya existe.** Cualquier referencia de este documento a ramas o
commits del frontend (§44) presupone ese repositorio, y sólo aplica una vez creado.

---

## 46. Checkpoint

Único archivo escrito: `auditoria/fase-2b3b2-diseno-api-frontend-horarios.md` (actualizado a
F2B.3b.2.0.2). Sin código backend, sin código frontend, sin migraciones, sin tests, sin `git add`,
sin commit, sin push, sin cambio de rama.

Working tree esperado — **SOLO CHECKPOINT**:

```
?? auditoria/fase-2b3b2-diseno-api-frontend-horarios.md
```
