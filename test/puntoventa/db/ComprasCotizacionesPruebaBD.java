package puntoventa.db;

import static pruebas.Prueba.*;
import static pruebas.BD.*;

import java.util.List;

/** Casos CU-COM (compras/facturas/borradores) y CU-COT (cotizaciones) del plan de pruebas. */
public class ComprasCotizacionesPruebaBD {
    public static void main(String[] a) {
        compras();
        cotizaciones();
        terminar("ComprasCotizacionesPruebaBD");
    }

    static RenglonCompra renglon(String id, boolean nuevo, String cant, String precio, String factor) {
        RenglonCompra r = new RenglonCompra();
        r.idProducto = id; r.nuevo = nuevo; r.nombre = "Compra " + id; r.cantidad = cant; r.precioCompra = precio;
        r.factor = factor; r.unidadCompra = "Caja"; r.unidadVenta = "Pieza"; r.precioMenudeo = "20"; r.precioMayoreo = "15";
        r.maxDescuento = 10; r.codigoBarras = ""; r.llevaIva = true;
        return r;
    }

    static void compras() {
        CompraDAO c = new CompraDAO();
        ProveedorDAO prov = new ProveedorDAO();
        Integer idProv = prov.insertarProveedor("Proveedor Prueba " + siguiente(), "5511112222", "", "", "");

        // CU-COM-01 factura completa: producto existente + producto nuevo con factor 12 (caja -> piezas)
        String existente = producto("COM", 10, 20.0, 15.0);
        String nuevo = "TCOMNUEVO" + siguiente();
        List<RenglonCompra> rs = List.of(renglon(existente, false, "5", "8", "1"), renglon(nuevo, true, "2", "120", "12"));
        String folio = "F-" + siguiente();
        String idCompra = c.guardarFacturaCompleta(new String[]{ADMIN, "Factura prueba", folio, "2026-11-20", "Central"},
                idProv, rs, 280.0, 44.8, 324.8, 0);
        check(idCompra != null && idCompra.startsWith("C"), "CU-COM-01 id compra: " + idCompra);
        check(stock(existente) == 15, "CU-COM-01 existente 10+5=15: " + stock(existente));
        check(stock(nuevo) == 24, "CU-COM-01 nuevo 2 cajas x 12 = 24 piezas: " + stock(nuevo));
        check(num("SELECT precio_compra FROM producto WHERE id_producto=?", nuevo) == 10.0, "CU-COM-01 costo unitario 120/12=10");
        check(num("SELECT monto FROM compras WHERE id_compra=?", idCompra) == 324.8, "CU-COM-01 monto con IVA");
        check(entero("SELECT count(*) FROM kardex WHERE referencia=? AND tipo_movimiento='Compra'", idCompra) == 2, "CU-COM-01 kardex Compra x2");
        check(descuadreKardex(existente, 10) == null, "CU-COM-01 kardex cuadra: " + descuadreKardex(existente, 10));

        // CU-COM-02 folio de proveedor duplicado
        check(c.folioYaRegistrado(idProv, folio), "CU-COM-02 folio detectado como registrado");
        check(!c.folioYaRegistrado(idProv, "OTRO-" + folio), "CU-COM-02 folio nuevo libre");
        String err = error(() -> c.guardarFacturaCompleta(new String[]{ADMIN, "dup", folio, "", ""}, idProv,
                List.of(renglon(existente, false, "1", "8", "1")), 8, 0, 8, 0));
        check(err != null && stock(existente) == 15, "CU-COM-02 guardar con folio repetido falla sin mover stock: " + stock(existente));

        // CU-COM-03 transacción: si un renglón falla no queda nada (ni producto nuevo ni stock)
        String nuevo2 = "TCOMNUEVO" + siguiente();
        int compras = entero("SELECT count(*) FROM compras");
        err = error(() -> c.guardarFacturaCompleta(new String[]{ADMIN, "falla", "", "", ""}, 0,
                List.of(renglon(existente, false, "3", "8", "1"), renglon(nuevo2, true, "1", "5", "1"), renglon("NOEXISTE_X", false, "1", "5", "1")),
                0, 0, 0, 0));
        check(err != null, "CU-COM-03 renglón con producto inexistente da error");
        check(entero("SELECT count(*) FROM compras") == compras && stock(existente) == 15
                && entero("SELECT count(*) FROM producto WHERE id_producto=?", nuevo2) == 0, "CU-COM-03 rollback completo");

        // CU-COM-04 borrador: guardar, retomar, actualizar y terminar (no mueve inventario hasta terminar)
        List<RenglonCompra> rb = List.of(renglon(existente, false, "4", "9", "1"));
        Integer idB = c.guardarBorrador(new String[]{ADMIN, "Borrador", "B-1", "", "Central"}, idProv, "36", rb, 0);
        check(idB != null && stock(existente) == 15, "CU-COM-04 borrador guardado sin mover stock");
        String[] cab = c.obtenerCabeceraBorrador(idB);
        check(cab != null && "B-1".equals(cab[1]) && "36".equals(cab[5].replace(".00", "")), "CU-COM-04 cabecera retomada");
        check(c.obtenerRenglonesBorrador(idB).size() == 1, "CU-COM-04 renglones retomados");
        Integer idB2 = c.guardarBorrador(new String[]{ADMIN, "Borrador", "B-1", "", "Central"}, idProv, "54",
                List.of(renglon(existente, false, "6", "9", "1")), idB);
        check(idB.equals(idB2) && c.obtenerRenglonesBorrador(idB).get(0).cantidad.equals("6"), "CU-COM-04 actualizar borrador reemplaza renglones");
        check(VentasPruebaBD.cuenta(c.obtenerComprasYBorradores()) >= 2, "CU-COM-04 listado compras + borradores");
        String idC2 = c.guardarFacturaCompleta(new String[]{ADMIN, "De borrador", "B-1", "", "Central"}, idProv,
                c.obtenerRenglonesBorrador(idB), 54, 0, 54, idB);
        check(idC2 != null && stock(existente) == 21, "CU-COM-04 terminar borrador suma stock 15+6=21: " + stock(existente));
        check(entero("SELECT count(*) FROM compra_borrador WHERE id_borrador=?", idB) == 0, "CU-COM-04 borrador eliminado al terminar");

        // CU-COM-05 descartar borrador
        Integer idB3 = c.guardarBorrador(new String[]{ADMIN, "", "", "", ""}, 0, "", rb, 0);
        c.eliminarBorrador(idB3);
        check(c.obtenerCabeceraBorrador(idB3) == null, "CU-COM-05 borrador descartado");

        // CU-COM-06 quitar un producto de una compra registrada: regresa stock y kardex
        c.eliminarProdCompra(new String[]{idC2, existente});
        check(stock(existente) == 15, "CU-COM-06 quitar renglón descuenta 6: " + stock(existente));
        check(descuadreKardex(existente, 10) == null, "CU-COM-06 kardex cuadra: " + descuadreKardex(existente, 10));
        c.actualizarTotalesCompra(idC2, 0, 0, 0);   // la pantalla recalcula los totales después de quitar
        check(num("SELECT monto FROM compras WHERE id_compra=?", idC2) == 0.0, "CU-COM-06 totales recalculados");
        check(entero("SELECT count(*) FROM kardex WHERE id_producto=? AND tipo_movimiento='Venta'", existente) == 0,
                "CU-COM-06 quitar un renglón de compra deja en el kardex una 'Venta' que no existió");

        // CU-COM-07 proveedor: alta, edición, baja
        check(idProv != null && idProv > 0, "CU-COM-07 alta proveedor");
        String nombreEd = "Prov Editado " + siguiente();
        check(prov.editarProveedor(idProv, nombreEd, "5511112222", "a@b.com", "Calle 1", "XAXX010101000"), "CU-COM-07 editar");
        check("XAXX010101000".equals(prov.obtenerDatosProveedor(idProv)[0]), "CU-COM-07 datos editados");
        check(prov.cambiarEstatusProveedor(idProv, "Inactivo"), "CU-COM-07 inactivar");
        check(VentasPruebaBD.cuenta(prov.buscarProveedores(nombreEd)) == 1, "CU-COM-07 buscar");
        boolean enCombo = false;
        try { java.sql.ResultSet r = prov.obtenerProveedores(); while (r.next()) enCombo |= r.getInt(1) == idProv; } catch (Exception e) {}
        check(!enCombo, "CU-COM-07 inactivo no aparece en el combo de compras");
    }

