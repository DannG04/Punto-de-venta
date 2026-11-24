package puntoventa.ui;

import static pruebas.Prueba.*;

import java.awt.Window;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * CU-UI-01: cada pantalla se construye y carga sus tablas contra la base de pruebas
 * sin errores. Necesita pantalla (no headless): un vigilante cierra cualquier
 * diálogo que aparezca y lo reporta como fallo.
 */
public class PantallasPruebaBD {
    static final List<String> errores = new ArrayList<>();
    static final Logger LOG = Logger.getLogger("PuntoDeVenta");

    public static void main(String[] a) throws Exception {
        System.setProperty("java.awt.headless", "false");
        LOG.addHandler(new Handler() {
            @Override public void publish(LogRecord r) { synchronized (errores) { errores.add("log: " + r.getMessage()); } }
            @Override public void flush() {}
            @Override public void close() {}
        });
        Thread vigilante = new Thread(() -> {
            while (true) {
                for (Window w : Window.getWindows()) {
                    if (w instanceof JDialog d && d.isShowing() && d.isModal()) {
                        String txt = textoDialogo(d);
                        synchronized (errores) { errores.add("diálogo '" + d.getTitle() + "': " + txt); }
                        SwingUtilities.invokeLater(d::dispose);
                    }
                }
                try { Thread.sleep(200); } catch (InterruptedException e) { return; }
            }
        });
        vigilante.setDaemon(true);
        vigilante.start();

        JFrame marco = new JFrame("Prueba de pantallas");
        Object[][] pantallas = {
            {"Ventas", (Callable<JPanel>) VentasP::new}, {"Inventario", (Callable<JPanel>) InventarioP::new},
            {"Apartados", (Callable<JPanel>) ApartadosP::new}, {"Devoluciones", (Callable<JPanel>) DevolucionesP::new},
            {"Clientes", (Callable<JPanel>) ClientesP::new}, {"Gastos", (Callable<JPanel>) GastosP::new},
            {"Ganancias", (Callable<JPanel>) GananciasP::new}, {"Cotizaciones", (Callable<JPanel>) CotizacionesP::new},
            {"Compras", (Callable<JPanel>) ComprasP::new}, {"Empleados", (Callable<JPanel>) EmpleadosP::new},
            {"Proveedores", (Callable<JPanel>) ProveedoresP::new}, {"Categorías", (Callable<JPanel>) CategoriasP::new},
            {"Kardex", (Callable<JPanel>) KardexP::new}, {"Empresa", (Callable<JPanel>) EmpresaP::new},
            {"Administración", (Callable<JPanel>) AdministracionP::new},
            {"Actualizaciones", (Callable<JPanel>) () -> new ActualizacionesP(r -> {})},
        };
        for (Object[] p : pantallas) {
            synchronized (errores) { errores.clear(); }
            String nombre = (String) p[0];
            try {
                @SuppressWarnings("unchecked") Callable<JPanel> crear = (Callable<JPanel>) p[1];
                JPanel panel = crear.call();
                SwingUtilities.invokeAndWait(() -> { marco.setContentPane(panel); marco.setSize(1366, 768); marco.validate(); });
                Thread.sleep(300);
            } catch (Throwable t) {
                synchronized (errores) { errores.add("excepción: " + t); }
            }
            synchronized (errores) { check(errores.isEmpty(), "CU-UI-01 pantalla " + nombre + " sin errores: " + errores); }
        }
        marco.dispose();
        terminar("PantallasPruebaBD");
    }

    static String textoDialogo(JDialog d) {
        JOptionPane op = buscar(d.getContentPane());
        return op == null ? "(sin texto)" : String.valueOf(op.getMessage());
    }

    static JOptionPane buscar(java.awt.Container c) {
        for (java.awt.Component x : c.getComponents()) {
            if (x instanceof JOptionPane o) return o;
            if (x instanceof java.awt.Container cc) { JOptionPane r = buscar(cc); if (r != null) return r; }
        }
        return null;
    }
}
