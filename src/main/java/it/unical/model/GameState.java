package it.unical.model;

import java.util.List;

public class GameState {
    private GameMap map;
    private List<Player> players;
    private Player currentPlayer;
    private int turn;
    private boolean gameOver;



    public GameMap getMap() {
        return map;
    }

    public void setMap(GameMap map) {
        this.map = map;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public void setPlayers(List<Player> players) {
        this.players = players;
    }

    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    public void setCurrentPlayer(Player currentPlayer) {
        this.currentPlayer = currentPlayer;
    }

    public int getTurn() {
        return turn;
    }

    public void setTurn(int turn) {
        this.turn = turn;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public void setGameOver(boolean gameOver) {
        this.gameOver = gameOver;
    }

    // Costruttori, getter e setter

    // Gestione del turno
    public void nextTurn() {  }

    // Verifiche di condizioni di vittoria
    public boolean checkGameOver() { return true; }
    public Player getWinner() { return currentPlayer; }

    // Aggiornamento dello stato
    public void update() {
        // Aggiorna le posizioni delle flotte
        // Risolve le battaglie
        // Aggiorna la produzione dei sistemi
    }

    public void initialize() {
    }

    public void addFleet(Fleet newFleet) {
    }

    public List<StarSystem> getSystemsOwnedBy(Player currentPlayer) {
    return null;}
}