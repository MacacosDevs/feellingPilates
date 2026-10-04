# F2E R1 — Provenance, validación y aceptación materializada

Status: AUTHORITY_PROVENANCE_COMPLETE / ACCEPTED / NOT_PUBLISHED
Fecha de contraste físico: 2026-09-16 UTC
Rol: DOCUMENTER / FRESH_F2E_R1_FINAL_AUTHORITY_DOCUMENTER
Workflow: DOCUMENTATION_ONLY / NO_TEST_EXECUTION / NO_RUNTIME_OR_GIT_MUTATIONS
Run documental actual: `run_6b83c0a8f3a4`
Task / Dispatch actual: `task_41443902b374 / ctx_77c671acdfb6`
Corrección provenance anterior preservada: `run_b0efaa8ebe42 / task_f12663b70140 / ctx_9eed7ce97d17`
Materialización documental anterior preservada: `run_3c3b68d0f06b / task_3fdcc495e28d / ctx_44008bd58f29`
Clase: consolidación de evidencia por documenter y resolución terminal posterior competente por
coordinator; audit task_5f12bc24768c PASS / gate_b263068ee65c PASS. La entrega del documenter
sigue histórica/pending; no audit independiente ni autoaceptación de ese rol.

Los PENDING conservados describen exclusivamente el corte histórico de entrega de este documenter,
antes del audit/gate reales posteriores PASS. Sección11 registra la aceptación competente actual,
distinguiendo sus nuevos bytes de los sellos documentales inspeccionados por el auditor.

## 1. Resultado y binding exacto

R1 está MATERIALIZED, con validación técnica fresh PASS y GAP1–5 CLOSED. Las siete definiciones
originales TECH01/02/03/04/05/06/08 están RECOVERED_AUTHORITATIVELY; TECH02/03/04/05/06
quedan CLOSED por mapping físico y evidencia fresh competente. TECH01 queda CLOSED por la prueba
nativa first/later verificada en `run_8b529b21e8ad / gate_4ab844c58bbb`; TECH08 queda CLOSED
exclusivamente por ese requisito metadata solapado (sección10). TECH07 conserva
CLOSED FOR_CURRENT_R1_HOST_INVARIANT ONLY. R1 ahora ACCEPTED / NOT_PUBLISHED tras audit
independiente task_5f12bc24768c PASS y gate_b263068ee65c PASS reales (sección11).
El cierre residual permitió actualizar autoridad, no aceptó R1 por sí solo. El documenter entregó
aceptación pendiente. Publication READY_FOR_PUBLICATION_PREFLIGHT; ejecución no autorizada aquí.

| Objeto | Valor físico congelado |
| --- | --- |
| Checkout | `/Users/jesusaldaircruzortiz/Desktop/Feelingpilates/feelingpilates` |
| Branch | `operacion/excepciones-horario-fecha` |
| HEAD | `a0ec85818b771d4ac924b427fa1e90244ea9fe8e` |
| Upstream local | `origin/operacion/excepciones-horario-fecha` |
| Antes del delta documental original | tracked CLEAN / staging EMPTY / exact21 candidate untracked / sin extras |
| Candidate | 11 main + 10 test/helper; baseline preexistente del usuario, IMMUTABLE para este rol |
| Sorted path LF SHA-256 | `f400a0602f95e318845da670bee4f819f057842adf8a60506564d5bd75e41d14` |
| SHA-256 de manifest hash + two spaces + path + LF | `e2b64abd6aba8a050df6184f6c5440d87db83a67182fb43e622f48cf96f4f3ce` |
| Diseño CLOSED / PUBLISHED | `auditoria/fase-2e-diseno-adapters-read-only-snapshot-consistency.md` |
| Diseño SHA-256 | `6c72cba1f83fbc2bcf3b3219d8252d2410ec482e30ae85d30fd2eeb04e8883d8` |
| Sole implementation authority APPROVED / PUBLISHED / ACTIVE | `auditoria/handoffs/HANDOFF-F2E-R1-RESERVA-READER-JPA-READ-ONLY.md` |
| Handoff SHA-256 | `3fd71faca4d4c049ad5cb37b52bc6fd512509cf5b696bdc5c28d28cb966af8ef` |
| ESTADO pre-documentación SHA-256 | `bf74b1dab6465bbc593c6b141e3ce4f7207f5980e57589c6c961c8fb0e884cc4` |
| ESTADO entrada de corrección provenance anterior, dirty autorizado | `bca2d82576a9da804a69f9a2c44e3e07db817a96e8f8a63629d1f6228ca6babb` |
| Review entrada de corrección provenance anterior, untracked autorizado | `189976c40f6a34171abe46e2f8391dca1e248bcf0d690a185cc3ec9bf22d2439` |
| ESTADO entrada actual / snapshot externo .before | `267ae9f6b6f76a7bd86d9e520f9c65ee7ad04bb9a9f4615e7b6a68b3c07bcae1` |
| Review entrada actual / snapshot externo .before | `4801262ca2dfc63af6d597f849ec66db512ab730c2b753ce1dbccd9e49696a44` |

Binding de esta entrega pendiente: branch/HEAD/candidate path+content/diseño/handoff de la tabla,
main technical Run `run_6d0dfb237a61 / gate_01a63c36e518`, provenance Run histórico
`run_b0efaa8ebe42 / gate_ad9e29b8d8e8 FAIL`, native Run `run_8b529b21e8ad /
gate_4ab844c58bbb PASS`. Entrada física: staging EMPTY, sole tracked ESTADO dirty y exact21
candidate + review existente =22 untracked, sin extras. Snapshots preservados:
`/tmp/feelingpilates-f2e-final-authority.DVYXaM/ESTADO-ACTUAL.md.before` y
`F2E-R1-PROVENANCE-VALIDACION-ACEPTACION.md.before`. Único delta: estos dos documentos;
no recovery repetido, ejecución técnica, modificación de originales o implementación.

Las secciones2–6 siguientes conservan el corte documental histórico `run_b0efaa8ebe42`:
sus referencias a «actual», OPEN, PENDING y evidencia insuficiente pertenecen a aquel corte.
TECH02–06, originales S0/S1/S2 y sus sellos se conservan sin cambios. La vista vigente es
secciones1/7/11; sección10 conserva el corte documental auditado; sección9 conserva su
resolución histórica FAIL, sin reescribirla como PASS.

### Trazabilidad histórica — lectura y snapshots del documenter anterior

Los dos párrafos siguientes pertenecen al documenter provenance de `run_b0efaa8ebe42`,
no al preflight de esta entrega; la entrada vigente está sellada en la tabla y sección10.

Los canónicos se leyeron en orden AGENTS: README/ESTADO, handoff activo, diseño final36/37,
MAPA-LEGACY-Y-MIGRACION, ARQUITECTURA-ACTUAL y DECISIONES-ARQUITECTONICAS, reviews residual
y del handoff, y orquestacion README/WORKFLOW/STATE-MACHINE/GATES/ROLES. Se cargaron las guías
instaladas version-matched de Orca CLI/orchestration. Los marcadores internos históricos de
NOT_STARTED/NOT_AUTHORIZED en diseño/handoff/reviews permanecen preservados; el lifecycle posterior
compete a ESTADO-ACTUAL y no modifica sus bytes.

Snapshots físicos de entrada ORIGINALES de prior Run `run_3c3b68d0f06b`:
`/tmp/feelingpilates-f2e-authority.3QM0i0/pre-integrity.json` y `start-ESTADO-ACTUAL.md`.
Baseline de la corrección ACTUAL `run_b0efaa8ebe42`, bajo
`/tmp/feelingpilates-f2e-tech-recovery.uj4KrG/`: `starting-integrity.json`,
`starting-ESTADO-ACTUAL.md` y `starting-F2E-R1-PROVENANCE-VALIDACION-ACEPTACION.md`;
los dos documentos coinciden con los hashes dirty autorizados de entrada arriba.
Se verificó el candidato contra el manifest original directamente
con SHA-256 de cada archivo, además del helper read-only `integrity.mjs` sin modificarlo.
El delta de este documenter se limita a ESTADO y este review. Los 21 archivos no le son atribuibles.
No se verificó remoto live ni se ejecutó fetch.

## 2. Cadena histórica real de evidencia y límites de las decisiones

La lectura física de `orca orchestration task-list --run run_7fdc6b2552a6 --json` confirma:

| Actor histórico | Task / Dispatch / mensaje | Resultado real |
| --- | --- | --- |
| Worker A authority recon | `task_175cee0e2af6 / ctx_d9655d0bed9a / msg_e6b4fbc2dcbd` | FAIL por canónico stale NOT_STARTED frente a candidate21 |
| Worker B technical recon | `task_d84d23c864b5 / ctx_f87987fc2e53 / msg_0b1c969bef8d` | FAIL; ocho TECH PROVENANCE_UNAVAILABLE / DISPOSITION_UNVERIFIABLE y cinco gaps técnicos |
| Síntesis coordinator resume-only | `task_d5e284c49b64 / gate_fbb14ecd0a05` | PASS para reconstrucción/resume exclusivamente |

Los FAIL de ambos workers no se transforman retroactivamente en PASS. El gate resume-only no
cerró TECH, no aceptó ni publicó R1. El preflight reportó controles físicos; éstos no definen la
identidad histórica de un finding desconocido.

La lectura real de `orca orchestration task-list --run run_6d0dfb237a61 --json` y originales
externos confirma la cadena posterior:

| Etapa | Identidad | Disposición |
| --- | --- | --- |
| Correction.2 | `task_df1da32a7978 / ctx_5f6eeb08e8e7 / msg_b072817bd208` | MATERIALIZED; cuatro test/testinfra paths GAP3/GAP4; desarrollo excluido de validación formal |
| Fresh independent technical audit | `task_8a6b068015dd / ctx_ad561af5fcce / msg_1a1cde68af29` | PASS — FIVE ACCEPTANCE GAPS CLOSED AND TECHNICAL VALIDATION VERIFIED |
| Technical gate-only | `task_4714a303190a / gate_01a63c36e518` | PASS a 2026-09-16T16:19:14 UTC; READY_FOR_F2E_R1_AUTHORITY_DOCUMENTATION_AND_ACCEPTANCE_MATERIALIZATION solamente |

El audit competente original es
`/tmp/feelingpilates-f2e-correction2.t0ivuh/audit-TECHNICAL-REPORT.md`,
SHA-256 `765e91eb30923cb4c449dd07f9b4f7ba8f98090bf63c8bca9338bd0ff5076c97`.
Su PASS técnico y P0=0/P1=0/P2=1 se preservan en su scope; no determina canonical TECH closure
ni aceptación de autoridad. El gate posterior permite materializar documentación, sin
aceptación/publicación/cutover. No se transfiere automáticamente un PASS técnico a cierre original: la correspondencia recuperada y
la suficiencia por finding se documentan separadamente en sección3.

La raíz de custodia de los originales es `/tmp/feelingpilates-f2e-correction2.t0ivuh` (E en
las tablas siguientes). Los archivos result.json contienen command/cwd/start/end/exit y snapshots
source before/after; los logs son los originales sanitizados. Los sellos se recalcularon físicamente
durante esta documentación y coinciden con los del audit. Los summaries son navegación, no sustitutos
de JSON/logs/XML originales. Esta raíz /tmp es evidencia operacional local, no almacenamiento
permanente garantizado; el presente review conserva los valores y sellos sin reparar los originales.


El authority audit/gate anterior `run_3c3b68d0f06b / gate_f598ddc359bd` conserva FAIL
por los siete originales no recuperados dentro de su scope acotado. `run_b0efaa8ebe42`
supersede ese missing-definition blocker por recuperación ampliada autenticada, sin convertir
el audit/gate anterior ni los preflight Worker FAILs en PASS. La nueva insuficiencia de aceptación
original TECH01/08 se registra abierta, distinta de provenance desconocida.

## 3. Correspondencia original TECH — corte histórico de provenance y cierre separado

La corrección actual se materializa en `run_b0efaa8ebe42`, Task/Dispatch
`task_f12663b70140 / ctx_9eed7ce97d17`, rol DOCUMENTER /
F2E_R1_HISTORICAL_PROVENANCE_CORRECTOR, bajo autorización condicional Phase B tras recuperar
las siete definiciones. Fuente de recuperación física:
`/tmp/feelingpilates-f2e-tech-recovery.uj4KrG/RECOVERY-REPORT.md`,
SHA-256 `a8462f86fb79edc55b5140241c80a18b70e31ed6380c36b18f2a7f0e3032d2ce`, por
`task_c42c90b259c2 / ctx_04b86b5c6d48`; el coordinador verificó independientemente
los siete originales y sus mappings antes de este despacho. Este rol persiste documentación,
no emite audit, gate ni aceptación.

Las búsquedas repository-only y archivos Orca acotados del corte anterior no recuperaron originales:
esa ausencia fue verdadera para aquel scope, no una inexistencia universal. La recuperación ampliada
lee fuentes literales de sesiones Desktop independientes S0/S1/S2 y sus asignaciones originales.
Los originales no se afirman Git-tracked. Los archivos /tmp y provider sessions son custodia local
sin garantía de retención permanente; por eso los ocho bloques originales, refinamientos adversos
relevantes y sellos se conservan dentro de este review existente. Los archivos Orca consultados en
recuperación eran sourceExact=true/contentComplete=false, con clipping/omisiones y EOF acotado:
no equivalen a un archivo exhaustivo de provider history. La ausencia en ellos no invalida S0/S1/S2.

Literal IDs: `F2E-R1-TECH-01..08`. `TECH01/TECH-01` y análogos sólo son aliases
ortográficos de esos mismos IDs; AD, F2E-R1-IA y design37.13 gates no se renumeran.
La autoridad normativa de implementación sigue siendo exclusivamente el diseño publicado y
sole handoff exactos sellados en sección1; los informes originales conservan acceptance requirements
históricos y trazabilidad, no crean otra autoridad de implementación.

| ID original / alias | Recuperación | Disposición documental actual |
| --- | --- | --- |
| F2E-R1-TECH-01 / TECH01 | RECOVERED_AUTHORITATIVELY | OPEN — ORIGINAL_REAL_JDBC_METADATA_ACCEPTANCE_EVIDENCE_UNPROVEN |
| F2E-R1-TECH-02 / TECH02 | RECOVERED_AUTHORITATIVELY | CLOSED — physical precedence + fresh actual cases |
| F2E-R1-TECH-03 / TECH03 | RECOVERED_AUTHORITATIVELY | CLOSED — exact provenance/context/V2 recompute + fresh actual cases |
| F2E-R1-TECH-04 / TECH04 | RECOVERED_AUTHORITATIVELY | CLOSED — safe value schemas/int domains/cardinality + fresh actual cases |
| F2E-R1-TECH-05 / TECH05 | RECOVERED_AUTHORITATIVELY | CLOSED — actual transaction completion/rollback + fresh actual cases |
| F2E-R1-TECH-06 / TECH06 | RECOVERED_AUTHORITATIVELY | CLOSED — typed catalog/construction drift before SQL + fresh actual cases |
| F2E-R1-TECH-07 / TECH07 | ORIGINAL_DEFINITION_RECOVERED | CLOSED FOR_CURRENT_R1_HOST_INVARIANT ONLY; original named process OLD_PROCESS_ONLY |
| F2E-R1-TECH-08 / TECH08 | RECOVERED_AUTHORITATIVELY | OPEN only for overlapping original TECH01 metadata proof requirement |

TECH01/08 abiertos identifican insuficiencia de evidencia de aceptación original. No se ha probado
un nuevo defecto de código productivo, una suite fallida ni invalidación de GAP1–5/PASS técnico.
La comparación configurada ya no enmascara la negativa metadata: el path configurado permanece
autorizado y se rechaza causalmente ENTREGADA. Sin embargo F2eSelectOnlyRole.java:214–220 lee
ORIGINAL nativa de pgjdbc y sintetiza ENTREGADA con metadata Proxy; los tests físicos exigen
ORIGINAL==CONFIGURADA==DESCRIPTOR, ENTREGADA!=ORIGINAL. Eso no acredita el valor hostil
independientemente originado por JDBC nativo que S2 exigió para primera y posteriores observaciones.
Ninguna suite verde, C0/C1/C2 ni el PASS técnico genérico dispensa ese requisito.

### 3.1 Fuentes originales autenticadas y custodia

S0 is the original inline assistant audit, not a current assignment, summary echo or later correction claim. Session01a09da3-9cdd-7de2-bb6e-64c77fd24fbe physical metadata line1 identifies Codex Desktop/vscode, creation2026-09-14T01:59:00.604Z, exact checkout/branch/HEAD above. Original user message line9 at01:59:03.694Z references the physically present audit assignment attachment:

