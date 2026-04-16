
import javax.swing.table.DefaultTableModel;

/*
 * Panel Kardex: historial de movimientos de inventario por producto.
 */
public class KardexP extends javax.swing.JPanel {

    ConexionBD conect = new ConexionBD();
    DefaultTableModel modeloKardex = new DefaultTableModel();
    private String idProductoSeleccionado = null;
    private java.util.LinkedHashMap<String, String[]> productosEncontrados = new java.util.LinkedHashMap<>();

    public KardexP() {
        initComponents();
        modeloKardex = (DefaultTableModel) tablaKardex.getModel();

        spnDesde.setModel(new javax.swing.SpinnerDateModel());
        javax.swing.JSpinner.DateEditor edDesde = new javax.swing.JSpinner.DateEditor(spnDesde, "yyyy-MM-dd");
        spnDesde.setEditor(edDesde);

        spnHasta.setModel(new javax.swing.SpinnerDateModel());
        javax.swing.JSpinner.DateEditor edHasta = new javax.swing.JSpinner.DateEditor(spnHasta, "yyyy-MM-dd");
        spnHasta.setEditor(edHasta);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlKardexTitulo = new javax.swing.JPanel();
        lblKardexTitulo = new javax.swing.JLabel();
        pnlKardexMain = new javax.swing.JPanel();
        pnlKardexControls = new javax.swing.JPanel();
        pnlBusqueda = new javax.swing.JPanel();
        lblBuscar = new javax.swing.JLabel();
        txtBuscarProd = new javax.swing.JTextField();
        cmbProductosK = new javax.swing.JComboBox();
        pnlInfoProd = new javax.swing.JPanel();
        lblNombreProd = new javax.swing.JLabel();
        lblCodigoProd = new javax.swing.JLabel();
        lblExistenciaProd = new javax.swing.JLabel();
        pnlFiltros = new javax.swing.JPanel();
        lblDesde = new javax.swing.JLabel();
        spnDesde = new javax.swing.JSpinner();
        lblHasta = new javax.swing.JLabel();
        spnHasta = new javax.swing.JSpinner();
        btnFiltrar = new javax.swing.JButton();
        btnExportarKardex = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        tablaKardex = new javax.swing.JTable();

        setLayout(new java.awt.BorderLayout());

        lblKardexTitulo.setFont(new java.awt.Font("Noto Serif", 1, 36)); // NOI18N
        lblKardexTitulo.setForeground(new java.awt.Color(78, 150, 150));
        lblKardexTitulo.setText("Kardex de Producto");
        pnlKardexTitulo.add(lblKardexTitulo);

        add(pnlKardexTitulo, java.awt.BorderLayout.NORTH);

        pnlKardexMain.setLayout(new java.awt.BorderLayout());

        pnlKardexControls.setLayout(new java.awt.GridLayout(3, 1));

        lblBuscar.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        lblBuscar.setForeground(new java.awt.Color(78, 150, 150));
        lblBuscar.setText("Buscar:");
        pnlBusqueda.add(lblBuscar);

        txtBuscarProd.setFont(new java.awt.Font("Noto Serif", 0, 16)); // NOI18N
        txtBuscarProd.setPreferredSize(new java.awt.Dimension(200, 32));
        txtBuscarProd.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtBuscarProdKeyReleased(evt);
            }
        });
        pnlBusqueda.add(txtBuscarProd);

        cmbProductosK.setFont(new java.awt.Font("Noto Serif", 0, 14)); // NOI18N
        cmbProductosK.setPreferredSize(new java.awt.Dimension(300, 32));
        cmbProductosK.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbProductosKActionPerformed(evt);
            }
        });
        pnlBusqueda.add(cmbProductosK);

        pnlKardexControls.add(pnlBusqueda);

        lblNombreProd.setFont(new java.awt.Font("Noto Serif", 1, 14)); // NOI18N
        lblNombreProd.setForeground(new java.awt.Color(78, 150, 150));
        lblNombreProd.setText("Nombre: —");
        pnlInfoProd.add(lblNombreProd);

        lblCodigoProd.setFont(new java.awt.Font("Noto Serif", 1, 14)); // NOI18N
        lblCodigoProd.setForeground(new java.awt.Color(78, 150, 150));
        lblCodigoProd.setText("   |   Código: —");
        pnlInfoProd.add(lblCodigoProd);

        lblExistenciaProd.setFont(new java.awt.Font("Noto Serif", 1, 14)); // NOI18N
        lblExistenciaProd.setForeground(new java.awt.Color(78, 150, 150));
        lblExistenciaProd.setText("   |   Existencia: —");
        pnlInfoProd.add(lblExistenciaProd);

        pnlKardexControls.add(pnlInfoProd);

        lblDesde.setFont(new java.awt.Font("Noto Serif", 1, 16)); // NOI18N
        lblDesde.setForeground(new java.awt.Color(78, 150, 150));
        lblDesde.setText("Desde:");
        pnlFiltros.add(lblDesde);

        spnDesde.setPreferredSize(new java.awt.Dimension(140, 32));
        pnlFiltros.add(spnDesde);

        lblHasta.setFont(new java.awt.Font("Noto Serif", 1, 16)); // NOI18N
        lblHasta.setForeground(new java.awt.Color(78, 150, 150));
        lblHasta.setText("Hasta:");
        pnlFiltros.add(lblHasta);

        spnHasta.setPreferredSize(new java.awt.Dimension(140, 32));
        pnlFiltros.add(spnHasta);

        btnFiltrar.setFont(new java.awt.Font("Noto Serif", 1, 16)); // NOI18N
        btnFiltrar.setForeground(new java.awt.Color(78, 150, 150));
        btnFiltrar.setText("Filtrar");
        btnFiltrar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnFiltrarActionPerformed(evt);
            }
        });
        pnlFiltros.add(btnFiltrar);

        btnExportarKardex.setFont(new java.awt.Font("Noto Serif", 1, 16)); // NOI18N
        btnExportarKardex.setForeground(new java.awt.Color(78, 150, 150));
        btnExportarKardex.setText("Exportar Excel");
        btnExportarKardex.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnExportarKardexActionPerformed(evt);
            }
        });
        pnlFiltros.add(btnExportarKardex);

        pnlKardexControls.add(pnlFiltros);

        pnlKardexMain.add(pnlKardexControls, java.awt.BorderLayout.NORTH);

        tablaKardex.setFont(new java.awt.Font("Noto Serif", 0, 14)); // NOI18N
        tablaKardex.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Fecha", "Tipo", "Cantidad", "Exist. Anterior", "Exist. Posterior", "Referencia", "Empleado"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane1.setViewportView(tablaKardex);

        pnlKardexMain.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        add(pnlKardexMain, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void txtBuscarProdKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtBuscarProdKeyReleased
        String filtro = txtBuscarProd.getText().trim();
        if (filtro.length() < 2) return;
        productosEncontrados.clear();
        cmbProductosK.removeAllItems();
        java.sql.ResultSet rs = conect.buscarProductoKardex(filtro);
        try {
            while (rs != null && rs.next()) {
                String id = rs.getString("id_producto");
                String nombre = rs.getString("nombre");
                String cantidad = rs.getString("cantidad");
                String display = id + " - " + nombre;
                productosEncontrados.put(display, new String[]{id, nombre, cantidad});
                cmbProductosK.addItem(display);
            }
        } catch (java.sql.SQLException e) {
            System.out.println("Error buscando productos: " + e.getMessage());
        }
    }//GEN-LAST:event_txtBuscarProdKeyReleased

    private void cmbProductosKActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbProductosKActionPerformed
        String selected = (String) cmbProductosK.getSelectedItem();
        if (selected == null || !productosEncontrados.containsKey(selected)) return;
        String[] info = productosEncontrados.get(selected);
        idProductoSeleccionado = info[0];
        lblNombreProd.setText("Nombre: " + info[1]);
        lblCodigoProd.setText("   |   Código: " + info[0]);
        lblExistenciaProd.setText("   |   Existencia: " + info[2]);
        cargarKardex();
    }//GEN-LAST:event_cmbProductosKActionPerformed

    private void btnFiltrarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFiltrarActionPerformed
        cargarKardex();
    }//GEN-LAST:event_btnFiltrarActionPerformed

    private void btnExportarKardexActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExportarKardexActionPerformed
        if (idProductoSeleccionado == null) {
            Mise.JOption("Seleccione un producto primero", "Aviso", javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }
        java.util.Date desde = (java.util.Date) spnDesde.getValue();
        java.util.Date hasta = (java.util.Date) spnHasta.getValue();
        java.time.LocalDate fechaDesde = desde.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
        java.time.LocalDate fechaHasta = hasta.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
        String nombreProd = lblNombreProd.getText().replace("Nombre: ", "");
        Excel.reporteKardex(idProductoSeleccionado, nombreProd, fechaDesde, fechaHasta);
    }//GEN-LAST:event_btnExportarKardexActionPerformed

    private void cargarKardex() {
        if (idProductoSeleccionado == null) return;
        java.util.Date desde = (java.util.Date) spnDesde.getValue();
        java.util.Date hasta = (java.util.Date) spnHasta.getValue();
        java.time.LocalDate fechaDesde = desde.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
        java.time.LocalDate fechaHasta = hasta.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
        Mise.limpiarTabla(modeloKardex);
        java.sql.ResultSet rs = conect.kardexProducto(idProductoSeleccionado, fechaDesde, fechaHasta);
        try {
            while (rs != null && rs.next()) {
                modeloKardex.addRow(new Object[]{
                    rs.getString("fecha"),
                    rs.getString("tipo_movimiento"),
                    rs.getInt("cantidad"),
                    rs.getInt("existencia_anterior"),
                    rs.getInt("existencia_posterior"),
                    rs.getString("referencia"),
                    rs.getString("empleado")
                });
            }
        } catch (java.sql.SQLException e) {
            System.out.println("Error cargando kardex: " + e.getMessage());
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnExportarKardex;
    private javax.swing.JButton btnFiltrar;
    private javax.swing.JComboBox cmbProductosK;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblBuscar;
    private javax.swing.JLabel lblCodigoProd;
    private javax.swing.JLabel lblDesde;
    private javax.swing.JLabel lblExistenciaProd;
    private javax.swing.JLabel lblHasta;
    private javax.swing.JLabel lblKardexTitulo;
    private javax.swing.JLabel lblNombreProd;
    private javax.swing.JPanel pnlBusqueda;
    private javax.swing.JPanel pnlFiltros;
    private javax.swing.JPanel pnlInfoProd;
    private javax.swing.JPanel pnlKardexControls;
    private javax.swing.JPanel pnlKardexMain;
    private javax.swing.JPanel pnlKardexTitulo;
    private javax.swing.JSpinner spnDesde;
    private javax.swing.JSpinner spnHasta;
    private javax.swing.JTable tablaKardex;
    private javax.swing.JTextField txtBuscarProd;
    // End of variables declaration//GEN-END:variables
}
