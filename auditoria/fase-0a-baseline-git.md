# FeelingPilates — Fase 0A: Baseline Git

> Inspección de sólo lectura realizada el 2026-08-20. Las cifras excluyen este checkpoint, creado por la tarea. No se ejecutaron `add`, `commit`, `reset`, `restore`, `checkout`, `clean`, `stash`, cambios de branch ni instalaciones.

## 1. Estado del repositorio

- Raíz Git: `/Users/jesusaldaircruzortiz/Desktop/Feelingpilates/feelingpilates` (la carpeta superior no es repositorio).
- Branch: `main`; HEAD: `f18df68e31f53d0fa5b1974f7e878832b4409cd3`.
- Upstream: `origin/main`; relación: `ahead 0`, `behind 0`, no divergida.
- Remote fetch/push: `origin https://github.com/MacacosDevs/feellingPilates.git`.
- Estado: working tree sucio; índice limpio; 35 rutas tracked cambiadas y 55 archivos untracked antes de este checkpoint.
- Últimos 10 commits, del más reciente al más antiguo:
  1. `f18df68e31f53d0fa5b1974f7e878832b4409cd3` — 2026-08-16 — Ajustes de horarios,turnos y validaciones con asignaciones.
  2. `f435bcda84da55629766b68e0aedb72cf50293fe` — 2026-08-16 — Gestion de reservas, horarios, personal, permisos, actividades.
  3. `055b45a4498799cd12f155b3d349017b64b78126` — 2026-08-01 — Validaciones de los pagos de Stripe.
  4. `966c4abd6ce1485b7a1422cbbd11ef78f69d1aff` — 2026-07-30 — Integracion de login con Google, paquetes y Stripe.
  5. `7c00c24ff190b6e01959bc9c5c8618eb3c7cc04f` — 2026-07-22 — Login movil, perfil e imagen.
  6. `8c8df45faacb1a7de5e8958384fdb4bcd3ffe51f` — 2026-07-22 — Roles, usuarios, salones y mapas.
  7. `664d684cf3578048a46223edcac1ecf757e4faa5` — 2026-07-21 — Log DB environment variables in main method.
  8. `7bf7ca3b82b3e732d5e90245210a9824730b1ed5` — 2026-07-21 — Implement debug logging for DB environment variables.
  9. `ae79a464c7886c2faaef1af5b557d0db0b9d04c3` — 2026-07-21 — Add Dockerfile for multi-stage build.
  10. `a64a65774cd8adfbb678acf03eee1c155dc4bf4c` — 2026-07-21 — usuarios y configuraciones.

## 2. Resumen Git

| Tipo | Cantidad | Evidencia |
|---|---:|---|
| Tracked modificados | 23 | `git diff --name-status` |
| Tracked eliminados | 12 | nueve Java y tres SQL; `git ls-files --deleted` |
| Renombrados detectados por Git | 0 | `git diff --summary --find-renames`; los destinos siguen untracked |
| Untracked exactos | 55 | 32 Java, 21 SQL y 2 documentos; `git ls-files --others --exclude-standard` |
| Staged | 0 | `git diff --cached --name-status` vacío |
| Conflictos/unmerged | 0 | porcelain v2 sin entradas `u` |
| Diff tracked | 35 archivos | 263 inserciones, 387 eliminaciones |

Categorías: A producto; B test; C migración/esquema; D configuración versionable; E local/secreto; F documentación; G generado/build; H IDE/SO/temporal; I renombre/reemplazo; J duplicado/sospechoso; K no determinable. Rutas Java abreviadas desde `src/main/java/com/feelingpilates/` y SQL desde `src/main/resources/`.

## 3. Cambios tracked

