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

    private static String nvl(String s) {
        return s == null ? "" : s;
    }
}
