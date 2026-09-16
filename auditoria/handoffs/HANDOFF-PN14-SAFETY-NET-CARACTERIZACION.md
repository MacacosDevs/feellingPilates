# FeelingPilates — Handoff PN14 — Safety net/caracterización

Status al materializar: `CANDIDATE / PENDING_FRESH_AUDIT / NOT_APPROVED / NOT_ACTIVE`.
Tipo: `FUTURE_TEST_ONLY_IMPLEMENTATION_CONTRACT`. Implementación: `NOT_AUTHORIZED / NOT_STARTED`.
Este payload no se autoautoriza; permanecerá inmutable después de su audit. La activación
posterior pertenece a `ESTADO-ACTUAL.md`, con el SHA-256 físico exacto auditado y evidencia/gate
competentes. Las marcas candidate de este archivo conservarán ese snapshot histórico.

## 1. Autoridad y objetivo exacto

El repositorio es autoridad; Orca coordina y conserva provenance. Leer AGENTS, README y ESTADO,
este handoff sólo cuando esté activado por su hash, mapa, arquitectura, decisiones y dominio
aplicables, checkpoint PN13 §§13–14 y reviews citados, y protocolo de orquestación.

Materializar exclusivamente el primer slice publicado de PN13: `SAFETY_NET / CHARACTERIZATION`,
tests de comportamiento y contratos API del legacy observado, antes de cualquier cambio SQL,
tabla aditiva, snapshot objetivo o writer nuevo. No implementar ninguna regla futura PN13,
corregir comportamiento inseguro ni refactorizar producción. `REUSE / REWORK / RETIRE` en el
mapa clasifica la transición futura; no concede escritura. La secuencia general expand/backfill
del mapa no altera el orden ejecutivo de PN13 §13: el primer safety net precede todo SQL.

PN13 permanece `MATERIALIZED / ACCEPTED / PUBLISHED / CLOSED`, workflow terminal `PUBLISHED`,
documentation/publication/closure gates `PASS`, P0=0/P1=0/P2=1 exclusivamente por
`NEW-PN13-017 OPEN / P2 / EDITORIAL / NON_BLOCKING / IMPLEMENTATION_INDEPENDENT`.
No reabrir ni corregir PN13. El target production PN13 sigue `DESIGNED_NOT_IMPLEMENTED /
IMPLEMENTATION_NOT_AUTHORIZED`, incluso si posteriormente se autorizan estos tests nuevos.

## 2. Identidad y preflight futuro fail-closed

```text
WORKTREE: /Users/jesusaldaircruzortiz/Desktop/Feelingpilates/feelingpilates-payments-notifications
BRANCH: pagos/pagos-notificaciones-r1
BASELINE HEAD: 12f52781177694693be7d6dc2efc71009c5f45b3
BASELINE STAGING: EMPTY
BASELINE SOURCE: unchanged src/main, all existing src/test, pom.xml, resources
PN14 CANDIDATE CHECKPOINT: auditoria/fase-pn14-autorizacion-implementacion-safety-net-caracterizacion.md
CURRENT EXECUTION ENTRY: NOT_SATISFIED / NOT_AUTHORIZED
```

Antes de escribir, el EXECUTOR verifica físicamente path, branch, HEAD exacto, staging vacío,
upstream/live origin y ausencia de mutación concurrente. No fetch/pull para reconciliar.
Verifica el hash de este handoff contra el aceptado explícitamente por ESTADO, el checkpoint
PN14 referenciado allí y el review fresh independiente + gate del coordinador resuelto PASS.
El checkpoint PN13 y autoridades preservadas deben coincidir con los hashes de §3.
No basta un mensaje, la existencia de estos archivos ni una recomendación de audit.

