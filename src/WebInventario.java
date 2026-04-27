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
    private final String contra = "Daniel183.";

    private Connection conn = null;

    private Connection getConn() throws SQLException {
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
    private String consultarProductoJson(String codigo) {
        try {
            PreparedStatement ps = getConn().prepareStatement(
                "SELECT p.id_producto, p.nombre, p.cantidad, p.precio_mayoreo, p.precio_menudeo, "
                + "COALESCE(c.nombre,'Sin categoría') AS categoria, p.max_descuento "
                + "FROM producto p LEFT JOIN categoria c ON p.id_categoria=c.id_categoria "
                + "WHERE p.id_producto=?");
            ps.setString(1, codigo);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                return "{\"error\":\"Producto no encontrado\"}";
            }
            return "{"
                + "\"id_producto\":\"" + escaparJson(rs.getString("id_producto")) + "\","
                + "\"nombre\":\"" + escaparJson(rs.getString("nombre")) + "\","
                + "\"cantidad\":" + rs.getInt("cantidad") + ","
                + "\"precio_mayoreo\":" + rs.getDouble("precio_mayoreo") + ","
                + "\"precio_menudeo\":" + rs.getDouble("precio_menudeo") + ","
                + "\"categoria\":\"" + escaparJson(rs.getString("categoria")) + "\","
                + "\"max_descuento\":" + rs.getDouble("max_descuento")
                + "}";
        } catch (SQLException e) {
            return "{\"error\":\"" + escaparJson(e.getMessage()) + "\"}";
        }
    }

    /** Devuelve las categorías activas como JSON array */
    private String obtenerCategoriasJson() {
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
    private String registrarProductoJson(String cuerpo) {
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
    private String editarProductoJson(String cuerpo) {
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
            return "{\"ok\":true}";
        } catch (SQLException e) {
            return "{\"error\":\"" + escaparJson(e.getMessage()) + "\"}";
        }
    }

    /** Devuelve productos con stock <= umbral como JSON array */
    private String stockBajoJson(int umbral) {
        StringBuilder json = new StringBuilder("[");
        try {
            PreparedStatement ps = getConn().prepareStatement(
                "SELECT p.id_producto, p.nombre, p.cantidad, p.precio_mayoreo, p.precio_menudeo, "
                + "COALESCE(c.nombre,'Sin categoría') AS categoria "
                + "FROM producto p LEFT JOIN categoria c ON p.id_categoria=c.id_categoria "
                + "WHERE p.cantidad<=? ORDER BY p.cantidad ASC, p.nombre ASC");
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
        } else if (uri.equals("/producto") && method == Method.POST) {
            return guardarProducto(session);
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
            + "    if(seccionActual==='stockbajo') cargarStock();\n"
            + "  };\n"
            + "  ws.onmessage=function(e){\n"
            + "    try{\n"
            + "      var data=JSON.parse(e.data);\n"
            + "      switch(pendiente){\n"
            + "        case 'categorias': cargarCategorias(data);siguienteCola();return;\n"
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
            + "\n"
            // ---- NUEVO: registrar producto ----
            + "document.getElementById('nScanBtn').addEventListener('click',function(){\n"
            + "  iniciarScanner(document.getElementById('nVisor'),document.getElementById('nScanBtn'),document.getElementById('nStopBtn'),function(cod){\n"
            + "    document.getElementById('nCodigo').value=cod;document.getElementById('nNombre').focus();\n"
            + "  });\n"
            + "});\n"
            + "document.getElementById('nStopBtn').addEventListener('click',detenerScanner);\n"
            + "document.getElementById('nGuardar').addEventListener('click',function(){\n"
            + "  var d={codigo:document.getElementById('nCodigo').value.trim(),nombre:document.getElementById('nNombre').value.trim(),\n"
            + "    cantidad:document.getElementById('nCantidad').value||'0',precioMayoreo:document.getElementById('nMayoreo').value||'0',\n"
            + "    precioMenudeo:document.getElementById('nMenudeo').value||'0',idCategoria:document.getElementById('nCategoria').value,\n"
            + "    maxDescuento:document.getElementById('nMaxDesc').value||'100'};\n"
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
            + "  div.innerHTML='<div class=\"tarjeta\"><h2>'+escHtml(p.nombre)+'</h2>'\n"
            + "    +'<div class=\"fila\"><span class=\"etiqueta\">Código</span><span class=\"valor\">'+escHtml(p.id_producto)+'</span></div>'\n"
            + "    +'<div class=\"fila\"><span class=\"etiqueta\">Categoría</span><span class=\"valor\">'+escHtml(p.categoria)+'</span></div>'\n"
            + "    +'<div class=\"fila\"><span class=\"etiqueta\">Cantidad en stock</span><span class=\"valor '+sc+'\">'+p.cantidad+'</span></div>'\n"
            + "    +'<div class=\"fila\"><span class=\"etiqueta\">Precio mayoreo</span><span class=\"valor\">$'+fmtNum(p.precio_mayoreo)+'</span></div>'\n"
            + "    +'<div class=\"fila\"><span class=\"etiqueta\">Precio menudeo</span><span class=\"valor\">$'+fmtNum(p.precio_menudeo)+'</span></div>'\n"
            + "    +'<div class=\"fila\"><span class=\"etiqueta\">Desc. máximo</span><span class=\"valor\">'+p.max_descuento+'%</span></div></div>';\n"
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
            + "  document.getElementById('eForm').style.display='block';\n"
            + "}\n"
            + "document.getElementById('eGuardar').addEventListener('click',function(){\n"
            + "  var d=JSON.stringify({codigo:editarCodigo,nombre:document.getElementById('eNombre').value.trim(),\n"
            + "    cantidad:document.getElementById('eCantidad').value||'0',precioMayoreo:document.getElementById('eMayoreo').value||'0',\n"
            + "    precioMenudeo:document.getElementById('eMenudeo').value||'0',idCategoria:document.getElementById('eCategoria').value,\n"
            + "    maxDescuento:document.getElementById('eMaxDesc').value||'100'});\n"
            + "  if(wsListo){enviarWS('editar-guardar','editar:'+d);}\n"
            + "  else{pendiente='editar-guardar';fetch('/producto',{method:'POST',headers:{'Content-Type':'application/json'},body:d}).then(function(r){return r.json();}).then(function(r){respuestaEditar(r);siguienteCola();}).catch(function(){respuestaEditar({error:'Error de conexión'});siguienteCola();});}\n"
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
    private Response servirCategorias() {
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
    // POST /producto  →  insertar producto en la BD
    // -------------------------------------------------------------------------
    private Response guardarProducto(IHTTPSession session) {
        try {
            // Leer cuerpo de la petición
            Map<String, String> archivos = new HashMap<>();
            session.parseBody(archivos);
            String cuerpo = archivos.get("postData");
            if (cuerpo == null || cuerpo.isBlank()) {
                return jsonResponse(Response.Status.BAD_REQUEST, "{\"error\":\"Cuerpo vacío\"}");
            }

            // Extraer campos del JSON
            String codigo       = extraerJson(cuerpo, "codigo");
            String nombre       = extraerJson(cuerpo, "nombre");
            String cantidad     = extraerJson(cuerpo, "cantidad");
            String mayoreo      = extraerJson(cuerpo, "precioMayoreo");
            String menudeo      = extraerJson(cuerpo, "precioMenudeo");
            String idCatStr     = extraerJson(cuerpo, "idCategoria");
            String maxDescStr   = extraerJson(cuerpo, "maxDescuento");

            if (codigo.isBlank() || nombre.isBlank()) {
                return jsonResponse(Response.Status.BAD_REQUEST,
                    "{\"error\":\"El código y el nombre son obligatorios\"}");
            }

            // Parsear valores numéricos con defaults seguros
            int    cant       = parsearInt(cantidad, 0);
            double pMayoreo   = parsearDouble(mayoreo, 0.0);
            double pMenudeo   = parsearDouble(menudeo, 0.0);
            int    idCat      = parsearInt(idCatStr, 0);
            double maxDesc    = parsearDouble(maxDescStr, 100.0);

            // Insertar con PreparedStatement
            String sql = "INSERT INTO producto(id_producto, nombre, cantidad, "
                       + "precio_mayoreo, precio_menudeo, id_categoria, max_descuento) "
                       + "VALUES (?,?,?,?,?,?,?)";

            PreparedStatement ps = getConn().prepareStatement(sql);
            ps.setString(1, codigo);
            ps.setString(2, nombre);
            ps.setInt(3, cant);
            ps.setDouble(4, pMayoreo);
            ps.setDouble(5, pMenudeo);
            if (idCat > 0) {
                ps.setInt(6, idCat);
            } else {
                ps.setNull(6, Types.INTEGER);
            }
            ps.setDouble(7, maxDesc);
            ps.executeUpdate();

            return jsonResponse(Response.Status.OK, "{\"ok\":true}");

        } catch (ResponseException | java.io.IOException e) {
            return jsonResponse(Response.Status.BAD_REQUEST,
                "{\"error\":\"Error al leer la petición\"}");
        } catch (SQLException e) {
            String msg = e.getMessage() != null ? e.getMessage() : "Error de base de datos";
            // Mensaje amigable para código duplicado
            if (msg.contains("duplicate key") || msg.contains("already exists")) {
                msg = "Ya existe un producto con ese código";
            }
            return jsonResponse(Response.Status.INTERNAL_ERROR,
                "{\"error\":\"" + escaparJson(msg) + "\"}");
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
