# F2E R4 — minimum persistence prerequisite, reconciled schema authority

Status at capture: AUTHORIZED_SCHEMA_CANDIDATE / NOT_ACCEPTED / NOT_PUBLISHED.
This is a separate schema lifecycle under the current explicit human instruction.
R1-R3 CLOSED and the reconciled R4 reader design CLOSED; reader R4 and R5-R6 NOT_AUTHORIZED.
No deployment, cutover, writer, import/backfill or productive routing is authorized.

## Canonical reconstruction and ownership

The canonical design is auditoria/fase-2e-r4-diseno-reader-ajustes-fecha-reconciliado.md §2,
SHA256 d966e32318c10b5fa6226f17f63001d71fbbe24419d7dee70842a97f9c7204d4.
It separates adjustment persistence from historical V47 assignment exclusion. Historical migrations
are provenance only and remain byte-identical. Original closed main37e7100 has49 SQL, max46.
Independent native authority preflight task_536c94cd510c/ctx_83633d620448 verified the contract
and closure; its one P1 concerned default-suite fixture ownership, not schema semantics.
Human gate gate_ae89151aa63d is explicitly AUTHORIZED for exactly four existing test paths,
with no guard weakening. The separate four-file reconciliation is AUDITED/PUBLISHED/INTEGRATED/CLOSED by PR #25,
merge b21f5518dc5956fedef9ba041ac8bece8d192d54, candidate cee5e45fd0bd6a83d28fe3858e69b17925cb5ce2.
Fresh GPT-6.1-Sol High audit task_c243e33fd9b9/ctx_c2510101c9d8 APPROVED 0/0/0,
independent550 tests0/0/0 and46 adversarial guard checks. Full factual audit receipt is
published in PR #25 body to preserve exactly four repository paths.
This schema candidate base is exact clean live main b21f5518dc5956fedef9ba041ac8bece8d192d54.
The current inventory remains49/max46; V47 was rechecked absent immediately before creation.

The Flyway next number must be rechecked against fresh live main, including other lanes.
The only production delta is src/main/resources/db/migration/V47__programacion_ajuste_fecha.sql;
no existing migration, Java runtime, API, repository, entity, config or dependency changes.
PostgreSQL validation stays in the already authorized existing fourth test path:
src/test/java/com/feelingpilates/programacion/ProgramacionPersistenciaTest.java.
This schema-specific test delta is separate from the frozen fixture-compatibility candidate.
There is no fifth test path. Process documentation and receipts have a separate original
schema-lifecycle authority; they do not expand the four-file reconciliation scope.

## Exact minimum schema

One empty table public.programacion_ajuste_fecha with exactly twelve columns in this order:
id UUID NOT NULL PK DEFAULT gen_random_uuid(); tipo VARCHAR(16) NOT NULL; fecha DATE NOT NULL;
asignacion_serie_id UUID nullable; salon_resultado_id UUID nullable FK salon(id);
instructor_resultado_id UUID nullable FK usuario(id); tipo_actividad_resultado_id UUID nullable FK tipo_actividad(id);
hora_inicio_resultado/hora_fin_resultado TIME WITHOUT TIME ZONE nullable;
activo BOOLEAN NOT NULL DEFAULT true; creado_en/actualizado_en TIMESTAMPTZ NOT NULL DEFAULT now().
Technical defaults follow V41 and are permitted by closed R4 §2. They create no mutation,
ordering, validity or automatic-update policy. PostgreSQL microsecond precision is retained.
FKs use default validated/nondeferrable MATCH SIMPLE, UPDATE/DELETE NO ACTION: no cascades.
Referenced result masters may consequently reject deletion/update while referenced;
this necessary referential impact is part of the design, not productive activation.

