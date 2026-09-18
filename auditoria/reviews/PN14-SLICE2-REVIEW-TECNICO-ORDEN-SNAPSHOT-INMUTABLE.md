# FeelingPilates — PN14 Slice2 — review técnico AJENO Orden + snapshot inmutable

Tipo: AJENO_EVIDENCE_ONLY / EVIDENCE_ONLY / NOT_SELF_AUTHORIZING /
NOT_IMPLEMENTATION_AUTHORITY / NOT_NORMATIVE. Persistencia por DOCUMENTER separado;
no nuevo audit, reimplementación ni resolución de gate por el escritor.

## 1. Provenance y método de transcripción

Run técnico `run_8a13daf26e26`; auditor fresh `task_31b5fa20fa0d / ctx_894271bb43cb`.
BODY ORIGINAL real `msg_8d2897a11fb2`, recuperado mediante `orca orchestration inbox --full
--limit 1000 --json` y selección exacta del ID, no el payload de lifecycle.
Único done `msg_1992ac1809e6`, count1, succeeded/completed/accepted/settled/released,
filesModified=[]; confirmación durable en technicalGate.audit del root técnico.
Gate técnico AJENO `task_fe0ff2ab6f3b / gate_180145ac5766`, completed/resolved/PASS,
provenance coordinator_gate_resolution. Checkpoint anterior `task_7db2d948f544 /
gate_a0e9708711f8` es historical provenance only, nunca autoridad actual de este proceso.

Esta es una **transcripción seleccionada** del BODY original: criterios T01–T18, decisiones,
findings, foundationReview, provenance sustantiva y validación se preservan literalmente en
los objetos seleccionados. Los mapas mecánicos históricos508, RAW90 y listas completas de
686 métodos permanecen recuperables sin pérdida en el BODY Orca original y resultados root;
no se afirma transcripción íntegra/verbatim de todo aquel payload. El resumen XML siguiente
conserva las 86 clases, hashes y counts del full, con su fuente acotada686 identificada.
Referencias STATE/RUNBOOK y gates antiguos dentro de las citas son hechos de aquel corte
histórico; no requisitos públicos operativos. Estado competente actual: ESTADO/checkpoint/ORQ.

BODY UTF8 original: 196908 bytes / SHA256 `ec0fc33b14de189f042692eadc91bcd411c9ae03b38b07c9c4b053490239e0dd`.

## 2. Conclusión, T01–T18, decisiones y findings AJENOS — objetos literales

