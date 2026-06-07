# Registro de factura de compra desde Compras — Plan de Implementación

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Permitir registrar una factura de compra completa desde el módulo Compras (encabezado fiscal, productos renglón por renglón con alta/vínculo, código de barras, IVA, conversión de unidades) dejando inventario y kardex correctos.

**Architecture:** Se extiende el flujo de dos pasos existente de `ComprasP` (Opción A). La BD gana columnas en `proveedor`, `producto`, `compras`, `compra_producto` y un `reg_compra_prod` con factor de conversión (migración `V1.14`). `ConexionBD` gana métodos para el encabezado, alta/vínculo de productos y resolución de código. La conversión a unidad de venta y el registro en kardex ocurren **en vivo** por renglón vía el SP (igual que hoy).

**Tech Stack:** Java (Swing, JDBC), PostgreSQL (stored procedures + triggers), NetBeans/Ant.

---

## Convenciones de verificación para este proyecto

Este proyecto **no tiene pruebas automatizadas** y la GUI es generada por NetBeans. Por instrucción del usuario, **la compilación la ejecuta el usuario** (no correr `ant compile`/`ant run`). Por tanto, en cada tarea:

- **"Compilar":** el ejecutor pide al usuario compilar (`ant compile`) y confirmar que no hay errores antes de continuar.
- **"Verificar SQL":** se corre la consulta indicada con `psql -U postgres -d punto_de_venta` y se compara contra el resultado esperado.
- **"Verificar UI":** pasos manuales concretos dentro de la app (`ant run` por el usuario) con el resultado esperado.
- Los componentes visuales nuevos se agregan en el **editor de formularios de NetBeans** (cada tarea de UI lista los componentes, sus propiedades y a qué handler se conectan). El **código de los handlers y de la lógica va completo** en el plan.

Base de datos de pruebas: usar la factura de ejemplo **Nota de Remisión P-99843** (Subtotal $9,331.44 / IVA $1,493.03 / Total $10,824.47).

---

## Mapa de archivos

| Archivo | Responsabilidad | Acción |
|---------|-----------------|--------|
| `migrations/V1.14__compras_factura.sql` | Esquema nuevo + `reg_compra_prod`/`elim_compra_prod` con factor | Crear |
| `src/ConexionBD.java` | Métodos de proveedor (RFC), encabezado de compra, alta/vínculo de producto, resolución de código, kardex | Modificar |
| `src/ProveedoresP.java` | Campo RFC en alta/edición de proveedor | Modificar |
| `src/ComprasP.java` | Paso 1 (encabezado) y Paso 2 (renglones, totales, cuadre, guardar) | Modificar |
| `src/InventarioP.java` | Exponer campos nuevos del producto en alta/edición | Modificar |
| `src/VentasP.java` | Resolver escaneo por `id_producto` o `codigo_barras` | Modificar |

---

## Task 1: Migración de base de datos `V1.14`

**Files:**
- Create: `migrations/V1.14__compras_factura.sql`

- [ ] **Step 1: Escribir la migración completa**

Crear `migrations/V1.14__compras_factura.sql` con este contenido exacto:

```sql
-- Migración: Registro de factura de compra completa
-- Fecha: 2026-06-07
BEGIN;

-- 1. proveedor: RFC del emisor (domicilio fiscal usa la columna `direccion` existente)
ALTER TABLE proveedor ADD COLUMN IF NOT EXISTS rfc VARCHAR(13);

-- 2. producto: código de barras, costo, IVA, unidades y factor de conversión
ALTER TABLE producto ADD COLUMN IF NOT EXISTS codigo_barras     VARCHAR(20);
ALTER TABLE producto ADD COLUMN IF NOT EXISTS precio_compra     NUMERIC(10,2);
ALTER TABLE producto ADD COLUMN IF NOT EXISTS lleva_iva         BOOLEAN DEFAULT false;
ALTER TABLE producto ADD COLUMN IF NOT EXISTS unidad_compra     VARCHAR(20);
ALTER TABLE producto ADD COLUMN IF NOT EXISTS unidad_venta      VARCHAR(20);
ALTER TABLE producto ADD COLUMN IF NOT EXISTS factor_conversion NUMERIC(10,3) DEFAULT 1;
ALTER TABLE producto ALTER COLUMN nombre TYPE VARCHAR(100);
CREATE INDEX IF NOT EXISTS idx_producto_codigo_barras ON producto(codigo_barras);

-- 3. compras: encabezado fiscal de la factura
ALTER TABLE compras ADD COLUMN IF NOT EXISTS folio_proveedor VARCHAR(30);
ALTER TABLE compras ADD COLUMN IF NOT EXISTS fecha_factura   DATE;
ALTER TABLE compras ADD COLUMN IF NOT EXISTS origen          VARCHAR(100);
ALTER TABLE compras ADD COLUMN IF NOT EXISTS subtotal        NUMERIC(10,2);
ALTER TABLE compras ADD COLUMN IF NOT EXISTS iva             NUMERIC(10,2);
-- `monto` existente = gran total (subtotal + iva)

CREATE UNIQUE INDEX IF NOT EXISTS uq_compra_folio_prov
  ON compras (id_proveedor, folio_proveedor)
  WHERE id_proveedor IS NOT NULL AND folio_proveedor IS NOT NULL;

-- 4. compra_producto: snapshot de cómo se compró el renglón
ALTER TABLE compra_producto ADD COLUMN IF NOT EXISTS unidad_compra     VARCHAR(20);
ALTER TABLE compra_producto ADD COLUMN IF NOT EXISTS factor_conversion NUMERIC(10,3);
ALTER TABLE compra_producto ADD COLUMN IF NOT EXISTS lleva_iva         BOOLEAN;

-- 5. reg_compra_prod con factor de conversión.
--    cant/prec vienen en UNIDAD DE COMPRA; el inventario se sube en UNIDAD DE VENTA (cant*factor).
CREATE OR REPLACE FUNCTION reg_compra_prod(
    idecom  VARCHAR,
    idepro  id_producto_dominio,
    prec    NUMERIC,
    cant    INTEGER,
    factor  NUMERIC,
    acum    BOOLEAN
) RETURNS void AS $$
DECLARE
    cant_ant   INTEGER;
    factor_ant NUMERIC;
    fac        NUMERIC := COALESCE(NULLIF(factor,0), 1);
BEGIN
    SELECT cantidad, COALESCE(factor_conversion,1)
      INTO cant_ant, factor_ant
      FROM compra_producto
     WHERE id_compra = idecom AND id_producto = idepro;

    IF NOT FOUND THEN
        INSERT INTO compra_producto(
            id_compra, id_producto, cantidad, precio_adquirido, precio_total,
            unidad_compra, factor_conversion, lleva_iva)
        VALUES (idecom, idepro, cant, prec, prec*cant,
            (SELECT unidad_compra FROM producto WHERE id_producto=idepro),
            fac,
            (SELECT COALESCE(lleva_iva,false) FROM producto WHERE id_producto=idepro));
        UPDATE producto
           SET cantidad = cantidad + (cant * fac),
               precio_compra = prec / fac
         WHERE id_producto = idepro;
    ELSIF acum THEN
        UPDATE compra_producto
           SET cantidad = cant_ant + cant,
               precio_adquirido = prec,
               precio_total = prec*(cant_ant + cant),
               factor_conversion = fac
         WHERE id_compra = idecom AND id_producto = idepro;
        UPDATE producto
           SET cantidad = cantidad + (cant * fac),
               precio_compra = prec / fac
         WHERE id_producto = idepro;
    ELSE
        UPDATE compra_producto
           SET cantidad = cant,
               precio_adquirido = prec,
               precio_total = prec*cant,
               factor_conversion = fac
         WHERE id_compra = idecom AND id_producto = idepro;
        UPDATE producto
           SET cantidad = cantidad - (cant_ant * factor_ant) + (cant * fac),
               precio_compra = prec / fac
         WHERE id_producto = idepro;
    END IF;
END;
$$ LANGUAGE plpgsql;

-- 6. elim_compra_prod: revierte stock convertido y limpia el kardex reciente del renglón
CREATE OR REPLACE FUNCTION elim_compra_prod(
    idecom VARCHAR,
    idepro id_producto_dominio
) RETURNS void AS $$
DECLARE
    cant_ant   INTEGER;
    factor_ant NUMERIC;
BEGIN
    SELECT cantidad, COALESCE(factor_conversion,1)
      INTO cant_ant, factor_ant
      FROM compra_producto
     WHERE id_compra = idecom AND id_producto = idepro;
    IF FOUND THEN
        DELETE FROM compra_producto
         WHERE id_compra = idecom AND id_producto = idepro;
        UPDATE producto
           SET cantidad = cantidad - (cant_ant * factor_ant)
         WHERE id_producto = idepro;
        DELETE FROM kardex
         WHERE id_producto = idepro
           AND referencia = idecom
           AND tipo_movimiento = 'Compra';
    END IF;
END;
$$ LANGUAGE plpgsql;

COMMIT;
```

