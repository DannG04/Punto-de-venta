/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author mayra
 */
public class Hora extends Thread {
    boolean running;
    String fecha;
    
    public Hora(){
        running = true;
        fecha = obtenerFecha();
    }
    
    public void run(){
        while(running){
            try{
                sleep(50);
            } catch(InterruptedException e){
                System.out.println("Error al dormir el hilo");
            }
            Interfaz.labelHora.setText(obtenerHora() + ", " + fecha);
        }
    }
    
    public final String obtenerFecha(){
        java.time.LocalDateTime ahora = java.time.LocalDateTime.now();
        // Define el formato deseado. Locale.setDefault(new Locale("es", "ES")) es importante para el idioma.
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("EEEE, dd 'de' MMMM 'del' yyyy", new java.util.Locale("es", "ES"));;
        return ahora.format(formatter);
    }
    
    public String obtenerHora(){
        java.time.LocalDateTime ahora = java.time.LocalDateTime.now();
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("HH:mm");
        return ahora.format(formatter);
    }
}
