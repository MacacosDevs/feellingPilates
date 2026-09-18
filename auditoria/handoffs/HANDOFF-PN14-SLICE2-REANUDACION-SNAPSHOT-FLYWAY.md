# FeelingPilates — PN14 Slice2 — reanudación exacta del candidato / reconciliación Flyway

Status de este corte: `CORRECTIVE_AUTHORIZATION_CANDIDATE / PENDING_FRESH_AUTHORIZATION_AUDIT / NOT_ACTIVE`.
`LOCAL_CANDIDATE_PRESERVED / TECHNICAL_IMPLEMENTATION_NOT_RESUMED`.
WORKFLOW_PROFILE: DOCUMENTATION_ONLY_SCOPE_RECONCILIATION; no corrección técnica en este Run.
Date2026-09-16; run_bc3f744161b5 / task_f874df8d2b86 / ctx_f5256d16cee2.
Rol PAYMENTS_SLICE2_SCOPE_AUTHORIZATION_CORRECTION_DOCUMENTER / SINGLE_WRITER / EVIDENCE_BOUND;
no auditor, corrector ni aprobación propia. P0/P1/P2 propios: NOT_ASSESSED.

## 1. Autoridad y propósito exclusivo

Leer AGENTS: README/ESTADO, handoff Slice2 original completo §§1–9 y este addendum;
Dominio §§13.1–13.7, DA014/021/022, Arquitectura Payments y mapa; checkpoint Slice2 original
§§5–6,8, sus review/manifest, checkpoints históricos pertinentes; README/WORKFLOW/
STATE-MACHINE/GATES/ROLES de orquestación. El repositorio manda; Orca conserva evidencia
operacional recuperable. README distingue canon operativo, checkpoint, handoff y reviews.

Contrato original inmutable:
`auditoria/handoffs/HANDOFF-PN14-SLICE2-ORDEN-SNAPSHOT-INMUTABLE.md`,
SHA256 `4d7e7803557f75a3d62afe67a757687a63b9c627b0fdbf2580d89feab36fdd7d`.
Todo su dominio, arquitectura, blueprint, provenance, T01–T18 y límites permanece obligatorio.
Este addendum propone exclusivamente autorizar a un futuro corrector DOS literales en UN test
existente para reanudar el exacto candidato detenido; no permite editar producción ni generar
otra vez los32 CREATE. Su eficacia depende de §7; hoy no autoriza ninguna escritura técnica.

PN13 CLOSED/PUBLISHED/TERMINAL y Slice1 CLOSED/PUBLISHED/TERMINAL permanecen. NEW-PN13-017
OPEN/P2/EDITORIAL/NON_BLOCKING/IMPLEMENTATION_INDEPENDENT; ningún otro finding cerrado se reabre.
No cambia dominio, autoridad productiva, coexistencia o cutover: MAPA no requiere edición.

## 2. Evidencia AJENA realmente recuperada y límite del diagnóstico

Recon fresh independiente READ_ONLY: task_278e350687f3 / ctx_a60b73f296ac;
único worker_done msg_79c9a1ead968, succeeded/settled/accepted/released. Resultado autoritativo:
**BODY JSON msg_6c82d77b9408**, supersede msg_a1eb81d8b6e5; payload lifecycle no es el resultado.
RECON_RESULT=PASS / SOLE_OBSERVED_BLOCKER_CONFIRMED=true / filesModified=[] / no builds/tests.
Este PASS es reconnaissance de scope y causa observada, no audit técnico ni promesa fullPASS.

Entrada coordinador task_80829775fcb9/run_bc3f744161b5, result JSON contiene all502FileSHA256
y all32CandidateSHA256 físicos. Comparados con recon y task_5021af20f143/run_e786453bf13f.
Éste verificó el candidato técnico detenido, no lo aprobó. Executor anterior task_c52eef681f84 /
ctx_44bb26db1747 terminó **failed**, uniqueDone msg_d58b8a0dd91f, settled/released;
BODY msg_f11194b6b025 declara STOP / SCOPE_EXPANSION_REQUIRED. No se convierte failed en succeeded.

