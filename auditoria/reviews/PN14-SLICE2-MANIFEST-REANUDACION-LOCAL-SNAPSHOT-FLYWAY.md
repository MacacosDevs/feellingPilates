# PN14 Slice2 — manifest finito de reanudación local / snapshot Flyway

Status: CONDITIONAL / PENDING_FINAL_FRESH_VERIFICATION_AND_FINAL_GATE / NOT_SATISFIED.
Creado ÚLTIMO, tras ESTADO/newhandoff/newcheckpoint/newreview físicos finales.
Este manifest protege bytes y no se autoautoriza; autoridad exclusiva de handoff nuevo §7,
ESTADO append competente ycheckpoint §8. AJENO initialaudit/initialgate reales documentados,
finalfreshverifier/finalgate aún APPLICABLE/PENDING; implementación NOT_RESUMED_YET.
No propio selfSHA niwholeFinal506raw dentro de sí mismo: todos los bindings que incluyen este
manifest se emiten EXTERNOS en BODY estructurado materializador/verificador/coordinator/finalgate,
sin ciclos y sin ninguna edición documental después finalgate. No technicalPASS propio.

## 1. Contrato físico exacto, prefixes, cuatro coreSHA y condición finita

```json
{
  "kind": "LOCAL_UNCOMMITTED_AUDITED_DOCUMENTATION_ENTRY",
  "resumeProfile": "EXACT_PRESERVED_CANDIDATE_RESUME",
  "statusAtMaterialization": "CONDITIONAL_PENDING_FINAL_VERIFICATION_AND_FINAL_GATE_NOT_SATISFIED",
  "role": "DOCUMENTATION_ACCEPTANCE_ACTIVATION_MATERIALIZER / SEPARATE_DOCUMENTER / NOT_AUDITOR",
  "runId": "run_bc3f744161b5",
  "taskId": "task_8b1e56ae86f0",
  "dispatchId": "ctx_25d9756f1da3",
  "worktree": "/Users/jesusaldaircruzortiz/Desktop/Feelingpilates/feelingpilates-payments-notifications",
  "branch": "pagos/pagos-notificaciones-r1",
  "HEAD": "1564fb5b2e6f9465b83adce8d6c53a418c99330b",
  "upstreamHEAD": "1564fb5b2e6f9465b83adce8d6c53a418c99330b",
  "liveOriginHEAD": "1564fb5b2e6f9465b83adce8d6c53a418c99330b",
  "aheadBehind": "0/0",
  "staging": "EMPTY",
  "indexSHA256": "d19d3b5f32c37fa739275daeefa5426dc758dcc7f5a0e17696edb2b8e371809c",
  "indexEntriesSHA256": "6a0e4429e84f4768e4d73382c45cc94ceeb0bc7a4e3b2e64f3f0350b2da334f8",
  "acceptedImmutableNewHandoffSHA256": "0168c3825b16c8dbace6ebb9c22fbbeefb1aabf5efecf5922f9c4c5fe1b340d8",
  "coreFileSHA256": {
    "auditoria/ESTADO-ACTUAL.md": "a05c8347cd6e8e975f89516d5c52229deaf497cd465f72077d685e1b4734296d",
    "auditoria/handoffs/HANDOFF-PN14-SLICE2-REANUDACION-SNAPSHOT-FLYWAY.md": "0168c3825b16c8dbace6ebb9c22fbbeefb1aabf5efecf5922f9c4c5fe1b340d8",
    "auditoria/fase-pn14-slice2-reconciliacion-scope-flyway.md": "7d44cbb06441ab2ecd24fe04886de6c0c06c7a10b24eca468215ab8efb028c71",
    "auditoria/reviews/PN14-SLICE2-REVIEW-RECONCILIACION-SCOPE-FLYWAY.md": "6a90e4b61ebccb4baba95bf67b0157b3fc2998d648d6baa50e778e826cddd4c7"
  },
  "manifestSelfSHA256": "EXTERNAL_ONLY_NO_SELF_HASH",
  "localEntryManifestSHA256": "EXTERNAL_ONLY_NO_SELF_HASH",
  "wholeFinal506RawSHA256": "EXTERNAL_ONLY_NO_CYCLE",
  "wholeFinal506FileSHA256": "EXTERNAL_ONLY_IN_MATERIALIZER_VERIFIER_COORDINATOR_FINAL_GATE_BODIES",
  "finalAuthorityExactPaths": [
    "auditoria/ESTADO-ACTUAL.md",
    "auditoria/handoffs/HANDOFF-PN14-SLICE2-REANUDACION-SNAPSHOT-FLYWAY.md",
    "auditoria/fase-pn14-slice2-reconciliacion-scope-flyway.md",
    "auditoria/reviews/PN14-SLICE2-REVIEW-RECONCILIACION-SCOPE-FLYWAY.md",
    "auditoria/reviews/PN14-SLICE2-MANIFEST-REANUDACION-LOCAL-SNAPSHOT-FLYWAY.md"
  ],
  "ownWriteAllowlist": [
    {
      "path": "auditoria/ESTADO-ACTUAL.md",
      "mode": "APPEND_ONLY_END",
      "prefixes": [
        64649,
        74494
      ]
    },
    {
      "path": "auditoria/fase-pn14-slice2-reconciliacion-scope-flyway.md",
      "mode": "APPEND_ONLY_END",
      "prefixes": [
        14737
      ]
    },
    {
      "path": "auditoria/reviews/PN14-SLICE2-REVIEW-RECONCILIACION-SCOPE-FLYWAY.md",
      "mode": "CREATE"
    },
    {
      "path": "auditoria/reviews/PN14-SLICE2-MANIFEST-REANUDACION-LOCAL-SNAPSHOT-FLYWAY.md",
      "mode": "CREATE_LAST"
    }
  ],
  "otherOriginal501": "PROTECTED_RAW_BYTE_IDENTICAL / NO_WRITE",
  "completePrefixes": [
    {
      "path": "auditoria/ESTADO-ACTUAL.md",
      "kind": "ORIGINAL_502_ESTADO_PREFIX",
      "bytes": 64649,
      "SHA256": "f21576b6a909b904ab1cdb2c6616b31199904150a35d320e1e0f7c27b4ee77d0"
    },
    {
      "path": "auditoria/ESTADO-ACTUAL.md",
      "kind": "ACCEPTED_INITIAL_GATE_COMPLETE_PREFIX",
      "bytes": 74494,
      "SHA256": "e96a292da67591caab20daa7c596750861c5927a155237b37098c3a009e8ea2d"
    },
    {
      "path": "auditoria/fase-pn14-slice2-reconciliacion-scope-flyway.md",
      "kind": "INITIAL_CHECKPOINT_COMPLETE_PREFIX",
      "bytes": 14737,
      "SHA256": "20d6984f3a708179cf36013e32db6f1de080a24a64db7a68ba6beb5694b5f06c"
    }
  ],
  "originalSixAuthoritySource": "actual task_fe41eb6e6c0d/run_ee58d2f04418 result authorityFileSHA256",
  "originalSixAuthorityFileSHA256": {
    "auditoria/ESTADO-ACTUAL.md": "f21576b6a909b904ab1cdb2c6616b31199904150a35d320e1e0f7c27b4ee77d0",
    "auditoria/contexto/MAPA-LEGACY-Y-MIGRACION.md": "f7ef171bfa7abe1001ff8380716f6883f4db66d7b13c15c3ecc8c1b0dbf962c3",
    "auditoria/fase-pn14-slice2-autorizacion-orden-snapshot-inmutable.md": "be4f3348f5918f4640c597899fcb23b1243bfaca01c03e3ed25040f0a9801cbc",
    "auditoria/handoffs/HANDOFF-PN14-SLICE2-ORDEN-SNAPSHOT-INMUTABLE.md": "4d7e7803557f75a3d62afe67a757687a63b9c627b0fdbf2580d89feab36fdd7d",
    "auditoria/reviews/PN14-SLICE2-MANIFEST-ENTRADA-LOCAL-ORDEN-SNAPSHOT-INMUTABLE.md": "ffea6f1861a0f595336d3376af2f259b26837dd75c3c67d7c2ebbe7a4cd0622b",
    "auditoria/reviews/PN14-SLICE2-REVIEW-AUTORIZACION-ORDEN-SNAPSHOT-INMUTABLE.md": "1477ba79459681a195cccad310540952f8f1d84ab1069a87154de340a279c3d3"
  },
  "originalSixESTADOInterpretation": "EXACT_ORIGINAL64649_PREFIX; other5 whole files unchanged",
  "original502Source": "actual task_80829775fcb9/run_bc3f744161b5 all502FileSHA256 result; equal authoritative recon BODY msg_6c82d77b9408",
  "original502Count": 502,
  "original502RawSHA256": "841da81baa66226a5ef9049a7273683070905edcea142e2870cf6c68a00d8fd0",
  "original470Count": 470,
  "original470RawSHA256": "953a14964a4f5d11f75b852753fc28aa0b7f3fb7857b7077022fa6084d18e72d",
  "stoppedCandidateSource": "actual task_5021af20f143/run_e786453bf13f all32CandidateSHA256",
  "ownBefore": {
    "count": 504,
    "rawSHA256": "aa70b28dbb67c045f338bf94278bc2bc64772011294119e4300e3333f89e12d6",
    "indexSHA256": "d19d3b5f32c37fa739275daeefa5426dc758dcc7f5a0e17696edb2b8e371809c",
    "HEAD": "1564fb5b2e6f9465b83adce8d6c53a418c99330b",
    "upstreamHEAD": "1564fb5b2e6f9465b83adce8d6c53a418c99330b",
    "liveOriginHEAD": "1564fb5b2e6f9465b83adce8d6c53a418c99330b",
    "staging": "EMPTY",
    "dirtyCount": 40
  },
  "expectedFinalFileCount": 506,
  "expectedFinalDirtyFileCount": 42,
  "newDocsOnlyThisRun": [
    "auditoria/handoffs/HANDOFF-PN14-SLICE2-REANUDACION-SNAPSHOT-FLYWAY.md",
    "auditoria/fase-pn14-slice2-reconciliacion-scope-flyway.md",
    "auditoria/reviews/PN14-SLICE2-REVIEW-RECONCILIACION-SCOPE-FLYWAY.md",
    "auditoria/reviews/PN14-SLICE2-MANIFEST-REANUDACION-LOCAL-SNAPSHOT-FLYWAY.md"
  ],
  "initialFreshAudit": {
    "taskId": "task_8be2fd0f4dfe",
    "dispatchId": "ctx_95cd697fecd8",
    "statusBody": "msg_9174b578aff1",
    "uniqueDone": "msg_f8caacbe69e5",
    "doneCount": 1,
    "outcome": "succeeded",
    "settled": true,
    "accepted": true,
    "released": true,
    "verdict": "SCOPE_RECONCILIATION_AUTHORIZATION_AUDIT=PASS",
    "P0": 0,
    "P1": 0,
    "newP2": 0,
    "preexistingNonBlockingP2": "NEW-PN13-017 unchanged",
    "filesModified": []
  },
  "initialResolvedGate": {
    "taskId": "task_48dc5aedfde6",
    "gateId": "gate_0bc68ac55878",
    "taskStatus": "completed",
    "gateStatus": "resolved",
    "resolution": "PASS",
    "resolved_at": "2026-09-16 21:20:59",
    "provenance": "coordinator_gate_resolution",
    "candidateFileSHA256": {
      "auditoria/ESTADO-ACTUAL.md": "e96a292da67591caab20daa7c596750861c5927a155237b37098c3a009e8ea2d",
      "auditoria/handoffs/HANDOFF-PN14-SLICE2-REANUDACION-SNAPSHOT-FLYWAY.md": "0168c3825b16c8dbace6ebb9c22fbbeefb1aabf5efecf5922f9c4c5fe1b340d8",
      "auditoria/fase-pn14-slice2-reconciliacion-scope-flyway.md": "20d6984f3a708179cf36013e32db6f1de080a24a64db7a68ba6beb5694b5f06c"
    },
    "authorityFileSHA256": {
      "auditoria/ESTADO-ACTUAL.md": "e96a292da67591caab20daa7c596750861c5927a155237b37098c3a009e8ea2d",
      "auditoria/handoffs/HANDOFF-PN14-SLICE2-REANUDACION-SNAPSHOT-FLYWAY.md": "0168c3825b16c8dbace6ebb9c22fbbeefb1aabf5efecf5922f9c4c5fe1b340d8",
      "auditoria/fase-pn14-slice2-reconciliacion-scope-flyway.md": "20d6984f3a708179cf36013e32db6f1de080a24a64db7a68ba6beb5694b5f06c"
    },
    "scope": "DOCUMENTARY_ACCEPTANCE_ONLY / NO_TECHNICAL_WRITES"
  },
  "finalFreshVerifier": {
    "taskId": "task_4153eb194c80",
    "statusAtMaterialization": "APPLICABLE/PENDING",
    "actualTaskStatus": "ready",
    "result": null,
    "required": "fresh independent READ_ONLY; succeeded; uniqueDone competent accepted actualDispatch; FINAL_SCOPE_RECONCILIATION_VERIFICATION=PASS; P0=P1=0; filesModified=[]"
  },
  "finalCoordinatorGate": {
    "taskId": "task_86f0daaa60b1",
    "gateId": "gate_6dff94e12724",
    "statusAtMaterialization": "APPLICABLE/PENDING",
    "actualTaskStatus": "blocked",
    "resolution": null,
    "required": "taskcompleted/gateRESOLVED/PASS real coordinator_gate_resolution with exact5 candidate AND authority SHA maps plus external manifestselfSHA/localEntryManifestSHA256/whole506count,raw,map exhaustive equality physical/materializer/verifier/coordinator; no omissions/extras/postmutation/securitystop/humanpending"
  },
  "activationConditionAuthority": "newimmutablehandoff§7 / ESTADO last acceptanceappend / newcheckpoint§8",
  "effectiveOnlyAfterRealFinalConjunction": "LOCAL_CANDIDATE_PRESERVED / AUTHORITY_RECONCILED / READY_TO_RESUME_TECHNICAL_VALIDATION; READY_TO_RESUME_BOUNDED_CORRECTION / IMPLEMENTATION_NOT_RESUMED",
  "implementation": "NOT_RESUMED_YET",
  "historicalFull": "FAIL / 683tests / 1failure / 0errors / 0skips / exit1 / T18FAIL",
  "technicalAudit": "NOT_AUDITED",
  "technicalGate": "NOT_REACHED",
  "postFinalGateDocumentEdits": "NONE_PERMITTED",
  "futureInitialExistingFileWriteAllowlist": [
    {
      "path": "src/test/java/com/feelingpilates/programacion/ProgramacionPersistenciaTest.java",
      "beforeSHA256": "0a4ca66231d7f0545568ecbef6bb09e58270b4d68c4f567ebaac22d7a9ae9ea7",
      "line47": ".isEqualTo(\"47\") -> .isEqualTo(\"49\")",
      "line48": ".hasSize(50) -> .hasSize(52)",
      "hypotheticalAfterSHA256": "f6d81bd5cb6b57d8440ab581da967e2bc77570c988f2db5f84fdc1c1b26d4372",
      "hypotheticalAfter": "NOT_WRITTEN / MEMORY_ONLY",
      "otherBytes": "ALL_PRESERVED including flywayMigraDesdeV1HastaV47 name and14semanticcases; no configuration/skips/dynamicpins/otherpath/production; no32regeneration"
    }
  ],
  "migrationProtection": {
    "oldCount": 50,
    "oldRawSHA256": "e2848476012dc44133776cad500f2bb0870ecf7c1b66cdd9dbcab0ee5fddb398",
    "currentCount": 52,
    "currentMaxMajor": 49,
    "V48": {
      "src/main/resources/db/migration/V48__pn14_slice2_orden_snapshot_expand.sql": "edc12860431820d634d4bc837eae79a5f3604718f83d99207b1ac8765459248e"
    },
    "V49": {
      "src/main/resources/db/migration/V49__pn14_slice2_snapshot_inmutabilidad.sql": "1aa858e265a389e9feeca1c691ace72e36fe87bc48fbdc79ff2e632fc3da280e"
    }
  },
  "protected32Permission": "PRESERVE / NO_WRITE / NO_OVERWRITE / NO_REGENERATION",
  "currentTestsMavenHostTechnicalAuditTechnicalGates": "NOT_APPLICABLE / NOT_EXECUTED / NO_PASS",
  "writerFindings": "NOT_ASSESSED",
  "GitWrites": false,
  "publication": "NOT_PERFORMED / NO_PERMISSION / auto_publish=false",
  "closure": "NOT_AUTHORIZED",
  "F2E": "NOT_INSPECTED_NOT_INTEGRATED"
}
```

## 2. ALL original502 rawSHA256 individuales — mapa COMPLETO de entrada autoritativa

Fuente física/coordinador task_80829775fcb9 result all502FileSHA256, recuperado task-list --run,
comparado exhaustivamente con recon autoritativo BODY msg_6c82d77b9408 ycandidate detenido.
Esta tabla/mapa es el baseline previo al initialdocumenter; raw841da81... íntegro.
ÚNICA excepción ESTADO:64649bytes exactprefix; otros501 CADA byte preservado incluyendo32 ytest.
No hash del manifest actual dentro del mapa original, ni baseline genérico waived.

