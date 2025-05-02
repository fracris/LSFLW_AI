package it.unical.model;

import java.awt.*;
import java.util.List;

public class Player {
    private int id;
    private String name;
    private Color color;
    private boolean isAI;
    private List<StarSystem> ownedSystems;
    private List<Fleet> ownedFleets;

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

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public boolean isAI() {
        return isAI;
    }

    public void setAI(boolean AI) {
        isAI = AI;
    }

    public List<StarSystem> getOwnedSystems() {
        return ownedSystems;
    }

    public void setOwnedSystems(List<StarSystem> ownedSystems) {
        this.ownedSystems = ownedSystems;
    }

    public List<Fleet> getOwnedFleets() {
        return ownedFleets;
    }

    public void setOwnedFleets(List<Fleet> ownedFleets) {
        this.ownedFleets = ownedFleets;
    }

    // Metodi per gestire i possedimenti
    public void addSystem(StarSystem system) {  }
    public void removeSystem(StarSystem system) {  }
    public void addFleet(Fleet fleet) {  }
    public void removeFleet(Fleet fleet) {  }

    // Statistiche
    public int getTotalShips() { return 0;  }
    public int getTotalSystems() { return 0; }
}
