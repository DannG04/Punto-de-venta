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
        return (letra>='a' && letra<='z'); //Verifica si la letra es minúscula
    }
    
    public static boolean letrasMayus(char letra){
        return (letra>='A' && letra<='Z');//Verifica si la letra es mayúscula
    }
    
    public static boolean numeros(char letra){
        return (letra>='0' && letra<='9');//Verifica si la letra es un número
    }
    
    public static boolean tamaño(String palabra, int límite){
        return palabra.length() == límite;//Verifica si la palabra tiene el tamaño correcto
    }
    
    public static boolean espacios(String palabra, char letra){//Verifica si hay espacios
        int i = palabra.length();
        return (palabra.charAt(i-1) == ' ') && (letra == ' ');
    }
    
    public static boolean inicioEspacios(char letra){//Verifica si hay espacios al inicio
        return letra == ' ';
    }
    
    public static boolean inicioPunto(char letra){//Verifica si hay un punto al inicio
        return letra == '.';
    }
    
    public static boolean punto(String palabra, char letra){//Verifica si hay un punto al final
        int i = palabra.length(); 
        return (palabra.charAt(i-1) == '.') && (letra == '.');
    }
    
    public static boolean guionShort(char letra) {//Verifica si hay un guión
        return letra == '-';
    }
    
    public static boolean hayPuntos(String palabra){//Verifica si hay puntos
        int cont = 0;
        int i = palabra.length(); 
        for(int j=0; j<i; j++){
            if(palabra.charAt(j) == '.')
                cont++;
        }
        return cont>0;
    }
    
}