- [ ] **Step 2: Aplicar la migración**

Pedir al usuario ejecutar:
```
psql -U postgres -d punto_de_venta -f migrations/V1.14__compras_factura.sql
```
Esperado: `COMMIT` sin errores.

- [ ] **Step 3: Verificar el esquema**

Run:
```
psql -U postgres -d punto_de_venta -c "\d producto" -c "\d compras" -c "\d compra_producto" -c "\d proveedor"
```
Esperado: aparecen `producto.codigo_barras, precio_compra, lleva_iva, unidad_compra, unidad_venta, factor_conversion`; `nombre` es `character varying(100)`; `compras.folio_proveedor, fecha_factura, origen, subtotal, iva`; `compra_producto.unidad_compra, factor_conversion, lleva_iva`; `proveedor.rfc`.

- [ ] **Step 4: Verificar la conversión del SP con un producto de prueba**

Run:
```
psql -U postgres -d punto_de_venta -c "INSERT INTO producto(id_producto,nombre,cantidad,precio_mayoreo,precio_menudeo,max_descuento,unidad_compra,unidad_venta,factor_conversion,lleva_iva) VALUES ('TST001','Prueba',0,1,1,0,'Caja','Pieza',60,false);"
psql -U postgres -d punto_de_venta -c "INSERT INTO compras(id_empleado,descripcion,monto) SELECT id_empleado,'tmp',0 FROM empleado LIMIT 1 RETURNING id_compra;"
```
Tomar el `id_compra` devuelto (variable `CID`) y correr:
```
psql -U postgres -d punto_de_venta -c "SELECT reg_compra_prod('CID','TST001'::id_producto_dominio,44.25,40,60,true);"
psql -U postgres -d punto_de_venta -c "SELECT cantidad, precio_compra FROM producto WHERE id_producto='TST001';"
psql -U postgres -d punto_de_venta -c "SELECT tipo_movimiento, cantidad, referencia FROM kardex WHERE id_producto='TST001';"
```
Esperado: `producto.cantidad = 2400`, `precio_compra = 0.74` (44.25/60 redondeado a 2), y un renglón de kardex `Compra | 2400 | CID`.

- [ ] **Step 5: Limpiar datos de prueba**

Run:
```
psql -U postgres -d punto_de_venta -c "SELECT elim_compra_prod('CID','TST001'::id_producto_dominio);"
psql -U postgres -d punto_de_venta -c "DELETE FROM producto WHERE id_producto='TST001'; DELETE FROM compras WHERE id_compra='CID';"
```
Esperado: producto eliminado, kardex del renglón eliminado, stock revertido a 0 antes del borrado.

- [ ] **Step 6: Commit**

```bash
git add migrations/V1.14__compras_factura.sql
git commit -m "feat(db): migracion V1.14 factura de compra (unidades, IVA, codigo de barras, RFC)"
```

---

## Task 2: `ConexionBD` — proveedor con RFC

**Files:**
- Modify: `src/ConexionBD.java` (métodos de proveedor)

- [ ] **Step 1: Localizar los métodos de proveedor**

Run:
```
grep -n "proveedor" src/ConexionBD.java
```
Esperado: ver `insertarProveedor`, `actualizarProveedor`, `obtenerProveedores`, y un método que trae los datos de un proveedor por id (si no existe, se crea en este task).

- [ ] **Step 2: Agregar/ajustar `insertarProveedor` para incluir `rfc`**

Reemplazar el cuerpo del INSERT de proveedor por uno que incluya `rfc` (orden de `campos`: `nombre, telefono, email, direccion, rfc`):

```java
public boolean insertarProveedor(String[] campos) {
    boolean band = false;
    String sql = "INSERT INTO proveedor(nombre, telefono, email, direccion, rfc) VALUES(?,?,?,?,?);";
    try {
        Connection conexion = DriverManager.getConnection(url + nameBD, usuario, contra);
        PreparedStatement pstm = conexion.prepareStatement(sql);
        pstm.setString(1, campos[0]);
        pstm.setString(2, campos[1]);
        pstm.setString(3, campos[2]);
        pstm.setString(4, campos[3]);
        pstm.setString(5, campos[4]);
        pstm.executeUpdate();
        conexion.close();
        band = true;
    } catch (SQLException e) {
        Mise.JOption(e.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
    }
    return band;
}
```
> Si la firma existente difiere (más/menos campos), conservar las columnas actuales y solo **añadir** `rfc` al final del INSERT y un `pstm.setString(n, campos[n-1])`.

- [ ] **Step 3: Ajustar `actualizarProveedor` para incluir `rfc`**

Añadir `rfc = ?` al `SET` del UPDATE y su `pstm.setString` correspondiente (mismo patrón que el INSERT).

- [ ] **Step 4: Agregar método para traer los datos fiscales de un proveedor**

Agregar:
```java
public String[] obtenerDatosProveedor(int idProveedor) {
    String[] datos = {"", ""}; // [0]=rfc, [1]=direccion
    String sql = "SELECT COALESCE(rfc,'') rfc, COALESCE(direccion,'') direccion FROM proveedor WHERE id_proveedor = ?;";
    try {
        Connection conexion = DriverManager.getConnection(url + nameBD, usuario, contra);
        PreparedStatement pstm = conexion.prepareStatement(sql);
        pstm.setInt(1, idProveedor);
        ResultSet rs = pstm.executeQuery();
        if (rs.next()) {
            datos[0] = rs.getString("rfc");
            datos[1] = rs.getString("direccion");
        }
        conexion.close();
    } catch (SQLException e) {
        System.out.println("Error al obtener datos del proveedor: " + e.getMessage());
    }
    return datos;
}
```

