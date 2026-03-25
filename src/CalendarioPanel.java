import java.awt.*;
import java.awt.event.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import javax.swing.*;
import javax.swing.border.*;

public class CalendarioPanel extends JPanel {

    private static final Color COLOR_HOY_BG       = new Color(78, 150, 150);
    private static final Color COLOR_HOY_FG        = Color.WHITE;
    private static final Color COLOR_SELEC_BG      = new Color(220, 240, 240);
    private static final Color COLOR_SELEC_BORDER  = new Color(50, 110, 110);
    private static final Color COLOR_HOVER_BG      = new Color(200, 230, 230);
    private static final Color COLOR_FUTURO_FG     = new Color(170, 170, 170);
    private static final Color COLOR_HEADER_FG     = new Color(90, 90, 90);
    private static final String[] DIAS_SEMANA      = {"D", "L", "M", "M", "J", "V", "S"};

    private final LocalDate hoy;
    private LocalDate fechaSeleccionada;
    private YearMonth mesActual;

    private JLabel labelMesAnio;
    private JButton btnAnterior;
    private JButton btnSiguiente;
    private JPanel gridDias;

    public CalendarioPanel() {
        hoy = LocalDate.now();
        fechaSeleccionada = hoy;
        mesActual = YearMonth.from(hoy);
        construirUI();
        renderizarMes();
    }

    private void construirUI() {
        setLayout(new BorderLayout(0, 6));
        setBorder(new EmptyBorder(8, 12, 8, 12));

        // Panel de cabecera con navegacion
        JPanel panelHeader = new JPanel(new BorderLayout(4, 0));
        panelHeader.setOpaque(false);

        btnAnterior = crearBotonNav("‹");
        btnSiguiente = crearBotonNav("›");

        labelMesAnio = new JLabel("", SwingConstants.CENTER);
        labelMesAnio.setFont(new Font("Noto Serif", Font.BOLD, 13));

        panelHeader.add(btnAnterior, BorderLayout.WEST);
        panelHeader.add(labelMesAnio, BorderLayout.CENTER);
        panelHeader.add(btnSiguiente, BorderLayout.EAST);

        btnAnterior.addActionListener(e -> {
            mesActual = mesActual.minusMonths(1);
            renderizarMes();
        });
        btnSiguiente.addActionListener(e -> {
            if (!mesActual.equals(YearMonth.from(hoy))) {
                mesActual = mesActual.plusMonths(1);
                renderizarMes();
            }
        });

        // Grid de dias
        gridDias = new JPanel(new GridLayout(7, 7, 3, 3));
        gridDias.setOpaque(false);

        add(panelHeader, BorderLayout.NORTH);
        add(gridDias, BorderLayout.CENTER);
    }

    private void renderizarMes() {
        // Actualizar etiqueta del mes
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMMM yyyy", new Locale("es", "MX"));
        String textoMes = mesActual.format(fmt);
        labelMesAnio.setText(textoMes.substring(0, 1).toUpperCase() + textoMes.substring(1));

        // Deshabilitar "siguiente" si ya estamos en el mes actual
        btnSiguiente.setEnabled(!mesActual.equals(YearMonth.from(hoy)));

        // Limpiar grid
        gridDias.removeAll();

        // Cabeceras de dias de semana
        for (String dia : DIAS_SEMANA) {
            JLabel lbl = new JLabel(dia, SwingConstants.CENTER);
            lbl.setFont(new Font("Noto Serif", Font.BOLD, 11));
            lbl.setForeground(COLOR_HEADER_FG);
            lbl.setPreferredSize(new Dimension(34, 28));
            gridDias.add(lbl);
        }

        // Calcular offset (domingo=0, lunes=1, ..., sabado=6)
        LocalDate primerDia = mesActual.atDay(1);
        int offset = primerDia.getDayOfWeek().getValue() % 7; // ISO: Mon=1..Sun=7 -> Sun=0

        // Celdas en blanco para el offset
        for (int i = 0; i < offset; i++) {
            JPanel vacio = new JPanel();
            vacio.setOpaque(false);
            gridDias.add(vacio);
        }

        // Celdas de dias del mes
        int diasEnMes = mesActual.lengthOfMonth();
        for (int dia = 1; dia <= diasEnMes; dia++) {
            LocalDate fecha = mesActual.atDay(dia);
            boolean esHoy       = fecha.equals(hoy);
            boolean esSelec     = fecha.equals(fechaSeleccionada);
            boolean esFuturo    = fecha.isAfter(hoy);
            gridDias.add(crearCeldaDia(dia, esHoy, esSelec, esFuturo));
        }

        gridDias.revalidate();
        gridDias.repaint();
    }

    private JPanel crearCeldaDia(int dia, boolean esHoy, boolean esSelec, boolean esFuturo) {
        JPanel celda = new JPanel(new GridBagLayout());
        celda.setPreferredSize(new Dimension(34, 34));

        JLabel lbl = new JLabel(String.valueOf(dia), SwingConstants.CENTER);
        lbl.setFont(new Font("Noto Serif", Font.PLAIN, 12));
        celda.add(lbl);

        if (esHoy) {
            celda.setBackground(COLOR_HOY_BG);
            celda.setBorder(BorderFactory.createLineBorder(COLOR_HOY_BG, 1));
            lbl.setForeground(COLOR_HOY_FG);
            lbl.setFont(new Font("Noto Serif", Font.BOLD, 12));
        } else if (esSelec) {
            celda.setBackground(COLOR_SELEC_BG);
            celda.setBorder(BorderFactory.createLineBorder(COLOR_SELEC_BORDER, 2));
            lbl.setForeground(Color.BLACK);
        } else if (esFuturo) {
            celda.setOpaque(false);
            celda.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
            lbl.setForeground(COLOR_FUTURO_FG);
        } else {
            celda.setOpaque(false);
            celda.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
            lbl.setForeground(UIManager.getColor("Label.foreground"));

            final int diaFinal = dia;
            celda.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            celda.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    seleccionarFecha(LocalDate.of(mesActual.getYear(), mesActual.getMonth(), diaFinal));
                }
                @Override
                public void mouseEntered(MouseEvent e) {
                    if (!esSelec) {
                        celda.setOpaque(true);
                        celda.setBackground(COLOR_HOVER_BG);
                        celda.repaint();
                    }
                }
                @Override
                public void mouseExited(MouseEvent e) {
                    if (!esSelec) {
                        celda.setOpaque(false);
                        celda.repaint();
                    }
                }
            });
        }

        // Dia de hoy tambien es clickeable (para re-seleccionarlo si se navego)
        if (esHoy) {
            final int diaFinal = dia;
            celda.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            celda.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    seleccionarFecha(LocalDate.of(mesActual.getYear(), mesActual.getMonth(), diaFinal));
                }
            });
        }

        return celda;
    }

    private JButton crearBotonNav(String texto) {
        JButton btn = new JButton(texto);
        btn.setFont(new Font("Noto Serif", Font.BOLD, 16));
        btn.setForeground(COLOR_HOY_BG);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(32, 28));
        return btn;
    }

    public void seleccionarFecha(LocalDate fecha) {
        if (!fecha.isAfter(hoy)) {
            fechaSeleccionada = fecha;
            mesActual = YearMonth.from(fecha);
            renderizarMes();
        }
    }

    public LocalDate getFechaSeleccionada() {
        return fechaSeleccionada;
    }
}