Evidencia exclusivamente histórica:
baseline BEFORE ANY INITIAL WRITE 638 tests/75classes, failures0/errors0/skips0, requiredSkips0,
exit0; nuevo focal45/11classes PASS; viejo focal67/13classes PASS; full683/86classes,
**1failure/0errors/0skips/exit1 = FAIL**, requiredSkips0. T18 sigue FAIL.
Full log `target/pn14-slice2-final-full.log` SHA256
`9d57c485dcbe1448e0aad530d3e3829577b615d1ed0e5487124239267ba9cb59`:
15904–15910 único método con versión esperada47/observada49; 26137/26140 full683 y BUILD FAILURE;
23575 fresh52/max49; 23689 upgrade50→52/all50checksums preservados. XML
`target/surefire-reports/TEST-com.feelingpilates.programacion.ProgramacionPersistenciaTest.xml`
SHA256 `c5e5ed8728a5edc978789ee4dd6c47c05d49ac1f1a6c9d3bd6ff5eb2f82d775a`:
15tests/1failure/0errors/0skips; los otros14 PASS históricos. Primer assertion falla en47;
la de count línea48 NO fue alcanzada:52 se acredita por inventario y T14/T15/logs, no por ella.

Recon no observó otra decisión de producto/arquitectura faltante ni otro requiredpath.
Diagnóstico causal acotado, no audit exhaustivo de corrección del candidato.
Toda contradicción adicional exige STOP según §9.

## 3. Cadena global versus las14 protecciones de Programación

La clase tiene15 tests y contexto compartido @SpringBootTest/@Import(TestcontainersConfiguration.class)/
@Transactional, PostgreSQL16-alpine, Flyway enabled y Hibernate validate sin target localV47.
Sólo `flywayMigraDesdeV1HastaV47` (línea46) pinnea cadena global latest/count en47–48.
No delimita un target de migración exclusivo de Programación. El nombre se conserva por trazabilidad.

Historia local Payments recuperada por recon (commit: latest/count físicos y assertion):
cf23b2dbd3609bcc14e1e863d54e304da7fb4c59:41/44 →
eda09cd97d9f325f27555ce16743518fd76a39b7:43/46 →
a6335be22ab8c646f07ff515c417be0f6ff98ce0:46/49 →
95900d8a1d787a24aff4ee4e10f69d540ce81339:47/50.
Origen checkpoint modelo-base-programacion:74,81 separa cadena V1→V41/44 de JPA/tablas/queries.
Checkpoint dark-launch-ajustes-programacion-fecha:54–69 preserva V47/constraints.
Versiones V22_1/V22_2/V22_3 explican50 archivos hasta47. Sólo V48/V49 autorizadas producen49/52.

Los14 métodos restantes conservan CADA byte: metamodel JPA incluyendo AjusteProgramacionFecha,
tablas V41 vacías, traslape semanal/vigencia, conflicto cross-salon, vigencias disjuntas/último día,
adyacencia semiabierta, dos segmentos instructor/actividad y exclusión de vigencia futura en
ambos repositorios. Todos14 tuvieron PASS PostgreSQL histórico, pero requieren fresh15focal futuro.
V41/V47 y demás50 migraciones intactas. V48 expande sólo orden_venta/compra snapshot/
compra_componente_snapshot/informe_backfill_snapshot, sin DML/backfill ni schema Programación.
V49 sólo encoder/constraints/guards foundation; no altera tablas Programación/Turno/horario/reserva.
Legacy financiero/estado/expiración continúa writable sin reinterpretar el contrato congelado.

## 4. ÚNICA modificación técnica futura y precedencia estrecha

