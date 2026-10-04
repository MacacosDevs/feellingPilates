# FeelingPilates — Fase 0C: Flyway y ambientes

> Inspección de sólo lectura finalizada el 2026-08-21. No se inició la aplicación, no se ejecutó Flyway, no se modificó ninguna BD y no se leyó ni mostró ninguna credencial. La única consulta SQL se ejecutó dentro de una transacción `READ ONLY`.

## 1. Baseline

- Raíz Git: `/Users/jesusaldaircruzortiz/Desktop/Feelingpilates/feelingpilates`.
- Branch: `baseline/preservacion-working-tree-2026-08-20`.
- HEAD: `143983c740c7fcfff7e6c17d32d6796a05619c91`.
- Upstream observado: `origin/baseline/preservacion-working-tree-2026-08-20`.
- Working tree previo a este checkpoint: limpio; sin staged, unstaged ni untracked.
- `git ls-remote` confirmó el mismo HEAD en la branch baseline remota y `f18df68e31f53d0fa5b1974f7e878832b4409cd3` en `origin/main`.
- Coincide con el baseline preservado de Fase 0B. No hay cambios funcionales posteriores; este documento es el único cambio de Fase 0C.

## 2. Ambientes

| Ambiente | Evidencia | BD esperada | Flyway automático | Acceso disponible |
|---|---|---|---|---|
| local/dev | Perfil default `dev`, `.env` con perfil dev y host local, `docker-compose.yml` | PostgreSQL 16 en Docker, volumen persistente | SÍ | SÍ, sólo lectura; inspeccionado |
| test | `@SpringBootTest`, `TestcontainersConfiguration`, `postgres:16-alpine` | PostgreSQL 16 efímero | SÍ | Disponible al crear contenedor; no iniciado en 0C |
| production | `application-prod.properties` y `Dockerfile` | PostgreSQL por configuración base | SÍ | NO DISPONIBLE; no hay destino ni autorización |

- No hay evidencia de un ambiente `staging`.
- No existen GitHub Actions, scripts de deploy, manifiestos de plataforma ni otra configuración CI/CD en esta raíz.
- `README.md` no documenta ambientes; `.env.example` describe sólo ejecución dev.

## 3. Configuración Flyway

- Spring Boot `4.1.0`; Flyway resuelto localmente `12.4.0`.
- Dependencias: `spring-boot-flyway`, `flyway-core` y `flyway-database-postgresql`; driver PostgreSQL en runtime.
- `spring.flyway.enabled=true` está en `application.properties`: el contexto Spring ejecuta migrate automáticamente antes de validar JPA.
- `dev` y `prod` sólo cambian JPA/logging; ninguno deshabilita ni limita Flyway.
- Locations no se configura: aplica el default `classpath:db/migration`.
- Baseline no se configura: `baselineOnMigrate=false`, versión default `1`.
- Validación no se configura: `validateOnMigrate=true`; naming estricto está en
  `false` por default.
- `outOfOrder=false`, target `latest` y `cleanDisabled=true` por defaults de Flyway 12.4.0. `cleanDisabled` no neutraliza un `DELETE` dentro de una migración.
- JPA usa `ddl-auto=validate`; no crea ni corrige el esquema.

Riesgo de arranque accidental:

- Una BD correcta en V22 o anterior recibiría automáticamente V22.1–V40, incluido el borrado masivo de V36.
- Una BD con las antiguas V14/V15/V16 de pagos debería fallar primero en validación por versión/checksum/descripcion distintos. Desactivar validación no la vuelve compatible: V22.1 intentaría crear objetos ya existentes.
- Una BD vacía llega a V40, pero V36 elimina los catálogos semilla y deja sus tablas vacías antes de V38–V40.
- El mismo riesgo existe con perfil `prod`; no hay guard específico de producción.

## 4. Inventario de migraciones

