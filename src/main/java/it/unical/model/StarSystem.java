package it.unical.model;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public class StarSystem {
    private final int id;
    private final String name;
    private final Point position;
    private int ships;
    private Player owner;
    private final int productionRate;
    private final List<StarSystem> connectedSystems;
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

    public void connectTo(StarSystem other) {
        if (!connectedSystems.contains(other)) {
            connectedSystems.add(other);
            other.connectTo(this);
        }
    }

    public void addShips(int amount) {
        ships += amount;
    }

    public void removeShips(int amount) {
        if (ships >= amount) {
            ships -= amount;
        }
    }

    public void produceShips() {
        if (owner != null) {
            ships += productionRate;
        }
    }

    public void setOwner(Player newOwner) {
        this.owner = newOwner;
    }

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