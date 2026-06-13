package puntoventa.db;

import puntoventa.util.*;
import puntoventa.report.*;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

/**
 *
 * @author mayra
 */
public class ConexionBD extends BaseDAO {

    // Credenciales, constructor (lectura de db.properties), inst(), query() y nvl()
    // ahora viven en BaseDAO. Las 36 llamadas crudas conect.inst/query se resuelven
    // por herencia. Esta clase queda como fachada de delegadores hacia los DAOs.

    // DAOs por dominio (la fachada delega en ellos)
    private final EmpleadoDAO empleados = new EmpleadoDAO();
    private final CategoriaDAO categorias = new CategoriaDAO();
    private final ProveedorDAO proveedores = new ProveedorDAO();
    private final GastoDAO gastos = new GastoDAO();
    private final GananciaDAO ganancias = new GananciaDAO();
    private final EmpresaDAO empresa = new EmpresaDAO();
    private final ClienteDAO clientes = new ClienteDAO();
    private final DevolucionDAO devoluciones = new DevolucionDAO();
    private final CompraDAO compras = new CompraDAO();

    // FUNCION DE LA TABLA EMPLEADO
    public boolean insertarEmpleado(String[] campos) { return empleados.insertarEmpleado(campos); }

    public boolean actualizarEmpleado(String[] campos, String idEm) { return empleados.actualizarEmpleado(campos, idEm); }

    public boolean inactivarEmpleado(String idEm, boolean act) { return empleados.inactivarEmpleado(idEm, act); }

    public boolean verificarUsuario(String uss) { return empleados.verificarUsuario(uss); }

    public String[] idEmpleado(String uss, String paswor) { return empleados.idEmpleado(uss, paswor); }

    // FUNCIONES DE LA TABLA PRODUCTO
    public void insertarProducto(String[] datos) {//Función para insertar un producto
        String columnas = "producto(nombre, cantidad, precio_mayoreo, precio_menudeo)";
        String instruccion = "INSERT INTO " + columnas + " VALUES (?,?,?,?);";
        try {
            Connection conexion = DriverManager.getConnection(url + nameBD, usuario, contra);
            PreparedStatement pstm = conexion.prepareStatement(instruccion);
            pstm.setString(1, datos[0]);
            pstm.setInt(2, Integer.parseInt(datos[1]));
            pstm.setDouble(3, Double.parseDouble(datos[2]));
            pstm.setDouble(4, Double.parseDouble(datos[3]));
            pstm.executeUpdate();
            conexion.close();
        } catch (SQLException e) {
            GestorErrores.manejar(e);
        }
    }
    
    public void insertarProductoConCodigo(String[] datos) {//Función para insertar un producto con código personalizado
        String columnas = "producto(id_producto, nombre, cantidad, precio_mayoreo, precio_menudeo)";
        String instruccion = "INSERT INTO " + columnas + " VALUES (?,?,?,?,?);";
        try {
            Connection conexion = DriverManager.getConnection(url + nameBD, usuario, contra);
            PreparedStatement pstm = conexion.prepareStatement(instruccion);
            pstm.setString(1, datos[0]); // id_producto (código personalizado)
            pstm.setString(2, datos[1]); // nombre
            pstm.setInt(3, Integer.parseInt(datos[2])); // cantidad
            pstm.setDouble(4, Double.parseDouble(datos[3])); // precio_mayoreo
            pstm.setDouble(5, Double.parseDouble(datos[4])); // precio_menudeo
            pstm.executeUpdate();
            conexion.close();
        } catch (SQLException e) {
            GestorErrores.manejar(e);
        }
    }

    public void actualizarProducto(String ideprod, String[] datos) {//Función para actualizar un producto
        String columnas = "nombre = ?, cantidad = ?, precio_mayoreo = ?, precio_menudeo = ?";
        String instruccion = "UPDATE producto SET " + columnas + " WHERE id_producto = ?;";
        try {
            Connection conexion = DriverManager.getConnection(url + nameBD, usuario, contra);
            PreparedStatement pstm = conexion.prepareStatement(instruccion);
            pstm.setString(1, datos[0]);
            pstm.setInt(2, Integer.parseInt(datos[1]));
            pstm.setDouble(3, Double.parseDouble(datos[2]));
            pstm.setDouble(4, Double.parseDouble(datos[3]));
            pstm.setString(5, ideprod);
            pstm.executeUpdate();
            conexion.close();
        } catch (SQLException e) {
            GestorErrores.manejar(e);
        }
    }

    public boolean eliminarProducto(String ideprod) {//Función para dar de baja un producto (baja lógica)
        boolean band = false;
        try {
            Connection conexion = DriverManager.getConnection(url + nameBD, usuario, contra);
            CallableStatement cstm = conexion.prepareCall("{call baja_producto(?::id_producto_dominio)}");
            cstm.setString(1, ideprod);
            cstm.execute();
            conexion.close();
            band = true;
        } catch (SQLException e) {
            GestorErrores.manejar(e);
        }
        return band;
    }

    public boolean reactivarProducto(String ideprod) {//Reactiva un producto dado de baja (conserva historial)
        boolean band = false;
        try {
            Connection conexion = DriverManager.getConnection(url + nameBD, usuario, contra);
            CallableStatement cstm = conexion.prepareCall("{call reactivar_producto(?::id_producto_dominio)}");
            cstm.setString(1, ideprod);
            cstm.execute();
            conexion.close();
            band = true;
        } catch (SQLException e) {
            GestorErrores.manejar(e);
        }
        return band;
    }

    public String archivarProducto(String ideprod) {//Archiva un producto liberando su código de barras; devuelve el código archivado
        String archivado = "";
        try {
            Connection conexion = DriverManager.getConnection(url + nameBD, usuario, contra);
            CallableStatement cstm = conexion.prepareCall("{? = call archivar_producto(?::id_producto_dominio)}");
            cstm.registerOutParameter(1, Types.VARCHAR);
            cstm.setString(2, ideprod);
            cstm.execute();
            archivado = cstm.getString(1);
            conexion.close();
        } catch (SQLException e) {
            GestorErrores.manejar(e);
        }
        return archivado;
    }

