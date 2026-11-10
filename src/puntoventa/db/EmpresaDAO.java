package puntoventa.db;

import puntoventa.util.*;

import java.sql.*;

/**
 * DAO del dominio Empresa: datos del negocio (fila id=1) e inicialización.
 *
 * @author mayra
 */
public class EmpresaDAO extends BaseDAO {

    public ResultSet obtenerEmpresa() {
        return query("SELECT * FROM empresa WHERE id = 1");
    }

    public boolean actualizarEmpresa(String nombre, String razonSocial, String rfc,
            String telefono, String correo, String direccion, String ciudad,
            String estado, String cp, String mensajeTicket, String logoRuta) {
        String instruccion = "UPDATE empresa SET nombre=?, razon_social=?, rfc=?, telefono=?, correo=?, " +
            "direccion=?, ciudad=?, estado=?, cp=?, mensaje_ticket=?, logo_ruta=? WHERE id=1;";
        try {
            Connection conexion = DriverManager.getConnection(url + nameBD, usuario, contra);
            PreparedStatement pstm = conexion.prepareStatement(instruccion);
            pstm.setString(1, nombre);
            pstm.setString(2, razonSocial.isEmpty() ? null : razonSocial);
            pstm.setString(3, rfc.isEmpty() ? null : rfc);
            pstm.setString(4, telefono.isEmpty() ? null : telefono);
            pstm.setString(5, correo.isEmpty() ? null : correo);
            pstm.setString(6, direccion.isEmpty() ? null : direccion);
            pstm.setString(7, ciudad.isEmpty() ? null : ciudad);
            pstm.setString(8, estado.isEmpty() ? null : estado);
            pstm.setString(9, cp.isEmpty() ? null : cp);
            pstm.setString(10, mensajeTicket.isEmpty() ? null : mensajeTicket);
            pstm.setString(11, logoRuta.isEmpty() ? null : logoRuta);
            pstm.executeUpdate();
            conexion.close();
            return true;
        } catch (SQLException e) {
            GestorErrores.manejar(e);
            return false;
        }
    }

    public boolean empresaInicializada() {
        try {
            ResultSet rs = query("SELECT nombre FROM empresa WHERE id=1 AND nombre <> 'Mi Empresa'");
            if (rs != null && rs.next()) return true;
        } catch (Exception e) {
            // tabla aún no existe o nombre es el default
        }
        return false;
    }
}
