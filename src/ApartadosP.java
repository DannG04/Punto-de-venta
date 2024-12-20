
import javax.swing.table.DefaultTableModel;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */

/**
 *
 * @author 76905
 */
public class ApartadosP extends javax.swing.JPanel {

    DefaultTableModel modelo = new DefaultTableModel();
    ConexionBD conect = new ConexionBD();
    boolean alertaP = false;
    String idAp = "";
    
    public ApartadosP() {
        initComponents();
        modelo=(DefaultTableModel)tablaAp.getModel();
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

        dialogAp = new javax.swing.JDialog();
        panelProducto = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        codp = new javax.swing.JFormattedTextField();
        jLabel8 = new javax.swing.JLabel();
        cantp = new javax.swing.JFormattedTextField();
        regPr = new javax.swing.JButton();
        regAp = new javax.swing.JButton();
        panelApartado = new javax.swing.JPanel();
        cantMinLabel = new javax.swing.JLabel();
        cantMinF = new javax.swing.JFormattedTextField();
        jLabel10 = new javax.swing.JLabel();
        cantPagF = new javax.swing.JFormattedTextField();
        jLabel11 = new javax.swing.JLabel();
        vigF = new javax.swing.JFormattedTextField();
        jLabel9 = new javax.swing.JLabel();
        idclientep = new javax.swing.JFormattedTextField();
        hechoB4 = new javax.swing.JButton();
        panelSald = new javax.swing.JPanel();
        jLabel12 = new javax.swing.JLabel();
        idApField = new javax.swing.JFormattedTextField();
        jLabel13 = new javax.swing.JLabel();
        cantSaldaF = new javax.swing.JFormattedTextField();
        jLabel14 = new javax.swing.JLabel();
        recSaldF = new javax.swing.JFormattedTextField();
        jLabel15 = new javax.swing.JLabel();
        cambCT = new javax.swing.JFormattedTextField();
        regSaldasion = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        reg = new javax.swing.JButton();
        sald = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        buskAp = new javax.swing.JFormattedTextField();
        botonBusk = new javax.swing.JToggleButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        tablaAp = new javax.swing.JTable();

        dialogAp.setTitle("Registro de Apartados");
        dialogAp.setAlwaysOnTop(true);
        dialogAp.setMinimumSize(new java.awt.Dimension(400, 310));
        dialogAp.setModal(true);
        dialogAp.setResizable(false);
        dialogAp.setSize(new java.awt.Dimension(400, 310));
        dialogAp.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                dialogApWindowClosing(evt);
            }
        });
        dialogAp.getContentPane().setLayout(new java.awt.CardLayout());

        panelProducto.setLayout(new java.awt.GridLayout(0, 1));

        jLabel3.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(78, 150, 150));
        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel3.setText("Código del Producto:");
        panelProducto.add(jLabel3);

        codp.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        codp.setPreferredSize(new java.awt.Dimension(200, 35));
        codp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                codpActionPerformed(evt);
            }
        });
        codp.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                codpKeyTyped(evt);
            }
        });
        panelProducto.add(codp);

        jLabel8.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        jLabel8.setForeground(new java.awt.Color(78, 150, 150));
        jLabel8.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel8.setText("Cantidad: ");
        panelProducto.add(jLabel8);

        cantp.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        cantp.setPreferredSize(new java.awt.Dimension(200, 35));
        cantp.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                cantpKeyTyped(evt);
            }
        });
        panelProducto.add(cantp);

        regPr.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        regPr.setForeground(new java.awt.Color(78, 150, 150));
        regPr.setText("Registrar producto");
        regPr.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                regPrActionPerformed(evt);
            }
        });
        panelProducto.add(regPr);

        regAp.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        regAp.setForeground(new java.awt.Color(78, 150, 150));
        regAp.setText("Registrar apartado");
        regAp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                regApActionPerformed(evt);
            }
        });
        panelProducto.add(regAp);

        dialogAp.getContentPane().add(panelProducto, "card2");

        panelApartado.setLayout(new java.awt.GridLayout(0, 1));

        cantMinLabel.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        cantMinLabel.setForeground(new java.awt.Color(78, 150, 150));
        cantMinLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        cantMinLabel.setText("Cantidad mínima a pagar:");
        panelApartado.add(cantMinLabel);

        cantMinF.setEditable(false);
        cantMinF.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        cantMinF.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        cantMinF.setPreferredSize(new java.awt.Dimension(200, 35));
        panelApartado.add(cantMinF);

        jLabel10.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        jLabel10.setForeground(new java.awt.Color(78, 150, 150));
        jLabel10.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel10.setText("Cantidad Pagada:");
        panelApartado.add(jLabel10);

        cantPagF.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        cantPagF.setFont(new java.awt.Font("Noto Serif", 0, 18)); // NOI18N
        cantPagF.setPreferredSize(new java.awt.Dimension(150, 30));
        panelApartado.add(cantPagF);

        jLabel11.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        jLabel11.setForeground(new java.awt.Color(78, 150, 150));
        jLabel11.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel11.setText("Vigencia:");
        panelApartado.add(jLabel11);

        vigF.setEditable(false);
        vigF.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        vigF.setFont(new java.awt.Font("Noto Serif", 0, 18)); // NOI18N
        vigF.setPreferredSize(new java.awt.Dimension(150, 30));
        panelApartado.add(vigF);

        jLabel9.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        jLabel9.setForeground(new java.awt.Color(78, 150, 150));
        jLabel9.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel9.setText("ID cliente:");
        panelApartado.add(jLabel9);

        idclientep.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        idclientep.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        idclientep.setPreferredSize(new java.awt.Dimension(200, 35));
        idclientep.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                idclientepKeyTyped(evt);
            }
        });
        panelApartado.add(idclientep);

        hechoB4.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        hechoB4.setForeground(new java.awt.Color(78, 150, 150));
        hechoB4.setText("Registrar");
        hechoB4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                hechoB4ActionPerformed(evt);
            }
        });
        panelApartado.add(hechoB4);

        dialogAp.getContentPane().add(panelApartado, "card3");

        panelSald.setLayout(new java.awt.GridLayout(0, 1));

        jLabel12.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        jLabel12.setForeground(new java.awt.Color(78, 150, 150));
        jLabel12.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel12.setText("ID Apartado:");
        panelSald.add(jLabel12);

        idApField.setEditable(false);
        idApField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        idApField.setFont(new java.awt.Font("Noto Serif", 0, 18)); // NOI18N
        idApField.setPreferredSize(new java.awt.Dimension(150, 30));
        idApField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                idApFieldKeyReleased(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                idApFieldKeyTyped(evt);
            }
        });
        panelSald.add(idApField);

        jLabel13.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        jLabel13.setForeground(new java.awt.Color(78, 150, 150));
        jLabel13.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel13.setText("Salda con:");
        panelSald.add(jLabel13);

        cantSaldaF.setEditable(false);
        cantSaldaF.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        cantSaldaF.setFont(new java.awt.Font("Noto Serif", 0, 18)); // NOI18N
        cantSaldaF.setPreferredSize(new java.awt.Dimension(150, 30));
        panelSald.add(cantSaldaF);

        jLabel14.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        jLabel14.setForeground(new java.awt.Color(78, 150, 150));
        jLabel14.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel14.setText("Recibido:");
        panelSald.add(jLabel14);

        recSaldF.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        recSaldF.setFont(new java.awt.Font("Noto Serif", 0, 18)); // NOI18N
        recSaldF.setPreferredSize(new java.awt.Dimension(150, 30));
        recSaldF.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                recSaldFKeyReleased(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                recSaldFKeyTyped(evt);
            }
        });
        panelSald.add(recSaldF);

        jLabel15.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        jLabel15.setForeground(new java.awt.Color(78, 150, 150));
        jLabel15.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel15.setText("Cambio: $");
        panelSald.add(jLabel15);

        cambCT.setEditable(false);
        cambCT.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        cambCT.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        cambCT.setPreferredSize(new java.awt.Dimension(200, 35));
        panelSald.add(cambCT);

        regSaldasion.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        regSaldasion.setForeground(new java.awt.Color(78, 150, 150));
        regSaldasion.setText("Saldar");
        regSaldasion.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                regSaldasionActionPerformed(evt);
            }
        });
        panelSald.add(regSaldasion);

        dialogAp.getContentPane().add(panelSald, "card4");

        dialogAp.setLocationRelativeTo(null);

        setLayout(new java.awt.BorderLayout());

        jPanel2.setLayout(new java.awt.GridBagLayout());

        reg.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        reg.setForeground(new java.awt.Color(78, 150, 150));
        reg.setText("Registrar");
        reg.setPreferredSize(new java.awt.Dimension(125, 35));
        reg.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                regActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.RELATIVE;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        jPanel2.add(reg, gridBagConstraints);

        sald.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        sald.setForeground(new java.awt.Color(78, 150, 150));
        sald.setText("Saldar");
        sald.setPreferredSize(new java.awt.Dimension(125, 35));
        sald.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saldActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        jPanel2.add(sald, gridBagConstraints);

        add(jPanel2, java.awt.BorderLayout.EAST);

        jPanel1.setLayout(new java.awt.GridBagLayout());

        jLabel7.setFont(new java.awt.Font("Noto Serif", 1, 36)); // NOI18N
        jLabel7.setForeground(new java.awt.Color(78, 150, 150));
        jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel7.setText("Sistema de Apartados");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        jPanel1.add(jLabel7, gridBagConstraints);

        buskAp.setFont(new java.awt.Font("Noto Serif", 0, 18)); // NOI18N
        buskAp.setPreferredSize(new java.awt.Dimension(600, 30));
        buskAp.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                buskApKeyReleased(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                buskApKeyTyped(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 0);
        jPanel1.add(buskAp, gridBagConstraints);

        botonBusk.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        botonBusk.setForeground(new java.awt.Color(78, 150, 150));
        botonBusk.setText("Buscar");
        botonBusk.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botonBuskActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        jPanel1.add(botonBusk, gridBagConstraints);

        add(jPanel1, java.awt.BorderLayout.NORTH);

        tablaAp.setFont(new java.awt.Font("Noto Serif", 0, 18)); // NOI18N
        tablaAp.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ID", "Cant. Pagada", "Cant. Restante", "Fecha"
            }
        ));
        jScrollPane1.setViewportView(tablaAp);

        add(jScrollPane1, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void regActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_regActionPerformed
        paneles(1);
        idAp = generarID();
        conect.inst("INSERT INTO apartado(idapartado) VALUES('" + idAp +"');");
        dialogAp.setVisible(true);
    }//GEN-LAST:event_regActionPerformed

    private void saldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saldActionPerformed
        paneles(3);
        if(tablaAp.getSelectedRow() != -1){
            mostrarDatSald();
            dialogAp.setVisible(true);
        } else{
            javax.swing.JOptionPane.showMessageDialog(null, "Seleccione la fila que desea saldar", "Error",javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_saldActionPerformed

    private void codpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_codpActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_codpActionPerformed

    private void codpKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_codpKeyTyped
        char letra = evt.getKeyChar();
        if(!Cake.letrasMayus(letra) && !Cake.numeros(letra)){
            evt.consume();
        }
    }//GEN-LAST:event_codpKeyTyped

    private void cantpKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_cantpKeyTyped
        char letra = evt.getKeyChar();
        if(!Cake.numeros(letra)){
            evt.consume();
        }

        if(Cake.tamaño(cantp.getText(), 10)){
            evt.consume();
        }
    }//GEN-LAST:event_cantpKeyTyped

    private void regApActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_regApActionPerformed
        if(consultarAp()){
            cantMinPago();
            vigenciaFut();
            paneles(2);
        } else{
            javax.swing.JOptionPane.showMessageDialog(null, "LLene todos los campos", "Error",javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_regApActionPerformed

    private void hechoB4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hechoB4ActionPerformed
        if(!cantPagF.getText().isEmpty() && !vigF.getText().isEmpty() && !idclientep.getText().isEmpty()){
            if(Double.parseDouble(cantPagF.getText()) >= Double.parseDouble(cantMinF.getText())){
                registrarApartado();
                mostrarTabla();
                dialogAp.setVisible(false);
            } else{
                javax.swing.JOptionPane.showMessageDialog(null, "El pago debe ser mayor o igual a la cantidad mínima a pagar", "Error",javax.swing.JOptionPane.ERROR_MESSAGE);
            }
        } else{
            javax.swing.JOptionPane.showMessageDialog(null, "LLene todos los campos", "Error",javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_hechoB4ActionPerformed

    private void regSaldasionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_regSaldasionActionPerformed
        if(!recSaldF.getText().isEmpty()){
            if(Double.parseDouble(recSaldF.getText()) >= Double.parseDouble(cantSaldaF.getText())){
                saldarProdAp();
                mostrarTabla();
                dialogAp.setVisible(false);
            }
        } else{
            javax.swing.JOptionPane.showMessageDialog(null, "LLene todos los campos", "Error",javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_regSaldasionActionPerformed

    private void idclientepKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_idclientepKeyTyped
        char letra = evt.getKeyChar();
        if(!Cake.letrasMayus(letra) && !Cake.numeros(letra)){
            evt.consume();
        }
    }//GEN-LAST:event_idclientepKeyTyped

    private void regPrActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_regPrActionPerformed
        if(!codp.getText().isEmpty() && !cantp.getText().isEmpty()){
            registrarProdA();
            codp.setText("");
            cantp.setText("");
        } else{
            javax.swing.JOptionPane.showMessageDialog(null, "LLene todos los campos", "Error",javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_regPrActionPerformed

    private void dialogApWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_dialogApWindowClosing
        verificarAp();
    }//GEN-LAST:event_dialogApWindowClosing

    private void idApFieldKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_idApFieldKeyTyped
        char letra = evt.getKeyChar();
        if(!Cake.numeros(letra)){
            evt.consume();
        }
    }//GEN-LAST:event_idApFieldKeyTyped

    private void idApFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_idApFieldKeyReleased
        
    }//GEN-LAST:event_idApFieldKeyReleased

    private void buskApKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_buskApKeyReleased
        if(evt.getKeyChar() == java.awt.event.KeyEvent.VK_BACK_SPACE){
            botonBusk.setSelected(false);
            mostrarTabla();
        }
    }//GEN-LAST:event_buskApKeyReleased

    private void buskApKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_buskApKeyTyped
        char letra = evt.getKeyChar();
        if(!Cake.letrasMayus(letra) && !Cake.letrasMinus(letra) && !Cake.numeros(letra) && !(Cake.inicioEspacios(letra))){
            evt.consume();
        }

        if(!(buskAp.getText().isEmpty())){
            if(Cake.espacios(buskAp.getText(), letra)){
                evt.consume();
            }
        }
        else{
            if(Cake.inicioEspacios(letra)){
                evt.consume();
            }
        }

        if(Cake.tamaño(buskAp.getText(), 30)){
            evt.consume();
        }
    }//GEN-LAST:event_buskApKeyTyped

    private void botonBuskActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botonBuskActionPerformed
        if(buskAp.getText().length()==14){
            buscarAp();
        }
    }//GEN-LAST:event_botonBuskActionPerformed

    private void recSaldFKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_recSaldFKeyTyped
        char letra = evt.getKeyChar();
        if(!Cake.numeros(letra)){
            evt.consume();
        }
    }//GEN-LAST:event_recSaldFKeyTyped

    private void recSaldFKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_recSaldFKeyReleased
        if(!recSaldF.getText().isEmpty()){
            cambCT.setText("" + cambioT(recSaldF.getText()));
        }
    }//GEN-LAST:event_recSaldFKeyReleased

    public Double cambioT(String palabra){
        Double precT = Double.valueOf(cantSaldaF.getText());
        Double cambioNum = Double.valueOf(palabra);
        if(cambioNum >= precT){
            cambioNum = cambioNum-precT;
        } else {
            cambioNum = 0.0;
        }
        return cambioNum;
    }
    
    public final void mostrarTabla(){
        limpiarTabla();
        String inst = "SELECT * FROM apartado";
        llenarTabla(inst);
    }
    
    public void llenarTabla(String instruccion){
        java.sql.ResultSet rs = conect.query(instruccion);
        if(rs != null){
            try{
                while(rs.next()){
                    modelo.addRow( new Object[]{rs.getString("idapartado"), rs.getDouble("cantidad_dada"), rs.getDouble("cantidad_falta"), rs.getDate("fecha_limite")});
                }
            }catch(Exception e){
                System.out.println("Ha ocurrido un error");
            }
        }
    }
    
    public void limpiarTabla(){
        int a=modelo.getRowCount();    
        while(a!=0){ 
            if(a!=0)
                modelo.removeRow(0);                      
            a=modelo.getRowCount();
        }
    }
    
    public void paneles(int a){
        panelProducto.setVisible(false);
        panelApartado.setVisible(false);
        panelSald.setVisible(false);
        switch(a){
            case 1:
                panelProducto.setVisible(true);
                break;
            case 2:
                panelApartado.setVisible(true);
                break;
            case 3:
                panelSald.setVisible(true);
                break;
            default:
                break;
        }
    }
    
    public void verificarAp(){
        java.sql.ResultSet rs = conect.query("SELECT * FROM apartado WHERE idapartado='" + idAp + "';");
        
        try{
            while(rs.next()){
                if(rs.getString("idcliente") == null){
                    String inst = "DELETE FROM detalleapartado WHERE idapartado='" + idAp + "';";
                    System.out.println(inst);
                    conect.inst(inst);
                    inst = "DELETE FROM apartado WHERE idapartado='" + idAp + "';";
                    System.out.println(inst);
                    conect.inst(inst);
                }
            }
        }catch(Exception e){
            System.out.println("Error al consultar producto");
        }
    }
    
    public void registrarProdA(){
        String elemento = codp.getText();
        int cant = Integer.parseInt(cantp.getText());
        java.sql.ResultSet rs = conect.query("SELECT * FROM producto WHERE idproducto='" + elemento + "';");
        
        try{
            while(rs.next()){
                String aux;
                if(cant>12){
                    aux = "precio_mayoreo";
                } else{
                    aux = "precio_menudeo";
                }
                Double precio = rs.getDouble(aux) * cant;
                
                String inst = "INSERT INTO detalleapartado VALUES(" + cant +", " + precio + ", '" + elemento +"', '" + idAp + "');";
                System.out.println(inst);
                conect.inst(inst);
            }
        }catch(Exception e){
            System.out.println("Error al consultar producto");
        }
    }
    
    public void saldarProdAp(){
        String elemento = idApField.getText();
        Double pagoDeu = (Double)tablaAp.getValueAt(tablaAp.getSelectedRow(), 1);
        Double pagoEnt = (Double)tablaAp.getValueAt(tablaAp.getSelectedRow(), 2) + pagoDeu;
        pagoDeu = 0.0;
        String estao = "Entregado";
        
        String instruct = "UPDATE apartado SET cantidad_falta=" + pagoDeu +", cantidad_dada=" + pagoEnt +", "
                + "estado='" + estao +"' ";
        instruct = instruct + "WHERE idapartado='" + elemento +"';";
        System.out.println(instruct);
        conect.inst(instruct);
    }
    
    public void mostrarDatSald(){
        String elemento = tablaAp.getValueAt(tablaAp.getSelectedRow(), 0).toString();
        String cantpag = tablaAp.getValueAt(tablaAp.getSelectedRow(), 2).toString();
        idApField.setText(elemento);
        cantSaldaF.setText(cantpag);
        cambCT.setText("0.0");
    }
    
    public boolean consultarAp(){
        boolean band = false;
        java.sql.ResultSet rs = conect.query("SELECT * FROM detalleapartado WHERE idapartado='" + idAp + "';");
        try{
            if(rs.isBeforeFirst()){
                band = true;
            }
        } catch(Exception e){
            System.out.println("Error");
        }
        return band;
    }
    
    
    public void registrarApartado(){
        Double pagoEnt = Double.valueOf(cantPagF.getText());
        Double pagoDeu = Double.parseDouble(cantMinF.getText()) * 2;
        pagoDeu = pagoDeu-pagoEnt;
        String estao = "Vigente";
        String fechaL= vigF.getText();
        String idC = idclientep.getText();
        
        String instruct = "UPDATE apartado SET cantidad_falta=" + pagoDeu +", cantidad_dada=" + pagoEnt +", "
                + "estado='" + estao +"', idcliente='" + idC +"', fecha_limite='" + fechaL + "' ";
        instruct = instruct + "WHERE idapartado='" + idAp +"';";
        System.out.println(instruct);
        conect.inst(instruct);
    }
    
    public void cantMinPago(){
        java.sql.ResultSet rs = conect.query("SELECT SUM(precio) FROM detalleapartado WHERE idapartado='" + idAp + "';");
        try{
            while(rs.next()){
                Double prec = rs.getDouble(1);
                prec = prec/2;
                cantMinF.setText("" + prec);
                //cantidadInventario(true, cod, cant);
            }
        } catch(Exception e){
            System.out.println("Error al consultar el producto");
        }
    }
    
    public void buscarAp(){
        String  ins = "SELECT * FROM apartado WHERE idapartado='" + buskAp.getText() + "';";
        limpiarTabla();
        llenarTabla(ins);
    }
    
    public void vigenciaFut(){
        java.time.LocalDateTime fecha = java.time.LocalDateTime.now();
        java.time.LocalDateTime fechaFut = fecha.plusDays(30);
        java.time.format.DateTimeFormatter formateado = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String fechaVig = fechaFut.format(formateado);
        vigF.setText(fechaVig);
    }
    
    public void cantidadInventario(boolean op, String idprod, int cantTem){
        String instruccion = "SELECT cantidad FROM producto WHERE idproducto='" + idprod + "';";
        java.sql.ResultSet rs = conect.query(instruccion);
        if(rs != null){
            try{
                while(rs.next()){
                    int x = 0;
                    int cantBD = rs.getInt("cantidad");

                    instruccion = "UPDATE producto SET cantidad=";
                    if(op){//SI ES TRUE SE DECREMENTA LA CANTIDAD EN EL INVENTARIO
                        x = cantBD-cantTem;
                    } else{//SI ES FALSE SE INCREMENTA LA CANTIDAD
                        x = cantBD+cantTem;
                    }
                    instruccion = instruccion + x + " WHERE idproducto='" + idprod + "';";
                    if(cantBD<12)
                        alertaP = true;
                    System.out.println(instruccion);
                    if(conect.inst(instruccion))
                        System.out.println("Inventario Actualizado");
                    else
                        System.out.println("No se actualizo el inventario");
                }
                
            }catch(Exception e){
                System.out.println("Error");
            }
            
        }
    }
    
    public String generarHora(String formato){
        java.time.LocalDateTime ahora = java.time.LocalDateTime.now();
        java.time.format.DateTimeFormatter formateado = java.time.format.DateTimeFormatter.ofPattern(formato);
        return ahora.format(formateado);
    }
    
    public String generarID(){
        String hora = generarHora("HH:mm:ss");
        String fecha = generarHora("yyyy-MM-dd");
        String id = "";
        fecha = fecha+hora;
        for (int i=0; i<fecha.length();i++) {
            if(Character.isDigit(fecha.charAt(i))){
                id = id + fecha.charAt(i);
            }
        }
        System.out.println(id);
        return id;
    }
    

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JToggleButton botonBusk;
    private javax.swing.JFormattedTextField buskAp;
    private javax.swing.JFormattedTextField cambCT;
    private javax.swing.JFormattedTextField cantMinF;
    private javax.swing.JLabel cantMinLabel;
    private javax.swing.JFormattedTextField cantPagF;
    private javax.swing.JFormattedTextField cantSaldaF;
    private javax.swing.JFormattedTextField cantp;
    private javax.swing.JFormattedTextField codp;
    private javax.swing.JDialog dialogAp;
    private javax.swing.JButton hechoB4;
    private javax.swing.JFormattedTextField idApField;
    private javax.swing.JFormattedTextField idclientep;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPanel panelApartado;
    private javax.swing.JPanel panelProducto;
    private javax.swing.JPanel panelSald;
    private javax.swing.JFormattedTextField recSaldF;
    private javax.swing.JButton reg;
    private javax.swing.JButton regAp;
    private javax.swing.JButton regPr;
    private javax.swing.JButton regSaldasion;
    private javax.swing.JButton sald;
    private javax.swing.JTable tablaAp;
    private javax.swing.JFormattedTextField vigF;
    // End of variables declaration//GEN-END:variables
}
