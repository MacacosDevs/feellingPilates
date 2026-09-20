# F2E — policy operacional candidata R1

Version: `F2E-EXECUTION-POLICY-V1`; authority class: `PROCESS_PROPOSAL / NON_PRODUCT_AUTHORITY`.
Status: `MATERIALIZED_CANDIDATE / PENDING_FRESH_INDEPENDENT_PROCESS_AUDIT`.
Annex version-bound to installed Orca `1.4.205`; no modifica ORQ-PROTOCOL-V1.
El [RUNBOOK](F2E-RUNBOOK.md) explica el HOW y el [STATE](F2E-STATE.json) es cache derivada.
Scope presente `F2E_OPTIMIZED_EXECUTION_BOOTSTRAP_R1 / PROCESS_ONLY / DOCUMENTATION_ONLY`.
Ninguna optimización propuesta está aceptada/activa por este archivo o este Run.

## 1. Autoridad, alcance y activación prospectiva

ORQ README prohíbe fijar SKUs/runIDs/contadores en el protocolo genérico: esta policy
es un **anexo operacional F2E versionado**, con catálogo/routing bound a versión y
evidencia física. Los cinco documentos genéricos quedan sin modificación y prevalecen;
no se copian/duplican sus reglas para reemplazar autoridad. Las referencias y hashes
competentes están en STATE y el mapping §11 distingue norma previa, propuesta y hecho.
Contradicción: fail closed, AUTHORITY_RECONCILIATION_REQUIRED, sin override tácito.

La selección USER_SELECTED_PROCESS_ONLY del gate `gate_11bf6b2c5681` abre sólo
materialización de tres documentos, verificación coordinadora, audit independiente
fresh y bootstrap gate; terminal competente máximo READY_FOR_CONTROLLED_PROCESS_PUBLICATION.
Autor puede entregar MATERIALIZED, nunca self-audit acceptance. Publicación de
proceso, activación de proceso, implementation handoff R2 e implementación son
lifecycles separados, todavía NOT_AUTHORIZED aquí. No nuevo requisito funcional.
R1 cerrado/aceptado/publicado, R2 diseño completo/auditado/publicado/cerrado,
ACTIVE HANDOFF NINGUNO, R2 implementation NOT_AUTHORIZED, legacy productivo,
dark launch preservado, cutover y R3–R6 no autorizados; sin integración Payments.

Las clases siguientes sólo rigen **después** de publicación/activación de proceso
competentes, con su scope explícito. ACTIVE_IMMEDIATELY es prioridad propuesta
para esa futura activación, no permiso para usar la optimización en este bootstrap:

| Clase prospectiva | Prácticas / límite |
| --- | --- |
| ACTIVE_IMMEDIATELY | Tool-first, delta-first, compact return EXTENDS, RAW/SUMMARY separados, debugging falsable, fan-out adaptativo, no silent fallback, métricas ligeras, precedencia/subordinación cache y Human Gates. Invariantes equivalentes ya normativos siguen rigiendo por ORQ actual. |
| PILOT_FIRST | Luna mechanical+deterministic validation; Terra bounded; progressive disclosure; FAST/GATE; presupuestos correctivos; STATE packaging. Cada piloto necesita autoridad y medida independiente. |
| DEFER | Terra high-risk fresh acceptance; Luna high-risk backend; automatic publication/cutover/activation; aggressive RAW deletion; automatic schema reconciliation; unsupported tooling. |
| DEFER / FORBIDDEN | Silent fallback: prohibido; DEFER no es permiso posterior. Acciones automáticas o destructivas fuera de autoridad tampoco se habilitan por una etiqueta. |

Cambiar routing/modelo/effort/scope exige nuevo binding/registro y autoridad pertinente,
sin inventar variantes de Terra ni calidad por nombre. Una promoción de piloto
necesita medición/audit independiente y decisión competente, nunca autopromoción.

## 2. Capability discovery física anterior a routing

