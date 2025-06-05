package it.unical.model;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class Player {
    private int id;
    private String name;
    private Color color;
    private boolean isAI;
    private List<StarSystem> ownedSystems;
    private List<StarSystem> systemsLost;
    private List<Fleet> fleets;

    public Player(int id, String name, Color color, boolean isAI) {
        this.id = id;
        this.name = name;
        this.color = color;
        this.isAI = isAI;
        this.ownedSystems = new ArrayList<>();
        this.fleets = new ArrayList<>();
        this.systemsLost = new ArrayList<>();
    }

    // Metodi per gestire i sistemi posseduti
    public void addSystem(StarSystem system) {
        if (!ownedSystems.contains(system)) {
            ownedSystems.add(system);
            system.setOwner(this);
        }
    }

    public void removeSystem(StarSystem system) {
        ownedSystems.remove(system);
        if (system.getOwner() == this) {
            system.setOwner(null);
        }
    }

    public List<StarSystem> getSystemsLost() {
        return systemsLost;
    }

    public void setSystemsLost(StarSystem system) {
        this.systemsLost.add(system);
    }


    // Metodi per gestire le flotte
    public void addFleet(Fleet fleet) {
        fleets.add(fleet);
    }

    public void removeFleet(Fleet fleet) {
        fleets.remove(fleet);
    }

    // Calcola il totale delle navi del giocatore
    public int getTotalShips() {
        int totalShips = 0;

        // Somma le navi in tutti i sistemi posseduti
        for (StarSystem system : ownedSystems) {
            totalShips += system.getShips();
        }

        // Somma le navi in tutte le flotte
        for (Fleet fleet : fleets) {
            totalShips += fleet.getShips();
        }

        return totalShips;
    }

    // Getters e setters
    public int getId() { return id; }
    public String getName() { return name; }
    public Color getColor() { return color; }
    public boolean isAI() { return isAI; }
    public List<StarSystem> getOwnedSystems() { return ownedSystems; }
    public List<Fleet> getFleets() { return fleets; }
}