Closed types CANCELACION/REEMPLAZO/ADICION. Shape check explicitly tests every null:
CANCELACION target required/all five result fields absent; REEMPLAZO target plus all results
required; ADICION target absent/all results required. Forms apply to inactive rows too.
Positive time check (both absent OR end>start) together with shape check rejects partial results,
zero/reversed/overnight ranges. No nominal-row FK: logical multiversion series existence is
future reader/evaluation authority. A logically orphan target is valid persistence here.
One partial unique btree index (asignacion_serie_id,fecha) WHERE activo AND tipo IN
(CANCELACION,REEMPLAZO), covering both types together; inactive duplicates allowed.
One partial nonunique btree date index (fecha) WHERE activo. PK supplies its own index.
Identical additions with distinct physical UUIDs remain distinct valid facts.

No assignment overlap exclusion, extension, trigger, backfill, seed, repair, grant,
index by salon/instructor, additional constraints, entity/repository/writer/reader restored.
DDL technical content SHA256: afcd7316ec922e1afd10fa719cd3d26815b62e8b0a46a3641a266df8ee390e06. Flyway checksum is measured only after
actual application, not invented here. SchemaFingerprint/reader descriptor is future R4 work;
V47 schema tests do not satisfy reader RR/SELECT-only/projection/host acceptance.

## Deterministic schema acceptance and closure

Fresh default clean regression must exercise sealed R1-R3 fixtures on their exact V46/49
schemas and global programming/schema tests on complete V1→V47/50. No test skips, no
resource stripping/shadowing, no manual compiled-class changes and no relaxed unknown-version guard.
Prove actual V46→V47 upgrade, zero extra migrations on re-migrate, Flyway validate/checksum
identity for original49, missing-table negative42P01 on V46, and empty adjustment table.
Compare old-table data, column/default/type/nullability metadata, keys/constraints/indexes
and extensions before/after; retain overlapping active assignment versions without exclusion.
Physical metadata must prove exact12 columns, defaults, PK/three FKs/three checks/two partial
indexes/no user trigger; test all three forms and all64 target/result-null combinations per
type with active/inactive cases, SQLStates23502/23514/23503/23505, positive/equal/reversed
ranges, microseconds, technical timestamps without ordering policy, logical orphan targets,
inactive duplicates, cross-type active uniqueness, reactivation/date/target/type changes,
identical additions, other targets/dates, and deterministic concurrent conflict with physical
pg_stat_activity lock observation before competing commit yields23505.
A fresh independent native GPT-6.1-Sol High auditor must bind exact candidate bytes and
observe relevant tests; require P0/P1=0. Bounded corrections consume policy budgets and
require fresh independent re-audit; no fifth test path without HUMAN_GATE.
Publish accepted schema candidate/receipt normally and verify exact live canonical merge,
unchanged audited bytes and allowlist, unchanged historical migrations/runtime and clean checkout.
Separate process closure must not assert a future merge as already done. Final resolution
uses live Git/GitHub plus retained hashed evidence. R1-R3 CLOSED, legacy productive,
dark launch/no cutover and R4-R6 NOT_AUTHORIZED beyond schema prerequisite remain invariant.

## Coordinator execution at candidate capture

Fresh ./mvnw clean test: **555 tests, 0 failures, 0 errors, 0 skipped**, BUILD SUCCESS.
Programming persistence/schema suite:21 tests; fixtures R1-R3 still use49/head46,
while complete application Flyway applies50/head47. PostgreSQL16.14/Testcontainers, Java21.
Physical installed V47 history checksum: **-1624594375**; measured by the upgrade test
from flyway_schema_history, not inferred from a SQL filename or historical migration.
The upgrade test also verifies original49 script/version/checksum identities and old data/schema.
Targeted FAST2:21 PASS. FAST1 had one metadata-query text||PostgreSQL internal-char
ambiguity; explicit casts corrected only the test query. SQL migration bytes unchanged;
raw FAST1/FAST2 logs retained. No published candidate correction/audit budget was used.
Fresh independent exact-byte schema audit is PENDING; these are coordinator measurements,
not independent acceptance, publication, integration or closure.
Evidence root: /Users/jesusaldaircruzortiz/.codex/feelingpilates-evidence/run_6c6245bb15ba/,
local retained by RUN_COORDINATOR with no guaranteed external backup.
