# Handoff F2E R2 — provenance y perfil de materialización DESIGN/RESEARCH

Estado documental: `MATERIALIZED_CANDIDATE / PENDING_FRESH_REAUDIT_AND_GATE`.
Función: registro de autorización/provenance/profile del despacho documental actual.
**NOT_ACTIVE_IMPLEMENTATION_HANDOFF; NOT_APPROVAL; NOT_PUBLISHED.**
Fecha: 2026-09-16. Run `run_df3cbaebd5d7`.
Corrección actual Task `task_a2e7ce748158`; Dispatch `ctx_ef6c945da2a1`.
Worker `term_7971b0d5-c45b-488f-82ff-9d3ebffc4fd4`, corrector documental fresh,
no auditor; coordinador `term_6940950d-86ad-4a8e-a7cc-9bd3fc569779`.
Materialización inicial histórica `task_c195cda67de7 / ctx_c45b806cebbf`, worker
`term_c7bda15d-8fd8-4c6e-9689-4b3359cae333`, completado/liberado.

## 1. Autorización existente registrada, no creada por este archivo

El gate real precedente `gate_236c6b0bdf43` del Run `run_b000b8a5b647`,
Gate Task `task_ab1d650831a4`, está resolved/PASS (2026-09-16T21:21:17Z), con
semántica literal `PASS — F2E R2 PREFLIGHT COMPLETE / READY_FOR_R2_DESIGN_AUTHORITY_MATERIALIZATION`.
Su clasificación de entrada es `R2_DESIGN_AUTHORITY_INCOMPLETE` y su único
nextLifecycle es `R2_DESIGN_AUTHORITY_MATERIALIZATION`. No aprobó una instancia R2
que todavía no existía, un implementation handoff ni implementación. Su prohibición
`CURRENT_REPOSITORY_MUTATION` pertenece al Run preflight read-only; el despacho
actual, separado y explícito, autoriza exactamente los dos NEW documentos de §3.
Este handoff registra esa autorización vigente sin retroactivarla al preflight.

Después de la materialización inicial, audit independiente fresh
`task_1c407b9a88a1 / ctx_3c018d86ea0f` terminó con dos P1 y verdict literal
**FAIL — F2E R2 DESIGN AUTHORITY REMAINS INCOMPLETE**. Reporte original físico
`/tmp/feelingpilates-f2e-r2-design.iUQKrV/AUDIT-R2-DESIGN.md`, SHA
`bda484b6140405491740b6c193fd120d06dbd3550cc26ee458866500f489109a`.
Auditor inicial completado/liberado; no se reescribe su FAIL ni los reportes de
materializador/verificación iniciales como PASS. El despacho actual es el stage
normal `CORRECT` documental de WORKFLOW en el scope original, sólo residuals
R2-DESIGN-B-01 (metadata SQL/catálogo/capture) y R2-DESIGN-AB-02 (exact K de rechazos).
No crea una nueva fase ni implementación/handoff ACTIVE; corrección materializada
queda `PENDING_FRESH_REAUDIT_AND_GATE`, sin cierre competente de P1 por el corrector.
Preimages externas originales preservadas: `DESIGN-PRE-CORRECTION.md` SHA
`3a6eb4e4b38ff90b66e85680c407ae808dbba471b21d4be79dd2c321cd77afaa` y
`RESEARCH-HANDOFF-PRE-CORRECTION.md` SHA
`e9c56d7beb47f8558a756fe2e132aa3874069fd902cbb0d26270646256c1bb83`.

Entradas externas leídas completas, físicamente conservadas bajo
`/tmp/feelingpilates-f2e-r2-preflight.7Qm8QZ/`:

| Archivo exacto | SHA-256 físico |
| --- | --- |
| REPORT.md | 9eedcb08498d75071a811c27e84099619f08991d689ac9d84dcbc7598701eec3 |
| COORDINATOR-REPORT.md | c716efab2cebc754603d109766a76e8ed9ad2814778b55c5028e95c8a4a09999 |
| AUTHORITY-RECON.md | d537e2981263d3455c6439e7339cb788c87f9ce60834e62350ae33bc459297a5 |
| DEPENDENCY-RECON.md | 0309020835ca313791e5a8e0cb35200fa6ae6942ef29a8337b0e8756d136c885 |
| AUDIT-R2-PREFLIGHT.md | d6f2c8a62f337bee5db5c084d64c2793dcfde4056a32264bcfc365041b14a101 |
| GATE-RESULT.json | b54221dd752dd48b7e2c92e1fc5c633c0d3eccad745c66faed54f92a9a4cf8f2 |

