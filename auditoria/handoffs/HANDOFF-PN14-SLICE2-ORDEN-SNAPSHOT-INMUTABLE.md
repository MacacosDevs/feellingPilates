# FeelingPilates — PN14 Slice2 — Orden + snapshot inmutable

Status al materializar: `CANDIDATE / PENDING_FRESH_DOCUMENT_AUDIT / NOT_APPROVED / NOT_ACTIVE`.
Tipo: `FUTURE_BOUNDED_IMPLEMENTATION_CONTRACT`; implementación actual `NOT_AUTHORIZED / NOT_STARTED`.
Contrato inmutable tras aceptación por SHA físico externo. Sus marcas candidate conservan este
snapshot; activación posterior sólo por ESTADO/checkpoint, evidencia AJENA y gates competentes.

## 1. Autoridad, entrada y resultado exclusivamente Slice2

Leer AGENTS en orden: README/ESTADO; handoff PN14 activo y canónicos; checkpoints/reviews;
README/WORKFLOW/STATE-MACHINE/GATES/ROLES de orquestación. Autoridad funcional: Dominio
§§13.1–13.7, especialmente productos y contrato histórico §§13.1–13.2. Autoridad técnica:
DA-014/021/022 y PN13 §§4,8,9,13,18; transición: mapa sección Payments & Notifications.
PN14 original handoff §§8–9 exige autorización propia de cada slice. Slice1 checkpoint §9 y
review de cierre conservan evidencia AJENA; no reconstruir ni reescribir análisis anteriores.

PN13 `MATERIALIZED / ACCEPTED / PUBLISHED / CLOSED`; Slice1 `IMPLEMENTED / VALIDATED /
AUDITED / ACCEPTED / PUBLISHED / CLOSED`, workflow terminal PUBLISHED. Entrada conceptual
PN13§13 fila2: Slice1 PASS + máximo Flyway revalidado. Salida futura: tipos/tablas aditivos de
OrdenVenta/Compra/CompraComponenteSnapshot, backfill determinista auditable y lectura histórica
interna independiente de catálogo. No es activación productiva ni sustitución de autoridad.

No Pago, Acreditacion, derechos, ledger, settlement, Stripe/Inbox/Outbox, transferencias,
reembolsos ejecutables, Reservas/asistencia, schedulers, endpoints, DTOs, seguridad o catálogo
writers. No inspección/integración de otros worktrees o candidatos F2E, ni préstamo de
implementación histórica e515152. Slices3–12 requieren autoridad propia; NEW-PN13-017 permanece
OPEN/P2/EDITORIAL/NON_BLOCKING/IMPLEMENTATION_INDEPENDENT, sin fix ni reopen.

## 2. Preflight futuro exhaustivo — STOP antes de cualquier write

```text
WORKTREE: /Users/jesusaldaircruzortiz/Desktop/Feelingpilates/feelingpilates-payments-notifications
BRANCH: pagos/pagos-notificaciones-r1
LOCAL HEAD / CONFIGURED UPSTREAM / SUCCESSFUL LIVE ORIGIN: 1564fb5b2e6f9465b83adce8d6c53a418c99330b
UPSTREAM: origin/pagos/pagos-notificaciones-r1; ahead/behind 0/0
STAGING: EMPTY; baseline index raw SHA256 d19d3b5f32c37fa739275daeefa5426dc758dcc7f5a0e17696edb2b8e371809c
PUBLISHED BASELINE: 466 raw files; manifest SHA256 83cbda445a593d825edbb4501f13dde4d543aaf27bc6c1c550277b77897e3ec4
ESTADO published prefix: 50823 bytes / afb415473e15cbeb3045634efabd200546c8397a6d10f1b4f44f7db3b2513f50
MAPA published prefix: 40907 bytes / 2c155ba748fdcf1e9d4c831521234fde01e4d74ec9f78ed7e47878b2067a5a26
CURRENT ENTRY: NOT_SATISFIED / NO_IMPLEMENTATION_PERMISSION
```

Obtener físicamente branch/HEAD/index/staging/WT, upstream y ls-remote read-only; no fetch/pull.
Manifest completo: paths tracked + untracked no ignorados, orden lexicográfico UTF-8 por path,
concatenación por archivo `path + NUL + sha256(raw bytes) + LF`, SHA256 de concatenación.
Conservar todos los hashes before/after, index físico y staged entries; no atribuir baseline ajeno.

Entrada eventual sólo `LOCAL_UNCOMMITTED_AUDITED_DOCUMENTATION_ENTRY` expresamente activada por
checkpoint§6. Único dirty admitido: los seis docs exactos de checkpoint§5, todos pinneados raw
por manifest físico y binding final independiente; los 466 originales siguen iguales salvo
append-only ESTADO/mapa con prefixes completos anteriores. Handoff/checkpoint/review/manifest
Slice1, toda historia PN13, canónicos de dominio/arquitectura/decisiones y trece tests/helpers
publicados son inmutables. Este EXECUTOR no escribe ninguno de los seis docs.

Todos los CREATE de §7 deben estar ABSENT, incluso candidatos no tracked; no UPDATE permitido
en producción existente. Todo allowed UPDATE documental pertenece a otro rol y exige baseline
raw registrado. Se verifican accepted hash del contrato, cinco core hashes del manifest y SHA
externo del propio manifest contra resultados únicos del materializador/verificador/gate final.
HEAD nuevo, staging no vacío, otro dirty, hash/path faltante, candidato ya existente, gate/evidencia
UNKNOWN/FAIL/SKIPPED, decisión pendiente: `ENTRY_MISMATCH / STOP / NO_WRITES`. No dirty waiver ni
inferir baseline descendiente. Full regression competente BEFORE ANY FUTURE WRITE es obligatoria.

## 3. Tipos, ownership y valores congelados

