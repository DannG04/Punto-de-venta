package puntoventa.db;

import static pruebas.Prueba.*;
import static pruebas.BD.*;

/**
 * Casos CU-CR de la capa de datos para los comandos rápidos de Ventas.
 * Corre headless: si algún método abriera una ventana lanzaría HeadlessException
 * y la prueba se caería, así se valida que no haya diálogos.
 */
public class ComandosRapidosPruebaBD {
    public static void main(String[] a) {
        VentaDAO v = new VentaDAO();
        v.limpiarVentaTemp();
        String p = producto("CR", 5, 10.0, 8.0);
        check(v.insertarVentaTempSinDialogo(new String[]{p, "2"}, true) == null, "CU-CR-10 alta sin error regresa null");
        check(entero("SELECT cantidad_prod FROM venta_temp WHERE id_producto=?", p) == 2, "CU-CR-10 entró al carrito");
        String err = v.insertarVentaTempSinDialogo(new String[]{p, "9"}, true);
        check(err != null && err.contains("No hay suficiente cantidad"), "CU-CR-11 sin existencia regresa mensaje: " + err);
        check(entero("SELECT cantidad_prod FROM venta_temp WHERE id_producto=?", p) == 2, "CU-CR-11 carrito intacto");
        err = v.insertarVentaTempSinDialogo(new String[]{"NOEXISTE999", "1"}, true);
        check(err != null && err.contains("no existe"), "CU-CR-12 inexistente regresa mensaje: " + err);
        check(v.actualizarDescuentoTempSinDialogo(p, 0) == null, "CU-CR-13 descuento sin error");
        v.limpiarVentaTemp();
        int esperado = entero("SELECT count(*)::int FROM producto WHERE estatus='Activo' AND cantidad<=12");
        check(new ProductoDAO().contarProductosPocos() == esperado, "CU-CR-14 conteo de poco stock = " + esperado);
        terminar("ComandosRapidosPruebaBD");
    }
}
