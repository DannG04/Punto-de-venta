package puntoventa.report;

import puntoventa.db.*;
import puntoventa.util.*;

import java.awt.Desktop;
import java.io.*;
import java.time.LocalDate;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class Excel {
    public static double utilidadDelEjercicio;
    static ConexionBD conect = new ConexionBD();
    
    public static void balanceGeneral() {//Método que genera el balance general

        Workbook book = new XSSFWorkbook();
        Sheet sheet = book.createSheet("Reporte");
        LocalDate fecha = LocalDate.now();
        //Obtenemos la fecha actual
        String dia = String.valueOf(fecha.getDayOfMonth());
        String mes = String.valueOf(fecha.getMonthValue());
        String anio = String.valueOf(fecha.getYear());
        try {
            String[] empBGData = obtenerDatosEmpresa();
            insertarLogoEmpresa(book, sheet, empBGData[3]);

            // Estilos de encabezados
            CellStyle tituloEstilo = book.createCellStyle();
            tituloEstilo.setFillForegroundColor(IndexedColors.WHITE.getIndex());
            tituloEstilo.setFillBackgroundColor(IndexedColors.WHITE.getIndex());
            tituloEstilo = estilodeCelda(tituloEstilo);
            tituloEstilo.setAlignment(HorizontalAlignment.CENTER);
            tituloEstilo.setVerticalAlignment(VerticalAlignment.CENTER);
            Font fuenteTitulo = book.createFont();
            tituloEstilo.setFillForegroundColor(IndexedColors.WHITE.getIndex());
            tituloEstilo.setFillForegroundColor(IndexedColors.AQUA.getIndex());
            tituloEstilo = estilodeCelda(tituloEstilo);
            tituloEstilo.setBorderRight(BorderStyle.THIN);
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
            String[] empBG = obtenerDatosEmpresa();
            celdaTitulo.setCellValue(empBG[0] + (empBG[1].isEmpty() ? "" : "  RFC: " + empBG[1]));
            sheet.addMergedRegion(new CellRangeAddress(3, 3, 1, 5));
            String mes2 = cambiarmes(mes);
            celdaFecha.setCellValue("Balance general del " + dia + " de " + mes2 + " del año " + anio);

            BalGeneral bg = new BalGeneral(sheet, 6);
            obtenerUtilidad();
            bg.llenarBalance(datosEstilo,utilidadDelEjercicio);

            sheet.setZoom(150);
            String directoryName = "BalancesGenerales";
            File directory = new File(directoryName);
            if (!directory.exists()) {
                directory.mkdirs();
            }
            String fileName = "balanceGeneral" + dia + "_" + mes + "_" + anio;
            File file = new File(directoryName + "/" + fileName + ".xlsx");
            FileOutputStream fileOut = new FileOutputStream(file);
            book.write(fileOut);
            fileOut.close();
            Desktop.getDesktop().open(file);

        } catch (FileNotFoundException ex) {
            Logger.getLogger(Excel.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Excel.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public static void estadodeResultados(Double inv_ini) {//Método que genera el estado de resultados
        ConexionBD conexion = new ConexionBD();

        Workbook book = new XSSFWorkbook();
        Sheet sheet = book.createSheet("Estado de resultados");
        LocalDate fecha = LocalDate.now();
        String dia = String.valueOf(fecha.getDayOfMonth());
        String mes = String.valueOf(fecha.getMonthValue());
        String anio = String.valueOf(fecha.getYear());
        try {
            String[] empERData = obtenerDatosEmpresa();
            insertarLogoEmpresa(book, sheet, empERData[3]);


            // Estilos de encabezados
            CellStyle tituloEstilo = book.createCellStyle();
            tituloEstilo.setFillForegroundColor(IndexedColors.WHITE.getIndex());
            tituloEstilo.setFillBackgroundColor(IndexedColors.WHITE.getIndex());
            tituloEstilo = estilodeCelda(tituloEstilo);
            tituloEstilo.setAlignment(HorizontalAlignment.CENTER);
            tituloEstilo.setVerticalAlignment(VerticalAlignment.CENTER);
            Font fuenteTitulo = book.createFont();
            tituloEstilo.setFillForegroundColor(IndexedColors.WHITE.getIndex());
            tituloEstilo.setFillForegroundColor(IndexedColors.AQUA.getIndex());
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
            String[] empER = obtenerDatosEmpresa();
            celdaTitulo.setCellValue(empER[0] + (empER[1].isEmpty() ? "" : "  RFC: " + empER[1]));
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
            CellStyle celdasNormales = book.createCellStyle();
            celdasNormales.setFillForegroundColor(IndexedColors.WHITE.getIndex());
            celdasNormales.setFillBackgroundColor(IndexedColors.WHITE.getIndex());
            celdasNormales = estilodeCelda(celdasNormales);
            celdasNormales.setAlignment(HorizontalAlignment.LEFT);

            
            //Datos y cuentas
            Row Ventas = sheet.createRow(5);
            Cell celdaVentas = Ventas.createCell(0);
            sheet.addMergedRegion(new CellRangeAddress(5, 5, 0, 1));
            celdaVentas.setCellStyle(celdasNormales);
            celdaVentas.setCellValue("Ventas");
            Cell celdaVentas2 = Ventas.createCell(2);
            celdaVentas2.setCellStyle(celdasNormales);
            Cell celdaVentas3 = Ventas.createCell(3);
            celdaVentas3.setCellStyle(celdasNormales);
            Cell celdaVentas4 = Ventas.createCell(4);
            celdaVentas4.setCellStyle(celdasNormales);
            Cell celdaVentas5 = Ventas.createCell(5);
            celdaVentas5.setCellStyle(celdasNormales);
            celdaVentas5.setCellValue(conexion.ObtenerDato("total_ventas"));
            Row DevVentas = sheet.createRow(6);
            Cell celdaDevVentas = DevVentas.createCell(0);
            sheet.addMergedRegion(new CellRangeAddress(6, 6, 0, 1));
            celdaDevVentas.setCellStyle(celdasNormales);
            celdaDevVentas.setCellValue("(-)Dev. Ventas");
            Cell celdaDevVentas2 = DevVentas.createCell(2);
            celdaDevVentas2.setCellStyle(celdasNormales);
            Cell celdaDevVentas3 = DevVentas.createCell(3);
            celdaDevVentas3.setCellStyle(celdasNormales);
            Cell celdaDevVentas4 = DevVentas.createCell(4);
            celdaDevVentas4.setCellStyle(celdasNormales);
            Cell celdaDevVentas5 = DevVentas.createCell(5);
            celdaDevVentas5.setCellStyle(celdasNormales);
            celdaDevVentas4.setCellValue(conexion.ObtenerDato("total_devoluciones"));
            Row DescVentas = sheet.createRow(7);
            Cell celdaDescVentas = DescVentas.createCell(0);
            sheet.addMergedRegion(new CellRangeAddress(7, 7, 0, 1));
            celdaDescVentas.setCellStyle(celdasNormales);
            celdaDescVentas.setCellValue("(-)Desc. Ventas");
            Cell celdaDescVentas2 = DescVentas.createCell(2);
            celdaDescVentas2.setCellStyle(celdasNormales);
            Cell celdaDescVentas3 = DescVentas.createCell(3);
            celdaDescVentas3.setCellStyle(celdasNormales);
            Cell celdaDescVentas4 = DescVentas.createCell(4);
            celdaDescVentas4.setCellStyle(celdasConOperacion);
            Cell celdaDescVentas5 = DescVentas.createCell(5);
            celdaDescVentas5.setCellStyle(celdasConOperacion);
            celdaDescVentas4.setCellValue(0);
            Row VentasNetas = sheet.createRow(8);
            Cell celdaVentasNetas = VentasNetas.createCell(0);
            sheet.addMergedRegion(new CellRangeAddress(8, 8, 0, 1));
            celdaVentasNetas.setCellStyle(celdasNormales);
            celdaVentasNetas.setCellValue("Ventas Netas");
            Cell celdaVentasNetas2 = VentasNetas.createCell(2);
            celdaVentasNetas2.setCellStyle(celdasNormales);
            Cell celdaVentasNetas3 = VentasNetas.createCell(3);
            celdaVentasNetas3.setCellStyle(celdasNormales);
            Cell celdaVentasNetas4 = VentasNetas.createCell(4);
            celdaVentasNetas4.setCellStyle(celdasNormales);
            Cell celdaVentasNetas5 = VentasNetas.createCell(5);
            celdaVentasNetas5.setCellStyle(celdasNormales);
            celdaVentasNetas5.setCellFormula("F6-E7-E8");
            Row CostoVentas = sheet.createRow(9);
            Cell celdaCostoVentas = CostoVentas.createCell(0);
            sheet.addMergedRegion(new CellRangeAddress(9, 9, 0, 1));
            celdaCostoVentas.setCellStyle(celdasNormales);
            celdaCostoVentas.setCellValue("Costo de Ventas");
            Cell celdaCostoVentas2 = CostoVentas.createCell(2);
            celdaCostoVentas2.setCellStyle(celdasNormales);
            Cell celdaCostoVentas3 = CostoVentas.createCell(3);
            celdaCostoVentas3.setCellStyle(celdasNormales);
            Cell celdaCostoVentas4 = CostoVentas.createCell(4);
            celdaCostoVentas4.setCellStyle(celdasNormales);
            Cell celdaCostoVentas5 = CostoVentas.createCell(5);
            celdaCostoVentas5.setCellStyle(celdasNormales);
            Row InventarioInicial = sheet.createRow(10);
            Cell celdaInventarioInicial = InventarioInicial.createCell(0);
            sheet.addMergedRegion(new CellRangeAddress(10, 10, 0, 1));
            celdaInventarioInicial.setCellStyle(celdasNormales);
            celdaInventarioInicial.setCellValue("(+)Inventario Inicial");
            Cell celdaInventarioInicial2 = InventarioInicial.createCell(2);
            celdaInventarioInicial2.setCellStyle(celdasNormales);
            Cell celdaInventarioInicial3 = InventarioInicial.createCell(3);
            celdaInventarioInicial3.setCellStyle(celdasNormales);
            Cell celdaInventarioInicial4 = InventarioInicial.createCell(4);
            celdaInventarioInicial4.setCellStyle(celdasNormales);
            celdaInventarioInicial4.setCellValue(inv_ini);
            Cell celdaInventarioInicial5 = InventarioInicial.createCell(5);
            celdaInventarioInicial5.setCellStyle(celdasNormales);
            Row ComprasNetas = sheet.createRow(11);
            Cell celdaComprasNetas = ComprasNetas.createCell(0);
            sheet.addMergedRegion(new CellRangeAddress(11, 11, 0, 1));
            celdaComprasNetas.setCellStyle(celdasNormales);
            celdaComprasNetas.setCellValue("Compras Netas");
            Cell celdaComprasNetas2 = ComprasNetas.createCell(2);
            celdaComprasNetas2.setCellStyle(celdasNormales);
            Cell celdaComprasNetas3 = ComprasNetas.createCell(3);
            celdaComprasNetas3.setCellStyle(celdasNormales);
            Cell celdaComprasNetas4 = ComprasNetas.createCell(4);
            celdaComprasNetas4.setCellStyle(celdasConOperacion);
            celdaComprasNetas4.setCellValue(conexion.ObtenerDato("total_compras"));
            Cell celdaComprasNetas5 = ComprasNetas.createCell(5);
            celdaComprasNetas5.setCellStyle(celdasNormales);
            Row TotalMercancias = sheet.createRow(12);
            Cell celdaTotalMercancias = TotalMercancias.createCell(0);
            sheet.addMergedRegion(new CellRangeAddress(12, 12, 0, 1));
            celdaTotalMercancias.setCellStyle(celdasNormales);
            celdaTotalMercancias.setCellValue("Total de Mercancias");
            Cell celdaTotalMercancias2 = TotalMercancias.createCell(2);
            celdaTotalMercancias2.setCellStyle(celdasNormales);
            Cell celdaTotalMercancias3 = TotalMercancias.createCell(3);
            celdaTotalMercancias3.setCellStyle(celdasNormales);
            Cell celdaTotalMercancias4 = TotalMercancias.createCell(4);
            celdaTotalMercancias4.setCellStyle(celdasNormales);
            celdaTotalMercancias4.setCellFormula("E11+E12");
            Cell celdaTotalMercancia5 = TotalMercancias.createCell(5);
            celdaTotalMercancia5.setCellStyle(celdasNormales);
            Row InventarioFinal = sheet.createRow(13);
            Cell celdaInventarioFinal = InventarioFinal.createCell(0);
            sheet.addMergedRegion(new CellRangeAddress(13, 13, 0, 1));
            celdaInventarioFinal.setCellStyle(celdasNormales);
            celdaInventarioFinal.setCellValue("(-)Inventario Final");
            Cell celdaInventarioFinal2 = InventarioFinal.createCell(2);
            celdaInventarioFinal2.setCellStyle(celdasNormales);
            Cell celdaInventarioFinal3 = InventarioFinal.createCell(3);
            celdaInventarioFinal3.setCellStyle(celdasNormales);
            Cell celdaInventarioFinal4 = InventarioFinal.createCell(4);
            celdaInventarioFinal4.setCellStyle(celdasConOperacion);
            celdaInventarioFinal4.setCellValue(conexion.ObtenerDato("inventario_final"));
            Cell celdaInventarioFinal5 = InventarioFinal.createCell(5);
            celdaInventarioFinal5.setCellStyle(celdasNormales);
            Row CostoMercanciasVendidas = sheet.createRow(14);
            Cell celdaCostoMercanciasVendidas = CostoMercanciasVendidas.createCell(0);
            sheet.addMergedRegion(new CellRangeAddress(14, 14, 0, 1));
            celdaCostoMercanciasVendidas.setCellStyle(celdasNormales);
            celdaCostoMercanciasVendidas.setCellValue("Costo de lo vendido");
            Cell celdaCostoMercanciasVendidas2 = CostoMercanciasVendidas.createCell(2);
            celdaCostoMercanciasVendidas2.setCellStyle(celdasNormales);
            Cell celdaCostoMercanciasVendidas3 = CostoMercanciasVendidas.createCell(3);
            celdaCostoMercanciasVendidas3.setCellStyle(celdasNormales);
            Cell celdaCostoMercanciasVendidas4 = CostoMercanciasVendidas.createCell(4);
            celdaCostoMercanciasVendidas4.setCellStyle(celdasNormales);
            Cell celdaCostoMercanciasVendidas5 = CostoMercanciasVendidas.createCell(5);
            celdaCostoMercanciasVendidas5.setCellStyle(celdasConOperacion);
            celdaCostoMercanciasVendidas5.setCellFormula("E10+E13-E14");
            Row UtilidadBruta = sheet.createRow(15);
            Cell celdaUtilidadBruta = UtilidadBruta.createCell(0);
            sheet.addMergedRegion(new CellRangeAddress(15, 15, 0, 1));
            celdaUtilidadBruta.setCellValue("Utilidad Bruta");
            celdaUtilidadBruta.setCellStyle(celdasNormales);
            Cell celdaUtilidadBruta2 = UtilidadBruta.createCell(2);
            celdaUtilidadBruta2.setCellStyle(celdasNormales);
            Cell celdaUtilidadBruta3 = UtilidadBruta.createCell(3);
            celdaUtilidadBruta3.setCellStyle(celdasNormales);
            Cell celdaUtilidadBruta4 = UtilidadBruta.createCell(4);
            celdaUtilidadBruta4.setCellStyle(celdasNormales);
            Cell celdaUtilidadBruta5 = UtilidadBruta.createCell(5);
            celdaUtilidadBruta5.setCellStyle(celdasNormales);
            celdaUtilidadBruta5.setCellFormula("F9-F15");
            Row GastosTotales = sheet.createRow(16);
            Cell celdaGastosTotales = GastosTotales.createCell(0);
            sheet.addMergedRegion(new CellRangeAddress(16, 16, 0, 1));
            celdaGastosTotales.setCellValue("(-)Gastos Totales");
            Cell celdaGastosTotales2 = GastosTotales.createCell(2);
            celdaGastosTotales2.setCellStyle(celdasNormales);
            Cell celdaGastosTotales3 = GastosTotales.createCell(3);
            celdaGastosTotales3.setCellStyle(celdasNormales);
            Cell celdaGastosTotales4 = GastosTotales.createCell(4);
            celdaGastosTotales4.setCellStyle(celdasNormales);
            Cell celdaGastosTotales5 = GastosTotales.createCell(5);
            celdaGastosTotales5.setCellStyle(celdasConOperacion);
            celdaGastosTotales5.setCellValue(conexion.ObtenerDato("total_gastos"));

            Row UtilidadOperacion = sheet.createRow(17);
            Cell celdaUtilidadOperacion = UtilidadOperacion.createCell(0);
            sheet.addMergedRegion(new CellRangeAddress(17, 17, 0, 1));
            celdaUtilidadOperacion.setCellValue("Utilidad de Operación");
            celdaUtilidadOperacion.setCellStyle(celdasNormales);
            Cell celdaUtilidadOperacion2 = UtilidadOperacion.createCell(2);
            celdaUtilidadOperacion2.setCellStyle(celdasNormales);
            Cell celdaUtilidadOperacion3 = UtilidadOperacion.createCell(3);
            celdaUtilidadOperacion3.setCellStyle(celdasNormales);
            Cell celdaUtilidadOperacion4 = UtilidadOperacion.createCell(4);
            celdaUtilidadOperacion4.setCellStyle(celdasNormales);
            Cell celdaUtilidadOperacion5 = UtilidadOperacion.createCell(5);
            celdaUtilidadOperacion5.setCellStyle(celdasNormales);
            celdaUtilidadOperacion5.setCellFormula("F16-F17");
            Row OtrosIngresos = sheet.createRow(18);
            Cell celdaOtrosIngresos = OtrosIngresos.createCell(0);
            sheet.addMergedRegion(new CellRangeAddress(18, 18, 0, 1));
            celdaOtrosIngresos.setCellStyle(celdasNormales);
            celdaOtrosIngresos.setCellValue("(+)Otros Ingresos");
            Cell celdaOtrosIngresos2 = OtrosIngresos.createCell(2);
            celdaOtrosIngresos2.setCellStyle(celdasNormales);
            Cell celdaOtrosIngresos3 = OtrosIngresos.createCell(3);
            celdaOtrosIngresos3.setCellStyle(celdasNormales);
            Cell celdaOtrosIngresos4 = OtrosIngresos.createCell(4);
            celdaOtrosIngresos4.setCellStyle(celdasNormales);
            Cell celdaOtrosIngresos5 = OtrosIngresos.createCell(5);
            celdaOtrosIngresos5.setCellStyle(celdasConOperacion);
            celdaOtrosIngresos5.setCellValue(conexion.ObtenerDato("total_otras_ganancias"));
            Row UtilidadAntesImpuestos = sheet.createRow(19);
            Cell celdaUtilidadAntesImpuestos = UtilidadAntesImpuestos.createCell(0);
            sheet.addMergedRegion(new CellRangeAddress(19, 19, 0, 1));
            celdaUtilidadAntesImpuestos.setCellStyle(celdasNormales);
            celdaUtilidadAntesImpuestos.setCellValue("Utilidad antes Impuestos");
            Cell celdaUtilidadAntesImpuestos2 = UtilidadAntesImpuestos.createCell(2);
            celdaUtilidadAntesImpuestos2.setCellStyle(celdasNormales);
            Cell celdaUtilidadAntesImpuestos3 = UtilidadAntesImpuestos.createCell(3);
            celdaUtilidadAntesImpuestos3.setCellStyle(celdasNormales);
            Cell celdaUtilidadAntesImpuestos4 = UtilidadAntesImpuestos.createCell(4);
            celdaUtilidadAntesImpuestos4.setCellStyle(celdasNormales);
            Cell celdaUtilidadAntesImpuestos5 = UtilidadAntesImpuestos.createCell(5);
            celdaUtilidadAntesImpuestos5.setCellStyle(celdasNormales);
            celdaUtilidadAntesImpuestos5.setCellFormula("F18+F19");
            Row Impuestos = sheet.createRow(20);
            Cell celdaImpuestos = Impuestos.createCell(0);
            sheet.addMergedRegion(new CellRangeAddress(20, 20, 0, 1));
            celdaImpuestos.setCellStyle(celdasNormales);
            celdaImpuestos.setCellValue("(-)Impuestos");
            Cell celdaImpuestos2 = Impuestos.createCell(2);
            celdaImpuestos2.setCellStyle(celdasNormales);
            Cell celdaImpuestos3 = Impuestos.createCell(3);
            celdaImpuestos3.setCellStyle(celdasNormales);
            Cell celdaImpuestos4 = Impuestos.createCell(4);
            celdaImpuestos4.setCellStyle(celdasNormales);
            Cell celdaImpuestos5 = Impuestos.createCell(5);
            celdaImpuestos5.setCellStyle(celdasConOperacion);
            celdaImpuestos5.setCellFormula("F20*0.16");
            Row UtilidadDelEjercicio = sheet.createRow(21);
            Cell celdaUtilidadDelEjercicio = UtilidadDelEjercicio.createCell(0);
            sheet.addMergedRegion(new CellRangeAddress(21, 21, 0, 1));
            celdaUtilidadDelEjercicio.setCellStyle(celdasNormales);
            celdaUtilidadDelEjercicio.setCellValue("Utilidad del Ejercicio");
            Cell celdaUtilidadDelEjercicio2 = UtilidadDelEjercicio.createCell(2);
            celdaUtilidadDelEjercicio2.setCellStyle(celdasNormales);
            Cell celdaUtilidadDelEjercicio3 = UtilidadDelEjercicio.createCell(3);
            celdaUtilidadDelEjercicio3.setCellStyle(celdasNormales);
            Cell celdaUtilidadDelEjercicio4 = UtilidadDelEjercicio.createCell(4);
            celdaUtilidadDelEjercicio4.setCellStyle(celdasNormales);
            Cell celdaUtilidadDelEjercicio5 = UtilidadDelEjercicio.createCell(5);
            CellStyle utilidadDelEjercicioEstilo2 = book.createCellStyle();
            utilidadDelEjercicioEstilo2=estilodeCelda(utilidadDelEjercicioEstilo2);
            utilidadDelEjercicioEstilo2.setFillBackgroundColor(IndexedColors.YELLOW.getIndex());
            utilidadDelEjercicioEstilo2.setFillForegroundColor(IndexedColors.YELLOW.getIndex());

            celdaUtilidadDelEjercicio5.setCellStyle(utilidadDelEjercicioEstilo2);
            celdaUtilidadDelEjercicio5.setCellFormula("F20-F21");

            sheet.setZoom(150);
            String directoryName = "Estados de resultados";
            File directory = new File(directoryName);
            if (!directory.exists()) {
                directory.mkdirs();
            }
            String fileName = "estadodeResultados" + dia + "_" + mes + "_" + anio;
            File file = new File(directoryName + "/" + fileName + ".xlsx");
            FileOutputStream fileOut = new FileOutputStream(file);
            book.write(fileOut);
            fileOut.close();
            Desktop.getDesktop().open(file);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Excel.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Excel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static void reporteDiario(String nombreRD, java.time.LocalDate fecha) {
        String dia  = String.valueOf(fecha.getDayOfMonth());
        String mes  = String.valueOf(fecha.getMonthValue());
        String anio = String.valueOf(fecha.getYear());

        Workbook book = new XSSFWorkbook();
        Sheet sheet = book.createSheet("Reporte Diario");

        // ── Estilos ───────────────────────────────────────────────────────────
        CellStyle estEmpresa   = crearEstiloEmpresa(book);
        CellStyle estTitulo    = crearEstiloTitulo(book);
        CellStyle estSubtitulo = crearEstiloSubtitulo(book);
        CellStyle estEncabezado= crearEstiloEncabezado(book);
        CellStyle estDatos     = crearEstiloDatos(book);
        CellStyle estMonto     = crearEstiloMonto(book);
        CellStyle estTotal     = crearEstiloTotal(book);
        CellStyle estMontoTotal= crearEstiloMontoTotal(book);
        CellStyle estPositivo  = crearEstiloResultadoPositivo(book);
        CellStyle estNegativo  = crearEstiloResultadoNegativo(book);

        int fila = 0;

        // ════════════════════════════════════════════════════════════════════
        // BLOQUE 1 — ENCABEZADO DE EMPRESA
        // ════════════════════════════════════════════════════════════════════
        String[] emp = obtenerDatosEmpresa();
        // [0]nombre [1]razonSocial [2]rfc [3]telefono [4]correo
        // [5]direccion [6]ciudad [7]estado [8]cp [9]logoRuta
        String nombre      = emp[0];
        String razonSocial = emp[1];
        String rfc         = emp[2];
        String telefono    = emp[3];
        String correo      = emp[4];
        String direccion   = emp[5];
        String ciudad      = emp[6];
        String estado      = emp[7];
        String cp          = emp[8];
        String logoRuta    = emp[9];

        // Logo
        if (!logoRuta.isEmpty()) {
            try {
                java.io.File logoFile = new java.io.File(logoRuta);
                if (logoFile.exists()) {
                    byte[] logoBytes = java.nio.file.Files.readAllBytes(logoFile.toPath());
                    int tipo = logoRuta.toLowerCase().endsWith(".png")
                            ? Workbook.PICTURE_TYPE_PNG
                            : Workbook.PICTURE_TYPE_JPEG;
                    int idx = book.addPicture(logoBytes, tipo);
                    Drawing<?> drawing = sheet.createDrawingPatriarch();
                    ClientAnchor anchor = book.getCreationHelper().createClientAnchor();
                    anchor.setCol1(0); anchor.setRow1(fila);
                    anchor.setCol2(1); anchor.setRow2(fila + 3);
                    drawing.createPicture(anchor, idx);
                    fila += 3;
                }
            } catch (Exception e) {
                System.out.println("Logo no cargado: " + e.getMessage());
            }
        }

        // Nombre empresa
        fila = agregarFilaMerge(sheet, fila, 0, 4, nombre, estEmpresa);

        // Razón social
        if (!razonSocial.isEmpty())
            fila = agregarFilaMerge(sheet, fila, 0, 4, razonSocial, estEmpresa);

        // RFC y Teléfono — misma fila
        if (!rfc.isEmpty() || !telefono.isEmpty()) {
            Row r = sheet.createRow(fila++);
            crearCelda(r, 0, rfc.isEmpty()      ? "" : "RFC: " + rfc,      estSubtitulo);
            crearCelda(r, 3, telefono.isEmpty() ? "" : "Tel: " + telefono, estSubtitulo);
        }

        // Correo
        if (!correo.isEmpty())
            fila = agregarFilaMerge(sheet, fila, 0, 4, correo, estSubtitulo);

        // Dirección completa
        String dirCompleta = java.util.stream.Stream.of(direccion, ciudad, estado,
                                cp.isEmpty() ? "" : "C.P. " + cp)
                .filter(s -> !s.isEmpty())
                .collect(java.util.stream.Collectors.joining(", "));
        if (!dirCompleta.isEmpty())
            fila = agregarFilaMerge(sheet, fila, 0, 4, dirCompleta, estSubtitulo);

        fila++; // separador

        // ════════════════════════════════════════════════════════════════════
        // BLOQUE 2 — TÍTULO DEL REPORTE
        // ════════════════════════════════════════════════════════════════════
        fila = agregarFilaMerge(sheet, fila, 0, 4, "REPORTE DIARIO", estTitulo);
        fila = agregarFilaMerge(sheet, fila, 0, 4, fecha(), estSubtitulo);
        fila++; // separador

        // ════════════════════════════════════════════════════════════════════
        // BLOQUE 3 — DATOS DE LA BASE DE DATOS
        // ════════════════════════════════════════════════════════════════════
        double totalVentas = 0, ventasNetas = 0, totalDevoluciones = 0,
            numTrans = 0, ticketProm = 0, totalCompras = 0,
            totalGastos = 0, otrasGanancias = 0,
            resultadoNeto = 0, valorInventario = 0;
        try {
            java.sql.ResultSet rs = conect.reporte_diario(fecha);
            if (rs != null && rs.next()) {
                totalVentas       = rs.getDouble(1);
                ventasNetas       = rs.getDouble(2);
                totalDevoluciones = rs.getDouble(3);
                numTrans          = rs.getDouble(4);    // bigint → getLong
                ticketProm        = rs.getDouble(5);
                totalCompras      = rs.getDouble(6);
                totalGastos       = rs.getDouble(7);
                otrasGanancias    = rs.getDouble(8);
                resultadoNeto     = rs.getDouble(9);
                valorInventario   = rs.getDouble(10);
            }
        } catch (java.sql.SQLException e) {
            System.out.println("Error al obtener reporte: " + e.getMessage());
        }

        // ════════════════════════════════════════════════════════════════════
        // BLOQUE 4 — SECCIÓN: RESUMEN OPERATIVO
        // ════════════════════════════════════════════════════════════════════
        fila = agregarFilaMerge(sheet, fila, 0, 4, "RESUMEN OPERATIVO", estEncabezado);

        // encabezados de columna
        Row encOp = sheet.createRow(fila++);
        crearCelda(encOp, 0, "CONCEPTO",   estEncabezado);
        crearCelda(encOp, 3, "MONTO",      estEncabezado);
        crearCelda(encOp, 4, "REFERENCIA", estEncabezado);

        Object[][] operativo = {
            {"Número de transacciones", numTrans,       "ventas del día"},
            {"Ticket promedio",         ticketProm,     "total ventas / transacciones"},
            {"Valor del inventario",    valorInventario,"a precio de costo actual"},
        };
        for (Object[] r : operativo) {
            Row row = sheet.createRow(fila++);
            crearCelda(row, 0, (String) r[0], estDatos);
            Cell cMonto = row.createCell(3);
            cMonto.setCellValue((Double) r[1]);
            cMonto.setCellStyle(estMonto);
            crearCelda(row, 4, (String) r[2], estDatos);
        }
        fila++; // separador

        // ════════════════════════════════════════════════════════════════════
        // BLOQUE 5 — SECCIÓN: ESTADO DE RESULTADOS DEL DÍA
        // ════════════════════════════════════════════════════════════════════
        fila = agregarFilaMerge(sheet, fila, 0, 4, "ESTADO DE RESULTADOS DEL DÍA", estEncabezado);

        Row encRes = sheet.createRow(fila++);
        crearCelda(encRes, 0, "CONCEPTO",  estEncabezado);
        crearCelda(encRes, 2, "DEBE",      estEncabezado);
        crearCelda(encRes, 3, "HABER",     estEncabezado);

        // filas: {concepto, debe o null, haber o null}
        int filaVentasBrutas = fila;
        Object[][] resultados = {
            {"(+)  Ventas brutas",         totalVentas,       null},
            {"(-)  Devoluciones de venta", null,              totalDevoluciones},
            {"(=)  Ventas netas",          ventasNetas,       null},
            {"(-)  Compras",               null,              totalCompras},
            {"(-)  Gastos operativos",     null,              totalGastos},
            {"(+)  Otras ganancias",       otrasGanancias,    null},
        };
        for (Object[] r : resultados) {
            Row row = sheet.createRow(fila++);
            crearCelda(row, 0, (String) r[0], estDatos);

            Cell cDebe = row.createCell(2);
            cDebe.setCellStyle(estMonto);
            if (r[1] != null) cDebe.setCellValue((Double) r[1]);

            Cell cHaber = row.createCell(3);
            cHaber.setCellStyle(estMonto);
            if (r[2] != null) cHaber.setCellValue((Double) r[2]);
        }

        // Fila de resultado neto con fórmula Excel
        // DEBE total  = ventas brutas + ventas netas + otras ganancias (col C)
        // HABER total = devoluciones + compras + gastos (col D)
        int filaInicio = filaVentasBrutas + 1; // 1-indexed para Excel
        int filaFin    = fila;                 // última fila de datos

        Row filaRes = sheet.createRow(fila++);
        crearCelda(filaRes, 0, "RESULTADO NETO DEL DÍA", estTotal);
        Cell cNetoDebe  = filaRes.createCell(2);
        Cell cNetoHaber = filaRes.createCell(3);
        // La diferencia real va en DEBE si positivo, HABER si negativo
        CellStyle estiloNeto = resultadoNeto >= 0 ? estPositivo : estNegativo;
        cNetoDebe.setCellValue(resultadoNeto);
        cNetoDebe.setCellStyle(estiloNeto);
        cNetoHaber.setCellStyle(estMontoTotal); // vacía pero con estilo de total

        fila++; // separador

        // ════════════════════════════════════════════════════════════════════
        // BLOQUE 6 — PIE: NOTA AL PIE
        // ════════════════════════════════════════════════════════════════════
        CellStyle estNota = crearEstiloDatos(book);
        Font fuenteNota = book.createFont();
        fuenteNota.setItalic(true);
        fuenteNota.setFontHeightInPoints((short) 8);
        estNota.setFont(fuenteNota);

        fila = agregarFilaMerge(sheet, fila, 0, 4,
            "* El valor del inventario refleja el costo de adquisición más reciente por producto.",
            estNota);
        fila = agregarFilaMerge(sheet, fila, 0, 4,
            "* Reporte generado el " + new java.util.Date(),
            estNota);

        // ── Anchos de columna ────────────────────────────────────────────
        sheet.setColumnWidth(0, 10000); // concepto
        sheet.setColumnWidth(1, 1000);  // espaciado
        sheet.setColumnWidth(2, 4000);  // debe
        sheet.setColumnWidth(3, 4000);  // haber / monto
        sheet.setColumnWidth(4, 6000);  // referencia

        // ── Guardar ──────────────────────────────────────────────────────
        try {
            String dirReportes = System.getProperty("user.home") + "/Documents/Reportes de ventas";
            java.io.File directorio = new java.io.File(dirReportes);
            if (!directorio.exists()) directorio.mkdirs();

            String nombreArchivo = nombreRD + dia + "_" + mes + "_" + anio + ".xlsx";
            java.io.File archivo = new java.io.File(dirReportes + "/" + nombreArchivo);

            if (archivo.exists() && !archivo.canWrite()) {
                javax.swing.JOptionPane.showMessageDialog(null,
                    "El archivo ya está abierto.\nCiérralo e intenta de nuevo.",
                    "Archivo en uso", javax.swing.JOptionPane.WARNING_MESSAGE);
                book.close();
                return;
            }

            java.io.FileOutputStream out = new java.io.FileOutputStream(archivo);
            book.write(out);
            out.close();
            System.out.println("Reporte guardado en: " + archivo.getAbsolutePath());
            java.awt.Desktop.getDesktop().open(archivo);

        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(null,
                "Error al guardar el reporte:\n" + e.getMessage(),
                "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        }

        try { book.close(); } catch (Exception ignored) {}
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private static int agregarFilaMerge(Sheet sheet, int fila, int col1, int col2,
                                        String valor, CellStyle estilo) {
        sheet.addMergedRegion(new CellRangeAddress(fila, fila, col1, col2));
        Row row = sheet.createRow(fila);
        Cell c = row.createCell(col1);
        c.setCellValue(valor);
        c.setCellStyle(estilo);
        return fila + 1;
    }

    private static void crearCelda(Row row, int col, String valor, CellStyle estilo) {
        Cell c = row.createCell(col);
        c.setCellValue(valor);
        c.setCellStyle(estilo);
    }
    
    public static void reporteKardex(String idProducto, String nombreProducto, java.time.LocalDate desde, java.time.LocalDate hasta) {//Método que genera el reporte de kardex de un producto
        String dia = String.valueOf(hasta.getDayOfMonth());
        String mes = String.valueOf(hasta.getMonthValue());
        String anio = String.valueOf(hasta.getYear());

        Workbook book = new XSSFWorkbook();
        Sheet sheet = book.createSheet("Kardex");

        CellStyle tituloEstilo = crearEstiloTitulo(book);
        CellStyle encabezadoEstilo = crearEstiloEncabezado(book);
        CellStyle datosEstilo = crearEstiloDatos(book);

        int fila = 0;

        // Título
        CellRangeAddress fusT = new CellRangeAddress(fila, fila, 0, 6);
        sheet.addMergedRegion(fusT);
        Row rowTitulo = sheet.createRow(fila++);
        Cell cTitulo = rowTitulo.createCell(0);
        cTitulo.setCellValue("KARDEX — " + nombreProducto + " (" + idProducto + ")");
        cTitulo.setCellStyle(tituloEstilo);

        // Periodo
        CellRangeAddress fusP = new CellRangeAddress(fila, fila, 0, 6);
        sheet.addMergedRegion(fusP);
        Row rowPeriodo = sheet.createRow(fila++);
        Cell cPeriodo = rowPeriodo.createCell(0);
        cPeriodo.setCellValue("Periodo: " + desde + " al " + hasta);
        cPeriodo.setCellStyle(tituloEstilo);

        // Encabezados
        String[] columnas = {"Fecha", "Tipo", "Cantidad", "Exist. Anterior", "Exist. Posterior", "Referencia", "Empleado"};
        Row rowEnc = sheet.createRow(fila++);
        for (int i = 0; i < columnas.length; i++) {
            Cell c = rowEnc.createCell(i);
            c.setCellValue(columnas[i]);
            c.setCellStyle(encabezadoEstilo);
        }

        // Datos
        java.sql.ResultSet rs = conect.kardexProducto(idProducto, desde, hasta);
        try {
            while (rs != null && rs.next()) {
                Row rowD = sheet.createRow(fila++);
                String[] vals = {
                    rs.getString("fecha"),
                    rs.getString("tipo_movimiento"),
                    String.valueOf(rs.getInt("cantidad")),
                    String.valueOf(rs.getInt("existencia_anterior")),
                    String.valueOf(rs.getInt("existencia_posterior")),
                    rs.getString("referencia") != null ? rs.getString("referencia") : "",
                    rs.getString("empleado") != null ? rs.getString("empleado") : ""
                };
                for (int i = 0; i < vals.length; i++) {
                    Cell c = rowD.createCell(i);
                    c.setCellValue(vals[i]);
                    c.setCellStyle(datosEstilo);
                }
            }
        } catch (java.sql.SQLException e) {
            System.out.println("Error generando kardex Excel: " + e.getMessage());
        }

        for (int i = 0; i < columnas.length; i++) {
            sheet.autoSizeColumn(i);
        }

        try {
            String directoryName = "Reportes de ventas";
            new java.io.File(directoryName).mkdirs();
            String fileName = "Kardex_" + idProducto + "_" + dia + "_" + mes + "_" + anio + ".xlsx";
            java.io.File file = new java.io.File(directoryName + "/" + fileName);
            java.io.FileOutputStream fileOut = new java.io.FileOutputStream(file);
            book.write(fileOut);
            fileOut.close();
            java.awt.Desktop.getDesktop().open(file);
        } catch (Exception e) {
            System.out.println("Error al guardar kardex Excel: " + e.getMessage());
        }

        try { book.close(); } catch (Exception e) { System.out.println("Error al cerrar libro"); }
    }

    private static CellStyle crearEstiloTitulo(Workbook buk){//Estilo para el titulo
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
    
    private static CellStyle crearEstiloEncabezado(Workbook buk){//Estilo para el encabezado
        CellStyle estilo = buk.createCellStyle();
        
        //Color de Fondo
        estilo.setFillForegroundColor(IndexedColors.AQUA.getIndex());
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
    
    private static CellStyle crearEstiloDatos(Workbook buk){//Estilo para los datos
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
    
    

    public static String cambiarmes(String mes) {//Método que cambia el número del mes a su nombre
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

    public static CellStyle estilodeCelda(CellStyle Estilo) {//Método que crea el estilo de las celdas
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
    
    public static CellStyle estilodeCeldaOperacion(CellStyle Estilo) {//Método que crea el estilo de las celdas
        
        Estilo.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        Estilo.setBorderBottom(BorderStyle.MEDIUM);
        Estilo.setBorderLeft(BorderStyle.THIN);
        Estilo.setBorderRight(BorderStyle.THIN);
        Estilo.setBorderBottom(BorderStyle.THIN);
        Estilo.setAlignment(HorizontalAlignment.CENTER);
        Estilo.setBorderTop(BorderStyle.THIN);
        return Estilo;
    }

    private static CellStyle crearEstiloEmpresa(Workbook book) {
        CellStyle s = book.createCellStyle();
        Font f = book.createFont();
        f.setBold(true);
        f.setFontHeightInPoints((short) 14);
        f.setFontName("Arial");
        s.setFont(f);
        s.setAlignment(HorizontalAlignment.CENTER);
        return s;
    }

    private static CellStyle crearEstiloSubtitulo(Workbook book) {
        CellStyle s = book.createCellStyle();
        Font f = book.createFont();
        f.setFontHeightInPoints((short) 10);
        f.setFontName("Arial");
        s.setFont(f);
        s.setAlignment(HorizontalAlignment.CENTER);
        return s;
    }

    private static CellStyle crearEstiloMonto(Workbook book) {
        CellStyle s = book.createCellStyle();
        Font f = book.createFont();
        f.setFontName("Arial");
        f.setFontHeightInPoints((short) 10);
        s.setFont(f);
        s.setDataFormat(book.createDataFormat().getFormat("$#,##0.00;($#,##0.00)"));
        s.setAlignment(HorizontalAlignment.RIGHT);
        return s;
    }

    private static CellStyle crearEstiloTotal(Workbook book) {
        CellStyle s = book.createCellStyle();
        Font f = book.createFont();
        f.setBold(true);
        f.setFontName("Arial");
        f.setFontHeightInPoints((short) 10);
        s.setFont(f);
        s.setBorderTop(BorderStyle.MEDIUM);
        return s;
    }

    private static CellStyle crearEstiloMontoTotal(Workbook book) {
        CellStyle s = crearEstiloMonto(book);
        Font f = book.createFont();
        f.setBold(true);
        f.setFontName("Arial");
        s.setFont(f);
        s.setBorderTop(BorderStyle.MEDIUM);
        return s;
    }

    private static CellStyle crearEstiloResultadoPositivo(Workbook book) {
        CellStyle s = crearEstiloMontoTotal(book);
        Font f = book.createFont();
        f.setBold(true);
        f.setFontName("Arial");
        f.setColor(IndexedColors.DARK_GREEN.getIndex());
        s.setFont(f);
        s.setDataFormat(book.createDataFormat().getFormat("$#,##0.00;($#,##0.00)"));
        return s;
    }

    private static CellStyle crearEstiloResultadoNegativo(Workbook book) {
        CellStyle s = crearEstiloMontoTotal(book);
        Font f = book.createFont();
        f.setBold(true);
        f.setFontName("Arial");
        f.setColor(IndexedColors.RED.getIndex());
        s.setFont(f);
        s.setDataFormat(book.createDataFormat().getFormat("$#,##0.00;($#,##0.00)"));
        return s;
    }
    
    public static String fecha(){//Método que obtiene la fecha actual
        java.time.LocalDateTime ahora = java.time.LocalDateTime.now();
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd 'de' MMMM 'del' yyyy", new java.util.Locale("es", "ES"));;
        return ahora.format(formatter);
    }
    
    private static String[] obtenerDatosEmpresa() {
        // defaults
        String nombre      = "Mi Empresa";
        String razonSocial = "";
        String rfc         = "";
        String telefono    = "";
        String correo      = "";
        String direccion   = "";
        String ciudad      = "";
        String estado      = "";
        String cp          = "";
        String logoRuta    = "";

        try {
            java.sql.ResultSet rs = conect.obtenerEmpresa();
            if (rs != null && rs.next()) {
                String v;

                v = rs.getString("nombre");
                if (v != null && !v.isEmpty()) nombre = v;

                v = rs.getString("razon_social");
                if (v != null && !v.isEmpty()) razonSocial = v;

                v = rs.getString("rfc");
                if (v != null && !v.isEmpty()) rfc = v;

                v = rs.getString("telefono");
                if (v != null && !v.isEmpty()) telefono = v;

                v = rs.getString("correo");
                if (v != null && !v.isEmpty()) correo = v;

                v = rs.getString("direccion");
                if (v != null && !v.isEmpty()) direccion = v;

                v = rs.getString("ciudad");
                if (v != null && !v.isEmpty()) ciudad = v;

                v = rs.getString("estado");
                if (v != null && !v.isEmpty()) estado = v;

                v = rs.getString("cp");
                if (v != null && !v.isEmpty()) cp = v;

                v = rs.getString("logo_ruta");
                if (v != null && !v.isEmpty()) logoRuta = v;
            }
        } catch (Exception ignored) {}

        // [0]nombre [1]razonSocial [2]rfc [3]telefono [4]correo
        // [5]direccion [6]ciudad [7]estado [8]cp [9]logoRuta
        return new String[]{nombre, razonSocial, rfc, telefono, correo,
                            direccion, ciudad, estado, cp, logoRuta};
    }

    private static boolean isImageFile(String path) {
        String lower = path.toLowerCase();
        return lower.endsWith(".png") || lower.endsWith(".jpg") || lower.endsWith(".jpeg");
    }

    private static void insertarLogoEmpresa(Workbook book, Sheet sheet, String logoRuta) {
        // Si no hay logo válido configurado, el reporte se exporta sin imagen.
        if (logoRuta == null || logoRuta.isEmpty()) return;
        File f = new File(logoRuta);
        if (!f.exists() || !isImageFile(logoRuta)) return;
        try (InputStream is = new FileInputStream(f)) {
            int pictType = logoRuta.toLowerCase().endsWith(".png")
                    ? Workbook.PICTURE_TYPE_PNG : Workbook.PICTURE_TYPE_JPEG;
            int imgIndex = book.addPicture(IOUtils.toByteArray(is), pictType);
            Drawing draw = sheet.createDrawingPatriarch();
            ClientAnchor ancho = book.getCreationHelper().createClientAnchor();
            ancho.setCol1(0);
            ancho.setRow1(1);
            Picture pict = draw.createPicture(ancho, imgIndex);
            pict.resize(1, 3);
            sheet.addMergedRegion(new CellRangeAddress(1, 3, 0, 0));
        } catch (Exception ignored) {
            // Si la imagen está corrupta o falla la lectura, se exporta sin logo.
        }
    }

    public static void obtenerUtilidad(){//Método que obtiene la utilidad del ejercicio
        LocalDate fecha = LocalDate.now();
        String dia = String.valueOf(fecha.getDayOfMonth());
        String mes = String.valueOf(fecha.getMonthValue());
        String anio = String.valueOf(fecha.getYear());
        String fileName = "/estadodeResultados" + dia + "_" + mes + "_" + anio;
        String filePath;
       
        try {
            filePath = "Estados de resultados" + fileName + ".xlsx";
            FileInputStream fis = new FileInputStream(filePath);
        
            
            Workbook workbook = new XSSFWorkbook(fis);

            Sheet sheet = workbook.getSheetAt(0); // Obtén la primera hoja
            Cell cell = sheet.getRow(21).getCell(5); // Obtén la celda F21 

            FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
            CellValue cellValue = evaluator.evaluate(cell);

            utilidadDelEjercicio = cellValue.getNumberValue(); // Guarda el valor en una variable

            System.out.println("Utilidad del ejercicio: " + utilidadDelEjercicio);

        } catch (IOException e) {
            GestorErrores.registrar(e);
        }

    }
}