```json
{
  "role": "FRESH_INDEPENDENT_PAYMENTS_SLICE2_CORRECTIVE_TECHNICAL_AUDITOR",
  "mode": "READ_ONLY/FRESH/ADVERSARIAL",
  "runId": "run_8a13daf26e26",
  "taskId": "task_31b5fa20fa0d",
  "dispatchId": "ctx_894271bb43cb",
  "milestone": "SLICE_2_TECHNICAL_ACCEPTANCE",
  "STATUS": "TECHNICAL_AUDIT_COMPLETE",
  "SLICE2_TECHNICAL_AUDIT": "PASS",
  "TECHNICAL_AUDIT": "PASS",
  "executiveAssessment": "Complete coherent 32-file foundation independently re-audited against physical repository authority and fresh validation artifacts. PN14-S2-FRESH-TA-001 is technically CLOSED: every relevant trusted wrapper is validated before freeze, matching contract/hash/member/total cannot mask provenance incompatibility, contradictory sources in any input position produce review with complete original evidence and no freeze, compatible duplicates/replay remain valid. No new P0/P1/P2 finding; editorial NEW-PN13-017 remains OPEN/P2/NON_BLOCKING/IMPLEMENTATION_INDEPENDENT. This report is evidence for a separate competent technical gate, not that gate's resolution or permission to publish/activate/cut over.",
  "findings": {
    "newFindingTotals": {
      "P0": 0,
      "P1": 0,
      "P2": 0
    },
    "newFindings": [],
    "combinedOpenFindingTotals": {
      "P0": 0,
      "P1": 0,
      "P2": 1
    },
    "combinedOpen": [
      {
        "id": "NEW-PN13-017",
        "status": "OPEN",
        "severity": "P2",
        "classification": "EDITORIAL",
        "blocking": "NON_BLOCKING",
        "implementation": "IMPLEMENTATION_INDEPENDENT",
        "disposition": "Inherited unchanged; no fix and no reopen."
      }
    ],
    "PN14-S2-FRESH-TA-001": {
      "status": "CLOSED",
      "severity": "P1",
      "closureType": "INDEPENDENT_TECHNICAL_FINDING_CLOSURE_ONLY_NOT_GATE",
      "normativeEvidence": [
        "auditoria/handoffs/HANDOFF-PN14-SLICE2-ORDEN-SNAPSHOT-INMUTABLE.md:226-241 (§6 full authoritative scope envelope, complete metadata/hash/reference per field, contradictory trusted sources review, never arbitrary priority/latest wins)",
        "Same handoff §8 T12:342-365 missing/contradictory sources no guessed freeze; §6 complete raw append-only reports",
        "auditoria/orquestacion/PAYMENTS-AUTONOMOUS-STATE.json:2437-2444 own-envelope complete provenance semantics and existing-contract bounded2-path authorization",
        "Persisted human decision task_90e5821e06a0 and actual authority gate task_273ab43151a0/gate_13a2873a05cf"
      ],
      "codeEvidence": [
        "src/main/java/com/feelingpilates/pagos/ventas/aplicacion/BackfillOrdenSnapshot.java:25-41 (all trusted and null/incompatible contract handling)",
        "src/main/java/com/feelingpilates/pagos/ventas/aplicacion/BackfillOrdenSnapshot.java:47-55 (validate ALL trusted nonnull contracts before freeze; review early return)",
        "src/main/java/com/feelingpilates/pagos/ventas/aplicacion/BackfillOrdenSnapshot.java:72-95 (scope/membership/money/ownership/number and every contract purchase's wrapper raw/hash/ref/actor/time/origin/rule/per-field binding)",
        "src/main/java/com/feelingpilates/pagos/ventas/aplicacion/BackfillOrdenSnapshot.java:97-104 (every original source/row, metadata, raw and multiplicity preserved without source discard)",
        "src/main/java/com/feelingpilates/pagos/ventas/aplicacion/BackfillOrdenSnapshot.java:57-64 (freeze only after clean adjudication, rollback before conflict report)"
      ],
      "testEvidence": [
        "src/test/java/com/feelingpilates/pagos/ventas/BackfillOrdenSnapshotTest.java:90-125 same-contract five metadata variants, both orders plus all six three-source permutations; every scenario review, freeze call count0, complete raw/metadata evidence",
        "src/test/java/com/feelingpilates/pagos/ventas/BackfillOrdenSnapshotTest.java:127-134 exact compatible duplicates and reversed replay preserve stored winner",
        "src/test/java/com/feelingpilates/pagos/ventas/BackfillOrdenSnapshotTest.java:136 onwards per-field-only reference/hash contradictions both orders, matching overall metadata, zero freeze",
        "Physical fresh focal48 and full686 XML: all9 BackfillOrdenSnapshotTest methods executed without failure/error/skip"
      ],
      "adversarialAssessment": "Representative first nonnull contract is only selected candidate; it cannot authorize ignoring any other relevant source. Every trusted contract is checked against its OWN envelope, including purchases missing from observed raw rows; invalid source adds causes before any freeze. Logical decision is invariant under all positions/permutations. Original evidence serialization retains actual input order/multiplicity; source report hashes may consequently differ for different contradictory input sequences, which honors accepted original-evidence/append-only semantics and is not arbitrary winner selection. Compatible duplicates are unaffected.",
      "perFieldAuthorityAssessment": "No domain contract/constructor, allowed source type, commercial policy, API or trust inventory was changed. Complete authoritative backfill envelopes already bind a full scope contract and each field's reference/hash to that envelope's provenance; STATE2438 explicitly includes per-field own-envelope validation under existing contracts, original handoff§6 already requires checked hash/reference for every field. New checks at92-94 enforce those bindings for EACH relevant trusted contract in the application boundary. Generic CampoFuente syntactic construction does not by itself authorize an unbound alternative historical evidence graph or mixed-source merge behavior. No concrete accepted contradictory contract was found, and no new product/source-merging rule is inferred."
    }
  },
  "criteria": [
    {
      "id": "T01",
      "verdict": "PASS",
      "severity": "NONE",
      "criterion": "Immutable whole-product commercial snapshot, defensive lists and stable identity",
      "evidence": [
        "src/main/java/com/feelingpilates/pagos/ventas/dominio/Compra.java:11-32",
        "src/main/java/com/feelingpilates/pagos/ventas/dominio/CompraComponenteSnapshot.java",
        "src/main/java/com/feelingpilates/pagos/ventas/dominio/OrdenVenta.java:8-28",
        "src/test/java/com/feelingpilates/pagos/ventas/OrdenSnapshotDominioTest.java:11",
        "Whole product stays one root; mixed activities are immutable components; no catalog price/composition reinterpretation."
      ]
    },
    {
      "id": "T02",
      "verdict": "PASS",
      "severity": "NONE",
      "criterion": "Ownership, currency, numbering, quantities, valid ISO and checked money",
      "evidence": [
        "src/main/java/com/feelingpilates/pagos/ventas/dominio/ImporteMonetario.java",
        "src/main/java/com/feelingpilates/pagos/ventas/dominio/OrdenVenta.java:8-28",
        "src/main/java/com/feelingpilates/pagos/ventas/dominio/Compra.java:11-32",
        "src/test/java/com/feelingpilates/pagos/ventas/OrdenSnapshotDominioTest.java:19,27,35",
        "Duplicate/gap roots/components/activity and mismatched client/order/currency, negative/zero invalid quantities and overflow rejected."
      ]
    },
    {
      "id": "T03",
      "verdict": "PASS",
      "severity": "NONE",
      "criterion": "Commercial amount is purchased amount, no component allocation or multiplication",
      "evidence": [
        "src/main/java/com/feelingpilates/pagos/ventas/dominio/OrdenVenta.java:19",
        "src/test/java/com/feelingpilates/pagos/ventas/OrdenSnapshotDominioTest.java:42",
        "src/test/java/com/feelingpilates/pagos/ventas/PN14Slice2Fixtures.java",
        "Synthetic acquired 12345 MXN versus current catalog 99999; totals sum each existing purchase once."
      ]
    },
    {
      "id": "T04",
      "verdict": "PASS",
      "severity": "NONE",
      "criterion": "Complete versioned policies and typed canonical content/hash",
      "evidence": [
        "src/main/java/com/feelingpilates/pagos/ventas/dominio/PoliticaComercialSnapshot.java",
        "src/main/java/com/feelingpilates/pagos/ventas/dominio/ContenidoSnapshotCanonico.java",
        "src/main/java/com/feelingpilates/pagos/ventas/dominio/ProvenienciaSnapshot.java:13-27",
        "src/test/java/com/feelingpilates/pagos/ventas/PoliticaSnapshotCanonicoTest.java:11,21,29,38,45",
        "Strict UTF8, sorted byte ordering, typed/null distinctions, checked money and UTC exact microsecond precision, deterministic UUID/hash; decoder validates types/canonical form. No runtime benefit-policy activation."
      ]
    },
    {
      "id": "T05",
      "verdict": "PASS",
      "severity": "NONE",
      "criterion": "Sealed root/order/components immutable; catalog mutation cannot rewrite history",
      "evidence": [
        "src/main/resources/db/migration/V49__pn14_slice2_snapshot_inmutabilidad.sql:231-299",
        "src/test/java/com/feelingpilates/pagos/ventas/OrdenSnapshotPersistenciaTest.java:9,29",
        "SQL direct UPDATE/DELETE/TRUNCATE and component INSERT after sealing rejected; renamed/deactivated catalog/activity cannot rewrite frozen names/prices/policies/quantities."
      ]
    },
    {
      "id": "T06",
      "verdict": "PASS",
      "severity": "NONE",
      "criterion": "Real PostgreSQL ownership and referential constraints",
      "evidence": [
        "src/main/resources/db/migration/V48__pn14_slice2_orden_snapshot_expand.sql",
        "src/main/resources/db/migration/V49__pn14_slice2_snapshot_inmutabilidad.sql:79-229",
        "src/test/java/com/feelingpilates/pagos/ventas/OrdenSnapshotPersistenciaTest.java:37,46",
        "Composite client/order/currency FKs, unique line/component/activity, quantity check, referenced activity deletion RESTRICT and no cascade; actual SQL exercises constraints."
      ]
    },
    {
      "id": "T07",
      "verdict": "PASS",
      "severity": "NONE",
      "criterion": "Complete bundles/canonical consistency and legacy JPA coexistence",
      "evidence": [
        "src/main/resources/db/migration/V49__pn14_slice2_snapshot_inmutabilidad.sql:79-229,245-265",
        "src/main/java/com/feelingpilates/pagos/ventas/infraestructura/OrdenSnapshotJdbcAdapter.java:73-95",
        "src/test/java/com/feelingpilates/pagos/ventas/OrdenSnapshotPersistenciaTest.java:63,74,84,93",
        "Only existing legacy Compra owns JPA table mapping; JDBC updates explicit new columns for existing IDs. Legacy finance/state/expiration/motivo updates preserve bundle; old insert/update/delete without bundle still works. Invalid partial bundle or policy/content/hash fails closed."
      ]
    },
    {
      "id": "T08",
      "verdict": "PASS",
      "severity": "NONE",
      "criterion": "Deferred atomic whole-order freeze and complete rollback",
      "evidence": [
        "src/main/java/com/feelingpilates/pagos/ventas/infraestructura/OrdenSnapshotJdbcAdapter.java:15-53",
        "src/main/resources/db/migration/V49__pn14_slice2_snapshot_inmutabilidad.sql:300-348",
        "src/test/java/com/feelingpilates/pagos/ventas/OrdenSnapshotTransaccionTest.java:17,55,67",
        "Real proxy/REQUIRES_NEW transaction and independent connection verify first-root/components existed before second-line FK failure and every new effect rolls back; PREPARANDO cannot commit or incomplete group seal."
      ]
    },
    {
      "id": "T09",
      "verdict": "PASS",
      "severity": "NONE",
      "criterion": "Stable replay and conflict preserves winner",
      "evidence": [
        "src/main/java/com/feelingpilates/pagos/ventas/infraestructura/OrdenSnapshotJdbcAdapter.java:33-53",
        "src/main/java/com/feelingpilates/pagos/ventas/infraestructura/InformeBackfillJdbcAdapter.java:17-38",
        "src/test/java/com/feelingpilates/pagos/ventas/OrdenSnapshotIdempotenciaTest.java:10,17,31",
        "Same identity/payload returns stored winner with exact UUID/hash/instant/count; mismatch rolls back before append-only review report, preserving raw and winner. No Pago/accreditation insertion."
      ]
    },
    {
      "id": "T10",
      "verdict": "PASS",
      "severity": "NONE",
      "criterion": "Independent concurrent transactions, bounded locks, membership re-read",
      "evidence": [
        "src/main/java/com/feelingpilates/pagos/ventas/infraestructura/OrdenSnapshotJdbcAdapter.java:25-32",
        "src/test/java/com/feelingpilates/pagos/ventas/OrdenSnapshotConcurrenciaTest.java:15,33,56",
        "Distinct DriverManager connections/backend PIDs, CyclicBarrier(2) before lock, bounded futures; actual table-lock waiter for racing legacy INSERT changes membership to three and is rejected; advisory/table/row lock order + fresh re-read, 5s lock and 10s statement timeout, retry after timeout; full log:23686,23693."
      ]
    },
    {
      "id": "T11",
      "verdict": "PASS",
      "severity": "NONE",
      "criterion": "Only trusted complete historical source freezes",
      "evidence": [
        "src/main/java/com/feelingpilates/pagos/ventas/aplicacion/FuenteHistoricaCompra.java",
        "src/main/java/com/feelingpilates/pagos/ventas/aplicacion/BackfillOrdenSnapshot.java:23-59",
        "src/test/java/com/feelingpilates/pagos/ventas/BackfillOrdenSnapshotTest.java:36,43",
        "Trusted synthetic contract freezes historical terms, never current catalog; financial receipt alone yields review, complete missing commercial/policy/component paths and no guessed terms."
      ]
    },
    {
      "id": "T12",
      "verdict": "PASS",
      "severity": "NONE",
      "criterion": "All relevant trusted envelopes and contradictions fail closed in any position/permutation",
      "evidence": [
        "src/main/java/com/feelingpilates/pagos/ventas/aplicacion/BackfillOrdenSnapshot.java:25-55,72-104",
        "src/test/java/com/feelingpilates/pagos/ventas/BackfillOrdenSnapshotTest.java:54,64,76,83,90,127,136",
        "Every verified nonnull contract validated against its own wrapper before any freeze; null contract among trusted also contradicts. Same contract/hash/member/total cannot mask actor/reference/raw/time/origin or per-field binding contradiction. Five metadata variants each two orders plus all six three-source permutations; zero freeze calls and full raw evidence preserved. Exact compatible duplicates/replay still freeze/replay. Missing/corrupt group, ownership/currency, gaps, unknown quantity and checked overflow all review."
      ]
    },
    {
      "id": "T13",
      "verdict": "PASS",
      "severity": "NONE",
      "criterion": "Real PostgreSQL backfill replay, conflict, concurrency and append-only reports",
      "evidence": [
        "src/test/java/com/feelingpilates/pagos/ventas/BackfillOrdenSnapshotPostgresTest.java:12,19,32,39,47",
        "src/main/java/com/feelingpilates/pagos/ventas/infraestructura/InformeBackfillJdbcAdapter.java:17-38",
        "src/main/java/com/feelingpilates/pagos/ventas/aplicacion/BackfillOrdenSnapshot.java:60-64",
        "Independent PIDs148/149 with barrier, one order/report and total24690 in full log:23562. Repeat preserves keys/hash/count/amount; prior rejected report retained; corrected source may freeze only unfrozen scope; sealed winner not corrected; incomplete group makes no partial snapshot, late legacy row conflicts."
      ]
    },
    {
      "id": "T14",
      "verdict": "PASS",
      "severity": "NONE",
      "criterion": "Fresh all52 migrations to V49 on actual PostgreSQL16 with validator parity",
      "evidence": [
        "src/test/java/com/feelingpilates/pagos/ventas/OrdenSnapshotMigracionTest.java:14,31",
        "src/main/resources/db/migration/V49__pn14_slice2_snapshot_inmutabilidad.sql:100-229",
        "target/run_8a13daf26e26-full.log:23547",
        "Fresh PG16.14 all52 maxV49; Java/SQL canonical parity for policies, root/components/order and all available currencies; actual full context succeeds, required skips0."
      ]
    },
    {
      "id": "T15",
      "verdict": "PASS",
      "severity": "NONE",
      "criterion": "Upgrade50/V47 to52/V49 preserves legacy data and every historical checksum",
      "evidence": [
        "src/test/java/com/feelingpilates/pagos/ventas/OrdenSnapshotMigracionTest.java:39-81",
        "target/run_8a13daf26e26-full.log:23661",
        "Dummy target47 establishes50 applied rows;21 legacy variants, fingerprints of every original column, schemas/defaults/indexes/constraints and CRUD/defaults unchanged after upgrade; all50 original DB checksum rows compared at74, not sample/last checksum. Foundation tables remain empty, no automatic backfill. Physical source50 checksums unchanged."
      ]
    },
    {
      "id": "T16",
      "verdict": "PASS",
      "severity": "NONE",
      "criterion": "Internal frozen history only, isolated client ownership and no mutable catalog fallback",
      "evidence": [
        "src/main/java/com/feelingpilates/pagos/ventas/infraestructura/ConsultaHistoricaSnapshotJdbcAdapter.java:11-24",
        "src/test/java/com/feelingpilates/pagos/ventas/ConsultaHistoricaSnapshotTest.java:12,35",
        "Restricted real DB role SELECT only snapshot tables; catalog access forbidden yet query succeeds. No Paquete/PaqueteActividad/TipoActividad/Usuario join; frozen client IDs in every join/filter; no bundle returns no snapshot. Catalog rename/deactivate/recomposition does not change terms; DESC created/idASC stable ordering. Legacy public JSON remains unchanged."
      ]
    },
    {
      "id": "T17",
      "verdict": "PASS",
      "severity": "NONE",
      "criterion": "Inward architecture with no competing JPA mapping or automatic/API wiring",
      "evidence": [
        "src/test/java/com/feelingpilates/pagos/ventas/VentasSnapshotArquitecturaTest.java:12,34",
        "All18 production foundation Java files and all external production class references inspected",
        "Pure domain Java; application owns inward ports; JDBC infrastructure depends inward. Bytecode/descriptor rules over real classes; no new dependency or annotations/component/service/bean/config/controller/scheduler/writer wiring; no external runtime reference to foundation. Existing legacy Compra sole JPA owner."
      ]
    },
    {
      "id": "T18",
      "verdict": "PASS",
      "severity": "NONE",
      "criterion": "Published Slice1 safety net and full regression preserved",
      "evidence": [
        "target/run_8a13daf26e26-slice1.log:13 classes/67 tests",
        "target/run_8a13daf26e26-full.log:86 classes/686 tests",
        "target/run_8a13daf26e26-programacion.log:15 tests",
        "Protected ProgramacionPersistenciaTest SHA256=f6d81bd5cb6b57d8440ab581da967e2bc77570c988f2db5f84fdc1c1b26d4372",
        "Published M01-M12 tests byte-identical; all source files outside authorized four entry deltas unchanged. Protected test exactly HEAD with only approved Flyway literals47->49 and50->52; independent in-memory comparison, not regenerated. All required tests have0 failure/error/skip."
      ]
    }
  ],
  "foundationReview": {
    "all32ContentsReviewed": true,
    "productionJava": 18,
    "SQLMigrations": 2,
    "testClasses": 11,
    "testHelper": 1,
    "reviewBasis": "Complete coherent content and cross-file domain/canon/JDBC/SQL/tests/legacy seams, not just corrector changed lines; green tests used as execution evidence, not architecture authority.",
    "commercialIdentityAndMoney": "Immutable root/component commercial values; original purchase UUID and whole product identity; checked ISO currency/minor-unit money and whole-order sum.",
    "canonicalHash": "Typed Java and PostgreSQL canonical reconstruction/parity, UTF8 ordering, null/empty distinctions, strict UTC microseconds and deterministic UUID/hash/source contract distinction.",
    "ownership": "Composite SQL root/order/component client/currency FKs, stable client filter and frozen projections, no customer leakage.",
    "JPAJDBC": "Legacy Compra sole JPA mapping; JDBC only new bundle fields on existing rows, manual/inactive adapters, old financial state/expiry/motivo CRUD preserved.",
    "transactionsAndConcurrency": "REQUIRES_NEW rollback atomicity; advisory scope then table membership lock then ordered row locks/re-read; deferred final state and canonical validator; independent backend PIDs/barriers/timeouts/racing insertion coverage.",
    "trustedBackfill": "All missing/incomplete/contradictory trusted scope evidence reviewed fail closed, complete original evidence appended; no implicit trust, current catalog inference or live inventory.",
    "historyAndMigration": "Fresh52/V49, upgrade50/V47 to52/V49 with all50 applied checksums preserved; all historical50 source hashes unchanged; no source migration modification, renumbering or live backfill.",
    "architectureAndActivation": "No new JPA owner, library, controller, scheduled/startup automatic activation, live backfill, API reader/writer switch, benefit/payment/refund semantics, F2E, cutover or Slice3+."
  },
  "requires_human_decision": false,
  "p1_correctable": false,
  "technicalGate": "NOT_REACHED_BY_THIS_AUDITOR",
  "selfGatePass": false,
  "recommendation": "Candidate qualifies for a separate competent SLICE_2_TECHNICAL_ACCEPTANCE technical gate. Only after actual separate technical PASS may an authorized separate documenter persist the versioned STATE/checkpoint, then STOP_MILESTONE_COMPLETE/HUMAN_GATE. This finding closure/report does not settle that gate, publish, Git-checkpoint, activate production, cut over or authorize Slice3+.",
  "executionRestrictionsObserved": {
    "sourceWrites": 0,
    "documentationWrites": 0,
    "testWrites": 0,
    "buildsLaunched": 0,
    "testsLaunched": 0,
    "GitWrites": 0,
    "delegation": 0,
    "liveDBAccess": 0,
    "filesModified": [],
    "beforeAfterFileSHAStable": true,
    "indexStable": true,
    "reportPath": null,
    "durableReportMode": "Orca STATUS BODY only; no audit report file written."
  },
  "technicalBudget": {
    "cycle": 1,
    "max": 2,
    "auditCycleCompletedByThisReport": true,
    "consumptionRecording": "Coordinator must record completed cycle1 from delivered audit; auditor does not alter STATE or self-gate."
  },
  "remotePublication": false,
  "localGitCheckpoint": false,
  "productiveActivation": false,
  "cutover": false,
  "laterSlices": false
}
```

