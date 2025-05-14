package it.unical.controller;

import it.unical.ai.AIPlayer;
import it.unical.gui.GameFrame;
import it.unical.gui.GamePanel;
import it.unical.model.*;

import java.awt.Dimension;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.*;

public class GameController {

    private GameState gameState;
    private GameFrame gameFrame;
    private InputController inputController;
    private Timer gameTimer;
    private Map<Player, AIPlayer> aiPlayers;
    private final int UPDATE_RATE = 10;
    private int tickCounter = 0;
    private int playerTurn = 0;
    private int sendPerc = 100;
    private Difficulty difficulty;


    public GameController(Difficulty difficulty) {
        this.difficulty = difficulty;

        // Crea lo stato di gioco
        gameState = new GameState(new Dimension(1600, 900));

        // Crea il controller di input
        inputController = new InputController(this);

        // Inizializza la mappa degli AI players
        aiPlayers = new HashMap<>();

        // Inizializza il timer di gioco
        gameTimer = new Timer(UPDATE_RATE, e -> update());
    }


    public void showPauseDialog() {
        // Ferma il game loop
        gameTimer.stop();
        paused = true;

        // Opzioni del dialog
        String[] options = {"Riprendi", "Menu Principale"};

        // Popup modale FlatLaf
        int choice = JOptionPane.showOptionDialog(
                gameFrame,                                // parent component
                "Il gioco è in pausa",                    // message
                "Pausa",                                  // title
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,                                     // icona di default
                options,                                  // labels dei pulsanti
                options[0]                                // default: “Riprendi”
        );

        if (choice == 0) {
            // Riprendi
            gameTimer.start();
            paused = false;
        } else {
            // Torna al menu principale
            backToMainMenu();
        }
    }

    // Rimuovi togglePause(), oppure rimappalo a showPauseDialog()
    public void togglePause() {
        if (paused) {
            // se vuoi mantenere un semplice toggle…
            gameTimer.start();
            paused = false;
        } else {
            showPauseDialog();
        }
    }



    // Modifica il metodo sendFleet se necessario per assicurarti che rimanga sempre almeno una nave
    public void sendFleet(StarSystem source, StarSystem target, int ships) {
        // Assicurati di mantenere almeno una nave nel sistema di origine
        int shipsToSend = Math.min(ships, source.getShips() - 1);

        if (shipsToSend > 0) {
            Player humanPlayer = gameState.getHumanPlayer();
            gameState.sendFleet(humanPlayer, source, target, shipsToSend);
        }
    }

    private boolean paused = false;



    public void backToMainMenu() {
        // Ferma il timer (se non già fermo)
        gameTimer.stop();
        // Chiudi la finestra di gioco (GameFrame)
        if (gameFrame != null) gameFrame.dispose();
        // Apri il menu principale
        SwingUtilities.invokeLater(() -> new it.unical.gui.MainMenuFrame());
    }


    public int getSendPerc() {
        return sendPerc;
    }

    public void setSendPerc(int sendPerc) {
        this.sendPerc = sendPerc;
    }

    public void setGameState(GameState gameState) {
        this.gameState = gameState;
    }

    public GameFrame getGameFrame() {
        return gameFrame;
    }

    // Inizializza e avvia il gioco
    public void initGame() {
        boolean withAI = true;

        int numSystem;
        if (difficulty instanceof Difficulty.Easy) {
            numSystem = 10;
        }
        else if (difficulty instanceof Difficulty.Medium) {
            numSystem = 20;
        }
        else /* must be Hard */ {
            numSystem = 30;
        }



        // Inizializza lo stato di gioco con i parametri del livello
        gameState.initGame(difficulty, numSystem, withAI);

        // Crea gli AIPlayer per ogni giocatore IA
        List<Player> aiPlayers = gameState.getAiPlayers();
        for (Player aiPlayer : aiPlayers) {
            this.aiPlayers.put(aiPlayer, new AIPlayer(aiPlayer, gameState, difficulty));
        }

        // IMPORTANTE: Aggiorna le viste dei sistemi se GameFrame è già stato creato
        if (gameFrame != null && gameFrame.getGamePanel() != null) {
            gameFrame.getGamePanel().updateSystemViews();
            System.out.println("Aggiornate le viste dei sistemi: " +
                    gameState.getGameMap().getSystems().size() + " sistemi trovati");
        }

        // Avvia il timer di gioco
        gameTimer.start();
    }

    public void setGameFrame(GameFrame gameFrame) {
        this.gameFrame = gameFrame;
        if (this.gameFrame != null) {
            this.gameFrame.setVisible(true);
        }
    }

    private void update() {
        // Incrementa il contatore di tick
        tickCounter++;

        // Aggiorna lo stato del gioco e le flotte
        gameState.updateGameState();
        gameState.getGameMap().updateFleets(0.3);

        if (tickCounter % (200/(gameState.getPlayers().size()-1)) == 0) {
            handleAIPlayers(aiPlayers.get(gameState.getPlayers().get(playerTurn+1)));
            playerTurn = (playerTurn+1)%(gameState.getPlayers().size()-1);
        }

        // Aggiorna l'interfaccia grafica
        if (gameFrame != null) {
            gameFrame.updateUI();
        }

        // Game Over!
        if (gameState.isGameOver()) {
            gameTimer.stop();
            String winner = gameState.getWinner().getName();
            System.out.println("Game Over! Il vincitore è: " + winner);

            // Mostra dialog e torna al menu principale
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(
                        gameFrame,
                        "Game Over! Ha vinto: " + winner,
                        "Game Over",
                        JOptionPane.INFORMATION_MESSAGE
                );
                // Chiude la finestra di gioco
                gameFrame.dispose();
                // Riapre il menu principale
                new it.unical.gui.MainMenuFrame();
            });
        }
    }

    private void handleAIPlayers(AIPlayer i) {
        // Esegui il ragionamento per ogni IA
        try {
            i.performTurn();
        } catch (Exception e) {
            System.err.println("Errore durante il turno dell'IA: " + e.getMessage());
            e.printStackTrace();
        }
    }

//    // Invia una flotta da un sistema a un altro (per il giocatore umano)
//    public void sendFleet(StarSystem source, StarSystem target, int ships) {
//        Player humanPlayer = gameState.getHumanPlayer();
//        gameState.sendFleet(humanPlayer, source, target, ships);
//    }

    // Getters
    public GameState getGameState() {
        return gameState;
    }

    public GamePanel getGamePanel() {
        if (gameFrame != null) {
            return gameFrame.getGamePanel();
        }
        return null;
    }

    public InputController getInputController() {
        return inputController;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }
}
