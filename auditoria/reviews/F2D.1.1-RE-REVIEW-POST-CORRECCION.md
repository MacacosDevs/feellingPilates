# FeelingPilates — Re-review F2D.1 post F2D.1.1

## 1. Pre-flight

Branch:
`operacion/excepciones-horario-fecha`

HEAD:
`e5bcf2e229ac018be3f30c55c517865fb4d80f06`

Working tree:
Sólo ` M auditoria/fase-2d1-diseno-ajustes-programacion-fecha.md`.
Ninguna modificación staged. Upstream `0 ahead / 0 behind`.

SHA-256 checkpoint:
`0d923e69615df403153390a852ad6568bfecc586a5e54f68fcf3d825836520a4`

`git diff --check`:
PASS

Scope correcto:
SI

No se reejecutaron tests, conforme a la instrucción para este re-review documental.

## 2. P1 originales

P1-1:
PARCIAL

Evidencia:
El checkpoint sí elimina controllers y consumers explícitos, mantiene `TurnoInstructor` como autoridad y define el fence futuro `LEGACY → MIGRANDO → NUEVA` ([checkpoint:115 (line 115)]\(/Users/jesusaldaircruzortiz/Desktop/Feelingpilates/feelingpilates/auditoria/fase-2d1-diseno-ajustes-programacion-fecha.md:115)).

Sin embargo, F2D.2 también incorpora `ImpactoAjustesEnExcepcionHorario` al writer operativo de excepciones ([checkpoint:732 (line 732)]\(/Users/jesusaldaircruzortiz/Desktop/Feelingpilates/feelingpilates/auditoria/fase-2d1-diseno-ajustes-programacion-fecha.md:732)). El servicio productivo actual inyecta todos los `ValidadorImpactoExcepcionHorario` y los ejecuta antes de persistir ([SalonHorarioExcepcionService.java (line 51)]\(/Users/jesusaldaircruzortiz/Desktop/Feelingpilates/feelingpilates/src/main/java/com/feelingpilates/ubicaciones/servicio/SalonHorarioExcepcionService.java:51), [SalonHorarioExcepcionService.java (line 153)]\(/Users/jesusaldaircruzortiz/Desktop/Feelingpilates/feelingpilates/src/main/java/com/feelingpilates/ubicaciones/servicio/SalonHorarioExcepcionService.java:153)).

Por tanto, datos dark-launch de `programacion_ajuste_fecha` podrían rechazar una operación productiva legacy antes del cutover. Eso contradice “sin consumers productivos nuevos” y “infraestructura nueva aislada”.

P1-2:
CERRADO

Evidencia:
Target nominal `(asignacionSerieId, fecha)`, cardinalidad exacta 0/1/>1, ausencia deliberada de FK simple y EXCLUDE temporal sobre `programacion_asignacion` con `vigente_hasta` nullable correctamente especificados ([checkpoint:210 (line 210)]\(/Users/jesusaldaircruzortiz/Desktop/Feelingpilates/feelingpilates/auditoria/fase-2d1-diseno-ajustes-programacion-fecha.md:210), [checkpoint:624 (line 624)]\(/Users/jesusaldaircruzortiz/Desktop/Feelingpilates/feelingpilates/auditoria/fase-2d1-diseno-ajustes-programacion-fecha.md:624)).

P1-3:
CERRADO

Evidencia:
Policy A inversa cubre Asignacion y BloqueProgramacion, proyecta el estado resultante, rechaza 0/>1 targets y prohíbe reasignación, cancelación o silencio automáticos ([checkpoint:474 (line 474)]\(/Users/jesusaldaircruzortiz/Desktop/Feelingpilates/feelingpilates/auditoria/fase-2d1-diseno-ajustes-programacion-fecha.md:474)).

P1-4:
CERRADO

Evidencia:
Existe una única secuencia: nominales → ajustes → horario del salón final → maestros/invariantes → filtro/orden. Los casos origen cerrado/destino abierto y original fuera/replacement dentro aparecen correctamente ([checkpoint:375 (line 375)]\(/Users/jesusaldaircruzortiz/Desktop/Feelingpilates/feelingpilates/auditoria/fase-2d1-diseno-ajustes-programacion-fecha.md:375)).

P1-5:
CERRADO

