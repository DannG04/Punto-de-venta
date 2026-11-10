package puntoventa.db;

import puntoventa.util.*;

import java.sql.*;

/**
 * DAO del dominio Cotizaciones: carrito (cotizacion_temp), guardado, consultas,
 * conversión a venta y cancelación.
 *
 * Llamada cruzada: convertirCotizacionAVenta persiste los descuentos en
 * venta_detalle, así que instancia un VentaDAO hermano.
 *
 * @author mayra
 */
public class CotizacionDAO extends BaseDAO {

    private final VentaDAO ventas = new VentaDAO();

    public ResultSet buscarProductosCotizacion(String filtro) {
        ResultSet rs = null;
        String instruccion = "SELECT id_producto, nombre, precio_menudeo, precio_mayoreo, cantidad FROM producto " +
            "WHERE estatus = 'Activo' AND (LOWER(nombre) LIKE LOWER(?) OR LOWER(id_producto) LIKE LOWER(?)) " +
            "ORDER BY nombre LIMIT 50";
        try {
            Connection conexion = DriverManager.getConnection(url + nameBD, usuario, contra);
            PreparedStatement pstm = conexion.prepareStatement(instruccion);
            pstm.setString(1, "%" + filtro + "%");
            pstm.setString(2, "%" + filtro + "%");
            rs = pstm.executeQuery();
        } catch (SQLException e) {
            GestorErrores.manejar(e);
        }
        return rs;
    }

    public void limpiarCotizacionTemp() {
        inst("DELETE FROM cotizacion_temp;");
    }

    public void insertarCotizacionTemp(String idProd, int cantidad, String tipoVenta, boolean acum) {
        try {
            Connection conn = DriverManager.getConnection(url + nameBD, usuario, contra);
            int cantExistente = 0;
            if (acum) {
                PreparedStatement chk = conn.prepareStatement(
                    "SELECT COALESCE(cantidad_prod,0) FROM cotizacion_temp WHERE id_producto=?");
                chk.setString(1, idProd);
                ResultSet rs = chk.executeQuery();
                if (rs.next()) cantExistente = rs.getInt(1);
            }
            PreparedStatement del = conn.prepareStatement("DELETE FROM cotizacion_temp WHERE id_producto=?");
            del.setString(1, idProd);
            del.executeUpdate();
            int cantFinal = cantExistente + cantidad;
            String priceCol = "Mayoreo".equals(tipoVenta) ? "precio_mayoreo" : "precio_menudeo";
            PreparedStatement ins = conn.prepareStatement(
                "INSERT INTO cotizacion_temp(id_producto, nombre_p, cantidad_prod, tipo_venta, precio_dado, precio_total, descuento_pct) " +
                "SELECT id_producto, nombre, ?, ?, " + priceCol + ", " + priceCol + "*?, 0 " +
                "FROM producto WHERE id_producto=?");
            ins.setInt(1, cantFinal);
            ins.setString(2, tipoVenta);
            ins.setInt(3, cantFinal);
            ins.setString(4, idProd);
            ins.executeUpdate();
            conn.close();
        } catch (SQLException e) {
            GestorErrores.manejar(e);
        }
    }

    public void eliminarCotizacionTemp(String idProd) {
        try {
            Connection conn = DriverManager.getConnection(url + nameBD, usuario, contra);
            PreparedStatement del = conn.prepareStatement("DELETE FROM cotizacion_temp WHERE id_producto=?");
            del.setString(1, idProd);
            del.executeUpdate();
            conn.close();
        } catch (SQLException e) {
            GestorErrores.manejar(e);
        }
    }

    public ResultSet mostrarCotizacionTemp() {
        return query("SELECT * FROM cotizacion_temp");
    }

    public double sumaCotizacionTemp() {
        double sum = 0.0;
        try {
            Connection conn = DriverManager.getConnection(url + nameBD, usuario, contra);
            ResultSet rs = conn.createStatement().executeQuery(
                "SELECT COALESCE(SUM(precio_total),0) FROM cotizacion_temp");
            if (rs.next()) sum = rs.getDouble(1);
            conn.close();
        } catch (SQLException e) {}
        return sum;
    }

    public void actualizarDescuentoCotiTemp(String idProd, double descuento) {
        String sql = "UPDATE cotizacion_temp SET descuento_pct=?, " +
            "precio_total=precio_dado*cantidad_prod*(1-?/100) WHERE id_producto=?";
        try {
            Connection conn = DriverManager.getConnection(url + nameBD, usuario, contra);
            PreparedStatement pstm = conn.prepareStatement(sql);
            pstm.setDouble(1, descuento);
            pstm.setDouble(2, descuento);
            pstm.setString(3, idProd);
            pstm.executeUpdate();
            conn.close();
        } catch (SQLException e) {
            GestorErrores.manejar(e);
        }
    }