## 3. Provenance sustantiva del audit — objeto literal histórico

```json
{
  "physicalReadingOrder": [
    "AGENTS.md; auditoria/README-REESTRUCTURACION.md; auditoria/ESTADO-ACTUAL.md current-only reconciliation and applicable history",
    "Original Slice2 handoff ALL §§1-9 and T01-T18; complete resume handoff; Dominio13, DA014/021/022, Arquitectura Payments17, MAPA and PN13 pertinent accepted scope",
    "Original and resume Slice2 checkpoints, authorization/reconciliation reviews, physical full manifests; PN14 original/Slice1 safety-net closure pertinent acceptance",
    "auditoria/orquestacion/README.md, WORKFLOW.md, STATE-MACHINE.md, GATES.md, ROLES.md; active RUNBOOK version1 and STATE version2"
  ],
  "originalHandoffSHA256": "4d7e7803557f75a3d62afe67a757687a63b9c627b0fdbf2580d89feab36fdd7d",
  "resumeHandoffSHA256": "0168c3825b16c8dbace6ebb9c22fbbeefb1aabf5efecf5922f9c4c5fe1b340d8",
  "entry": {
    "taskId": "task_90e5821e06a0",
    "status": "completed",
    "full508RawSHA256": "53c301210279d66c8fe7e047e0003ba790e38fa9a236690cc78c55512d6d07d4",
    "persistedHumanDecision": {
      "finding": "PN14-S2-FRESH-TA-001",
      "authorizedPaths": [
        "src/main/java/com/feelingpilates/pagos/ventas/aplicacion/BackfillOrdenSnapshot.java",
        "src/test/java/com/feelingpilates/pagos/ventas/BackfillOrdenSnapshotTest.java"
      ],
      "semantics": [
        "Validate every relevant trusted-source full provenance relationship.",
        "Input-order independent adjudication; relevant contradiction fail-closed REQUIERE_REVISION.",
        "Only required tests proving order/permutation invariance."
      ],
      "technicalBudget": {
        "max": 2,
        "consumed": 0
      },
      "remotePublication": false,
      "localGitCheckpoint": false
    },
    "exactDeltaToCurrent": [
      "auditoria/ESTADO-ACTUAL.md",
      "auditoria/orquestacion/PAYMENTS-AUTONOMOUS-STATE.json",
      "src/main/java/com/feelingpilates/pagos/ventas/aplicacion/BackfillOrdenSnapshot.java",
      "src/test/java/com/feelingpilates/pagos/ventas/BackfillOrdenSnapshotTest.java"
    ]
  },
  "authorityAudit": {
    "taskId": "task_e79f9b8b31fb",
    "dispatchId": "ctx_a75d874b84e6",
    "fullBODY": "msg_917c8ab0e29e",
    "uniqueDone": "msg_4736e78c364c",
    "uniqueDoneCount": 1,
    "outcome": "succeeded",
    "taskStatus": "completed",
    "dispatchStatus": "completed",
    "settlementStage": "settled",
    "resourceStage": "released",
    "full508MapIndependentlyEqualToAuthorityGate": true
  },
  "actualRootGate": {
    "taskId": "task_273ab43151a0",
    "status": "completed",
    "provenance": "coordinator_gate_resolution",
    "gateId": "gate_13a2873a05cf",
    "gateStatus": "resolved",
    "resolution": "PASS",
    "rootQuestion": "Does the persisted human decision competently reconcile PN14-S2-FRESH-TA-001 corrective authority for exactly two technical paths, with active 0/2 budget and no other scope?",
    "uniqueGate": true,
    "preCorrectionCount": 508,
    "preCorrectionRawSHA256": "28650922f8e4678fe5348401765e93e3afe9c6aee79a2af9d4be44fb47ae8aa8",
    "candidateFileSHA256": {
      "auditoria/orquestacion/PAYMENTS-AUTONOMOUS-STATE.json": "f330c6ed641e976cb91463a8689fc02db4980e27234d122e36ab4ab265bd41c7",
      "auditoria/ESTADO-ACTUAL.md": "4d531f2110b434fe7974d9f293cb89e01d074a757ae0f86a18ed84000c10036f"
    },
    "authorityFileSHA256": {
      "auditoria/orquestacion/PAYMENTS-AUTONOMOUS-STATE.json": "f330c6ed641e976cb91463a8689fc02db4980e27234d122e36ab4ab265bd41c7",
      "auditoria/ESTADO-ACTUAL.md": "4d531f2110b434fe7974d9f293cb89e01d074a757ae0f86a18ed84000c10036f"
    },
    "exact2Binding": true,
    "preCorrectionFull508MapRecoveredAndCompared": true,
    "exactDeltaToCurrent": [
      "src/main/java/com/feelingpilates/pagos/ventas/aplicacion/BackfillOrdenSnapshot.java",
      "src/test/java/com/feelingpilates/pagos/ventas/BackfillOrdenSnapshotTest.java"
    ],
    "other506Preserved": true,
    "meaning": "Authority reconciliation gate only; technical acceptance gate NOT_REACHED.",
    "operationalDocCount": 2
  },
  "corrector": {
    "taskId": "task_0563160f7919",
    "dispatchId": "ctx_3dd0623cbe29",
    "fullBODY": "msg_b8f169f4903b",
    "uniqueDone": "msg_04f5f956b784",
    "uniqueDoneCount": 1,
    "outcome": "succeeded",
    "taskStatus": "completed",
    "dispatchStatus": "completed",
    "settlementStage": "settled",
    "resourceStage": "released",
    "before508MatchesActualAuthorityGate": true,
    "after508MatchesOwnPhysicalCandidate": true,
    "reportedDiffNotUsedAsSoleEvidence": true,
    "oldNewSHA256": {
      "src/main/java/com/feelingpilates/pagos/ventas/aplicacion/BackfillOrdenSnapshot.java": {
        "before": "b24e1b829431e5a91f7ca32af5d4b690b52c45c95ed75530d79eb08c729d4a0f",
        "after": "ed9eede27dd40ee6f29cbb1c0221080f7e9f6a7efa2d2e07320fb37c3334d7a3"
      },
      "src/test/java/com/feelingpilates/pagos/ventas/BackfillOrdenSnapshotTest.java": {
        "before": "4adff58b23be5225ea236d6f95ff05a39f85607ab9fe83b177a8ccbba96fe80f",
        "after": "11aae9109af2528289ed51c8bb15a4a1724d57c97d14c8257ff8cd54ac165b6d"
      }
    }
  },
  "scopeAssessment": "Exactly2 technical deltas from authority gate and exactly2 operational-document deltas from new-run entry, no additional file creation/deletion. Original32 candidate not regenerated. Protected f6 test, all52 migrations including historical50, legacy source, domain/application/API/financial/refund/dependencies/config/history preserved. Required validation ran after actual corrector settled/released. No delegation, scope correction or new product decision required."
}
```