Bindings de preflight preservados: autoridad `task_d9ade2ca6de6 / ctx_60e2483e9c8d`;
dependencias `task_67c356f7ed45 / ctx_10c9b5b13f22`; auditor independiente
`task_e1665ce806f5 / ctx_08d33604dd7d`, verdict literal
`PASS — R2 REQUIRES DESIGN AUTHORITY MATERIALIZATION BEFORE HANDOFF`.
No se atribuye ese audit a los documentos nuevos ni se reutiliza su contexto como
audit fresh de materialización. Los dos strengthenings de síntesis fueron corregidos
y reverified antes del PASS: vectores/ordered/pre-callback fingerprint commitments
R1 no son aceptación R2, y publicación no es prerrequisito universal del protocolo.
Preimagen conservada `COORDINATOR-REPORT-PRE-AUDIT-CORRECTION.md`, SHA
`7d60e89a51c2eee20efdc088fb38d2b8e2fe6a523804a2769d7627731e3c4ed5`.
El shorthand original `LEGACY_PUNCTUAL_UNKNOWN_INTENT` sigue visible en el recon,
pero es inválido y no se adopta: son dos escenarios legales específicos de tipo.

## 2. Baseline físico, precedencia y fuentes

Repo `/Users/jesusaldaircruzortiz/Desktop/Feelingpilates/feelingpilates`, branch
`operacion/excepciones-horario-fecha`. HEAD/upstream/live origin inmutables
`6c2eacc870499e74ead74c1851630f9f53b1c676`; ahead/behind0/0. Entrada tracked CLEAN,
index EMPTY, untracked0, baseline dirty autorizado NINGUNO. Snapshot completo
`/tmp/feelingpilates-f2e-r2-design.iUQKrV/BASELINE.json`: 460 paths y hashes.
Inventory SHA `d7964ab357d282a21b3289f0511f6289a3e32f8dd0a13a615f8a4dd93384d5cd`;
index SHA `b053b254ae09434071a7dcd9922c2d2f3dbc6edde0ddaa2935c763432527e682`;
refs SHA `e75a76fb06f4b02f953961c777ee0d8c776b1f6cbcb980b69dd98656ff097e8b`.
El inventario usa JSON compacto ordenado path→SHA UTF-8; refs usa show-ref strip.
Ese CLEAN/untracked0 es la entrada histórica del materializador. El corrector
entra con exactamente los dos candidatos NEW ya existentes untracked, tracked
CLEAN/index EMPTY, sin atribuirse su creación. `IGNORED-BASELINE.json` conserva
660 hashes ignored y `R1-IDENTITY.json` conserva exact21; se recomputan ambos,
junto con tracked460/index/refs antes/después, sin reparar outputs preexistentes.

R1 exact21: main11/test10, path SHA
`f400a0602f95e318845da670bee4f819f057842adf8a60506564d5bd75e41d14`, content SHA
`e2b64abd6aba8a050df6184f6c5440d87db83a67182fb43e622f48cf96f4f3ce`;
manifiesto externo `R1-DEPENDENCY-IDENTITY.json` en el directorio preflight.
Path stream sorted LF con terminal LF; content stream `SHA  path\n` por path.
No se atribuyen al documenter los commits, código, tests o PASS preexistentes R1.

Se respeta lectura física AGENTS→README/ESTADO→handoff/canónicos pertinentes→
checkpoint/reviews→protocolo completo. ESTADO tiene ACTIVE HANDOFF NINGUNO;
el R1 consumido es histórico, no autoridad de ejecución R2. Sus PENDING documentales
del corte escrito se preservan; el preflight corroboró el cierre operativo
`run_fb92631a2300 / gate_7e4a087bd1ce PASS`, sin reabrirlo.