- [ ] **Step 5: Compilar**

Pedir al usuario `ant compile`. Esperado: sin errores.

- [ ] **Step 6: Commit**

```bash
git add src/ConexionBD.java
git commit -m "feat(conexion): proveedor con RFC y obtenerDatosProveedor"
```

---

## Task 3: `ConexionBD` — encabezado de compra (folio, fecha, origen, totales)

**Files:**
- Modify: `src/ConexionBD.java` (`insertarCompraConProveedor` y nuevos métodos)

- [ ] **Step 1: Reemplazar `insertarCompraConProveedor` para guardar encabezado completo**

`campos` pasa a ser: `[id_empleado, descripcion, monto, folio, fechaFactura(yyyy-MM-dd o ""), origen]`.

```java
public String insertarCompraConProveedor(String[] campos, int idProveedor) {
    String idCompra = "";
    String sql = "INSERT INTO compras(id_empleado, descripcion, monto, id_proveedor, folio_proveedor, fecha_factura, origen) "
               + "VALUES(?::curp_dominio, ?, ?::numeric, ?, ?, ?::date, ?) RETURNING id_compra;";
    try {
        Connection conexion = DriverManager.getConnection(url + nameBD, usuario, contra);
        PreparedStatement pstm = conexion.prepareStatement(sql);
        pstm.setString(1, campos[0]);
        pstm.setString(2, campos[1]);
        pstm.setString(3, campos[2]);
        if (idProveedor > 0) pstm.setInt(4, idProveedor); else pstm.setNull(4, Types.INTEGER);
        pstm.setString(5, campos[3].isEmpty() ? null : campos[3]);
        pstm.setString(6, campos[4].isEmpty() ? null : campos[4]);
        pstm.setString(7, campos[5].isEmpty() ? null : campos[5]);
        ResultSet rs = pstm.executeQuery();
        if (rs.next()) idCompra = rs.getString("id_compra");
        conexion.close();
    } catch (SQLException e) {
        Mise.JOption(e.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
    }
    return idCompra;
}
```

- [ ] **Step 2: Agregar verificación de folio duplicado**

```java
public boolean folioYaRegistrado(int idProveedor, String folio) {
    if (idProveedor <= 0 || folio == null || folio.trim().isEmpty()) return false;
    String sql = "SELECT 1 FROM compras WHERE id_proveedor = ? AND folio_proveedor = ? LIMIT 1;";
    boolean existe = false;
    try {
        Connection conexion = DriverManager.getConnection(url + nameBD, usuario, contra);
        PreparedStatement pstm = conexion.prepareStatement(sql);
        pstm.setInt(1, idProveedor);
        pstm.setString(2, folio.trim());
        ResultSet rs = pstm.executeQuery();
        existe = rs.next();
        conexion.close();
    } catch (SQLException e) {
        System.out.println("Error al verificar folio: " + e.getMessage());
    }
    return existe;
}
```

- [ ] **Step 3: Agregar guardado de totales de la factura**

```java
public void actualizarTotalesCompra(String idCompra, double subtotal, double iva, double total) {
    String sql = "UPDATE compras SET subtotal = ?, iva = ?, monto = ? WHERE id_compra = ?;";
    try {
        Connection conexion = DriverManager.getConnection(url + nameBD, usuario, contra);
        PreparedStatement pstm = conexion.prepareStatement(sql);
        pstm.setDouble(1, subtotal);
        pstm.setDouble(2, iva);
        pstm.setDouble(3, total);
        pstm.setString(4, idCompra);
        pstm.executeUpdate();
        conexion.close();
    } catch (SQLException e) {
        Mise.JOption(e.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
    }
}
```

- [ ] **Step 4: Compilar**

Pedir al usuario `ant compile`. Esperado: sin errores. (`ComprasP` aún llama con el array viejo; se corrige en Task 7 — si la compilación falla por el tamaño del array, continuar a Task 7 antes de probar en runtime. Para mantener compilación verde, en este punto el llamado en `ComprasP.hechoB1ActionPerformed` debe actualizarse mínimamente; ver nota.)

> **Nota de compilación:** `insertarCompraConProveedor` ahora espera `campos` de longitud 6. Para no romper la compilación entre tasks, en este step ajustar **solo la construcción del array** en `ComprasP.hechoB1ActionPerformed` a:
> ```java
> String[] campos = {Interfaz.idVendedor, rasF.getText(), "0", "", "", ""};
> ```
> La UI real del encabezado se construye en Task 7.

- [ ] **Step 5: Commit**

```bash
git add src/ConexionBD.java src/ComprasP.java
git commit -m "feat(conexion): encabezado de compra con folio, fecha, origen y totales"
```

---

## Task 4: `ConexionBD` — alta/vínculo de producto y resolución de código

**Files:**
- Modify: `src/ConexionBD.java`

- [ ] **Step 1: Agregar `resolverCodigo` (busca por id_producto o codigo_barras)**

```java
public String resolverCodigo(String codigo) {
    String idProd = null;
    String sql = "SELECT id_producto FROM producto WHERE id_producto = ?::id_producto_dominio OR codigo_barras = ? LIMIT 1;";
    try {
        Connection conexion = DriverManager.getConnection(url + nameBD, usuario, contra);
        PreparedStatement pstm = conexion.prepareStatement(sql);
        pstm.setString(1, codigo);
        pstm.setString(2, codigo);
        ResultSet rs = pstm.executeQuery();
        if (rs.next()) idProd = rs.getString("id_producto");
        conexion.close();
    } catch (SQLException e) {
        System.out.println("Error al resolver codigo: " + e.getMessage());
    }
    return idProd; // null si no existe
}
```
> Nota: el cast `?::id_producto_dominio` puede fallar si `codigo` no cumple el dominio; envolver en sub-consulta segura: usar en su lugar `WHERE id_producto::varchar = ? OR codigo_barras = ?` para evitar el cast estricto.

Versión final a usar:
```java
String sql = "SELECT id_producto FROM producto WHERE id_producto::varchar = ? OR codigo_barras = ? LIMIT 1;";
```

- [ ] **Step 2: Agregar lectura de un producto para autollenar el renglón**

```java
public String[] obtenerProductoParaCompra(String idProducto) {
    // [0]nombre [1]codigo_barras [2]unidad_compra [3]unidad_venta [4]factor [5]lleva_iva("t"/"f") [6]precio_menudeo
    String[] d = null;
    String sql = "SELECT nombre, COALESCE(codigo_barras,'') cb, COALESCE(unidad_compra,'') uc, "
               + "COALESCE(unidad_venta,'') uv, COALESCE(factor_conversion,1) f, COALESCE(lleva_iva,false) iva, "
               + "COALESCE(precio_menudeo,0) pm FROM producto WHERE id_producto = ?;";
    try {
        Connection conexion = DriverManager.getConnection(url + nameBD, usuario, contra);
        PreparedStatement pstm = conexion.prepareStatement(sql);
        pstm.setString(1, idProducto);
        ResultSet rs = pstm.executeQuery();
        if (rs.next()) {
            d = new String[]{ rs.getString("nombre"), rs.getString("cb"), rs.getString("uc"),
                rs.getString("uv"), rs.getString("f"), rs.getBoolean("iva") ? "t" : "f",
                String.valueOf(rs.getDouble("pm")) };
        }
        conexion.close();
    } catch (SQLException e) {
        System.out.println("Error al leer producto: " + e.getMessage());
    }
    return d; // null si no existe
}
```