## 4. Gate técnico real — selección literal del result root

```json
{
  "role": "PAYMENTS_SLICE2_TECHNICAL_ACCEPTANCE_GATE_COORDINATOR",
  "provenance": "coordinator_gate_resolution",
  "runId": "run_8a13daf26e26",
  "taskId": "task_fe0ff2ab6f3b",
  "gateId": "gate_180145ac5766",
  "question": "Is SLICE_2_TECHNICAL_ACCEPTANCE complete after the authorized PN14-S2-FRESH-TA-001 correction, all required fresh validations and independent technical re-audit PASS, without publication, productive activation, cutover or later-slice authority?",
  "gateStatus": "resolved",
  "resolution": "PASS",
  "meaning": "SLICE2_TECHNICAL_ACCEPTANCE_ONLY_NOT_PUBLICATION_NOT_PRODUCTIVE_ACTIVATION_NOT_CUTOVER_NOT_LATER_SLICE",
  "correctionBudget": {
    "max": 2,
    "consumed": 1,
    "cycles": [
      {
        "number": 1,
        "correctorTaskId": "task_0563160f7919",
        "freshAuditTaskId": "task_31b5fa20fa0d",
        "status": "COMPLETED_FRESH_REAUDIT_PASS"
      }
    ],
    "additionalCycleAuthorization": "ONLY_ORIGINAL_TWO_PATH_FINITE_SEMANTICS_AND_BUDGET_IF_NEEDED_NO_OTHER_SCOPE"
  },
  "prohibitions": [
    "NO_STAGING",
    "NO_COMMIT",
    "NO_PUSH",
    "NO_PUBLICATION",
    "NO_PRODUCTIVE_ACTIVATION",
    "NO_CUTOVER",
    "NO_SLICE3_TO_12",
    "NO_OTHER_TECHNICAL_WRITES"
  ]
}
```

Recibo AJENO literal de lifecycle (selección que omite sólo la repetición de criterios ya en §2):

```json
{
  "taskId": "task_31b5fa20fa0d",
  "dispatchId": "ctx_894271bb43cb",
  "bodyId": "msg_8d2897a11fb2",
  "doneId": "msg_1992ac1809e6",
  "uniqueDoneCount": 1,
  "outcome": "succeeded",
  "taskStatus": "completed",
  "dispatchStatus": "completed",
  "stage": "settled",
  "accepted": true,
  "released": true,
  "verdict": "SLICE2_TECHNICAL_AUDIT=PASS",
  "findings": {
    "newFindingTotals": {
      "P0": 0,
      "P1": 0,
      "P2": 0
    },
    "newFindings": [],
    "combinedOpenFindingTotals": {
      "P0": 0,
      "P1": 0,
      "P2": 1
    },
    "combinedOpen": [
      {
        "id": "NEW-PN13-017",
        "status": "OPEN",
        "severity": "P2",
        "classification": "EDITORIAL",
        "blocking": "NON_BLOCKING",
        "implementation": "IMPLEMENTATION_INDEPENDENT",
        "disposition": "Inherited unchanged; no fix and no reopen."
      }
    ],
    "PN14-S2-FRESH-TA-001": {
      "status": "CLOSED",
      "severity": "P1",
      "closureType": "INDEPENDENT_TECHNICAL_FINDING_CLOSURE_ONLY_NOT_GATE",
      "normativeEvidence": [
        "auditoria/handoffs/HANDOFF-PN14-SLICE2-ORDEN-SNAPSHOT-INMUTABLE.md:226-241 (§6 full authoritative scope envelope, complete metadata/hash/reference per field, contradictory trusted sources review, never arbitrary priority/latest wins)",
        "Same handoff §8 T12:342-365 missing/contradictory sources no guessed freeze; §6 complete raw append-only reports",
        "auditoria/orquestacion/PAYMENTS-AUTONOMOUS-STATE.json:2437-2444 own-envelope complete provenance semantics and existing-contract bounded2-path authorization",
        "Persisted human decision task_90e5821e06a0 and actual authority gate task_273ab43151a0/gate_13a2873a05cf"
      ],
      "codeEvidence": [
        "src/main/java/com/feelingpilates/pagos/ventas/aplicacion/BackfillOrdenSnapshot.java:25-41 (all trusted and null/incompatible contract handling)",
        "src/main/java/com/feelingpilates/pagos/ventas/aplicacion/BackfillOrdenSnapshot.java:47-55 (validate ALL trusted nonnull contracts before freeze; review early return)",
        "src/main/java/com/feelingpilates/pagos/ventas/aplicacion/BackfillOrdenSnapshot.java:72-95 (scope/membership/money/ownership/number and every contract purchase's wrapper raw/hash/ref/actor/time/origin/rule/per-field binding)",
        "src/main/java/com/feelingpilates/pagos/ventas/aplicacion/BackfillOrdenSnapshot.java:97-104 (every original source/row, metadata, raw and multiplicity preserved without source discard)",
        "src/main/java/com/feelingpilates/pagos/ventas/aplicacion/BackfillOrdenSnapshot.java:57-64 (freeze only after clean adjudication, rollback before conflict report)"
      ],
      "testEvidence": [
        "src/test/java/com/feelingpilates/pagos/ventas/BackfillOrdenSnapshotTest.java:90-125 same-contract five metadata variants, both orders plus all six three-source permutations; every scenario review, freeze call count0, complete raw/metadata evidence",
        "src/test/java/com/feelingpilates/pagos/ventas/BackfillOrdenSnapshotTest.java:127-134 exact compatible duplicates and reversed replay preserve stored winner",
        "src/test/java/com/feelingpilates/pagos/ventas/BackfillOrdenSnapshotTest.java:136 onwards per-field-only reference/hash contradictions both orders, matching overall metadata, zero freeze",
        "Physical fresh focal48 and full686 XML: all9 BackfillOrdenSnapshotTest methods executed without failure/error/skip"
      ],
      "adversarialAssessment": "Representative first nonnull contract is only selected candidate; it cannot authorize ignoring any other relevant source. Every trusted contract is checked against its OWN envelope, including purchases missing from observed raw rows; invalid source adds causes before any freeze. Logical decision is invariant under all positions/permutations. Original evidence serialization retains actual input order/multiplicity; source report hashes may consequently differ for different contradictory input sequences, which honors accepted original-evidence/append-only semantics and is not arbitrary winner selection. Compatible duplicates are unaffected.",
      "perFieldAuthorityAssessment": "No domain contract/constructor, allowed source type, commercial policy, API or trust inventory was changed. Complete authoritative backfill envelopes already bind a full scope contract and each field's reference/hash to that envelope's provenance; STATE2438 explicitly includes per-field own-envelope validation under existing contracts, original handoff§6 already requires checked hash/reference for every field. New checks at92-94 enforce those bindings for EACH relevant trusted contract in the application boundary. Generic CampoFuente syntactic construction does not by itself authorize an unbound alternative historical evidence graph or mixed-source merge behavior. No concrete accepted contradictory contract was found, and no new product/source-merging rule is inferred."
    }
  },
  "physicalFull508MapEqual": true,
  "filesModified": []
}
```

## 5. Pins completos candidato32, test protegido y validación fresh

