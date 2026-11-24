package puntoventa.ui;

import puntoventa.db.*;
import puntoventa.report.*;
import puntoventa.util.*;
import puntoventa.web.*;


import java.awt.Color;
import java.awt.Font;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import javax.swing.table.DefaultTableModel;

/*
 * Panel Kardex: historial de movimientos de inventario por producto.
 * Los componentes que no están en KardexP.form se agregan en el constructor.
 */
public class KardexP extends javax.swing.JPanel {

    ConexionBD conect = new ConexionBD();
    DefaultTableModel modeloKardex = new DefaultTableModel();
    private String idProductoSeleccionado = null;
    // display del combo -> {id, nombre, cantidad, estatus}
    private java.util.LinkedHashMap<String, String[]> productosEncontrados = new java.util.LinkedHashMap<>();

    // Componentes agregados fuera del editor de formularios
    private final javax.swing.JCheckBox chkIncluirBaja = new javax.swing.JCheckBox("Incluir dados de baja");
    private final javax.swing.JButton btnHoy = new javax.swing.JButton("Hoy");
    private final javax.swing.JButton btn7Dias = new javax.swing.JButton("7 días");
    private final javax.swing.JButton btnMes = new javax.swing.JButton("Este mes");
    private final javax.swing.JLabel lblTipo = new javax.swing.JLabel("Tipo:");
    private final javax.swing.JComboBox<String> cmbTipo = new javax.swing.JComboBox<>();
    private final javax.swing.JLabel lblResumen = new javax.swing.JLabel(" ");
    private final javax.swing.JLabel lblEstado = new javax.swing.JLabel(" ");
    private final javax.swing.Timer timerBusqueda = new javax.swing.Timer(300, e -> buscarProductos());

    private KardexPeriodo periodoActual;
    /** Movimientos visibles en la tabla (después del filtro por tipo), en el mismo orden. */
    private final java.util.List<KardexPeriodo.Movimiento> mostrados = new java.util.ArrayList<>();

    private static final String TODOS_LOS_TIPOS = "Todos los tipos";
    private static final DateTimeFormatter FMT_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final int COL_FOLIO = 2, COL_ENTRADA = 3, COL_SALIDA = 4, COL_SALDO = 5;
    private static final Color VERDE = new Color(0, 128, 60);
    private static final Color ROJO = new Color(190, 30, 30);
    private static final Color AZUL = new Color(30, 90, 170);
    private static final Color AMARILLO = new Color(255, 243, 205);

    public KardexP() {
        initComponents();

        modeloKardex = new DefaultTableModel(new Object[][]{},
                new String[]{"Fecha", "Tipo", "Folio", "Entrada", "Salida", "Saldo", "Empleado", "Observación"}) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tablaKardex.setModel(modeloKardex);
        configurarTabla();

        spnDesde.setModel(new javax.swing.SpinnerDateModel());
        spnDesde.setEditor(new javax.swing.JSpinner.DateEditor(spnDesde, "dd/MM/yyyy"));
        spnHasta.setModel(new javax.swing.SpinnerDateModel());
        spnHasta.setEditor(new javax.swing.JSpinner.DateEditor(spnHasta, "dd/MM/yyyy"));

        configurarFiltros();
        configurarResumen();

        LocalDate hoy = LocalDate.now();
        ponerRango(hoy.withDayOfMonth(1), hoy);
        timerBusqueda.setRepeats(false);
    }