    public String guardarCotizacion(String idEmp, String idCli, double total,
                                    java.sql.Date vigencia, String notas) {
        String idCoti = "";
        try {
            Connection conn = DriverManager.getConnection(url + nameBD, usuario, contra);
            PreparedStatement ins = conn.prepareStatement(
                "INSERT INTO cotizacion(id_empleado, id_cliente, total_cotizacion, fecha_vigencia, notas) " +
                "VALUES(?::CHAR(18), ?::CHAR(18), ?, ?, ?) RETURNING id_cotizacion");
            ins.setString(1, idEmp);
            ins.setString(2, idCli);
            ins.setDouble(3, total);
            if (vigencia != null) ins.setDate(4, vigencia);
            else ins.setNull(4, Types.DATE);
            ins.setString(5, (notas == null || notas.isEmpty()) ? null : notas);
            ResultSet rs = ins.executeQuery();
            if (rs.next()) idCoti = rs.getString(1);

            if (!idCoti.isEmpty()) {
                PreparedStatement det = conn.prepareStatement(
                    "INSERT INTO cotizacion_detalle(id_cotizacion, id_producto, nombre_p, cantidad, " +
                    "tipo_venta, precio_dado, descuento_pct, precio_total) " +
                    "SELECT ?, id_producto, nombre_p, cantidad_prod, tipo_venta, precio_dado, descuento_pct, precio_total " +
                    "FROM cotizacion_temp");
                det.setString(1, idCoti);
                det.executeUpdate();
                conn.createStatement().execute("DELETE FROM cotizacion_temp");
            }
            conn.close();
        } catch (SQLException e) {
            GestorErrores.manejar(e);
        }
        return idCoti;
    }

    public ResultSet obtenerCotizaciones(String filtro) {
        String sql =
            "SELECT c.id_cotizacion, TO_CHAR(c.fecha_cotizacion,'DD/MM/YYYY HH24:MI') AS fecha, " +
            "COALESCE(cl.nombre, 'Público general') AS cliente, " +
            "c.total_cotizacion, c.estatus, " +
            "COALESCE(TO_CHAR(c.fecha_vigencia,'DD/MM/YYYY'),'—') AS vigencia " +
            "FROM cotizacion c LEFT JOIN cliente cl ON c.id_cliente = cl.id_cliente ";
        if (filtro != null && !filtro.isEmpty()) {
            sql += "WHERE c.id_cotizacion ILIKE ? OR cl.nombre ILIKE ? OR c.estatus ILIKE ? ";
        }
        sql += "ORDER BY c.fecha_cotizacion DESC";
        try {
            Connection conn = DriverManager.getConnection(url + nameBD, usuario, contra);
            if (filtro != null && !filtro.isEmpty()) {
                PreparedStatement pstm = conn.prepareStatement(sql);
                String like = "%" + filtro + "%";
                pstm.setString(1, like); pstm.setString(2, like); pstm.setString(3, like);
                return pstm.executeQuery();
            } else {
                return conn.createStatement(
                    ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY).executeQuery(sql);
            }
        } catch (SQLException e) {
            GestorErrores.manejar(e);
        }
        return null;
    }

    public ResultSet obtenerDetalleCotizacion(String idCoti) {
        ResultSet rs = null;
        String sql = "SELECT id_producto, nombre_p, cantidad, tipo_venta, precio_dado, descuento_pct, precio_total " +
            "FROM cotizacion_detalle WHERE id_cotizacion=? ORDER BY id_cotizacion_detalle";
        try {
            Connection conn = DriverManager.getConnection(url + nameBD, usuario, contra);
            PreparedStatement pstm = conn.prepareStatement(sql);
            pstm.setString(1, idCoti);
            rs = pstm.executeQuery();
        } catch (SQLException e) {
            GestorErrores.manejar(e);
        }
        return rs;
    }

    public ResultSet obtenerCotizacionHeader(String idCoti) {
        ResultSet rs = null;
        String sql =
            "SELECT c.id_cotizacion, TO_CHAR(c.fecha_cotizacion,'DD/MM/YYYY HH24:MI') AS fecha, " +
            "COALESCE(cl.nombre,'Público general') AS cliente, c.total_cotizacion, c.estatus, " +
            "COALESCE(TO_CHAR(c.fecha_vigencia,'DD/MM/YYYY'),'') AS vigencia, " +
            "COALESCE(c.notas,'') AS notas, c.id_empleado " +
            "FROM cotizacion c LEFT JOIN cliente cl ON c.id_cliente = cl.id_cliente " +
            "WHERE c.id_cotizacion=?";
        try {
            Connection conn = DriverManager.getConnection(url + nameBD, usuario, contra);
            PreparedStatement pstm = conn.prepareStatement(sql);
            pstm.setString(1, idCoti);
            rs = pstm.executeQuery();
        } catch (SQLException e) {
            GestorErrores.manejar(e);
        }
        return rs;
    }