Una eventual entrada local SIN commit de los documentos PN14 exige una transición competente
que declare expresamente `LOCAL_UNCOMMITTED_AUDITED_DOCUMENTATION_ENTRY`, HEAD baseline exacto
anterior, staging EMPTY, y un manifest físico aprobado con cada path, SHA-256 y delta permitido.
El delta documental autorizado queda acotado a estos dos archivos nuevos, el bloque PN14 de
ESTADO y la aclaración PN14 del mapa: no permite otras ediciones de esos canónicos ni paths
adicionales. Toda evidencia nueva de review/activación escrita por roles separados debe estar
enumerada por path/hash en ese manifest aprobado; este contrato no autoriza al EXECUTOR a
crearla o editarla. El manifest incluye el hash exacto del handoff auditado, checkpoint, review
y referencias del gate verificado físicamente y por la provenance operacional competente.
El dirty documental así identificado es baseline autorizado y se conserva byte-identical:
ni se atribuye al EXECUTOR ni se rechaza automáticamente por estar uncommitted. Cualquier delta
ajeno, hash desconocido o path no enumerado falla cerrado; no se concede una excepción general.

Aceptación del contrato y entrada de ejecución son gates distintos. El protocolo general no
impone publicación universal antes de todo EXECUTE; este contrato tampoco concede entrada local
por sí solo. Si el profile/transición competente exige publicación documental PN14 previa,
la entrada local no está habilitada: un PUBLISHER separado completa publicación/verificación y
cierre aplicables, y una nueva autorización competente fija físicamente el HEAD publicado,
hashes y manifest antes de ejecutar. No inferir otro HEAD ni permiso para usarlo desde aquí.
Este Run no publica PN14 ni demuestra autorización remota. Ningún cambio de HEAD respecto del
baseline se acepta sin esa nueva autorización expresa, aunque sea un commit documental.

## 3. Autoridad física preservada

Hashes SHA-256 del baseline publicado inspeccionado en esta materialización (no hashes de
snapshots intermedios históricos de R1.2):

| Path | SHA-256 |
| --- | --- |
| `auditoria/fase-pn13-materializacion-autoridad-pagos-notificaciones.md` | `5605569945e72a9d7ceff2778c64a9444d077ad4b6d80af53ea8e381d71d1749` |
| `auditoria/handoffs/HANDOFF-PN13-MATERIALIZACION-AUTORIDAD-PAGOS-NOTIFICACIONES.md` | `601d285a23c87b131b4b4946d2f858ad918faea12e492d76f7e9da7acd073e22` |
| `auditoria/ARQUITECTURA-ACTUAL.md` | `95eee81e34a3437628882b628ae138d6680c272767b8f8ebafb1027f675c7bda` |
| `auditoria/DECISIONES-ARQUITECTONICAS.md` | `305bb270f770029e4ea576019b9410970f12c7e70da554fa7b5e09f737123b83` |
| `auditoria/contexto/DOMINIO-FUNCIONAL.md` | `0580272ff72a41c841830e2e6cf13e8c1022e3716a59d741a780f4661a4b0b3a` |
| `auditoria/reviews/HANDOFF-PN13-MATERIALIZACION-AUTORIDAD-PAGOS-NOTIFICACIONES-REVIEW.md` | `26a0e67588f7a9e5bd13c79aa9006a833d63834cf3cf1a1a19467194e27df5f5` |
| `auditoria/reviews/PN13-REVIEW-MATERIALIZACION-AUTORIDAD-PAGOS-NOTIFICACIONES.md` | `cda17b50e562059382f42c46ebe827fedf6519682f95259860be4d0a48215477` |
| `auditoria/reviews/PN13-R1.2-REVIEW-MATERIALIZACION-AUTORIDAD-PAGOS-NOTIFICACIONES.md` | `da21981cbb4d0925fc7732e645580dd22e5e369938f6b165a0e7009d0eb4510b` |
| `auditoria/reviews/PN13-REVIEW-PUBLICACION-AUTORIDAD-PAGOS-NOTIFICACIONES.md` | `96378bcc74da86f656d5eaf0a08042e246f6ce6f87bee3e77873679228c17a1f` |
| `auditoria/reviews/PN13-REVIEW-CIERRE-PUBLICACION-AUTORIDAD-PAGOS-NOTIFICACIONES.md` | `675529f53540a86994b82c041203efb8b1d3b5003c501a74e88e900b9f78e901` |

La matriz histórica del mapa se preserva íntegra. Arquitectura §§17–18 describe la verdad física;
Dominio §13 y DA-014–022 son diseño objetivo aceptado, no comportamiento a exigir al legacy.
El snapshot before/after del EXECUTOR cubre TODOS los tracked/untracked no ignorados, más los
fingerprints de source/index; no atribuirle el dirty documental preexistente autorizado.

