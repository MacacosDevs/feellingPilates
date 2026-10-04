# F2E R2 — diseño de integración del lector Turno legacy

Estado: `MATERIALIZED_CANDIDATE / PENDING_FRESH_REAUDIT_AND_GATE`.
Tipo: `DESIGN / RESEARCH`, corrector documental dedicado, **no auditor**.
Run: `run_df3cbaebd5d7`; corrector Task: `task_a2e7ce748158`; Dispatch: `ctx_ef6c945da2a1`.
Materialización inicial histórica: `task_c195cda67de7 / ctx_c45b806cebbf`.
Fecha: 2026-09-16. Implementación: `NOT_AUTHORIZED`; publicación: `NOT_EXECUTED`.

## 1. Autoridad, baseline y alcance de esta candidate

Raíz física: `/Users/jesusaldaircruzortiz/Desktop/Feelingpilates/feelingpilates`.
Branch `operacion/excepciones-horario-fecha`; HEAD/upstream/origin live inmutables
`6c2eacc870499e74ead74c1851630f9f53b1c676`. Entrada verificada: tracked CLEAN,
index EMPTY, untracked0, ahead/behind0/0. Baseline externo:
`/tmp/feelingpilates-f2e-r2-design.iUQKrV/BASELINE.json`, 460 archivos tracked.
Inventory SHA `d7964ab357d282a21b3289f0511f6289a3e32f8dd0a13a615f8a4dd93384d5cd`;
index SHA `b053b254ae09434071a7dcd9922c2d2f3dbc6edde0ddaa2935c763432527e682`;
refs SHA `e75a76fb06f4b02f953961c777ee0d8c776b1f6cbcb980b69dd98656ff097e8b`.
El baseline preexistente R1 no pertenece a este documenter ni al corrector.
El corte CLEAN/untracked0 anterior es la entrada histórica del materializador;
la entrada del corrector conserva estos dos candidatos untracked, index EMPTY y
tracked CLEAN, sin atribuir su creación a esta corrección. También conserva los
660 hashes ignored de `IGNORED-BASELINE.json` y R1 exact21 de `R1-IDENTITY.json`.

Corrección cronológica dentro del mismo scope, stage documental `CORRECT` de WORKFLOW:
audit independiente `task_1c407b9a88a1 / ctx_3c018d86ea0f`, reporte externo
`/tmp/feelingpilates-f2e-r2-design.iUQKrV/AUDIT-R2-DESIGN.md`, SHA
`bda484b6140405491740b6c193fd120d06dbd3550cc26ee458866500f489109a`,
verdict original **FAIL — F2E R2 DESIGN AUTHORITY REMAINS INCOMPLETE**.
Sus P1 `R2-DESIGN-B-01` y `R2-DESIGN-AB-02` motivan únicamente las correcciones
de metadata/captura y enumeración de rechazos de §§2/4–7 y referencias dependientes.
Preimages externas preservadas `DESIGN-PRE-CORRECTION.md` SHA
`3a6eb4e4b38ff90b66e85680c407ae808dbba471b21d4be79dd2c321cd77afaa` y
`RESEARCH-HANDOFF-PRE-CORRECTION.md` SHA
`e9c56d7beb47f8558a756fe2e132aa3874069fd902cbb0d26270646256c1bb83`.
Materializador y auditor iniciales completados/liberados; sus reportes originales
no se reescriben como PASS. Esta corrección no cierra competentemente los P1;
re-audit fresh independiente y gate de los bytes corregidos siguen pendientes.

`D` designa exclusivamente
`auditoria/fase-2e-diseno-adapters-read-only-snapshot-consistency.md`, leído completo,
SHA `6c72cba1f83fbc2bcf3b3219d8252d2410ec482e30ae85d30fd2eeb04e8883d8`.
`S` designa `auditoria/fase-2e-identidad-semantica-detector-read-only.md`.
ESTADO señala `ACTIVE HANDOFF: NINGUNO`; el handoff R1 está consumido y NOT_ACTIVE.
Sus PENDING del corte documental se conservan: el cierre posterior corroborado por
preflight `run_b000b8a5b647` incluye `run_fb92631a2300 / gate_7e4a087bd1ce PASS`.
No se reabre R1 ni se atribuye ese PASS a esta candidate.

Autorización concreta precedente: `run_b000b8a5b647 / task_ab1d650831a4 /
gate_236c6b0bdf43`, resolved/PASS, semántica literal:
`PASS — F2E R2 PREFLIGHT COMPLETE / READY_FOR_R2_DESIGN_AUTHORITY_MATERIALIZATION`.
La evidencia completa y sus hashes están en el handoff de investigación asociado.
Ese gate y el despacho actual permiten sólo esta materialización documental acotada.
El handoff asociado registra provenance/profile de esta autorización; no crea aprobación,
handoff ACTIVE ni autoridad de implementación.

Esta candidate precisa tres gaps de integración R2. Las cláusulas anteriores de D8,
D12.1–12.3 y su PASS histórico permanecen. Las decisiones nuevas aquí identificadas
como `R2-ID`, `R2-SQL`, `R2-RR`, `R2-CHECKSUM`, `R2-ARCH` y `R2-PROFILE` quedan
**pendientes de auditoría fresh y gate competente**. No se presentan como CLOSED,
PUBLISHED o IMPLEMENTATION_AUTHORIZED. Tras aceptación competente constituirían la
instancia R2 del diseño, con precedencia sólo para estos gaps; D36–37 sigue R1-only
salvo la reutilización expresamente delimitada de fundamentos.

## 2. Contrato preservado de adquisición de sources

Port conceptual único:
`readForDate(LegacyTurnReadContext context, LegacyTurnScope(Set<UUID> salonIds, LocalDate fecha))`.
R2 recibe fecha exacta y salones no-null/no-vacíos/sin null; no recibe miembro,
instructor, actividad, reserva, rango, candidates ni flags elegibles por caller.
`dayOfWeek = (short)(fecha.getDayOfWeek().getValue() % 7)`; domingo0.
Sólo turnos activos: RECURRENTE del weekday derivado y EXCEPCION/CANCELACION de
fecha exacta. No se aplica la selección productiva de prioridad entre tipos legacy.
Se observan todas sus filas relevantes.

Un executor nativo dedicado plain, `LegacyTurnProjectionQueryExecutor`, produce
`LegacyTurnMemberRow` de once campos y `LegacyAssignmentRow` de cinco, según §5.
Primera query header+LEFT JOIN membership; segunda query todos los assignments de
**todos** los turnIds derivados, incluidos no-miembros. Correlación exclusiva turnId.
Sin join cartesiano miembros×assignments, entities, repos legacy o navegación lazy.
Sólo con cero filas reales de MEMBERS se omite query2 y el resultado es vacío válido,
sin `IN ()`. Filas inválidas sin parent UUID usable omiten query2 y abortan; nunca
se convierten en éxito vacío. Con otros parent UUIDs usables se enumeran sus assignments.

`LegacyTurnReadSet` tiene **exactamente un componente**:
`sources: List<GenericSourceSnapshot>`. Lista required, copia defensiva, 0..N;
no nulls, identities duplicadas, entidades, proxy, stream JPA, DetectorResult,
candidate, operational artifact ni metadata mutable. Un read set exitoso contiene
todos los sources representables; un fallo no contiene read set. El owner test-only
retiene aparte evidencia transaccional/capture, también para éxito vacío; no se añade
un sink de reporte ni se altera este envelope para alojarla.

Assignment atom: PK física `(turno_id, usuario_id, tipo_actividad_id)`;
identity literal `urn:f2e:legacy-assignment:v1:turn=<turnId>:member=<instructorId>:activity=<activityId>`.
Gap identity literal `urn:f2e:legacy-gap:v1:turn=<turnId>:member=<memberId|ABSENT>:marker=<marker>`.
UUID textual lower-case. Gap sin miembros usa marker de identity `ABSENT_MEMBER`;
gap de miembro sin assignment usa `ABSENT_ASSIGNMENT`, aunque sus markers incluyen
también ABSENT_ACTIVITY. Gap no es PK, serie, target o identidad de negocio.
`recordIds` conserva posiciones físicas D12.3: `[turnId]`, `[turnId,memberId]`,
`[turnId,instructorId,activityId]` respectivamente; no inventa assignmentId.
No se reordena esa lista por valor ni se elimina una coincidencia UUID entre roles.

Sea M el set de miembros, A(m) las filas de ese miembro y O las assignments no-miembro:

```text
K = 1                                  si M vacío y O vacío
K = |O| + sum(m in M, max(1, |A(m)|))    en otro caso
```

0 miembros/0 assignments → 1 gap; 0 miembros/N assignments → N atoms con
ABSENT_MEMBER+NON_MEMBER_ASSIGNMENT; 1 miembro/0 → 1 gap ABSENT_ASSIGNMENT+ABSENT_ACTIVITY;
1 miembro/1 → 1; 1 miembro/N → N; N miembros/0 → N gaps;
N miembros con a_i y O → sum(max(1,a_i))+|O|. Misma actividad en distintos miembros
son PKs distintas. Ordering sources: turnId, instructor/member, activity, evidenceKey;
UUIDs por orden SQL PostgreSQL unsigned, absence primero; evidenceKey UTF-8 unsigned.
El orden no expresa preferencia ni selecciona target.

Vocabulario cerrado de markers D12.3, sin enums nuevos del core:

```text
ABSENT_MEMBER ABSENT_ASSIGNMENT ABSENT_ACTIVITY NON_MEMBER_ASSIGNMENT
FULL_TURN_RANGE_FALLBACK EXPLICIT_ASSIGNMENT_RANGE INCOMPLETE_RANGE RANGE_OUTSIDE_TURN
DUPLICATE_PHYSICAL_ROW DUPLICATE_LOGICAL_ATOM INVALID_REQUIRED_FIELD
ORPHAN_TURN_HEADER INVALID_SOURCE_TYPE
```

Los primeros ocho describen sources representables. Los últimos cinco sólo el
rechazo operacional; DUPLICATE_* nunca se normaliza deduplicando para publicar.
History marker `LEGACY_FUNCTIONAL_VALIDITY_NOT_PERSISTED` pertenece al contrato
temporal D8.3, separado del vocabulario estructural anterior.

Ambos raw null → rango completo `[turnStart,turnEnd)`, `rangeRule=LEGACY_FULL_TURN_FALLBACK`
y marker **distinto** `FULL_TURN_RANGE_FALLBACK`. Ambos presentes → valores literales;
positivo/contenido → EXPLICIT_ASSIGNMENT_RANGE. Uno null → INCOMPLETE_RANGE;
ambos presentes no positivos o fuera → RANGE_OUTSIDE_TURN, sin recortar.
NON_MEMBER_ASSIGNMENT con header confiable es representable; assignment sin header
correlacionable es ORPHAN_TURN_HEADER y aborta.

