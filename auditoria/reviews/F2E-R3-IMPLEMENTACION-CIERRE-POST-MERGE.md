# F2E R3 — cierre separado post-merge de implementación

Corte: 2026-10-03T22:49:20.023945Z. Clase: PROCESS_ONLY / CANDIDATE / NON_PRODUCT_AUTHORITY.
Base física y main live verificados: `3329cc0d8d8c2d6b44df980e7e96e3b24dcfe3a8`. Esta unidad no pertenece al
WRITE_SCOPE de implementación ni modifica sus veinte rutas.

## Hechos integrados y evidencia

PR #21: https://github.com/MacacosDevs/feellingPilates/pull/21, MERGED;
merge `3329cc0d8d8c2d6b44df980e7e96e3b24dcfe3a8`, head `c22952c18d0da732594314e7e34e150bdc8db856`,
candidato técnico `94d8f06c16ee64d85c45e7cd6e47ec4ab5ce375f`.
La diferencia base PR #20 → merge PR #21 es exactamente 20 fuentes + un recibo.
Manifiesto SHA-256 de las 20 fuentes: `850c856de80478575deb3691c8a54fe5986fc9bb0c9f4b7eefa1b24797aff3d8`.
Framing: `SHA256  ruta\n`, orden UTF-8, LF final.
Diseño sellado SHA-256 `42a21c09d137d363e938d441a499d48c5ec7537a73ee23861bfce2c4d75677f9`;
handoff `a74d139ca6c924dcbb98c368d80f38a17cf423408c7db30054e621b67b4cefa9`;
revisión handoff `94c6063a66c918a9a88dc6e77bd3b3e72b913161610c0e1dde060796136bcfce`.
PR #19 y PR #20 son precedentes integrados, con gate humano R1 resuelto por el usuario.

Auditoría técnica original NOT_APPROVED 0/1/0, seguida de Correction.1 acotada a
boundary y test transaccional R3. Reauditoría por agente nuevo APPROVED 0/0/0,
P1-1 CLOSED; Task `task_50459af87d58`, Dispatch `ctx_07373e2b6353`.
Informe SHA-256 `9d31e8ba0f2f6fd7866f04af4bb36e88f8028d874572b7b548febf7436ba05bc`.
Gate `gate_54c01ce6906e` resuelto READY_FOR_CONTROLLED_PUBLICATION.
Recibo técnico publicado: `auditoria/reviews/F2E-R3-IMPLEMENTACION-READER-PROGRAMACION-NOMINAL-CLEAN-MAIN-REVIEW.md`.

Validación sobre bytes aprobados: clean compile 217 fuentes, full 549 tests
(36 R3 + 94 R1/R2 + 37 detector + 382 Lane 1–4), cero failures/errors/skips.
JDBC_BINDING, CHECKSUM_STABILITY y CHECKSUM_SENSITIVITY PASS mediante PostgreSQL
real y probes ejecutados; el recibo técnico detalla las pruebas y límites.
Este lifecycle documental no vuelve a ejecutar Maven ni declara nuevos tests.
Se conserva el red previo a Correction.1, el EOF histórico de fixture y retry
sin cambios, y la limitación histórica de XML de precisión inicial; los gates
vigentes usan evidencia fresca intacta. RAW local en `/Users/jesusaldaircruzortiz/.codex/feelingpilates-evidence/run_f90eb621de3d/`, responsabilidad
RUN_COORDINATOR, sin backup externo garantizado: r3-implementation/ (525 archivos
indexados), r3-audit-round1/, r3-correction1/, r3-audit-round2/ y
R3-PR21-INTEGRATED.json. Sus manifiestos permiten comprobar hashes y disponibilidad.

## Scope separado y condición de cierre

Allowlist exacta, DEFAULT_DENY:

- MOD `auditoria/ESTADO-ACTUAL.md`
- MOD `auditoria/orquestacion/F2E-STATE.json`
- NEW `auditoria/reviews/F2E-R3-IMPLEMENTACION-CIERRE-POST-MERGE.md`
- NEW `auditoria/reviews/F2E-R3-IMPLEMENTACION-CIERRE-INDEPENDENT-RECEIPT.md` — sólo recibo factual después de auditoría, sin tocar los tres bytes auditados.

El manifiesto externo completo de los tres archivos candidatos incluye STATE;
no se incrusta en STATE ni en este archivo. El payload almacenado en STATE
excluye STATE y recibos posteriores para evitar autorreferencia. IDs de la nueva
auditoría/gate/PR no creados quedan pendientes y se resuelven desde recibos reales.
No se modifica STATE después de audit/push para insertar resultados futuros.

CLOSED sólo cuando: auditoría independiente del candidato exacto APPROVED con P0/P1/P2 0/0/0; recibo factual de esa auditoría publicado como hijo directo sin cambiar los tres archivos auditados; merge normal de ese historial en main verificado live; delta respecto de 3329cc0d limitado a las cuatro rutas de proceso; las 20 fuentes R3 y las tres autoridades selladas conservan sus hashes aprobados; PR #19/#20/#21 y candidato/recibo de cierre son ancestros del main verificado. Si falta una prueba: PROCESS_CLOSURE_PENDING / FAIL_CLOSED.

Antes de cumplirse: implementación INTEGRATED, cierre PENDING. Después de pruebas
positivas sobre main live: IMPLEMENTED / ACCEPTED / PUBLISHED / INTEGRATED / CLOSED.
Una mera descendencia del anchor, un cache o este candidato no bastan.
La auditoría independiente de proceso revisará también el post-merge real del
PR #21. Correcciones documentales acotadas y una reauditoría nueva se permiten
bajo la autorización del lifecycle; los intentos anteriores se conservan.

## Invariantes y siguiente transición

TurnoInstructor LEGACY_VIVO / PRODUCTIVO; R3 DARK_LAUNCH sin beans/callers/routing
productivo. Flyway V46, 49 migrations; V47 ausente; migración R3 ninguna.
Sin ajustes, cutover, Client API/web/reservas, Payments ni Notifications nuevos.
R4–R6 y owner multi-reader R6 NOT_AUTHORIZED. No nuevo piloto de modelos, costo,
quota o métricas inventadas. El cierre sólo registra hechos; no activa producto,
model routing ni otra implementación. Tras CLOSED no hay siguiente implementación
autorizada: requiere nuevo lifecycle competente.