| Estado Git | Archivo | Categoría | ¿Necesario? | Evidencia | Acción sugerida |
|---|---|---|---|---|---|
| M | `auth/AuthController.java` | A | Sí | Desactiva endpoint Google; coincide con build y test fallido conocidos | PRESERVAR EN BASELINE |
| M | `auth/AuthService.java` | A | Sí | Desactiva flujo Google en servicio; compila en conjunto actual | PRESERVAR EN BASELINE |
| M | `pagos/controlador/PagoController.java` | A | Sí | Retira reembolso Stripe; coordinado con `PagoService`, DTO eliminado y V35 | PRESERVAR EN BASELINE |
| M | `pagos/controlador/PaqueteController.java` | A | Sí | Usa `ActividadPaqueteResponse` untracked y paquetes con categoría nula | PRESERVAR EN BASELINE |
| M | `pagos/dto/PaqueteResponse.java` | A | Sí | Su contrato requiere `ActividadPaqueteResponse` | PRESERVAR EN BASELINE |
| D | `pagos/dto/ReembolsoResponse.java` | A | Sí, como eliminación | Ya no lo referencia el código actual; endpoint Stripe retirado | PRESERVAR EN BASELINE |
| M | `pagos/entidad/Compra.java` | A | Sí | Campos de caja/sede/grupo/motivo dependen de V23, V25–V27 | PRESERVAR EN BASELINE |
| M | `pagos/entidad/Paquete.java` | A | Sí | Relación con `PaqueteActividad` untracked y V23 | PRESERVAR EN BASELINE |
| M | `pagos/repositorio/CompraRepository.java` | A | Sí | Consultas usadas por `VentaService` untracked | PRESERVAR EN BASELINE |
| M | `pagos/repositorio/PaqueteRepository.java` | A | Sí | Método usado por `PaqueteGestionService` untracked | PRESERVAR EN BASELINE |
| M | `pagos/servicio/PagoService.java` | A | Sí | Retira reembolso Stripe; resto de pagos compila | PRESERVAR EN BASELINE |
| M | `seguridad/JwtAuthFilter.java` | A | Sí, crítico | Importa `ContextoAutenticacionService` untracked | PRESERVAR EN BASELINE |
| M | `ubicaciones/controlador/SalonController.java` | A | Sí | Contrato de salón ahora devuelve fecha de creación | PRESERVAR EN BASELINE |
| M | `ubicaciones/controlador/TipoActividadController.java` | A | Sí | Usa participantes, etiquetas, permisos V37 y búsqueda V40 | PRESERVAR EN BASELINE |
| D | `ubicaciones/controlador/TipoMaquinaController.java` | I | Sí, como sustitución | Reemplazado mecánicamente por `TipoRecursoController` | PARECE RENOMBRE |
| M | `ubicaciones/dto/CatalogoItemRequest.java` | A | Sí | Añade participantes/etiquetas consumidos por controller actual | PRESERVAR EN BASELINE |
| D | `ubicaciones/dto/MaquinaItem.java` | I | Sí, como sustitución | Equivalente funcional `RecursoItem` | PARECE RENOMBRE |
| D | `ubicaciones/dto/MaquinaItemResponse.java` | I | Sí, como sustitución | Equivalente funcional `RecursoItemResponse` | PARECE RENOMBRE |
| M | `ubicaciones/dto/SalonDetalleResponse.java` | A | Sí | Cambia máquinas por `RecursoItemResponse` untracked | PRESERVAR EN BASELINE |
| M | `ubicaciones/dto/SalonRequest.java` | A | Sí | Cambia máquinas por `RecursoItem` untracked | PRESERVAR EN BASELINE |
| M | `ubicaciones/dto/SalonResponse.java` | A | Sí | Sustituye `permitePareja` por `creadoEn`; V38 elimina columna | PRESERVAR EN BASELINE |
| M | `ubicaciones/dto/TipoActividadResponse.java` | A | Sí | Refleja V38/V40 y entidad actual | PRESERVAR EN BASELINE |
| D | `ubicaciones/dto/TipoMaquinaResponse.java` | I | Sí, como sustitución | Equivalente funcional `TipoRecursoResponse` | PARECE RENOMBRE |
| M | `ubicaciones/entidad/Salon.java` | A | Sí | Retira campo eliminado por V38 | PRESERVAR EN BASELINE |
| D | `ubicaciones/entidad/SalonMaquina.java` | I | Sí, como sustitución | V36 renombra tabla; destino `SalonRecurso` | PARECE RENOMBRE |
| M | `ubicaciones/entidad/TipoActividad.java` | A | Sí | Mapea columnas V38 y V40 | PRESERVAR EN BASELINE |
| D | `ubicaciones/entidad/TipoMaquina.java` | I | Sí, como sustitución | V36 renombra tabla; destino `TipoRecurso` | PARECE RENOMBRE |
| D | `ubicaciones/repositorio/SalonMaquinaRepository.java` | I | Sí, como sustitución | Reemplazado por `SalonRecursoRepository` | PARECE RENOMBRE |
| M | `ubicaciones/repositorio/SalonRepository.java` | A | Sí | Proyección actual usa `creado_en`, no `permite_pareja` | PRESERVAR EN BASELINE |
| M | `ubicaciones/repositorio/TipoActividadRepository.java` | A | Sí | Consulta `etiquetas` creada por V40 | PRESERVAR EN BASELINE |
| D | `ubicaciones/repositorio/TipoMaquinaRepository.java` | I | Sí, como sustitución | Reemplazado por `TipoRecursoRepository` | PARECE RENOMBRE |
| M | `ubicaciones/servicio/SalonService.java` | A | Sí, crítico | Importa seis tipos/repositories de recurso untracked | PRESERVAR EN BASELINE |
| D | `db/migration/V14__paquetes_y_compras.sql` | I | Sí, renumerada | Blob idéntico a V22.1 | PARECE RENOMBRE |
| D | `db/migration/V15__compra_idempotencia.sql` | I | Sí, renumerada | Blob idéntico a V22.2 | PARECE RENOMBRE |
| D | `db/migration/V16__permiso_reembolsar_pagos.sql` | I | Sí, renumerada | Blob idéntico a V22.3 | PARECE RENOMBRE |

