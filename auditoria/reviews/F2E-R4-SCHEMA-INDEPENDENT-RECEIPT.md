# F2E R4 — factual independent minimum schema acceptance receipt

APPROVED SCHEMA PREREQUISITE ONLY; P0/P1/P2=0/0/0.
Exact audited candidate b178288d718c281ea2ee127581cbb2bbf5cf3722.
Fresh native Task task_3446ce7ae962 / Dispatch ctx_12758fab9195, requested/effective GPT-6.1-Sol High.
Original report SHA256 2c218180f330e2d6f332f7814a70ebdcaf62dbf90840d5edbd6e7132af396720. Full report reproduced below.
This factual receipt is the sole delta in a direct child of that candidate; no tested bytes change.
Publication/merge/process closure remain separate pending transitions. No reader/activation/deploy claim.

# Independent minimum R4 schema prerequisite acceptance audit

**Verdict: APPROVED SCHEMA PREREQUISITE ONLY. P0=0, P1=0, P2=0. No HUMAN_GATE is required for this exact candidate.** Publication, integration and separate process closure remain pending; this audit neither performs nor claims those transitions.

Run run_6c6245bb15ba; task task_3446ce7ae962; dispatch ctx_12758fab9195; fresh auditor term_a7f24f5d-355b-464a-b979-b29766ca5527, distinct from coordinator/author term_46731b4b-6388-4ff9-846c-53ca94d2935f. No delegation. Requested **codex/gpt-6.1-sol/high**, effective **codex/gpt-6.1-sol/high**, observed provider projection **codex/gpt-6.1-sol** and terminal **GPT-6.1-Sol high**. SCHEMA-AUDIT-START.json and freshly retained worker-show/terminal observation independently bind these facts to the current task, dispatch and workspace; model identity is not itself acceptance evidence.

## Exact candidate, scope and preservation

Repository /Users/jesusaldaircruzortiz/orca/workspaces/feelingpilates/f2e-clean-main-reconciliation; branch AldairCruz7/f2e-r4-schema-prerequisite. Exact full committed candidate **b178288d718c281ea2ee127581cbb2bbf5cf3722** has immediate parent/canonical clean-main base **b21f5518dc5956fedef9ba041ac8bece8d192d54**. Initial and final HEAD equal the candidate, tracked/index/untracked status is clean, base-relative and cached diff checks pass. Nonmutating live main resolves b21f551 at both captures; schema branch is not yet published at the live-ref capture.

| Candidate path | SHA256 of full committed and working bytes |
| --- | --- |
| auditoria/fase-2e-r4-prerrequisito-esquema-reconciliado.md | f095844d82e6e1b091d02ef914035cc0ac0b984a8069f197f6ffb1f656b4623c |
| src/main/resources/db/migration/V47__programacion_ajuste_fecha.sql | afcd7316ec922e1afd10fa719cd3d26815b62e8b0a46a3641a266df8ee390e06 |
| src/test/java/com/feelingpilates/programacion/ProgramacionPersistenciaTest.java | 0c846a24d982f3a2673525c9c59b0acaa07e813ceebe56524e7987a80f8d8cf3 |

The exact three-path diff matches external SCHEMA-ALLOWLIST.json and SCHEMA-CANDIDATE.tsv. Two new files and one additive existing test modification; 425 tracked files versus 423 at base. Only the production V47 path changes production resources. The test delta adds DataSource access, five schema tests and their helpers inside the already authorized fourth ProgramacionPersistenciaTest path; existing methods remain unchanged. No fifth test file is created. All **422 other baseline working-byte members** match SCHEMA-BASELINE.tsv; all **49 historical migration blobs** independently match base and candidate. Runtime Java, three reconciled fixture configurations, other tests/config/dependencies and sealed R1-R3 authorities remain identical. The repository stayed read-only; all test/build outputs are external. Git archive's mvnw.cmd CRLF conversion is the sole attributed archive/blob normalization under unchanged .gitattributes, unused on this platform; every other archived source byte matches its committed blob before and after execution.

## Fresh authority reconstruction and ownership

