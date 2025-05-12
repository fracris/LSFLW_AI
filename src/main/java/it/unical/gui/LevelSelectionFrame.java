package it.unical.gui;
// LevelSelectionFrame.java

import it.unical.Main;

import javax.swing.*;
import java.awt.*;

public class LevelSelectionFrame extends JFrame {
    public LevelSelectionFrame() {
        setTitle("Seleziona Livello");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setBackground(new Color(30, 30, 30));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel label = new JLabel("Seleziona il livello:");
        label.setFont(new Font("Arial", Font.BOLD, 20));
        label.setForeground(Color.WHITE);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(Box.createVerticalGlue());
        panel.add(label);
        panel.add(Box.createVerticalStrut(20));

        String[] levels = {"Facile", "Medio", "Difficile"};
        for (String lvl : levels) {
            JButton btn = new JButton(lvl);
            btn.setAlignmentX(Component.CENTER_ALIGNMENT);
            btn.addActionListener(e -> {
                Main.startGame(lvl);
                dispose();
            });
            panel.add(btn);
            panel.add(Box.createVerticalStrut(10));
        }

        panel.add(Box.createVerticalGlue());
        add(panel);
        setVisible(true);
    }
}
