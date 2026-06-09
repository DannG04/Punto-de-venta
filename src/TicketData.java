import java.util.ArrayList;
import java.util.List;

enum TipoTicket { VENTA, APARTADO_NUEVO, APARTADO_SALDO }

public class TicketData {
    public TipoTicket tipo;
    // Empresa
    public String empresaNombre = "";
    public String empresaTel = "";
    public String empresaDireccion = "";
    public String empresaMensaje = "";
    public String logoRuta = "";
    // Transacción
    public String idTransaccion = "";
    public String fecha = "";
    public String vendedor = "";
    public String clienteNombre = "";
    // Productos: cada String[] = {num, nombre, cantidad, precioUnitario, total}
    public List<String[]> productos = new ArrayList<>();
    // Totales
    public double subtotal = 0;
    public double descuento = 0;
    public double total = 0;
    // Solo VENTA
    public String formaPago = "";
    public double recibido = 0;
    public double cambio = 0;
    // Solo APARTADO_*
    public double cantidadDada = 0;
    public double cantidadFaltante = 0;
    public String fechaLimite = "";
}