## 4. Única allowlist futura de escritura del EXECUTOR

Crear estos trece archivos, todos inexistentes en el baseline; no escribir ningún otro path.
Once tests obligatorios y dos helpers permitidos (helpers sólo si se necesitan):

```text
src/test/java/com/feelingpilates/pagos/caracterizacion/PagoIntentoPN14Test.java
src/test/java/com/feelingpilates/pagos/caracterizacion/PagoWebhookPN14Test.java
src/test/java/com/feelingpilates/pagos/caracterizacion/PagoReconciliacionPN14Test.java
src/test/java/com/feelingpilates/pagos/caracterizacion/PagoLecturasPN14Test.java
src/test/java/com/feelingpilates/pagos/caracterizacion/VentaServicePN14Test.java
src/test/java/com/feelingpilates/pagos/caracterizacion/CatalogoPN14Test.java
src/test/java/com/feelingpilates/pagos/caracterizacion/PagosApiPN14Test.java
src/test/java/com/feelingpilates/pagos/caracterizacion/VentasCatalogoApiPN14Test.java
src/test/java/com/feelingpilates/pagos/caracterizacion/ReservasPN14Test.java
src/test/java/com/feelingpilates/pagos/caracterizacion/ReservasApiPN14Test.java
src/test/java/com/feelingpilates/pagos/caracterizacion/CompraPersistenciaPN14Test.java
src/test/java/com/feelingpilates/pagos/caracterizacion/PN14Fixtures.java
src/test/java/com/feelingpilates/pagos/caracterizacion/StripeResponseGetterPN14Fake.java
```

No globs autorizantes. Si cualquier path ya existe al entrar, STOP y reconciliar el manifest:
esta autorización de creación no permite sobrescribirlo. Helpers contienen sólo fixtures/fakes,
no implementación de dominio, reglas objetivo ni copias de servicios productivos.

Prohibidos TODOS los `src/main` y cualquier test/helper existente, `pom.xml`, `.mvn`, wrappers,
`src/test/resources` (incluido MockMaker), otros resources, SQL/migraciones/schema, configuración
Spring/runtime/secrets, frontend/mobile, documentación y reviews para el EXECUTOR.
No producción nueva, ledger, Inbox/Outbox, adapters objetivo ni otro módulo de Reservas.
F2E, su implementación/readers y sus otros worktrees no se inspeccionan ni integran; F2D y
Programación conservan autoridad y fingerprints. Lectura de Reserva/TurnoInstructor legacy
necesaria para este contrato no habilita modificación de sus writers ni de programación.

## 5. Matriz mínima de aceptación conductual

Cada fila se enlaza en la entrega técnica a métodos de test concretos, input, resultado observado
y regresión detectada. Verificar resultados, estado guardado, parámetros y ausencia de efectos
en ramas rechazadas. Tests vacíos, sólo source/annotations, autoigualdad o un fake que sustituya
el comportamiento bajo test no cumplen. La eliminación de una aserción crítica elimina cobertura
de la fila y debe hacer FALLAR la aceptación en el audit; una regresión del comportamiento debe
hacer FALLAR el test. No claim de mutation-testing ejecutado si sólo existe análisis adversarial.

