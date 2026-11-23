package puntoventa.db;

import puntoventa.util.*;

import java.sql.*;

/**
 * DAO del dominio Producto: catálogo, alta/edición (con y sin categoría),
 * baja/reactivación/archivado, consultas para compra y descuentos.
 *
 * @author mayra
 */
public class ProductoDAO extends BaseDAO {

    public void insertarProducto(String[] datos) {//Función para insertar un producto
        String columnas = "producto(nombre, cantidad, precio_mayoreo, precio_menudeo)";
        String instruccion = "INSERT INTO " + columnas + " VALUES (?,?,?,?);";
        try (Connection conexion = conectar()) {
            PreparedStatement pstm = conexion.prepareStatement(instruccion);
            pstm.setString(1, datos[0]);
            pstm.setInt(2, Integer.parseInt(datos[1]));
            pstm.setDouble(3, Double.parseDouble(datos[2]));
            pstm.setDouble(4, Double.parseDouble(datos[3]));
            pstm.executeUpdate();
        } catch (SQLException e) {
            GestorErrores.manejar(e);
        }
    }

    public void insertarProductoConCodigo(String[] datos) {//Función para insertar un producto con código personalizado
        String columnas = "producto(id_producto, nombre, cantidad, precio_mayoreo, precio_menudeo)";
        String instruccion = "INSERT INTO " + columnas + " VALUES (?,?,?,?,?);";
        try (Connection conexion = conectar()) {
            PreparedStatement pstm = conexion.prepareStatement(instruccion);
            pstm.setString(1, datos[0]); // id_producto (código personalizado)
            pstm.setString(2, datos[1]); // nombre
            pstm.setInt(3, Integer.parseInt(datos[2])); // cantidad
            pstm.setDouble(4, Double.parseDouble(datos[3])); // precio_mayoreo
            pstm.setDouble(5, Double.parseDouble(datos[4])); // precio_menudeo
            pstm.executeUpdate();
        } catch (SQLException e) {
            GestorErrores.manejar(e);
        }
    }

    public void actualizarProducto(String ideprod, String[] datos) {//Función para actualizar un producto
        String columnas = "nombre = ?, cantidad = ?, precio_mayoreo = ?, precio_menudeo = ?";
        String instruccion = "UPDATE producto SET " + columnas + " WHERE id_producto = ?;";
        try (Connection conexion = conectar()) {
            PreparedStatement pstm = conexion.prepareStatement(instruccion);
            pstm.setString(1, datos[0]);
            pstm.setInt(2, Integer.parseInt(datos[1]));
            pstm.setDouble(3, Double.parseDouble(datos[2]));
            pstm.setDouble(4, Double.parseDouble(datos[3]));
            pstm.setString(5, ideprod);
            pstm.executeUpdate();
        } catch (SQLException e) {
            GestorErrores.manejar(e);
        }
    }

    public boolean eliminarProducto(String ideprod) {//Función para dar de baja un producto (baja lógica)
        boolean band = false;
        try (Connection conexion = conectar()) {
            CallableStatement cstm = conexion.prepareCall("{call baja_producto(?::id_producto_dominio)}");
            cstm.setString(1, ideprod);
            cstm.execute();
            band = true;
        } catch (SQLException e) {
            GestorErrores.manejar(e);
        }
        return band;
    }

    public boolean reactivarProducto(String ideprod) {//Reactiva un producto dado de baja (conserva historial)
        boolean band = false;
        try (Connection conexion = conectar()) {
            CallableStatement cstm = conexion.prepareCall("{call reactivar_producto(?::id_producto_dominio)}");
            cstm.setString(1, ideprod);
            cstm.execute();
            band = true;
        } catch (SQLException e) {
            GestorErrores.manejar(e);
        }
        return band;
    }

    public String archivarProducto(String ideprod) {//Archiva un producto liberando su código de barras; devuelve el código archivado
        String archivado = "";
        try (Connection conexion = conectar()) {
            CallableStatement cstm = conexion.prepareCall("{? = call archivar_producto(?::id_producto_dominio)}");
            cstm.registerOutParameter(1, Types.VARCHAR);
            cstm.setString(2, ideprod);
            cstm.execute();
            archivado = cstm.getString(1);
        } catch (SQLException e) {
            GestorErrores.manejar(e);
        }
        return archivado;
    }

