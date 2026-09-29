# F2E R3 — candidato de preauditoría para activación del handoff

Status: `MATERIALIZED_CANDIDATE / PENDING_FRESH_INDEPENDENT_AUDIT / NOT_APPROVED / NOT_PUBLISHED`.
Canonical base: `636a08f791197a5537032afb8885dd32bf058c35`.
This document records a proposed process transition. It has no activation or
implementation effect before independent approval, publication, and a separate
deterministic finalization on canonical main.

## Prerequisite authority

1. R3 design is `CLOSED`: `auditoria/fase-2e-r3-diseno-reader-programacion-nominal-reconciliado.md`, SHA-256 `42a21c09d137d363e938d441a499d48c5ec7537a73ee23861bfce2c4d75677f9`.
2. The R3 implementation handoff is `MATERIALIZED / RECONCILED / INDEPENDENTLY_AUDITED / APPROVED / PUBLISHED / INTEGRATED / NOT_ACTIVE`: `auditoria/handoffs/HANDOFF-F2E-R3-IMPLEMENTACION-READER-PROGRAMACION-NOMINAL-CLEAN-MAIN.md`, SHA-256 `a74d139ca6c924dcbb98c368d80f38a17cf423408c7db30054e621b67b4cefa9`.
3. Its independent approval review is `auditoria/reviews/F2E-R3-REVIEW-HANDOFF-IMPLEMENTACION-READER-PROGRAMACION-NOMINAL-CLEAN-MAIN.md`, receipt commit `c9f28f2dd1901a1af8a874e3ced89c66e9b5f02b`, verdict `APPROVED`, P0/P1/P2 `0/0/0`.
4. Canonical main includes the design, handoff, review, and handoff process closure. The pinned main is the base above. A changed main fails this publication gate.

## Frozen implementation allowlist

The handoff's exact fenced path sets remain immutable. SHA-256 uses sorted UTF-8
repository-relative paths, joined with LF and a final LF, as defined there.

| Set | Count | SHA-256 |
| --- | ---: | --- |
| CURRENT_R3_AUTHORIZED_NEW | 18 | `594afab1a5107aa5ab22d35dfa9409249d7e7caecbbb4588b926f23b8146ce07` |
| CURRENT_R3_AUTHORIZED_MODIFIED | 2 | `2168358dbf50ccae22d0bcfa9d15499e26e3d1f3e5a0fd5d16669860c0c71f29` |
| WRITE_SCOPE | 20 | `b1a7180e430b916b56a87363a2a01992f0cf8c3b55af2a0011a0f62ce4e4e12a` |
| CURRENT_R3_READ_ONLY | 17 | `1deff2786d5b591ec044c2249bda7eb6c1f30c7e7771e9770f040cf37e9d091e` |
| CURRENT_R3_PROVENANCE_ONLY | 4 | `17786d6343b0b676797a85d829fc727db13e62208f3861ca4ec96777b740e31e` |
| TOTAL_ACTIVE_PATHS | 37 | `bb86bc48c4ea9f9fb5e64249ec8bd46070a12705048a74b7b9b4cfa7fa9ccb8d` |

`DEFAULT_DENY` is enforced. Only the published 20-path WRITE_SCOPE may be used
by a later authorized implementation lifecycle. This candidate grants no write
authority now. Any extra implementation path requires a new reviewed authority.

## Proposed activation gate

An independent fresh read-only audit must approve this exact candidate commit
with P0=0 and P1=0. Its receipt must name that commit and be published with the
candidate in one PR. The approval publication leaves R3 `NOT_AUTHORIZED /
NOT_IMPLEMENTED` and the handoff `NOT_ACTIVE`. Once the approval merge is an
ancestor of canonical main, one process-only finalization may record handoff
`ACTIVE`, implementation authority `AUTHORIZED_TO_START`, and implementation
`NOT_IMPLEMENTED`. The transition is invalid if main or prerequisite bytes
move unexpectedly, or if finalization changes product scope.

No product code, tests, migrations, product configuration, closed R3 design,
published handoff, published handoff review, or implementation allowlist may
change in this activation lifecycle.

## Preserved product boundary

Flyway remains V46; V47 is absent and no R3 migration is required. Duplicate
series/date assignments fail closed at read time. The R3 reader contract is
`f2eReaderTransactionManager / MANDATORY / readOnly=true`; individual acceptance
ownership is `REQUIRES_NEW / REPEATABLE_READ / readOnly=true`. `TurnoInstructor`
remains `LEGACY_VIVO / PRODUCTIVO`; Programación productive authority is
unchanged. R3 remains `DARK_LAUNCH` with no productive routing. Client API, web,
and reservations do not change. R4–R6 and cutover remain unauthorized. Payments
and Notifications remain out of scope. `AUTHORIZED_TO_START`, if reached, will
not mean implemented, productive, or cut over.
