# FeelingPilates — Reglas de trabajo con agentes de IA

Status: CANONICAL
Last updated: 2026-08-25
Repository verification: VERIFIED
Last verified against commit:
8c40594d2caf8b5230b364cb76cd8f48fe5ed98a
Verification scope: workflow

## 1. Propósito

Este documento define el protocolo para intervenir FeelingPilates con ayuda de agentes de IA sin perder trazabilidad, scope ni control sobre el repositorio.

Principio:

```text
Agentes
→ razonan y operan

Git + tests
→ aportan evidencia material

Documentación
→ conserva estado y decisiones

Chats
→ son temporales
```

## 2. Roles

### ChatGPT web

Responsabilidades habituales:

- coordinación;
- reconstrucción de contexto;
- análisis arquitectónico;
- preparación de intervenciones;
- interpretación de resultados;
- decisión sobre gates y siguiente paso.

No sustituye la verificación del repositorio.

### Claude Code

Responsabilidades habituales:

- lectura profunda del repositorio;
- diseño detallado;
- implementación;
- creación/actualización de checkpoints;
- ejecución de tests/builds;
- investigaciones locales.

Debe trabajar siempre con alcance explícito.

### Codex

Responsabilidad habitual:

- review independiente/adversarial;
- búsqueda de fallos de concurrencia;
- inconsistencias de dominio;
- seguridad;
- persistencia;
- APIs;
- mutaciones que deberían detectar los tests.

No debe modificar el mismo working tree simultáneamente con un implementador.

### Git

Es la fuente de verdad sobre:

- archivos existentes;
- commits;
- branches;
- diffs.

La documentación puede describir el estado esperado, pero no reemplaza Git.

### Tests

Aportan evidencia ejecutable de los comportamientos que cubren.

Regla:

```text
tests verdes != arquitectura aprobada
```

### Checkpoints

Conservan el diseño, implementación y decisiones de una fase en su contexto histórico.

No son automáticamente autoridad sobre el estado presente.

---

# 3. Pre-flight obligatorio

Antes de una intervención que interactúe con el repositorio:

1. confirmar repositorio;
2. confirmar branch;
3. confirmar HEAD;
4. revisar working tree;
5. verificar tracking/remoto cuando sea relevante;
6. leer `ESTADO-ACTUAL.md`;
7. leer el checkpoint específico;
8. leer los canónicos necesarios para la tarea;
9. ejecutar baseline si la intervención puede afectar comportamiento.

Regla:

```text
documentación esperada != estado real del repo
```

Una discrepancia obliga a detenerse y reconciliarla antes de continuar.

---

# 4. Alcance

Toda intervención debe declarar:

- objetivo;
- archivos/áreas permitidos;
- áreas explícitamente fuera de scope;
- base esperada;
- stop conditions.

No se debe:

- arreglar incidentalmente problemas ajenos;
- ampliar una migración por conveniencia;
- reinterpretar silenciosamente un diseño;
- tocar frontend/mobile desde una fase backend salvo autorización explícita.

---

# 5. Ejecución

Durante una intervención:

- trabajar sólo sobre la branch indicada;
- no mezclar trabajo de dos fases;
- preservar tests existentes;
- no imprimir secretos;
- no limpiar cambios ajenos;
- no usar `reset`, `clean`, force push o equivalentes destructivos sin autorización explícita;
- no ejecutar dos agentes modificadores simultáneamente sobre el mismo working tree.

Si Claude está implementando, Codex debe revisar después, no modificar a la vez.

---

# 6. Reporte de salida

Toda intervención técnica debe informar como mínimo:

```text
Branch
Base / HEAD inicial
Archivos tocados
Scope del diff
Tests ejecutados
Tests PASS/FAIL
Failures
Errors
Skipped
Build
Migraciones
Working tree
Commit si existe
Pendientes
Stop conditions encontradas
```

Una intervención de diseño debe especificar además:

```text
Checkpoint
Decisiones cerradas
Decisiones abiertas
Código productivo modificado: sí/no
Tests modificados: sí/no
```

---

# 7. Review

La implementación y el diseño se revisan por separado cuando el riesgo lo justifica.

Un review debe inspeccionar:

- código/diff real;
- diseño contractual;
- tests;
- concurrencia;
- seguridad;
- persistencia;
- compatibilidad;
- scope.

No debe aprobar sólo porque la suite esté verde.

Clasificación habitual:

- P0: problema crítico;
- P1: bloquea el cierre;
- P2: ajuste no fundamental, según contexto.

---

# 8. Gate

Una fase avanza sólo después del gate requerido.

En fases con review adversarial:

```text
P0 = 0
P1 = 0
```

es la condición habitual para cierre.

Un P2 puede:

- quedar como riesgo no bloqueante;
- requerir una subfase;
- bloquear si afecta el contrato concreto.

Debe decidirse explícitamente.

---

# 9. Estados de fase

Usar:

- `PLANIFICADA`
- `DISEÑANDO`
- `EN_REVIEW`
- `REQUIERE_AJUSTE`
- `DISEÑO_APROBADO`
- `IMPLEMENTANDO`
- `IMPLEMENTADA_EN_REVIEW`
- `CERRADA`

Para una intervención correctiva específica:

- `PREPARADA`
- `EN_EJECUCION`
- `COMPLETADA`
- `ABORTADA`

No confundir:

```text
Fase:
REQUIERE_AJUSTE

Intervención correctiva:
PREPARADA
```

con una fase ya corregida.

---

# 10. Commits y push

Reglas generales:

- no commit antes de las validaciones definidas;
- usar staging explícito;
- revisar `diff --check`;
- revisar scope;
- no `amend` salvo autorización;
- no force push;
- no merge automático a `main`;
- push después del gate cuando el flujo de fase así lo indique;
- conservar hashes completos de checkpoints/implementaciones importantes.

---

# 11. Actualización documental

## Durante un diseño no aprobado

Actualizar:

- checkpoint específico;
- `ESTADO-ACTUAL.md` sólo para reflejar el estado real `EN_REVIEW` o `REQUIERE_AJUSTE`.

No presentar el diseño como arquitectura materializada.

## Después de un gate de diseño

Puede registrarse la decisión como aceptada.

Su materialización sigue siendo `NO_INICIADA` hasta que exista código.

## Después de una implementación antes de review

Estado:

`IMPLEMENTADA_EN_REVIEW`

No:

`CERRADA`.

## Después del gate final

Actualizar sólo los canónicos afectados:

- `ESTADO-ACTUAL.md`;
- `ARQUITECTURA-ACTUAL.md`;
- `DECISIONES-ARQUITECTONICAS.md`;
- `MAPA-LEGACY-Y-MIGRACION.md`;
- `DOMINIO-FUNCIONAL.md`.

No reescribir todos los documentos por rutina.

---

# 12. Obsolescencia

Los documentos canónicos deben indicar cuando sea razonable:

```text
Last updated
Repository verification
Verification scope
```

Si se sabe que un documento ya no representa el repositorio:

```text
Status: STALE
```

hasta reconciliarlo.

No modificar código para hacerlo coincidir con documentación desactualizada.

---

# 13. Stop conditions

Detener una intervención cuando:

- branch o HEAD no coinciden;
- working tree contiene cambios no esperados;
- baseline falla antes de modificar;
- el diseño contradice el código real de forma material;
- aparece necesidad de migración fuera de scope;
- aparece necesidad de nueva dependencia no autorizada;
- una API consumida tendría que romperse;
- cerrar una carrera requiere cambiar el protocolo aprobado;
- se descubre un problema arquitectónico que exige reabrir diseño;
- se requiere tocar otro repositorio no autorizado;
- el agente no puede verificar una precondición crítica.

No improvisar arquitectura para evitar detenerse.

---

# 14. Corte de conversaciones

Cortar una conversación cuando:

- termina una fase grande;
- cambia el dominio de trabajo;
- se acumulan varias rondas diseño/review/fix;
- empieza a ser difícil distinguir propuestas de implementaciones;
- existe un punto estable de handoff.

Procedimiento:

```text
cerrar o congelar intervención
→ verificar estado
→ actualizar documentación aplicable
→ crear handoff
→ actualizar ESTADO-ACTUAL
→ iniciar conversación nueva
→ verificar repositorio antes de operar
```

El chat anterior queda como histórico no autoritativo.

---

# 15. Inicio de conversación nueva

Una conversación nueva debe recibir un bootstrap breve con:

- nombre del proyecto;
- rol del chat;
- rutas de documentación;
- orden de lectura;
- punto actual resumido;
- regla contra suposiciones;
- referencia a estas reglas de trabajo.

No debe recibir toda la cronología.

Antes de modificar código, el nuevo agente debe verificar el repositorio.
