package it.unical.ai;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.text.SimpleDateFormat;
import java.util.Date;

import it.unical.mat.embasp.base.Handler;
import it.unical.mat.embasp.base.InputProgram;
import it.unical.mat.embasp.base.OptionDescriptor;
import it.unical.mat.embasp.base.Output;
import it.unical.mat.embasp.languages.asp.ASPInputProgram;
import it.unical.mat.embasp.platforms.desktop.DesktopHandler;
import it.unical.mat.embasp.specializations.dlv2.desktop.DLV2DesktopService;
import it.unical.model.*;

public class AIPlayer {
    private Difficulty difficulty;
    private Player player;
    private GameState gameState;
    private String aspStrategy;
    private String consolidamento_strategy = "encodings/consolidamento_strategy.txt";


    private boolean isInitialized;
    // Directory per salvare i log dei fatti ASP
    private final String logDirectory = "logs/asp_facts/";
    // Formato data per i nomi dei file
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
    // Oggetto che mantiene le metriche del turno precedente
    private PreviousGameMetrics previousMetrics;

    public AIPlayer(Player player, GameState gameState, Difficulty difficulty) {
        this.player = player;
        this.gameState = gameState;
        this.difficulty = difficulty;


        int enemySystemCount=gameState.getAiPlayers().size();
        int neutralSystemCount=gameState.getGameMap().getSystems().size()-gameState.getPlayers().size();
        int myShipsTotal= player.getTotalShips();
        int enemyShipsTotal=0;
        Map<Integer,int[]> specMetrics = new HashMap<>();


        for(Player p:gameState.getPlayers()) {
            if(player.getId()!=p.getId()) {
                System.out.println(p.getId() + " " + p.getName());
                specMetrics.put(p.getId(),new int[]{p.getOwnedSystems().size(),p.getTotalShips()});
                enemyShipsTotal += p.getTotalShips();
            }
        }

        enemyShipsTotal-=player.getTotalShips();
        // Inizializza le metriche con i valori predefiniti
        this.previousMetrics = new PreviousGameMetrics(1,enemySystemCount,neutralSystemCount,myShipsTotal,enemyShipsTotal,specMetrics);

        if (!player.isAI()) {
            System.err.println("ATTENZIONE: AIPlayer assegnato a un giocatore non IA");
            return;
        }

        if(difficulty instanceof Difficulty.Easy)
        {
            this.aspStrategy = "encodings/easy.txt";

        }
        else if (difficulty instanceof Difficulty.Medium || difficulty instanceof Difficulty.Hard)
        {
            this.aspStrategy = "encodings/choose_strategy.asp";
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
            // Determina quale approccio usare in base al livello di difficoltà
            if (difficulty instanceof Difficulty.Easy) {
                // Per il livello facile, esegui la strategia direttamente senza metriche precedenti
                executeEasyStrategy();
            } else {
                // Per i livelli medio e difficile, usa l'approccio a due fasi
                executeAdvancedStrategy();
            }

            if(difficulty instanceof Difficulty.Medium || difficulty instanceof Difficulty.Hard) {
                previousMetrics.updateFromGameState(gameState, player);
            }

        } catch (Exception e) {
            System.err.println("Errore durante il turno IA: " + e.getMessage());
            e.printStackTrace();
        }
    }


    private void executeEasyStrategy() throws Exception {
        // Genera i fatti ASP base (senza metriche precedenti)
        String aspFacts = convertGameStateToASP(false);



//        System.out.println("--- ASP Facts (Easy) START ---");
//        System.out.println(aspFacts);
//        System.out.println("--- ASP Facts (Easy) END ---\n");

        // Salva i fatti ASP in un file per debug
        saveAspFactsToFile(aspFacts);


        // Configura il solver DLV
        Handler handler = new DesktopHandler(new DLV2DesktopService("lib/dlv.exe"));

        // Imposta la strategia ASP per il livello facile
        InputProgram strategyProgram = new ASPInputProgram();
        strategyProgram.addFilesPath(aspStrategy);
        handler.addProgram(strategyProgram);

        // Imposta i fatti del gioco
        InputProgram factsProgram = new ASPInputProgram();
        factsProgram.addProgram(aspFacts);
        handler.addProgram(factsProgram);

        // Esegue DLV2
        Output output = handler.startSync();

        // Debug: stampa l'output DLV
        if (output != null) {
            System.out.println("--- DLV Output (Easy) START ---");
            System.out.println(output.getOutput());
            System.out.println("--- DLV Output (Easy) END ---\n");
        }

        if (output == null || (output.getErrors() != null && !output.getErrors().isEmpty())) {
            System.err.println("Errore durante l'esecuzione di EMBASP: " + output.getErrors());
            return;
        }

        // Estrazione e interpretazione degli answer set
        List<String> actions = parseAnswerSets(output.getOutput());

        if (!actions.isEmpty()) {
            executeActionsFromStrings(actions);
        } else {
            System.out.println("Nessun answer set valido trovato per " + player.getName());
        }
    }


