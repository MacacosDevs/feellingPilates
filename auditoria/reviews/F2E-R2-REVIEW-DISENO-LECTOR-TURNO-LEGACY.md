# F2E R2 — review independiente del diseño de integración del lector Turno legacy

Run `run_df3cbaebd5d7`, 2026-09-16. Receipt/persistencia por el coordinador
`term_6940950d-86ad-4a8e-a7cc-9bd3fc569779`; no es autoauditoría del diseño.
Resultado independiente vigente: **PASS — F2E R2 DESIGN AUTHORITY COMPLETE FOR HANDOFF**.
Publicación de esta autoridad: **NOT_PUBLISHED / NOT_EXECUTED**.
Implementación R2/handoff de implementación ACTIVE: **NOT_AUTHORIZED / NOT_MATERIALIZED**.

## 1. Cadena, corte competente y custodia

Entrada física CLEAN/index EMPTY/untracked0, HEAD/upstream/live
`6c2eacc870499e74ead74c1851630f9f53b1c676`, branch
`operacion/excepciones-horario-fecha`, ahead/behind0/0, sin fetch.
R1 terminal `run_fb92631a2300 / gate_7e4a087bd1ce PASS`; no se reabre.
Preflight R2 `run_b000b8a5b647 / gate_236c6b0bdf43 PASS` permitió exclusivamente
`R2_DESIGN_AUTHORITY_MATERIALIZATION`, no implementación.

| Rol separado | Task / Dispatch reales | Entrega y settlement |
| --- | --- | --- |
| Materializador inicial | task_c195cda67de7 / ctx_c45b806cebbf | R2_DESIGN_AUTHORITY_MATERIALIZED; Done msg_e39d12d1b9e1; completed/released; transcript captured; ACK posterior a release |
| Auditor inicial fresh | task_1c407b9a88a1 / ctx_3c018d86ea0f | FAIL histórico, P0=0/P1=2; Done msg_514c92c72711; completed/released; transcript captured; ACK posterior a release |
| Corrector documental acotado | task_a2e7ce748158 / ctx_ef6c945da2a1 | R2_DESIGN_AUTHORITY_MATERIALIZED; Done msg_20b89d9edc46 a2026-09-16T22:15:37Z; completed/released; transcript captured; ACK posterior a release |
| Reauditor fresh distinto | task_9a5c9b6b0b55 / ctx_4d4dfda77e33 | PASS, P0=0/P1=0/P2=0; Done msg_5fe1bdca614f a2026-09-16T22:28:11Z; completed/released; transcript captured; ACK posterior a release |

Los cuatro workers fueron separados; el corrector no se autoaprueba y el auditor
fresh no modifica repo. Los reportes completos siguientes son los originales exactos,
no una reconstrucción semántica. Se preserva el FAIL para los bytes iniciales.
La verificación coordinadora inicial sólo admitió aquellos bytes a audit;
la corrección posterior sólo B-01/AB-02 fue medida contra sus preimages, no contra
HEAD ni atribuida al baseline R1. La reauditoría volvió a revisar las doce preguntas,
todos los contratos y scope, no sólo el delta. El cierre de los P1 es de diseño,
no ejecución/aceptación técnica ni validación JDBC de R2.

| Input sellado | SHA-256 |
| --- | --- |
| Diseño primario publicado, read-only | 6c72cba1f83fbc2bcf3b3219d8252d2410ec482e30ae85d30fd2eeb04e8883d8 |
| Diseño R2 inicial histórico / preimage | 3a6eb4e4b38ff90b66e85680c407ae808dbba471b21d4be79dd2c321cd77afaa |
| Research handoff inicial histórico / preimage | e9c56d7beb47f8558a756fe2e132aa3874069fd902cbb0d26270646256c1bb83 |
| Diseño R2 corregido auditado | db4673dd0705c41e62d0b77339d45b2cd1e87c51e26064112ad95b843dafe9cf |
| Research handoff R2 corregido auditado | 221347b46c5032b384a306c61908c8fd0f2c26076455609fb880098be8f0d2d4 |
| Materializer report original | cb7786eb20b83b13d01ba7893fd74141b504e33cab1fcaae53bffb670b482b2e |
| Corrector report original | 15dd2fbc8b06cf0c42000ed8f442ec20250f4ac4d8e8b96d6d21a2cba72515ee |
| Verificación coordinadora corregida externa | c97c2009be8f7bf9a2503315c325a6da365b86dc790158f015fb7075a9c8f80b |
| Audit inicial original íntegro | bda484b6140405491740b6c193fd120d06dbd3550cc26ee458866500f489109a |
| Re-audit original íntegro | bf3057c4f5d085e049f76ab901289c58bf694d9048b46c31c90fa99330acb27b |
| R1 exact21 path-set preservado | f400a0602f95e318845da670bee4f819f057842adf8a60506564d5bd75e41d14 |
| R1 exact21 content preservado | e2b64abd6aba8a050df6184f6c5440d87db83a67182fb43e622f48cf96f4f3ce |
| ESTADO entrada al audit, histórico/read-only en ese corte | cd3d7d71923eb80cd951a01f6618d6dc6c2a19105becedd0e21f0e22c005a9b1 |

Custodia local externa: `/tmp/feelingpilates-f2e-r2-design.iUQKrV/`, sin garantía
de retención de /tmp. Los dos informes de auditoría quedan durables completos aquí;
preimages/reportes de autores mantienen su identidad histórica externa. Las cláusulas
normativas y la provenance completa de decisiones están en el
[diseño R2 auditado](../fase-2e-r2-diseno-lector-turno-legacy-integracion.md) y el
[handoff de investigación](../handoffs/HANDOFF-F2E-R2-DISENO-LECTOR-TURNO-LEGACY.md).
Ese handoff no es implementación ni ACTIVE.

Los headers PENDING_FRESH_REAUDIT_AND_GATE de los dos inputs sellados describen el
corte de entrega del corrector. No se alteran bytes ya auditados para insertar un
resultado futuro en su propia preimage. El PASS competente posterior es este receipt
del informe independiente; la resolución real del gate corresponde a la sección
terminal R2 de ESTADO-ACTUAL, después de resolverse. Este review no se autoeleva a
gate y no inventa IDs futuros. Ningún auditor afirmó revisar este receipt futuro,
el estado postgate ni publicación futura.

## 2. Cierre independiente y siguiente lifecycle limitado

| Gap | Instancia auditada | Disposición independiente de diseño |
| --- | --- | --- |
| A | Diseño §§2–3, contexto10/URN/PK/gaps/maps26+20/D13/envelope; exact K de rechazo corregido | COMPLETE |
| B | Diseño §§4–7, six-shape catalog/typed binds/capture/RR native owner y RESOURCE explícito/checksum3tables | COMPLETE |
| C | Diseño §§8–9, única future bounded ArchitectureTest exception, R1literalsets/global SEALED R2union y siete capacidades | COMPLETE |
| B-01 histórico | Native local URL/user; getSchema/getCatalog prohibidos; actualdb/schema por probe cerrado RESOURCE, sin SQL guard oculta | CLOSED BY FRESH INDEPENDENT DESIGN RE-AUDIT |
| AB-02 histórico | Pending projected errors/key-first/ASSIGNMENTS/exact K; keylessordinal frente a hard operational immediate abort | CLOSED BY FRESH INDEPENDENT DESIGN RE-AUDIT |

La selección profile-specific R2 exige `R2_DESIGN_AUTHORITY_PUBLICATION` separada
antes de un futuro handoff de implementación ACTIVE. No es una precondición
universal del protocolo ni equivale a ACTIVE/PUBLISHED ahora. Suficiencia para
AUTHORING se verifica aquí a nivel diseño, con gate competente y autorización
documental separada pendientes de sus unidades propias. El próximo lifecycle
limitado que puede autorizar el gate es publicación de autoridad de diseño,
no implementación, no handoff ACTIVE, no consumers/config productiva ni R3–R6.
Publicación y closure requerirán sus propios scope, preflight, auditor fresh y gates.

TurnoInstructor `LEGACY_VIVO / PRODUCTIVO`; R1 `CLOSED / ACCEPTED / PUBLISHED`,
dark launch `PRESERVED`. R2 diseño `DARK_LAUNCH / NON_PRODUCTIVE`, no implementación
ni beans/default/prod/callers autorizados. Cutover `NOT_AUTHORIZED`;
R3–R6 `NOT_AUTHORIZED_IN_R2`; Payments/Notifications `OUT_OF_SCOPE`;
Autopilot/FeelingPilatesOrchestrator/HostValidator `OLD_PROCESS_ONLY`.
P2-EVIDENCE-01 R1 `NON_BLOCKING / PRESERVED`, sin fabricación ni nuevos conteos.
Tests/Maven/host/JDBC/SQL/container `NOT_APPLICABLE / NOT_EXECUTED` en esta unidad
documental, nunca BUILD PASS ni green counts inventados.

## 3. Original íntegro — audit inicial FAIL

<!-- BEGIN_ORIGINAL_INITIAL_AUDIT -->
# Auditoría independiente fresh — F2E R2 design authority

Fecha: 2026-09-16. Run `run_df3cbaebd5d7`. Auditor: terminal `term_e2a41ac4-fda6-406c-86be-3d40db42daff`; Task `task_1c407b9a88a1`; Dispatch `ctx_3c018d86ea0f`. Rol exclusivamente auditor independiente de diseño: no documenter, no preflight auditor, no auditor R1. No workers delegados, correcciones, aprobación propia del autor ni modificación de repositorio.

## Resultado y alcance de la conclusión

Hay dos hallazgos P1 abiertos. GAP A está instanciado en contexto/representación/identidad, pero conserva una inconsistencia de contabilidad de rechazos; GAP B permanece incompleto por observación nativa con SQL implícita y por ese prefijo de aborto; GAP C está resuelto como decisión candidata explícita y acotada. La autoridad no alcanza todavía para autorizar la escritura de un handoff mecánico de implementación sin decisiones de reparación adicionales.

Esta conclusión examina los dos documentos candidatos existentes, no un futuro review, gate o cambio de ESTADO. El audit no publica autoridad ni activa handoffs o ejecución. R1 permanece cerrado/aceptado/publicado y su implementación exacta sigue intacta; el hallazgo de integración R2 no reabre R1.

## Evidencia, autoridad y lectura

Se inspeccionaron físicamente branch, HEAD, upstream, staging, working tree y AGENTS antes de evaluar. Se siguió la precedencia README/ESTADO, canónicos y handoff pertinentes, checkpoints/reviews concretos, protocolo completo. Se leyeron ambos candidatos completos, el diseño F2E principal y la autoridad de identidad semántica completos, los canónicos de arquitectura/decisiones/dominio/mapa, el handoff R1 consumido y sus cláusulas pertinentes, reviews F2E/R1 y los seis documentos del preflight. Se leyó README/WORKFLOW/STATE-MACHINE/GATES/ROLES de orquestación; no se dedujo una nueva fase desde conversaciones.

Clases de evidencia:

- **NORMATIVE existente:** AGENTS, README/ESTADO, diseño principal F2E (en particular D8, D12.1–12.3, D13, D18–24, D26–28, D31), identidad semántica, ARQUITECTURA-ACTUAL, DECISIONES-ARQUITECTONICAS, contexto/DOMINIO-FUNCIONAL, contexto/MAPA-LEGACY-Y-MIGRACION y protocolo de orquestación.
- **NEW_CURRENT_R2_INTEGRATION_DESIGN_DECISION_CANDIDATE:** el nuevo diseño R2. Sus instancias concretas pueden refinar autoridad previa, pero no constituyen hechos históricos ni decisiones ya aprobadas/publicadas. El nuevo handoff es DESIGN/RESEARCH/provenance, NOT_ACTIVE_IMPLEMENTATION_HANDOFF.
- **SUPPORTING:** implementación/core/schema/migraciones/guard/testinfra físicos, reviews y handoffs consumidos, reportes de materialización/coordinador/preflight y fuente local del driver. Se inspeccionaron independientemente; no sustituyen autoridad productiva ni el gate competente.
- D36–37 del diseño principal siguen siendo R1-only. Sólo las adopciones expresas de normalización/catalog ID D36.4–36.5 y framing LP/SEQ D36.8 se aceptan como primitivas neutrales. No se importan fórmulas, claim, contexto, registry, orden/count o aceptación Reserva por analogía.

