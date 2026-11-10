package puntoventa.db;

import puntoventa.util.*;

import java.sql.*;

/**
 * DAO del dominio Otras Ganancias: alta y actualización.
 *
 * @author mayra
 */
public class GananciaDAO extends BaseDAO {

    public void insertarOtraGanancia(String[] campos) {//Función para insertar otra ganancia
        String columnas = "otras_ganancias(descripcion, monto)";
        String instruccion = "INSERT INTO " + columnas + " VALUES(?,?);";
        try {
            Connection conexion = DriverManager.getConnection(url + nameBD, usuario, contra);
            PreparedStatement pstm = conexion.prepareStatement(instruccion);
            pstm.setString(1, campos[0]);
            pstm.setDouble(2, Double.parseDouble(campos[1]));
            pstm.executeUpdate();
            conexion.close();
        } catch (SQLException e) {
            GestorErrores.manejar(e);
        }
    }

    public void actualizarOtraGanancia(String[] campos) {//Función para actualizar otra ganancia
        String columnas = "SET descripcion=?, monto=?";
        String instruccion = "UPDATE otras_ganancias " + columnas + " WHERE id_otras_ganancias=?;";
        try {
            Connection conexion = DriverManager.getConnection(url + nameBD, usuario, contra);
            PreparedStatement pstm = conexion.prepareStatement(instruccion);
            pstm.setString(1, campos[0]);
            pstm.setDouble(2, Double.parseDouble(campos[1]));
            pstm.setString(3, campos[2]);
            pstm.executeUpdate();
            conexion.close();
        } catch (SQLException e) {
            GestorErrores.manejar(e);
        }
    }
}
