package puntoventa.db;

import puntoventa.util.*;
import puntoventa.report.*;

import java.sql.*;

/**
 * DAO del dominio Apartados: encabezado, abonos, cancelación/entrega, detalle,
 * consultas y armado de datos para el ticket.
 *
 * Llamada cruzada: obtenerDatosApartado necesita los datos de la empresa para el
 * encabezado del ticket, así que instancia un EmpresaDAO hermano.
 *
 * @author mayra
 */
public class ApartadoDAO extends BaseDAO {

    private final EmpresaDAO empresa = new EmpresaDAO();

    public TicketData obtenerDatosApartado(String idApartado, TipoTicket tipo) {
        TicketData datos = new TicketData();
        datos.tipo = tipo;
        datos.idTransaccion = idApartado;
        try {
            ResultSet emp = empresa.obtenerEmpresa();
            if (emp != null && emp.next()) {
                datos.empresaNombre  = nvl(emp.getString("nombre"));
                datos.empresaTel     = nvl(emp.getString("telefono"));
                String dir    = nvl(emp.getString("direccion"));
                String ciudad = nvl(emp.getString("ciudad"));
                datos.empresaDireccion = dir.isEmpty() ? ciudad
                                       : (ciudad.isEmpty() ? dir : dir + ", " + ciudad);
                datos.empresaMensaje = nvl(emp.getString("mensaje_ticket"));
                String logo = emp.getString("logo_ruta");
                datos.logoRuta = logo == null ? "" : logo;
            }
        } catch (Exception e) { GestorErrores.registrar(e); }
        try {
            ResultSet ap = query(
                "SELECT a.fecha_inicio, a.fecha_limite, a.cantidad_dada, a.cantidad_faltante, a.cantidad_total, " +
                "c.nombre AS cliente_nombre, e.nombre AS empleado_nombre " +
                "FROM apartado a " +
                "JOIN cliente c ON a.id_cliente = c.id_cliente " +
                "JOIN empleado e ON a.id_empleado = e.id_empleado " +
                "WHERE a.id_apartado = '" + idApartado + "'");
            if (ap != null && ap.next()) {
                datos.fecha             = nvl(ap.getString("fecha_inicio"));
                datos.fechaLimite       = nvl(ap.getString("fecha_limite"));
                datos.clienteNombre     = nvl(ap.getString("cliente_nombre"));
                datos.vendedor          = nvl(ap.getString("empleado_nombre"));
                datos.cantidadDada      = ap.getDouble("cantidad_dada");
                datos.cantidadFaltante  = ap.getDouble("cantidad_faltante");
                datos.total             = ap.getDouble("cantidad_total");
            }
        } catch (Exception e) { GestorErrores.registrar(e); }
        try {
            ResultSet det = seleccionarProductos(idApartado);
            int num = 1;
            while (det != null && det.next()) {
                datos.productos.add(new String[]{
                    String.valueOf(num++),
                    nvl(det.getString("nombre")),
                    String.valueOf(det.getInt("cantidad")),
                    "$" + String.format("%.2f", det.getDouble("precio_unitario")),
                    "$" + String.format("%.2f", det.getDouble("precio_total"))
                });
                datos.subtotal += det.getDouble("precio_total");
            }
        } catch (Exception e) { GestorErrores.registrar(e); }
        return datos;
    }

    public String insertarApartado(String[] campos) {//Función para insertar un apartado
        String idAp = "";
        try {
            Connection conexion = DriverManager.getConnection(url + nameBD, usuario, contra);
            CallableStatement cstm = conexion.prepareCall("{call reg_apartado(?::curp_dominio,?::curp_dominio)}");
            cstm.registerOutParameter(1, Types.VARCHAR);
            cstm.setString(1, campos[0]);
            cstm.setString(2, campos[1]);
            cstm.execute();
            idAp = cstm.getString(1);
            conexion.close();
        } catch (SQLException e) {
            GestorErrores.manejar(e);
        }
        return idAp;
    }

    public boolean actualizarApartado(String idAp, Double cantPag){//Función para actualizar un apartado
        boolean band = false;
        try{
            Connection conexion = DriverManager.getConnection(url + nameBD, usuario, contra);
            CallableStatement cstm = conexion.prepareCall("{call act_apartado(?,?)}");
            cstm.setString(1, idAp);
            cstm.setObject(2, cantPag, Types.NUMERIC);
            cstm.execute();
            conexion.close();
            band = true;
        } catch(SQLException e){
            GestorErrores.manejar(e);
        }
        return band;
    }

