# FeelingPilates — Fase 2A.2: Integridad de ventana de reservas en políticas de salón

Fecha: 2026-09-20
Branch de trabajo: `AldairCruz7/integracion-backend-lane3-vigencia-politicas`
Commit base candidato: `4bfeb47d722ec7ff9d3b3a2b4c96a732124cce17`

## 1. Contexto y Hallazgo

Durante la auditoría independiente de Lane 3B, se detectó el siguiente hallazgo P1 sobre la migración pendiente `V42__salon_politicas_programacion.sql`:

- `V42` carecía de un constraint a nivel de base de datos que validara la relación entre `anticipacion_maxima_reserva_horas` y `anticipacion_minima_reserva_horas`.
- Existían checks univariados (`min >= 0`, `max IS NULL OR max > 0`), pero no una restricción cruzada, lo cual permitía persistir ventanas de reserva invertidas (ej. `min = 48`, `max = 24`).

## 2. Resolución de Autoridad

De acuerdo con el documento de diseño fundacional `auditoria/fase-1b-diseno-programacion.md`, Sección 13 ("Ventana de reservas"):

> "Una sesión es publicable si existe, no está cancelada, su `AsignacionFecha` está `CONFIRMADA` (o el salón no lo exige), y `ahora ∈ [inicio − antMax, inicio − antMin]`."

Para que el intervalo temporal `[inicio − antMax, inicio − antMin]` sea no vacío y matemáticamente coherente, se exige inequívocamente que `inicio − antMax <= inicio − antMin`, lo que equivale a `antMax >= antMin` siempre que `antMax` esté definido.

Clasificación de autoridad: **B. INVARIANT_IMPLIED_UNAMBIGUOUSLY_BY_ACCEPTED_DOMAIN_CONTRACT**.

## 3. Corrección en V42

Dado que `V42` aún no ha sido publicada a `main` ni desplegada a ambientes productivos, se añadió la restricción directamente en `V42__salon_politicas_programacion.sql`:

```sql
ALTER TABLE salon
    ADD CONSTRAINT chk_salon_anticipacion_reserva_coherente
        CHECK (anticipacion_maxima_reserva_horas IS NULL OR anticipacion_maxima_reserva_horas >= anticipacion_minima_reserva_horas);
```

Se preservaron intactos todos los constraints originales:
- `chk_salon_anticipacion_minima_no_negativa`
- `chk_salon_anticipacion_maxima_positiva`
- `chk_salon_plazo_respuesta_confirmacion_no_negativo`
- `chk_salon_margen_materializacion_no_negativo`
- `chk_salon_confirmacion_requiere_plazo`

## 4. Evidencia de Tests (PostgreSQL / Testcontainers)

En `src/test/java/com/feelingpilates/ubicaciones/UbicacionesPersistenciaTest.java` se incorporaron:

1. **Caso negativo**: `v42RechazaVentanaReservaInvertida` confirma que intentar actualizar `anticipacion_minima_reserva_horas = 48` y `anticipacion_maxima_reserva_horas = 24` es rechazado por PostgreSQL arrojando `DataIntegrityViolationException`.
2. **Casos positivos**: `v42PermiteVentanaReservaValidaYLimitesAbiertos` verifica:
   - `max > min` (`min = 24, max = 48`) es válido.
   - `max == min` (`min = 24, max = 24`) es válido.
   - `max NULL` con `min > 0` (`min = 12, max = null`) es válido.
   - Estado por defecto transicional (`min = 0, max = null`) se mantiene validado por `v42AgregaPoliticasDeSalonConValoresTransicionales`.
