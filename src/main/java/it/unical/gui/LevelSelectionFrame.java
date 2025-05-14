package it.unical.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.Timer;
import com.formdev.flatlaf.FlatDarkLaf;

public class LevelSelectionFrame extends JFrame {
    public LevelSelectionFrame() {
        FlatDarkLaf.install();
        setTitle("Seleziona Livello");
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setUndecorated(true);
        setOpacity(0f);

        // Panel con sfondo a gradiente
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

        // Titolo
        JLabel title = new JLabel("Seleziona il livello");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 28f));
        title.setForeground(new Color(200,200,255,0));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(Box.createVerticalGlue());
        panel.add(title);
        panel.add(Box.createVerticalStrut(40));

        // Pulsanti con animazione hover
        String[] levels = {"Facile", "Medio", "Difficile"};
        for (String lvl : levels) {
            AnimatedButton btn = new AnimatedButton(lvl);
            btn.setAlignmentX(Component.CENTER_ALIGNMENT);
            btn.setMaximumSize(new Dimension(200, 50));
            btn.addActionListener(e -> {
                it.unical.Main.startGame(lvl);
                dispose();
            });
            panel.add(btn);
            panel.add(Box.createVerticalStrut(20));
        }

        panel.add(Box.createVerticalGlue());
        add(panel);
        setVisible(true);

        fadeInWindow();
        fadeInTitle(title);
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

    // Pulsante con hover scaling
    private static class AnimatedButton extends JButton {
        private float scale = 1f;
        public AnimatedButton(String text) {
            super(text);
            setFont(getFont().deriveFont(Font.BOLD, 18f));
            setFocusPainted(false);
            setBackground(new Color(70,70,120));
            setForeground(Color.WHITE);
            setBorder(BorderFactory.createEmptyBorder(10,20,10,20));
            setContentAreaFilled(false);
            setOpaque(true);

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) { animateScale(1.1f); }
                @Override
                public void mouseExited(MouseEvent e) { animateScale(1f); }
            });
        }
        private void animateScale(float target) {
            Timer t = new Timer(15, null);
            t.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    scale += (target - scale) * 0.2f;
                    if (Math.abs(scale - target) < 0.01f) { scale = target; ((Timer)e.getSource()).stop(); }
                    repaint();
                }
            }); t.start();
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            int w = getWidth(), h = getHeight();
            int sw = (int)(w * scale), sh = (int)(h * scale);
            g2.translate((w-sw)/2, (h-sh)/2);
            g2.scale(scale, scale);
            super.paintComponent(g2);
            g2.dispose();
        }
    }

    public static void main(String[] args) { SwingUtilities.invokeLater(LevelSelectionFrame::new); }
}