## 4. Archivos untracked

| Estado Git | Archivo | Categoría | ¿Necesario? | Evidencia | Acción sugerida |
|---|---|---|---|---|---|
| ?? | `auditoria/00-revalidacion-repositorio-completo.md` | F | Sí | Fuente del build/Flyway ya comprobados | PRESERVAR EN BASELINE |
| ?? | `auditoria/04-arquitectura-objetivo.md` | F | Sí | Arquitectura objetivo y prerequisito de baseline | PRESERVAR EN BASELINE |
| ?? | `pagos/controlador/PaqueteGestionController.java` | A | Sí | Entrada REST a `PaqueteGestionService` | PRESERVAR EN BASELINE |
| ?? | `pagos/controlador/VentaController.java` | A | Sí | Entrada REST a `VentaService`; permisos V32–V34 | PRESERVAR EN BASELINE |
| ?? | `pagos/dto/ActividadPaqueteRequest.java` | A | Sí | Usado por requests y `PaqueteGestionService` | PRESERVAR EN BASELINE |
| ?? | `pagos/dto/ActividadPaqueteResponse.java` | A | Sí, crítico | Referido por dos clases tracked | PRESERVAR EN BASELINE |
| ?? | `pagos/dto/ActualizarPaqueteRequest.java` | A | Sí | Usado por controller/service de paquetes | PRESERVAR EN BASELINE |
| ?? | `pagos/dto/CambiarEstadoVentaRequest.java` | A | Sí | Usado por `VentaController` | PRESERVAR EN BASELINE |
| ?? | `pagos/dto/CrearPaqueteRequest.java` | A | Sí | Usado por controller/service de paquetes | PRESERVAR EN BASELINE |
| ?? | `pagos/dto/ItemCarritoRequest.java` | A | Sí | Usado por carrito y `VentaService` | PRESERVAR EN BASELINE |
| ?? | `pagos/dto/PaqueteGestionResponse.java` | A | Sí | Contrato controller/service de paquetes | PRESERVAR EN BASELINE |
| ?? | `pagos/dto/RegistrarVentaCarritoRequest.java` | A | Sí | Contrato de `VentaController` | PRESERVAR EN BASELINE |
| ?? | `pagos/dto/RegistrarVentaRequest.java` | A | Sí | Contrato de `VentaController` | PRESERVAR EN BASELINE |
| ?? | `pagos/dto/SedeVentaResponse.java` | A | Sí | Usado por `VentaService/Controller` | PRESERVAR EN BASELINE |
| ?? | `pagos/dto/VentaCarritoResponse.java` | A | Sí | Usado por `VentaService/Controller` | PRESERVAR EN BASELINE |
| ?? | `pagos/dto/VentaResponse.java` | A | Sí | Usado por venta simple, carrito e historial | PRESERVAR EN BASELINE |
| ?? | `pagos/entidad/PaqueteActividad.java` | A | Sí, crítico | Referida por `Paquete` tracked; tabla V23 | PRESERVAR EN BASELINE |
| ?? | `pagos/servicio/PaqueteGestionService.java` | A | Sí | Implementa catálogo; usa repositorios tracked | PRESERVAR EN BASELINE |
| ?? | `pagos/servicio/VentaService.java` | A | Sí | Implementa caja, carrito e historial | PRESERVAR EN BASELINE |
| ?? | `seguridad/ContextoAutenticacionService.java` | A | Sí, crítico | Requerido por `JwtAuthFilter` tracked | PRESERVAR EN BASELINE |
| ?? | `ubicaciones/controlador/ActividadRecursoController.java` | A | Sí | Gestiona relación creada por V36/V39 | PRESERVAR EN BASELINE |
| ?? | `ubicaciones/controlador/TipoRecursoController.java` | I | Sí | Sustituye controller de máquinas | PARECE RENOMBRE |
| ?? | `ubicaciones/dto/ActividadRecursoRequest.java` | A | Sí | Contrato de `ActividadRecursoController` | PRESERVAR EN BASELINE |
| ?? | `ubicaciones/dto/ActividadRecursoResponse.java` | A | Sí | Contrato de `ActividadRecursoController` | PRESERVAR EN BASELINE |
| ?? | `ubicaciones/dto/RecursoItem.java` | I | Sí, crítico | Requerido por `SalonRequest/SalonService` tracked | PARECE RENOMBRE |
| ?? | `ubicaciones/dto/RecursoItemResponse.java` | I | Sí, crítico | Requerido por response/service tracked | PARECE RENOMBRE |
| ?? | `ubicaciones/dto/TipoRecursoResponse.java` | I | Sí | Sustituye response de máquinas | PARECE RENOMBRE |
| ?? | `ubicaciones/entidad/ActividadRecurso.java` | A | Sí | Mapea tabla V36 y columna final V39 | PRESERVAR EN BASELINE |
| ?? | `ubicaciones/entidad/SalonRecurso.java` | I | Sí, crítico | Sustituye `SalonMaquina`; tabla renombrada V36 | PARECE RENOMBRE |
| ?? | `ubicaciones/entidad/TipoRecurso.java` | I | Sí, crítico | Sustituye `TipoMaquina`; ampliamente referida | PARECE RENOMBRE |
| ?? | `ubicaciones/repositorio/ActividadRecursoRepository.java` | A | Sí | Requerido por controller de relación | PRESERVAR EN BASELINE |
| ?? | `ubicaciones/repositorio/SalonRecursoRepository.java` | I | Sí, crítico | Requerido por `SalonService` tracked | PARECE RENOMBRE |
| ?? | `ubicaciones/repositorio/TipoRecursoRepository.java` | I | Sí, crítico | Requerido por service y dos controllers | PARECE RENOMBRE |
| ?? | `usuarios/controlador/PermisoController.java` | A | Sí | Expone `RolService#listarPermisos` | PRESERVAR EN BASELINE |
| ?? | `db/migration/V22_1__paquetes_y_compras.sql` | I | Sí | Contenido exacto de V14 pagos, sin colisión de versión | PARECE RENOMBRE |
| ?? | `db/migration/V22_2__compra_idempotencia.sql` | I | Sí | Contenido exacto de V15 idempotencia | PARECE RENOMBRE |
| ?? | `db/migration/V22_3__permiso_reembolsar_pagos.sql` | I | Sí | Contenido exacto de V16 reembolso | PARECE RENOMBRE |
| ?? | `db/migration/V23__caja_paquete_actividades.sql` | C | Sí | Esquema usado por catálogo/ventas | PRESERVAR EN BASELINE |
| ?? | `db/migration/V24__eliminar_paquetes_semilla.sql` | C | Sí | Estado de datos del catálogo gestionable | PRESERVAR EN BASELINE |
| ?? | `db/migration/V25__compra_salon.sql` | C | Sí | `Compra.salon` y filtros de venta | PRESERVAR EN BASELINE |
| ?? | `db/migration/V26__compra_grupo.sql` | C | Sí | Carrito/ticket de `VentaService` | PRESERVAR EN BASELINE |
| ?? | `db/migration/V27__compra_motivo_estado.sql` | C | Sí | Estado/reembolso local de venta | PRESERVAR EN BASELINE |
| ?? | `db/migration/V28__permisos_caja_granulares.sql` | C | Sí | Base de permisos luego renombrados | PRESERVAR EN BASELINE |
| ?? | `db/migration/V29__permiso_vista_caja.sql` | C | Sí | Base de permiso de vista luego renombrado | PRESERVAR EN BASELINE |
| ?? | `db/migration/V30__simplificar_descripcion_permisos_caja.sql` | C | Sí | Conserva estado textual del catálogo RBAC | PRESERVAR EN BASELINE |
| ?? | `db/migration/V31__reestructurar_permisos_caja.sql` | C | Sí | Paso requerido antes de códigos V32 | PRESERVAR EN BASELINE |
| ?? | `db/migration/V32__renombrar_permisos_caja_a_venta.sql` | C | Sí | Códigos usados por `VentaController` | PRESERVAR EN BASELINE |
| ?? | `db/migration/V33__granularizar_permisos_catalogo_venta.sql` | C | Sí | Permisos usados por gestión de paquetes | PRESERVAR EN BASELINE |
| ?? | `db/migration/V34__renombrar_permisos_catalogo_a_servicios.sql` | C | Sí | Códigos finales de `PaqueteGestionController` | PRESERVAR EN BASELINE |
| ?? | `db/migration/V35__eliminar_permiso_venta_reembolsar.sql` | C | Sí | Coordinado con eliminación de endpoint Stripe | PRESERVAR EN BASELINE |
| ?? | `db/migration/V36__recursos_y_actividad_recurso.sql` | C | Sí; despliegue riesgoso | Esquema requerido por clases de recurso | PRESERVAR EN BASELINE |
| ?? | `db/migration/V37__permisos_actividades.sql` | C | Sí | Authorities usadas por actividad/recursos | PRESERVAR EN BASELINE |
| ?? | `db/migration/V38__participantes_por_reserva.sql` | C | Sí | `TipoActividad` y `Salon` actuales | PRESERVAR EN BASELINE |
| ?? | `db/migration/V39__cantidad_actividad_recurso.sql` | C | Sí | Mapeo final de `ActividadRecurso.cantidad` | PRESERVAR EN BASELINE |
| ?? | `db/migration/V40__etiquetas_actividad.sql` | C | Sí | Entidad y consulta de etiquetas | PRESERVAR EN BASELINE |

