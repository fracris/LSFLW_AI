package it.unical.model;

import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import it.unical.mat.embasp.base.Handler;
import it.unical.mat.embasp.base.InputProgram;
import it.unical.mat.embasp.base.Output;
import it.unical.mat.embasp.languages.asp.ASPInputProgram;
import it.unical.mat.embasp.platforms.desktop.DesktopHandler;
import it.unical.mat.embasp.specializations.dlv2.desktop.DLV2DesktopService;

public class AIPlayer {
    private Player player;
    private GameState gameState;
    private String aspStrategy = "encodings/basic_strategy.txt";
    private boolean isInitialized;

    public AIPlayer(Player player, GameState gameState) {
        this.player = player;
        this.gameState = gameState;

        if (!player.isAI()) {
            System.err.println("ATTENZIONE: AIPlayer assegnato a un giocatore non IA");
            return;
        }

        File aspFile = new File(aspStrategy);
        if (!aspFile.exists()) {
            System.err.println("ERRORE: File di strategia ASP non trovato in: " + aspStrategy);
            return;
        }

        this.isInitialized = true;
    }

    public void performTurn() {
        if (!isInitialized) {
            System.err.println("AIPlayer non inizializzato, impossibile eseguire il turno");
            return;
        }

        try {
            // Genera i fatti ASP
            String aspFacts = convertGameStateToASP();

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

            Output output = handler.startSync();

            if (output == null || output.getErrors() != null && !output.getErrors().isEmpty()) {
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
        } catch (Exception e) {
            System.err.println("Errore durante il turno IA: " + e.getMessage());
            e.printStackTrace();
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