Toda fila header requerida válida exige turnId/tipo/active/salon/rango positivo/
timestamps; recurrente day0..6 requerido/date null, puntual date requerida/day null.
La nulabilidad por tipo reproduce D8.2 y el CHECK físico de
`src/main/resources/db/migration/V15__calendario_instructores.sql` líneas33–35:
RECURRENTE exige dia_semana IS NOT NULL/fecha IS NULL; EXCEPCION/CANCELACION
fecha IS NOT NULL/dia_semana IS NULL. No es una nueva regla funcional ni inferencia.
Todos los headers repetidos por LEFT JOIN deben ser byte-equivalentes en sus diez
campos comunes. Null miembro sólo es la única fila LEFT JOIN de turno sin miembros;
null más miembro real del mismo turno es invariante rota. PK membership repetida,
PK assignment repetida, identidad lógica repetida, header incompatible, tipo
desconocido, PK requerida null o payload imposible abortan **todo** antes del core.

`LegacyAdapterInputInvalid` porta lista inmutable no-vacía de `LegacyAdapterRejection`:
code, queryId lógico catalogado, scope seguro, marker y IDs/raw no sensibles disponibles.
Caller inválido usa ADAPTER_INPUT_INVALID antes de SQL; projected required/type inválido
usa ADAPTER_INPUT_INVALID; duplicate/unmatched/incoherent correlation usa
READ_SET_INVARIANT_VIOLATION. IDs faltantes no se fabrican. QueryId para rechazo del
caller es el data contract members aunque no haya execution. Scope inválido sólo
retiene componentes válidos; un valor ausente no se sustituye por dato imaginado.
Ordinal estable identifica fila sin key; observedPhysicalRowCount conserva duplicados.
Una PK repetida cuenta un rejected attempted atom, con multiplicidad física >1.
Header inválido conserva K rechazos de unidades intentadas cuando las keys permiten
calcularlo; row sin key suficiente cuenta una unidad por ordinal. Se preserva D12.3,
sin copiar enum/messages/safeContext de ReservationReadException.

La validación se divide mecánicamente en extracción de keys y validación completa
de payload/correlación de **ambas** projections, sin publicar nada entre ellas.
Un error proyectado no-key de required/type/header payload con turnId UUID físico
válido y facts membership correlacionables queda pendiente en una lista privada;
no impide recoger los parent turnIds UUID distintos usables ni los member UUIDs
validados. Null membership se admite sólo como sentinel físico LEFT JOIN conforme
al contrato; no es un UUID fabricado. Se ejecuta la query ASSIGNMENTS ya existente
para todos esos parents, con binding natural §5, incluso para header inválido y
no-miembros, y se completa la enumeración estructural K antes de emitir la lista
inmutable exacta de rejections. Header inválido no se mapea a source/enum ilegal.
La elección de keys para enumerar no acepta el payload ni debilita sus constraints.

Ejemplo negativo de contrato/proyección: turnId/member UUID válidos y
createdAtTechnical requerido null. Sin assignments se rechaza exactamente 1 gap;
con 3 activity PKs distintas de ese miembro se rechazan exactamente 3 unidades.
Estos casos no afirman datos PostgreSQL nativos malformados existentes ni permiten
writes para producirlos. Cada rejection representa una unidad intentada D12.3;
duplicate PK/sourceIdentity conserva una única unidad rechazada y toda su
observedPhysicalRowCount, también al enumerar K. El ledger privado agrupa evidencia
repetida por key segura para contabilidad, nunca deduplica output aceptable ni
elige una fila. Una row sin key suficiente conserva exactamente una unidad por su
ordinal estable; no se le inventa identity ni K. Si existen otros parent UUIDs
usables, se completa su enumeración con ASSIGNMENTS; si ninguno existe, se omite
query2 y se aborta por esos ordinales. Lista derivada sólo contiene UUIDs non-null
validados, no caller key, fallback, query extra, retry o key reconstruida.

Acceso JPA/JDBC, SQL policy, binding, recurso, transacción, probes o completion
fallidos abortan inmediatamente por su owner operacional. Nunca se continúa una
TX JDBC abortada para obtener K ni se fabrican rejections/semántica por esos fallos.
Si un fallo físico impide terminar la enumeración, sólo se retiene lo observado
privadamente, sin anunciar K completo ni publicar la lista pendiente como completa.

Error channel excluye SQL/binds/URL/credentials/PII, salvo UUIDs técnicos/fecha/tipo raw
no sensible necesarios. Causes originales de acceso quedan internas y restringidas;
un fallo físico aborta como SOURCE_ACCESS_FAILURE operacional D19, sin source ni
status semántico. Policy/topología/probes/identity guards mantienen su identidad
operacional y precedencia; no se envuelven como rechazo de source ni acceso genérico.
No catch-all RuntimeException a read-set invariant; un defecto desconocido se propaga.

Observational counts D12.3: read=valid+rejected; physical rows cuentan aparte.
Éxito rejected0, published=valid y downstream evaluations=results=published.
Abort por payload proyectado tras enumeración: read=valid+rejected con K exacto
para keys suficientes y unidades ordinales para insuficientes; sin double count
de duplicate PK/identity, manteniendo physical rows aparte. Abort físico/operacional
antes de enumeración completa no afirma totales K completos ni rejections inventadas.
Todo abort: published=evaluations=results=0 aunque existan observaciones internas válidas.
No partial source/output, classifier invocation, reporte de éxito ni cero anomalías.
Estas métricas son contrato de pruebas/error, no un nuevo collector/sink runtime R6.

## 3. R2-ID — contexto, bytes, provenance e identidades

### 3.1 Contrato separado y autoridad confiable

`LegacyTurnReadContext` es un tipo conceptual nuevo persistence-agnostic bajo `read`.
**No** extiende/edita `ReadSnapshotContext`, `ReadSnapshotIdentifiers`, Reserva types,
sus singleton enums/claims/catalogs ni sus identities. Campos exactos en este orden:

| Campo | Tipo y ownership |
| --- | --- |
| runIdentity | String required del caller de prueba |
| attemptIdentity | String required del caller; nuevo para nuevo intento completo |
| sourceName | String copiado sólo del descriptor R2 validado |
| schemaFingerprint | String `sha256:`+64 lower hex, observado en bootstrap del recurso |
| projectionCatalogVersion | enum R2 propio, único constant `R2_LEGACY_TURN_V1` |
| ruleCatalogVersion | String required explícita de reglas, sin default ni clock |
| businessZone | ZoneId explícita, `getId()` |
| scopeCanonical | String derivada internamente del único scope tipado, no caller-input |
| snapshotClaim | enum R2 propio, sólo `R2_INTERNAL_RR_TEST` |
| snapshotEvidenceId | 64 lower hex, construido sólo por owner RR R2 después de probes válidos |

Los names son contratos conceptuales, no allowlist de filenames para implementar.
El catálogo R2 sella: projectionContractId=`R2_LEGACY_TURN_PROJECTION`, version=`V1`,
canonicalProjectionContractValue=`R2_LEGACY_TURN_PROJECTION/V1`; mapper/aggregator contract
`LegacyTurnProjectionMapper/V1`; sourceSystem LEGACY y sólo LEGACY_RECURRENTE,
LEGACY_EXCEPCION, LEGACY_CANCELACION. Incluye rows/aliases/types de §5 y el catálogo
SQL de §4. No strings libres, alias/version fallback, UNKNOWN o negociación.
Query/executor/mapper/context/provenance deben compartir exactamente este catálogo;
drift falla startup/preSQL. Rule catalog no redefine enums ni semántica del core.
El campo projectionCatalogVersion se serializa exactamente por enum.name():
`R2_LEGACY_TURN_V1`; no se sustituye por la pareja projectionContractId/version.

Descriptor R2 inmutable no-bean, constructor privado, propiedad de configuración de
prueba R2 explícita. Campos exactos: fixtureKey K, fixtureIdentity=`fixture-r2-`+K,
sourceName=`fixture:postgres16:r2:`+K, schemaFingerprint observado,
jdbcUrlCanonicaSinCredenciales, databaseName, schemaName=`public`, credentialPrincipal,
referencias Java exactas DS/EMF/TM/sharedEM/inspector del grafo §6. K es key ASCII
`[a-z0-9][a-z0-9-]*` del catálogo de fixtures, no del test method/callback.
Schema fingerprint usa bootstrap real Flyway+schema de la misma instancia; puede
reutilizar capacidad neutral de huella instalada, sin caller esperado como sustituto.
Fingerprint cubre columnas/ordinal/tipos/nullability/constraints/PK/FK/indexes y
historia aplicada versions/scripts/checksums/success, sin OIDs/clocks/install times.
No se concede SELECT de schema/Flyway al reader ni se consulta dentro de capture.

El owner R2 tiene registry **propio**, privado, no global/static/DB/filesystem/caller:
key bytes `SEQ("F2E-R2-RR-INVOCATION-KEY-V1", fixtureIdentity, runIdentity, attemptIdentity)`.
Una sola invocación readForDate por par run/attempt en la vida del ApplicationContext
test-only R2. Scope/seed inválidos no reservan; reserva atómica ausente→ACTIVE antes
de probes. Colisión concurrente/reuse falla preSQL; no check-then-put separado.
Completion real success→SUCCESS; rollback/fallo antes de escape→ABORTED;
interrupción sin completion demostrable→UNKNOWN/ACTIVE. Markers no se borran al
soltar lock ni por rollback; retry externo exige otro attempt, nunca reader retry.
El cierre del contexto elimina ese namespace; no unicidad durable/global ni lifecycle
de runKey/attemptKey/boundaryKey/invocationKey de R1 por analogía. Context reference,
scope bytes y snapshot evidence quedan asociados privadamente a esa reserva y Session;
una segunda llamada o contexto sustituto no obtiene aceptación. Main reader valida
labels/catalog/scope/evidence, owner verifica asociación completa y retiene resultados
provisionales hasta todos los guards y completion. No caller elige IDs de output.

### 3.2 Framing y scalar contract completo

Decisión R2 propia que instancia D13: toda fórmula `H(args...)` significa exactamente
lowercaseHex(SHA-256(SEQ(bytes(args)...))). `bytes(text)=UTF8-strict(text)`;
`bytes(byteString)=byteString` sin recodificación, para MAP/SEQ ya construidos.
`LP(x)=ASCII(byteLength(x))+":"+x`;
`SEQ(x1..xN)=ASCII(N)+":"+LP(x1)..LP(xN)`, counts sin signo/leading zeros excepto0.
Bytes anidados reciben LP exterior; no se aplanan. Sólo fundamento LP/SEQ compartido
de D36.8; no se importan domains F2E-R1/V2 ni fórmulas de D37.