E = `/Users/jesusaldaircruzortiz/.codex/feelingpilates-evidence/run_748f43974201`.
Ejecutable seleccionado `/usr/local/bin/orca`, aplicación `/Applications/Orca.app`;
version `1.4.205`, runtime ready/reachable
`0e2c16d5-24a6-467e-a596-62595a0e2721`. No sustituir ejecutable si falla.
Se leyó schema real COMMAND-SCHEMA, help worker-start, status/runtime y catálogo
del ASAR instalado; no investigación remota necesaria para hechos locales.

| Evidencia SUPPORTING física | SHA-256 / identidad |
| --- | --- |
| E/COMMAND-SCHEMA.json | 96a99d38310ff04e8b5f0d0b2d497b4aae9500d08c62e6f1ae0fcba6653a8b80 |
| E/MATERIALIZER-WORKER-START-HELP.txt | 5c6263a7ea66d1b46669b4372aaa416a08c2ba11c0ed1f6489441f758113cbee |
| E/MATERIALIZER-STATUS.json | 92798f4bfb6aee4ae4d6edf9de53eba5c803734d55147e4e482245772614d73d |
| Installed app.asar | e9dc6ccd1cc9304b056239cf7d25ef6ad0cae1abf73dc241c305b83812fbb2bd |
| ASAR out/main/index.js | 1ab3d722b64274fd3e8475cb31d52e047fd17d8d19560eaf6eb047acae05ff01 |
| E/ORCA-MODEL-CATALOG-SOURCE.txt, original excerpt | f78278685696b1e54b533d226ef2a6f8e0c4e4a0ae85e09b67d0be2e93c8703f |
| E/MATERIALIZER-FULL-LAUNCH-CATALOG.txt, completo5catálogos | 8a99293ec5aa5d76cb0d60fafb935494f59460de778f0230541f282afa07a4c2 |
| E/MATERIALIZER-CATALOG-IDENTITY.json | bd850b58b80429980e97cc4191db9cc9e1b59ec73aa272dc20f902e33f9939af |
| E/MATERIALIZER-LAUNCH.json | cc4eb0d10800ceb06cd3fcb64ecbdf9cf8361b99fd0a546496d004def784930a |
| E/MATERIALIZER-TRANSCRIPT-MODEL.json | 32a314f22bfebb520917048beaf6ee68dc54f3f3297abd81157ba6ceb4b86fce |

Full excerpt es slice UTF-8 exacta entre offsets de caracteres 2640262 inclusive y
2649556 exclusive (`var wyn=` hasta `function tbn(`); incluye registros Jyn de Claude,
Codex, Gemini, Cursor, Grok y validación launch/VF/ebn. Reproducción por script externo
materializer_evidence.py source, lectura directa del header ASAR y rehash internal;
original excerpt e identidades se preservan. Descripciones comerciales del catálogo
no prueban calidad ni disponibilidad provider. Catálogo de otros agentes dinámicos
no fue consultado/ejecutado; sólo IDs estáticos realmente expuestos, sin adivinarlos.

Help/source confirman worker-start: --model acepta opaque provider ID para Claude,
Codex, Cursor; --effort requiere --model y choices válidos. Ninguno combina con
--terminal; no configurar nuevo modelo reusando terminal. Capability remota
`orchestration.worker-launch-preferences.v1` requerida y advertised en destino,
no asumir que local ready prueba remote. El modo de ejecución sigue setting del
usuario y se registra en receipt; no flag caller para elegirlo. No igualar fastMode
del catálogo, modo de validación FAST y superficie de ejecución Orca.

Siempre contrastar launch.requested, launch.effective y transcript observado:
materializador requested=effective codex/gpt-5.6-sol/ultra; TUI observada
`gpt-5.6-sol ultra`, modo receipt terminal. Esto demuestra lanzamiento/observación
actual, no audit de calidad, aceptación crítica ni todos los provider combinations.
Toda otra combinación se declara RUNTIME_UNVERIFIED/CONDITIONAL. Si target no está
disponible, receipt difiere o observación no confirma: stop MODEL_ROUTING_UNAVAILABLE,
escalación registrada; nunca fallback silencioso o modelo distinto con mismo label.

## 3. Matriz exhaustiva de catálogo, effort y modo

