# Fase 2B.3b.2a — Cierre de Autoridad de Contrato Post-Merge: PUT /api/salones/{id}

Documento de proceso y autoridad de gobernanza post-merge para Lane 4 (F2B.3b.2a).

---

## 1. Identidad y Proveniencia de Integración

- **Candidato de producto Lane 4 auditado**: `1496954a11e9af2d1833056261b4eb42aaa99a33`
- **Pull Request de integración**: PR #6 (`MacacosDevs/AldairCruz7/integracion-backend-lane4-horario-versionado` → `main`)
- **Estado de PR #6**: `MERGED`
- **Commit de integración en `main`**: `827ee063eddeb3b5902e8bd5562becf60e691f37`
- **Ancestro verificado**: SÍ (`git merge-base --is-ancestor 1496954a11e9af2d1833056261b4eb42aaa99a33 origin/main` exitoso)
- **Árbol de producto idéntico**: Tree SHA `735a73b75729d43551525b81b71833f1e5bf30e8` sin drift alguno entre candidato y merge commit.

---

## 2. Contexto de Auditoría y Brecha de Secuenciación

La integración técnica de producto de Lane 4 fue auditada de forma independiente y exhaustiva previo a su incorporación definitiva en la rama principal.

### Resultado de la auditoría técnica previa:
- **P0**: 0
- **P1**: 1 (no atribuible a defecto de código; brecha de confirmación documental de producto)
- **P2**: 2 (hallazgos informativos no bloqueantes)

Todas las áreas técnicas resultaron aprobadas:
- Adaptaciones de baseline: PASS
- Migraciones V44, V45, V46: PASS
- Dominio y lógica de negocio: PASS
- Persistencia y resolver temporal: PASS
- Writers y modelo de concurrencia: PASS
- Reverse-impact: PASS
- Seguridad y autorización granular: PASS
- Contrato API: MATCH
- Contrato de errores: PASS
- Regresión completa: 382 / 382 tests PASS
- Exclusión estricta de F2E: PASS
- Exclusión estricta de Pagos y Notificaciones: PASS
- Dark-launch / sin cutover prematuro: PASS

### Brecha de secuenciación de gobernanza:
El único hallazgo bloqueante P1 requería la confirmación humana explícita de autoridad de producto respecto al endurecimiento del contrato externamente observable en el endpoint `PUT /api/salones/{id}`. Antes de que el artefacto de autoridad correspondiente pudiera materializarse y registrarse formalmente en el repositorio, el PR #6 fue mergeado en `origin/main`.

El presente artefacto subsana **exclusivamente** dicha brecha de secuenciación de gobernanza post-merge, formalizando la autoridad de producto requerida sin alterar el código ni el comportamiento del producto.

---

## 3. Declaración Explícita de Autoridad de Producto Humana

La autoridad de producto humana ha emitido formalmente la siguiente confirmación vinculante:

> "Confirmo como autoridad de producto que `PUT /api/salones/{id}` no debe modificar directamente la programación semanal de horarios. Cualquier cambio de horarios deberá realizarse mediante las operaciones de Horario Versionado. Si el `PUT` intenta modificar `horarios`, deberá rechazarse con `400 HORARIOS_REQUIEREN_VERSIONADO`. El resto del comportamiento autorizado del endpoint debe preservarse."

Esta declaración se asume como autoridad plenaria y suficiente para fijar la especificación contractual del endpoint.

---

## 4. Contrato Definitivo Aceptado de `PUT /api/salones/{id}`

Conforme a la autoridad confirmada y la implementación presente en `main`:

1. **Preservación del comportamiento base de actualización del salón**:
   Las actualizaciones de datos generales del salón (nombre, dirección, teléfono, calle, números, colonia, código postal, referencias, coordenadas, tipos de actividad y recursos) continúan operando normalmente, siempre que `horarios` no sea enviado o coincida con la configuración semanal vigente.
2. **Eliminación de la mutación directa de horarios**:
   El endpoint `PUT /api/salones/{id}` no acepta la modificación directa, inserción ni eliminación destructiva de horarios semanales.
3. **Semántica de rechazo explícita (`400 HORARIOS_REQUIEREN_VERSIONADO`)**:
   Cuando el payload de `PUT /api/salones/{id}` incluya una colección `horarios` que difiera de la configuración semanal efectiva actual (incluyendo alteración de rangos, agregado o supresión de días de la semana, o el envío de una lista vacía para un salón que posee horarios), la solicitud se rechaza de inmediato antes de alterar la entidad:
   - **HTTP Status**: `400 Bad Request`
   - **Código de error**: `HORARIOS_REQUIEREN_VERSIONADO`
   - **Excepción**: `ValidacionException` interceptada por `GlobalExceptionHandler` extrayendo el prefijo canónico mediante `CodigoErrorExtractor`.
4. **Protección histórica e integridad de versiones**:
   Queda erradicado el patrón legacy destructivo (delete/insert no versionado) que impedía la trazabilidad temporal. No se permite ningún bypass que vulnere el historial de versiones.
5. **Rutas autorizadas para operaciones sobre horarios**:
   Cualquier mutación de la programación de horarios de un salón debe realizarse exclusivamente a través de los endpoints de Horario Versionado autorizados:
   - `POST /api/salones/{id}/horarios/versiones`
   - `POST /api/salones/{id}/horarios/cierres`
6. **Consistencia de diseño y persistencia (Fase 2B)**:
   Este contrato salvaguarda los invariantes fundamentales:
   - Invariante de no solapamiento temporal garantizado por la migración Flyway `V45__horario_operacion_exclude_vigencia.sql` (`EXCLUDE USING gist` sobre `daterange`).
   - Soporte de múltiples versiones históricas por día de la semana (`V46__horario_operacion_drop_unique_dia.sql`).
   - Modelo de bloqueo transaccional concurrente (`SalonLock`).
   - Consistencia con las especificaciones aceptadas en `auditoria/fase-2b-diseno-versionado-horario.md`, `auditoria/fase-2b2-consumidores-horario-temporal.md`, `auditoria/fase-2b3b-diseno-writers-concurrencia.md` y `auditoria/fase-2b3b2-diseno-api-frontend-horarios.md`.

---

## 5. Delimitación de Alcance y Exclusiones

- **Frontend F2E**: Este cierre de autoridad no autoriza la implementación, avance ni activación de la Fase 2E (excepciones de calendario / fechas puntuales).
- **Pagos y Notificaciones**: No se autoriza ninguna interacción ni desarrollo en los módulos de pagos, caja o notificaciones pendientes.
- **Límite de hallazgos P2**: Los ítems P2 señalados en la auditoría técnica previa (representación de tipos numéricos cortos/smallint, semántica de Lane 3, `ImpactoTurnosRecurrentesEnHorario`, o clasificaciones descriptivas de tests) continúan como observaciones informativas de archivo histórico y quedan expresamente fuera de este documento de autoridad.
- **Cero cambios de producto**: Este documento no introduce ninguna modificación a clases de producción, configuraciones de infraestructura, scripts de base de datos ni pruebas automatizadas.
