package it.unical.controller;

import it.unical.gui.GameFrame;
import it.unical.gui.GamePanel;
import it.unical.model.*;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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


    public GameController() {

        // Crea lo stato di gioco
        gameState = new GameState(new Dimension(1600, 900));

        // Crea il controller di input
        inputController = new InputController(this);

        // Inizializza la mappa degli AI players
        aiPlayers = new HashMap<>();

        // Inizializza il timer di gioco
        gameTimer = new Timer(UPDATE_RATE, e -> update());
    }

    public void setGameState(GameState gameState) {
        this.gameState = gameState;
    }

    public GameFrame getGameFrame() {
        return gameFrame;
    }

    // Inizializza e avvia il gioco
    public void initGame() {
        // Inizializza lo stato di gioco
        gameState.initGame(20, 2, true); // 20 sistemi, 2 giocatori, con IA

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
}