No hay cambios B, D, E, G, H, J o K entre las 90 rutas inventariadas. El checkpoint nuevo se excluye de esta tabla por instrucción.

## 5. Archivos ignorados/sensibles

| Archivo/grupo | Estado | Riesgo | Recomendación conceptual |
|---|---|---|---|
| `.env` | ignored | Configuración local potencialmente sensible: BD, JWT, Google y Stripe | NO VERSIONAR; EXCLUIR DEL BASELINE |
| `.idea/` (16 archivos) | ignored | IDE; `dataSources*` puede contener metadatos locales de BD | EXCLUIR DEL BASELINE |
| `.claude/settings.local.json` | ignored globalmente | Preferencias/permisos locales | EXCLUIR DEL BASELINE |
| `target/` (223 archivos, 1.1 MB) | ignored | Clases y salidas Maven generadas | EXCLUIR DEL BASELINE |
| `HELP.md` | ignored | Guía generada por plantilla Spring | EXCLUIR DEL BASELINE |

`application.properties` está tracked, sin cambio, y delega los campos sensibles a variables de entorno. `.env.example` está tracked, sin cambio, como plantilla. No se copiaron valores. No aparecen logs, dumps, backups, `.DS_Store`, `.vscode`, `node_modules`, `build`, `dist` o `coverage` dentro de esta raíz Git.