La consulta read-only de gates corrobora `run_fb92631a2300 / gate_7e4a087bd1ce` resolved/PASS, semántica R1 CLOSED/ACCEPTED/PUBLISHED/DARK_LAUNCH_PRESERVED, y `run_b000b8a5b647 / gate_236c6b0bdf43` resolved/PASS sólo para una unidad separada de materialización DESIGN/RESEARCH. Los PENDING del corte documental previo no se reinterpretan como reapertura R1. El preflight encontró A/B/C; su PASS no auditó estos bytes nuevos ni autorizó implementation handoff o implementación.

## Hallazgos residuales

### R2-DESIGN-B-01 — P1 — observación nativa exige SQL fuera del CLOSED_SET

Ubicación exacta: `auditoria/fase-2e-r2-diseno-lector-turno-legacy-integracion.md`, §4 líneas373–435, §6.2 líneas544–558 y §6.3 líneas605–612. §4 sella sólo MEMBERS, ASSIGNMENTS e isolation/readOnly/snapshot, y excluye SQL de schema y funciones nuevas. §6.2 exige catalog/schema reales y repetición final de metadata nativa, pero declara que DoReturningWork sólo inspecciona recurso local y no ejecuta SQL. §6.3 fija el orden/prefijos de captura y ausencia de SQL para resource inicial inválido.

La fuente local independiente de pgjdbc42.7.11 demuestra que `PgConnection.getSchema()` no es observación local: `org/postgresql/jdbc/PgConnection.java` líneas1717–1725 crea Statement y ejecuta `select current_schema()` en cada llamada. `getCatalog()` líneas1130–1141 además ejecuta `select current_catalog` cuando su cache es null; no se afirma que esta segunda consulta ocurra siempre. La consulta de schema basta por sí sola para el bloqueo. Fuente inspeccionada leyendo el ZIP, sin ejecutar Java ni JDBC: `/Users/jesusaldaircruzortiz/.m2/repository/org/postgresql/postgresql/42.7.11/postgresql-42.7.11-sources.jar`, SHA-256 `5156b9a1076e69ede16266ceb0c20a7ecd0f3f7d5a5a388480333ec8339ee198`.

Si se obtiene el schema nativo por ese método, hay una shape no catalogada y explícitamente prohibida. La observación inicial antecede la capture/probes y la final sucede dentro de la ventana sellada: los manifests y prefijos no describen todas las ejecuciones. Como inferencia estática de estas llamadas internas, Hibernate StatementInspector no ve el Statement creado dentro del driver; una envoltura exterior de Connection/PreparedStatement tampoco demuestra automáticamente su captura. No se confunde inspector con evidencia de ejecución.

Si se evita la consulta sustituyendo schema observado por descriptor/cached expected o metadata transformada, se infringe la independencia exigida por §6.2. El diseño no fija una tercera solución demostrada que observe esos labels reales sin SQL. No basta relegar el problema a la implementación o contar sólo los statements Hibernate. Debe reconciliarse explícitamente observación nativa, catálogo, clasificación/capture y prefijos, sin que el implementador invente permiso. No se propone ni aplica una corrección en este audit.

### R2-DESIGN-AB-02 — P1 — aborto de MEMBERS impide calcular K para header inválido con keys suficientes

Ubicación exacta: candidato R2 §2 líneas81–93 y138–140 frente a §6.3 líneas605–608; autoridad previa `auditoria/fase-2e-diseno-adapters-read-only-snapshot-consistency.md`, D12.3 líneas764–770. D12.3 exige un rejection por cada attempted logical atom, calculando K por memberships y assignments cuando el header es inválido; sólo la fila sin key suficiente usa unidad por ordinal. El candidato afirma preservar K cuando las keys permiten calcularlo, pero ordena que MEMBERS access/payload inválido corte antes de ASSIGNMENTS.

Contraejemplo concreto de payload proyectado inválido: turnId y un memberInstructorId son UUID válidos; `createdAtTechnical` requerido es null. En una variante hay cero assignments y K=1; en otra hay tres assignments con distintas activity PKs para ese miembro y K=3. La proyección MEMBERS es idéntica en ambas. Las keys físicas suficientes están presentes y permiten consultar las assignments; el error requerido del header impide publicar sources, no elimina esas keys. Al cortar antes de la segunda consulta no se puede emitir el número exacto de rejections exigido por D12.3. El fallback de ordinal no aplica porque las keys no faltan.

El contraejemplo es un caso negativo de contrato/proyección para el diseño, no una afirmación de filas null existentes en el schema sano. Tampoco exige continuar una transacción tras un fallo JDBC de acceso. El conflicto específico es el payload inválido correlacionable, indistinguible con el prefijo declarado. Debe distinguirse ese caso y reconciliarse el orden de validación/captura y contabilidad con D12.3; no se puede elegir silenciosamente K aproximado o relajar la autoridad anterior.

## GAP A — instancia de contexto, identidad y representation

Evaluación: **INCOMPLETE por R2-DESIGN-AB-02; el resto de la instancia de identidad está concretamente definido, no diferido**.

§§2–3 fijan port independiente `readForDate(fecha,salonIds)`, fecha requerida, salons no-null/no-empty, validación de elementos, duplicados y orden determinista, derivación Sunday0 sin clock ni caller weekday; scope canónico único y validación antes de SQL/registry. El descriptor de fixture es test-owned/private, con sourceName y schemaFingerprint observados en bootstrap, no assertions del caller. Contexto R2 propio de diez campos y registry propio por ApplicationContext; key LP/SEQ de fixture/run/attempt, reserva atómica y outcomes ACTIVE/SUCCESS/ABORTED/UNKNOWN vinculados a completion real, sin importar registry R1 o retry interno.

Se verificó mecánicamente el conteo real: **26 keys observables distintas**, expandiendo las celdas con dos nombres. Son turnId, type, active, salonId, dayOfWeek, date, turnStart, turnEnd, createdAtTechnical, updatedAtTechnical, memberInstructorId, instructorId, activityId, assignmentStartRaw, assignmentEndRaw, effectiveStart, effectiveEnd, rangeRule, atomKind, evidenceKey, membershipStatus, markers, historyStatus, historyMarker, scenarioPolicy, evaluationDate. Las veinte keys metadata expresamente enumeradas son disjuntas, por lo que normalizedFields es exactamente46, sin mapa adicional caller. MAP usa sólo observableFields26; sourceFingerprint no incorpora su propio hash ni provenance/execution. No hay recursión, dependency sobre target desconocido ni intent reconstruido.

LP/SEQ, UTF-8 estricto, sorting unsigned UTF-8, UUID textual lower y orden unsigned de UUID/tuplas, fecha/time/UTC a micros, booleans/short, NULL físico frente a ABSENT sin fila, markers cerrados y entradas contadas quedan instanciados. El orden natural Java UUID se usa únicamente para binding/expansión, no para identidad, SQL/checksum ni PK compuesta.

Las cinco fórmulas D13 están concretas: execution depende de run/attempt/source/schema/projection enum/rules/zone/scope; logical depende de execution/claim/evidence; sourceFingerprint de schema/projection pair/source kind/identity/MAP26; snapshotIdentity de logical/kind/identity/sourceFP; readSetFingerprint de entries ordenadas, incluso vacío. §3.1/§3.4 declaran expresamente `.name()`=`R2_LEGACY_TURN_V1` para execution y la pareja `R2_LEGACY_TURN_PROJECTION`/`V1` para source/provenance: no hay una conversión ambigua pendiente. La evidencia snapshot se mantiene privada del owner y su hash pasa al context; el context no prueba RR por mera assertion del caller. La validez de esa observación sigue bloqueada por B-01, no por bytes indefinidos.

Los siete campos existentes de EvidenceProvenance se preservan; recordIds posicionales físicos no se ordenan ni inventan. Assignment URN conserva la PK triple D12.3; gap URN ABSENT/member y marker no se presenta como PK de una fila inexistente. K éxito y nonmembers quedan fieles, con la excepción de contabilidad de aborto descrita. Los trece markers estructurales están cerrados, ocho representables y cinco reject-only; history marker es distinto. Fallback rule `LEGACY_FULL_TURN_FALLBACK` y marker `FULL_TURN_RANGE_FALLBACK` son distintos. Rango incompleto/fuera retiene evidencia literal sin clipping ni intervalo elegible inventado.

Fecha/tipo/active son source facts; evaluationDate, rangeRule/markers/scenarioPolicy son derivaciones declaradas. Timestamps created/updated son técnicos y no vigencia/historia. Puntuales usan los dos escenarios legales LEGACY_EXCEPTION_UNKNOWN_INTENT y LEGACY_CANCELLATION_UNKNOWN_INTENT; UNKNOWN_INTENT domina anomalía. El shorthand PUNCTUAL no se adopta; claim histórico recurrente queda downstream y no convierte puntual en escenario ilegal. No se reciben claims semánticos ni se generan candidates/results. LegacyTurnReadSet tiene sólo sources, copias inmutables y retorno completo o total aborto pre-core, sin metadata envelope ni parciales.

## GAP B — SQL, owner RR, capture, privileges y no-write

Evaluación: **INCOMPLETE por B-01 y AB-02**. Se reconoce la materialización concreta de los demás requisitos; hashes correctos o una buena intención de test no resuelven estos conflictos.

Se verificaron las dos DATA SQL literales: header/members LEFT JOIN sin Cartesian y assignments de todos los parent IDs derivados, incluso nonmembers; sin parents se omite assignments. Predicado active RECURRENTE por weekday derivado y EXCEPCION/CANCELACION por fecha exacta, sólo fecha+salones. Once aliases/scalars header y cinco assignments están definidos por ordinal y clase exacta: UUID, String, Boolean, Short, LocalDate, LocalTime y OffsetDateTime según columna. La expansión natural Java UUID tipada se separa expresamente del orden físico PostgreSQL/canónico/checksum unsigned.

Recomputación documental independiente de todos los canonical byte lengths y Catalog IDs (SHA-256 de dominio/framing declarado, sin SQL ejecutada):

| Shape | Bytes UTF-8 | Catalog ID recomputado |
| --- | ---: | --- |
| R2_LEGACY_MEMBERS_V1 | 579 | 9852b6e9487a71cb47d76e834e82eceafcfbdd194b6e66d718f7da1ccdf3a769 |
| R2_LEGACY_ASSIGNMENTS_V1 | 290 | 6b21c28ee8961f783e986704604791181aa175592abc0b73553fa321445ce219 |
| R2_TX_ISOLATION_V1 | 47 | 4a669a2f628e12468e0d532889e0bcafd38c09a5aadfb27f2f20f56159c1671e |
| R2_TX_READ_ONLY_V1 | 47 | 9963ea856cdf9bfb3e9c440b1c3a6c062fd375a906f465cbe7a7ac88878716c7 |
| R2_TX_SNAPSHOT_V1 | 34 | 24f02692f50e880b77a78f9ea64501ce276b7b0e2a93c880a4c23e03bbe19aa0 |

Todos coinciden con el documento y SQL-CATALOG-VERIFICATION. La igualdad de probes I/R con R1 sólo refleja texto/framing; no transfiere owner/RC/counts. El catálogo matemático es determinista, pero **no cubre la observación nativa exigida**.

§§5–6 distinguen plan/catalog/StatementInspector, setters de binds reales, execute/ResultSet reales y recurso físico; no atribuyen valores/execution a inspector. Hay grafo R2 único/qualified DS/EMF/EM/TM/PU/inspector, sin importar config Reserva, sin segundo admin EMF/TM; owner real proxy REQUIRES_NEW/RR/readOnly distinto del reader MANDATORY. Native original PgConnection y bound holder/session/EM/TM/DS se contrastan antes/después de cada statement y al final, labels nativos/configurados por separado, sin transformar URL para hacer match. Completion real impide provisional escape; invalid caller/context no SQL ni reserva; initial I/R/S antes de construir context; final I/R/S y snapshot textual inicial/final igual, sin retry/fallback. Es una instancia sustancial, pero la afirmación guard local sin SQL no es implementable como está sellada.

