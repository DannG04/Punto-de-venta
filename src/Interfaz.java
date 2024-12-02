
import com.jtattoo.plaf.mcwin.McWinLookAndFeel;
import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.Toolkit;
import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;



/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */

/**
 *
 * @author rodth
 */
public class Interfaz extends javax.swing.JFrame {

    static String us = "";
    
    static Boolean admActivo = false;
    static Boolean userActivo = false;
    
    java.awt.Color pred = new java.awt.Color(78,150,150);
    
    //Paneles
    InventarioP invPanel = new InventarioP();
    VentasP ventPanel = new VentasP();
    AdministracionP docsPanel = new AdministracionP();
    ApartadosP apPanel = new ApartadosP();
    ClientesP cliPanel = new ClientesP();
    
    static String idVendedor = "DANIEL123";
    
    boolean iniDia = false;
    Dimension tamanio = Toolkit.getDefaultToolkit().getScreenSize();

    
    
    /**
     * Creates new form Interfaz
     */
    public Interfaz() {
        initComponents();
        
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        botonGrup = new javax.swing.ButtonGroup();
        popupMenu = new javax.swing.JPopupMenu();
        opcion1 = new javax.swing.JMenuItem();
        opcion2 = new javax.swing.JMenuItem();
        panelPrin = new javax.swing.JPanel();
        perfil = new javax.swing.JButton();
        closeSesion = new javax.swing.JButton();
        initerDia = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        botones = new javax.swing.JPanel();
        boton0 = new javax.swing.JToggleButton();
        boton1 = new javax.swing.JToggleButton();
        boton2 = new javax.swing.JToggleButton();
        boton3 = new javax.swing.JToggleButton();
        boton4 = new javax.swing.JToggleButton();
        boton5 = new javax.swing.JToggleButton();
        iniSession = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        etiquetaN1 = new javax.swing.JLabel();
        etiquetaC1 = new javax.swing.JLabel();
        contraF1 = new javax.swing.JPasswordField();
        eyeB1 = new javax.swing.JToggleButton();
        contInc = new javax.swing.JLabel();
        veriB1 = new javax.swing.JButton();
        usuarioBox = new javax.swing.JComboBox<>();

        popupMenu.setToolTipText("");

        opcion1.setText("Nombre: ");
        popupMenu.add(opcion1);

        opcion2.setText("Puesto:");
        opcion2.setToolTipText("");
        popupMenu.add(opcion2);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Punto de Ventas");
        setMinimumSize(new java.awt.Dimension(430, 400));
        setSize(new java.awt.Dimension(430, 400));

        panelPrin.setBackground(new java.awt.Color(0, 102, 102));
        panelPrin.setLayout(new java.awt.GridBagLayout());

        perfil.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/userCi.png"))); // NOI18N
        perfil.setMaximumSize(new java.awt.Dimension(90, 90));
        perfil.setMinimumSize(new java.awt.Dimension(90, 90));
        perfil.setPreferredSize(new java.awt.Dimension(90, 90));
        perfil.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                perfilMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                perfilMouseReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 12, 12);
        panelPrin.add(perfil, gridBagConstraints);

