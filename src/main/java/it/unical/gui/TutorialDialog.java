package it.unical.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

public class TutorialDialog extends JDialog {

    public TutorialDialog(JFrame owner) {
        super(owner, "Tutorial di Gioco", true);
        setSize(620, 520);
        setLocationRelativeTo(owner);
        setUndecorated(true);

        // Main container with gradient background
        JPanel container = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                GradientPaint gp = new GradientPaint(
                        0, 0, new Color(20, 20, 40),
                        0, getHeight(), new Color(50, 50, 90)
                );
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        container.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Title bar
        JLabel header = new JLabel("Tutorial di Gioco");
        header.setFont(header.getFont().deriveFont(Font.BOLD, 24f));
        header.setForeground(new Color(220, 220, 255));
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        header.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Content panel with sections
        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        content.add(createSection("Obiettivo del Gioco", new String[]{
                "Conquista o difendi sistemi stellari.",
                "Gestisci navi e strategie AI avanzate."
        }));
        content.add(Box.createVerticalStrut(15));
        content.add(createSection("Comandi Principali", new String[]{
                "P: Pausa / Riprendi il gioco.",
                "ESC: Torna al menu principale."
        }));
        content.add(Box.createVerticalStrut(15));
        content.add(createSection("Invio di Flotte", new String[]{
                "• Modalità Classica: click sistema origine + click destinazione.",
                "• Modalità Automatica: click&drag tra sistemi propri (freccia grafica).",
                "• Doppio click per disattivare automazione."
        }));
        content.add(Box.createVerticalStrut(15));
        content.add(createSection("Percentuale di Invio", new String[]{
                "25%, 50%, 75% o 100% delle navi disponibili.",
                "Seleziona dal pannello di controllo."
        }));

        // Wrap content in scroll pane for overflow but hide scrollbar
        JScrollPane scroll = new JScrollPane(content);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        // Footer hint
        JLabel footer = new JLabel("Premi 'P' o ESC per chiudere");
        footer.setFont(footer.getFont().deriveFont(Font.ITALIC, 12f));
        footer.setForeground(new Color(180, 180, 220));
        footer.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        footer.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Assemble container
        Box vbox = Box.createVerticalBox();
        vbox.add(header);
        vbox.add(scroll);
        vbox.add(Box.createVerticalStrut(10));
        vbox.add(footer);
        container.add(vbox, BorderLayout.CENTER);

        // Key bindings
        InputMap im = container.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = container.getActionMap();
        im.put(KeyStroke.getKeyStroke('P'), "close");
        im.put(KeyStroke.getKeyStroke('p'), "close");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "close");
        am.put("close", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        setContentPane(container);
    }

    private JPanel createSection(String titleText, String[] lines) {
        JPanel section = new JPanel();
        section.setOpaque(false);
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));

        JLabel t = new JLabel(titleText);
        t.setFont(t.getFont().deriveFont(Font.BOLD, 18f));
        t.setForeground(new Color(200, 200, 255));
        t.setAlignmentX(Component.LEFT_ALIGNMENT);
        section.add(t);
        section.add(Box.createVerticalStrut(5));

        for (String line : lines) {
            JLabel lbl = new JLabel(line);
            lbl.setFont(lbl.getFont().deriveFont(Font.PLAIN, 14f));
            lbl.setForeground(new Color(220, 220, 240));
            lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
            lbl.setBorder(BorderFactory.createEmptyBorder(2, 15, 2, 0));
            section.add(lbl);
        }

        return section;
    }
}
