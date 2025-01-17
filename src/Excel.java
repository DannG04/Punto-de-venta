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
    static ConexionBD conect = new ConexionBD();
    
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

    public static void estadodeResultados(Double inv_ini) {
         ConexionBD conexion = new ConexionBD();

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
            tituloEstilo.setBorderRight(BorderStyle.THIN);
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
            CellStyle celdasConOperacion = book.createCellStyle();
            celdasConOperacion.setFillForegroundColor(IndexedColors.WHITE.getIndex());
            celdasConOperacion.setFillBackgroundColor(IndexedColors.WHITE.getIndex());
            celdasConOperacion = estilodeCeldaOperacion(celdasConOperacion);
            celdasConOperacion.setAlignment(HorizontalAlignment.LEFT);
            celdasConOperacion.setVerticalAlignment(VerticalAlignment.CENTER);
            celdasConOperacion.setBorderBottom(BorderStyle.MEDIUM);
            
            //Datos y cuentas
            Row Ventas = sheet.createRow(5);
            Cell celdaVentas = Ventas.createCell(0);
            CellStyle ventasEstilo = book.createCellStyle();
            sheet.addMergedRegion(new CellRangeAddress(5, 5, 0, 1));
            ventasEstilo.setFillBackgroundColor(IndexedColors.WHITE.getIndex());
            ventasEstilo.setFillForegroundColor(IndexedColors.WHITE.getIndex());
            ventasEstilo=estilodeCelda(ventasEstilo);
            ventasEstilo.setAlignment(HorizontalAlignment.LEFT);
            celdaVentas.setCellStyle(ventasEstilo);
            celdaVentas.setCellValue("Ventas");
            Cell celdaVentas2 = Ventas.createCell(2);
            celdaVentas2.setCellStyle(ventasEstilo);
            Cell celdaVentas3 = Ventas.createCell(3);
            celdaVentas3.setCellStyle(ventasEstilo);
            Cell celdaVentas4 = Ventas.createCell(4);
            celdaVentas4.setCellStyle(ventasEstilo);
            Cell celdaVentas5 = Ventas.createCell(5);
            celdaVentas5.setCellStyle(ventasEstilo);
            celdaVentas5.setCellValue(conexion.ObtenerDato("total_ventas"));
            Row DevVentas = sheet.createRow(6);
            Cell celdaDevVentas = DevVentas.createCell(0);
            CellStyle devVentasEstilo = book.createCellStyle();
            sheet.addMergedRegion(new CellRangeAddress(6, 6, 0, 1));
            devVentasEstilo.setFillBackgroundColor(IndexedColors.WHITE.getIndex());
            devVentasEstilo.setFillForegroundColor(IndexedColors.WHITE.getIndex());
            devVentasEstilo=estilodeCelda(devVentasEstilo);
            devVentasEstilo.setAlignment(HorizontalAlignment.LEFT);
            celdaDevVentas.setCellStyle(devVentasEstilo);
            celdaDevVentas.setCellValue("(-)Dev. Ventas");
            Cell celdaDevVentas2 = DevVentas.createCell(2);
            celdaDevVentas2.setCellStyle(devVentasEstilo);
            Cell celdaDevVentas3 = DevVentas.createCell(3);
            celdaDevVentas3.setCellStyle(devVentasEstilo);
            Cell celdaDevVentas4 = DevVentas.createCell(4);
            celdaDevVentas4.setCellStyle(devVentasEstilo);
            Cell celdaDevVentas5 = DevVentas.createCell(5);
            celdaDevVentas5.setCellStyle(devVentasEstilo);
            celdaDevVentas4.setCellValue(conexion.ObtenerDato("total_devoluciones"));
            Row DescVentas = sheet.createRow(7);
            Cell celdaDescVentas = DescVentas.createCell(0);
            CellStyle descVentasEstilo = book.createCellStyle();
            sheet.addMergedRegion(new CellRangeAddress(7, 7, 0, 1));
            descVentasEstilo.setFillBackgroundColor(IndexedColors.WHITE.getIndex());
            descVentasEstilo.setFillForegroundColor(IndexedColors.WHITE.getIndex());
            descVentasEstilo=estilodeCelda(descVentasEstilo);
            descVentasEstilo.setAlignment(HorizontalAlignment.LEFT);
            celdaDescVentas.setCellStyle(descVentasEstilo);
            celdaDescVentas.setCellValue("(-)Desc. Ventas");
            Cell celdaDescVentas2 = DescVentas.createCell(2);
            celdaDescVentas2.setCellStyle(descVentasEstilo);
            Cell celdaDescVentas3 = DescVentas.createCell(3);
            celdaDescVentas3.setCellStyle(descVentasEstilo);
            Cell celdaDescVentas4 = DescVentas.createCell(4);
            celdaDescVentas4.setCellStyle(celdasConOperacion);
            Cell celdaDescVentas5 = DescVentas.createCell(5);
            celdaDescVentas5.setCellStyle(celdasConOperacion);
            celdaDescVentas4.setCellValue(0);
            Row VentasNetas = sheet.createRow(8);
            Cell celdaVentasNetas = VentasNetas.createCell(0);
            CellStyle ventasNetasEstilo = book.createCellStyle();
            sheet.addMergedRegion(new CellRangeAddress(8, 8, 0, 1));
            ventasNetasEstilo.setFillBackgroundColor(IndexedColors.WHITE.getIndex());
            ventasNetasEstilo.setFillForegroundColor(IndexedColors.WHITE.getIndex());
            ventasNetasEstilo=estilodeCelda(ventasNetasEstilo);
            ventasNetasEstilo.setAlignment(HorizontalAlignment.LEFT);
            celdaVentasNetas.setCellStyle(ventasNetasEstilo);
            celdaVentasNetas.setCellValue("Ventas Netas");
            Cell celdaVentasNetas2 = VentasNetas.createCell(2);
            celdaVentasNetas2.setCellStyle(ventasNetasEstilo);
            Cell celdaVentasNetas3 = VentasNetas.createCell(3);
            celdaVentasNetas3.setCellStyle(ventasNetasEstilo);
            Cell celdaVentasNetas4 = VentasNetas.createCell(4);
            celdaVentasNetas4.setCellStyle(ventasNetasEstilo);
            Cell celdaVentasNetas5 = VentasNetas.createCell(5);
            celdaVentasNetas5.setCellStyle(ventasNetasEstilo);
            celdaVentasNetas5.setCellFormula("F6-F7-F8");
            Row CostoVentas = sheet.createRow(9);
            Cell celdaCostoVentas = CostoVentas.createCell(0);
            CellStyle costoVentasEstilo = book.createCellStyle();
            sheet.addMergedRegion(new CellRangeAddress(9, 9, 0, 1));
            costoVentasEstilo.setFillBackgroundColor(IndexedColors.WHITE.getIndex());
            costoVentasEstilo.setFillForegroundColor(IndexedColors.WHITE.getIndex());
            costoVentasEstilo=estilodeCelda(costoVentasEstilo);
            costoVentasEstilo.setAlignment(HorizontalAlignment.LEFT);
            celdaCostoVentas.setCellStyle(costoVentasEstilo);
            celdaCostoVentas.setCellValue("Costo de Ventas");
            Cell celdaCostoVentas2 = CostoVentas.createCell(2);
            celdaCostoVentas2.setCellStyle(costoVentasEstilo);
            Cell celdaCostoVentas3 = CostoVentas.createCell(3);
            celdaCostoVentas3.setCellStyle(costoVentasEstilo);
            Cell celdaCostoVentas4 = CostoVentas.createCell(4);
            celdaCostoVentas4.setCellStyle(costoVentasEstilo);
            Cell celdaCostoVentas5 = CostoVentas.createCell(5);
            celdaCostoVentas5.setCellStyle(costoVentasEstilo);
            Row InventarioInicial = sheet.createRow(10);
            Cell celdaInventarioInicial = InventarioInicial.createCell(0);
            CellStyle inventarioInicialEstilo = book.createCellStyle();
            sheet.addMergedRegion(new CellRangeAddress(10, 10, 0, 1));
            inventarioInicialEstilo.setFillBackgroundColor(IndexedColors.WHITE.getIndex());
            inventarioInicialEstilo.setFillForegroundColor(IndexedColors.WHITE.getIndex());
            inventarioInicialEstilo=estilodeCelda(inventarioInicialEstilo);
            inventarioInicialEstilo.setAlignment(HorizontalAlignment.LEFT);
            celdaInventarioInicial.setCellStyle(inventarioInicialEstilo);
            celdaInventarioInicial.setCellValue("(+)Inventario Inicial");
            Cell celdaInventarioInicial2 = InventarioInicial.createCell(2);
            celdaInventarioInicial2.setCellStyle(inventarioInicialEstilo);
            Cell celdaInventarioInicial3 = InventarioInicial.createCell(3);
            celdaInventarioInicial3.setCellStyle(inventarioInicialEstilo);
            Cell celdaInventarioInicial4 = InventarioInicial.createCell(4);
            celdaInventarioInicial4.setCellStyle(inventarioInicialEstilo);
            celdaInventarioInicial4.setCellValue(inv_ini);
            Cell celdaInventarioInicial5 = InventarioInicial.createCell(5);
            celdaInventarioInicial5.setCellStyle(inventarioInicialEstilo);
            Row ComprasNetas = sheet.createRow(11);
            Cell celdaComprasNetas = ComprasNetas.createCell(0);
            CellStyle comprasNetasEstilo = book.createCellStyle();
            sheet.addMergedRegion(new CellRangeAddress(11, 11, 0, 1));
            comprasNetasEstilo.setFillBackgroundColor(IndexedColors.WHITE.getIndex());
            comprasNetasEstilo.setFillForegroundColor(IndexedColors.WHITE.getIndex());
            comprasNetasEstilo=estilodeCelda(comprasNetasEstilo);
            comprasNetasEstilo.setAlignment(HorizontalAlignment.LEFT);
            celdaComprasNetas.setCellStyle(comprasNetasEstilo);
            celdaComprasNetas.setCellValue("Compras Netas");
            Cell celdaComprasNetas2 = ComprasNetas.createCell(2);
            celdaComprasNetas2.setCellStyle(comprasNetasEstilo);
            Cell celdaComprasNetas3 = ComprasNetas.createCell(3);
            celdaComprasNetas3.setCellStyle(comprasNetasEstilo);
            Cell celdaComprasNetas4 = ComprasNetas.createCell(4);
            celdaComprasNetas4.setCellStyle(celdasConOperacion);
            celdaComprasNetas4.setCellValue(conexion.ObtenerDato("total_compras"));
            Cell celdaComprasNetas5 = ComprasNetas.createCell(5);
            celdaComprasNetas5.setCellStyle(comprasNetasEstilo);
            Row TotalMercancias = sheet.createRow(12);
            Cell celdaTotalMercancias = TotalMercancias.createCell(0);
            CellStyle totalMercanciasEstilo = book.createCellStyle();
            sheet.addMergedRegion(new CellRangeAddress(12, 12, 0, 1));
            totalMercanciasEstilo.setFillBackgroundColor(IndexedColors.WHITE.getIndex());
            totalMercanciasEstilo.setFillForegroundColor(IndexedColors.WHITE.getIndex());
            totalMercanciasEstilo=estilodeCelda(totalMercanciasEstilo);
            totalMercanciasEstilo.setAlignment(HorizontalAlignment.LEFT);
            celdaTotalMercancias.setCellStyle(totalMercanciasEstilo);
            celdaTotalMercancias.setCellValue("Total de Mercancias");
            Cell celdaTotalMercancias2 = TotalMercancias.createCell(2);
            celdaTotalMercancias2.setCellStyle(totalMercanciasEstilo);
            Cell celdaTotalMercancias3 = TotalMercancias.createCell(3);
            celdaTotalMercancias3.setCellStyle(totalMercanciasEstilo);
            Cell celdaTotalMercancias4 = TotalMercancias.createCell(4);
            celdaTotalMercancias4.setCellStyle(totalMercanciasEstilo);
            celdaTotalMercancias4.setCellFormula("E11+E12");
            Cell celdaTotalMercancia5 = TotalMercancias.createCell(5);
            celdaTotalMercancia5.setCellStyle(totalMercanciasEstilo);
            Row InventarioFinal = sheet.createRow(13);
            Cell celdaInventarioFinal = InventarioFinal.createCell(0);
            CellStyle inventarioFinalEstilo = book.createCellStyle();
            sheet.addMergedRegion(new CellRangeAddress(13, 13, 0, 1));
            inventarioFinalEstilo.setFillBackgroundColor(IndexedColors.WHITE.getIndex());
            inventarioFinalEstilo.setFillForegroundColor(IndexedColors.WHITE.getIndex());
            inventarioFinalEstilo=estilodeCelda(inventarioFinalEstilo);
            inventarioFinalEstilo.setAlignment(HorizontalAlignment.LEFT);
            celdaInventarioFinal.setCellStyle(inventarioFinalEstilo);
            celdaInventarioFinal.setCellValue("(-)Inventario Final");
            Cell celdaInventarioFinal2 = InventarioFinal.createCell(2);
            celdaInventarioFinal2.setCellStyle(inventarioFinalEstilo);
            Cell celdaInventarioFinal3 = InventarioFinal.createCell(3);
            celdaInventarioFinal3.setCellStyle(inventarioFinalEstilo);
            Cell celdaInventarioFinal4 = InventarioFinal.createCell(4);
            celdaInventarioFinal4.setCellStyle(celdasConOperacion);
            celdaInventarioFinal4.setCellValue(conexion.ObtenerDato("inventario_final"));
            Cell celdaInventarioFinal5 = InventarioFinal.createCell(5);
            celdaInventarioFinal5.setCellStyle(inventarioFinalEstilo);
            Row CostoMercanciasVendidas = sheet.createRow(14);
            Cell celdaCostoMercanciasVendidas = CostoMercanciasVendidas.createCell(0);
            CellStyle costoMercanciasVendidasEstilo = book.createCellStyle();
            sheet.addMergedRegion(new CellRangeAddress(14, 14, 0, 1));
            costoMercanciasVendidasEstilo.setFillBackgroundColor(IndexedColors.WHITE.getIndex());
            costoMercanciasVendidasEstilo.setFillForegroundColor(IndexedColors.WHITE.getIndex());
            costoMercanciasVendidasEstilo=estilodeCelda(costoMercanciasVendidasEstilo);
            costoMercanciasVendidasEstilo.setAlignment(HorizontalAlignment.LEFT);
            celdaCostoMercanciasVendidas.setCellStyle(costoMercanciasVendidasEstilo);
            celdaCostoMercanciasVendidas.setCellValue("Costo de lo vendido");
            Cell celdaCostoMercanciasVendidas2 = CostoMercanciasVendidas.createCell(2);
            celdaCostoMercanciasVendidas2.setCellStyle(costoMercanciasVendidasEstilo);
            Cell celdaCostoMercanciasVendidas3 = CostoMercanciasVendidas.createCell(3);
            celdaCostoMercanciasVendidas3.setCellStyle(costoMercanciasVendidasEstilo);
            Cell celdaCostoMercanciasVendidas4 = CostoMercanciasVendidas.createCell(4);
            celdaCostoMercanciasVendidas4.setCellStyle(costoMercanciasVendidasEstilo);
            Cell celdaCostoMercanciasVendidas5 = CostoMercanciasVendidas.createCell(5);
            celdaCostoMercanciasVendidas5.setCellStyle(celdasConOperacion);
            celdaCostoMercanciasVendidas5.setCellFormula("E10+E13-E14");
            Row UtilidadBruta = sheet.createRow(15);
            Cell celdaUtilidadBruta = UtilidadBruta.createCell(0);
            CellStyle utilidadBrutaEstilo = book.createCellStyle();
            sheet.addMergedRegion(new CellRangeAddress(15, 15, 0, 1));
            utilidadBrutaEstilo.setFillBackgroundColor(IndexedColors.WHITE.getIndex());
            utilidadBrutaEstilo.setFillForegroundColor(IndexedColors.WHITE.getIndex());
            utilidadBrutaEstilo=estilodeCelda(utilidadBrutaEstilo);
            utilidadBrutaEstilo.setAlignment(HorizontalAlignment.LEFT);
            celdaUtilidadBruta.setCellStyle(utilidadBrutaEstilo);
            celdaUtilidadBruta.setCellValue("Utilidad Bruta");
            Cell celdaUtilidadBruta2 = UtilidadBruta.createCell(2);
            celdaUtilidadBruta2.setCellStyle(utilidadBrutaEstilo);
            Cell celdaUtilidadBruta3 = UtilidadBruta.createCell(3);
            celdaUtilidadBruta3.setCellStyle(utilidadBrutaEstilo);
            Cell celdaUtilidadBruta4 = UtilidadBruta.createCell(4);
            celdaUtilidadBruta4.setCellStyle(utilidadBrutaEstilo);
            Cell celdaUtilidadBruta5 = UtilidadBruta.createCell(5);
            celdaUtilidadBruta5.setCellStyle(utilidadBrutaEstilo);
            celdaUtilidadBruta5.setCellFormula("F9-F15");
            Row GastosTotales = sheet.createRow(16);
            Cell celdaGastosTotales = GastosTotales.createCell(0);
            CellStyle gastosTotalesEstilo = book.createCellStyle();
            sheet.addMergedRegion(new CellRangeAddress(16, 16, 0, 1));
            gastosTotalesEstilo.setFillBackgroundColor(IndexedColors.WHITE.getIndex());
            gastosTotalesEstilo.setFillForegroundColor(IndexedColors.WHITE.getIndex());
            gastosTotalesEstilo=estilodeCelda(gastosTotalesEstilo);
            gastosTotalesEstilo.setAlignment(HorizontalAlignment.LEFT);
            celdaGastosTotales.setCellStyle(gastosTotalesEstilo);
            celdaGastosTotales.setCellValue("(-)Gastos Totales");
            Cell celdaGastosTotales2 = GastosTotales.createCell(2);
            celdaGastosTotales2.setCellStyle(gastosTotalesEstilo);
            Cell celdaGastosTotales3 = GastosTotales.createCell(3);
            celdaGastosTotales3.setCellStyle(gastosTotalesEstilo);
            Cell celdaGastosTotales4 = GastosTotales.createCell(4);
            celdaGastosTotales4.setCellStyle(gastosTotalesEstilo);
            Cell celdaGastosTotales5 = GastosTotales.createCell(5);
            celdaGastosTotales5.setCellStyle(celdasConOperacion);
            celdaGastosTotales5.setCellValue(conexion.ObtenerDato("total_gastos"));

            Row UtilidadOperacion = sheet.createRow(17);
            Cell celdaUtilidadOperacion = UtilidadOperacion.createCell(0);
            CellStyle utilidadOperacionEstilo = book.createCellStyle();
            sheet.addMergedRegion(new CellRangeAddress(17, 17, 0, 1));
            utilidadOperacionEstilo.setFillBackgroundColor(IndexedColors.WHITE.getIndex());
            utilidadOperacionEstilo.setFillForegroundColor(IndexedColors.WHITE.getIndex());
            utilidadOperacionEstilo=estilodeCelda(utilidadOperacionEstilo);
            utilidadOperacionEstilo.setAlignment(HorizontalAlignment.LEFT);
            celdaUtilidadOperacion.setCellStyle(utilidadOperacionEstilo);
            celdaUtilidadOperacion.setCellValue("Utilidad de Operación");
            Cell celdaUtilidadOperacion2 = UtilidadOperacion.createCell(2);
            celdaUtilidadOperacion2.setCellStyle(utilidadOperacionEstilo);
            Cell celdaUtilidadOperacion3 = UtilidadOperacion.createCell(3);
            celdaUtilidadOperacion3.setCellStyle(utilidadOperacionEstilo);
            Cell celdaUtilidadOperacion4 = UtilidadOperacion.createCell(4);
            celdaUtilidadOperacion4.setCellStyle(utilidadOperacionEstilo);
            Cell celdaUtilidadOperacion5 = UtilidadOperacion.createCell(5);
            celdaUtilidadOperacion5.setCellStyle(utilidadOperacionEstilo);
            celdaUtilidadOperacion5.setCellFormula("F16-F17");
            Row OtrosIngresos = sheet.createRow(18);
            Cell celdaOtrosIngresos = OtrosIngresos.createCell(0);
            CellStyle otrosIngresosEstilo = book.createCellStyle();
            sheet.addMergedRegion(new CellRangeAddress(18, 18, 0, 1));
            otrosIngresosEstilo.setFillBackgroundColor(IndexedColors.WHITE.getIndex());
            otrosIngresosEstilo.setFillForegroundColor(IndexedColors.WHITE.getIndex());
            otrosIngresosEstilo=estilodeCelda(otrosIngresosEstilo);
            otrosIngresosEstilo.setAlignment(HorizontalAlignment.LEFT);
            celdaOtrosIngresos.setCellStyle(otrosIngresosEstilo);
            celdaOtrosIngresos.setCellValue("(+)Otros Ingresos");
            Cell celdaOtrosIngresos2 = OtrosIngresos.createCell(2);
            celdaOtrosIngresos2.setCellStyle(otrosIngresosEstilo);
            Cell celdaOtrosIngresos3 = OtrosIngresos.createCell(3);
            celdaOtrosIngresos3.setCellStyle(otrosIngresosEstilo);
            Cell celdaOtrosIngresos4 = OtrosIngresos.createCell(4);
            celdaOtrosIngresos4.setCellStyle(otrosIngresosEstilo);
            Cell celdaOtrosIngresos5 = OtrosIngresos.createCell(5);
            celdaOtrosIngresos5.setCellStyle(celdasConOperacion);
            celdaOtrosIngresos5.setCellValue(conexion.ObtenerDato("total_otras_ganancias"));
            Row UtilidadAntesImpuestos = sheet.createRow(19);
            Cell celdaUtilidadAntesImpuestos = UtilidadAntesImpuestos.createCell(0);
            CellStyle utilidadAntesImpuestosEstilo = book.createCellStyle();
            sheet.addMergedRegion(new CellRangeAddress(19, 19, 0, 1));
            utilidadAntesImpuestosEstilo.setFillBackgroundColor(IndexedColors.WHITE.getIndex());
            utilidadAntesImpuestosEstilo.setFillForegroundColor(IndexedColors.WHITE.getIndex());
            utilidadAntesImpuestosEstilo=estilodeCelda(utilidadAntesImpuestosEstilo);
            utilidadAntesImpuestosEstilo.setAlignment(HorizontalAlignment.LEFT);
            celdaUtilidadAntesImpuestos.setCellStyle(utilidadAntesImpuestosEstilo);
            celdaUtilidadAntesImpuestos.setCellValue("Utilidad antes Impuestos");
            Cell celdaUtilidadAntesImpuestos2 = UtilidadAntesImpuestos.createCell(2);
            celdaUtilidadAntesImpuestos2.setCellStyle(utilidadAntesImpuestosEstilo);
            Cell celdaUtilidadAntesImpuestos3 = UtilidadAntesImpuestos.createCell(3);
            celdaUtilidadAntesImpuestos3.setCellStyle(utilidadAntesImpuestosEstilo);
            Cell celdaUtilidadAntesImpuestos4 = UtilidadAntesImpuestos.createCell(4);
            celdaUtilidadAntesImpuestos4.setCellStyle(utilidadAntesImpuestosEstilo);
            Cell celdaUtilidadAntesImpuestos5 = UtilidadAntesImpuestos.createCell(5);
            celdaUtilidadAntesImpuestos5.setCellStyle(utilidadAntesImpuestosEstilo);
            celdaUtilidadAntesImpuestos5.setCellFormula("F18+F19");
            Row Impuestos = sheet.createRow(20);
            Cell celdaImpuestos = Impuestos.createCell(0);
            CellStyle impuestosEstilo = book.createCellStyle();
            sheet.addMergedRegion(new CellRangeAddress(20, 20, 0, 1));
            impuestosEstilo.setFillBackgroundColor(IndexedColors.WHITE.getIndex());
            impuestosEstilo.setFillForegroundColor(IndexedColors.WHITE.getIndex());
            impuestosEstilo=estilodeCelda(impuestosEstilo);
            impuestosEstilo.setAlignment(HorizontalAlignment.LEFT);
            celdaImpuestos.setCellStyle(impuestosEstilo);
            celdaImpuestos.setCellValue("(-)Impuestos");
            Cell celdaImpuestos2 = Impuestos.createCell(2);
            celdaImpuestos2.setCellStyle(impuestosEstilo);
            Cell celdaImpuestos3 = Impuestos.createCell(3);
            celdaImpuestos3.setCellStyle(impuestosEstilo);
            Cell celdaImpuestos4 = Impuestos.createCell(4);
            celdaImpuestos4.setCellStyle(impuestosEstilo);
            Cell celdaImpuestos5 = Impuestos.createCell(5);
            celdaImpuestos5.setCellStyle(celdasConOperacion);
            celdaImpuestos5.setCellFormula("F20*0.16");
            Row UtilidadDelEjercicio = sheet.createRow(21);
            Cell celdaUtilidadDelEjercicio = UtilidadDelEjercicio.createCell(0);
            CellStyle utilidadDelEjercicioEstilo = book.createCellStyle();
            sheet.addMergedRegion(new CellRangeAddress(21, 21, 0, 1));
            utilidadDelEjercicioEstilo.setFillBackgroundColor(IndexedColors.WHITE.getIndex());
            utilidadDelEjercicioEstilo.setFillForegroundColor(IndexedColors.WHITE.getIndex());
            utilidadDelEjercicioEstilo=estilodeCelda(utilidadDelEjercicioEstilo);
            utilidadDelEjercicioEstilo.setAlignment(HorizontalAlignment.LEFT);
            celdaUtilidadDelEjercicio.setCellStyle(utilidadDelEjercicioEstilo);
            celdaUtilidadDelEjercicio.setCellValue("Utilidad del Ejercicio");
            Cell celdaUtilidadDelEjercicio2 = UtilidadDelEjercicio.createCell(2);
            celdaUtilidadDelEjercicio2.setCellStyle(utilidadDelEjercicioEstilo);
            Cell celdaUtilidadDelEjercicio3 = UtilidadDelEjercicio.createCell(3);
            celdaUtilidadDelEjercicio3.setCellStyle(utilidadDelEjercicioEstilo);
            Cell celdaUtilidadDelEjercicio4 = UtilidadDelEjercicio.createCell(4);
            celdaUtilidadDelEjercicio4.setCellStyle(utilidadDelEjercicioEstilo);
            Cell celdaUtilidadDelEjercicio5 = UtilidadDelEjercicio.createCell(5);
            CellStyle utilidadDelEjercicioEstilo2 = book.createCellStyle();
            utilidadDelEjercicioEstilo2=estilodeCelda(utilidadDelEjercicioEstilo2);
            utilidadDelEjercicioEstilo2.setFillBackgroundColor(IndexedColors.YELLOW.getIndex());
            utilidadDelEjercicioEstilo2.setFillForegroundColor(IndexedColors.YELLOW.getIndex());

            celdaUtilidadDelEjercicio5.setCellStyle(utilidadDelEjercicioEstilo2);
            celdaUtilidadDelEjercicio5.setCellFormula("F20-F21");

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
    
    public static void reporteDiario(String nombreRD){
        //Crear un libro de Excel
        Workbook book = new XSSFWorkbook();
        //Crear una hoja
        Sheet sheet = book.createSheet("Reporte Diario");
        
        //Crear estilos
        CellStyle tituloEstilo = crearEstiloTitulo(book);
        CellStyle encabezadoEstilo = crearEstiloEncabezado(book);
        CellStyle datosEstilo = crearEstiloDatos(book);
        
        //Filas ocupadas
        int filasOcup = 0;
        
        //Titulo
        CellRangeAddress celdaFusionT = new CellRangeAddress(0, 0, 0, 2);//Se fusionan 3 celdas
        sheet.addMergedRegion(celdaFusionT);
        Row filaTitulo = sheet.createRow(filasOcup);//Celda creada en la region fusionada
        Cell celdaTitulo = filaTitulo.createCell(0);
        celdaTitulo.setCellValue("REPORTE DIARIO");
        celdaTitulo.setCellStyle(tituloEstilo); //Se aplica el estilo
        filasOcup++;
        
        //Fecha
        CellRangeAddress celdaFusionF = new CellRangeAddress(1, 1, 0, 2);
        sheet.addMergedRegion(celdaFusionF);
        Row filaFecha = sheet.createRow(filasOcup);
        Cell celdaFecha = filaFecha.createCell(0);
        celdaFecha.setCellValue(fecha());
        celdaFecha.setCellStyle(tituloEstilo);
        filasOcup++;
        
        //Obtener la tabla
        java.sql.ResultSet rs = conect.reporte_diario();
        Double[] datos = new Double[5];
        try{
            while(rs.next()){
                datos[0] = rs.getDouble(1);
                datos[1] = rs.getDouble(2);
                datos[2] = rs.getDouble(3);
                datos[3] = rs.getDouble(4);
                datos[4] = rs.getDouble(5);
            }
        } catch(java.sql.SQLException e){
            System.out.println(e.getMessage());
        }
        
        //Escribir el encabezado de la tabla
        Row filaE = sheet.createRow(filasOcup);
        for (int i=0; i<3; i++){
            Cell celdaE = filaE.createCell(i);
            String neim = "";
            switch(i){
                case 0 -> neim = "CUENTA";
                case 1 -> neim = "DEBE";
                case 2 -> neim = "HABER";
            }
            celdaE.setCellValue(neim);
            celdaE.setCellStyle(encabezadoEstilo); //Aplicar el estilo
        }
        filasOcup++;
        
        //Escribir los nombres de las cuentas
        int lim = filasOcup + 5;
        int aux = 0;
        while (filasOcup < lim){
            Row filaC = sheet.createRow(filasOcup);
            
            //Cuentas
            Cell celdaC = filaC.createCell(0);
            String neim = "";
            switch(aux){
                case 0 -> neim = "Ventas";
                case 1 -> neim = "Devoluciones de Venta";
                case 2 -> neim = "Compras";
                case 3 -> neim = "Gastos";
                case 4 -> neim = "Otras Ganancias";
            }
            celdaC.setCellValue(neim);
            //Aplicar el estilo
            celdaC.setCellStyle(datosEstilo);
            
            //DEBE
            Cell celdaD = filaC.createCell(1);
            if(aux==0 || aux==4){
                Double dato = 0.0;
                switch(aux){
                    case 0 -> dato = datos[0];
                    case 4 -> dato = datos[4];
                }
                celdaD.setCellValue(dato);
            }
            celdaD.setCellStyle(datosEstilo);
            
            //HABER
            Cell celdaH = filaC.createCell(2);
            if(aux==1 || aux==2 || aux==3){
                Double dato = 0.0;
                switch(aux){
                    case 1 -> dato = datos[1];
                    case 2 -> dato = datos[2];
                    case 3 -> dato = datos[3];
                }
                celdaH.setCellValue(dato);
            }
            celdaH.setCellStyle(datosEstilo);
            
            filasOcup++;
            aux++;
        }
        
        //Suma
        Row sumaFila = sheet.createRow(filasOcup);
        //DEBE
        Cell celdaSumD = sumaFila.createCell(1);
        celdaSumD.setCellFormula("SUM(B" + (lim - 4) + ":B" + (filasOcup) + ")");
        celdaSumD.setCellStyle(datosEstilo);
        //HABER
        Cell celdaSumH = sumaFila.createCell(2);
        celdaSumH.setCellFormula("SUM(C" + (lim - 4) + ":C" + (filasOcup) + ")");
        celdaSumH.setCellStyle(datosEstilo);
        
        //Ajustar el tamaño de las columnas
        for (int i = 0; i < 3; i++) {
            sheet.autoSizeColumn(i);
        }
        
        //Guardar el archivo Excel
        try {
            FileOutputStream fos = new FileOutputStream(nombreRD);
            book.write(fos);
            System.out.println("Archivo Excel creado");
        } catch(Exception e) {
            System.out.println("Error al guardar el excel");
        }
        
        //Cerrar el libro
        try {
            book.close();
        } catch(Exception e) {
            System.out.println("Error al cerrar el archivo");
        }
        
    }
    
    private static CellStyle crearEstiloTitulo(Workbook buk){
        CellStyle estilo = buk.createCellStyle();
        
        // Fuente en Negrita
        Font fuente = buk.createFont();
        fuente.setBold(true);
        fuente.setFontName("Times New Roman");
        fuente.setFontHeightInPoints((short)16);
        estilo.setFont(fuente);
        
        //Alineacion
        estilo.setAlignment(HorizontalAlignment.CENTER);
        estilo.setVerticalAlignment(VerticalAlignment.CENTER);
        
        return estilo;
    }
    
    private static CellStyle crearEstiloEncabezado(Workbook buk){
        CellStyle estilo = buk.createCellStyle();
        
        //Color de Fondo
        estilo.setFillForegroundColor(IndexedColors.ROSE.getIndex());
        estilo.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        
        //Bordes
        estilo.setBorderTop(BorderStyle.THIN);
        estilo.setBorderBottom(BorderStyle.THIN);
        estilo.setBorderLeft(BorderStyle.THIN);
        estilo.setBorderRight(BorderStyle.THIN);
        
        // Fuente en Negrita
        Font fuente = buk.createFont();
        fuente.setBold(true);
        fuente.setFontName("Times New Roman");
        fuente.setFontHeightInPoints((short)12);
        estilo.setFont(fuente);
        
        //Alineacion
        estilo.setAlignment(HorizontalAlignment.CENTER);
        estilo.setVerticalAlignment(VerticalAlignment.CENTER);
        
        return estilo;
    }
    
    private static CellStyle crearEstiloDatos(Workbook buk){
        CellStyle estilo = buk.createCellStyle();
        
        //Bordes
        estilo.setBorderTop(BorderStyle.THIN);
        estilo.setBorderBottom(BorderStyle.THIN);
        estilo.setBorderLeft(BorderStyle.THIN);
        estilo.setBorderRight(BorderStyle.THIN);
        
        //Fuente
        Font fuente = buk.createFont();
        fuente.setFontName("Times New Roman");
        fuente.setFontHeightInPoints((short)12);
        estilo.setFont(fuente);
        
        //Alineacion
        estilo.setAlignment(HorizontalAlignment.LEFT);
        estilo.setVerticalAlignment(VerticalAlignment.CENTER);
        
        return estilo;
    }
    
    public static void abrirExcel(String nombreXLSX){
        try {
            // Crear un objeto File con la ruta del archivo
            File archivoExcel = new File(nombreXLSX);

            // Verificar si el escritorio soporta la acción de apertura
            if (Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();

                // Abrir el archivo en el programa predeterminado (Excel)
                if (archivoExcel.exists()) {
                    desktop.open(archivoExcel);
                    System.out.println("Archivo Excel fue abierto exitosamente.");
                } else {
                    System.out.println("El archivo " + nombreXLSX + " no existe");
                }
            } else {
                System.out.println("El entorno no soporta abrir aplicaciones.");
            }
        } catch (Exception e) {
            System.out.println("Error al abrir el archivo");
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
    
    public static CellStyle estilodeCeldaOperacion(CellStyle Estilo) {
        
        Estilo.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        Estilo.setBorderBottom(BorderStyle.MEDIUM);
        Estilo.setBorderLeft(BorderStyle.THIN);
        Estilo.setBorderRight(BorderStyle.THIN);
        Estilo.setBorderBottom(BorderStyle.THIN);
        Estilo.setAlignment(HorizontalAlignment.CENTER);
        Estilo.setBorderTop(BorderStyle.THIN);
        return Estilo;
    }
    
    public static String fecha(){
        java.time.LocalDateTime ahora = java.time.LocalDateTime.now();
        // Define el formato deseado. Locale.setDefault(new Locale("es", "ES")) es importante para el idioma.
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd 'de' MMMM 'del' yyyy", new java.util.Locale("es", "ES"));;
        return ahora.format(formatter);
    }
}