UTF-8 strict, sin NUL/surrogate inválido/replacement; required string no-null/no-blank,
sin trim/casefold/NFC. UUID textual canónico lower-case; boolean `true|false`;
Short decimal sin padding; DATE `uuuu-MM-dd`; TIME `HH:mm:ss.SSSSSS`;
TIMESTAMPTZ UTC `uuuu-MM-dd'T'HH:mm:ss.SSSSSS'Z'`. Sub-microsegundo falla,
timezone default/Clock/random nunca se usa. En maps String, SQL null físico=`NULL`,
sin fila/dimensión inexistente=`ABSENT`; nunca null Java. Scalar vacío sólo permitido
en framing declarado de secuencia vacía, no en un required field.
No scalar textual proyectado válido admite los tokens NULL/ABSENT como alternativa
de enum/UUID/time/date. Así absence, SQL null, lista vacía y scalar válido difieren.

Dos órdenes **distintos**: D12.2 obliga UUID **natural Java** para las listas bound;
identity/checksum sets y PostgreSQL PK/output usan 16 bytes UUID **unsigned**.
No se cambia binding a unsigned por analogía D36.8. Se validan/copían sets antes de
ordenar; duplicados de frontera serializada se rechazan, nunca dedupe para ocultar.

```text
scopeBytes = SEQ("F2E-R2-READ-SCOPE-V1", "READ_FOR_DATE", ASCII(salonCount),
                 salonUUIDsUnsigned..., fecha)
scopeCanonical = UTF8-decode-strict(scopeBytes)

entry(key,value) = SEQ("F2E-R2-NORMALIZED-FIELD-V1", key, value)
MAP(fields) = SEQ("F2E-R2-NORMALIZED-FIELDS-V1", ASCII(entryCount),
                  entriesSortedByUnsignedUtf8Key...)
markersString = UTF8-decode-strict(SEQ("F2E-R2-MARKERS-V1", ASCII(markerCount),
                                      distinctMarkersSortedUnsignedUtf8...))
```

Todas las keys/values required; duplicate key/marker se rechaza antes de hash.
Sin markers usa la secuencia count0, nunca omission ni string ambiguo.

### 3.3 Payload normalizado exacto

`observableFields` tiene exactamente estas 26 keys. Sin mapas caller adicionales:

| Key | Valor determinista |
| --- | --- |
| turnId | UUID header |
| type | RECURRENTE/EXCEPCION/CANCELACION físico |
| active | true observado |
| salonId | UUID header |
| dayOfWeek | Short físico o NULL según forma |
| date | DATE física o NULL según forma |
| turnStart, turnEnd | TIME físicas positivas |
| createdAtTechnical, updatedAtTechnical | UTC micros; técnica exclusivamente |
| memberInstructorId | UUID si instructor del atom es miembro; NULL si no existe membership correspondiente |
| instructorId | UUID assignment o miembro del gap; ABSENT para gap sin miembros |
| activityId | UUID assignment; ABSENT para gap |
| assignmentStartRaw, assignmentEndRaw | TIME/NULL para assignment; ABSENT para gap sin fila |
| effectiveStart, effectiveEnd | fallback del turno o ambos raw presentes literales; ABSENT ambos para gap o INCOMPLETE_RANGE |
| rangeRule | vocabulario literal definido abajo |
| atomKind | ASSIGNMENT / GAP_ABSENT_MEMBER / GAP_ABSENT_ASSIGNMENT |
| evidenceKey | sourceIdentity URN exacta de §2 |
| membershipStatus | MEMBER / NON_MEMBER / NO_MEMBERS |
| markers | markersString cerrado anterior |
| historyStatus | CURRENT_SNAPSHOT_ONLY |
| historyMarker | LEGACY_FUNCTIONAL_VALIDITY_NOT_PERSISTED |
| scenarioPolicy | valor derivado definido abajo, sin invocar classifier |
| evaluationDate | scope.fecha explícita, no clock |

rangeRule cerrado R2: ambos null assignment=`LEGACY_FULL_TURN_FALLBACK`;
positivo/contenido explícito=`LEGACY_EXPLICIT_ASSIGNMENT_RANGE`;
una null=`LEGACY_INCOMPLETE_RANGE`; ambos presentes inválidos=`LEGACY_RANGE_OUTSIDE_TURN`;
gap=`NOT_APPLICABLE`. Son representación de evidence nueva R2, no enums core ni
revisión de markers D12.3. RANGE_OUTSIDE_TURN retiene ambos raw/effective literalmente
como evidence, nunca construye intervalo elegible inválido. Fallback/explicit markers
pueden coexistir con NON_MEMBER_ASSIGNMENT. M vacío en assignment añade ABSENT_MEMBER,
membershipStatus NO_MEMBERS; M no vacío y instructor ausente añade NON_MEMBER_ASSIGNMENT,
membershipStatus NON_MEMBER; member gap o assignment miembro usa MEMBER.

SourceAtomType sólo deriva tipo físico. scenarioPolicy puntual usa exactamente
`LEGACY_EXCEPTION_UNKNOWN_INTENT` o `LEGACY_CANCELLATION_UNKNOWN_INTENT`, con o sin
markers. No existe valor legal `LEGACY_PUNCTUAL_UNKNOWN_INTENT`.
Recurrente anómalo representable usa `INCOMPATIBLE_EVIDENCE`; recurrente completo
usa el literal de policy `RECURRENT_CLAIM_DEPENDENT`, **no** un DetectionScenario nuevo:
downstream el claim legal determina STANDARD_EVALUATION/REQUIRED_TARGET o
LEGACY_HISTORY_REQUIRED. R2 no recibe claim semántico ni clasifica.
Sources mantienen CURRENT_SNAPSHOT_ONLY; historia recurrente requerida obtiene
UNKNOWN_HISTORY/UNSUPPORTED y marker de falta de validez funcional sólo al evaluar
el claim legal. Puntual conserva UNKNOWN_INTENT/CURRENT_SNAPSHOT_ONLY y el marker
de historia, nunca scenario histórico ilegal ni reemplazo/adición/EXPECTED_ABSENCE.
UNKNOWN_INTENT domina anomalía estructural puntual. Ningún timestamp reconstruye historia.

### 3.4 Instancia determinista de las cinco fórmulas D13

Se conservan domains y componentes genéricos de D13; R2 sólo sella framing/inputs.
`canonicalNormalizedFields` aquí es **MAP(observableFields)** de las 26 keys anteriores;
excluye IDs de ejecución/provenance para evitar autorreferencia. Evaluación fecha y
policy de evidence son observables normalizados R2 versionados, no selección.

```text
executionProvenanceId = H("F2E-EXECUTION-V1", runIdentity, attemptIdentity,
  sourceName, schemaFingerprint, "R2_LEGACY_TURN_V1", ruleCatalogVersion,
  businessZone.getId(), scopeCanonical)

logicalSnapshotId = H("F2E-LOGICAL-SNAPSHOT-V1", executionProvenanceId,
  "R2_INTERNAL_RR_TEST", snapshotEvidenceId)

sourceFingerprint = H("F2E-SOURCE-V1", schemaFingerprint,
  "R2_LEGACY_TURN_PROJECTION", "V1", "LEGACY", sourceAtomType.name(),
  sourceIdentity, MAP(observableFields))

snapshotIdentity = H("F2E-ATOM-SNAPSHOT-V1", logicalSnapshotId,
  sourceAtomType.name(), sourceIdentity, sourceFingerprint)

readSetEntry = SEQ("LEGACY", sourceAtomType.name(), sourceIdentity, sourceFingerprint)
logicalReadSetFingerprint = H("F2E-READSET-V1", sortedReadSetEntries...)
```

Read-set sort compara los cuatro componentes por bytes UTF-8 unsigned, no hash ni
iteration order; el orden de source list §2 y el sort del fingerprint cumplen fines
distintos. Vacío usa `H("F2E-READSET-V1")`. ReadSet mantiene sólo sources; su fingerprint
lo calcula/retiene el owner de prueba. IDs 64 lower hex se tratan como ASCII, no bytes
decoded. sourceFingerprint excluye run/attempt/zone/snapshot, pero incluye fecha
de evaluación que sí figura en payload; cambiar cualquiera de las 26 keys lo cambia.
execution ID no prueba simultaneidad; logical ID depende de ejecución como D13 exige,
sin importar la fórmula R1 que la excluye. Read-set hash describe contenido y no MVCC.

EvidenceProvenance conserva los siete campos core existentes:
sourceName/schemaFingerprint confiables; recordIds posicionales §2;
ruleId=`R2_LEGACY_TURN_PROJECTION`, ruleVersion=`V1`;
businessTimeContext=decode(SEQ("F2E-R2-BUSINESS-CONTEXT-V1", businessZone.getId(), scopeBytes));
normalizedFields tiene **exactamente** la unión disjunta de observableFields26 y:

```text
runIdentity attemptIdentity businessZone scopeCanonical operation
projectionCatalogVersion projectionContractId projectionContractVersion ruleCatalogVersion
snapshotClaim snapshotEvidenceId executionProvenanceId logicalSnapshotId
sourceSystem sourceAtomType sourceIdentity sourceFingerprint snapshotIdentity
transactionIsolation transactionAccessMode
```

operation READ_FOR_DATE; catálogo y enum names exactos arriba;
transactionIsolation=`repeatable read`, transactionAccessMode=`read only` observados.
Keys restantes copian context/IDs sin transformación. Ninguna otra key; el core
semanticHash no sustituye estas fórmulas. SQL/binds/native object identity/backend PID/
credentials/connection labels sensibles no entran a provenance ni hashes de source.
Pg snapshot textual queda en evidencia privada del owner, su hash en context.

Se mapean todas las projections validadas antes de construir output final; copias
defensivas en cada frontera, recompute independiente desde payload final y checks
cross-record: scope/context/query/mapper/catalog coinciden; recordIds y URN corresponden
a las filas físicas; todas las sources comparten execution/logical IDs, con snapshot
individual. Digest asociado a otra preimage en la misma invocación aborta sin sal/dedupe.
Recompute inconsistente aborta sin parciales. Golden vectors nuevos y fingerprints
ordenados de statements/commitment pre-callback son `NOT_YET_NORMATIVE`; no se importan
D37.4/37.8 ni se exige número de vectores. Capture ordenada de §6 es evidencia literal,
no una nueva fórmula de fingerprint de statements.

## 4. R2-SQL — catálogo cerrado literal