```json
{
  ".env.example": "4578f61ab7fb104e72564a6183e25cd5ef21dca90ab13bee8bd88921054a9da6",
  ".gitattributes": "5775a77ad2f6d12dde053a3fb0cca4309189163f0888f4e7e984cd0ab327028b",
  ".gitignore": "99868de8c49276583ca9830e50174d0b310445ca6c39523c407042ed440e122a",
  ".mvn/wrapper/maven-wrapper.properties": "488e1b3f2e641779d4636abf9390845f901e64607261bc3c0b0bfe4fe96e6706",
  "AGENTS.md": "1d70ebbc5bfef3c0004a7ca53ce8a91bf6a9660a4085e2c7ff2a44a5d58038c4",
  "Dockerfile": "ba169b54da91a2f8cee98c6c08df7a699402118d0a94e1057a00304faec8f945",
  "README.md": "3239d68abf409c88ca8d2603e195b6128fa2fbdab6dfc13d94a6bfa0c0474096",
  "auditoria/00-revalidacion-repositorio-completo.md": "845fb0a569a7d3efe31418c28f074f074625d8d04bdb3217450bfbfe724471bc",
  "auditoria/04-arquitectura-objetivo.md": "684e5630e83c8d3da6d024f5f76ca4a43df31d07a5e7141f18d5143042b24b40",
  "auditoria/ARQUITECTURA-ACTUAL.md": "95eee81e34a3437628882b628ae138d6680c272767b8f8ebafb1027f675c7bda",
  "auditoria/DECISIONES-ARQUITECTONICAS.md": "305bb270f770029e4ea576019b9410970f12c7e70da554fa7b5e09f737123b83",
  "auditoria/ESTADO-ACTUAL.md": "f21576b6a909b904ab1cdb2c6616b31199904150a35d320e1e0f7c27b4ee77d0",
  "auditoria/README-REESTRUCTURACION.md": "747227acc83ad7337d8a1214646ee089def5ed42ec6eee637d22ff96969ce2b2",
  "auditoria/REGLAS-DE-TRABAJO-IA.md": "84799b8946e428e7e907bc20b96362e699347e7307d0ba886c4d85c06ffa55ea",
  "auditoria/contexto/DOMINIO-FUNCIONAL.md": "0580272ff72a41c841830e2e6cf13e8c1022e3716a59d741a780f4661a4b0b3a",
  "auditoria/contexto/MAPA-LEGACY-Y-MIGRACION.md": "f7ef171bfa7abe1001ff8380716f6883f4db66d7b13c15c3ecc8c1b0dbf962c3",
  "auditoria/fase-0a-baseline-git.md": "5f01bc46d5bba75fbd27038d21c10db2d103eea9464df8a5c76298305924f95d",
  "auditoria/fase-0b-preservacion-baseline.md": "647d13174e9c2b9c792459e63c6ce6fcc07d29a2e11e87bfb1561fcf174ad36c",
  "auditoria/fase-0c-flyway-ambientes.md": "851eac715066d287f490d291668663c50030dbb6b9ebbb074f4ae18634180830",
  "auditoria/fase-0c1-saneamiento-flyway.md": "7091a45752d2b3b46964ec9ce800221d393c44722de13e0456f2e144b5d75cd4",
  "auditoria/fase-0d-tests-baseline.md": "03dd5f71b78a388c818e93b89fd7ebcd58da7849890faf4e483218a078a60d36",
  "auditoria/fase-0e1-reservas-idor.md": "497faadf161b1950aab4f8e1a6499ad9daf30bd453740f0a89e5c99b0bc03c93",
  "auditoria/fase-0e2-scope-salon.md": "de646ec3881ae074ecf695af29140342fe1f4fe30f0228590e07fb4d09a4d24e",
  "auditoria/fase-0e3-redaccion-secretos.md": "599468c77c195320e24cf4b68830c1901ca9d01b99aca26bf479e7ae5786a6a4",
  "auditoria/fase-1a-safety-net-calendario.md": "e51acba3a1ce42d046238b92d97b9df52a79c9047b5dea7047e48a8c2925548e",
  "auditoria/fase-1a1-safety-net-calendario.md": "dba837ab1ce0c3f427a143f7eae38d34c1acd4aa24c4815932f60d4bcb8ed0e4",
  "auditoria/fase-1b-diseno-programacion.md": "69fe7874ab348b135ccb73b790f3d30538111e3f15659fae18b8d2e0adecee11",
  "auditoria/fase-1c-modelo-base-programacion.md": "c3fbfc1447cec29a03ef9926a28ffb514fb668ae700046c00e3916b3fcf9943b",
  "auditoria/fase-1c1-safety-net-modelo-base.md": "ccfeee535b78f68fa6542c66a75885c84cc359dbdbaf6d2c21f5fc2581086792",
  "auditoria/fase-1c2-query-binding-vigencias.md": "067f8a64642226b71ddf52d90542fb592fd165c5aafd4a81d8495ad212dcff05",
  "auditoria/fase-2a-horario-vigencia-politicas.md": "268382162fdfb47453aa38d6c7db9455e022f6e78050db59d188821f4665f076",
  "auditoria/fase-2a1-safety-net-migracion-horario.md": "517caba47e08465b70b7b2ff8a3e218c83a42271c420360e9de342c5030ddf9a",
  "auditoria/fase-2b-diseno-versionado-horario.md": "83266b98f371c5bb1c22a324508da5513703b88ddeab3ed1d8f47b1f15ea2c9d",
  "auditoria/fase-2b1-nucleo-temporal-horario.md": "7cf60ac43c1ae2d4529311b7830f200ac45b74c52a93984c3f380e4eebbfec60",
  "auditoria/fase-2b2-consumidores-horario-temporal.md": "d1e56d741673fc186a9371d54f6d35cc34553870b3b6e3ea0bbd3881591648d2",
  "auditoria/fase-2b3a-persistencia-versionado-horario.md": "6def09db373968f8dabe9c31aea50982b6317e356a9119428f50025808465445",
  "auditoria/fase-2b3b-diseno-writers-concurrencia.md": "76cfd65feee30fea0ae0bc63eab9011069fb0cc2213a089716cafa72b9e35959",
  "auditoria/fase-2b3b1-writers-concurrencia-horario.md": "b612f65ee385586ae034823a41b6ecb407ddd1b52138f653c6e923a9a8dea508",
  "auditoria/fase-2b3b2-diseno-api-frontend-horarios.md": "70f49df3e19755d992057eb0785eeebbb1be849e65458609ad277d12198bf2fd",
  "auditoria/fase-2b3b2a-api-backend-horarios.md": "b1edfb108bf59244b4c35fd0de73f51ebc1b94a541b6874b27225521a3b0ac28",
  "auditoria/fase-2c1-diseno-excepciones-horario-fecha.md": "b0ddb92e171246d561cedd7a3ebe2d93fd815b389870e622ddb578608a3ff25c",
  "auditoria/fase-2c2-implementacion-excepciones-horario-fecha.md": "a858286d7a8efd1d77195ef18a620450bc4cbe404a159353c5b5638b9c6c344a",
  "auditoria/fase-2d1-diseno-ajustes-programacion-fecha.md": "58af39f41b3bc089ebbd4ec67f684e270087ddf4eb695f2c7b55276d0aff352e",
  "auditoria/fase-2d2-implementacion-dark-launch-ajustes-programacion-fecha.md": "d7eb6eb5c86ee0658207b8be957517f3715937d66389b8ab2f41c1ac52a042cf",
  "auditoria/fase-2e-diseno-adapters-read-only-snapshot-consistency.md": "6c72cba1f83fbc2bcf3b3219d8252d2410ec482e30ae85d30fd2eeb04e8883d8",
  "auditoria/fase-2e-identidad-semantica-detector-read-only.md": "6f850e9723f9861456d646039e4b233cff20d013ff956cab98f7370dffac4670",
  "auditoria/fase-2e-preparacion-migracion-controlada.md": "e92c78df47cce8a3cd486f97d85216716003a4617ed03374dd080558fab61b9f",
  "auditoria/fase-pn13-materializacion-autoridad-pagos-notificaciones.md": "5605569945e72a9d7ceff2778c64a9444d077ad4b6d80af53ea8e381d71d1749",
  "auditoria/fase-pn14-autorizacion-implementacion-safety-net-caracterizacion.md": "8b861d8a16a71a102155cf1e6c2b8d84dc343cbaf45e43baef480f5e0ed3b6df",
  "auditoria/fase-pn14-slice1-safety-net-caracterizacion.md": "c7b3dc12b77d3146181124d11f55cfde836ca8d883117a6b2548412934c14efe",
  "auditoria/fase-pn14-slice2-autorizacion-orden-snapshot-inmutable.md": "be4f3348f5918f4640c597899fcb23b1243bfaca01c03e3ed25040f0a9801cbc",
  "auditoria/handoffs/HANDOFF-F2D1-1.md": "96c114ecb042324894987549feef9cf57a67a636f76e923499bb33ead46f11b8",
  "auditoria/handoffs/HANDOFF-F2D2.md": "914d69eac171f895d6e8d62a97d4ebc00330fc2a088bb9daee278860f6ee12c3",
  "auditoria/handoffs/HANDOFF-F2E-ADAPTERS-SNAPSHOT-DESIGN-AUTHORITY-GAP-R1-PROVENANCE-JPA-TX.md": "ff74c850bce03e2ec757d4dde8c3f4a3b39a71c35e219b4a8e7b3ccf629cbb77",
  "auditoria/handoffs/HANDOFF-F2E-ADAPTERS-SNAPSHOT-DESIGN-AUTHORITY-GAP-R1.md": "85c902d45eff21f2d5f8392e54970e7468b25cfbbeab6af229573fc95d198bac",
  "auditoria/handoffs/HANDOFF-F2E-DETECTOR-READ-ONLY-NUCLEO-PURO.md": "a655cd147f0cee1b9f8fc86a3351fe07ba53be2904a7adacdabace2d4c2b55ae",
  "auditoria/handoffs/HANDOFF-F2E-DISENO-ADAPTERS-READ-ONLY-SNAPSHOT-CONSISTENCY.md": "bec439a3c7be004f71186009a8e3198b11ab5c69d32a220d1bc863715b7955a2",
  "auditoria/handoffs/HANDOFF-F2E-IDENTIDAD-DETECTOR-READ-ONLY.md": "3468ec1c1efbc532169a3c379fc262d27bb8f4d81d3d8cad8911934a83ca6330",
  "auditoria/handoffs/HANDOFF-F2E-PREPARACION.md": "2c6c4d3bd5b17be3cf56293edcf4460c5d12fe24adc78bc18529c1321ffbe3a3",
  "auditoria/handoffs/HANDOFF-F2E-R1-RESERVA-READER-JPA-READ-ONLY.md": "3fd71faca4d4c049ad5cb37b52bc6fd512509cf5b696bdc5c28d28cb966af8ef",
  "auditoria/handoffs/HANDOFF-PN13-MATERIALIZACION-AUTORIDAD-PAGOS-NOTIFICACIONES.md": "601d285a23c87b131b4b4946d2f858ad918faea12e492d76f7e9da7acd073e22",
  "auditoria/handoffs/HANDOFF-PN14-SAFETY-NET-CARACTERIZACION.md": "df540a241671b5c1c173a5aaf6d809871e9beb8bd8248d16025ff0984b7f48ab",
  "auditoria/handoffs/HANDOFF-PN14-SLICE2-ORDEN-SNAPSHOT-INMUTABLE.md": "4d7e7803557f75a3d62afe67a757687a63b9c627b0fdbf2580d89feab36fdd7d",
  "auditoria/intervenciones/F2D.1.1-CORRECCION-POST-REVIEW.md": "bd1ec9134c2b46d56580471170f54f9d52fb174bc30e2ea0e33ecb025ba0047d",
  "auditoria/intervenciones/F2D.1.2-AISLAMIENTO-DARK-LAUNCH.md": "d114eaba9701363d791c09a44bf576b25a53aa0dd314980ce9e90f77289d8bc7",
  "auditoria/intervenciones/F2D.2-PREPARACION-INTERVENCION-ORIGINAL.md": "59d09c998deb391a455a12c42fb25fcb7d0dfa1911a6d2ebe4e589d1935b4296",
  "auditoria/intervenciones/F2D.2.1-CORRECCION-IDENTIDAD-CONCURRENCIA.md": "bdd4fd689dc34f3466ec0c02927bd105ed1029f3b8a88b4fe5fc362ac1ddd2eb",
  "auditoria/intervenciones/F2D.2.2-CIERRE-CARRERA-AJUSTE-ID.md": "e7565f5cc442df09b43413daa76d62c8c2718c9bd21ce99386cb7551a3547376",
  "auditoria/orquestacion/GATES.md": "b3137de84aa171dd6f11bf439c79b937e44a52de54a5cb841fd57048678d490b",
  "auditoria/orquestacion/README.md": "21d094499f49a7c7f0878e4c183114c4ff8525446dca5e8db1151a90e7146a7c",
  "auditoria/orquestacion/ROLES.md": "8c19f5981cad4941a34356fecde833168b1754620e167fd323b36c35490dbb50",
  "auditoria/orquestacion/STATE-MACHINE.md": "0641e4725947d1fb2f16cf304255b0b30739090bb810488672503a206f6f13d8",
  "auditoria/orquestacion/WORKFLOW.md": "107857275a38dfb23f094ec35054113b88de404e0ae7100c2bf527071de130d1",
  "auditoria/reviews/F2D.1-REVIEW-AJUSTES-PUNTUALES.md": "ac2e44ffc745b0e01efd8897616d7e3809c46191bd7708a1ecd4d37d7d1bce84",
  "auditoria/reviews/F2D.1.1-RE-REVIEW-POST-CORRECCION.md": "960d1bea4a7206eb83727198771a2065841d51b0e904a5b2833a9dc264d5132e",
  "auditoria/reviews/F2D.1.2-RE-REVIEW-FINAL.md": "bbea8a2c8d199a1f0ad22cdc59a9abb197471fcdd35d8a10ca2cb7deaed83068",
  "auditoria/reviews/F2D.2-REVIEW-DOCUMENTAL.md": "27051de1652d1d625e1610bf3c158ae7ea810a4373316ec904aceb151b5673c8",
  "auditoria/reviews/F2D.2-REVIEW-INTERVENCION.md": "832ed54ee4dc0f21b6d1aeda009cb305e450b3a306d3f82145446a0266846aa3",
  "auditoria/reviews/F2D.2.1-RE-REVIEW.md": "fbc49e530df34ed01f59fbd60f9a37571602251d562b806c6ffc29298fb82936",
  "auditoria/reviews/F2D.2.2-RE-REVIEW-FINAL.md": "af7e008547a45b19a525c6a92ac4344d8c4d28fb734a85179d54f09cc928bd00",
  "auditoria/reviews/F2E-ADAPTERS-SNAPSHOT-AUTHORITY-GAP-R1-DESIGN-REVIEW.md": "9cc0b9da623055b2f02aa42007da7c5ebe9d883a78e159b892df9512f1fb6873",
  "auditoria/reviews/F2E-ADAPTERS-SNAPSHOT-DESIGN-REVIEW.md": "292a5241a4e9ffd383b1b956b80a1be41b49af5cbccf2f3c411713f05188b031",
  "auditoria/reviews/F2E-ADAPTERS-SNAPSHOT-RESIDUAL-AUTHORITY-GAP-R1-PROVENANCE-JPA-TX-DESIGN-REVIEW.md": "ae2ce8a4271271df9d401ffdc3a58dc168ddec9291aa6b4dbdacd88dea8a7315",
  "auditoria/reviews/F2E-DETECTOR-READ-ONLY-NUCLEO-PURO-REVIEW-IMPLEMENTACION.md": "513aa7428f3ea09918a919e603e4859e3f17048c75d2ea253312c59a8b929e41",
  "auditoria/reviews/F2E-IDENTIDAD-DETECTOR-REVIEW-DISENO.md": "ad7e82ab341d463e60107902f88c353e29cc2112a56379489582bd69fdc0cf3c",
  "auditoria/reviews/F2E.1-REVIEW-DISENO-PREPARACION.md": "55495934a19aabc4a36965fcbdf2ebbbf5a7c80eb2cfd739420ac5745dc0c366",
  "auditoria/reviews/HANDOFF-F2E-ADAPTERS-SNAPSHOT-DESIGN-AUTHORITY-GAP-R1-PROVENANCE-JPA-TX-REVIEW.md": "f58468207709f5996e104f38d0220b58883c867c318533d54b26808df3e93496",
  "auditoria/reviews/HANDOFF-F2E-ADAPTERS-SNAPSHOT-DESIGN-AUTHORITY-GAP-R1-REVIEW.md": "a7259410c83759aed2e2e18ccdb3256e6eae2267ee0600b417d0cbf6b5f33700",
  "auditoria/reviews/HANDOFF-F2E-DETECTOR-READ-ONLY-NUCLEO-PURO-REVIEW.md": "210caafac5f0d5138d012e83002860e1dd069a5275ea88f1588c7a23e1115084",
  "auditoria/reviews/HANDOFF-F2E-DISENO-ADAPTERS-READ-ONLY-SNAPSHOT-CONSISTENCY-REVIEW.md": "82dba97a7a45f54a36479484ed9ce1500e810c05663086ec009f6b1f6f5c912e",
  "auditoria/reviews/HANDOFF-F2E-IDENTIDAD-DETECTOR-READ-ONLY-REVIEW.md": "1848db9ae03640c30d381cd0cdb701ef9d24277038abe746d3a5bee9f96e4a18",
  "auditoria/reviews/HANDOFF-F2E-PREPARACION-REVIEW-DOCUMENTAL.md": "2a317c57b0d272d67cd6aedec01ef0a9ee7242911804abdef6ba5b7cc1b60ea3",
  "auditoria/reviews/HANDOFF-F2E-R1-RESERVA-READER-JPA-READ-ONLY-REVIEW.md": "a4a3ff1c9f1aaaad4a6d36233a4a4be0b407f0d899fc22f2d1973a10ef19aa61",
  "auditoria/reviews/HANDOFF-PN13-MATERIALIZACION-AUTORIDAD-PAGOS-NOTIFICACIONES-REVIEW.md": "26a0e67588f7a9e5bd13c79aa9006a833d63834cf3cf1a1a19467194e27df5f5",
  "auditoria/reviews/PN13-R1.2-REVIEW-MATERIALIZACION-AUTORIDAD-PAGOS-NOTIFICACIONES.md": "da21981cbb4d0925fc7732e645580dd22e5e369938f6b165a0e7009d0eb4510b",
  "auditoria/reviews/PN13-REVIEW-CIERRE-PUBLICACION-AUTORIDAD-PAGOS-NOTIFICACIONES.md": "675529f53540a86994b82c041203efb8b1d3b5003c501a74e88e900b9f78e901",
  "auditoria/reviews/PN13-REVIEW-MATERIALIZACION-AUTORIDAD-PAGOS-NOTIFICACIONES.md": "cda17b50e562059382f42c46ebe827fedf6519682f95259860be4d0a48215477",
  "auditoria/reviews/PN13-REVIEW-PUBLICACION-AUTORIDAD-PAGOS-NOTIFICACIONES.md": "96378bcc74da86f656d5eaf0a08042e246f6ce6f87bee3e77873679228c17a1f",
  "auditoria/reviews/PN14-MANIFEST-ENTRADA-LOCAL-SAFETY-NET-CARACTERIZACION.md": "3d9f96251e13fb43ed2766c63223521390a5569f0a0d544fba9289bd5f4e6dc2",
  "auditoria/reviews/PN14-REVIEW-AUTORIZACION-SAFETY-NET-CARACTERIZACION.md": "377f7c1bef17be075d09cf2a5bf422cdab47f11ce93cdf5ec18aa1bd3d03eec1",
  "auditoria/reviews/PN14-SLICE1-REVIEW-CIERRE-PUBLICACION-SAFETY-NET-CARACTERIZACION.md": "fcce395459efd66cbfdb42dba4513fd3af5f4eecdc9cabb878238b06e319554e",
  "auditoria/reviews/PN14-SLICE1-REVIEW-PUBLICACION-SAFETY-NET-CARACTERIZACION.md": "bdceb0a0418e8895a2b8276a939a264e51d88cba84a5f701cd7739aa67954185",
  "auditoria/reviews/PN14-SLICE1-REVIEW-TECNICO-SAFETY-NET-CARACTERIZACION.md": "b7c546a26956995cc7754dfd0701f65a839ea9aab5e7d9837c58099502d42ede",
  "auditoria/reviews/PN14-SLICE2-MANIFEST-ENTRADA-LOCAL-ORDEN-SNAPSHOT-INMUTABLE.md": "ffea6f1861a0f595336d3376af2f259b26837dd75c3c67d7c2ebbe7a4cd0622b",
  "auditoria/reviews/PN14-SLICE2-REVIEW-AUTORIZACION-ORDEN-SNAPSHOT-INMUTABLE.md": "1477ba79459681a195cccad310540952f8f1d84ab1069a87154de340a279c3d3",
  "docker-compose.yml": "6fb250325b99acf75d11bcfd9470cdeb2ef0d601f46f8cc4b91eb94bc02e9e79",
  "mvnw": "cae96cef89ebea3531221f4ae17c23cf8edf67d00eae8306d4186ae1bbed4d02",
  "mvnw.cmd": "46eedb8419bd14fe70d5bb2916d7b6f51806e51b39d5b76a42610384ca929c1c",
  "pom.xml": "904b68767ace499c7d842a5052d22f1ad93640b06cf3233ee0adf171dc949347",
  "src/main/java/com/feelingpilates/FeelingpilatesApplication.java": "ce45c75c3fadc07930792cdc5dad3500a247b57372162089a9c32b5ab4e08782",
  "src/main/java/com/feelingpilates/auth/AuthController.java": "5d294da039891718eb957e5e6b84c1a518d4f3c880dbb4c05350881c561c060d",
  "src/main/java/com/feelingpilates/auth/AuthService.java": "e9c7b719b6c0005fcfebd263ab0f0c7d4962bbd7bf6eb99cc6b9f59a81bc0f41",
  "src/main/java/com/feelingpilates/auth/GoogleLoginDisabledException.java": "921eaeab246461e78c0d3544ce3d58d076fb44232ede2a3224d1d34bc6e03d7f",
  "src/main/java/com/feelingpilates/auth/GoogleTokenVerifier.java": "42e5c9b6d0e6f68b6a1c440ef966b3c7acdb2cd75f13bdf413c23b4ed79e0009",
  "src/main/java/com/feelingpilates/auth/dto/CompletarInvitacionRequest.java": "77726a5c6af84da1b3ed5f450ce31721bfbebd5cdad127887e0f2db2939b84c9",
  "src/main/java/com/feelingpilates/auth/dto/GoogleTokenRequest.java": "3d3050ece86d455c24288a1a3de8dc2c1a5990f72ec299efa954a57cb88b4e03",
  "src/main/java/com/feelingpilates/auth/dto/InvitacionInfoResponse.java": "35bb815004f8a1ae9b2970e3a5c711350f879edf5fe1daddb0b5e89bdb8dddce",
  "src/main/java/com/feelingpilates/auth/dto/LoginRequest.java": "30b2870bd3b0bd6ca74fbc9267ff4b175211d1c431d9c1667e049bfb474574f0",
  "src/main/java/com/feelingpilates/auth/dto/RegistroRequest.java": "b68717c73d1739959017410ec7d6bbab826a13ecb59e1eebc25f6f4fd1813071",
  "src/main/java/com/feelingpilates/auth/dto/TokenResponse.java": "b577f598c25f774f19849afcb2d78a628454e2deea7ca71f5a99592d453f5e6e",
  "src/main/java/com/feelingpilates/calendario/controlador/EspecialidadInstructorController.java": "c023d39ca3edb47d1809ce00185e16b0c5e8a0b787c9db3447fa9063ca59bb14",
  "src/main/java/com/feelingpilates/calendario/controlador/ReservaController.java": "0dbed16e3f9e8e58ad77b25e3d4047b785515393ecf77f29d6ad8485cea60dc3",
  "src/main/java/com/feelingpilates/calendario/controlador/TurnoInstructorController.java": "e33df71d53905116d213c243dac4fdeb1542792e2cf5a291f7b7caab41310d5f",
  "src/main/java/com/feelingpilates/calendario/dto/ActualizarTurnoRequest.java": "eb62ab9412ec6bde2b40f90ce38d8b62a4ebfc671e353c870660df572eeabe44",
  "src/main/java/com/feelingpilates/calendario/dto/AsignacionInstructorRequest.java": "74b85fde4ddadc1bffbe61c8e020d03e25371d158582ca20b9ac8c2eb7aa968a",
  "src/main/java/com/feelingpilates/calendario/dto/EspecialidadResponse.java": "32f8ed7f1e3a34741e7fb3e991ed90fcffdd7757e55e2f66679f564f6d6ca64b",
  "src/main/java/com/feelingpilates/calendario/dto/EspecialidadesRequest.java": "51fb656aa62a670dd08c4dcda313b352addd80530bc5c60a665a4b5dafd5fbbb",
  "src/main/java/com/feelingpilates/calendario/dto/ReservaRequest.java": "cccc6ed55daf4e7dc005b78802c8e4cfb4297e4b8b253db00c06f7ad24f8967d",
  "src/main/java/com/feelingpilates/calendario/dto/ReservaResponse.java": "93579b164b9d1d160113550459d9b29b2be06e4cc4f080bb123ef570e284e647",
  "src/main/java/com/feelingpilates/calendario/dto/TurnoInstructorRequest.java": "7ea569478f60ec401f60e399c7b92e433e679a3a7204f2a1f52c902dcc07620c",
  "src/main/java/com/feelingpilates/calendario/dto/TurnoInstructorResponse.java": "bb4aee2a8574582e86afaea0e0b1d7e06919cc090631304e915892fe062ac5c4",
  "src/main/java/com/feelingpilates/calendario/entidad/Reserva.java": "e0ff7f3cfee2ee152ae0e0f9bc815a929224c6d0fc7616736dfe36e612ecab2a",
  "src/main/java/com/feelingpilates/calendario/entidad/TurnoInstructor.java": "51b31fc611bbc36794bd4a6685a339eceada545ef90608fbe03844f88706fb2d",
  "src/main/java/com/feelingpilates/calendario/entidad/TurnoInstructorAsignacion.java": "9abe42c781e5039183ac74b0b697cea0ce33309d928d560bbc1807c6084da724",
  "src/main/java/com/feelingpilates/calendario/repositorio/ReservaRepository.java": "ac5804deed6634182c2b2164e1b03a1ee1a9167043f9f80f41a7dd10aa12cce9",
  "src/main/java/com/feelingpilates/calendario/repositorio/TurnoInstructorAsignacionRepository.java": "5c7fc89c3e8f63656ee06b950c02f8ca308c8264b98dd6bf164ca085aa2a67be",
  "src/main/java/com/feelingpilates/calendario/repositorio/TurnoInstructorRepository.java": "33a9d4bfb858e61406d63e18f736c3852bc436723834879ab1b363911f867d3e",
  "src/main/java/com/feelingpilates/calendario/servicio/EspecialidadInstructorService.java": "7c23d872bc45a26dd11c3726642cbc8da5ea2e3ec80e8d641cc5e39729ac83a6",
  "src/main/java/com/feelingpilates/calendario/servicio/ImpactoPuntualEnExcepcionHorario.java": "039bb161fa8681439d2574d8c4e6b087822ac310525294ce92119e2c76059734",
  "src/main/java/com/feelingpilates/calendario/servicio/ImpactoTurnosRecurrentesEnHorario.java": "7a464b3afcd76f36c55b9d990ffa67b17ea728ba75814b235c36cdc8748eb100",
  "src/main/java/com/feelingpilates/calendario/servicio/ReservaService.java": "73476b7ccbcaa4e4ab847af4bf93aaa086ad4150b3f1b8b64e11e08df9ae5f51",
  "src/main/java/com/feelingpilates/calendario/servicio/TurnoInstructorService.java": "062074537a230c849b0feb9e5a76dc4a45893f367dee349914a6d97603791871",
  "src/main/java/com/feelingpilates/comun/entidad/EntidadBase.java": "4b5561c6f448edf2a4b8b3d1e33d6763b807b556fbf3248c286252bd3fb1d1b2",
  "src/main/java/com/feelingpilates/config/OpenApiConfig.java": "46a8fbd919825d090272ceaac846d893916cf02da3f054995f37c06eca27db31",
  "src/main/java/com/feelingpilates/config/RelojConfig.java": "28ceba595899c42ebfdb0717b4ec6614fe008d071da4c20959c4c1b92423cc7f",
  "src/main/java/com/feelingpilates/exception/CodigoErrorExtractor.java": "2a6362b362f86779501d2ce5e99e7717d24ef08c6ab5e595f83bdc03ecf026bc",
  "src/main/java/com/feelingpilates/exception/ConflictException.java": "7949dd22778596b30d5badf648039c8bfe4955bcd0981b07aabc7a7b5ce9a2e4",
  "src/main/java/com/feelingpilates/exception/ErrorResponse.java": "61f8f91b6c699741d25ae30bf7de6b1d035d7a97ea00fb397c0c9914cec58e51",
  "src/main/java/com/feelingpilates/exception/GlobalExceptionHandler.java": "5f04be2a3c8f5c71fa3d952691d71bd09a1baca89026827d08a34e3b2e738707",
  "src/main/java/com/feelingpilates/exception/ResourceNotFoundException.java": "6bb391695c2a5a88dca6eb79f0c1ab9c3f2f806b100eab4b73681f466e6dbe22",
  "src/main/java/com/feelingpilates/exception/ValidacionException.java": "d3bdacc8903855fc9040ceb579484679be1ecc9b945706a37e1672279e12476e",
  "src/main/java/com/feelingpilates/notificaciones/EmailService.java": "05df60b8ba3a039dca822b94265a0746bc250d0af4a156b6da2b64a10588fbce",
  "src/main/java/com/feelingpilates/notificaciones/EmailServiceConsola.java": "25e537faa297ecf759127722b2161fa6c5486b0ef8d6c8647943187acbbaa492",
  "src/main/java/com/feelingpilates/pagos/StripeConfig.java": "b8cc75eb9deb84b8f45519252dc5cc503d2b205b1503f902433e79fd0283e9cf",
  "src/main/java/com/feelingpilates/pagos/controlador/PagoController.java": "10d4be15ee787cc0f4509eac0ef5fa9cc12bf5679a258f96e3e929000544bfe8",
  "src/main/java/com/feelingpilates/pagos/controlador/PaqueteController.java": "540278e38d6369fccb797657760d25be2a1501d773a5ef301df71776b4cb7e8b",
  "src/main/java/com/feelingpilates/pagos/controlador/PaqueteGestionController.java": "426f6fc0b515e2dc02093e1b36391edb6fc97ecc7bfd90eaeced93dd146ea799",
  "src/main/java/com/feelingpilates/pagos/controlador/VentaController.java": "da0bcc5f1b64cb5898ff63dbad90e85a79d2cb506af04dec6fb49a0a62af4a5e",
  "src/main/java/com/feelingpilates/pagos/dto/ActividadPaqueteRequest.java": "63992b2c649db4163ba7793f66541ce42695312a5a65180d7fd825fbc928a989",
  "src/main/java/com/feelingpilates/pagos/dto/ActividadPaqueteResponse.java": "821d3af0e3880b53fe8967fc42735549ddc49689967913ccd13af821fb1fb4a5",
  "src/main/java/com/feelingpilates/pagos/dto/ActualizarPaqueteRequest.java": "9ca2e68677d3f40b882f1770b25bf1d3bcb65641a6d32af7c5e80910bd472634",
  "src/main/java/com/feelingpilates/pagos/dto/CambiarEstadoVentaRequest.java": "c4f72a73c7f9c7a2fef7c2782d973d79178c2b127595360a3efe66f485028f5a",
  "src/main/java/com/feelingpilates/pagos/dto/CompraResponse.java": "25ad5d56d7ab4a5e7d10903d2635e91c16c56f9fd0353d23a0878020b5c7c1a3",
  "src/main/java/com/feelingpilates/pagos/dto/CrearPagoRequest.java": "ba958dfe6d4b2c7f5a0da526d6296d1832baf79137ac875287f51d247d81379c",
  "src/main/java/com/feelingpilates/pagos/dto/CrearPagoResponse.java": "3363344535f9a411c69548131fed540a12e9cd7d66f5e47e4f440367b7e280c1",
  "src/main/java/com/feelingpilates/pagos/dto/CrearPaqueteRequest.java": "d16e825e27f2e3fe2b129a7bec48088de363c178a82f9fbeb29e9749100a8036",
  "src/main/java/com/feelingpilates/pagos/dto/ItemCarritoRequest.java": "1ca507bb933a246a8dc22b8e089188991aa6484330d7f82076793ec5217973b6",
  "src/main/java/com/feelingpilates/pagos/dto/PaqueteActivoResponse.java": "6a207cf2db354a0203cb90fc02ba943c5e3b32b7e5295920e8bed2f989d980d4",
  "src/main/java/com/feelingpilates/pagos/dto/PaqueteGestionResponse.java": "b8a97ad5357f0b0db69fbdf445a460efb7f48b07469a8260e1fd23971e4aa30a",
  "src/main/java/com/feelingpilates/pagos/dto/PaqueteResponse.java": "c7c9f2333c294231b055c5f25964a7ad565d3e6955fd9ab9a318c09956b52d29",
  "src/main/java/com/feelingpilates/pagos/dto/RegistrarVentaCarritoRequest.java": "3d416f06a5d3d21095428415e1f50ea0b914cd3dd091f0f06db5678e5f7d8e45",
  "src/main/java/com/feelingpilates/pagos/dto/RegistrarVentaRequest.java": "89a6a158b4fb7e5e86b60a04f0894b5f19a540ecb66ab58c81939802d798923c",
  "src/main/java/com/feelingpilates/pagos/dto/SedeVentaResponse.java": "b0d1c785c7ce5e4d0bb732886f5a813f1f07efe7a295dabee3dbed37c134d006",
  "src/main/java/com/feelingpilates/pagos/dto/VentaCarritoResponse.java": "a41bada5044f4ed97bbfce68c05851feec18a63388e64f5c032a43745c56d67c",
  "src/main/java/com/feelingpilates/pagos/dto/VentaResponse.java": "7b993d203b2b0bbc1ceff4b561425303bbd7fb1fb5e490c571dcd4d720e8169f",
  "src/main/java/com/feelingpilates/pagos/entidad/Compra.java": "28cd47b610e41217f61f8240e81a555dc35e330c2b133413d486645233231d0b",
  "src/main/java/com/feelingpilates/pagos/entidad/Paquete.java": "4a09f9cec8367919a9c5c21c9cc12743946df8a61bf553ad6f52b80a553b6f0e",
  "src/main/java/com/feelingpilates/pagos/entidad/PaqueteActividad.java": "42a9c1f4f593c65e4e133171aea1824368e50279144c6e9727123403a1b18735",
  "src/main/java/com/feelingpilates/pagos/repositorio/CompraRepository.java": "dcd28774a8d95b075175280b940ec945fbc3f013ab6c450d83ddc1e8551979cf",
  "src/main/java/com/feelingpilates/pagos/repositorio/PaqueteRepository.java": "ba0b278edc6e22dca2c2b54a8622d3592fb7b481b1dec29a610c2513e0d427a6",
  "src/main/java/com/feelingpilates/pagos/servicio/PagoService.java": "fe62d2ba9e6d8cb57e953ace83b2a607aa9960cd61c216a5f07b403446998686",
  "src/main/java/com/feelingpilates/pagos/servicio/PaqueteGestionService.java": "36e57c522e5e2d098376c79a9814905be87e1bd56c97461b20626532d5f87f45",
  "src/main/java/com/feelingpilates/pagos/servicio/VentaService.java": "42af8081d32d74c5425ba2406b221502de3cdfe053e64d177314431940959e0f",
  "src/main/java/com/feelingpilates/pagos/ventas/aplicacion/BackfillOrdenSnapshot.java": "b24e1b829431e5a91f7ca32af5d4b690b52c45c95ed75530d79eb08c729d4a0f",
  "src/main/java/com/feelingpilates/pagos/ventas/aplicacion/CompraHistoricaSnapshot.java": "38e28e926cad743026284f466ac04c66e4d6ed55c44cf883f04eb99089e6a44f",
  "src/main/java/com/feelingpilates/pagos/ventas/aplicacion/CongelarOrdenSnapshot.java": "6ab0f95187a8adbedf988c4b01034b69774a4ee21d21dfeb4ff23a8ad33148b7",
  "src/main/java/com/feelingpilates/pagos/ventas/aplicacion/ConsultaHistoricaSnapshot.java": "2431565f7fb7b308fe8eede3eb6bb7fc36edbf961ea43f4408bfbfe3c1608709",
  "src/main/java/com/feelingpilates/pagos/ventas/aplicacion/FuenteHistoricaCompra.java": "c86e88aa4f279c12d7b176b6c816b309c821426a635e2bf5e837634450a156b7",
  "src/main/java/com/feelingpilates/pagos/ventas/aplicacion/InformeBackfillSnapshot.java": "25c6d9da5b3810bf46f2ff30e8777b61f5404221188c59a0a0a624cd448d8e11",
  "src/main/java/com/feelingpilates/pagos/ventas/aplicacion/RepositorioOrdenSnapshot.java": "ffbc35925dc241ed098b6a61c6539ac00d538ba34d5080f45ac92576af523064",
  "src/main/java/com/feelingpilates/pagos/ventas/dominio/Compra.java": "e9899d11aec2b2738ff1ce739a76cd66f0dcd8b49545bef669837d0bfff0de96",
  "src/main/java/com/feelingpilates/pagos/ventas/dominio/CompraComponenteSnapshot.java": "5ae0ad2d7e73d997942138858d1496b20bd23184115828fabc9a7618ee0cfd73",
  "src/main/java/com/feelingpilates/pagos/ventas/dominio/ContenidoSnapshotCanonico.java": "483db6f1f7cf4cfd3da71453373c393b22d58aeee408e6c99c439e30e478a6ac",
  "src/main/java/com/feelingpilates/pagos/ventas/dominio/ImporteMonetario.java": "e5cbd912a040ed15219e5424b45eedb7588ca79e2abbfbc20c36d0545a46b2d0",
  "src/main/java/com/feelingpilates/pagos/ventas/dominio/OrdenVenta.java": "e5122cbaf04d36dbff241a51431346e1944c40937587dc9d5520f76cee2d49fe",
  "src/main/java/com/feelingpilates/pagos/ventas/dominio/PoliticaComercialSnapshot.java": "c16184239f82777bfec7056b68640bc6129298d14789ca229696ad475616e58c",
  "src/main/java/com/feelingpilates/pagos/ventas/dominio/ProvenienciaSnapshot.java": "695373e25e825e71d4c1dafe07c231f2b936b459420d5ba78ed926908c7823b5",
  "src/main/java/com/feelingpilates/pagos/ventas/infraestructura/ConsultaHistoricaSnapshotJdbcAdapter.java": "df438e2c89a7201d84a2103b9edb2631a556e6c87d669741786da46ea404e3ce",
  "src/main/java/com/feelingpilates/pagos/ventas/infraestructura/FuenteHistoricaCompraJdbcAdapter.java": "89223c356530395a541582501a827ab5431dbe4480d4dd26e99021140c0f083b",
  "src/main/java/com/feelingpilates/pagos/ventas/infraestructura/InformeBackfillJdbcAdapter.java": "8458d295f3ae99518be95035a9317d24c18ff8a954abf90521e3443908164a3a",
  "src/main/java/com/feelingpilates/pagos/ventas/infraestructura/OrdenSnapshotJdbcAdapter.java": "2959e7c12fbdbaee09d1b1ea14dfd3d271e5b219026b2a27bf3b2a86dee82b21",
  "src/main/java/com/feelingpilates/programacion/dominio/OcurrenciaEfectiva.java": "26ec805223fb304e818d63d920924c4ce2e66419e9e00a7d0bc0835d02841d56",
  "src/main/java/com/feelingpilates/programacion/dominio/OcurrenciaNominal.java": "eacbe71b4bfabc80b7e155ded6ac0a3426c748a4383d6e63041bc6e21a054379",
  "src/main/java/com/feelingpilates/programacion/dominio/ProgramacionInvarianteException.java": "58ec3f758940d0f3c915bb46d822dca480f1dad4e02444019ddac63bf756fffd",
  "src/main/java/com/feelingpilates/programacion/dominio/ReferenciaOcurrencia.java": "611d34112c796097c28835b488769747146be65f8f1a33f1485fdec7e4c5b82b",
  "src/main/java/com/feelingpilates/programacion/entidad/AjusteProgramacionFecha.java": "8b56681e034897bf94540d71a147a79844e9807f24908d9431e74d0b748b2f66",
  "src/main/java/com/feelingpilates/programacion/entidad/Asignacion.java": "df94c5b78b63cfefdec1d382ffb0b1a422cb03f198d17d02a145ece7c4514a9c",
  "src/main/java/com/feelingpilates/programacion/entidad/BloqueProgramacion.java": "d74931a06f5bbb8f9914ee0952fc1ace970a4797a2ea7e8e4f0551d31ff6e5f4",
  "src/main/java/com/feelingpilates/programacion/repositorio/AjusteProgramacionFechaRepository.java": "b445f3a4fd90568ac55089dff711d933f54e97f408d9ec8acd0769e16ff7cfe2",
  "src/main/java/com/feelingpilates/programacion/repositorio/AsignacionRepository.java": "1231b32b9cd2951ace1fa5c09dc18f0732134f58de498a9f3bd8014764c65db2",
  "src/main/java/com/feelingpilates/programacion/repositorio/BloqueProgramacionRepository.java": "3d352580a0e1411ae3faaa9870003109c100e1cd3ad8d3ff8cf0100b3c610adc",
  "src/main/java/com/feelingpilates/programacion/servicio/AjusteProgramacionFechaPersistence.java": "6707a0bbf804a46fcabce0d9b93da40cf41de1c7fb0871b0153974563db77608",
  "src/main/java/com/feelingpilates/programacion/servicio/AjusteProgramacionFechaService.java": "924bf3d9a47ceef7b3a36b50dd253eabb379736ee8024c38baddceb0b7865e84",
  "src/main/java/com/feelingpilates/programacion/servicio/AplicadorAjustesProgramacion.java": "48bb9c3e5de6ff60c42a169741017cd72720f24dbc016df38c83eca12548723b",
  "src/main/java/com/feelingpilates/programacion/servicio/BloqueProgramacionService.java": "88401fa65d46effae9204be90f9fec5de3d4fd80aedad7bee1766448e0f03f88",
  "src/main/java/com/feelingpilates/programacion/servicio/ConflictoAjusteProgramacionException.java": "bb88c219702a132c7d5f0810ae3fbb16752520c81b202a5b5972ebebbdae9532",
  "src/main/java/com/feelingpilates/programacion/servicio/ConflictoAjusteProgramacionTranslator.java": "4c2475ec4d64a28e5803301efae35ebb9a3c0aa93c1b19f7ac2086218a77d12d",
  "src/main/java/com/feelingpilates/programacion/servicio/ImpactoBloquesEnHorario.java": "44a9b27988bf3aecf022142455fac29322405ba7ed1efacd942820b05e69eb5d",
  "src/main/java/com/feelingpilates/programacion/servicio/ProgramacionDiagnostico.java": "fc138f968dac91b48c9fd4dd1fe6e9b74cc192d62794ca0c0c35b2977031ffd6",
  "src/main/java/com/feelingpilates/programacion/servicio/ProgramacionDiagnosticoSlf4j.java": "b9b3ed7c9bc68c555aeb855bfc855858ee0f9bd06abe1f7b6100f5d07307868f",
  "src/main/java/com/feelingpilates/programacion/servicio/ProgramacionEfectiva.java": "275df044f2f7dc006ccc8da3c3162ed48de165dd23901bd729235b7275ab752a",
  "src/main/java/com/feelingpilates/programacion/servicio/ProgramacionErrores.java": "82e43df533785c7c8b28c0465927e9b2d97fab302729a19d5934842c268449a5",
  "src/main/java/com/feelingpilates/programacion/servicio/ProgramacionNominal.java": "77b69e8b8fdcd95a327c99f4981346c5f717fa317f46bca52fc74630c2bf9e75",
  "src/main/java/com/feelingpilates/programacion/servicio/ProgramacionPolicyA.java": "ea3ab95a2c0afeeee59b87f51dd41309c989304587d132b7f7b91fd50df55307",
  "src/main/java/com/feelingpilates/programacion/servicio/ProgramacionValidador.java": "99548ec93254874cb5ea1c184ab38aab0ec09ae5d081a14eb9bf727a11d17edf",
  "src/main/java/com/feelingpilates/seguridad/AutorizadorSalon.java": "ace316cc5f9012976b56608dda40e0faa0d8462358b4f20a458e52e1de5710ab",
  "src/main/java/com/feelingpilates/seguridad/ContextoAutenticacionService.java": "fdd43b4dd1dd8d9ea3a3d560e5eb7c66e306768cbd37cb903c6c3d30769947fc",
  "src/main/java/com/feelingpilates/seguridad/JwtAuthFilter.java": "a3ad8a5dcec23beb2d694d041611197f32bf5b090c4bd22d7c3bc1503b5dd25a",
  "src/main/java/com/feelingpilates/seguridad/JwtService.java": "27532723264a858539074bcf0c70e2c53d6f942988244668b8e504edd415af7c",
  "src/main/java/com/feelingpilates/seguridad/SecurityConfig.java": "530aff488fea0f15628e523b5e9fdc2fcee5c2fa679fcd520073117d94c2786b",
  "src/main/java/com/feelingpilates/seguridad/UsuarioAutenticado.java": "af281255a69f0194068c6bdd501ccee99c43568552286c17074caabcd945d7a8",
  "src/main/java/com/feelingpilates/transicion/programacion/detector/CandidateEvidenceGenerator.java": "200c756d133797c91825e75bb83f67a6816ba775636ed8bb9d408228fb54e4b1",
  "src/main/java/com/feelingpilates/transicion/programacion/detector/CandidateEvidenceItem.java": "09bb9e5167db7215d5bd9a5d7216c805188abf6a85e7c3acc698345566ca68f4",
  "src/main/java/com/feelingpilates/transicion/programacion/detector/CandidateGenerationResult.java": "c4fbcd9e6be2ebd657c40b67fc2e0e5a268bdbfefe6f4dc465ff7faca31b1a2c",
  "src/main/java/com/feelingpilates/transicion/programacion/detector/ClassificationContext.java": "ac96d09dacf2c6ca154599e7419d58f63dc5514af24dfd029b733c8981405a60",
  "src/main/java/com/feelingpilates/transicion/programacion/detector/DetectorCandidate.java": "e290b93622349d89d20846af5db8569baacb502bae5737b76a8d2cc676a9e24a",
  "src/main/java/com/feelingpilates/transicion/programacion/detector/DetectorClassifier.java": "9d9ca605f5d8b5e6c954d3b7ee79c21091bd53d5302f1bc7523dc237f837c586",
  "src/main/java/com/feelingpilates/transicion/programacion/detector/DetectorEvaluationRequest.java": "f0cca9293b5882a5e32fbc6909b2f499b8a52089735720743e3c82b4ed74e774",
  "src/main/java/com/feelingpilates/transicion/programacion/detector/DetectorInputInvalidException.java": "07012791b58315c5f4e29abe77e0d87434e110e32e6ced9631216a729c820571",
  "src/main/java/com/feelingpilates/transicion/programacion/detector/DetectorResult.java": "7beeed3a8eeb9583c923f6f00a489b9c73ae8aea844556deb2e1e5e38f1cef7c",
  "src/main/java/com/feelingpilates/transicion/programacion/detector/DetectorValidation.java": "4ba05ba4af1a9820d55ec745a55b542bf63ebdb2d29a67bd740e94bc211d94c9",
  "src/main/java/com/feelingpilates/transicion/programacion/detector/DetectorVocabulary.java": "4123e78b89872daeb0d9fb03e8d49e5e732041f828e14b13f36fb187546d11f6",
  "src/main/java/com/feelingpilates/transicion/programacion/detector/EvidenceProvenance.java": "ba4312f10c96b1268e45e75895f3c818b9ab03afa7df22a09989785a7738bf83",
  "src/main/java/com/feelingpilates/transicion/programacion/detector/F2DAuthorityGuard.java": "fd178edda27b75dac42f6120c8a7d661e44d283becf25ed4a431e87e17e820be",
  "src/main/java/com/feelingpilates/transicion/programacion/detector/F2DCompatibilityResult.java": "39596944ec86761e58c4164db1c80dcdcc97638f8b8d74f7bce74c3b2f417e8d",
  "src/main/java/com/feelingpilates/transicion/programacion/detector/GenericSourceSnapshot.java": "36de8fe63df709e6213e0e2b9d2fef9b05d9f66afee768596ffc8377bc75348a",
  "src/main/java/com/feelingpilates/transicion/programacion/detector/HistoricalProgrammingTargetSnapshot.java": "1ca4586d116f0568dffc717667e5dddae8562947d344de5944ef3c5bb3a69d84",
  "src/main/java/com/feelingpilates/transicion/programacion/detector/ProgrammingCandidateSnapshot.java": "af5adc2977a6936301452f9d461b8c54123e0a4fe1ba5943ecfecce57e6275f7",
  "src/main/java/com/feelingpilates/transicion/programacion/detector/ReservationSourceSnapshot.java": "7856887d858fd7658eda6c68f54bc995d0481fd4257cb9417265c90cf144c293",
  "src/main/java/com/feelingpilates/transicion/programacion/detector/ReservedSubinterval.java": "3ea117a444d4cd169a7d6e88769245aef6f7e9bb1d76874bb35d74762b724c1f",
  "src/main/java/com/feelingpilates/transicion/programacion/detector/SemanticHash.java": "fc92a2a1f489ee7a69601f33764e86d1fda1a4220f82c21081e4a8f5e8b218dd",
  "src/main/java/com/feelingpilates/transicion/programacion/detector/SourceSnapshot.java": "eedd2c098e2adf1b6d90e2bfabce4227ad434d23b1f272376b66edaccd5a79fa",
  "src/main/java/com/feelingpilates/ubicaciones/controlador/ActividadRecursoController.java": "a8e32dce32810ce2ecb5954b217f74b71d5d78128ec3c69c02377de046da0ecb",
  "src/main/java/com/feelingpilates/ubicaciones/controlador/SalonController.java": "95f0b7f5070a422908da6862b3648d1f05086aca697ece24d268b5fd670a52f8",
  "src/main/java/com/feelingpilates/ubicaciones/controlador/SalonHorarioExcepcionController.java": "a002dd514bfbd0c4d4a2825bb21fd4de1f2b22f8704f69de946393341e429cb4",
  "src/main/java/com/feelingpilates/ubicaciones/controlador/SalonHorarioOperacionController.java": "9bfc794dbb1156f579e11346d51f71dbd7049181161faf451e8288981b7299bb",
  "src/main/java/com/feelingpilates/ubicaciones/controlador/TipoActividadController.java": "c065761cf8ac0c4044d2bb06d93d4c682e1eb8d5c17fefaac8ab052093abd980",
  "src/main/java/com/feelingpilates/ubicaciones/controlador/TipoRecursoController.java": "3c7a3cfd06272dc5c65586a1129773491e343623c59c5f5292f8b33817d587b5",
  "src/main/java/com/feelingpilates/ubicaciones/controlador/UbicacionController.java": "4de4a95d4efdc65fb767684efd2784a9b80e33a146f945cf7aa049e3380db36d",
  "src/main/java/com/feelingpilates/ubicaciones/dominio/CambioExcepcionHorario.java": "099c01d31110531cabde19d5193ada4f6d765c4ec581a13cc41dca9fec92ab27",
  "src/main/java/com/feelingpilates/ubicaciones/dominio/CambioHorarioOperacion.java": "b5dd831f58700bd26f62baad34cdaf6d589c6d9969315b2f52cfb6a9f71b4d2b",
  "src/main/java/com/feelingpilates/ubicaciones/dominio/CoberturaVigencia.java": "becf8209f388911f6fbf147cfade494c72776c1d68c919e7a69ed0c1b2633539",
  "src/main/java/com/feelingpilates/ubicaciones/dominio/ConflictoProgramacion.java": "638487b31a53257f48a55eba915cc628a925d68ca685279529e5e2e0a723cb64",
  "src/main/java/com/feelingpilates/ubicaciones/dominio/ConflictoProgramacionPuntual.java": "acc25f526eef0f7b34404b5a8a47ded058324dc2d179d268d0d74c302f72186d",
  "src/main/java/com/feelingpilates/ubicaciones/dominio/DiaSemanaOperacion.java": "82e55c361a3bb7a0db017949a2ca720862df63e3b6d15b93b7b3667ecb7de91f",
  "src/main/java/com/feelingpilates/ubicaciones/dominio/HorarioEfectivo.java": "4da178437797f072784a4237eafcb32538980940e5ad17b5744c7898fee99cbe",
  "src/main/java/com/feelingpilates/ubicaciones/dominio/RangoVigencia.java": "cbb81f643dd588d9b4780009335c5abaeea10363acfc5fe726f68c07df8eb5af",
  "src/main/java/com/feelingpilates/ubicaciones/dominio/ValidadorImpactoCambioHorarioOperacion.java": "3cbfebe6e84881df152f164cf0886f54603a18452c62bf714e33fec3a6d2aba4",
  "src/main/java/com/feelingpilates/ubicaciones/dominio/ValidadorImpactoExcepcionHorario.java": "9b25af23a322e5477189f88a1084736a4053f7bc911d62f06c6ecd3ff17222c0",
  "src/main/java/com/feelingpilates/ubicaciones/dto/ActividadRecursoRequest.java": "42195e7efac33f4ced92a7af6c7c73017920c60eea8feda6f04d2c0b58641dac",
  "src/main/java/com/feelingpilates/ubicaciones/dto/ActividadRecursoResponse.java": "5242faa3790ac5fbba53dab5ecee60eb7dc644b9b74643a57f5c25d00e451bdf",
  "src/main/java/com/feelingpilates/ubicaciones/dto/CatalogoItemRequest.java": "f7801673ffff0585ab2724cca2d77e61f2e881c8cb99e97c13492b5e0f0f3ee8",
  "src/main/java/com/feelingpilates/ubicaciones/dto/CerrarHorarioSalonRequest.java": "1b5f212fd2b3083ba95e908111d51ee90f04204779062d8475f64ecaa5673b64",
  "src/main/java/com/feelingpilates/ubicaciones/dto/EstadoResponse.java": "cf2daa0675a5a3eb786ddeb298e295fd9c122f35ab70473a90866585c04cc2da",
  "src/main/java/com/feelingpilates/ubicaciones/dto/GuardarExcepcionSalonPorFechaRequest.java": "23afa0ee27aa8f533bce1794cea7b47254f8e9697f50ac3632092c000e835a84",
  "src/main/java/com/feelingpilates/ubicaciones/dto/GuardarExcepcionSalonRequest.java": "4f3450c2c1304dc44f6b6406fa532ec92e0a3c20249b8e1efc4f84810dc780da",
  "src/main/java/com/feelingpilates/ubicaciones/dto/HorarioOperacionRequest.java": "fa0c890eeb2c1e8561287bafd89d5a93186fbbe5e70cf014950b80922c82f8dc",
  "src/main/java/com/feelingpilates/ubicaciones/dto/HorarioOperacionResponse.java": "adc6a0f18b59b6de278b42386d0e293e7bedd5c7c0eeb7fabb03582adfe69df8",
  "src/main/java/com/feelingpilates/ubicaciones/dto/HorarioOperacionVersionResponse.java": "b44112fd39a06f91176679b7669ce48e8e7b3d117c65ab111b1d7bb4f1144ec5",
  "src/main/java/com/feelingpilates/ubicaciones/dto/MunicipioResponse.java": "0e4d3eacb2c68b89145d3eb200993b57cbfb871223d90b229c38af92e8f0e65d",
  "src/main/java/com/feelingpilates/ubicaciones/dto/RecursoItem.java": "8838976864b2ee82ac743b857b547c5785106808048112fecb74815eba535350",
  "src/main/java/com/feelingpilates/ubicaciones/dto/RecursoItemResponse.java": "55789a41a9bc99d48027354ad576b444c9351f039cec811679141a1a65c02286",
  "src/main/java/com/feelingpilates/ubicaciones/dto/SalonDetalleResponse.java": "7e03157b97500c37beeb88cd136e6bc973ad4cb8bb80a9e3af2c91d76e115525",
  "src/main/java/com/feelingpilates/ubicaciones/dto/SalonHorarioExcepcionResponse.java": "2bfef73dcfe1881c174b8db4fa03018617b8962e1edbc75469fceb4f30444665",
  "src/main/java/com/feelingpilates/ubicaciones/dto/SalonRequest.java": "bfae3f2d7a664855fd34a81f075c6f6f4c0d89a410d5a45fb8a559d944d8a12d",
  "src/main/java/com/feelingpilates/ubicaciones/dto/SalonResponse.java": "f4927da181e5849a91bcffc5eca6ca8a843714dbc836f1e3d061ac008642dd91",
  "src/main/java/com/feelingpilates/ubicaciones/dto/TipoActividadResponse.java": "d18f07cf6ac1121202e718fe7482a07e6f1fb8adf58336e06ccfa50caf03bc2f",
  "src/main/java/com/feelingpilates/ubicaciones/dto/TipoRecursoResponse.java": "776ec8dd3ad42a459408eb37bf0b6f56d55768a68a5b896c6a6acbb2559f296d",
  "src/main/java/com/feelingpilates/ubicaciones/dto/VersionarHorarioSalonRequest.java": "3801a84e23a771180022b9c5b27dcb4f4766ebb70fe13d22d5667470d68d47fb",
  "src/main/java/com/feelingpilates/ubicaciones/entidad/ActividadRecurso.java": "a5a132051e17bd60a472b82e448d8f3ae478398a5cf9d37a9f3578b215ed6c93",
  "src/main/java/com/feelingpilates/ubicaciones/entidad/Estado.java": "40362dfb120e8ced3cbf884d62f03e775f82cb513728d5e1ce819a013748da24",
  "src/main/java/com/feelingpilates/ubicaciones/entidad/HorarioOperacion.java": "b46f5347dca93f0b28cb985a190c98ddd8f7a06a84049314b6d1b1a4afb48c1e",
  "src/main/java/com/feelingpilates/ubicaciones/entidad/Municipio.java": "ca6fb3375b294a89fb1cf419d7a8586709951af768f2d15d08d39439974efdb8",
  "src/main/java/com/feelingpilates/ubicaciones/entidad/Salon.java": "f11a0ff568f6dbaa2495bbf4d573932fb9ce0c88df1b2bd88227e029d2543e92",
  "src/main/java/com/feelingpilates/ubicaciones/entidad/SalonHorarioExcepcion.java": "6143ec922fa2d105d4984df6445d5f253773ae10f7d7f1dd58e1d3a5178b21ee",
  "src/main/java/com/feelingpilates/ubicaciones/entidad/SalonRecurso.java": "082cc2c125b2be4d56f9fd52fe90ee1174e9493d87ee41b1d12cc6ab01c8170d",
  "src/main/java/com/feelingpilates/ubicaciones/entidad/TipoActividad.java": "358452407559aaf18fe2b14c6ff40a02465cfbc6a6824e2f5f0c2a2b6a3322ea",
  "src/main/java/com/feelingpilates/ubicaciones/entidad/TipoRecurso.java": "af65fb9f859dc598a0786f292ee6c218086e0360ee29c2fb00b07ed8f61b57b7",
  "src/main/java/com/feelingpilates/ubicaciones/repositorio/ActividadRecursoRepository.java": "fd92ed9491b88dd74ad94d215df5ed06347df32f0e723a2ccbe469d5efd2ca75",
  "src/main/java/com/feelingpilates/ubicaciones/repositorio/EstadoRepository.java": "1114d5d1770470fe9e32060f684dce9b1fe06561414c4e7b0360c33928792375",
  "src/main/java/com/feelingpilates/ubicaciones/repositorio/HorarioOperacionRepository.java": "1b1e0a11453524a6176dfaee7cc2e5f50aeb22767ac53b5c42cc0531d6e60448",
  "src/main/java/com/feelingpilates/ubicaciones/repositorio/MunicipioRepository.java": "e46ed8cfdc4b287ee7943123727dd95afbf56dc1a7bbd31ee89764648a547a34",
  "src/main/java/com/feelingpilates/ubicaciones/repositorio/SalonHorarioExcepcionRepository.java": "c4335e795fb3c939c623c109c801d21328faa615c5da6bc82b0bbd00d29a27ac",
  "src/main/java/com/feelingpilates/ubicaciones/repositorio/SalonRecursoRepository.java": "c767945d18c7626cc3d6af23c4af2f28e06e538ed9fc817b750ae98fb2c3f91b",
  "src/main/java/com/feelingpilates/ubicaciones/repositorio/SalonRepository.java": "30b642c40bf10711c1ef8dd93c2d18cbba07cbc9e77b72edcd55f87285de053d",
  "src/main/java/com/feelingpilates/ubicaciones/repositorio/TipoActividadRepository.java": "58b790e54a68b0cd0eb43e15b230bf68fba9f65697ed91ce5926b3b263c479e9",
  "src/main/java/com/feelingpilates/ubicaciones/repositorio/TipoRecursoRepository.java": "465cf9193fe39e8ca1826708d5672890428141dac42051f0ccdaa5e7eb44f379",
  "src/main/java/com/feelingpilates/ubicaciones/servicio/CerrarHorarioOperacion.java": "cd0e6c09b9293167b9f99a6ae5650e31b4667ebca7a8c8b7a04751bb6fabdbba",
  "src/main/java/com/feelingpilates/ubicaciones/servicio/ConflictoExcepcionHorarioTranslator.java": "973869fb10b73b73d6449bc5ece39b6447edfcc8d0ec08b7162ba11ea078b9e3",
  "src/main/java/com/feelingpilates/ubicaciones/servicio/ConflictoVigenciaHorarioTranslator.java": "3497d67418bcd59592c79240f5d743608067c2a1aecf35ccdbd778e0a6fec2d7",
  "src/main/java/com/feelingpilates/ubicaciones/servicio/HorarioEfectivoSalon.java": "065e544c912e283c7800d109d4ec2b2f9f6016af40b272cf2bfa242db4c4c970",
  "src/main/java/com/feelingpilates/ubicaciones/servicio/HorarioOperacionErrores.java": "38b41d9e23e731f3e00484fbb43937cc17d00eebb9f3816658e427c8428f43b7",
  "src/main/java/com/feelingpilates/ubicaciones/servicio/HorarioOperacionResolver.java": "d90e18e46797830de52caf32dde6e36b343765a40a6cd909a8c6bd74b766a339",
  "src/main/java/com/feelingpilates/ubicaciones/servicio/SalonHorarioExcepcionErrores.java": "e84335c04f9075a917254b7f87468390fc801c6a775136dd7ed42b9c787e2d23",
  "src/main/java/com/feelingpilates/ubicaciones/servicio/SalonHorarioExcepcionService.java": "dd20b1f87cea434940aa23237935bf134164887d1d2248970eb6bc9339bb67d3",
  "src/main/java/com/feelingpilates/ubicaciones/servicio/SalonHorarioOperacionService.java": "4d80e324c4c01408892b0d0ebf4199ec594f2b6ee05bc0ceefdf8efde0a17e9d",
  "src/main/java/com/feelingpilates/ubicaciones/servicio/SalonLock.java": "f79c565e21296f30c5651cadd1db9d35a499e07e214625705ffc98246d0ab29d",
  "src/main/java/com/feelingpilates/ubicaciones/servicio/SalonLocks.java": "de5ed5431dfcf854825652362ddf5f95e4d351da123a5beee76ae69df3069fd2",
  "src/main/java/com/feelingpilates/ubicaciones/servicio/SalonService.java": "f1263f89419198e0ed2ea800c9b47ee7ec34b5a7161f55e1ab6aa7af5d269d36",
  "src/main/java/com/feelingpilates/ubicaciones/servicio/VersionarHorarioOperacion.java": "051b682acdf7b5682657f43746f2bdd4f383885b0d73784a43811aad5f3be3a8",
  "src/main/java/com/feelingpilates/usuarios/controlador/AdminUsuarioController.java": "87a1485a78cd6437055a61e5366938252f55de435ebb002bd810302645dba459",
  "src/main/java/com/feelingpilates/usuarios/controlador/PermisoController.java": "29187df18c7a5b7e0a02efc876005f9cf6e3b2d42075c80b1284d5972b907d95",
  "src/main/java/com/feelingpilates/usuarios/controlador/RolController.java": "2d6635c7632af589e044f310746f6e82fc4a25c8b40855d107c36d679e820c68",
  "src/main/java/com/feelingpilates/usuarios/controlador/UsuarioController.java": "10ff264a5ee460db9c9c390e404f1d3612daf05dd968773c3aa44c30937d1d3b",
  "src/main/java/com/feelingpilates/usuarios/dto/ActualizarPerfilRequest.java": "4e93d69e3f939d5278caa6cdbe8a925e9b38f80bad2f2d9c6ee49e2e140580f6",
  "src/main/java/com/feelingpilates/usuarios/dto/ActualizarPermisosRolRequest.java": "f7c246a3b326fac444d3b1bdc0db575163679b60d323ff68d58e86ba7b05cbcb",
  "src/main/java/com/feelingpilates/usuarios/dto/ActualizarRolRequest.java": "f6a33d368451c129901fd95fc5ceaa6cb6acef80a5eb8a485293a94a1ed3c5cb",
  "src/main/java/com/feelingpilates/usuarios/dto/ActualizarSedesRequest.java": "ae4d3bd2d0dd95dcb11987b32270d3d8cc7109dcf1c9de1c766c899373ce150b",
  "src/main/java/com/feelingpilates/usuarios/dto/AltaPersonalResponse.java": "d28cd7fb4b4333136157a5c20ebcb6474848bd447f047b6f78cfe5e9f8d9a094",
  "src/main/java/com/feelingpilates/usuarios/dto/CrearClienteRequest.java": "3c4282bed078a8e3d0148c046b001f19a5673406c7813fa7a181785d98612cfa",
  "src/main/java/com/feelingpilates/usuarios/dto/CrearPersonalRequest.java": "bb532d0f46b717355377a400983972bbc28c61fcc228d142546c0d7ceebf8ca1",
  "src/main/java/com/feelingpilates/usuarios/dto/CrearRolRequest.java": "45708a047d437fbb654ca9cb3b930ca9f6dd441ecde7eda478ff47582bcbb0e4",
  "src/main/java/com/feelingpilates/usuarios/dto/FotoUsuario.java": "cef8cf8d4435bb379a3f3d39d0cb4880e58f4b212d0ed875268d0cf82e113f16",
  "src/main/java/com/feelingpilates/usuarios/dto/PermisoResponse.java": "2eb93798a940a9887bea7da79fac874120b2213a326c34ec420950e5284bafa5",
  "src/main/java/com/feelingpilates/usuarios/dto/RolAsignadoResponse.java": "1717ef7a406520e86c4a7f23d39300f91193573e7d60adce3debec1674d8dbc8",
  "src/main/java/com/feelingpilates/usuarios/dto/RolConteoResponse.java": "020913a2e5c33b468834d2a55b415f583587a945dc0800edcfdd505a79b4e561",
  "src/main/java/com/feelingpilates/usuarios/dto/RolResponse.java": "4de17b45d1d0247f06a9feac369e1c5eff448bfa1e5bbf21370aecfbd063f670",
  "src/main/java/com/feelingpilates/usuarios/dto/UsuarioResponse.java": "4ee3f4b8918ec9b9e1d058b4d40fc38c9864fd95f2bcee009b74ebe1928532b8",
  "src/main/java/com/feelingpilates/usuarios/entidad/InvitacionUsuario.java": "18466ef4b9b9e891f988dabce32d2ff5b2674a9594e54747484b4c3739e39d0b",
  "src/main/java/com/feelingpilates/usuarios/entidad/PerfilInstructor.java": "9f77c77089b29e3ef615d83c443663d1615df5e034293a4d0812fc40a44c175f",
  "src/main/java/com/feelingpilates/usuarios/entidad/Permiso.java": "49595916cd73f19fd58789825d9d6f6d031435bde15f26502afa21547bc278ca",
  "src/main/java/com/feelingpilates/usuarios/entidad/Rol.java": "3c572b3890742009c79aa62e2fd74a31ee6963cb96bbc2dda5bddc91018fd7e6",
  "src/main/java/com/feelingpilates/usuarios/entidad/Usuario.java": "616d67f14e5799c826ff9acb72566b2ac095f5471274dcce7eff88161a3e3a36",
  "src/main/java/com/feelingpilates/usuarios/entidad/UsuarioRol.java": "f7dd388628d9679ef9198d0477c36b6f59a23b1a58a9fae4628c8908d8bc36a5",
  "src/main/java/com/feelingpilates/usuarios/repositorio/InvitacionUsuarioRepository.java": "2a105e4d6dd7fb46d32fcb6a44979917eeb37d0eaa461ec7d744dbe97ecdb564",
  "src/main/java/com/feelingpilates/usuarios/repositorio/PerfilInstructorRepository.java": "bc33d0cce03eddf925069f35fccfd3ab9a22a955c467b352d4cd039d75b8060f",
  "src/main/java/com/feelingpilates/usuarios/repositorio/PermisoRepository.java": "d9e587730314f0a784a1c8d41820e6e4dde62c372492421f878bfd1d693e0bab",
  "src/main/java/com/feelingpilates/usuarios/repositorio/RolRepository.java": "b32f30132287297609b581da6a86752a0578c6e9f939dec78bf2323c079e39f2",
  "src/main/java/com/feelingpilates/usuarios/repositorio/UsuarioRepository.java": "e95625627bdf76b1f177c5a1214bd96bd18c6319086b95438ba6281ccda2ad4a",
  "src/main/java/com/feelingpilates/usuarios/servicio/AltaUsuarioService.java": "81fd33faca54c0794079606d4021838adbc1a19aef356e6b021325468eb5b6fc",
  "src/main/java/com/feelingpilates/usuarios/servicio/InstructorLocks.java": "58d98aa3a53f4533475cb7a042129f291c2edea7a80089943a97bea1551f3b91",
  "src/main/java/com/feelingpilates/usuarios/servicio/PermisoResolver.java": "8c6edc535678f48d2c7833cfa1893e2264aebc223bae3558b035a9e7a95130a2",
  "src/main/java/com/feelingpilates/usuarios/servicio/RolService.java": "2395f30507e4e45b02803f8531823c06be9f73e3f569a22103244c12bd517d57",
  "src/main/java/com/feelingpilates/usuarios/servicio/SedeRolValidador.java": "043064bb798d94b22cb0a46c6a9f4b100b51485a6b8137b1519581de448f075a",
  "src/main/java/com/feelingpilates/usuarios/servicio/UsuarioService.java": "9be863a69c376d884ccbcde289d50023e771a837f08501fb6c7cd17217c1c031",
  "src/main/resources/application-dev.properties": "a9f8e82daea14f4f11ce14f68a183a7d484615fcbce79fe163059d6c9b765a8f",
  "src/main/resources/application-prod.properties": "b36a3c6ce3d860af97816c1f6160f38857f9bf80d8f18d7a42c8b49e7340fcf0",
  "src/main/resources/application.properties": "f07b8b033f0fc9d23926b7f33352c7453182b4664acbc593f6e6862cc90b5f96",
  "src/main/resources/db/migration/V10__salones_semilla.sql": "0806a7d947f5266a2a86ee4494885ca4413ffcb46a0d5f88c4044a07f6780f76",
  "src/main/resources/db/migration/V11__salones_gestion.sql": "686184cebdda9003c51f45ab0cc6c7a486c4511026dea1e71892fbe75985e59b",
  "src/main/resources/db/migration/V12__salon_direccion_completa.sql": "baaf4378963894eb972917767172297130ee59e204f84fc9962d10e8129f5c3b",
  "src/main/resources/db/migration/V13__usuario_foto_binaria.sql": "01a04f3919be365a770b273a00168616a0cfb33412ab711ec493edd4c217b4ef",
  "src/main/resources/db/migration/V14__salon_inventario_maquinas.sql": "7ef8be8cfd0426ba643b3c43bc848812c92c063d2ffe3681024c34eb8dd843e8",
  "src/main/resources/db/migration/V15__calendario_instructores.sql": "956e8530816d830756bbc8c477b61105b26f5e2e3bc0d96d468d748a87796310",
  "src/main/resources/db/migration/V16__permisos_calendario_granular.sql": "161edc552853d6c83464b5787f7f5065ab8577d7454f27bdec5e7fa2f5c006da",
  "src/main/resources/db/migration/V17__turno_instructor_actividad.sql": "43b1ab7f41feaddc9bce6961581cd2f99f8a9c90e482ce7f8ba5fd29c544190f",
  "src/main/resources/db/migration/V18__salon_horario_excepcion.sql": "453b8e5afff4a20c3d2234ea311485a7fee77efeb7a6333dfe6417d8126f7627",
  "src/main/resources/db/migration/V19__turno_instructor_multiples.sql": "256199bce25ef692f444753307c2723149cee449f8ee4082a18fa57389d0d0b1",
  "src/main/resources/db/migration/V1__esquema_usuarios_rbac.sql": "99481b564267ac0c5e5e2f4aa1175140ce24a6213e853bceb6bf115f3924e9f8",
  "src/main/resources/db/migration/V20__turno_instructor_asignacion.sql": "9302feeace2fa0547e850f7e053bc670c8a6734951fe38f57ee0de6a5065b7f1",
  "src/main/resources/db/migration/V21__limpiar_asignaciones_sin_especialidad.sql": "dd5318b5b3060756eeb440daed26236416916ff0b0353e68619bbb80b53998ad",
  "src/main/resources/db/migration/V22_1__paquetes_y_compras.sql": "cf979d2f8e8346359835d7f3bd23ada8c4a2370715a216f17e7060f7b8c19c32",
  "src/main/resources/db/migration/V22_2__compra_idempotencia.sql": "12aa65639797ab47f625b58c4fe5dcf8c3f57b142a31c1409f28ebcfbed50523",
  "src/main/resources/db/migration/V22_3__permiso_reembolsar_pagos.sql": "fb62666ab153404e7f912c8d845489c69945f06ad59ee3946b1a43206687c201",
  "src/main/resources/db/migration/V22__asignacion_rango_horario.sql": "2651e52a312b11c1e7daaf19bf152b5df2d569b40a62c9890d8be7a721bd182b",
  "src/main/resources/db/migration/V23__caja_paquete_actividades.sql": "8a7dcbda84afada3a0d43ec55047f15c7e4ff969c522e2cb013b7c6bc81bca01",
  "src/main/resources/db/migration/V24__eliminar_paquetes_semilla.sql": "3439e511642a81e03b00a8fbd2d82c20df383fcdde6e3972133b6733130031df",
  "src/main/resources/db/migration/V25__compra_salon.sql": "151d97d78d46cf5047145cdad3f82212ed5c020b4e70f304d03312c927707ff7",
  "src/main/resources/db/migration/V26__compra_grupo.sql": "32a29edf4df2173cbf090f70cddcec94106411bc3501043e1b2858109f08acf6",
  "src/main/resources/db/migration/V27__compra_motivo_estado.sql": "ff29c4097edf05ba3c1a5e36e592f51f4398b64b38f2b8f1b9a384723ad41e7e",
  "src/main/resources/db/migration/V28__permisos_caja_granulares.sql": "39caeddc01248976ce069dc3d4f131a55b1192d41753d5fbdef906c93f1e329a",
  "src/main/resources/db/migration/V29__permiso_vista_caja.sql": "e703ffe6cd3c6cf6083e7b9799ecf3226cee8690f9b6fc821c94ff967b933c23",
  "src/main/resources/db/migration/V2__datos_iniciales_rbac.sql": "0dd3f21ad92449ae2f0833d6ab15a4870aced4a44c942dd2007ec2845809bd02",
  "src/main/resources/db/migration/V30__simplificar_descripcion_permisos_caja.sql": "9721121a15e9361d1de0875080101c592647a091406b812561d2018e4aaf1f33",
  "src/main/resources/db/migration/V31__reestructurar_permisos_caja.sql": "0f9b7d0c08c4fe0a4ac179ede49166a1aaeffbdf02887e448e230862bba198d9",
  "src/main/resources/db/migration/V32__renombrar_permisos_caja_a_venta.sql": "19ef5766baf60dd5adc285412a111a70ab5c9f871e9af3a985ba3827fbe25e50",
  "src/main/resources/db/migration/V33__granularizar_permisos_catalogo_venta.sql": "7894280b0a26b47d0f5d72782ed5623ddc3181f1512b48f6add6271e25bb0f3e",
  "src/main/resources/db/migration/V34__renombrar_permisos_catalogo_a_servicios.sql": "84b311b98f2add720a4b2b746dd8458941afda7f299c42d1f3f2c51948ea9b7f",
  "src/main/resources/db/migration/V35__eliminar_permiso_venta_reembolsar.sql": "052d173e9d123276a8e74484b31baf51cf0530ebc49daad6f9acd12daf4b046a",
  "src/main/resources/db/migration/V36__recursos_y_actividad_recurso.sql": "247bd2e963c8b340de5e9370d237386b2f488dfeb4a738fbf0b9ab4c10bb31df",
  "src/main/resources/db/migration/V37__permisos_actividades.sql": "f9150e47cae63f3672e423356028cdf854a896dacd77fc976fedcc55bfd5c323",
  "src/main/resources/db/migration/V38__participantes_por_reserva.sql": "3ab7d119c919693a87958c481218ce0642c9333ec3fda60744f2d54ea2eaccb8",
  "src/main/resources/db/migration/V39__cantidad_actividad_recurso.sql": "09673b33102ac04e2493ee74046b792d8685c8a2e0cb026b0330bbeb947fdd0f",
  "src/main/resources/db/migration/V3__invitaciones_usuario.sql": "a1f899eef559a66201bc529af54e1d30427c5b88dc97adea25809d9b12ba0506",
  "src/main/resources/db/migration/V40__etiquetas_actividad.sql": "df0fe140f9b0a0d7fa956e421589c2a336cd5280dd58dd39e193596794145e33",
  "src/main/resources/db/migration/V41__programacion_bloque_asignacion.sql": "dc2e2ab0eaeb1070e803210bdaa8093899810f7251272229de07f000507001f4",
  "src/main/resources/db/migration/V42__salon_politicas_programacion.sql": "f90e8c872b15a5e3057c3b32dde61e1ccb32acf52e5701d72be5ba841c950628",
  "src/main/resources/db/migration/V43__horario_operacion_vigencia.sql": "f66940c681c74a765e25029a7fe75fe9d64cf0ed05e6c0faa657f81ff32a3761",
  "src/main/resources/db/migration/V44__btree_gist_extension.sql": "a290425e1d173f7132fa66e1360411c0897b326e79744b07a95fc74af08c6722",
  "src/main/resources/db/migration/V45__horario_operacion_exclude_vigencia.sql": "270d646845ab485b8b5fdb787134e4204c9c120da406d269d6d820a8a71875bf",
  "src/main/resources/db/migration/V46__horario_operacion_drop_unique_dia.sql": "f0de75f89423779dcc22e1a90aecd5e09cb084c11b32546463b54c45ecc4e755",
  "src/main/resources/db/migration/V47__programacion_ajustes_fecha.sql": "76d6690b0c42c5a462b4bc630074906dde092f67f5951e1b458ad7b9100a1a3c",
  "src/main/resources/db/migration/V48__pn14_slice2_orden_snapshot_expand.sql": "edc12860431820d634d4bc837eae79a5f3604718f83d99207b1ac8765459248e",
  "src/main/resources/db/migration/V49__pn14_slice2_snapshot_inmutabilidad.sql": "1aa858e265a389e9feeca1c691ace72e36fe87bc48fbdc79ff2e632fc3da280e",
  "src/main/resources/db/migration/V4__usuario_admin_inicial.sql": "28398c642f80dc50668634cdfbfc7c699bddeb5511d57d4ba9f7653f7fc96655",
  "src/main/resources/db/migration/V5__permiso_activar_usuario.sql": "3aa1f0e16b57cb3ba3f49f8e1f3f7f9309636f0e6bb8857182f357c7ed680eef",
  "src/main/resources/db/migration/V6__rol_super_admin.sql": "86b0e88db30e280ce87c5a22d7e6440a4812129aa04f8e014f2fa2ff754c8c21",
  "src/main/resources/db/migration/V7__categoria_permiso.sql": "8bc8c9d2067b57b783f615787cb92000e19e8a5ef53ecd52be77fca8afc41e32",
  "src/main/resources/db/migration/V8__permiso_gestionar_roles.sql": "4481cded81b9264fc2fc1b74a735e32d238543a1130bf66ee3732920545062b5",
  "src/main/resources/db/migration/V9__catalogo_ubicaciones.sql": "177fb922724e544e99c009383d8946228c6ef3aa3e88350acb67dd72a1c2f230",
  "src/test/java/com/feelingpilates/FeelingpilatesApplicationTests.java": "681dda7032122ea2592cc76da8fa4ef183699ba7f5fb2082c7e32a2d10e5ae54",
  "src/test/java/com/feelingpilates/TestcontainersConfiguration.java": "1f01c277c974e804506a05fa236b9c6997e9552e92e4160439f23cbbadeeaa9b",
  "src/test/java/com/feelingpilates/auth/AuthControllerTest.java": "9eb507cf8905963f6c2d0135fb5bff5733d6fdb7956e0d05d008b5e21e5e1b0e",
  "src/test/java/com/feelingpilates/auth/dto/CompletarInvitacionRequestTest.java": "375645c0e4251273e1dbf60d2ca6c41d8641dbd5519fc1641aa0ee3faeb3ef8c",
  "src/test/java/com/feelingpilates/calendario/ReservaControllerSecurityTest.java": "070a8b93701de8cd652cc1d3de34449132e108e39e8d49d095d3f869ffb8fae4",
  "src/test/java/com/feelingpilates/calendario/ReservaServiceCaracterizacionTest.java": "6c93b3544f9201d810624220710c15f31a25abcc3a57bd4c9bb8f7a5905ce116",
  "src/test/java/com/feelingpilates/calendario/TurnoInstructorServiceCaracterizacionTest.java": "409e39c35cfdfc7ea6e7a455046246e0eb810da2b913f1d3a83a210bd7e4e412",
  "src/test/java/com/feelingpilates/calendario/TurnoInstructorServiceHorarioVersionadoTest.java": "0db0ecc3b7c06b95aee3dbe796664919ccdcfe2c0e450155739ec449446b9297",
  "src/test/java/com/feelingpilates/calendario/servicio/ImpactoPuntualEnExcepcionHorarioTest.java": "d4e2980d482e0f3087415f37b4c5a329962c36ddbab82ec4f82bed00abf40615",
  "src/test/java/com/feelingpilates/calendario/servicio/ImpactoTurnosRecurrentesEnHorarioTest.java": "105e8bc38ce608c218e3d0170990b65fc84de7af0dfc3b7cbd9e475b34b3947b",
  "src/test/java/com/feelingpilates/exception/CodigoErrorExtractorArquitecturaTest.java": "5dedf9d88004291ef3a52284bf1a85cdf14953f9ea944361ef44d143872e0869",
  "src/test/java/com/feelingpilates/exception/CodigoErrorExtractorTest.java": "125dbffa8d5dd6689789bdbb4cfc6a022e46b7e8569507228924292e79a0ebf7",
  "src/test/java/com/feelingpilates/exception/GlobalExceptionHandlerTest.java": "281941cf8176375a2b9b08f7da2f1e408e74216ff18922b4ea3e019d01a6f829",
  "src/test/java/com/feelingpilates/notificaciones/EmailServiceConsolaTest.java": "83b095e6e7ab8d64f5881ee66cfd85c83e0e4e7dd01df8f9915d94fa57313564",
  "src/test/java/com/feelingpilates/pagos/caracterizacion/CatalogoPN14Test.java": "28d9fc1a1ef05095e03df402e7cf5030be01cb05aa8a67ae1894afc24992946f",
  "src/test/java/com/feelingpilates/pagos/caracterizacion/CompraPersistenciaPN14Test.java": "70bf0043a47a9c3d57bae069f0b4dad9443d525e6a7bfca5502cc24b88e379c3",
  "src/test/java/com/feelingpilates/pagos/caracterizacion/PN14Fixtures.java": "ac894811e45df7a0b433d10451bd4075125c064d3661a8c0d56737011c8b2c01",
  "src/test/java/com/feelingpilates/pagos/caracterizacion/PagoIntentoPN14Test.java": "0282cde45d8211c60697a5e8e995b62addbcd0e74e9dcb0a51a25c93a046df33",
  "src/test/java/com/feelingpilates/pagos/caracterizacion/PagoLecturasPN14Test.java": "1b36bb8bb64192437745c46e6320f025fe5e72ae1b91ce11721f14f5daacde08",
  "src/test/java/com/feelingpilates/pagos/caracterizacion/PagoReconciliacionPN14Test.java": "1fe64052181919b9d6e3e41362c40115324638ce86d1ad762191e3597157e901",
  "src/test/java/com/feelingpilates/pagos/caracterizacion/PagoWebhookPN14Test.java": "9be4353e096185ac6db6f4248df680e6698826b5e5fe37e9bfd237c167f52cd2",
  "src/test/java/com/feelingpilates/pagos/caracterizacion/PagosApiPN14Test.java": "4d2fca0bf6a62eee30851364065a8b29ff770344a676c5ec87c742b052e47e52",
  "src/test/java/com/feelingpilates/pagos/caracterizacion/ReservasApiPN14Test.java": "e3e990cd793e790d8bf8f238fa2da87a2a6d44443ec3b218a75815272fac6e01",
  "src/test/java/com/feelingpilates/pagos/caracterizacion/ReservasPN14Test.java": "826a469a107a56c12d5516bfb595569f899b71ba1a7947eaeee9c7a526e4aff5",
  "src/test/java/com/feelingpilates/pagos/caracterizacion/StripeResponseGetterPN14Fake.java": "687a7e1d002dc03cf38dae2c884cf228cd0b2f36e9679f5c14866013c5cb886c",
  "src/test/java/com/feelingpilates/pagos/caracterizacion/VentaServicePN14Test.java": "3cf7da622e94f87f56cd2f46a7366c8f54c615adfb04605797c4939488b5913f",
  "src/test/java/com/feelingpilates/pagos/caracterizacion/VentasCatalogoApiPN14Test.java": "0ce9bb46e3ee22a65b26426fdab8d82ce891d6966d5c2c53ac02697d3992a48b",
  "src/test/java/com/feelingpilates/pagos/ventas/BackfillOrdenSnapshotPostgresTest.java": "5dda0a95515c880a08e8a2487ad7a0c2e3040464283ab689266425381890f0a4",
  "src/test/java/com/feelingpilates/pagos/ventas/BackfillOrdenSnapshotTest.java": "4adff58b23be5225ea236d6f95ff05a39f85607ab9fe83b177a8ccbba96fe80f",
  "src/test/java/com/feelingpilates/pagos/ventas/ConsultaHistoricaSnapshotTest.java": "077e3d9ce4055d335e9706f336f226fed382deef7c1df785c1c578a7c14102a0",
  "src/test/java/com/feelingpilates/pagos/ventas/OrdenSnapshotConcurrenciaTest.java": "79e2f8f807eccdd9c687a775fbabf213f2aea5e5129ebf3a51bb11b2d6caf607",
  "src/test/java/com/feelingpilates/pagos/ventas/OrdenSnapshotDominioTest.java": "55c605f696d26dee29a3ec585f18de2c2c6b36ce4592b47d7fc79858d3799471",
  "src/test/java/com/feelingpilates/pagos/ventas/OrdenSnapshotIdempotenciaTest.java": "4075033c5439bfbaa46adc6758a90d10c7c38572edc858e3ce129af90ce2e121",
  "src/test/java/com/feelingpilates/pagos/ventas/OrdenSnapshotMigracionTest.java": "a6fdb43bc4b54293c0db96c97ee7db304e8fb7575eb1974db6aa3673a1d1f0b1",
  "src/test/java/com/feelingpilates/pagos/ventas/OrdenSnapshotPersistenciaTest.java": "9d275805c51a31a13d44cec227f114933f7aef9fd1833bd6a0c6d70c8e525b77",
  "src/test/java/com/feelingpilates/pagos/ventas/OrdenSnapshotTransaccionTest.java": "9d5b6e713ff238df9dcefa24f3dc8d45c90affcb1c350f334f96bda6c5695c11",
  "src/test/java/com/feelingpilates/pagos/ventas/PN14Slice2Fixtures.java": "403a48fa7ee4d4de6a80bfd2f057c6229087e5b8d56788e22aae91785ed98fd9",
  "src/test/java/com/feelingpilates/pagos/ventas/PoliticaSnapshotCanonicoTest.java": "644201fc91164971c027e8d58d9bdf71f94a7095c0675f484ab807bb9db96bf9",
  "src/test/java/com/feelingpilates/pagos/ventas/VentasSnapshotArquitecturaTest.java": "a75ee330361c1b79bf3224edd167da8250ddeffb27c9e33c4f16c514b4a45814",
  "src/test/java/com/feelingpilates/programacion/AjusteIdConcurrenciaTest.java": "b32fbd0df451aff8e07e59433b439786cadafdb897f4f8a31b75ab638b68666a",
  "src/test/java/com/feelingpilates/programacion/AjusteProgramacionFechaPersistenceTest.java": "90e44ea63a3654860d6d4126727f7084d69249e18368eeeec030cb58db9bce1e",
  "src/test/java/com/feelingpilates/programacion/DarkLaunchArquitecturaTest.java": "b6d3a82a5575d3fc7a6ca6e540ce3467c1d7599c3760b147222a41c4b66fe1ba",
  "src/test/java/com/feelingpilates/programacion/DarkLaunchIntegracionTest.java": "e3cf7e3ca11157d68890fd01c63b04731ab02e2180dda8a6dac6880c59dc3726",
  "src/test/java/com/feelingpilates/programacion/MigracionV46V47Test.java": "50c4463cda021cecb1523c084000fc077b63ecae3f0aaeee9838da2eebc3e59b",
  "src/test/java/com/feelingpilates/programacion/ProgramacionConcurrenciaTest.java": "a0b3ecc779b393ca9cd4ea6cc4a7420ddada23a9c5bc4722ecca4b08c7f1906a",
  "src/test/java/com/feelingpilates/programacion/ProgramacionPersistenciaTest.java": "0a4ca66231d7f0545568ecbef6bb09e58270b4d68c4f567ebaac22d7a9ae9ea7",
  "src/test/java/com/feelingpilates/programacion/RelecturaAjustePostLocksTest.java": "ad9a8c7565224ab70061d8f0c45dcb4a2f6dc2022285515d5b16e958ef07538d",
  "src/test/java/com/feelingpilates/programacion/repositorio/BloqueProgramacionRepositoryVigenciaTest.java": "e90211144efd46b3946816525c875dd4866ae2106117e076dd87f64e784e6c57",
  "src/test/java/com/feelingpilates/programacion/servicio/AjusteProgramacionFechaServiceTest.java": "dc79610d039d1fabd490212ff7595055924e9833bbe816ac2990314077a3c484",
  "src/test/java/com/feelingpilates/programacion/servicio/AplicadorAjustesProgramacionTest.java": "2ff5e5655d15422ea3574485962640fd60c22ebf72937586fb1b38db4e91adad",
  "src/test/java/com/feelingpilates/programacion/servicio/BloqueProgramacionServiceTest.java": "983f158612728ee4e2560e794188358f64e6f039db48b561121a611e5c2ebf7b",
  "src/test/java/com/feelingpilates/programacion/servicio/ImpactoBloquesEnHorarioTest.java": "6986d12a538e57a58f866517775a7d6e2830da5c8a7127defd75309f3f7a83ac",
  "src/test/java/com/feelingpilates/programacion/servicio/LocksOrdenadosTest.java": "3dff81ef349d770870d4c48c116caffa5fcf2070aa203ec2f17aedaec29e411f",
  "src/test/java/com/feelingpilates/programacion/servicio/ProgramacionEfectivaTest.java": "7843c02ffa5ad7a1da0835f8c3254ef86350542fe3e715dd3482e9981610b3d6",
  "src/test/java/com/feelingpilates/programacion/servicio/ProgramacionPolicyATest.java": "73966a3e489b4210748bf9e7d2a8f0595141a23892da2ee6ac6731b9fe76dd69",
  "src/test/java/com/feelingpilates/programacion/servicio/ProgramacionValidadorTest.java": "9f510971e1ccd6bc242c74076df792f01836efbdd3d405b3bf589e7b2ae92be2",
  "src/test/java/com/feelingpilates/programacion/servicio/StaleDiscoveryAjusteProgramacionTest.java": "06c5d2b281708635a44d86f3be9ca9e15b679c72cc4a79aacca9fbc9ae42c367",
  "src/test/java/com/feelingpilates/seguridad/AutorizacionContextualControllerTest.java": "bed51e685a5451e26872dbf9ffa2d9d1272ebd89db961980e44fdc433900f40a",
  "src/test/java/com/feelingpilates/seguridad/AutorizadorSalonTest.java": "79e438a9d0dc237a491bb1c511cd62b45f1d7a2a22490df4f94c1fb6752c00af",
  "src/test/java/com/feelingpilates/transicion/programacion/detector/CandidateEvidenceGeneratorTest.java": "16f0c8cfb45a1ec7f2522d0726381cf1021826d901a0dcd32937c2155ffd52e3",
  "src/test/java/com/feelingpilates/transicion/programacion/detector/DetectorArchitectureIsolationTest.java": "b38f7e23e82174bd33808aaf424158dd9e769b16574d5735fc372a5b9195e10d",
  "src/test/java/com/feelingpilates/transicion/programacion/detector/DetectorClassifierTest.java": "c25c0be74a4ea3c17218b152035fde4d020c216349ba0fc96b1f5f12f0247cca",
  "src/test/java/com/feelingpilates/transicion/programacion/detector/DetectorImmutabilityTest.java": "feacdaabc35291a04f3fd9747b5ae4304a79f1f5bc6e098ceca89b13dbd407f4",
  "src/test/java/com/feelingpilates/transicion/programacion/detector/DetectorResultInvariantTest.java": "cb5f0a44ad8463dd02f5838c5767c4e2b13ceba582cfbbee419b12bb2bc81fd9",
  "src/test/java/com/feelingpilates/transicion/programacion/detector/DetectorTestFixtures.java": "fe5dcbeb4f770b5cacc69061369af5ec89bf5981c6fe061ed1db50806f573dfb",
  "src/test/java/com/feelingpilates/transicion/programacion/detector/F2DAuthorityGuardTest.java": "cc74f82486574b92f52b9ad9375ce6b43d7bfab8f30fe2d156202d4aed4431ed",
  "src/test/java/com/feelingpilates/ubicaciones/HorarioOperacionConcurrenciaTest.java": "1a94bcc0cbbe36d5a68669f0133ffc355612a1a0cc0d6445804f03cbdc043679",
  "src/test/java/com/feelingpilates/ubicaciones/HorarioOperacionMigracionV42V43Test.java": "bd1a753322cd7c7d0c7a6f46d1e0f0111af77eeea2a5a36db50de88f3d366537",
  "src/test/java/com/feelingpilates/ubicaciones/HorarioOperacionMigracionV43V46Test.java": "86c5896be9def5348188b0566467a4cd7a85f3a539b30c42ec7af092babe258e",
  "src/test/java/com/feelingpilates/ubicaciones/HorarioOperacionVersionadoPersistenciaTest.java": "ebff877409634ff6174ee929e5b744e3804905767276d53512fae778beaf0d5f",
  "src/test/java/com/feelingpilates/ubicaciones/HorarioOperacionWritersPersistenciaTest.java": "7faecc9ce2f64f0951006371726c587f4aa7f45687abd93368a73dc04c4aa084",
  "src/test/java/com/feelingpilates/ubicaciones/SalonHorarioExcepcionConcurrenciaTest.java": "e5fb7984fc9206f0986eefab1b8956a50453c2cb39b476507fec57a25f50ac88",
  "src/test/java/com/feelingpilates/ubicaciones/SalonHorarioExcepcionPersistenciaTest.java": "3070512484504d56b1d4c012c7aa6d9fef65c2ddafb90ba6d4ed714ca60b5ab8",
  "src/test/java/com/feelingpilates/ubicaciones/SalonHorarioOperacionHistorialPersistenciaTest.java": "04717d21611e996c65061a509d9e0e3fab10f55c1f3c6972b4e93f09f3b0c220",
  "src/test/java/com/feelingpilates/ubicaciones/UbicacionesPersistenciaTest.java": "10d85876b22c8c7b16fe21d26e4e7331affc612ac0d43a7186664a8ac2f1ef64",
  "src/test/java/com/feelingpilates/ubicaciones/controlador/SalonHorarioExcepcionControllerTest.java": "273fccbce9e8b1d11337e05be187bd1d9908fe395424705b4ad38f6495a14827",
  "src/test/java/com/feelingpilates/ubicaciones/controlador/SalonHorarioOperacionControllerTest.java": "3dacbf7ff6739f4bb309803a83609ca28a32fd0622fda75559f6d87c0fcd5968",
  "src/test/java/com/feelingpilates/ubicaciones/dominio/CoberturaVigenciaTest.java": "804cbdb018a76954533a92df20a7de91078b2c53f4134b6f698bc7724ecdde1c",
  "src/test/java/com/feelingpilates/ubicaciones/dominio/DiaSemanaOperacionTest.java": "6e05f36e3a0acc388bbb86bcd84c8f12894b738392d746ca0298382432042f32",
  "src/test/java/com/feelingpilates/ubicaciones/dominio/RangoVigenciaTest.java": "e99495cc63f6fe1138844f78f3ed630885235369a5a32aa8ce5e1faeba69773b",
  "src/test/java/com/feelingpilates/ubicaciones/repositorio/HorarioOperacionRepositoryTest.java": "fb62d6d8b3eb9d1a5e0020160cbe870bf5a8c3814b971713e47435c7e9a76193",
  "src/test/java/com/feelingpilates/ubicaciones/servicio/CerrarHorarioOperacionTest.java": "aa8163bcad98c84db99ea576c9e77f7453b2dafb19691858eb19e475bb017898",
  "src/test/java/com/feelingpilates/ubicaciones/servicio/ConflictoExcepcionHorarioTranslatorTest.java": "c9b49b69f8db810dd88141a132db89fc65c736bdcce67403e7685f58839c3f98",
  "src/test/java/com/feelingpilates/ubicaciones/servicio/ConflictoVigenciaHorarioTranslatorTest.java": "d22788695c88e7120b2ac7fbc70538bfe08fc61cad4f20cb735c58f3c53bcd4e",
  "src/test/java/com/feelingpilates/ubicaciones/servicio/HorarioEfectivoSalonTest.java": "23f8e641f87438184c7c8983013662c64e6e7ebc0d42013fd25e0c11a9f2654d",
  "src/test/java/com/feelingpilates/ubicaciones/servicio/HorarioOperacionErroresTest.java": "e2a3f7cb0cf8c9fc42fcf75524bc54fc9d28c2a20f15ba4a01eee5d45f35b1b4",
  "src/test/java/com/feelingpilates/ubicaciones/servicio/HorarioOperacionResolverPersistenciaTest.java": "1f93faa3b40e479aff6ce17bb3889eac32dd753a3c0f71efd24613e3c11e52e2",
  "src/test/java/com/feelingpilates/ubicaciones/servicio/HorarioOperacionResolverTest.java": "a11b885d0d16b02d2360742a4ba247a10fb8da891c020b3b7a8fef9d36a8f7e2",
  "src/test/java/com/feelingpilates/ubicaciones/servicio/SalonHorarioExcepcionServiceTest.java": "31c5c36ddb5880c2861889671b9f59791411da07d2a785a80e16836c647346f8",
  "src/test/java/com/feelingpilates/ubicaciones/servicio/SalonServiceTest.java": "d14de2a22785c016390c4a79a49faa06ca3b3ffb1f9859fd929dadea28ab64d1",
  "src/test/java/com/feelingpilates/ubicaciones/servicio/VersionarHorarioOperacionTest.java": "51182f479980f766670a59c07a2942e8aca0d603f727ac19ab955bda16214c1c",
  "src/test/resources/mockito-extensions/org.mockito.plugins.MockMaker": "8eca853a5a457f17e0479b71dfbe95f2a13aff9eeff250261ecdc8ca9b847a6a"
}
```