## 6. Código Java

- Clases Java afectadas: 32 tracked (23 M, 9 D) y 32 untracked. Todas son producto (no tests); módulo: auth 2, seguridad 2, pagos 26, ubicaciones 33 y usuarios 1.
- Funcionalidad nueva/intencional: ventas presenciales y carrito, catálogo de paquetes por actividad, permisos vivos desde BD, recursos genéricos, participantes/etiquetas de actividad y listado de permisos.
- Reemplazos: ocho clases/interfaces `*Maquina*` pasan a `*Recurso*`; no son copias coexistentes. `ReembolsoResponse` y el reembolso Stripe se eliminan, coordinados con V35.
- Dependencias actuales: los casos críticos están en las tablas anteriores y en §12; omitir cualquier destino untracked rompe compilación o mapeo JPA.
- Compilación: `00-revalidacion` documenta compilación limpia exitosa de las 150 fuentes actuales. Las clases eliminadas no participaron; todas las actuales sí.
- Tests: no hay Java de test cambiado/untracked. Resultado previo: 10 tests, 9 pasan y uno falla (`AuthControllerTest#googleStubDevuelve501`, esperado 501/obtenido 500 por endpoint Google comentado).
- No se identificaron fuentes experimentales, copias o clases abandonadas fuera de los reemplazos explícitos.

## 7. Flyway

| Versión/archivo | Estado en HEAD | Estado working tree | Git | Observación |
|---|---|---|---|---|
| V1 | existe | existe | tracked igual | esquema usuarios/RBAC |
| V2 | existe | existe | tracked igual | datos RBAC |
| V3 | existe | existe | tracked igual | invitaciones |
| V4 | existe | existe | tracked igual | admin inicial |
| V5 | existe | existe | tracked igual | permiso activar usuario |
| V6 | existe | existe | tracked igual | super admin |
| V7 | existe | existe | tracked igual | categoría permiso |
| V8 | existe | existe | tracked igual | gestión roles |
| V9 | existe | existe | tracked igual | ubicaciones |
| V10 | existe | existe | tracked igual | salones semilla |
| V11 | existe | existe | tracked igual | gestión salones |
| V12 | existe | existe | tracked igual | dirección salón |
| V13 | existe | existe | tracked igual | foto usuario |
| V14 inventario máquinas | existe | existe | tracked igual | una de dos V14 en HEAD |
| V14 paquetes/compras | existe | no existe | deleted | contenido reaparece como V22.1 |
| V15 calendario | existe | existe | tracked igual | una de dos V15 en HEAD |
| V15 idempotencia | existe | no existe | deleted | contenido reaparece como V22.2 |
| V16 permisos calendario | existe | existe | tracked igual | una de dos V16 en HEAD |
| V16 permiso reembolso | existe | no existe | deleted | contenido reaparece como V22.3 |
| V17 | existe | existe | tracked igual | actividad en turno |
| V18 | existe | existe | tracked igual | excepción horario |
| V19 | existe | existe | tracked igual | múltiples instructores |
| V20 | existe | existe | tracked igual | asignación turno |
| V21 | existe | existe | tracked igual | limpieza asignaciones |
| V22 | existe | existe | tracked igual | rango asignación |
| V22.1 | no existe | existe | untracked | renumeración exacta V14 pagos |
| V22.2 | no existe | existe | untracked | renumeración exacta V15 idempotencia |
| V22.3 | no existe | existe | untracked | renumeración exacta V16 reembolso |
| V23 | no existe | existe | untracked | paquete-actividad/caja |
| V24 | no existe | existe | untracked | elimina semillas |
| V25 | no existe | existe | untracked | sede de compra |
| V26 | no existe | existe | untracked | grupo/ticket |
| V27 | no existe | existe | untracked | motivo de estado |
| V28 | no existe | existe | untracked | permisos caja |
| V29 | no existe | existe | untracked | vista caja |
| V30 | no existe | existe | untracked | descripciones permisos |
| V31 | no existe | existe | untracked | reestructura permisos |
| V32 | no existe | existe | untracked | renombra caja a venta |
| V33 | no existe | existe | untracked | granulariza catálogo |
| V34 | no existe | existe | untracked | catálogo a servicios |
| V35 | no existe | existe | untracked | elimina reembolso Stripe |
| V36 | no existe | existe | untracked | recursos; destructiva |
| V37 | no existe | existe | untracked | permisos actividades |
| V38 | no existe | existe | untracked | participantes/esquema recurso |
| V39 | no existe | existe | untracked | cantidad total recurso |
| V40 | no existe | existe | untracked | etiquetas actividad |