La concurrencia RR está controlada MEMBERS→writer real commit→ASSIGNMENTS; read RR conserva snapshot y una lectura posterior con nuevo attempt observa el cambio. RC negativo es raw/reference test-only separado; no accepted R2 RC context/readForDate ni fallback. El reader en RC falla antes de DATA. No se mezcla la ventana concurrente con checksum quiescente.

§7 fija principal SELECT-only exactamente sobre turno_instructor, turno_instructor_usuario y turno_instructor_asignacion, revocaciones efectivas/PUBLIC/memberships y no permisos Reserva/Flyway/maestros/sequence/app functions. Los controles INSERT/UPDATE/DELETE/DDL denegados42501 ocurren en conexiones/TX separadas antes de medición; un write permitido aunque rollback no cuenta como denied. Setup/Flyway son writes reales previos, no zero global writes.

Checksum propio R2 cubre todas las columnas persistidas vivas observadas del schemaV47: parent10, membership2, assignment5; no antigua usuario_id dropped, ni schemaordinal muerto. Column names/type tags/null-presence/column count/composite PK components con unsigned order y domains R2 quedan concretos. Parent IDs se congelan scoped; children se seleccionan afresh antes/después detectando inserción/deletion, parent membership del predicado se recompara para detectar scope change; vacío produce exactamente tres tables vacías. No checksum global, no reuse de Reserva selectors/domains/simpleUUID. No-write exige ventanas quiescentes antes/read completion/después, no estadísticas o rollback como sustituto. Estas decisiones son refinamientos coherentes, no migración ni activación.

## GAP C — guard y siete capacidades D24.1

Evaluación: **COMPLETE como decisión candidata nueva, explícita y acotada**, sujeta a gate/publicación y futuro handoff exacto; ningún guard cambió ahora.

Se leyó completo ReservaJpaReaderArchitectureTest. El rootwalk recursivo actual compara sets literales exactmain11/test10 y lee todo Java en read/adapter.jpa; también conserva prohibiciones de framework/managed-resource/write/config/bean, singleton enum R1 y MANDATORY TM explícito. No se dedujo permiso de abrirlo por existencia de código o por D24.1 genérico.

§8 elige sólo una excepción futura al path `src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/ReservaJpaReaderArchitectureTest.java`: preservar sets literales R1 11/10, invariantes existentes/singletons, declarar por separado futuros sets R2 main/test SEALED y disjuntos y comparar el walk global con unión exacta, sin extra/missing/overlap. No prefix filter, root hiding, skip ni sets R2 adivinados. El futuro allowlist exacto requiere otro handoff/audit; no se cambia ahora este test ni se concede escritura a otros tests R1 por asociación. Es permiso candidato nuevo explícito, no permiso automático histórico D24.1.

Se inspeccionaron los cinco fuentes testinfra completos, el guard y RuntimeIsolationTest, no sólo nombres/clases. Clasificación de cada capacidad exacta recuperada de D24.1:

| Capacidad | Clasificación candidata | Evaluación independiente del source completo |
| --- | --- | --- |
| PostgreSQL container | REUSE_AS_IS, capacidad aislada | Factory postgres:16-alpine separable; no importa config completa con beans/fixtures Reserva. R2 graph independiente. |
| Flyway boot | REUSE_AS_IS, capacidad aislada | migrate/validate/schema-fingerprint neutral separable; r1-a, applied50/current47 y fixtures son hardcodes R1 que no se convierten en umbrales/fixture R2 universales. |
| SELECT-only setup | EXTEND_WITH_R2_NEUTRAL_CAPABILITY | F2eSelectOnlyRole tiene principal/grant reserva y wrappers/negatives R1; instancia R2 tres tables y preservación exacta de factory/default R1, no wholesale reuse. |
| SQL inspection | EXTEND_WITH_R2_NEUTRAL_CAPABILITY | Inspector actual cuatro entries R1 y snapshot denied; neutral normalizer/hash/capture más catálogo R2 separado sin ampliar default R1. B-01 impide declarar suficiente todavía el catálogo R2. |
| Checksum canonicalizer | EXTEND_WITH_R2_NEUTRAL_CAPABILITY | F2eSliceChecksum actual Reserva selectors/simpleUUID/domains; R2 seam all-column/compositePK separado con bytes/goldens R1 intactos. |
| Architecture rule | EXTEND_WITH_R2_NEUTRAL_CAPABILITY | Sólo excepción futura exacta §8; literal R1 sets y global union strict preservados, no importación amplia de tests. |
| Transaction test owner | NOT_REUSABLE, runtime actual | ReaderTransactionTestHarness hardcodes Reserva API/context/registry/manifest y RC; requiere owner RR R2 separado. Conceptos resource/completion son supporting, no import de harness completo. |

La distinción capacidad/fuente completa es real y consistente. Toda extensión shared requiere path exacto autorizado y evidencia futura de preservación de semántica R1; productionR1 y core/contracts/migrations/pom/product config permanecen read-only. No se deduce que los cinco seams deban necesariamente modificarse ni que tests R1 no compartidos sean escribibles.

## Fronteras, propuestas y perfil

TurnoInstructor sigue LEGACY_VIVO/PRODUCTIVO. R2 es sólo DARK_LAUNCH/NON_PRODUCTIVE, clases plain constructor DI, wiring/proxies de prueba explícitos, beans default/prod ausentes y cero callers productivos. No controller/job/listener/consumer/configswitch/activation ni report sink/material audit. Cutover NOT_AUTHORIZED. R3 nominal candidates, R4 ajustes NEW_* de fecha exacta, R5 effective graph/backing y R6 RR cross-source/shadow/composition quedan fuera; no targets/crosswalk/resolver/fence/migración. Payments/Notifications OUT_OF_SCOPE; Autopilot/orchestrator/HostValidator antiguos OLD_PROCESS_ONLY.

§10 separa aceptación futura competente de hechos ya probados. Golden vectors nuevos, ordered commitments/pre-callback fingerprints R1, exact Maven commands, futuros test/file counts y los históricos R1 59/649/host7/native4/TECH first-later no son acceptance R2 vigente. No se inventan tests ejecutados ni SQL runtime. Las instancias nuevas de SQL/context/identity/checksum/owner/guard son decisiones actuales candidatas expresas, no historia normativa ya publicada.

El perfil actual es DESIGN_RESEARCH/LOCAL_MATERIALIZATION; publicación de autoridad no está incluida ni ocurrió. La siguiente unidad separada propuesta `R2_DESIGN_AUTHORITY_PUBLICATION`, antes de un implementation handoff ACTIVE, es una decisión profile-specific R2 explícita, **no requisito universal del protocolo**. AUTHORING de handoff se distingue de publicación/ACTIVE y exige autorización separada tras diseño competente auditado/gated. Este audit FAIL impide declarar hoy esa suficiencia: corresponde tratamiento/corrección de los P1 en una unidad autorizada, nueva verificación independiente/gate, y sólo después evaluar AUTHORING separado y publicación bajo el perfil aprobado. No se inventa una autorización de corrección, publicación, implementación o una próxima fase ejecutable. El coordinador conserva la responsabilidad de persistir este reporte exacto en el review declarado y resolver el gate/estado real, no el auditor.

## Doce respuestas obligatorias

| # | Pregunta | Respuesta explícita |
| --- | --- | --- |
| 1 | ¿Definición R2 fiel? | Parcialmente: scope, sources-only, PK/URN, escenarios y límites sí; no fidelidad completa de bookkeeping D12.3 por AB-02. |
| 2 | ¿GAP A instanciado sin diferir? | NO completamente. Contexto/26+20 keys/bytes/D13/provenance están instanciados; K de rechazo no es ejecutable con el prefijo definido, AB-02. |
| 3 | ¿GAP B instanciado sin analogía R1? | NO completo. Hay instancia propia sustancial y no analogía RC/contexto, pero B-01/AB-02 quedan abiertos. |
| 4 | ¿GAP C resuelto sin debilitar guards R1? | SÍ como decisión candidata bounded §8: sets R1 preservados, sets R2 sealed futuros, global union exacta y único path excepcional. Nada cambia ahora. |
| 5 | ¿Owner/recurso RR implementable/auditable? | NO tal como sellado: proxy/graph/completion definidos, pero labels nativos sin SQL contradicen pgjdbc y el CLOSED_SET, B-01. |
| 6 | ¿SQL/capture cerrados deterministas suficientes para handoff? | NO. Los cinco lengths/IDs sí son correctos; consulta driver implícita y prefijo K no resueltos, B-01/AB-02. |
| 7 | ¿Identidad/provenance/canonical suficiente? | SÍ respecto a representación determinista y fórmulas: enum.name y projection pair expresos, MAP26/union46 y no recursión; trust operacional del recurso pendiente de B-01. |
| 8 | ¿Shared testinfra seguro para R1? | SÍ en el diseño de ownership/capacidades y preservación, sujeto al futuro handoff exacto y pruebas competentes; no se ha modificado ni probado runtime ahora. |
| 9 | ¿Fronteras productivas preservadas? | SÍ: legacy productivo, R2 no productivo/default-prod absent, cero activación/callers autorizados. |
| 10 | ¿Sucesores excluidos? | SÍ: R3–R6 y cutover/targets/crosswalk/resolver/fence/report sink/material audit/migración no autorizados. |
| 11 | ¿Proposed-only claramente no normativo? | SÍ: candidatos nuevos, NOT_YET_NORMATIVE e históricos R1 expresamente separados; profile-specific publication no universalizada. |
| 12 | ¿Autoridad suficiente para authoring mecánico de handoff sujeto a publicación/ACTIVE? | NO hoy por dos P1. Tras corrección autorizada y audit/gate competente puede reevaluarse AUTHORING separado; publicación propia del profile precede ACTIVE, sin permiso actual de implementación. |

## Prueba física before/after y no intervención

Repo `/Users/jesusaldaircruzortiz/Desktop/Feelingpilates/feelingpilates`. Snapshots independientes de entrada y final recomputados desde bytes físicos y Git con GIT_OPTIONAL_LOCKS=0, no sólo comparando conclusiones del materializer/coordinador. Ambos snapshots son idénticos entre sí y al baseline autorizado para tracked/ignored/index/refs/R1. El baseline preexistente del auditor son únicamente dos NEW documentos untracked; no se atribuyen al auditor.

| Medición | Before | After |
| --- | --- | --- |
| Branch | operacion/excepciones-horario-fecha | igual |
| HEAD / upstream | 6c2eacc870499e74ead74c1851630f9f53b1c676 | igual |
| Ahead/behind | 0/0 | 0/0 |
| Tracked paths / changed hashes | 460 / 0 | 460 / 0 |
| Ignored paths / changed hashes / path delta | 660 / 0 / 0 | 660 / 0 / 0 |
| Inventory SHA | d7964ab357d282a21b3289f0511f6289a3e32f8dd0a13a615f8a4dd93384d5cd | igual |
| Index SHA / staged names | b053b254ae09434071a7dcd9922c2d2f3dbc6edde0ddaa2935c763432527e682 / vacío | igual |
| Refs SHA | e75a76fb06f4b02f953961c777ee0d8c776b1f6cbcb980b69dd98656ff097e8b | igual |
| R1 exact paths | 21 (main11/test10) | 21, sin delta |
| R1 path SHA | f400a0602f95e318845da670bee4f819f057842adf8a60506564d5bd75e41d14 | igual |
| R1 content SHA | e2b64abd6aba8a050df6184f6c5440d87db83a67182fb43e622f48cf96f4f3ce | igual |
| git diff --check / cached --check | exit0 / exit0, sin output | igual |

Fresh `GIT_OPTIONAL_LOCKS=0 git ls-remote --exit-code --heads origin refs/heads/operacion/excepciones-horario-fecha` exit0 devuelve exactamente `6c2eacc870499e74ead74c1851630f9f53b1c676`, incluyendo la repetición final; no fetch ni escritura de refs. Untracked antes/después: sólo los dos candidatos abajo. Ambos pasan comprobación explícita de whitespace de archivo completo: cero trailing whitespace, cero CR y final LF presente. No se expusieron contenidos de archivos ignored/secretos. implementationDelta0, repositoryDeltaByAuditor0.