Path exacto: `src/test/java/com/feelingpilates/programacion/ProgramacionPersistenciaTest.java`.
SHA256 BEFORE obligatorio `0a4ca66231d7f0545568ecbef6bb09e58270b4d68c4f567ebaac22d7a9ae9ea7`.

```diff
-        assertThat(flyway.info().current().getVersion().getVersion()).isEqualTo("47");
-        assertThat(flyway.info().applied()).hasSize(50);
+        assertThat(flyway.info().current().getVersion().getVersion()).isEqualTo("49");
+        assertThat(flyway.info().applied()).hasSize(52);
```

Líneas47/48; cada otro byte idéntico: nombre de método, imports, annotations, wiring, fixtures,
helpers,14 métodos y assertions. SHA AFTER meramente hipotético calculado en memoria por recon:
`f6d81bd5cb6b57d8440ab581da967e2bc77570c988f2db5f84fdc1c1b26d4372`; NO escrito ahora.
Se mantienen ambos pins explícitos, no dynamicmax, eliminación/relajación de count, skip/disable,
assumption, renumber o configFlyway. Un único existingfile adicional; ningún UPDATE productivo.

Sólo cuando §7 sea realPASS, este addendum supersede condiciones de entrada originales §§2,5,7–9
y checkpoint original§§6,8 EXCLUSIVAMENTE para:
(a) exact32 candidato PRESENT con los hashes§5 en lugar de CREATE32ABSENT;
(b)52/max49 con V48/V49 PRESENT exactsha en lugar de50/max47/ABSENT;
(c) entrada correctiva conocida RED del único pin global en lugar de exigir fullgreen otra vez
antes de esta corrección. El baseline638 verde ANTES de la generación inicial32 permanece
históricamente cumplido e intacto. No se repite esa generación ni se elimina la obligación verde
del conjunto: tras DOS literales, TODA validación nueva§8 debe pasar. Full683 sigue FAIL.
No waiver perpetuo/unlimited/unknown failure. Las demás condiciones originales no se superseden.
Final documental PASS habilita autoridad para reanudar bajo este contrato, jamás technicalgatePASS.

## 5. Snapshot detenido y pins32 obligatorios / PRESERVE sin regeneración

Preflight de este writer antes de cualquier write: worktree Payments y branch
pagos/pagos-notificaciones-r1; localHEAD=upstream(origin/pagos/pagos-notificaciones-r1)=liveorigin
`1564fb5b2e6f9465b83adce8d6c53a418c99330b`, ls-remote read-only exitoso, ahead/behind0/0.
Staging EMPTY; index rawSHA256
`d19d3b5f32c37fa739275daeefa5426dc758dcc7f5a0e17696edb2b8e371809c`.
502 tracked+nonignored untracked, rawmanifest
`841da81baa66226a5ef9049a7273683070905edcea142e2870cf6c68a00d8fd0`.
Algoritmo: exactpaths orden UTF8, path+NUL+lowercaseSHA256(rawbytes)+LF, SHA256 concatenación.
Original470 intactos en entrada, raw
`953a14964a4f5d11f75b852753fc28aa0b7f3fb7857b7077022fa6084d18e72d`.
Todos502 PROTECTED salvo append documental finito ESTADO64649bytes/raw
`f21576b6a909b904ab1cdb2c6616b31199904150a35d320e1e0f7c27b4ee77d0`.
Sólo este Run documental añade los paths§6. No se atribuye baseline ajeno al escritor.

Los32 originalmente CREATE quedan **PRESERVE / NO_WRITE**, ahora y en futureinitialresume;
tabla exacta de task_5021af20f143, recon BODY msg_6c82d77b9408 y task_80829775fcb9 concordantes:

