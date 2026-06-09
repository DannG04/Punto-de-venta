import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Capa centralizada de manejo de errores.
 * Registra el detalle técnico completo en logs/app.log (y consola) y
 * traduce las excepciones a mensajes amigables para el usuario.
 */
public class GestorErrores {

    private static final Logger LOGGER = Logger.getLogger("PuntoDeVenta");
    private static boolean configurado = false;

    static {
        configurar();
    }

    private static synchronized void configurar() {
        if (configurado) return;
        try {
            Path dir = Paths.get("logs");
            if (!Files.exists(dir)) Files.createDirectories(dir);
            FileHandler fileHandler = new FileHandler("logs/app.log", true); // append
            fileHandler.setFormatter(new SimpleFormatter());
            fileHandler.setLevel(Level.ALL);
            LOGGER.addHandler(fileHandler);
            LOGGER.setLevel(Level.ALL);
            LOGGER.setUseParentHandlers(true); // mantiene también la salida a consola
        } catch (IOException e) {
            System.out.println("No se pudo inicializar el log de errores: " + e.getMessage());
        }
        configurado = true;
    }

    /** Registra el detalle técnico completo (archivo + consola). Deriva el contexto del llamador. */
    public static void registrar(Throwable e) {
        registrar(e, contextoAutomatico());
    }

    /** Registra el detalle técnico completo (archivo + consola) con un contexto explícito. */
    public static void registrar(Throwable e, String contexto) {
        LOGGER.log(Level.SEVERE, "[" + contexto + "] " + detalleTecnico(e), e);
    }

    /** Registra y muestra un diálogo amigable al usuario. Deriva el contexto del llamador. */
    public static void manejar(Throwable e) {
        manejar(e, contextoAutomatico());
    }

    /** Registra y muestra un diálogo amigable al usuario con un contexto explícito. */
    public static void manejar(Throwable e, String contexto) {
        registrar(e, contexto);
        Mise.JOption(mensajeAmigable(e), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
    }

    /** Traduce la excepción a un mensaje claro para el usuario (función pura). */
    public static String mensajeAmigable(Throwable e) {
        SQLException sql = buscarSQLException(e);
        if (sql != null) {
            String estado = sql.getSQLState();
            if (estado != null) {
                switch (estado) {
                    case "23505": return "Ya existe un registro con esos datos.";
                    case "23503": return "No se puede completar porque el registro está en uso por otros datos.";
                    case "23502": return "Faltan datos obligatorios.";
                    case "23514": return "Alguno de los datos ingresados no es válido.";
                    case "22001": return "Uno de los valores es demasiado largo.";
                    case "22P02":
                    case "22003": return "Alguno de los datos tiene un formato incorrecto.";
                    case "P0001": return sql.getMessage(); // regla de negocio escrita en el stored procedure
                }
                if (estado.startsWith("08")) {
                    return "No hay conexión con la base de datos. Verifique e intente de nuevo.";
                }
            }
        }
        return "Ocurrió un error al procesar la operación. Intente de nuevo.";
    }

    /** Recorre la cadena de causas buscando una SQLException (el driver puede envolverla). */
    private static SQLException buscarSQLException(Throwable e) {
        Throwable actual = e;
        while (actual != null) {
            if (actual instanceof SQLException) return (SQLException) actual;
            actual = actual.getCause();
        }
        return null;
    }

    private static String detalleTecnico(Throwable e) {
        SQLException sql = buscarSQLException(e);
        if (sql != null) {
            return "SQLState=" + sql.getSQLState() + " | " + sql.getMessage();
        }
        return e == null ? "(sin detalle)" : e.getMessage();
    }

    /** Deriva "Clase.metodo" del primer frame fuera de GestorErrores/Thread. */
    private static String contextoAutomatico() {
        for (StackTraceElement el : Thread.currentThread().getStackTrace()) {
            String cn = el.getClassName();
            if (!cn.equals("java.lang.Thread") && !cn.equals("GestorErrores")) {
                return el.getClassName() + "." + el.getMethodName();
            }
        }
        return "desconocido";
    }
}
