# Parches de actualización — Punto de Venta

Procedimiento para actualizar usuarios que YA tienen la app + PostgreSQL + la BD.
No reinstala PostgreSQL ni restaura la BD: solo aplica migraciones nuevas y cambia el JAR.

## Cómo sacar un parche

1. Escribe tus migraciones nuevas en `migrations/` siguiendo la plantilla de
   `migrations/README.md` (idempotentes + auto-registro en `schema_version`).
   Numéralas consecutivas: `V1.14__...`, `V1.15__...`.
2. Compila el JAR:  `ant jar`
3. Empaqueta:  `powershell -ExecutionPolicy Bypass -File installer\update\empaquetar_update.ps1 -Version 1.14`
   → genera `installer\output\PuntoVenta-Update-1.14.zip`.
4. Envía el ZIP al usuario.

## Qué hace el usuario

1. Descomprime el ZIP.
2. Clic derecho en `aplicar_actualizacion.bat` → **Ejecutar como administrador**
   (o doble clic; pedirá permisos solo).
3. El parche respalda su BD, aplica las migraciones pendientes y reemplaza el JAR.
   Al terminar puede abrir la app normalmente.

## Seguridad y reversa

- Antes de tocar nada se crea `…\PuntoVenta\base_datos\backup_pre_<fecha>.tar`.
- El JAR anterior queda en `…\PuntoVenta\Proy_Ventas.jar.<fecha>.bak`.
- Si una migración falla, su transacción se revierte y el parche se detiene sin
  cambiar el JAR. Para restaurar la BD a su estado previo:
  ```
  pg_restore -U postgres -d punto_de_venta --clean "ruta\backup_pre_<fecha>.tar"
  ```
- El log queda en `…\PuntoVenta\actualizacion.log`.

## Limitaciones conocidas

- Si la contraseña de la BD contiene el carácter `!`, edita `db.properties` o
  ejecuta el SQL a mano (el parsing batch no lo soporta).
- El parche aplica las migraciones por orden de nombre de archivo. Numera las de
  un mismo release de forma consecutiva (1.14, 1.15, 1.16) y evita saltos raros.