Dominio Codex exacto E7 = minimal, low, medium, high, xhigh, max, ultra.
Claude C5 = low, medium, high, xhigh, max. Cursor C3 = low, medium, high.
Grok G4 = low, medium, high, xhigh. `standard` significa ausencia de opción de modo,
no es nombre de un modo/flag nuevo de Orca. `UNSET` representa no opción explícita,
**no** un effort/model SKU adicional. CatalogDefault sólo describe UI/launcher;
worker-start sin --effort deja elección efectiva a configuración/provider y requiere
observación, no equivale a launch con un default explicitado.

Cada lista de la tabla se expande cartesianamente: **cada** modelo individual ×
**cada** effort listado × **cada** valor de modo listado tiene el estado de su fila,
salvo excepción Sol ultra explícita. E/ROUTING-MATRIX.json enumera esas celdas y los
rechazos Codex de efforts no soportados. Opciones fuera de listas/modos declarados
son NOT_SUPPORTED; desconocido opaque puede ser aceptado por launcher pero es
NOT_CATALOG_CONFIRMED y routing diferido, no confirmación del catálogo.

Routing labels: PROVEN_DEFAULT requiere evidencia de task real, validación y audit
fresh independiente satisfactorios + decisión de promoción; **ninguna celda lo posee**
al corte. PILOT_FIRST = sólo piloto bounded expresamente autorizado, no ejecución
probada. ESCALATION_ONLY = target reservado para riesgo/judgment elevado o alternativa
fuera del piloto, sólo con selección competente. NOT_SUPPORTED = no launch capability
o no elegibilidad de esta policy, distinguir ambos motivos en evidencia.

| Agent / cada model ID exacto | Cada effort catalogado / default | Cada modo catalogado | Launch evidence y routing prospectivo |
| --- | --- | --- | --- |
| codex / gpt-5.6-sol | E7 / medium | standard; no fastMode declarado | Declared launch choices; ESCALATION_ONLY, runtime-unverified salvo ultra observado actual. Calidad/task acceptance no probadas. |
| codex / gpt-5.6-terra | E7 / medium | standard; no fastMode declarado | Declared choices; PILOT_FIRST bounded; cada effort runtime-unverified/conditional. High-risk fresh acceptance NOT_SUPPORTED por policy/DEFER. |
| codex / gpt-5.6-luna | minimal, low, medium, high, xhigh, max / medium | standard; no fastMode declarado | medium PILOT_FIRST mechanical+validation; otros efforts ESCALATION_ONLY fuera de piloto planeado, runtime-unverified. High-risk backend NOT_SUPPORTED por policy/DEFER. ultra NOT_SUPPORTED por catálogo. |
| codex / gpt-5.5 | minimal, low, medium, high, xhigh / medium | standard | ESCALATION_ONLY condicionado a decisión competente y runtime verification; max/ultra NOT_SUPPORTED por catálogo. |
| codex / gpt-5.2-codex | minimal, low, medium, high, xhigh / medium | standard | ESCALATION_ONLY condicionado; max/ultra NOT_SUPPORTED por catálogo. |
| claude / fable | C5 / high | standard | Choices declaradas PF(true); ESCALATION_ONLY condicionado, runtime-unverified. |
| claude / opus | C5 / high | fastMode=false, fastMode=true | standard launch+effort declarados; ESCALATION_ONLY runtime-unverified. fastMode true es sólo toggle MID_SESSION /fast, NOT_SUPPORTED por worker-start para seleccionar ese modo; no flag fast aquí. |
| claude / sonnet | C5 / high | standard | Model UI default; ESCALATION_ONLY condicionado, runtime-unverified; no task calidad demostrada. |
| claude / haiku | UNSET / no opción effort | standard | --model declarado, effort explícito NOT_SUPPORTED; ESCALATION_ONLY condicionado, runtime-unverified. |
| cursor / auto | UNSET / no opción effort | standard | Launch --model declarado, ESCALATION_ONLY condicionado; policy crítica exige provider exacto observado, auto no prueba selección conocida. effort explícito NOT_SUPPORTED. |
| cursor / gpt-5.3-codex | C3 / high | fastMode=false, fastMode=true | effort compuesto en opaque model por launcher; false ESCALATION_ONLY conditional. true expuesto UI/launcher general, NOT_SUPPORTED como override worker-start (sin flag fastMode). Runtime-unverified en ambos. |
| cursor / claude-opus-4-8 | C3 / high | thinking=false, thinking=true | thinking true default UI; effort compuesto; ESCALATION_ONLY conditional con thinking observado. Ambos valores expuestos UI, selección explícita thinking NOT_SUPPORTED por worker-start; no flag thinking. Runtime-unverified. |
| gemini / gemini-3-pro-preview, gemini-3-flash-preview, gemini-2.5-pro, gemini-2.5-flash | UNSET / no effort | standard | Catálogo general real, sin supportsWorkerLaunchPreferences; NOT_SUPPORTED para este routing worker-start con --model. No provider ejecución demostrada. |
| grok / grok-4.6 | G4 / high | standard | Catálogo general real, sin supportsWorkerLaunchPreferences; NOT_SUPPORTED para worker-start --model. Runtime-unverified. |
| grok / grok-4.5 | low, medium, high / high | standard | Igual; xhigh y cualquier otro effort fuera de choices NOT_SUPPORTED. |

