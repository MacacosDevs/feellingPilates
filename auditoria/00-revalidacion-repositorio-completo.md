# FeelingPilates — Revalidación del repositorio completo

> Fecha de inspección: 2026-08-20. Fuente autoritativa: archivos presentes en el árbol de trabajo de `feelingpilates/`, incluidos cambios no confirmados en Git.
> Las rutas `auditoria/01-estado-actual.md`, `auditoria/02-ventas-pagos-stripe.md` y `auditoria/03-notificaciones-outbox.md` no existen en este equipo, en `HEAD`, en las ramas locales/remotas ni en los adjuntos disponibles. Por ello no se inventan textos ni cifras previas: el contraste exacto se marca `NO CONFIRMADO`; los temas expresamente enumerados en el encargo se usan como hipótesis.

## 1. Repositorio y toolchain

`CONFIRMADO POR CÓDIGO ACTUAL`

| Dato | Estado actual | Comparación con 01 |
|---|---|---|
| Raíz Git | `feelingpilates/`; la carpeta superior no es repositorio | `NO CONFIRMADO ANTERIORMENTE` |
| Branch | `main`, alineada con `origin/main` | `NO CONFIRMADO ANTERIORMENTE` |
| Commit | `f18df68e31f53d0fa5b1974f7e878832b4409cd3` | `NO CONFIRMADO ANTERIORMENTE` |
| Estado Git | Sucio: 35 rutas tracked modificadas/eliminadas y 53 archivos no rastreados previos a este checkpoint | `CAMBIÓ` respecto del commit; 01 no está disponible |
| Java productivo | 150 archivos | `CAMBIÓ`: `HEAD` contiene 127; cifra de 01 no confirmable |
| Tests | 3 archivos Java; 2 clases con 10 métodos `@Test` | `SIN CAMBIOS` frente a `HEAD`; 01 no confirmable |
| Packages principales | `auth`, `calendario`, `comun`, `config`, `exception`, `notificaciones`, `pagos`, `seguridad`, `ubicaciones`, `usuarios` | `CAMBIÓ` en contenido; nombres de 01 no confirmables |
| Flyway | 43 migraciones, versiones 1–22, 22.1–22.3 y 23–40 | `CAMBIÓ`: `HEAD` contiene 25 y versiones duplicadas 14/15/16 |
| Configuración | `pom.xml`, `application*.properties`, `.env.example`, `.env` ignorado, wrapper Maven, Dockerfile/Compose | `NO CONFIRMADO ANTERIORMENTE` |

El estado del disco no equivale al commit: ventas, recursos y V22.1–V40 dependen de archivos no rastreados. Un clon de `f18df68` no reproduce el sistema inspeccionado y conserva conflictos Flyway de versión que el árbol actual está corrigiendo mediante borrados/renumeración aún no confirmados.

Archivos: `.git/`, `pom.xml`, `src/main/resources/application.properties`, `src/main/resources/db/migration/`.

## 2. Inventario actual

| Tipo | Cantidad actual | Criterio |
|---|---:|---|
| Java productivo | 150 | `src/main/java/**/*.java` |
| Entidades JPA | 21 | archivos con `@Entity` |
| Controllers REST | 18 | archivos con `@RestController` |
| Services | 15 | archivos con `@Service` |
| Repositories | 19 | interfaces bajo `repositorio` |
| DTOs | 61 | archivos bajo `dto` |
| Records | 69 | declaraciones en 66 archivos, incluidas anidadas y fuera de `dto` |
| Tests Java | 3 | dos clases de test más `TestcontainersConfiguration` |
| Métodos de test | 10 | 1 de contexto y 9 de auth |
| Migraciones Flyway | 43 | SQL presente en disco |
| Jobs `@Scheduled` | 1 | reconciliación de compras Stripe |
| `@Async` / `@EnableAsync` | 0 | no existe |
| Spring Events | 0 | sin publisher/listener/event classes |
| Workers explícitos | 0 | existe un trabajo periódico dentro de `PagoService`, no un worker separado |

Distribución Java: auth 9, calendario 20, común 1, config 1, excepciones 5, notificaciones 2, pagos 30, seguridad 5, ubicaciones 42 y usuarios 34.

Integraciones: Stripe está activa en backend; Google ID Token tiene verificador pero flujo HTTP deshabilitado; email es solo consola; Google Maps se usa como enlace/geocodificación desde clientes, no como servicio de backend. No hay FCM, SMS ni WhatsApp.

Evidencia: `PagoService#crearIntentoPago/procesarWebhook/reconciliarComprasPendientes`; `GoogleTokenVerifier#verificar`; `EmailServiceConsola#enviarInvitacionCliente`.

## 3. Módulos actuales

Paquete/módulo: `auth`  
Responsabilidad real: registro, login local, invitaciones y emisión JWT; Google permanece comentado.  
Entidades: usa `Usuario` e `InvitacionUsuario`; no posee entidades.  
Services: `AuthService`, `GoogleTokenVerifier` (componente).  
Controllers: `AuthController`.  
Dependencias principales: `usuarios`, `seguridad`, Google API Client.

