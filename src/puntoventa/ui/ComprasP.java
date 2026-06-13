package puntoventa.ui;

import puntoventa.db.*;
import puntoventa.report.*;
import puntoventa.util.*;
import puntoventa.web.*;


import javax.swing.table.DefaultTableModel;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */

/**
 *
 * @author mayra
 */
public class ComprasP extends javax.swing.JPanel {

    String id_compra = "";
    ConexionBD conect = new ConexionBD();

    DefaultTableModel modeloCom = new DefaultTableModel();
    DefaultTableModel modeloProdCom = new DefaultTableModel();
    DefaultTableModel modeloProd = new DefaultTableModel();

    boolean ins = true;

    // Carrito de captura: los renglones viven en memoria hasta "Guardar factura".
    // modoCarrito = true al registrar una compra nueva; false al editar una compra ya guardada.
    private boolean modoCarrito = false;
    private final java.util.List<ConexionBD.RenglonCompra> carrito = new java.util.ArrayList<>();
    // Encabezado pendiente (solo en modoCarrito, aún no escrito en la BD)
    private int headIdProveedor = -1;
    private String headFolio = "", headFechaSql = "", headOrigen = "", headDescripcion = "";
    // id del borrador que se está capturando/retomando (0 = ninguno todavía)
    private int idBorradorActual = 0;
    // botón "Dejar pendiente" (se construye en el constructor, junto a "Guardar factura")
    private javax.swing.JButton dejarPendienteBtn;
    // botón "Descartar pendiente" (panel lateral; elimina un borrador 'En proceso')
    private javax.swing.JButton descartarPendienteBtn;
    // selector de fecha de factura: botón que muestra la fecha y abre el CalendarioPanel
    // (mismo componente que usa el reporte diario). Reemplaza al campo de texto generado.
    private javax.swing.JButton fechaFactBtn;
    private java.time.LocalDate fechaFacturaSel = java.time.LocalDate.now();

    // Proveedor
    private java.util.ArrayList<Integer> proveedorIds = new java.util.ArrayList<>();

    // Categoría (alta de producto desde compra)
    private java.util.ArrayList<Integer> categoriaIds = new java.util.ArrayList<>();

    // Alta de proveedor desde compras
    private javax.swing.JDialog nuevoProvDialog;
    private javax.swing.JTextField nombreProvNuevoF;
    private javax.swing.JTextField telefonoProvNuevoF;
    private javax.swing.JTextField emailProvNuevoF;
    private javax.swing.JTextField direccionProvNuevoF;
    private javax.swing.JTextField rfcProvNuevoF;
    private javax.swing.JLabel avisoProvNuevoL;

    /**
     * Creates new form ComprasP
     */
    public ComprasP() {
        initComponents();
        modeloCom = (DefaultTableModel)tablaCompras.getModel();
        modeloProdCom = (DefaultTableModel)tablaProdCom.getModel();
        modeloProd = (DefaultTableModel)tablaProd.getModel();
        construirNuevoProvDialog();
        agregarBotonesInfo();
        construirBotonDejarPendiente();
        construirBotonDescartarPendiente();
        construirSelectorFecha();
    }