        closeSesion.setFont(new java.awt.Font("Noto Serif", 1, 12)); // NOI18N
        closeSesion.setForeground(new java.awt.Color(78, 150, 150));
        closeSesion.setText("Cerrar sesión");
        closeSesion.setMaximumSize(new java.awt.Dimension(115, 25));
        closeSesion.setMinimumSize(new java.awt.Dimension(115, 25));
        closeSesion.setPreferredSize(new java.awt.Dimension(115, 25));
        closeSesion.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeSesionActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 0);
        panelPrin.add(closeSesion, gridBagConstraints);

        initerDia.setFont(new java.awt.Font("Noto Serif", 1, 12)); // NOI18N
        initerDia.setForeground(new java.awt.Color(78, 150, 150));
        initerDia.setText("Iniciar día");
        initerDia.setMaximumSize(new java.awt.Dimension(115, 25));
        initerDia.setMinimumSize(new java.awt.Dimension(115, 25));
        initerDia.setPreferredSize(new java.awt.Dimension(115, 25));
        initerDia.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                initerDiaActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 0);
        panelPrin.add(initerDia, gridBagConstraints);

        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/astetik.gif"))); // NOI18N
        jLabel3.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jLabel3.setPreferredSize(new java.awt.Dimension(1000, 600));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 4;
        panelPrin.add(jLabel3, gridBagConstraints);

        getContentPane().add(panelPrin, java.awt.BorderLayout.CENTER);
        panelPrin.setVisible(false);

        botones.setBackground(new java.awt.Color(0, 102, 102));
        botones.setLayout(new javax.swing.BoxLayout(botones, javax.swing.BoxLayout.Y_AXIS));

        botonGrup.add(boton0);
        boton0.setFont(new java.awt.Font("Noto Serif", 1, 16)); // NOI18N
        boton0.setForeground(new java.awt.Color(78, 150, 150));
        boton0.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/jom.png"))); // NOI18N
        boton0.setText("Principal");
        boton0.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        boton0.setMaximumSize(new java.awt.Dimension(300, 400));
        boton0.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                boton0ActionPerformed(evt);
            }
        });
        botones.add(boton0);
        boton0.setVisible(false);

        botonGrup.add(boton1);
        boton1.setFont(new java.awt.Font("Noto Serif", 1, 16)); // NOI18N
        boton1.setForeground(new java.awt.Color(78, 150, 150));
        boton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/inv.png"))); // NOI18N
        boton1.setText("Inventario");
        boton1.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        boton1.setMaximumSize(new java.awt.Dimension(300, 400));
        boton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                boton1ActionPerformed(evt);
            }
        });
        botones.add(boton1);
        boton1.setVisible(false);

        botonGrup.add(boton2);
        boton2.setFont(new java.awt.Font("Noto Serif", 1, 16)); // NOI18N
        boton2.setForeground(new java.awt.Color(78, 150, 150));
        boton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/ventA.png"))); // NOI18N
        boton2.setText("Ventas");
        boton2.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        boton2.setMaximumSize(new java.awt.Dimension(300, 400));
        boton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                boton2ActionPerformed(evt);
            }
        });
        botones.add(boton2);
        boton2.setVisible(false);

        botonGrup.add(boton3);
        boton3.setFont(new java.awt.Font("Noto Serif", 1, 16)); // NOI18N
        boton3.setForeground(new java.awt.Color(78, 150, 150));
        boton3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/carp.png"))); // NOI18N
        boton3.setText("Administración");
        boton3.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        boton3.setMaximumSize(new java.awt.Dimension(300, 400));
        boton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                boton3ActionPerformed(evt);
            }
        });
        botones.add(boton3);
        boton3.setVisible(false);

        botonGrup.add(boton4);
        boton4.setFont(new java.awt.Font("Noto Serif", 1, 16)); // NOI18N
        boton4.setForeground(new java.awt.Color(78, 150, 150));
        boton4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/doc.png"))); // NOI18N
        boton4.setText("Sistema de Apartados");
        boton4.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        boton4.setMaximumSize(new java.awt.Dimension(300, 400));
        boton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                boton4ActionPerformed(evt);
            }
        });
        botones.add(boton4);
        boton4.setVisible(false);

        botonGrup.add(boton5);
        boton5.setFont(new java.awt.Font("Noto Serif", 1, 16)); // NOI18N
        boton5.setForeground(new java.awt.Color(78, 150, 150));
        boton5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/usuario.png"))); // NOI18N
        boton5.setText("Registro de Clientes");
        boton5.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        boton5.setMaximumSize(new java.awt.Dimension(300, 400));
        boton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                boton5ActionPerformed(evt);
            }
        });
        botones.add(boton5);
        boton5.setVisible(false);

        getContentPane().add(botones, java.awt.BorderLayout.WEST);
        botones.setVisible(false);

        iniSession.setMaximumSize(new java.awt.Dimension(430, 400));
        iniSession.setMinimumSize(new java.awt.Dimension(430, 400));
        iniSession.setPreferredSize(new java.awt.Dimension(430, 400));
        iniSession.setLayout(new java.awt.GridBagLayout());

        jLabel2.setFont(new java.awt.Font("Berlin Sans FB", 0, 36)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(102, 102, 102));
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/usuariu1.png"))); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 25, 10);
        iniSession.add(jLabel2, gridBagConstraints);

        etiquetaN1.setFont(new java.awt.Font("Noto Serif", 1, 12)); // NOI18N
        etiquetaN1.setForeground(new java.awt.Color(78, 150, 150));
        etiquetaN1.setText("Nombre de Usuario:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        iniSession.add(etiquetaN1, gridBagConstraints);

        etiquetaC1.setFont(new java.awt.Font("Noto Serif", 1, 12)); // NOI18N
        etiquetaC1.setForeground(new java.awt.Color(78, 150, 150));
        etiquetaC1.setText("Contraseña:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        iniSession.add(etiquetaC1, gridBagConstraints);

        contraF1.setFont(new java.awt.Font("Noto Serif", 0, 12)); // NOI18N
        contraF1.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        contraF1.setPreferredSize(new java.awt.Dimension(180, 25));
        contraF1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                contraF1KeyTyped(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 0);
        iniSession.add(contraF1, gridBagConstraints);

        eyeB1.setBackground(new java.awt.Color(255, 255, 254));
        eyeB1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/eye2.png"))); // NOI18N
        eyeB1.setBorderPainted(false);
        eyeB1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                eyeB1ActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        iniSession.add(eyeB1, gridBagConstraints);

        contInc.setFont(new java.awt.Font("Noto Serif", 0, 10)); // NOI18N
        contInc.setForeground(new java.awt.Color(204, 0, 51));
        contInc.setText(" ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 3;
        iniSession.add(contInc, gridBagConstraints);

        veriB1.setFont(new java.awt.Font("Noto Serif", 1, 12)); // NOI18N
        veriB1.setForeground(new java.awt.Color(78, 150, 150));
        veriB1.setText("Iniciar Sesión");
        veriB1.setPreferredSize(new java.awt.Dimension(150, 30));
        veriB1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                veriB1ActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.insets = new java.awt.Insets(15, 10, 10, 10);
        iniSession.add(veriB1, gridBagConstraints);

        usuarioBox.setFont(new java.awt.Font("Noto Serif", 0, 12)); // NOI18N
        usuarioBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Daniel", "Gaelin", "Elisa" }));
        usuarioBox.setPreferredSize(new java.awt.Dimension(180, 25));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        iniSession.add(usuarioBox, gridBagConstraints);

        getContentPane().add(iniSession, java.awt.BorderLayout.PAGE_START);

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void contraF1KeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_contraF1KeyTyped
        char letra = evt.getKeyChar();
        
        if(evt.getKeyChar()== KeyEvent.VK_ENTER){
            iniciarSesion();
        }
        
        if(!Cake.numeros(letra)){
            evt.consume();
        }

        if(Cake.tamaño(contraF1.getText(), 10)){
            evt.consume();
        }
    }//GEN-LAST:event_contraF1KeyTyped

    private void eyeB1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_eyeB1ActionPerformed
        if(eyeB1.isSelected()){
            contraF1.setEchoChar((char)0);
            eyeB1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/eye1.png")));
        }
        else{
            contraF1.setEchoChar('*');
            eyeB1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/eye2.png")));
        }
    }//GEN-LAST:event_eyeB1ActionPerformed

    private void veriB1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_veriB1ActionPerformed
        iniciarSesion();
    }//GEN-LAST:event_veriB1ActionPerformed

    private void perfilMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_perfilMouseReleased
        if (evt.isPopupTrigger()) {
            popupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
        }
    }//GEN-LAST:event_perfilMouseReleased

    private void boton0ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_boton0ActionPerformed
        botonesFore(0);
        visibilidad(0);
    }//GEN-LAST:event_boton0ActionPerformed

    private void boton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_boton1ActionPerformed
        botonesFore(1);
        visibilidad(1);
    }//GEN-LAST:event_boton1ActionPerformed

    private void boton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_boton2ActionPerformed
        botonesFore(2);
        visibilidad(2);
    }//GEN-LAST:event_boton2ActionPerformed

    private void boton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_boton3ActionPerformed
        botonesFore(3);
        visibilidad(3);
    }//GEN-LAST:event_boton3ActionPerformed

    private void boton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_boton4ActionPerformed
        botonesFore(4);
        visibilidad(4);
    }//GEN-LAST:event_boton4ActionPerformed

    private void boton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_boton5ActionPerformed
        botonesFore(5);
        visibilidad(5);
    }//GEN-LAST:event_boton5ActionPerformed

    private void perfilMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_perfilMousePressed
        if (evt.isPopupTrigger()) {
            popupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
        }
    }//GEN-LAST:event_perfilMousePressed

    private void initerDiaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_initerDiaActionPerformed
        if(!iniDia){
            JOption("Dia iniciado","Dia info");
            initerDia.setText("Terminar día");
            iniDia = true;
            VentasP.diaV = 0;
            AdministracionP.contDei++;
            boton2.setVisible(true);
        }
        else{
            docsPanel.mostrarCierreCaja();
            docsPanel.VentasDia.setVisible(true);
            JOption("Día terminado. Se han guardado las ventas del día", "Mensaje");
            initerDia.setText("Ïniciar día");
            iniDia = false;
            AdministracionP.regTVentas();
            boton2.setVisible(false);
        }
    }//GEN-LAST:event_initerDiaActionPerformed

    private void closeSesionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeSesionActionPerformed
        if(!iniDia){
            JOption("Hasta luego", "Salir");
            userActivo = false;
            admActivo = false;
            contraF1.setText("");
            contInc.setText("");
            eyeB1.setSelected(false);
            eyeB1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/eye2.png")));
            iniSession.setVisible(true);
            botones.setVisible(false);
            panelPrin.setVisible(false);
            this.setSize(430, 400);
            this.setLocationRelativeTo(null);
        }
        else{
            javax.swing.JOptionPane.showMessageDialog(null, "Debe terminar el día para cerrar sesión");
        }
    }//GEN-LAST:event_closeSesionActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) throws UnsupportedLookAndFeelException {
        
        try {
            UIManager.setLookAndFeel(new McWinLookAndFeel());
        } catch (UnsupportedLookAndFeelException ex) {
            //Logger.getLogger(Interfaz.class.getName()).log(Level.SEVERE, null, ex);
        }

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Interfaz().setVisible(true);
            }
        });
    }
    
    public void iniciarSesion(){
        us = usuarioBox.getSelectedItem().toString();
        String c = contraF1.getText();
        if(!us.isEmpty() || !c.isEmpty()){
            if(AdministracionP.userC.containsKey(us)){
                if(!AdministracionP.userC.get(us).equals(c)){
                    contInc.setText("Contraseña incorrecta");
                }
                else{
                    JOption("Bienvenido " + us, "Inicio de sesión");
                    userActivo = true;
                    if(us.equals(AdministracionP.administrador)){
                        admActivo = true;
                    }
                    
                    opcion1.setText("Nombre: " + us);
                    if(admActivo){
                        opcion2.setText("Puesto: Administrador");
                    } else {
                        opcion2.setText("Puesto: Vendedor");
                    }
                    
                    boton0.setForeground(Color.BLACK);
                    boton0.setSelected(true);
                    iniSession.setVisible(false);
                    botonesVis();
                    botones.setVisible(true);
                    panelPrin.setVisible(true);
                    this.setSize(tamanio.width, tamanio.height-20);
                    this.setLocationRelativeTo(null);
                }
            }
            else{
                contInc.setText("Datos érroneos, intentelo de nuevo");
            }
        } else {
            contInc.setText("Debe llenar todos los campos");
        }
    }
    
    public void visibilidad(int a){
        panelPrin.setVisible(false);
        invPanel.setVisible(false);
        ventPanel.setVisible(false);
        docsPanel.setVisible(false);
        apPanel.setVisible(false);
        cliPanel.setVisible(false);
        switch(a){
            case 0://Panel Principal
                panelPrin.setVisible(true);
                break;
            case 1://Panel Inventario
                getContentPane().add(invPanel, java.awt.BorderLayout.CENTER);
                this.add(invPanel);
                invPanel.mostrarTabla();
                if(admActivo){
                    InventarioP.panelBotones.setVisible(true);
                }
                else{
                    InventarioP.panelBotones.setVisible(false);
                }
                invPanel.setVisible(true);
                break;
            case 2://Panel Registro de Ventas
                getContentPane().add(ventPanel, java.awt.BorderLayout.CENTER);
                this.add(ventPanel);
                ventPanel.setVisible(true);
                break;
            case 3://Panel Administración
                getContentPane().add(docsPanel, java.awt.BorderLayout.CENTER);
                this.add(docsPanel);
                docsPanel.setVisible(true);
                break;
            case 4://Panel Sistema de Apartados
                getContentPane().add(apPanel, java.awt.BorderLayout.CENTER);
                this.add(apPanel);
                apPanel.setVisible(true);
                break;
            case 5://Panel Registro de Clientes
                getContentPane().add(cliPanel, java.awt.BorderLayout.CENTER);
                this.add(cliPanel);
                cliPanel.setVisible(true);
                break;
        }
    }
    
    public void botonesFore(int x){
        boton0.setForeground(pred);
        boton1.setForeground(pred);
        boton2.setForeground(pred);
        boton3.setForeground(pred);
        boton4.setForeground(pred);
        boton5.setForeground(pred);
        switch(x){
            case 0:
                boton0.setForeground(Color.BLACK);
                break;
            case 1:
                boton1.setForeground(Color.BLACK);
                break;
            case 2:
                boton2.setForeground(Color.BLACK);
                break;
            case 3:
                boton3.setForeground(Color.BLACK);
                break;
            case 4:
                boton4.setForeground(Color.BLACK);
                break;
            case 5:
                boton5.setForeground(Color.BLACK);
                break;
        }
    }
    
    public void botonesVis(){
        if(userActivo){
            boton0.setVisible(true);
            boton1.setVisible(true);
            if(admActivo){
                boton3.setVisible(true);
            }
            boton4.setVisible(true);
            boton5.setVisible(true);
        }
    }
    
    public static void JOption(String mensaje, String titulo){
        javax.swing.JOptionPane optionPane = new javax.swing.JOptionPane(mensaje);
        javax.swing.JDialog dialog = optionPane.createDialog(titulo);
        dialog.setAlwaysOnTop(true);
        dialog.setVisible(true);
        dialog.toFront();
        dialog.requestFocus();
    }
    public static int JOptionYesNo(String mensaje, String titulo){
        javax.swing.JOptionPane optionPane = new javax.swing.JOptionPane(mensaje, javax.swing.JOptionPane.QUESTION_MESSAGE, javax.swing.JOptionPane.YES_NO_OPTION);
        javax.swing.JDialog dialog = optionPane.createDialog(titulo);
        dialog.setAlwaysOnTop(true);
        dialog.setVisible(true);
        dialog.toFront();
        dialog.requestFocus();
        return (int) optionPane.getValue();
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JToggleButton boton0;
    private javax.swing.JToggleButton boton1;
    private javax.swing.JToggleButton boton2;
    private javax.swing.JToggleButton boton3;
    private javax.swing.JToggleButton boton4;
    private javax.swing.JToggleButton boton5;
    private javax.swing.ButtonGroup botonGrup;
    private javax.swing.JPanel botones;
    private javax.swing.JButton closeSesion;
    public static javax.swing.JLabel contInc;
    public static javax.swing.JPasswordField contraF1;
    private javax.swing.JLabel etiquetaC1;
    private javax.swing.JLabel etiquetaN1;
    public static javax.swing.JToggleButton eyeB1;
    private javax.swing.JPanel iniSession;
    private javax.swing.JButton initerDia;
    public static javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JMenuItem opcion1;
    private javax.swing.JMenuItem opcion2;
    private javax.swing.JPanel panelPrin;
    private javax.swing.JButton perfil;
    private javax.swing.JPopupMenu popupMenu;
    private javax.swing.JComboBox<String> usuarioBox;
    private javax.swing.JButton veriB1;
    // End of variables declaration//GEN-END:variables
}
