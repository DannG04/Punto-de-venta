package puntoventa.ui;

import static pruebas.Prueba.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicReference;
import puntoventa.util.Actualizador.Release;
import puntoventa.util.VersionApp;

public class ActualizacionesPrueba {
    public static void main(String[] a) throws Exception {
        AtomicReference<Release> avisado = new AtomicReference<>();
        ActualizacionesP p = new ActualizacionesP(avisado::set);

        // sin consulta (sin internet)
        p.mostrar(null);
        check(p.lblEstado.getText().equals("No se pudo consultar. Revisa la conexión a internet."), "sin red: " + p.lblEstado.getText());
        check(!p.btnActualizar.isEnabled(), "sin red: boton deshabilitado");
        check(p.lblInstalada.getText().equals("Versión instalada: " + VersionApp.actual()), "version instalada: " + p.lblInstalada.getText());

        // misma versión
        p.mostrar(new Release(VersionApp.actual(), "nada", LocalDate.of(2026, 11, 1), "u", "s"));
        check(p.lblEstado.getText().equals("Tienes la versión más reciente"), "al dia: " + p.lblEstado.getText());
        check(!p.btnActualizar.isEnabled(), "al dia: boton deshabilitado");

        // versión nueva
        p.mostrar(new Release("9.0", "• Nuevo corte de caja", LocalDate.of(2026, 11, 25), "u", "s"));
        check(p.lblDisponible.getText().equals("Versión disponible: 9.0 (publicada 25/11)"), "disponible: " + p.lblDisponible.getText());
        check(p.txtNotas.getText().contains("Nuevo corte de caja"), "notas");
        check(p.btnActualizar.isEnabled(), "nueva: boton habilitado");

        // resultado de la última actualización (archivo que deja aplicar_actualizacion.bat --auto)
        Path dir = Files.createTempDirectory("pvact");
        check(Interfaz.mensajeUltimaActualizacion(dir) == null, "sin archivo -> null");
        Files.writeString(dir.resolve("ultima_actualizacion.txt"), "OK 1.23 \r\n");
        check("Actualizado a la versión 1.23".equals(Interfaz.mensajeUltimaActualizacion(dir)), "OK -> mensaje");
        check(!Files.exists(dir.resolve("ultima_actualizacion.txt")), "OK -> archivo borrado");
        Files.writeString(dir.resolve("ultima_actualizacion.txt"), "ERROR migracion V1.30\r\n");
        check(("La actualización no se completó; la versión anterior sigue funcionando. Detalle en actualizacion.log")
                .equals(Interfaz.mensajeUltimaActualizacion(dir)), "ERROR -> mensaje");
        check(!Files.exists(dir.resolve("ultima_actualizacion.txt")), "ERROR -> archivo borrado");

        terminar("ActualizacionesPrueba");
    }
}
