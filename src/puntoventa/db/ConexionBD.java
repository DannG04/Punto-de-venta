package puntoventa.db;

import puntoventa.report.*;

import java.sql.*;

/**
 * Fachada de acceso a datos.
 *
 * Credenciales, constructor (lectura de db.properties), inst(), query() y nvl()
 * viven en BaseDAO; las llamadas crudas conect.inst/query se resuelven por
 * herencia. Cada método de dominio es un delegador de una línea hacia su DAO.
 *
 * @author mayra
 */
public class ConexionBD extends BaseDAO {

    // DAOs por dominio (la fachada delega en ellos)
    private final EmpleadoDAO empleados = new EmpleadoDAO();
    private final CategoriaDAO categorias = new CategoriaDAO();
    private final ProveedorDAO proveedores = new ProveedorDAO();
    private final GastoDAO gastos = new GastoDAO();
    private final GananciaDAO ganancias = new GananciaDAO();
    private final EmpresaDAO empresa = new EmpresaDAO();
    private final ClienteDAO clientes = new ClienteDAO();
    private final DevolucionDAO devoluciones = new DevolucionDAO();
    private final CompraDAO compras = new CompraDAO();
    private final ProductoDAO productos = new ProductoDAO();
    private final ListaPreciosDAO listasPrecios = new ListaPreciosDAO();
    private final ApartadoDAO apartados = new ApartadoDAO();
    private final VentaDAO ventas = new VentaDAO();
    private final CotizacionDAO cotizaciones = new CotizacionDAO();
    private final ReporteDAO reportes = new ReporteDAO();

    // FUNCION DE LA TABLA EMPLEADO
    public boolean insertarEmpleado(String[] campos) { return empleados.insertarEmpleado(campos); }

    public boolean actualizarEmpleado(String[] campos, String idEm) { return empleados.actualizarEmpleado(campos, idEm); }

    public boolean inactivarEmpleado(String idEm, boolean act) { return empleados.inactivarEmpleado(idEm, act); }

    public boolean verificarUsuario(String uss) { return empleados.verificarUsuario(uss); }

    public String[] idEmpleado(String uss, String paswor) { return empleados.idEmpleado(uss, paswor); }

    // FUNCIONES DE LA TABLA PRODUCTO
    public void insertarProducto(String[] datos) { productos.insertarProducto(datos); }

    public void insertarProductoConCodigo(String[] datos) { productos.insertarProductoConCodigo(datos); }

    public void actualizarProducto(String ideprod, String[] datos) { productos.actualizarProducto(ideprod, datos); }

    public boolean eliminarProducto(String ideprod) { return productos.eliminarProducto(ideprod); }

    public boolean reactivarProducto(String ideprod) { return productos.reactivarProducto(ideprod); }

    public String archivarProducto(String ideprod) { return productos.archivarProducto(ideprod); }

    public String estadoProducto(String ideprod) { return productos.estadoProducto(ideprod); }

    public ResultSet obtenerProductosInactivos() { return productos.obtenerProductosInactivos(); }

    public TicketData obtenerDatosVenta(String idVenta) { return ventas.obtenerDatosVenta(idVenta); }

    public TicketData obtenerDatosApartado(String idApartado, TipoTicket tipo) { return apartados.obtenerDatosApartado(idApartado, tipo); }

    public boolean hayPocosProductos() { return productos.hayPocosProductos(); }

    // FUNCIONES DE LA TABLA DE VENTA TEMPORAL
    public void limpiarVentaTemp() { ventas.limpiarVentaTemp(); }

    public void insertarVentaTemp(String[] campos, boolean acum) { ventas.insertarVentaTemp(campos, acum); }

    public void eliminarVentaTemp(String ide) { ventas.eliminarVentaTemp(ide); }

    public Double sumaVentaTemp() { return ventas.sumaVentaTemp(); }

    // FUNCIONES DE LA TABLA VENTA
    public String registrarVenta(String idEmp, String idCli) { return ventas.registrarVenta(idEmp, idCli); }

    public String registrarVentaConFormaPago(String idEmp, String idCli, String formaPago) { return ventas.registrarVentaConFormaPago(idEmp, idCli, formaPago); }