    private void construirSelectorFecha() {
        // Reemplaza el campo de texto de fecha por un botón que muestra la fecha elegida y abre
        // el CalendarioPanel (igual que el reporte diario), para agilizar la captura.
        fechaFactBtn = new javax.swing.JButton();
        fechaFactBtn.setFont(new java.awt.Font("Noto Serif", 0, 18));
        fechaFactBtn.setPreferredSize(new java.awt.Dimension(250, 30));
        fechaFactBtn.setMinimumSize(new java.awt.Dimension(250, 30));
        fechaFactBtn.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        fechaFactBtn.setIcon(SvgIcon.load("/icons/reporteDiario.svg", SvgIcon.SMALL));
        fechaFactBtn.setToolTipText("Clic para elegir la fecha de la factura en el calendario.");
        fechaFactBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                abrirCalendarioFactura();
            }
        });
        actualizarTextoFechaBtn();
        // Sustituir el campo de texto generado por el botón, conservando su posición en la rejilla.
        java.awt.Container cont = fechaFactF.getParent();
        if (cont != null && cont.getLayout() instanceof java.awt.GridBagLayout) {
            java.awt.GridBagLayout gbl = (java.awt.GridBagLayout) cont.getLayout();
            java.awt.GridBagConstraints gbc = gbl.getConstraints(fechaFactF);
            cont.remove(fechaFactF);
            cont.add(fechaFactBtn, gbc);
            cont.revalidate();
            cont.repaint();
        }
    }

    private void actualizarTextoFechaBtn() {
        if (fechaFacturaSel == null) {
            fechaFactBtn.setText("Seleccionar fecha…");
        } else {
            fechaFactBtn.setText(fechaFacturaSel.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        }
    }

    private void abrirCalendarioFactura() {
        CalendarioPanel calPanel = new CalendarioPanel();
        if (fechaFacturaSel != null) calPanel.seleccionarFecha(fechaFacturaSel);

        javax.swing.JButton btnAceptar = new javax.swing.JButton("Aceptar");
        javax.swing.JButton btnCancelar = new javax.swing.JButton("Cancelar");
        javax.swing.JPanel btnPanel = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 6, 0));
        btnPanel.add(btnCancelar);
        btnPanel.add(btnAceptar);

        javax.swing.JPanel contenido = new javax.swing.JPanel(new java.awt.BorderLayout(0, 10));
        contenido.setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 12, 8, 12));
        contenido.add(new javax.swing.JLabel("Seleccione la fecha de la factura:"), java.awt.BorderLayout.NORTH);
        contenido.add(calPanel, java.awt.BorderLayout.CENTER);
        contenido.add(btnPanel, java.awt.BorderLayout.SOUTH);

        javax.swing.JDialog dialog = new javax.swing.JDialog(comDialog, "Fecha de la factura", true);
        dialog.setContentPane(contenido);
        dialog.pack();
        dialog.setLocationRelativeTo(comDialog);
        dialog.setAlwaysOnTop(true);

        final boolean[] ok = {false};
        btnAceptar.addActionListener(e -> { ok[0] = true; dialog.dispose(); });
        btnCancelar.addActionListener(e -> dialog.dispose());
        dialog.setVisible(true);

        if (ok[0]) {
            fechaFacturaSel = calPanel.getFechaSeleccionada();
            actualizarTextoFechaBtn();
        }
    }

    private void construirBotonDescartarPendiente() {
        descartarPendienteBtn = new javax.swing.JButton("Descartar pendiente");
        descartarPendienteBtn.setFont(new java.awt.Font("Noto Serif", 1, 18));
        descartarPendienteBtn.setBackground(new java.awt.Color(252, 149, 149));
        descartarPendienteBtn.setIcon(SvgIcon.load("/icons/delete.svg", SvgIcon.MEDIUM));
        descartarPendienteBtn.setToolTipText("Elimina una factura 'En proceso' seleccionada. No afecta inventario.");
        java.awt.GridBagConstraints gbc = new java.awt.GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gbc.insets = new java.awt.Insets(10, 30, 10, 30);
        descartarPendienteBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                descartarPendienteActionPerformed(evt);
            }
        });
        jPanel3.setPreferredSize(new java.awt.Dimension(150, 160));
        jPanel3.add(descartarPendienteBtn, gbc);
        jPanel3.revalidate();
    }

    private void descartarPendienteActionPerformed(java.awt.event.ActionEvent evt) {
        if (capturaEnProceso()) return;
        int row = tablaCompras.getSelectedRow();
        if (row == -1) {
            Mise.JOption("Seleccione la factura 'En proceso' que desea descartar.", "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
            return;
        }
        String estado = "" + tablaCompras.getValueAt(row, 5);
        if (!"En proceso".equals(estado)) {
            Mise.JOption("Solo se pueden descartar facturas 'En proceso'.\nLas compras terminadas no se eliminan desde aquí.", "Aviso", javax.swing.JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int res = Mise.JOptionYesNo("¿Descartar definitivamente esta factura pendiente?\n"
            + "Se perderá la captura del borrador (no afecta inventario).", "Descartar pendiente");
        if (res != 0) return;
        conect.eliminarBorrador(Integer.parseInt("" + tablaCompras.getValueAt(row, 0)));
        mostrarTablaCom();
    }

    private void construirBotonDejarPendiente() {
        dejarPendienteBtn = new javax.swing.JButton("Dejar pendiente");
        dejarPendienteBtn.setFont(new java.awt.Font("Noto Serif", 1, 18));
        dejarPendienteBtn.setBackground(new java.awt.Color(255, 221, 148));
        dejarPendienteBtn.setToolTipText("Guarda la factura como 'En proceso' para terminarla después. No afecta inventario.");
        dejarPendienteBtn.setPreferredSize(new java.awt.Dimension(200, 33));
        dejarPendienteBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dejarPendienteActionPerformed(evt);
            }
        });

        // Reorganizar panelTotales en DOS filas para que los botones no se salgan de la pantalla
        // en monitores pequeños: arriba los totales, abajo los botones.
        panelTotales.removeAll();
        panelTotales.setLayout(new java.awt.BorderLayout());

        javax.swing.JPanel filaTotales = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 15, 4));
        filaTotales.add(lblSubtotal);
        filaTotales.add(lblIva);
        filaTotales.add(lblTotal);
        filaTotales.add(lblTotalFactura);
        filaTotales.add(totalFacturaF);
        filaTotales.add(lblCuadre);

        javax.swing.JPanel filaBotones = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 15, 4));
        filaBotones.add(dejarPendienteBtn);
        filaBotones.add(guardarFacturaBtn);

        panelTotales.add(filaTotales, java.awt.BorderLayout.NORTH);
        panelTotales.add(filaBotones, java.awt.BorderLayout.SOUTH);
        panelTotales.revalidate();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        comDialog = new javax.swing.JDialog();
        panelRegCompra = new javax.swing.JPanel();
        jLabelProv = new javax.swing.JLabel();
        proveedorCombo = new javax.swing.JComboBox<>();
        nuevoProvBtn = new javax.swing.JButton();
        lblRfc = new javax.swing.JLabel();
        valRfc = new javax.swing.JLabel();
        lblFolio = new javax.swing.JLabel();
        folioF = new javax.swing.JFormattedTextField();
        lblFechaFact = new javax.swing.JLabel();
        fechaFactF = new javax.swing.JFormattedTextField();
        lblOrigen = new javax.swing.JLabel();
        origenF = new javax.swing.JFormattedTextField();
        jLabel4 = new javax.swing.JLabel();
        hechoB1 = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        rasF = new javax.swing.JTextPane();
        prodComDialog = new javax.swing.JFrame();
        panelRegProdC = new javax.swing.JPanel();
        jScrollPane7 = new javax.swing.JScrollPane();
        tablaProd = new javax.swing.JTable();
        jScrollPane6 = new javax.swing.JScrollPane();
        tablaProdCom = new javax.swing.JTable();
        jPanel5 = new javax.swing.JPanel();
        jLabel9 = new javax.swing.JLabel();
        codP = new javax.swing.JFormattedTextField();
        jLabelCodBarras = new javax.swing.JLabel();
        codBarrasP = new javax.swing.JFormattedTextField();
        buscarP = new javax.swing.JButton();
        estadoP = new javax.swing.JLabel();
        jLabelConcepto = new javax.swing.JLabel();
        conceptoP = new javax.swing.JFormattedTextField();
        jLabel11 = new javax.swing.JLabel();
        cantP = new javax.swing.JFormattedTextField();
        jLabelUnidadCompra = new javax.swing.JLabel();
        unidadCompraP = new javax.swing.JComboBox<>();
        jLabelFactor = new javax.swing.JLabel();
        factorP = new javax.swing.JFormattedTextField();
        jLabelUnidadVenta = new javax.swing.JLabel();
        unidadVentaP = new javax.swing.JComboBox<>();
        jLabel12 = new javax.swing.JLabel();
        precP = new javax.swing.JFormattedTextField();
        ivaP = new javax.swing.JCheckBox();
        jLabelMargen = new javax.swing.JLabel();
        margenP = new javax.swing.JFormattedTextField();
        jLabelPMenudeo = new javax.swing.JLabel();
        pMenudeoP = new javax.swing.JFormattedTextField();
        jLabelPMayoreo = new javax.swing.JLabel();
        pMayoreoP = new javax.swing.JFormattedTextField();
        jLabelCategoria = new javax.swing.JLabel();
        categoriaCombo = new javax.swing.JComboBox<>();
        jLabelMaxDescuento = new javax.swing.JLabel();
        maxDescuentoP = new javax.swing.JFormattedTextField();
        agP = new javax.swing.JButton();
        acP = new javax.swing.JButton();
        elP = new javax.swing.JButton();
        heP = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        panelTotales = new javax.swing.JPanel();
        lblSubtotal = new javax.swing.JLabel();
        lblIva = new javax.swing.JLabel();
        lblTotal = new javax.swing.JLabel();
        lblTotalFactura = new javax.swing.JLabel();
        totalFacturaF = new javax.swing.JFormattedTextField();
        lblCuadre = new javax.swing.JLabel();
        guardarFacturaBtn = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        lblOrdenCompras = new javax.swing.JLabel();
        cmbOrdenCompras = new javax.swing.JComboBox<>();
        jScrollPane1 = new javax.swing.JScrollPane();
        tablaCompras = new javax.swing.JTable();
        jPanel3 = new javax.swing.JPanel();
        agreCompra = new javax.swing.JButton();
        actCompra = new javax.swing.JButton();

        comDialog.setTitle("Compras");
        comDialog.setAlwaysOnTop(true);
        comDialog.setMinimumSize(new java.awt.Dimension(620, 600));
        comDialog.setModal(true);
        comDialog.setSize(new java.awt.Dimension(630, 500));
        comDialog.getContentPane().setLayout(new java.awt.CardLayout());

        panelRegCompra.setLayout(new java.awt.GridBagLayout());

        jLabelProv.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        jLabelProv.setText("Proveedor:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(16, 20, 8, 12);
        panelRegCompra.add(jLabelProv, gridBagConstraints);

        proveedorCombo.setFont(new java.awt.Font("Noto Serif", 0, 18)); // NOI18N
        proveedorCombo.setPreferredSize(new java.awt.Dimension(250, 30));
        proveedorCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                proveedorComboActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(16, 0, 8, 8);
        panelRegCompra.add(proveedorCombo, gridBagConstraints);

        nuevoProvBtn.setFont(new java.awt.Font("Noto Serif", 1, 16)); // NOI18N
        nuevoProvBtn.setBackground(new java.awt.Color(153, 204, 255));
        nuevoProvBtn.setText("+ Nuevo");
        nuevoProvBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nuevoProvBtnActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(16, 8, 8, 20);
        panelRegCompra.add(nuevoProvBtn, gridBagConstraints);

        lblRfc.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        lblRfc.setText("RFC:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(8, 20, 8, 12);
        panelRegCompra.add(lblRfc, gridBagConstraints);

        valRfc.setFont(new java.awt.Font("Noto Serif", 0, 18)); // NOI18N
        valRfc.setText(" ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(8, 0, 8, 20);
        panelRegCompra.add(valRfc, gridBagConstraints);

        lblFolio.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        lblFolio.setText("Folio / Nº remisión:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(8, 20, 8, 12);
        panelRegCompra.add(lblFolio, gridBagConstraints);

        folioF.setFont(new java.awt.Font("Noto Serif", 0, 18)); // NOI18N
        folioF.setPreferredSize(new java.awt.Dimension(250, 30));
        folioF.setMinimumSize(new java.awt.Dimension(250, 30));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(8, 0, 8, 20);
        panelRegCompra.add(folioF, gridBagConstraints);

        lblFechaFact.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        lblFechaFact.setText("Fecha factura:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(8, 20, 8, 12);
        panelRegCompra.add(lblFechaFact, gridBagConstraints);

        fechaFactF.setFont(new java.awt.Font("Noto Serif", 0, 18)); // NOI18N
        fechaFactF.setPreferredSize(new java.awt.Dimension(250, 30));
        fechaFactF.setMinimumSize(new java.awt.Dimension(250, 30));
        fechaFactF.setToolTipText("dd/MM/yyyy");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(8, 0, 8, 20);
        panelRegCompra.add(fechaFactF, gridBagConstraints);

        lblOrigen.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        lblOrigen.setText("Origen:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(8, 20, 8, 12);
        panelRegCompra.add(lblOrigen, gridBagConstraints);

        origenF.setFont(new java.awt.Font("Noto Serif", 0, 18)); // NOI18N
        origenF.setPreferredSize(new java.awt.Dimension(250, 30));
        origenF.setMinimumSize(new java.awt.Dimension(250, 30));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(8, 0, 8, 20);
        panelRegCompra.add(origenF, gridBagConstraints);

        jLabel4.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        jLabel4.setText("Descripcion:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(8, 20, 8, 12);
        panelRegCompra.add(jLabel4, gridBagConstraints);

        hechoB1.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        hechoB1.setBackground(new java.awt.Color(125, 255, 177));
        hechoB1.setIcon(SvgIcon.load("/icons/guardar.svg", SvgIcon.MEDIUM));
        hechoB1.setText("Continuar");
        hechoB1.setPreferredSize(new java.awt.Dimension(100, 40));
        hechoB1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                hechoB1ActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        panelRegCompra.add(hechoB1, gridBagConstraints);

        jScrollPane2.setPreferredSize(new java.awt.Dimension(342, 70));
        jScrollPane2.setMinimumSize(new java.awt.Dimension(342, 70));

        rasF.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        rasF.setPreferredSize(new java.awt.Dimension(340, 50));
        rasF.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                rasFKeyTyped(evt);
            }
        });
        jScrollPane2.setViewportView(rasF);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(8, 0, 8, 20);
        panelRegCompra.add(jScrollPane2, gridBagConstraints);

        comDialog.getContentPane().add(panelRegCompra, "card2");

        comDialog.setLocationRelativeTo(null);

        prodComDialog.setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        prodComDialog.setTitle("Compras - captura de productos");
        prodComDialog.setMinimumSize(new java.awt.Dimension(800, 450));
        prodComDialog.setPreferredSize(new java.awt.Dimension(1000, 500));
        prodComDialog.setSize(new java.awt.Dimension(1000, 500));
        prodComDialog.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                prodComDialogWindowClosing(evt);
            }
        });
        prodComDialog.getContentPane().setLayout(new java.awt.CardLayout());

        panelRegProdC.setLayout(new java.awt.BorderLayout());

        jScrollPane7.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        jScrollPane7.setMinimumSize(new java.awt.Dimension(150, 150));
        jScrollPane7.setOpaque(false);
        jScrollPane7.setPreferredSize(new java.awt.Dimension(250, 250));

        tablaProd.setFont(new java.awt.Font("Noto Serif", 0, 16)); // NOI18N
        tablaProd.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Producto", "Nombre"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tablaProd.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
        tablaProd.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tablaProdMouseClicked(evt);
            }
        });
        jScrollPane7.setViewportView(tablaProd);

        javax.swing.JPanel panelTablas = new javax.swing.JPanel(new java.awt.GridLayout(1, 2, 8, 0));
        panelTablas.add(jScrollPane7);

        tablaProdCom.setFont(new java.awt.Font("Noto Serif", 0, 18)); // NOI18N
        tablaProdCom.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Código", "Concepto", "Cant", "Unidad", "P.Unit s/IVA", "IVA", "Importe s/IVA"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane6.setViewportView(tablaProdCom);

        panelTablas.add(jScrollPane6);

        jPanel5.setLayout(new java.awt.GridBagLayout());

        jLabel9.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        jLabel9.setText("Código:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 10, 6, 6);
        jPanel5.add(jLabel9, gridBagConstraints);

        codP.setEditable(true);
        codP.setFont(new java.awt.Font("Noto Serif", 0, 18)); // NOI18N
        codP.setPreferredSize(new java.awt.Dimension(200, 30));
        codP.setMinimumSize(new java.awt.Dimension(200, 30));
        codP.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                codPKeyTyped(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 6, 6);
        jPanel5.add(codP, gridBagConstraints);

        jLabelCodBarras.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        jLabelCodBarras.setText("Cód. barras:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 10, 6, 6);
        jPanel5.add(jLabelCodBarras, gridBagConstraints);

        codBarrasP.setFont(new java.awt.Font("Noto Serif", 0, 18)); // NOI18N
        codBarrasP.setPreferredSize(new java.awt.Dimension(200, 30));
        codBarrasP.setMinimumSize(new java.awt.Dimension(200, 30));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 6, 6);
        jPanel5.add(codBarrasP, gridBagConstraints);

        buscarP.setFont(new java.awt.Font("Noto Serif", 1, 16)); // NOI18N
        buscarP.setBackground(new java.awt.Color(153, 204, 255));
        buscarP.setIcon(SvgIcon.load("/icons/lupa.svg", SvgIcon.SMALL));
        buscarP.setText("Buscar");
        buscarP.setPreferredSize(new java.awt.Dimension(100, 30));
        buscarP.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buscarPActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 6, 6);
        jPanel5.add(buscarP, gridBagConstraints);

        estadoP.setFont(new java.awt.Font("Noto Serif", 1, 14)); // NOI18N
        estadoP.setText(" ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 6, 10);
        jPanel5.add(estadoP, gridBagConstraints);

        jLabelConcepto.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        jLabelConcepto.setText("Concepto:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 10, 6, 6);
        jPanel5.add(jLabelConcepto, gridBagConstraints);

        conceptoP.setFont(new java.awt.Font("Noto Serif", 0, 18)); // NOI18N
        conceptoP.setPreferredSize(new java.awt.Dimension(260, 30));
        conceptoP.setMinimumSize(new java.awt.Dimension(260, 30));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 6, 10);
        jPanel5.add(conceptoP, gridBagConstraints);

        jLabel11.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        jLabel11.setText("Cantidad:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 10, 6, 6);
        jPanel5.add(jLabel11, gridBagConstraints);

        cantP.setFont(new java.awt.Font("Noto Serif", 0, 18)); // NOI18N
        cantP.setPreferredSize(new java.awt.Dimension(200, 30));
        cantP.setMinimumSize(new java.awt.Dimension(200, 30));
        cantP.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                cantPKeyTyped(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 6, 6);
        jPanel5.add(cantP, gridBagConstraints);

        jLabelUnidadCompra.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        jLabelUnidadCompra.setText("Unidad compra:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 6, 6);
        jPanel5.add(jLabelUnidadCompra, gridBagConstraints);

        unidadCompraP.setFont(new java.awt.Font("Noto Serif", 0, 18)); // NOI18N
        unidadCompraP.setModel(new javax.swing.DefaultComboBoxModel<>(new String[]{"Pieza","Caja","Bolsa","Bulto","Botella","Lata","Paquete","Bote","Barra","Vaso","Tetra Pak"}));
        unidadCompraP.setPreferredSize(new java.awt.Dimension(200, 30));
        unidadCompraP.setMinimumSize(new java.awt.Dimension(200, 30));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 6, 10);
        jPanel5.add(unidadCompraP, gridBagConstraints);

        jLabelFactor.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        jLabelFactor.setText("Factor:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 10, 6, 6);
        jPanel5.add(jLabelFactor, gridBagConstraints);

        factorP.setFont(new java.awt.Font("Noto Serif", 0, 18)); // NOI18N
        factorP.setPreferredSize(new java.awt.Dimension(200, 30));
        factorP.setMinimumSize(new java.awt.Dimension(200, 30));
        factorP.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                recalcularPMenudeo();
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 6, 6);
        jPanel5.add(factorP, gridBagConstraints);

        jLabelUnidadVenta.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        jLabelUnidadVenta.setText("Unidad venta:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 6, 6);
        jPanel5.add(jLabelUnidadVenta, gridBagConstraints);

        unidadVentaP.setFont(new java.awt.Font("Noto Serif", 0, 18)); // NOI18N
        unidadVentaP.setModel(new javax.swing.DefaultComboBoxModel<>(new String[]{"Pieza","Caja","Bolsa","Bulto","Botella","Lata","Paquete","Bote","Barra","Vaso","Tetra Pak"}));
        unidadVentaP.setPreferredSize(new java.awt.Dimension(200, 30));
        unidadVentaP.setMinimumSize(new java.awt.Dimension(200, 30));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 6, 10);
        jPanel5.add(unidadVentaP, gridBagConstraints);

        jLabel12.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        jLabel12.setText("P. adquirido (sin IVA):");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 10, 6, 6);
        jPanel5.add(jLabel12, gridBagConstraints);

        precP.setFont(new java.awt.Font("Noto Serif", 0, 18)); // NOI18N
        precP.setPreferredSize(new java.awt.Dimension(200, 30));
        precP.setMinimumSize(new java.awt.Dimension(200, 30));
        precP.setToolTipText("Precio por unidad de compra, SIN IVA. El IVA se agrega en los totales si marca 'Lleva IVA'.");
        precP.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                precPKeyTyped(evt);
            }
        });
        precP.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                recalcularPMenudeo();
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 6, 6);
        jPanel5.add(precP, gridBagConstraints);

        ivaP.setFont(new java.awt.Font("Noto Serif", 1, 16)); // NOI18N
        ivaP.setText("Lleva IVA");
        ivaP.setToolTipText("Marque si este renglón causa IVA (16%). El precio se captura SIN IVA.");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 6, 6);
        jPanel5.add(ivaP, gridBagConstraints);

        jLabelMargen.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        jLabelMargen.setText("Margen %:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 10, 6, 6);
        jPanel5.add(jLabelMargen, gridBagConstraints);

        margenP.setFont(new java.awt.Font("Noto Serif", 0, 18)); // NOI18N
        margenP.setPreferredSize(new java.awt.Dimension(200, 30));
        margenP.setMinimumSize(new java.awt.Dimension(200, 30));
        margenP.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                recalcularPMenudeo();
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 6, 6);
        jPanel5.add(margenP, gridBagConstraints);

        jLabelPMenudeo.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        jLabelPMenudeo.setText("Precio menudeo:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 6, 6);
        jPanel5.add(jLabelPMenudeo, gridBagConstraints);

        pMenudeoP.setFont(new java.awt.Font("Noto Serif", 0, 18)); // NOI18N
        pMenudeoP.setPreferredSize(new java.awt.Dimension(200, 30));
        pMenudeoP.setMinimumSize(new java.awt.Dimension(200, 30));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 6, 10);
        jPanel5.add(pMenudeoP, gridBagConstraints);

        jLabelPMayoreo.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        jLabelPMayoreo.setText("Precio mayoreo:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 10, 6, 6);
        jPanel5.add(jLabelPMayoreo, gridBagConstraints);

        pMayoreoP.setFont(new java.awt.Font("Noto Serif", 0, 18)); // NOI18N
        pMayoreoP.setPreferredSize(new java.awt.Dimension(200, 30));
        pMayoreoP.setMinimumSize(new java.awt.Dimension(200, 30));
        pMayoreoP.setToolTipText("Debe ser menor que el precio menudeo.");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 6, 6);
        jPanel5.add(pMayoreoP, gridBagConstraints);

        jLabelCategoria.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        jLabelCategoria.setText("Categoría:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 6, 6);
        jPanel5.add(jLabelCategoria, gridBagConstraints);

        categoriaCombo.setFont(new java.awt.Font("Noto Serif", 0, 18)); // NOI18N
        categoriaCombo.setPreferredSize(new java.awt.Dimension(200, 30));
        categoriaCombo.setMinimumSize(new java.awt.Dimension(200, 30));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 6, 10);
        jPanel5.add(categoriaCombo, gridBagConstraints);

        jLabelMaxDescuento.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        jLabelMaxDescuento.setText("Desc. máximo (%):");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 10, 6, 6);
        jPanel5.add(jLabelMaxDescuento, gridBagConstraints);

        maxDescuentoP.setFont(new java.awt.Font("Noto Serif", 0, 18)); // NOI18N
        maxDescuentoP.setPreferredSize(new java.awt.Dimension(200, 30));
        maxDescuentoP.setMinimumSize(new java.awt.Dimension(200, 30));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 6, 6);
        jPanel5.add(maxDescuentoP, gridBagConstraints);

        agP.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        agP.setBackground(new java.awt.Color(125, 255, 177));
        agP.setIcon(SvgIcon.load("/icons/add-task.svg", SvgIcon.MEDIUM));
        agP.setText("Agregar");
        agP.setPreferredSize(new java.awt.Dimension(125, 33));
        agP.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                agPActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(12, 10, 10, 6);
        jPanel5.add(agP, gridBagConstraints);

        acP.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        acP.setBackground(new java.awt.Color(255, 251, 128));
        acP.setIcon(SvgIcon.load("/icons/edit.svg", SvgIcon.MEDIUM));
        acP.setText("Actualizar");
        acP.setPreferredSize(new java.awt.Dimension(125, 33));
        acP.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                acPActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(12, 6, 10, 6);
        jPanel5.add(acP, gridBagConstraints);

        elP.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        elP.setBackground(new java.awt.Color(252, 149, 149));
        elP.setIcon(SvgIcon.load("/icons/delete.svg", SvgIcon.MEDIUM));
        elP.setText("Eliminar");
        elP.setPreferredSize(new java.awt.Dimension(125, 33));
        elP.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                elPActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(12, 6, 10, 6);
        jPanel5.add(elP, gridBagConstraints);

        heP.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        heP.setBackground(new java.awt.Color(173, 216, 230));
        heP.setIcon(SvgIcon.load("/icons/clean.svg", SvgIcon.MEDIUM));
        heP.setText("Limpiar");
        heP.setToolTipText("Vacía los campos y los habilita para capturar un producto nuevo.");
        heP.setPreferredSize(new java.awt.Dimension(125, 33));
        heP.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                hePActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(12, 6, 10, 10);
        jPanel5.add(heP, gridBagConstraints);

        panelRegProdC.add(panelTablas, java.awt.BorderLayout.CENTER);

        jPanel4.setLayout(new java.awt.GridLayout(1, 0));

        jLabel1.setFont(new java.awt.Font("Noto Serif", 1, 24)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(78, 150, 150));
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel1.setText("Productos Inventario");
        jPanel4.add(jLabel1);

        jLabel2.setFont(new java.awt.Font("Noto Serif", 1, 24)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(78, 150, 150));
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel2.setText("Productos Comprados");
        jPanel4.add(jLabel2);

        panelRegProdC.add(jPanel4, java.awt.BorderLayout.NORTH);

        panelTotales.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 15, 8));

        lblSubtotal.setFont(new java.awt.Font("Noto Serif", 1, 16)); // NOI18N
        lblSubtotal.setText("Subtotal: $0.00");
        panelTotales.add(lblSubtotal);

        lblIva.setFont(new java.awt.Font("Noto Serif", 1, 16)); // NOI18N
        lblIva.setText("IVA 16%: $0.00");
        panelTotales.add(lblIva);

        lblTotal.setFont(new java.awt.Font("Noto Serif", 1, 16)); // NOI18N
        lblTotal.setText("Total: $0.00");
        panelTotales.add(lblTotal);

        lblTotalFactura.setFont(new java.awt.Font("Noto Serif", 1, 16)); // NOI18N
        lblTotalFactura.setText("Total en factura:");
        panelTotales.add(lblTotalFactura);

        totalFacturaF.setFont(new java.awt.Font("Noto Serif", 0, 16)); // NOI18N
        totalFacturaF.setPreferredSize(new java.awt.Dimension(110, 28));
        totalFacturaF.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                totalFacturaFKeyReleased(evt);
            }
        });
        panelTotales.add(totalFacturaF);

        lblCuadre.setFont(new java.awt.Font("Noto Serif", 1, 16)); // NOI18N
        lblCuadre.setText(" ");
        panelTotales.add(lblCuadre);

        guardarFacturaBtn.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        guardarFacturaBtn.setBackground(new java.awt.Color(125, 255, 177));
        guardarFacturaBtn.setIcon(SvgIcon.load("/icons/guardar.svg", SvgIcon.MEDIUM));
        guardarFacturaBtn.setText("Guardar factura");
        guardarFacturaBtn.setPreferredSize(new java.awt.Dimension(220, 33));
        guardarFacturaBtn.setMaximumSize(new java.awt.Dimension(220, 33));
        guardarFacturaBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                guardarFacturaBtnActionPerformed(evt);
            }
        });
        panelTotales.add(guardarFacturaBtn);

        javax.swing.JScrollPane scrollCaptura = new javax.swing.JScrollPane(jPanel5);
        scrollCaptura.setBorder(null);
        scrollCaptura.getVerticalScrollBar().setUnitIncrement(16);
        javax.swing.JPanel panelSur = new javax.swing.JPanel(new java.awt.BorderLayout());
        panelSur.add(scrollCaptura, java.awt.BorderLayout.CENTER);
        panelSur.add(panelTotales, java.awt.BorderLayout.SOUTH);
        panelRegProdC.add(panelSur, java.awt.BorderLayout.SOUTH);

        prodComDialog.getContentPane().add(panelRegProdC, "card3");

        prodComDialog.setLocationRelativeTo(null);

        setLayout(new java.awt.BorderLayout());

        jPanel1.setLayout(new java.awt.GridBagLayout());

        jLabel3.setFont(new java.awt.Font("Noto Serif", 1, 36)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(78, 150, 150));
        jLabel3.setIcon(SvgIcon.load("/icons/compras.svg", SvgIcon.LARGE));
        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel3.setText("Compras");
        jLabel3.setPreferredSize(new java.awt.Dimension(163, 70));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.CENTER;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 5, 10);
        jPanel1.add(jLabel3, gridBagConstraints);

        lblOrdenCompras.setFont(new java.awt.Font("Noto Serif", 1, 16)); // NOI18N
        lblOrdenCompras.setText("Ordenar:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 8, 10);
        jPanel1.add(lblOrdenCompras, gridBagConstraints);

        cmbOrdenCompras.setFont(new java.awt.Font("Noto Serif", 0, 16)); // NOI18N
        cmbOrdenCompras.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] {"Sin ordenar", "Más reciente", "Más antiguo", "Mayor monto", "Menor monto"}));
        cmbOrdenCompras.setPreferredSize(new java.awt.Dimension(200, 30));
        cmbOrdenCompras.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbOrdenComprasActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 8, 10);
        jPanel1.add(cmbOrdenCompras, gridBagConstraints);

        add(jPanel1, java.awt.BorderLayout.NORTH);

        jScrollPane1.setPreferredSize(new java.awt.Dimension(540, 402));

        tablaCompras.setFont(new java.awt.Font("Noto Serif", 0, 14)); // NOI18N
        tablaCompras.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Compra", "Folio", "Proveedor", "Fecha", "Total", "Estado"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane1.setViewportView(tablaCompras);

        add(jScrollPane1, java.awt.BorderLayout.CENTER);

        jPanel3.setPreferredSize(new java.awt.Dimension(150, 105));
        jPanel3.setLayout(new java.awt.GridBagLayout());

        agreCompra.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        agreCompra.setBackground(new java.awt.Color(125, 255, 177));
        agreCompra.setIcon(SvgIcon.load("/icons/add-task.svg", SvgIcon.MEDIUM));
        agreCompra.setText("Agregar");
        agreCompra.setPreferredSize(new java.awt.Dimension(127, 33));
        agreCompra.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                agreCompraActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(10, 30, 10, 30);
        jPanel3.add(agreCompra, gridBagConstraints);

        actCompra.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        actCompra.setBackground(new java.awt.Color(255, 251, 128));
        actCompra.setIcon(SvgIcon.load("/icons/edit.svg", SvgIcon.MEDIUM));
        actCompra.setText("Actualizar");
        actCompra.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                actCompraActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(10, 30, 10, 30);
        jPanel3.add(actCompra, gridBagConstraints);

        add(jPanel3, java.awt.BorderLayout.LINE_END);
    }// </editor-fold>//GEN-END:initComponents

    private void agreCompraActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_agreCompraActionPerformed
        if (capturaEnProceso()) return;
        cargarProveedores();
        folioF.setText(""); origenF.setText(""); rasF.setText(""); valRfc.setText("");
        fechaFacturaSel = java.time.LocalDate.now();   // por defecto, hoy
        actualizarTextoFechaBtn();
        comDialog.setVisible(true);
    }//GEN-LAST:event_agreCompraActionPerformed

    private void proveedorComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_proveedorComboActionPerformed
        int idx = proveedorCombo.getSelectedIndex();
        if (idx > 0 && idx <= proveedorIds.size()) {
            String[] fiscal = conect.obtenerDatosProveedor(proveedorIds.get(idx - 1));
            valRfc.setText(fiscal[0].isEmpty() ? "(sin RFC)" : fiscal[0]);
            valRfc.setToolTipText(fiscal[1]);
        } else {
            valRfc.setText("");
            valRfc.setToolTipText(null);
        }
    }//GEN-LAST:event_proveedorComboActionPerformed

    private void nuevoProvBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nuevoProvBtnActionPerformed
        nombreProvNuevoF.setText("");
        telefonoProvNuevoF.setText("");
        emailProvNuevoF.setText("");
        direccionProvNuevoF.setText("");
        rfcProvNuevoF.setText("");
        avisoProvNuevoL.setText(" ");
        nuevoProvDialog.setVisible(true);
    }//GEN-LAST:event_nuevoProvBtnActionPerformed

    private void actCompraActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_actCompraActionPerformed
        if (capturaEnProceso()) return;
        int row = tablaCompras.getSelectedRow();
        if(row != -1){
            String estado = "" + tablaCompras.getValueAt(row, 5);
            if ("En proceso".equals(estado)) {
                // Es un borrador: se retoma en modo carrito donde se dejó.
                retomarBorrador(Integer.parseInt("" + tablaCompras.getValueAt(row, 0)));
                return;
            }
            modoCarrito = false;
            idBorradorActual = 0;
            id_compra = "" + tablaCompras.getValueAt(row, 0);
            mostrarTablaProd();
            mostrarTablaProdCom();
            actualizarTotalesUI();
            cargarCategoriasCombo();
            ajustarDialogoProd();
            prodComDialog.setVisible(true);
        } else{
            Mise.JOption("Debe seleccionar la fila que desea actualizar", "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_actCompraActionPerformed

    private void retomarBorrador(int idBorrador) {
        String[] cab = conect.obtenerCabeceraBorrador(idBorrador);
        if (cab == null) {
            Mise.JOption("No se pudo cargar el borrador.", "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
            return;
        }
        idBorradorActual = idBorrador;
        modoCarrito = true;
        headIdProveedor = Integer.parseInt(cab[0]);
        headFolio = cab[1];
        headFechaSql = cab[2];
        headOrigen = cab[3];
        headDescripcion = cab[4];
        carrito.clear();
        carrito.addAll(conect.obtenerRenglonesBorrador(idBorrador));
        id_compra = "";
        ins = true;
        cargarCategoriasCombo();
        mostrarTablaProd();
        mostrarTablaProdCom();
        limpiarCapturaRenglon();
        totalFacturaF.setText(cab[5]);
        lblCuadre.setText("");
        actualizarTotalesUI();
        ajustarDialogoProd();
        prodComDialog.setVisible(true);
    }

    private void hechoB1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hechoB1ActionPerformed
        int idProvSeleccionado = -1;
        int idx = proveedorCombo.getSelectedIndex();
        if (idx > 0 && idx <= proveedorIds.size()) idProvSeleccionado = proveedorIds.get(idx - 1);

        String folio = folioF.getText().trim();
        if (idProvSeleccionado > 0 && !folio.isEmpty() && conect.folioYaRegistrado(idProvSeleccionado, folio)) {
            int r = Mise.JOptionYesNo("Esta factura (folio " + folio + ") ya fue registrada para este proveedor.\n¿Registrarla de todos modos?", "Folio duplicado");
            if (r != 0) return;
        }

        String fechaSql = "";
        if (fechaFacturaSel != null) {
            fechaSql = fechaFacturaSel.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        }

        // No se escribe nada en la BD todavía: el encabezado queda en memoria y los
        // renglones se capturan en el carrito. Todo se guarda junto en "Guardar factura".
        headIdProveedor = idProvSeleccionado;
        headFolio = folio;
        headFechaSql = fechaSql;
        headOrigen = origenF.getText().trim();
        headDescripcion = rasF.getText().trim();
        modoCarrito = true;
        idBorradorActual = 0;
        carrito.clear();
        id_compra = "";
        ins = true;

        cargarCategoriasCombo();
        mostrarTablaProd();
        mostrarTablaProdCom();
        limpiarCapturaRenglon();
        totalFacturaF.setText("");
        lblCuadre.setText("");
        actualizarTotalesUI();
        comDialog.setVisible(false);
        ajustarDialogoProd();
        prodComDialog.setVisible(true);
    }//GEN-LAST:event_hechoB1ActionPerformed

    private void codPKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_codPKeyTyped
        char letra = evt.getKeyChar();
        if(!Cake.letrasMayus(letra) && !Cake.numeros(letra) && !Cake.guionShort(letra)){
            evt.consume();
        }

        if(Cake.tamaño(codP.getText(), 20)){
            evt.consume();
        }
    }//GEN-LAST:event_codPKeyTyped

    private void cantPKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_cantPKeyTyped
        char letra = evt.getKeyChar();
        if(!Cake.numeros(letra)){
            evt.consume();
        }

        if(Cake.tamaño(cantP.getText(), 10)){
            evt.consume();
        }
    }//GEN-LAST:event_cantPKeyTyped

    private void precPKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_precPKeyTyped
        char letra = evt.getKeyChar();
        if(!Cake.numeros(letra) && !Cake.inicioPunto(letra)){
            evt.consume();
        }
        
        if(!(precP.getText().isEmpty())){
            if(Cake.hayPuntos(precP.getText()) && letra=='.'){
                evt.consume();
            } else{
                if(Cake.punto(precP.getText(), letra)){
                    evt.consume();
                }
            }
        } else{
            if(Cake.inicioPunto(letra)){
                evt.consume();
            }
        }
        
        if(Cake.tamaño(precP.getText(), 10)){
            evt.consume();
        }
    }//GEN-LAST:event_precPKeyTyped

    private void agPActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_agPActionPerformed
        String codigo = codP.getText().trim();
        if (codigo.isEmpty() || precP.getText().trim().isEmpty() || cantP.getText().trim().isEmpty()) {
            Mise.JOption("Debe llenar código, cantidad y precio de compra.", "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
            return;
        }
        String factorStr = factorP.getText().trim().isEmpty() ? "1" : factorP.getText().trim();
        double factor;
        try { factor = Double.parseDouble(factorStr); } catch (NumberFormatException e) { factor = 1; }
        if (factor <= 0) { Mise.JOption("El factor debe ser mayor a 0.", "Error", javax.swing.JOptionPane.ERROR_MESSAGE); return; }

        // El inventario (producto.cantidad) es entero: cantidad*factor debe dar unidades enteras,
        // de lo contrario el stock se redondearía en silencio.
        double cantNum;
        try { cantNum = Double.parseDouble(cantP.getText().trim()); } catch (NumberFormatException e) { cantNum = 0; }
        double unidadesVenta = cantNum * factor;
        if (Math.abs(unidadesVenta - Math.rint(unidadesVenta)) > 1e-9) {
            Mise.JOption("Cantidad × factor = " + unidadesVenta + " no es un número entero de unidades de venta.\n"
                + "Ajuste el factor o la cantidad (el inventario se maneja en unidades enteras).",
                "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
            return;
        }

        String idProd = conect.resolverCodigo(codigo);
        boolean nuevo = (idProd == null);

        ConexionBD.RenglonCompra r = new ConexionBD.RenglonCompra();
        r.nuevo = nuevo;
        r.precioCompra = precP.getText().trim();
        r.cantidad = cantP.getText().trim();
        r.factor = factorStr;

        if (nuevo) {
            if (!codigo.matches("^[A-Za-z0-9_-]{3,50}$")) {
                Mise.JOption("El código interno debe ser 3-50 caracteres (letras, números, - o _).", "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (conceptoP.getText().trim().isEmpty()) {
                Mise.JOption("Escriba el concepto/nombre del producto nuevo.", "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
                return;
            }
            String cb = codBarrasP.getText().trim();
            if (!cb.isEmpty() && !cb.matches("^[0-9]{8,14}$")) {
                Mise.JOption("El código de barras debe tener entre 8 y 14 dígitos.", "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (conect.codigoBarrasDuplicado(cb, codigo)) {
                Mise.JOption("Ese código de barras ya está en otro producto.", "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (pMenudeoP.getText().trim().isEmpty() || pMayoreoP.getText().trim().isEmpty()) {
                Mise.JOption("Capture el precio menudeo y el precio mayoreo del producto nuevo.", "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
                return;
            }
            double pMenudeo, pMayoreo;
            try {
                pMenudeo = Double.parseDouble(pMenudeoP.getText().trim());
                pMayoreo = Double.parseDouble(pMayoreoP.getText().trim());
            } catch (NumberFormatException e) {
                Mise.JOption("Los precios menudeo y mayoreo deben ser numéricos.", "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (pMayoreo >= pMenudeo) {
                Mise.JOption("El precio mayoreo debe ser menor que el precio menudeo.", "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
                return;
            }
            double maxDescuento = 100;
            if (!maxDescuentoP.getText().trim().isEmpty()) {
                try { maxDescuento = Double.parseDouble(maxDescuentoP.getText().trim()); } catch (NumberFormatException e) { maxDescuento = 100; }
                if (maxDescuento < 0) maxDescuento = 0;
                if (maxDescuento > 100) maxDescuento = 100;
            }
            Integer idCategoria = null;
            int idxCat = categoriaCombo.getSelectedIndex();
            if (idxCat > 0 && idxCat < categoriaIds.size()) idCategoria = categoriaIds.get(idxCat);

            // El código de barras no debe repetirse en otro renglón nuevo del mismo carrito.
            if (modoCarrito && !cb.isEmpty()) {
                for (ConexionBD.RenglonCompra rc : carrito) {
                    if (!codigo.equals(rc.idProducto) && cb.equals(rc.codigoBarras)) {
                        Mise.JOption("Ese código de barras ya está en otro renglón de esta factura.", "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }
            }
            idProd = codigo;
            r.idProducto = codigo;
            r.nombre = conceptoP.getText().trim();
            r.codigoBarras = cb;
            r.unidadCompra = (String) unidadCompraP.getSelectedItem();
            r.unidadVenta = (String) unidadVentaP.getSelectedItem();
            r.llevaIva = ivaP.isSelected();
            r.precioMenudeo = pMenudeoP.getText().trim();
            r.precioMayoreo = pMayoreoP.getText().trim();
            r.maxDescuento = maxDescuento;
            r.idCategoria = idCategoria;

            // En modo edición (compra ya guardada) el producto se crea de inmediato.
            if (!modoCarrito) {
                String[] d = { codigo, conceptoP.getText().trim(), cb,
                    (String) unidadCompraP.getSelectedItem(), (String) unidadVentaP.getSelectedItem(),
                    factorStr, ivaP.isSelected() ? "t" : "f",
                    pMenudeoP.getText().trim(), pMayoreoP.getText().trim(), precP.getText().trim() };
                if (!conect.crearProductoDesdeCompra(d, idCategoria, maxDescuento)) return;
            }
        } else {
            r.idProducto = idProd;
            String[] p = conect.obtenerProductoParaCompra(idProd);
            if (p != null) {
                r.nombre = p[0];
                r.codigoBarras = p[1];
                r.unidadCompra = p[2];
                r.unidadVenta = p[3];
                r.llevaIva = "t".equals(p[5]);
            } else {
                r.nombre = conceptoP.getText().trim();
                r.unidadCompra = (String) unidadCompraP.getSelectedItem();
                r.unidadVenta = (String) unidadVentaP.getSelectedItem();
                r.llevaIva = ivaP.isSelected();
            }
        }

        if (modoCarrito) {
            // Upsert por código: si el producto ya está en el carrito, se reemplaza el renglón.
            int idxExist = -1;
            for (int i = 0; i < carrito.size(); i++) {
                if (carrito.get(i).idProducto.equals(r.idProducto)) { idxExist = i; break; }
            }
            if (idxExist >= 0) carrito.set(idxExist, r); else carrito.add(r);
        } else {
            String[] campos = { id_compra, idProd, precP.getText().trim(), cantP.getText().trim(), factorStr };
            conect.insertarProdCompra(campos, ins);
        }
        ins = true;
        limpiarCapturaRenglon();
        mostrarTablaProdCom();
        actualizarTotalesUI();
    }//GEN-LAST:event_agPActionPerformed

    private void limpiarCapturaRenglon() {
        codP.setText(""); codBarrasP.setText(""); conceptoP.setText("");
        cantP.setText(""); precP.setText(""); factorP.setText(""); margenP.setText("");
        pMenudeoP.setText(""); pMayoreoP.setText(""); maxDescuentoP.setText("");
        if (categoriaCombo.getItemCount() > 0) categoriaCombo.setSelectedIndex(0);
        ivaP.setSelected(false); estadoP.setText("");
        setCamposNuevo(true);
    }

    private double[] calcularTotales() {
        double subtotal = 0, iva = 0;
        if (modoCarrito) {
            for (ConexionBD.RenglonCompra r : carrito) {
                double imp = parseD(r.precioCompra) * parseD(r.cantidad);
                subtotal += imp;
                if (r.llevaIva) iva += imp * 0.16;
            }
            return new double[]{ subtotal, iva, subtotal + iva };
        }
        java.sql.ResultSet rs = conect.query(
            "SELECT precio_total, COALESCE(lleva_iva,false) iva FROM compra_producto WHERE id_compra='" + id_compra + "';");
        try {
            while (rs.next()) {
                double imp = rs.getDouble("precio_total");
                subtotal += imp;
                if (rs.getBoolean("iva")) iva += imp * 0.16;
            }
        } catch (java.sql.SQLException e) { System.out.println("Error al calcular totales"); }
        return new double[]{ subtotal, iva, subtotal + iva };
    }

    private double parseD(String s) {
        try { return Double.parseDouble(s.trim()); } catch (Exception e) { return 0; }
    }

    private void actualizarTotalesUI() {
        double[] t = calcularTotales();
        lblSubtotal.setText(String.format(java.util.Locale.US, "Subtotal: $%.2f", t[0]));
        lblIva.setText(String.format(java.util.Locale.US, "IVA 16%%: $%.2f", t[1]));
        lblTotal.setText(String.format(java.util.Locale.US, "Total: $%.2f", t[2]));
        verificarCuadre(t[2]);
    }

    private void verificarCuadre(double totalCalc) {
        String txt = totalFacturaF.getText().trim();
        if (txt.isEmpty()) { lblCuadre.setText(""); return; }
        try {
            double totalFact = Double.parseDouble(txt);
            boolean cuadra = Math.abs(totalFact - totalCalc) <= 0.50;
            lblCuadre.setText(cuadra ? "✓ Cuadra" : "✗ No cuadra");
            lblCuadre.setForeground(cuadra ? new java.awt.Color(0,150,0) : java.awt.Color.RED);
        } catch (NumberFormatException e) { lblCuadre.setText(""); }
    }

    private void totalFacturaFKeyReleased(java.awt.event.KeyEvent evt) {
        verificarCuadre(calcularTotales()[2]);
    }

    private void guardarFacturaBtnActionPerformed(java.awt.event.ActionEvent evt) {
        if (modeloProdCom.getRowCount() == 0) {
            Mise.JOption("Agregue al menos un producto antes de guardar.", "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
            return;
        }
        double[] t = calcularTotales();
        String txt = totalFacturaF.getText().trim();
        if (modoCarrito) {
            // Al terminar la factura el Total en factura es OBLIGATORIO, para verificar el cuadre sí o sí.
            if (txt.isEmpty()) {
                Mise.JOption("Escriba el Total en factura para poder guardar (es obligatorio).", "Falta el total", javax.swing.JOptionPane.WARNING_MESSAGE);
                totalFacturaF.requestFocus();
                return;
            }
            double totalFact;
            try {
                totalFact = Double.parseDouble(txt);
            } catch (NumberFormatException e) {
                Mise.JOption("El Total en factura debe ser un número.", "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
                totalFacturaF.requestFocus();
                return;
            }
            if (Math.abs(totalFact - t[2]) > 0.50) {
                int r = Mise.JOptionYesNo("El total calculado ($" + String.format(java.util.Locale.US,"%.2f",t[2])
                    + ") NO cuadra con el de la factura ($" + String.format(java.util.Locale.US,"%.2f",totalFact) + ").\n"
                    + "¿Seguro que desea continuar? Su factura no cuadra.", "La factura no cuadra");
                if (r != 0) return;
            }
            // Única escritura a inventario: todo el carrito se guarda en una sola transacción
            // y, si venía de un borrador, se elimina dentro de la misma transacción.
            String[] cab = { Interfaz.idVendedor, headDescripcion, headFolio, headFechaSql, headOrigen };
            String idNueva = conect.guardarFacturaCompleta(cab, headIdProveedor, carrito, t[0], t[1], t[2], idBorradorActual);
            if (idNueva == null || idNueva.isEmpty()) {
                // Falló y se revirtió todo: no se cierra para no perder la captura.
                Mise.JOption("No se pudo guardar la factura. Revise los datos e intente de nuevo.", "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
                return;
            }
            id_compra = idNueva;
            carrito.clear();
            modoCarrito = false;
            idBorradorActual = 0;
        } else {
            // Edición de una compra ya terminada: el total en factura sigue siendo opcional.
            if (!txt.isEmpty()) {
                try {
                    if (Math.abs(Double.parseDouble(txt) - t[2]) > 0.50) {
                        int r = Mise.JOptionYesNo("El total calculado ($" + String.format(java.util.Locale.US,"%.2f",t[2])
                            + ") no cuadra con el de la factura ($" + txt + ").\n¿Guardar de todos modos?", "Totales no cuadran");
                        if (r != 0) return;
                    }
                } catch (NumberFormatException ignored) {}
            }
            conect.actualizarTotalesCompra(id_compra, t[0], t[1], t[2]);
        }
        prodComDialog.setVisible(false);
        mostrarTablaCom();
    }

    private void dejarPendienteActionPerformed(java.awt.event.ActionEvent evt) {
        if (!modoCarrito) {
            // Solo aplica a una compra en captura (nueva o borrador retomado), no a una ya terminada.
            Mise.JOption("Esta opción solo aplica al capturar una compra nueva.", "Aviso", javax.swing.JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if (carrito.isEmpty()) {
            Mise.JOption("Agregue al menos un producto antes de dejar la factura pendiente.", "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
            return;
        }
        // El borrador NO toca inventario; el total en factura aquí es opcional.
        String[] cab = { Interfaz.idVendedor, headDescripcion, headFolio, headFechaSql, headOrigen };
        Integer id = conect.guardarBorrador(cab, headIdProveedor, totalFacturaF.getText().trim(), carrito, idBorradorActual);
        if (id == null) {
            Mise.JOption("No se pudo guardar el borrador. Intente de nuevo.", "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
            return;
        }
        carrito.clear();
        modoCarrito = false;
        idBorradorActual = 0;
        prodComDialog.setVisible(false);
        mostrarTablaCom();
        Mise.JOption("La factura quedó guardada como 'En proceso'.\nPuede retomarla cuando quiera con el botón Actualizar.", "Guardada", javax.swing.JOptionPane.INFORMATION_MESSAGE);
    }

    private boolean capturaEnProceso() {
        // La ventana de captura es no-modal y se puede minimizar. Si ya hay una abierta
        // (aunque esté minimizada), evitamos abrir otra compra que pisaría id_compra.
        if (prodComDialog != null && prodComDialog.isVisible()) {
            prodComDialog.setExtendedState(java.awt.Frame.NORMAL);
            prodComDialog.toFront();
            prodComDialog.requestFocus();
            Mise.JOption("Ya hay una compra en captura.\nTermínela (Guardar factura) o ciérrela antes de abrir otra.",
                "Compra en proceso", javax.swing.JOptionPane.WARNING_MESSAGE);
            return true;
        }
        return false;
    }

    private void ajustarDialogoProd() {
        // Adapta el diálogo a la pantalla (clave en laptops de 13"): nunca más grande que la pantalla.
        prodComDialog.setExtendedState(java.awt.Frame.NORMAL);
        java.awt.Dimension scr = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        int w = Math.min(1120, scr.width - 40);
        int h = Math.min(700, scr.height - 60);
        prodComDialog.setSize(w, h);
        prodComDialog.setLocationRelativeTo(null);
    }

    private void setCamposNuevo(boolean nuevo) {
        conceptoP.setEditable(nuevo);
        unidadCompraP.setEnabled(nuevo);
        unidadVentaP.setEnabled(nuevo);
        factorP.setEditable(nuevo);
        ivaP.setEnabled(nuevo);
        margenP.setEditable(nuevo);
        pMenudeoP.setEditable(nuevo);
        pMayoreoP.setEditable(nuevo);
        categoriaCombo.setEnabled(nuevo);
        maxDescuentoP.setEditable(nuevo);
        estadoP.setText(nuevo ? "● Nuevo" : "Existente");
    }

    private void buscarPActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buscarPActionPerformed
        String codigo = codP.getText().trim();
        if (codigo.isEmpty()) codigo = codBarrasP.getText().trim();
        if (codigo.isEmpty()) { Mise.JOption("Escriba un código interno o de barras.", "Error", javax.swing.JOptionPane.ERROR_MESSAGE); return; }

        String idProd = conect.resolverCodigo(codigo);
        if (idProd != null) {
            String[] p = conect.obtenerProductoParaCompra(idProd);
            codP.setText(idProd);
            conceptoP.setText(p[0]);
            codBarrasP.setText(p[1]);
            unidadCompraP.setSelectedItem(p[2]);
            unidadVentaP.setSelectedItem(p[3]);
            factorP.setText(p[4]);
            ivaP.setSelected("t".equals(p[5]));
            setCamposNuevo(false);
        } else {
            if (factorP.getText().trim().isEmpty()) factorP.setText("1");
            if (margenP.getText().trim().isEmpty()) margenP.setText("30");
            if (maxDescuentoP.getText().trim().isEmpty()) maxDescuentoP.setText("100");
            setCamposNuevo(true);
        }
    }//GEN-LAST:event_buscarPActionPerformed

    private void recalcularPMenudeo() {
        if (!pMenudeoP.isEditable()) return;
        try {
            double precio = Double.parseDouble(precP.getText().trim());
            double factor = Double.parseDouble(factorP.getText().trim());
            double margen = Double.parseDouble(margenP.getText().trim());
            if (factor <= 0) return;
            double costoUnit = precio / factor;
            double venta = costoUnit * (1 + margen / 100.0);
            pMenudeoP.setText(String.format(java.util.Locale.US, "%.2f", venta));
        } catch (NumberFormatException ignored) {}
    }

    private void acPActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_acPActionPerformed
        int row = tablaProdCom.getSelectedRow();
        if (row == -1) {
            Mise.JOption("Seleccione la fila de la tabla de Productos Comprados que desea actualizar", "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (modoCarrito) {
            // El renglón vive en memoria: se puede reeditar por completo, incluso un producto
            // nuevo (concepto, precios, unidades…), porque todavía no se escribió en inventario.
            ConexionBD.RenglonCompra r = carrito.get(row);
            codP.setText(r.idProducto);
            conceptoP.setText(r.nombre);
            codBarrasP.setText(r.codigoBarras == null ? "" : r.codigoBarras);
            unidadCompraP.setSelectedItem(r.unidadCompra);
            unidadVentaP.setSelectedItem(r.unidadVenta);
            factorP.setText(r.factor);
            ivaP.setSelected(r.llevaIva);
            precP.setText(r.precioCompra);
            cantP.setText(r.cantidad);
            if (r.nuevo) {
                pMenudeoP.setText(r.precioMenudeo);
                pMayoreoP.setText(r.precioMayoreo);
                maxDescuentoP.setText(String.valueOf(r.maxDescuento));
                int idxCat = (r.idCategoria == null) ? 0 : categoriaIds.indexOf(r.idCategoria);
                categoriaCombo.setSelectedIndex(idxCat < 0 ? 0 : idxCat);
                setCamposNuevo(true);
            } else {
                setCamposNuevo(false);
            }
            ins = false;
            return;
        }
        codP.setText("" + tablaProdCom.getValueAt(row, 0));
        cantP.setText("" + tablaProdCom.getValueAt(row, 2));
        precP.setText("" + tablaProdCom.getValueAt(row, 4));
        buscarPActionPerformed(null);
        ins = false;
    }//GEN-LAST:event_acPActionPerformed

    private void elPActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_elPActionPerformed
        int row = tablaProdCom.getSelectedRow();
        if(row == -1){
            Mise.JOption("Seleccione la fila de la tabla de Productos Comprados que desea eliminar", "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        } else{
            if (modoCarrito) {
                carrito.remove(row);
            } else {
                String[] campos = {id_compra, "" + tablaProdCom.getValueAt(row, 0)};
                conect.eliminarProdCompra(campos);
            }
            mostrarTablaProdCom();
            actualizarTotalesUI();
            ins = true;
            limpiarCapturaRenglon();
        }
    }//GEN-LAST:event_elPActionPerformed

    private void prodComDialogWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_prodComDialogWindowClosing
        if (modoCarrito) {
            // En modo carrito no se escribió nada en inventario. Cerrar descarta los cambios
            // de esta sesión (si retomó un borrador, el borrador guardado se conserva tal cual).
            if (!carrito.isEmpty()) {
                int res = Mise.JOptionYesNo("Hay una factura en captura sin terminar.\n"
                    + "Si cierra se perderán los cambios no guardados de esta sesión.\n"
                    + "Use \"Dejar pendiente\" si quiere terminarla después.\n\n¿Cerrar de todos modos?", "Cerrar ventana");
                if (res != 0) return;
            }
            carrito.clear();
            modoCarrito = false;
            idBorradorActual = 0;
            prodComDialog.setVisible(false);
            return;
        }
        if(modeloProdCom.getRowCount() == 0){
            int res = Mise.JOptionYesNo("No registró ningun producto, ¿seguro que desea cerrar esta ventana?"
                    + "\n(al realizar esta acción la compra no podrá guardarse)", "Cerrar ventana");
            if(res == 0){
                conect.eliminarCompra(id_compra);
                prodComDialog.dispose();
            }
        } else{
            // Persistir totales aunque se cierre con la "X" (no solo con "Guardar factura"),
            // para que compras.monto/subtotal/iva no queden en 0/NULL con renglones ya commiteados.
            double[] t = calcularTotales();
            conect.actualizarTotalesCompra(id_compra, t[0], t[1], t[2]);
            prodComDialog.setVisible(false);
            mostrarTablaCom();
        }
    }//GEN-LAST:event_prodComDialogWindowClosing

    private void rasFKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_rasFKeyTyped
        char letra = evt.getKeyChar();
        if(!Cake.letrasMayus(letra) && !Cake.numeros(letra) && !Cake.letrasMinus(letra) && !(Cake.inicioEspacios(letra))){
            evt.consume();
        }

        if(!(rasF.getText().isEmpty())){
            if(Cake.espacios(rasF.getText(), letra)){
                evt.consume();
            }
        }
        else{
            if(Cake.inicioEspacios(letra)){
                evt.consume();
            }
        }

        if(Cake.tamaño(rasF.getText(), 100)){
            evt.consume();
        }
    }//GEN-LAST:event_rasFKeyTyped

    private void hePActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hePActionPerformed
        // Botón "Limpiar": vacía el formulario y re-habilita los campos para un producto nuevo.
        ins = true;
        tablaProdCom.clearSelection();
        limpiarCapturaRenglon();
        codP.requestFocus();
    }//GEN-LAST:event_hePActionPerformed

    private void tablaProdMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tablaProdMouseClicked
        codP.setText("" + tablaProd.getValueAt(tablaProd.getSelectedRow(), 0));
        buscarPActionPerformed(null);
    }//GEN-LAST:event_tablaProdMouseClicked

    private void cmbOrdenComprasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbOrdenComprasActionPerformed
        aplicarOrdenCompras();
    }//GEN-LAST:event_cmbOrdenComprasActionPerformed

    private void aplicarOrdenCompras() {
        String sel = (String) cmbOrdenCompras.getSelectedItem();
        if ("Sin ordenar".equals(sel)) {
            tablaCompras.setRowSorter(null);
            return;
        }
        javax.swing.table.TableRowSorter<javax.swing.table.DefaultTableModel> sorter =
            new javax.swing.table.TableRowSorter<>(modeloCom);
        tablaCompras.setRowSorter(sorter);
        if ("Más reciente".equals(sel)) {
            sorter.setComparator(3, java.util.Comparator.comparing(Object::toString));
            sorter.setSortKeys(java.util.Arrays.asList(new javax.swing.RowSorter.SortKey(3, javax.swing.SortOrder.DESCENDING)));
        } else if ("Más antiguo".equals(sel)) {
            sorter.setComparator(3, java.util.Comparator.comparing(Object::toString));
            sorter.setSortKeys(java.util.Arrays.asList(new javax.swing.RowSorter.SortKey(3, javax.swing.SortOrder.ASCENDING)));
        } else if ("Mayor monto".equals(sel)) {
            sorter.setComparator(4, java.util.Comparator.comparingDouble(o -> {
                try { return Double.parseDouble(o.toString()); } catch (Exception e) { return 0.0; }
            }));
            sorter.setSortKeys(java.util.Arrays.asList(new javax.swing.RowSorter.SortKey(4, javax.swing.SortOrder.DESCENDING)));
        } else if ("Menor monto".equals(sel)) {
            sorter.setComparator(4, java.util.Comparator.comparingDouble(o -> {
                try { return Double.parseDouble(o.toString()); } catch (Exception e) { return 0.0; }
            }));
            sorter.setSortKeys(java.util.Arrays.asList(new javax.swing.RowSorter.SortKey(4, javax.swing.SortOrder.ASCENDING)));
        }
        sorter.sort();
    }

    public void cargarCategoriasCombo(){
        categoriaIds.clear();
        categoriaCombo.removeAllItems();
        categoriaCombo.addItem("Sin categoría");
        categoriaIds.add(0);
        java.sql.ResultSet rs = conect.obtenerCategorias();
        if (rs != null) {
            try {
                while (rs.next()) {
                    categoriaIds.add(rs.getInt("id_categoria"));
                    categoriaCombo.addItem(rs.getString("nombre"));
                }
            } catch (java.sql.SQLException e) {
                System.out.println("Error al cargar categorías: " + e.getMessage());
            }
        }
    }

    public void cargarProveedores(){
        proveedorIds.clear();
        proveedorCombo.removeAllItems();
        proveedorCombo.addItem("-- Sin proveedor --");
        java.sql.ResultSet rs = conect.obtenerProveedores();
        if(rs != null){
            try{
                while(rs.next()){
                    proveedorIds.add(rs.getInt("id_proveedor"));
                    proveedorCombo.addItem(rs.getString("nombre"));
                }
            } catch(java.sql.SQLException e){
                System.out.println("Error al cargar proveedores");
            }
        }
    }

    private void construirNuevoProvDialog() {
        nuevoProvDialog = new javax.swing.JDialog();
        nuevoProvDialog.setTitle("Nuevo proveedor");
        nuevoProvDialog.setAlwaysOnTop(true);
        nuevoProvDialog.setMinimumSize(new java.awt.Dimension(550, 390));
        nuevoProvDialog.setModal(true);
        nuevoProvDialog.setResizable(false);

        javax.swing.JPanel panel = new javax.swing.JPanel(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints gbc;

        nombreProvNuevoF = new javax.swing.JTextField();
        telefonoProvNuevoF = new javax.swing.JTextField();
        emailProvNuevoF = new javax.swing.JTextField();
        direccionProvNuevoF = new javax.swing.JTextField();
        rfcProvNuevoF = new javax.swing.JTextField();
        avisoProvNuevoL = new javax.swing.JLabel(" ");

        String[] etiquetas = { "Nombre:", "Teléfono:", "Email:", "Dirección:", "RFC:" };
        javax.swing.JTextField[] campos = { nombreProvNuevoF, telefonoProvNuevoF, emailProvNuevoF, direccionProvNuevoF, rfcProvNuevoF };
        for (int i = 0; i < etiquetas.length; i++) {
            javax.swing.JLabel lbl = new javax.swing.JLabel(etiquetas[i]);
            lbl.setFont(new java.awt.Font("Noto Serif", 1, 18));
            gbc = new java.awt.GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = i;
            gbc.anchor = java.awt.GridBagConstraints.WEST;
            gbc.insets = new java.awt.Insets(10, 10, 10, 10);
            panel.add(lbl, gbc);

            campos[i].setFont(new java.awt.Font("Noto Serif", 0, 18));
            campos[i].setPreferredSize(new java.awt.Dimension(250, 30));
            gbc = new java.awt.GridBagConstraints();
            gbc.gridx = 1;
            gbc.gridy = i;
            gbc.anchor = java.awt.GridBagConstraints.WEST;
            gbc.insets = new java.awt.Insets(10, 10, 10, 10);
            panel.add(campos[i], gbc);
        }

        nombreProvNuevoF.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                char letra = evt.getKeyChar();
                if (!Cake.letrasMayus(letra) && !Cake.letrasMinus(letra) && !Cake.numeros(letra) && !Cake.inicioEspacios(letra)) {
                    evt.consume();
                }
            }
        });
        telefonoProvNuevoF.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                if (!Cake.numeros(evt.getKeyChar())) evt.consume();
            }
        });
        direccionProvNuevoF.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                if (Cake.tamaño(direccionProvNuevoF.getText(), 300)) evt.consume();
            }
        });

        avisoProvNuevoL.setFont(new java.awt.Font("Noto Serif", 0, 12));
        avisoProvNuevoL.setForeground(new java.awt.Color(204, 0, 51));
        avisoProvNuevoL.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        gbc = new java.awt.GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = etiquetas.length;
        gbc.gridwidth = 2;
        panel.add(avisoProvNuevoL, gbc);

        javax.swing.JButton guardarBtn = new javax.swing.JButton("Registrar");
        guardarBtn.setFont(new java.awt.Font("Noto Serif", 1, 18));
        guardarBtn.setBackground(new java.awt.Color(125, 255, 177));
        guardarBtn.setIcon(SvgIcon.load("/icons/guardar.svg", SvgIcon.MEDIUM));
        guardarBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                guardarNuevoProveedor();
            }
        });
        gbc = new java.awt.GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = etiquetas.length + 1;
        gbc.gridwidth = 2;
        gbc.insets = new java.awt.Insets(10, 10, 10, 10);
        panel.add(guardarBtn, gbc);

        nuevoProvDialog.getContentPane().add(panel);
        nuevoProvDialog.setLocationRelativeTo(null);
    }

    private void guardarNuevoProveedor() {
        String nombre = nombreProvNuevoF.getText().trim();
        String email = emailProvNuevoF.getText().trim();
        if (nombre.isEmpty()) {
            avisoProvNuevoL.setText("El nombre es obligatorio");
            return;
        }
        if (!email.isEmpty() && (!email.contains("@") || !email.contains("."))) {
            avisoProvNuevoL.setText("El email no tiene un formato válido");
            return;
        }
        String rfc = rfcProvNuevoF.getText().trim().toUpperCase();
        if (!rfc.isEmpty() && !rfc.matches("^[A-ZÑ&]{3,4}[0-9]{6}[A-Z0-9]{3}$")) {
            Mise.JOption("El RFC no tiene un formato válido. Se guardará de todos modos.", "Advertencia", javax.swing.JOptionPane.WARNING_MESSAGE);
        }
        Integer idNuevo = conect.insertarProveedor(nombre, telefonoProvNuevoF.getText().trim(), email, direccionProvNuevoF.getText().trim(), rfc);
        if (idNuevo != null) {
            nuevoProvDialog.setVisible(false);
            cargarProveedores();
            int idx = proveedorIds.indexOf(idNuevo);
            if (idx >= 0) {
                proveedorCombo.setSelectedIndex(idx + 1);
            }
        }
    }

    public void mostrarTablaCom(){
        Mise.limpiarTabla(modeloCom);
        // Une borradores (En proceso) y compras (Terminada). Los borradores aparecen arriba.
        java.sql.ResultSet rs = conect.query(
            "SELECT id, folio, prov, fecha, total, estado FROM ("
          + "  SELECT b.id_borrador::varchar AS id, COALESCE(b.folio_proveedor,'') AS folio, COALESCE(pr.nombre,'') AS prov, "
          + "         b.fecha_creacion::date AS fecha, "
          + "         COALESCE(b.total_factura,(SELECT COALESCE(SUM(precio_compra*cantidad),0) "
          + "             FROM compra_borrador_producto bp WHERE bp.id_borrador=b.id_borrador)) AS total, "
          + "         'En proceso' AS estado, 0 AS orden "
          + "    FROM compra_borrador b LEFT JOIN proveedor pr ON pr.id_proveedor=b.id_proveedor "
          + "  UNION ALL "
          + "  SELECT c.id_compra::varchar, COALESCE(c.folio_proveedor,''), COALESCE(pr.nombre,''), "
          + "         c.fecha_compra::date, c.monto, 'Terminada', 1 "
          + "    FROM compras c LEFT JOIN proveedor pr ON pr.id_proveedor=c.id_proveedor "
          + ") t ORDER BY orden, id DESC;");
        if (rs == null) return;
        try{
            while(rs.next()){
                modeloCom.addRow(new Object[]{ rs.getString("id"), rs.getString("folio"),
                    rs.getString("prov"), rs.getDate("fecha"), rs.getDouble("total"), rs.getString("estado") });
            }
        } catch(java.sql.SQLException e){ System.out.println("Error al mostrar la tabla compras"); }
    }
    
    public void mostrarTablaProdCom(){
        Mise.limpiarTabla(modeloProdCom);
        if (modoCarrito) {
            for (ConexionBD.RenglonCompra r : carrito) {
                double imp = parseD(r.precioCompra) * parseD(r.cantidad);
                modeloProdCom.addRow(new Object[]{ r.idProducto, r.nombre,
                    (int) parseD(r.cantidad), r.unidadCompra, parseD(r.precioCompra),
                    r.llevaIva ? "Sí" : "No", imp });
            }
            return;
        }
        java.sql.ResultSet rs = conect.query(
            "SELECT cp.id_producto, p.nombre, cp.cantidad, COALESCE(cp.unidad_compra,'') uc, "
          + "cp.precio_adquirido, COALESCE(cp.lleva_iva,false) iva, cp.precio_total "
          + "FROM compra_producto cp JOIN producto p ON p.id_producto = cp.id_producto "
          + "WHERE cp.id_compra='" + id_compra + "';");
        try{
            while(rs.next()){
                modeloProdCom.addRow(new Object[]{ rs.getString("id_producto"), rs.getString("nombre"),
                    rs.getInt("cantidad"), rs.getString("uc"), rs.getDouble("precio_adquirido"),
                    rs.getBoolean("iva") ? "Sí" : "No", rs.getDouble("precio_total") });
            }
        } catch(java.sql.SQLException e){
            System.out.println("Error al mostrar productos comprados");
        }
    }
    
    public void mostrarTablaProd(){
        Mise.limpiarTabla(modeloProd);
        java.sql.ResultSet rs = conect.query("SELECT * FROM producto WHERE estatus = 'Activo' ORDER BY id_producto");
        try{
            while(rs.next()){
                modeloProd.addRow(new Object[]{rs.getString("id_producto"), rs.getString("nombre")});
            }
        } catch(java.sql.SQLException e){
            System.out.println("Error al mostrar la tabla de productos");
        }
    }

    private void agregarBotonesInfo() {
        cantP.setToolTipText("Cuántas unidades de compra recibes en esta entrega");
        unidadCompraP.setToolTipText("Cómo viene el producto del proveedor (ej: Caja, Bolsa…)");
        factorP.setToolTipText("Unidades de venta que contiene una unidad de compra (ej: 24 piezas por caja)");
        unidadVentaP.setToolTipText("Cómo vendes el producto al cliente (ej: Pieza, Botella…)");

        java.awt.GridBagConstraints gbc = new java.awt.GridBagConstraints();
        gbc.anchor = java.awt.GridBagConstraints.WEST;

        gbc.gridx = 4; gbc.gridy = 3;
        gbc.insets = new java.awt.Insets(6, 6, 6, 10);
        jPanel5.add(crearBtnInfo(
            "Cantidad y Unidad de compra",
            "<html><b>Cantidad:</b> Cuántas unidades de compra recibes en esta entrega.<br>"
            + "Ejemplo: si te llegan 5 cajas, escribe <b>5</b>.<br><br>"
            + "<b>Unidad de compra:</b> Cómo viene el producto del proveedor.<br>"
            + "Ejemplo: <b>Caja</b>, Bolsa, Bulto, Botella…</html>"
        ), gbc);

        gbc = new java.awt.GridBagConstraints();
        gbc.anchor = java.awt.GridBagConstraints.WEST;
        gbc.gridx = 4; gbc.gridy = 4;
        gbc.insets = new java.awt.Insets(6, 6, 6, 10);
        jPanel5.add(crearBtnInfo(
            "Factor y Unidad de venta",
            "<html><b>Factor:</b> Cuántas unidades de venta contiene una unidad de compra.<br>"
            + "Ejemplo: si compras una <b>Caja</b> con 24 <b>Piezas</b> adentro, el factor es <b>24</b>.<br>"
            + "Si compras y vendes en la misma unidad (pieza por pieza), el factor es <b>1</b>.<br><br>"
            + "<b>Unidad de venta:</b> Cómo vendes el producto al cliente.<br>"
            + "Ejemplo: Pieza, Botella, Lata…<br><br>"
            + "<i>Inventario que se agrega = Cantidad × Factor</i></html>"
        ), gbc);
    }

    private javax.swing.JButton crearBtnInfo(String titulo, String mensaje) {
        javax.swing.JButton btn = new javax.swing.JButton("?");
        btn.setFont(new java.awt.Font("Noto Serif", java.awt.Font.BOLD, 15));
        btn.setForeground(new java.awt.Color(78, 150, 150));
        btn.setBackground(new java.awt.Color(220, 242, 242));
        btn.setPreferredSize(new java.awt.Dimension(32, 32));
        btn.setMinimumSize(new java.awt.Dimension(32, 32));
        btn.setMargin(new java.awt.Insets(0, 0, 0, 0));
        btn.setFocusPainted(false);
        btn.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        btn.setToolTipText("Haz clic para ver más información");
        btn.addActionListener(e -> javax.swing.JOptionPane.showMessageDialog(
            prodComDialog, mensaje, titulo, javax.swing.JOptionPane.INFORMATION_MESSAGE));
        return btn;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton acP;
    private javax.swing.JButton actCompra;
    private javax.swing.JComboBox<String> cmbOrdenCompras;
    private javax.swing.JButton agP;
    private javax.swing.JButton agreCompra;
    private javax.swing.JButton buscarP;
    private javax.swing.JFormattedTextField cantP;
    private javax.swing.JComboBox<String> categoriaCombo;
    private javax.swing.JFormattedTextField codBarrasP;
    private javax.swing.JFormattedTextField codP;
    private javax.swing.JDialog comDialog;
    private javax.swing.JFormattedTextField conceptoP;
    private javax.swing.JButton elP;
    private javax.swing.JLabel estadoP;
    private javax.swing.JFormattedTextField factorP;
    private javax.swing.JFormattedTextField fechaFactF;
    private javax.swing.JFormattedTextField folioF;
    private javax.swing.JButton guardarFacturaBtn;
    private javax.swing.JButton heP;
    private javax.swing.JButton hechoB1;
    private javax.swing.JCheckBox ivaP;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JLabel jLabelCategoria;
    private javax.swing.JLabel jLabelCodBarras;
    private javax.swing.JLabel jLabelConcepto;
    private javax.swing.JLabel jLabelFactor;
    private javax.swing.JLabel jLabelMargen;
    private javax.swing.JLabel jLabelMaxDescuento;
    private javax.swing.JLabel jLabelPMayoreo;
    private javax.swing.JLabel jLabelPMenudeo;
    private javax.swing.JLabel jLabelProv;
    private javax.swing.JLabel jLabelUnidadCompra;
    private javax.swing.JLabel jLabelUnidadVenta;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JLabel lblCuadre;
    private javax.swing.JLabel lblFechaFact;
    private javax.swing.JLabel lblFolio;
    private javax.swing.JLabel lblIva;
    private javax.swing.JLabel lblOrdenCompras;
    private javax.swing.JLabel lblOrigen;
    private javax.swing.JLabel lblRfc;
    private javax.swing.JLabel lblSubtotal;
    private javax.swing.JLabel lblTotal;
    private javax.swing.JLabel lblTotalFactura;
    private javax.swing.JFormattedTextField margenP;
    private javax.swing.JFormattedTextField maxDescuentoP;
    private javax.swing.JButton nuevoProvBtn;
    private javax.swing.JFormattedTextField origenF;
    private javax.swing.JPanel panelRegCompra;
    private javax.swing.JPanel panelRegProdC;
    private javax.swing.JPanel panelTotales;
    private javax.swing.JFormattedTextField precP;
    private javax.swing.JFrame prodComDialog;
    private javax.swing.JComboBox<String> proveedorCombo;
    private javax.swing.JFormattedTextField pMayoreoP;
    private javax.swing.JFormattedTextField pMenudeoP;
    private javax.swing.JTextPane rasF;
    private javax.swing.JFormattedTextField totalFacturaF;
    private javax.swing.JComboBox<String> unidadCompraP;
    private javax.swing.JComboBox<String> unidadVentaP;
    private javax.swing.JLabel valRfc;
    private javax.swing.JTable tablaCompras;
    private javax.swing.JTable tablaProd;
    private javax.swing.JTable tablaProdCom;
    // End of variables declaration//GEN-END:variables
}
