package puntoventa.db;

import puntoventa.util.*;

import java.sql.*;
import java.time.format.DateTimeFormatter;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

/**
 * Clase base de TODOS los DAOs.
 *
 * Absorbe lo común que antes vivía en ConexionBD: credenciales de la base,
 * el constructor que lee db.properties, las funciones generales inst()/query()
 * y el helper nvl(). Cada DAO la extiende y, al instanciarse, carga db.properties
 * igual que hoy hace cada new ConexionBD() (mismo comportamiento, sin pool).
 *
 * @author mayra
 */
public class BaseDAO {
    //Server credenciales: url: dpg-cucgfkhopnds739808h0-a.oregon-postgres.render.com contrasenia: IdIwMrtXsrYNLKBsoDM7yR4fW6fGHxWP usuario: daniel183 base: tienda_punto_venta_40vd
    String url = "jdbc:postgresql://localhost:5432/";
    String nameBD = "punto_de_venta";
    String usuario = "postgres";
    String contra = "root";

    DateTimeFormatter formato = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public BaseDAO() {
        File f = new File("db.properties");
        if (!f.exists()) return;
        try (InputStream in = new FileInputStream(f)) {
            Properties props = new Properties();
            props.load(in);
            String host = props.getProperty("db.host", "localhost").trim();
            String port = props.getProperty("db.port", "5432").trim();
            String name = props.getProperty("db.name", nameBD).trim();
            String user = props.getProperty("db.user", usuario).trim();
            String pass = props.getProperty("db.password", contra);
            url = "jdbc:postgresql://" + host + ":" + port + "/";
            nameBD = name;
            usuario = user;
            contra = pass != null ? pass : contra;
        } catch (Exception ignored) {}
    }

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
            GestorErrores.registrar(e);
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
            GestorErrores.registrar(e);
        }
        return rs;
    }

    protected static String nvl(String s) { return s == null ? "" : s; }
}