    public String convertirCotizacionAVenta(String idCoti, String formaPago) {
        String idVenta = "";
        try {
            Connection conn = DriverManager.getConnection(url + nameBD, usuario, contra);

            // Obtener datos de la cotizacion
            PreparedStatement sel = conn.prepareStatement(
                "SELECT id_empleado, id_cliente FROM cotizacion WHERE id_cotizacion=?");
            sel.setString(1, idCoti);
            ResultSet rsCoti = sel.executeQuery();
            if (!rsCoti.next()) { conn.close(); return ""; }
            String idEmp = rsCoti.getString("id_empleado");
            String idCli = rsCoti.getString("id_cliente");
            if (idCli == null || idCli.isEmpty()) idCli = "XAXX111111HCCXXXX0";

            // Copiar cotizacion_detalle a venta_temp
            conn.createStatement().execute("DELETE FROM venta_temp");
            PreparedStatement copyTemp = conn.prepareStatement(
                "INSERT INTO venta_temp(id_producto, nombre_p, cantidad_prod, tipo_venta, precio_dado, precio_total, descuento_pct) " +
                "SELECT id_producto, nombre_p, cantidad, tipo_venta::\"tipoVenta\", precio_dado, precio_total, descuento_pct " +
                "FROM cotizacion_detalle WHERE id_cotizacion=?");
            copyTemp.setString(1, idCoti);
            copyTemp.executeUpdate();

            // Crear venta con el stored procedure
            CallableStatement cstm = conn.prepareCall("{call realizar_venta(?::curp_dominio,?::curp_dominio)}");
            cstm.registerOutParameter(1, Types.VARCHAR);
            cstm.setString(1, idEmp);
            cstm.setString(2, idCli);
            cstm.execute();
            idVenta = cstm.getString(1);

            if (!idVenta.isEmpty()) {
                // Actualizar forma de pago
                PreparedStatement updVenta = conn.prepareStatement(
                    "UPDATE venta SET forma_pago=? WHERE id_venta=?");
                updVenta.setString(1, formaPago);
                updVenta.setString(2, idVenta);
                updVenta.executeUpdate();

                // Retroalimentar kardex
                String sqlKardex =
                    "UPDATE kardex SET referencia=?, id_empleado=?::CHAR(18) " +
                    "WHERE id_kardex IN (" +
                    "  SELECT DISTINCT ON (vd.id_producto) k.id_kardex " +
                    "  FROM venta_detalle vd JOIN kardex k ON k.id_producto=vd.id_producto " +
                    "  WHERE vd.id_venta=? AND k.referencia IS NULL AND k.tipo_movimiento='Venta' " +
                    "  ORDER BY vd.id_producto, k.id_kardex DESC)";
                PreparedStatement psKardex = conn.prepareStatement(sqlKardex);
                psKardex.setString(1, idVenta);
                psKardex.setString(2, idEmp);
                psKardex.setString(3, idVenta);
                psKardex.executeUpdate();

                // Persistir descuentos en venta_detalle
                ResultSet rsDet = conn.prepareStatement(
                    "SELECT id_producto, descuento_pct FROM cotizacion_detalle WHERE id_cotizacion='" + idCoti + "' AND descuento_pct>0")
                    .executeQuery();
                while (rsDet.next()) {
                    ventas.actualizarDescuentoVenta(idVenta, rsDet.getString("id_producto"), rsDet.getDouble("descuento_pct"));
                }

                // Marcar cotizacion como convertida
                PreparedStatement updCoti = conn.prepareStatement(
                    "UPDATE cotizacion SET estatus='Convertida', id_venta_convertida=? WHERE id_cotizacion=?");
                updCoti.setString(1, idVenta);
                updCoti.setString(2, idCoti);
                updCoti.executeUpdate();
            }
            conn.createStatement().execute("DELETE FROM venta_temp");
            conn.close();
        } catch (SQLException e) {
            GestorErrores.manejar(e);
        }
        return idVenta;
    }

    public void cancelarCotizacion(String idCoti) {
        try {
            Connection conn = DriverManager.getConnection(url + nameBD, usuario, contra);
            PreparedStatement pstm = conn.prepareStatement(
                "UPDATE cotizacion SET estatus='Cancelada' WHERE id_cotizacion=?");
            pstm.setString(1, idCoti);
            pstm.executeUpdate();
            conn.close();
        } catch (SQLException e) {
            GestorErrores.manejar(e);
        }
    }
}
