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
    
    String url = "jdbc:postgresql://localhost:5432/";
    String nameBD = "punto_venta";
    String usuario = "postgres";
    String contra = "mayraK";
    
    DateTimeFormatter formato = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    public boolean inst(String instruccion) {
        boolean band = true;
        
        try{
            Connection conexion = DriverManager.getConnection("jdbc:postgresql://localhost:5432/" + nameBD, "postgres", "mayraK");
            Statement s = conexion.createStatement();
            int rs = s.executeUpdate(instruccion);
            conexion.close();
            band = true;
        }catch(Exception e){
            System.out.println("Error al ejecutar la instruccion");
            band = false;
        }
        return band;
    }
    
    public ResultSet query(String instruccion) {
        ResultSet rs = null;
        
        try{
            Connection conexion = DriverManager.getConnection(url + nameBD, usuario, contra);
            Statement s = conexion.createStatement();
            rs = s.executeQuery(instruccion);
            conexion.close();
        }catch(Exception e){
            System.out.println("Error al obtener el Result Set");
        }
        return rs;
    }
    
    public boolean insertarApartado(String dato) {
        boolean band = false;
        
        String nameTabla = "apartado ";
        String columnas = "(idapartado))";
        
        String instruccion = "INSERT INTO " + nameTabla + columnas + " VALUES (?);";
        
        try{
            Connection conexion = DriverManager.getConnection(url + nameBD, usuario, contra);
            PreparedStatement pstm = conexion.prepareStatement(instruccion);
            pstm.setString(1, dato);
            conexion.close();
            band = true;
        }catch(Exception e){
            System.out.println(e);
        }
        return band;
    }
    
    public boolean insertarCliente(String[] datos) {
        boolean band = false;
        
        String nameTabla = "cliente ";
        String columnas = "(idcliente, nombre, numero)";
        
        String instruccion = "INSERT INTO " + nameTabla + columnas + " VALUES (?,?,?);";
 
        try{
            Connection conexion = DriverManager.getConnection(url + nameBD, usuario, contra);
            PreparedStatement pstm = conexion.prepareStatement(instruccion);
            pstm.setString(1, datos[0]);
            pstm.setString(2, datos[1]);
            pstm.setInt(3, Integer.parseInt(datos[2]));
            conexion.close();
            band = true;
        }catch(Exception e){
            System.out.println(e);
        }
        return band;
    }
    
    public boolean insertarCompra(String[] datos) {
        boolean band = false;
        
        String nameTabla = "compra ";
        String columnas = "(idcompra, fecha_adquirido, total)";
        
        String instruccion = "INSERT INTO " + nameTabla + columnas + " VALUES (?,?,?);";
 
        LocalDate fechAdq = LocalDate.parse(datos[1], formato);
        
        try{
            Connection conexion = DriverManager.getConnection(url + nameBD, usuario, contra);
            PreparedStatement pstm = conexion.prepareStatement(instruccion);
            pstm.setString(1, datos[0]);
            pstm.setDate(2, Date.valueOf(fechAdq));
            pstm.setDouble(3, Double.parseDouble(datos[2]));
            conexion.close();
            band = true;
        }catch(Exception e){
            System.out.println(e);
        }
        return band;
    }
    
    public boolean insertarCompraClienteReg(String[] datos) {
        boolean band = false;
        
        String nameTabla = "compraclienteregistrado ";
        String columnas = "(idventa, idcliente)";
        
        String instruccion = "INSERT INTO " + nameTabla + columnas + " VALUES (?,?);";
 
        try{
            Connection conexion = DriverManager.getConnection(url + nameBD, usuario, contra);
            PreparedStatement pstm = conexion.prepareStatement(instruccion);
            pstm.setString(1, datos[0]);
            pstm.setString(2, datos[1]);
            conexion.close();
            band = true;
        }catch(Exception e){
            System.out.println(e);
        }
        return band;
    }
    
    public boolean insertarCompraProducto(String[] datos) {
        boolean band = false;
        
        String nameTabla = "compraproducto ";
        String columnas = "(cantidad, precio_adquirido, idproducto, idcompra)";
        
        String instruccion = "INSERT INTO " + nameTabla + columnas + " VALUES (?,?,?,?);";
 
        try{
            Connection conexion = DriverManager.getConnection(url + nameBD, usuario, contra);
            PreparedStatement pstm = conexion.prepareStatement(instruccion);
            pstm.setInt(1, Integer.parseInt(datos[0]));
            pstm.setDouble(2, Double.parseDouble(datos[1]));
            pstm.setString(3, datos[2]);
            pstm.setString(4, datos[3]);
            conexion.close();
            band = true;
        }catch(Exception e){
            System.out.println(e);
        }
        return band;
    }
    
    public boolean insertarDetalleApartado(String[] datos) {
        boolean band = false;
        
        String nameTabla = "detalleapartado ";
        String columnas = "(cantidad, precio, idproducto, idapartado)";
        
        String instruccion = "INSERT INTO " + nameTabla + columnas + " VALUES (?,?,?,?);";
 
        try{
            Connection conexion = DriverManager.getConnection(url + nameBD, usuario, contra);
            PreparedStatement pstm = conexion.prepareStatement(instruccion);
            pstm.setInt(1, Integer.parseInt(datos[0]));
            pstm.setDouble(2, Double.parseDouble(datos[1]));
            pstm.setString(3, datos[2]);
            pstm.setString(4, datos[3]);
            conexion.close();
            band = true;
        }catch(Exception e){
            System.out.println(e);
        }
        return band;
    }
    
    public boolean insertarDetallesDevolucion(String[] datos) {
        boolean band = false;
        
        String nameTabla = "detallesdevolucion ";
        String columnas = "(precio_regresar, cantidad, iddev, idproducto)";
        
        String instruccion = "INSERT INTO " + nameTabla + columnas + " VALUES (?,?,?,?);";
 
        try{
            Connection conexion = DriverManager.getConnection(url + nameBD, usuario, contra);
            PreparedStatement pstm = conexion.prepareStatement(instruccion);
            pstm.setDouble(1, Double.parseDouble(datos[0]));
            pstm.setInt(2, Integer.parseInt(datos[1]));
            pstm.setString(3, datos[2]);
            pstm.setString(4, datos[3]);
            conexion.close();
            band = true;
        }catch(Exception e){
            System.out.println(e);
        }
        return band;
    }
    
    public boolean insertarDetalleVenta(String[] datos) {
        boolean band = false;
        
        String nameTabla = "detalleventa ";
        String columnas = "(cantidad, precio, idproducto, idventa)";
        
        String instruccion = "INSERT INTO " + nameTabla + columnas + " VALUES (?,?,?,?);";
 
        try{
            Connection conexion = DriverManager.getConnection(url + nameBD, usuario, contra);
            PreparedStatement pstm = conexion.prepareStatement(instruccion);
            pstm.setInt(1, Integer.parseInt(datos[0]));
            pstm.setDouble(2, Double.parseDouble(datos[1]));
            pstm.setString(3, datos[2]);
            pstm.setString(4, datos[3]);
            conexion.close();
            band = true;
        }catch(Exception e){
            System.out.println(e);
        }
        return band;
    }
    
    public boolean insertarDevolucion(String[] datos) {
        boolean band = false;
        
        String nameTabla = "devolucion ";
        String columnas = "(iddev, fecha_devolucion, motivo, monto, idventa)";
        
        String instruccion = "INSERT INTO " + nameTabla + columnas + " VALUES (?,?,?,?,?);";
 
        LocalDate fechaDev = LocalDate.parse(datos[1], formato);
        
        try{
            Connection conexion = DriverManager.getConnection(url + nameBD, usuario, contra);
            PreparedStatement pstm = conexion.prepareStatement(instruccion);
            pstm.setString(1, datos[0]);
            pstm.setDate(2, Date.valueOf(fechaDev));
            pstm.setString(3, datos[2]);
            pstm.setDouble(4, Double.parseDouble(datos[3]));
            pstm.setString(5, datos[4]);
            conexion.close();
            band = true;
        }catch(Exception e){
            System.out.println(e);
        }
        return band;
    }
    
    public boolean insertarGastos(String[] datos) {
        boolean band = false;
        
        String nameTabla = "gastos ";
        String columnas = "(idgastos, fecha_realizado, monto, motivo, idvendedor)";
        
        String instruccion = "INSERT INTO " + nameTabla + columnas + " VALUES (?,?,?,?,?);";
 
        LocalDate fechaRea = LocalDate.parse(datos[1], formato);
        
        try{
            Connection conexion = DriverManager.getConnection(url + nameBD, usuario, contra);
            PreparedStatement pstm = conexion.prepareStatement(instruccion);
            pstm.setString(1, datos[0]);
            pstm.setDate(2, Date.valueOf(fechaRea));
            pstm.setDouble(3, Double.parseDouble(datos[2]));
            pstm.setString(4, datos[3]);
            pstm.setString(5, datos[4]);
            conexion.close();
            band = true;
        }catch(Exception e){
            System.out.println(e);
        }
        return band;
    }
    
    public boolean insertarProducto(String[] datos) {
        boolean band = false;
        
        String nameTabla = "producto ";
        String columnas = "(idproducto, nombre, cantidad, precio_mayoreo, precio_menudeo)";
        
        String instruccion = "INSERT INTO " + nameTabla + columnas + " VALUES (?,?,?,?,?);";
        
        try{
            Connection conexion = DriverManager.getConnection(url + nameBD, usuario, contra);
            PreparedStatement pstm = conexion.prepareStatement(instruccion);
            pstm.setString(1, datos[0]);
            pstm.setString(2, datos[1]);
            pstm.setInt(3, Integer.parseInt(datos[2]));
            pstm.setDouble(4, Double.parseDouble(datos[3]));
            pstm.setDouble(5, Double.parseDouble(datos[4]));
            pstm.executeUpdate();
            conexion.close();
            band = true;
        }catch(Exception e){
            System.out.println(e);
        }
        return band;
    }
    
    public boolean insertarVendedor(String[] datos) {
        boolean band = false;
        
        String nameTabla = "vendedor ";
        String columnas = "(idvendedor, nombre, salario, fecha_ingreso)";
        
        String instruccion = "INSERT INTO " + nameTabla + columnas + " VALUES (?,?,?,?);";
        
        LocalDate fechaRea = LocalDate.parse(datos[3], formato);
 
        try{
            Connection conexion = DriverManager.getConnection(url + nameBD, usuario, contra);
            PreparedStatement pstm = conexion.prepareStatement(instruccion);
            pstm.setString(1, datos[0]);
            pstm.setString(2, datos[1]);
            pstm.setDouble(3, Double.parseDouble(datos[2]));
            pstm.setDate(4, Date.valueOf(fechaRea));
            conexion.close();
            band = true;
        }catch(Exception e){
            System.out.println(e);
        }
        return band;
    }
    
    public boolean insertarVenta(String[] datos) {
        boolean band = false;
        
        String nameTabla = "venta ";
        String columnas = "(idventa, tipo_venta, total, fecha_venta, idvendedor)";
        
        String instruccion = "INSERT INTO " + nameTabla + columnas + " VALUES (?,?,?,?,?);";
        
        LocalDate fechaVent = LocalDate.parse(datos[3], formato);
 
        try{
            Connection conexion = DriverManager.getConnection(url + nameBD, usuario, contra);
            PreparedStatement pstm = conexion.prepareStatement(instruccion);
            pstm.setString(1, datos[0]);
            pstm.setString(2, datos[1]);
            pstm.setDouble(3, Double.parseDouble(datos[2]));
            pstm.setDate(4, Date.valueOf(fechaVent));
            pstm.setString(5, datos[4]);
            conexion.close();
            band = true;
        }catch(Exception e){
            System.out.println(e);
        }
        return band;
    }
    
    public boolean insertarRegVentaT(String[] datos){
        boolean band = false;
        
        String nameTabla = "registroventat ";
        String columnas = "(idventat, totalventat)";
        
        String instruccion = "INSERT INTO " + nameTabla + columnas + " VALUES (?,?);";
 
        try{
            Connection conexion = DriverManager.getConnection(url + nameBD, usuario, contra);
            PreparedStatement pstm = conexion.prepareStatement(instruccion);
            pstm.setString(1, datos[0]);
            pstm.setDouble(2, Double.parseDouble(datos[1]));
            conexion.close();
            band = true;
        }catch(Exception e){
            System.out.println(e);
        }
        return band;
    }
    
}