Evidencia:
F2D.2 endurece `crearBloque`, `crearAsignacion` y cualquier writer recurrente nuevo sobre instructor, salón, actividad, rango, vigencia, serie, activo, bloque o día. Todos participan en los locks y Policy A ([checkpoint:545 (line 545)]\(/Users/jesusaldaircruzortiz/Desktop/Feelingpilates/feelingpilates/auditoria/fase-2d1-diseno-ajustes-programacion-fecha.md:545)).

P1-6:
CERRADO

Evidencia:
Discovery preliminar → salones ordenados → instructores ordenados → relectura → comparación de versión/lock set → conflicto reintentable → validación/persistencia. Los recurrentes participan del mismo protocolo ([checkpoint:558 (line 558)]\(/Users/jesusaldaircruzortiz/Desktop/Feelingpilates/feelingpilates/auditoria/fase-2d1-diseno-ajustes-programacion-fecha.md:558)).

P1-7:
CERRADO

Evidencia:
Se elimina `ImpactoReservasEnAjusteProgramacion` y la heurística `covered-before/not-covered-after`. Una reserva sin identidad nueva inequívoca bloquea el cutover futuro ([checkpoint:447 (line 447)]\(/Users/jesusaldaircruzortiz/Desktop/Feelingpilates/feelingpilates/auditoria/fase-2d1-diseno-ajustes-programacion-fecha.md:447)).

P1-8:
CERRADO

Evidencia:
El resolver revalida salón, instructor, actividad, rol, especialidad y oferta; omite fail-closed y emite señal estructurada. Los writers maestros fuera de scope están enumerados y su auditoría es fence bloqueante de activación ([checkpoint:419 (line 419)]\(/Users/jesusaldaircruzortiz/Desktop/Feelingpilates/feelingpilates/auditoria/fase-2d1-diseno-ajustes-programacion-fecha.md:419)).

Resumen:

Cerrados:
7/8

Parciales:
P1-1

No cerrados:
Ninguno completamente no cerrado.

## 3. P2 originales

P2-1:
RESUELTO

Recursos naturales con `PUT`, UUID aportado para adiciones, no-op real, update, soft delete y códigos 200/201/404 definidos. API marcada fuera de F2D.2.

P2-2:
RESUELTO

`calendario.editar` queda limitado a reemplazo sólo de horario. Cambios de salón, instructor, actividad y adiciones exigen `calendario.gestionar`; cross-salon autoriza ambos salones antes de locks.

P2-3:
RESUELTO

Helpers con deduplicación, UUID ascendente, sets por operación y orden global único salones → instructores.

## 4. Mutaciones A–O

A:
DETECTA

B:
DETECTA

C:
DETECTA

D:
DETECTA

E:
DETECTA

F:
DETECTA

G:
DETECTA

H:
DETECTA

I:
DETECTA

J:
DETECTA

K:
DETECTA

L:
DETECTA

M:
DETECTA

N:
NO DETECTA

El propio alcance introduce un consumer productivo indirecto: el adapter de ajustes dentro del writer operativo de excepciones.

O:
DETECTA

Resumen:
14 DETECTA, 0 PARCIAL, 1 NO DETECTA.

## 5. ProgramacionEfectiva

Nominal antes de ajustes:
SI

Ajustes antes de horario final:
SI

Horario del salón resultado:
SI

Origen cerrado → destino abierto:
APARECE

Original fuera → replacement válido:
APARECE

Scoping post-ajustes:
SI

Resultado:
PASS

## 6. Persistencia/V47

EXCLUDE temporal:
Correcto sobre `serie_id` + `daterange(vigente_desde, vigente_hasta, '[]')`, condicionado por `activo`.

Target/snapshot:
Separados. Cancelación sin resultado; reemplazo y adición con snapshot completo.

CHECKs:
Tipo, forma completa por tipo y `horaFin > horaInicio`.

Unique target:
Índice parcial correcto para CANCELACION/REEMPLAZO activos por serie+fecha.

FK serieId:
NINGUNA, correctamente.

Índices:
Target, salón resultado+fecha, instructor resultado+fecha y fecha activa. No existe unique excesivo por salón/hora.

Resultado:
PASS

## 7. Concurrencia

SalonLocks ordenados:
SI; deduplicados y UUID ascendente.

InstructorLocks ordenados:
SI; deduplicados y UUID ascendente.