| Fuente | Clase y uso |
| --- | --- |
| auditoria/README-REESTRUCTURACION.md; auditoria/ESTADO-ACTUAL.md; AGENTS.md | NORMATIVE: precedencia, estado, baseline, roles; no successor implícito |
| auditoria/fase-2e-diseno-adapters-read-only-snapshot-consistency.md, completo, SHA6c72cba1f83fbc2bcf3b3219d8252d2410ec482e30ae85d30fd2eeb04e8883d8 | NORMATIVE: D8/12/13/18/19/20/21–24/26–30; D36–37 R1-only salvo fundamentos compartidos delimitados; D33.2 SUPPORTING proposal |
| auditoria/fase-2e-identidad-semantica-detector-read-only.md, SHA6f850e9723f9861456d646039e4b233cff20d013ff956cab98f7370dffac4670 | NORMATIVE: source/candidate, historia e intención, legalidad de escenarios |
| auditoria/ARQUITECTURA-ACTUAL.md; auditoria/DECISIONES-ARQUITECTONICAS.md; auditoria/contexto/DOMINIO-FUNCIONAL.md; auditoria/contexto/MAPA-LEGACY-Y-MIGRACION.md | NORMATIVE: autoridad legacy, temporalidad, dark launch, cutover y fronteras |
| auditoria/orquestacion/README.md; WORKFLOW.md; STATE-MACHINE.md; GATES.md; ROLES.md | NORMATIVE: perfiles competentes, audit/gate separados y publication≠activation |
| Reviews F2E, review R1, handoffs consumidos y reportes preflight | SUPPORTING: cronología, autorización precedente y findings; no nueva autoridad implementativa |
| Schema/migraciones/core/guard/testinfra R1 físicos | SUPPORTING: compatibilidad y hardcodes; código no establece decisiones productivas |
| auditoria/fase-2e-r2-diseno-lector-turno-legacy-integracion.md | NEW_CURRENT_R2_INTEGRATION_DESIGN_DECISION_CANDIDATE: pendiente audit/gate, no CLOSED/PUBLISHED |

ESTADO SHA `cd3d7d71923eb80cd951a01f6618d6dc6c2a19105becedd0e21f0e22c005a9b1`;
review R1 SHA `b324a51ddae26e00bb9b8714c48402a3001f0c99ef5f2fd1db7038ebd5adfde9`;
handoff R1 SHA `3fd71faca4d4c049ad5cb37b52bc6fd512509cf5b696bdc5c28d28cb966af8ef`.

## 3. Scope y allowlist exclusivos de materialización/corrección documental

Escritura sólo apply_patch y sólo dos rutas NEW candidatas existentes de repo;
el delta del corrector es revisión de sus bytes iniciales, no creación:

1. `auditoria/fase-2e-r2-diseno-lector-turno-legacy-integracion.md`.
2. `auditoria/handoffs/HANDOFF-F2E-R2-DISENO-LECTOR-TURNO-LEGACY.md`.

Report inicial histórico preservado:
`/tmp/feelingpilates-f2e-r2-design.iUQKrV/MATERIALIZER-REPORT.md`.
Único report authored por el corrector actual:
`/tmp/feelingpilates-f2e-r2-design.iUQKrV/CORRECTOR-REPORT.md`, apply_patch.
Review fresh y ESTADO postgate están reservados al coordinador: no son propiedad
de este worker ni ampliación de su allowlist. No modifica D publicado.

Objetivo: materializar mínimo diseño competente de integración, preservando toda la
semántica R2 existente, cerrando exactamente tres gaps para revisión independiente:

| Gap | Materialización candidata | Evidencia de salida |
| --- | --- | --- |
| A: contexto/identidad/provenance | Contexto y resource/registry R2 propios; formulas D13 instanciadas, LP/SEQ, keysets exactos, null/ABSENT/rangos/markers/technical/history/scenarios; LegacyTurnReadSet sources-only inmutable; AB-02 pending projected errors+UUID keys+ASSIGNMENTS para K exacto y ordinal fallback sólo keys insuficientes | Diseño §§2–3, source provenance §12; no extensión ReadSnapshotContext/Reserva; re-audit pendiente |
| B: SQL/RR/no-write | SQL literal 2DATA+4probes, seis shapes/IDs cerrados; RESOURCE explícito zero binds por sharedEM observa database/schema reales, guards URL/principal/grafo/referencias locales; getSchema/getCatalog prohibidos; owner RR/readOnly test-only, reader MANDATORY; I/R/RESOURCE/S inicial/final, snapshot y nativePgConnection association; capture prefixes exactos; role exact3tables+built-ins mínimos/checksum compositePK/all-column | Diseño §§4–7, aceptación §10; B-01/AB-02 corregidos documentalmente, sin cierre competente ni assertion de ejecución del inspector |
| C: preservation guard | Una estrategia: futura excepción sólo a ReservaJpaReaderArchitectureTest; exact R1main11/test10 conservados, separate SEALED futureR2allowlists y union equality/no unknowns; siete capacidades clasificadas | Diseño §§8–9; guard no editado, paths R2 futuros no adivinados |

