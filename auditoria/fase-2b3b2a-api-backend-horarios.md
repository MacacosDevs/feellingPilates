# Fase 2B.3b.2a — Checkpoint de implementación: API administrativa backend de horarios versionados

Diseño autoritativo: `auditoria/fase-2b3b2-diseno-api-frontend-horarios.md` (F2B.3b.2.0.2).
No se reinterpretó ninguna decisión cerrada allí.

## Base

Branch: `operacion/horario-versionado-api`
HEAD base: `e944a1bd214df5d1a646b95d7cda4e2b1f9a6df1`
Working tree base: limpio (confirmado en pre-flight).

## Estado final

- API Versionar: **IMPLEMENTADA** — `POST /api/salones/{salonId}/horarios/versiones` → 201 + `HorarioOperacionVersionResponse`
- API Cerrar: **IMPLEMENTADA** — `POST /api/salones/{salonId}/horarios/cierres` → 200 + `HorarioOperacionVersionResponse`
- API Historial: **IMPLEMENTADA** — `GET /api/salones/{salonId}/horarios/historial[?diaSemana]` → 200 + lista
- Security: **IMPLEMENTADA** — `@PreAuthorize('salon.administrar')` en versionar/cerrar, `@PreAuthorize('salon.leer')` en historial
- Scope: **IMPLEMENTADO** — `AutorizadorSalon.verificarAccesoSalon` como primera sentencia en los tres métodos del service; 403 antes que 404
- Malformed request: **400 PROBADO** — `HttpMessageNotReadableException` y `MethodArgumentTypeMismatchException` con handlers nuevos, `codigo: null`
- Whitelist 409: **PROBADA** — 6 códigos cerrados en `HorarioOperacionErrores.esConflictoDeEstado`, traducidos en `SalonHorarioOperacionService`; cualquier otro código sigue en 400 (probado explícitamente)
- Extractor: **ÚNICO** — `CodigoErrorExtractor` en `com.feelingpilates.exception`, una sola regex, dos consumidores (`HorarioOperacionErrores` y `GlobalExceptionHandler`)
- ErrorResponse.codigo: **IMPLEMENTADO** — campo aditivo, poblado en el único sitio de construcción (`GlobalExceptionHandler.build`)
- Historial 403/400/404/200[]: **PROBADO** — controller test (mocks) + persistencia real (PostgreSQL/Testcontainers), como tres/cuatro resultados distintos y explícitos
- PUT legacy: **INTACTO** — sin cambios de comportamiento; sólo se corrigió Javadoc obsoleto en `SalonService` (§27.5 del diseño); tests existentes de `SalonServiceTest` siguen verdes
- Writers: **INTACTOS** — `git diff` contra el HEAD base para `VersionarHorarioOperacion.java`, `CerrarHorarioOperacion.java`, `SalonLock.java` es **vacío**
- Migraciones: **SIN CAMBIOS** — `git diff` contra el HEAD base para `src/main/resources/db/migration` es **vacío**; sin V47
- Frontend: **SIN CAMBIOS** — `/Feelingpilates/web` no se tocó
- Tests: **398/398 PASS** (baseline 337 + 56 nuevos F2B.3b.2a + 5 nuevos F2B.3b.2a.1, ver abajo)
- Build: **PASS** (`./mvnw clean compile` → BUILD SUCCESS)
- Flyway: **V1→V46 PASS** (verificado por el arranque exitoso de todos los `@SpringBootTest`, sin V47)
- JPA: **PASS** (`ddl-auto=validate` en los tres perfiles; todos los contextos Spring Boot cargaron sin error)

## Archivos productivos tocados

Nuevos:
- `src/main/java/com/feelingpilates/exception/CodigoErrorExtractor.java`
- `src/main/java/com/feelingpilates/ubicaciones/controlador/SalonHorarioOperacionController.java`
- `src/main/java/com/feelingpilates/ubicaciones/servicio/SalonHorarioOperacionService.java`
- `src/main/java/com/feelingpilates/ubicaciones/dto/VersionarHorarioSalonRequest.java`
- `src/main/java/com/feelingpilates/ubicaciones/dto/CerrarHorarioSalonRequest.java`
- `src/main/java/com/feelingpilates/ubicaciones/dto/HorarioOperacionVersionResponse.java`

Modificados:
- `src/main/java/com/feelingpilates/exception/ErrorResponse.java` (campo aditivo `codigo`)
- `src/main/java/com/feelingpilates/exception/GlobalExceptionHandler.java` (`codigo` en `build(...)`, handlers de request malformado)
- `src/main/java/com/feelingpilates/ubicaciones/repositorio/HorarioOperacionRepository.java` (`findVersionesOrdenadas`)
- `src/main/java/com/feelingpilates/ubicaciones/servicio/HorarioOperacionErrores.java` (`esConflictoDeEstado` + whitelist)
- `src/main/java/com/feelingpilates/ubicaciones/servicio/SalonService.java` (sólo Javadoc, §27.5)

Tests nuevos:
- `src/test/java/com/feelingpilates/exception/CodigoErrorExtractorTest.java`
- `src/test/java/com/feelingpilates/exception/GlobalExceptionHandlerTest.java`
- `src/test/java/com/feelingpilates/ubicaciones/servicio/HorarioOperacionErroresTest.java`
- `src/test/java/com/feelingpilates/ubicaciones/controlador/SalonHorarioOperacionControllerTest.java` (31 tests)
- `src/test/java/com/feelingpilates/ubicaciones/SalonHorarioOperacionHistorialPersistenciaTest.java` (7 tests, PostgreSQL real)