Read the full candidate authority document, migration and ProgramacionPersistenciaTest source, closed R4 design, current runbook/policy, V41 default convention, schema preflight and checkpoint, fixture approval/GATE/closure evidence, manifests and coordinator validation artifacts. Authoritative schema semantics are exclusively closed R4 design §2, SHA256 **d966e32318c10b5fa6226f17f63001d71fbbe24419d7dee70842a97f9c7204d4**, plus explicit user-authorized minimum schema lifecycle. Historical NOT_AUTHORIZED design captures govern their historical design-only scope and do not revoke this later schema authorization. Historical V47 bundles, reader/entity/repository code and assignment exclusions supply no authority here.

Live PR24 reports MERGED, exact merge37e7100cd35b522f6bae8e41a6960f04291c22c4, head0d6ff7033d4d714f616707cc4379a520e47f69f8; this closed design process precedes PR25. Live PR25 reports MERGED at2026-10-04T01:07:22Z, exact candidate **cee5e45fd0bd6a83d28fe3858e69b17925cb5ce2**, merge **b21f5518dc5956fedef9ba041ac8bece8d192d54**. Its body contains the complete independent fixture receipt, task_c243e33fd9b9/ctx_c2510101c9d8 APPROVED0/0/0, preserving exactly four repository test paths. Native worker-show confirms that dispatch succeeded/settled with outcome succeeded. Fresh gate-list returns gate_ae89151aa63d resolved AUTHORIZED for exactly those four paths, preserved fail-closed guards and no reader/R5/R6 activation. Thus the old preflight P1 fixture-ownership conflict is resolved; its old HUMAN_GATE is preserved as history, not re-raised. Current schema validation remains additive inside the existing authorized fourth file.

Canonical base inventory independently has49 versioned SQL, max46, no version47 file. The precreation preflight/checkpoint records V47 absent and the actual candidate parent is that same freshly verified live base; no preexisting47 is replaced and no historical migration is rewritten. Existing other-lane V48/V49 mentions in policy are COORDINATION_REPORTED/NOT_INTEGRATED rather than current canonical migrations. Live main still has no47 at the audit capture. Recheck canonical live main/version availability before publication/integration; movement requires fresh binding and compatibility assessment.

## Independently rederived contract

| Column | PostgreSQL type | Nullable | Default |
| --- | --- | --- | --- |
| id | UUID, physical primary key | no | gen_random_uuid() |
| tipo | VARCHAR(16) | no | none |
| fecha | DATE | no | none |
| asignacion_serie_id | UUID | yes, form constrained | none |
| salon_resultado_id | UUID, FK salon(id) | yes, form constrained | none |
| instructor_resultado_id | UUID, FK usuario(id) | yes, form constrained | none |
| tipo_actividad_resultado_id | UUID, FK tipo_actividad(id) | yes, form constrained | none |
| hora_inicio_resultado | TIME WITHOUT TIME ZONE, precision6 | yes, form constrained | none |
| hora_fin_resultado | TIME WITHOUT TIME ZONE, precision6 | yes, form constrained | none |
| activo | BOOLEAN | no | true |
| creado_en | TIMESTAMPTZ, precision6 | no | now() |
| actualizado_en | TIMESTAMPTZ, precision6 | no | now() |

Exactly12 ordered columns, one nondeferrable UUID PK, three validated/nondeferrable result-master FKs with MATCH SIMPLE and UPDATE/DELETE NO ACTION, and three checks. Types are exactly CANCELACION/REEMPLAZO/ADICION. Cancellation requires series and all five result fields absent; replacement requires series and all five present; addition requires series absent and all five present. Explicit IS NULL/IS NOT NULL predicates with NOT NULL tipo prevent SQL UNKNOWN from admitting partial shapes. The positive-range check, combined with the shape check, allows cancellation's absent interval and requires end>start for replacement/addition; equal/reversed/overnight intervals reject. Forms apply to inactive rows too.

