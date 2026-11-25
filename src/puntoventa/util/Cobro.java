package puntoventa.util;

/** Cálculos de montos del cobro (clase pura). */
public final class Cobro {

    private static final double TOLERANCIA = 0.005;

    private Cobro() {}

    /** Monto ≥ 0 con a lo más un punto y dos decimales; null si no es válido (no acepta "," ni "$"). */
    public static Double parsearMonto(String texto) {
        if (texto == null) return null;
        String t = texto.trim();
        if (!t.matches("\\d+(\\.\\d{0,2})?")) return null;
        return Double.valueOf(t);
    }

    public static boolean alcanza(double total, double recibido) {
        return recibido + TOLERANCIA >= total;
    }

    public static double cambio(double total, double recibido) {
        double c = Math.round((recibido - total) * 100.0) / 100.0;
        return c > 0 ? c : 0.0;
    }

    public static String formato(double monto) {
        return String.format(java.util.Locale.US, "%.2f", monto);
    }
}
