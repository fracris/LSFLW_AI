package it.unical.controller;

import it.unical.controller.GameController;
import it.unical.gui.GamePanel;
import it.unical.model.Player;
import it.unical.model.StarSystem;

import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class InputController extends MouseAdapter {
    private GameController gameController;
    private Point lastMousePosition;

    public InputController(GameController gameController) {
        this.gameController = gameController;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        lastMousePosition = e.getPoint();


        // Converti le coordinate del mouse in coordinate della mappa
        GamePanel gamePanel = gameController.getGamePanel();
        Point mapPoint = gamePanel.screenToMap(e.getPoint());

        // Trova il sistema sotto il cursore (se c'è)
        StarSystem clickedSystem = gamePanel.findSystemAt(mapPoint);

        if (clickedSystem != null) {
            StarSystem selectedSystem = gamePanel.getSelectedSystem();

            if (selectedSystem == null) {
                // Nessun sistema selezionato, seleziona questo
                gamePanel.setSelectedSystem(clickedSystem);
            } else if (selectedSystem == clickedSystem) {
                // Cliccato lo stesso sistema, deseleziona
                gamePanel.setSelectedSystem(null);
            } else if (selectedSystem.getConnectedSystems().contains(clickedSystem)) {
                // Cliccato un sistema connesso, imposta come target
                gamePanel.setTargetSystem(clickedSystem);
            } else {
                // Cliccato un sistema non connesso, cambia selezione
                gamePanel.setSelectedSystem(clickedSystem);
            }

            // Aggiorna i controlli
            gameController.getGamePanel().repaint();
            gameController.getGameFrame().getControlPanel().updateControls();
            gameController.getGameFrame().getStatusPanel().updateSelectedSystemInfo();
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        // Implementa il trascinamento della mappa (panning)
        // TODO: Implementare
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        lastMousePosition = null;
    }
}