## 3. Tabla exhaustiva original502 / exactpaths UTF8

| Path exacto original | SHA256 rawbytes original |
| --- | --- |
| .env.example | 4578f61ab7fb104e72564a6183e25cd5ef21dca90ab13bee8bd88921054a9da6 |
| .gitattributes | 5775a77ad2f6d12dde053a3fb0cca4309189163f0888f4e7e984cd0ab327028b |
| .gitignore | 99868de8c49276583ca9830e50174d0b310445ca6c39523c407042ed440e122a |
| .mvn/wrapper/maven-wrapper.properties | 488e1b3f2e641779d4636abf9390845f901e64607261bc3c0b0bfe4fe96e6706 |
| AGENTS.md | 1d70ebbc5bfef3c0004a7ca53ce8a91bf6a9660a4085e2c7ff2a44a5d58038c4 |
| Dockerfile | ba169b54da91a2f8cee98c6c08df7a699402118d0a94e1057a00304faec8f945 |
| README.md | 3239d68abf409c88ca8d2603e195b6128fa2fbdab6dfc13d94a6bfa0c0474096 |
| auditoria/00-revalidacion-repositorio-completo.md | 845fb0a569a7d3efe31418c28f074f074625d8d04bdb3217450bfbfe724471bc |
| auditoria/04-arquitectura-objetivo.md | 684e5630e83c8d3da6d024f5f76ca4a43df31d07a5e7141f18d5143042b24b40 |
| auditoria/ARQUITECTURA-ACTUAL.md | 95eee81e34a3437628882b628ae138d6680c272767b8f8ebafb1027f675c7bda |
| auditoria/DECISIONES-ARQUITECTONICAS.md | 305bb270f770029e4ea576019b9410970f12c7e70da554fa7b5e09f737123b83 |
| auditoria/ESTADO-ACTUAL.md | f21576b6a909b904ab1cdb2c6616b31199904150a35d320e1e0f7c27b4ee77d0 |
| auditoria/README-REESTRUCTURACION.md | 747227acc83ad7337d8a1214646ee089def5ed42ec6eee637d22ff96969ce2b2 |
| auditoria/REGLAS-DE-TRABAJO-IA.md | 84799b8946e428e7e907bc20b96362e699347e7307d0ba886c4d85c06ffa55ea |
| auditoria/contexto/DOMINIO-FUNCIONAL.md | 0580272ff72a41c841830e2e6cf13e8c1022e3716a59d741a780f4661a4b0b3a |
| auditoria/contexto/MAPA-LEGACY-Y-MIGRACION.md | f7ef171bfa7abe1001ff8380716f6883f4db66d7b13c15c3ecc8c1b0dbf962c3 |
| auditoria/fase-0a-baseline-git.md | 5f01bc46d5bba75fbd27038d21c10db2d103eea9464df8a5c76298305924f95d |
| auditoria/fase-0b-preservacion-baseline.md | 647d13174e9c2b9c792459e63c6ce6fcc07d29a2e11e87bfb1561fcf174ad36c |
| auditoria/fase-0c-flyway-ambientes.md | 851eac715066d287f490d291668663c50030dbb6b9ebbb074f4ae18634180830 |
| auditoria/fase-0c1-saneamiento-flyway.md | 7091a45752d2b3b46964ec9ce800221d393c44722de13e0456f2e144b5d75cd4 |
| auditoria/fase-0d-tests-baseline.md | 03dd5f71b78a388c818e93b89fd7ebcd58da7849890faf4e483218a078a60d36 |
| auditoria/fase-0e1-reservas-idor.md | 497faadf161b1950aab4f8e1a6499ad9daf30bd453740f0a89e5c99b0bc03c93 |
| auditoria/fase-0e2-scope-salon.md | de646ec3881ae074ecf695af29140342fe1f4fe30f0228590e07fb4d09a4d24e |
| auditoria/fase-0e3-redaccion-secretos.md | 599468c77c195320e24cf4b68830c1901ca9d01b99aca26bf479e7ae5786a6a4 |
| auditoria/fase-1a-safety-net-calendario.md | e51acba3a1ce42d046238b92d97b9df52a79c9047b5dea7047e48a8c2925548e |
| auditoria/fase-1a1-safety-net-calendario.md | dba837ab1ce0c3f427a143f7eae38d34c1acd4aa24c4815932f60d4bcb8ed0e4 |
| auditoria/fase-1b-diseno-programacion.md | 69fe7874ab348b135ccb73b790f3d30538111e3f15659fae18b8d2e0adecee11 |
| auditoria/fase-1c-modelo-base-programacion.md | c3fbfc1447cec29a03ef9926a28ffb514fb668ae700046c00e3916b3fcf9943b |
| auditoria/fase-1c1-safety-net-modelo-base.md | ccfeee535b78f68fa6542c66a75885c84cc359dbdbaf6d2c21f5fc2581086792 |
| auditoria/fase-1c2-query-binding-vigencias.md | 067f8a64642226b71ddf52d90542fb592fd165c5aafd4a81d8495ad212dcff05 |
| auditoria/fase-2a-horario-vigencia-politicas.md | 268382162fdfb47453aa38d6c7db9455e022f6e78050db59d188821f4665f076 |
| auditoria/fase-2a1-safety-net-migracion-horario.md | 517caba47e08465b70b7b2ff8a3e218c83a42271c420360e9de342c5030ddf9a |
| auditoria/fase-2b-diseno-versionado-horario.md | 83266b98f371c5bb1c22a324508da5513703b88ddeab3ed1d8f47b1f15ea2c9d |
| auditoria/fase-2b1-nucleo-temporal-horario.md | 7cf60ac43c1ae2d4529311b7830f200ac45b74c52a93984c3f380e4eebbfec60 |
| auditoria/fase-2b2-consumidores-horario-temporal.md | d1e56d741673fc186a9371d54f6d35cc34553870b3b6e3ea0bbd3881591648d2 |
| auditoria/fase-2b3a-persistencia-versionado-horario.md | 6def09db373968f8dabe9c31aea50982b6317e356a9119428f50025808465445 |
| auditoria/fase-2b3b-diseno-writers-concurrencia.md | 76cfd65feee30fea0ae0bc63eab9011069fb0cc2213a089716cafa72b9e35959 |
| auditoria/fase-2b3b1-writers-concurrencia-horario.md | b612f65ee385586ae034823a41b6ecb407ddd1b52138f653c6e923a9a8dea508 |
| auditoria/fase-2b3b2-diseno-api-frontend-horarios.md | 70f49df3e19755d992057eb0785eeebbb1be849e65458609ad277d12198bf2fd |
| auditoria/fase-2b3b2a-api-backend-horarios.md | b1edfb108bf59244b4c35fd0de73f51ebc1b94a541b6874b27225521a3b0ac28 |
| auditoria/fase-2c1-diseno-excepciones-horario-fecha.md | b0ddb92e171246d561cedd7a3ebe2d93fd815b389870e622ddb578608a3ff25c |
| auditoria/fase-2c2-implementacion-excepciones-horario-fecha.md | a858286d7a8efd1d77195ef18a620450bc4cbe404a159353c5b5638b9c6c344a |
| auditoria/fase-2d1-diseno-ajustes-programacion-fecha.md | 58af39f41b3bc089ebbd4ec67f684e270087ddf4eb695f2c7b55276d0aff352e |
| auditoria/fase-2d2-implementacion-dark-launch-ajustes-programacion-fecha.md | d7eb6eb5c86ee0658207b8be957517f3715937d66389b8ab2f41c1ac52a042cf |
| auditoria/fase-2e-diseno-adapters-read-only-snapshot-consistency.md | 6c72cba1f83fbc2bcf3b3219d8252d2410ec482e30ae85d30fd2eeb04e8883d8 |
| auditoria/fase-2e-identidad-semantica-detector-read-only.md | 6f850e9723f9861456d646039e4b233cff20d013ff956cab98f7370dffac4670 |
| auditoria/fase-2e-preparacion-migracion-controlada.md | e92c78df47cce8a3cd486f97d85216716003a4617ed03374dd080558fab61b9f |
| auditoria/fase-pn13-materializacion-autoridad-pagos-notificaciones.md | 5605569945e72a9d7ceff2778c64a9444d077ad4b6d80af53ea8e381d71d1749 |
| auditoria/fase-pn14-autorizacion-implementacion-safety-net-caracterizacion.md | 8b861d8a16a71a102155cf1e6c2b8d84dc343cbaf45e43baef480f5e0ed3b6df |
| auditoria/fase-pn14-slice1-safety-net-caracterizacion.md | c7b3dc12b77d3146181124d11f55cfde836ca8d883117a6b2548412934c14efe |
| auditoria/fase-pn14-slice2-autorizacion-orden-snapshot-inmutable.md | be4f3348f5918f4640c597899fcb23b1243bfaca01c03e3ed25040f0a9801cbc |
| auditoria/handoffs/HANDOFF-F2D1-1.md | 96c114ecb042324894987549feef9cf57a67a636f76e923499bb33ead46f11b8 |
| auditoria/handoffs/HANDOFF-F2D2.md | 914d69eac171f895d6e8d62a97d4ebc00330fc2a088bb9daee278860f6ee12c3 |
| auditoria/handoffs/HANDOFF-F2E-ADAPTERS-SNAPSHOT-DESIGN-AUTHORITY-GAP-R1-PROVENANCE-JPA-TX.md | ff74c850bce03e2ec757d4dde8c3f4a3b39a71c35e219b4a8e7b3ccf629cbb77 |
| auditoria/handoffs/HANDOFF-F2E-ADAPTERS-SNAPSHOT-DESIGN-AUTHORITY-GAP-R1.md | 85c902d45eff21f2d5f8392e54970e7468b25cfbbeab6af229573fc95d198bac |
| auditoria/handoffs/HANDOFF-F2E-DETECTOR-READ-ONLY-NUCLEO-PURO.md | a655cd147f0cee1b9f8fc86a3351fe07ba53be2904a7adacdabace2d4c2b55ae |
| auditoria/handoffs/HANDOFF-F2E-DISENO-ADAPTERS-READ-ONLY-SNAPSHOT-CONSISTENCY.md | bec439a3c7be004f71186009a8e3198b11ab5c69d32a220d1bc863715b7955a2 |
| auditoria/handoffs/HANDOFF-F2E-IDENTIDAD-DETECTOR-READ-ONLY.md | 3468ec1c1efbc532169a3c379fc262d27bb8f4d81d3d8cad8911934a83ca6330 |
| auditoria/handoffs/HANDOFF-F2E-PREPARACION.md | 2c6c4d3bd5b17be3cf56293edcf4460c5d12fe24adc78bc18529c1321ffbe3a3 |
| auditoria/handoffs/HANDOFF-F2E-R1-RESERVA-READER-JPA-READ-ONLY.md | 3fd71faca4d4c049ad5cb37b52bc6fd512509cf5b696bdc5c28d28cb966af8ef |
| auditoria/handoffs/HANDOFF-PN13-MATERIALIZACION-AUTORIDAD-PAGOS-NOTIFICACIONES.md | 601d285a23c87b131b4b4946d2f858ad918faea12e492d76f7e9da7acd073e22 |
| auditoria/handoffs/HANDOFF-PN14-SAFETY-NET-CARACTERIZACION.md | df540a241671b5c1c173a5aaf6d809871e9beb8bd8248d16025ff0984b7f48ab |
| auditoria/handoffs/HANDOFF-PN14-SLICE2-ORDEN-SNAPSHOT-INMUTABLE.md | 4d7e7803557f75a3d62afe67a757687a63b9c627b0fdbf2580d89feab36fdd7d |
| auditoria/intervenciones/F2D.1.1-CORRECCION-POST-REVIEW.md | bd1ec9134c2b46d56580471170f54f9d52fb174bc30e2ea0e33ecb025ba0047d |
| auditoria/intervenciones/F2D.1.2-AISLAMIENTO-DARK-LAUNCH.md | d114eaba9701363d791c09a44bf576b25a53aa0dd314980ce9e90f77289d8bc7 |
| auditoria/intervenciones/F2D.2-PREPARACION-INTERVENCION-ORIGINAL.md | 59d09c998deb391a455a12c42fb25fcb7d0dfa1911a6d2ebe4e589d1935b4296 |
| auditoria/intervenciones/F2D.2.1-CORRECCION-IDENTIDAD-CONCURRENCIA.md | bdd4fd689dc34f3466ec0c02927bd105ed1029f3b8a88b4fe5fc362ac1ddd2eb |
| auditoria/intervenciones/F2D.2.2-CIERRE-CARRERA-AJUSTE-ID.md | e7565f5cc442df09b43413daa76d62c8c2718c9bd21ce99386cb7551a3547376 |
| auditoria/orquestacion/GATES.md | b3137de84aa171dd6f11bf439c79b937e44a52de54a5cb841fd57048678d490b |
| auditoria/orquestacion/README.md | 21d094499f49a7c7f0878e4c183114c4ff8525446dca5e8db1151a90e7146a7c |
| auditoria/orquestacion/ROLES.md | 8c19f5981cad4941a34356fecde833168b1754620e167fd323b36c35490dbb50 |
| auditoria/orquestacion/STATE-MACHINE.md | 0641e4725947d1fb2f16cf304255b0b30739090bb810488672503a206f6f13d8 |
| auditoria/orquestacion/WORKFLOW.md | 107857275a38dfb23f094ec35054113b88de404e0ae7100c2bf527071de130d1 |
| auditoria/reviews/F2D.1-REVIEW-AJUSTES-PUNTUALES.md | ac2e44ffc745b0e01efd8897616d7e3809c46191bd7708a1ecd4d37d7d1bce84 |
| auditoria/reviews/F2D.1.1-RE-REVIEW-POST-CORRECCION.md | 960d1bea4a7206eb83727198771a2065841d51b0e904a5b2833a9dc264d5132e |
| auditoria/reviews/F2D.1.2-RE-REVIEW-FINAL.md | bbea8a2c8d199a1f0ad22cdc59a9abb197471fcdd35d8a10ca2cb7deaed83068 |
| auditoria/reviews/F2D.2-REVIEW-DOCUMENTAL.md | 27051de1652d1d625e1610bf3c158ae7ea810a4373316ec904aceb151b5673c8 |
| auditoria/reviews/F2D.2-REVIEW-INTERVENCION.md | 832ed54ee4dc0f21b6d1aeda009cb305e450b3a306d3f82145446a0266846aa3 |
| auditoria/reviews/F2D.2.1-RE-REVIEW.md | fbc49e530df34ed01f59fbd60f9a37571602251d562b806c6ffc29298fb82936 |
| auditoria/reviews/F2D.2.2-RE-REVIEW-FINAL.md | af7e008547a45b19a525c6a92ac4344d8c4d28fb734a85179d54f09cc928bd00 |
| auditoria/reviews/F2E-ADAPTERS-SNAPSHOT-AUTHORITY-GAP-R1-DESIGN-REVIEW.md | 9cc0b9da623055b2f02aa42007da7c5ebe9d883a78e159b892df9512f1fb6873 |
| auditoria/reviews/F2E-ADAPTERS-SNAPSHOT-DESIGN-REVIEW.md | 292a5241a4e9ffd383b1b956b80a1be41b49af5cbccf2f3c411713f05188b031 |
| auditoria/reviews/F2E-ADAPTERS-SNAPSHOT-RESIDUAL-AUTHORITY-GAP-R1-PROVENANCE-JPA-TX-DESIGN-REVIEW.md | ae2ce8a4271271df9d401ffdc3a58dc168ddec9291aa6b4dbdacd88dea8a7315 |
| auditoria/reviews/F2E-DETECTOR-READ-ONLY-NUCLEO-PURO-REVIEW-IMPLEMENTACION.md | 513aa7428f3ea09918a919e603e4859e3f17048c75d2ea253312c59a8b929e41 |
| auditoria/reviews/F2E-IDENTIDAD-DETECTOR-REVIEW-DISENO.md | ad7e82ab341d463e60107902f88c353e29cc2112a56379489582bd69fdc0cf3c |
| auditoria/reviews/F2E.1-REVIEW-DISENO-PREPARACION.md | 55495934a19aabc4a36965fcbdf2ebbbf5a7c80eb2cfd739420ac5745dc0c366 |
| auditoria/reviews/HANDOFF-F2E-ADAPTERS-SNAPSHOT-DESIGN-AUTHORITY-GAP-R1-PROVENANCE-JPA-TX-REVIEW.md | f58468207709f5996e104f38d0220b58883c867c318533d54b26808df3e93496 |
| auditoria/reviews/HANDOFF-F2E-ADAPTERS-SNAPSHOT-DESIGN-AUTHORITY-GAP-R1-REVIEW.md | a7259410c83759aed2e2e18ccdb3256e6eae2267ee0600b417d0cbf6b5f33700 |
| auditoria/reviews/HANDOFF-F2E-DETECTOR-READ-ONLY-NUCLEO-PURO-REVIEW.md | 210caafac5f0d5138d012e83002860e1dd069a5275ea88f1588c7a23e1115084 |
| auditoria/reviews/HANDOFF-F2E-DISENO-ADAPTERS-READ-ONLY-SNAPSHOT-CONSISTENCY-REVIEW.md | 82dba97a7a45f54a36479484ed9ce1500e810c05663086ec009f6b1f6f5c912e |
| auditoria/reviews/HANDOFF-F2E-IDENTIDAD-DETECTOR-READ-ONLY-REVIEW.md | 1848db9ae03640c30d381cd0cdb701ef9d24277038abe746d3a5bee9f96e4a18 |
| auditoria/reviews/HANDOFF-F2E-PREPARACION-REVIEW-DOCUMENTAL.md | 2a317c57b0d272d67cd6aedec01ef0a9ee7242911804abdef6ba5b7cc1b60ea3 |
| auditoria/reviews/HANDOFF-F2E-R1-RESERVA-READER-JPA-READ-ONLY-REVIEW.md | a4a3ff1c9f1aaaad4a6d36233a4a4be0b407f0d899fc22f2d1973a10ef19aa61 |
| auditoria/reviews/HANDOFF-PN13-MATERIALIZACION-AUTORIDAD-PAGOS-NOTIFICACIONES-REVIEW.md | 26a0e67588f7a9e5bd13c79aa9006a833d63834cf3cf1a1a19467194e27df5f5 |
| auditoria/reviews/PN13-R1.2-REVIEW-MATERIALIZACION-AUTORIDAD-PAGOS-NOTIFICACIONES.md | da21981cbb4d0925fc7732e645580dd22e5e369938f6b165a0e7009d0eb4510b |
| auditoria/reviews/PN13-REVIEW-CIERRE-PUBLICACION-AUTORIDAD-PAGOS-NOTIFICACIONES.md | 675529f53540a86994b82c041203efb8b1d3b5003c501a74e88e900b9f78e901 |
| auditoria/reviews/PN13-REVIEW-MATERIALIZACION-AUTORIDAD-PAGOS-NOTIFICACIONES.md | cda17b50e562059382f42c46ebe827fedf6519682f95259860be4d0a48215477 |
| auditoria/reviews/PN13-REVIEW-PUBLICACION-AUTORIDAD-PAGOS-NOTIFICACIONES.md | 96378bcc74da86f656d5eaf0a08042e246f6ce6f87bee3e77873679228c17a1f |
| auditoria/reviews/PN14-MANIFEST-ENTRADA-LOCAL-SAFETY-NET-CARACTERIZACION.md | 3d9f96251e13fb43ed2766c63223521390a5569f0a0d544fba9289bd5f4e6dc2 |
| auditoria/reviews/PN14-REVIEW-AUTORIZACION-SAFETY-NET-CARACTERIZACION.md | 377f7c1bef17be075d09cf2a5bf422cdab47f11ce93cdf5ec18aa1bd3d03eec1 |
| auditoria/reviews/PN14-SLICE1-REVIEW-CIERRE-PUBLICACION-SAFETY-NET-CARACTERIZACION.md | fcce395459efd66cbfdb42dba4513fd3af5f4eecdc9cabb878238b06e319554e |
| auditoria/reviews/PN14-SLICE1-REVIEW-PUBLICACION-SAFETY-NET-CARACTERIZACION.md | bdceb0a0418e8895a2b8276a939a264e51d88cba84a5f701cd7739aa67954185 |
| auditoria/reviews/PN14-SLICE1-REVIEW-TECNICO-SAFETY-NET-CARACTERIZACION.md | b7c546a26956995cc7754dfd0701f65a839ea9aab5e7d9837c58099502d42ede |
| auditoria/reviews/PN14-SLICE2-MANIFEST-ENTRADA-LOCAL-ORDEN-SNAPSHOT-INMUTABLE.md | ffea6f1861a0f595336d3376af2f259b26837dd75c3c67d7c2ebbe7a4cd0622b |
| auditoria/reviews/PN14-SLICE2-REVIEW-AUTORIZACION-ORDEN-SNAPSHOT-INMUTABLE.md | 1477ba79459681a195cccad310540952f8f1d84ab1069a87154de340a279c3d3 |
| docker-compose.yml | 6fb250325b99acf75d11bcfd9470cdeb2ef0d601f46f8cc4b91eb94bc02e9e79 |
| mvnw | cae96cef89ebea3531221f4ae17c23cf8edf67d00eae8306d4186ae1bbed4d02 |
| mvnw.cmd | 46eedb8419bd14fe70d5bb2916d7b6f51806e51b39d5b76a42610384ca929c1c |
| pom.xml | 904b68767ace499c7d842a5052d22f1ad93640b06cf3233ee0adf171dc949347 |
| src/main/java/com/feelingpilates/FeelingpilatesApplication.java | ce45c75c3fadc07930792cdc5dad3500a247b57372162089a9c32b5ab4e08782 |
| src/main/java/com/feelingpilates/auth/AuthController.java | 5d294da039891718eb957e5e6b84c1a518d4f3c880dbb4c05350881c561c060d |
| src/main/java/com/feelingpilates/auth/AuthService.java | e9c7b719b6c0005fcfebd263ab0f0c7d4962bbd7bf6eb99cc6b9f59a81bc0f41 |
| src/main/java/com/feelingpilates/auth/GoogleLoginDisabledException.java | 921eaeab246461e78c0d3544ce3d58d076fb44232ede2a3224d1d34bc6e03d7f |
| src/main/java/com/feelingpilates/auth/GoogleTokenVerifier.java | 42e5c9b6d0e6f68b6a1c440ef966b3c7acdb2cd75f13bdf413c23b4ed79e0009 |
| src/main/java/com/feelingpilates/auth/dto/CompletarInvitacionRequest.java | 77726a5c6af84da1b3ed5f450ce31721bfbebd5cdad127887e0f2db2939b84c9 |
| src/main/java/com/feelingpilates/auth/dto/GoogleTokenRequest.java | 3d3050ece86d455c24288a1a3de8dc2c1a5990f72ec299efa954a57cb88b4e03 |
| src/main/java/com/feelingpilates/auth/dto/InvitacionInfoResponse.java | 35bb815004f8a1ae9b2970e3a5c711350f879edf5fe1daddb0b5e89bdb8dddce |
| src/main/java/com/feelingpilates/auth/dto/LoginRequest.java | 30b2870bd3b0bd6ca74fbc9267ff4b175211d1c431d9c1667e049bfb474574f0 |
| src/main/java/com/feelingpilates/auth/dto/RegistroRequest.java | b68717c73d1739959017410ec7d6bbab826a13ecb59e1eebc25f6f4fd1813071 |
| src/main/java/com/feelingpilates/auth/dto/TokenResponse.java | b577f598c25f774f19849afcb2d78a628454e2deea7ca71f5a99592d453f5e6e |
| src/main/java/com/feelingpilates/calendario/controlador/EspecialidadInstructorController.java | c023d39ca3edb47d1809ce00185e16b0c5e8a0b787c9db3447fa9063ca59bb14 |
| src/main/java/com/feelingpilates/calendario/controlador/ReservaController.java | 0dbed16e3f9e8e58ad77b25e3d4047b785515393ecf77f29d6ad8485cea60dc3 |
| src/main/java/com/feelingpilates/calendario/controlador/TurnoInstructorController.java | e33df71d53905116d213c243dac4fdeb1542792e2cf5a291f7b7caab41310d5f |
| src/main/java/com/feelingpilates/calendario/dto/ActualizarTurnoRequest.java | eb62ab9412ec6bde2b40f90ce38d8b62a4ebfc671e353c870660df572eeabe44 |
| src/main/java/com/feelingpilates/calendario/dto/AsignacionInstructorRequest.java | 74b85fde4ddadc1bffbe61c8e020d03e25371d158582ca20b9ac8c2eb7aa968a |
| src/main/java/com/feelingpilates/calendario/dto/EspecialidadResponse.java | 32f8ed7f1e3a34741e7fb3e991ed90fcffdd7757e55e2f66679f564f6d6ca64b |
| src/main/java/com/feelingpilates/calendario/dto/EspecialidadesRequest.java | 51fb656aa62a670dd08c4dcda313b352addd80530bc5c60a665a4b5dafd5fbbb |
| src/main/java/com/feelingpilates/calendario/dto/ReservaRequest.java | cccc6ed55daf4e7dc005b78802c8e4cfb4297e4b8b253db00c06f7ad24f8967d |
| src/main/java/com/feelingpilates/calendario/dto/ReservaResponse.java | 93579b164b9d1d160113550459d9b29b2be06e4cc4f080bb123ef570e284e647 |
| src/main/java/com/feelingpilates/calendario/dto/TurnoInstructorRequest.java | 7ea569478f60ec401f60e399c7b92e433e679a3a7204f2a1f52c902dcc07620c |
| src/main/java/com/feelingpilates/calendario/dto/TurnoInstructorResponse.java | bb4aee2a8574582e86afaea0e0b1d7e06919cc090631304e915892fe062ac5c4 |
| src/main/java/com/feelingpilates/calendario/entidad/Reserva.java | e0ff7f3cfee2ee152ae0e0f9bc815a929224c6d0fc7616736dfe36e612ecab2a |
| src/main/java/com/feelingpilates/calendario/entidad/TurnoInstructor.java | 51b31fc611bbc36794bd4a6685a339eceada545ef90608fbe03844f88706fb2d |
| src/main/java/com/feelingpilates/calendario/entidad/TurnoInstructorAsignacion.java | 9abe42c781e5039183ac74b0b697cea0ce33309d928d560bbc1807c6084da724 |
| src/main/java/com/feelingpilates/calendario/repositorio/ReservaRepository.java | ac5804deed6634182c2b2164e1b03a1ee1a9167043f9f80f41a7dd10aa12cce9 |
| src/main/java/com/feelingpilates/calendario/repositorio/TurnoInstructorAsignacionRepository.java | 5c7fc89c3e8f63656ee06b950c02f8ca308c8264b98dd6bf164ca085aa2a67be |
| src/main/java/com/feelingpilates/calendario/repositorio/TurnoInstructorRepository.java | 33a9d4bfb858e61406d63e18f736c3852bc436723834879ab1b363911f867d3e |
| src/main/java/com/feelingpilates/calendario/servicio/EspecialidadInstructorService.java | 7c23d872bc45a26dd11c3726642cbc8da5ea2e3ec80e8d641cc5e39729ac83a6 |
| src/main/java/com/feelingpilates/calendario/servicio/ImpactoPuntualEnExcepcionHorario.java | 039bb161fa8681439d2574d8c4e6b087822ac310525294ce92119e2c76059734 |
| src/main/java/com/feelingpilates/calendario/servicio/ImpactoTurnosRecurrentesEnHorario.java | 7a464b3afcd76f36c55b9d990ffa67b17ea728ba75814b235c36cdc8748eb100 |
| src/main/java/com/feelingpilates/calendario/servicio/ReservaService.java | 73476b7ccbcaa4e4ab847af4bf93aaa086ad4150b3f1b8b64e11e08df9ae5f51 |
| src/main/java/com/feelingpilates/calendario/servicio/TurnoInstructorService.java | 062074537a230c849b0feb9e5a76dc4a45893f367dee349914a6d97603791871 |
| src/main/java/com/feelingpilates/comun/entidad/EntidadBase.java | 4b5561c6f448edf2a4b8b3d1e33d6763b807b556fbf3248c286252bd3fb1d1b2 |
| src/main/java/com/feelingpilates/config/OpenApiConfig.java | 46a8fbd919825d090272ceaac846d893916cf02da3f054995f37c06eca27db31 |
| src/main/java/com/feelingpilates/config/RelojConfig.java | 28ceba595899c42ebfdb0717b4ec6614fe008d071da4c20959c4c1b92423cc7f |
| src/main/java/com/feelingpilates/exception/CodigoErrorExtractor.java | 2a6362b362f86779501d2ce5e99e7717d24ef08c6ab5e595f83bdc03ecf026bc |
| src/main/java/com/feelingpilates/exception/ConflictException.java | 7949dd22778596b30d5badf648039c8bfe4955bcd0981b07aabc7a7b5ce9a2e4 |
| src/main/java/com/feelingpilates/exception/ErrorResponse.java | 61f8f91b6c699741d25ae30bf7de6b1d035d7a97ea00fb397c0c9914cec58e51 |
| src/main/java/com/feelingpilates/exception/GlobalExceptionHandler.java | 5f04be2a3c8f5c71fa3d952691d71bd09a1baca89026827d08a34e3b2e738707 |
| src/main/java/com/feelingpilates/exception/ResourceNotFoundException.java | 6bb391695c2a5a88dca6eb79f0c1ab9c3f2f806b100eab4b73681f466e6dbe22 |
| src/main/java/com/feelingpilates/exception/ValidacionException.java | d3bdacc8903855fc9040ceb579484679be1ecc9b945706a37e1672279e12476e |
| src/main/java/com/feelingpilates/notificaciones/EmailService.java | 05df60b8ba3a039dca822b94265a0746bc250d0af4a156b6da2b64a10588fbce |
| src/main/java/com/feelingpilates/notificaciones/EmailServiceConsola.java | 25e537faa297ecf759127722b2161fa6c5486b0ef8d6c8647943187acbbaa492 |
| src/main/java/com/feelingpilates/pagos/StripeConfig.java | b8cc75eb9deb84b8f45519252dc5cc503d2b205b1503f902433e79fd0283e9cf |
| src/main/java/com/feelingpilates/pagos/controlador/PagoController.java | 10d4be15ee787cc0f4509eac0ef5fa9cc12bf5679a258f96e3e929000544bfe8 |
| src/main/java/com/feelingpilates/pagos/controlador/PaqueteController.java | 540278e38d6369fccb797657760d25be2a1501d773a5ef301df71776b4cb7e8b |
| src/main/java/com/feelingpilates/pagos/controlador/PaqueteGestionController.java | 426f6fc0b515e2dc02093e1b36391edb6fc97ecc7bfd90eaeced93dd146ea799 |
| src/main/java/com/feelingpilates/pagos/controlador/VentaController.java | da0bcc5f1b64cb5898ff63dbad90e85a79d2cb506af04dec6fb49a0a62af4a5e |
| src/main/java/com/feelingpilates/pagos/dto/ActividadPaqueteRequest.java | 63992b2c649db4163ba7793f66541ce42695312a5a65180d7fd825fbc928a989 |
| src/main/java/com/feelingpilates/pagos/dto/ActividadPaqueteResponse.java | 821d3af0e3880b53fe8967fc42735549ddc49689967913ccd13af821fb1fb4a5 |
| src/main/java/com/feelingpilates/pagos/dto/ActualizarPaqueteRequest.java | 9ca2e68677d3f40b882f1770b25bf1d3bcb65641a6d32af7c5e80910bd472634 |
| src/main/java/com/feelingpilates/pagos/dto/CambiarEstadoVentaRequest.java | c4f72a73c7f9c7a2fef7c2782d973d79178c2b127595360a3efe66f485028f5a |
| src/main/java/com/feelingpilates/pagos/dto/CompraResponse.java | 25ad5d56d7ab4a5e7d10903d2635e91c16c56f9fd0353d23a0878020b5c7c1a3 |
| src/main/java/com/feelingpilates/pagos/dto/CrearPagoRequest.java | ba958dfe6d4b2c7f5a0da526d6296d1832baf79137ac875287f51d247d81379c |
| src/main/java/com/feelingpilates/pagos/dto/CrearPagoResponse.java | 3363344535f9a411c69548131fed540a12e9cd7d66f5e47e4f440367b7e280c1 |
| src/main/java/com/feelingpilates/pagos/dto/CrearPaqueteRequest.java | d16e825e27f2e3fe2b129a7bec48088de363c178a82f9fbeb29e9749100a8036 |
| src/main/java/com/feelingpilates/pagos/dto/ItemCarritoRequest.java | 1ca507bb933a246a8dc22b8e089188991aa6484330d7f82076793ec5217973b6 |
| src/main/java/com/feelingpilates/pagos/dto/PaqueteActivoResponse.java | 6a207cf2db354a0203cb90fc02ba943c5e3b32b7e5295920e8bed2f989d980d4 |
| src/main/java/com/feelingpilates/pagos/dto/PaqueteGestionResponse.java | b8a97ad5357f0b0db69fbdf445a460efb7f48b07469a8260e1fd23971e4aa30a |
| src/main/java/com/feelingpilates/pagos/dto/PaqueteResponse.java | c7c9f2333c294231b055c5f25964a7ad565d3e6955fd9ab9a318c09956b52d29 |
| src/main/java/com/feelingpilates/pagos/dto/RegistrarVentaCarritoRequest.java | 3d416f06a5d3d21095428415e1f50ea0b914cd3dd091f0f06db5678e5f7d8e45 |
| src/main/java/com/feelingpilates/pagos/dto/RegistrarVentaRequest.java | 89a6a158b4fb7e5e86b60a04f0894b5f19a540ecb66ab58c81939802d798923c |
| src/main/java/com/feelingpilates/pagos/dto/SedeVentaResponse.java | b0d1c785c7ce5e4d0bb732886f5a813f1f07efe7a295dabee3dbed37c134d006 |
| src/main/java/com/feelingpilates/pagos/dto/VentaCarritoResponse.java | a41bada5044f4ed97bbfce68c05851feec18a63388e64f5c032a43745c56d67c |
| src/main/java/com/feelingpilates/pagos/dto/VentaResponse.java | 7b993d203b2b0bbc1ceff4b561425303bbd7fb1fb5e490c571dcd4d720e8169f |
| src/main/java/com/feelingpilates/pagos/entidad/Compra.java | 28cd47b610e41217f61f8240e81a555dc35e330c2b133413d486645233231d0b |
| src/main/java/com/feelingpilates/pagos/entidad/Paquete.java | 4a09f9cec8367919a9c5c21c9cc12743946df8a61bf553ad6f52b80a553b6f0e |
| src/main/java/com/feelingpilates/pagos/entidad/PaqueteActividad.java | 42a9c1f4f593c65e4e133171aea1824368e50279144c6e9727123403a1b18735 |
| src/main/java/com/feelingpilates/pagos/repositorio/CompraRepository.java | dcd28774a8d95b075175280b940ec945fbc3f013ab6c450d83ddc1e8551979cf |
| src/main/java/com/feelingpilates/pagos/repositorio/PaqueteRepository.java | ba0b278edc6e22dca2c2b54a8622d3592fb7b481b1dec29a610c2513e0d427a6 |
| src/main/java/com/feelingpilates/pagos/servicio/PagoService.java | fe62d2ba9e6d8cb57e953ace83b2a607aa9960cd61c216a5f07b403446998686 |
| src/main/java/com/feelingpilates/pagos/servicio/PaqueteGestionService.java | 36e57c522e5e2d098376c79a9814905be87e1bd56c97461b20626532d5f87f45 |
| src/main/java/com/feelingpilates/pagos/servicio/VentaService.java | 42af8081d32d74c5425ba2406b221502de3cdfe053e64d177314431940959e0f |
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
| src/main/java/com/feelingpilates/programacion/dominio/OcurrenciaEfectiva.java | 26ec805223fb304e818d63d920924c4ce2e66419e9e00a7d0bc0835d02841d56 |
| src/main/java/com/feelingpilates/programacion/dominio/OcurrenciaNominal.java | eacbe71b4bfabc80b7e155ded6ac0a3426c748a4383d6e63041bc6e21a054379 |
| src/main/java/com/feelingpilates/programacion/dominio/ProgramacionInvarianteException.java | 58ec3f758940d0f3c915bb46d822dca480f1dad4e02444019ddac63bf756fffd |
| src/main/java/com/feelingpilates/programacion/dominio/ReferenciaOcurrencia.java | 611d34112c796097c28835b488769747146be65f8f1a33f1485fdec7e4c5b82b |
| src/main/java/com/feelingpilates/programacion/entidad/AjusteProgramacionFecha.java | 8b56681e034897bf94540d71a147a79844e9807f24908d9431e74d0b748b2f66 |
| src/main/java/com/feelingpilates/programacion/entidad/Asignacion.java | df94c5b78b63cfefdec1d382ffb0b1a422cb03f198d17d02a145ece7c4514a9c |
| src/main/java/com/feelingpilates/programacion/entidad/BloqueProgramacion.java | d74931a06f5bbb8f9914ee0952fc1ace970a4797a2ea7e8e4f0551d31ff6e5f4 |
| src/main/java/com/feelingpilates/programacion/repositorio/AjusteProgramacionFechaRepository.java | b445f3a4fd90568ac55089dff711d933f54e97f408d9ec8acd0769e16ff7cfe2 |
| src/main/java/com/feelingpilates/programacion/repositorio/AsignacionRepository.java | 1231b32b9cd2951ace1fa5c09dc18f0732134f58de498a9f3bd8014764c65db2 |
| src/main/java/com/feelingpilates/programacion/repositorio/BloqueProgramacionRepository.java | 3d352580a0e1411ae3faaa9870003109c100e1cd3ad8d3ff8cf0100b3c610adc |
| src/main/java/com/feelingpilates/programacion/servicio/AjusteProgramacionFechaPersistence.java | 6707a0bbf804a46fcabce0d9b93da40cf41de1c7fb0871b0153974563db77608 |
| src/main/java/com/feelingpilates/programacion/servicio/AjusteProgramacionFechaService.java | 924bf3d9a47ceef7b3a36b50dd253eabb379736ee8024c38baddceb0b7865e84 |
| src/main/java/com/feelingpilates/programacion/servicio/AplicadorAjustesProgramacion.java | 48bb9c3e5de6ff60c42a169741017cd72720f24dbc016df38c83eca12548723b |
| src/main/java/com/feelingpilates/programacion/servicio/BloqueProgramacionService.java | 88401fa65d46effae9204be90f9fec5de3d4fd80aedad7bee1766448e0f03f88 |
| src/main/java/com/feelingpilates/programacion/servicio/ConflictoAjusteProgramacionException.java | bb88c219702a132c7d5f0810ae3fbb16752520c81b202a5b5972ebebbdae9532 |
| src/main/java/com/feelingpilates/programacion/servicio/ConflictoAjusteProgramacionTranslator.java | 4c2475ec4d64a28e5803301efae35ebb9a3c0aa93c1b19f7ac2086218a77d12d |
| src/main/java/com/feelingpilates/programacion/servicio/ImpactoBloquesEnHorario.java | 44a9b27988bf3aecf022142455fac29322405ba7ed1efacd942820b05e69eb5d |
| src/main/java/com/feelingpilates/programacion/servicio/ProgramacionDiagnostico.java | fc138f968dac91b48c9fd4dd1fe6e9b74cc192d62794ca0c0c35b2977031ffd6 |
| src/main/java/com/feelingpilates/programacion/servicio/ProgramacionDiagnosticoSlf4j.java | b9b3ed7c9bc68c555aeb855bfc855858ee0f9bd06abe1f7b6100f5d07307868f |
| src/main/java/com/feelingpilates/programacion/servicio/ProgramacionEfectiva.java | 275df044f2f7dc006ccc8da3c3162ed48de165dd23901bd729235b7275ab752a |
| src/main/java/com/feelingpilates/programacion/servicio/ProgramacionErrores.java | 82e43df533785c7c8b28c0465927e9b2d97fab302729a19d5934842c268449a5 |
| src/main/java/com/feelingpilates/programacion/servicio/ProgramacionNominal.java | 77b69e8b8fdcd95a327c99f4981346c5f717fa317f46bca52fc74630c2bf9e75 |
| src/main/java/com/feelingpilates/programacion/servicio/ProgramacionPolicyA.java | ea3ab95a2c0afeeee59b87f51dd41309c989304587d132b7f7b91fd50df55307 |
| src/main/java/com/feelingpilates/programacion/servicio/ProgramacionValidador.java | 99548ec93254874cb5ea1c184ab38aab0ec09ae5d081a14eb9bf727a11d17edf |
| src/main/java/com/feelingpilates/seguridad/AutorizadorSalon.java | ace316cc5f9012976b56608dda40e0faa0d8462358b4f20a458e52e1de5710ab |
| src/main/java/com/feelingpilates/seguridad/ContextoAutenticacionService.java | fdd43b4dd1dd8d9ea3a3d560e5eb7c66e306768cbd37cb903c6c3d30769947fc |
| src/main/java/com/feelingpilates/seguridad/JwtAuthFilter.java | a3ad8a5dcec23beb2d694d041611197f32bf5b090c4bd22d7c3bc1503b5dd25a |
| src/main/java/com/feelingpilates/seguridad/JwtService.java | 27532723264a858539074bcf0c70e2c53d6f942988244668b8e504edd415af7c |
| src/main/java/com/feelingpilates/seguridad/SecurityConfig.java | 530aff488fea0f15628e523b5e9fdc2fcee5c2fa679fcd520073117d94c2786b |
| src/main/java/com/feelingpilates/seguridad/UsuarioAutenticado.java | af281255a69f0194068c6bdd501ccee99c43568552286c17074caabcd945d7a8 |
| src/main/java/com/feelingpilates/transicion/programacion/detector/CandidateEvidenceGenerator.java | 200c756d133797c91825e75bb83f67a6816ba775636ed8bb9d408228fb54e4b1 |
| src/main/java/com/feelingpilates/transicion/programacion/detector/CandidateEvidenceItem.java | 09bb9e5167db7215d5bd9a5d7216c805188abf6a85e7c3acc698345566ca68f4 |
| src/main/java/com/feelingpilates/transicion/programacion/detector/CandidateGenerationResult.java | c4fbcd9e6be2ebd657c40b67fc2e0e5a268bdbfefe6f4dc465ff7faca31b1a2c |
| src/main/java/com/feelingpilates/transicion/programacion/detector/ClassificationContext.java | ac96d09dacf2c6ca154599e7419d58f63dc5514af24dfd029b733c8981405a60 |
| src/main/java/com/feelingpilates/transicion/programacion/detector/DetectorCandidate.java | e290b93622349d89d20846af5db8569baacb502bae5737b76a8d2cc676a9e24a |
| src/main/java/com/feelingpilates/transicion/programacion/detector/DetectorClassifier.java | 9d9ca605f5d8b5e6c954d3b7ee79c21091bd53d5302f1bc7523dc237f837c586 |
| src/main/java/com/feelingpilates/transicion/programacion/detector/DetectorEvaluationRequest.java | f0cca9293b5882a5e32fbc6909b2f499b8a52089735720743e3c82b4ed74e774 |
| src/main/java/com/feelingpilates/transicion/programacion/detector/DetectorInputInvalidException.java | 07012791b58315c5f4e29abe77e0d87434e110e32e6ced9631216a729c820571 |
| src/main/java/com/feelingpilates/transicion/programacion/detector/DetectorResult.java | 7beeed3a8eeb9583c923f6f00a489b9c73ae8aea844556deb2e1e5e38f1cef7c |
| src/main/java/com/feelingpilates/transicion/programacion/detector/DetectorValidation.java | 4ba05ba4af1a9820d55ec745a55b542bf63ebdb2d29a67bd740e94bc211d94c9 |
| src/main/java/com/feelingpilates/transicion/programacion/detector/DetectorVocabulary.java | 4123e78b89872daeb0d9fb03e8d49e5e732041f828e14b13f36fb187546d11f6 |
| src/main/java/com/feelingpilates/transicion/programacion/detector/EvidenceProvenance.java | ba4312f10c96b1268e45e75895f3c818b9ab03afa7df22a09989785a7738bf83 |
| src/main/java/com/feelingpilates/transicion/programacion/detector/F2DAuthorityGuard.java | fd178edda27b75dac42f6120c8a7d661e44d283becf25ed4a431e87e17e820be |
| src/main/java/com/feelingpilates/transicion/programacion/detector/F2DCompatibilityResult.java | 39596944ec86761e58c4164db1c80dcdcc97638f8b8d74f7bce74c3b2f417e8d |
| src/main/java/com/feelingpilates/transicion/programacion/detector/GenericSourceSnapshot.java | 36de8fe63df709e6213e0e2b9d2fef9b05d9f66afee768596ffc8377bc75348a |
| src/main/java/com/feelingpilates/transicion/programacion/detector/HistoricalProgrammingTargetSnapshot.java | 1ca4586d116f0568dffc717667e5dddae8562947d344de5944ef3c5bb3a69d84 |
| src/main/java/com/feelingpilates/transicion/programacion/detector/ProgrammingCandidateSnapshot.java | af5adc2977a6936301452f9d461b8c54123e0a4fe1ba5943ecfecce57e6275f7 |
| src/main/java/com/feelingpilates/transicion/programacion/detector/ReservationSourceSnapshot.java | 7856887d858fd7658eda6c68f54bc995d0481fd4257cb9417265c90cf144c293 |
| src/main/java/com/feelingpilates/transicion/programacion/detector/ReservedSubinterval.java | 3ea117a444d4cd169a7d6e88769245aef6f7e9bb1d76874bb35d74762b724c1f |
| src/main/java/com/feelingpilates/transicion/programacion/detector/SemanticHash.java | fc92a2a1f489ee7a69601f33764e86d1fda1a4220f82c21081e4a8f5e8b218dd |
| src/main/java/com/feelingpilates/transicion/programacion/detector/SourceSnapshot.java | eedd2c098e2adf1b6d90e2bfabce4227ad434d23b1f272376b66edaccd5a79fa |
| src/main/java/com/feelingpilates/ubicaciones/controlador/ActividadRecursoController.java | a8e32dce32810ce2ecb5954b217f74b71d5d78128ec3c69c02377de046da0ecb |
| src/main/java/com/feelingpilates/ubicaciones/controlador/SalonController.java | 95f0b7f5070a422908da6862b3648d1f05086aca697ece24d268b5fd670a52f8 |
| src/main/java/com/feelingpilates/ubicaciones/controlador/SalonHorarioExcepcionController.java | a002dd514bfbd0c4d4a2825bb21fd4de1f2b22f8704f69de946393341e429cb4 |
| src/main/java/com/feelingpilates/ubicaciones/controlador/SalonHorarioOperacionController.java | 9bfc794dbb1156f579e11346d51f71dbd7049181161faf451e8288981b7299bb |
| src/main/java/com/feelingpilates/ubicaciones/controlador/TipoActividadController.java | c065761cf8ac0c4044d2bb06d93d4c682e1eb8d5c17fefaac8ab052093abd980 |
| src/main/java/com/feelingpilates/ubicaciones/controlador/TipoRecursoController.java | 3c7a3cfd06272dc5c65586a1129773491e343623c59c5f5292f8b33817d587b5 |
| src/main/java/com/feelingpilates/ubicaciones/controlador/UbicacionController.java | 4de4a95d4efdc65fb767684efd2784a9b80e33a146f945cf7aa049e3380db36d |
| src/main/java/com/feelingpilates/ubicaciones/dominio/CambioExcepcionHorario.java | 099c01d31110531cabde19d5193ada4f6d765c4ec581a13cc41dca9fec92ab27 |
| src/main/java/com/feelingpilates/ubicaciones/dominio/CambioHorarioOperacion.java | b5dd831f58700bd26f62baad34cdaf6d589c6d9969315b2f52cfb6a9f71b4d2b |
| src/main/java/com/feelingpilates/ubicaciones/dominio/CoberturaVigencia.java | becf8209f388911f6fbf147cfade494c72776c1d68c919e7a69ed0c1b2633539 |
| src/main/java/com/feelingpilates/ubicaciones/dominio/ConflictoProgramacion.java | 638487b31a53257f48a55eba915cc628a925d68ca685279529e5e2e0a723cb64 |
| src/main/java/com/feelingpilates/ubicaciones/dominio/ConflictoProgramacionPuntual.java | acc25f526eef0f7b34404b5a8a47ded058324dc2d179d268d0d74c302f72186d |
| src/main/java/com/feelingpilates/ubicaciones/dominio/DiaSemanaOperacion.java | 82e55c361a3bb7a0db017949a2ca720862df63e3b6d15b93b7b3667ecb7de91f |
| src/main/java/com/feelingpilates/ubicaciones/dominio/HorarioEfectivo.java | 4da178437797f072784a4237eafcb32538980940e5ad17b5744c7898fee99cbe |
| src/main/java/com/feelingpilates/ubicaciones/dominio/RangoVigencia.java | cbb81f643dd588d9b4780009335c5abaeea10363acfc5fe726f68c07df8eb5af |
| src/main/java/com/feelingpilates/ubicaciones/dominio/ValidadorImpactoCambioHorarioOperacion.java | 3cbfebe6e84881df152f164cf0886f54603a18452c62bf714e33fec3a6d2aba4 |
| src/main/java/com/feelingpilates/ubicaciones/dominio/ValidadorImpactoExcepcionHorario.java | 9b25af23a322e5477189f88a1084736a4053f7bc911d62f06c6ecd3ff17222c0 |
| src/main/java/com/feelingpilates/ubicaciones/dto/ActividadRecursoRequest.java | 42195e7efac33f4ced92a7af6c7c73017920c60eea8feda6f04d2c0b58641dac |
| src/main/java/com/feelingpilates/ubicaciones/dto/ActividadRecursoResponse.java | 5242faa3790ac5fbba53dab5ecee60eb7dc644b9b74643a57f5c25d00e451bdf |
| src/main/java/com/feelingpilates/ubicaciones/dto/CatalogoItemRequest.java | f7801673ffff0585ab2724cca2d77e61f2e881c8cb99e97c13492b5e0f0f3ee8 |
| src/main/java/com/feelingpilates/ubicaciones/dto/CerrarHorarioSalonRequest.java | 1b5f212fd2b3083ba95e908111d51ee90f04204779062d8475f64ecaa5673b64 |
| src/main/java/com/feelingpilates/ubicaciones/dto/EstadoResponse.java | cf2daa0675a5a3eb786ddeb298e295fd9c122f35ab70473a90866585c04cc2da |
| src/main/java/com/feelingpilates/ubicaciones/dto/GuardarExcepcionSalonPorFechaRequest.java | 23afa0ee27aa8f533bce1794cea7b47254f8e9697f50ac3632092c000e835a84 |
| src/main/java/com/feelingpilates/ubicaciones/dto/GuardarExcepcionSalonRequest.java | 4f3450c2c1304dc44f6b6406fa532ec92e0a3c20249b8e1efc4f84810dc780da |
| src/main/java/com/feelingpilates/ubicaciones/dto/HorarioOperacionRequest.java | fa0c890eeb2c1e8561287bafd89d5a93186fbbe5e70cf014950b80922c82f8dc |
| src/main/java/com/feelingpilates/ubicaciones/dto/HorarioOperacionResponse.java | adc6a0f18b59b6de278b42386d0e293e7bedd5c7c0eeb7fabb03582adfe69df8 |
| src/main/java/com/feelingpilates/ubicaciones/dto/HorarioOperacionVersionResponse.java | b44112fd39a06f91176679b7669ce48e8e7b3d117c65ab111b1d7bb4f1144ec5 |
| src/main/java/com/feelingpilates/ubicaciones/dto/MunicipioResponse.java | 0e4d3eacb2c68b89145d3eb200993b57cbfb871223d90b229c38af92e8f0e65d |
| src/main/java/com/feelingpilates/ubicaciones/dto/RecursoItem.java | 8838976864b2ee82ac743b857b547c5785106808048112fecb74815eba535350 |
| src/main/java/com/feelingpilates/ubicaciones/dto/RecursoItemResponse.java | 55789a41a9bc99d48027354ad576b444c9351f039cec811679141a1a65c02286 |
| src/main/java/com/feelingpilates/ubicaciones/dto/SalonDetalleResponse.java | 7e03157b97500c37beeb88cd136e6bc973ad4cb8bb80a9e3af2c91d76e115525 |
| src/main/java/com/feelingpilates/ubicaciones/dto/SalonHorarioExcepcionResponse.java | 2bfef73dcfe1881c174b8db4fa03018617b8962e1edbc75469fceb4f30444665 |
| src/main/java/com/feelingpilates/ubicaciones/dto/SalonRequest.java | bfae3f2d7a664855fd34a81f075c6f6f4c0d89a410d5a45fb8a559d944d8a12d |
| src/main/java/com/feelingpilates/ubicaciones/dto/SalonResponse.java | f4927da181e5849a91bcffc5eca6ca8a843714dbc836f1e3d061ac008642dd91 |
| src/main/java/com/feelingpilates/ubicaciones/dto/TipoActividadResponse.java | d18f07cf6ac1121202e718fe7482a07e6f1fb8adf58336e06ccfa50caf03bc2f |
| src/main/java/com/feelingpilates/ubicaciones/dto/TipoRecursoResponse.java | 776ec8dd3ad42a459408eb37bf0b6f56d55768a68a5b896c6a6acbb2559f296d |
| src/main/java/com/feelingpilates/ubicaciones/dto/VersionarHorarioSalonRequest.java | 3801a84e23a771180022b9c5b27dcb4f4766ebb70fe13d22d5667470d68d47fb |
| src/main/java/com/feelingpilates/ubicaciones/entidad/ActividadRecurso.java | a5a132051e17bd60a472b82e448d8f3ae478398a5cf9d37a9f3578b215ed6c93 |
| src/main/java/com/feelingpilates/ubicaciones/entidad/Estado.java | 40362dfb120e8ced3cbf884d62f03e775f82cb513728d5e1ce819a013748da24 |
| src/main/java/com/feelingpilates/ubicaciones/entidad/HorarioOperacion.java | b46f5347dca93f0b28cb985a190c98ddd8f7a06a84049314b6d1b1a4afb48c1e |
| src/main/java/com/feelingpilates/ubicaciones/entidad/Municipio.java | ca6fb3375b294a89fb1cf419d7a8586709951af768f2d15d08d39439974efdb8 |
| src/main/java/com/feelingpilates/ubicaciones/entidad/Salon.java | f11a0ff568f6dbaa2495bbf4d573932fb9ce0c88df1b2bd88227e029d2543e92 |
| src/main/java/com/feelingpilates/ubicaciones/entidad/SalonHorarioExcepcion.java | 6143ec922fa2d105d4984df6445d5f253773ae10f7d7f1dd58e1d3a5178b21ee |
| src/main/java/com/feelingpilates/ubicaciones/entidad/SalonRecurso.java | 082cc2c125b2be4d56f9fd52fe90ee1174e9493d87ee41b1d12cc6ab01c8170d |
| src/main/java/com/feelingpilates/ubicaciones/entidad/TipoActividad.java | 358452407559aaf18fe2b14c6ff40a02465cfbc6a6824e2f5f0c2a2b6a3322ea |
| src/main/java/com/feelingpilates/ubicaciones/entidad/TipoRecurso.java | af65fb9f859dc598a0786f292ee6c218086e0360ee29c2fb00b07ed8f61b57b7 |
| src/main/java/com/feelingpilates/ubicaciones/repositorio/ActividadRecursoRepository.java | fd92ed9491b88dd74ad94d215df5ed06347df32f0e723a2ccbe469d5efd2ca75 |
| src/main/java/com/feelingpilates/ubicaciones/repositorio/EstadoRepository.java | 1114d5d1770470fe9e32060f684dce9b1fe06561414c4e7b0360c33928792375 |
| src/main/java/com/feelingpilates/ubicaciones/repositorio/HorarioOperacionRepository.java | 1b1e0a11453524a6176dfaee7cc2e5f50aeb22767ac53b5c42cc0531d6e60448 |
| src/main/java/com/feelingpilates/ubicaciones/repositorio/MunicipioRepository.java | e46ed8cfdc4b287ee7943123727dd95afbf56dc1a7bbd31ee89764648a547a34 |
| src/main/java/com/feelingpilates/ubicaciones/repositorio/SalonHorarioExcepcionRepository.java | c4335e795fb3c939c623c109c801d21328faa615c5da6bc82b0bbd00d29a27ac |
| src/main/java/com/feelingpilates/ubicaciones/repositorio/SalonRecursoRepository.java | c767945d18c7626cc3d6af23c4af2f28e06e538ed9fc817b750ae98fb2c3f91b |
| src/main/java/com/feelingpilates/ubicaciones/repositorio/SalonRepository.java | 30b642c40bf10711c1ef8dd93c2d18cbba07cbc9e77b72edcd55f87285de053d |
| src/main/java/com/feelingpilates/ubicaciones/repositorio/TipoActividadRepository.java | 58b790e54a68b0cd0eb43e15b230bf68fba9f65697ed91ce5926b3b263c479e9 |
| src/main/java/com/feelingpilates/ubicaciones/repositorio/TipoRecursoRepository.java | 465cf9193fe39e8ca1826708d5672890428141dac42051f0ccdaa5e7eb44f379 |
| src/main/java/com/feelingpilates/ubicaciones/servicio/CerrarHorarioOperacion.java | cd0e6c09b9293167b9f99a6ae5650e31b4667ebca7a8c8b7a04751bb6fabdbba |
| src/main/java/com/feelingpilates/ubicaciones/servicio/ConflictoExcepcionHorarioTranslator.java | 973869fb10b73b73d6449bc5ece39b6447edfcc8d0ec08b7162ba11ea078b9e3 |
| src/main/java/com/feelingpilates/ubicaciones/servicio/ConflictoVigenciaHorarioTranslator.java | 3497d67418bcd59592c79240f5d743608067c2a1aecf35ccdbd778e0a6fec2d7 |
| src/main/java/com/feelingpilates/ubicaciones/servicio/HorarioEfectivoSalon.java | 065e544c912e283c7800d109d4ec2b2f9f6016af40b272cf2bfa242db4c4c970 |
| src/main/java/com/feelingpilates/ubicaciones/servicio/HorarioOperacionErrores.java | 38b41d9e23e731f3e00484fbb43937cc17d00eebb9f3816658e427c8428f43b7 |
| src/main/java/com/feelingpilates/ubicaciones/servicio/HorarioOperacionResolver.java | d90e18e46797830de52caf32dde6e36b343765a40a6cd909a8c6bd74b766a339 |
| src/main/java/com/feelingpilates/ubicaciones/servicio/SalonHorarioExcepcionErrores.java | e84335c04f9075a917254b7f87468390fc801c6a775136dd7ed42b9c787e2d23 |
| src/main/java/com/feelingpilates/ubicaciones/servicio/SalonHorarioExcepcionService.java | dd20b1f87cea434940aa23237935bf134164887d1d2248970eb6bc9339bb67d3 |
| src/main/java/com/feelingpilates/ubicaciones/servicio/SalonHorarioOperacionService.java | 4d80e324c4c01408892b0d0ebf4199ec594f2b6ee05bc0ceefdf8efde0a17e9d |
| src/main/java/com/feelingpilates/ubicaciones/servicio/SalonLock.java | f79c565e21296f30c5651cadd1db9d35a499e07e214625705ffc98246d0ab29d |
| src/main/java/com/feelingpilates/ubicaciones/servicio/SalonLocks.java | de5ed5431dfcf854825652362ddf5f95e4d351da123a5beee76ae69df3069fd2 |
| src/main/java/com/feelingpilates/ubicaciones/servicio/SalonService.java | f1263f89419198e0ed2ea800c9b47ee7ec34b5a7161f55e1ab6aa7af5d269d36 |
| src/main/java/com/feelingpilates/ubicaciones/servicio/VersionarHorarioOperacion.java | 051b682acdf7b5682657f43746f2bdd4f383885b0d73784a43811aad5f3be3a8 |
| src/main/java/com/feelingpilates/usuarios/controlador/AdminUsuarioController.java | 87a1485a78cd6437055a61e5366938252f55de435ebb002bd810302645dba459 |
| src/main/java/com/feelingpilates/usuarios/controlador/PermisoController.java | 29187df18c7a5b7e0a02efc876005f9cf6e3b2d42075c80b1284d5972b907d95 |
| src/main/java/com/feelingpilates/usuarios/controlador/RolController.java | 2d6635c7632af589e044f310746f6e82fc4a25c8b40855d107c36d679e820c68 |
| src/main/java/com/feelingpilates/usuarios/controlador/UsuarioController.java | 10ff264a5ee460db9c9c390e404f1d3612daf05dd968773c3aa44c30937d1d3b |
| src/main/java/com/feelingpilates/usuarios/dto/ActualizarPerfilRequest.java | 4e93d69e3f939d5278caa6cdbe8a925e9b38f80bad2f2d9c6ee49e2e140580f6 |
| src/main/java/com/feelingpilates/usuarios/dto/ActualizarPermisosRolRequest.java | f7c246a3b326fac444d3b1bdc0db575163679b60d323ff68d58e86ba7b05cbcb |
| src/main/java/com/feelingpilates/usuarios/dto/ActualizarRolRequest.java | f6a33d368451c129901fd95fc5ceaa6cb6acef80a5eb8a485293a94a1ed3c5cb |
| src/main/java/com/feelingpilates/usuarios/dto/ActualizarSedesRequest.java | ae4d3bd2d0dd95dcb11987b32270d3d8cc7109dcf1c9de1c766c899373ce150b |
| src/main/java/com/feelingpilates/usuarios/dto/AltaPersonalResponse.java | d28cd7fb4b4333136157a5c20ebcb6474848bd447f047b6f78cfe5e9f8d9a094 |
| src/main/java/com/feelingpilates/usuarios/dto/CrearClienteRequest.java | 3c4282bed078a8e3d0148c046b001f19a5673406c7813fa7a181785d98612cfa |
| src/main/java/com/feelingpilates/usuarios/dto/CrearPersonalRequest.java | bb532d0f46b717355377a400983972bbc28c61fcc228d142546c0d7ceebf8ca1 |
| src/main/java/com/feelingpilates/usuarios/dto/CrearRolRequest.java | 45708a047d437fbb654ca9cb3b930ca9f6dd441ecde7eda478ff47582bcbb0e4 |
| src/main/java/com/feelingpilates/usuarios/dto/FotoUsuario.java | cef8cf8d4435bb379a3f3d39d0cb4880e58f4b212d0ed875268d0cf82e113f16 |
| src/main/java/com/feelingpilates/usuarios/dto/PermisoResponse.java | 2eb93798a940a9887bea7da79fac874120b2213a326c34ec420950e5284bafa5 |
| src/main/java/com/feelingpilates/usuarios/dto/RolAsignadoResponse.java | 1717ef7a406520e86c4a7f23d39300f91193573e7d60adce3debec1674d8dbc8 |
| src/main/java/com/feelingpilates/usuarios/dto/RolConteoResponse.java | 020913a2e5c33b468834d2a55b415f583587a945dc0800edcfdd505a79b4e561 |
| src/main/java/com/feelingpilates/usuarios/dto/RolResponse.java | 4de17b45d1d0247f06a9feac369e1c5eff448bfa1e5bbf21370aecfbd063f670 |
| src/main/java/com/feelingpilates/usuarios/dto/UsuarioResponse.java | 4ee3f4b8918ec9b9e1d058b4d40fc38c9864fd95f2bcee009b74ebe1928532b8 |
| src/main/java/com/feelingpilates/usuarios/entidad/InvitacionUsuario.java | 18466ef4b9b9e891f988dabce32d2ff5b2674a9594e54747484b4c3739e39d0b |
| src/main/java/com/feelingpilates/usuarios/entidad/PerfilInstructor.java | 9f77c77089b29e3ef615d83c443663d1615df5e034293a4d0812fc40a44c175f |
| src/main/java/com/feelingpilates/usuarios/entidad/Permiso.java | 49595916cd73f19fd58789825d9d6f6d031435bde15f26502afa21547bc278ca |
| src/main/java/com/feelingpilates/usuarios/entidad/Rol.java | 3c572b3890742009c79aa62e2fd74a31ee6963cb96bbc2dda5bddc91018fd7e6 |
| src/main/java/com/feelingpilates/usuarios/entidad/Usuario.java | 616d67f14e5799c826ff9acb72566b2ac095f5471274dcce7eff88161a3e3a36 |
| src/main/java/com/feelingpilates/usuarios/entidad/UsuarioRol.java | f7dd388628d9679ef9198d0477c36b6f59a23b1a58a9fae4628c8908d8bc36a5 |
| src/main/java/com/feelingpilates/usuarios/repositorio/InvitacionUsuarioRepository.java | 2a105e4d6dd7fb46d32fcb6a44979917eeb37d0eaa461ec7d744dbe97ecdb564 |
| src/main/java/com/feelingpilates/usuarios/repositorio/PerfilInstructorRepository.java | bc33d0cce03eddf925069f35fccfd3ab9a22a955c467b352d4cd039d75b8060f |
| src/main/java/com/feelingpilates/usuarios/repositorio/PermisoRepository.java | d9e587730314f0a784a1c8d41820e6e4dde62c372492421f878bfd1d693e0bab |
| src/main/java/com/feelingpilates/usuarios/repositorio/RolRepository.java | b32f30132287297609b581da6a86752a0578c6e9f939dec78bf2323c079e39f2 |
| src/main/java/com/feelingpilates/usuarios/repositorio/UsuarioRepository.java | e95625627bdf76b1f177c5a1214bd96bd18c6319086b95438ba6281ccda2ad4a |
| src/main/java/com/feelingpilates/usuarios/servicio/AltaUsuarioService.java | 81fd33faca54c0794079606d4021838adbc1a19aef356e6b021325468eb5b6fc |
| src/main/java/com/feelingpilates/usuarios/servicio/InstructorLocks.java | 58d98aa3a53f4533475cb7a042129f291c2edea7a80089943a97bea1551f3b91 |
| src/main/java/com/feelingpilates/usuarios/servicio/PermisoResolver.java | 8c6edc535678f48d2c7833cfa1893e2264aebc223bae3558b035a9e7a95130a2 |
| src/main/java/com/feelingpilates/usuarios/servicio/RolService.java | 2395f30507e4e45b02803f8531823c06be9f73e3f569a22103244c12bd517d57 |
| src/main/java/com/feelingpilates/usuarios/servicio/SedeRolValidador.java | 043064bb798d94b22cb0a46c6a9f4b100b51485a6b8137b1519581de448f075a |
| src/main/java/com/feelingpilates/usuarios/servicio/UsuarioService.java | 9be863a69c376d884ccbcde289d50023e771a837f08501fb6c7cd17217c1c031 |
| src/main/resources/application-dev.properties | a9f8e82daea14f4f11ce14f68a183a7d484615fcbce79fe163059d6c9b765a8f |
| src/main/resources/application-prod.properties | b36a3c6ce3d860af97816c1f6160f38857f9bf80d8f18d7a42c8b49e7340fcf0 |
| src/main/resources/application.properties | f07b8b033f0fc9d23926b7f33352c7453182b4664acbc593f6e6862cc90b5f96 |
| src/main/resources/db/migration/V10__salones_semilla.sql | 0806a7d947f5266a2a86ee4494885ca4413ffcb46a0d5f88c4044a07f6780f76 |
| src/main/resources/db/migration/V11__salones_gestion.sql | 686184cebdda9003c51f45ab0cc6c7a486c4511026dea1e71892fbe75985e59b |
| src/main/resources/db/migration/V12__salon_direccion_completa.sql | baaf4378963894eb972917767172297130ee59e204f84fc9962d10e8129f5c3b |
| src/main/resources/db/migration/V13__usuario_foto_binaria.sql | 01a04f3919be365a770b273a00168616a0cfb33412ab711ec493edd4c217b4ef |
| src/main/resources/db/migration/V14__salon_inventario_maquinas.sql | 7ef8be8cfd0426ba643b3c43bc848812c92c063d2ffe3681024c34eb8dd843e8 |
| src/main/resources/db/migration/V15__calendario_instructores.sql | 956e8530816d830756bbc8c477b61105b26f5e2e3bc0d96d468d748a87796310 |
| src/main/resources/db/migration/V16__permisos_calendario_granular.sql | 161edc552853d6c83464b5787f7f5065ab8577d7454f27bdec5e7fa2f5c006da |
| src/main/resources/db/migration/V17__turno_instructor_actividad.sql | 43b1ab7f41feaddc9bce6961581cd2f99f8a9c90e482ce7f8ba5fd29c544190f |
| src/main/resources/db/migration/V18__salon_horario_excepcion.sql | 453b8e5afff4a20c3d2234ea311485a7fee77efeb7a6333dfe6417d8126f7627 |
| src/main/resources/db/migration/V19__turno_instructor_multiples.sql | 256199bce25ef692f444753307c2723149cee449f8ee4082a18fa57389d0d0b1 |
| src/main/resources/db/migration/V1__esquema_usuarios_rbac.sql | 99481b564267ac0c5e5e2f4aa1175140ce24a6213e853bceb6bf115f3924e9f8 |
| src/main/resources/db/migration/V20__turno_instructor_asignacion.sql | 9302feeace2fa0547e850f7e053bc670c8a6734951fe38f57ee0de6a5065b7f1 |
| src/main/resources/db/migration/V21__limpiar_asignaciones_sin_especialidad.sql | dd5318b5b3060756eeb440daed26236416916ff0b0353e68619bbb80b53998ad |
| src/main/resources/db/migration/V22_1__paquetes_y_compras.sql | cf979d2f8e8346359835d7f3bd23ada8c4a2370715a216f17e7060f7b8c19c32 |
| src/main/resources/db/migration/V22_2__compra_idempotencia.sql | 12aa65639797ab47f625b58c4fe5dcf8c3f57b142a31c1409f28ebcfbed50523 |
| src/main/resources/db/migration/V22_3__permiso_reembolsar_pagos.sql | fb62666ab153404e7f912c8d845489c69945f06ad59ee3946b1a43206687c201 |
| src/main/resources/db/migration/V22__asignacion_rango_horario.sql | 2651e52a312b11c1e7daaf19bf152b5df2d569b40a62c9890d8be7a721bd182b |
| src/main/resources/db/migration/V23__caja_paquete_actividades.sql | 8a7dcbda84afada3a0d43ec55047f15c7e4ff969c522e2cb013b7c6bc81bca01 |
| src/main/resources/db/migration/V24__eliminar_paquetes_semilla.sql | 3439e511642a81e03b00a8fbd2d82c20df383fcdde6e3972133b6733130031df |
| src/main/resources/db/migration/V25__compra_salon.sql | 151d97d78d46cf5047145cdad3f82212ed5c020b4e70f304d03312c927707ff7 |
| src/main/resources/db/migration/V26__compra_grupo.sql | 32a29edf4df2173cbf090f70cddcec94106411bc3501043e1b2858109f08acf6 |
| src/main/resources/db/migration/V27__compra_motivo_estado.sql | ff29c4097edf05ba3c1a5e36e592f51f4398b64b38f2b8f1b9a384723ad41e7e |
| src/main/resources/db/migration/V28__permisos_caja_granulares.sql | 39caeddc01248976ce069dc3d4f131a55b1192d41753d5fbdef906c93f1e329a |
| src/main/resources/db/migration/V29__permiso_vista_caja.sql | e703ffe6cd3c6cf6083e7b9799ecf3226cee8690f9b6fc821c94ff967b933c23 |
| src/main/resources/db/migration/V2__datos_iniciales_rbac.sql | 0dd3f21ad92449ae2f0833d6ab15a4870aced4a44c942dd2007ec2845809bd02 |
| src/main/resources/db/migration/V30__simplificar_descripcion_permisos_caja.sql | 9721121a15e9361d1de0875080101c592647a091406b812561d2018e4aaf1f33 |
| src/main/resources/db/migration/V31__reestructurar_permisos_caja.sql | 0f9b7d0c08c4fe0a4ac179ede49166a1aaeffbdf02887e448e230862bba198d9 |
| src/main/resources/db/migration/V32__renombrar_permisos_caja_a_venta.sql | 19ef5766baf60dd5adc285412a111a70ab5c9f871e9af3a985ba3827fbe25e50 |
| src/main/resources/db/migration/V33__granularizar_permisos_catalogo_venta.sql | 7894280b0a26b47d0f5d72782ed5623ddc3181f1512b48f6add6271e25bb0f3e |
| src/main/resources/db/migration/V34__renombrar_permisos_catalogo_a_servicios.sql | 84b311b98f2add720a4b2b746dd8458941afda7f299c42d1f3f2c51948ea9b7f |
| src/main/resources/db/migration/V35__eliminar_permiso_venta_reembolsar.sql | 052d173e9d123276a8e74484b31baf51cf0530ebc49daad6f9acd12daf4b046a |
| src/main/resources/db/migration/V36__recursos_y_actividad_recurso.sql | 247bd2e963c8b340de5e9370d237386b2f488dfeb4a738fbf0b9ab4c10bb31df |
| src/main/resources/db/migration/V37__permisos_actividades.sql | f9150e47cae63f3672e423356028cdf854a896dacd77fc976fedcc55bfd5c323 |
| src/main/resources/db/migration/V38__participantes_por_reserva.sql | 3ab7d119c919693a87958c481218ce0642c9333ec3fda60744f2d54ea2eaccb8 |
| src/main/resources/db/migration/V39__cantidad_actividad_recurso.sql | 09673b33102ac04e2493ee74046b792d8685c8a2e0cb026b0330bbeb947fdd0f |
| src/main/resources/db/migration/V3__invitaciones_usuario.sql | a1f899eef559a66201bc529af54e1d30427c5b88dc97adea25809d9b12ba0506 |
| src/main/resources/db/migration/V40__etiquetas_actividad.sql | df0fe140f9b0a0d7fa956e421589c2a336cd5280dd58dd39e193596794145e33 |
| src/main/resources/db/migration/V41__programacion_bloque_asignacion.sql | dc2e2ab0eaeb1070e803210bdaa8093899810f7251272229de07f000507001f4 |
| src/main/resources/db/migration/V42__salon_politicas_programacion.sql | f90e8c872b15a5e3057c3b32dde61e1ccb32acf52e5701d72be5ba841c950628 |
| src/main/resources/db/migration/V43__horario_operacion_vigencia.sql | f66940c681c74a765e25029a7fe75fe9d64cf0ed05e6c0faa657f81ff32a3761 |
| src/main/resources/db/migration/V44__btree_gist_extension.sql | a290425e1d173f7132fa66e1360411c0897b326e79744b07a95fc74af08c6722 |
| src/main/resources/db/migration/V45__horario_operacion_exclude_vigencia.sql | 270d646845ab485b8b5fdb787134e4204c9c120da406d269d6d820a8a71875bf |
| src/main/resources/db/migration/V46__horario_operacion_drop_unique_dia.sql | f0de75f89423779dcc22e1a90aecd5e09cb084c11b32546463b54c45ecc4e755 |
| src/main/resources/db/migration/V47__programacion_ajustes_fecha.sql | 76d6690b0c42c5a462b4bc630074906dde092f67f5951e1b458ad7b9100a1a3c |
| src/main/resources/db/migration/V48__pn14_slice2_orden_snapshot_expand.sql | edc12860431820d634d4bc837eae79a5f3604718f83d99207b1ac8765459248e |
| src/main/resources/db/migration/V49__pn14_slice2_snapshot_inmutabilidad.sql | 1aa858e265a389e9feeca1c691ace72e36fe87bc48fbdc79ff2e632fc3da280e |
| src/main/resources/db/migration/V4__usuario_admin_inicial.sql | 28398c642f80dc50668634cdfbfc7c699bddeb5511d57d4ba9f7653f7fc96655 |
| src/main/resources/db/migration/V5__permiso_activar_usuario.sql | 3aa1f0e16b57cb3ba3f49f8e1f3f7f9309636f0e6bb8857182f357c7ed680eef |
| src/main/resources/db/migration/V6__rol_super_admin.sql | 86b0e88db30e280ce87c5a22d7e6440a4812129aa04f8e014f2fa2ff754c8c21 |
| src/main/resources/db/migration/V7__categoria_permiso.sql | 8bc8c9d2067b57b783f615787cb92000e19e8a5ef53ecd52be77fca8afc41e32 |
| src/main/resources/db/migration/V8__permiso_gestionar_roles.sql | 4481cded81b9264fc2fc1b74a735e32d238543a1130bf66ee3732920545062b5 |
| src/main/resources/db/migration/V9__catalogo_ubicaciones.sql | 177fb922724e544e99c009383d8946228c6ef3aa3e88350acb67dd72a1c2f230 |
| src/test/java/com/feelingpilates/FeelingpilatesApplicationTests.java | 681dda7032122ea2592cc76da8fa4ef183699ba7f5fb2082c7e32a2d10e5ae54 |
| src/test/java/com/feelingpilates/TestcontainersConfiguration.java | 1f01c277c974e804506a05fa236b9c6997e9552e92e4160439f23cbbadeeaa9b |
| src/test/java/com/feelingpilates/auth/AuthControllerTest.java | 9eb507cf8905963f6c2d0135fb5bff5733d6fdb7956e0d05d008b5e21e5e1b0e |
| src/test/java/com/feelingpilates/auth/dto/CompletarInvitacionRequestTest.java | 375645c0e4251273e1dbf60d2ca6c41d8641dbd5519fc1641aa0ee3faeb3ef8c |
| src/test/java/com/feelingpilates/calendario/ReservaControllerSecurityTest.java | 070a8b93701de8cd652cc1d3de34449132e108e39e8d49d095d3f869ffb8fae4 |
| src/test/java/com/feelingpilates/calendario/ReservaServiceCaracterizacionTest.java | 6c93b3544f9201d810624220710c15f31a25abcc3a57bd4c9bb8f7a5905ce116 |
| src/test/java/com/feelingpilates/calendario/TurnoInstructorServiceCaracterizacionTest.java | 409e39c35cfdfc7ea6e7a455046246e0eb810da2b913f1d3a83a210bd7e4e412 |
| src/test/java/com/feelingpilates/calendario/TurnoInstructorServiceHorarioVersionadoTest.java | 0db0ecc3b7c06b95aee3dbe796664919ccdcfe2c0e450155739ec449446b9297 |
| src/test/java/com/feelingpilates/calendario/servicio/ImpactoPuntualEnExcepcionHorarioTest.java | d4e2980d482e0f3087415f37b4c5a329962c36ddbab82ec4f82bed00abf40615 |
| src/test/java/com/feelingpilates/calendario/servicio/ImpactoTurnosRecurrentesEnHorarioTest.java | 105e8bc38ce608c218e3d0170990b65fc84de7af0dfc3b7cbd9e475b34b3947b |
| src/test/java/com/feelingpilates/exception/CodigoErrorExtractorArquitecturaTest.java | 5dedf9d88004291ef3a52284bf1a85cdf14953f9ea944361ef44d143872e0869 |
| src/test/java/com/feelingpilates/exception/CodigoErrorExtractorTest.java | 125dbffa8d5dd6689789bdbb4cfc6a022e46b7e8569507228924292e79a0ebf7 |
| src/test/java/com/feelingpilates/exception/GlobalExceptionHandlerTest.java | 281941cf8176375a2b9b08f7da2f1e408e74216ff18922b4ea3e019d01a6f829 |
| src/test/java/com/feelingpilates/notificaciones/EmailServiceConsolaTest.java | 83b095e6e7ab8d64f5881ee66cfd85c83e0e4e7dd01df8f9915d94fa57313564 |
| src/test/java/com/feelingpilates/pagos/caracterizacion/CatalogoPN14Test.java | 28d9fc1a1ef05095e03df402e7cf5030be01cb05aa8a67ae1894afc24992946f |
| src/test/java/com/feelingpilates/pagos/caracterizacion/CompraPersistenciaPN14Test.java | 70bf0043a47a9c3d57bae069f0b4dad9443d525e6a7bfca5502cc24b88e379c3 |
| src/test/java/com/feelingpilates/pagos/caracterizacion/PN14Fixtures.java | ac894811e45df7a0b433d10451bd4075125c064d3661a8c0d56737011c8b2c01 |
| src/test/java/com/feelingpilates/pagos/caracterizacion/PagoIntentoPN14Test.java | 0282cde45d8211c60697a5e8e995b62addbcd0e74e9dcb0a51a25c93a046df33 |
| src/test/java/com/feelingpilates/pagos/caracterizacion/PagoLecturasPN14Test.java | 1b36bb8bb64192437745c46e6320f025fe5e72ae1b91ce11721f14f5daacde08 |
| src/test/java/com/feelingpilates/pagos/caracterizacion/PagoReconciliacionPN14Test.java | 1fe64052181919b9d6e3e41362c40115324638ce86d1ad762191e3597157e901 |
| src/test/java/com/feelingpilates/pagos/caracterizacion/PagoWebhookPN14Test.java | 9be4353e096185ac6db6f4248df680e6698826b5e5fe37e9bfd237c167f52cd2 |
| src/test/java/com/feelingpilates/pagos/caracterizacion/PagosApiPN14Test.java | 4d2fca0bf6a62eee30851364065a8b29ff770344a676c5ec87c742b052e47e52 |
| src/test/java/com/feelingpilates/pagos/caracterizacion/ReservasApiPN14Test.java | e3e990cd793e790d8bf8f238fa2da87a2a6d44443ec3b218a75815272fac6e01 |
| src/test/java/com/feelingpilates/pagos/caracterizacion/ReservasPN14Test.java | 826a469a107a56c12d5516bfb595569f899b71ba1a7947eaeee9c7a526e4aff5 |
| src/test/java/com/feelingpilates/pagos/caracterizacion/StripeResponseGetterPN14Fake.java | 687a7e1d002dc03cf38dae2c884cf228cd0b2f36e9679f5c14866013c5cb886c |
| src/test/java/com/feelingpilates/pagos/caracterizacion/VentaServicePN14Test.java | 3cf7da622e94f87f56cd2f46a7366c8f54c615adfb04605797c4939488b5913f |
| src/test/java/com/feelingpilates/pagos/caracterizacion/VentasCatalogoApiPN14Test.java | 0ce9bb46e3ee22a65b26426fdab8d82ce891d6966d5c2c53ac02697d3992a48b |
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
| src/test/java/com/feelingpilates/programacion/AjusteIdConcurrenciaTest.java | b32fbd0df451aff8e07e59433b439786cadafdb897f4f8a31b75ab638b68666a |
| src/test/java/com/feelingpilates/programacion/AjusteProgramacionFechaPersistenceTest.java | 90e44ea63a3654860d6d4126727f7084d69249e18368eeeec030cb58db9bce1e |
| src/test/java/com/feelingpilates/programacion/DarkLaunchArquitecturaTest.java | b6d3a82a5575d3fc7a6ca6e540ce3467c1d7599c3760b147222a41c4b66fe1ba |
| src/test/java/com/feelingpilates/programacion/DarkLaunchIntegracionTest.java | e3cf7e3ca11157d68890fd01c63b04731ab02e2180dda8a6dac6880c59dc3726 |
| src/test/java/com/feelingpilates/programacion/MigracionV46V47Test.java | 50c4463cda021cecb1523c084000fc077b63ecae3f0aaeee9838da2eebc3e59b |
| src/test/java/com/feelingpilates/programacion/ProgramacionConcurrenciaTest.java | a0b3ecc779b393ca9cd4ea6cc4a7420ddada23a9c5bc4722ecca4b08c7f1906a |
| src/test/java/com/feelingpilates/programacion/ProgramacionPersistenciaTest.java | 0a4ca66231d7f0545568ecbef6bb09e58270b4d68c4f567ebaac22d7a9ae9ea7 |
| src/test/java/com/feelingpilates/programacion/RelecturaAjustePostLocksTest.java | ad9a8c7565224ab70061d8f0c45dcb4a2f6dc2022285515d5b16e958ef07538d |
| src/test/java/com/feelingpilates/programacion/repositorio/BloqueProgramacionRepositoryVigenciaTest.java | e90211144efd46b3946816525c875dd4866ae2106117e076dd87f64e784e6c57 |
| src/test/java/com/feelingpilates/programacion/servicio/AjusteProgramacionFechaServiceTest.java | dc79610d039d1fabd490212ff7595055924e9833bbe816ac2990314077a3c484 |
| src/test/java/com/feelingpilates/programacion/servicio/AplicadorAjustesProgramacionTest.java | 2ff5e5655d15422ea3574485962640fd60c22ebf72937586fb1b38db4e91adad |
| src/test/java/com/feelingpilates/programacion/servicio/BloqueProgramacionServiceTest.java | 983f158612728ee4e2560e794188358f64e6f039db48b561121a611e5c2ebf7b |
| src/test/java/com/feelingpilates/programacion/servicio/ImpactoBloquesEnHorarioTest.java | 6986d12a538e57a58f866517775a7d6e2830da5c8a7127defd75309f3f7a83ac |
| src/test/java/com/feelingpilates/programacion/servicio/LocksOrdenadosTest.java | 3dff81ef349d770870d4c48c116caffa5fcf2070aa203ec2f17aedaec29e411f |
| src/test/java/com/feelingpilates/programacion/servicio/ProgramacionEfectivaTest.java | 7843c02ffa5ad7a1da0835f8c3254ef86350542fe3e715dd3482e9981610b3d6 |
| src/test/java/com/feelingpilates/programacion/servicio/ProgramacionPolicyATest.java | 73966a3e489b4210748bf9e7d2a8f0595141a23892da2ee6ac6731b9fe76dd69 |
| src/test/java/com/feelingpilates/programacion/servicio/ProgramacionValidadorTest.java | 9f510971e1ccd6bc242c74076df792f01836efbdd3d405b3bf589e7b2ae92be2 |
| src/test/java/com/feelingpilates/programacion/servicio/StaleDiscoveryAjusteProgramacionTest.java | 06c5d2b281708635a44d86f3be9ca9e15b679c72cc4a79aacca9fbc9ae42c367 |
| src/test/java/com/feelingpilates/seguridad/AutorizacionContextualControllerTest.java | bed51e685a5451e26872dbf9ffa2d9d1272ebd89db961980e44fdc433900f40a |
| src/test/java/com/feelingpilates/seguridad/AutorizadorSalonTest.java | 79e438a9d0dc237a491bb1c511cd62b45f1d7a2a22490df4f94c1fb6752c00af |
| src/test/java/com/feelingpilates/transicion/programacion/detector/CandidateEvidenceGeneratorTest.java | 16f0c8cfb45a1ec7f2522d0726381cf1021826d901a0dcd32937c2155ffd52e3 |
| src/test/java/com/feelingpilates/transicion/programacion/detector/DetectorArchitectureIsolationTest.java | b38f7e23e82174bd33808aaf424158dd9e769b16574d5735fc372a5b9195e10d |
| src/test/java/com/feelingpilates/transicion/programacion/detector/DetectorClassifierTest.java | c25c0be74a4ea3c17218b152035fde4d020c216349ba0fc96b1f5f12f0247cca |
| src/test/java/com/feelingpilates/transicion/programacion/detector/DetectorImmutabilityTest.java | feacdaabc35291a04f3fd9747b5ae4304a79f1f5bc6e098ceca89b13dbd407f4 |
| src/test/java/com/feelingpilates/transicion/programacion/detector/DetectorResultInvariantTest.java | cb5f0a44ad8463dd02f5838c5767c4e2b13ceba582cfbbee419b12bb2bc81fd9 |
| src/test/java/com/feelingpilates/transicion/programacion/detector/DetectorTestFixtures.java | fe5dcbeb4f770b5cacc69061369af5ec89bf5981c6fe061ed1db50806f573dfb |
| src/test/java/com/feelingpilates/transicion/programacion/detector/F2DAuthorityGuardTest.java | cc74f82486574b92f52b9ad9375ce6b43d7bfab8f30fe2d156202d4aed4431ed |
| src/test/java/com/feelingpilates/ubicaciones/HorarioOperacionConcurrenciaTest.java | 1a94bcc0cbbe36d5a68669f0133ffc355612a1a0cc0d6445804f03cbdc043679 |
| src/test/java/com/feelingpilates/ubicaciones/HorarioOperacionMigracionV42V43Test.java | bd1a753322cd7c7d0c7a6f46d1e0f0111af77eeea2a5a36db50de88f3d366537 |
| src/test/java/com/feelingpilates/ubicaciones/HorarioOperacionMigracionV43V46Test.java | 86c5896be9def5348188b0566467a4cd7a85f3a539b30c42ec7af092babe258e |
| src/test/java/com/feelingpilates/ubicaciones/HorarioOperacionVersionadoPersistenciaTest.java | ebff877409634ff6174ee929e5b744e3804905767276d53512fae778beaf0d5f |
| src/test/java/com/feelingpilates/ubicaciones/HorarioOperacionWritersPersistenciaTest.java | 7faecc9ce2f64f0951006371726c587f4aa7f45687abd93368a73dc04c4aa084 |
| src/test/java/com/feelingpilates/ubicaciones/SalonHorarioExcepcionConcurrenciaTest.java | e5fb7984fc9206f0986eefab1b8956a50453c2cb39b476507fec57a25f50ac88 |
| src/test/java/com/feelingpilates/ubicaciones/SalonHorarioExcepcionPersistenciaTest.java | 3070512484504d56b1d4c012c7aa6d9fef65c2ddafb90ba6d4ed714ca60b5ab8 |
| src/test/java/com/feelingpilates/ubicaciones/SalonHorarioOperacionHistorialPersistenciaTest.java | 04717d21611e996c65061a509d9e0e3fab10f55c1f3c6972b4e93f09f3b0c220 |
| src/test/java/com/feelingpilates/ubicaciones/UbicacionesPersistenciaTest.java | 10d85876b22c8c7b16fe21d26e4e7331affc612ac0d43a7186664a8ac2f1ef64 |
| src/test/java/com/feelingpilates/ubicaciones/controlador/SalonHorarioExcepcionControllerTest.java | 273fccbce9e8b1d11337e05be187bd1d9908fe395424705b4ad38f6495a14827 |
| src/test/java/com/feelingpilates/ubicaciones/controlador/SalonHorarioOperacionControllerTest.java | 3dacbf7ff6739f4bb309803a83609ca28a32fd0622fda75559f6d87c0fcd5968 |
| src/test/java/com/feelingpilates/ubicaciones/dominio/CoberturaVigenciaTest.java | 804cbdb018a76954533a92df20a7de91078b2c53f4134b6f698bc7724ecdde1c |
| src/test/java/com/feelingpilates/ubicaciones/dominio/DiaSemanaOperacionTest.java | 6e05f36e3a0acc388bbb86bcd84c8f12894b738392d746ca0298382432042f32 |
| src/test/java/com/feelingpilates/ubicaciones/dominio/RangoVigenciaTest.java | e99495cc63f6fe1138844f78f3ed630885235369a5a32aa8ce5e1faeba69773b |
| src/test/java/com/feelingpilates/ubicaciones/repositorio/HorarioOperacionRepositoryTest.java | fb62d6d8b3eb9d1a5e0020160cbe870bf5a8c3814b971713e47435c7e9a76193 |
| src/test/java/com/feelingpilates/ubicaciones/servicio/CerrarHorarioOperacionTest.java | aa8163bcad98c84db99ea576c9e77f7453b2dafb19691858eb19e475bb017898 |
| src/test/java/com/feelingpilates/ubicaciones/servicio/ConflictoExcepcionHorarioTranslatorTest.java | c9b49b69f8db810dd88141a132db89fc65c736bdcce67403e7685f58839c3f98 |
| src/test/java/com/feelingpilates/ubicaciones/servicio/ConflictoVigenciaHorarioTranslatorTest.java | d22788695c88e7120b2ac7fbc70538bfe08fc61cad4f20cb735c58f3c53bcd4e |
| src/test/java/com/feelingpilates/ubicaciones/servicio/HorarioEfectivoSalonTest.java | 23f8e641f87438184c7c8983013662c64e6e7ebc0d42013fd25e0c11a9f2654d |
| src/test/java/com/feelingpilates/ubicaciones/servicio/HorarioOperacionErroresTest.java | e2a3f7cb0cf8c9fc42fcf75524bc54fc9d28c2a20f15ba4a01eee5d45f35b1b4 |
| src/test/java/com/feelingpilates/ubicaciones/servicio/HorarioOperacionResolverPersistenciaTest.java | 1f93faa3b40e479aff6ce17bb3889eac32dd753a3c0f71efd24613e3c11e52e2 |
| src/test/java/com/feelingpilates/ubicaciones/servicio/HorarioOperacionResolverTest.java | a11b885d0d16b02d2360742a4ba247a10fb8da891c020b3b7a8fef9d36a8f7e2 |
| src/test/java/com/feelingpilates/ubicaciones/servicio/SalonHorarioExcepcionServiceTest.java | 31c5c36ddb5880c2861889671b9f59791411da07d2a785a80e16836c647346f8 |
| src/test/java/com/feelingpilates/ubicaciones/servicio/SalonServiceTest.java | d14de2a22785c016390c4a79a49faa06ca3b3ffb1f9859fd929dadea28ab64d1 |
| src/test/java/com/feelingpilates/ubicaciones/servicio/VersionarHorarioOperacionTest.java | 51182f479980f766670a59c07a2942e8aca0d603f727ac19ab955bda16214c1c |
| src/test/resources/mockito-extensions/org.mockito.plugins.MockMaker | 8eca853a5a457f17e0479b71dfbe95f2a13aff9eeff250261ecdc8ca9b847a6a |

