package puntoventa.db;

import puntoventa.util.*;

import java.sql.*;

/**
 * DAO de Reportes: corte y totales del día, reporte diario y estado de
 * resultados. El kardex está en KardexDAO.
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
        try (Connection conexion = conectar()) {
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

    public double ObtenerDato(String nombreCol) {//Función para obtener un dato de la base de datos
        double total = 0;
        try (Connection conn = conectar();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM estado_resultados(CAST(? AS NUMERIC))")) {
            ps.setDouble(1, 0.00);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    total = rs.getDouble(nombreCol);
                }
            }
        } catch (SQLException e) {
            GestorErrores.registrar(e);
        }
        return total;
    }

    public ResultSet reporte_diario() {//Función para obtener el reporte diario
        ResultSet rs = null;
        try (Connection conexion = conectar()) {
            CallableStatement cstm = conexion.prepareCall("{call reporte_diario()}");
            rs = cstm.executeQuery();
        } catch (SQLException e) {
            GestorErrores.manejar(e);
        }
        return rs;
    }

    public ResultSet reporte_diario(java.time.LocalDate fecha) {//Función para obtener el reporte diario por fecha
        ResultSet rs = null;
        try (Connection conexion = conectar()) {
            CallableStatement cstm = conexion.prepareCall("{call reporte_diario(?)}");
            cstm.setDate(1, java.sql.Date.valueOf(fecha));
            rs = cstm.executeQuery();
        } catch (SQLException e) {
            GestorErrores.manejar(e);
        }
        return rs;
    }

    public ResultSet balanceGeneral(java.time.LocalDate hasta) {//Caja, inventario y clientes desde el 1 de enero del año de la fecha
        return consultarONulo("SELECT * from balance_general(?, ?)", "01/01/" + hasta.getYear(), hasta.toString());
    }

    public ResultSet anticipoClientes() {//Anticipos de clientes (apartados) para el balance general
        return consultarONulo("SELECT anticipo_clientes()");
    }
}