| ID / tests | Fuente legacy observada | Obligación y compatibilidad |
| --- | --- | --- |
| M01 / PagoIntento | `PagoService.crearIntentoPago`, `Compra` | Activo/existente; monto del servidor, moneda `mxn`, metadata compraId, automatic methods + redirects NEVER, save inicial y asociación PI, respuesta compraId/clientSecret/publishableKey; no efectos si paquete/usuario inválido. |
| M02 / PagoIntento | `reusarSiExiste` por entrada pública | Clave no blank reutiliza PI y no crea/guarda compra; null/blank toma rama nueva; RequestOptions lleva clave; errores create/retrieve se traducen a ValidacionException. Reuso por misma clave con usuario/paquete distintos ocurre antes de validarlos: caracterizar como LEGACY_NOT_TARGET, sin añadir payload guard. |
| M03 / PagoWebhook + PagosApi | `Webhook.constructEvent`, controller raw String | Firma HMAC-SHA256 real sobre bytes raw con secreto dummy; firma inválida/payload alterado no muta y HTTP 400; header requerido; payload válido HTTP 200 sin JWT y se transmite sin normalizar. SDK procesa evento real, no mock de constructEvent. |
| M04 / PagoWebhook | switch y deserializador | succeeded pendiente->pagada y expiración, succeeded duplicado sin save/renovar, payment_failed->fallida incluso desde pagada, succeeded posterior vuelve pagada; charge.refunded->reembolsada, duplicado sin save y PI null sin efecto; compra desconocida/tipo ignorado sin efecto. JSON versión distinta ejercita fallback raw y deserialización tipada donde compatible. No Inbox/event.id dedupe/máquina PN futura; regresiones legacy explícitas LEGACY_NOT_TARGET. |
| M05 / PagoReconciliacion | `reconciliarComprasPendientes` | Consulta pendientes; succeeded paga, canceled cancela; estado no resuelto antiguo cancela, reciente conserva; PI null no consulta; excepción Stripe conserva y continúa siguiente compra. Sin red ni scheduler productivo. Límite estricto isBefore se cubre con tiempo acotado, sin sleep ni Clock/refactor productivo. |
| M06 / PagoLecturas | activos/historial y DTOs | Activos sólo fecha no null y estrictamente futura, primer vigente en orden repo por pilates/bacu_fit, combo ocupa ambas; sin saldo/créditos. Historial conserva monto de Compra pero nombre/categoría de Paquete mutable; cambiar catálogo cambia esos campos; categoria null provoca NPE histórico actual (LEGACY_NOT_TARGET), sin imponer snapshot futuro. fechaInicio resta vigencia actual; expiración no es calendario PN objetivo. |
| M07 / VentaService | venta simple/carrito/sedes | Efectivo Y transferencia pagada inmediatamente, expiración por vigencia actual, una Compra por unidad, grupo UUID compartido, numeroItem 1-based y total; método stripe/inválido rechazado; paquete inactivo/missing y sede/actor inválidos; global ADMIN/SUPER_ADMIN vs sede propia. No pending validation, orden/settlement ni acreditación nueva. |
| M08 / VentaService | refund e historial | Refund caja pagada->reembolsada + motivo, rechaza stripe/no pagada; no llamada monetaria ni ledger. Historial DTO nombres actuales y nulos históricos registradaPor/salon/grupo/item/motivo; filtros métodos, estado, sede/actor, búsqueda trim, rango UTC inclusivo con límites default. Sin producto-entero/refund PN. |
| M09 / Catalogo | controllers/repos/PaqueteGestionService/PaqueteActividad | Catálogo público sólo activos según consulta y DTO composición, null categoria permitido; gestión listar/crear/actualizar/habilitar/deshabilitar, reemplazo actividades y validación actividad existente. IDs/DTO/precio/vigencia actuales; cambios de catálogo no crean snapshot. Map REUSE+REWORK/REWORK queda intacto. |
| M10 / PagosApi + VentasCatalogoApi | controllers/DTOs/SecurityConfig/advice | MockMvc con config seguridad real y principal UsuarioAutenticado: rutas de arquitectura §§17–18, HTTP 200/201, fields DTO exactos, body intento opcional/key, GET propio usa principal, público/webhook sin JWT; 401 sin autenticación para privadas, 403 sin autoridad donde anotada; venta/servicios permisos actuales (incluido habilitar con autoridad deshabilitar), buscar propio vs todos; validación @Valid 400 y errores servicio/advice observados. No inventar permiso PN. |
| M11 / Reservas + ReservasApi | ReservaService/ReservaController/ReservaRequest/Response | Crear CONFIRMADA duración actividad, acceso reserva.administrar, SalonLock antes de lectura efectiva/save, horario completo/especialidad/turno/traslape y errores sin persistir; cancelación CANCELADA y repetición actual vuelve save sin crédito. GET list/mias con principal y permisos actuales, POST 201, DELETE 204, DTO completo y @NotNull 400, 401/403. Reusar tests legacy existentes read-only; completar crear/cancelar y orden de interacciones sin reemplazarlos. Sin consumo, allowance ni atomicidad crédito. |
| M12 / CompraPersistencia | CompraRepository/PaqueteRepository + Flyway actual | PostgreSQL real: save/reload defaults, asociaciones y monto persistido; consultas por key/PI/usuario/estado/grupo/orden y filtros ventas/catalog; uniques PI/idempotency key rechazan duplicado. Dos transacciones concurrentes con misma key: exactamente un commit/una fila y violación constraint competidora, barrera/timeout y rollback independiente. Carrito válido + item inválido por VentaService realmente proxied: rollback de fila previa probado desde transacción nueva. No usar mocks para claim DB atomicity. |

