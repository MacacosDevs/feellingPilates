# FeelingPilates — Handoff F2E / preparación

Handoff status: `CLOSED / HISTORICAL / SUPERSEDED_BY_F2E1_DESIGN_CLOSURE`

Target unit: `F2E / preparación`

Type: `PREPARATION / DESIGN`

Este handoff materializó autoridad persistente exclusivamente para preparar y cerrar el diseño
verificable de una futura transición controlada desde el dark launch F2D.2. Su auditoría fresh e
independiente de contrato reportó `P0=0 / P1=0 / P2=0` y queda persistida en
`auditoria/reviews/HANDOFF-F2E-PREPARACION-REVIEW-DOCUMENTAL.md`. Sus exit conditions fueron
satisfechas por el checkpoint F2E.1 y su audit final independiente `P0=0 / P1=0 / P2=0`, persistido
en `auditoria/reviews/F2E.1-REVIEW-DISENO-PREPARACION.md`; por ello este handoff queda cerrado e
histórico. No existe handoff activo derivado de este cierre.

La unidad autorizada no implementa, migra, activa ni ejecuta cutover. Su producto es evidencia y
contratos de preparación que permitan decidir, mediante otra intervención y otro gate, si existe
una fase posterior segura.

## 1. Autoridad y trazabilidad

La autoridad de este handoff procede de fuentes físicas del repositorio, no de chats:

- `auditoria/ESTADO-ACTUAL.md` registra F2D.2 como cerrada, materializada, aprobada técnica y
  documentalmente, publicada y con cierre de publicación verificado;
- `auditoria/ARQUITECTURA-ACTUAL.md` mantiene `TurnoInstructor` como programación
  `LEGACY_VIVO / PRODUCTIVO` y la programación nueva como no productiva;
- `auditoria/DECISIONES-ARQUITECTONICAS.md`, especialmente DA-004 y DA-013, exige migración
  incremental, preservación de consumers y dark launch aislado;
- `auditoria/contexto/MAPA-LEGACY-Y-MIGRACION.md` mantiene una única autoridad productiva y
  enumera las condiciones todavía pendientes antes del cutover;
- `auditoria/fase-2d1-diseno-ajustes-programacion-fecha.md`, en «Cutover futuro por salón» y
  «Secuencia futura cerrada», autoriza expresamente la secuencia funcional siguiente:

```text
F2D.2 dark launch
→ F2E / preparación
→ LEGACY
→ MIGRANDO
→ NUEVA
```

La numeración no es la autoridad por sí sola. La autoridad material es el diseño F2D.1 aprobado,
su review final, el cierre F2D.2 y los canónicos vigentes.

Predecessor cerrado e histórico:

`auditoria/handoffs/HANDOFF-F2D2.md`

No se reabre, reescribe ni reejecuta F2D.2.

## 2. Baseline de materialización del handoff

```text
Branch: operacion/excepciones-horario-fecha
HEAD: 23caf0c3c7994b600f330024e45fb68ec94d3449
Working tree inicial: CLEAN
Handoff funcional activo previo: NINGUNO
```

Estos valores registran el corte físico de creación. El inicio eventual de F2E exige un pre-flight
nuevo; no puede reutilizar este snapshot como sustituto de la evidencia actual.

## 3. Prerrequisito F2D.2 preservado

El cierre físico y documental disponible establece:

```text
F2D.2: CLOSED / CERRADA
design: APROBADO
materialization: MATERIALIZED
technical gate: PASS
documentation gate: PASS
publication: PASS
publication closure: PASS
runtime: DARK_LAUNCH
productive: NOT_PRODUCTIVE
cutover: false
authority: TurnoInstructor / LEGACY_VIVO / PRODUCTIVO
```

La materialización y publicación de F2D.2 no equivalen a productividad ni cutover. Este handoff
preserva esos ejes sin reinterpretarlos.

## 4. Propósito exacto

F2E / preparación debe producir un diseño autocontenido y verificable para:

1. conocer físicamente la coexistencia legacy/nueva;
2. definir cómo auditar datos sin modificarlos;
3. diseñar normalización y migración de series sin ejecutarlas;
4. especificar un resolver comparativo legacy/nuevo sin implementarlo;
5. preparar una identidad o referencia futura inequívoca para reservas;
6. cerrar la estrategia futura de writers, readers y consumers;
7. diseñar el fence, los gates, los abort conditions y la evidencia de una activación posterior;
8. identificar blockers y delimitar una posible intervención siguiente.

F2E / preparación no activa el modelo nuevo ni cambia la autoridad productiva.

## 5. Entry conditions

Antes de iniciar la unidad, un agente debe verificar físicamente y registrar:

- F2D.2 `CERRADA` y `MATERIALIZED`;
- technical gate `PASS`;
- documentation gate `PASS`;
- publication closure `PASS`;
- runtime `DARK_LAUNCH`;
- productive `NOT_PRODUCTIVE`;
- `cutover=false`;
- `TurnoInstructor` como única autoridad productiva de programación;
- ningún handoff funcional activo previo incompatible;
- branch, `HEAD`, staging y working tree actuales;
- cualquier dirty baseline expresamente autorizado, separado del delta de F2E;
- presencia y coherencia de los inputs obligatorios de la sección 7;
- scope exclusivamente preparatorio, read-only y documental;
- ausencia de una contradicción material entre canónicos y evidencia física.

Si una condición no se cumple, no se inicia la unidad. Se registra el blocker y se aplica
`HUMAN_STOP` cuando corresponda.

## 6. Workflow profile y gates

```text
WORKFLOW_PROFILE: F2E_PREPARATION_DESIGN_ONLY
MATERIALIZACIÓN TÉCNICA: NOT_APPLICABLE
IMPLEMENTATION GATE: NOT_APPLICABLE
TESTS GATE: NOT_APPLICABLE
HOST VALIDATION: NOT_APPLICABLE
SCOPE/SECURITY CONTROLS: APPLICABLE
DESIGN/DOCUMENTATION GATE: APPLICABLE / PENDING
```

`NOT_APPLICABLE` no significa `PASS`. Los controles de scope, touched paths, `HEAD`, staging y
seguridad no pueden omitirse. Cualquier publicación o cierre de publicación de outputs de F2E
requiere autoridad y profile propios; no se infiere desde este handoff.

Gate de aprobación del handoff:

```text
HANDOFF_CONTRACT: PASS
CANONICAL_CONSISTENCY: PASS
SECURITY_SCOPE: PASS
```

Siguiente gate de F2E / preparación:

`FRESH_INDEPENDENT_DESIGN_DOCUMENT_AUDIT: PENDING`

La aprobación del handoff sólo habilita la unidad preparatoria aquí delimitada. No autoriza
implementación. La aprobación posterior del diseño F2E tampoco autoriza automáticamente una fase
técnica, migración, activación o cutover.

## 7. Required inputs

F2E / preparación debe usar, como mínimo:

- canónicos vigentes de estado, arquitectura, decisiones, reglas de trabajo, dominio y migración;
- diseño/checkpoint F2D.1 aprobado:
  `auditoria/fase-2d1-diseno-ajustes-programacion-fecha.md`;
- review final aprobatorio F2D.1:
  `auditoria/reviews/F2D.1.2-RE-REVIEW-FINAL.md`;
- SHA-256 aprobado del checkpoint F2D.1:
  `58af39f41b3bc089ebbd4ec67f684e270087ddf4eb695f2c7b55276d0aff352e`;
- checkpoint de implementación F2D.2:
  `auditoria/fase-2d2-implementacion-dark-launch-ajustes-programacion-fecha.md`;
- review y cierre F2D.2:
  `auditoria/reviews/F2D.2-REVIEW-DOCUMENTAL.md`;
- predecessor histórico `auditoria/handoffs/HANDOFF-F2D2.md`;
- branch y `HEAD` verificados fresh;
- código y migraciones físicos mediante inspección read-only;
- inventario físico actual de entidades, writers, readers, consumers, jobs, controllers,
  servicios e integraciones;
- una fuente o snapshot de datos sólo cuando haya autorización expresa, identificable y
  read-only.

No se usan chats, memoria de agentes ni resultados inferidos como fuente canónica.

## 8. Scope autorizado

Se autorizan exclusivamente actividades de preparación y diseño:

1. pre-flight físico fresh;
2. inventario read-only de esquema, entidades, migraciones, writers, readers, consumers,
   integraciones legacy e integraciones del modelo nuevo;
3. diseño del contrato de auditoría de datos;
4. diseño de normalización/migración de series;
5. clasificación y diseño de criterios de anomalías;
6. diseño de abort conditions y rollback conceptual;
7. especificación del resolver comparativo legacy/nuevo;
8. definición del criterio de equivalencia entre ambas fuentes;
9. preparación de identidad/referencia futura para reservas;
10. matriz legacy/nuevo por entidad, writer, reader y consumer;
11. identificación y clasificación de blockers para fases posteriores;
12. definición de inputs, evidence requirements, gates, stops y fail-closed;
13. auditoría/diseño de la necesidad de hardening inverso de writers maestros;
14. preparación de un futuro checkpoint autocontenido F2E / preparación.

La inspección de código, migraciones, jobs, controllers, servicios, reservas, frontend y mobile
es únicamente read-only y sólo para identificar contratos o consumers. Su inclusión en el
inventario no autoriza modificarlos.

## 9. Auditoría de datos

F2E autoriza diseñar el contrato de auditoría de datos, que debe definir al menos:

- fuentes y snapshot;
- alcance temporal y por salón;
- queries o procedimientos read-only propuestos;
- métricas, conteos y evidencia reproducible;
- anomalías y severidad;
- criterios de equivalencia;
- tratamiento de datos ambiguos;
- abort conditions;
- custodia, trazabilidad y no mutación.

Una auditoría material de datos está dentro del scope sólo si una autoridad expresa identifica
una fuente concreta y permite acceso read-only. Este handoff no identifica ni autoriza por sí
mismo ninguna fuente de datos. Mientras no exista esa autorización, el checkpoint debe registrar:

`DATA_SOURCE_NOT_AVAILABLE`

No se inventan conteos, equivalencias, anomalías, ausencia de anomalías ni resultados de datos.
No se normaliza, repara, migra o modifica información bajo la etiqueta de auditoría.

## 10. Normalización y migración de series

F2E autoriza únicamente diseñar un contrato que especifique:

- clases de anomalías;
- datos elegibles;
- datos ambiguos o no mapeables;
- precondiciones y abort conditions;
- reconciliación antes/después;
- idempotencia esperada;
- evidencia requerida;
- rollback conceptual;
- orden y aislamiento propuestos;
- tratamiento fail-closed.

Quedan prohibidos en esta unidad la migración productiva, la normalización material, la reparación
de datos, la ejecución de SQL mutante y la creación de una migración Flyway.

## 11. Resolver comparativo legacy/nuevo

F2E debe especificar, sin implementar, un resolver comparativo que cubra:

- inputs físicos y ventana de comparación;
- outputs deterministas y evidencia;
- identidad legacy y nueva;
- criterio exacto de equivalencia;
- divergencias esperadas y no esperadas;
- cardinalidad y duplicados;
- casos ambiguos;
- diagnóstico y trazabilidad;
- comportamiento fail-closed.

Ante ambigüedad o falta de identidad, el resolver diseñado no puede elegir, deduplicar ni asumir
equivalencia silenciosamente. La implementación del resolver requiere una intervención posterior
explícita.

## 12. Reservas

`Reserva` y `ReservaService` permanecen en el universo productivo legacy de `TurnoInstructor`.

F2E puede diseñar:

- estrategia de mapping;
- identidad o referencia futura;
- criterios de mapping inequívoco;
- evidencia de asociación;
- casos ambiguos;
- blockers para migración o cutover.

Una coincidencia por salón, instructor, actividad, fecha u horario no es por sí sola identidad.
Una reserva sin mapeo inequívoco bloquea cualquier cutover posterior.

F2E no modifica `Reserva`, `ReservaService`, repositorios de reserva, controllers, frontend o
mobile, ni integra productivamente reservas con el modelo nuevo.

## 13. Writers, readers y consumers

F2E debe inventariar y diseñar una matriz que incluya, cuando existan:

- writers legacy;
- writers nuevos internos;
- readers;
- consumers;
- jobs;
- controllers;
- servicios;
- reservas;
- frontend y mobile como consumers productivos o potenciales.

