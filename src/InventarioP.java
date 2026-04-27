
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;
import javax.swing.table.DefaultTableModel;
import com.google.zxing.BarcodeFormat;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */

/**
 *
 * @author 76905
 */
public class InventarioP extends javax.swing.JPanel {

    static DefaultTableModel modelo = new DefaultTableModel();
    boolean aux = false;
    ConexionBD conect = new ConexionBD();

    // Categoría - IDs para mapeo en combos
    private java.util.ArrayList<Integer> categoriaIds = new java.util.ArrayList<>();
    private java.util.ArrayList<Integer> filtroCategoriaIds = new java.util.ArrayList<>();
    private boolean cargandoFiltro = false; // evita que el listener se dispare al poblar el combo

    // Campo max_descuento (manejado fuera del form editor)
    private javax.swing.JLabel jLabelMaxDesc = new javax.swing.JLabel("Desc. máximo (%):");
    private javax.swing.JFormattedTextField maxDescuento = new javax.swing.JFormattedTextField();

    // Tabla de precios por lista (manejada fuera del form editor)
    private javax.swing.JLabel jLabelListaPrecios = new javax.swing.JLabel("Precios por lista:");
    private javax.swing.JTable tblListaPrecios;
    private javax.swing.JScrollPane jScrollPaneListaPrecios;
    private java.util.ArrayList<Integer> precioListaIds = new java.util.ArrayList<>();