Paquete/módulo: `seguridad`  
Responsabilidad real: firma/validación JWT, filtro stateless, CORS, permisos vigentes desde BD y principal autenticado.  
Entidades: ninguna. Services: `JwtService`, `ContextoAutenticacionService`.  
Controllers: ninguno.  
Dependencias principales: `usuarios`, Spring Security, JJWT.

Paquete/módulo: `usuarios`  
Responsabilidad real: perfiles, fotos, RBAC, sedes por rol, alta de cliente/personal, especialidades del instructor e invitación.  
Entidades: `Usuario`, `UsuarioRol`, `Rol`, `Permiso`, `InvitacionUsuario`, `PerfilInstructor`.  
Services: `UsuarioService`, `AltaUsuarioService`, `RolService`, `PermisoResolver`, `SedeRolValidador`.  
Controllers: `UsuarioController`, `AdminUsuarioController`, `RolController`, `PermisoController`.  
Dependencias principales: `ubicaciones`, `notificaciones`, seguridad, Thumbnailator.

Paquete/módulo: `ubicaciones`  
Responsabilidad real: estados/municipios, salones, horario semanal, horario excepcional, oferta de actividades e inventario.  
Entidades: `Estado`, `Municipio`, `Salon`, `HorarioOperacion`, `SalonHorarioExcepcion`, `TipoActividad`, `TipoRecurso`, `SalonRecurso`, `ActividadRecurso`.  
Services: `SalonService`, `SalonHorarioExcepcionService`; parte del catálogo opera directamente en controllers.  
Controllers: seis de ubicación/salón/actividad/recurso.  
Dependencias principales: `usuarios`; calendario consume horario, salón, actividad y excepciones.

Paquete/módulo funcional: `actividades` dentro de `ubicaciones`  
Responsabilidad real: catálogo, duración, participantes informativos, etiquetas, recursos requeridos y especialidades.  
Entidades: `TipoActividad`, `ActividadRecurso`, relación `Usuario.especialidades`.  
Services: lógica repartida entre controllers y `EspecialidadInstructorService`.  
Controllers: `TipoActividadController`, `ActividadRecursoController`, `EspecialidadInstructorController`.  
Dependencias principales: `calendario`, `pagos`, recursos y usuarios.

Paquete/módulo: `calendario`  
Responsabilidad real: bloques recurrentes/puntuales/cancelaciones, asignaciones instructor-actividad y reservas.  
Entidades: `TurnoInstructor`, `TurnoInstructorAsignacion`, `Reserva`.  
Services: `TurnoInstructorService`, `ReservaService`, `EspecialidadInstructorService`.  
Controllers: `TurnoInstructorController`, `ReservaController`, `EspecialidadInstructorController`.  
Dependencias principales: usuarios, salones, horarios, actividades; no pagos ni recursos al reservar.

Paquete/módulo funcional: `reservas`, aún dentro de `calendario`  
Responsabilidad real: crear/listar/cancelar una reserva con fecha y horas copiadas.  
Entidades: `Reserva`. Services: `ReservaService`. Controllers: `ReservaController`.  
Dependencias principales: turnos, usuario, salón y actividad.  
No posee capacidad, beneficio consumido, sesión compartida ni confirmación.

Paquete/módulo: `pagos` / API `ventas`  
Responsabilidad real: catálogo de paquetes, compra Stripe, venta presencial, carrito/ticket, historial y reembolso local.  
Entidades: `Paquete`, `PaqueteActividad`, `Compra`; no existen `Venta`, `LineaVenta` ni `Pago`.  
Services: `PagoService`, `VentaService`, `PaqueteGestionService`.  
Controllers: `PagoController`, `VentaController`, `PaqueteController`, `PaqueteGestionController`.  
Dependencias principales: Stripe, usuarios, salones y actividades.

Paquete/módulo: `notificaciones`  
Responsabilidad real: interfaz de invitación por email y simulación en log.  
Entidades: ninguna. Services: `EmailServiceConsola`. Controllers: ninguno.  
Dependencias principales: llamada síncrona desde `AltaUsuarioService`; no proveedor ni persistencia.

Paquetes transversales: `comun` aporta `EntidadBase`; `exception` traduce errores HTTP; `config` configura OpenAPI. La organización es package-by-feature en primer nivel y layered dentro de cada feature, con lógica de catálogo también en controllers.

## 4. Revalidación Auditoría 01

Debido a la ausencia física de 01, “hallazgo previo” identifica las hipótesis exigidas por este encargo, no una cita del checkpoint.

