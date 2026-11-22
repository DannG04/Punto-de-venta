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

    /** Abre una conexión nueva. Quien la pide la cierra (usar siempre try-with-resources). */
    protected Connection conectar() throws SQLException {
        return DriverManager.getConnection(url + nameBD, usuario, contra);
    }

    /**
     * Asigna los parámetros de un PreparedStatement. Los String se envían sin tipo
     * para que PostgreSQL los infiera igual que a un literal entre comillas (así
     * sirven para columnas de dominio, enteras o de fecha); null se envía como NULL.
     */
    protected static void asignar(PreparedStatement ps, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            Object p = params[i];
            if (p == null) ps.setNull(i + 1, Types.NULL);
            else if (p instanceof String) ps.setObject(i + 1, p, Types.OTHER);
            else ps.setObject(i + 1, p);
        }
    }

    /**
     * Ejecuta una consulta con parámetros y devuelve un ResultSet ya desconectado:
     * la conexión se cierra antes de regresar, así que el llamador no tiene nada
     * que cerrar. Funciona porque el driver de PostgreSQL trae todas las filas a
     * memoria al ejecutar; el ResultSet se puede recorrer (y reposicionar) sin conexión.
     * Lanza SQLException para que cada método decida cómo reportar el error.
     */
    protected ResultSet consultar(String sql, Object... params) throws SQLException {
        try (Connection conexion = conectar()) {
            if (params.length == 0) {
                // Sin parámetros se usa Statement simple (mismo comportamiento que el query() histórico)
                return conexion.createStatement(
                        ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY).executeQuery(sql);
            }
            PreparedStatement ps = conexion.prepareStatement(sql,
                    ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            asignar(ps, params);
            return ps.executeQuery();
        }
    }

    /** Ejecuta INSERT/UPDATE/DELETE con parámetros y devuelve las filas afectadas. */
    protected int ejecutar(String sql, Object... params) throws SQLException {
        try (Connection conexion = conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {
            asignar(ps, params);
            return ps.executeUpdate();
        }
    }

    /**
     * Fija una variable de sesión (p. ej. kardex.referencia) en la conexión dada.
     * Equivale a SET nombre = 'valor', pero con el valor como parámetro.
     */
    protected static void fijarVariableSesion(Connection conexion, String nombre, String valor) throws SQLException {
        try (PreparedStatement ps = conexion.prepareStatement("SELECT set_config(?, ?, false)")) {
            ps.setString(1, nombre);
            ps.setString(2, valor);
            ps.execute();
        }
    }

    // inst() y query() son protected a propósito: el SQL solo se escribe dentro de puntoventa.db.
    // Los paneles y reportes piden datos mediante métodos con nombre de la fachada/DAOs.
    protected boolean inst(String instruccion) {//Función para ejecutar instrucciones
        try (Connection conexion = conectar();
             Statement s = conexion.createStatement()) {
            s.executeUpdate(instruccion);
            return true;
        } catch (Exception e) {
            GestorErrores.registrar(e);
            return false;
        }
    }

    protected ResultSet query(String instruccion) {//Función para obtener un ResultSet
        return consultarONulo(instruccion);
    }

    /** Como consultar(), pero si falla registra el error en el log y devuelve null (no muestra diálogo). */
    protected ResultSet consultarONulo(String sql, Object... params) {
        try {
            return consultar(sql, params);
        } catch (Exception e) {
            GestorErrores.registrar(e);
            return null;
        }
    }

    /** Como ejecutar(), pero si falla registra el error en el log y devuelve false (no muestra diálogo). */
    protected boolean ejecutarOFalso(String sql, Object... params) {
        try {
            ejecutar(sql, params);
            return true;
        } catch (Exception e) {
            GestorErrores.registrar(e);
            return false;
        }
    }

    protected static String nvl(String s) { return s == null ? "" : s; }
}