## 4. Candidato exact32 PRESENT — pin individual / NO_WRITE

```json
{
  "src/main/java/com/feelingpilates/pagos/ventas/aplicacion/BackfillOrdenSnapshot.java": "b24e1b829431e5a91f7ca32af5d4b690b52c45c95ed75530d79eb08c729d4a0f",
  "src/main/java/com/feelingpilates/pagos/ventas/aplicacion/CompraHistoricaSnapshot.java": "38e28e926cad743026284f466ac04c66e4d6ed55c44cf883f04eb99089e6a44f",
  "src/main/java/com/feelingpilates/pagos/ventas/aplicacion/CongelarOrdenSnapshot.java": "6ab0f95187a8adbedf988c4b01034b69774a4ee21d21dfeb4ff23a8ad33148b7",
  "src/main/java/com/feelingpilates/pagos/ventas/aplicacion/ConsultaHistoricaSnapshot.java": "2431565f7fb7b308fe8eede3eb6bb7fc36edbf961ea43f4408bfbfe3c1608709",
  "src/main/java/com/feelingpilates/pagos/ventas/aplicacion/FuenteHistoricaCompra.java": "c86e88aa4f279c12d7b176b6c816b309c821426a635e2bf5e837634450a156b7",
  "src/main/java/com/feelingpilates/pagos/ventas/aplicacion/InformeBackfillSnapshot.java": "25c6d9da5b3810bf46f2ff30e8777b61f5404221188c59a0a0a624cd448d8e11",
  "src/main/java/com/feelingpilates/pagos/ventas/aplicacion/RepositorioOrdenSnapshot.java": "ffbc35925dc241ed098b6a61c6539ac00d538ba34d5080f45ac92576af523064",
  "src/main/java/com/feelingpilates/pagos/ventas/dominio/Compra.java": "e9899d11aec2b2738ff1ce739a76cd66f0dcd8b49545bef669837d0bfff0de96",
  "src/main/java/com/feelingpilates/pagos/ventas/dominio/CompraComponenteSnapshot.java": "5ae0ad2d7e73d997942138858d1496b20bd23184115828fabc9a7618ee0cfd73",
  "src/main/java/com/feelingpilates/pagos/ventas/dominio/ContenidoSnapshotCanonico.java": "483db6f1f7cf4cfd3da71453373c393b22d58aeee408e6c99c439e30e478a6ac",
  "src/main/java/com/feelingpilates/pagos/ventas/dominio/ImporteMonetario.java": "e5cbd912a040ed15219e5424b45eedb7588ca79e2abbfbc20c36d0545a46b2d0",
  "src/main/java/com/feelingpilates/pagos/ventas/dominio/OrdenVenta.java": "e5122cbaf04d36dbff241a51431346e1944c40937587dc9d5520f76cee2d49fe",
  "src/main/java/com/feelingpilates/pagos/ventas/dominio/PoliticaComercialSnapshot.java": "c16184239f82777bfec7056b68640bc6129298d14789ca229696ad475616e58c",
  "src/main/java/com/feelingpilates/pagos/ventas/dominio/ProvenienciaSnapshot.java": "695373e25e825e71d4c1dafe07c231f2b936b459420d5ba78ed926908c7823b5",
  "src/main/java/com/feelingpilates/pagos/ventas/infraestructura/ConsultaHistoricaSnapshotJdbcAdapter.java": "df438e2c89a7201d84a2103b9edb2631a556e6c87d669741786da46ea404e3ce",
  "src/main/java/com/feelingpilates/pagos/ventas/infraestructura/FuenteHistoricaCompraJdbcAdapter.java": "89223c356530395a541582501a827ab5431dbe4480d4dd26e99021140c0f083b",
  "src/main/java/com/feelingpilates/pagos/ventas/infraestructura/InformeBackfillJdbcAdapter.java": "8458d295f3ae99518be95035a9317d24c18ff8a954abf90521e3443908164a3a",
  "src/main/java/com/feelingpilates/pagos/ventas/infraestructura/OrdenSnapshotJdbcAdapter.java": "2959e7c12fbdbaee09d1b1ea14dfd3d271e5b219026b2a27bf3b2a86dee82b21",
  "src/main/resources/db/migration/V48__pn14_slice2_orden_snapshot_expand.sql": "edc12860431820d634d4bc837eae79a5f3604718f83d99207b1ac8765459248e",
  "src/main/resources/db/migration/V49__pn14_slice2_snapshot_inmutabilidad.sql": "1aa858e265a389e9feeca1c691ace72e36fe87bc48fbdc79ff2e632fc3da280e",
  "src/test/java/com/feelingpilates/pagos/ventas/BackfillOrdenSnapshotPostgresTest.java": "5dda0a95515c880a08e8a2487ad7a0c2e3040464283ab689266425381890f0a4",
  "src/test/java/com/feelingpilates/pagos/ventas/BackfillOrdenSnapshotTest.java": "4adff58b23be5225ea236d6f95ff05a39f85607ab9fe83b177a8ccbba96fe80f",
  "src/test/java/com/feelingpilates/pagos/ventas/ConsultaHistoricaSnapshotTest.java": "077e3d9ce4055d335e9706f336f226fed382deef7c1df785c1c578a7c14102a0",
  "src/test/java/com/feelingpilates/pagos/ventas/OrdenSnapshotConcurrenciaTest.java": "79e2f8f807eccdd9c687a775fbabf213f2aea5e5129ebf3a51bb11b2d6caf607",
  "src/test/java/com/feelingpilates/pagos/ventas/OrdenSnapshotDominioTest.java": "55c605f696d26dee29a3ec585f18de2c2c6b36ce4592b47d7fc79858d3799471",
  "src/test/java/com/feelingpilates/pagos/ventas/OrdenSnapshotIdempotenciaTest.java": "4075033c5439bfbaa46adc6758a90d10c7c38572edc858e3ce129af90ce2e121",
  "src/test/java/com/feelingpilates/pagos/ventas/OrdenSnapshotMigracionTest.java": "a6fdb43bc4b54293c0db96c97ee7db304e8fb7575eb1974db6aa3673a1d1f0b1",
  "src/test/java/com/feelingpilates/pagos/ventas/OrdenSnapshotPersistenciaTest.java": "9d275805c51a31a13d44cec227f114933f7aef9fd1833bd6a0c6d70c8e525b77",
  "src/test/java/com/feelingpilates/pagos/ventas/OrdenSnapshotTransaccionTest.java": "9d5b6e713ff238df9dcefa24f3dc8d45c90affcb1c350f334f96bda6c5695c11",
  "src/test/java/com/feelingpilates/pagos/ventas/PN14Slice2Fixtures.java": "403a48fa7ee4d4de6a80bfd2f057c6229087e5b8d56788e22aae91785ed98fd9",
  "src/test/java/com/feelingpilates/pagos/ventas/PoliticaSnapshotCanonicoTest.java": "644201fc91164971c027e8d58d9bdf71f94a7095c0675f484ab807bb9db96bf9",
  "src/test/java/com/feelingpilates/pagos/ventas/VentasSnapshotArquitecturaTest.java": "a75ee330361c1b79bf3224edd167da8250ddeffb27c9e33c4f16c514b4a45814"
}
```

