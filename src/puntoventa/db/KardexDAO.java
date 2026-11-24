package puntoventa.db;

import puntoventa.util.*;
import java.sql.*;
import java.time.LocalDate;

/**
 * DAO del kardex: búsqueda de productos, movimientos de un periodo con saldo
 * inicial y detalle del documento (venta, compra, apartado o devolución) de un folio.
 */
public class KardexDAO extends BaseDAO {

    /** Busca por nombre, código interno o código de barras (exacto). */
    public ResultSet buscarProductos(String filtro, boolean incluirBaja) {
        String patron = "%" + filtro.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_") + "%";
        return consultarONulo(
            "SELECT id_producto, nombre, cantidad, estatus FROM producto " +
            "WHERE (estatus = 'Activo' OR ?) " +
            "AND (LOWER(nombre) LIKE LOWER(?) OR LOWER(id_producto) LIKE LOWER(?) OR codigo_barras = ?) " +
            "ORDER BY (estatus = 'Activo') DESC, nombre LIMIT 30",
            incluirBaja, patron, patron, filtro);
    }

    /** Movimientos del periodo [desde, hasta] (días completos), en el orden en que ocurrieron. */
    public KardexPeriodo consultar(String idProducto, LocalDate desde, LocalDate hasta) {
        KardexPeriodo p = new KardexPeriodo(idProducto, desde, hasta);
        Timestamp ini = Timestamp.valueOf(desde.atStartOfDay());
        Timestamp fin = Timestamp.valueOf(hasta.plusDays(1).atStartOfDay());
        try {
            ResultSet rs = consultar("SELECT cantidad FROM producto WHERE id_producto = ?", idProducto);
            p.existenciaActual = rs.next() ? rs.getInt(1) : 0;

            rs = consultar(
                "SELECT k.id_kardex, k.fecha, k.tipo_movimiento, k.cantidad, " +
                "k.existencia_anterior, k.existencia_posterior, k.referencia, k.observacion, " +
                "COALESCE(e.nombre, k.id_empleado) AS empleado " +
                "FROM kardex k LEFT JOIN empleado e ON e.id_empleado = k.id_empleado " +
                "WHERE k.id_producto = ? AND k.fecha >= ? AND k.fecha < ? " +
                "ORDER BY k.id_kardex", idProducto, ini, fin);
            while (rs.next()) {
                KardexPeriodo.Movimiento m = new KardexPeriodo.Movimiento();
                m.id = rs.getLong("id_kardex");
                m.fecha = rs.getTimestamp("fecha").toLocalDateTime();
                m.tipo = rs.getString("tipo_movimiento");
                m.cantidad = rs.getInt("cantidad");
                m.existenciaAnterior = rs.getInt("existencia_anterior");
                m.existenciaPosterior = rs.getInt("existencia_posterior");
                m.referencia = rs.getString("referencia");
                m.empleado = rs.getString("empleado");
                m.observacion = rs.getString("observacion");
                p.movimientos.add(m);
            }

            // Saldo al iniciar el periodo: el último saldo registrado antes de 'desde'.
            rs = consultar(
                "SELECT existencia_posterior FROM kardex WHERE id_producto = ? AND fecha < ? " +
                "ORDER BY id_kardex DESC LIMIT 1", idProducto, ini);
            if (rs.next()) {
                p.saldoInicial = rs.getInt(1);
            } else if (!p.movimientos.isEmpty()) {
                p.saldoInicial = p.movimientos.get(0).existenciaAnterior;
            } else {
                // Sin movimientos antes ni durante: el saldo es el anterior al siguiente movimiento, o la existencia.
                rs = consultar(
                    "SELECT existencia_anterior FROM kardex WHERE id_producto = ? AND fecha >= ? " +
                    "ORDER BY id_kardex LIMIT 1", idProducto, fin);
                p.saldoInicial = rs.next() ? rs.getInt(1) : p.existenciaActual;
            }
            p.calcular();
        } catch (SQLException e) {
            GestorErrores.manejar(e);
        }
        return p;
    }

