package puntoventa.db;

import static pruebas.Prueba.*;
import static pruebas.BD.*;

/** Casos CU-INV (productos), CU-CAT, CU-CLI, CU-EMP (empleados y acceso), CU-EMPR, CU-GAS, CU-GAN. */
public class CatalogosPruebaBD {
    public static void main(String[] a) {
        productos();
        categorias();
        clientes();
        empleados();
        empresaGastosGanancias();
        terminar("CatalogosPruebaBD");
    }

    static void productos() {
        ProductoDAO p = new ProductoDAO();
        CategoriaDAO cat = new CategoriaDAO();
        String nomCat = "Cat " + siguiente();
        cat.insertarCategoria(nomCat, "");
        int idCat = entero("SELECT id_categoria FROM categoria WHERE nombre=?", nomCat);

        // CU-INV-01 alta con código propio, categoría y descuento máximo
        String id = "TINV" + siguiente();
        p.insertarProductoConCodigoYCategoria(new String[]{id, "Martillo", "10", "80", "100"}, idCat, 15);
        check(stock(id) == 10 && "Activo".equals(p.estadoProducto(id)), "CU-INV-01 alta");
        check(VentasPruebaBD.cuenta(p.obtenerInventarioPorCategoria(idCat)) == 1, "CU-INV-01 aparece en su categoría");
        check(VentasPruebaBD.cuenta(p.buscarInventario(id)) == 1, "CU-INV-01 búsqueda en inventario");

        // CU-INV-02 código duplicado e inválido
        String err = error(() -> p.insertarProductoConCodigoYCategoria(new String[]{id, "Otro", "1", "1", "1"}, null, 100));
        check(err != null && err.contains("23505"), "CU-INV-02 código duplicado: " + err);
        err = error(() -> p.insertarProductoConCodigoYCategoria(new String[]{"a b", "X", "1", "1", "1"}, null, 100));
        check(err != null, "CU-INV-02 código con espacio rechazado");

        // CU-INV-03 código de barras: resolución y duplicado
        p.actualizarCamposCompraProducto(id, "7501234567890", false, "Pieza", "Pieza", 1);
        check(id.equals(p.resolverCodigo("7501234567890")) && id.equals(p.resolverCodigo(id)), "CU-INV-03 resolver por código de barras o id");
        check(p.codigoBarrasDuplicado("7501234567890", "OTRO") && !p.codigoBarrasDuplicado("7501234567890", id), "CU-INV-03 duplicado");
        check(p.resolverCodigo("0000000") == null, "CU-INV-03 código inexistente");

        // CU-INV-04 editar (precios, categoría) sin tocar stock
        p.actualizarProductoConCategoria(id, new String[]{"Martillo grande", "10", "85", "110"}, null, 20);
        check(num("SELECT precio_menudeo FROM producto WHERE id_producto=?", id) == 110.0 && p.obtenerMaxDescuento(id) == 20.0, "CU-INV-04 editado");

        // CU-INV-05 ajuste manual de existencias desde la edición debe quedar en kardex (no como venta)
        p.actualizarProductoConCategoria(id, new String[]{"Martillo grande", "7", "85", "110"}, null, 20);
        check(entero("SELECT count(*) FROM kardex WHERE id_producto=? AND tipo_movimiento='Venta'", id) == 0,
                "CU-INV-05 bajar existencias a mano se registra en kardex como 'Venta'");
        p.actualizarProductoConCategoria(id, new String[]{"Martillo grande", "12", "85", "110"}, null, 20);
        check(descuadreKardex(id, 10) == null, "CU-INV-05 subir existencias a mano no deja rastro en kardex: " + descuadreKardex(id, 10));

        // CU-INV-06 baja lógica: con stock se rechaza; con 0 se permite; reactivar
        err = error(() -> p.eliminarProducto(id));
        check(err != null && err.contains("stock mayor a 0"), "CU-INV-06 baja con stock: " + err);
        exec("UPDATE producto SET cantidad=0 WHERE id_producto=?", id);
        check(p.eliminarProducto(id) && "Inactivo".equals(p.estadoProducto(id)), "CU-INV-06 baja con stock 0");
        check(VentasPruebaBD.cuenta(p.buscarProductosActivos(id)) == 0, "CU-INV-06 no aparece en activos");
        check(VentasPruebaBD.cuenta(p.obtenerProductosInactivos()) >= 1, "CU-INV-06 aparece en dados de baja");
        check(p.reactivarProducto(id) && "Activo".equals(p.estadoProducto(id)), "CU-INV-06 reactivar");

        // CU-INV-07 archivar libera el código para reutilizarlo y conserva el historial
        String archivado = p.archivarProducto(id);
        check(archivado.startsWith(id + "_B") && p.estadoProducto(id) == null, "CU-INV-07 archivado como " + archivado);
        p.insertarProductoConCodigoYCategoria(new String[]{id, "Martillo nuevo", "1", "1", "2"}, null, 100);
        check("Activo".equals(p.estadoProducto(id)), "CU-INV-07 código reutilizado");
        check(p.resolverCodigo("7501234567890") == null || !p.resolverCodigo("7501234567890").equals(id),
                "CU-INV-07 el código de barras sigue en el archivado");

        // CU-INV-08 stock bajo (<= 12)
        check(p.hayPocosProductos(), "CU-INV-08 hay productos con poco stock");
        check(VentasPruebaBD.cuenta(p.obtenerProductosPocos()) >= 1, "CU-INV-08 listado de stock bajo");

        // CU-INV-09 producto creado desde compra con todos los campos
        String idc = "TINVC" + siguiente();
        check(p.crearProductoDesdeCompra(new String[]{idc, "Clavos", "", "Caja", "Pieza", "100", "t", "1.5", "1.2", "0.5"}, idCat, 5),
                "CU-INV-09 crear desde compra");
        String[] d = p.obtenerProductoParaCompra(idc);
        check(d != null && "Caja".equals(d[2]) && "t".equals(d[5]), "CU-INV-09 datos para compra");
        check(p.obtenerPrecioCompra(idc) == 0.5 && p.obtenerPrecioCompra("NOEXISTE") == null, "CU-INV-09 precio de compra");
    }

