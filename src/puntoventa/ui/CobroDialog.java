package puntoventa.ui;

import puntoventa.db.ConexionBD;
import puntoventa.util.Cobro;
import puntoventa.util.GuardaEnter;

import java.awt.*;
import javax.swing.*;

/**
 * Cobro con teclado: Recibido trae el total y Enter cobra. F1-F4 forma de pago,
 * F5 cliente, Esc vuelve al carrito sin cobrar.
 */
public class CobroDialog extends JDialog {

    /** Lo que se cobró; null en {@link #mostrar()} si se canceló. */
    public static final class Resultado {
        public final String formaPago;
        public final double recibido;
        public final double cambio;
        public final String idCliente;
        public final String nombreCliente;
        /** true si se confirmó con Enter (el ticket debe esperar a que se suelte). */
        public final boolean conEnter;

        Resultado(String formaPago, double recibido, double cambio, String idCliente, String nombreCliente, boolean conEnter) {
            this.formaPago = formaPago;
            this.recibido = recibido;
            this.cambio = cambio;
            this.idCliente = idCliente;
            this.nombreCliente = nombreCliente;
            this.conEnter = conEnter;
        }
    }

    public static final String ID_PUBLICO_GENERAL = "XAXX111111HCCXXXX0";
    public static final String NOMBRE_PUBLICO_GENERAL = "Público general";
    private static final String[] FORMAS = {"Efectivo", "Transferencia", "Tarjeta", "Otro"};
    private static final Color ROJO = new Color(190, 30, 30);
    private static final Color VERDE = new Color(20, 120, 60);

    private final double total;
    private final ConexionBD conect;
    private final GuardaEnter guarda = new GuardaEnter(300);

    private final JTextField recibidoField = new JTextField(10);
    private final JLabel cambioLabel = new JLabel();
    private final JLabel clienteLabel = new JLabel();
    private final JLabel errorLabel = new JLabel(" ");
    private final JToggleButton[] botonesForma = new JToggleButton[FORMAS.length];

    private String formaPago = FORMAS[0];
    private String idCliente = ID_PUBLICO_GENERAL;
    private String nombreCliente = NOMBRE_PUBLICO_GENERAL;
    private Resultado resultado;