Todo bajo `com.feelingpilates.pagos.ventas`. Dominio Java21 puro sin Spring/JPA/Stripe/legacy
entities; aplicación depende sólo de dominio y sus propios puertos; infraestructura depende de
ambos y adapta JDBC/transacción Spring. Nada expone el comando en controller ni lo conecta a
PagoService/VentaService. No beans autoactivados: infraestructura sin @Component/@Service ni
configuración productiva; tests construyen wiring explícito. No nueva dependencia/pom/Clock/config.

| Tipo / package suffix | Forma y responsabilidad |
| --- | --- |
| `ImporteMonetario` / dominio | record `(long unidadesMinimas, String monedaIso)`; importe 0..Long.MAX_VALUE, ISO4217 uppercase exacto, Currency.getInstance válido, escala de unidad mínima explícita en política/provenance; no floating point ni catálogo. |
| `PoliticaComercialSnapshot` / dominio | record con records/enums anidados de vigencia, reserva/cancelación, recuperación y reembolso; campos exactos §4, versión de esquema `PN14_S2_1`; valores completos de fuente autoritativa, no defaults de producto. |
| `ProvenienciaSnapshot` / dominio | record de origen/tipo, referencia, actor UUID, instante evidencia, raw fuente, hash raw, regla/version y lista campo→fuente; no privilegio implícito por ser caller Java. |
| `CompraComponenteSnapshot` / dominio | record `(UUID id, UUID compraId, int numero, UUID actividadId, String nombreActividad, int cantidad, String politicaVersion)`; actividad positiva explícita, lista defensiva inmutable; unidad no es crédito universal. |
| `Compra` / dominio | record `(UUID id, UUID ordenId, UUID clienteId, int numeroLinea, UUID productoFuenteId, String nombreProducto, TipoProducto tipoProducto, ImporteMonetario precioVenta, PoliticaComercialSnapshot politica, List<CompraComponenteSnapshot> componentes, ProvenienciaSnapshot proveniencia, Instant congeladoEn, String contratoHash)`; TipoProducto CLASE_INDIVIDUAL/PAQUETE. Una Compra = un producto entero adquirido, sin dividir precio por componente. |
| `OrdenVenta` / dominio | record `(UUID id, UUID clienteId, String scopeKey, ImporteMonetario total, List<Compra> compras, Instant congeladoEn, String payloadHash)`; 1..N compras, números consecutivos1..N, suma checked con Math.addExact, una moneda/cliente; cantidades comerciales no multiplican de nuevo el monto de cada fila compra legacy. |
| `ContenidoSnapshotCanonico` / dominio | serialización/hashes/identidades deterministas §4 y §6; sin acceso a infraestructura. |
| `CongelarOrdenSnapshot` / aplicacion | clase pura; `ejecutar(Entrada)` con record anidado Entrada full authoritative values + IDs de compras existentes + scope/membership esperado + provenance + instante explícito capturado por autoridad backend; retorna Resultado congelado/replay. Valida y delega transacción a puerto; no crea Compra legacy ni confirma dinero. |
| `RepositorioOrdenSnapshot` / aplicacion | puerto `congelar(OrdenVenta propuesta, EvidenciaScope evidencia)` atómico y `buscar(UUID ordenId, UUID clienteId)`; evidencia scope anidada inmutable con listado completo y total autoritativos. |
| `FuenteHistoricaCompra` / aplicacion | puerto de sobres históricos verificados y filas raw legacy; records anidados Fuente/Grupo/Fila, campos faltantes y conflictos explícitos; no puerto a PaqueteRepository. |
| `BackfillOrdenSnapshot` / aplicacion | comando invocado explícitamente sólo en dummy tests en este slice; normaliza, valida grupo, congela o reporta revisión completa §6. |
| `InformeBackfillSnapshot` / aplicacion | resultado inmutable con scope/ids/raw/hashes/faltantes/causas/contadores/importes/congeladas/replays/revisión; nunca omite rechazados. Interfaz anidada Puerto con registrar(InformeBackfillSnapshot) para append/replay; Backfill sólo depende de ese puerto. |
| `ConsultaHistoricaSnapshot` / aplicacion | puerto `buscar(UUID clienteId, UUID ordenId)` y `listar(UUID clienteId)`; sólo proyecciones confiables congeladas ordenadas congeladoEn DESC,id ASC. |
| `CompraHistoricaSnapshot` / aplicacion | record de proyección anidando línea/componentes/políticas/provenance; no entidad legacy ni JSON HTTP actual. |
| `OrdenSnapshotJdbcAdapter` / infraestructura | implementa puerto transaccional, attach aditivo a compra existente y persistencia orden/componentes; único writer de columnas snapshot. |
| `FuenteHistoricaCompraJdbcAdapter` / infraestructura | obtiene únicamente compra raw; sobres confiables entregados como argumento explícito por fuente verificada; jamás inventa policy ni consulta catálogo. |
| `InformeBackfillJdbcAdapter` / infraestructura | implementa InformeBackfillSnapshot.Puerto, append/replay de informe/provenance, sin editar evidencia previa. |
| `ConsultaHistoricaSnapshotJdbcAdapter` / infraestructura | SQL de §6; no Paquete/TipoActividad/Usuario joins para nombres históricos. |

La actual `com.feelingpilates.pagos.entidad.Compra` sigue siendo el ÚNICO mapeo JPA writable de
tabla compra para campos legacy. No segunda @Entity compra, ni herencia de EntidadBase en dominio.
El adapter JDBC usa UPDATE que nombra sólo columnas snapshot sobre IDs existentes; Hibernate
actual no las mapea y sus INSERT/UPDATE preservan las columnas nuevas. No modificar Compra.java:
el mapping aditivo no es necesario para este slice. No setters de snapshot en entidades legacy,
ni replace/save de objeto completo. Tests demuestran intercalación JDBC freeze/JPA estado sin
borrado de snapshot; ninguna validación inmoviliza accidentalmente estado/monto/vigencia legacy.

## 4. Blueprint físico exacto y representación canónica

