# F2E R4 — recibo factual de auditoría independiente de diseño

Run `run_477ea987415a`; Task `task_b31cab48b966`; Dispatch `ctx_876fa9a64312`.
Auditor nuevo independiente, separado del autor y preflight, sin correcciones ni subagents.
APPROVED DESIGN ONLY; P0/P1/P2 **0/0/0**; HUMAN_GATE NONE.
Candidato `43248a657f2d860c82ce24c1f362350af08215e7`; diseño SHA256 `d966e32318c10b5fa6226f17f63001d71fbbe24419d7dee70842a97f9c7204d4`.
Este recibo es hijo directo del candidato sin cambiar sus bytes.
Informe externo local retenido por RUN_COORDINATOR, sin backup externo garantizado:
`/Users/jesusaldaircruzortiz/.codex/feelingpilates-evidence/run_477ea987415a/r4-design-audit1.md`, SHA256 `86d45b3afa17fed85c5ddd7677844c43d2cc1cf6bfd92593b1e5d07844e5ced2`.
El texto completo del informe se reproduce abajo sin alteración; el archivo externo conserva su hash propio.
Publicación/merge y cierre de proceso son posteriores; este audit no implementa ni activa R4 ni instala esquema.

---

# Independent R4 design audit — APPROVED

VERDICT: **APPROVED (DESIGN ONLY)**

P0: **0** — no findings.
P1: **0** — no findings.
P2: **0** — no findings.
HUMAN_GATE: **NONE identified for approval of these design bytes**.
Corrections: none requested or performed; P2 2 / P1 1 budgets remain unused. This audit grants no budget expansion.

Task task_b31cab48b966; Dispatch ctx_876fa9a64312; independent dispatched auditor, not author/corrector; no subagents. Scope is the committed candidate resolved from HEAD, not implementation acceptance, schema installation, publication, or process closure.

## Exact candidate and repository evidence

- Checkout: `/Users/jesusaldaircruzortiz/orca/workspaces/feelingpilates/f2e-clean-main-reconciliation`.
- Baseline HEAD and final HEAD: `43248a657f2d860c82ce24c1f362350af08215e7`.
- Canonical base: `5c8a9ab165a1852652ba0e01d06ff65ef6bc24c7`.
- Baseline and final `git status --porcelain=v1`: empty; final includes all untracked files. Unstaged/index diffs empty.
- Candidate: `auditoria/fase-2e-r4-diseno-reader-ajustes-fecha-reconciliado.md`, 145 lines.
- Baseline and final candidate SHA256: `d966e32318c10b5fa6226f17f63001d71fbbe24419d7dee70842a97f9c7204d4`.
- All committed changed paths base→candidate: **only that candidate document**, added, 145 lines. No source, migration, sealed authority, test, routing, or configuration delta.
- HEAD tree: `25315f1c71af41944dd45dcabca8229482bcf05e`; migration tree `7813346bbf81ce6665d45021c9b9b145782b5523`; programacion source tree `86c1e377726ec96ad6ead16fd717ceec4cf48ad0`.
- Final independently hashed 420 tracked files: sorted UTF-8 `SHA256  path LF` manifest digest `cec92b0c202689f81a10a8f0e7bff4428753fd0263b82837e76bc41ce4cf2467`. Working bytes equal committed bytes except checkout-normalized `mvnw.cmd`: `git check-attr` reports text=set/eol=crlf, and CRLF→LF matches HEAD exactly. Working SHA256 `46eedb8419bd14fe70d5bb2916d7b6f51806e51b39d5b76a42610384ca929c1c`; committed SHA256 `4a361e1374a3e5ad6d03e18e9adc0cf181ac5058ac6203b76f0ba3b456b56481`. This is not an audit mutation. No whole-tree baseline manifest was recorded; baseline clean Git status/HEAD and final content comparison are the preservation evidence.

The sole auditor-created path is this external report. No repository or DB writes, migrations, build/tests, fetch, publication, or implementation occurred. No DB connection was opened; repository schema facts do not establish physical production DB state.

## Citation key

All current citations resolve at the exact candidate HEAD above.