- Conjunto exacto: `V1`–`V22`, `V22.1`, `V22.2`, `V22.3` y `V23`–`V40`.
- Total: 43 migraciones y 43 versiones únicas; no hay versiones duplicadas.
- Archivos V1–V13: esquema/datos RBAC, invitaciones, admin, ubicación y salones.
- V14 `V14__salon_inventario_maquinas.sql`.
- V15 `V15__calendario_instructores.sql`.
- V16 `V16__permisos_calendario_granular.sql`.
- V17–V22: actividad de turno, excepción de horario, múltiples instructores, asignaciones, limpieza y rango horario.
- `V22_1__paquetes_y_compras.sql`, `V22_2__compra_idempotencia.sql`, `V22_3__permiso_reembolsar_pagos.sql`.
- V23–V35: caja/paquetes/compras y evolución de permisos.
- `V36__recursos_y_actividad_recurso.sql`.
- V37–V40: permisos de actividades, participantes, cantidad de recurso y etiquetas.
- La prueba efímera documentada en Fase 0B aplicó exactamente las 43 hasta V40.

## 5. Historia V14/V15/V16

El HEAD antiguo `f18df68` contenía simultáneamente estas colisiones:

| Versión | Migración de operación | Migración de pagos |
|---|---|---|
| V14 | inventario de máquinas | paquetes/compras |
| V15 | calendario de instructores | idempotencia de compra |
| V16 | permisos granulares de calendario | permiso de reembolso |

El baseline conserva las tres de operación como V14/V15/V16 y mueve pagos a
V22.1/V22.2/V22.3. Los tres pares de blobs son idénticos byte por byte; sólo cambia
nombre/versión. Git muestra que pagos se incorporó a commits antes que las versiones
de operación fueran confirmadas, por lo que una instalación histórica alternativa
es plausible y no puede descartarse sin consultar su historial.

## 6. Bases inspeccionables

| Candidato | Clasificación | Resultado |
|---|---|---|
| Docker local `feelingpilates-db` | SEGURO PARA LECTURA | PostgreSQL 16 saludable; perfil dev/host local; volumen nombrado persistente |
| Testcontainers | SEGURO PARA LECTURA | Efímero; evidencia previa V1–V40; no se recreó en 0C |
| Production | NO SEGURO SIN AUTORIZACIÓN | Perfil definido, pero sin destino, historial ni acceso demostrable |
| Staging | NO DISPONIBLE | No existe evidencia en el repositorio |

La BD local se consultó con el `psql` del propio contenedor, sin iniciar la app. No se consultó ninguna tabla de negocio. El host no tiene `psql`; no se inició ni alteró ningún contenedor.

## 7. flyway_schema_history

Consulta local/dev: `BEGIN TRANSACTION READ ONLY`, selección de las ocho columnas autorizadas, `COMMIT`. Las 43 filas tienen `type=SQL` y `success=SÍ`.

