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
    String nameBD = "tienda_punto_venta";
    String usuario = "postgres";
    String contra = "Daniel183.";
    
    DateTimeFormatter formato = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // FUNCIONES GENERALES
    public boolean inst(String instruccion) {
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

    public ResultSet query(String instruccion) {
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
    public boolean insertarEmpleado(String[] campos) {
        boolean band = false;
        String columnas = "cliente(id_cliente, nombre, telefono)";
        String instruccion = "INSERT INTO " + columnas + " VALUES(?,?,?);";
        try {
            Connection conexion = DriverManager.getConnection(url + nameBD, usuario, contra);
            PreparedStatement pstm = conexion.prepareStatement(instruccion);
            pstm.setString(1, campos[0]);
            pstm.setString(2, campos[1]);
            pstm.setString(3, campos[2])
            pstm.executeUpdate();
            conexion.close();
            band = true;
        } catch (SQLException e) {
            Mise.JOption(e.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
        return band;
    }

    public boolean actualizarEmpleado(String[] campos, String idEm) {
        boolean band = false;
        String columnas = "SET puesto=?, telefono=?";
        String instruccion = "UPDATE empleado " + columnas + " WHERE id_empleado=?;";
        try {
            Connection conexion = DriverManager.getConnection(url + nameBD, usuario, contra);
            PreparedStatement pstm = conexion.prepareStatement(instruccion);
            pstm.setString(1, campos[0]);
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

    public void inactivarEmpleado(String idEm, boolean act) {
        try {
            Connection conexion = DriverManager.getConnection(url + nameBD, usuario, contra);
            CallableStatement cstm = conexion.prepareCall("{call inactivar_cliente(?::curp_dominio,?)}");
            cstm.setString(1, idEm)
            cstm.setBoolean(2, act);
            cstm.execute();
            conexion.close();
        } catch (SQLException e) {
            Mise.JOption(e.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public String[] idEmpleado(String uss, String paswor) {
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
    public void insertarProducto(String[] datos) {
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

    public void actualizarProducto(String ideprod, String[] datos) {
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

    public boolean eliminarProducto(String ideprod) {
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

    public boolean hayPocosProductos() {
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
    public void insertarVentaTemp(String[] campos, boolean acum) {
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

    public void eliminarVentaTemp(String ide) {
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

    public Double sumaVentaTemp() {
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
    public String registrarVenta(String idEmp, String idCli) {
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

    // FUNCIONES DE LA TABLA COMPRAS
    public String insertarCompra(String[] campos) {
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

    public void actualizarCompra(String[] campos) {
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

    public void eliminarCompra(String idCom) {
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
    public void insertarProdCompra(String[] campos, boolean ac) {
        try {
            Connection conexion = DriverManager.getConnection(url + nameBD, usuario, contra);
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

    public void eliminarProdCompra(String[] campos) {
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
    public boolean insertarCliente(String[] campos) {
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

    public void actualizarCliente(String[] campos, String idC) {
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

    public void inactivarCliente(String idC, boolean act) {
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
    public String insertarDevolucion(String[] campos) {
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

    public void eliminarDevolucion(String idDev) {
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
    public void insertarProdDevolucion(String[] campos, boolean ac) {
        try {
            Connection conexion = DriverManager.getConnection(url + nameBD, usuario, contra);
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

    public void eliminarProdDevolucion(String[] campos) {
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
    public String insertarApartado(String[] campos) {
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
    
    public boolean actualizarApartado(String idAp, Double cantPag){
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

    public void eliminarApartado(String idAp) {
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

    public void revisarApartado() {
        try {
            Connection conexion = DriverManager.getConnection(url + nameBD, usuario, contra);
            CallableStatement cstm = conexion.prepareCall("{call ap_vigentes()}");
            cstm.execute();
            conexion.close();
        } catch (SQLException e) {
            Mise.JOption(e.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    public double cantidadCancelarApartado(String idAp) {
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

    public void cancelarApartado(String idAp) {
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

    public void entregarApartado(String idAp) {
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
    public void insertarProdApartado(String[] campos, boolean ac) {
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

    public void eliminarProdApartado(String[] campos) {
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
    public void insertarGasto(String[] campos) {
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

    public void actualizarGasto(String idGasto, String[] campos) {
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
    public void insertarOtraGanancia(String[] campos) {
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

    public void actualizarOtraGanancia(String[] campos) {
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

    public double ObtenerDato(String nombreCol) {
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

    public ResultSet reporte_diario() {
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

    public ResultSet seleccionarVendedor(String idVendedor) {
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

    public ResultSet seleccionarProductos(String idApartado) {
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
}
