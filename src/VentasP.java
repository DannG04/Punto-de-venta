
import br.com.adilson.util.Extenso;
import br.com.adilson.util.PrinterMatrix;
import java.io.FileInputStream;
import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.SimpleDoc;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
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
public class VentasP extends javax.swing.JPanel {

    static int diaV = 0;
            
    String prod = "";
    Integer cant = 0;
    Integer precioU = 0;
    Integer precioUT = 0;
    String cod = "";
    
    Integer desc = 0;
    
    static Integer precT = 0;
    DefaultTableModel modelo = new DefaultTableModel();
    
    static java.util.ArrayList<String> registroCod = new java.util.ArrayList<>();
    static java.util.ArrayList<Integer> registroCant = new java.util.ArrayList<>();
    static java.util.ArrayList<Integer> registroPrecU = new java.util.ArrayList<>();
    static java.util.ArrayList<Integer> registroPrecT = new java.util.ArrayList<>();
    
    
    /**
     * Creates new form Ventas
     */
    public VentasP() {
        initComponents();
        modelo=(DefaultTableModel)tablaVentas.getModel();
        totalLabel.setText("Total: " + precT);
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

        jDialog1 = new javax.swing.JDialog();
        jLabel2 = new javax.swing.JLabel();
        codp = new javax.swing.JFormattedTextField();
        jLabel7 = new javax.swing.JLabel();
        cantP = new javax.swing.JFormattedTextField();
        hechoB2 = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        tablaVentas = new javax.swing.JTable();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        codigoProd = new javax.swing.JFormattedTextField();
        cantidadProd = new javax.swing.JFormattedTextField();
        jLabel4 = new javax.swing.JLabel();
        totalLabel = new javax.swing.JLabel();
        reg = new javax.swing.JButton();
        genT = new javax.swing.JButton();
        elim = new javax.swing.JButton();
        actB = new javax.swing.JButton();

        jDialog1.setModal(true);
        jDialog1.setResizable(false);
        jDialog1.setSize(new java.awt.Dimension(230, 180));
        jDialog1.getContentPane().setLayout(new java.awt.GridLayout(0, 1));

        jLabel2.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(78, 150, 150));
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setText("Código del Producto:");
        jDialog1.getContentPane().add(jLabel2);

        codp.setEditable(false);
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
        jDialog1.getContentPane().add(codp);

        jLabel7.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        jLabel7.setForeground(new java.awt.Color(78, 150, 150));
        jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel7.setText("Cantidad: ");
        jDialog1.getContentPane().add(jLabel7);