```json
{
  "candidateFileCount": 32,
  "candidateFileSHA256": {
    "src/main/java/com/feelingpilates/pagos/ventas/aplicacion/BackfillOrdenSnapshot.java": "ed9eede27dd40ee6f29cbb1c0221080f7e9f6a7efa2d2e07320fb37c3334d7a3",
    "src/main/java/com/feelingpilates/pagos/ventas/aplicacion/CompraHistoricaSnapshot.java": "38e28e926cad743026284f466ac04c66e4d6ed55c44cf883f04eb99089e6a44f",
    "src/main/java/com/feelingpilates/pagos/ventas/aplicacion/CongelarOrdenSnapshot.java": "6ab0f95187a8adbedf988c4b01034b69774a4ee21d21dfeb4ff23a8ad33148b7",
    "src/main/java/com/feelingpilates/pagos/ventas/aplicacion/ConsultaHistoricaSnapshot.java": "2431565f7fb7b308fe8eede3eb6bb7fc36edbf961ea43f4408bfbfe3c1608709",
    "src/main/java/com/feelingpilates/pagos/ventas/aplicacion/FuenteHistoricaCompra.java": "c86e88aa4f279c12d7b176b6c816b309c821426a635e2bf5e837634450a156b7",
    "src/main/java/com/feelingpilates/pagos/ventas/aplicacion/InformeBackfillSnapshot.java": "25c6d9da5b3810bf46f2ff30e8777b61f5404221188c59a0a0a624cd448d8e11",
    "src/main/java/com/feelingpilates/pagos/ventas/aplicacion/RepositorioOrdenSnapshot.java": "ffbc35925dc241ed098b6a61c6539ac00d538ba34d5080f45ac92576af523064",
    "src/main/java/com/feelingpilates/pagos/ventas/dominio/Compra.java": "e9899d11aec2b2738ff1ce739a76cd66f0dcd8b49545bef669837d0bfff0de96",
    "src/main/java/com/feelingpilates/pagos/ventas/dominio/CompraComponenteSnapshot.java": "5ae0ad2d7e73d997942138858d1496b20bd23184115828fabc9a7618ee0cfd73",
    "src/main/java/com/feelingpilates/pagos/ventas/dominio/ContenidoSnapshotCanonico.java": "483db6f1f7cf4cfd3da71453373c393b22d58aeee408e6c99c439e30e478a6ac",
    "src/main/java/com/feelingpilates/pagos/ventas/dominio/ImporteMonetario.java": "e5cbd912a040ed15219e5424b45eedb7588ca79e2abbfbc20c36d0545a46b2d0",
    "src/main/java/com/feelingpilates/pagos/ventas/dominio/OrdenVenta.java": "e5122cbaf04d36dbff241a51431346e1944c40937587dc9d5520f76cee2d49fe",
    "src/main/java/com/feelingpilates/pagos/ventas/dominio/PoliticaComercialSnapshot.java": "c16184239f82777bfec7056b68640bc6129298d14789ca229696ad475616e58c",
    "src/main/java/com/feelingpilates/pagos/ventas/dominio/ProvenienciaSnapshot.java": "695373e25e825e71d4c1dafe07c231f2b936b459420d5ba78ed926908c7823b5",
    "src/main/java/com/feelingpilates/pagos/ventas/infraestructura/ConsultaHistoricaSnapshotJdbcAdapter.java": "df438e2c89a7201d84a2103b9edb2631a556e6c87d669741786da46ea404e3ce",
    "src/main/java/com/feelingpilates/pagos/ventas/infraestructura/FuenteHistoricaCompraJdbcAdapter.java": "89223c356530395a541582501a827ab5431dbe4480d4dd26e99021140c0f083b",
    "src/main/java/com/feelingpilates/pagos/ventas/infraestructura/InformeBackfillJdbcAdapter.java": "8458d295f3ae99518be95035a9317d24c18ff8a954abf90521e3443908164a3a",
    "src/main/java/com/feelingpilates/pagos/ventas/infraestructura/OrdenSnapshotJdbcAdapter.java": "2959e7c12fbdbaee09d1b1ea14dfd3d271e5b219026b2a27bf3b2a86dee82b21",
    "src/main/resources/db/migration/V48__pn14_slice2_orden_snapshot_expand.sql": "edc12860431820d634d4bc837eae79a5f3604718f83d99207b1ac8765459248e",
    "src/main/resources/db/migration/V49__pn14_slice2_snapshot_inmutabilidad.sql": "1aa858e265a389e9feeca1c691ace72e36fe87bc48fbdc79ff2e632fc3da280e",
    "src/test/java/com/feelingpilates/pagos/ventas/BackfillOrdenSnapshotPostgresTest.java": "5dda0a95515c880a08e8a2487ad7a0c2e3040464283ab689266425381890f0a4",
    "src/test/java/com/feelingpilates/pagos/ventas/BackfillOrdenSnapshotTest.java": "11aae9109af2528289ed51c8bb15a4a1724d57c97d14c8257ff8cd54ac165b6d",
    "src/test/java/com/feelingpilates/pagos/ventas/ConsultaHistoricaSnapshotTest.java": "077e3d9ce4055d335e9706f336f226fed382deef7c1df785c1c578a7c14102a0",
    "src/test/java/com/feelingpilates/pagos/ventas/OrdenSnapshotConcurrenciaTest.java": "79e2f8f807eccdd9c687a775fbabf213f2aea5e5129ebf3a51bb11b2d6caf607",
    "src/test/java/com/feelingpilates/pagos/ventas/OrdenSnapshotDominioTest.java": "55c605f696d26dee29a3ec585f18de2c2c6b36ce4592b47d7fc79858d3799471",
    "src/test/java/com/feelingpilates/pagos/ventas/OrdenSnapshotIdempotenciaTest.java": "4075033c5439bfbaa46adc6758a90d10c7c38572edc858e3ce129af90ce2e121",
    "src/test/java/com/feelingpilates/pagos/ventas/OrdenSnapshotMigracionTest.java": "a6fdb43bc4b54293c0db96c97ee7db304e8fb7575eb1974db6aa3673a1d1f0b1",
    "src/test/java/com/feelingpilates/pagos/ventas/OrdenSnapshotPersistenciaTest.java": "9d275805c51a31a13d44cec227f114933f7aef9fd1833bd6a0c6d70c8e525b77",
    "src/test/java/com/feelingpilates/pagos/ventas/OrdenSnapshotTransaccionTest.java": "9d5b6e713ff238df9dcefa24f3dc8d45c90affcb1c350f334f96bda6c5695c11",
    "src/test/java/com/feelingpilates/pagos/ventas/PN14Slice2Fixtures.java": "403a48fa7ee4d4de6a80bfd2f057c6229087e5b8d56788e22aae91785ed98fd9",
    "src/test/java/com/feelingpilates/pagos/ventas/PoliticaSnapshotCanonicoTest.java": "644201fc91164971c027e8d58d9bdf71f94a7095c0675f484ab807bb9db96bf9",
    "src/test/java/com/feelingpilates/pagos/ventas/VentasSnapshotArquitecturaTest.java": "a75ee330361c1b79bf3224edd167da8250ddeffb27c9e33c4f16c514b4a45814"
  },
  "immutables": {
    "protectedProgramacionTestSHA256": "f6d81bd5cb6b57d8440ab581da967e2bc77570c988f2db5f84fdc1c1b26d4372",
    "protectedTestExactlyTwoAuthorizedLiteralChangesComparedToPhysicalHEAD": true,
    "all52MigrationCount": 52,
    "old50MigrationCount": 50,
    "old50RawDigest": "e2848476012dc44133776cad500f2bb0870ecf7c1b66cdd9dbcab0ee5fddb398",
    "estadoOriginal90265BytePrefix": "99869f99acffdd1a9518f861f12d17d3a9454fe7ec3f2e9de994726c67603386",
    "all506OtherPathsComparedToActualAuthorityGate": true
  }
}
```

Validación literal seleccionada (arrays mecánicos excluidos expresamente):

```json
{
  "requiredRootTask": "task_3042ac8b14f9",
  "actualRootStatus": "completed",
  "structuredResultStatus": "REQUIRED_VALIDATION_PASS",
  "execution": "New authorized physical static handoff commands; NO historical HostValidator workflow revived",
  "environment": {
    "JDK": "Microsoft21.0.11",
    "Maven": "3.9.16",
    "Docker": "29.6.1 Docker Desktop desktop-linux",
    "Testcontainers": "1.21.3",
    "PostgreSQL": "16.14 postgres:16-alpine",
    "Ryuk": "ENABLED_STARTED",
    "dummyEnvironment": true,
    "requiredSkips": 0
  },
  "all508StableBeforeAndAfterExecutionAndOwnAudit": true,
  "referenceSourceRawSHA256": "0116b4cf5d6b43b4a91dda0b8792183496e4a40f96dea6466210dd5a42dbf4ac",
  "rootStructuredPhaseCapture": [
    {
      "phase": "programacion",
      "exitCode": 0,
      "startedAt": "2026-09-17T00:14:14.237Z",
      "capturedAt": "2026-09-17T00:14:47.828Z",
      "log": "target/run_8a13daf26e26-programacion.log",
      "logSHA256": "11919d8c8fe2bd64ba27b22be2d99a94c5ec05292eeb61d0625d7da1bcbf053d",
      "logMtimeUtc": "2026-09-17T00:14:25.136557Z",
      "classes": 1,
      "totals": {
        "tests": 15,
        "failures": 0,
        "errors": 0,
        "skipped": 0
      }
    },
    {
      "phase": "slice2",
      "exitCode": 0,
      "startedAt": "2026-09-17T00:14:47.828Z",
      "capturedAt": "2026-09-17T00:15:20.646Z",
      "log": "target/run_8a13daf26e26-slice2.log",
      "logSHA256": "972a2a6a47ea848fdf01c324741751069fad89acca0361ddbf592b8043daaea9",
      "logMtimeUtc": "2026-09-17T00:15:08.178467Z",
      "classes": 11,
      "totals": {
        "tests": 48,
        "failures": 0,
        "errors": 0,
        "skipped": 0
      }
    },
    {
      "phase": "slice1",
      "exitCode": 0,
      "startedAt": "2026-09-17T00:15:20.647Z",
      "capturedAt": "2026-09-17T00:15:39.804Z",
      "log": "target/run_8a13daf26e26-slice1.log",
      "logSHA256": "e1f3cadae41a2cc97b819710181d03d4fe0ad4868dce028c47a11ba3df0687d0",
      "logMtimeUtc": "2026-09-17T00:15:29.118584Z",
      "classes": 13,
      "totals": {
        "tests": 67,
        "failures": 0,
        "errors": 0,
        "skipped": 0
      }
    },
    {
      "phase": "full",
      "exitCode": 0,
      "startedAt": "2026-09-17T00:15:39.804Z",
      "capturedAt": "2026-09-17T00:16:42.672Z",
      "log": "target/run_8a13daf26e26-full.log",
      "logSHA256": "8caf4cf6e633a0212a90d8e2cb3b65a5d9dd32a406d4b834b6a524391e872d4b",
      "logMtimeUtc": "2026-09-17T00:16:28.536804Z",
      "classes": 86,
      "totals": {
        "tests": 686,
        "failures": 0,
        "errors": 0,
        "skipped": 0
      }
    }
  ],
  "all86PhysicalFullXMLHashesClassesMethodsCountsMtimesComparedToCompletedRootResult": true,
  "focalXMLHandling": "Surefire focal XML files were overwritten normally by the later full run; current86 physical XML are the full run. Focal class/count/log evidence independently matches completed root captured phase results; no claim stale focal XML survives on disk.",
  "requiredSkips": 0,
  "expectedActualSlice2Tests": 48,
  "oldTests": 45,
  "newRegressionMethods": 3,
  "completeActualFullTests": 686,
  "historical683": "History only, not acceptance substitution.",
  "independentTestExecution": false,
  "mutationTesting": false,
  "limitations": "Auditor did not launch builds/tests or write artifacts; independently inspected fresh required-root log/XML bytes and asserted code/test semantics. Execution evidence proves those tests ran against stable source; normative/architectural review is independent.",
  "physicalFullXMLMismatchCount": 0
}
```

### XML full — resumen completo86 clases / evidencia acotada686

Surefire sobrescribió normalmente los focal XML al ejecutar full: no se afirma que los XML focales previos sigan físicos. Full totals686/0/0/0,86clases,686casos,requiredSkips0; los logs y capturas root sostienen los focals15/48/67. Todas las filas son selección exacta de `validation.physicalCurrentFullXML` del BODY original; allí son recuperables los nombres de los686 casos y mtimes originales.

