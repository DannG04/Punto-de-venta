package puntoventa.util;

import java.io.InputStream;
import java.util.Properties;

/**
 * Número de versión de la app. Vive en /version.properties dentro del JAR y lo
 * mantiene installer/update/publicar_version.ps1.
 */
public final class VersionApp {

    private VersionApp() {}

    /** Versión instalada; "0" si no se puede leer. */
    public static String actual() {
        try (InputStream in = VersionApp.class.getResourceAsStream("/version.properties")) {
            if (in == null) return "0";
            Properties p = new Properties();
            p.load(in);
            String v = normalizar(p.getProperty("app.version"));
            return v.isEmpty() ? "0" : v;
        } catch (Exception e) {
            return "0";
        }
    }

    /** Quita espacios y una "v"/"V" inicial ("v1.23" → "1.23"). */
    public static String normalizar(String v) {
        if (v == null) return "";
        String s = v.trim();
        if (s.startsWith("v") || s.startsWith("V")) s = s.substring(1);
        return s;
    }

    /** true si {@code remota} es mayor que {@code local}, comparando por partes numéricas. */
    public static boolean esMasNueva(String remota, String local) {
        int[] r = partes(normalizar(remota));
        if (r == null) return false;
        int[] l = partes(normalizar(local));
        if (l == null) l = new int[]{0};
        for (int i = 0; i < Math.max(r.length, l.length); i++) {
            int a = i < r.length ? r[i] : 0;
            int b = i < l.length ? l[i] : 0;
            if (a != b) return a > b;
        }
        return false;
    }

    private static int[] partes(String v) {
        if (v.isEmpty()) return null;
        String[] s = v.split("\\.");
        int[] n = new int[s.length];
        try {
            for (int i = 0; i < s.length; i++) n[i] = Integer.parseInt(s[i]);
        } catch (NumberFormatException e) {
            return null;
        }
        return n;
    }
}
