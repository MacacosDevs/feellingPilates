# FeelingPilates — Fase 0B: Preservación del baseline

> Checkpoint de preservación Git ejecutado el 2026-08-20. Este documento describe
> el commit que contiene este checkpoint; no certifica seguridad de despliegue.

## 1. Pre-flight

- Raíz Git: `/Users/jesusaldaircruzortiz/Desktop/Feelingpilates/feelingpilates`.
- Branch origen: `main`.
- HEAD origen: `f18df68e31f53d0fa5b1974f7e878832b4409cd3`.
- Upstream de origen: `origin/main`, `ahead 0` / `behind 0` según las referencias
  disponibles al ejecutar el pre-flight.
- Índice inicial: limpio; `git diff --cached --name-status` no devolvió rutas.
- Working tree: coincide con el inventario de Fase 0A más el propio checkpoint 0A.
- Inventario observado: 35 rutas tracked cambiadas y 56 untracked: las 55 previas
  a 0A más `auditoria/fase-0a-baseline-git.md`.
- No se observaron conflictos, Java/SQL nuevos, cambios tracked adicionales ni
  archivos sensibles no esperados.

## 2. Branch de preservación

- Branch solicitada: `baseline/preservacion-working-tree-2026-08-20`.
- Antes de crearla no existía como branch local ni como referencia remota
  almacenada; `git ls-remote --heads` tampoco devolvió una branch remota.
- Creada desde `main` mediante `git switch -c`.
- Branch confirmada después del cambio:
  `baseline/preservacion-working-tree-2026-08-20`.
- HEAD después del cambio de branch permaneció en
  `f18df68e31f53d0fa5b1974f7e878832b4409cd3`.
- No se ejecutó push.

## 3. Manifiesto preservado

El manifiesto permitido se construyó desde Fase 0A y este checkpoint. Incluye:

- 23 Java tracked modificados y nueve Java tracked eliminados.
- 32 Java untracked funcionales.
- Tres SQL tracked eliminados: pagos V14/V15/V16.
- 21 SQL untracked: V22.1/V22.2/V22.3 y V23–V40.
- Reemplazos completos `Maquina` → `Recurso` en entidades, DTOs, repositories
  y controllers.
- Documentos `auditoria/00-revalidacion-repositorio-completo.md`,
  `auditoria/04-arquitectura-objetivo.md`,
  `auditoria/fase-0a-baseline-git.md` y este checkpoint 0B.
- Total esperado del snapshot: 92 rutas cambiadas respecto del HEAD origen:
  35 tracked cambiadas, 55 untracked previas a 0A, 0A y 0B.

## 4. Exclusiones

Se excluyen expresamente del baseline:

- `.env` y cualquier archivo de secretos, token, certificado o credencial local.
- `.idea/` y configuración local de IDE.
- `.claude/settings.local.json`.
- `target/` y demás salidas generadas.
- `HELP.md`, logs y temporales ignorados.
- Los repositorios hermanos `../web` y `../FeelingPiltaesAppMobile`.

No se usará `git add .`, `git add -A` ni `git add -f`.

## 5. Staging

- Estado: PASS.
- Método: rutas individuales explícitas tomadas del manifiesto de Fase 0A;
  borrados staged con `git add -u -- <rutas>`.
- No se usó `git add .`, `git add -A` ni `git add -f`.
- Índice sin detección de renames: 92 rutas, distribuidas en 57 added,
  23 modified y 12 deleted.
- Verificación untracked: 32 Java, 21 SQL y cuatro auditorías staged.
- Verificación de borrados: nueve Java y tres SQL staged.
- `.env`, `target/`, `.idea/`, `.claude/settings.local.json` y `HELP.md`
  no aparecen en el índice.
- `git diff --cached --stat` con detección de renames: 82 entradas,
  2,987 inserciones y 181 eliminaciones.
- `git diff --cached --check` señaló espacios finales exclusivamente en líneas
  preexistentes de los documentos nuevos 00 y 04. Se registran como advertencia;
  no se editaron porque esta fase prohíbe modificar esos checkpoints.

## 6. Renombres/reemplazos

- Estado: PASS.
- Los ocho orígenes `*Maquina*` eliminados y los ocho destinos `*Recurso*`
  agregados están juntos en el índice.
- Git detectó siete pares Java como rename: controller, tres DTOs, dos entidades
  y `SalonMaquinaRepository` → `SalonRecursoRepository`.
- `TipoMaquinaRepository` → `TipoRecursoRepository` quedó correctamente como
  delete + add; la detección automática de rename no es requisito.
- Git detectó V14 pagos → V22.1, V15 idempotencia → V22.2 y V16 reembolso →
  V22.3 como renames de contenido 100% idéntico.
- V23–V40 están staged conjuntamente.

## 7. Revisión de secretos

