
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
public class ProveedoresP extends javax.swing.JPanel {
    int idProv = -1;
    DefaultTableModel modelo = new DefaultTableModel();
    ConexionBD conect = new ConexionBD();

    public ProveedoresP() {
        initComponents();
        modelo = (DefaultTableModel) tablaProv.getModel();
        mostrarTabla("");
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        registroDialog = new javax.swing.JDialog();
        panelRegistro = new javax.swing.JPanel();
        jLabelNomR = new javax.swing.JLabel();
        nomProvR = new javax.swing.JFormattedTextField();
        jLabelTelR = new javax.swing.JLabel();
        telProvR = new javax.swing.JFormattedTextField();
        jLabelEmailR = new javax.swing.JLabel();
        emailProvR = new javax.swing.JFormattedTextField();
        jLabelDirR = new javax.swing.JLabel();
        dirProvR = new javax.swing.JFormattedTextField();
        jLabelRfcR = new javax.swing.JLabel();
        rfcProvR = new javax.swing.JFormattedTextField();
        labelincR = new javax.swing.JLabel();
        regProvBtn = new javax.swing.JButton();

        editarDialog = new javax.swing.JDialog();
        panelEditar = new javax.swing.JPanel();
        jLabelNomE = new javax.swing.JLabel();
        nomProvE = new javax.swing.JFormattedTextField();
        jLabelTelE = new javax.swing.JLabel();
        telProvE = new javax.swing.JFormattedTextField();
        jLabelEmailE = new javax.swing.JLabel();
        emailProvE = new javax.swing.JFormattedTextField();
        jLabelDirE = new javax.swing.JLabel();
        dirProvE = new javax.swing.JFormattedTextField();
        jLabelRfcE = new javax.swing.JLabel();
        rfcProvE = new javax.swing.JFormattedTextField();
        labelincE = new javax.swing.JLabel();
        editProvBtn = new javax.swing.JButton();

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tablaProv = new javax.swing.JTable();
        jPanel2 = new javax.swing.JPanel();
        agregarBtn = new javax.swing.JButton();
        editarBtn = new javax.swing.JButton();
        inactivarBtn = new javax.swing.JButton();
        buscarField = new javax.swing.JFormattedTextField();
        buscarBtn = new javax.swing.JButton();

        // ========== DIALOGO REGISTRO ==========
        registroDialog.setTitle("Registrar Proveedor");
        registroDialog.setAlwaysOnTop(true);
        registroDialog.setMinimumSize(new java.awt.Dimension(550, 390));
        registroDialog.setModal(true);
        registroDialog.setResizable(false);
        registroDialog.getContentPane().setLayout(new java.awt.CardLayout());

        panelRegistro.setLayout(new java.awt.GridBagLayout());

        jLabelNomR.setFont(new java.awt.Font("Noto Serif", 1, 18));
        jLabelNomR.setForeground(new java.awt.Color(78, 150, 150));
        jLabelNomR.setText("Nombre:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        panelRegistro.add(jLabelNomR, gridBagConstraints);

        nomProvR.setFont(new java.awt.Font("Noto Serif", 0, 18));
        nomProvR.setPreferredSize(new java.awt.Dimension(250, 30));
        nomProvR.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                nomProvRKeyTyped(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        panelRegistro.add(nomProvR, gridBagConstraints);

        jLabelTelR.setFont(new java.awt.Font("Noto Serif", 1, 18));
        jLabelTelR.setForeground(new java.awt.Color(78, 150, 150));
        jLabelTelR.setText("Teléfono:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        panelRegistro.add(jLabelTelR, gridBagConstraints);

        telProvR.setFont(new java.awt.Font("Noto Serif", 0, 18));
        telProvR.setPreferredSize(new java.awt.Dimension(250, 30));
        telProvR.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                telProvRKeyTyped(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        panelRegistro.add(telProvR, gridBagConstraints);

        jLabelEmailR.setFont(new java.awt.Font("Noto Serif", 1, 18));
        jLabelEmailR.setForeground(new java.awt.Color(78, 150, 150));
        jLabelEmailR.setText("Email:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        panelRegistro.add(jLabelEmailR, gridBagConstraints);

        emailProvR.setFont(new java.awt.Font("Noto Serif", 0, 18));
        emailProvR.setPreferredSize(new java.awt.Dimension(250, 30));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        panelRegistro.add(emailProvR, gridBagConstraints);

        jLabelDirR.setFont(new java.awt.Font("Noto Serif", 1, 18));
        jLabelDirR.setForeground(new java.awt.Color(78, 150, 150));
        jLabelDirR.setText("Dirección:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        panelRegistro.add(jLabelDirR, gridBagConstraints);

        dirProvR.setFont(new java.awt.Font("Noto Serif", 0, 18));
        dirProvR.setPreferredSize(new java.awt.Dimension(250, 30));
        dirProvR.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                dirProvRKeyTyped(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        panelRegistro.add(dirProvR, gridBagConstraints);

        jLabelRfcR.setFont(new java.awt.Font("Noto Serif", 1, 18));
        jLabelRfcR.setForeground(new java.awt.Color(78, 150, 150));
        jLabelRfcR.setText("RFC:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        panelRegistro.add(jLabelRfcR, gridBagConstraints);

        rfcProvR.setFont(new java.awt.Font("Noto Serif", 0, 18));
        rfcProvR.setPreferredSize(new java.awt.Dimension(250, 30));
        rfcProvR.setMinimumSize(new java.awt.Dimension(250, 30));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        panelRegistro.add(rfcProvR, gridBagConstraints);

        labelincR.setFont(new java.awt.Font("Noto Serif", 0, 12));
        labelincR.setForeground(new java.awt.Color(204, 0, 51));
        labelincR.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        labelincR.setText(" ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        panelRegistro.add(labelincR, gridBagConstraints);

        regProvBtn.setFont(new java.awt.Font("Noto Serif", 1, 18));
        regProvBtn.setBackground(new java.awt.Color(125, 255, 177));
        regProvBtn.setIcon(SvgIcon.load("/icons/guardar.svg", SvgIcon.MEDIUM));
        regProvBtn.setText("Registrar");
        regProvBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                regProvBtnActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        panelRegistro.add(regProvBtn, gridBagConstraints);

        registroDialog.getContentPane().add(panelRegistro, "card2");
        registroDialog.setLocationRelativeTo(null);

        // ========== DIALOGO EDITAR ==========
        editarDialog.setTitle("Editar Proveedor");
        editarDialog.setAlwaysOnTop(true);
        editarDialog.setMinimumSize(new java.awt.Dimension(550, 390));
        editarDialog.setModal(true);
        editarDialog.setResizable(false);
        editarDialog.getContentPane().setLayout(new java.awt.CardLayout());

        panelEditar.setLayout(new java.awt.GridBagLayout());

        jLabelNomE.setFont(new java.awt.Font("Noto Serif", 1, 18));
        jLabelNomE.setForeground(new java.awt.Color(78, 150, 150));
        jLabelNomE.setText("Nombre:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        panelEditar.add(jLabelNomE, gridBagConstraints);

        nomProvE.setFont(new java.awt.Font("Noto Serif", 0, 18));
        nomProvE.setPreferredSize(new java.awt.Dimension(250, 30));
        nomProvE.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                nomProvEKeyTyped(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        panelEditar.add(nomProvE, gridBagConstraints);

        jLabelTelE.setFont(new java.awt.Font("Noto Serif", 1, 18));
        jLabelTelE.setForeground(new java.awt.Color(78, 150, 150));
        jLabelTelE.setText("Teléfono:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        panelEditar.add(jLabelTelE, gridBagConstraints);

        telProvE.setFont(new java.awt.Font("Noto Serif", 0, 18));
        telProvE.setPreferredSize(new java.awt.Dimension(250, 30));
        telProvE.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                telProvEKeyTyped(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        panelEditar.add(telProvE, gridBagConstraints);

        jLabelEmailE.setFont(new java.awt.Font("Noto Serif", 1, 18));
        jLabelEmailE.setForeground(new java.awt.Color(78, 150, 150));
        jLabelEmailE.setText("Email:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        panelEditar.add(jLabelEmailE, gridBagConstraints);

        emailProvE.setFont(new java.awt.Font("Noto Serif", 0, 18));
        emailProvE.setPreferredSize(new java.awt.Dimension(250, 30));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        panelEditar.add(emailProvE, gridBagConstraints);

        jLabelDirE.setFont(new java.awt.Font("Noto Serif", 1, 18));
        jLabelDirE.setForeground(new java.awt.Color(78, 150, 150));
        jLabelDirE.setText("Dirección:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        panelEditar.add(jLabelDirE, gridBagConstraints);

        dirProvE.setFont(new java.awt.Font("Noto Serif", 0, 18));
        dirProvE.setPreferredSize(new java.awt.Dimension(250, 30));
        dirProvE.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                dirProvEKeyTyped(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        panelEditar.add(dirProvE, gridBagConstraints);

        jLabelRfcE.setFont(new java.awt.Font("Noto Serif", 1, 18));
        jLabelRfcE.setForeground(new java.awt.Color(78, 150, 150));
        jLabelRfcE.setText("RFC:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        panelEditar.add(jLabelRfcE, gridBagConstraints);

        rfcProvE.setFont(new java.awt.Font("Noto Serif", 0, 18));
        rfcProvE.setPreferredSize(new java.awt.Dimension(250, 30));
        rfcProvE.setMinimumSize(new java.awt.Dimension(250, 30));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        panelEditar.add(rfcProvE, gridBagConstraints);

        labelincE.setFont(new java.awt.Font("Noto Serif", 0, 12));
        labelincE.setForeground(new java.awt.Color(204, 0, 51));
        labelincE.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        labelincE.setText(" ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        panelEditar.add(labelincE, gridBagConstraints);

        editProvBtn.setFont(new java.awt.Font("Noto Serif", 1, 18));
        editProvBtn.setBackground(new java.awt.Color(255, 251, 128));
        editProvBtn.setIcon(SvgIcon.load("/icons/edit.svg", SvgIcon.MEDIUM));
        editProvBtn.setText("Actualizar");
        editProvBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editProvBtnActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        panelEditar.add(editProvBtn, gridBagConstraints);

        editarDialog.getContentPane().add(panelEditar, "card2");
        editarDialog.setLocationRelativeTo(null);

        // ========== PANEL PRINCIPAL ==========
        setLayout(new java.awt.BorderLayout());

        jLabel1.setFont(new java.awt.Font("Noto Serif", 1, 36));
        jLabel1.setForeground(new java.awt.Color(78, 150, 150));
        jLabel1.setIcon(SvgIcon.load("/icons/proveedor.svg", SvgIcon.LARGE));
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("Proveedores");
        jPanel1.add(jLabel1);

        add(jPanel1, java.awt.BorderLayout.NORTH);

        tablaProv.setFont(new java.awt.Font("Noto Serif", 0, 18));
        tablaProv.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {},
            new String [] {
                "ID", "Nombre", "Teléfono", "Email", "Dirección", "Estatus"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane1.setViewportView(tablaProv);

        add(jScrollPane1, java.awt.BorderLayout.CENTER);

        jPanel2.setLayout(new java.awt.GridBagLayout());

        agregarBtn.setFont(new java.awt.Font("Noto Serif", 1, 18));
        agregarBtn.setBackground(new java.awt.Color(125, 255, 177));
        agregarBtn.setIcon(SvgIcon.load("/icons/add-task.svg", SvgIcon.MEDIUM));
        agregarBtn.setText("Agregar");
        agregarBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                agregarBtnActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        jPanel2.add(agregarBtn, gridBagConstraints);

        editarBtn.setFont(new java.awt.Font("Noto Serif", 1, 18));
        editarBtn.setBackground(new java.awt.Color(255, 251, 128));
        editarBtn.setIcon(SvgIcon.load("/icons/edit.svg", SvgIcon.MEDIUM));
        editarBtn.setText("Editar");
        editarBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editarBtnActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        jPanel2.add(editarBtn, gridBagConstraints);

        inactivarBtn.setFont(new java.awt.Font("Noto Serif", 1, 18));
        inactivarBtn.setBackground(new java.awt.Color(252, 149, 149));
        inactivarBtn.setIcon(SvgIcon.load("/icons/cancelar.svg", SvgIcon.MEDIUM));
        inactivarBtn.setText("Inactivar");
        inactivarBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                inactivarBtnActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        jPanel2.add(inactivarBtn, gridBagConstraints);

        buscarField.setFont(new java.awt.Font("Noto Serif", 0, 14));
        buscarField.setPreferredSize(new java.awt.Dimension(120, 30));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.insets = new java.awt.Insets(20, 10, 5, 10);
        jPanel2.add(buscarField, gridBagConstraints);

        buscarBtn.setFont(new java.awt.Font("Noto Serif", 1, 18));
        buscarBtn.setBackground(new java.awt.Color(153, 204, 255));
        buscarBtn.setIcon(SvgIcon.load("/icons/lupa.svg", SvgIcon.MEDIUM));
        buscarBtn.setText("Buscar");
        buscarBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buscarBtnActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 10, 10);
        jPanel2.add(buscarBtn, gridBagConstraints);

        add(jPanel2, java.awt.BorderLayout.LINE_END);
    }// </editor-fold>//GEN-END:initComponents

    private void agregarBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_agregarBtnActionPerformed
        nomProvR.setText("");
        telProvR.setText("");
        emailProvR.setText("");
        dirProvR.setText("");
        rfcProvR.setText("");
        labelincR.setText(" ");
        registroDialog.setVisible(true);
    }//GEN-LAST:event_agregarBtnActionPerformed

    private void editarBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editarBtnActionPerformed
        if(tablaProv.getSelectedRow() != -1){
            idProv = Integer.parseInt("" + tablaProv.getValueAt(tablaProv.getSelectedRow(), 0));
            nomProvE.setText("" + tablaProv.getValueAt(tablaProv.getSelectedRow(), 1));
            String tel = tablaProv.getValueAt(tablaProv.getSelectedRow(), 2) != null ? "" + tablaProv.getValueAt(tablaProv.getSelectedRow(), 2) : "";
            String email = tablaProv.getValueAt(tablaProv.getSelectedRow(), 3) != null ? "" + tablaProv.getValueAt(tablaProv.getSelectedRow(), 3) : "";
            String dir = tablaProv.getValueAt(tablaProv.getSelectedRow(), 4) != null ? "" + tablaProv.getValueAt(tablaProv.getSelectedRow(), 4) : "";
            telProvE.setText(tel.equals("null") ? "" : tel);
            emailProvE.setText(email.equals("null") ? "" : email);
            dirProvE.setText(dir.equals("null") ? "" : dir);
            String[] fiscal = conect.obtenerDatosProveedor(idProv);
            rfcProvE.setText(fiscal[0]);
            labelincE.setText(" ");
            editarDialog.setVisible(true);
        } else {
            Mise.JOption("Seleccione la fila que desea editar", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_editarBtnActionPerformed

    private void inactivarBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_inactivarBtnActionPerformed
        if(tablaProv.getSelectedRow() != -1){
            int id = Integer.parseInt("" + tablaProv.getValueAt(tablaProv.getSelectedRow(), 0));
            String estatusActual = "" + tablaProv.getValueAt(tablaProv.getSelectedRow(), 5);
            String nuevoEstatus = estatusActual.equals("Activo") ? "Inactivo" : "Activo";
            String accion = nuevoEstatus.equals("Inactivo") ? "inactivar" : "activar";
            int res = Mise.JOptionYesNo("¿Está seguro que desea " + accion + " a este proveedor?", "Proveedor");
            if(res == 0){
                conect.cambiarEstatusProveedor(id, nuevoEstatus);
                mostrarTabla("");
            }
        } else {
            Mise.JOption("Seleccione la fila del proveedor", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_inactivarBtnActionPerformed

    private void buscarBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buscarBtnActionPerformed
        mostrarTabla(buscarField.getText().trim());
    }//GEN-LAST:event_buscarBtnActionPerformed

    private void regProvBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_regProvBtnActionPerformed
        String nombre = nomProvR.getText().trim();
        String email  = emailProvR.getText().trim();
        if (nombre.isEmpty()) {
            labelincR.setText("El nombre es obligatorio");
            return;
        }
        if (!email.isEmpty() && (!email.contains("@") || !email.contains("."))) {
            labelincR.setText("El email no tiene un formato válido");
            return;
        }
        String rfc = rfcProvR.getText().trim().toUpperCase();
        if (!rfc.isEmpty() && !rfc.matches("^[A-ZÑ&]{3,4}[0-9]{6}[A-Z0-9]{3}$")) {
            Mise.JOption("El RFC no tiene un formato válido. Se guardará de todos modos.", "Advertencia", javax.swing.JOptionPane.WARNING_MESSAGE);
        }
        if (conect.insertarProveedor(nombre, telProvR.getText().trim(), email, dirProvR.getText().trim(), rfcProvR.getText().trim()) != null) {
            mostrarTabla("");
            registroDialog.setVisible(false);
        }
    }//GEN-LAST:event_regProvBtnActionPerformed

    private void editProvBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editProvBtnActionPerformed
        String nombre = nomProvE.getText().trim();
        String email  = emailProvE.getText().trim();
        if (nombre.isEmpty()) {
            labelincE.setText("El nombre es obligatorio");
            return;
        }
        if (!email.isEmpty() && (!email.contains("@") || !email.contains("."))) {
            labelincE.setText("El email no tiene un formato válido");
            return;
        }
        String rfc = rfcProvE.getText().trim().toUpperCase();
        if (!rfc.isEmpty() && !rfc.matches("^[A-ZÑ&]{3,4}[0-9]{6}[A-Z0-9]{3}$")) {
            Mise.JOption("El RFC no tiene un formato válido. Se guardará de todos modos.", "Advertencia", javax.swing.JOptionPane.WARNING_MESSAGE);
        }
        int conf = Mise.JOptionYesNo("¿Confirmar los cambios al proveedor?", "Editar Proveedor");
        if (conf == 0) {
            if (conect.editarProveedor(idProv, nombre, telProvE.getText().trim(), email, dirProvE.getText().trim(), rfcProvE.getText().trim())) {
                mostrarTabla("");
                editarDialog.setVisible(false);
            }
        }
    }//GEN-LAST:event_editProvBtnActionPerformed

    private void telProvRKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_telProvRKeyTyped
        char letra = evt.getKeyChar();
        if(!Cake.numeros(letra)){
            evt.consume();
        }
        if(Cake.tamaño(telProvR.getText(), 15)){
            evt.consume();
        }
    }//GEN-LAST:event_telProvRKeyTyped

    private void telProvEKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_telProvEKeyTyped
        char letra = evt.getKeyChar();
        if(!Cake.numeros(letra)){
            evt.consume();
        }
        if(Cake.tamaño(telProvE.getText(), 15)){
            evt.consume();
        }
    }//GEN-LAST:event_telProvEKeyTyped

    private void nomProvRKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_nomProvRKeyTyped
        char letra = evt.getKeyChar();
        if (!Cake.letrasMayus(letra) && !Cake.letrasMinus(letra) && !Cake.numeros(letra) && !Cake.inicioEspacios(letra)) {
            evt.consume();
        }
        if (nomProvR.getText().isEmpty()) {
            if (Cake.inicioEspacios(letra)) evt.consume();
        } else {
            if (Cake.espacios(nomProvR.getText(), letra)) evt.consume();
        }
        if (Cake.tamaño(nomProvR.getText(), 150)) evt.consume();
    }//GEN-LAST:event_nomProvRKeyTyped

    private void nomProvEKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_nomProvEKeyTyped
        char letra = evt.getKeyChar();
        if (!Cake.letrasMayus(letra) && !Cake.letrasMinus(letra) && !Cake.numeros(letra) && !Cake.inicioEspacios(letra)) {
            evt.consume();
        }
        if (nomProvE.getText().isEmpty()) {
            if (Cake.inicioEspacios(letra)) evt.consume();
        } else {
            if (Cake.espacios(nomProvE.getText(), letra)) evt.consume();
        }
        if (Cake.tamaño(nomProvE.getText(), 150)) evt.consume();
    }//GEN-LAST:event_nomProvEKeyTyped

    private void dirProvRKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_dirProvRKeyTyped
        if (Cake.tamaño(dirProvR.getText(), 300)) evt.consume();
    }//GEN-LAST:event_dirProvRKeyTyped

    private void dirProvEKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_dirProvEKeyTyped
        if (Cake.tamaño(dirProvE.getText(), 300)) evt.consume();
    }//GEN-LAST:event_dirProvEKeyTyped

    public void mostrarTabla(String filtro){
        Mise.limpiarTabla(modelo);
        String sql = "SELECT * FROM proveedor";
        if(filtro != null && !filtro.isEmpty()){
            sql += " WHERE LOWER(nombre) LIKE LOWER('%" + filtro.replace("'", "''") + "%')";
        }
        sql += " ORDER BY id_proveedor";
        java.sql.ResultSet rs = conect.query(sql);
        if(rs != null){
            try{
                while(rs.next()){
                    modelo.addRow(new Object[]{
                        rs.getInt("id_proveedor"),
                        rs.getString("nombre"),
                        rs.getString("telefono"),
                        rs.getString("email"),
                        rs.getString("direccion"),
                        rs.getString("estatus")
                    });
                }
            } catch(Exception e){
                System.out.println("Error al mostrar la tabla proveedores");
            }
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton agregarBtn;
    private javax.swing.JButton buscarBtn;
    private javax.swing.JFormattedTextField buscarField;
    private javax.swing.JFormattedTextField dirProvE;
    private javax.swing.JFormattedTextField dirProvR;
    private javax.swing.JButton editProvBtn;
    private javax.swing.JDialog editarDialog;
    private javax.swing.JButton editarBtn;
    private javax.swing.JFormattedTextField emailProvE;
    private javax.swing.JFormattedTextField emailProvR;
    private javax.swing.JButton inactivarBtn;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabelDirE;
    private javax.swing.JLabel jLabelDirR;
    private javax.swing.JLabel jLabelEmailE;
    private javax.swing.JLabel jLabelEmailR;
    private javax.swing.JLabel jLabelNomE;
    private javax.swing.JLabel jLabelNomR;
    private javax.swing.JLabel jLabelRfcE;
    private javax.swing.JLabel jLabelRfcR;
    private javax.swing.JLabel jLabelTelE;
    private javax.swing.JLabel jLabelTelR;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel labelincE;
    private javax.swing.JLabel labelincR;
    private javax.swing.JFormattedTextField nomProvE;
    private javax.swing.JFormattedTextField nomProvR;
    private javax.swing.JPanel panelEditar;
    private javax.swing.JPanel panelRegistro;
    private javax.swing.JButton regProvBtn;
    private javax.swing.JDialog registroDialog;
    private javax.swing.JFormattedTextField rfcProvE;
    private javax.swing.JFormattedTextField rfcProvR;
    private javax.swing.JTable tablaProv;
    private javax.swing.JFormattedTextField telProvE;
    private javax.swing.JFormattedTextField telProvR;
    // End of variables declaration//GEN-END:variables
}
