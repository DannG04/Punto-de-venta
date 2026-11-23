BEGIN;
ALTER TABLE otras_ganancias 
ALTER COLUMN id_otras_ganancias TYPE VARCHAR(18);

CREATE OR REPLACE FUNCTION set_campos_og()
RETURNS TRIGGER AS $$
BEGIN
    NEW.id_otras_ganancias := 'O' || TO_CHAR(clock_timestamp(), 'YYYYMMDDHH24MISSMS');
    NEW.fecha_otras_ganancias := CURRENT_DATE;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION verif_puesto()
RETURNS TRIGGER AS $$
DECLARE
    num INTEGER;
BEGIN
    IF NEW.puesto='gerente' THEN
        IF (TG_OP = 'INSERT') THEN
            SELECT COUNT(id_empleado) INTO num FROM empleado WHERE puesto='gerente';
        ELSIF (TG_OP = 'UPDATE') THEN
            SELECT COUNT(id_empleado) INTO num FROM empleado WHERE puesto='gerente' AND id_empleado!=OLD.id_empleado;
        END IF;
        IF num=0 THEN
            RETURN NEW;
        ELSE
            RAISE EXCEPTION 'Sólo puede existir un único Gerente';
            RETURN NULL;
        END IF;
    ELSE
        RETURN NEW;
    END IF;
END;
$$ LANGUAGE plpgsql;
COMMIT;