| Hallazgo previo/hipótesis | Estado actual | Evidencia |
|---|---|---|
| Package-by-feature/layered | `CONFIRMADO` | features de primer nivel; subpackages `controlador/dto/entidad/repositorio/servicio` |
| Separación `auth` vs `seguridad` | `CONFIRMADO` | `AuthService/AuthController` frente a `SecurityConfig/JwtAuthFilter/JwtService` |
| Ubicaciones limitadas | `CAMBIÓ` | ahora incluye horario excepcional, actividades y recursos genéricos |
| Calendario limitado | `CAMBIÓ` | 20 archivos, múltiples instructores, asignaciones por rango y excepciones |
| Seguridad basada en RBAC | `CONFIRMADO` | `@PreAuthorize`, JWT stateless y permisos recalculados por request |
| IDOR de reservas | `CONFIRMADO` | `ReservaController#listarMias(clienteId)` acepta cualquier UUID autenticado |
| Alcance de sede incompleto | `CONFIRMADO` | permisos se unen globalmente; calendario y reembolso no validan sede del principal |
| JWT confía permisos del token | `YA NO APLICA` | `JwtAuthFilter` valida firma pero recalcula roles/permisos desde BD |
| JWT/sede plenamente resuelto | `INCOMPLETO` | JWT/principal no transportan sede y las authorities no están acotadas por sede |
| Logs sensibles | `CONFIRMADO` | `FeelingpilatesApplication#main/debugEnv` registra longitud y extremos de password; email registra enlace-token completo |
| Google Auth disponible | `YA NO APLICA` | verificador existe, endpoint y service están comentados; test esperado ya falla |
| Flyway conflictivo | `CAMBIÓ` | disco valida 43 sin duplicados; `HEAD` sí conserva duplicados 14/15/16 |
| Build Java 21 no comprobado | `YA NO APLICA` | Java 21.0.11 y compilación limpia exitosa |
| Testing escaso | `CONFIRMADO` | solo contexto/auth; 1 de 10 tests falla |
| Concurrencia desprotegida | `CONFIRMADO` | sin `@Version`, locks, isolation especial ni exclusion constraints |

Etiquetas globales: estado técnico `CONFIRMADO POR CÓDIGO ACTUAL`; redacción/cifras exactas de 01 `NO CONFIRMADO`.

## 5. Revalidación Ventas/Pagos/Stripe

| Propuesta/hallazgo previo | Clasificación | Estado actual y evidencia |
|---|---|---|
| Entidad `Venta` | `DEBE REEVALUARSE` | no existe; `VentaService` usa `Compra` para caja |
| `LineaVenta` | `DEBE REEVALUARSE` | carrito crea una `Compra` por unidad, agrupada por `grupoCompraId/numeroItem` |
| Entidad `Pago` separada | `SIGUE APLICANDO` | `Compra` mezcla intento, pago, venta y supuesto beneficio adquirido |
| Efectivo/transferencia | `EL CÓDIGO ACTUAL YA LO RESUELVE` | `Compra.MetodoPago`; `VentaService#registrarVentaCarrito` transaccional |
| Clases individuales/saldo/créditos | `SIGUE APLICANDO` | paquete declara cantidades, pero no hay ledger, saldo ni consumo por reserva |
| Beneficio adquirido persistente | `SIGUE APLICANDO` | compra referencia paquete mutable; no snapshot completo ni derechos por actividad |
| PaymentIntent | `EL CÓDIGO ACTUAL YA LO RESUELVE` | creación real con Stripe SDK y `clientSecret` |
| Stripe webhook firmado | `EL CÓDIGO ACTUAL YA LO RESUELVE` | firma verificada; procesa éxito, fallo y `charge.refunded` |
| Stripe Event ID / Inbox | `SIGUE APLICANDO` | event id no se guarda; no hay inbox ni tabla de eventos |
| Reembolsos | `DEBE REEVALUARSE` | caja cambia estado local; Stripe solo observa reembolso externo, no lo inicia |
| Idempotencia | `DEBE REEVALUARSE` | key UNIQUE + Stripe key; check-then-insert, no vínculo usuario/paquete y sin idempotencia webhook |
| Reconciliación | `EL CÓDIGO ACTUAL YA LO RESUELVE` | scheduler cada 5 minutos consulta PaymentIntent pendiente |

Hallazgos adicionales: `PagoService#aCompraResponse` hace `categoria.name()` aunque los paquetes nuevos dejan categoría `null`; puede fallar al listar compras. V35 afirma que Stripe “aún no está integrado”, pero el código actual sí lo está. El permiso específico de reembolso Stripe fue eliminado; `venta.gestion.gestionar` permite reembolsar cualquier venta de caja sin validación de sede.

Archivos: `pagos/entidad/Compra.java`; `pagos/servicio/PagoService.java`; `pagos/servicio/VentaService.java`; V22.1–V35.

## 6. Revalidación Notificaciones/Outbox

| Capacidad previa | Clasificación | Evidencia actual |
|---|---|---|
| Proveedor email real | `SIGUE APLICANDO` | solo `EmailServiceConsola` |
| Firebase/FCM/push | `SIGUE APLICANDO` | inexistente |
| SMS/WhatsApp | `SIGUE APLICANDO` | inexistente |
| Outbox | `SIGUE APLICANDO` | sin entidad, tabla o publisher |
| `Notification` / `NotificationAttempt` | `SIGUE APLICANDO` | inexistentes |
| Workers/retry de notificación | `SIGUE APLICANDO` | no hay; el único scheduler es Stripe |
| Spring Events / `@Async` | `SIGUE APLICANDO` | cero usos |
| Preferencias | `SIGUE APLICANDO` | no existe modelo de preferencias |
| Recordatorios | `SIGUE APLICANDO` | inexistentes |
| Mensajes de reserva/pago/promoción | `SIGUE APLICANDO` | no existen flujos de mensaje |
| Invitación de usuario | `DEBE REEVALUARSE` | llamada síncrona dentro de transacción y enlace con token en log |

