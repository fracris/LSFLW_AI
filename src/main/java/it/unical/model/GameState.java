package it.unical.model;

import java.util.ArrayList;
import java.util.List;
import java.awt.Color;
import java.awt.Dimension;

public class GameState {
    private GameMap gameMap;
    private List<Player> players;
    private Player currentPlayer;
    private int currentTurn;
    private boolean gameOver;
    private Player winner;

    public GameState(Dimension mapSize) {
        this.gameMap = new GameMap(mapSize);
        this.players = new ArrayList<>();
        this.currentTurn = 1;
        this.gameOver = false;
    }

    // Inizializza una nuova partita
    public void initGame(int numSystems, int numPlayers, boolean withAI) {
        // Genera la mappa
        gameMap.generateRandomMap(numSystems, 100);

        // Crea i giocatori
        players.clear();
        Color[] playerColors = {Color.BLUE, Color.RED, Color.GREEN, Color.YELLOW};

        for (int i = 0; i < numPlayers; i++) {
            boolean isAI = (i > 0 && withAI); // Solo il primo giocatore è umano se withAI è true
            Player player = new Player(i, "Giocatore " + (i + 1), playerColors[i], isAI);
            players.add(player);
        }

        // Assegna sistemi iniziali
        assignInitialSystems();

        // Imposta il primo giocatore
        currentPlayer = players.get(0);
        currentTurn = 1;
        gameOver = false;
        winner = null;
    }

    // Assegna i sistemi iniziali ai giocatori
    private void assignInitialSystems() {
        List<StarSystem> systems = gameMap.getSystems();
        int numPlayers = players.size();

        // Calcola quanti sistemi per giocatore (almeno 1)
        int systemsPerPlayer = Math.max(1, systems.size() / (numPlayers * 3));

        for (int i = 0; i < numPlayers; i++) {
            Player player = players.get(i);

            // Cerca di prendere sistemi distanti tra loro per ogni giocatore
            int startIdx = (systems.size() / numPlayers) * i;
            for (int j = 0; j < systemsPerPlayer; j++) {
                int idx = (startIdx + j * (systems.size() / (systemsPerPlayer * numPlayers))) % systems.size();
                StarSystem system = systems.get(idx);

                // Assegna il sistema al giocatore
                player.addSystem(system);

                // Aggiungi delle navi iniziali
                system.addShips(10);
            }
        }
    }

    // Passa al prossimo turno
    public void nextTurn() {
        // Aggiorna lo stato del gioco
        updateGameState();

        // Trova il prossimo giocatore
        int currentIndex = players.indexOf(currentPlayer);
        currentIndex = (currentIndex + 1) % players.size();
        currentPlayer = players.get(currentIndex);

        // Se siamo tornati al primo giocatore, incrementa il contatore dei turni
        if (currentIndex == 0) {
            currentTurn++;
        }

        // Se il giocatore corrente è l'IA, esegui il suo turno automaticamente
        if (currentPlayer.isAI()) {
            // TODO: Qui chiameremo il modulo dell'IA (DLV2/EmbASP)
            // Per ora facciamo un comportamento semplice
            playAITurn();
        }
    }

    // Aggiorna lo stato del gioco
    private void updateGameState() {
        // Aggiorna le produzioni dei sistemi
        for (StarSystem system : gameMap.getSystems()) {
            system.produceShips();
        }

        // Controlla se c'è un vincitore
        checkGameOver();
    }

    // Controlla se il gioco è finito
    private void checkGameOver() {
        // Conta quanti giocatori hanno ancora sistemi
        List<Player> activePlayers = new ArrayList<>();

        for (Player player : players) {
            if (!player.getOwnedSystems().isEmpty()) {
                activePlayers.add(player);
            }
        }

        // Se resta un solo giocatore attivo, ha vinto
        if (activePlayers.size() == 1) {
            gameOver = true;
            winner = activePlayers.get(0);
        }
        // TODO: Aggiungi altre condizioni di vittoria se necessario
    }

    // Implementazione semplice di un turno IA (temporaneo, sarà sostituito da DLV2/EmbASP)
    private void playAITurn() {
        for (StarSystem sourceSystem : currentPlayer.getOwnedSystems()) {
            // Se il sistema ha abbastanza navi, attacca un sistema vicino
            if (sourceSystem.getShips() > 10) {
                for (StarSystem targetSystem : sourceSystem.getConnectedSystems()) {
                    // Attacca sistemi nemici o neutrali
                    if (targetSystem.getOwner() != currentPlayer) {
                        int shipsToSend = sourceSystem.getShips() / 2;
                        if (shipsToSend > 0) {
                            sendFleet(sourceSystem, targetSystem, shipsToSend);
                            break;
                        }
                    }
                }
            }
        }

        // Passa al prossimo turno
        nextTurn();
    }

    // Invia una flotta da un sistema a un altro
    public Fleet sendFleet(StarSystem source, StarSystem target, int ships) {
        if (source.getOwner() == currentPlayer && source.getShips() >= ships) {
            // Rimuovi le navi dal sistema di origine
            source.removeShips(ships);

            // Crea una nuova flotta
            int fleetId = gameMap.getFleets().size();
            Fleet fleet = new Fleet(fleetId, currentPlayer, ships, source, target, 0.05);

            // Aggiungi la flotta al giocatore e alla mappa
            currentPlayer.addFleet(fleet);
            gameMap.addFleet(fleet);

            return fleet;
        }
        return null;
    }

    // Getters
    public GameMap getGameMap() { return gameMap; }
    public List<Player> getPlayers() { return players; }
    public Player getCurrentPlayer() { return currentPlayer; }
    public int getCurrentTurn() { return currentTurn; }
    public boolean isGameOver() { return gameOver; }
    public Player getWinner() { return winner; }
}