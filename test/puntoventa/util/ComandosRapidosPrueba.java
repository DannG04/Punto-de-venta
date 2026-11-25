package puntoventa.util;

import static pruebas.Prueba.*;

/** Casos CU-CR: captura rápida en Ventas, montos del cobro y protección contra Enter de más (no usan la base). */
public class ComandosRapidosPrueba {
    public static void main(String[] a) {
        CapturaVenta c;
        c = CapturaVenta.interpretar("");            check(c.tipo == CapturaVenta.Tipo.VACIO, "CU-CR-01 vacío");
        c = CapturaVenta.interpretar("   ");         check(c.tipo == CapturaVenta.Tipo.VACIO, "CU-CR-01 solo espacios");
        c = CapturaVenta.interpretar(null);          check(c.tipo == CapturaVenta.Tipo.VACIO, "CU-CR-01 null");
        c = CapturaVenta.interpretar("7501234567890");
        check(c.tipo == CapturaVenta.Tipo.PRODUCTO && c.cantidad == 1 && c.texto.equals("7501234567890") && c.soloDigitos(), "CU-CR-02 código cantidad 1");
        c = CapturaVenta.interpretar("0012");        check(c.texto.equals("0012") && c.soloDigitos(), "CU-CR-02 conserva ceros");
        c = CapturaVenta.interpretar("3*7501234567890");
        check(c.tipo == CapturaVenta.Tipo.PRODUCTO && c.cantidad == 3 && c.texto.equals("7501234567890"), "CU-CR-03 N*código");
        c = CapturaVenta.interpretar(" 3 * 750 ");   check(c.cantidad == 3 && c.texto.equals("750"), "CU-CR-03 tolera espacios");
        c = CapturaVenta.interpretar("ABC-12");      check(c.tipo == CapturaVenta.Tipo.PRODUCTO && !c.soloDigitos(), "CU-CR-04 código con guion no es soloDígitos");
        c = CapturaVenta.interpretar("coca cola");   check(c.tipo == CapturaVenta.Tipo.PRODUCTO && c.texto.equals("coca cola") && !c.soloDigitos(), "CU-CR-04 texto");
        c = CapturaVenta.interpretar("2*coca");      check(c.cantidad == 2 && c.texto.equals("coca"), "CU-CR-04 N*texto");
        for (String malo : new String[]{"3*", "0*750", "*750", "abc*750", "-2*750", "10000*750", "2*3*750"}) {
            c = CapturaVenta.interpretar(malo);
            check(c.tipo == CapturaVenta.Tipo.INVALIDO && c.mensaje != null && !c.mensaje.isEmpty(), "CU-CR-05 inválido: " + malo);
        }
        check(CapturaVenta.interpretar("9999*1").cantidad == 9999, "CU-CR-05 máximo permitido");

        check(Cobro.parsearMonto("100") == 100.0 && Cobro.parsearMonto("74.1") == 74.1, "CU-CR-06 montos válidos");
        for (String m : new String[]{"", "100,50", "$100", "1.2.3", "-5", "abc", "1.234"}) check(Cobro.parsearMonto(m) == null, "CU-CR-06 monto inválido: " + m);
        check(Cobro.alcanza(74.10, 74.1) && !Cobro.alcanza(74.10, 74.0), "CU-CR-07 alcanza con tolerancia");
        check(Cobro.cambio(74.0, 100.0) == 26.0 && Cobro.cambio(74.0, 50.0) == 0.0, "CU-CR-07 cambio");
        check(Cobro.formato(74.1).equals("74.10"), "CU-CR-07 formato");

        GuardaEnter g = new GuardaEnter(300);
        check(!g.permite(1000), "CU-CR-08 sin abrir no permite");
        g.abrir(1000);
        check(!g.permite(1000) && !g.permite(1299) && g.permite(1300) && g.permite(5000), "CU-CR-08 bloquea los primeros 300 ms");

        terminar("ComandosRapidosPrueba");
    }
}
