# Fase 0E.3 — Redacción de secretos, logs y tokens

## Checkpoint

- Branch base verificada: `seguridad/scope-salon`
- Commit base verificado: `59fbd1cd330b317eef864759eee897517887f75e`
- Branch de trabajo: `seguridad/redaccion-secretos`
- La branch base estaba respaldada por `origin/seguridad/scope-salon` antes de crear la branch de trabajo.
- Flyway: sin cambios

## Exposiciones encontradas

### A. Valores sensibles expuestos

- `FeelingpilatesApplication` imprimía datos de diagnóstico de la conexión DB: host, usuario, longitud del password y sus caracteres extremos, tanto por salida estándar como por SLF4J.
- `EmailServiceConsola` registraba destinatario y URL completa de invitación; la URL incluía el token de un solo uso.
- El `toString` generado automáticamente para `CompletarInvitacionRequest` incluía el token de invitación y la contraseña recibida.
- El cuerpo de error HTTP reflejaba `request.getRequestURI()`. En `GET /api/auth/invitaciones/{token}`, una invitación inválida podía reaparecer en el campo `path` de la respuesta.

### B. Nombres de configuración sin valor

- Se encontraron nombres de propiedades y variables sensibles en inyección de configuración, comentarios y mensajes operativos. Esas coincidencias no imprimen el valor recibido.

### C. Mensajes seguros

- Los mensajes acotados de Stripe y Google informan estado de inicialización, categoría de error o IDs operativos; no interpolan la clave secreta, `clientSecret`, JWT, header `Authorization` ni token de Google.
- El mensaje nuevo de email simulado sólo informa que se generó una invitación y declara que destinatario y enlace fueron omitidos.

### D. Falsos positivos o uso funcional

- El token de invitación existe en memoria para persistirlo, construir el enlace y validar/completar la invitación. Su uso funcional no equivale a registrarlo.
- `TokenResponse` entrega el JWT de autenticación como parte del contrato HTTP esperado; no se encontró logging del DTO o del JWT.
- `CrearPagoResponse.clientSecret` es parte del contrato existente del flujo de pago; no se encontró un log que lo exponga y no se modificó PagoService.

## Cambios aplicados

- Se eliminó todo el bloque de diagnóstico de variables DB de `FeelingpilatesApplication`; no se sustituyó el password por fragmentos ni por otra señal derivada.
- `EmailServiceConsola` dejó de registrar nombre, correo, URL y token. Conserva un único mensaje operativo seguro.
- `CompletarInvitacionRequest.toString()` redacta token y contraseña sin cambiar serialización, validación ni contrato HTTP.
- `GlobalExceptionHandler` redacta el segmento secreto de la ruta de consulta de invitaciones antes de incluirla en `ErrorResponse.path`. La ruta fija `/api/auth/invitaciones/completar` conserva su comportamiento.
- No se modificaron generación, envío en memoria, validación ni consumo funcional del token.

## Datos que ya no se registran o reflejan

- Host y usuario de DB usados por el diagnóstico eliminado.
- Longitud, primer carácter y último carácter del password de DB.
- Nombre y correo del destinatario de la invitación simulada.
- URL completa de invitación.
- Token de invitación en logs, `toString` y rutas reflejadas en respuestas de error.
- Contraseña de completar invitación en `toString`.

## Tests

- `EmailServiceConsolaTest` captura el log real y verifica que aparece el evento seguro, pero no el token, la URL completa ni el correo.
- `CompletarInvitacionRequestTest` verifica que `toString` redacta token y contraseña.
- `AuthControllerTest#invitacionInvalidaNoReflejaTokenEnRespuesta` verifica que una respuesta 404 no contiene el token y devuelve la ruta redactada.
- Pruebas específicas de logging/DTO: 2/2 PASS.
- Suite completa: 26/26 PASS.

## Búsqueda final

Se revisaron en fuentes productivas `log.*`, `System.out`, `System.err` y `printStackTrace`, cruzados con password/contraseña, secret, token, `clientSecret`, authorization, Stripe, JWT e invitación.

- No quedan `System.out`, `System.err` ni `printStackTrace` productivos.
- No se encontró logging explícito de password, JWT, header `Authorization`, token de invitación, URL de invitación, `clientSecret`, API key o clave secreta.
- Permanecen mensajes operativos de Stripe y Google que no incluyen los valores sensibles.

## Gaps futuros

- `InvitacionUsuario.token` continúa persistido en plaintext. Cambiarlo a hash requiere schema, migración y adaptación del lookup; queda fuera de esta fase.
- El token continúa formando parte de la ruta del endpoint existente. La respuesta de error de la aplicación ya lo redacta, pero una fase futura debe evaluar el contrato y la configuración de access logs/proxies para evitar captura de rutas sensibles fuera de la aplicación.
- Una política transversal para representaciones seguras de todos los DTOs de autenticación queda fuera de la revisión acotada al flujo de invitación.
- No se implementaron email real, Outbox, reintentos ni rediseño de Notificaciones.

## Resultado

PASSWORD DB EN LOG:
ELIMINADO

TOKEN INVITACION EN LOG:
ELIMINADO

URL INVITACION CON TOKEN:
ELIMINADA

OTROS SECRETOS ENCONTRADOS:
Token y contraseña en `toString` del request de completar invitación, y token reflejado en la ruta de respuestas de error; ambos corregidos sin registrar valores reales.

TOKEN EN BD:
PLAINTEXT PENDIENTE

TESTS:
26/26 PASS

BUILD:
PASS

FLYWAY:
SIN CAMBIOS

Branch:
`seguridad/redaccion-secretos`

Commit:
el commit que contiene este checkpoint, reportado al finalizar la fase
