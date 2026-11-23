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
        try (Connection conexion = conectar()) {
            PreparedStatement pstm = conexion.prepareStatement(instruccion);
            pstm.setString(1, campos[0]);
            pstm.setString(2, campos[1]);
            pstm.setString(3, campos[2]);
            pstm.executeUpdate();
            band = true;
        } catch (SQLException e) {
            GestorErrores.manejar(e);
        }
        return band;
    }

    public void actualizarCliente(String[] campos, String idC) {//Función para actualizar un cliente
        String columnas = "SET id_cliente=?, nombre=?, telefono=?";
        String instruccion = "UPDATE cliente " + columnas + " WHERE id_cliente=?;";
        try (Connection conexion = conectar()) {
            PreparedStatement pstm = conexion.prepareStatement(instruccion);
            pstm.setString(1, campos[0]);
            pstm.setString(2, campos[1]);
            pstm.setString(3, campos[2]);
            pstm.setString(4, idC);
            pstm.executeUpdate();
        } catch (SQLException e) {
            GestorErrores.manejar(e);
        }
    }

    public void inactivarCliente(String idC, boolean act) {//Función para inactivar un cliente
        try (Connection conexion = conectar()) {
            CallableStatement cstm = conexion.prepareCall("{call inactivar_cliente(?::curp_dominio,?)}");
            cstm.setString(1, idC);
            cstm.setBoolean(2, act);
            cstm.execute();
        } catch (SQLException e) {
            GestorErrores.manejar(e);
        }
    }

    // CONSULTAS PARA LLENAR TABLAS DE LA UI

    public ResultSet obtenerClientes() {//Todos los clientes
        return consultarONulo("SELECT * FROM cliente");
    }

    public ResultSet obtenerClientesActivos() {//Clientes activos
        return consultarONulo("SELECT * FROM cliente WHERE estatus='Activo'");
    }

    public ResultSet buscarClientesActivosPorId(String texto) {//Clientes activos cuyo id (CURP) contiene el texto
        if (texto == null || texto.isEmpty()) return obtenerClientesActivos();
        return consultarONulo("SELECT * FROM cliente WHERE estatus='Activo' AND id_cliente LIKE ?", "%" + texto + "%");
    }

    public ResultSet buscarClientesActivos(String filtro) {//Clientes activos por id o nombre (sin distinguir mayúsculas), ordenados por nombre
        if (filtro == null || filtro.isEmpty())
            return consultarONulo("SELECT id_cliente, nombre FROM cliente WHERE estatus='Activo' ORDER BY nombre");
        String like = "%" + filtro + "%";
        return consultarONulo("SELECT id_cliente, nombre FROM cliente WHERE estatus='Activo' AND (id_cliente ILIKE ? OR nombre ILIKE ?) ORDER BY nombre",
                like, like);
    }
}
