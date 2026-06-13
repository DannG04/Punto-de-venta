package puntoventa.db;

import puntoventa.util.*;

import java.sql.*;

/**
 * DAO del dominio Compras: encabezado, detalle (con kardex), factura completa
 * transaccional, borradores y validación de folios.
 *
 * @author mayra
 */
public class CompraDAO extends BaseDAO {

    public String insertarCompra(String[] campos) {//Función para insertar una compra
        String idCompra = "";
        try {
            Connection conexion = DriverManager.getConnection(url + nameBD, usuario, contra);
            CallableStatement cstm = conexion.prepareCall("{call reg_compra(?::curp_dominio,?,?)}");
            cstm.registerOutParameter(1, Types.VARCHAR);
            cstm.setString(1, campos[0]);
            cstm.setString(2, campos[1]);
            cstm.setObject(3, campos[2], Types.NUMERIC);
            cstm.execute();
            idCompra = cstm.getString(1);
            conexion.close();
        } catch (SQLException e) {
            GestorErrores.manejar(e);
        }
        return idCompra;
    }

    public void actualizarCompra(String[] campos) {//Función para actualizar una compra
        String columnas = "descripcion=?, monto=?";
        String instruccion = "UPDATE compras SET " + columnas + " WHERE id_compra=?;";
        try {
            Connection conexion = DriverManager.getConnection(url + nameBD, usuario, contra);
            PreparedStatement pstm = conexion.prepareCall(instruccion);
            pstm.setString(1, campos[0]);
            pstm.setDouble(2, Double.parseDouble(campos[1]));
            pstm.setString(3, campos[2]);
            pstm.executeUpdate();
            conexion.close();
        } catch (SQLException e) {
            GestorErrores.manejar(e);
        }
    }

    public void eliminarCompra(String idCom) {//Función para eliminar una compra
        try {
            Connection conexion = DriverManager.getConnection(url + nameBD, usuario, contra);
            CallableStatement cstm = conexion.prepareCall("{call elim_compra(?)}");
            cstm.setString(1, idCom);
            cstm.execute();
            conexion.close();
        } catch (SQLException e) {
            GestorErrores.manejar(e);
        }
    }

    public void insertarProdCompra(String[] campos, boolean ac) {//Función para insertar un producto en una compra
        try {
            Connection conexion = DriverManager.getConnection(url + nameBD, usuario, contra);
            // Obtener id_empleado de la compra para el kardex
            String idEmpCompra = "";
            PreparedStatement psEmp = conexion.prepareStatement("SELECT id_empleado FROM compras WHERE id_compra = ?");
            psEmp.setString(1, campos[0]);
            ResultSet rsEmp = psEmp.executeQuery();
            if (rsEmp.next()) idEmpCompra = rsEmp.getString(1);
            // Establecer variables de sesión para el trigger kardex
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
            GestorErrores.manejar(e);
        }
    }

    public void eliminarProdCompra(String[] campos) {//Función para eliminar un producto de una compra
        try {
            Connection conexion = DriverManager.getConnection(url + nameBD, usuario, contra);
            CallableStatement cstm = conexion.prepareCall("{call elim_compra_prod(?,?::id_producto_dominio)}");
            cstm.setString(1, campos[0]);
            cstm.setString(2, campos[1]);
            cstm.execute();
            conexion.close();
        } catch (SQLException e) {
            GestorErrores.manejar(e);
        }
    }