No Java/tests/config/migraciones/pom/core/legacy edits, tests/build/Maven,
containers/JDBC/SQL, staging/index/ref mutations, commit/push/publicación ni niños.
Consultas Git/lecturas físicas y coordinación Orca son evidencia, no ejecución del
reader. No self audit o PASS de arquitectura basado en comprobaciones del autor.

## 4. WORKFLOW_PROFILE competente para esta unidad

Perfil seleccionado: `DESIGN_RESEARCH / LOCAL_MATERIALIZATION`, scope documental
bounded §3; prepare y snapshot, materialización candidata, scope comparison,
**fresh independent design audit**, gate competente y actualización de estado por
coordinador si procede. Implementación, tests y host delivery de código no aplican
a esta unidad. Safety/scope guards sí aplican. El audit inicial es FAIL preservado;
re-audit fresh y gate de los bytes corregidos están `PENDING_FRESH_REAUDIT_AND_GATE`;
este archivo no inventa sus futuros Task/Dispatch/Gate IDs ni los da por creados.

Base física: WORKFLOW §§perfil/prepare/design/research/local delivery/publication
(líneas9–15,54–68); GATES clasificación por perfil (líneas11–35); ROLES separación
documentación/audit (líneas7–21); STATE-MACHINE terminal no autoriza nueva unidad.
El protocolo pide perfil explícito y evidencia aplicable, no publicación universal.
Los reports externos apoyan el profile, no sustituyen autoridad física.

**Publicación de autoridad R2: no incluida, no automática y no ejecutada.**
El profile R2 seleccionado propone/requiere una siguiente unidad separada
`R2_DESIGN_AUTHORITY_PUBLICATION`, permitida como siguiente lifecycle por el usuario,
condicionada a diseño auditado/gate competente, scope propio y comprobaciones reales
de publicación. Bajo este profile, la publicación de esta autoridad deberá preceder
la activación de un implementation handoff R2 como ACTIVE. Es decisión actual R2
profile-specific, pendiente de revisión de diseño; no requisito universal inventado
ni permiso de publicar dentro de este despacho. Publicación≠activación productiva.

Readiness para **AUTHORING** de un futuro implementation handoff se puede evaluar
después del diseño auditado/gate bajo autorización separada; no se confunde con la
publicación/activación de la autoridad. Este handoff de investigación no es ese
implementation handoff, no fija filenames de implementación, no es ACTIVE ni habilita
ejecución R2. Activación futura requiere su propio handoff exacto, independiente
audit/approval/gate y publication prerequisite específica del profile seleccionado.

## 5. Contratos preservados y límite de reutilización

R2 sólo sources para fecha+salones, activa RECURRENTE weekday domingo0 y puntual
exact-date; once header/membership fields LEFTJOIN y cinco assignments para todos
derived turnIds UUID físicos validados, incluso no-miembros y headers con payload
no-key inválido pendiente. Sólo cero rows reales de MEMBERS es éxito vacío/omite
query2. Invalid rows sin parent UUID usable omiten query2 y abortan por ordinal;
con otros parents usables se completa su enumeración. K, URNs assignment
PK triple/gaps ABSENT|member, recordIds sólo físicos, closed markers y distinción
LEGACY_FULL_TURN_FALLBACK vs FULL_TURN_RANGE_FALLBACK permanecen. Required,
duplicados y unmatched headers abortan TODO pre-core; ningún partial/source/
classifier output. Error de payload header con keys suficientes espera ASSIGNMENTS
para exactamente K rejections: 0 assignments→1 gap; 3 PKs de actividad→3 unidades.
Duplicate PK/sourceIdentity conserva 1 rejected atom y observedPhysicalRowCount
completo; keys insuficientes 1 por ordinal estable, nunca fabricated keys/K.
Acceso/policy/binding/recurso/TX/probes/completion fallidos abortan inmediato por
owner operacional distinto; no continuar TX JDBC abortada ni publicar pending
errors como K completo. Validación/publicación sólo tras ambas projections válidas.
Sólo escenarios puntuales LEGACY_EXCEPTION_UNKNOWN_INTENT y
LEGACY_CANCELLATION_UNKNOWN_INTENT; UNKNOWN_INTENT prevalece sobre anomalía
puntual. Timestamps técnicos no acreditan historia; claim histórico legal sólo
recurrente. Default/prod beans y productive callers cero; clases plain constructorDI
y proxies test-only. Concurrencia native RR y quiescent no-write son ventanas distintas.