`CONFIRMADO POR CÓDIGO ACTUAL`: notificaciones sigue siendo un stub sin entrega durable, intentos, reintento ni trazabilidad. `AltaUsuarioService#crearClienteSalon` guarda usuario/invitación y llama directamente al servicio de consola.

## 7. Programación actual

`REQUISITO DE NEGOCIO`: cada salón programa bloques propios, una asignación equivale a instructor+rango+una actividad, puede haber instructores simultáneos, y el instructor no puede traslaparse globalmente.

El código aproxima “programación” mediante `calendario`, pero mezcla plantilla, bloque, excepción, cancelación y reserva. No existe package `programacion`, identidad de ocurrencia, confirmación ni ventana de reservas.

| Entidad actual | Clasificación | Motivo |
|---|---|---|
| `TurnoInstructor` | `RESPONSABILIDAD MEZCLADA` | bloque + recurrencia + excepción/cancelación + conjunto de instructores |
| `TurnoInstructorAsignacion` | `EVOLUCIONABLE` | fila une instructor+rango+actividad, pero PK impide repetir actividad en rangos distintos |
| `Reserva` | `RESPONSABILIDAD MEZCLADA` | contiene booking y copia de una ocurrencia sin identidad compartida |
| `HorarioOperacion` | `EVOLUCIONABLE` | horario semanal por salón, sin vigencia/historia |
| `SalonHorarioExcepcion` | `CONSERVABLE` | expresa cerrado u horario especial por fecha |
| `Salon` | `EVOLUCIONABLE` | contiene oferta/inventario, no políticas de confirmación/reserva |
| `TipoActividad` | `CONSERVABLE` | ya posee duración, participantes y etiquetas |
| `TipoRecurso` / `SalonRecurso` / `ActividadRecurso` | `CONSERVABLE` | catálogo, inventario y consumo por reserva están representados |
| `Usuario.especialidades` | `EVOLUCIONABLE` | valida capacitación, no disponibilidad |
| `PerfilInstructor` | `RESPONSABILIDAD MEZCLADA` | perfil y `infoHorarios` JSONB sin uso; duplica conceptualmente programación |

Archivos: `calendario/entidad/*.java`, `ubicaciones/entidad/*.java`, `usuarios/entidad/PerfilInstructor.java`.

## 8. Horarios y bloques

- Cada `HorarioOperacion` pertenece a un salón/día; BD impone un solo intervalo por salón/día y `hora_cierre > hora_apertura`.
- Cada `TurnoInstructor` pertenece a un salón y es recurrente por día o puntual por fecha. Pueden existir N bloques no traslapados.
- `TurnoInstructorService#validarDentroDeHorarioSalon` usa horario semanal o especial de fecha.
- `validarSinTraslape` implementa semántica `[inicio, fin)`: dos bloques que se tocan son válidos.
- La regla de no traslape es check-then-insert solo en Java; dos transacciones concurrentes pueden superarla.
- Para una `EXCEPCION`, compara contra los recurrentes y por ello impide crear un bloque especial que se traslape con el bloque normal que conceptualmente debería sustituir.
- `CANCELACION` requiere instructores y horas en el DTO/tabla, pero omite validación de horario/traslape y actúa como marcador de día completo por instructor.
- Los recurrentes carecen de `vigenteDesde/vigenteHasta`; editar muta la misma fila y pierde historia.
- `SalonService#actualizar` reemplaza todos los horarios sin comprobar turnos/reservas ya existentes.

Evidencia: `HorarioOperacion`; V11 `UNIQUE(salon_id,dia_semana)`; `TurnoInstructorService#crear/actualizarTurno/validarSinTraslape`.

## 9. Instructores y asignaciones

`CONFIRMADO POR CÓDIGO ACTUAL`: un bloque admite varios instructores y ambos pueden impartir la misma actividad. La exclusividad se aplica al bloque físico, no a la actividad.

Gaps:

- `AsignacionInstructorRequest` acepta una lista de actividades para un único rango; contradice el requisito de una actividad por asignación.
- La tabla sí guarda una fila por actividad, pero su PK `(turno,instructor,actividad)` impide dos rangos disjuntos de la misma actividad dentro del bloque.
- `ReservaService#crear` solo comprueba que el instructor tenga la especialidad. No exige que la actividad esté asignada en ese turno ni respeta `horaInicio/horaFin` de la asignación.
- No se busca traslape del instructor contra turnos de otros salones. Un instructor puede quedar programado simultáneamente en sedes distintas.
- La reserva sí busca traslape global por instructor/fecha, pero eso ocurre tarde y bloquea también clientes adicionales de una misma sesión grupal.
- No existen estado/plazo de confirmación ni política `requiereConfirmacionInstructor` por salón.

Evidencia: `TurnoInstructorAsignacion`; `TurnoInstructorService#resolverAsignaciones`; `ReservaService#crear`; `ReservaRepository#existeTraslape`.

## 10. Actividades

