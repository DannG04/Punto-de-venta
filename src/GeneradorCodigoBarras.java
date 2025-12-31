import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import javax.imageio.ImageIO;

/**
 * Clase para generar códigos de barras usando ZXing
 * Soporta múltiples formatos: EAN-13, CODE_128, QR_CODE
 */
public class GeneradorCodigoBarras {
    
    /**
     * Genera un código de producto automático basado en timestamp
     * Formato: PV + timestamp de 10 dígitos
     * Ejemplo: PV1735567890
     */
    public static String generarCodigoAutomatico() {
        long timestamp = System.currentTimeMillis() / 1000; // segundos
        return "PV" + timestamp;
    }
    
    /**
     * Genera un código EAN-13 válido
     * @param codigo Código base de 12 dígitos
     * @return Código EAN-13 con dígito verificador
     */
    public static String generarEAN13(String codigo) {
        // Si el código no tiene 12 dígitos, rellenamos con ceros a la izquierda
        if (codigo.length() < 12) {
            codigo = String.format("%12s", codigo).replace(' ', '0');
        } else if (codigo.length() > 12) {
            codigo = codigo.substring(0, 12);
        }
        
        // Calcular dígito verificador
        int suma = 0;
        for (int i = 0; i < 12; i++) {
            int digito = Character.getNumericValue(codigo.charAt(i));
            if (i % 2 == 0) {
                suma += digito;
            } else {
                suma += digito * 3;
            }
        }
        int digitoVerificador = (10 - (suma % 10)) % 10;
        
        return codigo + digitoVerificador;
    }
    
    /**
     * Genera imagen de código de barras
     * @param codigo El código a convertir en barras
     * @param formato BarcodeFormat (EAN_13, CODE_128, etc.)
     * @param ancho Ancho en píxeles
     * @param alto Alto en píxeles
     * @return BufferedImage con el código de barras
     */
    public static BufferedImage generarImagenCodigoBarras(String codigo, BarcodeFormat formato, int ancho, int alto) {
        try {
            BitMatrix bitMatrix = new MultiFormatWriter().encode(codigo, formato, ancho, alto);
            return MatrixToImageWriter.toBufferedImage(bitMatrix);
        } catch (WriterException e) {
            System.out.println("Error al generar código de barras: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Guarda el código de barras como imagen PNG
     * @param codigo El código a convertir
     * @param rutaArchivo Ruta donde guardar la imagen
     * @param formato BarcodeFormat
     * @param ancho Ancho en píxeles
     * @param alto Alto en píxeles
     */
    public static void guardarCodigoBarrasArchivo(String codigo, String rutaArchivo, BarcodeFormat formato, int ancho, int alto) {
        try {
            Path path = FileSystems.getDefault().getPath(rutaArchivo);
            BitMatrix bitMatrix = new MultiFormatWriter().encode(codigo, formato, ancho, alto);
            MatrixToImageWriter.writeToPath(bitMatrix, "PNG", path);
        } catch (WriterException | IOException e) {
            System.out.println("Error al guardar código de barras: " + e.getMessage());
        }
    }
    
    /**
     * Valida si un código es numérico (para EAN-13)
     * @param codigo Código a validar
     * @return true si es válido
     */
    public static boolean esCodigoNumerico(String codigo) {
        return codigo != null && codigo.matches("\\d+");
    }
    
    /**
     * Genera código CODE_128 (acepta letras y números)
     * @param codigo Código alfanumérico
     * @param ancho Ancho en píxeles
     * @param alto Alto en píxeles
     * @return BufferedImage con el código
     */
    public static BufferedImage generarCODE128(String codigo, int ancho, int alto) {
        return generarImagenCodigoBarras(codigo, BarcodeFormat.CODE_128, ancho, alto);
    }
    
    /**
     * Genera código QR (puede contener más información)
     * @param datos Datos a codificar
     * @param tamanio Tamaño en píxeles (cuadrado)
     * @return BufferedImage con el código QR
     */
    public static BufferedImage generarQR(String datos, int tamanio) {
        return generarImagenCodigoBarras(datos, BarcodeFormat.QR_CODE, tamanio, tamanio);
    }
}