- [ ] **Step 3: Agregar verificación de código de barras duplicado**

```java
public boolean codigoBarrasDuplicado(String codigoBarras, String idExcluir) {
    if (codigoBarras == null || codigoBarras.trim().isEmpty()) return false;
    String sql = "SELECT 1 FROM producto WHERE codigo_barras = ? AND id_producto <> ? LIMIT 1;";
    boolean dup = false;
    try {
        Connection conexion = DriverManager.getConnection(url + nameBD, usuario, contra);
        PreparedStatement pstm = conexion.prepareStatement(sql);
        pstm.setString(1, codigoBarras.trim());
        pstm.setString(2, idExcluir == null ? "" : idExcluir);
        ResultSet rs = pstm.executeQuery();
        dup = rs.next();
        conexion.close();
    } catch (SQLException e) {
        System.out.println("Error al verificar codigo de barras: " + e.getMessage());
    }
    return dup;
}
```

- [ ] **Step 4: Agregar alta de producto desde compras (transacción atómica de creación)**

```java
public boolean crearProductoDesdeCompra(String[] d) {
    // d: [0]id_producto [1]nombre [2]codigo_barras [3]unidad_compra [4]unidad_venta
    //    [5]factor [6]lleva_iva("t"/"f") [7]precio_menudeo [8]precio_mayoreo [9]precio_compra
    boolean ok = false;
    String sql = "INSERT INTO producto(id_producto, nombre, cantidad, precio_mayoreo, precio_menudeo, "
               + "max_descuento, codigo_barras, lleva_iva, unidad_compra, unidad_venta, factor_conversion, precio_compra) "
               + "VALUES (?,?,0,?,?,0,?,?,?,?,?,?);";
    try {
        Connection conexion = DriverManager.getConnection(url + nameBD, usuario, contra);
        PreparedStatement pstm = conexion.prepareStatement(sql);
        pstm.setString(1, d[0]);
        pstm.setString(2, d[1]);
        pstm.setDouble(3, Double.parseDouble(d[8]));
        pstm.setDouble(4, Double.parseDouble(d[7]));
        pstm.setString(5, d[2].isEmpty() ? null : d[2]);
        pstm.setBoolean(6, "t".equals(d[6]));
        pstm.setString(7, d[3]);
        pstm.setString(8, d[4]);
        pstm.setDouble(9, Double.parseDouble(d[5]));
        pstm.setDouble(10, Double.parseDouble(d[9]));
        pstm.executeUpdate();
        conexion.close();
        ok = true;
    } catch (SQLException e) {
        Mise.JOption(e.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
    }
    return ok;
}
```
> El producto se crea con `cantidad = 0`; la subida de stock (y el kardex) la hace `reg_compra_prod` en Task 5. Crear el producto antes de registrar el renglón mantiene el alta + renglón consistentes.

- [ ] **Step 5: Compilar**

Pedir al usuario `ant compile`. Esperado: sin errores.

- [ ] **Step 6: Commit**

```bash
git add src/ConexionBD.java
git commit -m "feat(conexion): resolver codigo, leer/crear producto desde compras"
```

---

## Task 5: `ConexionBD` — `insertarProdCompra` con factor

**Files:**
- Modify: `src/ConexionBD.java` (`insertarProdCompra`, líneas ~491-517)

- [ ] **Step 1: Reemplazar `insertarProdCompra` para pasar el factor al SP**

`campos`: `[id_compra, id_producto, precio_compra_unidad, cantidad_compra, factor]`.

```java
public void insertarProdCompra(String[] campos, boolean ac) {
    try {
        Connection conexion = DriverManager.getConnection(url + nameBD, usuario, contra);
        String idEmpCompra = "";
        PreparedStatement psEmp = conexion.prepareStatement("SELECT id_empleado FROM compras WHERE id_compra = ?");
        psEmp.setString(1, campos[0]);
        ResultSet rsEmp = psEmp.executeQuery();
        if (rsEmp.next()) idEmpCompra = rsEmp.getString(1);
        Statement stmtSet = conexion.createStatement();
        stmtSet.execute("SET kardex.tipo = 'Compra'");
        stmtSet.execute("SET kardex.referencia = '" + campos[0] + "'");
        if (!idEmpCompra.isEmpty())
            stmtSet.execute("SET kardex.empleado = '" + idEmpCompra + "'");
        CallableStatement cstm = conexion.prepareCall("{call reg_compra_prod(?,?::id_producto_dominio,?,?,?,?)}");
        cstm.setString(1, campos[0]);
        cstm.setString(2, campos[1]);
        cstm.setObject(3, campos[2], Types.NUMERIC);   // precio por unidad de compra
        cstm.setObject(4, campos[3], Types.INTEGER);   // cantidad en unidad de compra
        cstm.setObject(5, campos[4], Types.NUMERIC);   // factor
        cstm.setBoolean(6, ac);
        cstm.execute();
        conexion.close();
    } catch (SQLException e) {
        Mise.JOption(e.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
    }
}
```

- [ ] **Step 2: Compilar**

Pedir al usuario `ant compile`. Esperado: error en `ComprasP.agPActionPerformed` por el array de 4 (se corrige en Task 8). Para mantener verde, ajustar temporalmente en `ComprasP.agPActionPerformed` el array a 5 con factor 1:
```java
String[] campos = {id_compra, codP.getText(), precP.getText(), cantP.getText(), "1"};
```
Recompilar: sin errores.

- [ ] **Step 3: Commit**

```bash
git add src/ConexionBD.java src/ComprasP.java
git commit -m "feat(conexion): insertarProdCompra pasa factor de conversion"
```

---

## Task 6: `ProveedoresP` — campo RFC

**Files:**
- Modify: `src/ProveedoresP.java`

- [ ] **Step 1: Agregar componentes RFC en NetBeans (alta y edición)**

En el editor de formularios de `ProveedoresP`, en `panelRegistro` y `panelEditar`:
- Agregar `JLabel` `jLabelRfcR` ("RFC:") y `JFormattedTextField` `rfcProvR` (alta).
- Agregar `JLabel` `jLabelRfcE` ("RFC:") y `JFormattedTextField` `rfcProvE` (edición).
- Colocar después del campo Dirección, mismas fuentes/insets que los demás (`Noto Serif`, tamaño 18).

- [ ] **Step 2: Incluir RFC en el registro**

En el handler del botón registrar (`regProvBtnActionPerformed` o equivalente), donde se arma el array de campos para `insertarProveedor`, agregar `rfcProvR.getText().trim()` como **quinto** elemento, en orden `nombre, telefono, email, direccion, rfc`:
```java
String[] campos = { nomProvR.getText().trim(), telProvR.getText().trim(),
                    emailProvR.getText().trim(), dirProvR.getText().trim(),
                    rfcProvR.getText().trim() };
conect.insertarProveedor(campos);
```

- [ ] **Step 3: Incluir RFC en la edición**

En el handler de editar, agregar `rfcProvE.getText().trim()` al array que va a `actualizarProveedor`, en la misma posición que el SET del UPDATE definido en Task 2 Step 3.

