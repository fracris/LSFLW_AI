package it.unical.ai;

import it.unical.model.Fleet;
import it.unical.model.GameState;
import it.unical.model.Player;
import it.unical.model.StarSystem;

import java.util.HashMap;
import java.util.Map;

public class PreviousGameMetrics {
    private int mySystemCount;
    private int enemySystemCount;
    private int neutralSystemCount;
    private int myShipsTotal;
    private int enemyShipsTotal;
    private Map<Integer,Integer> specificEnemySystemCount = new HashMap<>();
    private Map<Integer,Integer> specificEnemyShipsTotal = new HashMap<>();

    // Costruttore con valori predefiniti
    public PreviousGameMetrics() {
        // Valori predefiniti come nel metodo getDefaultPreviousMetrics()
        this.mySystemCount = 1;
        this.enemySystemCount = 1;
        this.neutralSystemCount = 8;
        this.myShipsTotal = 110;
        this.enemyShipsTotal = 110;
    }

    // Costruttore completo
    public PreviousGameMetrics(int mySystemCount, int enemySystemCount, int neutralSystemCount,
                               int myShipsTotal, int enemyShipsTotal, Map<Integer,int[]> specificMetrics) {
        this.mySystemCount = mySystemCount;
        this.enemySystemCount = enemySystemCount;
        this.neutralSystemCount = neutralSystemCount;
        this.myShipsTotal = myShipsTotal;
        this.enemyShipsTotal = enemyShipsTotal;
        for(int key:specificMetrics.keySet()) {
            specificEnemySystemCount.put(key,specificMetrics.get(key)[0]);
            specificEnemyShipsTotal.put(key,specificMetrics.get(key)[1]);
            System.out.print(key + " " + specificEnemySystemCount.get(key) + " " + specificEnemyShipsTotal.get(key));
            System.out.println();
        }
    }

    // Getter e setter
    public int getMySystemCount() {
        return mySystemCount;
    }

    public void setMySystemCount(int mySystemCount) {
        this.mySystemCount = mySystemCount;
    }

    public int getEnemySystemCount() {
        return enemySystemCount;
    }

    public void setEnemySystemCount(int enemySystemCount) {
        this.enemySystemCount = enemySystemCount;
    }

    public int getNeutralSystemCount() {
        return neutralSystemCount;
    }

    public void setNeutralSystemCount(int neutralSystemCount) {
        this.neutralSystemCount = neutralSystemCount;
    }

    public int getMyShipsTotal() {
        return myShipsTotal;
    }

    public void setMyShipsTotal(int myShipsTotal) {
        this.myShipsTotal = myShipsTotal;
    }

    public int getEnemyShipsTotal() {
        return enemyShipsTotal;
    }

    public void setEnemyShipsTotal(int enemyShipsTotal) {
        this.enemyShipsTotal = enemyShipsTotal;
    }

    // Metodo per convertire i dati in formato ASP
    public String toAspFacts() {
        StringBuilder facts = new StringBuilder();
        facts.append("previous_my_system_count(").append(mySystemCount).append(").\n");
        facts.append("previous_enemy_system_count(").append(enemySystemCount).append(").\n");
        facts.append("previous_neutral_system_count(").append(neutralSystemCount).append(").\n");
        facts.append("previous_my_ships_total(").append(myShipsTotal).append(").\n");
        facts.append("previous_enemy_ships_total(").append(enemyShipsTotal).append(").\n");
        for(int i: specificEnemySystemCount.keySet()){
            facts.append("previous_enemy_system_count(").append(i).append(",").append(specificEnemySystemCount.get(i)).append(").\n");
            facts.append("previous_enemy_ships_total(").append(i).append(",").append(specificEnemyShipsTotal.get(i)).append(").\n");
        }
        return facts.toString();
    }

    // Metodo per aggiornare le metriche dal game state attuale
    public void updateFromGameState(GameState gameState, Player player) {
        int mySystemCount = 0;
        int enemySystemCount = 0;
        int neutralSystemCount = 0;
        int myShipsTotal = 0;
        int enemyShipsTotal = 0;

        for (StarSystem system : gameState.getGameMap().getSystems()) {
            if (system.getOwner() == null) {
                neutralSystemCount++;
            } else if (system.getOwner().getId() == player.getId()) {
                mySystemCount++;
                myShipsTotal += system.getShips();
            } else {
                enemySystemCount++;
                enemyShipsTotal += system.getShips();
            }
        }

        // Aggiungi anche le navi nelle flotte
        for (Fleet fleet : gameState.getGameMap().getFleets()) {
            if (fleet.getOwner().getId() == player.getId()) {
                myShipsTotal += fleet.getShips();
            } else {
                enemyShipsTotal += fleet.getShips();
            }
        }

        this.mySystemCount = mySystemCount;
        this.enemySystemCount = enemySystemCount;
        this.neutralSystemCount = neutralSystemCount;
        this.myShipsTotal = myShipsTotal;
        this.enemyShipsTotal = enemyShipsTotal;

        for(Player p:gameState.getPlayers()) {
            if(player.getId()!=p.getId()) {
                System.out.print(p.getName()+ ": ");
                specificEnemySystemCount.put(p.getId(),p.getOwnedSystems().size());
                specificEnemyShipsTotal.put(p.getId(),p.getTotalShips());
                System.out.println("Sistemi: " + specificEnemySystemCount.get(p.getId()) + ", Navi: " + specificEnemyShipsTotal.get(p.getId()));
                enemyShipsTotal += p.getTotalShips();
            }
        }

    }
}
