
import javax.swing.table.DefaultTableModel;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */

/**
 *
 * @author mayra
 */
public class EmpleadosP extends javax.swing.JPanel {

    
    ConexionBD conect = new ConexionBD();
    
    DefaultTableModel modeloEmp = new DefaultTableModel();
    
    boolean bandAux;
    
    public EmpleadosP() {
        initComponents();
        modeloEmp = (DefaultTableModel)tablaEmp.getModel();
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

        regDialog = new javax.swing.JDialog();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        botonEReg = new javax.swing.JButton();
        errorContraseña = new javax.swing.JLabel();
        contra1EReg = new javax.swing.JPasswordField();
        contra2EReg = new javax.swing.JPasswordField();
        nomEReg = new javax.swing.JFormattedTextField();
        curpEReg = new javax.swing.JFormattedTextField();
        telEReg = new javax.swing.JFormattedTextField();
        userEReg = new javax.swing.JFormattedTextField();
        jLabel9 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        puestoEReg = new javax.swing.JComboBox<>();
        errorUsuario = new javax.swing.JLabel();
        activarEmp = new javax.swing.JButton();
        actuDialog = new javax.swing.JDialog();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        actEBoton = new javax.swing.JButton();
        nomEAct = new javax.swing.JFormattedTextField();
        curpEAct = new javax.swing.JFormattedTextField();
        telEAct = new javax.swing.JFormattedTextField();
        puestoEAct = new javax.swing.JComboBox<>();
        jLabel19 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tablaEmp = new javax.swing.JTable();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        panelBotones = new javax.swing.JPanel();
        agB = new javax.swing.JButton();
        eliB = new javax.swing.JButton();
        actB = new javax.swing.JButton();

        regDialog.setTitle("Registrar Empleado");
        regDialog.setAlwaysOnTop(true);
        regDialog.setMinimumSize(new java.awt.Dimension(440, 640));
        regDialog.setModal(true);
        regDialog.setPreferredSize(new java.awt.Dimension(440, 640));
        regDialog.setSize(new java.awt.Dimension(440, 640));
        regDialog.getContentPane().setLayout(new java.awt.GridBagLayout());
        regDialog.setLocationRelativeTo(null);

        jLabel2.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(78, 150, 150));
        jLabel2.setText("Registro de Empleados");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        regDialog.getContentPane().add(jLabel2, gridBagConstraints);

        jLabel3.setFont(new java.awt.Font("Noto Serif", 1, 14)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(78, 150, 150));
        jLabel3.setText("Nombre de Empleado");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        regDialog.getContentPane().add(jLabel3, gridBagConstraints);

        jLabel4.setFont(new java.awt.Font("Noto Serif", 1, 14)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(78, 150, 150));
        jLabel4.setText("CURP de Empleado");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        regDialog.getContentPane().add(jLabel4, gridBagConstraints);

        jLabel5.setFont(new java.awt.Font("Noto Serif", 1, 14)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(78, 150, 150));
        jLabel5.setText("Puesto a asignar");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        regDialog.getContentPane().add(jLabel5, gridBagConstraints);

        jLabel6.setFont(new java.awt.Font("Noto Serif", 1, 14)); // NOI18N
        jLabel6.setForeground(new java.awt.Color(78, 150, 150));
        jLabel6.setText("Nombre de usuario");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        regDialog.getContentPane().add(jLabel6, gridBagConstraints);

        jLabel7.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        jLabel7.setForeground(new java.awt.Color(78, 150, 150));
        jLabel7.setText("Datos de inicio de sesión");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.insets = new java.awt.Insets(20, 5, 5, 5);
        regDialog.getContentPane().add(jLabel7, gridBagConstraints);

        jLabel8.setFont(new java.awt.Font("Noto Serif", 1, 14)); // NOI18N
        jLabel8.setForeground(new java.awt.Color(78, 150, 150));
        jLabel8.setText("Confirmar contraseña");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 15;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        regDialog.getContentPane().add(jLabel8, gridBagConstraints);

        botonEReg.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        botonEReg.setForeground(new java.awt.Color(78, 150, 150));
        botonEReg.setText("Registrar");
        botonEReg.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botonERegActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 18;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        regDialog.getContentPane().add(botonEReg, gridBagConstraints);

        errorContraseña.setFont(new java.awt.Font("Noto Serif", 0, 10)); // NOI18N
        errorContraseña.setForeground(new java.awt.Color(204, 0, 51));
        errorContraseña.setText("La contraseña debe ser de al menos tres caracteres");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 17;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        regDialog.getContentPane().add(errorContraseña, gridBagConstraints);

