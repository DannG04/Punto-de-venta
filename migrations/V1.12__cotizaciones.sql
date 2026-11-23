-- V1.12: Módulo de cotizaciones
-- 2026-04-21

BEGIN;

CREATE TABLE IF NOT EXISTS cotizacion (
    id_cotizacion       VARCHAR(15)  PRIMARY KEY,
    fecha_cotizacion    TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    id_empleado         CHAR(18)     REFERENCES empleado(id_empleado) ON UPDATE CASCADE ON DELETE CASCADE,
    id_cliente          CHAR(18)     REFERENCES cliente(id_cliente)   ON UPDATE CASCADE ON DELETE CASCADE,
    total_cotizacion    NUMERIC(10,2),
    estatus             VARCHAR(20)  DEFAULT 'Pendiente',  -- Pendiente | Convertida | Cancelada
    id_venta_convertida VARCHAR(15)  REFERENCES venta(id_venta)       ON UPDATE CASCADE ON DELETE SET NULL,
    fecha_vigencia      DATE,
    notas               TEXT
);

CREATE SEQUENCE IF NOT EXISTS cotizacion_seq START 1;

CREATE OR REPLACE FUNCTION gen_id_cotizacion() RETURNS TRIGGER AS $$
BEGIN
    NEW.id_cotizacion := 'COT-' || LPAD(nextval('cotizacion_seq')::TEXT, 10, '0');
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_trigger WHERE tgname = 'trg_gen_id_cotizacion') THEN
        CREATE TRIGGER trg_gen_id_cotizacion
            BEFORE INSERT ON cotizacion
            FOR EACH ROW EXECUTE FUNCTION gen_id_cotizacion();
    END IF;
END;
$$;

CREATE TABLE IF NOT EXISTS cotizacion_detalle (
    id_cotizacion_detalle SERIAL       PRIMARY KEY,
    id_cotizacion         VARCHAR(15)  REFERENCES cotizacion(id_cotizacion)   ON UPDATE CASCADE ON DELETE CASCADE,
    id_producto           VARCHAR(50)  REFERENCES producto(id_producto)        ON UPDATE CASCADE ON DELETE CASCADE,
    nombre_p              VARCHAR(50),
    cantidad              INTEGER,
    tipo_venta            VARCHAR(10),
    precio_dado           NUMERIC(10,2),
    descuento_pct         NUMERIC(5,2) DEFAULT 0,
    precio_total          NUMERIC(10,2)
);

CREATE TABLE IF NOT EXISTS cotizacion_temp (
    id_producto   VARCHAR(50) REFERENCES producto(id_producto) ON UPDATE CASCADE ON DELETE CASCADE,
    nombre_p      VARCHAR(50),
    cantidad_prod INTEGER,
    tipo_venta    VARCHAR(10),
    precio_dado   NUMERIC(10,2),
    precio_total  NUMERIC(10,2),
    descuento_pct NUMERIC(5,2) DEFAULT 0
);

COMMIT;
