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
    public void generateMap(int numSystem, Difficulty difficulty) {
        systems.clear();

        StarSystem newSystem;
        Point[] positions;
        int[] productionRates;

        if (difficulty instanceof Difficulty.Easy) {
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
            productionRates= new int[]{2, 1, 1, 3, 2, 2, 3, 1, 1, 2};
        } else if (difficulty instanceof Difficulty.Medium) {
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
            productionRates= new int[]{2,1,1,2,1,1,2,1,1,2,1,1,1,1,1,1,1,2,1,2};
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
    private void createConnections(Difficulty difficulty) {
        if (difficulty instanceof Difficulty.Easy) {
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
        } else if (difficulty instanceof Difficulty.Medium) {
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

    // Trova i sistemi più vicini a un dato sistema
    private List<StarSystem> findNearestSystems(StarSystem system, int count) {
        List<StarSystem> result = new ArrayList<>();
        List<StarSystem> sortedSystems = new ArrayList<>(systems);

        // Ordina i sistemi per distanza
        sortedSystems.sort((s1, s2) -> {
            double d1 = s1.getPosition().distance(system.getPosition());
            double d2 = s2.getPosition().distance(system.getPosition());
            return Double.compare(d1, d2);
        });

        // Salta il primo perché è il sistema stesso
        for (int i = 1; i <= count && i < sortedSystems.size(); i++) {
            result.add(sortedSystems.get(i));
        }

        return result;
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
        checkFleetCollisions();
        for (Fleet fleet : new ArrayList<>(fleets)) {
            fleet.update(deltaTime);
            if (fleet.hasArrived()) {
                handleFleetArrival(fleet);
            }
        }
    }

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
                if(defender.isAI()) defender.setSystemsLost(destination);


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


    // Nuovo metodo per controllare le collisioni tra flotte
    private void checkFleetCollisions() {
        List<Fleet> fleetsToRemove = new ArrayList<>();

        // Controlla ogni coppia di flotte per possibili collisioni
        for (int i = 0; i < fleets.size(); i++) {
            Fleet fleet1 = fleets.get(i);
            if (fleetsToRemove.contains(fleet1)) continue;

            for (int j = i + 1; j < fleets.size(); j++) {
                Fleet fleet2 = fleets.get(j);
                if (fleetsToRemove.contains(fleet2)) continue;

                // Verifica se le flotte sono di giocatori diversi
                if (fleet1.getOwner() != fleet2.getOwner()) {
                    // Verifica se sono sulla stessa connessione (stessa strada)
                    if (areOnSameConnection(fleet1, fleet2)) {
                        // Verifica se si scontrano (si incrociano o si sovrappongono)
                        if (areColliding(fleet1, fleet2)) {
                            // Risolvi la collisione
                            resolveCollision(fleet1, fleet2, fleetsToRemove);
                        }
                    }
                }
            }
        }

        // Rimuovi le flotte distrutte
        for (Fleet fleet : fleetsToRemove) {
            Player owner = fleet.getOwner();
            owner.removeFleet(fleet);
            fleets.remove(fleet);
        }
    }

    // Controlla se due flotte sono sulla stessa connessione
    private boolean areOnSameConnection(Fleet fleet1, Fleet fleet2) {
        StarSystem src1 = fleet1.getSource();
        StarSystem dst1 = fleet1.getDestination();
        StarSystem src2 = fleet2.getSource();
        StarSystem dst2 = fleet2.getDestination();

        // Sono sulla stessa connessione se condividono gli stessi sistemi (in qualsiasi ordine)
        return (src1 == src2 && dst1 == dst2) || (src1 == dst2 && dst1 == src2);
    }

    // Controlla se due flotte si scontrano (incrociano o sovrappongono)
    private boolean areColliding(Fleet fleet1, Fleet fleet2) {
        double progress1 = fleet1.getProgress();
        double progress2 = fleet2.getProgress();

        // Se le flotte viaggiano nella stessa direzione
        if (fleet1.getSource() == fleet2.getSource()) {
            // Collisione se la distanza tra i progressi è molto piccola
            return Math.abs(progress1 - progress2) < 0.05;
        } else {
            // Se viaggiano in direzioni opposte, collisione se i progressi sommati superano 1
            return progress1 + progress2 >= 1.0 && Math.abs(progress1 + progress2 - 1.0) < 0.05;
        }
    }

    // Risolve la collisione tra due flotte
    private void resolveCollision(Fleet fleet1, Fleet fleet2, List<Fleet> fleetsToRemove) {
        int ships1 = fleet1.getShips();
        int ships2 = fleet2.getShips();

        if (ships1 > ships2) {
            // Fleet1 vince
            // Sottrai il numero di navi della flotta sconfitta a quella vincente
            fleet1.setShips(ships1 - ships2);
            fleetsToRemove.add(fleet2);
        } else if (ships2 > ships1) {
            // Fleet2 vince
            fleet2.setShips(ships2 - ships1);
            fleetsToRemove.add(fleet1);
        } else {
            // Pareggio: entrambe le flotte si distruggono a vicenda
            fleetsToRemove.add(fleet1);
            fleetsToRemove.add(fleet2);
        }
    }


    // Getters
    public List<StarSystem> getSystems() { return systems; }
    public List<Fleet> getFleets() { return fleets; }
    public Dimension getMapSize() { return mapSize; }
}