/Users/jesusaldaircruzortiz/.codex/attachments/18dfeb40-440b-466f-935b-fa31b3710b8e/pasted-text.txt

Attachment SHA e916a76533ea0cfd7f56c3dc3e0cc59c694afe02e525dd4b68a92955d09ec7ff. It explicitly assigns FRESH_INDEPENDENT_TECHNICAL_AUDITOR / F2E_R1_IMPLEMENTATION_AUDITOR / adversarial topology, identity, lifecycle and regression roles, READ_ONLY/FRESH/INDEPENDENT/NO_FIXES and rejection of implementer self-validation. Assistant response line265 at02:12:47.057Z independently records exact HEAD/design/handoff SHA,21 authorized candidate paths and all8 original Severity/Evidence/Failure mode/Impact/Blocking/Minimum correction blocks.

There is no attested historical Orca Run/Task/Dispatch ID for these original Desktop sessions; none is invented. Provider session identity, original role attachment, UTC rows, exact cwd/Git metadata, authority hashes and successive fresh re-audits establish original F2E R1 origin. Current Orca runs are later consumers of the same authority chain. No old source fingerprint/count replaces current e2 binding.

Source identities below are sealed from physical bytes; raw-row SHA includes its original LF, assistant-text SHA is UTF8 concatenated text. Historical line numbers in quoted findings describe historical source, not today’s line positions.

#### S0 — ORIGINAL independent technical audit

Path: /Users/jesusaldaircruzortiz/.codex/sessions/2026/09/13/rollout-2026-09-13T19-59-00-01a09da3-9cdd-7de2-bb6e-64c77fd24fbe.jsonl

Physical JSONL row 265; UTC 2026-09-14T02:12:47.057Z; file SHA256 0925093a4a953361f7498a245335e431ebfd02195d90603abaf59f1db3f96fba; raw row SHA256 1854c56abb67f2e91cb81ea00a08cd0b51486dc3e7d7c4318add757f24f1d94f; assistant text SHA256 7afe0848fb329556557f61d6da5d74ab29ab50c2901f228ca0192b8e8d282420.

#### C0 — same-session correction SELF-REPORT only

Path: /Users/jesusaldaircruzortiz/.codex/sessions/2026/09/13/rollout-2026-09-13T19-59-00-01a09da3-9cdd-7de2-bb6e-64c77fd24fbe.jsonl

Physical JSONL row 1032; UTC 2026-09-14T02:51:49.681Z; file SHA256 0925093a4a953361f7498a245335e431ebfd02195d90603abaf59f1db3f96fba; raw row SHA256 60cff98e48c1398d6e3c53d51296068a9841c2e3c8b4b7c202b74b12c87792d7; assistant text SHA256 d78a251fe3a3297f20238b9c132f2944f2a54de8a17b94ed1dcf63eb4dd874a9.

#### S1 — fresh independent re-audit

Path: /Users/jesusaldaircruzortiz/.codex/sessions/2026/09/13/rollout-2026-09-13T20-58-50-01a09dda-6574-7610-ad0d-3b21cf049796.jsonl

Physical JSONL row 327; UTC 2026-09-14T03:11:00.624Z; file SHA256 6cbc206e056d7b67f1e993824783f4f3b968bc072f7de2d75252b60c81280e9a; raw row SHA256 91f702edf96c489894164a17eb1b1733e07216438cccba06d7813a95b93412b6; assistant text SHA256 4c585beb55a62e8a2375f5b56bad65d538bfde997857d253d8073e10819f635c.

#### C1 — same-session residual correction SELF-REPORT only

Path: /Users/jesusaldaircruzortiz/.codex/sessions/2026/09/13/rollout-2026-09-13T20-58-50-01a09dda-6574-7610-ad0d-3b21cf049796.jsonl

Physical JSONL row 955; UTC 2026-09-14T03:45:15.389Z; file SHA256 6cbc206e056d7b67f1e993824783f4f3b968bc072f7de2d75252b60c81280e9a; raw row SHA256 8565c42b77e2ede51fd13bcdbc1aa1c7859f4b91faa2aab13127777d24d19d08; assistant text SHA256 a2dd18710668332ad7d022f39ad404e955a7fc041de5854b9ed340aa6b1dc106.

#### S2 — fresh independent residual re-audit

Path: /Users/jesusaldaircruzortiz/.codex/sessions/2026/09/13/rollout-2026-09-13T21-47-52-01a09e07-48c5-79b2-b863-6534e3808c0d.jsonl

Physical JSONL row 410; UTC 2026-09-14T04:01:34.908Z; file SHA256 a4d7db60671e5168e8d4b51a71297f1621d4c7d39bc35d0c4214523ec5fab602; raw row SHA256 bbfd3a864763a49d64a7ebf67a46ed2b5c066c39b5a8777fdd69f7fec19cb9bf; assistant text SHA256 afd0284328cd5fb0bb72505aee8e02d7368dcbb65cfb8542a1175540644e5d3d.

#### C2 — same-session final correction SELF-REPORT only

Path: /Users/jesusaldaircruzortiz/.codex/sessions/2026/09/13/rollout-2026-09-13T21-47-52-01a09e07-48c5-79b2-b863-6534e3808c0d.jsonl

Physical JSONL row 993; UTC 2026-09-14T04:25:12.914Z; file SHA256 a4d7db60671e5168e8d4b51a71297f1621d4c7d39bc35d0c4214523ec5fab602; raw row SHA256 e9061dfcbc34ec7f831938859d119369a09c0e314f44d43ad44af04967d74174; assistant text SHA256 cfb8551270f96057e62c795c1e6cd9b9b793fe5f30c2a859e62bdbc363a78426.

S1 session metadata creation2026-09-14T02:58:50.900Z, exact cwd/Git baseline; user line9 at02:58:53.278Z references attachment /Users/jesusaldaircruzortiz/.codex/attachments/ac083c38-63e1-4cfa-892a-b7a3f0eac54e/pasted-text.txt SHA f9c4afa577232291a8aa2272873c26d58d2210f38e4172634eb5d636cf508898. Explicit role FRESH_INDEPENDENT_TECHNICAL_REAUDITOR; correction assertions are not trusted. S1 preserves TECH02/03/05/06 CLOSED and reopens01/04/08. Its07 closed assertion is subsequently falsified by S2, not used as present approval.

S2 session metadata creation2026-09-14T03:47:52.678Z, exact cwd/Git baseline; user line9 at03:47:54.715Z references attachment /Users/jesusaldaircruzortiz/.codex/attachments/ac08c1dc-f01d-403f-9f49-999db0a9f05c/pasted-text.txt SHA 8f20646aa388acdf1b08a3fd3e5f7082a41343af84cdfc044490cf097bde947b. Explicit FRESH_INDEPENDENT_RESIDUAL_TECHNICAL_AUDITOR. S2 preserves02/03/04/05/06 CLOSED but reopens01/07/08. C2 subsequently claims01/08 corrected and only07 old-owner gap left; it is a correction SELF-REPORT, not independent closure authority.

Actual code plus original current fresh receipts/logs/retained XML and the independent technical audit are used below to verify mappings. Original definitions never come from C0/C1/C2 summaries.


### 3.2 Binding común obligatorio para CADA finding y mapping actual

Cada bloque TECH01/02/03/04/05/06/07/08 de esta sección está vinculado al mismo checkout, branch,
HEAD y candidate exact21 content SHA
`e2b64abd6aba8a050df6184f6c5440d87db83a67182fb43e622f48cf96f4f3ce`,
path SHA `f400a0602f95e318845da670bee4f819f057842adf8a60506564d5bd75e41d14`,
diseño SHA `6c72cba1f83fbc2bcf3b3219d8252d2410ec482e30ae85d30fd2eeb04e8883d8`
y handoff SHA `3fd71faca4d4c049ad5cb37b52bc6fd512509cf5b696bdc5c28d28cb966af8ef`.
Sus paths concretos son parte del manifest per-file sección8. Fuente independiente competente
para cada mapping: `run_6d0dfb237a61 / task_8a6b068015dd / ctx_ad561af5fcce`,
audit físico `E/audit-TECHNICAL-REPORT.md`, SHA
`765e91eb30923cb4c449dd07f9b4f7ba8f98090bf63c8bca9338bd0ff5076c97`,
más los originales S0/S1/S2 y la recuperación física arriba sellada.
Los receipts/logs fresh específicos por suite, comando exacto, UTC, root y sellos son sección5;
los casos nominales abajo se corroboran contra XML actuales retenidos de fresh full o host,
no contra un targeted XML original sobrescrito. La lectura estructural conserva no failure/error/skipped.
El audit técnico original inspeccionó los controles físicos, no emitió cierre original numerado:
el enlace ID/definición es la recuperación actual verificada por coordinador, sujeto a nuevo audit fresh.

| XML físico bajo target/surefire-reports/ | Stage realmente retenido | Cases / failure / error / skip | SHA-256 |
| --- | --- | --- | --- |
| TEST-com.feelingpilates.transicion.programacion.adapter.jpa.ReservaProjectionMapperTest.xml | fresh full | 14/0/0/0 | e76ee184cb2df8dbb279f9705bfce9ddb62e6cb8031eb808a54847c5ac14ec2f |
| TEST-com.feelingpilates.transicion.programacion.adapter.jpa.ReservaJpaReaderTransactionTest.xml | fresh full | 28/0/0/0 | 7bf17aaf2e13facd2d49b1e77fb0c16d3695a25c660a37467a610dda90916d4c |
| TEST-com.feelingpilates.transicion.programacion.adapter.jpa.ReservaJpaReaderPostgreSqlTest.xml | fresh separate host | 7/0/0/0 | 218fdbdd6b320a6a2409d827aa6a932011d0de085b60a9d37727a70de55cfa92 |
| TEST-com.feelingpilates.transicion.programacion.adapter.jpa.ReservaJpaReaderArchitectureTest.xml | fresh full | 4/0/0/0 | ac22abfe89046848e79220954450ac7ac12214dbf8f94a273d6cc083734a4c30 |
| TEST-com.feelingpilates.transicion.programacion.adapter.jpa.ReservaJpaReaderRuntimeIsolationTest.xml | fresh full | 6/0/0/0 | 4d4d3c665903b641fc79518599c2031f6ffe8a1207ceea4669ea101567b183d2 |

### TECH-01 — Complete original JDBC metadata URL authorization

Status RECOVERED_AUTHORITATIVELY. Original origin/source/role/time/lineage: S0 row265 and common authentic provenance above; S1/S2 refinements/regression remain part of historical chain.

#### Literal original definition

~~~text
ID: F2E-R1-TECH-01
Severity: P1
Evidence: ReaderTransactionTestHarness.java:181-247; targeted runtime logs show DatabaseMetaData URL query parameters.
Failure mode: sanitizarUrl removes the complete query before validation. A real URL with forbidden or behavior-changing parameters is reduced to the expected endpoint, while the hostile test appends a query only after sanitization.
Impact: resource provenance may be accepted without validating the exact connection URL required by authority; test can pass despite the real weakness.
Blocking: YES
Minimum correction: validate the original metadata URL according to the closed contract and exercise a hostile real DataSource/connection URL, not a post-sanitization observation.
~~~

Current design/handoff connection: Design37.4.1/37.9/37.12; handoff10.2–10.4/14.I/S: exact sealed endpoint/database and participating metadata must agree before probes; destructive sanitization cannot authorize a resource.

Actual validated implementation paths: src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/testinfra/ReaderTransactionTestHarness.java:350–428 reads metadata from the joined Session/Connection. validarRecurso validates configured and metadata URL independently; validarUrlJdbcOriginal requires complete descriptor equality, rejecting query/fragment/user-info/percent-encoding/noncanonical host/database/port. No cached first observation is authority. F2eSelectOnlyRole.java:197–241 decorates getMetaData().getURL() of the actual DataSource-delivered participating reader-role Connection.

Actual tests: ReservaJpaReaderTransactionTest.java:381 urlMetadataHostilDeConexionRealParticipanteRechazaConAutoridadConfiguradaValida; :403 urlJdbcOriginalRechazaMatrizHostilCompletaAntesDeProbes; :431 urlHostilEnPrimeraObservacionYLuegoDeExitoSeRechazaDesdeDescriptor.

Existing fresh command/stage/probe evidence: Transaction targeted28/28 root/log and fresh full Transaction XML28/28 contain all3 cases. They assert descriptor==configured==underlying metadata original, delivered metadata!=original, actual metadata observations and Connection delivery, zero new statements/no accepted output. First/later observation and8 variants execute on the real participating connection path.

Independent audit support: common actual technical report, task/dispatch/source SHA and fresh evidence below, not a claim of previously emitted numbered closure.

Supported current mechanisms and limits: Complete metadata is validated before capture/probes and hostile provenance cannot escape. Removing metadata comparison leaves configured comparison authorized and makes these negative tests fail. This is candidly a hostile metadata decorator on a real participating connection, not a claim that raw pgjdbc itself originated the hostile URL; it is not mutation of a post-observation ObservacionRecurso. Both original defect and S2 configured-path-masking residual have supported current mappings.

Disposición documental / soporte recuperado: definition RECOVERED_AUTHORITATIVELY; current closure OPEN — ORIGINAL_REAL_JDBC_METADATA_ACCEPTANCE_EVIDENCE_UNPROVEN. Complete URL authorization and causal participant metadata decorator are supported; underlying ORIGINAL driver URL stays canonical while hostile ENTREGADA is proxy-generated. Latest original independent audit specifically distinguishes proxied metadata from an independently hostile real JDBC URL; generic technical PASS/C2 self-closure do not adjudicate that stronger condition. Preserve candidate freeze; no new code failure proven and no repair authorized. Independent authority resolution required before acceptance.

### TECH-02 — Physical invalidity before scope failure classification

Status RECOVERED_AUTHORITATIVELY. Original origin/source/role/time/lineage: S0 row265 and common authentic provenance above; S1/S2 refinements/regression remain part of historical chain.

#### Literal original definition

~~~text
ID: F2E-R1-TECH-02
Severity: P1
Evidence: ReservaJpaReader.java:253-265.
Failure mode: readByScope evaluates salon/date membership before mapper validation. A physical row with null salonId or date is reported as READ_SET_INVARIANT_VIOLATION.
Impact: violates the exhaustive failure contract, which assigns projected null/type/range failures to ADAPTER_INPUT_INVALID.
Blocking: YES
Minimum correction: validate physical row scalar/null invariants before scope-membership checks and add precedence tests.
~~~

Current design/handoff connection: Design36.1–36.3/37.12; handoff8.1/8.2/14.C/F: invalid projected scalars use ADAPTER_INPUT_INVALID, only a valid outside-scope row uses READ_SET_INVARIANT_VIOLATION.

Actual validated implementation paths: src/main/java/com/feelingpilates/transicion/programacion/adapter/jpa/ReservaJpaReader.java:306–364 invokes validarFilasFisicas/mapper.validarFilaFisica before completeness/duplicates/scope membership in BOTH operations.

Actual tests: ReservaProjectionMapperTest.java:306 validacionFisicaPrecedeScopeParaCadaNullEstadoPrecisionYRango; :90 rechazaFilaNulaEstadoDesconocidoYRangoNoPositivoComoInputDelAdapter. Null salon/date and other required scalars, state, precision and positive range are checked before simultaneous scope mismatch.

Existing fresh command/stage/probe evidence: Mapper targeted14/14 root/log and retained fresh full Mapper XML14/14 contain these exact cases;0 failure/error/skip. S1/S2 independent historical re-audits preserve TECH02 CLOSED.

Independent audit support: common actual technical report, task/dispatch/source SHA and fresh evidence below, not a claim of previously emitted numbered closure.

Why satisfied in validated e2 candidate: Invalid row cannot be misclassified by scope access; deterministic physical ordinal/column and typed invalid-input failure abort the entire batch without partial output.

Disposición documental / soporte recuperado: mapped requirement satisfied; no unmet original requirement identified. Disposición documental actual: CLOSED, por definición original autenticada, mapping físico al candidate e2, casos XML retenidos, receipts/logs fresh y audit técnico competente; no por conteo histórico ni self-report. Su revisión de autoridad fresh sigue PENDING.

### TECH-03 — Complete provenance/business-context/identity reconstruction

Status RECOVERED_AUTHORITATIVELY. Original origin/source/role/time/lineage: S0 row265 and common authentic provenance above; S1/S2 refinements/regression remain part of historical chain.

#### Literal original definition

~~~text
ID: F2E-R1-TECH-03
Severity: P1
Evidence: ReservaJpaReader.java:154-195.
Failure mode: final cross-consistency verifies map size and only selected normalizedFields. It does not reconstruct or compare the exact 32-key/value map or businessTimeContext from final immutable output.
Impact: a provenance map with false values can remain internally accepted if its size and the small checked subset match.
Blocking: YES
Minimum correction: reconstruct and compare the full authoritative provenance and all four identity preimages from final immutable objects.
~~~

