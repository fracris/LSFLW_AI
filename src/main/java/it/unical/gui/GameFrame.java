package it.unical.gui;

import it.unical.controller.GameController;

import javax.swing.*;
import java.awt.*;

public class GameFrame extends JFrame {
    private final GamePanel gamePanel;
    private final StatusPanel statusPanel;
    private final ControlPanel controlPanel;

    public GameFrame(String title, GameController gameController) {
        super(title);

        gamePanel    = new GamePanel(gameController);
        statusPanel  = new StatusPanel(gameController);
        controlPanel = new ControlPanel(gameController);

        gameController.setGameFrame(this);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1024, 768);
        setMinimumSize(new Dimension(800, 600));
        setLayout(new BorderLayout());


        add(gamePanel, BorderLayout.CENTER);
        add(statusPanel, BorderLayout.EAST);
        add(controlPanel, BorderLayout.SOUTH);

        gamePanel.addMouseListener(gameController.getInputController());
        gamePanel.addMouseMotionListener(gameController.getInputController());
        gamePanel.addMouseWheelListener(gameController.getInputController());

        setLocationRelativeTo(null);
    }

    public void updateUI() {
        gamePanel.repaint();
        statusPanel.updateStatus();
        controlPanel.updateControls();
    }

    public GamePanel getGamePanel() { return gamePanel; }
    public StatusPanel getStatusPanel() { return statusPanel; }
    public ControlPanel getControlPanel() { return controlPanel; }
}