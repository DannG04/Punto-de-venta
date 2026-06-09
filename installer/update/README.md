# Parches de actualización — Punto de Venta

Procedimiento para actualizar usuarios que YA tienen la app + PostgreSQL + la BD.
No reinstala PostgreSQL ni restaura la BD: solo aplica migraciones nuevas y cambia el JAR.

## Cómo sacar un parche

1. Escribe tus migraciones nuevas en `migrations/` siguiendo la plantilla de
   `migrations/README.md` (idempotentes + auto-registro en `schema_version`).
   Numéralas consecutivas: `V1.14__...`, `V1.15__...`.
2. Compila el JAR:  `ant jar`
3. Empaqueta:  `powershell -ExecutionPolicy Bypass -File installer\update\empaquetar_update.ps1 -Version 1.15`
   → genera `installer\output\PuntoVenta-Update-1.15.zip`.
4. Envía el ZIP al usuario.

---

## Proceso detallado para aplicar el parche en una máquina

### Antes de empezar (checklist)

- [ ] La máquina tiene la app instalada (existe `db.properties` en la carpeta de
      instalación, normalmente `C:\Program Files\PuntoVenta\`).
- [ ] El servicio de PostgreSQL está corriendo (el parche necesita conectarse para
      respaldar la BD y aplicar migraciones).
- [ ] Hay espacio en disco para un respaldo extra de la BD (el `.tar` puede pesar
      desde unos MB hasta varios cientos de MB según el negocio).
- [ ] Nadie está usando la app en ese momento — **el parche la cierra a la fuerza**
      con `taskkill`, sin avisar.
- [ ] Sabes en qué versión está el cliente, para confirmar al final que el parche
      avanzó lo esperado.

### Paso a paso — qué hace `aplicar_actualizacion.bat`

1. **Descomprime el ZIP** en cualquier carpeta (USB, escritorio, descargas...). El
   script no necesita estar dentro de la instalación.
2. **Ejecuta `aplicar_actualizacion.bat`** (doble clic). Se relanza solo pidiendo
   permisos de Administrador (UAC) — acepta el aviso.
3. En la consola vas a ver, en este orden:
   1. **Localiza la instalación**: busca primero en el registro de Windows (clave
      de Inno Setup, vista nativa y WOW6432Node) y, si no la encuentra, usa
      `C:\Program Files\PuntoVenta`. Si ahí no hay `db.properties`, se detiene con
      `[ERROR] No se encontro la instalacion`.
   2. **Lee `db.properties`**: host, puerto, nombre de BD, usuario y contraseña.
   3. **Busca PostgreSQL** (`psql.exe`) probando las versiones 18 → 14 en
      `Program Files\PostgreSQL\<v>\bin`. Si no aparece, se detiene.
   4. **Crea el respaldo de seguridad**: `pg_dump -F t` hacia
      `<INSTALL_DIR>\base_datos\backup_pre_<fecha_hora>.tar`.
      **Si el respaldo falla, el script se detiene aquí mismo sin tocar nada**
      (ni BD ni JAR) — es la primera red de seguridad.
   5. **Cierra la app** (`taskkill /IM PuntoVenta.exe /T /F`).
   6. **Inicializa `schema_version`**: corre `_bootstrap_schema_version.sql`
      (crea la tabla si no existe y, *solo si está vacía*, la siembra con el
      historial base 1.0–1.12 — ver nota de renumeración más abajo).
   7. **Aplica migraciones pendientes**: recorre `migrations\V*.sql` en orden
      alfabético, compara cada número de versión contra `schema_version` y:
      - `[SKIP] V<x> ya estaba aplicada` → no hace nada.
      - `[..] Aplicando V<x> ...` → la corre con `psql -v ON_ERROR_STOP=1`.
        Si falla: **esa transacción se revierte sola, el script se detiene, el
        JAR NO se reemplaza**, y te dice dónde quedó el respaldo para restaurar
        a mano.
      - Si tiene éxito, la registra en `schema_version` (`INSERT ... ON CONFLICT
        DO NOTHING`) y sigue con la siguiente.
   8. **Reemplaza el JAR**: guarda el actual como
      `Proy_Ventas.jar.<fecha_hora>.bak` y copia el nuevo. Si el ZIP trae
      carpeta `lib\`, también la copia encima de la existente.
4. Al terminar muestra `Actualizacion completada con exito` y se queda en pausa
   (`pause`) esperando una tecla. Ya se puede abrir la app normalmente.

### Verificación después de actualizar

- Abre la app y confirma que inicia sin errores y que lo nuevo del release está
  ahí (p. ej. los campos agregados en Compras por V1.15).
- Revisa `<INSTALL_DIR>\actualizacion.log`: debe tener líneas
  `Aplicada V1.13`, `Aplicada V1.14`, `Aplicada V1.15` (o el `[SKIP]` correspondiente
  si el cliente ya traía ese contenido con el nombre viejo) y cerrar con
  `Actualizacion OK`.
- Si quieres confirmarlo a nivel BD:
  ```
  psql -U postgres -d punto_de_venta -c "SELECT * FROM schema_version ORDER BY version;"
  ```
  Debe listar 1.13, 1.14 y 1.15 entre las versiones registradas.

---

## Seguridad y reversa

- Antes de tocar nada se crea `…\PuntoVenta\base_datos\backup_pre_<fecha>.tar`.
- El JAR anterior queda en `…\PuntoVenta\Proy_Ventas.jar.<fecha>.bak`.
- Si una migración falla, su transacción se revierte y el parche se detiene sin
  cambiar el JAR. Para restaurar la BD a su estado previo:
  ```
  pg_restore -U postgres -d punto_de_venta --clean "ruta\backup_pre_<fecha>.tar"
  ```
- El log queda en `…\PuntoVenta\actualizacion.log`.

## Solución de problemas

| Síntoma | Causa probable | Qué hacer |
|---|---|---|
| `No se encontro la instalacion en: ...` | La app no está en `Program Files\PuntoVenta` ni registrada por Inno Setup | Reejecuta pasando la ruta real como argumento: `aplicar_actualizacion.bat "C:\ruta\real"` |
| `No se encontro PostgreSQL (psql.exe)` | PostgreSQL no instalado, en ruta no estándar, o versión fuera del rango 14–18 | `installer\scripts\check_postgres.bat` y `diagnostico_postgres.bat` para diagnosticar |
| `No se pudo crear el respaldo` | El servicio de PostgreSQL no está corriendo o `db.properties` tiene credenciales viejas | Arranca el servicio con `installer\scripts\iniciar_postgres.bat` y revisa `db.properties` |
| `Fallo la migracion V<x>` | Error de sintaxis SQL, o datos del cliente que no encajan con lo que la migración asume | Lee el mensaje de `psql` en consola; si hace falta, restaura `backup_pre_<fecha>.tar`, corrige la migración y reempaqueta |
| El script termina pero la app no abre | JAR corrupto en el ZIP, o `lib\` desactualizado | Compara tamaños/fechas contra `Proy_Ventas.jar.<fecha>.bak`; si hace falta, copia el `.bak` de vuelta |

## Limitaciones conocidas

- Si la contraseña de la BD contiene el carácter `!`, edita `db.properties` o
  ejecuta el SQL a mano (el parsing batch no lo soporta).
- El parche aplica las migraciones por orden de nombre de archivo. Numera las de
  un mismo release de forma consecutiva (1.14, 1.15, 1.16) y evita saltos raros.
- **Cuidado al renumerar migraciones ya distribuidas** (como pasó en este release:
  `V1.13_apartados` → `V1.14_apartados` y `V1.14__compras_factura` →
  `V1.15__compras_factura` para liberar el "1.13" para una migración nueva).
  El baseline sembrado en `_bootstrap_schema_version.sql` usa solo el número de
  versión como identificador — si ese número queda marcado como "ya aplicado"
  pero ahora corresponde a contenido distinto al que los clientes ya tienen, esa
  migración nueva se saltará sin aplicarse jamás. Por eso el baseline de este
  parche solo llega hasta `1.12`: deja que 1.13/1.14/1.15 corran de nuevas en
  todos los clientes (1.14 y 1.15 son idempotentes, así que en quien ya tenía
  ese contenido con el nombre viejo simplemente no hacen nada).
