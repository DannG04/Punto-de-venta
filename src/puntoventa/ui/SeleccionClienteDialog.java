package puntoventa.ui;

import puntoventa.db.ConexionBD;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

/** Elegir cliente con teclado: escribir CURP o nombre, ↓ a la tabla, Enter elige, Esc cancela. */
public class SeleccionClienteDialog extends JDialog {

    private final ConexionBD conect;
    private final JTextField filtro = new JTextField(24);
    private final DefaultTableModel modelo = new DefaultTableModel(new Object[]{"CURP", "Nombre"}, 0) {
        @Override
        public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable tabla = new JTable(modelo);
    private String[] elegido;

    /** Regresa {id, nombre} del cliente elegido, o null si se canceló. */
    public static String[] elegir(Window owner, ConexionBD conect) {
        SeleccionClienteDialog d = new SeleccionClienteDialog(owner, conect);
        d.setVisible(true);
        return d.elegido;
    }

    private SeleccionClienteDialog(Window owner, ConexionBD conect) {
        super(owner, "Elegir cliente", ModalityType.APPLICATION_MODAL);
        this.conect = conect;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel p = new JPanel(new BorderLayout(8, 8));
        p.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        JPanel arriba = new JPanel(new BorderLayout(8, 0));
        JLabel l = new JLabel("CURP o nombre:");
        l.setFont(new Font("Noto Serif", Font.BOLD, 18));
        filtro.setFont(new Font("Noto Serif", Font.PLAIN, 18));
        arriba.add(l, BorderLayout.WEST);
        arriba.add(filtro, BorderLayout.CENTER);
        p.add(arriba, BorderLayout.NORTH);

        tabla.setFont(new Font("Noto Serif", Font.PLAIN, 16));
        tabla.setRowHeight(26);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) elegirSeleccionado();
            }
        });
        p.add(new JScrollPane(tabla), BorderLayout.CENTER);

        JPanel abajo = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        JLabel ayuda = new JLabel("↓ tabla · Enter elegir · Esc volver");
        ayuda.setFont(new Font("Noto Serif", Font.PLAIN, 14));
        JButton registrar = new JButton("Registrar nuevo cliente");
        registrar.setFont(new Font("Noto Serif", Font.BOLD, 16));
        registrar.setFocusable(false);
        registrar.addActionListener(e -> ClientesP.Registro.setVisible(true));
        abajo.add(ayuda);
        abajo.add(registrar);
        p.add(abajo, BorderLayout.SOUTH);
        setContentPane(p);

        filtro.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { cargar(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { cargar(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { cargar(); }
        });
        // al volver de registrar un cliente nuevo, recargar la lista
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowActivated(java.awt.event.WindowEvent e) { cargar(); }
        });

        CobroDialog.tecla(filtro, JComponent.WHEN_FOCUSED, "ENTER", "aTabla", this::entrarATabla);
        CobroDialog.tecla(filtro, JComponent.WHEN_FOCUSED, "DOWN", "aTablaAbajo", this::entrarATabla);
        // JTable trae su Enter ("siguiente fila") en este InputMap: se reemplaza
        CobroDialog.tecla(tabla, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, "ENTER", "elegir", this::elegirSeleccionado);
        CobroDialog.tecla(getRootPane(), JComponent.WHEN_IN_FOCUSED_WINDOW, "ESCAPE", "cancelar", this::dispose);

        cargar();
        setSize(560, 420);
        setLocationRelativeTo(owner);
    }

    private void cargar() {
        modelo.setRowCount(0);
        java.sql.ResultSet rs = conect.buscarClientesActivos(filtro.getText().trim());
        if (rs == null) return;
        try {
            while (rs.next()) modelo.addRow(new Object[]{rs.getString("id_cliente"), rs.getString("nombre")});
        } catch (java.sql.SQLException e) {
            puntoventa.util.GestorErrores.registrar(e);
        }
    }

    private void entrarATabla() {
        if (modelo.getRowCount() == 0) {
            Toolkit.getDefaultToolkit().beep();
            return;
        }
        tabla.setRowSelectionInterval(0, 0);
        tabla.requestFocusInWindow();
    }

    private void elegirSeleccionado() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) return;
        elegido = new String[]{"" + modelo.getValueAt(fila, 0), "" + modelo.getValueAt(fila, 1)};
        dispose();
    }
}
