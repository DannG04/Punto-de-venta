package puntoventa.db;

import puntoventa.util.*;

import java.sql.*;

/**
 * DAO del dominio Gastos: alta y actualización.
 *
 * @author mayra
 */
public class GastoDAO extends BaseDAO {

    public void insertarGasto(String[] campos) {//Función para insertar un gasto
        String columnas = "gastos(id_empleado, descripcion, monto)";
        String instruccion = "INSERT INTO " + columnas + " VALUES(?,?,?);";
        try {
            Connection conexion = DriverManager.getConnection(url + nameBD, usuario, contra);
            PreparedStatement pstm = conexion.prepareStatement(instruccion);
            pstm.setString(1, campos[0]);
            pstm.setString(2, campos[1]);
            pstm.setDouble(3, Double.parseDouble(campos[2]));
            pstm.executeUpdate();
            conexion.close();
        } catch (SQLException e) {
            GestorErrores.manejar(e);
        }
    }

    public void actualizarGasto(String idGasto, String[] campos) {//Función para actualizar un gasto
        String columnas = "SET descripcion=?, monto=?";
        String instruccion = "UPDATE gastos " + columnas + " WHERE id_gasto=?;";
        try {
            Connection conexion = DriverManager.getConnection(url + nameBD, usuario, contra);
            PreparedStatement pstm = conexion.prepareStatement(instruccion);
            pstm.setString(1, campos[0]);
            pstm.setDouble(2, Double.parseDouble(campos[1]));
            pstm.setString(3, idGasto);
            pstm.executeUpdate();
            conexion.close();
        } catch (SQLException e) {
            GestorErrores.manejar(e);
        }
    }
}