    public String estadoProducto(String ideprod) {//Devuelve "Activo", "Inactivo" o null si el código no existe
        String estado = null;
        try (Connection conexion = conectar()) {
            PreparedStatement pstm = conexion.prepareStatement("SELECT estatus FROM producto WHERE id_producto = ?");
            pstm.setString(1, ideprod);
            ResultSet rs = pstm.executeQuery();
            if (rs.next()) estado = rs.getString(1);
        } catch (SQLException e) {
            GestorErrores.manejar(e);
        }
        return estado;
    }

    public ResultSet obtenerProductosInactivos() {//Productos dados de baja (no cierra la conexión: el ResultSet queda usable)
        ResultSet rs = null;
        String instruccion = "SELECT id_producto, nombre, cantidad, fecha_baja FROM producto " +
            "WHERE estatus = 'Inactivo' ORDER BY fecha_baja DESC NULLS LAST, nombre";
        try (Connection conexion = conectar()) {
            PreparedStatement pstm = conexion.prepareStatement(instruccion);
            rs = pstm.executeQuery();
        } catch (SQLException e) {
            GestorErrores.manejar(e);
        }
        return rs;
    }

    public boolean hayPocosProductos() {//Función para verificar si hay pocos productos
        boolean hay = false;
        try (Connection conexion = conectar()) {
            CallableStatement cstm = conexion.prepareCall("{call hay_pocos_prod()}");
            ResultSet rs = cstm.executeQuery();
            while (rs.next()) {
                hay = rs.getBoolean(1);
            }
        } catch (SQLException e) {
            GestorErrores.manejar(e);
        }
        return hay;
    }

    public String resolverCodigo(String codigo) {//Función para resolver un id_producto a partir del id o del código de barras
        String idProd = null;
        String sql = "SELECT id_producto FROM producto WHERE id_producto::varchar = ? OR codigo_barras = ? LIMIT 1;";
        try (Connection conexion = conectar()) {
            PreparedStatement pstm = conexion.prepareStatement(sql);
            pstm.setString(1, codigo);
            pstm.setString(2, codigo);
            ResultSet rs = pstm.executeQuery();
            if (rs.next()) idProd = rs.getString("id_producto");
        } catch (SQLException e) {
            System.out.println("Error al resolver codigo: " + e.getMessage());
        }
        return idProd; // null si no existe
    }

    public String[] obtenerProductoParaCompra(String idProducto) {
        // [0]nombre [1]codigo_barras [2]unidad_compra [3]unidad_venta [4]factor [5]lleva_iva("t"/"f") [6]precio_menudeo
        String[] d = null;
        String sql = "SELECT nombre, COALESCE(codigo_barras,'') cb, COALESCE(unidad_compra,'') uc, "
                   + "COALESCE(unidad_venta,'') uv, COALESCE(factor_conversion,1) f, COALESCE(lleva_iva,false) iva, "
                   + "COALESCE(precio_menudeo,0) pm FROM producto WHERE id_producto = ?;";
        try (Connection conexion = conectar()) {
            PreparedStatement pstm = conexion.prepareStatement(sql);
            pstm.setString(1, idProducto);
            ResultSet rs = pstm.executeQuery();
            if (rs.next()) {
                d = new String[]{ rs.getString("nombre"), rs.getString("cb"), rs.getString("uc"),
                    rs.getString("uv"), rs.getString("f"), rs.getBoolean("iva") ? "t" : "f",
                    String.valueOf(rs.getDouble("pm")) };
            }
        } catch (SQLException e) {
            System.out.println("Error al leer producto: " + e.getMessage());
        }
        return d; // null si no existe
    }

    public boolean codigoBarrasDuplicado(String codigoBarras, String idExcluir) {//Función para verificar si un código de barras ya está en uso por otro producto
        if (codigoBarras == null || codigoBarras.trim().isEmpty()) return false;
        String sql = "SELECT 1 FROM producto WHERE codigo_barras = ? AND id_producto <> ? LIMIT 1;";
        boolean dup = false;
        try (Connection conexion = conectar()) {
            PreparedStatement pstm = conexion.prepareStatement(sql);
            pstm.setString(1, codigoBarras.trim());
            pstm.setString(2, idExcluir == null ? "" : idExcluir);
            ResultSet rs = pstm.executeQuery();
            dup = rs.next();
        } catch (SQLException e) {
            System.out.println("Error al verificar codigo de barras: " + e.getMessage());
        }
        return dup;
    }