Current design/handoff connection: Design37.5–37.7; handoff6.1.3–6.1.6/14.T require the exact32 keys/values, business context and final immutable record identity preimages.

Actual validated implementation paths: src/main/java/com/feelingpilates/transicion/programacion/adapter/jpa/ReservaJpaReader.java:169–273 creates independent verification calculation, recomputes execution/logical/source/snapshot IDs, reconstructs all32 expected normalized fields and framed businessTimeContext, then compares complete map and record/row/provenance equality. ReadSnapshotIdentifiers provides exact V2 framing. historical target is empty/ABSENT.

Actual tests: ReservaProjectionMapperTest.java:368 consistenciaFinalRechazaCampoNoRevisadoContextoExtraFaltanteValorEIdentidad; :445 recomputacionDeReferenciaIndependienteCubreSeisPreimagenesV2; :56 reproduceLosSeisVectoresV2YLaProyeccionCanonicaDe1210Bytes. TransactionTest.java:531 recomputeFinalRechazaEvidenciaStatementsEInvocacionConPreimagenInconsistente.

Existing fresh command/stage/probe evidence: Mapper14 and Transaction28 targeted roots/logs plus retained fresh full XML contain all mapped cases,0 failure/error/skip. S1/S2 preserve historical TECH03 CLOSED.

Independent audit support: common actual technical report, task/dispatch/source SHA and fresh evidence below, not a claim of previously emitted numbered closure.

Why satisfied in validated e2 candidate: Complete map equality rejects wrong/missing/extra fields rather than map-size-only acceptance; all identity preimages and business context agree before immutable batch output, otherwise exact consistency exception discards everything.

Disposición documental / soporte recuperado: mapped requirement satisfied; no unmet original requirement identified. Disposición documental actual: CLOSED, por definición original autenticada, mapping físico al candidate e2, casos XML retenidos, receipts/logs fresh y audit técnico competente; no por conteo histórico ni self-report. Su revisión de autoridad fresh sigue PENDING.

### TECH-04 — Closed safeContext values and finite/cardinality-safe domains

Status RECOVERED_AUTHORITATIVELY. Original origin/source/role/time/lineage: S0 row265 and common authentic provenance above; S1/S2 refinements/regression remain part of historical chain.

#### Literal original definition

~~~text
ID: F2E-R1-TECH-04
Severity: P1
Evidence: ReservationReadException.java:8-33.
Failure mode: safeContext validates only key membership and non-null values. It accepts invalid operation/scope values, unordered or malformed UUID lists, arbitrary physical columns, oversized/non-ASCII SQLState, raw SQL or sensitive text under an allowed key.
Impact: the exact failure/safe-reporting contract and PII/SQL/credential exclusion are not enforced.
Blocking: YES
Minimum correction: enforce the closed value schema per key and add hostile value/exfiltration tests.
~~~

Current design/handoff connection: Design36.3; handoff8.3/14.F require exhaustive schemas and SQL/credentials/PII exclusion. S1 additionally requires actual integer domains and elimination of uniform4096 limit causing valid typed failures to collapse.

Actual validated implementation paths: src/main/java/com/feelingpilates/transicion/programacion/read/ReservationReadException.java:13–159 enforces operation/scope/physical-column/date/UUID/hash/SQLState and control schemas; counts[0,INT_MAX], ordinal[1,INT_MAX], vendor[INT_MIN,INT_MAX], parseInt plus exact decimal roundtrip. UUID lists are canonical lowercase, unsigned ascending/unique, with String-representation-derived capacity and no arbitrary4096 bound.

Actual tests: ReservaProjectionMapperTest.java:187 safeContextAplicaEsquemaCerradoDeValoresSinTransportarPayloadHostil; :229 safeContextAplicaDominiosIntCanonicosYRechazaOverflow; :267 safeContextSoportaListaCanonicaMayorAlAntiguoLimiteYConservaFalloTipado; :288 limiteListaUuidDerivaDeLaRepresentacionStringSinInventarLimiteDeNegocio.

Existing fresh command/stage/probe evidence: Mapper targeted14/14 root/log and retained full XML contain all4 exact cases,0 failure/error/skip. The high-cardinality read preserves SOURCE_RECORD_NOT_FOUND. S2 marks04 CLOSED after S1 finite-domain residual.

Independent audit support: common actual technical report, task/dispatch/source SHA and fresh evidence below, not a claim of previously emitted numbered closure.

Why satisfied in validated e2 candidate: Allowed keys cannot transport arbitrary payload; values must meet closed grammar/domain. Overflow is rejected, legitimate large canonical lists preserve typed R1 failures, and safe reporting excludes SQL/customer/credential payload.

Disposición documental / soporte recuperado: mapped requirement satisfied; no unmet original requirement identified. Disposición documental actual: CLOSED, por definición original autenticada, mapping físico al candidate e2, casos XML retenidos, receipts/logs fresh y audit técnico competente; no por conteo histórico ni self-report. Su revisión de autoridad fresh sigue PENDING.

### TECH-05 — Registry terminal classification at actual transaction completion

Status RECOVERED_AUTHORITATIVELY. Original origin/source/role/time/lineage: S0 row265 and common authentic provenance above; S1/S2 refinements/regression remain part of historical chain.

#### Literal original definition

~~~text
ID: F2E-R1-TECH-05
Severity: P1
Evidence: ReaderTransactionTestHarness.java:142-175 and 412-435; no rollback test exists in ReservaJpaReaderTransactionTest.
Failure mode: success is recorded inside the transactional target before the Spring interceptor completes the transaction. A rollback-only or transaction-completion failure occurs after the method returns internally and cannot transition ACTIVE to CONSUMED_ABORTED/OPEN_AFTER_ABORT.
Impact: registry lifecycle can claim COMPLETED_SUCCESS although no result escaped through a successful transaction completion.
Blocking: YES
Minimum correction: bind terminal registry classification to actual transaction completion and test a real rollback/commit failure.
~~~

Current design/handoff connection: Design37.7.1; handoff10.5/14.V/W: successful accepted escape, rollback/completion failure and UNKNOWN govern consumed-state transitions.

Actual validated implementation paths: src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/testinfra/ReaderTransactionTestHarness.java:252–270 registers actual Spring synchronization. Only afterCompletion(STATUS_COMMITTED) with validated result consumes success; rollback consumes abort, other completion UNKNOWN. Synchronized registry retains consumed markers; actual harness/reader proxies name the same manager.

Actual tests: ReservaJpaReaderTransactionTest.java:473 rollbackOnlyTrasBodyNormalNoMarcaExitoNiDejaEscaparResultado; :488 falloRealEnBeforeCommitClasificaAbortTrasCruzarInterceptor; :502 exitoPermaneceActiveEnBeforeCommitYSeConsumeSoloEnAfterCompletion; :342 interrupcionNoClasificadaDejaUnknownYBloqueaTodoElRun; :324 falloDespuesDeConstruirIdentidadesConservaAbortadoYExigeNuevoAttempt.

Existing fresh command/stage/probe evidence: Transaction targeted28/28 root/log and retained fresh full XML contain all5 cases,0 failure/error/skip. S1/S2 preserve historical05 CLOSED.

Independent audit support: common actual technical report, task/dispatch/source SHA and fresh evidence below, not a claim of previously emitted numbered closure.

Why satisfied in validated e2 candidate: beforeCommit remains ACTIVE; only committed completion crossing the real interceptor yields CONSUMED_SUCCESS/COMPLETED_SUCCESS. Rollback/completion errors expose no accepted result and preserve consumed abort/UNKNOWN markers.

Disposición documental / soporte recuperado: mapped requirement satisfied; no unmet original requirement identified. Disposición documental actual: CLOSED, por definición original autenticada, mapping físico al candidate e2, casos XML retenidos, receipts/logs fresh y audit técnico competente; no por conteo histórico ni self-report. Su revisión de autoridad fresh sigue PENDING.

### TECH-06 — Single typed catalog and pre-SQL drift rejection

Status RECOVERED_AUTHORITATIVELY. Original origin/source/role/time/lineage: S0 row265 and common authentic provenance above; S1/S2 refinements/regression remain part of historical chain.

#### Literal original definition

~~~text
ID: F2E-R1-TECH-06
Severity: P1
Evidence: ReadSnapshotContext.java:68-124; ReservaProjectionQueryExecutor.java:22-33; ReservaProjectionMapper.java:98-134; ReservaJpaReader.java:34-37.
Failure mode: the closed projection catalog is duplicated across enum metadata, SQL literals, mapper literals and digest constants. Executor and mapper do not accept the enum and no startup check rejects drift.
Impact: query/mapper/catalog provenance can diverge without the required construction/startup failure.
Blocking: YES
Minimum correction: bind executor, mapper, reader and provenance to the single typed catalog authority and validate drift before SQL.
~~~

Current design/handoff connection: Design37.3/37.7; handoff6.1.2/6.1.6/14.E/T require R1_RESERVA_V1 as sole typed SQL/query/mapper/provenance authority.

Actual validated implementation paths: src/main/java/com/feelingpilates/transicion/programacion/read/ReadSnapshotContext.java ProjectionCatalogVersion owns closed metadata. ReservaProjectionQueryExecutor.java:24–50 receives enum and validates SQL/digests; ReservaProjectionMapper constructor validates mapper metadata/order. ReservaJpaReader.java:55–77 checks executor/catalog/mapper instance equality and derives provenance from catalog.

Actual tests: ReservaProjectionMapperTest.java:410 construccionRechazaDriftSqlOrdenMapperDigestYReaderAntesDeSql; ReservaJpaReaderArchitectureTest.java:51 contratoPublicoUsaCatalogoCerradoYTransactionManagerExplicito. Independently wrong SQL/order/mapper/digest/catalog identities must fail construction before SQL.

Existing fresh command/stage/probe evidence: Mapper14/14 and Architecture4/4 targeted roots/logs; full Mapper XML contains drift case. Full69/649 corroborates same source. S1/S2 preserve historical06 CLOSED.

Independent audit support: common actual technical report, task/dispatch/source SHA and fresh evidence below, not a claim of previously emitted numbered closure.

Why satisfied in validated e2 candidate: Query/mapper/catalog cannot diverge into an executable reader; constructor drift checks occur before SQL, and final cross-consistency checks catalog-derived provenance before output.

Disposición documental / soporte recuperado: mapped requirement satisfied; no unmet original requirement identified. Disposición documental actual: CLOSED, por definición original autenticada, mapping físico al candidate e2, casos XML retenidos, receipts/logs fresh y audit técnico competente; no por conteo histórico ni self-report. Su revisión de autoridad fresh sigue PENDING.

### TECH-08 — Complete acceptance cases and actual participating hostile resources

Status RECOVERED_AUTHORITATIVELY. Original origin/source/role/time/lineage: S0 row265 and common authentic provenance above; S1/S2 refinements/regression remain part of historical chain.

#### Literal original definition

~~~text
ID: F2E-R1-TECH-08
Severity: P1
Evidence: the five R1 test classes contain 32 tests but omit multiple mandatory acceptance cases; hostile resource tests mutate ObservacionRecurso after observation.
Failure mode: absent or self-referential evidence includes actual hostile DS/EMF/TM/EM/Session/Connection graphs, real rollback, manifest mutation, digest collision, exhaustive failure precedence/safe context, independent V2 preimage recomputation and JDBC proof for rejected SQL.
Impact: 32/32 and 622/622 can remain green while material authority violations F2E-R1-TECH-01 through F2E-R1-TECH-06 remain present.
Blocking: YES
Minimum correction: add the missing handoff-required acceptance cases and replace synthetic seams where the contract requires mutation of the authoritative resource.
~~~

Current design/handoff connection: Original TECH08 explicitly covers missing handoff14.A–W cases; design37.9–37.13/37.12 connects current requirements without renumbering. S2 requires actual participating graph/physical switch, not independent connections supplied into observation.

Actual validated implementation paths: All5 allowlisted suites and testinfra. F2ePostgresTestConfiguration.java:130–322 boots participating DS/EMF/TM/sharedEM/Session graph variants and asserts graph participant use. F2eSelectOnlyRole.java:179–290 delivers a switching Connection from the ACTUAL reader DataSource to JpaTransactionManager; SQL methods change its actual delegate between two distinct unwrapped PGConnection resources. The two delegates are acquired during DataSource delivery, not opened by harness to substitute an observation. Harness obtains its before/after physical resource through the transaction-bound Session. Negative decorators are test-only.

Actual tests: TransactionTest.java:226 cambioDeConexionDespuesDePruebaDescartaResultadoYConsumeAbortado; :250 grafosHostilesRealesLleganAlValidadorSinMutarObservacionRecurso covers6 seams; :513 manifiestoRealRechazaFaltaExtraOrdenDataExtraYCatalogoIncorrecto; :549 sqlDesconocidaEsRechazadaAntesDePreparacionJdbcReal; :285 colisionEnCadaDominioReservaTodoONada; :473/:488 rollback/completion. MapperTest.java:445 independent6 preimages; :519 colisionDigestConPreimagenDistintaFallaCerradoEnSesionReal; :306/:368 precedence/provenance; :187/:229/:267 safeContext. Runtime tests SQL/checksum vectors/default+prod absence, Architecture exactscope, PostgreSQL grants/native queries/42501/causal checksums.

Existing fresh command/stage/probe evidence: All5 targeted59, full69/649 and separatehost7 on exacte2 pass. Fresh full Mapper14 and Transaction28 XML contain all named cases; fresh hostPG7 XML includes denied INSERT omitted by regexJSON arrays. Original sanitized logs prove manifests/denial/checksum. Independent technical audit's actual source scrutiny verifies hostile graphs/metadata, pre-JDBC rejection, collisions, rollback/beforeCommit and vector/context cases.

Independent audit support: common actual technical report, task/dispatch/source SHA and fresh evidence below, not a claim of previously emitted numbered closure.

Supported current mechanisms and limits: Actual graph participation and genuinely distinct physical delegate switch are asserted; no accepted hostile output; proper abort state. Independent reference preimages and missing-case coverage are physically executed. Forbidden SQL is denied before its JDBC preparation. Negative hostile switch is not claimed to be a valid snapshot. Runtime isolation/source allowlist/current no-write evidence remain bounded to R1; no unsupported broader acceptance claim.

Disposición documental / soporte recuperado: definition RECOVERED_AUTHORITATIVELY; current closure OPEN — OVERLAPPING_ORIGINAL_TECH01_METADATA_PROOF_REQUIREMENT_UNPROVEN. Actual participating graph/physical-switch, independent references and other cases are supported; overlapping original independently-hostile metadata evidence remains unproven while TECH-01 stays OPEN. Preserve candidate freeze; no new code failure proven and no repair authorized. Independent authority resolution required before acceptance.


### TECH-07 — Original histórico y control host R1 actual separado

~~~text
ID: F2E-R1-TECH-07
Severity: P1
Evidence: active handoff sections 13–14 and orchestration ROLES/GATES; no separate HostValidator run or executable evidence exists.
Failure mode: Case B treats host assertions inside implementation tests as the HostValidator gate.
Impact: collapses the required deterministic independent host role into implementation/test evidence and omits its source-fingerprint/preflight lifecycle.
Blocking: YES
Minimum correction: execute the authorized external deterministic HostValidator plan and preserve its before/after evidence for re-audit.
~~~

S1 afirmó CLOSED del proceso antiguo; S2 lo reabrió por inexistencia del plan F2E R1
configured/static/non-LLM HostValidator (sólo f2d2). Se conserva abajo la reapertura literal.
C2 señaló al owner nominal `/Users/jesusaldaircruzortiz/FeelingPilatesOrchestrator`
y su condición histórica no versionada/hardcoded; es self-report histórico, no constatación del
owner hoy ni cierre independiente. Owner y mecanismos HostValidator/Autopilot quedan
OLD_PROCESS_ONLY, nunca revividos ni retrospectivamente aprobados.

Disposición actual: CLOSED FOR_CURRENT_R1_HOST_INVARIANT ONLY. Requirement vigente y autoridad:
diseño27/28/37.10 + handoff10/13/14. Mapping físico: F2ePostgresTestConfiguration
Flyway50/current47/pending0/JPA validate; F2eSelectOnlyRole hard grants/INSERT42501;
harness/reader proxied same explicit manager/sharedEM/Session/Connection; inspector ordered
2 probes+1 data SELECT; F2eSliceChecksum before/after persistent observer. Casos XML nominales:
hostRealCumpleImagenRedFlywayV47YJpaValidate,
principalLectorPuedeSeleccionarReservaPeroInsertEsDenegado,
grantsDelPrincipalSonExactamenteLecturaDeReservaSinCreateTempDmlSecuenciaOFuncion,
leeProjectionNativaPorIdentidadesSinAlterarElSlice,
leePorScopeAcotadoYDevuelveOrdenInmutable; current fresh host7 original receipt/log,
preflight/during/after y audit competente sellados se detallan en secciones5/6.
No se confunde este control actual con cumplimiento retrospectivo del viejo proceso nombrado.

