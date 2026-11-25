package puntoventa.ui;

import puntoventa.db.*;
import puntoventa.report.*;
import puntoventa.util.*;
import puntoventa.web.*;


import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */

/**
 *
 * @author 76905
 */
public class VentasP extends javax.swing.JPanel {

    String id_cliente="";

    static Double precT = 0.0;
    DefaultTableModel modelo = new DefaultTableModel();
    DefaultTableModel modeloBusc = new DefaultTableModel();
    DefaultTableModel modeloCli = new DefaultTableModel();
    DefaultTableModel modeloProdP = new DefaultTableModel();

    ConexionBD conect = new ConexionBD();
    String idTemV = "";

    // Campos de descuento (manejados fuera del form editor)
    private javax.swing.JLabel jLabelDctProd = new javax.swing.JLabel("Desc. producto (%):");
    private javax.swing.JFormattedTextField dctProd = new javax.swing.JFormattedTextField();
    private javax.swing.JLabel jLabelDctGlobal = new javax.swing.JLabel("Desc. global (%):");
    private javax.swing.JFormattedTextField dctGlobal = new javax.swing.JFormattedTextField();
    private javax.swing.JLabel dctPActLabel = new javax.swing.JLabel("Descuento (%):");
    private javax.swing.JFormattedTextField dctPAct = new javax.swing.JFormattedTextField();

    // Selector de lista de precios (manejado fuera del form editor)
    private javax.swing.JLabel jLabelLista = new javax.swing.JLabel("Lista de precios activa:");
    private javax.swing.JComboBox<String> cmbListaPrecios = new javax.swing.JComboBox<>();
    private java.util.ArrayList<Integer> listaIds = new java.util.ArrayList<>();

    // Comandos rápidos de teclado (manejados fuera del form editor)
    private final javax.swing.JLabel avisoLabel = new javax.swing.JLabel(" ");
    private int cantidadPendiente = 1;        // cantidad de un "N*texto" mientras se elige en la búsqueda
    private Runnable alCambiarStock;          // Interfaz refresca el aviso de poco stock después de cobrar

    /**
     * Creates new form Ventas
     */
    public VentasP() {
        initComponents();

        // Limpiar residuos de sesiones anteriores
        conect.limpiarVentaTemp();

        // Reemplazar el modelo de tablaVentas para agregar columna "Dct%"
        DefaultTableModel nuevoModelo = new DefaultTableModel(
            new Object[][]{},
            new String[]{"Codigo", "Producto", "Cantidad", "Tipo de Venta", "Precio Dado", "Precio Total", "Dct%"}
        ) {
            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) { return false; }
        };
        tablaVentas.setModel(nuevoModelo);
        modelo = nuevoModelo;

        modeloBusc = (DefaultTableModel)tablaBusc.getModel();
        modeloCli = (DefaultTableModel)tablaCli.getModel();
        modeloProdP = (DefaultTableModel)tablaProdPocos.getModel();
        totalLabel.setText("Total: " + precT);

        // --- Descuento por producto en jPanel2 (gridy=1, entre totalLabel y Código) ---
        java.awt.GridBagConstraints gbc = new java.awt.GridBagConstraints();
        jLabelDctProd.setFont(new java.awt.Font("Noto Serif", 1, 18));
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.ipadx = 16;
        gbc.anchor = java.awt.GridBagConstraints.WEST;
        gbc.insets = new java.awt.Insets(5, 10, 5, 10);
        jPanel2.add(jLabelDctProd, gbc);

