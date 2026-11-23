BEGIN;

CREATE TABLE empresa (
    id             INTEGER PRIMARY KEY DEFAULT 1,
    nombre         VARCHAR(100)  NOT NULL DEFAULT 'Mi Empresa',
    razon_social   VARCHAR(150),
    rfc            VARCHAR(13),
    telefono       VARCHAR(20),
    correo         VARCHAR(100),
    direccion      VARCHAR(200),
    ciudad         VARCHAR(100),
    estado         VARCHAR(100),
    cp             VARCHAR(10),
    mensaje_ticket VARCHAR(200)  DEFAULT 'Muchas gracias por su compra.',
    logo_ruta      VARCHAR(255),
    CHECK (id = 1)
);

INSERT INTO empresa DEFAULT VALUES;

COMMIT;
