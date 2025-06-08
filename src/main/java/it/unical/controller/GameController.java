package it.unical.controller;

import it.unical.ai.AIPlayer;
import it.unical.gui.GameFrame;
import it.unical.gui.GamePanel;
import it.unical.model.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import javax.swing.*;

public class GameController {

    private GameState gameState;
    private GameFrame gameFrame;
    private InputController inputController;
    private Timer gameTimer;
    private Map<Player, AIPlayer> aiPlayers;
    private final int UPDATE_RATE = 10;
    private int tickCounter = 0;
    private int playerTurn = 0; // Indice per il turno round-robin degli AI
    private int sendPerc = 100;
    private Difficulty difficulty;
    private volatile boolean paused = false;
    private volatile boolean gameRunning = true;

    // Executor per operazioni asincrone del GameController
    private final ExecutorService controllerExecutor = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r, "GameController-Worker");
        t.setDaemon(true);
        return t;
    });


    private int getAITurnInterval() {
        int numAIPlayers = gameState.getPlayers().size() - 1; // Escludi il giocatore umano
        if (numAIPlayers <= 0) return Integer.MAX_VALUE; // Nessun AI
        return 200 / numAIPlayers; // Come nel codice originale
    }

    public GameController(Difficulty difficulty) {
        this.difficulty = difficulty;
        gameState = new GameState(new Dimension(1600, 900));
        inputController = new InputController(this);
        aiPlayers = new HashMap<>();
        gameTimer = new Timer(UPDATE_RATE, e -> update());
    }

    public void showPauseDialog() {
        pauseGame();

        // Creo un JDialog modale legato al gameFrame
        JDialog pauseDialog = new JDialog(gameFrame, "Pausa", Dialog.ModalityType.APPLICATION_MODAL);
        pauseDialog.setUndecorated(true);

        // Pannello con messaggio
        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(new Color(30, 30, 60));
        content.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel label = new JLabel(
                "<html><div style='text-align: center;'>"
                        + "Gioco in pausa.<br>"
                        + "Premi <b>P</b> per riprendere.<br>"
                        + "Premi <b>ESC</b> per tornare al menu principale."
                        + "</div></html>",
                SwingConstants.CENTER
        );
        label.setForeground(Color.WHITE);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 16f));
        content.add(label, BorderLayout.CENTER);

        pauseDialog.setContentPane(content);
        pauseDialog.pack();

        // Calcolo posizione: orizzontalmente centrato, 20px sotto la barra del frame
        Point frameLoc = gameFrame.getLocationOnScreen();
        int frameX = frameLoc.x;
        int frameY = frameLoc.y;
        int frameW = gameFrame.getWidth();

        int dialogW = pauseDialog.getWidth();
        int x = frameX + (frameW - dialogW) / 2;
        int y = frameY + 60; // 20px di margine dal top
        pauseDialog.setLocation(x, y);

        // Key bindings
        JRootPane root = pauseDialog.getRootPane();
        InputMap im = root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = root.getActionMap();

        im.put(KeyStroke.getKeyStroke('P'), "resume");
        im.put(KeyStroke.getKeyStroke('p'), "resume");
        am.put("resume", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                pauseDialog.dispose();
                resumeGame();
            }
        });

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "toMenu");
        am.put("toMenu", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                pauseDialog.dispose();
                backToMainMenu();
            }
        });

        // Mostro il dialog; questo blocca finché non viene dispose()
        pauseDialog.setVisible(true);
    }


    private void pauseGame() {
        gameTimer.stop();
        paused = true;
        System.out.println("Gioco messo in pausa");
    }

    private void resumeGame() {
        if (gameRunning) {
            gameTimer.start();
            paused = false;
            System.out.println("Gioco ripreso");
        }
    }


    private void shutdownAIPlayers() {
        if (aiPlayers.isEmpty()) return;

        System.out.println("Iniziando la chiusura di " + aiPlayers.size() + " AIPlayer...");

        List<Future<Void>> shutdownTasks = new ArrayList<>();

        for (Map.Entry<Player, AIPlayer> entry : aiPlayers.entrySet()) {
            Future<Void> task = controllerExecutor.submit(() -> {
                try {
                    AIPlayer aiPlayer = entry.getValue();
                    System.out.println("Chiudendo AIPlayer per " + entry.getKey().getName());
                    aiPlayer.shutdown();
                    System.out.println("AIPlayer chiuso per " + entry.getKey().getName());
                    return null;
                } catch (Exception e) {
                    System.err.println("Errore durante la chiusura di AIPlayer per " +
                            entry.getKey().getName() + ": " + e.getMessage());
                    return null;
                }
            });
            shutdownTasks.add(task);
        }

        for (Future<Void> task : shutdownTasks) {
            try {
                task.get(3, TimeUnit.SECONDS);
            } catch (TimeoutException e) {
                System.err.println("Timeout durante la chiusura di un AIPlayer");
                task.cancel(true);
            } catch (Exception e) {
                System.err.println("Errore durante l'attesa della chiusura AIPlayer: " + e.getMessage());
            }
        }

        aiPlayers.clear();
        System.out.println("Tutti gli AIPlayer sono stati chiusi");
    }



    public void initGame() {
        controllerExecutor.submit(() -> {
            try {
                initGameAsync();
            } catch (Exception e) {
                System.err.println("Errore durante l'inizializzazione del gioco: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    private void initGameAsync() {
        boolean withAI = true;

        int numSystem;
        if (difficulty instanceof Difficulty.Easy) {
            numSystem = 10;
        } else if (difficulty instanceof Difficulty.Medium) {
            numSystem = 20;
        } else {
            numSystem = 30;
        }

        gameState.initGame(difficulty, numSystem, withAI);

        // Crea gli AIPlayer per ogni giocatore IA in modo asincrono
        List<Player> aiPlayersList = gameState.getAiPlayers();
        List<Future<Void>> initTasks = new ArrayList<>();

        for (Player aiPlayer : aiPlayersList) {
            Future<Void> task = controllerExecutor.submit(() -> {
                try {
                    System.out.println("Inizializzando AIPlayer per " + aiPlayer.getName());
                    AIPlayer aiPlayerInstance = new AIPlayer(aiPlayer, gameState, difficulty);
                    synchronized (aiPlayers) {
                        this.aiPlayers.put(aiPlayer, aiPlayerInstance);
                    }
                    System.out.println("AIPlayer inizializzato per " + aiPlayer.getName());
                    return null;
                } catch (Exception e) {
                    System.err.println("Errore nell'inizializzazione AIPlayer per " +
                            aiPlayer.getName() + ": " + e.getMessage());
                    e.printStackTrace();
                    return null;
                }
            });
            initTasks.add(task);
        }

        // Attendi che tutti gli AIPlayer siano inizializzati
        for (Future<Void> task : initTasks) {
            try {
                task.get(5, TimeUnit.SECONDS);
            } catch (Exception e) {
                System.err.println("Errore o timeout nell'inizializzazione AIPlayer: " + e.getMessage());
            }
        }

        SwingUtilities.invokeLater(() -> {
            if (gameFrame != null && gameFrame.getGamePanel() != null) {
                gameFrame.getGamePanel().updateSystemViews();
                System.out.println("Aggiornate le viste dei sistemi: " +
                        gameState.getGameMap().getSystems().size() + " sistemi trovati");
            }
        });

        SwingUtilities.invokeLater(() -> {
            if (gameRunning) {
                gameTimer.start();
                System.out.println("Timer del gioco avviato");

                // Calcola e stampa la frequenza AI
                int aiTurnInterval = getAITurnInterval();
                int frequencyMs = aiTurnInterval * UPDATE_RATE;
                System.out.println("AI turni ogni " + aiTurnInterval + " ticks (" + frequencyMs + "ms) per " +
                        aiPlayersList.size() + " giocatori AI");
            }
        });
    }

    public void setGameFrame(GameFrame gameFrame) {
        this.gameFrame = gameFrame;
        if (this.gameFrame != null) {
            this.gameFrame.setVisible(true);
        }
    }

    private void update() {
        if (!gameRunning || paused) return;

        tickCounter++;

        // Aggiorna lo stato del gioco
        try {
            gameState.updateGameState();
            gameState.getGameMap().updateFleets(0.3);
        } catch (Exception e) {
            System.err.println("Errore durante l'aggiornamento dello stato del gioco: " + e.getMessage());
        }

        // **GESTIONE AI CON ROUND-ROBIN E TIMING CORRETTO**
        // Solo se ci sono giocatori AI
        if (!aiPlayers.isEmpty()) {
            int aiTurnInterval = getAITurnInterval();

            if (tickCounter % aiTurnInterval == 0) {
                handleAIPlayerTurn();
            }
        }

        // Aggiorna l'interfaccia grafica su EDT
        if (gameFrame != null) {
            SwingUtilities.invokeLater(() -> {
                try {
                    gameFrame.repaint();
                    if (gameFrame.getGamePanel() != null) {
                        gameFrame.updateUI();
                    }
                } catch (Exception e) {
                    System.err.println("Errore durante il repaint: " + e.getMessage());
                }
            });
        }

        checkGameOver();
    }

    /**
     * Gestisce il turno di un singolo AI player in modo round-robin
     */
    private void handleAIPlayerTurn() {
        List<Player> aiPlayersList = gameState.getAiPlayers();

        if (aiPlayersList.isEmpty()) return;

        // Calcola quale AI deve giocare questo turno (round-robin)
        Player currentAIPlayer = aiPlayersList.get(playerTurn);
        AIPlayer aiPlayerInstance = aiPlayers.get(currentAIPlayer);

        if (aiPlayerInstance != null && (!currentAIPlayer.getOwnedSystems().isEmpty() || currentAIPlayer.getTotalShips()!=0)) {
            // Esegui il turno in modo asincrono
            controllerExecutor.submit(() -> {
                try {
                    if (!aiPlayerInstance.isExecuting()) {
                        System.out.println("Turno AI: " + currentAIPlayer.getName() +
                                " (tick: " + tickCounter + ")");
                        aiPlayerInstance.performTurn();
                    } else {
                        System.out.println("AI " + currentAIPlayer.getName() +
                                " ancora in esecuzione, salto turno");
                    }
                } catch (Exception e) {
                    System.err.println("Errore durante il turno AI per " +
                            currentAIPlayer.getName() + ": " + e.getMessage());
                    e.printStackTrace();
                }
            });
        }

        // Passa al prossimo AI player (round-robin)
        playerTurn = (playerTurn + 1) % aiPlayersList.size();
    }


    // ==================== GETTERS E SETTERS ====================

    public boolean isGameRunning() {
        return gameRunning;
    }

    public boolean isPaused() {
        return paused;
    }

    public GameState getGameState() {
        return gameState;
    }

    public void setGameState(GameState gameState) {
        this.gameState = gameState;
    }

    public GameFrame getGameFrame() {
        return gameFrame;
    }

    public GamePanel getGamePanel() {
        if (gameFrame != null) {
            return gameFrame.getGamePanel();
        }
        return null;
    }

    public InputController getInputController() {
        return inputController;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    public Map<Player, AIPlayer> getAiPlayers() {
        synchronized (aiPlayers) {
            return new HashMap<>(aiPlayers);
        }
    }

    public Timer getGameTimer() {
        return gameTimer;
    }

    public int getTickCounter() {
        return tickCounter;
    }

    public int getUpdateRate() {
        return UPDATE_RATE;
    }

    public int getSendPerc() {
        return sendPerc;
    }

    public void setSendPerc(int sendPerc) {
        this.sendPerc = sendPerc;
    }

    // Nel GameController, aggiungi questo metodo al shutdown:

    private void forceKillAllDLVProcesses() {
        try {
            if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                ProcessBuilder pb = new ProcessBuilder("taskkill", "/F", "/IM", "dlv.exe");
                Process killProcess = pb.start();
                killProcess.waitFor(3, TimeUnit.SECONDS);
                System.out.println("Terminati tutti i processi DLV rimasti");
            } else {
                ProcessBuilder pb = new ProcessBuilder("pkill", "-f", "dlv");
                Process killProcess = pb.start();
                killProcess.waitFor(3, TimeUnit.SECONDS);
                System.out.println("Terminati tutti i processi DLV rimasti");
            }
        } catch (Exception e) {
            System.err.println("Errore durante terminazione globale processi DLV: " + e.getMessage());
        }
    }




    // Aggiungi un flag per controllare se il controller è in fase di shutdown
    private volatile boolean isShuttingDown = false;

    // Modifica il metodo sendFleet per controllare lo stato prima di sottomettere task
    public void sendFleet(StarSystem source, StarSystem target, int ships) {

        if (!canSubmitTasks()) {
            return; // Silenzioso se in shutdown
        }
        // Controlla se siamo in shutdown prima di sottomettere il task
        if (isShuttingDown || controllerExecutor.isShutdown()) {
            System.out.println("Tentativo di inviare flotta ignorato: GameController in shutdown");
            return;
        }

        try {
            controllerExecutor.submit(() -> {
                try {
                    // Doppio controllo all'interno del task
                    if (isShuttingDown || !gameRunning) {
                        return;
                    }

                    int shipsToSend = Math.min(ships, source.getShips() - 1);
                    if (shipsToSend > 0 && !paused) {
                        Player humanPlayer = gameState.getHumanPlayer();
                        gameState.sendFleet(humanPlayer, source, target, shipsToSend);
                        System.out.println("Flotta inviata: " + shipsToSend + " navi da sistema " +
                                source.getId() + " a sistema " + target.getId());
                    }
                } catch (Exception e) {
                    if (!isShuttingDown) { // Non loggare errori durante shutdown
                        System.err.println("Errore durante l'invio della flotta: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            });
        } catch (RejectedExecutionException e) {
            System.out.println("Task rifiutato: GameController già in shutdown");
        }
    }

    // Modifica checkGameOver per essere più robusto
    private void checkGameOver() {
        if (gameState.isGameOver() && !isShuttingDown) {
            // Controlla se siamo già in shutdown per evitare chiamate multiple
            if (isShuttingDown || controllerExecutor.isShutdown()) {
                return;
            }

            try {
                controllerExecutor.submit(() -> {
                    if (isShuttingDown) return; // Doppio controllo

                    stopGame();

                    SwingUtilities.invokeLater(() -> {
                        String message;
                        if (gameState.getWinner() != null) {
                            String winner = gameState.getWinner().getName();
                            message = "Game Over! Ha vinto: " + winner;
                            System.out.println("Game Over! Il vincitore è: " + winner);
                        } else {
                            message = "Game Over! Hai perso :(";
                            System.out.println("Game Over! Mi dispiace. Hai perso la partita");
                        }

                        JOptionPane.showMessageDialog(
                                gameFrame,
                                message,
                                "Game Over",
                                JOptionPane.INFORMATION_MESSAGE
                        );

                        if (gameFrame != null) {
                            gameFrame.dispose();
                        }
                        new it.unical.gui.MainMenuFrame();
                    });
                });
            } catch (RejectedExecutionException e) {
                System.out.println("Task GameOver rifiutato: già in shutdown");
                // Esegui direttamente su EDT come fallback
                SwingUtilities.invokeLater(() -> {
                    if (gameFrame != null) {
                        gameFrame.dispose();
                    }
                    new it.unical.gui.MainMenuFrame();
                });
            }
        }
    }

    // Modifica backToMainMenu per essere più robusto
    public void backToMainMenu() {
        if (isShuttingDown) {
            return; // Evita chiamate multiple
        }

        try {
            controllerExecutor.submit(() -> {
                try {
                    stopGame();

                    SwingUtilities.invokeLater(() -> {
                        if (gameFrame != null) {
                            gameFrame.dispose();
                        }
                        new it.unical.gui.MainMenuFrame();
                    });

                } catch (Exception e) {
                    if (!isShuttingDown) {
                        System.err.println("Errore durante il ritorno al menu principale: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            });
        } catch (RejectedExecutionException e) {
            System.out.println("Task backToMainMenu rifiutato: già in shutdown");
            // Esegui direttamente come fallback
            SwingUtilities.invokeLater(() -> {
                if (gameFrame != null) {
                    gameFrame.dispose();
                }
                new it.unical.gui.MainMenuFrame();
            });
        }
    }



    // Modifica stopGame con shutdown più ordinato
    private void stopGame() {
        // Imposta il flag di shutdown per prima cosa
        isShuttingDown = true;
        gameRunning = false;

        System.out.println("Iniziando shutdown del GameController...");

        // Ferma il timer
        if (gameTimer != null && gameTimer.isRunning()) {
            gameTimer.stop();
            System.out.println("Timer del gioco fermato");
        }

        // Chiudi gli AI players
        shutdownAIPlayers();

        // Cleanup globale DLV
        forceKillAllDLVProcesses();

        // Chiudi l'executor con timeout
        shutdownExecutors();

        System.out.println("Gioco fermato completamente");
    }

    // Modifica shutdownExecutors per essere più ordinato
    private void shutdownExecutors() {
        System.out.println("Chiudendo executor del GameController...");

        controllerExecutor.shutdown(); // Impedisce nuovi task

        controllerExecutor.shutdownNow(); // Forza l'interruzione

        System.out.println("Executor del GameController chiuso");
    }

    // Aggiungi un metodo per controllare se è safe sottomettere task
    public boolean canSubmitTasks() {
        return !isShuttingDown && !controllerExecutor.isShutdown() && gameRunning;
    }
}
