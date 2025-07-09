package it.unical.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import com.formdev.flatlaf.FlatDarkLaf;
import it.unical.model.Difficulty;
import it.unical.Main;

public class LevelSelectionFrame extends JFrame {

    public LevelSelectionFrame() {
        FlatDarkLaf.install();
        setTitle("Seleziona Livello");
        setSize(520, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setUndecorated(true);
        setOpacity(0f);

        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                GradientPaint gp = new GradientPaint(0, 0, new Color(10, 10, 30), 0, getHeight(), new Color(30, 30, 60));
                g2.setPaint(gp);
                g2.fillRect(0,0,getWidth(),getHeight());
            }
        };
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        JLabel title = new JLabel("Seleziona il livello");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 28f));
        title.setForeground(new Color(200,200,255,0));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(Box.createVerticalGlue());
        panel.add(title);
        panel.add(Box.createVerticalStrut(40));

        panel.add(createLevelPanel("Facile", Difficulty.easy(), new String[]{
                "• Una sola strategia di gioco disponibile",
                "• Nessun consolidamento delle truppe",
                "• Possibilità di inviare un'azione alla volta"
        }));

        panel.add(Box.createVerticalStrut(20));

        panel.add(createLevelPanel("Medio", Difficulty.medium(), new String[]{
                "• Strategie disponibili: Attacco diretto, Cooperazione, Difesa o Espansione",
                "• Una strategia attiva per ogni decisione",
                "• Rafforzamento dei confini disponibile con un massimo di 3 invii"
        }));

        panel.add(Box.createVerticalStrut(20));

        panel.add(createLevelPanel("Difficile", Difficulty.hard(), new String[]{
                "• Due strategie selezionabili per ogni decisione",
                "• Rafforzamento dei confini con un massimo di 5 invii",
                "• Necessaria rapidità nelle decisioni"
        }));


        panel.add(Box.createVerticalGlue());
        add(panel);
        setVisible(true);

        fadeInWindow();
        fadeInTitle(title);
    }

    private JPanel createLevelPanel(String levelName, Difficulty difficulty, String[] points) {
        JPanel levelPanel = new JPanel();
        levelPanel.setLayout(new BoxLayout(levelPanel, BoxLayout.Y_AXIS));
        levelPanel.setBackground(new Color(50, 50, 80));
        levelPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JButton levelButton = new JButton(levelName);
        levelButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        levelButton.setFont(levelButton.getFont().deriveFont(Font.BOLD, 18f));
        levelButton.setBackground(new Color(70,70,120));
        levelButton.setForeground(Color.WHITE);
        levelButton.setFocusPainted(false);
        levelButton.setBorder(BorderFactory.createEmptyBorder(10,20,10,20));
        levelButton.setContentAreaFilled(true);
        levelButton.setOpaque(true);
        levelButton.addActionListener(e -> {
            Main.startGame(difficulty);
            dispose();
        });

        levelPanel.add(levelButton);
        levelPanel.add(Box.createVerticalStrut(10));

        for (String point : points) {
            JLabel pointLabel = new JLabel(point);
            pointLabel.setForeground(Color.WHITE);
            pointLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            levelPanel.add(pointLabel);
        }

        return levelPanel;
    }

    private void fadeInWindow() {
        Timer t = new Timer(20, null);
        t.addActionListener(e -> {
            float o = getOpacity() + 0.02f;
            if (o >= 1f) { o = 1f; ((Timer)e.getSource()).stop(); }
            setOpacity(o);
        });
        t.start();
    }

    private void fadeInTitle(JLabel title) {
        Timer t = new Timer(30, null);
        t.setInitialDelay(300);
        t.addActionListener(new ActionListener() {
            int alpha = 0;
            @Override
            public void actionPerformed(ActionEvent e) {
                alpha += 15;
                if (alpha >= 255) { alpha = 255; ((Timer)e.getSource()).stop(); }
                title.setForeground(new Color(200,200,255,alpha));
            }
        });
        t.start();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(LevelSelectionFrame::new);
    }
}