| Path exacto | Raw SHA256 obligatorio |
| --- | --- |
| src/main/java/com/feelingpilates/pagos/ventas/aplicacion/BackfillOrdenSnapshot.java | b24e1b829431e5a91f7ca32af5d4b690b52c45c95ed75530d79eb08c729d4a0f |
| src/main/java/com/feelingpilates/pagos/ventas/aplicacion/CompraHistoricaSnapshot.java | 38e28e926cad743026284f466ac04c66e4d6ed55c44cf883f04eb99089e6a44f |
| src/main/java/com/feelingpilates/pagos/ventas/aplicacion/CongelarOrdenSnapshot.java | 6ab0f95187a8adbedf988c4b01034b69774a4ee21d21dfeb4ff23a8ad33148b7 |
| src/main/java/com/feelingpilates/pagos/ventas/aplicacion/ConsultaHistoricaSnapshot.java | 2431565f7fb7b308fe8eede3eb6bb7fc36edbf961ea43f4408bfbfe3c1608709 |
| src/main/java/com/feelingpilates/pagos/ventas/aplicacion/FuenteHistoricaCompra.java | c86e88aa4f279c12d7b176b6c816b309c821426a635e2bf5e837634450a156b7 |
| src/main/java/com/feelingpilates/pagos/ventas/aplicacion/InformeBackfillSnapshot.java | 25c6d9da5b3810bf46f2ff30e8777b61f5404221188c59a0a0a624cd448d8e11 |
| src/main/java/com/feelingpilates/pagos/ventas/aplicacion/RepositorioOrdenSnapshot.java | ffbc35925dc241ed098b6a61c6539ac00d538ba34d5080f45ac92576af523064 |
| src/main/java/com/feelingpilates/pagos/ventas/dominio/Compra.java | e9899d11aec2b2738ff1ce739a76cd66f0dcd8b49545bef669837d0bfff0de96 |
| src/main/java/com/feelingpilates/pagos/ventas/dominio/CompraComponenteSnapshot.java | 5ae0ad2d7e73d997942138858d1496b20bd23184115828fabc9a7618ee0cfd73 |
| src/main/java/com/feelingpilates/pagos/ventas/dominio/ContenidoSnapshotCanonico.java | 483db6f1f7cf4cfd3da71453373c393b22d58aeee408e6c99c439e30e478a6ac |
| src/main/java/com/feelingpilates/pagos/ventas/dominio/ImporteMonetario.java | e5cbd912a040ed15219e5424b45eedb7588ca79e2abbfbc20c36d0545a46b2d0 |
| src/main/java/com/feelingpilates/pagos/ventas/dominio/OrdenVenta.java | e5122cbaf04d36dbff241a51431346e1944c40937587dc9d5520f76cee2d49fe |
| src/main/java/com/feelingpilates/pagos/ventas/dominio/PoliticaComercialSnapshot.java | c16184239f82777bfec7056b68640bc6129298d14789ca229696ad475616e58c |
| src/main/java/com/feelingpilates/pagos/ventas/dominio/ProvenienciaSnapshot.java | 695373e25e825e71d4c1dafe07c231f2b936b459420d5ba78ed926908c7823b5 |
| src/main/java/com/feelingpilates/pagos/ventas/infraestructura/ConsultaHistoricaSnapshotJdbcAdapter.java | df438e2c89a7201d84a2103b9edb2631a556e6c87d669741786da46ea404e3ce |
| src/main/java/com/feelingpilates/pagos/ventas/infraestructura/FuenteHistoricaCompraJdbcAdapter.java | 89223c356530395a541582501a827ab5431dbe4480d4dd26e99021140c0f083b |
| src/main/java/com/feelingpilates/pagos/ventas/infraestructura/InformeBackfillJdbcAdapter.java | 8458d295f3ae99518be95035a9317d24c18ff8a954abf90521e3443908164a3a |
| src/main/java/com/feelingpilates/pagos/ventas/infraestructura/OrdenSnapshotJdbcAdapter.java | 2959e7c12fbdbaee09d1b1ea14dfd3d271e5b219026b2a27bf3b2a86dee82b21 |
| src/main/resources/db/migration/V48__pn14_slice2_orden_snapshot_expand.sql | edc12860431820d634d4bc837eae79a5f3604718f83d99207b1ac8765459248e |
| src/main/resources/db/migration/V49__pn14_slice2_snapshot_inmutabilidad.sql | 1aa858e265a389e9feeca1c691ace72e36fe87bc48fbdc79ff2e632fc3da280e |
| src/test/java/com/feelingpilates/pagos/ventas/BackfillOrdenSnapshotPostgresTest.java | 5dda0a95515c880a08e8a2487ad7a0c2e3040464283ab689266425381890f0a4 |
| src/test/java/com/feelingpilates/pagos/ventas/BackfillOrdenSnapshotTest.java | 4adff58b23be5225ea236d6f95ff05a39f85607ab9fe83b177a8ccbba96fe80f |
| src/test/java/com/feelingpilates/pagos/ventas/ConsultaHistoricaSnapshotTest.java | 077e3d9ce4055d335e9706f336f226fed382deef7c1df785c1c578a7c14102a0 |
| src/test/java/com/feelingpilates/pagos/ventas/OrdenSnapshotConcurrenciaTest.java | 79e2f8f807eccdd9c687a775fbabf213f2aea5e5129ebf3a51bb11b2d6caf607 |
| src/test/java/com/feelingpilates/pagos/ventas/OrdenSnapshotDominioTest.java | 55c605f696d26dee29a3ec585f18de2c2c6b36ce4592b47d7fc79858d3799471 |
| src/test/java/com/feelingpilates/pagos/ventas/OrdenSnapshotIdempotenciaTest.java | 4075033c5439bfbaa46adc6758a90d10c7c38572edc858e3ce129af90ce2e121 |
| src/test/java/com/feelingpilates/pagos/ventas/OrdenSnapshotMigracionTest.java | a6fdb43bc4b54293c0db96c97ee7db304e8fb7575eb1974db6aa3673a1d1f0b1 |
| src/test/java/com/feelingpilates/pagos/ventas/OrdenSnapshotPersistenciaTest.java | 9d275805c51a31a13d44cec227f114933f7aef9fd1833bd6a0c6d70c8e525b77 |
| src/test/java/com/feelingpilates/pagos/ventas/OrdenSnapshotTransaccionTest.java | 9d5b6e713ff238df9dcefa24f3dc8d45c90affcb1c350f334f96bda6c5695c11 |
| src/test/java/com/feelingpilates/pagos/ventas/PN14Slice2Fixtures.java | 403a48fa7ee4d4de6a80bfd2f057c6229087e5b8d56788e22aae91785ed98fd9 |
| src/test/java/com/feelingpilates/pagos/ventas/PoliticaSnapshotCanonicoTest.java | 644201fc91164971c027e8d58d9bdf71f94a7095c0675f484ab807bb9db96bf9 |
| src/test/java/com/feelingpilates/pagos/ventas/VentasSnapshotArquitecturaTest.java | a75ee330361c1b79bf3224edd167da8250ddeffb27c9e33c4f16c514b4a45814 |