    // FUNCIONES DE REPORTES Y KARDEX
    public ResultSet corteDiario(java.time.LocalDate fecha) { return reportes.corteDiario(fecha); }

    public ResultSet totalesDia(java.time.LocalDate fecha) { return reportes.totalesDia(fecha); }

    public ResultSet buscarProductoKardex(String filtro) { return reportes.buscarProductoKardex(filtro); }

    public ResultSet buscarProductosCotizacion(String filtro) { return cotizaciones.buscarProductosCotizacion(filtro); }

    public ResultSet kardexProducto(String idProducto, java.time.LocalDate desde, java.time.LocalDate hasta) { return reportes.kardexProducto(idProducto, desde, hasta); }

    // FUNCIONES DE LA TABLA COMPRAS
    public String insertarCompra(String[] campos) { return compras.insertarCompra(campos); }

    public void actualizarCompra(String[] campos) { compras.actualizarCompra(campos); }

    public void eliminarCompra(String idCom) { compras.eliminarCompra(idCom); }

    public void insertarProdCompra(String[] campos, boolean ac) { compras.insertarProdCompra(campos, ac); }

    public void eliminarProdCompra(String[] campos) { compras.eliminarProdCompra(campos); }

    public String resolverCodigo(String codigo) { return productos.resolverCodigo(codigo); }

    public String[] obtenerProductoParaCompra(String idProducto) { return productos.obtenerProductoParaCompra(idProducto); }

    public boolean codigoBarrasDuplicado(String codigoBarras, String idExcluir) { return productos.codigoBarrasDuplicado(codigoBarras, idExcluir); }

    public boolean crearProductoDesdeCompra(String[] d, Integer idCategoria, double maxDescuento) { return productos.crearProductoDesdeCompra(d, idCategoria, maxDescuento); }

    public String guardarFacturaCompleta(String[] cab, int idProveedor,
            java.util.List<RenglonCompra> renglones, double subtotal, double iva, double total,
            int idBorradorAEliminar) {
        return compras.guardarFacturaCompleta(cab, idProveedor, renglones, subtotal, iva, total, idBorradorAEliminar);
    }

    public Integer guardarBorrador(String[] cab, int idProveedor, String totalFactura,
            java.util.List<RenglonCompra> renglones, int idBorradorExistente) {
        return compras.guardarBorrador(cab, idProveedor, totalFactura, renglones, idBorradorExistente);
    }

    public String[] obtenerCabeceraBorrador(int idBorrador) { return compras.obtenerCabeceraBorrador(idBorrador); }

    public java.util.List<RenglonCompra> obtenerRenglonesBorrador(int idBorrador) { return compras.obtenerRenglonesBorrador(idBorrador); }

    public void eliminarBorrador(int idBorrador) { compras.eliminarBorrador(idBorrador); }

    // FUNCIONES DE LA TABLA CLIENTE
    public boolean insertarCliente(String[] campos) { return clientes.insertarCliente(campos); }

    public void actualizarCliente(String[] campos, String idC) { clientes.actualizarCliente(campos, idC); }

    public void inactivarCliente(String idC, boolean act) { clientes.inactivarCliente(idC, act); }

    // FUNCIONES DE LA TABLA DE DEVOLUCIONES
    public String insertarDevolucion(String[] campos) { return devoluciones.insertarDevolucion(campos); }

    public void eliminarDevolucion(String idDev) { devoluciones.eliminarDevolucion(idDev); }

    public void insertarProdDevolucion(String[] campos, boolean ac) { devoluciones.insertarProdDevolucion(campos, ac); }

    public void eliminarProdDevolucion(String[] campos) { devoluciones.eliminarProdDevolucion(campos); }

    // FUNCIONES PARA LA TABLA APARTADO
    public String insertarApartado(String[] campos) { return apartados.insertarApartado(campos); }

    public boolean actualizarApartado(String idAp, Double cantPag) { return apartados.actualizarApartado(idAp, cantPag); }

    public void eliminarApartado(String idAp) { apartados.eliminarApartado(idAp); }

    public void revisarApartado() { apartados.revisarApartado(); }

