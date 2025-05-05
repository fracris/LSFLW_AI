package it.unical.model;

import java.awt.Point;
import java.awt.Color;
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
    private Color color;

    // Costruttore
    public StarSystem(int id, String name, Point position, int productionRate) {
        this.id = id;
        this.name = name;
        this.position = position;
        this.ships = 0;
        this.owner = null;
        this.productionRate = productionRate;
        this.connectedSystems = new ArrayList<>();
        this.color = Color.GRAY; // Sistema neutrale
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

    public boolean removeShips(int amount) {
        if (ships >= amount) {
            ships -= amount;
            return true;
        }
        return false;
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
        if (newOwner != null) {
            this.color = newOwner.getColor();
        } else {
            this.color = Color.GRAY;
        }
    }

    // Getters e setters
    public int getId() { return id; }
    public String getName() { return name; }
    public Point getPosition() { return position; }
    public int getShips() { return ships; }
    public Player getOwner() { return owner; }
    public List<StarSystem> getConnectedSystems() { return connectedSystems; }
    public Color getColor() { return color; }
    public int getProductionRate() { return productionRate; }

    public void setShips(int i) {
        ships = i;
    }
}