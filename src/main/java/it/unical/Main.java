package it.unical;


import it.unical.controller.GameController;
import it.unical.gui.GameFrame;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GameController gameWindow = new GameController();
            gameWindow.startGame();
        });
    }
}