No tests, Maven/build, contenedores, SQL/JDBC, mecanismos antiguos, migraciones, staging, configuración, commits/push o activaciones se ejecutaron. No failures/errors/skips de suites nuevos existen porque no se ejecutaron; no se fabrican conteos. Única escritura autorizada del auditor: este reporte externo mediante apply_patch. No hay comentario operacional adicional sobre una ejecución inexistente.

## Inventario de hashes de entrada y candidatos

Todos SHA-256 físicos recomputados. Rutas de la primera tabla relativas a `/tmp/feelingpilates-f2e-r2-design.iUQKrV/`.

| Entrada | SHA-256 |
| --- | --- |
| BASELINE.json | fd5d6233566a1dee2b911d290f9d6e0d67b872816f5e82d858e69bb5a1e20cab |
| IGNORED-BASELINE.json | 0c68ef42982bbd9b0584d685422c548c114c7beeafb9f33191b4a4a20055e2ca |
| R1-IDENTITY.json | e2ce99868dc6747697028e0f9c928285b29c45de8c81827b7a4251aa99aedda0 |
| MATERIALIZER-REPORT.md | cb7786eb20b83b13d01ba7893fd74141b504e33cab1fcaae53bffb670b482b2e |
| COORDINATOR-VERIFICATION.md | 7cc5da60cfbfd9521024e69baefb8bc191ff75481971c69304a970974b97a9a3 |
| COORDINATOR-AFTER-WORKER.json | 0822deece28f1b06a54d26b2b0b08e90cb38efcf3656d00e64d231636dbcb100 |
| SQL-CATALOG-VERIFICATION.json | 4d6a7300373c0896c63e0df91f0ca9c588da07b7f82f8e6721e98eea6ee6638f |

| Candidato / fuente normativa relevante | SHA-256 |
| --- | --- |
| auditoria/fase-2e-r2-diseno-lector-turno-legacy-integracion.md (881 líneas) | 3a6eb4e4b38ff90b66e85680c407ae808dbba471b21d4be79dd2c321cd77afaa |
| auditoria/handoffs/HANDOFF-F2E-R2-DISENO-LECTOR-TURNO-LEGACY.md (193 líneas) | e9c56d7beb47f8558a756fe2e132aa3874069fd902cbb0d26270646256c1bb83 |
| AGENTS.md | 1d70ebbc5bfef3c0004a7ca53ce8a91bf6a9660a4085e2c7ff2a44a5d58038c4 |
| auditoria/README-REESTRUCTURACION.md | 747227acc83ad7337d8a1214646ee089def5ed42ec6eee637d22ff96969ce2b2 |
| auditoria/ESTADO-ACTUAL.md | cd3d7d71923eb80cd951a01f6618d6dc6c2a19105becedd0e21f0e22c005a9b1 |
| auditoria/fase-2e-diseno-adapters-read-only-snapshot-consistency.md | 6c72cba1f83fbc2bcf3b3219d8252d2410ec482e30ae85d30fd2eeb04e8883d8 |
| auditoria/fase-2e-identidad-semantica-detector-read-only.md | 6f850e9723f9861456d646039e4b233cff20d013ff956cab98f7370dffac4670 |
| auditoria/ARQUITECTURA-ACTUAL.md | 63733821b86e89aacecbc1573c1e6d4d4b396fe6162f6871b7f810060bc81a7c |
| auditoria/DECISIONES-ARQUITECTONICAS.md | 6dc6fb6230ce794449883cd858864eebab2bdfb6cd2bc5c7ac4fa4e023ce579b |
| auditoria/contexto/DOMINIO-FUNCIONAL.md | 1abce9e0bc187a1cfb34d384faa4b340150eebd7a8e8213922e90f853cb266df |
| auditoria/contexto/MAPA-LEGACY-Y-MIGRACION.md | ee3739a10435e19e2a1f123c95a9c3446c7c7c3c72b85e56c7ef40147e72ce8f |
| auditoria/orquestacion/README.md | 21d094499f49a7c7f0878e4c183114c4ff8525446dca5e8db1151a90e7146a7c |
| auditoria/orquestacion/WORKFLOW.md | 107857275a38dfb23f094ec35054113b88de404e0ae7100c2bf527071de130d1 |
| auditoria/orquestacion/STATE-MACHINE.md | 0641e4725947d1fb2f16cf304255b0b30739090bb810488672503a206f6f13d8 |
| auditoria/orquestacion/GATES.md | b3137de84aa171dd6f11bf439c79b937e44a52de54a5cb841fd57048678d490b |
| auditoria/orquestacion/ROLES.md | 8c19f5981cad4941a34356fecde833168b1754620e167fd323b36c35490dbb50 |

Supporting sources recomputados, prefijo testinfra `src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/testinfra/`:

| Supporting source | SHA-256 |
| --- | --- |
| F2ePostgresTestConfiguration.java | ba0fc95e89b85a00dd679037c8a726ae534ad506753227245ad8c00c3d040369 |
| F2eSelectOnlyRole.java | 532502268c445a38beef4339f6fa9eced0770a0af665a29c2beada7f43f434c1 |
| F2eStatementPolicyInspector.java | 512da4beb1aa9c845aaab02097b2ab798a5fa9f7b18b0d45cb9374ad8408be94 |
| F2eSliceChecksum.java | db4757b6cb7b52087ccfa2da70505844dd935a78d016e34fdbeb64a5ca96ca25 |
| ReaderTransactionTestHarness.java | 1fe20bccf194dfe6b339aa8d8a6985d250f8e4df21b1119be57e1048b72630ef |
| ../ReservaJpaReaderArchitectureTest.java | e8303ef30a703f39b939624d6dd4d5252a8fe90ab07bd442cb81a2d976625009 |

Preflight supporting reports, prefijo `/tmp/feelingpilates-f2e-r2-preflight.7Qm8QZ/`:

| Reporte | SHA-256 |
| --- | --- |
| REPORT.md | 9eedcb08498d75071a811c27e84099619f08991d689ac9d84dcbc7598701eec3 |
| COORDINATOR-REPORT.md | c716efab2cebc754603d109766a76e8ed9ad2814778b55c5028e95c8a4a09999 |
| AUTHORITY-RECON.md | d537e2981263d3455c6439e7339cb788c87f9ce60834e62350ae33bc459297a5 |
| DEPENDENCY-RECON.md | 0309020835ca313791e5a8e0cb35200fa6ae6942ef29a8337b0e8756d136c885 |
| AUDIT-R2-PREFLIGHT.md | d6f2c8a62f337bee5db5c084d64c2793dcfde4056a32264bcfc365041b14a101 |
| GATE-RESULT.json | b54221dd752dd48b7e2c92e1fc5c633c0d3eccad745c66faed54f92a9a4cf8f2 |

Residuals explícitos: P0=0, P1=2 (B-01 y AB-02), ningún permiso de reparación implícito; implementación/pruebas/publicación/activación no iniciadas en este audit; suficiencia para handoff pendiente. GAP C no oculta ninguno de los P1 de A/B. El coordinador recibe este reporte físico y su hash, no una autoridad editorial autoaprobada.

FAIL — F2E R2 DESIGN AUTHORITY REMAINS INCOMPLETE

<!-- END_ORIGINAL_INITIAL_AUDIT -->

## 4. Original íntegro — re-audit fresh del diseño corregido

<!-- BEGIN_ORIGINAL_FRESH_REAUDIT -->
# F2E R2 — fresh independent design-authority re-audit

Date: 2026-09-16. Run `run_df3cbaebd5d7`; Task `task_9a5c9b6b0b55`; Dispatch `ctx_4d4dfda77e33`; worker `term_1ba06adf-a190-4d55-ab81-6cc3332c00c2`.
Role: FRESH INDEPENDENT R2 DESIGN AUTHORITY AUDITOR / READ_ONLY. This auditor is distinct from materializer `task_c195cda67de7 / ctx_c45b806cebbf`, original auditor `task_1c407b9a88a1 / ctx_3c018d86ea0f`, corrector `task_a2e7ce748158 / ctx_ef6c945da2a1`, and the preflight/R1 auditors. No workers were spawned. Coordinator: `term_6940950d-86ad-4a8e-a7cc-9bd3fc569779`.

## 1. Finding and authority cut

The corrected design resolves all three integration gaps and both original P1 at DESIGN level. Current unresolved findings: P0=0 / P1=0 / P2=0. This is sufficiency for a mechanical implementation-handoff authoring lifecycle under separate authorization, not implementation authorization, a publication receipt, an activation, runtime acceptance, or cutover.

The original `AUDIT-R2-DESIGN.md` remains the authentic historical FAIL with P1=2. Its 30,992 bytes still hash to `bda484b6140405491740b6c193fd120d06dbd3550cc26ee458866500f489109a`. Its pre-correction conclusions were valid for those preimages; neither correction nor this re-audit rewrites them as an original PASS. The initial materializer report and old five-shape evidence are historical inputs, not current candidate identities.

The coordinator's corrected verification is admission to independent re-audit, not competent approval. I independently read the complete corrected candidates and both complete preimages, original audit, corrector report, metadata diagnostic, corrected coordinator report/snapshot/catalog, preflight reports, primary F2E design and semantic authority. I inspected the actual supporting guard, runtime-isolation evidence, five shared testinfra sources, core contracts, migrations, and primary pgjdbc sources. No conclusion here adopts a worker/coordinator assertion in place of physical source inspection.

Physical reading followed AGENTS: branch/HEAD/index/tree first; README/ESTADO; relevant handoff and architectural/domain/semantic canons; concrete F2E design reviews and R1 closure review/handoff clauses; then the full five orchestration protocol documents. There is no separately existing R2 implementation checkpoint/intervention/ACTIVE handoff to invoke. The original audit, preimages, corrector report and this report establish this bounded CORRECT/re-audit trace.

The cut includes current baseline ESTADO only. The coordinator-reserved R2 repository review and eventual postgate ESTADO have not been authored in this cut; I did not audit future terminal metadata, future gates, future publication, or a future implementation handoff. This report itself creates no approval/gate or persistent repository authority.

## 2. Frozen bytes and independent source identities

Repository root: `/Users/jesusaldaircruzortiz/Desktop/Feelingpilates/feelingpilates`. Relative repository citations below resolve there. `D` = `auditoria/fase-2e-diseno-adapters-read-only-snapshot-consistency.md`; `S` = `auditoria/fase-2e-identidad-semantica-detector-read-only.md`; `I` = `auditoria/fase-2e-r2-diseno-lector-turno-legacy-integracion.md`; `H` = `auditoria/handoffs/HANDOFF-F2E-R2-DISENO-LECTOR-TURNO-LEGACY.md`. The external correction directory is `/tmp/feelingpilates-f2e-r2-design.iUQKrV/`; preflight directory is `/tmp/feelingpilates-f2e-r2-preflight.7Qm8QZ/`.

| Current candidate / preserved preimage | Bytes | Independently recomputed SHA-256 |
| --- | ---: | --- |
| I, corrected current design, 1,024 LF lines | 76545 | db4673dd0705c41e62d0b77339d45b2cd1e87c51e26064112ad95b843dafe9cf |
| H, corrected current research handoff, 249 LF lines | 18639 | 221347b46c5032b384a306c61908c8fd0f2c26076455609fb880098be8f0d2d4 |
| DESIGN-PRE-CORRECTION.md, historical initial design | 63853 | 3a6eb4e4b38ff90b66e85680c407ae808dbba471b21d4be79dd2c321cd77afaa |
| RESEARCH-HANDOFF-PRE-CORRECTION.md, historical initial handoff | 14053 | e9c56d7beb47f8558a756fe2e132aa3874069fd902cbb0d26270646256c1bb83 |

