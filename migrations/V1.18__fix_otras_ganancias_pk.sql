-- V1.18: Corrige colisión de PK en otras_ganancias
-- El trigger usaba MS (milisegundos, 3 dígitos); cuando ap_vigentes() anula
-- varios apartados en el mismo milisegundo se generaba el mismo id.
-- Se cambia a US (microsegundos, 6 dígitos) y se amplía la columna a VARCHAR(21).
BEGIN;

ALTER TABLE otras_ganancias
    ALTER COLUMN id_otras_ganancias TYPE VARCHAR(21);

CREATE OR REPLACE FUNCTION set_campos_og()
RETURNS TRIGGER AS $$
BEGIN
    NEW.id_otras_ganancias := 'O' || TO_CHAR(clock_timestamp(), 'YYYYMMDDHH24MISSUS');
    NEW.fecha_otras_ganancias := CURRENT_DATE;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

COMMIT;
