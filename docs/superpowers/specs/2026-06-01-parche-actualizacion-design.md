# Diseño: Parche de actualización para usuarios existentes

**Fecha:** 2026-06-01
**Autor:** Daniel Gonzalez Ruiz
**Estado:** Aprobado — pendiente de plan de implementación

## Contexto

El instalador v1.0 (`installer/setup.iss`, Inno Setup) ya fue repartido a usuarios. Cada
instalación incluye:

- JRE embebido + `Proy_Ventas.jar` envuelto en `PuntoVenta.exe` (launch4j).
- PostgreSQL 18 (solo si no había uno) con la BD `punto_de_venta` restaurada desde
  `installer/base_datos/backup.tar`.
- `db.properties` generado en tiempo de instalación con las credenciales de conexión.

**Punto de partida confirmado:** la BD que repartió el instalador v1.0 ya trae **todas las
migraciones hasta V1.13 aplicadas**. Por tanto todos los usuarios actuales están en el
esquema V1.13. Lo que viene son fixes nuevos (migraciones V1.14+) más un JAR nuevo.

**Problema:** el `setup.iss` actual siempre hace `CREATE DATABASE` + `pg_restore`. Eso
**no** se puede usar para actualizar a un usuario con datos reales: les pisaría la BD.
Además no existe una tabla de versión de esquema, así que hoy no hay forma de saber
qué migraciones tiene aplicadas una instalación dada.

## Objetivo

Entregar un paquete de actualización autónomo (ZIP que el usuario ejecuta con doble clic /
"Ejecutar como administrador") que, contra la instalación y la BD existentes:

