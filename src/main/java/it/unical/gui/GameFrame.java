package it.unical.gui;

import it.unical.controller.GameController;

import javax.swing.*;
import java.awt.*;

public class GameFrame extends JFrame {
    private GamePanel gamePanel;
    private StatusPanel statusPanel;
    private ControlPanel controlPanel;
    private GameController gameController;

    public GameFrame(String title, GameController gameController) {
        super(title);
        this.gameController = gameController;

        // 1) Crea i pannelli
        gamePanel    = new GamePanel(gameController);
        statusPanel  = new StatusPanel(gameController);
        controlPanel = new ControlPanel(gameController);

        // 2) Ora che gamePanel non è più null, imposta il frame nel controller
        gameController.setGameFrame(this);

        // Configurazione base della finestra
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1024, 768);
        setMinimumSize(new Dimension(800, 600));
        setLayout(new BorderLayout());


        // Aggiunge i pannelli alla finestra
        add(gamePanel, BorderLayout.CENTER);
        add(statusPanel, BorderLayout.EAST);
        add(controlPanel, BorderLayout.SOUTH);

        // Imposta gamePanel come listener delle azioni dell'utente
        gamePanel.addMouseListener(gameController.getInputController());
        gamePanel.addMouseMotionListener(gameController.getInputController());

        // Centro la finestra sullo schermo
        setLocationRelativeTo(null);
    }

    // Metodi per aggiornare l'interfaccia
    public void updateUI() {
        gamePanel.repaint();
        statusPanel.updateStatus();
        controlPanel.updateControls();
    }

    // Getters
    public GamePanel getGamePanel() { return gamePanel; }
    public StatusPanel getStatusPanel() { return statusPanel; }
    public ControlPanel getControlPanel() { return controlPanel; }
}