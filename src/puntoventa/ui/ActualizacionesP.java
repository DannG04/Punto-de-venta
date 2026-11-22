package puntoventa.ui;

import java.awt.*;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;
import javax.swing.*;
import puntoventa.util.Actualizador;
import puntoventa.util.Actualizador.Release;
import puntoventa.util.GestorErrores;
import puntoventa.util.Mise;
import puntoventa.util.SvgIcon;
import puntoventa.util.VersionApp;

/**
 * Administración → Actualizaciones: muestra la versión instalada y la publicada,
 * y aplica el parche (descarga verificada + aplicar_actualizacion.bat --auto).
 */
public class ActualizacionesP extends JPanel {

    private static final DateTimeFormatter DD_MM = DateTimeFormatter.ofPattern("dd/MM");

    final JLabel lblInstalada = new JLabel();
    final JLabel lblDisponible = new JLabel(" ");
    final JLabel lblEstado = new JLabel(" ");
    final JTextArea txtNotas = new JTextArea(8, 40);
    final JButton btnBuscar = new JButton("Buscar ahora");
    final JButton btnActualizar = new JButton("Actualizar ahora");

    /** Se avisa a Interfaz cada vez que una consulta (manual) encuentra un release. */
    private final Consumer<Release> alConsultar;
    private Release actual;

    public ActualizacionesP(Consumer<Release> alConsultar) {
        this.alConsultar = alConsultar;
        initComponents();
        mostrar(null);
        lblEstado.setText("Pulse \"Buscar ahora\" para revisar si hay una versión nueva.");
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        JLabel titulo = new JLabel("Actualizaciones");
        titulo.setFont(new Font("Noto Serif", Font.BOLD, 36));
        titulo.setForeground(new Color(78, 150, 150));
        titulo.setHorizontalAlignment(SwingConstants.CENTER);
        titulo.setBorder(BorderFactory.createEmptyBorder(16, 20, 8, 0));
        titulo.setIcon(SvgIcon.load("/icons/servidor_movil.svg", SvgIcon.LARGE));
        add(titulo, BorderLayout.NORTH);

        JPanel centro = new JPanel(new GridBagLayout());
        centro.setBorder(BorderFactory.createEmptyBorder(8, 40, 8, 40));
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;
        gc.insets = new Insets(6, 6, 6, 6);
        Font f = new Font("Noto Serif", Font.BOLD, 18);
        for (JLabel l : new JLabel[]{lblInstalada, lblDisponible, lblEstado}) {
            l.setFont(f);
            centro.add(l, gc);
        }
        JLabel nov = new JLabel("Novedades:");
        nov.setFont(f);
        centro.add(nov, gc);
        txtNotas.setEditable(false);
        txtNotas.setLineWrap(true);
        txtNotas.setWrapStyleWord(true);
        txtNotas.setFont(new Font("Noto Serif", Font.PLAIN, 15));
        gc.fill = GridBagConstraints.BOTH;
        gc.weighty = 1;
        centro.add(new JScrollPane(txtNotas), gc);
        add(centro, BorderLayout.CENTER);

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 12));
        estiloBoton(btnBuscar, new Color(165, 205, 250), "/icons/lupa.svg");
        estiloBoton(btnActualizar, new Color(125, 255, 177), "/icons/done.svg");
        btnBuscar.addActionListener(e -> buscar());
        btnActualizar.addActionListener(e -> actualizar());
        botones.add(btnBuscar);
        botones.add(btnActualizar);
        add(botones, BorderLayout.SOUTH);
    }

    private static void estiloBoton(JButton b, Color fondo, String icono) {
        b.setFont(new Font("Noto Serif", Font.BOLD, 18));
        b.setBackground(fondo);
        b.setIcon(SvgIcon.load(icono, SvgIcon.MEDIUM));
    }

    /** Muestra el resultado de una consulta. null = no se pudo consultar. */
    public void mostrar(Release r) {
        actual = r;
        lblInstalada.setText("Versión instalada: " + VersionApp.actual());
        if (r == null) {
            lblDisponible.setText(" ");
            txtNotas.setText("");
            lblEstado.setText("No se pudo consultar. Revisa la conexión a internet.");
            btnActualizar.setEnabled(false);
        } else if (!VersionApp.esMasNueva(r.version(), VersionApp.actual())) {
            lblDisponible.setText(" ");
            txtNotas.setText("");
            lblEstado.setText("Tienes la versión más reciente");
            btnActualizar.setEnabled(false);
        } else {
            String fecha = r.publicada() == null ? "" : " (publicada " + r.publicada().format(DD_MM) + ")";
            lblDisponible.setText("Versión disponible: " + r.version() + fecha);
            txtNotas.setText(r.notas());
            txtNotas.setCaretPosition(0);
            lblEstado.setText("Hay una versión nueva lista para instalar.");
            btnActualizar.setEnabled(true);
        }
    }

    private void buscar() {
        btnBuscar.setEnabled(false);
        lblEstado.setText("Buscando...");
        new SwingWorker<Release, Void>() {
            @Override protected Release doInBackground() { return Actualizador.buscar(); }
            @Override protected void done() {
                Release r = null;
                try { r = get(); } catch (Exception e) { GestorErrores.registrar(e); }
                mostrar(r);
                if (r != null) alConsultar.accept(r);
                btnBuscar.setEnabled(true);
            }
        }.execute();
    }

    private void actualizar() {
        Release r = actual;
        if (r == null) return;
        if (Actualizador.hayVentaEnCurso()) {
            Mise.JOption("Hay una venta en curso; termínela o vacíe el carrito.", "Actualizaciones", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (Mise.JOptionYesNo("La app se cerrará y se actualizará. Tardará unos minutos.\n¿Desea continuar?", "Actualizar") != 0) return;

        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this), "Descargando actualización", Dialog.ModalityType.APPLICATION_MODAL);
        JProgressBar barra = new JProgressBar(0, 100);
        barra.setStringPainted(true);
        JButton cancelar = new JButton("Cancelar");
        JPanel cont = new JPanel(new BorderLayout(10, 10));
        cont.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        cont.add(new JLabel("Descargando versión " + r.version() + "..."), BorderLayout.NORTH);
        cont.add(barra, BorderLayout.CENTER);
        cont.add(cancelar, BorderLayout.SOUTH);
        dlg.setContentPane(cont);
        dlg.setSize(420, 160);
        dlg.setLocationRelativeTo(this);
        dlg.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        SwingWorker<Path, Integer> w = new SwingWorker<>() {
            @Override protected Path doInBackground() throws Exception {
                return Actualizador.descargar(r, p -> publish((int) Math.round(p * 100)));
            }
            @Override protected void process(java.util.List<Integer> v) { barra.setValue(v.get(v.size() - 1)); }
            @Override protected void done() {
                dlg.dispose();
                if (isCancelled()) return;
                try {
                    Actualizador.lanzarYSalir(get(), Path.of(System.getProperty("user.dir")));
                } catch (Exception e) {
                    Throwable c = e.getCause() != null ? e.getCause() : e;
                    GestorErrores.registrar(c);
                    Mise.JOption(c.getMessage() == null ? "No se pudo actualizar." : c.getMessage(),
                            "Actualizaciones", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        cancelar.addActionListener(e -> w.cancel(true));
        w.execute();
        dlg.setVisible(true);
    }
}