Esquema actual `public`; únicamente tabla compra expandida y tres tablas nuevas. No política
global editable ni nueva autoridad comercial. UUID nativos; strings text salvo moneda char(3),
hash char(64) lowercase hex; instantes timestamptz(6), UTC ISO8601 Z con exactamente6 decimales
en canon y zona `America/Mexico_City`. Entrada con nanos no divisible por1000 se rechaza, sin
redondeo silencioso. No calcular inicio/expiración de derechos ni llamar reloj del dispositivo.

`orden_venta`: id uuid PK; cliente_id uuid NOT NULL FK usuario(id) RESTRICT; scope_key text
NOT NULL UNIQUE; moneda_iso char(3) NOT NULL; total_unidades_minimas bigint NOT NULL CHECK>=0;
numero_lineas integer NOT NULL CHECK>0; estado_fundacion text NOT NULL CHECK in
('PREPARANDO','CONGELADA'); congelado_en timestamptz(6) NOT NULL; payload_canonico text NOT NULL;
payload_hash char(64) NOT NULL; procedencia_canonica text NOT NULL. UNIQUE(id,cliente_id),
UNIQUE(id,cliente_id,moneda_iso). No pago_settlement_id en esta expansión: DA014 pointer/FK a
Pago de misma orden queda DIFERIDO al slice propio, no enforcement ficticio ni asignación Slice2.

`compra`: conservar cada columna/tipo/default/constraint/index existente. ADD columnas sin
DEFAULT, todas NULL para rows y writers legacy: orden_venta_id uuid, cliente_snapshot_id uuid,
numero_linea integer, producto_fuente_id uuid, nombre_producto_snapshot text,
tipo_producto_snapshot text, precio_venta_unidades_minimas bigint, moneda_snapshot_iso char(3),
congelado_en timestamptz(6), contrato_canonico text, contrato_hash char(64), politica_canonica
text, politica_hash char(64), procedencia_canonica text; más las columnas policy tipadas siguientes:

| Columnas policy en compra | SQL / Java / semántica congelada |
| --- | --- |
| politica_esquema, politica_id, politica_version | text/String no blank; esquema PN14_S2_1; referencia y versión inequívocas. |
| zona_negocio | text/String exactamente America/Mexico_City. |
| vigencia_unidad, vigencia_cantidad, extension_alcance | text enum DIAS/MESES, integer/int>0, text enum MISMA_ACTIVIDAD/TODAS_LAS_ACTIVIDADES_ACTIVAS. |
| reserva_limite_post_vencimiento_dias | integer/int>=0; compromiso antes del inicio del día siguiente, sesión antes de inicio del día vencimiento+N+1; versión incluida. |
| cancelacion_anticipacion_segundos | bigint/long>=0; horas naturales exactas de autoridad expresadas en segundos, igualdad al cutoff válida. |
| cancelacion_cuota_mensual | integer/int>=0; clave cliente+YearMonth de inicio sesión+policyVersion, sin evaluar cuota aquí. |
| recuperacion_unidad, recuperacion_cantidad, recuperacion_politica_id, recuperacion_politica_version | text DIAS/MESES, integer>0, text no blank, text no blank; misma actividad, derecho nuevo, no revive anterior. |
| reembolso_alcance, reembolso_ventana_adicional, reembolso_politica_id, reembolso_politica_version | text PRODUCTO_ENTERO, boolean false, text no blank, text no blank. No política opcional de componentes o ventana automática. |
| unidad_monetaria_exponente | smallint/int0..9; coincide Currency ISO para la moneda, dato explícito validado. |

Política canónica también fija constantes vinculantes: saldo0 no extiende, no acorta, expirado
no revive, ancla Acreditacion.COMPLETADA, límites exclusivos, cancelación libera capacidad,
consumo tardía/cuota agotada/no_asistida, estudio restaura sin cuota, asistencia pendiente sin
timer y resolución ADMIN, reintegro != dinero, refund exige integridad y K=B=C=E=R=0 sin
contención/expiración. Sólo almacenar estos términos; ejecutarlos pertenece a otros slices.
Todas las referencias/versiones/duración/cutoff/cuota provienen del sobre confiable. No completar
retroactivamente una política faltante con los términos actuales PN13. No monto por componente.

CHECK de bundle: si orden_venta_id NULL, TODAS las nuevas columnas NULL; si no NULL, todas NOT
NULL excepto producto_fuente_id (nullable sólo por evidencia histórica de identidad ausente,
registrada en provenance; producto identidad estable alternativa en sobre obligatoria). CHECK
cliente_snapshot_id=usuario_id; precio>=0; numero_linea>0; nombre no blank; enums/valores anteriores;
moneda uppercase `[A-Z]{3}`; policy/schema completos; hashes hex64. UNIQUE(orden_venta_id,
numero_linea); UNIQUE(id,cliente_snapshot_id). FK(orden_venta_id,cliente_snapshot_id,
moneda_snapshot_iso)→orden_venta(id,cliente_id,moneda_iso) RESTRICT, sin cascade. producto_fuente_id
es correlación historical sin FK a catálogo mutable; paquete_id actual conserva su FK existente.
El precio se congela desde compra.monto_centavos observado/pagado y evidencia contractual
concordante, jamás Paquete.precioCentavos; currency raw se conserva en provenance y normaliza
ISO sólo en snapshot. No CHECK permanente precio=monto legacy: posterior cambio legacy no
reescribe precio histórico; defensa de igualdad existe en única transacción de freeze.

`compra_componente_snapshot`: id uuid PK; compra_id uuid NOT NULL; cliente_id uuid NOT NULL;
numero integer NOT NULL>0; actividad_id uuid NOT NULL FK tipo_actividad(id) RESTRICT; nombre_actividad
text NOT NULL no blank; cantidad integer NOT NULL>0; politica_version text NOT NULL no blank;
UNIQUE(compra_id,numero), UNIQUE(compra_id,actividad_id); FK(compra_id,cliente_id)→compra(id,
cliente_snapshot_id) RESTRICT. No cantidades inferidas de nombre/categoria ni universal credit.
El nombre es frozen; actividad FK asegura identidad existente, no dependencia normativa del
estado activo. Prohibir DELETE físico de identidad referenciada; desactivación lógica permitida.