- [ ] **Step 4: Cargar RFC al abrir el diálogo de edición**

Donde se llenan los campos del diálogo de edición con los datos de la fila seleccionada, agregar el seteo de `rfcProvE` desde la columna RFC. Si la tabla de proveedores no muestra RFC, leerlo con `obtenerDatosProveedor(idProv)`:
```java
String[] fiscal = conect.obtenerDatosProveedor(idProv);
rfcProvE.setText(fiscal[0]);
```

- [ ] **Step 5: Validación de formato RFC (aviso, no bloqueo)**

Antes de registrar/actualizar, si el RFC no está vacío y no cumple el patrón, avisar pero continuar:
```java
String rfc = rfcProvR.getText().trim().toUpperCase();
if (!rfc.isEmpty() && !rfc.matches("^[A-ZÑ&]{3,4}[0-9]{6}[A-Z0-9]{3}$")) {
    Mise.JOption("El RFC no tiene un formato válido. Se guardará de todos modos.", "Advertencia", javax.swing.JOptionPane.WARNING_MESSAGE);
}
```
(Replicar para edición con `rfcProvE`.)

- [ ] **Step 6: Compilar y verificar UI**

Pedir al usuario `ant compile` y `ant run`.
Verificar UI: registrar un proveedor con RFC `EPI790314K22` → guardar → editar el mismo → el campo RFC muestra `EPI790314K22`.
Verificar SQL: `psql -U postgres -d punto_de_venta -c "SELECT nombre, rfc FROM proveedor ORDER BY id_proveedor DESC LIMIT 1;"` → muestra el RFC.

- [ ] **Step 7: Commit**

```bash
git add src/ProveedoresP.java src/ProveedoresP.form
git commit -m "feat(proveedores): captura de RFC en alta y edicion"
```

---

## Task 7: `ComprasP` Paso 1 — encabezado de la factura

**Files:**
- Modify: `src/ComprasP.java`

- [ ] **Step 1: Agregar componentes del encabezado en NetBeans**

En `panelRegCompra` (el `comDialog`), agregar bajo el combo de proveedor:
- `JLabel` `lblRfc` + `JLabel` `valRfc` (solo lectura, muestra RFC del proveedor).
- `JButton` `nuevoProvBtn` ("+ Nuevo") junto al combo.
- `JLabel` `lblFolio` + `JFormattedTextField` `folioF`.
- `JLabel` `lblFechaFact` + `JFormattedTextField` `fechaFactF` (formato `dd/MM/yyyy`).
- `JLabel` `lblOrigen` + `JFormattedTextField` `origenF`.
- Mantener el `rasF` (notas) y el botón `hechoB1` (renombrar texto a "Continuar →").

Aumentar `comDialog.setSize` a ~`(620, 480)` para que quepan los campos.

- [ ] **Step 2: Mostrar RFC/dirección al elegir proveedor**

Agregar listener al `proveedorCombo` (`proveedorComboActionPerformed`):
```java
private void proveedorComboActionPerformed(java.awt.event.ActionEvent evt) {
    int idx = proveedorCombo.getSelectedIndex();
    if (idx > 0 && idx <= proveedorIds.size()) {
        String[] fiscal = conect.obtenerDatosProveedor(proveedorIds.get(idx - 1));
        valRfc.setText(fiscal[0].isEmpty() ? "(sin RFC)" : fiscal[0]);
        valRfc.setToolTipText(fiscal[1]); // direccion
    } else {
        valRfc.setText("");
        valRfc.setToolTipText(null);
    }
}
```

- [ ] **Step 3: Botón "+ Nuevo proveedor"**

```java
private void nuevoProvBtnActionPerformed(java.awt.event.ActionEvent evt) {
    Interfaz.cambiarPanel(new ProveedoresP()); // abre el módulo de proveedores
    comDialog.setVisible(false);
}
```
> Verificar el nombre real del método de navegación en `Interfaz.java` (buscar cómo otros paneles cambian de vista) y usar ese. Si no hay uno público, mostrar un aviso indicando registrar el proveedor en su módulo y recargar con `cargarProveedores()` al volver.

- [ ] **Step 4: Validación de folio y construcción del encabezado**

Reemplazar `hechoB1ActionPerformed`:
```java
private void hechoB1ActionPerformed(java.awt.event.ActionEvent evt) {
    int idProvSeleccionado = -1;
    int idx = proveedorCombo.getSelectedIndex();
    if (idx > 0 && idx <= proveedorIds.size()) idProvSeleccionado = proveedorIds.get(idx - 1);

    String folio = folioF.getText().trim();
    if (idProvSeleccionado > 0 && !folio.isEmpty() && conect.folioYaRegistrado(idProvSeleccionado, folio)) {
        int r = Mise.JOptionYesNo("Esta factura (folio " + folio + ") ya fue registrada para este proveedor.\n¿Registrarla de todos modos?", "Folio duplicado");
        if (r != 0) return;
    }

    String fechaSql = "";
    String fechaUi = fechaFactF.getText().trim();
    if (!fechaUi.isEmpty()) {
        try {
            java.text.SimpleDateFormat in = new java.text.SimpleDateFormat("dd/MM/yyyy");
            java.text.SimpleDateFormat out = new java.text.SimpleDateFormat("yyyy-MM-dd");
            fechaSql = out.format(in.parse(fechaUi));
        } catch (java.text.ParseException ex) {
            Mise.JOption("Fecha de factura inválida (use dd/MM/yyyy).", "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
            return;
        }
    }

    String descripcion = rasF.getText().trim();
    String[] campos = { Interfaz.idVendedor, descripcion, "0", folio, fechaSql, origenF.getText().trim() };
    id_compra = conect.insertarCompraConProveedor(campos, idProvSeleccionado);
    if (id_compra == null || id_compra.isEmpty()) return;

    mostrarTablaProd();
    mostrarTablaProdCom();
    comDialog.setVisible(false);
    prodComDialog.setVisible(true);
}
```
> `descripcion` ya no es obligatoria (la factura se identifica por folio). Quitar la validación previa de `rasF` vacío.

- [ ] **Step 5: Limpiar campos al abrir el encabezado**

En `agreCompraActionPerformed`, tras `cargarProveedores()`, limpiar:
```java
folioF.setText(""); fechaFactF.setText(""); origenF.setText(""); rasF.setText(""); valRfc.setText("");
```

- [ ] **Step 6: Compilar y verificar UI**

`ant compile` + `ant run` (usuario).
Verificar UI: Compras → Agregar → elegir proveedor (con RFC) → se muestra su RFC → teclear folio `P-99843`, fecha `06/06/2026`, origen `Oaxaca de Juárez` → Continuar → abre Paso 2 sin error.
Verificar SQL: `psql -U postgres -d punto_de_venta -c "SELECT folio_proveedor, fecha_factura, origen FROM compras ORDER BY id_compra DESC LIMIT 1;"` → muestra los datos.

- [ ] **Step 7: Commit**

```bash
git add src/ComprasP.java src/ComprasP.form
git commit -m "feat(compras): paso 1 encabezado de factura (folio, fecha, origen, RFC)"
```

---

## Task 8: `ComprasP` Paso 2 — captura de renglón con alta/vínculo

**Files:**
- Modify: `src/ComprasP.java`

