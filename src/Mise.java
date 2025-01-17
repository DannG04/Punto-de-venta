/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

import javax.swing.table.DefaultTableModel;
/**
 *
 * @author mayra
 */
public class Mise {
    public static void limpiarTabla(DefaultTableModel modelo){
        int a=modelo.getRowCount();    
        while(a!=0){ 
            if(a!=0)
                modelo.removeRow(0);                      
            a=modelo.getRowCount();
        }
    }
    
    public static void JOption(String mensaje, String titulo, int tipo){
        javax.swing.JOptionPane optionPane = new javax.swing.JOptionPane(mensaje, tipo);
        javax.swing.JDialog dialog = optionPane.createDialog(titulo);
        dialog.setAlwaysOnTop(true);
        dialog.setVisible(true);
        dialog.toFront();
        dialog.requestFocus();
    }
    
    public static int JOptionYesNo(String mensaje, String titulo){
        String[] opciones = {"Sí", "No"};
        javax.swing.JOptionPane optionPane = new javax.swing.JOptionPane(mensaje, javax.swing.JOptionPane.QUESTION_MESSAGE, javax.swing.JOptionPane.YES_NO_OPTION, null, opciones);
        javax.swing.JDialog dialog = optionPane.createDialog(titulo);
        dialog.setAlwaysOnTop(true);
        dialog.setVisible(true);
        dialog.toFront();
        dialog.requestFocus();
        
        Object seleccion = optionPane.getValue();
        if (seleccion == null) {
            return javax.swing.JOptionPane.CLOSED_OPTION;
        }
        // Convertir la selección a un índice
        return seleccion.equals("Sí") ? javax.swing.JOptionPane.YES_OPTION : javax.swing.JOptionPane.NO_OPTION;
    }
    
    public static String JOptionInput(String mensaje, String titulo) {
        javax.swing.JOptionPane optionPane = new javax.swing.JOptionPane(mensaje, javax.swing.JOptionPane.QUESTION_MESSAGE, javax.swing.JOptionPane.DEFAULT_OPTION);
        optionPane.setWantsInput(true); // Habilitar la entrada de texto
        javax.swing.JDialog dialog = optionPane.createDialog(titulo);
        dialog.setAlwaysOnTop(true);
        dialog.setVisible(true);
        dialog.toFront();
        dialog.requestFocus();

        Object input = optionPane.getInputValue();
        if (input == javax.swing.JOptionPane.UNINITIALIZED_VALUE) {
            return ""; // Devuelve una cadena vacía si el usuario cancela la entrada
        }

        return input.toString();
    }
    
}