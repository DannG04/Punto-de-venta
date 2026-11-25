package puntoventa.db;

import puntoventa.util.*;
import puntoventa.report.*;

import java.sql.*;

/**
 * DAO del dominio Ventas: carrito (venta_temp), registro de la venta,
 * descuentos y armado de datos para el ticket.
 *
 * Llamada cruzada: obtenerDatosVenta necesita los datos de la empresa para el
 * encabezado del ticket, así que instancia un EmpresaDAO hermano.
 *
 * @author mayra
 */
public class VentaDAO extends BaseDAO {

    private final EmpresaDAO empresa = new EmpresaDAO();

    public TicketData obtenerDatosVenta(String idVenta) {
        TicketData datos = new TicketData();
        datos.tipo = TipoTicket.VENTA;
        datos.idTransaccion = idVenta;
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
            ResultSet v = obtenerEncabezadoVentaTicket(idVenta);
            if (v != null && v.next()) {
                datos.fecha         = nvl(v.getString("fecha_venta"));
                datos.total         = v.getDouble("total_venta");
                datos.formaPago     = nvl(v.getString("forma_pago"));
                datos.clienteNombre = nvl(v.getString("cliente_nombre"));
                datos.vendedor      = nvl(v.getString("empleado_nombre"));
            }
        } catch (Exception e) { GestorErrores.registrar(e); }
        try {
            ResultSet det = obtenerProductosVentaTicket(idVenta);
            int num = 1;
            while (det != null && det.next()) {
                datos.productos.add(new String[]{
                    String.valueOf(num++),
                    nvl(det.getString("nombre")),
                    String.valueOf(det.getInt("cantidad_producto")),
                    "$" + String.format("%.2f", det.getDouble("precio_dado")),
                    "$" + String.format("%.2f", det.getDouble("precio_total"))
                });
                datos.subtotal += det.getDouble("precio_total");
            }
        } catch (Exception e) { GestorErrores.registrar(e); }
        return datos;
    }

    // FUNCIONES DE LA TABLA DE VENTA TEMPORAL
    public void limpiarVentaTemp() {//Vacía el carrito
        // Desde V1.23 el carrito no reserva stock: el inventario se descuenta al cobrar (realizar_venta).
        try (Connection conexion = conectar()) {
            CallableStatement cstm = conexion.prepareCall("{call vaciar_venta_temp()}");
            cstm.execute();
        } catch (SQLException e) {
            GestorErrores.manejar(e);
        }
    }

    public void insertarVentaTemp(String[] campos, boolean acum) {//Función para insertar una venta temporal
        mostrarSiHayError(insertarVentaTempSinDialogo(campos, acum));
    }

    /** Igual que insertarVentaTemp pero sin ventana: regresa null si salió bien o el mensaje para el usuario. */
    public String insertarVentaTempSinDialogo(String[] campos, boolean acum) {
        try (Connection conexion = conectar()) {
            CallableStatement cstm = conexion.prepareCall("{call set_venta_temp(?::id_producto_dominio,?,?)}");
            cstm.setString(1, campos[0]);
            cstm.setObject(2, campos[1], Types.INTEGER);
            cstm.setBoolean(3, acum);
            cstm.execute();
            return null;
        } catch (SQLException e) {
            GestorErrores.registrar(e);
            return GestorErrores.mensajeAmigable(e);
        }
    }

    private static void mostrarSiHayError(String mensaje) {
        if (mensaje != null) Mise.JOption(mensaje, "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
    }

    public void eliminarVentaTemp(String ide) {//Función para eliminar una venta temporal
        try (Connection conexion = conectar()) {
            CallableStatement cstm = conexion.prepareCall("{call eliminar_prod(?::id_producto_dominio)}");
            cstm.setString(1, ide);
            cstm.execute();
        } catch (SQLException e) {
            GestorErrores.manejar(e);
        }
    }

    public boolean ventaEnCurso() {//true si el carrito (venta_temp) tiene productos
        try (Connection conexion = conectar();
             ResultSet rs = conexion.createStatement().executeQuery("SELECT EXISTS(SELECT 1 FROM venta_temp)")) {
            return rs.next() && rs.getBoolean(1);
        } catch (SQLException e) {
            GestorErrores.manejar(e);
            return true; // ante la duda, no dejar actualizar
        }
    }

    public Double sumaVentaTemp() {//Función para obtener la suma de la venta temporal
        Double sum = 0.0;
        try (Connection conexion = conectar()) {
            CallableStatement cstm = conexion.prepareCall("{call suma_venta_temp()}");
            ResultSet rs = cstm.executeQuery();
            while (rs.next()) {
                sum = rs.getDouble(1);
            }
        } catch (SQLException e) {
            GestorErrores.manejar(e);
        }
        return sum;
    }

    // FUNCIONES DE LA TABLA VENTA
    public String registrarVenta(String idEmp, String idCli) {//Función para registrar una venta
        String idVenta = "";
        try (Connection conexion = conectar()) {
            CallableStatement cstm = conexion.prepareCall("{call realizar_venta(?::curp_dominio,?::curp_dominio)}");
            cstm.registerOutParameter(1, Types.VARCHAR);
            cstm.setString(1, idEmp);
            cstm.setString(2, idCli);
            cstm.execute();
            idVenta = cstm.getString(1);
        } catch (SQLException e) {
            GestorErrores.manejar(e);
        }
        return idVenta;
    }

    public String registrarVentaConFormaPago(String idEmp, String idCli, String formaPago) {//Función para registrar una venta con forma de pago
        String idVenta = registrarVenta(idEmp, idCli);
        if (!idVenta.isEmpty()) {
            try (Connection conexion = conectar()) {
                // Actualizar forma de pago
                PreparedStatement pstm = conexion.prepareStatement("UPDATE venta SET forma_pago=? WHERE id_venta=?");
                pstm.setString(1, formaPago);
                pstm.setString(2, idVenta);
                pstm.executeUpdate();
                // El kardex de la venta (folio, empleado y hora del cobro) lo registra realizar_venta (V1.23).
            } catch (SQLException e) {
                GestorErrores.manejar(e);
            }
        }
        return idVenta;
    }

    // FUNCIONES DE DESCUENTOS
    public void actualizarDescuentoTemp(String idProducto, double descuento) {//Función para actualizar el descuento en venta_temp
        mostrarSiHayError(actualizarDescuentoTempSinDialogo(idProducto, descuento));
    }

    /** Igual que actualizarDescuentoTemp pero sin ventana: null si salió bien o el mensaje para el usuario. */
    public String actualizarDescuentoTempSinDialogo(String idProducto, double descuento) {
        try (Connection conexion = conectar()) {
            CallableStatement cstm = conexion.prepareCall("{call actualizar_desc_temp(?::id_producto_dominio, ?::numeric)}");
            cstm.setString(1, idProducto);
            cstm.setDouble(2, descuento);
            cstm.execute();
            return null;
        } catch (SQLException e) {
            GestorErrores.registrar(e);
            return GestorErrores.mensajeAmigable(e);
        }
    }

    public void actualizarDescuentoVenta(String idVenta, String idProducto, double descuento) {//Función para guardar descuento en venta_detalle
        String instruccion = "UPDATE venta_detalle SET descuento_pct = ? WHERE id_venta = ? AND id_producto = ?;";
        try (Connection conexion = conectar()) {
            PreparedStatement pstm = conexion.prepareStatement(instruccion);
            pstm.setDouble(1, descuento);
            pstm.setString(2, idVenta);
            pstm.setString(3, idProducto);
            pstm.executeUpdate();
        } catch (SQLException e) {
            GestorErrores.manejar(e);
        }
    }

    public double obtenerMinMaxDescuentoTemp() {//Función para obtener el menor max_descuento entre los productos del carrito
        double minMax = 100.0;
        try (Connection conexion = conectar()) {
            Statement s = conexion.createStatement();
            ResultSet rs = s.executeQuery(
                "SELECT COALESCE(MIN(p.max_descuento), 100) FROM venta_temp vt JOIN producto p ON vt.id_producto = p.id_producto;");
            if (rs.next()) {
                minMax = rs.getDouble(1);
            }
        } catch (SQLException e) {
            GestorErrores.manejar(e);
        }
        return minMax;
    }

    // CONSULTAS PARA LLENAR TABLAS DE LA UI Y TICKETS

    public ResultSet obtenerVentaTemp() {//Renglones del carrito
        return consultarONulo("SELECT * FROM venta_temp");
    }

    public ResultSet obtenerVentas() {//Todas las ventas
        return consultarONulo("SELECT * FROM venta");
    }

    public ResultSet buscarVentasPorId(String texto) {//Ventas cuyo id contiene el texto; texto vacío = todas
        if (texto == null || texto.isEmpty()) return obtenerVentas();
        return consultarONulo("SELECT * FROM venta WHERE id_venta LIKE ?", "%" + texto + "%");
    }

    public ResultSet obtenerDetalleVenta(String idVenta) {//Renglones de una venta
        return consultarONulo("SELECT * FROM venta_detalle WHERE id_venta = ?", idVenta);
    }

    public ResultSet obtenerEncabezadoVentaTicket(String idVenta) {//Total, fecha, forma de pago, cliente y vendedor de una venta
        return consultarONulo(
            "SELECT v.total_venta, v.fecha_venta, v.forma_pago, " +
            "COALESCE(c.nombre,'Cliente General') AS cliente_nombre, e.nombre AS empleado_nombre " +
            "FROM venta v " +
            "LEFT JOIN cliente c ON v.id_cliente = c.id_cliente " +
            "JOIN empleado e ON v.id_empleado = e.id_empleado " +
            "WHERE v.id_venta = ?", idVenta);
    }

    public ResultSet obtenerProductosVentaTicket(String idVenta) {//Productos de una venta con su nombre
        return consultarONulo(
            "SELECT p.nombre, vd.cantidad_producto, vd.precio_dado, vd.precio_total " +
            "FROM venta_detalle vd JOIN producto p ON vd.id_producto = p.id_producto " +
            "WHERE vd.id_venta = ?", idVenta);
    }
}
