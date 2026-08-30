# FeelingPilates — Review de handoff F2E / detector read-only — núcleo puro

## 1. Identidad del audit persistido

Target handoff:
`auditoria/handoffs/HANDOFF-F2E-DETECTOR-READ-ONLY-NUCLEO-PURO.md`

Target unit:
`F2E / detector read-only — materialización mínima del núcleo puro`

Type:
`IMPLEMENTATION_READ_ONLY`

Role:
`HANDOFF_AUDITOR / IMPLEMENTATION_SCOPE_AUDITOR / DOCUMENT_AUDITOR`

Mode:
`READ_ONLY`

Fresh independent:
`SI`

Branch auditada:
`operacion/excepciones-horario-fecha`

HEAD auditado:
`ac0d0790c575edc216d0573ff524e38494d01a8b`

Lifecycle auditado antes de la activación documental:
`MATERIALIZED / READY_FOR_FRESH_INDEPENDENT_HANDOFF_DOCUMENT_AUDIT / NOT_APPROVED / NOT_ACTIVE`

El auditor operó en modo read-only. Este review conserva de forma autocontenida el resultado de la
reauditoría final fresh e independiente; no dice retroactivamente que el handoff auditado ya fuera
activo y no autoaprueba el delta documental posterior de activación.

## 2. Resultado final y gates

```text
Previous P0: 0
Previous P1: 3
Previous P2: 0

New P0: 0
New P1: 0
New P2: 0

HANDOFF_CONTRACT: PASS
SECURITY_SCOPE: PASS
CANONICAL_CONSISTENCY: PASS
IMPLEMENTATION_BOUNDARY: PASS
RUNTIME_ISOLATION: PASS
TEST_CONTRACT: PASS
READY_TO_APPROVE_AND_ACTIVATE: SI
Requires human decision: NO
P1 correctable: NO — no existen P1 pendientes
```

**Veredicto persistido: A. HANDOFF NÚCLEO PURO APROBADO — P0=0 / P1=0.**

## 3. Cierre trazable de los P1 previos

### P1-1 — Contrato candidate/result/error

Queda cerrado el contrato que conserva todos los `candidates[]` generados, tanto `ELIGIBLE` como
`REJECTED`; los rechazados no se descartan y retienen su evidencia, razones, identidad y
provenance. `generated_candidate_count` cuenta todos los candidates y es igual a
`size(candidates[])`; `candidate_count` cuenta exclusivamente los elegibles. La clasificación
semántica se mantiene separada de `INPUT_INVALID` y `ENVIRONMENT_FAILURE`.

Una incompatibilidad material con la autoridad F2D se conserva como `F2D_AUTHORITY_CONFLICT`,
`blocking` y fail-closed. No se degrada a un unsupported genérico ni se normaliza por inferencia.

### P1-2 — No-selection con N=1

Para `candidate_count=0`, `candidate_count=1` y `candidate_count>1`, el contrato exige:

```text
selection_status = NOT_SELECTED_BY_DETECTOR
```

Un único candidate no significa selected, resolved, winner ni final mapping. El modelo no contiene
un campo de target seleccionado, ni un sustituto material equivalente.

### P1-3 — UNKNOWN_INTENT legacy

Para `TurnoInstructor.EXCEPCION` legacy con intención desconocida y para
`TurnoInstructor.CANCELACION` legacy con intención desconocida, el resultado es:

```text
UNSUPPORTED + UNKNOWN_INTENT
```

Un candidate único no elimina `UNSUPPORTED`. `AMBIGUOUS` permanece un eje ortogonal adicional
cuando existe ambigüedad real; no reemplaza ni vuelve excluyente `UNSUPPORTED + UNKNOWN_INTENT`.

## 4. Scope aprobado y límites preservados

La autorización posterior queda limitada a crear archivos nuevos del núcleo puro:

```text
main:  src/main/java/com/feelingpilates/transicion/programacion/detector/**
tests: src/test/java/com/feelingpilates/transicion/programacion/detector/**
```

El núcleo debe ser `PURE JAVA`, inmutable, in-memory y read-only. Se autorizan snapshots fuente,
modelos inmutables de candidate/evidence y result/status, generator puro, classifier fail-closed,
guard puro de compatibilidad F2D y tests unitarios, arquitectónicos, de non-mutation y determinismo.

No se pueden modificar archivos productivos existentes. Spring, Spring Boot, Spring Data,
JPA/Jakarta Persistence, JDBC, repositories, entities productivas, services productivos,
controllers, configuration, scheduler/listeners, DB, filesystem/network I/O y runtime wiring están
prohibidos. Adapters, runtime coordinator y metrics/report runtime están `OUT_OF_SCOPE`.

`HOST_VALIDATION` es `NOT_REQUIRED` para esta unidad exacta; introducir Spring, JPA, DB,
Testcontainers, Docker o infraestructura externa sería una `SCOPE_VIOLATION`.

## 5. Autoridad y decisiones preservadas

```text
F2D.2: CLOSED / PASS
F2E.1: CLOSED / PASS
Identity/detector design: CLOSED / PASS
D03 detector-only: CLOSED
D04: CLOSED
D09 detector-only: CLOSED
D10 detector-only: CLOSED
D11 detector-only: CLOSED
D08: DEFERRED
Data source: DATA_SOURCE_NOT_AVAILABLE
Data audit: NOT_PERFORMED
Persisted crosswalk: NOT_AUTHORIZED
Resolver: NOT_AUTHORIZED
Fence: NOT_AUTHORIZED
Migration: NOT_AUTHORIZED
MIGRANDO: NO
NUEVA: NO
Cutover: false
Runtime: DARK_LAUNCH
Productive: NOT_PRODUCTIVE
Product authority: TurnoInstructor / LEGACY_VIVO / PRODUCTIVO
```

Se preservan `reservation_identity=reserva.id`,
`programming_target_identity=ReferenciaOcurrencia` aprobada por F2D y
`reservation_consumption_snapshot=[horaInicio,horaFin)`. No se modifica `Reserva`, no hay FK,
association table, persistencia ni selección/resolver final.

## 6. Autorización derivada y siguiente gate

Al persistirse este resultado, el handoff puede quedar:

```text
APPROVED / ACTIVE / AUTHORIZED_FOR_IMPLEMENTATION_READ_ONLY
```

`ACTIVE` autoriza exclusivamente iniciar la implementación de esta unidad dentro de su allowlist.
No significa implementación realizada, código existente, tests ejecutados o PASS, technical gate
PASS, technical audit PASS, checkpoint creado, publicación, migración, cutover ni cambio de
autoridad. Las exit conditions siguen pendientes: materialización del código/tests, respeto de
allowlist, retención y conteos de candidates, no-selection N=1, UNKNOWN_INTENT, frontera de
errores, non-mutation, aislamiento arquitectónico y un audit fresh de implementación con P0=0/P1=0.

El siguiente gate de esa implementación será `FRESH_INDEPENDENT_IMPLEMENTATION_AUDIT`; sus tests,
audit técnico y checkpoint siguen `PENDING / NOT_PERFORMED / NOT_CREATED` hasta que un executor
materialice el scope permitido.

## 7. Límite de esta persistencia

Este review conserva el resultado del auditor independiente. El DOCUMENTER que actualiza este
review, el handoff y `ESTADO-ACTUAL.md` no se autoaudita. Antes de commit, push o ejecución de la
unidad debe existir una auditoría documental fresh e independiente de esta activación.