- [ ] **Step 1: Agregar campos de captura en NetBeans**

En `jPanel5` (panel de captura del `prodComDialog`), reorganizar/añadir:
- `codP` (código interno, editable ahora), `codBarrasP` (`JFormattedTextField`), botón `buscarP` ("🔍 Buscar"), `JLabel` `estadoP` (muestra "Nuevo"/"Existente").
- `conceptoP` (`JFormattedTextField`, nombre).
- `cantP` (existente), `unidadCompraP` (`JComboBox<String>` con `Pieza, Caja, Bolsa, Bulto, Botella, Lata, Paquete, Bote, Barra, Vaso, Tetra Pak, kg`), `factorP` (`JFormattedTextField`), `unidadVentaP` (`JComboBox<String>` mismas opciones).
- `precP` (P. compra, existente), `ivaP` (`JCheckBox` "Lleva IVA"), `margenP` (`JFormattedTextField`, %), `pVentaP` (`JFormattedTextField`).
- Botones existentes `agP`/`acP`/`elP` (Agregar/Actualizar/Quitar). Quitar `heP` de aquí (el guardar va en Task 9).

Hacer `codP.setEditable(true)`.

- [ ] **Step 2: Helper para habilitar/deshabilitar campos según existente/nuevo**

```java
private void setCamposNuevo(boolean nuevo) {
    conceptoP.setEditable(nuevo);
    unidadCompraP.setEnabled(nuevo);
    unidadVentaP.setEnabled(nuevo);
    factorP.setEditable(nuevo);
    ivaP.setEnabled(nuevo);
    margenP.setEditable(nuevo);
    pVentaP.setEditable(nuevo);
    estadoP.setText(nuevo ? "● Nuevo" : "Existente");
}
```

- [ ] **Step 3: Botón Buscar (resuelve y autollena)**

```java
private void buscarPActionPerformed(java.awt.event.ActionEvent evt) {
    String codigo = codP.getText().trim();
    if (codigo.isEmpty()) codigo = codBarrasP.getText().trim();
    if (codigo.isEmpty()) { Mise.JOption("Escriba un código interno o de barras.", "Error", javax.swing.JOptionPane.ERROR_MESSAGE); return; }

    String idProd = conect.resolverCodigo(codigo);
    if (idProd != null) {
        String[] p = conect.obtenerProductoParaCompra(idProd);
        codP.setText(idProd);
        conceptoP.setText(p[0]);
        codBarrasP.setText(p[1]);
        unidadCompraP.setSelectedItem(p[2]);
        unidadVentaP.setSelectedItem(p[3]);
        factorP.setText(p[4]);
        ivaP.setSelected("t".equals(p[5]));
        setCamposNuevo(false);
    } else {
        if (factorP.getText().trim().isEmpty()) factorP.setText("1");
        if (margenP.getText().trim().isEmpty()) margenP.setText("30");
        setCamposNuevo(true);
    }
}
```

- [ ] **Step 4: Sugerir precio de venta por margen**

Listener `keyReleased` en `precP`, `factorP` y `margenP` que recalcula `pVentaP` cuando es producto nuevo:
```java
private void recalcularPVenta() {
    if (!pVentaP.isEditable()) return; // existente: no tocar
    try {
        double precio = Double.parseDouble(precP.getText().trim());
        double factor = Double.parseDouble(factorP.getText().trim());
        double margen = Double.parseDouble(margenP.getText().trim());
        if (factor <= 0) return;
        double costoUnit = precio / factor;
        double venta = costoUnit * (1 + margen / 100.0);
        pVentaP.setText(String.format(java.util.Locale.US, "%.2f", venta));
    } catch (NumberFormatException ignored) {}
}
```
Conectar los tres campos a `recalcularPVenta()` en sus eventos `keyReleased`.

- [ ] **Step 5: Reemplazar Agregar renglón (`agPActionPerformed`)**

```java
private void agPActionPerformed(java.awt.event.ActionEvent evt) {
    String codigo = codP.getText().trim();
    if (codigo.isEmpty() || precP.getText().trim().isEmpty() || cantP.getText().trim().isEmpty()) {
        Mise.JOption("Debe llenar código, cantidad y precio de compra.", "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        return;
    }
    String factorStr = factorP.getText().trim().isEmpty() ? "1" : factorP.getText().trim();
    double factor;
    try { factor = Double.parseDouble(factorStr); } catch (NumberFormatException e) { factor = 1; }
    if (factor <= 0) { Mise.JOption("El factor debe ser mayor a 0.", "Error", javax.swing.JOptionPane.ERROR_MESSAGE); return; }

    String idProd = conect.resolverCodigo(codigo);
    boolean nuevo = (idProd == null);

    if (nuevo) {
        if (!codigo.matches("^[A-Za-z0-9_-]{3,50}$")) {
            Mise.JOption("El código interno debe ser 3-50 caracteres (letras, números, - o _).", "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (conceptoP.getText().trim().isEmpty()) {
            Mise.JOption("Escriba el concepto/nombre del producto nuevo.", "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
            return;
        }
        String cb = codBarrasP.getText().trim();
        if (!cb.isEmpty() && !cb.matches("^[0-9]{8,14}$")) {
            Mise.JOption("El código de barras debe tener entre 8 y 14 dígitos.", "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (conect.codigoBarrasDuplicado(cb, codigo)) {
            Mise.JOption("Ese código de barras ya está en otro producto.", "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
            return;
        }
        String pVenta = pVentaP.getText().trim().isEmpty() ? "0" : pVentaP.getText().trim();
        String[] d = { codigo, conceptoP.getText().trim(), cb,
            (String) unidadCompraP.getSelectedItem(), (String) unidadVentaP.getSelectedItem(),
            factorStr, ivaP.isSelected() ? "t" : "f", pVenta, pVenta, "0" };
        if (!conect.crearProductoDesdeCompra(d)) return; // aborta si falla el alta
        idProd = codigo;
    }

    String[] campos = { id_compra, idProd, precP.getText().trim(), cantP.getText().trim(), factorStr };
    conect.insertarProdCompra(campos, ins);
    ins = true;
    limpiarCapturaRenglon();
    mostrarTablaProdCom();
    actualizarTotalesUI(); // definido en Task 9
}

private void limpiarCapturaRenglon() {
    codP.setText(""); codBarrasP.setText(""); conceptoP.setText("");
    cantP.setText(""); precP.setText(""); factorP.setText(""); margenP.setText(""); pVentaP.setText("");
    ivaP.setSelected(false); estadoP.setText("");
    setCamposNuevo(true);
}
```

- [ ] **Step 6: Ajustar `mostrarTablaProdCom` a las columnas nuevas**

Cambiar el modelo de `tablaProdCom` (en NetBeans) a columnas: `Código, Concepto, Cant, Unidad, P.Unit, IVA, Importe`. Y el método:
```java
public void mostrarTablaProdCom(){
    Mise.limpiarTabla(modeloProdCom);
    java.sql.ResultSet rs = conect.query(
        "SELECT cp.id_producto, p.nombre, cp.cantidad, COALESCE(cp.unidad_compra,'') uc, "
      + "cp.precio_adquirido, COALESCE(cp.lleva_iva,false) iva, cp.precio_total "
      + "FROM compra_producto cp JOIN producto p ON p.id_producto = cp.id_producto "
      + "WHERE cp.id_compra='" + id_compra + "';");
    try{
        while(rs.next()){
            modeloProdCom.addRow(new Object[]{ rs.getString("id_producto"), rs.getString("nombre"),
                rs.getInt("cantidad"), rs.getString("uc"), rs.getDouble("precio_adquirido"),
                rs.getBoolean("iva") ? "Sí" : "No", rs.getDouble("precio_total") });
        }
    } catch(java.sql.SQLException e){ System.out.println("Error al mostrar productos comprados"); }
}
```