    /** Detalle del documento de un folio del kardex, o null si no se encuentra. */
    public DocumentoKardex documento(String folio) {
        if (folio == null || folio.isEmpty()) return null;
        try {
            switch (Character.toUpperCase(folio.charAt(0))) {
                case 'V': return venta(folio);
                case 'C': return compra(folio);
                case 'A': return apartado(folio);
                case 'D': return devolucion(folio);
                default:  return null;
            }
        } catch (SQLException e) {
            GestorErrores.manejar(e);
            return null;
        }
    }

    private DocumentoKardex venta(String folio) throws SQLException {
        ResultSet rs = consultar(
            "SELECT v.fecha_venta, COALESCE(e.nombre, v.id_empleado) AS emp, " +
            "COALESCE(c.nombre, v.id_cliente) AS cli, COALESCE(v.forma_pago, 'Efectivo') AS fp, v.total_venta " +
            "FROM venta v LEFT JOIN empleado e ON e.id_empleado = v.id_empleado " +
            "LEFT JOIN cliente c ON c.id_cliente = v.id_cliente WHERE v.id_venta = ?", folio);
        if (!rs.next()) return null;
        DocumentoKardex d = new DocumentoKardex();
        d.titulo = "Venta " + folio;
        d.datos.add(new String[]{"Fecha", texto(rs.getDate("fecha_venta"))});
        d.datos.add(new String[]{"Empleado", rs.getString("emp")});
        d.datos.add(new String[]{"Cliente", rs.getString("cli")});
        d.datos.add(new String[]{"Forma de pago", rs.getString("fp")});
        d.datos.add(new String[]{"Total", dinero(rs.getDouble("total_venta"))});
        d.columnas = new String[]{"Código", "Producto", "Cantidad", "Precio", "Desc. %", "Importe"};
        rs = consultar(
            "SELECT d.id_producto, COALESCE(p.nombre, '') AS nombre, d.cantidad_producto, d.precio_dado, " +
            "COALESCE(d.descuento_pct, 0) AS dct, d.precio_total FROM venta_detalle d " +
            "LEFT JOIN producto p ON p.id_producto = d.id_producto WHERE d.id_venta = ? ORDER BY d.id_producto", folio);
        while (rs.next()) {
            d.filas.add(new Object[]{rs.getString(1), rs.getString(2), rs.getInt(3),
                dinero(rs.getDouble(4)), String.format("%.2f", rs.getDouble(5)), dinero(rs.getDouble(6))});
        }
        return d;
    }

    private DocumentoKardex compra(String folio) throws SQLException {
        ResultSet rs = consultar(
            "SELECT c.fecha_compra, COALESCE(e.nombre, c.id_empleado) AS emp, COALESCE(pr.nombre, '') AS prov, " +
            "COALESCE(c.folio_proveedor, '') AS folio_prov, COALESCE(c.descripcion, '') AS des, c.monto " +
            "FROM compras c LEFT JOIN empleado e ON e.id_empleado = c.id_empleado " +
            "LEFT JOIN proveedor pr ON pr.id_proveedor = c.id_proveedor WHERE c.id_compra = ?", folio);
        if (!rs.next()) return null;
        DocumentoKardex d = new DocumentoKardex();
        d.titulo = "Compra " + folio;
        d.datos.add(new String[]{"Fecha", texto(rs.getDate("fecha_compra"))});
        d.datos.add(new String[]{"Empleado", rs.getString("emp")});
        d.datos.add(new String[]{"Proveedor", rs.getString("prov")});
        d.datos.add(new String[]{"Folio proveedor", rs.getString("folio_prov")});
        d.datos.add(new String[]{"Descripción", rs.getString("des")});
        d.datos.add(new String[]{"Monto", dinero(rs.getDouble("monto"))});
        d.columnas = new String[]{"Código", "Producto", "Cantidad", "Factor", "Precio", "Importe"};
        rs = consultar(
            "SELECT cp.id_producto, COALESCE(p.nombre, '') AS nombre, cp.cantidad, COALESCE(cp.factor_conversion, 1), " +
            "cp.precio_adquirido, cp.precio_total FROM compra_producto cp " +
            "LEFT JOIN producto p ON p.id_producto = cp.id_producto WHERE cp.id_compra = ? ORDER BY cp.id_producto", folio);
        while (rs.next()) {
            d.filas.add(new Object[]{rs.getString(1), rs.getString(2), rs.getInt(3),
                rs.getBigDecimal(4).stripTrailingZeros().toPlainString(), dinero(rs.getDouble(5)), dinero(rs.getDouble(6))});
        }
        return d;
    }