## 5. Migraciones antiguas exact50 — ALL individualSHA protegidos

Mismo algoritmo exactpath+NUL+SHA(raw)+LF; rawdigest
`e2848476012dc44133776cad500f2bb0870ecf7c1b66cdd9dbcab0ee5fddb398`.
V48/V49 pins en §1 y§4, únicos adicionales:52/max49. No SQL edits ni checksum nuevo ejecutado aquí.

```json
{
  "src/main/resources/db/migration/V10__salones_semilla.sql": "0806a7d947f5266a2a86ee4494885ca4413ffcb46a0d5f88c4044a07f6780f76",
  "src/main/resources/db/migration/V11__salones_gestion.sql": "686184cebdda9003c51f45ab0cc6c7a486c4511026dea1e71892fbe75985e59b",
  "src/main/resources/db/migration/V12__salon_direccion_completa.sql": "baaf4378963894eb972917767172297130ee59e204f84fc9962d10e8129f5c3b",
  "src/main/resources/db/migration/V13__usuario_foto_binaria.sql": "01a04f3919be365a770b273a00168616a0cfb33412ab711ec493edd4c217b4ef",
  "src/main/resources/db/migration/V14__salon_inventario_maquinas.sql": "7ef8be8cfd0426ba643b3c43bc848812c92c063d2ffe3681024c34eb8dd843e8",
  "src/main/resources/db/migration/V15__calendario_instructores.sql": "956e8530816d830756bbc8c477b61105b26f5e2e3bc0d96d468d748a87796310",
  "src/main/resources/db/migration/V16__permisos_calendario_granular.sql": "161edc552853d6c83464b5787f7f5065ab8577d7454f27bdec5e7fa2f5c006da",
  "src/main/resources/db/migration/V17__turno_instructor_actividad.sql": "43b1ab7f41feaddc9bce6961581cd2f99f8a9c90e482ce7f8ba5fd29c544190f",
  "src/main/resources/db/migration/V18__salon_horario_excepcion.sql": "453b8e5afff4a20c3d2234ea311485a7fee77efeb7a6333dfe6417d8126f7627",
  "src/main/resources/db/migration/V19__turno_instructor_multiples.sql": "256199bce25ef692f444753307c2723149cee449f8ee4082a18fa57389d0d0b1",
  "src/main/resources/db/migration/V1__esquema_usuarios_rbac.sql": "99481b564267ac0c5e5e2f4aa1175140ce24a6213e853bceb6bf115f3924e9f8",
  "src/main/resources/db/migration/V20__turno_instructor_asignacion.sql": "9302feeace2fa0547e850f7e053bc670c8a6734951fe38f57ee0de6a5065b7f1",
  "src/main/resources/db/migration/V21__limpiar_asignaciones_sin_especialidad.sql": "dd5318b5b3060756eeb440daed26236416916ff0b0353e68619bbb80b53998ad",
  "src/main/resources/db/migration/V22_1__paquetes_y_compras.sql": "cf979d2f8e8346359835d7f3bd23ada8c4a2370715a216f17e7060f7b8c19c32",
  "src/main/resources/db/migration/V22_2__compra_idempotencia.sql": "12aa65639797ab47f625b58c4fe5dcf8c3f57b142a31c1409f28ebcfbed50523",
  "src/main/resources/db/migration/V22_3__permiso_reembolsar_pagos.sql": "fb62666ab153404e7f912c8d845489c69945f06ad59ee3946b1a43206687c201",
  "src/main/resources/db/migration/V22__asignacion_rango_horario.sql": "2651e52a312b11c1e7daaf19bf152b5df2d569b40a62c9890d8be7a721bd182b",
  "src/main/resources/db/migration/V23__caja_paquete_actividades.sql": "8a7dcbda84afada3a0d43ec55047f15c7e4ff969c522e2cb013b7c6bc81bca01",
  "src/main/resources/db/migration/V24__eliminar_paquetes_semilla.sql": "3439e511642a81e03b00a8fbd2d82c20df383fcdde6e3972133b6733130031df",
  "src/main/resources/db/migration/V25__compra_salon.sql": "151d97d78d46cf5047145cdad3f82212ed5c020b4e70f304d03312c927707ff7",
  "src/main/resources/db/migration/V26__compra_grupo.sql": "32a29edf4df2173cbf090f70cddcec94106411bc3501043e1b2858109f08acf6",
  "src/main/resources/db/migration/V27__compra_motivo_estado.sql": "ff29c4097edf05ba3c1a5e36e592f51f4398b64b38f2b8f1b9a384723ad41e7e",
  "src/main/resources/db/migration/V28__permisos_caja_granulares.sql": "39caeddc01248976ce069dc3d4f131a55b1192d41753d5fbdef906c93f1e329a",
  "src/main/resources/db/migration/V29__permiso_vista_caja.sql": "e703ffe6cd3c6cf6083e7b9799ecf3226cee8690f9b6fc821c94ff967b933c23",
  "src/main/resources/db/migration/V2__datos_iniciales_rbac.sql": "0dd3f21ad92449ae2f0833d6ab15a4870aced4a44c942dd2007ec2845809bd02",
  "src/main/resources/db/migration/V30__simplificar_descripcion_permisos_caja.sql": "9721121a15e9361d1de0875080101c592647a091406b812561d2018e4aaf1f33",
  "src/main/resources/db/migration/V31__reestructurar_permisos_caja.sql": "0f9b7d0c08c4fe0a4ac179ede49166a1aaeffbdf02887e448e230862bba198d9",
  "src/main/resources/db/migration/V32__renombrar_permisos_caja_a_venta.sql": "19ef5766baf60dd5adc285412a111a70ab5c9f871e9af3a985ba3827fbe25e50",
  "src/main/resources/db/migration/V33__granularizar_permisos_catalogo_venta.sql": "7894280b0a26b47d0f5d72782ed5623ddc3181f1512b48f6add6271e25bb0f3e",
  "src/main/resources/db/migration/V34__renombrar_permisos_catalogo_a_servicios.sql": "84b311b98f2add720a4b2b746dd8458941afda7f299c42d1f3f2c51948ea9b7f",
  "src/main/resources/db/migration/V35__eliminar_permiso_venta_reembolsar.sql": "052d173e9d123276a8e74484b31baf51cf0530ebc49daad6f9acd12daf4b046a",
  "src/main/resources/db/migration/V36__recursos_y_actividad_recurso.sql": "247bd2e963c8b340de5e9370d237386b2f488dfeb4a738fbf0b9ab4c10bb31df",
  "src/main/resources/db/migration/V37__permisos_actividades.sql": "f9150e47cae63f3672e423356028cdf854a896dacd77fc976fedcc55bfd5c323",
  "src/main/resources/db/migration/V38__participantes_por_reserva.sql": "3ab7d119c919693a87958c481218ce0642c9333ec3fda60744f2d54ea2eaccb8",
  "src/main/resources/db/migration/V39__cantidad_actividad_recurso.sql": "09673b33102ac04e2493ee74046b792d8685c8a2e0cb026b0330bbeb947fdd0f",
  "src/main/resources/db/migration/V3__invitaciones_usuario.sql": "a1f899eef559a66201bc529af54e1d30427c5b88dc97adea25809d9b12ba0506",
  "src/main/resources/db/migration/V40__etiquetas_actividad.sql": "df0fe140f9b0a0d7fa956e421589c2a336cd5280dd58dd39e193596794145e33",
  "src/main/resources/db/migration/V41__programacion_bloque_asignacion.sql": "dc2e2ab0eaeb1070e803210bdaa8093899810f7251272229de07f000507001f4",
  "src/main/resources/db/migration/V42__salon_politicas_programacion.sql": "f90e8c872b15a5e3057c3b32dde61e1ccb32acf52e5701d72be5ba841c950628",
  "src/main/resources/db/migration/V43__horario_operacion_vigencia.sql": "f66940c681c74a765e25029a7fe75fe9d64cf0ed05e6c0faa657f81ff32a3761",
  "src/main/resources/db/migration/V44__btree_gist_extension.sql": "a290425e1d173f7132fa66e1360411c0897b326e79744b07a95fc74af08c6722",
  "src/main/resources/db/migration/V45__horario_operacion_exclude_vigencia.sql": "270d646845ab485b8b5fdb787134e4204c9c120da406d269d6d820a8a71875bf",
  "src/main/resources/db/migration/V46__horario_operacion_drop_unique_dia.sql": "f0de75f89423779dcc22e1a90aecd5e09cb084c11b32546463b54c45ecc4e755",
  "src/main/resources/db/migration/V47__programacion_ajustes_fecha.sql": "76d6690b0c42c5a462b4bc630074906dde092f67f5951e1b458ad7b9100a1a3c",
  "src/main/resources/db/migration/V4__usuario_admin_inicial.sql": "28398c642f80dc50668634cdfbfc7c699bddeb5511d57d4ba9f7653f7fc96655",
  "src/main/resources/db/migration/V5__permiso_activar_usuario.sql": "3aa1f0e16b57cb3ba3f49f8e1f3f7f9309636f0e6bb8857182f357c7ed680eef",
  "src/main/resources/db/migration/V6__rol_super_admin.sql": "86b0e88db30e280ce87c5a22d7e6440a4812129aa04f8e014f2fa2ff754c8c21",
  "src/main/resources/db/migration/V7__categoria_permiso.sql": "8bc8c9d2067b57b783f615787cb92000e19e8a5ef53ecd52be77fca8afc41e32",
  "src/main/resources/db/migration/V8__permiso_gestionar_roles.sql": "4481cded81b9264fc2fc1b74a735e32d238543a1130bf66ee3732920545062b5",
  "src/main/resources/db/migration/V9__catalogo_ubicaciones.sql": "177fb922724e544e99c009383d8946228c6ef3aa3e88350acb67dd72a1c2f230"
}
```

