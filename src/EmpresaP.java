import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
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
    private JLabel lblImagen;

    public EmpresaP() {
        initComponents();
        cargarDatos();
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        JLabel lblTitulo = new JLabel("Datos de la Empresa");
        lblTitulo.setFont(new Font("Noto Serif", Font.BOLD, 36));
        lblTitulo.setBorder(BorderFactory.createEmptyBorder(16, 20, 8, 0));
        lblTitulo.setForeground(new Color(78, 150, 150));
        lblTitulo.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblTitulo.setIcon(SvgIcon.load("/icons/empresa.svg", SvgIcon.LARGE));
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

        btnExaminar = new JButton("Examinar");
        btnExaminar.setFont(new Font("Noto Serif", Font.BOLD, 15));
        btnExaminar.setBackground(new Color(255, 251, 128));
        btnExaminar.setIcon(SvgIcon.load("/icons/examinar.svg", SvgIcon.MEDIUM));
        btnExaminar.setPreferredSize(new java.awt.Dimension(160, 35));

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
            JLabel label = new JLabel(fields[i][0]);
            label.setFont(new Font("Noto Serif", Font.BOLD, 16));
            inputs[i].setFont(new Font("Noto Serif", Font.PLAIN, 15));

            gc.gridx = 0; gc.gridy = i; gc.weightx = 0;
            formPanel.add(label, gc);
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

        lblImagen = new JLabel("");
        lblImagen.setPreferredSize(new Dimension(120, 120));
        gc.gridx = 0; gc.gridy = inputs.length; gc.weightx = 0; gc.gridwidth = 1;
        gc.anchor = GridBagConstraints.NORTH;
        formPanel.add(lblImagen, gc);

        actualizarPreviewLogo();
        add(new JScrollPane(formPanel), BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        btnGuardar = new JButton("Guardar");
        btnGuardar.setFont(new Font("Noto Serif", Font.BOLD, 15));
        btnGuardar.setBackground(new Color(125, 255, 177));
        btnGuardar.setIcon(SvgIcon.load("/icons/guardar.svg", SvgIcon.MEDIUM));
        btnGuardar.setPreferredSize(new java.awt.Dimension(160, 35));

        btnCancelar = new JButton("Cancelar");
        btnCancelar.setFont(new Font("Noto Serif", Font.BOLD, 15));
        btnCancelar.setBackground(new Color(252, 149, 149));
        btnCancelar.setIcon(SvgIcon.load("/icons/cancelar.svg", SvgIcon.MEDIUM));
        btnCancelar.setPreferredSize(new java.awt.Dimension(160, 35));

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

                actualizarPreviewLogo();
            }
        } catch (Exception e) {
            GestorErrores.registrar(e);
        }
    }

    // Método separado para actualizar la preview del logo
    private void actualizarPreviewLogo() {
        String ruta = txtLogoRuta.getText().trim();
        File f = new File(ruta);
        if (!ruta.isEmpty() && f.exists()) {
            ImageIcon icon = new ImageIcon(
                new ImageIcon(f.getAbsolutePath())
                    .getImage()
                    .getScaledInstance(120, 120, Image.SCALE_SMOOTH)
            );
            lblImagen.setIcon(icon);
            lblImagen.setToolTipText(f.getName());
        } else {
            lblImagen.setIcon(SvgIcon.load("/icons/imagen.svg", 120));
            lblImagen.setToolTipText(null);
        }
    }

    private void examinarLogo() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("Imágenes", "ico", "png"));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            txtLogoRuta.setText(chooser.getSelectedFile().getAbsolutePath());
            actualizarPreviewLogo(); // actualiza la preview al seleccionar archivo
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
                actualizarPreviewLogo(); // refresca preview tras convertir a PNG
            } catch (IOException ex) {
                GestorErrores.registrar(ex);
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