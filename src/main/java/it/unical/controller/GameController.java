package it.unical.controller;

import it.unical.gui.GameFrame;
import it.unical.gui.GamePanel;
import it.unical.gui.StarSystemView;
import it.unical.model.Fleet;
import it.unical.model.GameState;
import it.unical.model.Player;
import it.unical.model.StarSystem;

import java.awt.Dimension;
import java.awt.Point;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import javax.swing.Timer;

public class GameController {
    private GameState gameState;
    private GameFrame gameFrame;
    private InputController inputController;
    private Timer gameTimer;
    private Timer aiTimer;
    private static final int UPDATE_RATE = 33;      // Milliseconds
    private static final int AI_MOVE_RATE = 1000;     // Milliseconds per AI move

    public GameController() {
        // Crea lo stato di gioco
        gameState = new GameState(new Dimension(1600, 900));

        // Crea il controller di input
        inputController = new InputController(this);

        // Timer per aggiornamenti generali (flotte + GUI)
        gameTimer = new Timer(UPDATE_RATE, e -> update());
        // Timer dedicato per le mosse dell'IA
        aiTimer = new Timer(AI_MOVE_RATE, e -> performAIMove());
        aiTimer.setRepeats(true);
    }

    public GameFrame getGameFrame() {
        return gameFrame;
    }

    public void setGameFrame(GameFrame gameFrame) {
        this.gameFrame = gameFrame;
    }

    // Inizializza e avvia il gioco
    public void initGame() {
        gameState.initGame(20, 2, true); // 20 sistemi, 2 giocatori, con IA

        // Crea e mostra la finestra di gioco
        gameFrame = new GameFrame("Little Stars for Little Wars", this);
        gameFrame.setVisible(true);

        // Avvia i timer
        gameTimer.start();
        aiTimer.start();
    }

    // Aggiorna movimenti flotte e GUI
    private void update() {
        gameState.getGameMap().updateFleets(1.0);
        gameState.updateGameState();
        if (gameFrame != null) {
            gameFrame.updateUI();
        }
    }

    // Mossa dell'IA: chiamato periodicamente dall'aiTimer
    private void performAIMove() {
        for (Player ai : gameState.getAiPlayers()) {
            StarSystem source = chooseSourceForAI(ai);
            if (source == null) continue;
            StarSystem target = chooseTargetForAI(source);
            int ships = decideNumShips(source);
            if (target != null && ships > 0) {
                gameState.sendFleet(ai, source, target, ships);
            }
        }
    }

    // Selettore sistema sorgente per IA: prende un sistema con navi sufficienti
    private StarSystem chooseSourceForAI(Player ai) {
        List<StarSystem> candidates = ai.getOwnedSystems().stream()
                .filter(s -> s.getShips() > 5)
                .collect(Collectors.toList());
        if (candidates.isEmpty()) return null;
        int idx = ThreadLocalRandom.current().nextInt(candidates.size());
        return candidates.get(idx);
    }

    // Selettore sistema bersaglio per IA: sceglie un vicino non di sua proprietà
    private StarSystem chooseTargetForAI(StarSystem source) {
        List<StarSystem> neighbors = source.getConnectedSystems().stream()
                .filter(s -> s.getOwner() != source.getOwner())
                .collect(Collectors.toList());
        if (neighbors.isEmpty()) return null;
        int idx = ThreadLocalRandom.current().nextInt(neighbors.size());
        return neighbors.get(idx);
    }

    // Decide quante navi inviare: metà di quelle presenti (almeno 1)
    private int decideNumShips(StarSystem source) {
        int available = source.getShips();
        return available > 1 ? available / 2 : 0;
    }

    // Invia una flotta da un sistema a un altro (usato sia da UI che da IA)
    public Fleet sendFleet(StarSystem source, StarSystem target, int ships) {
        return gameState.sendFleet(source.getOwner(), source, target, ships);
    }

//    // Restituisce la vista di un sistema sotto un punto specifico, o null
//    public StarSystemView getSystemViewAt(Point p) {
//        return gameFrame.getGamePanel().getSystemViews().stream()
//                .filter(v -> v.contains(p))
//                .findFirst()
//                .orElse(null);
//    }

//    // Gestisce il click sulla mappa: seleziona sorgente o target
//    public void handleMapClick(Point p) {
//        StarSystemView view = getSystemViewAt(p);
//        if (view == null) return;
//        StarSystem sys = view.getSystem();
//        StarSystem selected = gameFrame.getGamePanel().getSelectedSystem();
//        if (selected == null && sys.getOwner() == gameState.getHumanPlayer()) {
//            gameFrame.getGamePanel().setSelectedSystem(sys);
//        } else {
//            gameFrame.getGamePanel().setTargetSystem(sys);
//        }
//        gameFrame.getControlPanel().updateControls();
//    }

    // Getters per GUI e input
    public GameState getGameState() {
        return gameState;
    }

    public GamePanel getGamePanel() {
        return gameFrame.getGamePanel();
    }

    public InputController getInputController() {
        return inputController;
    }
}
