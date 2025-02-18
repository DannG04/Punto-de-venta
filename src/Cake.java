/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author 76905
 */
public class Cake {
    public static boolean letrasMinus(char letra){
        return (letra>='a' && letra<='z');
    }
    
    public static boolean letrasMayus(char letra){
        return (letra>='A' && letra<='Z');
    }
    
    public static boolean numeros(char letra){
        return (letra>='0' && letra<='9');
    }
    
    public static boolean tamaño(String palabra, int límite){
        return palabra.length() == límite;
    }
    
    public static boolean espacios(String palabra, char letra){
        int i = palabra.length(); 
        return (palabra.charAt(i-1) == ' ') && (letra == ' ');
    }
    
    public static boolean inicioEspacios(char letra){
        return letra == ' ';
    }
    
    public static boolean inicioPunto(char letra){
        return letra == '.';
    }
    
    public static boolean punto(String palabra, char letra){
        int i = palabra.length(); 
        return (palabra.charAt(i-1) == '.') && (letra == '.');
    }
    
    public static boolean guionShort(char letra) {
        return letra == '-';
    }
    
    public static boolean hayPuntos(String palabra){
        int cont = 0;
        int i = palabra.length(); 
        for(int j=0; j<i; j++){
            if(palabra.charAt(j) == '.')
                cont++;
        }
        return cont>0;
    }
    
}
