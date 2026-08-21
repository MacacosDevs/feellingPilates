# FeelingPilates — Fase 0C.1: Saneamiento Flyway pre-release

## 1. Pre-flight

- Fecha: 2026-08-21.
- Raíz Git: `/Users/jesusaldaircruzortiz/Desktop/Feelingpilates/feelingpilates`.
- Branch inicial: `baseline/preservacion-working-tree-2026-08-20`.
- HEAD inicial autorizado: `0b0d9b5b717e27bb647da081f3d3fc4a65417e1d`.
- Working tree inicial: limpio.
- Ancestro inmediato: `143983c740c7fcfff7e6c17d32d6796a05619c91`.
- Ese ancestro sigue siendo el snapshot funcional original.
- El HEAD inicial sólo agrega `auditoria/fase-0c-flyway-ambientes.md`.
- El pre-flight actualizado coincidió por completo.

## 2. Branch

- Se creó `stabilizacion/flyway-pre-release` desde el HEAD autorizado.
- Comando: `git switch -c stabilizacion/flyway-pre-release`.
- No se tocó `main`; no hubo merge, rebase, tag ni push.
- Se inspeccionaron V11, V14, V15, V19–V23 y V24–V40.

## 3. V22.x

- `V22_1__paquetes_y_compras.sql`: SIN CAMBIOS.
- `V22_2__compra_idempotencia.sql`: SIN CAMBIOS.
- `V22_3__permiso_reembolsar_pagos.sql`: SIN CAMBIOS.
- No se renumeró ninguna migración.
- El conjunto conserva 43 archivos y 43 versiones únicas.
- V22.1 crea `paquete`/`compra` e inserta ocho paquetes semilla.
- Sus UUID usan `gen_random_uuid()` y no son deterministas.
- Sus firmas de negocio sí son deterministas en la sentencia `VALUES`.
- V22.1/V22.2/V22.3 permanecen como historia canónica.

## 4. V24

Antes:

- `DELETE FROM paquete WHERE categoria IS NOT NULL`.
- La condición podía incluir cualquier paquete real con categoría.

Identificación segura:

- Las ocho semillas proceden exclusivamente del `INSERT` de V22.1.
- Se enumeran categoría, nombre, descripción, precio, vigencia, texto unitario,
  destacado y orden de cada fila conocida.
- No se inventaron UUID.
- La semilla precede a cualquier alta posterior entre versiones publicadas.
- Una fila editada deja de coincidir y se conserva.
- Una fila con compras o composición asociada se conserva.

Después:

- Un CTE contiene sólo las ocho firmas conocidas.
- Se elimina como máximo una fila exacta por firma.
- En una instalación nueva se retiraron exactamente las ocho semillas.
- Checksum nuevo: `-783142445`.
- Motivo: retirar mock data sin poner en riesgo catálogo de negocio.

## 5. V36

Antes:

- Renombraba máquina → recurso y luego vaciaba ocho tablas.
- Borraba inventario, oferta, asignaciones, especialidades y reservas.
- Borraba composición de paquetes y ambos catálogos con sus UUID.
- Creaba `actividad_recurso` con semántica transitoria.

Después:

- Renombra `tipo_maquina` → `tipo_recurso`.
- Renombra `salon_maquina` → `salon_recurso`.
- Renombra `tipo_maquina_id` → `tipo_recurso_id`.
- Renombra PK, UNIQUE, CHECK y FKs asociados.
- No ejecuta ningún `DELETE` y no reconstruye filas.
- Conserva UUID, inventario y referencias a actividades.
- Crea `actividad_recurso` de manera aditiva y vacía.
- No infiere mappings por nombres ni `Duo Reformer = 2`.
- `cantidad` nace como unidades TOTALES consumidas por reserva.
- Checksum nuevo: `751867630`.

Prueba de preservación V35 → V40:

- Se usó PostgreSQL 16 efímero sin volumen persistente.
- Flyway se limitó primero a V35.
- Se insertaron datos sintéticos en cada conjunto antes borrado.
- Recurso sintético: UUID terminado en `3601`.
- Actividad sintética: UUID terminado en `3602`.
- Inventario sintético: cantidad `7`.
- Se añadieron oferta, especialidad, asignación, paquete/composición y reserva.
- Tras V36–V40, recurso y actividad conservaron los mismos UUID.
- El inventario conservó cantidad `7`.
- Oferta, especialidad, asignación, composición y reserva conservaron una fila.
- `actividad_recurso` quedó con cero mappings inventados.
- JPA `validate` pasó.

