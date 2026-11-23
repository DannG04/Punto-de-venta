package puntoventa.ui;

import puntoventa.db.*;
import puntoventa.report.*;
import puntoventa.util.*;
import puntoventa.web.*;

public class TicketDialog extends javax.swing.JDialog {

    private final TicketData datos;
    private final ConexionBD conect;
    private boolean yaImprimio = false;

    public TicketDialog(TicketData datos, ConexionBD conect) {
        super((java.awt.Frame) null, true);
        this.datos = datos;
        this.conect = conect;
        initComponents();
        ticketPane.setText(TicketBuilder.construir(datos));
        ticketPane.setCaretPosition(0);
        setTitle("Ticket");
        setSize(420, 580);
        setResizable(false);
        setLocationRelativeTo(null);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                cerrar();
            }
        });
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        ticketPane = new javax.swing.JEditorPane();
        pnlBotones = new javax.swing.JPanel();
        btnImprimir = new javax.swing.JButton();
        btnCerrar = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);

        ticketPane.setContentType("text/html"); // NOI18N
        ticketPane.setEditable(false);
        jScrollPane1.setViewportView(ticketPane);

        getContentPane().add(jScrollPane1, java.awt.BorderLayout.CENTER);

        pnlBotones.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        btnImprimir.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        btnImprimir.setBackground(new java.awt.Color(153, 204, 255));
        btnImprimir.setIcon(SvgIcon.load("/icons/imprimir.svg", SvgIcon.MEDIUM));
        btnImprimir.setText("Imprimir");
        btnImprimir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnImprimirActionPerformed(evt);
            }
        });
        pnlBotones.add(btnImprimir);

        btnCerrar.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        btnCerrar.setBackground(new java.awt.Color(252, 149, 149));
        btnCerrar.setIcon(SvgIcon.load("/icons/cancelar.svg", SvgIcon.MEDIUM));
        btnCerrar.setText("Cerrar");
        btnCerrar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCerrarActionPerformed(evt);
            }
        });
        pnlBotones.add(btnCerrar);

        getContentPane().add(pnlBotones, java.awt.BorderLayout.SOUTH);
    }// GEN-END:initComponents

    private void btnImprimirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnImprimirActionPerformed
        imprimirSegunTipo();
        yaImprimio = true;
        dispose();
    }//GEN-LAST:event_btnImprimirActionPerformed

    private void btnCerrarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCerrarActionPerformed
        cerrar();
    }//GEN-LAST:event_btnCerrarActionPerformed

    private void cerrar() {
        if (!yaImprimio && hayImpresora()) {
            imprimirSegunTipo();
        }
        dispose();
    }

    private boolean hayImpresora() {
        return javax.print.PrintServiceLookup.lookupDefaultPrintService() != null;
    }

    private void imprimirSegunTipo() {
        switch (datos.tipo) {
            case VENTA:
                GenTicket.generarTicketVenta(datos.idTransaccion);
                break;
            case APARTADO_NUEVO:
            case APARTADO_SALDO:
                GenTicket.generarTicketApartado(conect.seleccionarApartado(datos.idTransaccion));
                break;
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCerrar;
    private javax.swing.JButton btnImprimir;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPanel pnlBotones;
    private javax.swing.JEditorPane ticketPane;
    // End of variables declaration//GEN-END:variables
}
