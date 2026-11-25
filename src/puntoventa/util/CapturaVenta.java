package puntoventa.util;

/**
 * Interpreta lo que se escribe (o escanea) en el campo de código de Ventas.
 * Formatos: vacío (cobrar), "código", "texto a buscar" o "N*código" para una cantidad.
 * Clase pura: sin Swing ni base de datos.
 */
public final class CapturaVenta {

    public enum Tipo { VACIO, PRODUCTO, INVALIDO }

    public static final int CANTIDAD_MAXIMA = 9999;
    public static final int LONGITUD_MAXIMA = 40;

    public final Tipo tipo;
    public final int cantidad;
    public final String texto;
    public final String mensaje;

    private CapturaVenta(Tipo tipo, int cantidad, String texto, String mensaje) {
        this.tipo = tipo;
        this.cantidad = cantidad;
        this.texto = texto;
        this.mensaje = mensaje;
    }

    public static CapturaVenta interpretar(String entrada) {
        String e = entrada == null ? "" : entrada.trim();
        if (e.isEmpty()) return new CapturaVenta(Tipo.VACIO, 1, "", null);

        int asterisco = e.indexOf('*');
        if (asterisco < 0) return new CapturaVenta(Tipo.PRODUCTO, 1, e, null);

        String izq = e.substring(0, asterisco).trim();
        String der = e.substring(asterisco + 1).trim();
        Integer cantidad = cantidadValida(izq);
        if (cantidad == null) {
            return invalido("Cantidad no válida: use un número de 1 a " + CANTIDAD_MAXIMA + " antes del *");
        }
        if (der.isEmpty() || der.indexOf('*') >= 0) {
            return invalido("Falta el código después de " + cantidad + "*");
        }
        return new CapturaVenta(Tipo.PRODUCTO, cantidad, der, null);
    }

    /** true si el texto (código) son solo dígitos, como un código de barras. */
    public boolean soloDigitos() {
        if (texto.isEmpty()) return false;
        for (int i = 0; i < texto.length(); i++) {
            if (!Character.isDigit(texto.charAt(i))) return false;
        }
        return true;
    }

    private static Integer cantidadValida(String s) {
        if (s.isEmpty() || s.length() > 4) return null;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) < '0' || s.charAt(i) > '9') return null;
        }
        int n = Integer.parseInt(s);
        return (n >= 1 && n <= CANTIDAD_MAXIMA) ? n : null;
    }

    private static CapturaVenta invalido(String mensaje) {
        return new CapturaVenta(Tipo.INVALIDO, 1, "", mensaje);
    }
}