`TipoActividad` tiene `duracionMinutos` (default 60, positivo), `participantesPorReserva` y etiquetas. `ReservaService` calcula fin como inicio + duración; por tanto soporta duraciones futuras de 30 minutos a nivel de cálculo individual.

No existe generador/listado de slots: el cliente propone `horaInicio`. Tampoco se valida alineación con una partición de slots, que la actividad esté ofrecida por el salón, que esté asignada al instructor en ese rango o que continúe activa.

Especialidades (`instructor_actividad`) y oferta del salón (`salon_tipo_actividad`) existen, pero la creación de reserva solo consulta especialidad.

Archivos: `TipoActividad.java`; `TipoActividadController`; `EspecialidadInstructorService`; `ReservaService#crear`.

## 11. Excepciones por fecha

| Necesidad confirmada | Soporte actual | Gap |
|---|---|---|
| Cerrar salón | Sí | `SalonHorarioExcepcion.cerrado`; reservas no lo revalidan al crear |
| Ampliar/reducir horario del día | Sí | se usa al crear excepción de turno, no al reservar sobre recurrente ya existente |
| Crear bloque especial | Parcial | `EXCEPCION`, pero el validador la enfrenta al recurrente |
| Cancelar bloque normal | Parcial | `CANCELACION` cancela al instructor todo el día, no referencia bloque |
| Modificar bloque temporalmente | Parcial | no hay edición de puntual ni vínculo de reemplazo |
| Agregar/quitar/reemplazar instructor | Parcial | se puede crear una excepción, con semántica de sustitución global por instructor |
| Modificar rango/cambiar actividad | Parcial | excepción nueva; asignación/rango luego son ignorados por reserva |
| Cancelar actividad de un instructor | No | cancelación no distingue actividad ni intervalo |

`ReservaService#turnosVigentes` resuelve `CANCELACION > EXCEPCION > RECURRENTE` por instructor+salón+fecha. La presencia de cualquier excepción sustituye todos sus recurrentes de ese día; no compone ajustes finos ni una programación efectiva completa.

## 12. Reservas actuales

La API exige `salonId`, `instructorId`, `clienteId`, `tipoActividadId`, fecha e inicio. Calcula el fin, localiza turnos vigentes, impide cualquier reserva traslapada del instructor y guarda una fila `Reserva` confirmada.

Gaps confirmados:

- No existe endpoint de disponibilidad por actividad→fecha→salones→horarios→instructor.
- No valida horario especial/cierre del salón al reservar, rango/actividad de asignación ni actividad ofrecida por sede.
- No hay ventana máxima/mínima, pasado/futuro, confirmación, cupo, inventario, saldo/beneficio ni notificaciones.
- No evita reserva simultánea del mismo cliente con otro instructor.
- El check de instructor convierte una clase grupal en reserva individual: el segundo cliente es rechazado.
- `GET /mias?clienteId=` es IDOR; el principal no se usa.
- Cancelar solo cambia estado y requiere permiso administrativo; no hay reglas, motivo, ownership ni devolución de beneficio.
- Cambiar turnos, horario o recursos no revisa reservas existentes.

Evidencia: `ReservaController`; `ReservaService#crear/cancelar/turnosVigentes`; `ReservaRepository`.

Separar conceptualmente programación y reservas está sustentado por el código actual: hoy `calendario` decide la plantilla y también persiste clientes, mientras no existe agregado compartido de sesión/capacidad/consumo. Es una conclusión de límites de responsabilidad del monolito, no una decisión de despliegue.

## 13. Sesiones/ocurrencias

No existen `Sesion`, `SesionReservable` u `Ocurrencia`. Una reserva copia fecha, salón, instructor, actividad e intervalo, por lo que dos clientes no pueden referirse a la misma clase ni compartir estado/capacidad.

Una proyección dinámica puede calcular horarios teóricos mientras no haya estado por fecha. Sin embargo, confirmación del instructor, cancelación/modificación puntual, reservas múltiples, consumo compartido, historial y notificaciones necesitan una identidad estable equivalente de ocurrencia. El código actual no la proporciona; `Reserva.id` identifica al cliente reservado, no a la sesión.

`REQUISITO DE NEGOCIO`: una sesión no confirmada no es reservable. Estado actual: no existe ningún estado entre plantilla y reserva; todo turno vigente es aceptado inmediatamente.

## 14. Equipamiento y capacidad

`CAMBIÓ RESPECTO A AUDITORÍA PREVIA`: máquinas fueron generalizadas a recursos.

- `TipoRecurso`: catálogo global.
- `SalonRecurso`: inventario por salón.
- `ActividadRecurso.cantidad`: unidades totales consumidas por una reserva de actividad; permite representar Reformer=1 y Duo=2.
- `TipoActividad.participantesPorReserva`: informativo desde V39; no multiplica el consumo.
- `Salon.tiposActividad`: oferta por sede.

Ninguna de estas relaciones es consultada por `ReservaService`. No hay suma de reservas simultáneas, capacidad restante, asignación compartida entre instructores, lock del último recurso ni respuesta de cupo. Dos sesiones simultáneas no comparten un contador porque no existe sesión ni cálculo de ocupación.

