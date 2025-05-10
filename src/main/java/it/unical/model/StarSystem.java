package it.unical.model;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public class StarSystem {
    private int id;
    private String name;
    private Point position;
    private int ships;
    private Player owner;
    private int productionRate;
    private List<StarSystem> connectedSystems;
    private boolean automated=false;
    private StarSystem automatedTo=null;
    private int sendMode=100;

    public StarSystem getAutomatedTo() {
        return automatedTo;
    }

    public void setAutomatedTo(StarSystem automatedTo) {
        this.automatedTo = automatedTo;
    }

    public boolean isAutomated() {
        return automated;
    }

    public void setAutomated(boolean automated) {
        this.automated = automated;
    }

    public int getSendMode() {
        return sendMode;
    }

    public void setSendMode(int sendMode) {
        this.sendMode = sendMode;
    }

    public StarSystem(int id, String name, Point position, int productionRate) {
        this.id = id;
        this.name = name;
        this.position = position;
        this.ships = 0;
        this.owner = null;
        this.productionRate = productionRate;
        this.connectedSystems = new ArrayList<>();
    }

    // Metodi per connettere i sistemi stellari (creazione del grafo)
    public void connectTo(StarSystem other) {
        if (!connectedSystems.contains(other)) {
            connectedSystems.add(other);
            other.connectTo(this);
        }
    }

    // Metodi per la gestione delle flotte e delle navi
    public void addShips(int amount) {
        ships += amount;
    }

    public void removeShips(int amount) {
        if (ships >= amount) {
            ships -= amount;
        }
    }

    // Metodo per la produzione di navi per turno
    public void produceShips() {
        if (owner != null) {
            ships += productionRate;
        }
    }

    // Metodo per cambiare proprietario
    public void setOwner(Player newOwner) {
        this.owner = newOwner;
    }

    // Getters e setters
    public int getId() { return id; }
    public String getName() { return name; }
    public Point getPosition() { return position; }
    public int getShips() { return ships; }
    public Player getOwner() { return owner; }
    public List<StarSystem> getConnectedSystems() { return connectedSystems; }
    public int getProductionRate() { return productionRate; }

    public void setShips(int i) {
        ships = i;
    }
}