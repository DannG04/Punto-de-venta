
import com.jtattoo.plaf.mcwin.McWinLookAndFeel;
import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.Toolkit;
import java.awt.Dimension;
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
    DevolucionesP devPanel = new DevolucionesP();
    ClientesP cliPanel = new ClientesP();
    GastosP gastPanel =  new GastosP();
    GananciasP ganPanel = new GananciasP();
    //Administracion
    ComprasP comPanel = new ComprasP();
    EmpleadosP empPanel = new EmpleadosP();
    
    //Conexion
    ConexionBD conect = new ConexionBD();
    static String idVendedor = "";
    
    boolean iniDia = false;
    Dimension tamanio = Toolkit.getDefaultToolkit().getScreenSize();

    /**
     * Creates new form Interfaz
     */
    public Interfaz() {
        initComponents();
        Hora fecha = new Hora();
        fecha.start();
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
        opcion3 = new javax.swing.JMenuItem();
        adminPM = new javax.swing.JPopupMenu();
        comp1 = new javax.swing.JMenuItem();
        emp2 = new javax.swing.JMenuItem();
        regV3 = new javax.swing.JMenuItem();
        regCont = new javax.swing.JMenu();
        balG5 = new javax.swing.JMenuItem();
        estR6 = new javax.swing.JMenuItem();
        vtasHoy7 = new javax.swing.JMenuItem();
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
        boton6 = new javax.swing.JToggleButton();
        boton7 = new javax.swing.JToggleButton();
        boton8 = new javax.swing.JToggleButton();
        iniSession = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        etiquetaN1 = new javax.swing.JLabel();
        etiquetaC1 = new javax.swing.JLabel();
        contraF1 = new javax.swing.JPasswordField();
        eyeB1 = new javax.swing.JToggleButton();
        contInc = new javax.swing.JLabel();
        veriB1 = new javax.swing.JButton();
        usuarioBox = new javax.swing.JComboBox<>();
        jSeparator1 = new javax.swing.JSeparator();
        horaPanel = new javax.swing.JPanel();
        labelHora = new javax.swing.JLabel();

        popupMenu.setToolTipText("");

        opcion1.setText("Nombre: ");
        popupMenu.add(opcion1);

        opcion2.setText("Puesto:");
        opcion2.setToolTipText("");
        popupMenu.add(opcion2);

        opcion3.setText("jMenuItem1");
        popupMenu.add(opcion3);

        comp1.setText("Compras");
        comp1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comp1ActionPerformed(evt);
            }
        });
        adminPM.add(comp1);

        emp2.setText("Empleados");
        emp2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                emp2ActionPerformed(evt);
            }
        });
        adminPM.add(emp2);

        regV3.setText("Registro de Ventas");
        regV3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                regV3ActionPerformed(evt);
            }
        });
        adminPM.add(regV3);

        regCont.setText("Registros Contables");

        balG5.setText("Balance General");
        balG5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                balG5ActionPerformed(evt);
            }
        });
        regCont.add(balG5);

        estR6.setText("Estado de Resultados");
        estR6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                estR6ActionPerformed(evt);
            }
        });
        regCont.add(estR6);

        vtasHoy7.setText("Reporte Diario");
        vtasHoy7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                vtasHoy7ActionPerformed(evt);
            }
        });
        regCont.add(vtasHoy7);

        adminPM.add(regCont);

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Punto de Ventas");
        setMinimumSize(new java.awt.Dimension(430, 400));
        setResizable(false);
        setSize(new java.awt.Dimension(430, 400));
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        panelPrin.setBackground(new java.awt.Color(204, 226, 249));
        panelPrin.setLayout(new java.awt.GridBagLayout());

        perfil.setBackground(new java.awt.Color(204, 226, 249));
        perfil.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/usuariu1.png"))); // NOI18N
        perfil.setMaximumSize(new java.awt.Dimension(90, 90));
        perfil.setMinimumSize(new java.awt.Dimension(90, 90));
        perfil.setPreferredSize(new java.awt.Dimension(160, 160));
        perfil.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                perfilMouseEntered(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 12, 0);
        panelPrin.add(perfil, gridBagConstraints);

        closeSesion.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        closeSesion.setForeground(new java.awt.Color(78, 150, 150));
        closeSesion.setText("Cerrar sesión");
        closeSesion.setMaximumSize(new java.awt.Dimension(115, 25));
        closeSesion.setMinimumSize(new java.awt.Dimension(115, 25));
        closeSesion.setPreferredSize(new java.awt.Dimension(160, 60));
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

        initerDia.setFont(new java.awt.Font("Noto Serif", 1, 18)); // NOI18N
        initerDia.setForeground(new java.awt.Color(78, 150, 150));
        initerDia.setText("Iniciar día");
        initerDia.setMaximumSize(new java.awt.Dimension(115, 25));
        initerDia.setMinimumSize(new java.awt.Dimension(115, 25));
        initerDia.setPreferredSize(new java.awt.Dimension(160, 60));
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
        jLabel3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/fondo.jpg"))); // NOI18N
        jLabel3.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jLabel3.setMaximumSize(new java.awt.Dimension(2147483647, 2147483647));
        jLabel3.setMinimumSize(new java.awt.Dimension(1620, 990));
        jLabel3.setPreferredSize(new java.awt.Dimension(1620, 990));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 3;
        panelPrin.add(jLabel3, gridBagConstraints);

        getContentPane().add(panelPrin, java.awt.BorderLayout.CENTER);
        panelPrin.setVisible(false);

        botones.setBackground(new java.awt.Color(204, 226, 249));
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
        boton3.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                boton3MouseClicked(evt);
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

        botonGrup.add(boton6);
        boton6.setFont(new java.awt.Font("Noto Serif", 1, 16)); // NOI18N
        boton6.setForeground(new java.awt.Color(78, 150, 150));
        boton6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/dev.png"))); // NOI18N
        boton6.setText("Devoluciones");
        boton6.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        boton6.setMaximumSize(new java.awt.Dimension(300, 400));
        boton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                boton6ActionPerformed(evt);
            }
        });
        botones.add(boton6);
        boton3.setVisible(false);

        botonGrup.add(boton7);
        boton7.setFont(new java.awt.Font("Noto Serif", 1, 16)); // NOI18N
        boton7.setForeground(new java.awt.Color(78, 150, 150));
        boton7.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/gtos.png"))); // NOI18N
        boton7.setText("Gastos");
        boton7.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        boton7.setMaximumSize(new java.awt.Dimension(300, 400));
        boton7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                boton7ActionPerformed(evt);
            }
        });
        botones.add(boton7);
        boton3.setVisible(false);

        botonGrup.add(boton8);
        boton8.setFont(new java.awt.Font("Noto Serif", 1, 16)); // NOI18N
        boton8.setForeground(new java.awt.Color(78, 150, 150));
        boton8.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/gansias.png"))); // NOI18N
        boton8.setText("Ganancias");
        boton8.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        boton8.setMaximumSize(new java.awt.Dimension(300, 400));
        boton8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                boton8ActionPerformed(evt);
            }
        });
        botones.add(boton8);
        boton3.setVisible(false);

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
        usuarioBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Daniel Gonzalez", "Gael Eduardo García", "Elisa Sánchez" }));
        usuarioBox.setPreferredSize(new java.awt.Dimension(180, 25));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        iniSession.add(usuarioBox, gridBagConstraints);
        iniSession.add(jSeparator1, new java.awt.GridBagConstraints());

        getContentPane().add(iniSession, java.awt.BorderLayout.PAGE_START);

        horaPanel.setBackground(new java.awt.Color(204, 226, 249));
        horaPanel.setLayout(new java.awt.GridBagLayout());

        labelHora.setFont(new java.awt.Font("Ubuntu Sans Mono", 0, 18)); // NOI18N
        labelHora.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        labelHora.setText("00:00");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        horaPanel.add(labelHora, gridBagConstraints);

        getContentPane().add(horaPanel, java.awt.BorderLayout.SOUTH);
        horaPanel.setVisible(false);

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

    private void boton0ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_boton0ActionPerformed
        botonesFore(0);
        visibilidad(0, 0);
    }//GEN-LAST:event_boton0ActionPerformed

    private void boton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_boton1ActionPerformed
        botonesFore(1);
        visibilidad(1, 0);
    }//GEN-LAST:event_boton1ActionPerformed

    private void boton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_boton2ActionPerformed
        botonesFore(2);
        visibilidad(2, 0);
    }//GEN-LAST:event_boton2ActionPerformed

    private void boton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_boton4ActionPerformed
        botonesFore(4);
        visibilidad(4, 0);
    }//GEN-LAST:event_boton4ActionPerformed

    private void boton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_boton5ActionPerformed
        botonesFore(5);
        visibilidad(5, 0);
    }//GEN-LAST:event_boton5ActionPerformed

    private void initerDiaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_initerDiaActionPerformed
        if(!iniDia){
            Mise.JOption("Día iniciado","Información del día", javax.swing.JOptionPane.PLAIN_MESSAGE);
            initerDia.setText("Terminar día");
            iniDia = true;
            boton2.setVisible(true);
        }
        else{
            docsPanel.mostrarCierreCaja();
            docsPanel.cierreCajaDialog.setVisible(true);
            Mise.JOption("Día terminado. Se han guardado las ventas del día", "Cierre de caja", javax.swing.JOptionPane.PLAIN_MESSAGE);
            initerDia.setText("Ïniciar día");
            iniDia = false;
            boton2.setVisible(false);
        }
    }//GEN-LAST:event_initerDiaActionPerformed

    private void closeSesionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeSesionActionPerformed
        if(!iniDia){
            Mise.JOption("Hasta luego", "Salir", javax.swing.JOptionPane.PLAIN_MESSAGE);
            userActivo = false;
            admActivo = false;
            contraF1.setText("");
            contInc.setText("");
            eyeB1.setSelected(false);
            eyeB1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/eye2.png")));
            iniSession.setVisible(true);
            botones.setVisible(false);
            panelPrin.setVisible(false);
            horaPanel.setVisible(false);
            this.setSize(430, 400);
            this.setLocationRelativeTo(null);
        }
        else{
            Mise.JOption(null, "Debe terminar el día para cerrar sesión", javax.swing.JOptionPane.WARNING_MESSAGE);
        }
    }//GEN-LAST:event_closeSesionActionPerformed

    private void boton3MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_boton3MouseClicked
        botonesFore(3);
        adminPM.show(evt.getComponent(), evt.getX(), evt.getY());
    }//GEN-LAST:event_boton3MouseClicked

    private void boton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_boton6ActionPerformed
        visibilidad(6, 0);
    }//GEN-LAST:event_boton6ActionPerformed

    private void comp1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comp1ActionPerformed
        visibilidad(3, 1);
    }//GEN-LAST:event_comp1ActionPerformed

    private void emp2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_emp2ActionPerformed
        visibilidad(3, 2);
    }//GEN-LAST:event_emp2ActionPerformed

    private void balG5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_balG5ActionPerformed
        Excel.balanceGeneral();
    }//GEN-LAST:event_balG5ActionPerformed

    private void estR6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_estR6ActionPerformed
        String res = Mise.JOptionInput("Ingrese el inventario inicial", "Estado de Resultados");
        try{
            Double invini = Double.valueOf(res);
            Excel.estadodeResultados(invini);
        } catch(NumberFormatException e){
            Mise.JOption("Debe ingresar un numero", "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_estR6ActionPerformed

    private void vtasHoy7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_vtasHoy7ActionPerformed
        Excel.reporteDiario("Reporte_Diario");
    }//GEN-LAST:event_vtasHoy7ActionPerformed

    private void perfilMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_perfilMouseEntered
        popupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
    }//GEN-LAST:event_perfilMouseEntered

    private void regV3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_regV3ActionPerformed
        visibilidad(3, 3);
    }//GEN-LAST:event_regV3ActionPerformed

    private void boton7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_boton7ActionPerformed
        visibilidad(7, 0);
    }//GEN-LAST:event_boton7ActionPerformed

    private void boton8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_boton8ActionPerformed
        visibilidad(8, 0);
    }//GEN-LAST:event_boton8ActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        int res = Mise.JOptionYesNo("Seguro que desea cerrar la ventana?", "Cerrar Ventana");
        if(res == 0){
            System.exit(0);
        }
    }//GEN-LAST:event_formWindowClosing

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
            if(empPanel.userC.containsKey(us)){
                if(!empPanel.userC.get(us).equals(c)){
                    contInc.setText("Contraseña incorrecta");
                }
                else{
                    idVendedor = conect.idEmpleado(us);
                    if(idVendedor.equals("")){
                        contInc.setText("Error al iniciar sesión");
                    } else{
                        Mise.JOption("Bienvenido " + us, "Inicio de sesión", javax.swing.JOptionPane.PLAIN_MESSAGE);
                        userActivo = true;
                        if(us.equals(empPanel.administrador)){
                            admActivo = true;
                        }
                        opcion2.setText("Curp: " + idVendedor);

                        opcion1.setText("Nombre: " + us);
                        if(admActivo){
                            opcion3.setText("Puesto: Gerente");
                        } else {
                            opcion3.setText("Puesto: Vendedor");
                        }

                        boton0.setForeground(Color.BLACK);
                        boton0.setSelected(true);
                        iniSession.setVisible(false);
                        botonesVis();
                        botones.setVisible(true);
                        panelPrin.setVisible(true);
                        horaPanel.setVisible(true);
                        this.setSize(tamanio.width, tamanio.height-20);
                        this.setLocationRelativeTo(null);
                    }
                }
            }
            else{
                contInc.setText("Datos érroneos, intentelo de nuevo");
            }
        } else {
            contInc.setText("Debe llenar todos los campos");
        }
    }
    
    public void visibilidad(int a, int b){
        panelPrin.setVisible(false);
        invPanel.setVisible(false);
        ventPanel.setVisible(false);
        docsPanel.setVisible(false);
        apPanel.setVisible(false);
        cliPanel.setVisible(false);
        devPanel.setVisible(false);
        gastPanel.setVisible(false);
        ganPanel.setVisible(false);
        panelesAdm(b);
        switch(a){
            case 0://Panel Principal
                panelPrin.setVisible(true);
                break;
            case 1://Panel Inventario
                getContentPane().add(invPanel, java.awt.BorderLayout.CENTER);
                this.add(invPanel);
                invPanel.mostrarTabla("");
                invPanel.panelBotones.setVisible(admActivo);
                invPanel.setVisible(true);
                break;
            case 2://Panel Registro de Ventas
                getContentPane().add(ventPanel, java.awt.BorderLayout.CENTER);
                this.add(ventPanel);
                ventPanel.mostrarTablaVentaT();
                ventPanel.mostrarBusqueda("");
                ventPanel.setVisible(true);
                break;
            // NO HAY CASE 3
            case 4://Panel Sistema de Apartados
                getContentPane().add(apPanel, java.awt.BorderLayout.CENTER);
                this.add(apPanel);
                apPanel.mostrarTablaAp("");
                apPanel.setVisible(true);
                break;
            case 5://Panel Registro de Clientes
                getContentPane().add(cliPanel, java.awt.BorderLayout.CENTER);
                this.add(cliPanel);
                cliPanel.mostrarTabla();
                cliPanel.setVisible(true);
                break;
            case 6://Panel Devoluciones
                getContentPane().add(devPanel, java.awt.BorderLayout.CENTER);
                this.add(devPanel);
                devPanel.mostrarDev();
                devPanel.setVisible(true);
                break;
            case 7://Panel de Gastos
                getContentPane().add(gastPanel, java.awt.BorderLayout.CENTER);
                this.add(gastPanel);
                gastPanel.mostrarTablaGastos();
                gastPanel.setVisible(true);
                break;
            case 8://Panel de Ganancias
                getContentPane().add(ganPanel, java.awt.BorderLayout.CENTER);
                this.add(ganPanel);
                ganPanel.mostrarTablaOG();
                ganPanel.setVisible(true);
                break;
        }
    }
    
    public void panelesAdm(int b){
        comPanel.setVisible(false);
        empPanel.setVisible(false);
        docsPanel.setVisible(false);
        switch(b){
            case 1://Panel de Compras
                getContentPane().add(comPanel, java.awt.BorderLayout.CENTER);
                this.add(comPanel);
                comPanel.mostrarTablaCom();
                comPanel.setVisible(true);
                break;
            case 2://Panel de Empleados
                getContentPane().add(empPanel, java.awt.BorderLayout.CENTER);
                this.add(empPanel);
                empPanel.mostrarTablaEmp();
                empPanel.setVisible(true);
                break;
            case 3://Panel de Registro de Ventas
                getContentPane().add(docsPanel, java.awt.BorderLayout.CENTER);
                this.add(docsPanel);
                docsPanel.mostrarTablaVentas();
                docsPanel.setVisible(true);
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
            boton3.setVisible(admActivo);
            boton4.setVisible(true);
            boton5.setVisible(true);
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPopupMenu adminPM;
    private javax.swing.JMenuItem balG5;
    private javax.swing.JToggleButton boton0;
    private javax.swing.JToggleButton boton1;
    private javax.swing.JToggleButton boton2;
    public javax.swing.JToggleButton boton3;
    private javax.swing.JToggleButton boton4;
    private javax.swing.JToggleButton boton5;
    private javax.swing.JToggleButton boton6;
    private javax.swing.JToggleButton boton7;
    private javax.swing.JToggleButton boton8;
    private javax.swing.ButtonGroup botonGrup;
    private javax.swing.JPanel botones;
    private javax.swing.JButton closeSesion;
    private javax.swing.JMenuItem comp1;
    public static javax.swing.JLabel contInc;
    public static javax.swing.JPasswordField contraF1;
    private javax.swing.JMenuItem emp2;
    private javax.swing.JMenuItem estR6;
    private javax.swing.JLabel etiquetaC1;
    private javax.swing.JLabel etiquetaN1;
    public static javax.swing.JToggleButton eyeB1;
    public static javax.swing.JPanel horaPanel;
    private javax.swing.JPanel iniSession;
    private javax.swing.JButton initerDia;
    public static javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JSeparator jSeparator1;
    public static javax.swing.JLabel labelHora;
    private javax.swing.JMenuItem opcion1;
    private javax.swing.JMenuItem opcion2;
    private javax.swing.JMenuItem opcion3;
    private javax.swing.JPanel panelPrin;
    private javax.swing.JButton perfil;
    private javax.swing.JPopupMenu popupMenu;
    private javax.swing.JMenu regCont;
    private javax.swing.JMenuItem regV3;
    private javax.swing.JComboBox<String> usuarioBox;
    private javax.swing.JButton veriB1;
    private javax.swing.JMenuItem vtasHoy7;
    // End of variables declaration//GEN-END:variables
}
