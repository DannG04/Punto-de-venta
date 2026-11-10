package puntoventa.db;

import puntoventa.util.*;

import java.sql.*;

/**
 * DAO de Reportes: corte y totales del día, reporte diario, estado de
 * resultados y kardex de producto.
 *
 * @author mayra
 */
public class ReporteDAO extends BaseDAO {

    public ResultSet corteDiario(java.time.LocalDate fecha) {//Función para obtener las ventas del día con forma de pago y empleado
        ResultSet rs = null;
        String instruccion =
            "SELECT v.id_venta, " +
            "CAST(v.fecha_venta AS VARCHAR) AS hora, " +
            "COALESCE(e.nombre, v.id_empleado) AS empleado, " +
            "COALESCE((SELECT SUM(vd.precio_total) FROM venta_detalle vd WHERE vd.id_venta = v.id_venta), 0) AS subtotal, " +
            "GREATEST(0, COALESCE((SELECT SUM(vd.precio_total) FROM venta_detalle vd WHERE vd.id_venta = v.id_venta), 0) - v.total_venta) AS descuento, " +
            "v.total_venta, " +
            "COALESCE(v.forma_pago, 'Efectivo') AS forma_pago " +
            "FROM venta v " +
            "LEFT JOIN empleado e ON v.id_empleado = e.id_empleado " +
            "WHERE v.fecha_venta = ? " +
            "ORDER BY v.id_venta";
        try {
            Connection conexion = DriverManager.getConnection(url + nameBD, usuario, contra);
            PreparedStatement pstm = conexion.prepareStatement(instruccion);
            pstm.setDate(1, java.sql.Date.valueOf(fecha));
            rs = pstm.executeQuery();
        } catch (SQLException e) {
            GestorErrores.manejar(e);
        }
        return rs;
    }

    public ResultSet totalesDia(java.time.LocalDate fecha) {//Función para obtener los 5 totales del día
        return reporte_diario(fecha);
    }

    public ResultSet buscarProductoKardex(String filtro) {//Función para buscar productos por nombre o código
        ResultSet rs = null;
        String instruccion = "SELECT id_producto, nombre, cantidad FROM producto " +
            "WHERE estatus = 'Activo' AND (LOWER(nombre) LIKE LOWER(?) OR LOWER(id_producto) LIKE LOWER(?)) " +
            "ORDER BY nombre LIMIT 20";
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

    public ResultSet kardexProducto(String idProducto, java.time.LocalDate desde, java.time.LocalDate hasta) {//Función para obtener el kardex de un producto filtrado por fechas
        ResultSet rs = null;
        String instruccion =
            "SELECT k.fecha, k.tipo_movimiento, k.cantidad, " +
            "k.existencia_anterior, k.existencia_posterior, k.referencia, " +
            "COALESCE(e.nombre, k.id_empleado) AS empleado " +
            "FROM kardex k " +
            "LEFT JOIN empleado e ON k.id_empleado = e.id_empleado " +
            "WHERE k.id_producto = ? " +
            "AND k.fecha::date >= ? AND k.fecha::date <= ? " +
            "ORDER BY k.fecha DESC";
        try {
            Connection conexion = DriverManager.getConnection(url + nameBD, usuario, contra);
            PreparedStatement pstm = conexion.prepareStatement(instruccion);
            pstm.setString(1, idProducto);
            pstm.setDate(2, java.sql.Date.valueOf(desde));
            pstm.setDate(3, java.sql.Date.valueOf(hasta));
            rs = pstm.executeQuery();
        } catch (SQLException e) {
            GestorErrores.manejar(e);
        }
        return rs;
    }

    public double ObtenerDato(String nombreCol) {//Función para obtener un dato de la base de datos
        double total = 0;
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = DriverManager.getConnection(url + nameBD, usuario, contra);
            ps = conn.prepareStatement("SELECT * FROM estado_resultados(CAST(? AS NUMERIC))");
            ps.setDouble(1, 0.00);
            rs = ps.executeQuery();

            if (rs.next()) {
                total = rs.getDouble(nombreCol);
            }

        } catch (SQLException e) {
            GestorErrores.registrar(e);
        } finally {
            try {
                if (rs != null)
                    rs.close();
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException e) {
                GestorErrores.registrar(e);
            }
        }
        return total;
    }

    public ResultSet reporte_diario() {//Función para obtener el reporte diario
        ResultSet rs = null;
        try {
            Connection conexion = DriverManager.getConnection(url + nameBD, usuario, contra);
            CallableStatement cstm = conexion.prepareCall("{call reporte_diario()}");
            rs = cstm.executeQuery();
            conexion.close();
        } catch (SQLException e) {
            GestorErrores.manejar(e);
        }
        return rs;
    }

    public ResultSet reporte_diario(java.time.LocalDate fecha) {//Función para obtener el reporte diario por fecha
        ResultSet rs = null;
        try {
            Connection conexion = DriverManager.getConnection(url + nameBD, usuario, contra);
            CallableStatement cstm = conexion.prepareCall("{call reporte_diario(?)}");
            cstm.setDate(1, java.sql.Date.valueOf(fecha));
            rs = cstm.executeQuery();
            conexion.close();
        } catch (SQLException e) {
            GestorErrores.manejar(e);
        }
        return rs;
    }
}
