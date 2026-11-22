package puntoventa.util;

import static pruebas.Prueba.*;

public class VersionAppPrueba {
    public static void main(String[] a) {
        check(VersionApp.esMasNueva("1.23", "1.22"));
        check(VersionApp.esMasNueva("v1.10", "1.9"));
        check(VersionApp.esMasNueva("1.100", "1.22"));
        check(VersionApp.esMasNueva("2.0", "1.99"));
        check(VersionApp.esMasNueva("1.22.1", "1.22"));
        check(!VersionApp.esMasNueva("1.22", "1.22"));
        check(!VersionApp.esMasNueva("v1.22", "1.22"));
        check(!VersionApp.esMasNueva("1.3", "1.22"));
        check(!VersionApp.esMasNueva("basura", "1.22"));
        check(!VersionApp.esMasNueva(null, "1.22"));
        check(VersionApp.normalizar(" V1.23 ").equals("1.23"));
        check(VersionApp.actual().equals("1.22"), "actual() lee version.properties: " );
        terminar("VersionAppPrueba");
    }
}