Todos los Codex E7 choices exactos lowercase; no Terra-preview/Terra-fast/Terra-high
SKU inventado. Todos los modelos admiten registro de UNSET efectivo sólo observado;
no promoverlo a esfuerzo conocido. Claudes minimal/ultra, Cursor minimal/xhigh/max/ultra,
Codex modos fast/thinking y cualquier modo no declarado: NOT_SUPPORTED. UI exposé
no prueba que worker-start tenga parámetro para ese modo. Cursor thinking default
no constituye evidencia de ejecución ni equivalencia a Claude agent opus.
Configuraciones UI/mid-session requieren autoridad, capacidad propia y recibo observado
antes de routing; no alterar una ejecución bound de audit para probarlas.

## 4. Routing por riesgo y pilotos previstos

La elegibilidad de un task cruza catálogo+runtime+scope+autoridad+independencia;
el modelo no crea autoridad. Sol ultra es el mayor effort con receipt/observación
actual y target seleccionado para trabajo crítico sujeto a competencia demostrada.
Una futura aceptación crítica necesita auditor fresh capaz, exact candidate/autoridad
y pruebas completas; no se declara Sol adecuado por nombre ni este documenter como
audit proof. Otros Sol efforts requieren demostrar suficiencia por tarea.

| Task type prospectivo | Target y delimitación | Escalar ante |
| --- | --- | --- |
| Mechanical inventory/hash/manifest; deterministic result packaging | Luna medium PILOT_FIRST; scripts first, scope cerrado y validación independiente | Divergencia/hash faltante, P0/P1, semántica, autoridad o cualquier write fuera de scope |
| Bounded technical/reasoning/docs; P2 mechanical; P1 ya autorizado inequívoco | Terra PILOT_FIRST, effort explícito del E7 según task; cero semántica nueva | Alternativas válidas, root cause incierta, P0/P1 nuevo/no acotado, TX/PG/JPA/shared schema/scope conflict |
| Arquitectura compleja, JPA/RR/native JDBC, concurrencia/snapshot, autoridad/ambiguous P0/P1, cross-lane, fresh high-risk acceptance | Sol ESCALATION_ONLY, mayor effort demostrado suficiente; selección y fresh independence competentes | Falta de autoridad/evidencia/capacidad → Human Gate; modelo más capaz no resuelve juicio humano |

Pilotos Terra previstos: resumen técnico de diff ya autorizado con archivos referidos;
reconciliación de referencias documentales mecánicas sin reinterpretar diseño;
corrección P2 acotada por autoridad; una corrección P1 con solución única autorizada.
Cada piloto necesita nuevo task/scope/audit, éxito o falla medidos y escalation criteria
anteriores. No se lanzó ningún task Terra ni Luna aquí; pilot actual registry vacío.
High-risk Terra acceptance y Luna backend siguen DEFER. No aumentar effort o cambiar
SKU silenciosamente para salir de fallo; target unavailable stop y receipt de escalación.

## 5. FAST/GATE, evidencia y fan-out

