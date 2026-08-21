# Fase 0E.1 — Corrección de IDOR en Reservas

- Fecha: 2026-08-21
- Branch: `seguridad/reservas-idor`
- Base: `f029c67d72788b1a3f3d696ac3e10d5fa12b2afc` (`stabilizacion/tests-baseline`)
- Commit: el commit que contiene este checkpoint, reportado al finalizar la fase

## Vulnerabilidad

**VULNERABILIDAD:** IDOR en el endpoint de reservas propias.

El flujo anterior era:

```http
GET /api/reservas/mias?clienteId=<UUID>
```

`ReservaController#listarMias` recibía `clienteId` mediante `@RequestParam` y lo enviaba directamente a `ReservaService#listarPorCliente`. El endpoint no comparaba ese UUID con el principal. La única barrera era la autenticación global de `SecurityConfig`; por tanto, cualquier cliente autenticado podía consultar las reservas confirmadas de otro cliente si conocía o adivinaba su UUID.

## Corrección

El contrato de autoservicio queda como:

```http
GET /api/reservas/mias
Authorization: Bearer <token>
```

`ReservaController#listarMias` recibe el `UsuarioAutenticado` mediante `@AuthenticationPrincipal` y pasa exclusivamente `usuario.id()` a `ReservaService#listarPorCliente`. Este principal es el mecanismo ya existente: `JwtAuthFilter` valida el token, toma su subject, resuelve desde base de datos el usuario activo y sus permisos mediante `ContextoAutenticacionService`, y crea el `UsuarioAutenticado` del `SecurityContext`.

Un parámetro extra `clienteId` ya no forma parte de la firma del endpoint y no altera la consulta: la identidad siempre procede del principal autenticado.

- **ANTES:** `clienteId` controlado por el request.
- **DESPUÉS:** `clienteId` derivado del principal.
- **OWNERSHIP:** PROTEGIDO para `GET /api/reservas/mias`.

## Compatibilidad y alcance revisado

- El código móvil disponible no consume `/api/reservas/mias`; las pantallas de reservas siguen usando datos simulados. No hay consumidor real que requiera conservar `?clienteId=`.
- No existe un endpoint `GET /api/reservas/{id}` de detalle.
- `DELETE /api/reservas/{id}` exige `reserva.administrar` y no es actualmente un flujo de autoservicio del cliente. Aplicar ownership o scope por salón a esa operación requiere definir autorización administrativa contextual, por lo que se documenta para 0E.2 y no se amplía esta fase.
- `GET /api/reservas` es una consulta administrativa por salón y fecha protegida por `calendario.leer`; su scope por salón también queda fuera de esta corrección puntual.
- No se añadió un endpoint administrativo nuevo.

## Archivos modificados

- `src/main/java/com/feelingpilates/calendario/controlador/ReservaController.java`
- `src/test/java/com/feelingpilates/calendario/ReservaControllerSecurityTest.java`
- `auditoria/fase-0e1-reservas-idor.md`

No fue necesario modificar `ReservaService`, `ContextoAutenticacionService`, RBAC, pagos, Stripe, Programación ni el esquema.

## Pruebas

Pruebas específicas ejecutadas:

```text
./mvnw -Dtest=ReservaControllerSecurityTest test
Tests run: 4, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

Casos demostrados:

1. Cliente A consulta sus reservas y el servicio recibe el ID de A desde el principal.
2. Cliente A envía `clienteId` de B como query param y el servicio sigue recibiendo únicamente el ID de A.
3. Aunque B tiene reservas, la respuesta solicitada por A no contiene reservas de B.
4. Un request sin autenticación recibe `401` y no llega al servicio.

Suite completa:

```text
./mvnw test
Tests run: 14, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

**TESTS:** 14/14 PASS.

## Build y Flyway

```text
./mvnw clean compile
BUILD SUCCESS
```

- **BUILD:** PASS.
- **FLYWAY:** SIN CAMBIOS. La suite validó correctamente las 43 migraciones existentes; no se creó ni modificó SQL en `src/main/resources/db/migration`.
