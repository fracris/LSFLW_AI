package it.unical;

import it.unical.controller.GameController;
import it.unical.gui.GameFrame;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.io.File;

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
            try {
                System.out.println("=== Little Stars for Little Wars ===");
                System.out.println("Avvio del gioco...");

                // IMPORTANTE: Cambia l'ordine di inizializzazione
                // 1. Crea il controller di gioco
                GameController gameController = new GameController();

                // 2. Inizializza il gioco (genera la mappa)
                System.out.println("Inizializzazione del gioco...");
                gameController.initGame();

                // 3. Crea la finestra di gioco (dopo aver inizializzato il gioco)
                System.out.println("Creazione dell'interfaccia grafica...");
                GameFrame gameFrame = new GameFrame("Little Stars for Little Wars", gameController);

                // 4. Aggiorna le viste dei sistemi
                gameFrame.getGamePanel().updateSystemViews();

                // 5. Mostra la finestra
                gameFrame.setVisible(true);
                System.out.println("Gioco avviato con successo!");

            } catch (Exception e) {
                System.err.println("Errore nell'avvio del gioco: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
}