    public boolean crearProductoDesdeCompra(String[] d, Integer idCategoria, double maxDescuento) {
        // d: [0]id_producto [1]nombre [2]codigo_barras [3]unidad_compra [4]unidad_venta
        //    [5]factor [6]lleva_iva("t"/"f") [7]precio_menudeo [8]precio_mayoreo [9]precio_compra
        boolean ok = false;
        String sql = "INSERT INTO producto(id_producto, nombre, cantidad, precio_mayoreo, precio_menudeo, "
                   + "max_descuento, codigo_barras, lleva_iva, unidad_compra, unidad_venta, factor_conversion, precio_compra, id_categoria) "
                   + "VALUES (?,?,0,?,?,?,?,?,?,?,?,?,?);";
        try (Connection conexion = conectar()) {
            PreparedStatement pstm = conexion.prepareStatement(sql);
            pstm.setString(1, d[0]);
            pstm.setString(2, d[1]);
            pstm.setDouble(3, Double.parseDouble(d[8]));
            pstm.setDouble(4, Double.parseDouble(d[7]));
            pstm.setDouble(5, maxDescuento);
            pstm.setString(6, d[2].isEmpty() ? null : d[2]);
            pstm.setBoolean(7, "t".equals(d[6]));
            pstm.setString(8, d[3]);
            pstm.setString(9, d[4]);
            pstm.setDouble(10, Double.parseDouble(d[5]));
            pstm.setDouble(11, Double.parseDouble(d[9]));
            if (idCategoria != null && idCategoria > 0) {
                pstm.setInt(12, idCategoria);
            } else {
                pstm.setNull(12, Types.INTEGER);
            }
            pstm.executeUpdate();
            ok = true;
        } catch (SQLException e) {
            Mise.JOption(e.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
        return ok;
    }

    public void insertarProductoConCodigoYCategoria(String[] datos, Integer idCategoria, double maxDescuento) {//Función para insertar un producto con código y categoría
        String instruccion = "INSERT INTO producto(id_producto, nombre, cantidad, precio_mayoreo, precio_menudeo, id_categoria, max_descuento) VALUES (?,?,?,?,?,?,?);";
        try (Connection conexion = conectar()) {
            PreparedStatement pstm = conexion.prepareStatement(instruccion);
            pstm.setString(1, datos[0]);
            pstm.setString(2, datos[1]);
            pstm.setInt(3, Integer.parseInt(datos[2]));
            pstm.setDouble(4, Double.parseDouble(datos[3]));
            pstm.setDouble(5, Double.parseDouble(datos[4]));
            if (idCategoria != null && idCategoria > 0) {
                pstm.setInt(6, idCategoria);
            } else {
                pstm.setNull(6, Types.INTEGER);
            }
            pstm.setDouble(7, maxDescuento);
            pstm.executeUpdate();
        } catch (SQLException e) {
            GestorErrores.manejar(e);
        }
    }

    public void actualizarProductoConCategoria(String ideprod, String[] datos, Integer idCategoria, double maxDescuento) {//Función para actualizar un producto con categoría
        String instruccion = "UPDATE producto SET nombre=?, cantidad=?, precio_mayoreo=?, precio_menudeo=?, id_categoria=?, max_descuento=? WHERE id_producto=?;";
        try (Connection conexion = conectar()) {
            PreparedStatement pstm = conexion.prepareStatement(instruccion);
            pstm.setString(1, datos[0]);
            pstm.setInt(2, Integer.parseInt(datos[1]));
            pstm.setDouble(3, Double.parseDouble(datos[2]));
            pstm.setDouble(4, Double.parseDouble(datos[3]));
            if (idCategoria != null && idCategoria > 0) {
                pstm.setInt(5, idCategoria);
            } else {
                pstm.setNull(5, Types.INTEGER);
            }
            pstm.setDouble(6, maxDescuento);
            pstm.setString(7, ideprod);
            pstm.executeUpdate();
        } catch (SQLException e) {
            GestorErrores.manejar(e);
        }
    }

    public void actualizarCamposCompraProducto(String idProducto, String codigoBarras, boolean llevaIva,
            String unidadCompra, String unidadVenta, double factor) {
        String sql = "UPDATE producto SET codigo_barras=?, lleva_iva=?, unidad_compra=?, unidad_venta=?, factor_conversion=? WHERE id_producto=?;";
        try (Connection conexion = conectar()) {
            PreparedStatement pstm = conexion.prepareStatement(sql);
            pstm.setString(1, codigoBarras == null || codigoBarras.isEmpty() ? null : codigoBarras);
            pstm.setBoolean(2, llevaIva);
            pstm.setString(3, unidadCompra);
            pstm.setString(4, unidadVenta);
            pstm.setDouble(5, factor);
            pstm.setString(6, idProducto);
            pstm.executeUpdate();
        } catch (SQLException e) {
            Mise.JOption(e.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    public double obtenerMaxDescuento(String idProducto) {//Función para obtener el descuento máximo de un producto
        double maxDesc = 100.0;
        String instruccion = "SELECT max_descuento FROM producto WHERE id_producto = ?;";
        try (Connection conexion = conectar()) {
            PreparedStatement pstm = conexion.prepareStatement(instruccion);
            pstm.setString(1, idProducto);
            ResultSet rs = pstm.executeQuery();
            if (rs.next()) {
                maxDesc = rs.getDouble("max_descuento");
            }
        } catch (SQLException e) {
            GestorErrores.manejar(e);
        }
        return maxDesc;
    }

    // CONSULTAS PARA LLENAR TABLAS DE LA UI

    public ResultSet obtenerProductosActivos() {//Productos activos ordenados por código
        return consultarONulo("SELECT * FROM producto WHERE estatus = 'Activo' ORDER BY id_producto");
    }

    public ResultSet buscarProductosActivos(String texto) {//Productos activos cuyo nombre o código contiene el texto
        if (texto == null || texto.isEmpty()) return obtenerProductosActivos();
        String like = "%" + texto + "%";
        return consultarONulo("SELECT * FROM producto WHERE estatus = 'Activo' AND (nombre LIKE ? OR CAST(id_producto AS TEXT) LIKE ?) ORDER BY id_producto",
                like, like);
    }

    public ResultSet obtenerProductosPocos() {//Productos activos con 12 piezas o menos
        return consultarONulo("SELECT * FROM producto WHERE estatus = 'Activo' AND cantidad<=12 ORDER BY id_producto");
    }

    public ResultSet buscarInventario(String texto) {//Inventario (con nombre de categoría); texto vacío = todo
        String base = "SELECT p.*, c.nombre AS categoria_nombre FROM producto p LEFT JOIN categoria c ON p.id_categoria = c.id_categoria WHERE p.estatus = 'Activo' ";
        if (texto == null || texto.isEmpty()) return consultarONulo(base + "ORDER BY p.id_producto");
        String like = "%" + texto + "%";
        return consultarONulo(base + "AND (p.nombre LIKE ? OR CAST(p.id_producto AS TEXT) LIKE ?) ORDER BY p.id_producto", like, like);
    }

    public ResultSet obtenerInventarioPorCategoria(int idCategoria) {//Inventario activo de una categoría
        return consultarONulo("SELECT p.*, c.nombre AS categoria_nombre FROM producto p LEFT JOIN categoria c ON p.id_categoria = c.id_categoria "
                + "WHERE p.id_categoria = ? AND p.estatus = 'Activo' ORDER BY p.id_producto", idCategoria);
    }

    public ResultSet obtenerInventarioSinCategoria() {//Respaldo para bases sin la tabla categoria
        return consultarONulo("SELECT *, '' AS categoria_nombre FROM producto WHERE estatus = 'Activo' ORDER BY id_producto");
    }

    public Double obtenerPrecioCompra(String idProducto) {//Precio de compra actual; null si el producto no existe o falla la consulta
        try {
            ResultSet rs = consultar("SELECT COALESCE(precio_compra,0) AS pc FROM producto WHERE id_producto = ?", idProducto);
            return rs.next() ? rs.getDouble("pc") : null;
        } catch (SQLException e) {
            GestorErrores.registrar(e);
            return null;
        }
    }
}