    // Guarda la factura de compra COMPLETA en una sola transacción:
    // encabezado + productos nuevos + renglones (stock/kardex vía reg_compra_prod) + totales.
    // Si cualquier paso falla, se revierte todo (no quedan productos huérfanos ni stock a medias).
    // Devuelve el id_compra generado, o null si falló.
    public String guardarFacturaCompleta(String[] cab, int idProveedor,
            java.util.List<RenglonCompra> renglones, double subtotal, double iva, double total,
            int idBorradorAEliminar) {
        // cab: [0]id_empleado [1]descripcion [2]folio [3]fecha(yyyy-MM-dd o "") [4]origen
        // idBorradorAEliminar: si > 0, borra ese borrador dentro de la MISMA transacción
        //                       (terminar el borrador = crear la compra real y eliminarlo, todo o nada)
        Connection conexion = null;
        try {
            conexion = DriverManager.getConnection(url + nameBD, usuario, contra);
            conexion.setAutoCommit(false);

            // 1. Encabezado de la compra
            String sqlCab = "INSERT INTO compras(id_empleado, descripcion, monto, id_proveedor, folio_proveedor, fecha_factura, origen) "
                          + "VALUES(?::curp_dominio, ?, ?::numeric, ?, ?, ?::date, ?) RETURNING id_compra;";
            String idCompra;
            try (PreparedStatement pstm = conexion.prepareStatement(sqlCab)) {
                pstm.setString(1, cab[0]);
                pstm.setString(2, cab[1]);
                pstm.setObject(3, total, Types.NUMERIC);
                if (idProveedor > 0) pstm.setInt(4, idProveedor); else pstm.setNull(4, Types.INTEGER);
                pstm.setString(5, cab[2].isEmpty() ? null : cab[2]);
                pstm.setString(6, cab[3].isEmpty() ? null : cab[3]);
                pstm.setString(7, cab[4].isEmpty() ? null : cab[4]);
                ResultSet rs = pstm.executeQuery();
                if (!rs.next()) { conexion.rollback(); return null; }
                idCompra = rs.getString("id_compra");
            }

            // 2. Variables de sesión para el trigger de kardex (misma conexión/transacción)
            try (Statement st = conexion.createStatement()) {
                st.execute("SET kardex.tipo = 'Compra'");
                st.execute("SET kardex.referencia = '" + idCompra + "'");
                if (cab[0] != null && !cab[0].isEmpty())
                    st.execute("SET kardex.empleado = '" + cab[0] + "'");
            }

            String sqlProd = "INSERT INTO producto(id_producto, nombre, cantidad, precio_mayoreo, precio_menudeo, "
                           + "max_descuento, codigo_barras, lleva_iva, unidad_compra, unidad_venta, factor_conversion, precio_compra, id_categoria) "
                           + "VALUES (?,?,0,?,?,?,?,?,?,?,?,?,?);";

            // 3. Productos nuevos + renglones
            for (RenglonCompra r : renglones) {
                if (r.nuevo) {
                    try (PreparedStatement pp = conexion.prepareStatement(sqlProd)) {
                        pp.setString(1, r.idProducto);
                        pp.setString(2, r.nombre);
                        pp.setDouble(3, Double.parseDouble(r.precioMayoreo));
                        pp.setDouble(4, Double.parseDouble(r.precioMenudeo));
                        pp.setDouble(5, r.maxDescuento);
                        pp.setString(6, (r.codigoBarras == null || r.codigoBarras.isEmpty()) ? null : r.codigoBarras);
                        pp.setBoolean(7, r.llevaIva);
                        pp.setString(8, r.unidadCompra);
                        pp.setString(9, r.unidadVenta);
                        pp.setDouble(10, Double.parseDouble(r.factor));
                        pp.setDouble(11, Double.parseDouble(r.precioCompra));
                        if (r.idCategoria != null && r.idCategoria > 0) pp.setInt(12, r.idCategoria);
                        else pp.setNull(12, Types.INTEGER);
                        pp.executeUpdate();
                    }
                }
                try (CallableStatement cstm = conexion.prepareCall("{call reg_compra_prod(?,?::id_producto_dominio,?,?,?,?)}")) {
                    cstm.setString(1, idCompra);
                    cstm.setString(2, r.idProducto);
                    cstm.setObject(3, r.precioCompra, Types.NUMERIC);
                    cstm.setObject(4, r.cantidad, Types.INTEGER);
                    cstm.setObject(5, r.factor, Types.NUMERIC);
                    cstm.setBoolean(6, true);  // acumula si el código se repite dentro de la misma factura
                    cstm.execute();
                }
            }

            // 4. Totales (subtotal/iva; monto ya quedó en el encabezado)
            try (PreparedStatement pt = conexion.prepareStatement(
                    "UPDATE compras SET subtotal = ?, iva = ?, monto = ? WHERE id_compra = ?;")) {
                pt.setDouble(1, subtotal);
                pt.setDouble(2, iva);
                pt.setDouble(3, total);
                pt.setString(4, idCompra);
                pt.executeUpdate();
            }

            // 5. Si venía de un borrador, eliminarlo (cascade borra sus renglones)
            if (idBorradorAEliminar > 0) {
                try (PreparedStatement pb = conexion.prepareStatement(
                        "DELETE FROM compra_borrador WHERE id_borrador = ?;")) {
                    pb.setInt(1, idBorradorAEliminar);
                    pb.executeUpdate();
                }
            }

            conexion.commit();
            return idCompra;
        } catch (SQLException e) {
            if (conexion != null) { try { conexion.rollback(); } catch (SQLException ig) {} }
            GestorErrores.manejar(e);
            return null;
        } finally {
            if (conexion != null) {
                try { conexion.setAutoCommit(true); conexion.close(); } catch (SQLException ig) {}
            }
        }
    }

