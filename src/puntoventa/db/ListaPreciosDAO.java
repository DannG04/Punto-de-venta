package puntoventa.db;

import puntoventa.util.*;

import java.sql.*;

/**
 * DAO del dominio Listas de Precios: listas activas, overrides de precio por
 * producto/lista y aplicación del precio de lista al carrito (venta_temp).
 *
 * @author mayra
 */
public class ListaPreciosDAO extends BaseDAO {

    public ResultSet obtenerListas() {//Función para obtener las listas de precios activas
        return query("SELECT id_lista, nombre FROM lista_precios WHERE estatus='Activo' ORDER BY id_lista");
    }

    public double obtenerPrecioEnLista(String idProducto, int idLista) {//Función para obtener el precio de un producto en una lista (-1 si no existe)
        // producto_precio actúa como override opcional: si no hay fila para la lista,
        // las listas de fábrica "Menudeo"/"Mayoreo" caen a las columnas del producto.
        double precio = -1.0;
        String instruccion =
            "SELECT COALESCE(pp.precio, " +
            "                CASE lp.nombre " +
            "                     WHEN 'Menudeo' THEN p.precio_menudeo " +
            "                     WHEN 'Mayoreo' THEN p.precio_mayoreo " +
            "                END) AS precio " +
            "FROM lista_precios lp " +
            "JOIN producto p ON p.id_producto = ? " +
            "LEFT JOIN producto_precio pp " +
            "       ON pp.id_producto = p.id_producto AND pp.id_lista = lp.id_lista " +
            "WHERE lp.id_lista = ?;";
        try {
            Connection conexion = DriverManager.getConnection(url + nameBD, usuario, contra);
            PreparedStatement pstm = conexion.prepareStatement(instruccion);
            pstm.setString(1, idProducto);
            pstm.setInt(2, idLista);
            ResultSet rs = pstm.executeQuery();
            if (rs.next()) {
                double p = rs.getDouble("precio");
                if (!rs.wasNull()) {
                    precio = p;
                }
            }
            conexion.close();
        } catch (SQLException e) {
            GestorErrores.manejar(e);
        }
        return precio;
    }

    public void actualizarPrecioEnLista(String idProducto, int idLista, double precio) {//Función para actualizar el precio de un producto en una lista
        String instruccion = "UPDATE producto_precio SET precio=? WHERE id_producto=? AND id_lista=?;";
        try {
            Connection conexion = DriverManager.getConnection(url + nameBD, usuario, contra);
            PreparedStatement pstm = conexion.prepareStatement(instruccion);
            pstm.setDouble(1, precio);
            pstm.setString(2, idProducto);
            pstm.setInt(3, idLista);
            pstm.executeUpdate();
            conexion.close();
        } catch (SQLException e) {
            GestorErrores.manejar(e);
        }
    }

    public void insertarPrecioEnLista(String idProducto, int idLista, double precio) {//Función para insertar o actualizar el precio de un producto en una lista
        String instruccion = "INSERT INTO producto_precio(id_producto, id_lista, precio) VALUES(?,?,?) " +
                             "ON CONFLICT (id_producto, id_lista) DO UPDATE SET precio = EXCLUDED.precio;";
        try {
            Connection conexion = DriverManager.getConnection(url + nameBD, usuario, contra);
            PreparedStatement pstm = conexion.prepareStatement(instruccion);
            pstm.setString(1, idProducto);
            pstm.setInt(2, idLista);
            pstm.setDouble(3, precio);
            pstm.executeUpdate();
            conexion.close();
        } catch (SQLException e) {
            GestorErrores.manejar(e);
        }
    }

    public void eliminarPrecioEnLista(String idProducto, int idLista) {//Función para quitar el override de precio (vuelve a usarse el precio base del producto)
        String instruccion = "DELETE FROM producto_precio WHERE id_producto=? AND id_lista=?;";
        try {
            Connection conexion = DriverManager.getConnection(url + nameBD, usuario, contra);
            PreparedStatement pstm = conexion.prepareStatement(instruccion);
            pstm.setString(1, idProducto);
            pstm.setInt(2, idLista);
            pstm.executeUpdate();
            conexion.close();
        } catch (SQLException e) {
            Mise.JOption(e.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    public void actualizarPrecioListaTemp(String idProducto, double nuevoPrecio, String nombreLista) {//Función para sobreescribir el precio en venta_temp con el de la lista activa
        String instruccion = "UPDATE venta_temp SET precio_dado=?, precio_total=?*cantidad_prod WHERE id_producto=?;";
        try {
            Connection conexion = DriverManager.getConnection(url + nameBD, usuario, contra);
            PreparedStatement pstm = conexion.prepareStatement(instruccion);
            pstm.setDouble(1, nuevoPrecio);
            pstm.setDouble(2, nuevoPrecio);
            pstm.setString(3, idProducto);
            pstm.executeUpdate();
            conexion.close();
        } catch (SQLException e) {
            GestorErrores.manejar(e);
        }
    }
}