RUNBOOK §§4–7 fija operación y provenance; clasificación FAST/GATE PILOT_FIRST.
FAST sólo desarrollo/corrección; GATE única vía de aceptación, fresh exact candidate,
allowlist, validations competentes y audit independiente. No adelgazar safety proofs
ni reciclar PASS históricos como fresh. El profile manda aplicabilidad, no ahorro.
Document-only sin drift/obligación técnica: tests/DB/host NOT_APPLICABLE, no PASS.

GATE implementativo pertinente incluye targeted/full regression, PG/Testcontainers,
Flyway vigente compatible, JPA owner/TX/isolation/readOnly/native physical resource,
multi-statement RR snapshot, catálogo/capture SQL+bounds, SELECT-only/denied writes,
checksums/no-write/concurrencia, architecture/core purity, ausencia default/productive
y zero callers, cross-lane/schema/diff, fresh audit/Decision Gate. Real host cuando
autoridad D27/implementación lo requiera. No new semantic tests contract aquí: abrir
D26/27 y R2 §§4–10. Raw reuse sólo mismos bound bytes/env/scope/authority/freshness
permitida por competent profile; required fresh no se reemplaza por reuse.

Default fan-out propuesto uno competente + validación determinista + uno NUEVO fresh
independiente + gate. Extra workers sólo trigger documentado P0/P1, TX/concurrencia,
evidence conflict, arquitectura/authority, PG/JPA, cross-lane/schema; registrar
bounded ownership y rol. Nunca confidence-only fan-out ni concurrent modifiers.
En bootstrap un único materializador, auditor futuro coordinado separadamente.
RAW retenida y SUMMARY con refs/hashes son obligatorios según RUNBOOK §7; no RAW
deletion agresiva, summary-only acceptance, archivos /tmp sin retención ni selfaudit.

## 6. Presupuestos correctivos prospectivos y debugging

Strict proposed F2E pilot; NOT_ACTIVE en bootstrap, que sigue normal ORQ.
Contadores por clase y stage (technical/documentation/publication-closure), causales
por finding; no reset al cambiar worker/effort o profile sin decisión competente.
Un intento materializado y validado consume ciclo; operational pre-semantic failure
sin resultado consumido se registra aparte conforme ORQ, sin semantic cycle ficticio.

| Clase de corrección | Máximo candidato / requisitos |
| --- | --- |
| P2 mechanical | 2; scope/autoridad explícitos, nada incidental |
| P1 bounded inequívoco inside authority | 1; artefacto correctivo competente y NUEVO fresh audit posterior |
| JPA/TX/snapshot | 1 sólo si autoridad ya fija exactamente semántica/solución; si no HUMAN_GATE inmediatamente |
| P0 judgment/authority contradiction | 0; Human Gate |
| Scope/schema/migration/cross-lane ownership/API/domain/productive activation/cutover/out-of-allowlist | 0; Human Gate antes de escribir |

Las clases se solapan aplicando límite más restrictivo, nunca sumar allowances.
Agotar → CORRECTION_BUDGET_EXHAUSTED fail closed; no loop ni task nuevo para evadirlo.
Los P2 no bloquean por defecto salvo profile concreto: presupuesto limita corrección,
no convierte el P2 histórico en blocker. Re-audit fresh independiente obligatorio
tras cambios y competent GATE requerido para aceptar. Debug reproduce → root cause
→ invariant autorizado/known-working → hipótesis única falsable → cambio mínimo
autorizado → targeted validation → fresh re-audit; cero random fix loops.

## 7. Human Gates completos y stops de proceso

Cada código siguiente falla cerrado y es human-owned judgment. El coordinador detecta,
registra evidencias/hashes y escala; **detectar no autoriza resolución unilateral**.
Trabajo dependiente detenido hasta decisión humana explícita y autoridad/versionado
pertinentes; ni Sol ni un gate automático crean esa decisión.

