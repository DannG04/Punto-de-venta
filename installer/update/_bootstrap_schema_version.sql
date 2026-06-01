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
-- instalador v1.0 (migraciones V1.0 a V1.13). Así el parche nunca re-aplica
-- lo viejo: solo correrá las versiones que no estén aquí.
INSERT INTO schema_version (version, descripcion)
SELECT v, 'Baseline previo al sistema de versionado'
FROM (VALUES
    ('1.0'),('1.1'),('1.2'),('1.3'),('1.4'),('1.5'),
    ('1.6'),('1.7'),('1.10'),('1.11'),('1.12'),('1.13')
) AS t(v)
WHERE NOT EXISTS (SELECT 1 FROM schema_version);

COMMIT;