R2-DESIGN-B-01 se instancia por catálogo literal cerrado seis shapes (2DATA+4probes),
RESOURCE `SELECT current_database() AS database_name, current_schema() AS schema_name`,
dos String non-null, zero binds. Tupla real inicial/final contra expected confiable,
sin sustitución/cache/transformación. getURL/getUserName nativos locales genuinos,
guards inicial/final locales por doReturningWork sin SQL; getSchema/getCatalog
prohibidos en invocación medida sin excepción por cache. SQL RESOURCE por sharedEM,
plan/inspector/JDBC real/PgConnection bound como todas las shapes. Orden §6.3:
Iinitial/Rinitial/RESOURCEinitial/Sinitial/MEMBERS/[ASSIGNMENTS]/Ifinal/Rfinal/
RESOURCEfinal/Sfinal/guard local final/completion. Mismatch URL/grafo/principal
inicial zero invocation SQL; mismatch RESOURCEinitial ya ejecutó probes, zero DATA.
Bootstrap/schema-validation metadata fuera de invocación/capture, sin zeroSQL global
falso. Lifecycle JDBC owner observado aparte/corroborado, no DATA/probeSQL.
Built-ins efectivos sólo los necesarios a probes; sin functions de aplicación,
source table grants adicionales ni bypass SELECT/schema/driver. R1closed4 intacto.

Se recuperan las siete capacidades D24.1, no importación del config/harness R1
completo: container/Flyway REUSE_AS_IS como capacidades separables; role, SQL
inspection, checksum y architecture EXTEND_WITH_R2_NEUTRAL_CAPABILITY; transaction
test owner NOT_REUSABLE en su runtime Reserva/RC actual. Preservación concreta de
R1 y exact future shared-file approvals según diseño §9. Reutilizar LP/SEQ no
reutiliza domains/context/registry/query count/identities Reserva. La excepción futura
de guard está limitada al único path físico de diseño §8, no se edita ahora.

NOT_YET_NORMATIVE: golden vectors, orden de fingerprint commitments, pre-callback
commitment R1, exact Maven commands, future test/file counts, históricos R1
59/649/host7/native4 y native first/later TECH. No tests ejecutados ni implementación
R2 aceptada. D26/27 exigirá evidencia real PG16/Testcontainers/FlywayV47/validate,
types/binding/0–1–N/inmutabilidad/scenarios/abort/RR/capture/role/checksum/architecture/
ausencia productiva en la futura unidad implementativa competente, no este despacho.

TurnoInstructor `LEGACY_VIVO/PRODUCTIVO`; R1 `CLOSED/ACCEPTED/PUBLISHED` inmutable;
R2 `DARK_LAUNCH/NON_PRODUCTIVE` diseñado, no activado; R3–R6
`NOT_AUTHORIZED_IN_R2`; Payments/Notifications `OUT_OF_SCOPE`. Cero nominal/effective
selection, crosswalk/resolver/fence/report sink/material audit/migration/cutover.
Autopilot/orquestador/HostValidator antiguos `OLD_PROCESS_ONLY`.

## 6. Exit evidence y siguiente lifecycle propuesto

Salida histórica del documenter: los dos candidatos materializados, matriz
clause-by-clause y three-gap closure para revisión, hashes en MATERIALIZER-REPORT externo.
El corte inicial MATERIALIZER-REPORT y su entrega se conservan históricamente.
Salida del corrector: revisión sólo de B-01/AB-02 y referencias consistentes en
estos dos candidatos; mapping/hashes/CLOSED6/failure paths en CORRECTOR-REPORT.
Comparación before/after de todos460 tracked/660 ignored, index, refs, HEAD/upstream
y R1exact21; exactamente
dos NEW untracked permitidos y cero delta implementativo/out-of-allowlist.
Esto es medición de scope del autor, no audit independiente ni autoaprobación.

Coordinador debe obtener re-audit fresh independiente y gate competente de esta
corrección, sin atribuirlos a este corrector. P1 cierre competente pendiente. Sólo después, evaluar
la publicación R2 separada ya permitida, con profile/scope y resultado físicos.
AUTHORING/ACTIVE de implementation handoff e implementación siguen unidades
separadas no autorizadas aquí. No se anuncia next functional phase, IDs futuros,
implementación aceptada, publicación completada o cambio de autoridad productiva.