| XML físico | SHA256 raw | Clase | Tests/failures/errors/skipped |
| --- | --- | --- | --- |
| target/surefire-reports/TEST-com.feelingpilates.FeelingpilatesApplicationTests.xml | 69ea6b21844ac168e655c51c8d4ba96de9b8a511cdcf0a12901e96845f8a4bd9 | com.feelingpilates.FeelingpilatesApplicationTests | 1/0/0/0 |
| target/surefire-reports/TEST-com.feelingpilates.auth.AuthControllerTest.xml | 064bb42c12c1d491c83c2a4881966abed9cce00f1495153720c79d9824b6ade0 | com.feelingpilates.auth.AuthControllerTest | 10/0/0/0 |
| target/surefire-reports/TEST-com.feelingpilates.auth.dto.CompletarInvitacionRequestTest.xml | ee1b1c5856c84d18341716d6714b1bd210f35b9d1d5fb598862df5a7e778b4c5 | com.feelingpilates.auth.dto.CompletarInvitacionRequestTest | 1/0/0/0 |
| target/surefire-reports/TEST-com.feelingpilates.calendario.ReservaControllerSecurityTest.xml | f80887862b58e73a509a93960c6dcdcab23dc4d49f51704ee67cc2ee30381a24 | com.feelingpilates.calendario.ReservaControllerSecurityTest | 4/0/0/0 |
| target/surefire-reports/TEST-com.feelingpilates.calendario.ReservaServiceCaracterizacionTest.xml | fd5fa66178df89dde9e00fe886d929f5bc8d64250939035111f866cc62dbae82 | com.feelingpilates.calendario.ReservaServiceCaracterizacionTest | 15/0/0/0 |
| target/surefire-reports/TEST-com.feelingpilates.calendario.TurnoInstructorServiceCaracterizacionTest.xml | dde0e443b88736e68643861ee97ff4b53a9801109cb8a7ef5e65c30f1be450fc | com.feelingpilates.calendario.TurnoInstructorServiceCaracterizacionTest | 38/0/0/0 |
| target/surefire-reports/TEST-com.feelingpilates.calendario.TurnoInstructorServiceHorarioVersionadoTest.xml | e61e7db0557e040da2edd967403901ab6f59adb1e518c9314234c04f47299755 | com.feelingpilates.calendario.TurnoInstructorServiceHorarioVersionadoTest | 14/0/0/0 |
| target/surefire-reports/TEST-com.feelingpilates.calendario.servicio.ImpactoPuntualEnExcepcionHorarioTest.xml | f04b958d25621f196f2dce6680d294f4ff19a3e2e2ca304ea7f7202994c7ca29 | com.feelingpilates.calendario.servicio.ImpactoPuntualEnExcepcionHorarioTest | 12/0/0/0 |
| target/surefire-reports/TEST-com.feelingpilates.calendario.servicio.ImpactoTurnosRecurrentesEnHorarioTest.xml | 57b6e3e31379090d6a014710e472b3561928cd5b69979f2cc95e03e90ac27869 | com.feelingpilates.calendario.servicio.ImpactoTurnosRecurrentesEnHorarioTest | 7/0/0/0 |
| target/surefire-reports/TEST-com.feelingpilates.exception.CodigoErrorExtractorArquitecturaTest.xml | 4b5fec66bae82f4da42e64fe41c7f14e20922c4532f02f2f87474414d3ff6573 | com.feelingpilates.exception.CodigoErrorExtractorArquitecturaTest | 7/0/0/0 |
| target/surefire-reports/TEST-com.feelingpilates.exception.CodigoErrorExtractorTest.xml | 8cd610b42e7b09bc7ff6c4a1f00c1721d65af86a0c1b9ab6a066d46292310ef9 | com.feelingpilates.exception.CodigoErrorExtractorTest | 7/0/0/0 |
| target/surefire-reports/TEST-com.feelingpilates.exception.GlobalExceptionHandlerTest.xml | a68c0fc020070b5f8b8bfaae34517b92cd2551ea41e20fff43bf8d681a62887b | com.feelingpilates.exception.GlobalExceptionHandlerTest | 6/0/0/0 |
| target/surefire-reports/TEST-com.feelingpilates.notificaciones.EmailServiceConsolaTest.xml | 4f79e47e8923d8cdb83c5a7391cd38720ea072f41777b15b2ee5e10aed38bafb | com.feelingpilates.notificaciones.EmailServiceConsolaTest | 1/0/0/0 |
| target/surefire-reports/TEST-com.feelingpilates.pagos.caracterizacion.CatalogoPN14Test.xml | 5e70cbbadd7b119c87ee8a7b36d268580f71358650b29f53373624f417418c6f | com.feelingpilates.pagos.caracterizacion.CatalogoPN14Test | 3/0/0/0 |
| target/surefire-reports/TEST-com.feelingpilates.pagos.caracterizacion.CompraPersistenciaPN14Test.xml | b2930d863564234460e2ccad3daf61f0e61f2ac73c46114af909772e6718a4f6 | com.feelingpilates.pagos.caracterizacion.CompraPersistenciaPN14Test | 4/0/0/0 |
| target/surefire-reports/TEST-com.feelingpilates.pagos.caracterizacion.PagoIntentoPN14Test.xml | efd54f23ee6bdbbb8ac8e74b2fa9a26ea5c0658e9d348f81593aec598a3a29fb | com.feelingpilates.pagos.caracterizacion.PagoIntentoPN14Test | 5/0/0/0 |
| target/surefire-reports/TEST-com.feelingpilates.pagos.caracterizacion.PagoLecturasPN14Test.xml | f686a416fefe4aa4d103aae99c5ee777d0b02797d86b1de33b1c8c611e864dae | com.feelingpilates.pagos.caracterizacion.PagoLecturasPN14Test | 3/0/0/0 |
| target/surefire-reports/TEST-com.feelingpilates.pagos.caracterizacion.PagoReconciliacionPN14Test.xml | 4ad23d5baae157bbdc88b63f05bd5206616c965fbf6959e56828ecac5ee0a1d7 | com.feelingpilates.pagos.caracterizacion.PagoReconciliacionPN14Test | 2/0/0/0 |
| target/surefire-reports/TEST-com.feelingpilates.pagos.caracterizacion.PagoWebhookPN14Test.xml | 185bb343be8ad401e76cf3176143ba94cc8162ac1b8bb465314d13236e587c93 | com.feelingpilates.pagos.caracterizacion.PagoWebhookPN14Test | 5/0/0/0 |
| target/surefire-reports/TEST-com.feelingpilates.pagos.caracterizacion.PagosApiPN14Test.xml | 2f49cd866c122aac500548923b3a9d50f8627d8fe43afad29ed9eddc72108ec3 | com.feelingpilates.pagos.caracterizacion.PagosApiPN14Test | 4/0/0/0 |
| target/surefire-reports/TEST-com.feelingpilates.pagos.caracterizacion.ReservasApiPN14Test.xml | b117c83b54ed8936856bba376ca5f6d6754657baa7bfd2fc31c66e59ea849e2d | com.feelingpilates.pagos.caracterizacion.ReservasApiPN14Test | 3/0/0/0 |
| target/surefire-reports/TEST-com.feelingpilates.pagos.caracterizacion.ReservasPN14Test.xml | 88c4ae374a87fb063c6cd074130804e785e5b7809c138e3d5785aa55142c3ba2 | com.feelingpilates.pagos.caracterizacion.ReservasPN14Test | 4/0/0/0 |
| target/surefire-reports/TEST-com.feelingpilates.pagos.caracterizacion.VentaServicePN14Test.xml | adaeb386dd281d602bc3afd41d710a622309212e358a01e646fb8186fb27562c | com.feelingpilates.pagos.caracterizacion.VentaServicePN14Test | 8/0/0/0 |
| target/surefire-reports/TEST-com.feelingpilates.pagos.caracterizacion.VentasCatalogoApiPN14Test.xml | b5df96babca028ab0e89f420fbf3008240959175c20135646704c9df1d7ef533 | com.feelingpilates.pagos.caracterizacion.VentasCatalogoApiPN14Test | 7/0/0/0 |
| target/surefire-reports/TEST-com.feelingpilates.pagos.ventas.BackfillOrdenSnapshotPostgresTest.xml | 3e976015a9d9d7004846dad13b8b419daae4622d48bb64e6e7c0b79e916072d7 | com.feelingpilates.pagos.ventas.BackfillOrdenSnapshotPostgresTest | 5/0/0/0 |
| target/surefire-reports/TEST-com.feelingpilates.pagos.ventas.BackfillOrdenSnapshotTest.xml | 3690cff009529e2a40b1b5188b3ef4396dc468f82acc26361cb9cf8d1ade9480 | com.feelingpilates.pagos.ventas.BackfillOrdenSnapshotTest | 9/0/0/0 |
| target/surefire-reports/TEST-com.feelingpilates.pagos.ventas.ConsultaHistoricaSnapshotTest.xml | 73c818cbec0322649d5380a7ba8419c6a093a1c386ac78a7e321de8a261df6e4 | com.feelingpilates.pagos.ventas.ConsultaHistoricaSnapshotTest | 2/0/0/0 |
| target/surefire-reports/TEST-com.feelingpilates.pagos.ventas.OrdenSnapshotConcurrenciaTest.xml | 976f3bc5b38ea48541cc18d2289b755ca4b712a26fc260eefc5c2f9055d7d489 | com.feelingpilates.pagos.ventas.OrdenSnapshotConcurrenciaTest | 3/0/0/0 |
| target/surefire-reports/TEST-com.feelingpilates.pagos.ventas.OrdenSnapshotDominioTest.xml | 9ee037596b35b25dc2c884dcfc24295250e0cfa9d01b4c58515f39fb84115e9c | com.feelingpilates.pagos.ventas.OrdenSnapshotDominioTest | 5/0/0/0 |
| target/surefire-reports/TEST-com.feelingpilates.pagos.ventas.OrdenSnapshotIdempotenciaTest.xml | 11ad205c4ee37c87e4cb2e85f9f7a94d260b5104dd08ab9a90ffe797fde7b07d | com.feelingpilates.pagos.ventas.OrdenSnapshotIdempotenciaTest | 3/0/0/0 |
| target/surefire-reports/TEST-com.feelingpilates.pagos.ventas.OrdenSnapshotMigracionTest.xml | 929e1d399d74bae8979e9b4f2522b80ec033e621af5fc9ff310b5de31fe1b740 | com.feelingpilates.pagos.ventas.OrdenSnapshotMigracionTest | 3/0/0/0 |
| target/surefire-reports/TEST-com.feelingpilates.pagos.ventas.OrdenSnapshotPersistenciaTest.xml | 4931f50749946a704809889c284232b66a762d5a5115763601961ad39d9554ae | com.feelingpilates.pagos.ventas.OrdenSnapshotPersistenciaTest | 8/0/0/0 |
| target/surefire-reports/TEST-com.feelingpilates.pagos.ventas.OrdenSnapshotTransaccionTest.xml | 18615583497c2b8c71561bec9d2a395dad59c54eeb5356d71666871caf0ed81b | com.feelingpilates.pagos.ventas.OrdenSnapshotTransaccionTest | 3/0/0/0 |
| target/surefire-reports/TEST-com.feelingpilates.pagos.ventas.PoliticaSnapshotCanonicoTest.xml | 672fa0584ab4771c734bf4b26f95a3d4f17954770dd9aa65d7cfb0540c37e822 | com.feelingpilates.pagos.ventas.PoliticaSnapshotCanonicoTest | 5/0/0/0 |
| target/surefire-reports/TEST-com.feelingpilates.pagos.ventas.VentasSnapshotArquitecturaTest.xml | edf759254990d521d2a317cf517f55920e56a8e5567900122a0252c02d6da461 | com.feelingpilates.pagos.ventas.VentasSnapshotArquitecturaTest | 2/0/0/0 |
| target/surefire-reports/TEST-com.feelingpilates.programacion.AjusteIdConcurrenciaTest.xml | e3b776abd2c43e957b32f290999213eb14be8d7304458429a4237f474fa67f51 | com.feelingpilates.programacion.AjusteIdConcurrenciaTest | 1/0/0/0 |
| target/surefire-reports/TEST-com.feelingpilates.programacion.AjusteProgramacionFechaPersistenceTest.xml | 152b58f144849dbdb67d53bfccb513da3d347be3de27a12b335f711ab6b621fb | com.feelingpilates.programacion.AjusteProgramacionFechaPersistenceTest | 4/0/0/0 |
| target/surefire-reports/TEST-com.feelingpilates.programacion.DarkLaunchArquitecturaTest.xml | 74c39fa765d35c1fbacc9eec603f9f17d5a833e03e08f97a70ef7e06aba178a5 | com.feelingpilates.programacion.DarkLaunchArquitecturaTest | 3/0/0/0 |
| target/surefire-reports/TEST-com.feelingpilates.programacion.DarkLaunchIntegracionTest.xml | bde3cb4d18198791fdcd6b0c38c3fc8c3eda8bacf87d98da28031535b77b3370 | com.feelingpilates.programacion.DarkLaunchIntegracionTest | 1/0/0/0 |
| target/surefire-reports/TEST-com.feelingpilates.programacion.MigracionV46V47Test.xml | 31a768a1f6660a558bbaaaf77d8d538c21d60db0a7eb16a82f2dd9311ae1a2ab | com.feelingpilates.programacion.MigracionV46V47Test | 1/0/0/0 |
| target/surefire-reports/TEST-com.feelingpilates.programacion.ProgramacionConcurrenciaTest.xml | 3fec66842800beae8524b15b6596e6c776469493bce98b13cda6939c79a529fc | com.feelingpilates.programacion.ProgramacionConcurrenciaTest | 5/0/0/0 |
| target/surefire-reports/TEST-com.feelingpilates.programacion.ProgramacionPersistenciaTest.xml | b81128e6cac89a5f193acfb9423e673e40d302f7c6774b9404138c1810544ef2 | com.feelingpilates.programacion.ProgramacionPersistenciaTest | 15/0/0/0 |
| target/surefire-reports/TEST-com.feelingpilates.programacion.RelecturaAjustePostLocksTest.xml | 69885e49cc9afa9d3b8bf3d3dd3e9174114847574a2fd6911cdd0f13cd220287 | com.feelingpilates.programacion.RelecturaAjustePostLocksTest | 1/0/0/0 |
| target/surefire-reports/TEST-com.feelingpilates.programacion.repositorio.BloqueProgramacionRepositoryVigenciaTest.xml | fd214deeea222197d808178dcb9ac6a64a7e3dba9fc3602c29cfe09c049b3d25 | com.feelingpilates.programacion.repositorio.BloqueProgramacionRepositoryVigenciaTest | 6/0/0/0 |
| target/surefire-reports/TEST-com.feelingpilates.programacion.servicio.AjusteProgramacionFechaServiceTest.xml | 334064c165c55b67330c0524735e997a6a473d409dd0ceae1e22ee4c1bc62e39 | com.feelingpilates.programacion.servicio.AjusteProgramacionFechaServiceTest | 10/0/0/0 |
| target/surefire-reports/TEST-com.feelingpilates.programacion.servicio.AplicadorAjustesProgramacionTest.xml | d2fe40daf29461f78fe0e2f9b8abc80c0b93fe42a6e7f852fbf3f7c6f9b6d9eb | com.feelingpilates.programacion.servicio.AplicadorAjustesProgramacionTest | 6/0/0/0 |
| target/surefire-reports/TEST-com.feelingpilates.programacion.servicio.BloqueProgramacionServiceTest.xml | 13c4ac773b076eaf94b67200db2925222f29af0c59addfe2435cbae9ce91bf59 | com.feelingpilates.programacion.servicio.BloqueProgramacionServiceTest | 52/0/0/0 |
| target/surefire-reports/TEST-com.feelingpilates.programacion.servicio.ImpactoBloquesEnHorarioTest.xml | 033ef019169397e31cad50fd2ab6938146ec41659d2d7c7aa70eab34c101b8d7 | com.feelingpilates.programacion.servicio.ImpactoBloquesEnHorarioTest | 5/0/0/0 |
| target/surefire-reports/TEST-com.feelingpilates.programacion.servicio.LocksOrdenadosTest.xml | ccf4a6c7eeb27f5ceed818dd966741f05685551328cc242c38fc64b0d7712ad7 | com.feelingpilates.programacion.servicio.LocksOrdenadosTest | 2/0/0/0 |
| target/surefire-reports/TEST-com.feelingpilates.programacion.servicio.ProgramacionEfectivaTest.xml | 32753068b7e1d31f66d90a086502df2067650eb9f046971ca8da7807a469f629 | com.feelingpilates.programacion.servicio.ProgramacionEfectivaTest | 5/0/0/0 |
| target/surefire-reports/TEST-com.feelingpilates.programacion.servicio.ProgramacionPolicyATest.xml | 1e3dbfdd3d90e3a08632e955bc0107f1a0e741d05451826cd59d21a9ce1e2f65 | com.feelingpilates.programacion.servicio.ProgramacionPolicyATest | 3/0/0/0 |
| target/surefire-reports/TEST-com.feelingpilates.programacion.servicio.ProgramacionValidadorTest.xml | ed5fafb56e8786db06c90cac8f3246581100018675d4fce96c758a11b7baec43 | com.feelingpilates.programacion.servicio.ProgramacionValidadorTest | 13/0/0/0 |
| target/surefire-reports/TEST-com.feelingpilates.programacion.servicio.StaleDiscoveryAjusteProgramacionTest.xml | a8b03ced549c2c28c40c402ec30f352ac44587b2a7c01ce7fde77197c0404507 | com.feelingpilates.programacion.servicio.StaleDiscoveryAjusteProgramacionTest | 2/0/0/0 |
| target/surefire-reports/TEST-com.feelingpilates.seguridad.AutorizacionContextualControllerTest.xml | 9b4a7932d1dfd081b2f241c69098b659497619909f04f5d425ce938e927add1c | com.feelingpilates.seguridad.AutorizacionContextualControllerTest | 3/0/0/0 |
| target/surefire-reports/TEST-com.feelingpilates.seguridad.AutorizadorSalonTest.xml | 31924a3fa3b6bc903ab441739a17d1d2b9337442cfafecf8ce482e84791fe103 | com.feelingpilates.seguridad.AutorizadorSalonTest | 6/0/0/0 |
| target/surefire-reports/TEST-com.feelingpilates.transicion.programacion.detector.CandidateEvidenceGeneratorTest.xml | 1bc9a96286d88a002d1e219c132210e717189ad1da8a6c4047cdb9d9c9c153c7 | com.feelingpilates.transicion.programacion.detector.CandidateEvidenceGeneratorTest | 4/0/0/0 |
| target/surefire-reports/TEST-com.feelingpilates.transicion.programacion.detector.DetectorArchitectureIsolationTest.xml | 9e9a2404db5bb1886c595cab795e9b8b5fce7928508d16a8cbc95cbc1f60d751 | com.feelingpilates.transicion.programacion.detector.DetectorArchitectureIsolationTest | 3/0/0/0 |
| target/surefire-reports/TEST-com.feelingpilates.transicion.programacion.detector.DetectorClassifierTest.xml | a4b9e335a147148a9f16ed8c3b40c9448dcbbc3375d1f0f87c3dfd169ab9a62d | com.feelingpilates.transicion.programacion.detector.DetectorClassifierTest | 14/0/0/0 |
| target/surefire-reports/TEST-com.feelingpilates.transicion.programacion.detector.DetectorImmutabilityTest.xml | 92a4134f098598727cccafeca0c5285261441430ecf9c56d8efc92d4b1b91719 | com.feelingpilates.transicion.programacion.detector.DetectorImmutabilityTest | 3/0/0/0 |
| target/surefire-reports/TEST-com.feelingpilates.transicion.programacion.detector.DetectorResultInvariantTest.xml | d8664984351968d7521dad0e9b7d279137873f41ad810b5cafde91b0202a5ae6 | com.feelingpilates.transicion.programacion.detector.DetectorResultInvariantTest | 11/0/0/0 |
| target/surefire-reports/TEST-com.feelingpilates.transicion.programacion.detector.F2DAuthorityGuardTest.xml | 1940877edd13727ac7273b7eff30a5860fc1dbcf2346aea27939468675ebd054 | com.feelingpilates.transicion.programacion.detector.F2DAuthorityGuardTest | 2/0/0/0 |
| target/surefire-reports/TEST-com.feelingpilates.ubicaciones.HorarioOperacionConcurrenciaTest.xml | 8ea5d1393243b2b6ca380095476ab4746a70cdad631263a72636ff1837920c75 | com.feelingpilates.ubicaciones.HorarioOperacionConcurrenciaTest | 7/0/0/0 |
| target/surefire-reports/TEST-com.feelingpilates.ubicaciones.HorarioOperacionMigracionV42V43Test.xml | 3860e6a9edf8f10b7eb6067211d8e98be6e851b86a7c6ee31b8101713e07c923 | com.feelingpilates.ubicaciones.HorarioOperacionMigracionV42V43Test | 1/0/0/0 |
| target/surefire-reports/TEST-com.feelingpilates.ubicaciones.HorarioOperacionMigracionV43V46Test.xml | 9494136bbcef8b120222597f3a4c8e568d38c329993c6df59cd3fef8f28f4977 | com.feelingpilates.ubicaciones.HorarioOperacionMigracionV43V46Test | 1/0/0/0 |
| target/surefire-reports/TEST-com.feelingpilates.ubicaciones.HorarioOperacionVersionadoPersistenciaTest.xml | 56de7962f4a7ec4834adeb090d13fb6c5652cda39145869e77d74b789a081ae6 | com.feelingpilates.ubicaciones.HorarioOperacionVersionadoPersistenciaTest | 11/0/0/0 |
| target/surefire-reports/TEST-com.feelingpilates.ubicaciones.HorarioOperacionWritersPersistenciaTest.xml | 00459eeaefce119c78dc4113f18bd0ca9f6c1172b0e6ead54281d2f0ff121800 | com.feelingpilates.ubicaciones.HorarioOperacionWritersPersistenciaTest | 8/0/0/0 |
| target/surefire-reports/TEST-com.feelingpilates.ubicaciones.SalonHorarioExcepcionConcurrenciaTest.xml | d2f09c7b444189315c4fc1c1cd3b12c3691e06d3d40152d45cf72876e28069df | com.feelingpilates.ubicaciones.SalonHorarioExcepcionConcurrenciaTest | 7/0/0/0 |
| target/surefire-reports/TEST-com.feelingpilates.ubicaciones.SalonHorarioExcepcionPersistenciaTest.xml | bc321acf5d382009efa5e2614b86f5343ab60e3a2e4f0b5614f42a97f0511972 | com.feelingpilates.ubicaciones.SalonHorarioExcepcionPersistenciaTest | 9/0/0/0 |
| target/surefire-reports/TEST-com.feelingpilates.ubicaciones.SalonHorarioOperacionHistorialPersistenciaTest.xml | b7640faf2f83f3dc3991391759b44d203fa9caa7285695c11d9db04ce0b2da4e | com.feelingpilates.ubicaciones.SalonHorarioOperacionHistorialPersistenciaTest | 7/0/0/0 |
| target/surefire-reports/TEST-com.feelingpilates.ubicaciones.UbicacionesPersistenciaTest.xml | e44faca4b89263c9eb5103576bb4dbda49d52b2157527c0478df863a0b45d1d2 | com.feelingpilates.ubicaciones.UbicacionesPersistenciaTest | 8/0/0/0 |
| target/surefire-reports/TEST-com.feelingpilates.ubicaciones.controlador.SalonHorarioExcepcionControllerTest.xml | 21aed72e8f1f1362f592f876b3b0339923e0b396fe0be73cba70c8279630fa6a | com.feelingpilates.ubicaciones.controlador.SalonHorarioExcepcionControllerTest | 14/0/0/0 |
| target/surefire-reports/TEST-com.feelingpilates.ubicaciones.controlador.SalonHorarioOperacionControllerTest.xml | 8e111afda2391358a1768f3fce8e3a9be351abd30f08a55c9e82f17b61a920ad | com.feelingpilates.ubicaciones.controlador.SalonHorarioOperacionControllerTest | 31/0/0/0 |
| target/surefire-reports/TEST-com.feelingpilates.ubicaciones.dominio.CoberturaVigenciaTest.xml | ee5df0ebdb40a0d087541a0bbeede8c66a3d2b2ffe6b90b5eee9fcf0fef47fae | com.feelingpilates.ubicaciones.dominio.CoberturaVigenciaTest | 18/0/0/0 |
| target/surefire-reports/TEST-com.feelingpilates.ubicaciones.dominio.DiaSemanaOperacionTest.xml | 391a088f2dd885d9ccae8ba122023c80dbefd82f0989bafc6a0d82e11253b02e | com.feelingpilates.ubicaciones.dominio.DiaSemanaOperacionTest | 7/0/0/0 |
| target/surefire-reports/TEST-com.feelingpilates.ubicaciones.dominio.RangoVigenciaTest.xml | 13f9fac67e21f44e55d82d46264bafd402ea63aaa6f3b35dfbf009c404d47824 | com.feelingpilates.ubicaciones.dominio.RangoVigenciaTest | 14/0/0/0 |
| target/surefire-reports/TEST-com.feelingpilates.ubicaciones.repositorio.HorarioOperacionRepositoryTest.xml | cae14a11036574e9c0d158e50fc453f2309b10db5521bb6e94568d7fac0f6f3f | com.feelingpilates.ubicaciones.repositorio.HorarioOperacionRepositoryTest | 14/0/0/0 |
| target/surefire-reports/TEST-com.feelingpilates.ubicaciones.servicio.CerrarHorarioOperacionTest.xml | 547fc8729de96d377086bf67a596e14fb7371ba57669c47ecd5433d299768ae2 | com.feelingpilates.ubicaciones.servicio.CerrarHorarioOperacionTest | 15/0/0/0 |
| target/surefire-reports/TEST-com.feelingpilates.ubicaciones.servicio.ConflictoExcepcionHorarioTranslatorTest.xml | d743843488f82e4b749ef9ef0bb573937fd6d000bc914a43b8bec4a390367230 | com.feelingpilates.ubicaciones.servicio.ConflictoExcepcionHorarioTranslatorTest | 8/0/0/0 |
| target/surefire-reports/TEST-com.feelingpilates.ubicaciones.servicio.ConflictoVigenciaHorarioTranslatorTest.xml | 5bf66361dc3966b283aa7f2fc547353e51169c298a4dbec5b9eaeace609c2754 | com.feelingpilates.ubicaciones.servicio.ConflictoVigenciaHorarioTranslatorTest | 8/0/0/0 |
| target/surefire-reports/TEST-com.feelingpilates.ubicaciones.servicio.HorarioEfectivoSalonTest.xml | 2d0ad3668610c931be4f3e4574a1a85b444bfecc5f8b4c21898471229ea3a9b8 | com.feelingpilates.ubicaciones.servicio.HorarioEfectivoSalonTest | 10/0/0/0 |
| target/surefire-reports/TEST-com.feelingpilates.ubicaciones.servicio.HorarioOperacionErroresTest.xml | d157eb59255610fb7715476289a0bba626a2478b7ced350fc5c9deeaf692b81c | com.feelingpilates.ubicaciones.servicio.HorarioOperacionErroresTest | 5/0/0/0 |
| target/surefire-reports/TEST-com.feelingpilates.ubicaciones.servicio.HorarioOperacionResolverPersistenciaTest.xml | b80ffd6974c49e6445b3677cc8c757476b913ddc4a0ad495449be88ecb30bfe1 | com.feelingpilates.ubicaciones.servicio.HorarioOperacionResolverPersistenciaTest | 1/0/0/0 |
| target/surefire-reports/TEST-com.feelingpilates.ubicaciones.servicio.HorarioOperacionResolverTest.xml | 32c761691a8daa250f1ee25a61577336f7e3b30ef6a92ba393fea8af0be188a2 | com.feelingpilates.ubicaciones.servicio.HorarioOperacionResolverTest | 5/0/0/0 |
| target/surefire-reports/TEST-com.feelingpilates.ubicaciones.servicio.SalonHorarioExcepcionServiceTest.xml | fef84583528105b98278c1e041444aa8742c21d795e8668fefb07e03255c27f5 | com.feelingpilates.ubicaciones.servicio.SalonHorarioExcepcionServiceTest | 39/0/0/0 |
| target/surefire-reports/TEST-com.feelingpilates.ubicaciones.servicio.SalonServiceTest.xml | e64e7309e2433674856f44384ada3b8e908209b710356501b71be3ff8ce6d62d | com.feelingpilates.ubicaciones.servicio.SalonServiceTest | 18/0/0/0 |
| target/surefire-reports/TEST-com.feelingpilates.ubicaciones.servicio.VersionarHorarioOperacionTest.xml | eca4e6cd6fa1963309aba6235cb1caf7926b98db1b6b9ee50ba3044ef64b6ecf | com.feelingpilates.ubicaciones.servicio.VersionarHorarioOperacionTest | 20/0/0/0 |