| rank | version | description | script | checksum | installed_on |
|---:|---:|---|---|---:|---|
| 1 | 1 | esquema usuarios rbac | `V1__esquema_usuarios_rbac.sql` | -907901435 | 2026-07-19 19:01:58 |
| 2 | 2 | datos iniciales rbac | `V2__datos_iniciales_rbac.sql` | 899342371 | 2026-07-19 19:01:58 |
| 3 | 3 | invitaciones usuario | `V3__invitaciones_usuario.sql` | -1825842744 | 2026-07-19 21:15:23 |
| 4 | 4 | usuario admin inicial | `V4__usuario_admin_inicial.sql` | -1985970880 | 2026-07-19 21:21:34 |
| 5 | 5 | permiso activar usuario | `V5__permiso_activar_usuario.sql` | -692875249 | 2026-07-19 21:40:49 |
| 6 | 6 | rol super admin | `V6__rol_super_admin.sql` | 1064306939 | 2026-07-19 21:56:35 |
| 7 | 7 | categoria permiso | `V7__categoria_permiso.sql` | -102105623 | 2026-07-19 23:26:29 |
| 8 | 8 | permiso gestionar roles | `V8__permiso_gestionar_roles.sql` | -586437006 | 2026-07-19 23:43:40 |
| 9 | 9 | catalogo ubicaciones | `V9__catalogo_ubicaciones.sql` | -980886295 | 2026-07-20 01:16:04 |
| 10 | 10 | salones semilla | `V10__salones_semilla.sql` | 1181149217 | 2026-07-20 01:22:02 |
| 11 | 11 | salones gestion | `V11__salones_gestion.sql` | 1650444910 | 2026-07-21 23:49:47 |
| 12 | 12 | salon direccion completa | `V12__salon_direccion_completa.sql` | 726972647 | 2026-07-22 00:03:52 |
| 13 | 13 | usuario foto binaria | `V13__usuario_foto_binaria.sql` | -680853843 | 2026-07-22 18:47:34 |
| 14 | 14 | salon inventario maquinas | `V14__salon_inventario_maquinas.sql` | 275981680 | 2026-07-25 18:34:35 |
| 15 | 15 | calendario instructores | `V15__calendario_instructores.sql` | 1056196994 | 2026-07-28 22:45:54 |
| 16 | 16 | permisos calendario granular | `V16__permisos_calendario_granular.sql` | -2121894176 | 2026-08-01 10:58:25 |
| 17 | 17 | turno instructor actividad | `V17__turno_instructor_actividad.sql` | 1717044415 | 2026-08-01 12:22:52 |
| 18 | 18 | salon horario excepcion | `V18__salon_horario_excepcion.sql` | 1817654971 | 2026-08-01 13:17:00 |
| 19 | 19 | turno instructor multiples | `V19__turno_instructor_multiples.sql` | 932055491 | 2026-08-04 19:35:11 |
| 20 | 20 | turno instructor asignacion | `V20__turno_instructor_asignacion.sql` | -562308381 | 2026-08-04 20:33:06 |
| 21 | 21 | limpiar asignaciones sin especialidad | `V21__limpiar_asignaciones_sin_especialidad.sql` | 1887842790 | 2026-08-04 20:44:46 |
| 22 | 22 | asignacion rango horario | `V22__asignacion_rango_horario.sql` | 1756299380 | 2026-08-06 22:46:07 |
| 23 | 22.1 | paquetes y compras | `V22_1__paquetes_y_compras.sql` | 1311374726 | 2026-08-16 08:54:42 |
| 24 | 22.2 | compra idempotencia | `V22_2__compra_idempotencia.sql` | -823439175 | 2026-08-16 08:54:42 |
| 25 | 22.3 | permiso reembolsar pagos | `V22_3__permiso_reembolsar_pagos.sql` | -160288120 | 2026-08-16 08:54:42 |
| 26 | 23 | caja paquete actividades | `V23__caja_paquete_actividades.sql` | 1287242521 | 2026-08-16 08:54:42 |
| 27 | 24 | eliminar paquetes semilla | `V24__eliminar_paquetes_semilla.sql` | -1996884299 | 2026-08-16 09:16:27 |
| 28 | 25 | compra salon | `V25__compra_salon.sql` | -1568127376 | 2026-08-16 09:35:33 |
| 29 | 26 | compra grupo | `V26__compra_grupo.sql` | 1519039083 | 2026-08-16 13:40:38 |
| 30 | 27 | compra motivo estado | `V27__compra_motivo_estado.sql` | 1261405547 | 2026-08-16 14:30:58 |
| 31 | 28 | permisos caja granulares | `V28__permisos_caja_granulares.sql` | 1830933037 | 2026-08-16 14:50:15 |
| 32 | 29 | permiso vista caja | `V29__permiso_vista_caja.sql` | 84736854 | 2026-08-16 15:03:06 |
| 33 | 30 | simplificar descripcion permisos caja | `V30__simplificar_descripcion_permisos_caja.sql` | 1267152076 | 2026-08-16 15:22:59 |
| 34 | 31 | reestructurar permisos caja | `V31__reestructurar_permisos_caja.sql` | -553399349 | 2026-08-16 16:27:16 |
| 35 | 32 | renombrar permisos caja a venta | `V32__renombrar_permisos_caja_a_venta.sql` | -895024854 | 2026-08-16 17:08:44 |
| 36 | 33 | granularizar permisos catalogo venta | `V33__granularizar_permisos_catalogo_venta.sql` | 1061779196 | 2026-08-16 17:31:04 |
| 37 | 34 | renombrar permisos catalogo a servicios | `V34__renombrar_permisos_catalogo_a_servicios.sql` | -1153207525 | 2026-08-16 17:52:42 |
| 38 | 35 | eliminar permiso venta reembolsar | `V35__eliminar_permiso_venta_reembolsar.sql` | 334900416 | 2026-08-16 18:35:42 |
| 39 | 36 | recursos y actividad recurso | `V36__recursos_y_actividad_recurso.sql` | 1241050759 | 2026-08-16 19:46:34 |
| 40 | 37 | permisos actividades | `V37__permisos_actividades.sql` | -511225137 | 2026-08-16 19:46:34 |
| 41 | 38 | participantes por reserva | `V38__participantes_por_reserva.sql` | -1389875518 | 2026-08-16 22:00:29 |
| 42 | 39 | cantidad actividad recurso | `V39__cantidad_actividad_recurso.sql` | 363385110 | 2026-08-16 22:15:32 |
| 43 | 40 | etiquetas actividad | `V40__etiquetas_actividad.sql` | -1143209197 | 2026-08-16 22:58:35 |

