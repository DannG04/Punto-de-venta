
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
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
    VentasP dialog=new VentasP();
    
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
        Registro = new javax.swing.JDialog();
        jLabel8 = new javax.swing.JLabel();
        curpCliente2 = new javax.swing.JFormattedTextField();
        jLabel9 = new javax.swing.JLabel();
        nomCliente2 = new javax.swing.JFormattedTextField();
        jLabel10 = new javax.swing.JLabel();
        numTel2 = new javax.swing.JFormattedTextField();
        regC1 = new javax.swing.JButton();
        labelinc2 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tablaC = new javax.swing.JTable();
        jPanel2 = new javax.swing.JPanel();
        regC = new javax.swing.JButton();
        actC = new javax.swing.JButton();
        alta = new javax.swing.JButton();
        elimC1 = new javax.swing.JButton();

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

        Registro.setAlwaysOnTop(true);
        Registro.setMinimumSize(new java.awt.Dimension(520, 250));
        Registro.setModal(true);
        Registro.setPreferredSize(new java.awt.Dimension(520, 250));
        Registro.getContentPane().setLayout(new java.awt.GridBagLayout());
        Registro.setLocationRelativeTo(null);

        jLabel8.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        jLabel8.setForeground(new java.awt.Color(78, 150, 150));
        jLabel8.setText("CURP:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.ipadx = 16;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        Registro.getContentPane().add(jLabel8, gridBagConstraints);

        curpCliente2.setFont(new java.awt.Font("Noto Serif", 0, 18)); // NOI18N
        curpCliente2.setMinimumSize(new java.awt.Dimension(250, 30));
        curpCliente2.setPreferredSize(new java.awt.Dimension(250, 30));
        curpCliente2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                curpCliente2ActionPerformed(evt);
            }
        });
        curpCliente2.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                curpCliente2KeyTyped(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        Registro.getContentPane().add(curpCliente2, gridBagConstraints);

        jLabel9.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        jLabel9.setForeground(new java.awt.Color(78, 150, 150));
        jLabel9.setText("Nombre:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.ipadx = 16;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        Registro.getContentPane().add(jLabel9, gridBagConstraints);

        nomCliente2.setFont(new java.awt.Font("Noto Serif", 0, 18)); // NOI18N
        nomCliente2.setMinimumSize(new java.awt.Dimension(250, 30));
        nomCliente2.setPreferredSize(new java.awt.Dimension(250, 30));
        nomCliente2.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                nomCliente2KeyTyped(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        Registro.getContentPane().add(nomCliente2, gridBagConstraints);

        jLabel10.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        jLabel10.setForeground(new java.awt.Color(78, 150, 150));
        jLabel10.setText("Número de teléfono:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.ipadx = 16;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        Registro.getContentPane().add(jLabel10, gridBagConstraints);

        numTel2.setFont(new java.awt.Font("Noto Serif", 0, 18)); // NOI18N
        numTel2.setMinimumSize(new java.awt.Dimension(250, 30));
        numTel2.setPreferredSize(new java.awt.Dimension(250, 30));
        numTel2.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                numTel2KeyTyped(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        Registro.getContentPane().add(numTel2, gridBagConstraints);

        regC1.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        regC1.setForeground(new java.awt.Color(78, 150, 150));
        regC1.setText("Registrar");
        regC1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                regC1ActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.insets = new java.awt.Insets(30, 10, 10, 10);
        Registro.getContentPane().add(regC1, gridBagConstraints);

        labelinc2.setFont(new java.awt.Font("Noto Serif", 0, 12)); // NOI18N
        labelinc2.setForeground(new java.awt.Color(204, 0, 51));
        labelinc2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        labelinc2.setText(" ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        Registro.getContentPane().add(labelinc2, gridBagConstraints);

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

        alta.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        alta.setForeground(new java.awt.Color(78, 150, 150));
        alta.setText("Alta");
        alta.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                altaActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = 2;
        jPanel2.add(alta, gridBagConstraints);

        elimC1.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        elimC1.setForeground(new java.awt.Color(78, 150, 150));
        elimC1.setText("Baja");
        elimC1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                elimC1ActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        jPanel2.add(elimC1, gridBagConstraints);

        add(jPanel2, java.awt.BorderLayout.LINE_END);
    }// </editor-fold>//GEN-END:initComponents

    private void regCActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_regCActionPerformed
        Registro.setVisible(true);
        labelinc2.setText("");
    }//GEN-LAST:event_regCActionPerformed

    private void actCActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_actCActionPerformed
        if(tablaC.getSelectedRow() != -1){
            idCli = "" + tablaC.getValueAt(tablaC.getSelectedRow(), 0);
            curpCliente1.setText(idCli);
            nomCliente1.setText("" + tablaC.getValueAt(tablaC.getSelectedRow(), 1));
            numTel1.setText("" + tablaC.getValueAt(tablaC.getSelectedRow(), 2));
            actualizarDialog.setVisible(true);
        } else{
            Mise.JOption("Seleccione la fila que desea actualizar", "Error", JOptionPane.ERROR_MESSAGE);
            labelinc1.setText("Seleccione la fila que desea actualizar");
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

    private void altaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_altaActionPerformed
        if(tablaC.getSelectedRow() != -1){
            idCli = "" + tablaC.getValueAt(tablaC.getSelectedRow(), 0);
            int res = Mise.JOptionYesNo("¿Está seguro que desea dar de alta a este cliente?", "Cliente");
            if(res == 0){
                conect.inactivarCliente(idCli, false);
                mostrarTabla();
            }
        } else{
            Mise.JOption("Seleccione la fila que desea dar de alta", "Error",JOptionPane.ERROR_MESSAGE );
        }
    }//GEN-LAST:event_altaActionPerformed

    private void elimC1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_elimC1ActionPerformed
        if(tablaC.getSelectedRow() != -1){
            idCli = "" + tablaC.getValueAt(tablaC.getSelectedRow(), 0);
            int res = Mise.JOptionYesNo("¿Está seguro que desea dar de baja a este cliente?", "Cliente");
            if(res == 0){
                conect.inactivarCliente(idCli, true);
                mostrarTabla();
            }
        } else{
            Mise.JOption("Seleccione la fila que desea dar de baja", "Error",JOptionPane.ERROR_MESSAGE );
        }
    }//GEN-LAST:event_elimC1ActionPerformed

    private void regC1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_regC1ActionPerformed
        if(!nomCliente2.getText().isEmpty() || !numTel2.getText().isEmpty()){
            if(numTel2.getText().length() == 10){
                if(curpCliente2.getText().length() == 18){
                    String[] campos = {curpCliente2.getText(), nomCliente2.getText(), numTel2.getText()};
                    if(conect.insertarCliente(campos)){
                        mostrarTabla();
                        curpCliente2.setText("");
                        nomCliente2.setText("");
                        numTel2.setText("");
                        labelinc2.setText("");
                        Registro.setVisible(false);
                    }
                } else{
                    labelinc2.setText("La CURP debe ser de 18 dígitos alfanumericos");
                }
            } else{
                labelinc2.setText("El número telefónico debe ser de 10 dígitos");
            }
        } else{
            labelinc2.setText("Debe llenar todos los campos");
        }
    }//GEN-LAST:event_regC1ActionPerformed

    private void curpCliente2KeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_curpCliente2KeyTyped
        // TODO add your handling code here:
        char letra = evt.getKeyChar();
        if(!Cake.letrasMayus(letra) && !Cake.numeros(letra)){
            evt.consume();
        }

        if(Cake.tamaño(curpCliente2.getText(), 18)){
            evt.consume();
        }
    }//GEN-LAST:event_curpCliente2KeyTyped

    private void nomCliente2KeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_nomCliente2KeyTyped
        // TODO add your handling code here:
        char letra = evt.getKeyChar();
        if(!Cake.letrasMayus(letra) && !Cake.numeros(letra) && !Cake.letrasMinus(letra) && !(Cake.inicioEspacios(letra))){
            evt.consume();
        }

        if(!(nomCliente2.getText().isEmpty())){
            if(Cake.espacios(nomCliente2.getText(), letra)){
                evt.consume();
            }
        }
        else{
            if(Cake.inicioEspacios(letra)){
                evt.consume();
            }
        }

        if(Cake.tamaño(nomCliente2.getText(), 50)){
            evt.consume();
        }
    }//GEN-LAST:event_nomCliente2KeyTyped

    private void numTel2KeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_numTel2KeyTyped
        // TODO add your handling code here:
        char letra = evt.getKeyChar();
        if(!Cake.numeros(letra)){
            evt.consume();
        }
        
        if(Cake.tamaño(numTel2.getText(), 10)){
            evt.consume();
        }
    }//GEN-LAST:event_numTel2KeyTyped

    private void curpCliente2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_curpCliente2ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_curpCliente2ActionPerformed

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
    public static javax.swing.JDialog Registro;
    private javax.swing.JButton actC;
    private javax.swing.JButton actC1;
    private javax.swing.JDialog actualizarDialog;
    private javax.swing.JButton alta;
    private javax.swing.JFormattedTextField curpCliente1;
    private javax.swing.JFormattedTextField curpCliente2;
    private javax.swing.JButton elimC1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    public static javax.swing.JLabel labelinc1;
    public static javax.swing.JLabel labelinc2;
    private javax.swing.JFormattedTextField nomCliente1;
    private javax.swing.JFormattedTextField nomCliente2;
    private javax.swing.JFormattedTextField numTel1;
    private javax.swing.JFormattedTextField numTel2;
    private javax.swing.JButton regC;
    private javax.swing.JButton regC1;
    private javax.swing.JTable tablaC;
    // End of variables declaration//GEN-END:variables
}
