# FeelingPilates — Lane 0: Allowlist y Autoridad de Integración

Fecha: 2026-09-20
Branch operativa: `AldairCruz7/integracion-backend-lane0-baseline-flyway`
Base HEAD: `f18df68e31f53d0fa5b1974f7e878832b4409cd3` (origin/main)

## 1. Autoridades de Lane 0

1. **Saneamiento Flyway pre-release (Autoridad A)**:
   - Commit: `769ca0a279a69794dcfb08db8e55378240231a82`
   - Documento: `auditoria/fase-0c1-saneamiento-flyway.md`
   - Resuelve el conflicto de números de migración duplicados (V14, V15, V16 de pagos vs calendario/ubicaciones).
   - Establece la secuencia canónica V1→V40 (renombrando duplicados a V22_1, V22_2, V22_3 y saneando V24, V36, V38, V39).

2. **Restablecimiento del baseline de tests y compilación (Autoridad B)**:
   - Commit: `f029c67d72788b1a3f3d696ac3e10d5fa12b2afc`
   - Documento: `auditoria/fase-0d-tests-baseline.md`
   - Resuelve el fallo en compilación y tests del flujo de autenticación de Google.
   - Establece el contrato funcional: Google login deshabilitado responde HTTP 501 Not Implemented vía `GoogleLoginDisabledException` y `GlobalExceptionHandler`.
   - Limpieza de código muerto en `AuthService.java` (`MAIN_BASELINE_ADAPTATION`): retiro de referencias a `crearClienteGoogle` inexistente y campo no cableado `GoogleTokenVerifier`.

3. **Clausura mínima de esquema JPA para V36 (Autoridad de Preservación 0B / 0C)**:
   - Documentos: `auditoria/fase-0a-baseline-git.md`, `auditoria/fase-0b-preservacion-baseline.md`, `auditoria/fase-0c-flyway-ambientes.md`
   - La migración V36 renombra en PostgreSQL las tablas `tipo_maquina` -> `tipo_recurso` y `salon_maquina` -> `salon_recurso`.
   - Para que la validación estricta de Hibernate (`spring.jpa.hibernate.ddl-auto=validate`) sea exitosa, las entidades JPA y sus clases dependientes en `ubicaciones` deben alinearse al esquema relacional canónico (`Recurso` en lugar de `Maquina`).

## 2. Clasificación exhaustiva del inventario histórico

Todos los caminos presentes en la genealogía histórica entre `f18df68` y `f029c67` están clasificados de la siguiente manera:

### LANE0_ACCEPTED (Incluidos en el delta de integración de Lane 0)
- **Migraciones Flyway V1→V40**:
  - `src/main/resources/db/migration/V14__paquetes_y_compras.sql` (eliminada por renumeración a V22_1)
  - `src/main/resources/db/migration/V15__compra_idempotencia.sql` (eliminada por renumeración a V22_2)
  - `src/main/resources/db/migration/V16__permiso_reembolsar_pagos.sql` (eliminada por renumeración a V22_3)
  - `src/main/resources/db/migration/V22_1__paquetes_y_compras.sql`
  - `src/main/resources/db/migration/V22_2__compra_idempotencia.sql`
  - `src/main/resources/db/migration/V22_3__permiso_reembolsar_pagos.sql`
  - `src/main/resources/db/migration/V23__caja_paquete_actividades.sql`
  - `src/main/resources/db/migration/V24__eliminar_paquetes_semilla.sql`
  - `src/main/resources/db/migration/V25__compra_salon.sql`
  - `src/main/resources/db/migration/V26__compra_grupo.sql`
  - `src/main/resources/db/migration/V27__compra_motivo_estado.sql`
  - `src/main/resources/db/migration/V28__permisos_caja_granulares.sql`
  - `src/main/resources/db/migration/V29__permiso_vista_caja.sql`
  - `src/main/resources/db/migration/V30__simplificar_descripcion_permisos_caja.sql`
  - `src/main/resources/db/migration/V31__reestructurar_permisos_caja.sql`
  - `src/main/resources/db/migration/V32__renombrar_permisos_caja_a_venta.sql`
  - `src/main/resources/db/migration/V33__granularizar_permisos_catalogo_venta.sql`
  - `src/main/resources/db/migration/V34__renombrar_permisos_catalogo_a_servicios.sql`
  - `src/main/resources/db/migration/V35__eliminar_permiso_venta_reembolsar.sql`
  - `src/main/resources/db/migration/V36__recursos_y_actividad_recurso.sql`
  - `src/main/resources/db/migration/V37__permisos_actividades.sql`
  - `src/main/resources/db/migration/V38__participantes_por_reserva.sql`
  - `src/main/resources/db/migration/V39__cantidad_actividad_recurso.sql`
  - `src/main/resources/db/migration/V40__etiquetas_actividad.sql`