| Clase | Códigos exactos |
| --- | --- |
| Producto/dominio/contrato/diseño | PRODUCT_DECISION_REQUIRED; DOMAIN_RULE_CHANGE_REQUIRED; API_CONTRACT_CHANGE_REQUIRED; DESIGN_AUTHORITY_CHANGE_REQUIRED; IMPLEMENTATION_HANDOFF_SCOPE_CHANGE_REQUIRED |
| TX/snapshot/recurso/isolation | JPA_TRANSACTION_SEMANTICS_CHANGE_REQUIRED; SNAPSHOT_CONSISTENCY_RULE_CHANGE_REQUIRED; PHYSICAL_RESOURCE_IDENTITY_CHANGE_REQUIRED; ISOLATION_LEVEL_CHANGE_REQUIRED |
| DB/dependencias/cross-lane | DATABASE_SCHEMA_CHANGE_REQUIRED; MIGRATION_CHANGE_REQUIRED; FLYWAY_BASELINE_RECONCILIATION_REQUIRED; NEW_DEPENDENCY_REQUIRED; CROSS_LANE_DEPENDENCY_REQUIRED |
| Scope/autoridad/invariante/evidencia | SCOPE_EXPANSION_REQUIRED; AUTHORITY_RECONCILIATION_REQUIRED; NO_WRITE_INVARIANT_BREACH; REAL_HOST_VALIDATION_UNAVAILABLE; EVIDENCE_INSUFFICIENT |
| Findings/presupuesto/ambigüedad | UNRESOLVED_P0; UNRESOLVED_P1_REQUIRING_AUTHORITY; CORRECTION_BUDGET_EXHAUSTED; AMBIGUOUS_AUTHORITY |
| Identidad/seguridad | BASELINE_DRIFT; REMOTE_DIVERGENCE; UNEXPECTED_REPOSITORY_MUTATION |
| Transiciones humanas separadas | PUBLICATION_REQUIRED; PRODUCTIVE_ACTIVATION_REQUIRED; CUTOVER_REQUIRED; MILESTONE_COMPLETE |

Aliases adicionales exclusivamente process stops: MODEL_ROUTING_UNAVAILABLE,
STATE_DRIFT, SNAPSHOT_EVIDENCE_MISMATCH, R1_IMMUTABILITY_BREACH. No enum/código
productivo nuevo ni semántica de detector/reader. Clasificar causa bajo ORQ; metadata
stale inequívocamente reconstruible/operational pre-semantic failure admite recovery
determinista seguro **ya autorizado**, con receipts, sin semantic FAIL inventado.
Aliases no hacen HUMAN_STOP automático por invocation/schema mismatch recuperable;
falta de autoridad, evidencia indispensable irrecuperable, semántica o security breach
sí exige stop/human decision. REAL_HOST_VALIDATION_UNAVAILABLE presupone host requerido
sin camino seguro; Docker sandbox inaccesible con HostValidator disponible no lo dispara.

## 8. Cross-lane, ownership y Flyway integrado

Payments Slice2: IMPLEMENTED/VALIDATED/AUDITED/ACCEPTED/PUBLISHED/CLOSED reportado,
branch  (canonical main base 7298b98231b164e37e090003175ba4dd0c57e060)`pagos/pagos-notificaciones-r1`, HEAD
`8a912217adf6ea2d7d56e3e818845c3338db250e`, Run `run_6859a7f36296`;
V48/V49 y observed V49/52 `COORDINATION_REPORTED / NOT_VERIFIED_HERE / NOT_INTEGRATED_HERE`.
Classification SHARED_BUT_COMPATIBLE + INTEGRATION_POINTS_IDENTIFIED. Fuente competent
coordination: ESTADO cierre R2 cross-lane; no re-audit o Git read de aquella lane aquí.

| Owner | Límite operativo preservado |
| --- | --- |
| F2E | Reserva read/snapshot; Programacion/Turno read sólo scope autorizado por futuro handoff |
| Reservations | Write/lifecycle/state/capacity de Reserva |
| Payments | Financial rights/credits/commitments/consumption-restoration |
| Notifications | Delivery infrastructure |
| TurnoInstructor legacy | Autoridad LEGACY_VIVO/PRODUCTIVO; sin switch/retirement |

CROSS_LANE_DEPENDENCY_REQUIRED **antes** de shared changes a Reserva.java,
ReservaRepository.java, ReservaService.java, tablas/estados físicos de reservas,
ReservationReadPort, ReservaJpaReader, capacity/cupos, TurnoInstructor, Programacion.
Consultar ownership/autoridad de cada lane; no ampliar scope ni escribir mientras se
consulta. Este stop tampoco permite cambiar archivos shared bajo un allowlist ajeno.

