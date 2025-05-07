package it.unical.model;

import java.util.ArrayList;
import java.util.List;
import java.awt.Color;
import java.awt.Dimension;

public class GameState {
    private GameMap gameMap;
    private List<Player> players;
    private boolean gameOver;
    private Player winner;
    private static int count=0;

    public GameState(Dimension mapSize) {
        this.gameMap   = new GameMap(mapSize);
        this.players   = new ArrayList<>();
        this.gameOver  = false;
        this.winner    = null;
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

        // Reset stato di vittoria
        gameOver = false;
        winner = null;
    }

    /** Restituisce il giocatore umano (assumiamo sia sempre il primo in lista) */
    public Player getHumanPlayer() {
        for (Player p : players) {
            if (!p.isAI()) return p;
        }
        return null;
    }
    // Assegna i sistemi iniziali ai giocatori
    private void assignInitialSystems() {
        List<StarSystem> systems = gameMap.getSystems();
        int numPlayers = players.size();

        int systemsPerPlayer = 1;

        for (int i = 0; i < numPlayers; i++) {
            Player player = players.get(i);

            // Cerca di prendere sistemi distanti tra loro per ogni giocatore
            int startIdx = (systems.size() / numPlayers) * i;
            for (int j = 0; j < systemsPerPlayer; j++) {
                int idx = startIdx % systems.size();
                StarSystem system = systems.get(idx);

                // Assegna il sistema al giocatore
                player.addSystem(system);

                // Aggiungi delle navi iniziali
                system.addShips(10);
            }
        }
    }

    /** Restituisce tutti i giocatori IA */
    public List<Player> getAiPlayers() {
        List<Player> ais = new ArrayList<>();
        for (Player p : players) {
            if (p.isAI()) ais.add(p);
        }
        return ais;
    }

    /** Chiamalo ad ogni tick per produrre navi e controllare game over */
    public void updateGameState() {
        // Produzione di tutte le navi
        if(count%10==0) {
            for (StarSystem sys : gameMap.getSystems()) {
                sys.produceShips();
            }
        }

        // Controlla se c'è un vincitore
        checkGameOver();
        count++;
    }

    // Controlla se il gioco è finito
    private void checkGameOver() {
        List<Player> active = new ArrayList<>();
        for (Player p : players) {
            if (!p.getOwnedSystems().isEmpty()) {
                active.add(p);
            }
        }
        if (active.size() == 1) {
            gameOver = true;
            winner   = active.get(0);
        }
    }

    /**
     * Invia una flotta da source a target.
     * Non dipende più da "currentPlayer": chiama direttamente sendFleet su un giocatore.
     */
    public Fleet sendFleet(Player who, StarSystem source, StarSystem target, int ships) {
        if (source.getOwner() == who && source.getShips() >= ships) {
            source.removeShips(ships);
            int fleetId = gameMap.getFleets().size();
            Fleet f = new Fleet(fleetId, who, ships, source, target, 0.05);
            who.addFleet(f);
            gameMap.addFleet(f);
            return f;
        }
        return null;
    }

    // --- getters ---
    public GameMap getGameMap()    { return gameMap; }
    public List<Player> getPlayers(){ return players; }
    public boolean isGameOver()    { return gameOver; }
    public Player getWinner()      { return winner; }

}