Esta candidate fija catálogo R2 **CLOSED_SET** de seis shapes, no lifecycle CLOSED.
Sólo dos DATA y cuatro probe shapes necesarias. Cada code block es una línea SQL exacta
sin newline integrante/semicolon/comment. Parámetros named son los únicos del executor.

`R2_LEGACY_MEMBERS_V1`:

```sql
SELECT t.id AS turn_id, t.tipo AS turn_type, t.activo AS turn_active, t.salon_id AS salon_id, t.dia_semana AS day_of_week, t.fecha AS turn_date, t.hora_inicio AS turn_start, t.hora_fin AS turn_end, t.creado_en AS created_at_technical, t.actualizado_en AS updated_at_technical, m.usuario_id AS member_instructor_id FROM public.turno_instructor t LEFT JOIN public.turno_instructor_usuario m ON m.turno_id = t.id WHERE t.salon_id IN (:salonIds) AND t.activo = :active AND ((t.tipo = :recurrentType AND t.dia_semana = :dayOfWeek) OR ((t.tipo = :exceptionType OR t.tipo = :cancellationType) AND t.fecha = :fecha)) ORDER BY t.id, m.usuario_id NULLS FIRST
```

`R2_LEGACY_ASSIGNMENTS_V1`:

```sql
SELECT a.turno_id AS turn_id, a.usuario_id AS instructor_id, a.tipo_actividad_id AS activity_id, a.hora_inicio AS assignment_start_raw, a.hora_fin AS assignment_end_raw FROM public.turno_instructor_asignacion a WHERE a.turno_id IN (:turnIds) ORDER BY a.turno_id, a.usuario_id, a.tipo_actividad_id
```

`R2_TX_ISOLATION_V1`, `R2_TX_READ_ONLY_V1`, `R2_TX_SNAPSHOT_V1` respectivamente:

```sql
SELECT current_setting('transaction_isolation')
```

```sql
SELECT current_setting('transaction_read_only')
```

```sql
SELECT pg_current_snapshot()::text
```

`R2_TX_RESOURCE_IDENTITY_V1`, probe de metadata del owner por el mismo shared EM:

```sql
SELECT current_database() AS database_name, current_schema() AS schema_name
```

No lee una nueva tabla ni aporta input funcional productivo. Observa database/schema
reales sobre la misma PgConnection bound; descriptor/bootstrap conserva los expected
confiables y se compara el resultado real, nunca se devuelve expected en su lugar.

R2 adopta explícitamente como capacidad neutral `F2E_SQL_CANON_V1` y
`F2E_SQL_CATALOG_ID_V1` de D36.4–36.5: UTF-8, ASCII whitespace collapsed fuera de
quotes, quotes preservadas, sin casefold/Unicode normalization; ?/?digits/$digits→?,
lista sólo markers→(?*); null/empty/NUL/comment/semicolon/unclosed quote rechazan.
`:name` se conserva en normalizer de SQL observada; sólo la derivación **documental**
del SQL named reemplaza cada parámetro exacto por marker. Runtime normaliza el SQL
renderizado por Hibernate, no inventa values ni modifica listas. Identidad exacta:
SHA-256(UTF8("F2E_SQL_CATALOG_ID_V1\n")||ASCII(byteLength(C))||":"||UTF8(C)), lower hex.
La tabla siguiente sella identidades calculadas documentalmente, sin ejecutar SQL/tests:

| Logical ID | Canonical UTF-8 byte length | Catalog statement ID |
| --- | --- | --- |
| R2_LEGACY_MEMBERS_V1 | 579 | 9852b6e9487a71cb47d76e834e82eceafcfbdd194b6e66d718f7da1ccdf3a769 |
| R2_LEGACY_ASSIGNMENTS_V1 | 290 | 6b21c28ee8961f783e986704604791181aa175592abc0b73553fa321445ce219 |
| R2_TX_ISOLATION_V1 | 47 | 4a669a2f628e12468e0d532889e0bcafd38c09a5aadfb27f2f20f56159c1671e |
| R2_TX_READ_ONLY_V1 | 47 | 9963ea856cdf9bfb3e9c440b1c3a6c062fd375a906f465cbe7a7ac88878716c7 |
| R2_TX_SNAPSHOT_V1 | 34 | 24f02692f50e880b77a78f9ea64501ce276b7b0e2a93c880a4c23e03bbe19aa0 |
| R2_TX_RESOURCE_IDENTITY_V1 | 75 | 0ed00ba3ec87635658759a60f48bc9ea57df82a338b863f23be641fdf6b7b3ad |

Canonical DATA strings **exactas**, integrantes del catálogo:

```text
SELECT t.id AS turn_id, t.tipo AS turn_type, t.activo AS turn_active, t.salon_id AS salon_id, t.dia_semana AS day_of_week, t.fecha AS turn_date, t.hora_inicio AS turn_start, t.hora_fin AS turn_end, t.creado_en AS created_at_technical, t.actualizado_en AS updated_at_technical, m.usuario_id AS member_instructor_id FROM public.turno_instructor t LEFT JOIN public.turno_instructor_usuario m ON m.turno_id = t.id WHERE t.salon_id IN (?*) AND t.activo = ? AND ((t.tipo = ? AND t.dia_semana = ?) OR ((t.tipo = ? OR t.tipo = ?) AND t.fecha = ?)) ORDER BY t.id, m.usuario_id NULLS FIRST
SELECT a.turno_id AS turn_id, a.usuario_id AS instructor_id, a.tipo_actividad_id AS activity_id, a.hora_inicio AS assignment_start_raw, a.hora_fin AS assignment_end_raw FROM public.turno_instructor_asignacion a WHERE a.turno_id IN (?*) ORDER BY a.turno_id, a.usuario_id, a.tipo_actividad_id
```

Canonical probes son sus cuatro strings SQL literales anteriores. Igual hash de probes
I/R con R1 demuestra igual texto/framing; **no** transfiere claim, rol, orden, count,
owner o READ_COMMITTED R1. R1 mantiene sus cuatro entradas y snapshot denegado.
No SHOW/SET/SQL de catálogo de schema/sequence, SELECT genérico, WITH o locks;
funciones built-in permitidas sólo en las cuatro shapes exactas anteriores, sin
categoría abierta de funciones/metadata ni bypass para SQL interna del driver.
unknown SQL/SELECT y normalization/class/denylist/catalog miss fallan **antes de JDBC**.
Driver begin/isolation/readOnly/commit/rollback son operaciones lifecycle JDBC del
owner, observadas separadamente y corroboradas por probes efectivos, no DATA ni
probe SQL catalogada. No se permite SET en el inspector ni se finge el SQL interno
de esas operaciones como SQL inspeccionada. Todas las SQL requeridas por guards
de esta invocación atraviesan captura/catálogo; no se invocan métodos de metadata
que creen statements implícitos adicionales (§6.2).

## 5. Projections, binding y captura tipada

Tuple mapping ordinal cerrado; aliases lower snake exactos de §4. Cada posición
produce record concreto; no interface projection proxy. Tipos PostgreSQL→Java:

| Row/ordinal | Alias / logical field | PostgreSQL / Java | Null |
| --- | --- | --- | --- |
| Member1 | turn_id / turnId | uuid / UUID | NO |
| Member2 | turn_type / type | varchar / String luego enum legal | NO |
| Member3 | turn_active / active | boolean / Boolean validado | NO |
| Member4 | salon_id / salonId | uuid / UUID | NO |
| Member5 | day_of_week / dayOfWeek | smallint / Short | forma |
| Member6 | turn_date / date | date / LocalDate | forma |
| Member7,8 | turn_start, turn_end / turnStart, turnEnd | time / LocalTime | NO |
| Member9,10 | created_at_technical, updated_at_technical / createdAtTechnical, updatedAtTechnical | timestamptz / OffsetDateTime | NO |
| Member11 | member_instructor_id / memberInstructorId | uuid / UUID | SÍ LEFT JOIN |
| Assignment1 | turn_id / turnId | uuid / UUID | NO |
| Assignment2 | instructor_id / instructorId | uuid / UUID | NO |
| Assignment3 | activity_id / activityId | uuid / UUID | NO |
| Assignment4,5 | assignment_start_raw, assignment_end_raw / assignmentStartRaw, assignmentEndRaw | time / LocalTime | SÍ ambos independientes |
| Resource1 | database_name / databaseName observado | name / String | NO |
| Resource2 | schema_name / schemaName observado | name / String | NO |

No coerción permisiva de UUID desde string/arbitrary object, boolean desde number,
day desde fuera0..6 o tipo desconocido. JDBC DATE/TIME/TIMESTAMPTZ mapean por codecs
tipados del stack, UTC técnico; conversion necesaria explicitada y probada sobre PG,
sin default timezone o `toString()` genérico para valores desconocidos.
Probes I/R/S producen String non-null: `repeatable read`, `on`, texto exacto snapshot.
`on`→`read only` sólo para contexto; cualquier otro valor aborta.
RESOURCE produce exactamente una tupla de dos String non-null en el orden
database_name/schema_name; cardinalidad, tipo o mismatch contra descriptor aborta
por el owner de recurso, sin coerción ni sustitución por expected/cache.

`EntityManager.createNativeQuery`→unwrap `NativeQuery`; exclusivamente named bindings
del contrato D12.2. Lists defensivas **natural UUID** por `setParameterList(name, list, UUID.class)`;
scalar `setParameter(name,value,ExactJavaClass)`, sin TemporalType/text/arrays/casts.
Constantes active true y tipos RECURRENTE/EXCEPCION/CANCELACION son del executor.

| Query | Ordered named parameter plan | ExactJavaClass |
| --- | --- | --- |
| Members | salonIds, active, recurrentType, dayOfWeek, exceptionType, cancellationType, fecha | UUID.class list; Boolean.class; String.class; Short.class; String.class; String.class; LocalDate.class |
| Assignments | turnIds UUID físicos validados usables de MEMBERS, aun con payload no-key pendiente inválido; no membership filter | UUID.class list |
| I/R/RESOURCE/S probes | ninguno, zero binds; owner por sharedEM | ninguno |

En members con s salones, positions JDBC1..s corresponden a lista natural salonIds;
s+1 active, s+2 recurrentType, s+3 dayOfWeek, s+4 exceptionType, s+5 cancellationType,
s+6 fecha. Assignments positions1..t corresponden a lista natural de turnIds.
Cada occurrence named es la del string exacto §4, no duplicación inventada del binder.
Hibernate expande list markers automáticamente; el executor nunca construye markers,
concatena UUIDs, usa ANY/Array/temp table o nullable filters. Raw nullable son datos,
no parámetros de filtro. Parent turnIds se derivan sin exigir header payload completo
válido: se validan keys primero y se conserva todo error pendiente sin publicar (§2).
Cero derived turnIds omite binding/query2: éxito sólo si MEMBERS realmente devolvió
cero filas, de otro modo abort por keys insuficientes. Scope caller vacío
rechaza antes de createNativeQuery/probes/capture en el owner.

