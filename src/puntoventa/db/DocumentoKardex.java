package puntoventa.db;

import java.util.ArrayList;
import java.util.List;

/** Documento (venta, compra, apartado o devolución) al que apunta un folio del kardex. */
public class DocumentoKardex {
    public String titulo;
    /** Pares etiqueta / valor del encabezado. */
    public final List<String[]> datos = new ArrayList<>();
    public String[] columnas;
    public final List<Object[]> filas = new ArrayList<>();
}