M04/M06/M07/M08/M11 son caracterización del legacy, incluyendo quirks `LEGACY_NOT_TARGET`,
no aprobación productiva de esos comportamientos. Si se descubre otro comportamiento relevante,
registrar evidencia técnica de la discrepancia para audit; no cambiar reglas ni producción.
M12 prueba únicamente persistencia/constraint y rollback actuales. No demuestra ausencia de
carreras de creación PaymentIntent, atomicidad externa Stripe/DB ni locks/ledger/último crédito
objetivo. Invocar anotaciones @Transactional sobre instancia directa o mocks NO prueba atomicidad.

## 6. Seams deterministas y aislamiento de tests

Dependencies existentes: JUnit Jupiter, AssertJ, Mockito subclass, Spring test/MockMvc/security,
Testcontainers PostgreSQL, SDK stripe-java 29.4.0. No instalar/cambiar dependencia o MockMaker.
Inspección read-only del jar instalado confirmó `ApiResource.getGlobalResponseGetter` y
`setGlobalResponseGetter(StripeResponseGetter)`: PaymentIntent.create/retrieve llaman ese seam.
El fake enumerado intercepta request(ApiRequest, Type), valida método/path/params/options y
devuelve PaymentIntent dummy o excepción prevista; requestStream/rawRequest inesperado falla
cerrado, nunca delega a transporte HTTP. Capturar/restaurar getter y cualquier Stripe.apiKey
global en finally/AfterEach, incluso ante fallo; ResourceLock compartido para tests Stripe,
ejecución secuencial sin paralelismo JUnit. Evitar contexto full que instale StripeConfig durante
estos tests; nunca mutar configuración productiva. Ningún mock estático requerido.

Firmas webhook se calculan con JDK Mac/HmacSHA256, secreto `whsec_pn14_dummy`, timestamp vigente
para tolerancia del SDK y payloads JSON literales inline, pi_/evt_ dummy; incluir exactitud raw
con whitespace y payload alterado. No fixture con secretos, datos reales o identificadores vivos.
Servicio real bajo test con repositories Mockito y save Answer asignando UUID/timestamps dummy;
DTO records reales. Tiempo actual no tiene Clock: usar capturas antes/después con intervalos
acotados y fixtures muy anteriores/posteriores; documentar que el instante exacto de igualdad
con now no se controla en esta topología y no afirmar que se probó esa igualdad exacta.
Las ramas fecha pasada/futura/null y las ventanas acotadas siguen obligatorias. No sleeps ni
tests flaky; no mocking estático de tiempo ni cambios de Clock productivo.

M12 usa slice JPA test-only con TestcontainersConfiguration existente y postgres:16-alpine,
Flyway/schema actuales, fixtures sintéticas aisladas y configuración/beans anidados en el nuevo
test. VentaService importado como bean transaccional con repos actuales y usuario/salón fixture;
leer verificación post-rollback desde otra transacción real. Concurrencia usa conexiones/
TransactionTemplate separadas, barrera antes de inserts (no barrera tras flush bloqueante),
timeouts finitos y limpieza sólo de fixtures en contenedor efímero. No DDL objetivo ni SQL file.
No levantar FeelingpilatesApplication.main/.env ni apuntar a DB instalada/live. Tests MockMvc
reusan patrones de ReservaControllerSecurityTest: @WebMvcTest, SecurityConfig y doubles test-only
con principal; servicio mock sólo para boundary HTTP, nunca como safety net del servicio real.

Si el seam o la topología requerida no es viable con estos paths/dependencies, STOP /
`SCOPE_EXPANSION_REQUIRED`; conservar diagnóstico y pedir contrato separado. No cambiar
producción/runtime/pom/resources, suprimir M12 ni bajar una obligación para lograr verde.

