import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
/*
 * Panel de Catálogo de Categorías – CRUD completo.
 * Misma guía visual que ClientesP.java / ProveedoresP.java.
 */
public class CategoriasP extends javax.swing.JPanel {
    int idCat = -1;
    DefaultTableModel modelo = new DefaultTableModel();
    ConexionBD conect = new ConexionBD();
    public CategoriasP() {
        initComponents();
        modelo = (DefaultTableModel) tablaC.getModel();
        mostrarTabla();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        agregarDialog = new javax.swing.JDialog();
        panelAgregar = new javax.swing.JPanel();
        jLabelNomAg = new javax.swing.JLabel();
        nomField = new javax.swing.JFormattedTextField();
        jLabelDescAg = new javax.swing.JLabel();
        descField = new javax.swing.JFormattedTextField();
        labelIncAg = new javax.swing.JLabel();
        btnGuardarAg = new javax.swing.JButton();
        editarDialog = new javax.swing.JDialog();
        panelEditar = new javax.swing.JPanel();
        jLabelNomEd = new javax.swing.JLabel();
        nomField2 = new javax.swing.JFormattedTextField();
        jLabelDescEd = new javax.swing.JLabel();
        descField2 = new javax.swing.JFormattedTextField();
        labelIncEd = new javax.swing.JLabel();
        btnGuardarEd = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tablaC = new javax.swing.JTable();
        jPanel2 = new javax.swing.JPanel();
        btnAgregar = new javax.swing.JButton();
        btnEditar = new javax.swing.JButton();
        btnToggleEstatus = new javax.swing.JButton();
        jLabelBuscar = new javax.swing.JLabel();
        buscarField = new javax.swing.JFormattedTextField();
        btnBuscar = new javax.swing.JButton();

        agregarDialog.setTitle("Agregar Categoría");
        agregarDialog.setAlwaysOnTop(true);
        agregarDialog.setMinimumSize(new java.awt.Dimension(500, 280));
        agregarDialog.setModal(true);
        agregarDialog.setResizable(false);
        agregarDialog.getContentPane().setLayout(new java.awt.CardLayout());

        panelAgregar.setLayout(new java.awt.GridBagLayout());

        jLabelNomAg.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        jLabelNomAg.setForeground(new java.awt.Color(78, 150, 150));
        jLabelNomAg.setText("Nombre: *");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 14, 6, 10);
        panelAgregar.add(jLabelNomAg, gridBagConstraints);

        nomField.setFont(new java.awt.Font("Noto Serif", 0, 18)); // NOI18N
        nomField.setPreferredSize(new java.awt.Dimension(300, 32));
        nomField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                nomFieldKeyTyped(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 6, 14);
        panelAgregar.add(nomField, gridBagConstraints);

        jLabelDescAg.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        jLabelDescAg.setForeground(new java.awt.Color(78, 150, 150));
        jLabelDescAg.setText("Descripción:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 14, 6, 10);
        panelAgregar.add(jLabelDescAg, gridBagConstraints);

        descField.setFont(new java.awt.Font("Noto Serif", 0, 18)); // NOI18N
        descField.setPreferredSize(new java.awt.Dimension(300, 32));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 10, 6, 14);
        panelAgregar.add(descField, gridBagConstraints);

        labelIncAg.setFont(new java.awt.Font("Noto Serif", 0, 12)); // NOI18N
        labelIncAg.setForeground(new java.awt.Color(204, 0, 51));
        labelIncAg.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        labelIncAg.setText(" ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        panelAgregar.add(labelIncAg, gridBagConstraints);

        btnGuardarAg.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        btnGuardarAg.setForeground(new java.awt.Color(78, 150, 150));
        btnGuardarAg.setText("Guardar");
        btnGuardarAg.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGuardarAgActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 14, 10);
        panelAgregar.add(btnGuardarAg, gridBagConstraints);