| External correction input | Bytes | Independently recomputed SHA-256 |
| --- | ---: | --- |
| BASELINE.json | 68409 | fd5d6233566a1dee2b911d290f9d6e0d67b872816f5e82d858e69bb5a1e20cab |
| IGNORED-BASELINE.json | 108328 | 0c68ef42982bbd9b0584d685422c548c114c7beeafb9f33191b4a4a20055e2ca |
| R1-IDENTITY.json | 2487 | e2ce99868dc6747697028e0f9c928285b29c45de8c81827b7a4251aa99aedda0 |
| MATERIALIZER-REPORT.md, historical | 17539 | cb7786eb20b83b13d01ba7893fd74141b504e33cab1fcaae53bffb670b482b2e |
| AUDIT-R2-DESIGN.md, historical FAIL | 30992 | bda484b6140405491740b6c193fd120d06dbd3550cc26ee458866500f489109a |
| CORRECTOR-REPORT.md | 22037 | 15dd2fbc8b06cf0c42000ed8f442ec20250f4ac4d8e8b96d6d21a2cba72515ee |
| NATIVE-METADATA-DIAGNOSTIC.md | 3128 | f4a71b59bc7329bf9e333a285c0cac603e912323400558a3127670a8d4ed231c |
| COORDINATOR-VERIFICATION-CORRECTED.md | 10325 | c97c2009be8f7bf9a2503315c325a6da365b86dc790158f015fb7075a9c8f80b |
| COORDINATOR-AFTER-CORRECTOR.json | 1523 | 8b3ed3e2ea8ccb8224b2592a23bd0ba876544020e0f655b64bac8ade6dcac0ee |
| SQL-CATALOG-VERIFICATION-CORRECTED.json | 2490 | ae1f9444803c595bf145e6b700da57abd23b5d9f541ab5972ada9a6a5ca397d0 |

| Preflight input, SUPPORTING traceability | Independently recomputed SHA-256 |
| --- | --- |
| REPORT.md | 9eedcb08498d75071a811c27e84099619f08991d689ac9d84dcbc7598701eec3 |
| COORDINATOR-REPORT.md, corrected synthesis | c716efab2cebc754603d109766a76e8ed9ad2814778b55c5028e95c8a4a09999 |
| AUTHORITY-RECON.md | d537e2981263d3455c6439e7339cb788c87f9ce60834e62350ae33bc459297a5 |
| DEPENDENCY-RECON.md | 0309020835ca313791e5a8e0cb35200fa6ae6942ef29a8337b0e8756d136c885 |
| AUDIT-R2-PREFLIGHT.md | d6f2c8a62f337bee5db5c084d64c2793dcfde4056a32264bcfc365041b14a101 |
| GATE-RESULT.json | b54221dd752dd48b7e2c92e1fc5c633c0d3eccad745c66faed54f92a9a4cf8f2 |

Normative sources were inspected as documents, not inferred from runtime code:

| Normative physical source | SHA-256 / relevant authority |
| --- | --- |
| AGENTS.md | 1d70ebbc5bfef3c0004a7ca53ce8a91bf6a9660a4085e2c7ff2a44a5d58038c4; physical baseline, order, ownership, role independence |
| auditoria/README-REESTRUCTURACION.md | 747227acc83ad7337d8a1214646ee089def5ed42ec6eee637d22ff96969ce2b2; repository precedence and document classification |
| auditoria/ESTADO-ACTUAL.md | cd3d7d71923eb80cd951a01f6618d6dc6c2a19105becedd0e21f0e22c005a9b1; §§R1 closure, authority/boundaries; no ACTIVE implementation handoff |
| D, full primary design | 6c72cba1f83fbc2bcf3b3219d8252d2410ec482e30ae85d30fd2eeb04e8883d8; §§8,12.1–12.3,13,18–24.1,26–30; §§36–37 R1-only except expressly adopted neutral primitives |
| S, full semantic identity design | 6f850e9723f9861456d646039e4b233cff20d013ff956cab98f7370dffac4670; §§6–10,13,17, source/target/intent/history separation |
| auditoria/ARQUITECTURA-ACTUAL.md | 63733821b86e89aacecbc1573c1e6d4d4b396fe6162f6871b7f810060bc81a7c; legacy authority/dark launch |
| auditoria/DECISIONES-ARQUITECTONICAS.md | 6dc6fb6230ce794449883cd858864eebab2bdfb6cd2bc5c7ac4fa4e023ce579b; DA004/DA012/DA013 |
| auditoria/contexto/DOMINIO-FUNCIONAL.md | 1abce9e0bc187a1cfb34d384faa4b340150eebd7a8e8213922e90f853cb266df; temporal/product boundaries |
| auditoria/contexto/MAPA-LEGACY-Y-MIGRACION.md | ee3739a10435e19e2a1f123c95a9c3446c7c7c3c72b85e56c7ef40147e72ce8f; LEGACY_VIVO and separate cutover |
| auditoria/orquestacion/README.md | 21d094499f49a7c7f0878e4c183114c4ff8525446dca5e8db1151a90e7146a7c |
| auditoria/orquestacion/WORKFLOW.md | 107857275a38dfb23f094ec35054113b88de404e0ae7100c2bf527071de130d1; profile/block composition, documentary correction and separate publication |
| auditoria/orquestacion/STATE-MACHINE.md | 0641e4725947d1fb2f16cf304255b0b30739090bb810488672503a206f6f13d8; independent state dimensions/transition evidence |
| auditoria/orquestacion/GATES.md | b3137de84aa171dd6f11bf439c79b937e44a52de54a5cb841fd57048678d490b; applicability and competent actual gates |
| auditoria/orquestacion/ROLES.md | 8c19f5981cad4941a34356fecde833168b1754620e167fd323b36c35490dbb50; no self-audit, role separation |

The consumed R1 handoff SHA is `3fd71faca4d4c049ad5cb37b52bc6fd512509cf5b696bdc5c28d28cb966af8ef`, its exhaustive §3 and boundaries §§15–16 preserve R1 only. The R1 consolidated review SHA is `b324a51ddae26e00bb9b8714c48402a3001f0c99ef5f2fd1db7038ebd5adfde9`; §12 preserves closure chronology, historical FAILs and TECH/count limitations. The original adapters design review and residual R1 topology review corroborate their accepted historical scopes, not a new R2 approval.

I/H are NEW R2 INTEGRATION DESIGN DECISION CANDIDATES. Their R2-ID/SQL/RR/CHECKSUM/ARCH/PROFILE instances are legitimate subjects of this audit, not purported historical primary-design facts. Preflight/reports/Orca are SUPPORTING provenance. Schema/Java/tests/driver demonstrate implementability and constraints, not new productive authority. D33's successor handoffs remain proposals. Existing R1 authority is normative only within its sealed scope.

Fresh read-only Orca `gate-list` independently corroborated R1 `run_fb92631a2300 / gate_7e4a087bd1ce` resolved/PASS at `2026-09-16 20:51:36`, and preflight `run_b000b8a5b647 / gate_236c6b0bdf43` resolved/PASS at `2026-09-16 21:21:17`. The former closes R1, the latter permits bounded R2 design materialization only. Neither approves current I/H or implementation. Written PENDING labels in sealed R1 documents retain their historical documentary cuts.

## 3. Correction scope and preserved design

I compared every preimage/current byte through full reads and line-diff calculation. Changes are only correction status/provenance, the two residual mechanisms and their dependent binding/capture/role/acceptance/traceability references. No unrelated new domain decision or successor scope was introduced by CORRECT.

Design §3 (the entire context/bytes/maps/formulas/provenance contract), §8 (entire bounded architecture strategy), and §9 (entire seven-capability classification) are byte-identical to their preimages. Their independent section hashes are respectively `3ebe0791a30f666f12b2f07a28bba98d50286e3034e7bc4be27723718e375be8`, `d084bd61b4c0b5813e984666dce2ba7aa65164efd0f7ed30f0045aa369a6e6a1`, and `e6fa3b41c5f99fe586771565d1136ca60de8d24868ecdb8978fb3ed22808a156`.

Actual I changes concentrate at lines3–38 (chronology),81–83/164–209 (AB02),432–574 (six-shape/projection/binding references),630–745 (native metadata/capture/failures),766–770 (necessary built-in access),917–920 (dependent acceptance),951–953/988–1012 (pending status/trace). H changes are equivalent chronology/scope/contract references, not an implementation allowlist. The RESOURCE addition observes native metadata, reads no new table and supplies no new functional source. Key-first enumeration changes when a pending projected error is emitted; it does not legalize that payload or change K/URN/source semantics. The existing profile-specific future publication decision was already in the preimage and remains intact.

## 4. GAP A — instantiated context, representation and source identity

Assessment: COMPLETE at design level. Basis: I§§2–3, H§§3/5, D§§8/12.3/13/18.2 and S source/history/intent clauses.

R2 has its own exact ten-field persistence-agnostic `LegacyTurnReadContext`, separate enum `R2_LEGACY_TURN_V1`, projection contract `R2_LEGACY_TURN_PROJECTION/V1`, and internal claim `R2_INTERNAL_RR_TEST`. No extension/import of R1 context/identities/singletons is assumed. An immutable test-owned descriptor observes schema/Flyway and binds trusted source labels and graph references. Caller seeds cannot assert those resource facts. A separate atomic fixture/run/attempt registry owns ACTIVE/terminal state; invalid seed/scope does not reserve, reuse fails before invocation SQL, completion evidence controls terminalization, markers survive rollback/release, interruption does not become success, and external retry requires another attempt. This is a bounded R2 decision, not inherited R1 uniqueness machinery.

Scope rejects null/empty/null members, copies and validates UUID inputs, explicitly rejects serialized duplicates and derives Sunday0/date predicates; canonical set ordering and immutable envelope are sealed. Assignment identity uses exactly the physical triple PK URN, gaps exactly the evidence URN with ABSENT/member and marker. Positional `recordIds` preserve physical roles even equal UUID values; no invented assignmentId/absence PK, intent or temporal history. Facts, derived structural markers and unknown intent are distinguished.

I§3.2 closes strict UTF-8, LP/SEQ nested framing, lower hex SHA-256, UUID lower form, boolean/Short/date/time/UTC technical microseconds, NULL versus ABSENT versus empty sequence, required strings, and unsigned key/identity/PK ordering. Natural Java UUID parameter ordering remains a separate binding rule. No default zone/clock, trim/casefold/Unicode normalization, permissive scalar conversion or sub-microsecond truncation is accepted.

Independent parsing of the actual observable table expands comma-grouped keys to EXACTLY26 unique keys; metadata block has EXACTLY20 unique keys; intersection empty, normalized union EXACTLY46. `sourceFingerprint` hashes MAP26 only, not its own provenance IDs. The five D13 formulas retain their generic domains/components with explicit R2 inputs and framing. Execution → logical snapshot → atom snapshot dependencies are acyclic; source/readset hashes describe source content, not MVCC or unknown history. Final maps/provenance/URN/recordIds/context share the exact versioned instance and require independent recomputation/cross-record agreement; digest-preimage collision or drift aborts. `EvidenceProvenance`'s actual seven-field core contract supports this without a core modification.

Raw range facts survive nullable/full fallback/incomplete/outside cases. Full-turn rangeRule and its structural marker are distinct literals; incomplete/gap effective values are ABSENT, invalid ranges remain evidence and never become invalid eligible intervals. Punctual scenarios are exactly `LEGACY_EXCEPTION_UNKNOWN_INTENT` and `LEGACY_CANCELLATION_UNKNOWN_INTENT`; UNKNOWN_INTENT dominates punctual anomaly. `LEGACY_PUNCTUAL_UNKNOWN_INTENT` is expressly illegal. Recurrent anomalous evidence uses INCOMPATIBLE_EVIDENCE; complete recurrent policy delegates only later legal claims. Technical timestamps never establish functional history; currentSnapshot-only and downstream UNKNOWN_HISTORY remain distinct. Physical classifier compatibility was checked, not used to invent these rules.

`LegacyTurnReadSet` has only immutable sources, complete successful empty/nonempty scope or total operational abort pre-core. It carries no candidates/classification/report sink/metadata, managed entity, stream or proxy. The corrected exact-rejection bookkeeping in §5 below closes the A/B count obligation. No integration instance is left for an executor to choose architecturally; future filenames/commands/vectors are separately authorized handoff details.

## 5. Original P1 R2-DESIGN-AB-02 — exact rejected attempted atoms

Historical severity: P1. Original failure was I-preimage§6.3's immediate MEMBERS payload abort, contradicting D12.3's exact K rejections when valid parent/member keys allow assignments to determine K. An invalid but correlatable header with0 versus3 assignments was indistinguishable before query2.

