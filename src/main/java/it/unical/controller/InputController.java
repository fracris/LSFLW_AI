package it.unical.controller;

import it.unical.controller.GameController;
import it.unical.gui.GamePanel;
import it.unical.model.Player;
import it.unical.model.StarSystem;

import javax.swing.plaf.basic.BasicSliderUI;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

public class InputController extends MouseAdapter {
    private GameController gameController;
    private Point lastMousePosition;

    public InputController(GameController gameController) {
        this.gameController = gameController;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        lastMousePosition = e.getPoint();

        // Controlla se è il turno del giocatore umano
        Player currentPlayer = gameController.getGameState().getCurrentPlayer();
        if (currentPlayer.isAI()) {
            return; // Non fare nulla durante il turno dell'IA
        }

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
        if (lastMousePosition != null) {
            Point currentMousePosition = e.getPoint();
            int deltaX = currentMousePosition.x - lastMousePosition.x;
            int deltaY = currentMousePosition.y - lastMousePosition.y;

            GamePanel gamePanel = gameController.getGamePanel();
            gamePanel.panMap(deltaX, deltaY);

            lastMousePosition = currentMousePosition;
            gamePanel.repaint();
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        lastMousePosition = null;
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        GamePanel gamePanel = gameController.getGamePanel();
        int notches = e.getWheelRotation();
        if (notches < 0) {
            // Zoom in
            gamePanel.zoomMap(1.1, e.getPoint()); // Increase zoom by 10%
        } else {
            // Zoom out
            gamePanel.zoomMap(0.9, e.getPoint()); // Decrease zoom by 10%
        }
        gamePanel.repaint();
    }
}