### 3.3 Refinamientos adversos literales posteriores — historia sellada

S1 row327, source/file/raw/text seals y rol independiente en sección3.1. Bloques P1 originales:

~~~text
ID: F2E-R1-TECH-01

Severity: P1

Evidence: `ReaderTransactionTestHarness.validarYCanonizarUrlOriginal` compares the original metadata URL with a cached observed URL, then strips the query before comparison with the configured closed descriptor. Successful PostgreSQL executions exposed metadata URLs containing query parameters.

Failure mode: Unauthorized information present in the initially observed URL can be accepted and erased before closed-authority comparison.

Impact: Resource provenance is not deterministically authorized from the complete original JDBC metadata URL.

Blocking: YES

Minimum correction: Validate the complete original `DatabaseMetaData.getURL()` directly against the immutable closed descriptor before deriving any sanitized/report-safe representation, with real hostile tests for query, fragment, user-info and percent-encoding.

ID: F2E-R1-TECH-04

Severity: P1

Evidence: `ReservationReadException` accepts a 4096-digit `requestedCount`. A canonical ordered list of 111 unique UUIDs is 4106 characters and causes generic `IllegalArgumentException` during typed failure construction.

Failure mode: Unbounded numeric grammars accept impossible values, while the uniform length bound prevents legitimate high-cardinality failure context from preserving its required failure type.

Impact: Safe-reporting and failure-classification contracts are materially non-conformant.

Blocking: YES

Minimum correction: Bind numeric fields to their actual domains and reconcile request cardinality with per-key serialization limits so valid typed failures cannot collapse into generic construction errors.

ID: F2E-R1-TECH-08

Severity: P1

Evidence: Critical hostile variants are introduced by replacing fields in a local `ObservacionRecurso`; the actual Spring DataSource/EMF/TM/EntityManager/Session graph remains valid. The connection-switch test uses a distinct proxy over the same physical connection.

Failure mode: Acceptance tests can pass while actual hostile dependency wiring or a genuine transaction-bound physical-connection change remains unexercised.

Impact: Resource-provenance enforcement lacks required real-runtime acceptance evidence.

Blocking: YES

Minimum correction: Exercise independently booted or injected hostile Spring/JPA resource graphs for every critical seam and perform a genuine non-null transaction-bound physical-connection identity change observed by the production validator.
~~~

S2 row410, source/file/raw/text seals y rol independiente en sección3.1. Bloques P1 originales:

~~~text
ID: F2E-R1-TECH-01
Severity: P1
Evidence: The validator checks `urlConfigurada()` before metadata at [ReaderTransactionTestHarness.java](/Users/jesusaldaircruzortiz/Desktop/Feelingpilates/feelingpilates/src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/testinfra/ReaderTransactionTestHarness.java:314). The real hostile setup strips the query and records it separately at [F2eSelectOnlyRole.java](/Users/jesusaldaircruzortiz/Desktop/Feelingpilates/feelingpilates/src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/testinfra/F2eSelectOnlyRole.java:81). The real test does not assert the actual metadata URL at [ReservaJpaReaderTransactionTest.java](/Users/jesusaldaircruzortiz/Desktop/Feelingpilates/feelingpilates/src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/ReservaJpaReaderTransactionTest.java:280); other hostile URLs are supplied by a metadata proxy.
Failure mode: The claimed real hostile test remains green if metadata authorization is removed because the separate configured-URL comparison fails first.
Impact: The implementation looks mechanically correct, but publication-grade evidence does not prove hostile original metadata on the first or later real connection observation.
Blocking: YES
Minimum correction: Exercise a real connection whose asserted `DatabaseMetaData.getURL()` contains the hostile authority-bearing value, keep the configured descriptor path otherwise authorized, and prove the test fails specifically through the metadata comparison on both first and later observations.

ID: F2E-R1-TECH-07
Severity: P1
Evidence: The protocol requires a non-LLM static configured allowlist at [GATES.md](/Users/jesusaldaircruzortiz/Desktop/Feelingpilates/feelingpilates/auditoria/orquestacion/GATES.md:55). The external workflow’s `host_validation_plans` contains only `f2d2`, and `HostValidator.plan_is_valid()` recognizes only the hard-coded F2D2 plan. No F2E R1 plan exists.
Failure mode: Direct commands run by the auditing agent can reproduce tests but cannot produce the required non-LLM HostValidator gate or its authoritative result.
Impact: The correction claim `HostValidator: PASS` is not independently reproducible under the active protocol.
Blocking: YES
Minimum correction: Configure and approve an exact allowlisted F2E R1 HostValidator plan, then execute it through the non-LLM HostValidator with invariant pre/post fingerprints and preserved source state.

ID: F2E-R1-TECH-08
Severity: P1
Evidence: Alternate DS/EMF/TM/EM references are placed into the descriptor at [F2ePostgresTestConfiguration.java](/Users/jesusaldaircruzortiz/Desktop/Feelingpilates/feelingpilates/src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/testinfra/F2ePostgresTestConfiguration.java:212), while the actual beans remain unchanged. The alternate Session is supplied through a harness field, and the connection case opens `readerDataSource.getConnection()` and passes it into the second observation at [F2ePostgresTestConfiguration.java](/Users/jesusaldaircruzortiz/Desktop/Feelingpilates/feelingpilates/src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/testinfra/F2ePostgresTestConfiguration.java:275). The handoff explicitly forbids this at [HANDOFF-F2E-R1-RESERVA-READER-JPA-READ-ONLY.md](/Users/jesusaldaircruzortiz/Desktop/Feelingpilates/feelingpilates/auditoria/handoffs/HANDOFF-F2E-R1-RESERVA-READER-JPA-READ-ONLY.md:1080).
Failure mode: Tests substitute authority/observation references while the real Spring invocation stays on the authorized participating graph. The connection test supplies an unrelated second connection rather than changing the transaction-bound connection.
Impact: Green hostile tests do not prove the exact runtime topology and physical-connection stability invariants required for publication.
Blocking: YES
Minimum correction: Build hostile contexts in which the actual participating Spring DS/EMF/TM/shared-EM/transaction-bound Session relationships differ from the immutable descriptor, and prove physical stability using the actual transaction-bound resource without an independently opened connection or observation-substitution seam.
~~~

S2 row410, sección original que distingue metadata proxy de URL JDBC real independientemente hostil:

~~~text
## TECH-01 — JDBC URL AUTHORITY

Trust root: `DescriptorRecursoLector`

Original metadata URL: Read directly from `Connection.getMetaData().getURL()`.

First hostile observation: Rejected by the current comparison, but the first-observation test uses proxied metadata rather than an independently hostile real JDBC URL.

Cached observation authority: NONE

Authorization-before-normalization: YES; exact descriptor equality occurs before regex parsing or any reduction.

Query contract: Zero query parameters, as required by the active handoff. `?` and descriptor inequality reject queries.

Fragment: Rejected.

User-info: Rejected.

Percent encoding: Rejected generically through `%`.

Real hostile DataSource: PARTIAL; the DataSource is genuinely reconfigured, but the test strips the query into an `ApplicationName` property and does not establish that the resulting real `DatabaseMetaData.getURL()` contains the hostile URL.

Production validator path: Reads metadata, but the real-DataSource test aborts first on `urlConfigurada()` before reaching metadata authorization.

Zero accepted probes: YES

Zero accepted data SQL: YES

Zero accepted evidence/output: YES

Test false-positive risk: YES. Removing the metadata comparison would not fail the real hostile-DataSource test because validation fails first on the separately maintained configured URL.

Residual weakness: The implementation order is mechanically conforming, but mandatory real-resource acceptance evidence for hostile first/later metadata observations remains absent. The remaining metadata-hostile matrix is implemented by a dynamic metadata proxy.

Status: OPEN — P1 acceptance-evidence non-conformance
~~~

Las líneas/rutas/counts/status de las citas anteriores describen candidates históricas.
No se transforman en resultados actuales ni se reescriben. C0/C1/C2 son correction self-claims;
ni sus cierres, ni S1/S2 historical closure counts, ni generic technical PASS sustituyen código físico,
casos nominales fresh y aceptación-evidence específica. Provenance ORIGINAL completa
RECOVERED_AUTHORITATIVELY != evidencia de CLOSURE suficiente.

## 4. GAP1–5: resultado técnico preservado y mapping físico

Los siguientes son GAP explícitos del preflight y del audit técnico; no son alias TECH.

| GAP | Implementación / test físicos bajo src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/ | Evidencia original E/ | Disposición |
| --- | --- | --- | --- |
| GAP1 | ReservaJpaReaderPostgreSqlTest.principalLectorPuedeSeleccionarReservaPeroInsertEsDenegado; testinfra/F2eSelectOnlyRole y F2eSliceChecksum | host.log:138–140; PostgreSqlTest result/log; host result; audit sección GAP1 | CLOSED |
| GAP2 | ReservaJpaReaderPostgreSqlTest.leePorScopeAcotadoYDevuelveOrdenInmutable; testinfra/F2eSliceChecksum | host.log:142–143; PostgreSqlTest result/log; RuntimeIsolationTest result/log para vectors; audit GAP2 | CLOSED |
| GAP3 | ReservaJpaReaderPostgreSqlTest.huellaInstaladaEsEstableYCoincideConProvenanceReal y seamPuroDetectaSchemaFlywayNullYEstadoConOrdenEstableSinJdbc; testinfra/F2ePostgresTestConfiguration private composer | host.log:134,145; PostgreSqlTest result/log; full result/log; audit GAP3 | CLOSED — FINGERPRINT VERIFIED WITHOUT SUCCESSFUL DB MUTATION |
| GAP4 | testinfra/ReaderTransactionTestHarness.validarLabelsConfiables/autoridadLabelsConfiables; ReservaJpaReaderTransactionTest | ReservaJpaReaderTransactionTest.log:218–219,307 y result; full transaction XML; audit GAP4 | CLOSED — EXACT TRUSTED-LABEL AUTHORITY PROVEN |
| GAP5 | ReservaJpaReaderRuntimeIsolationTest inicia FeelingpilatesApplication en default/prod | ReservaJpaReaderRuntimeIsolationTest.log:53–54 y result; full result/log; audit GAP5 | CLOSED |

GAP1: baseline persistente → INSERT PostgreSQL42501 en reader-role transaction separada → containment
rollback/close de esa conexión → clean proxied read → checksum AFTER, con igualdad de hashes y count
incluido target ausente. Rollback por sí solo no es no-write proof.
GAP2: BY_SCOPE congela PKs, compara all11-column persistent checksums y reenumera PKs/returned IDs;
no sustituye checksum persistente por snapshots en memoria.
GAP3: fingerprint live lee diez catálogos schema/Flyway version/script/checksum/success con framing
null/value y orden canónico. Sensibilidad usa la misma private pure composer con catálogos sintéticos
sin JDBC; UPDATE/ALTER de sensibilidad eliminados. Respuesta obligatoria: ANY successful database
mutation for formal fingerprint correctness/sensitivity proof = NO. Bootstrap efímero autorizado
pre-window y tests persistentes ajenos en la suite global no se presentan como fingerprint proof.
GAP4: enumera todos los instance String fields de la jerarquía, rechaza hidden duplicates antes
de map/set collapse, exige exact set de tres private final labels y valores descriptor/context antes
de capture/probes. Respuesta obligatoria: ANY unauthorized trusted label causes precondition failure
= YES. Ocho hostile shapes y negativos de values/context prueban cero nueva preparación JDBC.
GAP5: default/prod se refrescan normalmente; todos los R1 names/types ausentes, legacy beans presentes;
sin config productiva modificada.

## 5. Comandos formales, tiempos, conteos y sellos originales

Todos se ejecutaron previamente en el checkout exacto de sección1 por el runner técnico.
Este DOCUMENTER no ejecutó tests/build/SQL/Docker. Se contrastaron las siete JSON roots y sus
tuplas por suite con los logs originales, command/cwd y source before/after, sin usar desarrollo
ni conteos históricos52/642. Todos los snapshots before/after coinciden con candidate21/pathSHA/
contentSHA y staging/tracked vacíos.

| Stage (E/stem.result.json y E/stem.log) | Exact command | UTC start → end, 2026-09-16 | Suites/tests/failures/errors/skips | Exit / build |
| --- | --- | --- | --- | --- |
| ReservaProjectionMapperTest | `./mvnw -Dtest=ReservaProjectionMapperTest test` | 16:08:41.071Z → 16:08:49.794Z | 1/14/0/0/0 | 0 / BUILD SUCCESS |
| ReservaJpaReaderArchitectureTest | `./mvnw -Dtest=ReservaJpaReaderArchitectureTest test` | 16:08:53.439Z → 16:08:56.588Z | 1/4/0/0/0 | 0 / BUILD SUCCESS |
| ReservaJpaReaderRuntimeIsolationTest | `./mvnw -Dtest=ReservaJpaReaderRuntimeIsolationTest test` | 16:09:02.952Z → 16:09:17.567Z | 1/6/0/0/0 | 0 / BUILD SUCCESS |
| ReservaJpaReaderTransactionTest | `./mvnw -Dtest=ReservaJpaReaderTransactionTest test` | 16:09:21.147Z → 16:09:56.869Z | 1/28/0/0/0 | 0 / BUILD SUCCESS |
| ReservaJpaReaderPostgreSqlTest | `./mvnw -Dtest=ReservaJpaReaderPostgreSqlTest test` | 16:10:00.217Z → 16:10:11.470Z | 1/7/0/0/0 | 0 / BUILD SUCCESS |
| full | `./mvnw test` | 16:10:15.483Z → 16:11:55.261Z | 69/649/0/0/0 | 0 / BUILD SUCCESS |
| host (separate stage) | `./mvnw -Dtest=ReservaJpaReaderPostgreSqlTest test` | 16:12:00.750Z → 16:12:12.687Z | 1/7/0/0/0 | 0 / BUILD SUCCESS |

Targeted total =14+4+6+28+7=59; full649/69 y separate host7 son ejecuciones distintas.

| Stem bajo E/ | result.json SHA-256 | sanitized log SHA-256 |
| --- | --- | --- |
| ReservaProjectionMapperTest | e32946fe7f315656d4c2a5f8014ec7233433b4e020a7c5848eed19a659bd561b | f5f525edca4654bfc5d8370d92f8262530c42d4efabbcad131c96c4a8e34054f |
| ReservaJpaReaderArchitectureTest | 8560c02f497423650a3d710734617d3e125b8e204b55f8d0b72bbfdafcca782c | c93ed4adcf27200ed583ac280ffdc7b3df752376a4ef148e1f21e5f45b0d949a |
| ReservaJpaReaderRuntimeIsolationTest | 287669b0ff442e59271576944a4854b91dccc8dd3aef3809ae27fa5abdc3fa89 | 485d2009fed7af575060069922d777ff7ff46f7c93ef3761ecc8d8c9ab085d63 |
| ReservaJpaReaderTransactionTest | 78734b2542636287d6f4acf6707b73cca9efdaff45418f2ad4b808df197dbeaa | 6e8bc1caf84251ad22b88a9511d595d456dcc685f3a1a0dc82716a1ec90896bf |
| ReservaJpaReaderPostgreSqlTest | 9938d993c17a20f8f06a1957da59df881deef64a1df54f78b3b5392d25a418d9 | 8aa660c297ab81cbf55d6743c18339465417b82940ee78b5d1944b79a90db6eb |
| full | d74b9bd4c851b52f14707a2f732cb26e5a3d608a1bbea165745ee9d3197ac499 | 2d4bac055bf5220d83b37ec021f854528dd9644e6288217331cbc8240378ef0e |
| host | 21a11a7e2b7bed8fac91d46bcaff6075f4f14f64742b8ccba436b51d0f5792a0 | 337f442cd1493405cc44ce6e2222a87ebc19ae5c0f68a4507b2aa5ccbac4affd |