One unique partial btree on (asignacion_serie_id,fecha) WHERE activo AND tipo IN ('CANCELACION','REEMPLAZO') couples both target types. One nonunique partial btree on fecha WHERE activo supports future exact-date access; the PK has its own index. Inactive duplicates and identical additions with distinct physical IDs remain valid. No FK to a nominal assignment version/series; logical orphan targets are valid persistence. No timestamp ordering/mutation policy: reversed technical timestamps are valid, defaults match V41 convention allowed by §2, and no automatic update trigger exists. Result-master delete/key-update rejection is the intended FK referential impact.

Exactly one empty additive table; no assignment exclusion, ALTER to old tables, extensions, grants, triggers, seed/backfill/repair, index by salon/instructor, entity/repository/writer/reader, productive activation or cutover. R4 reader/descriptor/SELECT-only/RR/host acceptance is future separately authorized work and is not satisfied by this schema audit.

## Fresh self-contained technical acceptance

An untouched git archive of exact candidate b178288 created **/Users/jesusaldaircruzortiz/.codex/feelingpilates-evidence/run_6c6245bb15ba/schema-audit1-ctx_12758fab9195/source**, initially without target output. Java21.0.11, Docker29.6.1, Testcontainers1.21.3 and real **PostgreSQL16.14**, postgres:16-alpine. Source is unedited throughout. No classpath stripping/shadowing, compiled-class mutation, test exclusion, skip or migration repair. Both commands independently freshly clean and compile the candidate:

1. `./mvnw clean test -Dtest=ProgramacionPersistenciaTest`: exit0, **21 tests,0 failures,0 errors,0 skipped**, BUILD SUCCESS, completed2026-10-03T19:13:49-06:00. Targeted Surefire XML was copied externally before the subsequent clean.
2. Default `./mvnw clean test`: exit0, **555 tests,0 failures,0 errors,0 skipped,58 suites**, BUILD SUCCESS, completed2026-10-03T19:15:25-06:00. Totals independently summed from freshly retained XML agree with Maven output. Programming persistence suite is21 (16 prior fixture/programming tests plus five added schema tests), not21 newly introduced schema tests. R1-R3 regression executes against their approved head46/49 schemas while the complete application uses actual head47/50.

The following are actual assertions freshly executed in both runs, with exact source, testcase names, XML, full logs and checksum stdout retained:

- `r4EsquemaFisicoTieneSoloElContratoMinimo`: physical information_schema/pg_constraint/pg_index/pg_trigger verify all12 names/types/nullability/defaults, varchar16, precision6, exact PK/3FK/3CHECK inventory and validated/nondeferrable states; FK targets/columns and NO ACTION/SIMPLE actions; exactly PK plus two valid btrees, keys, uniqueness and predicates; no user trigger and no assignment exclusion.
- `r4TresFormasRechazanTodaCombinacionParcialInclusoInactiva`: **384 real SQL insert attempts** =3 types×2 active states×64 target/result null masks, each isolated by savepoint. Expected valid masks cancellation1/replacement63/addition62, hence6 successful valid shapes and378 rejected23514. No mocked schema or permissive negative fixture replaces actual DDL.
- `r4ChecksNotNullPkFkYPrecisionSePruebanEnPostgresql`: each of six NOT NULL fields rejects23502; unknown/case/space-modified types and equal/reversed ranges reject23514; each missing result-master FK rejects23503; duplicate physical ID rejects23505. Microsecond time/timestamptz values round-trip exactly, including allowed reversed technical timestamps. Default UUID uniqueness, activo=true and both now() timestamps equal transaction_timestamp() are physically checked. Default cancellations use arbitrary logical series without a nominal FK.
- `r4UnicidadParcialCubreTiposActivacionCambiosYAdicionesIdenticas`: active cancellation/replacement and cancellation/cancellation conflict23505; two inactive duplicates succeed; inactive→active conflict, date change, series change and addition→replacement transition conflict23505. Different date/series succeeds, deactivation frees target and one inactive target can then activate. Identical additions with different IDs both persist.
- `r4UpgradeV46PreservaHistoriaDatosEsquemaYSerializaTargetConcurrente`: separate fresh PostgreSQL starts with actual49/head46 and validates; absent table query rejects42P01. Inserts old-master/block data and **two overlapping active versions of one assignment series**, captures all old-table rows, columns/default/type/nullability, constraints/indexes and extensions. Actual V46→V47 executes **exactly one** migration, validates, has50/head47/zero pending; every original49 version/script/checksum is equal; remigrate executes0 and validates. Old data/schema/extensions match exactly, the new table is empty, and assignment exclusion remains absent. Result-master delete and PK-update attempts reject23503. In two independent real connections, competing target insert is observed via **pg_stat_activity.wait_event_type='Lock' before first commit**; first commits, competing insert returns23505 and exactly one target row persists. A bounded physical-lock assertion prevents a sleep-only concurrency claim.