RAW90 = cuatro logs +86 XML full, todos byte-identical al certificado EntryTask `task_8020d35c11af`, result.raw90EvidenceFileSHA256; mapa90 completo recuperable por task-list de `run_6859a7f36296`. Digest path+NUL+SHA+LF `1c2cd3c9333946254bb96b787fe24f45dd4b8759933458ba56c669789509b173`. Los cuatro logSHA se conservan literalmente arriba en rootStructuredPhaseCapture. No builds/tests/reaper/HostValidator ejecutados o reactivados por el escritor; auditor tampoco ejecutó tests ni mutation testing.

## 6. Independencia y alcance

PN13 permanece MATERIALIZED / ACCEPTED / PUBLISHED / CLOSED / TERMINAL; PN14 contrato
ACCEPTED, Slice1 ACCEPTED / PUBLISHED / CLOSED / TERMINAL. NEW-PN13-017 sigue OPEN / P2 /
EDITORIAL / NON_BLOCKING / IMPLEMENTATION_INDEPENDENT; es el único residual combinado,
sin fix ni reapertura. No nuevo producto, pago/acreditación/derechos/ledger/settlement,
regla de estado/transferencia/refund, API, dependencia, reader/writer switch, live backfill,
activación productiva, fence, cutover, integración/inspección de candidatos F2E o slices3–12.
No handoff, task ni autorización Slice3. Final de este Run: STOP / HUMAN_GATE_MILESTONE_COMPLETE;
ninguna continuación funcional automática.

DOCUMENTATION_GATE / PUBLICATION_GATE / PUBLICATION_CLOSURE_GATE: PENDING en este corte. Este review no audita al writer ni concede publicación. Launch writer requested/effective model=null, effort=null (default), provider observado codex/gpt-5.6-sol por worker-show; esfuerzo observado UNKNOWN.
