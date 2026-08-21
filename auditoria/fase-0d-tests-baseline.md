# FeelingPilates — Fase 0D: baseline de tests verde

Fecha: 2026-08-21
Branch base: `stabilizacion/flyway-pre-release`
Commit base: `769ca0a279a69794dcfb08db8e55378240231a82`
Branch de trabajo: `stabilizacion/tests-baseline`

## Pre-flight

- La branch base y el commit coincidieron con los valores esperados.
- El working tree estaba limpio.
- `stabilizacion/flyway-pre-release` seguía a `origin/stabilizacion/flyway-pre-release`.
- `stabilizacion/tests-baseline` no existía y se creó desde el commit base.

## CAUSA

`POST /api/auth/google` no tenía un mapping activo porque el endpoint estaba
comentado. Spring resolvía la petición mediante `ResourceHttpRequestHandler` y
lanzaba `NoResourceFoundException`. El `GlobalExceptionHandler` trataba esa
excepción inesperada mediante su handler genérico, por lo que el resultado era
HTTP 500 con `ErrorResponse`, en lugar del HTTP 501 esperado.

Reproducción aislada antes de modificar código:

- Test: `AuthControllerTest#googleStubDevuelve501`.
- Handler observado: `ResourceHttpRequestHandler`.
- Excepción observada: `NoResourceFoundException`.
- Resultado: 1 test, 1 failure; esperado 501, actual 500.

## CONTRATO

Google deshabilitado -> 501.

`POST /api/auth/google` se conserva como stub explícito. Responde HTTP 501
`Not Implemented` con el formato `ErrorResponse` estándar y el mensaje
`El inicio de sesión con Google está deshabilitado`.

El stub no recibe ni valida un DTO, no invoca `AuthService`, no usa
`GoogleTokenVerifier`, no crea usuarios, no emite JWT y no realiza llamadas a
Google. No se reactivó ni implementó OAuth.

## Corrección

- `AuthController` expone el mapping `/google` y lanza una excepción específica.
- `GoogleLoginDisabledException` acota el estado funcional deshabilitado.
- `GlobalExceptionHandler` traduce sólo esa excepción a HTTP 501 y conserva el
  handler genérico de excepciones inesperadas como HTTP 500.
- `AuthControllerTest` comprueba status, cuerpo estándar, path, mensaje y cero
  interacciones con `GoogleTokenVerifier`.

## Archivos modificados

- `src/main/java/com/feelingpilates/auth/AuthController.java`.
- `src/main/java/com/feelingpilates/auth/GoogleLoginDisabledException.java`.
- `src/main/java/com/feelingpilates/exception/GlobalExceptionHandler.java`.
- `src/test/java/com/feelingpilates/auth/AuthControllerTest.java`.
- `auditoria/fase-0d-tests-baseline.md`.

No se modificaron configuración, dependencias, schema ni archivos SQL.

## TESTS

Antes:

- Baseline heredado de fase 0C1: 9/10 PASS.
- Reproducción aislada: 0/1 PASS; 1 failure; 0 errors.

Después:

- Test afectado: 1/1 PASS.
- Clase `AuthControllerTest`: 9/9 PASS.
- Suite `./mvnw test`: 10/10 PASS; 0 failures; 0 errors; 0 skipped.

Resultado final: **10/10 PASS**.

## BUILD

- Comando: `./mvnw clean compile`.
- Resultado: **PASS — BUILD SUCCESS**.

## FLYWAY

- **SIN CAMBIOS** en migraciones o configuración.
- La suite arrancó con PostgreSQL 16 efímero mediante Testcontainers.
- Flyway validó y aplicó 43 migraciones hasta V40.
- El contexto de Spring y JPA inició correctamente.

## Commit

- Mensaje: `fix: restablecer baseline verde de tests`.
- El hash autoritativo es el commit que contiene este documento.
- No se hizo push, merge ni tag.

## Resultado final

- Branch: `stabilizacion/tests-baseline`.
- Contrato Google deshabilitado: HTTP 501 explícito.
- Tests: 10/10 PASS.
- Build: PASS.
- Flyway: SIN CAMBIOS.
