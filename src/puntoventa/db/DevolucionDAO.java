package puntoventa.db;

import puntoventa.util.*;

import java.sql.*;

/**
 * DAO del dominio Devoluciones: encabezado y detalle (con kardex).
 *
 * @author mayra
 */
public class DevolucionDAO extends BaseDAO {

    public String insertarDevolucion(String[] campos) {//Función para insertar una devolución
        String idDev = "";
        try {
            Connection conexion = DriverManager.getConnection(url + nameBD, usuario, contra);
            CallableStatement cstm = conexion.prepareCall("{call reg_devolucion(?,?)}");
            cstm.registerOutParameter(1, Types.VARCHAR);
            cstm.setString(1, campos[0]);
            cstm.setObject(2, campos[1], Types.NUMERIC);
            cstm.execute();
            idDev = cstm.getString(1);
            conexion.close();
        } catch (SQLException e) {
            GestorErrores.manejar(e);
        }
        return idDev;
    }

    public void eliminarDevolucion(String idDev) {//Función para eliminar una devolución
        try {
            Connection conexion = DriverManager.getConnection(url + nameBD, usuario, contra);
            CallableStatement cstm = conexion.prepareCall("{call elim_devolucion(?)}");
            cstm.setString(1, idDev);
            cstm.execute();
            conexion.close();
        } catch (SQLException e) {
            GestorErrores.manejar(e);
        }
    }

    public void insertarProdDevolucion(String[] campos, boolean ac) {//Función para insertar un producto en una devolución
        try {
            Connection conexion = DriverManager.getConnection(url + nameBD, usuario, contra);
            // Obtener id_empleado de la venta original para el kardex
            String idEmpDev = "";
            PreparedStatement psEmp = conexion.prepareStatement(
                "SELECT v.id_empleado FROM devolucion_ventas dv " +
                "JOIN venta v ON v.id_venta = dv.id_venta " +
                "WHERE dv.id_devolucion = ?");
            psEmp.setString(1, campos[0]);
            ResultSet rsEmp = psEmp.executeQuery();
            if (rsEmp.next()) idEmpDev = rsEmp.getString(1);
            // Establecer variables de sesión para el trigger kardex
            Statement stmtSet = conexion.createStatement();
            stmtSet.execute("SET kardex.tipo = 'Devolucion'");
            stmtSet.execute("SET kardex.referencia = '" + campos[0] + "'");
            if (!idEmpDev.isEmpty())
                stmtSet.execute("SET kardex.empleado = '" + idEmpDev + "'");
            CallableStatement cstm = conexion.prepareCall("{call reg_devolucion_prod(?,?,?::id_producto_dominio,?,?)}");
            cstm.setString(1, campos[0]);
            cstm.setString(2, campos[1]);
            cstm.setString(3, campos[2]);
            cstm.setBoolean(4, ac);
            cstm.setObject(5, campos[3], Types.INTEGER);
            cstm.execute();
            conexion.close();
        } catch (SQLException e) {
            GestorErrores.manejar(e);
        }
    }

    public void eliminarProdDevolucion(String[] campos) {//Función para eliminar un producto de una devolución
        try {
            Connection conexion = DriverManager.getConnection(url + nameBD, usuario, contra);
            CallableStatement cstm = conexion.prepareCall("{call elim_devolucion_prod(?,?::id_producto_dominio)}");
            cstm.setString(1, campos[0]);
            cstm.setString(2, campos[1]);
            cstm.execute();
            conexion.close();
        } catch (SQLException e) {
            GestorErrores.manejar(e);
        }
    }
}
