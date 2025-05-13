package it.unical.ai;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.text.SimpleDateFormat;
import java.util.Date;

import it.unical.mat.embasp.base.Handler;
import it.unical.mat.embasp.base.InputProgram;
import it.unical.mat.embasp.base.Output;
import it.unical.mat.embasp.languages.asp.ASPInputProgram;
import it.unical.mat.embasp.platforms.desktop.DesktopHandler;
import it.unical.mat.embasp.specializations.dlv2.desktop.DLV2DesktopService;
import it.unical.model.*;

public class AIPlayer {
    private Player player;
    private GameState gameState;
    private String aspStrategy = "encodings/strategy_selector.txt";
    private boolean isInitialized;
    // Directory per salvare i log dei fatti ASP
    private final String logDirectory = "logs/asp_facts/";
    // Formato data per i nomi dei file
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
    // Oggetto che mantiene le metriche del turno precedente
    private PreviousGameMetrics previousMetrics;

    public AIPlayer(Player player, GameState gameState) {
        this.player = player;
        this.gameState = gameState;
        // Inizializza le metriche con i valori predefiniti
        this.previousMetrics = new PreviousGameMetrics();

        if (!player.isAI()) {
            System.err.println("ATTENZIONE: AIPlayer assegnato a un giocatore non IA");
            return;
        }

        File aspFile = new File(aspStrategy);
        if (!aspFile.exists()) {
            System.err.println("ERRORE: File di strategia ASP non trovato in: " + aspStrategy);
            return;
        }

        // Crea la directory per i log se non esiste
        createLogDirectory();

        this.isInitialized = true;
    }

    // Metodo per creare la directory dei log
    private void createLogDirectory() {
        File directory = new File(logDirectory);
        if (!directory.exists()) {
            boolean created = directory.mkdirs();
            if (!created) {
                System.err.println("ERRORE: Impossibile creare la directory per i log: " + logDirectory);
            }
        }
    }

