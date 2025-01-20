
import java.time.LocalDate;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.poi.ss.usermodel.*;
import java.sql.*;

public class BalGeneral {
    private int indiceFilas=0;
    private double sumas=0;
    private Sheet hoja;
    private ConexionBD conect = new ConexionBD();

    public BalGeneral(Sheet sheet, int x){
        hoja=sheet;
        indiceFilas=x;
    }

    public void llenarBalance(){
        LocalDate fecha = LocalDate.now();
        ResultSet result;
        try {
            //result = conect.query("SELECT total_caja('01/01/"+fecha.getYear()+"','"+fecha.toString()+"');");
            //insertarCampoActivo("Caja",result.getString(1));
            insertarCampoActivo("Caja"," ");
            //sumas = sumas+result.getDouble(1);
            //result = conect.query("SELECT total_inventario();");
            //insertarCampoActivo("Inventario",result.getString(1));
            insertarCampoActivo("Inventario"," ");
            //sumas = sumas+result.getDouble(1);
            //result = conect.query("SELECT total_cliente_balance();");
            //
            insertarCampoActivo("Clientes"," ");
            //sumas = sumas+result.getDouble(1);
            insertarCampoActivo("Total de Activo Circulante",Double.toString(sumas));
        } catch (Exception e) {
            Logger.getLogger(Excel.class.getName()).log(Level.SEVERE, null, e);
            System.out.println("El error viene de acá");
        }
       
        insertarCampoActivo("No circulante", "");
        insertarCampoActivo("Muebles y equipo"," ");
        insertarCampoActivo("Total de Activos"," ");
        insertarCampoPasivo("Circulante" ," ", 5);
        insertarCampoPasivo("Obligaciones Laborales"," ",6);
        insertarCampoPasivo("No circulante", " ", 7);
        insertarCampoPasivo("Obligaciones a largo plazo"," ",8);
        insertarCampoPasivo("Total Pasivos"," ",9);
        insertarCampoPasivo("Total de patrimonio", " ",10);
        insertarCampoPasivo("Capital"," ",11);
        insertarCampoPasivo("Total de Pasivo más capital","",12);
    }

    private void insertarCampoActivo(String cuenta, String dato){
        Row fila = hoja.createRow(indiceFilas);
        Cell c = fila.createCell(0);
        fila.createCell(1);
        Cell d = fila.createCell(2);
        c.setCellValue(cuenta);
        d.setCellValue(dato);
        indiceFilas++;
    }
    
    private void insertarCampoPasivo(String cuenta,String dato, int fila){
        Row f = hoja.getRow(fila);
        Cell c = f.createCell(3);
        f.createCell(4);
        Cell d = f.createCell(5);
        c.setCellValue(cuenta);
        d.setCellValue(dato);
    }
}