- **Reparación de compilación y baseline de autenticación (fase 0D)**:
  - `src/main/java/com/feelingpilates/auth/AuthController.java`
  - `src/main/java/com/feelingpilates/auth/AuthService.java`
  - `src/main/java/com/feelingpilates/auth/GoogleLoginDisabledException.java`
  - `src/main/java/com/feelingpilates/exception/GlobalExceptionHandler.java`
  - `src/test/java/com/feelingpilates/auth/AuthControllerTest.java`
- **Clausura de esquema JPA ubicaciones (recurso vs maquina)**:
  - `src/main/java/com/feelingpilates/ubicaciones/controlador/ActividadRecursoController.java`
  - `src/main/java/com/feelingpilates/ubicaciones/controlador/SalonController.java`
  - `src/main/java/com/feelingpilates/ubicaciones/controlador/TipoActividadController.java`
  - `src/main/java/com/feelingpilates/ubicaciones/controlador/TipoMaquinaController.java` (reemplazado por TipoRecursoController)
  - `src/main/java/com/feelingpilates/ubicaciones/controlador/TipoRecursoController.java`
  - `src/main/java/com/feelingpilates/ubicaciones/dto/ActividadRecursoRequest.java`
  - `src/main/java/com/feelingpilates/ubicaciones/dto/ActividadRecursoResponse.java`
  - `src/main/java/com/feelingpilates/ubicaciones/dto/CatalogoItemRequest.java`
  - `src/main/java/com/feelingpilates/ubicaciones/dto/MaquinaItem.java` (reemplazado por RecursoItem)
  - `src/main/java/com/feelingpilates/ubicaciones/dto/MaquinaItemResponse.java` (reemplazado por RecursoItemResponse)
  - `src/main/java/com/feelingpilates/ubicaciones/dto/RecursoItem.java`
  - `src/main/java/com/feelingpilates/ubicaciones/dto/RecursoItemResponse.java`
  - `src/main/java/com/feelingpilates/ubicaciones/dto/SalonDetalleResponse.java`
  - `src/main/java/com/feelingpilates/ubicaciones/dto/SalonRequest.java`
  - `src/main/java/com/feelingpilates/ubicaciones/dto/SalonResponse.java`
  - `src/main/java/com/feelingpilates/ubicaciones/dto/TipoActividadResponse.java`
  - `src/main/java/com/feelingpilates/ubicaciones/dto/TipoMaquinaResponse.java` (reemplazado por TipoRecursoResponse)
  - `src/main/java/com/feelingpilates/ubicaciones/dto/TipoRecursoResponse.java`
  - `src/main/java/com/feelingpilates/ubicaciones/entidad/ActividadRecurso.java`
  - `src/main/java/com/feelingpilates/ubicaciones/entidad/Salon.java`
  - `src/main/java/com/feelingpilates/ubicaciones/entidad/SalonMaquina.java` (reemplazado por SalonRecurso)
  - `src/main/java/com/feelingpilates/ubicaciones/entidad/SalonRecurso.java`
  - `src/main/java/com/feelingpilates/ubicaciones/entidad/TipoActividad.java`
  - `src/main/java/com/feelingpilates/ubicaciones/entidad/TipoMaquina.java` (reemplazado por TipoRecurso)
  - `src/main/java/com/feelingpilates/ubicaciones/entidad/TipoRecurso.java`
  - `src/main/java/com/feelingpilates/ubicaciones/repositorio/ActividadRecursoRepository.java`
  - `src/main/java/com/feelingpilates/ubicaciones/repositorio/SalonMaquinaRepository.java` (reemplazado por SalonRecursoRepository)
  - `src/main/java/com/feelingpilates/ubicaciones/repositorio/SalonRecursoRepository.java`
  - `src/main/java/com/feelingpilates/ubicaciones/repositorio/SalonRepository.java`
  - `src/main/java/com/feelingpilates/ubicaciones/repositorio/TipoActividadRepository.java`
  - `src/main/java/com/feelingpilates/ubicaciones/repositorio/TipoMaquinaRepository.java` (reemplazado por TipoRecursoRepository)
  - `src/main/java/com/feelingpilates/ubicaciones/repositorio/TipoRecursoRepository.java`
  - `src/main/java/com/feelingpilates/ubicaciones/servicio/SalonService.java`
