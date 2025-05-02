package it.unical.model;

import java.util.List;

public class GameMap {
    private List<StarSystem> systems;
    private List<Fleet> fleets;
    private int width;
    private int height;

    // Costruttori, getter e setter

    // Metodi per generare una mappa casuale
    public void generateRandomMap(int numSystems, int density) {  }

    // Metodi per trovare percorsi
    public List<StarSystem> findPath(StarSystem start, StarSystem end) { return systems; }

    // Metodi di utilità
    public StarSystem getSystemAt(int x, int y) { return systems.get(0); }
    public List<StarSystem> getNeighbors(StarSystem system) { return systems; }
}