    public void eliminarApartado(String idAp) {//Función para eliminar un apartado
        try {
            Connection conexion = DriverManager.getConnection(url + nameBD, usuario, contra);
            CallableStatement cstm = conexion.prepareCall("{call elim_apartado(?)}");
            cstm.setString(1, idAp);
            cstm.execute();
            conexion.close();
        } catch (SQLException e) {
            GestorErrores.manejar(e);
        }
    }

    public void revisarApartado() {//Función para revisar los apartados
        try {
            Connection conexion = DriverManager.getConnection(url + nameBD, usuario, contra);
            CallableStatement cstm = conexion.prepareCall("{call ap_vigentes()}");
            cstm.execute();
            conexion.close();
        } catch (SQLException e) {
            GestorErrores.manejar(e);
        }
    }

    public double cantidadCancelarApartado(String idAp) {// Función para obtener la cantidad a cancelar de un apartado
        double monto = 0.0;
        try {
            Connection conexion = DriverManager.getConnection(url + nameBD, usuario, contra);
            CallableStatement cstm = conexion.prepareCall("{call cancelar_ap_cliente(?)}");
            cstm.setString(1, idAp);
            ResultSet rs = cstm.executeQuery();
            while (rs.next()) {
                monto = rs.getDouble(1);
            }
            conexion.close();
        } catch (SQLException e) {
            GestorErrores.manejar(e);
        }
        return monto;
    }

    public void cancelarApartado(String idAp) {//Función para cancelar un apartado
        try {
            Connection conexion = DriverManager.getConnection(url + nameBD, usuario, contra);
            CallableStatement cstm = conexion.prepareCall("{call cancelar_ap(?)}");
            cstm.setString(1, idAp);
            cstm.execute();
            conexion.close();
        } catch (SQLException e) {
            GestorErrores.manejar(e);
        }
    }

    public void entregarApartado(String idAp) {//Función para entregar un apartado
        try {
            Connection conexion = DriverManager.getConnection(url + nameBD, usuario, contra);
            CallableStatement cstm = conexion.prepareCall("{call entregar_ap(?)}");
            cstm.setString(1, idAp);
            cstm.execute();
            conexion.close();
        } catch (SQLException e) {
            GestorErrores.manejar(e);
        }
    }

    public void insertarProdApartado(String[] campos, boolean ac) {//Función para insertar un producto en un apartado
        try {
            Connection conexion = DriverManager.getConnection(url + nameBD, usuario, contra);
            CallableStatement cstm = conexion.prepareCall("{call reg_apdet(?,?::id_producto_dominio,?,?)}");
            cstm.setString(1, campos[0]);
            cstm.setString(2, campos[1]);
            cstm.setObject(3, campos[2], Types.INTEGER);
            cstm.setBoolean(4, ac);
            cstm.execute();
            conexion.close();
        } catch (SQLException e) {
            GestorErrores.manejar(e);
        }
    }

    public void eliminarProdApartado(String[] campos) {//Función para eliminar un producto de un apartado
        try {
            Connection conexion = DriverManager.getConnection(url + nameBD, usuario, contra);
            CallableStatement cstm = conexion.prepareCall("{call elim_apdet(?,?::id_producto_dominio)}");
            cstm.setString(1, campos[0]);
            cstm.setString(2, campos[1]);
            cstm.execute();
            conexion.close();
        } catch (SQLException e) {
            GestorErrores.manejar(e);
        }
    }

    public ResultSet seleccionarApartado(String idap) {
        ResultSet resultado = null;
        try {
            Connection conexion = DriverManager.getConnection(url + nameBD, usuario, contra);
            PreparedStatement consulta = conexion.prepareStatement(
                    "SELECT id_apartado,id_empleado,fecha_inicio,fecha_limite,cantidad_dada,cantidad_faltante,cantidad_total FROM apartado where id_apartado ='"
                            + idap + "';");
            resultado = consulta.executeQuery();
        } catch (Exception e) {
            GestorErrores.registrar(e);
        }
        return resultado;
    }

    public ResultSet seleccionarProductos(String idApartado) {//Función para seleccionar los productos de un apartado
        ResultSet resultado = null;
        try {
            Connection conexion = DriverManager.getConnection(url + nameBD, usuario, contra);
            Statement stmt = conexion.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            String query = "SELECT * FROM producto, apartado_detalle where producto.id_producto = apartado_detalle.id_producto and id_apartado = '"
                    + idApartado + "';";
            resultado = stmt.executeQuery(query);
        } catch (Exception e) {
            GestorErrores.registrar(e);
        }
        return resultado;
    }
}
