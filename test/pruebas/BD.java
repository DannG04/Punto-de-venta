package pruebas;

import java.awt.HeadlessException;
import java.io.FileInputStream;
import java.sql.*;
import java.util.Properties;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * Utilidades para las pruebas que usan la base de datos (clases *PruebaBD).
 * Lee la misma db.properties que la app (directorio de trabajo), así que apunta
 * a la base de pruebas que deja test/preparar_bd_pruebas.sh, nunca a la real.
 */
public final class BD {
    private static String url, usuario, contra;
    private static String ultimoLog;
    // referencia fuerte: los Logger se recolectan si nadie los sujeta
    private static final Logger LOG = Logger.getLogger("PuntoDeVenta");
    private static int contador = (int) (System.currentTimeMillis() / 1000 % 100000);

    static {
        Properties p = new Properties();
        try (FileInputStream in = new FileInputStream("db.properties")) { p.load(in); }
        catch (Exception e) { throw new IllegalStateException("Falta db.properties en el directorio de trabajo", e); }
        url = "jdbc:postgresql://" + p.getProperty("db.host") + ":" + p.getProperty("db.port") + "/" + p.getProperty("db.name");
        usuario = p.getProperty("db.user");
        contra = p.getProperty("db.password");
        LOG.addHandler(new Handler() {
            @Override public void publish(LogRecord r) { ultimoLog = r.getMessage(); }
            @Override public void flush() {}
            @Override public void close() {}
        });
    }

    private BD() {}

    public static Connection con() throws SQLException { return DriverManager.getConnection(url, usuario, contra); }

    /** Primer valor de la primera fila (null si no hay filas). */
    public static Object val(String sql, Object... params) {
        try (Connection c = con(); PreparedStatement ps = c.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) ps.setObject(i + 1, params[i]);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? rs.getObject(1) : null; }
        } catch (SQLException e) { throw new RuntimeException(sql + " -> " + e.getMessage(), e); }
    }

    public static double num(String sql, Object... p) { Object o = val(sql, p); return o == null ? Double.NaN : ((Number) o).doubleValue(); }
    public static int entero(String sql, Object... p) { Object o = val(sql, p); return o == null ? -1 : ((Number) o).intValue(); }
    public static String texto(String sql, Object... p) { Object o = val(sql, p); return o == null ? null : o.toString().trim(); }

    public static void exec(String sql, Object... params) {
        try (Connection c = con(); PreparedStatement ps = c.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) ps.setObject(i + 1, params[i]);
            ps.execute();
        } catch (SQLException e) { throw new RuntimeException(sql + " -> " + e.getMessage(), e); }
    }

    /**
     * Ejecuta una acción de la app y devuelve el error que el usuario habría visto
     * (o que quedó en el log), o null si no hubo error. En modo headless el diálogo
     * de error lanza HeadlessException; eso cuenta como "se mostró un error".
     */
    public static String error(Runnable accion) {
        ultimoLog = null;
        boolean dialogo = false;
        try { accion.run(); }
        catch (HeadlessException e) { dialogo = true; }
        if (ultimoLog != null) return ultimoLog;
        return dialogo ? "(diálogo sin detalle en el log)" : null;
    }

    public static synchronized int siguiente() { return ++contador; }

    /** Crea un producto de prueba con id único y devuelve su id. */
    public static String producto(String prefijo, int stock, double menudeo, double mayoreo) {
        String id = "T" + prefijo + siguiente();
        exec("INSERT INTO producto(id_producto, nombre, cantidad, precio_menudeo, precio_mayoreo, precio_compra) VALUES(?,?,?,?,?,?)",
                id, "Prueba " + id, stock, menudeo, mayoreo, mayoreo / 2);
        return id;
    }

    public static int stock(String idProducto) { return entero("SELECT cantidad FROM producto WHERE id_producto=?", idProducto); }

    /** CURP sintácticamente válida y única para clientes/empleados de prueba. */
    public static String curp() {
        int n = siguiente();
        return String.format("PEPJ%02d%02d%02dHDFRRN%c%d", 40 + n % 50, 1 + n % 12, 1 + n % 28,
                (char) ('A' + (n / 10) % 26), n % 10);
    }

    /** Teléfono de 10 dígitos único. */
    public static String telefono() { return String.format("55%08d", siguiente()); }

    /**
     * Invariante del kardex: partiendo del stock con el que se creó el producto,
     * la suma de los movimientos debe dar el stock actual y cada renglón debe
     * encadenar con el anterior (existencia_anterior = existencia_posterior previa).
     * Devuelve null si cuadra, o la descripción del descuadre.
     */
    public static String descuadreKardex(String idProducto, int stockInicial) {
        try (Connection c = con(); PreparedStatement ps = c.prepareStatement(
                "SELECT cantidad, existencia_anterior, existencia_posterior, tipo_movimiento FROM kardex WHERE id_producto=? ORDER BY id_kardex")) {
            ps.setString(1, idProducto);
            int esperado = stockInicial;
            StringBuilder sb = new StringBuilder();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    // V1.24: el alta de un producto con existencia deja "Inventario inicial" (0 -> N)
                    if ("Inventario inicial".equals(rs.getString(4))) esperado = rs.getInt(2);
                    if (rs.getInt(2) != esperado)
                        sb.append(" salto ").append(esperado).append("->").append(rs.getInt(2)).append(" antes de ").append(rs.getString(4)).append(';');
                    esperado = rs.getInt(3);
                }
            }
            int real = stock(idProducto);
            if (esperado != real) sb.append(" kardex termina en ").append(esperado).append(" pero el stock es ").append(real);
            return sb.length() == 0 ? null : sb.toString().trim();
        } catch (SQLException e) { return e.getMessage(); }
    }

    public static final String ADMIN = "GARC850315HDFRZNL5";
    public static final String CLIENTE_GENERICO = "XAXX111111HCCXXXX0";
}