## 6. Scope documental exact3 inicial y exact5 final / roles separados

Este writer sólo apply_patch:
CREATE auditoria/handoffs/HANDOFF-PN14-SLICE2-REANUDACION-SNAPSHOT-FLYWAY.md;
CREATE auditoria/fase-pn14-slice2-reconciliacion-scope-flyway.md;
APPEND_ONLY_END auditoria/ESTADO-ACTUAL.md preservando COMPLETO64649bytes/raw§5.
No otra escritura. Original5 de6, TODOS oldhandoffs/reviews/checkpoints y otros501 intactos.
Review/manifest siguientes ABSENT ahora; únicamente acceptance materializer separado tras
initial fresh audit y primer gate realesPASS los crea. Paths totales finales:

```json
[
  "auditoria/ESTADO-ACTUAL.md",
  "auditoria/handoffs/HANDOFF-PN14-SLICE2-REANUDACION-SNAPSHOT-FLYWAY.md",
  "auditoria/fase-pn14-slice2-reconciliacion-scope-flyway.md",
  "auditoria/reviews/PN14-SLICE2-REVIEW-RECONCILIACION-SCOPE-FLYWAY.md",
  "auditoria/reviews/PN14-SLICE2-MANIFEST-REANUDACION-LOCAL-SNAPSHOT-FLYWAY.md"
]
```

