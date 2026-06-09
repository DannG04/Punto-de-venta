import java.io.File;

public class TicketBuilder {

    // Punto de extensión futuro: añadir sobrecarga construir(TicketData, TicketConfig)
    public static String construir(TicketData d) {
        return "<html><body>" +
            htmlEncabezado(d) +
            htmlDatosTransaccion(d) +
            htmlProductos(d) +
            htmlTotales(d) +
            htmlPie(d) +
            "</body></html>";
    }

    // TODO: if (config.mostrarEncabezado)
    private static String htmlEncabezado(TicketData d) {
        StringBuilder sb = new StringBuilder("<center>");
        if (!d.logoRuta.isEmpty()) {
            File f = new File(d.logoRuta);
            if (f.exists()) {
                sb.append("<img src=\"").append(f.toURI().toString())
                  .append("\" width=\"80\" height=\"80\"><br>");
            }
        }
        if (!d.empresaNombre.isEmpty())
            sb.append("<b><font size=\"4\">").append(esc(d.empresaNombre)).append("</font></b><br>");
        if (!d.empresaTel.isEmpty())
            sb.append("Tel: ").append(esc(d.empresaTel)).append("<br>");
        if (!d.empresaDireccion.isEmpty())
            sb.append(esc(d.empresaDireccion)).append("<br>");
        sb.append("</center><hr>");
        return sb.toString();
    }

    // TODO: if (config.mostrarDatosTransaccion)
    private static String htmlDatosTransaccion(TicketData d) {
        String titulo = d.tipo == TipoTicket.VENTA ? "COMPROBANTE DE VENTA"
                      : d.tipo == TipoTicket.APARTADO_NUEVO ? "COMPROBANTE DE APARTADO"
                      : "COMPROBANTE DE PAGO";
        StringBuilder sb = new StringBuilder();
        sb.append("<center><b>").append(titulo).append("</b></center><br>");
        sb.append("<b>Folio:</b> ").append(esc(d.idTransaccion)).append("<br>");
        sb.append("<b>Fecha:</b> ").append(esc(d.fecha)).append("<br>");
        if (!d.clienteNombre.isEmpty())
            sb.append("<b>Cliente:</b> ").append(esc(d.clienteNombre)).append("<br>");
        sb.append("<b>Vendedor:</b> ").append(esc(d.vendedor)).append("<br>");
        if (d.tipo == TipoTicket.APARTADO_NUEVO || d.tipo == TipoTicket.APARTADO_SALDO)
            sb.append("<b>Fecha límite:</b> ").append(esc(d.fechaLimite)).append("<br>");
        sb.append("<hr>");
        return sb.toString();
    }

    // TODO: if (config.mostrarProductos)
    private static String htmlProductos(TicketData d) {
        if (d.productos.isEmpty()) return "";
        StringBuilder sb = new StringBuilder(
            "<table width=\"100%\" border=\"0\" cellpadding=\"2\">" +
            "<tr><td><b>#</b></td><td><b>Producto</b></td>" +
            "<td><b>Cant</b></td><td><b>P.Unit</b></td><td><b>Total</b></td></tr>");
        for (String[] p : d.productos) {
            sb.append("<tr><td>").append(esc(p[0])).append("</td>")
              .append("<td>").append(esc(p[1])).append("</td>")
              .append("<td>").append(esc(p[2])).append("</td>")
              .append("<td>").append(esc(p[3])).append("</td>")
              .append("<td>").append(esc(p[4])).append("</td></tr>");
        }
        sb.append("</table><hr>");
        return sb.toString();
    }

    // TODO: if (config.mostrarTotales)
    private static String htmlTotales(TicketData d) {
        StringBuilder sb = new StringBuilder();
        if (d.descuento > 0) {
            sb.append("<b>Subtotal:</b> $").append(fmt(d.subtotal)).append("<br>");
            sb.append("<b>Descuento:</b> -$").append(fmt(d.descuento)).append("<br>");
        }
        sb.append("<b>Total:</b> $").append(fmt(d.total)).append("<br>");
        if (d.tipo == TipoTicket.VENTA) {
            sb.append("<b>Forma de pago:</b> ").append(esc(d.formaPago)).append("<br>");
            if (d.recibido > 0) {
                sb.append("<b>Recibido:</b> $").append(fmt(d.recibido)).append("<br>");
                sb.append("<b>Cambio:</b> $").append(fmt(d.cambio)).append("<br>");
            }
        } else {
            sb.append("<b>Pagado:</b> $").append(fmt(d.cantidadDada)).append("<br>");
            sb.append("<b>Restante:</b> $").append(fmt(d.cantidadFaltante)).append("<br>");
        }
        return sb.toString();
    }

    // TODO: if (config.mostrarPie)
    private static String htmlPie(TicketData d) {
        if (d.empresaMensaje.isEmpty()) return "";
        return "<hr><center><i>" + esc(d.empresaMensaje) + "</i></center>";
    }

    private static String esc(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private static String fmt(double v) {
        return String.format("%.2f", v);
    }
}
