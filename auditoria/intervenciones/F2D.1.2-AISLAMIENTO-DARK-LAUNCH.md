# FeelingPilates — F2D.1.2
## Corrección post re-review de aislamiento dark-launch

F2D.1.1 fue ejecutada y posteriormente sometida a re-review adversarial.

Resultado del re-review:

P0: 0
P1: 1
P2: 0

Veredicto:

REQUIERE F2D.1.2 — PERSISTE P1

NO implementar F2D.2.
NO modificar código.
NO modificar tests.
NO crear migraciones.
NO modificar frontend/mobile.
NO hacer commit.
NO hacer push.

Esta intervención es EXCLUSIVAMENTE DOCUMENTAL.

==================================================
1. BASE ESPERADA
==================================================

Repository root:

/Users/jesusaldaircruzortiz/Desktop/Feelingpilates/feelingpilates

Branch:

operacion/excepciones-horario-fecha

HEAD:

e5bcf2e229ac018be3f30c55c517865fb4d80f06

Upstream esperado:

0 ahead / 0 behind

Working tree esperado:

únicamente:

M auditoria/fase-2d1-diseno-ajustes-programacion-fecha.md

Checkpoint:

auditoria/fase-2d1-diseno-ajustes-programacion-fecha.md

SHA-256 esperado del checkpoint después de F2D.1.1:

0d923e69615df403153390a852ad6568bfecc586a5e54f68fcf3d825836520a4

==================================================
2. FUENTE DEL HALLAZGO
==================================================

Leer obligatoriamente:

auditoria/reviews/F2D.1-REVIEW-AJUSTES-PUNTUALES.md

auditoria/intervenciones/F2D.1.1-CORRECCION-POST-REVIEW.md

auditoria/fase-2d1-diseno-ajustes-programacion-fecha.md

y contrastar con el código actual relevante de:

SalonHorarioExcepcionService
ValidadorImpactoExcepcionHorario
SalonHorarioExcepcionErrores

La auditoría posterior a F2D.1.1 detectó:

P1 — ImpactoAjustesEnExcepcionHorario rompe el aislamiento dark-launch.

El checkpoint declara simultáneamente:

1. F2D.2 es DARK LAUNCH;
2. no existen consumers productivos nuevos;
3. TurnoInstructor continúa siendo autoridad productiva;
4. programacion_* debe permanecer aislado;

pero incluye:

ImpactoAjustesEnExcepcionHorario

dentro del flujo productivo actual de modificación de excepciones
de horario.

Como SalonHorarioExcepcionService ejecuta los
ValidadorImpactoExcepcionHorario antes de persistir, datos contenidos
en programacion_ajuste_fecha podrían vetar una operación productiva
legacy antes del cutover.

Eso contradice el dark launch.

==================================================
3. DECISIÓN DE F2D.1.2
==================================================

Adoptar como corrección:

ImpactoAjustesEnExcepcionHorario
NO pertenece a F2D.2.

Debe diferirse explícitamente a la futura fase de activación/cutover,
cuando exista el fence efectivo por salón.

NO resolver este P1 adelantando la implementación del fence a F2D.2.

Razón:

el fence persistido LEGACY/MIGRANDO/NUEVA está explícitamente fuera
del alcance F2D.2.

Ampliar F2D.2 para implementarlo únicamente con el fin de proteger
este adapter contradice la estrategia de dark launch y expande
innecesariamente el scope.

==================================================
4. INVARIANTE DE DARK LAUNCH
==================================================

Cerrar explícitamente una regla general:

Durante F2D.2 ningún estado contenido exclusivamente en la nueva
programación puede alterar el resultado observable de un flujo
productivo legacy.

Esto incluye:

- permitir;
- rechazar;
- modificar;
- ocultar;
- transformar;

una operación productiva.

Por tanto, durante F2D.2:

programacion_ajuste_fecha

NO puede participar directa ni indirectamente en:

- controllers productivos;
- writers productivos legacy;
- validadores ejecutados por writers productivos;
- consumers productivos.

Los services internos y tests F2D.2 pueden leer/escribir la nueva
infraestructura de forma aislada.

==================================================
5. IMPACTOAJUSTESENEXCEPCIONHORARIO
==================================================

Eliminarlo del scope de implementación de F2D.2.

Si aparece:

- en alcance;
- arquitectura propuesta;
- wiring;
- lista de adapters;
- test plan;
- mutaciones;
- integración;
- secuencia de implementación;

corregir todas las referencias necesarias para que exista UNA SOLA
especificación vigente.

No dejar una sección antigua que todavía indique que el adapter se
implementará o registrará durante F2D.2.

==================================================
6. FASE FUTURA
==================================================

Conservar el concepto como diseño futuro.

ImpactoAjustesEnExcepcionHorario podrá incorporarse únicamente en una
fase de activación donde el fence por salón sea efectivo.

Semántica futura:

LEGACY:
la nueva programación NO afecta el writer productivo legacy.

MIGRANDO:
writers externos bloqueados conforme a la estrategia de cutover.

NUEVA:
la programación nueva puede participar en validaciones productivas
según el contrato aprobado de esa futura fase.

No implementar esto ahora.

No crear nombre artificial de fase si el proyecto todavía no lo tiene
cerrado.

Puede quedar descrito simplemente como:

FASE FUTURA DE ACTIVACIÓN/CUTOVER.

==================================================
7. IMPACTO SOBRE HORARIOS
==================================================