        cantP.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        cantP.setPreferredSize(new java.awt.Dimension(200, 35));
        cantP.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                cantPKeyTyped(evt);
            }
        });
        jDialog1.getContentPane().add(cantP);

        hechoB2.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        hechoB2.setForeground(new java.awt.Color(78, 150, 150));
        hechoB2.setText("Hecho");
        hechoB2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                hechoB2ActionPerformed(evt);
            }
        });
        jDialog1.getContentPane().add(hechoB2);
        hechoB2.setVisible(false);

        jDialog1.setLocationRelativeTo(null);

        setLayout(new java.awt.BorderLayout());

        jScrollPane1.setPreferredSize(new java.awt.Dimension(540, 402));

        tablaVentas.setFont(new java.awt.Font("Noto Serif", 0, 14)); // NOI18N
        tablaVentas.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Producto", "Cantidad", "Precio Unitario", "Precio Total"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane1.setViewportView(tablaVentas);

        add(jScrollPane1, java.awt.BorderLayout.CENTER);

        jLabel1.setFont(new java.awt.Font("Noto Serif", 1, 36)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(78, 150, 150));
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("Ventas");
        jPanel1.add(jLabel1);

        add(jPanel1, java.awt.BorderLayout.NORTH);

        jPanel2.setLayout(new java.awt.GridBagLayout());

        jLabel3.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(78, 150, 150));
        jLabel3.setText("Código:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.ipadx = 16;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        jPanel2.add(jLabel3, gridBagConstraints);

        codigoProd.setFont(new java.awt.Font("Noto Serif", 0, 18)); // NOI18N
        codigoProd.setPreferredSize(new java.awt.Dimension(150, 30));
        codigoProd.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                codigoProdKeyTyped(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        jPanel2.add(codigoProd, gridBagConstraints);

        cantidadProd.setFont(new java.awt.Font("Noto Serif", 0, 18)); // NOI18N
        cantidadProd.setPreferredSize(new java.awt.Dimension(150, 30));
        cantidadProd.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                cantidadProdKeyTyped(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        jPanel2.add(cantidadProd, gridBagConstraints);

        jLabel4.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(78, 150, 150));
        jLabel4.setText("Cantidad:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.ipadx = 16;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        jPanel2.add(jLabel4, gridBagConstraints);

        totalLabel.setFont(new java.awt.Font("Noto Serif", 1, 40)); // NOI18N
        totalLabel.setForeground(new java.awt.Color(78, 150, 150));
        totalLabel.setText("Total: ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 120, 10);
        jPanel2.add(totalLabel, gridBagConstraints);

        reg.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        reg.setForeground(new java.awt.Color(78, 150, 150));
        reg.setText("Registrar");
        reg.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                regActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.insets = new java.awt.Insets(20, 10, 10, 10);
        jPanel2.add(reg, gridBagConstraints);

        genT.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        genT.setForeground(new java.awt.Color(78, 150, 150));
        genT.setText("Generar Ticket");
        genT.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                genTActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTH;
        gridBagConstraints.insets = new java.awt.Insets(50, 10, 10, 10);
        jPanel2.add(genT, gridBagConstraints);

        elim.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        elim.setForeground(new java.awt.Color(78, 150, 150));
        elim.setText("Eliminar");
        elim.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                elimActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        jPanel2.add(elim, gridBagConstraints);

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
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        jPanel2.add(actB, gridBagConstraints);

        add(jPanel2, java.awt.BorderLayout.LINE_END);
    }// </editor-fold>//GEN-END:initComponents

    private void regActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_regActionPerformed
        // TODO add your handling code here:
        if(!codigoProd.getText().isEmpty() && !cantidadProd.getText().isEmpty()){
            guardarDatos();
            
            //LIMPIAR
            codigoProd.setText("");
            cantidadProd.setText("");
        }
    }//GEN-LAST:event_regActionPerformed

    private void genTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_genTActionPerformed
        // TODO add your handling code here:
        //GENERA TICKET
        generarTicket();
        limpiarTabla();
        
        diaV = diaV + precT;
        precT = 0;
        totalLabel.setText("Total: " + precT);
        
        //Se elimina de la BD
        registroCod.clear();
        registroCant.clear();
        registroPrecT.clear();
        registroPrecU.clear();
    }//GEN-LAST:event_genTActionPerformed

    private void codigoProdKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_codigoProdKeyTyped
        // TODO add your handling code here:
        char letra = evt.getKeyChar();
        if(!Cake.letrasMayus(letra) && !Cake.numeros(letra)){
            evt.consume();
        }
    }//GEN-LAST:event_codigoProdKeyTyped

    private void cantidadProdKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_cantidadProdKeyTyped
        // TODO add your handling code here:
        char letra = evt.getKeyChar();
        if(!Cake.numeros(letra)){
            evt.consume();
        }
    }//GEN-LAST:event_cantidadProdKeyTyped

    private void elimActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_elimActionPerformed
        if(tablaVentas.getSelectedRow() != -1){
            int a = tablaVentas.getSelectedRow();
            eliminarElementos(a);
            totalLabel.setText("Total: " + precT);
        }
        else
            JOptionPane.showMessageDialog(null,"Debe seleccionar la fila que desea eliminar");
    }//GEN-LAST:event_elimActionPerformed

    private void codpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_codpActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_codpActionPerformed

    private void codpKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_codpKeyTyped

    }//GEN-LAST:event_codpKeyTyped

    private void cantPKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_cantPKeyTyped
        char letra = evt.getKeyChar();
        if(!Cake.numeros(letra)){
            evt.consume();
        }

        if(Cake.tamaño(cantP.getText(), 10)){
            evt.consume();
        }
    }//GEN-LAST:event_cantPKeyTyped

    private void hechoB2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hechoB2ActionPerformed
        // TODO add your handling code here:
        if(codp.getText().isEmpty() || cantP.getText().isEmpty()){
            javax.swing.JOptionPane.showMessageDialog(null, "Todos los campos deben ser completados", "Error",javax.swing.JOptionPane.ERROR_MESSAGE);
        }
        else{
            int a = tablaVentas.getSelectedRow();
            actualizarElementos(a);
            mostrarTabla();
            totalLabel.setText("Total: " + precT);
            jDialog1.setVisible(false);
        }
    }//GEN-LAST:event_hechoB2ActionPerformed

    private void actBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_actBActionPerformed
        // TODO add your handling code here:
        if(tablaVentas.getSelectedRow() != -1){
            int a = tablaVentas.getSelectedRow();
            hechoB2.setVisible(true);
            //MOSTRAR LOS DATOS EN EL FIELD
            codp.setText(registroCod.get(a));
            cantP.setText("" + registroCant.get(a));
            
            jDialog1.setVisible(true);
        }
        else
            JOptionPane.showMessageDialog(null,"Debe seleccionar la fila que desea actualizar");
    }//GEN-LAST:event_actBActionPerformed

    public final void mostrarTabla(){
        if(modelo.getRowCount() != 0){
            limpiarTabla();
            llenarTabla();
        }
        else{
            llenarTabla();
        }
    }
    
    public void llenarTabla(){
        int a = registroCod.size();
        for(int i=0;i<a;i++){
            modelo.addRow(new Object[]{registroCod.get(i),registroCant.get(i), registroPrecU.get(i),registroPrecT.get(i)});
        }
    }
    
    public  void limpiarTabla(){
     int a=modelo.getRowCount();    
        while(a!=0){ 
            if(a!=0)
                modelo.removeRow(0);                      
            a=modelo.getRowCount();
        }
    }
    
    public void actualizarElementos(int a){
        registroCant.set(a, Integer.valueOf(cantP.getText()));
        precT = precT - registroPrecT.get(a);
        registroPrecT.set(a, registroPrecU.get(a) * registroCant.get(a));
        precT = precT + registroPrecT.get(a);
    }
    
    public void guardarDatos (){
        cod = codigoProd.getText();
        int a = InventarioP.codInv.indexOf(cod);
        if(a != -1){
            //Registramos el producto y verificamos si está o no
            if(registroCod.contains(cod)){
                acumularProd(cod, a);
            }
            else{
                consulta(a);
            }
            precT = precT + precioUT;
            totalLabel.setText("Total: " + precT);
        }
        else{
            JOptionPane.showMessageDialog(null, "No se encontró el producto", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public void consulta(int a){
        prod = InventarioP.nombreInv.get(a);
        cant = Integer.valueOf(cantidadProd.getText());
        //Decrementar la cantidad de productos y actualizarlo
        Integer b = InventarioP.cantInv.get(a)-cant;
        InventarioP.cantInv.set(a, b);
        InventarioP.mostrarTabla();
        //
        precioU = InventarioP.precioInv.get(a);
        precioUT = precioU*cant;
        modelo.addRow(new Object[]{prod,cant,precioU,precioUT});
        //Agregarlo al registro
        registroCod.add(cod);
        registroCant.add(cant);
        registroPrecU.add(precioU);
        registroPrecT.add(precioUT);
    }
    
    public void acumularProd(String cod, int c){
        int a = registroCod.indexOf(cod);
        //operacion y decremento en el inventario
        cant = Integer.valueOf(cantidadProd.getText());
        Integer b = InventarioP.cantInv.get(c)-cant;
        InventarioP.cantInv.set(c, b);
        InventarioP.mostrarTabla();
        
        precioU = registroPrecU.get(a);
        precioUT = precioU*cant;
        
        //se suma a lo acumulado y se registra
        cant = cant + registroCant.get(a);
        registroCant.set(a, cant);
        precioUT = precioUT + registroPrecT.get(a);
        registroPrecT.set(a, precioUT);
        
        //Actualizar la tabla
        modelo.setValueAt(cant, a, 1);
        modelo.setValueAt(precioUT, a, 3);
    }
    
    public void eliminarElementos(int a){
        //Se regresan los productos al inventario
        int b = InventarioP.codInv.indexOf(registroCod.get(a));
        int c = InventarioP.cantInv.get(b) + registroCant.get(a);
        InventarioP.cantInv.set(b, c);
        //Se actualiza el total
        precT = precT - registroPrecT.get(a);
        //Se elimina el elemento
        registroCod.remove(a);
        registroCant.remove(a);
        registroPrecU.remove(a);
        registroPrecT.remove(a);
        InventarioP.mostrarTabla();
        
        modelo.removeRow(tablaVentas.getSelectedRow());
    }
    
    public void generarTicket(){
//        String ticketcitu = "PRODUCTO   CANT    PCIO U. DESC    TOTAL";
//        
//        int max = registroCod.size();
//        for(int i=0; i<max; i++){
//            ticketcitu = ticketcitu + "\n" + registroCod.get(i) + "     " + registroCant.get(i) + "     " + desc + "%        " + registroPrecT.get(i);
//        }
//        
//        javax.swing.JOptionPane.showMessageDialog(null, ticketcitu, "Ticket", javax.swing.JOptionPane.PLAIN_MESSAGE);
          PrinterMatrix printer = new PrinterMatrix();
          
          String numeroFactura = "B0001", nombreVendedor = "Fer0";
          
          Extenso e = new Extenso();
          
          e.setNumber(101.85);
          printer.setOutSize(9, 32);
          
          printer.printCharAtCol(1, 1, 32, "*");
          printer.printTextWrap(1, 2, 8, 32, "FACTURA DE VENTA");
          printer.printTextWrap(2, 3, 1, 32, "Folio: "+numeroFactura);
          printer.printTextWrap(3, 3, 1, 32, "Fecha de emision : 00/00/00");
          printer.printTextWrap(4, 3, 1, 32, "Hora: 00:00");
          printer.printTextWrap(5, 3, 1, 32, "Vendedor: "+nombreVendedor);
          printer.printTextWrap(6, 3, 1, 32, "producto 1");
          printer.printTextWrap(7, 3, 1, 32, "------Muchas gracias por su compra.------");
          
          printer.toFile("impresion.txt");
          
          FileInputStream inputStream = null;
          
          try{
              inputStream = new FileInputStream("impresion.txt");

              }catch(Exception ex){
                  ex.printStackTrace();
              }
          if(inputStream==null){
              return;
          }
             
          DocFlavor  docFormat = DocFlavor.INPUT_STREAM.AUTOSENSE;
          Doc document = new SimpleDoc(inputStream, docFormat, null);
          PrintRequestAttributeSet attributeSet = new HashPrintRequestAttributeSet();
          PrintService defaultPrintService = PrintServiceLookup.lookupDefaultPrintService();
          
          if(defaultPrintService != null){
              DocPrintJob printJob = defaultPrintService.createPrintJob();
              try{
                  printJob.print(document, attributeSet);
                  
              }catch(Exception ex){
                  ex.printStackTrace();
              }
          }else{
              System.out.println("No hay una impresora instalada");
          }
              
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton actB;
    private javax.swing.JFormattedTextField cantP;
    private javax.swing.JFormattedTextField cantidadProd;
    private javax.swing.JFormattedTextField codigoProd;
    private javax.swing.JFormattedTextField codp;
    private javax.swing.JButton elim;
    private javax.swing.JButton genT;
    private javax.swing.JButton hechoB2;
    private javax.swing.JDialog jDialog1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton reg;
    private javax.swing.JTable tablaVentas;
    private javax.swing.JLabel totalLabel;
    // End of variables declaration//GEN-END:variables
}
