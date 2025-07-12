package it.unical.controller;

import it.unical.gui.GamePanel;
import it.unical.model.Player;
import it.unical.model.StarSystem;

import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

public class InputController extends MouseAdapter {
    private final GameController gameController;
    private Point lastMousePosition;


    public InputController(GameController gameController) {
        this.gameController = gameController;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        lastMousePosition = e.getPoint();

        GamePanel gamePanel = gameController.getGamePanel();
        Point mapPoint = gamePanel.screenToMap(e.getPoint());

        StarSystem clickedSystem = gamePanel.findSystemAt(mapPoint);

        if (clickedSystem != null) {
            StarSystem selectedSystem = gamePanel.getSelectedSystem();

            if (selectedSystem == null) {
                Player humanPlayer = gameController.getGameState().getHumanPlayer();
                if (clickedSystem.getOwner() == humanPlayer) {
                    gamePanel.setSelectedSystem(clickedSystem);
                }
            } else if (selectedSystem == clickedSystem) {
                gamePanel.setSelectedSystem(null);
            } else if (selectedSystem.getConnectedSystems().contains(clickedSystem)) {
                gamePanel.setTargetSystem(clickedSystem);
            } else {
                gamePanel.setSelectedSystem(clickedSystem);
            }

            gameController.getGameFrame().getControlPanel().sendFleet();
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
                gamePanel.zoomMap(1.1, e.getPoint());
            } else {
                gamePanel.zoomMap(0.9, e.getPoint());
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
                            while (selectedSystem.isAutomated() && selectedSystem.getAutomatedTo()==targetSystem && selectedSystem.getShips() > 1) {
                                int shipsToSend = calculateShipsToSend(selectedSystem, selectedSystem.getSendMode());

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

    private int calculateShipsToSend(StarSystem system, int percentage) {
        int totalShips = system.getShips();
        int shipsToSend = totalShips * percentage / 100;

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