    static void categorias() {
        CategoriaDAO c = new CategoriaDAO();
        String n = "Herramientas " + siguiente();
        check(c.insertarCategoria(n, "desc"), "CU-CAT-01 alta");
        int id = entero("SELECT id_categoria FROM categoria WHERE nombre=?", n);
        check(c.editarCategoria(id, n + " X", ""), "CU-CAT-02 editar");
        check(VentasPruebaBD.cuenta(c.buscarCategoriasPorNombre(n + " X")) == 1, "CU-CAT-02 buscar");
        String err = error(() -> c.insertarCategoria(n + " X", ""));
        check(err != null, "CU-CAT-03 se pueden crear dos categorías con el mismo nombre");
        check(c.cambiarEstatusCategoria(id, "Inactivo"), "CU-CAT-04 inactivar");
        boolean enCombo = false;
        try { java.sql.ResultSet r = c.obtenerCategorias(); while (r.next()) enCombo |= r.getInt(1) == id; } catch (Exception e) {}
        check(!enCombo, "CU-CAT-04 inactiva no aparece en combos");
    }

    static void clientes() {
        ClienteDAO c = new ClienteDAO();
        String curp = curp(), tel = telefono();
        check(c.insertarCliente(new String[]{curp, "Cliente Prueba", tel}), "CU-CLI-01 alta");
        String err = error(() -> c.insertarCliente(new String[]{curp(), "Otro", tel}));
        check(err != null && err.contains("único número"), "CU-CLI-02 teléfono repetido: " + err);
        err = error(() -> c.insertarCliente(new String[]{"CURPMALA", "X", telefono()}));
        check(err != null, "CU-CLI-02 CURP inválida");
        err = error(() -> c.insertarCliente(new String[]{curp(), "X", "123"}));
        check(err != null, "CU-CLI-02 teléfono de menos de 10 dígitos");
        String tel2 = telefono();
        c.actualizarCliente(new String[]{curp, "Cliente Editado", tel2}, curp);
        check("Cliente Editado".equals(texto("SELECT nombre FROM cliente WHERE id_cliente=?", curp)), "CU-CLI-03 editar");
        c.inactivarCliente(curp, true);
        check(VentasPruebaBD.cuenta(c.buscarClientesActivosPorId(curp)) == 0, "CU-CLI-04 baja");
        c.inactivarCliente(curp, false);
        check(VentasPruebaBD.cuenta(c.buscarClientesActivos("Cliente Editado")) >= 1, "CU-CLI-04 alta de nuevo");
        err = error(() -> c.inactivarCliente(CLIENTE_GENERICO, true));
        check(err != null && err.contains("Genérico"), "CU-CLI-05 no se puede dar de baja al Cliente Genérico: " + err);
    }