V36 renombra máquinas a recursos y borra datos de `reserva`, actividades, especialidades, paquetes relacionados e inventario; es una migración destructiva de datos, aunque Flyway la ejecuta correctamente en esquema vacío.

Evidencia: `ActividadRecurso`, `SalonRecurso`, `TipoRecurso`; V36/V38/V39; ausencia de repositorios de recurso en `ReservaService`.

## 15. Concurrencia

| Invariante | Protección actual | Riesgo |
|---|---|---|
| Bloques del salón no traslapados | consulta y comparación Java | check-then-insert; carrera |
| Instructor global sin traslape | ninguna al programar | doble sede/horario permitido |
| Instructor sin dos reservas | consulta `COUNT > 0` | carrera; sin constraint de rango |
| Mismo cliente sin doble reserva | ninguna | reservas simultáneas posibles |
| Última plaza | no implementada | sin protección |
| Último recurso/Reformer | no implementada | sin protección |
| Duo Reformer | dato de consumo existe | no se consume ni bloquea |
| Cambio con reservas existentes | ninguna | historia/capacidad pueden quedar incoherentes |
| Idempotencia de pago | UNIQUE + Stripe | carrera local y posible reutilización cruzada de key |
| Webhook repetido/desordenado | checks parciales de estado | sin Event ID/Inbox/versionado |

No hay `@Version`, `@Lock`, `SELECT FOR UPDATE`, exclusion constraint ni aislamiento configurado. Los tests confirmaron `READ_COMMITTED` por defecto. Las UNIQUE relevantes cubren email, catálogo, horario por día, excepciones activas e idempotency key, no intervalos ni cupo.

## 16. API móvil actual vs requerida

| Flujo requerido | API actual |
|---|---|
| Elegir actividad | catálogo administrativo protegido; no búsqueda pública de disponibilidad |
| Elegir fecha | no endpoint de programación efectiva |
| Ver salones | `GET /api/salones` lista sedes, sin filtrar disponibilidad/actividad |
| Ver horarios e instructor | endpoint administrativo devuelve turnos/reglas crudas |
| Ver capacidad | inexistente |
| Reservar sesión concreta | POST recibe seis decisiones/IDs y no `sessionId` |

La app móvil solo integra auth/perfil. `src/data/clases.ts` declara explícitamente que no hay backend de horario/reservas y genera clases mock con capacidad fija 8; `ClassesScreen` filtra esos datos localmente. La web administrativa sí consume turnos, pero sus cuatro pantallas de reservas muestran “Próximamente”.

Conclusión: la API está lejos del flujo requerido y obligaría al cliente a interpretar o inventar disponibilidad. No existe un contrato cliente orientado a actividad+fecha ni respuesta de programación efectiva.

## 17. Build

`CONFIRMADO POR CÓDIGO ACTUAL`

| Comprobación | Resultado | Clasificación |
|---|---|---|
| `java -version` | Microsoft OpenJDK 21.0.11 LTS, arm64 | toolchain correcto |
| `mvn -version` | comando global no instalado | no bloquea: existe wrapper |
| `./mvnw -version` | Maven 3.9.16 sobre Java 21 | toolchain correcto |
| Compilación limpia | 150 fuentes, `BUILD SUCCESS` | sin error de código |
| Flyway en test | 43 validadas/aplicadas, PostgreSQL 16.14, esquema v40 | éxito |
| Tests | 10 ejecutados; 9 pasan, 1 falla | `ERROR REAL DEL CÓDIGO/TEST` |

El build se ejecutó sobre una copia en `/private/tmp` sin `.git`, `.env` ni `target`, usando dependencias locales offline. El primer intento de tests dentro del sandbox no pudo abrir Docker (`Operation not permitted`): fue `ERROR DE TOOLCHAIN/ENTORNO`. Repetido con acceso a Docker, Testcontainers arrancó y reveló el resultado real.

Fallo real: `AuthControllerTest#googleStubDevuelve501`, línea 135, esperaba HTTP 501 y obtuvo 500. `AuthController` tiene `/google` comentado y `GlobalExceptionHandler#handleGeneric` convierte la ruta no resuelta en 500.

## 18. Flyway

Versiones presentes: `1..22`, `22.1`, `22.2`, `22.3`, `23..40`. Total 43; no hay duplicados en el árbol actual. Flyway las validó y aplicó en ese orden sobre una BD vacía.

`CAMBIÓ RESPECTO A AUDITORÍA PREVIA`: `HEAD` contiene 25 archivos y colisiones `V14`, `V15`, `V16`; el disco elimina las tres migraciones de pagos con esas versiones y las reintroduce como V22.1–V22.3, todavía sin rastrear. También añade V23–V40.

Riesgos observados, sin corregir:

- el estado funcional depende de 21 migraciones no rastreadas; el commit declarado no es reproducible;
- V22.1/V22.2/V22.3 conservan comentarios internos “V14/V15/V16”;
- V35 dice que Stripe no está integrado, contradicho por `PagoService`;
- V36 borra reservas y catálogos/datos relacionados, no solo renombra recursos;
- V22 agrega rangos de asignación sin CHECK de par nulo, orden o pertenencia al bloque;
- no existen constraints de exclusión para bloques/reservas.