Antes de futura validación integrada identificar head Flyway realmente integrado y
validar compatibilidad de migraciones F2E exigidas y autoridad/profile vigentes.
V47/50 no terminales globales permanentes; V48/49/52 de coordinación no migraciones
F2E aprobadas por este anexo. Cero integración Payments, merge/schema migration
reconciliation automática o transfer financiero/Reserva/capacity. Incompatibilidad
→ FLYWAY_BASELINE_RECONCILIATION_REQUIRED / CROSS_LANE_DEPENDENCY_REQUIRED competentes.

## 9. Métricas mecánicas y pilot registry

Cada Run conserva receipts/start/end/fuentes mecánicas. Unknown/unavailable=null,
distinto de cero observado; units explícitas. No tokens/costo/cuota fabricados.
worker launch por receipt Task/Dispatch deduplicado; counters sólo efectivos
observados (requested se registra separado). elapsed por timestamps verificables;
RAW/SUMMARY bytes de manifiestos de archivos, sin doble conteo por referencias.

```text
wallClockMinutes, workersLaunched, lunaTasks, terraTasks, solTasks,
perEffortCounters[exactIdentifier][minimal|low|medium|high|xhigh|max|ultra|UNSET],
targetedValidations, fullValidations, postgresValidations,
testcontainersValidations, realHostValidations, correctionCycles,
freshAuditFindings, humanGates, summaryEvidenceBytes, rawEvidenceBytes,
gateFailures, auditReopens, baselineDriftEvents, crossLaneReviews,
validationReruns, defectsFoundFAST, defectsFoundGATE
```

Las validaciones contabilizan ejecución real+exit/receipt, no planeadas/NOT_APPLICABLE.
Finding count exige audit competente; no asumir0 por auditor pending. Separar stage/
correction class y métricas de tareas versus Run completo. Materializador observado:
solTasks1/terraTasks0/lunaTasks0/effortultra1 en este despacho; freshAuditFindings null
porque audit posterior aún no ocurrió. No inferir fleet totals sin receipts completos.

Terra pilot registry futuro, por task individual, sin actual pilot aquí:

```text
taskId, taskType, modelIdentifier, effort, successFailure,
escalatedYESNO, escalationTarget, correctionRequiredYESNO,
freshAuditFindingsAfterTask, severityOfFindings, wallClockMinutes,
launchReceiptRef, observedModelRef, candidateRef, auditRef
```

YES/NO sólo observado, unknown=null; successFailure con resultado real y audit
separado. PILOT_FIRST tareas planeadas §4 se comparan con modelo/effort reales,
escalaciones, correcciones y severidad residual en audit fresh. Criterio de promoción
necesita tareas independientes representativas y aprobación competente registrada;
el piloto no se declara PROVEN_DEFAULT por lanzamiento exitoso ni por nombre Terra.

## 10. Publication/activation y clases diferidas

Auto publication=false. Bootstrap gate PASS sólo habilita readiness competente
para futura **publicación controlada de proceso** con autoridad/profile/gates propios.
Process activation requiere autoridad competente aparte, seleccionando prácticas
aceptadas y condiciones de piloto; no activa todas por poner ACTIVE_IMMEDIATELY.
Implementation handoff R2 no materializado/activo y R2 implementation NOT_AUTHORIZED;
después de proceso seleccionado, sus lifecycles requieren autorizaciones propias.
R2 design §11/research §4 son suficiencia de diseño para futuro authoring, no permiso
de executor presente. Publicación nunca productive activation/cutover; aceptación
nunca cutover. MILESTONE_COMPLETE no dispara fase funcional ni publicación automática.

No schema/domain/API/dependency decision por optimización; unsupported tooling,
silent fallback y pérdida de RAW no se vuelven legales tras piloto. Las reglas
normativas se abren por referencia y se preservan. Candidate exact audited bytes se
bindan externamente sin post-audit STATE edits; cambio → nuevo candidato/audit/gate.

## 11. Provenance de cada regla de esta policy