        agregarDialog.getContentPane().add(panelAgregar, "card2");

        agregarDialog.setLocationRelativeTo(null);

        editarDialog.setTitle("Editar Categoría");
        editarDialog.setAlwaysOnTop(true);
        editarDialog.setMinimumSize(new java.awt.Dimension(500, 280));
        editarDialog.setModal(true);
        editarDialog.setResizable(false);
        editarDialog.getContentPane().setLayout(new java.awt.CardLayout());

        panelEditar.setLayout(new java.awt.GridBagLayout());

        jLabelNomEd.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        jLabelNomEd.setForeground(new java.awt.Color(78, 150, 150));
        jLabelNomEd.setText("Nombre: *");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 14, 6, 10);
        panelEditar.add(jLabelNomEd, gridBagConstraints);

        nomField2.setFont(new java.awt.Font("Noto Serif", 0, 18)); // NOI18N
        nomField2.setPreferredSize(new java.awt.Dimension(300, 32));
        nomField2.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                nomField2KeyTyped(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 6, 14);
        panelEditar.add(nomField2, gridBagConstraints);

        jLabelDescEd.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        jLabelDescEd.setForeground(new java.awt.Color(78, 150, 150));
        jLabelDescEd.setText("Descripción:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 14, 6, 10);
        panelEditar.add(jLabelDescEd, gridBagConstraints);

        descField2.setFont(new java.awt.Font("Noto Serif", 0, 18)); // NOI18N
        descField2.setPreferredSize(new java.awt.Dimension(300, 32));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 10, 6, 14);
        panelEditar.add(descField2, gridBagConstraints);

        labelIncEd.setFont(new java.awt.Font("Noto Serif", 0, 12)); // NOI18N
        labelIncEd.setForeground(new java.awt.Color(204, 0, 51));
        labelIncEd.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        labelIncEd.setText(" ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        panelEditar.add(labelIncEd, gridBagConstraints);

        btnGuardarEd.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        btnGuardarEd.setForeground(new java.awt.Color(78, 150, 150));
        btnGuardarEd.setText("Guardar");
        btnGuardarEd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGuardarEdActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 14, 10);
        panelEditar.add(btnGuardarEd, gridBagConstraints);

        editarDialog.getContentPane().add(panelEditar, "card2");

        editarDialog.setLocationRelativeTo(null);

        setLayout(new java.awt.BorderLayout());

        jLabel1.setFont(new java.awt.Font("Noto Serif", 1, 36)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(78, 150, 150));
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("Catálogo de Categorías");
        jPanel1.add(jLabel1);

        add(jPanel1, java.awt.BorderLayout.NORTH);

        tablaC.setFont(new java.awt.Font("Noto Serif", 0, 16)); // NOI18N
        tablaC.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ID", "Nombre", "Descripción", "Estatus"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tablaC.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
        jScrollPane1.setViewportView(tablaC);

        add(jScrollPane1, java.awt.BorderLayout.CENTER);

        jPanel2.setLayout(new java.awt.GridBagLayout());

        btnAgregar.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        btnAgregar.setForeground(new java.awt.Color(78, 150, 150));
        btnAgregar.setText("Agregar");
        btnAgregar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAgregarActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(10, 20, 10, 20);
        jPanel2.add(btnAgregar, gridBagConstraints);

        btnEditar.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        btnEditar.setForeground(new java.awt.Color(78, 150, 150));
        btnEditar.setText("Editar");
        btnEditar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEditarActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(10, 20, 10, 20);
        jPanel2.add(btnEditar, gridBagConstraints);

        btnToggleEstatus.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        btnToggleEstatus.setForeground(new java.awt.Color(78, 150, 150));
        btnToggleEstatus.setText("Inactivar/Activar");
        btnToggleEstatus.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnToggleEstatusActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(10, 20, 10, 20);
        jPanel2.add(btnToggleEstatus, gridBagConstraints);

        jLabelBuscar.setFont(new java.awt.Font("Noto Serif", 1, 14)); // NOI18N
        jLabelBuscar.setForeground(new java.awt.Color(78, 150, 150));
        jLabelBuscar.setText("Buscar por nombre:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.insets = new java.awt.Insets(20, 20, 4, 20);
        jPanel2.add(jLabelBuscar, gridBagConstraints);

        buscarField.setFont(new java.awt.Font("Noto Serif", 0, 14)); // NOI18N
        buscarField.setPreferredSize(new java.awt.Dimension(180, 28));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.insets = new java.awt.Insets(4, 20, 4, 20);
        jPanel2.add(buscarField, gridBagConstraints);

        btnBuscar.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        btnBuscar.setForeground(new java.awt.Color(78, 150, 150));
        btnBuscar.setText("Buscar");
        btnBuscar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBuscarActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.insets = new java.awt.Insets(4, 20, 10, 20);
        jPanel2.add(btnBuscar, gridBagConstraints);

        add(jPanel2, java.awt.BorderLayout.LINE_END);
    }// </editor-fold>//GEN-END:initComponents

    private void nomFieldKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_nomFieldKeyTyped
        char letra = evt.getKeyChar();
        if (!Cake.letrasMayus(letra) && !Cake.letrasMinus(letra) && !Cake.numeros(letra) && !Cake.inicioEspacios(letra)) {
            evt.consume();
        }
        if (!nomField.getText().isEmpty()) {
            if (Cake.espacios(nomField.getText(), letra)) evt.consume();
        } else {
            if (Cake.inicioEspacios(letra)) evt.consume();
        }
        if (Cake.tamaño(nomField.getText(), 100)) evt.consume();
    }//GEN-LAST:event_nomFieldKeyTyped

    private void nomField2KeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_nomField2KeyTyped
        char letra = evt.getKeyChar();
        if (!Cake.letrasMayus(letra) && !Cake.letrasMinus(letra) && !Cake.numeros(letra) && !Cake.inicioEspacios(letra)) {
            evt.consume();
        }
        if (!nomField2.getText().isEmpty()) {
            if (Cake.espacios(nomField2.getText(), letra)) evt.consume();
        } else {
            if (Cake.inicioEspacios(letra)) evt.consume();
        }
        if (Cake.tamaño(nomField2.getText(), 100)) evt.consume();
    }//GEN-LAST:event_nomField2KeyTyped

    private void btnGuardarAgActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGuardarAgActionPerformed
        guardarCategoria();
    }//GEN-LAST:event_btnGuardarAgActionPerformed

    private void btnGuardarEdActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGuardarEdActionPerformed
        guardarEdicion();
    }//GEN-LAST:event_btnGuardarEdActionPerformed

    private void btnAgregarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAgregarActionPerformed
        abrirAgregar();
    }//GEN-LAST:event_btnAgregarActionPerformed

    private void btnEditarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditarActionPerformed
        abrirEditar();
    }//GEN-LAST:event_btnEditarActionPerformed

    private void btnToggleEstatusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnToggleEstatusActionPerformed
        toggleEstatus();
    }//GEN-LAST:event_btnToggleEstatusActionPerformed

    private void btnBuscarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBuscarActionPerformed
        buscarCategoria();
    }//GEN-LAST:event_btnBuscarActionPerformed

    // Acciones
    private void abrirAgregar() {
        nomField.setText("");
        descField.setText("");
        labelIncAg.setText(" ");
        agregarDialog.setVisible(true);
    }
    private void guardarCategoria() {
        String nombre = nomField.getText().trim();
        if (nombre.isEmpty()) {
            labelIncAg.setText("El nombre es obligatorio");
            return;
        }
        boolean ok = conect.insertarCategoria(nombre, descField.getText().trim());
        if (ok) {
            mostrarTabla();
            agregarDialog.setVisible(false);
        }
    }
    private void abrirEditar() {
        if (tablaC.getSelectedRow() == -1) {
            Mise.JOption("Seleccione la categoría que desea editar", "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        int row = tablaC.getSelectedRow();
        idCat = Integer.parseInt(tablaC.getValueAt(row, 0).toString());
        nomField2.setText(tablaC.getValueAt(row, 1).toString());
        descField2.setText(nullToEmpty(tablaC.getValueAt(row, 2)));
        labelIncEd.setText(" ");
        editarDialog.setVisible(true);
    }
    private void guardarEdicion() {
        String nombre = nomField2.getText().trim();
        if (nombre.isEmpty()) {
            labelIncEd.setText("El nombre es obligatorio");
            return;
        }
        boolean ok = conect.editarCategoria(idCat, nombre, descField2.getText().trim());
        if (ok) {
            mostrarTabla();
            editarDialog.setVisible(false);
        }
    }
    private void toggleEstatus() {
        if (tablaC.getSelectedRow() == -1) {
            Mise.JOption("Seleccione la categoría que desea activar/inactivar", "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        int row = tablaC.getSelectedRow();
        idCat = Integer.parseInt(tablaC.getValueAt(row, 0).toString());
        String estatusActual = tablaC.getValueAt(row, 3).toString();
        String nuevoEstatus = estatusActual.equals("Activo") ? "Inactivo" : "Activo";
        int res = Mise.JOptionYesNo(
                "¿Desea cambiar el estatus de la categoría a \"" + nuevoEstatus + "\"?",
                "Cambiar Estatus");
        if (res == 0) {
            conect.cambiarEstatusCategoria(idCat, nuevoEstatus);
            mostrarTabla();
        }
    }
    private void buscarCategoria() {
        String texto = buscarField.getText().trim();
        Mise.limpiarTabla(modelo);
        java.sql.ResultSet rs = conect.buscarCategoriasPorNombre(texto);
        cargarResultado(rs);
    }
    public void mostrarTabla() {
        Mise.limpiarTabla(modelo);
        java.sql.ResultSet rs = conect.obtenerTodasCategorias();
        cargarResultado(rs);
    }
    private void cargarResultado(java.sql.ResultSet rs) {
        if (rs == null) return;
        try {
            while (rs.next()) {
                modelo.addRow(new Object[]{
                    rs.getInt("id_categoria"),
                    rs.getString("nombre"),
                    rs.getString("descripcion"),
                    rs.getString("estatus")
                });
            }
        } catch (java.sql.SQLException e) {
            System.out.println("Error al cargar tabla categorías: " + e.getMessage());
        }
    }
    private String nullToEmpty(Object val) {
        return val == null ? "" : val.toString();
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JDialog agregarDialog;
    private javax.swing.JButton btnAgregar;
    private javax.swing.JButton btnBuscar;
    private javax.swing.JButton btnEditar;
    private javax.swing.JButton btnGuardarAg;
    private javax.swing.JButton btnGuardarEd;
    private javax.swing.JButton btnToggleEstatus;
    private javax.swing.JFormattedTextField buscarField;
    private javax.swing.JFormattedTextField descField;
    private javax.swing.JFormattedTextField descField2;
    private javax.swing.JDialog editarDialog;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabelBuscar;
    private javax.swing.JLabel jLabelDescAg;
    private javax.swing.JLabel jLabelDescEd;
    private javax.swing.JLabel jLabelNomAg;
    private javax.swing.JLabel jLabelNomEd;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel labelIncAg;
    private javax.swing.JLabel labelIncEd;
    private javax.swing.JFormattedTextField nomField;
    private javax.swing.JFormattedTextField nomField2;
    private javax.swing.JPanel panelAgregar;
    private javax.swing.JPanel panelEditar;
    private javax.swing.JTable tablaC;
    // End of variables declaration//GEN-END:variables
}
