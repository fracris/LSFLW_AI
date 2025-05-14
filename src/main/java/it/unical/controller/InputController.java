package it.unical.controller;

import it.unical.gui.GamePanel;
import it.unical.model.Player;
import it.unical.model.StarSystem;

import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.Arrays;

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
                Player humanPlayer = gameController.getGameState().getHumanPlayer();
                if (clickedSystem.getOwner() == humanPlayer) {
                    gamePanel.setSelectedSystem(clickedSystem);
                }
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

            gameController.getGameFrame().getControlPanel().sendFleet();
            // Aggiorna i controlli
            gameController.getGamePanel().repaint();
            gameController.getGameFrame().getControlPanel().updateControls();
            gameController.getGameFrame().getStatusPanel().updateSelectedSystemInfo();
        }
    }

    public void mouseDragged(MouseEvent e) {
        Point currentMousePosition = e.getPoint();
        GamePanel gamePanel = gameController.getGamePanel();

        if (lastMousePosition != null && gamePanel != null) {
            StarSystem selectedSystem = gamePanel.getSelectedSystem();

            if (selectedSystem == null) {
                // Gestione normale del trascinamento per spostare la mappa
                int dx = currentMousePosition.x - lastMousePosition.x;
                int dy = currentMousePosition.y - lastMousePosition.y;
                gamePanel.panMap(dx, dy);
                gamePanel.repaint();
            }
        }

        lastMousePosition = currentMousePosition;
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {

        GamePanel gamePanel = gameController.getGamePanel();
        if (gamePanel != null) {
            int notches = e.getWheelRotation();
            if (notches < 0) {
                // Zoom avanti
                gamePanel.zoomMap(1.1, e.getPoint()); // Zoom in con un fattore di ingrandimento
            } else {
                // Zoom indietro
                gamePanel.zoomMap(0.9, e.getPoint()); // Zoom out con un fattore di riduzione
            }
            gamePanel.repaint();
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        Point currentMousePosition = e.getPoint();
        GamePanel gamePanel = gameController.getGamePanel();
        Point mapEndPoint = gamePanel.screenToMap(currentMousePosition);
        StarSystem selectedSystem = gamePanel.getSelectedSystem();
        if(selectedSystem!=null && currentMousePosition != lastMousePosition) {
            StarSystem targetSystem = gamePanel.findSystemAt(mapEndPoint);

            if (targetSystem != null && selectedSystem.getConnectedSystems().contains(targetSystem)) {
                // Invio automatico di flotte
                Player humanPlayer = gameController.getGameState().getHumanPlayer();
                if (selectedSystem.getOwner() == humanPlayer) {
                    gamePanel.setSelectedSystem(null);
                    if(selectedSystem.getAutomatedTo()!=targetSystem) {
                        if(!selectedSystem.isAutomated()) {
                            selectedSystem.setAutomated(true);
                        }
                        selectedSystem.setAutomatedTo(targetSystem);
                        selectedSystem.setSendMode(gameController.getSendPerc());
                        new Thread(() -> {
                            while (selectedSystem.isAutomated() && selectedSystem.getAutomatedTo()==targetSystem && selectedSystem.getShips() > 1) { // Modifica: deve avere più di 1 nave
                                // Calcola quante navi inviare in base alla percentuale
                                int shipsToSend = calculateShipsToSend(selectedSystem, selectedSystem.getSendMode());

                                // Invia le navi solo se ci sono abbastanza navi
                                if (shipsToSend > 0) {
                                    gameController.sendFleet(selectedSystem, targetSystem, shipsToSend);
                                }

                                gamePanel.repaint();
                                try {
                                    Thread.sleep(2000);
                                } catch (InterruptedException ex) {
                                    throw new RuntimeException(ex);
                                }
                            }
                        }).start();
                    }
                    else {
                        selectedSystem.setAutomated(false);
                        selectedSystem.setAutomatedTo(null);
                    }
                }
            }
        } else lastMousePosition = null;
    }

    // Metodo per calcolare quante navi inviare in base alla percentuale
    private int calculateShipsToSend(StarSystem system, int percentage) {
        int totalShips = system.getShips();
        int shipsToSend = totalShips * percentage / 100;

        // Assicurati di non inviare 0 navi e di lasciare sempre almeno una nave
        return Math.max(1, Math.min(shipsToSend, totalShips - 1));
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2 && !e.isConsumed()) {
            e.consume();
            GamePanel gamePanel = gameController.getGamePanel();
            Point mapPoint = gamePanel.screenToMap(e.getPoint());
            StarSystem selectedSystem = gamePanel.findSystemAt(mapPoint);

            if (selectedSystem != null && selectedSystem.isAutomated()) {
                selectedSystem.setAutomated(false);
                selectedSystem.setAutomatedTo(null);
            }
        }
    }
}