- Estado: PASS.
- Revisión por nombres, ignore, rutas staged y contenido agregado del diff.
- Se encontraron 33 usos de términos potenciales. Corresponden a documentación,
  nombres de variables, flujo de tokens/credenciales y placeholders permitidos.
- Una heurística de asignación larga señaló `AuthService.java:69`; la inspección
  redactada confirmó una llamada a método sin literal ni concatenación, no un
  secreto embebido.
- No se detectaron claves privadas, tokens JWT/GitHub/AWS, API keys Google ni
  secretos Stripe con formato real.
- No se mostraron valores durante la revisión.

## 8. Snapshot del índice

- Estado: PASS.
- El índice se exportó mediante `git checkout-index --all --force` a
  `/private/tmp/feelingpilates-0b.2my9Re`, fuera del repositorio.
- La copia contiene `pom.xml`, `mvnw`, `.mvn`, `src`, 150 fuentes Java y
  43 migraciones SQL.
- La copia no contenía `.env` ni `target/` antes de ejecutar Maven.
- La exportación no alteró el working tree original.

## 9. Build

- Estado: PASS.
- Java: OpenJDK 21.0.11 LTS.
- Maven Wrapper: Apache Maven 3.9.16 sobre Java 21.0.11.
- Comando desde el snapshot staged: `./mvnw clean compile`.
- Resultado: `BUILD SUCCESS`; compiladas 150 fuentes con `release 21`.

## 10. Flyway efímero

- Estado: PASS.
- Mecanismo: `FeelingpilatesApplicationTests` con Testcontainers y
  `postgres:16-alpine`; PostgreSQL observado: 16.14.
- La primera ejecución dentro del sandbox no obtuvo permiso para el socket
  Docker; se repitió con acceso explícitamente autorizado al Docker local.
- Flyway validó correctamente 43 migraciones con versiones únicas.
- Aplicó las 43 migraciones sobre un esquema vacío y alcanzó versión v40.
- V36 se ejecutó únicamente dentro de la BD efímera del test.
- No se usó ninguna base real, compartida ni configurada desde `.env`.

## 11. Tests

- Estado: BASELINE CON FALLO CONOCIDO PRESERVABLE.
- Comando desde el snapshot staged: `./mvnw test`.
- Surefire: 10 tests, 9 passing, 1 failing, 0 errors, 0 skipped.
- Único fallo: `AuthControllerTest#googleStubDevuelve501`.
- No aparecieron fallos adicionales; no se corrigió el test ni el código.

## 12. Validación pre-commit

- Estado: PASS.
- Branch confirmada: `baseline/preservacion-working-tree-2026-08-20`.
- HEAD pre-commit confirmado:
  `f18df68e31f53d0fa5b1974f7e878832b4409cd3`.
- Índice final sin detección de renames: 92 rutas; 57 added, 23 modified y
  12 deleted.
- No quedaron cambios unstaged ni archivos untracked dentro del repositorio.
- El manifiesto contiene conjuntamente Java funcional, SQL V22.1–V40,
  eliminaciones V14/V15/V16, reemplazos Maquina→Recurso y auditorías 00/04/0A/0B.
- Las exclusiones sensibles/locales siguen fuera del índice.
- La revisión final de secretos no encontró valores reales.
- `git diff --cached --check` conserva sólo la advertencia documentada de
  whitespace en 00/04; no se modificaron esos documentos.

## 13. Commit baseline

- Estado: PRESERVADO EN EL COMMIT QUE CONTIENE ESTE CHECKPOINT.
- Mensaje: `chore: preservar snapshot baseline funcional`.
- Es un commit de preservación, no una certificación de feature, arquitectura,
  migraciones de producción ni seguridad de V36.

## 14. Estado post-commit

- Branch conservada: `baseline/preservacion-working-tree-2026-08-20`.
- HEAD post-commit: el commit que contiene este checkpoint.
- Working tree: limpio después del commit, salvo archivos locales ignorados que
  permanecen deliberadamente fuera de Git.
- No se hizo push, tag, merge, Pull Request ni regreso a `main`.

## 15. Riesgos pendientes

- V36 es necesaria para reproducir el esquema actual, pero contiene operaciones
  destructivas sobre datos y **no está aprobada para despliegue**.
- Sigue pendiente Fase 0C para revisar `flyway_schema_history` por ambiente y la
  compatibilidad de la renumeración V14/V15/V16 → V22.1/V22.2/V22.3.
- El fallo conocido del stub Google se preserva, no se corrige en esta fase.

## 16. Resultado

- BASELINE GIT: **PRESERVADO**.
- BUILD: **PASS**.
- FLYWAY BD EFÍMERA: **PASS**.
- TESTS: **9/10 — FALLO CONOCIDO GOOGLE**.
- SEGURO PARA DEPLOY: **NO**.
- Motivo: pendiente Fase 0C de historiales Flyway/V36 y demás estabilización.
