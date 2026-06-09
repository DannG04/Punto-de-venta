import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoWSD;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.sql.*;
import java.util.*;
import java.security.KeyStore;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocketFactory;

/**
 * Servidor HTTPS embebido para registro de inventario desde dispositivos móviles.
 * Accesible en https://<IP-local>:8765 desde cualquier dispositivo en la misma red.
 * HTTPS es necesario para que el navegador móvil permita acceso a la cámara en vivo.
 * Usa WebSocket (NanoWSD) para consultas de productos en tiempo real.
 */
public class WebInventario extends NanoWSD {

    public static final int PUERTO = 8765;

    // Mismos parámetros que ConexionBD
    private final String url = "jdbc:postgresql://localhost:5432/";
    private final String nameBD = "punto_de_venta";
    private final String usuario = "postgres";
    private final String contra = "root";

    private Connection conn = null;

    // synchronized: pgjdbc Connection NO es thread-safe. El hilo del WebSocket y los
    // hilos del pool HTTP de NanoHTTPD comparten esta misma conexión, y al cargar la
    // página varias consultas (categorias/listas por WS y por fetch) coinciden. Sin
    // serializar, el acceso concurrente corrompe/reinicia la conexión a mitad de una
    // operación, se pierde la respuesta y la pantalla de stock queda colgada.
    private synchronized Connection getConn() throws SQLException {
        // isValid(2) hace un ping real a la BD; detecta conexiones muertas por inactividad
        // que isClosed() no detecta (es solo un flag local del driver)
        if (conn == null || conn.isClosed() || !conn.isValid(2)) {
            try { if (conn != null) conn.close(); } catch (Exception ignored) {}
            conn = DriverManager.getConnection(url + nameBD, usuario, contra);
        }
        return conn;
    }