## 6. V38

Antes:

- Añadía `participantes_por_reserva`.
- Renombraba `cantidad` a `cantidad_por_participante`.
- Eliminaba `modo_consumo` y `salon.permite_pareja`.

Después:

- Sólo añade `participantes_por_reserva NOT NULL DEFAULT 1 CHECK >= 1`.
- No modifica `actividad_recurso.cantidad`.
- No ejecuta `DROP COLUMN`.
- `modo_consumo` no existe porque la nueva V36 nunca lo crea.
- `salon.permite_pareja` queda temporalmente como columna legacy.
- Java no la mapea y JPA permite la columna adicional.
- Duo Reformer se modela mediante actividad/recurso, no con el flag legacy.
- Su retiro físico corresponde a una futura migración contract.
- Checksum nuevo: `-1289802220`.

## 7. V39

- `actividad_recurso.cantidad` significa unidades totales por reserva.
- No significa cantidad por participante.
- `participantes_por_reserva` es independiente y no multiplica esa cantidad.
- V36 crea la tabla vacía directamente con columna final `cantidad`.
- Ninguna migración inserta asociaciones actividad→recurso.
- No existe `cantidad_por_participante` en la nueva historia.
- No hay filas de semántica anterior que transformar.
- Checksum nuevo: `-408799761`.

## 8. Operaciones destructivas restantes

Se buscaron `DELETE`, `TRUNCATE`, `DROP TABLE`, `DROP COLUMN`,
`ALTER ... TYPE` y `SET NOT NULL` en V24–V40.

| Migración | Operación | Clasificación | Razón |
|---|---|---|---|
| V24 | `DELETE` de semillas | INTENCIONAL Y SEGURA | Firma completa, una fila máxima y guardas FK |
| V35 | `DELETE rol_permiso` | INTENCIONAL Y SEGURA | Código de permiso legado exacto |
| V35 | `DELETE permiso` | INTENCIONAL Y SEGURA | Catálogo RBAC exacto; endpoint retirado |
| V36 | FKs `ON DELETE CASCADE` | INTENCIONAL Y SEGURA | Definición relacional; no ejecuta borrado |
| V40 | `NOT NULL DEFAULT '{}'` | INTENCIONAL Y SEGURA | Backfill automático a arreglo vacío |

- No queda `TRUNCATE`, `DROP TABLE`, `DROP COLUMN` ni `ALTER ... TYPE`.
- No queda `SET NOT NULL` sin default/backfill.
- No queda ningún `DELETE` masivo de datos de negocio.
- V25–V34 son aditivas o actualizaciones dirigidas de metadata/RBAC.
- V37 y V40 permanecen sin cambios.

## 9. Schema final

Tablas comprobadas:

- `tipo_recurso`, `salon_recurso`, `actividad_recurso`, `tipo_actividad`.
- `reserva`, `paquete_actividad`, `turno_instructor_asignacion`.
- `instructor_actividad`.

Columnas comprobadas:

- Existe `actividad_recurso.cantidad`.
- No existen `cantidad_por_participante` ni `modo_consumo`.
- Existen `participantes_por_reserva` y `etiquetas` en `tipo_actividad`.
- `salon.permite_pareja` existe sólo como legacy deliberado.

Constraints comprobados:

- `tipo_recurso_pkey`, `tipo_recurso_nombre_key`.
- `salon_recurso_pkey`, `salon_recurso_cantidad_check`.
- `salon_recurso_salon_id_fkey`.
- `salon_recurso_tipo_recurso_id_fkey`.
- El schema satisface las 150 fuentes Java sin modificar Java.

## 10. Build

- Comando: `./mvnw clean compile`.
- Resultado: BUILD SUCCESS.
- Fuentes compiladas: 150.
- Java, `pom.xml` y configuración: sin cambios.

## 11. Flyway efímero

- Motor: PostgreSQL 16.14.
- Testcontainers desde schema vacío: PASS.
- 43 migraciones validadas y aplicadas; 43 versiones únicas.
- Secuencia V1 → V40; versión final V40.
- JPA `validate`: PASS.
- Prueba adicional V35 con datos → V40: PASS.
- Inspecciones posteriores: transacciones `READ ONLY`.
- Contenedores efímeros eliminados al terminar.

