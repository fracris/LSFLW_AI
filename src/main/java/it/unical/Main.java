package it.unical;

import it.unical.controller.GameController;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Main {
    public static void main(String[] args) {
        // Imposta il look and feel del sistema operativo
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Avvia il gioco nell'Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            GameController gameController = new GameController();
            gameController.initGame();
        });
    }
}