    static void cotizaciones() {
        CotizacionDAO co = new CotizacionDAO();
        VentaDAO v = new VentaDAO();
        v.limpiarVentaTemp();
        co.limpiarCotizacionTemp();
        String p1 = producto("COT", 30, 10.0, 8.0);
        String p2 = producto("COT", 30, 50.0, 40.0);

        // CU-COT-01 armar cotización: no toca stock, respeta menudeo/mayoreo elegido, acumula
        co.insertarCotizacionTemp(p1, 4, "Menudeo", true);
        co.insertarCotizacionTemp(p1, 2, "Menudeo", true);
        co.insertarCotizacionTemp(p2, 12, "Mayoreo", true);
        check(stock(p1) == 30 && stock(p2) == 30, "CU-COT-01 cotizar no mueve stock");
        check(entero("SELECT cantidad_prod FROM cotizacion_temp WHERE id_producto=?", p1) == 6, "CU-COT-01 acumula 4+2");
        check(Math.abs(co.sumaCotizacionTemp() - (60 + 480)) < 0.01, "CU-COT-01 suma 60+480=540: " + co.sumaCotizacionTemp());
        check(VentasPruebaBD.cuenta(co.buscarProductosCotizacion(p1)) == 1, "CU-COT-01 búsqueda de producto");

        // CU-COT-02 descuento por renglón y quitar renglón
        co.actualizarDescuentoCotiTemp(p2, 10);
        check(Math.abs(co.sumaCotizacionTemp() - (60 + 432)) < 0.01, "CU-COT-02 10% a p2 -> 492: " + co.sumaCotizacionTemp());
        co.eliminarCotizacionTemp(p1);
        co.insertarCotizacionTemp(p1, 6, "Menudeo", false);

        // CU-COT-03 guardar
        String id = co.guardarCotizacion(ADMIN, CLIENTE_GENERICO, co.sumaCotizacionTemp(), java.sql.Date.valueOf("2026-12-31"), "nota");
        check(id.startsWith("COT-"), "CU-COT-03 id: " + id);
        check(entero("SELECT count(*) FROM cotizacion_detalle WHERE id_cotizacion=?", id) == 2, "CU-COT-03 2 renglones");
        check(entero("SELECT count(*) FROM cotizacion_temp") == 0, "CU-COT-03 carrito de cotización vacío");
        check(VentasPruebaBD.cuenta(co.obtenerCotizaciones("Pendiente")) >= 1, "CU-COT-03 listado filtrado por estatus");

        // CU-COT-04 convertir con una venta en curso en Ventas: se bloquea sin perder el carrito
        String pv = producto("COT", 5, 1.0, 1.0);
        v.insertarVentaTemp(new String[]{pv, "1"}, true);
        String err = error(() -> co.convertirCotizacionAVenta(id, "Efectivo"));
        check(err != null && v.ventaEnCurso() && stock(pv) == 4, "CU-COT-04 bloqueado y carrito intacto");
        v.limpiarVentaTemp();

        // CU-COT-05 convertir: crea venta con descuentos, descuenta stock, marca Convertida
        String venta = co.convertirCotizacionAVenta(id, "Transferencia");
        check(venta != null && venta.startsWith("V"), "CU-COT-05 venta: " + venta);
        check(num("SELECT total_venta FROM venta WHERE id_venta=?", venta) == 492.0, "CU-COT-05 total respeta el descuento 492: "
                + num("SELECT total_venta FROM venta WHERE id_venta=?", venta));
        check(num("SELECT total_venta FROM venta WHERE id_venta=?", venta) == num("SELECT total_cotizacion FROM cotizacion WHERE id_cotizacion=?", id),
                "CU-COT-05 total de la venta = total cotizado");
        check(stock(p1) == 24 && stock(p2) == 18, "CU-COT-05 stock p1=" + stock(p1) + " p2=" + stock(p2));
        check("Convertida".equals(texto("SELECT estatus FROM cotizacion WHERE id_cotizacion=?", id)), "CU-COT-05 estatus Convertida");
        check("Transferencia".equals(texto("SELECT forma_pago FROM venta WHERE id_venta=?", venta)), "CU-COT-05 forma de pago");
        check(!v.ventaEnCurso(), "CU-COT-05 no deja basura en el carrito de ventas");
        check(descuadreKardex(p2, 30) == null, "CU-COT-05 kardex cuadra: " + descuadreKardex(p2, 30));

        // CU-COT-06 al convertir se debe cobrar el tipo de precio cotizado (la pantalla deja elegir Menudeo/Mayoreo)
        String p3 = producto("COT", 30, 10.0, 8.0);
        co.insertarCotizacionTemp(p3, 5, "Mayoreo", true);    // cotizado: 5 x 8 = 40
        String id2 = co.guardarCotizacion(ADMIN, CLIENTE_GENERICO, co.sumaCotizacionTemp(), null, "");
        String venta2 = co.convertirCotizacionAVenta(id2, "Efectivo");
        check(num("SELECT total_venta FROM venta WHERE id_venta=?", venta2) == 40.0,
                "CU-COT-06 se cotizaron 5 a mayoreo ($40) y la venta cobró " + num("SELECT total_venta FROM venta WHERE id_venta=?", venta2));

        String p5 = producto("COT", 30, 10.0, 8.0);
        co.insertarCotizacionTemp(p5, 12, "Menudeo", true);   // cotizado: 12 x 10 = 120
        String id5 = co.guardarCotizacion(ADMIN, CLIENTE_GENERICO, co.sumaCotizacionTemp(), null, "");
        String venta5 = co.convertirCotizacionAVenta(id5, "Efectivo");
        check(num("SELECT total_venta FROM venta WHERE id_venta=?", venta5) == 120.0,
                "CU-COT-06 se cotizaron 12 a menudeo ($120) y la venta cobró " + num("SELECT total_venta FROM venta WHERE id_venta=?", venta5));

        // CU-COT-07 cancelar
        co.insertarCotizacionTemp(p1, 1, "Menudeo", true);
        String id3 = co.guardarCotizacion(ADMIN, CLIENTE_GENERICO, 10, null, "");
        co.cancelarCotizacion(id3);
        check("Cancelada".equals(texto("SELECT estatus FROM cotizacion WHERE id_cotizacion=?", id3)), "CU-COT-07 cancelada");

        // CU-COT-08 sin stock al convertir: no debe crear venta ni dejar stock movido
        String p4 = producto("COT", 3, 10.0, 8.0);
        co.insertarCotizacionTemp(p4, 3, "Menudeo", true);
        String id4 = co.guardarCotizacion(ADMIN, CLIENTE_GENERICO, 30, null, "");
        exec("UPDATE producto SET cantidad=1 WHERE id_producto=?", p4);
        int ventas = entero("SELECT count(*) FROM venta");
        err = error(() -> co.convertirCotizacionAVenta(id4, "Efectivo"));
        check(err != null && entero("SELECT count(*) FROM venta") == ventas, "CU-COT-08 sin stock: error y sin venta");
        check(stock(p4) == 1 && "Pendiente".equals(texto("SELECT estatus FROM cotizacion WHERE id_cotizacion=?", id4)), "CU-COT-08 queda pendiente");
    }
}