HEAD tiene 25 archivos pero versiones duplicadas 14/15/16; Flyway no puede validar ese conjunto. El working tree tiene 43 versiones únicas (`1..22`, `22.1..22.3`, `23..40`) y la revalidación aplicó las 43 hasta v40 en PostgreSQL vacío. Salvo los tres pares renumerados, los hashes no muestran otras migraciones distintas con contenido idéntico.

Riesgo de historial/checksum: Flyway identifica por versión/descripcion/checksum, no sólo por contenido. Si un ambiente ya registró las migraciones de pagos como V14/V15/V16, retirarlas y volverlas a ofrecer como V22.x puede producir migraciones aplicadas ausentes, objetos duplicados o validación fallida. Debe inventariarse `flyway_schema_history` de cada ambiente antes del baseline/despliegue.

## 8. Renumeración V14/V15/V16

| Archivo anterior | Archivo actual | Contenido equivalente | Cambios adicionales |
|---|---|---|---|
| `V14__paquetes_y_compras.sql` | `V22_1__paquetes_y_compras.sql` | Sí, SHA-256 idéntico | Ninguno; sólo nombre/versión |
| `V15__compra_idempotencia.sql` | `V22_2__compra_idempotencia.sql` | Sí, SHA-256 idéntico | Ninguno; sólo nombre/versión |
| `V16__permiso_reembolsar_pagos.sql` | `V22_3__permiso_reembolsar_pagos.sql` | Sí, SHA-256 idéntico | Ninguno; sólo nombre/versión |

La sustitución elimina las colisiones con V14 inventario, V15 calendario y V16 permisos calendario. Explica por qué el conjunto del working tree valida en esquema vacío; no demuestra seguridad sobre historiales ya aplicados.

## 9. Migraciones V23–V40

| Versión | Capacidad | Código actual dependiente | Resultado |
|---|---|---|---|
| V23 | paquete-actividad y método/cajero | `Paquete`, `PaqueteActividad`, `VentaService` | PRESERVAR EN BASELINE |
| V24 | retira catálogo mock sembrado | `PaqueteGestionService` gestiona catálogo real | PRESERVAR EN BASELINE |
| V25 | sede de venta | `Compra.salon`, `VentaService`, filtros repository | PRESERVAR EN BASELINE |
| V26 | ticket multi-línea | `grupoCompraId/numeroItem`, carrito | PRESERVAR EN BASELINE |
| V27 | motivo cancelar/reembolsar | `Compra.motivoEstado`, `VentaService` | PRESERVAR EN BASELINE |
| V28 | permisos granulares caja | cadena histórica de permisos V31/V32 | PRESERVAR EN BASELINE |
| V29 | permiso de vista | cadena histórica V31/V32 | PRESERVAR EN BASELINE |
| V30 | textos de permisos | catálogo RBAC/UI | PRESERVAR EN BASELINE |
| V31 | estructura de permisos caja | precondición de V32 | PRESERVAR EN BASELINE |
| V32 | códigos `venta.*` | `VentaController` | PRESERVAR EN BASELINE |
| V33 | granularidad catálogo | precondición de V34 | PRESERVAR EN BASELINE |
| V34 | códigos `venta.servicios.*` | `PaqueteGestionController` | PRESERVAR EN BASELINE |
| V35 | quita permiso reembolso Stripe | eliminación tracked de endpoint/service/DTO | PRESERVAR EN BASELINE |
| V36 | máquinas→recursos y relación actividad-recurso | todo `Tipo/Salon/ActividadRecurso` | PRESERVAR EN BASELINE |
| V37 | permisos actividades | `TipoActividadController`, recurso/actividad | PRESERVAR EN BASELINE |
| V38 | participantes; retira `permite_pareja` | `TipoActividad`, `Salon`, DTOs | PRESERVAR EN BASELINE |
| V39 | cantidad total por actividad | `ActividadRecurso.cantidad` | PRESERVAR EN BASELINE |
| V40 | etiquetas `text[]` | entidad, DTO, búsqueda repository | PRESERVAR EN BASELINE |