- [ ] **Step 7: Actualizar/Quitar renglón con los nuevos índices de columna**

En `acPActionPerformed`, leer de la fila: código (col 0), cantidad (col 2), precio (col 4); y poner `ins=false`:
```java
codP.setText("" + tablaProdCom.getValueAt(tablaProdCom.getSelectedRow(), 0));
cantP.setText("" + tablaProdCom.getValueAt(tablaProdCom.getSelectedRow(), 2));
precP.setText("" + tablaProdCom.getValueAt(tablaProdCom.getSelectedRow(), 4));
buscarPActionPerformed(null); // autollena unidades/factor del producto
ins = false;
```
En `elPActionPerformed`, el array sigue siendo `[id_compra, codigo(col 0)]`; tras eliminar, llamar `actualizarTotalesUI()`.

- [ ] **Step 8: Compilar y verificar**

`ant compile` + `ant run` (usuario).
Verificar UI: en Paso 2, teclear código nuevo `CHI015`, concepto, U.compra `Caja`, factor `60`, U.venta `Pieza`, P.compra `44.25`, margen `30` → `P.venta` muestra `0.96` → Agregar → aparece renglón `40 Caja 44.25 ... 1770.00`.
Verificar SQL:
```
psql -U postgres -d punto_de_venta -c "SELECT cantidad, factor_conversion FROM producto WHERE id_producto='CHI015';"
psql -U postgres -d punto_de_venta -c "SELECT tipo_movimiento, cantidad FROM kardex WHERE id_producto='CHI015';"
```
Esperado: `producto.cantidad = 2400`, kardex `Compra | 2400`.

- [ ] **Step 9: Commit**

```bash
git add src/ComprasP.java src/ComprasP.form
git commit -m "feat(compras): paso 2 captura de renglon con alta/vinculo, unidades, IVA y margen"
```

---

## Task 9: `ComprasP` Paso 2 — totales, cuadre y guardar factura

**Files:**
- Modify: `src/ComprasP.java`

- [ ] **Step 1: Agregar componentes de totales en NetBeans**

Bajo `tablaProdCom`, agregar una franja: `JLabel` `lblSubtotal`, `lblIva`, `lblTotal` (muestran montos); `JFormattedTextField` `totalFacturaF` (total tecleado de la factura); `JLabel` `lblCuadre` (✓/✗); y botón `guardarFacturaBtn` ("💾 Guardar factura", verde, grande, abajo a la derecha).

- [ ] **Step 2: Calcular y mostrar totales desde la BD**

```java
private double[] calcularTotales() {
    double subtotal = 0, iva = 0;
    java.sql.ResultSet rs = conect.query(
        "SELECT precio_total, COALESCE(lleva_iva,false) iva FROM compra_producto WHERE id_compra='" + id_compra + "';");
    try {
        while (rs.next()) {
            double imp = rs.getDouble("precio_total");
            subtotal += imp;
            if (rs.getBoolean("iva")) iva += imp * 0.16;
        }
    } catch (java.sql.SQLException e) { System.out.println("Error al calcular totales"); }
    return new double[]{ subtotal, iva, subtotal + iva };
}

private void actualizarTotalesUI() {
    double[] t = calcularTotales();
    lblSubtotal.setText(String.format(java.util.Locale.US, "Subtotal: $%.2f", t[0]));
    lblIva.setText(String.format(java.util.Locale.US, "IVA 16%%: $%.2f", t[1]));
    lblTotal.setText(String.format(java.util.Locale.US, "Total: $%.2f", t[2]));
    verificarCuadre(t[2]);
}

private void verificarCuadre(double totalCalc) {
    String txt = totalFacturaF.getText().trim();
    if (txt.isEmpty()) { lblCuadre.setText(""); return; }
    try {
        double totalFact = Double.parseDouble(txt);
        boolean cuadra = Math.abs(totalFact - totalCalc) <= 0.50;
        lblCuadre.setText(cuadra ? "✓ Cuadra" : "✗ No cuadra");
        lblCuadre.setForeground(cuadra ? new java.awt.Color(0,150,0) : java.awt.Color.RED);
    } catch (NumberFormatException e) { lblCuadre.setText(""); }
}
```
Conectar `totalFacturaF` (keyReleased) a `verificarCuadre(calcularTotales()[2])`.

- [ ] **Step 3: Guardar factura**

```java
private void guardarFacturaBtnActionPerformed(java.awt.event.ActionEvent evt) {
    if (modeloProdCom.getRowCount() == 0) {
        Mise.JOption("Agregue al menos un producto antes de guardar.", "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        return;
    }
    double[] t = calcularTotales();
    String txt = totalFacturaF.getText().trim();
    if (!txt.isEmpty()) {
        try {
            if (Math.abs(Double.parseDouble(txt) - t[2]) > 0.50) {
                int r = Mise.JOptionYesNo("El total calculado ($" + String.format(java.util.Locale.US,"%.2f",t[2])
                    + ") no cuadra con el de la factura ($" + txt + ").\n¿Guardar de todos modos?", "Totales no cuadran");
                if (r != 0) return;
            }
        } catch (NumberFormatException ignored) {}
    }
    conect.actualizarTotalesCompra(id_compra, t[0], t[1], t[2]);
    prodComDialog.setVisible(false);
    mostrarTablaCom();
}
```

- [ ] **Step 4: Recalcular totales al abrir el Paso 2**

En `actCompraActionPerformed` y al final de `hechoB1ActionPerformed` (tras `mostrarTablaProdCom()`), añadir `actualizarTotalesUI();`.

- [ ] **Step 5: Ajustar el cierre de ventana**

`prodComDialogWindowClosing` se mantiene: si no hay renglones pregunta y borra la compra; si hay, cierra y refresca. Verificar que tras cerrar con renglones se llame `mostrarTablaCom()` (ya lo hace).

- [ ] **Step 6: Actualizar `mostrarTablaCom` para mostrar folio**

Cambiar el modelo de `tablaCompras` (NetBeans) a `Compra, Folio, Proveedor, Fecha, Total` y el método:
```java
public void mostrarTablaCom(){
    Mise.limpiarTabla(modeloCom);
    java.sql.ResultSet rs = conect.query(
        "SELECT c.id_compra, COALESCE(c.folio_proveedor,'') folio, COALESCE(pr.nombre,'') prov, "
      + "c.fecha_compra, c.monto FROM compras c LEFT JOIN proveedor pr ON pr.id_proveedor=c.id_proveedor "
      + "ORDER BY c.id_compra DESC;");
    try{
        while(rs.next()){
            modeloCom.addRow(new Object[]{ rs.getString("id_compra"), rs.getString("folio"),
                rs.getString("prov"), rs.getDate("fecha_compra"), rs.getDouble("monto") });
        }
    } catch(java.sql.SQLException e){ System.out.println("Error al mostrar la tabla compras"); }
}
```
> Ajustar `aplicarOrdenCompras` a los nuevos índices de columna (fecha=3, monto=4).

