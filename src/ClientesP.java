
import javax.swing.table.DefaultTableModel;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */

/**
 *
 * @author 76905
 */
public class ClientesP extends javax.swing.JPanel {

    String idCli = "";
    DefaultTableModel modelo = new DefaultTableModel();
    ConexionBD conect = new ConexionBD();
    
    public ClientesP() {
        initComponents();
        modelo=(DefaultTableModel)tablaC.getModel();
        mostrarTabla();
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
        jLabel5 = new javax.swing.JLabel();
        curpCliente1 = new javax.swing.JFormattedTextField();
        jLabel6 = new javax.swing.JLabel();
        nomCliente1 = new javax.swing.JFormattedTextField();
        jLabel7 = new javax.swing.JLabel();
        numTel1 = new javax.swing.JFormattedTextField();
        labelinc1 = new javax.swing.JLabel();
        actC1 = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tablaC = new javax.swing.JTable();
        jPanel2 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        curpCliente = new javax.swing.JFormattedTextField();
        jLabel2 = new javax.swing.JLabel();
        nomCliente = new javax.swing.JFormattedTextField();
        jLabel3 = new javax.swing.JLabel();
        numTel = new javax.swing.JFormattedTextField();
        regC = new javax.swing.JButton();
        labelinc = new javax.swing.JLabel();
        actC = new javax.swing.JButton();
        elimC = new javax.swing.JButton();

        actualizarDialog.setTitle("Actualizar");
        actualizarDialog.setAlwaysOnTop(true);
        actualizarDialog.setMinimumSize(new java.awt.Dimension(550, 300));
        actualizarDialog.setModal(true);
        actualizarDialog.setResizable(false);
        actualizarDialog.getContentPane().setLayout(new java.awt.CardLayout());

        jPanel3.setLayout(new java.awt.GridBagLayout());

        jLabel5.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(78, 150, 150));
        jLabel5.setText("CURP:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.ipadx = 16;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        jPanel3.add(jLabel5, gridBagConstraints);

        curpCliente1.setFont(new java.awt.Font("Noto Serif", 0, 18)); // NOI18N
        curpCliente1.setPreferredSize(new java.awt.Dimension(250, 30));
        curpCliente1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                curpCliente1KeyTyped(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        jPanel3.add(curpCliente1, gridBagConstraints);

        jLabel6.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        jLabel6.setForeground(new java.awt.Color(78, 150, 150));
        jLabel6.setText("Nombre:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.ipadx = 16;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        jPanel3.add(jLabel6, gridBagConstraints);

        nomCliente1.setFont(new java.awt.Font("Noto Serif", 0, 18)); // NOI18N
        nomCliente1.setPreferredSize(new java.awt.Dimension(250, 30));
        nomCliente1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                nomCliente1KeyTyped(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        jPanel3.add(nomCliente1, gridBagConstraints);

        jLabel7.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        jLabel7.setForeground(new java.awt.Color(78, 150, 150));
        jLabel7.setText("Número de teléfono:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.ipadx = 16;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        jPanel3.add(jLabel7, gridBagConstraints);

        numTel1.setFont(new java.awt.Font("Noto Serif", 0, 18)); // NOI18N
        numTel1.setPreferredSize(new java.awt.Dimension(250, 30));
        numTel1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                numTel1KeyTyped(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        jPanel3.add(numTel1, gridBagConstraints);

        labelinc1.setFont(new java.awt.Font("Noto Serif", 0, 12)); // NOI18N
        labelinc1.setForeground(new java.awt.Color(204, 0, 51));
        labelinc1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        labelinc1.setText(" ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        jPanel3.add(labelinc1, gridBagConstraints);

        actC1.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        actC1.setForeground(new java.awt.Color(78, 150, 150));
        actC1.setText("Actualizar");
        actC1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                actC1ActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        jPanel3.add(actC1, gridBagConstraints);

        actualizarDialog.getContentPane().add(jPanel3, "card2");

        actualizarDialog.setLocationRelativeTo(null);

        setLayout(new java.awt.BorderLayout());

        jLabel1.setFont(new java.awt.Font("Noto Serif", 1, 36)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(78, 150, 150));
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("Registro de Clientes");
        jPanel1.add(jLabel1);

        add(jPanel1, java.awt.BorderLayout.NORTH);

        tablaC.setFont(new java.awt.Font("Noto Serif", 0, 18)); // NOI18N
        tablaC.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "CURP", "Nombre", "Teléfono", "Estatus"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane1.setViewportView(tablaC);

        add(jScrollPane1, java.awt.BorderLayout.CENTER);

        jPanel2.setLayout(new java.awt.GridBagLayout());

        jLabel4.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(78, 150, 150));
        jLabel4.setText("CURP:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.ipadx = 16;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        jPanel2.add(jLabel4, gridBagConstraints);

        curpCliente.setFont(new java.awt.Font("Noto Serif", 0, 18)); // NOI18N
        curpCliente.setPreferredSize(new java.awt.Dimension(250, 30));
        curpCliente.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                curpClienteKeyTyped(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        jPanel2.add(curpCliente, gridBagConstraints);

        jLabel2.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(78, 150, 150));
        jLabel2.setText("Nombre:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.ipadx = 16;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        jPanel2.add(jLabel2, gridBagConstraints);

        nomCliente.setFont(new java.awt.Font("Noto Serif", 0, 18)); // NOI18N
        nomCliente.setPreferredSize(new java.awt.Dimension(250, 30));
        nomCliente.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                nomClienteKeyTyped(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        jPanel2.add(nomCliente, gridBagConstraints);

        jLabel3.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(78, 150, 150));
        jLabel3.setText("Número de teléfono:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.ipadx = 16;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        jPanel2.add(jLabel3, gridBagConstraints);

        numTel.setFont(new java.awt.Font("Noto Serif", 0, 18)); // NOI18N
        numTel.setPreferredSize(new java.awt.Dimension(250, 30));
        numTel.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                numTelKeyTyped(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        jPanel2.add(numTel, gridBagConstraints);

        regC.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        regC.setForeground(new java.awt.Color(78, 150, 150));
        regC.setText("Registrar");
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

        elimC.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        elimC.setForeground(new java.awt.Color(78, 150, 150));
        elimC.setText("Baja");
        elimC.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                elimCActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        jPanel2.add(elimC, gridBagConstraints);

        add(jPanel2, java.awt.BorderLayout.LINE_END);
    }// </editor-fold>//GEN-END:initComponents

    private void regCActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_regCActionPerformed
        if(!nomCliente.getText().isEmpty() || !numTel.getText().isEmpty()){
            if(numTel.getText().length() == 10){
                if(curpCliente.getText().length() == 18){
                    String[] campos = {curpCliente.getText(), nomCliente.getText(), numTel.getText()};
                    conect.insertarCliente(campos);
                    mostrarTabla();
                    curpCliente.setText("");
                    nomCliente.setText("");
                    numTel.setText("");
                    labelinc.setText("");
                } else{
                    labelinc.setText("La CURP debe ser de 18 dígitos alfanumericos");
                }
            } else{
                labelinc.setText("El número telefónico debe ser de 10 dígitos");
            }
        } else{
            labelinc.setText("Debe llenar todos los campos");
        }
    }//GEN-LAST:event_regCActionPerformed

    private void nomClienteKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_nomClienteKeyTyped
        char letra = evt.getKeyChar();
        if(!Cake.letrasMayus(letra) && !Cake.numeros(letra) && !Cake.letrasMinus(letra) && !(Cake.inicioEspacios(letra))){
            evt.consume();
        }

        if(!(nomCliente.getText().isEmpty())){
            if(Cake.espacios(nomCliente.getText(), letra)){
                evt.consume();
            }
        }
        else{
            if(Cake.inicioEspacios(letra)){
                evt.consume();
            }
        }

        if(Cake.tamaño(nomCliente.getText(), 50)){
            evt.consume();
        }
    }//GEN-LAST:event_nomClienteKeyTyped

    private void numTelKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_numTelKeyTyped
        char letra = evt.getKeyChar();
        if(!Cake.numeros(letra)){
            evt.consume();
        }
        
        if(Cake.tamaño(numTel.getText(), 10)){
            evt.consume();
        }
    }//GEN-LAST:event_numTelKeyTyped

    private void curpClienteKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_curpClienteKeyTyped
        char letra = evt.getKeyChar();
        if(!Cake.letrasMayus(letra) && !Cake.numeros(letra)){
            evt.consume();
        }

        if(Cake.tamaño(curpCliente.getText(), 18)){
            evt.consume();
        }
    }//GEN-LAST:event_curpClienteKeyTyped

    private void actCActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_actCActionPerformed
        if(tablaC.getSelectedRow() != -1){
            idCli = "" + tablaC.getValueAt(tablaC.getSelectedRow(), 0);
            curpCliente1.setText(idCli);
            nomCliente1.setText("" + tablaC.getValueAt(tablaC.getSelectedRow(), 1));
            numTel1.setText("" + tablaC.getValueAt(tablaC.getSelectedRow(), 2));
            actualizarDialog.setVisible(true);
        } else{
            labelinc.setText("Seleccione la fila que desea actualizar");
        }
    }//GEN-LAST:event_actCActionPerformed

    private void curpCliente1KeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_curpCliente1KeyTyped
        char letra = evt.getKeyChar();
        if(!Cake.letrasMayus(letra) && !Cake.numeros(letra)){
            evt.consume();
        }

        if(Cake.tamaño(curpCliente1.getText(), 18)){
            evt.consume();
        }
    }//GEN-LAST:event_curpCliente1KeyTyped

    private void nomCliente1KeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_nomCliente1KeyTyped
        char letra = evt.getKeyChar();
        if(!Cake.letrasMayus(letra) && !Cake.numeros(letra) && !Cake.letrasMinus(letra) && !(Cake.inicioEspacios(letra))){
            evt.consume();
        }

        if(!(nomCliente1.getText().isEmpty())){
            if(Cake.espacios(nomCliente1.getText(), letra)){
                evt.consume();
            }
        }
        else{
            if(Cake.inicioEspacios(letra)){
                evt.consume();
            }
        }

        if(Cake.tamaño(nomCliente1.getText(), 50)){
            evt.consume();
        }
    }//GEN-LAST:event_nomCliente1KeyTyped

    private void numTel1KeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_numTel1KeyTyped
        char letra = evt.getKeyChar();
        if(!Cake.numeros(letra)){
            evt.consume();
        }
        
        if(Cake.tamaño(numTel1.getText(), 10)){
            evt.consume();
        }
    }//GEN-LAST:event_numTel1KeyTyped

    private void actC1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_actC1ActionPerformed
        if(!nomCliente1.getText().isEmpty() || !numTel1.getText().isEmpty()){
            if(numTel1.getText().length() == 10){
                if(curpCliente1.getText().length() == 18){
                    String[] campos = {curpCliente1.getText(), nomCliente1.getText(), numTel1.getText()};
                    conect.actualizarCliente(campos, idCli);
                    mostrarTabla();
                    actualizarDialog.setVisible(false);
                    curpCliente1.setText("");
                    nomCliente1.setText("");
                    numTel1.setText("");
                    labelinc1.setText("");
                } else{
                    labelinc1.setText("La CURP debe ser de 18 dígitos alfanumericos");
                }
            } else{
                labelinc1.setText("El número telefónico debe ser de 10 dígitos");
            }
        } else{
            labelinc1.setText("Debe llenar todos los campos");
        }
    }//GEN-LAST:event_actC1ActionPerformed