Los checksums registrados de V22.1–V22.3 y V36–V40 coinciden con los archivos del
baseline. No se ejecutó `validate`; la comparación se calculó localmente sobre los
archivos.

## 8. Matriz por ambiente

| Ambiente | Última versión | V14 pagos aplicada | V15 idem aplicada | V16 refund aplicada | V22.x | V23-35 | V36 | V37-40 | Evidencia |
|---|---|---|---|---|---|---|---|---|---|
| local/dev persistente | 40 | NO | NO | NO | SÍ | SÍ | SÍ | SÍ | historial read-only; scripts/checksums exactos |
| test efímero Fase 0B | 40 | NO | NO | NO | SÍ | SÍ | SÍ | SÍ | checkpoint 0B; instancia ya destruida |
| production definido | NO INSPECCIONADO | NO INSPECCIONADO | NO INSPECCIONADO | NO INSPECCIONADO | NO INSPECCIONADO | NO INSPECCIONADO | NO INSPECCIONADO | NO INSPECCIONADO | sin acceso/destino demostrado |

## 9. Compatibilidad V22.x

**Escenario A — sólo V14 inventario, V15 calendario, V16 permisos.**
V22.1–V22.3 son versiones posteriores y pueden aplicarse normalmente si los demás
prerrequisitos V1–V22 están íntegros. La BD local/dev demuestra este escenario:
registró las tres versiones de operación y luego las tres V22.x con éxito.

**Escenario B — pagos ejecutados como V14/V15/V16.**
El baseline resuelve esas versiones a scripts distintos. Con la configuración actual,
`validateOnMigrate` debe bloquear por descripción/checksum incompatibles. Si se
forzara más allá de la validación, Flyway consideraría V22.x pendientes: V22.1
intentaría recrear `paquete`/`compra`, V22.2 repetiría columna/UNIQUE y V22.3 podría
duplicar el permiso. Además, inventario/calendario no habrían sido aplicados aunque
sus versiones aparecerían ocupadas. No se soluciona con `repair` ni renumerando a
ciegas; requiere inventario y una reconciliación específica por ambiente.

**Escenario C — ninguna BD persistente anterior.**
El conjunto actual es consistente para una instalación nueva: Testcontainers llegó
a V40. V22.x pueden conservarse, sujeto al riesgo independiente de V36 y al catálogo
vacío resultante.

## 10. V36

V36 primero renombra `tipo_maquina→tipo_recurso`, `salon_maquina→salon_recurso` y
la columna FK. Esos renombres por sí solos preservan filas. Después ejecuta:

