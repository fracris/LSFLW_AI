package it.unical.model;

import java.awt.Dimension;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
    public void generateRandomMap(int numSystems, int minDistance) {
        Random rand = new Random();
        systems.clear();

        for (int i = 0; i < numSystems; i++) {
            boolean validPosition = false;
            Point position = null;

            // Trova una posizione valida per il nuovo sistema stellare
            while (!validPosition) {
                int x = rand.nextInt(mapSize.width - 100) + 50;
                int y = rand.nextInt(mapSize.height - 100) + 50;
                position = new Point(x, y);

                validPosition = true;
                // Controlla che sia abbastanza distante dagli altri sistemi
                for (StarSystem system : systems) {
                    if (position.distance(system.getPosition()) < minDistance) {
                        validPosition = false;
                        break;
                    }
                }
            }

            // Crea il nuovo sistema con un tasso di produzione casuale (1-5)
            StarSystem newSystem = new StarSystem(
                    i,
                    "Sistema " + (i + 1),
                    position,
                    rand.nextInt(5) + 1
            );

            systems.add(newSystem);
        }

        // Crea connessioni tra i sistemi (costruisce il grafo)
        createConnections();
    }

    // Crea connessioni tra i sistemi stellari
    private void createConnections() {
        // Implementazione semplice: connette ogni sistema ai 2-3 sistemi più vicini
        for (StarSystem system : systems) {
            List<StarSystem> nearestSystems = findNearestSystems(system, 3);
            for (StarSystem nearest : nearestSystems) {
                system.connectTo(nearest);
            }
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
        List<Fleet> arrivedFleets = new ArrayList<>();

        for (Fleet fleet : fleets) {
            fleet.update(deltaTime);

            if (fleet.hasArrived()) {
                arrivedFleets.add(fleet);
            }
        }

        // Gestisce le flotte arrivate
        for (Fleet fleet : arrivedFleets) {
            handleFleetArrival(fleet);
        }
    }

    // Gestisce l'arrivo di una flotta a destinazione
    private void handleFleetArrival(Fleet fleet) {
        StarSystem destination = fleet.getDestination();
        Player fleetOwner = fleet.getOwner();
        int fleetShips = fleet.getShips();

        if (destination.getOwner() == null || destination.getOwner() == fleetOwner) {
            // Sistema neutrale o amico: aggiungi le navi
            destination.addShips(fleetShips);
            if (destination.getOwner() == null) {
                destination.setOwner(fleetOwner);
            }
        } else {
            // Sistema nemico: battaglia
            int defenderShips = destination.getShips();

            if (fleetShips > defenderShips) {
                // L'attaccante vince
                destination.setShips(fleetShips - defenderShips);
                destination.setOwner(fleetOwner);
            } else {
                // Il difensore vince (o pareggio)
                destination.setShips(defenderShips - fleetShips);
            }
        }

        // Rimuovi la flotta dopo l'arrivo
        fleetOwner.removeFleet(fleet);
        fleets.remove(fleet);
    }

    // Getters
    public List<StarSystem> getSystems() { return systems; }
    public List<Fleet> getFleets() { return fleets; }
    public Dimension getMapSize() { return mapSize; }
}