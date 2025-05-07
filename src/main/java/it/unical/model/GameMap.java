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

// Crea connessioni tra i sistemi stellari in modo da garantire che tutti i sistemi siano collegati in un unico grafo connesso
        Set<StarSystem> connected = new HashSet<>();
        Queue<StarSystem> toConnect = new LinkedList<>();

// Inizializza con il primo sistema
        connected.add(systems.get(0));
        toConnect.add(systems.get(0));

// Collega iterativamente i sistemi finché non sono tutti parte dello stesso grafo
        while (connected.size() < systems.size()) {
            StarSystem current = toConnect.poll();

            // Trova i sistemi non ancora collegati più vicini a 'current'
            List<StarSystem> nearest = findNearestSystems(current, systems.size());
            for (StarSystem system : nearest) {
                if (!connected.contains(system)) {
                    // Collega i due sistemi
                    current.connectTo(system);
                    connected.add(system);
                    toConnect.add(system);
                    break; // Collega un sistema alla volta per evitare cicli
                }
            }
        }

// Una volta garantita la connessione di tutti i sistemi, aggiungi connessioni aggiuntive per varietà
        for (StarSystem system : systems) {
            List<StarSystem> nearest = findNearestSystems(system, 3);
            for (StarSystem nearby : nearest) {
                system.connectTo(nearby);
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
        Player attacker       = fleet.getOwner();
        Player defender       = destination.getOwner();
        int fleetShips        = fleet.getShips();

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