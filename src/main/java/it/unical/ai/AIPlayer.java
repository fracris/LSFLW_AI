
package it.unical.ai;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.concurrent.atomic.AtomicBoolean;

import it.unical.mat.embasp.base.Handler;
import it.unical.mat.embasp.base.InputProgram;
import it.unical.mat.embasp.base.OptionDescriptor;
import it.unical.mat.embasp.base.Output;
import it.unical.mat.embasp.languages.asp.ASPInputProgram;
import it.unical.mat.embasp.platforms.desktop.DesktopHandler;
import it.unical.mat.embasp.specializations.dlv2.desktop.DLV2DesktopService;
import it.unical.model.*;

public class AIPlayer {
    private final Difficulty difficulty;
    private Player player;
    private final GameState gameState;
    private String aspStrategy;

    private boolean isInitialized;

    private final ExecutorService dlvExecutor = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r, "DLV-" + (player != null ? player.getId() : "unknown"));
        t.setDaemon(true);
        return t;
    });

    private static final int DLV_TIMEOUT_SECONDS = 3;

    private final AtomicBoolean isExecuting = new AtomicBoolean(false);

    private final Set<Handler> activeHandlers = Collections.synchronizedSet(new HashSet<>());

    public AIPlayer(Player player, GameState gameState, Difficulty difficulty) {
        this.player = player;
        this.gameState = gameState;
        this.difficulty = difficulty;


        Map<Integer, int[]> specMetrics = new HashMap<>();

        for (Player p : gameState.getPlayers()) {
            if (player.getId() != p.getId()) {
                System.out.println(p.getId() + " " + p.getName());
                specMetrics.put(p.getId(), new int[]{p.getOwnedSystems().size(), p.getTotalShips()});
            }
        }


        if (!player.isAI()) {
            System.err.println("ATTENZIONE: AIPlayer assegnato a un giocatore non IA");
            return;
        }

        if (difficulty instanceof Difficulty.Easy) {
            this.aspStrategy = "encodings/easy.txt";
        } else if (difficulty instanceof Difficulty.Medium || difficulty instanceof Difficulty.Hard) {
            this.aspStrategy = "encodings/medium-hard.txt";
        }

        File aspFile = new File(aspStrategy);
        if (!aspFile.exists()) {
            System.err.println("ERRORE: File di strategia ASP non trovato in: " + aspStrategy);
            return;
        }


        this.isInitialized = true;

        Runtime.getRuntime().addShutdownHook(new Thread(this::forceCleanupDLVProcesses));
    }

    public void performTurn() {
        if (!isInitialized) {
            System.err.println("AIPlayer non inizializzato, impossibile eseguire il turno");
            return;
        }

        if (!isExecuting.compareAndSet(false, true)) {
            System.out.println("AIPlayer " + player.getName() + " ancora in esecuzione, salto questo turno");
            return;
        }

        System.out.println("Eseguendo turno per: " + player.getName());

        dlvExecutor.submit(() -> {
            try {
                if (difficulty instanceof Difficulty.Easy) {
                    executeEasyStrategy();
                } else {
                    executeAdvancedStrategy();
                }

                System.out.println("Turno completato per: " + player.getName());
            } catch (Exception e) {
                System.err.println("Errore durante il turno IA per " + player.getName() + ": " + e.getMessage());
                e.printStackTrace();
            } finally {
                isExecuting.set(false);
            }
        });
    }


    private Output executeDLVWithTimeout(Handler handler) throws Exception {
        activeHandlers.add(handler);

        ExecutorService tempExecutor = Executors.newSingleThreadExecutor();

        try {
            Future<Output> task = tempExecutor.submit(() -> {
                try {
                    return handler.startSync();
                } catch (Exception e) {
                    throw new RuntimeException("Errore DLV: " + e.getMessage(), e);
                }
            });

            Output result = task.get(DLV_TIMEOUT_SECONDS, TimeUnit.SECONDS);

            cleanupHandler(handler);

            return result;

        } catch (TimeoutException e) {
            System.err.println("DLV timeout (" + DLV_TIMEOUT_SECONDS + "s) per " + player.getName());

            forceKillDLVProcesses();
            cleanupHandler(handler);

            throw new Exception("DLV timeout");
        } catch (ExecutionException e) {
            cleanupHandler(handler);
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException) {
                RuntimeException re = (RuntimeException) cause;
                if (re.getCause() instanceof Exception) {
                    throw (Exception) re.getCause();
                }
                throw re;
            }
            throw new Exception("Errore nell'esecuzione DLV", cause);
        } finally {
            tempExecutor.shutdownNow();
            activeHandlers.remove(handler);
        }
    }


    private void cleanupHandler(Handler handler) {
        try {
            if (handler instanceof DesktopHandler) {
                System.gc();
            }
        } catch (Exception e) {
            System.err.println("Errore durante cleanup handler: " + e.getMessage());
        }
    }


    private void forceKillDLVProcesses() {
        try {
            if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                ProcessBuilder pb = new ProcessBuilder("taskkill", "/F", "/IM", "dlv.exe");
                Process killProcess = pb.start();
                boolean finished = killProcess.waitFor(2, TimeUnit.SECONDS);
                if (!finished) {
                    killProcess.destroyForcibly();
                }
                System.out.println("Terminati processi DLV per " + player.getName());
            } else {
                ProcessBuilder pb = new ProcessBuilder("pkill", "-f", "dlv");
                Process killProcess = pb.start();
                boolean finished = killProcess.waitFor(2, TimeUnit.SECONDS);
                if (!finished) {
                    killProcess.destroyForcibly();
                }
                System.out.println("Terminati processi DLV per " + player.getName());
            }
        } catch (Exception e) {
            System.err.println("Errore durante terminazione forzata processi DLV: " + e.getMessage());
        }
    }


    private void forceCleanupDLVProcesses() {
        System.out.println("Cleanup forzato DLV per " + player.getName());

        synchronized (activeHandlers) {
            for (Handler handler : new HashSet<>(activeHandlers)) {
                try {
                    cleanupHandler(handler);
                } catch (Exception e) {
                    System.err.println("Errore cleanup handler: " + e.getMessage());
                }
            }
            activeHandlers.clear();
        }

        forceKillDLVProcesses();
    }

    private void executeEasyStrategy() throws Exception {
        String aspFacts = convertGameStateToASP(false);


        Handler handler = null;
        try {
            handler = new DesktopHandler(new DLV2DesktopService("lib/dlv.exe"));
            InputProgram strategyProgram = new ASPInputProgram();
            strategyProgram.addFilesPath(aspStrategy);
            handler.addProgram(strategyProgram);

            InputProgram factsProgram = new ASPInputProgram();
            factsProgram.addProgram(aspFacts);
            handler.addProgram(factsProgram);

            Output output = executeDLVWithTimeout(handler);

            if (output != null) {
                System.out.println("--- DLV Output (Easy) per " + player.getName() + " ---");
                System.out.println(output.getOutput());
            }

            if (output == null || (output.getErrors() != null && !output.getErrors().isEmpty())) {
                System.err.println("Errore durante l'esecuzione di EMBASP per " + player.getName() + ": " +
                        (output != null ? output.getErrors() : "output null"));
                return;
            }

            List<String> actions = parseAnswerSets(output.getOutput());
            if (!actions.isEmpty()) {
                executeActionsFromStrings(actions);
            } else {
                System.out.println("Nessun answer set valido trovato per " + player.getName());
            }
        } finally {
            if (handler != null) {
                cleanupHandler(handler);
            }
        }
    }

    private void executeAdvancedStrategy() throws Exception {
        Handler handler1 = null;
        Handler handler2 = null;

        try {
            String aspFacts = convertGameStateToASP(true);

            System.out.println(aspFacts);

            String difficultyLevel = (difficulty instanceof Difficulty.Medium) ? "medium" : "hard";
            aspFacts += "difficulty(" + difficultyLevel + ").\n";



            handler1 = new DesktopHandler(new DLV2DesktopService("lib/dlv.exe"));
            OptionDescriptor option = new OptionDescriptor(" --printonlyoptimum");
            handler1.addOption(option);

            InputProgram strategyChooserProgram = new ASPInputProgram();
            strategyChooserProgram.addFilesPath(aspStrategy);
            handler1.addProgram(strategyChooserProgram);

            InputProgram factsProgramPhase1 = new ASPInputProgram();
            factsProgramPhase1.addProgram(aspFacts);
            handler1.addProgram(factsProgramPhase1);

            Output outputPhase1 = executeDLVWithTimeout(handler1);

            if (outputPhase1 != null) {
                System.out.println("--- DLV Output (Phase 1) per " + player.getName() + " ---");
                System.out.println(outputPhase1.getOutput());
            }

            if (outputPhase1 == null || (outputPhase1.getErrors() != null && !outputPhase1.getErrors().isEmpty())) {
                System.err.println("Errore durante l'esecuzione di EMBASP (Phase 1) per " + player.getName() + ": " +
                        (outputPhase1 != null ? outputPhase1.getErrors() : "output null"));
                return;
            }

            List<String> chosenStrategies = extractChosenStrategies(outputPhase1.getOutput());
            if (chosenStrategies.isEmpty()) {
                System.err.println("Nessuna strategia trovata nella fase 1 per " + player.getName());
            } else {
                System.out.println(chosenStrategies);
            }

            List<String> actions1 = parseAnswerSets(outputPhase1.getOutput());
            if (!actions1.isEmpty()) {
                executeActionsFromStrings(actions1);
            }

            StringBuilder aspFacts2 = parseFactsSets(outputPhase1.getOutput());
            for (String sendfleet : actions1) {
                aspFacts2.append(sendfleet).append(".\n");
            }



            handler2 = new DesktopHandler(new DLV2DesktopService("lib/dlv.exe"));
            OptionDescriptor option2 = new OptionDescriptor(" --printonlyoptimum");
            handler2.addOption(option2);

            InputProgram executionProgram = new ASPInputProgram();
            String consolidamento_strategy = "encodings/consolidation.txt";
            executionProgram.addFilesPath(consolidamento_strategy);
            handler2.addProgram(executionProgram);

            InputProgram factsProgramPhase2 = new ASPInputProgram();
            factsProgramPhase2.addProgram(aspFacts2.toString());
            handler2.addProgram(factsProgramPhase2);

            Output outputPhase2 = executeDLVWithTimeout(handler2);

            if (outputPhase2 != null) {
                System.out.println("--- DLV Output (Phase 2) per " + player.getName() + " ---");
                System.out.println(outputPhase2.getOutput());
            }

            if (outputPhase2 == null || (outputPhase2.getErrors() != null && !outputPhase2.getErrors().isEmpty())) {
                System.err.println("Errore durante l'esecuzione di EMBASP (Phase 2) per " + player.getName() + ": " +
                        (outputPhase2 != null ? outputPhase2.getErrors() : "output null"));
                return;
            }

            List<String> actions2 = parseAnswerSets2(outputPhase2.getOutput());
            if (!actions2.isEmpty()) {
                executeActionsFromStrings(actions2);
            }
        } finally {
            if (handler1 != null) {
                cleanupHandler(handler1);
            }
            if (handler2 != null) {
                cleanupHandler(handler2);
            }
        }
    }


    public void shutdown() {
        System.out.println("Shutdown AIPlayer per " + player.getName());

        forceCleanupDLVProcesses();

        dlvExecutor.shutdownNow();
        try {
            if (!dlvExecutor.awaitTermination(1, TimeUnit.SECONDS)) {
                System.err.println("Timeout nella chiusura dell'executor per " + player.getName());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        System.out.println("Shutdown completato per " + player.getName());
    }

    public boolean isExecuting() {
        return isExecuting.get();
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
        String[] predicates = {
                "enemy\\(\\d+\\)",
                "enemy_system\\(\\d+,\\d+\\)",
                "connected\\(\\d+,\\d+\\)",
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

        facts.append("me(").append(player.getId()).append("). ");

        if (player.getSystemsLost() != null && includeMetrics) {
            for (StarSystem system : player.getSystemsLost()) {
                facts.append("system_lost(").append(system.getId()).append("). ");
            }
            player.getSystemsLost().clear();
        }


        for (StarSystem system : gameState.getGameMap().getSystems()) {
            facts.append("system(").append(system.getId()).append("). ");
            if (system.getOwner() != null) {
                facts.append("owner(").append(system.getId()).append(",")
                        .append(system.getOwner().getId()).append("). ");
            } else {
                facts.append("neutral_system(").append(system.getId()).append("). ");
            }
            facts.append("ships(").append(system.getId()).append(",")
                    .append(system.getShips()).append("). ");

            if(includeMetrics)
            {
                facts.append("production(").append(system.getId()).append(",")
                        .append(system.getProductionRate()).append("). ");
            }
        }

        for (StarSystem system : gameState.getGameMap().getSystems()) {
            for (StarSystem connected : system.getConnectedSystems()) {
                if (system.getId() < connected.getId()) {
                    facts.append("connected(")
                            .append(system.getId()).append(",")
                            .append(connected.getId()).append("). ");
                }
            }
        }

        for (Fleet fleet : gameState.getGameMap().getFleets()) {
            facts.append("fleet(")
                    .append(fleet.getId()).append(",")
                    .append(fleet.getOwner().getId()).append(",")
                    .append(fleet.getShips()).append(",")
                    .append(fleet.getSource().getId()).append(",")
                    .append(fleet.getDestination().getId()).append("). ");
        }

        return facts.toString();
    }


    private List<String> parseAnswerSets(String dlvOutput) {
        List<String> actions = new ArrayList<>();
        Pattern p = Pattern.compile("send_fleet\\([^)]*\\)");
        Matcher m = p.matcher(dlvOutput);
        while (m.find()) {
            actions.add(m.group());
        }
        System.out.println("Azioni estratte per " + player.getName() + ": " + actions.size());
        return actions;
    }

    private List<String> parseAnswerSets2(String dlvOutput) {
        List<String> actions = new ArrayList<>();
        Pattern p = Pattern.compile("consolidation_fleet\\(([^,]+),([^,]+),([^\\)]+)\\)");
        Matcher m = p.matcher(dlvOutput);
        while (m.find()) {
            String from = m.group(1).trim();
            String to = m.group(2).trim();
            String ships = m.group(3).trim();
            String sendFleet = "send_fleet(" + from + "," + to + "," + ships + ")";
            actions.add(sendFleet);
        }
        System.out.println("Azioni estratte e convertite per " + player.getName() + ": " + actions.size());
        return actions;
    }

    private void executeActionsFromStrings(List<String> actions) {


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
                int ships = Integer.parseInt(params[2].trim());

                StarSystem source = findSystemById(sourceId);
                StarSystem target = findSystemById(targetId);

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
                if (source.getShips() < ships) {
                    System.err.println("Non abbastanza navi: " + atomStr);
                    continue;
                }

                Fleet fleet = gameState.sendFleet(player, source, target, ships);
                if (fleet != null) {
                    executedAny = true;
                    System.out.println("Flotta inviata da " + player.getName() + ": " + atomStr);
                }
            } catch (Exception e) {
                System.err.println("Errore parsing action per " + player.getName() + ": " + atomStr);
                e.printStackTrace();
            }
        }

        if (!executedAny) {
            System.out.println("Nessuna flotta inviata da " + player.getName());
        }
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

