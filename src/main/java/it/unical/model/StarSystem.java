package it.unical.model;

import javax.swing.text.Position;
import java.util.List;

public class StarSystem {
    private int id;
    private String name;
    private Position position;
    private int ships;
    private Player owner;
    private List<StarSystem> connectedSystems;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public int getShips() {
        return ships;
    }

    public void setShips(int ships) {
        this.ships = ships;
    }

    public Player getOwner() {
        return owner;
    }

    public void setOwner(Player owner) {
        this.owner = owner;
    }

    public List<StarSystem> getConnectedSystems() {
        return connectedSystems;
    }

    public void setConnectedSystems(List<StarSystem> connectedSystems) {
        this.connectedSystems = connectedSystems;
    }

    // Metodi per gestire la connessione ad altri sistemi
    public void connectTo(StarSystem other) {  }

    // Metodi per gestire le flotte e le battaglie
    public void addShips(int amount) {  }
    public void removeShips(int amount) {  }
    public boolean battle(Fleet incomingFleet) { return false; }
}