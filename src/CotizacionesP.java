import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.text.ParseException;

public class CotizacionesP extends javax.swing.JPanel {

    String idClienteCoti = "XAXX111111HCCXXXX0";
    String nombreClienteCoti = "Público general";
    double totalCotiActual = 0.0;

    DefaultTableModel modeloCarrito = new DefaultTableModel();
    DefaultTableModel modeloCotizaciones = new DefaultTableModel();
    DefaultTableModel modeloDetalle = new DefaultTableModel();
    DefaultTableModel modeloCliBusc = new DefaultTableModel();

    ConexionBD conect = new ConexionBD();

    // Dialogs
    JDialog clienteDialog;
    JDialog detalleDialog;

    // Controls (right panel)
    JFormattedTextField codigoCoti;
    JFormattedTextField cantidadCoti;
    JFormattedTextField dctProdCoti;
    JComboBox<String> cmbTipo;
    JLabel totalCotiLabel;
    JLabel clienteLabel;
    JTextField vigenciaCoti;
    JTextField notasCoti;

    // Carrito table
    JTable tablaCarrito;

    // Cotizaciones table
    JTextField Busc;
    JTable tablaCotizaciones;

    // Client search inside dialog
    JTextField buscCliField;
    JTable tablaCliBusc;

    // Product search dialog
    JDialog productoDialog;
    JTextField buscProdField;
    JTable tablaProdBusc;
    DefaultTableModel modeloProdBusc = new DefaultTableModel();

    public CotizacionesP() {
        setLayout(new BorderLayout());
        conect.limpiarCotizacionTemp();
        buildUI();
        buildClienteDialog();
        buildProductoDialog();
        buildDetalleDialog();
        mostrarTablaCarrito();
        mostrarTablaCotizaciones("");
    }