`informe_backfill_snapshot`: id uuid PK; scope_key text NOT NULL; fuente_payload_hash char(64)
NOT NULL; estado text NOT NULL CONGELADA/REPLAY/REQUIERE_REVISION; fuente_raw text NOT NULL;
fuente_raw_hash char(64) NOT NULL; fuentes_canonicas text NOT NULL; faltantes_canonicos text
NOT NULL; causas_canonicas text NOT NULL; compra_ids_canonicos text NOT NULL; conteo_observado
integer NOT NULL>=0; total_observado_unidades_minimas numeric(20,0) nullable; moneda_observada
text nullable; evidencia_en timestamptz(6) NOT NULL; regla_version text NOT NULL PN14_S2_1;
UNIQUE(scope_key,fuente_payload_hash). Totales inválidos se conservan raw, no se fuerzan a bigint
ni se convierten a pago. Informe no representa contrato confiable y no alimenta lectura histórica.

Canonicalización `PN14_S2_1`: secuencia ordenada de pares campo/valor tipados, cada par como
`longitudUTF8(campo):campo + longitudUTF8(valor):valor + LF`; longitud decimal ASCII sin ceros
iniciales; NULL único valor `~NULL` con presencia tipada, strings exactos sin trim/normalización
Unicode, UUID lowercase con guiones, enteros decimal sin +/ceros, boolean true/false, enum exacto,
Instant UTC6. Nombres de campo ordenados por bytes UTF8; listas compras por numeroLinea y
componentes por numero; arrays/mappings provenance por fieldPath UTF8, duplicados rechazados.
Valores estructurados usan esa misma codificación recursiva y cuentan bytes del contenido
completo; cadenas literales ~NULL se distinguen por tipo STRING, NULL por tipo NULL. Cada valor
incluye prefijo tipo `STRING:`, `UUID:`, `LONG:`, `INT:`, `BOOL:`, `INSTANT:`, `ENUM:`, `LIST:` o
`OBJECT:`; tipo NULL codifica `NULL:~NULL`. Hash SHA256 lowercase del UTF8 canónico. Contrato
incluye todos campos raíz/policy/componentes/provenance, excepto su propio hash; order incluye
todos contratos y membership/total/instante, excepto su hash. Policy hash sólo policy completa.

DB verifica `encode(sha256(convert_to(canonico,'UTF8')),'hex')=hash`, sin pgcrypto/pom nuevo;
funciones SQL PN14_S2 en V49 reconstruyen policy/contrato/order con el MISMO encoder/orden,
comparan campos tipados/canon/hash y componentes. No permitir canon/hash válidos con contenido
tipado distinto. V49 sólo funciones/constraints/triggers de estas cuatro tablas.

Inmutabilidad DB/application: insertar orden PREPARANDO; attach todas raíces, insertar componentes,
validar y sellar CONGELADA antes de commit. Sólo transición PREPARANDO→CONGELADA sin modificar
payload; trigger deferred exige CONGELADA, número1..N consecutivo, conteos y suma exacta checked
numeric antes de cast bigint, moneda/cliente únicos, componente1..N no vacío y policy/canon/hash
coherentes. Ningún estado PREPARANDO puede commit. Orden sellada prohíbe cualquier UPDATE/DELETE;
componentes prohíben UPDATE/DELETE y INSERT después del sello; raíces sólo NULL→bundle completo
en orden PREPARANDO, una vez. Después no actualizar/borrar sus nuevas columnas ni id/usuario_id
de raíz congelada. DELETE raíz congelada restringido. Roots sin bundle conservan DELETE/UPDATE
legacy normal. Updates sólo de columnas LEGACY financieras/estado/fecha_expiracion/motivo/etc.
permitidos y no cambian contrato; actualizado_en legacy permitido. Informe es append-only.
Metadata nueva de procesamiento editable: NINGUNA en Slice2. No TRUNCATE permitido en las tablas
nuevas ni compra con snapshots: guard triggers; no claims contra superuser/DDL administrado.

## 5. Flyway condicional y compatibilidad de expansión

Inspección actual: 50 archivos físicos versionados, máximo V47 existente
`src/main/resources/db/migration/V47__programacion_ajustes_fecha.sql`. Reservas de nombres sólo
condicionadas al mismo baseline verificado, sin reserva histórica PN13:

1. CREATE `src/main/resources/db/migration/V48__pn14_slice2_orden_snapshot_expand.sql`: tablas,
   columnas NULL seguras, tipos/checks/FKs/índices propios; cero backfill live o modificación de rows.
2. CREATE `src/main/resources/db/migration/V49__pn14_slice2_snapshot_inmutabilidad.sql`: encoder,
   validadores diferidos y guards de inmutabilidad/atomicidad exclusivamente foundation anterior.

En FUTURE IMPLEMENTATION ENTRY volver a enumerar y hashear TODOS los50 y máximo. Debe seguir
V47, ambos filenames/versiones ausentes y checksums baseline idénticos. Cambio de máximo,
ocupación V48/V49, menos/más migraciones o HEAD cambiado: `AUTHORIZATION_MISMATCH / STOP /
NO_WRITES`; no renumerar silenciosamente. Nueva reserva exige nuevo contrato/gate. Las dos son
el mínimo separado expand/enforcement de este blueprint; no V50 ni editar históricas ni permisos.
No ejecución SQL productiva, scheduling ni automatic backfill al startup/Flyway. Required real
PostgreSQL fresh/upgrade tests sólo dummy. Writers viejos INSERT sin columnas nuevas y actualizan
campos antiguos; constraints snapshot sólo se activan sobre bundles completos explícitos.