## 12. Tests

- Comando: `./mvnw test`.
- Total: 10; pasan: 9; fallan: 1.
- Único fallo: `AuthControllerTest#googleStubDevuelve501`.
- Esperaba HTTP 501 y observó HTTP 500.
- Coincide exactamente con el baseline autorizado.
- Fallos nuevos: ninguno.
- Google no se corrigió por estar fuera de alcance.

## 13. BD local

Identidad previa:

- Contenedor: `feelingpilates-db`; servicio: `postgres`.
- Volumen: `feelingpilates_feelingpilates-data`.
- Proyecto compose: `feelingpilates`; único usuario del volumen.
- Host local; ninguna URL remota detectada.
- El hash del servicio coincidió con el compose actual.
- Una ruta histórica del label ya no existía; el hash resolvió la identidad.

Recreación:

- `docker compose down -v` eliminó sólo contenedor, volumen y red del proyecto.
- No se ejecutó ningún prune global.
- `docker compose up -d postgres` levantó sólo PostgreSQL.
- El nuevo volumen recibió las etiquetas correctas.
- Antes de Flyway había cero tablas públicas y ningún historial.
- Los datos locales previos se descartaron con autorización explícita.
- La aplicación temporal se detuvo; PostgreSQL quedó levantado.

## 14. Historial local nuevo

- 43 migraciones exitosas y 43 versiones únicas.
- Último rank: 43; versión final: V40.
- Checksums: V24 `-783142445`, V36 `751867630`.
- Checksums: V38 `-1289802220`, V39 `-408799761`.
- La aplicación inició en perfil dev contra localhost.
- Flyway y JPA `validate`: PASS.
- No se cargaron datos manuales en la BD local nueva.

## 15. Diff

Cambios permitidos:

- V24, V36, V38 y V39.
- `auditoria/fase-0c1-saneamiento-flyway.md`.

Controles:

- V1–V23 y V22.1–V22.3: sin cambios.
- V37 y V40: sin cambios.
- Java, configuración y secrets: sin cambios.
- Ningún archivo generado está versionado.

| Migración | Antes | Después | Motivo |
|---|---|---|---|
| V22.1–V22.3 | Historia canónica | SIN CAMBIOS | Conservar versiones aprobadas |
| V24 | Borrado por categoría | Ocho firmas exactas con guardas | Proteger catálogo real |
| V36 | Ocho DELETE totales | Renames + tabla aditiva vacía | Preservar IDs y negocio |
| V38 | Renames y drops | Sólo expand; legacy permanece | Contract posterior |
| V39 | Rename semántico | Checkpoint no-op | No reinterpretar datos |

## 16. Commit

- Mensaje: `fix: sanear migraciones Flyway pre-release`.
- Un único commit contiene cuatro migraciones y este checkpoint.
- Su hash autoritativo es el commit que contiene este documento.
- No push, merge ni tag.

## 17. Resultado

- Branch: `stabilizacion/flyway-pre-release`.
- Build, Flyway efímero, preservación y JPA: PASS.
- Tests: baseline conocido; ningún fallo nuevo.
- BD LOCAL: RECREADA.
- Flyway/JPA local desde vacío: PASS.
- HISTORIA FLYWAY PRE-RELEASE: SANEADA.
- SEGURO PARA PRIMER DEPLOY EN CUANTO A HISTORIA FLYWAY: SÍ.
- Esto evalúa sólo Flyway, no readiness productivo integral.

## 18. Riesgos restantes

- `salon.permite_pareja` requiere una futura migración contract.
- Java no la utiliza y JPA no exige retirarla.
- `actividad_recurso` nace vacía; los mappings deben definirse por negocio.
- `Duo Reformer = 2` debe capturarse como cantidad total explícita.
- V24 conserva semillas modificadas o referenciadas para evitar pérdida.
- V35 mantiene el retiro dirigido del permiso RBAC obsoleto.
- El fallo Google conocido sigue pendiente fuera de esta fase.
- No se evaluaron seguridad general, frontend, mobile ni producción completa.
- No se hizo push de la branch de estabilización.
