package it.unical.model;

import java.awt.Dimension;
import java.awt.Point;
import java.util.*;

public class GameMap {
    private List<StarSystem> systems;
    private List<Fleet> fleets;
    private Dimension mapSize;

    public GameMap(Dimension mapSize) {
        this.systems = new ArrayList<>();
        this.fleets = new ArrayList<>();
        this.mapSize = mapSize;
    }

    // Crea una mappa casuale con un dato numero di sistemi
    public void generateMap(int numSystem, String difficulty) {
        systems.clear();

        StarSystem newSystem;
        Point[] positions;
        int[] productionRates;

        if (difficulty.equals("facile")) {
            positions = new Point[]{
                    new Point(200, (mapSize.height - 20) / 2),
                    new Point(300, (mapSize.height - 20) / 2 - 100),
                    new Point(300, (mapSize.height - 20) / 2 + 100),
                    new Point(320, (mapSize.height - 20) / 2),
                    new Point(400, (mapSize.height - 20) / 2 - 100),
                    new Point(400, (mapSize.height - 20) / 2 + 100),
                    new Point(480, (mapSize.height - 20) / 2),
                    new Point(500, (mapSize.height - 20) / 2 - 100),
                    new Point(500, (mapSize.height - 20) / 2 + 100),
                    new Point(600, (mapSize.height - 20) / 2),
            };
            productionRates= new int[]{2, 1, 1, 3, 2, 2, 3, 1, 1, 1};
        } else if (difficulty.equals("medio")) {
            positions = new Point[]{
                    new Point(200, (mapSize.height - 20) / 2),
                    new Point(200, (mapSize.height - 20) / 2 - 200),
                    new Point(200, (mapSize.height - 20) / 2 + 100),
                    new Point(300, (mapSize.height - 20) / 2 - 100),
                    new Point(300, (mapSize.height - 20) / 2),
                    new Point(300, (mapSize.height - 20) / 2 + 100),
                    new Point(400, (mapSize.height - 20) / 2 - 200),
                    new Point(400, (mapSize.height - 20) / 2 - 100),
                    new Point(400, (mapSize.height - 20) / 2),
                    new Point(400, (mapSize.height - 20) / 2 + 100),
                    new Point(500, (mapSize.height - 20) / 2 - 200),
                    new Point(500, (mapSize.height - 20) / 2 - 100),
                    new Point(500, (mapSize.height - 20) / 2),
                    new Point(500, (mapSize.height - 20) / 2 + 100),
                    new Point(600, (mapSize.height - 20) / 2 - 200),
                    new Point(600, (mapSize.height - 20) / 2 - 100),
                    new Point(600, (mapSize.height - 20) / 2),
                    new Point(700, (mapSize.height - 20) / 2 - 200),
                    new Point(700, (mapSize.height - 20) / 2),
                    new Point(700, (mapSize.height - 20) / 2 + 100),
            };
            productionRates= new int[]{2,1,1,2,1,1,2,1,1,2,1,1,1,1,1,1,1,1,2,1};
        } else {
            positions = new Point[]{
                    new Point(100, 100), new Point(100, 500),

                    new Point(200, 150), new Point(200, 250), new Point(200, 350), new Point(200, 450),
                    new Point(300, 150), new Point(300, 250), new Point(300, 350), new Point(300, 450),
                    new Point(350, 200), new Point(350, 300), new Point(350, 400),
                    new Point(400, 150), new Point(400, 250), new Point(400, 350), new Point(400, 450),
                    new Point(450, 200), new Point(450, 300), new Point(450, 400),
                    new Point(500, 150), new Point(500, 250), new Point(500, 350), new Point(500, 450),
                    new Point(600, 150), new Point(600, 250), new Point(600, 350), new Point(600, 450),
                    new Point(700, 100), new Point(700, 500),
            };
            productionRates = new int[]{
                    3, 3, 1, 1, 1,
                    1, 1, 2, 2, 1,
                    2, 5, 2, 1, 5,
                    5, 1, 2, 5, 2,
                    1, 2, 2, 1, 1,
                    1, 1, 1, 3, 3
            };
        }

        for(int i = 0; i < numSystem; i++) {
            newSystem = new StarSystem(i, "Sistema " + i, positions[i], productionRates[i]);
            systems.add(newSystem);
        }

        createConnections(difficulty);
    }

