package puntoventa.web;

import static pruebas.Prueba.*;
import static pruebas.BD.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.cert.X509Certificate;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/** Casos CU-WEB: servidor de inventario para el celular (HTTPS en el puerto 8765). */
public class WebInventarioPruebaBD {
    static HttpClient cliente;
    static String base;

    public static void main(String[] a) throws Exception {
        // certificado autofirmado sin SAN: se acepta tal cual, igual que el celular tras la advertencia
        System.setProperty("jdk.internal.httpclient.disableHostnameVerification", "true");
        SSLContext ssl = SSLContext.getInstance("TLS");
        ssl.init(null, new TrustManager[]{new X509TrustManager() {
            public void checkClientTrusted(X509Certificate[] c, String t) {}
            public void checkServerTrusted(X509Certificate[] c, String t) {}
            public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
        }}, null);
        cliente = HttpClient.newBuilder().sslContext(ssl).build();
        base = "https://127.0.0.1:" + WebInventario.PUERTO;

        WebInventario srv = new WebInventario();
        srv.start(5000, true);
        try {
            // CU-WEB-01 la página carga por HTTPS
            HttpResponse<String> r = get("/");
            check(r.statusCode() == 200 && r.body().contains("<html"), "CU-WEB-01 página principal por HTTPS: " + r.statusCode());
            check(get("/quagga.min.js").statusCode() == 200, "CU-WEB-01 lector de códigos (quagga.min.js)");
            check(get("/noexiste").statusCode() == 404, "CU-WEB-01 ruta desconocida -> 404");

            // CU-WEB-02 consultar un producto de ESTA base (la de db.properties)
            String p = producto("WEB", 7, 12.5, 10.0);
            r = get("/buscar?codigo=" + p);
            boolean baseDePruebas = r.body().contains("\"id_producto\":\"" + p + "\"");
            check(baseDePruebas,
                    "CU-WEB-02 el servidor móvil debe usar la base configurada en db.properties; respondió: " + recorta(r.body()));
            check(get("/buscar").statusCode() == 400, "CU-WEB-02 sin código -> 400");

            // CU-WEB-03 listas y categorías
            check(get("/listas").body().contains("Menudeo"), "CU-WEB-03 listas de precios: " + recorta(get("/listas").body()));
            check(get("/categorias").statusCode() == 200, "CU-WEB-03 categorías");

            // Seguridad: si el servidor no está conectado a la base de pruebas, las altas
            // irían a otra base (p. ej. la real en localhost:5432). No se escribe nada.
            if (!baseDePruebas) {
                check(false, "CU-WEB-04 OMITIDO: el servidor no apunta a la base de pruebas; no se envían altas ni ediciones");
            } else {
            // CU-WEB-04 alta y edición desde el celular
            String nuevo = "TWEBN" + siguiente();
            r = post("/producto", "{\"codigo\":\"" + nuevo + "\",\"nombre\":\"Desde celular\",\"cantidad\":\"3\",\"precioMenudeo\":\"20\",\"precioMayoreo\":\"18\",\"precios\":\"\"}");
            check(r.body().contains("\"ok\":true") && stock(nuevo) == 3, "CU-WEB-04 alta: " + recorta(r.body()));
            r = post("/producto/editar", "{\"codigo\":\"" + nuevo + "\",\"nombre\":\"Editado\",\"cantidad\":\"3\",\"precioMenudeo\":\"25\",\"precioMayoreo\":\"18\",\"precios\":\"\"}");
            check(r.body().contains("\"ok\":true") && num("SELECT precio_menudeo FROM producto WHERE id_producto=?", nuevo) == 25.0,
                    "CU-WEB-04 edición: " + recorta(r.body()));
            r = post("/producto", "{\"codigo\":\"" + nuevo + "\",\"nombre\":\"Duplicado\"}");
            check(r.body().contains("Ya existe"), "CU-WEB-04 código repetido: " + recorta(r.body()));
            r = post("/producto", "{\"codigo\":\"\",\"nombre\":\"\"}");
            check(r.body().contains("obligatorios"), "CU-WEB-04 campos obligatorios");
            }
        } finally {
            srv.stop();
        }
        terminar("WebInventarioPruebaBD");
    }

    static String recorta(String s) { return s.length() > 160 ? s.substring(0, 160) + "…" : s; }

    static HttpResponse<String> get(String ruta) throws Exception {
        return cliente.send(HttpRequest.newBuilder(URI.create(base + ruta)).GET().build(), HttpResponse.BodyHandlers.ofString());
    }

    static HttpResponse<String> post(String ruta, String json) throws Exception {
        return cliente.send(HttpRequest.newBuilder(URI.create(base + ruta)).header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json)).build(), HttpResponse.BodyHandlers.ofString());
    }
}