Orden global único:
SALONES → INSTRUCTORES.

Writers recurrentes participan:
SI.

TOCTOU cerrado:
SI para CANCELACION/REEMPLAZO.

Cross-salon:
Origen/destino e instructor original/nuevo incluidos según operación.

Resultado:
PASS

## 8. Reservas

Legacy participa:
NO en ajustes F2D.2.

Heurística causal:
NO.

Blocker cutover futuro:
SI; mapeo/referencia inequívoca obligatorio.

Resultado:
PASS

## 9. Fail-closed

Instructor:
Estado activo y rol revalidados; inválido se omite y señala.

Actividad:
Estado activo revalidado.

Especialidad:
Compatibilidad actual revalidada.

Salón/operación:
Salón activo, oferta y `HorarioEfectivoSalon` final revalidados.

Master-data policy:
Resolver fail-closed en F2D.2; writers maestros enumerados fuera de scope; auditoría bloqueante antes de `NUEVA`.

Resultado:
PASS

## 10. Dark launch / autoridad

F2D.2 dark launch:
NO, por contradicción interna.

Controllers nuevos F2D.2:
NO

TurnoInstructor autoridad:
SI

Doble autoridad:
SI, indirecta: los ajustes no son fuente visible de programación, pero adquieren poder productivo para vetar cambios de horario legacy.

Fence futuro:
Bien especificado, pero no implementado y por tanto incapaz de aislar el adapter incluido en F2D.2.

Resultado:
FAIL

## 11. Identidad

Recurrente:
`(SERIE_ASIGNACION, serieId, fecha)`

Replacement:
La misma identidad recurrente.

Adición:
`(AJUSTE, ajusteId, fecha)`

Resultado:
PASS

## 12. Fidelidad de intervención

Correcciones F2D.1.1 aplicadas:
Aplicadas completa y literalmente.

Especificaciones antiguas contradictorias activas:
NINGUNA. Existe, sin embargo, una contradicción nueva/interna entre dark launch y `ImpactoAjustesEnExcepcionHorario`.

Scope ampliado:
NO respecto de la intervención preparada.

Resultado:
PASS de fidelidad literal; no implica aprobación del diseño.

## 13. Decisiones abiertas

Checkpoint declara:
NINGUNA

Auditoría encuentra:
Una decisión bloqueante: diferir `ImpactoAjustesEnExcepcionHorario` hasta la activación/fence, o implementar un fence que impida que datos dark-launch afecten el writer productivo. La segunda opción contradice el scope actual, que excluye el fence persistido.

Resultado:
FAIL

## 14. Hallazgos nuevos

### P0

NINGUNO

### P1

`ImpactoAjustesEnExcepcionHorario` rompe el aislamiento dark-launch.

El checkpoint afirma que F2D.2 no incorpora consumers productivos, pero agrega un adapter al conjunto ejecutado sincrónicamente por `SalonHorarioExcepcionService`. Ese conjunto rechaza la operación si cualquier adapter reporta conflicto ([SalonHorarioExcepcionErrores.java (line 75)]\(/Users/jesusaldaircruzortiz/Desktop/Feelingpilates/feelingpilates/src/main/java/com/feelingpilates/ubicaciones/servicio/SalonHorarioExcepcionErrores.java:75)).

Así, programación no productiva puede modificar el resultado observable de un writer productivo antes de existir cutover o fence.

### P2

NINGUNO

## 15. Gate

P0:
0

P1:
1

P2:
0

8 P1 originales cerrados:
NO

¿F2D.1 puede pasar a DISEÑO\_APROBADO?:
NO

¿Puede iniciarse F2D.2 después del cierre documental correspondiente?:
NO

## 16. Veredicto

**B. REQUIERE F2D.1.2 — PERSISTEN P1**

Justificación:
F2D.1.1 cierra correctamente siete de los ocho P1 originales y resuelve persistencia, identidad, composición, locks, TOCTOU, reservas y fail-closed. No obstante, el alcance de F2D.2 contradice su propio fence: `ImpactoAjustesEnExcepcionHorario` convierte los ajustes dark-launch en una dependencia con efecto productivo sobre el writer vigente de excepciones. Debe retirarse/diferirse del alcance F2D.2 o quedar protegido por un fence efectivo antes de aprobar el diseño.