Inspección SQL no ve valores ni execution. Por eso la futura evidencia de prueba
asocia, por ordinal de statement y reserva R2, **tres registros distintos**:
(a) plan nombrado tipado/valores canónicos del executor, (b) inspector catalog ID,
(c) observación JDBC real antes/después de delegar sobre Connection bound (§6).
Los cuatro probes, incluido RESOURCE inicial/final, tienen plan explícito zero binds,
catalog ID del inspector y execution/ResultSet JDBC real en la misma asociación;
el resource guard no usa sólo assertion del wrapper o resultado del descriptor.
Registro bind privado: namedParameter, exactJavaClass, scalar/list flag, length,
lista/valor copiado; registro JDBC: statementOrdinal, JDBCposition, setter real,
runtimeClass/value canónico/jdbcTypeArg si existe, y resultado de delegation.
`setObject(index,UUID[,Types.OTHER])`, `setBoolean`, `setString`, `setShort`, `setDate`
son las conversiones competentes de UUID/Boolean/String/Short/LocalDate respectivamente;
setDate observa java.sql.Date y canon DATE, no afirma recibir LocalDate del JDBC setter.
Se compara cada slot con plan original. No ParameterMetaData query ni tipos/binds
inferidos del inspector. Setter incompatible, slot ausente/extra/reordenado, SQL
shape/hash distinto o lista expandida de longitud diferente aborta antes de aceptación.
bind evidence es interna de tests, nunca map core/error/report; no hay PII requerida.

## 6. R2-RR — recurso, owner y evidencia nativa test-only

### 6.1 Grafo único y qualifiers exactos

Nombres siguientes son decisión de wiring R2, no permiso de crear archivos ahora:

| Recurso/bean | Construcción / consumers |
| --- | --- |
| f2eR2PrivilegedDataSource | container/Flyway/fixture/grants/observer/cleanup, fuera de ventana |
| f2eR2ReaderDataSource | login R2 SELECT-only; sólo reader EMF/TM |
| f2eR2StatementPolicyInspector | instancia única con catálogo R2 de §4; property de ese EMF/capture owner |
| f2eR2ReaderEntityManagerFactory | LocalContainerEntityManagerFactoryBean, PU f2eR2ReaderPersistenceUnit, sólo reader DS; ddl-auto validate |
| f2eR2ReaderEntityManager | único SharedEntityManagerCreator proxy de ese EMF; executor/owner |
| f2eR2ReaderTransactionManager | JpaTransactionManager(EMF), setDataSource exactamente reader DS |
| legacyTurnProjectionQueryExecutor | plain constructor(sharedEM,catalogR2) |
| legacyTurnProjectionMapper | plain constructor(catalogR2), incluye aggregation |
| legacyTurnJpaReader | plain constructor(executor,mapper,catalogR2,labels confiables), proxy test-only |
| legacyTurnTransactionTestOwner | TEST-ONLY bean separado/proxied, descriptor/registry/capture propios |

Cada injection/factory parameter lleva el qualifier exacto; no @Primary, configurer
default, type fallback, routing DS, SET ROLE, scan/repository enable nuevo ni privileged
EMF/TM. Propiedad `hibernate.session_factory.statement_inspector` contiene instancia,
no classname que cree otra. Parent/default resources no son candidatos.

Owner público conceptual `inRepeatableReadOnly` declara
`@Transactional(transactionManager="f2eR2ReaderTransactionManager", propagation=REQUIRES_NEW,
isolation=REPEATABLE_READ, readOnly=true)`; abre/cierra única TX. Reader readForDate declara
`@Transactional(transactionManager="f2eR2ReaderTransactionManager", propagation=MANDATORY,
readOnly=true)`. Nombrar manager en main plain no crea bean productivo; antes de R6
sólo registration explícita test-only permite advisor. Outside TX/wrong manager/missing
named manager falla antes de query, sin fallback. Proxy owner y proxy reader son distintos;
self-invocation/`new` sólo sirve unit mapping, jamás propagation proof.
Reader no abre/suspende TX, no eleva isolation, no downgrade/retry/REQUIRES_NEW.

### 6.2 Native same-resource y controles independientes

Startup compara descriptor/configured DS/EMF/TM/sharedEM/inspector por identidad;
EMF DS/PU/property y TM EMF/DS deben ser exactos. Después del begin y antes de capture,
owner valida mismo EntityManagerHolder/Session bound a EMF, joined TX, Spring readOnly/RR,
Session defaultReadOnly y MANUAL flush. Obtiene sólo por ese sharedEM/Session.doReturningWork
Connection y su **PgConnection nativo unwrap**, guardando ambas referencias privadas.
No Connection suministrada por caller, segundo EM, getConnection/DriverManager/JdbcTemplate
en reader/executor/owner/probes ni sesiones independientes.

Los guards inicial/final de grafo, configured URL, native URL/principal y referencias
son exclusivamente locales, sin SQL. Metadata real sin transformación:
PgDatabaseMetaData.getURL()/getUserName() del PgConnection original y configured URL
validan por separado contra descriptor/bootstrap. URL permitida
exactamente `jdbc:postgresql://<host-lower-ascii>:<canonical-port>/<databaseName>`, puerto1..65535,
sin query/fragment/userinfo/password/percent-encoding/alias. Principal exacto;
no strip de query nativa para hacerla coincidir. Mismatch configurado **o** metadata
local nativa aborta independientemente aunque el otro plano coincida. getURL y
getUserName delegan a métodos locales del driver; wrapper sólo registra/reenvía los
valores genuinos, nunca transforma metadata nativa para obtener match.
La observación inicial usa primera conexión bound de **esta** invocación, la final
la última antes de completion. No incorpora requisito histórico TECH first/later
entre invocaciones R1; estas observaciones son guards internos R2.

Antes/después de **cada** statement, owner/executor test evidence exige mismas referencias
Connection transaccional y PgConnection nativo, Session/EM holder/TM/DS del descriptor.
Final repite guard local URL/principal/grafo y compara referencias; mismo wrapper
sobre otro PgConnection no prueba same-resource. Session.doReturningWork se permite
exclusivamente para estas inspecciones locales y referencias; no prepara/ejecuta SQL.
Se prohíbe invocar Connection/PgConnection.getSchema() o getCatalog() desde owner,
reader o probes en esta invocación medida, independientemente de un posible cache.
No metadata traversal/ParameterMetaData ni otra API que genere SQL implícita.

Supporting primario físico pgjdbc42.7.11 sources.jar SHA
`5156b9a1076e69ede16266ceb0c20a7ecd0f3f7d5a5a388480333ec8339ee198`:
PgConnection.java L1717–1725 getSchema ejecuta `select current_schema()`;
L1130–1141 getCatalog puede ejecutar `select current_catalog` si cache null;
PgDatabaseMetaData.java L121–128 getURL/getUserName son delegaciones locales.
Por eso database/schema reales se observan **únicamente** por el probe explícito
RESOURCE de §4 inicial/final mediante EntityManager.createNativeQuery→NativeQuery
del mismo sharedEM, Session y PgConnection bound. Su tupla real §5 debe coincidir
byte-for-byte con descriptor.databaseName/schemaName observados en bootstrap,
sin trim/casefold, cached expected o labels esperados como sustituto. RESOURCE no
se ejecuta dentro de doReturningWork; atraviesa plan/inspector/JDBC real/capture.
Es metadata de validación interna, no nueva source ni input funcional. Context no
transporta referencias JPA/native, tupla RESOURCE ni URLs; sus diez campos y las
cinco fórmulas/26+20 keys de §3 permanecen intactos.

Flyway/ddl-auto validate/schema fingerprint y metadata de bootstrap ocurren antes
de la invocación medida, fuera de su capture y separadas de sus guards. No se
declara cero SQL global de bootstrap. La captura scoped cubre todos los probes,
DATA y SQL exigidos por guards dentro de la invocación; ningún SQL de driver o
metadata implícita recibe bypass ni queda oculto fuera del catálogo de seis shapes.

Instrumentación TEST-ONLY de reader DS/Connection/PreparedStatement reenvía setters/
execute/ResultSet y registra binding+execution reales sin sustituir datos, metadata,
resource ni resultados. Expone unwrap genuino de PgConnection para comparar recurso.
Para cada prepare/execute guarda ordinal, SQL hash renderizado, parámetros bound
observados, Connection/PgConnection originales, execute entered/completed/failed;
fuera de capture o sin asociación inspector/plan se rechaza. No añade SQL/queries.
Inspector sólo prueba política **antes** de prepare; no se afirma que execution
ocurrió hasta la llamada JDBC real y su retorno/exception. Recursos object identities
y evidence privados no se serializan ni participan en source hashes.
JDBC begin/commit/rollback/isolation/readOnly pertenecen al advisor/TM; no son DATA
capturada. La instrumentación observa completion real sin atribuirles un catalog ID.

### 6.3 Snapshot evidence, orden y failure prefixes

Tras registro único/guard local inicial válidos, capture ThreadLocal no-anidable inicia
antes del primer probe, permanece hasta completion; thread distinto/capture ausente/
reuse/missing/extra statement aborta. El owner crea context después de I/R/RESOURCE/Sinitial
observados y validados, con evidencia R2 específica:

```text
snapshotEvidenceId = H("F2E-R2-RR-TEST-EVIDENCE-V1", descriptor.fixtureIdentity,
  runIdentity, attemptIdentity, "f2eR2ReaderTransactionManager", "f2eR2ReaderPersistenceUnit",
  "repeatable read", "read only", pgSnapshotInitialExactText)
```

Claim sólo consistencia interna de dos projections R2 en TX de prueba; no
MULTI_READER_MVCC/SAME_LOGICAL_SNAPSHOT productivo cross-source ni evidencia de R6.
Texto snapshot inicial/final byte-exact, sin trim/sort/cast a txId/fingerprint de contenido.
Cada statement observation se liga a la misma reserva/Session/PgConnection y a esa
snapshotEvidenceId. Los primeros probes se sellan retrospectivamente con el ID creado
de sus resultados antes de callback; no compromiso/fingerprint pre-callback de manifest.

Orden de invocación exitosa sellado por roles, sin imponer futuro test-count:

```text
Iinitial → Rinitial → RESOURCEinitial → Sinitial → MEMBERS
→ [ASSIGNMENTS sólo si parent turnIds UUID usables no vacío]
→ Ifinal → Rfinal → RESOURCEfinal → Sfinal → native local final guard → completion real
```

