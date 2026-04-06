/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 *
 * @author mayra
 */
public class ConexionBD {
    //Server credenciales: url: dpg-cucgfkhopnds739808h0-a.oregon-postgres.render.com contrasenia: IdIwMrtXsrYNLKBsoDM7yR4fW6fGHxWP usuario: daniel183 base: tienda_punto_venta_40vd
    String url = "jdbc:postgresql://localhost:5432/";
    String nameBD = "punto_de_venta";
    String usuario = "postgres";
    String contra = "Daniel183.";
    
    DateTimeFormatter formato = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    // FUNCIONES GENERALES
    public boolean inst(String instruccion) {//Función para ejecutar instrucciones
        boolean band = true;
        try {
            Connection conexion = DriverManager.getConnection(url + nameBD, usuario, contra);
            Statement s = conexion.createStatement();
            int rs = s.executeUpdate(instruccion);
            conexion.close();
            band = true;
        } catch (Exception e) {
            System.out.println("Error al ejecutar la instruccion");
            band = false;
        }
        return band;
    }

    public ResultSet query(String instruccion) {//Función para obtener un ResultSet
        ResultSet rs = null;
        try {
            Connection conexion = DriverManager.getConnection(url + nameBD, usuario, contra);
            Statement s = conexion.createStatement();
            rs = s.executeQuery(instruccion);
            conexion.close();
        } catch (Exception e) {
            System.out.println("Error al obtener el Result Set");
        }
        return rs;
    }

