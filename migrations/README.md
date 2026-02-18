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
| 1.0 | `V1.0__baseline.tar` | Base de datos inicial del proyecto | 2026-02-17 |

> **Cada desarrollador debe agregar su migración a esta tabla cuando la suba.**

---

## Reglas del equipo

- **Nunca** modifiques un script que ya fue subido al repositorio y aplicado por otros
- **Siempre** crea un script nuevo con la siguiente versión
- **Siempre** prueba tu script en tu BD local antes de hacer commit
- Si tu migración depende de otra, indícalo en el comentario del archivo