## 7. Comandos requeridos y entorno futuro

No ejecutados por el DOCUMENTER. EXECUTOR registra versions, fingerprint antes/después y logs
externos/build reports, sin escribir documentación. JDK 21 y wrapper Maven del repo; Docker
competente con PostgreSQL efímero y api.version=1.44 existente; imagen postgres:16-alpine
disponible antes de validar. Dependencias cacheadas o resolución Maven permitida sólo como
infraestructura de build; tests no llaman red/provider. No secretos live ni DB externa.

En macOS fijar `JAVA_HOME` al JDK21 real identificado por `/usr/libexec/java_home -v 21`; no
reutilizar HOME/CODEX_HOME. Docker context/DOCKER_HOST debe ser el daemon local competente,
verificado por docker version/info; no fijar endpoint ficticio ni deshabilitar checks/reaper.
Los comandos Maven siguientes se ejecutan todos con entorno dummy y paralelismo desactivado:

```sh
java -version
./mvnw -version
docker version
docker info
env -u DB_HOST -u DB_PORT -u DB_NAME -u DB_USER -u DB_PASSWORD STRIPE_SECRET_KEY= STRIPE_PUBLISHABLE_KEY=pk_test_pn14_dummy STRIPE_WEBHOOK_SECRET=whsec_pn14_dummy JWT_SECRETO=pn14-dummy-secret-at-least-thirty-two-bytes COMPRA_PENDIENTE_EXPIRA_MINUTOS=60 TZ=UTC ./mvnw -Djunit.jupiter.execution.parallel.enabled=false -DskipTests=false -Dmaven.test.skip=false test
env -u DB_HOST -u DB_PORT -u DB_NAME -u DB_USER -u DB_PASSWORD STRIPE_SECRET_KEY= STRIPE_PUBLISHABLE_KEY=pk_test_pn14_dummy STRIPE_WEBHOOK_SECRET=whsec_pn14_dummy JWT_SECRETO=pn14-dummy-secret-at-least-thirty-two-bytes COMPRA_PENDIENTE_EXPIRA_MINUTOS=60 TZ=UTC ./mvnw -Djunit.jupiter.execution.parallel.enabled=false -DskipTests=false -Dmaven.test.skip=false -DfailIfNoTests=true -Dtest=PagoIntentoPN14Test,PagoWebhookPN14Test,PagoReconciliacionPN14Test,PagoLecturasPN14Test,VentaServicePN14Test,CatalogoPN14Test,PagosApiPN14Test,VentasCatalogoApiPN14Test,ReservasPN14Test,ReservasApiPN14Test,CompraPersistenciaPN14Test,ReservaServiceCaracterizacionTest,ReservaControllerSecurityTest test
env -u DB_HOST -u DB_PORT -u DB_NAME -u DB_USER -u DB_PASSWORD STRIPE_SECRET_KEY= STRIPE_PUBLISHABLE_KEY=pk_test_pn14_dummy STRIPE_WEBHOOK_SECRET=whsec_pn14_dummy JWT_SECRETO=pn14-dummy-secret-at-least-thirty-two-bytes COMPRA_PENDIENTE_EXPIRA_MINUTOS=60 TZ=UTC ./mvnw -Djunit.jupiter.execution.parallel.enabled=false -DskipTests=false -Dmaven.test.skip=false test
git diff --check
git diff --cached --check
```

Primera suite completa = BASELINE antes de crear tests; focal = después de materializar; última
suite completa = salida. Todas obligatorias; reports tests/failures/errors/skipped/build y
presencia de los once tests nuevos + los dos legacy focales. Cero skipped de tests requeridos,
cero failures/errors; ningún disabled/assumption/optional attempt convierte falta de entorno en
PASS. Si Docker/host no es competente, validación BLOCKED ambiental y plan estático allowlisted
del HOST_VALIDATOR separado, con preflight/fingerprint, produce evidencia antes de aceptación;
no improvisar comandos host desde AgentResult. Baseline fallida detiene escritura; no corregir
tests existentes ni usar el PASS histórico PN13 como baseline técnico.

## 8. Salida, roles y gates futuros

