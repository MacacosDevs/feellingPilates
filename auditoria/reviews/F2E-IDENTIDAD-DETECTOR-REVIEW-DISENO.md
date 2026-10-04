# FeelingPilates — Review de diseño F2E / identidad, semántica legacy y detector-only

## 1. Identidad del audit

Target:
`F2E / cierre de identidad, semántica legacy y contrato detector-only`

Checkpoint:
`auditoria/fase-2e-identidad-semantica-detector-read-only.md`

Role:
`DESIGN_AUDITOR / DOCUMENT_AUDITOR`

Mode:
`READ_ONLY`

Fresh independent:
`SI`

Branch auditada:
`operacion/excepciones-horario-fecha`

HEAD auditado:
`258c21b0383579a6734bc20f87b0c84008cc9bdf`

El audit fue exclusivamente read-only. Este artefacto persiste fielmente su resultado; no autoriza
implementación ni aprueba por anticipado el delta documental que materializa el cierre.

## 2. Resultado final

```text
Previous P1: 1
P1 D04 target/snapshot separation closed: SI
New P0: 0
New P1: 0
New P2: 0
IDENTITY_LEGACY_DETECTOR_DESIGN_GATE: PASS
READY_FOR_DESIGN_CLOSURE: SI
Requires human decision: NO
P1 correctable: NO
Can implementation start now: NO
```

**Veredicto: A. IDENTIDAD / SEMÁNTICA LEGACY / DETECTOR-ONLY APROBADO — P0=0 / P1=0.**

## 3. Corrección final D04 y contrato aprobado

La separación que cerró el P1 fue:

```text
Antes:
target = occurrence + reserved interval

Final aprobado:
programming_target_identity = effective occurrence reference / ReferenciaOcurrencia F2D
reservation_identity = reserva.id
reservation_consumption_snapshot = reserved_subinterval [horaInicio,horaFin)
```

Por tanto:

```text
programming_target_identity
!= reservation_identity
!= reservation_consumption_snapshot
```

El intervalo reservado no forma parte de `candidate_target_reference` ni de la identidad target.
Una occurrence admite `0..N` Reservas. Dos Reservas pueden compartir target y potencialmente el
mismo intervalo sin colisión, porque su identidad es `reserva.id`. Containment aporta candidate
evidence, no identity. Una asociación histórica puede conservar su target reference aunque la
occurrence sea posteriormente cancelada o suprimida.

## 4. Decisiones cerradas por el diseño

### D03 detector-only

`CLOSED_BY_THIS_DESIGN` para generación `0..N`, candidate evidence, múltiples candidatos,
ambigüedad, provenance, clasificación y reporte; sin selección ni persistencia.

```text
candidate_count > 1
+ sin regla inequívoca aprobada y aplicable
-> MULTIPLE_CANDIDATES
-> AMBIGUOUS
-> ambiguity_reason requerida
-> NOT_SELECTED_BY_DETECTOR
-> BLOCKER
```

`candidate_count=0` no significa universalmente `MISSING`. Una `CANCELACION` válida con target
nominal válido produce `EXPECTED_ABSENCE / SUPPRESSED`; `MISSING` queda reservado para ausencia
anómala de algo requerido.

### D09 detector-only

`CLOSED_BY_THIS_DESIGN`. La historia legacy demostrable se limita a `CURRENT_SNAPSHOT_ONLY`.
Historia no demostrable se clasifica `UNKNOWN_HISTORY / UNSUPPORTED` o equivalente. Los timestamps
`created_at` y `updated_at` no equivalen automáticamente a vigencia funcional.

### D10 detector-only

`CLOSED_BY_THIS_DESIGN`. Quedan caracterizadas separadamente legacy `CANCELACION`, legacy
`EXCEPCION`, new `CANCELACION`, new `REEMPLAZO` y new `ADICION`. No se infiere intención legacy ni
se aplica mapping automático entre una forma puntual legacy y un ajuste nuevo.

### D11 detector-only

`CLOSED_BY_THIS_DESIGN`. Ownership conceptual, input contract, output/result contract, error
contract, observability y F2D compatibility guard quedan cerrados a nivel de diseño. El futuro
executor no debe decidir arquitectura sustancial de identidad detector-only.

Para Reservas, el result contract separa como mínimo:

```text
source_reservation_id
source_reservation_snapshot
source_reserved_subinterval
candidate_target_reference
candidate_evidence
mapping_status
ambiguity_status
ambiguity_reason
selection_status
blocking_status
provenance
```

`candidate_target_reference` no incluye el reserved interval.

## 5. Compatibilidad F2D

```text
F2D design authority: PRESERVADA
F2D materialization evidence: PRESERVADA
F2D_CONTRACT_COMPATIBILITY: PASS
```

No se reinterpretan `ReferenciaOcurrencia`, `serieId + fecha`, `ajusteId + fecha` ni las demás
identidades aprobadas. Diseño F2D.1 y evidencia de materialización F2D.2 conservan sus roles
separados.

## 6. Decisiones y evidencia que siguen fuera

```text
D08: DEFERRED
Cross-salon cohort final: NOT_RESOLVED
Fence persistence/enforcement: NOT_AUTHORIZED
Data source: DATA_SOURCE_NOT_AVAILABLE
Data audit material execution: NOT_PERFORMED
Real counts/coverage/mappings: NOT_AVAILABLE
```

No se persiste crosswalk, no se implementa resolver o fence, no se normaliza, no se migra y no se
entra en `MIGRANDO` o `NUEVA`.

## 7. Estado preservado y no-autorizaciones

```text
Implementation: NOT_AUTHORIZED
Migration: NOT_AUTHORIZED
Fence: NOT_AUTHORIZED
Cutover: false
Runtime: DARK_LAUNCH
Productive: NOT_PRODUCTIVE
Authority: TurnoInstructor / LEGACY_VIVO / PRODUCTIVO
Product authority change: NO
```

El candidate future scope `IMPLEMENTATION_READ_ONLY` sigue exclusivamente
`CANDIDATE / FUTURE / NOT_AUTHORIZED`. Requiere cierre de esta unidad, un nuevo handoff específico
y su audit/activación correspondiente. No es la siguiente unidad autorizada.

## 8. Lifecycle y límite del cierre

El resultado del audit permite cerrar esta unidad como `DESIGN_APPROVED / CLOSED`. `CLOSED`
significa únicamente que la unidad `DESIGN / RESEARCH` fue completada y auditada. No significa
implementation completed, detector implemented, crosswalk persisted, resolver/fence implemented,
migration executed, cutover executed ni cambio de autoridad.

El handoff activo puede cerrarse como histórico porque sus exit conditions quedaron satisfechas.
No se crea un handoff posterior.

Este review registra el resultado del auditor independiente sobre el HEAD auditado. El DOCUMENTER
que persiste el lifecycle no se autoaudita. Antes de `git add`, commit o push, el delta de cierre
debe superar una auditoría documental fresh e independiente.
