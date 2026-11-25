package puntoventa.util;

import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;

/**
 * Estado físico de la tecla Enter en toda la aplicación (sin importar qué ventana tenga
 * el foco). Los diálogos del cobro lo consultan al abrirse: si Enter sigue abajo, esperan
 * a que se suelte. Suponerlo por cómo se abrió fallaba cuando la tecla se soltaba antes
 * de que la ventana terminara de abrir.
 */
public final class EstadoEnter {

    private static volatile boolean abajo = false;
    private static boolean instalado = false;

    private EstadoEnter() {}

    /** Registra el observador global de teclado (una sola vez). */
    public static synchronized void instalar() {
        if (instalado) return;
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(e -> {
            procesar(e);
            return false; // solo observa, no consume
        });
        instalado = true;
    }

    /** Actualiza el estado con un evento de teclado. */
    public static void procesar(KeyEvent e) {
        if (e.getKeyCode() != KeyEvent.VK_ENTER) return;
        if (e.getID() == KeyEvent.KEY_PRESSED) abajo = true;
        else if (e.getID() == KeyEvent.KEY_RELEASED) abajo = false;
    }

    public static boolean abajo() {
        return abajo;
    }
}
