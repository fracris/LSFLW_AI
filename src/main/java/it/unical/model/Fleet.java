package it.unical.model;

public class Fleet {
    private int id;
    private int ships;
    private Player owner;
    private StarSystem source;
    private StarSystem destination;
    private int turnsToArrival;

    public Fleet(int shipsToSend, Player currentPlayer, StarSystem source, StarSystem target) {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public StarSystem getSource() {
        return source;
    }

    public void setSource(StarSystem source) {
        this.source = source;
    }

    public StarSystem getDestination() {
        return destination;
    }

    public void setDestination(StarSystem destination) {
        this.destination = destination;
    }

    public int getTurnsToArrival() {
        return turnsToArrival;
    }

    public void setTurnsToArrival(int turnsToArrival) {
        this.turnsToArrival = turnsToArrival;
    }

    // Metodi per gestire il movimento
    public void move() {
        this.turnsToArrival--;
    }

    public boolean hasArrived() {
        return turnsToArrival <= 0;
    }
}
