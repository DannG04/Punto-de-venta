
import javax.swing.table.DefaultTableModel;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */

/**
 *
 * @author mayra
 */
public class DevolucionesP extends javax.swing.JPanel {

    DefaultTableModel modeloDev = new DefaultTableModel();
    DefaultTableModel modeloVen = new DefaultTableModel();
    DefaultTableModel modeloVenDet = new DefaultTableModel();
    DefaultTableModel modeloProdD = new DefaultTableModel();
    
    ConexionBD conect = new ConexionBD();
    
    String id_dev = "";
    String id_ven = "";
    
    public DevolucionesP() {
        initComponents();
        modeloDev = (DefaultTableModel)tablaD.getModel();
        modeloVen = (DefaultTableModel)tablaV.getModel();
        modeloVenDet = (DefaultTableModel)tablaProdVenta.getModel();
        modeloProdD = (DefaultTableModel)tablaProdDev.getModel();
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

        ventasBusDialog = new javax.swing.JDialog();
        jScrollPane2 = new javax.swing.JScrollPane();
        tablaV = new javax.swing.JTable();
        jPanel3 = new javax.swing.JPanel();
        sumLabel = new javax.swing.JLabel();
        buskVen = new javax.swing.JFormattedTextField();
        prodDevDialog = new javax.swing.JDialog();
        panelRegProdC = new javax.swing.JPanel();
        jScrollPane6 = new javax.swing.JScrollPane();
        tablaProdDev = new javax.swing.JTable();
        jPanel5 = new javax.swing.JPanel();
        jLabel9 = new javax.swing.JLabel();
        codP = new javax.swing.JFormattedTextField();
        jLabel11 = new javax.swing.JLabel();
        cantP = new javax.swing.JFormattedTextField();
        jLabel12 = new javax.swing.JLabel();
        motP = new javax.swing.JComboBox<>();
        agP = new javax.swing.JButton();
        elP = new javax.swing.JButton();
        heP = new javax.swing.JButton();
        jScrollPane7 = new javax.swing.JScrollPane();
        tablaProdVenta = new javax.swing.JTable();
        jPanel4 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tablaD = new javax.swing.JTable();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        regC = new javax.swing.JButton();
        labelinc = new javax.swing.JLabel();
        actC = new javax.swing.JButton();

        ventasBusDialog.setTitle("Devoluciones");
        ventasBusDialog.setAlwaysOnTop(true);
        ventasBusDialog.setMinimumSize(new java.awt.Dimension(700, 500));
        ventasBusDialog.setModal(true);
        ventasBusDialog.setPreferredSize(new java.awt.Dimension(700, 500));
        ventasBusDialog.setSize(new java.awt.Dimension(700, 500));

        tablaV.setFont(new java.awt.Font("Noto Serif", 0, 18)); // NOI18N
        tablaV.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Venta", "Empleado", "Monto", "Fecha"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tablaV.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tablaVMouseClicked(evt);
            }
        });
        jScrollPane2.setViewportView(tablaV);

        ventasBusDialog.getContentPane().add(jScrollPane2, java.awt.BorderLayout.CENTER);

        jPanel3.setLayout(new java.awt.GridBagLayout());

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
        jPanel3.add(sumLabel, gridBagConstraints);

        buskVen.setFont(new java.awt.Font("Noto Serif", 0, 18)); // NOI18N
        buskVen.setPreferredSize(new java.awt.Dimension(500, 30));
        buskVen.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                buskVenKeyReleased(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                buskVenKeyTyped(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 0);
        jPanel3.add(buskVen, gridBagConstraints);

        ventasBusDialog.getContentPane().add(jPanel3, java.awt.BorderLayout.PAGE_START);

        ventasBusDialog.setLocationRelativeTo(null);

        prodDevDialog.setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        prodDevDialog.setTitle("Devoluciones");
        prodDevDialog.setAlwaysOnTop(true);
        prodDevDialog.setMinimumSize(new java.awt.Dimension(1000, 500));
        prodDevDialog.setModal(true);
        prodDevDialog.setPreferredSize(new java.awt.Dimension(1000, 500));
        prodDevDialog.setResizable(false);
        prodDevDialog.setSize(new java.awt.Dimension(1000, 500));
        prodDevDialog.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                prodDevDialogWindowClosing(evt);
            }
        });
        prodDevDialog.getContentPane().setLayout(new java.awt.CardLayout());

        panelRegProdC.setLayout(new java.awt.BorderLayout());

        tablaProdDev.setFont(new java.awt.Font("Noto Serif", 0, 18)); // NOI18N
        tablaProdDev.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Producto", "Motivo", "Cantidad", "Total"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane6.setViewportView(tablaProdDev);

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
        jLabel12.setText("Motivo:");
        jPanel5.add(jLabel12);

        motP.setFont(new java.awt.Font("Noto Serif", 1, 16)); // NOI18N
        motP.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "El producto estaba en mal estado", "Otro motivo" }));
        motP.setPreferredSize(new java.awt.Dimension(340, 35));
        jPanel5.add(motP);

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

        jScrollPane7.setPreferredSize(new java.awt.Dimension(250, 402));

        tablaProdVenta.setFont(new java.awt.Font("Noto Serif", 0, 18)); // NOI18N
        tablaProdVenta.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Producto", "Cantidad"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tablaProdVenta.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tablaProdVentaMouseClicked(evt);
            }
        });
        jScrollPane7.setViewportView(tablaProdVenta);

        panelRegProdC.add(jScrollPane7, java.awt.BorderLayout.WEST);

        jPanel4.setLayout(new java.awt.GridLayout());

        jLabel2.setFont(new java.awt.Font("Noto Serif", 1, 24)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(78, 150, 150));
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel2.setText("Productos Venta");
        jPanel4.add(jLabel2);

        jLabel3.setFont(new java.awt.Font("Noto Serif", 1, 24)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(78, 150, 150));
        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel3.setText("Productos Devueltos");
        jPanel4.add(jLabel3);

        panelRegProdC.add(jPanel4, java.awt.BorderLayout.NORTH);

        prodDevDialog.getContentPane().add(panelRegProdC, "card3");

        prodDevDialog.setLocationRelativeTo(null);

        setLayout(new java.awt.BorderLayout());

        tablaD.setFont(new java.awt.Font("Noto Serif", 0, 18)); // NOI18N
        tablaD.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Devolucion", "Venta", "Fecha", "Monto"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane1.setViewportView(tablaD);

        add(jScrollPane1, java.awt.BorderLayout.CENTER);

        jLabel1.setFont(new java.awt.Font("Noto Serif", 1, 36)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(78, 150, 150));
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("Devoluciones de Venta");
        jPanel1.add(jLabel1);

        add(jPanel1, java.awt.BorderLayout.NORTH);

        jPanel2.setPreferredSize(new java.awt.Dimension(150, 141));
        jPanel2.setLayout(new java.awt.GridBagLayout());

        regC.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        regC.setForeground(new java.awt.Color(78, 150, 150));
        regC.setText("Registrar ");
        regC.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                regCActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.insets = new java.awt.Insets(30, 10, 10, 10);
        jPanel2.add(regC, gridBagConstraints);

        labelinc.setFont(new java.awt.Font("Noto Serif", 0, 12)); // NOI18N
        labelinc.setForeground(new java.awt.Color(204, 0, 51));
        labelinc.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        labelinc.setText(" ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        jPanel2.add(labelinc, gridBagConstraints);

        actC.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        actC.setForeground(new java.awt.Color(78, 150, 150));
        actC.setText("Actualizar");
        actC.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                actCActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        jPanel2.add(actC, gridBagConstraints);

        add(jPanel2, java.awt.BorderLayout.EAST);
    }// </editor-fold>//GEN-END:initComponents

    private void regCActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_regCActionPerformed
        mostrarVen("");
        ventasBusDialog.setVisible(true);
    }//GEN-LAST:event_regCActionPerformed

    private void actCActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_actCActionPerformed
        if(tablaD.getSelectedRow() == -1){
            Mise.JOption("Seleccione la fila que desea actualizar", "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        } else{
            id_dev = "" + tablaD.getValueAt(tablaD.getSelectedRow(), 0);
            id_ven = "" + tablaD.getValueAt(tablaD.getSelectedRow(), 1);
            mostrarVenDet();
            mostrarProdD();
            prodDevDialog.setVisible(true);
        }
    }//GEN-LAST:event_actCActionPerformed

    private void buskVenKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_buskVenKeyReleased
        if(!buskVen.getText().isEmpty()){
            mostrarVen("SELECT * FROM venta WHERE id_venta LIKE '%" + buskVen.getText() + "%';");
        }
        if(evt.getKeyChar() == java.awt.event.KeyEvent.VK_BACK_SPACE){
            mostrarVen("SELECT * FROM venta WHERE id_venta LIKE '%" + buskVen.getText() + "%';");
        }
    }//GEN-LAST:event_buskVenKeyReleased

    private void buskVenKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_buskVenKeyTyped
        char letra = evt.getKeyChar();
        if(!Cake.letrasMayus(letra) && !Cake.letrasMinus(letra) && !Cake.numeros(letra) && !(Cake.inicioEspacios(letra))){
            evt.consume();
        }

        if(!(buskVen.getText().isEmpty())){
            if(Cake.espacios(buskVen.getText(), letra)){
                evt.consume();
            }
        }
        else{
            if(Cake.inicioEspacios(letra)){
                evt.consume();
            }
        }

        if(Cake.tamaño(buskVen.getText(), 30)){
            evt.consume();
        }
    }//GEN-LAST:event_buskVenKeyTyped

    private void tablaVMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tablaVMouseClicked
        if(tablaV.getSelectedRow() != -1) {
            id_ven = "" + tablaV.getValueAt(tablaV.getSelectedRow(), 0);
            int res = Mise.JOptionYesNo("Desea registrar la devolución de la venta: " + id_ven + ", ¿es correcto?", "Confirmar venta");
            if(res==0){
                id_dev = conect.insertarDevolucion(new String[]{id_ven, "0"});
                mostrarVenDet();
                mostrarProdD();
                ventasBusDialog.setVisible(false);
                prodDevDialog.setVisible(true);
            }
        }
    }//GEN-LAST:event_tablaVMouseClicked

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

    private void agPActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_agPActionPerformed
        if(codP.getText().isEmpty() || cantP.getText().isEmpty()){
            Mise.JOption("Debe llenar todos los campos", "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        } else{
            String[] campos = {id_dev, id_ven, codP.getText(), cantP.getText()};
            if(motP.getSelectedIndex() == 0){
                conect.insertarProdDevolucion(campos, true);
            } else{
                conect.insertarProdDevolucion(campos, false);
            }
            codP.setText("");
            cantP.setText("");
        }
        mostrarProdD();
    }//GEN-LAST:event_agPActionPerformed

    private void elPActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_elPActionPerformed
        if(tablaProdDev.getSelectedRow() == -1){
            Mise.JOption("Seleccione la fila de la tabla de Productos Devueltos que desea eliminar", "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        } else{
            String[] campos = {id_dev, "" + tablaProdDev.getValueAt(tablaProdDev.getSelectedRow(), 0)};
            conect.eliminarProdDevolucion(campos);
            mostrarProdD();
        }
    }//GEN-LAST:event_elPActionPerformed

    private void prodDevDialogWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_prodDevDialogWindowClosing
        if(tablaProdDev.getRowCount() == 0){
            int res = Mise.JOptionYesNo("No registró ningun producto, ¿seguro que desea cerrar esta ventana?"
                + "\n(al realizar esta acción la devolución no podrá guardarse)", "Cerrar ventana");
            if(res == 0){
                conect.eliminarDevolucion(id_dev);
                prodDevDialog.setVisible(false);
            }
        } else{
            prodDevDialog.setVisible(false);
            mostrarDev();
        }
    }//GEN-LAST:event_prodDevDialogWindowClosing

    private void hePActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hePActionPerformed
        prodDevDialog.setVisible(false);
        Mise.JOption("Cantidad a devolver: " + sumaTotalDev(), "Total devolución", javax.swing.JOptionPane.PLAIN_MESSAGE);
        mostrarDev();
    }//GEN-LAST:event_hePActionPerformed

    private void tablaProdVentaMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tablaProdVentaMouseClicked
        codP.setText("" + modeloVenDet.getValueAt(tablaProdVenta.getSelectedRow(), 0));
    }//GEN-LAST:event_tablaProdVentaMouseClicked

    public double sumaTotalDev(){
        double tot = 0.0;
        java.sql.ResultSet rs = conect.query("SELECT monto FROM devolucion_ventas WHERE id_devolucion='" + id_dev + "';");
        try{
            while(rs.next()){
                tot = rs.getDouble(1);
            }
        } catch(java.sql.SQLException e){
            System.out.println("Error al obtener el monto de la devolucion");
        }
        return tot;
    }
    
    public void mostrarDev(){
        Mise.limpiarTabla(modeloDev);
        java.sql.ResultSet rs = conect.query("SELECT * FROM devolucion_ventas;");
        try{
            while(rs.next()){
                modeloDev.addRow(new Object[]{rs.getString("id_devolucion"), rs.getString("id_venta"), rs.getDate("fecha_devolucion"), rs.getDouble("monto")});
            }
        } catch(java.sql.SQLException e){
            System.out.println("Error al mostrar la tabla Devoluciones");
        }
    }
    
    public void mostrarVen(String ins){
        if(ins.equals("")){
            ins = "SELECT * FROM venta;";
        }
        Mise.limpiarTabla(modeloVen);
        java.sql.ResultSet rs = conect.query(ins);
        try{
            while(rs.next()){
                modeloVen.addRow(new Object[]{rs.getString("id_venta"), rs.getString("id_empleado"), rs.getDouble("total_venta"), rs.getDate("fecha_venta")});
            }
        } catch(java.sql.SQLException e){
            System.out.println("Error al mostrar la tabla Ventas");
        }
    }
    
    public void mostrarVenDet(){
        Mise.limpiarTabla(modeloVenDet);
        java.sql.ResultSet rs = conect.query("SELECT * FROM venta_detalle WHERE id_venta='" + id_ven + "';");
        try{
            while(rs.next()){
                modeloVenDet.addRow(new Object[]{rs.getString("id_producto"), rs.getString("cantidad_producto")});
            }
        } catch(java.sql.SQLException e){
            System.out.println(e.getMessage());
        }
    }
    
    public void mostrarProdD(){
        Mise.limpiarTabla(modeloProdD);
        java.sql.ResultSet rs = conect.query("SELECT * FROM devolucion_ventas_detalle WHERE id_devolucion='"+ id_dev + "';");
        try{
            while(rs.next()){
                modeloProdD.addRow(new Object[]{rs.getString("id_producto"), rs.getString("motivo"), rs.getInt("cantidad"), rs.getDouble("total")});
            }
        } catch(java.sql.SQLException e){
            System.out.println("Error al mostrar la tabla Devoluciones");
        }
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton actC;
    private javax.swing.JButton agP;
    private javax.swing.JFormattedTextField buskVen;
    private javax.swing.JFormattedTextField cantP;
    private javax.swing.JFormattedTextField codP;
    private javax.swing.JButton elP;
    private javax.swing.JButton heP;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane7;
    public static javax.swing.JLabel labelinc;
    private javax.swing.JComboBox<String> motP;
    private javax.swing.JPanel panelRegProdC;
    private javax.swing.JDialog prodDevDialog;
    private javax.swing.JButton regC;
    private javax.swing.JLabel sumLabel;
    private javax.swing.JTable tablaD;
    private javax.swing.JTable tablaProdDev;
    private javax.swing.JTable tablaProdVenta;
    private javax.swing.JTable tablaV;
    private javax.swing.JDialog ventasBusDialog;
    // End of variables declaration//GEN-END:variables
}