    // FUNCION DE LA TABLA EMPLEADO
    public boolean insertarEmpleado(String[] campos) {//Función para insertar un empleado
        boolean band = false;
        String columnas = "empleado(id_empleado, nombre, puesto, telefono, usuario, contrasenia)";
        String instruccion = "INSERT INTO " + columnas + " VALUES(?,?,?,?,?,?);";
        try {
            Connection conexion = DriverManager.getConnection(url + nameBD, usuario, contra);
            PreparedStatement pstm = conexion.prepareStatement(instruccion);
            pstm.setString(1, campos[0]);
            pstm.setString(2, campos[1]);
            pstm.setObject(3, campos[2], Types.OTHER);
            pstm.setString(4, campos[3]);
            pstm.setString(5, campos[4]);
            pstm.setString(6, campos[5]);
            pstm.executeUpdate();
            conexion.close();
            band = true;
        } catch (SQLException e) {
            Mise.JOption(e.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
        return band;
    }

    public boolean actualizarEmpleado(String[] campos, String idEm) {//Función para actualizar un empleado
        boolean band = false;
        String columnas = "SET puesto=?, telefono=?";
        String instruccion = "UPDATE empleado " + columnas + " WHERE id_empleado=?;";
        try {
            Connection conexion = DriverManager.getConnection(url + nameBD, usuario, contra);
            PreparedStatement pstm = conexion.prepareStatement(instruccion);
            pstm.setObject(1, campos[0], Types.OTHER);
            pstm.setString(2, campos[1]);
            pstm.setString(3, idEm);
            pstm.executeUpdate();
            conexion.close();
            band = true;
        } catch (SQLException e) {
            Mise.JOption(e.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
        return band;
    }

    public boolean inactivarEmpleado(String idEm, boolean act) {//Función para inactivar un empleado
        boolean band = false;
        try {
            Connection conexion = DriverManager.getConnection(url + nameBD, usuario, contra);
            CallableStatement cstm = conexion.prepareCall("{call inactivar_empleado(?::curp_dominio,?)}");
            cstm.setString(1, idEm);
            cstm.setBoolean(2, act);
            cstm.execute();
            conexion.close();
            band = true;
        } catch (SQLException e) {
            Mise.JOption(e.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
        return band;
    }
    
    public boolean verificarUsuario(String uss) {//Función para verificar si un usuario ya existe
        boolean band = false;
        try {
            Connection conexion = DriverManager.getConnection(url + nameBD, usuario, contra);
            CallableStatement cstm = conexion.prepareCall("{call verif_user(?)}");
            cstm.setString(1, uss);
            ResultSet rs = cstm.executeQuery();
            while(rs.next()){
                band = rs.getBoolean(1);
            }
            conexion.close();
        } catch (SQLException e) {
            Mise.JOption(e.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
        return band;
    }
    
    public String[] idEmpleado(String uss, String paswor) {//Función para obtener los datos de un empleado
        String[] dates = new String[4];
        try {
            Connection conexion = DriverManager.getConnection(url + nameBD, usuario, contra);
            CallableStatement cstm = conexion.prepareCall("{call ide_emp(?,?)}");
            cstm.setString(1, uss);
            cstm.setString(2, paswor);
            ResultSet rs = cstm.executeQuery();
            while(rs.next()){
                dates[0] = rs.getString("ide");
                dates[1] = rs.getString("neim");
                dates[2] = rs.getString("pues");
                dates[3] = rs.getString("tel");
            }
            conexion.close();
        } catch (SQLException e) {
            Mise.JOption(e.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
        return dates;
    }

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
            Mise.JOption(e.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
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
            Mise.JOption(e.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
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
            Mise.JOption(e.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean eliminarProducto(String ideprod) {//Función para eliminar un producto
        boolean band = false;
        try {
            Connection conexion = DriverManager.getConnection(url + nameBD, usuario, contra);
            CallableStatement cstm = conexion.prepareCall("{call delete_producto(?::id_producto_dominio)}");
            cstm.setString(1, ideprod);
            cstm.execute();
            conexion.close();
            band = true;
        } catch (SQLException e) {
            Mise.JOption(e.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
        return band;
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
            Mise.JOption(e.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
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
            Mise.JOption(e.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
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
            Mise.JOption(e.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
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
            Mise.JOption(e.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
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
            Mise.JOption(e.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
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
                Mise.JOption(e.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
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
            Mise.JOption(e.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
        return rs;
    }

    public ResultSet totalesDia(java.time.LocalDate fecha) {//Función para obtener los 5 totales del día
        return reporte_diario(fecha);
    }

    public ResultSet buscarProductoKardex(String filtro) {//Función para buscar productos por nombre o código
        ResultSet rs = null;
        String instruccion = "SELECT id_producto, nombre, cantidad FROM producto " +
            "WHERE LOWER(nombre) LIKE LOWER(?) OR LOWER(id_producto) LIKE LOWER(?) " +
            "ORDER BY nombre LIMIT 20";
        try {
            Connection conexion = DriverManager.getConnection(url + nameBD, usuario, contra);
            PreparedStatement pstm = conexion.prepareStatement(instruccion);
            pstm.setString(1, "%" + filtro + "%");
            pstm.setString(2, "%" + filtro + "%");
            rs = pstm.executeQuery();
        } catch (SQLException e) {
            Mise.JOption(e.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
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
            Mise.JOption(e.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
        return rs;
    }

    // FUNCIONES DE LA TABLA COMPRAS
    public String insertarCompra(String[] campos) {//Función para insertar una compra
        String idCompra = "";
        try {
            Connection conexion = DriverManager.getConnection(url + nameBD, usuario, contra);
            CallableStatement cstm = conexion.prepareCall("{call reg_compra(?::curp_dominio,?,?)}");
            cstm.registerOutParameter(1, Types.VARCHAR);
            cstm.setString(1, campos[0]);
            cstm.setString(2, campos[1]);
            cstm.setObject(3, campos[2], Types.NUMERIC);
            cstm.execute();
            idCompra = cstm.getString(1);
            conexion.close();
        } catch (SQLException e) {
            Mise.JOption(e.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
        return idCompra;
    }

    public void actualizarCompra(String[] campos) {//Función para actualizar una compra
        String columnas = "descripcion=?, monto=?";
        String instruccion = "UPDATE compras SET " + columnas + " WHERE id_compra=?;";
        try {
            Connection conexion = DriverManager.getConnection(url + nameBD, usuario, contra);
            PreparedStatement pstm = conexion.prepareCall(instruccion);
            pstm.setString(1, campos[0]);
            pstm.setDouble(2, Double.parseDouble(campos[1]));
            pstm.setString(3, campos[2]);
            pstm.executeUpdate();
            conexion.close();
        } catch (SQLException e) {
            Mise.JOption(e.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    public void eliminarCompra(String idCom) {//Función para eliminar una compra
        try {
            Connection conexion = DriverManager.getConnection(url + nameBD, usuario, contra);
            CallableStatement cstm = conexion.prepareCall("{call elim_compra(?)}");
            cstm.setString(1, idCom);
            cstm.execute();
            conexion.close();
        } catch (SQLException e) {
            Mise.JOption(e.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    // FUNCIONES EN LA TABLA COMPRA PRODUCTOS
    public void insertarProdCompra(String[] campos, boolean ac) {//Función para insertar un producto en una compra
        try {
            Connection conexion = DriverManager.getConnection(url + nameBD, usuario, contra);
            // Obtener id_empleado de la compra para el kardex
            String idEmpCompra = "";
            PreparedStatement psEmp = conexion.prepareStatement("SELECT id_empleado FROM compras WHERE id_compra = ?");
            psEmp.setString(1, campos[0]);
            ResultSet rsEmp = psEmp.executeQuery();
            if (rsEmp.next()) idEmpCompra = rsEmp.getString(1);
            // Establecer variables de sesión para el trigger kardex
            Statement stmtSet = conexion.createStatement();
            stmtSet.execute("SET kardex.tipo = 'Compra'");
            stmtSet.execute("SET kardex.referencia = '" + campos[0] + "'");
            if (!idEmpCompra.isEmpty())
                stmtSet.execute("SET kardex.empleado = '" + idEmpCompra + "'");
            CallableStatement cstm = conexion.prepareCall("{call reg_compra_prod(?,?::id_producto_dominio,?,?,?)}");
            cstm.setString(1, campos[0]);
            cstm.setString(2, campos[1]);
            cstm.setObject(3, campos[2], Types.NUMERIC);
            cstm.setObject(4, campos[3], Types.INTEGER);
            cstm.setBoolean(5, ac);
            cstm.execute();
            conexion.close();
        } catch (SQLException e) {
            Mise.JOption(e.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    public void eliminarProdCompra(String[] campos) {//Función para eliminar un producto de una compra
        try {
            Connection conexion = DriverManager.getConnection(url + nameBD, usuario, contra);
            CallableStatement cstm = conexion.prepareCall("{call elim_compra_prod(?,?::id_producto_dominio)}");
            cstm.setString(1, campos[0]);
            cstm.setString(2, campos[1]);
            cstm.execute();
            conexion.close();
        } catch (SQLException e) {
            Mise.JOption(e.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    // FUNCIONES DE LA TABLA CLIENTE
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
            Mise.JOption(e.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
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
            Mise.JOption(e.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
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
            Mise.JOption(e.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    // FUNCIONES DE LA TABLA DE DEVOLUCIONES
    public String insertarDevolucion(String[] campos) {//Función para insertar una devolución
        String idDev = "";
        try {
            Connection conexion = DriverManager.getConnection(url + nameBD, usuario, contra);
            CallableStatement cstm = conexion.prepareCall("{call reg_devolucion(?,?)}");
            cstm.registerOutParameter(1, Types.VARCHAR);
            cstm.setString(1, campos[0]);
            cstm.setObject(2, campos[1], Types.NUMERIC);
            cstm.execute();
            idDev = cstm.getString(1);
            conexion.close();
        } catch (SQLException e) {
            Mise.JOption(e.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
        return idDev;
    }

    public void eliminarDevolucion(String idDev) {//Función para eliminar una devolución
        try {
            Connection conexion = DriverManager.getConnection(url + nameBD, usuario, contra);
            CallableStatement cstm = conexion.prepareCall("{call elim_devolucion(?)}");
            cstm.setString(1, idDev);
            cstm.execute();
            conexion.close();
        } catch (SQLException e) {
            Mise.JOption(e.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    // FUNCIONES PARA LA TABLA DE DEVOLUCION DETALLE
    public void insertarProdDevolucion(String[] campos, boolean ac) {//Función para insertar un producto en una devolución
        try {
            Connection conexion = DriverManager.getConnection(url + nameBD, usuario, contra);
            // Obtener id_empleado de la venta original para el kardex
            String idEmpDev = "";
            PreparedStatement psEmp = conexion.prepareStatement(
                "SELECT v.id_empleado FROM devolucion_ventas dv " +
                "JOIN venta v ON v.id_venta = dv.id_venta " +
                "WHERE dv.id_devolucion = ?");
            psEmp.setString(1, campos[0]);
            ResultSet rsEmp = psEmp.executeQuery();
            if (rsEmp.next()) idEmpDev = rsEmp.getString(1);
            // Establecer variables de sesión para el trigger kardex
            Statement stmtSet = conexion.createStatement();
            stmtSet.execute("SET kardex.tipo = 'Devolucion'");
            stmtSet.execute("SET kardex.referencia = '" + campos[0] + "'");
            if (!idEmpDev.isEmpty())
                stmtSet.execute("SET kardex.empleado = '" + idEmpDev + "'");
            CallableStatement cstm = conexion.prepareCall("{call reg_devolucion_prod(?,?,?::id_producto_dominio,?,?)}");
            cstm.setString(1, campos[0]);
            cstm.setString(2, campos[1]);
            cstm.setString(3, campos[2]);
            cstm.setBoolean(4, ac);
            cstm.setObject(5, campos[3], Types.INTEGER);
            cstm.execute();
            conexion.close();
        } catch (SQLException e) {
            Mise.JOption(e.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    public void eliminarProdDevolucion(String[] campos) {//Función para eliminar un producto de una devolución
        try {
            Connection conexion = DriverManager.getConnection(url + nameBD, usuario, contra);
            CallableStatement cstm = conexion.prepareCall("{call elim_devolucion_prod(?,?::id_producto_dominio)}");
            cstm.setString(1, campos[0]);
            cstm.setString(2, campos[1]);
            cstm.execute();
            conexion.close();
        } catch (SQLException e) {
            Mise.JOption(e.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

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
            Mise.JOption(e.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
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
            Mise.JOption(e.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
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
            Mise.JOption(e.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    public void revisarApartado() {//Función para revisar los apartados
        try {
            Connection conexion = DriverManager.getConnection(url + nameBD, usuario, contra);
            CallableStatement cstm = conexion.prepareCall("{call ap_vigentes()}");
            cstm.execute();
            conexion.close();
        } catch (SQLException e) {
            Mise.JOption(e.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
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
            Mise.JOption(e.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
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
            Mise.JOption(e.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
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
            Mise.JOption(e.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
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
            Mise.JOption(e.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
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
            Mise.JOption(e.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    // FUNCIONES PARA LA TABLA GASTOS
    public void insertarGasto(String[] campos) {//Función para insertar un gasto
        String columnas = "gastos(id_empleado, descripcion, monto)";
        String instruccion = "INSERT INTO " + columnas + " VALUES(?,?,?);";
        try {
            Connection conexion = DriverManager.getConnection(url + nameBD, usuario, contra);
            PreparedStatement pstm = conexion.prepareStatement(instruccion);
            pstm.setString(1, campos[0]);
            pstm.setString(2, campos[1]);
            pstm.setDouble(3, Double.parseDouble(campos[2]));
            pstm.executeUpdate();
            conexion.close();
        } catch (SQLException e) {
            Mise.JOption(e.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    public void actualizarGasto(String idGasto, String[] campos) {//Función para actualizar un gasto
        String columnas = "SET descripcion=?, monto=?";
        String instruccion = "UPDATE gastos " + columnas + " WHERE id_gasto=?;";
        try {
            Connection conexion = DriverManager.getConnection(url + nameBD, usuario, contra);
            PreparedStatement pstm = conexion.prepareStatement(instruccion);
            pstm.setString(1, campos[0]);
            pstm.setDouble(2, Double.parseDouble(campos[1]));
            pstm.setString(3, idGasto);
            pstm.executeUpdate();
            conexion.close();
        } catch (SQLException e) {
            Mise.JOption(e.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    // FUNCIONES PARA LA TABLA OTRAS GANANCIAS
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
            Mise.JOption(e.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
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
            Mise.JOption(e.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    // FUNCIONES DE LA TABLA PROVEEDOR
    public boolean insertarProveedor(String nombre, String telefono, String email, String direccion) {//Función para insertar un proveedor
        boolean band = false;
        String instruccion = "INSERT INTO proveedor(nombre, telefono, email, direccion) VALUES(?,?,?,?);";
        try {
            Connection conexion = DriverManager.getConnection(url + nameBD, usuario, contra);
            PreparedStatement pstm = conexion.prepareStatement(instruccion);
            pstm.setString(1, nombre);
            pstm.setString(2, telefono.isEmpty() ? null : telefono);
            pstm.setString(3, email.isEmpty() ? null : email);
            pstm.setString(4, direccion.isEmpty() ? null : direccion);
            pstm.executeUpdate();
            conexion.close();
            band = true;
        } catch (SQLException e) {
            Mise.JOption(e.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
        return band;
    }

    public boolean editarProveedor(int id, String nombre, String telefono, String email, String direccion) {//Función para editar un proveedor
        boolean band = false;
        String instruccion = "UPDATE proveedor SET nombre=?, telefono=?, email=?, direccion=? WHERE id_proveedor=?;";
        try {
            Connection conexion = DriverManager.getConnection(url + nameBD, usuario, contra);
            PreparedStatement pstm = conexion.prepareStatement(instruccion);
            pstm.setString(1, nombre);
            pstm.setString(2, telefono.isEmpty() ? null : telefono);
            pstm.setString(3, email.isEmpty() ? null : email);
            pstm.setString(4, direccion.isEmpty() ? null : direccion);
            pstm.setInt(5, id);
            pstm.executeUpdate();
            conexion.close();
            band = true;
        } catch (SQLException e) {
            Mise.JOption(e.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
        return band;
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
            Mise.JOption(e.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
        return band;
    }

    public ResultSet obtenerProveedores() {//Función para obtener proveedores activos
        return query("SELECT id_proveedor, nombre FROM proveedor WHERE estatus='Activo' ORDER BY nombre");
    }

    public String insertarCompraConProveedor(String[] campos, int idProveedor) {//Función para insertar una compra con proveedor
        String idCompra = "";
        try {
            Connection conexion = DriverManager.getConnection(url + nameBD, usuario, contra);
            String instruccion = "INSERT INTO compras(id_empleado, descripcion, monto, id_proveedor) VALUES(?::curp_dominio, ?, ?::numeric, ?) RETURNING id_compra;";
            PreparedStatement pstm = conexion.prepareStatement(instruccion);
            pstm.setString(1, campos[0]);
            pstm.setString(2, campos[1]);
            pstm.setString(3, campos[2]);
            if(idProveedor > 0){
                pstm.setInt(4, idProveedor);
            } else {
                pstm.setNull(4, Types.INTEGER);
            }
            ResultSet rs = pstm.executeQuery();
            if(rs.next()){
                idCompra = rs.getString("id_compra");
            }
            conexion.close();
        } catch (SQLException e) {
            Mise.JOption(e.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
        return idCompra;
    }

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
            System.out.println("Error al obtener " + nombreCol + ": " + e.getMessage());
        } finally {
            try {
                if (rs != null)
                    rs.close();
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException e) {
                System.out.println("Error al cerrar conexiones: " + e.getMessage());
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
            Mise.JOption(e.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
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
            Mise.JOption(e.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
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
            System.out.println("Error: " + e.toString());
        }
        return resultado;

    }

    public ResultSet seleccionarVendedor(String idVendedor) {//Función para seleccionar un vendedor
        ResultSet resultado = null;
        try {
            Connection conexion = DriverManager.getConnection(url + nameBD, usuario, contra);
            PreparedStatement consulta = conexion.prepareStatement(
                    "SELECT nombre FROM empleado where id_empleado = '" + idVendedor + "'");
            resultado = consulta.executeQuery();
        } catch (Exception e) {
            System.out.println("Error: " + e.toString());
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
            System.out.println("Error: " + e.toString());
        }
        return resultado;
    }

    // FUNCIONES DE LA TABLA CATEGORIA
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
            Mise.JOption(e.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
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
            Mise.JOption(e.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
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
            Mise.JOption(e.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
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
            System.out.println("Error al buscar categorías: " + e.getMessage());
        }
        return rs;
    }

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
            Mise.JOption(e.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
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
            Mise.JOption(e.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
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
            Mise.JOption(e.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
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
            Mise.JOption(e.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    // FUNCIONES DE LISTA DE PRECIOS
    public ResultSet obtenerListas() {//Función para obtener las listas de precios activas
        return query("SELECT id_lista, nombre FROM lista_precios WHERE estatus='Activo' ORDER BY id_lista");
    }

    public double obtenerPrecioEnLista(String idProducto, int idLista) {//Función para obtener el precio de un producto en una lista (-1 si no existe)
        double precio = -1.0;
        String instruccion = "SELECT precio FROM producto_precio WHERE id_producto=? AND id_lista=?;";
        try {
            Connection conexion = DriverManager.getConnection(url + nameBD, usuario, contra);
            PreparedStatement pstm = conexion.prepareStatement(instruccion);
            pstm.setString(1, idProducto);
            pstm.setInt(2, idLista);
            ResultSet rs = pstm.executeQuery();
            if (rs.next()) {
                precio = rs.getDouble("precio");
            }
            conexion.close();
        } catch (SQLException e) {
            Mise.JOption(e.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
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
            Mise.JOption(e.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
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
            Mise.JOption(e.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
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
            Mise.JOption(e.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
        return minMax;
    }
}
