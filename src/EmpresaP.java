import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.ResultSet;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

public class EmpresaP extends javax.swing.JPanel {

    private ConexionBD con = new ConexionBD();

    private JTextField txtNombre;
    private JTextField txtRazonSocial;
    private JTextField txtRfc;
    private JTextField txtTelefono;
    private JTextField txtCorreo;
    private JTextField txtDireccion;
    private JTextField txtCiudad;
    private JTextField txtEstado;
    private JTextField txtCp;
    private JTextField txtMensaje;
    private JTextField txtLogoRuta;
    private JButton btnExaminar;
    private JButton btnGuardar;
    private JButton btnCancelar;

    public EmpresaP() {
        initComponents();
        cargarDatos();
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        JLabel lblTitulo = new JLabel("Datos de la Empresa");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 18));
        lblTitulo.setBorder(BorderFactory.createEmptyBorder(16, 20, 8, 0));
        add(lblTitulo, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(5, 5, 5, 5);
        gc.fill = GridBagConstraints.HORIZONTAL;

        txtNombre      = new JTextField(30);
        txtRazonSocial = new JTextField(30);
        txtRfc         = new JTextField(13);
        txtTelefono    = new JTextField(20);
        txtCorreo      = new JTextField(30);
        txtDireccion   = new JTextField(40);
        txtCiudad      = new JTextField(20);
        txtEstado      = new JTextField(20);
        txtCp          = new JTextField(10);
        txtMensaje     = new JTextField(40);
        txtLogoRuta    = new JTextField(35);
        txtLogoRuta.setEditable(false);
        btnExaminar    = new JButton("Examinar");

        String[][] fields = {
            {"Nombre *",          null},
            {"Razón social",      null},
            {"RFC",               null},
            {"Teléfono",          null},
            {"Correo",            null},
            {"Dirección",         null},
            {"Ciudad",            null},
            {"Estado",            null},
            {"CP",                null},
            {"Mensaje en ticket", null},
            {"Logo (.ico/.png)",  null}
        };
        JTextField[] inputs = {txtNombre, txtRazonSocial, txtRfc, txtTelefono, txtCorreo,
            txtDireccion, txtCiudad, txtEstado, txtCp, txtMensaje, txtLogoRuta};

        for (int i = 0; i < inputs.length; i++) {
            gc.gridx = 0; gc.gridy = i; gc.weightx = 0;
            formPanel.add(new JLabel(fields[i][0]), gc);
            gc.gridx = 1; gc.weightx = 1;
            if (inputs[i] == txtLogoRuta) {
                JPanel logoRow = new JPanel(new BorderLayout(5, 0));
                logoRow.add(txtLogoRuta, BorderLayout.CENTER);
                logoRow.add(btnExaminar, BorderLayout.EAST);
                formPanel.add(logoRow, gc);
            } else {
                formPanel.add(inputs[i], gc);
            }
        }

        add(new JScrollPane(formPanel), BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnGuardar  = new JButton("Guardar");
        btnCancelar = new JButton("Cancelar");
        btnPanel.add(btnCancelar);
        btnPanel.add(btnGuardar);
        add(btnPanel, BorderLayout.SOUTH);

        btnExaminar.addActionListener(e -> examinarLogo());
        btnGuardar.addActionListener(e -> guardar());
        btnCancelar.addActionListener(e -> cargarDatos());
    }

    public void cargarDatos() {
        try {
            ResultSet rs = con.obtenerEmpresa();
            if (rs != null && rs.next()) {
                txtNombre.setText(nvl(rs.getString("nombre")));
                txtRazonSocial.setText(nvl(rs.getString("razon_social")));
                txtRfc.setText(nvl(rs.getString("rfc")));
                txtTelefono.setText(nvl(rs.getString("telefono")));
                txtCorreo.setText(nvl(rs.getString("correo")));
                txtDireccion.setText(nvl(rs.getString("direccion")));
                txtCiudad.setText(nvl(rs.getString("ciudad")));
                txtEstado.setText(nvl(rs.getString("estado")));
                txtCp.setText(nvl(rs.getString("cp")));
                txtMensaje.setText(nvl(rs.getString("mensaje_ticket")));
                txtLogoRuta.setText(nvl(rs.getString("logo_ruta")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void examinarLogo() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("Imágenes", "ico", "png"));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            txtLogoRuta.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }

    private void guardar() {
        String nombre = txtNombre.getText().trim();
        if (nombre.isEmpty()) {
            Mise.JOption("El nombre de la empresa es obligatorio.", "Validación",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        String rfc = txtRfc.getText().trim();
        if (rfc.length() > 13) {
            Mise.JOption("El RFC no puede tener más de 13 caracteres.", "Validación",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        String cp = txtCp.getText().trim();
        if (cp.length() > 10) {
            Mise.JOption("El CP no puede tener más de 10 caracteres.", "Validación",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        String mensaje = txtMensaje.getText().trim();
        if (mensaje.length() > 200) {
            Mise.JOption("El mensaje no puede tener más de 200 caracteres.", "Validación",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String logoRuta = txtLogoRuta.getText().trim();
        if (!logoRuta.isEmpty()) {
            File logoFile = new File(logoRuta);
            if (!logoFile.exists()) {
                Mise.JOption("El archivo de logo no existe.", "Validación",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                // Siempre guardar como PNG para compatibilidad con Excel/ImageIO
                Image img = new ImageIcon(logoFile.getAbsolutePath()).getImage();
                int w = img.getWidth(null);
                int h = img.getHeight(null);
                if (w <= 0) w = 64;
                if (h <= 0) h = 64;
                BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2 = bi.createGraphics();
                g2.drawImage(img, 0, 0, null);
                g2.dispose();
                File destPng = new File("logo.png");
                ImageIO.write(bi, "png", destPng);
                logoRuta = destPng.getAbsolutePath();
                txtLogoRuta.setText(logoRuta);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        boolean ok = con.actualizarEmpresa(nombre,
                txtRazonSocial.getText().trim(),
                rfc,
                txtTelefono.getText().trim(),
                txtCorreo.getText().trim(),
                txtDireccion.getText().trim(),
                txtCiudad.getText().trim(),
                txtEstado.getText().trim(),
                cp,
                mensaje,
                logoRuta);

        if (ok) {
            // Actualizar título e ícono de la ventana
            JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
            if (frame != null) {
                frame.setTitle(nombre);
                if (!logoRuta.isEmpty()) {
                    File lf = new File(logoRuta);
                    if (lf.exists()) {
                        frame.setIconImage(new ImageIcon(lf.getAbsolutePath()).getImage());
                    }
                }
            }
            Mise.JOption("Datos de la empresa guardados correctamente.", "Guardado",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private String nvl(String s) {
        return s == null ? "" : s;
    }
}