I ambas `repeatable read`; R ambas `on`; RESOURCE ambas tuplas reales iguales al
descriptor database/schema y entre sí; Sfinal==Sinitial. Cada occurrence usa catálogo
shape exacto §4 y asociación tipada §5. Cero turnos es MEMBERS vacío sin ASSIGNMENTS,
con probes finales y resource guard igualmente obligatorios. Sólo después de mapping/
recompute/manifest/resource/snapshot checks y **commit/completion exitoso** escapa read set;
no salida desde target antes de interception completion como evidencia suficiente.

Failure capture es prefijo veraz del mismo orden, con estas distinciones obligatorias:

| Fallo/camino | SQL de invocación observada / corte | Resultado |
| --- | --- | --- |
| caller/context/scope inválidos, registry collision | sin capture/probes/DATA, zero invocation SQL | error preSQL; no reserva nueva en caller inválido |
| guard local inicial de configured/native URL/principal/grafo/referencias inválido | antes de capture, zero invocation SQL | abort operacional, sin context/output |
| Iinitial/Rinitial inválido o fallo físico/policy | sólo prefijo hasta ese probe; zero DATA | abort inmediato; no probes restantes |
| RESOURCEinitial tupla/cardinalidad/tipo/database/schema inválidos | Iinitial/Rinitial/RESOURCEinitial ya observados según stages reales; zero DATA; sin Sinitial | abort de recurso, no promesa zero SQL |
| Sinitial inválido/fallido | prefijo inicial I/R/RESOURCE/S, zero DATA | abort de snapshot, no context válido |
| MEMBERS acceso/policy/binding/recurso/TX fallidos | prefijo inicial + stages reales de MEMBERS, sin ASSIGNMENTS | abort físico/operacional inmediato; K no completado/no fabricado |
| MEMBERS payload no-key/header inválido con keys UUID correlacionables suficientes | prefijo inicial + MEMBERS + ASSIGNMENTS por los parents UUID usables | errors pendientes privados; enumerar K exacto §2 y después abort total, sin probes finales |
| MEMBERS rows con keys insuficientes y otros parent UUIDs usables | prefijo inicial + MEMBERS + ASSIGNMENTS de esos parents | una unidad por ordinal insuficiente; enumeración de las otras unidades, después abort total |
| MEMBERS no vacío y ningún parent UUID usable | prefijo inicial + MEMBERS, sin ASSIGNMENTS | error por ordinal, sin éxito vacío ni K supuesto |
| MEMBERS realmente cero rows | prefijo inicial + MEMBERS + Ifinal/Rfinal/RESOURCEfinal/Sfinal + guard local/completion | éxito vacío válido sólo al completar todos los controles |
| ASSIGNMENTS fallo físico/policy/binding/recurso/TX | stages reales hasta ese statement, sin consultas adicionales/probes finales | abort inmediato; pending errors no publicados como K completo |
| payload/correlación/duplicate de projections con acceso completado | ambas DATA si hay parents usables, luego validación completa §2 | rejections exactas inmutables D12.3, abort pre-core sin probes finales |
| final I/R/RESOURCE/S/guard local o manifest/recompute/completion fallidos | prefijo real alcanzado; statements anteriores conservados | abort operacional separado, descartar todo output provisional |

Ningún error pendiente autoriza seguir tras una TX JDBC abortada. Se siguen sólo
las dos DATA existentes cuando extracción válida de keys permite enumerar rechazos;
la validación completa y publicación se realizan después de ambas projections.
Policy/topología/probes/binding/recurso/completion no se convierten en
LegacyAdapterRejection ni en status semántico; no fabricated K, fallback/extra query,
retry o dedupe output. Para cualquier invalid scope published/evaluations/results=0.
Captura diferencia inspected/prepare/execute entered/completed/failed; un
statement inspeccionado puede haber fallado antes de execute. No exige completar probes
restantes de una TX fallida ni acepta prefijo truncado como éxito. Policy rejection
registre razón/ID seguro y cero JDBC de statement rechazado. Siempre finally cierra
capture y terminaliza registry conforme completion, sin borrar markers ni fabricar
successful execution/result. Unknown interruption bloquea reuse. No retry interno.

Fail-closed: wrong advisor/TM/EMF/PU/DS/EM/Session/native resource, caller labels,
missing inspector, forged catalog/scope/context/evidence, metadata drift, RR/readOnly
incorrectos, snapshot textual distinto, binding/manifest/recompute mismatch o grant
fence ausente → cero outputs aceptados, canal operacional D19. No MISSING/UNSUPPORTED/
EXPECTED_ABSENCE ni resultado classifier sobre input parcial.

## 7. R2-CHECKSUM — SELECT-only y contrato tres tablas completo

Login efímero distinto del admin, sin SET ROLE/herencia de rol privilegiado:
CONNECT al DB, USAGE public, SELECT **exactamente** public.turno_instructor,
public.turno_instructor_usuario, public.turno_instructor_asignacion. Sin CREATE/TEMP/
DML/sequence/application-function privileges ni ownership/superuser/bypassRLS.
Revocación efectiva de PUBLIC y memberships se verifica antes del gate de reader.
No grants a reserva, maestros, programacion_* o flyway_schema_history.
Sólo acceso/EXECUTE efectivo necesario a los built-ins current_setting,
current_database, current_schema y pg_current_snapshot de las cuatro shapes
exactas §4, conforme privilegios PostgreSQL efectivos; ningún EXECUTE a funciones
de aplicación, SQL permisiva o grant de tabla source adicional. RESOURCE no altera
las tres tablas protegidas/grants SELECT ni autoriza DML/DDL de invocación.
Privileged Flyway/fixtures/setup/grants son writes previos reales; no "zero global writes".

Controles INSERT/UPDATE/DELETE/DDL con login lector en conexiones/TX separadas antes
de medición exigen denegación privilege SQLState42501; cualquier éxito falla gate,
aunque luego se haga rollback. No se necesita mutation exitosa para demostrar guard.
Cleanup/control/observer no pasan por reader EMF/capture ni se usan para MVCC proof.
Narrow ports, architecture, SQL policy, DB fence y checksum son acumulativos;
rollback/readOnly annotation/statistics sólo no bastan.

Observer admin congela **parent turnIds** con los mismos predicados fecha/salones/
active/tipos del port antes del baseline. En ambos before y after selecciona parents
por ese set y **children por turno_id IN frozenParentIds cada vez**, no sólo por PKs
children inicialmente existentes: así inserts/deletes children cambian counts/hash.
Sin scan global. Conserva además igualdad de set de parents relevantes por el predicado
antes/después, detectando un parent nuevo que cambiaría el scope; no extiende checksum
a rows externas. Se incluyen todos los children del parent aunque non-member/nullable.
TurnIds vacío sigue exactamente tres entries de tabla vacía, nunca slice sin tablas.

Todas las columns persistidas del schema V47 instalado, orden **schemaordinal vivo**,
no projection/reflection/ResultSet incidental. Ordinal dropped se omite; nombres/tipos/
nullability/PK observados deben coincidir con contrato; columna nueva/desconocida falla
compatibilidad, nunca se ignora. Lista física tras V19/V20/V22:

| Table | All-column order y tags | PK ordering |
| --- | --- | --- |
| public.turno_instructor | id U, salon_id U, tipo S, dia_semana I nullable, fecha D nullable, hora_inicio T, hora_fin T, activo B, creado_en Z, actualizado_en Z | (id) |
| public.turno_instructor_usuario | turno_id U, usuario_id U | (turno_id, usuario_id) |
| public.turno_instructor_asignacion | turno_id U, usuario_id U, tipo_actividad_id U, hora_inicio T nullable, hora_fin T nullable | (turno_id, usuario_id, tipo_actividad_id) |

V15 usuario_id fue dropped por V19; no queda en parent checksum. V19 menciona también
drop de antigua actividad: columnas dropped no cambian la secuencia viva anterior.
UUID PK compara lexicográficamente tuplas de componentes16bytes unsigned, no Java
natural para binding. PK compuesta conserva componentes y orden; no concatenación
ambigua ni surrogate. Column-name/type incluidos por field y table/schema contract;
row count exacto, y schemaFingerprint observado vincula schema instalado.

R2 reutiliza **sólo el fundamento compartido LP/SEQ** de D36.8. La instancia siguiente
es decisión R2-CHECKSUM propia coherente con D20.4: misma SHA-256/framing, domains R2
separados, codecs de §3.2 y tags aquí sellados; no domains/query/scope/column set/
FilaHash(UUID simple) de Reserva ni nueva exigencia de algoritmo o vectores.

```text
field = SEQ("F2E-R2-CHECKSUM-FIELD-V1", columnName, typeTag, "V", canonicalValue)
nullField = SEQ("F2E-R2-CHECKSUM-FIELD-V1", columnName, typeTag, "N", emptyBytes)
rowHash = H("F2E-R2-CHECKSUM-ROW-V1", ASCII(columnCount), fieldsSchemaOrdinal...)
tableHash = H("F2E-R2-CHECKSUM-TABLE-V1", schemaTable, ASCII(rowCount),
               rowHashesPhysicalCompositePkOrder...)
checksumScope = SEQ("F2E-R2-CHECKSUM-SCOPE-V1", "READ_FOR_DATE", scopeBytes,
                    ASCII(frozenTurnCount), frozenTurnUUIDsUnsigned...)
entry = SEQ("F2E-R2-CHECKSUM-SLICE-TABLE-ENTRY-V1", schemaTable, tableHash)
sliceHash = H("F2E-R2-CHECKSUM-SLICE-V1", checksumScope, "3", entriesUnsignedUtf8TableName...)
```

Tags U/S/D/T/Z/B/I: UUID lower canonical, raw UTF8 string sans trim/casefold,
date/time/UTCmicros §3.2, true|false, signed decimal Short/integer. No float/double,
sub-microsecond, implicit NULL scalar or literal NULL replacing null framing.
SQL NULL N/empty distinto de S value empty/"null"/0/false. Table vacío es
H(TABLEdomain,table,"0"); no implicit sentinel. Entries orden exacto:
public.turno_instructor, public.turno_instructor_asignacion, public.turno_instructor_usuario.
Counts/hash por tabla y slice idénticos before/after; tablas/dimensión/scope no se omiten.

