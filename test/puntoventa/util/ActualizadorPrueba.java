package puntoventa.util;

import static pruebas.Prueba.*;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.List;
import java.util.zip.*;
import puntoventa.util.Actualizador.Release;

public class ActualizadorPrueba {

    static String leer(String ruta) throws IOException { return Files.readString(Path.of(ruta)); }

    static Path tmp(String contenido) throws IOException {
        Path p = Files.createTempFile("pv", ".txt");
        Files.writeString(p, contenido);
        return p;
    }

    public static void main(String[] a) throws Exception {
        // --- parseo ---
        Release r = Actualizador.parsear(leer("test/recursos/release_ok.json"));
        check(r != null, "parsea release_ok");
        check(r.version().equals("1.23"), "version normalizada");
        check(r.notas().contains("Corrige") && r.notas().contains("\n"), "notas con salto de linea");
        check(r.notas().contains("\"Cotizaciones\"") && r.notas().contains("C:\\Ventas") && r.notas().startsWith("\u2022"),
                "escapes decodificados: " + r.notas());
        check(r.publicada().equals(LocalDate.of(2026, 11, 25)), "fecha publicada");
        check(r.urlZip().endsWith("PuntoVenta-Update-1.23.zip"), "url zip");
        check(r.urlSha().endsWith("PuntoVenta-Update-1.23.zip.sha256"), "url sha");
        check(Actualizador.parsear(leer("test/recursos/release_sin_sha.json")) == null, "sin sha -> null");
        check(Actualizador.parsear("{\"message\":\"API rate limit exceeded\"}") == null, "rate limit -> null");
        check(Actualizador.parsear("no es json") == null, "basura -> null");
        check(Actualizador.parsear(null) == null, "null -> null");

        // --- sha256 ---
        check(Actualizador.sha256(tmp("abc")).equals("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad"), "sha256 abc");

        // --- comando de lanzamiento (ruta con espacios y paréntesis) ---
        // El .bat va por nombre (se ejecuta con directorio = carpeta del paquete): si el primer
        // argumento de "cmd /c" empieza con comillas, cmd quita la primera y la última del
        // renglón y rompe rutas con espacios (p. ej. C:\Users\Juan Perez\AppData\Local\Temp).
        List<String> cmd = Actualizador.comandoActualizacion(Path.of("C:\\Program Files (x86)\\PuntoVenta"));
        check(cmd.equals(List.of("cmd", "/c", ".\\aplicar_actualizacion.bat",
                "--auto", "C:\\Program Files (x86)\\PuntoVenta")), "comando: " + cmd);

        // --- servidor local que imita a GitHub ---
        byte[] zip = zipCon("aplicar_actualizacion.bat", "@echo off\r\necho hola\r\n");
        Path zipTmp = Files.createTempFile("pv", ".zip");
        Files.write(zipTmp, zip);
        String hash = Actualizador.sha256(zipTmp);
        HttpServer srv = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        String base = "http://127.0.0.1:" + srv.getAddress().getPort();
        // los assets de GitHub responden con redirección a otro host
        srv.createContext("/zip", ex -> { ex.getResponseHeaders().add("Location", base + "/real.zip"); ex.sendResponseHeaders(302, -1); ex.close(); });
        srv.createContext("/real.zip", ex -> { ex.sendResponseHeaders(200, zip.length); ex.getResponseBody().write(zip); ex.close(); });
        srv.createContext("/ok.sha256", ex -> responder(ex, 200, hash + "  PuntoVenta-Update-9.9.zip\n"));
        srv.createContext("/malo.sha256", ex -> responder(ex, 200, "0".repeat(64)));
        // mismas URLs que el release real, pero apuntando al servidor local
        String json = leer("test/recursos/release_ok.json")
                .replace("https://github.com/DannG04/Punto-de-venta/releases/download", base + "/download");
        srv.createContext("/latest", ex -> responder(ex, 200, json));
        srv.createContext("/limite", ex -> responder(ex, 403, "{\"message\":\"API rate limit exceeded\"}"));
        srv.start();
        try {
            System.setProperty("pdv.releases.url", base + "/latest");
            Release rl = Actualizador.buscar();
            check(rl != null && rl.version().equals("1.23") && rl.urlZip().equals(base + "/download/v1.23/PuntoVenta-Update-1.23.zip"), "buscar() local: " + rl);
            System.setProperty("pdv.releases.url", base + "/limite");
            check(Actualizador.buscar() == null, "HTTP 403 -> null");
            System.setProperty("pdv.releases.url", "http://127.0.0.1:1/nada");
            long t0 = System.currentTimeMillis();
            check(Actualizador.buscar() == null, "sin servidor -> null");
            check(System.currentTimeMillis() - t0 < 12000, "sin servidor no tarda mas del timeout");

            // descarga correcta (con redirección)
            Release ok = new Release("9.9", "", LocalDate.now(), base + "/zip", base + "/ok.sha256");
            double[] ultimo = {0};
            Path carpeta = Actualizador.descargar(ok, p -> ultimo[0] = p);
            check(Files.exists(carpeta.resolve("aplicar_actualizacion.bat")), "zip descomprimido en " + carpeta);
            check(Files.exists(carpeta.resolve("lib").resolve("x.jar")), "subcarpetas descomprimidas");
            check(carpeta.getFileName().toString().equals("PuntoVenta-Update-9.9"), "nombre de carpeta");
            check(ultimo[0] >= 0.99, "progreso llega a 1: " + ultimo[0]);

            // hash incorrecto
            Release malo = new Release("9.8", "", LocalDate.now(), base + "/zip", base + "/malo.sha256");
            boolean lanzo = false;
            try {
                Actualizador.descargar(malo, p -> {});
            } catch (IOException e) {
                lanzo = e.getMessage() != null && !e.getMessage().isBlank();
            }
            check(lanzo, "hash incorrecto lanza IOException con mensaje");
            check(!Files.exists(Path.of(System.getProperty("java.io.tmpdir"), "PuntoVenta-Update-9.8")),
                    "carpeta borrada tras hash incorrecto");
        } finally {
            srv.stop(0);
            System.clearProperty("pdv.releases.url");
        }
        terminar("ActualizadorPrueba");
    }

    static void responder(HttpExchange ex, int code, String body) throws IOException {
        byte[] b = body.getBytes(StandardCharsets.UTF_8);
        ex.sendResponseHeaders(code, b.length);
        ex.getResponseBody().write(b);
        ex.close();
    }

    static byte[] zipCon(String nombre, String contenido) throws IOException {
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        try (ZipOutputStream z = new ZipOutputStream(bo)) {
            z.putNextEntry(new ZipEntry(nombre));
            z.write(contenido.getBytes(StandardCharsets.UTF_8));
            z.closeEntry();
            z.putNextEntry(new ZipEntry("lib/x.jar"));
            z.write(new byte[]{1, 2, 3});
            z.closeEntry();
        }
        return bo.toByteArray();
    }
}
