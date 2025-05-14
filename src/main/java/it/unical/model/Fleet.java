package it.unical.model;

import java.awt.*;

public class Fleet {
    private int id;
    private Player owner;
    private int ships;
    private StarSystem source;
    private StarSystem destination;
    private double progress; // 0.0 a 1.0, dove 1.0 indica l'arrivo
    private double speed;

    public Fleet(int id, Player owner, int ships, StarSystem source, StarSystem destination, double speed) {
        this.id = id;
        this.owner = owner;
        this.ships = ships;
        this.source = source;
        this.destination = destination;
        this.progress = 0.0;
        this.speed = speed;
    }

    public void setShips(int ships) {
        this.ships = ships;
    }

    // Metodo per aggiornare la posizione della flotta
    public void update(double deltaTime) {
        progress += speed * deltaTime;
        if (progress >= 1.0) {
            progress = 1.0;
        }
    }

    // Metodo per verificare se la flotta è arrivata
    public boolean hasArrived() {
        return progress >= 1.0;
    }

    // Calcola la posizione attuale della flotta (interpolazione)
    public Point getCurrentPosition() {
        int x1 = source.getPosition().x;
        int y1 = source.getPosition().y;
        int x2 = destination.getPosition().x;
        int y2 = destination.getPosition().y;

        int x = (int) (x1 + (x2 - x1) * progress);
        int y = (int) (y1 + (y2 - y1) * progress);

        return new Point(x, y);
    }

    // Getters e setters
    public int getId() { return id; }
    public Player getOwner() { return owner; }
    public Color getColor() { return owner.getColor(); }
    public int getShips() { return ships; }
    public StarSystem getSource() { return source; }
    public StarSystem getDestination() { return destination; }
    public double getProgress() { return progress; }
}