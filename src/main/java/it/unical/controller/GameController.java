package it.unical.controller;

import it.unical.gui.GameFrame;
import it.unical.gui.GamePanel;
import it.unical.model.*;

import java.awt.Dimension;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.Timer;

public class GameController {

    private GameState gameState;
    private GameFrame gameFrame;
    private InputController inputController;
    private Timer gameTimer;
    private Map<Player, AIPlayer> aiPlayers;
    private final int UPDATE_RATE = 10;
    private int tickCounter = 0;
    private int sendPerc = 100;
    private String difficulty;

    public GameController(String difficulty) {
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
        int numSystem = switch (difficulty.toLowerCase()) {
            case "facile" -> 10;
            case "medio" -> 20;
            default -> 30;
        };

        // Inizializza lo stato di gioco con i parametri del livello
        gameState.initGame(difficulty.toLowerCase(), numSystem, withAI);

        // Crea gli AIPlayer per ogni giocatore IA
        List<Player> aiPlayers = gameState.getAiPlayers();
        for (Player aiPlayer : aiPlayers) {
            this.aiPlayers.put(aiPlayer, new AIPlayer(aiPlayer, gameState));
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

        // Aggiorna lo stato del gioco
        gameState.updateGameState();

        // Aggiorna le flotte (movimento)
        gameState.getGameMap().updateFleets(0.3);

        if (tickCounter % 200 == 0) {
            handleAIPlayers();
        }

        // Aggiorna l'interfaccia grafica
        if (gameFrame != null) {
            gameFrame.updateUI();
        }

        // Controlla se il gioco è finito
        if (gameState.isGameOver()) {
            gameTimer.stop();
            System.out.println("Game Over! Il vincitore è: " + gameState.getWinner().getName());
        }
    }

    private void handleAIPlayers() {
        // Esegui il ragionamento per ogni IA
        for (AIPlayer ai : aiPlayers.values()) {
            try {
                ai.performTurn();
            } catch (Exception e) {
                System.err.println("Errore durante il turno dell'IA: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    // Invia una flotta da un sistema a un altro (per il giocatore umano)
    public void sendFleet(StarSystem source, StarSystem target, int ships) {
        Player humanPlayer = gameState.getHumanPlayer();
        gameState.sendFleet(humanPlayer, source, target, ships);
    }

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

    public String getDifficulty() {
        return difficulty;
    }
}