## 6. Freeze/backfill/provenance/lectura interna deterministas

Comando de fundación no es purchasewriter activo. Acepta full authoritative envelope, sólo IDs
de Compra ya existente y whole order scope. Nunca carga precio/policy/nombre/composición desde
catálogo actual. Puede congelar contrato sin pretender PAGADA=ACREDITADA ni crear beneficios.
Para precio histórico debe existir importe adquirido/pagado verificable concordante con monto
persistido; estado actual pagada por sí solo no prueba términos históricos completos.

Fuentes admisibles: contrato/ticket exportado inmutable contemporáneo identificando compra,
cliente, moneda, productos/composición/políticas/versiones; evento/archive versionado cuyo hash,
fecha efectiva y membership ligan inequívocamente la venta; evidencia comercial firmada/verificada
por responsable autorizado que aporta esos valores y la provenance documental original. Pago
Stripe/receipt por sí solo acredita importe/moneda, NO policy/componentes/nombre/tipo. Raw compra
sirve para id/usuario/grupo/numero/importe y correlaciones, NO historial comercial perdido. Git
seed/migración o configuración actual no prueba aplicación histórica a esa compra. Sin fuente
trusted de cada campo no hay snapshot confiable. No existe inventario/data audit live en este Run.
Verificación de sobres registra tipo/ref/raw SHA256/actor/instante/regla, hash y referencia de cada
campo; si fuentes confiables discrepan, REQUIERE_REVISION, nunca prioridad arbitraria/latest wins.

Scope estable: `LEGACY_GRUPO:<grupoUUID>` si grupo no null; `LEGACY_COMPRA:<compraUUID>` si null.
UUID orden = UUID.nameUUIDFromBytes(UTF8(`PN14_S2_1:ORDEN:`+scope)); UUID componente = mismo
algoritmo con `PN14_S2_1:COMPONENTE:<compraUUID>:<numero>`. Hashes source y contractual aparte:
no UUID aleatorio/now/recuento variable en replay. Número para nullgroup=1 sólo si evidencia
confirma producto único y no group perdido; numero_item anómalo requiere revisión, no guessing.
Cada fila compra representa una unidad de producto según fuente de venta comprobada; cantidad
de clases de sus componentes exige sobre histórico explícito. No extraer '4' del nombre ni elegir
Pilates genérico desde categoria. Compra original ID se conserva.

Grupo no null: comprobar listado completo expected IDs/conteo/números1..N y total/currency del
sobre contra TODAS las filas del grupo. Mismo usuario canónico y ISO currency, no numeros
null/duplicados/gaps, producto entero por fila, sumas exactas >=0<=Long.MAX_VALUE y no overflow;
validar límites de cada importe y unidad mínima. Contiguidad sola no prueba grupo completo:
sin evidencia trusted de membership/conteo/total, bloquear grupo completo. Un receipt de pago
no crea entidad Pago ni settlement. Invalid/incompleto/inconsistente deja todas sus raíces NULL;
informe REQUIERE_REVISION identifica scope, exact raw source/row IDs/hashes, missing fieldPaths,
causas tipadas FUENTE_AUSENTE/FUENTE_CONTRADICTORIA/GRUPO_INCOMPLETO/CLIENTE_INCONSISTENTE/
MONEDA_INCONSISTENTE/NUMERACION_INVALIDA/IMPORTE_INVALIDO/PAYLOAD_CONTRADICTORIO.

Serialización: transacción real única por scope; advisory xact lock derivado SHA256(scope) primeros
64bits signed big-endian (colisiones serializan, no alteran identidad); LOCK TABLE compra IN SHARE
ROW EXCLUSIVE MODE antes de leer membership bloquea temporalmente INSERT/UPDATE legacy
concurrentes que no toman advisory lock. Luego orden existente FOR UPDATE, compras ordenadas
UUID FOR UPDATE; releer scope/membership/hash/importe tras lock, validar sobre, attach+components+
sello+informe en misma transacción. Rollback completo ante cualquier constraint/hash/conflicto.
No SKIP LOCKED, partial group commit o TOCTOU. Lock timeout/error se reporta y conserva cero
efectos; retry explícito misma clave/hash. El lock tabla no activa un fence permanente: después
del commit los writers viejos siguen; un agregado tardío al mismo grupo produce mismatch en
backfill posterior y revisión, nunca reescribe order ya congelada.

Scope/id ya sellado + mismo contenido íntegro/hash → devuelve ganador estable sin nuevos rows
ni timestamps. Payload distinto bajo misma clave/id → fail-closed sin sustituir winner; informe
contradictorio append-only permitido en transacción de reporte separada después de rollback,
preserva raw y REQUIERE_REVISION. Intentos iguales informe unique scope+sourcehash hacen replay
sin tocar anterior. Dos concurrentes producen una única orden/componente set y mismo resultado;
auditar conteos antes/después, sumas expected/observed/frozen/revisión, filas sin bundle, atomicidad.
Si nuevos sobres corrigen faltantes de scope no congelado, nuevo sourcehash deja informe previo
intacto; sólo valores históricos trusted completos permiten freeze. No corregir snapshot sellado.

Required synthetic fixture demuestra: sobre contemporáneo con compra12345MXN y policy/composición
firmadas dummy; catálogo actual99999 y composición diferente; freeze devuelve12345 y términos
del sobre. Otro raw legacy sin sobre reporta todos los faltantes y deja NULL; grupo truncado o
cantidades desconocidas no se congela. Fixture es sintética, no claim de data audit real.