    public String estadoProducto(String ideprod) {//Devuelve "Activo", "Inactivo" o null si el código no existe
        String estado = null;
        try {
            Connection conexion = DriverManager.getConnection(url + nameBD, usuario, contra);
            PreparedStatement pstm = conexion.prepareStatement("SELECT estatus FROM producto WHERE id_producto = ?");
            pstm.setString(1, ideprod);
            ResultSet rs = pstm.executeQuery();
            if (rs.next()) estado = rs.getString(1);
            conexion.close();
        } catch (SQLException e) {
            GestorErrores.manejar(e);
        }
        return estado;
    }

    public ResultSet obtenerProductosInactivos() {//Productos dados de baja (no cierra la conexión: el ResultSet queda usable)
        ResultSet rs = null;
        String instruccion = "SELECT id_producto, nombre, cantidad, fecha_baja FROM producto " +
            "WHERE estatus = 'Inactivo' ORDER BY fecha_baja DESC NULLS LAST, nombre";
        try {
            Connection conexion = DriverManager.getConnection(url + nameBD, usuario, contra);
            PreparedStatement pstm = conexion.prepareStatement(instruccion);
            rs = pstm.executeQuery();
        } catch (SQLException e) {
            GestorErrores.manejar(e);
        }
        return rs;
    }

