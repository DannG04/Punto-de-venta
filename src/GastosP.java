
import javax.swing.table.DefaultTableModel;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */

/**
 *
 * @author mayra
 */
public class GastosP extends javax.swing.JPanel {

    ConexionBD conect = new ConexionBD();
    String id_gasto = "";
    
    DefaultTableModel modeloG = new DefaultTableModel();
    
    public GastosP() {
        initComponents();
        modeloG = (DefaultTableModel)tablaG.getModel();
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

        actualizarDialog = new javax.swing.JDialog();
        jPanel3 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        desF1 = new javax.swing.JTextPane();
        monF1 = new javax.swing.JFormattedTextField();
        actG1 = new javax.swing.JButton();
        labelinc1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tablaG = new javax.swing.JTable();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        agG = new javax.swing.JButton();
        labelinc = new javax.swing.JLabel();
        actG = new javax.swing.JButton();
        elimG = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        desF = new javax.swing.JTextPane();
        monF = new javax.swing.JFormattedTextField();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();

        actualizarDialog.setTitle("Actualizar");
        actualizarDialog.setAlwaysOnTop(true);
        actualizarDialog.setMaximumSize(new java.awt.Dimension(400, 260));
        actualizarDialog.setMinimumSize(new java.awt.Dimension(400, 260));
        actualizarDialog.setModal(true);
        actualizarDialog.setPreferredSize(new java.awt.Dimension(400, 260));
        actualizarDialog.setResizable(false);
        actualizarDialog.getContentPane().setLayout(new java.awt.CardLayout());

        jPanel3.setLayout(new java.awt.GridBagLayout());

        jLabel4.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(78, 150, 150));
        jLabel4.setText("Descripción:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.ipadx = 16;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        jPanel3.add(jLabel4, gridBagConstraints);

        jLabel5.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(78, 150, 150));
        jLabel5.setText("Monto:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.ipadx = 16;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        jPanel3.add(jLabel5, gridBagConstraints);

        desF1.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        desF1.setPreferredSize(new java.awt.Dimension(200, 50));
        desF1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                desF1KeyTyped(evt);
            }
        });
        jScrollPane3.setViewportView(desF1);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        jPanel3.add(jScrollPane3, gridBagConstraints);

        monF1.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        monF1.setPreferredSize(new java.awt.Dimension(200, 35));
        monF1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                monF1KeyTyped(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        jPanel3.add(monF1, gridBagConstraints);

        actG1.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        actG1.setForeground(new java.awt.Color(78, 150, 150));
        actG1.setText("Actualizar");
        actG1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                actG1ActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        jPanel3.add(actG1, gridBagConstraints);

        labelinc1.setFont(new java.awt.Font("Noto Serif", 0, 12)); // NOI18N
        labelinc1.setForeground(new java.awt.Color(204, 0, 51));
        labelinc1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        labelinc1.setText(" ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        jPanel3.add(labelinc1, gridBagConstraints);

        actualizarDialog.getContentPane().add(jPanel3, "card2");

        actualizarDialog.setLocationRelativeTo(null);

        setLayout(new java.awt.BorderLayout());

        tablaG.setFont(new java.awt.Font("Noto Serif", 0, 18)); // NOI18N
        tablaG.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Gastos", "Empleado", "Fecha", "Descripción", "Monto"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane1.setViewportView(tablaG);

        add(jScrollPane1, java.awt.BorderLayout.CENTER);

        jLabel1.setFont(new java.awt.Font("Noto Serif", 1, 36)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(78, 150, 150));
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("Gastos");
        jPanel1.add(jLabel1);

        add(jPanel1, java.awt.BorderLayout.NORTH);

        jPanel2.setPreferredSize(new java.awt.Dimension(400, 341));
        jPanel2.setLayout(new java.awt.GridBagLayout());

        agG.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        agG.setForeground(new java.awt.Color(78, 150, 150));
        agG.setText("Agregar");
        agG.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                agGActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.insets = new java.awt.Insets(30, 10, 10, 10);
        jPanel2.add(agG, gridBagConstraints);

        labelinc.setFont(new java.awt.Font("Noto Serif", 0, 12)); // NOI18N
        labelinc.setForeground(new java.awt.Color(204, 0, 51));
        labelinc.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        labelinc.setText(" ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        jPanel2.add(labelinc, gridBagConstraints);

        actG.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        actG.setForeground(new java.awt.Color(78, 150, 150));
        actG.setText("Actualizar");
        actG.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                actGActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        jPanel2.add(actG, gridBagConstraints);

        elimG.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        elimG.setForeground(new java.awt.Color(78, 150, 150));
        elimG.setText("Eliminar");
        elimG.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                elimGActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        jPanel2.add(elimG, gridBagConstraints);

        desF.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        desF.setPreferredSize(new java.awt.Dimension(200, 100));
        desF.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                desFKeyTyped(evt);
            }
        });
        jScrollPane2.setViewportView(desF);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        jPanel2.add(jScrollPane2, gridBagConstraints);

        monF.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        monF.setPreferredSize(new java.awt.Dimension(200, 35));
        monF.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                monFKeyTyped(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        jPanel2.add(monF, gridBagConstraints);

        jLabel2.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(78, 150, 150));
        jLabel2.setText("Descripción:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.ipadx = 16;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        jPanel2.add(jLabel2, gridBagConstraints);

        jLabel3.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(78, 150, 150));
        jLabel3.setText("Monto:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.ipadx = 16;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        jPanel2.add(jLabel3, gridBagConstraints);

        add(jPanel2, java.awt.BorderLayout.EAST);
    }// </editor-fold>//GEN-END:initComponents

    private void agGActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_agGActionPerformed
        if(desF.getText().isEmpty() || monF.getText().isEmpty()){
            labelinc.setText("Llene todos los campos");
        } else{
            String[] campos = {Interfaz.idVendedor, desF.getText(), monF.getText()};
            conect.insertarGasto(campos);
            labelinc.setText("");
            mostrarTablaGastos();
            desF.setText("");
            monF.setText("");
        }
    }//GEN-LAST:event_agGActionPerformed

    private void actGActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_actGActionPerformed
        if(tablaG.getSelectedRow() == -1){
            labelinc.setText("Seleccione la fila que desea actualizar");
        } else{
            id_gasto = "" + tablaG.getValueAt(tablaG.getSelectedRow(), 0);
            desF1.setText("" + tablaG.getValueAt(tablaG.getSelectedRow(), 3));
            monF1.setText("" + tablaG.getValueAt(tablaG.getSelectedRow(), 4));
            labelinc.setText("");
            actualizarDialog.setVisible(true);
        }
    }//GEN-LAST:event_actGActionPerformed

    private void elimGActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_elimGActionPerformed
        if(tablaG.getSelectedRow() == -1){
            labelinc.setText("Seleccione la fila que desea eliminar");
        } else{
            id_gasto = "" + tablaG.getValueAt(tablaG.getSelectedRow(), 0);
            conect.inst("DELETE FROM gastos WHERE id_gasto='" + id_gasto + "';");
            labelinc.setText("");
            mostrarTablaGastos();
        }
    }//GEN-LAST:event_elimGActionPerformed

    private void desFKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_desFKeyTyped
        char letra = evt.getKeyChar();
        if(!Cake.letrasMayus(letra) && !Cake.numeros(letra) && !Cake.letrasMinus(letra) && !(Cake.inicioEspacios(letra))){
            evt.consume();
        }

        if(!(desF.getText().isEmpty())){
            if(Cake.espacios(desF.getText(), letra)){
                evt.consume();
            }
        }
        else{
            if(Cake.inicioEspacios(letra)){
                evt.consume();
            }
        }

        if(Cake.tamaño(desF.getText(), 50)){
            evt.consume();
        }
    }//GEN-LAST:event_desFKeyTyped

    private void monFKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_monFKeyTyped
        char letra = evt.getKeyChar();
        if(!Cake.numeros(letra) && !Cake.inicioPunto(letra)){
            evt.consume();
        }

        if(!(monF.getText().isEmpty())){
            if(Cake.hayPuntos(monF.getText()) && letra=='.'){
                evt.consume();
            } else{
                if(Cake.punto(monF.getText(), letra)){
                    evt.consume();
                }
            }
        } else{
            if(Cake.inicioPunto(letra)){
                evt.consume();
            }
        }

        if(Cake.tamaño(monF.getText(), 10)){
            evt.consume();
        }
    }//GEN-LAST:event_monFKeyTyped

    private void desF1KeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_desF1KeyTyped
        char letra = evt.getKeyChar();
        if(!Cake.letrasMayus(letra) && !Cake.numeros(letra) && !Cake.letrasMinus(letra) && !(Cake.inicioEspacios(letra))){
            evt.consume();
        }

        if(!(desF1.getText().isEmpty())){
            if(Cake.espacios(desF1.getText(), letra)){
                evt.consume();
            }
        }
        else{
            if(Cake.inicioEspacios(letra)){
                evt.consume();
            }
        }

        if(Cake.tamaño(desF1.getText(), 50)){
            evt.consume();
        }
    }//GEN-LAST:event_desF1KeyTyped

    private void monF1KeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_monF1KeyTyped
        char letra = evt.getKeyChar();
        if(!Cake.numeros(letra) && !Cake.inicioPunto(letra)){
            evt.consume();
        }

        if(!(monF1.getText().isEmpty())){
            if(Cake.hayPuntos(monF1.getText()) && letra=='.'){
                evt.consume();
            } else{
                if(Cake.punto(monF1.getText(), letra)){
                    evt.consume();
                }
            }
        } else{
            if(Cake.inicioPunto(letra)){
                evt.consume();
            }
        }

        if(Cake.tamaño(monF1.getText(), 10)){
            evt.consume();
        }
    }//GEN-LAST:event_monF1KeyTyped

    private void actG1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_actG1ActionPerformed
        if(desF1.getText().isEmpty() || monF1.getText().isEmpty()){
            labelinc1.setText("Llene todos los campos");
        } else{
            String[] campos = {desF1.getText(), monF1.getText()};
            conect.actualizarGasto(id_gasto, campos);
            labelinc1.setText("");
            actualizarDialog.setVisible(false);
            mostrarTablaGastos();
        }
    }//GEN-LAST:event_actG1ActionPerformed

    public void mostrarTablaGastos(){
        Mise.limpiarTabla(modeloG);
        java.sql.ResultSet rs = conect.query("SELECT * FROM gastos order by fecha_gasto;");
        try{
            while(rs.next()){
                modeloG.addRow(new Object[]{rs.getString("id_gasto"), rs.getString("id_empleado"), rs.getDate("fecha_gasto"), rs.getString("descripcion"), rs.getDouble("monto")});
            }
        } catch(java.sql.SQLException e){
            System.out.println("Error al mostrar la tabla gastos");
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton actG;
    private javax.swing.JButton actG1;
    private javax.swing.JDialog actualizarDialog;
    private javax.swing.JButton agG;
    private javax.swing.JTextPane desF;
    private javax.swing.JTextPane desF1;
    private javax.swing.JButton elimG;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    public static javax.swing.JLabel labelinc;
    public static javax.swing.JLabel labelinc1;
    private javax.swing.JFormattedTextField monF;
    private javax.swing.JFormattedTextField monF1;
    private javax.swing.JTable tablaG;
    // End of variables declaration//GEN-END:variables
}
