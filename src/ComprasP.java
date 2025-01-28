
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

    /**
     * Creates new form ComprasP
     */
    public ComprasP() {
        initComponents();
        modeloCom = (DefaultTableModel)tablaCompras.getModel();
        modeloProdCom = (DefaultTableModel)tablaProdCom.getModel();
        modeloProd = (DefaultTableModel)tablaProd.getModel();
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
        jLabel4 = new javax.swing.JLabel();
        hechoB1 = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        rasF = new javax.swing.JTextPane();
        prodComDialog = new javax.swing.JDialog();
        panelRegProdC = new javax.swing.JPanel();
        jScrollPane7 = new javax.swing.JScrollPane();
        tablaProd = new javax.swing.JTable();
        jScrollPane6 = new javax.swing.JScrollPane();
        tablaProdCom = new javax.swing.JTable();
        jPanel5 = new javax.swing.JPanel();
        jLabel9 = new javax.swing.JLabel();
        codP = new javax.swing.JFormattedTextField();
        jLabel11 = new javax.swing.JLabel();
        cantP = new javax.swing.JFormattedTextField();
        jLabel12 = new javax.swing.JLabel();
        precP = new javax.swing.JFormattedTextField();
        agP = new javax.swing.JButton();
        acP = new javax.swing.JButton();
        elP = new javax.swing.JButton();
        heP = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tablaCompras = new javax.swing.JTable();
        jPanel3 = new javax.swing.JPanel();
        agreCompra = new javax.swing.JButton();
        actCompra = new javax.swing.JButton();

        comDialog.setTitle("Compras");
        comDialog.setAlwaysOnTop(true);
        comDialog.setMinimumSize(new java.awt.Dimension(550, 260));
        comDialog.setModal(true);
        comDialog.setSize(new java.awt.Dimension(550, 260));
        comDialog.getContentPane().setLayout(new java.awt.CardLayout());

        panelRegCompra.setLayout(new java.awt.GridBagLayout());

        jLabel4.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(78, 150, 150));
        jLabel4.setText("Descripcion:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.insets = new java.awt.Insets(20, 20, 20, 20);
        panelRegCompra.add(jLabel4, gridBagConstraints);

        hechoB1.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        hechoB1.setForeground(new java.awt.Color(78, 150, 150));
        hechoB1.setText("Hecho");
        hechoB1.setPreferredSize(new java.awt.Dimension(100, 40));
        hechoB1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                hechoB1ActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        panelRegCompra.add(hechoB1, gridBagConstraints);

        jScrollPane2.setPreferredSize(new java.awt.Dimension(342, 150));

        rasF.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        rasF.setPreferredSize(new java.awt.Dimension(340, 50));
        rasF.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                rasFKeyTyped(evt);
            }
        });
        jScrollPane2.setViewportView(rasF);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        panelRegCompra.add(jScrollPane2, gridBagConstraints);

        comDialog.getContentPane().add(panelRegCompra, "card2");

        comDialog.setLocationRelativeTo(null);

        prodComDialog.setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        prodComDialog.setTitle("Compras");
        prodComDialog.setAlwaysOnTop(true);
        prodComDialog.setMinimumSize(new java.awt.Dimension(1400, 550));
        prodComDialog.setModal(true);
        prodComDialog.setPreferredSize(new java.awt.Dimension(1400, 435));
        prodComDialog.setSize(new java.awt.Dimension(1400, 550));
        prodComDialog.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                prodComDialogWindowClosing(evt);
            }
        });
        prodComDialog.getContentPane().setLayout(new java.awt.CardLayout());

        panelRegProdC.setLayout(new java.awt.BorderLayout());

        jScrollPane7.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        jScrollPane7.setMinimumSize(new java.awt.Dimension(550, 402));
        jScrollPane7.setOpaque(false);
        jScrollPane7.setPreferredSize(new java.awt.Dimension(550, 402));

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

        panelRegProdC.add(jScrollPane7, java.awt.BorderLayout.WEST);

        tablaProdCom.setFont(new java.awt.Font("Noto Serif", 0, 18)); // NOI18N
        tablaProdCom.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Producto", "Cantidad", "Precio Adquirido", "Total"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane6.setViewportView(tablaProdCom);

        panelRegProdC.add(jScrollPane6, java.awt.BorderLayout.EAST);

        jPanel5.setLayout(new java.awt.GridLayout(0, 1));

        jLabel9.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        jLabel9.setForeground(new java.awt.Color(78, 150, 150));
        jLabel9.setText("Código:");
        jPanel5.add(jLabel9);

        codP.setEditable(false);
        codP.setFont(new java.awt.Font("Noto Serif", 0, 18)); // NOI18N
        codP.setPreferredSize(new java.awt.Dimension(150, 30));
        codP.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                codPKeyTyped(evt);
            }
        });
        jPanel5.add(codP);

        jLabel11.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        jLabel11.setForeground(new java.awt.Color(78, 150, 150));
        jLabel11.setText("Cantidad:");
        jPanel5.add(jLabel11);

        cantP.setFont(new java.awt.Font("Noto Serif", 0, 18)); // NOI18N
        cantP.setPreferredSize(new java.awt.Dimension(150, 30));
        cantP.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                cantPKeyTyped(evt);
            }
        });
        jPanel5.add(cantP);

        jLabel12.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        jLabel12.setForeground(new java.awt.Color(78, 150, 150));
        jLabel12.setText("Precio adqurido:");
        jPanel5.add(jLabel12);

        precP.setFont(new java.awt.Font("Noto Serif", 0, 18)); // NOI18N
        precP.setPreferredSize(new java.awt.Dimension(150, 30));
        precP.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                precPKeyTyped(evt);
            }
        });
        jPanel5.add(precP);

        agP.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        agP.setForeground(new java.awt.Color(78, 150, 150));
        agP.setText("Agregar");
        agP.setPreferredSize(new java.awt.Dimension(125, 33));
        agP.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                agPActionPerformed(evt);
            }
        });
        jPanel5.add(agP);

        acP.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        acP.setForeground(new java.awt.Color(78, 150, 150));
        acP.setText("Actualizar");
        acP.setPreferredSize(new java.awt.Dimension(125, 33));
        acP.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                acPActionPerformed(evt);
            }
        });
        jPanel5.add(acP);

        elP.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        elP.setForeground(new java.awt.Color(78, 150, 150));
        elP.setText("Eliminar");
        elP.setPreferredSize(new java.awt.Dimension(125, 33));
        elP.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                elPActionPerformed(evt);
            }
        });
        jPanel5.add(elP);

        heP.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        heP.setForeground(new java.awt.Color(78, 150, 150));
        heP.setText("Hecho");
        heP.setPreferredSize(new java.awt.Dimension(125, 33));
        heP.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                hePActionPerformed(evt);
            }
        });
        jPanel5.add(heP);

        panelRegProdC.add(jPanel5, java.awt.BorderLayout.CENTER);

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

        prodComDialog.getContentPane().add(panelRegProdC, "card3");

        prodComDialog.setLocationRelativeTo(null);

        setLayout(new java.awt.BorderLayout());

        jLabel3.setFont(new java.awt.Font("Noto Serif", 1, 36)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(78, 150, 150));
        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel3.setText("Compras");
        jLabel3.setPreferredSize(new java.awt.Dimension(163, 70));
        add(jLabel3, java.awt.BorderLayout.PAGE_START);

        jScrollPane1.setPreferredSize(new java.awt.Dimension(540, 402));

        tablaCompras.setFont(new java.awt.Font("Noto Serif", 0, 14)); // NOI18N
        tablaCompras.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Compra", "Empleado", "Fecha", "Descripcion", "Monto"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false
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
        agreCompra.setForeground(new java.awt.Color(78, 150, 150));
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
        gridBagConstraints.insets = new java.awt.Insets(10, 30, 10, 30);
        jPanel3.add(agreCompra, gridBagConstraints);

        actCompra.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        actCompra.setForeground(new java.awt.Color(78, 150, 150));
        actCompra.setText("Actualizar");
        actCompra.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                actCompraActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(10, 30, 10, 30);
        jPanel3.add(actCompra, gridBagConstraints);

        add(jPanel3, java.awt.BorderLayout.LINE_END);
    }// </editor-fold>//GEN-END:initComponents

    private void agreCompraActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_agreCompraActionPerformed
        comDialog.setVisible(true);
    }//GEN-LAST:event_agreCompraActionPerformed

    private void actCompraActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_actCompraActionPerformed
        if(tablaCompras.getSelectedRow() != -1){
            id_compra = "" + tablaCompras.getValueAt(tablaCompras.getSelectedRow(), 0);
            mostrarTablaProd();
            mostrarTablaProdCom();
            prodComDialog.setVisible(true);
        } else{
            Mise.JOption("Debe seleccionar la fila que desea actualizar", "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_actCompraActionPerformed

    private void hechoB1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hechoB1ActionPerformed
        if(rasF.getText().isEmpty()){
            Mise.JOption("El campo debe ser completado", "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        } else {
            String[] campos = {Interfaz.idVendedor, rasF.getText(), "0"};
            id_compra = conect.insertarCompra(campos);
            mostrarTablaProd();
            mostrarTablaProdCom();
            //Se hace visible el dialog
            comDialog.setVisible(false);
            prodComDialog.setVisible(true);
        }
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
        if(codP.getText().isEmpty() || precP.getText().isEmpty() || cantP.getText().isEmpty()){
            Mise.JOption("Debe llenar todos los campos", "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        } else{
            String[] campos = {id_compra, codP.getText(), precP.getText(), cantP.getText()};
            conect.insertarProdCompra(campos, ins);
            ins = true;
            codP.setText("");
            cantP.setText("");
            precP.setText("");
        }
        mostrarTablaProdCom();
    }//GEN-LAST:event_agPActionPerformed

    private void acPActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_acPActionPerformed
        if(tablaProdCom.getSelectedRow() == -1){
            Mise.JOption("Seleccione la fila de la tabla de Productos Comprados que desea actualizar", "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        } else{
            codP.setText("" + tablaProdCom.getValueAt(tablaProdCom.getSelectedRow(), 0));
            precP.setText("" + tablaProdCom.getValueAt(tablaProdCom.getSelectedRow(), 2));
            cantP.setText("" + tablaProdCom.getValueAt(tablaProdCom.getSelectedRow(), 1));
            ins = false;
        }
    }//GEN-LAST:event_acPActionPerformed

    private void elPActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_elPActionPerformed
        if(tablaProdCom.getSelectedRow() == -1){
            Mise.JOption("Seleccione la fila de la tabla de Productos Comprados que desea eliminar", "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        } else{
            String[] campos = {id_compra, "" + tablaProdCom.getValueAt(tablaProdCom.getSelectedRow(), 0)};
            conect.eliminarProdCompra(campos);
            mostrarTablaProdCom();
        }
    }//GEN-LAST:event_elPActionPerformed

    private void prodComDialogWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_prodComDialogWindowClosing
        if(modeloProdCom.getRowCount() == 0){
            int res = Mise.JOptionYesNo("No registró ningun producto, ¿seguro que desea cerrar esta ventana?"
                    + "\n(al realizar esta acción la compra no podrá guardarse)", "Cerrar ventana");
            if(res == 0){
                conect.eliminarCompra(id_compra);
                prodComDialog.dispose();
            }
        } else{
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
        prodComDialog.setVisible(false);
        rasF.setText("");
        mostrarTablaCom();
    }//GEN-LAST:event_hePActionPerformed

    private void tablaProdMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tablaProdMouseClicked
        codP.setText("" + tablaProd.getValueAt(tablaProd.getSelectedRow(), 0));
    }//GEN-LAST:event_tablaProdMouseClicked
    
    public void mostrarTablaCom(){
        Mise.limpiarTabla(modeloCom);
        java.sql.ResultSet rs = conect.query("SELECT * FROM compras");
        try{
            while(rs.next()){
                modeloCom.addRow(new Object[]{rs.getString("id_compra"), rs.getString("id_empleado"), rs.getDate("fecha_compra"),
                rs.getString("descripcion"), rs.getDouble("monto")});
            }
        } catch(java.sql.SQLException e){
            System.out.println("Error al mostrar la tabla compras");
        }
    }
    
    public void mostrarTablaProdCom(){
        Mise.limpiarTabla(modeloProdCom);
        java.sql.ResultSet rs = conect.query("SELECT * FROM compra_producto WHERE id_compra='" + id_compra + "';");
        try{
            while(rs.next()){
                modeloProdCom.addRow(new Object[]{rs.getString("id_producto"), rs.getInt("cantidad"), rs.getDouble("precio_adquirido"), 
                rs.getDouble("precio_total")});
            }
        } catch(java.sql.SQLException e){
            System.out.println("Error al mostrar la tabla de compra apartado");
        }
    }
    
    public void mostrarTablaProd(){
        Mise.limpiarTabla(modeloProd);
        java.sql.ResultSet rs = conect.query("SELECT * FROM producto ORDER BY id_producto");
        try{
            while(rs.next()){
                modeloProd.addRow(new Object[]{rs.getString("id_producto"), rs.getString("nombre")});
            }
        } catch(java.sql.SQLException e){
            System.out.println("Error al mostrar la tabla de productos");
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton acP;
    private javax.swing.JButton actCompra;
    private javax.swing.JButton agP;
    private javax.swing.JButton agreCompra;
    private javax.swing.JFormattedTextField cantP;
    private javax.swing.JFormattedTextField codP;
    private javax.swing.JDialog comDialog;
    private javax.swing.JButton elP;
    private javax.swing.JButton heP;
    private javax.swing.JButton hechoB1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JPanel panelRegCompra;
    private javax.swing.JPanel panelRegProdC;
    private javax.swing.JFormattedTextField precP;
    private javax.swing.JDialog prodComDialog;
    private javax.swing.JTextPane rasF;
    private javax.swing.JTable tablaCompras;
    private javax.swing.JTable tablaProd;
    private javax.swing.JTable tablaProdCom;
    // End of variables declaration//GEN-END:variables
}
