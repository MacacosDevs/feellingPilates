# FeelingPilates — Review técnico de implementación F2E / detector read-only — núcleo puro

## 1. Identidad del audit persistido

Target unit: `F2E / detector read-only — materialización mínima del núcleo puro`

Type: `IMPLEMENTATION_READ_ONLY`

Role: `IMPLEMENTATION_AUDITOR / TECHNICAL_AUDITOR / ARCHITECTURE_AUDITOR`

Mode: `FRESH / INDEPENDENT / READ_ONLY`

Branch auditada: `operacion/excepciones-horario-fecha`

HEAD auditado: `1ebf0010e7376719627ecff9bdf592b8c2aa2f6f`

Working tree auditado:

```text
28 archivos untracked
21 production
7 test/helper
exclusivamente dentro de las allowlists detector/**
existing tracked files modified: NONE
```

Este review persiste fielmente el technical audit final fresh e independiente. El DOCUMENTER que lo materializa no se autoaudita ni aprueba el delta documental de cierre.

## 2. Resultado final y gates

```text
P0: 0
P1: 0
P2: 0
SCOPE_GATE: PASS
SEMANTIC_CONTRACT_GATE: PASS
IMMUTABILITY_GATE: PASS
RUNTIME_ISOLATION_GATE: PASS
TEST_GATE: PASS
TECHNICAL_IMPLEMENTATION_GATE: PASS
READY_FOR_IMPLEMENTATION_CLOSURE: SI
Requires human decision: NO
P1 correctable: NO — no existen P1 pendientes
```

**Veredicto persistido: A. NÚCLEO PURO IMPLEMENTATION PASS — P0=0 / P1=0.**

## 3. Historia causal de findings

La primera auditoría reportó `P0=0 / P1=4 / P2=0`: (1) bypass legacy entre `SourceAtomType` y `DetectionScenario`; (2) multiplicidad nominal sin ambigüedad; (3) D04 sin preservación conjunta de target histórico y supresión actual; y (4) `DetectorResult` no valid-by-construction. La primera corrección cerró P1-1/P1-2/P1-3 y dejó P1-4 residual. La última corrección cerró P1-4. El audit final no halló P0, P1 ni P2 pendientes.

### P1-1 — CLOSED

`SourceAtomType ↔ DetectionScenario` tiene validación bidireccional. Los bypass legacy genéricos de `EXCEPCION` y `CANCELACION` están cerrados; la consistencia `SourceSystem/SourceAtomType` pasa. La semántica legacy permanece `UNSUPPORTED + UNKNOWN_INTENT`.

### P1-2 — CLOSED

Para `new CANCELACION` y `REEMPLAZO`, multiplicidad nominal target mayor que uno produce `AMBIGUOUS`, `blocking`, razón reproducible y `NOT_SELECTED_BY_DETECTOR`, además de la semántica de incompatibilidad correspondiente.

### P1-3 — CLOSED

`Reserva` preserva el historical programming target `ReferenciaOcurrencia` separado del current effective outcome. La cancelación o supresión actual no borra la referencia histórica; `EXPECTED_ABSENCE / SUPPRESSED` se conserva. No existe crosswalk persistido, DB ni adapters.

### P1-4 — CLOSED

`DetectorResult` es valid-by-construction. La ambigüedad se valida bidireccionalmente y exige causa real. `MULTIPLE_NOMINAL_TARGETS` exige multiplicidad nominal real y la razón de múltiples elegibles exige `candidateCount > 1`. La validación relacional de `historyStatus` está activa; `UNKNOWN_HISTORY` es coherente con `UNSUPPORTED`, razón y blocking según D09. La relación result/effective pasa, el conflicto F2D bloquea y los estados públicos inválidos son rechazados.

## 4. Evidencia de implementación y tests

La materialización se limita a 21 archivos production y 7 test/helper nuevos bajo `src/main/java/com/feelingpilates/transicion/programacion/detector/**` y `src/test/java/com/feelingpilates/transicion/programacion/detector/**`. No se modificó ningún archivo productivo existente. Se descubrieron seis clases de test y el helper `DetectorTestFixtures`.

```text
Targeted tests: 37
Failures: 0
Errors: 0
Skipped: 0
Result: PASS / BUILD SUCCESS
```

La suite más amplia es `OPTIONAL_ATTEMPT`. La última evidencia fresh disponible registró 118 errores ambientales Docker/Testcontainers, sin fallos conocidos del detector. `HOST_VALIDATION` no fue requerida: no es PASS de suite global ni blocker del núcleo puro.

## 5. Scope y autoridad preservados

```text
Pure Java / in-memory / immutable / read-only: PRESERVED
Spring: NONE
JPA: NONE
DB / persistence: NONE
Runtime wiring / productive consumer: NONE
Adapters: NOT_IMPLEMENTED / OUT_OF_SCOPE
Persisted crosswalk: NOT_AUTHORIZED
Resolver: NOT_AUTHORIZED
Fence: NOT_AUTHORIZED
D08: DEFERRED
Migration: NOT_AUTHORIZED
MIGRANDO: NO
NUEVA: NO
Cutover: false
Data source: DATA_SOURCE_NOT_AVAILABLE
Data audit: NOT_PERFORMED
Runtime: DARK_LAUNCH
Productive: NOT_PRODUCTIVE
Product authority: TurnoInstructor / LEGACY_VIVO / PRODUCTIVO
```

La compatibilidad F2D se preserva; el conflicto F2D sigue fail-closed y blocking. Esta materialización no introduce resolver, selector material, persistence, adapters ni transición de autoridad.

## 6. Lifecycle y límite de este review

La evidencia permite `IMPLEMENTATION CLOSED`: núcleo puro materializado, tests aplicables PASS y technical implementation audit PASS. No significa runtime activo, productively consumed, publicación, adapters, DB, data audit, crosswalk, resolver, fence, migración, cutover ni cambio de autoridad.

Las exit conditions del handoff quedan satisfechas: allowlist, archivos nuevos solamente, contratos/resultados, generación `0..N`, retención y conteos, no-selection, `UNKNOWN_INTENT`, target histórico, valid-by-construction, tests, inmutabilidad, no Spring/JPA/DB, sin persistence/resolver/fence/runtime activation y audit fresh con `P0=0/P1=0`.

El handoff puede quedar `COMPLETED / CLOSED / HISTORICAL`; no existe handoff activo ni se crea una siguiente unidad. El siguiente paso es sólo `READ_NEXT / NEXT_HANDOFF_SCOPE`.

Este review registra el resultado del auditor independiente. Antes de `git add`, commit o push, el delta documental de cierre requiere una auditoría documental fresh e independiente.