Evidencia: salida real “Successfully validated 43 migrations” y “Successfully applied 43 ... v40”; directorio `src/main/resources/db/migration`.

## 19. Seguridad

| Hallazgo | Estado | Evidencia |
|---|---|---|
| JWT firma/expiración | sólido básico | `JwtService#validarYObtenerClaims` |
| Permisos revocados en vivo | resuelto | `JwtAuthFilter` recalcula desde BD y exige usuario activo |
| Issuer/audience JWT | ausente | solo subject, claims, iat, exp y firma |
| Alcance de sede | incompleto | authorities globales; calendario no valida `UsuarioRol.salon` |
| IDOR “mis reservas” | confirmado | clienteId llega por query y no se compara con principal |
| IDOR excepción salón | confirmado | DELETE recibe salonId pero service elimina solo por id |
| Reembolso de caja | demasiado amplio | permiso gestionar, sin scope de sede/propiedad |
| Idempotency key Stripe | exposición cruzada posible | reutiliza por key sin comprobar usuario/paquete |
| Secretos versionados | no se confirmó secreto real | `.env` ignorado; properties usan variables, con defaults de desarrollo |
| Logs sensibles | confirmado | extremos/longitud de DB password y URL de invitación con token |
| Google Auth | deshabilitado | verificador seguro por audiencia, pero flujo HTTP comentado |
| Webhook Stripe | firma presente | endpoint público valida `Stripe-Signature`; no Inbox/Event ID |

También existe registro público de clientes sin rate limiting observable y los claims roles/permisos se incluyen en JWT aunque el servidor los ignora. La compra Stripe usa el principal para usuario, mientras venta de caja sí valida sede al registrar; esas son protecciones actuales que deben conservarse como hechos.

Archivos: `SecurityConfig`, `JwtAuthFilter`, `ContextoAutenticacionService`, `ReservaController`, `SalonHorarioExcepcionController`, `VentaService`, `PagoService`, `FeelingpilatesApplication`.

## 20. Testing

Cobertura real:

- `FeelingpilatesApplicationTests#contextLoads`: arranque, JPA y todas las migraciones.
- `AuthControllerTest`: registro, duplicado, login, contraseña incorrecta, `/me`, edición propia, acceso admin denegado y stub Google.
- 9 de los 10 métodos pasan; Google falla 501 vs 500.

Cobertura inexistente: programación, horario semanal, excepciones, instructores/asignaciones, reservas, concurrencia, recursos/capacidad/Duo, pagos, Stripe/webhook/idempotencia/reconciliación/reembolso, ventas/carrito, notificaciones y seguridad por sede/IDOR.

No hay tests unitarios puros ni pruebas simultáneas. Testcontainers hace que todo el conjunto dependa de Docker. El profile dev activa SQL DEBUG, produciendo salida extensa y ejecutando el scheduler de pagos al iniciar contexto.

## 21. Current → Functional Gap de Programación

| Concepto actual | Responsabilidad actual | Concepto funcional futuro relacionado | Gap |
|---|---|---|---|
| `Salon` | sede, oferta, inventario | políticas por salón | sin confirmación ni ventanas de reserva |
| `HorarioOperacion` | intervalo semanal | horario operativo vigente | sin vigencia/historia; uno por día |
| `SalonHorarioExcepcion` | cerrado/horario especial | ajuste de fecha | existe, pero reserva no lo revalida |
| `TurnoInstructor` | bloque+tipo+instructores | bloque recurrente/ajuste | mezcla conceptos; sin vigencia |
| `TurnoInstructorAsignacion` | instructor+actividad+rango | asignación atómica | DTO agrupa actividades; PK limita segmentos |
| `Usuario.especialidades` | capacitación | elegibilidad instructor | no equivale a disponibilidad |
| `PerfilInstructor.infoHorarios` | JSONB sin uso | programación | responsabilidad duplicada/no integrada |
| `ReservaService#turnosVigentes` | precedencia simple | programación efectiva | no compone ajustes ni horarios efectivos completos |
| `TipoActividad` | duración/participantes/tags | definición de slots | duración existe; slots no se exponen |
| `Reserva` | cliente + ocurrencia copiada | booking de sesión | sin `sessionId`, cupo, consumo o beneficio |
| inexistente | — | confirmación instructor | gap total |
| inexistente | — | sesión/ocurrencia concreta | gap crítico para estado compartido |
| `SalonRecurso` | inventario | capacidad compartida | dato no consumido |
| `ActividadRecurso` | unidades por reserva | consumo de equipamiento | no se suma ni bloquea |
| endpoints de turno | CRUD de reglas | consulta móvil de disponibilidad | contrato incorrecto para el flujo requerido |

Resultado funcional: horario semanal y excepción del salón están parcialmente soportados; programación recurrente, ajustes de instructor y reservas existen de forma básica; programación efectiva, vigencias, confirmación, sesión, cupo compartido, ventanas y consumo de beneficios no están resueltos.

## 22. Delta contra 01/02/03

### A. Auditorías previas que siguen vigentes

