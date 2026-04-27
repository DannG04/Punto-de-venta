import br.com.adilson.util.Extenso;
import br.com.adilson.util.PrinterMatrix;
import java.io.FileInputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.sql.ResultSet;
import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.SimpleDoc;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.swing.JOptionPane;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author origi
 */
public class GenTicket {
    static ConexionBD con = new ConexionBD();

    public static void generarTicketApartado(ResultSet resultado) {
        try {
            String nomEmpresa = "", telEmpresa = "", dirEmpresa = "", msgTicket = "Muchas gracias por su compra.";
            try {
                ResultSet emp = con.obtenerEmpresa();
                if (emp != null && emp.next()) {
                    nomEmpresa = nvl(emp.getString("nombre"));
                    telEmpresa = nvl(emp.getString("telefono"));
                    String dir = nvl(emp.getString("direccion"));
                    String ciudad = nvl(emp.getString("ciudad"));
                    dirEmpresa = dir.isEmpty() ? ciudad : (ciudad.isEmpty() ? dir : dir + ", " + ciudad);
                    String msg = nvl(emp.getString("mensaje_ticket"));
                    if (!msg.isEmpty()) msgTicket = msg;
                }
            } catch (Exception ignored) {}

            if (resultado.next()) {
                String idApartado = resultado.getString("id_apartado");
                String idEmpleado = resultado.getString("id_empleado");
                String fechaInicio = resultado.getString("fecha_inicio");
                String fechaLimite = resultado.getString("fecha_limite");
                double cantidadDada = resultado.getDouble("cantidad_dada");
                double cantidadFaltante = resultado.getDouble("cantidad_faltante");
                double cantidadTotal = resultado.getDouble("cantidad_total");
                String nombre = "";
                ResultSet emp = con.seleccionarVendedor(idEmpleado);
                while (emp.next()) {
                    nombre = emp.getString("nombre");
                    System.out.println("Nombre: " + emp.getString("nombre"));
                }

                DateFormat formatoHora = new SimpleDateFormat("HH:mm:ss");
                Date date = new Date();
                Date fechahoy = new Date();
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                String fechaActual = sdf.format(fechahoy);
                PrinterMatrix printer = new PrinterMatrix();
                Extenso e = new Extenso();
                e.setNumber(cantidadTotal);

                int totalRows = 18; // Ajusta según el contenido del ticket

                printer.setOutSize(totalRows, 52); // Ajusta el ancho según sea necesario

                int row = 1;
                printer.printCharAtCol(row, row, 44, "*");
                if (!nomEmpresa.isEmpty()) {
                    row++;
                    printer.printTextWrap(row, row + 1, 10, 52, nomEmpresa);
                }
                if (!telEmpresa.isEmpty()) {
                    row++;
                    printer.printTextWrap(row, row + 1, 3, 52, "Tel: " + telEmpresa);
                }
                if (!dirEmpresa.isEmpty()) {
                    row++;
                    printer.printTextWrap(row, row + 1, 3, 52, dirEmpresa);
                }
                row++;
                printer.printCharAtCol(row, row, 44, "*");
                row++;
                printer.printTextWrap(row, row + 1, 10, 52, "COMPROBANTE DE APARTADO");
                row++;
                printer.printTextWrap(row, row + 1, 1, 52, "Folio: " + idApartado);
                row++;
                printer.printTextWrap(row, row + 1, 1, 52, "Fecha de emisión : " + fechaActual);
                row++;
                printer.printTextWrap(row, row + 1, 1, 52, "Hora: " + formatoHora.format(date));
                row++;
                printer.printTextWrap(row, row + 1, 1, 52, "Vendedor: " + nombre);
                row++;
                printer.printTextWrap(row, row + 1, 1, 52, "-------------------------------------------");
                row++;
                printer.printTextWrap(row, row + 1, 1, 52, "Fecha Inicio: " + fechaInicio);
                row++;
                printer.printTextWrap(row, row + 1, 1, 52, "Fecha Límite de pago: " + fechaLimite);
                row++;
                printer.printTextWrap(row, row + 1, 1, 52, "Cantidad Dada: " + cantidadDada);
                row++;
                printer.printTextWrap(row, row + 1, 1, 52, "Cantidad Faltante: " + cantidadFaltante);
                row++;
                printer.printTextWrap(row, row + 1, 1, 52, "Cantidad Total: " + cantidadTotal);
                row++;
                printer.printTextWrap(row, row + 1, 1, 52, "-------" + msgTicket + "-------");

                printer.toFile("impresionApartado.txt");

                FileInputStream inputStream = null;
                try {
                    inputStream = new FileInputStream("impresionApartado.txt");
                    Mise.JOption("Ticket generado", "Ticket", JOptionPane.PLAIN_MESSAGE);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                if (inputStream == null) {
                    return;
                }

                DocFlavor docFormat = DocFlavor.INPUT_STREAM.AUTOSENSE;
                Doc document = new SimpleDoc(inputStream, docFormat, null);
                PrintRequestAttributeSet attributeSet = new HashPrintRequestAttributeSet();
                PrintService defaultPrintService = PrintServiceLookup.lookupDefaultPrintService();

                if (defaultPrintService != null) {
                    DocPrintJob printJob = defaultPrintService.createPrintJob();
                    try {
                        printJob.print(document, attributeSet);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                } else {
                    System.out.println("No hay una impresora instalada");
                }
            } else {
                System.out.println("No se encontraron resultados en el ResultSet.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void generarTicketProductos(ResultSet resultado) {
        try {
            String nomEmpresa = "", telEmpresa = "", dirEmpresa = "", msgTicket = "Muchas gracias por su compra.";
            try {
                ResultSet emp = con.obtenerEmpresa();
                if (emp != null && emp.next()) {
                    nomEmpresa = nvl(emp.getString("nombre"));
                    telEmpresa = nvl(emp.getString("telefono"));
                    String dir = nvl(emp.getString("direccion"));
                    String ciudad = nvl(emp.getString("ciudad"));
                    dirEmpresa = dir.isEmpty() ? ciudad : (ciudad.isEmpty() ? dir : dir + ", " + ciudad);
                    String msg = nvl(emp.getString("mensaje_ticket"));
                    if (!msg.isEmpty()) msgTicket = msg;
                }
            } catch (Exception ignored) {}

            DateFormat formatoHora = new SimpleDateFormat("HH:mm:ss");
            Date date = new Date();
            Date fechahoy = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            String fechaActual = sdf.format(fechahoy);
            PrinterMatrix printer = new PrinterMatrix();

            // Contar el número de filas en el ResultSet
            resultado.last();
            int rows = resultado.getRow();
            resultado.beforeFirst();

            int totalRows = rows + 16; // Ajusta según el contenido del ticket

            printer.setOutSize(totalRows, 55); // Ajusta el ancho según sea necesario

            int rowIndex = 1;
            printer.printCharAtCol(rowIndex, rowIndex, 44, "*");
            if (!nomEmpresa.isEmpty()) {
                rowIndex++;
                printer.printTextWrap(rowIndex, rowIndex + 1, 10, 55, nomEmpresa);
            }
            if (!telEmpresa.isEmpty()) {
                rowIndex++;
                printer.printTextWrap(rowIndex, rowIndex + 1, 3, 55, "Tel: " + telEmpresa);
            }
            if (!dirEmpresa.isEmpty()) {
                rowIndex++;
                printer.printTextWrap(rowIndex, rowIndex + 1, 3, 55, dirEmpresa);
            }
            rowIndex++;
            printer.printCharAtCol(rowIndex, rowIndex, 44, "*");
            rowIndex++;
            printer.printTextWrap(rowIndex, rowIndex + 1, 10, 55, "FACTURA DE ENTREGA");
            rowIndex++;
            printer.printTextWrap(rowIndex, rowIndex + 1, 1, 55, "Fecha de emisión : " + fechaActual);
            rowIndex++;
            printer.printTextWrap(rowIndex, rowIndex + 1, 1, 55, "Hora: " + formatoHora.format(date));
            rowIndex++;
            printer.printTextWrap(rowIndex, rowIndex + 1, 1, 55, "-------------------------------------------");
            rowIndex++;
            printer.printTextWrap(rowIndex, rowIndex + 1, 1, 55, "     Producto         Cant  P.Unit  P.Total");
            rowIndex++;
            double total = 0;
            while (resultado.next()) {
                String nombre = resultado.getString("nombre");
                int cantidad = resultado.getInt("cantidad");
                double precioUnitario = resultado.getDouble("precio_unitario");
                double precioTotal = resultado.getDouble("precio_total");
                total += precioTotal;

                printer.printTextWrap(rowIndex, rowIndex + 1, 1, 22, nombre);
                printer.printTextWrap(rowIndex, rowIndex + 1, 24, 26, String.valueOf(cantidad));
                printer.printTextWrap(rowIndex, rowIndex + 1, 30, 33, String.valueOf(precioUnitario));
                printer.printTextWrap(rowIndex, rowIndex + 1, 38, 43, String.valueOf(precioTotal));
                rowIndex++;
            }

            printer.printTextWrap(rowIndex, rowIndex + 1, 1, 55, "-------------------------------------------");
            printer.printTextWrap(rowIndex + 1, rowIndex + 2, 1, 55, "Total: " + total);
            printer.printTextWrap(rowIndex + 2, rowIndex + 3, 1, 55, "-------" + msgTicket + "-------");

            printer.toFile("impresionPagado.txt");

            FileInputStream inputStream = null;
            try {
                inputStream = new FileInputStream("impresionPagado.txt");
                Mise.JOption("Ticket generado", "Ticket", JOptionPane.PLAIN_MESSAGE);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            if (inputStream == null) {
                return;
            }

            DocFlavor docFormat = DocFlavor.INPUT_STREAM.AUTOSENSE;
            Doc document = new SimpleDoc(inputStream, docFormat, null);
            PrintRequestAttributeSet attributeSet = new HashPrintRequestAttributeSet();
            PrintService defaultPrintService = PrintServiceLookup.lookupDefaultPrintService();

            if (defaultPrintService != null) {
                DocPrintJob printJob = defaultPrintService.createPrintJob();
                try {
                    printJob.print(document, attributeSet);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else {
                System.out.println("No hay una impresora instalada");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void generarTicketCotizacion(String idCotizacion) {
        try {
            String nomEmpresa = "", telEmpresa = "", dirEmpresa = "", msgTicket = "Cotización sin compromiso de precio.";
            try {
                ResultSet emp = con.obtenerEmpresa();
                if (emp != null && emp.next()) {
                    nomEmpresa = nvl(emp.getString("nombre"));
                    telEmpresa = nvl(emp.getString("telefono"));
                    String dir = nvl(emp.getString("direccion"));
                    String ciudad = nvl(emp.getString("ciudad"));
                    dirEmpresa = dir.isEmpty() ? ciudad : (ciudad.isEmpty() ? dir : dir + ", " + ciudad);
                    String msg = nvl(emp.getString("mensaje_ticket"));
                    if (!msg.isEmpty()) msgTicket = msg;
                }
            } catch (Exception ignored) {}

            ResultSet header = con.obtenerCotizacionHeader(idCotizacion);
            if (header == null || !header.next()) {
                System.out.println("No se encontró la cotización: " + idCotizacion);
                return;
            }
            String fecha    = nvl(header.getString("fecha"));
            String cliente  = nvl(header.getString("cliente"));
            String total    = String.format("%.2f", header.getDouble("total_cotizacion"));
            String estatus  = nvl(header.getString("estatus"));
            String vigencia = nvl(header.getString("vigencia"));
            String notas    = nvl(header.getString("notas"));

            ResultSet detalle = con.obtenerDetalleCotizacion(idCotizacion);
            if (detalle == null) return;
            detalle.last();
            int rows = detalle.getRow();
            detalle.beforeFirst();

            int totalRows = rows + 22;
            PrinterMatrix printer = new PrinterMatrix();
            printer.setOutSize(totalRows, 55);

            int r = 1;
            printer.printCharAtCol(r, r, 44, "*");
            if (!nomEmpresa.isEmpty()) {
                r++; printer.printTextWrap(r, r + 1, 10, 55, nomEmpresa);
            }
            if (!telEmpresa.isEmpty()) {
                r++; printer.printTextWrap(r, r + 1, 3, 55, "Tel: " + telEmpresa);
            }
            if (!dirEmpresa.isEmpty()) {
                r++; printer.printTextWrap(r, r + 1, 3, 55, dirEmpresa);
            }
            r++; printer.printCharAtCol(r, r, 44, "*");
            r++; printer.printTextWrap(r, r + 1, 13, 55, "COTIZACION");
            r++; printer.printTextWrap(r, r + 1, 1, 55, "Folio: " + idCotizacion);
            r++; printer.printTextWrap(r, r + 1, 1, 55, "Fecha: " + fecha);
            r++; printer.printTextWrap(r, r + 1, 1, 55, "Cliente: " + cliente);
            r++; printer.printTextWrap(r, r + 1, 1, 55, "Estatus: " + estatus);
            if (!vigencia.isEmpty() && !"—".equals(vigencia)) {
                r++; printer.printTextWrap(r, r + 1, 1, 55, "Valida hasta: " + vigencia);
            }
            r++; printer.printTextWrap(r, r + 1, 1, 55, "-------------------------------------------");
            r++; printer.printTextWrap(r, r + 1, 1, 55, "     Producto         Cant  P.Unit  P.Total");
            r++;
            double subtotal = 0;
            while (detalle.next()) {
                String nombre = nvl(detalle.getString("nombre_p"));
                int cant      = detalle.getInt("cantidad");
                double pUnit  = detalle.getDouble("precio_dado");
                double pTotal = detalle.getDouble("precio_total");
                subtotal += pTotal;
                printer.printTextWrap(r, r + 1, 1, 22, nombre);
                printer.printTextWrap(r, r + 1, 24, 26, String.valueOf(cant));
                printer.printTextWrap(r, r + 1, 30, 33, String.format("%.2f", pUnit));
                printer.printTextWrap(r, r + 1, 38, 44, String.format("%.2f", pTotal));
                r++;
            }
            printer.printTextWrap(r, r + 1, 1, 55, "-------------------------------------------");
            printer.printTextWrap(r + 1, r + 2, 1, 55, "Total: $" + total);
            if (!notas.isEmpty()) {
                printer.printTextWrap(r + 2, r + 3, 1, 55, "Notas: " + notas);
                r++;
            }
            printer.printTextWrap(r + 2, r + 3, 1, 55, "---" + msgTicket + "---");

            printer.toFile("impresionCotizacion.txt");

            java.io.FileInputStream inputStream = null;
            try {
                inputStream = new java.io.FileInputStream("impresionCotizacion.txt");
                javax.swing.JOptionPane.showMessageDialog(null, "Ticket de cotización generado", "Ticket", javax.swing.JOptionPane.PLAIN_MESSAGE);
            } catch (Exception ex) { ex.printStackTrace(); }
            if (inputStream == null) return;

            javax.print.DocFlavor docFormat = javax.print.DocFlavor.INPUT_STREAM.AUTOSENSE;
            javax.print.Doc document = new javax.print.SimpleDoc(inputStream, docFormat, null);
            javax.print.attribute.PrintRequestAttributeSet attrs = new javax.print.attribute.HashPrintRequestAttributeSet();
            javax.print.PrintService ps = javax.print.PrintServiceLookup.lookupDefaultPrintService();
            if (ps != null) {
                try { ps.createPrintJob().print(document, attrs); } catch (Exception ex) { ex.printStackTrace(); }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String nvl(String s) {
        return s == null ? "" : s;
    }
}