Fresh complete V1→V47 runs independently prove the full application migration chain; the upgrade test separately proves V46→V47 history preservation. In both targeted and default runs installed flyway_schema_history stdout is **R4_SCHEMA_INSTALLED_FLYWAY=V47__programacion_ajuste_fecha.sql:-1624594375**, physically measured rather than inferred. SQL SHA256 **afcd7316ec922e1afd10fa719cd3d26815b62e8b0a46a3641a266df8ee390e06** matches the exact candidate. Coordinator SCHEMA-REGRESSION.log and SCHEMA-VALIDATION-SUMMARY.json agree555/0/0/0,21 and checksum; inspected and hashed, never substituted for these independent fresh executions. Prior fixture/baseline tests supply authorization provenance only and are not actual V47 proof.

## Numbered findings and decision

1. **P0: none (0).** No unbounded authority/ownership conflict, scope breach or productive activation.
2. **P1: none (0).** Exact design/schema and approved fixture compatibility are supported by current actual clean/upgrade/default PostgreSQL regression. No bounded correction requested.
3. **P2: none (0).** Historical nonblocking evidence/process findings remain preserved provenance; no new finding or silent repair.

**APPROVED SCHEMA PREREQUISITE ONLY**, because P0=P1=0 and current exact-byte acceptance passed. `humanGateRequired=false`, `scopeExpansionRequired=false`, `requires_human_decision=false`, `p1_correctable=false`, `corrective_artifact=null`; schema correction cycles used by this audit0. The earlier separate documentary closure P1 budget remains1/1 and is not reset. Any later changed schema/test/authority byte requires fresh relevant validation and independent re-audit; out-of-scope/ambiguous authority requires the competent HUMAN_GATE, not an automatic expansion.

R1-R3 remain CLOSED; legacy TurnoInstructor productive status and dark launch/no cutover are preserved by unchanged code/authority, without claiming observation of production runtime. No production DB/deploy or future reader claims. What remains for coordinator: factual acceptance receipt/publication under the original separate schema lifecycle, normal merge verified against live canonical main with exact audited bytes and scope, then separate fresh process closure audit and final live verification. No schema PR publication/merge or lifecycle closure has occurred at this capture; approval is not false closure or permission for R4 reader/R5-R6.

## Durable RAW and SUMMARY evidence

Repository writes: none. Authorized external report is /Users/jesusaldaircruzortiz/.codex/feelingpilates-evidence/run_6c6245bb15ba/schema-audit1.md; unique external evidence root **/Users/jesusaldaircruzortiz/.codex/feelingpilates-evidence/run_6c6245bb15ba/schema-audit1-ctx_12758fab9195**. Retention owner RUN_COORDINATOR, retained locally for this lifecycle, no guaranteed external backup. Source archive, all logs/exit codes, original targeted XML, fresh default XML, native receipts and manifests remain available. Mailbox checked initially, at source/test checkpoints and before completion; no redirect received at report capture. Evidence index hashes every raw artifact and records bytes/location/source reconstruction; it excludes itself and the report to avoid circular hashing. Report hash is returned separately.

