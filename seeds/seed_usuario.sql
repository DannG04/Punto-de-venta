-- Insert rápido de un usuario saltándose el trigger de verificación
-- (trigger_verif_puesto, el que impide tener más de un gerente).
--
-- Datos de acceso:
--   usuario:     admin
--   contraseña:  admin
--   puesto:      gerente
--
-- Se desactiva el trigger solo durante el INSERT y se reactiva al final.
-- Requiere ser superusuario o dueño de la tabla (el usuario postgres lo es).

BEGIN;

ALTER TABLE empleado DISABLE TRIGGER trigger_verif_puesto;

INSERT INTO empleado (id_empleado, nombre, puesto, telefono, usuario, contrasenia, estatus)
VALUES ('GARC850315HDFRZNL5', 'Administrador', 'gerente', '5512345678', 'admin', 'admin', 'Activo')
ON CONFLICT (id_empleado) DO NOTHING;

ALTER TABLE empleado ENABLE TRIGGER trigger_verif_puesto;

COMMIT;