- [ ] **Step 7: Compilar y verificar end-to-end con la factura P-99843**

`ant compile` + `ant run` (usuario). Capturar los 20 renglones de la factura de ejemplo.
Verificar UI: la franja muestra `Subtotal: $9,331.44`, `IVA 16%: $1,493.03`, `Total: $10,824.47`; teclear `10824.47` en total de factura → `✓ Cuadra` → Guardar.
Verificar SQL:
```
psql -U postgres -d punto_de_venta -c "SELECT subtotal, iva, monto FROM compras ORDER BY id_compra DESC LIMIT 1;"
```
Esperado: `9331.44 | 1493.03 | 10824.47` (±0.01 por redondeo).

- [ ] **Step 8: Commit**

```bash
git add src/ComprasP.java src/ComprasP.form
git commit -m "feat(compras): totales, cuadre contra factura y guardado del encabezado"
```

---

## Task 10: `InventarioP` — exponer campos nuevos del producto

**Files:**
- Modify: `src/InventarioP.java`

- [ ] **Step 1: Localizar el alta/edición de producto**

Run:
```
grep -n "insertarProductoConCodigoYCategoria\|actualizarProductoConCategoria\|codigo_barras\|precio_menudeo" src/InventarioP.java
```
Esperado: ubicar los handlers de registro/edición y los campos del formulario.

- [ ] **Step 2: Agregar componentes en NetBeans**

En los diálogos de alta y edición de `InventarioP`, agregar: `codigo_barras`, `lleva_iva` (checkbox), `unidad_compra`/`unidad_venta` (combos con la misma lista que `ComprasP`), `factor_conversion`, `precio_compra` (este último puede ser solo lectura: lo fija Compras).

- [ ] **Step 3: Agregar métodos de persistencia con los campos nuevos**

Agregar en `ConexionBD` (junto a los de producto):
```java
public void actualizarCamposCompraProducto(String idProducto, String codigoBarras, boolean llevaIva,
        String unidadCompra, String unidadVenta, double factor) {
    String sql = "UPDATE producto SET codigo_barras=?, lleva_iva=?, unidad_compra=?, unidad_venta=?, factor_conversion=? WHERE id_producto=?;";
    try {
        Connection conexion = DriverManager.getConnection(url + nameBD, usuario, contra);
        PreparedStatement pstm = conexion.prepareStatement(sql);
        pstm.setString(1, codigoBarras == null || codigoBarras.isEmpty() ? null : codigoBarras);
        pstm.setBoolean(2, llevaIva);
        pstm.setString(3, unidadCompra);
        pstm.setString(4, unidadVenta);
        pstm.setDouble(5, factor);
        pstm.setString(6, idProducto);
        pstm.executeUpdate();
        conexion.close();
    } catch (SQLException e) {
        Mise.JOption(e.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
    }
}
```

- [ ] **Step 4: Conectar el alta y la edición**

- En el alta de producto de `InventarioP`: usar `crearProductoDesdeCompra` (Task 4) **o** tras `insertarProductoConCodigoYCategoria` llamar `actualizarCamposCompraProducto(...)` con los nuevos campos. Elegir la segunda opción para no duplicar la lógica de categoría/descuento.
- En la edición: tras `actualizarProductoConCategoria(...)`, llamar `actualizarCamposCompraProducto(...)`.
- Al abrir la edición, precargar los campos nuevos con `obtenerProductoParaCompra(idProducto)`.

- [ ] **Step 5: Compilar y verificar UI**

`ant compile` + `ant run` (usuario).
Verificar UI: editar `CHI015` en Inventario → se ven `Caja`/`Pieza`/`60`/IVA; cambiar factor a `48`, guardar; reabrir → muestra `48`.
Verificar SQL: `psql -U postgres -d punto_de_venta -c "SELECT codigo_barras, unidad_compra, unidad_venta, factor_conversion, lleva_iva FROM producto WHERE id_producto='CHI015';"`.

- [ ] **Step 6: Commit**

```bash
git add src/InventarioP.java src/InventarioP.form src/ConexionBD.java
git commit -m "feat(inventario): editar codigo de barras, IVA, unidades y factor de producto"
```

---

## Task 11: `VentasP` — escaneo por id o código de barras

**Files:**
- Modify: `src/VentasP.java` (`regActionPerformed`, ~907-929)

- [ ] **Step 1: Resolver el código antes de agregar a la venta**

En `regActionPerformed`, reemplazar:
```java
String idProd = codigoProd.getText();
```
por:
```java
String idProd = conect.resolverCodigo(codigoProd.getText().trim());
if (idProd == null) {
    Mise.JOption("No existe un producto con ese código o código de barras.", "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
    return;
}
```
El resto del método (`obtenerMaxDescuento`, `insertarVentaTemp`, etc.) usa ese `idProd` resuelto.

- [ ] **Step 2: Compilar y verificar UI**

`ant compile` + `ant run` (usuario).
Verificar UI (con un producto que tenga `id_producto`=`CHI015` y `codigo_barras`=`7501021411228`):
- Escanear/teclear `CHI015` → se agrega a la venta.
- Escanear/teclear `7501021411228` → se agrega el mismo producto.
- Verificar que un producto viejo cuyo `id_producto` es un barcode (sin `codigo_barras`) **sigue** agregándose al teclear su id.

- [ ] **Step 3: Commit**

```bash
git add src/VentasP.java
git commit -m "feat(ventas): escaneo resuelve por id_producto o codigo_barras"
```

---

## Self-review (cobertura del spec)

- §3.1 proveedor.rfc → Task 1, Task 2, Task 6. ✓
- §3.2 producto (codigo_barras, precio_compra, lleva_iva, unidades, factor, nombre 100) → Task 1; uso en Tasks 4, 8, 10. ✓
- §3.3 compras (folio, fecha_factura, origen, subtotal, iva; índice antiduplicado) → Task 1; uso en Tasks 3, 7, 9. ✓
- §3.4 compra_producto (snapshot) → Task 1; uso en Task 5/8. ✓
- §3.5 reg_compra_prod/elim con factor → Task 1, Task 5. ✓
- §4 flujo dos pasos → Task 7 (Paso 1), Task 8/9 (Paso 2). ✓
- §5 lógica de renglón en vivo + kardex por producto → Task 1 (SP), Task 5, Task 8. ✓
- §6 validaciones (RFC, folio dup, dominio id, barcode, requeridos, cuadre) → Tasks 6, 7, 8, 9. ✓
- §7 alcance acompañante (Inventario, Ventas/resolverCodigo) → Task 10, Task 11 (helper en Task 4). ✓
- §8 fuera de alcance → respetado (sin IVA en ventas, sin doble unidad). ✓

Consistencia de nombres verificada: `resolverCodigo`, `obtenerProductoParaCompra`, `crearProductoDesdeCompra`, `insertarProdCompra(campos,ac)` con factor, `actualizarTotalesCompra`, `folioYaRegistrado`, `codigoBarrasDuplicado`, `actualizarTotalesUI`/`calcularTotales`/`verificarCuadre` usados consistentemente entre tasks.