| Original bajo E/ | SHA-256 |
| --- | --- |
| post-manifest.sha256 | e2b64abd6aba8a050df6184f6c5440d87db83a67182fb43e622f48cf96f4f3ce |
| audit-TECHNICAL-REPORT.md | 765e91eb30923cb4c449dd07f9b4f7ba8f98090bf63c8bca9338bd0ff5076c97 |
| host-preflight.json | 93720e39ff8e55b0cca6a6a3ef5292ff6fd7ef7f09ff24790eb8a2c7a18078f8 |
| host-during.json | 9cd34a1faf31cfb958c0f9eb5daaf9f7124493fa49549f0e3b646068ca583517 |
| host-after.json | 1d674bbb8ec96986a6b53aebfcbb8f1fd60ab2363cd73147d3e5c45fb13ba100 |
| validate.mjs | 0b6e6291d8c3d021735e304ef0e686f619518b5fc37bac72322cef49c9633871 |
| host-topology.mjs | 7915981098eb56fead4c2955857a3399000f86d6f0d06bd51500b0144fdbbb73 |
| formal-validation-summary.md (navigation) | 9b594aa3e4190aae879576e0cbe6aa743cf26d284aaf5f6f6f4b5450f1647497 |
| COORDINATOR-RESULT.md (gate navigation) | 40b0c5f2b306d47e766a5e0f04280e74ae8ae57d3f2724ca8ecfe46a195c28ab |

### P2-EVIDENCE-01 — NON_BLOCKING

JSON individual-case arrays son incompletos por extracción regex: Mapper13/14, Architecture4/4,
Runtime6/6, Transaction17/28, PostgreSQL6/7, full632/649, host6/7. Denied-INSERT falta en
los arrays PostgreSQL/host. No se califican completos ni se fabrican records.
Roots y logs sanitizados corroboran las ejecuciones; parsing estructural independiente de los
XML fresh disponibles corroboró69 suites/649 actual cases sin failure/error/skipped.
68 suites/642 cases corresponden al full; los7 PostgreSQL fueron sobrescritos por el host
posterior. XML targeted originales no fueron retenidos: no se reconstruye su case provenance.
El full Mapper14 y Transaction28, y el host PostgreSQL7 con denied-INSERT presente, corroboran
comportamientos del mismo candidate inmutable; el log host prueba SQLState42501 y checksums.

Paths originales de XML: checkout/target/surefire-reports/TEST-com.feelingpilates.transicion.programacion.adapter.jpa.
más cada basename siguiente (sólo se persisten seals, no raw properties/credentials):

| Basename | Stage actual conservado | SHA-256 |
| --- | --- | --- |
| ReservaProjectionMapperTest.xml | fresh full | e76ee184cb2df8dbb279f9705bfce9ddb62e6cb8031eb808a54847c5ac14ec2f |
| ReservaJpaReaderTransactionTest.xml | fresh full | 7bf17aaf2e13facd2d49b1e77fb0c16d3695a25c660a37467a610dda90916d4c |
| ReservaJpaReaderPostgreSqlTest.xml | fresh host | 218fdbdd6b320a6a2409d827aa6a932011d0de085b60a9d37727a70de55cfa92 |

La deuda no reabre GAP ni invalida la evidencia técnica corroborada. Mejorar parser/retención
requiere trabajo futuro autorizado; aquí no se modificó evidencia original ni se rerun tests.

## 6. TECH07 — CLOSED FOR_CURRENT_R1_HOST_INVARIANT

Requirement actual: real implementation host gate PostgreSQL16/JPA/Flyway, SELECT-only grants,
denied INSERT42501, exact ordered SQL/same resource y before/after scoped checksum equality,
bound al candidate de sección1. Source: autorización vigente de materialización TECH07,
diseño27/28/37.10 + handoff10/13/14. Original historical finding text RECOVERED en sección3,
incluida la reapertura S2 del antiguo plan no-LLM ausente; el cierre siguiente se limita
expresamente a la invariante vigente y no cierra retrospectivamente el viejo proceso.

Implementation: F2ePostgresTestConfiguration establece Flyway50 SUCCESS/currentV47/pending0 y
JPA validate, reader DS/EMF/TM/sharedEM separados de privileged plane; F2eSelectOnlyRole hard fence;
ReaderTransactionTestHarness proxy REQUIRES_NEW/RC/readOnly y ReservaJpaReader proxy MANDATORY
con mismo manager explícito; misma Session/physicalConnection y inspector fail-closed.
Test mapping: los siete métodos ReservaJpaReaderPostgreSqlTest, Transaction28 para recursos/
advisors/manifest/labels/identity y Runtime6 para dark launch; source hashes en apéndice.
Evidence: E/host.result.json y host.log más host-preflight/during/after, originales sellados
en sección5; sección4 GAP1–5, SQL/checksum values abajo.
Audit: task_8a6b068015dd/ctx_ad561af5fcce/msg_1a1cde68af29 y audit-TECHNICAL-REPORT.md físico:
fresh host evidence VERIFIED, sin canonical TECH closure por aquel auditor.
Disposition documental: CLOSED FOR_CURRENT_R1_HOST_INVARIANT; authority audit de esta
materialización PENDING. No retroactive old-process approval.

Host preflight16:12:00.164Z: Darwin, desktop-linux, Docker Engine29.6.1/API1.55,
postgres:16-alpine digest `sha256:57c72fd2a128e416c7fcc499958864df5301e940bca0a56f58fddf30ffc07777`.
During16:12:07.313Z: running container
`097f33ebfccc720e3e9a08816712dd93b6d539207a900850be717d83fdf3919c`,
mapped5432→55770, temporary volume. Container coincide con host.log:49.
After16:12:17.249Z: sin PostgreSQL fixture remanente. El host stage separado comienza después
del full; no se infiere de targeted Testcontainers previo. PG16.14 real, Flyway50/current47,
no pending y JPA validate corroborados por bootstrap/test/log.

Ordered reader manifest observado y verificado, exactamente dos probes + una data query:

1. isolation probe `SELECT current_setting('transaction_isolation')` → read committed;
   ID `4a669a2f628e12468e0d532889e0bcafd38c09a5aadfb27f2f20f56159c1671e`.
2. read-only probe `SELECT current_setting('transaction_read_only')` → on (canonical read only);
   ID `9963ea856cdf9bfb3e9c440b1c3a6c062fd375a906f465cbe7a7ac88878716c7`.
3. data query BY_RESERVATION_IDS
   ID `dfa84c5db84f44c41adb82b0a58a2f080fc05a0612df15ababbd5dc064192a5b`,
   o BY_SCOPE ID `c04cd40e2e3b991de845e73df65b6c9988be10c21cc1ee71474ab848b1e8eb9a`.

host.log:135/137/142 contiene las listas exactas ordenadas. Same connection antes/después y
cross-consistency se verifican antes de escape; el inspector aislado no se presenta como prueba
suficiente de physicalConnection/grants/checksum.

| Host measured scope | BEFORE = AFTER filas | hashTabla BEFORE = AFTER | hashSlice BEFORE = AFTER |
| --- | --- | --- | --- |
| GAP1 selected reservation | 1 | 020768fe27ed97b53280c775e63f99dd7e6a2b99fd3fff2d74175034fbf21f47 | e5497ad872f8703d84ec0d3beaff2a358f0202de04592f6713abc1e75463e10f |
| GAP1 denied insert target | 0 | 7cd08818f587c66f33716592705b5d716a19fd1ac434cb65e0defce21abeb916 | 44128bc8ad0ce810d9f6d554a5c87b77bedb89696a3759085ddb6c073eec03c2 |
| GAP2 frozen BY_SCOPE | 2 | 38ab5d18eeec8a67175a60b43d4562263ddc6dd1b2811a387e4cbcc1a593c099 | f894603fd2c3dfb1dbcf45b444919900e3203ecf9e29cb5e6eb8a0b55599dd6c |

Denial-then-read/target statementObservationFingerprint:
`be5e0b51994d7e20dd95976b326580c7e71aca472fe065c8138f9e57eea0dd5d`.
BY_SCOPE fingerprint:
`02a12daea618b09f2d97106e7fb2d780b75ee4a9597179af4742dff1d905f1ca`.
Live schema+Flyway fingerprint:
`sha256:357de86f42d38e16467acf97e331c607061db99e84f1aa2d0cb3212a407739f0`,
igual a provenance reader y estable en metadata SELECT-only. Checksum all11 persistent columns
incluye cliente_id sólo privadamente en hash; no se publica PII ni se confunde con source projection
V2 que la excluye. Equality prueba no mutación persistente en scope/window quiescente, no ausencia
de bootstrap writes autorizados ni de writes de tests existentes ajenos.

Historical owner `/Users/jesusaldaircruzortiz/FeelingPilatesOrchestrator`, nominal
HostValidator/Autopilot: OLD_PROCESS_ONLY. El rol de recuperación leyó fuentes históricas;
este corrector documental no ejecuta el owner ni revalida su estado actual.
No se ejecutó ni reinstaló el mecanismo viejo; el coordinator reportó sólo lectura de archivos
históricos para buscar provenance, sin autoridad operativa (follow-up histórico de prior Run
`run_3c3b68d0f06b / msg_ad7c6ddab5f9`).
La invariante REQUIRED se satisface por runner host actual con evidencia física equivalente,
sin elevar el mecanismo viejo a autoridad actual. Adapter implementation host gate tiene material
product source/custodian/credential NOT_REQUIRED; futuro material data audit exige named source/
custodian/real SELECT-only credential y Testcontainers NO lo sustituye (diseño28).
DATA_SOURCE_NOT_AVAILABLE y material data audit NOT_PERFORMED/NOT_AUTHORIZED permanecen intactos.

## 7. Lifecycle vigente, independencia y salida autorizada

```text
R1 design: CLOSED / PUBLISHED
R1 handoff: APPROVED / PUBLISHED / ACTIVE
R1 implementation: MATERIALIZED / IMMUTABLE_VALIDATED_CANDIDATE
R1 fresh technical validation / independent technical audit: PASS
GAP1 / GAP2 / GAP3 / GAP4 / GAP5: CLOSED
Original TECH01 / TECH02 / TECH03 / TECH04 / TECH05 / TECH06 / TECH08 provenance: RECOVERED_AUTHORITATIVELY
TECH02 / TECH03 / TECH04 / TECH05 / TECH06: CLOSED
TECH01: CLOSED — INDEPENDENTLY_VERIFIED_NATIVE_FIRST_LATER_METADATA_PROOF
TECH08: CLOSED — EXACT_SHARED_NATIVE_METADATA_OVERLAP_ONLY
TECH07: CLOSED FOR_CURRENT_R1_HOST_INVARIANT ONLY
Documentation: MATERIALIZED / INDEPENDENT_ACCEPTANCE_AUTHORITY_VERIFIED
Authority audit/gate current Run: task_5f12bc24768c PASS / gate_b263068ee65c PASS
Authority acceptance requirement: SATISFIED_BY_INDEPENDENT_AUDIT_AND_COMPETENT_GATE
R1 acceptance: ACCEPTED / NOT_PUBLISHED
R1 publication: NOT_PUBLISHED / READY_FOR_PUBLICATION_PREFLIGHT
Publication execution: NOT_AUTHORIZED_IN_THIS_RUN
Runtime: DARK_LAUNCH / NOT_PRODUCTIVE
TurnoInstructor: LEGACY_VIVO / PRODUCTIVO / productive authority unchanged
Cutover: NOT_AUTHORIZED / false
R2-R6 / migration / crosswalk / resolver / fence / material data audit: NOT_AUTHORIZED
Payments / Notifications / Capacity / Mobile: OUT_OF_SCOPE
```

Siguiente fase posible: PUBLICATION_PREFLIGHT separado, con autorización y gate propios, sobre
el candidato exacto aceptado y los artefactos finales de sección11. La implementación no se reabre;
no se autoriza publicación, cutover ni R2–R6 aquí. La aceptación es resolución competente posterior,
no autoaprobación del documenter. Checkpoints/reviews históricos y separación de roles preservados.

## 8. Apéndice — exact21 per-file manifest congelado

Formato canónico: SHA-256, dos espacios, path relativo, LF; orden lexicográfico de paths.
Este bloque persiste el manifest físico original E/post-manifest.sha256, sin atribuír su
implementación al documenter.

```text
572a50371f9c6451b283c25ef1d5bceb022112f60e4f97fb15679ae436aecd23  src/main/java/com/feelingpilates/transicion/programacion/adapter/jpa/ReservaJpaReader.java
91a2489f8bbdbbd61a335b9b867ab78a408e4be51ccb3443d59888f1c5918943  src/main/java/com/feelingpilates/transicion/programacion/adapter/jpa/mapper/ReservaProjectionMapper.java
9230ffce14ef1b709565a231a9c3e63f9648e731f9f155fd2e3dab5bcdf09e12  src/main/java/com/feelingpilates/transicion/programacion/adapter/jpa/policy/F2eSqlPolicyViolationException.java
3e492de34576e3dab69676bbaf4dfdf4b2a0f6b044e9f8580ea73a22ed4381b7  src/main/java/com/feelingpilates/transicion/programacion/adapter/jpa/projection/ReservaProjectionQueryExecutor.java
cfdcfaa3dd9ba7438cc24e5a712ce707fbac9b38837e8319c905d38039232e7d  src/main/java/com/feelingpilates/transicion/programacion/adapter/jpa/projection/ReservaProjectionRow.java
7f88cddf5b6c0739daa4c62c84b038b37b4112db311abf5911ba780a6d25d0ed  src/main/java/com/feelingpilates/transicion/programacion/read/ReadSnapshotContext.java
1c23fb83cdec352b885e5ce6272f49edafb0388f832f9be134544647f6c72aed  src/main/java/com/feelingpilates/transicion/programacion/read/ReadSnapshotIdentifiers.java
69e5a2891f866e6724563e36bc3e657ccaa81760a8acf97fbf28bd85998ef78f  src/main/java/com/feelingpilates/transicion/programacion/read/ReservationReadException.java
e3eefd0903eae855f9da4458dc22a9d2a2570b5993acc2c0cd8512a674ea6979  src/main/java/com/feelingpilates/transicion/programacion/read/ReservationReadFailureCode.java
6c7e54271a56803a01c12ea5484d2b2690a4f1b4236f86464b6dafbcafafb5b6  src/main/java/com/feelingpilates/transicion/programacion/read/ReservationReadPort.java
689f802b2a1a053a0400025e170d83bb16db4e1a48bf2d7830e84f6ab5d17f99  src/main/java/com/feelingpilates/transicion/programacion/read/ReservationScope.java
e8303ef30a703f39b939624d6dd4d5252a8fe90ab07bd442cb81a2d976625009  src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/ReservaJpaReaderArchitectureTest.java
f99a82c1af35ba711cfe3ebdbeef8c0cdf6d10a5c1917eb1fa22f2cddf6a7890  src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/ReservaJpaReaderPostgreSqlTest.java
a7f4db9c51ee7306e9a284f4c6a69eb9484cf2dd3cb30fb40402204770a7e9cd  src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/ReservaJpaReaderRuntimeIsolationTest.java
72407f0db1c01490463208ba08791fbebf48925518b4aaf95438d051c48d12e8  src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/ReservaJpaReaderTransactionTest.java
1a63df3f56cf3e4c62122d972d43c7564c1e71f6e79a7928d7fd88e32ae9cdfa  src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/ReservaProjectionMapperTest.java
ba0fc95e89b85a00dd679037c8a726ae534ad506753227245ad8c00c3d040369  src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/testinfra/F2ePostgresTestConfiguration.java
532502268c445a38beef4339f6fa9eced0770a0af665a29c2beada7f43f434c1  src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/testinfra/F2eSelectOnlyRole.java
db4757b6cb7b52087ccfa2da70505844dd935a78d016e34fdbeb64a5ca96ca25  src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/testinfra/F2eSliceChecksum.java
512da4beb1aa9c845aaab02097b2ab798a5fa9f7b18b0d45cb9374ad8408be94  src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/testinfra/F2eStatementPolicyInspector.java
1fe20bccf194dfe6b339aa8d8a6985d250f8e4df21b1119be57e1048b72630ef  src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/testinfra/ReaderTransactionTestHarness.java
```

## 9. Resolución competente histórica posterior al corte del documenter anterior

Run `run_b0efaa8ebe42`. Esta sección es metadata terminal persistida por el coordinador,
no autoaudit del documenter ni aceptación/publicación. Las secciones1–8 conservan el corte
documental inspeccionado; sus PENDING son históricos y esta resolución fue el estado de ese corte.
El FAIL y OPEN siguientes se preservan literalmente; el residual posterior distinto es sección10.

