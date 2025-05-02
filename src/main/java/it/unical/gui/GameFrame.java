package it.unical.gui;

import it.unical.controller.GameController;
import it.unical.controller.InputController;

import javax.swing.*;
import java.awt.*;

public class GameFrame extends JFrame {
    private GamePanel gamePanel;
    private StatusPanel statusPanel;
    private ControlPanel controlPanel;

    public GameFrame(GameController controller) {
        super("Little Stars for Little Wars");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1024, 768);
        setLayout(new BorderLayout());

        // Inizializza i pannelli
        gamePanel = new GamePanel(controller);
        statusPanel = new StatusPanel(controller);
        controlPanel = new ControlPanel(controller);

        // Aggiunge i pannelli al frame
        add(gamePanel, BorderLayout.CENTER);
        add(statusPanel, BorderLayout.NORTH);
        add(controlPanel, BorderLayout.SOUTH);

        // Inizializza gli ascoltatori
        addKeyListener(new InputController(controller));

        setVisible(true);
    }

    public GamePanel getGamePanel() {
        return gamePanel;
    }

    public void setGamePanel(GamePanel gamePanel) {
        this.gamePanel = gamePanel;
    }

    public ControlPanel getControlPanel() {
        return controlPanel;
    }

    public void setControlPanel(ControlPanel controlPanel) {
        this.controlPanel = controlPanel;
    }

    public StatusPanel getStatusPanel() {
        return statusPanel;
    }

    public void setStatusPanel(StatusPanel statusPanel) {
        this.statusPanel = statusPanel;
    }

    public void update() {
        gamePanel.repaint();
        statusPanel.update();
        controlPanel.update();
    }
}