Para cada elemento debe registrar fuente leída/escrita, autoridad, estado, salón o alcance,
riesgos, dependencia de identidad y condición futura de transición.

Se prohíbe activar writers nuevos, cambiar rutas productivas de lectura, crear consumers
productivos, retirar consumers legacy o permitir doble autoridad.

## 14. Fence y cutover futuro

F2E puede diseñar, pero no implementar ni persistir, un fence por salón con estados:

```text
LEGACY
MIGRANDO
NUEVA
```

Contrato futuro preservado:

- `LEGACY`: writers y consumers productivos sólo legacy;
- `MIGRANDO`: writers externos detenidos; migración y auditoría internas, sin doble autoridad;
- `NUEVA`: writers y consumers productivos sólo nuevos; writers legacy rechazados.

F2E no entra realmente en `MIGRANDO` ni en `NUEVA`, no persiste estado productivo del fence y no
ejecuta ninguna transición.

Cutover está expresamente fuera de scope. Antes de cualquier cutover posterior deben existir, sin
declararlos satisfechos anticipadamente:

- identidad inequívoca;
- estrategia de reservas;
- auditoría de datos con fuente autorizada;
- normalización/migración cuando aplique;
- resolver comparativo;
- estrategia exacta de writers/readers/consumers;
- fence y gate de activación aprobados;
- ausencia de ambigüedades bloqueantes.

## 15. Hardening inverso

F2E puede auditar y decidir documentalmente si los writers maestros requieren hardening inverso
antes o después de una activación. Debe identificar writers, invariantes, riesgos, orden y fase
propuesta.

No implementa hardening. Si el diseño concluye que es necesario, su materialización requiere una
intervención separada, scope explícito y gate propio.

## 16. Decisiones trasladadas a F2E

F2E debe cerrar como decisiones de diseño/preparación:

| Decisión | Resultado exigido en F2E | Ejecución material |
| --- | --- | --- |
| Contrato de auditoría de datos | Diseño, fuentes admisibles, evidencia y stops | Sólo con fuente read-only autorizada |
| Normalización/migración de series | Contrato, elegibilidad, anomalías, idempotencia y rollback conceptual | Fase posterior |
| Resolver comparativo | Especificación, identidad, equivalencia y fail-closed | Fase posterior |
| Reservas | Estrategia preparatoria, criterios inequívocos y blockers | Fase posterior |
| Writers/readers/consumers | Inventario y estrategia exacta de transición | Switching posterior |
| Fence/gate de activación | Diseño de estados, transiciones, evidencia y abort conditions | Implementación/cutover posterior |
| Hardening inverso | Decisión de necesidad y scope | Intervención posterior, si aplica |

F2E sólo deja preparadas para un cutover posterior las mutaciones, implementaciones, migraciones,
switches, persistencia del fence, activación y cambio de autoridad. No resuelve esas acciones al
cerrar su diseño.

Si una decisión de diseño no puede cerrarse desde canónicos y evidencia autorizada, se registra
como blocker; no se inventa una política.

## 17. Expected outputs

La unidad debe producir un checkpoint autocontenido de diseño/preparación que contenga, como
mínimo:

- pre-flight y scope físico;
- matriz legacy/nuevo por entidad, writer, reader y consumer;
- inventario de writers, readers, consumers, jobs, controllers, servicios e integraciones;
- plan de auditoría de datos y estado de disponibilidad de la fuente;
- clasificación de anomalías;
- contrato de normalización/migración;
- abort conditions y rollback conceptual;
- especificación del resolver comparativo;
- criterio exacto de equivalencia;
- estrategia preparatoria de identidad/referencia para reservas;
- estrategia futura de writers/readers/consumers;
- diseño del fence y gate futuro, sin implementación;
- decisión sobre hardening inverso;
- evidence requirements;
- decisiones cerradas;
- decisiones abiertas y su causa;
- blockers clasificados;
- scope exacto propuesto para una siguiente intervención.

Estos outputs no equivalen a autorización de implementación. El scope propuesto para la
siguiente intervención es una recomendación sujeta a un nuevo handoff/intervención y gate.

## 18. Scope explícitamente prohibido