    private void buildUI() {
        // ---- PAGE_START: title ----
        JPanel panelTitle = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel titulo = new JLabel("Cotizaciones");
        titulo.setFont(new Font("Noto Serif", Font.BOLD, 22));
        titulo.setForeground(new Color(78, 150, 150));
        panelTitle.add(titulo);
        add(panelTitle, BorderLayout.PAGE_START);

        // ---- LINE_END: controls ----
        JPanel jPanel2 = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 8, 5, 8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;

        // Código
        addLabel(jPanel2, "Código del producto:", row, 0, gbc);
        JPanel panelCodigo = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        panelCodigo.setOpaque(false);
        codigoCoti = new JFormattedTextField();
        codigoCoti.setFont(new Font("Noto Serif", Font.PLAIN, 16));
        codigoCoti.setPreferredSize(new Dimension(118, 28));
        codigoCoti.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                char c = evt.getKeyChar();
                if (!Cake.letrasMayus(c) && !Cake.numeros(c) && !Cake.guionShort(c)) evt.consume();
                if (Cake.tamaño(codigoCoti.getText(), 20)) evt.consume();
            }
        });
        JButton btnBuscarProd = new JButton("...");
        btnBuscarProd.setFont(new Font("Noto Serif", Font.BOLD, 13));
        btnBuscarProd.setToolTipText("Buscar producto por nombre o código");
        btnBuscarProd.setPreferredSize(new Dimension(44, 28));
        btnBuscarProd.addActionListener(e -> {
            Mise.limpiarTabla(modeloProdBusc);
            buscProdField.setText("");
            cargarProductos("");
            productoDialog.setVisible(true);
        });
        panelCodigo.add(codigoCoti);
        panelCodigo.add(btnBuscarProd);
        addComp(jPanel2, panelCodigo, row, 1, gbc);
        row++;

        // Cantidad
        addLabel(jPanel2, "Cantidad:", row, 0, gbc);
        cantidadCoti = new JFormattedTextField();
        cantidadCoti.setFont(new Font("Noto Serif", Font.PLAIN, 16));
        cantidadCoti.setPreferredSize(new Dimension(170, 28));
        cantidadCoti.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                char c = evt.getKeyChar();
                if (!Cake.numeros(c)) evt.consume();
                if (Cake.tamaño(cantidadCoti.getText(), 6)) evt.consume();
            }
        });
        addComp(jPanel2, cantidadCoti, row, 1, gbc);
        row++;

        // Tipo de venta
        addLabel(jPanel2, "Tipo de venta:", row, 0, gbc);
        cmbTipo = new JComboBox<>(new String[]{"Menudeo", "Mayoreo"});
        cmbTipo.setFont(new Font("Noto Serif", Font.PLAIN, 15));
        cmbTipo.setPreferredSize(new Dimension(170, 28));
        addComp(jPanel2, cmbTipo, row, 1, gbc);
        row++;

        // Descuento por producto
        addLabel(jPanel2, "Desc. producto (%):", row, 0, gbc);
        dctProdCoti = new JFormattedTextField();
        dctProdCoti.setFont(new Font("Noto Serif", Font.PLAIN, 16));
        dctProdCoti.setPreferredSize(new Dimension(170, 28));
        dctProdCoti.setText("0");
        dctProdCoti.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                char c = evt.getKeyChar();
                if (!Cake.numeros(c) && !Cake.inicioPunto(c)) evt.consume();
                if (Cake.tamaño(dctProdCoti.getText(), 5)) evt.consume();
            }
        });
        addComp(jPanel2, dctProdCoti, row, 1, gbc);
        row++;

        // Botones agregar / eliminar
        JButton btnAgregar = new JButton("Agregar");
        btnAgregar.setFont(new Font("Noto Serif", Font.BOLD, 15));
        btnAgregar.setForeground(new Color(78, 150, 150));
        btnAgregar.addActionListener(e -> agregarAlCarrito());
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1;
        jPanel2.add(btnAgregar, gbc);

        JButton btnElim = new JButton("Quitar");
        btnElim.setFont(new Font("Noto Serif", Font.BOLD, 15));
        btnElim.setForeground(new Color(78, 150, 150));
        btnElim.addActionListener(e -> quitarDelCarrito());
        gbc.gridx = 1; gbc.gridy = row; gbc.gridwidth = 1;
        jPanel2.add(btnElim, gbc);
        row++;

        // Total
        totalCotiLabel = new JLabel("Total: $0.00");
        totalCotiLabel.setFont(new Font("Noto Serif", Font.BOLD, 18));
        totalCotiLabel.setForeground(new Color(78, 150, 150));
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        jPanel2.add(totalCotiLabel, gbc);
        gbc.gridwidth = 1;
        row++;

        // Separador
        JSeparator sep = new JSeparator();
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        jPanel2.add(sep, gbc);
        gbc.gridwidth = 1;
        row++;

        // Cliente
        addLabel(jPanel2, "Cliente:", row, 0, gbc);
        JButton btnSelecCliente = new JButton("Seleccionar");
        btnSelecCliente.setFont(new Font("Noto Serif", Font.PLAIN, 14));
        btnSelecCliente.addActionListener(e -> {
            Mise.limpiarTabla(modeloCliBusc);
            cargarClientes("");
            clienteDialog.setVisible(true);
        });
        addComp(jPanel2, btnSelecCliente, row, 1, gbc);
        row++;

        clienteLabel = new JLabel("Público general");
        clienteLabel.setFont(new Font("Noto Serif", Font.ITALIC, 14));
        clienteLabel.setForeground(new Color(60, 60, 60));
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        jPanel2.add(clienteLabel, gbc);
        gbc.gridwidth = 1;
        row++;

        // Vigencia
        addLabel(jPanel2, "Vigencia (dd/MM/yyyy):", row, 0, gbc);
        vigenciaCoti = new JTextField();
        vigenciaCoti.setFont(new Font("Noto Serif", Font.PLAIN, 15));
        vigenciaCoti.setPreferredSize(new Dimension(170, 28));
        vigenciaCoti.setToolTipText("Fecha límite de la cotización, ej: 30/04/2026");
        addComp(jPanel2, vigenciaCoti, row, 1, gbc);
        row++;

        // Notas
        addLabel(jPanel2, "Notas:", row, 0, gbc);
        notasCoti = new JTextField();
        notasCoti.setFont(new Font("Noto Serif", Font.PLAIN, 15));
        notasCoti.setPreferredSize(new Dimension(170, 28));
        addComp(jPanel2, notasCoti, row, 1, gbc);
        row++;

        // Guardar
        JButton btnGuardar = new JButton("Guardar Cotización");
        btnGuardar.setFont(new Font("Noto Serif", Font.BOLD, 15));
        btnGuardar.setForeground(new Color(78, 150, 150));
        btnGuardar.addActionListener(e -> guardarCotizacion());
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        jPanel2.add(btnGuardar, gbc);
        gbc.gridwidth = 1;
        row++;

        // Limpiar
        JButton btnLimpiar = new JButton("Limpiar carrito");
        btnLimpiar.setFont(new Font("Noto Serif", Font.PLAIN, 14));
        btnLimpiar.addActionListener(e -> limpiarCarrito());
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        jPanel2.add(btnLimpiar, gbc);

        add(jPanel2, BorderLayout.LINE_END);

        // ---- CENTER: tables ----
        JPanel jPanel3 = new JPanel();
        jPanel3.setLayout(new BoxLayout(jPanel3, BoxLayout.PAGE_AXIS));

        // Carrito table
        JLabel lblCarrito = new JLabel("  Carrito actual");
        lblCarrito.setFont(new Font("Noto Serif", Font.BOLD, 16));
        lblCarrito.setForeground(new Color(78, 150, 150));
        lblCarrito.setAlignmentX(LEFT_ALIGNMENT);
        jPanel3.add(lblCarrito);

        tablaCarrito = new JTable(new DefaultTableModel(
            new Object[][]{},
            new String[]{"Código", "Producto", "Cantidad", "Tipo", "P.Unit", "Total", "Dct%"}
        ) {
            public boolean isCellEditable(int r, int c) { return false; }
        });
        modeloCarrito = (DefaultTableModel) tablaCarrito.getModel();
        tablaCarrito.setFont(new Font("Noto Serif", Font.PLAIN, 13));
        tablaCarrito.setRowHeight(22);
        JScrollPane scrollCarrito = new JScrollPane(tablaCarrito);
        scrollCarrito.setPreferredSize(new Dimension(600, 160));
        scrollCarrito.setAlignmentX(LEFT_ALIGNMENT);
        jPanel3.add(scrollCarrito);

        // Search cotizaciones
        JPanel panelBusc = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelBusc.setAlignmentX(LEFT_ALIGNMENT);
        JLabel lblBusc = new JLabel("Buscar cotizaciones:");
        lblBusc.setFont(new Font("Noto Serif", Font.BOLD, 15));
        Busc = new JTextField(20);
        Busc.setFont(new Font("Noto Serif", Font.PLAIN, 14));
        Busc.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                mostrarTablaCotizaciones(Busc.getText().trim());
            }
        });
        panelBusc.add(lblBusc);
        panelBusc.add(Busc);
        jPanel3.add(panelBusc);

        // Cotizaciones table
        tablaCotizaciones = new JTable(new DefaultTableModel(
            new Object[][]{},
            new String[]{"ID", "Fecha", "Cliente", "Total", "Estatus", "Vigencia"}
        ) {
            public boolean isCellEditable(int r, int c) { return false; }
        });
        modeloCotizaciones = (DefaultTableModel) tablaCotizaciones.getModel();
        tablaCotizaciones.setFont(new Font("Noto Serif", Font.PLAIN, 13));
        tablaCotizaciones.setRowHeight(22);
        JScrollPane scrollCotizaciones = new JScrollPane(tablaCotizaciones);
        scrollCotizaciones.setPreferredSize(new Dimension(600, 200));
        scrollCotizaciones.setAlignmentX(LEFT_ALIGNMENT);
        jPanel3.add(scrollCotizaciones);

        // Action buttons for saved cotizaciones
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        panelBotones.setAlignmentX(LEFT_ALIGNMENT);

        JButton btnConvertir = new JButton("Convertir a Venta");
        btnConvertir.setFont(new Font("Noto Serif", Font.BOLD, 14));
        btnConvertir.setForeground(new Color(78, 150, 150));
        btnConvertir.addActionListener(e -> convertirAVenta());

        JButton btnCancelarCoti = new JButton("Cancelar Cot.");
        btnCancelarCoti.setFont(new Font("Noto Serif", Font.PLAIN, 13));
        btnCancelarCoti.addActionListener(e -> cancelarCotizacion());

        JButton btnImprimir = new JButton("Imprimir");
        btnImprimir.setFont(new Font("Noto Serif", Font.PLAIN, 13));
        btnImprimir.addActionListener(e -> imprimirCotizacion());

        JButton btnDetalle = new JButton("Ver detalle");
        btnDetalle.setFont(new Font("Noto Serif", Font.PLAIN, 13));
        btnDetalle.addActionListener(e -> verDetalle());

        panelBotones.add(btnConvertir);
        panelBotones.add(btnCancelarCoti);
        panelBotones.add(btnImprimir);
        panelBotones.add(btnDetalle);
        jPanel3.add(panelBotones);

        add(jPanel3, BorderLayout.CENTER);
    }

    private void addLabel(JPanel p, String text, int row, int col, GridBagConstraints gbc) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Noto Serif", Font.BOLD, 15));
        lbl.setForeground(new Color(78, 150, 150));
        gbc.gridx = col; gbc.gridy = row; gbc.gridwidth = 1;
        p.add(lbl, gbc);
    }

    private void addComp(JPanel p, JComponent c, int row, int col, GridBagConstraints gbc) {
        gbc.gridx = col; gbc.gridy = row; gbc.gridwidth = 1;
        p.add(c, gbc);
    }

    private void buildClienteDialog() {
        clienteDialog = new JDialog();
        clienteDialog.setTitle("Seleccionar cliente");
        clienteDialog.setModal(true);
        clienteDialog.setSize(420, 320);
        clienteDialog.setLocationRelativeTo(this);
        clienteDialog.setLayout(new BorderLayout(6, 6));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel lbl = new JLabel("Buscar:");
        buscCliField = new JTextField(20);
        buscCliField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                cargarClientes(buscCliField.getText().trim());
            }
        });
        top.add(lbl);
        top.add(buscCliField);
        clienteDialog.add(top, BorderLayout.NORTH);

        tablaCliBusc = new JTable(new DefaultTableModel(
            new Object[][]{}, new String[]{"CURP", "Nombre"}
        ) {
            public boolean isCellEditable(int r, int c) { return false; }
        });
        modeloCliBusc = (DefaultTableModel) tablaCliBusc.getModel();
        tablaCliBusc.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int row = tablaCliBusc.getSelectedRow();
                if (row != -1) {
                    idClienteCoti = "" + modeloCliBusc.getValueAt(row, 0);
                    nombreClienteCoti = "" + modeloCliBusc.getValueAt(row, 1);
                    clienteLabel.setText(nombreClienteCoti);
                    clienteDialog.setVisible(false);
                }
            }
        });
        clienteDialog.add(new JScrollPane(tablaCliBusc), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnGenerico = new JButton("Público general");
        btnGenerico.addActionListener(e -> {
            idClienteCoti = "XAXX111111HCCXXXX0";
            nombreClienteCoti = "Público general";
            clienteLabel.setText(nombreClienteCoti);
            clienteDialog.setVisible(false);
        });
        bottom.add(btnGenerico);
        clienteDialog.add(bottom, BorderLayout.SOUTH);
    }

    private void buildProductoDialog() {
        productoDialog = new JDialog();
        productoDialog.setTitle("Buscar producto");
        productoDialog.setModal(true);
        productoDialog.setSize(600, 370);
        productoDialog.setLocationRelativeTo(this);
        productoDialog.setLayout(new BorderLayout(6, 6));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel lbl = new JLabel("Buscar:");
        lbl.setFont(new Font("Noto Serif", Font.BOLD, 14));
        buscProdField = new JTextField(25);
        buscProdField.setFont(new Font("Noto Serif", Font.PLAIN, 14));
        buscProdField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                cargarProductos(buscProdField.getText().trim());
            }
        });
        top.add(lbl);
        top.add(buscProdField);
        productoDialog.add(top, BorderLayout.NORTH);

        tablaProdBusc = new JTable(new DefaultTableModel(
            new Object[][]{}, new String[]{"Código", "Nombre", "P.Menudeo", "P.Mayoreo", "Existencia"}
        ) {
            public boolean isCellEditable(int r, int c) { return false; }
        });
        modeloProdBusc = (DefaultTableModel) tablaProdBusc.getModel();
        tablaProdBusc.setFont(new Font("Noto Serif", Font.PLAIN, 13));
        tablaProdBusc.setRowHeight(22);
        tablaProdBusc.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int row = tablaProdBusc.getSelectedRow();
                if (row != -1) {
                    codigoCoti.setText("" + modeloProdBusc.getValueAt(row, 0));
                    productoDialog.setVisible(false);
                }
            }
        });
        productoDialog.add(new JScrollPane(tablaProdBusc), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnCerrar = new JButton("Cancelar");
        btnCerrar.addActionListener(e -> productoDialog.setVisible(false));
        bottom.add(btnCerrar);
        productoDialog.add(bottom, BorderLayout.SOUTH);
    }

    private void cargarProductos(String filtro) {
        Mise.limpiarTabla(modeloProdBusc);
        ResultSet rs = conect.buscarProductosCotizacion(filtro);
        try {
            while (rs != null && rs.next()) {
                modeloProdBusc.addRow(new Object[]{
                    rs.getString("id_producto"),
                    rs.getString("nombre"),
                    rs.getDouble("precio_menudeo"),
                    rs.getDouble("precio_mayoreo"),
                    rs.getInt("cantidad")
                });
            }
        } catch (java.sql.SQLException e) {}
    }

    private void buildDetalleDialog() {
        detalleDialog = new JDialog();
        detalleDialog.setTitle("Detalle de cotización");
        detalleDialog.setModal(true);
        detalleDialog.setSize(600, 350);
        detalleDialog.setLocationRelativeTo(this);
        JTable tablaDetalle = new JTable(new DefaultTableModel(
            new Object[][]{},
            new String[]{"Código", "Producto", "Cantidad", "Tipo", "P.Unit", "Dct%", "Total"}
        ) {
            public boolean isCellEditable(int r, int c) { return false; }
        });
        modeloDetalle = (DefaultTableModel) tablaDetalle.getModel();
        detalleDialog.add(new JScrollPane(tablaDetalle), BorderLayout.CENTER);
        JButton btnCerrar = new JButton("Cerrar");
        btnCerrar.addActionListener(e -> detalleDialog.setVisible(false));
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        p.add(btnCerrar);
        detalleDialog.add(p, BorderLayout.SOUTH);
    }

    private void cargarClientes(String filtro) {
        Mise.limpiarTabla(modeloCliBusc);
        String sql = "SELECT id_cliente, nombre FROM cliente WHERE estatus='Activo'";
        if (!filtro.isEmpty()) sql += " AND (id_cliente ILIKE '%" + filtro + "%' OR nombre ILIKE '%" + filtro + "%')";
        sql += " ORDER BY nombre";
        ResultSet rs = conect.query(sql);
        try {
            while (rs != null && rs.next()) {
                modeloCliBusc.addRow(new Object[]{rs.getString("id_cliente"), rs.getString("nombre")});
            }
        } catch (java.sql.SQLException e) {}
    }

    private void agregarAlCarrito() {
        String idProd = codigoCoti.getText().trim();
        String cantStr = cantidadCoti.getText().trim();
        if (idProd.isEmpty() || cantStr.isEmpty()) {
            Mise.JOption("Debe ingresar código y cantidad.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        int cant;
        try { cant = Integer.parseInt(cantStr); } catch (NumberFormatException ex) {
            Mise.JOption("La cantidad debe ser un número entero.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        double descuento = dctProdCoti.getText().isEmpty() ? 0.0 : Double.parseDouble(dctProdCoti.getText());
        double maxDesc = conect.obtenerMaxDescuento(idProd);
        if (descuento > maxDesc) {
            Mise.JOption("El descuento máximo para este producto es " + maxDesc + "%.\nSe aplicará ese límite.",
                "Advertencia", JOptionPane.WARNING_MESSAGE);
            descuento = maxDesc;
            dctProdCoti.setText("" + maxDesc);
        }
        String tipoVenta = (String) cmbTipo.getSelectedItem();
        conect.insertarCotizacionTemp(idProd, cant, tipoVenta, true);
        if (descuento > 0) conect.actualizarDescuentoCotiTemp(idProd, descuento);
        mostrarTablaCarrito();
        codigoCoti.setText("");
        cantidadCoti.setText("");
        dctProdCoti.setText("0");
    }

    private void quitarDelCarrito() {
        int row = tablaCarrito.getSelectedRow();
        if (row == -1) {
            Mise.JOption("Seleccione una fila del carrito para quitar.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String idProd = "" + modeloCarrito.getValueAt(row, 0);
        conect.eliminarCotizacionTemp(idProd);
        mostrarTablaCarrito();
    }

    private void guardarCotizacion() {
        if (modeloCarrito.getRowCount() == 0) {
            Mise.JOption("El carrito está vacío. Agregue productos antes de guardar.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        java.sql.Date vigencia = null;
        String vigStr = vigenciaCoti.getText().trim();
        if (!vigStr.isEmpty()) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                sdf.setLenient(false);
                java.util.Date d = sdf.parse(vigStr);
                vigencia = new java.sql.Date(d.getTime());
            } catch (ParseException ex) {
                Mise.JOption("Formato de vigencia inválido. Use dd/MM/yyyy.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
        double total = conect.sumaCotizacionTemp();
        String notas = notasCoti.getText().trim();
        String idCoti = conect.guardarCotizacion(Interfaz.idVendedor, idClienteCoti, total, vigencia, notas);
        if (!idCoti.isEmpty()) {
            Mise.JOption("Cotización guardada: " + idCoti, "Éxito", JOptionPane.PLAIN_MESSAGE);
            resetFormulario();
            mostrarTablaCotizaciones("");
        } else {
            Mise.JOption("Error al guardar la cotización.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void convertirAVenta() {
        int row = tablaCotizaciones.getSelectedRow();
        if (row == -1) {
            Mise.JOption("Seleccione una cotización de la lista.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String estatus = "" + modeloCotizaciones.getValueAt(row, 4);
        if (!"Pendiente".equals(estatus)) {
            Mise.JOption("Solo se pueden convertir cotizaciones con estatus 'Pendiente'.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String idCoti = "" + modeloCotizaciones.getValueAt(row, 0);
        String[] opciones = {"Efectivo", "Transferencia", "Tarjeta", "Otro"};
        String formaPago = (String) JOptionPane.showInputDialog(
            this, "Seleccione la forma de pago:", "Convertir a Venta",
            JOptionPane.QUESTION_MESSAGE, null, opciones, opciones[0]);
        if (formaPago == null) return;
        String idVenta = conect.convertirCotizacionAVenta(idCoti, formaPago);
        if (!idVenta.isEmpty()) {
            Mise.JOption("Venta registrada: " + idVenta + "\nCotización convertida exitosamente.",
                "Éxito", JOptionPane.PLAIN_MESSAGE);
            mostrarTablaCotizaciones("");
        } else {
            Mise.JOption("Error al convertir la cotización.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cancelarCotizacion() {
        int row = tablaCotizaciones.getSelectedRow();
        if (row == -1) {
            Mise.JOption("Seleccione una cotización de la lista.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String estatus = "" + modeloCotizaciones.getValueAt(row, 4);
        if (!"Pendiente".equals(estatus)) {
            Mise.JOption("Solo se pueden cancelar cotizaciones con estatus 'Pendiente'.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String idCoti = "" + modeloCotizaciones.getValueAt(row, 0);
        int conf = Mise.JOptionYesNo("¿Cancelar la cotización " + idCoti + "?", "Confirmar");
        if (conf == 0) {
            conect.cancelarCotizacion(idCoti);
            mostrarTablaCotizaciones("");
        }
    }

    private void imprimirCotizacion() {
        int row = tablaCotizaciones.getSelectedRow();
        if (row == -1) {
            Mise.JOption("Seleccione una cotización de la lista.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String idCoti = "" + modeloCotizaciones.getValueAt(row, 0);
        GenTicket.generarTicketCotizacion(idCoti);
    }

    private void verDetalle() {
        int row = tablaCotizaciones.getSelectedRow();
        if (row == -1) {
            Mise.JOption("Seleccione una cotización de la lista.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String idCoti = "" + modeloCotizaciones.getValueAt(row, 0);
        Mise.limpiarTabla(modeloDetalle);
        ResultSet rs = conect.obtenerDetalleCotizacion(idCoti);
        try {
            while (rs != null && rs.next()) {
                modeloDetalle.addRow(new Object[]{
                    rs.getString("id_producto"), rs.getString("nombre_p"),
                    rs.getInt("cantidad"), rs.getString("tipo_venta"),
                    rs.getDouble("precio_dado"), rs.getDouble("descuento_pct"),
                    rs.getDouble("precio_total")
                });
            }
        } catch (java.sql.SQLException e) {}
        detalleDialog.setTitle("Detalle: " + idCoti + " | " + modeloCotizaciones.getValueAt(row, 2));
        detalleDialog.setVisible(true);
    }

    public final void mostrarTablaCarrito() {
        Mise.limpiarTabla(modeloCarrito);
        ResultSet rs = conect.mostrarCotizacionTemp();
        try {
            while (rs != null && rs.next()) {
                modeloCarrito.addRow(new Object[]{
                    rs.getString("id_producto"), rs.getString("nombre_p"),
                    rs.getInt("cantidad_prod"), rs.getString("tipo_venta"),
                    rs.getDouble("precio_dado"), rs.getDouble("precio_total"),
                    rs.getDouble("descuento_pct")
                });
            }
        } catch (java.sql.SQLException e) {}
        totalCotiActual = conect.sumaCotizacionTemp();
        totalCotiLabel.setText(String.format("Total: $%.2f", totalCotiActual));
    }

    public void mostrarTablaCotizaciones(String filtro) {
        Mise.limpiarTabla(modeloCotizaciones);
        ResultSet rs = conect.obtenerCotizaciones(filtro);
        try {
            while (rs != null && rs.next()) {
                modeloCotizaciones.addRow(new Object[]{
                    rs.getString("id_cotizacion"), rs.getString("fecha"),
                    rs.getString("cliente"), rs.getDouble("total_cotizacion"),
                    rs.getString("estatus"), rs.getString("vigencia")
                });
            }
        } catch (java.sql.SQLException e) {}
    }

    public void limpiarCarrito() {
        conect.limpiarCotizacionTemp();
        mostrarTablaCarrito();
    }

    private void resetFormulario() {
        conect.limpiarCotizacionTemp();
        idClienteCoti = "XAXX111111HCCXXXX0";
        nombreClienteCoti = "Público general";
        clienteLabel.setText(nombreClienteCoti);
        vigenciaCoti.setText("");
        notasCoti.setText("");
        codigoCoti.setText("");
        cantidadCoti.setText("");
        dctProdCoti.setText("0");
        mostrarTablaCarrito();
    }
}