    // Crea connessioni tra i sistemi stellari
    private void createConnections(String difficulty) {
        if (difficulty.equals("facile")) {
            systems.get(0).connectTo(systems.get(1));
            systems.get(0).connectTo(systems.get(2));
            systems.get(0).connectTo(systems.get(3));
            systems.get(1).connectTo(systems.get(4));
            systems.get(2).connectTo(systems.get(5));
            systems.get(3).connectTo(systems.get(6));
            systems.get(4).connectTo(systems.get(7));
            systems.get(5).connectTo(systems.get(8));
            systems.get(6).connectTo(systems.get(9));
            systems.get(7).connectTo(systems.get(9));
            systems.get(8).connectTo(systems.get(9));
        } else if (difficulty.equals("medio")) {
            systems.get(0).connectTo(systems.get(1));
            systems.get(0).connectTo(systems.get(3));
            systems.get(1).connectTo(systems.get(3));
            systems.get(2).connectTo(systems.get(4));
            systems.get(3).connectTo(systems.get(4));
            systems.get(3).connectTo(systems.get(6));
            systems.get(4).connectTo(systems.get(5));
            systems.get(4).connectTo(systems.get(7));
            systems.get(4).connectTo(systems.get(9));
            systems.get(5).connectTo(systems.get(9));
            systems.get(6).connectTo(systems.get(7));
            systems.get(7).connectTo(systems.get(10));
            systems.get(8).connectTo(systems.get(11));
            systems.get(8).connectTo(systems.get(12));
            systems.get(8).connectTo(systems.get(13));
            systems.get(9).connectTo(systems.get(13));
            systems.get(10).connectTo(systems.get(11));
            systems.get(10).connectTo(systems.get(14));
            systems.get(11).connectTo(systems.get(15));
            systems.get(12).connectTo(systems.get(16));
            systems.get(13).connectTo(systems.get(16));
            systems.get(13).connectTo(systems.get(19));
            systems.get(14).connectTo(systems.get(17));
            systems.get(15).connectTo(systems.get(16));
            systems.get(15).connectTo(systems.get(17));
            systems.get(15).connectTo(systems.get(18));
            systems.get(16).connectTo(systems.get(18));
            systems.get(16).connectTo(systems.get(19));
        } else {
            systems.get(0).connectTo(systems.get(2));
            systems.get(1).connectTo(systems.get(5));
            systems.get(2).connectTo(systems.get(3)); systems.get(2).connectTo(systems.get(6));
            systems.get(3).connectTo(systems.get(4)); systems.get(3).connectTo(systems.get(7));
            systems.get(4).connectTo(systems.get(5)); systems.get(4).connectTo(systems.get(8));
            systems.get(5).connectTo(systems.get(9));
            systems.get(6).connectTo(systems.get(7)); systems.get(6).connectTo(systems.get(10)); systems.get(6).connectTo(systems.get(13));
            systems.get(7).connectTo(systems.get(8)); systems.get(7).connectTo(systems.get(10)); systems.get(7).connectTo(systems.get(11));
            systems.get(8).connectTo(systems.get(9)); systems.get(8).connectTo(systems.get(11)); systems.get(8).connectTo(systems.get(12));
            systems.get(9).connectTo(systems.get(12)); systems.get(9).connectTo(systems.get(16));
            systems.get(10).connectTo(systems.get(13)); systems.get(10).connectTo(systems.get(14));
            systems.get(11).connectTo(systems.get(14)); systems.get(11).connectTo(systems.get(15));
            systems.get(12).connectTo(systems.get(15)); systems.get(12).connectTo(systems.get(16));
            systems.get(13).connectTo(systems.get(17)); systems.get(13).connectTo(systems.get(20));
            systems.get(14).connectTo(systems.get(17)); systems.get(14).connectTo(systems.get(18));
            systems.get(15).connectTo(systems.get(18)); systems.get(15).connectTo(systems.get(19));
            systems.get(16).connectTo(systems.get(19)); systems.get(16).connectTo(systems.get(23));
            systems.get(17).connectTo(systems.get(20)); systems.get(17).connectTo(systems.get(21));
            systems.get(18).connectTo(systems.get(21)); systems.get(18).connectTo(systems.get(22));
            systems.get(19).connectTo(systems.get(22)); systems.get(19).connectTo(systems.get(23));
            systems.get(20).connectTo(systems.get(21)); systems.get(20).connectTo(systems.get(24));
            systems.get(21).connectTo(systems.get(22)); systems.get(21).connectTo(systems.get(25));
            systems.get(22).connectTo(systems.get(23)); systems.get(22).connectTo(systems.get(26));
            systems.get(23).connectTo(systems.get(27));
            systems.get(24).connectTo(systems.get(25)); systems.get(24).connectTo(systems.get(28));
            systems.get(25).connectTo(systems.get(26));
            systems.get(26).connectTo(systems.get(27));
            systems.get(27).connectTo(systems.get(29));
        }
    }


