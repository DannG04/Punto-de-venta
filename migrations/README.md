# Migraciones de Base de Datos

Este sistema usa migraciones versionadas para mantener la base de datos sincronizada entre todos los desarrolladores.

---

## Configuración inicial (solo la primera vez)

1. Instalar **PostgreSQL** y tener `pgAdmin` o acceso a `psql`
2. Restaurar el backup base desde este mismo directorio:

**Con pgAdmin:**
- Abrir pgAdmin → clic derecho en "Databases" → "Create" → "Database"
- Nombre: `punto_de_venta`
- Clic derecho en la base creada → "Restore..."
- Seleccionar el archivo `V1.0__baseline.tar`
- En "Format" seleccionar **Custom or tar**
- Clic en "Restore"

**Con psql (línea de comandos):**
```bash
createdb -U postgres punto_de_venta
pg_restore -U postgres -d punto_de_venta V1.0__baseline.tar
```

3. Verificar credenciales en `src/ConexionBD.java`:
```java
String url      = "jdbc:postgresql://localhost:5432/";
String nameBD   = "punto_de_venta";
String usuario  = "postgres";
String contra   = "root";
```

---

## Cómo aplicar migraciones

Cada vez que hagas `git pull`, revisa si hay archivos `.sql` nuevos en esta carpeta que no hayas ejecutado.

**Orden de ejecución:** sigue el número de versión de menor a mayor.

**Con pgAdmin:**
- Abrir pgAdmin → seleccionar la base `punto_de_venta`
- Menú "Tools" → "Query Tool"
- Abrir el archivo `.sql` correspondiente
- Ejecutar con F5

**Con psql:**
```bash
psql -U postgres -d punto_de_venta -f V1.1__nombre_migracion.sql
```

---

## Cómo crear una migración nueva

Cuando hagas un cambio en la base de datos, crea un archivo `.sql` en esta carpeta siguiendo esta convención:

```
V[version]__[descripcion_corta].sql
```

**Ejemplos:**
```
V1.1__agregar_tabla_categorias.sql
V1.2__agregar_tabla_proveedores.sql
V1.3__descuentos_en_ventas.sql
```

### Reglas para escribir el script

Siempre envuelve los cambios en una transacción para que si algo falla, no quede la BD a medias:

```sql
-- V1.1__agregar_tabla_categorias.sql
-- Descripcion: Agrega la tabla de categorias de productos
-- Autor: Tu nombre
-- Fecha: YYYY-MM-DD

BEGIN;

CREATE TABLE categoria (
    id_categoria SERIAL PRIMARY KEY,
    nombre       VARCHAR(100) NOT NULL,
    descripcion  VARCHAR(255),
    estatus      VARCHAR(10) DEFAULT 'Activo'
                 CHECK (estatus IN ('Activo', 'Inactivo'))
);

-- Si el script modifica una tabla existente, siempre usa IF NOT EXISTS
-- o verifica antes para no romper BDs que ya tengan el cambio:
ALTER TABLE producto
    ADD COLUMN IF NOT EXISTS id_categoria INTEGER
    REFERENCES categoria(id_categoria);

COMMIT;
```

---

## Historial de versiones

| Versión | Archivo | Descripción | Fecha |
|---------|---------|-------------|-------|
| 1.0  | `V1.0__baseline.tar`                   | Base de datos inicial del proyecto                              | 2026-02-17 |
| 1.1  | `V1.1__add_categoria.sql`              | Categorías de productos y columna en producto                   | 2026-03-08 |
| 1.2  | `V1.2__add_proveedor.sql`              | Catálogo de proveedores y columna en compras                    | 2026-03-21 |
| 1.3  | `V1.3__reporte_diario_fecha.sql`       | Reporte diario por fecha como función PostgreSQL                | 2026-03-25 |
| 1.4  | `V1.4__add_descuentos.sql`             | Descuentos por producto y en venta_temp / venta_detalle         | 2026-03-28 |
| 1.5  | `V1.5__fix_trigger_descuento.sql`      | Corrige trigger verif_exist_update para respetar descuento_pct  | 2026-03-28 |
| 1.6  | `V1.6__forma_pago.sql`                 | Forma de pago en venta y tabla forma_pago_venta                 | 2026-03-28 |
| 1.7  | `V1.7__kardex.sql`                     | Tabla kardex de movimientos de inventario                       | 2026-03-28 |
| 1.10 | `V1.10__lista_precios.sql`             | Listas de precios (menudeo/mayoreo) y precio por lista          | 2026-04-06 |
| 1.11 | `V1.11__empresa.sql`                   | Tabla empresa con datos del negocio para tickets                | 2026-04-20 |
| 1.12 | `V1.12__cotizaciones.sql`              | Módulo de cotizaciones con conversión a venta                   | 2026-04-21 |
| 1.13 | `V1.13__fix_precio_lista_override.sql` | Re-siembra listas de fábrica y ajusta override de precio_lista  | 2026-06-01 |

> **Cada desarrollador debe agregar su migración a esta tabla cuando la suba.**

---

## Control de versiones en producción (schema_version)

Las instalaciones de usuarios llevan una tabla `schema_version` que registra qué
migraciones se han aplicado. La crea y siembra `installer/update/_bootstrap_schema_version.sql`.

**Toda migración nueva (V1.14 en adelante) DEBE:**

1. Ser idempotente: usar `IF NOT EXISTS` (DDL) y `WHERE NOT EXISTS` / `ON CONFLICT`
   (DML) para poder re-ejecutarse sin romper nada.
2. Auto-registrarse al final, dentro de su propia transacción.

### Plantilla

```sql
-- V1.14__descripcion_corta.sql
-- Descripcion: ...
-- Autor: ...
-- Fecha: YYYY-MM-DD

BEGIN;

-- ... cambios idempotentes ...
ALTER TABLE producto ADD COLUMN IF NOT EXISTS ejemplo TEXT;

-- Registrar la versión (el parche también la registra, pero esto deja
-- constancia cuando la corres a mano con psql -f en desarrollo).
INSERT INTO schema_version (version, descripcion)
VALUES ('1.14', 'descripcion corta')
ON CONFLICT (version) DO NOTHING;

COMMIT;
```

> **Ordenamiento:** el parche aplica las migraciones por nombre de archivo ascendente.
> Numera las migraciones de un mismo release de forma consecutiva (1.14, 1.15, 1.16).

---

## Reglas del equipo

- **Nunca** modifiques un script que ya fue subido al repositorio y aplicado por otros
- **Siempre** crea un script nuevo con la siguiente versión
- **Siempre** prueba tu script en tu BD local antes de hacer commit
- Si tu migración depende de otra, indícalo en el comentario del archivo