N=normativa física preexistente; U=selección/propuesta nueva del usuario, candidata
sin activación; F=SUPPORTING físico sin autoridad normativa. U complete persisted
E/MATERIALIZER-SPEC.txt SHA `49c8e847d30aea2cab2173c0b1f4912a6132a45da070177a6c1f9fef8744a8e4`.
N paths y exact hashes: STATE.authorityRefs/authorityHashes/handoffRefs/handoffHashes;
F paths/hashes: §2, E/MATERIALIZER-SOURCE-HASHES.json y external manifests/report.

| Policy sección/reglas | N: fuentes/cláusulas competentes | U: propuesta/selección presente | F: prueba/provenance delimitada |
| --- | --- | --- | --- |
| §1 annexo/scope/activation classes | ORQ README Evolución/Autoridad; WORKFLOW profile/documental; ESTADO próximo lifecycle; R2 §11/research §4 | Tres NEW process candidate, activation prospectiva, clases ACTIVE/PILOT/DEFER | predecessor STARTING-GATES; baseline/allowlist; no future gate proof |
| §2 executable/discovery/launch checks | ROLES modelos operacionales; GATES schema/inputs; WORKFLOW unsupported profile | Discovery real antes routing, requested/effective/observed, no silent fallback | Schema/help/runtime/ASAR/full excerpt y launch+TUI hashes §2 |
| §3 matrix/eligibility/every effort/mode | ORQ roles capacidades abstractas, no SKU norma | Routing classes, cero PROVEN_DEFAULT sin task audit/promotion; combinations conditional | Jyn catalogs/FF/PF/composeModelValue/$yn/VF/ebn exact excerpt; matrix expanded |
| §4 risk routing/pilots | D18–24/26/27; R2 §§3–10; ROLES independencia; GATES severidad/autoridad | Luna medium mechanical, Terra bounded PILOT_FIRST, Sol complex/high-risk ESCALATION_ONLY | Actual Sol ultra launch+observation only; Terra/Luna pilot inexistente |
| §5 tool/FASTGATE/RAW/fan-out | GATES scope/tests/host/implementation/documentation; D26/27; R2 §§4–10; ROLES | FASTGATE PILOT_FIRST, fan-out triggers, RAW summary split/reuse limitado | Exact candidate/env/receipt futuros, no historical test PASS para esta unidad |
| §6 budget/debug | WORKFLOW CORRECT/re-audit; GATES counters/severity/pre-semantic; STATE-MACHINE recovery | Max2P2/max1P1/max1TXconditional/zero judgment; debugging falsable; bootstrap no activation | Findings/corrective artifact y timestamps cuando existan; no inventar cycles |
| §7 every HumanGate/aliases | REGLAS §13; GATES stops/HostValidator/pre-semantic; STATE-MACHINE HUMAN_STOP/recovery; D19/20/28/30 | Catálogo fail-closed human-owned completo y process aliases sin domain enum | Receipts/cause/finding exigidos; operacional recuperable no semantic FAIL ficticio |
| §8 crosslane/ownership/Flyway | ESTADO cross-lane cierre R2; arquitectura§7–10; dominio reservas/capacidad; mapa legacy; D24/26/30; R2 §9 | Stop before shared writes, no auto reconciliation/ownership transfer | Payments facts COORDINATION_REPORTED únicamente, no integración/validación actual |
| §9 metrics/Terra registry/promotion | GATES evidencia/AgentResult; ROLES separar ejecución/audit | Métricas sólo mecánicas/null unknown, Terra piloto separado/independent approval | Launch+TUI/actual manifests/receipts; findings de audit posterior null |
| §10 publication/activation/exclusions/binding | WORKFLOW publication/closure; STATE-MACHINE dimensiones; ROLES publisher; R2 §11/research §4; ESTADO secuencia | Separate process activation, no automated transition; binding no circular/selfupdates | predecessor publicado sí; candidate audit/publication futuros no probados |
| §11 provenance | README documentación autoridad/historia; AGENTS | Mapping N/U/F íntegro a nivel sección; proposals no product authority | Exact hashes/paths físicos, sin canónico basado sólo en chat |

Todas las reglas operacionales y cada código de §7 están cubiertos por su fila.
Las propuestas no sustituyen ORQ normal. Semantic safety audit es posterior,
fresh e independiente; este materializador no declara aceptación ni calidad probada.