Acceptancewriter puede APPEND_ONLY_END ESTADO y NUEVOcheckpoint, preservando también sus
prefixes completos iniciales registrados externamente por este writer; CREATE AJENOreview y manifest.
NUEVOhandoff queda inmutable en SHA aceptado por initialGate. Ningún oldcheckpoint/MAPA edit.
Review evidencia AJENA, no autoridad/selfauthorization. Manifest escrito ÚLTIMO después de los
otros4 docs. Incluye tabla COMPLETA original502 de task_80829775fcb9, sólo excepción prefixESTADO,
exact32pins, sixraw originalauth, original50migrations/checksums/digest
`e2848476012dc44133776cad500f2bb0870ecf7c1b66cdd9dbcab0ee5fddb398`,
V48/V49 pins, testbefore, index/staging/HEAD/WT y finiteallowedappend/prefixes.
Sus cuatro coreSHA finales son físicos; NO propio selfSHA ni wholecurrentraw circular.
PropioSHA manifest, candidateFileSHA256/authorityFileSHA256 exact5 y wholecurrent count/rawmap
se entregan EXTERNAMENTE en resultados materializador/verificador/finalGate sin ciclo.

## 7. Eficacia final SI Y SÓLO SI — ninguna autoactivación

Secuencia separada obligatoria:
initialdocumenter → fresh DOCUMENT_AUDITOR task_8be2fd0f4dfe →
task_48dc5aedfde6/gate_0bc68ac55878 → separate acceptance materializer AJENOreview/manifest →
fresh final verifier task_4153eb194c80 → task_86f0daaa60b1/gate_6dff94e12724.

Initialauditor debe completar succeeded/uniqueDone competente accepted con
SCOPE_RECONCILIATION_AUTHORIZATION_AUDIT=PASS, P0=0/P1=0/filesModified=[], fresh independiente,
exact3docsha/protected502/prefix/pins evidenciados. Primer gate COMPLETED/RESOLVED/PASS,
provenance coordinator_gate_resolution, candidateFileSHA256 exact3 idéntico físico/writer/auditor.
Sólo permite acceptance documental, no corrección técnica. HOY auditor y gate APPLICABLE/PENDING.

Después acceptancewriter persiste audit/gate reales y entrada local condicional en ESTADO/
NUEVOcheckpoint, originalNEWhandoff immutableacceptedhash. Finalverifier fresh independiente
de todoswriters completa succeeded, único worker_done competente accepted del Dispatch real,
FINAL_SCOPE_RECONCILIATION_VERIFICATION=PASS, P0=P1=0/filesModified=[].
Finalgate COMPLETED/RESOLVED/PASS real por coordinador, provenance coordinator_gate_resolution,
con candidateFileSHA256 Y authorityFileSHA256 EXACT5, localEntryManifestSHA256 propio externo,
wholecurrentraw/count/map idénticos a bytes físicos finales, materializador/verificador y
integridad independiente coordinador. Sin extras/omisiones, humanos pendientes/securitystop
ni mutación posterior. HOY finalverifier/finalgate APPLICABLE/PENDING, ningún PASS fabricado.

