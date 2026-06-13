package puntoventa.db;

import puntoventa.util.*;

import java.sql.*;

/**
 * DAO del dominio Empleado: alta/baja, autenticación y selección de vendedores.
 *
 * @author mayra
 */
public class EmpleadoDAO extends BaseDAO {

    public boolean insertarEmpleado(String[] campos) {//Función para insertar un empleado
        boolean band = false;
        String columnas = "empleado(id_empleado, nombre, puesto, telefono, usuario, contrasenia)";
        String instruccion = "INSERT INTO " + columnas + " VALUES(?,?,?,?,?,?);";
        try {
            Connection conexion = DriverManager.getConnection(url + nameBD, usuario, contra);
            PreparedStatement pstm = conexion.prepareStatement(instruccion);
            pstm.setString(1, campos[0]);
            pstm.setString(2, campos[1]);
            pstm.setObject(3, campos[2], Types.OTHER);
            pstm.setString(4, campos[3]);
            pstm.setString(5, campos[4]);
            pstm.setString(6, campos[5]);
            pstm.executeUpdate();
            conexion.close();
            band = true;
        } catch (SQLException e) {
            GestorErrores.manejar(e);
        }
        return band;
    }

    public boolean actualizarEmpleado(String[] campos, String idEm) {//Función para actualizar un empleado
        boolean band = false;
        String columnas = "SET puesto=?, telefono=?";
        String instruccion = "UPDATE empleado " + columnas + " WHERE id_empleado=?;";
        try {
            Connection conexion = DriverManager.getConnection(url + nameBD, usuario, contra);
            PreparedStatement pstm = conexion.prepareStatement(instruccion);
            pstm.setObject(1, campos[0], Types.OTHER);
            pstm.setString(2, campos[1]);
            pstm.setString(3, idEm);
            pstm.executeUpdate();
            conexion.close();
            band = true;
        } catch (SQLException e) {
            GestorErrores.manejar(e);
        }
        return band;
    }

    public boolean inactivarEmpleado(String idEm, boolean act) {//Función para inactivar un empleado
        boolean band = false;
        try {
            Connection conexion = DriverManager.getConnection(url + nameBD, usuario, contra);
            CallableStatement cstm = conexion.prepareCall("{call inactivar_empleado(?::curp_dominio,?)}");
            cstm.setString(1, idEm);
            cstm.setBoolean(2, act);
            cstm.execute();
            conexion.close();
            band = true;
        } catch (SQLException e) {
            GestorErrores.manejar(e);
        }
        return band;
    }

    public boolean verificarUsuario(String uss) {//Función para verificar si un usuario ya existe
        boolean band = false;
        try {
            Connection conexion = DriverManager.getConnection(url + nameBD, usuario, contra);
            CallableStatement cstm = conexion.prepareCall("{call verif_user(?)}");
            cstm.setString(1, uss);
            ResultSet rs = cstm.executeQuery();
            while(rs.next()){
                band = rs.getBoolean(1);
            }
            conexion.close();
        } catch (SQLException e) {
            GestorErrores.manejar(e);
        }
        return band;
    }

    public String[] idEmpleado(String uss, String paswor) {//Función para obtener los datos de un empleado
        String[] dates = new String[4];
        try {
            Connection conexion = DriverManager.getConnection(url + nameBD, usuario, contra);
            CallableStatement cstm = conexion.prepareCall("{call ide_emp(?,?)}");
            cstm.setString(1, uss);
            cstm.setString(2, paswor);
            ResultSet rs = cstm.executeQuery();
            while(rs.next()){
                dates[0] = rs.getString("ide");
                dates[1] = rs.getString("neim");
                dates[2] = rs.getString("pues");
                dates[3] = rs.getString("tel");
            }
            conexion.close();
        } catch (SQLException e) {
            GestorErrores.manejar(e);
        }
        return dates;
    }

    public ResultSet seleccionarVendedor(String idVendedor) {//Función para seleccionar un vendedor
        ResultSet resultado = null;
        try {
            Connection conexion = DriverManager.getConnection(url + nameBD, usuario, contra);
            PreparedStatement consulta = conexion.prepareStatement(
                    "SELECT nombre FROM empleado where id_empleado = '" + idVendedor + "'");
            resultado = consulta.executeQuery();
        } catch (Exception e) {
            GestorErrores.registrar(e);
        }
        return resultado;
    }
}