        contra1EReg.setMinimumSize(new java.awt.Dimension(150, 25));
        contra1EReg.setPreferredSize(new java.awt.Dimension(300, 25));
        contra1EReg.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                contra1ERegKeyTyped(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 14;
        regDialog.getContentPane().add(contra1EReg, gridBagConstraints);

        contra2EReg.setMinimumSize(new java.awt.Dimension(150, 25));
        contra2EReg.setPreferredSize(new java.awt.Dimension(300, 25));
        contra2EReg.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                contra2ERegKeyTyped(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 16;
        regDialog.getContentPane().add(contra2EReg, gridBagConstraints);

        nomEReg.setFont(new java.awt.Font("Noto Serif", 1, 14)); // NOI18N
        nomEReg.setMinimumSize(new java.awt.Dimension(150, 25));
        nomEReg.setPreferredSize(new java.awt.Dimension(300, 25));
        nomEReg.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                nomERegKeyTyped(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        regDialog.getContentPane().add(nomEReg, gridBagConstraints);

        curpEReg.setFont(new java.awt.Font("Noto Serif", 1, 14)); // NOI18N
        curpEReg.setMinimumSize(new java.awt.Dimension(150, 25));
        curpEReg.setPreferredSize(new java.awt.Dimension(300, 25));
        curpEReg.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                curpERegKeyTyped(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        regDialog.getContentPane().add(curpEReg, gridBagConstraints);

        telEReg.setFont(new java.awt.Font("Noto Serif", 1, 14)); // NOI18N
        telEReg.setMinimumSize(new java.awt.Dimension(150, 25));
        telEReg.setPreferredSize(new java.awt.Dimension(300, 25));
        telEReg.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                telERegKeyTyped(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        regDialog.getContentPane().add(telEReg, gridBagConstraints);

        userEReg.setFont(new java.awt.Font("Noto Serif", 1, 14)); // NOI18N
        userEReg.setMinimumSize(new java.awt.Dimension(150, 25));
        userEReg.setPreferredSize(new java.awt.Dimension(300, 25));
        userEReg.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                userERegKeyReleased(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                userERegKeyTyped(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 11;
        regDialog.getContentPane().add(userEReg, gridBagConstraints);

        jLabel9.setFont(new java.awt.Font("Noto Serif", 1, 14)); // NOI18N
        jLabel9.setForeground(new java.awt.Color(78, 150, 150));
        jLabel9.setText("Contraseña");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 13;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        regDialog.getContentPane().add(jLabel9, gridBagConstraints);

        jLabel18.setFont(new java.awt.Font("Noto Serif", 1, 14)); // NOI18N
        jLabel18.setForeground(new java.awt.Color(78, 150, 150));
        jLabel18.setText("No. Telefónico");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        regDialog.getContentPane().add(jLabel18, gridBagConstraints);

        puestoEReg.setFont(new java.awt.Font("Noto Serif", 1, 14)); // NOI18N
        puestoEReg.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Vendedor", "Gerente" }));
        puestoEReg.setPreferredSize(new java.awt.Dimension(300, 25));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        regDialog.getContentPane().add(puestoEReg, gridBagConstraints);

        errorUsuario.setFont(new java.awt.Font("Noto Serif", 0, 10)); // NOI18N
        errorUsuario.setForeground(new java.awt.Color(204, 0, 51));
        errorUsuario.setText("El nombre de usuario debe ser mayor a cuatro caracteres");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        regDialog.getContentPane().add(errorUsuario, gridBagConstraints);

        activarEmp.setFont(new java.awt.Font("Noto Serif", 1, 13)); // NOI18N
        activarEmp.setText("*");
        activarEmp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                activarEmpActionPerformed(evt);
            }
        });
        regDialog.getContentPane().add(activarEmp, new java.awt.GridBagConstraints());
        activarEmp.setToolTipText("Registrar a un empleado que ya se ha registrado antes");

        actuDialog.setTitle("Actualizar Empleado");
        actuDialog.setAlwaysOnTop(true);
        actuDialog.setMinimumSize(new java.awt.Dimension(420, 340));
        actuDialog.setModal(true);
        actuDialog.setPreferredSize(new java.awt.Dimension(420, 340));
        actuDialog.setSize(new java.awt.Dimension(420, 340));
        actuDialog.getContentPane().setLayout(new java.awt.GridBagLayout());
        actuDialog.setLocationRelativeTo(null);

        jLabel11.setFont(new java.awt.Font("Noto Serif", 1, 14)); // NOI18N
        jLabel11.setForeground(new java.awt.Color(78, 150, 150));
        jLabel11.setText("Nombre de Empleado");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        actuDialog.getContentPane().add(jLabel11, gridBagConstraints);

        jLabel12.setFont(new java.awt.Font("Noto Serif", 1, 14)); // NOI18N
        jLabel12.setForeground(new java.awt.Color(78, 150, 150));
        jLabel12.setText("CURP de Empleado");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        actuDialog.getContentPane().add(jLabel12, gridBagConstraints);

        jLabel13.setFont(new java.awt.Font("Noto Serif", 1, 14)); // NOI18N
        jLabel13.setForeground(new java.awt.Color(78, 150, 150));
        jLabel13.setText("No. Telefónico");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        actuDialog.getContentPane().add(jLabel13, gridBagConstraints);

        actEBoton.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        actEBoton.setForeground(new java.awt.Color(78, 150, 150));
        actEBoton.setText("Actualizar");
        actEBoton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                actEBotonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.insets = new java.awt.Insets(15, 5, 5, 5);
        actuDialog.getContentPane().add(actEBoton, gridBagConstraints);

        nomEAct.setEditable(false);
        nomEAct.setFont(new java.awt.Font("Noto Serif", 1, 14)); // NOI18N
        nomEAct.setMinimumSize(new java.awt.Dimension(150, 25));
        nomEAct.setPreferredSize(new java.awt.Dimension(300, 25));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        actuDialog.getContentPane().add(nomEAct, gridBagConstraints);

        curpEAct.setEditable(false);
        curpEAct.setFont(new java.awt.Font("Noto Serif", 1, 14)); // NOI18N
        curpEAct.setMinimumSize(new java.awt.Dimension(150, 25));
        curpEAct.setPreferredSize(new java.awt.Dimension(300, 25));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        actuDialog.getContentPane().add(curpEAct, gridBagConstraints);

        telEAct.setFont(new java.awt.Font("Noto Serif", 1, 14)); // NOI18N
        telEAct.setMinimumSize(new java.awt.Dimension(150, 25));
        telEAct.setPreferredSize(new java.awt.Dimension(300, 25));
        telEAct.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                telEActKeyTyped(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        actuDialog.getContentPane().add(telEAct, gridBagConstraints);

        puestoEAct.setFont(new java.awt.Font("Noto Serif", 1, 14)); // NOI18N
        puestoEAct.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Vendedor", "Gerente" }));
        puestoEAct.setPreferredSize(new java.awt.Dimension(300, 25));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        actuDialog.getContentPane().add(puestoEAct, gridBagConstraints);

        jLabel19.setFont(new java.awt.Font("Noto Serif", 1, 14)); // NOI18N
        jLabel19.setForeground(new java.awt.Color(78, 150, 150));
        jLabel19.setText("Puesto");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        actuDialog.getContentPane().add(jLabel19, gridBagConstraints);

        setLayout(new java.awt.BorderLayout());

        tablaEmp.setFont(new java.awt.Font("Noto Serif", 0, 18)); // NOI18N
        tablaEmp.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Curp", "Nombre", "Puesto", "Telefono"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane1.setViewportView(tablaEmp);

        add(jScrollPane1, java.awt.BorderLayout.CENTER);

        jPanel1.setLayout(new java.awt.GridBagLayout());

        jLabel1.setFont(new java.awt.Font("Noto Serif", 1, 36)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(78, 150, 150));
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("Empleados");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        jPanel1.add(jLabel1, gridBagConstraints);

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
    }// </editor-fold>//GEN-END:initComponents

    private void agBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_agBActionPerformed
        //Abre el diálogo para registrar a un empleado
        bandAux = false;
        regDialog.setVisible(true);
    }//GEN-LAST:event_agBActionPerformed

    private void eliBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_eliBActionPerformed
        //Elimina a un empleado
        if(tablaEmp.getSelectedRow() != -1){
            int res = Mise.JOptionYesNo("¿Está seguro que desea dar de baja a este empleado?", "Empleado");
            if(res == 0){
                String cuur = "" + tablaEmp.getValueAt(tablaEmp.getSelectedRow(), 0);
                conect.inactivarEmpleado(cuur, true);
                mostrarTablaEmp();
            }
        } else{
            Mise.JOption("Seleccione la fila que desea eliminar", "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_eliBActionPerformed

    private void actBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_actBActionPerformed
        //Actualiza a un empleado
        if(tablaEmp.getSelectedRow() != -1){
            curpEAct.setText("" + tablaEmp.getValueAt(tablaEmp.getSelectedRow(), 0));
            nomEAct.setText("" + tablaEmp.getValueAt(tablaEmp.getSelectedRow(), 1));
            telEAct.setText("" + tablaEmp.getValueAt(tablaEmp.getSelectedRow(), 3));
            puestoEAct.setSelectedItem("" + tablaEmp.getValueAt(tablaEmp.getSelectedRow(), 2));
            actuDialog.setVisible(true);
        } else{
            Mise.JOption("Seleccione la fila que desea actualizar", "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_actBActionPerformed

    private void botonERegActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botonERegActionPerformed
        //Registra a un empleado
        if(nomEReg.getText().isEmpty() || curpEReg.getText().isEmpty() || telEReg.getText().isEmpty() || userEReg.getText().isEmpty() || contra1EReg.getText().isEmpty() || contra2EReg.getText().isEmpty()){
            Mise.JOption("Debe llenar todos los campos", "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        } else{
            if(bandAux){
                if(contra1EReg.getText().length() < 3){
                    errorContraseña.setText("La contraseña debe ser de al menos tres caracteres");
                }else{
                    if(contra1EReg.getText().equals(contra2EReg.getText())){
                        String[] campos = {curpEReg.getText(), nomEReg.getText(), "" + puestoEReg.getSelectedItem(), telEReg.getText(), userEReg.getText(), contra2EReg.getText()};
                        if(conect.insertarEmpleado(campos)){
                            nomEReg.setText(""); curpEReg.setText(""); telEReg.setText(""); userEReg.setText(""); contra1EReg.setText(""); contra2EReg.setText("");
                            regDialog.setVisible(false);
                            mostrarTablaEmp();
                        }
                    } else{
                        errorContraseña.setText("La confirmación de contraseña es incorrecta");
                    }
                }
            } else{
                Mise.JOption("No es posible registrar al empleado con el nombre de usuario que se ingresó", "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_botonERegActionPerformed

    private void actEBotonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_actEBotonActionPerformed
        //Actualiza los datos de un empleado
        if(telEAct.getText().isEmpty()){
            Mise.JOption("Llene todos los campos", "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        } else{
            String[] campos = {"" + puestoEAct.getSelectedItem(), telEAct.getText()};
            if(conect.actualizarEmpleado(campos, curpEAct.getText())){
                actuDialog.setVisible(false);
                mostrarTablaEmp();
            }
        }
    }//GEN-LAST:event_actEBotonActionPerformed

    private void curpERegKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_curpERegKeyTyped
        //Verifica que la CURP no tenga más de 18 caracteres
        char letra = evt.getKeyChar();
        if(!Cake.letrasMayus(letra) && !Cake.numeros(letra)){
            evt.consume();
        }

        if(Cake.tamaño(curpEReg.getText(), 18)){
            evt.consume();
        }
    }//GEN-LAST:event_curpERegKeyTyped

    private void nomERegKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_nomERegKeyTyped
        //Verifica que el nombre no tenga más de 50 caracteres
        char letra = evt.getKeyChar();
        if(!Cake.letrasMayus(letra) && !Cake.numeros(letra) && !Cake.letrasMinus(letra) && !(Cake.inicioEspacios(letra))){
            evt.consume();
        }

        if(!(nomEReg.getText().isEmpty())){
            if(Cake.espacios(nomEReg.getText(), letra)){
                evt.consume();
            }
        }
        else{
            if(Cake.inicioEspacios(letra)){
                evt.consume();
            }
        }

        if(Cake.tamaño(nomEReg.getText(), 50)){
            evt.consume();
        }
    }//GEN-LAST:event_nomERegKeyTyped

    private void telERegKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_telERegKeyTyped
        //Verifica que el teléfono sea de diez dígitos
        char letra = evt.getKeyChar();
        if(!Cake.numeros(letra)){
            evt.consume();
        }
        
        if(Cake.tamaño(telEReg.getText(), 10)){
            evt.consume();
        }
    }//GEN-LAST:event_telERegKeyTyped

    private void telEActKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_telEActKeyTyped
        //Verifica que el teléfono sea de diez dígitos
        char letra = evt.getKeyChar();
        if(!Cake.numeros(letra)){
            evt.consume();
        }
        
        if(Cake.tamaño(telEAct.getText(), 10)){
            evt.consume();
        }
    }//GEN-LAST:event_telEActKeyTyped

    private void userERegKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_userERegKeyTyped
        //Verifica que el nombre de usuario sea de al menos cinco caracteres
        char letra = evt.getKeyChar();
        if(!Cake.letrasMayus(letra) && !Cake.letrasMinus(letra) && !Cake.numeros(letra)){
            evt.consume();
        }

        if(Cake.tamaño(userEReg.getText(), 10)){
            evt.consume();
        }
    }//GEN-LAST:event_userERegKeyTyped

    private void contra1ERegKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_contra1ERegKeyTyped
        //Verifica que la contraseña sea de al menos tres caracteres
        char letra = evt.getKeyChar();
        if(!Cake.letrasMayus(letra) && !Cake.letrasMinus(letra) && !Cake.numeros(letra)){
            evt.consume();
        }

        if(Cake.tamaño(contra1EReg.getText(), 10)){
            evt.consume();
        }
    }//GEN-LAST:event_contra1ERegKeyTyped

    private void contra2ERegKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_contra2ERegKeyTyped
        //Verifica que la contraseña sea de al menos tres caracteres
        char letra = evt.getKeyChar();
        if(!Cake.letrasMayus(letra) && !Cake.letrasMinus(letra) && !Cake.numeros(letra)){
            evt.consume();
        }

        if(Cake.tamaño(contra2EReg.getText(), 10)){
            evt.consume();
        }
    }//GEN-LAST:event_contra2ERegKeyTyped

    private void userERegKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_userERegKeyReleased
        //Verifica si el nombre de usuario ya existe
        if(userEReg.getText().length() <= 4){
            errorUsuario.setText("El nombre de usuario debe ser mayor a cuatro caracteres");
        } else{
            if(conect.verificarUsuario(userEReg.getText())){
                errorUsuario.setText("No es posible utilizar ese nombre");
            } else{
                errorUsuario.setText("");
                bandAux = true;
            }
        }
    }//GEN-LAST:event_userERegKeyReleased

    private void activarEmpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_activarEmpActionPerformed
        //Vuelve a registrar a un empleado que ya se ha registrado antes
        String cuur = Mise.JOptionInput("Ingresa la CURP del empleado:", "Registrar empleado");
        if(conect.inactivarEmpleado(cuur, false)){
            mostrarTablaEmp();
            Mise.JOption("Empleado registrado nuevamente", "Empleado", javax.swing.JOptionPane.PLAIN_MESSAGE);
            regDialog.setVisible(false);
        }
    }//GEN-LAST:event_activarEmpActionPerformed

    public void mostrarTablaEmp(){//Muestra la tabla de empleados
        Mise.limpiarTabla(modeloEmp);
        java.sql.ResultSet rs = conect.query("SELECT * FROM empleado WHERE estatus='Activo'");
        try{
            while(rs.next()){
                modeloEmp.addRow(new Object[]{rs.getString("id_empleado"), rs.getString("nombre"), rs.getString("puesto"), rs.getString("telefono")});
            }
        } catch(java.sql.SQLException e){
            System.out.println("Error al mostrar la tabla empleado");
        }
    }
    

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton actB;
    private javax.swing.JButton actEBoton;
    private javax.swing.JButton activarEmp;
    private javax.swing.JDialog actuDialog;
    private javax.swing.JButton agB;
    private javax.swing.JButton botonEReg;
    private javax.swing.JPasswordField contra1EReg;
    private javax.swing.JPasswordField contra2EReg;
    private javax.swing.JFormattedTextField curpEAct;
    private javax.swing.JFormattedTextField curpEReg;
    private javax.swing.JButton eliB;
    private javax.swing.JLabel errorContraseña;
    private javax.swing.JLabel errorUsuario;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JFormattedTextField nomEAct;
    private javax.swing.JFormattedTextField nomEReg;
    public javax.swing.JPanel panelBotones;
    private javax.swing.JComboBox<String> puestoEAct;
    private javax.swing.JComboBox<String> puestoEReg;
    private javax.swing.JDialog regDialog;
    private javax.swing.JTable tablaEmp;
    private javax.swing.JFormattedTextField telEAct;
    private javax.swing.JFormattedTextField telEReg;
    private javax.swing.JFormattedTextField userEReg;
    // End of variables declaration//GEN-END:variables
}
