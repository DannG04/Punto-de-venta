package puntoventa.ui;

import static pruebas.Prueba.*;

import javax.swing.*;

/**
 * Revisión final, Critical 1: Enter en el ticket debe llegar a "imprimir" desde el componente
 * que recibe el foco (el JEditorPane del ticket se tragaba el Enter con insert-break).
 * Necesita pantalla (no headless): java -cp ... puntoventa.ui.TeclasTicketPrueba
 */
public class TeclasTicketPrueba {
    public static void main(String[] a) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            puntoventa.report.TicketData d = new puntoventa.report.TicketData();
            d.tipo = puntoventa.report.TipoTicket.VENTA;
            d.idTransaccion = "VPRUEBA";
            TicketDialog t = new TicketDialog(d, null);
            t.activarTeclasRapidas();
            KeyStroke enter = KeyStroke.getKeyStroke("ENTER");
            for (java.awt.Component c : todos(t.getContentPane())) {
                if (c instanceof JComponent && c.isFocusable()) {
                    Object accion = ((JComponent) c).getInputMap(JComponent.WHEN_FOCUSED).get(enter);
                    check(accion == null || "imprimir".equals(accion),
                            "CU-CR-17 ningún componente enfocable se traga Enter: " + c.getClass().getSimpleName() + " -> " + accion);
                }
            }
            t.dispose();
        });
        terminar("TeclasTicketPrueba");
    }

    private static java.util.List<java.awt.Component> todos(java.awt.Container raiz) {
        java.util.List<java.awt.Component> r = new java.util.ArrayList<>();
        for (java.awt.Component c : raiz.getComponents()) {
            r.add(c);
            if (c instanceof java.awt.Container) r.addAll(todos((java.awt.Container) c));
        }
        return r;
    }
}