F2E / preparación no autoriza:

- código productivo;
- controllers o endpoints nuevos;
- frontend o mobile;
- cambios en `Reserva`, `ReservaService` o repositorios de reserva;
- migración o normalización productiva;
- reparación o mutación de datos;
- migraciones Flyway;
- writer switching;
- consumer switching;
- productive read path nuevo;
- productive write path nuevo;
- integración productiva con `TurnoInstructor`;
- integración productiva de reservas con programación nueva;
- consumers productivos nuevos;
- retiro de consumers legacy;
- fence persistido;
- entrada en `MIGRANDO`;
- entrada en `NUEVA`;
- cutover;
- cambio de autoridad productiva;
- doble autoridad;
- retiro de contratos legacy;
- eliminación de `TurnoInstructor`;
- activación de `BloqueProgramacion`, `Asignacion`, `AjusteProgramacionFecha` o
  `ProgramacionEfectiva` como autoridad productiva;
- hardening de writers maestros;
- reabrir o reejecutar F2D.2.

## 19. Fail-closed

Ante ambigüedad, evidencia incompleta o fuentes contradictorias:

```text
NO migrar
NO normalizar
NO reparar
NO mapear silenciosamente
NO asumir equivalencia
NO activar
NO cambiar authority
NO ejecutar cutover
```

Se registra un blocker con evidencia, alcance e impacto. La ausencia de datos no se transforma en
un resultado positivo.

## 20. HUMAN_STOP

Se aplica `HUMAN_STOP` ante una decisión humana real, incluyendo:

- autoridad insuficiente;
- contradicción canónica material no reconciliable;
- fuente de datos esencial pero no autorizada;
- decisión de producto o arquitectura no resoluble desde canónicos;
- mapeo ambiguo que exige política de negocio;
- cambio de autoridad productiva;
- cutover;
- P0, P1 no corregible o `SECURITY_STOP`;
- necesidad de mutar un path o sistema fuera del scope.

No se usa `HUMAN_STOP` para problemas mecánicos recuperables y seguros. Esos problemas se
clasifican, corrigen o reintentan conforme al protocolo sin consumir una decisión semántica.

## 21. Exit conditions

F2E / preparación sólo puede considerarse cerrada cuando:

- el checkpoint de diseño/preparación es autocontenido;
- existe evidencia suficiente para cada claim;
- no se inventaron resultados de datos;
- decisiones necesarias para el gate siguiente están explícitas;
- decisiones abiertas y blockers están clasificados;
- el scope exacto de una intervención posterior está delimitado sin autorizarla;
- no ocurrió ninguna mutación productiva;
- no se modificaron código, tests, migraciones, controllers, frontend o mobile;
- `TurnoInstructor` sigue siendo la autoridad productiva;
- runtime productivo y rutas productivas no cambiaron;
- `DARK_LAUNCH` y `NOT_PRODUCTIVE` permanecen;
- `cutover=false`;
- no existe doble autoridad;
- una auditoría fresh e independiente de diseño/documentación reporta `P0=0` y `P1=0`.

Antes de ese audit el estado máximo es `DESIGN_PREPARED / IN_REVIEW`. El autor del checkpoint no
puede aprobarlo.

## 22. Regla para cualquier fase posterior

Cualquier implementación, auditoría material con nueva fuente, migración, activación o cutover
requiere cumulativamente:

```text
nuevo handoff o intervención
+ scope explícito
+ pre-flight fresh
+ evidencia requerida
+ gate correspondiente
```

Ni este handoff, ni su aprobación, ni el futuro cierre documental de F2E confieren esa autoridad.

## 23. Cierre histórico

Las exit conditions de la sección 21 quedaron satisfechas por F2E.1: existe checkpoint
autocontenido, no hubo mutaciones productivas, `TurnoInstructor` y los ejes
`DARK_LAUNCH / NOT_PRODUCTIVE / cutover=false` permanecen, y el audit fresh e independiente final
reportó `P0=0 / P1=0 / P2=0`. Este cierre consume únicamente la autoridad preparatoria de este
handoff. No autoriza implementación, migración, F2E.2, cutover ni cambio de autoridad, y no crea
un handoff siguiente.