1. Aplique solo las migraciones SQL pendientes, de forma segura y re-ejecutable.
2. Reemplace el `Proy_Ventas.jar` (y dependencias en `lib\` si cambiaron).
3. Nunca destruya datos del usuario.

No-objetivos (YAGNI): auto-actualización desde la app, servidor de actualizaciones,
parches diferenciales binarios.

## Enfoque elegido

Opción A: ZIP con un `.bat` orquestador genérico + tabla `schema_version` para control
de versiones. Reusa el know-how de los `.bat` existentes, no toca el código Java, y es
trivial de regenerar por release (solo cambia el contenido de `migrations\` y el `.jar`).

## Estructura del paquete

```
PuntoVenta-Update-<version>.zip
├── aplicar_actualizacion.bat      ← el usuario ejecuta ESTE (clic derecho → admin)
├── Proy_Ventas.jar                ← el JAR nuevo compilado
├── lib\                           ← (opcional) solo .jar de dependencias que cambiaron
└── migrations\
    ├── V1.14__<fix1>.sql
    └── V1.15__<fix2>.sql          ← migraciones nuevas de esta tanda
```

El `.bat` es genérico y estable: aplica todos los `.sql` de `migrations\` que aún no estén
registrados y copia el `.jar`. Para el siguiente parche solo se cambia el contenido de
`migrations\` y el `.jar`.

## Tabla `schema_version`

Bootstrap idempotente que corre el `.bat` antes de las migraciones:

```sql
CREATE TABLE IF NOT EXISTS schema_version (
    version     VARCHAR(20) PRIMARY KEY,
    descripcion VARCHAR(255),
    aplicada_en TIMESTAMP NOT NULL DEFAULT now()
);

-- Usuarios actuales están en V1.13 pero no tienen la tabla.
-- Si está vacía, sembramos el baseline para NO re-aplicar lo viejo.
INSERT INTO schema_version (version, descripcion)
SELECT '1.13', 'Baseline previo al sistema de versionado'
WHERE NOT EXISTS (SELECT 1 FROM schema_version);
```

Cada migración nueva se auto-registra al final, dentro de su propia transacción:

```sql
-- V1.14__fix_xxx.sql
BEGIN;
  -- ... cambios idempotentes (IF NOT EXISTS / WHERE NOT EXISTS) ...
  INSERT INTO schema_version (version, descripcion)
  VALUES ('1.14', 'fix xxx') ON CONFLICT (version) DO NOTHING;
COMMIT;
```

El `.bat` consulta `schema_version` antes de cada archivo y salta los ya aplicados.

## Flujo del `.bat` (`aplicar_actualizacion.bat`)

1. **Auto-elevación:** si no corre como admin, se relanza con `runas` (Program Files está
   protegido).
2. **Localiza la instalación:** lee `InstallLocation` de la clave de desinstalación de Inno
   Setup en el registro (AppId `{A1B2C3D4-E5F6-7890-ABCD-EF1234567890}_is1`). Fallback:
   `%ProgramFiles%\PuntoVenta`.
3. **Lee `db.properties`** de esa carpeta: `db.host`, `db.port`, `db.name`, `db.user`,
   `db.password`.
4. **Encuentra `psql.exe` / `pg_dump.exe`:** escanea `PostgreSQL\{18,17,16,15,14}\bin`
   (misma lógica que el instalador).
5. **Respaldo de seguridad:** `pg_dump -F t` de la BD a
   `<install>\base_datos\backup_pre_<version>_<fecha>.tar`. Si el dump falla, **aborta**
   sin tocar nada más.
6. **Cierra la app:** `taskkill /IM PuntoVenta.exe /T /F` (cierra el .exe y su `javaw` hijo).
7. **Migraciones:** corre el bootstrap de `schema_version`; recorre `migrations\*.sql` en
   orden de versión, salta las ya registradas, ejecuta las pendientes con
   `psql -v ON_ERROR_STOP=1`. Si una falla, **aborta y avisa** (su transacción revierte).
8. **Reemplaza el JAR:** respalda el viejo a `Proy_Ventas.jar.bak` y copia el nuevo. Igual
   con `lib\` si se incluyeron dependencias.
9. **Log + resumen:** en pantalla y en `<install>\actualizacion_<version>.log`.

## Manejo de errores y seguridad

- `psql -v ON_ERROR_STOP=1`: cualquier error SQL detiene el parche; cada migración es
  atómica por su `BEGIN/COMMIT`, así que no queda la BD a medias.
- **Orden:** backup → migraciones → swap del JAR. Si las migraciones fallan, el JAR viejo
  sigue intacto y la app sigue operando con la BD anterior.
- **Idempotencia doble:** `schema_version` evita re-aplicar y las migraciones se escriben
  con `IF NOT EXISTS` como cinturón y tirantes.
- **Credenciales:** se leen de `db.properties`; `PGPASSWORD` se setea solo en el entorno
  local del `.bat`, nunca se persiste ni se imprime en el log.

## Cambios de una sola vez (fuera del parche)

- En `installer/setup.iss`, tras el `pg_restore`, agregar un `[Run]` que ejecute el
  bootstrap de `schema_version` para que las instalaciones nuevas nazcan con la tabla
  sembrada en la versión correcta.
- Subir `MyAppVersion` a la versión del release (p. ej. `1.14`) para mantener consistencia.

## Componentes y responsabilidades

| Componente | Qué hace | Depende de |
|---|---|---|
| `aplicar_actualizacion.bat` | Orquesta: localiza install, lee config, respalda, migra, swap JAR | registro, `db.properties`, psql/pg_dump |
| `_bootstrap_schema_version.sql` (o embebido en el .bat) | Crea y siembra `schema_version` | — |
| `migrations\V*.sql` | Cambios de esquema/datos + auto-registro de versión | `schema_version` |
| Ajuste a `setup.iss` | Siembra `schema_version` en instalaciones nuevas | — |

## Criterios de éxito

- Correr el parche en una instalación V1.13 con datos reales aplica las migraciones nuevas,
  conserva todos los datos y deja la app corriendo con el JAR nuevo.
- Correr el mismo parche dos veces no produce cambios ni errores la segunda vez.
- Si las migraciones fallan, la BD y el JAR quedan en el estado previo (sin corrupción).
- Existe un respaldo `backup_pre_<version>_<fecha>.tar` tras cada corrida.