Sólo la conjunción real y append competente hace eficaz
LOCAL_UNCOMMITTED_AUDITED_DOCUMENTATION_ENTRY / EXACT_PRESERVED_CANDIDATE_RESUME:
authority READY_TO_RESUME_BOUNDED_CORRECTION, IMPLEMENTATION_NOT_RESUMED.
Antes de futura initialwrite corrector recupera resultados/gates/Task/Dispatch/uniqueDone reales,
revalida HEAD/upstream/live/indexEMPTY/0–0, original502rawmap (ESTADO sólo prefix permitido),
final5raw docmanifest/prefixes,32presentunchanged,50oldmigrations intactos,52/max49/twoSQLsha,
protectedtest todavía beforeSHA§4, no deleted/unexpected dirty/newfile.
ÚNICA initialwrite futura: dos literales§4; ningún32overwrite/regeneración ni docwrite.
Este candidato NO satisface aún esa entrada. Sin publicación previa requerida ni permiso Git.
Ninguna edición documental después de finalgate; cambio de bytes requiere nueva autorización.

## 8. Validación POSTCORRECCIÓN nueva completa / orden obligatorio

Entorno exactamente originalhandoff§8: JDK21 real, Maven wrapper, Docker host/context competente,
api.version1.44 existente, Ryuk no desactivado, PostgreSQL16-alpine real efímero dummy; nada H2/live.
Comandos físicos copiados literalmente del handoff original§8, más focal protegido de15 tests;
ninguna transcripción escapada de un histórico sustituye estos comandos. Plan futuro, NO ejecutado aquí:

```sh
export JAVA_HOME="$(/usr/libexec/java_home -v 21)"
java -version
./mvnw -version
docker version
docker info
env -u DB_HOST -u DB_PORT -u DB_NAME -u DB_USER -u DB_PASSWORD STRIPE_SECRET_KEY= STRIPE_PUBLISHABLE_KEY=pk_test_pn14_dummy STRIPE_WEBHOOK_SECRET=whsec_pn14_dummy JWT_SECRETO=pn14-dummy-secret-at-least-thirty-two-bytes COMPRA_PENDIENTE_EXPIRA_MINUTOS=60 TZ=UTC ./mvnw -Djunit.jupiter.execution.parallel.enabled=false -DskipTests=false -Dmaven.test.skip=false -DfailIfNoTests=true -Dtest=ProgramacionPersistenciaTest test
env -u DB_HOST -u DB_PORT -u DB_NAME -u DB_USER -u DB_PASSWORD STRIPE_SECRET_KEY= STRIPE_PUBLISHABLE_KEY=pk_test_pn14_dummy STRIPE_WEBHOOK_SECRET=whsec_pn14_dummy JWT_SECRETO=pn14-dummy-secret-at-least-thirty-two-bytes COMPRA_PENDIENTE_EXPIRA_MINUTOS=60 TZ=UTC ./mvnw -Djunit.jupiter.execution.parallel.enabled=false -DskipTests=false -Dmaven.test.skip=false -DfailIfNoTests=true -Dtest=OrdenSnapshotDominioTest,PoliticaSnapshotCanonicoTest,OrdenSnapshotPersistenciaTest,OrdenSnapshotTransaccionTest,OrdenSnapshotIdempotenciaTest,OrdenSnapshotConcurrenciaTest,BackfillOrdenSnapshotTest,BackfillOrdenSnapshotPostgresTest,OrdenSnapshotMigracionTest,ConsultaHistoricaSnapshotTest,VentasSnapshotArquitecturaTest test
env -u DB_HOST -u DB_PORT -u DB_NAME -u DB_USER -u DB_PASSWORD STRIPE_SECRET_KEY= STRIPE_PUBLISHABLE_KEY=pk_test_pn14_dummy STRIPE_WEBHOOK_SECRET=whsec_pn14_dummy JWT_SECRETO=pn14-dummy-secret-at-least-thirty-two-bytes COMPRA_PENDIENTE_EXPIRA_MINUTOS=60 TZ=UTC ./mvnw -Djunit.jupiter.execution.parallel.enabled=false -DskipTests=false -Dmaven.test.skip=false -DfailIfNoTests=true -Dtest=PagoIntentoPN14Test,PagoWebhookPN14Test,PagoReconciliacionPN14Test,PagoLecturasPN14Test,VentaServicePN14Test,CatalogoPN14Test,PagosApiPN14Test,VentasCatalogoApiPN14Test,ReservasPN14Test,ReservasApiPN14Test,CompraPersistenciaPN14Test,ReservaServiceCaracterizacionTest,ReservaControllerSecurityTest test
env -u DB_HOST -u DB_PORT -u DB_NAME -u DB_USER -u DB_PASSWORD STRIPE_SECRET_KEY= STRIPE_PUBLISHABLE_KEY=pk_test_pn14_dummy STRIPE_WEBHOOK_SECRET=whsec_pn14_dummy JWT_SECRETO=pn14-dummy-secret-at-least-thirty-two-bytes COMPRA_PENDIENTE_EXPIRA_MINUTOS=60 TZ=UTC ./mvnw -Djunit.jupiter.execution.parallel.enabled=false -DskipTests=false -Dmaven.test.skip=false test
git diff --check
git diff --cached --check
```

