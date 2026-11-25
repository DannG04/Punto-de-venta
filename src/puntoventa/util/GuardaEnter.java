package puntoventa.util;

/**
 * Evita que un Enter mantenido o repetido confirme un diálogo recién abierto:
 * durante los primeros {@code esperaMs} después de abrir, Enter no cuenta; y si el
 * diálogo se abrió con Enter presionado, tampoco cuenta hasta que se suelte
 * (la repetición automática del teclado empieza después de esos milisegundos).
 */
public final class GuardaEnter {

    private final long esperaMs;
    private long apertura = -1;
    private boolean esperandoSoltar = false;

    public GuardaEnter(long esperaMs) {
        this.esperaMs = esperaMs;
    }

    public void abrir(long ahoraMs) {
        abrir(ahoraMs, false);
    }

    /** @param enterPresionado true si el diálogo se abrió con un Enter que aún puede estar sostenido */
    public void abrir(long ahoraMs, boolean enterPresionado) {
        apertura = ahoraMs;
        esperandoSoltar = enterPresionado;
    }

    /** Se llama al soltar Enter dentro del diálogo. */
    public void soltado() {
        esperandoSoltar = false;
    }

    public boolean permite(long ahoraMs) {
        return apertura >= 0 && !esperandoSoltar && ahoraMs - apertura >= esperaMs;
    }
}