Consulta interna SQL proyecta orden_venta JOIN compra ON orden/id+cliente JOIN
compra_componente_snapshot por compra/id+cliente, WHERE estado_fundacion='CONGELADA' y cliente
parameter canónico; fields exclusivamente frozen, sin JOIN/read Paquete/PaqueteActividad/
TipoActividad/Usuario para nombres/políticas. Incluye origen/hashes/versiones y total/líneas.
No fallback catálogo para NULL: se informa no_snapshot o resultado confiable ausente y reporte
de revisión accesible internamente. DTO actual/JSON HTTP no recibe snapshots ni cambio semántico:
quirks M06/M08, categoría null/NPE, nombres actuales, reuso/transferencia/refund legacy siguen.
Switch futuro de consumidores exige su propio gate/API compatibility/data audit/cutover; opcional,
no parte de Slice2. No automatic scheduled backfill ni liveDB inspection/execution.

## 7. Allowlist FUTURA finita — sólo CREATE, ninguna UPDATE existente

Cada línea es path exacto, modo y razón; no globs, helpers extra ni archivos implícitos.

| Path | Modo | Razón |
| --- | --- | --- |
| src/main/java/com/feelingpilates/pagos/ventas/dominio/ImporteMonetario.java | CREATE | importe checked/ISO |
| src/main/java/com/feelingpilates/pagos/ventas/dominio/PoliticaComercialSnapshot.java | CREATE | policy frozen completa y nested enums/records |
| src/main/java/com/feelingpilates/pagos/ventas/dominio/ProvenienciaSnapshot.java | CREATE | fuente trusted por campo |
| src/main/java/com/feelingpilates/pagos/ventas/dominio/CompraComponenteSnapshot.java | CREATE | actividad/cantidad immutable |
| src/main/java/com/feelingpilates/pagos/ventas/dominio/Compra.java | CREATE | contrato raíz Java puro |
| src/main/java/com/feelingpilates/pagos/ventas/dominio/OrdenVenta.java | CREATE | agregado cliente/total/membership |
| src/main/java/com/feelingpilates/pagos/ventas/dominio/ContenidoSnapshotCanonico.java | CREATE | canon/hash/key compartidos |
| src/main/java/com/feelingpilates/pagos/ventas/aplicacion/CongelarOrdenSnapshot.java | CREATE | full input validated foundation |
| src/main/java/com/feelingpilates/pagos/ventas/aplicacion/RepositorioOrdenSnapshot.java | CREATE | puerto freeze atómico |
| src/main/java/com/feelingpilates/pagos/ventas/aplicacion/FuenteHistoricaCompra.java | CREATE | puerto histórico sin catálogo |
| src/main/java/com/feelingpilates/pagos/ventas/aplicacion/BackfillOrdenSnapshot.java | CREATE | backfill explícito/revisión |
| src/main/java/com/feelingpilates/pagos/ventas/aplicacion/InformeBackfillSnapshot.java | CREATE | reporte completo |
| src/main/java/com/feelingpilates/pagos/ventas/aplicacion/ConsultaHistoricaSnapshot.java | CREATE | puerto histórico interno |
| src/main/java/com/feelingpilates/pagos/ventas/aplicacion/CompraHistoricaSnapshot.java | CREATE | proyección sin HTTP |
| src/main/java/com/feelingpilates/pagos/ventas/infraestructura/OrdenSnapshotJdbcAdapter.java | CREATE | JDBC única escritura snapshot/transacción |
| src/main/java/com/feelingpilates/pagos/ventas/infraestructura/FuenteHistoricaCompraJdbcAdapter.java | CREATE | raw compra y sobres explícitos |
| src/main/java/com/feelingpilates/pagos/ventas/infraestructura/InformeBackfillJdbcAdapter.java | CREATE | evidencia append/replay |
| src/main/java/com/feelingpilates/pagos/ventas/infraestructura/ConsultaHistoricaSnapshotJdbcAdapter.java | CREATE | SQL frozen independiente |
| src/main/resources/db/migration/V48__pn14_slice2_orden_snapshot_expand.sql | CREATE CONDITIONAL §5 | expansión foundation |
| src/main/resources/db/migration/V49__pn14_slice2_snapshot_inmutabilidad.sql | CREATE CONDITIONAL §5 | invariantes foundation |
| src/test/java/com/feelingpilates/pagos/ventas/OrdenSnapshotDominioTest.java | CREATE | T01–T03 |
| src/test/java/com/feelingpilates/pagos/ventas/PoliticaSnapshotCanonicoTest.java | CREATE | T04 |
| src/test/java/com/feelingpilates/pagos/ventas/OrdenSnapshotPersistenciaTest.java | CREATE | T05–T07 |
| src/test/java/com/feelingpilates/pagos/ventas/OrdenSnapshotTransaccionTest.java | CREATE | T08 |
| src/test/java/com/feelingpilates/pagos/ventas/OrdenSnapshotIdempotenciaTest.java | CREATE | T09 |
| src/test/java/com/feelingpilates/pagos/ventas/OrdenSnapshotConcurrenciaTest.java | CREATE | T10 |
| src/test/java/com/feelingpilates/pagos/ventas/BackfillOrdenSnapshotTest.java | CREATE | T11–T12 |
| src/test/java/com/feelingpilates/pagos/ventas/BackfillOrdenSnapshotPostgresTest.java | CREATE | T13 |
| src/test/java/com/feelingpilates/pagos/ventas/OrdenSnapshotMigracionTest.java | CREATE | T14–T15 |
| src/test/java/com/feelingpilates/pagos/ventas/ConsultaHistoricaSnapshotTest.java | CREATE | T16 |
| src/test/java/com/feelingpilates/pagos/ventas/VentasSnapshotArquitecturaTest.java | CREATE | T17 |
| src/test/java/com/feelingpilates/pagos/ventas/PN14Slice2Fixtures.java | CREATE | fixtures synthetic y test-only DB/wiring, ningún helper productivo |

Total32 paths:18 Java main+2SQL+11tests+1helper. Existing source/tests/pom/wrappers/resources/
application/security/controllers/DTOs/services/catalogwriters/config unchanged. No actualización
de Compra.java requerida/autorizada. Si falta path indispensable, STOP y nuevo scope, no improvisar.

