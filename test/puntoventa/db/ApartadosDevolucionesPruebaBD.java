package puntoventa.db;

import static pruebas.Prueba.*;
import static pruebas.BD.*;

import puntoventa.report.TicketData;
import puntoventa.report.TipoTicket;

/** Casos CU-APA (apartados) y CU-DEV (devoluciones) del plan de pruebas. */
public class ApartadosDevolucionesPruebaBD {
    public static void main(String[] a) {
        apartados();
        devoluciones();
        terminar("ApartadosDevolucionesPruebaBD");
    }

    static void apartados() {
        ApartadoDAO ap = new ApartadoDAO();
        String cli = "CAPM040417MOCSCYA8";

        // CU-APA-01 crear apartado: vigente, 30 días, total 0
        String id = ap.insertarApartado(new String[]{ADMIN, cli});
        check(id != null && id.startsWith("A"), "CU-APA-01 id apartado: " + id);
        check("Vigente".equals(texto("SELECT estado::text FROM apartado WHERE id_apartado=?", id)), "CU-APA-01 estado Vigente");
        check(entero("SELECT fecha_limite - fecha_inicio FROM apartado WHERE id_apartado=?", id) == 30, "CU-APA-01 vigencia 30 días");

        // CU-APA-02 agregar productos: descuenta stock, calcula total
        String p1 = producto("APA", 20, 100.0, 80.0);
        String p2 = producto("APA", 5, 50.0, 40.0);
        ap.insertarProdApartado(new String[]{id, p1, "2"}, true);
        ap.insertarProdApartado(new String[]{id, p2, "1"}, true);
        check(stock(p1) == 18 && stock(p2) == 4, "CU-APA-02 stock apartado p1=" + stock(p1) + " p2=" + stock(p2));
        check(num("SELECT cantidad_total FROM apartado WHERE id_apartado=?", id) == 250.0, "CU-APA-02 total 2*100+50=250");

        // CU-APA-03 acumular el mismo producto
        ap.insertarProdApartado(new String[]{id, p1, "1"}, true);
        check(stock(p1) == 17 && num("SELECT cantidad_total FROM apartado WHERE id_apartado=?", id) == 350.0,
                "CU-APA-03 acumula a 3 piezas: stock=" + stock(p1) + " total=" + num("SELECT cantidad_total FROM apartado WHERE id_apartado=?", id));

        // CU-APA-04 sin stock suficiente
        String err = error(() -> ap.insertarProdApartado(new String[]{id, p2, "10"}, true));
        check(err != null && err.contains("No hay suficiente"), "CU-APA-04 stock insuficiente: " + err);
        check(stock(p2) == 4, "CU-APA-04 stock intacto: " + stock(p2));

        // CU-APA-04b no se puede apartar un producto dado de baja
        String pBaja = producto("APA", 5, 10.0, 8.0);
        exec("UPDATE producto SET estatus='Inactivo' WHERE id_producto=?", pBaja);
        err = error(() -> ap.insertarProdApartado(new String[]{id, pBaja, "1"}, true));
        check(err != null && err.contains("dado de baja") && stock(pBaja) == 5, "CU-APA-04b apartar producto dado de baja: " + err);

        // CU-APA-05 abono inicial y abono >= total rechazado
        check(ap.actualizarApartado(id, 100.0), "CU-APA-05 abono 100");
        check(num("SELECT cantidad_faltante FROM apartado WHERE id_apartado=?", id) == 250.0, "CU-APA-05 faltante 250");
        err = error(() -> ap.actualizarApartado(id, 350.0));
        check(err != null && err.contains("registrelo como una venta"), "CU-APA-05 pagar el total completo se rechaza: " + err);

        // CU-APA-06 quitar un producto: regresa stock y recalcula total/faltante
        ap.eliminarProdApartado(new String[]{id, p2});
        check(stock(p2) == 5, "CU-APA-06 stock regresa: " + stock(p2));
        check(num("SELECT cantidad_total FROM apartado WHERE id_apartado=?", id) == 300.0
                && num("SELECT cantidad_faltante FROM apartado WHERE id_apartado=?", id) == 200.0, "CU-APA-06 total 300 faltante 200");

        // CU-APA-07 no se puede eliminar un apartado con productos; uno vacío sí
        err = error(() -> ap.eliminarApartado(id));
        check(err != null && err.contains("tiene productos"), "CU-APA-07 eliminar con productos: " + err);
        String vacio = ap.insertarApartado(new String[]{ADMIN, cli});
        ap.eliminarApartado(vacio);
        check(entero("SELECT count(*) FROM apartado WHERE id_apartado=?", vacio) == 0, "CU-APA-07 eliminar apartado vacío");

        // CU-APA-08 ticket del apartado (la cantidad debe ser la apartada, no el stock)
        TicketData t = ap.obtenerDatosApartado(id, TipoTicket.APARTADO_NUEVO);
        check(t.productos.size() == 1 && "3".equals(t.productos.get(0)[2]), "CU-APA-08 ticket muestra 3 piezas apartadas: "
                + (t.productos.isEmpty() ? "-" : t.productos.get(0)[2]));
        check(t.cantidadDada == 100.0 && t.cantidadFaltante == 200.0, "CU-APA-08 ticket abonado/faltante");

        // CU-APA-09 entregar (liquidar): genera venta y queda Entregado; ya no se puede cancelar
        int ventasAntes = entero("SELECT count(*) FROM venta");
        ap.entregarApartado(id);
        check("Entregado".equals(texto("SELECT estado::text FROM apartado WHERE id_apartado=?", id)), "CU-APA-09 Entregado");
        check(entero("SELECT count(*) FROM venta") == ventasAntes + 1, "CU-APA-09 se registró la venta");
        check(num("SELECT total_venta FROM venta WHERE id_cliente=? ORDER BY id_venta DESC LIMIT 1", cli) == 300.0, "CU-APA-09 venta por 300");
        check(stock(p1) == 17, "CU-APA-09 entregar no vuelve a descontar stock: " + stock(p1));
        err = error(() -> ap.cancelarApartado(id));
        check(err != null && err.contains("vigentes"), "CU-APA-09 cancelar un entregado se rechaza: " + err);

        // CU-APA-10 cancelar: regresa stock, registra otra ganancia proporcional a los días
        String id2 = ap.insertarApartado(new String[]{ADMIN, cli});
        String p3 = producto("APA", 10, 60.0, 50.0);
        ap.insertarProdApartado(new String[]{id2, p3, "2"}, true);
        ap.actualizarApartado(id2, 30.0);
        exec("UPDATE apartado SET fecha_inicio=CURRENT_DATE-15, fecha_limite=CURRENT_DATE+15 WHERE id_apartado=?", id2);
        check(Math.abs(ap.cantidadCancelarApartado(id2) - 15.0) < 0.01, "CU-APA-10 retención 15/30 de 30 = 15: " + ap.cantidadCancelarApartado(id2));
        ap.cancelarApartado(id2);
        check("Cancelado".equals(texto("SELECT estado::text FROM apartado WHERE id_apartado=?", id2)), "CU-APA-10 Cancelado");
        check(stock(p3) == 10, "CU-APA-10 stock regresa: " + stock(p3));
        check(num("SELECT monto FROM otras_ganancias WHERE descripcion=?", "Cancelación del apartado: " + id2) == 15.0, "CU-APA-10 otra ganancia 15");
        check(descuadreKardex(p3, 10) == null, "CU-APA-10 kardex cuadra: " + descuadreKardex(p3, 10));
        check(entero("SELECT count(*) FROM kardex WHERE id_producto=? AND tipo_movimiento='Venta'", p3) == 0,
                "CU-APA-10 apartar se registra en kardex como 'Venta' (tipos: "
                + texto("SELECT string_agg(tipo_movimiento||'/'||coalesce(referencia,'null'), ', ') FROM kardex WHERE id_producto=?", p3) + ")");

        // CU-APA-11 revisión de vencidos: anula, regresa stock, el abono pasa a otras ganancias
        String id4 = ap.insertarApartado(new String[]{ADMIN, cli});
        String p4 = producto("APA", 8, 40.0, 30.0);
        ap.insertarProdApartado(new String[]{id4, p4, "3"}, true);
        ap.actualizarApartado(id4, 20.0);
        exec("UPDATE apartado SET fecha_inicio=CURRENT_DATE-40, fecha_limite=CURRENT_DATE-10 WHERE id_apartado=?", id4);
        ap.revisarApartado();
        check("Anulado".equals(texto("SELECT estado::text FROM apartado WHERE id_apartado=?", id4)), "CU-APA-11 Anulado");
        check(stock(p4) == 8, "CU-APA-11 stock regresa: " + stock(p4));
        check(num("SELECT monto FROM otras_ganancias WHERE descripcion=?", "Anulación del apartado: " + id4) == 20.0, "CU-APA-11 abono a otras ganancias");
        check("Vigente".equals(texto("SELECT estado::text FROM apartado WHERE id_apartado=?", id2)) == false, "CU-APA-11 no toca cancelados");

        // CU-APA-12 búsqueda
        check(VentasPruebaBD.cuenta(ap.buscarApartados(id4)) == 1, "CU-APA-12 buscar por id");
    }