    // Guarda (inserta o actualiza) un borrador de compra con sus renglones, en una transacción.
    // NO toca inventario ni kardex. Devuelve el id_borrador, o null si falló.
    public Integer guardarBorrador(String[] cab, int idProveedor, String totalFactura,
            java.util.List<RenglonCompra> renglones, int idBorradorExistente) {
        // cab: [0]id_empleado [1]descripcion [2]folio [3]fecha(yyyy-MM-dd o "") [4]origen
        Connection conexion = null;
        try {
            conexion = DriverManager.getConnection(url + nameBD, usuario, contra);
            conexion.setAutoCommit(false);

            Integer idBorrador = (idBorradorExistente > 0) ? idBorradorExistente : null;
            if (idBorrador == null) {
                String sql = "INSERT INTO compra_borrador(id_empleado, id_proveedor, folio_proveedor, "
                           + "fecha_factura, origen, descripcion, total_factura) "
                           + "VALUES(?,?,?,?::date,?,?,?::numeric) RETURNING id_borrador;";
                try (PreparedStatement pstm = conexion.prepareStatement(sql)) {
                    pstm.setString(1, cab[0]);
                    if (idProveedor > 0) pstm.setInt(2, idProveedor); else pstm.setNull(2, Types.INTEGER);
                    pstm.setString(3, cab[2].isEmpty() ? null : cab[2]);
                    pstm.setString(4, cab[3].isEmpty() ? null : cab[3]);
                    pstm.setString(5, cab[4].isEmpty() ? null : cab[4]);
                    pstm.setString(6, cab[1].isEmpty() ? null : cab[1]);
                    pstm.setString(7, (totalFactura == null || totalFactura.isEmpty()) ? null : totalFactura);
                    ResultSet rs = pstm.executeQuery();
                    if (!rs.next()) { conexion.rollback(); return null; }
                    idBorrador = rs.getInt("id_borrador");
                }
            } else {
                String sql = "UPDATE compra_borrador SET id_proveedor=?, folio_proveedor=?, fecha_factura=?::date, "
                           + "origen=?, descripcion=?, total_factura=?::numeric, fecha_modificacion=now() WHERE id_borrador=?;";
                try (PreparedStatement pstm = conexion.prepareStatement(sql)) {
                    if (idProveedor > 0) pstm.setInt(1, idProveedor); else pstm.setNull(1, Types.INTEGER);
                    pstm.setString(2, cab[2].isEmpty() ? null : cab[2]);
                    pstm.setString(3, cab[3].isEmpty() ? null : cab[3]);
                    pstm.setString(4, cab[4].isEmpty() ? null : cab[4]);
                    pstm.setString(5, cab[1].isEmpty() ? null : cab[1]);
                    pstm.setString(6, (totalFactura == null || totalFactura.isEmpty()) ? null : totalFactura);
                    pstm.setInt(7, idBorrador);
                    pstm.executeUpdate();
                }
                try (PreparedStatement pd = conexion.prepareStatement(
                        "DELETE FROM compra_borrador_producto WHERE id_borrador=?;")) {
                    pd.setInt(1, idBorrador);
                    pd.executeUpdate();
                }
            }

            String sqlLinea = "INSERT INTO compra_borrador_producto(id_borrador, linea, es_nuevo, id_producto, "
                            + "nombre, codigo_barras, unidad_compra, unidad_venta, factor, lleva_iva, precio_compra, "
                            + "cantidad, precio_menudeo, precio_mayoreo, max_descuento, id_categoria) "
                            + "VALUES(?,?,?,?,?,?,?,?,?::numeric,?,?::numeric,?::integer,?::numeric,?::numeric,?::numeric,?);";
            int linea = 1;
            for (RenglonCompra r : renglones) {
                try (PreparedStatement pl = conexion.prepareStatement(sqlLinea)) {
                    pl.setInt(1, idBorrador);
                    pl.setInt(2, linea++);
                    pl.setBoolean(3, r.nuevo);
                    pl.setString(4, r.idProducto);
                    pl.setString(5, r.nombre);
                    pl.setString(6, (r.codigoBarras == null || r.codigoBarras.isEmpty()) ? null : r.codigoBarras);
                    pl.setString(7, r.unidadCompra);
                    pl.setString(8, r.unidadVenta);
                    pl.setString(9, r.factor);
                    pl.setBoolean(10, r.llevaIva);
                    pl.setString(11, r.precioCompra);
                    pl.setString(12, r.cantidad);
                    pl.setString(13, (r.precioMenudeo == null || r.precioMenudeo.isEmpty()) ? null : r.precioMenudeo);
                    pl.setString(14, (r.precioMayoreo == null || r.precioMayoreo.isEmpty()) ? null : r.precioMayoreo);
                    pl.setObject(15, r.maxDescuento, Types.NUMERIC);
                    if (r.idCategoria != null && r.idCategoria > 0) pl.setInt(16, r.idCategoria);
                    else pl.setNull(16, Types.INTEGER);
                    pl.executeUpdate();
                }
            }

            conexion.commit();
            return idBorrador;
        } catch (SQLException e) {
            if (conexion != null) { try { conexion.rollback(); } catch (SQLException ig) {} }
            GestorErrores.manejar(e);
            return null;
        } finally {
            if (conexion != null) {
                try { conexion.setAutoCommit(true); conexion.close(); } catch (SQLException ig) {}
            }
        }
    }

