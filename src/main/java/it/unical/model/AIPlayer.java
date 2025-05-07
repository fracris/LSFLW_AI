package it.unical.model;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AIPlayer {
    private Player player;
    private GameState gameState;
    private String dlv2Path;
    private String aspStrategy;
    private boolean isInitialized;
    private int timeout = 10; // Timeout in secondi

    public AIPlayer(Player player, GameState gameState, String dlv2Path, String aspStrategy) {
        this.player = player;
        this.gameState = gameState;
        this.dlv2Path = dlv2Path;
        this.aspStrategy = aspStrategy;

        if (!player.isAI()) {
            System.err.println("ATTENZIONE: AIPlayer assegnato a un giocatore non IA");
            return;
        }

        File dlv2File = new File(dlv2Path);
        if (!dlv2File.exists()) {
            System.err.println("ERRORE: DLV2 non trovato in: " + dlv2Path);
            return;
        }

        File aspFile = new File(aspStrategy);
        if (!aspFile.exists()) {
            System.err.println("ERRORE: File di strategia ASP non trovato in: " + aspStrategy);
            return;
        }

        this.isInitialized = true;
        //System.out.println("AIPlayer inizializzato per " + player.getName());
    }

    public boolean performTurn() {
        if (!isInitialized) {
            System.err.println("AIPlayer non inizializzato, impossibile eseguire il turno");
            return false;
        }

        try {
            //System.out.println("Esecuzione del turno IA per " + player.getName());

            // Genera i fatti ASP
            String aspFacts = convertGameStateToASP();
            //System.out.println("=== Fatti ASP per IA ===\n" + aspFacts);

            // Esecuzione diretta del comando DLV con i parametri corretti
            ProcessBuilder pb = new ProcessBuilder(
                    dlv2Path,
                    "--stdin",
                    aspStrategy
            );
            pb.redirectErrorStream(true); // Combina stderr con stdout per semplificare la lettura

            System.out.println("Esecuzione comando: " + String.join(" ", pb.command()));

            Process process = pb.start();

            // Invio dei fatti ASP allo standard input del processo DLV
            try (OutputStreamWriter writer = new OutputStreamWriter(process.getOutputStream())) {
                writer.write(aspFacts);
                writer.flush();
            }

            // Lettura dell'output con timeout
            StringBuilder output = new StringBuilder();
            ExecutorService executor = Executors.newSingleThreadExecutor();
            Future<String> future = executor.submit(() -> {
                StringBuilder sb = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line).append("\n");
                    }
                }
                return sb.toString();
            });

            try {
                String result = future.get(timeout, TimeUnit.SECONDS);
                output.append(result);
            } catch (TimeoutException e) {
                future.cancel(true);
                process.destroyForcibly();
                System.err.println("ERRORE: Timeout durante l'esecuzione di DLV dopo " + timeout + " secondi");
                return false;
            } finally {
                executor.shutdownNow();
            }

            // Stampa l'output completo per debug
            String result = output.toString();
            System.out.println("=== Output DLV completo ===\n" + result);

            // Estrai gli answerset dall'output
            List<String> actions = parseAnswerSets(result);
            if (!actions.isEmpty()) {
                //System.out.println("=== Azioni estratte ===");
                for (String action : actions) {
                    //System.out.println(action);
                }
                return executeActionsFromStrings(actions);
            } else {
                System.out.println("Nessun answer set valido trovato per " + player.getName());
                return false;
            }
        } catch (Exception e) {
            System.err.println("Errore durante il turno IA: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }


    private List<String> parseAnswerSets(String dlvOutput) {
        List<String> actions = new ArrayList<>();
        // Pattern che cattura send_fleet(arg1,arg2,arg3)
        Pattern p = Pattern.compile("send_fleet\\([^)]*\\)");
        Matcher m = p.matcher(dlvOutput);
        while (m.find()) {
            actions.add(m.group());
        }
        System.out.println("Azioni estratte: " + actions.size());
        return actions;
    }


    private boolean executeActionsFromStrings(List<String> actions) {
        boolean actionsExecuted = false;

        for (String atomStr : actions) {
            try {
                // Estrai i parametri da send_fleet(source,target,ships)
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
                    /*
                    System.err.println("Il sistema " + sourceId + " non ha abbastanza navi: " +
                            source.getShips() + " < " + ships);

                     */
                    continue;
                }

                Fleet fleet = gameState.sendFleet(player, source, target, ships);
                if (fleet != null) {
                    /*
                    System.out.println("IA " + player.getName() +
                            " invia " + ships + " navi da " +
                            source.getName() + " a " + target.getName());

                     */
                    return true;
                }
            } catch (Exception e) {
                System.err.println("Errore nell'interpretazione dell'azione: " + atomStr);
                e.printStackTrace();
            }
        }

        return actionsExecuted;
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
                    .append((int)fleet.getProgress()).append(").\n");
        }

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

    // Metodo per impostare il timeout (opzionale)
    public void setTimeout(int seconds) {
        this.timeout = seconds;
    }
}
