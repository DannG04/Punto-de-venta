package pruebas;

/** Mini arnés de pruebas (el proyecto no usa JUnit): cada prueba es una clase con main. */
public final class Prueba {
    private static int ok = 0, fallos = 0;

    private Prueba() {}

    public static void check(boolean condicion, String descripcion) {
        if (condicion) ok++;
        else { fallos++; System.out.println("[FALLA] " + descripcion); }
    }

    public static void check(boolean condicion) {
        check(condicion, "línea " + Thread.currentThread().getStackTrace()[2].getLineNumber());
    }

    /** Imprime el resumen y termina con código 1 si hubo fallos. */
    public static void terminar(String nombre) {
        System.out.println(nombre + ": " + ok + " OK, " + fallos + " fallos");
        System.exit(fallos == 0 ? 0 : 1);
    }
}