## 10. Riesgo V36

- Estado Git: untracked y necesaria para reproducir el working tree; renombra `tipo_maquina→tipo_recurso`, `salon_maquina→salon_recurso`, la FK y crea `actividad_recurso`.
- Destrucción: ejecuta `DELETE` total sobre `salon_recurso`, `salon_tipo_actividad`, `turno_instructor_asignacion`, `instructor_actividad`, `paquete_actividad`, `reserva`, `tipo_recurso` y `tipo_actividad`. No elimina esas tablas, pero sí sus datos.
- Dependencias posteriores/código: V38 altera `tipo_actividad/actividad_recurso`, V39 renombra su cantidad y V40 altera `tipo_actividad`; `SalonService` usa repositories de recurso, las clases `*Maquina*` están eliminadas y `ActividadRecurso` requiere la tabla nueva.
- Clasificación baseline: **NECESARIA PARA REPRODUCIR WORKING TREE**.
- Seguridad despliegue real: **NO CONFIRMADO / RIESGOSA**. Preservarla en Git no autoriza ejecutarla sobre datos reales.

## 11. Frontend/mobile/otros

- Esta raíz Git contiene sólo backend; no contiene `web` ni mobile. `../FeelingPiltaesAppMobile` es otro Git y `../web` está fuera de esta raíz: no forman parte de estas 90 rutas.
- El mobile tiene estado propio no limpio observado en sólo lectura y requiere baseline separado; no se debe hacer staging desde la carpeta superior ni mezclar repositorios.

## 12. Dependencias críticas entre cambios

| Origen actual | Dependencia no tracked/esquema | Impacto |
|---|---|---|
| `JwtAuthFilter` M | `ContextoAutenticacionService` ?? | CRÍTICO PARA BASELINE: omitirlo rompe compilación |
| `PaqueteController/Response/Paquete` M | DTOs y `PaqueteActividad` ?? + V23 | CRÍTICO: contrato/JPA incompletos |
| `Compra/Repositories` M | `VentaService/Controller/DTOs` ?? + V23,V25–V27 | CRÍTICO: caja y carrito incompletos |
| `SalonService/Request/Response` M | `Recurso*` ?? + V36 | CRÍTICO: imports y esquema inexistentes |
| `TipoActividad` y repository M | V38–V40 + DTOs actuales | CRÍTICO: columnas requeridas por JPA/SQL |
| Clases `*Maquina*` D | clases `*Recurso*` ?? | CRÍTICO: preservar borrados y destinos juntos |
| Reembolso Stripe retirado | V35 | Mantener código y permiso en el mismo snapshot |
| Authorities `venta.*`/`actividades.*` | V28–V37 | Sin migraciones, autorización no coincide con BD |

## 13. Reproducibilidad de HEAD

- ¿Compilaría? **NO CONFIRMADO**: por inspección, HEAD posee sus dependencias Java tracked, pero no se compiló aislado en esta fase.
- ¿Flyway validaría? **NO**: contiene dos migraciones distintas para cada versión V14, V15 y V16.
- ¿Faltaría funcionalidad? **SÍ**: no incluye ventas/catálogo actuales, permisos vivos, recursos genéricos, participantes/etiquetas ni V22.1–V40.
- ¿Qué no obtendría un clon? Los 55 untracked; recibiría versiones anteriores de 23 modificados y conservaría 12 eliminados. HEAD tiene 127 Java/25 migraciones frente a 150/43 actuales; no reproduce el sistema inspeccionado.

## 14. Reproducibilidad del working tree

**BASELINE CANDIDATO: SÍ CON REVISIÓN.** Excluyendo secretos/IDE/build y este checkpoint, los 90 cambios forman un conjunto coordinado: las referencias Java cierran, las sustituciones tienen destino, 150 fuentes compilaron y 43 migraciones se aplicaron en BD vacía. La revisión obligatoria es el historial Flyway de cada ambiente, el carácter destructivo de V36 y el único test fallido; no son motivo para perder el snapshot.