| Hallazgo | Checkpoint | Evidencia actual |
|---|---|---|
| Arquitectura híbrida por feature/capas | 01 | árbol de packages actual |
| IDOR y scope de sede incompleto | 01 | `ReservaController`, authorities globales |
| Logs sensibles | 01 | aplicación e email consola |
| Testing y concurrencia insuficientes | 01 | 10 tests; sin locks/constraints de rango |
| Falta separar venta/pago/beneficio | 02 | entidad `Compra` concentra responsabilidades |
| Falta saldo/consumo e Inbox Stripe | 02 | no hay ledger ni Event ID |
| Falta proveedor/outbox/retry/preferencias | 03 | solo dos archivos de notificación |

### B. Hallazgos que cambiaron por tener el repositorio completo

| Hallazgo previo | Estado actual | Impacto |
|---|---|---|
| Java 21 no disponible | Java 21.0.11; compila | elimina bloqueo de toolchain |
| Stripe ausente/incompleto | PaymentIntent, webhook y reconciliación reales | 02 debe partir de implementación parcial existente |
| Sin venta presencial | efectivo/transferencia, carrito y reembolso local | dominio creció, aún sobre `Compra` |
| Calendario simple | múltiples instructores, rangos y puntuales | más base reutilizable y nuevos gaps semánticos |
| Máquinas específicas | recursos genéricos + actividad→consumo | capacidad tiene datos, no lógica transaccional |
| Flyway duplicado | disco actual válido con 43; commit aún inválido | distinguir árbol de trabajo de `HEAD` |

### C. Código nuevo/no analizado anteriormente

| Área | Archivos principales | Impacto arquitectónico |
|---|---|---|
| Ventas | `VentaController/Service`, DTOs carrito | ticket multi-línea transaccional sin entidad Venta |
| Servicios/paquetes | `PaqueteGestion*`, `PaqueteActividad` | paquete abierto por actividad |
| Recursos | `TipoRecurso`, `SalonRecurso`, `ActividadRecurso` | base para capacidad compartida |
| Actividades | duración, participantes, etiquetas | slots y búsqueda tienen datos base |
| Seguridad | `ContextoAutenticacionService` | permisos live desde BD, no scope de sede |
| Migraciones | V22.1–V40 | pagos, ventas, permisos, recursos y destrucción V36 |

Las filas atribuibles literalmente a 01/02/03 permanecen `NO CONFIRMADO` porque los documentos no están disponibles; estas tablas contrastan las hipótesis explícitas del encargo con código real.

## 23. Impacto sobre Prompt 04

El Prompt 04 puede continuar sin cambios: **NO**

Áreas que deben actualizarse: programación efectiva y vigencias; semántica de asignación atómica; ajustes por fecha; confirmación; sesión/ocurrencia; reservas separadas conceptualmente; capacidad y recursos compartidos; ventanas por salón; consumo de beneficios; concurrencia; estado real de Stripe/ventas; seguridad por sede; baseline Flyway/Git.

Suposiciones de 01/02/03 que ya no deben utilizarse: que Stripe, webhook, reconciliación, efectivo/transferencia o carrito no existen; que solo hay máquinas sin relación actividad→recurso; que calendario solo admite un instructor/actividad; que el build no puede probarse por Java 8; que el árbol actual tiene duplicados Flyway. No debe asumirse tampoco que `HEAD` contiene el estado auditado.

Nuevos hechos que Prompt 04 debe considerar: 150 fuentes y 43 migraciones; árbol sucio/no reproducible; turno-asignación por rango parcialmente existente; duración y consumo por reserva modelados; reserva grupal actualmente imposible; horario excepcional existente pero no aplicado al reservar; falta identidad de sesión; Stripe activo sin Inbox y con reembolso iniciable ausente; 1 test real fallido; V36 borra datos.

## 24. Archivos críticos para arquitectura objetivo

- Programación: `calendario/entidad/TurnoInstructor.java`, `TurnoInstructorAsignacion.java`, `servicio/TurnoInstructorService.java`, repositories y DTOs de turno.
- Reservas: `calendario/entidad/Reserva.java`, `ReservaService.java`, `ReservaRepository.java`, `ReservaController.java`.
- Horarios: `ubicaciones/entidad/HorarioOperacion.java`, `SalonHorarioExcepcion.java`, ambos services/repositories y `SalonService.java`.
- Actividades/capacidad: `TipoActividad.java`, `TipoRecurso.java`, `SalonRecurso.java`, `ActividadRecurso.java` y controllers asociados.
- Instructor/seguridad: `Usuario.java`, `PerfilInstructor.java`, `UsuarioRol.java`, `ContextoAutenticacionService.java`, `JwtAuthFilter.java`.
- Pagos/beneficios: `Compra.java`, `Paquete.java`, `PaqueteActividad.java`, `PagoService.java`, `VentaService.java`.
- Persistencia: V11, V14–V22, V22.1–V40, con atención especial a V36/V38/V39.
- Contratos clientes: `ReservaController`, `TurnoInstructorController`, `FeelingPiltaesAppMobile/src/data/clases.ts`, `web/src/api/calendario.ts` y pantallas web de reservas.

Este listado delimita evidencia para el siguiente análisis; no constituye arquitectura objetivo, roadmap ni propuesta de implementación.