    /**
     * @param abiertoConEnter true si se abrió con Enter (ese Enter puede seguir sostenido)
     */
    public CobroDialog(Window owner, double total, ConexionBD conect, boolean abiertoConEnter) {
        super(owner, "Cobrar", ModalityType.APPLICATION_MODAL);
        this.total = total;
        this.conect = conect;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        construir();
        registrarTeclas();
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowOpened(java.awt.event.WindowEvent e) {
                guarda.abrir(System.currentTimeMillis(), abiertoConEnter);
                recibidoField.requestFocusInWindow();
                recibidoField.selectAll();
            }
        });
        pack();
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    /** Muestra el diálogo (modal) y regresa lo cobrado, o null si se canceló. */
    public Resultado mostrar() {
        setVisible(true);
        return resultado;
    }

    private void construir() {
        Font normal = new Font("Noto Serif", Font.BOLD, 22);
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(BorderFactory.createEmptyBorder(16, 24, 12, 24));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 6, 6, 6);
        g.anchor = GridBagConstraints.WEST;

        JLabel totalLabel = new JLabel("$" + Cobro.formato(total));
        totalLabel.setFont(new Font("Noto Serif", Font.BOLD, 32));
        recibidoField.setFont(new Font("Noto Serif", Font.BOLD, 28));
        recibidoField.setText(Cobro.formato(total));
        recibidoField.setHorizontalAlignment(JTextField.RIGHT);
        cambioLabel.setFont(new Font("Noto Serif", Font.BOLD, 40));
        cambioLabel.setForeground(VERDE);

        fila(p, g, 0, etiqueta("Total:", normal), totalLabel);
        fila(p, g, 1, etiqueta("Recibido:", normal), recibidoField);
        fila(p, g, 2, etiqueta("CAMBIO:", normal), cambioLabel);

        JPanel formas = new JPanel(new GridLayout(1, FORMAS.length, 6, 0));
        ButtonGroup grupo = new ButtonGroup();
        for (int i = 0; i < FORMAS.length; i++) {
            final int idx = i;
            JToggleButton b = new JToggleButton("F" + (i + 1) + " " + FORMAS[i]);
            b.setFont(new Font("Noto Serif", Font.PLAIN, 16));
            b.setFocusable(false);
            b.addActionListener(e -> elegirForma(idx));
            grupo.add(b);
            formas.add(b);
            botonesForma[i] = b;
        }
        g.gridx = 0; g.gridy = 3; g.gridwidth = 2; g.fill = GridBagConstraints.HORIZONTAL;
        p.add(formas, g);

        clienteLabel.setFont(new Font("Noto Serif", Font.PLAIN, 18));
        g.gridy = 4;
        p.add(clienteLabel, g);

        errorLabel.setFont(new Font("Noto Serif", Font.BOLD, 18));
        errorLabel.setForeground(ROJO);
        g.gridy = 5;
        p.add(errorLabel, g);

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        JButton volver = new JButton("Esc  Volver");
        JButton cobrar = new JButton("Enter  Cobrar");
        for (JButton b : new JButton[]{volver, cobrar}) {
            b.setFont(new Font("Noto Serif", Font.BOLD, 18));
            b.setFocusable(false);
            botones.add(b);
        }
        cobrar.setBackground(new Color(125, 255, 177));
        volver.addActionListener(e -> cancelar());
        cobrar.addActionListener(e -> cobrar(false));
        g.gridy = 6;
        p.add(botones, g);

        setContentPane(p);
        recibidoField.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyTyped(java.awt.event.KeyEvent e) {
                char c = e.getKeyChar();
                if (!Character.isDigit(c) && c != '.') e.consume();
            }
        });
        recibidoField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { actualizarCambio(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { actualizarCambio(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { actualizarCambio(); }
        });
        elegirForma(0);
        actualizarCliente();
    }

    private static JLabel etiqueta(String texto, Font f) {
        JLabel l = new JLabel(texto);
        l.setFont(f);
        return l;
    }

    private static void fila(JPanel p, GridBagConstraints g, int y, JComponent a, JComponent b) {
        g.gridwidth = 1; g.fill = GridBagConstraints.NONE;
        g.gridx = 0; g.gridy = y;
        p.add(a, g);
        g.gridx = 1;
        p.add(b, g);
    }

    private void registrarTeclas() {
        JRootPane raiz = getRootPane();
        for (int i = 0; i < FORMAS.length; i++) {
            final int idx = i;
            tecla(raiz, JComponent.WHEN_IN_FOCUSED_WINDOW, "F" + (i + 1), "forma" + i, () -> elegirForma(idx));
        }
        tecla(raiz, JComponent.WHEN_IN_FOCUSED_WINDOW, "F5", "cliente", this::cambiarCliente);
        tecla(raiz, JComponent.WHEN_IN_FOCUSED_WINDOW, "ENTER", "cobrar", () -> cobrar(true));
        tecla(raiz, JComponent.WHEN_IN_FOCUSED_WINDOW, "released ENTER", "soltarEnter", guarda::soltado);
        tecla(raiz, JComponent.WHEN_IN_FOCUSED_WINDOW, "ESCAPE", "cancelar", this::cancelar);
        // el campo de texto tiene su propio Enter: se reemplaza para que cobre
        tecla(recibidoField, JComponent.WHEN_FOCUSED, "ENTER", "cobrar", () -> cobrar(true));
    }

    static void tecla(JComponent c, int condicion, String teclaTexto, String nombre, Runnable accion) {
        c.getInputMap(condicion).put(KeyStroke.getKeyStroke(teclaTexto), nombre);
        c.getActionMap().put(nombre, new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) { accion.run(); }
        });
    }

    private void elegirForma(int idx) {
        formaPago = FORMAS[idx];
        for (int i = 0; i < botonesForma.length; i++) {
            botonesForma[i].setSelected(i == idx);
            botonesForma[i].setFont(botonesForma[i].getFont().deriveFont(i == idx ? Font.BOLD : Font.PLAIN));
        }
        boolean efectivo = idx == 0;
        if (!efectivo) recibidoField.setText(Cobro.formato(total));
        recibidoField.setEnabled(efectivo);
        errorLabel.setText(" ");
        actualizarCambio();
    }

    private void actualizarCambio() {
        if (!recibidoField.isEnabled()) {
            cambioLabel.setText("$0.00");
            return;
        }
        Double monto = Cobro.parsearMonto(recibidoField.getText());
        cambioLabel.setText(monto == null ? "—" : "$" + Cobro.formato(Cobro.cambio(total, monto)));
    }

    private void actualizarCliente() {
        clienteLabel.setText("Cliente: " + nombreCliente + "   (F5 cambiar)");
    }

    private void cambiarCliente() {
        String[] elegido = SeleccionClienteDialog.elegir(this, conect);
        if (elegido != null) {
            idCliente = elegido[0];
            nombreCliente = elegido[1];
            actualizarCliente();
        }
        recibidoField.requestFocusInWindow();
    }

    private void cobrar(boolean conEnter) {
        if (conEnter && !guarda.permite(System.currentTimeMillis())) return;
        double recibido = total;
        if ("Efectivo".equals(formaPago)) {
            Double monto = Cobro.parsearMonto(recibidoField.getText());
            if (monto == null) {
                error("Escriba un monto válido, por ejemplo 500 o 74.50");
                return;
            }
            if (!Cobro.alcanza(total, monto)) {
                error("Lo recibido no debe ser menor que el total");
                return;
            }
            recibido = monto;
        }
        resultado = new Resultado(formaPago, recibido, Cobro.cambio(total, recibido), idCliente, nombreCliente, conEnter);
        dispose();
    }

    private void error(String mensaje) {
        errorLabel.setText(mensaje);
        Toolkit.getDefaultToolkit().beep();
        recibidoField.requestFocusInWindow();
        recibidoField.selectAll();
    }

    private void cancelar() {
        resultado = null;
        dispose();
    }
}