- **C**: `auditoria/fase-2e-r4-diseno-reader-ajustes-fecha-reconciliado.md`.
- **D**: `auditoria/fase-2e-diseno-adapters-read-only-snapshot-consistency.md`.
- **S**: `auditoria/fase-2e-identidad-semantica-detector-read-only.md`.
- **N**: `auditoria/fase-2e-r3-diseno-reader-programacion-nominal-reconciliado.md` (sections named below).
- Java paths abbreviated below are under `src/main/java/com/feelingpilates/`; test owner under `src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/testinfra/`.
- **H**: Git object `95900d8a1d787a24aff4ee4e10f69d540ce81339:src/main/resources/db/migration/V47__programacion_ajustes_fecha.sql`, read using git show without materialization.

## Independent challenge results

1. **Source/schema prerequisite — PASS.** Independently enumerated 49 migration SQL files ending V46 and searched current migration and Java source for adjustment table/entity/port, finding none. V41 `V41__programacion_bloque_asignacion.sql`:4–48 creates nominal blocks/assignments; V46 `V46__horario_operacion_drop_unique_dia.sql`:9–10 changes salon hours, not adjustment persistence. H:4–29 installs assignment overlap preflight/exclusion; H:31–96 separately defines adjustments, shape checks, result FKs and indexes, without dependency on that exclusion. C:24–43 selects only the twelve adjustment fields, necessary integrity and date access contract, excludes dimensional indexes, arbitrary version FK, overlap exclusion, triggers/backfill/repair and the bundle. The date index is a bounded access requirement rather than a claim that SQL cannot execute without it. No migration version/checksum is invented; independent schema authority is required first (C:26,126,143). Minimal means this reader's selected persistence contract, not restoration of every historical effect.

2. **Concept versus entity — PASS.** Current source inventory lacks AjusteProgramacionFecha. Historical entity at H's commit (`programacion/entidad/AjusteProgramacionFecha.java`:19–26,93–111) is managed and mutable; historical repository extends JpaRepository and has broader queries. C:45 distinguishes the conceptual shape from current authorized persistence and refuses entity/repository resurrection. Current-snapshot-only evidence cannot prove date immutability; the writer identity rule remains separate. This is consistent with S:665–674 and does not silently relax writer authority.

3. **Exact-date port/SQL/types/all fields — PASS.** C:53–75 specifies the exact context/date operation, immutable sources/backing, twelve scalar columns, date/active named bindings, and deterministic date/unsigned UUID order. D:403–442 and D:538–661 provide the same projection-first/date-only contract and typed NativeQuery scalar mechanism. Current `transicion/programacion/adapter/jpa/projection/NominalProjectionQueryExecutor.java`:25–31 physically uses that binding mechanism. R4 has no range, target list, origin-salon filter, join, winner query, or fallback schema. All physical fields have corresponding PostgreSQL/Java types and form-specific null rules; precision and scalar validation precede output. Current R3 code is evidence of available vocabulary/mechanism, not acceptance of a future R4 executor.

4. **Three source mappings/identity/provenance — PASS.** C:79–91 maps exactly NEW_CANCELACION, NEW_REEMPLAZO, NEW_ADICION with NEW_DARK_LAUNCH. Actual `detector/DetectorVocabulary.java`:8–18 and `DetectorValidation.java`:39–54 admit these combinations; `GenericSourceSnapshot.java` and `EvidenceProvenance.java` provide the actual immutable maps/string identity and provenance carriers. Actual `programacion/dominio/ReferenciaOcurrencia.java`:6–9 defines SERIE_ASIGNACION and AJUSTE. Cancellation/replacement target series/date; addition id/date; no synthetic series or nominal physical ID claim. C:79–83 includes all fields, explicit null token, real schema descriptor, record ID and rules, framed hashes with content/execution separation. `read/ReadSnapshotIdentifiers.java` actually exposes secuenciaTextos/mapaCanonico with length framing and canonical byte ordering. R4 does not substitute the core semanticHash for its stated fingerprint. S:665–710 independently confirms identity, expected outcome and legacy/operation separation.

5. **Cardinality, duplicates, failures and no partials — PASS.** C:47,60,75,111–122 requires validation of all rows, unique physical IDs, unique targeted active adjustment per series/date, strict order, bijective backing and all-or-nothing output. Two distinct additions with identical dimensions stay separate. Missing nominal target is not an R4 database error; it belongs to future semantic classification (C:91; S:665–674). Zero is valid only after completed successful execution; missing schema/null list/privilege/query failure is never empty success. Failure categories preserve cause/SQLState; no deduplication, repair, retry or latest-record selection. Separate negative duplicate fixtures avoid the unreasonable requirement to insert forbidden duplicates into a schema whose constraints remain intact (C:134).

