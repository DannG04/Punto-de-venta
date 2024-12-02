import java.awt.Desktop;
import java.io.*;
import java.time.LocalDate;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JOptionPane;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class Excel {
    
    public static void balanceGeneral() {

        Workbook book = new XSSFWorkbook();
        Sheet sheet = book.createSheet("Reporte");
        LocalDate fecha = LocalDate.now();
        //Obtenemos la fecha actual
        String dia = String.valueOf(fecha.getDayOfMonth());
        String mes = String.valueOf(fecha.getMonthValue());
        String anio = String.valueOf(fecha.getYear());
        try {
            InputStream is = new FileInputStream("src/img/logo.png");
            byte[] bytes = IOUtils.toByteArray(is);
            int imgIndex = book.addPicture(bytes, Workbook.PICTURE_TYPE_PNG);
            is.close();

            CreationHelper help = book.getCreationHelper();
            Drawing draw = sheet.createDrawingPatriarch();

            ClientAnchor ancho = help.createClientAnchor();
            ancho.setCol1(0);
            ancho.setRow1(1);
            Picture pict = draw.createPicture(ancho, imgIndex);
            pict.resize(1, 3);
            sheet.addMergedRegion(new CellRangeAddress(1, 3, 0, 0));

            // Estilos de encabezados
            CellStyle tituloEstilo = book.createCellStyle();
            tituloEstilo.setAlignment(HorizontalAlignment.CENTER);
            tituloEstilo.setVerticalAlignment(VerticalAlignment.CENTER);
            tituloEstilo.setFillForegroundColor(IndexedColors.WHITE.getIndex());
            tituloEstilo.setFillBackgroundColor(IndexedColors.WHITE.getIndex());
            tituloEstilo = estilodeCelda(tituloEstilo);
            Font fuenteTitulo = book.createFont();
            fuenteTitulo.setFontName("Times new roman");
            fuenteTitulo.setBold(true);
            fuenteTitulo.setFontHeightInPoints((short) 14);
            tituloEstilo.setFont(fuenteTitulo);

            Row filaTitulo = sheet.createRow(1);
            Cell celdaTitulo = filaTitulo.createCell(1);
            celdaTitulo.setCellStyle(tituloEstilo);
            celdaTitulo.setCellValue("Balance General");

            sheet.addMergedRegion(new CellRangeAddress(1, 2, 1, 5));

            String[] cabecera = new String[]{"Activo", "1", "2", "Pasivo", "1", "2"};
            CellStyle Estilo = book.createCellStyle();
            Estilo.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            Estilo.setFillBackgroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            Estilo = estilodeCelda(Estilo);

            Font font = book.createFont();
            font.setFontName("Arial");
            font.setBold(true);
            font.setColor(IndexedColors.BLACK.getIndex());
            font.setFontHeightInPoints((short) 12);
            Estilo.setFont(font);

            Row filaEncabezados = sheet.createRow(4);
            for (int i = 0; i < cabecera.length; i++) {
                Cell celdaEnzabezado = filaEncabezados.createCell(i);
                celdaEnzabezado.setCellStyle(Estilo);
                celdaEnzabezado.setCellValue(cabecera[i]);
            }

            sheet.setColumnWidth(0, 3000);
            sheet.setColumnWidth(1, 3000);
            sheet.setColumnWidth(2, 3000);
            sheet.setColumnWidth(3, 3000);
            sheet.setColumnWidth(4, 3000);
            sheet.setColumnWidth(5, 3000);

            Row filaDatos = sheet.createRow(5);
            CellStyle datosEstilo = book.createCellStyle();
            datosEstilo.setFillForegroundColor(IndexedColors.WHITE.getIndex());
            datosEstilo.setFillBackgroundColor(IndexedColors.WHITE.getIndex());
            datosEstilo = estilodeCelda(datosEstilo);
            datosEstilo.setAlignment(HorizontalAlignment.LEFT);
            datosEstilo.setVerticalAlignment(VerticalAlignment.CENTER);
            Font fuenteDatos = book.createFont();
            fuenteDatos.setFontName("Arial");
            fuenteDatos.setBold(false);
            fuenteDatos.setFontHeightInPoints((short) 12);
            datosEstilo.setFont(fuenteDatos);
            String[] datos = new String[]{"Circulante", "", "", "Circulante", "", ""};
            for (int i = 0; i < datos.length; i++) {
                Cell celdaDatos = filaDatos.createCell(i);
                celdaDatos.setCellStyle(datosEstilo);
                celdaDatos.setCellValue(datos[i]);
            }

            CellStyle fechaEstilo = book.createCellStyle();
            fechaEstilo.setFillForegroundColor(IndexedColors.WHITE.getIndex());
            fechaEstilo.setFillBackgroundColor(IndexedColors.WHITE.getIndex());
            fechaEstilo = estilodeCelda(fechaEstilo);
            fechaEstilo.setAlignment(HorizontalAlignment.CENTER);
            fechaEstilo.setVerticalAlignment(VerticalAlignment.CENTER);
            Font fuenteFecha = book.createFont();
            fuenteFecha.setFontName("Arial");
            fuenteFecha.setFontHeightInPoints((short) 10);
            Row filaFecha = sheet.createRow(3);
            Cell celdaFecha = filaFecha.createCell(1);
            celdaFecha.setCellStyle(fechaEstilo);
            celdaTitulo.setCellValue("Punto de venta S.A.");
            sheet.addMergedRegion(new CellRangeAddress(3, 3, 1, 5));
            String mes2 = cambiarmes(mes);
            celdaFecha.setCellValue("Balance general del " + dia + " de " + mes2 + " del año " + anio);

            sheet.setZoom(150);
            String fileName = "balanceGeneral" + dia + "_" + mes + "_" + anio;
            File file = new File("BalancesGenerales" + "/" + fileName + ".xlsx");
            FileOutputStream fileOut = new FileOutputStream(file);
            book.write(fileOut);
            fileOut.close();
            Desktop.getDesktop().open(file);
            JOptionPane.showMessageDialog(null, "Reporte Generado");

        } catch (FileNotFoundException ex) {
            Logger.getLogger(Excel.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Excel.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public static void estadodeResultados() {
        Workbook book = new XSSFWorkbook();
        Sheet sheet = book.createSheet("Estado de resultados");
        LocalDate fecha = LocalDate.now();
        String dia = String.valueOf(fecha.getDayOfMonth());
        String mes = String.valueOf(fecha.getMonthValue());
        String anio = String.valueOf(fecha.getYear());
        try {
            InputStream is = new FileInputStream("src/img/logo.png");
            byte[] bytes = IOUtils.toByteArray(is);
            int imgIndex = book.addPicture(bytes, Workbook.PICTURE_TYPE_PNG);
            is.close();

            CreationHelper help = book.getCreationHelper();
            Drawing draw = sheet.createDrawingPatriarch();

            ClientAnchor ancho = help.createClientAnchor();
            ancho.setCol1(0);
            ancho.setRow1(1);
            Picture pict = draw.createPicture(ancho, imgIndex);
            pict.resize(1, 3);
            sheet.addMergedRegion(new CellRangeAddress(1, 3, 0, 0));


            // Estilos de encabezados
            CellStyle tituloEstilo = book.createCellStyle();
            tituloEstilo.setFillForegroundColor(IndexedColors.WHITE.getIndex());
            tituloEstilo.setFillBackgroundColor(IndexedColors.WHITE.getIndex());
            tituloEstilo = estilodeCelda(tituloEstilo);
            tituloEstilo.setAlignment(HorizontalAlignment.CENTER);
            tituloEstilo.setVerticalAlignment(VerticalAlignment.CENTER);
            Font fuenteTitulo = book.createFont();
            tituloEstilo.setFillForegroundColor(IndexedColors.WHITE.getIndex());
            tituloEstilo.setFillBackgroundColor(IndexedColors.WHITE.getIndex());
            tituloEstilo = estilodeCelda(tituloEstilo);
            fuenteTitulo.setFontName("Times new roman");
            fuenteTitulo.setBold(true);
            fuenteTitulo.setFontHeightInPoints((short) 14);
            tituloEstilo.setFont(fuenteTitulo);

            Row filaTitulo = sheet.createRow(1);
            Cell celdaTitulo = filaTitulo.createCell(1);
            
            celdaTitulo.setCellStyle(tituloEstilo);
            celdaTitulo.setCellValue("Estado de resultados");

            sheet.addMergedRegion(new CellRangeAddress(1, 2, 1, 5));

            CellStyle fechaEstilo = book.createCellStyle();
            fechaEstilo.setFillForegroundColor(IndexedColors.WHITE.getIndex());
            fechaEstilo.setFillBackgroundColor(IndexedColors.WHITE.getIndex());
            fechaEstilo = estilodeCelda(fechaEstilo);
            fechaEstilo.setAlignment(HorizontalAlignment.CENTER);
            fechaEstilo.setVerticalAlignment(VerticalAlignment.CENTER);
            Font fuenteFecha = book.createFont();
            fuenteFecha.setFontName("Arial");
            fuenteFecha.setFontHeightInPoints((short) 10);
            Row filaFecha = sheet.createRow(3);
            Cell celdaFecha = filaFecha.createCell(1);
            celdaFecha.setCellStyle(fechaEstilo);
            celdaTitulo.setCellValue("Punto de venta S.A.");
            sheet.addMergedRegion(new CellRangeAddress(3, 3, 1, 5));
            String mes2 = cambiarmes(mes);
            celdaFecha.setCellValue("Estado de resultados del " + dia + " de " + mes2 + " del año " + anio);
            String[] cabecera = new String[]{"1", "2", "3", "4"};
            sheet.addMergedRegion(new CellRangeAddress(4, 4, 0, 1));
            Row filaEncabezados = sheet.createRow(4);
            Cell celdaVacia = filaEncabezados.createCell(0);
            CellStyle encaEstilo = book.createCellStyle();
            encaEstilo.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            encaEstilo.setFillBackgroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            encaEstilo = estilodeCelda(encaEstilo);
            celdaVacia.setCellStyle(encaEstilo);
            for (int i = 0; i < cabecera.length; i++) {
                Cell celdaEnzabezado = filaEncabezados.createCell(i + 2);
                celdaEnzabezado.setCellStyle(encaEstilo);
                celdaEnzabezado.setCellValue(cabecera[i]);
            }

            sheet.setZoom(150);
            String fileName = "estadodeResultados" + dia + "_" + mes + "_" + anio;
            File file = new File("Estados de resultados/" + fileName + ".xlsx");
            FileOutputStream fileOut = new FileOutputStream(file);
            book.write(fileOut);
            fileOut.close();
            Desktop.getDesktop().open(file);
            JOptionPane.showMessageDialog(null, "Reporte Generado");

        } catch (FileNotFoundException ex) {
            Logger.getLogger(Excel.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Excel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void reporteDiario() {
        Workbook book = new XSSFWorkbook();
        Sheet sheet = book.createSheet("Reporte Diario");
        LocalDate fecha = LocalDate.now();
        String dia = String.valueOf(fecha.getDayOfMonth());
        String mes = String.valueOf(fecha.getMonthValue());
        String anio = String.valueOf(fecha.getYear());
        try {
            InputStream is = new FileInputStream("src/img/logo.png");
            byte[] bytes = IOUtils.toByteArray(is);
            int imgIndex = book.addPicture(bytes, Workbook.PICTURE_TYPE_PNG);
            is.close();

            CreationHelper help = book.getCreationHelper();
            Drawing draw = sheet.createDrawingPatriarch();

            ClientAnchor ancho = help.createClientAnchor();
            ancho.setCol1(0);
            ancho.setRow1(1);
            Picture pict = draw.createPicture(ancho, imgIndex);
            pict.resize(1, 3);
            sheet.addMergedRegion(new CellRangeAddress(1, 3, 0, 0));


            // Estilos de encabezados
            CellStyle tituloEstilo = book.createCellStyle();
            tituloEstilo.setAlignment(HorizontalAlignment.CENTER);
            tituloEstilo.setVerticalAlignment(VerticalAlignment.CENTER);
            Font fuenteTitulo = book.createFont();
            tituloEstilo.setFillForegroundColor(IndexedColors.WHITE.getIndex());
            tituloEstilo.setFillBackgroundColor(IndexedColors.WHITE.getIndex());
            tituloEstilo = estilodeCelda(tituloEstilo);
            fuenteTitulo.setFontName("Times new roman");
            fuenteTitulo.setBold(true);
            fuenteTitulo.setFontHeightInPoints((short) 14);
            tituloEstilo.setFont(fuenteTitulo);

            Row filaTitulo = sheet.createRow(1);
            Cell celdaTitulo = filaTitulo.createCell(1);
            celdaTitulo.setCellStyle(tituloEstilo);
            celdaTitulo.setCellValue("Reporte de ventas");

            sheet.addMergedRegion(new CellRangeAddress(1, 2, 1, 5));

            CellStyle fechaEstilo = book.createCellStyle();
            fechaEstilo.setFillForegroundColor(IndexedColors.WHITE.getIndex());
            fechaEstilo.setFillBackgroundColor(IndexedColors.WHITE.getIndex());
            fechaEstilo = estilodeCelda(fechaEstilo);
            fechaEstilo.setAlignment(HorizontalAlignment.CENTER);
            fechaEstilo.setVerticalAlignment(VerticalAlignment.CENTER);
            Font fuenteFecha = book.createFont();
            fuenteFecha.setFontName("Arial");
            fuenteFecha.setFontHeightInPoints((short) 10);
            Row filaFecha = sheet.createRow(3);
            Cell celdaFecha = filaFecha.createCell(1);
            celdaFecha.setCellStyle(fechaEstilo);
            celdaTitulo.setCellValue("Punto de venta S.A.");
            sheet.addMergedRegion(new CellRangeAddress(3, 3, 1, 5));
            String mes2 = cambiarmes(mes);
            celdaFecha.setCellValue("Reporte de venta del " + dia + " de " + mes2 + " del año " + anio);
            // Encabezados
            sheet.addMergedRegion(new CellRangeAddress(4, 4, 0, 1));
            sheet.addMergedRegion(new CellRangeAddress(4, 4, 2, 3));
            sheet.addMergedRegion(new CellRangeAddress(4, 4, 4, 5));
            Row filaEncabezados = sheet.createRow(4);
            String[] encabezados = new String[]{"Producto", "Cantidad", "Total"};
            CellStyle encaEstilo = book.createCellStyle();
            encaEstilo.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            encaEstilo.setFillBackgroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            encaEstilo = estilodeCelda(encaEstilo);
            for (int i = 0; i < encabezados.length; i++) {
                if(i == 0){
                    Cell celdaVacia = filaEncabezados.createCell(0);
                    celdaVacia.setCellValue(encabezados[i]);
                    celdaVacia.setCellStyle(encaEstilo);
                }
                if(i == 1){
                    Cell celdaVacia = filaEncabezados.createCell(2);
                    celdaVacia.setCellValue(encabezados[i]);
                    celdaVacia.setCellStyle(encaEstilo);
                }
                if(i == 2){
                    Cell celdaVacia = filaEncabezados.createCell(4);
                    celdaVacia.setCellValue(encabezados[i]);
                    celdaVacia.setCellStyle(encaEstilo);
                }
                
            }

            sheet.setZoom(150);
            String fileName = "Ventas" + dia + "_" + mes + "_" + anio;
            File file = new File("Reportes de ventas/" + fileName + ".xlsx");
            FileOutputStream fileOut = new FileOutputStream(file);
            book.write(fileOut);
            fileOut.close();
            Desktop.getDesktop().open(file);
            JOptionPane.showMessageDialog(null, "Reporte Generado");
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Excel.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Excel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static String cambiarmes(String mes) {
        switch (mes) {
            case "1":
                mes = "Enero";
                break;
            case "2":
                mes = "Febrero";
                break;
            case "3":
                mes = "Marzo";
                break;
            case "4":
                mes = "Abril";
                break;
            case "5":
                mes = "Mayo";
                break;
            case "6":
                mes = "Junio";
                break;
            case "7":
                mes = "Julio";
                break;
            case "8":
                mes = "Agosto";
                break;
            case "9":
                mes = "Septiembre";
                break;
            case "10":
                mes = "Octubre";
                break;
            case "11":
                mes = "Noviembre";
                break;
            case "12":
                mes = "Diciembre";
                break;
            default:
                break;
        }
        return mes;
    }

    public static CellStyle estilodeCelda(CellStyle Estilo) {
        // Estilos de encabezados
        Estilo.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        Estilo.setBorderBottom(BorderStyle.THIN);
        Estilo.setBorderLeft(BorderStyle.THIN);
        Estilo.setBorderRight(BorderStyle.THIN);
        Estilo.setBorderBottom(BorderStyle.THIN);
        Estilo.setAlignment(HorizontalAlignment.CENTER);
        Estilo.setBorderTop(BorderStyle.THIN);
        return Estilo;
    }
}