## 6. Lectura futura obligatoria y STOP

Antes de cualquier futureinitialwrite, corrector recupera audit/gates/Task/Dispatch/uniqueDone
reales yrevalida original502fullmap salvo ESTADO exact64649/74494 prefixes appendfinito,
checkpoint14737prefix/handoffinmutable, cuatro coreSHA/externalexact5 candidate YauthoritySHA,
manifestselfSHA/localEntryManifestSHA256/wholecurrent506rawcountmap físico idéntico entre
materializador/verificador/coordinator/finalgate. Branch/HEAD/upstream/live/indexEMPTY/0–0,
32PRESENTrawunchanged,protectedtestbefore,50oldmigrations/52max49/V48V49sha, ningún inesperado
 dirty/new/deleted. Sólo actualfinalconjunction activa entrada local; nunca prosa ni alias PASS.
No publicación previa requerida, sin stage/commit/push; manifest no dirtywaiver general/perpetuo.

Futureinitialwrite único dos literales líneas47–48 de ProgramacionPersistenciaTest, todosotrosbytes
inalterados; afterSHA hipotético NOT_WRITTEN. Única narrowprecedence handoff nuevo §4 sobre
32PRESENT/52max49/knownREDguard; baseline638PASS anterior intacto, full683/T18 sigueFAIL.
Aftertwofix completefreshgreen REQUIRED: protected15focal → T01–T18/11newclasses → M01–M12/
13publishedclasses → full → realPG16/Testcontainers/fresh52/upgradeV47/all50checksums/
legacycompatibility/conexiones,PIDs independientes/barriers/timeouts/winner/replay/conflict/
rollback/atomicidad/canon/inmutabilidad → requiredSkips0 → freshaudit técnico AJENO → gate separado.
Comandos/entorno completos literales handoffs originales/nuevo§8; counts/exit/logs/XML nuevos.
Dockerblocked HostValidator competente separado, nunca skipPASS. No regeneration32/producción/
config/skips/dynamicpins/runtime/livebackfill/cutover/fence/F2E/slices3–12/publicación/cierre.
PN13/Slice1 terminal; NEW-PN13-017 preexistente nonblocking unchanged; no closedfinding reopen.
Otro fallo/contradicción/path/decisión no derivable STOP PRODUCT_OR_ARCHITECTURAL_AUTHORITY_REQUIRED
/ SCOPE_EXPANSION_REQUIRED / NO_WRITES. Hash/evidencia mismatch STOP AUTHORIZATION_MISMATCH.
STOP conserva oldwriter/candidato/evidencia/dirty, sin reset/clean/stash/delete/downgrade.