No eliminar la necesidad funcional futura de proteger ajustes cuando
se cambie HorarioEfectivoSalon.

La corrección es temporal/arquitectónica:

NO:

"los ajustes nunca afectan cambios de horario"

SÍ:

"esa protección no puede integrarse al writer productivo mientras
los ajustes sean dark-launch".

Antes de activar un salón en NUEVA deberá existir una estrategia que
impida que cambios de horario dejen programación nueva productiva en
estado inválido.

==================================================
8. MUTACIÓN N
==================================================

Actualizar la matriz adversarial.

Mutación N:

consumer mezcla legacy y nueva fuente

debe quedar:

DETECTADA

por la regla:

durante dark launch no existe ninguna integración de ajustes nuevos
con consumers/writers productivos legacy.

ImpactoAjustesEnExcepcionHorario queda diferido hasta existir fence.

==================================================
9. MUTACIONES RELACIONADAS
==================================================

Verificar que la corrección NO reabra:

P1-8 / mutación J.

Aunque ImpactoAjustesEnExcepcionHorario se difiera, el resolver
ProgramacionEfectiva de F2D.2 debe continuar fail-closed frente a
horario efectivo y master-data actual.

La protección writer-time sobre excepciones productivas es distinta
de la validación read-time del resolver dark-launch.

Mantener esa distinción explícita.

==================================================
10. TEST PLAN
==================================================

Eliminar de F2D.2 cualquier test que requiera integrar
ImpactoAjustesEnExcepcionHorario con SalonHorarioExcepcionService.

Añadir/ajustar pruebas arquitectónicas o de integración que demuestren:

- no existe bean/adapter F2D conectado al writer productivo de
  excepciones;
- programacion_ajuste_fecha no puede vetar una excepción legacy;
- SalonHorarioExcepcionService conserva durante F2D.2 únicamente
  los adapters productivos previamente existentes;
- ProgramacionEfectiva continúa validando HorarioEfectivoSalon
  de manera aislada.

No implementar estos tests ahora.
Sólo definirlos en el diseño.

==================================================
11. SCOPE F2D.2 FINAL
==================================================

Actualizar la sección de alcance para mantener dentro:

- V47 propuesta;
- AjusteProgramacionFecha;
- repositorio;
- resolver nominal/efectivo;
- locks;
- hardening BloqueProgramacion/Asignacion;
- Policy A inversa;
- fail-closed;
- tests internos/concurrencia/PostgreSQL.

Y fuera:

- controllers públicos;
- frontend;
- mobile;
- Reserva legacy adapter;
- cutover;
- fence persistido;
- consumers productivos;
- adapters que alteren writers productivos legacy;
- ImpactoAjustesEnExcepcionHorario.

==================================================
12. DECISIONES ABIERTAS
==================================================

Después de la corrección:

Decisiones abiertas:
NINGUNA

sólo si efectivamente ya no queda elección arquitectónica bloqueante.

Si aparece otra contradicción:

NO inventar solución.

Declarar F2D.1 BLOQUEADA y reportarla.

==================================================
13. SCOPE DE ESTA INTERVENCIÓN
==================================================

Modificar EXCLUSIVAMENTE:

auditoria/fase-2d1-diseno-ajustes-programacion-fecha.md

NO modificar:

auditoria/reviews/
auditoria/intervenciones/
auditoria/handoffs/
auditoria/ESTADO-ACTUAL.md
otros canónicos
src/main
src/test
migraciones
frontend
mobile

==================================================
14. VALIDACIÓN
==================================================

Antes de modificar:

git branch --show-current
git rev-parse HEAD
git status --short

shasum -a 256 \
auditoria/fase-2d1-diseno-ajustes-programacion-fecha.md

Debe coincidir con:

0d923e69615df403153390a852ad6568bfecc586a5e54f68fcf3d825836520a4

Después:

git status --short
git diff --check

git diff -- \
auditoria/fase-2d1-diseno-ajustes-programacion-fecha.md

calcular nuevo SHA-256.

No ejecutar tests:
esta intervención sigue siendo documental.

==================================================
15. NO COMMIT
==================================================

NO git add.
NO commit.
NO push.

F2D.1 deberá someterse nuevamente a re-review independiente.

==================================================
16. SALIDA OBLIGATORIA
==================================================

# F2D.1.2 — Aislamiento dark-launch corregido

Branch:
...

HEAD:
...

Checkpoint:
...

SHA-256 antes:
...

SHA-256 después:
...

P1 corregido:
ImpactoAjustesEnExcepcionHorario rompe aislamiento dark-launch

ImpactoAjustesEnExcepcionHorario en F2D.2:
SI / NO

Fase futura:
...

Regla dark-launch:
...

Mutación N:
DETECTADA / NO

P1-8 preservado:
SI / NO

ProgramacionEfectiva fail-closed:
SI / NO

Scope F2D.2:
...

Consumers productivos nuevos:
NINGUNO / ...

Decisiones abiertas:
NINGUNA / ...

src/main:
SIN CAMBIOS

src/test:
SIN CAMBIOS

Migraciones:
SIN CAMBIOS

Otros documentos:
SIN CAMBIOS

git diff --check:
PASS / FAIL

Working tree:
...

Commit:
NO CREADO

F2D.1:
LISTA PARA NUEVO RE-REVIEW / BLOQUEADA

F2D.2:
NO INICIADA

DETENTE.

NO COMMIT.
NO PUSH.
NO EJECUTES F2D.2.