    private void executeAdvancedStrategy() throws Exception {
        // FASE 1: Determina la strategia ottimale

        // Genera i fatti ASP completi (con metriche precedenti)
        String aspFacts = convertGameStateToASP(true);

        // Aggiungi il fatto del livello di difficoltà
        String difficultyLevel = (difficulty instanceof Difficulty.Medium) ? "medium" : "hard";
        aspFacts += "difficulty(" + difficultyLevel + ").\n";


        saveAspFactsToFile(aspFacts);

        // Configura il solver DLV
        Handler handler = new DesktopHandler(new DLV2DesktopService("lib/dlv.exe"));
        OptionDescriptor option = new OptionDescriptor(" --printonlyoptimum");
        handler.addOption(option);

        // Imposta la strategia ASP per la scelta della strategia
        InputProgram strategyChooserProgram = new ASPInputProgram();
        strategyChooserProgram.addFilesPath(aspStrategy);
        handler.addProgram(strategyChooserProgram);

        // Imposta i fatti del gioco
        InputProgram factsProgramPhase1 = new ASPInputProgram();
        factsProgramPhase1.addProgram(aspFacts);
        handler.addProgram(factsProgramPhase1);

        // Esegue DLV2 per la fase 1
        Output outputPhase1 = handler.startSync();

        // Debug: stampa l'output DLV della fase 1
        if (outputPhase1 != null) {
            System.out.println("--- DLV Output (Phase 1) START ---");
            System.out.println(outputPhase1.getOutput());
            System.out.println("--- DLV Output (Phase 1) END ---\n");
        }

        if (outputPhase1 == null || (outputPhase1.getErrors() != null && !outputPhase1.getErrors().isEmpty())) {
            System.err.println("Errore durante l'esecuzione di EMBASP (Phase 1): " + outputPhase1.getErrors());
            return;
        }

        // Estrai la strategia scelta
        List<String> chosenStrategies = extractChosenStrategies(outputPhase1.getOutput());

        if (chosenStrategies.isEmpty()) {
            System.err.println("Nessuna strategia trovata nella fase 1. Impossibile procedere.");
            return;
        }

        // Debug: stampa le strategie scelte
        System.out.println("Strategie scelte: " + chosenStrategies);

        // Estrazione e interpretazione degli answer set della fase 2
        List<String> actions1 = parseAnswerSets(outputPhase1.getOutput());

        if (!actions1.isEmpty()) {
            executeActionsFromStrings(actions1);
        }
        else {
            System.out.println("Nessun answer set valido trovato per " + player.getName() + " nella fase 2");
        }

        StringBuilder aspFacts2 = parseFactsSets(outputPhase1.getOutput());


        // Aggiungi le strategie scelte ai fatti ASP
        //StringBuilder enhancedFacts = new StringBuilder(aspFacts);
        for (String sendfleet : actions1) {
            aspFacts2.append(sendfleet).append(".\n");
        }


        Handler handler2 = new DesktopHandler(new DLV2DesktopService("lib/dlv.exe"));
        OptionDescriptor option2 = new OptionDescriptor(" --printonlyoptimum");

        handler2.addOption(option2);

        // Imposta la strategia ASP per l'esecuzione
        InputProgram executionProgram = new ASPInputProgram();
        executionProgram.addFilesPath(consolidamento_strategy);
        handler2.addProgram(executionProgram);

        // Imposta i fatti del gioco con le strategie scelte
        InputProgram factsProgramPhase2 = new ASPInputProgram();
        factsProgramPhase2.addProgram(aspFacts2.toString());
        handler2.addProgram(factsProgramPhase2);

        // Esegue DLV2 per la fase 2
        Output outputPhase2 = handler2.startSync();

        // Debug: stampa l'output DLV della fase 2
        if (outputPhase2 != null) {
            System.out.println("--- DLV Output (Phase 2) START ---");
            System.out.println(outputPhase2.getOutput());
            System.out.println("--- DLV Output (Phase 2) END ---\n");
        }

        if (outputPhase2 == null || (outputPhase2.getErrors() != null && !outputPhase2.getErrors().isEmpty())) {
            System.err.println("Errore durante l'esecuzione di EMBASP (Phase 2): " + outputPhase2.getErrors());
            return;
        }

        // Estrazione e interpretazione degli answer set della fase 2
        List<String> actions2 = parseAnswerSets2(outputPhase2.getOutput());

        if (!actions2.isEmpty()) {
            executeActionsFromStrings(actions2);
        } else {
            System.out.println("Nessun answer set valido trovato per " + player.getName() + " nella fase 2");
        }
    }



