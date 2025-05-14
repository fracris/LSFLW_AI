package it.unical.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.Timer;
import com.formdev.flatlaf.FlatDarkLaf;

public class MainMenuFrame extends JFrame {
    private float opacity = 0f;
    private JLabel title;

    public MainMenuFrame() {
        // Set modern Look and Feel
        FlatDarkLaf.install();

        setTitle("Little Stars for Little Wars");
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setUndecorated(true);
        setOpacity(0f);

        // Background panel with gradient
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                GradientPaint gp = new GradientPaint(0, 0, new Color(10, 10, 30), 0, getHeight(), new Color(40, 40, 80));
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        title = new JLabel("Little Stars for Little Wars");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 32f));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setForeground(new Color(200, 200, 255, 0));

        AnimatedButton playButton = new AnimatedButton("Play");
        playButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        playButton.setMaximumSize(new Dimension(150, 50));
        playButton.addActionListener(e -> {
            new LevelSelectionFrame();
            dispose();
        });

        panel.add(Box.createVerticalGlue());
        panel.add(title);
        panel.add(Box.createVerticalStrut(40));
        panel.add(playButton);
        panel.add(Box.createVerticalGlue());

        // Aggiungi sezione crediti
        addCredits(panel);

        add(panel);
        setVisible(true);

        fadeInWindow();
        fadeInTitle();
    }

    private void fadeInWindow() {
        Timer timer = new Timer(20, null);
        timer.addActionListener(e -> {
            opacity += 0.02f;
            if (opacity >= 1f) {
                opacity = 1f;
                timer.stop();
            }
            setOpacity(opacity);
        });
        timer.start();
    }

    private void fadeInTitle() {
        Timer timer = new Timer(30, null);
        timer.addActionListener(new ActionListener() {
            float alpha = 0f;
            @Override
            public void actionPerformed(ActionEvent e) {
                alpha += 0.03f;
                if (alpha >= 1f) {
                    alpha = 1f;
                    ((Timer)e.getSource()).stop();
                }
                title.setForeground(new Color(200, 200, 255, (int)(alpha*255)));
                title.repaint();
            }
        });
        timer.setInitialDelay(500);
        timer.start();
    }

    // Custom button with hover scaling
    private static class AnimatedButton extends JButton {
        private float scale = 1f;
        public AnimatedButton(String text) {
            super(text);
            setFont(getFont().deriveFont(Font.BOLD, 18f));
            setFocusPainted(false);
            setBackground(new Color(70, 70, 120));
            setForeground(Color.WHITE);
            setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    animateScale(1.1f);
                }
                @Override
                public void mouseExited(MouseEvent e) {
                    animateScale(1f);
                }
            });
        }

        private void animateScale(float target) {
            Timer timer = new Timer(15, null);
            timer.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    scale += (target - scale) * 0.2f;
                    if (Math.abs(scale - target) < 0.01f) {
                        scale = target;
                        ((Timer)e.getSource()).stop();
                    }
                    repaint();
                }
            });
            timer.start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g.create();
            int w = getWidth();
            int h = getHeight();
            int sw = (int)(w * scale);
            int sh = (int)(h * scale);
            int x = (w - sw) / 2;
            int y = (h - sh) / 2;
            g2d.translate(x, y);
            g2d.scale(scale, scale);
            super.paintComponent(g2d);
            g2d.dispose();
        }
    }

    private void addCredits(JPanel panel) {
        JPanel creditsPanel = new JPanel();
        creditsPanel.setOpaque(false);
        creditsPanel.setLayout(new BoxLayout(creditsPanel, BoxLayout.Y_AXIS));

        JLabel creditsTitle = new JLabel("Credits");
        creditsTitle.setFont(new Font("Arial", Font.BOLD, 14));
        creditsTitle.setForeground(new Color(180, 180, 200));
        creditsTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel team = new JLabel("Sviluppato da: Giuseppe Rudi, Simone Cozza, Francesco Cristiano");
        team.setFont(new Font("Arial", Font.PLAIN, 12));
        team.setForeground(new Color(180, 180, 200));
        team.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel affiliation = new JLabel("Università della Calabria - Dipartimento di Matematica e Informatica");
        affiliation.setFont(new Font("Arial", Font.PLAIN, 12));
        affiliation.setForeground(new Color(180, 180, 200));
        affiliation.setAlignmentX(Component.CENTER_ALIGNMENT);

        creditsPanel.add(Box.createVerticalStrut(20));
        creditsPanel.add(creditsTitle);
        creditsPanel.add(Box.createVerticalStrut(5));
        creditsPanel.add(team);
        creditsPanel.add(affiliation);
        creditsPanel.add(Box.createVerticalStrut(10));

        panel.add(Box.createVerticalStrut(30));
        panel.add(creditsPanel);
    }
}