Current disposition: CLOSED BY THIS INDEPENDENT DESIGN RE-AUDIT, not by corrector self-approval. Exact corrective clauses: I§2 lines164–195/205–209; §5 lines553–565; §6.3 lines721–745; §10 Abort row line917; H§5 lines176–190.

The private key-first ledger collects validated non-null physical parent UUIDs and correlatable membership facts independently of non-key required/type/header validity. That invalid payload remains pending and never becomes a source or classifier input. The already-authorized ASSIGNMENTS query runs for all usable parent keys, including invalid-header parents and non-member assignments, using the unchanged natural UUID binding and no new filters. Both completed projections determine the exact attempted units before immutable rejections and total scope abort. There is no additional query, guessed K, dropped invalid field, null binder key, reconstructed key, retry, or output/callback leak.

Required negative fixture contract: same physical valid parent/member UUID and null required createdAtTechnical; with0 assignments, `max(1,0)=1` rejected gap; with3 distinct activity triple-PKs for that member, `max(1,3)=3` rejected assignment units. This is projected-contract/mapper evidence, not a fabricated claim that native PG violates a NOT NULL constraint or an authorization to write malformed DB state. Nonmembers contribute |O| with no member filter.

Partial usable keys are explicit: keyless rows retain one rejection per stable ordinal without invented identity/K; other usable parents still enumerate assignments. Nonempty MEMBERS with no usable parent key skips query2 and ABORTS; it is never conflated with actual zero-row MEMBERS, which alone can successfully return empty after final guards/completion.

D12.3 duplicate accounting remains one rejected attempted atom per duplicated physical PK/source identity with its full observedPhysicalRowCount. The private ledger groups evidence for countability; it does not silently deduplicate accepted output. Physical rows and logical attempted units remain separate, and multiple reasons do not multiply the same attempted atom. Header/member duplicates likewise force total abort.

Hard JPA/JDBC/source access, SQL policy, bind, resource, TX, probe and completion failures STOP immediately through their own operational channel. They do not continue an aborted transaction to discover K, emit pending errors as a completed rejection list, or claim invented total counts. For completed projected-error enumeration, read=valid+rejected exactly; for early physical abort, only observed private evidence is retained without a false complete K. For every abort, published=evaluations=results=0. These distinctions resolve the original contradiction without redesigning source semantics.

## 6. GAP B and original P1 R2-DESIGN-B-01 — closed SQL and native RR

Assessment: COMPLETE at design level. Historical B01 severity: P1; current disposition: CLOSED BY THIS INDEPENDENT DESIGN RE-AUDIT. Basis: I§§4–7/10, H§5, D§§12.2/18–20/26.4–26.5. No SQL was executed.

### 6.1 Independent SQL byte/hash recomputation

I extracted all six actual §4 SQL blocks, replaced only exact named parameters outside `::` casts for documentary rendering, collapsed marker-only list to `(?*)`, and independently hashed `UTF8("F2E_SQL_CATALOG_ID_V1\n") || ASCII(byteLength(C)) || ":" || UTF8(C)`. These calculations do not execute SQL or a test. The DATA strings match the separately printed canonical DATA strings; probes are exact literal strings.

| Logical catalog ID | Independently measured canonical UTF-8 bytes | Independently measured statement ID |
| --- | ---: | --- |
| R2_LEGACY_MEMBERS_V1 | 579 | 9852b6e9487a71cb47d76e834e82eceafcfbdd194b6e66d718f7da1ccdf3a769 |
| R2_LEGACY_ASSIGNMENTS_V1 | 290 | 6b21c28ee8961f783e986704604791181aa175592abc0b73553fa321445ce219 |
| R2_TX_ISOLATION_V1 | 47 | 4a669a2f628e12468e0d532889e0bcafd38c09a5aadfb27f2f20f56159c1671e |
| R2_TX_READ_ONLY_V1 | 47 | 9963ea856cdf9bfb3e9c440b1c3a6c062fd375a906f465cbe7a7ac88878716c7 |
| R2_TX_SNAPSHOT_V1 | 34 | 24f02692f50e880b77a78f9ea64501ce276b7b0e2a93c880a4c23e03bbe19aa0 |
| R2_TX_RESOURCE_IDENTITY_V1 | 75 | 0ed00ba3ec87635658759a60f48bc9ea57df82a338b863f23be641fdf6b7b3ad |

All six match the current documented table and corrected coordinator catalog. CLOSED_SET means exactly TWO DATA + I/R/RESOURCE/S shapes; it is not a lifecycle CLOSED declaration. Header/member LEFT JOIN and all assignments by derived parents have exact aliases/11+5 ordinals/classes/predicates and PK SQL ordering, no Cartesian join/entity navigation or caller member filter. Resource is exactly `SELECT current_database() AS database_name, current_schema() AS schema_name`, one two-String non-null tuple, zero binds, no new source table.

Parameter plan names/classes/positions are complete: UUID lists natural Java order via typed Hibernate list binding, Boolean/String/Short/LocalDate scalar types, header s+6 slots and assignment t slots; no nullable filters, text UUID, arrays/ANY/temp tables/custom list expansion. A zero-row header result skips query2 without IN(); invalid correlatable payload uses only validated derived keys per §5 above. Projection codec requirements are explicit and PG-native conversion evidence belongs to future acceptance.

SQL normalization/hash are explicitly adopted neutral D36.4–36.5 capabilities. Equal I/R text/hash does not import R1 RC/claim/count/catalog. Unknown SELECT/normalization/class/denylist/catalog failure stops before JDBC; built-ins are permitted only in these four exact shapes, not an open function/schema/driver bypass. Inspector policy, typed executor plan, and real JDBC setter/prepare/execute/ResultSet/resource evidence are three distinct correlated records. An inspected SQL is not proof of bound values or execution. Each probe, including RESOURCE, needs its zero-bind plan, inspector ID and actual bound-native execution evidence.

### 6.2 B01 primary driver source and corrected mechanism

I independently opened the primary static source archive `/Users/jesusaldaircruzortiz/.m2/repository/org/postgresql/postgresql/42.7.11/postgresql-42.7.11-sources.jar` without loading/running its classes. Archive SHA: `5156b9a1076e69ede16266ceb0c20a7ecd0f3f7d5a5a388480333ec8339ee198`.

`org/postgresql/jdbc/PgConnection.java:1717–1726` shows getSchema always creates a Statement and executes `select current_schema()`; lines1130–1141 show getCatalog can execute `select current_catalog` when cached catalog is null. `PgDatabaseMetaData.java:121–128` delegates getURL/getUserName; `PgConnection.java:690–701` returns creatingURL/queryExecutor's saved user locally. Thus the original all-local metadata assertion was wrong, and the diagnostic correctly characterizes both hidden-SQL cases, not just one.

Current exact correction: I§6.2 lines630–672 forbids Connection/PgConnection.getSchema/getCatalog inside the measured owner/reader/probe invocation regardless of cache, also forbids implicit-SQL metadata traversal/ParameterMetaData. Session.doReturningWork is limited to local graph/native reference/unchanged genuine native URL/user inspection, with no SQL prepared/executed there. Actual database/schema are freshly observed by the explicit RESOURCE query through the same qualified shared EntityManager/Session/bound original PgConnection and actual inspector/JDBC capture, initial and final. Descriptor/bootstrap expected values only compare; they never replace actual query output or constitute cached observed facts. Null/cardinality/type/database/schema mismatch aborts as a resource error.

This removes hidden uncaptured guard SQL from the proposed measured invocation; it does not merely relabel getSchema/getCatalog as zero-SQL or excuse them outside capture. Initial/final local configured and original native URL/principal guards remain independent and unchanged; wrapper forwards genuine values without sanitizing or synthesizing a matching native value. Bootstrap Flyway/ddl-validation/schema fingerprint/metadata is explicitly outside the acceptance window and is not falsely declared globally zero-SQL. Owner JDBC begin/isolation/read-only/commit/rollback lifecycle is separately observed/corroborated, not mislabeled as inspected DATA/probe execution. Driver metadata SQL has no such exception. No misrepresentation of driver sources persists.

### 6.3 Implementable owner, resource association and capture

I§6.1 seals distinct actual proxied TEST-ONLY owner REQUIRES_NEW/REPEATABLE_READ/readOnly and reader MANDATORY/readOnly with explicit `f2eR2ReaderTransactionManager`. Unique qualified reader DS/EMF/PU/TM/sharedEM/inspector references are checked at startup and invocation. Wrong/missing advisor/manager/resources, outside TX or RC fail before DATA, without fallback. Reader owns no new TX, isolation upgrade, retry or internal REQUIRES_NEW. Existing R1 runtime owner is explicitly NOT_REUSABLE.

The graph includes separate privileged setup/observer DS; only the SELECT-only reader DS backs reader EMF and TM. The actual bound EMHolder/Session is joined, Spring RR/readOnly, Session defaultReadOnly/MANUAL. Connection and original unwrapped native PgConnection refs come only from that Session; no independent reader/probe connection, second EM, DriverManager, JdbcTemplate or caller-supplied resource. Before and after EVERY statement, including all probes, both original refs and graph association must remain exact. Final local metadata guard and RESOURCE probe do not substitute wrapper equality for native association.

Capture starts before the first probe and stays associated with the invocation through actual transaction completion; no nesting/cross-thread capture. Context is created only after validated I/R/RESOURCE/S initial observations, with snapshot evidence derived from this R2 owner/fixture/run/attempt and exact observed snapshot text. The initial records are retrospectively associated with that ID; no R1 pre-callback fingerprint commitment is asserted. Output remains provisional until all mapping/recompute/manifest/resource/snapshot guards and actual successful completion; synchronization/finally cleanup terminalizes registry and closes capture, never deleting consumed markers or leaking partial output on rollback/unknown/beforeCommit/completion failure.

Successful sequence is I → R → RESOURCE → S → MEMBERS → [ASSIGNMENTS if usable parents] → I → R → RESOURCE → S → final local guard → real completion. Both I must be repeatable read, both R on, both actual RESOURCE tuples match trusted bootstrap and each other, and initial/final pg_current_snapshot exact texts must match. With actual empty MEMBERS, success still requires all final probes/guards/completion; this is nine SQL occurrences rather than ten for the ordinary two-DATA path, not a future suite/test-count commitment.

I§6.3's full failure table is coherent: invalid caller/context/scope/registry or local initial graph/URL/user/ref failure has zero invocation SQL; I/R failure stops at its actual stage; initial RESOURCE mismatch honestly records prior I/R/RESOURCE and zero DATA/no S; S failure stops before context/DATA; hard MEMBERS/ASSIGNMENTS policy/bind/access/resource/TX failure stops immediately. Only safe completed projected-error collection continues to the existing assignment query for exact K. Keyless nonempty and true empty paths are distinct. Completed invalid projections abort pre-core before final probes. Final probe/local/manifest/recompute/completion failure preserves its actual prefix and discards output. Records distinguish inspect/prepare/execute entered/completed/failed; policy rejection claims zero JDBC only for the rejected statement. No failed TX is required to run trailing probes or expose complete rejected counts.

### 6.4 Database fence, checksum and concurrency

I§7 instantiates D20 without Reserva analogy. Real SELECT-only login has only the three Turno source tables, necessary DB/schema access and effective built-in access for the four exact probes, no privileged inheritance/ownership/superuser/bypassRLS/DML/DDL/temp/sequence/application-function/additional source grants. Actual PUBLIC/membership privileges are checked, not ignored. Separate pre-window denied INSERT/UPDATE/DELETE/DDL controls require42501; any successful write is a failure even if rolled back. Privileged migration/fixture/grants/cleanup writes are acknowledged outside the window.

Checksum covers every persisted live column in installed physical schemaordinal, exact current parent10/member2/assignment5 columns and null codecs, with compound PK tuple ordering unsigned16-byte UUID components. Unknown/new column or schema drift fails, never silently omits data. Parents are frozen from the exact date/salon/active/type predicate; before/after parent scope-set agreement detects entering/leaving relevant parents. Children are selected afresh by frozen parent IDs in EACH pass, detecting inserted/deleted children rather than freezing old child PKs. Empty parent scope still hashes exactly three empty tables with counts. R2 domains/LP/SEQ/tags/null framing/order are fully instantiated and separate from Reserva's selectors/simple-PK helper and historical goldenbytes.