No tocados (confirmado por `git diff` vacío contra el HEAD base):
- `VersionarHorarioOperacion.java`, `CerrarHorarioOperacion.java`, `SalonLock.java`
- `src/main/resources/db/migration/**`
- `/Feelingpilates/web` (frontend), `/Feelingpilates/FeelingPiltaesAppMobile` (mobile)

## Endpoints

| Método | Path | Status éxito | Security | Scope |
|---|---|---|---|---|
| POST | `/api/salones/{salonId}/horarios/versiones` | 201 | `salon.administrar` | `AutorizadorSalon`, 1ª sentencia |
| POST | `/api/salones/{salonId}/horarios/cierres` | 200 | `salon.administrar` | `AutorizadorSalon`, 1ª sentencia |
| GET | `/api/salones/{salonId}/horarios/historial[?diaSemana]` | 200 | `salon.leer` | `AutorizadorSalon`, 1ª sentencia |

## DTOs

- `VersionarHorarioSalonRequest(Short diaSemana, LocalDate efectivoDesde, LocalTime horaApertura, LocalTime horaCierre)`
- `CerrarHorarioSalonRequest(Short diaSemana, LocalDate efectivoDesde)`
- `HorarioOperacionVersionResponse(short diaSemana, LocalTime horaApertura, LocalTime horaCierre, LocalDate vigenteDesde, LocalDate vigenteHasta)` — sin `id`, sin `salonId`, `null` preservado (sin sentinels)

## Clasificación de errores (verificada por tests)

- 400: `DIA_SEMANA_INVALIDO`, `HORA_CIERRE_DEBE_SER_POSTERIOR`, `EFECTIVO_DESDE_EN_EL_PASADO`, cualquier código fuera de la whitelist, Bean Validation, request malformado (JSON/fecha/hora/UUID/tipo de query param)
- 409: `YA_EXISTE_VERSION_EN_ESA_FECHA`, `VERSIONADO_INTERMEDIO_NO_SOPORTADO`, `NO_EXISTE_VERSION_VIGENTE_EN_ESA_FECHA`, `CANCELACION_DE_VERSION_NO_SOPORTADA`, `CIERRE_CON_VERSIONES_FUTURAS`, `PROGRAMACION_INCOMPATIBLE_CON_HORARIO`, `CONFLICTO_VIGENCIA_HORARIO` (ya existente, sin cambios)
- 404: salón inexistente (versionar/cerrar vía `SalonLock`; historial vía `existsById`)
- 403: scope o permiso insuficiente

## 20 mutaciones conceptuales — todas DETECTADAS

Ver `SalonHorarioOperacionControllerTest`, `SalonHorarioOperacionHistorialPersistenciaTest`,
`CodigoErrorExtractorTest`, `HorarioOperacionErroresTest`, `GlobalExceptionHandlerTest`. Cada una
de las 20 mutaciones listadas en el diseño (§63) tiene al menos un assert que la revierte de PASS a
FAIL: autorización antes de repositorio, 403/404/200[] distintos, whitelist cerrada de 409, mensaje
y código preservados en `ErrorResponse`, extractor único sin falsos positivos, sin UUID ni sentinels
en el historial, y PUT legacy sin cambio de comportamiento (suite baseline intacta).

Mutación #16 ("Parser duplicado"): **DETECTADA** (corregido en F2B.3b.2a.1, ver abajo). En esta
entrega original la unicidad del extractor sólo se había verificado por búsqueda manual; no existía
ningún assert automático que revirtiera de PASS a FAIL si `GlobalExceptionHandler` o
`HorarioOperacionErrores` volvían a implementar un parser propio.

## F2B.3b.2a.1 — Corrección de review (mutación #16)

Único P1 del review de Codex sobre F2B.3b.2a: mutación #16 ("Parser duplicado") no estaba protegida
por ningún test automático, aunque la implementación productiva ya era correcta (un solo
`CodigoErrorExtractor`, una sola regex repo-wide, ambos consumidores delegando).

- Test nuevo: `src/test/java/com/feelingpilates/exception/CodigoErrorExtractorArquitecturaTest.java`
  (5 tests). Guard de arquitectura determinista, sin dependencia nueva: lee como texto
  `GlobalExceptionHandler.java` y `HorarioOperacionErrores.java` y falla si aparece
  `java.util.regex.Pattern`, `java.util.regex.Matcher` o `Pattern.compile`, o si falta la delegación
  explícita `CodigoErrorExtractor.extraer(`.
- Prueba de mutación: se insertó temporalmente (sin commit) un import de
  `java.util.regex.Pattern`/`Matcher` en `HorarioOperacionErrores.java`; el nuevo test **FALLÓ**
  (1 failure) como se esperaba. Se revirtió la mutación de inmediato; `git diff` contra el HEAD base
  para `src/main/java` quedó vacío tras la reversión.
- Alcance: exclusivamente los dos consumidores (`GlobalExceptionHandler`,
  `HorarioOperacionErrores`); no es un linter repo-wide.
- Producción: **SIN CAMBIOS** (`git diff` contra el HEAD base para `src/main/java` es vacío).
- Migraciones: **SIN CAMBIOS**.
- Tests: 393/393 → **398/398 PASS**.

## Bloqueadores

Ninguno. Sin necesidad de cambiar semántica de writers, sin migración nueva, `AutorizadorSalon`
pudo ejecutarse siempre como primera sentencia, 403 y 404 quedaron distinguibles sin romper scope,
`ErrorResponse.codigo` es aditivo y no rompe compatibilidad, historial no necesitó UUID ni sentinel.

## F2B.3b.2b

LISTA EN DISEÑO / BLOQUEADA PARA IMPLEMENTACIÓN — bloqueo operativo (repositorio Git propio de
`/Feelingpilates/web`, §45 del diseño), no de contrato. F2B.3b.2a no depende de eso y quedó
completa de forma independiente.
