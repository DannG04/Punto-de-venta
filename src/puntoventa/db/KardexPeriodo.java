package puntoventa.db;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Movimientos del kardex de un producto en un periodo, con el saldo inicial y
 * la verificación de que la cadena de saldos sea continua.
 */
public class KardexPeriodo {

    /** Tipos de movimiento válidos (mismos que el CHECK de la tabla kardex, V1.23). */
    public static final String[] TIPOS = {
        "Inventario inicial", "Venta", "Compra", "Cancelacion compra",
        "Devolucion", "Cancelacion devolucion", "Apartado", "Cancelacion apartado",
        "Ajuste entrada", "Ajuste salida", "Ajuste", "Merma"
    };

    public static class Movimiento {
        public long id;
        public LocalDateTime fecha;
        public String tipo;
        public int cantidad;
        public int existenciaAnterior;
        public int existenciaPosterior;
        public String referencia;
        public String empleado;
        public String observacion;
        /** true si la existencia anterior no coincide con el saldo del movimiento previo. */
        public boolean salto;

        public int entrada() { return cantidad > 0 ? cantidad : 0; }
        public int salida()  { return cantidad < 0 ? -cantidad : 0; }
    }

    public final String idProducto;
    public final LocalDate desde;
    public final LocalDate hasta;
    public final List<Movimiento> movimientos = new ArrayList<>();
    public int saldoInicial;
    public int existenciaActual;
    public int entradas;
    public int salidas;
    public int saltos;

    public KardexPeriodo(String idProducto, LocalDate desde, LocalDate hasta) {
        this.idProducto = idProducto;
        this.desde = desde;
        this.hasta = hasta;
    }

    public int saldoFinal() {
        return movimientos.isEmpty() ? saldoInicial : movimientos.get(movimientos.size() - 1).existenciaPosterior;
    }

    /** El periodo llega hasta hoy, así que su saldo final debe ser la existencia actual. */
    public boolean llegaAHoy() {
        return !hasta.isBefore(LocalDate.now());
    }

    /** Sin saltos y, si el periodo llega a hoy, terminando en la existencia actual. */
    public boolean cuadra() {
        return saltos == 0 && (!llegaAHoy() || saldoFinal() == existenciaActual);
    }

    /** Explicación de si el kardex cuadra, para la pantalla y el reporte. */
    public String descripcionEstado() {
        if (saltos > 0) {
            return "No cuadra: " + saltos + (saltos == 1 ? " salto" : " saltos")
                    + " en el historial. Hubo movimientos que no se registraron.";
        }
        if (llegaAHoy() && saldoFinal() != existenciaActual) {
            return "No cuadra: el kardex termina en " + saldoFinal()
                    + " pero la existencia actual es " + existenciaActual + ".";
        }
        if (llegaAHoy()) {
            return "Cuadra: el saldo final coincide con la existencia actual.";
        }
        return "Sin saltos en el periodo. El saldo final es la existencia al cierre del "
                + hasta.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")) + ".";
    }

    /** Calcula entradas, salidas y saltos a partir de los movimientos ya cargados. */
    void calcular() {
        entradas = 0;
        salidas = 0;
        saltos = 0;
        int esperado = saldoInicial;
        for (Movimiento m : movimientos) {
            m.salto = m.existenciaAnterior != esperado;
            if (m.salto) saltos++;
            entradas += m.entrada();
            salidas += m.salida();
            esperado = m.existenciaPosterior;
        }
    }
}