    public TicketData obtenerDatosVenta(String idVenta) {
        TicketData datos = new TicketData();
        datos.tipo = TipoTicket.VENTA;
        datos.idTransaccion = idVenta;
        try {
            ResultSet emp = obtenerEmpresa();
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
            ResultSet v = query(
                "SELECT v.total_venta, v.fecha_venta, v.forma_pago, " +
                "COALESCE(c.nombre,'Cliente General') AS cliente_nombre, e.nombre AS empleado_nombre " +
                "FROM venta v " +
                "LEFT JOIN cliente c ON v.id_cliente = c.id_cliente " +
                "JOIN empleado e ON v.id_empleado = e.id_empleado " +
                "WHERE v.id_venta = '" + idVenta + "'");
            if (v != null && v.next()) {
                datos.fecha         = nvl(v.getString("fecha_venta"));
                datos.total         = v.getDouble("total_venta");
                datos.formaPago     = nvl(v.getString("forma_pago"));
                datos.clienteNombre = nvl(v.getString("cliente_nombre"));
                datos.vendedor      = nvl(v.getString("empleado_nombre"));
            }
        } catch (Exception e) { GestorErrores.registrar(e); }
        try {
            ResultSet det = query(
                "SELECT p.nombre, vd.cantidad_producto, vd.precio_dado, vd.precio_total " +
                "FROM venta_detalle vd JOIN producto p ON vd.id_producto = p.id_producto " +
                "WHERE vd.id_venta = '" + idVenta + "'");
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

    public TicketData obtenerDatosApartado(String idApartado, TipoTicket tipo) {
        TicketData datos = new TicketData();
        datos.tipo = tipo;
        datos.idTransaccion = idApartado;
        try {
            ResultSet emp = obtenerEmpresa();
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

    public boolean hayPocosProductos() {//Función para verificar si hay pocos productos
        boolean hay = false;
        try {
            Connection conexion = DriverManager.getConnection(url + nameBD, usuario, contra);
            CallableStatement cstm = conexion.prepareCall("{call hay_pocos_prod()}");
            ResultSet rs = cstm.executeQuery();
            while (rs.next()) {
                hay = rs.getBoolean(1);
            }
            conexion.close();
        } catch (SQLException e) {
            GestorErrores.manejar(e);
        }
        return hay;
    }

    // FUNCIONES DE LA TABLA DE VENTA TEMPORAL
    public void limpiarVentaTemp() {//Función para limpiar la tabla de venta temporal
        inst("DELETE FROM venta_temp;");
    }

    public void insertarVentaTemp(String[] campos, boolean acum) {//Función para insertar una venta temporal
        try {
            Connection conexion = DriverManager.getConnection(url + nameBD, usuario, contra);
            CallableStatement cstm = conexion.prepareCall("{call set_venta_temp(?::id_producto_dominio,?,?)}");
            cstm.setString(1, campos[0]);
            cstm.setObject(2, campos[1], Types.INTEGER);
            cstm.setBoolean(3, acum);
            cstm.execute();
            conexion.close();
        } catch (SQLException e) {
            GestorErrores.manejar(e);
        }
    }

    public void eliminarVentaTemp(String ide) {//Función para eliminar una venta temporal
        try {
            Connection conexion = DriverManager.getConnection(url + nameBD, usuario, contra);
            CallableStatement cstm = conexion.prepareCall("{call eliminar_prod(?::id_producto_dominio)}");
            cstm.setString(1, ide);
            cstm.execute();
            conexion.close();
        } catch (SQLException e) {
            GestorErrores.manejar(e);
        }
    }

    public Double sumaVentaTemp() {//Función para obtener la suma de la venta temporal
        Double sum = 0.0;
        try {
            Connection conexion = DriverManager.getConnection(url + nameBD, usuario, contra);
            CallableStatement cstm = conexion.prepareCall("{call suma_venta_temp()}");
            ResultSet rs = cstm.executeQuery();
            while (rs.next()) {
                sum = rs.getDouble(1);
            }
            conexion.close();
        } catch (SQLException e) {
            GestorErrores.manejar(e);
        }
        return sum;
    }

    // FUNCIONES DE LA TABLA VENTA
    public String registrarVenta(String idEmp, String idCli) {//Función para registrar una venta
        String idVenta = "";
        try {
            Connection conexion = DriverManager.getConnection(url + nameBD, usuario, contra);
            CallableStatement cstm = conexion.prepareCall("{call realizar_venta(?::curp_dominio,?::curp_dominio)}");
            cstm.registerOutParameter(1, Types.VARCHAR);
            cstm.setString(1, idEmp);
            cstm.setString(2, idCli);
            cstm.execute();
            idVenta = cstm.getString(1);
            conexion.close();
        } catch (SQLException e) {
            GestorErrores.manejar(e);
        }
        return idVenta;
    }

    public String registrarVentaConFormaPago(String idEmp, String idCli, String formaPago) {//Función para registrar una venta con forma de pago
        String idVenta = registrarVenta(idEmp, idCli);
        if (!idVenta.isEmpty()) {
            try {
                Connection conexion = DriverManager.getConnection(url + nameBD, usuario, contra);
                // Actualizar forma de pago
                PreparedStatement pstm = conexion.prepareStatement("UPDATE venta SET forma_pago=? WHERE id_venta=?");
                pstm.setString(1, formaPago);
                pstm.setString(2, idVenta);
                pstm.executeUpdate();
                // Retroalimentar referencia y empleado en kardex para los productos de esta venta
                // (el trigger kardex se dispara al agregar al carrito, antes de que exista el id_venta)
                String sqlKardex =
                    "UPDATE kardex SET referencia = ?, id_empleado = ?::CHAR(18) " +
                    "WHERE id_kardex IN (" +
                    "  SELECT DISTINCT ON (vd.id_producto) k.id_kardex " +
                    "  FROM venta_detalle vd " +
                    "  JOIN kardex k ON k.id_producto = vd.id_producto " +
                    "  WHERE vd.id_venta = ? " +
                    "  AND k.referencia IS NULL " +
                    "  AND k.tipo_movimiento = 'Venta' " +
                    "  ORDER BY vd.id_producto, k.id_kardex DESC" +
                    ")";
                PreparedStatement psKardex = conexion.prepareStatement(sqlKardex);
                psKardex.setString(1, idVenta);
                psKardex.setString(2, idEmp);
                psKardex.setString(3, idVenta);
                psKardex.executeUpdate();
                conexion.close();
            } catch (SQLException e) {
                GestorErrores.manejar(e);
            }
        }
        return idVenta;
    }

    public ResultSet corteDiario(java.time.LocalDate fecha) {//Función para obtener las ventas del día con forma de pago y empleado
        ResultSet rs = null;
        String instruccion =
            "SELECT v.id_venta, " +
            "CAST(v.fecha_venta AS VARCHAR) AS hora, " +
            "COALESCE(e.nombre, v.id_empleado) AS empleado, " +
            "COALESCE((SELECT SUM(vd.precio_total) FROM venta_detalle vd WHERE vd.id_venta = v.id_venta), 0) AS subtotal, " +
            "GREATEST(0, COALESCE((SELECT SUM(vd.precio_total) FROM venta_detalle vd WHERE vd.id_venta = v.id_venta), 0) - v.total_venta) AS descuento, " +
            "v.total_venta, " +
            "COALESCE(v.forma_pago, 'Efectivo') AS forma_pago " +
            "FROM venta v " +
            "LEFT JOIN empleado e ON v.id_empleado = e.id_empleado " +
            "WHERE v.fecha_venta = ? " +
            "ORDER BY v.id_venta";
        try {
            Connection conexion = DriverManager.getConnection(url + nameBD, usuario, contra);
            PreparedStatement pstm = conexion.prepareStatement(instruccion);
            pstm.setDate(1, java.sql.Date.valueOf(fecha));
            rs = pstm.executeQuery();
        } catch (SQLException e) {
            GestorErrores.manejar(e);
        }
        return rs;
    }

    public ResultSet totalesDia(java.time.LocalDate fecha) {//Función para obtener los 5 totales del día
        return reporte_diario(fecha);
    }

    public ResultSet buscarProductoKardex(String filtro) {//Función para buscar productos por nombre o código
        ResultSet rs = null;
        String instruccion = "SELECT id_producto, nombre, cantidad FROM producto " +
            "WHERE estatus = 'Activo' AND (LOWER(nombre) LIKE LOWER(?) OR LOWER(id_producto) LIKE LOWER(?)) " +
            "ORDER BY nombre LIMIT 20";
        try {
            Connection conexion = DriverManager.getConnection(url + nameBD, usuario, contra);
            PreparedStatement pstm = conexion.prepareStatement(instruccion);
            pstm.setString(1, "%" + filtro + "%");
            pstm.setString(2, "%" + filtro + "%");
            rs = pstm.executeQuery();
        } catch (SQLException e) {
            GestorErrores.manejar(e);
        }
        return rs;
    }

    public ResultSet buscarProductosCotizacion(String filtro) {
        ResultSet rs = null;
        String instruccion = "SELECT id_producto, nombre, precio_menudeo, precio_mayoreo, cantidad FROM producto " +
            "WHERE estatus = 'Activo' AND (LOWER(nombre) LIKE LOWER(?) OR LOWER(id_producto) LIKE LOWER(?)) " +
            "ORDER BY nombre LIMIT 50";
        try {
            Connection conexion = DriverManager.getConnection(url + nameBD, usuario, contra);
            PreparedStatement pstm = conexion.prepareStatement(instruccion);
            pstm.setString(1, "%" + filtro + "%");
            pstm.setString(2, "%" + filtro + "%");
            rs = pstm.executeQuery();
        } catch (SQLException e) {
            GestorErrores.manejar(e);
        }
        return rs;
    }

    public ResultSet kardexProducto(String idProducto, java.time.LocalDate desde, java.time.LocalDate hasta) {//Función para obtener el kardex de un producto filtrado por fechas
        ResultSet rs = null;
        String instruccion =
            "SELECT k.fecha, k.tipo_movimiento, k.cantidad, " +
            "k.existencia_anterior, k.existencia_posterior, k.referencia, " +
            "COALESCE(e.nombre, k.id_empleado) AS empleado " +
            "FROM kardex k " +
            "LEFT JOIN empleado e ON k.id_empleado = e.id_empleado " +
            "WHERE k.id_producto = ? " +
            "AND k.fecha::date >= ? AND k.fecha::date <= ? " +
            "ORDER BY k.fecha DESC";
        try {
            Connection conexion = DriverManager.getConnection(url + nameBD, usuario, contra);
            PreparedStatement pstm = conexion.prepareStatement(instruccion);
            pstm.setString(1, idProducto);
            pstm.setDate(2, java.sql.Date.valueOf(desde));
            pstm.setDate(3, java.sql.Date.valueOf(hasta));
            rs = pstm.executeQuery();
        } catch (SQLException e) {
            GestorErrores.manejar(e);
        }
        return rs;
    }

    // FUNCIONES DE LA TABLA COMPRAS
    public String insertarCompra(String[] campos) { return compras.insertarCompra(campos); }

    public void actualizarCompra(String[] campos) { compras.actualizarCompra(campos); }

    public void eliminarCompra(String idCom) { compras.eliminarCompra(idCom); }

    public void insertarProdCompra(String[] campos, boolean ac) { compras.insertarProdCompra(campos, ac); }

    public void eliminarProdCompra(String[] campos) { compras.eliminarProdCompra(campos); }

    public String resolverCodigo(String codigo) {//Función para resolver un id_producto a partir del id o del código de barras
        String idProd = null;
        String sql = "SELECT id_producto FROM producto WHERE id_producto::varchar = ? OR codigo_barras = ? LIMIT 1;";
        try {
            Connection conexion = DriverManager.getConnection(url + nameBD, usuario, contra);
            PreparedStatement pstm = conexion.prepareStatement(sql);
            pstm.setString(1, codigo);
            pstm.setString(2, codigo);
            ResultSet rs = pstm.executeQuery();
            if (rs.next()) idProd = rs.getString("id_producto");
            conexion.close();
        } catch (SQLException e) {
            System.out.println("Error al resolver codigo: " + e.getMessage());
        }
        return idProd; // null si no existe
    }

    public String[] obtenerProductoParaCompra(String idProducto) {
        // [0]nombre [1]codigo_barras [2]unidad_compra [3]unidad_venta [4]factor [5]lleva_iva("t"/"f") [6]precio_menudeo
        String[] d = null;
        String sql = "SELECT nombre, COALESCE(codigo_barras,'') cb, COALESCE(unidad_compra,'') uc, "
                   + "COALESCE(unidad_venta,'') uv, COALESCE(factor_conversion,1) f, COALESCE(lleva_iva,false) iva, "
                   + "COALESCE(precio_menudeo,0) pm FROM producto WHERE id_producto = ?;";
        try {
            Connection conexion = DriverManager.getConnection(url + nameBD, usuario, contra);
            PreparedStatement pstm = conexion.prepareStatement(sql);
            pstm.setString(1, idProducto);
            ResultSet rs = pstm.executeQuery();
            if (rs.next()) {
                d = new String[]{ rs.getString("nombre"), rs.getString("cb"), rs.getString("uc"),
                    rs.getString("uv"), rs.getString("f"), rs.getBoolean("iva") ? "t" : "f",
                    String.valueOf(rs.getDouble("pm")) };
            }
            conexion.close();
        } catch (SQLException e) {
            System.out.println("Error al leer producto: " + e.getMessage());
        }
        return d; // null si no existe
    }

    public boolean codigoBarrasDuplicado(String codigoBarras, String idExcluir) {//Función para verificar si un código de barras ya está en uso por otro producto
        if (codigoBarras == null || codigoBarras.trim().isEmpty()) return false;
        String sql = "SELECT 1 FROM producto WHERE codigo_barras = ? AND id_producto <> ? LIMIT 1;";
        boolean dup = false;
        try {
            Connection conexion = DriverManager.getConnection(url + nameBD, usuario, contra);
            PreparedStatement pstm = conexion.prepareStatement(sql);
            pstm.setString(1, codigoBarras.trim());
            pstm.setString(2, idExcluir == null ? "" : idExcluir);
            ResultSet rs = pstm.executeQuery();
            dup = rs.next();
            conexion.close();
        } catch (SQLException e) {
            System.out.println("Error al verificar codigo de barras: " + e.getMessage());
        }
        return dup;
    }

    public boolean crearProductoDesdeCompra(String[] d, Integer idCategoria, double maxDescuento) {
        // d: [0]id_producto [1]nombre [2]codigo_barras [3]unidad_compra [4]unidad_venta
        //    [5]factor [6]lleva_iva("t"/"f") [7]precio_menudeo [8]precio_mayoreo [9]precio_compra
        boolean ok = false;
        String sql = "INSERT INTO producto(id_producto, nombre, cantidad, precio_mayoreo, precio_menudeo, "
                   + "max_descuento, codigo_barras, lleva_iva, unidad_compra, unidad_venta, factor_conversion, precio_compra, id_categoria) "
                   + "VALUES (?,?,0,?,?,?,?,?,?,?,?,?,?);";
        try {
            Connection conexion = DriverManager.getConnection(url + nameBD, usuario, contra);
            PreparedStatement pstm = conexion.prepareStatement(sql);
            pstm.setString(1, d[0]);
            pstm.setString(2, d[1]);
            pstm.setDouble(3, Double.parseDouble(d[8]));
            pstm.setDouble(4, Double.parseDouble(d[7]));
            pstm.setDouble(5, maxDescuento);
            pstm.setString(6, d[2].isEmpty() ? null : d[2]);
            pstm.setBoolean(7, "t".equals(d[6]));
            pstm.setString(8, d[3]);
            pstm.setString(9, d[4]);
            pstm.setDouble(10, Double.parseDouble(d[5]));
            pstm.setDouble(11, Double.parseDouble(d[9]));
            if (idCategoria != null && idCategoria > 0) {
                pstm.setInt(12, idCategoria);
            } else {
                pstm.setNull(12, Types.INTEGER);
            }
            pstm.executeUpdate();
            conexion.close();
            ok = true;
        } catch (SQLException e) {
            Mise.JOption(e.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
        return ok;
    }

    public String guardarFacturaCompleta(String[] cab, int idProveedor,
            java.util.List<RenglonCompra> renglones, double subtotal, double iva, double total,
            int idBorradorAEliminar) {
        return compras.guardarFacturaCompleta(cab, idProveedor, renglones, subtotal, iva, total, idBorradorAEliminar);
    }

    public Integer guardarBorrador(String[] cab, int idProveedor, String totalFactura,
            java.util.List<RenglonCompra> renglones, int idBorradorExistente) {
        return compras.guardarBorrador(cab, idProveedor, totalFactura, renglones, idBorradorExistente);
    }

    public String[] obtenerCabeceraBorrador(int idBorrador) { return compras.obtenerCabeceraBorrador(idBorrador); }

    public java.util.List<RenglonCompra> obtenerRenglonesBorrador(int idBorrador) { return compras.obtenerRenglonesBorrador(idBorrador); }

    public void eliminarBorrador(int idBorrador) { compras.eliminarBorrador(idBorrador); }

    // FUNCIONES DE LA TABLA CLIENTE
    public boolean insertarCliente(String[] campos) { return clientes.insertarCliente(campos); }

    public void actualizarCliente(String[] campos, String idC) { clientes.actualizarCliente(campos, idC); }

    public void inactivarCliente(String idC, boolean act) { clientes.inactivarCliente(idC, act); }

    // FUNCIONES DE LA TABLA DE DEVOLUCIONES
    public String insertarDevolucion(String[] campos) { return devoluciones.insertarDevolucion(campos); }

    public void eliminarDevolucion(String idDev) { devoluciones.eliminarDevolucion(idDev); }

    public void insertarProdDevolucion(String[] campos, boolean ac) { devoluciones.insertarProdDevolucion(campos, ac); }

    public void eliminarProdDevolucion(String[] campos) { devoluciones.eliminarProdDevolucion(campos); }

    // FUNCIONES PARA LA TABLA APARTADO
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

    // FUNCIONES PARA LA TABLA APARTADO DETALLE
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

    // FUNCIONES PARA LA TABLA GASTOS
    public void insertarGasto(String[] campos) { gastos.insertarGasto(campos); }

    public void actualizarGasto(String idGasto, String[] campos) { gastos.actualizarGasto(idGasto, campos); }

    // FUNCIONES PARA LA TABLA OTRAS GANANCIAS
    public void insertarOtraGanancia(String[] campos) { ganancias.insertarOtraGanancia(campos); }

    public void actualizarOtraGanancia(String[] campos) { ganancias.actualizarOtraGanancia(campos); }

    // FUNCIONES DE LA TABLA PROVEEDOR
    public Integer insertarProveedor(String nombre, String telefono, String email, String direccion, String rfc) { return proveedores.insertarProveedor(nombre, telefono, email, direccion, rfc); }

    public boolean editarProveedor(int id, String nombre, String telefono, String email, String direccion, String rfc) { return proveedores.editarProveedor(id, nombre, telefono, email, direccion, rfc); }

    public String[] obtenerDatosProveedor(int idProveedor) { return proveedores.obtenerDatosProveedor(idProveedor); }

    public boolean cambiarEstatusProveedor(int id, String estatus) { return proveedores.cambiarEstatusProveedor(id, estatus); }

    public ResultSet obtenerProveedores() { return proveedores.obtenerProveedores(); }

    public boolean folioYaRegistrado(int idProveedor, String folio) { return compras.folioYaRegistrado(idProveedor, folio); }

    public void actualizarTotalesCompra(String idCompra, double subtotal, double iva, double total) { compras.actualizarTotalesCompra(idCompra, subtotal, iva, total); }

    public double ObtenerDato(String nombreCol) {//Función para obtener un dato de la base de datos
        double total = 0;
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = DriverManager.getConnection(url + nameBD, usuario, contra);
            ps = conn.prepareStatement("SELECT * FROM estado_resultados(CAST(? AS NUMERIC))");
            ps.setDouble(1, 0.00);
            rs = ps.executeQuery();

            if (rs.next()) {
                total = rs.getDouble(nombreCol);
            }

        } catch (SQLException e) {
            GestorErrores.registrar(e);
        } finally {
            try {
                if (rs != null)
                    rs.close();
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException e) {
                GestorErrores.registrar(e);
            }
        }
        return total;
    }

    public ResultSet reporte_diario() {//Función para obtener el reporte diario
        ResultSet rs = null;
        try {
            Connection conexion = DriverManager.getConnection(url + nameBD, usuario, contra);
            CallableStatement cstm = conexion.prepareCall("{call reporte_diario()}");
            rs = cstm.executeQuery();
            conexion.close();
        } catch (SQLException e) {
            GestorErrores.manejar(e);
        }
        return rs;
    }

    public ResultSet reporte_diario(java.time.LocalDate fecha) {//Función para obtener el reporte diario por fecha
        ResultSet rs = null;
        try {
            Connection conexion = DriverManager.getConnection(url + nameBD, usuario, contra);
            CallableStatement cstm = conexion.prepareCall("{call reporte_diario(?)}");
            cstm.setDate(1, java.sql.Date.valueOf(fecha));
            rs = cstm.executeQuery();
            conexion.close();
        } catch (SQLException e) {
            GestorErrores.manejar(e);
        }
        return rs;
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

    public ResultSet seleccionarVendedor(String idVendedor) { return empleados.seleccionarVendedor(idVendedor); }

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

    // FUNCIONES DE LA TABLA CATEGORIA
    public boolean insertarCategoria(String nombre, String descripcion) { return categorias.insertarCategoria(nombre, descripcion); }

    public boolean editarCategoria(int id, String nombre, String descripcion) { return categorias.editarCategoria(id, nombre, descripcion); }

    public boolean cambiarEstatusCategoria(int id, String estatus) { return categorias.cambiarEstatusCategoria(id, estatus); }

    public ResultSet obtenerCategorias() { return categorias.obtenerCategorias(); }

    public ResultSet obtenerTodasCategorias() { return categorias.obtenerTodasCategorias(); }

    public ResultSet buscarCategoriasPorNombre(String filtro) { return categorias.buscarCategoriasPorNombre(filtro); }

    // FUNCIONES DE PRODUCTO CON CATEGORÍA
    public void insertarProductoConCodigoYCategoria(String[] datos, Integer idCategoria, double maxDescuento) {//Función para insertar un producto con código y categoría
        String instruccion = "INSERT INTO producto(id_producto, nombre, cantidad, precio_mayoreo, precio_menudeo, id_categoria, max_descuento) VALUES (?,?,?,?,?,?,?);";
        try {
            Connection conexion = DriverManager.getConnection(url + nameBD, usuario, contra);
            PreparedStatement pstm = conexion.prepareStatement(instruccion);
            pstm.setString(1, datos[0]);
            pstm.setString(2, datos[1]);
            pstm.setInt(3, Integer.parseInt(datos[2]));
            pstm.setDouble(4, Double.parseDouble(datos[3]));
            pstm.setDouble(5, Double.parseDouble(datos[4]));
            if (idCategoria != null && idCategoria > 0) {
                pstm.setInt(6, idCategoria);
            } else {
                pstm.setNull(6, Types.INTEGER);
            }
            pstm.setDouble(7, maxDescuento);
            pstm.executeUpdate();
            conexion.close();
        } catch (SQLException e) {
            GestorErrores.manejar(e);
        }
    }

    public void actualizarProductoConCategoria(String ideprod, String[] datos, Integer idCategoria, double maxDescuento) {//Función para actualizar un producto con categoría
        String instruccion = "UPDATE producto SET nombre=?, cantidad=?, precio_mayoreo=?, precio_menudeo=?, id_categoria=?, max_descuento=? WHERE id_producto=?;";
        try {
            Connection conexion = DriverManager.getConnection(url + nameBD, usuario, contra);
            PreparedStatement pstm = conexion.prepareStatement(instruccion);
            pstm.setString(1, datos[0]);
            pstm.setInt(2, Integer.parseInt(datos[1]));
            pstm.setDouble(3, Double.parseDouble(datos[2]));
            pstm.setDouble(4, Double.parseDouble(datos[3]));
            if (idCategoria != null && idCategoria > 0) {
                pstm.setInt(5, idCategoria);
            } else {
                pstm.setNull(5, Types.INTEGER);
            }
            pstm.setDouble(6, maxDescuento);
            pstm.setString(7, ideprod);
            pstm.executeUpdate();
            conexion.close();
        } catch (SQLException e) {
            GestorErrores.manejar(e);
        }
    }

    public void actualizarCamposCompraProducto(String idProducto, String codigoBarras, boolean llevaIva,
            String unidadCompra, String unidadVenta, double factor) {
        String sql = "UPDATE producto SET codigo_barras=?, lleva_iva=?, unidad_compra=?, unidad_venta=?, factor_conversion=? WHERE id_producto=?;";
        try {
            Connection conexion = DriverManager.getConnection(url + nameBD, usuario, contra);
            PreparedStatement pstm = conexion.prepareStatement(sql);
            pstm.setString(1, codigoBarras == null || codigoBarras.isEmpty() ? null : codigoBarras);
            pstm.setBoolean(2, llevaIva);
            pstm.setString(3, unidadCompra);
            pstm.setString(4, unidadVenta);
            pstm.setDouble(5, factor);
            pstm.setString(6, idProducto);
            pstm.executeUpdate();
            conexion.close();
        } catch (SQLException e) {
            Mise.JOption(e.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    // FUNCIONES DE DESCUENTOS
    public double obtenerMaxDescuento(String idProducto) {//Función para obtener el descuento máximo de un producto
        double maxDesc = 100.0;
        String instruccion = "SELECT max_descuento FROM producto WHERE id_producto = ?;";
        try {
            Connection conexion = DriverManager.getConnection(url + nameBD, usuario, contra);
            PreparedStatement pstm = conexion.prepareStatement(instruccion);
            pstm.setString(1, idProducto);
            ResultSet rs = pstm.executeQuery();
            if (rs.next()) {
                maxDesc = rs.getDouble("max_descuento");
            }
            conexion.close();
        } catch (SQLException e) {
            GestorErrores.manejar(e);
        }
        return maxDesc;
    }

    public void actualizarDescuentoTemp(String idProducto, double descuento) {//Función para actualizar el descuento en venta_temp
        try {
            Connection conexion = DriverManager.getConnection(url + nameBD, usuario, contra);
            CallableStatement cstm = conexion.prepareCall("{call actualizar_desc_temp(?::id_producto_dominio, ?::numeric)}");
            cstm.setString(1, idProducto);
            cstm.setDouble(2, descuento);
            cstm.execute();
            conexion.close();
        } catch (SQLException e) {
            GestorErrores.manejar(e);
        }
    }

    public void actualizarDescuentoVenta(String idVenta, String idProducto, double descuento) {//Función para guardar descuento en venta_detalle
        String instruccion = "UPDATE venta_detalle SET descuento_pct = ? WHERE id_venta = ? AND id_producto = ?;";
        try {
            Connection conexion = DriverManager.getConnection(url + nameBD, usuario, contra);
            PreparedStatement pstm = conexion.prepareStatement(instruccion);
            pstm.setDouble(1, descuento);
            pstm.setString(2, idVenta);
            pstm.setString(3, idProducto);
            pstm.executeUpdate();
            conexion.close();
        } catch (SQLException e) {
            GestorErrores.manejar(e);
        }
    }

    // FUNCIONES DE LA TABLA EMPRESA
    public ResultSet obtenerEmpresa() { return empresa.obtenerEmpresa(); }

    public boolean actualizarEmpresa(String nombre, String razonSocial, String rfc,
            String telefono, String correo, String direccion, String ciudad,
            String estado, String cp, String mensajeTicket, String logoRuta) {
        return empresa.actualizarEmpresa(nombre, razonSocial, rfc, telefono, correo,
            direccion, ciudad, estado, cp, mensajeTicket, logoRuta);
    }

    public boolean empresaInicializada() { return empresa.empresaInicializada(); }

    // FUNCIONES DE LISTA DE PRECIOS
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

    public double obtenerMinMaxDescuentoTemp() {//Función para obtener el menor max_descuento entre los productos del carrito
        double minMax = 100.0;
        try {
            Connection conexion = DriverManager.getConnection(url + nameBD, usuario, contra);
            Statement s = conexion.createStatement();
            ResultSet rs = s.executeQuery(
                "SELECT COALESCE(MIN(p.max_descuento), 100) FROM venta_temp vt JOIN producto p ON vt.id_producto = p.id_producto;");
            if (rs.next()) {
                minMax = rs.getDouble(1);
            }
            conexion.close();
        } catch (SQLException e) {
            GestorErrores.manejar(e);
        }
        return minMax;
    }

    // ---- COTIZACIONES ----

    public void limpiarCotizacionTemp() {
        inst("DELETE FROM cotizacion_temp;");
    }

    public void insertarCotizacionTemp(String idProd, int cantidad, String tipoVenta, boolean acum) {
        try {
            Connection conn = DriverManager.getConnection(url + nameBD, usuario, contra);
            int cantExistente = 0;
            if (acum) {
                PreparedStatement chk = conn.prepareStatement(
                    "SELECT COALESCE(cantidad_prod,0) FROM cotizacion_temp WHERE id_producto=?");
                chk.setString(1, idProd);
                ResultSet rs = chk.executeQuery();
                if (rs.next()) cantExistente = rs.getInt(1);
            }
            PreparedStatement del = conn.prepareStatement("DELETE FROM cotizacion_temp WHERE id_producto=?");
            del.setString(1, idProd);
            del.executeUpdate();
            int cantFinal = cantExistente + cantidad;
            String priceCol = "Mayoreo".equals(tipoVenta) ? "precio_mayoreo" : "precio_menudeo";
            PreparedStatement ins = conn.prepareStatement(
                "INSERT INTO cotizacion_temp(id_producto, nombre_p, cantidad_prod, tipo_venta, precio_dado, precio_total, descuento_pct) " +
                "SELECT id_producto, nombre, ?, ?, " + priceCol + ", " + priceCol + "*?, 0 " +
                "FROM producto WHERE id_producto=?");
            ins.setInt(1, cantFinal);
            ins.setString(2, tipoVenta);
            ins.setInt(3, cantFinal);
            ins.setString(4, idProd);
            ins.executeUpdate();
            conn.close();
        } catch (SQLException e) {
            GestorErrores.manejar(e);
        }
    }

    public void eliminarCotizacionTemp(String idProd) {
        try {
            Connection conn = DriverManager.getConnection(url + nameBD, usuario, contra);
            PreparedStatement del = conn.prepareStatement("DELETE FROM cotizacion_temp WHERE id_producto=?");
            del.setString(1, idProd);
            del.executeUpdate();
            conn.close();
        } catch (SQLException e) {
            GestorErrores.manejar(e);
        }
    }

    public ResultSet mostrarCotizacionTemp() {
        return query("SELECT * FROM cotizacion_temp");
    }

    public double sumaCotizacionTemp() {
        double sum = 0.0;
        try {
            Connection conn = DriverManager.getConnection(url + nameBD, usuario, contra);
            ResultSet rs = conn.createStatement().executeQuery(
                "SELECT COALESCE(SUM(precio_total),0) FROM cotizacion_temp");
            if (rs.next()) sum = rs.getDouble(1);
            conn.close();
        } catch (SQLException e) {}
        return sum;
    }

    public void actualizarDescuentoCotiTemp(String idProd, double descuento) {
        String sql = "UPDATE cotizacion_temp SET descuento_pct=?, " +
            "precio_total=precio_dado*cantidad_prod*(1-?/100) WHERE id_producto=?";
        try {
            Connection conn = DriverManager.getConnection(url + nameBD, usuario, contra);
            PreparedStatement pstm = conn.prepareStatement(sql);
            pstm.setDouble(1, descuento);
            pstm.setDouble(2, descuento);
            pstm.setString(3, idProd);
            pstm.executeUpdate();
            conn.close();
        } catch (SQLException e) {
            GestorErrores.manejar(e);
        }
    }

    public String guardarCotizacion(String idEmp, String idCli, double total,
                                    java.sql.Date vigencia, String notas) {
        String idCoti = "";
        try {
            Connection conn = DriverManager.getConnection(url + nameBD, usuario, contra);
            PreparedStatement ins = conn.prepareStatement(
                "INSERT INTO cotizacion(id_empleado, id_cliente, total_cotizacion, fecha_vigencia, notas) " +
                "VALUES(?::CHAR(18), ?::CHAR(18), ?, ?, ?) RETURNING id_cotizacion");
            ins.setString(1, idEmp);
            ins.setString(2, idCli);
            ins.setDouble(3, total);
            if (vigencia != null) ins.setDate(4, vigencia);
            else ins.setNull(4, Types.DATE);
            ins.setString(5, (notas == null || notas.isEmpty()) ? null : notas);
            ResultSet rs = ins.executeQuery();
            if (rs.next()) idCoti = rs.getString(1);

            if (!idCoti.isEmpty()) {
                PreparedStatement det = conn.prepareStatement(
                    "INSERT INTO cotizacion_detalle(id_cotizacion, id_producto, nombre_p, cantidad, " +
                    "tipo_venta, precio_dado, descuento_pct, precio_total) " +
                    "SELECT ?, id_producto, nombre_p, cantidad_prod, tipo_venta, precio_dado, descuento_pct, precio_total " +
                    "FROM cotizacion_temp");
                det.setString(1, idCoti);
                det.executeUpdate();
                conn.createStatement().execute("DELETE FROM cotizacion_temp");
            }
            conn.close();
        } catch (SQLException e) {
            GestorErrores.manejar(e);
        }
        return idCoti;
    }

    public ResultSet obtenerCotizaciones(String filtro) {
        String sql =
            "SELECT c.id_cotizacion, TO_CHAR(c.fecha_cotizacion,'DD/MM/YYYY HH24:MI') AS fecha, " +
            "COALESCE(cl.nombre, 'Público general') AS cliente, " +
            "c.total_cotizacion, c.estatus, " +
            "COALESCE(TO_CHAR(c.fecha_vigencia,'DD/MM/YYYY'),'—') AS vigencia " +
            "FROM cotizacion c LEFT JOIN cliente cl ON c.id_cliente = cl.id_cliente ";
        if (filtro != null && !filtro.isEmpty()) {
            sql += "WHERE c.id_cotizacion ILIKE ? OR cl.nombre ILIKE ? OR c.estatus ILIKE ? ";
        }
        sql += "ORDER BY c.fecha_cotizacion DESC";
        try {
            Connection conn = DriverManager.getConnection(url + nameBD, usuario, contra);
            if (filtro != null && !filtro.isEmpty()) {
                PreparedStatement pstm = conn.prepareStatement(sql);
                String like = "%" + filtro + "%";
                pstm.setString(1, like); pstm.setString(2, like); pstm.setString(3, like);
                return pstm.executeQuery();
            } else {
                return conn.createStatement(
                    ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY).executeQuery(sql);
            }
        } catch (SQLException e) {
            GestorErrores.manejar(e);
        }
        return null;
    }

    public ResultSet obtenerDetalleCotizacion(String idCoti) {
        ResultSet rs = null;
        String sql = "SELECT id_producto, nombre_p, cantidad, tipo_venta, precio_dado, descuento_pct, precio_total " +
            "FROM cotizacion_detalle WHERE id_cotizacion=? ORDER BY id_cotizacion_detalle";
        try {
            Connection conn = DriverManager.getConnection(url + nameBD, usuario, contra);
            PreparedStatement pstm = conn.prepareStatement(sql);
            pstm.setString(1, idCoti);
            rs = pstm.executeQuery();
        } catch (SQLException e) {
            GestorErrores.manejar(e);
        }
        return rs;
    }

    public ResultSet obtenerCotizacionHeader(String idCoti) {
        ResultSet rs = null;
        String sql =
            "SELECT c.id_cotizacion, TO_CHAR(c.fecha_cotizacion,'DD/MM/YYYY HH24:MI') AS fecha, " +
            "COALESCE(cl.nombre,'Público general') AS cliente, c.total_cotizacion, c.estatus, " +
            "COALESCE(TO_CHAR(c.fecha_vigencia,'DD/MM/YYYY'),'') AS vigencia, " +
            "COALESCE(c.notas,'') AS notas, c.id_empleado " +
            "FROM cotizacion c LEFT JOIN cliente cl ON c.id_cliente = cl.id_cliente " +
            "WHERE c.id_cotizacion=?";
        try {
            Connection conn = DriverManager.getConnection(url + nameBD, usuario, contra);
            PreparedStatement pstm = conn.prepareStatement(sql);
            pstm.setString(1, idCoti);
            rs = pstm.executeQuery();
        } catch (SQLException e) {
            GestorErrores.manejar(e);
        }
        return rs;
    }

    public String convertirCotizacionAVenta(String idCoti, String formaPago) {
        String idVenta = "";
        try {
            Connection conn = DriverManager.getConnection(url + nameBD, usuario, contra);

            // Obtener datos de la cotizacion
            PreparedStatement sel = conn.prepareStatement(
                "SELECT id_empleado, id_cliente FROM cotizacion WHERE id_cotizacion=?");
            sel.setString(1, idCoti);
            ResultSet rsCoti = sel.executeQuery();
            if (!rsCoti.next()) { conn.close(); return ""; }
            String idEmp = rsCoti.getString("id_empleado");
            String idCli = rsCoti.getString("id_cliente");
            if (idCli == null || idCli.isEmpty()) idCli = "XAXX111111HCCXXXX0";

            // Copiar cotizacion_detalle a venta_temp
            conn.createStatement().execute("DELETE FROM venta_temp");
            PreparedStatement copyTemp = conn.prepareStatement(
                "INSERT INTO venta_temp(id_producto, nombre_p, cantidad_prod, tipo_venta, precio_dado, precio_total, descuento_pct) " +
                "SELECT id_producto, nombre_p, cantidad, tipo_venta::\"tipoVenta\", precio_dado, precio_total, descuento_pct " +
                "FROM cotizacion_detalle WHERE id_cotizacion=?");
            copyTemp.setString(1, idCoti);
            copyTemp.executeUpdate();

            // Crear venta con el stored procedure
            CallableStatement cstm = conn.prepareCall("{call realizar_venta(?::curp_dominio,?::curp_dominio)}");
            cstm.registerOutParameter(1, Types.VARCHAR);
            cstm.setString(1, idEmp);
            cstm.setString(2, idCli);
            cstm.execute();
            idVenta = cstm.getString(1);

            if (!idVenta.isEmpty()) {
                // Actualizar forma de pago
                PreparedStatement updVenta = conn.prepareStatement(
                    "UPDATE venta SET forma_pago=? WHERE id_venta=?");
                updVenta.setString(1, formaPago);
                updVenta.setString(2, idVenta);
                updVenta.executeUpdate();

                // Retroalimentar kardex
                String sqlKardex =
                    "UPDATE kardex SET referencia=?, id_empleado=?::CHAR(18) " +
                    "WHERE id_kardex IN (" +
                    "  SELECT DISTINCT ON (vd.id_producto) k.id_kardex " +
                    "  FROM venta_detalle vd JOIN kardex k ON k.id_producto=vd.id_producto " +
                    "  WHERE vd.id_venta=? AND k.referencia IS NULL AND k.tipo_movimiento='Venta' " +
                    "  ORDER BY vd.id_producto, k.id_kardex DESC)";
                PreparedStatement psKardex = conn.prepareStatement(sqlKardex);
                psKardex.setString(1, idVenta);
                psKardex.setString(2, idEmp);
                psKardex.setString(3, idVenta);
                psKardex.executeUpdate();

                // Persistir descuentos en venta_detalle
                ResultSet rsDet = conn.prepareStatement(
                    "SELECT id_producto, descuento_pct FROM cotizacion_detalle WHERE id_cotizacion='" + idCoti + "' AND descuento_pct>0")
                    .executeQuery();
                while (rsDet.next()) {
                    actualizarDescuentoVenta(idVenta, rsDet.getString("id_producto"), rsDet.getDouble("descuento_pct"));
                }

                // Marcar cotizacion como convertida
                PreparedStatement updCoti = conn.prepareStatement(
                    "UPDATE cotizacion SET estatus='Convertida', id_venta_convertida=? WHERE id_cotizacion=?");
                updCoti.setString(1, idVenta);
                updCoti.setString(2, idCoti);
                updCoti.executeUpdate();
            }
            conn.createStatement().execute("DELETE FROM venta_temp");
            conn.close();
        } catch (SQLException e) {
            GestorErrores.manejar(e);
        }
        return idVenta;
    }

    public void cancelarCotizacion(String idCoti) {
        try {
            Connection conn = DriverManager.getConnection(url + nameBD, usuario, contra);
            PreparedStatement pstm = conn.prepareStatement(
                "UPDATE cotizacion SET estatus='Cancelada' WHERE id_cotizacion=?");
            pstm.setString(1, idCoti);
            pstm.executeUpdate();
            conn.close();
        } catch (SQLException e) {
            GestorErrores.manejar(e);
        }
    }
}
