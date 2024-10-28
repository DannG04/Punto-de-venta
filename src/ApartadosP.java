
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
    
    public ApartadosP() {
        initComponents();
        modelo=(DefaultTableModel)tablaAp.getModel();
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
        regP = new javax.swing.JButton();
        regAp = new javax.swing.JButton();
        panelApartado = new javax.swing.JPanel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        cantPagF = new javax.swing.JFormattedTextField();
        jLabel11 = new javax.swing.JLabel();
        vigF = new javax.swing.JFormattedTextField();
        hechoB4 = new javax.swing.JButton();
        panelSald = new javax.swing.JPanel();
        jLabel12 = new javax.swing.JLabel();
        idApF = new javax.swing.JFormattedTextField();
        jLabel13 = new javax.swing.JLabel();
        cantSaldaF = new javax.swing.JFormattedTextField();
        regAPAR = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        reg = new javax.swing.JButton();
        elim = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tablaAp = new javax.swing.JTable();

        dialogAp.setTitle("Registro de Apartados");
        dialogAp.setMinimumSize(new java.awt.Dimension(400, 310));
        dialogAp.setModal(true);
        dialogAp.setResizable(false);
        dialogAp.setSize(new java.awt.Dimension(400, 310));
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

        regP.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        regP.setForeground(new java.awt.Color(78, 150, 150));
        regP.setText("Registrar producto");
        regP.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                regPActionPerformed(evt);
            }
        });
        panelProducto.add(regP);

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

        jLabel9.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        jLabel9.setForeground(new java.awt.Color(78, 150, 150));
        jLabel9.setText("Cantidad mínima a pagar:");
        panelApartado.add(jLabel9);

        jLabel10.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        jLabel10.setForeground(new java.awt.Color(78, 150, 150));
        jLabel10.setText("Cantidad Pagada:");
        panelApartado.add(jLabel10);

        cantPagF.setFont(new java.awt.Font("Noto Serif", 0, 18)); // NOI18N
        cantPagF.setPreferredSize(new java.awt.Dimension(150, 30));
        panelApartado.add(cantPagF);

        jLabel11.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        jLabel11.setForeground(new java.awt.Color(78, 150, 150));
        jLabel11.setText("Vigencia:");
        panelApartado.add(jLabel11);

        vigF.setFont(new java.awt.Font("Noto Serif", 0, 18)); // NOI18N
        vigF.setPreferredSize(new java.awt.Dimension(150, 30));
        panelApartado.add(vigF);

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
        jLabel12.setText("ID Apartado:");
        panelSald.add(jLabel12);

        idApF.setFont(new java.awt.Font("Noto Serif", 0, 18)); // NOI18N
        idApF.setPreferredSize(new java.awt.Dimension(150, 30));
        panelSald.add(idApF);

        jLabel13.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        jLabel13.setForeground(new java.awt.Color(78, 150, 150));
        jLabel13.setText("Salda con:");
        panelSald.add(jLabel13);

        cantSaldaF.setEditable(false);
        cantSaldaF.setFont(new java.awt.Font("Noto Serif", 0, 18)); // NOI18N
        cantSaldaF.setPreferredSize(new java.awt.Dimension(150, 30));
        panelSald.add(cantSaldaF);

        regAPAR.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        regAPAR.setForeground(new java.awt.Color(78, 150, 150));
        regAPAR.setText("Saldar");
        regAPAR.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                regAPARActionPerformed(evt);
            }
        });
        panelSald.add(regAPAR);

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

        elim.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        elim.setForeground(new java.awt.Color(78, 150, 150));
        elim.setText("Saldar");
        elim.setPreferredSize(new java.awt.Dimension(125, 35));
        elim.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                elimActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        jPanel2.add(elim, gridBagConstraints);

        add(jPanel2, java.awt.BorderLayout.EAST);

        jLabel7.setFont(new java.awt.Font("Noto Serif", 1, 36)); // NOI18N
        jLabel7.setForeground(new java.awt.Color(78, 150, 150));
        jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel7.setText("Sistema de Apartados");
        jPanel1.add(jLabel7);

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
        dialogAp.setVisible(true);
    }//GEN-LAST:event_regActionPerformed

    private void elimActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_elimActionPerformed
        paneles(3);
        dialogAp.setVisible(true);
    }//GEN-LAST:event_elimActionPerformed

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

    private void regPActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_regPActionPerformed
        
    }//GEN-LAST:event_regPActionPerformed

    private void regApActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_regApActionPerformed
        paneles(2);
    }//GEN-LAST:event_regApActionPerformed

    private void hechoB4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hechoB4ActionPerformed
        dialogAp.setVisible(false);
    }//GEN-LAST:event_hechoB4ActionPerformed

    private void regAPARActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_regAPARActionPerformed
        dialogAp.setVisible(false);
    }//GEN-LAST:event_regAPARActionPerformed

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

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JFormattedTextField cantPagF;
    private javax.swing.JFormattedTextField cantSaldaF;
    private javax.swing.JFormattedTextField cantp;
    private javax.swing.JFormattedTextField codp;
    private javax.swing.JDialog dialogAp;
    private javax.swing.JButton elim;
    private javax.swing.JButton hechoB4;
    private javax.swing.JFormattedTextField idApF;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
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
    private javax.swing.JButton reg;
    private javax.swing.JButton regAPAR;
    private javax.swing.JButton regAp;
    private javax.swing.JButton regP;
    private javax.swing.JTable tablaAp;
    private javax.swing.JFormattedTextField vigF;
    // End of variables declaration//GEN-END:variables
}