Quiescent all-table before → RR reader/real completion → all-table after equality is separate from controlled concurrency. The latter places a real admin writer commit between MEMBERS and ASSIGNMENTS, verifies RR original state, then a new attempt observes committed state. RC raw/reference negative control is a separate test path without valid R2 context/reader/claim, never an accepted RC fallback; R2 reader-in-RC separately rejects before DATA. Observer SQL/writes are not attributed to reader or smuggled through its EM/capture. No R6 cross-source snapshot claim or material-source audit follows.

## 7. GAP C — preservation guard and seven capabilities

Assessment: COMPLETE at design level. Basis: I§§8–9 (unchanged preimages), H§§3/5, D§§22–24.1/26.6. The prospective authority conflict is expressly resolved as a NEW bounded R2 design candidate exception, not inferred D24.1 permission.

I read the complete actual `src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/ReservaJpaReaderArchitectureTest.java`. Its Files.walk includes all shared main read/adapter and test adapter roots; allowlist asserts exact11main/10test, not only a Reserva prefix. Its other guards enforce plain reader/mapper/executor, forbidden writer/escape/config tokens across full roots, R1 enum/claim singleton1 and both R1 methods' explicit f2eReaderTransactionManager/MANDATORY/readOnly. Runtime-isolation guard separately checks default/prod absence and existing productive legacy service. No test was run or claimed to have failed prospectively.

Only ONE future existing non-testinfra exception is chosen: that exact ArchitectureTest path, solely enumeration/preservation composition. R1 literal11/10 sets and every other guard remain intact; future R2 main/test sets must be separately SEALED exact filenames, disjoint, and physical root enumeration must equal their global union with no extra/missing/overlap. No startsWith/prefix hiding, skipped roots, wildcard/count-only/unknown guessed sets are allowed. Shared testinfra remains in R1's ownership set and extension requires its own preservation proof. Other existing R1 tests are not implicitly writable. Exact future implementation allowlist and independent handoff approval/activation are still required; no source is edited now.

The exact D24.1 seven capabilities were independently contrasted with whole physical sources:

| Capability | Classification | Physical support and bounded interpretation |
| --- | --- | --- |
| PostgreSQL container | REUSE_AS_IS | F2ePostgresTestConfiguration.java; isolated postgres16-alpine factory/lifecycle, not importing all Reserva config/fixtures/beans |
| Flyway boot | REUSE_AS_IS | Same source; isolated migrate/validate/schema-FP algorithm; r1-a/Reserva fixtures and historical applied50 remain R1 constraints, not universal R2 acceptance values |
| SELECT-only setup | EXTEND_WITH_R2_NEUTRAL_CAPABILITY | F2eSelectOnlyRole.java; current R1 principal/reserva grant/denied control and wrapper behavior preserved; distinct exact R2 grants/login/no widening |
| SQL inspection | EXTEND_WITH_R2_NEUTRAL_CAPABILITY | F2eStatementPolicyInspector.java; neutral normalize/hash/capture seam, separate R2 instance/catalog; R1 four statements and denied snapshot/manifest unchanged |
| Checksum canonicalizer | EXTEND_WITH_R2_NEUTRAL_CAPABILITY | F2eSliceChecksum.java; R2 compound-PK/all-column/three-table seam; R1 selectors/simple-PK/domains/order/framing/vectors unchanged |
| Architecture rule | EXTEND_WITH_R2_NEUTRAL_CAPABILITY | Exact non-testinfra ArchitectureTest exception above, all R1 invariants preserved plus sealed R2/global-union checks |
| Transaction test owner | NOT_REUSABLE | ReaderTransactionTestHarness.java; actual ReservationReadPort/Reservation snapshots/RC/registry/formulas/manifest hardcodes require distinct R2 runtime owner; neutral owner concept only |

All five sources live under `src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/testinfra/`. Reading their complete physical code shows why capability reuse does not authorize importing/editing a whole source. R1's harness even calls getCatalog/getSchema before its R1 capture; that supporting historical code is expressly not the R2 owner template or a license to carry hidden SQL into R2. Future neutral seams must be precisely selected by separately audited handoff and preserve original R1 semantics and competent regressions. No production R1/core/contracts/migration/pom/product config extension is authorized by this table.

## 8. All twelve mandatory answers

| # | Question | Independent final answer and basis |
| --- | --- | --- |
| 1 | Faithful R2 definition? | YES. I§2/D§§8/12: standalone legacy Turno reader over exact three tables/date+salons; active recurrent Sunday0 and punctual exact-date, two scalar DATA projections/all assignments inclnonmembers, sources-only/no classifier or target selection. |
| 2 | GAP A instantiated, not deferred? | YES. I§§2–3 seals own trust/context/descriptor/registry, exact26+20 maps, URNs/physical IDs, scalar/absence/bytes/five D13 formulas/immutable envelope; corrected §2 rejection enumeration completes its count obligation. See §§4–5 here. |
| 3 | GAP B instantiated without R1 analogy? | YES. I§§4–7 seals six exact shapes, typed natural UUID binding, separate actual R2 RR owner/resource/snapshot/capture/fence/checksum/concurrency. R1 RC/enums/formulas/counts/catalog/metadata proof are not transferred. See §6 here. |
| 4 | GAP C resolved without weakening R1 guards? | YES. I§8 selects one explicit bounded future exception outside testinfra, literal R1sets/all guards preserved and separate sealed R2/global union; no source modification/current permission. See §7 here. |
| 5 | RR owner/resource implementable and auditable? | YES, AS DESIGN. I§6 qualified/proxied ownerREQUIRES_NEW/RR/readOnly versus readerMANDATORY, actual bound Session/original PgConnection every statement, local native metadata plus explicit real RESOURCE, exact snapshot begin/end and real completion/failure cleanup. Runtime proof remains future acceptance. |
| 6 | Closed deterministic SQL/capture sufficient for handoff? | YES. All6 lengths/IDs independently match; exact DATA/probe aliases/types/slots/capture association, actual execution distinct from inspection, success/empty/abort prefixes and lifecycle/bootstrap distinctions leave no SQL guard loophole. B01 closed by §6.2 here. |
| 7 | Deterministic identity/provenance/canonical representation sufficient? | YES. Exact26 observable +20 disjoint metadata keys, LP/SEQ/scalars/NULL/ABSENT/sort, physical/gap URNs, D13 acyclic formulas, seven-field provenance/final recompute and legal history/intent policy fully instantiated. No source fingerprint recursion or caller resource assertion. |
| 8 | Shared testinfra safe for R1? | YES, WITH THE EXPLICIT FUTURE HANDOFF PRESERVATION CONDITIONS. I§§8–9 has exactly7 capability classifications, isolated container/Flyway reuse, bounded neutral seams/R1 preserved and runtime owner NOT_REUSABLE. Whole Reserva config is not generic. No shared edits now. |
| 9 | Productive boundaries preserved? | YES. I§§10–12/H§5/ESTADO/architecture maintain TurnoInstructor LEGACY_VIVO/PRODUCTIVO, R1 closed/nonproductive/dark launch, R2 designed dark/nonproductive/default-prod bean and caller absence as future obligations, no productive switch; cutover NOT_AUTHORIZED. |
| 10 | Successors excluded? | YES. R3 nominal candidates, R4 exact-date NEW_* adjustments, R5 effective graph/backing, R6 cross-source RR/shadow/composition excluded. No targets/crosswalk/resolver/fence/report sink/material audit/migration/productive shadow; Payments/Notifications out, old mechanisms OLD_PROCESS_ONLY. |
| 11 | Proposed-only clearly non-normative? | YES. I§§3.4/10–12/H§5: new vectors/ordered fingerprint or pre-callback commitments/exact Maven commands/future counts/R1 59/649/host7/native4/TECH first-later do not become current R2 criteria; candidate decisions are identified as pending competent gates, historical code/reports not productive authority. Literal current capture order is a specific audited design decision, not a deferred fingerprint commitment. |
| 12 | Authority sufficient to author a mechanical R2 implementation handoff subject to publication/activation prerequisites? | YES, DESIGN READINESS ONLY after competent design gate and separate authoring authorization. I§11/H§4 explicitly permits evaluation of AUTHORING readiness separately; chosen profile requires separate R2_DESIGN_AUTHORITY_PUBLICATION and competent closure before future implementation handoff ACTIVE, then exact handoff audit/approval/activation and separate implementation authorization. Nothing is authored/activated/published now. |

These twelve answers revisit the whole design, not only the correction delta. The new bounded instances faithfully refine the authorized A/B/C materialization. No other unsupported redesign or expanded authority was found.

## 9. Publication profile and minimum next lifecycle

Current unit is `R2_DESIGN_RESEARCH_LOCAL_MATERIALIZATION`, with original independent FAIL → bounded CORRECT → this fresh independent RE-AUDIT → coordinator's competent design/documentation gate still pending at this report cut. Scope/safety/documentary checks apply; tests/Maven/JDBC/container/SQL/host implementation delivery do not apply to this read-only documentary re-audit and were not executed.

The selected current R2 profile's next possible unit is separately scoped `R2_DESIGN_AUTHORITY_PUBLICATION`, after acceptance/gate of these design bytes and its own competent preflight/audit/publication gates. This is a concrete R2 design choice already in the preimage, not a universal protocol requirement. The protocol allows profile-specific composition. Publication is not currently performed/authorized by this worker and is not productive activation/cutover.

AUTHORING readiness may be evaluated under separate documentary authorization after competent design resolution; AUTHORING ≠ authority PUBLISHED ≠ handoff APPROVED/ACTIVE. This chosen R2 profile requires published/versioned/sealed design authority before a future implementation handoff becomes ACTIVE. That future handoff must seal exact implementation/shared-edit paths and applicable commands/acceptance evidence, undergo independent audit/approval, then explicit activation and separate implementation authorization. This research handoff is NOT_ACTIVE_IMPLEMENTATION_HANDOFF and never becomes implementation authority through publication alone.

No current next functional phase, implementation handoff, new productive authority or future gate/Task/Dispatch ID is invented. R1 recovery/reopening is not needed or authorized. Cutover=false/NOT_AUTHORIZED; material audit/D08/crosswalk/resolver/fence/migration and R3–R6 remain excluded.

## 10. Independent physical before/after scope proof

I independently hashed all actual460 tracked paths and660 ignored paths against complete supplied maps, not just the21 R1 paths; ignored contents/secrets were not exposed. I independently recomputed exact R1 path/content streams, actual index bytes/ref text, branch/HEAD/upstream/porcelain/ahead-behind and both diff checks at start and final documentary checkpoint. Every field in the before/after snapshot is identical.

| Measurement | Physical before and after |
| --- | --- |
| Branch | operacion/excepciones-horario-fecha |
| HEAD / upstream / fresh live origin | 6c2eacc870499e74ead74c1851630f9f53b1c676, all equal |
| Ahead/behind | 0/0 |
| Staging / tracked tree | EMPTY / CLEAN; no tracked delta or path-set change |
| Untracked set | Exactly I and H, the two already-materialized corrected design/research candidates; unchanged by auditor |
| Tracked count / complete inventory SHA | 460 / d7964ab357d282a21b3289f0511f6289a3e32f8dd0a13a615f8a4dd93384d5cd |
| Ignored count / complete inventory SHA | 660 / 9caf3c3d4dfea14a9244e2b62ed123807adfd0e61500d8e0c43211dab1df6ec0 |
| Tracked/ignored changed paths vs baseline | 0 / 0, including no extra/missing paths |
| Actual index byte SHA | b053b254ae09434071a7dcd9922c2d2f3dbc6edde0ddaa2935c763432527e682 |
| Actual refs SHA, stripped show-ref text | e75a76fb06f4b02f953961c777ee0d8c776b1f6cbcb980b69dd98656ff097e8b |
| R1 exact21 (main11/test10) path-set SHA | f400a0602f95e318845da670bee4f819f057842adf8a60506564d5bd75e41d14 |
| R1 exact21 physical content-manifest SHA | e2b64abd6aba8a050df6184f6c5440d87db83a67182fb43e622f48cf96f4f3ce |
| git diff --check / git diff --cached --check | Both exit0 with empty output, before/after |
| I/H explicit untracked hygiene | Each trailing whitespace0, CR0, finalLF=true; exact current byte hashes in §2 |
| Primary design / ESTADO | Exact unchanged source hashes in §2; no future review/state metadata in audit cut |

