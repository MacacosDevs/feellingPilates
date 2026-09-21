# F2E — runbook operacional candidato R1

Estado: `MATERIALIZED_CANDIDATE / PENDING_FRESH_INDEPENDENT_PROCESS_AUDIT`.
Profile: `F2E_OPTIMIZED_EXECUTION_BOOTSTRAP_R1 / PROCESS_ONLY / DOCUMENTATION_ONLY`.
Este anexo explica **cómo operar** una unidad autorizada. No define dominio, diseño,
allowlist de implementación ni siguiente fase funcional. Los documentos del protocolo
multiagente legacy ORQ-1 (`README.md`, `WORKFLOW.md`, `STATE-MACHINE.md`, `GATES.md`,
`ROLES.md`) corresponden a `LEGACY_PROTOCOL_PROVENANCE_ONLY` y no constituyen enlaces de
navegación ni dependencias activas; rige el rigor Orca Product Delivery actual como autoridad
de proceso activa. Routing y propuestas versionadas se encuentran en [F2E-EXECUTION-POLICY](F2E-EXECUTION-POLICY.md).


Vista operacional de activación R1: contrato `F2E-STATE-V2`, autorizado por el cierre
process-only en [ESTADO-ACTUAL](../ESTADO-ACTUAL.md#f2e--activación-del-proceso-optimizado-y-cierre-de-publicación-r1).
Los headers y §§1–11 del bootstrap conservan su corte histórico V1; la extensión
§10.1 gobierna sólo la reconstrucción del STATE nuevo. La política sigue subordinada
al producto. Este candidato no se autoactiva: publicación exacta auditada y gates
competentes condicionan su efecto; uso futuro sólo tras el gate terminal de activación.

## 1. Entrada física, selección y límites

Evidencia local durable `E`:
`/Users/jesusaldaircruzortiz/.codex/feelingpilates-evidence/run_748f43974201`.
Run `run_748f43974201`; materializador `task_b9b61bc7bb77 / ctx_64051785e565`,
terminal `term_580fb3a2-67ea-4f90-a95f-9d60123c12f5`; no auditor ni publisher.
Baseline: branch `operacion/excepciones-horario-fecha`, HEAD/upstream/live
`ab9705db6463ebef4576aff4f6559808cc0fdb9f`, upstream
`origin/operacion/excepciones-horario-fecha`, ahead/behind `0/0`, tracked CLEAN,
index sin delta, untracked0, 463 tracked y 660 ignored. `E/BASELINE.json` y
`E/MATERIALIZER-BEFORE.json` SHA-256
`049128ef66615785653afc565f1e596dd7ef245f8bbd6ebd642e413faf568ee3`.
Baseline dirty autorizado: ninguno; ignored preexistentes se preservan por hash.

Gate precedente real: `run_9e2fc3f4c74f / gate_11bf6b2c5681`, resolved/PASS,
`PASS — F2E R2 DESIGN AUTHORITY CLOSED / PUBLISHED / READY_FOR_NEXT_AUTHORIZED_LIFECYCLE`.
Receipt `E/STARTING-GATES.json`, SHA-256
`7b65647e8f7605bfb52e1288021d06a4d6d2dcf00a0ba5d49991a853bbec4345`.
Selecciona exactamente este bootstrap `USER_SELECTED_PROCESS_ONLY`, sin transferencia
de implementación. Es una elección de proceso; no un nuevo prerrequisito funcional.

Ownership presente: exactamente tres NEW paths:

- `auditoria/orquestacion/F2E-RUNBOOK.md`.
- `auditoria/orquestacion/F2E-STATE.json`.
- `auditoria/orquestacion/F2E-EXECUTION-POLICY.md`.

Allowlist congelada antes de editar: `E/ALLOWLIST.json`, SHA-256
`132ddc5fd2d81c62b0ec9df8f530d49693685481176b972776ab880211f4aad5`.
Escritura documental mediante apply_patch; reportes/snapshots externos bajo E.
Este despacho excluye stage/commit/push/fetch/publicación, build/tests, DB/JDBC/SQL,
producto, handoff authoring, configuración, migraciones, diseño de dominio,
reviews/ESTADO y otros archivos del repositorio. Un solo materializador; sin niños.

Invariantes de entrada y salida, verificables desde fuentes competentes:

| Eje | Estado preservado |
| --- | --- |
| R1 | CLOSED / ACCEPTED / PUBLISHED; exact21 main11/test10 inmutable en esta unidad |
| R2 diseño | COMPLETE / AUDITED / PUBLISHED / CLOSED |
| R2 implementation handoff | HISTORICAL ARTIFACT REPOSITORY-PUBLISHED (commit 6140978) / NOT_ACTIVE / ALLOWLIST_RECONCILIATION_REQUIRED; ACTIVE HANDOFF NINGUNO |
| Implementación R2 | NOT_AUTHORIZED / NOT_STARTED |
| TurnoInstructor | LEGACY_VIVO / PRODUCTIVO |
| Dark launch / cutover | PRESERVED / NOT_AUTHORIZED |
| R3–R6 | NOT_AUTHORIZED_IN_R2 |
| P2-EVIDENCE-01 | NON_BLOCKING / PRESERVED; no reparación de originales |

R1 path-set SHA-256 `f400a0602f95e318845da670bee4f819f057842adf8a60506564d5bd75e41d14`;
content-manifest SHA-256 `e2b64abd6aba8a050df6184f6c5440d87db83a67182fb43e622f48cf96f4f3ce`.
Recomputar desde los literales del guard físico y handoff consumido; no inferir
rutas por prefijos/conteos. E/MATERIALIZER-R1-IDENTITY.json registra igualdad
guard↔handoff y E/MATERIALIZER-R1-MANIFEST.tsv los hashes de cada miembro.

## 2. PREPARE, fuentes y contradicción

En cada unidad volver a inspeccionar físicamente branch, HEAD, upstream, refs,
index/staging, tracked/untracked/ignored, diff/check y remoto live cuando aplique.
No ejecutar comandos mutadores para obtener esa evidencia. Capturar manifiestos
before/after con algoritmo, timestamps y exit codes. El delta del agente se mide
contra su before; preservar y no atribuirle baseline dirty autorizado.

Leer en orden AGENTS: README-REESTRUCTURACION y ESTADO; handoff ACTIVE si existe y
canónicos pertinentes; checkpoint/review/intervención concretos; los cinco documentos
ORQ. `NINGUNO` no habilita consumir un handoff histórico como ACTIVE. La mínima
carga de contexto de §5 nunca sustituye esa inspección inicial obligatoria.

Precedencia operacional derivada, aplicada según la competencia del dato:

```text
autoridad normativa publicada de producto/diseño
→ implementation handoff activo y compatible con esa autoridad
→ reviews/audits competentes, con su scope y corte
→ Decision Gates reales resueltos y con binding verificable
→ lifecycle documentado en ESTADO
→ evidencia física Git
→ F2E-STATE (DERIVED / NON_PRODUCT_AUTHORITY / cache)
```

Git demuestra bytes y refs; no autoriza producto. Un gate no puede contradecir
diseño/dominio ni un review cambiar el estado operativo por sí solo. La precedencia
no permite ocultar contradicciones: clasificar el dato, abrir su fuente competente,
comprobar corte/hash/receipt y fallar cerrado si no existe reconciliación inequívoca.
Una decisión necesaria requiere Human Gate; el coordinador no la resuelve unilateralmente.

Interpretación de este corte: PENDING/NOT_PUBLISHED en headers R2 y cierre ESTADO/review
son preimages históricos sellados. Gate real precedente y Git corroboran cierre
posterior publicado; conservar historia y FAIL originales. No editar esos documentos
ni reabrir R1 por su antigüedad. Registrar fuentes/hashes en STATE y report externo.
En cuanto al handoff de implementación R2, se distinguen cuatro ejes: (1) el marcador interno
del documento pre-publicación como fue escrito históricamente (`MATERIALIZED_CANDIDATE / NOT_PUBLISHED`),
(2) el evento de publicación física en repositorio mediante el commit histórico `6140978bfd7b723fbbf9ddde1b5b5ba4f777c43c`,
(3) la autorización implementativa (`NOT_AUTHORIZED`), y (4) la activación operativa (`NOT_ACTIVE`).
Asimismo, las cinco dependencias históricas de orquestación (`README.md`, `WORKFLOW.md`, `STATE-MACHINE.md`,
`GATES.md`, `ROLES.md`) pertenecen al protocolo multiagente legacy (ORQ-1), cuyo equivalente de proceso
vigente es Orca Product Delivery (RUNBOOK y POLICY). Dichos documentos son `LEGACY_PROTOCOL_PROVENANCE_ONLY`,
accesibles vía objetos Git históricos, y no se trasplantan a la base limpia. El estado de la allowlist
del handoff es `HANDOFF_ALLOWLIST_RECONCILIATION_REQUIRED`; la integración de la fundación F2E es
completamente independiente de la activación de R2, la cual requerirá reconciliación explícita en su
propio ciclo futuro.

## 3. Lifecycle y gates por profile

Para una futura unidad de producto expresamente autorizada, usar esta secuencia de
bloques, seleccionando sólo fases/gates competentes del profile físico:

```text
autoridad → handoff aprobado/activo → implementación → validación determinista
→ audit NUEVO fresh independiente → Decision Gate → aceptación
→ publicación autorizada separadamente → cierre auditado
```

Authority/handoff authoring, approval/activation e implementación necesitan
autorizaciones propias. Acceptance no es cutover. Publication no es activación
productiva. El motor que no soporta un profile falla explícitamente conforme a
WORKFLOW; no lo degrada ni fabrica gates. No trasladar un recorrido ilustrativo a
requisito universal. Para cada transición exigir PASS sólo de gates aplicables que
deban estar resueltos antes de ella; futuros aplicables PENDING, ajenos NOT_APPLICABLE.

Profile presente, bajo protocolo normal, sin usar optimizaciones candidatas:

```text
PREPARE → DOCUMENT → coordinator scope verification
→ NEW fresh independent PROCESS/DOCUMENT AUDIT → bootstrap Decision Gate
→ READY_FOR_CONTROLLED_PROCESS_PUBLICATION (terminal de este profile)
```

El autor entrega `F2E_OPTIMIZED_EXECUTION_BOOTSTRAP_MATERIALIZED`; no entrega PASS
independiente ni declara alcanzado el terminal competente. Scope verification,
audit y bootstrap gate de los nuevos bytes siguen PENDING; IDs futuros null.
La siguiente acción tras materializar es verificación coordinadora y audit nuevo.
Correcciones actuales siguen ORQ normal y el scope correctivo competente; los
presupuestos propuestos de la policy no se aplican en este Run.

| Bloque presente | Aplicabilidad / estado al corte del autor |
| --- | --- |
| Autoridad/scope/safety/identidad | APPLICABLE; medición del autor, resolución competente pendiente |
| Audit independiente de proceso | APPLICABLE / PENDING / NOT_EXECUTED |
| Bootstrap Decision Gate | APPLICABLE / PENDING / no ID futuro inventado |
| Implementation/tests/host/DB | NOT_APPLICABLE / NOT_EXECUTED; nunca technical PASS |
| Publicación en este profile | NOT_AUTHORIZED / NOT_EXECUTED; lifecycle separado |
| Activación de proceso | NOT_AUTHORIZED / NOT_EXECUTED; autoridad competente separada |

La inaplicabilidad técnica se funda conjuntamente en scope documental puro,
bytes de implementación aceptados sin cambio, autoridad aplicable sin obligación
de rerun y evidencia física sin drift. Si cualquiera falla, detener/reclasificar
por fuente competente; no omitir evidencia técnica cuando sí corresponda.
Tras bootstrap PASS, publicación controlada de proceso necesita profile/gates/rol
publisher propios; activación de proceso necesita decisión competente propia.
Nada de ello materializa/activa handoff R2 ni autoriza implementación por continuidad.

## 4. Coordinación, dispatch e independencia

El coordinador prepara Task/Dispatch y ownership exactos, valida schema antes de
lanzar, abre autoridades, congela allowlist y baseline, y registra gates reales.
Sólo un modificador por working tree. AUDITOR y DOCUMENT_AUDITOR fresh/read-only
son distintos del executor/documenter/corrector y no heredan la conclusión del autor.
Un re-audit necesita contexto fresh y verifica evidencia actual, no sólo fixes.
Host validation es determinista por plan estático allowlisted, sin LLM ni comandos
arbitrarios tomados de resultados. Publisher sólo publica bytes/gates autorizados.

Descubrir ejecutable/version/runtime/schema/help/capabilities reales **antes** de
routing; verificar catálogo instalado según policy §2–4. Comparar requested/effective
del launch y modelo/effort observados en transcript; argumentos no bastan. Si falta
target, evidencia o capacidad remota, registrar stop/escalación; cero sustitución
silenciosa. No lanzar pilot para resolver una aceptación crítica sin autoridad.

Worker supervisado: usar CLI Orca, conservar Task y Dispatch en heartbeat/Done,
heartbeat cada5 minutos salvo ask/check --wait bloqueante, revisar inbox al iniciar
archivo, tras validación y antes de Done. Preguntas al coordinador mediante ask;
reanudar por messageId en timeout, sin duplicarlas. Exactamente un worker_done con
outcome succeeded/failed real y resumen ejecutivo de tres frases; luego idle.
No ACK anticipado, nueva tarea implícita ni publicación desde el worker.

El coordinador verifica settlement terminal, outcome, task/dispatch, scope y receipt,
captura transcript/artefactos y release/archive según contrato Orca. Conserva
receipts verificables; un worker_done sólo acredita entrega, no aprobación. ACK de
Done después del settlement/release comprobados, conforme a operación observada
en reviews R2. Si el archive/retención falla, registrar disponibilidad incompleta y
resolver por protocolo; no afirmar release ni evidencia capturada sin receipt.

Fan-out **prospectivo**: un worker competente → validación determinista → un auditor
NUEVO fresh independiente → gate. Worker adicional sólo por trigger registrado:
P0/P1, TX/concurrencia, evidencia conflictiva, arquitectura/autoridad, PG/JPA,
cross-lane o schema conflict. Registrar trigger, subscope, ownership, rol e independencia;
no añadir agentes para producir confianza verbal. Aquí no hay nested workers.

## 5. Contexto progresivo y mínimo de toda Task

Propuesta PILOT_FIRST, sin reemplazar lectura obligatoria ni esconder autoridad:

| Nivel | Abrir y transmitir |
| --- | --- |
| L0 | STATE validado/fresco; referencias verificables y mínimo obligatorio completo |
| L1 | STATE + cláusulas exactas de autoridad relevantes, path/hash/corte |
| L2 | Target, diff exacto y dependencias referenciadas con identidad |
| L3 | RAW seleccionada necesaria de tests/DB/logs; receipts/exit codes |
| L4 | Historia amplia y autoridad completa al escalar ambigüedad/conflicto |

Cada Task, desde L0, incluye branch/HEAD, lifecycle/profile, autoridad ACTIVA path+hash,
handoff activo path+hash o NINGUNO explícito, candidate+allowlist identity, gate actual
real/estado, fronteras productivas, P0/P1 sin resolver y guards cross-lane. El hash
exacto del candidato se obtiene del manifest externo actual; no usar null de cache
como binding de dispatch/audit. Un auditor elige y abre independientemente todas las
fuentes y RAW necesarias; resumen/contexto reducido no limita su acceso ni scope.
Si falta una cláusula, abrir L1–L4 antes de razonar/decidir. Registrar filesRead reales.

## 6. Herramientas y validación FAST/GATE

Propuesta tool-first/delta-first: inspección Git/filesystem → Maven/JUnit pertinente
→ PostgreSQL/Testcontainers/Flyway requerido → SQL/checksum/hash/manifest → summary
estructurado → razonamiento LLM. Es orden de dependencias/aplicación, no obligación
de ejecutar comandos inútiles o fuera de autorización. Herramientas establecen facts
antes de inferir; ni nombre del modelo ni logs narrados sustituyen receipts.
Este bootstrap sólo permite inspección/hash/JSON/documentos/diff; sin ejecución técnica.

FAST y GATE son propuesta PILOT_FIRST para futuras unidades autorizadas:

| Modo | Uso y evidencia |
| --- | --- |
| FAST | Desarrollo/corrección: compile/unit/architecture/TX/PG/SQL capture pertinentes, diffcheck y candidate allowlist. Sólo depuración/progreso; nunca aceptación. |
| GATE | Candidato/allowlist exactos y frescos; validación completa del milestone según autoridad/profile, targeted y full regression pertinentes, audit independiente fresh y Decision Gate. |

En GATE implementativo F2E reabrir frescamente, según scope competente: PostgreSQL
Testcontainers, compatibilidad con Flyway integrado vigente, owner JPA/TX real,
isolation/readOnly, native physical JDBC identity, snapshot RR multi-statement,
catálogo SQL cerrado/capture/binds, SELECT-only/denied-write, checksums persistidos,
no-write, concurrencia, architecture/dependencies, default/productive absence,
zero productive callers, guards cross-lane/schema y diffcheck. Host real cuando la
autoridad lo exige. D26/27 y diseño R2 §§4–10 fijan semántica; este anexo no la redefine
ni inventa filenames/comandos/conteos futuros. R1 RC y R2 RR permanecen separados.
V47/50 históricos no son head terminal universal; identificar head integrado real y
validar migraciones F2E requeridas/compatibilidad sin reconciliarlas automáticamente.

Safety invariants pueden depurarse en FAST y reaparecen en GATE competente fresco.
PASS histórico no es aceptación fresca. Reutilizar evidencia sólo si autoridad/profile
lo permiten, mismos bytes bound/entorno/scope/autoridad y freshness comprobada; nunca
sustituir evidencia mandatada fresh. Registrar reusedFrom/binding y motivo; un rerun
innecesario no aporta autoridad, una ausencia requerida provoca fail closed.

## 7. RAW, SUMMARY, retención y contrato worker

RAW y SUMMARY separados. SUMMARY lleva referencias exactas, hashes, exit codes,
receipts y limitaciones; workers devuelven summary primero. RAW incluye lo aplicable:
comandos/stdout/stderr, Maven XML, logs PG/Testcontainers/Flyway, SQL+bounds/binds,
owner TX/isolation/native JDBC identity, snapshot/checksum/concurrencia, host real,
Git/filesystem manifests y receipts Orca. SQL/logs restringidos se custodian en RAW
con acceso adecuado, nunca inventar campos faltantes ni reparar originales sellados.

No descartar RAW obligatoria. /tmp sin retención no es evidencia durable. E es durable
local retenida; no hay backup externo garantizado. Registrar por artefacto retentionOwner,
location, SHA-256, bytes, reconstructionCommand/source, disponibilidad y política de
retención; conservar archive receipts Orca. Si falta RAW necesaria y no es reconstruible
con autoridad/entorno/bytes equivalentes, EVIDENCE_INSUFFICIENT. Auditores no confían
en SUMMARY y abren selectivamente **toda** RAW necesaria. P2-EVIDENCE-01 histórico
se conserva como NON_BLOCKING; no permite perder RAW nueva requerida.

Contrato compacto prospectivo, EXTENSIÓN del AgentResult de GATES, nunca reducción:

```text
status, role, taskId, dispatchId, model, effort, scope, authorityRefs,
baseline, filesRead, filesChanged, validation, databaseEvidence,
snapshotIdentity, findings, severity, scopeExpansionRequired,
humanGateRequired, rawEvidenceRefs, summaryEvidence, nextTransition,
gate, P0, P1, P2, recommendation, requires_human_decision,
p1_correctable, corrective_artifact
```

Refs contienen path/hash/corte; changed paths con before/after; validation por bloque
APPLICABLE+PENDING/PASS/FAIL/BLOCKED o NOT_APPLICABLE, ejecución y exit codes. DB ausente
se declara NOT_APPLICABLE/NOT_EXECUTED, no PASS. Findings conserva IDs/severidad/evidencia
y autorización correctiva; P0/P1/P2 no inferidos por schema ni autor inventando audit.
`p1_correctable=false` y `corrective_artifact=null` cuando no corresponden; ningún
null significa control pasado. Liveness/delivery/launch no son gates de contenido.
Narrativa adicional sólo ante HUMAN_GATE_REQUIRED, MILESTONE_COMPLETE,
PUBLICATION_REQUIRED, petición humana o P0/P1 complejo; no elimina evidencia obligatoria.

## 8. Corrección, debugging y escalación

Antes de corregir: audit competente, finding exacto y artefacto correctivo autorizado,
scope cerrado y ausencia de juicio humano. Corregir sólo ese delta; preservar gates
ya resueltos; revalidar evidencia pertinente y re-audit fresh. Contadores separados
por clase y stage técnico/documental/cierre; no reutilizar corrector como auditor.
Presupuestos candidatos y Human Gates completos: policy §§6–7. En este bootstrap
se aplican reglas actuales de ORQ, sin activar los máximos nuevos.

Debug prospectivo: reproducir → root cause → invariante autorizada o ejemplo físico
known-working compatible → una hipótesis falsable → cambio mínimo autorizado →
validación targeted → re-audit fresh. No loops aleatorios, varias soluciones válidas
decididas por worker, ni drift del scope por conveniencia. Presupuesto agotado requiere
CORRECTION_BUDGET_EXHAUSTED, no otro agente para resetear contador.

Los Human Gates de policy §7 fallan cerrado: detectar no otorga resolución unilateral
al coordinador. Registrar gateCode, finding, evidencias/hashes, scope y decisión requerida;
escalar y detener trabajo dependiente hasta resolución explícita del dueño humano.
P0/contradicción/scope/producto/TX/schema/cross-lane no se convierten en P2 o corrección
mecánica. Fallo pre-semántico recuperable se clasifica primero por GATES/STATE-MACHINE:
sin AgentResult válido consumido no inventar semantic FAIL, P0/P1 ni ciclo correctivo.
Recovery determinista seguro autorizado registra causa/exit/receipt, conserva identidad,
historia y scopes; si requiere decisión o mutación no autorizada, stop competente.

## 9. Cross-lane y publicación separada

Ownership y ruta de escalación se consultan en policy §8 y canónicos referenciados.
Antes de cambios compartidos Reserva/Programación/Turno/capacity activar
CROSS_LANE_DEPENDENCY_REQUIRED; no escribir primero y coordinar después. Payments
reportado cerrado es COORDINATION_REPORTED, no validación/integración aquí. La futura
validación integrada identifica Flyway real y revisa compatibilidad F2E; no hace merge,
schema reconciliation ni cambios de ownership automáticos.

Publication necesita lifecycle, autorización, gates y publisher propios, exact manifest
auditado, staged audit cuando el profile lo exija, parent/commit/remote exactos, push
normal permitido y post-publication audit/closure fresh independientes. Instrucciones
de publicación concretas pertenecen a ese lifecycle; este runbook no las ejecuta.
`auto_publish=false`; terminal bootstrap máximo READY_FOR_CONTROLLED_PROCESS_PUBLICATION.
PUBLICATION_REQUIRED, PRODUCTIVE_ACTIVATION_REQUIRED y CUTOVER_REQUIRED son Human Gates
separados. Proceso publicado tampoco está activo por inferencia; sólo autoridad
competente de activación fija qué propuestas pasan a operar. No auto publicación,
cutover, activación ni implementation handoff por este bootstrap.

## 10. Schema STATE, identidad y reconstrucción

[F2E-STATE.json](F2E-STATE.json) schemaVersion `F2E-STATE-V1`, clase
`DERIVED / NON_PRODUCT_AUTHORITY / OPERATIONAL_CACHE`. Corte pre-audit de materialización;
no recibo terminal. Todos los siguientes campos son required; sin cuarto archivo de schema.

| Campos / tipo | Fuente y validación |
| --- | --- |
| schemaVersion, classification, lane / string | Versión exacta anterior y lane F2E; rechazar schema desconocido |
| repository, branch, head / string | Raíz física, branch y HEAD full40; igualdad preflight |
| upstream, liveRemote / object | ref/head, ahead/behind y receipt live hash; no tomar local tracking como remote live |
| lifecycle, activeMilestone / object/string | Selección competente/bootstrap pre-audit; ejes separados y boundaries preservados |
| authorityRefs / object; authorityHashes / object | Key→path y key→SHA-256 full64 de canónicos/protocolo/reviews; abrir fuente según competencia |
| handoffRefs, handoffHashes / object | ACTIVE null explícito, research provenance-only y consumed R1; hashes exactos, no activar historia |
| candidateIdentity / object | Payload manifest RUNBOOK+POLICY, hash y regla de exclusión STATE; external full3 receipt para binding final |
| allowlistIdentity / object | frozenRef/SHA64/paths exactamente3; zero out-of-scope |
| repositoryShape / object | beforeRef/SHA, tracked463/ignored660/index delta0/untracked0 inicial y R1 seals; comparar before/after |
| validationState / object | Profile y gates aplicables/PENDING versus técnicos NOT_APPLICABLE; no self PASS |
| databaseEvidenceRefs, rawEvidenceRefs / array | Refs tipadas path/SHA/availability; DB [] con NOT_APPLICABLE explícito |
| summaryEvidence / object | SummaryRef y binding externo; no hash recursivo del report que contiene STATE |
| openFindings / array | ID/severity/status/authority/source; histórico P2 preservado, auditoría nueva pending |
| crossLaneGuards / object | Coordination ref/status, ownership refs, stop-before-shared-write; ninguna integración |
| modelRoutingState / object | Policy/capability refs, receipt requested/effective/observed; runtime/task quality separados |
| currentGate, humanGate / object | Predecessor real resuelto; gate nuevo pending ID null; decisiones humanas no autoconcedidas |
| nextAuthorizedTransition, lastRun / object | Única acción materializada siguiente; Task/Dispatch/rol/capture pre-audit |

SHA64 lowercase; HEAD40 lowercase; timestamp ISO8601 UTC; refs no vacías cuando
aplicables. Null sólo unknown/not-created explícito, [] sólo ausencia declarada.
Validar keysets/tipos/valores, relaciones scope/profile/gates, hashes y existencia de
refs; schema parse por sí solo no valida esas relaciones. STATE no guarda prose de
dominio ni cambia autoridad. Un nuevo HEAD, fuente/hash, candidate/allowlist, staging,
runtime/catalog o evidencia indisponible provoca STATE_DRIFT/BASELINE_DRIFT según dato.

Reconstrucción reproducible: abrir fuentes de §2 en orden; verificar gates reales
desde receipts/Orca disponibles y preflight Git actual; rehash canónicos/handoffs,
tracked/ignored/index/refs y exact21; identificar profile/lifecycle autorizado;
reconstruir refs y estados derivados; comparar STATE. Diferencia material sin
reconciliación competente falla cerrado. Refresh requiere scope documental propio;
cache stale no autoriza editar canónicos ni implementar. Antes de cada dispatch,
validación, audit o gate, repetir freshness y enlazar snapshot exacto.

Evitar circular self-hashing: payload `E/CANDIDATE-PAYLOAD.tsv` contiene sólo RUNBOOK
y POLICY, líneas `SHA256  relativePath\n`, paths sorted UTF-8/LF/finalLF, sin header.
STATE contiene su SHA, se excluye del payload junto con receipts derivados. Tras
escribir STATE, `E/CANDIDATE-MANIFEST.tsv` registra **los tres archivos completos**
con igual framing y hash externo, sin incrustar éste en STATE. El report/dispatch
transporta ese full3 hash y hashes por archivo. El coordinador/gate externo bindará
exactamente bytes auditados; no editar STATE post-audit para insertar veredictos.
Todo cambio posterior crea candidato nuevo y exige nuevo audit competente.

## 10.1. STATE V2 — checkpoint operacional derivado sin autorreferencia

Scope de esta extensión: activación/cierre de proceso R1, no nuevo producto ni
reducción de freshness. V1 se preserva en el commit publicado de bootstrap y la tabla
anterior describe ese snapshot histórico. El STATE actual usa `F2E-STATE-V2` y conserva
exactamente los campos required de §10, con las diferencias de tipos siguientes:

- `head`, `upstream.head` y `liveRemote.head` son objetos selectores de fuente física;
  cada valor **resuelto** debe ser SHA40 lowercase. `anchorHead` conserva el checkpoint
  observado, nunca se presenta como HEAD terminal. El resolved STATE externo contiene
  los valores concretos de HEAD/upstream/live medidos, timestamp y receipts.
- `currentGate` y las fases de `validationState` conservan precedentes reales y
  selectores de gates nuevos por Run + semantic exacto + candidate binding; IDs no
  creados son null/PENDING. Nunca poner PASS por intención ni inventar un ID futuro.
- `lifecycle` contiene ejes bootstrap/publicación/activación/cierre y métricas futuras
  con frontera temporal. No contar bootstrap/cierre como pilotos Luna/Terra ni
  fabricar calidad, costo, tokens o quota. Los otros objetos mantienen tipos de §10.

Resolución obligatoria antes de cada task/audit/gate, bajo autoridad competente:
1. Abrir fuentes superiores en orden §2 y rehash authority/handoff/payload/allowlist;
   verificar clasificación DERIVED, versión conocida, keysets/tipos y RAW disponible.
2. Ejecutar Git status/refs/index/HEAD y ls-remote no-mutante. Resolver HEAD, upstream
   y live desde herramientas; comparar sus SHA40, branch y snapshot autorizado.
3. Antes de publicar este cierre, HEAD debe ser su anchor y sólo puede existir el
   delta documental exacto autorizado. Activación efectiva sigue NOT_ACTIVE; gates
   futuros aplicables son PENDING. Esta fase no habilita uso de política optimizada.
4. Después de publicar, probar commit con parent exacto anchor, diff exacto allowlist,
   blobs iguales a candidate auditado/gate de publicación, repo CLEAN/index EMPTY y
   HEAD=upstream=live,0/0. Sólo esa prueba hace la activación declarada operacional
   ACTIVE; uso en nuevos lifecycles permanece bloqueado mientras falta cierre final.
5. Abrir Orca/receipts reales del Run de activación: gate de autorización posterior a
   audit fresh PASS, publicación controlada y auditor post-publicación NUEVO PASS.
   Gate terminal debe ser único, resolved/PASS, semantic exacto, binding del commit,
   manifest y hashes actuales; nunca resolver por un título o un cache. Sólo entonces
   cierre CLOSED y nextAuthorizedTransition habilitado como readiness documental.
6. Materializar fuera del repo el resolved STATE concreto con HEAD/upstream/live,
   hashes vigentes, gate ID/resolution/timestamp, estado efectivo, métricas y next
   lifecycle. Registrar hash en receipt externo y snapshots. Ningún selector es
   permiso para omitir una medición, audit, gate ni para seguir una ref sin binding.

Este diseño evita almacenar dentro de un commit su propio SHA o gates todavía no
creados. Preservar archivos auditados: no editar STATE post-audit/post-push para
insertar resultados. Los receipts finales bindan los bytes completos, incluido STATE;
payload excluye STATE/receipts y contiene sólo los otros documentos del allowlist.
Falta de fuente/binding, contradicción, candidato cambiado, remote movido, selector
no soportado o gate ambiguo → fail closed STATE_DRIFT / EVIDENCE_INSUFFICIENT /
AUTHORITY_RECONCILIATION_REQUIRED según dato. Nunca reutilizar un resolved STATE sin
freshness. HEAD posterior en otro lifecycle requiere reconstrucción/binding fresco
bajo su autorización; no aceptar por mera descendencia del anchor. Las fuentes
superiores y los gates F2E/JPA/PG/snapshot/no-write/Human Gates permanecen intactos.

## 11. Medición, terminales y entrega

Métricas ligeras según policy §9, sólo observables mecánicos; unknown null. Sin
fabricar tokens/costo/cuota ni declarar calidad por SKU. Pilots Terra separados y
sin promoción propia; ninguno lanzado en este bootstrap.

Antes de Done: snapshot after, comparar cada tracked/ignored/index/ref contra before
y BASELINE, recomputar R1, exact tres NEW, diff/check y cached check, manifests/hashes
reales y provenance. Report durable `E/MATERIALIZER-REPORT.md` con Task/Dispatch,
modelo observado, filesRead/filesChanged, baseline/delta, capabilities/matriz,
evidencias/exit codes/hash, limitaciones y terminal del autor. Revisar inbox.

Terminales del materializador: F2E_OPTIMIZED_EXECUTION_BOOTSTRAP_MATERIALIZED si entrega
completa dentro de scope; F2E_OPTIMIZED_EXECUTION_BOOTSTRAP_INCOMPLETE si falta trabajo;
BLOCKED_BY_PROCESS_AUTHORITY_CONTRADICTION si autoridad incompatible impide continuar.
Son entrega/stop, nunca aceptación propia. Sólo un auditor independiente y gate real
pueden producir bootstrap PASS y readiness competente; publicación/activación pendientes
son lifecycle separados. Estado actual nuevo: PENDING audit/gate, no futuros IDs.

## 12. Provenance de todas las reglas de proceso

IDs N: reglas normativas preexistentes; U: propuestas/selección de proceso del usuario
registradas en este candidato, no activas; F: evidencia física SUPPORTING, sin poder
normativo. Identidades exactas en STATE.authorityRefs/authorityHashes, handoffRefs/
handoffHashes y E/MATERIALIZER-SOURCE-HASHES.json. U completo:
E/MATERIALIZER-SPEC.txt SHA `49c8e847d30aea2cab2173c0b1f4912a6132a45da070177a6c1f9fef8744a8e4`;
su persistencia conserva provenance del despacho, no reemplaza autoridad publicada.

| Sección/regla | N: fuente competente/cláusula | U: nueva selección/propuesta delimitada | F: corroboración exacta |
| --- | --- | --- | --- |
| §1 ownership/baseline/límites | AGENTS; REGLAS §§3–5/13; ESTADO cierre R2 y próximo lifecycle | Bootstrap PROCESS_ONLY, tres NEW, sin publicación/niños | BASELINE/ALLOWLIST/STARTING-GATES; before/R1 manifests |
| §2 lectura/precedencia/contradicción | README orden/autoridad; AGENTS; ORQ README Autoridad; ROLES transversales; ESTADO/review cortes históricos | Ladder derivado explícito y cache subordinada; fail closed | Source hashes, predecessor gate y Git/remote receipts |
| §3 lifecycle/profile/gates | WORKFLOW profile/documental/publicación/cierre; STATE-MACHINE dimensiones; GATES aplicabilidad/transición; diseño R2 §11/research §4 | Bootstrap prepare/document/scope/fresh process audit/gate/terminal limitado; activación separada | STARTING-GATES; IMPLEMENTATION bytes baseline unchanged |
| §4 roles/schema/independencia/host | ROLES tabla/transversales; GATES schema/HostValidator; WORKFLOW audit/correct | Adaptive fan-out con triggers; routing sin silent fallback | Installed CLI schema/help/runtime/catalog; launch+transcript; review R2 §1 receipts |
| §4 liveness/Done/settlement | ROLES separación; review R2 §1 como precedente SUPPORTING, no nueva norma genérica | Despacho supervisado heartbeat5min/inbox/Doneexact1/settlement/release/transcript/ACK | MATERIALIZER-SPEC y guías CLI versionadas; receipts Orca |
| §5 L0–L4/mínimo Task | AGENTS lectura; ROLES inputs; GATES scope/evidencia | Progressive disclosure PILOT_FIRST, mínimo completo y audit sin restricciones | Refs/hashes actuales de STATE y candidate full3 externo |
| §6 tool-first/FAST/GATE/reuse | GATES scope/tests/implementation/host; D18–24/26/27; R2 §§4–10 | Tool-first/delta-first; FASTGATE PILOT_FIRST, reuse condicionado sin saltar fresh | Git/FS actuales; RAW técnico sólo en futura unidad autorizada |
| §7 RAW/SUMMARY/retención | GATES evidencia; README tests/autoridad; review R1 §5 P2 histórico | Compact return EXTENDS, RAW separado, retención durable, narrative triggers | E manifests/receipts/XML/logs cuando aplicables; disponibilidad explícita |
| §8 correct/debug/HumanGate/recovery | WORKFLOW correction; GATES severidad/stops/pre-semántico/counters; STATE-MACHINE recovery | Debug falsable; budgets prospectivos policy; Human Gate catálogo fail closed | Findings/audit/corrective artifact futuros reales; sin IDs inventados |
| §9 cross-lane/publicación | ESTADO cross-lane y secuencia; D30; legacy mapa; WORKFLOW publicación/cierre; ROLES publisher | Ownership coordination/stop triggers policy; process publication/activation separadas | Payments COORDINATION_REPORTED; gate/Git publication sólo predecessor |
| §10 schema/binding/reconstruct/freshness | ORQ README cache no canon; GATES snapshots; ROLES before/after; STATE-MACHINE real state | STATEV1 y payload no circular PILOT_FIRST; refresh/re-audit ante cambio | External manifests actual files, source/R1/index/refs inventories |
| §11 métricas/terminal/entrega | GATES AgentResult; ROLES documenter no autoaudit | Métricas mecánicas; terminales de entrega, pilotos sin autopromoción | REPORT/validation/snapshots reales; fresh auditor/gate aún pendientes |
| §12 provenance | README repositorio autoridad y historia; AGENTS | Separación N/U/F por sección; toda propuesta queda candidata | SHA físicos de fuentes/E, sin elevar chat a canon |

Toda regla operacional de este anexo queda cubierta por su sección de la tabla;
una referencia SUPPORTING no se trata como aprobación normativa. Contradicción con
canónicos/ORQ: detener y escalar, sin modificar protocolo genérico ni producto.
