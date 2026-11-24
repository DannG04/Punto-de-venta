package puntoventa.db;

import static pruebas.Prueba.*;
import static pruebas.BD.*;

import java.io.File;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.List;

/** Casos CU-REP: reporte diario, corte, kardex, balance general, estado de resultados y archivos Excel. */
public class ReportesPruebaBD {
    static double[] reporte(LocalDate f) {
        try {
            ResultSet rs = new ReporteDAO().reporte_diario(f);
            rs.next();
            double[] r = new double[10];
            for (int i = 0; i < 10; i++) r[i] = rs.getDouble(i + 1);
            return r;
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    public static void main(String[] a) throws Exception {
        LocalDate hoy = LocalDate.now();
        ReporteDAO rep = new ReporteDAO();
        double[] r0 = reporte(hoy);
        double caja0 = num("SELECT total_caja(date_trunc('year', CURRENT_DATE)::date, CURRENT_DATE)");
        double ventasAnio0 = rep.ObtenerDato("total_ventas");

        // Movimientos del día: venta 100, devolución 20, compra 50 (sin IVA), gasto 30, otra ganancia 10
        VentaDAO v = new VentaDAO();
        v.limpiarVentaTemp();
        String p = producto("REP", 40, 10.0, 9.0);
        v.insertarVentaTemp(new String[]{p, "10"}, true);   // 10 x 9 (mayoreo a partir de 12: aquí menudeo) = 100
        String venta = v.registrarVentaConFormaPago(ADMIN, CLIENTE_GENERICO, "Efectivo");
        DevolucionDAO d = new DevolucionDAO();
        String dev = d.insertarDevolucion(new String[]{venta, "0"});
        d.insertarProdDevolucion(new String[]{dev, venta, p, "2"}, false);
        RenglonCompra r = ComprasCotizacionesPruebaBD.renglon(p, false, "5", "10", "1");
        r.llevaIva = false;
        new CompraDAO().guardarFacturaCompleta(new String[]{ADMIN, "Reporte", "", "", ""}, 0, List.of(r), 50, 0, 50, 0);
        new GastoDAO().insertarGasto(new String[]{ADMIN, "Gasto reporte", "30"});
        new GananciaDAO().insertarOtraGanancia(new String[]{"Ganancia reporte", "10"});

        // CU-REP-01 reporte diario: cada total cambia exactamente por el movimiento del día
        double[] r1 = reporte(hoy);
        String[] nom = {"total_ventas", "ventas_netas", "total_devoluciones", "num_transacciones", "ticket_promedio",
                "total_compras", "total_gastos", "total_otras_ganancias", "resultado_neto"};
        double[] esperado = {100, 80, 20, 1, Double.NaN, 50, 30, 10, 10};
        for (int i = 0; i < esperado.length; i++) {
            if (Double.isNaN(esperado[i])) continue;
            check(Math.abs((r1[i] - r0[i]) - esperado[i]) < 0.001, "CU-REP-01 " + nom[i] + " cambió " + (r1[i] - r0[i]) + ", se esperaba " + esperado[i]);
        }

        // CU-REP-02 corte diario lista la venta con su forma de pago y empleado
        ResultSet corte = rep.corteDiario(hoy);
        boolean enCorte = false;
        while (corte.next()) if (venta.equals(corte.getString("id_venta")))
            enCorte = corte.getDouble("total_venta") == 100.0 && "Efectivo".equals(corte.getString("forma_pago")) && "Administrador".equals(corte.getString("empleado"));
        check(enCorte, "CU-REP-02 la venta aparece en el corte del día");
        check(VentasPruebaBD.cuenta(rep.totalesDia(hoy)) == 1, "CU-REP-02 totales del día");

        // CU-REP-03 kardex del producto: venta, devolución y compra con sus referencias
        KardexDAO kardex = new KardexDAO();
        StringBuilder tipos = new StringBuilder();
        for (KardexPeriodo.Movimiento m : kardex.consultar(p, hoy, hoy).movimientos)
            tipos.append(m.tipo).append(m.cantidad).append(' ');
        String t = tipos.toString();
        check(t.contains("Venta-10") && t.contains("Devolucion2") && t.contains("Compra5"), "CU-REP-03 movimientos del kardex: " + t);
        check(descuadreKardex(p, 40) == null, "CU-REP-03 kardex cuadra: " + descuadreKardex(p, 40));
        check(kardex.consultar(p, hoy.minusDays(10), hoy.minusDays(1)).movimientos.isEmpty(), "CU-REP-03 filtro de fechas");
        check(VentasPruebaBD.cuenta(kardex.buscarProductos(p, false)) == 1, "CU-REP-03 búsqueda de producto");

        // CU-REP-04 balance general: caja = ventas + otras ganancias - compras - devoluciones - gastos
        double caja1 = num("SELECT total_caja(date_trunc('year', CURRENT_DATE)::date, CURRENT_DATE)");
        check(Math.abs((caja1 - caja0) - 10) < 0.001, "CU-REP-04 caja cambió " + (caja1 - caja0) + ", se esperaba 100+10-50-20-30=10");
        ResultSet bal = rep.balanceGeneral(hoy);
        check(bal != null && bal.next(), "CU-REP-04 balance_general devuelve una fila");

        // CU-REP-05 estado de resultados (acumulado del año)
        check(Math.abs(rep.ObtenerDato("total_ventas") - ventasAnio0 - 100) < 0.001, "CU-REP-05 ventas del año +100");

        // CU-REP-06 el valor del inventario debe ser el mismo en el reporte diario y en el balance
        double invReporte = reporte(hoy)[9];
        double invBalance = num("SELECT valor_inventario_actual()");
        check(Math.abs(invReporte - invBalance) < 0.01, "CU-REP-06 valor de inventario: reporte diario " + invReporte + " vs balance " + invBalance);

        // CU-REP-07 archivos Excel
        File trabajo = new File(".").getCanonicalFile();
        System.setProperty("user.home", trabajo.getPath());
        try { puntoventa.report.Excel.reporteDiario("ReporteDiario", hoy); } catch (Exception e) { }
        check(existeXlsx(new File(trabajo, "Documents/Reportes de ventas")), "CU-REP-07 Excel del reporte diario");
        try { puntoventa.report.Excel.balanceGeneral(0.0); } catch (Exception e) { }
        check(existeXlsx(new File(trabajo, "BalancesGenerales")), "CU-REP-07 Excel del balance general");
        try { puntoventa.report.Excel.estadodeResultados(0.0); } catch (Exception e) { }
        try { KardexPeriodo kp = kardex.consultar(p, hoy, hoy); puntoventa.report.Excel.reporteKardex("Prueba", kp, kp.movimientos, null); } catch (Exception e) { }
        File[] carpetas = trabajo.listFiles(File::isDirectory);
        int xlsx = 0;
        for (File c : carpetas) { File[] fs = c.listFiles((x, n) -> n.endsWith(".xlsx")); if (fs != null) xlsx += fs.length; }
        check(xlsx >= 3, "CU-REP-07 estado de resultados y kardex generan Excel (archivos en carpetas: " + xlsx + ")");

        terminar("ReportesPruebaBD");
    }

    static boolean existeXlsx(File dir) {
        File[] fs = dir.listFiles((x, n) -> n.endsWith(".xlsx"));
        return fs != null && fs.length > 0;
    }
}