| Tabla | Qué representa | DELETE en V36 | Posibles dependencias/consecuencia |
|---|---|---|---|
| `salon_recurso` | inventario y cantidades por salón | total | FK a salón/recurso; se pierde inventario físico |
| `salon_tipo_actividad` | oferta de actividades por sede | total | FKs sin cascade; se pierde qué ofrece cada salón |
| `turno_instructor_asignacion` | actividad/rango de instructor por turno | total | turno y usuario sobreviven sin asignaciones |
| `instructor_actividad` | especialidades del instructor | total | se pierde elegibilidad/capacitación registrada |
| `paquete_actividad` | composición y clases de cada paquete | total | paquete/compra sobreviven sin composición |
| `reserva` | reservas confirmadas/canceladas e historial | total | se pierde el booking completo; no hay tabla sustituta |
| `tipo_recurso` | catálogo global renombrado | total | se pierden IDs, nombres, estado y descripción |
| `tipo_actividad` | catálogo, duración y estado | total | se vacía tras borrar manualmente todas las FKs hijas |

- Las FKs existentes no usan cascade para esos borrados; V36 borra hijos en un orden
  que permite borrar después ambos catálogos.
- El `ON DELETE CASCADE` sólo aparece en la nueva `actividad_recurso`, creada vacía
  después de los `DELETE`; no recupera ni explica datos anteriores.
- No hay backfill, copia, tabla de archivo ni `INSERT`. Nada de lo borrado puede
  reconstruirse automáticamente desde el estado posterior; requiere backup, logs o
  decisiones humanas. En particular reservas e IDs históricos no son inferibles.
- V38 altera `tipo_actividad`/`actividad_recurso` y elimina `salon.permite_pareja`;
  V39 cambia la semántica/nombre de cantidad; V40 añade etiquetas.
- El Java actual depende de las tablas renombradas y de las columnas finales, pero
  esa dependencia de esquema no justifica destruir los datos.
- El historial demuestra ejecución, no cuántas filas existían: la pérdida efectiva
  en local/dev no es determinable con la consulta autorizada.

## 11. Estado de publicación de V36

| Estado | Resultado | Evidencia |
|---|---|---|
| VERSIONADA EN BASELINE | SÍ | commit `143983c` del 2026-08-20 |
| ENVIADA A ORIGIN | SÍ, sólo branch baseline | `ls-remote`; `origin/main` permanece en `f18df68` y no contiene V36 |
| PUBLICADA EN RELEASE/DEPLOY | NO HAY EVIDENCIA | sin tags, releases, workflow ni config de deployment |
| EJECUTADA EN BD PERSISTENTE | SÍ | local/dev, volumen Docker nombrado, V36 instalada 2026-08-16 |
| EJECUTADA EN TEST EFÍMERO | SÍ | Fase 0B; no cuenta como despliegue |
| EJECUTADA EN STAGING/PROD | NO INSPECCIONADO | no hay acceso ni ambiente identificable |

El checksum local de V36 (`1241050759`) coincide con el archivo versionado. Por tanto,
V36 ya es una migración aplicada y debe tratarse como inmutable. Una nueva V>40 por
sí sola no protege una BD que aún esté en V35: V36 borraría antes de llegar a ella.

## 12. Estrategia máquina→recurso

Estrategia conceptual futura, sin SQL:

1. Inventariar historia, backup verificable y conteos/IDs de las ocho tablas por BD.
2. Separar dos rutas: ambientes `<36`, que necesitan preservación previa a V36, y
   ambientes `≥36`, que sólo pueden recuperar desde backup/import aprobado.
3. En una BD pre-V36, conservar los UUID y cantidades mediante rename de metadatos
   o tablas paralelas; nunca borrar `tipo_maquina`/`salon_maquina` para migrarlas.
4. Crear `actividad_recurso` de forma aditiva y mantener intactos actividad, oferta,
   especialidades, asignaciones, paquetes y reservas.