    private List<String> extractChosenStrategies(String dlvOutput) {
        List<String> strategies = new ArrayList<>();
        Pattern p = Pattern.compile("chosen_strategy\\([^)]*\\)");
        Matcher m = p.matcher(dlvOutput);
        while (m.find()) {
            strategies.add(m.group());
        }
        return strategies;
    }


    private StringBuilder parseFactsSets(String dlvOutput) {
        StringBuilder factsBuilder = new StringBuilder();

        // Tutti i predicati che ci interessano
        String[] predicates = {
                "enemy\\(\\d+\\)",
                "enemy_system\\(\\d+,\\d+\\)",
                "undirected_connected\\(\\d+,\\d+\\)",
                "border_system\\(\\d+\\)",
                "my_system\\(\\d+\\)",
                "ships\\(\\d+,\\d+\\)",
                "difficulty\\([a-z]+\\)",
                "send_fleet\\(\\d+,\\d+,\\d+\\)"
        };

        for (String patternStr : predicates) {
            Pattern pattern = Pattern.compile(patternStr);
            Matcher matcher = pattern.matcher(dlvOutput);
            while (matcher.find()) {
                factsBuilder.append(matcher.group()).append(".\n");
            }
        }

        return factsBuilder;
    }




    private String convertGameStateToASP(boolean includeMetrics) {
        StringBuilder facts = new StringBuilder();

        facts.append("ai_player(").append(player.getId()).append(").\n");

        if(player.getSystemsLost()!=null) {
            for(StarSystem system: player.getSystemsLost()) {
                facts.append("system_lost(").append(system.getId()).append(").\n");
            }

            player.getSystemsLost().clear();
        }

        if(player.getSystemsGained()!=null) {
            for(StarSystem system: player.getSystemsGained()) {
                facts.append("system_gained(").append(system.getId()).append(").\n");
            }

            player.getSystemsGained().clear();
        }

        for (StarSystem system : gameState.getGameMap().getSystems()) {
            facts.append("system(").append(system.getId()).append(").\n");
            if (system.getOwner() != null) {
                facts.append("owner(").append(system.getId()).append(",")
                        .append(system.getOwner().getId()).append(").\n");
            } else {
                facts.append("neutral_system(").append(system.getId()).append(").\n");
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

//        for (Fleet fleet : gameState.getGameMap().getFleets()) {
//            facts.append("fleet(")
//                    .append(fleet.getId()).append(",")
//                    .append(fleet.getOwner().getId()).append(",")
//                    .append(fleet.getShips()).append(",")
//                    .append(fleet.getSource().getId()).append(",")
//                    .append(fleet.getDestination().getId()).append(",")
//                    .append((int) fleet.getProgress()).append(").\n");
//        }

        // Aggiungi i dati storici solo se richiesto
        if (includeMetrics) {
            facts.append("\n% Dati storici per confronto\n");
            facts.append(previousMetrics.toAspFacts());
        }

        return facts.toString();
    }

    /**
     * Salva i fatti ASP in un file con un suffisso opzionale
     */
    private void saveAspFactsToFile(String aspFacts, String suffix) {
        String timestamp = dateFormat.format(new Date());
        String filename = logDirectory + "player" + player.getId() + "_facts" + suffix + "_" + timestamp + ".asp";

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            writer.write("% Fatti ASP generati da " + player.getName() + " (ID: " + player.getId() + ")\n");
            writer.write("% Data: " + new Date() + "\n\n");
            writer.write(aspFacts);
            System.out.println("Fatti ASP salvati in: " + filename);
        } catch (IOException e) {
            System.err.println("Errore durante il salvataggio dei fatti ASP su file: " + e.getMessage());
        }
    }

    /**
     * Versione sovraccarica per compatibilità con il codice esistente
     */
    private void saveAspFactsToFile(String aspFacts) {
        saveAspFactsToFile(aspFacts, "");
    }


//    // Metodo per salvare i fatti ASP in un file
//    private void saveAspFactsToFile(String aspFacts) {
//        String timestamp = dateFormat.format(new Date());
//        String filename = logDirectory + "player" + player.getId() + "_facts_" + timestamp + ".asp";
//
//        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
//            writer.write("% Fatti ASP generati da " + player.getName() + " (ID: " + player.getId() + ")\n");
//            writer.write("% Data: " + new Date() + "\n\n");
//            writer.write(aspFacts);
//            System.out.println("Fatti ASP salvati in: " + filename);
//        } catch (IOException e) {
//            System.err.println("Errore durante il salvataggio dei fatti ASP su file: " + e.getMessage());
//        }
//    }

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


    private List<String> parseAnswerSets2(String dlvOutput) {
        List<String> actions = new ArrayList<>();
        // Match consolidation_fleet(From, To, Ships)
        Pattern p = Pattern.compile("consolidation_fleet\\(([^,]+),([^,]+),([^\\)]+)\\)");
        Matcher m = p.matcher(dlvOutput);
        while (m.find()) {
            String from = m.group(1).trim();
            String to = m.group(2).trim();
            String ships = m.group(3).trim();
            String sendFleet = "send_fleet(" + from + "," + to + "," + ships + ")";
            actions.add(sendFleet);
        }
        System.out.println("Azioni estratte e convertite: " + actions.size());
        return actions;
    }



    private void executeActionsFromStrings(List<String> actions) {
        // Salva le azioni eseguite in un file (solo per log)
        saveExecutedActionsToFile(actions);

        boolean executedAny = false;
        for (String atomStr : actions) {
            try {
                String content = atomStr.substring("send_fleet(".length(), atomStr.length() - 1);
                String[] params = content.split(",");
                if (params.length != 3) {
                    System.err.println("Formato non valido: " + atomStr);
                    continue;
                }

                int sourceId = Integer.parseInt(params[0].trim());
                int targetId = Integer.parseInt(params[1].trim());
                int ships    = Integer.parseInt(params[2].trim());

                StarSystem source = findSystemById(sourceId);
                StarSystem target = findSystemById(targetId);

                // validazioni…
                if (source == null || target == null) {
                    System.err.println("Sistema non trovato: " + atomStr);
                    continue;
                }
                if (!player.equals(source.getOwner())) {
                    System.err.println("Non è tuo: " + atomStr);
                    continue;
                }
                if (!source.getConnectedSystems().contains(target)) {
                    System.err.println("Non connessi: " + atomStr);
                    continue;
                }
                if (source.getShips() < ships) { // lascia sempre 1 nave
                    System.err.println("Non abbastanza navi: " + atomStr);
                    continue;
                }

                // Esegui la flotta
                Fleet fleet = gameState.sendFleet(player, source, target, ships);
                if (fleet != null) {

                    executedAny = true;
                    System.out.println("Flotta inviata: " + atomStr);
                }
            } catch (Exception e) {
                System.err.println("Errore parsing action: " + atomStr);
                e.printStackTrace();
            }
        }

        if (!executedAny) {
            System.out.println("Nessuna flotta inviata, tutti i send_fleet invalidi o già processati");
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