Inventory framing: SHA-256 of compact sorted JSON path→physical SHA for each inventory. R1 path stream: sorted paths joined by LF with final LF. R1 content stream: `SHA256(physical file) + two spaces + path + LF` per sorted path. Refs digest uses `git show-ref` stripped surrounding whitespace; this framing is explicit, not confused with the historical raw-terminal-LF digest.

Fresh final read-only remote command `GIT_OPTIONAL_LOCKS=0 git ls-remote --exit-code --heads origin refs/heads/operacion/excepciones-horario-fecha` exited0 and returned exactly `6c2eacc870499e74ead74c1851630f9f53b1c676` for that ref. No fetch or local-ref update occurred.

Measured auditor repository delta0; implementationDelta0; index/ref/config mutation0; Java/test/migration/SQL/pom/core/product configuration changes0; tests/build/Maven/JDBC/SQL/container execution0; old mechanisms0; checkout/fetch/stage/commit/push/activation0; child agents0. The only authored artifact is this uniquely named external report through apply_patch. Candidate creations/corrections and preexisting R1 implementation are baseline evidence, never attributed to this auditor. Read-only source parsing/hash/diff and Orca gate/inbox/heartbeat coordination are not technical test execution.

## 11. Residuals and result binding

Unresolved integration findings: NONE. Original P1 B01 and AB02 are independently CLOSED at DESIGN level as detailed in §§5–6. GAP A/B/C are COMPLETE at DESIGN level. No new P0/P1/P2 was found. Future runtime acceptance, exact implementation allowlist, competent design gate/publication and handoff activation remain obligations of separate units, not defects or falsely completed current work. R1's existing P2-EVIDENCE-01 remains NON_BLOCKING/PRESERVED history and is not promoted to an R2 criterion.

The closing result JSON delivered with this report identifies this exact report's physical post-write SHA-256, current design/research/input hashes, A/B/C dispositions, both residual closures, publication classification and implementationDelta0. A self-hash is not embedded recursively into its own preimage; the actual computed report hash is carried in the result/lifecycle delivery. The coordinator may persist these exact report bytes under its declared review path after worker settlement; doing so or updating postgate ESTADO is not an action or a future-byte audit by this worker.

Terminal verdict: **PASS — F2E R2 DESIGN AUTHORITY COMPLETE FOR HANDOFF**

<!-- END_ORIGINAL_FRESH_REAUDIT -->

## 5. Receipt cronológico — publicación probada y reconciliación de cierre

2026-09-17 America/Mexico_City, cierre Run `run_9e2fc3f4c74f`.
Las secciones1–4 y los dos originales anteriores conservan íntegramente su corte
histórico antes de publicación: initial FAIL → corrección → fresh PASS de diseño.
Sus NOT_PUBLISHED/PENDING y siguiente publicación son historia correcta, no una
contradicción actual. Este append es reconciliación coordinadora, no audit propio
ni atribución de bytes futuros a auditores previos. ESTADO-ACTUAL conserva la
autoridad operacional; este review conserva trazabilidad y sellos.

### 5.1 Identidad de publicación competente

| Binding publicado | Valor |
| --- | --- |
| Branch | operacion/excepciones-horario-fecha |
| Publication Run | run_ea24bf9a335a |
| Commit | 061dda98319722bcc2c601e707c25d7433ac44c1 |
| Único parent | 6c2eacc870499e74ead74c1851630f9f53b1c676 |
| Exact diff | Cuatro documentos: 3 NEW / 1 MODIFIED; cero implementación |
| Canonical manifest SHA-256 | f367f8d657b2f26237a98f22baea8f1dde54841685c4134f7b05683038d48fe2 |
| ESTADO preclosure histórico | aad8f9436dfc14f9ce45c645aa002e7692f91766483f48cbd82f8d93ea8d0d26 |
| R2 design inmutable publicado | db4673dd0705c41e62d0b77339d45b2cd1e87c51e26064112ad95b843dafe9cf |
| DESIGN/RESEARCH inmutable publicado | 221347b46c5032b384a306c61908c8fd0f2c26076455609fb880098be8f0d2d4 |
| Review preclosure histórico | 0863b2a704f56b9d55a141edb3d191629c25d35f8706a66b7c878e176564b785 |
| R1 exact21 path-set inmutable | f400a0602f95e318845da670bee4f819f057842adf8a60506564d5bd75e41d14 |
| R1 exact21 content inmutable | e2b64abd6aba8a050df6184f6c5440d87db83a67182fb43e622f48cf96f4f3ce |

Manifest UTF-8 TSV con header `path\tstatus\tsha256\tclassification`, paths
lexicográficos, LF/final LF; reconstruido desde diff real y blobs del commit,
no desde una copia del chat. Cada blob preclosure igual al working tree inicial.
Entrada de cierre HEAD=upstream=live origin=061dda9 exacto, ahead/behind0/0,
CLEAN/index EMPTY/untracked0, ambos diff checks exit0. Fresh non-mutating query:
`GIT_OPTIONAL_LOCKS=0 git ls-remote --exit-code --heads origin refs/heads/operacion/excepciones-horario-fecha`.
Completion gate real confirmado read-only con gate-list sobre publication Run.

Readiness `run_b04f6e0ac339 / gate_4954c971c1e7 PASS` precedió staging.
Auditor staged separado `task_152bef6a1caf / ctx_0c0a4fe8d250`, Done
`msg_1bb7deaafe57` a2026-09-18T04:13:56Z:
`PASS — F2E R2 DESIGN AUTHORITY STAGED SNAPSHOT VERIFIED`.
Original report SHA-256
`2f411211f0e60607ff21adbb9cc0adb86869620767d5a78c29929e76631ec7a1`.
Authorization `task_cb3bafa1910d / gate_40f8471dfebd`, PASS2026-09-18T04:14:28Z:
`PASS — AUTHORIZED_TO_COMMIT_AND_PUSH_EXACT_F2E_R2_DESIGN_AUTHORITY`.

Un único commit subject `docs(auditoria): publica autoridad de diseño F2E R2`,
un único push normal non-force fast-forward del SHA exacto a la branch exacta;
sin tags, refs adicionales, integración Payments ni contenido mutado.
Comando/receipt originales:

```text
git push --no-follow-tags --recurse-submodules=no origin 061dda98319722bcc2c601e707c25d7433ac44c1:refs/heads/operacion/excepciones-horario-fecha
To https://github.com/MacacosDevs/feellingPilates.git
   6c2eacc..061dda9  061dda98319722bcc2c601e707c25d7433ac44c1 -> operacion/excepciones-horario-fecha
exit0
```

Post-publication auditor fresh distinto `task_de971a00b41d / ctx_4685c46279e2`,
Done `msg_63062ec40ded` a2026-09-18T04:21:38Z:
`PASS — F2E R2 DESIGN AUTHORITY PUBLISHED EXACTLY`, quince respuestas verificadas,
P0/P1/P2=0/0/0. Original report SHA-256
`eb2d00966c9ed6ca948ed955c16149b13b8765ceb6f0f9900fc5d8bfc17e372b`.
Ambos workers terminales settled/released, transcripts captured, Done ACK tras
release. Completion `task_d08dce894db1 / gate_e140218d660b`, PASS2026-09-18T04:22:15Z:
`PASS — F2E R2 DESIGN AUTHORITY PUBLISHED / READY_FOR_DESIGN_PUBLICATION_CLOSURE`.

Custodia local original: `/tmp/feelingpilates-f2e-r2-design-publication-execution.QE5yLZ/`,
reportes `AUDIT-STAGED.md` y `AUDIT-POST-PUBLICATION.md`; evidencia
`POST-PUSH-EVIDENCE.json` SHA-256
`b5238b7b6daa1ce136e5eec6e9de390601484f89366f31544945b82f05373fd5`.
No garantía de retención de /tmp; identidades/diff/blobs siguen verificables por Git
y los IDs de audit/gates permiten rastrear evidencia operacional archivada.
Preimages de este cierre se preservan externamente bajo
`/tmp/feelingpilates-f2e-r2-design-closure.rKBHZH/`; este review anterior completo
es prefijo byte-exacto. Ni el FAIL ni los dos P1 originales fueron alterados.

### 5.2 Estado reconciliado y próximo proceso separado

R2 autoridad de diseño `COMPLETE / AUDITED / PUBLISHED`; design-publication
lifecycle `CLOSED — AUTHORITY_RECONCILIATION_MATERIALIZED` por diseño y publicación
competentes ya probados, no por un SHA futuro de closure. Los nuevos bytes de cierre
requieren todavía precommit audit independiente, authorization gate, publicación
de docs, post-closure audit distinto y final gate: `PENDING / NOT_EXECUTED` al
corte de esta entrega. No se inventan futuros IDs ni resultados.
Allowlist actual exclusivamente ESTADO-ACTUAL y este review; ningún nuevo path.
Diseño normativo/research handoff no cambian para registrar su propia publicación.
El research es published provenance DESIGN/RESEARCH, NO implementation authority.

Diseño §11 y research §4 + WORKFLOW/STATE-MACHINE/GATES/ROLES soportan futura
`R2_IMPLEMENTATION_HANDOFF_MATERIALIZATION` sólo con autorización documental propia;
publicación y closure competentes preceden handoff ACTIVE específico del profile.
Ese futuro handoff requiere audit/approval/activation propios, y la implementación
una autorización separada; no se materializan ni autorizan por este cierre.

Selección de siguiente lifecycle del usuario, condicionada al gate final de cierre:
`F2E_OPTIMIZED_EXECUTION_BOOTSTRAP_R1`, PROCESS_ONLY. No es nuevo prerrequisito
normativo de producto ni autorización R2; se elige su futura apertura, no se
implementa/diseña/publica/activa el proceso aquí. Necesita scope/profile/audit y
publicación/activación competentes propios. Checkpoint clean después de cerrar es
apto para iniciar bootstrap antes del handoff. Secuencia seleccionada: closure →
bootstrap process-only → process publication/activation competente → R2 handoff
bajo autorización propia → implementación sólo con autorización propia.

R1 `CLOSED / ACCEPTED / PUBLISHED`; GAP1–5 y TECH01–06/08 CLOSED, TECH07
`CLOSED FOR_CURRENT_R1_HOST_INVARIANT ONLY`, `P2-EVIDENCE-01 NON_BLOCKING / PRESERVED`.
R2 implementation handoff `NOT_MATERIALIZED / NOT_ACTIVE`; código `NOT_AUTHORIZED`.
TurnoInstructor `LEGACY_VIVO / PRODUCTIVO`; dark launch `PRESERVED`;
R2 runtime diseñado `DARK_LAUNCH / NON_PRODUCTIVE`, sin activación/default-product
beans/callers autorizados. Cutover `NOT_AUTHORIZED`; R3–R6 `NOT_AUTHORIZED_IN_R2`;
Payments/Notifications OUT_OF_SCOPE; procesos antiguos OLD_PROCESS_ONLY.
Tests/build/Maven/JDBC/SQL/containers no ejecutados por este cierre documental.

Payments Slice2 CLOSED reportado en su lane aislada, branch pagos/pagos-notificaciones-r1,
HEAD conocido8a912217adf6ea2d7d56e3e818845c3338db250e, Run run_6859a7f36296,
es coordinación, no re-audit Payments: `SHARED_BUT_COMPATIBLE + INTEGRATION_POINTS_IDENTIFIED`.
V48/V49 y baselineV49/52 pertenecen a esa lane. Cero integración/reconciliación Flyway
en este cierre y ninguna transferencia ownership financiero/Reserva/capacity.
Futuro handoff/implementación F2E revalidará head Flyway integrado vigente, no asumirá
V47/50 globalmente terminal; escrituras compartidas Reserva/Programación/cupos/
capacity requieren `CROSS_LANE_DEPENDENCY_REQUIRED` antes de escribir.