5. Backfill del catálogo/inventario uno-a-uno sólo cuando el significado anterior
   haya sido aprobado; conservar IDs facilita todas las referencias.
6. Cargar actividad→recurso/cantidad desde una matriz humana versionada. No inferir
   `Duo Reformer=2` ni el consumo total por nombre.
7. Verificar antes/después: conteos, conjunto de IDs, sumas de inventario, huérfanos,
   reservas, especialidades, asignaciones y composición de paquetes.
8. Cambiar lecturas/escrituras con compatibilidad temporal; retirar nombres antiguos
   únicamente en una migración contract posterior y con backup restaurable.
9. Para una BD ya migrada, una forward migration puede reconstruir sólo datos que
   existan en backup/export o mapping aprobado; no debe fabricar historia perdida.
10. Para una BD pre-V36, el rollout necesita una etapa de preservación anterior al
    paso destructivo y una restauración/transformación posterior. No desplegar el
    artefacto actual directamente ni asumir que una V41 aislada basta.

## 13. V23–V35

| Versión | Riesgo | Motivo sobre datos existentes |
|---|---|---|
| V23 | MEDIO | tabla/UNIQUE nuevos; default seguro, pero no convierte paquetes legacy a composición |
| V24 | ALTO | borra todo paquete con categoría; puede perder catálogo o fallar por compras FK |
| V25 | BAJO | FK de salón nullable, sin reescritura obligatoria |
| V26 | BAJO | columnas nullable e índice aditivos |
| V27 | BAJO | columna texto nullable |
| V28 | BAJO | inserta permisos nuevos; colisión sólo si hubo alta manual del mismo código |
| V29 | BAJO | permiso aditivo con el mismo riesgo de colisión manual |
| V30 | BAJO | cambia únicamente descripciones de permisos existentes |
| V31 | MEDIO | renombra códigos UNIQUE; puede colisionar y cambia contratos RBAC |
| V32 | MEDIO | renombra múltiples códigos; Java actual depende de los nombres finales |
| V33 | MEDIO | divide permisos; roles previos no reciben automáticamente todas las acciones |
| V34 | MEDIO | nuevo renombre de códigos con riesgo de colisión/configuración externa |
| V35 | MEDIO | elimina permiso y asignaciones `rol_permiso`; cambio RBAC destructivo |

Clasificación del bloque: **ALTO** por V24. No hay `TRUNCATE`, renames de tablas,
`SET NOT NULL` sin default/backfill ni `DROP TABLE` en V23–V35.

## 14. V37–V40

| Versión | Riesgo | Compatibilidad |
|---|---|---|
| V37 | MEDIO | inserta permisos y asignaciones; posibles códigos/relaciones manuales duplicados |
| V38 | ALTO | elimina `modo_consumo` y `salon.permite_pareja`; no transforma su semántica |
| V39 | ALTO | rename simple, pero reinterpreta cantidad por participante como total sin backfill |
| V40 | BAJO | añade `etiquetas` NOT NULL con default vacío; backfill automático seguro |

El Java actual requiere `participantes_por_reserva`, `actividad_recurso.cantidad`, la
ausencia de `permite_pareja` y `tipo_actividad.etiquetas`. Eso exige conservar la
secuencia, no asumir que los valores de V38/V39 son correctos para datos preexistentes.

## 15. Estrategia Flyway futura

- Forward-only: no editar nombre, contenido o checksum aplicado en ninguna BD
  persistente; una versión por número y archivo.
- Inventariar `flyway_schema_history` antes de cada promoción y comparar checksums.
- Migraciones pequeñas y aditivas: **expand → backfill verificable → contract**.
- Backfills separados, reanudables, medibles y con validaciones de conteo/orfandad.
- Probar desde esquema vacío y desde snapshot con datos representativos en
  PostgreSQL/Testcontainers; incluir rutas desde cada versión realmente existente.