## 8. Matriz FUTURA obligatoria y comandos exactos

| ID / nuevo testclass | Aserciones críticas mínimas |
| --- | --- |
| T01 / OrdenSnapshotDominioTest | lists defensive immutable, root/component identity immutable, mixed whole product; mutation catalog no reinterpreta precio/policy/cantidades. |
| T02 / OrdenSnapshotDominioTest | cliente/order/currency mismatch, duplicate/gap line y componente/activity, cantidad0/negativa, precio negativo/overflow/invalidISO rechazados sin side effects. |
| T03 / OrdenSnapshotDominioTest | compra paid12345 vs catálogo99999 devuelve12345; total checked por fila una unidad, no doble multiplicación ni distribución a componentes. |
| T04 / PoliticaSnapshotCanonicoTest | todas policies/versiones necesarias, NULL/blank/source absent rechazados; ISO exponent, DIAS/MESES, cutoff equality semantics sólo storage; unicode UTF8/order/instantUTC6 hash estable, cambio un campo cambia hash, tipo/canon mismatch falla. |
| T05 / OrdenSnapshotPersistenciaTest | SQL UPDATE/DELETE/root+order/components y INSERT postseal/TRUNCATE rechazados; nombres/precio/cantidad/policy congelados idénticos tras rename/deactivate catalog/activity. |
| T06 / OrdenSnapshotPersistenciaTest | PostgreSQL composite FK cliente/order/currency mismatch, lineunique, actividadFK, cantidad>0, no cascade, deletion referenciadas rechazado; SQL directo prueba constraints, no sólo mocks. |
| T07 / OrdenSnapshotPersistenciaTest | partial NULL bundle y policy/canon/hash incoherente fail; old JPA financial/state/expiration/motivo update preserves snapshot, oldwriter sin bundle INSERT/UPDATE/DELETE sigue igual. |
| T08 / OrdenSnapshotTransaccionTest | proxied real tx congela primer root/component y failure segunda línea rollback TODO desde nueva conexión; no PREPARANDO committed, wholecart scope no partial. |
| T09 / OrdenSnapshotIdempotenciaTest | sameid/key exactpayload replay sameUUID/hash/Instant/conteos; contradiction failclosed winner unchanged, reporte raw correcto; ninguna acreditación/Pago insert. |
| T10 / OrdenSnapshotConcurrenciaTest | dos conexiones/pids/barrera antes de lock, boundedtimeout: freeze/backfill scope mismo una stablewinner, resultado replay estable; legacy insert racing lock membership revalidation bloquea inconsistencia. |
| T11 / BackfillOrdenSnapshotTest | trusted synthetic sobre historical distinto actualcatalog produce frozen sólo fuente; receipt financiero incompleto no inventa policies/cantidades/tipo. |
| T12 / BackfillOrdenSnapshotTest | missingterms/ambiguoushist/corruptgroup/clientcurrency/gaps/incomplete membership/unknownquantity/amountoverflow REQUIERE_REVISION exactraw+missingfieldpaths, cero snapshots guessed; fuentes contradictorias sin latest wins. |
| T13 / BackfillOrdenSnapshotPostgresTest | repeat/concurrent backfill realPG mismoskeys/hash/conteos/importe, conflict no overwrite, grouprollback, informes append sin perder rechazados; correctionsource nueva sólo scope unfrozen. |
| T14 / OrdenSnapshotMigracionTest | fresh migrations50+2 maxV49 y constraints reales PG16, validators/canon parity; todo contexto actual compatible, no skips. |
| T15 / OrdenSnapshotMigracionTest | migrar dummy sólo target47, insertar varianteslegacy y raw fingerprints de cada columna, upgrade48/49 mantiene TODAS rows/values/defaults/indexes originales; legacywriters antes/después iguales, ninguna ejecución de backfill automática. |
| T16 / ConsultaHistoricaSnapshotTest | JDBC real snapshot history idéntica tras catalog replace/deactivate, consulta no Paquete/PaqueteActividad/TipoActividad/Usuario access; NULL no fallback y ownership cliente no leakage; JSON público legacy tests M06/M08 unchanged. |
| T17 / VentasSnapshotArquitecturaTest | meaningful dependency/import/bytecode rule sin librería nueva: dominio java only, app sólo dominio/puertos, infra inward; no JPA compra competidora, no publiccontroller/scheduler/config/wiring/payment dependency ni copia legacy. |
| T18 / FULL + focal Slice1 existente | M01–M12 íntegros byte-identical y fullregression, sin cambiar quirks/reuso/transferencia/refund/HTTP. |

Tests reales no disabled/assumptions/optionalDocker. Unit no mocks que sustituyan el objeto bajo
prueba. PostgreSQL16-alpine efímero dummy requerido fresh y upgradeV47; independenttx/connections,
hash/count/amount verification; no live DB/data audit. Evidencia logs/XML por class/method y
assertion mapping T01–T18, no claim mutation-testing si sólo análisis. Baseline falla→NO_WRITES.

JDK21 real macOS `export JAVA_HOME="$(/usr/libexec/java_home -v 21)"`; no HOME/CODEX_HOME,
Clock/pom/config/MockMaker/wrapper edits. Verificar Docker host/context competente y api.version
1.44 existente; no endpoint ficticio/desactivar reaper. Mismo entorno dummy PN14:

