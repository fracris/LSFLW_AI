package it.unical.controller;


import it.unical.gui.ControlPanel;
import it.unical.gui.GameFrame;
import it.unical.model.Fleet;
import it.unical.model.GameState;
import it.unical.model.StarSystem;

import java.util.List;
import java.util.Random;

public class GameController {
    private GameState state;
    private GameFrame view;
    private StarSystem selectedSystem;
    private StarSystem targetSystem;

    public GameController() {
        // Inizializza lo stato del gioco
        state = new GameState();
        state.initialize();

        // Inizializza la view
        view = new GameFrame(this);
    }

    public void startGame() {
        // Inizia il gioco
        gameLoop();
    }

    private void gameLoop() {
        // Per ora, il loop di gioco è semplice, verrà espanso in futuro
        // con l'integrazione di EmbASP e DLV2
        while (!state.isGameOver()) {
            // Aggiorna la view
            view.update();

            // Se l'attuale giocatore è l'IA
            if (state.getCurrentPlayer().isAI()) {
                // In futuro, qui verrà chiamato EmbASP
                // Per ora, eseguiamo solo una mossa casuale
                makeRandomAIMove();
            }

            // Attendi l'input del giocatore umano o passa al prossimo turno
            // per l'IA
        }
    }

    public void handleMapClick(int x, int y) {
        // Trova il sistema cliccato
        StarSystem clickedSystem = state.getMap().getSystemAt(x, y);

        if (clickedSystem != null) {
            // Se nessun sistema è selezionato, questo diventa il sistema selezionato
            if (selectedSystem == null) {
                if (clickedSystem.getOwner() == state.getCurrentPlayer()) {
                    selectedSystem = clickedSystem;
                }
            }
            // Altrimenti, se un sistema è già selezionato
            else {
                // Se clicchiamo su un sistema diverso, diventa il target
                if (clickedSystem != selectedSystem) {
                    targetSystem = clickedSystem;

                    // Invia una flotta
                    ControlPanel controlPanel = view.getControlPanel();
                    int percentage = controlPanel.getShipsPercentage();
                    sendFleet(selectedSystem, targetSystem, percentage);

                    // Resetta la selezione
                    selectedSystem = null;
                    targetSystem = null;
                }
                // Se clicchiamo sullo stesso sistema, deseleziona
                else {
                    selectedSystem = null;
                }
            }

            // Aggiorna la view
            view.update();
        }
    }

    public void endTurn() {
        // Passa al prossimo turno
        state.nextTurn();

        // Resetta la selezione
        selectedSystem = null;
        targetSystem = null;

        // Aggiorna la view
        view.update();
    }

    private void sendFleet(StarSystem source, StarSystem target, int percentage) {
        // Calcola il numero di navi da inviare
        int totalShips = source.getShips();
        int shipsToSend = totalShips * percentage / 100;

        if (shipsToSend > 0) {
            // Rimuovi le navi dal sistema di origine
            source.removeShips(shipsToSend);

            // Crea una nuova flotta
            Fleet newFleet = new Fleet(shipsToSend, state.getCurrentPlayer(), source, target);

            // Aggiungi la flotta allo stato di gioco
            state.addFleet(newFleet);
        }
    }

    private void makeRandomAIMove() {
        // Implementazione semplice per l'IA in attesa di EmbASP
        // Seleziona casualmente un sistema posseduto dall'IA
        List<StarSystem> aiSystems = state.getSystemsOwnedBy(state.getCurrentPlayer());

        if (!aiSystems.isEmpty()) {
            Random random = new Random();
            StarSystem source = aiSystems.get(random.nextInt(aiSystems.size()));

            // Trova tutti i sistemi adiacenti
            List<StarSystem> neighbors = state.getMap().getNeighbors(source);

            if (!neighbors.isEmpty()) {
                StarSystem target = neighbors.get(random.nextInt(neighbors.size()));

                // Invia una flotta con il 50% delle navi
                sendFleet(source, target, 50);
            }
        }

        // Termina il turno dell'IA
        endTurn();
    }

    // Getter
    public GameState getGameState() {
        return state;
    }

    public StarSystem getSelectedSystem() {
        return selectedSystem;
    }

    public void zoom(float v) {
    }

    public void deselectSystem() {
    }

    public void panView(int i, int i1) {
    }
}
