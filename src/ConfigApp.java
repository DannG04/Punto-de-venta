import java.awt.Image;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;
import javax.swing.ImageIcon;

/**
 * Lee config.properties generado por el instalador y expone los datos
 * de personalización de la empresa (nombre e ícono).
 *
 * El archivo se busca en el directorio de trabajo (junto al .exe),
 * que es donde el instalador lo coloca.
 */
public class ConfigApp {

    private static final String NOMBRE_DEFAULT = "Punto de Ventas";

    private static String nombreEmpresa = NOMBRE_DEFAULT;
    private static Image icono = null;
    private static boolean cargado = false;

    /** Carga config.properties una sola vez. Seguro llamar varias veces. */
    public static void cargar() {
        if (cargado) return;
        cargado = true;

        File archivo = new File("config.properties");
        if (!archivo.exists()) return;

        try (InputStream in = new FileInputStream(archivo)) {
            Properties props = new Properties();
            props.load(in);

            String nombre = props.getProperty("app.nombre", "").trim();
            if (!nombre.isEmpty()) nombreEmpresa = nombre;

            String logoRuta = props.getProperty("app.logo", "").trim();
            if (!logoRuta.isEmpty()) {
                File logoFile = new File(logoRuta);
                if (logoFile.exists()) {
                    icono = new ImageIcon(logoFile.getAbsolutePath()).getImage();
                }
            }

            ConexionBD con = new ConexionBD();
            if (!con.empresaInicializada()) {
                String razonSocial  = props.getProperty("app.razon_social", "").trim();
                String rfc          = props.getProperty("app.rfc", "").trim();
                String telefono     = props.getProperty("app.telefono", "").trim();
                String correo       = props.getProperty("app.correo", "").trim();
                String direccion    = props.getProperty("app.direccion", "").trim();
                String ciudad       = props.getProperty("app.ciudad", "").trim();
                String estado       = props.getProperty("app.estado", "").trim();
                String cp           = props.getProperty("app.cp", "").trim();
                String mensajeTicket = props.getProperty("app.mensaje_ticket", "").trim();
                con.actualizarEmpresa(nombreEmpresa, razonSocial, rfc, telefono, correo,
                        direccion, ciudad, estado, cp, mensajeTicket, logoRuta);
            }
        } catch (Exception e) {
            // Si falla la lectura se usan los valores por defecto
        }
    }

    public static String getNombreEmpresa() {
        return nombreEmpresa;
    }

    /** Devuelve la imagen del ícono, o null si no se configuró ninguno. */
    public static Image getIcono() {
        return icono;
    }
}
