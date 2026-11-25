package puntoventa.util;

/**
 * Evita que un Enter mantenido o repetido confirme un diálogo recién abierto:
 * durante los primeros {@code esperaMs} después de abrir, Enter no cuenta.
 */
public final class GuardaEnter {

    private final long esperaMs;
    private long apertura = -1;

    public GuardaEnter(long esperaMs) {
        this.esperaMs = esperaMs;
    }

    public void abrir(long ahoraMs) {
        apertura = ahoraMs;
    }

    public boolean permite(long ahoraMs) {
        return apertura >= 0 && ahoraMs - apertura >= esperaMs;
    }
}
