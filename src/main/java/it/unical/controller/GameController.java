package it.unical.controller;

import it.unical.gui.GameFrame;
import it.unical.gui.GamePanel;
import it.unical.model.Fleet;
import it.unical.model.GameState;
import it.unical.model.StarSystem;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Timer;

public class GameController {
    private GameState gameState;
    private GameFrame gameFrame;
    private InputController inputController;
    private Timer gameTimer;
    private final int UPDATE_RATE = 10; // Milliseconds (1 secondo)

    public GameController() {
        // Crea lo stato di gioco
        gameState = new GameState(new Dimension(1600, 900));

        // Crea il controller di input
        inputController = new InputController(this);

        // Inizializza il timer di gioco
        gameTimer = new Timer(UPDATE_RATE, e -> update());
    }

    public GameFrame getGameFrame() {
        return gameFrame;
    }

    public void setGameFrame(GameFrame gameFrame) {
        this.gameFrame = gameFrame;
    }

    // Inizializza e avvia il gioco
    public void initGame() {
        // Inizializza lo stato di gioco
        gameState.initGame(20, 2, true); // 20 sistemi, 2 giocatori, con IA

        // Crea e mostra la finestra di gioco
        gameFrame = new GameFrame("Little Stars for Little Wars", this);
        gameFrame.setVisible(true);

        // Avvia il timer di gioco
        gameTimer.start();
    }

    // Aggiorna lo stato del gioco (chiamato ogni tick del timer)
    private void update() {
        // Aggiorna le flotte
        gameState.getGameMap().updateFleets(1.0); // 1.0 = 1 secondo

        // Aggiorna l'interfaccia grafica
        if (gameFrame != null) {
            gameFrame.updateUI();
        }

        // Controlla se è il turno dell'IA e falla giocare automaticamente
        if (gameState.getCurrentPlayer().isAI()) {
            // Note: In realtà qui chiameremmo EmbASP/DLV2
            // Ma per ora semplicemente passiamo al prossimo turno dopo un breve ritardo
            nextTurn();
        }
    }

    // Invia una flotta da un sistema a un altro
    public Fleet sendFleet(StarSystem source, StarSystem target, int ships) {
        return gameState.sendFleet(source, target, ships);
    }

    // Passa al prossimo turno
    public void nextTurn() {
        gameState.nextTurn();
    }

    // Getters
    public GameState getGameState() {
        return gameState;
    }

    public GamePanel getGamePanel() {
        return gameFrame.getGamePanel();
    }

    public InputController getInputController() {
        return inputController;
    }
}