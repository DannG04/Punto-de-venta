BEGIN;

-- Permite registrar más de un gerente.
--
-- Antes, la función verif_puesto() (trigger BEFORE INSERT OR UPDATE en empleado)
-- contaba los empleados con puesto='gerente' y lanzaba la excepción
-- 'Sólo puede existir un único Gerente' si ya había uno.
--
-- Esta versión elimina ese conteo/restricción y conserva el resto del
-- comportamiento original: para puestos distintos de gerente, en los INSERT
-- sigue llamando a reg_empleado() (que crea el rol de BD correspondiente).

CREATE OR REPLACE FUNCTION public.verif_puesto() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
    IF NEW.puesto <> 'gerente' THEN
        IF (TG_OP = 'INSERT') THEN
            PERFORM reg_empleado(NEW.usuario, NEW.contrasenia, NEW.puesto);
        END IF;
    END IF;
    RETURN NEW;
END;
$$;

COMMIT;
