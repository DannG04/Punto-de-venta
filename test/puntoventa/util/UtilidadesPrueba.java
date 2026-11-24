package puntoventa.util;

import static pruebas.Prueba.*;

import java.sql.SQLException;
import puntoventa.report.GeneradorCodigoBarras;
import puntoventa.report.TicketBuilder;
import puntoventa.report.TicketData;
import puntoventa.report.TipoTicket;

/** Casos CU-UTL: mensajes de error, códigos de barras y armado del ticket (no usan la base). */
public class UtilidadesPrueba {
    public static void main(String[] a) {
        // CU-UTL-01 mensajes amigables por SQLState
        check(GestorErrores.mensajeAmigable(new SQLException("x", "23505")).equals("Ya existe un registro con esos datos."), "CU-UTL-01 23505");
        check(GestorErrores.mensajeAmigable(new SQLException("x", "08001")).startsWith("No hay conexión"), "CU-UTL-01 sin conexión");
        check(GestorErrores.mensajeAmigable(new SQLException("No hay suficiente cantidad de productos", "P0001"))
                .equals("No hay suficiente cantidad de productos"), "CU-UTL-01 regla de negocio P0001 se muestra tal cual");
        check(GestorErrores.mensajeAmigable(new RuntimeException(new SQLException("x", "23503"))).startsWith("No se puede completar"),
                "CU-UTL-01 SQLException envuelta");
        check(GestorErrores.mensajeAmigable(new RuntimeException("x")).startsWith("Ocurrió un error"), "CU-UTL-01 genérico");

        // CU-UTL-02 códigos de barras
        check(GeneradorCodigoBarras.generarEAN13("750123456789").equals("7501234567893"), "CU-UTL-02 dígito verificador EAN-13");
        check(GeneradorCodigoBarras.generarEAN13("123").length() == 13, "CU-UTL-02 rellena a 12 + verificador");
        check(GeneradorCodigoBarras.esCodigoNumerico("0123") && !GeneradorCodigoBarras.esCodigoNumerico("12A"), "CU-UTL-02 numérico");
        check(GeneradorCodigoBarras.generarCodigoAutomatico().matches("PV\\d{10}"), "CU-UTL-02 código automático PV + 10 dígitos");
        check(GeneradorCodigoBarras.generarCODE128("ABC-123", 200, 60) != null, "CU-UTL-02 imagen CODE-128");
        check(GeneradorCodigoBarras.generarQR("hola", 100) != null, "CU-UTL-02 imagen QR");

        // CU-UTL-03 ticket: datos, totales y escape de HTML en textos capturados por el usuario
        TicketData d = new TicketData();
        d.tipo = TipoTicket.VENTA;
        d.empresaNombre = "Ferretería <López> & Hijos";
        d.idTransaccion = "V20261123120000";
        d.productos.add(new String[]{"1", "Clavo 2\"", "3", "$1.00", "$3.00"});
        d.subtotal = 3; d.total = 3; d.formaPago = "Efectivo"; d.recibido = 5; d.cambio = 2;
        String html = TicketBuilder.construir(d);
        check(html.contains("V20261123120000") && html.contains("COMPROBANTE DE VENTA"), "CU-UTL-03 encabezado de venta");
        check(!html.contains("<López>") && html.contains("&lt;López&gt;"), "CU-UTL-03 nombre de empresa escapado");
        check(html.contains("Efectivo"), "CU-UTL-03 forma de pago");
        d.tipo = TipoTicket.APARTADO_NUEVO; d.cantidadDada = 1; d.cantidadFaltante = 2; d.fechaLimite = "2026-12-23";
        check(TicketBuilder.construir(d).contains("2026-12-23"), "CU-UTL-03 ticket de apartado con fecha límite");

        terminar("UtilidadesPrueba");
    }
}