    public double cantidadCancelarApartado(String idAp) { return apartados.cantidadCancelarApartado(idAp); }

    public void cancelarApartado(String idAp) { apartados.cancelarApartado(idAp); }

    public void entregarApartado(String idAp) { apartados.entregarApartado(idAp); }

    // FUNCIONES PARA LA TABLA APARTADO DETALLE
    public void insertarProdApartado(String[] campos, boolean ac) { apartados.insertarProdApartado(campos, ac); }

    public void eliminarProdApartado(String[] campos) { apartados.eliminarProdApartado(campos); }

    // FUNCIONES PARA LA TABLA GASTOS
    public void insertarGasto(String[] campos) { gastos.insertarGasto(campos); }

    public void actualizarGasto(String idGasto, String[] campos) { gastos.actualizarGasto(idGasto, campos); }

    // FUNCIONES PARA LA TABLA OTRAS GANANCIAS
    public void insertarOtraGanancia(String[] campos) { ganancias.insertarOtraGanancia(campos); }

    public void actualizarOtraGanancia(String[] campos) { ganancias.actualizarOtraGanancia(campos); }

    // FUNCIONES DE LA TABLA PROVEEDOR
    public Integer insertarProveedor(String nombre, String telefono, String email, String direccion, String rfc) { return proveedores.insertarProveedor(nombre, telefono, email, direccion, rfc); }

    public boolean editarProveedor(int id, String nombre, String telefono, String email, String direccion, String rfc) { return proveedores.editarProveedor(id, nombre, telefono, email, direccion, rfc); }

    public String[] obtenerDatosProveedor(int idProveedor) { return proveedores.obtenerDatosProveedor(idProveedor); }

    public boolean cambiarEstatusProveedor(int id, String estatus) { return proveedores.cambiarEstatusProveedor(id, estatus); }

    public ResultSet obtenerProveedores() { return proveedores.obtenerProveedores(); }

    public boolean folioYaRegistrado(int idProveedor, String folio) { return compras.folioYaRegistrado(idProveedor, folio); }

    public void actualizarTotalesCompra(String idCompra, double subtotal, double iva, double total) { compras.actualizarTotalesCompra(idCompra, subtotal, iva, total); }

    public double ObtenerDato(String nombreCol) { return reportes.ObtenerDato(nombreCol); }

    public ResultSet reporte_diario() { return reportes.reporte_diario(); }

    public ResultSet reporte_diario(java.time.LocalDate fecha) { return reportes.reporte_diario(fecha); }

    public ResultSet seleccionarApartado(String idap) { return apartados.seleccionarApartado(idap); }

    public ResultSet seleccionarVendedor(String idVendedor) { return empleados.seleccionarVendedor(idVendedor); }

    public ResultSet seleccionarProductos(String idApartado) { return apartados.seleccionarProductos(idApartado); }

    // FUNCIONES DE LA TABLA CATEGORIA
    public boolean insertarCategoria(String nombre, String descripcion) { return categorias.insertarCategoria(nombre, descripcion); }

    public boolean editarCategoria(int id, String nombre, String descripcion) { return categorias.editarCategoria(id, nombre, descripcion); }

    public boolean cambiarEstatusCategoria(int id, String estatus) { return categorias.cambiarEstatusCategoria(id, estatus); }

    public ResultSet obtenerCategorias() { return categorias.obtenerCategorias(); }

    public ResultSet obtenerTodasCategorias() { return categorias.obtenerTodasCategorias(); }

    public ResultSet buscarCategoriasPorNombre(String filtro) { return categorias.buscarCategoriasPorNombre(filtro); }

    // FUNCIONES DE PRODUCTO CON CATEGORÍA
    public void insertarProductoConCodigoYCategoria(String[] datos, Integer idCategoria, double maxDescuento) { productos.insertarProductoConCodigoYCategoria(datos, idCategoria, maxDescuento); }

    public void actualizarProductoConCategoria(String ideprod, String[] datos, Integer idCategoria, double maxDescuento) { productos.actualizarProductoConCategoria(ideprod, datos, idCategoria, maxDescuento); }