```sh
java -version
./mvnw -version
docker version
docker info
env -u DB_HOST -u DB_PORT -u DB_NAME -u DB_USER -u DB_PASSWORD STRIPE_SECRET_KEY= STRIPE_PUBLISHABLE_KEY=pk_test_pn14_dummy STRIPE_WEBHOOK_SECRET=whsec_pn14_dummy JWT_SECRETO=pn14-dummy-secret-at-least-thirty-two-bytes COMPRA_PENDIENTE_EXPIRA_MINUTOS=60 TZ=UTC ./mvnw -Djunit.jupiter.execution.parallel.enabled=false -DskipTests=false -Dmaven.test.skip=false test
env -u DB_HOST -u DB_PORT -u DB_NAME -u DB_USER -u DB_PASSWORD STRIPE_SECRET_KEY= STRIPE_PUBLISHABLE_KEY=pk_test_pn14_dummy STRIPE_WEBHOOK_SECRET=whsec_pn14_dummy JWT_SECRETO=pn14-dummy-secret-at-least-thirty-two-bytes COMPRA_PENDIENTE_EXPIRA_MINUTOS=60 TZ=UTC ./mvnw -Djunit.jupiter.execution.parallel.enabled=false -DskipTests=false -Dmaven.test.skip=false -DfailIfNoTests=true -Dtest=OrdenSnapshotDominioTest,PoliticaSnapshotCanonicoTest,OrdenSnapshotPersistenciaTest,OrdenSnapshotTransaccionTest,OrdenSnapshotIdempotenciaTest,OrdenSnapshotConcurrenciaTest,BackfillOrdenSnapshotTest,BackfillOrdenSnapshotPostgresTest,OrdenSnapshotMigracionTest,ConsultaHistoricaSnapshotTest,VentasSnapshotArquitecturaTest test
env -u DB_HOST -u DB_PORT -u DB_NAME -u DB_USER -u DB_PASSWORD STRIPE_SECRET_KEY= STRIPE_PUBLISHABLE_KEY=pk_test_pn14_dummy STRIPE_WEBHOOK_SECRET=whsec_pn14_dummy JWT_SECRETO=pn14-dummy-secret-at-least-thirty-two-bytes COMPRA_PENDIENTE_EXPIRA_MINUTOS=60 TZ=UTC ./mvnw -Djunit.jupiter.execution.parallel.enabled=false -DskipTests=false -Dmaven.test.skip=false -DfailIfNoTests=true -Dtest=PagoIntentoPN14Test,PagoWebhookPN14Test,PagoReconciliacionPN14Test,PagoLecturasPN14Test,VentaServicePN14Test,CatalogoPN14Test,PagosApiPN14Test,VentasCatalogoApiPN14Test,ReservasPN14Test,ReservasApiPN14Test,CompraPersistenciaPN14Test,ReservaServiceCaracterizacionTest,ReservaControllerSecurityTest test
env -u DB_HOST -u DB_PORT -u DB_NAME -u DB_USER -u DB_PASSWORD STRIPE_SECRET_KEY= STRIPE_PUBLISHABLE_KEY=pk_test_pn14_dummy STRIPE_WEBHOOK_SECRET=whsec_pn14_dummy JWT_SECRETO=pn14-dummy-secret-at-least-thirty-two-bytes COMPRA_PENDIENTE_EXPIRA_MINUTOS=60 TZ=UTC ./mvnw -Djunit.jupiter.execution.parallel.enabled=false -DskipTests=false -Dmaven.test.skip=false test
git diff --check
git diff --cached --check
```

Primera full antes de cualquier write futuro; nuevo focal, viejo focal y finalfull obligatorios.
Registrar baseline/full counts reales, once nuevas clases presentes, failures/errors/skips0,
requiredSkips0. Histórico Slice1 baseline590/focal67/full638 no sustituye baseline futuro.
Docker ambiental BLOCKED tiene HostValidator separado con plan estático exactamente estos
comandos/testpaths y fingerprints; no verde por skip ni otro entorno/H2. Este Run documental
no ejecuta tests/host: `NOT_APPLICABLE / NOT_EXECUTED`, nunca PASS.

## 9. Gates/lifecycle/rollback futuros, sin autoactivación

Este contrato sólo candidato. Initial DOCUMENT_AUDITOR fresh task_8984e1bf2a90 y coordinator
task_42c555e7bf1d/gate_e031cf779ca1: APPLICABLE/PENDING. Luego DOCUMENTER separado persiste review
AJENO/manifest exactos checkpoint§5 y activación condicional; fresh final verifier task_807e559a0955
y coordinator task_fe41eb6e6c0d/gate_6f54babec421: APPLICABLE/PENDING. No resultado futuro inventado.
Veredictos requeridos: initial SLICE2_AUTHORIZATION_AUDIT=PASS (alias DOCUMENTATION_AUDIT permitido),
final SLICE2_FINAL_AUTHORIZATION_VERIFICATION=PASS (alias FINAL_SLICE2_AUTHORIZATION_MATERIALIZATION_VERIFICATION
permitido); checkpoint§6 exige los nombres primarios reales del Task spec, no sólo alias.
Current implementation/tests/host NOT_APPLICABLE; current auto_publish=false, publication
NOT_PERFORMED/NO_PERMISSION, ninguna autorización publicada fabricada. Final gate acepta bindings
externos sin selfhash ni edición posterior; detalles checkpoint§6. Aceptación inicial no ejecuta código.

Después de entrada final real: EXECUTOR32paths → baseline/focused/full+realPG → fresh technical
AUDITOR → coordinador SCOPE/TESTS/IMPLEMENTATION/HOST gates; corrección acotada separada y
re-audit si autorizada → DOCUMENTER con nuevo scope propio → fresh DOCUMENT_AUDITOR/gate →
PUBLICATION/CLOSURE únicamente si son separadamente autorizadas, PUBLISHER/verifier/cierreaudit
independientes. Ningún commit/push inferido. Publicar no activa path productivo, fence ni cutover.

STOP ante autoridad/productdecision/materialinconsistency no derivable; ASK al coordinador
antes de writes, no producto inventado. Rollback antes de cutover: detener comando interno no
usado, conservar oldwriter, rows/evidencia/dirty; no reset/clean/stash ni delete de historia.
No schema downgrade destructivo, live backfill/cutover. Slice3 requiere handoff/gate y audit de
confianza/data real propios; snapshots synthetic o filas REQUIERE_REVISION no lo autorizan.
