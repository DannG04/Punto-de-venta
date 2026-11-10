package puntoventa.db;

import puntoventa.util.*;

import java.sql.*;

/**
 * DAO del dominio Proveedor: alta/edición, estatus y consultas.
 *
 * @author mayra
 */
public class ProveedorDAO extends BaseDAO {

    public Integer insertarProveedor(String nombre, String telefono, String email, String direccion, String rfc) {//Función para insertar un proveedor; regresa el id_proveedor generado o null si falló
        Integer idNuevo = null;
        String instruccion = "INSERT INTO proveedor(nombre, telefono, email, direccion, rfc) VALUES(?,?,?,?,?) RETURNING id_proveedor;";
        try {
            Connection conexion = DriverManager.getConnection(url + nameBD, usuario, contra);
            PreparedStatement pstm = conexion.prepareStatement(instruccion);
            pstm.setString(1, nombre);
            pstm.setString(2, telefono.isEmpty() ? null : telefono);
            pstm.setString(3, email.isEmpty() ? null : email);
            pstm.setString(4, direccion.isEmpty() ? null : direccion);
            pstm.setString(5, rfc == null || rfc.isEmpty() ? null : rfc);
            ResultSet rs = pstm.executeQuery();
            if (rs.next()) idNuevo = rs.getInt("id_proveedor");
            conexion.close();
        } catch (SQLException e) {
            Mise.JOption(e.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
        return idNuevo;
    }

    public boolean editarProveedor(int id, String nombre, String telefono, String email, String direccion, String rfc) {//Función para editar un proveedor
        boolean band = false;
        String instruccion = "UPDATE proveedor SET nombre=?, telefono=?, email=?, direccion=?, rfc=? WHERE id_proveedor=?;";
        try {
            Connection conexion = DriverManager.getConnection(url + nameBD, usuario, contra);
            PreparedStatement pstm = conexion.prepareStatement(instruccion);
            pstm.setString(1, nombre);
            pstm.setString(2, telefono.isEmpty() ? null : telefono);
            pstm.setString(3, email.isEmpty() ? null : email);
            pstm.setString(4, direccion.isEmpty() ? null : direccion);
            pstm.setString(5, rfc == null || rfc.isEmpty() ? null : rfc);
            pstm.setInt(6, id);
            pstm.executeUpdate();
            conexion.close();
            band = true;
        } catch (SQLException e) {
            GestorErrores.manejar(e);
        }
        return band;
    }

    public String[] obtenerDatosProveedor(int idProveedor) {//Función para obtener RFC y dirección de un proveedor
        String[] datos = {"", ""}; // [0]=rfc, [1]=direccion
        String sql = "SELECT COALESCE(rfc,'') rfc, COALESCE(direccion,'') direccion FROM proveedor WHERE id_proveedor = ?;";
        try {
            Connection conexion = DriverManager.getConnection(url + nameBD, usuario, contra);
            PreparedStatement pstm = conexion.prepareStatement(sql);
            pstm.setInt(1, idProveedor);
            ResultSet rs = pstm.executeQuery();
            if (rs.next()) {
                datos[0] = rs.getString("rfc");
                datos[1] = rs.getString("direccion");
            }
            conexion.close();
        } catch (SQLException e) {
            System.out.println("Error al obtener datos del proveedor: " + e.getMessage());
        }
        return datos;
    }

    public boolean cambiarEstatusProveedor(int id, String estatus) {//Función para cambiar el estatus de un proveedor
        boolean band = false;
        String instruccion = "UPDATE proveedor SET estatus=? WHERE id_proveedor=?;";
        try {
            Connection conexion = DriverManager.getConnection(url + nameBD, usuario, contra);
            PreparedStatement pstm = conexion.prepareStatement(instruccion);
            pstm.setString(1, estatus);
            pstm.setInt(2, id);
            pstm.executeUpdate();
            conexion.close();
            band = true;
        } catch (SQLException e) {
            GestorErrores.manejar(e);
        }
        return band;
    }

    public ResultSet obtenerProveedores() {//Función para obtener proveedores activos
        return query("SELECT id_proveedor, nombre FROM proveedor WHERE estatus='Activo' ORDER BY nombre");
    }
}
