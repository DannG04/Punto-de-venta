-- _bootstrap_schema_version.sql
-- Crea la tabla de control de versiones e inicializa el historial existente.
-- Idempotente: seguro de ejecutar varias veces.

BEGIN;

CREATE TABLE IF NOT EXISTS schema_version (
    version     VARCHAR(20) PRIMARY KEY,
    descripcion VARCHAR(255),
    aplicada_en TIMESTAMP NOT NULL DEFAULT now()
);

-- Si la tabla está vacía, sembramos el historial que YA traía la BD del
-- instalador v1.0 (migraciones V1.0 a V1.12). Así el parche nunca re-aplica
-- lo viejo: solo correrá las versiones que no estén aquí.
--
-- IMPORTANTE: NO incluir aquí '1.13' (ni superiores). Las migraciones
-- V1.13/V1.14/V1.15 fueron renumeradas justo antes de este parche
-- (V1.13_apartados -> V1.14_apartados, V1.14__compras_factura ->
-- V1.15__compras_factura) y se liberó "1.13" para una migración nueva
-- (fix_precio_lista_override) que NINGÚN cliente existente ha aplicado.
-- Si "1.13" se sembrara aquí como baseline, el parche la saltaría y esa
-- migración nunca llegaría a los clientes. Dejar que 1.13/1.14/1.15 corran
-- de nuevas: 1.14 y 1.15 son idempotentes (IF NOT EXISTS / CREATE OR REPLACE),
-- así que en clientes que ya tenían ese contenido bajo el nombre viejo
-- simplemente no hacen nada.
INSERT INTO schema_version (version, descripcion)
SELECT v, 'Baseline previo al sistema de versionado'
FROM (VALUES
    ('1.0'),('1.1'),('1.2'),('1.3'),('1.4'),('1.5'),
    ('1.6'),('1.7'),('1.10'),('1.11'),('1.12')
) AS t(v)
WHERE NOT EXISTS (SELECT 1 FROM schema_version);

COMMIT;