- No mezclar rename, borrado masivo y cambio funcional en una sola migración.
- Backup consistente y restauración ensayada antes de cualquier cambio destructivo.
- `validateOnMigrate` permanece activo; no usar `repair` para ocultar divergencias.
- Despliegue bloqueado si un historial no es conocido o una transformación humana
  no está aprobada.

## 16. Decisión V22.1–V22.3

**V22.x: DEPENDE DEL HISTORIAL DE AMBIENTES.**

Pueden y deben conservarse para local/dev, donde ya están aplicadas con sus checksums,
y son válidas para instalaciones nuevas o escenario A. No constituyen una solución
para una BD escenario B: allí V14/V15/V16 significan otra cosa y el baseline falla.
Production no fue inspeccionado; por ello no se puede asumir compatibilidad global.

## 17. Decisión V36

- APTA PARA BASELINE GIT: **SÍ** — preserva exactamente el estado auditado.
- APTA PARA NUEVA BD VACÍA: **SÍ** — alcanza V40, aunque vacía catálogos semilla.
- APTA PARA BD CON DATOS: **NO** — borra ocho conjuntos de negocio.
- APTA PARA DEPLOY AHORA: **NO**.
- DEBE SER REEMPLAZADA ANTES DEL PRIMER DEPLOY: **NO, con la evidencia actual**.

El último `NO` no aprueba V36: significa que ya fue aplicada en una BD persistente y
no debe editarse retroactivamente. Debe congelarse y resolverse con estrategia
forward-only. Sólo una decisión explícita de descartar/recrear ese ambiente cambiaría
la premisa; esta auditoría no la asume ni la ejecuta.

## 18. Gate de deploy

- [ ] Identificar todos los ambientes reales y consultar cada historial read-only.
- [ ] Clasificar cada BD como escenario V22 A, B o nueva; ninguna desconocida.
- [ ] Confirmar backup consistente, retención y restauración ensayada.
- [ ] Decidir si local/dev se recupera o se declara desechable por autoridad humana.
- [ ] Congelar checksums ya aplicados, especialmente V22.x y V36–V40.
- [ ] Diseñar ruta segura para BDs `<36`; no ejecutar V36 actual directamente.
- [ ] Diseñar recuperación forward-only para BDs `≥36` con fuente real de datos.
- [ ] Aprobar mappings de recurso, actividad, cantidad, participantes y paquetes.
- [ ] Probar migración sobre copia con reservas, inventario, turnos y compras reales.
- [ ] Verificar conteos, IDs, FKs, permisos, búsquedas y compatibilidad Java V38–V40.
- [ ] Ensayar fallo intermedio y restauración; definir ventana y responsables.
- [ ] Obtener aprobación técnica/negocio y sólo entonces marcar deploy como seguro.

Estado actual: **SEGURO PARA DEPLOY = NO**.

## 19. Decisiones humanas pendientes

- Si la BD local/dev y sus datos anteriores a V36 deben recuperarse o son desechables.
- Mapping de cada máquina antigua a `TipoRecurso`, conservando o no el mismo ID.
- Recurso y cantidad total requerida por cada actividad; `Duo Reformer=2` requiere
  confirmación, no inferencia.
- Conversión de `POR_PARTICIPANTE`/`POR_SESION` al `cantidad` total de V39.
- `participantes_por_reserva` correcto por actividad y destino de `permite_pareja`.
- Composición/cantidades de paquetes y tratamiento de paquetes legacy/categorías.
- Política para reservas históricas, actividades inactivas y mappings incompletos.
- Existencia y acceso autorizado a cualquier BD compartida/production no documentada.

## 20. Siguiente acción

**Resultado B:** diseñar una intervención forward-only porque V36 ya se ejecutó.
La intervención debe comenzar por un backup/export autorizado y una matriz humana de
mappings, y debe contemplar por separado BDs `<36` y `≥36`. Antes de desplegar, si
existe cualquier ambiente compartido no documentado, obtener primero su
`flyway_schema_history`. No implementar SQL hasta cerrar esas entradas.