EXECUTOR entrega estado `IMPLEMENTED_IN_REVIEW`, manifest before/after por path/hash,
HEAD/index/staging sin cambios, matriz M01–M12 a métodos/aserciones, logs de baseline/focal/full,
evidencia de constraints/concurrencia/rollback real y límites honestos. Todo main/pom/resources,
existing tests y autoridad documental baseline byte-identical. Builds ignorados son outputs,
no permiso para editar fuente. No documentación por EXECUTOR.

AUDITOR técnico fresh independiente READ_ONLY comprueba delta, compatibilidad, seguridad,
persistencia/concurrencia real, seams sin red, matriz y sensibilidad a regresiones, source
fingerprints y resultados. Requiere P0=P1=0 y SCOPE_GATE / TESTS_GATE /
TECHNICAL_IMPLEMENTATION_GATE PASS; HOST_VALIDATION aplicable a PostgreSQL/Testcontainers
resuelto PASS por evidencia competente, incluida vía HOST_VALIDATOR si necesaria. FALLIDO,
UNKNOWN, SKIPPED o BLOCKED no son PASS. No self-audit ni auditor compartiendo contexto ejecutor.

Tras aprobación técnica, DOCUMENTER separado con su allowlist nueva persiste evidencia técnica
y checkpoint/lifecycle, DOCUMENT_AUDITOR fresh resuelve DOCUMENTATION_GATE; coordinador aplica
gate competente. Publicación de tests requiere etapa y PUBLISHER separado, publication gate y
cierre documental/audit aplicables; no stage/commit/push durante primera implementación.
No broad git add, nuevas branches/worktrees, merge/rebase/cherry-pick del commit e515152 ni
fetch/pull/reset/clean/stash. No auto-fixes de comportamiento legacy ni avance al slice 2.

Rollback fail-closed: detener siguiente slice, conservar baseline dirty ajeno, nuevos tests y
diagnóstico para revisión; nunca reset/clean ni borrar evidencia o corregir código automáticamente.
Sólo una intervención posterior explícita puede corregir paths autorizados. Publicación no es
activación productiva, fence ni cutover. Runtime, Reservas/Programación, F2D/F2E no cambian.

## 9. Doce slices publicados, orden inalterable

| # | Slice exacto PN13 §13 | Estado al materializar este candidato |
| --- | --- | --- |
| 1 | Safety net/caracterización | CANDIDATE CONTRACT / NOT_AUTHORIZED |
| 2 | Orden + snapshot | NOT_AUTHORIZED |
| 3 | Derechos + ledger | NOT_AUTHORIZED |
| 4 | Pago interno + efectivo/transferencia | NOT_AUTHORIZED |
| 5 | Stripe Inbox | NOT_AUTHORIZED |
| 6 | Boundary Reserva-crédito | NOT_AUTHORIZED |
| 7 | Cancelación/asistencia/expiración | NOT_AUTHORIZED |
| 8 | Reembolso | NOT_AUTHORIZED |
| 9 | Outbox | NOT_AUTHORIZED |
| 10 | Email/push | NOT_AUTHORIZED |
| 11 | Disputas/alertas | NOT_AUTHORIZED |
| 12 | Cleanup/cutover | NOT_AUTHORIZED |

First slice PASS no autoriza nextslice: cada uno requiere su nuevo handoff, allowlist, entry,
tests, audits y gates competentes. No inferir permiso del roadmap ni del cierre terminal PN13.

## 10. Estado candidato y siguiente acción documental

```text
PN14 AUTHORIZATION CONTRACT: CANDIDATE / MATERIALIZED / PENDING_FRESH_AUDIT
SELF_AUDIT: NOT_PERFORMED
FRESH_INDEPENDENT_DOCUMENT_AUDIT: PENDING
COORDINATOR_AUTHORIZATION_GATE: PENDING
HANDOFF: NOT_APPROVED / NOT_ACTIVE
PN14 / FIRST_SLICE IMPLEMENTATION: NOT_AUTHORIZED / NOT_STARTED
NEXT CANDIDATE ACTION: FRESH_INDEPENDENT_DOCUMENT_AUDIT ONLY
PN13: PUBLISHED / CLOSED / NOT_REOPENED
TARGET PRODUCTION / RUNTIME / MIGRATION / CUTOVER: NOT_AUTHORIZED / UNCHANGED
```