    private void configurarTabla() {
        tablaKardex.setRowHeight(24);
        // El espacio sobrante va a "Observación"; las columnas numéricas conservan su ancho.
        tablaKardex.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_LAST_COLUMN);
        tablaKardex.getTableHeader().setReorderingAllowed(false);
        int[] anchos = {140, 170, 160, 80, 80, 80, 170, 320};
        for (int i = 0; i < anchos.length; i++) {
            tablaKardex.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);
        }
        javax.swing.table.DefaultTableCellRenderer render = new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(javax.swing.JTable t, Object valor,
                    boolean sel, boolean foco, int fila, int col) {
                super.getTableCellRendererComponent(t, valor, sel, foco, fila, col);
                int filaModelo = t.convertRowIndexToModel(fila);
                KardexPeriodo.Movimiento m = filaModelo < mostrados.size() ? mostrados.get(filaModelo) : null;
                setHorizontalAlignment(col >= COL_ENTRADA && col <= COL_SALDO ? RIGHT : LEFT);
                setFont(t.getFont().deriveFont(col == COL_SALDO ? Font.BOLD : Font.PLAIN));
                setToolTipText(null);
                if (!sel) {
                    setBackground(m != null && m.salto ? AMARILLO : t.getBackground());
                    Color fg = t.getForeground();
                    if (col == COL_ENTRADA) fg = VERDE;
                    else if (col == COL_SALIDA) fg = ROJO;
                    else if (col == COL_FOLIO) fg = AZUL;
                    setForeground(fg);
                }
                if (m != null && m.salto) {
                    setToolTipText("Salto: la existencia anterior era " + m.existenciaAnterior
                            + ", pero el saldo previo del kardex no coincide. Falta algún movimiento.");
                } else if (col == COL_FOLIO && valor != null && !valor.toString().isEmpty()) {
                    setToolTipText("Doble clic para ver el documento");
                }
                return this;
            }
        };
        for (int i = 0; i < tablaKardex.getColumnCount(); i++) {
            tablaKardex.getColumnModel().getColumn(i).setCellRenderer(render);
        }
        tablaKardex.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int fila = tablaKardex.rowAtPoint(e.getPoint());
                    if (fila >= 0) abrirDocumento(tablaKardex.convertRowIndexToModel(fila));
                }
            }
        });
    }

    private void configurarFiltros() {
        chkIncluirBaja.setFont(new Font("Noto Serif", Font.PLAIN, 14));
        chkIncluirBaja.setOpaque(false);
        chkIncluirBaja.addActionListener(e -> buscarProductos());
        pnlBusqueda.add(chkIncluirBaja);

        Font fBoton = new Font("Noto Serif", Font.BOLD, 14);
        btnHoy.setFont(fBoton);
        btnHoy.addActionListener(e -> { ponerRango(LocalDate.now(), LocalDate.now()); cargarKardex(); });
        btn7Dias.setFont(fBoton);
        btn7Dias.addActionListener(e -> { ponerRango(LocalDate.now().minusDays(6), LocalDate.now()); cargarKardex(); });
        btnMes.setFont(fBoton);
        btnMes.addActionListener(e -> { ponerRango(LocalDate.now().withDayOfMonth(1), LocalDate.now()); cargarKardex(); });

        lblTipo.setFont(new Font("Noto Serif", Font.BOLD, 16));
        cmbTipo.setFont(new Font("Noto Serif", Font.PLAIN, 14));
        cmbTipo.addItem(TODOS_LOS_TIPOS);
        for (String t : KardexPeriodo.TIPOS) cmbTipo.addItem(t);
        cmbTipo.addActionListener(e -> mostrarMovimientos());

        // Insertar después del selector "Hasta", antes de Filtrar / Exportar
        int i = java.util.Arrays.asList(pnlFiltros.getComponents()).indexOf(spnHasta) + 1;
        for (java.awt.Component c : new java.awt.Component[]{btnHoy, btn7Dias, btnMes, lblTipo, cmbTipo}) {
            pnlFiltros.add(c, i++);
        }
    }

    private void configurarResumen() {
        javax.swing.JPanel pnlResumen = new javax.swing.JPanel(new java.awt.GridLayout(2, 1, 0, 4));
        pnlResumen.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 8, 8, 8));
        lblResumen.setFont(new Font("Noto Serif", Font.BOLD, 15));
        lblResumen.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblEstado.setFont(new Font("Noto Serif", Font.BOLD, 15));
        lblEstado.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        pnlResumen.add(lblResumen);
        pnlResumen.add(lblEstado);
        pnlKardexMain.add(pnlResumen, java.awt.BorderLayout.SOUTH);
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
        lblKardexTitulo.setIcon(SvgIcon.load("/icons/kardex.svg", SvgIcon.LARGE));
        lblKardexTitulo.setText("Kardex de Producto");
        pnlKardexTitulo.add(lblKardexTitulo);

        add(pnlKardexTitulo, java.awt.BorderLayout.NORTH);

        pnlKardexMain.setLayout(new java.awt.BorderLayout());

        pnlKardexControls.setLayout(new java.awt.GridLayout(3, 1));

        lblBuscar.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
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
        lblNombreProd.setText("Nombre: —");
        pnlInfoProd.add(lblNombreProd);

        lblCodigoProd.setFont(new java.awt.Font("Noto Serif", 1, 14)); // NOI18N
        lblCodigoProd.setText("   |   Código: —");
        pnlInfoProd.add(lblCodigoProd);

        lblExistenciaProd.setFont(new java.awt.Font("Noto Serif", 1, 14)); // NOI18N
        lblExistenciaProd.setText("   |   Existencia: —");
        pnlInfoProd.add(lblExistenciaProd);

        pnlKardexControls.add(pnlInfoProd);

        lblDesde.setFont(new java.awt.Font("Noto Serif", 1, 16)); // NOI18N
        lblDesde.setText("Desde:");
        pnlFiltros.add(lblDesde);

        spnDesde.setPreferredSize(new java.awt.Dimension(140, 32));
        spnDesde.setFont(new java.awt.Font("Noto Serif", Font.PLAIN, 15));
        pnlFiltros.add(spnDesde);

        lblHasta.setFont(new java.awt.Font("Noto Serif", 1, 16)); // NOI18N
        lblHasta.setText("Hasta:");
        pnlFiltros.add(lblHasta);

        spnHasta.setPreferredSize(new java.awt.Dimension(140, 32));
        spnHasta.setFont(new java.awt.Font("Noto Serif", Font.PLAIN, 15));
        pnlFiltros.add(spnHasta);

        btnFiltrar.setFont(new java.awt.Font("Noto Serif", 1, 16)); // NOI18N
        btnFiltrar.setBackground(new java.awt.Color(153, 204, 255));
        btnFiltrar.setIcon(SvgIcon.load("/icons/filtro.svg", SvgIcon.MEDIUM));
        btnFiltrar.setText("Filtrar");
        btnFiltrar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnFiltrarActionPerformed(evt);
            }
        });
        pnlFiltros.add(btnFiltrar);

        btnExportarKardex.setFont(new java.awt.Font("Noto Serif", 1, 16)); // NOI18N
        btnExportarKardex.setBackground(new java.awt.Color(125, 255, 177));
        btnExportarKardex.setIcon(SvgIcon.load("/icons/excel.svg", SvgIcon.MEDIUM));
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
        // Enter (o un lector de código de barras) busca de inmediato; al teclear se espera una pausa.
        if (evt != null && evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
            timerBusqueda.stop();
            buscarProductos();
        } else {
            timerBusqueda.restart();
        }
    }//GEN-LAST:event_txtBuscarProdKeyReleased

    private void cmbProductosKActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbProductosKActionPerformed
        String selected = (String) cmbProductosK.getSelectedItem();
        if (selected == null || !productosEncontrados.containsKey(selected)) return;
        String[] info = productosEncontrados.get(selected);
        idProductoSeleccionado = info[0];
        lblNombreProd.setText("Nombre: " + info[1] + ("Activo".equals(info[3]) ? "" : " (dado de baja)"));
        lblCodigoProd.setText("   |   Código: " + info[0]);
        lblExistenciaProd.setText("   |   Existencia: " + info[2]);
        cargarKardex();
    }//GEN-LAST:event_cmbProductosKActionPerformed

    private void btnFiltrarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFiltrarActionPerformed
        cargarKardex();
    }//GEN-LAST:event_btnFiltrarActionPerformed

    private void btnExportarKardexActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExportarKardexActionPerformed
        if (idProductoSeleccionado == null || periodoActual == null) {
            Mise.JOption("Seleccione un producto primero", "Aviso", javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }
        String nombreProd = lblNombreProd.getText().replace("Nombre: ", "");
        Excel.reporteKardex(nombreProd, periodoActual, new java.util.ArrayList<>(mostrados), tipoSeleccionado());
    }//GEN-LAST:event_btnExportarKardexActionPerformed

    private void buscarProductos() {
        String filtro = txtBuscarProd.getText().trim();
        if (filtro.length() < 2) return;
        productosEncontrados.clear();
        cmbProductosK.removeAllItems();
        java.sql.ResultSet rs = conect.buscarProductoKardex(filtro, chkIncluirBaja.isSelected());
        try {
            while (rs != null && rs.next()) {
                String id = rs.getString("id_producto");
                String nombre = rs.getString("nombre");
                String estatus = rs.getString("estatus");
                String display = id + " - " + nombre + ("Activo".equals(estatus) ? "" : " [baja]");
                productosEncontrados.put(display, new String[]{id, nombre, rs.getString("cantidad"), estatus});
                cmbProductosK.addItem(display);  // el primero se selecciona y carga su kardex
            }
        } catch (java.sql.SQLException e) {
            GestorErrores.registrar(e);
        }
    }

    private void cargarKardex() {
        if (idProductoSeleccionado == null) return;
        LocalDate desde = fecha(spnDesde);
        LocalDate hasta = fecha(spnHasta);
        if (desde.isAfter(hasta)) {
            Mise.JOption("La fecha \"Desde\" no puede ser posterior a \"Hasta\".", "Fechas", javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }
        periodoActual = conect.consultarKardex(idProductoSeleccionado, desde, hasta);
        lblExistenciaProd.setText("   |   Existencia: " + periodoActual.existenciaActual);
        mostrarMovimientos();
    }

    /** Llena la tabla con los movimientos del periodo, aplicando el filtro por tipo. */
    private void mostrarMovimientos() {
        Mise.limpiarTabla(modeloKardex);
        mostrados.clear();
        if (periodoActual == null) return;
        String tipo = tipoSeleccionado();
        for (KardexPeriodo.Movimiento m : periodoActual.movimientos) {
            if (tipo != null && !tipo.equals(m.tipo)) continue;
            mostrados.add(m);
            modeloKardex.addRow(new Object[]{
                m.fecha.format(FMT_FECHA),
                m.tipo,
                m.referencia == null ? "" : m.referencia,
                m.entrada() == 0 ? "" : m.entrada(),
                m.salida() == 0 ? "" : m.salida(),
                m.existenciaPosterior,
                m.empleado == null ? "" : m.empleado,
                m.observacion == null ? "" : m.observacion
            });
        }
        actualizarResumen();
    }

    private void actualizarResumen() {
        KardexPeriodo p = periodoActual;
        String resumen = String.format("Saldo inicial: %d     Entradas: +%d     Salidas: -%d     Saldo final: %d     Existencia actual: %d",
                p.saldoInicial, p.entradas, p.salidas, p.saldoFinal(), p.existenciaActual);
        if (tipoSeleccionado() != null) {
            resumen += String.format("     (mostrando %d de %d movimientos)", mostrados.size(), p.movimientos.size());
        }
        lblResumen.setText(resumen);

        lblEstado.setForeground(!p.cuadra() ? ROJO : p.llegaAHoy() ? VERDE : Color.DARK_GRAY);
        lblEstado.setText(p.descripcionEstado() + (p.saltos > 0 ? " Los saltos se marcan en amarillo." : ""));
    }

    private void abrirDocumento(int filaModelo) {
        if (filaModelo < 0 || filaModelo >= mostrados.size()) return;
        KardexPeriodo.Movimiento m = mostrados.get(filaModelo);
        if (m.referencia == null || m.referencia.isEmpty()) {
            Mise.JOption("Este movimiento no tiene un documento asociado."
                    + (m.observacion == null ? "" : "\n" + m.observacion), "Kardex", javax.swing.JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        DocumentoKardex doc = conect.documentoKardex(m.referencia);
        if (doc == null) {
            Mise.JOption("No se encontró el documento " + m.referencia + ".", "Kardex", javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }
        mostrarDocumento(doc);
    }

    private void mostrarDocumento(DocumentoKardex doc) {
        javax.swing.JDialog dlg = new javax.swing.JDialog(javax.swing.SwingUtilities.getWindowAncestor(this),
                doc.titulo, java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setLayout(new java.awt.BorderLayout(8, 8));

        javax.swing.JPanel encabezado = new javax.swing.JPanel(new java.awt.GridBagLayout());
        encabezado.setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 12, 4, 12));
        java.awt.GridBagConstraints gc = new java.awt.GridBagConstraints();
        gc.anchor = java.awt.GridBagConstraints.WEST;
        gc.insets = new java.awt.Insets(2, 0, 2, 16);
        int filaEnc = 0;
        for (String[] dato : doc.datos) {
            javax.swing.JLabel etiqueta = new javax.swing.JLabel(dato[0] + ":");
            etiqueta.setFont(new Font("Noto Serif", Font.BOLD, 14));
            javax.swing.JLabel valor = new javax.swing.JLabel(dato[1] == null ? "" : dato[1]);
            valor.setFont(new Font("Noto Serif", Font.PLAIN, 14));
            gc.gridy = filaEnc++;
            gc.gridx = 0; gc.weightx = 0;
            encabezado.add(etiqueta, gc);
            gc.gridx = 1; gc.weightx = 1;
            encabezado.add(valor, gc);
        }
        if (doc.filas.isEmpty()) {
            javax.swing.JLabel aviso = new javax.swing.JLabel(
                    "El documento ya no tiene renglones: se quitaron después de registrar el movimiento.");
            aviso.setFont(new Font("Noto Serif", Font.ITALIC, 14));
            aviso.setForeground(ROJO);
            gc.gridy = filaEnc; gc.gridx = 0; gc.gridwidth = 2;
            encabezado.add(aviso, gc);
        }
        dlg.add(encabezado, java.awt.BorderLayout.NORTH);

        DefaultTableModel modelo = new DefaultTableModel(doc.columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        for (Object[] fila : doc.filas) modelo.addRow(fila);
        javax.swing.JTable tabla = new javax.swing.JTable(modelo);
        tabla.setFont(new Font("Noto Serif", Font.PLAIN, 14));
        tabla.setRowHeight(22);
        dlg.add(new javax.swing.JScrollPane(tabla), java.awt.BorderLayout.CENTER);

        javax.swing.JButton cerrar = new javax.swing.JButton("Cerrar");
        cerrar.setFont(new Font("Noto Serif", Font.BOLD, 14));
        cerrar.addActionListener(e -> dlg.dispose());
        javax.swing.JPanel pie = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));
        pie.add(cerrar);
        dlg.add(pie, java.awt.BorderLayout.SOUTH);

        dlg.setSize(780, 440);
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }

    /** Tipo elegido en el filtro, o null si se muestran todos. */
    private String tipoSeleccionado() {
        Object t = cmbTipo.getSelectedItem();
        return t == null || TODOS_LOS_TIPOS.equals(t) ? null : t.toString();
    }

    private void ponerRango(LocalDate desde, LocalDate hasta) {
        spnDesde.setValue(java.util.Date.from(desde.atStartOfDay(ZoneId.systemDefault()).toInstant()));
        spnHasta.setValue(java.util.Date.from(hasta.atStartOfDay(ZoneId.systemDefault()).toInstant()));
    }

    private static LocalDate fecha(javax.swing.JSpinner spn) {
        try {
            spn.commitEdit();  // toma lo que el usuario haya escrito aunque no haya salido del campo
        } catch (java.text.ParseException ignored) {
            // texto inválido: se usa el último valor válido
        }
        return ((java.util.Date) spn.getValue()).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
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
