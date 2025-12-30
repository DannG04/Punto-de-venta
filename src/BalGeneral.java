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

    //BalGeneral bg = new BalGeneral(sheet,6);
    //bg.llenarBalance(datosEstilo,50000); El número es la cantidad final del estado de resultados

    public void llenarBalance(CellStyle datosEstilos,double capital){ //Recibe el estilo de las celdas y la capital
        LocalDate fecha = LocalDate.now();
        ResultSet result;
        double cant[] = new double[3];
        double ap=0;
        try {       
            result = conect.query("SELECT * from balance_general('01/01/"+fecha.getYear()+"','"+fecha.toString()+"');");
            while(result.next()){
                cant[0]= result.getDouble(1);
                cant[1]= result.getDouble(2);
                cant[2]= result.getDouble(3);
            }
            insertarCampoActivo("Caja",String.valueOf(cant[0]),datosEstilos);
            insertarCampoActivo("Inventario",String.valueOf(cant[1]),datosEstilos);
            insertarCampoActivo("clientes", String.valueOf(cant[2]),datosEstilos);
            sumas = cant[0]+cant[1]+cant[2];
            insertarCampoActivo("Total de Activo",String.valueOf(sumas),datosEstilos);
            sumas =0;
            result = conect.query("SELECT anticipo_clientes();");
            while(result.next()){
                ap= result.getDouble(1);
            }
            insertarCampoPasivo("Anticipo de clientes",String.valueOf(ap) ,6,datosEstilos);
            sumas = sumas+ap;
            insertarCampoPasivo("Total Pasivos",String.valueOf(sumas),7,datosEstilos);
            insertarCampoPasivo("Capital",String.valueOf(capital),8,datosEstilos);
            sumas = sumas+capital;
            insertarCampoPasivo("Total de Pasivo más capital",String.valueOf(sumas),9,datosEstilos);
        } catch (Exception e) {
            Logger.getLogger(Excel.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    //Recibe el nombre de la cuenta de activos, la cantidad correspondiente a la cuenta y el estilo que se le pondrá a las celdas
    private void insertarCampoActivo(String cuenta, String dato, CellStyle estilo){
        Row fila = hoja.createRow(indiceFilas);
        Cell c = fila.createCell(0);
        c.setCellStyle(estilo);
        c.setCellValue(cuenta);
        Cell x = fila.createCell(1);
        x.setCellStyle(estilo);
        Cell d = fila.createCell(2);
        d.setCellStyle(estilo);
        d.setCellValue(dato);
        indiceFilas++;
    }
    
    //Recibe el nombre de la cuenta de pasivos, la cantidad correspondiente a la cuenta y el estilo que se le pondrá a las celdas
    private void insertarCampoPasivo(String cuenta,String dato, int fila, CellStyle estilo){
        Row f = hoja.getRow(fila);
        Cell c = f.createCell(3);
        c.setCellStyle(estilo);
        c.setCellValue(cuenta);
        Cell x = f.createCell(4);
        x.setCellStyle(estilo);
        Cell d = f.createCell(5);
        d.setCellStyle(estilo);
        d.setCellValue(dato);
    }
}
