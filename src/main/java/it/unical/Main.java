package it.unical;

import it.unical.controller.GameController;
import it.unical.gui.GameFrame;
import it.unical.gui.MainMenuFrame;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Main {
    public static void main(String[] args) {
        // Imposta look & feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Avvia GUI con il menu principale
        SwingUtilities.invokeLater(() -> {
            new MainMenuFrame();
        });
    }

    /**
     * Avvia il gioco con il livello selezionato.
     */
    public static void startGame(String level) {
        SwingUtilities.invokeLater(() -> {
            System.out.println("=== Little Stars for Little Wars ===");
            System.out.println("Livello scelto: " + level);
            System.out.println("Avvio del gioco...");

            GameController gameController = new GameController(level);
            gameController.initGame();

            GameFrame gameFrame = new GameFrame("Little Stars for Little Wars - " + level, gameController);
            gameFrame.getGamePanel().updateSystemViews();
            // GameFrame già rende visibile nel setGameFrame
        });
    }
}
