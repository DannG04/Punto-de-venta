package puntoventa.db;

/**
 * Renglón de compra capturado en memoria (carrito) antes de confirmar la factura.
 * Solo al guardar la factura se vuelca a la base (crear producto nuevo + reg_compra_prod).
 *
 * Antes era una clase anidada en ConexionBD (ConexionBD.RenglonCompra); se extrajo
 * como clase top-level del paquete puntoventa.db al partir ConexionBD en DAOs.
 *
 * @author mayra
 */
public class RenglonCompra {
    public boolean nuevo;          // true si el producto aún no existe en el catálogo
    public String idProducto;      // id_producto / código interno
    public String nombre;          // concepto
    public String codigoBarras;    // puede ir vacío
    public String unidadCompra;
    public String unidadVenta;
    public String factor;          // factor de conversión (como texto)
    public boolean llevaIva;
    public String precioCompra;    // precio por unidad de compra, sin IVA
    public String cantidad;        // en unidad de compra
    public String precioMenudeo;   // solo producto nuevo
    public String precioMayoreo;   // solo producto nuevo
    public double maxDescuento;    // solo producto nuevo
    public Integer idCategoria;    // solo producto nuevo (null = sin categoría)
}