    static void devoluciones() {
        VentaDAO v = new VentaDAO();
        DevolucionDAO d = new DevolucionDAO();
        v.limpiarVentaTemp();
        String p1 = producto("DEV", 30, 10.0, 8.0);
        String p2 = producto("DEV", 30, 25.0, 20.0);
        v.insertarVentaTemp(new String[]{p1, "5"}, true);
        v.insertarVentaTemp(new String[]{p2, "2"}, true);
        String venta = v.registrarVentaConFormaPago(ADMIN, CLIENTE_GENERICO, "Efectivo");   // 50 + 50 = 100

        // CU-DEV-01 registrar devolución
        String dev = d.insertarDevolucion(new String[]{venta, "0"});
        check(dev != null && dev.startsWith("D"), "CU-DEV-01 id devolución: " + dev);

        // CU-DEV-02 'Otro motivo' regresa el producto al inventario y suma el monto
        d.insertarProdDevolucion(new String[]{dev, venta, p1, "2"}, false);
        check(stock(p1) == 27, "CU-DEV-02 stock 25+2=27: " + stock(p1));
        check(num("SELECT monto FROM devolucion_ventas WHERE id_devolucion=?", dev) == 20.0, "CU-DEV-02 monto 20");
        check(entero("SELECT count(*) FROM kardex WHERE id_producto=? AND tipo_movimiento='Devolucion' AND referencia=?", p1, dev) == 1, "CU-DEV-02 kardex Devolucion");

        // CU-DEV-03 'mal estado' no regresa al inventario pero sí al monto
        d.insertarProdDevolucion(new String[]{dev, venta, p2, "1"}, true);
        check(stock(p2) == 28, "CU-DEV-03 producto en mal estado no regresa: " + stock(p2));
        check(num("SELECT monto FROM devolucion_ventas WHERE id_devolucion=?", dev) == 45.0, "CU-DEV-03 monto 20+25=45");

        // CU-DEV-04 cantidad mayor a la vendida / producto que no está en la venta
        String err = error(() -> d.insertarProdDevolucion(new String[]{dev, venta, p1, "9"}, false));
        check(err != null && err.contains("cantidad mayor"), "CU-DEV-04 cantidad mayor: " + err);
        String otro = producto("DEV", 3, 1.0, 1.0);
        err = error(() -> d.insertarProdDevolucion(new String[]{dev, venta, otro, "1"}, false));
        check(err != null && err.contains("no esta dentro"), "CU-DEV-04 producto ajeno a la venta: " + err);

        // CU-DEV-05 actualizar cantidad devuelta (reemplaza, no duplica stock)
        d.insertarProdDevolucion(new String[]{dev, venta, p1, "3"}, false);
        check(stock(p1) == 28, "CU-DEV-05 stock 25+3=28: " + stock(p1));
        check(num("SELECT monto FROM devolucion_ventas WHERE id_devolucion=?", dev) == 55.0, "CU-DEV-05 monto 30+25=55");

        // CU-DEV-06 segunda devolución sobre la misma venta se rechaza
        err = error(() -> d.insertarDevolucion(new String[]{venta, "0"}));
        check(err != null && err.contains("misma venta"), "CU-DEV-06 segunda devolución: " + err);

        // CU-DEV-07 quitar un producto de la devolución: stock y monto deben regresar
        d.eliminarProdDevolucion(new String[]{dev, p1});
        check(stock(p1) == 25, "CU-DEV-07 stock vuelve a 25: " + stock(p1));
        check(num("SELECT monto FROM devolucion_ventas WHERE id_devolucion=?", dev) == 25.0,
                "CU-DEV-07 monto debe bajar a 25 al quitar p1, quedó " + num("SELECT monto FROM devolucion_ventas WHERE id_devolucion=?", dev));
        check(descuadreKardex(p1, 30) == null, "CU-DEV-07 kardex cuadra: " + descuadreKardex(p1, 30));

        // CU-DEV-08 no se elimina una devolución con productos; vacía sí
        err = error(() -> d.eliminarDevolucion(dev));
        check(err != null && err.contains("con registros"), "CU-DEV-08 eliminar con productos: " + err);
        d.eliminarProdDevolucion(new String[]{dev, p2});
        d.eliminarDevolucion(dev);
        check(entero("SELECT count(*) FROM devolucion_ventas WHERE id_devolucion=?", dev) == 0, "CU-DEV-08 eliminada");

        // CU-DEV-09 devolución sobre una venta inexistente
        err = error(() -> d.insertarDevolucion(new String[]{"V00000000000000", "0"}));
        check(err != null, "CU-DEV-09 venta inexistente debe rechazarse");
    }
}
