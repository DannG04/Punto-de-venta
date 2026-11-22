BEGIN;

-- 1) Alta de empleados no-gerente fallaba SIEMPRE.
--    reg_empleado() comparaba el enum "puestoValido" (valores 'gerente','vendedor')
--    contra el literal 'Gerente' -> ERROR 22P02 invalid input value for enum.
--    Además hacía GRANT a los roles gerente/vendedor, que no existen en un
--    servidor PostgreSQL recién instalado (pg_restore no restaura roles), y
--    CREATE USER fallaba si el rol ya existía (reinstalación, recontratación).
--    La app siempre se conecta con el usuario de db.properties, así que estos
--    roles son solo informativos: se crean si faltan y nunca bloquean el alta.
CREATE OR REPLACE FUNCTION public.reg_empleado(nomb character varying, passwor character varying, puest "puestoValido")
    RETURNS void
    LANGUAGE plpgsql
AS $$
DECLARE
    grupo text := CASE WHEN puest = 'gerente' THEN 'gerente' ELSE 'vendedor' END;
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = grupo) THEN
        EXECUTE format('CREATE ROLE %I NOLOGIN', grupo);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = nomb) THEN
        EXECUTE format('CREATE USER %I WITH PASSWORD %L', nomb, passwor);
    END IF;
    EXECUTE format('GRANT %I TO %I', grupo, nomb);
EXCEPTION WHEN insufficient_privilege THEN
    -- Si el usuario de la app no puede crear roles, el alta del empleado sigue.
    RAISE NOTICE 'No se pudo crear el rol de BD para %: %', nomb, SQLERRM;
END;
$$;

-- 2) IDs por timestamp al segundo chocaban (venta + entrega de apartado o dos
--    cajas en el mismo segundo -> duplicate key venta_pkey).
--    Mismo formato (prefijo + YYYYMMDDHH24MISS, cabe en VARCHAR(15)); si el ID
--    ya existe se espera al siguiente segundo. El advisory lock serializa la
--    generación por tabla para que dos transacciones no tomen el mismo ID.
--    Los procedimientos recuperan el ID por valor centinela (-1 / -111), no
--    recalculando el timestamp, así que no les afecta.
CREATE OR REPLACE FUNCTION public.generar_id_unico(prefijo text, tabla text, columna text)
    RETURNS varchar
    LANGUAGE plpgsql
AS $$
DECLARE
    candidato varchar;
    existe boolean;
BEGIN
    PERFORM pg_advisory_xact_lock(hashtext('generar_id_unico:' || tabla));
    LOOP
        candidato := prefijo || TO_CHAR(clock_timestamp(), 'YYYYMMDDHH24MISS');
        EXECUTE format('SELECT EXISTS (SELECT 1 FROM %I WHERE %I = $1)', tabla, columna)
            INTO existe USING candidato;
        EXIT WHEN NOT existe;
        PERFORM pg_sleep(0.2);
    END LOOP;
    RETURN candidato;
END;
$$;

CREATE OR REPLACE FUNCTION public.set_campos_venta() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
    NEW.id_venta := generar_id_unico('V', 'venta', 'id_venta');
    NEW.fecha_venta := CURRENT_DATE;
    RETURN NEW;
END;
$$;

CREATE OR REPLACE FUNCTION public.set_campos_ap() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
    NEW.id_apartado := generar_id_unico('A', 'apartado', 'id_apartado');
    NEW.fecha_inicio := CURRENT_DATE;
    NEW.fecha_limite := NEW.fecha_inicio + INTERVAL '30 days';
    NEW.estado := 'Vigente';
    RETURN NEW;
END;
$$;

CREATE OR REPLACE FUNCTION public.set_campos_compras() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
    NEW.id_compra := generar_id_unico('C', 'compras', 'id_compra');
    NEW.fecha_compra := CURRENT_DATE;
    RETURN NEW;
END;
$$;

CREATE OR REPLACE FUNCTION public.set_campos_devv() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
    NEW.id_devolucion := generar_id_unico('D', 'devolucion_ventas', 'id_devolucion');
    NEW.fecha_devolucion := CURRENT_DATE;
    RETURN NEW;
END;
$$;

CREATE OR REPLACE FUNCTION public.set_campos_gstos() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
    NEW.id_gasto := generar_id_unico('G', 'gastos', 'id_gasto');
    NEW.fecha_gasto := CURRENT_DATE;
    RETURN NEW;
END;
$$;

COMMIT;
