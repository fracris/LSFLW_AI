package it.unical.controller;

import java.awt.event.*;

public class InputController extends KeyAdapter implements MouseListener, MouseMotionListener {
    private GameController controller;
    private int lastMouseX, lastMouseY;

    public InputController(GameController controller) {
        this.controller = controller;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_ESCAPE:
                // Deseleziona il sistema corrente
                controller.deselectSystem();
                break;
            case KeyEvent.VK_SPACE:
                // Termina il turno
                controller.endTurn();
                break;
            case KeyEvent.VK_LEFT:
                // Sposta la visuale a sinistra
                controller.panView(-10, 0);
                break;
            case KeyEvent.VK_RIGHT:
                // Sposta la visuale a destra
                controller.panView(10, 0);
                break;
            case KeyEvent.VK_UP:
                // Sposta la visuale in alto
                controller.panView(0, -10);
                break;
            case KeyEvent.VK_DOWN:
                // Sposta la visuale in basso
                controller.panView(0, 10);
                break;
            case KeyEvent.VK_PLUS:
            case KeyEvent.VK_EQUALS:
                // Aumenta lo zoom
                controller.zoom(1.1f);
                break;
            case KeyEvent.VK_MINUS:
                // Diminuisce lo zoom
                controller.zoom(0.9f);
                break;
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        // Gestito dal GamePanel
    }

    @Override
    public void mousePressed(MouseEvent e) {
        lastMouseX = e.getX();
        lastMouseY = e.getY();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        // Implementazione per il rilascio del mouse
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        // Non necessario
    }

    @Override
    public void mouseExited(MouseEvent e) {
        // Non necessario
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        // Calcola lo spostamento
        int dx = e.getX() - lastMouseX;
        int dy = e.getY() - lastMouseY;

        // Sposta la visuale
        controller.panView(dx, dy);

        // Aggiorna le coordinate
        lastMouseX = e.getX();
        lastMouseY = e.getY();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        // Implementazione per il movimento del mouse
    }
}