    public void performTurn() {
        if (!isInitialized) {
            System.err.println("AIPlayer non inizializzato, impossibile eseguire il turno");
            return;
        }

        try {
            // Genera i fatti ASP
            String aspFacts = convertGameStateToASP();

            // *** STAMPA I FATTI ASP PER DEBUG ***
            System.out.println("--- ASP Facts START ---");
            System.out.println(aspFacts);
            System.out.println("--- ASP Facts END ---\n");

            // Salva i fatti ASP in un file (solo per log)
            //saveAspFactsToFile(aspFacts);

            // Creazione del handler
            Handler handler = new DesktopHandler(new DLV2DesktopService("lib/dlv.exe"));

            // Imposta la strategia ASP
            InputProgram strategyProgram = new ASPInputProgram();
            strategyProgram.addFilesPath(aspStrategy);
            handler.addProgram(strategyProgram);

            // Imposta i fatti del gioco
            InputProgram factsProgram = new ASPInputProgram();
            factsProgram.addProgram(aspFacts);
            handler.addProgram(factsProgram);

            // Esegue DLV2
            Output output = handler.startSync();

            // *** STAMPA L'OUTPUT DLV PER DEBUG ***
            if (output != null) {
                System.out.println("--- DLV Output START ---");
                System.out.println(output.getOutput());
                System.out.println("--- DLV Output END ---\n");
            }

            if (output == null || (output.getErrors() != null && !output.getErrors().isEmpty())) {
                System.err.println("Errore durante l'esecuzione di EMBASP: " + output.getErrors());
                return;
            }

            // Estrazione e interpretazione degli answer set
            List<String> actions = parseAnswerSets(output.getOutput());

            // Salva anche gli answer set trovati
            //saveAnswerSetsToFile(output.getOutput());

            if (!actions.isEmpty()) {
                executeActionsFromStrings(actions);
            } else {
                System.out.println("Nessun answer set valido trovato per " + player.getName());
            }

            // Aggiorna le metriche precedenti con lo stato corrente del gioco
            // (da fare dopo l'esecuzione delle azioni per il prossimo turno)
            previousMetrics.updateFromGameState(gameState, player);

        } catch (Exception e) {
            System.err.println("Errore durante il turno IA: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Metodo per salvare i fatti ASP in un file
    private void saveAspFactsToFile(String aspFacts) {
        String timestamp = dateFormat.format(new Date());
        String filename = logDirectory + "player" + player.getId() + "_facts_" + timestamp + ".asp";

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            writer.write("% Fatti ASP generati da " + player.getName() + " (ID: " + player.getId() + ")\n");
            writer.write("% Data: " + new Date() + "\n\n");
            writer.write(aspFacts);
            System.out.println("Fatti ASP salvati in: " + filename);
        } catch (IOException e) {
            System.err.println("Errore durante il salvataggio dei fatti ASP su file: " + e.getMessage());
        }
    }

    // Metodo per salvare gli answer set in un file
    private void saveAnswerSetsToFile(String output) {
        String timestamp = dateFormat.format(new Date());
        String filename = logDirectory + "player" + player.getId() + "_answers_" + timestamp + ".txt";

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            writer.write("% Answer sets generati per " + player.getName() + " (ID: " + player.getId() + ")\n");
            writer.write("% Data: " + new Date() + "\n\n");
            writer.write(output);
            System.out.println("Answer sets salvati in: " + filename);
        } catch (IOException e) {
            System.err.println("Errore durante il salvataggio degli answer set su file: " + e.getMessage());
        }
    }

    private List<String> parseAnswerSets(String dlvOutput) {
        List<String> actions = new ArrayList<>();
        Pattern p = Pattern.compile("send_fleet\\([^)]*\\)");
        Matcher m = p.matcher(dlvOutput);
        while (m.find()) {
            actions.add(m.group());
        }
        System.out.println("Azioni estratte: " + actions.size());
        return actions;
    }

    private void executeActionsFromStrings(List<String> actions) {
        // Salva le azioni eseguite in un file (solo per log)
        //saveExecutedActionsToFile(actions);

        for (String atomStr : actions) {
            try {
                String content = atomStr.substring("send_fleet(".length(), atomStr.length() - 1);
                String[] params = content.split(",");
                if (params.length != 3) {
                    System.err.println("Formato non valido: " + atomStr);
                    continue;
                }
                System.out.println("Azione eseguita: " + params[0] + ", " + params[1] + ", " + params[2]);

                int sourceId = Integer.parseInt(params[0].trim());
                int targetId = Integer.parseInt(params[1].trim());
                int ships = Integer.parseInt(params[2].trim());

                StarSystem source = findSystemById(sourceId);
                StarSystem target = findSystemById(targetId);

                if (source == null || target == null) {
                    System.err.println("Sistema non trovato: source=" + sourceId + ", target=" + targetId);
                    continue;
                }
                if (!Objects.equals(source.getOwner(), player)) {
                    System.err.println("Il sistema " + sourceId + " non appartiene al giocatore " + player.getId());
                    continue;
                }
                if (!source.getConnectedSystems().contains(target)) {
                    System.err.println("I sistemi " + sourceId + " e " + targetId + " non sono connessi");
                    continue;
                }
                if (source.getShips() < ships) {
                    continue;
                }

                Fleet fleet = gameState.sendFleet(player, source, target, ships);
                if (fleet != null) {
                    return;
                }
            } catch (Exception e) {
                System.err.println("Errore nell'interpretazione dell'azione: " + atomStr);
                e.printStackTrace();
            }
        }
    }

    // Metodo per salvare le azioni eseguite in un file
    private void saveExecutedActionsToFile(List<String> actions) {
        String timestamp = dateFormat.format(new Date());
        String filename = logDirectory + "player" + player.getId() + "_actions_" + timestamp + ".txt";

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            writer.write("% Azioni eseguite da " + player.getName() + " (ID: " + player.getId() + ")\n");
            writer.write("% Data: " + new Date() + "\n\n");

            for (String action : actions) {
                writer.write(action + "\n");
            }

            System.out.println("Azioni eseguite salvate in: " + filename);
        } catch (IOException e) {
            System.err.println("Errore durante il salvataggio delle azioni su file: " + e.getMessage());
        }
    }

    private String convertGameStateToASP() {
        StringBuilder facts = new StringBuilder();

        facts.append("ai_player(").append(player.getId()).append(").\n");

        for (StarSystem system : gameState.getGameMap().getSystems()) {
            facts.append("system(").append(system.getId()).append(").\n");
            if (system.getOwner() != null) {
                facts.append("owner(").append(system.getId()).append(",")
                        .append(system.getOwner().getId()).append(").\n");
            } else {
                facts.append("neutral(").append(system.getId()).append(").\n");
            }
            facts.append("ships(").append(system.getId()).append(",")
                    .append(system.getShips()).append(").\n");
            facts.append("production(").append(system.getId()).append(",")
                    .append(system.getProductionRate()).append(").\n");
        }

        for (StarSystem system : gameState.getGameMap().getSystems()) {
            for (StarSystem connected : system.getConnectedSystems()) {
                if (system.getId() < connected.getId()) {
                    facts.append("connected(")
                            .append(system.getId()).append(",")
                            .append(connected.getId()).append(").\n");
                }
            }
        }

        for (Fleet fleet : gameState.getGameMap().getFleets()) {
            facts.append("fleet(")
                    .append(fleet.getId()).append(",")
                    .append(fleet.getOwner().getId()).append(",")
                    .append(fleet.getShips()).append(",")
                    .append(fleet.getSource().getId()).append(",")
                    .append(fleet.getDestination().getId()).append(",")
                    .append((int) fleet.getProgress()).append(").\n");
        }

        // Aggiungi i dati storici
        facts.append("\n% Dati storici per confronto\n");
        facts.append(previousMetrics.toAspFacts());
        return facts.toString();
    }

    private StarSystem findSystemById(int id) {
        for (StarSystem system : gameState.getGameMap().getSystems()) {
            if (system.getId() == id) return system;
        }
        return null;
    }

    public Player getPlayer() {
        return player;
    }
}