    // Aggiunge una flotta alla mappa
    public void addFleet(Fleet fleet) {
        fleets.add(fleet);
    }

    // Rimuove una flotta dalla mappa
    public void removeFleet(Fleet fleet) {
        fleets.remove(fleet);
    }

    // Aggiorna lo stato di tutte le flotte
    public void updateFleets(double deltaTime) {
        for (Fleet fleet : new ArrayList<>(fleets)) {
            fleet.update(deltaTime);
            if (fleet.hasArrived()) {
                handleFleetArrival(fleet);
            }
        }
    }

//    // Gestisce l'arrivo di una flotta a destinazione
//    private void handleFleetArrival(Fleet fleet) {
//        StarSystem destination = fleet.getDestination();
//        Player fleetOwner = fleet.getOwner();
//        int fleetShips = fleet.getShips();
//
//        if (destination.getOwner() == null || destination.getOwner() == fleetOwner) {
//            // Sistema neutrale o amico: aggiungi le navi
//            destination.addShips(fleetShips);
//            if (destination.getOwner() == null) {
//                destination.setOwner(fleetOwner);
//            }
//        } else {
//            // Sistema nemico: battaglia
//            int defenderShips = destination.getShips();
//
//            if (fleetShips > defenderShips) {
//                // L'attaccante vince
//                destination.setShips(fleetShips - defenderShips);
//                destination.setOwner(fleetOwner);
//            } else {
//                // Il difensore vince (o pareggio)
//                destination.setShips(defenderShips - fleetShips);
//            }
//        }
//
//        // Rimuovi la flotta dopo l'arrivo
//        fleetOwner.removeFleet(fleet);
//        fleets.remove(fleet);
//    }

    // Gestisce l'arrivo di una flotta a destinazione
    private void handleFleetArrival(Fleet fleet) {
        StarSystem destination = fleet.getDestination();
        Player attacker = fleet.getOwner();
        Player defender = destination.getOwner();
        int fleetShips = fleet.getShips();

        if (defender == null || defender == attacker) {
            // Sistema neutrale o amico
            if (defender == null) {
                attacker.addSystem(destination);
            }
            destination.addShips(fleetShips);
        } else {
            // Sistema nemico: battaglia
            int defendingShips = destination.getShips();

            if (fleetShips > defendingShips) {
                // Conquista
                defender.removeSystem(destination);
                if(destination.isAutomated()) {
                    destination.setAutomated(false);
                    destination.setAutomatedTo(null);
                }
                attacker.addSystem(destination);
                destination.setShips(fleetShips - defendingShips);
            } else {
                // Difensore vince o pareggio
                destination.setShips(defendingShips - fleetShips);
            }
        }

        // Rimuovi la flotta dopo l'arrivo
        attacker.removeFleet(fleet);
        fleets.remove(fleet);
    }

    // Getters
    public List<StarSystem> getSystems() { return systems; }
    public List<Fleet> getFleets() { return fleets; }
    public Dimension getMapSize() { return mapSize; }
}