- **Documentación de Autoridad**:
  - `auditoria/fase-0a-baseline-git.md`
  - `auditoria/fase-0b-preservacion-baseline.md`
  - `auditoria/fase-0c-flyway-ambientes.md`
  - `auditoria/fase-0c1-saneamiento-flyway.md`
  - `auditoria/fase-0d-tests-baseline.md`
  - `auditoria/fase-0-allowlist-integracion.md`

### LATER_LANE (Excluidos de Lane 0)
- **Seguridad (Lane 1)**:
  - `src/main/java/com/feelingpilates/seguridad/ContextoAutenticacionService.java` (exclusión explícita)
  - `src/main/java/com/feelingpilates/seguridad/JwtAuthFilter.java` (exclusión explícita de live-DB changes)
- **Pagos / Caja / Ventas**:
  - `src/main/java/com/feelingpilates/pagos/controlador/PagoController.java`
  - `src/main/java/com/feelingpilates/pagos/controlador/PaqueteController.java`
  - `src/main/java/com/feelingpilates/pagos/controlador/PaqueteGestionController.java`
  - `src/main/java/com/feelingpilates/pagos/controlador/VentaController.java`
  - `src/main/java/com/feelingpilates/pagos/dto/*` (todos los DTOs de venta/carrito/gestión)
  - `src/main/java/com/feelingpilates/pagos/entidad/PaqueteActividad.java`
  - `src/main/java/com/feelingpilates/pagos/entidad/Paquete.java` (adiciones a paquete)
  - `src/main/java/com/feelingpilates/pagos/entidad/Compra.java` (adiciones a compra)
  - `src/main/java/com/feelingpilates/pagos/repositorio/CompraRepository.java`
  - `src/main/java/com/feelingpilates/pagos/repositorio/PaqueteRepository.java`
  - `src/main/java/com/feelingpilates/pagos/servicio/PagoService.java`
  - `src/main/java/com/feelingpilates/pagos/servicio/PaqueteGestionService.java`
  - `src/main/java/com/feelingpilates/pagos/servicio/VentaService.java`
- **Usuarios / Roles**:
  - `src/main/java/com/feelingpilates/usuarios/controlador/PermisoController.java`

### UNRELATED_CONTAMINATION (Excluidos)
- `auditoria/00-revalidacion-repositorio-completo.md` (no pertenece al baseline de código)
- `auditoria/04-arquitectura-objetivo.md` (no pertenece al baseline de código)

## 3. Verificación de Regresión de Scope
- Lane 1 (Seguridad): Cero avance (`AutorizadorSalon` ausente, `ContextoAutenticacionService` ausente, `JwtAuthFilter` sin cambios).
- Lane 2 (Programación): Cero avance (`V41`, `BloqueProgramacion`, `Asignacion` ausentes).
- Lane 3 (Políticas/Vigencia): Cero avance (`V42`, `V43` ausentes).
- Lane 4 (Horario Versionado): Cero avance (`V44`–`V46`, controllers/writers versionados ausentes).
- F2E: Cero avance (`V47` ausente).
- Payments & Notifications R1: Cero avance (`V48`, `V49`, snapshots ausentes).
