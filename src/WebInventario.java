import fi.iki.elonen.NanoHTTPD;
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
 */
public class WebInventario extends NanoHTTPD {

    public static final int PUERTO = 8765;

    // Mismos parámetros que ConexionBD
    private final String url = "jdbc:postgresql://localhost:5432/";
    private final String nameBD = "punto_de_venta";
    private final String usuario = "postgres";
    private final String contra = "Daniel183.";

    private Connection conn = null;

    private Connection getConn() throws SQLException {
        if (conn == null || conn.isClosed()) {
            conn = DriverManager.getConnection(url + nameBD, usuario, contra);
        }
        return conn;
    }

    public WebInventario() throws IOException {
        super(PUERTO);
        // Evita reverse DNS lookup en cada conexión (causa principal de lentitud en LAN sin DNS)
        System.setProperty("java.net.preferIPv4Stack", "true");
        System.setProperty("sun.net.inetaddr.ttl", "60");
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

    @Override
    public Response serve(IHTTPSession session) {
        String uri = session.getUri();
        Method method = session.getMethod();

        // CORS preflight
        if (method == Method.OPTIONS) {
            return corsResponse(newFixedLengthResponse(""));
        }

        if (uri.equals("/") && method == Method.GET) {
            return servirFormulario();
        } else if (uri.equals("/quagga.min.js") && method == Method.GET) {
            return servirArchivoEstatico("quagga.min.js", "application/javascript");
        } else if (uri.equals("/categorias") && method == Method.GET) {
            return servirCategorias();
        } else if (uri.equals("/producto") && method == Method.POST) {
            return guardarProducto(session);
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
            + "    fetch('/categorias').then(r=>r.json()).then(cats=>{\n"
            + "      const sel=document.getElementById('categoria');\n"
            + "      cats.forEach(c=>{\n"
            + "        const o=document.createElement('option');\n"
            + "        o.value=c.id; o.textContent=c.nombre; sel.appendChild(o);\n"
            + "      });\n"
            + "    }).catch(()=>{});\n"
            + "\n"
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
            + "        inputStream:{\n"
            + "          type:'LiveStream',\n"
            + "          target: visor,\n"
            + "          constraints:{ facingMode:'environment', width:{ideal:1280}, height:{ideal:720} }\n"
            + "        },\n"
            + "        locator:{ patchSize:'medium', halfSample:true },\n"
            + "        decoder:{ readers:['ean_reader','ean_8_reader','code_128_reader','upc_reader','upc_e_reader'] },\n"
            + "        locate: true\n"
            + "      }, function(err){\n"
            + "        if(err){ mostrarEstado('Error al abrir la cámara: '+err, false); detener(); return; }\n"
            + "        Quagga.start();\n"
            + "        escaneando=true;\n"
            + "      });\n"
            + "\n"
            + "      // Requiere al menos 3 detecciones iguales consecutivas para evitar falsos positivos\n"
            + "      let ultimoCodigo='', conteo=0;\n"
            + "      Quagga.onDetected(function(data){\n"
            + "        const cod=data.codeResult.code;\n"
            + "        if(cod===ultimoCodigo){ conteo++; } else { ultimoCodigo=cod; conteo=1; }\n"
            + "        if(conteo>=3){\n"
            + "          detener();\n"
            + "          document.getElementById('codigo').value=cod;\n"
            + "          document.getElementById('nombre').focus();\n"
            + "          ultimoCodigo=''; conteo=0;\n"
            + "        }\n"
            + "      });\n"
            + "    });\n"
            + "\n"
            + "    stopBtn.addEventListener('click', detener);\n"
            + "\n"
            + "\n"
            + "    // Guardar producto\n"
            + "    document.getElementById('submitBtn').addEventListener('click',async()=>{\n"
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
            + "        mostrarEstado('El código y el nombre son obligatorios',false);\n"
            + "        return;\n"
            + "      }\n"
            + "      const btn=document.getElementById('submitBtn');\n"
            + "      btn.disabled=true; btn.textContent='Guardando...';\n"
            + "      try{\n"
            + "        const res=await fetch('/producto',{\n"
            + "          method:'POST',\n"
            + "          headers:{'Content-Type':'application/json'},\n"
            + "          body:JSON.stringify(datos)\n"
            + "        });\n"
            + "        const json=await res.json();\n"
            + "        if(json.ok){\n"
            + "          mostrarEstado('Producto guardado correctamente',true);\n"
            + "          document.getElementById('codigo').value='';\n"
            + "          document.getElementById('nombre').value='';\n"
            + "          document.getElementById('cantidad').value='1';\n"
            + "          document.getElementById('precioMayoreo').value='';\n"
            + "          document.getElementById('precioMenudeo').value='';\n"
            + "          document.getElementById('categoria').value='0';\n"
            + "          document.getElementById('maxDescuento').value='100';\n"
            + "        }else{\n"
            + "          mostrarEstado('Error: '+(json.error||'Error desconocido'),false);\n"
            + "        }\n"
            + "      }catch(e){\n"
            + "        mostrarEstado('Error de conexión: '+e.message,false);\n"
            + "      }finally{\n"
            + "        btn.disabled=false; btn.textContent='Guardar producto';\n"
            + "      }\n"
            + "    });\n"
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
