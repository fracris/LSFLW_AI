package it.unical.model;

import it.unical.mat.embasp.base.Handler;
import it.unical.mat.embasp.base.InputProgram;
import it.unical.mat.embasp.languages.asp.ASPInputProgram;
import it.unical.mat.embasp.languages.asp.AnswerSet;
import it.unical.mat.embasp.languages.asp.AnswerSets;
import it.unical.mat.embasp.platforms.desktop.DesktopHandler;
import it.unical.mat.embasp.specializations.dlv2.desktop.DLV2DesktopService;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.ArrayList;

public class AIPlayer {
    private Player player;
    private GameState gameState;
    private Handler handler;
    private String dlv2Path; // Percorso all'eseguibile DLV2
    private String aspStrategy; // File con le regole ASP per la strategia

    public AIPlayer(Player player, GameState gameState, String dlv2Path, String aspStrategy) {
        this.player = player;
        this.gameState = gameState;
        this.dlv2Path = dlv2Path;
        this.aspStrategy = aspStrategy;

        // Inizializza EmbASP con DLV2
        handler = new DesktopHandler(new DLV2DesktopService(dlv2Path));
    }

    // Metodo principale che verrà chiamato ogni secondo
    public void makeMoves() throws InvocationTargetException, IllegalAccessException, NoSuchMethodException, InstantiationException {
        // 1. Converti lo stato del gioco in fatti ASP
        String aspFacts = convertGameStateToASP();

        // 2. Carica il programma ASP con i fatti e la strategia
        InputProgram program = new ASPInputProgram();

        // Aggiungi i fatti generati dallo stato del gioco
        program.addProgram(aspFacts);

        try {
            // Aggiungi la strategia dal file
            program.addFilesPath(aspStrategy);
        } catch (Exception e) {
            System.err.println("Errore nel caricamento della strategia ASP: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        handler.addProgram(program);

        // 3. Esegui DLV2 e ottieni gli Answer Set
        AnswerSets answerSets = (AnswerSets) handler.startSync();

        // 4. Interpreta i risultati e esegui le mosse
        if (answerSets != null) {
            executeASPResults(answerSets);
        }

        // Pulisci il programma dopo l'esecuzione
        handler.removeProgram(program);
    }

    // Converte lo stato del gioco in fatti ASP
    private String convertGameStateToASP() {
        StringBuilder facts = new StringBuilder();

        // Aggiungi fatti sul giocatore
        facts.append("player(" + player.getId() + ").\n");

        // Aggiungi fatti sui sistemi
        for (StarSystem system : gameState.getGameMap().getSystems()) {
            facts.append("system(" + system.getId() + ").\n");

            if (system.getOwner() != null) {
                facts.append("owner(" + system.getId() + "," + system.getOwner().getId() + ").\n");
            }

            facts.append("ships(" + system.getId() + "," + system.getShips() + ").\n");
            facts.append("production(" + system.getId() + "," + system.getProductionRate() + ").\n");

            // Aggiungi fatti sulle connessioni
            for (StarSystem connected : system.getConnectedSystems()) {
                if (system.getId() < connected.getId()) { // Evita duplicati
                    facts.append("connected(" + system.getId() + "," + connected.getId() + ").\n");
                }
            }
        }

        // Aggiungi fatti sulle flotte
        for (Fleet fleet : gameState.getGameMap().getFleets()) {
            facts.append("fleet(" + fleet.getId() + ").\n");
            facts.append("fleet_owner(" + fleet.getId() + "," + fleet.getOwner().getId() + ").\n");
            facts.append("fleet_ships(" + fleet.getId() + "," + fleet.getShips() + ").\n");
            facts.append("fleet_source(" + fleet.getId() + "," + fleet.getSource().getId() + ").\n");
            facts.append("fleet_destination(" + fleet.getId() + "," + fleet.getDestination().getId() + ").\n");
            facts.append("fleet_progress(" + fleet.getId() + "," + fleet.getProgress() + ").\n");
        }

        // Aggiungi il turno corrente

        return facts.toString();
    }

    // Interpreta i risultati di DLV2 e esegui le mosse
    private void executeASPResults(AnswerSets answerSets) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException, InstantiationException {
        // Prendiamo il primo Answer Set (se disponibile)
        if (answerSets.getAnswersets().size() == 0) {
            System.out.println("Nessun answer set trovato.");
            return;
        }

        AnswerSet as = answerSets.getAnswersets().get(0);

        // Cerca i predicati "send_fleet"
        List<String> actions = new ArrayList<>();
        for (Object atom : as.getAtoms()) {
            if(atom instanceof String)
            {
                if (((String) atom).startsWith("send_fleet")) {
                    actions.add((String) atom);
                }
            }
        }

        // Esegui le azioni
        for (String action : actions) {
            // Format: send_fleet(sourceId, targetId, ships)
            String[] parts = action.substring(11, action.length() - 1).split(",");

            try {
                int sourceId = Integer.parseInt(parts[0]);
                int targetId = Integer.parseInt(parts[1]);
                int ships = Integer.parseInt(parts[2]);

                // Trova i sistemi corrispondenti
                StarSystem source = findSystemById(sourceId);
                StarSystem target = findSystemById(targetId);

                if (source != null && target != null) {
                    // Esegui l'invio della flotta
//                    gameState.sendFleet(source, target, ships);
                    System.out.println("IA invia flotta: " + ships + " navi da " + source.getName() + " a " + target.getName());
                }
            } catch (Exception e) {
                System.err.println("Errore nell'esecuzione dell'azione: " + action);
                e.printStackTrace();
            }
        }
    }

    // Trova un sistema per ID
    private StarSystem findSystemById(int id) {
        for (StarSystem system : gameState.getGameMap().getSystems()) {
            if (system.getId() == id) {
                return system;
            }
        }
        return null;
    }
}