package puntoventa.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;
import java.util.function.DoubleConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import puntoventa.db.ConexionBD;

/**
 * Busca versiones publicadas en GitHub Releases, descarga y verifica el parche
 * y lanza installer/update/aplicar_actualizacion.bat en modo --auto.
 * Sin dependencias: HttpClient del JDK y un parseo puntual del JSON de GitHub.
 */
public final class Actualizador {

    static final String ENDPOINT = "https://api.github.com/repos/DannG04/Punto-de-venta/releases/latest";
    private static final Duration TIMEOUT = Duration.ofSeconds(10);
    private static final String USER_AGENT = "PuntoVenta-Updater";

    /** Versión publicada y las URLs de su parche y de la huella SHA-256. */
    public record Release(String version, String notas, LocalDate publicada, String urlZip, String urlSha) {}

    private Actualizador() {}

    private static HttpClient cliente() {
        return HttpClient.newBuilder()
                .connectTimeout(TIMEOUT)
                .followRedirects(HttpClient.Redirect.NORMAL) // los assets redirigen a otro host
                .build();
    }

    private static HttpRequest.Builder peticion(String url) {
        return HttpRequest.newBuilder(URI.create(url)).header("User-Agent", USER_AGENT);
    }

    /** Consulta el último release. Sin red, error HTTP o respuesta incompleta → null (nunca excepción). */
    public static Release buscar() {
        String url = System.getProperty("pdv.releases.url", ENDPOINT); // la propiedad solo se usa en pruebas
        try {
            HttpResponse<String> r = cliente().send(
                    peticion(url).timeout(TIMEOUT).header("Accept", "application/vnd.github+json").GET().build(),
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (r.statusCode() != 200) {
                GestorErrores.registrar(new IOException("HTTP " + r.statusCode() + " al consultar " + url), "Actualizador.buscar");
                return null;
            }
            return parsear(r.body());
        } catch (Exception | StackOverflowError e) { // nunca debe tumbar el aviso ni el hilo
            GestorErrores.registrar(e, "Actualizador.buscar");
            if (e instanceof InterruptedException) Thread.currentThread().interrupt();
            return null;
        }
    }

    /** Lee la respuesta de releases/latest. null si falta tag_name, el .zip o el .zip.sha256. */
    public static Release parsear(String json) {
        if (json == null) return null;
        String tag = cadena(json, "tag_name");
        if (tag == null || VersionApp.normalizar(tag).isEmpty()) return null;
        String urlZip = null, urlSha = null;
        for (String u : cadenas(json, "browser_download_url")) {
            if (u.endsWith(".zip.sha256")) urlSha = u;
            else if (u.endsWith(".zip")) urlZip = u;
        }
        if (urlZip == null || urlSha == null) return null;
        String notas = cadena(json, "body");
        LocalDate fecha = null;
        Matcher f = Pattern.compile("\"published_at\"\\s*:\\s*\"(\\d{4}-\\d{2}-\\d{2})").matcher(json);
        if (f.find()) fecha = LocalDate.parse(f.group(1));
        return new Release(VersionApp.normalizar(tag), notas == null ? "" : notas, fecha, urlZip, urlSha);
    }

    private static String cadena(String json, String campo) {
        List<String> v = cadenas(json, campo);
        return v.isEmpty() ? null : v.get(0);
    }

    /**
     * Valores de texto de todas las apariciones de "campo": "...". Lector iterativo:
     * un regex con alternancia por carácter desbordaba la pila con notas de ~3 KB.
     */
    private static List<String> cadenas(String json, String campo) {
        List<String> res = new java.util.ArrayList<>();
        String clave = "\"" + campo + "\"";
        int i = json.indexOf(clave);
        while (i >= 0) {
            int p = i + clave.length();
            while (p < json.length() && Character.isWhitespace(json.charAt(p))) p++;
            if (p < json.length() && json.charAt(p) == ':') {
                p++;
                while (p < json.length() && Character.isWhitespace(json.charAt(p))) p++;
                if (p < json.length() && json.charAt(p) == '"') {
                    int ini = ++p;
                    while (p < json.length() && json.charAt(p) != '"') p += json.charAt(p) == '\\' ? 2 : 1;
                    if (p < json.length()) res.add(desescapar(json.substring(ini, p)));
                }
            }
            i = json.indexOf(clave, i + clave.length());
        }
        return res;
    }

    private static String desescapar(String s) {
        StringBuilder sb = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c != '\\' || i + 1 >= s.length()) { sb.append(c); continue; }
            char n = s.charAt(++i);
            switch (n) {
                case 'n' -> sb.append('\n');
                case 'r' -> { }                        // las notas se muestran con \n
                case 't' -> sb.append('\t');
                case 'b', 'f' -> { }
                case 'u' -> {
                    if (i + 4 < s.length()) {
                        sb.append((char) Integer.parseInt(s.substring(i + 1, i + 5), 16));
                        i += 4;
                    }
                }
                default -> sb.append(n);               // \" \\ \/
            }
        }
        return sb.toString();
    }

    /** SHA-256 del archivo en hexadecimal minúsculas. */
    public static String sha256(Path archivo) throws IOException {
        try (InputStream in = Files.newInputStream(archivo)) {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] buf = new byte[64 * 1024];
            int n;
            while ((n = in.read(buf)) > 0) md.update(buf, 0, n);
            return HexFormat.of().formatHex(md.digest());
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new IOException(e);
        }
    }

    /**
     * Descarga el ZIP y su huella a %TEMP%\PuntoVenta-Update-&lt;v&gt;, verifica y descomprime.
     * Ante cualquier fallo borra la carpeta y lanza IOException con un mensaje para el usuario.
     */
    public static Path descargar(Release r, DoubleConsumer progreso) throws IOException {
        Path carpeta = Path.of(System.getProperty("java.io.tmpdir"), "PuntoVenta-Update-" + r.version());
        borrar(carpeta);
        try {
            Files.createDirectories(carpeta);
            Path zip = carpeta.resolve("PuntoVenta-Update-" + r.version() + ".zip");
            HttpClient http = cliente();
            HttpResponse<InputStream> resp = http.send(peticion(r.urlZip()).GET().build(), HttpResponse.BodyHandlers.ofInputStream());
            if (resp.statusCode() != 200) throw new IOException("El servidor respondió " + resp.statusCode() + " al descargar la actualización.");
            long total = resp.headers().firstValueAsLong("Content-Length").orElse(-1);
            try (InputStream in = resp.body(); OutputStream out = Files.newOutputStream(zip)) {
                byte[] buf = new byte[64 * 1024];
                long leidos = 0;
                int n;
                while ((n = in.read(buf)) > 0) {
                    if (Thread.currentThread().isInterrupted()) throw new IOException("Descarga cancelada.");
                    out.write(buf, 0, n);
                    leidos += n;
                    if (total > 0) progreso.accept(Math.min(1.0, (double) leidos / total));
                }
            }
            progreso.accept(1.0);

            HttpResponse<String> sha = http.send(peticion(r.urlSha()).timeout(TIMEOUT).GET().build(),
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (sha.statusCode() != 200) throw new IOException("No se pudo descargar la huella de verificación (HTTP " + sha.statusCode() + ").");
            String esperado = sha.body().trim().split("\\s+")[0];
            if (!esperado.equalsIgnoreCase(sha256(zip))) {
                throw new IOException("El archivo descargado está dañado o incompleto (la huella no coincide). Intente de nuevo.");
            }
            descomprimir(zip, carpeta);
            return carpeta;
        } catch (IOException e) {
            borrar(carpeta);
            throw e;
        } catch (InterruptedException e) {
            borrar(carpeta);
            Thread.currentThread().interrupt();
            throw new IOException("Descarga cancelada.", e);
        } catch (RuntimeException e) {
            borrar(carpeta);
            throw new IOException("No se pudo descargar la actualización: " + e.getMessage(), e);
        }
    }

    private static void descomprimir(Path zip, Path destino) throws IOException {
        Path base = destino.toAbsolutePath().normalize();
        try (ZipInputStream z = new ZipInputStream(Files.newInputStream(zip))) {
            ZipEntry e;
            while ((e = z.getNextEntry()) != null) {
                Path p = base.resolve(e.getName().replace('\\', '/')).normalize();
                if (!p.startsWith(base)) throw new IOException("Paquete de actualización inválido.");
                if (e.isDirectory()) Files.createDirectories(p);
                else {
                    Files.createDirectories(p.getParent());
                    Files.copy(z, p, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
    }

    private static void borrar(Path carpeta) {
        if (!Files.exists(carpeta)) return;
        try (Stream<Path> s = Files.walk(carpeta)) {
            s.sorted(Comparator.reverseOrder()).forEach(p -> p.toFile().delete());
        } catch (IOException ignored) {
        }
    }

    /**
     * Comando para aplicar el parche. El .bat va por nombre y se ejecuta con la carpeta
     * del paquete como directorio: si el primer argumento de "cmd /c" empieza con
     * comillas, cmd quita la primera y la última del renglón y rompe rutas con espacios.
     * Lleva ".\" porque con NoDefaultCurrentDirectoryInExePath=1 (equipos endurecidos)
     * cmd no busca en la carpeta actual.
     */
    public static List<String> comandoActualizacion(Path instalacion) {
        return List.of("cmd", "/c", ".\\aplicar_actualizacion.bat", "--auto", instalacion.toString());
    }

    /** Lanza el actualizador (que cierra y reabre la app) y termina este proceso. */
    public static void lanzarYSalir(Path carpeta, Path instalacion) throws IOException {
        procesoActualizacion(carpeta, instalacion).start();
        System.exit(0);
    }

    /**
     * Proceso del actualizador con su salida en &lt;carpeta&gt;\lanzador.log: la app termina
     * enseguida y nadie leería las tuberías; además queda rastro si la actualización
     * nunca llega a correr (p. ej. UAC cancelado).
     */
    static ProcessBuilder procesoActualizacion(Path carpeta, Path instalacion) {
        return new ProcessBuilder(comandoActualizacion(instalacion))
                .directory(carpeta.toFile())
                .redirectErrorStream(true)
                .redirectOutput(carpeta.resolve("lanzador.log").toFile());
    }

    /** true si hay productos en el carrito de Ventas (actualizar los dejaría en el aire). */
    public static boolean hayVentaEnCurso() {
        return new ConexionBD().ventaEnCurso();
    }
}