    private DocumentoKardex apartado(String folio) throws SQLException {
        ResultSet rs = consultar(
            "SELECT a.fecha_inicio, a.fecha_limite, a.estado::text AS estado, COALESCE(e.nombre, a.id_empleado) AS emp, " +
            "COALESCE(c.nombre, a.id_cliente) AS cli, a.cantidad_total, a.cantidad_dada, a.cantidad_faltante " +
            "FROM apartado a LEFT JOIN empleado e ON e.id_empleado = a.id_empleado " +
            "LEFT JOIN cliente c ON c.id_cliente = a.id_cliente WHERE a.id_apartado = ?", folio);
        if (!rs.next()) return null;
        DocumentoKardex d = new DocumentoKardex();
        d.titulo = "Apartado " + folio;
        d.datos.add(new String[]{"Periodo", texto(rs.getDate("fecha_inicio")) + " al " + texto(rs.getDate("fecha_limite"))});
        d.datos.add(new String[]{"Estado", rs.getString("estado")});
        d.datos.add(new String[]{"Empleado", rs.getString("emp")});
        d.datos.add(new String[]{"Cliente", rs.getString("cli")});
        d.datos.add(new String[]{"Total", dinero(rs.getDouble("cantidad_total"))});
        d.datos.add(new String[]{"Abonado", dinero(rs.getDouble("cantidad_dada"))});
        d.datos.add(new String[]{"Faltante", dinero(rs.getDouble("cantidad_faltante"))});
        d.columnas = new String[]{"Código", "Producto", "Cantidad", "Precio", "Importe"};
        rs = consultar(
            "SELECT d.id_producto, COALESCE(p.nombre, '') AS nombre, d.cantidad, d.precio_unitario, d.precio_total " +
            "FROM apartado_detalle d LEFT JOIN producto p ON p.id_producto = d.id_producto " +
            "WHERE d.id_apartado = ? ORDER BY d.id_producto", folio);
        while (rs.next()) {
            d.filas.add(new Object[]{rs.getString(1), rs.getString(2), rs.getInt(3),
                dinero(rs.getDouble(4)), dinero(rs.getDouble(5))});
        }
        return d;
    }

    private DocumentoKardex devolucion(String folio) throws SQLException {
        ResultSet rs = consultar(
            "SELECT id_venta, fecha_devolucion, monto FROM devolucion_ventas WHERE id_devolucion = ?", folio);
        if (!rs.next()) return null;
        DocumentoKardex d = new DocumentoKardex();
        d.titulo = "Devolución " + folio;
        d.datos.add(new String[]{"Venta", rs.getString("id_venta")});
        d.datos.add(new String[]{"Fecha", texto(rs.getDate("fecha_devolucion"))});
        d.datos.add(new String[]{"Monto", dinero(rs.getDouble("monto"))});
        d.columnas = new String[]{"Código", "Producto", "Cantidad", "Motivo", "Importe"};
        rs = consultar(
            "SELECT d.id_producto, COALESCE(p.nombre, '') AS nombre, d.cantidad, COALESCE(d.motivo, ''), d.total " +
            "FROM devolucion_ventas_detalle d LEFT JOIN producto p ON p.id_producto = d.id_producto " +
            "WHERE d.id_devolucion = ? ORDER BY d.id_producto", folio);
        while (rs.next()) {
            d.filas.add(new Object[]{rs.getString(1), rs.getString(2), rs.getInt(3),
                rs.getString(4), dinero(rs.getDouble(5))});
        }
        return d;
    }

    private static String texto(java.sql.Date f) {
        return f == null ? "" : f.toLocalDate().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    private static String dinero(double v) {
        return String.format("$%,.2f", v);
    }
}