    // Devuelve el encabezado de un borrador para retomarlo.
    // [0]id_proveedor [1]folio [2]fecha(yyyy-MM-dd o "") [3]origen [4]descripcion [5]total_factura(o "")
    public String[] obtenerCabeceraBorrador(int idBorrador) {
        String[] cab = null;
        String sql = "SELECT COALESCE(id_proveedor,0) idp, COALESCE(folio_proveedor,'') folio, "
                   + "COALESCE(to_char(fecha_factura,'YYYY-MM-DD'),'') fecha, COALESCE(origen,'') origen, "
                   + "COALESCE(descripcion,'') descripcion, COALESCE(total_factura::varchar,'') total "
                   + "FROM compra_borrador WHERE id_borrador=?;";
        try {
            Connection conexion = DriverManager.getConnection(url + nameBD, usuario, contra);
            PreparedStatement pstm = conexion.prepareStatement(sql);
            pstm.setInt(1, idBorrador);
            ResultSet rs = pstm.executeQuery();
            if (rs.next()) {
                cab = new String[]{ rs.getString("idp"), rs.getString("folio"), rs.getString("fecha"),
                    rs.getString("origen"), rs.getString("descripcion"), rs.getString("total") };
            }
            conexion.close();
        } catch (SQLException e) {
            System.out.println("Error al leer borrador: " + e.getMessage());
        }
        return cab;
    }

    // Devuelve los renglones de un borrador para retomarlo.
    public java.util.List<RenglonCompra> obtenerRenglonesBorrador(int idBorrador) {
        java.util.List<RenglonCompra> lista = new java.util.ArrayList<>();
        String sql = "SELECT * FROM compra_borrador_producto WHERE id_borrador=? ORDER BY linea;";
        try {
            Connection conexion = DriverManager.getConnection(url + nameBD, usuario, contra);
            PreparedStatement pstm = conexion.prepareStatement(sql);
            pstm.setInt(1, idBorrador);
            ResultSet rs = pstm.executeQuery();
            while (rs.next()) {
                RenglonCompra r = new RenglonCompra();
                r.nuevo = rs.getBoolean("es_nuevo");
                r.idProducto = rs.getString("id_producto");
                r.nombre = rs.getString("nombre");
                r.codigoBarras = rs.getString("codigo_barras") == null ? "" : rs.getString("codigo_barras");
                r.unidadCompra = rs.getString("unidad_compra");
                r.unidadVenta = rs.getString("unidad_venta");
                r.factor = rs.getString("factor") == null ? "1" : rs.getString("factor");
                r.llevaIva = rs.getBoolean("lleva_iva");
                r.precioCompra = rs.getString("precio_compra");
                r.cantidad = rs.getString("cantidad");
                r.precioMenudeo = rs.getString("precio_menudeo") == null ? "" : rs.getString("precio_menudeo");
                r.precioMayoreo = rs.getString("precio_mayoreo") == null ? "" : rs.getString("precio_mayoreo");
                r.maxDescuento = rs.getDouble("max_descuento");
                int cat = rs.getInt("id_categoria");
                r.idCategoria = rs.wasNull() ? null : cat;
                lista.add(r);
            }
            conexion.close();
        } catch (SQLException e) {
            System.out.println("Error al leer renglones de borrador: " + e.getMessage());
        }
        return lista;
    }

    public void eliminarBorrador(int idBorrador) {
        try {
            Connection conexion = DriverManager.getConnection(url + nameBD, usuario, contra);
            PreparedStatement pstm = conexion.prepareStatement("DELETE FROM compra_borrador WHERE id_borrador=?;");
            pstm.setInt(1, idBorrador);
            pstm.executeUpdate();
            conexion.close();
        } catch (SQLException e) {
            GestorErrores.manejar(e);
        }
    }

    public boolean folioYaRegistrado(int idProveedor, String folio) {//Función para verificar si un folio de proveedor ya fue registrado
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

    public void actualizarTotalesCompra(String idCompra, double subtotal, double iva, double total) {//Función para actualizar los totales (subtotal, iva, monto) de una compra
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
}