## 15. Archivos a preservar

- Código: los 23 Java modificados, las nueve eliminaciones Java como parte del cambio y los 32 Java untracked.
- Migraciones: las tres eliminaciones V14/V15/V16, sus renumeraciones exactas V22.1–V22.3 y V23–V40; preservar el conjunto indivisible.
- Tests/config: no hay cambios; documentar 9/10 y conservar lo ya tracked (`pom.xml`, properties, `.env.example`, wrapper, Docker).
- Documentación: `auditoria/00-revalidacion-repositorio-completo.md`, `04-arquitectura-objetivo.md` y este checkpoint.

## 16. Archivos a excluir

- `.env` y cualquier credencial/token/certificado local.
- `target/`, clases, reportes y cualquier salida de build.
- `.idea/`, `.claude/settings.local.json`, `HELP.md` ignorado y otros ajustes locales/temporales/logs/dumps/backups.
- Todo archivo de los repositorios hermanos: deben preservarse en su propio Git, no en éste.

## 17. Archivos ambiguos

- Ninguna de las 90 rutas quedó en K; requieren decisión humana la compatibilidad V22.x con cada `flyway_schema_history`, V36 con datos y el baseline separado del mobile.
- El fallo Google/test se preserva como estado conocido y debe corregirse después, no ocultarse del snapshot.

## 18. Estrategia de commit recomendada

**C. Crear primero una branch de preservación y luego ordenar commits**, con un único commit snapshot inicial. Ventaja: conserva fielmente un estado cuya historia parcial se desconoce, sin fingir commits por dominio ni separar dependencias inseparables. Después, los fixes/refactors sí deben ir en commits pequeños.

Riesgos: el snapshot mezcla seguridad, ventas, recursos y migraciones; su revisión semántica es más difícil. Mitigación: este manifiesto, staging acotado, diff cached y tag/PR sólo después de build/test. La branch por sí sola no preserva archivos: el snapshot debe confirmarse después del checklist. No dividir V22.x–V40 ni los reemplazos máquina→recurso antes de tener una copia recuperable.

## 19. Checklist antes de baseline

- [ ] `git status --short` coincide con este inventario más este checkpoint.
- [ ] Branch/upstream/HEAD vuelven a registrarse; no aparecieron cambios concurrentes.
- [ ] Las 90 rutas previas y el checkpoint tienen decisión explícita de staging.
- [ ] `.env`, `.idea`, `.claude`, `target` y `HELP.md` siguen fuera del índice.
- [ ] No hay valores secretos en tracked/untracked ni en el diff staged.
- [ ] Los 32 Java y 21 SQL untracked están incluidos; no sólo los tracked.
- [ ] Borrados `*Maquina*` y destinos `*Recurso*` se incluyen juntos.
- [ ] V14/V15/V16 y V22.1/V22.2/V22.3 se incluyen juntos.
- [ ] Se inventarió `flyway_schema_history` de cada ambiente real.
- [ ] V36 fue revisada/aceptada como preservación y bloqueada para producción sin ensayo.
- [ ] Build limpio desde copia sin `.env` ni `target` vuelve a compilar 150 fuentes.
- [ ] Flyway valida 43 migraciones en PostgreSQL vacío.
- [ ] Tests y el fallo conocido Google quedan documentados; ningún fallo nuevo.
- [ ] `git diff --cached --name-status`, `--stat` y `--check` fueron revisados.
- [ ] El mobile, si se preservará, tiene un procedimiento independiente.
- [ ] Mensaje de commit declara “snapshot baseline” y no atribuye historia desconocida.

## 20. Comandos futuros propuestos

> **PROPUESTA; NO EJECUTADA.** Usar sólo si el status sigue coincidiendo con este checkpoint.

```bash
git switch -c baseline/preservacion-working-tree-2026-08-20
git status --short
git add -- src/main/java/com/feelingpilates/auth src/main/java/com/feelingpilates/pagos
git add -- src/main/java/com/feelingpilates/seguridad/JwtAuthFilter.java src/main/java/com/feelingpilates/seguridad/ContextoAutenticacionService.java
git add -- src/main/java/com/feelingpilates/ubicaciones src/main/java/com/feelingpilates/usuarios/controlador/PermisoController.java
git add -- src/main/resources/db/migration
git add -- auditoria/00-revalidacion-repositorio-completo.md auditoria/04-arquitectura-objetivo.md auditoria/fase-0a-baseline-git.md
git diff --cached --name-status
git diff --cached --stat
git diff --cached --check
git status --short
git commit -m "chore: preservar snapshot baseline funcional"
```

No usar `git add .` ni `git clean -fd`. Antes del commit deben inspeccionarse también los historiales Flyway reales y repetir build/tests; los comandos anteriores sólo describen staging explícitamente acotado.
