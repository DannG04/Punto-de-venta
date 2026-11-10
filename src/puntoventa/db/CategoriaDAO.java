package puntoventa.db;

import puntoventa.util.*;

import java.sql.*;

/**
 * DAO del dominio Categoría: alta/edición, estatus y consultas de catálogo.
 *
 * @author mayra
 */
public class CategoriaDAO extends BaseDAO {

    public boolean insertarCategoria(String nombre, String descripcion) {//Función para insertar una categoría
        boolean band = false;
        String instruccion = "INSERT INTO categoria(nombre, descripcion) VALUES(?,?);";
        try {
            Connection conexion = DriverManager.getConnection(url + nameBD, usuario, contra);
            PreparedStatement pstm = conexion.prepareStatement(instruccion);
            pstm.setString(1, nombre);
            pstm.setString(2, descripcion.isEmpty() ? null : descripcion);
            pstm.executeUpdate();
            conexion.close();
            band = true;
        } catch (SQLException e) {
            GestorErrores.manejar(e);
        }
        return band;
    }

    public boolean editarCategoria(int id, String nombre, String descripcion) {//Función para editar una categoría
        boolean band = false;
        String instruccion = "UPDATE categoria SET nombre=?, descripcion=? WHERE id_categoria=?;";
        try {
            Connection conexion = DriverManager.getConnection(url + nameBD, usuario, contra);
            PreparedStatement pstm = conexion.prepareStatement(instruccion);
            pstm.setString(1, nombre);
            pstm.setString(2, descripcion.isEmpty() ? null : descripcion);
            pstm.setInt(3, id);
            pstm.executeUpdate();
            conexion.close();
            band = true;
        } catch (SQLException e) {
            GestorErrores.manejar(e);
        }
        return band;
    }

    public boolean cambiarEstatusCategoria(int id, String estatus) {//Función para cambiar estatus de una categoría
        boolean band = false;
        String instruccion = "UPDATE categoria SET estatus=? WHERE id_categoria=?;";
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

    public ResultSet obtenerCategorias() {//Función para obtener categorías activas
        return query("SELECT id_categoria, nombre FROM categoria WHERE estatus='Activo' ORDER BY nombre");
    }

    public ResultSet obtenerTodasCategorias() {//Función para obtener todas las categorías
        return query("SELECT id_categoria, nombre, descripcion, estatus FROM categoria ORDER BY id_categoria");
    }

    public ResultSet buscarCategoriasPorNombre(String filtro) {//Función para buscar categorías por nombre
        ResultSet rs = null;
        String instruccion = "SELECT id_categoria, nombre, descripcion, estatus FROM categoria WHERE nombre ILIKE ? ORDER BY id_categoria";
        try {
            Connection conexion = DriverManager.getConnection(url + nameBD, usuario, contra);
            PreparedStatement pstm = conexion.prepareStatement(instruccion);
            pstm.setString(1, "%" + filtro + "%");
            rs = pstm.executeQuery();
        } catch (SQLException e) {
            GestorErrores.registrar(e);
        }
        return rs;
    }
}