    static void empleados() {
        EmpleadoDAO e = new EmpleadoDAO();
        String curp = curp(), usuario = "vend" + siguiente();
        check(!e.verificarUsuario(usuario), "CU-EMP-01 usuario libre");
        check(e.insertarEmpleado(new String[]{curp, "Vendedor Prueba", "vendedor", telefono(), usuario, "clave1"}), "CU-EMP-01 alta vendedor");
        check(e.verificarUsuario(usuario), "CU-EMP-01 usuario ocupado");

        // CU-EMP-02 login correcto / incorrecto / inactivo
        String[] datos = e.idEmpleado(usuario, "clave1");
        check(curp.equals(datos[0]) && "vendedor".equals(datos[2]), "CU-EMP-02 login correcto");
        String err = error(() -> e.idEmpleado(usuario, "mala"));
        check(err != null && err.contains("Contraseña incorrecta"), "CU-EMP-02 contraseña incorrecta: " + err);
        check(e.inactivarEmpleado(curp, true), "CU-EMP-03 baja");
        err = error(() -> e.idEmpleado(usuario, "clave1"));
        check(err != null, "CU-EMP-03 empleado dado de baja no puede entrar");
        boolean enCombo = false;
        try { java.sql.ResultSet r = e.obtenerUsuariosActivos(); while (r.next()) enCombo |= usuario.equals(r.getString(1)); } catch (Exception ex) {}
        check(!enCombo, "CU-EMP-03 no aparece en el combo del login");
        check(e.inactivarEmpleado(curp, false) && e.idEmpleado(usuario, "clave1")[0] != null, "CU-EMP-03 reactivar y entrar");
        check(e.actualizarEmpleado(new String[]{"gerente", telefono()}, curp), "CU-EMP-04 ascender a gerente");
        check(e.idEmpleado("admin", "admin")[0] != null, "CU-EMP-05 login admin/admin del seed");
    }

    static void empresaGastosGanancias() {
        EmpresaDAO em = new EmpresaDAO();
        check(em.actualizarEmpresa("Ferretería Prueba", "", "XAXX010101000", "5512345678", "", "Av. 1", "Oaxaca", "", "68000", "Gracias", ""),
                "CU-EMPR-01 guardar datos");
        check(em.empresaInicializada(), "CU-EMPR-01 empresa inicializada");
        String err = error(() -> em.actualizarEmpresa("X", "", "RFC-DEMASIADO-LARGO", "", "", "", "", "", "", "", ""));
        check(err != null, "CU-EMPR-02 RFC de más de 13 caracteres");

        GastoDAO g = new GastoDAO();
        String desc = "Luz " + siguiente();
        g.insertarGasto(new String[]{ADMIN, desc, "350.5"});
        String idG = texto("SELECT id_gasto FROM gastos WHERE descripcion=?", desc);
        check(idG != null, "CU-GAS-01 alta");
        g.actualizarGasto(idG, new String[]{desc, "400"});
        check(num("SELECT monto FROM gastos WHERE id_gasto=?", idG) == 400.0, "CU-GAS-02 editar");
        check(g.eliminarGasto(idG) && entero("SELECT count(*) FROM gastos WHERE id_gasto=?", idG) == 0, "CU-GAS-03 eliminar");

        GananciaDAO o = new GananciaDAO();
        String d2 = "Renta bodega " + siguiente();
        o.insertarOtraGanancia(new String[]{d2, "1000"});
        String idO = texto("SELECT id_otras_ganancias FROM otras_ganancias WHERE descripcion=?", d2);
        o.actualizarOtraGanancia(new String[]{d2, "1200", idO});
        check(num("SELECT monto FROM otras_ganancias WHERE id_otras_ganancias=?", idO) == 1200.0, "CU-GAN-01 alta y edición");
        check(o.eliminarOtraGanancia(idO), "CU-GAN-02 eliminar");
    }
}
