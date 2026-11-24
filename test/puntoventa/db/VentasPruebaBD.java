package puntoventa.db;

import static pruebas.Prueba.*;
import static pruebas.BD.*;

import puntoventa.report.TicketData;

/** Casos CU-VEN del plan docs/pruebas/PLAN_DE_PRUEBAS.md (carrito, cobro, descuentos, ticket). */
public class VentasPruebaBD {
    public static void main(String[] a) {
        VentaDAO v = new VentaDAO();
        v.limpiarVentaTemp();

        // CU-VEN-01 agregar al carrito a menudeo: descuenta stock y fija precio menudeo
        String p1 = producto("VEN", 50, 20.0, 15.0);
        v.insertarVentaTemp(new String[]{p1, "3"}, true);
        check(stock(p1) == 47, "CU-VEN-01 stock tras agregar 3: " + stock(p1));
        check(num("SELECT precio_dado FROM venta_temp WHERE id_producto=?", p1) == 20.0, "CU-VEN-01 precio menudeo");
        check("Menudeo".equals(texto("SELECT tipo_venta::text FROM venta_temp WHERE id_producto=?", p1)), "CU-VEN-01 tipo Menudeo");
        check(v.ventaEnCurso(), "CU-VEN-01 ventaEnCurso() = true con productos");

        // CU-VEN-02 acumular: mismo producto otra vez suma cantidades y pasa a mayoreo al llegar a 12
        v.insertarVentaTemp(new String[]{p1, "9"}, true);
        check(entero("SELECT cantidad_prod FROM venta_temp WHERE id_producto=?", p1) == 12, "CU-VEN-02 acumula 3+9=12");
        check(stock(p1) == 38, "CU-VEN-02 stock 50-12=38: " + stock(p1));
        check("Mayoreo".equals(texto("SELECT tipo_venta::text FROM venta_temp WHERE id_producto=?", p1)), "CU-VEN-02 12 piezas -> Mayoreo");
        check(num("SELECT precio_total FROM venta_temp WHERE id_producto=?", p1) == 180.0, "CU-VEN-02 total 12*15=180");

        // CU-VEN-03 actualizar cantidad (sin acumular) devuelve la diferencia al stock
        v.insertarVentaTemp(new String[]{p1, "2"}, false);
        check(entero("SELECT cantidad_prod FROM venta_temp WHERE id_producto=?", p1) == 2, "CU-VEN-03 reemplaza a 2");
        check(stock(p1) == 48, "CU-VEN-03 stock 48: " + stock(p1));

        // CU-VEN-04 stock insuficiente: error y no cambia nada
        String p2 = producto("VEN", 5, 10.0, 8.0);
        String err = error(() -> v.insertarVentaTemp(new String[]{p2, "6"}, true));
        check(err != null && err.contains("No hay suficiente cantidad"), "CU-VEN-04 mensaje stock insuficiente: " + err);
        check(stock(p2) == 5, "CU-VEN-04 stock intacto");
        check(entero("SELECT count(*) FROM venta_temp WHERE id_producto=?", p2) == 0, "CU-VEN-04 no entra al carrito");

        // CU-VEN-05 producto inexistente
        err = error(() -> v.insertarVentaTemp(new String[]{"NOEXISTE999", "1"}, true));
        check(err != null && err.contains("no existe"), "CU-VEN-05 producto inexistente: " + err);

        // CU-VEN-06 producto dado de baja no debe poder venderse
        String pBaja = producto("VEN", 3, 10.0, 8.0);
        exec("UPDATE producto SET estatus='Inactivo' WHERE id_producto=?", pBaja);
        err = error(() -> v.insertarVentaTemp(new String[]{pBaja, "1"}, true));
        check(err != null, "CU-VEN-06 vender producto INACTIVO debería rechazarse (se aceptó, stock=" + stock(pBaja) + ")");
        v.eliminarVentaTemp(pBaja);

        // CU-VEN-07 quitar renglón regresa el stock
        v.insertarVentaTemp(new String[]{p2, "4"}, true);
        v.eliminarVentaTemp(p2);
        check(stock(p2) == 5, "CU-VEN-07 eliminar renglón regresa stock: " + stock(p2));

        // CU-VEN-08 vaciar carrito regresa todo el stock
        String p3 = producto("VEN", 10, 5.0, 4.0);
        v.insertarVentaTemp(new String[]{p3, "4"}, true);
        v.limpiarVentaTemp();
        check(stock(p3) == 10 && stock(p1) == 50, "CU-VEN-08 vaciar regresa stock p3=" + stock(p3) + " p1=" + stock(p1));
        check(!v.ventaEnCurso(), "CU-VEN-08 carrito vacío");

        // CU-VEN-09 descuento por renglón y tope max_descuento
        exec("UPDATE producto SET max_descuento=10 WHERE id_producto=?", p3);
        v.insertarVentaTemp(new String[]{p3, "2"}, true);   // 2 x 5 = 10
        v.insertarVentaTemp(new String[]{p1, "1"}, true);   // 1 x 20
        check(v.obtenerMinMaxDescuentoTemp() == 10.0, "CU-VEN-09 menor max_descuento del carrito = 10: " + v.obtenerMinMaxDescuentoTemp());
        check(new ProductoDAO().obtenerMaxDescuento(p3) == 10.0, "CU-VEN-09 obtenerMaxDescuento");
        v.actualizarDescuentoTemp(p3, 10);
        check(num("SELECT precio_total FROM venta_temp WHERE id_producto=?", p3) == 9.0, "CU-VEN-09 10% sobre 10 = 9");
        check(Math.abs(v.sumaVentaTemp() - 29.0) < 0.001, "CU-VEN-09 suma carrito 9+20=29: " + v.sumaVentaTemp());
        check(stock(p3) == 8, "CU-VEN-09 aplicar descuento no mueve stock: " + stock(p3));

        // CU-VEN-10 cobrar con forma de pago: venta, detalle, carrito vacío, kardex referenciado
        String idVenta = v.registrarVentaConFormaPago(ADMIN, CLIENTE_GENERICO, "Tarjeta");
        check(idVenta != null && idVenta.startsWith("V"), "CU-VEN-10 id de venta: " + idVenta);
        check(num("SELECT total_venta FROM venta WHERE id_venta=?", idVenta) == 29.0, "CU-VEN-10 total con descuento 29");
        check("Tarjeta".equals(texto("SELECT forma_pago FROM venta WHERE id_venta=?", idVenta)), "CU-VEN-10 forma de pago");
        check(entero("SELECT count(*) FROM venta_detalle WHERE id_venta=?", idVenta) == 2, "CU-VEN-10 2 renglones");
        check(!v.ventaEnCurso(), "CU-VEN-10 carrito vacío tras cobrar");
        check(entero("SELECT count(*) FROM kardex WHERE referencia=? AND tipo_movimiento='Venta'", idVenta) == 2,
                "CU-VEN-10 kardex de la venta con referencia: " + entero("SELECT count(*) FROM kardex WHERE referencia=?", idVenta));
        v.actualizarDescuentoVenta(idVenta, p3, 10);
        check(num("SELECT descuento_pct FROM venta_detalle WHERE id_venta=? AND id_producto=?", idVenta, p3) == 10.0, "CU-VEN-10 descuento persistido");
        check(descuadreKardex(p3, 10) == null, "CU-VEN-10 kardex p3 cuadra: " + descuadreKardex(p3, 10));
        check(descuadreKardex(p1, 50) == null, "CU-VEN-10 kardex p1 cuadra: " + descuadreKardex(p1, 50));

        // CU-VEN-11 datos del ticket
        TicketData t = v.obtenerDatosVenta(idVenta);
        check(t.productos.size() == 2 && t.total == 29.0, "CU-VEN-11 ticket 2 productos total 29: " + t.productos.size() + " / " + t.total);
        check("Tarjeta".equals(t.formaPago) && "Cliente Genérico".equals(t.clienteNombre), "CU-VEN-11 ticket forma de pago y cliente");
        check(!t.empresaNombre.isEmpty(), "CU-VEN-11 ticket con nombre de empresa");

        // CU-VEN-12 consultas de ventas
        check(cuenta(v.buscarVentasPorId(idVenta)) == 1, "CU-VEN-12 buscar venta por id");
        check(cuenta(v.obtenerDetalleVenta(idVenta)) == 2, "CU-VEN-12 detalle de venta");

        // CU-VEN-13 cobrar con carrito vacío (la UI lo impide; la base no debe dejar una venta en NULL)
        int antes = entero("SELECT count(*) FROM venta");
        String errVacia = error(() -> v.registrarVenta(ADMIN, CLIENTE_GENERICO));
        check(errVacia != null && errVacia.contains("sin productos"), "CU-VEN-13 cobrar sin productos se rechaza: " + errVacia);
        check(entero("SELECT count(*) FROM venta") == antes, "CU-VEN-13 no se crea venta vacía");

        // CU-VEN-14 lista de precios: precio fijo en lista sobrescribe el del carrito
        ListaPreciosDAO lp = new ListaPreciosDAO();
        String p4 = producto("VEN", 20, 30.0, 25.0);
        check(lp.obtenerPrecioEnLista(p4, 1) == 30.0 && lp.obtenerPrecioEnLista(p4, 2) == 25.0, "CU-VEN-14 listas de fábrica usan precios base");
        exec("INSERT INTO lista_precios(nombre, descripcion) VALUES('Distribuidor','prueba') ON CONFLICT DO NOTHING");
        int idDist = entero("SELECT id_lista FROM lista_precios WHERE nombre='Distribuidor'");
        check(lp.obtenerPrecioEnLista(p4, idDist) == -1.0, "CU-VEN-14 sin precio en lista nueva -> -1");
        lp.insertarPrecioEnLista(p4, idDist, 22.5);
        check(lp.obtenerPrecioEnLista(p4, idDist) == 22.5, "CU-VEN-14 precio en lista");
        v.insertarVentaTemp(new String[]{p4, "2"}, true);
        lp.actualizarPrecioListaTemp(p4, 22.5, "Distribuidor");
        check(num("SELECT precio_total FROM venta_temp WHERE id_producto=?", p4) == 45.0, "CU-VEN-14 carrito con precio de lista 2*22.5");
        v.actualizarDescuentoTemp(p4, 10);
        check(num("SELECT precio_total FROM venta_temp WHERE id_producto=?", p4) == 40.5, "CU-VEN-14 descuento sobre el precio de lista: "
                + num("SELECT precio_total FROM venta_temp WHERE id_producto=?", p4));
        v.insertarVentaTemp(new String[]{p4, "3"}, false);
        check(num("SELECT precio_dado FROM venta_temp WHERE id_producto=?", p4) == 22.5, "CU-VEN-14 cambiar cantidad conserva el precio de lista");
        lp.actualizarPrecioListaTemp(p4, 25.0, "Mayoreo");
        check(num("SELECT precio_dado FROM venta_temp WHERE id_producto=?", p4) == 25.0
                && "Mayoreo".equals(texto("SELECT tipo_venta::text FROM venta_temp WHERE id_producto=?", p4)), "CU-VEN-14 lista Mayoreo con 3 piezas");
        lp.actualizarPrecioListaTemp(p4, 30.0, "Menudeo");
        check(num("SELECT precio_dado FROM venta_temp WHERE id_producto=?", p4) == 30.0
                && "Menudeo".equals(texto("SELECT tipo_venta::text FROM venta_temp WHERE id_producto=?", p4)), "CU-VEN-14 lista Menudeo vuelve al precio automático");
        v.insertarVentaTemp(new String[]{p4, "12"}, false);
        check(num("SELECT precio_dado FROM venta_temp WHERE id_producto=?", p4) == 25.0, "CU-VEN-14 en automático, 12 piezas pasan a mayoreo");
        v.insertarVentaTemp(new String[]{p4, "2"}, false);
        lp.eliminarPrecioEnLista(p4, idDist);
        check(lp.obtenerPrecioEnLista(p4, idDist) == -1.0, "CU-VEN-14 quitar precio de lista");
        v.limpiarVentaTemp();
        check(stock(p4) == 20, "CU-VEN-14 vaciar regresa stock");

        terminar("VentasPruebaBD");
    }

    static int cuenta(java.sql.ResultSet rs) {
        int n = 0;
        try { while (rs != null && rs.next()) n++; } catch (Exception e) { return -1; }
        return n;
    }
}