Quiescent no-write: Flyway→fixture/grants/control commit→freeze parents→before all
three→reader TX RR/capture/proxy→commit real→after all three/predicate set→compare
capture/statistics→cleanup. Sin writers concurrentes durante esa ventana.
Native RR concurrency es **otra prueba**: writer admin comitea cambio de asignación
entre MEMBERS y ASSIGNMENTS con latches; R2 conserva snapshot inicial y lectura
posterior con attempt nuevo ve nuevo estado. Control negativo RC evidencia interleaving
mediante owner/raw-reference de prueba separado que reproduce sólo las dos DATA:
nunca usa LegacyTurnReadContext/R2 reader/claim válido, ni habilita fallback RC.
La invocación del reader R2 en RC es otro negativo y debe rechazar antes de DATA.
Sin reutilizar before/after checksum concurrente para atribuir writes a R2. Observador
no llama SQL desde reader, datos materiales/custodian/credential de producción no son
prerrequisito del container; audit material sigue NOT_AUTHORIZED/DATA_SOURCE_NOT_AVAILABLE.

## 8. R2-ARCH — decisión única para el guard físico de R1

Se inspeccionó completo
`src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/ReservaJpaReaderArchitectureTest.java`.
Su helper Files.walk enumera recursivamente read y adapter/jpa main, y adapter/jpa test;
el método allowlist compara exactamente 11 main/10 test, no sólo Reserva files.
Una nueva clase R2 autorizada en D22/24.1 sería extra por esa inspección estática;
no se afirma test ejecutado/fallado ni defecto/R1 reabierto.

**Estrategia única elegida R2-ARCH:** conservar raíces compartidas D22 y ampliar
**futuramente sólo este archivo existente** para expresar composición de sets sellados.
Es una excepción de diseño explícita a la regla anterior "existing shared-testinfra only"
de D24.1 para esta sola ruta, limitada a enumeración/preservación, pendiente del mismo
fresh design audit/gate; no permiso presente para editarla. Evitar raíces/prefijos o
debilitar exactsets no satisface D22 ni guard de unknown extra.

Invariantes futuros: conservar literalmente el set R1main11 y R1test10 hoy en ese
archivo; verificar cada miembro existe y sigue bajo mismas reglas no-stereotype/
writer/escape. Un R2 handoff futuro deberá sellar separadamente `R2mainAllowlist` y
`R2testAllowlist` con filenames exactos auditados. Disjuntos de R1 y entre sí por roots;
sets unknown/pending no son aceptables para ejecución. Enumeraciones físicas separadas
por ownership explícito, más igualdad global, sin filenames guessed aquí:

```text
actualMain == R1main11 UNION SEALED_R2mainAllowlist
actualTest == R1test10 UNION SEALED_R2testAllowlist
actualR1Main == R1main11; actualR1Test == R1test10
actualR2Main == SEALED_R2mainAllowlist; actualR2Test == SEALED_R2testAllowlist
```

Unknown extra/faltante/overlap falla; no startsWith Reserva para esconder extras,
wildcards/skip/exclusion root/count-only. Shared testinfra modificado sigue owned por
R1set, no se cuenta como newR2; sus extensiones exigen exactapproval/preservation proof.
Conservar las otras pruebas guard: R1 plain/stereotypes; tokens prohibidos sobre
raíces completas; singleton ProjectionCatalogVersion/SnapshotClaim R1 exactamente1;
dos métodos R1 con `f2eReaderTransactionManager` MANDATORY/readOnly; no weakening.
Catálogo/claims/context R1 no se amplían. R2 tiene enumeración/contract assertions
propios separados y aplica no-writers/no escapes/no prod reachability D23/26.6.
No futura excepción a ningún otro existing test fuera de shared testinfra.
Un futuro executor debe obtener el handoff exacto que allowliste esta ruta antes de editar.

## 9. Las siete capacidades D24.1 recuperadas exactamente

Prefijo de fuentes testinfra `T`:
`src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/testinfra/`.
Clasificación distingue capacidad, source path físico y semántica/ownership. No
imputa genericidad al import/config/harness entero ni autoriza editar todas las fuentes.

| Capacidad exacta D24.1 | Clasificación | Source path / realidad física | Ownership y preservation proof futuro |
| --- | --- | --- | --- |
| PostgreSQL container | REUSE_AS_IS | T/F2ePostgresTestConfiguration.java, factory postgres:16-alpine | Reusar capacidad de arranque; no importar configuración Reserva completa. R2 monta su grafo test-only. Imagen/lifecycle R1 unchanged, R1 unit/integration regresión competente si se toca seam compartido. |
| Flyway boot | REUSE_AS_IS | T/F2ePostgresTestConfiguration.java, migrate/validate/currentV47 | Reusar algoritmo/schema fingerprint neutral; bootstrap contiene fixtures/key r1-a/applied50 y beans Reserva, no son R2 fixture ni universal threshold. Preservar todas las verificaciones R1 sin rebajarlas; R2 exige PG16/V47/valid checksums/no unexpected failed-pending/ddlvalidate. |
| SELECT-only setup | EXTEND_WITH_R2_NEUTRAL_CAPABILITY | T/F2eSelectOnlyRole.java, principal f2e_r1_reader_ y grant reserva | Capacidad neutral accepts sealed slice grants; R1 factory conserva principal/grants/denied control y wrapper behaviour exactos. R2 descriptor/login tres tablas separado; tests verifican no widening de R1 y negatives R2. |
| SQL inspection | EXTEND_WITH_R2_NEUTRAL_CAPABILITY | T/F2eStatementPolicyInspector.java, R1 four-entry catalog/deny pg snapshot | Reusar normalization/hash/capture enforcement, instancia catálogo R2 separada; R1 default four entries y snapshot denied intactos. Pruebas unknown SELECT/direct-wrapped policy identity/JDBC0 y R1 exactmanifest preservadas. |
| Checksum canonicalizer | EXTEND_WITH_R2_NEUTRAL_CAPABILITY | T/F2eSliceChecksum.java, Reserva selectors/FilaHash simpleUUID | Seam neutral compound PK/all-column/slice separados, §7. R1 selectors/order/domains/LP-SEQ/goldenbytes unchanged; recompute R1 original vectors + R2 tuples/null/allcolumns scope/counts. |
| Architecture rule | EXTEND_WITH_R2_NEUTRAL_CAPABILITY | ReservaJpaReaderArchitectureTest.java fuera de T, rootrecursive exactmain11/test10 | Excepción sólo §8. Invariantes/sets R1 preservados; R2 sets separados SEALED, union equality/noextra; no singleton widening. No otro test R1 modificado. |
| Transaction test owner | NOT_REUSABLE | T/ReaderTransactionTestHarness.java, ReservationReadPort/RC/Reserva snapshots/registry/manifest | Runtime API/harness entero no sirve R2. Reusar concepto owner test-only y seams neutrales autorizados, crear owner R2 distinto §6. R1 RC/registry/fórmulas/N3 no editados ni reinterpretados; R2 RR evidence/registry/capture separado y regressión R1 si shared seam cambia. |

REUSE_AS_IS se refiere a capacidad aislada ya existente; no promete que archivo entero
sea generic/importable sin adaptación de wiring. Todo seam nuevo/edit compartido deberá
tener filenames exactos y aprobación **en handoff futuro** y demostrar R1 semantics
unchanged; pruebas actuales no se ejecutan ni autoaprueban aquí.

## 10. Aceptación futura y mutation categories

Contrato de aceptación derivado de D12.3/26/27 y decisiones nuevas de integración:

| Materia | Evidencia objetiva futura requerida |
| --- | --- |
| Projection/binding | PG16 tipos/aliases/ordinals §5, named scalars/UUIDlist, natural order, real slot capture, null/empty preSQL, omit query2 empty, SQL y output ordering |
| Atoms/gaps | K y matrix0/1/N completa; PKtriple/URN/physical recordIds; nonmembers sin filtro; fallback/raw incomplete/outside/gaps, closed markers; totals exactos |
| Semántica | recurrent compatible/incompatible/history legal; dos scenarios puntuales exactos con/sin anomalía; UNKNOWN_INTENT precedence; timestamps technical-only, cero inferred intent/history |
| Abort | required/type/header no-key correlacionable queda privado hasta ASSIGNMENTS para K exacto; 0 assignments→1 gap, 3 activity PKs→3 rejections; keys insuficientes→1 unidad por ordinal y enumeración de otros parents; acceso/policy/binding/recurso/TX aborta inmediato sin K inventado; duplicate PK/identity→1 rechazo con physical multiplicity; siempre published/evaluations/results0 y ningún classifier/candidate parcial |
| Immutability/identity | defensive copies/payload inputs intactos/maps keyset exacto, recompute cinco fórmulas/scope/map/fields/provenance cross-consistency, source-only read set; ninguna managed/lazy/proxy escape |
| RR | advisors reales ownerREQUIRES_NEW/RR/readOnly y readerMANDATORY; outside/wrongTX negativos, descriptor/bound Session/nativePgConnection; I/R/RESOURCE/S inicio/final exactos, tupla database/schema real, snapshot igual; guards locales zeroSQL y getSchema/getCatalog prohibidos; same evidence por statement, real completion |
| Capture/no-write | CLOSED_SET seis shapes (2DATA+4probes), plan/binds/inspector/native execute independientes incluyendo RESOURCE zero binds; successful paths y failure prefixes §6.3, resource mismatch inicial con probes previos/zeroDATA; bootstrap separado; denied controls/SELECT-onlyexact3tables+built-ins mínimos, all-column scoped checksums/counts quiescent unchanged, no rollback/statistics como sustituto |
| Concurrencia | writer controlado entre members y assignments, RR snapshot stable/pasada posterior cambia; control RC raw/reference TEST-ONLY separado sin context/readForDate R2 aceptado; R2 reader en RC rechaza antes de DATA, cero downgrade; ventana distinta de checksum no-write |
| Host/schema | Docker/PostgreSQL16Testcontainers, FlywayV47 migrate+validate/checksums sin failed/pending inesperado, ddl-auto validate/no H2; plan estático Product Delivery competente, source/index integrity |
| Arquitectura/runtime | §8 union noextra/R1 preserved/R2separate; core sin framework, plain constructorDI, test proxies explícitos; default/prod beans ABSENT, cero controllers/consumers/productivecallers/config/trigger/reader switch |

Se prueba compatibilidad de sources con core puro usando inputs sintéticos completos,
sin implementar/invocar R3–R6 ni generar candidates dentro del reader. No tests
ejecutados, BUILD PASS/R2IMPLEMENTATION_ACCEPTED o aceptación actual declarados.
`NOT_YET_NORMATIVE`: nuevos golden vectors, fingerprint/commitment ordering pre-callback,
comandos Maven exactos, futuros conteos de archivos/tests/suites/host, R1 59/649/host7/native4,
lista histórica TECH/GAP y requisito native first/later R1. Un handoff posterior fija
comandos/filenames/plan host autorizados; ausencia de ellos aquí no permite al executor
decidir arquitectura ni ejecutar ahora. Captura literal ordenada ya sellada es distinta
de ese fingerprint de statements diferido.