Audit fresh independiente `task_7a60734d59d2 / ctx_848f387aa672`, unique worker_done
`msg_902b5e1687fb` en `2026-09-16T17:27:18Z`, outcome failed, settled y released;
archive transcript captured. Report físico
`/tmp/feelingpilates-f2e-tech-recovery.uj4KrG/AUDIT-AUTHORITY-REPORT.md`, SHA-256
`d8ff8c93361b6605ddbee1911b93030cb603c5381d808d6bbae6a70fda48008b`.
Verdict: `FAIL — ORIGINAL F2E-R1-TECH-01 REAL/NATIVE HOSTILE JDBC METADATA ACCEPTANCE
EVIDENCE UNPROVEN; F2E-R1-TECH-08 OPEN FOR THE OVERLAPPING REQUIREMENT`.
Sus13 respuestas autentican las definiciones y soportan todos los CLOSED en su scope:
TECH02–06 CLOSED; TECH07 CLOSED FOR_CURRENT_R1_HOST_INVARIANT ONLY.
TECH01/08 permanecen OPEN porque ORIGINAL nativa autorizada y ENTREGADA proxy-hostil
no prueban la condición histórica S2 de metadata real independientemente hostil primera/posterior.
No se infiere un nuevo defecto de código, se invalidan GAP1–5 o se reabre implementación.

Gate-only Task `task_cf9c20914c19`, gate `gate_ad9e29b8d8e8`, options PASS/FAIL:
initial pending `2026-09-16T17:27:43Z`; resolved `FAIL` en `2026-09-16T17:28:03Z`;
Task failed. Semántica: recuperación ORIGINAL completa pero evidencia original de aceptación
TECH01/08 insuficiente; `R1 MATERIALIZED / NOT_ACCEPTED / NOT_PUBLISHED`.
NO significa publicación/cutover/reemplazo de TurnoInstructor ni autorización R2–R6.
El técnico `run_6d0dfb237a61 / gate_01a63c36e518` sigue PASS, con GAP1–5 CLOSED,
59 targeted/69 suites649 full/7 host fresh originales y P2-EVIDENCE-01 NON_BLOCKING intactos.
No se fabrican arrays JSON ni XML targeted sobrescritos/unretained.

Snapshot documental efectivamente auditado: ESTADO SHA
`ba96e00deacc04df2625da78d5ead598b573158a4d078ca57a291b03d3ca588b`, review SHA
`cf92b2e60f29c1506b4cd9187ffb56ceed703f31007806bf5ecf75d6549b52e3`.
Los bytes de esta sección y la resolución espejo en ESTADO son metadata posterior de resultados
competentes, verificada por el coordinador; no se dice que el auditor inspeccionó esos bytes.
Mapping literal, evidencia técnica, P2 y exact21 manifest no se alteran después del audit.
Integridad física al gate: misma branch/HEAD, staging EMPTY, sole tracked ESTADO,
21 source+review existente=22 untracked, no extras, diff check PASS; path SHA f400a0602f95e318845da670bee4f819f057842adf8a60506564d5bd75e41d14
y content SHA e2b64abd6aba8a050df6184f6c5440d87db83a67182fb43e622f48cf96f4f3ce.
No code/test/config/SQL/Git mutation, test rerun, commit, push, publicación o legacy execution.
Productive TurnoInstructor LEGACY_VIVO/PRODUCTIVO, dark launch, cutoverfalseNOT_AUTHORIZED,
R2–R6NOT_AUTHORIZED permanecen. Siguiente paso: autorización separada de resolución específica
del requisito original de metadata, con review independiente y gate competente; no repair implícito.

## 10. Corte documental auditado — residual nativo TECH01/08 cerrado; aceptación entonces pendiente

Esta sección conserva la entrega previa al audit/gate, no el estado terminal vigente.
Sección11 registra ACCEPTED / NOT_PUBLISHED sólo por los resultados competentes posteriores.

Run documental `run_6b83c0a8f3a4 / task_41443902b374 / ctx_77c671acdfb6`.
Resultado de este rol: AUTHORITY_UPDATE_MATERIALIZED_PENDING_ACCEPTANCE_AUDIT_AND_GATE.
No es audit ni gate: R1 NOT_ACCEPTED / PENDING_INDEPENDENT_ACCEPTANCE_AUDIT_AND_GATE,
NOT_PUBLISHED / NOT_AUTHORIZED. Ningún final PASS de aceptación o ID futuro se inventa.

### 10.1 Fuente independiente y disposición exacta

Residual Run real `run_8b529b21e8ad`:
analista `task_60e34917f8f4 / ctx_90cb3b888ddb / msg_8245f0b9d215`,
EVIDENCE_ONLY_CLOSURE_FEASIBLE (plan, no ejecución ni PASS);
auditor fresh `task_ecd83cdad97f / ctx_ef96b159acfc / msg_2b461bad6df6`,
`PASS — TECH-01/08 NATIVE JDBC METADATA RESIDUAL VERIFIED`;
gate-only `task_fd4e5d9c44d9 / gate_4ab844c58bbb`,
pending 2026-09-16T17:50:29Z, PASS 17:50:43Z,
`PASS — TECH-01/08 RESIDUAL CLOSED / READY_FOR_ACCEPTANCE_AUTHORITY_UPDATE`.
Estos resultados son residuales técnicos; no aceptación/publicación R1.
Informe independiente AUDIT-NATIVE-REPORT.md SHA
`4782b8988dddbef65f7d160025666d9636adaa54bbc32f6e14bdb7757dbaf573`.
Se leen originales y dictamen; los summaries del coordinador sólo navegan.

TECH01 CLOSED exclusivamente por prueba nativa de URL original hostil, configuración autorizada
y rechazo causal de metadata first/later en candidate e2 congelado.
TECH08 CLOSED exclusivamente por ese residual metadata compartido; los otros casos obligatorios,
participating graph y physical delegate switch previamente corroborados se preservan.
Los bloques originales S0/S1/S2 y refinamientos de sección3 no se alteran, renumeran ni debilitan.
La matriz S1 existente conserva proxy simulation sobre participantes reales; nunca se declara nativa.
El auditor adjudicó literalmente la combinación matriz previa + query nativa en el residual S2:

~~~text
My adjudication: the existing runtime matrix covers S1's listed URL forms on actual participating
connections, but its hostile value generation remains simulation. The new evidence supplies
the missing independently native hostile query in BOTH S2 observation positions. S1 does not
literally demand that every malformed lexical form be independently generated by pgjdbc;
S2 sharpens the native-origin/causality/first-later gap, rather than establishing a universal
native malformed-connect-URL matrix. Thus the combined matrix plus native first/later query
satisfies the exact remaining condition. This does not equate proxy origin with native origin,
retroclassify fragment/user-info/percent cases as native, or waive a listed rejection form.
A query is itself prohibited by the closed contract, irrespective of whether its supported
property is benign. No security-sensitive property or endpoint change is required to prove this.

~~~

El contrato cerrado sigue rechazando query/fragment/user-info/percent/aliases.
No se inventa una matriz universal de conexiones malformed nativas, ni se dispensa un rechazo.
La propiedad soportada es driver-local; basta una query prohibida para acreditar el requisito.

### 10.2 Ejecución original, origen y causalidad

Raíz física N: `/tmp/feelingpilates-f2e-native-preflight.kQO3WB`.
Compilación original: `node /tmp/feelingpilates-f2e-native-preflight.kQO3WB/validate-native.mjs compile`,
javac -proc:none -implicit:none -g, exact21 fuentes existentes, salida externa classes,
2026-09-16T17:43:24.238Z–17:43:25.677Z exit0; 65 clases selladas y verificadas por auditor.
Classes externas FIRST en classpath antes de target/test-classes y target/classes.
Dependencias/no-candidate classes/resources retenidos son inputs, no rebuild fresh universal.
Ejecución real: `node /tmp/feelingpilates-f2e-native-preflight.kQO3WB/validate-native.mjs run2`;
argv original `jshell -R-Dapi.version=1.44 --class-path <classpath completo del receipt sellado> -`,
cwd checkout de sección1, UTC 2026-09-16T17:44:30.596Z–17:44:35.632Z,
exit0 / 4 probes / 2 negativas nativas / 2 positivas canónicas. No JUnit ni suite completa;
este DOCUMENTER no ejecutó compilación, Maven, JDBC, Docker, SQL o tests.

Driver42.7.11 / PostgreSQL16.14. Cadena nativa inspeccionada por auditor:
BaseDataSource.getConnection/getUrl → Driver.makeConnection → PgConnection.creatingURL →
PgConnection.getURL → PgDatabaseMetaData.getURL. Binary SHA
1981b31d3993c58702783c1cddf10a34e48c1f413d70ff1cb6def0a143484647;
sources SHA 5156b9a1076e69ede16266ceb0c20a7ecd0f3f7d5a5a388480333ec8339ee198.
`disableColumnSanitiser=true` es PGProperty nativa soportada, no modificación de endpoint,
credencial, permisos, SQL o autoridad. `mutacionMetadatos == null` se afirma antes de ambas
negativas: el wrapper existente sólo registra/reenvía ORIGINAL==ENTREGADA nativa.
CONFIGURADA==DESCRIPTOR permanece `jdbc:postgresql://localhost:54344/test`;
ORIGINAL==ENTREGADA es `jdbc:postgresql://localhost:54344/test?disableColumnSanitiser=true`.

Mismo DS bean/EMF/TM/sharedEM y Session/Connection realmente participantes.
Snapshot/restore público initializeFrom conserva estado nativo en objeto clean nunca registrado
ni conectado; no sustituye observer/connection/descriptor. Restauración en finally.
Primera significa primera invocación del harness fresh, no primera metadata del bootstrap Hibernate.
Cada negativa: +1 metadata lookup, +1 conexión normal REQUIRES_NEW; contadores acumulados visibles.
validarRecurso:380 pasa CONFIGURADA y :381 rechaza ORIGINAL; stack real exigido.
Rechazo antes de capture/probes/identidades aceptadas, cero reader SQL preparado/aceptado y output,
completion real con INVOCATION/BOUNDARY/ATTEMPT CONSUMED_ABORTED y RUN OPEN_AFTER_ABORT.
No se afirma ausencia de keys transitorias reservadas ni se ejecutó mutation experiment del código.
Extracción postcallback sólo lee strings/counters retenidos; conexión postcallback sólo reference.

Secuencia literal de originales run2-native.log/result, sin modificar el orden:

~~~text
DRIVER file:/Users/jesusaldaircruzortiz/.m2/repository/org/postgresql/postgresql/42.7.11/postgresql-42.7.11.jar
HARNESS file:/private/tmp/feelingpilates-f2e-native-preflight.kQO3WB/classes/
TOPOLOGY_NATIVE image=postgres:16-alpine container=a12f3676457c97f0f8cb0f3f591002d3a96d93fa3c87f5df7454e99a04fc8ee9 host=localhost port=54344 driver=42.7.11 provider=org.postgresql.Driver bootstrapComplete=true
CHECKSUM_NATIVE_BEFORE Resultado[filas=2, hashTabla=0a98a347d56b645ef8856dbad59056ee13b2d3b110b026219338c7ca31e45ec1, hashSlice=72c8a5a30980264b1fc9aa3b3414ac4372661908e1b40b86cdb3a037d8b0ba2d]
NATIVE_NEGATIVE first {ORIGINAL=jdbc:postgresql://localhost:54344/test?disableColumnSanitiser=true, OBSERVACIONES=2, DESCRIPTOR=jdbc:postgresql://localhost:54344/test, CONEXIONES_ENTREGADAS=3, ENTREGADA=jdbc:postgresql://localhost:54344/test?disableColumnSanitiser=true, CONFIGURADA=jdbc:postgresql://localhost:54344/test} {INVOCATION=CONSUMED_ABORTED, BOUNDARY=CONSUMED_ABORTED, RUN=OPEN_AFTER_ABORT, ATTEMPT=CONSUMED_ABORTED} metadataCompareLine=381 noOutput=true noNewReaderSQL=true
NATIVE_POSITIVE authorized-between {ORIGINAL=jdbc:postgresql://localhost:54344/test, OBSERVACIONES=3, DESCRIPTOR=jdbc:postgresql://localhost:54344/test, CONEXIONES_ENTREGADAS=4, ENTREGADA=jdbc:postgresql://localhost:54344/test, CONFIGURADA=jdbc:postgresql://localhost:54344/test} threeStatements=true
NATIVE_NEGATIVE later {ORIGINAL=jdbc:postgresql://localhost:54344/test?disableColumnSanitiser=true, OBSERVACIONES=4, DESCRIPTOR=jdbc:postgresql://localhost:54344/test, CONEXIONES_ENTREGADAS=5, ENTREGADA=jdbc:postgresql://localhost:54344/test?disableColumnSanitiser=true, CONFIGURADA=jdbc:postgresql://localhost:54344/test} {INVOCATION=CONSUMED_ABORTED, BOUNDARY=CONSUMED_ABORTED, RUN=OPEN_AFTER_ABORT, ATTEMPT=CONSUMED_ABORTED} metadataCompareLine=381 noOutput=true noNewReaderSQL=true
NATIVE_POSITIVE restored-final {ORIGINAL=jdbc:postgresql://localhost:54344/test, OBSERVACIONES=5, DESCRIPTOR=jdbc:postgresql://localhost:54344/test, CONEXIONES_ENTREGADAS=6, ENTREGADA=jdbc:postgresql://localhost:54344/test, CONFIGURADA=jdbc:postgresql://localhost:54344/test} threeStatements=true
CHECKSUM_NATIVE_AFTER Resultado[filas=2, hashTabla=0a98a347d56b645ef8856dbad59056ee13b2d3b110b026219338c7ca31e45ec1, hashSlice=72c8a5a30980264b1fc9aa3b3414ac4372661908e1b40b86cdb3a037d8b0ba2d] equality=true measuredIds=2 successfulMutationAttempts=0
NATIVE_RESIDUAL_ASSERTIONS_COMPLETE
~~~

Ambas positivas retornan una reserva; exactamente tres SELECT en orden:
`4a669a2f628e12468e0d532889e0bcafd38c09a5aadfb27f2f20f56159c1671e`,
`9963ea856cdf9bfb3e9c440b1c3a6c062fd375a906f465cbe7a7ac88878716c7`,
`dfa84c5db84f44c41adb82b0a58a2f080fc05a0612df15ababbd5dc064192a5b`.
Checksum BEFORE==AFTER, scope RESERVA_UNO/RESERVA_DOS, filas2 y hashes literales arriba:
0 mutation attempts en ventana medida, sin mutación persistente observada.
Las 50 migraciones EXISTENTES/currentV47/validate50, fixtures y grants privilegiados normales
ocurrieron ANTES de la medición; contienen DDL/DML y no se declaran zero global DB writes.
Checksum observer-plane SELECT no sustituye la metadata reader. Fixture propia detenida según
coordinador; este documenter no ejecutó Docker ni verificó liveness por ejecución.

### 10.3 Límites operacionales e historia conservados

Launcher inicial N/native.result.json/log: UTC17:43:44.757Z–17:43:45.664Z exit1/0probes,
0positivas/0negativas, NoClassDefFoundError BaseDataSource antes de fixture/prueba.
No se oculta ni cuenta como evidencia nativa; su mutation field genérico es no aplicable.
Recovery externo classloader/cwd/VM, sin alterar candidato o assertions nativas.
N/run2-run.before.json conserva wrapperCommand genérico terminado en run, mismatch documental
de bookkeeping explícito: no es receipt exacto del wrapper. Expanded argv y receipt final original
run2 correcto vinculan la ejecución; no se repara el original.

`run_b0efaa8ebe42 / gate_ad9e29b8d8e8 FAIL` de sección9 es verdadero histórico:
TECH01/08 OPEN por prueba nativa entonces ausente. La ejecución y audit posteriores distintos
resuelven ese residual técnico; ningún FAIL anterior se convierte retrospectivamente en PASS.
Main `run_6d0dfb237a61 / gate_01a63c36e518 PASS`, audit
`task_8a6b068015dd / ctx_ad561af5fcce` PASS — FIVE ACCEPTANCE GAPS CLOSED AND TECHNICAL
VALIDATION VERIFIED, 59targeted/69suites649full/host7 y GAP1–5 CLOSED permanecen evidencia
PRIOR fresh, no ejecutada en este Run. TECH02–06 mapping byte-preservado; TECH07
CLOSED FOR_CURRENT_R1_HOST_INVARIANT ONLY; named HostValidator/Autopilot OLD_PROCESS_ONLY,
sin aprobación retroactiva ni ejecución legacy.

P2-EVIDENCE-01 NON_BLOCKING conserva arrays JSON incompletos, deniedINSERT omitido en arrays,
targetedXML overwritten/unretained y límites de logs/retained full/hostXML; no repair fabricado.
Transaction XML28 SHA7bf17aaf2e13facd2d49b1e77fb0c16d3695a25c660a37467a610dda90916d4c
es prior fresh-full; casos proxy381/403/431 conservan origen simulado, no XML native ni targeted.
No nueva fuente, configuración, migración, staging, commit, push/fetch/pull/branch/history/worktree.
Diseño CLOSED/PUBLISHED, handoff APPROVED/PUBLISHED/ACTIVE, R1 MATERIALIZED /
IMMUTABLE_VALIDATED_CANDIDATE / NOT_ACCEPTED pendiente; publicación NOT_PUBLISHED/NOT_AUTHORIZED.
TurnoInstructor LEGACY_VIVO/PRODUCTIVO, dark launch PRESERVED, cutover false/NOT_AUTHORIZED,
R2–R6NOT_AUTHORIZED y Payments/Notifications OUT_OF_SCOPE permanecen.
Siguiente paso único: auditor independiente de aceptación/autoridad y gate competente
sobre el binding de sección1 y estos bytes; este rol no se autoaudita ni crea gate.

### 10.4 Sellos durables de fuentes originales

Los valores, dictamen y evidencia literal arriba permanecen en este review existente.
N es custodia operacional local sin garantía de retención permanente; sus enlaces son navegación.

| Original bajo N/ | SHA-256 recalculado físicamente |
| --- | --- |
| RESIDUAL-ANALYSIS.md | b0ff7be52d8b5955b5ec32c59b64b4705d43cbd9eabd3efd02de8faaf44f192a |
| AUDIT-NATIVE-REPORT.md | 4782b8988dddbef65f7d160025666d9636adaa54bbc32f6e14bdb7757dbaf573 |
| COORDINATOR-FINAL-RESULT.md | 13d253f5a5e5aace1f62a87288663d508d2211d4fc7e12c749b96252e980ecf2 |
| run2-native.result.json | 5313c1969b6c41a39568cfb6e2fcb93513c89b8f3b66bfa5cf55742b1d7af10f |
| run2-native.log | ef7ee27ae58f79f6812e9999da254fda230358093cbd5ccba5baee9822617b8f |
| run2-runtime-input.txt | e6079c406d75e64911162faa7d7f5ffb515932ee6b5acb47f7718ec428ca2760 |
| compile.result.json | 7f2c2cd5543900bdff914dc34b277ddb235865d69f5297f7ab6688ea93042e85 |
| class-manifest.sha256 | 6e7523e13cb1fdd94c8288b09ecb0a7f4434ec52b880c7261a0bbb3ef493caa1 |
| resource-manifest.sha256 | 0f57f5756506f531e8af80f73607561a3d9a1a24562221432b80a88c54b67080 |
| native.result.json | 4084478616f12eab5cd109453830546b336591a68900cf999e024523b540aab5 |
| native.log | 152de5230a5ac19dab4ffb225123990ea53b214e14f761daf8c182bbd97389ad |
| run2-run.before.json | 86626a1aa4b6e64af11840ab00165395b593c08a5ef20a43013e1a0f93c3d742 |

### 10.5 Input original run2 retenido literalmente

El bloque siguiente conserva stdin externo completo sellado; es evidencia previamente ejecutada,
no instrucción para ejecutar en este Run documental ni creación de Java/test/config de repositorio.

~~~java
import java.util.*;
import java.time.*;
import java.lang.reflect.*;
import java.util.concurrent.atomic.*;
import org.postgresql.*;
import org.postgresql.ds.*;
import org.springframework.aop.support.AopUtils;
import org.springframework.test.util.AopTestUtils;
import com.feelingpilates.transicion.programacion.adapter.jpa.testinfra.*;
import com.feelingpilates.transicion.programacion.read.*;
class NativeResidualProbe {
  static void require(boolean value, String reason) {
    if (!value) throw new AssertionError(reason);
  }
  static Object read(Object target, String name, Class<?>[] types, Object... args) throws Exception {
    Method m = target.getClass().getDeclaredMethod(name, types);
    m.setAccessible(true);
    return m.invoke(target, args);
  }
  static long count(Object target, String name) throws Exception {
    return (Long) read(target, name, new Class<?>[0]);
  }
  static Map<?,?> evidence(Object target) throws Exception {
    return (Map<?,?>) read(target, "evidenciaUrlMetadataPrueba", new Class<?>[0]);
  }
  static ReaderTransactionTestHarness.SemillaLectura seed(String name) {
    return new ReaderTransactionTestHarness.SemillaLectura("native-"+name,"attempt-"+name,
      "boundary-"+name,"invocation-"+name,"F2D-RULE-CATALOG/V1",
      ZoneId.of("America/Mexico_City"), ReadSnapshotContext.SnapshotClaim.SINGLE_READER_TEST);
  }
  static void negative(String name, ReaderTransactionTestHarness proxy, Object target,
      PGSimpleDataSource ds, PGSimpleDataSource clean, Object inspector, String canonical) throws Exception {
    Field f = ds.getClass().getDeclaredField("mutacionMetadatos");
    f.setAccessible(true);
    require(((AtomicReference<?>)f.get(ds)).get()==null, "Synthetic metadata mutation present");
    ds.setDisableColumnSanitiser(true);
    String actualNativeUrl = canonical+"?disableColumnSanitiser=true";
    require(ds.getURL().equals(actualNativeUrl), "Driver datasource URL serialization drift");
    require(read(target,"urlJdbcLectorPrueba",new Class<?>[0]).equals(canonical), "Configured path changed");
    long obs=count(ds,"observacionesMetadataDesdeMutacion"), delivered=count(ds,"conexionesEntregadas");
    long prepared=count(ds,"preparacionesJdbc"), accepted=count(inspector,"statementsAceptadosPrueba");
    var s=seed(name);
    IllegalStateException rejected=null;
    Object output=null;
    try { output=proxy.inSingleStatementReadOnly(s,Set.of(F2ePostgresTestConfiguration.RESERVA_UNO)); }
    catch (IllegalStateException e) { rejected=e; }
    require(output==null && rejected!=null, "Hostile native resource accepted");
    require(rejected.getMessage().equals("F2E reader resource provenance not proven"), "Wrong failure");
    require(Arrays.stream(rejected.getStackTrace()).anyMatch(e ->
      e.getClassName().equals(ReaderTransactionTestHarness.class.getName()) &&
      e.getMethodName().equals("validarRecurso") && e.getLineNumber()==381), "Failure not metadata comparison");
    Map<?,?> ev=evidence(target);
    require(canonical.equals(ev.get("DESCRIPTOR")) && canonical.equals(ev.get("CONFIGURADA")), "Authority changed");
    require(actualNativeUrl.equals(ev.get("ORIGINAL")) && actualNativeUrl.equals(ev.get("ENTREGADA")), "Native URL not proven");
    require(count(ds,"observacionesMetadataDesdeMutacion")==obs+1, "Wrong invocation URL observation count");
    require(count(ds,"conexionesEntregadas")==delivered+1, "Wrong participating delivery count");
    require(count(ds,"preparacionesJdbc")==prepared && count(inspector,"statementsAceptadosPrueba")==accepted, "Reader SQL escaped");
    Map<?,?> states=(Map<?,?>)read(target,"estadosPrueba",
      new Class<?>[]{ReaderTransactionTestHarness.SemillaLectura.class},s);
    require("CONSUMED_ABORTED".equals(states.get("ATTEMPT")), "Invocation abort not completed");
    System.out.println("NATIVE_NEGATIVE "+name+" "+ev+" "+states+" metadataCompareLine=381 noOutput=true noNewReaderSQL=true");
    ds.initializeFrom(clean);
    require(ds.getURL().equals(canonical), "Native property not removed");
  }
  static void positive(String name, ReaderTransactionTestHarness proxy, Object target,
      PGSimpleDataSource ds, Object inspector, String canonical) throws Exception {
    long before=count(inspector,"statementsAceptadosPrueba");
    require(proxy.inSingleStatementReadOnly(seed(name),Set.of(F2ePostgresTestConfiguration.RESERVA_UNO)).size()==1,"Canonical invocation failed");
    Map<?,?> ev=evidence(target);
    require(canonical.equals(ds.getURL()) && canonical.equals(ev.get("DESCRIPTOR")) &&
      canonical.equals(ev.get("CONFIGURADA")) && canonical.equals(ev.get("ORIGINAL")) &&
      canonical.equals(ev.get("ENTREGADA")), "Canonical resource drift");
    require(count(inspector,"statementsAceptadosPrueba")==before+3,"Canonical three-statement manifest count failed");
    System.out.println("NATIVE_POSITIVE "+name+" "+ev+" threeStatements=true");
  }
  static void run() throws Exception {
    Thread.currentThread().setContextClassLoader(NativeResidualProbe.class.getClassLoader());
    try (var context=F2ePostgresTestConfiguration.abrirContextoRecursoHostilPrueba("NINGUNO")) {
      var proxy=context.getBean("readerTransactionTestHarness",ReaderTransactionTestHarness.class);
      require(AopUtils.isAopProxy(proxy) && AopUtils.isAopProxy(context.getBean("reservaJpaReader")), "Missing actual transaction proxies");
      Object target=AopTestUtils.getUltimateTargetObject(proxy);
      PGSimpleDataSource ds=(PGSimpleDataSource)context.getBean("f2eReaderDataSource");
      require(ds.getClass().getName().equals(F2eSelectOnlyRole.class.getName()+"$DataSourceLectorContabilizado"), "Different datasource provider");
      Object inspector=context.getBean("f2eStatementPolicyInspector");
      String canonical=(String)read(target,"urlJdbcLectorPrueba",new Class<?>[0]);
      require(ds.getURL().equals(canonical), "Initial native datasource is not canonical");
      PGSimpleDataSource clean=new PGSimpleDataSource();
      clean.initializeFrom(ds);
      require(clean.getURL().equals(canonical), "Provider state snapshot is not canonical");
      System.out.println("DRIVER "+org.postgresql.Driver.class.getProtectionDomain().getCodeSource().getLocation());
      System.out.println("HARNESS "+ReaderTransactionTestHarness.class.getProtectionDomain().getCodeSource().getLocation());
      var container=context.getBean(org.testcontainers.containers.PostgreSQLContainer.class);
      System.out.println("TOPOLOGY_NATIVE image="+container.getDockerImageName()+" container="+container.getContainerId()+" host="+container.getHost()+" port="+container.getMappedPort(5432)+" driver="+org.postgresql.Driver.class.getPackage().getImplementationVersion()+" provider="+java.sql.DriverManager.getDriver(ds.getURL()).getClass().getName()+" bootstrapComplete=true");
      var privileged=(javax.sql.DataSource)context.getBean("f2ePrivilegedDataSource");
      var measuredIds=Set.of(F2ePostgresTestConfiguration.RESERVA_UNO,F2ePostgresTestConfiguration.RESERVA_DOS);
      var checksumBefore=F2eSliceChecksum.calcularPorIdentidades(privileged,measuredIds);
      System.out.println("CHECKSUM_NATIVE_BEFORE "+checksumBefore);
      try {
        negative("first",proxy,target,ds,clean,inspector,canonical);
        positive("authorized-between",proxy,target,ds,inspector,canonical);
        negative("later",proxy,target,ds,clean,inspector,canonical);
        positive("restored-final",proxy,target,ds,inspector,canonical);
        var checksumAfter=F2eSliceChecksum.calcularPorIdentidades(privileged,measuredIds);
        require(checksumBefore.equals(checksumAfter),"Persistent scoped database mutation");
        require(context.getBean("f2eReaderDataSource")==ds,"Participating datasource identity changed");
        System.out.println("CHECKSUM_NATIVE_AFTER "+checksumAfter+" equality=true measuredIds=2 successfulMutationAttempts=0");
      } finally { ds.initializeFrom(clean); }
    }
    System.out.println("NATIVE_RESIDUAL_ASSERTIONS_COMPLETE");
  }
}
try { NativeResidualProbe.run(); System.exit(0); } catch (Throwable e) { System.err.println("NATIVE_RESIDUAL_ASSERTION_FAILURE "+e.getClass().getName()+": "+e.getMessage()); System.exit(1); }
~~~

## 11. Resolución terminal competente — R1 ACCEPTED / NOT_PUBLISHED


Run actual `run_6b83c0a8f3a4`, coordinator PRODUCT_DELIVERY_COORDINATOR /
FINAL_ACCEPTANCE_GATE_COORDINATOR. El documenter `task_41443902b374 / ctx_77c671acdfb6`
entregó sólo AUTHORITY_UPDATE_MATERIALIZED_PENDING_ACCEPTANCE_AUDIT_AND_GATE;
unique worker_done `msg_8933fa1c36e9`, settled/released, transcript captured.
Aceptación no se atribuye al documenter, al gate residual ni a tests verdes por sí solos.

Audit NUEVO, fresh, independiente, READ_ONLY:
`task_5f12bc24768c / ctx_f99011b64799 / msg_17995a84cc7d`.
Unique worker_done succeeded a `2026-09-16T18:10:36Z`; Task/Dispatch settled,
terminal released, transcript captured y delivery completo acknowledged.
Verdicto: `PASS — F2E R1 AUTHORITY PROVENANCE COMPLETE AND ACCEPTANCE VERIFIED`.
Report físico `/tmp/feelingpilates-f2e-final-authority.DVYXaM/AUDIT-ACCEPTANCE-REPORT.md`,
SHA-256 `edc5b95b02dd1cb38cb3464f474302f8f8c410052ffc8372b69c4baeae619ac6`.
P0=0 / P1=0 / P2=1 NON_BLOCKING existente; ningún residual bloqueante.

Las quince respuestas independientes fueron YES: provenance auténtica de ocho TECH;
TECH01 cumple S0/S1/S2 por metadata nativa first/later sin source change; TECH08 sólo overlap;
TECH02–06 soportados; TECH07 estrechamente actual; GAP1–5 cerrados sobre exact snapshot;
todas las pruebas bind a content SHA exacto; FAIL históricos verdaderos; P2 transparente y
no bloqueante; cero implementation delta; TurnoInstructor productivo; dark launch preservado;
cutover/R2–R6 no autorizados; aceptación soportable separadamente de publicación.
El auditor verificó cero diferencias en sus 460 archivos de snapshot y 35 evidencias originales.

Gate-only Task coordinator-owned `task_59b41e1a65f6`, gate `gate_b263068ee65c`,
opciones PASS/FAIL: inicialmente `pending` a `2026-09-16T18:11:17Z`,
resolución real `PASS` a `2026-09-16T18:11:25Z`.
Semántica EXACTA:
`PASS — F2E R1 ACCEPTED / NOT_PUBLISHED / READY_FOR_PUBLICATION_PREFLIGHT`.
Se contrastó nuevamente integridad física antes de crear/resolver el gate; heartbeat o borrador
nunca sustituyeron el único worker_done terminal.

### Identidad exacta de aceptación

| Binding | Valor |
| --- | --- |
| Branch | `operacion/excepciones-horario-fecha` |
| Baseline HEAD | `a0ec85818b771d4ac924b427fa1e90244ea9fe8e` |
| Exact21 candidate path-set SHA-256 | `f400a0602f95e318845da670bee4f819f057842adf8a60506564d5bd75e41d14` |
| Candidate-content manifest SHA-256 | `e2b64abd6aba8a050df6184f6c5440d87db83a67182fb43e622f48cf96f4f3ce` |
| Design CLOSED / PUBLISHED SHA-256 | `6c72cba1f83fbc2bcf3b3219d8252d2410ec482e30ae85d30fd2eeb04e8883d8` |
| Sole handoff APPROVED / PUBLISHED / ACTIVE SHA-256 | `3fd71faca4d4c049ad5cb37b52bc6fd512509cf5b696bdc5c28d28cb966af8ef` |
| Main technical | `run_6d0dfb237a61 / gate_01a63c36e518 PASS` |
| Provenance recovery / previous acceptance | `run_b0efaa8ebe42 / gate_ad9e29b8d8e8 FAIL` histórico preservado |
| Native residual | `run_8b529b21e8ad / gate_4ab844c58bbb PASS` |
| Main independent technical audit | `task_8a6b068015dd / ctx_ad561af5fcce` PASS |
| Native independent technical audit | `task_ecd83cdad97f / ctx_ef96b159acfc` PASS; SHA `4782b8988dddbef65f7d160025666d9636adaa54bbc32f6e14bdb7757dbaf573` |
| Current independent acceptance audit | `task_5f12bc24768c / ctx_f99011b64799` PASS |
| Current acceptance gate | `task_59b41e1a65f6 / gate_b263068ee65c PASS` |

Los 18 prerrequisitos de aceptación están satisfechos: design/handoff válidos, exact21
congelado, GAP1–5 CLOSED, TECH01 CLOSED por prueba nativa, TECH02–06 CLOSED,
TECH07 CLOSED FOR_CURRENT_R1_HOST_INVARIANT ONLY, TECH08 CLOSED sólo por overlap,
ambos audits técnicos y sus gates PASS, P2 transparente/no bloqueante, fuente byte-idéntica.
El audit independiente actual y la integridad final fundamentan esta transición competente.
59 targeted / 69 suites y 649 full / host7 pertenecen a `run_6d0dfb237a61`;
native4 (2negativas/2positivas) pertenece a `run_8b529b21e8ad`.
Este Run documental no ejecutó tests, Maven, JDBC, SQL, compilación o containers.

### Bytes auditados y metadata terminal

El auditor inspeccionó ESTADO SHA
`692fc8b7d4271e04d5851db4c0bf38e66ca7500e067f71e2a473d2e6ce09553b`
y review SHA `0c6969f54fd3e27cc28caf8857683e0f613b4d0e8c74251a99639a7029a0840d`,
ambos con aceptación pendiente al corte. Copias byte-exactas externas:
`/tmp/feelingpilates-f2e-final-authority.DVYXaM/*.audited`.
Estos nuevos estados/IDs terminales sólo se persisten DESPUÉS del audit y gate reales PASS,
en los mismos dos documentos, como reconciliación terminal expresamente autorizada.
No se afirma que el auditor inspeccionó bytes futuros. El coordinador verifica el delta de
resultado y la identidad de los 21 archivos sin alterar definiciones S0/S1/S2, mapping,
input nativo, proofs, sellos originales o FAIL históricos; no autoaudita implementación.

Estado terminal: R1 `ACCEPTED / NOT_PUBLISHED`; candidate sigue
`MATERIALIZED / IMMUTABLE_VALIDATED_CANDIDATE`.
Publication `NOT_PUBLISHED / READY_FOR_PUBLICATION_PREFLIGHT`;
publication execution `NOT_AUTHORIZED_IN_THIS_RUN`.
TurnoInstructor `LEGACY_VIVO / PRODUCTIVO`, dark launch `PRESERVED / NOT_PRODUCTIVE`,
cutover `NOT_AUTHORIZED / false`, R2–R6 `NOT_AUTHORIZED`.
DATA_SOURCE_NOT_AVAILABLE / material data audit NOT_AUTHORIZED y Payments/Notifications
OUT_OF_SCOPE permanecen. Autopilot/FeelingPilatesOrchestrator/HostValidator siguen
OLD_PROCESS_ONLY; nada legado ejecutado, revivido o retroactivamente aprobado.
P2-EVIDENCE-01 `NON_BLOCKING`: arrays JSON incompletos y targeted XML overwritten/unretained,
logs/root counts y XML full/host retenidos corroborantes; ningún repair/fabricación.
Cero Java/test/config/SQL/migration mutation; staging EMPTY; ningún commit/push/publicación.
Sólo un PUBLICATION_PREFLIGHT futuro separado, con autorización y gate propios, es posible;
no publicación, activación productiva, cutover o R2–R6 en este Run.

## 12. Cierre de publicación materializado — R1 CLOSED / ACCEPTED / PUBLISHED

Run de cierre `run_fb92631a2300`, Task/Dispatch documental
`task_9a3a4512c780 / ctx_9dc3f5dc789f`; rol `F2E_R1_PUBLICATION_CLOSURE_DOCUMENTER`.
Resultado de entrega: `DOCUMENTATION_CLOSURE_MATERIALIZED_PENDING_INDEPENDENT_AUDIT`.
Este rol documenta la publicación ya probada; no es auditor, coordinator ni publisher.
Las secciones1–11 y el encabezado anterior son el preimage publicado histórico completo,
preservado como prefijo byte-exacto. Sus «actual», ACTIVE, OPEN, PENDING, NOT_ACCEPTED,
NOT_PUBLISHED y siguientes pasos describen sus cortes respectivos; no son autoridad operativa
vigente después de esta nueva unidad cronológica. No se alteran originales, citas ni mappings.

### 12.1 Cadena real posterior a la aceptación, sin reescribir historia

La sección11 conserva la aceptación competente de `run_6b83c0a8f3a4` y
`gate_b263068ee65c`: `ACCEPTED / NOT_PUBLISHED / READY_FOR_PUBLICATION_PREFLIGHT`.
Los estados anteriores `MATERIALIZED / NOT_ACCEPTED / NOT_PUBLISHED`, el authority FAIL
`run_3c3b68d0f06b / gate_f598ddc359bd`, el recovery/acceptance FAIL
`run_b0efaa8ebe42 / gate_ad9e29b8d8e8` y el residual nativo posterior
`run_8b529b21e8ad / gate_4ab844c58bbb PASS` permanecen verdaderos e intactos.
Ni los PASS posteriores ni la publicación convierten esos FAIL históricos en PASS.

| Etapa real | Identidad y resultado | Límite preservado |
| --- | --- | --- |
| Preflight original | `run_4a37e5400083 / gate_f48f00abaf3e` | Readiness y manifest originales HISTORICAL_ONLY; no reutilizados para bytes resealed |
| Primera publicación fallida | `run_683c285000f8` — `FAILED_AT_STAGED_DIFF_CHECK`, exit2 | 18 líneas con dos espacios finales cada una; cero commits/pushes; audits no despachados/gates no creados |
| Higiene y reseal | `run_89649c4139e8 / gate_629f95236c32 PASS` | Exactamente36 espacios eliminados, cero bytes añadidos, cero cambios no-space;1273 líneas/LF y semántica intactos |
| Equivalencia independiente | `task_c16274d5747b / ctx_44d647524ad4` — `PASS — F2E R1 ACCEPTANCE AUTHORITY EQUIVALENT AFTER WHITESPACE-ONLY CORRECTION` | Autoridad aceptada resealed; NOT_PUBLISHED, nuevo preflight requerido |
| Preflight resealed previo | `run_0e78daeb44b4 / gate_89bc92a8bae5 PASS` | Mismo snapshot; no reemplaza el nuevo audit/gate competente siguiente |
| Fresh preflight.2 | `run_b5c5d0cc61a0 / gate_cb4bc2d34c43 PASS` | `PASS — F2E R1 RESEALED_ACCEPTED_SNAPSHOT READY_TO_PUBLISH`; publicación separada |
| Ejecución exitosa | `run_8b913c526294` | Un commit exacto y un push normal nonforce sobre la branch exacta; ningún tag/otro ref |

Review original antes de higiene SHA-256
`cfd744c610e5f983527c6506e53a3a90402e74600625bacbec1150f9a89a4fe6`;
manifest original SHA-256
`c699ab84e194ad31bd1918cd5e7f9af1aa6078519cfbbc1eaca33dadf3c4c708`.
Ambos son HISTORICAL_ONLY; nunca se hacen pasar por el snapshot resealed publicado.
Las36 eliminaciones fueron sólo los sufijos de dos espacios en líneas557–562,565–570,573–578.
No se corrigieron hallazgos técnicos ni se debilitaron definiciones S0/S1/S2 o evidencia nativa.

### 12.2 Identidad exacta de la publicación competente

| Binding publicado | Valor |
| --- | --- |
| Branch exacta | `operacion/excepciones-horario-fecha` |
| Commit publicado / baseline físico de este cierre | `f5e0239d378119b9c1a5ec94e8f09db77cc4fe3c` |
| Único parent real | `a0ec85818b771d4ac924b427fa1e90244ea9fe8e` |
| Publication canonical path/status set | 23 paths:22 NEW /1 MODIFIED; exact21 + review nuevo + ESTADO modificado |
| Commit publication canonical manifest SHA-256 | `0c305f556c6753b130a53a18b2e510f49318f6d7f9fb39c669f5a2e1d5bd9b80` |
| Implementación exact21 sorted path LF SHA-256 | `f400a0602f95e318845da670bee4f819f057842adf8a60506564d5bd75e41d14` |
| Implementación exact21 content manifest SHA-256 | `e2b64abd6aba8a050df6184f6c5440d87db83a67182fb43e622f48cf96f4f3ce` |
| Diseño CLOSED / PUBLISHED SHA-256 | `6c72cba1f83fbc2bcf3b3219d8252d2410ec482e30ae85d30fd2eeb04e8883d8` |
| Handoff exacto publicado SHA-256 | `3fd71faca4d4c049ad5cb37b52bc6fd512509cf5b696bdc5c28d28cb966af8ef` |
| ESTADO publicado / preclosure SHA-256 | `94c477fca3d17113b00d2f3d35ca3f741b903b3e3fbd043ffd9f38af5d4d3237` |
| Review resealed publicado / preclosure SHA-256 | `c300e851a313b252fd1b12e32816540ff5939edd4dc44125fb6829033698f1e4` |

Manifest canónico publicado: UTF-8 TSV `path<TAB>status<TAB>sha256<TAB>classification<LF>`,
ordenado por path relativo, NEW para Git A y MODIFIED para M, con LF final.
Los dos SHA documentales de la tabla sellan los blobs históricos dentro de f5, no estos
nuevos bytes de cierre. Preimages inmutables: `git show f5e0239d378119b9c1a5ec94e8f09db77cc4fe3c:<path>`.
El manifest23 publicado permanece histórico e inmutable; este delta posterior sólo son dos docs.

Audit independiente de staging real `task_b93db209e206 / ctx_bdb22a3f225b`,
worker_done `msg_c71aadaf8785`, outcome succeeded:
`PASS — F2E R1 RESEALED STAGED ACCEPTED SNAPSHOT VERIFIED`.
Authorization gate real `task_4594a303de2c / gate_bd04ea00c795`, PASS a
`2026-09-16T20:14:43Z`:
`PASS — AUTHORIZED_TO_COMMIT_AND_PUSH_EXACT_RESEALED_F2E_R1_SNAPSHOT`.
El commit posterior tiene timestamp `2026-09-16T20:14:44+00:00`, parent único e identidad exacta.
Un solo push normal fast-forward, exit0, sin force, tags u otros refs:

```text
git push --no-follow-tags origin f5e0239d378119b9c1a5ec94e8f09db77cc4fe3c:refs/heads/operacion/excepciones-horario-fecha
```

Es receipt de una operación previa, no instrucción para ejecutarla de nuevo.
Sólo target local branch y origin-tracking branch pasaron del parent a f5; ningún otro ref cambió.
Audit NUEVO fresh e independiente de post-publicación
`task_a5c106ff3d5a / ctx_e16589253474`, worker_done `msg_359164d0ec0a`, outcome succeeded:
`PASS — F2E R1 RESEALED ACCEPTED SNAPSHOT PUBLISHED EXACTLY`,13 respuestas YES.
Completion gate real `task_4b0c8ac25cd0 / gate_65645a7533da`, creado pending a
`2026-09-16T20:20:13Z`, resuelto PASS a `2026-09-16T20:20:30Z`:
`PASS — F2E R1 RESEALED ACCEPTED SNAPSHOT PUBLISHED / READY_FOR_PUBLICATION_CLOSURE`.
La consulta read-only actual `gate-list` sobre ese Run/Task corroboró resolved/PASS real.
Ese gate prueba publicación y readiness para este cierre, sin aprobar los nuevos bytes documentales.

### 12.3 Fuentes físicas y sellos de custodia

Raíz P: `/tmp/feelingpilates-f2e-publication-execution2.fEAgdl`.
El documenter leyó los originales físicos siguientes y recalculó sus SHA-256.
Los enlaces externos son navegación/custodia operacional local, sin garantía de retención;
las identidades y resultados de publicación quedan persistidos aquí.

| Original bajo P/ | SHA-256 |
| --- | --- |
| FINAL-RESULT.json | `461988c268036e64c8313972e70d1758e637729eb4f69e5817ab0e1b58940716` |
| AUDIT-POST-PUBLICATION.md | `f79ab55e98062ef79779b404d893c97fa114047eb1e6bfeb3342871dd929b857` |
| COMMIT-PUBLICATION-MANIFEST.tsv | `0c305f556c6753b130a53a18b2e510f49318f6d7f9fb39c669f5a2e1d5bd9b80` |
| PUSH-RECEIPT.json | `8d9acc5c91d8156da3b36664d577f0622928e8d196560806af6000da8a187005` |
| COMPLETION-GATE.json | `fe5d87f07b4c9c4d4d8c6a24145d0888330a55a8226ee368b6b7c1ce93da1d0f` |

Raíz de entrada C: `/tmp/feelingpilates-f2e-publication-closure.HwP7MH`.
`PUBLISHED-STARTING-SNAPSHOT.json` SHA-256
`9770e5d1d7192edf0510a9177c4740c56c2ddfbb55ee2688716a16fda2bdf6b6` y
`LIVE-REMOTE-START.json` SHA-256
`6a001239ede6c7d7ce191b7ab1253cd4a7798b0ffee225ac89f0468c46a839f6`.
Entrada físicamente contrastada: HEAD/upstream/origin live iguales a f5, ahead/behind0/0,
staging EMPTY, working tree CLEAN, untracked0. Consulta independiente actual `ls-remote`
sólo sobre `refs/heads/operacion/excepciones-horario-fecha` corroboró f5, sin fetch ni mutación.
Hashes de ambos documentos antes del delta coinciden con los preimages Git y la tabla12.2;
exact21 permanece byte-idéntico al manifest publicado y al snapshot aceptado.

### 12.4 Estado vigente, separación de ejes y trabajo pendiente

```text
Design: CLOSED / PUBLISHED
Handoff: APPROVED / PUBLISHED / CONSUMED_BY_R1 / NOT_ACTIVE
CONSUMED IMPLEMENTATION HANDOFF: auditoria/handoffs/HANDOFF-F2E-R1-RESERVA-READER-JPA-READ-ONLY.md
ACTIVE HANDOFF: NINGUNO
Implementation: ACCEPTED / PUBLISHED / MATERIALIZED / IMMUTABLE_VALIDATED_CANDIDATE / NOT_REOPENED
R1 lifecycle: CLOSED
Technical validation / independent technical audit: PASS
GAP1-GAP5: CLOSED
TECH01-TECH06 / TECH08: CLOSED
TECH07: CLOSED FOR_CURRENT_R1_HOST_INVARIANT ONLY
Publication: PUBLISHED — f5e0239d378119b9c1a5ec94e8f09db77cc4fe3c
Publication closure documentation: MATERIALIZED / PENDING_INDEPENDENT_AUDIT
P2-EVIDENCE-01: NON_BLOCKING / PRESERVED
R1 runtime: DARK_LAUNCH / NOT_PRODUCTIVE
Dark launch: PRESERVED
TurnoInstructor: LEGACY_VIVO / PRODUCTIVO / PRODUCTIVE AUTHORITY UNCHANGED
Cutover: NOT_AUTHORIZED / false
R2-R6: NOT_AUTHORIZED
NEXT FUNCTIONAL PHASE: NINGUNA / NOT_AUTHORIZED
```

R1 CLOSED se materializa por autorización expresa sobre implementación aceptada y publicación
ya probadas; no depende de inventar un futuro commit de cierre. No equivale a closure audit/gate
PASS de `run_fb92631a2300` ni al estado terminal completo del workflow documental actual.
Precommit closure audit, commit authorization gate, publicación de los docs de cierre,
post-closure audit y final gate actuales: todos `PENDING / NOT_EXECUTED`.
No se inventan futuros IDs, resultados o SHA; no hace falta edición posterior para añadirlos.
Siguiente acción del workflow actual: audit documental independiente; las etapas posteriores
exigen sus resultados/gates competentes reales. No se infiere ni autoriza siguiente fase funcional.
La ruta/hash del handoff consumido sigue como autoridad exacta de implementación e historia;
ningún contenido histórico se reactiva como permiso operativo.

TECH01 conserva origen nativo/causalidad first/later independiente; TECH08 cierra sólo el overlap
exacto; TECH02–06 mappings intactos. TECH07 no aprueba retrospectivamente el plan antiguo.
Main technical `run_6d0dfb237a61`:59 targeted,69 suites/649 full,host7;
native `run_8b529b21e8ad`:4 probes (2 negativas/2 positivas). Son ejecuciones históricas,
ninguna nueva de este Run. P2 conserva arrays JSON incompletos y omitted denied-INSERT,
targeted XML overwritten/unretained, roots/logs originales y XML full/host retenidos corroborantes;
no originals repair ni casos/logs/XML fabricados, sin reapertura de GAP/aceptación.
Data source `DATA_SOURCE_NOT_AVAILABLE`; data audit material, migration y fence
`DEFERRED / NOT_AUTHORIZED`; D08 DEFERRED, crosswalk/resolver/selection NOT_AUTHORIZED.
Payments/Notifications/Capacity/Mobile OUT_OF_SCOPE. Autopilot/FeelingPilatesOrchestrator/
HostValidator `OLD_PROCESS_ONLY`, sin revival ni ejecución por este rol.
Este documenter sólo modifica ESTADO y añade esta sección al review existente: ningún
Java/test/config/SQL/migration, tests/Maven/JDBC/Docker, staging/commit/push/fetch/ref/config
mutation o childworker. Los controles before/after son verificación de entrega, no self-audit.