    private void elimCActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_elimCActionPerformed
        if(tablaC.getSelectedRow() != -1){
            Mise.JOptionYesNo("¿Está seguro que desea dar de baja a este cliente?", "Cliente");
        } else{
            labelinc.setText("Seleccione la fila que desea dar de baja");
        }
    }//GEN-LAST:event_elimCActionPerformed

    public void mostrarTabla(){
        Mise.limpiarTabla(modelo);
        java.sql.ResultSet rs = conect.query("SELECT * FROM cliente");
        if(rs != null){
            try{
                while(rs.next()){
                    modelo.addRow( new Object[]{rs.getString("id_cliente"), rs.getString("nombre"), rs.getString("telefono"), rs.getString("estatus")});
                }
            }catch(Exception e){
                System.out.println("Error al mostrar la tabla clientes");
            }
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton actC;
    private javax.swing.JButton actC1;
    private javax.swing.JDialog actualizarDialog;
    private javax.swing.JFormattedTextField curpCliente;
    private javax.swing.JFormattedTextField curpCliente1;
    private javax.swing.JButton elimC;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    public static javax.swing.JLabel labelinc;
    public static javax.swing.JLabel labelinc1;
    private javax.swing.JFormattedTextField nomCliente;
    private javax.swing.JFormattedTextField nomCliente1;
    private javax.swing.JFormattedTextField numTel;
    private javax.swing.JFormattedTextField numTel1;
    private javax.swing.JButton regC;
    private javax.swing.JTable tablaC;
    // End of variables declaration//GEN-END:variables
}