Future mutation categories, **no allowlist de implementación presente**:

| Categoría | Límite |
| --- | --- |
| EXPECTED_NEW | D24.1 port/scope/readset/rejection/context R2; dos concrete rows/executor, mapper/aggregator/reader, sólo read/adapter.jpa.projection/mapper/adapter.jpa; R2 unit/JPA/shape/scenario/count/binding/tx/RR/no-write/runtime/architecture tests y su test-only wiring |
| EXPECTED_MODIFIED | sólo seams shared testinfra expresamente seleccionados por futuro handoff y única excepción exacta ReservaJpaReaderArchitectureTest.java §8; ninguna inferencia de que todos requieran edición |
| READ_ONLY_DEPENDENCY | core, productionR1/Reserva/context/identities/catalog, legacy entities/repos/services, F2D, schema/migrations/Flyway/pom/runtimeconfig y canónicos existentes |
| FORBIDDEN | cualquier production tracked existente/core/legacyR1 mutation, schema/migrations/pom/config/applicationresources, controllers/front/mobile/jobs/listeners/runners/consumer/Payments/Notifications; R3–R6/composition/reportsink/materialaudit/crosswalk/resolver/fence/migration/activation/cutover |

Conceptual types D nombrados aquí no son filenames allowlisted; único futuro existing
test exception queda exacto §8. No implementation handoff se materializa en esta unidad.

## 11. R2-PROFILE — lifecycle competente seleccionado

`WORKFLOW_PROFILE=R2_DESIGN_RESEARCH_LOCAL_MATERIALIZATION` para el Run actual:
PREPARE físico→DOCUMENT dos paths permitidos→SCOPE verificado→FRESH_INDEPENDENT_DESIGN_AUDIT
→audit inicial FAIL→CORRECT documental acotado→RE-AUDIT fresh independiente
→gate coordinador de diseño/documentación. Scope/safety APPLICABLE; re-audit y gate de
estos bytes `PENDING_FRESH_REAUDIT_AND_GATE`. Implementation/tests/build/host/JDBC/container/SQL ejecución
`NOT_APPLICABLE` por ausencia de materia técnica; no se fabrican como PASS.
ESTADO postgate y review auditor pertenecen al coordinador/rol independiente,
fuera de allowlist del documenter. Publication en este Run `NOT_AUTHORIZED / NOT_EXECUTED`,
no staging/commit/push/ref/index mutation, auto_publish=false.

**Selección concreta para R2, no regla universal:** después de audit fresh y gate
competente, perfil separado `R2_DESIGN_AUTHORITY_PUBLICATION` antes de activar un
futuro implementation handoff R2. Razón: esta nueva integración necesita autoridad
versionada/sellada, consumible sin depender de /tmp/Orca/chat y de candidate local.
El usuario permite ese lifecycle separado como próximo posible trabajo; ejecutar
publicación requiere su preflight/scope exacto/auditor fresh/gates reales y policy
de publicación separados. No se publica ahora ni se inventan future Task/Gate IDs.

Después del gate de diseño, **handoff AUTHORING readiness** puede evaluarse por nueva
autorización documental; esa readiness no significa autoridad PUBLISHED ni handoff
APPROVED/ACTIVE. Este perfil R2 exige publication y closure gates competentes de la
autoridad antes de implementation handoff ACTIVE; luego el handoff propio necesita
audit/aprobación/activación e implementación autorizada separadamente. El presente
handoff de research nunca pasa a implementation ACTIVE por esa secuencia.
Readiness para authoring/publicación no declara ejecución ni aceptación R2.

Base protocolo: WORKFLOW §Workflow profile/bloques componibles, §Workflow documental,
§Workflow de publicación/cierre; STATE-MACHINE §Dimensiones; GATES §Aplicabilidad y
requisitos de transición; ROLES independencia. Publicación es selección competente
profile-specific, **no precondición universal** del protocolo; publicación no auto,
no productividad/cutover, futuros gates PENDING sólo cuando su lifecycle separado aplica.
Materializar provenance del handoff actual no autoaprueba esa autorización ni estos bytes.

## 12. Provenance cláusula por cláusula y cierre candidate de gaps

| Cláusula de esta candidate | NORMATIVE anterior | SUPPORTING físico/preflight | Nueva decisión R2 pendiente |
| --- | --- | --- | --- |
| §1 baseline/estado | AGENTS; README; ESTADO unidadR1/cierre/ACTIVE NINGUNO | BASELINE externo; preflight REPORT/GATE; refs/index/hashes460 | sólo binding de esta entrega, no cambio lifecycle R1 |
| §2 sources/rows/scope/atoms | D5/8/12.1–12.3/AD03–05/25/31/32; S6.2/7/8(D09)/9(D10) | V15/19/20/22, GenericSourceSnapshot/EvidenceProvenance/core | no cambio source semantics; representación cerrada complementada §3 |
| §2 rejections/totalabort/counts | D12.3/19; S10 error/13 failclosed | preflight AUTHORITY; audit inicial AB-02; physical core scenarios | bookkeeping de pending projected errors/UUID keys/ASSIGNMENTS/K exacto; ordinal/duplicate multiplicity preservados; hard operational abort separado |
| §3 context/catalog/registry/trust | D13/18.2/24.1; D37.1 límite R1-only | ReadSnapshotContext/Identifiers R1only; preflight gapA | R2-ID separado, enum/claim/registry/descriptor y bytes/map/keyset específicos |
| §3 formulas/provenance | D13 cinco fórmulas; D12.3 URNs; D36.8 sólo LP/SEQ | core seven fields; preflight gaps/propuestas excluidas | instancia MAP26/metadata exacta, H/codec/order y recompute R2, no R1V2 |
| §4/5 catálogo/binds | D8/12.2/20.2/AD31; D36.4–36.5 capacidad neutral explícitamente adoptada | actual inspector R1four denies snapshot; documentary SHA cálculo; audit inicial B-01 | R2-SQL literal closed6shapes/IDs/aliases/native bind evidence, RESOURCE zero binds; R1four unchanged |
| §6 recurso/owner/snapshot | D15/17/18.1–18.2/19/20/26.4 | current R1config/harness hardcodes; preflight gapB; pgjdbc42.7.11 source primario y audit B-01/AB-02 | R2-RR local guards sin SQL + probe RESOURCE explícito por same sharedEM/native bound resource; capture/prefijos completos; no TECH firstlater |
| §7 no-write/checksum | D20.1–20.4/26.3–26.5; sólo fundamento LP/SEQ D36.8 | V15/19/20/22; role/checksum Reserva hardcodes | R2-CHECKSUM domains/tags/formulas propios, three tables/all columns/compoundPK/frozen parent/select children each pass |
| §8 arquitectura | D22/23/24.1/26.6; DA004/DA013 | full physical ArchitectureTest rootrecursive11/10; gapC | R2-ARCH única exactfutureexception, R1 unchanged guards+SEALED R2+union |
| §9 siete capacidades | D24.1 exactseven, R1 patrón §24 | cinco testinfra source paths y ArchitectureTest físicos; DEPENDENCY recon | classifications por capacidad, no wholesale import ni silent shared edit |
| §10 acceptance/mutation | D12.3/24.1/26/27/28; S13 | preflight corrected synthesis/audit y R1preservation | sólo instancias de integración; comandos/counts/vectors no inventados |
| §11 lifecycle | ORQ README/WORKFLOW/STATE-MACHINE/GATES/ROLES; AGENTS | real preflight gate y actual despacho allowlist | R2-PROFILE publicación separada antes ACTIVE, no requisito universal/no publicación ahora |
| límites siguientes | ESTADO; arquitectura§7–10; DA004/012/013; dominio programación/reservas; mapa legacy programación/cutover; D28/30 | consumed R1handoff/review§12; S17/reviews históricos | ninguna nueva autoridad productiva/sucesor |

Los preflight reports externos son SUPPORTING/provenance, nunca canónicos por sí
solos. D33.2 es propuesta SUPPORTING, no activación. D36–37 y handoff consumido R1
son normativos sólo para preservar R1 y compartir capacidades expresamente nombradas.
Autoridad de esta nueva candidate sigue **PENDING_FRESH_REAUDIT_AND_GATE**;
FAIL inicial y sus dos P1 se preservan, no se convierten en PASS por esta corrección.

| Gap exacto | Materialización candidate | Estado competente actual |
| --- | --- | --- |
| A contexto/physical-gap identity/provenance/maps/formulas/envelope | §§2–3 R2-ID, instanceD13, immutable sources-only ReadSet; AB-02 bookkeeping corregido para exact K | CORRECTION_MATERIALIZED / PENDING_FRESH_REAUDIT_AND_GATE |
| B SQL/binding/capture/RRresource/no-write/checksum | §§4–7 R2-SQL/RR/CHECKSUM, six shapes/RESOURCE, guards locales y native association; B-01/AB-02 prefijos corregidos | CORRECTION_MATERIALIZED / PENDING_FRESH_REAUDIT_AND_GATE |
| C preservación arquitectura/siete capacidades | §§8–9 R2-ARCH única estrategia/excepción futurebounded y classifications intactas | RESOLUTION_MATERIALIZED / PENDING_FRESH_REAUDIT_AND_GATE |

Exit de este corrector: sólo corrección diseño/research materializada y scope/hash before-after
entregado externamente; ningún self-audit/gate/P0-P1 inventado. Siguiente control
único de estos bytes: fresh independiente design/document auditor y gate coordinador.
Revisión competente determinará suficiencia; tabla candidate no se autoeleva a gaps CLOSED.

TurnoInstructor `LEGACY_VIVO / PRODUCTIVO`; R1 `CLOSED / ACCEPTED / PUBLISHED / IMMUTABLE`.
R2 `DISEÑADO_NO_IMPLEMENTADO / DARK_LAUNCH / NON_PRODUCTIVE`, sin activación.
Default/prod beans ABSENT diseñado, productive callers/controllers/consumers/config0
como obligación futura, no contexto test ejecutado. R3–R6 `NOT_AUTHORIZED_IN_R2`;
Payments/Notifications OUT_OF_SCOPE. Crosswalk/selection/resolver/fence/reportsink/
materialaudit/migration/backfill/cutover NOT_AUTHORIZED, cutover=false. OldAutopilot,
FeelingPilatesOrchestrator y HostValidator nominal `OLD_PROCESS_ONLY`; la invariante
hostPG16/Flyway/Testcontainers real se preserva para futuro Product Delivery competente.