    /**
     * Creates new form Inventario
     */
    public InventarioP() {
        initComponents();
        modelo=(DefaultTableModel)tablaVentas.getModel();
        cargarFiltroCategorias();
        mostrarTabla("");

        // Agregar campo "Descuento máximo (%)" al diálogo de producto (gridy=8, empujando labelinc y botones)
        java.awt.GridBagConstraints gbcMaxDesc = new java.awt.GridBagConstraints();
        jLabelMaxDesc.setFont(new java.awt.Font("Noto Serif", 1, 18));
        jLabelMaxDesc.setForeground(new java.awt.Color(78, 150, 150));
        gbcMaxDesc.gridx = 0; gbcMaxDesc.gridy = 10;
        gbcMaxDesc.ipadx = 16;
        gbcMaxDesc.anchor = java.awt.GridBagConstraints.WEST;
        gbcMaxDesc.insets = new java.awt.Insets(10, 10, 10, 10);
        jDialog1.getContentPane().add(jLabelMaxDesc, gbcMaxDesc);

        maxDescuento.setFont(new java.awt.Font("Noto Serif", 1, 18));
        maxDescuento.setPreferredSize(new java.awt.Dimension(200, 35));
        maxDescuento.setText("100");
        maxDescuento.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                char c = evt.getKeyChar();
                if (!Cake.numeros(c) && !Cake.inicioPunto(c)) evt.consume();
                if (Cake.tamaño(maxDescuento.getText(), 5)) evt.consume();
            }
        });
        gbcMaxDesc = new java.awt.GridBagConstraints();
        gbcMaxDesc.gridx = 1; gbcMaxDesc.gridy = 10;
        gbcMaxDesc.anchor = java.awt.GridBagConstraints.WEST;
        gbcMaxDesc.insets = new java.awt.Insets(10, 10, 10, 10);
        jDialog1.getContentPane().add(maxDescuento, gbcMaxDesc);

        // --- Tabla "Precios por lista" al final del diálogo (gridy=11 y 12) ---
        jLabelListaPrecios.setFont(new java.awt.Font("Noto Serif", 1, 18));
        jLabelListaPrecios.setForeground(new java.awt.Color(78, 150, 150));
        java.awt.GridBagConstraints gbcTblLista = new java.awt.GridBagConstraints();
        gbcTblLista.gridx = 0; gbcTblLista.gridy = 11;
        gbcTblLista.gridwidth = 2;
        gbcTblLista.anchor = java.awt.GridBagConstraints.WEST;
        gbcTblLista.insets = new java.awt.Insets(14, 10, 4, 10);
        jDialog1.getContentPane().add(jLabelListaPrecios, gbcTblLista);

        tblListaPrecios = new javax.swing.JTable(new javax.swing.table.DefaultTableModel(
            new Object[][]{},
            new String[]{"Lista", "Precio"}
        ) {
            @Override
            public boolean isCellEditable(int row, int col) { return col == 1; }
            @Override
            public Class<?> getColumnClass(int col) { return col == 1 ? Double.class : String.class; }
        });
        tblListaPrecios.setFont(new java.awt.Font("Noto Serif", 0, 16));
        tblListaPrecios.setRowHeight(28);
        tblListaPrecios.getTableHeader().setFont(new java.awt.Font("Noto Serif", 1, 15));

        jScrollPaneListaPrecios = new javax.swing.JScrollPane(tblListaPrecios);
        jScrollPaneListaPrecios.setPreferredSize(new java.awt.Dimension(480, 100));

        gbcTblLista = new java.awt.GridBagConstraints();
        gbcTblLista.gridx = 0; gbcTblLista.gridy = 12;
        gbcTblLista.gridwidth = 2;
        gbcTblLista.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gbcTblLista.insets = new java.awt.Insets(0, 10, 14, 10);
        jDialog1.getContentPane().add(jScrollPaneListaPrecios, gbcTblLista);

        jDialog1.setMinimumSize(new java.awt.Dimension(550, 820));
        jDialog1.setSize(new java.awt.Dimension(550, 820));
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

        jDialog1 = new javax.swing.JDialog();
        leibel = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        precMen = new javax.swing.JFormattedTextField();
        jLabel5 = new javax.swing.JLabel();
        precMay = new javax.swing.JFormattedTextField();
        jLabel6 = new javax.swing.JLabel();
        cadd = new javax.swing.JFormattedTextField();
        jLabel7 = new javax.swing.JLabel();
        codigoProvField = new javax.swing.JFormattedTextField();
        checkGenerarCodigo = new javax.swing.JCheckBox();
        jLabelCategoria = new javax.swing.JLabel();
        categoriaCombo = new javax.swing.JComboBox<>();
        labelinc = new javax.swing.JLabel();
        hechoIns = new javax.swing.JButton();
        hechoAct = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        nom = new javax.swing.JTextPane();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        buskProd = new javax.swing.JFormattedTextField();
        sumLabel = new javax.swing.JLabel();
        filtroCategoriaLabel = new javax.swing.JLabel();
        filtroCategoria = new javax.swing.JComboBox<>();
        lblOrdenInv = new javax.swing.JLabel();
        cmbOrdenInv = new javax.swing.JComboBox<>();
        panelBotones = new javax.swing.JPanel();
        agB = new javax.swing.JButton();
        eliB = new javax.swing.JButton();
        actB = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        tablaVentas = new javax.swing.JTable();

        jDialog1.setAlwaysOnTop(true);
        jDialog1.setMinimumSize(new java.awt.Dimension(550, 580));
        jDialog1.setModal(true);
        jDialog1.setPreferredSize(new java.awt.Dimension(550, 580));
        jDialog1.setSize(new java.awt.Dimension(550, 580));
        jDialog1.getContentPane().setLayout(new java.awt.GridBagLayout());

        leibel.setFont(new java.awt.Font("Noto Serif", 1, 17)); // NOI18N
        leibel.setForeground(new java.awt.Color(121, 167, 167));
        leibel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        leibel.setText("El ID del producto se asignará automáticamente");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.ipadx = 16;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        jDialog1.getContentPane().add(leibel, gridBagConstraints);

        jLabel3.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(78, 150, 150));
        jLabel3.setText("Nombre: ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.ipadx = 16;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        jDialog1.getContentPane().add(jLabel3, gridBagConstraints);

        jLabel4.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(78, 150, 150));
        jLabel4.setText("Precio Mayoreo:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.ipadx = 16;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        jDialog1.getContentPane().add(jLabel4, gridBagConstraints);

        precMen.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        precMen.setPreferredSize(new java.awt.Dimension(200, 35));
        precMen.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                precMenKeyTyped(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        jDialog1.getContentPane().add(precMen, gridBagConstraints);

        jLabel5.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(78, 150, 150));
        jLabel5.setText("Cantidad: ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.ipadx = 16;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        jDialog1.getContentPane().add(jLabel5, gridBagConstraints);

        precMay.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        precMay.setPreferredSize(new java.awt.Dimension(200, 35));
        precMay.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                precMayKeyTyped(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        jDialog1.getContentPane().add(precMay, gridBagConstraints);

        jLabel6.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        jLabel6.setForeground(new java.awt.Color(78, 150, 150));
        jLabel6.setText("Precio Menudeo:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.ipadx = 16;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        jDialog1.getContentPane().add(jLabel6, gridBagConstraints);

        cadd.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        cadd.setPreferredSize(new java.awt.Dimension(200, 35));
        cadd.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                caddKeyTyped(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        jDialog1.getContentPane().add(cadd, gridBagConstraints);

        jLabel7.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        jLabel7.setForeground(new java.awt.Color(78, 150, 150));
        jLabel7.setText("Código del Proveedor:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.ipadx = 16;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        jDialog1.getContentPane().add(jLabel7, gridBagConstraints);

        codigoProvField.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        codigoProvField.setPreferredSize(new java.awt.Dimension(200, 35));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        jDialog1.getContentPane().add(codigoProvField, gridBagConstraints);

        checkGenerarCodigo.setFont(new java.awt.Font("Noto Serif", 0, 14)); // NOI18N
        checkGenerarCodigo.setText("Generar código propio");
        checkGenerarCodigo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkGenerarCodigoActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 5, 10);
        jDialog1.getContentPane().add(checkGenerarCodigo, gridBagConstraints);

        jLabelCategoria.setFont(new java.awt.Font("Noto Serif", 1, 18));
        jLabelCategoria.setForeground(new java.awt.Color(78, 150, 150));
        jLabelCategoria.setText("Categoría:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.ipadx = 16;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        jDialog1.getContentPane().add(jLabelCategoria, gridBagConstraints);

        categoriaCombo.setFont(new java.awt.Font("Noto Serif", 0, 16));
        categoriaCombo.setPreferredSize(new java.awt.Dimension(200, 35));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        jDialog1.getContentPane().add(categoriaCombo, gridBagConstraints);

        labelinc.setFont(new java.awt.Font("Noto Serif", 0, 12)); // NOI18N
        labelinc.setForeground(new java.awt.Color(204, 0, 51));
        labelinc.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        labelinc.setText(" ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = 2;
        jDialog1.getContentPane().add(labelinc, gridBagConstraints);

        hechoIns.setFont(new java.awt.Font("Noto Serif", 1, 20)); // NOI18N
        hechoIns.setForeground(new java.awt.Color(78, 150, 150));
        hechoIns.setText("Hecho");
        hechoIns.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                hechoInsActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.insets = new java.awt.Insets(30, 10, 10, 10);
        jDialog1.getContentPane().add(hechoIns, gridBagConstraints);
        hechoIns.setVisible(false);

        hechoAct.setFont(new java.awt.Font("Noto Serif", 1, 20)); // NOI18N
        hechoAct.setForeground(new java.awt.Color(78, 150, 150));
        hechoAct.setText("Hecho");
        hechoAct.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                hechoActActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.insets = new java.awt.Insets(30, 10, 10, 10);
        jDialog1.getContentPane().add(hechoAct, gridBagConstraints);
        hechoAct.setVisible(false);

        jScrollPane2.setPreferredSize(new java.awt.Dimension(200, 35));

        nom.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        nom.setPreferredSize(new java.awt.Dimension(200, 35));
        nom.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                nomKeyTyped(evt);
            }
        });
        jScrollPane2.setViewportView(nom);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jDialog1.getContentPane().add(jScrollPane2, gridBagConstraints);

        jDialog1.setLocationRelativeTo(null);

        setLayout(new java.awt.BorderLayout());

        jPanel1.setLayout(new java.awt.GridBagLayout());

        jLabel1.setFont(new java.awt.Font("Noto Serif", 1, 36)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(78, 150, 150));
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("Inventario");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.ipadx = 10;
        gridBagConstraints.ipady = 10;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        jPanel1.add(jLabel1, gridBagConstraints);

        buskProd.setFont(new java.awt.Font("Noto Serif", 0, 18)); // NOI18N
        buskProd.setMinimumSize(new java.awt.Dimension(200, 30));
        buskProd.setPreferredSize(new java.awt.Dimension(400, 30));
        buskProd.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                buskProdKeyReleased(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                buskProdKeyTyped(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        jPanel1.add(buskProd, gridBagConstraints);

        sumLabel.setBackground(new java.awt.Color(255, 255, 255));
        sumLabel.setFont(new java.awt.Font("Noto Serif", 1, 20)); // NOI18N
        sumLabel.setForeground(new java.awt.Color(78, 150, 150));
        sumLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        sumLabel.setText("Buscar:");
        sumLabel.setPreferredSize(new java.awt.Dimension(81, 40));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        jPanel1.add(sumLabel, gridBagConstraints);

        filtroCategoriaLabel.setFont(new java.awt.Font("Noto Serif", 1, 16));
        filtroCategoriaLabel.setForeground(new java.awt.Color(78, 150, 150));
        filtroCategoriaLabel.setText("Categoría:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 10, 10);
        jPanel1.add(filtroCategoriaLabel, gridBagConstraints);

        filtroCategoria.setFont(new java.awt.Font("Noto Serif", 0, 16));
        filtroCategoria.setPreferredSize(new java.awt.Dimension(300, 30));
        filtroCategoria.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                filtroCategoriaActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 10, 0);
        jPanel1.add(filtroCategoria, gridBagConstraints);

        lblOrdenInv.setFont(new java.awt.Font("Noto Serif", 1, 16)); // NOI18N
        lblOrdenInv.setForeground(new java.awt.Color(78, 150, 150));
        lblOrdenInv.setText("Ordenar:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 10, 10);
        jPanel1.add(lblOrdenInv, gridBagConstraints);

        cmbOrdenInv.setFont(new java.awt.Font("Noto Serif", 0, 16)); // NOI18N
        cmbOrdenInv.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] {"Sin ordenar", "A-Z (Nombre)", "Z-A (Nombre)", "Mayor existencia", "Menor existencia"}));
        cmbOrdenInv.setPreferredSize(new java.awt.Dimension(300, 30));
        cmbOrdenInv.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbOrdenInvActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 10, 0);
        jPanel1.add(cmbOrdenInv, gridBagConstraints);

        add(jPanel1, java.awt.BorderLayout.NORTH);

        panelBotones.setLayout(new java.awt.GridBagLayout());

        agB.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        agB.setForeground(new java.awt.Color(78, 150, 150));
        agB.setText("Agregar");
        agB.setPreferredSize(new java.awt.Dimension(125, 33));
        agB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                agBActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(10, 30, 10, 30);
        panelBotones.add(agB, gridBagConstraints);

        eliB.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        eliB.setForeground(new java.awt.Color(78, 150, 150));
        eliB.setText("Eliminar");
        eliB.setPreferredSize(new java.awt.Dimension(125, 33));
        eliB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                eliBActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(10, 30, 10, 30);
        panelBotones.add(eliB, gridBagConstraints);

        actB.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        actB.setForeground(new java.awt.Color(78, 150, 150));
        actB.setText("Actualizar");
        actB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                actBActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(10, 30, 10, 30);
        panelBotones.add(actB, gridBagConstraints);

        add(panelBotones, java.awt.BorderLayout.EAST);

        jScrollPane1.setPreferredSize(new java.awt.Dimension(540, 402));

        tablaVentas.setFont(new java.awt.Font("Noto Serif", 0, 14)); // NOI18N
        tablaVentas.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Código", "Producto", "Cantidad", "Precio Mayoreo", "Precio Menudeo", "Categoría"
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

        // Agregar listener para clic en la columna ID (columna 0)
        tablaVentas.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int column = tablaVentas.getColumnModel().getColumnIndexAtX(evt.getX());
                int row = evt.getPoint().y / tablaVentas.getRowHeight();
                
                // Si hizo clic en la columna 0 (ID) y hay una fila seleccionada
                if (row < tablaVentas.getRowCount() && row >= 0 && column == 0) {
                    int modelRow = tablaVentas.convertRowIndexToModel(row);
                    String idProducto = "" + modelo.getValueAt(modelRow, 0);
                    mostrarCodigoBarras(idProducto);
                }
            }
        });

        add(jScrollPane1, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void eliBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_eliBActionPerformed
        // TODO add your handling code here:
        
        if(tablaVentas.getSelectedRow() != -1){
            int resp = Mise.JOptionYesNo("¿Seguro que desea eliminar esta fila?", "Eliminar");
            if(resp == 0){
                int a = tablaVentas.convertRowIndexToModel(tablaVentas.getSelectedRow());
                if(conect.eliminarProducto("" + modelo.getValueAt(a, 0))){//Si se elimina muestra la nueva tabla
                    mostrarTabla("");
                }
            }
        }
        else
            Mise.JOption("Debe seleccionar la fila que desea eliminar", "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
    }//GEN-LAST:event_eliBActionPerformed

    private void agBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_agBActionPerformed
        // TODO add your handling code here:
        jDialog1.setTitle("Agregar Producto");
        leibel.setText("El ID del producto se asigna automáticamente");
        nom.setText("");
        precMen.setText("");
        precMay.setText("");
        cadd.setText("");
        labelinc.setText("");
        codigoProvField.setText("");
        checkGenerarCodigo.setSelected(false);

        // Habilitar campos de código al agregar
        codigoProvField.setEnabled(true);
        checkGenerarCodigo.setEnabled(true);

        // Cargar categorías activas en combo
        cargarCategoriasCombo();

        // Inicializar tabla de precios con todas las listas en 0
        limpiarPreciosLista();

        hechoIns.setVisible(true);
        hechoAct.setVisible(false);
        jDialog1.setVisible(true);
        aux = false;
    }//GEN-LAST:event_agBActionPerformed

    private void actBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_actBActionPerformed
        // ACTUALIZAR
        if(tablaVentas.getSelectedRow() != -1){
            jDialog1.setTitle("Actualizar Producto");
            labelinc.setText("");
            int a = tablaVentas.convertRowIndexToModel(tablaVentas.getSelectedRow());
            hechoIns.setVisible(false);
            hechoAct.setVisible(true);
            
            // Deshabilitar campos de código al actualizar
            codigoProvField.setEnabled(false);
            checkGenerarCodigo.setEnabled(false);
            
            // Cargar categorías activas en combo
            cargarCategoriasCombo();

            //MOSTRAR LOS DATOS EN EL FIELD
            mostrarDAct(a);
            jDialog1.setVisible(true);
            aux = false;
        }
        else
            Mise.JOption("Debe seleccionar la fila que desea actualizar", "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
    }//GEN-LAST:event_actBActionPerformed

    private void buskProdKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_buskProdKeyTyped
        char letra = evt.getKeyChar();
        if(!Cake.letrasMayus(letra) && !Cake.letrasMinus(letra) && !Cake.numeros(letra) && !(Cake.inicioEspacios(letra))){
            evt.consume();
        }
        
        if(!(buskProd.getText().isEmpty())){
            if(Cake.espacios(buskProd.getText(), letra)){
                evt.consume();
            }
        }
        else{
            if(Cake.inicioEspacios(letra)){
                evt.consume();
            }
        }
        
        if(Cake.tamaño(buskProd.getText(), 30)){
            evt.consume();
        }
    }//GEN-LAST:event_buskProdKeyTyped

    private void hechoInsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hechoInsActionPerformed
        if(nom.getText().isEmpty() || precMen.getText().isEmpty() || precMay.getText().isEmpty() || cadd.getText().isEmpty()){
            labelinc.setText("Todos los campos deben ser completados");
        } else{
            // Validar código
            if(!checkGenerarCodigo.isSelected() && codigoProvField.getText().isEmpty()){
                labelinc.setText("Debe ingresar un código del proveedor o marcar generar código propio");
                return;
            }

            if(Double.parseDouble(precMay.getText()) < Double.parseDouble(precMen.getText())){
                if(aux == false){
                    aux = true;

                    // Validar y obtener max_descuento
                    double maxDesc = 100.0;
                    if (!maxDescuento.getText().isEmpty()) {
                        maxDesc = Double.parseDouble(maxDescuento.getText());
                        if (maxDesc < 0) maxDesc = 0;
                        if (maxDesc > 100) maxDesc = 100;
                    }

                    // Determinar el código a usar
                    String codigoProducto;
                    if(checkGenerarCodigo.isSelected()){
                        codigoProducto = GeneradorCodigoBarras.generarCodigoAutomatico();
                    } else {
                        codigoProducto = codigoProvField.getText();
                    }

                    String campos[] = {codigoProducto, nom.getText(), cadd.getText(), precMay.getText(), precMen.getText()};
                    Integer idCat = getSelectedCategoriaId();
                    conect.insertarProductoConCodigoYCategoria(campos, idCat, maxDesc);

                    // Guardar precios por lista
                    guardarPreciosLista(codigoProducto);

                    mostrarTabla("");

                    // Limpiar campos
                    codigoProvField.setText("");
                    checkGenerarCodigo.setSelected(false);
                    codigoProvField.setEnabled(true);
                    maxDescuento.setText("100");

                    jDialog1.setVisible(false);
                }
            } else {
                labelinc.setText("El precio mayoreo debe ser menor que el precio menudeo");
            }
        }
    }//GEN-LAST:event_hechoInsActionPerformed

    private void hechoActActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hechoActActionPerformed
        if(nom.getText().isEmpty() || precMen.getText().isEmpty() || precMay.getText().isEmpty() || cadd.getText().isEmpty()){
            labelinc.setText("Todos los campos deben ser completados");
        } else{
            if(Double.parseDouble(precMay.getText()) < Double.parseDouble(precMen.getText())){
                if(aux == false){
                    aux = true;
                    int a = tablaVentas.convertRowIndexToModel(tablaVentas.getSelectedRow());
                    String elemento = "" + modelo.getValueAt(a, 0);

                    // Validar y obtener max_descuento
                    double maxDesc = 100.0;
                    if (!maxDescuento.getText().isEmpty()) {
                        maxDesc = Double.parseDouble(maxDescuento.getText());
                        if (maxDesc < 0) maxDesc = 0;
                        if (maxDesc > 100) maxDesc = 100;
                    }

                    String[] campos = {nom.getText(), cadd.getText(), precMay.getText(), precMen.getText()};
                    Integer idCat = getSelectedCategoriaId();
                    conect.actualizarProductoConCategoria(elemento, campos, idCat, maxDesc);

                    // Guardar precios por lista
                    guardarPreciosLista(elemento);

                    mostrarTabla("");
                    jDialog1.setVisible(false);
                }
            } else {
                labelinc.setText("El precio mayoreo debe ser menor que el precio menudeo");
            }
        }
    }//GEN-LAST:event_hechoActActionPerformed

    private void checkGenerarCodigoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkGenerarCodigoActionPerformed
        if (checkGenerarCodigo.isSelected()) {
            // Deshabilitar campo de código del proveedor
            codigoProvField.setEnabled(false);
            codigoProvField.setText("");
        } else {
            // Habilitar campo para ingresar código del proveedor
            codigoProvField.setEnabled(true);
            codigoProvField.requestFocus();
        }
    }//GEN-LAST:event_checkGenerarCodigoActionPerformed

    private void precMenKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_precMenKeyTyped
        char letra = evt.getKeyChar();
        if(!Cake.numeros(letra) && !Cake.inicioPunto(letra)){
            evt.consume();
        }
        
        if(!(precMen.getText().isEmpty())){
            if(Cake.hayPuntos(precMen.getText()) && letra=='.'){
                evt.consume();
            } else{
                if(Cake.punto(precMen.getText(), letra)){
                    evt.consume();
                }
            }
        } else{
            if(Cake.inicioPunto(letra)){
                evt.consume();
            }
        }
        
        if(Cake.tamaño(precMen.getText(), 10)){
            evt.consume();
        }
    }//GEN-LAST:event_precMenKeyTyped

    private void caddKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_caddKeyTyped
        char letra = evt.getKeyChar();
        if(!Cake.numeros(letra)){
            evt.consume();
        }
        
        if(Cake.tamaño(cadd.getText(), 10)){
            evt.consume();
        }
    }//GEN-LAST:event_caddKeyTyped

    private void precMayKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_precMayKeyTyped
        char letra = evt.getKeyChar();
        if(!Cake.numeros(letra) && !Cake.inicioPunto(letra)){
            evt.consume();
        }
        
        if(!(precMay.getText().isEmpty())){
            if(Cake.hayPuntos(precMay.getText()) && letra=='.'){
                evt.consume();
            } else{
                if(Cake.punto(precMay.getText(), letra)){
                    evt.consume();
                }
            }
        } else{
            if(Cake.inicioPunto(letra)){
                evt.consume();
            }
        }
        
        if(Cake.tamaño(precMay.getText(), 10)){
            evt.consume();
        }
    }//GEN-LAST:event_precMayKeyTyped

    private void buskProdKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_buskProdKeyReleased
        String busqueda = buskProd.getText();
        String instruccion = "SELECT p.*, c.nombre AS categoria_nombre FROM producto p LEFT JOIN categoria c ON p.id_categoria = c.id_categoria WHERE p.nombre LIKE '%" + busqueda + "%' OR CAST(p.id_producto AS TEXT) LIKE '%" + busqueda + "%' ORDER BY p.id_producto;";
        if(!buskProd.getText().isEmpty()){
            mostrarTabla(instruccion);
        }
        if(evt.getKeyCode() == KeyEvent.VK_BACK_SPACE){
            mostrarTabla(instruccion);
        }
    }//GEN-LAST:event_buskProdKeyReleased

    private void nomKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_nomKeyTyped
        char letra = evt.getKeyChar();
        if(!Cake.letrasMayus(letra) && !Cake.numeros(letra) && !Cake.letrasMinus(letra) && !(Cake.inicioEspacios(letra))){
            evt.consume();
        }
        
        if(!(nom.getText().isEmpty())){
            if(Cake.espacios(nom.getText(), letra)){
                evt.consume();
            }
        }
        else{
            if(Cake.inicioEspacios(letra)){
                evt.consume();
            }
        }
        
        if(Cake.tamaño(nom.getText(), 50)){
            evt.consume();
        }
    }//GEN-LAST:event_nomKeyTyped

    private void cmbOrdenInvActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbOrdenInvActionPerformed
        aplicarOrdenInv();
    }//GEN-LAST:event_cmbOrdenInvActionPerformed

    private void aplicarOrdenInv() {
        String sel = (String) cmbOrdenInv.getSelectedItem();
        if ("Sin ordenar".equals(sel)) {
            tablaVentas.setRowSorter(null);
            return;
        }
        javax.swing.table.TableRowSorter<javax.swing.table.DefaultTableModel> sorter =
            new javax.swing.table.TableRowSorter<>(modelo);
        tablaVentas.setRowSorter(sorter);
        if ("A-Z (Nombre)".equals(sel)) {
            sorter.setSortKeys(java.util.Arrays.asList(new javax.swing.RowSorter.SortKey(1, javax.swing.SortOrder.ASCENDING)));
        } else if ("Z-A (Nombre)".equals(sel)) {
            sorter.setSortKeys(java.util.Arrays.asList(new javax.swing.RowSorter.SortKey(1, javax.swing.SortOrder.DESCENDING)));
        } else if ("Mayor existencia".equals(sel)) {
            sorter.setComparator(2, java.util.Comparator.comparingInt(o -> {
                try { return Integer.parseInt(o.toString()); } catch (Exception e) { return 0; }
            }));
            sorter.setSortKeys(java.util.Arrays.asList(new javax.swing.RowSorter.SortKey(2, javax.swing.SortOrder.DESCENDING)));
        } else if ("Menor existencia".equals(sel)) {
            sorter.setComparator(2, java.util.Comparator.comparingInt(o -> {
                try { return Integer.parseInt(o.toString()); } catch (Exception e) { return 0; }
            }));
            sorter.setSortKeys(java.util.Arrays.asList(new javax.swing.RowSorter.SortKey(2, javax.swing.SortOrder.ASCENDING)));
        }
        sorter.sort();
    }

    //TABLAS
    public final void mostrarTabla(String inst){
        Mise.limpiarTabla(modelo);
        if(inst.isEmpty()){
            inst = "SELECT p.*, c.nombre AS categoria_nombre FROM producto p LEFT JOIN categoria c ON p.id_categoria = c.id_categoria ORDER BY p.id_producto";
        }
        java.sql.ResultSet rs = conect.query(inst);
        if (rs == null) {
            // Fallback: la tabla categoria puede no existir aún
            rs = conect.query("SELECT *, '' AS categoria_nombre FROM producto ORDER BY id_producto");
        }
        llenarTabla(rs);
    }
    
    public void llenarTabla(java.sql.ResultSet rs){
        if (rs == null) return;
        try{
            while(rs.next()){
                String catNombre = "";
                try { catNombre = rs.getString("categoria_nombre"); } catch(Exception ex) { /* columna no existe en query */ }
                if (catNombre == null) catNombre = "";
                modelo.addRow( new Object[]{rs.getString("id_producto"), rs.getString("nombre"), rs.getInt("cantidad"),
                    rs.getDouble("precio_mayoreo"), rs.getDouble("precio_menudeo"), catNombre});
            }
        }catch(java.sql.SQLException e){
            System.out.println("Error al mostrar el inventario: " + e.getMessage());
        }
    }
    
    public void mostrarDAct(int a){
        String elemento = "" + modelo.getValueAt(a, 0);
        leibel.setText("ID: " + elemento);
        nom.setText("" + modelo.getValueAt(a, 1));
        cadd.setText("" + modelo.getValueAt(a, 2));
        precMay.setText("" + modelo.getValueAt(a, 3));
        precMen.setText("" + modelo.getValueAt(a, 4));

        // Mostrar el código actual en el campo (solo lectura)
        codigoProvField.setText(elemento);
        checkGenerarCodigo.setSelected(false);

        // Cargar max_descuento actual desde la base de datos
        double maxDesc = conect.obtenerMaxDescuento(elemento);
        maxDescuento.setText("" + maxDesc);

        // Seleccionar la categoría actual en el combo
        String catActual = "" + modelo.getValueAt(a, 5);
        if (catActual != null && !catActual.isEmpty()) {
            for (int i = 0; i < categoriaCombo.getItemCount(); i++) {
                if (categoriaCombo.getItemAt(i).equals(catActual)) {
                    categoriaCombo.setSelectedIndex(i);
                    break;
                }
            }
        } else {
            categoriaCombo.setSelectedIndex(0); // "Sin categoría"
        }

        // Cargar precios actuales por lista
        cargarPreciosLista(elemento);
    }
    
    // Método para mostrar código de barras en un diálogo
    private void mostrarCodigoBarras(String idProducto){
        // Crear diálogo
        javax.swing.JDialog dialogCodigo = new javax.swing.JDialog();
        dialogCodigo.setTitle("Código de Barras - Producto " + idProducto);
        dialogCodigo.setModal(true);
        dialogCodigo.setSize(450, 300);
        dialogCodigo.setLocationRelativeTo(this);
        dialogCodigo.setLayout(new java.awt.BorderLayout());
        
        // Generar código de barras con el ID del producto
        BufferedImage imagen = GeneradorCodigoBarras.generarCODE128(idProducto, 350, 120);
        
        if(imagen != null){
            javax.swing.JLabel labelImagen = new javax.swing.JLabel(new ImageIcon(imagen));
            labelImagen.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
            dialogCodigo.add(labelImagen, java.awt.BorderLayout.CENTER);
            
            // Label con el ID
            javax.swing.JLabel labelTexto = new javax.swing.JLabel("ID: " + idProducto);
            labelTexto.setFont(new java.awt.Font("Noto Serif", java.awt.Font.BOLD, 16));
            labelTexto.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
            labelTexto.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
            dialogCodigo.add(labelTexto, java.awt.BorderLayout.NORTH);
            
            // Panel de botones inferior
            javax.swing.JPanel panelBotones = new javax.swing.JPanel();
            panelBotones.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 10, 10));
            
            // Botón Guardar
            javax.swing.JButton btnGuardar = new javax.swing.JButton("💾 Guardar");
            btnGuardar.setFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, 12));
            btnGuardar.addActionListener(e -> {
                guardarCodigoBarras(idProducto, imagen);
            });
            
            // Botón Imprimir
            javax.swing.JButton btnImprimir = new javax.swing.JButton("🖨️ Imprimir");
            btnImprimir.setFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, 12));
            btnImprimir.addActionListener(e -> {
                imprimirCodigoBarras(idProducto, imagen);
            });
            
            // Botón Cerrar
            javax.swing.JButton btnCerrar = new javax.swing.JButton("Cerrar");
            btnCerrar.setFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, 12));
            btnCerrar.addActionListener(e -> dialogCodigo.dispose());
            
            panelBotones.add(btnGuardar);
            panelBotones.add(btnImprimir);
            panelBotones.add(btnCerrar);
            
            dialogCodigo.add(panelBotones, java.awt.BorderLayout.SOUTH);
            
            dialogCodigo.setVisible(true);
        } else {
            Mise.JOption("Error al generar código de barras", "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }
    
    // Método para guardar el código de barras como imagen
    private void guardarCodigoBarras(String idProducto, BufferedImage imagen){
        javax.swing.JFileChooser fileChooser = new javax.swing.JFileChooser();
        fileChooser.setDialogTitle("Guardar Código de Barras");
        
        // Establecer nombre por defecto
        String nombreArchivo = "codigo_barras_" + idProducto + ".png";
        fileChooser.setSelectedFile(new java.io.File(nombreArchivo));
        
        // Filtro para solo archivos PNG
        javax.swing.filechooser.FileNameExtensionFilter filter = 
            new javax.swing.filechooser.FileNameExtensionFilter("Imágenes PNG", "png");
        fileChooser.setFileFilter(filter);
        
        int resultado = fileChooser.showSaveDialog(this);
        
        if(resultado == javax.swing.JFileChooser.APPROVE_OPTION){
            try {
                java.io.File archivo = fileChooser.getSelectedFile();
                
                // Asegurar extensión .png
                String rutaArchivo = archivo.getAbsolutePath();
                if(!rutaArchivo.toLowerCase().endsWith(".png")){
                    rutaArchivo += ".png";
                    archivo = new java.io.File(rutaArchivo);
                }
                
                // Guardar imagen
                javax.imageio.ImageIO.write(imagen, "png", archivo);
                
                Mise.JOption("Código de barras guardado exitosamente en:\n" + archivo.getAbsolutePath(), 
                            "Guardado", javax.swing.JOptionPane.INFORMATION_MESSAGE);
            } catch(java.io.IOException ex){
                Mise.JOption("Error al guardar el archivo: " + ex.getMessage(), 
                            "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    // Método para imprimir el código de barras
    private void imprimirCodigoBarras(String idProducto, BufferedImage imagen){
        try {
            // Crear un componente imprimible personalizado
            java.awt.print.PrinterJob job = java.awt.print.PrinterJob.getPrinterJob();
            job.setJobName("Código de Barras - " + idProducto);
            
            job.setPrintable(new java.awt.print.Printable() {
                @Override
                public int print(java.awt.Graphics graphics, java.awt.print.PageFormat pageFormat, int pageIndex) {
                    if (pageIndex > 0) {
                        return NO_SUCH_PAGE;
                    }
                    
                    java.awt.Graphics2D g2d = (java.awt.Graphics2D) graphics;
                    
                    // Trasladar al área imprimible
                    g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
                    
                    // Calcular escala para centrar la imagen
                    int anchoDisponible = (int) pageFormat.getImageableWidth();
                    int altoDisponible = (int) pageFormat.getImageableHeight();
                    
                    // Dibujar título
                    g2d.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 16));
                    String titulo = "Código de Barras - Producto: " + idProducto;
                    java.awt.FontMetrics fm = g2d.getFontMetrics();
                    int anchoTitulo = fm.stringWidth(titulo);
                    g2d.drawString(titulo, (anchoDisponible - anchoTitulo) / 2, 50);
                    
                    // Dibujar imagen del código de barras centrada
                    int x = (anchoDisponible - imagen.getWidth()) / 2;
                    int y = 100;
                    g2d.drawImage(imagen, x, y, null);
                    
                    // Dibujar el código debajo de la imagen
                    g2d.setFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, 14));
                    fm = g2d.getFontMetrics();
                    int anchoCodigo = fm.stringWidth(idProducto);
                    g2d.drawString(idProducto, (anchoDisponible - anchoCodigo) / 2, y + imagen.getHeight() + 30);
                    
                    return PAGE_EXISTS;
                }
            });
            
            // Mostrar diálogo de impresión
            if (job.printDialog()) {
                job.print();
                Mise.JOption("Código de barras enviado a impresora", 
                            "Imprimir", javax.swing.JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (java.awt.print.PrinterException ex) {
            Mise.JOption("Error al imprimir: " + ex.getMessage(), 
                        "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }
    
    // ── Métodos de lista de precios ──────────────────────────────────

    /** Carga los precios del producto en la tabla tblListaPrecios */
    private void cargarPreciosLista(String idProducto) {
        javax.swing.table.DefaultTableModel m = (javax.swing.table.DefaultTableModel) tblListaPrecios.getModel();
        m.setRowCount(0);
        precioListaIds.clear();
        java.sql.ResultSet rs = conect.obtenerListas();
        if (rs != null) {
            try {
                while (rs.next()) {
                    int idLista = rs.getInt("id_lista");
                    String nombre = rs.getString("nombre");
                    double precio = conect.obtenerPrecioEnLista(idProducto, idLista);
                    if (precio < 0) precio = 0.0;
                    m.addRow(new Object[]{nombre, precio});
                    precioListaIds.add(idLista);
                }
            } catch (java.sql.SQLException e) {
                System.out.println("Error al cargar precios por lista: " + e.getMessage());
            }
        }
    }

    /** Carga todas las listas con precio 0 (para producto nuevo) */
    private void limpiarPreciosLista() {
        javax.swing.table.DefaultTableModel m = (javax.swing.table.DefaultTableModel) tblListaPrecios.getModel();
        m.setRowCount(0);
        precioListaIds.clear();
        java.sql.ResultSet rs = conect.obtenerListas();
        if (rs != null) {
            try {
                while (rs.next()) {
                    precioListaIds.add(rs.getInt("id_lista"));
                    m.addRow(new Object[]{rs.getString("nombre"), 0.0});
                }
            } catch (java.sql.SQLException e) {
                System.out.println("Error al cargar listas: " + e.getMessage());
            }
        }
    }

    /** Persiste en producto_precio los precios editados en la tabla */
    private void guardarPreciosLista(String idProducto) {
        javax.swing.table.DefaultTableModel m = (javax.swing.table.DefaultTableModel) tblListaPrecios.getModel();
        // Confirmar cualquier edición en curso
        if (tblListaPrecios.isEditing()) {
            tblListaPrecios.getCellEditor().stopCellEditing();
        }
        for (int i = 0; i < m.getRowCount(); i++) {
            if (i >= precioListaIds.size()) break;
            int idLista = precioListaIds.get(i);
            Object val = m.getValueAt(i, 1);
            double precio = 0.0;
            if (val != null) {
                try { precio = Double.parseDouble(val.toString()); } catch (NumberFormatException ex) { /* ignorar */ }
            }
            conect.insertarPrecioEnLista(idProducto, idLista, precio);
        }
    }

    // ── Métodos de categoría ─────────────────────────────────────────

    /** Carga las categorías activas en el combo del diálogo agregar/editar producto */
    private void cargarCategoriasCombo() {
        categoriaCombo.removeAllItems();
        categoriaIds.clear();
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

    /** Carga las categorías en el combo filtro encima de la tabla */
    public void cargarFiltroCategorias() {
        cargandoFiltro = true;
        filtroCategoria.removeAllItems();
        filtroCategoriaIds.clear();
        filtroCategoria.addItem("Todas");
        filtroCategoriaIds.add(0);
        java.sql.ResultSet rs = conect.obtenerCategorias();
        if (rs != null) {
            try {
                while (rs.next()) {
                    filtroCategoriaIds.add(rs.getInt("id_categoria"));
                    filtroCategoria.addItem(rs.getString("nombre"));
                }
            } catch (java.sql.SQLException e) {
                System.out.println("Error al cargar filtro categorías: " + e.getMessage());
            }
        }
        cargandoFiltro = false;
    }

    /** Obtiene el ID de la categoría seleccionada en el combo del diálogo, null si "Sin categoría" */
    private Integer getSelectedCategoriaId() {
        int idx = categoriaCombo.getSelectedIndex();
        if (idx <= 0) return null;
        return categoriaIds.get(idx);
    }

    /** Acción del combo filtro de categoría */
    private void filtroCategoriaActionPerformed(java.awt.event.ActionEvent evt) {
        if (cargandoFiltro) return;
        int idx = filtroCategoria.getSelectedIndex();
        if (idx <= 0) {
            // "Todas" – mostrar todo
            mostrarTabla("");
        } else {
            int idCat = filtroCategoriaIds.get(idx);
            String inst = "SELECT p.*, c.nombre AS categoria_nombre FROM producto p LEFT JOIN categoria c ON p.id_categoria = c.id_categoria WHERE p.id_categoria = " + idCat + " ORDER BY p.id_producto";
            mostrarTabla(inst);
        }
    }


    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton actB;
    private javax.swing.JButton agB;
    private javax.swing.JFormattedTextField buskProd;
    private javax.swing.JFormattedTextField cadd;
    private javax.swing.JComboBox<String> categoriaCombo;
    private javax.swing.JCheckBox checkGenerarCodigo;
    private javax.swing.JComboBox<String> cmbOrdenInv;
    private javax.swing.JFormattedTextField codigoProvField;
    private javax.swing.JButton eliB;
    private javax.swing.JComboBox<String> filtroCategoria;
    private javax.swing.JLabel filtroCategoriaLabel;
    private javax.swing.JButton hechoAct;
    private javax.swing.JButton hechoIns;
    private javax.swing.JDialog jDialog1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabelCategoria;
    private javax.swing.JLabel lblOrdenInv;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    public static javax.swing.JLabel labelinc;
    private javax.swing.JLabel leibel;
    private javax.swing.JTextPane nom;
    public javax.swing.JPanel panelBotones;
    private javax.swing.JFormattedTextField precMay;
    private javax.swing.JFormattedTextField precMen;
    private javax.swing.JLabel sumLabel;
    public static javax.swing.JTable tablaVentas;
    // End of variables declaration//GEN-END:variables
}