    public WebInventario() throws IOException {
        super(PUERTO);
        // NanoHTTPD 2.3.1 llama InetAddress.getHostName() en HTTPSession para toda IP
        // que no sea loopback. En LAN sin DNS inverso, Java espera el timeout del SO
        // (~10-15s) por cada IP nueva. La primera conexión de un dispositivo será lenta;
        // cacheamos el resultado negativo 1 hora para que las siguientes sean instantáneas.
        java.security.Security.setProperty("networkaddress.cache.negative.ttl", "3600");
        System.setProperty("java.net.preferIPv4Stack", "true");
        // Silencia los SocketException de NanoHTTPD (browser cierra conexión TLS antes
        // de recibir respuesta completa — inofensivo pero satura el log)
        java.util.logging.Filter filtroSocket = record -> {
            Throwable t = record.getThrown();
            return !(t instanceof java.net.SocketException);
        };
        java.util.logging.Logger.getLogger("fi.iki.elonen.NanoHTTPD").setFilter(filtroSocket);
        java.util.logging.Logger.getLogger("fi.iki.elonen.NanoWSD").setFilter(filtroSocket);
        java.util.logging.Logger.getLogger("fi.iki.elonen").setFilter(filtroSocket);
        try {
            // Cargar keystore autofirmado para HTTPS
            java.nio.file.Path ksPath = java.nio.file.Paths.get(
                System.getProperty("user.dir"), "keystore.jks");
            char[] pass = "puntodeventa".toCharArray();
            KeyStore ks = KeyStore.getInstance("JKS");
            try (java.io.FileInputStream fis = new java.io.FileInputStream(ksPath.toFile())) {
                ks.load(fis, pass);
            }
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(
                KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(ks, pass);
            SSLContext ctx = SSLContext.getInstance("TLS");
            ctx.init(kmf.getKeyManagers(), null, null);
            SSLServerSocketFactory sslFactory = ctx.getServerSocketFactory();
            makeSecure(sslFactory, null);
        } catch (Exception e) {
            System.err.println("[WebInventario] No se pudo activar HTTPS: " + e.getMessage());
        }
    }

    // InetAddress.getByAddress(host,addr) fija el hostname a la cadena IP sin consulta DNS,
    // eliminando el timeout de resolución inversa (~10-15 s) en la primera conexión de cada dispositivo.
    @Override
    protected ClientHandler createClientHandler(final Socket socket, final InputStream inputStream) {
        InetAddress original = socket.getInetAddress();
        if (original == null || original.isLoopbackAddress()) {
            return super.createClientHandler(socket, inputStream);
        }
        try {
            final InetAddress sinDNS = InetAddress.getByAddress(
                original.getHostAddress(), original.getAddress());
            Socket wrapper = new Socket() {
                @Override public java.io.OutputStream getOutputStream() throws java.io.IOException { return socket.getOutputStream(); }
                @Override public InetAddress getInetAddress() { return sinDNS; }
                @Override public boolean isClosed() { return socket.isClosed(); }
                @Override public void close() throws java.io.IOException { socket.close(); }
                @Override public boolean isInputShutdown() { return socket.isInputShutdown(); }
                @Override public boolean isOutputShutdown() { return socket.isOutputShutdown(); }
            };
            return super.createClientHandler(wrapper, inputStream);
        } catch (Exception e) {
            return super.createClientHandler(socket, inputStream);
        }
    }

    /** Devuelve la IP local de la máquina en la red LAN (excluye VPNs, Docker y bridges) */
    public static String obtenerIPLocal() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface ni = interfaces.nextElement();
                if (ni.isLoopback() || !ni.isUp()) continue;
                // Excluir VPNs (Tailscale, OpenVPN), Docker bridges y interfaces virtuales
                if (ni.isPointToPoint()) continue;
                String nombre = ni.getName().toLowerCase();
                if (nombre.startsWith("docker") || nombre.startsWith("br-")
                        || nombre.startsWith("veth") || nombre.startsWith("virbr")
                        || nombre.startsWith("tun") || nombre.startsWith("tap")) continue;
                Enumeration<InetAddress> addrs = ni.getInetAddresses();
                while (addrs.hasMoreElements()) {
                    InetAddress addr = addrs.nextElement();
                    if (addr instanceof java.net.Inet4Address && !addr.isLinkLocalAddress()) {
                        return addr.getHostAddress();
                    }
                }
            }
        } catch (Exception e) {
            // fallback
        }
        return "localhost";
    }

    // -------------------------------------------------------------------------
    // WebSocket: consulta de productos en tiempo real
    // -------------------------------------------------------------------------
    @Override
    protected WebSocket openWebSocket(IHTTPSession handshake) {
        return new BuscarWS(handshake);
    }

    private class BuscarWS extends NanoWSD.WebSocket {
        public BuscarWS(IHTTPSession handshakeRequest) {
            super(handshakeRequest);
        }

        @Override
        protected void onOpen() {}

        @Override
        protected void onClose(NanoWSD.WebSocketFrame.CloseCode code, String reason,
                               boolean initiatedByRemote) {}

        @Override
        protected void onMessage(NanoWSD.WebSocketFrame message) {
            String texto = message.getTextPayload();
            if (texto == null || texto.isBlank() || "ping".equals(texto.trim())) return;
            texto = texto.trim();
            try {
                if (texto.equals("categorias")) {
                    send(obtenerCategoriasJson());
                } else if (texto.equals("listas")) {
                    send(obtenerListasJson());
                } else if (texto.startsWith("stockbajo:")) {
                    int umbral = parsearInt(texto.substring(10), 5);
                    send(stockBajoJson(umbral));
                } else if (texto.startsWith("editar:")) {
                    send(editarProductoJson(texto.substring(7)));
                } else if (texto.startsWith("{")) {
                    // JSON → registro de producto
                    send(registrarProductoJson(texto));
                } else {
                    // Texto plano → consulta por código de barras
                    send(consultarProductoJson(texto));
                }
            } catch (Exception e) {
                try { send("{\"error\":\"Error interno\"}"); } catch (IOException ignored) {}
            }
        }

        @Override
        protected void onPong(NanoWSD.WebSocketFrame pong) {}

        @Override
        protected void onException(IOException exception) {}
    }

    /** Consulta un producto por código y devuelve JSON */
    private synchronized String consultarProductoJson(String codigo) {
        try {
            PreparedStatement ps = getConn().prepareStatement(
                "SELECT p.id_producto, p.nombre, p.cantidad, p.precio_mayoreo, p.precio_menudeo, "
                + "COALESCE(c.nombre,'Sin categoría') AS categoria, p.max_descuento, "
                + "COALESCE(p.codigo_barras,'') AS codigo_barras, COALESCE(p.lleva_iva,false) AS lleva_iva, "
                + "COALESCE(p.unidad_compra,'') AS unidad_compra, COALESCE(p.unidad_venta,'') AS unidad_venta, "
                + "COALESCE(p.factor_conversion,1) AS factor_conversion, COALESCE(p.precio_compra,0) AS precio_compra, "
                + "p.id_categoria "
                + "FROM producto p LEFT JOIN categoria c ON p.id_categoria=c.id_categoria "
                + "WHERE p.estatus = 'Activo' AND p.id_producto=?");
            ps.setString(1, codigo);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                return "{\"error\":\"Producto no encontrado\"}";
            }
            int idCategoria = rs.getInt("id_categoria");
            if (rs.wasNull()) idCategoria = 0;
            return "{"
                + "\"id_producto\":\"" + escaparJson(rs.getString("id_producto")) + "\","
                + "\"nombre\":\"" + escaparJson(rs.getString("nombre")) + "\","
                + "\"cantidad\":" + rs.getInt("cantidad") + ","
                + "\"precio_mayoreo\":" + rs.getDouble("precio_mayoreo") + ","
                + "\"precio_menudeo\":" + rs.getDouble("precio_menudeo") + ","
                + "\"categoria\":\"" + escaparJson(rs.getString("categoria")) + "\","
                + "\"id_categoria\":" + idCategoria + ","
                + "\"max_descuento\":" + rs.getDouble("max_descuento") + ","
                + "\"codigo_barras\":\"" + escaparJson(rs.getString("codigo_barras")) + "\","
                + "\"lleva_iva\":" + rs.getBoolean("lleva_iva") + ","
                + "\"unidad_compra\":\"" + escaparJson(rs.getString("unidad_compra")) + "\","
                + "\"unidad_venta\":\"" + escaparJson(rs.getString("unidad_venta")) + "\","
                + "\"factor_conversion\":" + rs.getDouble("factor_conversion") + ","
                + "\"precio_compra\":" + rs.getDouble("precio_compra") + ","
                + "\"precios\":" + preciosProductoJson(codigo)
                + "}";
        } catch (SQLException e) {
            return "{\"error\":\"" + escaparJson(e.getMessage()) + "\"}";
        }
    }

    /**
     * Devuelve los precios por lista de un producto como JSON array.
     * producto_precio actúa como override opcional: si no hay fila para la lista,
     * las listas de fábrica "Menudeo"/"Mayoreo" caen a las columnas del producto.
     */
    private synchronized String preciosProductoJson(String codigo) {
        StringBuilder json = new StringBuilder("[");
        try {
            PreparedStatement ps = getConn().prepareStatement(
                "SELECT lp.id_lista, lp.nombre, "
                + "COALESCE(pp.precio, CASE lp.nombre "
                + "    WHEN 'Menudeo' THEN p.precio_menudeo "
                + "    WHEN 'Mayoreo' THEN p.precio_mayoreo END) AS precio "
                + "FROM lista_precios lp "
                + "JOIN producto p ON p.id_producto = ? "
                + "LEFT JOIN producto_precio pp ON pp.id_producto = p.id_producto AND pp.id_lista = lp.id_lista "
                + "WHERE lp.estatus = 'Activo' ORDER BY lp.id_lista");
            ps.setString(1, codigo);
            ResultSet rs = ps.executeQuery();
            boolean primero = true;
            while (rs.next()) {
                if (!primero) json.append(",");
                json.append("{\"id\":").append(rs.getInt("id_lista"))
                    .append(",\"nombre\":\"").append(escaparJson(rs.getString("nombre")))
                    .append("\",\"precio\":").append(rs.getDouble("precio")).append("}");
                primero = false;
            }
        } catch (SQLException e) {
            return "[]";
        }
        json.append("]");
        return json.toString();
    }

    /** Devuelve las listas de precios activas como JSON array */
    private synchronized String obtenerListasJson() {
        StringBuilder json = new StringBuilder("[");
        try {
            PreparedStatement ps = getConn().prepareStatement(
                "SELECT id_lista, nombre FROM lista_precios WHERE estatus='Activo' ORDER BY id_lista");
            ResultSet rs = ps.executeQuery();
            boolean primero = true;
            while (rs.next()) {
                if (!primero) json.append(",");
                json.append("{\"id\":").append(rs.getInt(1))
                    .append(",\"nombre\":\"").append(escaparJson(rs.getString(2))).append("\"}");
                primero = false;
            }
        } catch (SQLException e) {
            return "[{\"error\":\"" + escaparJson(e.getMessage()) + "\"}]";
        }
        json.append("]");
        return json.toString();
    }

    /** Devuelve las categorías activas como JSON array */
    private synchronized String obtenerCategoriasJson() {
        StringBuilder json = new StringBuilder("[");
        try {
            PreparedStatement ps = getConn().prepareStatement(
                "SELECT id_categoria, nombre FROM categoria WHERE estatus='Activo' ORDER BY nombre");
            ResultSet rs = ps.executeQuery();
            boolean primero = true;
            while (rs.next()) {
                if (!primero) json.append(",");
                json.append("{\"id\":").append(rs.getInt(1))
                    .append(",\"nombre\":\"").append(escaparJson(rs.getString(2))).append("\"}");
                primero = false;
            }
        } catch (SQLException e) {
            return "[{\"error\":\"" + escaparJson(e.getMessage()) + "\"}]";
        }
        json.append("]");
        return json.toString();
    }

    /** Registra un producto desde JSON y devuelve resultado */
    private synchronized String registrarProductoJson(String cuerpo) {
        try {
            String codigo   = extraerJson(cuerpo, "codigo");
            String nombre   = extraerJson(cuerpo, "nombre");
            if (codigo.isBlank() || nombre.isBlank()) {
                return "{\"error\":\"El código y el nombre son obligatorios\"}";
            }
            int    cant     = parsearInt(extraerJson(cuerpo, "cantidad"), 0);
            double pMayoreo = parsearDouble(extraerJson(cuerpo, "precioMayoreo"), 0.0);
            double pMenudeo = parsearDouble(extraerJson(cuerpo, "precioMenudeo"), 0.0);
            int    idCat    = parsearInt(extraerJson(cuerpo, "idCategoria"), 0);
            double maxDesc  = parsearDouble(extraerJson(cuerpo, "maxDescuento"), 100.0);
            String codBarras = extraerJson(cuerpo, "codigoBarras").trim();

            if (!codBarras.isEmpty() && codigoBarrasDuplicado(codBarras, codigo)) {
                return "{\"error\":\"El código de barras ya está en uso por otro producto\"}";
            }

            PreparedStatement ps = getConn().prepareStatement(
                "INSERT INTO producto(id_producto,nombre,cantidad,precio_mayoreo,precio_menudeo,id_categoria,max_descuento) VALUES(?,?,?,?,?,?,?)");
            ps.setString(1, codigo);
            ps.setString(2, nombre);
            ps.setInt(3, cant);
            ps.setDouble(4, pMayoreo);
            ps.setDouble(5, pMenudeo);
            if (idCat > 0) ps.setInt(6, idCat); else ps.setNull(6, Types.INTEGER);
            ps.setDouble(7, maxDesc);
            ps.executeUpdate();

            guardarCamposCompra(cuerpo, codigo);
            guardarPreciosLista(cuerpo, codigo);
            return "{\"ok\":true}";
        } catch (SQLException e) {
            String msg = e.getMessage() != null ? e.getMessage() : "Error de base de datos";
            if (msg.contains("duplicate key") || msg.contains("already exists")) {
                msg = "Ya existe un producto con ese código";
            }
            return "{\"error\":\"" + escaparJson(msg) + "\"}";
        }
    }

    /** Actualiza un producto existente desde JSON */
    private synchronized String editarProductoJson(String cuerpo) {
        try {
            String codigo   = extraerJson(cuerpo, "codigo");
            String nombre   = extraerJson(cuerpo, "nombre");
            if (codigo.isBlank() || nombre.isBlank()) {
                return "{\"error\":\"El código y el nombre son obligatorios\"}";
            }
            int    cant     = parsearInt(extraerJson(cuerpo, "cantidad"), 0);
            double pMayoreo = parsearDouble(extraerJson(cuerpo, "precioMayoreo"), 0.0);
            double pMenudeo = parsearDouble(extraerJson(cuerpo, "precioMenudeo"), 0.0);
            int    idCat    = parsearInt(extraerJson(cuerpo, "idCategoria"), 0);
            double maxDesc  = parsearDouble(extraerJson(cuerpo, "maxDescuento"), 100.0);
            String codBarras = extraerJson(cuerpo, "codigoBarras").trim();

            if (!codBarras.isEmpty() && codigoBarrasDuplicado(codBarras, codigo)) {
                return "{\"error\":\"El código de barras ya está en uso por otro producto\"}";
            }

            PreparedStatement ps = getConn().prepareStatement(
                "UPDATE producto SET nombre=?,cantidad=?,precio_mayoreo=?,precio_menudeo=?,id_categoria=?,max_descuento=? WHERE id_producto=?");
            ps.setString(1, nombre);
            ps.setInt(2, cant);
            ps.setDouble(3, pMayoreo);
            ps.setDouble(4, pMenudeo);
            if (idCat > 0) ps.setInt(5, idCat); else ps.setNull(5, Types.INTEGER);
            ps.setDouble(6, maxDesc);
            ps.setString(7, codigo);
            int rows = ps.executeUpdate();
            if (rows == 0) return "{\"error\":\"Producto no encontrado\"}";

            guardarCamposCompra(cuerpo, codigo);
            guardarPreciosLista(cuerpo, codigo);
            return "{\"ok\":true}";
        } catch (SQLException e) {
            return "{\"error\":\"" + escaparJson(e.getMessage()) + "\"}";
        }
    }

    /** Verifica si un código de barras ya está en uso por otro producto */
    private synchronized boolean codigoBarrasDuplicado(String codigoBarras, String idExcluir) {
        if (codigoBarras == null || codigoBarras.trim().isEmpty()) return false;
        try {
            PreparedStatement ps = getConn().prepareStatement(
                "SELECT 1 FROM producto WHERE codigo_barras = ? AND id_producto <> ? LIMIT 1");
            ps.setString(1, codigoBarras.trim());
            ps.setString(2, idExcluir == null ? "" : idExcluir);
            return ps.executeQuery().next();
        } catch (SQLException e) {
            return false;
        }
    }

    /** Persiste los campos de compra (código de barras, IVA, unidades, factor) */
    private synchronized void guardarCamposCompra(String cuerpo, String codigo) throws SQLException {
        String codBarras = extraerJson(cuerpo, "codigoBarras").trim();
        boolean llevaIva = Boolean.parseBoolean(extraerJson(cuerpo, "llevaIva").trim());
        String uCompra   = extraerJson(cuerpo, "unidadCompra").trim();
        String uVenta    = extraerJson(cuerpo, "unidadVenta").trim();
        double factor    = parsearDouble(extraerJson(cuerpo, "factor"), 1.0);
        if (factor <= 0) factor = 1.0;

        PreparedStatement ps = getConn().prepareStatement(
            "UPDATE producto SET codigo_barras=?, lleva_iva=?, unidad_compra=?, unidad_venta=?, factor_conversion=? WHERE id_producto=?");
        ps.setString(1, codBarras.isEmpty() ? null : codBarras);
        ps.setBoolean(2, llevaIva);
        ps.setString(3, uCompra.isEmpty() ? null : uCompra);
        ps.setString(4, uVenta.isEmpty() ? null : uVenta);
        ps.setDouble(5, factor);
        ps.setString(6, codigo);
        ps.executeUpdate();
    }

    /**
     * Persiste los precios por lista desde el campo "precios" del JSON.
     * Formato: "idLista:precio,idLista:precio,...". Precio 0 = sin override
     * (se borra la fila para que la lista use el precio base del producto).
     */
    private synchronized void guardarPreciosLista(String cuerpo, String codigo) throws SQLException {
        String precios = extraerJson(cuerpo, "precios").trim();
        if (precios.isEmpty()) return;
        for (String par : precios.split(",")) {
            int sep = par.indexOf(':');
            if (sep < 0) continue;
            int idLista  = parsearInt(par.substring(0, sep), 0);
            double valor = parsearDouble(par.substring(sep + 1), 0.0);
            if (idLista <= 0) continue;
            if (valor > 0) {
                PreparedStatement ps = getConn().prepareStatement(
                    "INSERT INTO producto_precio(id_producto,id_lista,precio) VALUES(?,?,?) "
                    + "ON CONFLICT (id_producto,id_lista) DO UPDATE SET precio = EXCLUDED.precio");
                ps.setString(1, codigo);
                ps.setInt(2, idLista);
                ps.setDouble(3, valor);
                ps.executeUpdate();
            } else {
                PreparedStatement ps = getConn().prepareStatement(
                    "DELETE FROM producto_precio WHERE id_producto=? AND id_lista=?");
                ps.setString(1, codigo);
                ps.setInt(2, idLista);
                ps.executeUpdate();
            }
        }
    }

    /** Devuelve productos con stock <= umbral como JSON array */
    private synchronized String stockBajoJson(int umbral) {
        StringBuilder json = new StringBuilder("[");
        try {
            PreparedStatement ps = getConn().prepareStatement(
                "SELECT p.id_producto, p.nombre, p.cantidad, p.precio_mayoreo, p.precio_menudeo, "
                + "COALESCE(c.nombre,'Sin categoría') AS categoria "
                + "FROM producto p LEFT JOIN categoria c ON p.id_categoria=c.id_categoria "
                + "WHERE p.estatus = 'Activo' AND p.cantidad<=? ORDER BY p.cantidad ASC, p.nombre ASC");
            ps.setInt(1, umbral);
            ResultSet rs = ps.executeQuery();
            boolean primero = true;
            while (rs.next()) {
                if (!primero) json.append(",");
                json.append("{\"id_producto\":\"").append(escaparJson(rs.getString("id_producto")))
                    .append("\",\"nombre\":\"").append(escaparJson(rs.getString("nombre")))
                    .append("\",\"cantidad\":").append(rs.getInt("cantidad"))
                    .append(",\"precio_mayoreo\":").append(rs.getDouble("precio_mayoreo"))
                    .append(",\"precio_menudeo\":").append(rs.getDouble("precio_menudeo"))
                    .append(",\"categoria\":\"").append(escaparJson(rs.getString("categoria")))
                    .append("\"}");
                primero = false;
            }
        } catch (SQLException e) {
            return "[]";
        }
        json.append("]");
        return json.toString();
    }

    // -------------------------------------------------------------------------
    // HTTP: páginas y endpoints REST
    // -------------------------------------------------------------------------
    @Override
    protected Response serveHttp(IHTTPSession session) {
        String uri = session.getUri();
        Method method = session.getMethod();

        // CORS preflight
        if (method == Method.OPTIONS) {
            return corsResponse(newFixedLengthResponse(""));
        }

        if ((uri.equals("/") || uri.equals("/consulta") || uri.equals("/editar")
                || uri.equals("/stockbajo")) && method == Method.GET) {
            return servirApp();
        } else if (uri.equals("/quagga.min.js") && method == Method.GET) {
            return servirArchivoEstatico("quagga.min.js", "application/javascript");
        } else if (uri.equals("/categorias") && method == Method.GET) {
            return servirCategorias();
        } else if (uri.equals("/listas") && method == Method.GET) {
            return jsonResponse(Response.Status.OK, obtenerListasJson());
        } else if (uri.equals("/producto") && method == Method.POST) {
            return guardarProducto(session, false);
        } else if (uri.equals("/producto/editar") && method == Method.POST) {
            return guardarProducto(session, true);
        } else if (uri.equals("/buscar") && method == Method.GET) {
            String codigo = session.getParameters().getOrDefault("codigo", List.of("")).get(0).trim();
            if (codigo.isEmpty()) {
                return jsonResponse(Response.Status.BAD_REQUEST, "{\"error\":\"Falta el código\"}");
            }
            return jsonResponse(Response.Status.OK, consultarProductoJson(codigo));
        }

        return newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "No encontrado");
    }

    // -------------------------------------------------------------------------
    // SPA: toda la app en una sola página (un solo WebSocket, sin recargas)
    // -------------------------------------------------------------------------
    private Response servirApp() {
        String html = "<!DOCTYPE html>\n"
            + "<html lang=\"es\"><head><meta charset=\"UTF-8\">\n"
            + "<meta name=\"viewport\" content=\"width=device-width,initial-scale=1.0,maximum-scale=1.0\">\n"
            + "<title>Inventario Móvil</title>\n"
            + "<style>\n"
            + "*{box-sizing:border-box;margin:0;padding:0}\n"
            + "body{font-family:sans-serif;background:#f0f4f8;padding:16px;max-width:520px;margin:0 auto}\n"
            + "h1{text-align:center;color:#4e9696;font-size:1.5em;margin-bottom:16px;padding-top:8px}\n"
            + "nav{display:flex;gap:6px;margin-bottom:16px}\n"
            + "nav a{flex:1;text-align:center;padding:10px 4px;border-radius:8px;text-decoration:none;font-weight:bold;font-size:.85em;border:2px solid #4e9696;color:#4e9696;cursor:pointer}\n"
            + "nav a.activo{background:#4e9696;color:#fff}\n"
            + ".seccion{display:none}\n"
            + ".seccion.visible{display:block}\n"
            + ".campo{margin-top:12px}\n"
            + "label{display:block;font-weight:bold;color:#444;margin-bottom:4px;font-size:.95em}\n"
            + "input,select{width:100%;padding:12px;border:1px solid #ccc;border-radius:8px;font-size:1em;background:#fff}\n"
            + "input:focus,select:focus{outline:none;border-color:#4e9696;box-shadow:0 0 0 2px #4e969633}\n"
            + ".btn-scan{background:#4e9696;color:#fff;border:none;border-radius:8px;padding:12px;font-size:1em;width:100%;margin-top:8px;cursor:pointer}\n"
            + ".btn-scan:active{background:#3a7a7a}\n"
            + ".btn-guardar{background:#4e9696;color:#fff;border:none;border-radius:8px;padding:16px;font-size:1.1em;font-weight:bold;width:100%;margin-top:24px;cursor:pointer}\n"
            + ".btn-guardar:active{background:#3a7a7a}\n"
            + ".visor{position:relative;width:100%;margin-top:8px;display:none;background:#000;border-radius:8px;overflow:hidden}\n"
            + ".visor video{width:100%;display:block}\n"
            + ".visor canvas.drawingBuffer{position:absolute;top:0;left:0;width:100%;height:100%}\n"
            + ".linea-scan{position:absolute;top:50%;left:5%;width:90%;height:2px;background:#4e9696;pointer-events:none}\n"
            + ".status{text-align:center;margin-top:14px;padding:12px;border-radius:8px;display:none;font-size:.95em}\n"
            + ".ok{background:#d4edda;color:#155724}\n"
            + ".err{background:#f8d7da;color:#721c24}\n"
            + ".tarjeta{background:#fff;border-radius:12px;padding:20px;box-shadow:0 2px 8px #0001;margin-top:20px}\n"
            + ".tarjeta h2{color:#4e9696;font-size:1.15em;margin-bottom:14px;border-bottom:1px solid #e0e0e0;padding-bottom:8px}\n"
            + ".fila{display:flex;justify-content:space-between;padding:7px 0;border-bottom:1px solid #f0f0f0;font-size:.97em}\n"
            + ".fila:last-child{border-bottom:none}\n"
            + ".fila .etiqueta{color:#666;font-weight:bold}\n"
            + ".fila .valor{color:#222;text-align:right;max-width:60%}\n"
            + ".stock-ok{color:#27ae60;font-weight:bold}\n"
            + ".stock-bajo{color:#e67e22;font-weight:bold}\n"
            + ".stock-cero{color:#c0392b;font-weight:bold}\n"
            + ".err-card{background:#f8d7da;color:#721c24;border-radius:12px;padding:16px;text-align:center;margin-top:20px;font-weight:bold}\n"
            + ".cargando{display:none;margin-top:28px;text-align:center}\n"
            + ".spinner{display:inline-block;width:40px;height:40px;border:4px solid #cde8e8;border-top-color:#4e9696;border-radius:50%;animation:giro .7s linear infinite}\n"
            + "@keyframes giro{to{transform:rotate(360deg)}}\n"
            + ".cargando p{margin-top:10px;color:#4e9696;font-size:.95em;font-weight:bold}\n"
            + ".filtro{display:flex;gap:8px;align-items:center;margin-bottom:16px}\n"
            + ".filtro label{font-weight:bold;color:#444;white-space:nowrap}\n"
            + ".filtro input{flex:1;padding:10px;border:1px solid #ccc;border-radius:8px;font-size:1em}\n"
            + ".filtro button{background:#4e9696;color:#fff;border:none;border-radius:8px;padding:10px 16px;font-size:1em;cursor:pointer}\n"
            + ".producto{background:#fff;border-radius:10px;padding:14px;margin-bottom:10px;box-shadow:0 1px 4px #0001}\n"
            + ".producto .nombre{font-weight:bold;color:#333;font-size:1.05em}\n"
            + ".producto .detalle{display:flex;justify-content:space-between;margin-top:6px;font-size:.9em;color:#666}\n"
            + ".badge{display:inline-block;padding:2px 8px;border-radius:12px;font-size:.8em;font-weight:bold}\n"
            + ".badge-cero{background:#f8d7da;color:#c0392b}\n"
            + ".badge-bajo{background:#fff3cd;color:#856404}\n"
            + ".vacio{text-align:center;color:#888;padding:40px 0;font-size:1.1em}\n"
            + ".total{text-align:center;color:#666;font-size:.9em;margin-bottom:12px}\n"
            + "#wsIndicator{text-align:center;font-size:.8em;padding:4px;margin-bottom:8px;border-radius:6px;display:none}\n"
            + ".check{display:flex;align-items:center;gap:8px;cursor:pointer}\n"
            + ".check input{width:auto;margin:0}\n"
            + ".campo-lista{display:flex;align-items:center;gap:10px;margin-top:8px}\n"
            + ".campo-lista label{flex:1;margin:0}\n"
            + ".campo-lista input{flex:1;width:auto}\n"
            + "</style></head><body>\n"
            + "<div id=\"wsIndicator\"></div>\n"
            + "<nav>\n"
            + "  <a href=\"#\" data-sec=\"nuevo\" class=\"activo\">&#x2795; Nuevo</a>\n"
            + "  <a href=\"#\" data-sec=\"consulta\">&#x1F50D; Consultar</a>\n"
            + "  <a href=\"#\" data-sec=\"editar\">&#x270F; Editar</a>\n"
            + "  <a href=\"#\" data-sec=\"stockbajo\">&#x26A0; Stock</a>\n"
            + "</nav>\n"
            + "\n"
            // ---- SECCIÓN: NUEVO ----
            + "<section id=\"sec-nuevo\" class=\"seccion visible\">\n"
            + "  <h1>Registrar Producto</h1>\n"
            + "  <div class=\"campo\">\n"
            + "    <label>Código de barras</label>\n"
            + "    <input type=\"text\" id=\"nCodigo\" placeholder=\"Escanea o escribe el código\">\n"
            + "    <button class=\"btn-scan\" id=\"nScanBtn\">&#x1F4F7; Abrir escáner</button>\n"
            + "    <div class=\"visor\" id=\"nVisor\"><div class=\"linea-scan\"></div></div>\n"
            + "    <button class=\"btn-scan\" id=\"nStopBtn\" style=\"display:none;background:#c0392b\">&#x23F9; Cerrar escáner</button>\n"
            + "  </div>\n"
            + "  <div class=\"campo\"><label>Nombre del producto</label><input type=\"text\" id=\"nNombre\" placeholder=\"Nombre\" autocomplete=\"off\"></div>\n"
            + "  <div class=\"campo\"><label>Cantidad</label><input type=\"number\" id=\"nCantidad\" value=\"1\" min=\"0\" inputmode=\"numeric\"></div>\n"
            + "  <div class=\"campo\"><label>Precio mayoreo</label><input type=\"number\" id=\"nMayoreo\" step=\"0.01\" placeholder=\"0.00\" inputmode=\"decimal\"></div>\n"
            + "  <div class=\"campo\"><label>Precio menudeo</label><input type=\"number\" id=\"nMenudeo\" step=\"0.01\" placeholder=\"0.00\" inputmode=\"decimal\"></div>\n"
            + "  <div class=\"campo\"><label>Categoría <span style=\"font-weight:normal;color:#888\">(opcional)</span></label><select id=\"nCategoria\"><option value=\"0\">Sin categoría</option></select></div>\n"
            + "  <div class=\"campo\"><label>Descuento máximo (%)</label><input type=\"number\" id=\"nMaxDesc\" value=\"100\" min=\"0\" max=\"100\" inputmode=\"numeric\"></div>\n"
            + "  <div class=\"campo\">\n"
            + "    <label>Código de barras <span style=\"font-weight:normal;color:#888\">(opcional)</span></label>\n"
            + "    <input type=\"text\" id=\"nCodBarras\" placeholder=\"Escanea o escribe el código de barras\">\n"
            + "    <button class=\"btn-scan\" id=\"nCbScanBtn\">&#x1F4F7; Escanear código de barras</button>\n"
            + "    <div class=\"visor\" id=\"nCbVisor\"><div class=\"linea-scan\"></div></div>\n"
            + "    <button class=\"btn-scan\" id=\"nCbStopBtn\" style=\"display:none;background:#c0392b\">&#x23F9; Cerrar escáner</button>\n"
            + "  </div>\n"
            + "  <div class=\"campo\"><label class=\"check\"><input type=\"checkbox\" id=\"nLlevaIva\"> Lleva IVA</label></div>\n"
            + "  <div class=\"campo\"><label>Unidad de compra</label><select id=\"nUCompra\"></select></div>\n"
            + "  <div class=\"campo\"><label>Unidad de venta</label><select id=\"nUVenta\"></select></div>\n"
            + "  <div class=\"campo\"><label>Factor de conversión</label><input type=\"number\" id=\"nFactor\" value=\"1\" step=\"0.001\" min=\"0\" inputmode=\"decimal\"></div>\n"
            + "  <div class=\"campo\"><label>Precios por lista</label><div id=\"nPrecios\"></div></div>\n"
            + "  <button class=\"btn-guardar\" id=\"nGuardar\">Guardar producto</button>\n"
            + "  <div class=\"status\" id=\"nStatus\"></div>\n"
            + "</section>\n"
            + "\n"
            // ---- SECCIÓN: CONSULTA ----
            + "<section id=\"sec-consulta\" class=\"seccion\">\n"
            + "  <h1>Consultar Producto</h1>\n"
            + "  <div class=\"campo\">\n"
            + "    <label>Código de barras</label>\n"
            + "    <input type=\"text\" id=\"cCodigo\" placeholder=\"Escanea o escribe el código\" inputmode=\"text\">\n"
            + "    <button class=\"btn-scan\" id=\"cScanBtn\">&#x1F4F7; Abrir escáner</button>\n"
            + "    <div class=\"visor\" id=\"cVisor\"><div class=\"linea-scan\"></div></div>\n"
            + "    <button class=\"btn-scan\" id=\"cStopBtn\" style=\"display:none;background:#c0392b\">&#x23F9; Cerrar escáner</button>\n"
            + "  </div>\n"
            + "  <div class=\"cargando\" id=\"cCargando\"><div class=\"spinner\"></div><p>Buscando producto...</p></div>\n"
            + "  <div id=\"cResultado\"></div>\n"
            + "</section>\n"
            + "\n"
            // ---- SECCIÓN: EDITAR ----
            + "<section id=\"sec-editar\" class=\"seccion\">\n"
            + "  <h1>Editar Producto</h1>\n"
            + "  <div class=\"campo\">\n"
            + "    <label>Código de barras</label>\n"
            + "    <input type=\"text\" id=\"eCodigo\" placeholder=\"Escanea o escribe el código\">\n"
            + "    <button class=\"btn-scan\" id=\"eScanBtn\">&#x1F4F7; Abrir escáner</button>\n"
            + "    <div class=\"visor\" id=\"eVisor\"><div class=\"linea-scan\"></div></div>\n"
            + "    <button class=\"btn-scan\" id=\"eStopBtn\" style=\"display:none;background:#c0392b\">&#x23F9; Cerrar escáner</button>\n"
            + "  </div>\n"
            + "  <div class=\"cargando\" id=\"eCargando\"><div class=\"spinner\"></div><p>Buscando producto...</p></div>\n"
            + "  <div id=\"eForm\" style=\"display:none\">\n"
            + "    <div class=\"campo\"><label>Nombre</label><input type=\"text\" id=\"eNombre\"></div>\n"
            + "    <div class=\"campo\"><label>Cantidad</label><input type=\"number\" id=\"eCantidad\" min=\"0\" inputmode=\"numeric\"></div>\n"
            + "    <div class=\"campo\"><label>Precio mayoreo</label><input type=\"number\" id=\"eMayoreo\" step=\"0.01\" inputmode=\"decimal\"></div>\n"
            + "    <div class=\"campo\"><label>Precio menudeo</label><input type=\"number\" id=\"eMenudeo\" step=\"0.01\" inputmode=\"decimal\"></div>\n"
            + "    <div class=\"campo\"><label>Categoría</label><select id=\"eCategoria\"><option value=\"0\">Sin categoría</option></select></div>\n"
            + "    <div class=\"campo\"><label>Descuento máximo (%)</label><input type=\"number\" id=\"eMaxDesc\" min=\"0\" max=\"100\" inputmode=\"numeric\"></div>\n"
            + "    <div class=\"campo\">\n"
            + "      <label>Código de barras <span style=\"font-weight:normal;color:#888\">(opcional)</span></label>\n"
            + "      <input type=\"text\" id=\"eCodBarras\" placeholder=\"Escanea o escribe el código de barras\">\n"
            + "      <button class=\"btn-scan\" id=\"eCbScanBtn\">&#x1F4F7; Escanear código de barras</button>\n"
            + "      <div class=\"visor\" id=\"eCbVisor\"><div class=\"linea-scan\"></div></div>\n"
            + "      <button class=\"btn-scan\" id=\"eCbStopBtn\" style=\"display:none;background:#c0392b\">&#x23F9; Cerrar escáner</button>\n"
            + "    </div>\n"
            + "    <div class=\"campo\"><label class=\"check\"><input type=\"checkbox\" id=\"eLlevaIva\"> Lleva IVA</label></div>\n"
            + "    <div class=\"campo\"><label>Unidad de compra</label><select id=\"eUCompra\"></select></div>\n"
            + "    <div class=\"campo\"><label>Unidad de venta</label><select id=\"eUVenta\"></select></div>\n"
            + "    <div class=\"campo\"><label>Factor de conversión</label><input type=\"number\" id=\"eFactor\" step=\"0.001\" min=\"0\" inputmode=\"decimal\"></div>\n"
            + "    <div class=\"campo\"><label>Precios por lista</label><div id=\"ePrecios\"></div></div>\n"
            + "    <button class=\"btn-guardar\" id=\"eGuardar\">Guardar cambios</button>\n"
            + "  </div>\n"
            + "  <div class=\"status\" id=\"eStatus\"></div>\n"
            + "</section>\n"
            + "\n"
            // ---- SECCIÓN: STOCK BAJO ----
            + "<section id=\"sec-stockbajo\" class=\"seccion\">\n"
            + "  <h1>Stock Bajo</h1>\n"
            + "  <div class=\"filtro\">\n"
            + "    <label>Stock &le;</label>\n"
            + "    <input type=\"number\" id=\"sUmbral\" value=\"5\" min=\"0\" inputmode=\"numeric\">\n"
            + "    <button id=\"sFiltrar\">Filtrar</button>\n"
            + "  </div>\n"
            + "  <div class=\"cargando\" id=\"sCargando\"><div class=\"spinner\"></div><p>Cargando...</p></div>\n"
            + "  <div id=\"sTotal\" class=\"total\"></div>\n"
            + "  <div id=\"sLista\"></div>\n"
            + "</section>\n"
            + "\n"
            + "<script src=\"/quagga.min.js\"></script>\n"
            + "<script>\n"
            // ---- NAVEGACIÓN SPA ----
            + "var seccionActual='nuevo';\n"
            + "var navLinks=document.querySelectorAll('nav a');\n"
            + "function irA(nombre){\n"
            + "  detenerScanner();\n"
            + "  var secciones=document.querySelectorAll('.seccion');\n"
            + "  for(var i=0;i<secciones.length;i++) secciones[i].classList.remove('visible');\n"
            + "  var target=document.getElementById('sec-'+nombre);\n"
            + "  if(target) target.classList.add('visible');\n"
            + "  for(var j=0;j<navLinks.length;j++){\n"
            + "    if(navLinks[j].getAttribute('data-sec')===nombre) navLinks[j].classList.add('activo');\n"
            + "    else navLinks[j].classList.remove('activo');\n"
            + "  }\n"
            + "  seccionActual=nombre;\n"
            + "  if(nombre==='stockbajo') cargarStock();\n"
            + "}\n"
            + "for(var i=0;i<navLinks.length;i++){\n"
            + "  navLinks[i].addEventListener('click',function(e){\n"
            + "    e.preventDefault();\n"
            + "    var sec=this.getAttribute('data-sec');\n"
            + "    irA(sec);\n"
            + "    history.pushState(null,'',sec==='nuevo'?'/':'/'+sec);\n"
            + "  });\n"
            + "}\n"
            + "window.addEventListener('popstate',function(){\n"
            + "  var p=location.pathname.replace('/','');\n"
            + "  irA(p||'nuevo');\n"
            + "});\n"
            + "(function(){var p=location.pathname.replace('/','');if(p&&p!=='nuevo')irA(p);})();\n"
            + "\n"
            // ---- UNIDADES (lista fija, igual que el escritorio) ----
            + "var UNIDADES=['Pieza','Caja','Bolsa','Bulto','Botella','Lata','Paquete','Bote','Barra','Vaso','Tetra Pak'];\n"
            + "function llenarUnidades(){\n"
            + "  var ids=['nUCompra','nUVenta','eUCompra','eUVenta'];\n"
            + "  for(var i=0;i<ids.length;i++){\n"
            + "    var sel=document.getElementById(ids[i]);if(!sel||sel.options.length)continue;\n"
            + "    for(var j=0;j<UNIDADES.length;j++){var o=document.createElement('option');o.value=UNIDADES[j];o.textContent=UNIDADES[j];sel.appendChild(o);}\n"
            + "  }\n"
            + "}\n"
            + "llenarUnidades();\n"
            + "\n"
            // ---- PRECIOS POR LISTA ----
            + "function cargarListas(listas){\n"
            + "  if(!Array.isArray(listas))return;\n"
            + "  var conts=['nPrecios','ePrecios'];\n"
            + "  for(var c=0;c<conts.length;c++){\n"
            + "    var cont=document.getElementById(conts[c]);if(!cont||cont.children.length)continue;\n"
            + "    for(var i=0;i<listas.length;i++){\n"
            + "      if(listas[i].error)continue;\n"
            + "      var fila=document.createElement('div');fila.className='campo-lista';\n"
            + "      var lab=document.createElement('label');lab.textContent=listas[i].nombre;\n"
            + "      var inp=document.createElement('input');inp.type='number';inp.step='0.01';inp.min='0';inp.value='0';\n"
            + "      inp.setAttribute('inputmode','decimal');inp.setAttribute('data-idlista',listas[i].id);\n"
            + "      fila.appendChild(lab);fila.appendChild(inp);cont.appendChild(fila);\n"
            + "    }\n"
            + "  }\n"
            + "}\n"
            + "function preciosStr(contId){\n"
            + "  var out=[],ins=document.querySelectorAll('#'+contId+' input[data-idlista]');\n"
            + "  for(var i=0;i<ins.length;i++){out.push(ins[i].getAttribute('data-idlista')+':'+(ins[i].value||'0'));}\n"
            + "  return out.join(',');\n"
            + "}\n"
            + "\n"
            // ---- WEBSOCKET COMPARTIDO ----
            + "var ws,wsListo=false,wsPing;\n"
            + "var pendiente=null,cola=[];\n"
            + "function enviarWS(tipo,msg){\n"
            + "  if(pendiente!==null){cola.push({tipo:tipo,msg:msg});return;}\n"
            + "  pendiente=tipo;\n"
            + "  ws.send(msg);\n"
            + "}\n"
            + "function siguienteCola(){\n"
            + "  pendiente=null;\n"
            + "  if(cola.length>0){var next=cola.shift();enviarWS(next.tipo,next.msg);}\n"
            + "}\n"
            + "function conectarWS(){\n"
            + "  ws=new WebSocket('wss://'+location.host);\n"
            + "  ws.onopen=function(){\n"
            + "    wsListo=true;cola=[];pendiente=null;\n"
            + "    enviarWS('categorias','categorias');\n"
            + "    enviarWS('listas','listas');\n"
            + "    if(seccionActual==='stockbajo') cargarStock();\n"
            + "  };\n"
            + "  ws.onmessage=function(e){\n"
            + "    try{\n"
            + "      var data=JSON.parse(e.data);\n"
            + "      switch(pendiente){\n"
            + "        case 'categorias': cargarCategorias(data);siguienteCola();return;\n"
            + "        case 'listas': cargarListas(data);siguienteCola();return;\n"
            + "        case 'stockbajo': mostrarStock(data);siguienteCola();return;\n"
            + "        case 'consulta': mostrarConsulta(data);siguienteCola();return;\n"
            + "        case 'editar-buscar': llenarEditar(data);siguienteCola();return;\n"
            + "        case 'editar-guardar': respuestaEditar(data);siguienteCola();return;\n"
            + "        case 'nuevo': respuestaNuevo(data);siguienteCola();return;\n"
            + "      }\n"
            + "    }catch(err){console.log('WS error',err);}\n"
            + "  };\n"
            + "  ws.onclose=function(){wsListo=false;clearInterval(wsPing);setTimeout(conectarWS,2000);};\n"
            + "  ws.onerror=function(){wsListo=false;};\n"
            + "  wsPing=setInterval(function(){if(wsListo&&pendiente===null)ws.send('ping');},3000);\n"
            + "}\n"
            + "conectarWS();\n"
            + "fetch('/categorias').then(function(r){return r.json();}).then(cargarCategorias).catch(function(){});\n"
            + "fetch('/listas').then(function(r){return r.json();}).then(cargarListas).catch(function(){});\n"
            + "\n"
            + "function cargarCategorias(cats){\n"
            + "  if(!Array.isArray(cats))return;\n"
            + "  var ids=['nCategoria','eCategoria'];\n"
            + "  for(var i=0;i<ids.length;i++){\n"
            + "    var sel=document.getElementById(ids[i]);\n"
            + "    if(sel.options.length>1) continue;\n"
            + "    for(var j=0;j<cats.length;j++){\n"
            + "      var o=document.createElement('option');o.value=cats[j].id;o.textContent=cats[j].nombre;sel.appendChild(o);\n"
            + "    }\n"
            + "  }\n"
            + "}\n"
            + "\n"
            // ---- SCANNER COMPARTIDO ----
            + "var escaneando=false,scanVisor=null,scanOnDetect=null,scanScanBtn=null,scanStopBtn=null;\n"
            + "function detenerScanner(){\n"
            + "  if(!escaneando)return;\n"
            + "  Quagga.stop();Quagga.offDetected(scanOnDetect);\n"
            + "  escaneando=false;\n"
            + "  if(scanVisor)scanVisor.style.display='none';\n"
            + "  if(scanScanBtn)scanScanBtn.style.display='block';\n"
            + "  if(scanStopBtn)scanStopBtn.style.display='none';\n"
            + "}\n"
            + "function iniciarScanner(visor,sBt,xBt,callback){\n"
            + "  detenerScanner();\n"
            + "  scanVisor=visor;scanScanBtn=sBt;scanStopBtn=xBt;\n"
            + "  visor.style.display='block';sBt.style.display='none';xBt.style.display='block';\n"
            + "  Quagga.init({inputStream:{type:'LiveStream',target:visor,constraints:{facingMode:'environment',width:{ideal:1280},height:{ideal:720}}},\n"
            + "    locator:{patchSize:'medium',halfSample:true},\n"
            + "    decoder:{readers:['ean_reader','ean_8_reader','code_128_reader','upc_reader','upc_e_reader']},locate:true\n"
            + "  },function(err){\n"
            + "    if(err){alert('Error cámara: '+err);detenerScanner();return;}\n"
            + "    Quagga.start();escaneando=true;\n"
            + "  });\n"
            + "  var ult='',cnt=0;\n"
            + "  scanOnDetect=function(data){\n"
            + "    var c=data.codeResult.code;\n"
            + "    if(c===ult){cnt++;}else{ult=c;cnt=1;}\n"
            + "    if(cnt>=3){detenerScanner();callback(c);ult='';cnt=0;}\n"
            + "  };\n"
            + "  Quagga.onDetected(scanOnDetect);\n"
            + "}\n"
            + "\n"
            // ---- UTILIDADES ----
            + "function mostrarEstado(elId,msg,ok){\n"
            + "  var el=document.getElementById(elId);\n"
            + "  el.textContent=msg;el.className='status '+(ok?'ok':'err');el.style.display='block';\n"
            + "  setTimeout(function(){el.style.display='none';},5000);\n"
            + "}\n"
            + "function escHtml(s){var d=document.createElement('div');d.textContent=s||'';return d.innerHTML;}\n"
            + "function fmtNum(n){return Number(n).toFixed(2);}\n"
            + "function fila(et,val){return '<div class=\"fila\"><span class=\"etiqueta\">'+et+'</span><span class=\"valor\">'+val+'</span></div>';}\n"
            + "\n"
            // ---- NUEVO: registrar producto ----
            + "document.getElementById('nScanBtn').addEventListener('click',function(){\n"
            + "  iniciarScanner(document.getElementById('nVisor'),document.getElementById('nScanBtn'),document.getElementById('nStopBtn'),function(cod){\n"
            + "    document.getElementById('nCodigo').value=cod;document.getElementById('nNombre').focus();\n"
            + "  });\n"
            + "});\n"
            + "document.getElementById('nStopBtn').addEventListener('click',detenerScanner);\n"
            + "document.getElementById('nCbScanBtn').addEventListener('click',function(){\n"
            + "  iniciarScanner(document.getElementById('nCbVisor'),document.getElementById('nCbScanBtn'),document.getElementById('nCbStopBtn'),function(cod){\n"
            + "    document.getElementById('nCodBarras').value=cod;\n"
            + "  });\n"
            + "});\n"
            + "document.getElementById('nCbStopBtn').addEventListener('click',detenerScanner);\n"
            + "document.getElementById('nGuardar').addEventListener('click',function(){\n"
            + "  var d={codigo:document.getElementById('nCodigo').value.trim(),nombre:document.getElementById('nNombre').value.trim(),\n"
            + "    cantidad:document.getElementById('nCantidad').value||'0',precioMayoreo:document.getElementById('nMayoreo').value||'0',\n"
            + "    precioMenudeo:document.getElementById('nMenudeo').value||'0',idCategoria:document.getElementById('nCategoria').value,\n"
            + "    maxDescuento:document.getElementById('nMaxDesc').value||'100',\n"
            + "    codigoBarras:document.getElementById('nCodBarras').value.trim(),llevaIva:document.getElementById('nLlevaIva').checked,\n"
            + "    unidadCompra:document.getElementById('nUCompra').value,unidadVenta:document.getElementById('nUVenta').value,\n"
            + "    factor:document.getElementById('nFactor').value||'1',precios:preciosStr('nPrecios')};\n"
            + "  if(!d.codigo||!d.nombre){mostrarEstado('nStatus','El código y el nombre son obligatorios',false);return;}\n"
            + "  var btn=document.getElementById('nGuardar');btn.disabled=true;btn.textContent='Guardando...';\n"
            + "  if(wsListo){enviarWS('nuevo',JSON.stringify(d));}\n"
            + "  else{pendiente='nuevo';fetch('/producto',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify(d)}).then(function(r){return r.json();}).then(function(j){respuestaNuevo(j);siguienteCola();}).catch(function(){respuestaNuevo({error:'Error de conexión'});siguienteCola();});}\n"
            + "});\n"
            + "function respuestaNuevo(json){\n"
            + "  var btn=document.getElementById('nGuardar');btn.disabled=false;btn.textContent='Guardar producto';\n"
            + "  if(json.ok){\n"
            + "    mostrarEstado('nStatus','Producto guardado correctamente',true);\n"
            + "    document.getElementById('nCodigo').value='';document.getElementById('nNombre').value='';\n"
            + "    document.getElementById('nCantidad').value='1';\n"
            + "    document.getElementById('nMayoreo').value='';document.getElementById('nMenudeo').value='';\n"
            + "    document.getElementById('nCategoria').value='0';document.getElementById('nMaxDesc').value='100';\n"
            + "    document.getElementById('nCodBarras').value='';document.getElementById('nLlevaIva').checked=false;\n"
            + "    document.getElementById('nUCompra').selectedIndex=0;document.getElementById('nUVenta').selectedIndex=0;\n"
            + "    document.getElementById('nFactor').value='1';\n"
            + "    var npi=document.querySelectorAll('#nPrecios input[data-idlista]');for(var i=0;i<npi.length;i++)npi[i].value='0';\n"
            + "  }else{mostrarEstado('nStatus','Error: '+(json.error||'Error desconocido'),false);}\n"
            + "}\n"
            + "\n"
            // ---- CONSULTA ----
            + "document.getElementById('cScanBtn').addEventListener('click',function(){\n"
            + "  iniciarScanner(document.getElementById('cVisor'),document.getElementById('cScanBtn'),document.getElementById('cStopBtn'),function(cod){\n"
            + "    document.getElementById('cCodigo').value=cod;buscarConsulta(cod);\n"
            + "  });\n"
            + "});\n"
            + "document.getElementById('cStopBtn').addEventListener('click',detenerScanner);\n"
            + "document.getElementById('cCodigo').addEventListener('keydown',function(e){if(e.key==='Enter'&&this.value.trim())buscarConsulta(this.value.trim());});\n"
            + "function buscarConsulta(codigo){\n"
            + "  document.getElementById('cResultado').innerHTML='';document.getElementById('cCargando').style.display='block';\n"
            + "  if(wsListo){enviarWS('consulta',codigo);}\n"
            + "  else{pendiente='consulta';fetch('/buscar?codigo='+encodeURIComponent(codigo)).then(function(r){return r.json();}).then(function(d){mostrarConsulta(d);siguienteCola();}).catch(function(){mostrarConsulta({error:'Error de conexión'});siguienteCola();});}\n"
            + "}\n"
            + "function mostrarConsulta(p){\n"
            + "  document.getElementById('cCargando').style.display='none';\n"
            + "  var div=document.getElementById('cResultado');\n"
            + "  if(p.error){div.innerHTML='<div class=\"err-card\">'+escHtml(p.error)+'</div>';return;}\n"
            + "  var sc=p.cantidad===0?'stock-cero':p.cantidad<=5?'stock-bajo':'stock-ok';\n"
            + "  var h='<div class=\"tarjeta\"><h2>'+escHtml(p.nombre)+'</h2>'\n"
            + "    +fila('Código',escHtml(p.id_producto))\n"
            + "    +(p.codigo_barras?fila('Código de barras',escHtml(p.codigo_barras)):'')\n"
            + "    +fila('Categoría',escHtml(p.categoria))\n"
            + "    +'<div class=\"fila\"><span class=\"etiqueta\">Cantidad en stock</span><span class=\"valor '+sc+'\">'+p.cantidad+'</span></div>'\n"
            + "    +fila('Precio mayoreo','$'+fmtNum(p.precio_mayoreo))\n"
            + "    +fila('Precio menudeo','$'+fmtNum(p.precio_menudeo))\n"
            + "    +fila('Precio de compra','$'+fmtNum(p.precio_compra))\n"
            + "    +fila('Lleva IVA',p.lleva_iva?'Sí':'No')\n"
            + "    +(p.unidad_compra?fila('Unidad de compra',escHtml(p.unidad_compra)):'')\n"
            + "    +(p.unidad_venta?fila('Unidad de venta',escHtml(p.unidad_venta)):'')\n"
            + "    +fila('Factor de conversión',p.factor_conversion)\n"
            + "    +fila('Desc. máximo',p.max_descuento+'%');\n"
            + "  if(Array.isArray(p.precios)&&p.precios.length){\n"
            + "    h+='<div class=\"fila\" style=\"border-top:1px solid #e0e0e0;margin-top:6px;padding-top:10px\"><span class=\"etiqueta\">Precios por lista</span><span class=\"valor\"></span></div>';\n"
            + "    for(var i=0;i<p.precios.length;i++){h+=fila('&bull; '+escHtml(p.precios[i].nombre),'$'+fmtNum(p.precios[i].precio));}\n"
            + "  }\n"
            + "  h+='</div>';div.innerHTML=h;\n"
            + "}\n"
            + "\n"
            // ---- EDITAR ----
            + "var editarCodigo='';\n"
            + "document.getElementById('eScanBtn').addEventListener('click',function(){\n"
            + "  iniciarScanner(document.getElementById('eVisor'),document.getElementById('eScanBtn'),document.getElementById('eStopBtn'),function(cod){\n"
            + "    document.getElementById('eCodigo').value=cod;buscarEditar(cod);\n"
            + "  });\n"
            + "});\n"
            + "document.getElementById('eStopBtn').addEventListener('click',detenerScanner);\n"
            + "document.getElementById('eCbScanBtn').addEventListener('click',function(){\n"
            + "  iniciarScanner(document.getElementById('eCbVisor'),document.getElementById('eCbScanBtn'),document.getElementById('eCbStopBtn'),function(cod){\n"
            + "    document.getElementById('eCodBarras').value=cod;\n"
            + "  });\n"
            + "});\n"
            + "document.getElementById('eCbStopBtn').addEventListener('click',detenerScanner);\n"
            + "document.getElementById('eCodigo').addEventListener('keydown',function(e){if(e.key==='Enter'&&this.value.trim())buscarEditar(this.value.trim());});\n"
            + "function buscarEditar(codigo){\n"
            + "  editarCodigo=codigo;\n"
            + "  document.getElementById('eForm').style.display='none';document.getElementById('eCargando').style.display='block';\n"
            + "  if(wsListo){enviarWS('editar-buscar',codigo);}\n"
            + "  else{pendiente='editar-buscar';fetch('/buscar?codigo='+encodeURIComponent(codigo)).then(function(r){return r.json();}).then(function(d){llenarEditar(d);siguienteCola();}).catch(function(){mostrarEstado('eStatus','Error de conexión',false);siguienteCola();});}\n"
            + "}\n"
            + "function llenarEditar(p){\n"
            + "  document.getElementById('eCargando').style.display='none';\n"
            + "  if(p.error){mostrarEstado('eStatus',p.error,false);return;}\n"
            + "  document.getElementById('eNombre').value=p.nombre;\n"
            + "  document.getElementById('eCantidad').value=p.cantidad;\n"
            + "  document.getElementById('eMayoreo').value=p.precio_mayoreo;\n"
            + "  document.getElementById('eMenudeo').value=p.precio_menudeo;\n"
            + "  document.getElementById('eMaxDesc').value=p.max_descuento;\n"
            + "  if(p.id_categoria!==undefined)document.getElementById('eCategoria').value=p.id_categoria;\n"
            + "  document.getElementById('eCodBarras').value=p.codigo_barras||'';\n"
            + "  document.getElementById('eLlevaIva').checked=!!p.lleva_iva;\n"
            + "  if(p.unidad_compra)document.getElementById('eUCompra').value=p.unidad_compra;\n"
            + "  if(p.unidad_venta)document.getElementById('eUVenta').value=p.unidad_venta;\n"
            + "  document.getElementById('eFactor').value=p.factor_conversion;\n"
            + "  if(Array.isArray(p.precios)){for(var i=0;i<p.precios.length;i++){var inp=document.querySelector('#ePrecios input[data-idlista=\"'+p.precios[i].id+'\"]');if(inp)inp.value=p.precios[i].precio;}}\n"
            + "  document.getElementById('eForm').style.display='block';\n"
            + "}\n"
            + "document.getElementById('eGuardar').addEventListener('click',function(){\n"
            + "  var d=JSON.stringify({codigo:editarCodigo,nombre:document.getElementById('eNombre').value.trim(),\n"
            + "    cantidad:document.getElementById('eCantidad').value||'0',precioMayoreo:document.getElementById('eMayoreo').value||'0',\n"
            + "    precioMenudeo:document.getElementById('eMenudeo').value||'0',idCategoria:document.getElementById('eCategoria').value,\n"
            + "    maxDescuento:document.getElementById('eMaxDesc').value||'100',\n"
            + "    codigoBarras:document.getElementById('eCodBarras').value.trim(),llevaIva:document.getElementById('eLlevaIva').checked,\n"
            + "    unidadCompra:document.getElementById('eUCompra').value,unidadVenta:document.getElementById('eUVenta').value,\n"
            + "    factor:document.getElementById('eFactor').value||'1',precios:preciosStr('ePrecios')});\n"
            + "  if(wsListo){enviarWS('editar-guardar','editar:'+d);}\n"
            + "  else{pendiente='editar-guardar';fetch('/producto/editar',{method:'POST',headers:{'Content-Type':'application/json'},body:d}).then(function(r){return r.json();}).then(function(r){respuestaEditar(r);siguienteCola();}).catch(function(){respuestaEditar({error:'Error de conexión'});siguienteCola();});}\n"
            + "});\n"
            + "function respuestaEditar(data){\n"
            + "  if(data.ok) mostrarEstado('eStatus','Producto actualizado',true);\n"
            + "  else mostrarEstado('eStatus','Error: '+(data.error||'Error'),false);\n"
            + "}\n"
            + "\n"
            // ---- STOCK BAJO ----
            + "function cargarStock(){\n"
            + "  var u=document.getElementById('sUmbral').value||'5';\n"
            + "  document.getElementById('sLista').innerHTML='';document.getElementById('sCargando').style.display='block';\n"
            + "  document.getElementById('sTotal').textContent='';\n"
            + "  if(wsListo){enviarWS('stockbajo','stockbajo:'+u);}\n"
            + "  else{document.getElementById('sCargando').querySelector('p').textContent='Esperando conexión...';}\n"
            + "}\n"
            + "function mostrarStock(prods){\n"
            + "  document.getElementById('sCargando').style.display='none';\n"
            + "  if(!Array.isArray(prods)){return;}\n"
            + "  document.getElementById('sTotal').textContent=prods.length+' producto'+(prods.length!==1?'s':'');\n"
            + "  var div=document.getElementById('sLista');\n"
            + "  if(!prods.length){div.innerHTML='<div class=\"vacio\">No hay productos con stock bajo</div>';return;}\n"
            + "  var h='';\n"
            + "  for(var i=0;i<prods.length;i++){\n"
            + "    var p=prods[i];\n"
            + "    var badge=p.cantidad===0?'badge-cero':'badge-bajo';\n"
            + "    h+='<div class=\"producto\"><div class=\"nombre\">'+escHtml(p.nombre)+' <span class=\"badge '+badge+'\">'+p.cantidad+'</span></div>'\n"
            + "      +'<div class=\"detalle\"><span>'+escHtml(p.id_producto)+'</span><span>'+escHtml(p.categoria)+'</span></div>'\n"
            + "      +'<div class=\"detalle\"><span>Mayoreo: $'+fmtNum(p.precio_mayoreo)+'</span><span>Menudeo: $'+fmtNum(p.precio_menudeo)+'</span></div></div>';\n"
            + "  }\n"
            + "  div.innerHTML=h;\n"
            + "}\n"
            + "document.getElementById('sFiltrar').addEventListener('click',cargarStock);\n"
            + "document.getElementById('sUmbral').addEventListener('keydown',function(e){if(e.key==='Enter')cargarStock();});\n"
            + "</script></body></html>\n";

        return corsResponse(newFixedLengthResponse(Response.Status.OK, "text/html; charset=utf-8", html));
    }

    // -------------------------------------------------------------------------
    // GET /quagga.min.js  →  librería Quagga2 servida localmente (offline)
    // -------------------------------------------------------------------------
    private static byte[] cacheQuagga = null;

    private Response servirArchivoEstatico(String nombre, String mimeType) {
        if (cacheQuagga == null) {
            try {
                java.nio.file.Path ruta = java.nio.file.Paths.get(
                    System.getProperty("user.dir"), nombre);
                cacheQuagga = java.nio.file.Files.readAllBytes(ruta);
            } catch (Exception e) {
                return newFixedLengthResponse(Response.Status.NOT_FOUND,
                    "text/plain", nombre + " no encontrado en: " + System.getProperty("user.dir"));
            }
        }
        Response res = newFixedLengthResponse(Response.Status.OK, mimeType,
            new java.io.ByteArrayInputStream(cacheQuagga), cacheQuagga.length);
        res.addHeader("Cache-Control", "max-age=86400");
        return res;
    }

    // -------------------------------------------------------------------------
    // GET /categorias  →  JSON con categorías activas
    // -------------------------------------------------------------------------
    private synchronized Response servirCategorias() {
        StringBuilder json = new StringBuilder("[");
        try {
            PreparedStatement ps = getConn().prepareStatement(
                "SELECT id_categoria, nombre FROM categoria WHERE estatus='Activo' ORDER BY nombre");
            ResultSet rs = ps.executeQuery();
            boolean primero = true;
            while (rs.next()) {
                if (!primero) json.append(",");
                json.append("{\"id\":").append(rs.getInt(1))
                    .append(",\"nombre\":\"").append(escaparJson(rs.getString(2))).append("\"}");
                primero = false;
            }
        } catch (SQLException e) {
            return jsonResponse(Response.Status.INTERNAL_ERROR,
                "{\"error\":\"" + escaparJson(e.getMessage()) + "\"}");
        }
        json.append("]");
        return jsonResponse(Response.Status.OK, json.toString());
    }

    // -------------------------------------------------------------------------
    // POST /producto         →  insertar producto en la BD
    // POST /producto/editar  →  actualizar producto existente
    // Fallback REST cuando el WebSocket no está disponible. Reutiliza los mismos
    // handlers que la vía WebSocket para no duplicar la lógica de negocio.
    // -------------------------------------------------------------------------
    private synchronized Response guardarProducto(IHTTPSession session, boolean editar) {
        try {
            Map<String, String> archivos = new HashMap<>();
            session.parseBody(archivos);
            String cuerpo = archivos.get("postData");
            if (cuerpo == null || cuerpo.isBlank()) {
                return jsonResponse(Response.Status.BAD_REQUEST, "{\"error\":\"Cuerpo vacío\"}");
            }
            String resultado = editar ? editarProductoJson(cuerpo) : registrarProductoJson(cuerpo);
            Response.Status status = resultado.contains("\"error\"")
                ? Response.Status.INTERNAL_ERROR : Response.Status.OK;
            return jsonResponse(status, resultado);
        } catch (ResponseException | java.io.IOException e) {
            return jsonResponse(Response.Status.BAD_REQUEST,
                "{\"error\":\"Error al leer la petición\"}");
        }
    }


    // -------------------------------------------------------------------------
    // Utilidades
    // -------------------------------------------------------------------------
    private Response jsonResponse(Response.Status status, String json) {
        return corsResponse(newFixedLengthResponse(status, "application/json; charset=utf-8", json));
    }

    private Response corsResponse(Response res) {
        res.addHeader("Access-Control-Allow-Origin", "*");
        res.addHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        res.addHeader("Access-Control-Allow-Headers", "Content-Type");
        return res;
    }

    /**
     * Extrae el valor de una clave en un JSON simple (string o número).
     * No depende de librerías externas.
     */
    private String extraerJson(String json, String clave) {
        // Busca "clave":"valor"
        String patron1 = "\"" + clave + "\":\"";
        int idx = json.indexOf(patron1);
        if (idx >= 0) {
            int inicio = idx + patron1.length();
            int fin = json.indexOf("\"", inicio);
            return fin >= 0 ? json.substring(inicio, fin) : "";
        }
        // Busca "clave":numero
        String patron2 = "\"" + clave + "\":";
        idx = json.indexOf(patron2);
        if (idx >= 0) {
            int inicio = idx + patron2.length();
            int fin1 = json.indexOf(",", inicio);
            int fin2 = json.indexOf("}", inicio);
            int fin = (fin1 < 0) ? fin2 : (fin2 < 0) ? fin1 : Math.min(fin1, fin2);
            return fin >= 0 ? json.substring(inicio, fin).trim() : "";
        }
        return "";
    }

    private String escaparJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", " ").replace("\r", "");
    }

    private int parsearInt(String s, int defecto) {
        try { return Integer.parseInt(s.trim()); } catch (Exception e) { return defecto; }
    }

    private double parsearDouble(String s, double defecto) {
        try { return Double.parseDouble(s.trim()); } catch (Exception e) { return defecto; }
    }
}
