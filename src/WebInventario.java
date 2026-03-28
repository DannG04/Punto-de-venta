import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoWSD;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
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

    /** Devuelve la IP local de la máquina en la red LAN (excluye link-local 169.254.x.x) */
    public static String obtenerIPLocal() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface ni = interfaces.nextElement();
                if (ni.isLoopback() || !ni.isUp()) continue;
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

        if (uri.equals("/") && method == Method.GET) {
            return servirFormulario();
        } else if (uri.equals("/consulta") && method == Method.GET) {
            return servirConsulta();
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
    // GET /  →  formulario HTML
    // -------------------------------------------------------------------------
    private Response servirFormulario() {
        String html = "<!DOCTYPE html>\n"
            + "<html lang=\"es\">\n"
            + "<head>\n"
            + "  <meta charset=\"UTF-8\">\n"
            + "  <meta name=\"viewport\" content=\"width=device-width,initial-scale=1.0,maximum-scale=1.0\">\n"
            + "  <title>Registrar Producto</title>\n"
            + "  <style>\n"
            + "    *{box-sizing:border-box;margin:0;padding:0}\n"
            + "    body{font-family:sans-serif;background:#f0f4f8;padding:16px;max-width:520px;margin:0 auto}\n"
            + "    h1{text-align:center;color:#4e9696;font-size:1.5em;margin-bottom:16px;padding-top:8px}\n"
            + "    nav{display:flex;gap:8px;margin-bottom:16px}\n"
            + "    nav a{flex:1;text-align:center;padding:10px;border-radius:8px;text-decoration:none;font-weight:bold;font-size:.95em;border:2px solid #4e9696;color:#4e9696}\n"
            + "    nav a.activo{background:#4e9696;color:#fff}\n"
            + "    .campo{margin-top:12px}\n"
            + "    label{display:block;font-weight:bold;color:#444;margin-bottom:4px;font-size:.95em}\n"
            + "    input,select{width:100%;padding:12px;border:1px solid #ccc;border-radius:8px;font-size:1em;background:#fff}\n"
            + "    input:focus,select:focus{outline:none;border-color:#4e9696;box-shadow:0 0 0 2px #4e969633}\n"
            + "    .btn-scan{background:#4e9696;color:#fff;border:none;border-radius:8px;padding:12px;font-size:1em;width:100%;margin-top:8px;cursor:pointer}\n"
            + "    .btn-scan:active{background:#3a7a7a}\n"
            + "    .btn-guardar{background:#4e9696;color:#fff;border:none;border-radius:8px;padding:16px;font-size:1.1em;font-weight:bold;width:100%;margin-top:24px;cursor:pointer}\n"
            + "    .btn-guardar:active{background:#3a7a7a}\n"
            + "    #visor{position:relative;width:100%;margin-top:8px;display:none;background:#000;border-radius:8px;overflow:hidden}\n"
            + "    #visor video{width:100%;display:block}\n"
            + "    #visor canvas.drawingBuffer{position:absolute;top:0;left:0;width:100%;height:100%}\n"
            + "    #linea{position:absolute;top:50%;left:5%;width:90%;height:2px;background:#4e9696;pointer-events:none}\n"
            + "    #status{text-align:center;margin-top:14px;padding:12px;border-radius:8px;display:none;font-size:.95em}\n"
            + "    .ok{background:#d4edda;color:#155724}\n"
            + "    .err{background:#f8d7da;color:#721c24}\n"
            + "  </style>\n"
            + "</head>\n"
            + "<body>\n"
            + "  <nav>\n"
            + "    <a href=\"/\" class=\"activo\">&#x2795; Registrar</a>\n"
            + "    <a href=\"/consulta\">&#x1F50D; Consultar</a>\n"
            + "  </nav>\n"
            + "  <h1>Registrar Producto</h1>\n"
            + "\n"
            + "  <div class=\"campo\">\n"
            + "    <label>Código de barras</label>\n"
            + "    <input type=\"text\" id=\"codigo\" placeholder=\"Escanea o escribe el código\">\n"
            + "    <button class=\"btn-scan\" id=\"scanBtn\">&#x1F4F7; Abrir escáner</button>\n"
            + "    <div id=\"visor\"><div id=\"linea\"></div></div>\n"
            + "    <button class=\"btn-scan\" id=\"stopBtn\" style=\"display:none;background:#c0392b\">&#x23F9; Cerrar escáner</button>\n"
            + "  </div>\n"
            + "\n"
            + "  <div class=\"campo\">\n"
            + "    <label>Nombre del producto</label>\n"
            + "    <input type=\"text\" id=\"nombre\" placeholder=\"Nombre\" autocomplete=\"off\">\n"
            + "  </div>\n"
            + "  <div class=\"campo\">\n"
            + "    <label>Cantidad</label>\n"
            + "    <input type=\"number\" id=\"cantidad\" value=\"1\" min=\"0\" inputmode=\"numeric\">\n"
            + "  </div>\n"
            + "  <div class=\"campo\">\n"
            + "    <label>Precio mayoreo</label>\n"
            + "    <input type=\"number\" id=\"precioMayoreo\" step=\"0.01\" placeholder=\"0.00\" inputmode=\"decimal\">\n"
            + "  </div>\n"
            + "  <div class=\"campo\">\n"
            + "    <label>Precio menudeo</label>\n"
            + "    <input type=\"number\" id=\"precioMenudeo\" step=\"0.01\" placeholder=\"0.00\" inputmode=\"decimal\">\n"
            + "  </div>\n"
            + "  <div class=\"campo\">\n"
            + "    <label>Categoría <span style=\"font-weight:normal;color:#888\">(opcional)</span></label>\n"
            + "    <select id=\"categoria\"><option value=\"0\">Sin categoría</option></select>\n"
            + "  </div>\n"
            + "  <div class=\"campo\">\n"
            + "    <label>Descuento máximo (%)</label>\n"
            + "    <input type=\"number\" id=\"maxDescuento\" value=\"100\" min=\"0\" max=\"100\" inputmode=\"numeric\">\n"
            + "  </div>\n"
            + "\n"
            + "  <button class=\"btn-guardar\" id=\"submitBtn\">Guardar producto</button>\n"
            + "  <div id=\"status\"></div>\n"
            + "\n"
            + "  <script src=\"/quagga.min.js\"></script>\n"
            + "  <script>\n"
            + "    // --- WebSocket con fallback HTTP ---\n"
            + "    let ws,wsListo=false,wsPing;\n"
            + "    function conectarWS(){\n"
            + "      ws=new WebSocket('wss://'+location.host);\n"
            + "      ws.onopen=()=>{\n"
            + "        wsListo=true;\n"
            + "        ws.send('categorias');\n"
            + "      };\n"
            + "      ws.onmessage=(e)=>{\n"
            + "        try{\n"
            + "          const data=JSON.parse(e.data);\n"
            + "          if(Array.isArray(data)){cargarCategorias(data);}\n"
            + "          else{manejarRespuestaGuardado(data);}\n"
            + "        }catch(err){}\n"
            + "      };\n"
            + "      ws.onclose=()=>{wsListo=false;clearInterval(wsPing);setTimeout(conectarWS,2000);};\n"
            + "      ws.onerror=()=>{wsListo=false;};\n"
            + "      wsPing=setInterval(()=>{if(wsListo)ws.send('ping');},3000);\n"
            + "    }\n"
            + "    conectarWS();\n"
            + "\n"
            + "    // Fallback HTTP para categorías\n"
            + "    fetch('/categorias').then(r=>r.json()).then(cargarCategorias).catch(()=>{});\n"
            + "\n"
            + "    function cargarCategorias(cats){\n"
            + "      const sel=document.getElementById('categoria');\n"
            + "      if(sel.options.length>1) return;\n"
            + "      cats.forEach(c=>{\n"
            + "        const o=document.createElement('option');\n"
            + "        o.value=c.id; o.textContent=c.nombre; sel.appendChild(o);\n"
            + "      });\n"
            + "    }\n"
            + "\n"
            + "    // --- Escáner de código de barras ---\n"
            + "    let escaneando=false;\n"
            + "    const visor=document.getElementById('visor');\n"
            + "    const scanBtn=document.getElementById('scanBtn');\n"
            + "    const stopBtn=document.getElementById('stopBtn');\n"
            + "\n"
            + "    function detener(){\n"
            + "      if(!escaneando) return;\n"
            + "      Quagga.stop();\n"
            + "      escaneando=false;\n"
            + "      visor.style.display='none';\n"
            + "      scanBtn.style.display='block';\n"
            + "      stopBtn.style.display='none';\n"
            + "    }\n"
            + "\n"
            + "    scanBtn.addEventListener('click',()=>{\n"
            + "      visor.style.display='block';\n"
            + "      scanBtn.style.display='none';\n"
            + "      stopBtn.style.display='block';\n"
            + "      Quagga.init({\n"
            + "        inputStream:{type:'LiveStream',target:visor,\n"
            + "          constraints:{facingMode:'environment',width:{ideal:1280},height:{ideal:720}}},\n"
            + "        locator:{patchSize:'medium',halfSample:true},\n"
            + "        decoder:{readers:['ean_reader','ean_8_reader','code_128_reader','upc_reader','upc_e_reader']},\n"
            + "        locate:true\n"
            + "      },function(err){\n"
            + "        if(err){mostrarEstado('Error al abrir la cámara: '+err,false);detener();return;}\n"
            + "        Quagga.start(); escaneando=true;\n"
            + "      });\n"
            + "      let ultimoCodigo='',conteo=0;\n"
            + "      Quagga.onDetected(function(data){\n"
            + "        const cod=data.codeResult.code;\n"
            + "        if(cod===ultimoCodigo){conteo++;}else{ultimoCodigo=cod;conteo=1;}\n"
            + "        if(conteo>=3){\n"
            + "          detener();\n"
            + "          document.getElementById('codigo').value=cod;\n"
            + "          document.getElementById('nombre').focus();\n"
            + "          ultimoCodigo='';conteo=0;\n"
            + "        }\n"
            + "      });\n"
            + "    });\n"
            + "    stopBtn.addEventListener('click',detener);\n"
            + "\n"
            + "    // --- Guardar producto ---\n"
            + "    document.getElementById('submitBtn').addEventListener('click',()=>{\n"
            + "      const datos={\n"
            + "        codigo:document.getElementById('codigo').value.trim(),\n"
            + "        nombre:document.getElementById('nombre').value.trim(),\n"
            + "        cantidad:document.getElementById('cantidad').value||'0',\n"
            + "        precioMayoreo:document.getElementById('precioMayoreo').value||'0',\n"
            + "        precioMenudeo:document.getElementById('precioMenudeo').value||'0',\n"
            + "        idCategoria:document.getElementById('categoria').value,\n"
            + "        maxDescuento:document.getElementById('maxDescuento').value||'100'\n"
            + "      };\n"
            + "      if(!datos.codigo||!datos.nombre){\n"
            + "        mostrarEstado('El código y el nombre son obligatorios',false); return;\n"
            + "      }\n"
            + "      const btn=document.getElementById('submitBtn');\n"
            + "      btn.disabled=true; btn.textContent='Guardando...';\n"
            + "      if(wsListo){\n"
            + "        ws.send(JSON.stringify(datos));\n"
            + "      }else{\n"
            + "        fetch('/producto',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify(datos)})\n"
            + "          .then(r=>r.json()).then(manejarRespuestaGuardado)\n"
            + "          .catch(()=>manejarRespuestaGuardado({error:'Error de conexión'}));\n"
            + "      }\n"
            + "    });\n"
            + "\n"
            + "    function manejarRespuestaGuardado(json){\n"
            + "      const btn=document.getElementById('submitBtn');\n"
            + "      btn.disabled=false; btn.textContent='Guardar producto';\n"
            + "      if(json.ok){\n"
            + "        mostrarEstado('Producto guardado correctamente',true);\n"
            + "        document.getElementById('codigo').value='';\n"
            + "        document.getElementById('nombre').value='';\n"
            + "        document.getElementById('cantidad').value='1';\n"
            + "        document.getElementById('precioMayoreo').value='';\n"
            + "        document.getElementById('precioMenudeo').value='';\n"
            + "        document.getElementById('categoria').value='0';\n"
            + "        document.getElementById('maxDescuento').value='100';\n"
            + "      }else{\n"
            + "        mostrarEstado('Error: '+(json.error||'Error desconocido'),false);\n"
            + "      }\n"
            + "    }\n"
            + "\n"
            + "    function mostrarEstado(msg,ok){\n"
            + "      const el=document.getElementById('status');\n"
            + "      el.textContent=msg; el.className=ok?'ok':'err'; el.style.display='block';\n"
            + "      setTimeout(()=>{el.style.display='none';},5000);\n"
            + "    }\n"
            + "  </script>\n"
            + "</body>\n"
            + "</html>\n";

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
    // GET /consulta  →  página para consultar producto por código de barras
    // -------------------------------------------------------------------------
    private Response servirConsulta() {
        String html = "<!DOCTYPE html>\n"
            + "<html lang=\"es\">\n"
            + "<head>\n"
            + "  <meta charset=\"UTF-8\">\n"
            + "  <meta name=\"viewport\" content=\"width=device-width,initial-scale=1.0,maximum-scale=1.0\">\n"
            + "  <title>Consultar Producto</title>\n"
            + "  <style>\n"
            + "    *{box-sizing:border-box;margin:0;padding:0}\n"
            + "    body{font-family:sans-serif;background:#f0f4f8;padding:16px;max-width:520px;margin:0 auto}\n"
            + "    h1{text-align:center;color:#4e9696;font-size:1.5em;margin-bottom:16px;padding-top:8px}\n"
            + "    nav{display:flex;gap:8px;margin-bottom:16px}\n"
            + "    nav a{flex:1;text-align:center;padding:10px;border-radius:8px;text-decoration:none;font-weight:bold;font-size:.95em;border:2px solid #4e9696;color:#4e9696}\n"
            + "    nav a.activo{background:#4e9696;color:#fff}\n"
            + "    .campo{margin-top:12px}\n"
            + "    label{display:block;font-weight:bold;color:#444;margin-bottom:4px;font-size:.95em}\n"
            + "    input{width:100%;padding:12px;border:1px solid #ccc;border-radius:8px;font-size:1em;background:#fff}\n"
            + "    input:focus{outline:none;border-color:#4e9696;box-shadow:0 0 0 2px #4e969633}\n"
            + "    .btn-scan{background:#4e9696;color:#fff;border:none;border-radius:8px;padding:12px;font-size:1em;width:100%;margin-top:8px;cursor:pointer}\n"
            + "    .btn-scan:active{background:#3a7a7a}\n"
            + "    #visor{position:relative;width:100%;margin-top:8px;display:none;background:#000;border-radius:8px;overflow:hidden}\n"
            + "    #visor video{width:100%;display:block}\n"
            + "    #visor canvas.drawingBuffer{position:absolute;top:0;left:0;width:100%;height:100%}\n"
            + "    #linea{position:absolute;top:50%;left:5%;width:90%;height:2px;background:#4e9696;pointer-events:none}\n"
            + "    #resultado{margin-top:20px;display:none}\n"
            + "    .tarjeta{background:#fff;border-radius:12px;padding:20px;box-shadow:0 2px 8px #0001}\n"
            + "    .tarjeta h2{color:#4e9696;font-size:1.15em;margin-bottom:14px;border-bottom:1px solid #e0e0e0;padding-bottom:8px}\n"
            + "    .fila{display:flex;justify-content:space-between;padding:7px 0;border-bottom:1px solid #f0f0f0;font-size:.97em}\n"
            + "    .fila:last-child{border-bottom:none}\n"
            + "    .fila .etiqueta{color:#666;font-weight:bold}\n"
            + "    .fila .valor{color:#222;text-align:right;max-width:60%}\n"
            + "    .stock-ok{color:#27ae60;font-weight:bold}\n"
            + "    .stock-bajo{color:#e67e22;font-weight:bold}\n"
            + "    .stock-cero{color:#c0392b;font-weight:bold}\n"
            + "    .err-card{background:#f8d7da;color:#721c24;border-radius:12px;padding:16px;text-align:center;margin-top:20px;font-weight:bold}\n"
            + "    #cargando{display:none;margin-top:28px;text-align:center}\n"
            + "    .spinner{display:inline-block;width:40px;height:40px;border:4px solid #cde8e8;border-top-color:#4e9696;border-radius:50%;animation:giro .7s linear infinite}\n"
            + "    @keyframes giro{to{transform:rotate(360deg)}}\n"
            + "    #cargando p{margin-top:10px;color:#4e9696;font-size:.95em;font-weight:bold}\n"
            + "  </style>\n"
            + "</head>\n"
            + "<body>\n"
            + "  <nav>\n"
            + "    <a href=\"/\">&#x2795; Registrar</a>\n"
            + "    <a href=\"/consulta\" class=\"activo\">&#x1F50D; Consultar</a>\n"
            + "  </nav>\n"
            + "  <h1>Consultar Producto</h1>\n"
            + "\n"
            + "  <div class=\"campo\">\n"
            + "    <label>Código de barras</label>\n"
            + "    <input type=\"text\" id=\"codigo\" placeholder=\"Escanea o escribe el código\" inputmode=\"text\">\n"
            + "    <button class=\"btn-scan\" id=\"scanBtn\">&#x1F4F7; Abrir escáner</button>\n"
            + "    <div id=\"visor\"><div id=\"linea\"></div></div>\n"
            + "    <button class=\"btn-scan\" id=\"stopBtn\" style=\"display:none;background:#c0392b\">&#x23F9; Cerrar escáner</button>\n"
            + "  </div>\n"
            + "\n"
            + "  <div id=\"cargando\"><div class=\"spinner\"></div><p>Buscando producto...</p></div>\n"
            + "  <div id=\"resultado\"></div>\n"
            + "\n"
            + "  <script src=\"/quagga.min.js\"></script>\n"
            + "  <script>\n"
            + "    // --- WebSocket: conexión persistente para consultas instantáneas ---\n"
            + "    let ws, wsListo=false;\n"
            + "    function conectarWS(){\n"
            + "      ws=new WebSocket('wss://'+location.host);\n"
            + "      ws.onopen=()=>{wsListo=true;};\n"
            + "      ws.onmessage=(e)=>{\n"
            + "        try{mostrarResultado(JSON.parse(e.data));}catch(err){mostrarResultado({error:'Error al procesar respuesta'});}\n"
            + "      };\n"
            + "      ws.onclose=()=>{wsListo=false;clearInterval(wsPing);setTimeout(conectarWS,2000);};\n"
            + "      ws.onerror=()=>{wsListo=false;};\n"
            + "      wsPing=setInterval(()=>{if(wsListo)ws.send('ping');},3000);\n"
            + "    }\n"
            + "    let wsPing;\n"
            + "    conectarWS();\n"
            + "\n"
            + "    function buscar(codigo){\n"
            + "      document.getElementById('resultado').style.display='none';\n"
            + "      document.getElementById('cargando').style.display='block';\n"
            + "      if(wsListo){\n"
            + "        ws.send(codigo);\n"
            + "      }else{\n"
            + "        fetch('/buscar?codigo='+encodeURIComponent(codigo))\n"
            + "          .then(r=>r.json()).then(mostrarResultado)\n"
            + "          .catch(()=>mostrarResultado({error:'Error de conexión'}));\n"
            + "      }\n"
            + "    }\n"
            + "\n"
            + "    // --- Escáner de código de barras ---\n"
            + "    let escaneando=false;\n"
            + "    const visor=document.getElementById('visor');\n"
            + "    const scanBtn=document.getElementById('scanBtn');\n"
            + "    const stopBtn=document.getElementById('stopBtn');\n"
            + "    const codigoInput=document.getElementById('codigo');\n"
            + "\n"
            + "    function detener(){\n"
            + "      if(!escaneando) return;\n"
            + "      Quagga.stop();\n"
            + "      escaneando=false;\n"
            + "      visor.style.display='none';\n"
            + "      scanBtn.style.display='block';\n"
            + "      stopBtn.style.display='none';\n"
            + "    }\n"
            + "\n"
            + "    scanBtn.addEventListener('click',()=>{\n"
            + "      visor.style.display='block';\n"
            + "      scanBtn.style.display='none';\n"
            + "      stopBtn.style.display='block';\n"
            + "      Quagga.init({\n"
            + "        inputStream:{\n"
            + "          type:'LiveStream',\n"
            + "          target:visor,\n"
            + "          constraints:{facingMode:'environment',width:{ideal:1280},height:{ideal:720}}\n"
            + "        },\n"
            + "        locator:{patchSize:'medium',halfSample:true},\n"
            + "        decoder:{readers:['ean_reader','ean_8_reader','code_128_reader','upc_reader','upc_e_reader']},\n"
            + "        locate:true\n"
            + "      },function(err){\n"
            + "        if(err){mostrarError('Error al abrir la cámara: '+err);detener();return;}\n"
            + "        Quagga.start();\n"
            + "        escaneando=true;\n"
            + "      });\n"
            + "      let ultimoCodigo='',conteo=0;\n"
            + "      Quagga.onDetected(function(data){\n"
            + "        const cod=data.codeResult.code;\n"
            + "        if(cod===ultimoCodigo){conteo++;}else{ultimoCodigo=cod;conteo=1;}\n"
            + "        if(conteo>=3){\n"
            + "          detener();\n"
            + "          codigoInput.value=cod;\n"
            + "          buscar(cod);\n"
            + "          ultimoCodigo='';conteo=0;\n"
            + "        }\n"
            + "      });\n"
            + "    });\n"
            + "\n"
            + "    stopBtn.addEventListener('click',detener);\n"
            + "\n"
            + "    codigoInput.addEventListener('keydown',function(e){\n"
            + "      if(e.key==='Enter'&&this.value.trim()) buscar(this.value.trim());\n"
            + "    });\n"
            + "\n"
            + "    function mostrarResultado(p){\n"
            + "      document.getElementById('cargando').style.display='none';\n"
            + "      const div=document.getElementById('resultado');\n"
            + "      if(p.error){\n"
            + "        div.innerHTML='<div class=\"err-card\">'+p.error+'</div>';\n"
            + "      }else{\n"
            + "        let sc='stock-ok';\n"
            + "        if(p.cantidad===0)sc='stock-cero';\n"
            + "        else if(p.cantidad<=5)sc='stock-bajo';\n"
            + "        div.innerHTML=\n"
            + "          '<div class=\"tarjeta\">'\n"
            + "          +'<h2>'+escHtml(p.nombre)+'</h2>'\n"
            + "          +'<div class=\"fila\"><span class=\"etiqueta\">Código</span><span class=\"valor\">'+escHtml(p.id_producto)+'</span></div>'\n"
            + "          +'<div class=\"fila\"><span class=\"etiqueta\">Categoría</span><span class=\"valor\">'+escHtml(p.categoria)+'</span></div>'\n"
            + "          +'<div class=\"fila\"><span class=\"etiqueta\">Cantidad en stock</span><span class=\"valor '+sc+'\">'+p.cantidad+'</span></div>'\n"
            + "          +'<div class=\"fila\"><span class=\"etiqueta\">Precio mayoreo</span><span class=\"valor\">$'+fmtNum(p.precio_mayoreo)+'</span></div>'\n"
            + "          +'<div class=\"fila\"><span class=\"etiqueta\">Precio menudeo</span><span class=\"valor\">$'+fmtNum(p.precio_menudeo)+'</span></div>'\n"
            + "          +'<div class=\"fila\"><span class=\"etiqueta\">Desc. máximo</span><span class=\"valor\">'+p.max_descuento+'%</span></div>'\n"
            + "          +'</div>';\n"
            + "      }\n"
            + "      div.style.display='block';\n"
            + "    }\n"
            + "    function fmtNum(n){return Number(n).toFixed(2);}\n"
            + "    function escHtml(s){const d=document.createElement('div');d.textContent=s||'';return d.innerHTML;}\n"
            + "    function mostrarError(msg){const d=document.getElementById('resultado');d.innerHTML='<div class=\"err-card\">'+msg+'</div>';d.style.display='block';}\n"
            + "  </script>\n"
            + "</body>\n"
            + "</html>\n";

        return corsResponse(newFixedLengthResponse(Response.Status.OK, "text/html; charset=utf-8", html));
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
