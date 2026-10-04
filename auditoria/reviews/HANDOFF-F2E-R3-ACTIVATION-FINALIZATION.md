# F2E R3 — deterministic handoff activation finalization

Status: `FINALIZATION_CANDIDATE / EFFECTIVE_ONLY_AFTER_VERIFIED_MERGE`.
This process record has no activation effect while it exists only on a local
branch or an unmerged pull request.

## Fixed inputs

- Approval publication: PR #18, merge commit
  `cd644059faee6c7baa73e05c74f19fc89c3756fe` on canonical `main`.
- Original activation candidate: `e397a56c7b42c160b2eb12ef2c30529418a75a01`.
- Correction.1: `0d0db6ef813fe04ddf11a3d72557311317761faa`.
- Fresh independent re-audit receipt:
  `auditoria/reviews/HANDOFF-F2E-R3-ACTIVATION-CORRECTION1-INDEPENDENT-REAUDIT-RECEIPT.md`,
  commit `875c94eea6c2b9ffdc16171777f51ea8a0f42b91`;
  `APPROVED`, P0/P1/P2 `0/0/0`, prior P1-1 and P1-2 `CLOSED`.
- Closed R3 design SHA-256:
  `42a21c09d137d363e938d441a499d48c5ec7537a73ee23861bfce2c4d75677f9`.
- Published R3 implementation handoff SHA-256:
  `a74d139ca6c924dcbb98c368d80f38a17cf423408c7db30054e621b67b4cefa9`.
- Published handoff review SHA-256:
  `94c6063a66c918a9a88dc6e77bd3b3e72b913161610c0e1dde060796136bcfce`.

## Effective condition and result

The transition is effective only when a finalization commit containing this
record and the matching current-state update is merged into canonical `main`,
the approval merge above remains its ancestor, all three prerequisite file
hashes above remain unchanged, and that finalization changes process artifacts
only. Verify these facts from Git and file bytes after merge. If any check
fails, this record does not activate the handoff.

When that condition holds, the published R3 implementation handoff is `ACTIVE`.
R3 implementation authority is `AUTHORIZED_TO_START`; implementation remains
`NOT_IMPLEMENTED`. The authority is restricted to the handoff's frozen
`WRITE_SCOPE` of 20 paths with `DEFAULT_DENY`. No additional path is authorized.

Before that condition holds, the handoff remains `NOT_ACTIVE` and R3
implementation remains `NOT_AUTHORIZED / NOT_IMPLEMENTED`. Neither this record
nor the independent audit receipt alone grants implementation authority.

Flyway remains V46, V47 absent, and R3 has no migration. `TurnoInstructor`
remains `LEGACY_VIVO / PRODUCTIVO`; Programación productive authority is
unchanged. R3 remains dark launch without productive routing. Client API, web,
reservations, R4–R6, cutover, Payments, and Notifications are outside this
transition. This finalization does not implement R3.