        dctProd.setFont(new java.awt.Font("Noto Serif", 0, 18));
        dctProd.setPreferredSize(new java.awt.Dimension(150, 30));
        dctProd.setText("0");
        dctProd.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                char c = evt.getKeyChar();
                if (!Cake.numeros(c) && !Cake.inicioPunto(c)) evt.consume();
                if (Cake.tamaño(dctProd.getText(), 5)) evt.consume();
            }
        });
        gbc = new java.awt.GridBagConstraints();
        gbc.gridx = 1; gbc.gridy = 1;
        gbc.anchor = java.awt.GridBagConstraints.WEST;
        gbc.insets = new java.awt.Insets(5, 10, 5, 10);
        jPanel2.add(dctProd, gbc);

        // --- Descuento global en jPanel2 (gridy=8, después de ventaReali) ---
        jLabelDctGlobal.setFont(new java.awt.Font("Noto Serif", 1, 18));
        gbc = new java.awt.GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 8;
        gbc.ipadx = 16;
        gbc.anchor = java.awt.GridBagConstraints.WEST;
        gbc.insets = new java.awt.Insets(10, 10, 5, 10);
        jPanel2.add(jLabelDctGlobal, gbc);

        dctGlobal.setFont(new java.awt.Font("Noto Serif", 0, 18));
        dctGlobal.setPreferredSize(new java.awt.Dimension(150, 30));
        dctGlobal.setText("0");
        dctGlobal.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                char c = evt.getKeyChar();
                if (!Cake.numeros(c) && !Cake.inicioPunto(c)) evt.consume();
                if (Cake.tamaño(dctGlobal.getText(), 5)) evt.consume();
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                aplicarDescuentoGlobal();
            }
        });
        gbc = new java.awt.GridBagConstraints();
        gbc.gridx = 1; gbc.gridy = 8;
        gbc.anchor = java.awt.GridBagConstraints.WEST;
        gbc.insets = new java.awt.Insets(10, 10, 5, 10);
        jPanel2.add(dctGlobal, gbc);

        // --- Descuento en panelAct (diálogo de actualizar item) ---
        // Reordenar: remover hechoAct, agregar campos de descuento, re-agregar hechoAct
        panelAct.remove(hechoAct);

        dctPActLabel.setFont(new java.awt.Font("Noto Serif", 1, 18));
        dctPActLabel.setForeground(new java.awt.Color(78, 150, 150));
        dctPActLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        panelAct.add(dctPActLabel);

        dctPAct.setFont(new java.awt.Font("Noto Serif", 1, 18));
        dctPAct.setPreferredSize(new java.awt.Dimension(200, 35));
        dctPAct.setText("0");
        dctPAct.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                char c = evt.getKeyChar();
                if (!Cake.numeros(c) && !Cake.inicioPunto(c)) evt.consume();
                if (Cake.tamaño(dctPAct.getText(), 5)) evt.consume();
            }
        });
        panelAct.add(dctPAct);

        panelAct.add(hechoAct);

        // --- Lista de precios en jPanel2 (gridy=9, después de descuento global) ---
        java.awt.GridBagConstraints gbcLista = new java.awt.GridBagConstraints();
        jLabelLista.setFont(new java.awt.Font("Noto Serif", 1, 18));
        gbcLista.gridx = 0; gbcLista.gridy = 9;
        gbcLista.ipadx = 16;
        gbcLista.anchor = java.awt.GridBagConstraints.WEST;
        gbcLista.insets = new java.awt.Insets(10, 10, 5, 10);
        jPanel2.add(jLabelLista, gbcLista);

        cmbListaPrecios.setFont(new java.awt.Font("Noto Serif", 0, 16));
        cmbListaPrecios.setPreferredSize(new java.awt.Dimension(150, 30));
        gbcLista = new java.awt.GridBagConstraints();
        gbcLista.gridx = 1; gbcLista.gridy = 9;
        gbcLista.anchor = java.awt.GridBagConstraints.WEST;
        gbcLista.insets = new java.awt.Insets(10, 10, 5, 10);
        jPanel2.add(cmbListaPrecios, gbcLista);

        cargarListasPrecios();

        configurarComandosRapidos();
        enfocarCodigo();
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

        ventaDialog = new javax.swing.JDialog();
        panelAct = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        codp = new javax.swing.JFormattedTextField();
        jLabel7 = new javax.swing.JLabel();
        cantP = new javax.swing.JFormattedTextField();
        hechoAct = new javax.swing.JButton();
        panelCamb = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        totalCT = new javax.swing.JFormattedTextField();
        jLabel6 = new javax.swing.JLabel();
        recCT = new javax.swing.JFormattedTextField();
        jLabel8 = new javax.swing.JLabel();
        cambCT = new javax.swing.JFormattedTextField();
        jLabelFormaPago = new javax.swing.JLabel();
        cmbFormaPago = new javax.swing.JComboBox<>();
        cancelarB = new javax.swing.JButton();
        hechoCamb = new javax.swing.JButton();
        regVentaDialog = new javax.swing.JDialog();
        panelRegVenta = new javax.swing.JPanel();
        jLabel9 = new javax.swing.JLabel();
        curpCliente = new javax.swing.JFormattedTextField();
        labelinc = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        tablaCli = new javax.swing.JTable();
        Reg = new javax.swing.JButton();
        prodPocosDialog = new javax.swing.JDialog();
        panelProdPoc = new javax.swing.JPanel();
        labelinc1 = new javax.swing.JLabel();
        jScrollPane4 = new javax.swing.JScrollPane();
        tablaProdPocos = new javax.swing.JTable();
        hechoAct1 = new javax.swing.JButton();
        jLabel11 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        codigoProd = new javax.swing.JFormattedTextField();
        cantidadProd = new javax.swing.JFormattedTextField();
        jLabel4 = new javax.swing.JLabel();
        totalLabel = new javax.swing.JLabel();
        reg = new javax.swing.JButton();
        ventaReali = new javax.swing.JButton();
        elim = new javax.swing.JButton();
        actB = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        pnlBuscar = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tablaVentas = new javax.swing.JTable();
        jLabel10 = new javax.swing.JLabel();
        Busc = new javax.swing.JTextField();
        jScrollPane2 = new javax.swing.JScrollPane();
        tablaBusc = new javax.swing.JTable();

        ventaDialog.setTitle("Venta");
        ventaDialog.setAlwaysOnTop(true);
        ventaDialog.setMinimumSize(new java.awt.Dimension(400, 300));
        ventaDialog.setModal(true);
        ventaDialog.setResizable(false);
        ventaDialog.setSize(new java.awt.Dimension(400, 300));
        ventaDialog.getContentPane().setLayout(new java.awt.CardLayout());

        panelAct.setLayout(new java.awt.GridLayout(0, 1));

        jLabel2.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(78, 150, 150));
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setText("Código del Producto:");
        panelAct.add(jLabel2);

        codp.setEditable(false);
        codp.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        codp.setPreferredSize(new java.awt.Dimension(200, 35));
        panelAct.add(codp);

        jLabel7.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        jLabel7.setForeground(new java.awt.Color(78, 150, 150));
        jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel7.setText("Cantidad: ");
        panelAct.add(jLabel7);

        cantP.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        cantP.setPreferredSize(new java.awt.Dimension(200, 35));
        cantP.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                cantPKeyTyped(evt);
            }
        });
        panelAct.add(cantP);

        hechoAct.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        hechoAct.setForeground(new java.awt.Color(78, 150, 150));
        hechoAct.setText("Hecho");
        hechoAct.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                hechoActActionPerformed(evt);
            }
        });
        panelAct.add(hechoAct);
        hechoAct.setVisible(false);

        ventaDialog.getContentPane().add(panelAct, "card2");

        panelCamb.setLayout(new java.awt.GridLayout(5, 2));

        jLabel5.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(78, 150, 150));
        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel5.setText("Total: $");
        panelCamb.add(jLabel5);

        totalCT.setEditable(false);
        totalCT.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        totalCT.setPreferredSize(new java.awt.Dimension(200, 35));
        panelCamb.add(totalCT);

        jLabel6.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        jLabel6.setForeground(new java.awt.Color(78, 150, 150));
        jLabel6.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel6.setText("Recibido: $");
        panelCamb.add(jLabel6);

        recCT.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        recCT.setPreferredSize(new java.awt.Dimension(200, 35));
        recCT.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                recCTKeyReleased(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                recCTKeyTyped(evt);
            }
        });
        panelCamb.add(recCT);

        jLabel8.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        jLabel8.setForeground(new java.awt.Color(78, 150, 150));
        jLabel8.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel8.setText("Cambio: $");
        panelCamb.add(jLabel8);

        cambCT.setEditable(false);
        cambCT.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        cambCT.setPreferredSize(new java.awt.Dimension(200, 35));
        panelCamb.add(cambCT);

        jLabelFormaPago.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        jLabelFormaPago.setForeground(new java.awt.Color(78, 150, 150));
        jLabelFormaPago.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabelFormaPago.setText("Forma de Pago:");
        panelCamb.add(jLabelFormaPago);

        cmbFormaPago.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        cmbFormaPago.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Efectivo", "Transferencia", "Tarjeta", "Otro" }));
        cmbFormaPago.setPreferredSize(new java.awt.Dimension(200, 35));
        panelCamb.add(cmbFormaPago);

        cancelarB.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        cancelarB.setForeground(new java.awt.Color(78, 150, 150));
        cancelarB.setText("Cancelar");
        cancelarB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelarBActionPerformed(evt);
            }
        });
        panelCamb.add(cancelarB);
        hechoAct.setVisible(false);

        hechoCamb.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        hechoCamb.setForeground(new java.awt.Color(78, 150, 150));
        hechoCamb.setText("Hecho");
        hechoCamb.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                hechoCambActionPerformed(evt);
            }
        });
        panelCamb.add(hechoCamb);
        hechoAct.setVisible(false);

        ventaDialog.getContentPane().add(panelCamb, "card3");

        ventaDialog.setLocationRelativeTo(null);

        regVentaDialog.setTitle("Venta");
        regVentaDialog.setMinimumSize(new java.awt.Dimension(500, 500));
        regVentaDialog.setModal(true);
        regVentaDialog.setResizable(false);
        regVentaDialog.setSize(new java.awt.Dimension(500, 500));
        regVentaDialog.getContentPane().setLayout(new java.awt.CardLayout());

        panelRegVenta.setLayout(new java.awt.GridBagLayout());

        jLabel9.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        jLabel9.setForeground(new java.awt.Color(78, 150, 150));
        jLabel9.setText("CURP cliente:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.ipadx = 16;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        panelRegVenta.add(jLabel9, gridBagConstraints);

        curpCliente.setFont(new java.awt.Font("Noto Serif", 0, 18)); // NOI18N
        curpCliente.setPreferredSize(new java.awt.Dimension(250, 30));
        curpCliente.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                curpClienteKeyReleased(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                curpClienteKeyTyped(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        panelRegVenta.add(curpCliente, gridBagConstraints);

        labelinc.setFont(new java.awt.Font("Noto Serif", 0, 12)); // NOI18N
        labelinc.setForeground(new java.awt.Color(204, 0, 51));
        labelinc.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        labelinc.setText(" ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        panelRegVenta.add(labelinc, gridBagConstraints);

        jScrollPane3.setPreferredSize(new java.awt.Dimension(450, 300));

        tablaCli.setFont(new java.awt.Font("Noto Serif", 0, 14)); // NOI18N
        tablaCli.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Curp", "Nombre"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tablaCli.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tablaCliMouseClicked(evt);
            }
        });
        jScrollPane3.setViewportView(tablaCli);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        panelRegVenta.add(jScrollPane3, gridBagConstraints);

        Reg.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        Reg.setBackground(new java.awt.Color(153, 204, 255));
        Reg.setIcon(SvgIcon.load("/icons/add-user.svg", SvgIcon.MEDIUM));
        Reg.setText("Registrar nuevo cliente");
        Reg.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                RegActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        panelRegVenta.add(Reg, gridBagConstraints);

        regVentaDialog.getContentPane().add(panelRegVenta, "card2");

        regVentaDialog.setLocationRelativeTo(null);

        prodPocosDialog.setTitle("Alerta");
        prodPocosDialog.setAlwaysOnTop(true);
        prodPocosDialog.setMinimumSize(new java.awt.Dimension(550, 450));
        prodPocosDialog.setModal(true);
        prodPocosDialog.setResizable(false);
        prodPocosDialog.setSize(new java.awt.Dimension(550, 450));
        prodPocosDialog.getContentPane().setLayout(new java.awt.CardLayout());

        panelProdPoc.setLayout(new java.awt.GridBagLayout());

        labelinc1.setFont(new java.awt.Font("Noto Serif", 0, 12)); // NOI18N
        labelinc1.setForeground(new java.awt.Color(204, 0, 51));
        labelinc1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        labelinc1.setText(" ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        panelProdPoc.add(labelinc1, gridBagConstraints);

        jScrollPane4.setPreferredSize(new java.awt.Dimension(500, 300));

        tablaProdPocos.setFont(new java.awt.Font("Noto Serif", 0, 14)); // NOI18N
        tablaProdPocos.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Producto", "Nombre", "Cantidad"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane4.setViewportView(tablaProdPocos);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        panelProdPoc.add(jScrollPane4, gridBagConstraints);

        hechoAct1.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        hechoAct1.setForeground(new java.awt.Color(78, 150, 150));
        hechoAct1.setText("Aceptar");
        hechoAct1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                hechoAct1ActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 10);
        panelProdPoc.add(hechoAct1, gridBagConstraints);
        hechoAct.setVisible(false);

        jLabel11.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        jLabel11.setForeground(new java.awt.Color(78, 150, 150));
        jLabel11.setText("Quedan pocos productos en el inventario:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.ipadx = 16;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 10, 10);
        panelProdPoc.add(jLabel11, gridBagConstraints);

        prodPocosDialog.getContentPane().add(panelProdPoc, "card2");

        prodPocosDialog.setLocationRelativeTo(null);

        setLayout(new java.awt.BorderLayout());

        jLabel1.setFont(new java.awt.Font("Noto Serif", 1, 36)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(78, 150, 150));
        jLabel1.setIcon(SvgIcon.load("/icons/ventas.svg", SvgIcon.LARGE));
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("Ventas");
        jPanel1.add(jLabel1);

        add(jPanel1, java.awt.BorderLayout.NORTH);

        jPanel2.setLayout(new java.awt.GridBagLayout());

        jLabel3.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        jLabel3.setText("Código:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.ipadx = 16;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        jPanel2.add(jLabel3, gridBagConstraints);

        codigoProd.setFont(new java.awt.Font("Noto Serif", 0, 18)); // NOI18N
        codigoProd.setPreferredSize(new java.awt.Dimension(150, 30));
        codigoProd.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                codigoProdKeyTyped(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        jPanel2.add(codigoProd, gridBagConstraints);

        cantidadProd.setFont(new java.awt.Font("Noto Serif", 0, 18)); // NOI18N
        cantidadProd.setPreferredSize(new java.awt.Dimension(150, 30));
        cantidadProd.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                cantidadProdKeyTyped(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        jPanel2.add(cantidadProd, gridBagConstraints);

        jLabel4.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        jLabel4.setText("Cantidad:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.ipadx = 16;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        jPanel2.add(jLabel4, gridBagConstraints);

        totalLabel.setFont(new java.awt.Font("Noto Serif", 1, 40)); // NOI18N
        totalLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        totalLabel.setText("Total: ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        jPanel2.add(totalLabel, gridBagConstraints);

        reg.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        reg.setText("Registrar");
        reg.setIcon(SvgIcon.load("/icons/add-task.svg", SvgIcon.MEDIUM));
        reg.setPreferredSize(new java.awt.Dimension(170, 33));
        reg.setBackground(new java.awt.Color(125, 255, 177));
        reg.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                regActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        jPanel2.add(reg, gridBagConstraints);

        ventaReali.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        ventaReali.setText("Venta Hecha");
        ventaReali.setIcon(SvgIcon.load("/icons/done.svg", SvgIcon.MEDIUM));
        ventaReali.setPreferredSize(new java.awt.Dimension(170, 33));
        ventaReali.setBackground(new java.awt.Color(204, 226, 249));
        ventaReali.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ventaRealiActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTH;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        jPanel2.add(ventaReali, gridBagConstraints);

        elim.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        elim.setText("Eliminar");
        elim.setIcon(SvgIcon.load("/icons/delete.svg", SvgIcon.MEDIUM));
        elim.setPreferredSize(new java.awt.Dimension(170, 33));
        elim.setBackground(new java.awt.Color(252, 149, 149));
        elim.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                elimActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        jPanel2.add(elim, gridBagConstraints);

        actB.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        actB.setText("Actualizar");
        actB.setIcon(SvgIcon.load("/icons/edit.svg", SvgIcon.MEDIUM));
        actB.setPreferredSize(new java.awt.Dimension(170, 33));
        actB.setBackground(new java.awt.Color(255, 251, 128));
        actB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                actBActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        jPanel2.add(actB, gridBagConstraints);

        add(jPanel2, java.awt.BorderLayout.LINE_END);

        jPanel3.setLayout(new javax.swing.BoxLayout(jPanel3, javax.swing.BoxLayout.Y_AXIS));

        jScrollPane1.setPreferredSize(new java.awt.Dimension(540, 402));

        tablaVentas.setFont(new java.awt.Font("Noto Serif", 0, 14)); // NOI18N
        tablaVentas.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Codigo", "Producto", "Cantidad", "Tipo de Venta", "Precio Dado", "Precio Total"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane1.setViewportView(tablaVentas);

        jPanel3.add(jScrollPane1);

        pnlBuscar.setMaximumSize(new java.awt.Dimension(2147483647, 60));
        pnlBuscar.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 10, 5));

        jLabel10.setFont(new java.awt.Font("Noto Serif", 1, 36)); // NOI18N
        jLabel10.setForeground(new java.awt.Color(78, 150, 150));
        jLabel10.setIcon(SvgIcon.load("/icons/lupaTitulo.svg", 36));
        jLabel10.setText("Buscar");
        pnlBuscar.add(jLabel10);

        Busc.setMaximumSize(new java.awt.Dimension(300, 30));
        Busc.setMinimumSize(new java.awt.Dimension(150, 30));
        Busc.setPreferredSize(new java.awt.Dimension(300, 30));
        Busc.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                BuscKeyReleased(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                BuscKeyTyped(evt);
            }
        });
        pnlBuscar.add(Busc);

        jPanel3.add(pnlBuscar);

        jScrollPane2.setPreferredSize(new java.awt.Dimension(452, 200));

        tablaBusc.setFont(new java.awt.Font("Noto Serif", 0, 14)); // NOI18N
        tablaBusc.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Codigo", "Producto", "Precio Menudeo", "Precio Mayoreo", "En Existencia"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tablaBusc.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tablaBuscMouseClicked(evt);
            }
        });
        jScrollPane2.setViewportView(tablaBusc);

        jPanel3.add(jScrollPane2);

        add(jPanel3, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void cantPKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_cantPKeyTyped
        char letra = evt.getKeyChar();
        if(!Cake.numeros(letra)){
            evt.consume();
        }

        if(Cake.tamaño(cantP.getText(), 10)){
            evt.consume();
        }
    }//GEN-LAST:event_cantPKeyTyped

    private void hechoActActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hechoActActionPerformed
        // Errores y avisos van a la línea roja de Ventas, no a ventanas
        int cantidad = (int) leerNumero(cantP.getText());
        if (codp.getText().isEmpty() || cantidad < 1 || cantidad > CapturaVenta.CANTIDAD_MAXIMA) {
            ventaDialog.setVisible(false);
            mostrarAviso("Cantidad no válida: use un número de 1 a " + CapturaVenta.CANTIDAD_MAXIMA);
            enfocarCodigo();
            return;
        }
        String idProd = codp.getText();
        String aviso = null;
        double descuento = leerNumero(dctPAct.getText());
        double maxDesc = conect.obtenerMaxDescuento(idProd);
        if (descuento > maxDesc) {
            aviso = "El descuento máximo para este producto es " + maxDesc + "%. Se aplicó ese límite.";
            descuento = maxDesc;
        }
        ventaDialog.setVisible(false);
        String error = conect.insertarVentaTempSinDialogo(new String[]{idProd, String.valueOf(cantidad)}, false);
        if (error != null) {
            mostrarAviso(error);
            enfocarCodigo();
            return;
        }
        String errDct = conect.actualizarDescuentoTempSinDialogo(idProd, descuento);
        if (errDct != null) aviso = errDct;
        String avisoLista = aplicarPrecioLista(idProd);
        if (avisoLista != null) aviso = avisoLista;
        mostrarTablaVentaT();
        seleccionarRenglon(idProd);
        if (aviso != null) mostrarAviso(aviso);
        else limpiarAviso();
        enfocarCodigo();
    }//GEN-LAST:event_hechoActActionPerformed

    private void recCTKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_recCTKeyTyped
        char letra = evt.getKeyChar();
        if(!Cake.numeros(letra) && !Cake.inicioPunto(letra)){
            evt.consume();
        }
        
        if(!(recCT.getText().isEmpty())){
            if(Cake.hayPuntos(recCT.getText()) && letra=='.'){
                evt.consume();
            } else{
                if(Cake.punto(recCT.getText(), letra)){
                    evt.consume();
                }
            }
        } else{
            if(Cake.inicioPunto(letra)){
                evt.consume();
            }
        }
        
        if(Cake.tamaño(recCT.getText(), 10)){
            evt.consume();
        }
    }//GEN-LAST:event_recCTKeyTyped

    private void hechoCambActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hechoCambActionPerformed
        if(recCT.getText().isEmpty()){
            Mise.JOption("El campo no puede estar vacío", "Error", JOptionPane.ERROR_MESSAGE);
        } else {
            Double a = Double.valueOf(recCT.getText());
            Double b = Double.valueOf(totalCT.getText());
            if(a < b){
                Mise.JOption("Lo recibido no debe ser menor que el total", "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                // Guardar descuentos del carrito antes de registrar la venta
                java.util.Map<String, Double> descuentos = new java.util.HashMap<>();
                for (int i = 0; i < modelo.getRowCount(); i++) {
                    String idProd = "" + modelo.getValueAt(i, 0);
                    Object dctObj = modelo.getValueAt(i, 6);
                    double dct = (dctObj != null && !dctObj.toString().isEmpty()) ? Double.parseDouble(dctObj.toString()) : 0.0;
                    if (dct > 0) descuentos.put(idProd, dct);
                }

                String formaPago = (String) cmbFormaPago.getSelectedItem();
                idTemV = conect.registrarVentaConFormaPago(Interfaz.idVendedor,  id_cliente, formaPago);
                if (idTemV == null || idTemV.isEmpty()) {
                    // La venta no se registró (el error ya se mostró): conservar el carrito
                    // para reintentar en lugar de vaciarlo y mostrar un ticket vacío.
                    return;
                }

                // Persistir descuentos en venta_detalle
                for (java.util.Map.Entry<String, Double> entry : descuentos.entrySet()) {
                    conect.actualizarDescuentoVenta(idTemV, entry.getKey(), entry.getValue());
                }

                conect.limpiarVentaTemp();
                Mise.limpiarTabla(modelo);
                precT = 0.0;
                totalLabel.setText("Total: " + precT);
                dctGlobal.setText("0");
                ventaDialog.setVisible(false);
                TicketData datosTicket = conect.obtenerDatosVenta(idTemV);
                datosTicket.recibido = recCT.getText().isEmpty() ? datosTicket.total : Double.valueOf(recCT.getText());
                datosTicket.cambio   = cambCT.getText().isEmpty() ? 0.0 : Double.valueOf(cambCT.getText());
                new TicketDialog(datosTicket, conect).setVisible(true);
                if(conect.hayPocosProductos()){
                    mostrarProdPocos();
                    prodPocosDialog.setVisible(true);
                }
            }
        }
    }//GEN-LAST:event_hechoCambActionPerformed

    private void cancelarBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelarBActionPerformed
        ventaDialog.setVisible(false);
    }//GEN-LAST:event_cancelarBActionPerformed

    private void recCTKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_recCTKeyReleased
        if(!recCT.getText().isEmpty()){
            cambCT.setText("" + cambioT(recCT.getText()));
        }
    }//GEN-LAST:event_recCTKeyReleased

    private void BuscKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BuscKeyTyped
        // Letras (con acentos y ñ), dígitos, espacio y guion; sin espacio al inicio
        char letra = evt.getKeyChar();
        boolean permitido = Character.isLetterOrDigit(letra) || letra == ' ' || letra == '-';
        if (!permitido || (letra == ' ' && Busc.getText().isEmpty())) {
            evt.consume();
        }
        int seleccion = Busc.getSelectionEnd() - Busc.getSelectionStart();
        if (Busc.getText().length() - seleccion >= CapturaVenta.LONGITUD_MAXIMA) {
            evt.consume();
        }
    }//GEN-LAST:event_BuscKeyTyped

    private void actBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_actBActionPerformed
        if(tablaVentas.getSelectedRow() != -1){
            int a = tablaVentas.getSelectedRow();
            hechoAct.setVisible(true);
            //MOSTRAR LOS DATOS EN EL FIELD
            mostrarDAct(a);
            panelAct.setVisible(true);
            panelCamb.setVisible(false);
            javax.swing.SwingUtilities.invokeLater(() -> { cantP.requestFocusInWindow(); cantP.selectAll(); });
            ventaDialog.setVisible(true);
            enfocarCodigo();
        } else{
            mostrarAviso("Seleccione un renglón con ↑/↓");
            enfocarCodigo();
        }
    }//GEN-LAST:event_actBActionPerformed

    private void elimActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_elimActionPerformed
        if (tablaVentas.getSelectedRow() != -1) {
            quitarSeleccionado();
        } else {
            mostrarAviso("Seleccione el renglón que desea quitar");
        }
        enfocarCodigo();
    }//GEN-LAST:event_elimActionPerformed

    private void ventaRealiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ventaRealiActionPerformed
        if(modelo.getRowCount() != 0){
            mostrarClientes("");
            regVentaDialog.setVisible(true);
        } else{
            Mise.JOption("No es posible registrar una venta sin productos", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_ventaRealiActionPerformed

    private void regActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_regActionPerformed
        // Mismo camino que Enter en el campo de código; si "Cantidad" tiene número, ese gana
        CapturaVenta c = CapturaVenta.interpretar(codigoProd.getText());
        if (c.tipo == CapturaVenta.Tipo.VACIO) {
            mostrarAviso("Escriba o escanee el código del producto");
            enfocarCodigo();
            return;
        }
        if (c.tipo == CapturaVenta.Tipo.INVALIDO) {
            mostrarAviso(c.mensaje);
            enfocarCodigo();
            return;
        }
        int cantidad = c.cantidad;
        String textoCant = cantidadProd.getText().trim();
        if (!textoCant.isEmpty()) {
            try { cantidad = Integer.parseInt(textoCant); } catch (NumberFormatException e) { cantidad = 0; }
            if (cantidad < 1 || cantidad > CapturaVenta.CANTIDAD_MAXIMA) {
                mostrarAviso("Cantidad no válida: use un número de 1 a " + CapturaVenta.CANTIDAD_MAXIMA);
                return;
            }
        }
        String idProd = resolverCodigo(c.texto);
        if (idProd == null) {
            if (c.soloDigitos()) {
                mostrarAviso("No existe un producto con el código " + c.texto);
                enfocarCodigo();
            } else {
                irABusqueda(c.texto, cantidad);
            }
            return;
        }
        if (agregarProducto(idProd, cantidad)) {
            codigoProd.setText("");
            cantidadProd.setText("");
            Busc.setText("");
            mostrarBusqueda("");
        }
        enfocarCodigo();
    }//GEN-LAST:event_regActionPerformed

    private void cantidadProdKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_cantidadProdKeyTyped
        // TODO add your handling code here:
        char letra = evt.getKeyChar();
        if(!Cake.numeros(letra)){
            evt.consume();
        }

        if(Cake.tamaño(cantidadProd.getText(), 10)){
            evt.consume();
        }
    }//GEN-LAST:event_cantidadProdKeyTyped

    private void codigoProdKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_codigoProdKeyTyped
        // Acepta código, código de barras, "N*código" o texto para buscar
        char letra = evt.getKeyChar();
        int seleccion = codigoProd.getSelectionEnd() - codigoProd.getSelectionStart();
        if (Character.isISOControl(letra)
                || codigoProd.getText().length() - seleccion >= CapturaVenta.LONGITUD_MAXIMA) {
            evt.consume();
        }
    }//GEN-LAST:event_codigoProdKeyTyped

    private void BuscKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BuscKeyReleased
        String palabra = Busc.getText();
        Mise.limpiarTabla(modeloBusc);
        mostrarBusqueda(palabra);
    }//GEN-LAST:event_BuscKeyReleased

    private void tablaBuscMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tablaBuscMouseClicked
        if(tablaBusc.getSelectedRow() != -1){
            codigoProd.setText((String)tablaBusc.getValueAt(tablaBusc.getSelectedRow(), 0));
            cantidadProd.setText("1");
        }
    }//GEN-LAST:event_tablaBuscMouseClicked

    private void tablaCliMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tablaCliMouseClicked
        id_cliente = "" + tablaCli.getValueAt(tablaCli.getSelectedRow(), 0);
        precT = conect.sumaVentaTemp();
        recCT.setText("");
        totalCT.setText("" + precT);
        cambCT.setText("0.0");
        panelAct.setVisible(false);
        panelCamb.setVisible(true);
        regVentaDialog.setVisible(false);
        ventaDialog.setVisible(true);
    }//GEN-LAST:event_tablaCliMouseClicked

    private void curpClienteKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_curpClienteKeyTyped
        char letra = evt.getKeyChar();
        if(!Cake.letrasMayus(letra) && !Cake.numeros(letra)){
            evt.consume();
        }

        if(Cake.tamaño(curpCliente.getText(), 18)){
            evt.consume();
        }
    }//GEN-LAST:event_curpClienteKeyTyped

    private void curpClienteKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_curpClienteKeyReleased
        String pal = curpCliente.getText();
        mostrarClientes(pal);
    }//GEN-LAST:event_curpClienteKeyReleased

    private void hechoAct1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hechoAct1ActionPerformed
        prodPocosDialog.setVisible(false);
    }//GEN-LAST:event_hechoAct1ActionPerformed
    
    private void RegActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_RegActionPerformed
        // TODO add your handling code here:
        ClientesP.Registro.setVisible(true);
    }//GEN-LAST:event_RegActionPerformed
    
    public Double cambioT(String palabra){
        totalCT.setText("" + precT);
        Double cambioNum = Double.valueOf(palabra);
        if(cambioNum >= precT){
            cambioNum = cambioNum-precT;
        } else {
            cambioNum = 0.0;
        }
        return cambioNum;
    }
    
    public final void mostrarTablaVentaT(){
        Mise.limpiarTabla(modelo);
        java.sql.ResultSet rs = conect.obtenerVentaTemp();
        try{
            while(rs.next()){
                double dct = 0.0;
                try { dct = rs.getDouble("descuento_pct"); } catch(java.sql.SQLException ex) { /* columna aún no existe */ }
                modelo.addRow(new Object[]{rs.getString("id_producto"), rs.getString("nombre_p"), rs.getInt("cantidad_prod"),
                    rs.getString("tipo_venta"), rs.getDouble("precio_dado"), rs.getDouble("precio_total"), dct});
            }
        }catch(java.sql.SQLException e){
            System.out.println("Error al tratar de llenar la tabla venta temporal");
        }
        precT = conect.sumaVentaTemp();
        totalLabel.setText("Total: " + precT);
    }
    
    public void mostrarBusqueda(String palabra){//Productos activos por nombre o código; palabra vacía = todos
        Mise.limpiarTabla(modeloBusc);
        java.sql.ResultSet rs = conect.buscarProductosActivos(palabra);
        try{
            while(rs.next()){
                modeloBusc.addRow( new Object[]{rs.getString("id_producto"), rs.getString("nombre"), rs.getDouble("precio_menudeo"), 
                    rs.getDouble("precio_mayoreo"), rs.getInt("cantidad")});
            }
        }catch(java.sql.SQLException e){
            System.out.println("Error en la tabla de busqueda de productos");
        }
    }
    
    public void mostrarClientes(String curp){//Clientes activos cuyo CURP contiene el texto; vacío = todos
        Mise.limpiarTabla(modeloCli);
        java.sql.ResultSet rs = conect.buscarClientesActivosPorId(curp);
        try{
            while(rs.next()){
                modeloCli.addRow( new Object[]{rs.getString("id_cliente"), rs.getString("nombre")});
            }
            // Notificar a la tabla que los datos han cambiado
            modeloCli.fireTableDataChanged();
        }catch(java.sql.SQLException e){
            System.out.println("Error al mostrar la tabla de clientes");
        }
    }
    
    public void mostrarProdPocos(){
        Mise.limpiarTabla(modeloProdP);
        java.sql.ResultSet rs = conect.obtenerProductosPocos();
        try{
            while(rs.next()){
                modeloProdP.addRow(new Object[]{rs.getString("id_producto"), rs.getString("nombre"), rs.getInt("cantidad")});
            }
        } catch(java.sql.SQLException e){
            System.out.println("Error al mostrar la tabla pocos productos");
        }
    }
    
    public void mostrarDAct(int a){
        String elemento = "" + modelo.getValueAt(a, 0);
        String canti = "" + modelo.getValueAt(a, 2);
        Object dctObj = modelo.getValueAt(a, 6);
        String dct = (dctObj != null) ? dctObj.toString() : "0";
        codp.setText(elemento);
        cantP.setText(canti);
        dctPAct.setText(dct);
    }

    private void cargarListasPrecios() {
        cmbListaPrecios.removeAllItems();
        listaIds.clear();
        java.sql.ResultSet rs = conect.obtenerListas();
        if (rs != null) {
            try {
                while (rs.next()) {
                    listaIds.add(rs.getInt("id_lista"));
                    cmbListaPrecios.addItem(rs.getString("nombre"));
                }
            } catch (java.sql.SQLException e) {
                System.out.println("Error al cargar listas de precios: " + e.getMessage());
            }
        }
        if (cmbListaPrecios.getItemCount() == 0) {
            listaIds.add(1);
            cmbListaPrecios.addItem("Menudeo");
        }
    }

    /** Aplica la lista de precios activa al renglón; regresa un aviso para la línea roja o null. */
    private String aplicarPrecioLista(String idProd) {
        if (listaIds.isEmpty()) return null;
        int idx = cmbListaPrecios.getSelectedIndex();
        if (idx < 0) return null;
        int idLista = listaIds.get(idx);
        String nombreLista = (String) cmbListaPrecios.getSelectedItem();
        double precioLista = conect.obtenerPrecioEnLista(idProd, idLista);
        if (precioLista < 0) {
            conect.actualizarPrecioListaTemp(idProd, 0, "Menudeo"); // vuelve al precio automático
            return "El producto no tiene precio en la lista \"" + nombreLista + "\". Se usó el precio de Menudeo.";
        }
        conect.actualizarPrecioListaTemp(idProd, precioLista, nombreLista);
        return null;
    }

    // ===================== COMANDOS RÁPIDOS DE TECLADO =====================

    private void configurarComandosRapidos() {
        javax.swing.JPanel sur = new javax.swing.JPanel(new java.awt.GridLayout(0, 1));
        sur.setBorder(javax.swing.BorderFactory.createEmptyBorder(4, 12, 6, 12));
        avisoLabel.setFont(new java.awt.Font("Noto Serif", java.awt.Font.BOLD, 20));
        avisoLabel.setForeground(new java.awt.Color(190, 30, 30));
        javax.swing.JLabel ayuda = new javax.swing.JLabel(
                "Enter Agregar · F2 Buscar · F4 Cantidad · Supr Quitar · +/- Cantidad · F12 COBRAR");
        ayuda.setFont(new java.awt.Font("Noto Serif", java.awt.Font.PLAIN, 16));
        sur.add(avisoLabel);
        sur.add(ayuda);
        add(sur, java.awt.BorderLayout.SOUTH);

        tecla(codigoProd, javax.swing.JComponent.WHEN_FOCUSED, "ENTER", "procesarCaptura", this::procesarCaptura);

        // Búsqueda sin mouse
        tecla(this, javax.swing.JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, "F2", "buscar", this::buscarDesdeCodigo);
        tecla(Busc, javax.swing.JComponent.WHEN_FOCUSED, "ENTER", "elegirBusqueda", () -> elegirDesdeBusqueda(true));
        tecla(Busc, javax.swing.JComponent.WHEN_FOCUSED, "DOWN", "bajarBusqueda", () -> elegirDesdeBusqueda(false));
        tecla(Busc, javax.swing.JComponent.WHEN_FOCUSED, "ESCAPE", "salirBusqueda", this::volverACodigo);
        // JTable trae su acción de Enter ("siguiente fila") en este InputMap: se reemplaza
        tecla(tablaBusc, javax.swing.JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, "ENTER", "agregarBusqueda",
                () -> agregarDesdeBusqueda(tablaBusc.getSelectedRow()));
        tecla(tablaBusc, javax.swing.JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, "ESCAPE", "salirTablaBusqueda", this::volverACodigo);

        // Editar el carrito con el campo de código vacío (con texto, las teclas escriben normal)
        tecla(codigoProd, javax.swing.JComponent.WHEN_FOCUSED, "UP", "renglonArriba", () -> { if (codigoVacio()) moverSeleccion(-1); });
        tecla(codigoProd, javax.swing.JComponent.WHEN_FOCUSED, "DOWN", "renglonAbajo", () -> { if (codigoVacio()) moverSeleccion(1); });
        javax.swing.Action borrarSiguiente = codigoProd.getActionMap().get(javax.swing.text.DefaultEditorKit.deleteNextCharAction);
        tecla(codigoProd, javax.swing.JComponent.WHEN_FOCUSED, "DELETE", "quitarRenglon", () -> {
            if (codigoVacio()) quitarSeleccionado();
            else if (borrarSiguiente != null) borrarSiguiente.actionPerformed(new java.awt.event.ActionEvent(codigoProd, 0, ""));
        });
        // "typed" cubre el + / - del teclado normal y del numérico (registrar también ADD/SUBTRACT lo haría dos veces)
        tecla(codigoProd, javax.swing.JComponent.WHEN_FOCUSED, "typed +", "masUno", () -> {
            if (codigoVacio()) cambiarCantidadSeleccionada(1); else codigoProd.replaceSelection("+");
        });
        tecla(codigoProd, javax.swing.JComponent.WHEN_FOCUSED, "typed -", "menosUno", () -> {
            if (codigoVacio()) cambiarCantidadSeleccionada(-1); else codigoProd.replaceSelection("-");
        });
        tecla(this, javax.swing.JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, "F4", "editarRenglon", () -> actBActionPerformed(null));

        // Diálogo de actualizar renglón (F4): Enter guarda, Esc cancela
        Runnable guardarRenglon = hechoAct::doClick;
        Runnable cancelarRenglon = () -> { ventaDialog.setVisible(false); enfocarCodigo(); };
        tecla(ventaDialog.getRootPane(), javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW, "ENTER", "guardarRenglon", guardarRenglon);
        tecla(ventaDialog.getRootPane(), javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW, "ESCAPE", "cancelarRenglon", cancelarRenglon);
        for (javax.swing.JComponent campo : new javax.swing.JComponent[]{cantP, dctPAct, codp}) {
            // los JFormattedTextField tienen su propio Enter; se reemplaza para que guarde
            tecla(campo, javax.swing.JComponent.WHEN_FOCUSED, "ENTER", "guardarRenglon", guardarRenglon);
        }
    }

    private boolean codigoVacio() {
        return codigoProd.getText().trim().isEmpty();
    }

    /** ↑/↓ sobre el carrito; sin selección, ↑ va al último renglón y ↓ al primero. */
    private void moverSeleccion(int paso) {
        int filas = modelo.getRowCount();
        if (filas == 0) return;
        int actual = tablaVentas.getSelectedRow();
        int nueva = actual < 0 ? (paso < 0 ? filas - 1 : 0) : Math.max(0, Math.min(filas - 1, actual + paso));
        tablaVentas.setRowSelectionInterval(nueva, nueva);
        tablaVentas.scrollRectToVisible(tablaVentas.getCellRect(nueva, 0, true));
    }

    /** +/-: suma o resta al renglón seleccionado; en 0 lo quita. Conserva su descuento. */
    void cambiarCantidadSeleccionada(int delta) {
        int fila = tablaVentas.getSelectedRow();
        if (fila < 0) return;
        String idProd = "" + modelo.getValueAt(fila, 0);
        int nueva = (int) leerNumero("" + modelo.getValueAt(fila, 2)) + delta;
        if (nueva <= 0) {
            quitarSeleccionado();
            return;
        }
        if (nueva > CapturaVenta.CANTIDAD_MAXIMA) {
            mostrarAviso("La cantidad máxima por renglón es " + CapturaVenta.CANTIDAD_MAXIMA);
            return;
        }
        double dct = leerNumero("" + modelo.getValueAt(fila, 6));
        String error = conect.insertarVentaTempSinDialogo(new String[]{idProd, String.valueOf(nueva)}, false);
        if (error != null) {
            mostrarAviso(error);
            return;
        }
        String aviso = dct > 0 ? conect.actualizarDescuentoTempSinDialogo(idProd, dct) : null;
        String avisoLista = aplicarPrecioLista(idProd);
        if (avisoLista != null) aviso = avisoLista;
        mostrarTablaVentaT();
        seleccionarRenglon(idProd);
        if (aviso != null) mostrarAviso(aviso);
        else limpiarAviso();
    }

    /** Supr: quita el renglón seleccionado y deja seleccionado el siguiente. */
    void quitarSeleccionado() {
        int fila = tablaVentas.getSelectedRow();
        if (fila < 0) return;
        conect.eliminarVentaTemp("" + modelo.getValueAt(fila, 0));
        mostrarTablaVentaT();
        int filas = modelo.getRowCount();
        if (filas > 0) {
            int sel = Math.min(fila, filas - 1);
            tablaVentas.setRowSelectionInterval(sel, sel);
        }
        limpiarAviso();
    }

    /** Registra una tecla (formato de KeyStroke.getKeyStroke(String)) con su acción. */
    private static void tecla(javax.swing.JComponent c, int condicion, String teclaTexto, String nombre, Runnable accion) {
        c.getInputMap(condicion).put(javax.swing.KeyStroke.getKeyStroke(teclaTexto), nombre);
        c.getActionMap().put(nombre, new javax.swing.AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) { accion.run(); }
        });
    }

    /** Enter en el campo de código: agrega, busca, avisa o (vacío) cobra. */
    private void procesarCaptura() {
        CapturaVenta c = CapturaVenta.interpretar(codigoProd.getText());
        if (c.tipo == CapturaVenta.Tipo.VACIO) {
            abrirCobro();
            return;
        }
        if (c.tipo == CapturaVenta.Tipo.INVALIDO) {
            mostrarAviso(c.mensaje);
            codigoProd.selectAll();
            return;
        }
        String idProd = resolverCodigo(c.texto);
        if (idProd != null) {
            if (agregarProducto(idProd, c.cantidad)) codigoProd.setText("");
            else codigoProd.selectAll();
        } else if (c.soloDigitos()) {
            mostrarAviso("No existe un producto con el código " + c.texto);
            codigoProd.selectAll();
        } else {
            irABusqueda(c.texto, c.cantidad);
        }
    }

    /** id del producto por id o código de barras; reintenta en mayúsculas. null si no existe. */
    private String resolverCodigo(String texto) {
        String id = conect.resolverCodigo(texto);
        String mayus = texto.toUpperCase();
        if (id == null && !mayus.equals(texto)) id = conect.resolverCodigo(mayus);
        return id;
    }

    /**
     * Agrega (acumulando) el producto al carrito. Errores y avisos van a la línea roja,
     * nunca a una ventana. Regresa true si el producto quedó en el carrito.
     */
    boolean agregarProducto(String idProd, int cantidad) {
        double dctExistente = descuentoEnCarrito(idProd);
        String error = conect.insertarVentaTempSinDialogo(new String[]{idProd, String.valueOf(cantidad)}, true);
        if (error != null) {
            mostrarAviso(error);
            return false;
        }
        String aviso = null;
        double descuento = leerNumero(dctProd.getText());
        if (descuento > 0) {
            double maxDesc = conect.obtenerMaxDescuento(idProd);
            if (descuento > maxDesc) {
                aviso = "El descuento máximo para este producto es " + maxDesc + "%. Se aplicó ese límite.";
                descuento = maxDesc;
            }
            dctProd.setText("0");
        } else {
            descuento = dctExistente; // volver a escanear no le quita el descuento que ya tenía
        }
        if (descuento > 0) {
            String errDct = conect.actualizarDescuentoTempSinDialogo(idProd, descuento);
            if (errDct != null) aviso = errDct;
        }
        String avisoLista = aplicarPrecioLista(idProd);
        if (avisoLista != null) aviso = avisoLista;
        mostrarTablaVentaT();
        seleccionarRenglon(idProd);
        if (aviso != null) mostrarAviso(aviso);
        else limpiarAviso();
        return true;
    }

    /** Descuento (%) que ya tiene el producto en el carrito; 0 si no está. */
    private double descuentoEnCarrito(String idProd) {
        for (int i = 0; i < modelo.getRowCount(); i++) {
            if (idProd.equals("" + modelo.getValueAt(i, 0))) {
                Object d = modelo.getValueAt(i, 6);
                return d == null ? 0.0 : leerNumero(d.toString());
            }
        }
        return 0.0;
    }

    private static double leerNumero(String texto) {
        try { return texto == null || texto.trim().isEmpty() ? 0.0 : Double.parseDouble(texto.trim()); }
        catch (NumberFormatException e) { return 0.0; }
    }

    /** Muestra el aviso en rojo y suena el error (en lugar de una ventana que el lector cerraría). */
    void mostrarAviso(String mensaje) {
        avisoLabel.setText(mensaje == null || mensaje.isEmpty() ? " " : mensaje);
        java.awt.Toolkit.getDefaultToolkit().beep();
    }

    void limpiarAviso() {
        avisoLabel.setText(" ");
    }

    void seleccionarRenglon(String idProd) {
        for (int i = 0; i < modelo.getRowCount(); i++) {
            if (idProd.equals("" + modelo.getValueAt(i, 0))) {
                tablaVentas.setRowSelectionInterval(i, i);
                tablaVentas.scrollRectToVisible(tablaVentas.getCellRect(i, 0, true));
                return;
            }
        }
    }

    public void enfocarCodigo() {
        javax.swing.SwingUtilities.invokeLater(codigoProd::requestFocusInWindow);
    }

    /** Pasa el texto a la búsqueda de productos y guarda la cantidad para cuando elijan uno. */
    private void irABusqueda(String texto, int cantidad) {
        cantidadPendiente = cantidad;
        Busc.setText(texto);
        mostrarBusqueda(texto);
        limpiarAviso();
        Busc.requestFocusInWindow();
    }

    /** F2: abre la búsqueda con lo que haya en el campo de código (respeta un "N*texto"). */
    private void buscarDesdeCodigo() {
        CapturaVenta c = CapturaVenta.interpretar(codigoProd.getText());
        if (c.tipo == CapturaVenta.Tipo.PRODUCTO) irABusqueda(c.texto, c.cantidad);
        else irABusqueda("", 1);
    }

    /** Enter (o ↓) en el campo de búsqueda. */
    private void elegirDesdeBusqueda(boolean agregarSiEsUnico) {
        int filas = modeloBusc.getRowCount();
        if (filas == 0) {
            mostrarAviso("No se encontraron productos con \"" + Busc.getText().trim() + "\"");
            return;
        }
        if (filas == 1 && agregarSiEsUnico) {
            agregarDesdeBusqueda(0);
            return;
        }
        tablaBusc.setRowSelectionInterval(0, 0);
        tablaBusc.scrollRectToVisible(tablaBusc.getCellRect(0, 0, true));
        tablaBusc.requestFocusInWindow();
    }

    private void agregarDesdeBusqueda(int fila) {
        if (fila < 0 || fila >= modeloBusc.getRowCount()) return;
        String idProd = "" + modeloBusc.getValueAt(fila, 0);
        if (agregarProducto(idProd, cantidadPendiente)) {
            codigoProd.setText("");
            volverACodigo();
        }
    }

    private void volverACodigo() {
        Busc.setText("");
        mostrarBusqueda("");
        cantidadPendiente = 1;
        enfocarCodigo();
    }

    /** Abre el cobro (Task 6 lo completa). */
    private void abrirCobro() {
    }

    private void aplicarDescuentoGlobal() {
        if (modelo.getRowCount() == 0) return;
        String texto = dctGlobal.getText();
        if (texto.isEmpty()) return;
        double dctGlobalVal;
        try { dctGlobalVal = Double.parseDouble(texto); } catch (NumberFormatException e) { return; }
        if (dctGlobalVal < 0 || dctGlobalVal > 100) return;

        double minMax = conect.obtenerMinMaxDescuentoTemp();
        if (dctGlobalVal > minMax) {
            Mise.JOption("El descuento máximo permitido para los productos del carrito es " + minMax
                + "%.\nSe aplicará ese límite.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            dctGlobalVal = minMax;
            dctGlobal.setText("" + minMax);
        }

        for (int i = 0; i < modelo.getRowCount(); i++) {
            String idProd = "" + modelo.getValueAt(i, 0);
            double maxDescProd = conect.obtenerMaxDescuento(idProd);
            double dctAplicar = Math.min(dctGlobalVal, maxDescProd);
            conect.actualizarDescuentoTemp(idProd, dctAplicar);
        }
        mostrarTablaVentaT();
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField Busc;
    private javax.swing.JButton Reg;
    private javax.swing.JButton actB;
    private javax.swing.JFormattedTextField cambCT;
    private javax.swing.JButton cancelarB;
    private javax.swing.JComboBox<String> cmbFormaPago;
    private javax.swing.JFormattedTextField cantP;
    private javax.swing.JFormattedTextField cantidadProd;
    private javax.swing.JFormattedTextField codigoProd;
    private javax.swing.JFormattedTextField codp;
    private javax.swing.JFormattedTextField curpCliente;
    private javax.swing.JButton elim;
    private javax.swing.JButton hechoAct;
    private javax.swing.JButton hechoAct1;
    private javax.swing.JButton hechoCamb;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JLabel jLabelFormaPago;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel pnlBuscar;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    public static javax.swing.JLabel labelinc;
    public static javax.swing.JLabel labelinc1;
    private javax.swing.JPanel panelAct;
    private javax.swing.JPanel panelCamb;
    private javax.swing.JPanel panelProdPoc;
    private javax.swing.JPanel panelRegVenta;
    private javax.swing.JDialog prodPocosDialog;
    private javax.swing.JFormattedTextField recCT;
    private javax.swing.JButton reg;
    private javax.swing.JDialog regVentaDialog;
    private javax.swing.JTable tablaBusc;
    private javax.swing.JTable tablaCli;
    private javax.swing.JTable tablaProdPocos;
    private javax.swing.JTable tablaVentas;
    private javax.swing.JFormattedTextField totalCT;
    private javax.swing.JLabel totalLabel;
    private javax.swing.JDialog ventaDialog;
    private javax.swing.JButton ventaReali;
    // End of variables declaration//GEN-END:variables
}