6. **Transaction authority/ownership/completion — PASS.** C:95–107 defines R4's own context/claim, manager-qualified proxied MANDATORY reader, separate test-only REQUIRES_NEW/RR/read-only owner, private reservation, real resource identity and initial/final PostgreSQL probes. Actual current `adapter/jpa/NominalJpaReader.java`:25–58 verifies qualified propagation, RR/read-only/synchronization/proxy and context authority; `NominalTransactionTestOwner.java`:41–48,66–93,95–155 demonstrates separate owner, observed resource/capture checks and completion guards. N TRANSACTION_SNAPSHOT_CONTRACT and D:1055–1140 independently reserve multi-reader ownership for R6. C:101 explicitly covers pre-body manager/proxy exception normalization rather than relying on annotation alone. Final capture verification makes the context commitment an expected commitment bound to subsequently observed SQL/binds; context construction alone is expressly insufficient. Provisional read sets cannot become successful caller results until owner completion; rollback/commit failure discards them (C:105,136). No ownership or claim is imported by extending sealed R1–R3 types.

7. **No-write/PostgreSQL/host acceptance — PASS as future design obligations.** C:126–137 requires integrated approved schema, real PostgreSQL, SELECT-only role, same owner/executor resource, default-deny SQL/bind capture, before/after evidence, sensitivity and RR concurrency proofs, isolated negative fixtures, no entity escape/flush/runtime reachability and REQUIRED real HostValidator. D:1780–1787 independently mandates the host gate. Privilege 42501 tests are explicitly separated from read-only 25006 tests; checksums during concurrent writing are not falsely demanded globally stable. Absent host blocks future implementation acceptance, not this documentation-only audit. Exact catalog/probe SQL, manifests, wiring, resource descriptor and checksum are mandatory future handoff inputs, not falsely asserted available now (C:73,105,126).

8. **R3/R5/R6/product boundaries — PASS.** N R3_V47_DECISION and BOUNDARIES reserve nominal reads/duplicate rejection to immutable R3 and adjustments to R4; D:550–579,1055–1104 separates fact reads, effective composition and multi-reader coordination. C:91,107,137,143–145 forbids R4 effective candidates/omissions/DetectorResult, R3 business calls, R5/R6 wiring or productive activation. Actual `calendario/servicio/ReservaService.java`:124–141 still uses legacy TurnoInstructor. The candidate-only delta proves these current source and sealed authority bytes were not changed. No claim is made about deployed runtime state.

## Authority and remaining gates

The preflight file `R4-PREFLIGHT-INDEPENDENT.json` was inspected only as provenance of the separately settled necessity audit (task_740b849fa904). Its prior APPROVED 0/0/0 is not evidence approving this candidate; the results above come from fresh current source, authority sections, Git inventories and historical objects. Author status lines and validation JSON are not implementation proof. Historical R3 document candidate status is interpreted through its later closure evidence and canonical integrated base, not rewritten retroactively; no R3 closure audit is reopened here.

No internal material contradiction, genuinely ambiguous authority, unreasonable acceptance requirement or missing material design decision was found. Remaining exact SQL catalog/probe/hash vectors, migration version/checksum, resource wiring and allowlists are expressly blocked until their distinct authority/handoff gates; they are not permission to invent these during implementation. C:141–145 cleanly separates design publication/merge/process closure, schema prerequisite authority and later R4 handoff. This APPROVED verdict authorizes neither migration nor implementation nor publication by this worker.

Tests/build/DB verification: **NOT_APPLICABLE — documentation-only independent design audit**. No technical PASS claim for future R4 is made. Repository preservation and exact-byte binding are read-only audit checks, not test execution.

Next work belongs to the coordinator: factual audit receipt and authorized design lifecycle publication/integration/closure, followed by MINIMUM_R4_SCHEMA_PREREQUISITE_AUTHORITY before a separate R4 implementation handoff. R1–R3 remain closed/immutable; dark launch and TurnoInstructor productive authority remain preserved; R4–R6 activation remains unauthorized.