    public void actualizarCamposCompraProducto(String idProducto, String codigoBarras, boolean llevaIva,
            String unidadCompra, String unidadVenta, double factor) {
        productos.actualizarCamposCompraProducto(idProducto, codigoBarras, llevaIva, unidadCompra, unidadVenta, factor);
    }

    // FUNCIONES DE DESCUENTOS
    public double obtenerMaxDescuento(String idProducto) { return productos.obtenerMaxDescuento(idProducto); }

    public void actualizarDescuentoTemp(String idProducto, double descuento) { ventas.actualizarDescuentoTemp(idProducto, descuento); }

    public void actualizarDescuentoVenta(String idVenta, String idProducto, double descuento) { ventas.actualizarDescuentoVenta(idVenta, idProducto, descuento); }

    // FUNCIONES DE LA TABLA EMPRESA
    public ResultSet obtenerEmpresa() { return empresa.obtenerEmpresa(); }

    public boolean actualizarEmpresa(String nombre, String razonSocial, String rfc,
            String telefono, String correo, String direccion, String ciudad,
            String estado, String cp, String mensajeTicket, String logoRuta) {
        return empresa.actualizarEmpresa(nombre, razonSocial, rfc, telefono, correo,
            direccion, ciudad, estado, cp, mensajeTicket, logoRuta);
    }

    public boolean empresaInicializada() { return empresa.empresaInicializada(); }

    // FUNCIONES DE LISTA DE PRECIOS
    public ResultSet obtenerListas() { return listasPrecios.obtenerListas(); }

    public double obtenerPrecioEnLista(String idProducto, int idLista) { return listasPrecios.obtenerPrecioEnLista(idProducto, idLista); }

    public void actualizarPrecioEnLista(String idProducto, int idLista, double precio) { listasPrecios.actualizarPrecioEnLista(idProducto, idLista, precio); }

    public void insertarPrecioEnLista(String idProducto, int idLista, double precio) { listasPrecios.insertarPrecioEnLista(idProducto, idLista, precio); }

    public void eliminarPrecioEnLista(String idProducto, int idLista) { listasPrecios.eliminarPrecioEnLista(idProducto, idLista); }

    public void actualizarPrecioListaTemp(String idProducto, double nuevoPrecio, String nombreLista) { listasPrecios.actualizarPrecioListaTemp(idProducto, nuevoPrecio, nombreLista); }

    public double obtenerMinMaxDescuentoTemp() { return ventas.obtenerMinMaxDescuentoTemp(); }

    // ---- COTIZACIONES ----
    public void limpiarCotizacionTemp() { cotizaciones.limpiarCotizacionTemp(); }

    public void insertarCotizacionTemp(String idProd, int cantidad, String tipoVenta, boolean acum) { cotizaciones.insertarCotizacionTemp(idProd, cantidad, tipoVenta, acum); }

    public void eliminarCotizacionTemp(String idProd) { cotizaciones.eliminarCotizacionTemp(idProd); }

    public ResultSet mostrarCotizacionTemp() { return cotizaciones.mostrarCotizacionTemp(); }

    public double sumaCotizacionTemp() { return cotizaciones.sumaCotizacionTemp(); }

    public void actualizarDescuentoCotiTemp(String idProd, double descuento) { cotizaciones.actualizarDescuentoCotiTemp(idProd, descuento); }

    public String guardarCotizacion(String idEmp, String idCli, double total,
                                    java.sql.Date vigencia, String notas) {
        return cotizaciones.guardarCotizacion(idEmp, idCli, total, vigencia, notas);
    }

    public ResultSet obtenerCotizaciones(String filtro) { return cotizaciones.obtenerCotizaciones(filtro); }

    public ResultSet obtenerDetalleCotizacion(String idCoti) { return cotizaciones.obtenerDetalleCotizacion(idCoti); }

    public ResultSet obtenerCotizacionHeader(String idCoti) { return cotizaciones.obtenerCotizacionHeader(idCoti); }

    public String convertirCotizacionAVenta(String idCoti, String formaPago) { return cotizaciones.convertirCotizacionAVenta(idCoti, formaPago); }

    public void cancelarCotizacion(String idCoti) { cotizaciones.cancelarCotizacion(idCoti); }
}
