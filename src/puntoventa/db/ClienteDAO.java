package puntoventa.db;

import puntoventa.util.*;

import java.sql.*;

/**
 * DAO del dominio Cliente: alta, edición e inactivación.
 *
 * @author mayra
 */
public class ClienteDAO extends BaseDAO {

    public boolean insertarCliente(String[] campos) {//Función para insertar un cliente
        boolean band = false;
        String columnas = "cliente(id_cliente, nombre, telefono)";
        String instruccion = "INSERT INTO " + columnas + " VALUES(?,?,?);";
        try {
            Connection conexion = DriverManager.getConnection(url + nameBD, usuario, contra);
            PreparedStatement pstm = conexion.prepareStatement(instruccion);
            pstm.setString(1, campos[0]);
            pstm.setString(2, campos[1]);
            pstm.setString(3, campos[2]);
            pstm.executeUpdate();
            conexion.close();
            band = true;
        } catch (SQLException e) {
            GestorErrores.manejar(e);
        }
        return band;
    }

    public void actualizarCliente(String[] campos, String idC) {//Función para actualizar un cliente
        String columnas = "SET id_cliente=?, nombre=?, telefono=?";
        String instruccion = "UPDATE cliente " + columnas + " WHERE id_cliente=?;";
        try {
            Connection conexion = DriverManager.getConnection(url + nameBD, usuario, contra);
            PreparedStatement pstm = conexion.prepareStatement(instruccion);
            pstm.setString(1, campos[0]);
            pstm.setString(2, campos[1]);
            pstm.setString(3, campos[2]);
            pstm.setString(4, idC);
            pstm.executeUpdate();
            conexion.close();
        } catch (SQLException e) {
            GestorErrores.manejar(e);
        }
    }

    public void inactivarCliente(String idC, boolean act) {//Función para inactivar un cliente
        try {
            Connection conexion = DriverManager.getConnection(url + nameBD, usuario, contra);
            CallableStatement cstm = conexion.prepareCall("{call inactivar_cliente(?::curp_dominio,?)}");
            cstm.setString(1, idC);
            cstm.setBoolean(2, act);
            cstm.execute();
            conexion.close();
        } catch (SQLException e) {
            GestorErrores.manejar(e);
        }
    }
}