Orden protegido15focal → Slice2 T01–T18/11classes nuevas → Slice1 M01–M12/13classes publicados →
full → comprobar evidencia realPG/Testcontainers fresh52/max49 y upgradeV47:50→52,
TODOS50checksums antiguos idénticos/legacycolumns/defaults/indices/constraints/CRUD,
concurrencia con conexiones/transactions/PIDs independientes, barreras/timeouts,
winner/replay/conflict/rollback/atomicidad/canon/immutability → requiredSkips0 →
fresh AUDITOR técnico independiente → gate técnico separado SCOPE/TESTS/IMPLEMENTATION/HOST.
Pruebas PG se ejecutan dentro de focal/full; extracción de logs/XML y comprobación sigue full.
Registrar todos counts/exit/failures/errors/skips frescos físicamente por class/method,
mapping T01–T18 y M01–M12 íntegro. Conteos638/45/67/683 previos NO se sustituyen por evidencia nueva.
Green requiere failures/errors/skips0, protected15 presentes y todas classes requeridas reales.
Docker ambiental BLOCKED: HostValidator separado competente sobre plan estático permitido;
nunca skip verde. No technicalaudit/gatePASS de este escritor o por simple testsverde.

## 9. Stops, exclusiones y lifecycle actual

Contradicción adicional real, otro fallo/scope indispensable/decisión no derivable:
STOP / PRODUCT_OR_ARCHITECTURAL_AUTHORITY_REQUIRED o SCOPE_EXPANSION_REQUIRED / NO_WRITES;
preguntar coordinador, sin inventar producto. Hash/index/HEAD/path/manifest/evidencia mismatch:
STOP / AUTHORIZATION_MISMATCH / NO_WRITES. Ausente/stale/UNKNOWN/FAIL/SKIPPED/BLOCKED no satisface
gates requeridos; única RED permitida en entrycorrectiva es la causa histórica exacta§2–4.
Rollback STOP conserva candidato, oldwriter y toda evidencia; no reset/clean/stash/delete/downgrade.

No Java producción, userdomain/backend/HTTP readerwriter switch, livebackfill/cutover/fence,
ledger/Pago/Acreditacion/derechos/StripeInbox/refund/Outbox/emailpush, recursos/pom/config/wrappers/
testhelpers, F2E inspección/integración, slices3–12, publicación/cierre, stage/commit/push/fetch/pull.
Current tests/host/technicalaudit/technicalgates NOT_APPLICABLE / NOT_PERFORMED, nunca PASS;
publicación NOT_PERFORMED / NO_PERMISSION / auto_publish=false. Baseline638 y full683FAIL
son historia, no ejecución propia. Current authorization NOT_ACTIVE y technicalimplementation
NOT_RESUMED. Próximo rol: initial fresh independent authorization audit exact3, luego gate.