| Auditor artifact relative to unique evidence root | SHA256 |
| --- | --- |
| identity.json | 27d4bb1b80d823a2ab97be745573903b47a8dd710bb044e52fa3454b741e09cf |
| final-verification.json | 973de19ae3710fad9e37a1e6893e6861577993035969447b8b80f18233c42a11 |
| candidate.diff | 2580ec74a5fb98031cf493e633425c2f6b1197c0e948d6bb91119ff5677b2f3e |
| validation-summary.json | a26b1c924d7a7fce600a0d6e954a0bdd2253917798a472b1163fe4504c03a839 |
| targeted.log | 0812a17575bad711d405c6db22d691e8645d53c647fa61bf72a3259aee088ed2 |
| targeted.exit | 9a271f2a916b0b6ee6cecb2426f0b3206ef074578be55d9bc94f6f3fe3ab86aa |
| regression.log | ab70e997308e4686e02095db2b84bdc903032213da5de88b4f0190b0507d7645 |
| regression.exit | 9a271f2a916b0b6ee6cecb2426f0b3206ef074578be55d9bc94f6f3fe3ab86aa |
| worker-show.json | 5c3c67d85054c2fd6eb7bd68b63921b3f7536036409d24425cda452712f1bef0 |
| terminal-observation.json | cfbc9efb0ba25a37bbc0d7538eb8190028ad567d39f6ed21f4e1e8dcd3fb9820 |
| live-gates.json | 58c1d44bff2b4ad3db5dfacc54f0cdf0cb31b3ddef67c93229678491cc36a326 |
| live-pr25.json | b81a2d59274aeef812fda9fc4da82714b81a599798900347e5e5ef9a085e0e53 |
| live-pr24.json | d2f906f76df070f3e16dd43065be863b2a0b223ea8ad89d81bc2d9fce927dc93 |
| fixture-audit-native.json | 2ff95292f6c5fb57cb6fb1b7c5227aae1bce88d65723cddde2495b7e908151ec |
| input-hashes.json | 5c54c19a8b3268830a1d9ddae1d0df5a196baf9ab6937790130d0bfbda52b1ce |
| live-main.txt | 8a02ecc7d08d547d6ad497879d1cc812eee32bd00c403784e5829521bda1b250 |

| Inspected coordinator input relative to run evidence root | SHA256 |
| --- | --- |
| SCHEMA-BASELINE.tsv | 84f31867480f642b07ad32c251a253b06b7196c335f8f5231ab54f9d56f36337 |
| SCHEMA-CANDIDATE.tsv | 1196028cbcfd0687a2095495f2f4f1d90e0c397742b63276ed843d88c15b6325 |
| SCHEMA-ALLOWLIST.json | 5b057879ddddbcb3fdf11aec4ddf552bc47c00e0733ea6cda10837b7d2d75737 |
| SCHEMA-VALIDATION-SUMMARY.json | a15ad64312993b7bdee30b0758eb3061601b98a47d7440b589a937ad27b8d989 |
| SCHEMA-REGRESSION.log | defad26d9a49a5e06d9ef6405d05536646daf851c4e7948d3c3fdea7fb90666c |
| SCHEMA-AUDIT-START.json | 1351b3632634c2ba3f2738886dbc0be6fa4543bb9a3e52982595c954460c528f |
| schema-preflight.md | 211f6f0e6ad4635ad2ec0480480ebd5347fc00f1997ba5fda80ef676585a4c87 |
| GATE-AUTHORIZED.json | d2d68ce37d5b7b78fe22e3367638024b2176fb8324b706e9d55c4bb74114811a |
| FIXTURE-CLOSED.json | 71d3e93a065188325cf24a8180e2fa720d51d5cfef469b60ab19e84aa048be47 |
| FIXTURE-PR25-INTEGRATED.json | 29138eb466027d625a08c8d66eb3b99e950d527d3b94f083b346eb0ef64ee4e8 |
| fixture-audit1.md | 13b67b1e67b44976fa9c439da397b934f83c08285bce932377269f79786e8942 |
| MIGRATIONS-BASELINE.tsv | 78a8125fb4f241a4d1937f1e6775ab6e1e189b95d02516d0bb19b3bcbcf32f4b |
| PREFLIGHT-CHECKPOINT.json | 2321c389f125071836e77a47a748cf167d762c04bcf5ec69ec4bfdc8692dc36d |
