-- V24: retira exclusivamente las ocho filas semilla conocidas de V22_1.
--
-- Los UUID fueron generados por PostgreSQL y no son deterministas. La firma completa
-- de cada semilla si lo es. Se elige como maximo la coincidencia exacta mas antigua,
-- que es la creada por V22_1 antes de cualquier alta posterior. Si fue modificada o
-- ya tiene compras/composicion asociadas, se conserva para no perder datos de negocio.
WITH semillas (
    categoria,
    nombre,
    descripcion,
    precio_centavos,
    vigencia_dias,
    unitario_texto,
    destacado,
    orden
) AS (
    VALUES
        ('pilates', '4 clases',  '4 clases grupales de Reformer o Mat, a tu elección.', 90000,  30, '$225 c/u', false, 1),
        ('pilates', '8 clases',  '8 clases grupales de Reformer o Mat, a tu elección.', 160000, 45, '$200 c/u', true,  2),
        ('pilates', '12 clases', '12 clases grupales de Reformer o Mat, a tu elección.', 216000, 60, '$180 c/u', false, 3),
        ('bacu_fit', '4 rentas Bacu Fit',  '4 sesiones individuales en la máquina Bacu Fit.', 100000, 30, '$250 c/u', false, 1),
        ('bacu_fit', '8 rentas Bacu Fit',  '8 sesiones individuales en la máquina Bacu Fit.', 184000, 45, '$230 c/u', true,  2),
        ('bacu_fit', '12 rentas Bacu Fit', '12 sesiones individuales en la máquina Bacu Fit.', 252000, 60, '$210 c/u', false, 3),
        ('combo', 'Combo 4 + 4', '4 clases de Pilates + 4 rentas de Bacu Fit.', 175000, 30, 'Ahorras $150', false, 1),
        ('combo', 'Combo 8 + 8', '8 clases de Pilates + 8 rentas de Bacu Fit.', 320000, 45, 'Ahorras $240', true,  2)
)
DELETE FROM paquete AS p
USING semillas AS s
WHERE p.id = (
    SELECT candidato.id
    FROM paquete AS candidato
    WHERE candidato.categoria = s.categoria
      AND candidato.nombre = s.nombre
      AND candidato.descripcion = s.descripcion
      AND candidato.precio_centavos = s.precio_centavos
      AND candidato.vigencia_dias = s.vigencia_dias
      AND candidato.unitario_texto = s.unitario_texto
      AND candidato.destacado = s.destacado
      AND candidato.activo = true
      AND candidato.orden = s.orden
    ORDER BY candidato.creado_en, candidato.id
    LIMIT 1
)
AND NOT EXISTS (
    SELECT 1 FROM compra AS c WHERE c.paquete_id = p.id
)
AND NOT EXISTS (
    SELECT 1 FROM paquete_actividad AS pa WHERE pa.paquete_id = p.id
);
