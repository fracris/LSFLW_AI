package it.unical.model;

import java.util.ArrayList;
import java.util.List;
import java.awt.Color;
import java.awt.Dimension;


public class GameState {
    private final GameMap gameMap;
    private final List<Player> players;
    private boolean gameOver;
    private Player winner;
    private static int count=0;

    public GameState(Dimension mapSize) {
        this.gameMap   = new GameMap(mapSize);
        this.players   = new ArrayList<>();
        this.gameOver  = false;
        this.winner    = null;
    }

    private void checkGameOver() {
        List<Player> active = new ArrayList<>();
        for (Player p : players) {
            if (!p.getOwnedSystems().isEmpty() || !p.getFleets().isEmpty()) {
                active.add(p);
            }
            if (!p.isAI() && p.getOwnedSystems().isEmpty() && p.getFleets().isEmpty()) {
                gameOver = true;
                return;
            }
        }
        if (active.size() == 1) {
            gameOver = true;
            winner   = active.get(0);
        }
    }

    public void initGame(Difficulty difficulty, int numSystem, boolean withAI) {
        gameMap.generateMap(numSystem, difficulty);
        Color[] playerColors;
        players.clear();
        int numPlayers;

        if(difficulty instanceof Difficulty.Easy){
            numPlayers=2;
            playerColors= new Color[]{Color.BLUE, Color.RED};
        }
        else if(difficulty instanceof Difficulty.Medium){
            numPlayers=3;
            playerColors= new Color[]{Color.BLUE, Color.RED, Color.GREEN};
        }
        else {
            numPlayers=4;
            playerColors= new Color[]{Color.BLUE, Color.RED, Color.GREEN, Color.YELLOW};
        }

        for (int i = 0; i < numPlayers; i++) {
            boolean isAI = (i > 0 && withAI);
            Player player = new Player(i, "Giocatore " + (i + 1), playerColors[i], isAI);
            players.add(player);
        }

        assignInitialSystems(difficulty);

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

    private void assignInitialSystems(Difficulty difficulty) {
        List<StarSystem> systems = gameMap.getSystems();
        int numPlayers = players.size();

        players.get(0).addSystem(systems.get(0));
        players.get(1).addSystem(systems.get(systems.size() - 1));
        if (numPlayers > 2) {
            if(difficulty instanceof Difficulty.Medium){
                players.get(2).addSystem(systems.get(17));
            } else {
                players.get(2).addSystem(systems.get(1));
            }
        }
        if (numPlayers > 3) {
            players.get(3).addSystem(systems.get(28));
        }
    }

    public List<Player> getAiPlayers() {
        List<Player> ais = new ArrayList<>();
        for (Player p : players) {
            if (p.isAI()) ais.add(p);
        }
        return ais;
    }

    public void updateGameState() {
        if(count%10==0) {
            for (StarSystem sys : gameMap.getSystems()) {
                sys.produceShips();
            }
        }

        checkGameOver();
        count++;
    }

    private int fleetId = 0;

    public Fleet sendFleet(Player who, StarSystem source, StarSystem target, int ships) {
        if (source.getOwner() == who && source.getShips() >= ships) {
            source.removeShips(ships);
            fleetId++;
            Fleet f = new Fleet(fleetId, who, ships, source, target, 0.02);
            who.addFleet(f);
            gameMap.addFleet(f);
            return f;
        }
        return null;
    }

    public GameMap getGameMap()    { return gameMap; }
    public List<Player> getPlayers(){ return players; }
    public boolean isGameOver()    { return gameOver; }
    public Player getWinner()      { return winner; }

}
